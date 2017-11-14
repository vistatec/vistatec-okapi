/*===========================================================================
  Copyright (C) 2008-2013 by the Okapi Framework contributors
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

package net.sf.okapi.filters.openoffice;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.MimeTypeMapper;
import net.sf.okapi.common.Namespaces;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.annotation.GenericAnnotation;
import net.sf.okapi.common.annotation.GenericAnnotationType;
import net.sf.okapi.common.annotation.GenericAnnotations;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.encoder.IEncoder;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.exceptions.OkapiIllegalFilterOperationException;
import net.sf.okapi.common.filters.FilterConfiguration;
import net.sf.okapi.common.filters.FilterUtil;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filters.IFilterConfigurationMapper;
import net.sf.okapi.common.filterwriter.GenericFilterWriter;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.filterwriter.ITSAnnotatorsRefContext;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.DocumentPart;
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.common.resource.TextUnit;
import net.sf.okapi.common.skeleton.GenericSkeleton;
import net.sf.okapi.common.skeleton.GenericSkeletonWriter;
import net.sf.okapi.common.skeleton.ISkeletonWriter;

import static net.sf.okapi.common.encoder.EncoderContext.TEXT;

/**
 * This class implements IFilter for XML documents in Open-Document format (ODF).
 * The expected input is the XML document itself. It can be used on ODF documents
 * that are not in Open-Office.org files (i.e. directly on the content.xml of the .odt).
 * For processing ODT, ODS, etc. documents, use the OpenOfficeFilter class,
 * which calls this filter as needed.
 */
public class ODFFilter implements IFilter {

	protected static final String NSURI_TEXT = "urn:oasis:names:tc:opendocument:xmlns:text:1.0";
	protected static final String NSURI_XLINK = "http://www.w3.org/1999/xlink";
	
	protected static final String TEXT_BOOKMARK_REF = "text:bookmark-ref";
	protected static final String OFFICE_ANNOTATION = "office:annotation";

	protected static final String DOCUMENT_TITLE = "dc:title";
	protected static final String DOCUMENT_DESCRIPTION = "dc:description";
	protected static final String DOCUMENT_SUBJECT = "dc:subject";
	protected static final String META_KEYWORD = "meta:keyword";
	protected static final String META_USER_DEFINED = "meta:user-defined";

	protected static final String META_NAME = "meta:name";

	protected static final String PAGE_NUMBER_TEXT = "text:page-number";
	protected static final String PAGE_COUNT_TEXT = "text:page-count";

	private static final String DEFAULT_ENCODER_CLASS_NAME = "net.sf.okapi.common.encoder.XMLEncoder";

//	private final Logger logger = LoggerFactory.getLogger(getClass());

	private Hashtable<String, ElementRule> toExtract;
	private Hashtable<String, AttributeRule> attrbutesToExtract;
	private ArrayList<String> toProtect;
	private ArrayList<String> subFlow;
	private LinkedList<Event> queue;
	private IEncoder elementEncoder;
	private String docName;
	private XMLStreamReader reader;
	private int otherId;
	private int tuId;
	private Parameters params;
	private GenericSkeleton skel;
	private TextFragment tf;
	private ITextUnit tu;
	private boolean canceled;
	private boolean hasNext;
	private int dynamicContentDepth = 0;
	private Stack<Context> context;
	private String lineBreak = "\n";
	private String containerMimeType;
	private EncoderManager encoderManager;
	private Stack<GenericAnnotations> annotations;
	private ITSAnnotatorsRefContext annotatorsRef;
	private RawDocument input;

