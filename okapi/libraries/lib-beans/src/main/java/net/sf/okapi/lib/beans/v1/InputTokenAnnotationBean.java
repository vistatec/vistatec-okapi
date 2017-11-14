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
import net.sf.okapi.steps.tokenization.common.InputTokenAnnotation;
import net.sf.okapi.steps.tokenization.common.Token;

public class InputTokenAnnotationBean extends PersistenceBean<InputTokenAnnotation> {

	private TokenBean inputToken = new TokenBean();
	
	@Override
	protected InputTokenAnnotation createObject(IPersistenceSession session) {
		return new InputTokenAnnotation(inputToken.get(Token.class, session));
	}

	@Override
	protected void fromObject(InputTokenAnnotation obj,
			IPersistenceSession session) {
		inputToken.set(obj.getInputToken(), session);
	}

	@Override
	protected void setObject(InputTokenAnnotation obj,
			IPersistenceSession session) {
		// No setters		
	}

	public void setInputToken(TokenBean inputToken) {
		this.inputToken = inputToken;
	}

	public TokenBean getInputToken() {
		return inputToken;
	}

}
