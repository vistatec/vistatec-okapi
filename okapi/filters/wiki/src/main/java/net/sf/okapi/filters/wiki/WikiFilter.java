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
===========================================================================*/

package net.sf.okapi.filters.wiki;

import static net.sf.okapi.filters.wiki.WikiPatterns.BOUNDARY;
import static net.sf.okapi.filters.wiki.WikiPatterns.TABLE_CELL_PATTERN;
import static net.sf.okapi.filters.wiki.WikiPatterns.TABLE_START_PATTERN;
import static net.sf.okapi.filters.wiki.WikiPatterns.TEMP_EXTRACT_PATTERN;
import static net.sf.okapi.filters.wiki.WikiPatterns.TEMP_PLACEHOLDER;
import static net.sf.okapi.filters.wiki.WikiPatterns.WHITESPACE_PATTERN;
import static net.sf.okapi.filters.wiki.WikiPatterns.blockDelimiters;
import static net.sf.okapi.filters.wiki.WikiPatterns.endCodes;
import static net.sf.okapi.filters.wiki.WikiPatterns.inlineDelimiters;
import static net.sf.okapi.filters.wiki.WikiPatterns.noWiki;
import static net.sf.okapi.filters.wiki.WikiPatterns.properties;
import static net.sf.okapi.filters.wiki.WikiPatterns.startCodes;
import static net.sf.okapi.filters.wiki.WikiPatterns.untranslatable;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.okapi.common.BOMNewlineEncodingDetector;
import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.filters.AbstractFilter;
import net.sf.okapi.common.filters.FilterConfiguration;
import net.sf.okapi.common.filters.FilterUtil;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filters.PropertyTextUnitPlaceholder;
import net.sf.okapi.common.filters.PropertyTextUnitPlaceholder.PlaceholderAccessType;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.filters.doxygen.DelimiterTokenizer;
import net.sf.okapi.filters.doxygen.PrefixSuffixTokenizer;
import net.sf.okapi.filters.doxygen.WhitespaceAdjustingEventBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link IFilter} for a Wiki markup file
 * 
 * @author Aaron Madlon-Kay
 */
@UsingParameters(Parameters.class)
public class WikiFilter extends AbstractFilter
{
	public static final String WIKI_MIME_TYPE = "text/x-wiki-txt";
	
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	
	private String linebreak = "\n";
	private WhitespaceAdjustingEventBuilder eventBuilder;
	private EncoderManager encoderManager;
	private boolean hasUtf8Bom;
	private boolean hasUtf8Encoding;
	private RawDocument currentRawDocument;
	private BOMNewlineEncodingDetector detector;
	private Parameters params;
	
	private PrefixSuffixTokenizer commentTokenizer;
	private LinkedList<String> extracted;
	
	public WikiFilter()
	{
		super();

		setMimeType(WIKI_MIME_TYPE);
		setMultilingual(false);
		setFilterWriter(new WikiWriter());	
		// Cannot use '_' or '-' in name: conflicts with other filters (e.g. plaintext, table)
		// for defining different configurations
		setName("okf_wiki"); //$NON-NLS-1$
		setDisplayName("Wiki Filter"); //$NON-NLS-1$
		addConfiguration(new FilterConfiguration(
				getName(),
				WIKI_MIME_TYPE,
				getClass().getName(),
				"Wiki Markup",
				"Text with wiki-style markup",
				Parameters.WIKI_PARAMETERS,
				".txt;"));
		setParameters(new Parameters());
	}

	@Override
	public IFilterWriter createFilterWriter()
	{
		return super.createFilterWriter();
	}

	@Override
	public void open(RawDocument input)
	{
		open(input, true);
	}

