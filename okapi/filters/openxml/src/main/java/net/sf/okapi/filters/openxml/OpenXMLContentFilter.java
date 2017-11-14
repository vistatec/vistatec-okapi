/*===========================================================================
  Copyright (C) 2009-2011 by the Okapi Framework contributors
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

package net.sf.okapi.filters.openxml;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import net.htmlparser.jericho.EndTag;
import net.htmlparser.jericho.StartTag;
import net.htmlparser.jericho.Tag;
import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.MimeTypeMapper;
import net.sf.okapi.common.encoder.EncoderContext;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.filters.FilterConfiguration;
import net.sf.okapi.common.filters.PropertyTextUnitPlaceholder;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.DocumentPart;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.common.skeleton.GenericSkeleton;
import net.sf.okapi.filters.abstractmarkup.AbstractMarkupFilter;
import net.sf.okapi.filters.abstractmarkup.config.TaggedFilterConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Filters Microsoft Office Word, Excel, and Powerpoint Documents.
 * OpenXML is the format of these documents.
 *
 * <p>Since OpenXML files are Zip files that contain XML documents,
 * <b>OpenXMLFilter</b> handles opening and processing the zip file, and
 * instantiates this filter to process the XML documents.
 *
 * <p>This filter extends AbstractBaseMarkupFilter, which extends
 * AbstractBaseFilter.  It uses the Jericho parser to analyze the
 * XML files.
 *
 * <p>The filter exhibits slightly differnt behavior depending on whether
 * the XML file is Word, Excel, Powerpoint, or a chart in Word.  The
 * tags in these files are configured in yaml configuration files that
 * specify the behavior of the tags.  These configuration files are
 * <ul>
 * <li>wordConfiguration.yml</li>
 * <li>excelConfiguration.yml</li>
 * <li>powerpointConfiguration.yml</li>
 * <li>wordChartConfiguration.yml</li>
 * </ul>
 *
 * In Word and Powerpoint, text is always surrounded by paragraph tags
 * &lt;w:p&gt; or &lt;a:p&gt;, which signal the beginning and end of the text unit
 * for this filter, and are marked as TEXT_UNIT_ELEMENTs in the configuration
 * files.  Inside these are one or more text runs surrounded by &lt;w:r&gt; or &lt;a:r&gt;
 * tags and marked as TEXT_RUN_ELEMENTS by the configuration files.  The text
 * itself occurs between text marker tags &lt;w:t&gt; or &lt;a:t&gt; tags, which are
 * designated TEXT_MARKER_ELEMENTS by the configuration files.  Tags between
 * and including &lt;w:r&gt; and &lt;w:t&gt; (which usually include a &lt;w:rPr&gt; tag sequence
 * for character style) are consolidated into a single MARKER_OPENING code.  Tags
 * between and including &lt;/w:t&gt; and &lt;/w:r&gt;, which sometimes include graphics
 * tags, are consolidated into a single MARKER_CLOSING code.  If there is no
 * text between &lt;w:r&gt; and &lt;/w:r&gt;, a single MARKER_PLACEHOLDER code is created
 * for the text run.  If there is no character style information,
 * &lt;w:r&gt;&lt;w:t&gt;text&lt;/w:t&gt;&lt;/w:r&gt; is not surrounded by MARKER_OPENING or
 * MARKER_CLOSING codes, to simplify things for translators; these are supplied
 * by OpenXMLContentSkeletonWriter during output.  The same is true for text
 * runs marked by &lt;a:r&gt; and &lt;a:t&gt; in Powerpoint files.
 *
 * Excel files are simpler, and only mark text by &lt;v&gt;, &lt;t&gt;, and &lt;text&gt; tags
 * in worksheet, sharedString, and comment files respectively.  These tags
 * work like TEXT_UNIT, TEXT_RUN, and TEXT_MARKER elements combined.
 */
public class OpenXMLContentFilter extends AbstractMarkupFilter {

	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	private ParseType configurationType;
	//	private Package p=null;
	private ParseType filetype=ParseType.MSWORD; // DWH 4-13-09
	private String sConfigFileName; // DWH 10-15-08
	private URL urlConfig; // DWH 3-9-09
	private StringBuilder sInExclusion = new StringBuilder();
	private boolean bInTextRun = false; // DWH 4-10-09
	private boolean bInSubTextRun = false; // DWH 4-10-09
	private boolean bBetweenTextMarkers=false; // DWH 4-14-09
	private boolean bAfterText = false; // DWH 4-10-09
	private TextRun trTextRun = null; // DWH 4-10-09
	private TextRun trNonTextRun = null; // DWH 5-5-09
	private boolean bIgnoredPreRun = false; // DWH 4-10-09
	private boolean bBeforeFirstTextRun = true; // DWH 4-15-09
	private boolean bInMainFile = false; // DWH 4-15-09
	private boolean bInSettingsFile = false; // DWH 4-12-10
	private boolean bExcludeTextInRun = false; // DWH 5-27-09
	private boolean bExcludeTextInUnit = false; // DWH 5-29-09
	private String sCurrentCharacterStyle = ""; // DWH 5-27-09
	private String sCurrentParagraphStyle = ""; // DWH 5-27-09
	private ConditionalParameters filterParams = null;
	private TreeSet<String> tsExcludeWordStyles = new TreeSet<String>(); // DWH 5-27-09 set of styles to exclude from translation
	private YamlParameters params=null; // DWH 7-16-09
	private TaggedFilterConfiguration config=null; // DWH 7-16-09
	private EncoderManager internalEncManager; // The encoderManager of the base class is not used
	private StringBuilder endpara = new StringBuilder();
	private String partName;
	private String pendingTagName;
	private String pendingTagText;
	private boolean bInPowerpointComment = false;

