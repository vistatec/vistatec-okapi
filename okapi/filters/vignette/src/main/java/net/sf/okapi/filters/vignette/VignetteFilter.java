/*===========================================================================
  Copyright (C) 2010-2011 by the Okapi Framework contributors
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

package net.sf.okapi.filters.vignette;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.sf.okapi.common.BOMNewlineEncodingDetector;
import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.IdGenerator;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.MimeTypeMapper;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.encoder.CDATAEncoder;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.encoder.IEncoder;
import net.sf.okapi.common.encoder.QuoteMode;
import net.sf.okapi.common.encoder.XMLEncoder;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.exceptions.OkapiUnsupportedEncodingException;
import net.sf.okapi.common.filters.FilterConfiguration;
import net.sf.okapi.common.filters.FilterUtil;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filters.IFilterConfigurationMapper;
import net.sf.okapi.common.filters.SubFilter;
import net.sf.okapi.common.filterwriter.GenericFilterWriter;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.DocumentPart;
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.StartSubDocument;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextUnit;
import net.sf.okapi.common.skeleton.GenericSkeleton;
import net.sf.okapi.common.skeleton.GenericSkeletonWriter;
import net.sf.okapi.common.skeleton.ISkeletonWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Implements the IFilter interface for Vignette export/import content.
 */
@UsingParameters(Parameters.class)
public class VignetteFilter implements IFilter {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final String STARTBLOCK = "<importContentInstance>";
	private final String ENDBLOCK = "</importContentInstance>";
	
	private Parameters params;
	private String lineBreak;
	private int tuId;
	private IdGenerator subDocId;
	private int sectionIndex;
	private int otherId;
	private LinkedList<Event> queue;
	private boolean hasNext;
	private EncoderManager encoderManager;
	private BufferedReader reader;
	private SubFilter subFilter;
	private LocaleId srcLoc;
	private LocaleId trgLoc;
	private DocumentBuilder docBuilder;
	private String[] partsNames;
	private String[] partsConfigurations;
	private Hashtable<String, String[]> docs;
	private String inputText;
	private int current; // Current position
	private boolean preprocessing;
	private TemporaryStore store;
	private File storeFile;
	private int counter;
	private IFilterConfigurationMapper fcMapper;
	private String currentVFullPath;
	private List<String> listOfPaths;
	private String rootId;
	private boolean monolingual;
	private IFilter filter;

	private RawDocument input;
	
	public VignetteFilter () {
		params = new Parameters();
		DocumentBuilderFactory Fact = DocumentBuilderFactory.newInstance();
		Fact.setValidating(false);
		
		// security concern. Turn off DTD processing
		// https://www.owasp.org/index.php/XML_External_Entity_%28XXE%29_Processing
		try {
			// Xerces 1 - http://xerces.apache.org/xerces-j/features.html#external-general-entities
			// Xerces 2 - http://xerces.apache.org/xerces2-j/features.html#external-general-entities
			Fact.setFeature("http://xml.org/sax/features/external-general-entities", false);
			 
			// Xerces 2 only - http://xerces.apache.org/xerces-j/features.html#external-general-entities
			Fact.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			 
			} catch (ParserConfigurationException e) {
				// Tried an unsupported feature. This may indicate that a different XML processor is being
				// used. If so, then its features need to be researched and applied correctly.
				// For example, using the Xerces 2 feature above on a Xerces 1 processor will throw this
				// exception.
				logger.warn("Unsupported DocumentBuilderFactory feature. Possible security vulnerabilities.", e);
			}
					
		try {
			docBuilder = Fact.newDocumentBuilder();
		}
		catch ( ParserConfigurationException e ) {
			throw new OkapiException("Error creating document builder.", e);
		}
	}
	
	@Override
	public void cancel () {
		// TODO: Support cancel
	}

	@Override
	public void close () {
		if ( input != null ) {
			input.close();
			input = null;
		}
		
		try {
			if ( reader != null ) {
				reader.close();
				reader = null;
			}
			if ( store != null ) {
				store.close();
				// Nullify and delete if not in pre-processing mode
				// In other words, not after the first pass.
				if ( !preprocessing ) {
					store = null;
					storeFile.delete();
				}
			}
		}
		catch ( IOException e ) {
			throw new OkapiIOException("Error when closing.", e);
		}
		// Nothing to do
		hasNext = false;
	}

