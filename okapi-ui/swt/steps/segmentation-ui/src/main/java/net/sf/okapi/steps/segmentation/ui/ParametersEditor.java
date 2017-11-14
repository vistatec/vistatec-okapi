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

package net.sf.okapi.steps.segmentation.ui;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.IContext;
import net.sf.okapi.common.IHelp;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.IParametersEditor;
import net.sf.okapi.common.ui.Dialogs;
import net.sf.okapi.common.ui.ISWTEmbeddableParametersEditor;
import net.sf.okapi.common.ui.OKCancelPanel;
import net.sf.okapi.common.ui.UIUtil;
import net.sf.okapi.lib.ui.segmentation.SRXEditor;
import net.sf.okapi.steps.segmentation.Parameters;
import net.sf.okapi.steps.segmentation.Parameters.SegmStrategy;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

@EditorFor(Parameters.class)
public class ParametersEditor implements IParametersEditor, ISWTEmbeddableParametersEditor {

	private Shell shell;
	private boolean result = false;
	private OKCancelPanel pnlActions;
	private Parameters params;
	private Button chkSegmentSource;
	private Button btGetSourceSRX;
	private Button btEditSourceSRX;
	private Button chkSegmentTarget;
	private Button btGetTargetSRX;
	private Button btEditTargetSRX;
	private Button chkCopySource;
	private Button chkCheckSegments;
	private Button chkForceSegmentedOutput;
	private Button chkRenumberCodes;
	private Text edSourceSRX;
	private Text edTargetSRX;
	private IHelp help;
	private String projectDir;
	private Composite mainComposite;
	private Group grpOptions;
	private Label lblBehaviorForSegmented;
	private List listBehaviorForSegmented;
	
	public boolean edit (IParameters params,
		boolean readOnly,
		IContext context)
	{
		boolean bRes = false;
		try {
			this.projectDir = context.getString("projDir");
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
		shell.setText("Segmentation");
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
					if ( help != null ) help.showWiki("Segmentation Step");
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
	
	/**
	 * @wbp.parser.entryPoint
	 */
	private void createComposite (Composite parent) {
		mainComposite = new Composite(parent, SWT.BORDER);
		mainComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		mainComposite.setLayout(new GridLayout(3, false));

		chkSegmentSource = new Button(mainComposite, SWT.CHECK);
		chkSegmentSource.setText("Segment the source text using the following SRX rules:");
		GridData gdTmp = new GridData();
		gdTmp.horizontalSpan = 3;
		chkSegmentSource.setLayoutData(gdTmp);
		chkSegmentSource.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateSourceDisplay();
				updateOptionsDisplay();
			}
		});

