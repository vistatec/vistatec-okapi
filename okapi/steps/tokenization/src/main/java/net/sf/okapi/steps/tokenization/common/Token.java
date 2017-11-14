/*===========================================================================
  Copyright (C) 2008-2009 by the Okapi Framework contributors
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

import net.sf.okapi.common.Range;
import net.sf.okapi.steps.tokenization.tokens.Tokens;

/**
 * 
 * 
 * @version 0.1 08.07.2009
 */

public class Token {
	
	/**
	 * Integer identifier of the token. 
	 * !!! Non-serializable. 
	 */
	private int tokenId; 
	
	/**
	 * Underlying lexem extracted by a lexer.
	 */
	private Lexem lexem;
	
	/**
	 * Percentage reflecting trustworthiness of the token recognition.
	 */
	private int score;
	
	public Token(int tokenId, Lexem lexem, int score) {
		
		super();
		
		this.tokenId = tokenId;
		this.lexem = lexem;
		setScore(score);
	}

	/**
	 * Gets integer identifier of the token. 
	 * !!! Non-serializable. 
	 */
	public int getTokenId() {
		
		return tokenId;
	}

	public Lexem getLexem() {
		
		return lexem;
	}

	public Range getRange() {
		
		if (lexem == null) return new Range(0, 0);
		
		return lexem.getRange();
	}

	public int getScore() {
		
		return score;
	}

	public int getLexerId() {
		
		if (lexem == null) return 0;
		
		return lexem.getLexerId();
	}

	public int getLexemId() {
		
		if (lexem == null) return 0;
		
		return lexem.getId();
	}
	
	public String getValue() {
		
		if (lexem == null) return "";
		
		return lexem.getValue();
	}

	public String getName() {
		
		return Tokens.getTokenName(tokenId);
	}
	
	public String getDescription() {
				
		return Tokens.getTokenDescription(tokenId);
	}

	@Override
	public String toString() {
		
		return String.format("%-15s\t%d\t%3d%%\t%s", 
				getName(), tokenId, score, lexem.toString());
	}

	public void setScore(int score) {
		
		if (score < 0)
			score = 0;
		
		if (score > 100)
			score = 100;
		
		this.score = score;
	}	
	
	public boolean isImmutable() {
		
		if (lexem == null) return false;
		
		return lexem.isImmutable();
	}

	public void setImmutable(boolean immutable) {
		
		if (lexem == null) return;

		lexem.setImmutable(immutable);
	}

	public void setDeleted(boolean deleted) {
		
		if (lexem == null) return;

		lexem.setDeleted(deleted);
	}

	public boolean isDeleted() {
		
		if (lexem == null) return false;
		
		return lexem.isDeleted();
		//return this.score == 0;		
	}

	public void delete() {
		
		setDeleted(true);
		//this.score = 0;
	}
	
	public void undelete() {
	
		setDeleted(false);
	}
}
