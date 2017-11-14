/*===========================================================================
  Copyright (C) 2010-2013 by the Okapi Framework contributors
-----------------------------------------------------------------------------
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
===========================================================================*/

package net.sf.okapi.filters.idml;

import com.ctc.wstx.api.WstxInputProperties;
import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.FileUtil;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.MimeTypeMapper;
import net.sf.okapi.common.StreamUtil;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.filters.FilterConfiguration;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filters.IFilterConfigurationMapper;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.skeleton.GenericSkeleton;
import net.sf.okapi.common.skeleton.ISkeletonWriter;
import net.sf.okapi.common.skeleton.ZipSkeleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipException;

import static net.sf.okapi.common.Util.toURI;

@UsingParameters(Parameters.class)
public class IDMLFilter implements IFilter {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String START_DOCUMENT_ID = "sd";
    private static final String END_DOCUMENT_ID = "ed";

    private Parameters params;

    private XMLInputFactory inputFactory;
    private XMLOutputFactory outputFactory;
    private XMLEventFactory eventFactory;

    private NextAction nextAction;

    private RawDocument rawDocument;
    private EncoderManager encoderManager;

    private File tempFile;
    private URI documentUri;

    private Document document;
    private SubDocument subDocument;

    private LinkedList<Event> queue;

    public IDMLFilter() {
        params = new Parameters();

        inputFactory = XMLInputFactory.newInstance();
        outputFactory = XMLOutputFactory.newInstance();
        eventFactory = XMLEventFactory.newInstance();

        // security concern. Turn off DTD processing
        // https://www.owasp.org/index.php/XML_External_Entity_%28XXE%29_Processing
        inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);

