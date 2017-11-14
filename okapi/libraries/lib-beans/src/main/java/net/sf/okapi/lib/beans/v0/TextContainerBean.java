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
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;

@Deprecated
public class TextContainerBean extends TextFragmentBean {
	
	private List<PropertyBean> properties = new ArrayList<PropertyBean>();
	private List<FactoryBean> annotations = new ArrayList<FactoryBean>();
	private List<SegmentBean> segments = new ArrayList<SegmentBean>();

	@Override
	public void init(IPersistenceSession session) {		
	}
	
	@Override
	public <T> T get(Class<T> classRef) {		
		
		TextFragment tf = super.get(TextFragment.class);
		TextContainer tc = new TextContainer(tf);
		
		for (PropertyBean prop : properties)
			tc.setProperty(prop.get(Property.class));
		
		for (FactoryBean annotationBean : annotations)
			tc.setAnnotation(annotationBean.get(IAnnotation.class));
		
		for (SegmentBean segment : segments)
			tc.getSegments().asList().add(segment.get(Segment.class));
		
		return classRef.cast(tc);
	}
	
	@Override
	public IPersistenceBean set(Object obj) {
		super.set(obj);
		
		if (obj instanceof TextContainer) {
			TextContainer tc = (TextContainer) obj;
						
			for (String propName : tc.getPropertyNames()) {
				PropertyBean propBean = new PropertyBean();
				propBean.set(tc.getProperty(propName));
				properties.add(propBean);
			}
			
			for (IAnnotation annotation : tc.getAnnotations()) {
				FactoryBean annotationBean = new FactoryBean();
				annotations.add(annotationBean);
				annotationBean.set(annotation);
			}
			
			List<Segment> segs = tc.getSegments().asList();
			if (segs != null)
				for (Segment segment : segs) {
					SegmentBean segBean = new SegmentBean();
					segments.add(segBean);
					segBean.set(segment);
				}
			
		}		
		return this;
	}

	public List<SegmentBean> getSegments() {
		return segments;
	}

	public void setSegments(List<SegmentBean> segments) {
		this.segments = segments;
	}

	public void setAnnotations(List<FactoryBean> annotations) {
		this.annotations = annotations;
	}

	public List<FactoryBean> getAnnotations() {
		return annotations;
	}

	public List<PropertyBean> getProperties() {
		return properties;
	}

	public void setProperties(List<PropertyBean> properties) {
		this.properties = properties;
	}
	
}
