/*===========================================================================
  Copyright (C) 2009-2010 by the Okapi Framework contributors
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

package net.sf.okapi.filters.html;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.EmptyStackException;

import net.htmlparser.jericho.Attribute;
import net.htmlparser.jericho.EndTag;
import net.htmlparser.jericho.EndTagType;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.StartTag;
import net.htmlparser.jericho.StartTagType;
import net.htmlparser.jericho.Tag;
import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.MimeTypeMapper;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.encoder.HtmlEncoder;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.filters.FilterConfiguration;
import net.sf.okapi.common.filters.PropertyTextUnitPlaceholder;
import net.sf.okapi.common.filters.PropertyTextUnitPlaceholder.PlaceholderAccessType;
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.common.skeleton.ISkeletonWriter;
import net.sf.okapi.filters.abstractmarkup.AbstractMarkupEventBuilder;
import net.sf.okapi.filters.abstractmarkup.AbstractMarkupFilter;
import net.sf.okapi.filters.abstractmarkup.ExtractionRuleState.RuleType;
import net.sf.okapi.filters.abstractmarkup.config.TaggedFilterConfiguration;
import net.sf.okapi.filters.abstractmarkup.config.TaggedFilterConfiguration.RULE_TYPE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@UsingParameters(Parameters.class)
public class HtmlFilter extends AbstractMarkupFilter {

	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	private Parameters parameters;
	private RawDocument tempSourceInput;
	private File tempSourceFile;

	public HtmlFilter() {
		super();
		setMimeType(MimeTypeMapper.HTML_MIME_TYPE);
		setFilterWriter(createFilterWriter());
		setParameters(new Parameters());
		setName("okf_html"); //$NON-NLS-1$
		setDisplayName("HTML/XHTML Filter"); //$NON-NLS-1$
		addConfiguration(new FilterConfiguration(getName(),
				MimeTypeMapper.HTML_MIME_TYPE, getClass().getName(),
				"HTML", "HTML or XHTML documents", //$NON-NLS-1$
				Parameters.NONWELLFORMED_PARAMETERS, ".html;.htm;")); //$NON-NLS-1$
		addConfiguration(new FilterConfiguration(getName() + "-wellFormed",
				MimeTypeMapper.XHTML_MIME_TYPE, getClass().getName(),
				"HTML (Well-Formed)", "XHTML and well-formed HTML documents", //$NON-NLS-1$
				Parameters.WELLFORMED_PARAMETERS));
	}

	@Override
	public ISkeletonWriter createSkeletonWriter() {
		return new HtmlSkeletonWriter();
	}

	@Override
	public void open(RawDocument input, boolean generateSkeleton) {
		// try to detect a document encoding
		String encoding = detectEncoding(input);
		
		setCurrentDocName(input.getInputURI() == null ? "" : input.getInputURI().getPath());
		
		// rewrite tidied file (make sure all attribute values have quotes)
		try {
			tempSourceInput = StreamedSourceCopy.htmlTidiedRewrite(input, isDocumentEncoding(), encoding, isBOM());
			// file handle so we can delete this later
			tempSourceFile = new File(tempSourceInput.getInputURI());
		} catch (IOException e) {
			throw new OkapiIOException("Error generating tidied source temp file", e);
		}
				
		super.open(tempSourceInput, generateSkeleton);
	}
	
	@Override
	public void close() {
		super.close();
		if (tempSourceFile != null) {
			tempSourceFile.delete();
		}
	}

	/**
	 * Initialize rule state and parser. Called before processing of each input.
	 */
	@Override
	protected void startFilter() {
		super.startFilter();
		if (!getConfig().isGlobalPreserveWhitespace()) {
			LOGGER.debug(
					"By default the HTML filter will collapse whitespace unless overridden in the configuration"); //$NON-NLS-1$
		}
		getEventBuilder().initializeCodeFinder(getConfig().isUseCodeFinder(),
				getConfig().getCodeFinderRules());
	}
	
	/**
	 * End the current filter processing and send the {@link Ending} {@link Event}
	 */
	@Override
	protected void endFilter() {
		super.endFilter();
		
		// delete temp source file
		if (tempSourceInput != null) {
			tempSourceInput.close();
			boolean success = (new File(tempSourceInput.getInputURI())).delete();
			if (!success) {
				LOGGER.warn("Couldn't delete HTML Filter tidied temp file");
			}
		}
	}

	@Override
	protected void preProcess(Segment segment) {
		super.preProcess(segment);

		// let the handlers deal with wellformed content
		if (getConfig().isWellformed()) {
			return;
		}

		// otherwise we can't assume a valid end tag and we must close any
		// TextUnits when we see a non inline tag
		if (segment instanceof Tag) {
			// We just hit a tag that could close the current TextUnit
			final Tag tag = (Tag) segment;
			boolean inlineTag = false;
			if (getConfig().getElementRuleTypeCandidate(tag.getName()) == RULE_TYPE.INLINE_ELEMENT
					|| getConfig().getElementRuleTypeCandidate(tag.getName()) == RULE_TYPE.INLINE_EXCLUDED_ELEMENT
					|| (getEventBuilder().isInsideTextRun() && (tag
							.getTagType() == StartTagType.COMMENT || tag
							.getTagType() == StartTagType.XML_PROCESSING_INSTRUCTION))
					|| (getConfig().isInlineCdata() && tag.getName().equals("![cdata["))) {
				inlineTag = true;
			}

			// if its an inline code let the handlers deal with it
			if (getEventBuilder().isCurrentTextUnit() && !inlineTag) {
				getEventBuilder().endTextUnit();
			}
		}
	}

	@Override
	/**
	 * Overridden to support non-wellformed (unbalanced tag exceptions in HTML)
	 */
	protected RULE_TYPE updateEndTagRuleState(EndTag endTag) {
		RULE_TYPE ruleType = getConfig().getElementRuleTypeCandidate(endTag.getName());
		RuleType currentState = null;

		switch (ruleType) {
		case INLINE_EXCLUDED_ELEMENT:
		case INLINE_ELEMENT:
			try {
				currentState = getRuleState().peekInlineRule();
				if (currentState.ruleName.equalsIgnoreCase(endTag.getName())) {
					currentState = getRuleState().popInlineRule();
					ruleType = currentState.ruleType;
				} else {				
					// must be a overlapping tags of some sort
					// treat it as an inline anyway but don't pop stack
					ruleType = RULE_TYPE.INLINE_ELEMENT;
				}
			} catch (EmptyStackException e) {
				// empty stack means the inline tags are not wellformed.
				// assume the tag is a valid inline tag - even if
				// it doesn't have a mate
				ruleType = RULE_TYPE.INLINE_ELEMENT;
			}
			break;
		case ATTRIBUTES_ONLY:
			// TODO: add a rule state for ATTRIBUTE_ONLY rules
			break;
		case GROUP_ELEMENT:
			try {
				currentState = getRuleState().popGroupRule();
				ruleType = currentState.ruleType;
			} catch (EmptyStackException e) {
				// treat as skeleton
				ruleType = RULE_TYPE.RULE_NOT_FOUND;
			}
			break;
		case EXCLUDED_ELEMENT:
			// must be wellformed - don't check for EmptyStackException
			try {
				currentState = getRuleState().popExcludedIncludedRule();
				ruleType = currentState.ruleType;
			} catch (EmptyStackException e) {
				// treat as skeleton
				ruleType = RULE_TYPE.RULE_NOT_FOUND;
			}
			break;
		case INCLUDED_ELEMENT:
			try {
				currentState = getRuleState().popExcludedIncludedRule();
				ruleType = currentState.ruleType;
			} catch (EmptyStackException e) {
				// treat as skeleton
				// treat as skeleton
				ruleType = RULE_TYPE.RULE_NOT_FOUND;
			}
			break;
		case TEXT_UNIT_ELEMENT:
			try {
				currentState = getRuleState().popTextUnitRule();
				ruleType = currentState.ruleType;
			} catch (EmptyStackException e) {
				// treat as skeleton
				ruleType = RULE_TYPE.RULE_NOT_FOUND;
			}
			break;
		default:
			break;
		}

		if (currentState != null) {
			// if the end tag doesn't match with what is on the stack then
			// assume the default (non-conditional) rule
			if (!currentState.ruleName.equalsIgnoreCase(endTag.getName())) {
				String character = Integer.toString(endTag.getBegin());
				LOGGER.debug("End tag {} and start tag {} do not match at character number {}",
						endTag.getName(), currentState.ruleName, character);
			}
		}

		return ruleType;
	}

	@Override
	protected PropertyTextUnitPlaceholder createPropertyTextUnitPlaceholder(
			PlaceholderAccessType type, String name, String value, Tag tag,
			Attribute attribute) {

		String normalizeAttributeName = normalizeAttributeName(name, value, tag);

		// Test for charset in meta tag - we need to isolate the position of
		// charset within the attribute value
		// i.e., content="text/html; charset=ISO-2022-JP"
		if (isMetaCharset(name, value, tag)
				&& value.toLowerCase().indexOf("charset=") != -1) { //$NON-NLS-1$
			// offset of attribute
			int mainStartPos = attribute.getBegin() - tag.getBegin();
			int mainEndPos = attribute.getEnd() - tag.getBegin();

			// adjust offset of value of the attribute
			int charsetValueOffset = value.toLowerCase()
					.lastIndexOf("charset=") + "charset=".length(); //$NON-NLS-1$

			int valueStartPos = (attribute.getValueSegment().getBegin() + charsetValueOffset)
					- tag.getBegin();
			int valueEndPos = attribute.getValueSegment().getEnd()
					- tag.getBegin();

			// get the charset value (encoding)
			String v = tag.toString().substring(valueStartPos, valueEndPos);
			return new PropertyTextUnitPlaceholder(type,
					normalizeAttributeName, v, mainStartPos, mainEndPos,
					valueStartPos, valueEndPos);
		}

		// name is normalized in super-class
		return super.createPropertyTextUnitPlaceholder(
				type,
				name,
				getEventBuilder().normalizeHtmlText(value, true,
						isPreserveWhitespace()), tag, attribute);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sf.okapi.common.markupfilter.BaseMarkupFilter#normalizeName(java.
	 * lang.String)
	 */
	@Override
	protected String normalizeAttributeName(String attrName, String attrValue,
			Tag tag) {
		// normalize values for HTML
		String normalizedName = attrName;

		// <meta http-equiv="Content-Type"
		// content="text/html; charset=ISO-2022-JP">
		if (isMetaCharset(attrName, attrValue, tag)) {
			normalizedName = Property.ENCODING;
			return normalizedName;
		}

		// <meta charset="ISO-2022-JP">
		if (tag.getName().equalsIgnoreCase("meta") && 
				attrName.equalsIgnoreCase(HtmlEncoder.CHARSET)) {
			normalizedName = Property.ENCODING;
			return normalizedName;
		}

		// <meta http-equiv="Content-Language" content="en"
		if (tag.getName().equalsIgnoreCase("meta")
				&& attrName.equalsIgnoreCase(HtmlEncoder.CONTENT)) {
			StartTag st = (StartTag) tag;
			if (st.getAttributeValue("http-equiv") != null) {
				if (st.getAttributeValue("http-equiv").equalsIgnoreCase(
						"Content-Language")) {
					normalizedName = Property.LANGUAGE;
					return normalizedName;
				}
			}
		}
		
		// <x lang="en"> or <x xml:lang="en">
		if (attrName.equalsIgnoreCase("lang")
				|| attrName.equalsIgnoreCase("xml:lang")) {
			normalizedName = Property.LANGUAGE;
			return normalizedName;
		}

		return normalizedName;
	}

	private boolean isMetaCharset(String attrName, String attrValue, Tag tag) {
		if (tag.getName().equalsIgnoreCase("meta")
				&& attrName.equalsIgnoreCase(HtmlEncoder.CONTENT)) {
			StartTag st = (StartTag) tag;
			if (st.getAttributeValue("http-equiv") != null
					&& st.getAttributeValue("content") != null) {
				if (st.getAttributeValue("http-equiv").equalsIgnoreCase(
						"Content-Type")
						&& st.getAttributeValue("content").toLowerCase()
								.contains("charset=")) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	protected TaggedFilterConfiguration getConfig() {
		return parameters.getTaggedConfig();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sf.okapi.common.filters.IFilter#setParameters(net.sf.okapi.common
	 * .IParameters)
	 */
	public void setParameters(IParameters params) {
		this.parameters = (Parameters) params;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.okapi.common.filters.IFilter#getParameters()
	 */
	public IParameters getParameters() {
		return parameters;
	}

	/**
	 * Initialize filter parameters from a URL.
	 * 
	 * @param config
	 */
	public void setParametersFromURL(URL config) {
		parameters = new Parameters(config);
	}

	/**
	 * Initialize filter parameters from a Java File.
	 * 
	 * @param config
	 */
	public void setParametersFromFile(File config) {
		parameters = new Parameters(config);
	}

	/**
	 * Initialize filter parameters from a String.
	 * 
	 * @param config
	 */
	public void setParametersFromString(String config) {
		parameters = new Parameters(config);
	}

	/**
	 * @return the {@link AbstractMarkupEventBuilder}
	 */
	public AbstractMarkupEventBuilder getEventBuilder() {
		return (AbstractMarkupEventBuilder) super.getEventBuilder();
	}

	@Override
	protected TagType determineTagType(Tag tag) {
		TextFragment.TagType codeType;
		
		// start tag or empty tag
		if (tag.getTagType() == StartTagType.NORMAL
				|| tag.getTagType() == StartTagType.UNREGISTERED) {
			StartTag startTag = ((StartTag) tag);

			// is this an empty tag?
			if (startTag.isSyntacticalEmptyElementTag()) {
				codeType = TextFragment.TagType.PLACEHOLDER;
			} else if (startTag.isEndTagRequired()) {
				if (getRuleState().isInlineExcludedState()) {
					codeType = TextFragment.TagType.PLACEHOLDER;
				} else {
					codeType = TextFragment.TagType.OPENING;
				}
			} else {
				if (getConfig().isWellformed()) {
					codeType = TextFragment.TagType.OPENING;
				} else {
					codeType = TextFragment.TagType.PLACEHOLDER;
				}
			}
		} else { // end or unknown tag
			if (tag.getTagType() == EndTagType.NORMAL
					|| tag.getTagType() == EndTagType.UNREGISTERED) {
				codeType = TextFragment.TagType.CLOSING;
			} else {
				codeType = TextFragment.TagType.PLACEHOLDER;
			}
		}
		
		return codeType;
	}
}
