/*===========================================================================
  Copyright (C) 2008-2012 by the Okapi Framework contributors
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
============================================================================*/

package net.sf.okapi.common.filterwriter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.okapi.common.Range;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.InvalidContentException;
import net.sf.okapi.common.resource.InvalidPositionException;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.common.resource.TextPart;

/**
 * Handles the conversion between a coded text object and a generic markup string.
 */
public class GenericContent {

	private static final Pattern patternOpening = Pattern.compile("\\<(\\d+?)\\>");
	private static final Pattern patternClosing = Pattern.compile("\\</(\\d+?)\\>");
	private static final Pattern patternIsolated = Pattern.compile("\\<(\\d+?)/\\>");
	private static final Pattern patternIsolatedB = Pattern.compile("\\<b(\\d+?)/\\>");
	private static final Pattern patternIsolatedE = Pattern.compile("\\<e(\\d+?)/\\>");

	private static final Pattern patternLCOpening = Pattern.compile("\\<g(\\d+?)\\>");
	private static final Pattern patternLCClosing = Pattern.compile("\\</g(\\d+?)\\>");
	private static final Pattern patternLCIsolated = Pattern.compile("\\<x(\\d+?)/\\>");
	private static final Pattern patternLCIsolatedB = Pattern.compile("\\<b(\\d+?)/\\>");
	private static final Pattern patternLCIsolatedE = Pattern.compile("\\<e(\\d+?)/\\>");

	private static final Pattern patternLCOpenCloseEncode = Pattern.compile("\\<(/?)(g+?\\d+?)\\>");
	private static final String replacementLCOpenCloseEncode = "<$1g$2>";
	private static final Pattern patternLCOpenCloseDecode = Pattern.compile("\\<(/)?g(g+?\\d+?)\\>");
	private static final String replacementLCOpenCloseDecode = "<$1$2>";

	private static final Pattern patternLCAllIsolatedEncode = Pattern.compile("\\<(([xbe])\\2*?\\d+?)/\\>");
	private static final String replacementLCAllIsolatedEncode = "<$2$1/>";
	private static final Pattern patternLCAllIsolatedDecode = Pattern.compile("\\<([xbe])(\\1+?\\d+?)/\\>");
	private static final String replacementLCAllIsolatedDecode = "<$2/>";

	private String codedText;
	private List<Code> codes;

	public GenericContent () {
		codedText = "";
	}
	
	public GenericContent (TextFragment content) {
		setContent(content);
	}
	
	public GenericContent setContent (TextFragment content) {
		codedText = content.getCodedText();
		codes = content.getCodes();
		return this;
	}

	/**
	 * Prints a generic string representation of a given segmented text, with optional
	 * markers to indicate the segments boundaries.
	 * @param container The container to output.
	 * @param showSegments True if segment boundaries should be shown.
	 * @return A string with the segmented text output.
	 */
	public String printSegmentedContent (TextContainer container,
		boolean showSegments)
	{
		return printSegmentedContent(container, showSegments, false);
	}
	
	/**
	 * Prints a string representation of a given segmented text, with optional
	 * markers to indicate the segments boundaries.
	 * @param container the container to output.
	 * @param showSegments true if segment boundaries should be shown.
	 * @param normalText true to show in-line real data instead of generic codes.
	 * @return a string with the segmented text output.
	 */
	public String printSegmentedContent (TextContainer container,
		boolean showSegments,
		boolean normalText)
	{
		StringBuilder tmp = new StringBuilder();
		for ( TextPart part : container ) {
			if ( part instanceof Segment ) {
				if ( showSegments ) tmp.append("[");
				tmp.append(setContent(part.text).toString(normalText));
				if ( showSegments ) tmp.append("]");
			}
			else {
				tmp.append(setContent(part.text).toString(normalText));
			}
		}
		return tmp.toString();
	}
	
	/**
	 * Generates a coded string containing code indexes in place of two-char code markers.
	 * @return The coded string.
	 */
	public String printMarkerIndexes () {
		StringBuilder tmp = new StringBuilder();
		for ( int i=0; i<codedText.length(); i++ ) {
			switch ( codedText.charAt(i) ) {
			case TextFragment.MARKER_OPENING:
			case TextFragment.MARKER_CLOSING:
			case TextFragment.MARKER_ISOLATED:
				int index = TextFragment.toIndex(codedText.charAt(++i));
				tmp.append("{");
				tmp.append(index);
				tmp.append("}");
				break;
			default:
				tmp.append(codedText.charAt(i));
				break;
			}
		}
		return tmp.toString();
	}
	
