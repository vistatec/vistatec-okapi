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
import net.sf.okapi.common.resource.BaseNameable;
import net.sf.okapi.common.resource.Property;

@Deprecated
public class BaseNameableBean implements IPersistenceBean{

	private String id;
	private FactoryBean skeleton = new FactoryBean();
	private String name;
	private String type;
	private String mimeType;
	private boolean isTranslatable;
	private boolean preserveWS;
	private List<PropertyBean> properties = new ArrayList<PropertyBean>();
	private List<FactoryBean> annotations = new ArrayList<FactoryBean>();
	private List<PropertyBean> sourceProperties = new ArrayList<PropertyBean>();
	
	@Override
	public <T> T get(Class<T> classRef) {
		BaseNameable bn = new BaseNameable();
		
		bn.setId(id);
		bn.setSkeleton(skeleton.get(ISkeleton.class));
		bn.setName(name);
		bn.setType(type);
		bn.setMimeType(mimeType);
		bn.setIsTranslatable(isTranslatable);
		bn.setPreserveWhitespaces(preserveWS);
		
		for (PropertyBean prop : properties)
			bn.setProperty(prop.get(Property.class));
		
		for (FactoryBean annotationBean : annotations)
			bn.setAnnotation(annotationBean.get(IAnnotation.class));
		
		for (PropertyBean prop : sourceProperties)
			bn.setSourceProperty(prop.get(Property.class));
		
		return classRef.cast(bn);
	}

	@Override
	public void init(IPersistenceSession session) {
	}

	@Override
	public IPersistenceBean set(Object obj) {
		if (obj instanceof BaseNameable) {
			BaseNameable bn = (BaseNameable) obj;
			
			id = bn.getId();
			skeleton.set(bn.getSkeleton());
			name = bn.getName();
			type = bn.getType();
			mimeType = bn.getMimeType();
			isTranslatable = bn.isTranslatable();
			preserveWS = bn.preserveWhitespaces();
			
			for (String propName : bn.getPropertyNames()) {
				PropertyBean propBean = new PropertyBean();
				propBean.set(bn.getProperty(propName));
				properties.add(propBean);
			}
			
			// TODO BaseNamable.getAnnotations()
			//annotations.set(bn.getAnnotations());
			
			for (String propName : bn.getSourcePropertyNames()) {
				PropertyBean propBean = new PropertyBean();
				propBean.set(bn.getSourceProperty(propName));
				sourceProperties.add(propBean);
			}
		}		
		return this;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public boolean isTranslatable() {
		return isTranslatable;
	}

	public void setTranslatable(boolean isTranslatable) {
		this.isTranslatable = isTranslatable;
	}

	public boolean isPreserveWS() {
		return preserveWS;
	}

	public void setPreserveWS(boolean preserveWS) {
		this.preserveWS = preserveWS;
	}

	public List<PropertyBean> getProperties() {
		return properties;
	}

	public void setProperties(List<PropertyBean> properties) {
		this.properties = properties;
	}

	public List<FactoryBean> getAnnotations() {
		return annotations;
	}

	public void setAnnotations(List<FactoryBean> annotations) {
		this.annotations = annotations;
	}

	public List<PropertyBean> getSourceProperties() {
		return sourceProperties;
	}

	public void setSourceProperties(List<PropertyBean> sourceProperties) {
		this.sourceProperties = sourceProperties;
	}

	public FactoryBean getSkeleton() {
		return skeleton;
	}

	public void setSkeleton(FactoryBean skeleton) {
		this.skeleton = skeleton;
	}

}