	public OpenXMLContentFilter(ConditionalParameters filterParams, String partName) {
		super(); // 1-6-09
		this.filterParams = filterParams;
		this.partName = partName;
		setMimeType(MimeTypeMapper.XML_MIME_TYPE);
		setFilterWriter(createFilterWriter());
		tsExcludeWordStyles = new TreeSet<String>();
		internalEncManager = new EncoderManager(); // DWH 5-14-09
		internalEncManager.setMapping(MimeTypeMapper.XML_MIME_TYPE, "net.sf.okapi.common.encoder.XMLEncoder");
		internalEncManager.setMapping(MimeTypeMapper.DOCX_MIME_TYPE, "net.sf.okapi.common.encoder.OpenXMLEncoder");
//		internalEncManager.setAllKnownMappings();
		internalEncManager.setDefaultOptions(null, "utf-8", "\n"); // DWH 5-14-09
		internalEncManager.updateEncoder(MimeTypeMapper.DOCX_MIME_TYPE); // DWH 5-14-09
	}

	public List<FilterConfiguration> getConfigurations () {
		List<FilterConfiguration> list = new ArrayList<FilterConfiguration>();
		list.add(new FilterConfiguration(getName(),
				getMimeType(),
				getClass().getName(),
				"Microsoft OpenXML Document",
				"Microsoft OpenXML files (Used inside Office documents)."));
		return list;
	}

	/**
	 * Logs information about the event fir the log level is FINEST.
	 * @param event event to log information about
	 */
	public void displayOneEvent(Event event) // DWH 4-22-09 LOGGER
	{
		Set<String> setter;
		if (LOGGER.isTraceEnabled())
		{
			String etyp=event.getEventType().toString();
			if (event.getEventType() == EventType.TEXT_UNIT) {
				//			assertTrue(event.getResource() instanceof TextUnit);
			} else if (event.getEventType() == EventType.DOCUMENT_PART) {
				//			assertTrue(event.getResource() instanceof DocumentPart);
			} else if (event.getEventType() == EventType.START_GROUP
					|| event.getEventType() == EventType.END_GROUP) {
				//			assertTrue(event.getResource() instanceof StartGroup || event.getResource() instanceof Ending);
			} else if (event.getEventType() == EventType.START_SUBFILTER
					|| event.getEventType() == EventType.END_SUBFILTER) {
				//				assertTrue(event.getResource() instanceof StartSubfilter || event.getResource() instanceof Ending);
			}
			if (etyp.equals("START"))
				LOGGER.trace("\n");
			LOGGER.trace("{}: ", etyp);
			if (event.getResource() != null) {
				LOGGER.trace("({})", event.getResource().getId());
				if (event.getResource() instanceof DocumentPart) {
					setter = ((DocumentPart) event.getResource()).getSourcePropertyNames();
					for(String seti : setter)
						LOGGER.trace(seti);
				} else {
					LOGGER.trace(event.getResource().toString());
				}
				if (event.getResource().getSkeleton() != null) {
					LOGGER.trace("*Skeleton: \n{}", event.getResource().getSkeleton().toString());
				}
			}
		}
	}

	public ParseType getParseType() {
		return filetype;
	}

	/**
	 * Sets the name of the Yaml configuration file for the current file type, reads the file, and sets the parameters.
	 * @param filetype type of XML in the current file
	 */
	public void setUpConfig(ParseType filetype)
	{
		this.filetype = filetype; // DWH 5-13-09
		switch(filetype)
		{
			case MSEXCEL:
				sConfigFileName = "/net/sf/okapi/filters/openxml/excelConfiguration.yml"; // DWH 1-5-09 groovy -> yml
				configurationType = ParseType.MSEXCEL;
				break;
			case MSPOWERPOINT:
				sConfigFileName = "/net/sf/okapi/filters/openxml/powerpointConfiguration.yml"; // DWH 1-5-09 groovy -> yml
				configurationType = ParseType.MSPOWERPOINT;
				break;
			case MSEXCELCOMMENT: // DWH 5-13-09
				sConfigFileName = "/net/sf/okapi/filters/openxml/excelCommentConfiguration.yml"; // DWH 1-5-09 groovy -> yml
				configurationType = ParseType.MSEXCEL;
				break;
			case MSWORDDOCPROPERTIES: // DWH 5-13-09
				sConfigFileName = "/net/sf/okapi/filters/openxml/wordDocPropertiesConfiguration.yml"; // DWH 5-25-09
				configurationType = ParseType.MSWORDDOCPROPERTIES;
				break;
			case MSPOWERPOINTCOMMENTS:
				sConfigFileName = "/net/sf/okapi/filters/openxml/powerpointCommentConfiguration.yml";
				configurationType = ParseType.MSPOWERPOINT;
				break;
			case MSWORD:
			default:
				sConfigFileName = "/net/sf/okapi/filters/openxml/wordConfiguration.yml"; // DWH 1-5-09 groovy -> yml
				configurationType = ParseType.MSWORD;
				break;
		}
		urlConfig = OpenXMLContentFilter.class.getResource(sConfigFileName); // DWH 3-9-09
		config = new TaggedFilterConfiguration(urlConfig);
//		setDefaultConfig(urlConfig); // DWH 7-16-09 no longer needed; AbstractMarkup now calls getConfig everywhere
		try
		{
			setParameters(new YamlParameters(urlConfig));
			// DWH 3-9-09 it doesn't update automatically from setDefaultConfig 7-16-09 YamlParameters
		}
		catch(Exception e)
		{
			throw new OkapiIOException("Can't read MS Office Filter Configuration File.");
		}
	}

