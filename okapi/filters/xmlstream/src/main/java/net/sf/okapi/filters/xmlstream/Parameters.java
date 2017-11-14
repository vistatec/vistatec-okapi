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

package net.sf.okapi.filters.xmlstream;

import java.io.File;
import java.net.URL;

import net.sf.okapi.common.IParameters;
import net.sf.okapi.filters.abstractmarkup.AbstractMarkupParameters;
import net.sf.okapi.filters.abstractmarkup.config.TaggedFilterConfiguration;

/**
 * {@link IParameters} based facade around the YAML configuration format.
 * 
 */
public class Parameters extends AbstractMarkupParameters {
	
	public static final String DEFAULT_PARAMETERS = "default.yml";
	public static final String DITA_PARAMETERS = "dita.yml";
	public static final String PROPERTY_XML_PARAMETERS = "javaPropertiesXml.yml";
	public static final String XML_ESCAPED_HTML = "xml_esc_html.yml";
	public static final String XML_CDATA_ESCAPED_HTML = "xml_cdata_esc_html.yml";
	public static final String IDD_ESCAPED_HTML = "idd_esc_html.yml";		
			
	/**
	 * XMLStreamFilter Parameters.
	 * Default constructor loads default.yml configuration
	 */
	public Parameters() {
		reset();
	}
	
	public Parameters(URL configPath) {
		setTaggedConfig(new TaggedFilterConfiguration(configPath));
	}

	public Parameters(File configFile) {
		setTaggedConfig(new TaggedFilterConfiguration(configFile));
	}

	public Parameters(String configAsString) {
		setTaggedConfig(new TaggedFilterConfiguration(configAsString));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.okapi.common.IParameters#reset()
	 */
	public void reset() {		
		setTaggedConfig(new TaggedFilterConfiguration(XmlStreamFilter.class.getResource(DEFAULT_PARAMETERS)));
	}
}
