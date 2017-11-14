package net.sf.okapi.lib.ui.verification;

import net.sf.okapi.common.filters.DefaultFilters;
import net.sf.okapi.common.filters.FilterConfigurationMapper;
import net.sf.okapi.common.ui.BaseHelp;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class ManualTry {

	static public void main (String[] args ) {
		Display dispMain = null;
		try {
			// Start the application
			dispMain = new Display();
			Shell shlMain = new Shell(dispMain);
			
			QualityCheckEditor qce = new QualityCheckEditor();
//			QualityCheckEditor2 qce = new QualityCheckEditor2();
			FilterConfigurationMapper fcMapper = new FilterConfigurationMapper();
			DefaultFilters.setMappings(fcMapper, true, true);
	    	BaseHelp help = new BaseHelp("dummyRoot");
			qce.initialize(shlMain, false, help, fcMapper, null);
			qce.edit(false);
		}
		catch ( Throwable e ) {
			e.printStackTrace();
		}
		finally {
			if ( dispMain != null ) dispMain.dispose();
		}
	}

}