	@Override
	public ISkeletonWriter createSkeletonWriter() {
		//return new VignetteSkeletonWriter();
		return new GenericSkeletonWriter();
	}

	@Override
	public IFilterWriter createFilterWriter () {
		return new GenericFilterWriter(createSkeletonWriter(), getEncoderManager());
	}

	@Override
	public List<FilterConfiguration> getConfigurations () {
		List<FilterConfiguration> list = new ArrayList<FilterConfiguration>();
		list.add(new FilterConfiguration(getName(),
			MimeTypeMapper.XML_MIME_TYPE,
			getClass().getName(),
			"Vignette Export/Import Content",
			"Default Vignette Export/Import Content configuration."));
		list.add(new FilterConfiguration(
				getName() + "-nocdata",
				MimeTypeMapper.XML_MIME_TYPE,
				getClass().getName(),
				"Vignette Export/Import Content (escaped HTML)",
				"Vignette files without CDATA sections.",
				"nocdata.fprm"));
		return list;
	}

	@Override
	public EncoderManager getEncoderManager () {
		if ( encoderManager == null ) {
			encoderManager = new EncoderManager();
			encoderManager.setMapping(MimeTypeMapper.XML_MIME_TYPE, "net.sf.okapi.common.encoder.XMLEncoder");
		}
		return encoderManager;
	}

	@Override
	public void setFilterConfigurationMapper (IFilterConfigurationMapper fcMapper) {
		this.fcMapper = fcMapper;
	}

	@Override
	public String getDisplayName () {
		return "Vignette Filter";
	}

	@Override
	public String getMimeType () {
		return MimeTypeMapper.XML_MIME_TYPE;
	}

	@Override
	public String getName () {
		return "okf_vignette";
	}

	@Override
	public Parameters getParameters () {
		return params;
	}

	@Override
	public boolean hasNext () {
		return hasNext;
	}

	@Override
	public Event next () {
		try {
			if ( !hasNext ) return null;
			if ( queue.size() == 0 ) {
				processBlock();
			}
			Event event = queue.poll();
			if ( event.getEventType() == EventType.END_DOCUMENT ) {
				hasNext = false;
				if ( !preprocessing ) {
					generateListOfPaths();
				}
			}
			return event;
		}
		catch ( Throwable e ) {
			throw new OkapiIOException("Error reading the input.", e);
		}
	}

	@Override
	public void open (RawDocument input) {
		open(input, true);
	}

	@Override
	public void open (RawDocument input,
		boolean generateSkeleton)
	{
		this.input = input;
		
		monolingual = params.getMonolingual();
		if ( monolingual ) {
			logger.info("- Monolingual processing");
		}
		else {
			logger.info("- Pre-processing pass");
		}

		partsNames = params.getPartsNamesAsList();
		partsConfigurations = params.getPartsConfigurationsAsList();
		if ( !params.checkData() ) {
			throw new OkapiException("Invalid parts description in the parameters.");
		}
		docs = new Hashtable<String, String[]>();
		
		trgLoc = input.getTargetLocale();
		if ( trgLoc == null ) {
			throw new OkapiException("You must specify a target locale.");
		}
		listOfPaths = new ArrayList<String>();
		
		// Just one pass for monolingual mode
		if ( monolingual ) {
			preprocessing = false;
			internalOpen(input);
			return;
		}
		
		//--- Else: Normal bilingual mode with two passes
		
		store = new TemporaryStore();
		try {
			storeFile = File.createTempFile("~okapi-30_vgnflt_", null);
		}
		catch ( IOException e ) {
			throw new OkapiIOException("Error creating temporary store.", e);
		}
		store.create(storeFile);
		counter = 0;

		// First pass: pre-processing
		// Get the list of the usable documents
		// and create the temporary store
		preprocessing = true;
		internalOpen(input);
		while ( hasNext() ) {
			next();
		}
		
		// Check if we have things to extract
		int toExtract = 0;
		for ( String sourceId : docs.keySet() ) {
			String[] data = docs.get(sourceId);
			if ( Util.isEmpty(data[0]) ) {
				// No source
				if ( !Util.isEmpty(data[1]) ) {
					// No source but target exists
					logger.warn("Entry '{}': No corresponding source entry exists for the target '{}'",
						data[1], trgLoc.toPOSIXLocaleId());
				}
			}
			else { // Source exists
				if ( Util.isEmpty(data[1]) ) {
					// Source exists but not the target
					logger.warn("Entry '{}': No entry exists for the target '{}'",
						data[0], trgLoc.toPOSIXLocaleId());
				}
				else {
					// Both source and target exists
					toExtract++;
				}
			}
		}
		if ( toExtract <= 0 ) {
			logger.warn("There are no entries to extract");
		}

		// Second pass: extraction
		logger.info("- Processing pass");
		preprocessing = false;
		internalOpen(input);
		store.openForRead(storeFile);		
		// Now, the caller will be calling next()
	}
	
