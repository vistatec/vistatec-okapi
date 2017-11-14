/*===========================================================================
  Copyright (C) 2011 by the Okapi Framework contributors
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

package net.sf.okapi.filters.rainbowkit;

public interface IManifestEditor {

	/**
	 * Opens the editor for the manifest files.
	 * @param parent UI parent object (can be null)
	 * @param manifest manifest object to edit
	 * @param inProcess true if a process is continued when the editor is closed.
	 * @return false on error or if the user cancels.
	 */
	public boolean edit (Object parent,
		Manifest manifest,
		boolean inProcess);	

}
