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

package net.sf.okapi.filters.vignette;

import net.sf.okapi.common.resource.EndSubfilter;
import net.sf.okapi.common.resource.StartSubfilter;
import net.sf.okapi.common.skeleton.GenericSkeletonWriter;

public class VignetteSkeletonWriter extends GenericSkeletonWriter {

	@Override
	public String processStartSubfilter (StartSubfilter resource) {		
		if ( resource.isReferent() || ( storageStack.size() > 0 )) {
			return super.processStartSubfilter(resource);
		}
		
		return "<![CDATA[";
	}
	
	@Override
	public String processEndSubfilter (EndSubfilter resource) {
		if ( storageStack.size() > 0 ) {
			return super.processEndSubfilter(resource);
		}

		return "]]>";
	}
}
