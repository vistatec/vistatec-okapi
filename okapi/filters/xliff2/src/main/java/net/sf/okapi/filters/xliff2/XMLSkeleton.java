/*===========================================================================
  Copyright (C) 2014 by the Okapi Framework contributors
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
============================================================================*/

package net.sf.okapi.filters.xliff2;

import net.sf.okapi.common.IResource;
import net.sf.okapi.common.ISkeleton;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.INameable;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.TextFragment;

public class XMLSkeleton implements ISkeleton {

	private IResource parent;
	private StringBuilder before;
	private String snippet;
	
	@Override
	public String toString () {
		return (before==null ? "" : before.toString())
			+ getSnippetString();
	}
	
	@Override
	public ISkeleton clone () {
		XMLSkeleton newSkel = new XMLSkeleton();
		newSkel.parent = this.parent;
		newSkel.before = new StringBuilder(this.before);
		newSkel.snippet = this.snippet;
		return newSkel;
	}
	
	@Override
	public void setParent (IResource parent) {
		this.parent = parent;
	}

	@Override
	public IResource getParent () {
		return parent;
	}

	public void add (String data) {
		if ( data != null ) {
			if ( before == null ) before = new StringBuilder();
			before.append(data);
		}
	}

	public void addValuePlaceholder (INameable referent,
		String propName,
		LocaleId locId)
	{
		add(TextFragment.makeRefMarker("$self$", propName));
	}
	
	/**
	 * Adds to this skeleton a placeholder for the content (in a given locale) of the resource
	 * to which this skeleton is attached.
	 * @param textUnit the resource object.
	 * @param locId the locale; use null if the reference is the source.
	 */
	public void addContentPlaceholder (ITextUnit textUnit,
		LocaleId locId)
	{
		add(TextFragment.makeRefMarker("$self$"));
	}
	
	private String getSnippetString () {
		if (snippet == null) return "";
		return snippet;
	}
}
