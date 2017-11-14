/*===========================================================================
  Copyright (C) 2009-2012 by the Okapi Framework contributors
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

package net.sf.okapi.steps.leveraging.ui;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.IContext;
import net.sf.okapi.common.IHelp;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.IParametersEditor;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.ui.Dialogs;
import net.sf.okapi.common.ui.ISWTEmbeddableParametersEditor;
import net.sf.okapi.common.ui.OKCancelPanel;
import net.sf.okapi.common.ui.TextAndBrowsePanel;
import net.sf.okapi.common.ui.UIUtil;
import net.sf.okapi.lib.ui.translation.ConnectorSelectionPanel;
import net.sf.okapi.lib.ui.translation.DefaultConnectors;
import net.sf.okapi.lib.ui.translation.IConnectorList;
import net.sf.okapi.steps.leveraging.Parameters;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

@EditorFor(Parameters.class)
public class ParametersEditor implements IParametersEditor, ISWTEmbeddableParametersEditor {

	private Shell shell;
	private boolean result = false;
	private OKCancelPanel pnlActions;
	private Parameters params;
	private Button chkLeverage;
	private ConnectorSelectionPanel pnlConnector;
	private IConnectorList connectors;
	private IHelp help;
	private Composite mainComposite;
	private IContext context;
	private Label stNoQueryThreshold;
	private Spinner spnNoQueryThreshold;
	private Label stThreshold;
	private Spinner spnThreshold;
	private Button chkDowngradeIBM;
	private Button chkCopySourceOnNoText;
	private Button chkFillTarget;
	private Button chkFillIfTargetIsEmpty;
	private Button chkFillIfTargetIsSameAsSource;
	private Label stFillTargetThreshold;
	private Spinner spnFillTargetThreshold;
	private Button chkMakeTMX;
	private TextAndBrowsePanel pnlTMXPath;
	private Button chkUseMTPrefix;
	private Button chkUseTargetPrefix;
	private Text edTargetPrefix;
	private Label stTargetPrefixThreshold;
	private Spinner spnTargetPrefixThreshold;
	
	public ParametersEditor () {
		connectors = DefaultConnectors.getConnectors();
	}
	
	public boolean edit (IParameters params,
		boolean readOnly,
		IContext context)
	{
		boolean bRes = false;
		try {
			this.context = context;
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
		this.context = context;
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
		shell.setText("Leveraging");
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
					if ( help != null ) help.showWiki("Leveraging Step");
					return;
				}
				if ( e.widget.getData().equals("o") ) saveData();
				shell.close();
			};
		};
		pnlActions = new OKCancelPanel(shell, SWT.NONE, OKCancelActions, true);
		pnlActions.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		pnlActions.btOK.setEnabled(!readOnly);
		if ( !readOnly ) {
			shell.setDefaultButton(pnlActions.btOK);
		}

		shell.pack();
		shell.setMinimumSize(shell.getSize());
		Point startSize = shell.getMinimumSize();
		if ( startSize.x < 600 ) startSize.x = 600;
		shell.setSize(startSize);
		
		setData();
		Dialogs.centerWindow(shell, parent);
	}

	private void createComposite (Composite parent) {
		mainComposite = new Composite(parent, SWT.BORDER);
		mainComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		mainComposite.setLayout(new GridLayout(2, false));

		chkLeverage = new Button(mainComposite, SWT.CHECK);
		chkLeverage.setText("Leverage the text units");
		GridData gdTmp = new GridData();
		gdTmp.horizontalSpan = 2;
		chkLeverage.setLayoutData(gdTmp);
		chkLeverage.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateOptionsDisplay();
			}
		});
		
		pnlConnector = new ConnectorSelectionPanel(mainComposite, SWT.NONE, connectors, context, null);
		gdTmp = new GridData(GridData.FILL_BOTH);
		gdTmp.horizontalSpan = 2;
		pnlConnector.setLayoutData(gdTmp);
		
		stNoQueryThreshold = new Label(mainComposite, SWT.NONE);
		stNoQueryThreshold.setText("Do not query if there is already a candidate with a score equals to or above:");
		
		spnNoQueryThreshold = new Spinner(mainComposite, SWT.BORDER);
		spnNoQueryThreshold.setMinimum(0);
		spnNoQueryThreshold.setMaximum(101); // Allow 101 to allow to always query
		spnNoQueryThreshold.setIncrement(1);
		spnNoQueryThreshold.setPageIncrement(10);

		stThreshold = new Label(mainComposite, SWT.NONE);
		stThreshold.setText("Accept matches that are equal or above this score:");
		
		spnThreshold = new Spinner(mainComposite, SWT.BORDER);
		spnThreshold.setMinimum(0);
		spnThreshold.setMaximum(100);
		spnThreshold.setIncrement(1);
		spnThreshold.setPageIncrement(10);

		chkDowngradeIBM = new Button(mainComposite, SWT.CHECK);
		chkDowngradeIBM.setText("Downgrade identical best exact matches");
		gdTmp = new GridData();
		gdTmp.horizontalSpan = 2;
		chkDowngradeIBM.setLayoutData(gdTmp);
		
		chkCopySourceOnNoText = new Button(mainComposite, SWT.CHECK);
		chkCopySourceOnNoText.setText("Copy the source into the target if the source content has no text");
		gdTmp = new GridData();
		gdTmp.horizontalSpan = 2;
		chkCopySourceOnNoText.setLayoutData(gdTmp);
		
		chkFillTarget = new Button(mainComposite, SWT.CHECK);
		chkFillTarget.setText("Fill the target with the best translation candidate");
		gdTmp = new GridData();
		gdTmp.horizontalSpan = 2;
		chkFillTarget.setLayoutData(gdTmp);
		chkFillTarget.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				stFillTargetThreshold.setEnabled(chkFillTarget.getSelection());
				spnFillTargetThreshold.setEnabled(chkFillTarget.getSelection());
				chkFillIfTargetIsEmpty.setEnabled(chkFillTarget.getSelection());
				if ( chkFillTarget.getSelection() ) {
					chkFillIfTargetIsSameAsSource.setEnabled(chkFillIfTargetIsEmpty.getSelection());
				}
				else {
					chkFillIfTargetIsSameAsSource.setEnabled(false);
				}
			}
		});
		
		stFillTargetThreshold = new Label(mainComposite, SWT.NONE);
		stFillTargetThreshold.setText("When the best candidate is equal or above this score:");
		gdTmp = new GridData();
		final int indent = 16;
		gdTmp.horizontalIndent = indent;
		stFillTargetThreshold.setLayoutData(gdTmp);
		
		spnFillTargetThreshold = new Spinner(mainComposite, SWT.BORDER);
		spnFillTargetThreshold.setMinimum(0);
		spnFillTargetThreshold.setMaximum(100);
		spnFillTargetThreshold.setIncrement(1);
		spnFillTargetThreshold.setPageIncrement(10);
		
		chkFillIfTargetIsEmpty = new Button(mainComposite, SWT.CHECK);
		chkFillIfTargetIsEmpty.setText("Only if the target is empty");
		gdTmp = new GridData();
		gdTmp.horizontalSpan = 2;
		gdTmp.horizontalIndent = indent;
		chkFillIfTargetIsEmpty.setLayoutData(gdTmp);
		chkFillIfTargetIsEmpty.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				chkFillIfTargetIsSameAsSource.setEnabled(chkFillIfTargetIsEmpty.getSelection());
			}
		});
		
		chkFillIfTargetIsSameAsSource = new Button(mainComposite, SWT.CHECK);
		chkFillIfTargetIsSameAsSource.setText("Or if the target is the same as the source");
		gdTmp = new GridData();
		gdTmp.horizontalSpan = 2;
		gdTmp.horizontalIndent = indent*2;
		chkFillIfTargetIsSameAsSource.setLayoutData(gdTmp);

		chkUseTargetPrefix = new Button(mainComposite, SWT.CHECK);
		chkUseTargetPrefix.setText("Add this prefix to the leveraged translation");
		gdTmp = new GridData();
		gdTmp.horizontalSpan = 2;
		chkUseTargetPrefix.setLayoutData(gdTmp);
		chkUseTargetPrefix.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				edTargetPrefix.setEnabled(chkUseTargetPrefix.getSelection());
				stTargetPrefixThreshold.setEnabled(chkUseTargetPrefix.getSelection());
				spnTargetPrefixThreshold.setEnabled(chkUseTargetPrefix.getSelection());
			}
		});
		
		stTargetPrefixThreshold = new Label(mainComposite, SWT.NONE);
		stTargetPrefixThreshold.setText("When the best candidate is equal or below this score:");
		gdTmp = new GridData();
		gdTmp.horizontalIndent = indent;
		stTargetPrefixThreshold.setLayoutData(gdTmp);

		spnTargetPrefixThreshold = new Spinner(mainComposite, SWT.BORDER);
		spnTargetPrefixThreshold.setMinimum(0);
		spnTargetPrefixThreshold.setMaximum(100);
		spnTargetPrefixThreshold.setIncrement(1);
		spnTargetPrefixThreshold.setPageIncrement(10);

		edTargetPrefix = new Text(mainComposite, SWT.BORDER);
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		gdTmp.horizontalSpan = 2;
		gdTmp.horizontalIndent = indent;
		edTargetPrefix.setLayoutData(gdTmp);
		
		
		chkMakeTMX = new Button(mainComposite, SWT.CHECK);
		chkMakeTMX.setText("Generate a TMX document");
		gdTmp = new GridData();
		gdTmp.horizontalSpan = 2;
		chkMakeTMX.setLayoutData(gdTmp);
		chkMakeTMX.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				pnlTMXPath.setEnabled(chkMakeTMX.getSelection());
				chkUseMTPrefix.setEnabled(chkMakeTMX.getSelection());
			}
		});
		
		pnlTMXPath = new TextAndBrowsePanel(mainComposite, SWT.NONE, false);
		pnlTMXPath.setSaveAs(true);
		pnlTMXPath.setBrowseFilters("TMX Documents (*.tmx)\tAll Files (*.*)", "*.tmx\t*.*");
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		gdTmp.horizontalSpan = 2;
		gdTmp.horizontalIndent = indent;
		pnlTMXPath.setLayoutData(gdTmp);
		
		chkUseMTPrefix = new Button(mainComposite, SWT.CHECK);
		chkUseMTPrefix.setText("If needed, add a MT prefix to the source text");
		gdTmp = new GridData();
		gdTmp.horizontalSpan = 2;
		gdTmp.horizontalIndent = indent;
		chkUseMTPrefix.setLayoutData(gdTmp);
	}
	
	private boolean showDialog () {
		shell.open();
		while ( !shell.isDisposed() ) {
			if ( !shell.getDisplay().readAndDispatch() )
				shell.getDisplay().sleep();
		}
		return result;
	}
	
	private void updateOptionsDisplay () {
		boolean enabled = chkLeverage.getSelection();
		pnlConnector.setEnabled(enabled);
		stNoQueryThreshold.setEnabled(enabled);
		spnNoQueryThreshold.setEnabled(enabled);
		stThreshold.setEnabled(enabled);
		spnThreshold.setEnabled(enabled);
		chkDowngradeIBM.setEnabled(enabled);
		chkCopySourceOnNoText.setEnabled(enabled);
		
		chkFillTarget.setEnabled(enabled);
		if ( enabled ) {
			chkFillIfTargetIsEmpty.setEnabled(chkFillTarget.getSelection());
			if ( chkFillTarget.getSelection() ) {
				chkFillIfTargetIsSameAsSource.setEnabled(chkFillIfTargetIsEmpty.getSelection());
			}
			else {
				chkFillIfTargetIsSameAsSource.setEnabled(false);
			}
		}
		else {
			chkFillIfTargetIsEmpty.setEnabled(false);
			chkFillIfTargetIsSameAsSource.setEnabled(false);
		}
		
		chkUseTargetPrefix.setEnabled(enabled);
		if ( enabled ) {
			edTargetPrefix.setEnabled(chkUseTargetPrefix.getSelection());
			stTargetPrefixThreshold.setEnabled(chkUseTargetPrefix.getSelection());
			spnTargetPrefixThreshold.setEnabled(chkUseTargetPrefix.getSelection());
		}
		else {
			edTargetPrefix.setEnabled(false);
			stTargetPrefixThreshold.setEnabled(false);
			spnTargetPrefixThreshold.setEnabled(false);
		}
		
		chkMakeTMX.setEnabled(enabled);
		if ( enabled ) {
			pnlTMXPath.setEnabled(chkMakeTMX.getSelection());
			chkUseMTPrefix.setEnabled(chkMakeTMX.getSelection());
			stFillTargetThreshold.setEnabled(chkFillTarget.getSelection());
			spnFillTargetThreshold.setEnabled(chkFillTarget.getSelection());
		}
		else {
			pnlTMXPath.setEnabled(false);
			chkUseMTPrefix.setEnabled(false);
			stFillTargetThreshold.setEnabled(false);
			spnFillTargetThreshold.setEnabled(false);
		}
	}

	private void setData () {
		chkLeverage.setSelection(params.getLeverage());
		pnlConnector.setData(params.getResourceClassName(), params.getResourceParameters());
		spnNoQueryThreshold.setSelection(params.getNoQueryThreshold());
		spnThreshold.setSelection(params.getThreshold());
		chkDowngradeIBM.setSelection(params.getDowngradeIdenticalBestMatches());
		chkFillTarget.setSelection(params.getFillTarget());
		chkFillIfTargetIsEmpty.setSelection(params.getFillIfTargetIsEmpty());
		chkFillIfTargetIsSameAsSource.setSelection(params.getFillIfTargetIsSameAsSource());
		spnFillTargetThreshold.setSelection(params.getFillTargetThreshold());
		chkMakeTMX.setSelection(params.getMakeTMX());
		pnlTMXPath.setText(params.getTMXPath());
		chkUseMTPrefix.setSelection(params.getUseMTPrefix());
		chkUseTargetPrefix.setSelection(params.getUseTargetPrefix());
		edTargetPrefix.setText(params.getTargetPrefix());
		spnTargetPrefixThreshold.setSelection(params.getTargetPrefixThreshold());
		chkCopySourceOnNoText.setSelection(params.getCopySourceOnNoText());
		updateOptionsDisplay();
	}

	private boolean saveData () {
		result = false;
		params.setLeverage(chkLeverage.getSelection());
		if ( !chkLeverage.getSelection() ) {
			result = true;
			return true; // Save only that option
		}
		if ( chkMakeTMX.getSelection() ) {
			if ( Util.isEmpty(pnlTMXPath.getText().trim()) ) {
				Dialogs.showError(shell,
					"You must provide a path for the TMX output.", null);
				return false;
			}
		}
		
		if ( chkUseTargetPrefix.getSelection() ) {
			if ( Util.isEmpty(edTargetPrefix.getText().trim()) ) {
				Dialogs.showError(shell,
					"You must provide a target prefix.", null);
				return false;
			}
		}
		
		params.setResourceClassName(pnlConnector.getConnectorClass());
		params.setResourceParameters(pnlConnector.getConnectorParameters());
		params.setNoQueryThreshold(spnNoQueryThreshold.getSelection());
		params.setThreshold(spnThreshold.getSelection());
		params.setDowngradeIdenticalBestMatches(chkDowngradeIBM.getSelection());
		params.setFillTarget(chkFillTarget.getSelection());
		params.setFillIfTargetIsEmpty(chkFillIfTargetIsEmpty.getSelection());
		params.setFillIfTargetIsSameAsSource(chkFillIfTargetIsSameAsSource.getSelection());
		params.setFillTargetThreshold(spnFillTargetThreshold.getSelection());
		params.setMakeTMX(chkMakeTMX.getSelection());
		params.setTMXPath(pnlTMXPath.getText());
		params.setUseMTPrefix(chkUseMTPrefix.getSelection());
		params.setUseTargetPrefix(chkUseTargetPrefix.getSelection());
		params.setTargetPrefix(edTargetPrefix.getText());
		params.setTargetPrefixThreshold(spnTargetPrefixThreshold.getSelection());
		params.setCopySourceOnNoText(chkCopySourceOnNoText.getSelection());
		
		result = true;
		return true;
	}
	
}
