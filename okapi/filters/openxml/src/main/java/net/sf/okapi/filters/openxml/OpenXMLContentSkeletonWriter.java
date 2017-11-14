/*===========================================================================
  Copyright (C) 2008-2011 by the Okapi Framework contributors
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

package net.sf.okapi.filters.openxml; // DWH 4-8-09

import static net.sf.okapi.filters.openxml.ParseType.MSPOWERPOINT;
import static net.sf.okapi.filters.openxml.ParseType.MSWORD;

import java.util.List;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.MimeTypeMapper;
import net.sf.okapi.common.encoder.EncoderContext;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.skeleton.GenericSkeletonWriter;

// For this to work, expandCodeContent has to be changed to protected in GenericSkeletonWriter

/**
 * <p>Implements ISkeletonWriter for OpenXMLContentFilter, which
 * filters Microsoft Office Word, Excel, and Powerpoint Documents.
 * OpenXML is the format of these documents.
 * 
 * <p>Since OpenXML files are Zip files that contain XML documents,
 * <b>OpenXMLZipFilter</b> handles opening and processing the zip file, and
 * instantiates this skeleton writer to process the XML documents.
 * 
 * <p>This skeleton writer exhibits slightly different behavior depending 
 * on whether the XML file is Word, Excel, Powerpoint, or a chart in Word.
 * If there is was no character style information in the original XML file, 
 * such as &lt;w:r&gt;&lt;w:t&gt;text&lt;/w:t&gt;&lt;/w:r&gt;, the tags were not made codes in
 * OpenXMLContentFilter, so these tags need to be reintroduced by this
 * skeleton writer.
 */

public class OpenXMLContentSkeletonWriter extends GenericSkeletonWriter {

	private static final char TAB = '\t';
	private static final char LINE_SEPARATOR = '\u2028';

	private ParseType configurationType; // DWH 4-10-09
	private EncoderManager internalEncoderManager; // The encoderManager of the super class is not used
	private boolean bInBlankText=false; // DWH 10-27-09

	
	public OpenXMLContentSkeletonWriter(ParseType configurationType) // DWH 4-8-09
	{
		super();
		this.configurationType = configurationType; // DWH 4-10-09
		internalEncoderManager = new EncoderManager(); // DWH 5-14-09
		internalEncoderManager.setMapping(MimeTypeMapper.XML_MIME_TYPE, "net.sf.okapi.common.encoder.XMLEncoder");
		internalEncoderManager.setMapping(MimeTypeMapper.DOCX_MIME_TYPE, "net.sf.okapi.common.encoder.OpenXMLEncoder");
		internalEncoderManager.setDefaultOptions(null, "utf-8", "\n"); // DWH 5-14-09
		internalEncoderManager.updateEncoder(MimeTypeMapper.DOCX_MIME_TYPE); // DWH 5-14-09
	}

	private String getRunPrefix() {
		return (configurationType == MSWORD) ? "w:" : "a:";
	}

