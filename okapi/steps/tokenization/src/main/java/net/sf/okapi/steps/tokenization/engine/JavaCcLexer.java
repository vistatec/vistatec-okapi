/*===========================================================================
  Copyright (C) 2009 by the Okapi Framework contributors
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

package net.sf.okapi.steps.tokenization.engine;

import java.io.IOException;
import java.io.StringReader;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.steps.tokenization.common.AbstractLexer;
import net.sf.okapi.steps.tokenization.common.Lexem;
import net.sf.okapi.steps.tokenization.common.Lexems;
import net.sf.okapi.steps.tokenization.engine.javacc.ParseException;
import net.sf.okapi.steps.tokenization.engine.javacc.SimpleCharStream;
import net.sf.okapi.steps.tokenization.engine.javacc.TokenMgrError;
import net.sf.okapi.steps.tokenization.engine.javacc.WordTokenizer;
import net.sf.okapi.steps.tokenization.engine.javacc.WordTokenizerTokenManager;
import net.sf.okapi.steps.tokenization.tokens.Tokens;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaCcLexer extends AbstractLexer {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private SimpleCharStream stream;
	private WordTokenizer tokenizer;
	private boolean hasNext;
	
	@Override
	protected boolean lexer_hasNext() {

		return hasNext;
	}

	@Override
	protected void lexer_init() {
		
		hasNext = false;
	}

	@Override
	protected Lexem lexer_next() {
	
		net.sf.okapi.steps.tokenization.engine.javacc.Token token = null;
		
		try {
			token = tokenizer.nextToken();
			
		} catch (TokenMgrError e) {
			
			logger.debug("JavaCC error: {}", e.getMessage());
			return null;
			
		} catch (Error e) {
		
			logger.debug("JavaCC error: {}", e.getMessage());
			return null;
		
		} catch (ParseException e) {

			logger.debug("JavaCC parsing exception: {}", e.getMessage());
			return null;
			
		} catch (IOException e) {

			logger.debug("JavaCC IO exception: {}", e.getMessage());
			return null;
		}

		if (token == null) {
			
			hasNext = false;
			return null;
		}
		
		int end = stream.bufpos + 1;
		int start = end - token.image.length();
		
		if (start < 0) return null;
		if (start > end) return null;
		
		int lexemId = token.kind;
		Lexem lexem = new Lexem(lexemId, token.image, start, end);
		
		return lexem;
	}

	@Override
	protected void lexer_open (String text, LocaleId language, Tokens tokens) {
		
		StringReader sr = new StringReader(text);
		stream = new SimpleCharStream(sr);
		WordTokenizerTokenManager tm = new WordTokenizerTokenManager(stream);
		
		tokenizer = new WordTokenizer(tm);
		hasNext = true;
	}

	public Lexems process(String text, LocaleId language, Tokens tokens) {
		return null;
	}

}
