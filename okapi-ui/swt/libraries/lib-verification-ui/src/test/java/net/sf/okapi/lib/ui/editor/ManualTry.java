package net.sf.okapi.lib.ui.editor;

import net.sf.okapi.common.filters.DefaultFilters;
import net.sf.okapi.common.filters.FilterConfigurationMapper;
import net.sf.okapi.common.filters.IFilterConfigurationMapper;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class ManualTry {

	static public void main (String[] args ) {
		Display dispMain = null;
		try {
			// Start the application
			dispMain = new Display();
			Shell shlMain = new Shell(dispMain);
			// Create and fill the configuration mapper
			IFilterConfigurationMapper fcMapper = new FilterConfigurationMapper();
			DefaultFilters.setMappings(fcMapper, false, true);
			// Initialize the editor
			PairEditorUserTest editor = new PairEditorUserTest(shlMain, fcMapper, true); 
			// Start
			editor.showDialog();
		}
		catch ( Throwable e ) {
			e.printStackTrace();
		}
		finally {
			if ( dispMain != null ) dispMain.dispose();
		}
	}

}
