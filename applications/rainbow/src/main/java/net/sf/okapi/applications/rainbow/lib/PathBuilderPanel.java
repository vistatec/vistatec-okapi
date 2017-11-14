/*===========================================================================
  Copyright (C) 2008 by the Okapi Framework contributors
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
============================================================================*/

package net.sf.okapi.applications.rainbow.lib;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class PathBuilderPanel extends Composite {
	
	private int initLevel = 0;
	private Button m_chkUseSubdir;
	private Text m_edSubdir;
	private Button m_chkUseExt;
	private Button m_chkUsePrefix;
	private Button m_chkUseSuffix;
	private Button m_chkUseReplace;
	private Text m_edExt;
	private Button m_rdExtReplace;
	private Button m_rdExtAppend;
	private Button m_rdExtPrepend;
	private Text m_edPrefix;
	private Text m_edSuffix;
	private Text m_edSearch;
	private Text m_edReplace;
	private Text m_edBefore;
	private Text m_edAfter;
	private PathBuilder m_TempPB;
	private String m_sSrcRoot;
	private String m_sTrgRoot;
	private String srcLangStr;
	private String trgLangStr;

	public PathBuilderPanel (Composite p_Parent,
		int p_nFlags)
	{
		super(p_Parent, p_nFlags);
		createContent();
	}

	private void createContent () {
		GridLayout layTmp = new GridLayout(5, false);
		layTmp.marginHeight = 0;
		layTmp.marginWidth = 0;
		setLayout(layTmp);
		
		final ModifyListener MLUpdate = new ModifyListener () {
			public void modifyText(ModifyEvent e) {
				updateSample();
			}
		};
		
		final SelectionAdapter SAUpdate = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateSample();
			};
		};

		m_chkUseSubdir = new Button(this, SWT.CHECK);
		m_chkUseSubdir.setText(Res.getString("PathBuilderPanel.subFolder")); //$NON-NLS-1$
		GridData gdTmp = new GridData();
		gdTmp.horizontalSpan = 2;
		m_chkUseSubdir.setLayoutData(gdTmp);
		m_chkUseSubdir.addSelectionListener(SAUpdate);
		
		m_edSubdir = new Text(this, SWT.BORDER);
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		gdTmp.horizontalSpan = 3;
		m_edSubdir.setLayoutData(gdTmp);
		m_edSubdir.addModifyListener(MLUpdate);
		
		m_chkUseExt = new Button(this, SWT.CHECK);
		m_chkUseExt.setText(Res.getString("PathBuilderPanel.extension")); //$NON-NLS-1$
		gdTmp = new GridData();
		gdTmp.horizontalSpan = 3;
		m_chkUseExt.setLayoutData(gdTmp);
		m_chkUseExt.addSelectionListener(SAUpdate);
		
		m_chkUsePrefix = new Button(this, SWT.CHECK);
		m_chkUsePrefix.setText(Res.getString("PathBuilderPanel.prefix")); //$NON-NLS-1$
		m_chkUsePrefix.addSelectionListener(SAUpdate);
		
		m_chkUseReplace = new Button(this, SWT.CHECK);
		m_chkUseReplace.setText(Res.getString("PathBuilderPanel.replaceThis")); //$NON-NLS-1$
		m_chkUseReplace.addSelectionListener(SAUpdate);
		
		m_edExt = new Text(this, SWT.BORDER);
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		gdTmp.horizontalSpan = 2;
		gdTmp.grabExcessHorizontalSpace = false;
		m_edExt.setLayoutData(gdTmp);
		m_edExt.addModifyListener(MLUpdate);
		
		Composite cmpTmp = new Composite(this, SWT.NONE);
		gdTmp = new GridData();
		gdTmp.verticalSpan = 3;
		cmpTmp.setLayoutData(gdTmp);
		layTmp = new GridLayout();
		layTmp.marginHeight = 0;
		layTmp.marginWidth = 0;
		cmpTmp.setLayout(layTmp);
		m_rdExtReplace = new Button(cmpTmp, SWT.RADIO);
		m_rdExtReplace.setText(Res.getString("PathBuilderPanel.extensionReplace")); //$NON-NLS-1$
		m_rdExtReplace.addSelectionListener(SAUpdate);
		m_rdExtAppend = new Button(cmpTmp, SWT.RADIO);
		m_rdExtAppend.setText(Res.getString("PathBuilderPanel.extensionAppend")); //$NON-NLS-1$
		m_rdExtAppend.addSelectionListener(SAUpdate);
		m_rdExtPrepend = new Button(cmpTmp, SWT.RADIO);
		m_rdExtPrepend.setText(Res.getString("PathBuilderPanel.extensionPrepend")); //$NON-NLS-1$
		m_rdExtPrepend.addSelectionListener(SAUpdate);

		m_edPrefix = new Text(this, SWT.BORDER);
		m_edPrefix.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		m_edPrefix.addModifyListener(MLUpdate);
		
		m_edSearch = new Text(this, SWT.BORDER);
		m_edSearch.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		m_edSearch.addModifyListener(MLUpdate);
		
		new Label(this, SWT.NONE); // place-holder
		new Label(this, SWT.NONE); // place-holder
		
		m_chkUseSuffix = new Button(this, SWT.CHECK);
		m_chkUseSuffix.setText(Res.getString("PathBuilderPanel.suffix")); //$NON-NLS-1$
		m_chkUseSuffix.addSelectionListener(SAUpdate);
		
		Label stTmp = new Label(this, SWT.NONE);
		stTmp.setText(Res.getString("PathBuilderPanel.replaceBy")); //$NON-NLS-1$
		
		new Label(this, SWT.NONE); // place-holder
		new Label(this, SWT.NONE); // place-holder
		
		m_edSuffix = new Text(this, SWT.BORDER);
		m_edSuffix.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		m_edSuffix.addModifyListener(MLUpdate);
		
		m_edReplace = new Text(this, SWT.BORDER);
		m_edReplace.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		m_edReplace.addModifyListener(MLUpdate);
		
		stTmp = new Label(this, SWT.NONE);
		stTmp.setText(Res.getString("PathBuilderPanel.before")); //$NON-NLS-1$
		
		m_edBefore = new Text(this, SWT.BORDER);
		m_edBefore.setEditable(false);
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		gdTmp.horizontalSpan = 4;
		m_edBefore.setLayoutData(gdTmp);
		
		stTmp = new Label(this, SWT.NONE);
		stTmp.setText(Res.getString("PathBuilderPanel.after")); //$NON-NLS-1$

		m_edAfter = new Text(this, SWT.BORDER);
		m_edAfter.setEditable(false);
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		gdTmp.horizontalSpan = 4;
		m_edAfter.setLayoutData(gdTmp);
		
		pack();
	}
	
	public void setTargetRoot (String p_sValue) {
		if (( p_sValue == null ) || ( p_sValue.length() == 0 ))
			m_sTrgRoot = null;
		else
			m_sTrgRoot = p_sValue;
	}
	
	public void setData (PathBuilder p_Data,
		String p_sSrcRoot,
		String p_sSrcPath,
		String p_sTrgRoot,
		String srcLang,
		String trgLang)
	{
		initLevel++;
		m_TempPB = new PathBuilder();
		m_TempPB.copyFrom(p_Data);
		m_sSrcRoot = p_sSrcRoot;
		m_sTrgRoot = p_sTrgRoot;
		this.srcLangStr = srcLang;
		this.trgLangStr = trgLang;
		m_edBefore.setText(p_sSrcPath);
		
		m_chkUseSubdir.setSelection(p_Data.useSubfolder());
		m_edSubdir.setText(p_Data.getSubfolder());

		m_chkUseExt.setSelection(p_Data.useExtension());
		m_edExt.setText(p_Data.getExtension());
		m_rdExtReplace.setSelection(p_Data.getExtensionType()==PathBuilder.EXTTYPE_REPLACE); 
		m_rdExtAppend.setSelection(p_Data.getExtensionType()==PathBuilder.EXTTYPE_APPEND); 
		m_rdExtPrepend.setSelection(p_Data.getExtensionType()==PathBuilder.EXTTYPE_PREPEND); 
		
		m_chkUsePrefix.setSelection(p_Data.usePrefix());
		m_edPrefix.setText(p_Data.getPrefix());
	
		m_chkUseSuffix.setSelection(p_Data.useSuffix());
		m_edSuffix.setText(p_Data.getSuffix());
		
		m_chkUseReplace.setSelection(p_Data.useReplace());
		m_edSearch.setText(p_Data.getSearch());
		m_edReplace.setText(p_Data.getReplace());
		
		initLevel--;
		updateSample();
	}
	
	public void saveData (PathBuilder pathBuilder) {
		pathBuilder.setUseSubfolder(m_chkUseSubdir.getSelection());
		pathBuilder.setSubfolder(m_edSubdir.getText());

		pathBuilder.setUseExtension(m_chkUseExt.getSelection());
		pathBuilder.setExtension(m_edExt.getText());
		if ( m_rdExtReplace.getSelection() ) pathBuilder.setExtensionType(PathBuilder.EXTTYPE_REPLACE);
		else if ( m_rdExtAppend.getSelection() ) pathBuilder.setExtensionType(PathBuilder.EXTTYPE_APPEND);
		else pathBuilder.setExtensionType(PathBuilder.EXTTYPE_PREPEND);
		
		pathBuilder.setUsePrefix(m_chkUsePrefix.getSelection());
		pathBuilder.setPrefix(m_edPrefix.getText());
		
		pathBuilder.setUseSuffix(m_chkUseSuffix.getSelection());
		pathBuilder.setSuffix(m_edSuffix.getText());
		
		pathBuilder.setUseReplace(m_chkUseReplace.getSelection());
		pathBuilder.setSearch(m_edSearch.getText());
		pathBuilder.setReplace(m_edReplace.getText());
	}

	public void setSourceLanguage (String newLang) {
		srcLangStr = newLang;
		updateSample();
	}
	
	public void setTargetLanguage (String newLang) {
		trgLangStr = newLang;
		updateSample();
	}
	
	public void updateSample ()
	{
		if ( initLevel > 0 ) return;
	
		saveData(m_TempPB);
		String sTmp = m_TempPB.getPath(m_edBefore.getText(), m_sSrcRoot, m_sTrgRoot, srcLangStr, trgLangStr);
		m_edAfter.setText(sTmp);
		
		m_edSubdir.setEnabled(m_chkUseSubdir.getSelection());
		m_edPrefix.setEnabled(m_chkUsePrefix.getSelection());
		m_edSuffix.setEnabled(m_chkUseSuffix.getSelection());
		m_edSearch.setEnabled(m_chkUseReplace.getSelection());
		m_edReplace.setEnabled(m_chkUseReplace.getSelection());
		boolean enabled = m_chkUseExt.getSelection();
		m_edExt.setEnabled(enabled);
		m_rdExtAppend.setEnabled(enabled);
		m_rdExtPrepend.setEnabled(enabled);
		m_rdExtReplace.setEnabled(enabled);
	}
}
