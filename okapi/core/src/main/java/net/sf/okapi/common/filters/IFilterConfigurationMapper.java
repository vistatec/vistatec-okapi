/*===========================================================================
  Copyright (C) 2009 by the Okapi Framework contributors
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

import java.util.Iterator;
import java.util.List;

import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.IParametersEditor;
import net.sf.okapi.common.IParametersEditorMapper;
import net.sf.okapi.common.exceptions.OkapiEditorCreationException;
import net.sf.okapi.common.exceptions.OkapiFilterCreationException;

/**
 * Common set of methods to manage filter configurations.
 * <p>This interface allows you to add and remove filter configurations from a central
 * place. You can instantiate the filter corresponding to a given configuration,
 * as well as manipulate its parameters.
 * <p>Classes that implements this interface should consider overriding the methods
 * related to custom configurations to provide application-specific storage mechanism
 * and naming convention. The methods related to custom configurations are, for example:
 * {@link #createCustomConfiguration(FilterConfiguration)},
 * {@link #deleteCustomParameters(FilterConfiguration)},
 * {@link #getCustomParameters(FilterConfiguration)},
 * {@link #getCustomParameters(FilterConfiguration, IFilter)}, and
 * {@link #saveCustomParameters(FilterConfiguration, IParameters)}.
 */
public interface IFilterConfigurationMapper extends IParametersEditorMapper {

	/**
	 * Adds a new configuration to this mapper.
	 * @param config the configuration to add.
	 */
	public void addConfiguration (FilterConfiguration config);
	
	/**
	 * Removes a given configuration from this mapper.
	 * @param configId the identifier of the configuration to remove.
	 */
	public void removeConfiguration (String configId);
	
	/**
	 * Adds all the predefined configurations of a given filter to this mapper.
	 * @param filterClass the class name of the filter to lookup.
	 */
	public void addConfigurations (String filterClass);

	/**
	 * Removes all the configurations (predefined and custom) of a given
	 * filter from this mapper. 
	 * @param filterClass the class name of the filter to lookup.
	 */
	public void removeConfigurations (String filterClass);

	/**
	 * Removes configuration mappings from this mapper.
	 * @param customOnly true to clear only the custom configurations, false to 
	 * clear all the configurations from this mapper.
	 */
	public void clearConfigurations (boolean customOnly);
	
	/**
	 * Adds a new editor mapping to this mapper.
	 * @param editorClass the class name of the editor to add.
	 * @param parametersClass the class name of the parameters this editor can edit.
	 */
	public void addEditor (String editorClass,
		String parametersClass);
	
	/**
	 * Removes a given editor from this mapper.
	 * @param editorClass the class name of the editor to remove.
	 */
	public void removeEditor (String editorClass);
	
	/**
	 * Removes all editor mappings for this mapper.
	 */
	public void clearEditors ();
	
	/**
	 * Creates an instance of the filter for a given configuration identifier
	 * and loads its corresponding parameters.
	 * @param configId the configuration identifier to use for look-up.
	 * @param existingFilter an optional existing instance of a filter. This argument can be null.
	 * If this argument is not null, it is checked against the requested filter and re-use
	 * if the requested filter and the provided instance are the same. If the provided
	 * instance is re-used, its parameters are always re-loaded.
	 * Providing an existing instance of the requested filter may allow for better
	 * efficiency.  
	 * @return a new {@link IFilter} object (with its parameters loaded) for the given
	 * configuration identifier, or null if the object could not be created.
	 * @throws OkapiFilterCreationException if the filter could not be created.
	 */
	public IFilter createFilter (String configId,
		IFilter existingFilter);

	/**
	 * Creates an instance of the filter for a given configuration identifier
	 * and loads its corresponding parameters.
	 * @param configId the configuration identifier to use for look-up.
	 * @return a new {@link IFilter} object (with its parameters loaded) for the given
	 * configuration identifier, or null if the object could not be created.
	 * @throws OkapiFilterCreationException if the filter could not be created.
	 */
	public IFilter createFilter (String configId);
	
	/**
	 * Creates an instance of the filter's parameters editor for a given 
	 * configuration identifier.
	 * @param configId the configuration identifier to use for look-up.
	 * @param existingFilter an optional existing instance of a filter. This 
	 * argument can be null. If this argument is not null and matches the filter
	 * of the given configuration it is used instead of a temporay instance, to
	 * get an instance of the parameters object for which the editor is requested.
	 * @return a new IParametersEditor object for the given
	 * configuration identifier, or null if no editor is available or if
	 * the object could not be created.
	 * @throws OkapiFilterCreationException if a filter needed to be created
	 * and could not.
	 * @throws OkapiEditorCreationException if the editor could not be created.
	 */
	public IParametersEditor createConfigurationEditor (String configId,
		IFilter existingFilter);
	
	/**
	 * Creates an instance of the filter's parameters editor for a given 
	 * configuration identifier.
	 * @param configId the configuration identifier to use for look-up.  
	 * @return a new IParametersEditor object for the given
	 * configuration identifier, or null if no editor is available or if
	 * the object could not be created.
	 * @throws OkapiFilterCreationException if a filter needed to be created
	 * and could not.
	 * @throws OkapiEditorCreationException if the editor could not be created.
	 */
	public IParametersEditor createConfigurationEditor (String configId);
	
