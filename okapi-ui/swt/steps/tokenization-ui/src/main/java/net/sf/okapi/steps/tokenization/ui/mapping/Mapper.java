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

package net.sf.okapi.steps.tokenization.ui.mapping;

import net.sf.okapi.common.BaseContext;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.ui.abstracteditor.AbstractParametersEditor;
import net.sf.okapi.steps.tokenization.ui.mapping.model.Parameters;

import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Widget;

public class Mapper extends AbstractParametersEditor {

	private static Parameters params = null;
	
	public static void main(String[] args) {
		
		Mapper mapper = new Mapper(); 
		params = (Parameters) mapper.createParameters();
		mapper.edit(params, false, new BaseContext());
	}
	
	@Override
	protected void createPages(TabFolder pageContainer) {

		addPage("Mapping", MappingTab.class);
	}

	@Override
	public IParameters createParameters() {
		
		return new Parameters(); 
	}

	@Override
	protected String getCaption() {
		
		return "Parameters mapper";
	}

	@Override
	protected void interop(Widget speaker) {


	}

	public static String getParametersClass(String editorClass) {
	
		if (Util.isEmpty(editorClass)) return "";
			
		if (params == null) {
			
			params = new Parameters();
			if (!params.loadFromResource("mapper.tprm")) return "";
		}
				
		return params.getParametersClass(editorClass);
	}
	
	public static String getEditorClass(String parametersClass) {
		
		if (Util.isEmpty(parametersClass)) return "";
		
		if (params == null) {
			
			params = new Parameters();
			if (!params.loadFromResource("mapper.tprm")) return "";
		}
				
		return params.getEditorClass(parametersClass);
	}
}
