/*===========================================================================
  Copyright (C) 2009-2013 by the Okapi Framework contributors
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

package net.sf.okapi.lib.translation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.okapi.common.HTMLCharacterEntities;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.filterwriter.XLIFFContent;
import net.sf.okapi.common.query.QueryResult;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Collection of helper method for preparing and querying translation resources.
 */
public class QueryUtil {

	private static final Pattern HTML_OPENCLOSE = Pattern.compile(
			"(\\<u(\\s+)id=['\"](.*?)['\"]>)|(\\</u\\>)", Pattern.CASE_INSENSITIVE);
	// Try both <br .../> and <br ...> as some engine return an HTML-type <br> rather than an XML-type one (<br/>) 
	private static final Pattern HTML_ISOLATED = Pattern.compile(
			"\\<br(\\s+)id=['\"](.*?)['\"](\\s*?)/?>", Pattern.CASE_INSENSITIVE);
	// Check also <span> as some engines add their own in the results
	private static final Pattern HTML_SPAN = Pattern.compile("\\<span\\s(.*?)>|\\</span>",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

	private static final Pattern NCR = Pattern.compile("&#(\\S+?);");
	private static final Pattern CER = Pattern.compile("(&\\w*?;)");

	private StringBuilder codesMarkers;
	private List<Code> codes;
	private XLIFFContent fmt;
	private HTMLCharacterEntities entities;

	public QueryUtil () {
		codesMarkers = new StringBuilder();
		fmt = new XLIFFContent();
	}

	/**
	 * Indicates if the last text fragment passed to {@link #separateCodesFromText(TextFragment)} has codes or not.
	 * 
	 * @return true if the fragment has one or more code, false if it does not.
	 */
	public boolean hasCode () {
		if ( codes == null ) return false;
		return (codes.size() > 0);
	}

	/**
	 * Separate and store codes of a given text fragment.
	 * 
	 * @param frag
	 *            the fragment to process. Use {@link #createNewFragmentWithCodes(String)} to 
	 *            reconstruct the text back with its codes at the end.
	 * @return the fragment content stripped of its codes.
	 */
	public String separateCodesFromText (TextFragment frag) {
		// Reset
		codesMarkers.setLength(0);
		codes = frag.getCodes();
		// Get coded text
		String text = frag.getCodedText();
		if (!frag.hasCode()) {
			return text; // No codes
		}
		// If there are codes: store them apart
		StringBuilder tmp = new StringBuilder();
		for (int i = 0; i < text.length(); i++) {
			switch (text.charAt(i)) {
			case TextFragment.MARKER_OPENING:
			case TextFragment.MARKER_CLOSING:
			case TextFragment.MARKER_ISOLATED:
				codesMarkers.append(text.charAt(i));
				codesMarkers.append(text.charAt(++i));
				break;
			default:
				tmp.append(text.charAt(i));
			}
		}
		// Return text without codes
		return tmp.toString();
	}

	/**
	 * Appends the codes stored apart using {@link #separateCodesFromText(TextFragment)} at the end of a given plain
	 * text. The text fragment provided must be the same and without code modifications, as the one used for the
	 * splitting.
	 * 
	 * @param plainText
	 *            new text to use (must be plain)
	 * @return the provided fragment, but with the new text and the original codes appended at the end.
	 */
	public TextFragment createNewFragmentWithCodes (String plainText) {
		return new TextFragment(plainText + codesMarkers, codes);
	}

    /**
     * Converts from coded texts to coded HTML.
     * The resulting strings are also valid XML.
     * @param frags the fragments to convert.
     * @return The resulting HTML string.
     */
    public List<String> toCodedHTML(List<TextFragment> frags) {
        List<String> html = new ArrayList<>();
        for (TextFragment frag : frags) {
            html.add(toCodedHTML(frag));
        }
        return html;
    }

	/**
	 * Converts from coded text to coded HTML.
	 * The resulting string is also valid XML.
	 * @param fragment
	 *            the fragment to convert.
	 * @return The resulting HTML string.
	 */
	public String toCodedHTML (TextFragment fragment) {
		if ( fragment == null ) {
			return "";
		}
		Code code;
		StringBuilder sb = new StringBuilder();
		String text = fragment.getCodedText();
		for ( int i = 0; i < text.length(); i++ ) {
			switch (text.charAt(i)) {
			case TextFragment.MARKER_OPENING:
				code = fragment.getCode(text.charAt(++i));
				sb.append(String.format("<u id='%d'>", code.getId()));
				break;
			case TextFragment.MARKER_CLOSING:
				i++;
				sb.append("</u>");
				break;
			case TextFragment.MARKER_ISOLATED:
				code = fragment.getCode(text.charAt(++i));
				switch ( code.getTagType() ) {
				case OPENING:
					sb.append(String.format("<br id='b%d'/>", code.getId()));
					break;
				case CLOSING:
					sb.append(String.format("<br id='e%d'/>", code.getId()));
					break;
				case PLACEHOLDER:
					sb.append(String.format("<br id='p%d'/>", code.getId()));
					break;
				}
				break;
			case '&':
				sb.append("&amp;");
				break;
			case '<':
				sb.append("&lt;");
				break;
			default:
				sb.append(text.charAt(i));
			}
		}
		return sb.toString();
	}

	/**
	 * Converts an HTML string created with {@link #toCodedHTML(TextFragment)} back into a text fragment,
	 * but with empty inline codes.
	 * @param text the HTML string to convert.
	 * @param fragment an optional text fragment where to place the converted content. Any existing codes will be
	 * replaced or removed by the codes coming from the HTML string. Use null to create a new fragment.
	 * @return the modified or created text fragment.
	 */
	public TextFragment fromCodedHTMLToFragment (String text,
		TextFragment fragment)
	{
		if ( Util.isEmpty(text) ) {
			if ( fragment != null ) {
				fragment.setCodedText("", true);
				return fragment;
			}
			else {
				return new TextFragment();
			}
		}
		
		text = text.toString().replace("&apos;", "'");
		text = text.replace("&lt;", "<");
		text = text.replace("&gt;", ">");
		text = text.replace("&quot;", "\"");
		StringBuilder sb = new StringBuilder();
		sb.append(text.replace("&amp;", "&"));
		if ( entities == null ) {
			entities = new HTMLCharacterEntities();
			entities.ensureInitialization(false);
		}

		// Un-escape character entity references
		Matcher m;
		while ( true ) {
			m = CER.matcher(sb.toString());
			if ( !m.find() ) break;
			int val = entities.lookupReference(m.group(0));
			if ( val != -1 ) {
				sb.replace(m.start(0), m.end(0), String.valueOf((char) val));
			}
			else { // Unknown entity
				// TODO: replace by something meaningful to allow continuing the replacements
				break; // Temporary, to avoid infinite loop
			}
		}

		// Un-escape numeric character references
		m = NCR.matcher(sb.toString());
		while ( m.find() ) {
			String val = m.group(1);
			int n = (int) '?'; // Default
			try {
				if (val.charAt(0) == 'x') { // Hexadecimal
					n = Integer.valueOf(m.group(1).substring(1), 16);
				}
				else { // Decimal
					n = Integer.valueOf(m.group(1));
				}
			}
			catch ( NumberFormatException e ) {
				// Just use default
			}
			sb.replace(m.start(0), m.end(0), String.valueOf((char) n));
			m = NCR.matcher(sb.toString());
		}

		ArrayList<Code> codes = new ArrayList<Code>();
		Code code;
		int id;
		
		// Opening/closing markers
		// This assume no-overlapping tags and no empty elements
		m = HTML_OPENCLOSE.matcher(sb.toString());
		Stack<Integer> stack = new Stack<Integer>();
		String markers;
		while ( m.find() ) {
			if ( m.group(1) != null ) {
				// It's an opening tag
				id = Util.strToInt(m.group(3), -1);
				code = new Code(TagType.OPENING, "Xpt", null);
				code.setId(id);
				codes.add(code);
				markers = String.format("%c%c", TextFragment.MARKER_OPENING,
					TextFragment.toChar(codes.size()-1));
				sb.replace(m.start(), m.end(), markers);
				stack.push(id);
			}
			else {
				// It's a closing tag
				if ( stack.isEmpty() ) {
					// If the stack is empty it means the string is not well-formed, or a start tag is missing.
//todo: log warning					
					markers = "";
				}
				else {
					code = new Code(TagType.CLOSING, "Xpt", null);
					// ID should be resolved automatically
					codes.add(code);
					markers = String.format("%c%c", TextFragment.MARKER_CLOSING,
						TextFragment.toChar(codes.size()-1));
				}
				sb.replace(m.start(), m.end(), markers);
			}
			// Next open or close
			m = HTML_OPENCLOSE.matcher(sb.toString());
		}

		m = HTML_ISOLATED.matcher(sb.toString());
		while ( m.find() ) {
			// Replace the HTML fake code by the coded text markers
			// Create the code based on the prefix of the id (b, e, or p)
			switch ( m.group(2).charAt(0) ) {
				case 'b':
					code = new Code(TagType.PLACEHOLDER, "Xph", null);
					break;
				case 'e':
					code = new Code(TagType.PLACEHOLDER, "Xph", null);
					break;
				case 'p':
					code = new Code(TagType.PLACEHOLDER, "Xph", null);
					break;
				default: // Error
					//TODO: Log error instead and better message
					throw new OkapiException("ID of isolated code modified.");
			}
			// Don't include first character in ID value
			id = Util.strToInt(m.group(2).substring(1), -1);
			code.setId(id);
			codes.add(code);
			markers = String.format("%c%c", TextFragment.MARKER_ISOLATED,
				TextFragment.toChar(codes.size()-1));
			sb.replace(m.start(), m.end(), markers);
			// Next isolated
			m = HTML_ISOLATED.matcher(sb.toString());
		}

		// Remove any span elements that may have been added
		// (some MT engines mark up their output with extra info)
		m = HTML_SPAN.matcher(sb.toString());
		while ( m.find() ) {
			sb.replace(m.start(), m.end(), "");
			m = HTML_SPAN.matcher(sb.toString());
		}

		// Create the fragment or update the existing one
		if ( fragment != null ) {
			fragment.setCodedText(sb.toString(), codes, true);
			return fragment;
		}
		else {
			return new TextFragment(sb.toString(), codes);
		}
	}

	/**
	 * Converts back a coded HTML to a coded text.
	 * (extra span elements are removed).
	 * 
	 * @param htmlText the coded HTML to convert back.
	 * @param fragment the original text fragment.
	 * @param addMissingCodes true to added codes that are in the original fragment but not in the HTML string.
	 * @return the coded text with its code markers.
	 */
	public String fromCodedHTML (String htmlText,
		TextFragment fragment,
		boolean addMissingCodes)
	{
		return fromCodedHTML(htmlText, fragment, addMissingCodes, true);
	}
	
	/**
	 * Converts back a coded HTML to a coded text.
	 * 
	 * @param htmlText the coded HTML to convert back.
	 * @param fragment the original text fragment.
	 * @param addMissingCodes true to added codes that are in the original fragment but not in the HTML string.
	 * @param removeSpans true to remove extra span HTML codes.
	 * @return the coded text with its code markers.
	 */
	public String fromCodedHTML (String htmlText,
		TextFragment fragment,
		boolean addMissingCodes,
		boolean removeSpans)
	{
		if ( Util.isEmpty(htmlText) ) {
			return "";
		}
		htmlText = htmlText.toString().replace("&apos;", "'");
		htmlText = htmlText.replace("&lt;", "<");
		htmlText = htmlText.replace("&gt;", ">");
		htmlText = htmlText.replace("&quot;", "\"");
		StringBuilder sb = new StringBuilder(htmlText.replace("&amp;", "&"));
		if ( entities == null ) {
			entities = new HTMLCharacterEntities();
			entities.ensureInitialization(false);
		}

		// Create a lists to verify the codes
		ArrayList<String> newCodes = new ArrayList<String>();
		ArrayList<String> oriCodes = new ArrayList<String>();
		for ( Code code : fragment.getCodes() ) {
			switch ( code.getTagType() ) {
			case OPENING:
				oriCodes.add(String.format("o%d", code.getId()));
				break;
			case CLOSING:
				oriCodes.add(String.format("c%d", code.getId()));
				break;
			case PLACEHOLDER:
				switch ( code.getTagType() ) {
				case OPENING:
					oriCodes.add(String.format("b%d", code.getId()));
					break;
				case CLOSING:
					oriCodes.add(String.format("e%d", code.getId()));
					break;
				case PLACEHOLDER:
					oriCodes.add(String.format("p%d", code.getId()));
					break;
				}
				break;
			}
		}

		// Un-escape character entity references
		Matcher m;
		while ( true ) {
			m = CER.matcher(sb.toString());
			if ( !m.find() ) break;
			int val = entities.lookupReference(m.group(0));
			if ( val != -1 ) {
				sb.replace(m.start(0), m.end(0), String.valueOf((char) val));
			}
			else { // Unknown entity
				// TODO: replace by something meaningful to allow continuing the replacements
				break; // Temporary, to avoid infinite loop
			}
		}

		// Un-escape numeric character references
		m = NCR.matcher(sb.toString());
		while ( m.find() ) {
			String val = m.group(1);
			int n = (int) '?'; // Default
			try {
				if (val.charAt(0) == 'x') { // Hexadecimal
					n = Integer.valueOf(m.group(1).substring(1), 16);
				}
				else { // Decimal
					n = Integer.valueOf(m.group(1));
				}
			}
			catch ( NumberFormatException e ) {
				// Just use default
			}
			sb.replace(m.start(0), m.end(0), String.valueOf((char) n));
			m = NCR.matcher(sb.toString());
		}

		// Opening/closing markers
		// This assume no-overlapping tags and no empty elements
		m = HTML_OPENCLOSE.matcher(sb.toString());
		Stack<Integer> stack = new Stack<Integer>();
		String markers;
		while ( m.find() ) {
			if ( m.group(1) != null ) {
				// It's an opening tag
				int id = Util.strToInt(m.group(3), -1);
				markers = String.format("%c%c", TextFragment.MARKER_OPENING,
					TextFragment.toChar(fragment.getIndex(id)));
				sb.replace(m.start(), m.end(), markers);
				stack.push(id);
				newCodes.add(String.format("o%d", id));
			}
			else {
				// It's a closing tag
				if ( stack.isEmpty() ) {
					// If the stack is empty it means the string is not well-formed, or a start tag is missing.
					// The two codes will be added automatically at the end of the entry
					markers = "";
				}
				else {
					newCodes.add(String.format("c%d", stack.peek()));
					markers = String.format("%c%c", TextFragment.MARKER_CLOSING,
						TextFragment.toChar(fragment.getIndexForClosing(stack.pop())));
				}
				sb.replace(m.start(), m.end(), markers);
			}
			m = HTML_OPENCLOSE.matcher(sb.toString());
		}

		m = HTML_ISOLATED.matcher(sb.toString());
		while ( m.find() ) {
			// Replace the HTML fake code by the coded text markers
			char isoType = m.group(2).charAt(0);
			int id = Util.strToInt(m.group(2).substring(1), -1);
			markers = String.format("%c%c", TextFragment.MARKER_ISOLATED,
				TextFragment.toChar(fragment.getIndex(id)));
			sb.replace(m.start(), m.end(), markers);
			m = HTML_ISOLATED.matcher(sb.toString());
			newCodes.add(String.format("%c%d", isoType, id));
		}

		// Remove any span elements that may have been added
		// (some MT engines mark up their output with extra info)
		if ( removeSpans ) {
			m = HTML_SPAN.matcher(sb.toString());
			while ( m.find() ) {
				sb.replace(m.start(), m.end(), "");
				m = HTML_SPAN.matcher(sb.toString());
			}
		}

		// Try to correct missing codes
		if ( addMissingCodes && ( newCodes.size() < oriCodes.size() )) {
			for ( String tmp : oriCodes ) {
				if ( !newCodes.contains(tmp) ) {
					switch ( tmp.charAt(0) ) {
					case 'o':
						sb.append(String.format("%c%c", TextFragment.MARKER_OPENING,
							TextFragment.toChar(fragment.getIndex(Integer.parseInt(tmp.substring(1))))));
						break;
					case 'c':
						sb.append(String.format("%c%c", TextFragment.MARKER_CLOSING,
							TextFragment.toChar(fragment.getIndexForClosing(Integer.parseInt(tmp.substring(1))))));
						break;
					case 'b':
					case 'e':
					case 'p':
						sb.append(String.format("%c%c", TextFragment.MARKER_ISOLATED,
							TextFragment.toChar(fragment.getIndex(Integer.parseInt(tmp.substring(1))))));
						break;
					}
				}
			}
		}

		return sb.toString();
	}

	/**
	 * Converts an HTML plain text into a plain text one.
	 * <p>This method assumes there is no elements in the text, just escaped characters.
	 * The methods supported NCRs as well as HTML CERs.
	 * @param text the HTML string to convert.
	 * @return the un-escaped text.
	 */
	public String fromPlainTextHTML (String text) {
		if ( Util.isEmpty(text) ) {
			return "";
		}
		text = text.toString().replace("&apos;", "'");
		text = text.replace("&lt;", "<");
		text = text.replace("&gt;", ">");
		text = text.replace("&quot;", "\"");
		StringBuilder sb = new StringBuilder();
		sb.append(text.replace("&amp;", "&"));
		if ( entities == null ) {
			entities = new HTMLCharacterEntities();
			entities.ensureInitialization(false);
		}

		// Un-escape character entity references
		Matcher m;
		while ( true ) {
			m = CER.matcher(sb.toString());
			if ( !m.find() ) break;
			int val = entities.lookupReference(m.group(0));
			if ( val != -1 ) {
				sb.replace(m.start(0), m.end(0), String.valueOf((char) val));
			}
			else { // Unknown entity
				// TODO: replace by something meaningful to allow continuing the replacements
				break; // Temporary, to avoid infinite loop
			}
		}

		// Un-escape numeric character references
		m = NCR.matcher(sb.toString());
		while ( m.find() ) {
			String val = m.group(1);
			int n = (int) '?'; // Default
			try {
				if ( val.charAt(0) == 'x' ) { // Hexadecimal
					n = Integer.valueOf(m.group(1).substring(1), 16);
				}
				else { // Decimal
					n = Integer.valueOf(m.group(1));
				}
			}
			catch (NumberFormatException e) {
				// Just use default
			}
			sb.replace(m.start(0), m.end(0), String.valueOf((char) n));
			m = NCR.matcher(sb.toString());
		}

		return sb.toString();
	}

	/**
	 * Converts from coded text to XLIFF.
	 * 
	 * @param fragment
	 *            the fragment to convert.
	 * @return The resulting XLIFF string.
	 * @see #fromXLIFF(Element, TextFragment)
	 */
	public String toXLIFF (TextFragment fragment) {
		if ( fragment == null ) {
			return "";
		}
		fmt.setContent(fragment);
		return fmt.toString();
	}

	// /**
	// * Converts back an XLIFF text to a coded text.
	// * @param text the XLIFF text to convert back.
	// * @param fragment the original text fragment.
	// * @return the coded text with its code markers.
	// */
	// public String fromXLIFF (String text,
	// TextFragment fragment)
	// {
	// if ( Util.isEmpty(text) ) return "";
	// // Un-escape first layer
	// text = text.replace("&apos;", "'");
	// text = text.replace("&lt;", "<");
	// text = text.replace("&gt;", ">");
	// text = text.replace("&quot;", "\"");
	// text = text.replace("&amp;", "&");
	// // Now we have XLIFF valid content
	//
	// // Read it to XML parser
	// // Un-escape XML
	//
	// //TODO: code conversion
	// return text;
	// }

	/**
	 * Converts back an XLIFF text contained in a given element into a TextFragment.
	 * 
	 * @param elem
	 *            The element containing the XLIFF data.
	 * @param original
	 *            the original TextFragment (cannot be null).
	 * @see #toXLIFF(TextFragment)
	 * @return the newly created text fragment.
	 */
	public TextFragment fromXLIFF (Element elem,
		TextFragment original)
	{
		NodeList list = elem.getChildNodes();
		int lastId = -1;
		int id = -1;
		Node node;
		Stack<Integer> stack = new Stack<Integer>();
		StringBuilder buffer = new StringBuilder();

		// Note that this parsing assumes non-overlapping codes.
		for ( int i = 0; i < list.getLength(); i++ ) {
			node = list.item(i);
			switch ( node.getNodeType() ) {
			case Node.TEXT_NODE:
				buffer.append(node.getNodeValue());
				break;
			case Node.ELEMENT_NODE:
				NamedNodeMap map = node.getAttributes();
				if ( node.getNodeName().equals("bpt") ) {
					id = getRawIndex(lastId, map.getNamedItem("id"));
					stack.push(id);
					buffer.append(String.format("%c%c", TextFragment.MARKER_OPENING,
						TextFragment.toChar(original.getIndex(id))));
				}
				else if ( node.getNodeName().equals("ept") ) {
					buffer.append(String.format("%c%c", TextFragment.MARKER_CLOSING,
						TextFragment.toChar(original.getIndexForClosing(stack.pop()))));
				}
				else if ( node.getNodeName().equals("ph") ) {
					id = getRawIndex(lastId, map.getNamedItem("id"));
					buffer.append(String.format("%c%c", TextFragment.MARKER_ISOLATED,
						TextFragment.toChar(original.getIndex(id))));
				}
				else if ( node.getNodeName().equals("it") ) {
					Node pos = map.getNamedItem("pos");
					if ( pos == null ) { // Error, but just treat it as a placeholder
						id = getRawIndex(lastId, map.getNamedItem("id"));
						buffer.append(String.format("%c%c", TextFragment.MARKER_ISOLATED,
							TextFragment.toChar(original.getIndex(id))));
					}
					else if ( pos.getNodeValue().equals("begin") ) {
						id = getRawIndex(lastId, map.getNamedItem("id"));
						buffer.append(String.format("%c%c", TextFragment.MARKER_OPENING,
							TextFragment.toChar(original.getIndex(id))));
					}
					else { // Assumes 'end'
						id = getRawIndex(lastId, map.getNamedItem("id"));
						buffer.append(String.format("%c%c", TextFragment.MARKER_CLOSING,
							TextFragment.toChar(original.getIndexForClosing(id))));
					}
				}
				break;
			}
		}

		return new TextFragment(buffer.toString(), original.getCodes());
	}

	/**
	 * Removes duplicates based on the Equals method of {@link QueryResult}. 
	 * Preserve the highest ranked query results.
	 * For example, duplicate results with the newest creation date are always retained.<br>
	 * <b>WARNING:</b> order is not preserved!
	 * 
	 * @param queryResults
	 *            the list of QueryResults to process.
	 * @return a new list of the QueryResults without duplicates.
	 */
	public static ArrayList<QueryResult> removeDuplicates (List<QueryResult> queryResults) {
		// Add QueryResults to linked hash in ranked order
		// to make sure we keep the highest ranked duplicates
		LinkedHashSet<QueryResult> dupRemove = new LinkedHashSet<QueryResult>(queryResults.size());
		Collections.sort(queryResults);
		for (QueryResult qr : queryResults) {
			dupRemove.add(qr);
		}
		return new ArrayList<QueryResult>(dupRemove);
	}

	private int getRawIndex (int lastIndex,
		Node attr)
	{
		if ( attr == null ) {
			return ++lastIndex;
		}
		return Integer.valueOf(attr.getNodeValue());
	}

}
