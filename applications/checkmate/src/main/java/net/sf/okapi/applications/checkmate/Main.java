/*===========================================================================
  Copyright (C) 2010 by the Okapi Framework contributors
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

package net.sf.okapi.applications.checkmate;

import java.io.File;
import java.net.URLDecoder;

import net.sf.okapi.common.Util;
import net.sf.okapi.common.filters.DefaultFilters;
import net.sf.okapi.common.filters.FilterConfigurationMapper;
import net.sf.okapi.common.filters.IFilterConfigurationMapper;
import net.sf.okapi.common.ui.BaseHelp;
import net.sf.okapi.lib.ui.verification.QualityCheckEditor;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class Main {

	public static void main (String args[]) {
		Display dispMain = null;
		try {
			// Compute the path of the help file
			File file = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getFile());
	    	String helpRoot = URLDecoder.decode(file.getAbsolutePath(),"utf-8"); //$NON-NLS-1$
	    	// Remove the JAR file if running an installed version
	    	if ( helpRoot.endsWith(".jar") ) helpRoot = Util.getDirectoryName(helpRoot); //$NON-NLS-1$
	    	// Remove the application folder in all cases
	    	helpRoot = Util.getDirectoryName(helpRoot);
	    	BaseHelp help = new BaseHelp(helpRoot + File.separator + "help"); //$NON-NLS-1$
	    	
			// Create the main shell
			dispMain = new Display();
			Shell shlMain = new Shell(dispMain);
			shlMain.setSize(700, 600);
			
			// Create the editor
			QualityCheckEditor editor = new QualityCheckEditor();
			// Create and fill the configuration mapper
			IFilterConfigurationMapper fcMapper = new FilterConfigurationMapper();
			DefaultFilters.setMappings(fcMapper, false, true);
			// Initialize the editor
			editor.initialize(shlMain, false, help, fcMapper, null);
			// start the application
			if ( args.length > 0 ) editor.showDialog(args[0], true);
			else editor.showDialog(null, false);
		}
		catch ( Throwable e ) {
			e.printStackTrace();
		}
		finally {
			if ( dispMain != null ) dispMain.dispose();
		}
    }

}