	/**
	 * Generates an generic coded string from the content.
	 * @return The generic string.
	 */
	@Override
	public String toString () {
		return toString(false);
	}
	
	/**
	 * Generates a generic coded string or an normal output from the content.
	 * @param normalText True to show in-line real data instead of generic codes.
	 * @return The output string.
	 */
	public String toString (boolean normalText) {
		StringBuilder tmp = new StringBuilder();
		int index;
		for ( int i=0; i<codedText.length(); i++ ) {
			switch ( codedText.codePointAt(i) ) {
			case TextFragment.MARKER_OPENING:
				index = TextFragment.toIndex(codedText.charAt(++i));
				if ( normalText ) tmp.append(codes.get(index).toString());
				else tmp.append(String.format("<%d>", codes.get(index).getId()));
				break;
			case TextFragment.MARKER_CLOSING:
				index = TextFragment.toIndex(codedText.charAt(++i));
				if ( normalText ) tmp.append(codes.get(index).toString());
				else tmp.append(String.format("</%d>", codes.get(index).getId()));
				break;
			case TextFragment.MARKER_ISOLATED:
				index = TextFragment.toIndex(codedText.charAt(++i));
				if ( normalText ) tmp.append(codes.get(index).toString());
				else {
					if ( codes.get(index).getTagType() == TagType.OPENING ) {
						tmp.append(String.format("<b%d/>", codes.get(index).getId()));
					}
					else if ( codes.get(index).getTagType() == TagType.CLOSING ) {
						tmp.append(String.format("<e%d/>", codes.get(index).getId()));
					}
					else {
						tmp.append(String.format("<%d/>", codes.get(index).getId()));
					}
				}
				break;
			default:
				tmp.append(codedText.charAt(i));
				break;
			}
		}
		return tmp.toString();
	}

	/**
	 * Gets the matching position in the coded text string of a given 
	 * position in the generic text output.
	 * @param position Generic text position to convert to coded text position.
	 * @return Calculated coded text position.
	 */
	public Range getCodedTextPosition (Range position) {
		Range result = new Range(0, 0);
		int genericPos = 0;
		int codedPos = 0;
		int index;
		for ( int i=0; i<codedText.length(); i++ ) {
			switch ( codedText.codePointAt(i) ) {
			case TextFragment.MARKER_OPENING:
				index = TextFragment.toIndex(codedText.charAt(++i));
				genericPos += String.format("<%d>", codes.get(index).getId()).length();
				codedPos += 2;
				break;
			case TextFragment.MARKER_CLOSING:
				index = TextFragment.toIndex(codedText.charAt(++i));
				genericPos += String.format("</%d>", codes.get(index).getId()).length();
				codedPos += 2;
				break;
			case TextFragment.MARKER_ISOLATED:
				index = TextFragment.toIndex(codedText.charAt(++i));
				if ( codes.get(index).getTagType() == TagType.OPENING ) {
					genericPos += String.format("<b%d/>", codes.get(index).getId()).length();
				}
				else if ( codes.get(index).getTagType() == TagType.CLOSING ) {
					genericPos += String.format("<e%d/>", codes.get(index).getId()).length();
				}
				else {
					genericPos += String.format("<%d/>", codes.get(index).getId()).length();
				}
				codedPos += 2;
				break;
			default:
				genericPos++;
				codedPos++;
				break;
			}
			if ( genericPos == position.start ) {
				result.start = codedPos;
				if ( position.start == position.end ) {
					result.end = result.start;
					return result;
				}
			}
			if ( genericPos == position.end ) {
				result.end = codedPos;
				return result;
			}
		}
		// Else: out-of-bounds or within an in-line code
		throw new InvalidPositionException (
			String.format("Position %d or %d is invalid.", position.start, position.end));
	}
	
