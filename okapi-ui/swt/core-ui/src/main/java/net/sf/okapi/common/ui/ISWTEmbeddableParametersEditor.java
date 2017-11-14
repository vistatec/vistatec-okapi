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

package net.sf.okapi.common.ui;

import net.sf.okapi.common.IContext;
import net.sf.okapi.common.IEmbeddableParametersEditor;
import net.sf.okapi.common.IParameters;

import org.eclipse.swt.widgets.Composite;

/**
 * Common way for SWT-based UI to implement an embedded panel defined 
 * through {@link IEmbeddableParametersEditor} interface. 
 */
public interface ISWTEmbeddableParametersEditor extends IEmbeddableParametersEditor {

	/**
	 * Initializes the object to be used as embedded editor.
	 * @param parent the composite parent where the editor is embedded.
	 * @param paramsObject the parameters to edit.
	 * @param context the context.
	 */
	public void initializeEmbeddableEditor (Composite parent,
		IParameters paramsObject,
		IContext context);
	
	/**
	 * Gets the Composite object (panel) of the editor. You must have called
	 * {@link #initializeEmbeddableEditor(Composite, IParameters, IContext)} before
	 * calling this method.
	 * @return the Composite object of the editor.
	 */
	public Composite getComposite ();
	
}