	/**
	 * Adds CDATA as a DocumentPart
	 * @param tag tag containing the CDATA
	 */
	protected void handleCdataSection(Tag tag) { // 1-5-09
		addToDocumentPart(tag.toString());
	}

	/**
	 * Handles text.  If in a text run, it ends the text run and
	 * adds the tags that were in it as a single MARKER_OPENING code.
	 * This would correspond to &lt;w:r&gt;...&lt;w:t&gt; in MSWord.  It will
	 * then start a new text run anticipating &lt;/w:t&gt;...&lt;/w:r&gt;.  If
	 * text is found that was not in a text run, i.e. it was not between
	 * text markers, it is not text to be processed by a user, so it
	 * becomes part of a new text run which will become part of a
	 * code.  If the text is not in a text unit, then it is added to a
	 * document part.
	 * @param text the text to be handled
	 */
	@Override
	protected void handleText(CharSequence text) {
		if (text==null) // DWH 4-14-09
			return;
		startDelayedTextUnit();
		String txt=text.toString();
		handleSomeText(txt, isWhiteSpace(text)); // DWH 5-14-09
	}

	private void handleSomeText(String tixt, boolean bWhiteSpace) // DWH 6-25-09 tixt was txt
	{
		String txt=tixt; // DWH 6-25-09 added this so txt can be changed for Excel index to shared strings
		if (getRuleState().isExludedState()) {
			sInExclusion.append(tixt);
			return;
		}
		// check for ignorable whitespace and add it to the skeleton
		// The Jericho html parser always pulls out the largest stretch of text
		// so standalone whitespace should always be ignorable if we are not
		// already processing inline text
//		if (text.isWhiteSpace() && !isInsideTextRun()) {
		if (bWhiteSpace && !isInsideTextRun()) {
			addToDocumentPart(txt);
			return;
		}
		if (canStartNewTextUnit())
		{
			addToDocumentPart(txt);
		}
		else
		{
			if (bInTextRun) // DWH 4-20-09 whole if revised
			{
				if (bBetweenTextMarkers)
				{
					if (filetype==ParseType.MSEXCEL && txt!=null && txt.length()>0 && txt.charAt(0)=='=')
						addToTextRun(txt); // DWH 5-13-09 don't treat Excel formula as text to be translated
					else if (bExcludeTextInRun || bExcludeTextInUnit) // DWH 5-29-09 don't treat as text if excluding text
						addToTextRun(internalEncManager.encode(txt, EncoderContext.TEXT)); // DWH 8-7-09 still have to encode text if not in text unit
					else
					{
						addTextRunToCurrentTextUnit(false); // adds a code for the preceding text run
						bAfterText = true;

						addToTextUnit(txt); // adds the text
						trTextRun = new TextRun(); // then starts a new text run for a code after the text
						bInTextRun = true;
					}
				}
				else
					addToTextRun(internalEncManager.encode(txt, EncoderContext.TEXT)); // for <w:delText>text</w:delText> don't translate deleted text (will be inside code)
			}
			else if (bInPowerpointComment) {
				addToTextUnit(txt);
			}
			else
			{
				trTextRun = new TextRun();
				bInTextRun = true;
				addToTextRun(internalEncManager.encode(txt, EncoderContext.TEXT)); // not inside text markers, so this text will become part of a code
			}
		}
	}

	/**
	 * Handles a tag that is anticipated to be a DocumentPart.  Since everything
	 * between TEXTUNIT markers is treated as an inline code, if there is a
	 * current TextUnit, this is added as a code in the text unit.
	 * @param tag a tag
	 */
	@Override
	protected void handleDocumentPart(Tag tag) {
		if (canStartNewTextUnit()) // DWH ifline and whole else: is an inline code if inside a text unit
			addToDocumentPart(tag.toString()); // 1-5-09
		else
			addCodeToCurrentTextUnit(tag);
	}

