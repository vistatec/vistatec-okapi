package net.sf.okapi.applications.serval;

import net.sf.okapi.common.query.IQuery;
import net.sf.okapi.common.ui.Dialogs;
import net.sf.okapi.common.ui.OKCancelPanel;
import net.sf.okapi.common.ui.UIUtil;
import net.sf.okapi.connectors.apertium.ApertiumMTConnector;
import net.sf.okapi.connectors.crosslanguage.CrossLanguageMTConnector;
import net.sf.okapi.connectors.globalsight.GlobalSightTMConnector;
import net.sf.okapi.connectors.google.GoogleMTv2Connector;
import net.sf.okapi.connectors.mymemory.MyMemoryTMConnector;
import net.sf.okapi.connectors.pensieve.PensieveTMConnector;
import net.sf.okapi.connectors.promt.ProMTConnector;
import net.sf.okapi.connectors.simpletm.SimpleTMConnector;
import net.sf.okapi.connectors.tda.TDASearchConnector;
import net.sf.okapi.connectors.translatetoolkit.TranslateToolkitTMConnector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

public class SelectionForm {

	private Shell shell;
	private IQuery result;
	private List lbResources;
	private OKCancelPanel pnlActions;

	public SelectionForm (Shell parent) {
		shell = new Shell(parent, SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.APPLICATION_MODAL);
		shell.setText("New Translation Resource");
		UIUtil.inheritIcon(shell, parent);
		shell.setLayout(new GridLayout(1, false));
		
		Label stTmp = new Label(shell, SWT.NONE);
		stTmp.setText("Please, selection the type of translation resource to create:");
		
		lbResources = new List(shell, SWT.BORDER | SWT.V_SCROLL);
		lbResources.setLayoutData(new GridData(GridData.FILL_BOTH));

		//TODO: get list and attached data from plugin system
		lbResources.add("Google MT v2 (Internet)");
		lbResources.add("SimpleTM local translation memory file");
		lbResources.add("GlobalSight TM Web service");
		lbResources.add("Translate Toolkit TM (remote or local)");
		lbResources.add("MyMemory TM (Internet)");
		lbResources.add("Pensieve TM");
		lbResources.add("Apertium MT (remote or local)");
		lbResources.add("ProMT (Internet)");
		lbResources.add("Cross-Language (Internet)");
		lbResources.add("TDA Search (Internet)");
		lbResources.setSelection(0);
		
		// Dialog-level buttons
		SelectionAdapter OKCancelActions = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				result = null;
				if ( e.widget.getData().equals("h") ) {
					//TODO
				}
				if ( e.widget.getData().equals("o") ) {
					int n = lbResources.getSelectionIndex();
					switch ( n ) {
					case 0: // Google MT
						result = new GoogleMTv2Connector();
						break;
					case 1: // SimpleTM
						result = new SimpleTMConnector();
						break;
					case 2: // GlobalSight TM
						result = new GlobalSightTMConnector();
						break;
					case 3: // Translate Toolkit TM
						result = new TranslateToolkitTMConnector();
						break;
					case 4: // MyMemory TM
						result = new MyMemoryTMConnector();
						break;
					case 5: // Pensieve TM
						result = new PensieveTMConnector();
						break;
					case 6: // Apertium MT
						result = new ApertiumMTConnector();
						break;
					case 7: // ProMT
						result = new ProMTConnector();
						break;
					case 8: // Cross-Language
						result = new CrossLanguageMTConnector();
						break;
					case 9: // TDA
						result = new TDASearchConnector();
						break;
					}
				}
				shell.close();
			};
		};
		pnlActions = new OKCancelPanel(shell, SWT.NONE, OKCancelActions, true);
		pnlActions.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		shell.setDefaultButton(pnlActions.btOK);

		shell.pack();
		shell.setMinimumSize(shell.getSize());
		Point startSize = shell.getMinimumSize();
		if ( startSize.y < 200 ) startSize.y = 200; 
		shell.setSize(startSize);
		Dialogs.centerWindow(shell, parent);
	}
	
	public IQuery showDialog () {
		shell.open();
		while ( !shell.isDisposed() ) {
			if ( !shell.getDisplay().readAndDispatch() )
				shell.getDisplay().sleep();
		}
		return result;
	}
}
