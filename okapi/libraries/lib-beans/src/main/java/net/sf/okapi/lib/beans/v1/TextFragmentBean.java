/*===========================================================================
  Copyright (C) 2010 by the Okapi Framework contributors
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

import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.lib.persistence.IPersistenceSession;
import net.sf.okapi.lib.persistence.PersistenceBean;

public class TextFragmentBean extends PersistenceBean<TextFragment> {

	private String text;
	private List<CodeBean> codes = new ArrayList<CodeBean>();

	@Override
	protected TextFragment createObject(IPersistenceSession session) {
		return new TextFragment();
	}

	@Override
	protected void fromObject(TextFragment obj, IPersistenceSession session) {
		text = obj.getCodedText();
		
		for (Code code : obj.getCodes()) {
			CodeBean codeBean = new CodeBean();
			codeBean.set(code, session);
			codes.add(codeBean);
		}			
	}

	@Override
	protected void setObject(TextFragment obj, IPersistenceSession session) {
//			for (CodeBean code : codes)
//				tf.getCodes().add(code.get(Code.class)); // tf.getCodes() returns Collections.unmodifiableList, no way to add
		
		List<Code> newCodes = new ArrayList<Code>();
		for (CodeBean code : codes)
			newCodes.add(code.get(Code.class, session));
		
		obj.setCodedText(text, newCodes);
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public List<CodeBean> getCodes() {
		return codes;
	}

	public void setCodes(List<CodeBean> codes) {
		this.codes = codes;
	}
}
