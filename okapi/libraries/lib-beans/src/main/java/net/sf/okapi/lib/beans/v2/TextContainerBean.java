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

import net.sf.okapi.common.annotation.IAnnotation;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.Segments;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextPart;
import net.sf.okapi.lib.beans.v1.AnnotationsBean;
import net.sf.okapi.lib.beans.v1.PropertyBean;
import net.sf.okapi.lib.persistence.IPersistenceSession;
import net.sf.okapi.lib.persistence.PersistenceBean;
import net.sf.okapi.lib.persistence.beans.FactoryBean;

public class TextContainerBean extends PersistenceBean<TextContainer> {
	
	private List<PropertyBean> properties = new ArrayList<PropertyBean>();
	private AnnotationsBean annotations = new AnnotationsBean();
	//private List<TextPartBean> parts = new ArrayList<TextPartBean>();
	private List<FactoryBean> parts = new ArrayList<FactoryBean>();
	private boolean segApplied;
	
	private SegmentsBean segments = new SegmentsBean();

	public TextContainerBean() {
		super();
	}
	
	@Override
	protected TextContainer createObject(IPersistenceSession session) {
		List<TextPart> pts = new ArrayList<TextPart>(); 
		for (FactoryBean partBean : parts) {
			TextPart part = partBean.get(TextPart.class, session);
			if (part != null)
				pts.add(part);
		}
		return new TextContainer(pts.toArray(new TextPart[] {}));
	}

	@Override
	protected void fromObject(TextContainer obj, IPersistenceSession session) {
		for (String propName : obj.getPropertyNames()) {
			PropertyBean propBean = new PropertyBean();
			propBean.set(obj.getProperty(propName), session);
			properties.add(propBean);
		}
		
		annotations.set(obj.getAnnotations(), session);
		
		for (int i = 0; i < obj.count(); i++) {
			TextPart part = obj.get(i);
//			TextPartBean partBean = part.isSegment() ? // refId
//					(SegmentBean) session.createBean(part.getClass()) : 
//						(TextPartBean) session.createBean(part.getClass());
			FactoryBean partBean = new FactoryBean();
			parts.add(partBean);
			partBean.set(part, session);
		}
		
		segApplied = obj.hasBeenSegmented();
		
		if (obj.getSegments() instanceof Segments) {
			segments.set((Segments) obj.getSegments(), session);
		}
	}

	@Override
	protected void setObject(TextContainer obj, IPersistenceSession session) {
		for (PropertyBean prop : properties)
			obj.setProperty(prop.get(Property.class, session));
		
		for (FactoryBean annotationBean : annotations.getItems())
			obj.setAnnotation(annotationBean.get(IAnnotation.class, session));
		
		obj.setHasBeenSegmentedFlag(segApplied);
		
		if (obj.getSegments() instanceof Segments) {
			Segments srcSegs = segments.get(Segments.class, session);
			Segments destSegs = (Segments) obj.getSegments();
			if (destSegs != null && srcSegs != null) {
				destSegs.setAlignmentStatus(srcSegs.getAlignmentStatus());
				destSegs.setParts(srcSegs.getParts());
			}
		}
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

	public List<FactoryBean> getParts() {
		return parts;
	}

	public void setParts(List<FactoryBean> parts) {
		this.parts = parts;
	}

	public boolean isSegApplied() {
		return segApplied;
	}

	public void setSegApplied(boolean segApplied) {
		this.segApplied = segApplied;
	}
	
	public SegmentsBean getSegments() {
		return segments;
	}

	public void setSegments(SegmentsBean segments) {
		this.segments = segments;
	}
}
