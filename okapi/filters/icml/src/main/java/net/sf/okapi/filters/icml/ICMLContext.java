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

import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.common.resource.TextUnit;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class ICMLContext {
	
	private static String ATTRIBUTENAME_MARKUPTAG = "MarkupTag";
	private static String ATTRIBUTEVALUE_XMLTAG = "XMLTag";
	private static String ATTRIBUTENAME_APPLIEDCHARACTERSTYLE ="AppliedCharacterStyle";
	private static String ATTRIBUTENAME_APPLIEDPARAGRAPHSTYLE ="AppliedParagraphStyle";
	private static String PREFIX_CTYPE_TAG = "x-tag_";
	private static String PREFIX_CTYPE_CHARACTERSTYLE = "x-cs_";
	private static String PREFIX_CTYPE_PARAGRAPHSTYLE = "x-ps_";
	
	private boolean inScope;
	private Node topNode;
	private Node scopeNode;
//	private Node contentNode;
	private TextFragment tf;
//	private int status;
	private boolean isReferent;
	private ICMLSkeleton skel;
	private String tuId;
	
	private final static boolean phOnly = false;
	
	/**
	 * Create a new context.
	 * @param rootNode Node when starting embedded context. Should be null for the top-level context.
	 */
	public ICMLContext (boolean isReferent,
		Node topNode)
	{
		this.isReferent = isReferent;
		this.topNode = topNode;
	}
	
	public void enterScope (Node scopeNode,
		String tuId)
	{
		this.scopeNode = scopeNode;
		this.tuId = tuId;
		tf = new TextFragment();
		//status = 0;
		inScope = true;
	}

	public Node getTopNode () {
		return topNode;
	}
	
	public Node getScopeNode () {
		return scopeNode;
	}

	public ICMLSkeleton getSkeleton () {
		return skel;
	}
	
	public void leaveScope () {
		inScope = false;
		skel = null;
	}
	
	public boolean inScope () {
		return inScope;
	}
	
	/**
	 * Adds the text unit to the given queue.
	 * @param queue the event queue where to add the event.
	 * @param evenIfEmpty true to create a TU event with empty content.
	 * @return true if a text unit with possibly inline codes was added, false otherwise.
	 */
	public boolean addToQueue (List<Event> queue,
		boolean evenIfEmpty)
	{
		if ( tf.isEmpty() && !evenIfEmpty ) return false; // Skip empty entries
		
//		if ( status == 1 ) {
//			// Only one content: no need for inline codes
//			// Reset the fragment to just the text
//			tf = new TextFragment(TextFragment.getText(tf.getCodedText()));
//			// Make the Content the top node
//			scopeNode = contentNode;
//		}
		
		// Create the text unit
		ITextUnit tu = new TextUnit(tuId, null, isReferent);
		tu.setSourceContent(tf);
		tu.setPreserveWhitespaces(true);
		if ( skel == null ) {
			skel = new ICMLSkeleton(topNode, scopeNode);
		}
		tu.setSkeleton(skel);
		// And add the new event to the queue
		queue.add(new Event(EventType.TEXT_UNIT, tu));
		// This object should not be called again
		
//		return (status != 1);
		return true;
	}
	
	/**
	 * Adds a Content element to the text unit.
	 * @param elem the Content element node.
	 */
	public void addContent (Element elem) {
		if ( phOnly ) {
			tf.append(TagType.PLACEHOLDER, "code", buildStartTag(elem));
			ICMLFilter.processContent(elem, tf);
			tf.append(TagType.PLACEHOLDER, "code", buildEndTag(elem));
		}
		else {
			tf.append(TagType.OPENING, "code", buildStartTag(elem));
			ICMLFilter.processContent(elem, tf);
			tf.append(TagType.CLOSING, "code", buildEndTag(elem));
		}
		//status++;
//		contentNode = elem;
	}
	
	public void addCode (Code code) {
		tf.append(code);
	}

	public void addCode (Node node) {
		if ( node.getNodeType() == Node.TEXT_NODE ) {
			String text = node.getNodeValue();
			for ( int i=0; i<text.length(); i++ ) {
				if ( !Character.isWhitespace(text.charAt(i)) ) {
					tf.append(TagType.PLACEHOLDER, "text", text);
					return;
				}
			}
			// Otherwise: just white spaces: no output
		}
		else if ( node.getNodeType() == Node.CDATA_SECTION_NODE ) {
			tf.append(TagType.PLACEHOLDER, "cdata", "<![CDATA["+node.getNodeValue()+"]]>");
		}
	}
	
	public void addReference (String key,
		NodeReference ref)
	{
		if ( skel == null ) {
			skel = new ICMLSkeleton(topNode, scopeNode);
		}
		// Clone the node, so it's not deleted when merging the surrounding content
		skel.addReferenceNode(key, ref);
	}
	
	public void addStartTag (Element elem) {
		if ( phOnly ) {
			tf.append(TagType.PLACEHOLDER,
				elem.getNodeName(), buildStartTag(elem));
		}
		else {
			tf.append(elem.hasChildNodes() ? TagType.OPENING : TagType.PLACEHOLDER, 
					getCtype(elem), buildStartTag(elem));
		}
	}
	
	public void addEndTag (Element elem) {
		if ( elem.hasChildNodes() ) {
			if ( phOnly ) {
				tf.append(TagType.PLACEHOLDER, elem.getNodeName(), buildEndTag(elem));
			}
			else {
				tf.append(TagType.CLOSING,
						getCtype(elem), buildEndTag(elem));
			}
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
	 * Get the character type for the code
	 * @param elem The Element to check for
	 * @return the character type, otherwise the element name
	 */
	private String getCtype(Element elem)
	{
		String elementName = elem.getNodeName();
		String cStyle = getCharacterStyle(elem);
		String tStyle = getXmlMarkUp(elem);
		String pStyle = getParagraphStyle(elem);
		
		if(pStyle != null && pStyle.length() > 1){
			elementName = PREFIX_CTYPE_PARAGRAPHSTYLE + pStyle;
		}
		else if(cStyle != null && cStyle.length() > 1){
			elementName = PREFIX_CTYPE_CHARACTERSTYLE + cStyle;
		}
		else if(tStyle != null && tStyle.length() > 1){
			elementName = PREFIX_CTYPE_TAG + tStyle;
		}
		
		return elementName;
	}
	
	/**
	 * Check if ParagraphStyle has been set
	 * @param elem The Element to check for
	 * @return true if element has paragraph style
	 */
	private String getParagraphStyle(Element elem)
	{
		String pStyle = elem.getAttribute(ATTRIBUTENAME_APPLIEDPARAGRAPHSTYLE);
		if(pStyle != null && pStyle.length() > 1)
		{
			pStyle = pStyle.substring(pStyle.lastIndexOf("/")  +1).replace(" ", "").replace("[","").replace("]", "").toUpperCase();
		}
		
		return pStyle;
	}
	
	/**
	 * Check if CharacterStyle has been set
	 * @param elem The Element to check for
	 * @return true if element has character style
	 */
	private String getCharacterStyle(Element elem)
	{
		String cStyle = elem.getAttribute(ATTRIBUTENAME_APPLIEDCHARACTERSTYLE);
		if(cStyle != null && cStyle.length() > 1)
		{
			cStyle = cStyle.substring(cStyle.lastIndexOf("/")  +1).replace(" ", "").replace("[","").replace("]", "").toUpperCase();
		}
		
		return cStyle;
	}
	
	/**
	 * Check if element has mark up tag attribute
	 * @param elem The Element to check for
	 * @return true if it is a mark up tag tag
	 */
	private String getXmlMarkUp(Element elem)
	{
		String tStyle = elem.getAttribute(ATTRIBUTENAME_MARKUPTAG);
		if(tStyle != null && tStyle.length() > 1)
		{
			if(tStyle.startsWith(ATTRIBUTEVALUE_XMLTAG +"/"))
				tStyle = tStyle.substring(tStyle.indexOf("/") +1).toUpperCase();
		}
		
		return tStyle;
	}
}
