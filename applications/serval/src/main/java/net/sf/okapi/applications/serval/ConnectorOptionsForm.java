package net.sf.okapi.applications.serval;

import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.ui.Dialogs;
import net.sf.okapi.common.ui.OKCancelPanel;
import net.sf.okapi.common.ui.UIUtil;
import net.sf.okapi.lib.translation.ResourceItem;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ConnectorOptionsForm {

	private Shell shell;
	private Text edSrcLang;
	private Text edTrgLang;
	private Text edName;
	private OKCancelPanel pnlActions;
	private ResourceItem resItem;
	private boolean result;
	private Text edParams;
	
	public ConnectorOptionsForm (Shell parent) {
		shell = new Shell(parent, SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.APPLICATION_MODAL);
		shell.setText("Translation Resource Options");
		UIUtil.inheritIcon(shell, parent);
		shell.setLayout(new GridLayout(2, false));
		
		Label stTmp = new Label(shell, SWT.NONE);
		stTmp.setText("Resource name:");
		
		edName = new Text(shell, SWT.BORDER);
		edName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		stTmp = new Label(shell, SWT.NONE);
		stTmp.setText("Source language:");
		
		edSrcLang = new Text(shell, SWT.BORDER);
		edSrcLang.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		stTmp = new Label(shell, SWT.NONE);
		stTmp.setText("Target language:");
		
		edTrgLang = new Text(shell, SWT.BORDER);
		edTrgLang.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
//		btEdit = new Button(shell, SWT.PUSH);
//		btEdit.setText("Parameters...");
//		btEdit.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		
		edParams = new Text(shell, SWT.BORDER | SWT.H_SCROLL | SWT.MULTI);
		GridData gdTmp = new GridData(GridData.FILL_BOTH);
		gdTmp.heightHint = 150;
		gdTmp.horizontalSpan = 2;
		edParams.setLayoutData(gdTmp);
		
		// Dialog-level buttons
		SelectionAdapter OKCancelActions = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				result = false;
				if ( e.widget.getData().equals("h") ) {
					//TODO
				}
				if ( e.widget.getData().equals("o") ) {
					if ( !checkData() ) return;
				}
				shell.close();
			};
		};
		pnlActions = new OKCancelPanel(shell, SWT.NONE, OKCancelActions, true);
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		gdTmp.horizontalSpan = 2;
		pnlActions.setLayoutData(gdTmp);
		shell.setDefaultButton(pnlActions.btOK);

		shell.pack();
		shell.setMinimumSize(shell.getSize());
		Point startSize = shell.getMinimumSize();
		if ( startSize.x < 500 ) startSize.x = 500; 
		shell.setSize(startSize);
		Dialogs.centerWindow(shell, parent);
	}
	
	private boolean checkData () {
		try {
			if ( edSrcLang.getText().length() == 0 ) {
				return false;
			}
			if ( edTrgLang.getText().length() == 0 ) {
				return false;
			}
			resItem.query.setLanguages(LocaleId.fromString(edSrcLang.getText()),
				LocaleId.fromString(edTrgLang.getText()));
			resItem.name = edName.getText();
	
			IParameters params = resItem.query.getParameters();
			if ( params != null ) {
				String tmp = edParams.getText().replace("\r", "");
				params.fromString(tmp);
			}
			result = true;
		}
		catch ( Throwable e ) {
			Dialogs.showError(shell, e.getMessage(), null);
		}
		return result;
	}
	
	public boolean showDialog (ResourceItem resItem) {
		this.resItem = resItem;
		edName.setText(resItem.name);
		edSrcLang.setText(resItem.query.getSourceLanguage().toString());
		edTrgLang.setText(resItem.query.getTargetLanguage().toString());
		
		IParameters params = resItem.query.getParameters();
		if ( params == null ) edParams.setText("");
		else edParams.setText(params.toString());
		edParams.setEditable(params!=null);

		shell.open();
		while ( !shell.isDisposed() ) {
			if ( !shell.getDisplay().readAndDispatch() )
				shell.getDisplay().sleep();
		}
		return result;
	}

}