	public ODFFilter () {
		toExtract = new Hashtable<String, ElementRule>();
		toExtract.put("text:p", new ElementRule("text:p", null));
		toExtract.put("text:h", new ElementRule("text:h", null));
		toExtract.put("dc:title", new ElementRule("dc:title", null));
		toExtract.put("dc:description", new ElementRule("dc:description", null));
		toExtract.put("dc:subject", new ElementRule("dc:subject", null));
		toExtract.put("meta:keyword", new ElementRule("meta:keyword", null));
		toExtract.put("meta:user-defined", new ElementRule("meta:user-defined", "meta:name"));
		toExtract.put("text:index-title-template", new ElementRule("text:index-title-template", null));
		
		attrbutesToExtract = new Hashtable<String, AttributeRule>();
		attrbutesToExtract.put("style:num-prefix", new AttributeRule("style:num-prefix", null));
		attrbutesToExtract.put("style:num-suffix", new AttributeRule("style:num-suffix", null));
		attrbutesToExtract.put("table:name", new AttributeRule("table:name", "application/vnd.oasis.opendocument.spreadsheet"));

		subFlow = new ArrayList<String>();
		subFlow.add("text:note");
		subFlow.add("office:annotation");
		
		toProtect = new ArrayList<String>();
		toProtect.add("text:initial-creator");
		toProtect.add("text:creation-date");
		toProtect.add("text:creation-time");
		toProtect.add("text:description");
		toProtect.add("text:user-defined");
		toProtect.add("text:print-time");
		toProtect.add("text:print-date");
		toProtect.add("text:printed-by");
		toProtect.add("text:editing-cycles");
		toProtect.add("text:editing-duration");
		toProtect.add("text:modification-time");
		toProtect.add("text:modification-date");
		toProtect.add("text:creator");
		toProtect.add("text:page-count");
		toProtect.add("text:paragraph-count");
		toProtect.add("text:word-count");
		toProtect.add("text:character-count");
		toProtect.add("text:table-count");
		toProtect.add("text:image-count");
		toProtect.add("text:object-count");
		toProtect.add("dc:date");
		toProtect.add("dc:creator");
		toProtect.add("text:note-citation");

		//TODO: Issue, protection not inherited by text:deletion, nor underlying p
		// so deleted text gets out
		toProtect.add("text:tracked-changes");

		toProtect.add("text:title"); // Content is defined elsewhere
		toProtect.add("text:subject"); // Content is defined elsewhere
		toProtect.add("text:keywords"); // Content is defined elsewhere

		toProtect.add(TEXT_BOOKMARK_REF); // Content is defined elsewhere

		toProtect.add(PAGE_NUMBER_TEXT);
		toProtect.add(PAGE_COUNT_TEXT);

		// Do it last to update the defaults if needed
		params = new Parameters();
		applyParameters();
	}

	public void close () {
		if (input != null) {
			input.close();
		}
		try {
			if ( reader != null ) {
				reader.close();
				reader = null;
			}
			hasNext = false;
		}
		catch ( XMLStreamException e) {
			throw new OkapiIOException(e);
		}
	}

	public void cancel () {
		canceled = true;
	}

	public boolean hasNext () {
		return hasNext;
	}

	public void open (RawDocument input) {
		open(input, true);
	}
	
	public void open (RawDocument input,
		boolean generateSkeleton)
	{
		this.input = input;
		applyParameters();
		canceled = false;
		containerMimeType = "";
		
		XMLInputFactory fact = XMLInputFactory.newInstance();
		fact.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, true);
		fact.setProperty(XMLInputFactory.IS_COALESCING, true);
		
		// security concern. Turn off DTD processing
		// https://www.owasp.org/index.php/XML_External_Entity_%28XXE%29_Processing
		fact.setProperty(XMLInputFactory.SUPPORT_DTD, false);			

		try {
			input.setEncoding("UTF-8"); // Force UTF-8 as the default encoding
			reader = fact.createXMLStreamReader(input.getStream());
		}
		catch ( XMLStreamException e ) {
			throw new OkapiIOException("Cannot create the XML stream.", e);
		}
		if ( input.getInputURI() != null ) {
			docName = input.getInputURI().getPath();
		}
		
		context = new Stack<Context>();
		context.push(new Context("", false));
		otherId = 0;
		tuId = 0;

		annotations = new Stack<GenericAnnotations>();
		annotatorsRef = new ITSAnnotatorsRefContext(reader);

		queue = new LinkedList<Event>();
		StartDocument startDoc = new StartDocument(String.valueOf(++otherId));
		startDoc.setLocale(input.getSourceLocale());
		startDoc.setName(docName);
		startDoc.setMimeType(MimeTypeMapper.ODF_MIME_TYPE);
		startDoc.setType(startDoc.getMimeType());
		//TODO: Fix the encoding as it is  not necessarily correct as the encoding is not retrieve from XMLStreamReader
		// We should use reader.getEncoding() when it's set
		startDoc.setEncoding("UTF-8", false);
		startDoc.setLineBreak(lineBreak);
		startDoc.setFilterParameters(params);
		startDoc.setFilterWriter(createFilterWriter());
		queue.add(new Event(EventType.START_DOCUMENT, startDoc));
		
		// load simplifier rules and send as an event
		if (!Util.isEmpty(((Parameters)getParameters()).getSimplifierRules())) {			
			Event cs = FilterUtil.createCodeSimplifierEvent(((Parameters)getParameters()).getSimplifierRules());
			queue.add(cs);
		}	
				
