/*===========================================================================
  Copyright (C) 2009-2011 by the Okapi Framework contributors
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

package net.sf.okapi.filters.pensieve;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.IResource;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.exceptions.OkapiBadFilterInputException;
import net.sf.okapi.common.filters.FilterConfiguration;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filters.IFilterConfigurationMapper;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextUnit;
import net.sf.okapi.common.skeleton.ISkeletonWriter;
import net.sf.okapi.tm.pensieve.common.MetadataType;
import net.sf.okapi.tm.pensieve.common.TranslationUnit;
import net.sf.okapi.tm.pensieve.seeker.ITmSeeker;
import net.sf.okapi.tm.pensieve.seeker.TmSeekerFactory;

/**
 * Implementation of the {@link IFilter} interface for Pensieve TM.
 */
@UsingParameters() // No parameters
public class PensieveFilter implements IFilter {

	private static final String MIMETYPE = "application/x-pensieve-tm";
	
	private Iterator<TranslationUnit> iterator;
	private int state;
	private LocaleId srcLoc;
	private LocaleId trgLoc;
	private String docName;
	private EncoderManager encoderManager;

	private RawDocument input;

	@Override
	public void cancel () {
		// TODO: implement cancel
	}

	@Override
	public void close() {
		if (input != null) {
			input.close();
		}
		
		// Nothing to do
		state = 0;
	}

	@Override
	public IFilterWriter createFilterWriter () {
		return new PensieveFilterWriter();
	}

	@Override
	public ISkeletonWriter createSkeletonWriter () {
		return null; // No skeleton for this filter
	}

	@Override
	public List<FilterConfiguration> getConfigurations () {
		List<FilterConfiguration> list = new ArrayList<FilterConfiguration>();
		list.add(new FilterConfiguration(getName(),
			MIMETYPE,
			getClass().getName(),
			"Pensieve TM",
			"Configuration for Pensieve translation memories.",
			null,
			".pentm;"));
		return list;
	}

	@Override
	public EncoderManager getEncoderManager () {
		if ( encoderManager == null ) {
			encoderManager = new EncoderManager();
			// No mapping needed
		}
		return encoderManager;
	}

	@Override
	public String getDisplayName () {
		return "Pensieve TM Filter";
	}

	@Override
	public String getMimeType () {
		return MIMETYPE;
	}

	@Override
	public String getName () {
		return "okf_pensieve";
	}

	@Override
	public IParameters getParameters () {
		return null;
	}

	@Override
	public boolean hasNext () {
		return (state > 0);
	}

	@Override
	public Event next () {
		switch ( state ) {
		case 1: // Send start of document
			StartDocument sd = new StartDocument("sdID");
			sd.setName(docName);
			sd.setEncoding("UTF-8", false);
			sd.setLocale(srcLoc);
			sd.setFilterParameters(getParameters());
			sd.setFilterWriter(createFilterWriter());
			sd.setType(MIMETYPE);
			sd.setMimeType(MIMETYPE);
			sd.setMultilingual(true);
			sd.setLineBreak(Util.LINEBREAK_UNIX);
			state = 2; // Normal state
			return new Event(EventType.START_DOCUMENT, sd);
			
		case 2: // Normal item, check if we have reached the end
			if ( !iterator.hasNext() ) { // End of document
				Ending ed = new Ending("edID");
				state = 0; // Done
				return new Event(EventType.END_DOCUMENT, ed);
			}
			// Otherwise: create the text unit
			TranslationUnit item = iterator.next();
			ITextUnit tu = new TextUnit(item.getMetadata().get(MetadataType.ID));
			tu.setName(tu.getId()); // In this case resname == id
			tu.setSourceContent(item.getSource().getContent());
			if ( !item.isTargetEmpty() ) {
				TextContainer tc = tu.createTarget(trgLoc, false, IResource.CREATE_EMPTY);
				tc.setContent(item.getTarget().getContent());
			}
			String data = item.getMetadata().get(MetadataType.TYPE);
			if ( !Util.isEmpty(data) ) tu.setType(data);
			data = item.getMetadata().get(MetadataType.GROUP_NAME);
			if ( !Util.isEmpty(data) ) tu.setProperty(new Property("Txt::GroupName", data, false));
			data = item.getMetadata().get(MetadataType.FILE_NAME);
			if ( !Util.isEmpty(data) ) tu.setProperty(new Property("Txt::FileName", data, false));
			return new Event(EventType.TEXT_UNIT, tu);
			
		default:
			return null;
		}			
	}

	@Override
	public void open (RawDocument input) {
		open(input, true);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void open (RawDocument input,
		boolean generateSkeleton)
	{
		this.input = input;
		
		srcLoc = input.getSourceLocale();
		trgLoc = input.getTargetLocale();
		if ( input.getInputURI() == null ) {
			throw new OkapiBadFilterInputException("Only input URI is supported for this filter.");
		}
		
		docName = Util.getDirectoryName(input.getInputURI().getPath());
		ITmSeeker seeker = TmSeekerFactory.createFileBasedTmSeeker(docName);
		//TODO: Not very clean way to get the iterator, maybe ITmSeeker should just includes Iterable methods
		iterator = ((Iterable<TranslationUnit>)seeker).iterator();
		state = 1;
	}

	@Override
	public void setFilterConfigurationMapper (IFilterConfigurationMapper fcMapper) {
	}

	@Override
	public void setParameters (IParameters params) {
	}

}
