/*===========================================================================
  Copyright (C) 2008-2016 by the Okapi Framework contributors
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

package net.sf.okapi.filters.its;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.its.ITSEngine;
import org.w3c.its.ITraversal;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.IdGenerator;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.annotation.GenericAnnotation;
import net.sf.okapi.common.annotation.GenericAnnotationType;
import net.sf.okapi.common.annotation.GenericAnnotations;
import net.sf.okapi.common.annotation.TermsAnnotation;
import net.sf.okapi.common.encoder.EncoderContext;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.encoder.IEncoder;
import net.sf.okapi.common.exceptions.OkapiBadFilterInputException;
import net.sf.okapi.common.exceptions.OkapiBadFilterParametersException;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.filters.FilterUtil;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filters.IFilterConfigurationMapper;
import net.sf.okapi.common.filters.InlineCodeFinder;
import net.sf.okapi.common.filters.SubFilter;
import net.sf.okapi.common.filterwriter.GenericFilterWriter;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.filterwriter.ITSContent;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.DocumentPart;
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.common.resource.TextUnit;
import net.sf.okapi.common.skeleton.GenericSkeleton;
import net.sf.okapi.common.skeleton.ISkeletonWriter;

public abstract class ITSFilter implements IFilter {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());

	protected Parameters params;
	protected EncoderManager encoderManager;
	protected Document doc;
	protected RawDocument input;
	protected String encoding;
	protected String docName;
	protected LocaleId srcLang;
	protected String lineBreak;
	protected boolean hasUTF8BOM;
	protected GenericSkeleton skel;
	protected IFilterConfigurationMapper fcMapper;
	
	private final String mimeType;
	private final boolean isHTML5;
	private boolean hasStandoffLocation;
	private boolean hasTargetPointer;
	
	private String trgLangCode; // can be null
	private ITSEngine trav;
	private long dataCategoriesToApply;
	private LinkedList<Event> queue;
	private int tuId;
	private IdGenerator otherId;
	private TextFragment frag;
	private Stack<ContextItem> context;
	private boolean canceled;
	private IEncoder cfEncoder;
	private TermsAnnotation terms;
	private Map<String, String> variables;
	private LinkedHashMap<String, GenericAnnotations> inlineLQIs;
	private boolean inNoEscapeContent;
	private int subfilterId = 1;
	private Map<Node, ITextUnit> attributeTargetPointersWithReferenceTUs = new HashMap<>();
	private Map<Node, ITextUnit> elementTargetPointersWithReferenceTUs = new HashMap<>();
	boolean skipExistingTargetText = false;

	public ITSFilter (boolean isHTML5,
		String mimeType,
		long dataCategoriesToApply)
	{
		this.isHTML5 = isHTML5;
		this.mimeType = mimeType;
		this.params = new Parameters();
		this.dataCategoriesToApply = dataCategoriesToApply;
	}
	
	/**
	 * Sets the ITS variables to pass to the ITS parameters feature.
	 * This method should be called before {@link #open(RawDocument, boolean)}.
	 * Those variables overwrite the default values set in the <code>its;params</code> elements.
	 * @param map the map of variables to pass. Can be null or empty.
	 */
	public void setITSVariables (Map<String, String> map) {
		variables = map;
	}

	@Override
	public void cancel () {
		canceled = true;
	}

	@Override
	public void close () {
		if (input != null) {
			input.close();
		}
	}

	@Override
	abstract public ISkeletonWriter createSkeletonWriter ();

	@Override
	public IFilterWriter createFilterWriter () {
		return new GenericFilterWriter(createSkeletonWriter(), getEncoderManager());
	}

	@Override
	public String getMimeType () {
		return mimeType;
	}

	@Override
	public Parameters getParameters () {
		return params;
	}

	@Override
	public boolean hasNext () {
		return (queue != null);
	}

	@Override
	public Event next () {
		if ( canceled ) {
			queue = null;
			return new Event(EventType.CANCELED);
		}
		if ( queue == null ) return null;

		// Process queue if it's not empty yet
		while ( true ) {
			if ( queue.size() > 0 ) {
				Event event = queue.poll();
				if ( event.getEventType() == EventType.END_DOCUMENT ) {
					queue = null;
				}
				return event;
			}

			// Process the next item, filling the queue
			process();
			// Ensure no infinite loop
			if ( queue.size() == 0 ) return null;
		}
	}

	@Override
	public void open (RawDocument input) {
		open(input, true);
	}
	
	@Override
	public void setFilterConfigurationMapper (IFilterConfigurationMapper fcMapper) {
		this.fcMapper = fcMapper;
	}

	@Override
	public void setParameters (IParameters params) {
		this.params = (Parameters)params;
	}
	
	abstract protected void initializeDocument ();
	
	protected void applyRules (ITSEngine itsEng) {
		// Applies all data categories by default
		itsEng.applyRules(dataCategoriesToApply);
	}
	
	abstract protected void createStartDocumentSkeleton (StartDocument startDoc);

	@Override
	public void open (RawDocument input,
		boolean generateSkeleton)
	{
		// Initializes the variables
		this.input = input;
		canceled = false;
		tuId = 0;
		otherId = new IdGenerator(null, "o");
		hasStandoffLocation = false;
		inNoEscapeContent = false;

		initializeDocument();
		
		// Allow no target locale too
		trgLangCode = null;
		if ( !Util.isNullOrEmpty(input.getTargetLocale()) ) {
			trgLangCode = input.getTargetLocale().toString().toLowerCase();
		}
		
		if ( params.useCodeFinder ) {
			params.codeFinder.compile();
		}

		// Create the ITS engine
		//trav = new ITSEngine(doc, input.getInputURI(), isHTML5, variables);
		trav = new ITSEngine(doc, input.getInputURI(), input.getEncoding(), isHTML5, variables);
		// Load the parameters file if there is one
		if ( params != null ) {
			if ( params.getDocument() != null ) {
				trav.addExternalRules(params.getDocument(), params.getURI());
			}
		}
		
		applyRules(trav);

		prepareTargetPointers();
		
		trav.startTraversal();
		context = new Stack<>();
		
		// Set the start event
		queue = new LinkedList<>();

		StartDocument startDoc = new StartDocument(otherId.createId());
		startDoc.setName(docName);
		String realEnc = doc.getInputEncoding();
		if ( realEnc != null ) encoding = realEnc;
		startDoc.setEncoding(encoding, hasUTF8BOM);
		startDoc.setLineBreak(lineBreak);
		startDoc.setLocale(srcLang);

		// Default quote mode == 3
		params.quoteModeDefined = true;
		params.quoteMode = 3; // quote is escaped, apos is not
		// Change the escapeQuotes option depending on whether translatable attributes rule
		// was triggered or not
		if ( !trav.getTranslatableAttributeRuleTriggered() ) {
			// Allow to not escape quotes only if there is no translatable attributes
			if ( !params.escapeQuotes ) {
				params.quoteModeDefined = true;
				params.quoteMode = 0; // quote and apos not escaped
			}
		}
		startDoc.setFilterParameters(params);
		
		startDoc.setFilterWriter(createFilterWriter());
		startDoc.setType(mimeType);
		startDoc.setMimeType(mimeType);

		createStartDocumentSkeleton(startDoc);
		startDoc.setSkeleton(skel);
		
		// Put the start document in the queue
		queue.add(new Event(EventType.START_DOCUMENT, startDoc));
		
		// load simplifier rules and send as an event
		if (!Util.isEmpty(params.getSimplifierRules())) {			
			Event cs = FilterUtil.createCodeSimplifierEvent(((Parameters)getParameters()).getSimplifierRules());
			queue.add(cs);
		}

		subfilterId = 1;
	}
	
	private void process () {
		Node node;
		nullFragment();
		skel = new GenericSkeleton();
		
		if ( context.size() > 0 ) {
			// If we are within an element, reset the fragment to append to it
			if ( context.peek().translate ) { // The stack is up-to-date already
				initFragment();
			}
		}
		
		while ( true ) {
			node = trav.nextNode();
			if ( node == null ) { // No more node: we stop
				Ending ending = new Ending(otherId.createId());
				if (( skel != null ) && ( !skel.isEmpty() )) {
					ending.setSkeleton(skel);
				}
				queue.add(new Event(EventType.END_DOCUMENT, ending));
				return;
			}
			
			// Else: valid node
			switch ( node.getNodeType() ) {
			case Node.CDATA_SECTION_NODE:
				if ( frag == null ) {
					skel.append(buildCDATA(node));
				}
				else {
					if ( extract() ) {
						if (params.inlineCdata) {
							frag.append(new Code(TagType.OPENING, Code.TYPE_CDATA, "<![CDATA["));
						}
						frag.append(node.getNodeValue());

						if (params.inlineCdata) {
							frag.append(new Code(TagType.CLOSING, Code.TYPE_CDATA, "]]>"));
						}
					}
					else {
						frag.append(TagType.PLACEHOLDER, null, buildCDATA(node));
					}
				}
				break;

			case Node.TEXT_NODE:
				if ( frag == null ) {//TODO: escape unsupported chars
					if ( skipExistingTargetText ) {
						skipExistingTargetText = false; // content placeholder for existing target text has already been inserted; no need to add text node contents (cf. #574)
					}
					else if ( inNoEscapeContent ) {
						skel.append(node.getNodeValue().replace("\n", lineBreak));
					}
					else {
						skel.append(Util.escapeToXML(
							node.getNodeValue().replace("\n", lineBreak), 0, false, null));
					}
				}
				else {
					if ( extract() ) {
						if ( params.lineBreakAsCode ) escapeAndAppend(frag, node.getNodeValue());
						else frag.append(node.getNodeValue());
					}
					else {//TODO: escape unsupported chars
						String text = node.getNodeValue().replace("\n", (params.escapeLineBreak ? "&#10;" : lineBreak));
						Code code = frag.append(TagType.PLACEHOLDER, null, Util.escapeToXML(text, 0, false, null));
						code.setCloneable(true); code.setDeleteable(true);
					}
				}
				break;
				
			case Node.ELEMENT_NODE:
				if ( processElementTag(node) ) return;
				break;
				
			case Node.PROCESSING_INSTRUCTION_NODE:
				if ( frag == null ) {
					skel.add(buildPI(node));
				}
				else {
					frag.append(TagType.PLACEHOLDER, null, buildPI(node));
				}
				break;
				
			case Node.COMMENT_NODE:
				if ( frag == null ) {
					skel.add(buildComment(node));
				}
				else {
					Code code = frag.append(TagType.PLACEHOLDER, null, buildComment(node));
					code.setCloneable(true); code.setDeleteable(true);
				}
				break;
				
			case Node.ENTITY_REFERENCE_NODE:
				// This get called only if params.protectEntityRef is true
				// Note: &lt;, etc. are not reported as entity references 
				if ( !trav.backTracking() ) {
					if ( frag == null ) {
						skel.add("&"+node.getNodeName()+";");
					}
					else {
						Code code = frag.append(TagType.PLACEHOLDER, Code.TYPE_REFERENCE, "&"+node.getNodeName()+";");
						code.setCloneable(true); code.setDeleteable(true);
					}
					
					// Some parsers provide the expanded content along with the reference node
					// If so, this needs to be swallowed (note that it can be nested)
					if ( node.hasChildNodes() ) {
						Node thisNode = node;
						do { // Read all the way down and back
							node = trav.nextNode();
						} while ( node != thisNode );
					}
					// It looks like the implementation of setExpandEntityReferences(false) is broken
					// the next node (rather than a child of this node) is generated with the expanded entity
					// Furthermore, the next node may have also the text after the entity reference.
				}
				// Else: Do not save the entity reference
				// Nothing to do, the content will be handled by TEXT_NODE	
				break;

			case Node.DOCUMENT_TYPE_NODE:
				// Handled in the start document process
				break;
			case Node.NOTATION_NODE:
				//TODO: handle notation nodes
				break;
			case Node.ENTITY_NODE:
				//TODO: handle entity nodes
				break;
			}
		}
	}

	private void escapeAndAppend (TextFragment frag,
		String text) 
	{
		for ( int i=0; i<text.length(); i++ ) {
			if ( text.charAt(i) == '\n' ) {
				Code code = frag.append(TagType.PLACEHOLDER, "lb", "&#10;");
				code.setCloneable(true); code.setDeleteable(true);
			}
			else {
				frag.append(text.charAt(i)); 
			}
		}
	}
	
	private void addStartTagToSkeleton (Node node) {
		StringBuilder tmp = new StringBuilder();
		tmp.append("<"
			+ ((node.getPrefix()==null) ? "" : node.getPrefix()+":")
			+ node.getLocalName());
		
		boolean checkEncoding = (isHTML5 && node.getLocalName().equals("meta"));
		
		if ( node.hasAttributes() ) {
			NamedNodeMap list = node.getAttributes();
			Attr attr;
			for ( int i=0; i<list.getLength(); i++ ) {
				attr = (Attr)list.item(i);

				if ( !attr.getSpecified() ) continue; // Skip auto-attributes

				tmp.append(" "
					+ ((attr.getPrefix()==null) ? "" : attr.getPrefix()+":")
					+ attr.getLocalName() + "=\"");

				// Add placeholder for existing target text, if needed
				if (this.attributeTargetPointersWithReferenceTUs.containsKey(attr)) {
					ITextUnit refValue = this.attributeTargetPointersWithReferenceTUs.get(attr);
					// Store the text part, add reference, and reset the buffer
					skel.append(tmp.toString());
					tmp.setLength(0);
					skel.addReference(refValue);
					tmp.append("\"");

					continue;
				}

				// Extract if needed
				if (( trav.getTranslate(attr) ) && ( attr.getValue().length() > 0 )) {
					// Store the text part and reset the buffer
					skel.append(tmp.toString());
					tmp.setLength(0);
					// Create the TU
					addAttributeTextUnit(attr, true);
					tmp.append("\"");
				}
				else if ( attr.getName().equals("xml:lang") ) {
					//String x = attr.getValue();
					//TODO: handle xml:lang
					tmp.append(Util.escapeToXML(attr.getNodeValue(), 3, false, null)
						+ "\"");
				}
				else if ( isHTML5 && attr.getName().equals("lang") ) {
					//String x = attr.getValue();
					//TODO: handle xml:lang
					tmp.append(Util.escapeToXML(attr.getNodeValue(), 3, false, null)
						+ "\"");
				}
				else if ( checkEncoding && attr.getName().equals("charset") ) {
					// For HTML5 documents: set the property placeholder for the encoding
					DocumentPart dp = new DocumentPart(otherId.createId(), false);
					dp.setProperty(new Property(Property.ENCODING, encoding));
					skel.append(tmp.toString());
					skel.addValuePlaceholder(dp, Property.ENCODING, LocaleId.EMPTY);
					skel.append("\"");
					queue.add(new Event(EventType.DOCUMENT_PART, dp, skel));
					// Reset the skleeton for next event
					skel = new GenericSkeleton();
					tmp.setLength(0);
				}
				else { //TODO: escape unsupported chars
					tmp.append(Util.escapeToXML(attr.getNodeValue(), 3, false, null)
						+ "\"");
				}
			}
		}
		if ( !isHTML5 && !node.hasChildNodes() ) tmp.append("/");
		else if ( isHTML5 && !node.hasChildNodes() ) {
			if ( !isHTML5VoidElement(node.getLocalName()) ) {
				tmp.append("></"
					+ ((node.getPrefix()==null) ? "" : node.getPrefix()+":")
					+ node.getLocalName());
			}
		}
		tmp.append(">");
		skel.append(tmp.toString());
	}

	private void addStartTagToFragment (Node node) {
		StringBuilder tmp = new StringBuilder();
		String id = null;
		tmp.append("<"
			+ ((node.getPrefix()==null) ? "" : node.getPrefix()+":")
			+ node.getLocalName());
		if ( node.hasAttributes() ) {
			NamedNodeMap list = node.getAttributes();
			Attr attr;
			for ( int i=0; i<list.getLength(); i++ ) {
				attr = (Attr)list.item(i);
				if ( !attr.getSpecified() ) continue; // Skip auto-attributes
				tmp.append(" "
					+ ((attr.getPrefix()==null) ? "" : attr.getPrefix()+":")
					+ attr.getLocalName() + "=\"");
				// Extract if needed
				if (( trav.getTranslate(attr) ) && ( attr.getValue().length() > 0 )) {
					id = addAttributeTextUnit(attr, false);
					tmp.append(TextFragment.makeRefMarker(id));
					tmp.append("\"");
				}
				else if ( attr.getName().equals("xml:lang") ) { // xml:lang
					tmp.append(Util.escapeToXML(attr.getNodeValue(), 3, false, null)
						+ "\"");
				}
//				else if ( isHTML5 && attr.getLocalName().startsWith("its-") ) {
//					// Strip out the ITS attributes, they will be re-generated on output
//				}
				else {
					tmp.append(Util.escapeToXML(attr.getNodeValue(), 3, false, null)
						+ "\"");
				}
			}
		}
		if ( !isHTML5 && !node.hasChildNodes() ) tmp.append("/");
		else if ( isHTML5 && !node.hasChildNodes() ) {
			if ( !isHTML5VoidElement(node.getLocalName()) ) {
				tmp.append("></"
					+ ((node.getPrefix()==null) ? "" : node.getPrefix()+":")
					+ node.getLocalName());
			}
		}
		tmp.append(">");
		// Set the inline code
		Code code = frag.append((node.hasChildNodes() ? TagType.OPENING : TagType.PLACEHOLDER),
			node.getLocalName(), tmp.toString());
		code.setReferenceFlag(id!=null); // Set reference flag if we created TU(s)
		// Attach ITS annotation if needed
		attachAnnotations(code, frag, (Element)node);
		code.setCloneable(true); code.setDeleteable(true);
	}
	
	/**
	 * Attaches the annotations of the current node to a give code.
	 * @param code the code (corresponding to the node being processed).
	 * @param frag the fragment where the inline code has been added.
	 * @param elem the element to which the code corresponds.
	 */
	private void attachAnnotations (Code code,
		TextFragment frag,
		Element elem)
	{
		// Map ITS annotations only if requested
		if ( !params.mapAnnotations ) return;
		
		// ITS annotators reference
		String value = trav.getAnnotatorsRef();
		Map<String, String> annotatorsRef = null;
		if ( value != null ) {
			GenericAnnotation.addAnnotation(code, new GenericAnnotation(GenericAnnotationType.ANNOT,
				GenericAnnotationType.ANNOT_VALUE, value));
			annotatorsRef = ITSContent.annotatorsRefToMap(value);
		}
		
		// ITS Localization Note
		value = trav.getLocNote(null);
		if ( value != null ) {
			String type = trav.getLocNoteType(null);
			GenericAnnotation.addAnnotation(code, new GenericAnnotation(GenericAnnotationType.LOCNOTE,
				GenericAnnotationType.LOCNOTE_VALUE, value,
				GenericAnnotationType.LOCNOTE_TYPE, type));
		}
		
		// ITS Domain
		// Not supported on an inline code
		
		// ITS Text Analysis
		GenericAnnotations anns = trav.getTextAnalysisAnnotation(null);
		if ( anns != null ) {
			anns.getFirstAnnotation(GenericAnnotationType.TA).setString(
				GenericAnnotationType.ANNOTATORREF, getAnnotatorRef(annotatorsRef, "text-analysis"));
			GenericAnnotations.addAnnotations(code, anns);
		}
		
		// ITS External resource
		StringBuilder extResList = new StringBuilder();
		value = trav.getExternalResourceRef(null);
		if ( value != null ) {
			extResList.append(value);
		}
		// Check the attributes for external resource metadata
		NamedNodeMap map = elem.getAttributes();
		for ( int i=0; i<map.getLength(); i++ ) {
			Attr attr = (Attr)map.item(i);
			value = trav.getExternalResourceRef(attr);
			if ( value != null ) {
				if ( extResList.length() > 0 ) extResList.append(" ");
				extResList.append(value);
			}
		}
		if ( extResList.length() > 0 ) {
			GenericAnnotation.addAnnotation(code, new GenericAnnotation(GenericAnnotationType.EXTERNALRES,
				GenericAnnotationType.EXTERNALRES_VALUE, extResList.toString()));
		}
		
		// ITS MT Confidence
		// For now the mapping says it's not supported at the inline level.

		// ITS Storage Size
		anns = trav.getStorageSizeAnnotation(null);
		if ( anns != null ) {
			GenericAnnotations.addAnnotations(code, anns);
		}
		
		// ITS Allowed Characters
		value = trav.getAllowedCharacters(null);
		if ( value != null ) {
			GenericAnnotation.addAnnotation(code, new GenericAnnotation(GenericAnnotationType.ALLOWEDCHARS,
				GenericAnnotationType.ALLOWEDCHARS_VALUE, value));
		}
		
		// ITS Terminology
		anns = trav.getTerminologyAnnotation(null);
		if ( anns != null ) {
			anns.getFirstAnnotation(GenericAnnotationType.TERM).setString(
				GenericAnnotationType.ANNOTATORREF, getAnnotatorRef(annotatorsRef, "terminology"));
			GenericAnnotations.addAnnotations(code, anns);
		}
		
		// ITS Localization Quality Rating
		anns = trav.getLocQualityRatingAnnotation();
		if ( anns != null ) {
			GenericAnnotations.addAnnotations(code, anns);
		}
		
		// Localization Quality Issues (this must be the last one we check
		// because we may return from this)
		anns = trav.getLocQualityIssueAnnotations(null);
		if ( anns == null ) return; // Done
		// Else: inline LQI are converted to text container-level annotation with offsets 
		if ( code.getTagType() == TagType.CLOSING ) {
			// Set the ending for the annotations we close
			anns = inlineLQIs.get(anns.getData());
			for ( GenericAnnotation ann : anns ) {
				// End position: frag length minus the last code length
				ann.setInteger(GenericAnnotationType.LQI_XEND, frag.length()-2);
			}
		}
		else { // Opening or place-holder
			if ( inlineLQIs == null ) inlineLQIs = new LinkedHashMap<>(2);
			for ( GenericAnnotation ann : anns ) {
				// Start position: fragment length (== position just after the last code)
				ann.setInteger(GenericAnnotationType.LQI_XSTART, frag.length());
			}
			inlineLQIs.put(anns.getData(), anns);
		}
		
	}

	
	private void applyCodeFinder (TextFragment tf) {
		// Find the inline codes
		params.codeFinder.process(tf);
		// Escape inline code content
		List<Code> codes = tf.getCodes();
		for ( Code code : codes ) {
			// Escape the data of the new inline code (and only them)
			if ( code.getType().equals(InlineCodeFinder.TAGTYPE) ) {
				if ( cfEncoder == null ) {					
					encoderManager.setDefaultOptions(getParameters(), this.encoding, this.lineBreak);
					encoderManager.updateEncoder(getMimeType());
					cfEncoder = getEncoderManager().getEncoder();
					//TODO: We should use the proper output encoding here, not force UTF-8, but we do not know it
					cfEncoder.setOptions(params, "utf-8", lineBreak);
				}
				code.setData(cfEncoder.encode(code.getData(), EncoderContext.TEXT));
			}
		}
	}
	
	private String addAttributeTextUnit (Attr attr,
		boolean addToSkeleton)
	{
		String id = String.valueOf(++tuId);
		ITextUnit tu = new TextUnit(id, attr.getValue(), true, mimeType);
		tu.setType("x-"+attr.getLocalName());
		
		// Deal with inline codes if needed
		if ( params.useCodeFinder ) {
			applyCodeFinder(tu.getSource().getFirstContent());
		}

		// We could have target entries, too, in some cases (ITS targetPointer) so we test for that
		// and process the target, if needed. Note that the attribute node is assumed to be the source node.
		TargetPointerEntry tpe = (TargetPointerEntry) attr.getUserData(TargetPointerEntry.SRC_TRGPTRFLAGNAME);
		Node targetPointerTargetNode = null;
		if ( tpe != null ) {
			targetPointerTargetNode = tpe.getTargetNode();

			if ( tpe != null && targetPointerTargetNode != null ) {
				tu.setTargetContent(LocaleId.fromString(this.trgLangCode), new TextFragment(tpe.getTargetNode().getTextContent()));
			}
		}

		// Set the ITS context for this attribute and set the relevant properties
		// (we could use directly trav, but this allows to avoid many trav.getXYZ() calls)
		// Note however, that trav should be used in some cases where the context alone is not enough
		ContextItem ci = new ContextItem(
			(context.isEmpty() ? attr.getParentNode() : context.peek().node), trav, attr);
		processTextUnit(tu, ci, null, attr);
		
		queue.add(new Event(EventType.TEXT_UNIT, tu));

		if ( addToSkeleton ) {
			if (tpe == null || targetPointerTargetNode == null) {
				if (attributeTargetPointersWithReferenceTUs.containsKey(attr))
					skel.addReference(attributeTargetPointersWithReferenceTUs.get(attr));
				else
					skel.addReference(tu);
			}
			else { // if there is a target pointer, leave source text in attribute intact and set reference for target pointer
				skel.add(attr.getValue());
				attributeTargetPointersWithReferenceTUs.put(targetPointerTargetNode, tu);
			}
		}

		return id;
	}

	private String buildEndTag (Node node) {
		if ( node.hasChildNodes() ) {
			return "</"
				+ ((node.getPrefix()==null) ? "" : node.getPrefix()+":")
				+ node.getLocalName() + ">";
		}
		else { // Start tag was set as an empty element
			return "";
		}
	}
	
	private String buildPI (Node node) {
		// Do not escape PI content
		return "<?" + node.getNodeName() + " " + node.getNodeValue() + "?>";
	}
	
	private String buildCDATA (Node node) {
		// Do not escape CDATA content
		return "<![CDATA[" + node.getNodeValue().replace("\n", lineBreak) + "]]>";
	}
	
	private String buildComment (Node node) {
		// Do not escape comments
		return "<!--" + node.getNodeValue().replace("\n", lineBreak) + "-->";
	}

	/**
	 * Processes the start or end tag of an element node.
	 * @param node Node to process.
	 * @return True if we need to return, false to continue processing.
	 */
	private boolean processElementTag (Node node) {

		if ( trav.backTracking() ) {

			if ( trav.getTerm(null) ) {
				if ( terms == null ) {
					terms = new TermsAnnotation();
				}
				terms.add(node.getTextContent(), trav.getTermInfo(null));
			}
			
			// Check for standoff insertion point
			if ( isHTML5 && node.getNodeName().equals("body") && !hasStandoffLocation ) {
				hasStandoffLocation = true;
				if ( frag != null ) { // Close the previous skeleton if needed
					addTextUnit(node, false, null /* targetText */);
				}
				// Add the marker for the standoff markup location
				// First: flush any existing skeleton parts
				if ( !skel.isEmpty() ) {
					DocumentPart dp = new DocumentPart(otherId.createId(), false, skel);
					queue.add(new Event(EventType.DOCUMENT_PART, dp));
					skel = new GenericSkeleton();
				}
				// Then create the skeleton with the placeholder
				skel.add(ITSContent.STANDOFFMARKER);
				DocumentPart dp = new DocumentPart(otherId.createId(), false, skel);
				queue.add(new Event(EventType.DOCUMENT_PART, dp));
				skel = new GenericSkeleton();
			}

			if ( frag == null ) { // Not an extraction: in skeleton
				skel.add(buildEndTag(node));
				if ( node.isSameNode(context.peek().node) ) context.pop();
				if ( isContextTranslatable() ) { // We are after non-translatable withinText='no', check parent again.
					initFragment();
				}
			}
			else { // Else we are within an extraction
				if ( node.isSameNode(context.peek().node) ) { // End of possible text-unit
					TextFragment existingTarget = null;

					TargetPointerEntry tpe = getTargetPointerEntry(node);
					if ( tpe != null ) {
						Node existingTargetNode = tpe.getTargetNode();

						if (existingTargetNode != null)
							existingTarget = new TextFragment(existingTargetNode.getTextContent());
					}

					return addTextUnit(node, true, existingTarget);
				}
				else { // Within text
					Code code = frag.append(TagType.CLOSING, node.getLocalName(), buildEndTag(node));
					attachAnnotations(code, frag, (Element)node);
					code.setCloneable(true); code.setDeleteable(true);
				}
			}

			// Checks if we are closing a no-escape element
			if ( isHTML5 && "script|style".indexOf(node.getLocalName()) != -1 ) {
				inNoEscapeContent = false;
			}
		}
		else { // Else: Start tag
			// Test if this node is involved in a source/target pair
			if ( hasTargetPointer ) {
				// NOTE MW: Nothing to do here really. Target text is taken care of by setting references.
//				TargetPointerEntry tpe = getTargetPointerEntry(node);
//				if ( tpe != null ) {
//					if ( tpe.getTargetNode() == node ) {
//						// This node is a target location
//						tpe.toString();
//						Node target = tpe.getTargetNode();
//					}
//					else {
//						// This node is a source with a target location
//						// TODO
//						tpe.toString();
//					}
//				}
			}
			
			if ( trav.getSubFilter(null) != null ) {
				processSubFilterContent(node, ((ITSEngine)trav).getSubFilter(null));
				moveToEnd(node); // Move to the end of this node
				return true; // Send the events
			}
			
			// Otherwise, treat the tag
			switch ( trav.getWithinText() ) {
			case ITraversal.WITHINTEXT_NESTED: //TODO: deal with nested elements
				// For now treat them as inline
			case ITraversal.WITHINTEXT_YES:
				if ( frag == null ) { // Not yet in extraction					
					// case of inline within non-translatable parent
					// but the translatable rule may not have applied yet and withinText implies translatable
					// so create a frag and add node to frag if future extraction possible
					// see https://bitbucket.org/okapiframework/okapi/issue/470/xml-its-filter-is-placing-extractable-text		
					if ( extract() ) {
						initFragment();					
						addStartTagToFragment(node);
					} else {
						addStartTagToSkeleton(node);
						if ( node.hasChildNodes() ) {
							context.push(new ContextItem(node, trav));
						}
					}					
				}
				else { // Already in extraction
					addStartTagToFragment(node);
				}
				break;
			default: // Not within text
				if ( frag == null ) { // Not yet in extraction
					addStartTagToSkeleton(node);

					// Check for existing target pointer contents; replace existing target text with content placeholder
					// (attributes with target pointers are dealt with during attribute procesing)
					if ( this.elementTargetPointersWithReferenceTUs.containsKey(node) ) {
						skel.addContentPlaceholder(elementTargetPointersWithReferenceTUs.get(node));
						if (node.hasChildNodes())
							skipExistingTargetText = true;
					}

					if ( extract() ) {
						initFragment();
					}
					if ( node.hasChildNodes() ) {
						context.push(new ContextItem(node, trav));
					}
					// Check if we are opening a no-escape element
					if ( isHTML5 && "script|style".indexOf(node.getLocalName()) != -1 ) {
						inNoEscapeContent = true;
					}
				}
				else { // Already in extraction
					// Queue the current item
					addTextUnit(node, false /* popStack */, null /* targetText */);
					addStartTagToSkeleton(node);

					// And create a new one
					if ( extract() ) {
						initFragment();
					}
					if ( node.hasChildNodes() ) {
						context.push(new ContextItem(node, trav));
					}
				}
				break;
			}
		}
		return false;
	}
	
	private void moveToEnd (Node start) {
		Node node = null;
		while ( (node = trav.nextNode()) != null ) {
			if ( node == start ) return;
		}
	}
	
	/**
	 * Process a content with a given sub-filter.
	 * @param node the node to process.
	 * @param configId the sub-filter configuration identifier.
	 */
	private void processSubFilterContent (Node node,
		String configId)
	{
		// Create the skeleton for the start tag
		// This will be used later
		addStartTagToSkeleton(node);

		// Instantiate the filter to use as sub-filter
		IFilter sf = fcMapper.createFilter(configId, null);
		if ( sf == null ) {
			throw new OkapiBadFilterInputException(String.format("Could not instantiate subfilter '%s'.", configId));
		}
		
		// Create the sub-filter wrapper
		// First, make sure we have defaults and set the default encoder
		//TODO: Issue: the encoding is the input encoding, not the output one
		encoderManager.setDefaultOptions(getParameters(), this.encoding, this.lineBreak);
		encoderManager.updateEncoder(getMimeType());
		// Then create the sub-filter
		SubFilter subfilter = new SubFilter(sf, 
			getEncoderManager().getEncoder(),
			subfilterId++, // sectionIndex
			"parentId",
			null // Parent name
			);

		// Process the content
		String content = node.getTextContent();
		subfilter.open(new RawDocument(content, srcLang));
		while (subfilter.hasNext()) {
			queue.add(subfilter.next());
		}
		subfilter.close();
		
		// Create the skeleton for the end tag
		GenericSkeleton skelAfter = new GenericSkeleton();
		skelAfter.add(buildEndTag(node));
		
		// Create the document part holding the re-writing mechanism
		queue.add(subfilter.createRefEvent(skel, skelAfter));
		
		// Just make sure this skeleton is reset for next time
		skel = new GenericSkeleton();
	}

	/**
	 * Indicates if the current node is to be extracted according its ITS state.
	 * @return true if it is to be extracted, false otherwise.
	 */
	private boolean extract () {
		// Check ITS translate
		if ( !trav.getTranslate(null) ) return false;
		
		// Check ITS locale filter
		String list = trav.getLocaleFilter();
		// null is none-defined, so default is '*'
		if (( list == null ) || list.equals("*") ) return true;
		if ( list.isEmpty() || list.equals("!*") ) return false;
		
		// More info for extended language range/filtering here:
		// http://www.rfc-editor.org/rfc/bcp/bcp47.txt
		if ( trgLangCode == null ) {
			// Log a warning that the data category cannot be used
			logger.warn("No target locale specified: Cannot use the provided ITS Locale Filter data category.");
			return true;
		}
		// Now check with one or more codes
		return ITSContent.isExtendedMatch(list, trgLangCode);
	}
	
	private boolean isContextTranslatable () {
		if ( context.size() == 0 ) return false;
		return context.peek().translate;
	}
	
	/**
	 * Adds a text unit to the queue if needed.
	 * @param node The current node.
	 * @param popStack True to pop the stack, false to leave the stack alone.
	 * @param targetText Optional existing target text which will be added to the textunit created here.
	 * @return True if a text unit was added to the queue, false otherwise.
	 */
	private boolean addTextUnit (Node node, boolean popStack, TextFragment targetText) {
		// Extract if there is some text, or if there is code and we always extract codes
		boolean extract = frag.hasText(false)
			|| ( params.extractIfOnlyCodes && frag.hasCode() );
		
		if ( extract ) {
			// Deal with inline codes if needed
			if ( params.useCodeFinder ) {
				applyCodeFinder(frag);
//TODO: probably need to adjust the LQI annotations!				
			}
		
			// Update the flag after the new codes
			extract = frag.hasText(false)
				|| ( params.extractIfOnlyCodes && frag.hasCode() );
		}
		if ( !extract ) {
			if ( !frag.isEmpty() ) { // Nothing but white spaces
				skel.add(frag.toText().replace("\n", (params.escapeLineBreak ? "&#10;" : lineBreak))); // Pass them as skeleton
			}
			nullFragment();
			if ( popStack ) {
				context.pop();
				skel.add(buildEndTag(node));
				if ( isContextTranslatable() ) {
					initFragment();
				}
			}
			return false;
		}

		// Create the unit
		ITextUnit tu = new TextUnit(String.valueOf(++tuId));
		tu.setMimeType(mimeType);
		tu.setSourceContent(frag);
		if (this.trgLangCode != null && targetText != null) {
			tu.setTargetContent(LocaleId.fromString(this.trgLangCode), targetText);
		}

		processTextUnit(tu, context.peek(), node.getNodeName(), null);

		// Process the skeleton
		// add place holder only if there is no targetpointer for this node;
		// otherwise, leave source text intact and add content placeholder to target element later
		if ( !hasTargetPointer(node) ) {
			if (elementTargetPointersWithReferenceTUs.containsKey(node))
				skel.addContentPlaceholder(elementTargetPointersWithReferenceTUs.get(node));
			else
				skel.addContentPlaceholder(tu);
		} else {
			skel.add(node.getTextContent());
			elementTargetPointersWithReferenceTUs.put(getTargetPointerTarget(node), tu);
		}

		if ( popStack ) {
			context.pop();
			skel.add(buildEndTag(node));
		}
		tu.setSkeleton(skel);
		queue.add(new Event(EventType.TEXT_UNIT, tu));
		nullFragment();
		if ( popStack && isContextTranslatable() ) {
			initFragment();
		}
		skel = new GenericSkeleton();
		return true;
	}

	private Node getTargetPointerTarget(Node node) {
		if (node.getUserData(TargetPointerEntry.SRC_TRGPTRFLAGNAME) != null)
			return ((TargetPointerEntry) node.getUserData(TargetPointerEntry.SRC_TRGPTRFLAGNAME)).getTargetNode();
		else if (node.getUserData(TargetPointerEntry.TRG_TRGPTRFLAGNAME) != null)
			return ((TargetPointerEntry) node.getUserData(TargetPointerEntry.TRG_TRGPTRFLAGNAME)).getTargetNode();
		else
			return null;
	}

	private boolean hasTargetPointer(Node node) {
		return (node.getUserData(TargetPointerEntry.SRC_TRGPTRFLAGNAME) != null || node.getUserData(TargetPointerEntry.TRG_TRGPTRFLAGNAME) != null);
	}

	/**
	 * Gets the annotator reference for a given data category from a given map.
	 * @param map the map to look up (can be null).
	 * @param dataCategory the data category to search for.
	 * @return the reference for the given data category
	 * or null if the map is null or the reference is not found.
	 */
	private String getAnnotatorRef (Map<String, String> map,
		String dataCategory)
	{
		if ( map == null ) return null;
		return map.get(dataCategory);
	}
	
	/**
	 * Sets the annotations and performs various pre-extraction processes
	 * for a given text unit.
	 * @param tu the text unit to process.
	 * @param ci the context of this text unit.
	 * @param nodeName the name of the node (element ot attribute name)
	 * @param attribute the attribute corresponding to this text unit, or null if we process an element.
	 */
	private void processTextUnit (ITextUnit tu,
		ContextItem ci,
		String nodeName,
		Attr attribute)
	{
		// ITS annotators reference
		String value = trav.getAnnotatorsRef();
		Map<String, String> annotatorsRef = null;
		if ( value != null ) {
			GenericAnnotation.addAnnotation(tu, new GenericAnnotation(GenericAnnotationType.ANNOT,
				GenericAnnotationType.ANNOT_VALUE, value));
			annotatorsRef = ITSContent.annotatorsRefToMap(value);
		}
		
		// ITS Localization Note
		if ( !Util.isEmpty(ci.locNote) ) {
			tu.setProperty(new Property(Property.NOTE, ci.locNote));
			String type = trav.getLocNoteType(attribute);
			GenericAnnotation.addAnnotation(tu, new GenericAnnotation(GenericAnnotationType.LOCNOTE,
				GenericAnnotationType.LOCNOTE_VALUE, ci.locNote,
				GenericAnnotationType.LOCNOTE_TYPE, type));
		}
		
		// ITS Domain
		if ( !Util.isEmpty(ci.domains) ) {
			GenericAnnotation.addAnnotation(tu, new GenericAnnotation(GenericAnnotationType.DOMAIN,
				GenericAnnotationType.DOMAIN_VALUE, ci.domains)
			);
		}
		
		// ITS Text Analysis
		if ( ci.ta != null ) {
			ci.ta.getFirstAnnotation(GenericAnnotationType.TA).setString(
				GenericAnnotationType.ANNOTATORREF, getAnnotatorRef(annotatorsRef, "text-analysis"));
			GenericAnnotations.addAnnotations(tu.getSource(), ci.ta);
		}
		
		// ITS External resource
		if ( !Util.isEmpty(ci.externalRes) ) {
			GenericAnnotation.addAnnotation(tu, new GenericAnnotation(GenericAnnotationType.EXTERNALRES,
				GenericAnnotationType.EXTERNALRES_VALUE, ci.externalRes)
			);
		}
		//TODO: Look at attributes for this element for additional external resource
		
		// ITS MT confidence
		if ( ci.mtConfidence != null ) {
			GenericAnnotation.addAnnotation(tu, new GenericAnnotation(GenericAnnotationType.MTCONFIDENCE,
				GenericAnnotationType.MTCONFIDENCE_VALUE, ci.mtConfidence,
				GenericAnnotationType.ANNOTATORREF, getAnnotatorRef(annotatorsRef, "mt-confidence"))
			);
		}
		
		// ITS Storage Size
		if ( ci.storageSize != null ) {
			GenericAnnotations.addAnnotations(tu.getSource(), ci.storageSize);
		}
		
		// ITS Allowed characters
		if ( ci.allowedChars != null ) {
			GenericAnnotation.addAnnotation(tu.getSource(), new GenericAnnotation(GenericAnnotationType.ALLOWEDCHARS,
				GenericAnnotationType.ALLOWEDCHARS_VALUE, ci.allowedChars)
			);
		}
		
		// ITS Provenance
		if ( ci.prov != null ) {
			GenericAnnotations.addAnnotations(tu, ci.prov);
		}
		
		// ITS Localization Quality Rating
		if ( ci.lqRating != null ) {
			GenericAnnotations.addAnnotations(tu.getSource(), ci.lqRating);
		}
		
		// ITS Localization Quality Issue
		if ( ci.lqIssues != null ) {
			GenericAnnotations.addAnnotations(tu.getSource(), ci.lqIssues);
		}
		
		// Attach also the inline LQI annotations at the text container level
		// (more logical to have the inline ones after the parent level ones)
		if ( inlineLQIs != null ) {
			for ( GenericAnnotations anns : inlineLQIs.values() ) {
				GenericAnnotations.addAnnotations(tu.getSource(), anns);
			}
		}
		
		// ITS Terminology
		if ( ci.terminology != null ) {
			ci.terminology.getFirstAnnotation(GenericAnnotationType.TERM).setString(
				GenericAnnotationType.ANNOTATORREF, getAnnotatorRef(annotatorsRef, "terminology"));
			GenericAnnotations.addAnnotations(tu.getSource(), ci.terminology);
		}
		
		// Backward compatibility: Set term info
		if ( terms != null ) {
			tu.getSource().setAnnotation(terms);
//			// Term as a generic annotation
//			GenericAnnotations anns = tu.getSource().getAnnotation(GenericAnnotations.class);
//			if ( anns == null ) {
//				// If there is no annotation yet, creates one
//				anns = new GenericAnnotations();
//				tu.getSource().setAnnotation(anns);
//			}
//			GenericAnnotation ann = anns.add(GenericAnnotationType.TERM);
//			ann.setString(GenericAnnotationType.TERM_INFO, value)

			terms = null; // Reset for next time
		}

		// Set the resname value (or null, which is fine)
		tu.setName(ci.idValue);
		
		// Set the information about preserving or not white-spaces
		if ( ci.preserveWS ) {
			tu.setPreserveWhitespaces(true);
		}
		else { // We also unwrap if we don't have to preserve
			tu.setPreserveWhitespaces(false);
			tu.getSource().unwrap(false, true);
		}
	}
	
	/**
	 * Initializes the frag global variable and its annotations.
	 */
	private void initFragment () {
		frag = new TextFragment();
		inlineLQIs = null;
	}

	/**
	 * Nullifies the frag global variables and its annotations.
	 */
	private void nullFragment () {
		frag = null;
		inlineLQIs = null;
	}

	/**
	 * Prepares the document for using target pointers.
	 * <p>Because of the way the skeleton is constructed and because target pointer can result in the target
	 * location being anywhere in the document, we need to perform a first pass to create the targetTable
	 * table. That table lists all the source nodes that have a target pointer and the corresponding target
	 * node with its status.
	 */
	private void prepareTargetPointers () {
		hasTargetPointer = false;
		try {
			// If there is no target pointers, just reset the flag
			if ( !trav.getTargetPointerRuleTriggered() ) {
				return;
			}
			// Else: gather the target locations
			trav.startTraversal();
	
			// Go through the document
			Node srcNode;
			while ( (srcNode = trav.nextNode()) != null ) {
				if ( srcNode.getNodeType() == Node.ELEMENT_NODE ) {
					// Use !backTracking() to get to the elements only once
					// and to include the empty elements (for attributes).
					if ( !trav.backTracking() ) {
						// Check the element
						if ( trav.getTranslate(null) ) {
							String pointer = trav.getTargetPointer(null);
							if ( pointer != null ) {
								if ( trav.getWithinText() != ITraversal.WITHINTEXT_NO ) {
									// Can't be within text and with a target pointer
									throw new OkapiBadFilterParametersException(
										"An element within text or nested cannot use the Target Pointer data category.");
								}
								setTargetPointerPair(trav.getXPath(), false, srcNode, pointer);
							}
						}
						// Check the attributes
						NamedNodeMap map = ((Element)srcNode).getAttributes();
						for ( int i=0; i<map.getLength(); i++ ) {
							Attr attr = (Attr)map.item(i);
							if ( trav.getTranslate(attr) ) {
								String pointer = trav.getTargetPointer(attr);
								if ( pointer != null ) {
									setTargetPointerPair(trav.getXPath(), true, (Node)attr, pointer);
								}
							}
						}
					}
				}
			}
		}
		finally {
			// Reset the traversal
			trav.startTraversal();
		}
	}

	private void setTargetPointerPair (XPath xpath,
		boolean isAttributeNode,
		Node srcNode,
		String pointer)
	{
		try {
			// Get the target node for the given XPath expression
			XPathExpression expr = xpath.compile(pointer);
			Node trgNode;
			if ( isAttributeNode ) {
				trgNode = (Node)expr.evaluate(((Attr)srcNode).getOwnerElement(), XPathConstants.NODE);
			}
			else {
				trgNode = (Node)expr.evaluate(srcNode, XPathConstants.NODE);
			}
			if ( trgNode == null ) {
				// No entry available: Not supported yet
				throw new OkapiIOException(
					"the Target Pointer feature does not yet support pairs with non-existing target node.");
			}
			// Check source and target nodes are compatible
			if ( srcNode.getNodeType() != trgNode.getNodeType() ) {
				logger.warn("Potential issue with target pointer '{}'.\nThe source and target node are of different types. "
					+ "Depending on the content of the source, this may or may not be an issue.", pointer);
			}

			// Create the entry
			TargetPointerEntry tpe = new TargetPointerEntry(srcNode, trgNode);
			// Set the flags on each nod
			srcNode.setUserData(TargetPointerEntry.SRC_TRGPTRFLAGNAME, tpe, null);
			trgNode.setUserData(TargetPointerEntry.TRG_TRGPTRFLAGNAME, tpe, null);
			hasTargetPointer = true;

			// Is the source translatable?
			boolean translate;
			if ( isAttributeNode ) translate = trav.getTranslate((Attr)srcNode);
			else translate = trav.getTranslate(null);
			tpe.setTranslate(translate);
			if ( !translate ) return; // Nothing more to do
			
			// Check if there is an existing target content
			// (Note: we allow target to be of a different node type)
			if ( trgNode.getNodeType() == Node.ELEMENT_NODE ) {
				boolean hasExistingTargetContent = trgNode.hasChildNodes();
				tpe.setHasExistingTargetContent(hasExistingTargetContent);
				if ( !hasExistingTargetContent ) {
					if ( isAttributeNode ) {
						trgNode.setTextContent(srcNode.getTextContent());
					}
					else {
						Node tmp = srcNode.getFirstChild();
						while ( tmp != null ) {
							trgNode.appendChild(tmp.cloneNode(true));
							//TODO: What about user data??? (ITS info to process the content
							tmp = tmp.getNextSibling();
						}
					}
				}
				// else: existing target content is dealt with elsewhere by added references in the right places
			}
			else { // Attribute
				String text = trgNode.getTextContent();
				tpe.setHasExistingTargetContent(!text.isEmpty());
				if ( text.isEmpty() ) {
					// This is where we can lose content if the source is an element and the target an attribute
					trgNode.setTextContent(srcNode.getTextContent());
				}
			}
		}
		catch ( XPathExpressionException e ) {
			throw new OkapiIOException(String.format("Bad XPath expression in target pointer '%s'.", pointer));
		}
	}

	/**
	 * Gets the target pointer entry for a given node.
	 * @param node the node to examine.
	 * @return the target pointer entry for that node, or null if there is none.
	 */
	public TargetPointerEntry getTargetPointerEntry (Node node) {
		TargetPointerEntry tpe = (TargetPointerEntry)node.getUserData(TargetPointerEntry.TRG_TRGPTRFLAGNAME);
		if ( tpe != null ) {
			// This node is a target location
			//TODO
		}
		else {
			tpe = (TargetPointerEntry)node.getUserData(TargetPointerEntry.SRC_TRGPTRFLAGNAME);
			if ( tpe != null ) {
				// This node is a source with a target location
				// TODO
			}
		}
		return tpe;
	}

	private boolean isHTML5VoidElement (String name) {
		return (",area,base,br,col,command,embed,hr,img,input,keygen,link,meta,param,source,track,wbr".indexOf(","+name.toLowerCase()) != -1);
	}
}
