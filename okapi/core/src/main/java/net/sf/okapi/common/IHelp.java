/*===========================================================================
  Copyright (C) 2009-2010 by the Okapi Framework contributors
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

/**
 * Common way of calling the help topics, regardless of the underlying system
 * (for example: eclipse application or simple Java application).
 */
public interface IHelp {

	/**
	 * Shows a given topic of the Okapi Wiki.
	 * @param topic the name of the topic. Any space will be replaced
	 * automatically by '_'.
	 */
	public void showWiki (String topic);

	/**
	 * Shows the help for a given topic.
	 * @param object the object for which the help is to be displayed. The package
	 * name of this parameter is used to compute the location of the help file. 
	 * @param filename the filename of the topic to call. The location is computed
	 * for the package path of the object parameter.
	 * @param query an option query string, or null.
	 */
	public void showTopic (Object object,
		String filename,
		String query);

	/**
	 * Shows the help for a given topic.
	 * @param object the object for which the help is to be displayed. The package
	 * name of this parameter is used to compute the location of the help file. 
	 * @param filename the filename of the topic to call. The location is computed
	 * for the package path of the object parameter.
	 */
	public void showTopic (Object object,
		String filename);

}
