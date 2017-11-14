/*===========================================================================
  Copyright (C) 2010-2011 by the Okapi Framework contributors
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

package net.sf.okapi.lib.beans.v2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.okapi.common.ISkeleton;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.annotation.IAnnotation;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextUnit;
import net.sf.okapi.lib.beans.v1.AnnotationsBean;
import net.sf.okapi.lib.beans.v1.PropertyBean;
import net.sf.okapi.lib.persistence.IPersistenceSession;
import net.sf.okapi.lib.persistence.PersistenceBean;
import net.sf.okapi.lib.persistence.beans.FactoryBean;

public class TextUnitBean extends PersistenceBean<ITextUnit> {
	private String id;
	private int refCount;
	private String name;
	private String type;
	private boolean isTranslatable;
	private boolean preserveWS;		
	private List<PropertyBean> properties = new ArrayList<PropertyBean>();
	private AnnotationsBean annotations = new AnnotationsBean();
	private TextContainerBean source = new TextContainerBean();
	private String mimeType;
	private Map<String, TextContainerBean> targets = new ConcurrentHashMap<String, TextContainerBean>();
	private FactoryBean skeleton = new FactoryBean();
	
	public TextUnitBean() {
		super();
	}
	
	@Override
	protected ITextUnit createObject(IPersistenceSession session) {
		return new TextUnit(getId());
	}

	@Override
	protected void fromObject(ITextUnit obj, IPersistenceSession session) {
		id = obj.getId();
		refCount = obj.getReferenceCount();
		name = obj.getName();
		type = obj.getType();
		isTranslatable = obj.isTranslatable();
		preserveWS = obj.preserveWhitespaces();
		skeleton.set(obj.getSkeleton(), session);

		for (String propName : obj.getPropertyNames()) {
			PropertyBean propBean = new PropertyBean();
			propBean.set(obj.getProperty(propName), session);
			properties.add(propBean);
		}
		
		annotations.set(obj.getAnnotations(), session);
								
		source.set(obj.getSource(), session);
		mimeType = obj.getMimeType();
		
		for (LocaleId locId : obj.getTargetLocales()) {
			TextContainerBean targetBean = new TextContainerBean();
			targets.put(locId.toString(), targetBean);
			targetBean.set(obj.getTarget(locId), session);
		}
	}

	@Override
	protected void setObject(ITextUnit obj, IPersistenceSession session) {
		obj.setId(id);
		obj.setReferenceCount(refCount);
		obj.setName(name);
		obj.setType(type);
		obj.setIsTranslatable(isTranslatable);
		obj.setPreserveWhitespaces(preserveWS);
		
		for (PropertyBean propBean : properties)
			obj.setProperty(propBean.get(Property.class, session));
		
		for (FactoryBean annotationBean : annotations.getItems())
			obj.setAnnotation(annotationBean.get(IAnnotation.class, session));
		
		obj.setSource(source.get(TextContainer.class, session));
		obj.setMimeType(mimeType);	
		
		for (String locTag : targets.keySet())
			obj.setTarget(LocaleId.fromString(locTag), targets.get(locTag).get(TextContainer.class, session));

		obj.setSkeleton(skeleton.get(ISkeleton.class, session)); // Skel can contains refs to source and targets 
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
	public String getMimeType() {
		return mimeType;
	}
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	public TextContainerBean getSource() {
		return source;
	}
	public void setSource(TextContainerBean source) {
		this.source = source;
	}

	public Map<String, TextContainerBean> getTargets() {
		return targets;
	}

	public void setTargets(Map<String, TextContainerBean> targets) {
		this.targets = targets;
	}

	public void setSkeleton(FactoryBean skeleton) {
		this.skeleton = skeleton;
	}

	public FactoryBean getSkeleton() {
		return skeleton;
	}
	
	public AnnotationsBean getAnnotations() {
		return annotations;
	}

	public void setAnnotations(AnnotationsBean annotations) {
		this.annotations = annotations;
	}

	public List<PropertyBean> getProperties() {
		return properties;
	}

	public void setProperties(List<PropertyBean> properties) {
		this.properties = properties;
	}

	public void setRefCount(int refCount) {
		this.refCount = refCount;
	}

	public int getRefCount() {
		return refCount;
	}
}
