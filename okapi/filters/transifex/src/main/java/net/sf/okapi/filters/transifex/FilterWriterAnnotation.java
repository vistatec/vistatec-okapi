/*===========================================================================
  Copyright (C) 2011 by the Okapi Framework contributors
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

package net.sf.okapi.filters.transifex;

import net.sf.okapi.common.annotation.IAnnotation;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.lib.transifex.ResourceInfo;

public class FilterWriterAnnotation implements IAnnotation {

	private IFilterWriter filterWriter;
	private Project project;
	private ResourceInfo info;
	
	public void setData (Project project,
		ResourceInfo info,
		IFilterWriter filterWriter)
	{
		this.project = project;
		this.info = info;
		this.filterWriter = filterWriter;
	}
	
	public IFilterWriter getFilterWriter () {
		return filterWriter;
	}
	
	public Project getProject () {
		return project;
	}
	
	public ResourceInfo getResourceInfo () {
		return info;
	}

}