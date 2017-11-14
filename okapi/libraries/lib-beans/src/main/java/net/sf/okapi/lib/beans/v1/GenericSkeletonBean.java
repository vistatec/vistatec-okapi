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

import net.sf.okapi.common.skeleton.GenericSkeleton;
import net.sf.okapi.common.skeleton.GenericSkeletonPart;
import net.sf.okapi.lib.persistence.IPersistenceSession;
import net.sf.okapi.lib.persistence.PersistenceBean;

public class GenericSkeletonBean extends PersistenceBean<GenericSkeleton> {

	private List<GenericSkeletonPartBean> parts = new ArrayList<GenericSkeletonPartBean>();

	@Override
	protected GenericSkeleton createObject(IPersistenceSession session) {
		return new GenericSkeleton();
	}

	@Override
	protected void fromObject(GenericSkeleton obj, IPersistenceSession session) {
		for (GenericSkeletonPart part : obj.getParts()) {
			GenericSkeletonPartBean partBean = new GenericSkeletonPartBean();
			parts.add(partBean);
			partBean.set(part, session);
		}
	}

	@Override
	protected void setObject(GenericSkeleton obj, IPersistenceSession session) {
		for (GenericSkeletonPartBean partBean : parts)
			obj.getParts().add(partBean.get(GenericSkeletonPart.class, session));
	}

	public List<GenericSkeletonPartBean> getParts() {
		return parts;
	}

	public void setParts(List<GenericSkeletonPartBean> parts) {
		this.parts = parts;
	}
}
