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

package net.sf.okapi.lib.extra;

import java.net.URL;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.okapi.common.ISimplifierRulesParameters;
import net.sf.okapi.common.ListUtil;
import net.sf.okapi.common.ParametersString;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.StringUtil;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.core.simplifierrules.ParseException;
import net.sf.okapi.core.simplifierrules.SimplifierRules;

/**
 * 
 * 
 * @version 0.1, 18.06.2009
 */

public abstract class AbstractParameters extends StringParameters implements INotifiable, ISimplifierRulesParameters {
	
	protected Component owner = null;
	private String data;

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	public AbstractParameters() {
		
		super();		
		
		parameters_init();
	}
	
	/**
	 * Called from the parameters' constructor. Create local objects here or leave empty. Initial values are assigned in reset().
	 */
	protected void parameters_init() {};
	
	/**
	 * Reset parameters values to defaults.
	 */
	protected abstract void parameters_reset();
	
	/**
	 * Load from buffer. The protected buffer variable is visible in all subclasses of BaseParameters.<p>
	 * @example myParam = buffer.getBoolean("myParam", false);
	 */
	protected abstract void parameters_load(ParametersString buffer);
	
	/**
	 * Save to buffer. The protected buffer variable is visible in all subclasses of BaseParameters.<p>
	 * @example buffer.setBoolean("myParam", myParam);
	 */
	protected abstract void parameters_save(ParametersString buffer);
	
	
	final public void reset() {
		
		parameters_reset();
	}
	
	final public void fromString(String data) {
		this.data = StringUtil.normalizeLineBreaks(data); 
		super.fromString(this.data);

		parameters_load(buffer); // this.getClass()
		setSimplifierRules(null);
		
		if (owner != null)
			owner.exec(this, Notification.PARAMETERS_CHANGED, null);
	}
	
	final public String toString () {
		buffer.reset();
		parameters_save(buffer);
		
		return super.toString();
	}
	
	public boolean exec(Object sender, String command, Object info) {
				
		if (command.equalsIgnoreCase(Notification.PARAMETERS_SET_OWNER)) {
			
			if (info instanceof Component)
				owner = (Component) info;
			
			return true;
		}
		return false;
	}
	
	public <T extends AbstractParameters> void loadGroup(ParametersString buffer, List<T> group, Class<T> elementClass) {
		
		if (elementClass == null) return;
		
		loadGroup(buffer, elementClass.getSimpleName(), group, elementClass);
	}
	
	public <T extends AbstractParameters> void loadGroup(ParametersString buffer, String groupName, List<T> group, Class<T> elementClass) {
		
		if (buffer == null) return;
		if (group == null) return;
		if (Util.isEmpty(groupName)) return;
		if (elementClass == null) return;
		
		group.clear();
		int count = buffer.getInteger(String.format("%sCount", groupName));
		
		for (int i = 0; i < count; i++ ) {
			
			T item = null;
			try {
				item = elementClass.newInstance();
				
			} catch (InstantiationException e) {
				
				//e.printStackTrace();
				logger.debug("Group element instantiation failed: {}", e.getMessage());
				return;
				
			} catch (IllegalAccessException e) {
				
				//e.printStackTrace();
				logger.debug("Group element instantiation failed: {}", e.getMessage());
				return;
			}
			
			if (item == null) return;
			
			item.parameters_load(new ParametersString(buffer.getGroup(String.format("%s%d", groupName, i))));
			
			group.add(item);
		}
		
		T item = ListUtil.getFirstNonNullItem(group);
		if (item == null) return;
		
		if (item.owner != null)
			item.owner.exec(group, Notification.PARAMETERS_CHANGED, null);
	}
	
	public <T extends AbstractParameters> void saveGroup(ParametersString buffer, String groupName, List<T> group) {
		
		if (buffer == null) return;
		if (group == null) return;
		if (Util.isEmpty(groupName)) return;
		
		buffer.setInteger(String.format("%sCount", groupName), group.size());
		
		for (int i = 0; i < group.size(); i++) {
			
			AbstractParameters item = group.get(i);
			ParametersString tmp = new ParametersString(); 
			
			item.parameters_save(tmp);
			buffer.setGroup(String.format("%s%d", groupName, i), tmp.toString());
		}
	}
	
	public <T extends AbstractParameters> void saveGroup(ParametersString buffer, List<T> group, Class<T> elementClass) {
		
		if (elementClass == null) return;
		
		saveGroup(buffer, elementClass.getSimpleName(), group);
	}
	
	public boolean loadFromResource(String resourceLocation) {

		// FIXME: Calling this.getClass().getResource(...) could give results other than 
		// expected if this class is extended by a class in another package.
		URL url = this.getClass().getResource(resourceLocation);
		if (url == null) return false;
		load(url, false);
		return true;
	}
	
	public boolean loadFromResource(Class<?> classRef, String resourceLocation) {

		if (classRef == null) 
			return loadFromResource(resourceLocation);

		//InputStream stream = classRef.getResourceAsStream(resourceLocation);
		URL url = classRef.getResource(resourceLocation);
		if (url == null) return false;
		load(url, false);
		return true;
	}
	
	public void saveToResource(String resourceLocation) {
		
		URL url = this.getClass().getResource(resourceLocation);
        if (url == null) return;
        
        save(url.getPath());
	}
	
	// TODO Test
	public void saveToResource(Class<?> classRef, String resourceLocation) {
		
		if (classRef == null) {
			
			saveToResource(resourceLocation);
			return;
		}
			 		
		URL url = classRef.getResource(resourceLocation);
        if (url == null) return;
        
        save(url.getPath());
	}
	
	public String getData() {
		
		return data;
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

