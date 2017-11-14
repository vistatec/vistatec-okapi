/*===========================================================================
  Copyright (C) 2008-2010 by the Okapi Framework contributors
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

package net.sf.okapi.common.filterwriter;

import java.util.List;
import java.util.Stack;

import net.sf.okapi.common.Util;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.TextFragment;

/**
 * Handles the conversion between a coded text object and TMX.
 */
public class TMXContent {

	/**
	 * Indicates that the inline codes should contain the original code.
	 */
	public static final int CODEMODE_ORIGINAL = 0;

	/**
	 * Indicates that the inline codes should contain nothing.
	 */
	public static final int CODEMODE_EMPTY = 1;
	
	/**
	 * Indicates that the inline codes should contain generic codes (e.g. &lt;1&gt;,&lt;/1&gt;,&lt;2/&gt;)
	 */
	public static final int CODEMODE_GENERIC = 2;
	
	/**
	 * Indicates that the inline codes should contain letter-codes generic inline code (like OmegaT (e.g. &lt;g0&gt;,&lt;/g0&gt;,&lt;x1/&gt;)
	 */
	public static final int CODEMODE_LETTERCODED = 3;
	
	private String codedText;
	private List<Code> codes;
	private boolean withTradosWorkarounds = false;
	private int defaultQuoteMode = 1;
	private int codeMode = CODEMODE_ORIGINAL;
	private int letterCodeOffset = 0;

	/**
	 * Creates a new TMXContent object without any content.
	 */
	public TMXContent () {
		codedText = "";
	}
	
	/**
	 * Creates a new TMXContent object and set its content to the given fragment.
	 * @param content The TextFragment object to format.
	 */
	public TMXContent (TextFragment content) {
		setContent(content);
	}

	/**
	 * Sets the flag that indicates if the TMX generated should use workarounds so the
	 * output can be read in some versions of Trados Translators' Workbench that have
	 * bugs leading to the lose of data.
	 * @param value true to use workarounds, false to not use workarounds.
	 */
	public void setTradosWorkarounds (boolean value) {
		withTradosWorkarounds = value;
	}
	
	/**
	 * Sets the flag that indicates if the TMX generated should use letter-coded inline content.
	 * Note that for OmegaT to take the work-around
	 * into account, the attribute <code>creationtool</code> must be set to "OmegaT".
	 * @param value true to use workarounds, false to not use workarounds.
	 * @param zeroBased true to have 0-based code, false for unaltered IDs.
	 */
	public void setLetterCodedMode (boolean value,
		boolean zeroBased)
	{
		if (value) {
			codeMode = CODEMODE_LETTERCODED;
		}
		letterCodeOffset = zeroBased ? 1 : 0;  // Minus 1 if zero-based
	}
	
	/**
	 * Indicates if this formatter is set to output letter-coded content.
	 * @return true if the formatter is set for OmegaT.
	 */
    public boolean getLetterCodedMode () {
    	return (codeMode == CODEMODE_LETTERCODED);
    }
	
	/**
	 * Sets the default quote mode. This value is used when using {@link #toString()}
	 * instead of {@link #toString(int, boolean)}.
	 * @param quoteMode 0=no quote escaped, 1=apos and quot, 2=#39 and quot,
	 * and 3=quot only.
	 */
	public void setQuoteMode (int quoteMode) {
		defaultQuoteMode = quoteMode;
	}
	
	/**
	 * Sets the type of content the inline codes should be output.
	 * @param codeMode the code for the inline code mode: one of the <code>CODEMODE_...</code> codes.
	 */
	public void setCodeMode (int codeMode) {
		this.codeMode = codeMode;
	}
	
	/**
	 * Gets the flag for the code mode currently set.
	 * @return the code mode currently set: one of the <code>CODEMODE_...</code> codes.
	 */
	public int getCodeMode () {
		return codeMode;
	}
	
	/**
	 * Sets the fragment to format.
	 * @param content The TextFragment object to format.
	 * @return Itself
	 */
	public TMXContent setContent (TextFragment content) {
		codedText = content.getCodedText();
		codes = content.getCodes();
		return this;
	}
	
