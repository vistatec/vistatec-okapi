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

import java.util.LinkedHashMap;

import net.sf.okapi.common.exceptions.OkapiEditorCreationException;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;

/**
 * Common set of methods to manage parameters editors.
 */
public class ParametersEditorMapper implements IParametersEditorMapper {

	/**
	 * Map of the editors for this mapper.
	 */
	protected LinkedHashMap<String, ClassInfo> editorMap;
	
	/**
	 * Map of the editor descriptions for this mapper.
	 */
	protected LinkedHashMap<String, ClassInfo> descMap;

	/**
	 * Creates an empty ParametersEditorMapper object.
	 */
	public ParametersEditorMapper () {
		editorMap = new LinkedHashMap<String, ClassInfo>();
		descMap = new LinkedHashMap<String, ClassInfo>();
	}
	
	@Override
	public void addEditor (ClassInfo editorClass,
		String parametersClassName)
	{
		editorMap.put(parametersClassName, editorClass);
	}

	@Override
	public void addEditor (String editorClassName,
		String parametersClassName)
	{
		editorMap.put(parametersClassName, new ClassInfo(editorClassName));
	}

	@Override
	public void addDescriptionProvider (ClassInfo descriptionProviderClass,
		String parametersClassName)
	{
		descMap.put(parametersClassName, descriptionProviderClass);
	}

	@Override
	public void addDescriptionProvider (String descriptionProviderClassName,
		String parametersClassName)
	{
		descMap.put(parametersClassName, new ClassInfo(descriptionProviderClassName));
	}

	@Override
	public void clearEditors () {
		editorMap.clear();
	}

	@Override
	public void clearDescriptionProviders () {
		descMap.clear();
	}

	@Override
	public void removeEditor (String className) {
		String found = null;
		for ( String key : editorMap.keySet() ) {
			if ( editorMap.get(key).name.equals(className) ) {
				found = key;
				break;
			}
		}
		if ( found != null ) {
			editorMap.remove(found);
		}
	}


	@Override
	public void removeDescriptionProvider (String className) {
		String found = null;
		for ( String key : descMap.keySet() ) {
			if ( descMap.get(key).name.equals(className) ) {
				found = key;
				break;
			}
		}
		if ( found != null ) {
			descMap.remove(found);
		}
	}

	@Override
	public IParametersEditor createParametersEditor (String parametersClassName) {
		ClassInfo ci = editorMap.get(parametersClassName);
		if ( ci == null ) return null;
		// Else: instantiate the editor
		IParametersEditor editor = null;
		try {
			if ( ci.loader == null ) {
				editor = (IParametersEditor)Class.forName(ci.name).newInstance();
			}
			else {
				editor = (IParametersEditor)Class.forName(ci.name,
					true, ci.loader).newInstance();
			}
		}
		catch ( InstantiationException e ) {
			throw new OkapiEditorCreationException(
				String.format("Cannot instantiate the editor '%s'", ci.name), e);
		}
		catch ( IllegalAccessException e ) {
			throw new OkapiEditorCreationException(
				String.format("Cannot instantiate the editor '%s'", ci.name), e);
		}
		catch ( ClassNotFoundException e ) {
			throw new OkapiEditorCreationException(
				String.format("Cannot instantiate the editor '%s'", ci.name), e);
		}
		return editor;
	}

	@Override
	public IEditorDescriptionProvider getDescriptionProvider (String parametersClassName) {
		ClassInfo ci = descMap.get(parametersClassName);
		if ( ci == null ) return null;
		// Else: instantiate the description provider
		IEditorDescriptionProvider descProv = null;
		try {
			if ( ci.loader == null ) {
				descProv = (IEditorDescriptionProvider)Class.forName(ci.name).newInstance();
			}
			else {
				descProv = (IEditorDescriptionProvider)Class.forName(ci.name,
					true, ci.loader).newInstance();
			}
		}
		catch ( InstantiationException e ) {
			throw new OkapiEditorCreationException(
				String.format("Cannot instantiate the description provider '%s'", ci.name), e);
		}
		catch ( IllegalAccessException e ) {
			throw new OkapiEditorCreationException(
				String.format("Cannot instantiate the description provider '%s'", ci.name), e);
		}
		catch ( ClassNotFoundException e ) {
			throw new OkapiEditorCreationException(
				String.format("Cannot instantiate the description provider '%s'", ci.name), e);
		}
		return descProv;
	}

}
