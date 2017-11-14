/*===========================================================================
  Copyright (C) 2008-2014 by the Okapi Framework contributors
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

package net.sf.okapi.lib.tkit.jarswitcher;

import net.sf.okapi.common.Util;

/**
 * A snippet, demonstrating creation of a version manager configuration file.
 */
public class SampleConfigBuilder {

	public static void main(String[] args) {
		VersionManager manager = new VersionManager();
		
		// Reset the default config from resources
		manager.getVersions().clear();
	
		// Create versions. The versions will be auto-sorted by date.
		manager.add("m23", "/D:/git_local_repo/okapi_master/m23/okapi", "2013-9-27");
		manager.add("m19", "/D:/git_local_repo/okapi_master/m19/okapi", "2012-11-24");
		manager.add("m20", "/D:/git_local_repo/okapi_master/m20/okapi", "2013-2-17");
		manager.add("m24", "/D:/git_local_repo/okapi_master/m24/okapi", "2014-1-6");
		manager.add("m21", "/D:/git_local_repo/okapi_master/m21/okapi", "2013-4-16");
		manager.add("m22", "/D:/git_local_repo/okapi_master/m22-hotfix/okapi", "2013-7-19");
		
		// Store the configuration file at some location. Extension can be different from .json
		manager.store(Util.buildPath(System.getProperty("user.home"), "okapi_releases.json"));
	}
}
