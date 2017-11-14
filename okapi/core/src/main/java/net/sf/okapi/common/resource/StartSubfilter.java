/*===========================================================================
  Copyright (C) 2008-2012 by the Okapi Framework contributors
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

package net.sf.okapi.common.resource;

import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.encoder.IEncoder;
import net.sf.okapi.common.filters.SubFilterSkeletonWriter;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.layerprovider.ILayerProvider;


public class StartSubfilter extends StartGroup {
	
	private StartDocument startDoc;
	private SubFilterSkeletonWriter skelWriter;
	private IEncoder parentEncoder;
	
	public StartSubfilter() {
	}

	/**
	 * Creates a new {@link StartSubfilter} object with the identifier of the group's parent
	 * and the group's identifier.
	 * @param id the identifier of this sub filter.
	 * @param startDoc The StartDocument resource of the subfilter.
	 * @param parentEncoder the parent encoder.
	 */
	public StartSubfilter (String id,
		StartDocument startDoc,
		IEncoder parentEncoder)
	{
		super(startDoc.getName(), null, false); // Not referenced by default
		this.startDoc = startDoc;
		this.parentEncoder = parentEncoder;
		setId(id);		
	}
	
	public LocaleId getLocale () {
		return startDoc.getLocale();
	}
	
	public String getEncoding () {
		return startDoc.getEncoding();
	}
	
	public boolean isMultilingual () {
		return startDoc.isMultilingual();
	}
	
	public IParameters getFilterParameters () {
		return startDoc.getFilterParameters();
	}
	
	public IFilterWriter getFilterWriter () {
		return startDoc.getFilterWriter();
	}
	
	public boolean hasUTF8BOM () {
		return startDoc.hasUTF8BOM();
	}
	
	public String getLineBreak () {
		return startDoc.getLineBreak();
	}

	public StartDocument getStartDoc () {
		return startDoc;
	}

	public SubFilterSkeletonWriter getSkeletonWriter () {
		return skelWriter;
	}

	public SubFilterSkeletonWriter createSkeletonWriter (StartSubfilter resource,
			LocaleId outputLocale,
			String outputEncoding,
			ILayerProvider layer)
	{
		this.skelWriter = new SubFilterSkeletonWriter(this);
		return this.skelWriter.setOptions(outputLocale, outputEncoding, this, layer);
	}

	public IEncoder getParentEncoder () {
		return parentEncoder;
	}
	
	// For serialization only

	protected void setSkelWriter(SubFilterSkeletonWriter skelWriter) {
		this.skelWriter = skelWriter;
	}

	protected void setStartDoc(StartDocument startDoc) {
		this.startDoc = startDoc;
	}

	protected void setParentEncoder(IEncoder parentEncoder) {
		this.parentEncoder = parentEncoder;
	}
	
}
