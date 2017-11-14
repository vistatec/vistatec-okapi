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

package net.sf.okapi.filters.openxml;

import java.io.File;
import java.net.URL;

import net.sf.okapi.common.BaseParameters;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.ISimplifierRulesParameters;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.core.simplifierrules.ParseException;
import net.sf.okapi.core.simplifierrules.SimplifierRules;
import net.sf.okapi.filters.abstractmarkup.config.TaggedFilterConfiguration;

/**
 * {@link IParameters} based facade around the YAML configuration format.
 * The parameters are read-only; calls to setXXX() are ignored.
 * 
 */
public class YamlParameters extends BaseParameters implements ISimplifierRulesParameters {
	private static final String DEFAULT_PARAMETERS = "wordConfiguration.yml"; // DWH 8-4-09 to fix Issue 89 

	public static String getDefualtParameterFile() {
		return DEFAULT_PARAMETERS;
	}

	private TaggedFilterConfiguration taggedConfig;

	public YamlParameters() {
		reset();
	}
	
	public YamlParameters(URL configPath) {
		setTaggedConfig(new TaggedFilterConfiguration(configPath));
	}

	public YamlParameters(File configFile) {
		setTaggedConfig(new TaggedFilterConfiguration(configFile));
	}

	public YamlParameters(String configAsString) {
		setTaggedConfig(new TaggedFilterConfiguration(configAsString));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.okapi.common.IParameters#fromString(java.lang.String)
	 */
	public void fromString(String data) {
		taggedConfig = new TaggedFilterConfiguration(data);
	}

	@Override
	public String toString() {
		return taggedConfig.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.okapi.common.IParameters#reset()
	 */
	public void reset() {		
		taggedConfig = new TaggedFilterConfiguration(OpenXMLContentFilter.class.getResource(DEFAULT_PARAMETERS));
		setSimplifierRules(null);
	}

	public TaggedFilterConfiguration getTaggedConfig() {
		return taggedConfig;
	}

	/**
	 * @param taggedConfig new configuration
	 */
	public void setTaggedConfig(TaggedFilterConfiguration taggedConfig) {
		this.taggedConfig = taggedConfig;
	}

	@Override
	public boolean getBoolean(String name) {
		return taggedConfig.getBooleanParameter(name);
	}

	@Override
	public void setBoolean(String name, boolean value) {
	}

	@Override
	public String getString(String name) {
		return taggedConfig.getStringParameter(name);
	}

	@Override
	public void setString(String name, String value) {
	}

	@Override
	public int getInteger(String name) {
		// HACK: tagged configurations don't support this
		return 0;
	}

	@Override
	public void setInteger(String name, int value) {
	}	
	
	@Override
	public String getSimplifierRules() {
		return taggedConfig.getSimplifierRules();
	}

	@Override
	public void setSimplifierRules(String rules) {
		taggedConfig.setSimplfierRules(rules);	
	}

	@Override
	public void validateSimplifierRules() throws ParseException {
		SimplifierRules r = new SimplifierRules(getSimplifierRules(), new Code());
		r.parse();
	}
}
