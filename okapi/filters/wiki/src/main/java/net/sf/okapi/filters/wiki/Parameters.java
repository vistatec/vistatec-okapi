/*===========================================================================
  Copyright (C) 2009-2011 by the Okapi Framework contributors
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

package net.sf.okapi.filters.wiki;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import net.sf.okapi.common.BaseParameters;
import net.sf.okapi.common.ISimplifierRulesParameters;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.core.simplifierrules.ParseException;
import net.sf.okapi.core.simplifierrules.SimplifierRules;

/**
 * Custom parameter implementation backed by YAML.  The normal
 * parameter getters and setters are stubbed.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class Parameters extends BaseParameters implements ISimplifierRulesParameters
{
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	
	public static final String WIKI_PARAMETERS = "wikiConfiguration.yml";
	
	private Yaml yaml;
	private Map<String, Object> config;
	private IdentityHashMap<Pattern, Pattern> customCodes;
	private boolean preserveWhitespace = false;

	public Parameters ()
	{	
		yaml = new Yaml();
		reset();
	}

	public void reset()
	{
		config = (Map) yaml.load(WikiFilter.class.getResourceAsStream(WIKI_PARAMETERS));
		initialize();
		setSimplifierRules(null);
	}
	
	private void initialize() {
		
		Object whitespace = config.get("preserve_whitespace");
		if (whitespace != null) preserveWhitespace = ((Boolean) whitespace).booleanValue();
		
		customCodes = new IdentityHashMap<Pattern, Pattern>();
		Object custom = config.get("custom_codes");
		if (custom != null) {
			for (HashMap<String, Object> c : ((ArrayList<HashMap<String, Object>>) custom)) {
				try {
					Object phRegex = c.get("pattern");
					if (phRegex != null) {
						customCodes.put(Pattern.compile((String)phRegex), null);
						continue;
					}
					Object startRegex = c.get("start_pattern");
					Object endRegex = c.get("end_pattern");
					if (startRegex != null & endRegex != null) {
						customCodes.put(Pattern.compile((String)startRegex),
								Pattern.compile((String)endRegex));
					}
				} catch (PatternSyntaxException ex) {
					LOGGER.warn("Regex pattern was invalid: " + ex.getPattern());
				} catch (NullPointerException ex) {
					LOGGER.warn("User-supplied custom regex for the Wiki filter was null. "
							+ "Make sure to enclose it in double-quotes in the config file.");
				}
			}
		}
	}

	@Override
	public String toString()
	{
		return yaml.dump(config);
	}
	
	public void fromString(String data)
	{
		config = (Map) yaml.load(data);
		initialize();
	}
	
	public boolean isPreserveWhitespace()
	{
		return preserveWhitespace;
	}

	public IdentityHashMap<Pattern, Pattern> getCustomCodePatterns() {
		return customCodes;
	}

	@Override
	public boolean getBoolean(String name) {
		return false;
	}

	@Override
	public void setBoolean(String name, boolean value) {
	}

	@Override
	public String getString(String name) {
		return null;
	}

	@Override
	public void setString(String name, String value) {
	}

	@Override
	public int getInteger(String name) {
		return 0;
	}

	@Override
	public void setInteger(String name, int value) {
	}
	
	@Override
	public String getSimplifierRules() {
		String sr = (String)config.get("simplifierRules");
		return sr;
	}

	@Override
	public void setSimplifierRules(String rules) {
		config.put("simplifierRules", rules);		
	}

	@Override
	public void validateSimplifierRules() throws ParseException {
		SimplifierRules r = new SimplifierRules(getSimplifierRules(), new Code());
		r.parse();
	}
}
