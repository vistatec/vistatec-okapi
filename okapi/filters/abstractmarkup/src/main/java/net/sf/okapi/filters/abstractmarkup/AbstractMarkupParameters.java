/*===========================================================================
  Copyright (C) 2010 by the Okapi Framework contributors
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

package net.sf.okapi.filters.abstractmarkup;

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
 */
public class AbstractMarkupParameters extends BaseParameters implements ISimplifierRulesParameters {
	
	private TaggedFilterConfiguration taggedConfig;
	private String title = "Parameters Editor";

	public AbstractMarkupParameters () {
		reset();
	}

	@Override
	public void fromString (String data) {
		taggedConfig = new TaggedFilterConfiguration(data);
	}

	@Override
	public String toString () {
		return taggedConfig.toString();
	}

	@Override
	public void reset () {		
		taggedConfig = new TaggedFilterConfiguration("collapse_whitespace: false\nassumeWellformed: true");
	}

	/**
	 * Gets the title to use with the parameter editor.
	 * @return the title to use with the parameter editor.
	 */
	public String getEditorTitle () {
		return title;
	}
	
	/**
	 * Sets the title to use with the parameters editor.
	 * @param title the title to use with the parameters editor.
	 */
	public void setEditorTitle (String title) {
		this.title = title;
	}

	/**
	 * Gets the TaggedFilterConfiguration object for this parameters object.
	 * @return the TaggedFilterConfiguration object for this parameters object.
	 */
	public TaggedFilterConfiguration getTaggedConfig() {
		return taggedConfig;
	}

	/**
	 * Sets the TaggedFilterConfiguration object for this parameters object.
	 * @param taggedConfig the TaggedFilterConfiguration object for this parameters object.
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
		return taggedConfig.getIntegerParameter(name);
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
