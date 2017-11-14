/*===========================================================================
  Copyright (C) 2008-2010 by the Okapi Framework contributors
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

package net.sf.okapi.applications.rainbow.utilities.alignment;

import net.sf.okapi.applications.rainbow.lib.SegmentationPanel;
import net.sf.okapi.applications.rainbow.lib.Utils;
import net.sf.okapi.common.ConfigurationString;
import net.sf.okapi.common.IContext;
import net.sf.okapi.common.IHelp;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.IParametersEditor;
import net.sf.okapi.common.ui.Dialogs;
import net.sf.okapi.common.ui.OKCancelPanel;
import net.sf.okapi.common.ui.UIUtil;
import net.sf.okapi.tm.simpletm.Database;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

public class Editor implements IParametersEditor {
	
	private Shell shell;
	private boolean result = false;
	private OKCancelPanel pnlActions;
	private Parameters params;
	private Button chkCreateTMX;
	private Text edTMXPath;
	private Button btGetTMXPath;
	private Button chkCreateTMXForUnknown;
	private Text edTMXForUnknownPath;
	private Button btGetTMXForUnknownPath;
	private Button chkUseTradosWorkarounds;
	private Button chkUseExclusion;
	private Text edExclusion;
	private Button chkCreateTM;
	private Text edTMPath;
	private Button btGetTMPath;
	private Button chkCheckSingleSegUnit;
	private Button chkUseAutoCorrection;
	private Button chkCreateAttributes;
	private Text edAttributes;
	private SegmentationPanel pnlSegmentation;
	private boolean inInit = true;
	private IHelp help;
	private String projectDir;
	private Text edMTKey;

	public boolean edit (IParameters p_Options,
		boolean readOnly,
		IContext context)
	{
		boolean bRes = false;
		try {
			shell = null;
			help = (IHelp)context.getObject("help");
			this.projectDir = context.getString("projDir");
			params = (Parameters)p_Options;
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
	
	private void create (Shell parent,
		boolean readOnly)
	{
		shell.setText("ID-Based Alignment");
		if ( parent != null ) UIUtil.inheritIcon(shell, parent);
		GridLayout layTmp = new GridLayout();
		layTmp.marginBottom = 0;
		layTmp.verticalSpacing = 0;
		shell.setLayout(layTmp);

		TabFolder tfTmp = new TabFolder(shell, SWT.NONE);
		tfTmp.setLayoutData(new GridData(GridData.FILL_BOTH));

		//--- Main tab
		
		Composite cmpTmp = new Composite(tfTmp, SWT.NONE);
		cmpTmp.setLayout(new GridLayout());
		TabItem tiTmp = new TabItem(tfTmp, SWT.NONE);
		tiTmp.setText("Options");
		tiTmp.setControl(cmpTmp);

		Group grpTmp = new Group(cmpTmp, SWT.NONE);
		grpTmp.setText("Segmentation");
		grpTmp.setLayout(new GridLayout());
		GridData gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		grpTmp.setLayoutData(gdTmp);
		
		pnlSegmentation = new SegmentationPanel(grpTmp, SWT.NONE,
			"Segment the extracted text using the following SRX rules:", help, projectDir);
		pnlSegmentation.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	
		grpTmp = new Group(cmpTmp, SWT.NONE);
		grpTmp.setText("Verification and Correction");
		grpTmp.setLayout(new GridLayout());
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		grpTmp.setLayoutData(gdTmp);
		
		chkCheckSingleSegUnit = UIUtil.createGridButton(grpTmp, SWT.CHECK,
			"Verify in-line codes for text units with a single segment", -1, 2);
		chkUseAutoCorrection = UIUtil.createGridButton(grpTmp, SWT.CHECK,
			"Use auto-correction automatically", -1, 2);
		
		Label stMT = new Label(grpTmp, SWT.NONE);
		stMT.setText("API key for Google MT (leave empty to not use):");
		
		edMTKey = new Text(grpTmp, SWT.BORDER);
		edMTKey.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		edMTKey.setEchoChar('*');
		
		//--- Output tab
		
		cmpTmp = new Composite(tfTmp, SWT.NONE);
		cmpTmp.setLayout(new GridLayout());
		tiTmp = new TabItem(tfTmp, SWT.NONE);
		tiTmp.setText("Output");
		tiTmp.setControl(cmpTmp);

		// TMX output
		grpTmp = new Group(cmpTmp, SWT.NONE);
		grpTmp.setText("TMX Output");
		grpTmp.setLayout(new GridLayout(2, false));
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		grpTmp.setLayoutData(gdTmp);
		
		chkCreateTMX = new Button(grpTmp, SWT.CHECK);
		chkCreateTMX.setText("Create a TMX document with the aligned entries:");
		gdTmp = new GridData();
		gdTmp.horizontalSpan = 2;
		chkCreateTMX.setLayoutData(gdTmp);
		chkCreateTMX.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateTMXOptions();
			}
		});
		
		edTMXPath = new Text(grpTmp, SWT.BORDER);
		edTMXPath.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		btGetTMXPath = new Button(grpTmp, SWT.PUSH);
		btGetTMXPath.setText("...");
		btGetTMXPath.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				String path = Dialogs.browseFilenamesForSave(shell, "TMX File", null, null,
					"TMX Documents (*.tmx)\tAll Files (*.*)",
					"*.tmx\t*.*");
				Utils.checkProjectDirAfterPick(path, edTMXPath, projectDir);				
			}
		});
		
		chkCreateTMXForUnknown = new Button(grpTmp, SWT.CHECK);
		chkCreateTMXForUnknown.setText("Create a TMX document with the source entries not found:");
		gdTmp = new GridData();
		gdTmp.horizontalSpan = 2;
		chkCreateTMXForUnknown.setLayoutData(gdTmp);
		chkCreateTMXForUnknown.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateTMXOptions();
			}
		});
		
		edTMXForUnknownPath = new Text(grpTmp, SWT.BORDER);
		edTMXForUnknownPath.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		btGetTMXForUnknownPath = new Button(grpTmp, SWT.PUSH);
		btGetTMXForUnknownPath.setText("...");
		btGetTMXForUnknownPath.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				String path = Dialogs.browseFilenamesForSave(shell, "TMX File", null, null,
					"TMX Documents (*.tmx)\tAll Files (*.*)",
					"*.tmx\t*.*");
				Utils.checkProjectDirAfterPick(path, edTMXForUnknownPath, projectDir);				
			}
		});
		
		chkUseTradosWorkarounds = new Button(grpTmp, SWT.CHECK);
		chkUseTradosWorkarounds.setText("Generate Trados workarounds");
		gdTmp = new GridData();
		gdTmp.horizontalSpan = 2;
		chkUseTradosWorkarounds.setLayoutData(gdTmp);
		
		chkUseExclusion = new Button(grpTmp, SWT.CHECK);
		chkUseExclusion.setText("Exclude segments where the source text matches this regular expression:");
		gdTmp = new GridData();
		gdTmp.horizontalSpan = 2;
		chkUseExclusion.setLayoutData(gdTmp);
		chkUseExclusion.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				edExclusion.setEnabled(chkUseExclusion.getSelection());
			}
		});
		
		edExclusion = new Text(grpTmp, SWT.BORDER);
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		gdTmp.horizontalSpan = 2;
		edExclusion.setLayoutData(gdTmp);
		
		// Simple TM output
		grpTmp = new Group(cmpTmp, SWT.NONE);
		grpTmp.setText("SimpleTM Output");
		grpTmp.setLayout(new GridLayout(2, false));
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		grpTmp.setLayoutData(gdTmp);
		
		chkCreateTM = new Button(grpTmp, SWT.CHECK);
		chkCreateTM.setText("Create a SimpleTM database with the following path:");
		gdTmp = new GridData();
		gdTmp.horizontalSpan = 2;
		chkCreateTM.setLayoutData(gdTmp);
		chkCreateTM.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateTMOptions();
			}
		});
		
		edTMPath = new Text(grpTmp, SWT.BORDER);
		edTMPath.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		btGetTMPath = new Button(grpTmp, SWT.PUSH);
		btGetTMPath.setText("...");
		btGetTMPath.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				String path = Dialogs.browseFilenamesForSave(shell, "Simple TM File", null, null,
					"Simple TMs (*"+Database.DATAFILE_EXT+")\tAll Files (*.*)",
					"*"+Database.DATAFILE_EXT+"\t*.*");
				Utils.checkProjectDirAfterPick(path, edTMPath, projectDir);
			}
		});
		
		// Attributes
		grpTmp = new Group(cmpTmp, SWT.NONE);
		grpTmp.setText("Attributes");
		grpTmp.setLayout(new GridLayout());
		gdTmp = new GridData(GridData.FILL_BOTH);
		grpTmp.setLayoutData(gdTmp);
		
		chkCreateAttributes = new Button(grpTmp, SWT.CHECK);
		chkCreateAttributes.setText("Use the following attributes (one per line in the format name=value):");
		chkCreateAttributes.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				edAttributes.setEnabled(chkCreateAttributes.getSelection());
			}
		});
		
		edAttributes = new Text(grpTmp, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		gdTmp = new GridData(GridData.FILL_BOTH);
		gdTmp.heightHint = 100;
		edAttributes.setLayoutData(gdTmp);

		//--- Dialog-level buttons

		SelectionAdapter OKCancelActions = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				result = false;
				if ( e.widget.getData().equals("h") ) {
					if ( help != null ) help.showWiki("Rainbow - ID-Based Alignment");
					return;
				}
				if ( e.widget.getData().equals("o") ) {
					if ( !saveData() ) return;
					else result = true;
				}
				shell.close();
			};
		};
		pnlActions = new OKCancelPanel(shell, SWT.NONE, OKCancelActions, true, "Execute");
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
		inInit = false;
		Dialogs.centerWindow(shell, parent);
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
		pnlSegmentation.setData(params.getSegment(), params.getSourceSrxPath(), params.getTargetSrxPath());
		chkCheckSingleSegUnit.setSelection(params.getCheckSingleSegUnit());
		chkUseAutoCorrection.setSelection(params.getUseAutoCorrection());
		
		chkCreateTMX.setSelection(params.getCreateTMX());
		edTMXPath.setText(params.getTmxPath());
		chkCreateTMXForUnknown.setSelection(params.getCreateTMXForUnknown());
		edTMXForUnknownPath.setText(params.getTmxForUnknownPath());

		edTMPath.setText(params.getTmPath());
		chkUseTradosWorkarounds.setSelection(params.getUseTradosWorkarounds());
		chkUseExclusion.setSelection(params.getUseExclusion());
		edExclusion.setText(params.getExclusion());
		updateTMXOptions();

		chkCreateTM.setSelection(params.getCreateTM());
		edTMPath.setEnabled(chkCreateTM.getSelection());
		btGetTMPath.setEnabled(chkCreateTM.getSelection());
		updateTMOptions();

		chkCreateAttributes.setSelection(params.getCreateAttributes());
		ConfigurationString tmp = new ConfigurationString(params.getAttributes());
		edAttributes.setText(tmp.toString());
		edAttributes.setEnabled(chkCreateAttributes.getSelection());
		
		edMTKey.setText(params.getMtKey());
	}

	private void updateTMXOptions () {
		edTMXPath.setEnabled(chkCreateTMX.getSelection());
		btGetTMXPath.setEnabled(chkCreateTMX.getSelection());
		edTMXForUnknownPath.setEnabled(chkCreateTMXForUnknown.getSelection());
		btGetTMXForUnknownPath.setEnabled(chkCreateTMXForUnknown.getSelection());
		chkUseTradosWorkarounds.setEnabled(chkCreateTMX.getSelection() || chkCreateTMXForUnknown.getSelection());
		edExclusion.setEnabled(chkUseExclusion.getSelection());
	}
	
	private void updateTMOptions () {
		edTMPath.setEnabled(chkCreateTM.getSelection());
		btGetTMPath.setEnabled(chkCreateTM.getSelection());
	}

	private boolean saveData () {
		if ( inInit ) return true;
		
		// Check segmentation info
		boolean segment = pnlSegmentation.getSegment();
		if ( segment ) {
			if ( pnlSegmentation.getSourceSRX().length() == 0 ) {
				Dialogs.showError(shell, "You must specify an SRX document for the source.", null);
				return false;
			}
			if ( pnlSegmentation.getTargetSRX().length() == 0 ) {
				Dialogs.showError(shell, "You must specify an SRX document for the target.", null);
				return false;
			}
		}
		// Check TMX output
		if ( chkCreateTMX.getSelection() ) {
			if ( edTMXPath.getText().length() == 0 ) {
				Dialogs.showError(shell, "You must specify the path of the TMX document.", null);
				return false;
			}
		}
		if ( chkCreateTMXForUnknown.getSelection() ) {
			if ( edTMXForUnknownPath.getText().length() == 0 ) {
				Dialogs.showError(shell, "You must specify the path of the TMX document.", null);
				return false;
			}
		}
		// Check TM output
		if ( chkCreateTM.getSelection() ) {
			if ( edTMPath.getText().length() == 0 ) {
				Dialogs.showError(shell, "You must specify the path of the SimpleTM database.", null);
				return false;
			}
		}
		// Check that we have at least one output
		if ( !chkCreateTMX.getSelection() && !chkCreateTMXForUnknown.getSelection() && !chkCreateTM.getSelection() ) {
			Dialogs.showError(shell, "You must specify at least one output.", null);
			return false;
		}
		
		// Set modified values (after we have checked everything)
		params.setSegment(segment);
		if ( segment ) {
			params.setSourceSrxPath(pnlSegmentation.getSourceSRX());
			params.setTargetSrxPath(pnlSegmentation.getTargetSRX());
		}
		params.setCheckSingleSegUnit(chkCheckSingleSegUnit.getSelection());
		params.setUseAutoCorrection(chkUseAutoCorrection.getSelection());

		params.setCreateTMX(chkCreateTMX.getSelection());
		if ( params.getCreateTMX() ) {
			params.setTmxPath(edTMXPath.getText());
			params.setUseTradosWorkarounds(chkUseTradosWorkarounds.getSelection());
			params.setUseExclusion(chkUseExclusion.getSelection());
			if ( params.getUseExclusion() ) {
				params.setExclusion(edExclusion.getText());
			}
		}

		params.setCreateTMXForUnknown(chkCreateTMXForUnknown.getSelection());
		if ( params.getCreateTMXForUnknown() ) {
			params.setTmxForUnknownPath(edTMXForUnknownPath.getText());
		}
		
		if ( params.getCreateTMX() || params.getCreateTMXForUnknown() ) {
			params.setUseTradosWorkarounds(chkUseTradosWorkarounds.getSelection());
			params.setUseExclusion(chkUseExclusion.getSelection());
			if ( params.getUseExclusion()) {
				params.setExclusion(edExclusion.getText());
			}
		}

		params.setCreateTM(chkCreateTM.getSelection());
		if ( params.getCreateTM() ) {
			params.setTmPath(edTMPath.getText());
		}

		params.setCreateAttributes(chkCreateAttributes.getSelection());
		if ( params.getCreateAttributes()) {
			ConfigurationString tmp = new ConfigurationString(edAttributes.getText());
			params.setAttributes(tmp.toString());
		}
		
		params.setMtKey(edMTKey.getText().trim());

		return true;
	}

}