		hasNext = true;
	}

	/**
	 * Sets the MIME type of the file containing this document. This is the MIME type found
	 * in the mimetype file of the zip file.
	 * @param mimeType the MIME type to set.
	 */
	public void setContainerMimeType (String mimeType) {
		containerMimeType = mimeType;
	}
	
	public String getName () {
		return "okf_odf";
	}

	public String getDisplayName () {
		return "ODF-Content Filter (BETA)";
	}

	public String getMimeType () {
		return MimeTypeMapper.ODF_MIME_TYPE;
	}

	public List<FilterConfiguration> getConfigurations () {
		List<FilterConfiguration> list = new ArrayList<FilterConfiguration>();
		list.add(new FilterConfiguration(getName(),
			MimeTypeMapper.ODF_MIME_TYPE,
			getClass().getName(),
			"OpenDocument",
			"XML OpenDocument files (e.g. use inside OpenOffice.org documents)."));
		return list;
	}
	
	public EncoderManager getEncoderManager () {
		if ( encoderManager == null ) {
			encoderManager = new EncoderManager();
			encoderManager.setMapping(MimeTypeMapper.ODF_MIME_TYPE, DEFAULT_ENCODER_CLASS_NAME);
		}
		return encoderManager;
	}

	public Parameters getParameters () {
		return params;
	}

	public Event next () {
		// Treat cancel
		if ( canceled ) {
			queue.clear();
			queue.add(new Event(EventType.CANCELED));
			hasNext = false;
		}
		// Fill the queue if it's empty
		if ( queue.isEmpty() ) {
			read();
		}
		// Update hasNext flag on the FINISHED event
		if ( queue.peek().getEventType() == EventType.END_DOCUMENT ) {
			hasNext = false;
		}
		// Return the head of the queue
		return queue.poll();
	}
	
	public void setFilterConfigurationMapper (IFilterConfigurationMapper fcMapper) {
	}

	public void setParameters (IParameters newParams) {
		params = (Parameters)newParams;
	}

	public ISkeletonWriter createSkeletonWriter() {
		return new GenericSkeletonWriter();
	}

	public IFilterWriter createFilterWriter () {
		return new GenericFilterWriter(createSkeletonWriter(), getEncoderManager());
	}

	private void read () {
		skel = new GenericSkeleton();
		tf = new TextFragment();
		try {
			while ( reader.hasNext() ) {
				switch ( reader.next() ) {
				case XMLStreamConstants.CHARACTERS:
				case XMLStreamConstants.CDATA:
				case XMLStreamConstants.SPACE:
					if ( context.peek().extract ) {
						tf.append(reader.getText());
					}
					else { // UTF-8 element content: no escape of quote nor extended chars
						skel.append(Util.escapeToXML(reader.getText(), 0, false, null));
					}
					break;
					
				case XMLStreamConstants.START_DOCUMENT:
					//TODO set resource.setTargetEncoding(SET REAL ENCODING);
					skel.append("<?xml version=\"1.0\" "
						+ ((reader.getEncoding()==null) ? "" : "encoding=\""+reader.getEncoding()+"\"")
						+ "?>");
					break;
				
				case XMLStreamConstants.END_DOCUMENT:
					Ending ending = new Ending(String.valueOf(++otherId));
					ending.setSkeleton(skel);
					queue.add(new Event(EventType.END_DOCUMENT, ending));
					return;
				
				case XMLStreamConstants.START_ELEMENT:
					processStartElement();
					break;
				
				case XMLStreamConstants.END_ELEMENT:
					if ( processEndElement() ) return; // Send an event
					break;
				
				case XMLStreamConstants.COMMENT:
					if ( context.peek().extract ) {
						tf.append(TagType.PLACEHOLDER, null, "<!--" + reader.getText() + "-->");
					}
					else {
						skel.append("<!--" + reader.getText() + "-->");
					}
					break;

				case XMLStreamConstants.PROCESSING_INSTRUCTION:
					if ( context.peek().extract ) {
						tf.append(TagType.PLACEHOLDER, null,
							"<?" + reader.getPITarget() + " " + reader.getPIData() + "?>");
					}
					else {
						skel.append("<?" + reader.getPITarget() + " " + reader.getPIData() + "?>");
					}
					break;
				}
			} // End of main while		

		}
		catch ( XMLStreamException e ) {
			throw new OkapiIOException(e);
		}
	}

	private void setTUInfo (String name) {
		tu.setType("x-"+name);
		//lang?? 
		//id???
	}
	
	// Build the start tag name, and store it in skel if inSkeleton==true
	private String buildStartTag (String name,
		boolean inSkeleton )
	{
		StringBuilder tmp = new StringBuilder();
		// Tag name
		tmp.append("<" + name);
		
		// Namespaces
		String prefix;
		int count = reader.getNamespaceCount();
		for ( int i=0; i<count; i++ ) {
			prefix = reader.getNamespacePrefix(i);
			tmp.append(String.format(" xmlns%s=\"%s\"",
				((prefix!=null) ? ":"+prefix : ""),
				reader.getNamespaceURI(i)));
		}

		// Attributes
		count = reader.getAttributeCount();
		for ( int i=0; i<count; i++ ) {
			if ( !reader.isAttributeSpecified(i) ) continue; // Skip defaults
			String attributeName = getAttributeName(i);
			// Is this attribute translatable?
			if (attrbutesToExtract.containsKey(attributeName) ) {
				AttributeRule rule = attrbutesToExtract.get(attributeName);
				// Use indexOf to handle both normal non-template and template cases
				if (( rule.mimeType == null ) || ( containerMimeType.indexOf(rule.mimeType) == 0 )) {
					// This is translatable, should we extract?
					String text = reader.getAttributeValue(i);
					if ( hasTrueText(text) ) {
						// Create a text unit
						ITextUnit tu = new TextUnit(String.valueOf(++tuId));
						tu.setSourceContent(new TextFragment(text));
						tu.setIsReferent(true);
						tu.setMimeType(MimeTypeMapper.ODF_MIME_TYPE);
						tu.setType("x-" + attributeName);
						queue.add(new Event(EventType.TEXT_UNIT, tu));
						tmp.append(String.format(" %s=\"", attributeName));
						skel.append(tmp.toString());
						skel.addReference(tu);
						tmp.setLength(0); // Reset buffer
						tmp.append("\""); // End of attribute value
						continue; // Next attribute
					}
					// Else: fall thru
				}
				// Else: fall thru
			}
			// Not translatable
			tmp.append(String.format(" %s=\"%s\"", attributeName,
				Util.escapeToXML(reader.getAttributeValue(i), 3, false, null)));
		}

		tmp.append(">");
		if ( inSkeleton ) {
			skel.append(tmp.toString());
		}
		return tmp.toString();
	}
	
	private boolean hasTrueText (String text) {
		if ( Util.isEmpty(text) ) return false;
		for ( int i=0; i<text.length(); i++ ) {
			if ( Character.isLetter(text.charAt(i)) ) return true;
		}
		return false;
	}
	
	private String buildEndTag (String name) {
		return "</" + name + ">";
	}
	
	private String makePrintName () {
		String prefix = reader.getPrefix();
		if (( prefix == null ) || ( prefix.length()==0 )) {
			return reader.getLocalName();
		}
		// Else: with a prefix
		return prefix + ":" + reader.getLocalName();
	}
	
	private void processStartElement () throws XMLStreamException {
		String name = makePrintName();
//logger.debug("open-"+name);
		if ( toExtract.containsKey(name) && !isDynamicContent() ) {
			if (( context.size() > 1 ) || ( subFlow.contains(name) )) { // Use nested mode
				// Create the new id for the new sub-flow
				String id = String.valueOf(++tuId);
				// Add the reference to the current context
				if ( context.peek().extract ) {
					Code code = tf.append(TagType.PLACEHOLDER, name, TextFragment.makeRefMarker(id));
					code.setReferenceFlag(true);
				}
				else { // Or in the skeleton
					skel.addReference(tu);
				}
				// Create the new text unit
				tu = new TextUnit(id);
				// Set it as a referent, and set the info
				tu.setIsReferent(true);
				setTUInfo(name);
				// Create the new fragment and skeleton
				// And add the start tag of the sub-flow to the new skeleton
				tf = new TextFragment();
				skel = new GenericSkeleton();
				buildStartTag(name, true);
				// Set the new variables are the new context
				context.push(new Context(name, true));
				context.peek().setVariables(tf, skel, tu);
			}
			else { // Not nested
				// Send document-part if there is a non-whitespace skeleton
				if ( !skel.isEmpty(true) ) {
					DocumentPart dp = new DocumentPart(String.valueOf(++otherId), false);
					dp.setSkeleton(skel);
					queue.add(new Event(EventType.DOCUMENT_PART, dp));
					skel = new GenericSkeleton(); // Start new skeleton 
				}
				// Start the new text-unit (append it to skel)
				buildStartTag(name, true);
				//TODO: need a way to set the TextUnit's name/id/restype/etc.
				tu = new TextUnit(null); // ID set only if needed
				setTUInfo(name);
				context.push(new Context(name, true));
				context.peek().setVariables(tf, skel, tu);
			}
		}
		else if ( subFlow.contains(name) ) { // Is it a sub-flow (not extractable)
			// Create the new id for the new sub-flow
			String id = String.valueOf(++tuId);
			// Add the reference to the current context
			if ( context.peek().extract ) {
				Code code = tf.append(TagType.PLACEHOLDER, name, TextFragment.makeRefMarker(id));
				code.setReferenceFlag(true);
				// Create the new text unit
				tu = new TextUnit(id);
				// Set it as a referent, and set the info
				tu.setIsReferent(true);
				setTUInfo(name);
				// Create the new fragment and skeleton
				tf = new TextFragment();
				// Create the skeleton and add the start tag
				skel = new GenericSkeleton();
				buildStartTag(name, true);
				// Add the start-tag to the new context 
				// Set the new variables are the new context
				context.push(new Context(name, true));
				context.peek().setVariables(tf, skel, tu);
			}
			else { // Or in the skeleton
				// Send the existing skeleton if needed
				if ( !skel.isEmpty(true) ) {
					DocumentPart dp = new DocumentPart(String.valueOf(++otherId), false);
					dp.setSkeleton(skel);
					queue.add(new Event(EventType.DOCUMENT_PART, dp));
					skel = new GenericSkeleton(); // Start new skeleton 
				}
				// Create the new text unit
				tu = new TextUnit(id);
				// Set it as a referent, and set the info
				//tu.setIsReferent(true);
				setTUInfo(name);
				// Create the new fragment and skeleton
				tf = new TextFragment();
				// Create the skeleton and add the start tag
				skel = new GenericSkeleton();
				buildStartTag(name, true);
				// Add the start-tag to the new context 
				// Set the new variables are the new context
				context.push(new Context(name, true));
				context.peek().setVariables(tf, skel, tu);
			}
		}
		else if ( context.peek().extract && name.equals("text:s") ) {
			String tmp = reader.getAttributeValue(NSURI_TEXT, "c");
			if ( tmp != null ) {
				int count = Integer.valueOf(tmp);
				for ( int i=0; i<count; i++ ) {
					tf.append(" ");
				}
			}
			else tf.append(" "); // Default=1
			reader.nextTag(); // Eat the end-element event
		}
		else if ( context.peek().extract && name.equals("text:tab") ) {
			tf.append("\t");
			reader.nextTag(); // Eat the end-element event
		}
		else if ( context.peek().extract && name.equals("text:line-break") ) {
			tf.append(new Code(TagType.PLACEHOLDER, "lb", "<text:line-break/>"));
			reader.nextTag(); // Eat the end-element event
		}
		else if (ElementWithAttributesIndicatingDynamicContent.isSupported(name) && (hasAttributesIndicatingDynamicContent() || isDynamicContent())) {
			dynamicContentDepth++;
			buildStartTag(name, true);
		}
		else {
			if ( context.peek().extract ) {
				if ( name.equals("text:a") ) {
					processStartALink(name);
				}
				else if ( toProtect.contains(name) ) {
					processReadOnlyInlineElement(name);
				}
				else { // Else: normal content
					annotatorsRef.readAndPush();
					GenericAnnotations ga = readITSAnnotations();
					Code code = tf.append(TagType.OPENING, name, buildStartTag(name, false));
					if ( ga != null ) {
						GenericAnnotations.addAnnotations(code, ga);
					}
					annotations.push(ga); // Can push no annotations (null)
//logger.debug("push annotation:"+ga);
				}
			}
			else { // Append to skeleton
				buildStartTag(name, true);
//logger.debug("skel starttag: "+name);
			}
		}
	}

	private boolean hasAttributesIndicatingDynamicContent() {
		int attributeCount = reader.getAttributeCount();
		for (int i = 0; i < attributeCount; i++) {
			String attributeName = getAttributeName(i);
			if (AttributeIndicatingDynamicContent.isSupported(attributeName)) {
				return true;
			}
		}
		return false;
	}

	private String getAttributeName(int i) {
		String attributeName;
		String prefix = reader.getAttributePrefix(i);
		if (null == prefix) {
			attributeName = reader.getAttributeLocalName(i);
		} else {
			attributeName = prefix + ":" + reader.getAttributeLocalName(i);
		}
		return attributeName;
	}

	/**
	 * Processes the ITS annotation on the current element.
	 * <p>The method ITSAnnotatorsRefContext#readAndPush() must be called before calling this method.
	 * @return a set of generic annotation, or null of there are none.
	 */
	private GenericAnnotations readITSAnnotations () {
		GenericAnnotations anns = null;
		String val1, val2, val3;
		
		// AnnotatorsRef
		GenericAnnotation ga = annotatorsRef.getAnnotation();
		if ( ga != null ) {
			anns = new GenericAnnotations(ga);
		}

		// Check for ITS translate
		val1 = reader.getAttributeValue(Namespaces.ITS_NS_URI, "translate");
		if ( val1 != null ) {
			if ( anns == null ) anns = new GenericAnnotations();
			anns.add(new GenericAnnotation(GenericAnnotationType.TRANSLATE,
				GenericAnnotationType.TRANSLATE_VALUE, val1.equals("yes")));
			// Do output as a single inline code (nested not allowed)
			//TODO as an option?: processReadOnlyInlineElement(name);
			// then return;
		}

		// Check for ITS Locale Filter
		val1 = reader.getAttributeValue(Namespaces.ITS_NS_URI, "localeFilterList");
		if ( val1 != null ) {
			val2 = reader.getAttributeValue(Namespaces.ITS_NS_URI, "localeFilterType");
			if ( val2 != null ) {
				if ( val2.equals("exclude") ) val1 = "!"+val1;
			}
			if ( anns == null ) anns = new GenericAnnotations();
			anns.add(new GenericAnnotation(GenericAnnotationType.LOCFILTER,
				GenericAnnotationType.LOCFILTER_VALUE, val1));
		}
		
		// Check for ITS Terminology
		val1 = reader.getAttributeValue(Namespaces.ITS_NS_URI, "term");
		if ( val1 != null ) {
			val2 = reader.getAttributeValue(Namespaces.ITS_NS_URI, "termInfoRef");
			if ( val2 != null ) {
				val2 = GenericAnnotationType.REF_PREFIX+val2;
			}
			val3 = reader.getAttributeValue(Namespaces.ITS_NS_URI, "termConfidence");
			if ( anns == null ) anns = new GenericAnnotations();
			anns.add(new GenericAnnotation(GenericAnnotationType.TERM,
				GenericAnnotationType.TERM_INFO, val2,
				GenericAnnotationType.TERM_CONFIDENCE, (val3==null) ? null : Double.parseDouble(val3),
				GenericAnnotationType.ANNOTATORREF, annotatorsRef.getAnnotatorRef("terminology")));
		}
		
		// Check for ITS Localization Note
		val1 = reader.getAttributeValue(Namespaces.ITS_NS_URI, "locNote");
		if ( val1 == null ) {
			val1 = reader.getAttributeValue(Namespaces.ITS_NS_URI, "locNoteRef");
			if ( val1 != null ) {
				val1 = GenericAnnotationType.REF_PREFIX+val1;
			}
		}
		if ( val1 != null ) {
			val2 = reader.getAttributeValue(Namespaces.ITS_NS_URI, "locNoteType");
			if ( val2 == null ) {
				val2 = "description";
			}
			if ( anns == null ) anns = new GenericAnnotations();
			anns.add(new GenericAnnotation(GenericAnnotationType.LOCNOTE,
				GenericAnnotationType.LOCNOTE_VALUE, val1,
				GenericAnnotationType.LOCNOTE_TYPE, val2));
		}
	
		return anns;
	}
	
	private void processStartALink (String name) {
		String data = buildStartTag(name, false);
		String href = reader.getAttributeValue(NSURI_XLINK, "href");
		if ( href != null ) {
			//TODO: set the property, but where???
		}
		annotatorsRef.readAndPush();
		GenericAnnotations ga = readITSAnnotations();
		Code code = tf.append(TagType.OPENING, name, data);
		if ( ga != null ) {
			GenericAnnotations.addAnnotations(code, ga);
		}
		annotations.push(ga); // Can push no annotations (null)
//logger.debug("push annotation");
	}
	
	private void processReadOnlyInlineElement (String name) throws XMLStreamException {
		StringBuilder tmp = new StringBuilder(buildStartTag(name, false));
		int stack = 1;
		String tmpName;
		while ( true ) {
			switch ( reader.next() ) {
			case XMLStreamConstants.CHARACTERS:
				String text = reader.getText();
				if (params.getEncodeCharacterEntityReferenceGlyphs() && ElementWithEncodableCharacters.isSupported(name)) {
					tmp.append(getDefaultEncoder().encode(text, TEXT));
				} else {
					tmp.append(text);
				}
				break;
			case XMLStreamConstants.START_ELEMENT:
				tmpName = makePrintName();
				tmp.append(buildStartTag(tmpName, false));
				if ( tmpName.equals(name) ) stack++;
				break;
			case XMLStreamConstants.END_ELEMENT:
				tmpName = makePrintName();
				tmp.append(buildEndTag(tmpName));
				if ( tmpName.equals(name) ) {
					stack--;
					tf.append(new Code(TagType.PLACEHOLDER, name, tmp.toString()));
//logger.debug("inline ph: "+name+"[{}]", tmp.toString());
					if ( stack == 0 ) return;
				}
				break;
			case XMLStreamConstants.COMMENT:
				tmp.append("<!--" + reader.getText() + "-->");
				break;
			case XMLStreamConstants.PROCESSING_INSTRUCTION:
				tmp.append("<?" + reader.getPITarget() + " "
					+ reader.getPIData() + "?>");
				break;
			case XMLStreamConstants.START_DOCUMENT:
			case XMLStreamConstants.END_DOCUMENT:
				// Should not occur
				throw new OkapiIllegalFilterOperationException("Invalid start or end document detected while processing inline element.");
			}
		}		
	}

	/**
	 * This method shouldn't be called before reader is initialized,
	 * as there is no information about the encoding before that point.
	 *
	 * @return default encoder for the filter.
     */
	private IEncoder getDefaultEncoder() {
		if (null != elementEncoder) {
			return elementEncoder;
		}
		try {
			elementEncoder = (IEncoder) Class.forName(DEFAULT_ENCODER_CLASS_NAME).newInstance();
			elementEncoder.setOptions(new DefaultEncoderParameters(true, true, true), reader.getEncoding(), null);

			return elementEncoder;
		} catch (ClassNotFoundException | IllegalAccessException
				| InstantiationException | NullPointerException ex) {
			throw new OkapiException(ex);
		}
	}

	//TODO
