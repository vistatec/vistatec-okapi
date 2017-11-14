/*===========================================================================
  Copyright (C) 2008-2011 by the Okapi Framework contributors
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

import java.util.LinkedList;
import java.util.List;

import net.sf.okapi.common.annotation.AltTranslation;
import net.sf.okapi.common.annotation.AltTranslationsAnnotation;
import net.sf.okapi.lib.persistence.IPersistenceSession;
import net.sf.okapi.lib.persistence.PersistenceBean;

public class AltTranslationsAnnotationBean extends PersistenceBean<AltTranslationsAnnotation> {

	private List<AltTranslationBean> list = new LinkedList<AltTranslationBean>();
	
	@Override
	protected AltTranslationsAnnotation createObject(IPersistenceSession session) {
		return new AltTranslationsAnnotation();
	}

	@Override
	protected void fromObject(AltTranslationsAnnotation obj,
			IPersistenceSession session) {
		for (AltTranslation annot : obj) {
			AltTranslationBean bean = new AltTranslationBean();
			list.add(bean);
			bean.set(annot, session);
		}		
	}

	@Override
	protected void setObject(AltTranslationsAnnotation obj,
			IPersistenceSession session) {
		for (AltTranslationBean bean : list) {
			AltTranslation annot = bean.get(AltTranslation.class, session);
			obj.add(annot);
		}		
	}

	public void setList(List<AltTranslationBean> list) {
		this.list = list;
	}

	public List<AltTranslationBean> getList() {
		return list;
	}

}