	private void internalOpen (RawDocument input) {
		BOMNewlineEncodingDetector detector = new BOMNewlineEncodingDetector(
			input.getStream(), "UTF-8"); // UTF-8 for encoding
		detector.detectAndRemoveBom();
		input.setEncoding(detector.getEncoding());
		String encoding = input.getEncoding();
		
		try {
			reader = new BufferedReader(new InputStreamReader(detector.getInputStream(),
				encoding));
		}
		catch ( UnsupportedEncodingException e ) {
			throw new OkapiUnsupportedEncodingException(
				String.format("The encoding '%s' is not supported.", encoding), e);
		}
		lineBreak = detector.getNewlineType().toString();
		boolean hasUTF8BOM = detector.hasUtf8Bom();
		String docName = null;
		if ( input.getInputURI() != null ) {
			docName = input.getInputURI().getPath();
		}
		
		//TODO: We may have to work with buffered block to handle very large files
		readAllData();
		
//		if ( docName != null ) 
//			rootId = docName;
//		else 
			rootId = IdGenerator.DEFAULT_ROOT_ID;

		tuId = 0;
		subDocId = new IdGenerator(rootId, IdGenerator.START_SUBDOCUMENT);
		//groupId = new IdGenerator(rootId, IdGenerator.START_GROUP);
		sectionIndex = 0;
		otherId = 0;
		
		// Set the start event
		queue = new LinkedList<Event>();
		StartDocument startDoc = new StartDocument(String.valueOf(++otherId));
		startDoc.setName(docName);
		startDoc.setEncoding(encoding, hasUTF8BOM);
		srcLoc = input.getSourceLocale();
		startDoc.setLocale(srcLoc);
		startDoc.setLineBreak(lineBreak);
		startDoc.setFilterParameters(getParameters());
		startDoc.setFilterWriter(createFilterWriter());
		startDoc.setType(MimeTypeMapper.XML_MIME_TYPE);
		startDoc.setMimeType(MimeTypeMapper.XML_MIME_TYPE);
		startDoc.setMultilingual(false);
		queue.add(new Event(EventType.START_DOCUMENT, startDoc));
		
		// load simplifier rules and send as an event
		if (!Util.isEmpty(((Parameters)getParameters()).getSimplifierRules())) {			
			Event cs = FilterUtil.createCodeSimplifierEvent(((Parameters)getParameters()).getSimplifierRules());
			queue.add(cs);
		}	
				
		hasNext = true;
	}

	@Override
	public void setParameters (IParameters params) {
		this.params = (Parameters)params;
	}

	private void readAllData () {
		StringBuilder tmp = new StringBuilder();
		char[] buf = new char[2048];
		int count = 0;
		try {
			while (( count = reader.read(buf)) != -1 ) {
				tmp.append(buf, 0, count);
			}
			
			inputText = tmp.toString().replace(lineBreak, "\n");
			current = 0;
		}
		catch ( IOException e ) {
			throw new OkapiIOException("Error reading the input.", e);
		}
		finally {
			if ( reader != null ) {
				try {
					reader.close();
				}
				catch ( IOException e ) {
					throw new OkapiIOException("Error closing the input.", e);
				}
			}
		}
	}