	/**
	 * Handles a start tag.  TEXT_UNIT_ELEMENTs start a new TextUnit.  TEXT_RUN_ELEMENTs
	 * start a new text run.  TEXT_MARKER_ELEMENTS set a flag that any following
	 * text will be between text markers.  ATTRIBUTES_ONLY tags have translatable text
	 * in the attributes, so within a text unit, it is added within a text run; otherwise it
	 * becomes a DocumentPart.
	 * @param startTag the start tag to process
	 */
	@Override
	protected void handleStartTag(StartTag startTag) {
		String sTagName;
		String sTagString;
		String sTagElementType; // DWH 6-13-09
		if (startTag==null) // DWH 4-14-09
			return;
		// If we were waiting to start a text unit, do so
		startDelayedTextUnit();
		sTagName = startTag.getName(); // DWH 2-26-09
		sTagString = startTag.toString(); // DWH 2-26-09
		sTagElementType = getConfig().getElementType(startTag); // DWH 6-15-10
		if (getRuleState().isExludedState()) {
			sInExclusion.append(sTagString);
			// process these tag types to update parser state
			switch (getConfig().getElementRuleTypeCandidate(sTagName)) {
				// DWH 1-23-09
				case EXCLUDED_ELEMENT:
					getRuleState().pushExcludedRule(sTagName);
					break;
				case INCLUDED_ELEMENT:
					getRuleState().pushIncludedRule(sTagName);
					break;
				case PRESERVE_WHITESPACE:
					getRuleState().pushPreserverWhitespaceRule(sTagName, true);
					break;
			}
			return;
		}
		if (sTagName.equals("p:text")) {
			bInPowerpointComment = true;
		}
		switch (getConfig().getElementRuleTypeCandidate(sTagName)) {
			// DWH 1-23-09
			case INLINE_ELEMENT:
				if (getParseType().equals(ParseType.MSEXCEL)) {
					if (canStartNewTextUnit()) {
						if (sTagElementType.equals("style")) // DWH 6-13-09
							// DWH 5-27-09 to exclude hidden styles
							sCurrentCharacterStyle = startTag.getAttributeValue("w:styleId");
						else if (sTagElementType.equals("hidden")) // DWH 6-13-09
						// DWH 5-27-09 to exclude hidden styles
						{
							if (!sCurrentCharacterStyle.equals(""))
								excludeStyle(sCurrentCharacterStyle);
						}
						addToDocumentPart(sTagString);
					}
					else
					{
						if (sTagElementType.equals("rstyle")) // DWH 6-13-09 text run style
						// DWH 5-29-09 in a text unit, some styles shouldn't be translated
						{
							sCurrentCharacterStyle = startTag.getAttributeValue("w:val");
							if (tsExcludeWordStyles.contains(sCurrentCharacterStyle))
								bExcludeTextInRun = true;
						}
						else if (sTagElementType.equals("pstyle")) // DWH 6-13-09 text unit style
						// DWH 5-29-09 in a text unit, some styles shouldn't be translated
						{
							sCurrentParagraphStyle = startTag.getAttributeValue("w:val");
							if (tsExcludeWordStyles.contains(sCurrentParagraphStyle))
								bExcludeTextInUnit = true;
						}
						else if (sTagElementType.equals("hidden") && !filterParams.getTranslateWordHidden())
						// DWH 6-13-09 to exclude hidden styles
						{
							if (bInTextRun)
								bExcludeTextInRun = true;
							else
								bExcludeTextInUnit = true;
						}
						if (bInTextRun) // DWH 4-9-09
							addToTextRun(startTag);
						else // DWH 5-7-09
						{
							addToNonTextRun(startTag); // DWH 5-5-09
						}
					}
				} else if (getParseType().equals(ParseType.MSWORD)) {
					if (canStartNewTextUnit()) {
						addToDocumentPart(sTagString);
					}
					else
					{
						if (bInTextRun) // DWH 4-9-09
							addToTextRun(startTag);
						else // DWH 5-7-09
						{
							addToNonTextRun(startTag); // DWH 5-5-09
						}
					}					}
				break;

			case ATTRIBUTES_ONLY:
				// we assume we have already ended any (non-complex) TextUnit in
				// the main while loop above
				List<PropertyTextUnitPlaceholder> propertyTextUnitPlaceholders;

				if (filterParams.getTranslateWordExcludeGraphicMetaData() &&
						(sTagName.equals("wp:docpr") || sTagName.equals("pic:cnvpr")))
				{
					// Don't start new text unit for meta data, e.g.
					// <wp:docPr id="1" name="Textfeld 1"/>

					// The following code is exactly the same as the 'default' behaviour.
					if (canStartNewTextUnit()) {
						addToDocumentPart(sTagString);
					}
					else if (bInTextRun) {
						addToTextRun(startTag);
					}
					else {
						addToNonTextRun(startTag);
					}
					break;
				}

				// Excel: Skip sheet names of hidden sheets if hidden sheets in general should be skipped
				if (!filterParams.getTranslateExcelHidden()
						&& partName.equals("xl/workbook.xml")
						&& sTagName.equals("sheet")
						&& "hidden".equals(startTag.getAttributeValue("state"))) {
					addToDocumentPart(sTagString);
					break;
				}

				if (canStartNewTextUnit()) // DWH 2-14-09 document part just created is part of inline codes
				{
					propertyTextUnitPlaceholders = createPropertyTextUnitPlaceholders(startTag); // 1-29-09
					if (propertyTextUnitPlaceholders != null && !propertyTextUnitPlaceholders.isEmpty()) { // 1-29-09
						startDocumentPart(sTagString, sTagName, propertyTextUnitPlaceholders);
						// DWH 1-29-09
						endDocumentPart();
					} else {
						// no attributes that need processing - just treat as skeleton
						addToDocumentPart(sTagString);
					}
				}
				else {
					propertyTextUnitPlaceholders = createPropertyTextUnitPlaceholders(startTag); // 1-29-09
					if (bInTextRun) // DWH 4-10-09
						addToTextRun(startTag,propertyTextUnitPlaceholders);
					else
						addToNonTextRun(startTag,propertyTextUnitPlaceholders);
				}
				break;
			case GROUP_ELEMENT:
				if (bInSettingsFile) // DWH 4-12-10 else is for <v:textbox ...> in settings.xml file
					addToDocumentPart(sTagString); // DWH 4-12-10 for <v:textbox ...> in settings.xml file
				break;
			case EXCLUDED_ELEMENT:
				getRuleState().pushExcludedRule(sTagName);
				sInExclusion.append(sTagString);
				break;
			case INCLUDED_ELEMENT:
				getRuleState().pushIncludedRule(sTagName);
				addToDocumentPart(sTagString);
				break;
			case TEXT_UNIT_ELEMENT:
				bExcludeTextInUnit = false; // DWH 5-29-09 only exclude text if specific circumstances occur
				addNonTextRunToCurrentTextUnit(); // DWH 5-5-09 trNonTextRun should be null at this point
				bBeforeFirstTextRun = true; // DWH 5-5-09 addNonTextRunToCurrentTextUnit sets it false
				if (startTag.isSyntacticalEmptyElementTag()) {// DWH 3-18-09 in case text unit element is a standalone tag (weird, but Microsoft does it)
					addToDocumentPart(sTagString); // 1-5-09
				}
				else {
					// Don't start the text-unit just yet -- it might be empty.  Note that we
					// can't just save the tag because of how Jericho implements its tag data as
					// pointers into the stream -- if we try to hold a Tag object too long, it
					// will become invalid.
					pendingTagName = startTag.getName();
					pendingTagText = startTag.toString();
				}
				break;
			case TEXT_RUN_ELEMENT: // DWH 4-10-09 smoosh text runs into single <x>text</x>
				bExcludeTextInRun = false; // DWH 5-29-09 only exclude text if specific circumstances occur
				if (canStartNewTextUnit()) // DWH 5-5-09 shouldn't happen
					addToDocumentPart(sTagString);
				else
				{
					addNonTextRunToCurrentTextUnit(); // DWH 5-5-09
					bBeforeFirstTextRun = false; // DWH 5-5-09
					if (bInTextRun)
						bInSubTextRun = true;
					else
					{
						bInTextRun = true;
						bAfterText = false;
						bIgnoredPreRun = false;
						bBetweenTextMarkers = false; // DWH 4-16-09
					}
					addToTextRun(startTag);
				}
				break;
			case TEXT_MARKER_ELEMENT: // DWH 4-14-09 whole case
				if (canStartNewTextUnit()) // DWH 5-5-09 shouldn't happen
					addToDocumentPart(sTagString);
				else
				{
					addNonTextRunToCurrentTextUnit(); // DWH 5-5-09
					if (bInTextRun)
					{
						bBetweenTextMarkers = true;
						addToTextRun(startTag);
					}
					else
						addToNonTextRun(sTagString);
				}
				break;
			case PRESERVE_WHITESPACE:
				getRuleState().pushPreserverWhitespaceRule(sTagName, true);
				addToDocumentPart(sTagString);
				break;
			default:
				if (canStartNewTextUnit()) // DWH 1-14-09 then not currently in text unit; added else
					addToDocumentPart(sTagString); // 1-5-09
				else if (bInTextRun) // DWH 4-10-09
					addToTextRun(startTag);
				else
					addToNonTextRun(startTag); // DWH 5-5-09
		}
	}
	private void startDelayedTextUnit() {
		if (pendingTagName != null) {
			getRuleState().pushTextUnitRule(pendingTagName);
			startTextUnit(new GenericSkeleton(pendingTagText));
			if (configurationType==ParseType.MSEXCEL ||
					configurationType==ParseType.MSWORDDOCPROPERTIES)
			// DWH 4-16-09 Excel and Word Charts don't have text runs or text markers
			{
				bInTextRun = true;
				bBetweenTextMarkers = true;
			}
			else
			{
				bInTextRun = false;
				bBetweenTextMarkers = false;
			}
			pendingTagName = null;
			pendingTagText = null;
		}
	}

