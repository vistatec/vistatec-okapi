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

package net.sf.okapi.common;

import net.sf.okapi.common.exceptions.OkapiEditorCreationException;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;

/**
 * Common set of methods to manage parameters editors and editor description providers.
 * Editing parameters can be done by a dedicated editor or by a generic editor.
 * This class is designed to manage these two types of class.
 * It associates names of parameters classes to editor classes or editor description
 * provider classes. This last type of class is used by the generic editor.
 */
public interface IParametersEditorMapper {

	/**
	 * Adds a new parameters editor mapping to this mapper.
	 * @param editorClass the class information of the editor to add.
	 * @param parametersClassName the class name of the parameters this editor can edit.
	 * If this class name is already listed, the exiting entry will be replaced by
	 * this one.
	 */
	public void addEditor (ClassInfo editorClass,
		String parametersClassName);
	
	/**
	 * Adds a new parameters editor mapping to this mapper. This is the same as
	 * calling <code>addEditor(new ClassInfo(editorClassName))</code>.
	 * @param editorClassName the class name of the editor to add.
	 * @param parametersClassName the class name of the parameters this editor can edit.
	 * If this class name is already listed, the exiting entry will be replaced by
	 * this one.
	 */
	public void addEditor (String editorClassName,
		String parametersClassName);
	
	/**
	 * Removes a given editor from this mapper.
	 * @param className the class name of the editor to remove.
	 */
	public void removeEditor (String className);
	
	/**
	 * Removes all editor mappings for this mapper.
	 */
	public void clearEditors ();

	/**
	 * Adds a new editor description provider mapping to this mapper.
	 * @param descriptionProviderClass the class information of the editor description
	 * provider to add.
	 * @param parametersClassName the class name of the parameters this editor can edit.
	 * If this class name is already listed, the exiting entry will be replaced by
	 * this one.
	 */
	public void addDescriptionProvider (ClassInfo descriptionProviderClass,
		String parametersClassName);
	
	/**
	 * Adds a new editor description provider mapping to this mapper. This is the same
	 * as calling <code>addDescriptionProvider(new ClassInfo(descriptionProviderClassName))</code>.
	 * @param descriptionProviderClassName the class name of the editor description
	 * provider to add.
	 * @param parametersClassName the class name of the parameters this editor can edit.
	 * If this class name is already listed, the exiting entry will be replaced by
	 * this one.
	 */
	public void addDescriptionProvider (String descriptionProviderClassName,
		String parametersClassName);
	
	/**
	 * Removes a given editor description provider from this mapper.
	 * @param className the class name of the editor description provider to remove.
	 */
	public void removeDescriptionProvider (String className);
	
	/**
	 * Removes all editor mappings for this mapper.
	 */
	public void clearDescriptionProviders ();

	/**
	 * Creates an instance of the parameters editor for a given parameters class name. 
	 * @param parametersClassName the parameters class name to use for lookup.
	 * @return a new IParametersEditor object for the given
	 * class name, or null if no editor is available or if
	 * the object could not be created.
	 * @throws OkapiEditorCreationException if the editor could not be created.
	 */
	public IParametersEditor createParametersEditor (String parametersClassName);
	
	/**
	 * Gets an object that can provide the UI description to use with a generic editor. 
	 * @param parametersClassName the name of the class for which to get the description provider.
	 * @return an editor description provider or null if none is set for this class.
	 */
	public IEditorDescriptionProvider getDescriptionProvider (String parametersClassName);

}