	private String serializeText(String text, boolean needsPreserveSpace) {
		StringBuilder sb = new StringBuilder();
		String sPreserve = "";
		if (configurationType != MSWORD && configurationType != MSPOWERPOINT) {
			return text;
		}
		String prefix = getRunPrefix();

		if (needsPreserveSpace)
			sPreserve = " xml:space=\"preserve\"";
		// Write opening tags
		sb.append("<").append(prefix).append("r><").append(prefix).append("t").append(sPreserve).append(">");
		// Write text
		for (char c : text.toCharArray()) {
			if (c == '\n') {
				sb.append("</").append(prefix).append("t>");
				sb.append("<").append(prefix).append("br/>");
				sb.append("<").append(prefix).append("t").append(sPreserve).append(">");
			}
			else {
				sb.append(c);
			}
		}
		// Write closing tags
		sb.append("</").append(prefix).append("t></").append(prefix).append("r>");
		return sb.toString();
	}
	/**
	 * Gets the content out of a coded text string.  If the text was "blank", i.e.
               * only surrounded by &lt;w:r&gt;&lt;w:t&gt; and &lt;/w:t&gt;&lt;/w:r&gt; in the original
               * input, these tags are restored around the translated text.  Codes are
               * expanded by calling expandCodeContent in GenericSkeletonWriter.
               * StyledText is not blank if it was surrounded by OPENING and CLOSING codes.
	 * @param tf TextFragment containing the coded text to expand
	 * @param langToUse output language to use, in en-US format
	 * @param context same as context variable in GenericFilterWriter
	 * @return text with all of the codes expanded and blank text surrounded
	 */
	@Override
	public String getContent (TextFragment tf,
		LocaleId langToUse,
		EncoderContext context)
	{
		String sTuff; // DWH 4-8-09
		String text=tf.getCodedText(); // DWH 10-27-09 they changed toString to show all text
		bInBlankText=false; // DWH 4-8-09
		boolean bHasBlankInText=false; // DWH 5-28-09
		int nSurroundingCodes=0; // DWH 4-8-09
		// Output simple text
		if ( !tf.hasCode() ) {
			if (text.length()>0)
			{
				sTuff = text; // DWH 5-22-09
				if ( internalEncoderManager == null ) // DWH 5-22-09 whole if-else: encode first
				{
					if ( getLayer() != null )
						sTuff = getLayer().encode(text, context);
				}
				else
				{	
					if ( getLayer() == null )
						sTuff = internalEncoderManager.encode(text, context);
					else
						sTuff = getLayer().encode(internalEncoderManager.encode(sTuff, context), context);
				}
				if (context.ordinal()==EncoderContext.SKELETON.ordinal())
					// DWH 5-22-09 add unencoded tags if needed 1-8-2013 ordinal
				{
					bHasBlankInText = needsXmlSpacePreserve(text);
					text = serializeText(sTuff, bHasBlankInText);
				}
				else if (context.ordinal()==EncoderContext.TEXT.ordinal() && configurationType==MSWORD) // DWH 1-7-2014
				{ // context has to be one more than nTextBoxLevel; if more, it is in an attribute
				  
					bHasBlankInText = needsXmlSpacePreserve(text);
					text = serializeText(sTuff, bHasBlankInText);
				}
				else
					text = sTuff; // DWH 5-22-09
				return text; // DWH 5-22-09
			}
			else
				return ""; // DWH 5-18-09 get nothing, return nothing
		}

		// Output text with in-line codes
		List<Code> codes = tf.getCodes();
		StringBuilder tmp = new StringBuilder();
		text = tf.getCodedText();
		String prefix = getRunPrefix();
		Code code;
		char ch;
		/**
		 * This loop is the source of some pretty gross hacks, and it needs to be
		 * rewritten (probably in conjunction with a chunk of OpenXMLContentFilter code).
		 * The way that this code tries to track XML state is insufficient and leads to
		 * occasional document corruption.
		 */
		for ( int i=0; i<text.length(); i++ ) {
			ch = text.charAt(i);
			switch ( ch ) {
			case TextFragment.MARKER_OPENING:
				tmp = blankEnd(context,nSurroundingCodes,tmp);
				code = codes.get(TextFragment.toIndex(text.charAt(++i)));
				if (checkForOpenTextTag(code.getData())) {
					bInBlankText = true;
				}
				tmp.append(expandCodeContent(code, langToUse, context));
				nSurroundingCodes++;
				break;
			case TextFragment.MARKER_CLOSING:
				code = codes.get(TextFragment.toIndex(text.charAt(++i)));
				if (!code.getData().startsWith("</w:t>") && !code.getData().startsWith("</a:t>")) {
					tmp = blankEnd(context,nSurroundingCodes,tmp);
				}
				else {
					bInBlankText = false;
				}
				tmp.append(expandCodeContent(code, langToUse, context));
				nSurroundingCodes--;
				break;
			case TextFragment.MARKER_ISOLATED:
				code = codes.get(TextFragment.toIndex(text.charAt(++i)));
				// Only close out the text/run if we need it
				if (!code.getData().startsWith("</w:t>") && !code.getData().startsWith("</a:t>")) {
					tmp = blankEnd(context,nSurroundingCodes,tmp);
				}
				else {
					bInBlankText = false;
				}
				if (checkForOpenTextTag(code.getData())) {
					bInBlankText = true;
				}
				if (code.getTagType()==TextFragment.TagType.OPENING) {
					nSurroundingCodes++;
				}
				else if (code.getTagType()==TextFragment.TagType.CLOSING)
					nSurroundingCodes--;
				tmp.append(expandCodeContent(code, langToUse, context));
				break;
			case TAB:
				// We add tabs as literal characters, which means we have to make sure
				// we are preserving space.
				addSpacePreserveToLastT(tmp);
				tmp.append(ch);
				break;
			case LINE_SEPARATOR:
				tmp.append("</").append(prefix).append("t><")
				    .append(prefix).append("br/><").append(prefix).append("t>");
				break;
			default:
				if (!bInBlankText && (nSurroundingCodes<=0))
				{
					if (context.ordinal()==EncoderContext.SKELETON.ordinal()) { // DWH 4-13-09 whole if 1-8-2014 ordinal
						bInBlankText = true;
						if (configurationType==MSWORD)
							tmp.append(encody("<w:r><w:t xml:space=\"preserve\">",context));
						else if (configurationType==MSPOWERPOINT)
							tmp.append(encody("<a:r><a:t>",context));
					}
					else if (context.ordinal()==EncoderContext.TEXT.ordinal() && configurationType==MSWORD) // DWH 1-7-2014
					//1-7-2014	else if (context.ordinal()==nTextBoxLevel+1 && context.ordinal()-nContentDepth==0 && configurationType==MSWORD)
					{ // only add codes around blank text if context is one above nTextBoxLevel, otherwise inside attributes
						bInBlankText = true;
						tmp.append(encody("<w:r><w:t xml:space=\"preserve\">",context));						
					}
				}
				if ( Character.isHighSurrogate(ch) ) {
					int cp = text.codePointAt(i);
					i++; // Skip low-surrogate
					if ( internalEncoderManager == null ) {
						if ( getLayer() == null ) {
							tmp.append(new String(Character.toChars(cp)));
						}
						else {
							tmp.append(getLayer().encode(cp, context));
						}
					}
					else {
						if ( getLayer() == null ) {
							tmp.append(internalEncoderManager.encode(cp, context));
						}
						else {
							tmp.append(getLayer().encode(
									internalEncoderManager.encode(cp, context),
								context));
						}
					}
				}
				else { // Non-supplemental case
					if ( internalEncoderManager == null ) {
						if ( getLayer() == null ) {
							tmp.append(ch);
						}
						else {
							tmp.append(getLayer().encode(ch, context));
						}
					}
					else {
						if ( getLayer() == null ) {
							tmp.append(internalEncoderManager.encode(ch, context));
						}
						else {
							tmp.append(getLayer().encode(
									internalEncoderManager.encode(ch, context),
								context));
						}
					}
				}
				break;
			}
		}
		tmp = blankEnd(context,nSurroundingCodes,tmp);
		return tmp.toString();
	}

