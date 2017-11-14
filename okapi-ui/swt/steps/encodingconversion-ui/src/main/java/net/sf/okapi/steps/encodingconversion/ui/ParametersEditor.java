/*===========================================================================
  Copyright (C) 2008-2009 by the Okapi Framework contributors
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

package net.sf.okapi.steps.encodingconversion.ui;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.IContext;
import net.sf.okapi.common.IHelp;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.IParametersEditor;
import net.sf.okapi.common.ui.Dialogs;
import net.sf.okapi.common.ui.ISWTEmbeddableParametersEditor;
import net.sf.okapi.common.ui.OKCancelPanel;
import net.sf.okapi.common.ui.UIUtil;
import net.sf.okapi.steps.encodingconversion.Parameters;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

@EditorFor(Parameters.class)
public class ParametersEditor implements IParametersEditor, ISWTEmbeddableParametersEditor {
	
	private Shell shell;
	private boolean result = false;
	private OKCancelPanel pnlActions;
	private Parameters params;
	private Button chkUnescapeNCR;
	private Button chkUnescapeCER;     
	private Button chkUnescapeJava;
	private Button rdEscapeToNCRHexaU;
	private Button rdEscapeToNCRHexaL;
	private Button rdEscapeToNCRDeci;
	private Button rdEscapeToCER;
	private Button rdEscapeToJavaU;
	private Button rdEscapeToJavaL;
	private Button rdEscapeToUserFormat;
	private Text edUserFormat;
	private Button rdEscapeUnsupported;
	private Button rdEscapeAll;
	private String formattedOutput;
	private Button chkUseBytes;
	private Button chkBOMonUTF8;
	private Button chkReportUnsupported;
	private IHelp help;
	private Composite mainComposite;

	@Override
	public boolean edit (IParameters params,
		boolean readOnly,
		IContext context)
	{
		boolean bRes = false;
		try {
			shell = null;
			help = (IHelp)context.getObject("help");
			this.params = (Parameters)params;
			shell = new Shell((Shell)context.getObject("shell"), SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.APPLICATION_MODAL);
			create((Shell)context.getObject("shell"), readOnly);
			return showDialog();
		}
		catch ( Exception e ) {
			Dialogs.showError(shell, e.getLocalizedMessage(), null);
			bRes = false;
		}
		finally {
			// Dispose of the shell, but not of the display
			if ( shell != null ) shell.dispose();
		}
		return bRes;
	}
	
	@Override
	public IParameters createParameters () {
		return new Parameters();
	}
	
	@Override
	public Composite getComposite () {
		return mainComposite;
	}

	@Override
	public void initializeEmbeddableEditor (Composite parent,
		IParameters paramsObject,
		IContext context)
	{
		params = (Parameters)paramsObject; 
		shell = (Shell)context.getObject("shell");
		createComposite(parent);
		setData();
	}

	@Override
	public String validateAndSaveParameters () {
		if ( !saveData() ) return null;
		return params.toString();
	}
	
	private void create (Shell parent,
		boolean readOnly)
	{
		shell.setText("Encoding Conversion");
		if ( parent != null ) UIUtil.inheritIcon(shell, parent);
		GridLayout layTmp = new GridLayout();
		layTmp.marginBottom = 0;
		layTmp.verticalSpacing = 0;
		shell.setLayout(layTmp);

		createComposite(shell);

		//--- Dialog-level buttons

		SelectionAdapter OKCancelActions = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				result = false;
				if ( e.widget.getData().equals("h") ) {
					if ( help != null ) help.showWiki("Encoding Conversion Step");
					return;
				}
				if ( e.widget.getData().equals("o") ) {
					if ( !saveData() ) return;
				}
				shell.close();
			};
		};
		pnlActions = new OKCancelPanel(shell, SWT.NONE, OKCancelActions, true);
		pnlActions.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		pnlActions.btOK.setEnabled(!readOnly);
		if ( !readOnly ) {
			shell.setDefaultButton(pnlActions.btOK);
		}

		setData();
		
		shell.pack();
		shell.setMinimumSize(shell.getSize());
		Dialogs.centerWindow(shell, parent);
	}
	
	private void createComposite (Composite parent) {
		mainComposite = new Composite(parent, SWT.NONE);
		mainComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layTmp = new GridLayout();
		layTmp.marginHeight = 0;
		layTmp.marginWidth = 0;
		mainComposite.setLayout(layTmp);
		
		TabFolder tfTmp = new TabFolder(mainComposite, SWT.NONE);
		tfTmp.setLayoutData(new GridData(GridData.FILL_BOTH));

		//--- Input tab

		Composite cmpTmp = new Composite(tfTmp, SWT.NONE);
		cmpTmp.setLayout(new GridLayout());
		TabItem tiTmp = new TabItem(tfTmp, SWT.NONE);
		tiTmp.setText("Input");
		tiTmp.setControl(cmpTmp);

		Group group = new Group(cmpTmp, SWT.NONE);
		group.setLayout(new GridLayout());
		group.setText("Un-escape the following notations");
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		chkUnescapeNCR = new Button(group, SWT.CHECK);
		chkUnescapeNCR.setText("Numeric character references (&&#225; or &&#xE1; or &&&xe1; --> \u00e1)");
		
		chkUnescapeCER = new Button(group, SWT.CHECK);
		chkUnescapeCER.setText("Character entity references (&&aacute; --> \u00e1)");
		
		chkUnescapeJava = new Button(group, SWT.CHECK);
		chkUnescapeJava.setText("Java-style escape notation (\\u00E1 or \\u00e1 --> \u00e1)");
		
		//--- Output tab

		cmpTmp = new Composite(tfTmp, SWT.NONE);
		cmpTmp.setLayout(new GridLayout());
		tiTmp = new TabItem(tfTmp, SWT.NONE);
		tiTmp.setText("Output");
		tiTmp.setControl(cmpTmp);

		group = new Group(cmpTmp, SWT.NONE);
		group.setLayout(new GridLayout());
		group.setText("What characters should be escaped");
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		rdEscapeUnsupported = new Button(group, SWT.RADIO);
		rdEscapeUnsupported.setText("Only the characters un-supported by the output encoding");
		
		rdEscapeAll = new Button(group, SWT.RADIO);
		rdEscapeAll.setText("All extended characters");

		group = new Group(cmpTmp, SWT.NONE);
		group.setLayout(new GridLayout());
		group.setText("Escape notation to use");
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		rdEscapeToNCRHexaU = new Button(group, SWT.RADIO);
		rdEscapeToNCRHexaU.setText("Uppercase hexadecimal numeric character reference (\u00e1 --> &&#xE1;)");
		
		rdEscapeToNCRHexaL = new Button(group, SWT.RADIO);
		rdEscapeToNCRHexaL.setText("Lowercase hexadecimal numeric character reference (\u00e1 --> &&#xe1;)");
		
		rdEscapeToNCRDeci = new Button(group, SWT.RADIO);
		rdEscapeToNCRDeci.setText("Decimal numeric character reference (\u00e1 --> &&#224;)");
		
		rdEscapeToCER = new Button(group, SWT.RADIO);
		rdEscapeToCER.setText("Character entity reference (\u00e1 --> &&aacute;)");
		
		rdEscapeToJavaU = new Button(group, SWT.RADIO);
		rdEscapeToJavaU.setText("Uppercase Java-style notation (\u00e1 --> \\u00E1)");
		
		rdEscapeToJavaL = new Button(group, SWT.RADIO);
		rdEscapeToJavaL.setText("Lowrcase Java-style notation (\u00e1 --> \\u00e1)");

		formattedOutput = "User-defined notation (\u00e1 --> %s)";
		rdEscapeToUserFormat = new Button(group, SWT.RADIO);
		rdEscapeToUserFormat.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		rdEscapeToUserFormat.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				edUserFormat.setEnabled(rdEscapeToUserFormat.getSelection());
				chkUseBytes.setEnabled(rdEscapeToUserFormat.getSelection());
			}
		});
		
		edUserFormat = new Text(group, SWT.BORDER);
		GridData gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		gdTmp.horizontalIndent = 16;
		edUserFormat.setLayoutData(gdTmp);
		edUserFormat.addModifyListener(new ModifyListener () {
			public void modifyText(ModifyEvent e) {
				updateUserOutput();
			}
		});
		
		chkUseBytes = new Button(group, SWT.CHECK);
		chkUseBytes.setText("Use the byte values");
		gdTmp = new GridData();
		gdTmp.horizontalIndent = 16;
		chkUseBytes.setLayoutData(gdTmp);
		
		group = new Group(cmpTmp, SWT.NONE);
		group.setLayout(new GridLayout());
		group.setText("Miscellaneous");
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		chkBOMonUTF8 = new Button(group, SWT.CHECK);
		chkBOMonUTF8.setText("Use Byte-Order-Mark for UTF-8 output");
		
		chkReportUnsupported = new Button(group, SWT.CHECK);
		chkReportUnsupported.setText("List characters not supported by the output encoding");
	}

	private void updateUserOutput () {
		String tmp = edUserFormat.getText();
		if ( tmp.length() == 0 ) tmp = "?";
		else {
			try {
				tmp = String.format(tmp, (int)0x00e1);
			}
			catch ( Exception e ) {
				tmp = "<!ERROR!>";
			}
		}
		rdEscapeToUserFormat.setText(String.format(formattedOutput, tmp));
	}
	
	private boolean showDialog () {
		shell.open();
		while ( !shell.isDisposed() ) {
			if ( !shell.getDisplay().readAndDispatch() )
				shell.getDisplay().sleep();
		}
		return result;
	}

	private void setData () {
		chkUnescapeNCR.setSelection(params.getUnescapeNCR());
		chkUnescapeCER.setSelection(params.getUnescapeCER());
		chkUnescapeJava.setSelection(params.getUnescapeJava());

		switch ( params.getEscapeNotation()) {
		case Parameters.ESCAPE_NCRDECI:
			rdEscapeToNCRDeci.setSelection(true);
			break;
		case Parameters.ESCAPE_CER:
			rdEscapeToCER.setSelection(true);
			break;
		case Parameters.ESCAPE_JAVAU:
			rdEscapeToJavaU.setSelection(true);
			break;
		case Parameters.ESCAPE_JAVAL:
			rdEscapeToJavaL.setSelection(true);
			break;
		case Parameters.ESCAPE_USERFORMAT:
			rdEscapeToUserFormat.setSelection(true);
			break;
		case Parameters.ESCAPE_NCRHEXAL:
			rdEscapeToNCRHexaL.setSelection(true);
			break;
		case Parameters.ESCAPE_NCRHEXAU:
		default:
			rdEscapeToNCRHexaU.setSelection(true);
			break;
		}
		edUserFormat.setText(params.getUserFormat());
		chkUseBytes.setSelection(params.getUseBytes());
		rdEscapeAll.setSelection(params.getEscapeAll());
		rdEscapeUnsupported.setSelection(!params.getEscapeAll());
		chkBOMonUTF8.setSelection(params.getBOMonUTF8());
		chkReportUnsupported.setSelection(params.getReportUnsupported());
		
		updateUserOutput();
		edUserFormat.setEnabled(rdEscapeToUserFormat.getSelection());
		chkUseBytes.setEnabled(rdEscapeToUserFormat.getSelection());
	}

	private boolean saveData () {
		params.setUnescapeNCR(chkUnescapeNCR.getSelection());
		params.setUnescapeCER(chkUnescapeCER.getSelection());
		params.setUnescapeJava(chkUnescapeJava.getSelection());
		params.setEscapeAll(rdEscapeAll.getSelection());
		params.setEscapeNotation(getEscapeNotation());
		String tmp = edUserFormat.getText();
		//TODO: check format
		params.setUserFormat(tmp);
		params.setUseBytes(chkUseBytes.getSelection());
		params.setBOMonUTF8(chkBOMonUTF8.getSelection());
		params.setReportUnsupported(chkReportUnsupported.getSelection());
		result = true;
		return result;
	}
	
	int getEscapeNotation () {
		if ( rdEscapeToNCRHexaL.getSelection() )
			return Parameters.ESCAPE_NCRHEXAL;
		if ( rdEscapeToCER.getSelection() )
			return Parameters.ESCAPE_CER;
		if ( rdEscapeToJavaL.getSelection() )
			return Parameters.ESCAPE_JAVAL;
		if ( rdEscapeToJavaU.getSelection() )
			return Parameters.ESCAPE_JAVAU;
		if ( rdEscapeToNCRDeci.getSelection() )
			return Parameters.ESCAPE_NCRDECI;
		if ( rdEscapeToUserFormat.getSelection() )
			return Parameters.ESCAPE_USERFORMAT;
		// Else and if ( rdEscapeToNCRHexaU.getSelection() )
		return Parameters.ESCAPE_NCRHEXAU;
	}

}