	private void updateDocumentList (String sourceId,
		boolean isSource)
	{
		if ( !docs.containsKey(sourceId) ) {
			docs.put(sourceId, new String[2]);
		}
		String[] data = docs.get(sourceId);
		// Source is at index 0, target at index 1
		// We just place the sourceId there for both
		if ( isSource ) data[0] = sourceId;
		else data[1] = sourceId;
	}

	private void processBlock ()
		throws SAXException, IOException
	{
		while ( true ) {
			// Look for the next block
			int start = inputText.indexOf(STARTBLOCK, current);
	
			// No more block: end of the document
			if ( start == -1 ) {
				// From current to end: to skeleton
				Ending ending = new Ending(String.valueOf(++otherId));
				ending.setSkeleton(new GenericSkeleton(
					inputText.substring(current).replace("\n", lineBreak)));
				queue.add(new Event(EventType.END_DOCUMENT, ending));
				return;
			}
			
			// Start of block found: look for end of block
			int end = inputText.indexOf(ENDBLOCK, start);
			if ( end == -1 ) {
				throw new OkapiIOException("Cannot find end of block.");
			}
			
			// End of block found
			if ( preprocessing ) {
				counter++;
			}
			else {
				// Parts between current and start go to skeleton
				DocumentPart dp = new DocumentPart(String.valueOf(++otherId), false);
				dp.setSkeleton(new GenericSkeleton(
					inputText.substring(current, start).replace("\n", lineBreak)));
				queue.add(new Event(EventType.DOCUMENT_PART, dp));
			}
			current = end+ENDBLOCK.length(); // For next time
	
			// Treat the content
			// Returns true when an event was found
			if ( processXMLBlock(start, current) ) {
				break; // Return
			}
		}
	}
	
	// Returns true if event was found
	private boolean processXMLBlock (int start,
		int end)
	{
		boolean eventFound = false;
		try {
			// Parse the block into a DOM-tree
			String content = inputText.substring(start, end);
			Document doc = docBuilder.parse(new InputSource(new StringReader(content)));
			
			// Get first 'contentInstance' element
			NodeList nodes = doc.getElementsByTagName("contentInstance");
			Element elem = (Element)nodes.item(0);
			currentVFullPath = elem.getAttribute("vcmLogicalPath") + "/" + elem.getAttribute("vcmName");

			// Get all 'attribute' elements in 'contentInstance'
			if ( monolingual ) {
				if ( processListForMonolingual(elem, content) ) eventFound = true;
			}
			else {
				if ( !preprocessing ) {
					logger.info("contentInstance vcmLogicalPath={}", currentVFullPath);
				}
				if ( processList(elem, content) ) eventFound = true;
			}
		}
		catch ( Throwable e ) {
			throw new OkapiIOException(
				String.format("XML parsing error in block starting at character %d.", start), e);
		}
		return eventFound;
	}
	
	
	private boolean processListForMonolingual (Element elem,
		String content) throws SAXException, IOException
	{
		// Parse the block into a DOM-tree
		String tmp = content;

		// Parse the source content
		Document oriDoc = docBuilder.parse(new InputSource(new StringReader(tmp)));
		// Get first 'contentInstance' element
		NodeList oriNodes = oriDoc.getElementsByTagName("contentInstance");
		elem = (Element)oriNodes.item(0);

		// Get all 'attribute' elements in 'contentInstance' 
		oriNodes = elem.getElementsByTagName("attribute");
		int last = 0;
		int[] pos;
		listOfPaths.add(currentVFullPath);
		
		// Start of sub-document
		StartSubDocument ssd = new StartSubDocument(subDocId.createId());
		ssd.setName(subDocId.toString());
		queue.add(new Event(EventType.START_SUBDOCUMENT, ssd));
		
		for ( int i=0; i<oriNodes.getLength(); i++ ) {
			Element tmpElem = (Element)oriNodes.item(i);
			String name = tmpElem.getAttribute("name");
			
			// See if the name is in the list of the parts to extract
			//TODO: We could have a faster way to detect if the name is listed and get j
			boolean found = false;
			int j;
			for ( j=0; j<partsNames.length; j++ ) {
				if ( name.equals(partsNames[j]) ) {
					found = true;
					break;
				}
			}
			if ( !found ) continue; // Not an attribute element to extract
			
			tmpElem = getFirstElement(tmpElem);
			String data = tmpElem.getTextContent();
			if ( Util.isEmpty(data) ) continue;
			
			// Get the range of the content in the target block
			pos = getRange(content, last, partsNames[j]);
			// Create the document part skeleton for the data before
			DocumentPart dp = new DocumentPart(String.valueOf(++otherId), false);
			dp.setSkeleton(new GenericSkeleton(content.substring(last, pos[0]).replace("\n", lineBreak)));
			queue.add(new Event(EventType.DOCUMENT_PART, dp));
			last = pos[1]; // For next part
			// Create the event from the original block
			processContent(data, partsNames[j], partsConfigurations[j]);

		}
		
		// End of group, and attached the skeleton for the end too
		Ending ending = new Ending(String.valueOf(++otherId));
		ending.setSkeleton(new GenericSkeleton(content.substring(last).replace("\n", lineBreak)));			
		queue.add(new Event(EventType.END_SUBDOCUMENT, ending));
		return true;
	}

