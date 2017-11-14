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

package net.sf.okapi.filters.plaintext.ui;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.okapi.common.ui.abstracteditor.IDialogPage;
import net.sf.okapi.common.ui.abstracteditor.SWTUtil;
import net.sf.okapi.lib.extra.filters.CompoundFilterParameters;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

/**
 * 
 * 
 * @version 0.1, 13.06.2009
 */

public class GeneralTab extends Composite implements IDialogPage {

	private static String NONE_ID = "%%%^^^$$$nonenonenone$$$^^^%%%";
	
	private Group extr;
	private Group spliced;
	private Label lblSplicer;
	private Combo splicer;
	private Button codes;
	private Text custom;
	private Group regex;
	private Text edExpression;
	private Spinner edSource;
	private Label lblTextUnitExtraction;
	private Label lblSrcGroup;
	private Button line;
	private Button rule;
	private Label lblSample;
	private Composite composite_2;
	private Text edSample;
	private Text edResult;
	private Pattern fullPattern;
	private boolean busy;
	private Composite composite_3;
	private Button chkDotAll;
	private Button chkIgnoreCase;
	private Button chkMultiline;
	private Button para;
	private Label label;

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public GeneralTab(Composite parent, int style) {
		super(parent, SWT.NONE);
		setLayout(new GridLayout(2, false));
		
		busy = true;
		
		extr = new Group(this, SWT.NONE);
		extr.setLayout(new GridLayout(1, false));
		extr.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		extr.setText("Extraction mode");
		
		para = new Button(extr, SWT.RADIO);
		para.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		para.setText("Extract by paragraphs");
		
		line = new Button(extr, SWT.RADIO);
		line.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		line.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				interop(e.widget);
			}
		});
		line.setText("Extract by lines");
		
		rule = new Button(extr, SWT.RADIO);
		rule.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		rule.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				interop(e.widget);
			}
		});
		rule.setText("Extract with a rule");
		
		spliced = new Group(this, SWT.NONE);
		spliced.setLayout(new GridLayout(2, false));
		spliced.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		spliced.setText("Spliced lines");
		
		lblSplicer = new Label(spliced, SWT.NONE);
		lblSplicer.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		lblSplicer.setAlignment(SWT.RIGHT);
		lblSplicer.setText("Splicer:");
		
		splicer = new Combo(spliced, SWT.NONE);
		splicer.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		splicer.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				interop(e.widget);
			}
		});
		splicer.setItems(new String[] {"None", "Backslash (\\)", "Underscore (_)", "Custom"});
		//comboSplicer.setText(comboSplicer.getItem(0));
		splicer.select(0);
				new Label(spliced, SWT.NONE);
				//		composite_1.setTabList(new Control[]{comboSplicer, btnCreateInlineCodes, textSplicer});
						
						custom = new Text(spliced, SWT.BORDER);
						custom.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
						new Label(spliced, SWT.NONE);
						
						codes = new Button(spliced, SWT.CHECK);
						codes.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
						codes.setText("Create inline codes for splicers");
		
		regex = new Group(this, SWT.NONE);
		regex.setLayout(new GridLayout(1, false));
		regex.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		regex.setText("Extraction rule");
		
		composite_2 = new Composite(regex, SWT.NONE);
		composite_2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		composite_2.setLayout(new GridLayout(5, false));
		
		lblTextUnitExtraction = new Label(composite_2, SWT.NONE);
		lblTextUnitExtraction.setAlignment(SWT.RIGHT);
		lblTextUnitExtraction.setText("Regular expression:");
		
		edExpression = new Text(composite_2, SWT.BORDER);
		edExpression.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateResults();
			}
		});
		edExpression.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		label = new Label(composite_2, SWT.NONE);
		label.setText("    ");
		
		lblSrcGroup = new Label(composite_2, SWT.NONE);
		lblSrcGroup.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblSrcGroup.setAlignment(SWT.RIGHT);
		lblSrcGroup.setText("Source group:");
		
		edSource = new Spinner(composite_2, SWT.BORDER);
		edSource.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateResults();
			}
		});
		new Label(composite_2, SWT.NONE);
		
		lblSample = new Label(composite_2, SWT.NONE);
		lblSample.setAlignment(SWT.RIGHT);
		lblSample.setText("Sample:");
		new Label(composite_2, SWT.NONE);
		new Label(composite_2, SWT.NONE);
		new Label(composite_2, SWT.NONE);
		new Label(composite_2, SWT.NONE);
		
		edSample = new Text(composite_2, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
		edSample.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateResults();
			}
		});
		edSample.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		edSample.setText("");
		new Label(composite_2, SWT.NONE);
		
		composite_3 = new Composite(composite_2, SWT.NONE);
		composite_3.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 2, 1));
		composite_3.setLayout(new GridLayout(1, false));
		
		chkDotAll = new Button(composite_3, SWT.CHECK);
		chkDotAll.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateResults();
			}
		});
		chkDotAll.setText("Dot also matches line-feed");
		chkDotAll.setText("Dot also matches line-feed");
		
		chkIgnoreCase = new Button(composite_3, SWT.CHECK);
		chkIgnoreCase.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateResults();
			}
		});
		chkIgnoreCase.setText("Ignore case difference");
		
		chkMultiline = new Button(composite_3, SWT.CHECK);
		chkMultiline.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateResults();
			}
		});
		chkMultiline.setText("Multi-line");
		new Label(composite_2, SWT.NONE);
		
		edResult = new Text(composite_2, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
		edResult.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		edResult.setEditable(false);
		new Label(composite_2, SWT.NONE);
		new Label(composite_2, SWT.NONE);
		new Label(composite_2, SWT.NONE);
//		composite_2.setTabList(new Control[]{edExpression, edSource, edSample, composite_3, edResult});
		
		busy = false;
				
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	
	private int getRegexOptions () {
		int tmp = 0;
		
		if (chkDotAll == null) return 0;
		if (chkIgnoreCase == null) return 0;
		if (chkMultiline == null) return 0;
		
		if ( chkDotAll.getSelection() ) tmp |= Pattern.DOTALL;
		if ( chkIgnoreCase.getSelection() ) tmp |= Pattern.CASE_INSENSITIVE;
		if ( chkMultiline.getSelection() ) tmp |= Pattern.MULTILINE;
		return tmp;
	}
	
	private void setRegexOptions (int value) {
		
		chkDotAll.setSelection((value & Pattern.DOTALL) == Pattern.DOTALL);
		chkIgnoreCase.setSelection((value & Pattern.CASE_INSENSITIVE) == Pattern.CASE_INSENSITIVE);
		chkMultiline.setSelection((value & Pattern.MULTILINE) == Pattern.MULTILINE);		
	}
	
	
	private String getSampleText() {
		// Change different line breaks type into \n cases
		String tmp = edSample.getText();
		tmp = tmp.replaceAll("\r\r\n", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		tmp = tmp.replace("\r\n", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		return tmp.replace("\r", "\n");  //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	private boolean updateResults () {
		boolean result = true;
		try {
			if (busy) return false;
			
			// Get the values
			fullPattern = Pattern.compile(edExpression.getText(), getRegexOptions());
			//int source = Integer.valueOf(edSource.getText());
			int source = edSource.getSelection();
			
			Matcher m1 = fullPattern.matcher(getSampleText());
			StringBuilder tmp = new StringBuilder();
			
			int startSearch = 0;
			
			while ( m1.find(startSearch) ) {
				
				if ( m1.start() == m1.end() ) break;
				boolean hasGroup = false;
				
				if ( tmp.length() > 0 ) tmp.append("-----\n");
				
				if (source != 0 ) {
					tmp.append("Source=[" + m1.group(source) + "]\n");
					hasGroup = true;
				}
				
				if ( !hasGroup ) tmp.append("Expression=[" + m1.group() + "]\n");
				startSearch = m1.end();
			}
			
			// If there is no match: tell it
			if ( tmp.length() == 0 ) {
				tmp.append("<No match>"); 
			}
			// Display the results
			edResult.setText(tmp.toString());
		}
		catch ( Throwable e ) {
			edResult.setText("Error: " + e.getMessage()); 
			result = false;
		}
		return result;
	}

	public void interop(Widget speaker) {
		
		@SuppressWarnings("unused")
		boolean paraMode = para.getSelection();
		boolean lineMode = line.getSelection();
		boolean ruleMode = rule.getSelection();
		
		SWTUtil.setAllEnabled(regex, ruleMode);
		SWTUtil.setAllEnabled(spliced, lineMode);
		
		if (splicer.getSelectionIndex() == 3 && lineMode) {
		
			custom.setEnabled(true);
			custom.setFocus();
		} 
		else {
			custom.setEnabled(false);
		}				
		
		if (lineMode) {
			
			if (splicer.getSelectionIndex() != 0) {
			
				codes.setEnabled(true);
			} 
			else {
				
				codes.setSelection(false);
				codes.setEnabled(false);
			}
		}
		

//		boolean slEnabled = line.getEnabled();
//		boolean slSelected = line.getSelection();
//		
//		boolean reEnabled = rule.getEnabled();
//		boolean reSelected = rule.getSelection();
//				
//		if (slSelected) {
//			
//			SWTUtil.setAllEnabled(regex, false);
//			SWTUtil.setAllEnabled(spliced, true);
//
////			btnExtractByParagraphs.setSelection(false);
////			btnExtractByParagraphs.setEnabled(false);
//			
//			rule.setSelection(false);
//			rule.setEnabled(false);
//		} else {
//					
//			SWTUtil.setAllEnabled(spliced, false);
//			
//			slEnabled = line.getEnabled(); // Update state
////			if (slEnabled) btnExtractByParagraphs.setEnabled(true);
//			if (slEnabled) rule.setEnabled(true);
//		}
//		
//		if (reSelected) {
//			
//			SWTUtil.setAllEnabled(regex, true);
//			SWTUtil.setAllEnabled(spliced, false);
//			
////			btnExtractByParagraphs.setSelection(false);
////			btnExtractByParagraphs.setEnabled(false);
//			
//			line.setSelection(false);
//			line.setEnabled(false);			
//			
//			edExpression.setFocus();
//		} else {
//						
//			SWTUtil.setAllEnabled(regex, false);
//			
//			reEnabled = rule.getEnabled(); // Update state
////			if (reEnabled) btnExtractByParagraphs.setEnabled(true);
//			if (reEnabled) line.setEnabled(true);
//		}
//	
//		slEnabled = line.getEnabled(); // Update state
//		if (splicer.getSelectionIndex() == 2 && slSelected) {
//							
//			custom.setEnabled(true);
//			custom.setFocus();
//		} else {
//			custom.setEnabled(false);
//		}				
	}
	
	public boolean load(Object data) {
		
		if (data instanceof CompoundFilterParameters) {
			
			CompoundFilterParameters params = (CompoundFilterParameters) data;
			
			Class<?> c = params.getParametersClass();
				
			if (c == net.sf.okapi.filters.plaintext.paragraphs.Parameters.class) {
				
				para.setSelection(true);
				line.setSelection(false);
				rule.setSelection(false);
			}
			else if (c == net.sf.okapi.filters.plaintext.spliced.Parameters.class) {
				
				para.setSelection(false);
				line.setSelection(true);				
				rule.setSelection(false);
			}		
			else if (c == net.sf.okapi.filters.plaintext.regex.Parameters.class) {
				
				para.setSelection(false);
				line.setSelection(false);
				rule.setSelection(true);
			}			
			else {
				
				para.setSelection(false);
				line.setSelection(true);
				rule.setSelection(false);
				splicer.select(0); // None for splicer
			}
			
		} 
		else if (data instanceof net.sf.okapi.filters.plaintext.paragraphs.Parameters) {
			
//			net.sf.okapi.filters.plaintext.paragraphs.Parameters params =
//				(net.sf.okapi.filters.plaintext.paragraphs.Parameters) data;
			
			// para.setSelection(params.extractParagraphs);
		}
		else if (data instanceof net.sf.okapi.filters.plaintext.spliced.Parameters) {
			
			net.sf.okapi.filters.plaintext.spliced.Parameters params =
				(net.sf.okapi.filters.plaintext.spliced.Parameters) data;
			
			if (params.splicer.equals(NONE_ID)) {
				splicer.select(0);
				custom.setText("");
			}
			else if (params.splicer.equals("\\")) {
				splicer.select(1);
				custom.setText("");
			}
			else if (params.splicer.equals("_")) {
				splicer.select(2);
				custom.setText("");
			} 
			else {
				splicer.select(3);
				custom.setText(params.splicer);
			}
			
			codes.setSelection(params.createPlaceholders);
			
		}
		else if (data instanceof net.sf.okapi.filters.plaintext.regex.Parameters) {
		
			net.sf.okapi.filters.plaintext.regex.Parameters params =
				(net.sf.okapi.filters.plaintext.regex.Parameters) data;
			
			edExpression.setText(params.rule);
			edSource.setSelection(params.sourceGroup);
			setRegexOptions(params.regexOptions);
			edSample.setText(params.sample);
		}
	
		return true;
	}

	public boolean save(Object data) {
		
		if (data instanceof CompoundFilterParameters) {
			
			CompoundFilterParameters params = (CompoundFilterParameters) data;
			
			if (para.getSelection())
				params.setParametersClass(net.sf.okapi.filters.plaintext.paragraphs.Parameters.class);

			else if (line.getSelection() && splicer.getSelectionIndex() == 0) // Lines, no splicer 
				params.setParametersClass(net.sf.okapi.filters.plaintext.base.Parameters.class);
			
			else if (line.getSelection() && splicer.getSelectionIndex() != 0) // Lines & splicer
				params.setParametersClass(net.sf.okapi.filters.plaintext.spliced.Parameters.class);
			
			else if (rule.getSelection())
				params.setParametersClass(net.sf.okapi.filters.plaintext.regex.Parameters.class);
					
			else
				params.setParametersClass(net.sf.okapi.filters.plaintext.base.Parameters.class);
		} 		
		else if (data instanceof net.sf.okapi.filters.plaintext.paragraphs.Parameters) {
			
			net.sf.okapi.filters.plaintext.paragraphs.Parameters params =
				(net.sf.okapi.filters.plaintext.paragraphs.Parameters) data;
			
			params.extractParagraphs = true; // For the compound filter always true, for stand-alone a choice 
		}
		else if (data instanceof net.sf.okapi.filters.plaintext.spliced.Parameters) {
			
			net.sf.okapi.filters.plaintext.spliced.Parameters params =
				(net.sf.okapi.filters.plaintext.spliced.Parameters) data;
		
			switch (splicer.getSelectionIndex()) {
			
				case 0:
					params.splicer = NONE_ID;
					break;
					
				case 1:
					params.splicer = "\\";
					break;
					
				case 2:
					params.splicer = "_";
					break;
			
				case 3:
					params.splicer = custom.getText();
					break;				
			}
			
			params.createPlaceholders = codes.getSelection();
			
		}
		else if (data instanceof net.sf.okapi.filters.plaintext.regex.Parameters) {
		
			net.sf.okapi.filters.plaintext.regex.Parameters params =
				(net.sf.okapi.filters.plaintext.regex.Parameters) data;
			
			params.rule = edExpression.getText();
			params.sourceGroup = edSource.getSelection();
			params.regexOptions = getRegexOptions();
			params.sample = edSample.getText();
		}
		
		return true;
	}

	public boolean canClose(boolean isOK) {
		
		return true;
	}

}

