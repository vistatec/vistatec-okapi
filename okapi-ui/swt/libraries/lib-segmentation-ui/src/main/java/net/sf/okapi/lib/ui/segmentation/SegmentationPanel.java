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

package net.sf.okapi.lib.ui.segmentation;

import net.sf.okapi.common.IHelp;
import net.sf.okapi.common.ui.Dialogs;
import net.sf.okapi.common.ui.UIUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Implements a composite panel to select source and target segmentation files.
 */
public class SegmentationPanel extends Composite {

	private Button chkSegment;
	private Label stSourceSRX;
	private Text edSourceSRX;
	private Button btGetSourceSRX;
	private Button btEditSourceSRX;
	private Label stTargetSRX;
	private Text edTargetSRX;
	private Button btGetTargetSRX;
	private Button btEditTargetSRX;
	private IHelp help;
	private String projectDir;

	public SegmentationPanel (Composite p_Parent,
		int p_nFlags,
		String segmentCaption,
		IHelp helpParam,
		String projectDir)
	{
		super(p_Parent, SWT.NONE);
		help = helpParam;
		this.projectDir = projectDir;
		createContent(segmentCaption);
	}
	
	private void createContent (String segmentCaption) {
		GridLayout layTmp = new GridLayout(4, false);
		layTmp.marginHeight = 0;
		layTmp.marginWidth = 0;
		setLayout(layTmp);

		chkSegment = new Button(this, SWT.CHECK);
		chkSegment.setText(segmentCaption);
		GridData gdTmp = new GridData();
		gdTmp.horizontalSpan = 4;
		chkSegment.setLayoutData(gdTmp);
		chkSegment.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateDisplay();
			}
		});

		stSourceSRX = new Label(this, SWT.NONE);
		stSourceSRX.setText("SRX file for the source:");
		
		edSourceSRX = new Text(this, SWT.BORDER);
		edSourceSRX.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		btGetSourceSRX = new Button(this, SWT.PUSH);
		btGetSourceSRX.setText("...");
		btGetSourceSRX.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				getSRXFile(edSourceSRX);
			}
		});
		
		btEditSourceSRX = new Button(this, SWT.PUSH);
		btEditSourceSRX.setText("Edit...");
		btEditSourceSRX.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				editSRXFile(edSourceSRX);
			}
		});
		
		stTargetSRX = new Label(this, SWT.NONE);
		stTargetSRX.setText("SRX file for the target:");
		
		edTargetSRX = new Text(this, SWT.BORDER);
		edTargetSRX.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		btGetTargetSRX = new Button(this, SWT.PUSH);
		btGetTargetSRX.setText("...");
		btGetTargetSRX.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				getSRXFile(edTargetSRX);
			}
		});
		
		btEditTargetSRX = new Button(this, SWT.PUSH);
		btEditTargetSRX.setText("Edit...");
		btEditTargetSRX.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				editSRXFile(edTargetSRX);
			}
		});

	}
	
	private void getSRXFile (Text edTextField) {
		String caption;
		if ( edTextField == edSourceSRX ) caption = "Select SRX for Source";
		else  caption = "Select SRX for Target";
		String[] paths = Dialogs.browseFilenames(getShell(), caption, false, null,
			"SRX Documents (*.srx)\tAll Files (*.*)",
			"*.srx\t*.*");
		if ( paths == null ) return;
		UIUtil.checkProjectFolderAfterPick(paths[0], edTextField, projectDir);
	}
	
	private void editSRXFile (Text edTextField) {
		try {
			SRXEditor editor = new SRXEditor(getShell(), true, help);
			String oriPath = edTextField.getText().replace("${ProjDir}", projectDir);
			if ( oriPath.length() == 0 ) oriPath = null;
			editor.showDialog(oriPath);
			String newPath = editor.getPath();
			UIUtil.checkProjectFolderAfterPick(newPath, edTextField, projectDir);
		}
		catch ( Throwable e ) {
			Dialogs.showError(getShell(), e.getMessage(), null);
		}
	}
	
	private void updateDisplay () {
		boolean enabled = chkSegment.getSelection();
		stSourceSRX.setEnabled(enabled);
		edSourceSRX.setEnabled(enabled);
		btGetSourceSRX.setEnabled(enabled);
		btEditSourceSRX.setEnabled(enabled);
		stTargetSRX.setEnabled(enabled);
		edTargetSRX.setEnabled(enabled);
		btGetTargetSRX.setEnabled(enabled);
		btEditTargetSRX.setEnabled(enabled);
	}
	
	public void setData (boolean segment,
		String sourceSRX,
		String targetSRX)
	{
		chkSegment.setSelection(segment);
		edSourceSRX.setText(sourceSRX);
		edTargetSRX.setText(targetSRX);
		updateDisplay();
	}

	public boolean getSegment () {
		return chkSegment.getSelection();
	}
	
	public String getSourceSRX () {
		return edSourceSRX.getText();
	}
	
	public String getTargetSRX () {
		return edTargetSRX.getText();
	}
}
