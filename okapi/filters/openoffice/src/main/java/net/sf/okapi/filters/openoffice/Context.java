/*===========================================================================
  Copyright (C) 2009-2013 by the Okapi Framework contributors
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

package net.sf.okapi.filters.openoffice;

import net.sf.okapi.common.annotation.GenericAnnotations;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.skeleton.GenericSkeleton;

class Context {

	public String name;
	public boolean extract;
	public GenericSkeleton skel;
	public TextFragment tf;
	public ITextUnit tu;
	public GenericAnnotations anns;

	public Context (String name,
		boolean extract)
	{
		this.name = name;
		this.extract = extract;
	}

	public void setVariables (TextFragment tf,
		GenericSkeleton skel,
		ITextUnit tu)
	{
		this.tf = tf;
		this.skel = skel;
		this.tu = tu;
	}

}
