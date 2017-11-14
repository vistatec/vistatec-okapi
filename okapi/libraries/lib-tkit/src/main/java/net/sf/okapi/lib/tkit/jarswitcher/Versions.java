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

import java.util.TreeSet;

/**
 * This class is a wrapper for TreeSet<VersionInfo> as the current implementation
 * of JSONUtil.fromJSON() is unable of handling parameterized classes.
 */
public class Versions extends TreeSet<VersionInfo> {

	private static final long serialVersionUID = -2904842241980155137L;

	public Versions() {
		super();
	}
}
