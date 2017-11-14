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

package net.sf.okapi.lib.extra.filters;

import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.Util;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.filters.FilterConfiguration;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filters.IFilterConfigurationMapper;
import net.sf.okapi.lib.extra.OkapiComponent;

/**
 * The root of the filters hierarchy. Defines generic methods for all kinds of filters.
 * 
 * @version 0.1, 10.06.2009
 */

public abstract class AbstractBaseFilter extends OkapiComponent implements IFilter {
	
	private String mimeType;
	private String displayName;
	
	List<FilterConfiguration> configList = new ArrayList<FilterConfiguration>();
	EncoderManager encoderManager;


//	@Override
//	protected void component_create() {
//	public AbstractFilter() {
//		
//		configList = new ArrayList<FilterConfiguration>();
//	}
	
	public AbstractBaseFilter() {
		super();
	}

	@Override
	public void setFilterConfigurationMapper (IFilterConfigurationMapper fcMapper) {
		//TODO: Implement if derived filters need sub-filters
	}

	protected void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public String getMimeType() {
		return mimeType;
	}
	
	protected void setDisplayName (String displayName) {
		this.displayName = displayName;
	}
	
	public String getDisplayName () {
		return displayName;
	}
	
	public EncoderManager getEncoderManager () {
		if ( encoderManager == null ) {
			encoderManager = new EncoderManager();
			encoderManager.setAllKnownMappings();
		}
		return encoderManager;
	}

	protected boolean addConfiguration(			
			boolean clearAllExisting,
			String configId,
			String name,
			String description,
			String parametersLocation) {
		
		if (configList == null) return false;
		
		if (clearAllExisting) configList.clear();
		
		return configList.add(new FilterConfiguration(
				configId,
				getMimeType(),
				getClass().getName(),
				name, description, parametersLocation));
	}
	
	protected boolean addConfiguration(			
			boolean clearAllExisting,
			String configId,
			String name,
			String description,
			String parametersLocation,
			String extensions) {
		
		if (configList == null) return false;
		
		if (clearAllExisting) configList.clear();
		
		return configList.add(new FilterConfiguration(
				configId,
				getMimeType(),
				getClass().getName(),
				name, description, parametersLocation, extensions));
	}
	
	protected boolean addConfigurations(List<FilterConfiguration> configs) {
	
		if (configList == null) return false;
		
		return configList.addAll(configs);
	}
	
	protected FilterConfiguration findConfiguration(String configId) {
		
		if (Util.isEmpty(configList)) return null;
		
		for (FilterConfiguration config : configList) {
			
			if (config == null) continue;
			if (config.configId.equalsIgnoreCase(configId)) 
				return config;
		}
		
		return null;
	}
	
	protected boolean removeConfiguration(String configId) {
		
		return configList.remove(findConfiguration(configId));
	}
				
	public List<FilterConfiguration> getConfigurations () {
		
		List<FilterConfiguration> res = new ArrayList<FilterConfiguration>();
		
		for (FilterConfiguration fc : configList) 
			res.add(new FilterConfiguration(
				fc.configId,
				getMimeType(),
				getClass().getName(),
				fc.name, fc.description, fc.parametersLocation, fc.extensions));
		
		return res;
	}

	public boolean setConfiguration(String configId) {
		
		return true;
	}
	

}

