/*===========================================================================
  Copyright (C) 2008-2010 by the Okapi Framework contributors
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

package net.sf.okapi.lib.beans.v1;

import net.sf.okapi.lib.persistence.IPersistenceSession;
import net.sf.okapi.lib.persistence.PersistenceBean;
import net.sf.okapi.steps.tokenization.common.Lexem;
import net.sf.okapi.steps.tokenization.common.Token;

public class TokenBean extends PersistenceBean<Token> {

	private int tokenId; 
	private LexemBean lexem = new LexemBean();
	private int score;
	
	@Override
	protected Token createObject(IPersistenceSession session) {
		return new Token(tokenId, lexem.get(Lexem.class, session), score);
	}

	@Override
	protected void fromObject(Token obj, IPersistenceSession session) {
		tokenId = obj.getTokenId();
		lexem.set(obj.getLexem(), session);
		score = obj.getScore();
	}

	@Override
	protected void setObject(Token obj, IPersistenceSession session) {
		obj.setScore(score);		
	}

	public int getTokenId() {
		return tokenId;
	}

	public void setTokenId(int tokenId) {
		this.tokenId = tokenId;
	}

	public LexemBean getLexem() {
		return lexem;
	}

	public void setLexem(LexemBean lexem) {
		this.lexem = lexem;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

}
