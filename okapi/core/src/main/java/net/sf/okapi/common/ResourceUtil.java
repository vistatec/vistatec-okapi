/*===========================================================================
  Copyright (C) 2008-2010 by the Okapi Framework contributors
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

package net.sf.okapi.common;

import net.sf.okapi.common.resource.BaseNameable;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.StartSubDocument;
import net.sf.okapi.common.skeleton.SkeletonUtil;

public class ResourceUtil {

	public static void copyProperties(BaseNameable srcResource, BaseNameable trgResource) {
		for (String propName : srcResource.getPropertyNames()) {
			trgResource.setProperty(srcResource.getProperty(propName));
		}
	}
	
	public static StartSubDocument startSubDocumentFromStartDocument(
			StartDocument sd,
			String id, 
			String parentId, 
			String name) {
		StartSubDocument ssd = new StartSubDocument(parentId, id);
		ResourceUtil.copyProperties(sd, ssd);
		ssd.setName(name);
		ISkeleton ssdSkel = sd.getSkeleton().clone();
		ssd.setSkeleton(ssdSkel);
		SkeletonUtil.changeParent(ssdSkel, sd, ssd);
		return ssd;		
	}
	
	public static StartDocument startDocumentFromStartSubDocument(
			StartSubDocument ssd,
			String id,
			String lineBreak) {
		StartDocument sd = new StartDocument(id);
		sd.setLineBreak("\n");
		sd.setSkeleton(ssd.getSkeleton().clone());
		ResourceUtil.copyProperties(ssd, sd);
		return sd;		
	}
}
