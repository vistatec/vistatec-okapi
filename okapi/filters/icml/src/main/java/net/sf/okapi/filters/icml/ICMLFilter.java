/*===========================================================================
  Copyright (C) 2015 by the Okapi Framework contributors
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

package net.sf.okapi.filters.icml;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.FileUtil;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.IdGenerator;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.MimeTypeMapper;
import net.sf.okapi.common.StreamUtil;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.exceptions.OkapiBadFilterInputException;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.filters.FilterConfiguration;
import net.sf.okapi.common.filters.FilterUtil;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filters.IFilterConfigurationMapper;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.CodeSimplifier;
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.StartGroup;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.common.resource.TextUnit;
import net.sf.okapi.common.skeleton.ISkeletonWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@UsingParameters(Parameters.class)
public class ICMLFilter implements IFilter {

	private static String ATTRIBUTENAME_MARKUPTAG = "MarkupTag";
	private static String ELEMENTNAME_XMLELEMENT = "XMLElement";
	private static String ATTRIBUTEVALUE_XMLTAG = "XMLTag";
	private static String ATTRIBUTEVALUE_KEYFIGURE_PREFIX = "kf";
	private static String ELEMENTNAME_STORY = "Story";
	private static String PREFIX_CTYPE_TAG = "x-tag_";
	private static String PREFIX_CTYPE_PI = "x-pi_";
	private static String INDESIGN_PI_SPECIALCHAR = "ACE";
	private static String SPECHIALCHAR_INDENT_HERE ="7";
	private static String SPECHIALCHAR_RIGHT_INDENT_TAB = "8";
	private static String SPECHIALCHAR_END_NESTED_STYLE = "3";
	private static String SPECHIALCHAR_SECTION_MARKER = "19";
	private static String SPECHIALCHAR_AUTO_PAGENUMBER ="18";
	
	private final static String DOCID = "sd";
	private final static String ENDID = "end";
	private final static String SPREADTYPE = "spread";
	private final static String STORYTYPE = "story";
	private final static String EMBEDDEDSTORIES = "embedded-stories";
	private final DocumentBuilder docBuilder;

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private URI docURI;
	private LinkedList<Event> queue;
	private LocaleId srcLoc;
	private Parameters params;
	private EncoderManager encoderManager;
	private HashMap<String, Document> stories;
	private LinkedHashMap<String, ArrayList<String>> spreads;
	private ArrayList<String> storiesDone;
	private Iterator<String> storyIter;
	private Iterator<String> spreadIter;
	private IdGenerator spreadIdGen;
	private IdGenerator storyIdGen;
	private int spreadStack;
	private String tuIdPrefix;
	private Stack<ICMLContext> ctx;
	private HashMap<String, Boolean> embeddedElements;
	private HashMap<String, Integer> embeddedElementsPos;
	private IdGenerator refGen;
	private IdGenerator tuIdGen;
	private int deconstructing;
	private RawDocument input;
	private Document doc;
	private File tempFile;
	private CodeSimplifier simplifier = new CodeSimplifier();

	public ICMLFilter () {
		try {
			params = new Parameters();
			DocumentBuilderFactory docFact = DocumentBuilderFactory.newInstance();
			docFact.setValidating(false);
			
			// security concern. Turn off DTD and external entity processing
			// https://www.owasp.org/index.php/XML_External_Entity_%28XXE%29_Processing
			try {
				// Xerces 1 - http://xerces.apache.org/xerces-j/features.html#external-general-entities
				// Xerces 2 - http://xerces.apache.org/xerces2-j/features.html#external-general-entities
				docFact.setFeature("http://xml.org/sax/features/external-general-entities", false);
				 
				// Xerces 2 only - http://xerces.apache.org/xerces-j/features.html#external-general-entities
				docFact.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
				 
				} catch (ParserConfigurationException e) {
					// Tried an unsupported feature. This may indicate that a different XML processor is being
					// used. If so, then its features need to be researched and applied correctly.
					// For example, using the Xerces 2 feature above on a Xerces 1 processor will throw this
					// exception.
					logger.warn("Unsupported DocumentBuilderFactory feature. Possible security vulnerabilities.", e);
				}
			
			docBuilder = docFact.newDocumentBuilder();
			
			embeddedElements = new HashMap<String, Boolean>();

			// Create position holder for each
			embeddedElementsPos = new HashMap<String, Integer>();
			for ( String name : embeddedElements.keySet() ) {
				embeddedElementsPos.put(name, -1);
			}
			simplifier.setPostSegmentation(false);
		}
		catch ( Throwable e ) {
			throw new OkapiIOException("Error initializing.\n"+e.getMessage(), e);
		}
	}

	@Override
	public void cancel () {
		// TODO
	}

	@Override
	public void close () {
		if (input != null) {
            input.close();
		}
    }

	@Override
	public ISkeletonWriter createSkeletonWriter () {
		return null; // There is no corresponding skeleton writer
	}
	
	@Override
	public IFilterWriter createFilterWriter () {
		return new ICMLFilterWriter();
	}

	@Override
	public EncoderManager getEncoderManager () {
		if ( encoderManager == null ) {
			encoderManager = new EncoderManager();
			encoderManager.setMapping(MimeTypeMapper.ICML_MIME_TYPE, "net.sf.okapi.common.encoder.XMLEncoder");
		}
		return encoderManager;
	}

	@Override
	public String getName () {
		return "okf_icml";
	}

	@Override
	public String getDisplayName () {
		return "ICML Filter";
	}

	@Override
	public String getMimeType () {
		return MimeTypeMapper.ICML_MIME_TYPE;
	}

	@Override
	public List<FilterConfiguration> getConfigurations () {
		List<FilterConfiguration> list = new ArrayList<FilterConfiguration>();
		list.add(new FilterConfiguration(getName(),
			MimeTypeMapper.ICML_MIME_TYPE,
			getClass().getName(),
			"ICML",
			"Adobe InDesign ICML documents",
			null,
			".wcml;.icml"));
		return list;
	}
	
	@Override
	public IParameters getParameters () {
		return params;
	}

	@Override
	public boolean hasNext () {
		return ( queue != null );
	}

	@Override
	public Event next () {
		if ( queue == null ) return null;
		if ( queue.size() > 0 ) {
			return queue.poll();
		}

		// Get the next event
		read();
		// End process if needed
		if ( queue.size() == 0 ) {
			queue = null; // No more
			Ending ending = new Ending("ed");
			return new Event(EventType.END_DOCUMENT, ending);
		}
		// Else, return the next event that was read
		return queue.poll();
	}

	@Override
	public void open (RawDocument input) {
		open(input, true);
	}
	
	@Override
	public void open (RawDocument input,
		boolean generateSkeleton)
	{
		queue = null;
		close();
		// Initializes the variables
        this.input = input;
		docURI = input.getInputURI();
		
		if ( docURI == null ) {
			if (input.getStream() != null) {
				// Create a temp file for the stream content
				tempFile = FileUtil.createTempFile("~okapi-ICMLFilter_");
		    	StreamUtil.copy(input.getStream(), tempFile);
		    	docURI = Util.toURI(tempFile.getAbsolutePath());
			}
			else {
				throw new OkapiBadFilterInputException("Input stream is null.");
			}
		}
		
		srcLoc = input.getSourceLocale();
		spreadIdGen = new IdGenerator(null, "spr");
		storyIdGen = new IdGenerator(null, "sto");
		
		// Gather the spreads
		gatherStories();

		// Add the start document event
		StartDocument sd = new StartDocument(DOCID);
		sd.setEncoding("UTF-8", false);
		sd.setName(docURI.getPath());
		sd.setLocale(srcLoc);
		sd.setMimeType(MimeTypeMapper.ICML_MIME_TYPE);
		sd.setLineBreak("\n");
		sd.setFilterParameters(params);
		sd.setFilterWriter(createFilterWriter());
		
		// Add the skeleton
		sd.setSkeleton(new ICMLSkeleton(doc));
		// Create the start document event
		queue = new LinkedList<Event>();
		queue.add(new Event(EventType.START_DOCUMENT, sd));
		
		// load simplifier rules and send as an event
		if (!Util.isEmpty(((Parameters)getParameters()).getSimplifierRules())) {			
			Event cs = FilterUtil.createCodeSimplifierEvent(((Parameters)getParameters()).getSimplifierRules());
			simplifier.setRules(((Parameters)getParameters()).getSimplifierRules());
			queue.add(cs);
		}	
		
		// Point to the first spread
		if ( spreads.size() > 0 ) {
			spreadIter = spreads.keySet().iterator();
			if ( spreadIter.hasNext() ) {
				String spreadName = spreadIter.next();
				storyIter = spreads.get(spreadName).iterator();
				
				StartGroup sg = new StartGroup(DOCID, spreadIdGen.createId());
				queue.add(new Event(EventType.START_GROUP, sg));
				sg.setName(spreadName);
				if ( spreadName.equals(EMBEDDEDSTORIES) ) {
					sg.setId(EMBEDDEDSTORIES);
				}
				else {
					sg.setType(SPREADTYPE);
				}
				spreadStack++;
			}
		}
	}
	
	@Override
	public void setFilterConfigurationMapper (IFilterConfigurationMapper fcMapper) {
		// TODO (if needed)
	}

	@Override
	public void setParameters (IParameters params) {
		this.params = (Parameters)params;
	}

	private void read () {
		if ( spreadIter == null ) return; // No content
		while ( true ) {
			// Check for next story in the current spread
			if ( storyIter.hasNext() ) {
				// At least one more story to process
				processStory(storyIter.next());
				return;
			}
			
			// Otherwise: close the previous spread if needed
			if ( spreadStack > 0 ) {
				Ending ending = new Ending(spreadIdGen.getLastId()+ENDID);
				queue.add(new Event(EventType.END_GROUP, ending));
				spreadStack--;
			}
			
			// Then try the next spread
			if ( spreadIter.hasNext() ) {
				String spreadName = spreadIter.next();
				storyIter = spreads.get(spreadName).iterator();
				StartGroup sg = new StartGroup(DOCID, spreadIdGen.createId());
				sg.setName(spreadName);
				if ( spreadName.equals(EMBEDDEDSTORIES) ) {
					sg.setId(EMBEDDEDSTORIES);
				}
				else {
					sg.setType(SPREADTYPE);
				}
				queue.add(new Event(EventType.START_GROUP, sg));
				spreadStack++;
			}	
			else {
				// Else: nothing else
				break;
			}
		}
	}
	
	/**
	 * Gathers all the stories to process, for each spread.
	 */
	private void gatherStories () {
		spreadIter = null;
		storyIter = null;
		spreads = new LinkedHashMap<String, ArrayList<String>>();
		storiesDone = new ArrayList<String>();
		stories = new HashMap<String, Document>();
		
		try 
		{
			InputSource is = new InputSource(input.getStream());
			doc = docBuilder.parse(is);
		
			gatherStoriesInSpread();
			gatherStoriesInStory();
		}
		catch ( Throwable e ) {
			throw new OkapiIOException("Error while gathering stories.\n"+e.getMessage(), e);
		}
	}
	
	/**
	 * Gather all the stories used in this spread.
	 * @return the total number of stories in the given spread.
	 */
	private int gatherStoriesInSpread ()
		throws SAXException, IOException, ParserConfigurationException
	{
		ArrayList<String> storyList = new ArrayList<String>();
		NodeList list = doc.getElementsByTagName("Story");
		for ( int i=0; i<list.getLength(); i++ ) {
			Element tf = (Element)list.item(i);
			String selfId = tf.getAttribute("Self");
			if ( Util.isEmpty(selfId) ) {
				throw new IOException("Missing value for Self.");
			}
			else
			{
				if(!IsEmbeddedStory(selfId))
				{
					// Add the the story to the lookup list
					if ( !storiesDone.contains(selfId) ) {
						storyList.add(selfId);
						storiesDone.add(selfId);
					}
				}
				
				stories.put(selfId, doc);
			}
		}

		// Add the stories for this spread to the overall list of stories to process
		spreads.put("Stories", storyList);
		// Return the number of stories in this spread
		return storyList.size();
	}
	
	/**
	 * is story an embedded story
	 */
	private boolean IsEmbeddedStory(String storyId)
			throws IOException
	{
		boolean isEmbedded = false;
		NodeList references = doc.getElementsByTagName("TextFrame");
		for ( int i=0; i<references.getLength(); i++ ) {
			Element tf = (Element)references.item(i);
			String parentStory = tf.getAttribute("ParentStory");
			if ( Util.isEmpty(parentStory) ) {
				throw new IOException("Missing value for parentStory.");
			}
			
			if(storyId.equals(parentStory))
			{
				isEmbedded = true;
				break;
			}
		}
		
		return isEmbedded;
	}
	
	/**
	 * Gather all the stories used in this story.
	 */
	private void gatherStoriesInStory ()
		throws SAXException, IOException, ParserConfigurationException
	{
		ArrayList<String> storyList = new ArrayList<String>();
		
		NodeList list = doc.getElementsByTagName("TextFrame");
		for ( int i=0; i<list.getLength(); i++ ) {
			Element tf = (Element)list.item(i);
			String tmp = tf.getAttribute("ParentStory");
			if ( Util.isEmpty(tmp) ) {
				throw new IOException("Missing value for parentStory.");
			}
			// Add the the story to the lookup list
			if ( !storiesDone.contains(tmp) ) {
				storyList.add(tmp);
				storiesDone.add(tmp);
			}
		}
		
		// If needed, add the stories for this story to the overall list of stories to process
		if ( !storyList.isEmpty() ) {
			ArrayList<String> existingList = spreads.get(EMBEDDEDSTORIES);
			if ( existingList == null ) {
				spreads.put(EMBEDDEDSTORIES, storyList);
			}
			else {
				existingList.addAll(storyList);
				spreads.put(EMBEDDEDSTORIES, existingList);
			}
		}
	}

	private void processStory (String storyId) {
		Document entry = stories.get(storyId);
		if ( entry == null ) {
			throw new OkapiIOException("No story entry found for "+storyId);
		}
		try {
			// Read the document in memory
			Document doc = entry;
			
			// Start the story group
			StartGroup sg = new StartGroup(spreadIdGen.getLastId(), storyIdGen.createId());
			sg.setName(storyId);
			sg.setType(STORYTYPE);
			sg.setSkeleton(new ICMLSkeleton(entry, doc));
			queue.add(new Event(EventType.START_GROUP, sg));
			
			// Prepare for traversal
			tuIdPrefix = storyId+"-";
			ctx = new Stack<ICMLContext>();
			refGen = new IdGenerator(null);
			tuIdGen = new IdGenerator(null);
			
			Node topNode = getStory(storyId);
			
			ctx.push(new ICMLContext(false, topNode));
			deconstructing = 0;

			// Reset the embedded elements position
			for ( String name : embeddedElementsPos.keySet() ) {
				embeddedElementsPos.put(name, -1);
			}
			
			// Traverse the story
			processNodes(topNode);
			
			// End the story group
			Ending ending = new Ending(storyIdGen.getLastId()+ENDID);
			queue.add(new Event(EventType.END_GROUP, ending));
		}
		catch ( Throwable e ) {
			throw new OkapiIOException(String.format("Error processing story file '%s'.\n"+e.getMessage(), storyId), e);
		}
	}
	
	private Node getStory(String storyId)
	{
		Node topNode = null;
		NodeList stories = doc.getElementsByTagName("Story");
		for ( int i=0; i<stories.getLength(); i++ ) {
			Element tf = (Element)stories.item(i);
			
			String tmp = tf.getAttribute("Self");
			if ( Util.isEmpty(tmp) ) {
				try {
					throw new IOException("Missing value for Story.");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(tmp.equals(storyId))
			{
				topNode = stories.item(i);
				break;
			}
		}
		return topNode;
	}
	
	private void processNodes (Node node) {
		while ( node != null) {
			
			if ( node.getNodeType() != Node.ELEMENT_NODE ) {
				if ( ctx.peek().inScope() ) {
					// Add to current entry if needed
					switch ( node.getNodeType() ) {
					case Node.TEXT_NODE:
					case Node.CDATA_SECTION_NODE:
						ctx.peek().addCode(node);
						break;
					default:
						throw new OkapiIOException("Unexpected node type: "+node.getNodeType());
					}
				}
				
				node = GetNextSiblingToProcessNodes(node);
				continue;
			}
			
			// Else: it's an element
			Element elem = (Element)node;
			String name = elem.getNodeName();
			
			
			// Process before the children
			if ( name.equals("Content") ) {
				ctx.peek().addContent(elem);
				node = GetNextSiblingToProcessNodes(elem);
				continue;
			}
			else if ( name.equals("ParagraphStyleRange") ) {
				// Process the start, and continue or move on depending on the return
				if(HasParentPSR(elem) == false)
				{
					if (doStartPSR(elem) ) {
						node = GetNextSiblingToProcessNodes(elem);
						continue;
					}
				}
				else if ( ctx.peek().inScope() ) 
				{
					if ( deconstructing == 0 ) deconstructing++;
					ctx.peek().addStartTag(elem);
					// Trigger a text unit, then re-set the context to continue in a new fragment
					Node tmpNode = ctx.peek().getScopeNode();
					triggerTextUnit(elem, false);
					ctx.peek().enterScope(tmpNode, makeTuId());
				}
			}
			else if ( embeddedElements.containsKey(name) ) {
				// Update the count for that element
				embeddedElementsPos.put(name, embeddedElementsPos.get(name) + 1);
				// Process the element
				if ( ctx.peek().inScope() ) {
					if ( embeddedElements.get(name) ) {
						// Create the inline code that holds the reference
						String key = refGen.createId();
						ctx.peek().addCode(new Code(TagType.PLACEHOLDER, name,
							String.format("<%s id=\"%s\"/>", ICMLSkeleton.NODEREMARKER, key)));
						ctx.peek().addReference(key, makeNodeReference(node));
						// Create the new context
						ctx.push(new ICMLContext(true, node));
					}
					else { // Do not extract: Just use a node reference
						String key = refGen.createId();
						ctx.peek().addCode(new Code(TagType.PLACEHOLDER, name,
							String.format("<%s id=\"%s\"/>", ICMLSkeleton.NODEREMARKER, key)));
						ctx.peek().addReference(key, makeNodeReference(node));
						// Moves to the next sibling
						node = GetNextSiblingToProcessNodes(elem);
						continue;
					}
				}
				else { // Not in scope
					// Move to the next sibling
					node = GetNextSiblingToProcessNodes(elem);
					continue;
				}
			}
			// IDML format specification show both <Br/> and <br/> used in example
			else if ( name.equalsIgnoreCase("Br") && params.getNewTuOnBr() ) {
				if ( ctx.peek().inScope() ) {
					if ( deconstructing == 0 ) deconstructing++;
					ctx.peek().addStartTag(elem);
					// Trigger a text unit, then re-set the context to continue in a new fragment
					Node tmpNode = ctx.peek().getScopeNode();
					triggerTextUnit(elem, false);
					ctx.peek().enterScope(tmpNode, makeTuId());
					node = GetNextSiblingToProcessNodes(elem);
					continue;
				}
				// Else: do nothing, like the default case
			}
			else {
				if ( ctx.peek().inScope() ) {
					if(isKeyFigure(elem))
					{
						String attribute = elem.getAttribute(ATTRIBUTENAME_MARKUPTAG);
						attribute = attribute.substring(attribute.indexOf("/") +1).toUpperCase();
						
						Code c = new Code(TagType.OPENING, PREFIX_CTYPE_TAG +attribute, buildStartTag(elem));
						c.setAnnotation("protected", null);
						ctx.peek().addCode(c);
					}
					else
						ctx.peek().addStartTag(elem);
					
				}
			}
		
			// Process the children (if any)
			if ( elem.hasChildNodes() ) {
				processNodes(elem.getFirstChild());
			}
			
			// When coming back from the children
			if ( name.equals("ParagraphStyleRange") ) {
				if(HasParentPSR(elem) == false)
				{
					// Trigger the text unit
					triggerTextUnit(elem, true);
					if ( deconstructing > 0 ) deconstructing--;
				}
				else
				{
					if ( ctx.peek().inScope() ) {
						ctx.peek().addEndTag(elem);
					}
				}
			}
			else if ( embeddedElements.containsKey(name) ) {
				if ( ctx.peek().inScope() ) {
					//TODO
				}
				ctx.pop();
			}
			else {
				if ( ctx.peek().inScope() ) {
					if(isKeyFigure(elem))
					{
						String attribute = elem.getAttribute(ATTRIBUTENAME_MARKUPTAG);
						attribute = attribute.substring(attribute.indexOf("/") +1).toUpperCase();
						
						Code c = new Code(TagType.CLOSING, PREFIX_CTYPE_TAG +attribute, buildEndTag(elem));
						c.setAnnotation("protected", null);
						ctx.peek().addCode(c);
					}
					else
						ctx.peek().addEndTag(elem);
					
				}
			}
			
			// Then move on to the next sibling
			node = GetNextSiblingToProcessNodes(elem);
		}
	}
	
	public String buildStartTag (Element elem) {
		StringBuilder sb = new StringBuilder("<"+elem.getNodeName());
		NamedNodeMap attrNames = elem.getAttributes();
		for ( int i=0; i<attrNames.getLength(); i++ ) {
			Attr attr = (Attr)attrNames.item(i);
			sb.append(" " + attr.getName() + "=\"");
			sb.append(Util.escapeToXML(attr.getValue(), 3, false, null));
			sb.append("\"");
		}
		// Make it an empty element if possible
		if ( elem.hasChildNodes() ) {
			sb.append(">");
		}
		else {
			sb.append("/>");
		}
		return sb.toString();
	}
	
	public String buildEndTag (Element elem) {
		if ( elem.hasChildNodes() ) {
			return "</"+elem.getNodeName()+">";
		}
		return ""; // If there are no children, the element was closed in buildStartTag()
	}
	
	/**
	 * Check if element is a key figure
	 * @param elem The Element to check for
	 * @return true if element is a key figure
	 */
	private Boolean isKeyFigure(Element elem)
	{
		Node parent = elem.getParentNode();
		String name = elem.getNodeName();
		String attribute = elem.getAttribute(ATTRIBUTENAME_MARKUPTAG);
		
		return (name == ELEMENTNAME_XMLELEMENT && 
				attribute != null && 
				attribute.startsWith(ATTRIBUTEVALUE_XMLTAG +"/" +ATTRIBUTEVALUE_KEYFIGURE_PREFIX +"_") && 
				parent != null && 
				parent.getNodeName() != ELEMENTNAME_STORY);
	}

	private void triggerTextUnit (Node node, boolean isEndTag) {
		// Trigger the text unit
		if ( ctx.peek().addToQueue(queue, deconstructing>0) && params.getSimplifyCodes() ) {
			// Try to simplify the inline codes if possible
			// We can access the text this way because it's not segmented yet
			ITextUnit tu = queue.getLast().getTextUnit();
			TextFragment tf = tu.getSource().getFirstContent();
			TextFragment[] res = simplifier.simplifyAll(tf, true);
			ICMLSkeleton skel = (ICMLSkeleton)tu.getSkeleton();
			// Move the native data into the skeleton if needed
			if ( res != null ) {
				// Check if the new fragment is empty
				if ( tu.getSource().isEmpty() && ( deconstructing == 0 )) {
					// Remove from queue
					queue.removeLast();
				}
				else {
					skel.addMovedParts(res);
				}
			}
			
			boolean hasParent = HasParentPSR(node);
			if(hasParent == false && isEndTag)
				hasParent = false;
			else
				hasParent = true; // True for TU triggered by Br for example
			
			skel.setForced(hasParent); 
		}
		ctx.peek().leaveScope();
	}
	
	private NodeReference makeNodeReference (Node targetNode) {
		String name = targetNode.getNodeName();
		return new NodeReference(name, embeddedElementsPos.get(name));
	}
	
	private String makeTuId () {
		return tuIdPrefix+tuIdGen.createId();
	}

	/**
	 * Processes the start of a ParagraphStyleRange
	 * @param node the node of the current element.
	 * @return true if the element has been dealt with, and the caller method should continue the loop with the next sibling,
	 * false if the caller need to just continue down. 
	 */
	private boolean doStartPSR (Node node) {
		NodeList list = ((Element)node).getElementsByTagName("Content");
		NodeList ranges = ((Element)node).getElementsByTagName("ParagraphStyleRange");
		if ( ranges.getLength() > 0) {
			// Several content: no shortcut
			ctx.peek().enterScope(node, makeTuId());
			if ( deconstructing > 0  ) deconstructing++; // Push new paragraph in deconstructed block
			return false;
		}
		if ( ranges.getLength() == 0) {
			
			if ( list.getLength() > 1) {
				// Several content: no shortcut
				ctx.peek().enterScope(node, makeTuId());
				if ( deconstructing > 0  ) deconstructing++; // Push new paragraph in deconstructed block
				return false;
			}
			if ( list.getLength() == 1) {
				// We have a single Content element
				Element cnt = (Element)list.item(0);
				// Create the text unit
				ITextUnit tu = new TextUnit(makeTuId());
				tu.setSourceContent(processContent(cnt, null));
				tu.setPreserveWhitespaces(true);
				if ( deconstructing > 0 ) deconstructing++; // Push new paragraph in deconstructed block
				ICMLSkeleton skl = new ICMLSkeleton(ctx.peek().getTopNode(), cnt);
				
				boolean forced = HasParentPSR(node);
				skl.setForced(forced);
				tu.setSkeleton(skl); // Merge directly on Content
				// And add the new event to the queue
				queue.add(new Event(EventType.TEXT_UNIT, tu));
			}
		}
		// Else: we have no content
		// In both case: move on to the next node
		return true;
	}
	
	/**
	 * Check if current node has parent ParagraphStyleRange node
	 * @param node the current node
	 * @return true if current node has a parent ParagraphStyleRange node
	 * false if has no parent ParagraphStyleRange node
	 */
	private boolean HasParentPSR(Node node)
	{
		boolean result = false;
		
		Node parent = node.getParentNode();
		do 
		{
			if(parent != null)
			{
				if(parent.getNodeName().equals("ParagraphStyleRange"))
				{
					result = true;
				}
				
				parent = parent.getParentNode();
			}
			
		} while (parent != null);
		
		return result;
	}
	
	/**
	 * Get next sibling
	 * @return the next sibling if node name is not Story
	 */
	private Node GetNextSiblingToProcessNodes(Node node)
	{
		Node nextSibling = node.getNextSibling();
		if(nextSibling != null && nextSibling.getNodeName().equals("Story"))
		{
			nextSibling = null;
		}
		
		return nextSibling;
	}

	/**
	 * Processes the content of a Content element.
	 * @param content the Content node.
	 * @param tf the text fragment where to put the content. Use null to create one.
	 * @return the modified text fragment (may be a new one).
	 */
	static TextFragment processContent (Element content,
		TextFragment tf)
	{
		if ( tf == null ) tf = new TextFragment();
		// We assume only TEXT and PI nodes, no inner elements!
		Node node = content.getFirstChild();
		while ( node != null ) {
			switch ( node.getNodeType() ) {
			case Node.TEXT_NODE:
				processText(tf, node.getNodeValue());
				break;
			case Node.PROCESSING_INSTRUCTION_NODE:
				ProcessingInstruction pi = (ProcessingInstruction)node;
				tf.append(TagType.PLACEHOLDER, GetProcessingInstructionType(pi), String.format("<?%s %s?>", pi.getTarget(), pi.getTextContent()));
				break;
			default:
				throw new OkapiIOException("Unexpected content in <Content>: "+node.getNodeType());
			}
			node = node.getNextSibling();
		}
		return tf;
	}
	
	/**
	 * Get the processing instruction type
	 * @param pi the processing instruction
	 * @return character type
	 */
	static String GetProcessingInstructionType(ProcessingInstruction pi)
	{
		String target = pi.getTarget();
		String textContent = pi.getTextContent();
		
		if(target.equals(INDESIGN_PI_SPECIALCHAR))
		{
			if(textContent.equals(SPECHIALCHAR_INDENT_HERE))
			{
				return PREFIX_CTYPE_PI +"INDENTHERETAB";
			}
			else if(textContent.equals(SPECHIALCHAR_RIGHT_INDENT_TAB))
			{
				return PREFIX_CTYPE_PI +"RIGHTINDENTTAB";
			}
			else if(textContent.equals(SPECHIALCHAR_END_NESTED_STYLE))
			{
				return PREFIX_CTYPE_PI +"ENDNESTEDSTYLE";
			}
			else if(textContent.equals(SPECHIALCHAR_SECTION_MARKER))
			{
				return PREFIX_CTYPE_PI +"SECTIONMARKER";
			}
			else if(textContent.equals(SPECHIALCHAR_AUTO_PAGENUMBER))
			{
				return PREFIX_CTYPE_PI +"AUTOPAGENUMBER";
			}
		}
		
		return "pi";
	}

	static void processText (TextFragment dest,
		String text)
	{
		for ( int i=0; i<text.length(); i++ ) {
			char ch = text.charAt(i);
			switch ( ch ) {
			//case '\u00a0': // Nonbreaking space
				//dest.append(TagType.PLACEHOLDER, "x-space_NONBREAKINGSPACE", String.valueOf(ch));
				//break;
			//case '\u2028': // Forced line-break
				//dest.append(TagType.PLACEHOLDER, "x-break_FORCEDLINEBREAK", String.valueOf(ch));
				//break;
			case '\u200b': // Discretionary line-break
				dest.append(TagType.PLACEHOLDER, "x-break_DISCRETIONARYLINEBREAK", String.valueOf(ch));
				break;
			//case '\u2011': // Non-breaking hyphen
				//dest.append(TagType.PLACEHOLDER, "x-hyphen_NONBREAKINGHYPHEN", String.valueOf(ch));
				//break;
			case '\u2012': // Figure dash
				dest.append(TagType.PLACEHOLDER, "x-dash_FIGUREDASH", String.valueOf(ch));
				break;
			//case '\u202f': // Fixed-width non-breaking space
				//dest.append(TagType.PLACEHOLDER, "x-space_FIXEDWIDTHNONBREAKINGSPACE", String.valueOf(ch));
				//break;
			case '\u200a': // Hair space
				dest.append(TagType.PLACEHOLDER, "x-space_HAIRSPACE", String.valueOf(ch));
				break;
			//case '\u2013': // En dash
				//dest.append(TagType.PLACEHOLDER, "x-dash_ENDASH", String.valueOf(ch));
				//break;
			//case '\u2014': // Em dash
				//dest.append(TagType.PLACEHOLDER, "x-dash_EMDASH", String.valueOf(ch));
				//break;
			case '\u2015': // Horizontal bar
				dest.append(TagType.PLACEHOLDER, "x-dash_HORIZONTALBAR", String.valueOf(ch));
				break;
			case '\u2006': // Sixth space
				dest.append(TagType.PLACEHOLDER, "x-space_SIXTHSPACE", String.valueOf(ch));
				break;
			case '\u2005': // Quarter space
				dest.append(TagType.PLACEHOLDER, "x-space_QUARTERSPACE", String.valueOf(ch));
				break;
			case '\u2004': // Third space
				dest.append(TagType.PLACEHOLDER, "x-space_THIRDSPACE", String.valueOf(ch));
				break;
			//case '\u2003': // Em space
				//dest.append(TagType.PLACEHOLDER, "x-space_EMSPACE", String.valueOf(ch));
				//break;
			//case '\u2002': // En space
				//dest.append(TagType.PLACEHOLDER, "x-space_ENSPACE", String.valueOf(ch));
				//break;
			case '\u2008': // Punctuation space
				dest.append(TagType.PLACEHOLDER, "x-space_PUNCTUATIONSPACE", String.valueOf(ch));
				break;
			case '\u2009': // Thin space
				dest.append(TagType.PLACEHOLDER, "x-space_THINSPACE", String.valueOf(ch));
				break;
			//case '\u2010': // Hyphen
				//dest.append(TagType.PLACEHOLDER, "x-dash_HYPHEN", String.valueOf(ch));
				//break;
			case '\u2007': // Figure space
				dest.append(TagType.PLACEHOLDER, "x-space_FIGURESPACE", String.valueOf(ch));
				break;
			case '\u2001': // Flush space
				dest.append(TagType.PLACEHOLDER, "x-space_FLUSHSPACE", String.valueOf(ch));
				break;
			case '\ufeff': // Text anchor (Not sure about this one, but for sure we don't want it in the text)
				dest.append(TagType.PLACEHOLDER, "x-anchor_TEXTANCHOR", String.valueOf(ch));
				break;
			default:
				dest.append(ch);
				break;
			}
			
		}
	}

}
