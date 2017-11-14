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

package net.sf.okapi.common.resource;

import net.sf.okapi.common.IResource;
import net.sf.okapi.common.LocaleId;

/**
 * Provides the methods common to all resources that can be named and have properties. 
 */
public interface INameable extends IWithProperties, IWithBilingualProperties, IWithAnnotations, IResource {

	/**
	 * Gets the name of this resource. The resource name corresponds to different things depending
	 * on the type of resource. For a StartDocument the name is the URI of the document. Otherwise,
	 * in most cases the name is the identifier of the resource (This is the equivalent of the XLIFF 
	 * resname attribute).
	 * @return This resource name, or null if there is none.
	 */
	public String getName () ;
	
	/**
	 * Sets the name of this resource. The resource name is the equivalent of the XLIFF resname attribute.
	 * @param name New name to set.
	 */
	public void setName (String name);

	/**
	 * Gets the type information associated with this resource. For example "button".
	 * @return The type information associated with this resource.
	 */
	public String getType ();
	
	/**
	 * Sets the type information associated with this resource. For example "button".
	 * @param value The new type information.
	 */
	public void setType (String value);
	
	/**
	 * Gets the type of content of this resource. For example "text/xml".
	 * @return The type of content of this resource.
	 */
	public String getMimeType ();
	
	/**
	 * Sets the type of content of this resource. For example "text/xml".
	 * @param value The new type of content of this resource.
	 */
	public void setMimeType (String value);
	
	/**
	 * Creates or get a target property based on the corresponding source.
	 * @param locId The target locale to use.
	 * @param name The name of the property to create (or retrieve)
	 * @param overwriteExisting True to overwrite any existing property.
	 * False to not create a new property if one exists already. 
	 * @param creationOptions Creation options:
	 * <ul><li>CREATE_EMPTY: Creates an empty property, only the read-only flag 
	 * of the source is copied.</li>
	 * <li>COPY_CONTENT: Creates a new property with all its data copied from 
	 * the source.</li></ul>
	 * @return The property that was created, or retrieved. 
	 */
	public Property createTargetProperty (LocaleId locId,
		String name,
		boolean overwriteExisting,
		int creationOptions);

	/**
	 * Indicates if the content of this resource is translatable.
	 * By default this indicator is set to true for all resources. 
	 * @return True if the content of this resource is translatable. False if
	 * it is not translatable.
	 */
	public boolean isTranslatable ();
	
	/**
	 * Sets the flag indicating if the content of this resource is translatable.
	 * @param value True to indicate that the content of this resource is translatable.
	 */
	public void setIsTranslatable (boolean value);

	/**
	 * Indicates if the white-spaces in the content of this resource should be preserved.
	 * By default this indicator is set to false for all resources. 
	 * @return True if the white-spaces in the content of this resource should be preserved.
	 */
	public boolean preserveWhitespaces ();

	/**
	 * sets the flag indicating if the white-spaces in the content of this resource should be preserved.
	 * @param value True to indicate that the white-spaces in the content of this resource should be preserved.
	 */
	public void setPreserveWhitespaces (boolean value);
	
}
