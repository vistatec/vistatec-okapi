/*===========================================================================
  Copyright (C) 2009-2011 by the Okapi Framework contributors
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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.okapi.common.ISkeleton;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.annotation.IAnnotation;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextUnitUtil;

@Deprecated
public class TextUnitBean implements IPersistenceBean {
	private String id;
	private String name;
	private String type;
	private boolean isTranslatable;
	private boolean preserveWS;
	private String mimeType;
	private TextContainerBean source = new TextContainerBean();
	private Map<String, TextContainerBean> targets = new ConcurrentHashMap<String, TextContainerBean>();
	private FactoryBean skeleton = new FactoryBean();
	private List<PropertyBean> properties = new ArrayList<PropertyBean>();
	private List<FactoryBean> annotations = new ArrayList<FactoryBean>();
	
	public <T> T get(Class<T> classRef) {
		ITextUnit tu = TextUnitUtil.buildTU(source.get(TextContainer.class));
		
		tu.setId(id);
		tu.setName(name);
		tu.setType(type);
		tu.setIsTranslatable(isTranslatable);
		tu.setPreserveWhitespaces(preserveWS);
		tu.setMimeType(mimeType);		
		tu.setSource(source.get(TextContainer.class));
		
		for (String locTag : targets.keySet())
			tu.setTarget(LocaleId.fromString(locTag), targets.get(locTag).get(TextContainer.class));
		
		tu.setSkeleton(skeleton.get(ISkeleton.class));
		
		for (PropertyBean prop : properties)
			tu.setProperty(prop.get(Property.class));
		
		for (FactoryBean annotationBean : annotations)
			tu.setAnnotation(annotationBean.get(IAnnotation.class));
		
		return classRef.cast(tu);
	}
	
	public IPersistenceBean set(Object obj) {
		if (obj instanceof ITextUnit) {
			ITextUnit tu = (ITextUnit)obj;
			
			id = tu.getId();
			name = tu.getName();
			type = tu.getType();
			isTranslatable = tu.isTranslatable();
			preserveWS = tu.preserveWhitespaces();
			mimeType = tu.getMimeType();			
			source.set(tu.getSource());
			
			for (LocaleId locId : tu.getTargetLocales()) {
				TextContainerBean targetBean = new TextContainerBean();
				targets.put(locId.toString(), targetBean);
				targetBean.set(tu.getTarget(locId));
			}
			
			skeleton.set(tu.getSkeleton());
			
			for (String propName : tu.getPropertyNames()) {
				PropertyBean propBean = new PropertyBean();
				propBean.set(tu.getProperty(propName));
				properties.add(propBean);
			}
			
			// TODO TextUnit.getAnnotations()
			//annotations.set(tu.getAnnotations());
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

	public void setSkeleton(FactoryBean skeleton) {
		this.skeleton = skeleton;
	}

	public FactoryBean getSkeleton() {
		return skeleton;
	}

	public Map<String, TextContainerBean> getTargets() {
		return targets;
	}

	public void setTargets(Map<String, TextContainerBean> targets) {
		this.targets = targets;
	}

	@Override
	public void init(IPersistenceSession session) {		
	}

	public List<FactoryBean> getAnnotations() {
		return annotations;
	}

	public void setAnnotations(List<FactoryBean> annotations) {
		this.annotations = annotations;
	}

	public List<PropertyBean> getProperties() {
		return properties;
	}

	public void setProperties(List<PropertyBean> properties) {
		this.properties = properties;
	}

}
