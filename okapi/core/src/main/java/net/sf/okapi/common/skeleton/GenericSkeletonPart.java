/*===========================================================================
  Copyright (C) 2008-2009 by the Okapi Framework contributors
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

package net.sf.okapi.common.skeleton;

import net.sf.okapi.common.IResource;
import net.sf.okapi.common.LocaleId;

/**
 * Part of {@link GenericSkeleton} object.
 */
public class GenericSkeletonPart { // public for OpenXML

	StringBuilder data;
	IResource parent = null;
	LocaleId locId = null;
	
	public GenericSkeletonPart() {
	}
	
	public GenericSkeletonPart (String data) {
		this.data = new StringBuilder(data);
	}

	public GenericSkeletonPart (char data) {
		this.data = new StringBuilder();
		this.data.append(data);
	}
	
	public GenericSkeletonPart(String data, IResource parent,
			LocaleId locId) {
		super();
		this.data = new StringBuilder(data);
		this.parent = parent;
		this.locId = locId;
	}
	
	public GenericSkeletonPart(char data, IResource parent,
			LocaleId locId) {
		super();
		this.data = new StringBuilder();
		this.data.append(data);
		this.parent = parent;
		this.locId = locId;
	}

	@Override
	public String toString () {
		return data.toString();
	}

	public void append (String data) {
		this.data.append(data);
	}

	public LocaleId getLocale () {
		return locId;
	}

	// for serialization
	public void setLocale (LocaleId locId) {
		this.locId = locId;
	}
	
	public IResource getParent() {
		return parent;
	}

	public void setParent(IResource newParent) {
		this.parent = newParent;
	}

	public StringBuilder getData () {
		return data;
	}

	public void setData(String data) {
		this.data = new StringBuilder(data);
	}
}
