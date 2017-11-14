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

package net.sf.okapi.common.ui;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import net.sf.okapi.common.IHelp;
import net.sf.okapi.common.Util;

/**
 * Default implementation of the {@link IHelp} interface.
 */
public class BaseHelp implements IHelp {

	private static final String PKGROOT = "net.sf.okapi.";
	private String root;
	
	/**
	 * Creates a new BaseHelp object with a root directory
	 * @param rootDirectory The root directory for the help files. 
	 */
	public BaseHelp (String rootDirectory) {
		root = rootDirectory;
		if ( !root.endsWith(File.separator) ) {
			root += File.separator;
		}
	}

	@Override
	public void showWiki (String topic) {
		Util.openWikiTopic(topic);
	}
	
	@Override
	public void showTopic (Object object,
		String filename)
	{
		showTopic(object, filename, null);
	}
	
	@Override
	public void showTopic (Object object,
		String filename,
		String query)
	{
		String path = "";
		if ( object != null ) {
			// Compute the sub-directories if needed
			// Get the package name
			path = object.getClass().getPackage().getName();
			// Remove the Okapi root
			path = path.replace(PKGROOT, ""); //$NON-NLS-1$
			// Replace the dots by the directories separators
			path = path.replace(".", "/"); //$NON-NLS-1$
		}
		
		// Now set the computed full path
		path = root + path + File.separator + filename + ".html"; //$NON-NLS-1$
		// Check if we need to add the file protocol
		if ( path.indexOf("://") == -1 ) path = "file://"+path;
		// Add the query if needed
		if ( query != null ) path += ("?" + query); //$NON-NLS-1$
		// Call the URL
		try {
			Util.openURL(new URL(path).toString());
			//UIUtil.start(new URL(path));
		}
		catch ( MalformedURLException e ) {
			e.printStackTrace();
		}
	}

}
