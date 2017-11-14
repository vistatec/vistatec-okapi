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

package net.sf.okapi.applications.rainbow.utilities.extraction;

import java.io.File;

import net.sf.okapi.applications.rainbow.lib.SegmentationPanel;
import net.sf.okapi.applications.rainbow.packages.xliff.OptionsEditor;
import net.sf.okapi.applications.rainbow.utilities.BaseUtility;
import net.sf.okapi.common.IContext;
import net.sf.okapi.common.IHelp;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.IParametersEditor;
import net.sf.okapi.common.ui.Dialogs;
import net.sf.okapi.common.ui.OKCancelPanel;
import net.sf.okapi.common.ui.UIUtil;
import net.sf.okapi.lib.ui.translation.ConnectorSelectionPanel;
import net.sf.okapi.lib.ui.translation.DefaultConnectors;
import net.sf.okapi.lib.ui.translation.IConnectorList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

public class Editor implements IParametersEditor {
	
	private Shell shell;
	private boolean result = false;
	private OKCancelPanel pnlActions;
	private Parameters params;
	private List lbTypes;
	private Button btOptions;
	private Text edDescription;
	private Text edName;
	private Text edOutputFolder;
	private Button chkCreateZip;
	private Text edSample;
	private Button chkPreTranslate;
	private Button chkUseFileName;
	private Button chkUseGroupName;
	private Label stThreshold;
	private Spinner spinThreshold;
	private SegmentationPanel pnlSegmentation;
	private boolean inInit = true;
	private IHelp help;
	private String projectDir;
	private IParameters xliffOptions;
	private IContext context;
	private ConnectorSelectionPanel connectorPanel;
	private Button chkUseTransRes2;
	private ConnectorSelectionPanel connectorPanel2;
	private IConnectorList connectors;
	
