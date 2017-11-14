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

import net.sf.okapi.common.annotation.IAnnotation;
import net.sf.okapi.common.resource.Property;

@Deprecated
public class PropertyBean implements IPersistenceBean {

	private String name;
	private String value;
	private boolean isReadOnly;
	private List<FactoryBean> annotations = new ArrayList<FactoryBean>();
	
	@Override
	public void init(IPersistenceSession session) {
	}

	@Override
	public <T> T get(Class<T> classRef) {
		Property prop = new Property(name, value, isReadOnly);
		
		for (FactoryBean annotationBean : annotations)
			prop.setAnnotation(annotationBean.get(IAnnotation.class));
		
		return classRef.cast(prop);
	}

	@Override
	public IPersistenceBean set(Object obj) {
		if (obj instanceof Property) {
			Property prop = (Property) obj;
			name = prop.getName();
			value = prop.getValue();
			isReadOnly = prop.isReadOnly();
			
			// TODO Property.getAnnotations()
			//annotations.write(tu.ge)
		}
		return this;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public List<FactoryBean> getAnnotations() {
		return annotations;
	}

	@Deprecated
	public void setAnnotations(List<FactoryBean> annotations) {
		this.annotations = annotations;
	}

	public String getName() {
		return name;
	}

	public boolean isReadOnly() {
		return isReadOnly;
	}

}