	/**
	 * Generates a TMX string from the content.
	 * This is the same as calling this.toString(quoteMode, true),
	 * where quoteMode is the value set by {@link #setQuoteMode(int)} or 1 by default.
	 * @return The string formatted in TMX.
	 */
	@Override
	public String toString () {
		return toString(defaultQuoteMode, true);
	}

	/**
	 * Generates a TMX string from the content.
	 * @param quoteMode 0=no quote escaped, 1=apos and quot, 2=#39 and quot,
	 * and 3=quot only.
	 * @param escapeGT True to always escape '&gt;' to gt.
	 * @return The string formatted in TMX.
	 */
	public String toString (int quoteMode,
		boolean escapeGT)
	{
		StringBuilder tmp = new StringBuilder();
		int index;
		int id;
		Code code;
		
		// Variables for OmegaT work-around
		Stack<Integer> otStack = new Stack<Integer>();
//2011-Sep-24: changed OT numbering to use id		int otId = 0; 

		for ( int i=0; i<codedText.length(); i++ ) {
			//TODO: output attribute 'type' whenever possible
			switch ( codedText.codePointAt(i) ) {
			case TextFragment.MARKER_OPENING:
				index = TextFragment.toIndex(codedText.charAt(++i));
				code = codes.get(index);
				if ( code.hasAnnotation("protected") ) {
					tmp.append("<hi type=\"protected\">");
				} else {
					if (Util.isEmpty(code.getType())) {
						tmp.append(String.format("<bpt i=\"%d\">", code.getId()));
					} else {
						tmp.append(String.format("<bpt i=\"%d\" type=\"%s\">", code.getId(), code.getType()));
					}
					
					switch ( codeMode ) {
					case CODEMODE_GENERIC:
						//otStack.push(otId++);
						otStack.push(code.getId());
						tmp.append(Util.escapeToXML(String.format("<%d>", otStack.peek()), quoteMode, escapeGT, null));
						break;
					case CODEMODE_LETTERCODED:
						//otStack.push(otId++);
						otStack.push(code.getId()-letterCodeOffset);
						tmp.append(Util.escapeToXML(String.format("<g%d>", otStack.peek()), quoteMode, escapeGT, null));
						break;
					case CODEMODE_EMPTY:
						// Nothing to output
						break;
					default:
						tmp.append(Util.escapeToXML(codes.get(index).toString(), quoteMode, escapeGT, null));
						break;
					}
					tmp.append("</bpt>");
				}
				break;
			case TextFragment.MARKER_CLOSING:
				index = TextFragment.toIndex(codedText.charAt(++i));
				code = codes.get(index);								
				tmp.append(String.format("<ept i=\"%d\">", code.getId()));				
				
				switch ( codeMode ) {
				case CODEMODE_GENERIC:
					tmp.append(Util.escapeToXML(String.format("</%d>", otStack.pop()), quoteMode, escapeGT, null));
					break;
				case CODEMODE_LETTERCODED:
					tmp.append(Util.escapeToXML(String.format("</g%d>", otStack.pop()), quoteMode, escapeGT, null));
					break;
				case CODEMODE_EMPTY:
					// Nothing to output
					break;
				default:
					tmp.append(Util.escapeToXML(codes.get(index).toString(), quoteMode, escapeGT, null));
					break;
				}
				tmp.append("</ept>");
				if ( code.hasAnnotation("protected") ) {
					tmp.append("</hi>");
				}
				break;
			case TextFragment.MARKER_ISOLATED:
				index = TextFragment.toIndex(codedText.charAt(++i));
				code = codes.get(index);
				id = code.getId();
				// Use <ph> or <it> depending on underlying tagType
				switch ( code.getTagType() ) {
				case PLACEHOLDER:
					if ( withTradosWorkarounds
						&& ((code.getData().indexOf('{') != -1 )
							|| (code.getData().indexOf('}') != -1 )
							|| (code.getData().indexOf('\\') != -1 )))
					{
						tmp.append("<ut>{\\cs6\\f1\\cf6\\lang1024 </ut>");
						tmp.append(Util.escapeToXML(code.toString(), quoteMode, escapeGT, null));
						tmp.append("<ut>}</ut>");
					}
					else {						
						if (Util.isEmpty(code.getType())) {
							tmp.append(String.format("<ph x=\"%d\">", id));
						} else {
							tmp.append(String.format("<ph x=\"%d\" type=\"%s\">", code.getId(), code.getType()));
						}
						
						switch ( codeMode ) {
						case CODEMODE_GENERIC:
							//tmp.append(Util.escapeToXML(String.format("<%d/>", otId++), quoteMode, escapeGT, null));
							tmp.append(Util.escapeToXML(String.format("<%d/>", id), quoteMode, escapeGT, null));
							break;
						case CODEMODE_LETTERCODED:
							//tmp.append(Util.escapeToXML(String.format("<x%d/>", otId++), quoteMode, escapeGT, null));
							tmp.append(Util.escapeToXML(String.format("<x%d/>", id-letterCodeOffset), quoteMode, escapeGT, null));
							break;
						case CODEMODE_EMPTY:
							// Nothing to output
							break;
						default:
							tmp.append(Util.escapeToXML(code.toString(), quoteMode, escapeGT, null));
							break;
						}
						tmp.append("</ph>");
					}
					break;
				case OPENING:
					if (Util.isEmpty(code.getType())) {
						tmp.append(String.format("<it x=\"%d\" pos=\"begin\">", code.getId()));
					} else {
						tmp.append(String.format("<it x=\"%d\" pos=\"begin\" type=\"%s\">", code.getId(), code.getType()));
					}
					
					switch ( codeMode ) {
					case CODEMODE_GENERIC:
						//tmp.append(Util.escapeToXML(String.format("<%d/>", otId++), quoteMode, escapeGT, null));
						tmp.append(Util.escapeToXML(String.format("<%d/>", id), quoteMode, escapeGT, null));
						break;
					case CODEMODE_LETTERCODED:
						//tmp.append(Util.escapeToXML(String.format("<x%d/>", otId++), quoteMode, escapeGT, null));
						tmp.append(Util.escapeToXML(String.format("<x%d/>", id-letterCodeOffset), quoteMode, escapeGT, null));
						break;
					case CODEMODE_EMPTY:
						// Nothing to output
						break;
					default:
						tmp.append(Util.escapeToXML(code.toString(), quoteMode, escapeGT, null));
						break;
					}
					tmp.append("</it>");
					break;
				case CLOSING:
					if (Util.isEmpty(code.getType())) {
						tmp.append(String.format("<it x=\"%d\" pos=\"end\">", code.getId()));
					} else {
						tmp.append(String.format("<it x=\"%d\" pos=\"end\" type=\"%s\">", code.getId(), code.getType()));
					}					switch ( codeMode ) {
					case CODEMODE_GENERIC:
						//tmp.append(Util.escapeToXML(String.format("<%d/>", otId++), quoteMode, escapeGT, null));
						tmp.append(Util.escapeToXML(String.format("<%d/>", id), quoteMode, escapeGT, null));
						break;
					case CODEMODE_LETTERCODED:
						//tmp.append(Util.escapeToXML(String.format("<x%d/>", otId++), quoteMode, escapeGT, null));
						tmp.append(Util.escapeToXML(String.format("<x%d/>", id-letterCodeOffset), quoteMode, escapeGT, null));
						break;
					case CODEMODE_EMPTY:
						// Nothing to output
						break;
					default:
						tmp.append(Util.escapeToXML(code.toString(), quoteMode, escapeGT, null));
						break;
					}
					tmp.append("</it>");
					break;
				}
				
				break;
			case '>':
				if ( escapeGT ) tmp.append("&gt;");
				else {
					if (( i > 0 ) && ( codedText.charAt(i-1) == ']' )) 
						tmp.append("&gt;");
					else
						tmp.append('>');
				}
				break;
			case '<':
				tmp.append("&lt;");
				break;
			case '\r': // Not a line-break in the XML context, but a literal
				tmp.append("&#13;");
				break;
			case '&':
				tmp.append("&amp;");
				break;
			case '"':
				if ( quoteMode > 0 ) tmp.append("&quot;");
				else tmp.append('"');
				break;
			case '\'':
				switch ( quoteMode ) {
				case 1:
					tmp.append("&apos;");
					break;
				case 2:
					tmp.append("&#39;");
					break;
				default:
					tmp.append(codedText.charAt(i));
					break;
				}
				break;
			default:
				tmp.append(codedText.charAt(i));
				break;
			}
		}
		return tmp.toString();
	}
}
