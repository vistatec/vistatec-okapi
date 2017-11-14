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

package net.sf.okapi.lib.beans.v0;

import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.TextFragment;

@Deprecated
public class TextFragmentBean implements IPersistenceBean {

	private String text;
	private List<CodeBean> codes = new ArrayList<CodeBean>();
	
	@Override
	public void init(IPersistenceSession session) {
	}
		
	@Override
	public <T> T get(Class<T> classRef) {
		TextFragment tf = new TextFragment(text); 
		
		for (CodeBean code : codes)
			tf.getCodes().add(code.get(Code.class));
		
		return classRef.cast(tf);
	}

	@Override
	public IPersistenceBean set(Object obj) {

		if (obj instanceof TextFragment) {
			TextFragment tc = (TextFragment) obj;
			text = tc.getCodedText();
			
			for (Code code : tc.getCodes()) {
				CodeBean codeBean = new CodeBean();
				codeBean.set(code);
				codes.add(codeBean);
			}			
		}		
		return this;
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