	private boolean processList (Element elem,
		String content) throws SAXException, IOException
	{
		String sourceId = null;
		String localeId = null;

		// Get all 'attribute' elements in 'contentInstance' 
		NodeList nodes = elem.getElementsByTagName("attribute");
		// Get info
		for ( int i=0; i<nodes.getLength(); i++ ) {
			elem = (Element)nodes.item(i);
			String name = elem.getAttribute("name");
			if ( name.equals(params.getLocaleId()) ) {
				localeId = getValueString(elem);
			}
			else if ( name.equals(params.getSourceId()) ) {
				sourceId = getValueString(elem);
			}
			if (( sourceId != null ) && ( localeId != null )) {
				break; // We are done
			}
		}
		
		// Skip block, if not all info is available
		if ( Util.isEmpty(localeId) || Util.isEmpty(sourceId) ) {
			// Warn during pre-processing, then treat as document part
			if ( preprocessing ) {
				logger.warn("Entry with incomplete data at {} number {}\nlocale='{}' sourceId='{}'",
					STARTBLOCK, counter, localeId, sourceId);
				return false;
			}
			else {
				logger.warn("Missing data, this section is skipped.");
				DocumentPart dp = new DocumentPart(String.valueOf(++otherId), false);
				dp.setSkeleton(new GenericSkeleton(content.replace("\n", lineBreak)));
				queue.add(new Event(EventType.DOCUMENT_PART, dp));
				return true;
			}
		}

		if ( preprocessing ) {
			if ( srcLoc.toPOSIXLocaleId().equals(localeId) ) {
				// For a source block: update the list, store the data and move on
				updateDocumentList(sourceId, true);
				store.writeBlock(sourceId, content);
				return false;
			}
			else if ( trgLoc.toPOSIXLocaleId().equals(localeId) ) {
				// For a target block: update the list and skip
				updateDocumentList(sourceId, false);
				return false;
			}
			else {
				// For other locales, just skip them
				return false;
			}
		}
		else {
			// Else, in extract mode: skip if not a target block
			boolean extract = true;
			if ( trgLoc.toPOSIXLocaleId().equals(localeId) ) {
				// If it's a target
				// Find its corresponding entry in the store
				String[] data = docs.get(sourceId);
				if ( data == null ) {
					extract = false;
				}
				else if ( Util.isEmpty(data[0]) ) {
					// No corresponding source was detected
					extract = false;
				}
			}
			else { // Not a target: skip
				extract = false;
			}
			
			logger.info("   LocaleId='{}', extract={}, sourceId='{}'",
				localeId, (extract ? "Yes" : "No"), sourceId);

			// If we don't extract
			if ( !extract ) {
				// Just send as document part
				DocumentPart dp = new DocumentPart(String.valueOf(++otherId), false);
				dp.setSkeleton(new GenericSkeleton(content.replace("\n", lineBreak)));
				queue.add(new Event(EventType.DOCUMENT_PART, dp));
				return true;
			}
			// Else: extract
		}
		
		// Find its corresponding entry in the store
		// Parse the block into a DOM-tree
		String tmp = findOriginalInStore(sourceId);
		if ( tmp == null ) {
			throw new OkapiIOException(String.format(
				"The sourceId attribute was not found ('%s').", sourceId));
		}

		// Parse the source content
		Document oriDoc = docBuilder.parse(new InputSource(new StringReader(tmp)));
		// Get first 'contentInstance' element
		NodeList oriNodes = oriDoc.getElementsByTagName("contentInstance");
		elem = (Element)oriNodes.item(0);

		// Get all 'attribute' elements in 'contentInstance' 
		oriNodes = elem.getElementsByTagName("attribute");

		int last = 0;
		int[] pos;
		
		listOfPaths.add(currentVFullPath);

		// Start of sub-document
		StartSubDocument ssd = new StartSubDocument(subDocId.createId());
		ssd.setName(sourceId);
		queue.add(new Event(EventType.START_SUBDOCUMENT, ssd));
		
		for ( int i=0; i<oriNodes.getLength(); i++ ) {
			Element tmpElem = (Element)oriNodes.item(i);
			String name = tmpElem.getAttribute("name");
			
			// See if the name is in the list of the parts to extract
			//TODO: We could have a faster way to detect if the name is listed and get j
			boolean found = false;
			int j;
			for ( j=0; j<partsNames.length; j++ ) {
				if ( name.equals(partsNames[j]) ) {
					found = true;
					break;
				}
			}
			if ( !found ) continue; // Not an attribute element to extract
			
			tmpElem = getFirstElement(tmpElem);
			String data = tmpElem.getTextContent();
			if ( Util.isEmpty(data) ) continue;
			
			// Get the range of the content in the target block
			pos = getRange(content, last, partsNames[j]);
			// Create the document part skeleton for the data before
			DocumentPart dp = new DocumentPart(String.valueOf(++otherId), false);
			dp.setSkeleton(new GenericSkeleton(content.substring(last, pos[0]).replace("\n", lineBreak)));
			queue.add(new Event(EventType.DOCUMENT_PART, dp));
			last = pos[1]; // For next part
			// Create the event from the original block
			processContent(data, partsNames[j], partsConfigurations[j]);

		}
		
		// End of group, and attached the skeleton for the end too
		Ending ending = new Ending(String.valueOf(++otherId));
		ending.setSkeleton(new GenericSkeleton(content.substring(last).replace("\n", lineBreak)));			
		queue.add(new Event(EventType.END_SUBDOCUMENT, ending));
		return true;
	}

