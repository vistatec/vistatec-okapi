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

package net.sf.okapi.steps.tokenization.common;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.steps.tokenization.tokens.Tokens;

/**
 * Common set of methods to extract lexems from a string or a list of tokens.
 * 
 * <p>There are 2 ways of extraction provided: iterator and a single call. The implementing class can implement either of the 2 methods.
 * The iterator way is normally more memory-effective, though the single call can be desirable when an existing lexer/tokenizer object is
 * being wrapped around by this interface.
 * 
 * <p>Lexers, implementing this interface, can do the following things:
 * <li> split up a string into lexems
 * <li> split up longer lexems on a token list, providing further tokenization of existing tokens
 * <li> merge shorter lexems on a token list, providing further tokenization of existing tokens.
 * <pre>
 * </pre>
 * Example of iterator:
 * <pre>
 * ILexer lexer; 
 * Tokens tokens = new Tokens();
 * 
 * lexer.open("Tokenize me!", tokens);
 * 
 * while (lexer.hasNext())
 *     Lexem lexem = lexer.next();
 *     
 * lexer.close;
 *  </pre>
 * Example of a single call:
 * <pre>
 * ILexer lexer; 
 * Tokens tokens = new Tokens();
 * 
 * Lexems lexems = lexer.process("Tokenize me!", tokens);
 *  </pre>
 *  
**/
public interface ILexer {

	/**
	 * Initializes the lexer.
	 */
	void init();
	
//	/**
//	 * Assigns an ID to the lexer. !!! Non-serializable. 
//	 * @param lexerId 
//	 */
//	void setLexerId(int lexerId);
//	
//	/**
//	 * Gets the previously assigned lexer ID. !!! Non-serializable.
//	 * @return The lexer ID. 
//	 */
//	int getLexerId();
//			
	/**
	 * Gets the current rules for this lexer.
	 * @return The current rules for this lexer
	 */
	LexerRules getRules();

	/**
	 * Sets new rules for this lexer.
	 * @param rules The new rules to use
	 */
	void setRules(LexerRules rules);

	/**
	 * Starts processing a string or a list of tokens, extracting lexems from them.
	 * @param text The string to be processed
	 * @param language The language of the text
	 * @param tokens The string to be processed
	 */
	void open(String text, LocaleId language, Tokens tokens);
	
	/**
	 * Alternative non-iterator way of extracting lexems. In opposite to open()-hasNext()-next()-close(), 
	 * all extraction is done by a single method call. Implementations might be less memory-effective compared to the iterator. 
	 * @param text The string to be processed
	 * @param language The language of the text
	 * @param tokens The string to be processed
	 * @return A list of extracted lexems
	 */
	Lexems process(String text, LocaleId language, Tokens tokens);
	
	/**
	 * Indicates if there is a lexem extracted.
	 * @return True if there is at least one lexem has been extracted, false if none
	 */
	boolean hasNext();
	
	/**
	 * Gets the next lexem available.
	 * @return The next lexem available or null if there are no events
	 */
	Lexem next();
	
	/**
	 * Called after the lexer is done with extracting lexems.
	 */
	void close ();
	
	/**
	 * Cancels the current process.
	 */
	void cancel ();
		
}
