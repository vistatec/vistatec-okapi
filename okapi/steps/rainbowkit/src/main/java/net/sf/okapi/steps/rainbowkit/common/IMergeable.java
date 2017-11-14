/*===========================================================================
  Copyright (C) 2013 by the Okapi Framework contributors
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

package net.sf.okapi.steps.rainbowkit.common;

/**
 * An interface for package writers that can merge new packages with existing
 * directories or packages.
 */
public interface IMergeable {

	/**
	 * Prepare a target directory for merging by e.g. moving files
	 * out of the way.
	 * @param dir Target directory
	 */
	public void prepareForMerge(String dir);

	/**
	 * Do post-merge cleanup, if necessary.
	 */
	public void doPostMerge();
}