	/**
	 * Gets the FilterConfiguration object for a given configuration identifier.
	 * @param configId the configuration identifier to search for.
	 * @return the FilterConfiguration object for the given configuration identifier,
	 * or null if a match could not be found.
	 */
	public FilterConfiguration getConfiguration (String configId);
	
	/**
	 * Gets the first filter configuration for a given MIME type.
	 * @param mimeType MIME type to search for.
	 * @return the filter configuration for the given MIME type, or null if none is found.
	 */
	public FilterConfiguration getDefaultConfiguration (String mimeType);
	
	/**
	 * Gets the first filter configuration for a given extension.
	 * @param ext the extension to search for (must be in the form ".ext"
	 * but can be in any case).
	 * @return the filter configuration for the given extension, or null if none is found.
	 */
	public FilterConfiguration getDefaultConfigurationFromExtension (String ext);
	
	/**
	 * Gets a list of information on all filters in this mapper.
	 * @return a list of information on all filters in this mapper.
	 */
	public List<FilterInfo> getFiltersInfo ();

	/**
	 * Gets an iterator on all configurations objects for this mapper.
	 * @return an iterator on all configurations for this mapper.
	 */
	public Iterator<FilterConfiguration> getAllConfigurations ();
	
	/**
	 * Gets a list of all FilterConfiguration objects for a given MIME type.
	 * @param mimeType mimeType MIME type to search for.
	 * @return a list of all FilterConfiguration objects found for the
	 * given MIME type (the list may be empty).
	 */
	public List<FilterConfiguration> getMimeConfigurations (String mimeType);

	/**
	 * Gets a list of all FilterConfiguration objects for a given filter class.
	 * @param filterClass the class name of the filter to search for.
	 * @return a list of all FilterConfiguration objects found for the
	 * given filter class name (the list may be empty).
	 */
	public List<FilterConfiguration> getFilterConfigurations (String filterClass);

	/**
	 * Gets the parameters for a given configuration (predefined or custom).
	 * @param config the configuration for which the parameters are requested.
	 * @return the parameters object for the given configuration.
	 * @see #getCustomParameters(FilterConfiguration)
	 */
	public IParameters getParameters (FilterConfiguration config);
	
	/**
	 * Gets the parameters for a given configuration (predefined or custom).
	 * @param config the configuration for which the parameters are requested.
	 * @param existingFilter optional existing instance of the filter for the given
	 * configuration. This argument can be null. If it not null, the provided filter
	 * may be used to load the parameters (if it matches the appropriate class).
	 * Providing this argument may allow the method to be more efficient by not 
	 * creating a temporary filter to get an instance of the parameters to load. 
	 * @return the parameters object for the given configuration.
	 */
	public IParameters getParameters (FilterConfiguration config,
		IFilter existingFilter);
	
	/**
	 * Creates a custom configuration object based on a give one.
	 * The new configuration is <b>not</b> added to the current list.
	 * @param baseConfig the base configuration from which to base the new one.
	 * @return a new {@link FilterConfiguration} object set with some
	 * default values, or null if the configuration could not be created. 
	 */
	public FilterConfiguration createCustomConfiguration (FilterConfiguration baseConfig);

	/**
	 * Gets the parameters for a given custom filter configuration.
	 * This method provides a way for this mapper to implements how it retrieves
	 * custom filter parameters.  
	 * @param config the custom configuration for which the method should return the 
	 * filter parameters.
	 * @param existingFilter optional existing instance of the filter for the given
	 * configuration. This argument can be null. If it not null, the provided filter
	 * may be used to load the parameters (if it matches the appropriate class).
	 * Providing this argument may allow the method to be more efficient by not 
	 * creating a temporary filter to get an instance of the parameters to load. 
	 * @return the parameters for the given custom filter configuration, or null
	 * if the parameters could not be provided, or if the corresponding filter does not have
	 * parameters.
	 * @throws OkapiFilterCreationException if the filter of the given configuration
	 * could not be created to load the parameters.
	 */
	public IParameters getCustomParameters (FilterConfiguration config,
		IFilter existingFilter);
	
	/**
	 * Gets the parameters for a given custom filter configuration.
	 * This method provides a way for this mapper to implements how it retrieves
	 * custom filter parameters.  
	 * @param config the custom configuration for which the method should return the 
	 * filter parameters.
	 * @return the parameters for the given custom filter configuration, or null
	 * if the parameters could not be provided, or if the corresponding filter does not have
	 * parameters.
	 * @see #getParameters(FilterConfiguration)
	 * @throws OkapiFilterCreationException if the filter of the given configuration
	 * could not be created to load the parameters.
	 */
	public IParameters getCustomParameters (FilterConfiguration config);

	/**
	 * Saves the parameters of a custom configuration. 
	 * This method provides a way for this mapper to implements how it stores
	 * custom filter parameters.
	 * @param config the custom configuration for which to save the parameters.
	 * @param params the parameters to save.
	 */
	public void saveCustomParameters (FilterConfiguration config, IParameters params);
	
	/**
	 * Deletes the parameters of a custom configuration.
	 * This method provides a way for this mapper to implements how it permanently 
	 * delete custom filter parameters. The actual configuration is not removed
	 * from this mapper, you must do it by calling {@link #removeConfiguration(String)}.
	 * @param config the custom configuration for which to delete the parameters. 
	 */
	public void deleteCustomParameters (FilterConfiguration config);

}
