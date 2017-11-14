/*===========================================================================
  Copyright (C) 2009-2011 by the Okapi Framework contributors
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

package net.sf.okapi.filters.openxml;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.FileUtil;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.MimeTypeMapper;
import net.sf.okapi.common.StreamUtil;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.exceptions.OkapiBadFilterInputException;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.exceptions.OkapiEncryptedDataException;
import net.sf.okapi.common.filters.FilterConfiguration;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filters.IFilterConfigurationMapper;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.skeleton.GenericSkeleton;
import net.sf.okapi.common.skeleton.ISkeletonWriter;
import net.sf.okapi.common.skeleton.ZipSkeleton;

import com.twelvemonkeys.io.ole2.CompoundDocument;
import com.twelvemonkeys.io.ole2.CorruptDocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Filters Microsoft Office Word, Excel, and Powerpoint Documents.
 * OpenXML is the format of these documents.
 * 
 * <p>Since OpenXML files are Zip files that contain XML documents,
 * this filter handles opening and processing the zip file, and
 * instantiates <b>OpenXMLContentFilter</b> to process the XML documents.
 * 
 * <p>A call to createFilterWriter returns OpenXMLZipFilterWriter, which is
 * the associated writer for this filter.  OpenXMLZipFilterWriter instantiates
 * OpenXMLContentSkeletonWriter. 
 */
@UsingParameters(ConditionalParameters.class)
public class OpenXMLFilter implements IFilter {
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	private enum NextAction {
		OPENZIP, NEXTINZIP, NEXTINSUBDOC, POSTPONED, DONE
	}

	private final static String MIMETYPE = MimeTypeMapper.XML_MIME_TYPE;
	private final static String documentId = "sd";
	
	private OpenXMLZipFileProxy zipFile;
	private File tempFile;
	private ZipEntry entry;
	private NextAction nextAction;
	private URI docURI;
	private Enumeration<? extends ZipEntry> entries;
	private int subDocumentId;
	private LinkedList<Event> queue;
	private LinkedList<Event> postponedEventsQueue;
	private LocaleId srcLang;
	private OpenXMLPartHandler currentPartHandler;
	private ConditionalParameters cparams=null; // DWH 6-16-09
	private DocumentType nZipType = null;
	private ParseType nFileType = ParseType.MSWORD;
	private AbstractTranslator translator=null;
	private LocaleId sOutputLanguage = LocaleId.US_ENGLISH;
	private String encoding="UTF-8"; // DWH 8-10-09 issue 104
	private EncoderManager encoderManager;
	private XMLInputFactory inputFactory = XMLInputFactory.newInstance();
	private XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
	private XMLEventFactory eventFactory = XMLEventFactory.newInstance();
	private RawDocument input;
	private Map<String, String> sharedStrings;

	public OpenXMLFilter () {
		cparams = new ConditionalParameters(); // DWH 6-16-09
		// security concern. Turn off DTD processing
		// https://www.owasp.org/index.php/XML_External_Entity_%28XXE%29_Processing
		inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
		sharedStrings = new HashMap<>();
		postponedEventsQueue = new LinkedList<>();
	}
	
	/**
	 * Creating the class with these two parameters allows automatic
	 * manipulation of text within TextUnits.  A copy of a source
	 * TextFragment is the parameter to the translator, and it
	 * can change the text.  The new text fragment is added to the
	 * TextUnit in the specified output language.
	 * @param translator the class that translates the text of a text fragment
	 * @param sOutputLanguage the locale of the output language, in the form en-US
	 */
	public OpenXMLFilter(AbstractTranslator translator, LocaleId sOutputLanguage) {
		this.translator = translator;
		this.sOutputLanguage = sOutputLanguage;
		cparams = new ConditionalParameters(); // DWH 6-16-09
		// security concern. Turn off DTD processing
		// https://www.owasp.org/index.php/XML_External_Entity_%28XXE%29_Processing
		inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);			