		edSourceSRX = new Text(mainComposite, SWT.BORDER);
		edSourceSRX.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		
		btGetSourceSRX = new Button(mainComposite, SWT.PUSH);
		btGetSourceSRX.setText("...");
		btGetSourceSRX.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				getSRXFile(edSourceSRX);
			}
		});
		
		btEditSourceSRX = new Button(mainComposite, SWT.PUSH);
		btEditSourceSRX.setText("Edit...");
		btEditSourceSRX.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				editSRXFile(edSourceSRX);
			}
		});
		
		chkSegmentTarget = new Button(mainComposite, SWT.CHECK);
		chkSegmentTarget.setText("Segment existing target text using the following SRX rules:");
		gdTmp = new GridData();
		gdTmp.horizontalSpan = 3;
		chkSegmentTarget.setLayoutData(gdTmp);
		chkSegmentTarget.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateTargetDisplay();
				updateOptionsDisplay();
			}
		});

		edTargetSRX = new Text(mainComposite, SWT.BORDER);
		edTargetSRX.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));

		btGetTargetSRX = new Button(mainComposite, SWT.PUSH);
		btGetTargetSRX.setText("...");
		btGetTargetSRX.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				getSRXFile(edTargetSRX);
			}
		});
		
		btEditTargetSRX = new Button(mainComposite, SWT.PUSH);
		btEditTargetSRX.setText("Edit...");
		btEditTargetSRX.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				editSRXFile(edTargetSRX);
			}
		});
		
		grpOptions = new Group(mainComposite, SWT.NONE);
		grpOptions.setText("Options");
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		gdTmp.horizontalSpan = 3;
		grpOptions.setLayoutData(gdTmp);
		grpOptions.setLayout(new GridLayout(1, false));
		
		lblBehaviorForSegmented = new Label(grpOptions, SWT.NONE);
		lblBehaviorForSegmented.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblBehaviorForSegmented.setText("Behavior if input text is already segmented:");
		
		listBehaviorForSegmented = new List(grpOptions, SWT.BORDER);
		listBehaviorForSegmented.setItems(new String[] {"Keep existing segmentation", "Overwrite existing segmentation (resegment)", "Keep existing segmentation, segment further against the SRX rules"});
		listBehaviorForSegmented.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		chkCopySource = new Button(grpOptions, SWT.CHECK);
		chkCopySource.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		chkCopySource.setText("Copy source into target if no target exists");

		chkCheckSegments = new Button(grpOptions, SWT.CHECK);
		chkCheckSegments.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		chkCheckSegments.setText("Verify that a target segment matches each source segment when a target content exists");
		
		chkForceSegmentedOutput = new Button(grpOptions, SWT.CHECK);
		chkForceSegmentedOutput.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		chkForceSegmentedOutput.setText("When possible force the output to show the segmentation");
		
		chkRenumberCodes = new Button(grpOptions, SWT.CHECK);
		chkRenumberCodes.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		chkRenumberCodes.setText("Renumber code IDs");
	}
		
	private boolean showDialog () {
		shell.open();
		while ( !shell.isDisposed() ) {
			if ( !shell.getDisplay().readAndDispatch() )
				shell.getDisplay().sleep();
		}
		return result;
	}

	private void getSRXFile (Text edTextField) {
		String caption;
		if ( edTextField == edSourceSRX ) caption = "Select SRX for Source";
		else  caption = "Select SRX for Target";
		String[] paths = Dialogs.browseFilenames(shell, caption, false, null,
			"SRX Documents (*.srx)\tAll Files (*.*)",
			"*.srx\t*.*");
		if ( paths == null ) return;
		UIUtil.checkProjectFolderAfterPick(paths[0], edTextField, projectDir);
	}
	
	private void editSRXFile (Text edTextField) {
		try {
			SRXEditor editor = new SRXEditor(shell, true, help);
			String oriPath = edTextField.getText();
			if ( projectDir != null ) {
				oriPath = oriPath.replace("${ProjDir}", projectDir);
			}
			if ( oriPath.length() == 0 ) oriPath = null;
			editor.showDialog(oriPath);
			String newPath = editor.getPath();
			UIUtil.checkProjectFolderAfterPick(newPath, edTextField, projectDir);
		}
		catch ( Throwable e ) {
			Dialogs.showError(shell, e.getMessage(), null);
		}
	}
	
	private void updateSourceDisplay () {
		edSourceSRX.setEnabled(chkSegmentSource.getSelection());
		btEditSourceSRX.setEnabled(chkSegmentSource.getSelection());
		btGetSourceSRX.setEnabled(chkSegmentSource.getSelection());
	}
	
	private void updateTargetDisplay () {
		edTargetSRX.setEnabled(chkSegmentTarget.getSelection());
		btEditTargetSRX.setEnabled(chkSegmentTarget.getSelection());
		btGetTargetSRX.setEnabled(chkSegmentTarget.getSelection());
	}
	
	private void updateOptionsDisplay () {
		boolean enabled = chkSegmentTarget.getSelection() || chkSegmentSource.getSelection();
		grpOptions.setEnabled(enabled);
		for ( Control ctrl : grpOptions.getChildren() ) {
			ctrl.setEnabled(enabled);
		}
	}
	
	private void setData () {
		chkSegmentSource.setSelection(params.getSegmentSource());
		edSourceSRX.setText(params.getSourceSrxPath());
		chkSegmentTarget.setSelection(params.getSegmentTarget());
		edTargetSRX.setText(params.getTargetSrxPath());
		chkCopySource.setSelection(params.getCopySource());
		chkCheckSegments.setSelection(params.getCheckSegments());
		chkForceSegmentedOutput.setSelection(params.getForcesegmentedOutput());
		chkRenumberCodes.setSelection(params.getRenumberCodes());
		listBehaviorForSegmented.setSelection(params.getSegmentationStrategy().ordinal());
		updateSourceDisplay();
		updateTargetDisplay();
		updateOptionsDisplay();
	}

	private boolean saveData () {
		params.setSegmentSource(chkSegmentSource.getSelection());
		if ( params.getSegmentSource()) {
			params.setSourceSrxPath(edSourceSRX.getText());
		}
		params.setSegmentTarget(chkSegmentTarget.getSelection());
		if ( params.getSegmentTarget()) {
			params.setTargetSrxPath(edTargetSRX.getText());
		}
		params.setCopySource(chkCopySource.getSelection());
		params.setCheckSegments(chkCheckSegments.getSelection());
		params.setForcesegmentedOutput(chkForceSegmentedOutput.getSelection());
		params.setRenumberCodes(chkRenumberCodes.getSelection());
		params.setSegmentationStrategy(SegmStrategy.values()[listBehaviorForSegmented.getSelectionIndex()]);
		result = true;
		return true;
	}
	
}
