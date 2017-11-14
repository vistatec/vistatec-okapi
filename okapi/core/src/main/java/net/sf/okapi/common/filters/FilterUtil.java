/*===========================================================================
  Copyright (C) 2009-2012 by the Okapi Framework contributors
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

package net.sf.okapi.common.filters;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.annotation.DeepenSegmentationAnnotaton;
import net.sf.okapi.common.annotation.SimplifierRulesAnnotaton;
import net.sf.okapi.common.exceptions.OkapiBadFilterInputException;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.resource.Custom;
import net.sf.okapi.core.simplifierrules.ParseException;
import net.sf.okapi.core.simplifierrules.SimplifierRules;

public class FilterUtil {
	
	/**
	 * Generate {@link Custom} {@link Event} for code simplifier rules.
	 * @param rules - simplifier rules as a single string.
	 * @return - {@link Event} with {@link Custom} resource and {@link SimplifierRulesAnnotaton}.
	 */
	public static final Event createCodeSimplifierEvent(String rules) {
		Custom cr = new Custom();
		SimplifierRulesAnnotaton a = new SimplifierRulesAnnotaton(); 
		// validate rules
		try {
			SimplifierRules.validate(rules);
		} catch (ParseException e) {
			throw new OkapiBadFilterInputException("Code simplifier rules are not valid.", e);
		}
		a.setRules(rules);
		cr.setAnnotation(a);
		return new Event(EventType.CUSTOM, cr);
	}

	/**
	 * Create an {@link Custom} {@link Event} that tells the SegmenterStep that it needs
	 * to deepen existing segmentation.
	 * @return {@link Event} with {@link Custom} resource and {@link DeepenSegmentationAnnotaton}.
	 */
	public static final Event createDeepenSegmentationEvent() {
		Custom cr = new Custom();
		DeepenSegmentationAnnotaton a = new DeepenSegmentationAnnotaton(); 		
		cr.setAnnotation(a);
		return new Event(EventType.CUSTOM, cr);
	}
	
	/**
	 * Creates an instance of the filter for a given configuration identifier
	 * and loads its corresponding parameters. Only Okapi default filter configurations 
	 * are accepted. 
	 * @param configId the filter configuration identifier. Can only be one of default filter 
	 * configurations.
	 * @return a new {@link IFilter} object (with its parameters loaded) for the given
	 * configuration identifier, or null if the object could not be created.
	 */
	public static IFilter createFilter(String configId) {
		IFilterConfigurationMapper fcMapper = new FilterConfigurationMapper();
		DefaultFilters.setMappings(fcMapper, true, true);
		return fcMapper.createFilter(configId);
	}
	
	/**
	 * Creates an instance of the filter for a given configuration identifier
	 * and loads its corresponding parameters.
	 * @param filterClass class of the filter.
	 * @param configId the filter configuration identifier. Can be either one of Okapi 
	 * default filter configurations or one of the built-in configurations defined in
	 * the filter class.
	 * @return a new {@link IFilter} object (with its parameters loaded) for the given
	 * configuration identifier, or null if the object could not be created.
	 */
	public static IFilter createFilter(Class<? extends IFilter> filterClass, String configId) {
		IFilterConfigurationMapper fcMapper = new FilterConfigurationMapper();
		DefaultFilters.setMappings(fcMapper, true, true);
		fcMapper.addConfigurations(filterClass.getName());
		return fcMapper.createFilter(configId);
	}
	
	/**
	 * Creates an instance of the filter for a given configuration identifier
	 * and loads its corresponding parameters. This method accepts a list of the
	 * URLs of fprm files defining custom configurations, and can be used to create
	 * a filter and configure its sub-filters in one call. 
	 * @param configId the filter configuration identifier. Can be either one of Okapi 
	 * default filter configurations or one of the custom configurations defined in
	 * the fprm files.
	 * @param customConfigs a list of the URLs of fprm files defining custom configurations.
	 * Every file name should follow the pattern of custom filter configurations, 
	 * i.e. contain a filter name like "okf_xmlstream@custom_config.fprm". The file extension 
	 * should be .fprm.
	 * @return a new {@link IFilter} object (with its parameters loaded) for the given
	 * configuration identifier, or null if the object could not be created.
	 */
	public static IFilter createFilter(String configId, URL... customConfigs) {
		FilterConfigurationMapper fcMapper = new FilterConfigurationMapper();
		DefaultFilters.setMappings(fcMapper, true, true);
		
		for (URL customConfig : customConfigs) {
			addCustomConfig(fcMapper, customConfig);			
		}
		
		IFilter filter = fcMapper.createFilter(configId);
		filter.setFilterConfigurationMapper(fcMapper);
		return filter;
	}
	
	/**
	 * Adds to a given {@link FilterConfigurationMapper} object the custom configuration 
	 * defined in the fprm file denoted by a given URL.
	 * @param fcMapper the given {@link FilterConfigurationMapper}.
	 * @param customConfig the URL of a fprm file defining the custom configuration
	 * the filter should be loaded from. The file extension should be .fprm.
	 * The file name should follow the pattern of custom filter configurations, 
	 * i.e. contain a filter name like "okf_xmlstream@custom_config.fprm".
	 * @return the configuration identifier or null if the configuration was not added.
	 */
	public static String addCustomConfig(FilterConfigurationMapper fcMapper, 
			URL customConfig) {
		String configId = null;
		try {
			String path = customConfig.toURI().getPath();
			String root = Util.getDirectoryName(path) + File.separator;
			configId = Util.getFilename(path, false);
			fcMapper.setCustomConfigurationsDirectory(root);
			fcMapper.addCustomConfiguration(configId);
			fcMapper.updateCustomConfigurations();
		} catch (URISyntaxException e) {
			throw new OkapiIOException(e);
		}
		return configId; 
	}
	
	/**
	 * Creates an instance of the filter for a given URL of a fprm file defining a
	 * custom configuration.
	 * @param customConfig the URL of a fprm file defining the custom configuration
	 * the filter should be loaded from. The file extension should be .fprm.
	 * The file name should follow the pattern of custom filter configurations, 
	 * i.e. contain a filter name like "okf_xmlstream@custom_config.fprm".
	 * @return a new {@link IFilter} object (with its parameters loaded) for the given
	 * configuration identifier, or null if the object could not be created.
	 */
	public static IFilter createFilter(URL customConfig) {
		FilterConfigurationMapper fcMapper = new FilterConfigurationMapper();
		DefaultFilters.setMappings(fcMapper, true, true);
		return fcMapper.createFilter(addCustomConfig(fcMapper, customConfig));
	}

}
