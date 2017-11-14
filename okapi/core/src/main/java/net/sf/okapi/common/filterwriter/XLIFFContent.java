/*===========================================================================
  Copyright (C) 2008-2013 by the Okapi Framework contributors
-----------------------------------------------------------------------------
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

import java.nio.charset.CharsetEncoder;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Namespaces;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.annotation.GenericAnnotation;
import net.sf.okapi.common.annotation.GenericAnnotationType;
import net.sf.okapi.common.annotation.GenericAnnotations;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.common.resource.TextPart;

import static net.sf.okapi.common.resource.Code.EXTENDED_CODE_TYPE_PREFIX;

/**
 * Handles the conversion between a coded text object and XLIFF.
 * In some case this class may output ITS attributes.
 * It assumes the namespace for ITS is declared and the corresponding prefix is 'its'.
 */
public class XLIFFContent {

	public static final String ITS_PREFIX = "its:";
	public static final String REF_PREFIX = "REF:";
	
	private String codedText;
	private List<Code> codes;
	private XLIFFContent innerContent;
	private CharsetEncoder chsEnc;
	private List<GenericAnnotations> standoff;
	private ITSContent itsCont;
	
	/**
	 * Creates a new XLIFFContent object without any content.
	 */
	public XLIFFContent () {
		codedText = "";
	}
	
	/**
	 * Creates a new XLIFFContent object and set its content to the given fragment.
	 * @param content The TextFragment object to format.
	 */
	public XLIFFContent (TextFragment content) {
		setContent(content);
	}

	public XLIFFContent(String codedText, List<Code> codes,
			XLIFFContent innerContent, CharsetEncoder chsEnc,
			List<GenericAnnotations> standoff, ITSContent itsCont) {
		super();
		this.codedText = codedText;
		this.codes = codes;
		this.innerContent = innerContent;
		this.chsEnc = chsEnc;
		this.standoff = standoff;
		this.itsCont = itsCont;
	}

	/**
	 * Sets the character set encoder to use. 
	 * @param chsEnc the character set encoder to use.
	 */
	public void setCharsetEncoder (CharsetEncoder chsEnc) {
		this.chsEnc = chsEnc;
	}
	
	/**
	 * Gets the current character set encoder.
	 * @return the current character set encoder.
	 */
	public CharsetEncoder getCharsetEncoder () {
		return chsEnc;
	}

	/**
	 * Sets the fragment to format.
	 * This method does not reset any possible standoff items. 
	 * @param content The TextFragment object to format.
	 * @return Itself
	 */
	public XLIFFContent setContent (TextFragment content) {
		return setContent(content, false);
	}
	
	/**
	 * Sets the fragment to format.
	 * @param content The TextFragment object to format.
	 * @param resetStandoff true to reset the ITS standoff items (e.g. when the fragment is the whole content of a text container).
	 * @return Itself
	 */
	public XLIFFContent setContent (TextFragment content,
		boolean resetStandoff)
	{
		codedText = content.getCodedText();
		codes = content.getCodes();
		// Check if we need to reset the standoff items
		if ( resetStandoff ) clearStandoff();
		return this;
	}
	

	
	/**
	 * Generates an XLIFF string from the content.
	 * This is the same as calling this.toString(1, true, false, false, false, true).
	 * @return The string formatted in XLIFF.
	 */
	@Override
	public String toString () {
		return toString(1, true, false, false, false, true, null);
	}

	/**
	 * Generates an XLIFF string from the content.
	 * This is the same as calling this.toString(1, true, false, gMode, false, true) and setting gMode.
	 * @param gMode True to use g/x markup, false to use bpt/ept/ph
	 * @return The string formatted in XLIFF.
	 */
	public String toString (boolean gMode) {
		return toString(1, true, false, gMode, false, true, null);
	}

