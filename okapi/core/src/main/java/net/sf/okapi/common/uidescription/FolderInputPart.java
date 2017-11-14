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

package net.sf.okapi.common.uidescription;

import net.sf.okapi.common.ParameterDescriptor;

/**
 * UI part descriptor for a folder/directory.
 * This UI part supports the following types: String.
 */
public class FolderInputPart extends AbstractPart {

	private String browseTitle;
	
	/**
	 * Creates a new FolderInputPart object with a given  parameter descriptor.
	 * @param paramDescriptor the parameter descriptor for this UI part.
	 * @param browseTitle the title to use for the folder browsing dialog.
	 */
	public FolderInputPart (ParameterDescriptor paramDescriptor,
		String browseTitle)
	{
		super(paramDescriptor);
		this.browseTitle = browseTitle;
	}

	@Override
	protected void checkType () {
		// Check type support
		if ( getType().equals(String.class) ) return;
		// Otherwise: call the base method.
		super.checkType();
	}

	/**
	 * Gets the title of the folder browsing dialog.
	 * @return the title of the folder browsing dialog.
	 */
	public String getBrowseTitle () {
		return browseTitle;
	}

	/**
	 * Sets the title of the folder browsing dialog.
	 * @param browseTitle the new title of the folder browsing dialog.
	 */
	public void setBrowseTitle (String browseTitle) {
		this.browseTitle = browseTitle;
	}

}
