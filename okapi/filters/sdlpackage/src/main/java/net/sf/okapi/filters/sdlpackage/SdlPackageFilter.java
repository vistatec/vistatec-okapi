/*===========================================================================
  Copyright (C) 2017 by the Okapi Framework contributors
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

package net.sf.okapi.filters.sdlpackage;

import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filters.FilterConfiguration;
import net.sf.okapi.common.filters.FilterConfigurationMapper;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.filters.archive.ArchiveFilter;
import net.sf.okapi.filters.archive.Parameters;

/**
 * Implements the {@link IFilter} interface for SDLPPX and SDLRPX files.
 * This is written on top of the ArchiveFilter class.
 * The entries generated are the ones from the .sdlxliff files in the sub-folder that
 * is named for the target language.
 */
public class SdlPackageFilter extends ArchiveFilter {
	
	public static final String MIME_TYPE = "application/x-sdlpackage";
	
	private List<FilterConfiguration> configs;
	
	public SdlPackageFilter () {
		super();
		Parameters params = (Parameters)getParameters();
		params.setMimeType(MIME_TYPE);
		params.setSimplifierRules(null);

		FilterConfigurationMapper fcMapper = new FilterConfigurationMapper();
		fcMapper.addConfigurations("net.sf.okapi.filters.xliff.XLIFFFilter");
		setFilterConfigurationMapper(fcMapper);
	
		configs = new ArrayList<>();
		FilterConfiguration fc = new FilterConfiguration(getName(), MIME_TYPE,
			this.getClass().getName(), "SDL Trados Package Files", "SDL Trados 2017 SDLPPX and SDLRPX files", null, ".sdlppx;.sdlrpx;");
		configs.add(fc);
	}
	
	@Override
	public String getName() {
		return "okf_sdlpackage";
	}
	
	@Override
	public String getDisplayName() {
		return "SDLPPX and SDLRPX Filter (BETA)";
	}

	@Override
	// We override the method in AbstractFilter because we cannot set the proper
	// information through the super class (not completely)
	public List<FilterConfiguration> getConfigurations() {
		return configs;
	}

	@Override
	public void open(RawDocument input,
		boolean generateSkeleton)
	{
		// Get the target from the input, like ArchiveFilter
		LocaleId trg = input.getTargetLocale();
		// If not there, try the AbstractFilter, but unlikely to work
		if ( trg == null ) trg = getTrgLoc();
		
		// Adjust the filename filter to get only the files in target folder
		// This must be done first because the parameters are used in super.open() 
		// to set the internal ArchiveFilter filenames variable
		Parameters params = (Parameters)getParameters();
		params.setFileNames(trg.toBCP47()+"/*.sdlxliff");
		params.setConfigIds("okf_xliff-sdl");

		// Then call the super method
		super.open(input, generateSkeleton);
	}

}