	/**
	 * Generates an XLIFF string from the content.
	 * <p>In some cases, a reference to an ITS standoff element may be generated.
	 * Use {@link #getStandoff()} to get the standoff information that needs to be output
	 * along with the inline reference. Each call to this method resets 
	 * the standoff information returned by {@link #getStandoff()}.
	 * @param quoteMode 0=no quote escaped, 1=apos and quot, 2=#39 and quot,
	 * and 3=quot only.
	 * @param escapeGT True to always escape '&gt;' to gt
	 * @param codeOnlyMode True when the in-line codes are to be set as raw-values.
	 * @param gMode True to use g/x markup, false to use bpt/ept/ph
	 * This option is to be used when the in-line code is an XLIFF-in-line code itself.
	 * @param codeAttrs True to include extended code attributes in the output.
	 * @param includeIts True to include ITS markup in the output.
	 * @param trgLocId the id of the target locale.
	 * @return The string formatted in XLIFF.
	 */
	public String toString (int quoteMode,
		boolean escapeGT,
		boolean codeOnlyMode,
		boolean gMode,
		boolean codeAttrs,
		boolean includeIts,
		LocaleId trgLocId)
	{
		StringBuilder tmp = new StringBuilder();
		int index;
		Code code;
		
		for ( int i=0; i<codedText.length(); i++ ) {
			switch ( codedText.codePointAt(i) ) {
			case TextFragment.MARKER_OPENING:
				index = TextFragment.toIndex(codedText.charAt(++i));
				code = codes.get(index);
				if ( codeOnlyMode ) {
					tmp.append(code.toString());
				}
				else {
					// Output the code (if it's not a marker-only one)
					if ( !code.hasOnlyAnnotation() ) {
						if ( gMode ) {
							insertCodeStart(tmp, TAG.g, code, codeAttrs);
							tmp.append(">");
						}
						else {
							insertCodeStart(tmp, TAG.bpt, code, codeAttrs); //TODO: escape unsupported chars
							tmp.append(">");
							tmp.append(Util.escapeToXML(code.toString(), quoteMode, escapeGT, chsEnc));
							tmp.append("</bpt>");
						}
					}
					// Then, if needed, output the marker element
					// (Markers linked to original codes have the marker inside the spanned content) 
					if ( code.hasAnnotation("protected") ) {
						tmp.append("<mrk mtype=\"protected\">");
					}
					else if ( includeIts && code.hasAnnotation(GenericAnnotationType.GENERIC) ) {
						tmp.append("<mrk");
						outputITSAttributes(code.getGenericAnnotations(), quoteMode, escapeGT, tmp, true, true, trgLocId);
						tmp.append(">");
					}
				}
				break;
			case TextFragment.MARKER_CLOSING:
				index = TextFragment.toIndex(codedText.charAt(++i));
				code = codes.get(index);
				if ( codeOnlyMode ) {
					tmp.append(code.toString());
				}
				else {
					// Close the marker, if needed
					if ( includeIts && code.hasAnnotation(GenericAnnotationType.GENERIC) ) {
						tmp.append("</mrk>");
					}
					else if ( code.hasAnnotation("protected") ) {
						tmp.append("</mrk>");
					}
					// Then close the code
					if ( !code.hasOnlyAnnotation() ) {
						if ( gMode ) {
							tmp.append("</g>");
						}
						else {
							insertCodeStart(tmp, TAG.ept, code, codeAttrs); //TODO: escape unsupported chars
							tmp.append(">");
							tmp.append(Util.escapeToXML(code.toString(), quoteMode, escapeGT, chsEnc));
							tmp.append("</ept>");
						}
					}
				}
				break;
			case TextFragment.MARKER_ISOLATED:
				index = TextFragment.toIndex(codedText.charAt(++i));
				code = codes.get(index);
				if ( codeOnlyMode ) {
					tmp.append(code.toString());
				}
				else {
					if ( gMode ) {
						if ( code.getTagType() == TagType.OPENING ) {
							insertCodeStart(tmp, TAG.bx, code, codeAttrs);
							tmp.append("/>");
						}
						else if ( code.getTagType() == TagType.CLOSING ) {
							insertCodeStart(tmp, TAG.ex, code, codeAttrs);
							tmp.append("/>");
						}
						else {
							insertCodeStart(tmp, TAG.x, code, codeAttrs);
							if ( includeIts && code.hasAnnotation(GenericAnnotationType.GENERIC) ) {
								outputITSAttributes(code.getGenericAnnotations(), quoteMode, escapeGT, tmp, true, false, null);
							}
							tmp.append("/>");
						}
					}
					else {
						if ( code.getTagType() == TagType.OPENING ) {
							insertCodeStart(tmp, TAG.it, code, codeAttrs); //TODO: escape unsupported chars
							tmp.append(" pos=\"open\">");
							tmp.append(Util.escapeToXML(code.toString(), quoteMode, escapeGT, chsEnc));
							tmp.append("</it>");
						}
						else if ( code.getTagType() == TagType.CLOSING ) {
							insertCodeStart(tmp, TAG.it, code, codeAttrs); //TODO: escape unsupported chars
							tmp.append(" pos=\"close\">");
							tmp.append(Util.escapeToXML(code.toString(), quoteMode, escapeGT, chsEnc));
							tmp.append("</it>");
						}
						else {
							insertCodeStart(tmp, TAG.ph, code, codeAttrs); //TODO: escape unsupported chars
							if ( includeIts && code.hasAnnotation(GenericAnnotationType.GENERIC) ) {
								outputITSAttributes(code.getGenericAnnotations(), quoteMode, escapeGT, tmp, true, false, trgLocId);
							}
							tmp.append(">");
							tmp.append(Util.escapeToXML(code.toString(), quoteMode, escapeGT, chsEnc));
							tmp.append("</ph>");
						}
					}
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
			case '\r': // Not a line-break in the XML context, but a literal
				tmp.append("&#13;");
				break;
			case '<':
				tmp.append("&lt;");
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
				if ( codedText.charAt(i) > 127 ) { // Extended chars
					if ( Character.isHighSurrogate(codedText.charAt(i)) ) {
						int cp = codedText.codePointAt(i++);
						String buf = new String(Character.toChars(cp));
						if (( chsEnc != null ) && !chsEnc.canEncode(buf) ) {
							tmp.append(String.format("&#x%X;", cp));
						} else {
							tmp.append(buf);
						}
					}
					else {
						if (( chsEnc != null ) && !chsEnc.canEncode(codedText.charAt(i)) ) {
							tmp.append(String.format("&#x%04X;", codedText.codePointAt(i)));
						}
						else { // No encoder or char is supported
							tmp.append(codedText.charAt(i));
						}
					}
				}
				else { // ASCII chars
					tmp.append(codedText.charAt(i));
				}
				break;
			}
		}
		return tmp.toString();
	}
	
	/**
	 * Generates an XLIFF string from a given text container.
	 * @param container The container to write out.
	 * @param quoteMode 0=no quote escaped, 1=apos and quot, 2=#39 and quot,
	 * and 3=quot only.
	 * @param escapeGT True to always escape '&gt;' to &amp;gt;
	 * @param withMarkers True to output mrk elements, false to output only 
	 * the content of mrk element.
	 * @param gMode True to use g/x markup, false to use bpt/ept/ph
	 * @param codeAttrs True to include extended code attributes in the output.
	 * @param includeIts True to include ITS markup in the output.
	 * @param trgLocId the id of the target locale.
	 * @return The coded string.
	 */
	public String toSegmentedString (TextContainer container,
		int quoteMode,
		boolean escapeGT,
		boolean withMarkers,
		boolean gMode,
		boolean codeAttrs,
		boolean includeIts,
		LocaleId trgLocId)
	{
		StringBuilder tmp = new StringBuilder();
		if ( innerContent == null ) {
			innerContent = new XLIFFContent();
			innerContent.setCharsetEncoder(chsEnc);
		}
		else {
			// Make sure the standoff items are cleared (we start a new container)
			innerContent.clearStandoff();
		}

		for ( TextPart part : container ) {
			// Segment marker if needed
			if ( withMarkers && part.isSegment() && container.hasBeenSegmented() ) {
				tmp.append("<mrk mid=\""+((Segment)part).id+"\" mtype=\"seg\"");
				if ( includeIts ) {
					GenericAnnotations anns = ((Segment)part).getAnnotation(GenericAnnotations.class);
					if ( anns != null ) {
						// Output the annotators references (not a great solution to do this here)
						GenericAnnotation ann = anns.getFirstAnnotation(GenericAnnotationType.ANNOT);
						if ( ann != null ) {
							String values = ann.getString(GenericAnnotationType.ANNOT_VALUE);
							tmp.append(" "+Namespaces.ITS_NS_PREFIX+":annotatorsRef=\""+values+"\"");
						}
						// Output the other ITS attributes
						outputITSAttributes(anns,
							quoteMode, escapeGT, tmp, true, false, trgLocId); // mrk=false here because it's a segment
					}
				}
				tmp.append(">");
			}
			// Fragment
			innerContent.setContent(part.text);
			tmp.append(innerContent.toString(quoteMode, escapeGT, false, gMode, codeAttrs, includeIts, trgLocId));
			standoff = innerContent.getStandoff(); // Trickle up the standoff information too.
			if ( withMarkers && part.isSegment() && container.hasBeenSegmented() ) {
				tmp.append("</mrk>");
			}
		}
		
		return tmp.toString();
	}

	/**
	 * Gets the standoff information for a possible list of annotations.
	 * @return null if there are no standoff markup to generate, or a list of {@link GenericAnnotations} objects.
	 * The data of each annotation set is the id that is used
	 * in the local markup to point to this standoff markup.
	 */
	public List<GenericAnnotations> getStandoff () {
		return standoff;
	}
	
	public void clearStandoff () {
		standoff = null;
		if ( itsCont != null ) itsCont.clearStandoff();
	}
	
	private void outputITSAttributes (GenericAnnotations anns,
		int quoteMode,
		boolean escapeGT,
		StringBuilder output,
		boolean inline,
		boolean mrk,
		LocaleId trgLocId)
	{
		if ( itsCont == null ) {
			itsCont = new ITSContent(chsEnc, false, true);
		}
		itsCont.outputAnnotations(anns, output, inline, mrk, true, trgLocId);
		standoff = itsCont.getStandoff();
	}

	/**
	 * Write the preamble for an inline tag into a supplied StringBuilder. The tag
	 * is <em>not</em> closed; you must do this yourself afterward.
	 * @param sb StringBuilder in which to insert the code preamble
	 * @param tag The type of tag to write
	 * @param code The tag data
	 */
	private void insertCodeStart (StringBuilder sb, TAG tag,
			Code code, boolean includeAttrs)
	{
		sb.append("<");
		sb.append(tag.toString());
		sb.append(" id=\"");
		sb.append(code.getId());
		sb.append("\"");
		
		if (!includeAttrs) return;
		
		if (!tag.supported.isEmpty() && !code.getType().equals(Code.TYPE_NULL)) {
			sb.append(" ctype=\"").append(createCtype(code, tag)).append("\"");
		}
		
		String displayText = code.getDisplayText();
		if (displayText == null
				&& (tag == TAG.x || tag == TAG.bx || tag == TAG.ex || tag == TAG.g)) {
			displayText = code.getData();
		}
		if (!Util.isEmpty(displayText)) {
			sb.append(" equiv-text=\"");
			sb.append(Util.escapeToXML(displayText, 1, false, null));
			sb.append("\"");
		}
		
		// has the code been combined with other codes (merged)?
		// then mark it so we know the data is a serialized codes string
		if (code.isMerged()) {
			sb.append(" okp:merged=\"");
			String merged = code.getMergedData();
			merged = Util.escapeWhitespaceForXML(merged);
			sb.append(Util.escapeToXML(merged, 1, false, null));
			sb.append("\"");
		}
	}

	/**
	 * creates a ctype value for the given {@link Code} object and the given {@link TAG}.
	 *
	 * @param code the code object
	 * @param tag the xliff TAG
	 * @return a ctype value for the given {@link Code} object and the given {@link TAG}.
	 */
	private String createCtype(Code code, TAG tag) {
		String result = "";

		String codeType = code.getType();
		if (!isValidCtype(tag, codeType)) {
			result = EXTENDED_CODE_TYPE_PREFIX;
		}

		if (codeType == null || codeType.isEmpty()) {
			result += "empty";
		}
		else {
			// escape xml
			result += Util.escapeToXML(codeType, 1, false, null);
			// replace blanks in ctype values to underscores
			result = result.replaceAll(" ", "_");
		}

		return result;
	}

	/**
	 * Check whether or not a given string is a valid ctype value for
	 * a given tag. Ctypes not predefined in the XLIFF specification
	 * are considered valid custom values if they are prefixed with "x-".
	 * @param tag Tag to check
	 * @param type ctype value
	 * @return Whether or not the value is valid for the given tag
	 */
	private boolean isValidCtype (TAG tag, String type) {
		if (!tag.supported.isEmpty() && tag.supported.contains(type)) {
			return true;
		}
		return type.startsWith(EXTENDED_CODE_TYPE_PREFIX);
	}
	
	private static final Set<String> X_AND_PH_TAGS = new HashSet<String>();
	private static final Set<String> OTHER_TAGS = new HashSet<String>();
	
	@SuppressWarnings("unchecked")
	enum TAG {
		x (X_AND_PH_TAGS),
		ph (X_AND_PH_TAGS),
		g (OTHER_TAGS),
		bx (OTHER_TAGS),
		ex (Collections.EMPTY_SET),
		bpt (OTHER_TAGS),
		ept (Collections.EMPTY_SET),
		it (OTHER_TAGS);
		
		public final Set<String> supported;
		
		TAG (Set<String> values) {
			supported = values;
		}
	}
	
	static {
		X_AND_PH_TAGS.add("image");
		X_AND_PH_TAGS.add("pb");
		X_AND_PH_TAGS.add("lb");
		
		OTHER_TAGS.add("bold");
		OTHER_TAGS.add("italic");
		OTHER_TAGS.add("underlined");
		OTHER_TAGS.add("link");
	}

	public final String getCodedText() {
		return codedText;
	}

	public final List<Code> getCodes() {
		return codes;
	}

	public final XLIFFContent getInnerContent() {
		return innerContent;
	}

	public final ITSContent getItsCont() {
		return itsCont;
	}
}