	private String findOriginalInStore (String sourceId) {
		boolean rewund = false;
		String stop = null;
		while ( true ) {
			String tmp[] = store.readNext();
			if ( tmp == null ) {
				if ( rewund ) return null; // Not found
				// Else: rewind the store
				store.close();
				store.openForRead(storeFile);
				rewund = true;
			}
			else {
				if ( tmp[0].equals(sourceId) ) {
					return tmp[1]; // Found
				}
				else {
					if ( stop != null ) {
						if ( tmp[0].equals(stop) ) {
							return null; // Stop here, not found
						}
						// Move to next
					}
					else {
						// Remember where to stop later
						stop = tmp[0];
					}
				}
			}
		}
	}
	
//	private String getContent (NodeList nodes,
//		String partName)
//	{
//		for ( int i=0; i<nodes.getLength(); i++ ) {
//			Element elem = (Element)nodes.item(i);
//			String name = elem.getAttribute("name");
//			if ( name.equals(partName) ) {
//				elem = getFirstElement(elem);
//				return elem.getTextContent();
//			}
//		}
//		return null;
//	}
	
	// Returns from start of attribute element for the given part name,
	// 0=start of the attribute element
	// 1=first char of the data in the value-type element
	// 2=start of the closing value-type element
	// [attribute name='name'][valueObject]data[/valueObject][/attribute]
	//                                     ^=0 ^=1
	private int[] getRange (String content,
		int start,
		String partName)
	{
		int[] res = new int[3];
		res[0] = -1;
		
		String tmp = String.format("<attribute name=\"%s\">", partName);
		int n = content.indexOf(tmp, start);
		if ( n == -1 ) return res;
		n = content.indexOf("<", n+1); // Start of value-type element
		// Meta characters are escaped so we can just do this: 
		res[0] = content.indexOf(">", n)+1;
		String name = content.substring(n + 1, res[0] - 1);
		res[1] = content.indexOf("</" + name, res[0]); // Not to stumble at tags in CDATA
		return res;
	}
	