	@Override
	public void open(RawDocument input, boolean generateSkeleton)
	{
		this.currentRawDocument = input;
		
		if (input.getInputURI() != null)
			setDocumentName(input.getInputURI().getPath());

		detector = new BOMNewlineEncodingDetector(input.getStream(), input.getEncoding());
		detector.detectAndRemoveBom();

		setEncoding(input.getEncoding());
		hasUtf8Bom = detector.hasUtf8Bom();
		hasUtf8Encoding = detector.hasUtf8Encoding();
		linebreak = detector.getNewlineType().toString();
		setNewlineType(linebreak);

		// set encoding to the user setting
		String detectedEncoding = getEncoding();

		// may need to override encoding based on what we detect
		if (detector.isDefinitive()) {
			detectedEncoding = detector.getEncoding();
			LOGGER.debug(String.format(
					"Overridding user set encoding (if any). Setting auto-detected encoding (%s).",
					detectedEncoding));
		} else if (!detector.isDefinitive() && getEncoding().equals(RawDocument.UNKOWN_ENCODING)) {
			detectedEncoding = detector.getEncoding();
			LOGGER.debug(String.format(
					"Default encoding and detected encoding not found. Using best guess encoding (%s)",
					detectedEncoding));
		}

		input.setEncoding(detectedEncoding);
		setEncoding(detectedEncoding);
		setOptions(input.getSourceLocale(), input.getTargetLocale(), detectedEncoding,
				generateSkeleton);
		
		// Read in entire file right now
		BufferedReader reader = new BufferedReader(input.getReader());
		StringBuilder builder = new StringBuilder();
		try {
			for (String line = reader.readLine(); line != null; line = reader.readLine())
				builder.append(line + linebreak);
		} catch (IOException e) {
			throw new OkapiIOException("IO error reading wiki markup file", e);
		}
		try {
			reader.close();
		} catch (IOException e) {
			LOGGER.warn("Error closing the wiki text buffered reader.", e);
		}
		
		DelimiterTokenizer tokenizer = new DelimiterTokenizer(TEMP_EXTRACT_PATTERN, builder.toString());
		builder = new StringBuilder();
		extracted = new LinkedList<String>();
		
		for (DelimiterTokenizer.Token t : tokenizer) {
			if (t.delimiter() != null) {
				extracted.add(t.delimiter());
				builder.append(TEMP_PLACEHOLDER);
			}
			builder.append(t.toString());
		}
		
		commentTokenizer = new PrefixSuffixTokenizer(blockDelimiters, builder.toString());
		
		// create EventBuilder with document name as rootId
		if (eventBuilder == null) {
			eventBuilder = new WhitespaceAdjustingEventBuilder();
		} else {
			eventBuilder.reset(null, this);
		}
		
		eventBuilder.addFilterEvent(createStartFilterEvent());
		eventBuilder.setPreserveWhitespace(params.isPreserveWhitespace());
		
		// load simplifier rules and send as an event
		if (!Util.isEmpty(params.getSimplifierRules())) {			
			Event cs = FilterUtil.createCodeSimplifierEvent(((Parameters)getParameters()).getSimplifierRules());
			eventBuilder.addFilterEvent(cs);
		}	
		
		for (Entry<Pattern, Pattern> e : params.getCustomCodePatterns().entrySet()) {
			inlineDelimiters.put(e.getKey(),
					e.getValue() == null ? Pattern.compile(BOUNDARY) : e.getValue());
			if (e.getValue() != null) {
				startCodes.put(e.getKey(), null);
				endCodes.put(e.getValue(), null);
			}
		}
	}

	@Override
	public void close()
	{
		if (currentRawDocument != null) {
			currentRawDocument.close();
		}
	}

	@Override
	public EncoderManager getEncoderManager()
	{
		if (encoderManager == null) {
			encoderManager = new EncoderManager();
			encoderManager.setMapping(WIKI_MIME_TYPE,
					"net.sf.okapi.common.encoder.DefaultEncoder");
		}
		return encoderManager;
	}

	@Override
	public IParameters getParameters()
	{
		return params;
	}

	@Override
	public void setParameters(IParameters params)
	{
		this.params = (Parameters) params;
	}

	@Override
	public boolean hasNext()
	{
		return eventBuilder.hasNext();
	}

	
	@Override
	public Event next()
	{		
		// process queued up events before we produce more
		while (eventBuilder.hasQueuedEvents())
			return eventBuilder.next();

		parse();
		
		return eventBuilder.next();
	}
	
	/**
	 * Parse the input file.
	 * 
	 * Top-level parse function; called by next().
	 * Iterates over the input file one line at a time.
	 */
	private void parse()
	{
		
		while (commentTokenizer.iterator().hasNext()) {
			
			if (isCanceled()) return;
			
			parseBlocks(commentTokenizer);
			
			if (eventBuilder.hasQueuedEvents()) return;
		}
		
		// Reached the end of the file.
		assert(extracted.isEmpty());
		
		// Close last unfinished TU and group, if present.
		if (eventBuilder.isCurrentTextUnit()) eventBuilder.endTextUnit();
		if (eventBuilder.isCurrentGroup()) eventBuilder.endGroup(null);
		
		eventBuilder.flushRemainingTempEvents();
		
		eventBuilder.addFilterEvent(createEndFilterEvent());
	}
	
