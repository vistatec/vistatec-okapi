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

package net.sf.okapi.filters.abstractmarkup;

import java.util.List;
import java.util.regex.Pattern;

import net.sf.okapi.common.Util;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.filters.EventBuilder;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filters.InlineCodeFinder;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.TextFragment;

public class AbstractMarkupEventBuilder extends EventBuilder {
	/*
	 * Typical whitespace space (U+0020) tab (U+0009) form feed (U+000C) line feed
	 * (U+000A) carriage return (U+000D) zero-width space (U+200B) (IE6 does not
	 * recognize these, they are treated as unprintable characters)
	 */
	private static final String WHITESPACE_REGEX = "[ \t\r\n\f\u200B]+";
	private static final Pattern WHITESPACE_PATTERN = Pattern.compile(WHITESPACE_REGEX);
		
	private boolean useCodeFinder = false;
	private InlineCodeFinder codeFinder;
	private EncoderManager encoderManager;
	private String encoding;
	private String lineBreak;
	
	public AbstractMarkupEventBuilder(String rootId, IFilter subFilter, 
			EncoderManager encoderManager, String encoding, String lineBreak) {
		super(rootId, subFilter);		
		codeFinder = new InlineCodeFinder();
		this.encoderManager = encoderManager;
		this.encoding = encoding;
		this.lineBreak = lineBreak;
	}
	
	/**
	 * Initializes the code finder. this must be called before the first time using it, for example 
	 * when starting to process the inputs.
	 * @param useCodeFinder true to use the code finder.
	 * @param rules the string representation of the rules.
	 */
	public void initializeCodeFinder (boolean useCodeFinder,
		String rules)
	{
		this.useCodeFinder = useCodeFinder;
		if ( useCodeFinder ) {
			codeFinder.fromString(rules);
			codeFinder.compile();
		}
	}
	
	/**
	 * Initializes the code finder. this must be called before the first time using it, for example 
	 * when starting to process the inputs.
	 * @param useCodeFinder true to use the code finder.
	 * @param rules the string representation of the rules.
	 */
	public void initializeCodeFinder (boolean useCodeFinder,
		List<String> rules)
	{
		this.useCodeFinder = useCodeFinder;
		if ( useCodeFinder ) {
			codeFinder.reset();
			for (String r : rules) {
				codeFinder.addRule(r);
			}
			codeFinder.compile();
		}
	}

	@Override
	protected ITextUnit postProcessTextUnit (ITextUnit textUnit) {
		// We can use getFirstPartContent() because nothing is segmented
		TextFragment text = textUnit.getSource().getFirstContent();
		// Treat the white spaces
		text.setCodedText(normalizeHtmlText(text.getCodedText(), false, textUnit.preserveWhitespaces()));
		// Apply the in-line codes rules if needed
		if ( useCodeFinder ) {
			encoderManager.setDefaultOptions(null, encoding, lineBreak);
			encoderManager.updateEncoder(textUnit.getMimeType());
//			IEncoder encoder = encoderManager.getEncoder();
			codeFinder.process(text);
			// Escape inline code content
//TODO: This must be put back to fix issue 431
// but the encoder is null in several test cases
//			List<Code> codes = text.getCodes();
//			for ( Code code : codes ) {
//				// Escape the data of the new inline code (and only them)
//				if ( code.getType().equals(InlineCodeFinder.TAGTYPE) ) {										
//					//code.setData(encoder.encode(code.getData(), EncoderContext.SKELETON));
//					code.setData(encoder.encode(code.getData(), EncoderContext.INLINE));
//				}
//			}
			
		}
		return textUnit;
	}
	
	public String normalizeHtmlText(String text, boolean insideAttribute, boolean preserveWhitespace) {
		// convert all entities to Unicode
		String decodedValue = text;
		
		if (!preserveWhitespace) {
			decodedValue = collapseWhitespace(decodedValue);
			decodedValue = decodedValue.trim();
		}

		decodedValue = Util.normalizeNewlines(decodedValue);
		return decodedValue;
	}
	
	private String collapseWhitespace(String text) {
		return WHITESPACE_PATTERN.matcher(text).replaceAll(" ");
	}
	
	protected String getEncoding() {
		return encoding;
	}
	
	protected String getLineBreak() {
		return lineBreak;
	}
}