	/**
	 * Updates a text fragment from a generic representation.
	 * @param genericText The generic text to use to update the fragment.
	 * @param fragment The text fragment to update.
	 * @param allowCodeDeletion True when missing in-line codes in the generic text
	 * means the corresponding codes should be deleted from the fragment.
	 * @throws InvalidContentException When the generic text is not valid, or does
	 * not correspond to the existing codes.
	 */
	public static void updateFragment (String genericText,
		TextFragment fragment,
		boolean allowCodeDeletion)
	{
		if ( genericText == null )
			throw new NullPointerException("Parameter genericText is null");

		// Case with no in-line codes
		if ( !fragment.hasCode() && ( genericText.indexOf('<') == -1 )) {
			fragment.setCodedText(genericText);
			return;
		}
		
		// Otherwise: we have in-line codes
		StringBuilder tmp = new StringBuilder(genericText);
		
		int n;
		int start = 0;
		int diff = 0;
		int index;
		Matcher m = patternOpening.matcher(genericText);
		while ( m.find(start) ) {
			n = m.start();
			index = fragment.getIndex(Integer.valueOf(m.group(1)));
			if ( index == -1 )
				throw new InvalidContentException(String.format("Invalid code: '%s'", m.group()));
			tmp.replace(n+diff, (n+diff)+m.group().length(), String.format("%c%c",
				(char)TextFragment.MARKER_OPENING, TextFragment.toChar(index)));
			diff += (2-m.group().length());
			start = n+m.group().length();
		}
		start = diff = 0;
		m = patternClosing.matcher(tmp.toString());
		while ( m.find(start) ) {
			n = m.start();
			index = fragment.getIndexForClosing(Integer.valueOf(m.group(1)));
			if ( index == -1 )
				throw new InvalidContentException(String.format("Invalid code: '%s'", m.group()));
			fragment.getCode(index).setId(-1); // For re-balancing
			tmp.replace(n+diff, (n+diff)+m.group().length(), String.format("%c%c",
				(char)TextFragment.MARKER_CLOSING, TextFragment.toChar(index)));
			diff += (2-m.group().length());
			start = n+m.group().length();
		}
		start = diff = 0;
		m = patternIsolated.matcher(tmp.toString());
		while ( m.find(start) ) {
			n = m.start();
			index = fragment.getIndex(Integer.valueOf(m.group(1)));
			if ( index == -1 )
				throw new InvalidContentException(String.format("Invalid code: '%s'", m.group()));
			tmp.replace(n+diff, (n+diff)+m.group().length(), String.format("%c%c",
				(char)TextFragment.MARKER_ISOLATED, TextFragment.toChar(index)));
			diff += (2-m.group().length());
			start = n+m.group().length();
		}
		start = diff = 0;
		m = patternIsolatedB.matcher(tmp.toString());
		while ( m.find(start) ) {
			n = m.start();
			index = fragment.getIndex(Integer.valueOf(m.group(1)));
			if ( index == -1 )
				throw new InvalidContentException(String.format("Invalid code: '%s'", m.group()));
			tmp.replace(n+diff, (n+diff)+m.group().length(), String.format("%c%c",
				(char)TextFragment.MARKER_ISOLATED, TextFragment.toChar(index)));
			diff += (2-m.group().length());
			start = n+m.group().length();
		}
		start = diff = 0;
		m = patternIsolatedE.matcher(tmp.toString());
		while ( m.find(start) ) {
			n = m.start();
			index = fragment.getIndexForClosing(Integer.valueOf(m.group(1)));
			if ( index == -1 )
				throw new InvalidContentException(String.format("Invalid code: '%s'", m.group()));
			fragment.getCode(index).setId(-1); // For re-balancing
			tmp.replace(n+diff, (n+diff)+m.group().length(), String.format("%c%c",
				(char)TextFragment.MARKER_ISOLATED, TextFragment.toChar(index)));
			diff += (2-m.group().length());
			start = n+m.group().length();
		}
		
		// Allow deletion of codes
		fragment.setCodedText(tmp.toString(), allowCodeDeletion);
	}

//	/**
//	 * Creates a new fragment from a numeric-codes text and corresponding codes.
//	 * <p>A numeric-coded text is like "&lt;1>text&lt;2/>&lt;/1>&lt;b3/>".
//	 * @param genericText the text to convert.
//	 * @param codes the codes to use with the coded text.
//	 * @param allowCodeDeletion true to allow the deletion of some codes.
//	 * @return the new fragment created from the text.
//	 */
//	public TextFragment fromNumericCodedToFragment (String genericText,
//		List<Code> codes,
//		boolean allowCodeDeletion)
//	{
//		TextFragment tf = new TextFragment("", codes);
//		updateFragment(genericText, tf, false);
//		return tf;
//		
//		if ( genericText == null )
//			throw new NullPointerException("Parameter genericText cannot be null");
//
//		// Case with no in-line codes
//		if ( Util.isEmpty(codes) && ( genericText.indexOf('<') == -1 )) {
//			return new TextFragment(genericText);
//		}
//		
//		// Otherwise: we have in-line codes
//		StringBuilder tmp = new StringBuilder(genericText);
//		
//		int n;
//		int start = 0;
//		int diff = 0;
//		int index;
//		
//		Matcher m = patternOpening.matcher(genericText);
//		while ( m.find(start) ) {
//			n = m.start();
//			index = Code.getIndex(codes, false, Integer.valueOf(m.group(1)));
//			if ( index == -1 )
//				throw new InvalidContentException(String.format("Invalid code: '%s'", m.group()));
//			tmp.replace(n+diff, (n+diff)+m.group().length(), String.format("%c%c",
//				(char)TextFragment.MARKER_OPENING, TextFragment.toChar(index)));
//			diff += (2-m.group().length());
//			start = n+m.group().length();
//		}
//		start = diff = 0;
//		m = patternClosing.matcher(tmp.toString());
//		while ( m.find(start) ) {
//			n = m.start();
//			index = Code.getIndex(codes, true, Integer.valueOf(m.group(1)));
//			if ( index == -1 )
//				throw new InvalidContentException(String.format("Invalid code: '%s'", m.group()));
//			codes.get(index).setId(-1); // For re-balancing
//			tmp.replace(n+diff, (n+diff)+m.group().length(), String.format("%c%c",
//				(char)TextFragment.MARKER_CLOSING, TextFragment.toChar(index)));
//			diff += (2-m.group().length());
//			start = n+m.group().length();
//		}
//		start = diff = 0;
//		m = patternIsolated.matcher(tmp.toString());
//		while ( m.find(start) ) {
//			n = m.start();
//			index = Code.getIndex(codes, false, Integer.valueOf(m.group(1)));
//			if ( index == -1 )
//				throw new InvalidContentException(String.format("Invalid code: '%s'", m.group()));
//			tmp.replace(n+diff, (n+diff)+m.group().length(), String.format("%c%c",
//				(char)TextFragment.MARKER_ISOLATED, TextFragment.toChar(index)));
//			diff += (2-m.group().length());
//			start = n+m.group().length();
//		}
//		start = diff = 0;
//		m = patternIsolatedB.matcher(tmp.toString());
//		while ( m.find(start) ) {
//			n = m.start();
//			index = Code.getIndex(codes, false, Integer.valueOf(m.group(1)));
//			if ( index == -1 )
//				throw new InvalidContentException(String.format("Invalid code: '%s'", m.group()));
//			tmp.replace(n+diff, (n+diff)+m.group().length(), String.format("%c%c",
//				(char)TextFragment.MARKER_ISOLATED, TextFragment.toChar(index)));
//			diff += (2-m.group().length());
//			start = n+m.group().length();
//		}
//		start = diff = 0;
//		m = patternIsolatedE.matcher(tmp.toString());
//		while ( m.find(start) ) {
//			n = m.start();
//			index = Code.getIndex(codes, true, Integer.valueOf(m.group(1)));
//			if ( index == -1 )
//				throw new InvalidContentException(String.format("Invalid code: '%s'", m.group()));
//			codes.get(index).setId(-1); // For re-balancing
//			tmp.replace(n+diff, (n+diff)+m.group().length(), String.format("%c%c",
//				(char)TextFragment.MARKER_ISOLATED, TextFragment.toChar(index)));
//			diff += (2-m.group().length());
//			start = n+m.group().length();
//		}
//		
//		
//		TextFragment tf = new TextFragment();
//		tf.setCodedText(tmp.toString(), codes, allowCodeDeletion);
//		return tf;
//	}

