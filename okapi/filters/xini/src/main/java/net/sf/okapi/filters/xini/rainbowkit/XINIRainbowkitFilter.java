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

package net.sf.okapi.filters.xini.rainbowkit;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.MimeTypeMapper;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.filters.FilterConfiguration;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filters.IFilterConfigurationMapper;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.skeleton.GenericSkeletonWriter;
import net.sf.okapi.common.skeleton.ISkeletonWriter;

/**
 * Filter for rainbow kid creation and merging of xini files.
 * 
 */
public class XINIRainbowkitFilter implements IFilter {
	private EncoderManager encoderManager;
	private XINIRainbowkitReader reader;
	private LinkedList<Event> queue;
	private String relDocName;
	
	public XINIRainbowkitFilter () {
		queue = new LinkedList<Event>();
	}

	/**
	 * For TKit merging only!
	 * 
	 * @param relDocName The relative path if the original document. Used to extract only the pages related to this document.
	 */
	public XINIRainbowkitFilter(String relDocName) {
		this();
		this.relDocName = relDocName;
	}

	@Override
	public void cancel () {
	}

	@Override
	public void close () {
		if ( reader != null ) {
			reader.close();
			reader = null;
		}
	}

	@Override
	public String getName () {
		return "okf_rainbowkitxini";
	}

	@Override
	public String getDisplayName () {
		return "XINI RainbowKit Filter";
	}

	@Override
	public String getMimeType () {
		return MimeTypeMapper.XINI_MIME_TYPE;
	}

	@Override
	public List<FilterConfiguration> getConfigurations () {
		List<FilterConfiguration> list = new ArrayList<FilterConfiguration>();
		list.add(new FilterConfiguration(getName(),
			MimeTypeMapper.XINI_MIME_TYPE,
			getClass().getName(),
			"XINI (Rainbow T-Kit)",
			"Configuration for XINI documents from ONTRAM T-Kits.",
			null));
		return list;
	}

	@Override
	public EncoderManager getEncoderManager () {
		if ( encoderManager == null ) {
			encoderManager = new EncoderManager();
			encoderManager.setMapping(MimeTypeMapper.XINI_MIME_TYPE, "net.sf.okapi.common.encoder.XMLEncoder");
		}
		return encoderManager;
	}

	@Override
	public IParameters getParameters () {
		return null;
	}

	@Override
	public boolean hasNext () {
		return !queue.isEmpty();
	}

	@Override
	public Event next () {
		return queue.poll();
	}

	@Override
	public void open (RawDocument input) {
		open(input, true);
	}

	@Override
	public void open (RawDocument input,
		boolean generateSkeleton)
	{
		close();
		
		// get events
		reader = new XINIRainbowkitReader();
		reader.open(input);
		// Reading the T-Kit
		queue.addAll(reader.getFilterEvents(relDocName));
	}

	@Override
	public void setFilterConfigurationMapper (IFilterConfigurationMapper fcMapper) {
	}

	@Override
	public void setParameters (IParameters params) {
	}

	@Override
	public ISkeletonWriter createSkeletonWriter() {
		return new GenericSkeletonWriter();
	}

	@Override
	public IFilterWriter createFilterWriter () {
		return new XINIRainbowkitWriter();
	}

}
