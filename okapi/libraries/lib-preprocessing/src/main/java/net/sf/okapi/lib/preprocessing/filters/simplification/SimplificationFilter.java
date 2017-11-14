/*===========================================================================
  Copyright (C) 2008-2013 by the Okapi Framework contributors
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

package net.sf.okapi.lib.preprocessing.filters.simplification;

import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.MimeTypeMapper;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.exceptions.OkapiBadFilterParametersException;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.filters.DefaultFilters;
import net.sf.okapi.common.filters.FilterConfiguration;
import net.sf.okapi.common.filters.FilterConfigurationMapper;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filters.IFilterConfigurationMapper;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.filters.xmlstream.XmlStreamFilter;
import net.sf.okapi.lib.preprocessing.filters.common.PreprocessingFilter;
import net.sf.okapi.steps.common.ResourceSimplifierStep;
import net.sf.okapi.steps.common.codesimplifier.CodeSimplifierStep;

@UsingParameters(Parameters.class)
public class SimplificationFilter extends PreprocessingFilter {

	private Parameters params;
	private IFilterConfigurationMapper fcMapper;
	
	public SimplificationFilter() {
		super(new XmlStreamFilter(), new ResourceSimplifierStep(), new CodeSimplifierStep());
		params = new Parameters();
		setParameters(params);
	}
	
	@Override
	public String getName() {
		return "okf_simplification";
	}
	
	@Override
	public String getDisplayName() {
		return "Simplification Filter";
	}
	
	@Override
	public List<FilterConfiguration> getConfigurations() {
		List<FilterConfiguration> list = new ArrayList<FilterConfiguration>();
		list.add(new FilterConfiguration(
				getName(),				
				MimeTypeMapper.XML_MIME_TYPE,
				getClass().getName(),
				"XML (Simplified resources and codes)",
				"Configuration for extracting resources from an XML file. Resources and then codes are simplified.",
				"xml.fprm",
				".xml;"));
		
		list.add(new FilterConfiguration(
				getName() + "-xmlResources",				
				MimeTypeMapper.XML_MIME_TYPE,
				getClass().getName(),
				"XML (Simplified resources)",
				"Configuration for extracting resources from an XML file. Resources are simplified.",
				"xml_resources.fprm",
				".xml;"));
		
		list.add(new FilterConfiguration(
				getName() + "-xmlCodes",				
				MimeTypeMapper.XML_MIME_TYPE,
				getClass().getName(),
				"XML (Simplified codes)",
				"Configuration for extracting resources from an XML file. Codes are simplified.",
				"xml_codes.fprm",
				".xml;"));
		
		return list;
	}
	
	@Override
	public Parameters getParameters() {
		return params;
	}
	
	@Override
	public void setParameters(IParameters params) {
		this.params = (Parameters) params;
	}
	
	@Override
	public void setFilterConfigurationMapper(IFilterConfigurationMapper fcMapper) {
		this.fcMapper = fcMapper;
	}
	
	@Override
	public void open(RawDocument input, boolean generateSkeleton) {
		if (params == null) {
			throw new OkapiBadFilterParametersException("Filter parameters are not specified");
		}
		if (Util.isEmpty(params.getFilterConfigId())) {
			throw new OkapiBadFilterParametersException("Filter config ID is not specified in filter parameters");
		}
		
		if (fcMapper == null) {
			fcMapper = new FilterConfigurationMapper();
			DefaultFilters.setMappings(fcMapper, true, true);
		}
		
		if (fcMapper == null) {
			throw new OkapiException("Filter Configuration Mapper is not set, cannot proceed");
		}
		
		IFilter newFilter = fcMapper.createFilter(params.getFilterConfigId());
		if (newFilter == null) {
			throw new OkapiBadFilterParametersException("Cannot create a filter for the filter config ID specified in filter parameters");
		}
		
		setFilter(newFilter);
		
		getSteps().clear();
		if (params.isSimplifyResources()) {
			getSteps().add(new ResourceSimplifierStep());
		}
		if (params.isSimplifyCodes()) {
			getSteps().add(new CodeSimplifierStep());
		}
		super.open(input, generateSkeleton);
	}
}