	/**
	 * Converts a letter-coded text to a fragment.
	 * <p>A letter-coded text is like "&lt;g1&gt;text&lt;x2/&gt;&lt;/g1&gt;&lt;b3/&gt;".
	 * @param text the text to convert.
	 * @param fragment optional existing fragment where to set the converted data, or null to create a new fragment.
	 * If an existing fragment is provided, no existing code is preserved: all codes are coming from the parsing
	 * of the input text, except if reuseCodes is set to true.
	 * @param reuseCodes true to re-use the codes of the provided text fragment. If a code is not found in the
	 * provided text fragment, one is created for the output.
	 * @return the new fragment created from the text.
	 */
	@Deprecated
	public static TextFragment fromLetterCodedToFragment (String text,
		TextFragment fragment,
		boolean reuseCodes)
	{
		return fromLetterCodedToFragment(text, fragment, reuseCodes, false);
	}

	/**
	 * Converts a letter-coded text to a fragment.
	 * <p>A letter-coded text is like "&lt;g1&gt;text&lt;x2/&gt;&lt;/g1&gt;&lt;b3/&gt;".
	 * @param text the text to convert.
	 * @param fragment optional existing fragment where to set the converted data, or null to create a new fragment.
	 * If an existing fragment is provided, no existing code is preserved: all codes are coming from the parsing
	 * of the input text, except if reuseCodes is set to true.
	 * @param reuseCodes true to re-use the codes of the provided text fragment. If a code is not found in the
	 * provided text fragment, one is created for the output.
	 * @param decodeEncodedLetterCodes true to reverse previous tag escaping, this should match the value of encodeExistingLetterCodes when the text was tag encoded
	 * @return the new fragment created from the text.
	 */
	public static TextFragment fromLetterCodedToFragment (String text,
		TextFragment fragment,
		boolean reuseCodes,
		boolean decodeEncodedLetterCodes)
	{
		// Case with no in-line codes
		if ( text.indexOf('<') == -1 ) {
			if ( fragment != null ) {
				fragment.setCodedText(text, true);
				return fragment;
			}
			else {
				return new TextFragment(text);
			}
		}
		// Adjust the reuse flag to avoid extra tests
		if ( fragment == null ) {
			reuseCodes = false;
		}
		
		// Otherwise: we have in-line codes
		ArrayList<Code> codes = new ArrayList<Code>();
		StringBuilder tmp = new StringBuilder(text);
		
		int n;
		int start = 0;
		int diff = 0;
		Code code;
		int index;
		
		Matcher m = patternLCOpening.matcher(text);
		while ( m.find(start) ) {
			n = m.start();
			code = null;
			if ( reuseCodes ) {
				index = fragment.getIndex(Integer.valueOf(m.group(1)));
				if ( index > -1 ) {
					code = fragment.getCode(index).clone();
				}
			}
			if ( code == null ) {
				code = new Code(TagType.OPENING, "Xpt", tmp.substring(n+diff, (n+diff)+m.group().length()));
				code.setId(Integer.valueOf(m.group(1)));
			}
			codes.add(code);
			tmp.replace(n+diff, (n+diff)+m.group().length(), String.format("%c%c",
				(char)TextFragment.MARKER_OPENING, TextFragment.toChar(codes.size()-1)));
			diff += (2-m.group().length());
			start = n+m.group().length();
		}
		start = diff = 0;
		m = patternLCClosing.matcher(tmp.toString());
		while ( m.find(start) ) {
			n = m.start();
			code = null;
			if ( reuseCodes ) {
				index = fragment.getIndexForClosing(Integer.valueOf(m.group(1)));
				if ( index > -1 ) {
					code = fragment.getCode(index).clone();
				}
			}
			if ( code == null ) {
				code = new Code(TagType.CLOSING, "Xpt", tmp.substring(n+diff, (n+diff)+m.group().length()));
				code.setId(Integer.valueOf(m.group(1)));
			}
			codes.add(code);
			tmp.replace(n+diff, (n+diff)+m.group().length(), String.format("%c%c",
				(char)TextFragment.MARKER_CLOSING, TextFragment.toChar(codes.size()-1)));
			diff += (2-m.group().length());
			start = n+m.group().length();
		}
		start = diff = 0;
		m = patternLCIsolated.matcher(tmp.toString());
		while ( m.find(start) ) {
			n = m.start();
			code = null;
			if ( reuseCodes ) {
				index = fragment.getIndex(Integer.valueOf(m.group(1)));
				if ( index > -1 ) {
					code = fragment.getCode(index).clone();
				}
			}
			if ( code == null ) {
				code = new Code(TagType.PLACEHOLDER, "Xph", tmp.substring(n+diff, (n+diff)+m.group().length()));
				code.setId(Integer.valueOf(m.group(1)));
			}
			codes.add(code);
			tmp.replace(n+diff, (n+diff)+m.group().length(), String.format("%c%c",
				(char)TextFragment.MARKER_ISOLATED, TextFragment.toChar(codes.size()-1)));
			diff += (2-m.group().length());
			start = n+m.group().length();
		}
		start = diff = 0;
		m = patternLCIsolatedB.matcher(tmp.toString());
		while ( m.find(start) ) {
			n = m.start();
			code = null;
			if ( reuseCodes ) {
				index = fragment.getIndex(Integer.valueOf(m.group(1)));
				if ( index > -1 ) {
					code = fragment.getCode(index).clone();
				}
			}
			if ( code == null ) {
				code = new Code(TagType.OPENING, "Xpt", tmp.substring(n+diff, (n+diff)+m.group().length()));
				code.setId(Integer.valueOf(m.group(1)));
			}
			codes.add(code);
			tmp.replace(n+diff, (n+diff)+m.group().length(), String.format("%c%c",
				(char)TextFragment.MARKER_ISOLATED, TextFragment.toChar(codes.size()-1)));
			diff += (2-m.group().length());
			start = n+m.group().length();
		}
		start = diff = 0;
		m = patternLCIsolatedE.matcher(tmp.toString());
		while ( m.find(start) ) {
			n = m.start();
			code = null;
			if ( reuseCodes ) {
				index = fragment.getIndexForClosing(Integer.valueOf(m.group(1)));
				if ( index > -1 ) {
					code = fragment.getCode(index).clone();
				}
			}
			if ( code == null ) {
				code = new Code(TagType.CLOSING, "Xpt", tmp.substring(n+diff, (n+diff)+m.group().length()));
				code.setId(Integer.valueOf(m.group(1)));
			}
			codes.add(code);
			tmp.replace(n+diff, (n+diff)+m.group().length(), String.format("%c%c",
				(char)TextFragment.MARKER_ISOLATED, TextFragment.toChar(codes.size()-1)));
			diff += (2-m.group().length());
			start = n+m.group().length();
		}

		String codedText = tmp.toString();

		if (decodeEncodedLetterCodes) {
			m = patternLCOpenCloseDecode.matcher(codedText);
			codedText = m.replaceAll(replacementLCOpenCloseDecode);
			m = patternLCAllIsolatedDecode.matcher(codedText);
			codedText = m.replaceAll(replacementLCAllIsolatedDecode);
		}

		// Create the fragment or update the existing one
		if ( fragment != null ) {
			fragment.setCodedText(codedText, codes, true);
			return fragment;
		}
		else {
			return new TextFragment(codedText, codes);
		}
	}

