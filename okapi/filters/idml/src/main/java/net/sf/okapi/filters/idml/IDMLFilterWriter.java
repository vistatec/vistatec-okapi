/*===========================================================================
  Copyright (C) 2010-2017 by the Okapi Framework contributors
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

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.MimeTypeMapper;
import net.sf.okapi.common.StreamUtil;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.exceptions.OkapiNotImplementedException;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.DocumentPart;
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.StartSubDocument;
import net.sf.okapi.common.skeleton.ISkeletonWriter;
import net.sf.okapi.common.skeleton.ZipSkeleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class IDMLFilterWriter implements IFilterWriter {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Parameters parameters;
    private final XMLOutputFactory outputFactory;
    private final XMLEventFactory eventFactory;

    private String outputPath;
    private ZipFile zipOriginal;
    private ZipOutputStream zipOutputStream;
    private byte[] buffer;
    private LocaleId sourceLocale;
    private LocaleId targetLocale;
    private File tempFile;
    private EncoderManager encoderManager;
    private ZipEntry subDocEntry;
    private IFilterWriter subDocWriter;
    private TreeMap<Integer, SubDocumentValues> tmSubDoc = new TreeMap<>();
    private int ndxSubDoc = 0;
    private OutputStream outputStream;

    IDMLFilterWriter(Parameters parameters, XMLOutputFactory outputFactory, XMLEventFactory eventFactory) {
        this.parameters = parameters;
        this.outputFactory = outputFactory;
        this.eventFactory = eventFactory;
    }

    @Override
    public void cancel() {
    }

    @Override
    public void close() {
        if (zipOutputStream == null) {
            return;
        }

        try {
            if (zipOriginal != null) {
                zipOriginal.close();
                zipOriginal = null;
            }

            // Close the output
            zipOutputStream.close();
            zipOutputStream = null;

            // If it was in a temporary file, copy it over the existing one
            // If the IFilter.close() is called before IFilterWriter.close()
            // this should allow to overwrite the input.
            if (tempFile != null) {
                StreamUtil.copy(new FileInputStream(tempFile), outputPath);
                tempFile.delete();
            }
            buffer = null;
        } catch (IOException e) {
            throw new OkapiIOException("Error closing IDML output.\n" + e.getMessage(), e);
        }
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
    public ISkeletonWriter getSkeletonWriter() {
        return null;
    }

    @Override
    public String getName() {
        return "IDMLFilterWriter";
    }

    @Override
    public IParameters getParameters() {
        return parameters;
    }

    @Override
    public void setParameters(IParameters params) {
        // Not used
    }

    @Override
    public Event handleEvent(Event event) {
        switch (event.getEventType()) {
            case START_DOCUMENT:
                processStartDocument(event.getStartDocument());
                break;
            case DOCUMENT_PART:
                processDocumentPart(event);
                break;
            case END_DOCUMENT:
                processEndDocument();
                break;
            case START_SUBDOCUMENT:
                processStartSubDocument((StartSubDocument) event.getResource());
                break;
            case END_SUBDOCUMENT:
                processEndSubDocument((Ending) event.getResource());
                break;
            case TEXT_UNIT:
            case START_GROUP:
            case END_GROUP:
            case START_SUBFILTER:
            case END_SUBFILTER:
                try {
                    subDocWriter.handleEvent(event);
                } catch (Throwable e) {
                    String mess = e.getMessage();
                    throw new OkapiNotImplementedException(mess, e); // kludge
                }
                break;
            default:
                break;
        }
        return event;
    }

    @Override
    public void setOptions(LocaleId locale, String defaultEncoding) {
        targetLocale = locale;
        // Ignore encoding. We always use UTF-8
    }

    @Override
    public void setOutput(String path) {
        outputPath = path;
    }

    @Override
    public void setOutput(OutputStream output) {
        this.outputStream = output;
    }

    private void processStartDocument(StartDocument res) {
        try {
            sourceLocale = res.getLocale();

            // reopen original zip file for reading
            ZipFile zipFile = ((ZipSkeleton) res.getSkeleton()).getOriginal();
            zipOriginal = new ZipFile(new File(zipFile.getName()), ZipFile.OPEN_READ);

            // Create the output stream from the path provided
            File tempZip = null;
            boolean useTemp = false;
            File f;
            OutputStream os = outputStream;
            if (outputStream == null) {
                f = new File(outputPath);
                if (f.exists()) {
                    // If the file exists, try to remove
                    useTemp = !f.delete();
                }
                if (useTemp) {
                    // Use a temporary output if we can overwrite for now
                    // If it's the input file, IFilter.close() will free it before we
                    // call close() here (that is if IFilter.close() is called correctly!)
                    tempZip = File.createTempFile("~okapi-22_idmlTmpZip_", null);
                    os = new FileOutputStream(tempZip.getAbsolutePath());
                } else {
                    Util.createDirectories(outputPath);
                    os = new FileOutputStream(outputPath);
                }
            }

            // create zip output
            zipOutputStream = new ZipOutputStream(os);

            // Create buffer for transfer
            buffer = new byte[2048];
        } catch (IOException e) {
            throw new OkapiIOException("Error creating output IDML.\n" + e.getMessage(), e);
        }
    }

    private void processEndDocument() {
        close();
    }

    /**
     * This passes a file that doesn't need processing from the input zip file to the output zip file.
     *
     * @param event corresponding to the file to be passed through
     */
    private void processDocumentPart(Event event) {
        DocumentPart documentPart = (DocumentPart) event.getResource();

        if (!(documentPart.getSkeleton() instanceof ZipSkeleton)) {
            subDocWriter.handleEvent(event);
            return;
        }

        ZipSkeleton skeleton = (ZipSkeleton) documentPart.getSkeleton();

        // Copy the entry data
        try {
            zipOutputStream.putNextEntry(new ZipEntry(skeleton.getEntry().getName()));

            // If the contents were modified by the filter, write out the new data
            String modifiedContents = skeleton.getModifiedContents();

            if (modifiedContents != null) {
                zipOutputStream.write(modifiedContents.getBytes(StandardCharsets.UTF_8));
            } else {
                InputStream input = zipOriginal.getInputStream(skeleton.getEntry());
                int len;
                while ((len = input.read(buffer)) > 0) {
                    zipOutputStream.write(buffer, 0, len);
                }
                input.close();
            }
            zipOutputStream.closeEntry();
        } catch (IOException e) {
            throw new OkapiIOException("Error writing zip file entry.");
        }
    }

    private void processStartSubDocument(StartSubDocument res) {
        ndxSubDoc++;

        // Set the temporary path and create it
        try {
            tempFile = File.createTempFile("~okapi-22_idmlTmp" + ndxSubDoc + "_", null);
        } catch (IOException e) {
            throw new OkapiIOException("Error opening temporary zip output file.");
        }

        subDocEntry = ((ZipSkeleton) res.getSkeleton()).getEntry();

        subDocWriter = new SubDocumentWriter(
                parameters, outputFactory, StandardCharsets.UTF_8, tempFile.getAbsolutePath(),
                new ReferenceableEventsWriter(
                    new ReferenceableEventsMerger(eventFactory, targetLocale),
                    new StyleRangeEventsGenerator(eventFactory)
                )
        );

        StartDocument sd = new StartDocument("sd");
        sd.setLineBreak("\n");
        sd.setSkeleton(res.getSkeleton());
        sd.setLocale(sourceLocale);
        subDocWriter.handleEvent(new Event(EventType.START_DOCUMENT, sd));

        SubDocumentValues subDocumentValues = new SubDocumentValues(subDocEntry, subDocWriter, tempFile);

        tmSubDoc.put(ndxSubDoc, subDocumentValues);
    }

    private void processEndSubDocument (Ending res) {
        try {
            SubDocumentValues subDocumentValues = tmSubDoc.get(ndxSubDoc--);
            subDocWriter = subDocumentValues.getFilterWriter();
            subDocEntry = subDocumentValues.getZipEntry();
            tempFile = subDocumentValues.getTempFile();
            // Finish writing the sub-document
            subDocWriter.handleEvent(new Event(EventType.END_DOCUMENT, res));
            subDocWriter.close();

            // Create the new entry from the temporary output file
            zipOutputStream.putNextEntry(new ZipEntry(subDocEntry.getName()));
            InputStream input = new FileInputStream(tempFile);
            int len;
            while ( (len = input.read(buffer)) > 0 ) {
                zipOutputStream.write(buffer, 0, len);
            }
            input.close();
            zipOutputStream.closeEntry();
            // Delete the temporary file
            tempFile.delete();
            tempFile = null;
        }
        catch ( IOException e ) {
            throw new OkapiIOException("Error closing zip output file.");
        }
    }

    private class SubDocumentValues {

        private final ZipEntry zipEntry;
        private final IFilterWriter filterWriter;
        private final File tempFile;

        SubDocumentValues(ZipEntry zipEntry, IFilterWriter filterWriter, File tempFile) {
            this.zipEntry = zipEntry;
            this.filterWriter = filterWriter;
            this.tempFile = tempFile;
        }

        ZipEntry getZipEntry() {
            return zipEntry;
        }

        IFilterWriter getFilterWriter() {
            return filterWriter;
        }

        File getTempFile() {
            return tempFile;
        }
    }
}
