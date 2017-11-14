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

package net.sf.okapi.steps.tokenization.common;

import net.sf.okapi.common.annotation.IAnnotation;
import net.sf.okapi.steps.tokenization.tokens.Tokens;

/**
 * 
 * 
 * @version 0.1 08.07.2009
 */

public class TokensAnnotation implements IAnnotation {

	private Tokens tokens = new Tokens();

	public TokensAnnotation (Tokens tokens) {
		super();
		this.tokens = tokens;
	}

	public void setTokens (Tokens tokens) {
		this.tokens = tokens;
	}

	public Tokens getTokens () {
		return tokens;
	} 
	
	public Tokens getFilteredList (String... tokenTypes) {
		if (tokens == null) return getTokens(); // return all
		return tokens.getFilteredList(tokenTypes);
	}

	public void addTokens (Tokens tokens) {
		this.tokens.addAll(tokens);
		// TODO Handle overlapping and duplicate ranges for the same token type
	}
	
	@Override
	public String toString () {
		StringBuilder sb = new StringBuilder();
		for ( Token token : tokens ) {
			if ( sb.length()>0 ) sb.append(" ");
			sb.append(token.toString());
		}
		return sb.toString();
	}
}
