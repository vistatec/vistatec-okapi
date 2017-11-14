/*===========================================================================
  Copyright (C) 2009-2011 by the Okapi Framework contributors
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

package net.sf.okapi.filters.markdown.ui;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.IContext;
import net.sf.okapi.common.IHelp;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.IParametersEditor;
import net.sf.okapi.common.ui.Dialogs;
import net.sf.okapi.common.ui.OKCancelPanel;
import net.sf.okapi.common.ui.filters.InlineCodeFinderPanel;
import net.sf.okapi.filters.markdown.Parameters;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

@EditorFor(Parameters.class)
public class Editor implements IParametersEditor {

	private Shell shell;
	private boolean result = false;

	private OKCancelPanel pnlActions;
	private Parameters params;
	private Button chkTranslateURLs;
    private Button chkTranslateCodeBlocks;
    private Button chkTranslateHeaderMetadata;
    private Button chkTranslateImageAltText;
	private Button chkUseCodeFinder;
	private InlineCodeFinderPanel pnlCodeFinder;
	private IHelp help;

	public boolean edit (IParameters p_Options,
		boolean readOnly,
		IContext context)
	{
		help = (IHelp)context.getObject("help");
		boolean bRes = false;
		shell = null;
		params = (Parameters)p_Options;
		try {
			shell = new Shell((Shell)context.getObject("shell"), SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.APPLICATION_MODAL);
			create((Shell)context.getObject("shell"), readOnly);
			return showDialog();
		}
		catch ( Exception E ) {
			Dialogs.showError(shell, E.getLocalizedMessage(), null);
			bRes = false;
		}
		finally {
			// Dispose of the shell, but not of the display
			if ( shell != null ) shell.dispose();
		}
		return bRes;
	}

	public IParameters createParameters () {
		return new Parameters();
	}

	private void create (Shell p_Parent,
		boolean readOnly)
	{
		shell.setText(Res.getString("editorCaption"));
		if ( p_Parent != null ) shell.setImage(p_Parent.getImage());
		GridLayout layTmp = new GridLayout();
		layTmp.marginBottom = 0;
		layTmp.verticalSpacing = 0;
		shell.setLayout(layTmp);

		Composite cmpTmp = new Composite(shell, SWT.NONE);
		layTmp = new GridLayout();
		cmpTmp.setLayout(layTmp);

		chkTranslateURLs = new Button(cmpTmp, SWT.CHECK);
		chkTranslateURLs.setText(Res.getString("translateUrls"));

		chkTranslateCodeBlocks = new Button(cmpTmp, SWT.CHECK);
		chkTranslateCodeBlocks.setText(Res.getString("translateCodeBlocks"));

		chkTranslateHeaderMetadata = new Button(cmpTmp, SWT.CHECK);
		chkTranslateHeaderMetadata.setText(Res.getString("translateHeaderMetadata"));

		chkTranslateImageAltText = new Button(cmpTmp, SWT.CHECK);
		chkTranslateImageAltText.setText(Res.getString("translateImageAltText"));

        chkUseCodeFinder = new Button(cmpTmp, SWT.CHECK);
		chkUseCodeFinder.setText(Res.getString("useInlineCodes"));
		chkUseCodeFinder.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateInlineCodes();
			};
		});

		pnlCodeFinder = new InlineCodeFinderPanel(cmpTmp, SWT.NONE);
		pnlCodeFinder.setLayoutData(new GridData(GridData.FILL_BOTH));

		//--- Dialog-level buttons

		SelectionAdapter OKCancelActions = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				result = false;
				if ( e.widget.getData().equals("h") ) {
					if ( help != null ) help.showWiki("PO Filter");
					return;
				}
				if ( e.widget.getData().equals("o") ) {
					if ( !saveData() ) return;
					result = true;
				}
				shell.close();
			};
		};
		pnlActions = new OKCancelPanel(shell, SWT.NONE, OKCancelActions, true);
		GridData gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		pnlActions.setLayoutData(gdTmp);
		pnlActions.btOK.setEnabled(!readOnly);
		if ( !readOnly ) {
			shell.setDefaultButton(pnlActions.btOK);
		}

		shell.pack();
		Rectangle Rect = shell.getBounds();
		shell.setMinimumSize(Rect.width, Rect.height);
		Dialogs.centerWindow(shell, p_Parent);
		setData();
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
		chkTranslateURLs.setSelection(params.getTranslateUrls());
		chkTranslateCodeBlocks.setSelection(params.getTranslateCodeBlocks());
		chkTranslateHeaderMetadata.setSelection(params.getTranslateHeaderMetadata());
		chkTranslateImageAltText.setSelection(params.getTranslateImageAltText());
		chkUseCodeFinder.setSelection(params.getUseCodeFinder());
		pnlCodeFinder.setRules(params.getCodeFinder().toString());

		updateInlineCodes();
		pnlCodeFinder.updateDisplay();
	}

	private boolean saveData () {
		String tmp = pnlCodeFinder.getRules();
		if ( tmp == null ) {
			return false;
		}
		else {
			params.getCodeFinder().fromString(tmp);
		}

		params.setTranslateUrls(chkTranslateURLs.getSelection());
		params.setTranslateCodeBlocks(chkTranslateCodeBlocks.getSelection());
		params.setTranslateHeaderMetadata(chkTranslateHeaderMetadata.getSelection());
		params.setTranslateImageAltText(chkTranslateImageAltText.getSelection());
		params.setUseCodeFinder(chkUseCodeFinder.getSelection());
		return true;
	}

	private void updateInlineCodes () {
		pnlCodeFinder.setEnabled(chkUseCodeFinder.getSelection());
	}

}
