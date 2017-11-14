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

import net.sf.okapi.common.annotation.Annotations;
import net.sf.okapi.common.annotation.IAnnotation;
import net.sf.okapi.lib.persistence.IPersistenceSession;
import net.sf.okapi.lib.persistence.PersistenceBean;
import net.sf.okapi.lib.persistence.beans.FactoryBean;

public class AnnotationsBean extends PersistenceBean<Iterable<IAnnotation>> {

	private List<FactoryBean> items = new ArrayList<FactoryBean>();
	
	@Override
	protected Iterable<IAnnotation> createObject(IPersistenceSession session) {
		return new Annotations();
	}

	@Override
	protected void fromObject(Iterable<IAnnotation> obj, IPersistenceSession session) {
		for (IAnnotation annotation : obj) {
			FactoryBean annotationBean = new FactoryBean();
			items.add(annotationBean);
			annotationBean.set(annotation, session);
		}
	}

	@Override
	protected void setObject(Iterable<IAnnotation> obj, IPersistenceSession session) {
		if (obj instanceof Annotations) { // Otherwise a read-only collection
			Annotations annots = (Annotations) obj; 
			for (FactoryBean annotationBean : items)
				annots.set(annotationBean.get(IAnnotation.class, session));
		}		
	}

	public List<FactoryBean> getItems() {
		return items;
	}

	public void setItems(List<FactoryBean> items) {
		this.items = items;
	}
}
