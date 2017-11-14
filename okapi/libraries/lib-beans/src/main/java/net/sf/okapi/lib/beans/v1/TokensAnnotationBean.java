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

import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.lib.persistence.IPersistenceSession;
import net.sf.okapi.lib.persistence.PersistenceBean;
import net.sf.okapi.steps.tokenization.common.Token;
import net.sf.okapi.steps.tokenization.common.TokensAnnotation;
import net.sf.okapi.steps.tokenization.tokens.Tokens;

public class TokensAnnotationBean extends PersistenceBean<TokensAnnotation> {

	private List<TokenBean> tokens = new ArrayList<TokenBean>();
	
	@Override
	protected TokensAnnotation createObject(IPersistenceSession session) {
		Tokens tlist = new Tokens();
		for (TokenBean tokenBean : tokens)
			tlist.add(tokenBean.get(Token.class, session));
		
		return new TokensAnnotation(tlist);
	}

	@Override
	protected void fromObject(TokensAnnotation obj, IPersistenceSession session) {
		Tokens tlist = obj.getTokens();
		for (Token token : tlist) {
			TokenBean tokenBean = new TokenBean();
			tokens.add(tokenBean);
			tokenBean.set(token, session);
		}
	}

	@Override
	protected void setObject(TokensAnnotation obj, IPersistenceSession session) {
	}

	public List<TokenBean> getTokens() {
		return tokens;
	}

	public void setTokens(List<TokenBean> tokens) {
		this.tokens = tokens;
	}

}
