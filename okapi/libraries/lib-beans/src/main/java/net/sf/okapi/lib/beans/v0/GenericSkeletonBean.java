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

import net.sf.okapi.common.skeleton.GenericSkeleton;
import net.sf.okapi.common.skeleton.GenericSkeletonPart;

@Deprecated
public class GenericSkeletonBean implements IPersistenceBean {

	private List<GenericSkeletonPartBean> parts = new ArrayList<GenericSkeletonPartBean>();
	
	@Override
	public <T> T get(Class<T> classRef) {
		GenericSkeleton skel = new GenericSkeleton();
		
		for (GenericSkeletonPartBean partBean : parts)
			skel.add(partBean.getData());

		return classRef.cast(skel);
	}

	@Override
	public void init(IPersistenceSession session) {		
	}

	@Override
	public IPersistenceBean set(Object obj) {
		if (obj instanceof GenericSkeleton) {
			GenericSkeleton skel = (GenericSkeleton) obj;
			
			for (GenericSkeletonPart part : skel.getParts()) {
				GenericSkeletonPartBean partBean = new GenericSkeletonPartBean();
				parts.add(partBean);
				partBean.set(part);
			}
		}
		return this;
	}

	public List<GenericSkeletonPartBean> getParts() {
		return parts;
	}

	public void setParts(List<GenericSkeletonPartBean> parts) {
		this.parts = parts;
	}

}