	private boolean checkForOpenTextTag(String text) {
		return text.endsWith("<w:t>") || text.endsWith("<w:t xml:space=\"preserve\">") ||
				 text.endsWith("<a:t>") || text.endsWith("<a:t xml:space=\"preserve\">");
	}

	/**
	 * Adds {@code xml:space="preserve"} to the last {@code &lt;w:t&gt;} element of the text in the given
	 * {@link StringBuilder}.
	 *
	 * @param tmp the text container
	 */
	void addSpacePreserveToLastT(StringBuilder tmp) {
		int startOfT = tmp.lastIndexOf("<w:t");
		if (startOfT != -1) {
			int endOfT = tmp.indexOf(">", startOfT);
			if (endOfT != -1) {
				String t = tmp.substring(startOfT, endOfT);
				if (!t.contains("xml:space")) {
					t += " xml:space=\"preserve\">";
					tmp.replace(startOfT, endOfT + 1, t);
				}
			}
		}
	}

	/**
	 * Returns true if the given text contains a space, tab or no-break space. In that case you
	 * have to add {@code xml:space="preserve"} to the {@code &lt;w:t&gt;} element.
	 *
	 * @param text text
	 * @return true if the given text contains a space, tab or no-break space
	 */
	static boolean needsXmlSpacePreserve(String text) {
		for (char c : text.toCharArray()) {
			// This catches things like ideographic space (U+3000).  NBSP
			// isn't flagged as whitespace in unicode, so we have to special-case it.
			if (Character.isWhitespace(c) || c == '\u00A0') return true;
		}
		return false;
	}

	/**
	 * Handles layers and encoding of a string to be expanded.
	 * @param s string to be expanded
	 * @return context same as context variable in getContent in GenericSkeletonWriter
	 * @param s string to be expanded
	 */
	private String encody(String s, EncoderContext context)
	{
		return(s); // DWH 5-14-09 no encoding is necessary for tags

	}
	private StringBuilder blankEnd(EncoderContext context, int nSurroundingCodes, StringBuilder tmp)
	{
		String tail = tmp.length() >= 6 ? tmp.substring(tmp.length() - 6) : "";
		if (bInBlankText && (nSurroundingCodes<=0) &&
				!(tail.equals("</w:r>") || tail.equals("</a:r>")))
		{
			if (context.ordinal()==EncoderContext.SKELETON.ordinal()) { // DWH 4-13-09 whole if 1-8-2014 ordinal
				bInBlankText = false;
				if (configurationType==MSWORD)
					tmp.append(encody("</w:t></w:r>",context));
				else if (configurationType==MSPOWERPOINT)
					tmp.append(encody("</a:t></a:r>",context));
			}
			else if (context.ordinal()==EncoderContext.TEXT.ordinal() && configurationType==MSWORD) // DWH 1-7-2014
			{ // each TextBox only adds 1 to context
			  // nContentDepth is how embedded the text is
				bInBlankText = false;
				tmp.append(encody("</w:t></w:r>",context));				
			}
		}
		else {
			bInBlankText = false;
		}
		return tmp;
	}
	
}