        if (inputFactory.isPropertySupported(WstxInputProperties.P_MAX_ATTRIBUTE_SIZE)) {
            inputFactory.setProperty(WstxInputProperties.P_MAX_ATTRIBUTE_SIZE, params.getMaxAttributeSize());
        }
    }

    @Override
    public void close() {
        if (rawDocument != null) {
            rawDocument.close();
        }

        if (tempFile != null) {
            tempFile.delete();
        }

        try {
            nextAction = NextAction.DONE;
            if (document != null) {
                document.close();
                document = null;
            }
        } catch (IOException e) {
            throw new OkapiIOException("Error closing zipped output file.");
        }

        if (subDocument != null) {
            subDocument.close();
        }
    }

    @Override
    public ISkeletonWriter createSkeletonWriter() {
        return null; // There is no corresponding skeleton writer
    }

    @Override
    public IFilterWriter createFilterWriter() {
        return new IDMLFilterWriter(params, outputFactory, eventFactory);
    }

    @Override
    public EncoderManager getEncoderManager() {
        if (encoderManager == null) {
            encoderManager = new EncoderManager();
            encoderManager.setMapping(MimeTypeMapper.XML_MIME_TYPE, "net.sf.okapi.common.encoder.XMLEncoder");
        }
        return encoderManager;
    }

    @Override
    public String getName() {
        return "okf_idml";
    }

    @Override
    public String getDisplayName() {
        return "IDML Filter";
    }

    @Override
    public String getMimeType() {
        return Document.MIME_TYPE;
    }

    @Override
    public List<FilterConfiguration> getConfigurations() {
        List<FilterConfiguration> list = new ArrayList<FilterConfiguration>();
        list.add(new FilterConfiguration(getName(),
                Document.MIME_TYPE,
                getClass().getName(),
                "IDML",
                "Adobe InDesign IDML documents",
                null,
                ".idml;"));

        return list;
    }

    @Override
    public Parameters getParameters() {
        return params;
    }

    @Override
    public void setParameters(IParameters params) {
        this.params = (Parameters) params;
    }

    @Override
    public boolean hasNext() {
        return (((queue != null) && (!queue.isEmpty())) || (nextAction != NextAction.DONE));
    }

    @Override
    public Event next() {
        if (queue == null) {
            return null;
        }

        if (queue.size() > 0) {
            return queue.poll();
        }

        try {
            // When the queue is empty: process next action
            switch (nextAction) {
                case OPEN_DOCUMENT:
                    return openDocument();
                case NEXT_IN_DOCUMENT:
                    return nextInDocument();
                case NEXT_IN_SUB_DOCUMENT:
                    Event e = nextInSubDocument();
                    if (e != null) {
                        return e;
                    }
                    // That subdoc is done; call another.  XXX This is hacky
                    // since it's a special case for handling NonTranslatablePartHandler;
                    // things that call real subfilters produce END_DOCUMENT stuff that
                    // is handled a different way.
                    nextAction = NextAction.NEXT_IN_DOCUMENT;
                    return next();
                default:
                    throw new OkapiException("Invalid next() call.");
            }
        } catch (IOException | XMLStreamException e) {
            throw new OkapiException("An error occurred during extraction", e);
        }
    }

    @Override
    public void open(RawDocument input) {
        open(input, true);
    }

    @Override
    public void open(RawDocument rawDocument, boolean generateSkeleton) {
        if (rawDocument == null) {
            throw new OkapiException("RawDocument is null");
        }

        // keep reference so we can clean up
        this.rawDocument = rawDocument;

        if (rawDocument.getInputURI() != null) {
            open(rawDocument.getInputURI());
            logger.debug("\nOpening {}", rawDocument.getInputURI().toString());
        } else if (rawDocument.getStream() != null) {
            open(rawDocument.getStream());
        } else {
            throw new OkapiException("InputResource has no input defined.");
        }
    }

    /**
     * Opens an input stream for filtering
     *
     * @param input an input stream to open and filter
     */
    void open(InputStream input) {
        // Create a temp file for the stream content
        tempFile = FileUtil.createTempFile("~okapi-23_IDMLFilter_");
        StreamUtil.copy(input, tempFile);
        open(toURI(tempFile.getAbsolutePath()));
    }

    void open(URI inputURI) {
        documentUri = inputURI;
        nextAction = NextAction.OPEN_DOCUMENT;
        queue = new LinkedList<>();
        logger.debug("\nOpening {}", inputURI.toString());
    }

    @Override
    public void setFilterConfigurationMapper(IFilterConfigurationMapper fcMapper) {
    }

    private Event openDocument() {
        try {
            document = new Document(params, inputFactory, eventFactory);
            nextAction = NextAction.NEXT_IN_DOCUMENT;

            return document.open(START_DOCUMENT_ID, documentUri, rawDocument.getSourceLocale(), rawDocument.getEncoding(), createFilterWriter());
        } catch (ZipException e) {
            throw new OkapiIOException("Error opening zipped input file.");
        } catch (IOException e) {
            throw new OkapiIOException("Error reading zipped input file.", e);
        } catch (XMLStreamException e) {
            throw new OkapiIOException("Error parsing XML content", e);
        }
    }

    private Event nextInDocument() throws IOException, XMLStreamException {

        while (document.hasNextSubDocument()) {
            subDocument = document.nextSubDocument();
            nextAction = NextAction.NEXT_IN_SUB_DOCUMENT;

            return subDocument.open();
        }

        close();
        Ending ending = new Ending(END_DOCUMENT_ID);

        return new Event(EventType.END_DOCUMENT, ending);
    }

    /**
     * Returns the next subdocument event.  If it is a TEXT_UNIT event,
     * it invokes the translator to manipulate the text before sending
     * on the event.  If it is an END_DOCUMENT event, it sends on
     * an END_SUBDOCUMENT event instead.
     *
     * @return a subdocument event
     */
    private Event nextInSubDocument() {
        Event event;

        while (subDocument.hasNextEvent()) {
            event = subDocument.nextEvent();

            switch (event.getEventType()) {
                case END_DOCUMENT:
                    // Change the END_DOCUMENT to END_SUBDOCUMENT
                    Ending ending = new Ending(subDocument.getId());
                    nextAction = NextAction.NEXT_IN_DOCUMENT;
                    ZipSkeleton skel = new ZipSkeleton(
                            (GenericSkeleton) event.getResource().getSkeleton(), subDocument.getZipFile(), subDocument.getZipEntry());
                    subDocument.close();
                    return new Event(EventType.END_SUBDOCUMENT, ending, skel);
                case DOCUMENT_PART:
                case TEXT_UNIT:
                case START_GROUP:
                case START_SUBFILTER:
                    // purposely falls through to default
                default: // Else: just pass the event through
                    subDocument.logEvent(event);
                    return event;
            }
        }
        // We can fall through to here if a part handler runs out of events.
        return null;
    }

    public void cancel() {
    }

    private enum NextAction {

        OPEN_DOCUMENT,
        NEXT_IN_DOCUMENT,
        NEXT_IN_SUB_DOCUMENT,
        DONE
    }
}
