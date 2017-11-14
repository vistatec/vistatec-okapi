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

import net.sf.okapi.common.ISkeleton;
import net.sf.okapi.common.annotation.IAnnotation;
import net.sf.okapi.common.resource.BaseNameable;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.lib.persistence.IPersistenceSession;
import net.sf.okapi.lib.persistence.PersistenceBean;
import net.sf.okapi.lib.persistence.beans.FactoryBean;

public class BaseNameableBean extends PersistenceBean<BaseNameable> {

	private String id;
	private String name;
	private String type;
	private FactoryBean skeleton = new FactoryBean();	
	private String mimeType;
	private boolean isTranslatable;
	private boolean preserveWS;
	private List<PropertyBean> properties = new ArrayList<PropertyBean>();
	private AnnotationsBean annotations = new AnnotationsBean();
	private List<PropertyBean> sourceProperties = new ArrayList<PropertyBean>();

	@Override
	protected BaseNameable createObject(IPersistenceSession session) {
		return new BaseNameable();
	}

	@Override
	protected void fromObject(BaseNameable obj, IPersistenceSession session) {
			id = obj.getId();			
			name = obj.getName();
			type = obj.getType();
			//obj.setSkeleton(new GenericSkeleton());
			skeleton.set(obj.getSkeleton(), session);
			mimeType = obj.getMimeType();
			isTranslatable = obj.isTranslatable();
			preserveWS = obj.preserveWhitespaces();
			
			for (String propName : obj.getPropertyNames()) {
				PropertyBean propBean = new PropertyBean();
				propBean.set(obj.getProperty(propName), session);
				properties.add(propBean);
			}
						
			annotations.set(obj.getAnnotations(), session);
			
			for (String propName : obj.getSourcePropertyNames()) {
				PropertyBean propBean = new PropertyBean();
				propBean.set(obj.getSourceProperty(propName), session);
				sourceProperties.add(propBean);
			}
	}

	@Override
	protected void setObject(BaseNameable obj, IPersistenceSession session) {
			obj.setId(id);		
			obj.setName(name);
			obj.setType(type);
			
			obj.setSkeleton(skeleton.get(ISkeleton.class, session));
			obj.setMimeType(mimeType);
			obj.setIsTranslatable(isTranslatable);
			obj.setPreserveWhitespaces(preserveWS);
			
			for (PropertyBean prop : properties)
				//obj.setProperty(prop.get(new Property(prop.getName(), prop.getValue(), prop.isReadOnly()), session));
				obj.setProperty(prop.get(Property.class, session));
			
			for (FactoryBean annotationBean : annotations.getItems())
				obj.setAnnotation(annotationBean.get(IAnnotation.class, session));
			
			for (PropertyBean prop : sourceProperties)
				//obj.setSourceProperty(prop.get(new Property(prop.getName(), prop.getValue(), prop.isReadOnly()), session));
				obj.setSourceProperty(prop.get(Property.class, session));
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

	public AnnotationsBean getAnnotations() {
		return annotations;
	}

	public void setAnnotations(AnnotationsBean annotations) {
		this.annotations = annotations;
	}
}