	private String getValueString (Element parent) {
		return getFirstElement(parent, "valueString").getTextContent();
	}
	
	private void processContent (String data,
		String partName,
		String partConfiguration)
	{
		if ( partConfiguration.equals("default") ) {
			ITextUnit tu = new TextUnit(String.valueOf(++tuId));
			tu.setSourceContent(new TextFragment(data));
			tu.setMimeType(MimeTypeMapper.XML_MIME_TYPE);
			tu.setType("x-"+partName);
			queue.add(new Event(EventType.TEXT_UNIT, tu));
		}
		else {
			filter = fcMapper.createFilter(partConfiguration, filter);
			//encoderManager.mergeMappings(filter.getEncoderManager());
			//IEncoder encoder = encoderManager.getEncoder();
						
			IEncoder encoder = params.getUseCDATA() ? 
					new CDATAEncoder("UTF-8", lineBreak) :
					new XMLEncoder("UTF-8", lineBreak, true, false, false, QuoteMode.ALL);
					
			subFilter = new SubFilter(filter, 
					encoder, ++sectionIndex, partName, partName);
			
//			groupId.createId(); // Create new Id for this group
//			if ( filter instanceof AbstractMarkupFilter ) { // For IdGenerator try-out
//				// The root id of is made of: rootId + subDocId + groupId
//				FilterState state = new FilterState(FILTER_STATE.STANDALONE_TEXTUNIT, 
//						subDocId.getLastId(), null, null, null); 
//				subFilter.setState(state);
//				subFilter.open(new RawDocument(data, srcLoc));
//				while ( subFilter.hasNext() ) {
//					queue.add(subFilter.next());
//				}
//				subFilter.close();
//			}
//			else {
//				subFilter.open(new RawDocument(data, srcLoc));
//					
//				// Change the START_DOCUMENT to START_SUBFILTER
//				Event event = subFilter.next(); // START_DOCUMENT
//				//StartDocument sd = (StartDocument)event.getResource();
//				StartSubfilter sg = new StartSubfilter(subDocId.getLastId(), groupId.getLastId(),
//						event.getStartDocument()); // Group id already created
//				sg.setType("x-"+partName);
//				queue.add(new Event(EventType.START_SUBFILTER, sg));
//				
//				while ( subFilter.hasNext() ) {
//					event = subFilter.next();
//					if ( event.getEventType() == EventType.END_DOCUMENT ) {
//						break;
//					}
//					queue.add(event);
//				}
//				subFilter.close();
//	
//				// Change the END_DOCUMENT to END_SUBFILTER
//				EndSubfilter ending = new EndSubfilter(groupId.createId());
//				ending.setSkeleton(event.getResource().getSkeleton());
//				queue.add(new Event(EventType.END_SUBFILTER, ending));
//			}
			RawDocument rd = new RawDocument(data, srcLoc);
			queue.addAll(subFilter.getEvents(rd));
			queue.add(subFilter.createRefEvent());
			// subfilter should close this - but to get rid of warning
			rd.close();
		}
	}
	
	/**
	 * Gets the first element of a given name in the given parent element.
	 * The element must exist.
	 * @param parent the element where to element is a child. 
	 * @param name name of the element to get
	 * @return the first element of a given name in the given parent element.
	 */
	private Element getFirstElement (Element parent,
		String name)
	{
		NodeList nodes = parent.getElementsByTagName(name);
		return (Element)nodes.item(0);
	}

	/**
	 * Gets the first child element of a given parent.
	 * @param parent the parent.
	 * @return the first child element of a given parent or null.
	 */
	private Element getFirstElement (Element parent) {
		Node node = parent.getFirstChild();
		while ( true ) {
			if ( node == null ) {
				return null;
			}
			if ( node.getNodeType() == Node.ELEMENT_NODE ) {
				return (Element)node;
			}
			node = node.getNextSibling();
		}
	}

	private void generateListOfPaths () {
		logger.info("\nNumber of parts to localize = {}", listOfPaths.size());
		for ( String tmp : listOfPaths ) {
			logger.info(tmp);
		}
	}

}