/*	private int optimizeFront (TextFragment frag) {
		int trace = 0;
		String text = frag.getCodedText();
		char ch;
		for ( int i=0; i<text.length(); i++ ) {
			ch = text.charAt(i);
			if ( TextFragment.isMarker(ch) ) {
				
				i++;
			}
			else if ( !Character.isWhitespace(ch) ) {
				if ( trace == 0 ) {
					if ( i > 0 ) return i;
					else return -1; // Can't optimize
				}
				else return -1;
			}
		}
		return text.length()+1; // No text, only codes and/or whitespace
	}
*/	
	private void addTU (String name) {
		// Send a document part if there is no content
		// But if it's in nested context, the parent is already referring to tu, and
		// changing that reference to dp is hard, so we just send an empty tu
		if ( tf.isEmpty() && ( context.size() < 3 )) {
			DocumentPart dp = new DocumentPart(String.valueOf(++otherId), false);
			skel.append(buildEndTag(name)+lineBreak);
			dp.setSkeleton(skel);
			queue.add(new Event(EventType.DOCUMENT_PART, dp));
		}
		else { // Else: Send a text unit
			//TODO: Optimize the content to remove extra code that could be outside the text
			// place the text unit reference and add to the queue
			skel.addContentPlaceholder(tu);
			if ( tu.getId() == null ) tu.setId(String.valueOf(++tuId));
			tu.setSourceContent(tf);
			tu.setSkeleton(skel);
			tu.setMimeType(MimeTypeMapper.ODF_MIME_TYPE);
			// Add closing tag to the fragment or skeleton
			// Add line break because ODF files don't have any
			// They are needed for example in RTF output
			if ( tu.isReferent() ) skel.append(buildEndTag(name));
			else skel.append(buildEndTag(name)+lineBreak);
			queue.add(new Event(EventType.TEXT_UNIT, tu));
		}
	}
	
	// Return true when it's ready to send an event
	private boolean processEndElement () {
		String name = makePrintName();
//logger.debug("close-"+name);		
		if ( context.peek().extract && name.equals(context.peek().name) ) {
			if ( context.size() > 2 ) { // Is it a nested TU
				// Add it to the queue
				addTU(name);
				context.pop();
				// Reset the current variable to the correct context
				tf = context.peek().tf;
				tu = context.peek().tu;
				skel = context.peek().skel;
				// No trigger of the events yet
				return false;
			}
			else { // Not embedded, pop first
				context.pop();
				addTU(name);
				// Trigger the events to be sent
				return true;
			}
		}
		else if (isDynamicContent() && ElementWithAttributesIndicatingDynamicContent.isSupported(name)) {
			dynamicContentDepth--;
			skel.append(buildEndTag(name));
		}
		else {
			if ( context.peek().extract ) {
				Code code = new Code(TagType.CLOSING, name, buildEndTag(name));
				tf.append(code);
				// Set annotation data if the opening tag had any (null is OK)
//logger.debug("next will pop annotation: "+name);
				GenericAnnotations.addAnnotations(code, annotations.pop());
				annotatorsRef.pop();
			}
			else {
				skel.append(buildEndTag(name));
				// Add extra line for some element to make things
				// more readable in plain-text format
				if ( name.equals("style:style")
					|| ( name.equals("text:list-style"))
					|| ( name.equals("draw:frame"))
					|| ( name.equals("text:list"))
					|| ( name.equals("text:list-item")) ) {
					skel.append(lineBreak);
				}
			}
		}
		return false;
	}

	private boolean isDynamicContent() {
		return dynamicContentDepth > 0;
	}

	private void applyParameters () {
		// Update the driver lists if needed
		refineProtectionList(TEXT_BOOKMARK_REF, params.getExtractReferences());
		refineProtectionList(OFFICE_ANNOTATION, params.getExtractReferences());

		refineExtractionMap(DOCUMENT_TITLE, new ElementRule(DOCUMENT_TITLE, null),
				params.getExtractMetadata());
		refineExtractionMap(DOCUMENT_DESCRIPTION, new ElementRule(DOCUMENT_DESCRIPTION, null),
				params.getExtractMetadata());
		refineExtractionMap(DOCUMENT_SUBJECT, new ElementRule(DOCUMENT_SUBJECT, null),
				params.getExtractMetadata());
		refineExtractionMap(META_KEYWORD, new ElementRule(META_KEYWORD, null),
				params.getExtractMetadata());
		refineExtractionMap(META_USER_DEFINED, new ElementRule(META_USER_DEFINED, META_NAME),
				params.getExtractMetadata());
	}

	private void refineProtectionList(String element, boolean condition) {
		if (toProtect.contains(element)) {
			if (condition) {
				toProtect.remove(element);
			}
		} else {
			if (!condition) {
				toProtect.add(element);
			}
		}
	}

	private void refineExtractionMap(String element, ElementRule rule, boolean condition) {
		if (toExtract.containsKey(element)) {
			if (!condition) {
				toExtract.remove(element);
			}
		} else {
			if (condition) {
				toExtract.put(element, rule);
			}
		}
	}

	private enum AttributeIndicatingDynamicContent {
		TABLE_FORMULA("table:formula"),

		UNSUPPORTED("");

		String value;

		AttributeIndicatingDynamicContent(String value) {
			this.value = value;
		}

		static AttributeIndicatingDynamicContent fromValue(String value) {
			if (null == value) {
				return UNSUPPORTED;
			}

			for (AttributeIndicatingDynamicContent attributeIndicatingDynamicContent : values()) {
				if (attributeIndicatingDynamicContent.getValue().equals(value)) {
					return attributeIndicatingDynamicContent;
				}
			}

			return UNSUPPORTED;
		}

		static boolean isSupported(String value) {
			return AttributeIndicatingDynamicContent.fromValue(value) != AttributeIndicatingDynamicContent.UNSUPPORTED;
		}

		String getValue() {
			return value;
		}
	}

	private enum ElementWithAttributesIndicatingDynamicContent {
		TABLE_TABLE_CELL("table:table-cell"),

		UNSUPPORTED("");

		String value;

		ElementWithAttributesIndicatingDynamicContent(String value) {
			this.value = value;
		}

		static ElementWithAttributesIndicatingDynamicContent fromValue(String value) {
			if (null == value) {
				return UNSUPPORTED;
			}

			for (ElementWithAttributesIndicatingDynamicContent elementWithAttributesIndicatingDynamicContent : values()) {
				if (elementWithAttributesIndicatingDynamicContent.getValue().equals(value)) {
					return elementWithAttributesIndicatingDynamicContent;
				}
			}

			return UNSUPPORTED;
		}

		static boolean isSupported(String value) {
			return ElementWithAttributesIndicatingDynamicContent.fromValue(value) != ElementWithAttributesIndicatingDynamicContent.UNSUPPORTED;
		}

		String getValue() {
			return value;
		}
	}

	private enum ElementWithEncodableCharacters {
		PAGE_NUMBER_TEXT("text:page-number"),
		BOOKMARK_REFERENCE_TEXT(TEXT_BOOKMARK_REF),

		UNSUPPORTED("");

		String value;

		ElementWithEncodableCharacters(String value) {
			this.value = value;
		}

		static ElementWithEncodableCharacters fromValue(String value) {
			if (null == value) {
				return UNSUPPORTED;
			}

			for (ElementWithEncodableCharacters elementWithEncodableCharacters : values()) {
				if (elementWithEncodableCharacters.getValue().equals(value)) {
					return elementWithEncodableCharacters;
				}
			}

			return UNSUPPORTED;
		}

		static boolean isSupported(String value) {
			return ElementWithEncodableCharacters.fromValue(value) != ElementWithEncodableCharacters.UNSUPPORTED;
		}

		String getValue() {
			return value;
		}
	}

	private class DefaultEncoderParameters implements IParameters {
		private static final String ESCAPE_GT = "escapeGT";
		private static final String ESCAPE_NBSP = "escapeNbsp";
		private static final String ESCAPE_LINE_BREAK = "escapeLineBreak";

		private boolean escapeGt;
		private boolean escapeNbsp;
		private boolean escapeLineBreak;

		DefaultEncoderParameters (boolean escapeGt, boolean escapeNbsp, boolean escapeLineBreak) {
			this.escapeGt = escapeGt;
			this.escapeNbsp = escapeNbsp;
			this.escapeLineBreak = escapeLineBreak;
		}

		@Override
		public boolean getBoolean(String name) {
			switch (name) {
				case ESCAPE_GT:
					return escapeGt;
				case ESCAPE_NBSP:
					return escapeNbsp;
				case ESCAPE_LINE_BREAK:
					return escapeLineBreak;
			}
			return false;
		}

		@Override
		public void setBoolean(String name, boolean value) {}

		@Override
		public void reset() {}

		@Override
		public void fromString(String data) {}

		@Override
		public void load(URL inputURL, boolean ignoreErrors) {}

		@Override
		public void load(InputStream inStream, boolean ignoreErrors) {}

		@Override
		public void save(String filePath) {}

		@Override
		public String getPath() {
			return null;
		}

		@Override
		public void setPath(String filePath) {}

		@Override
		public String getString(String name) {
			return null;
		}

		@Override
		public void setString(String name, String value) {}

		@Override
		public int getInteger(String name) {
			return 0;
		}

		@Override
		public void setInteger(String name, int value) {}

		@Override
		public ParametersDescription getParametersDescription() {
			return null;
		}
	}
}