	/**
	 * Handles end tags.  These either add to current text runs
	 * or end text runs or text units as appropriate.
	 * @param endTag the end tag to process
	 */
	@Override
	protected void handleEndTag(EndTag endTag) {
		// if in excluded state everything is skeleton including text
		String sTagName; // DWH 2-26-09
		String sTagString; // DWH 4-14-09
		String sTagElementType; // DWH 6-13-09
		if (endTag==null) // DWH 4-14-09
			return;
		sTagName = endTag.getName(); // DWH 2-26-09
		sTagElementType = getConfig().getElementType(endTag); // DWH 6-15-10 endTag was sTagName
		sTagString = endTag.toString(); // DWH 2-26-09
		if (getRuleState().isExludedState()) {
			sInExclusion.append(sTagString);
			// process these tag types to update parser state
			switch (getConfig().getElementRuleTypeCandidate(sTagName)) {
				// DWH 1-23-09
				case EXCLUDED_ELEMENT:
					getRuleState().popExcludedIncludedRule();
					break;
				case INCLUDED_ELEMENT:
					getRuleState().popExcludedIncludedRule();
					break;
				case PRESERVE_WHITESPACE:
					getRuleState().popPreserverWhitespaceRule();
					break;
			}
			if (sTagName.equals("p:text")) {
				bInPowerpointComment = false;
			}
			if (!getRuleState().isExludedState()) { // we just popped the topmost excluded element
				if (canStartNewTextUnit()) // not in a text unit
					addToDocumentPart(sInExclusion.toString()); // 1-5-09
				else if (bInTextRun)
					addToTextRun(sInExclusion.toString());
				else
					addToNonTextRun(sInExclusion.toString());
				sInExclusion = new StringBuilder();

			}
			return;
		}
		switch (getConfig().getElementRuleTypeCandidate(sTagName)) {
			// DWH 1-23-09
			case INLINE_ELEMENT:
				if (canStartNewTextUnit())
				{
					addToDocumentPart(sTagString); // DWH 5-29-09
				}
				else if (bInTextRun) // DWH 5-29-09
					addToTextRun(endTag);
				else if (sTagElementType.equals("delete")) // DWH 5-7-09 6-13-09
				{
					if (trNonTextRun!=null)
						addNonTextRunToCurrentTextUnit();
					addToTextUnitCode(TextFragment.TagType.CLOSING, sTagString, "delete"); // DWH 5-7-09 adds as opening d
				}
				else if (sTagElementType.equals("excell")) // DWH 6-13-09 cell in Excel sheet
				{
					addToDocumentPart(sTagString);
				}
				else
					addToNonTextRun(endTag); // DWH 5-5-09
				break;
			case GROUP_ELEMENT:
				if (!bInSettingsFile)  // DWH 4-12-10 else is for <v:textbox in settings.xml
				{
					addToDocumentPart(sTagString); // this should never happen
				}
				else
				{
					addToDocumentPart(sTagString);  // DWH 4-12-10 for <v:textbox in settings.xml
				}
				break;
			case EXCLUDED_ELEMENT:
				getRuleState().popExcludedIncludedRule();
				addToDocumentPart(sTagString);
				break;
			case INCLUDED_ELEMENT:
				getRuleState().popExcludedIncludedRule();
				addToDocumentPart(sTagString);
				break;
			case TEXT_UNIT_ELEMENT: // $$$
				// If we never started the text unit, flush it all as document part
				if (pendingTagName != null) {
					// XXX Should sanity check that this tag and pending one match
					addToDocumentPart(pendingTagText);
					addToDocumentPart(sTagString);
					pendingTagName = null;
					pendingTagText = null;
				}
				else {
					bExcludeTextInUnit = false; // DWH 5-29-09 only exclude text if specific circumstances occur
					if (bInTextRun)
					{
						addTextRunToCurrentTextUnit(true);
						bInTextRun = false;
					} // otherwise this is an illegal element, so just ignore it
					addNonTextRunToCurrentTextUnit(); // DWH 5-5-09
					bBetweenTextMarkers = true; // DWH 4-16-09 ???
					try
					{
						getRuleState().popTextUnitRule(); // DWH 6-19-10 could die if not in text unit
					}
					catch(Exception e) {}; // will do its best to recover anyway
					endTextUnit(new GenericSkeleton(endpara+sTagString)); // DWH 8-17-09
					endpara = new StringBuilder(); // DWH 8-17-09 for Powerpoint a:endParaRpr
				}
				break;
			case TEXT_RUN_ELEMENT: // DWH 4-10-09 smoosh text runs into single <x>text</x>
				bExcludeTextInRun = false; // DWH 5-29-09 only exclude text if specific circumstances occur
				if (canStartNewTextUnit()) // DWH 5-5-09
					addToDocumentPart(sTagString);
				else
				{
					addToTextRun(endTag);
					if (bInSubTextRun)
						bInSubTextRun = false;
					else if (bInTextRun)
					{
						bInTextRun = false;
					} // otherwise this is an illegal element, so just ignore it
				}
				break;
			case TEXT_MARKER_ELEMENT: // DWH 4-14-09 whole case
				if (canStartNewTextUnit()) // DWH 5-5-09
					addToDocumentPart(sTagString);
				else if (bInTextRun) // DWH 5-5-09 lacked else
				{
					bBetweenTextMarkers = false;
					addToTextRun(endTag);
				}
				else
					addToNonTextRun(sTagString); // DWH 5-5-09
				break;
			case PRESERVE_WHITESPACE:
				getRuleState().popPreserverWhitespaceRule();
				addToDocumentPart(sTagString);
				break;
			default:
				if (canStartNewTextUnit()) // DWH 1-14-09 then not currently in text unit; added else
					addToDocumentPart(sTagString); // not in text unit, so add to skeleton
				else if (bInTextRun) // DWH 4-9-09
					addToTextRun(endTag);
				else
					addToNonTextRun(endTag); // DWH 5-5-09
				break;
		}
	}