	public boolean edit (IParameters params,
		boolean readOnly,
		IContext context)
	{
		boolean bRes = false;
		try {
			shell = null;
			help = (IHelp)context.getObject("help");
			this.projectDir = context.getString("projDir");
			this.params = (Parameters)params;
			shell = new Shell((Shell)context.getObject("shell"), SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.APPLICATION_MODAL);
			connectors = DefaultConnectors.getConnectors();
	
			this.context = context;
			create((Shell)context.getObject("shell"), readOnly);
			//this.context = new BaseContext(context.getProperties());
			//this.context.setObject("shell", shell);
			
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
		shell.setText("Translation Package Creation");
		if ( parent != null ) UIUtil.inheritIcon(shell, parent);
		GridLayout layTmp = new GridLayout();
		layTmp.marginBottom = 0;
		layTmp.verticalSpacing = 0;
		shell.setLayout(layTmp);

		TabFolder tfTmp = new TabFolder(shell, SWT.NONE);
		tfTmp.setLayoutData(new GridData(GridData.FILL_BOTH));

		//--- Format tab

		Composite cmpTmp = new Composite(tfTmp, SWT.NONE);
		cmpTmp.setLayout(new GridLayout(2, false));
		TabItem tiTmp = new TabItem(tfTmp, SWT.NONE);
		tiTmp.setText("Package Format");
		tiTmp.setControl(cmpTmp);

		Label stTmp = new Label(cmpTmp, SWT.NONE);
		stTmp.setText("Type of package to create:");
		GridData gdTmp = new GridData();
		gdTmp.horizontalSpan = 2;
		stTmp.setLayoutData(gdTmp);

		lbTypes = new List(cmpTmp, SWT.BORDER);
		lbTypes.add("Generic XLIFF");
		lbTypes.add("OmegaT");
		lbTypes.add("Original + RTF layer");
		// Access the list through key rather than index
		lbTypes.setData("xliff\tomegat\trtf");
		gdTmp = new GridData(GridData.FILL_BOTH);
		gdTmp.heightHint = 70;
		gdTmp.horizontalSpan = 2;
		lbTypes.setLayoutData(gdTmp);
		lbTypes.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updatePackageType();
			}
		});
		
		btOptions = new Button(cmpTmp, SWT.PUSH);
		btOptions.setText("&Options...");
		gdTmp = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		btOptions.setLayoutData(gdTmp);
		UIUtil.ensureWidth(btOptions, 80);
		btOptions.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				editOptions();
			}
		});
		
		edDescription = new Text(cmpTmp, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		edDescription.setEditable(false);
		gdTmp = new GridData(GridData.FILL_BOTH);
		gdTmp.heightHint = 60;
		edDescription.setLayoutData(gdTmp);

		//--- Name tab
		
		cmpTmp = new Composite(tfTmp, SWT.NONE);
		cmpTmp.setLayout(new GridLayout(2, false));
		tiTmp = new TabItem(tfTmp, SWT.NONE);
		tiTmp.setText("Package Location");
		tiTmp.setControl(cmpTmp);

		stTmp = new Label(cmpTmp, SWT.NONE);
		stTmp.setText("Root of the output directory:");
		gdTmp = new GridData();
		gdTmp.horizontalSpan = 2;
		stTmp.setLayoutData(gdTmp);
		
		edOutputFolder = new Text(cmpTmp, SWT.BORDER);
		edOutputFolder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		edOutputFolder.addModifyListener(new ModifyListener () {
			public void modifyText(ModifyEvent e) {
				updateSample();
			}
		});

		Button btGetOutputFolder = new Button(cmpTmp, SWT.PUSH);
		btGetOutputFolder.setText("...");
		btGetOutputFolder.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dlg = new DirectoryDialog(shell);
				dlg.setFilterPath(edOutputFolder.getText());
				String tmp = dlg.open();
				if ( tmp != null ) {
					edOutputFolder.setText(tmp);
					edOutputFolder.selectAll();
					edOutputFolder.setFocus();
				}
			}
		});
		
		stTmp = new Label(cmpTmp, SWT.NONE);
		stTmp.setText("Package name:");
		gdTmp = new GridData();
		gdTmp.horizontalSpan = 2;
		stTmp.setLayoutData(gdTmp);
		
		edName = new Text(cmpTmp, SWT.BORDER);
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		gdTmp.horizontalSpan = 2;
		edName.setLayoutData(gdTmp);
		edName.addModifyListener(new ModifyListener () {
			public void modifyText(ModifyEvent e) {
				updateSample();
			}
		});
		
		stTmp = new Label(cmpTmp, SWT.NONE);
		stTmp.setText("Full path of the output directory:");
		gdTmp = new GridData();
		gdTmp.horizontalSpan = 2;
		stTmp.setLayoutData(gdTmp);
		
		edSample = new Text(cmpTmp, SWT.BORDER);
		edSample.setEditable(false);
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		gdTmp.horizontalSpan = 2;
		edSample.setLayoutData(gdTmp);

		//--- Options tab
		
		cmpTmp = new Composite(tfTmp, SWT.NONE);
		cmpTmp.setLayout(new GridLayout());
		tiTmp = new TabItem(tfTmp, SWT.NONE);
		tiTmp.setText("Options");
		tiTmp.setControl(cmpTmp);

		chkCreateZip = new Button(cmpTmp, SWT.CHECK);
		chkCreateZip.setText("Compress the package into a ZIP file");

		Group grpTmp = new Group(cmpTmp, SWT.NONE);
		grpTmp.setText("Segmentation");
		grpTmp.setLayout(new GridLayout());
		grpTmp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		pnlSegmentation = new SegmentationPanel(grpTmp, SWT.NONE,
			"Pre-segment the extracted text with the following rules:", null, projectDir);
		pnlSegmentation.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		//--- Pre-translation tab

		cmpTmp = new Composite(tfTmp, SWT.NONE);
		cmpTmp.setLayout(new GridLayout(2, false));
		tiTmp = new TabItem(tfTmp, SWT.NONE);
		tiTmp.setText("Pre-Translation");
		tiTmp.setControl(cmpTmp);

		chkPreTranslate = new Button(cmpTmp, SWT.CHECK);
		chkPreTranslate.setText("Pre-translate the extracted text");
		gdTmp = new GridData();
		gdTmp.horizontalSpan = 2;
		chkPreTranslate.setLayoutData(gdTmp);
		chkPreTranslate.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updatePretranslate();
			}
		});
		
		connectorPanel = new ConnectorSelectionPanel(cmpTmp, SWT.NONE, connectors, context,
			"Primary translation resource to use:");
		gdTmp = new GridData(GridData.FILL_BOTH);
		gdTmp.horizontalSpan = 2;
		connectorPanel.setLayoutData(gdTmp);
		
		chkUseFileName = new Button(cmpTmp, SWT.CHECK);
		chkUseFileName.setText("Penalize matches with a FileName attribute different from the document being processed");
		gdTmp = new GridData();
		gdTmp.horizontalSpan = 2;
		chkUseFileName.setLayoutData(gdTmp);

		chkUseGroupName = new Button(cmpTmp, SWT.CHECK | SWT.WRAP);
		chkUseGroupName.setText("Penalize matches with a GroupName attribute different from the group being processed");
		gdTmp = new GridData();
		gdTmp.horizontalSpan = 2;
		chkUseGroupName.setLayoutData(gdTmp);

		stThreshold = new Label(cmpTmp, SWT.NONE);
		stThreshold.setText("Leverage only matches greater or equal to: ");
		
		spinThreshold = new Spinner(cmpTmp, SWT.BORDER);
		spinThreshold.setMaximum(100);
		spinThreshold.setMinimum(0);
		spinThreshold.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		
		chkUseTransRes2 = new Button(cmpTmp, SWT.CHECK);
		chkUseTransRes2.setText("Use a secondary translation resource:");
		gdTmp = new GridData();
		gdTmp.horizontalSpan = 2;
		chkUseTransRes2.setLayoutData(gdTmp);
		chkUseTransRes2.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateUseTransRes2();
			}
		});
		
		connectorPanel2 = new ConnectorSelectionPanel(cmpTmp, SWT.NONE, connectors, context, null);
		gdTmp = new GridData(GridData.FILL_BOTH);
		gdTmp.horizontalSpan = 2;
		connectorPanel2.setLayoutData(gdTmp);
	
		//--- Dialog-level buttons

		SelectionAdapter OKCancelActions = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				result = false;
				if ( e.widget.getData().equals("h") ) {
					if ( help != null ) help.showWiki("Rainbow - Translation Package Creation");
					return;
				}
				if ( e.widget.getData().equals("o") ) {
					if ( !saveData(true) ) return;
					result = true;
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
	
	private void updatePackageType () {
		int n = lbTypes.getSelectionIndex();
		if ( n == -1 ) {
			btOptions.setEnabled(false);
			edDescription.setText("");
			return;
		}
		switch ( n ) {
		case 0: // XLIFF
			btOptions.setEnabled(true);
			edDescription.setText("Simple package where all files to translate are extracted to XLIFF. You can translate this package with any XLIFF editor.");
			break;
		case 1: // OmegaT
			btOptions.setEnabled(false);
			edDescription.setText("OmegaT project with all its files and directory structure in place. You can translate this package with OmegaT.");
			break;
		case 2: // Original + RTF
			btOptions.setEnabled(false);
			edDescription.setText("Package where all the files to translate are converted into an RTF file with Trados-compatible styles. You can translate this package with Trados Translator's Workbench or any compatible tool.");
			break;
		}
	}
	
	private void updateUseTransRes2 () {
		connectorPanel2.setEnabled(chkUseTransRes2.getSelection());
	}
	
	private void updatePretranslate () {
		boolean enabled = chkPreTranslate.getSelection();
		connectorPanel.setEnabled(enabled);
		chkUseFileName.setEnabled(enabled);
		chkUseGroupName.setEnabled(enabled);
		stThreshold.setEnabled(enabled);
		spinThreshold.setEnabled(enabled);
		chkUseTransRes2.setEnabled(enabled);
		if ( enabled ) {
			updateUseTransRes2();
		}
		else {
			connectorPanel2.setEnabled(false);
		}
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
		int n = -1;
		String[] aItems = ((String)lbTypes.getData()).split("\t", -2);
		for ( int i=0; i<aItems.length; i++ ) {
			if ( aItems[i].equals(params.getPkgType()) ) {
				n = i;
				break;
			}
		}
		if ( n < 0 ) n = 0;
		lbTypes.setSelection(n);
		edOutputFolder.setText(params.getOutputFolder());
		edName.setText(params.getPkgName());
		chkCreateZip.setSelection(params.getCreateZip());
		pnlSegmentation.setData(params.getPreSegment(), params.getSourceSRX(), params.getTargetSRX());
		
		chkPreTranslate.setSelection(params.getPreTranslate());
		connectorPanel.setData(params.getTransResClass(), params.getTransResParams());
		
		chkUseFileName.setSelection(params.getUseFileName());
		chkUseGroupName.setSelection(params.getUseGroupName());
		spinThreshold.setSelection(params.getThreshold());

		chkUseTransRes2.setSelection(params.getUseTransRes2());
		connectorPanel2.setData(params.getTransResClass2(), params.getTransResParams2());

		// TODO: This needs to be a clone, not the object itself, or it will get saved on cancel
		xliffOptions = params.getXliffOptions();

		updatePackageType();
		updatePretranslate();
		updateSample();
	}

	private boolean saveData (boolean checkValues) {
		if ( inInit ) return true;
		//TODO: verify options (empty path, etc.
		String tmp = edName.getText();
		if ( checkValues && ( tmp.length() == 0 )) {
			Dialogs.showError(shell, "You must provide a package name.", null);
			edName.setFocus();
			return false;
		}
		params.setPkgName(tmp);

		tmp = edOutputFolder.getText();
		if ( checkValues && ( tmp.length() == 0 )) {
			Dialogs.showError(shell, "You must provide an output directory.", null);
			edOutputFolder.setFocus();
			return false;
		}
		params.setOutputFolder(tmp);
		
		String[] aItems = ((String)lbTypes.getData()).split("\t", -2);
		params.setPkgType(aItems[lbTypes.getSelectionIndex()]);
		params.setCreateZip(chkCreateZip.getSelection());
		params.setPreSegment(pnlSegmentation.getSegment());
		params.setSourceSRX(pnlSegmentation.getSourceSRX());
		params.setTargetSRX(pnlSegmentation.getTargetSRX());

		params.setPreTranslate(chkPreTranslate.getSelection());
		params.setTransResClass(connectorPanel.getConnectorClass());
		params.setTransResParams(connectorPanel.getConnectorParameters());
		
		params.setUseFileName(chkUseFileName.getSelection());
		params.setUseGroupName(chkUseGroupName.getSelection());
		params.setThreshold(spinThreshold.getSelection());

		params.setUseTransRes2(chkUseTransRes2.getSelection());
		params.setTransResClass2(connectorPanel2.getConnectorClass());
		params.setTransResClass(connectorPanel2.getConnectorParameters());

		params.setXliffOptions(xliffOptions);
		return true;
	}
	
	private void updateSample () {
		saveData(false);
		String out = edOutputFolder.getText() + File.separator + edName.getText();
		edSample.setText(out.replace(BaseUtility.VAR_PROJDIR, projectDir));
	}
	
	private void editOptions () {
		int n = lbTypes.getSelectionIndex();
		if ( n == -1 ) return;
		switch ( n ) {
		case 0: // XLIFF
			OptionsEditor dlg = new OptionsEditor();
			context.setObject("shell", shell);
			dlg.edit(xliffOptions, false, context);
			break;
		}
	}

}
