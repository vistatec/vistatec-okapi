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

package net.sf.okapi.filters.doxygen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import net.sf.okapi.common.BaseParameters;
import net.sf.okapi.common.ISimplifierRulesParameters;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.core.simplifierrules.ParseException;
import net.sf.okapi.core.simplifierrules.SimplifierRules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

/**
 * Custom parameter implementation backed by YAML.  The normal
 * parameter getters and setters are stubbed.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class Parameters extends BaseParameters implements ISimplifierRulesParameters
{
	
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	
	public static final String DOXYGEN_PARAMETERS = "doxygenConfiguration.yml";
	
	private Yaml yaml;
	private Map<String, Object> config;
	private Map<String, Object> doxygenCommands;
	private Map<String, Object> htmlCommands;
	private IdentityHashMap<Pattern, Object> customCommands;
	private boolean preserveWhitespace = false;

	public Parameters ()
	{	
		yaml = new Yaml();
		reset();
	}

	public void reset()
	{
		config = (Map) yaml.load(DoxygenFilter.class.getResourceAsStream(DOXYGEN_PARAMETERS));
		initialize();
		setSimplifierRules(null);
	}
	
	private void initialize() {
		
		doxygenCommands = new HashMap<String, Object>();
		Object doxygen = config.get("doxygen_commands");
		if (doxygen != null) doxygenCommands = (Map<String, Object>) doxygen;
		
		htmlCommands = new HashMap<String, Object>();
		Object html = config.get("html_commands");
		if (html != null) htmlCommands = (Map<String, Object>) html;
		
		Object whitespace = config.get("preserve_whitespace");
		if (whitespace != null) preserveWhitespace = ((Boolean) whitespace).booleanValue();
		
		customCommands = new IdentityHashMap<Pattern, Object>();
		Object custom = config.get("custom_commands");
		if (custom != null)
			for (HashMap<String, Object> c : ((ArrayList<HashMap<String, Object>>) custom))
				try {
					String regex = (String) c.get("pattern");
					customCommands.put(Pattern.compile(regex), c);
				} catch (PatternSyntaxException ex) {
					LOGGER.warn("Regex pattern was invalid: " + ex.getPattern());
				} catch (NullPointerException ex) {
					LOGGER.warn("User-supplied custom regex for the Doxygen filter was null. "
							+ "Make sure to enclose it in double-quotes in the config file.");
				}
	}

	public boolean isDoxygenCommand(String cmd)
	{
		if (cmd.equals("@{") || cmd.equals("@}")) return true;
		
		if (!cmd.startsWith("\\") && !cmd.startsWith("@") && !cmd.startsWith("<")) return false;
		
		if (cmd.startsWith("<")) return htmlCommands.containsKey(clean(cmd));
		
		return doxygenCommands.containsKey(clean(cmd));
	}
	
	private String clean(String cmd)
	{
		if (cmd.equals("@{") || cmd.equals("@}")) return cmd;
		return cmd.replaceAll("[\\\\@<>/]|[\\[\\(\\{].*|\\s.*", "");
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
	
	public DoxygenCommand commandInfo(String rawCommand, Pattern pattern)
	{
		if (customCommands.containsKey(pattern)) {
			Map<String, Object> data = null;
			data = (Map<String, Object>) customCommands.get(pattern);
			return new DoxygenCommand(data, rawCommand, rawCommand, this);
		}
		
		if (!isDoxygenCommand(rawCommand)) return null;
		
		String cmdName = clean(rawCommand);
		
		Map<String, Object> data = null;
		
		if (rawCommand.startsWith("<")) data = (Map<String, Object>) htmlCommands.get(cmdName);
		else data = (Map<String, Object>) doxygenCommands.get(cmdName);
		
		return new DoxygenCommand(data, cmdName, rawCommand, this);
	}
	
	public boolean isPreserveWhitespace()
	{
		return preserveWhitespace;
	}

	public IdentityHashMap<Pattern, Object> getCustomCommandPatterns() {
		return customCommands;
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
		return getString(SIMPLIFIERRULES);
	}

	@Override
	public void setSimplifierRules(String rules) {
		setString(SIMPLIFIERRULES, rules);		
	}

	@Override
	public void validateSimplifierRules() throws ParseException {
		SimplifierRules r = new SimplifierRules(getSimplifierRules(), new Code());
		r.parse();
	}
}