	/**
	 * Treats XML comments as DocumentParts.
	 * @param tag comment tag
	 */
	@Override
	protected void handleComment(Tag tag) {
		handleDocumentPart(tag);
	}

	/**
	 * Treats XML doc type declaratons as DocumentParts.
	 * @param tag doc type declaration tag
	 */
	@Override
	protected void handleDocTypeDeclaration(Tag tag) {
		handleDocumentPart(tag);
	}

	/**
	 * Treats XML markup declaratons as DocumentParts.
	 * @param tag markup declaration tag
	 */
	@Override
	protected void handleMarkupDeclaration(Tag tag) {
		handleDocumentPart(tag);
	}

	/**
	 * Treats XML processing instructions as DocumentParts.
	 * @param tag processing instruction tag
	 */
	@Override
	protected void handleProcessingInstruction(Tag tag) {
		handleDocumentPart(tag);
	}

	/**
	 * Treats XML server common tags as DocumentParts.
	 * @param tag server common tag
	 */
	@Override
	protected void handleServerCommon(Tag tag) {
		handleDocumentPart(tag);
	}

	/**
	 * Treats server common escaped tags as DocumentParts.
	 * @param tag server common escaped tag
	 */
	@Override
	protected void handleServerCommonEscaped(Tag tag) {
		handleDocumentPart(tag);
	}

	/**
	 * Treats XML declaratons as DocumentParts.
	 * @param tag XML declaration tag
	 */
	@Override
	protected void handleXmlDeclaration(Tag tag) {
		handleDocumentPart(tag);
	}

