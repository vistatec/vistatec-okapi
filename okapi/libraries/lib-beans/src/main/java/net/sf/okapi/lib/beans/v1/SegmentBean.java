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

import net.sf.okapi.common.annotation.IAnnotation;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextPart;
import net.sf.okapi.lib.persistence.IPersistenceSession;
import net.sf.okapi.lib.persistence.beans.FactoryBean;

public class SegmentBean extends TextPartBean {
	
	private String id;
	private AnnotationsBean annotations = new AnnotationsBean();

	@Override
	protected TextPart createObject(IPersistenceSession session) {
		TextFragmentBean textBean = super.getPart();
		TextFragment text = null;
		
		if (textBean != null)
			text = textBean.get(TextFragment.class, session);
		else
			text = new TextFragment();
		
		return new Segment(id, text);
	}

	@Override
	protected void fromObject(TextPart obj, IPersistenceSession session) {
		super.fromObject(obj, session);
		
		if (obj instanceof Segment) {			
			Segment seg = (Segment) obj;
			id = seg.getId();
			annotations.set(seg.getAnnotations(), session);
		}
	}

	@Override
	protected void setObject(TextPart obj, IPersistenceSession session) {
		super.setObject(obj, session);
		if (obj instanceof Segment) {			
			Segment seg = (Segment) obj;
			for (FactoryBean annotationBean : annotations.getItems())
				seg.setAnnotation(annotationBean.get(IAnnotation.class, session));
		}
	}

	
	public String getSegment() {
		return id;
	}

	public void setSegment(String id) {
		this.id = id;
	}

	public void setAnnotations(AnnotationsBean annotations) {
		this.annotations = annotations;
	}

	public AnnotationsBean getAnnotations() {
		return annotations;
	}
}
