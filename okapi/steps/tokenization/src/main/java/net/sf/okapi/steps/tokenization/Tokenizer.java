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

package net.sf.okapi.steps.tokenization;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.LocaleFilter;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextUnitUtil;
import net.sf.okapi.steps.tokenization.common.TokensAnnotation;
import net.sf.okapi.steps.tokenization.tokens.Tokens;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * @version 0.1 08.07.2009
 */

public class Tokenizer {

	protected static TokenizationStep ts = new TokenizationStep();
	
	/**
	 * Extracts tokens from the given text.
	 * @param text Text to tokenize.
	 * @param language Language of the text.
	 * @param tokenNames Optional list of token names. If omitted, all tokens will be extracted.
	 * @return A list of Token objects.
	 */
	protected static synchronized Tokens tokenizeString(String text, LocaleId language, String... tokenNames) {
			
		Tokens res = new Tokens();
		
		if (ts == null)	return res;
		
		Parameters params = (Parameters) ts.getParameters();		
		params.reset();
		
		params.tokenizeSource = true;
		params.tokenizeTargets = false;
		
		params.setLocaleFilter(LocaleFilter.anyOf(language));		
		params.setTokenNames(tokenNames);
					
		ts.handleEvent(new Event(EventType.START_BATCH)); // Calls component_init();
		
		StartDocument startDoc = new StartDocument("tokenization");
		startDoc.setLocale(language);
		startDoc.setMultilingual(false);		
		Event event = new Event(EventType.START_DOCUMENT, startDoc);		
		ts.handleEvent(event);
				
		ITextUnit tu = TextUnitUtil.buildTU(text);
		event = new Event(EventType.TEXT_UNIT, tu);		
		ts.handleEvent(event);
		
		// Move tokens from the event's annotation to result
		TokensAnnotation ta = TextUnitUtil.getSourceAnnotation(tu, TokensAnnotation.class);
		if (ta != null)
			res.addAll(ta.getTokens());
		
		ts.handleEvent(new Event(EventType.END_BATCH)); // Calls component_done();
		
		return res;
	}
	
	private static Tokens doTokenize(Object text, LocaleId language, String... tokenNames) {
		
		if ( text == null ) return null;
		if ( Util.isNullOrEmpty(language) ) {
			Logger localLogger = LoggerFactory.getLogger(Tokenizer.class);
			localLogger.warn("Language is not set, cannot tokenize.");
			return null;
		}
		
		if (text instanceof ITextUnit) {
			ITextUnit tu = (ITextUnit)text;
			if ( tu.hasTarget(language) )
				return doTokenize(tu.getTarget(language), language, tokenNames);
			else
				return doTokenize(tu.getSource(), language, tokenNames);
		}
		else if (text instanceof TextContainer) {
			TextContainer tc = (TextContainer)text;
			if ( tc.contentIsOneSegment() ) {
				return doTokenize(tc.getFirstContent(), language, tokenNames);
			}
			else {
				return doTokenize(tc.getUnSegmentedContentCopy(), language, tokenNames);
			}
		}
		else if (text instanceof TextFragment) {
			TextFragment tf = (TextFragment)text;
			return doTokenize(TextUnitUtil.getText(tf), language, tokenNames);
		}
		else if (text instanceof String) {
			return tokenizeString((String) text, language, tokenNames);
		}
		
		return null;		
	}
	
	public static Tokens tokenize(ITextUnit textUnit, LocaleId language, String... tokenNames) {
		return doTokenize(textUnit, language, tokenNames);		
	}
	
	public static Tokens tokenize(TextContainer textContainer, LocaleId language, String... tokenNames) {
		return doTokenize(textContainer, language, tokenNames);		
	}
	
	public static Tokens tokenize(TextFragment textFragment, LocaleId language, String... tokenNames) {
		return doTokenize(textFragment, language, tokenNames);		
	}
	
	public static Tokens tokenize(String string, LocaleId language, String... tokenNames) {
		return doTokenize(string, language, tokenNames);		
	}

}
