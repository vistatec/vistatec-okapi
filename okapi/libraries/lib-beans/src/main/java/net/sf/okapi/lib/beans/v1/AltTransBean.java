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

public class AltTransBean extends PersistenceBean<Object> {

	private String srcLang;
	private String trgLang;
	private TextUnitBean tu = new TextUnitBean();
	
	@Override
	protected Object createObject(IPersistenceSession session) {
		return null;
	}

	@Override
	protected void fromObject(Object obj, IPersistenceSession session) {
	}

	@Override
	protected void setObject(Object obj, IPersistenceSession session) {
	}

	public String getSrcLang() {
		return srcLang;
	}

	public void setSrcLang(String srcLang) {
		this.srcLang = srcLang;
	}

	public String getTrgLang() {
		return trgLang;
	}

	public void setTrgLang(String trgLang) {
		this.trgLang = trgLang;
	}

	public TextUnitBean getTu() {
		return tu;
	}

	public void setTu(TextUnitBean tu) {
		this.tu = tu;
	}

}
