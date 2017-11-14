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

import net.sf.okapi.common.ISkeleton;
import net.sf.okapi.common.annotation.IAnnotation;
import net.sf.okapi.common.resource.Ending;

@Deprecated
public class EndingBean implements IPersistenceBean {

	private String id;
	private FactoryBean skeleton = new FactoryBean();
	private List<FactoryBean> annotations = new ArrayList<FactoryBean>();
	
	@Override
	public void init(IPersistenceSession session) {
	}

	@Override
	public <T> T get(Class<T> classRef) {
		Ending en = new Ending(id);
		
		en.setSkeleton(skeleton.get(ISkeleton.class));
		
		for (FactoryBean annotationBean : annotations)
			en.setAnnotation(annotationBean.get(IAnnotation.class));
		
		return classRef.cast(en);
	}

	@Override
	public IPersistenceBean set(Object obj) {
		if (obj instanceof Ending) {
			Ending en = (Ending) obj;
			
			id = en.getId();
			skeleton.set(en.getSkeleton());
			
			// TODO Ending.getAnnotations()
			//annotations.set(en.getAnnotations());
		}
		return this;
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

	public List<FactoryBean> getAnnotations() {
		return annotations;
	}

	public void setAnnotations(List<FactoryBean> annotations) {
		this.annotations = annotations;
	}
}
