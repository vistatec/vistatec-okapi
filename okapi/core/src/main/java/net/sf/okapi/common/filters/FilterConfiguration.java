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

import java.io.InputStream;
import java.net.URLClassLoader;

import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.StreamUtil;

/**
 * Data set defining a filter configuration.
 */
public class FilterConfiguration {

	/**
	 * Unique identifier for this configuration.
	 */
	public String configId;
	
	/**
	 * The full name of the class that implement the filter for this configuration. 
	 */
	public String filterClass;
	
	/**
	 * The location of the parameters for this configuration. This should be the
	 * name of the file where the configuration is stored in the package resource
	 * for a pre-defined configurations; it can be null if the configuration is 
	 * the default one, and it can be anything for a custom configuration.
	 */
	public String parametersLocation;
	
	/**
	 * The parameters for this configuration. It can be null if the configuration is 
	 * the default one, and it can be anything for a custom configuration. This 
	 * will override parametersLocation of non-null. 
	 */
	public IParameters parameters;
	
	/**
	 * Short localizable name for this configuration.
	 */
	public String name;
	
	/**
	 * Longer localizable description of for this configuration.
	 */
	public String description;
	
	/**
	 * Flag indicating if this configuration is custom or pre-defined.
	 */
	public boolean custom;
	
	/**
	 * MIME type for this configuration.
	 */
	public String mimeType;

	/**
	 * Class loader to use for this filter (null to use the default).
	 */
	public URLClassLoader classLoader;
	
	/**
	 * List of extensions corresponding to this configuration. The list can be null.
	 * Otherwise it must be in the form: ".ext1;.ext2;" The ';' must be present even
	 * when there is only one extension. All extensions must have the '.'
	 */
	public String extensions;
	
	/**
	 * Creates an empty FilterConfiguration object.
	 */
	public FilterConfiguration () {
	}
	
	/**
	 * Creates a FilterConfiguration object and initializes it.
	 * @param configId the configuration identifier.
	 * @param mimeType the MIME type associated with this configuration.
	 * @param filterClass the filter class name.
	 * @param name the localizable name of this configuration.
	 * @param description the localizable description of this configuration. 
	 * @param parametersLocation the location where the parameters for this configuration
	 * are stored.
	 * @param extensions the extensions for this configuration (eg. ".htm;.html;") 
	 */
	public FilterConfiguration (String configId,
		String mimeType,
		String filterClass,
		String name,
		String description,
		String parametersLocation,
		String extensions)
	{
		this(configId, mimeType, filterClass, name, description, parametersLocation, null, extensions);
	}
	
	/**
	 * Creates a FilterConfiguration object and initializes it.
	 * @param configId the configuration identifier.
	 * @param mimeType the MIME type associated with this configuration.
	 * @param filterClass the filter class name.
	 * @param name the localizable name of this configuration.
	 * @param description the localizable description of this configuration. 
	 * @param parametersLocation the location where the parameters for this configuration are stored.
	 */
	public FilterConfiguration (String configId,
		String mimeType,
		String filterClass,
		String name,
		String description,
		String parametersLocation)
	{
		this(configId, mimeType, filterClass, name, description, parametersLocation, null, null);
	}
	
	/**
	 * Creates a FilterConfiguration object and initializes it.
	 * @param configId the configuration identifier.
	 * @param mimeType the MIME type associated with this configuration.
	 * @param filterClass the filter class name.
	 * @param name the localizable name of this configuration.
	 * @param description the localizable description of this configuration. 
	 */
	public FilterConfiguration (String configId,
		String mimeType,
		String filterClass,
		String name,
		String description)
	{
		this(configId, mimeType, filterClass, name, description, null, null, null);
	}

	/**
	 * Creates a FilterConfiguration object and initializes it.
	 * @param configId the configuration identifier.
	 * @param mimeType the MIME type associated with this configuration.
	 * @param filterClass the filter class name.
	 * @param name the localizable name of this configuration.
	 * @param description the localizable description of this configuration. 
	 * @param parametersLocation the location where the parameters for this configuration are stored.
	 * (use null if there are no parameters).
	 * @param parameters custom parameter instance for this filter.
	 * @param extensions the extensions for this configuration (eg. ".htm;.html;") 
	 */
	public FilterConfiguration (String configId,
		String mimeType,
		String filterClass,
		String name,
		String description,
		String parametersLocation,
		IParameters parameters,
		String extensions)
	{
		this.configId = configId;
		this.mimeType = mimeType;
		this.name = name;
		this.description = description;
		this.filterClass = filterClass;
		this.parametersLocation = parametersLocation;
		this.parameters = parameters;
		this.extensions = extensions;
	}
	
	/**
	 * Load the {@link IParameters} from the give {@link InputStream}
	 * @param stream - stream of the parameter file.
	 */
	public void loadParametersFromStream(InputStream stream) {
		// stream parameters always overrides any other source
		parametersLocation = null;
		this.parameters.fromString(StreamUtil.streamUtf8AsString(stream));
	}
}
