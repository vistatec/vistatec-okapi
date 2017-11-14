/*===========================================================================
  Copyright (C) 2016-2017 by the Okapi Framework contributors
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.ResourceBundle;

/**
 * Helper object to load a default list of filters and their editor information.
 * The list is in the DefaultFilters.properties file.
 */
public class DefaultFilters {

	private static final String BUNDLE_NAME = "net.sf.okapi.common.filters.DefaultFilters";

	private static final Logger logger = LoggerFactory.getLogger(DefaultFilters.class);

	/**
	 * Add the default mappings provided in the DefaultFilters.properties file.
	 * @param fcMapper the mapper where to add the mapping.
	 * @param reset true to clear all filters, editors and dec descriptions in the mapper
	 * before setting the new ones.
	 * @param addConfigurations true to add the filters configurations, false to add 
	 * only the parameters editors and UI descriptions.
	 */
	public static void setMappings (IFilterConfigurationMapper fcMapper,
		boolean reset,
		boolean addConfigurations)
	{
		// Create the bundle and load it
		ResourceBundle res = ResourceBundle.getBundle(BUNDLE_NAME);
		Enumeration<String> keys = res.getKeys();
		ArrayList<String> list = Collections.list(keys);
		
		if ( reset ) {
			fcMapper.clearConfigurations(false);
			fcMapper.clearDescriptionProviders();
			fcMapper.clearEditors();
		}
		
		// Go through the keys
		for ( String key : list ) {
			// Skip non-filterClass entries
			if ( !key.startsWith("filterClass") ) continue;

			try {
				int n = key.indexOf('_');
				String suffix = key.substring(n);
				String value = res.getString(key);

				// Add the configurations for the filter
				if ( addConfigurations ) {
					fcMapper.addConfigurations(value);
				}

				String key2 = "parametersClass"+suffix;
				if ( list.contains(key2) ) {
					String paramsClass = res.getString(key2);
					// Add editor if available
					String key3 = "parametersEditorClass"+suffix;
					if ( list.contains(key3) ) {
						value = res.getString(key3);
						fcMapper.addEditor(value, paramsClass);
					}
					else { // Add editor descriptor if available
						key3 = "editorDescriptionProvider"+suffix;
						if ( list.contains(key3) ) {
							value = res.getString(key3);
							fcMapper.addDescriptionProvider(value, paramsClass);
						}
					}

				}
			}
			catch (Exception ex) {
				logger.warn("Error while trying to build filter for property key " + key + "Details: " + ex);
				continue;
			}

		} // End of for
		
	}

}