		sharedStrings = new HashMap<>();
		postponedEventsQueue = new LinkedList<>();
	}
	
	/**
	 * Closes the input zip file and completes the filter.
	 */
	public void close () {
		if (input != null) {
			input.close();
		}
		if (tempFile != null) {
			tempFile.delete();
		}
		
		try {
			nextAction = NextAction.DONE;
			if ( zipFile != null ) {
				zipFile.close();
				zipFile = null;
			}
		}
		catch (IOException e) {
			throw new OkapiIOException("Error closing zipped output file.");
		}
		
		if (currentPartHandler != null) {
			currentPartHandler.close(); 
		}
	}

	/**
	 * Creates the skeleton writer for use with this filter.
	 * Null return means implies GenericSkeletonWriter. 
	 * @return the skeleton writer
	 */
	public ISkeletonWriter createSkeletonWriter () {
		return null; // There is no corresponding skeleton writer
	}
	
	/**
	 * Creates the filter writer for use with this filter.
	 * @return the filter writer
	 */
	public IFilterWriter createFilterWriter () {
		return new OpenXMLZipFilterWriter(cparams, inputFactory, outputFactory, eventFactory);
	}

	public EncoderManager getEncoderManager () {
		if ( encoderManager == null ) {
			encoderManager = new EncoderManager();
			encoderManager.setMapping(MimeTypeMapper.XML_MIME_TYPE, "net.sf.okapi.common.encoder.XMLEncoder");
			encoderManager.setMapping(MimeTypeMapper.DOCX_MIME_TYPE, "net.sf.okapi.common.encoder.OpenXMLEncoder");
//			encoderManager.setAllKnownMappings();
		}
		return encoderManager;
	}

	public String getName () {
		return "okf_openxml";
	}

	public String getDisplayName () {
		return "OpenXML Filter";
	}

	public String getMimeType () {
		return MIMETYPE;
	}

	public List<FilterConfiguration> getConfigurations () {
		List<FilterConfiguration> list = new ArrayList<FilterConfiguration>();
		list.add(new FilterConfiguration(getName(),
			MIMETYPE,
			getClass().getName(),
			"Microsoft Office Document",
			"Microsoft Office documents (DOCX, DOCM, DOTX, DOTM, PPTX, PPTM, PPSX, PPSM, POTX, POTM, XLSX, XLSM, XLTX, XLTM, VSDX, VSDM).",
			null,
			".docx;.docm;.dotx;.dotm;.pptx;.pptm;.ppsx;.ppsm;.potx;.potm;.xlsx;.xlsm;.xltx;.xltm;.vsdx;.vsdm;"));
		return list;
	}

	/**
	 * Returns the current IParameters object.
	 * @return the current IParameters object
	 */
	public ConditionalParameters getParameters () {
		return cparams;
	}

	/**
	 * Returns true if the filter has a next event.
	 * @return whether or not the filter has a next event
	 */
	public boolean hasNext () {
		return ((( queue != null ) && ( !queue.isEmpty() )) || ( nextAction != NextAction.DONE ));
	}

	/**
	 * Returns the next zip filter event.
	 * @return the next zip filter event
	 */
	public Event next () {
		// Send remaining event from the queue first
		if ( queue.size() > 0 ) {
			return queue.poll();
		}

		try {
			
			// When the queue is empty: process next action
			switch ( nextAction ) {
			case OPENZIP:
				return openZipFile();
			case NEXTINZIP:
				Event e =  nextInZipFile();
				if (e.getEventType() == EventType.CUSTOM) {
					postponedEventsQueue.add(e);
				}
				return e;
			case NEXTINSUBDOC:
				e = nextInSubDocument();
				if (e != null) {
					return e;
				}
				// That subdoc is done; call another.  XXX This is hacky
				// since it's a special case for handling NonTranslatablePartHandler;
				// things that call real subfilters produce END_DOCUMENT stuff that
				// is handled a different way.
				nextAction = NextAction.NEXTINZIP;
				return next();
			case POSTPONED:
				return handlePostponedEvent();
			default:
				throw new OkapiException("Invalid next() call.");
			}
		}
		catch (IOException | XMLStreamException e) {
			throw new OkapiException("An error occurred during extraction", e);
		}
	}

	/**
	 * Opens a RawDocument for filtering, defaulting to generating the skeleton
	 * @param input a Raw Document to open and filter
	 */
	public void open (RawDocument input) {
		open(input, true);
	}
	
	/**
	 * Opens a RawDocument for filtering
	 * @param input a Raw Document to open and filter
	 * @param generateSkeleton true if a skeleton should be generated
	 */
	public void open (RawDocument input,
		boolean generateSkeleton)
	{
		if (input==null)
			throw new OkapiException("RawDocument is null");
		
		// save reference for cleanup in close
		this.input = input;
				
		setOptions(input.getSourceLocale(), input.getTargetLocale(),
			input.getEncoding(), generateSkeleton);
		if ( input.getInputCharSequence() != null ) {
			open(input.getInputCharSequence());
		}
		else if ( input.getInputURI() != null ) {
			open(input.getInputURI());
			LOGGER.debug("\nOpening {}", input.getInputURI().toString());
		}
		else if ( input.getStream() != null ) {
			open(input.getStream());
		}
		else {
			throw new OkapiException("InputResource has no input defined.");
		}
	}

	/**
	 * Opens an input stream for filtering
	 * @param input an input stream to open and filter
	 */
	public void open (InputStream input) {
//		// Not supported for this filter
//		throw new UnsupportedOperationException(
//			"Method is not supported for this filter.");\
		
		// Create a temp file for the stream content
		tempFile = FileUtil.createTempFile("~okapi-23_OpenXMLFilter_");
    	StreamUtil.copy(input, tempFile);
    	open(Util.toURI(tempFile.getAbsolutePath()));
	}

	/**
	 * Opens a character sequence for filtering
	 * @param inputText character sequence to open and filter
	 */
	private void open (CharSequence inputText) {
		// Not supported for this filter
		throw new UnsupportedOperationException(
			"Method is not supported for this filter.");
	}

	/**
	 * Opens a URI for filtering
	 * @param inputURI cURI to open and filter
	 */
	public void open (URI inputURI) {
		docURI = inputURI;
		nextAction = NextAction.OPENZIP;
		queue = new LinkedList<Event>();
		LOGGER.debug("\nOpening {}", inputURI.toString());
	}

	/**
	 * Sets language, encoding, and generation options for the filter.
	 * @param sourceLanguage source language in en-US format
	 * @param defaultEncoding encoding, such as "UTF-8"
	 * @param generateSkeleton true if skeleton should be generated
	 */
	public void setOptions (LocaleId sourceLanguage,
		String defaultEncoding,
		boolean generateSkeleton)
	{
		setOptions(sourceLanguage, null, defaultEncoding, generateSkeleton);
	}

	/**
	 * Sets language, encoding, and generation options for the filter.
	 * @param sourceLanguage source language in en-US format
	 * @param targetLanguage target language in de-DE format
	 * @param defaultEncoding encoding, such as "UTF-8"
	 * @param generateSkeleton true if skeleton should be generated
	 */
	public void setOptions (LocaleId sourceLanguage,
		LocaleId targetLanguage,
		String defaultEncoding,
		boolean generateSkeleton)
	{
		srcLang = sourceLanguage;
		encoding = defaultEncoding; // issue 104
	}

	@Override
	public void setFilterConfigurationMapper (IFilterConfigurationMapper fcMapper) {
	}

	@Override
	public void setParameters (IParameters params) {
		this.cparams = (ConditionalParameters)params;
	}

	/**
	 * Opens the document at the URI specified in the call to open(..),
	 * looks through the names of the XML files inside to determine
	 * the type, and creates a StartDocument Event.
	 */
	private Event openZipFile () {
		try
		{
			File fZip = new File(docURI.getPath());

			if (isZipFileEncrypted(fZip)) {
				throw new OkapiEncryptedDataException();
			}

			zipFile = new OpenXMLZipFileProxy(new ZipFile(fZip,ZipFile.OPEN_READ), inputFactory,
									     outputFactory, eventFactory, encoding, sharedStrings);
			nZipType = zipFile.createDocument(cparams);
			if (nZipType==null)
			{
				throw new OkapiBadFilterInputException("MS Office 2007 filter tried to open a file that is not aMicrosoft Office 2007 Word, Excel, or Powerpoint file.");
			}
			entries = nZipType.getZipFileEntries();
			subDocumentId = 0;
			nextAction = NextAction.NEXTINZIP;
			StartDocument startDoc = new StartDocument(documentId);
			startDoc.setName(docURI.getPath());
			startDoc.setLocale(srcLang);
			startDoc.setMimeType(MIMETYPE);
			startDoc.setFilterWriter(createFilterWriter());
			startDoc.setFilterParameters(getParameters());
			startDoc.setLineBreak("\n");
			startDoc.setEncoding(encoding, false);  // Office 2007 files don't have UTF8BOM
			startDoc.setFilterParameters(cparams);
			ZipSkeleton skel = new ZipSkeleton(zipFile.getZip(), null);
			return new Event(EventType.START_DOCUMENT, startDoc, skel);
		}
		catch ( ZipException e )
		{
			throw new OkapiIOException("Error opening zipped input file.");
		}
		catch ( IOException e )
		{
			throw new OkapiIOException("Error reading zipped input file.", e);
		}
		catch (XMLStreamException e) 
		{
			throw new OkapiIOException("Error parsing XML content", e);
		}
	}

	private boolean isZipFileEncrypted(File file) throws IOException {
		try {
			new CompoundDocument(file);
			return true;
		} catch (CorruptDocumentException e) {
			return false;
		}
	}

	/**
	 * Opens the next file in the zip fle, determines its type based on its name,
	 * reads the yaml configuration file and sets the parameters, then creates
	 * a DocumentPart Event if this file is to pass through unaltered, or 
	 * subdocument Events otherwise
	 * @return an appropriate Event for this XML file in the zip file
	 * @throws XMLStreamException
	 */
	private Event nextInZipFile () throws IOException, XMLStreamException {
		String sEntryName; // DWH 2-26-09
		String sDocType; // DWH 2-26-09
		while( entries.hasMoreElements() ) { // note that [Content_Types].xml is always first
			entry = entries.nextElement();
			sEntryName = entry.getName();
			sDocType = zipFile.getContentTypes().getContentType("/" + sEntryName);
			LOGGER.debug("\n\n<<<<<<< {} : {} >>>>>>>", sEntryName, sDocType);

			// TODO set this in the object state
			currentPartHandler = nZipType.getHandlerForFile(entry, sDocType);
			// TODO at this point I could stash the yaml params here, if I needed them, as
			// they will be configured in the part handler (if it uses yaml).
			nextAction = NextAction.NEXTINSUBDOC;
			return currentPartHandler.open(documentId, String.valueOf(++subDocumentId), srcLang);
		}

		if (postponedEventsQueue.isEmpty()) {
			// No more sub-documents: end of the ZIP document
			return completeDocument();
		}
		// textual references should be updated after translation of a visible element in the sheet
		nextAction = NextAction.POSTPONED;

		return new Event(EventType.NO_OP);
	}

	private Event completeDocument() {
		close();
		Ending ending = new Ending("ed");
		return new Event(EventType.END_DOCUMENT, ending);
	}

	private Event handlePostponedEvent() throws IOException, XMLStreamException {
		if (postponedEventsQueue.isEmpty()) {
			return completeDocument();
		}
		Event event = postponedEventsQueue.poll();
		PostponedDocumentPart postponedDocumentPart = (PostponedDocumentPart) event.getResource();

		if (!postponedDocumentPart.isPartHidden()) {
			ExcelFormulaPartHandler excelContentPartHandler =
					new ExcelFormulaPartHandler(cparams, postponedDocumentPart.getSkeleton(), sharedStrings,
												postponedDocumentPart.getZipEntry());

			nextAction = NextAction.NEXTINSUBDOC;

			ClarifiablePartHandler clarifiablePartHandler =
					new ClarifiablePartHandler(zipFile, postponedDocumentPart.getZipEntry());
			currentPartHandler = clarifiablePartHandler;
			ByteArrayInputStream bis =
					new ByteArrayInputStream(excelContentPartHandler.getModifiedContent().getBytes(StandardCharsets.UTF_8));
			return clarifiablePartHandler.open(bis);
		}
		ClarifiablePartHandler clarifiableParthandler =
				new ClarifiablePartHandler(zipFile, postponedDocumentPart.getZipEntry());
		currentPartHandler = clarifiableParthandler;
		ByteArrayInputStream bis =
				new ByteArrayInputStream(postponedDocumentPart.getSkeleton().toString().getBytes(StandardCharsets.UTF_8));

		return clarifiableParthandler.open(bis);
	}

	/**
	 * Returns the next subdocument event.  If it is a TEXT_UNIT event,
	 * it invokes the translator to manipulate the text before sending
	 * on the event.  If it is an END_DOCUMENT event, it sends on
	 * an END_SUBDOCUMENT event instead.
	 * @return a subdocument event
	 */
	private Event nextInSubDocument () {
		Event event;
		while ( currentPartHandler.hasNext() ) {
			event = currentPartHandler.next();
			switch ( event.getEventType() ) {
				case TEXT_UNIT:
					if (translator!=null)
					{
						translator.addToReferents(event);
						ITextUnit tu = event.getTextUnit();
						// We can use getFirstPartContent() because nothing is segmented yet
						TextFragment tfSource = tu.getSource().getFirstContent();
						String torg = translator.translate(tfSource,LOGGER,nFileType); // DWH 5-7-09 nFileType
						TextFragment tfTarget = tfSource.clone();
						tfTarget.setCodedText(torg);
						TextContainer tc = new TextContainer();
						tc.setContent(tfTarget);
						tu.setTarget(sOutputLanguage, tc);
						sharedStrings.put(tfSource.getCodedText(), torg);
						tfSource = null;
					}
					currentPartHandler.logEvent(event);
					return event;
				case END_DOCUMENT:
					// Change the END_DOCUMENT to END_SUBDOCUMENT
					Ending ending = new Ending(String.valueOf(subDocumentId));
					nextAction = NextAction.NEXTINZIP;
					ZipSkeleton skel = new ZipSkeleton(
						(GenericSkeleton)event.getResource().getSkeleton(), zipFile.getZip(), entry);
					currentPartHandler.close();
					return new Event(EventType.END_SUBDOCUMENT, ending, skel);				
				case DOCUMENT_PART:
				case START_GROUP:
				case START_SUBFILTER:
						if (translator!=null)
							translator.addToReferents(event);
						// purposely falls through to default
				default: // Else: just pass the event through
					currentPartHandler.logEvent(event);
					return event;
			}
		}
		// We can fall through to here if a part handler runs out of events.
		return null;
	}

	public void cancel() {
		// TODO Auto-generated method stub		
	}
}