	/**
	 * Returns name of the filter.
	 * @return name of the filter
	 */
	public String getName() {
		return "OpenXMLContentFilter";
	}
	/**
	 * Normalizes naming of attributes whose values are the
	 * encoding or a language name, so that they can be
	 * automatically changed to the output encoding and output.
	 * Unfortunately, this hard codes the tags to look for.
	 * @param attrName name of the attribute
	 * @param attrValue, value of the attribute
	 * @param tag tag that contains the attribute
	 * @return a normalized name for the attribute
	 */
	@Override
	protected String normalizeAttributeName(String attrName, String attrValue, Tag tag) {
		// normalize values for HTML
		String normalizedName = attrName;
		String tagName; // DWH 2-19-09 */
// Any attribute that encodes language should be renamed here to "language"
// Any attribute that encodes locale or charset should be normalized too
/*
		// <meta http-equiv="Content-Type"
		// content="text/html; charset=ISO-2022-JP">
		if (isMetaCharset(attrName, attrValue, tag)) {
			normalizedName = HtmlEncoder.NORMALIZED_ENCODING;
			return normalizedName;
		}

		// <meta http-equiv="Content-Language" content="en"
		if (tag.getName().equals("meta") && attrName.equals(HtmlEncoder.CONTENT)) {
			StartTag st = (StartTag) tag;
			if (st.getAttributeValue("http-equiv") != null) {
				if (st.getAttributeValue("http-equiv").equals("Content-Language")) {
					normalizedName = HtmlEncoder.NORMALIZED_LANGUAGE;
					return normalizedName;
				}
			}
		}
*/
		// <w:lang w:val="en-US" ...>
		tagName = tag.getName();
		if (tagName.equals("w:lang") || tagName.equals("w:themefontlang")) // DWH 4-3-09 themeFontLang
		{
			StartTag st = (StartTag) tag;
			if (st.getAttributeValue("w:val") != null)
			{
				normalizedName = Property.LANGUAGE;
				return normalizedName;
			}
		}
		else if (tagName.equals("c:lang")) // DWH 4-3-09
		{
			StartTag st = (StartTag) tag;
			if (st.getAttributeValue("val") != null)
			{
				normalizedName = Property.LANGUAGE;
				return normalizedName;
			}
		}
		else if (tagName.equals("a:endpararpr") || tagName.equals("a:rpr"))
		{
			StartTag st = (StartTag) tag;
			if (st.getAttributeValue("lang") != null)
			{
				normalizedName = Property.LANGUAGE;
				return normalizedName;
			}
		}
		return normalizedName;
	}

	/**
	 * Adds a text string to a sequence of tags that are
	 * not in a text run, that will become a single code.
	 * @param s the text string to add
	 */
	private void addToNonTextRun(String s) // DWH 5-5-09
	{
		if (trNonTextRun==null)
			trNonTextRun = new TextRun();
		trNonTextRun.append(s);
	}
	/**
	 * Adds a tag to a sequence of tags that are
	 * not in a text run, that will become a single code.
	 * @param tag the tag to add
	 */
	private void addToNonTextRun(Tag tag) // DWH 5-5-09
	{
		if (trNonTextRun==null)
			trNonTextRun = new TextRun();
		trNonTextRun.append(tag.toString());
	}
	/**
	 * Adds a tag and codes to a sequence of tags that are
	 * not in a text run, that will become a single code.
	 * @param tag the tag to add
	 * @param propertyTextUnitPlaceholders a list of codes of embedded text
	 */
	private void addToNonTextRun(Tag tag, List<PropertyTextUnitPlaceholder> propertyTextUnitPlaceholders)
	{
		String txt;
		int offset;
		if (trNonTextRun==null)
			trNonTextRun = new TextRun();
		txt=trNonTextRun.getText();
		offset=txt.length();
		trNonTextRun.appendWithPropertyTextUnitPlaceholders(tag.toString(),offset,propertyTextUnitPlaceholders);
	}
	/**
	 * Adds a text string to a text run that will become a single code.
	 * @param s the text string to add
	 */
	private void addToTextRun(String s)
	{
		if (trTextRun==null)
			trTextRun = new TextRun();
		trTextRun.append(s);
	}

	/**
	 * Adds a tag to a text run that will become a single code.
	 * @param tag the tag to add
	 */
	private void addToTextRun(Tag tag) // DWH 4-10-09 adds tag text to string that will be part of larger code later
	{
		// add something here to check if it was bold, italics, etc. to set a property
		if (trTextRun==null)
			trTextRun = new TextRun();
		trTextRun.append(tag.toString());
	}
	/**
	 * Adds a tag and codes to a text run that will become a single code.
	 * @param tag the tag to add
	 * @param propertyTextUnitPlaceholders a list of codes of embedded text
	 */
	private void addToTextRun(Tag tag, List<PropertyTextUnitPlaceholder> propertyTextUnitPlaceholders)
	{
		String txt;
		int offset;
		if (trTextRun==null)
			trTextRun = new TextRun();
		txt=trTextRun.getText();
		offset=txt.length();
		trTextRun.appendWithPropertyTextUnitPlaceholders(tag.toString(),offset,propertyTextUnitPlaceholders);
	}