	private void parseBlocks(PrefixSuffixTokenizer tokenizer)
	{

		for (PrefixSuffixTokenizer.Token t : tokenizer) {
		
			// First token (before any structure)
			if (t.prefixPattern() == null) {
				
				parseTextUnits(new DelimiterTokenizer(WHITESPACE_PATTERN, t.toString()));
			
			// Special case with table
			} else if (t.prefixPattern() == TABLE_START_PATTERN) {
				
				parseSkeleton(t.prefix());
				parseTextUnits(new DelimiterTokenizer(TABLE_CELL_PATTERN, t.toString()));
				
			} else if (isTranslatable(t)) {
				
				parseSkeleton(t.prefix());
				parseBlocks(new PrefixSuffixTokenizer(blockDelimiters, t.toString()));
			
			} else {
				parseSkeleton(t.prefix() + t.toString());
			}
		}
	}
	
	private void parseTextUnits(DelimiterTokenizer tokenizer)
	{
		for (DelimiterTokenizer.Token t : tokenizer) {
			
			if (t.delimiter() != null) parseSkeleton(t.delimiter());
			
			String text = t.toString();
			
			if (text.length() == 0) continue;
			
			if (text.trim().length() == 0) {
				parseSkeleton(text);
				continue;
			}
			
			eventBuilder.startTextUnit();
			parseInlineCodes(
				new PrefixSuffixTokenizer(inlineDelimiters, replaceExtracted(t.toString())),
				true);
			eventBuilder.endTextUnit();
		}	
	}
	
	private void parseInlineCodes(PrefixSuffixTokenizer tokenizer, boolean enabled)
	{
		for (PrefixSuffixTokenizer.Token t : tokenizer) {
			
			if (t.prefix() == null) {
				
				eventBuilder.addToTextUnit(t.toString());
			
			} else {
				
				if (enabled || shouldReenable(t)) {
					if (t.prefix().length() > 0) parseCode(t);
					enabled = shouldEnable(t);
				} else {
					eventBuilder.addToTextUnit(t.prefix());
				}
				
				parseInlineCodes(new PrefixSuffixTokenizer(inlineDelimiters, t.toString()), enabled);
			}
		}
	}
	
	// TODO: Support other, non-translatable properties like links, etc.
	private void parseCode(PrefixSuffixTokenizer.Token token) {
		
		Code code = new Code(getTagType(token), null, token.prefix());
		
		Pattern propPattern = properties.get(token.prefixPattern());
		if (propPattern != null) {
			
			Matcher m = propPattern.matcher(token.prefix());
			if (m.find()) {
				
				PropertyTextUnitPlaceholder p = new PropertyTextUnitPlaceholder(
						PlaceholderAccessType.TRANSLATABLE, "", m.group(), m.start(), m.end());
				
				ArrayList<PropertyTextUnitPlaceholder> ps = new ArrayList<PropertyTextUnitPlaceholder>();
				ps.add(p);
				
				eventBuilder.addToTextUnit(code, true, ps);
				return;
			}
		}
		
		eventBuilder.addToTextUnit(code);
	}

	private void parseSkeleton(String string)
	{
		eventBuilder.addDocumentPart(replaceExtracted(string));
	}
	
	private String replaceExtracted(String string)
	{
		while (string.contains(TEMP_PLACEHOLDER))
			string = string.replaceFirst(TEMP_PLACEHOLDER,
					Matcher.quoteReplacement(extracted.pop()));
		
		return string;
	}
	
	private boolean isTranslatable(PrefixSuffixTokenizer.Token t)
	{
		return !untranslatable.containsKey(t.prefixPattern());
	}
	
	private boolean shouldReenable(PrefixSuffixTokenizer.Token t)
	{
		return noWiki.containsValue(t.prefixPattern());
	}
	
	private boolean shouldEnable(PrefixSuffixTokenizer.Token t)
	{
		return !noWiki.containsKey(t.prefixPattern());
	}
	
	private TagType getTagType(PrefixSuffixTokenizer.Token t)
	{
		if (startCodes.containsKey(t.prefixPattern())) {
			return TagType.OPENING;
		} else if (endCodes.containsKey(t.prefixPattern())) {
			return TagType.CLOSING;
		} else {
			return TagType.PLACEHOLDER;
		}
	}

	@Override
	protected boolean isUtf8Bom()
	{
		return hasUtf8Bom;
	}

	@Override
	protected boolean isUtf8Encoding()
	{
		return hasUtf8Encoding;
	}

}