	/**
	 * Converts a text fragment into a letter-coded text.
	 * Use {@link #fromLetterCodedToFragment(String, TextFragment, boolean)} to convert back to a fragment.
	 * @param fragment the fragment to convert.
	 * @return the resulting letter-coded text. 
	 */
	@Deprecated
	public static String fromFragmentToLetterCoded (TextFragment fragment) {
		return fromFragmentToLetterCoded(fragment, false);
	}

	/**
	 * Converts a text fragment into a letter-coded text, optionally escaping
	 * tags that will interfere with conversion back to a fragment.
	 * 
	 * Use {@link #fromLetterCodedToFragment(String, TextFragment, boolean, boolean)} to
	 * convert back to a fragment.
	 * 
	 * @param fragment the fragment to convert.
	 * @param encodeExistingLetterCodes escape the tags.
	 * @return the resulting letter-coded text.
	 */
	public static String fromFragmentToLetterCoded (TextFragment fragment,
		boolean encodeExistingLetterCodes)
	{
		String codedText = fragment.getCodedText();
		List<Code> codes = fragment.getCodes();
		StringBuilder tmp = new StringBuilder();
		int index;

		if (encodeExistingLetterCodes) {
			Matcher m = patternLCOpenCloseEncode.matcher(codedText);
			codedText = m.replaceAll(replacementLCOpenCloseEncode);
			m = patternLCAllIsolatedEncode.matcher(codedText);
			codedText = m.replaceAll(replacementLCAllIsolatedEncode);
		}

		for ( int i=0; i<codedText.length(); i++ ) {
			switch ( codedText.codePointAt(i) ) {
			case TextFragment.MARKER_OPENING:
				index = TextFragment.toIndex(codedText.charAt(++i));
				tmp.append(String.format("<g%d>", codes.get(index).getId()));
				break;
			case TextFragment.MARKER_CLOSING:
				index = TextFragment.toIndex(codedText.charAt(++i));
				tmp.append(String.format("</g%d>", codes.get(index).getId()));
				break;
			case TextFragment.MARKER_ISOLATED:
				index = TextFragment.toIndex(codedText.charAt(++i));
				if ( codes.get(index).getTagType() == TagType.OPENING ) {
					tmp.append(String.format("<b%d/>", codes.get(index).getId()));
				}
				else if ( codes.get(index).getTagType() == TagType.CLOSING ) {
					tmp.append(String.format("<e%d/>", codes.get(index).getId()));
				}
				else {
					tmp.append(String.format("<x%d/>", codes.get(index).getId()));
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
