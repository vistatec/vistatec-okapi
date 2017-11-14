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

import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.TextFragment;

@Deprecated
public class SegmentBean implements IPersistenceBean {

	private String id;
	private TextFragmentBean text = new TextFragmentBean();
	
	@Override
	public void init(IPersistenceSession session) {
	}

	@Override
	public <T> T get(Class<T> classRef) {
		Segment seg = new Segment(id, text.get(TextFragment.class));		
		return classRef.cast(seg);
	}

	@Override
	public IPersistenceBean set(Object obj) {
		if (obj instanceof Segment) {
			Segment seg = (Segment) obj;
			id = seg.id;
			text.set(seg.text);
		}
		return this;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public TextFragmentBean getText() {
		return text;
	}

	public void setText(TextFragmentBean text) {
		this.text = text;
	}

}
