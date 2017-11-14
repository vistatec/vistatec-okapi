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

package net.sf.okapi.steps.tokenization.ui.mapping.model;

import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.ParametersString;
import net.sf.okapi.common.Util;
import net.sf.okapi.lib.extra.AbstractParameters;

public class Parameters extends AbstractParameters {

	private List<MappingItem> items; 
	
	@Override
	protected void parameters_init() {
		
		items = new ArrayList<MappingItem>(); 
	}

	@Override
	protected void parameters_load(ParametersString buffer) {
		
		loadGroup(buffer, items, MappingItem.class);
	}

	@Override
	protected void parameters_reset() {
		
		items.clear();
	}

	@Override
	protected void parameters_save(ParametersString buffer) {
		
		saveGroup(buffer, items, MappingItem.class);
	}
	
	public void addMapping(String editorClass, String parametersClass) {
		
		MappingItem item = new MappingItem();
		
		item.editorClass = editorClass;
		item.parametersClass = parametersClass;
		
		items.add(item);
	}

	public List<MappingItem> getItems() {
		
		return items;
	}

	public String getParametersClass(String editorClass) {
		
		if (Util.isEmpty(editorClass)) return "";
			
		for (MappingItem item : items) {
			
			if (item.editorClass.equalsIgnoreCase(editorClass))
				return item.parametersClass;
		} 
		
		return "";
	}
	
	public String getEditorClass(String parametersClass) {
		
		if (Util.isEmpty(parametersClass)) return "";
		
		for (MappingItem item : items) {
			
			if (item.parametersClass.equalsIgnoreCase(parametersClass))
				return item.editorClass;
		} 
		
		return "";
	}
	
//	public boolean loadMapping() {
//		
//		try {
//			load(getClass().getResource("mapper.tprm").toURI(), false);
//						
//		} catch (URISyntaxException e) {
//			
//			return false;
//		}
//		
//		return true;
//	}
//	
//	public void saveMapping() {
//		
//		save(getClass().getResource("mapper.tprm").getPath());
//	}
	
}