	/**
	 * Adds the text and codes in a text run as a single code in a text unit.
	 * If it is after text, it is added as a MARKER_CLOSING.  If no text
	 * was encountered and this is being called by an ending TEXT_RUN_ELEMENT
	 * or ending TEXT_UNIT_ELEMENT, it is added as a MARKER_PLACEHOLDER.
	 * Otherwise, it is added as a MARKER_OPENING.
	 * compatible contiguous text runs if desired, and creates a
	 * START_SUBDOCUMENT event
	 * @param bEndRun true if called while processing an end TEXT_RUN_ELEMENT
	 * or end TEXT_UNIT_ELEMENT
	 */
	private void addTextRunToCurrentTextUnit(boolean bEndRun) {
		List<PropertyTextUnitPlaceholder> propertyTextUnitPlaceholders;
		TextFragment.TagType codeType;
		String text;
		int len;
		if (trTextRun!=null && !(text=trTextRun.getText()).equals("")) // DWH 5-14-09 "" can occur with Character entities
		{
			if (bAfterText) {
				// This is extremely ugly, but this whole method is gross and needs to go.
				// Look for codes that terminate run one and start another -- treat as placeholders.
				if ((text.indexOf("</w:t>") != -1 && (text.indexOf("<w:t>") != -1 || text.indexOf("<w:t ") != -1)) ||
						(text.indexOf("</a:t>") != -1 && (text.indexOf("<a:t)") != -1 || text.indexOf("<a:t ") != -1))) {
					codeType = TextFragment.TagType.PLACEHOLDER;
				}
				else {
					codeType = TextFragment.TagType.CLOSING;
				}
			}
			else if (bEndRun) // if no text was encountered and this is the </w:r> or </w:p>, this is a standalone code
				codeType = TextFragment.TagType.PLACEHOLDER;
			else
				codeType = TextFragment.TagType.OPENING;
//			text = trTextRun.getText();
			if (codeType==TextFragment.TagType.OPENING &&
					!bBeforeFirstTextRun && // DWH 4-15-09 only do this if there wasn't stuff before <w:r>
					bInMainFile && // DWH 4-15-08 only do this in MSWORD document and MSPOWERPOINT slides
					((text.equals("<w:r><w:t>") || text.equals("<w:r><w:t xml:space=\"preserve\">")) ||
							(text.equals("<a:r><a:t>") || text.equals("<a:r><a:t xml:space=\"preserve\">"))))
			{
				bIgnoredPreRun = true; // don't put codes around text that has no attributes
				trTextRun = null;
				return;
			}
			else if (codeType==TextFragment.TagType.CLOSING && bIgnoredPreRun)
			{
				bIgnoredPreRun = false;
				if (text.endsWith("</w:t></w:r>") || text.endsWith("</a:t></a:r>"))
				{
					len = text.length();
					if (len>12) // take off the end codes and leave the rest as a placeholder code, if any
					{
						text = text.substring(0,len-12);
						codeType = TextFragment.TagType.CLOSING;
					}
					else
					{
						trTextRun = null;
						return;
					}
				}
			}
			propertyTextUnitPlaceholders = trTextRun.getPropertyTextUnitPlaceholders();
			if (propertyTextUnitPlaceholders != null && !propertyTextUnitPlaceholders.isEmpty()) {
				// add code and process actionable attributes
				addToTextUnitCode(codeType, text, "x", propertyTextUnitPlaceholders);
			} else {
				// no actionable attributes, just add the code as-is
				addToTextUnitCode(codeType, text, "x");
			}
			trTextRun = null;
			bBeforeFirstTextRun = false; // since the text run has now been added to the text unit
		}
	}
	private void addNonTextRunToCurrentTextUnit() { // DWW 5-5-09
		List<PropertyTextUnitPlaceholder> propertyTextUnitPlaceholders;
		TextFragment.TagType codeType;
		String text;
		if (trNonTextRun!=null)
		{
			text = trNonTextRun.getText();
			if (canStartNewTextUnit()) // DWH shouldn't happen
			{
				addToDocumentPart(text);
			}
			propertyTextUnitPlaceholders = trNonTextRun.getPropertyTextUnitPlaceholders();
			if (bBeforeFirstTextRun &&
					(propertyTextUnitPlaceholders==null || propertyTextUnitPlaceholders.size()==0))
			// if a nonTextRun occurs before the first text run, and it doesn't have any
			// embedded text, just add the tags to the skeleton after <w:r> or <a:r>.
			// Since skeleton is not a TextFragment, it can't have embedded text, so if
			// there is embedded text, do the else and make a PLACEHOLDER code
			{
				appendToFirstSkeletonPart(text);
			}
			else
			{
				codeType = TextFragment.TagType.PLACEHOLDER;
				if (propertyTextUnitPlaceholders != null && !propertyTextUnitPlaceholders.isEmpty()) {
					// add code and process actionable attributes
					addToTextUnitCode(codeType, text, "x", propertyTextUnitPlaceholders);
				} else {
					// no actionable attributes, just add the code as-is
					addToTextUnitCode(codeType, text, "x");
				}
			}
			trNonTextRun = null;
		}
	}
	public void excludeStyle(String sTyle) // DWH 5-27-09 to exclude selected styles or hidden text
	{
		if (sTyle!=null && !sTyle.equals(""))
			tsExcludeWordStyles.add(sTyle);
	}
	public ParseType getConfigurationType()
	{
		return configurationType;
	}
	protected void setBInMainFile(boolean bInMainFile) // DWH 4-15-09
	{
		this.bInMainFile = bInMainFile;
	}
	protected boolean getBInMainFile() // DWH 4-15-09
	{
		return bInMainFile;
	}
	protected void setBInSettingsFile(boolean bInSettingsFile) // DWH 4-12-10 for <v:textbox
	{
		this.bInSettingsFile = bInSettingsFile;
	}
	protected boolean getBInSettingsFile() // DWH 4-12-10 for <v:textbox
	{
		return bInSettingsFile;
	}
	public void setTsExcludeWordStyles(TreeSet<String> tsExcludeWordStyles)
	{
		this.tsExcludeWordStyles = tsExcludeWordStyles;
	}
	public TreeSet<String> getTsExcludeWordStyles()
	{
		return tsExcludeWordStyles;
	}

	@Override
	protected TaggedFilterConfiguration getConfig() {
		return config; // this may be bad if AbstractMarkup calls it too soon !!!!
	}

	public IParameters getParameters() { // DWH 7-16-09
		return params;
	}

	public void setParameters(IParameters params) { // DWH 7-16-09
		this.params = (YamlParameters)params;
	}

	public ConditionalParameters getFilterParameters() {
		return filterParams;
	}

	private void addToTextUnitCode(TagType codeType, String data, String type)
	{
		addToTextUnit(new Code(codeType, type, data));
	}
	private void addToTextUnitCode(TagType codeType, String data, String type, List<PropertyTextUnitPlaceholder> propertyTextUnitPlaceholders)
	{
		addToTextUnit(new Code(codeType, type, data), propertyTextUnitPlaceholders);
	}
	@Override
	public String toString() {
		return "OpenXMLContentFilter [" + partName + "]";
	}
}
