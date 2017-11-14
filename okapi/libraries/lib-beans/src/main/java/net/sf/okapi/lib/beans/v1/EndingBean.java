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

import net.sf.okapi.common.ISkeleton;
import net.sf.okapi.common.annotation.IAnnotation;
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.lib.persistence.IPersistenceSession;
import net.sf.okapi.lib.persistence.PersistenceBean;
import net.sf.okapi.lib.persistence.beans.FactoryBean;

public class EndingBean extends PersistenceBean<Ending> {

	private String id;
	private FactoryBean skeleton = new FactoryBean();
	private AnnotationsBean annotations = new AnnotationsBean();

	@Override
	protected Ending createObject(IPersistenceSession session) {
		return new Ending(id);
	}

	@Override
	protected void fromObject(Ending obj, IPersistenceSession session) {
		id = obj.getId();
		skeleton.set(obj.getSkeleton(), session);
		
		annotations.set(obj.getAnnotations(), session);
	}

	@Override
	protected void setObject(Ending obj, IPersistenceSession session) {
		obj.setId(id);
		obj.setSkeleton(skeleton.get(ISkeleton.class, session));
		
		for (FactoryBean annotationBean : annotations.getItems())
			obj.setAnnotation(annotationBean.get(IAnnotation.class, session));
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public FactoryBean getSkeleton() {
		return skeleton;
	}

	public void setSkeleton(FactoryBean skeleton) {
		this.skeleton = skeleton;
	}

	public AnnotationsBean getAnnotations() {
		return annotations;
	}

	public void setAnnotations(AnnotationsBean annotations) {
		this.annotations = annotations;
	}
}
