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

package net.sf.okapi.filters.xini;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.MimeTypeMapper;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.filters.FilterConfiguration;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filters.IFilterConfigurationMapper;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.skeleton.GenericSkeletonWriter;
import net.sf.okapi.common.skeleton.ISkeletonWriter;

/**
 * Okapi Xini Filter for filtering and translating xini files.
 * 
 */
@UsingParameters(Parameters.class)
public class XINIFilter implements IFilter {
	private Parameters params;
	private EncoderManager encoderManager;
	private XINIReader reader;
	private LinkedList<Event> queue;
	private RawDocument input;
	
	public XINIFilter () {
		params = new Parameters();
		queue = new LinkedList<Event>();
	}

	@Override
	public void cancel () {
	}

	@Override
	public void close () {
		if ( input != null ) {
			input.close();
			input = null;
		}
		
		if ( reader != null ) {
			reader.close();
			reader = null;
		}
	}

	@Override
	public String getName () {
		return "okf_xini";
	}

	@Override
	public String getDisplayName () {
		return "XINI Filter";
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
			"XINI",
			"Configuration for XINI documents from ONTRAM",
			null,
			".xini;"));
		list.add(new FilterConfiguration(getName()+"-noOutputSegmentation",
				MimeTypeMapper.XINI_MIME_TYPE,
				getClass().getName(),
				"XINI (no output segmentation)",
				"Configuration for XINI documents from ONTRAM (fields in the output are not segmented)",
				"noOutputSegmentation.fprm"));
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
	public Parameters getParameters () {
		return params;
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
		this.input = input;
		
		// get events
		reader = new XINIReader(params);
		reader.open(input);
		// XINI is an input file for the pipeline
		queue.addAll(reader.getFilterEvents());
	}

	@Override
	public void setFilterConfigurationMapper (IFilterConfigurationMapper fcMapper) {
	}

	@Override
	public void setParameters (IParameters params) {
		this.params = (Parameters)params;
	}

	@Override
	public ISkeletonWriter createSkeletonWriter() {
		return new GenericSkeletonWriter();
	}

	@Override
	public IFilterWriter createFilterWriter () {
		return new XINIWriter(params);
	}
	
}

