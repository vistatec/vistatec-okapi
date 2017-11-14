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

package net.sf.okapi.filters.openxml.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.IContext;
import net.sf.okapi.common.IHelp;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.IParametersEditor;
import net.sf.okapi.common.ui.Dialogs;
import net.sf.okapi.common.ui.OKCancelPanel;
import net.sf.okapi.common.ui.UIUtil;
import net.sf.okapi.filters.openxml.ConditionalParameters;
import net.sf.okapi.filters.openxml.Excell;
import net.sf.okapi.filters.openxml.ParseType;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

@EditorFor(ConditionalParameters.class)
@SuppressWarnings({"rawtypes", "unused"})
public class Editor implements IParametersEditor {

	private Shell shell;
	private IContext context;
	private boolean readOnly = false;
	private boolean result = false;
	private ConditionalParameters params;
	private IHelp help;	
	private Button btnHelp;
	private Button btnOk;
	private Button btnCancel;
	private Button btnTranslateDocumentProperties;
	private Button btnTranslateComments;
	private Button btnCleanAggressively;
	private Button btnTreatTabAsChar;
	private Button btnTreatLineBreakAsChar;
	private Button btnTranslateHeadersAndFooters;
	private Button btnTranslateHiddenText;
	private Button btnExcludeGraphicMetadata;
	private Button btnAutomaticallyAcceptRevisions;
	private Button btnIgnoreSoftHyphen;
    private Button btnReplaceNonBreakingHyphen;
	private Button btnStylesFromDocument;
	private Button btnColorsFromDocument;
	private Button btnExcludeExcelColumns;
	private Button btnTranslateHiddenCells;
	private Button btnTranslateSheetNames;
	private Button btnTranslateDiagramData;
	private Button btnTranslateDrawings;
	private Button btnExtractExternalHyperlinks;
	private List listExcludedWordStyles;
	private List listTranslatableFields;
	private List listExcelColorsToExclude;
	private List listExcelSheet1ColumnsToExclude;
	private List listExcelSheet2ColumnsToExclude;
	private List listExcelSheet3ColumnsToExclude;
	private Button btnTranslateNotes;
	private Button btnTranslateMasters;
	private Button btnIgnorePlaceholdersInMasters;
	private Button btnIncludedSlideNumbersOnly;
	private List listPowerpointIncludedSlideNumbers;

	public boolean edit (IParameters options,
		boolean readOnly,
		IContext context)
	{
		this.context = context;
		this.readOnly = readOnly;
		help = (IHelp)context.getObject("help");
		boolean bRes = false;
		shell = null;
		params = (ConditionalParameters)options;
		try {
			createContents();			
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
		return new ConditionalParameters();
	}
	
	/**
	 * @wbp.parser.entryPoint
	 * Create contents of the dialog.
	 */	
	protected void createContents() { // DWH 6-17-09 was private
		Shell parent = (Shell)context.getObject("shell");
		shell = new Shell(parent, SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.APPLICATION_MODAL);
		shell.setText("Office 2007 Filter Parameters");
		if ( parent != null ) UIUtil.inheritIcon(shell, parent);
		GridLayout layTmp = new GridLayout();
		layTmp.marginBottom = 0;
		layTmp.verticalSpacing = 0;
		shell.setLayout(layTmp);

		TabFolder tabFolder = new TabFolder(shell, SWT.NONE);
		//tabFolder.setLayoutData(BorderLayout.CENTER);
		GridData gdTmp = new GridData(GridData.FILL_BOTH);
		tabFolder.setLayoutData(gdTmp);
		{
			TabItem tbtmGeneralOptions_1 = new TabItem(tabFolder, SWT.NONE);
			tbtmGeneralOptions_1.setText("General Options");
			{
				Composite composite = new Composite(tabFolder, SWT.NONE);
				tbtmGeneralOptions_1.setControl(composite);
				composite.setLayout(new GridLayout(1, false));
				{
					btnTranslateDocumentProperties = new Button(composite, SWT.CHECK);
					btnTranslateDocumentProperties.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
					btnTranslateDocumentProperties.setSelection(true);
					btnTranslateDocumentProperties.setText("Translate Document Properties");
				}
				{
					btnTranslateComments = new Button(composite, SWT.CHECK);
					btnTranslateComments.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
					btnTranslateComments.setSelection(true);
					btnTranslateComments.setText("Translate Comments");
				}
				{
					btnCleanAggressively = new Button(composite, SWT.CHECK);
					btnCleanAggressively.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
					btnCleanAggressively.setSelection(true);
					btnCleanAggressively.setText("Clean Tags Aggressively");
				}
				{
                    btnTreatTabAsChar = new Button(composite, SWT.CHECK);
                    btnTreatTabAsChar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                    btnTreatTabAsChar.setSelection(false);
                    btnTreatTabAsChar.setText("Treat Tab as Character");
                }
				{
                    btnTreatLineBreakAsChar = new Button(composite, SWT.CHECK);
                    btnTreatLineBreakAsChar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                    btnTreatLineBreakAsChar.setSelection(false);
                    btnTreatLineBreakAsChar.setText("Treat Line Break as Character");
                }
			}
		}
		{
			TabItem tbtmWordOptions = new TabItem(tabFolder, SWT.NONE);
			tbtmWordOptions.setText("Word Options");
			{
				Composite composite = new Composite(tabFolder, SWT.NONE);
				tbtmWordOptions.setControl(composite);
				composite.setLayout(new GridLayout(2, false));
				Composite leftOptions = new Composite(composite, SWT.NONE);
				leftOptions.setLayout(new GridLayout());
				leftOptions.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
				{
					btnTranslateHeadersAndFooters = new Button(leftOptions, SWT.CHECK);
					btnTranslateHeadersAndFooters.setSelection(true);
					btnTranslateHeadersAndFooters.setText("Translate Headers and Footers");
				}
				{
					btnTranslateHiddenText = new Button(leftOptions, SWT.CHECK);
					btnTranslateHiddenText.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
					btnTranslateHiddenText.setSelection(true);
					btnTranslateHiddenText.setText("Translate Hidden Text");
				}
				{
					btnExcludeGraphicMetadata = new Button(leftOptions, SWT.CHECK);
					btnExcludeGraphicMetadata.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
					btnExcludeGraphicMetadata.setSelection(false);
					btnExcludeGraphicMetadata.setText("Exclude Graphical Metadata");
				}
				{
				    btnAutomaticallyAcceptRevisions = new Button(leftOptions, SWT.CHECK);
				    btnAutomaticallyAcceptRevisions.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
				    btnAutomaticallyAcceptRevisions.setSelection(true);
				    btnAutomaticallyAcceptRevisions.setText("Automatically Accept Revisions");
                }
				{
                    btnIgnoreSoftHyphen = new Button(leftOptions, SWT.CHECK);
                    btnIgnoreSoftHyphen.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
                    btnIgnoreSoftHyphen.setSelection(false);
                    btnIgnoreSoftHyphen.setText("Ignore Soft Hyphens");
                }
                {
                    btnReplaceNonBreakingHyphen = new Button(leftOptions, SWT.CHECK);
                    btnReplaceNonBreakingHyphen.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
                    btnReplaceNonBreakingHyphen.setSelection(false);
                    btnReplaceNonBreakingHyphen.setText("Replace Non-Breaking Hyphen with Regular Hyphen");
                }
				{
					btnExtractExternalHyperlinks = new Button(leftOptions, SWT.CHECK);
					btnExtractExternalHyperlinks.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
					btnExtractExternalHyperlinks.setSelection(false);
					btnExtractExternalHyperlinks.setText("Translate Hyperlink URLs");
				}
				{
					Label lblStyles = new Label(leftOptions, SWT.NONE);
					lblStyles.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER,
							true, false, 1, 1));
					lblStyles.setText("Translatable Fields:");
				}
				{
					listTranslatableFields = new List(leftOptions, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
					GridData gd_listTranslatableFields = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
					gd_listTranslatableFields.heightHint = 40;
					listTranslatableFields.setLayoutData(gd_listTranslatableFields);
					listTranslatableFields.setItems(new String[]{"HYPERLINK", "FORMTEXT", "TOC"});
				}
				Composite rightOptions = new Composite(composite, SWT.NONE);
				rightOptions.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
				rightOptions.setLayout(new GridLayout());
				{
					Label lblStyles = new Label(rightOptions, SWT.NONE);
					lblStyles.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
					lblStyles.setText("Styles to Exclude:");
				}
				{
					listExcludedWordStyles = new List(rightOptions, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
					GridData gd_listExcludedWordStyles = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
					gd_listExcludedWordStyles.heightHint = 40;
					listExcludedWordStyles.setLayoutData(gd_listExcludedWordStyles);
					listExcludedWordStyles.setItems(new String[] {"Emphasis", "ExcludeCharacterStyle", "ExcludeParagraphStyle", "Heading1", "Heading2", "Normal", "Title", "Strong", "Subtitle", "tw4winExternal"});
				}
			}
		}
		{
			TabItem tbtmExcelOptions = new TabItem(tabFolder, SWT.NONE);
			tbtmExcelOptions.setText("Excel Options");
			{
				Composite composite = new Composite(tabFolder, SWT.NONE);
				tbtmExcelOptions.setControl(composite);
				composite.setLayout(new GridLayout(2, false));
				{
					btnTranslateHiddenCells = new Button(composite, SWT.CHECK);
					btnTranslateHiddenCells.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
					btnTranslateHiddenCells.setSelection(false);
					btnTranslateHiddenCells.setText("Translate Hidden Rows and Columns");
				}
				new Label(composite, SWT.NONE);
				{
					btnExcludeExcelColumns = new Button(composite, SWT.CHECK);
					btnExcludeExcelColumns.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
					btnExcludeExcelColumns.setSelection(true);
					btnExcludeExcelColumns.setText("Exclude Marked Columns in Each Sheet");
				}
				new Label(composite, SWT.NONE);
				{
					btnTranslateSheetNames = new Button(composite, SWT.CHECK);
					btnTranslateSheetNames.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
					btnTranslateSheetNames.setSelection(true);
					btnTranslateSheetNames.setText("Translate Sheet Names");
				}
				new Label(composite, SWT.NONE);
				{
					btnTranslateDiagramData = new Button(composite, SWT.CHECK);
					btnTranslateDiagramData.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
					btnTranslateDiagramData.setSelection(true);
					btnTranslateDiagramData.setText("Translate Diagram Data (e.g. Smart Art)");
				}
				new Label(composite, SWT.NONE);
				{
					btnTranslateDrawings = new Button(composite, SWT.CHECK);
					btnTranslateDrawings.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
					btnTranslateDrawings.setSelection(true);
					btnTranslateDrawings.setText("Translate Drawings (e.g. Text fields)");
				}
				new Label(composite, SWT.NONE);
				{
					Label lblColorsToExclude = new Label(composite, SWT.NONE);
					lblColorsToExclude.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
					lblColorsToExclude.setText("Colors to Exclude:");
				}
				{
					Label lblSheetColumns = new Label(composite, SWT.NONE);
					lblSheetColumns.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
					lblSheetColumns.setText("Sheet 1 Columns to Exlude:");
				}
//						{
//							btnColorsFromDocument = new Button(compositeExcelColors, SWT.NONE);
//							btnColorsFromDocument.setBounds(289, 10, 146, 23);
//							btnColorsFromDocument.setText("Colors from Document ...");
//						}
				{
					listExcelColorsToExclude = new List(composite, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
					GridData gd_listExcelColorsToExclude = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
					gd_listExcelColorsToExclude.heightHint = 40;
					listExcelColorsToExclude.setLayoutData(gd_listExcelColorsToExclude);
					listExcelColorsToExclude.setItems(new String[] {"blue", "dark blue", "dark red", "green", "light blue", "light green", "orange", "purple", "red", "yellow"});
				}
				{
					listExcelSheet1ColumnsToExclude = new List(composite, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
					GridData gd_listExcelSheet1ColumnsToExclude = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
					gd_listExcelSheet1ColumnsToExclude.heightHint = 40;
					listExcelSheet1ColumnsToExclude.setLayoutData(gd_listExcelSheet1ColumnsToExclude);
					listExcelSheet1ColumnsToExclude.setItems(new String[] {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "X", "Y", "Z", "AA", "AB", "AC", "AD", "AE", "AF", "AG", "AH", "AI", "AJ", "AK", "AL", "AM", "AN", "AO", "AP", "AQ", "AR", "AS", "AT", "AU", "AV", "AX", "AY", "AZ", "BA", "BB", "BC", "BD", "BE", "BF", "BG", "BH", "BI", "BJ", "BK", "BL", "BM", "BN", "BO", "BP", "BQ", "BR", "BS", "BT", "BU", "BV", "BX", "BY", "BZ", "CA", "CB", "CC", "CD", "CE", "CF", "CG", "CH", "CI", "CJ", "CK", "CL", "CM", "CN", "CO", "CP", "CQ", "CR", "CS", "CT", "CU", "CV", "CX", "CY", "CZ", "DA", "DB", "DC", "DD", "DE", "DF", "DG", "DH", "DI", "DJ", "DK", "DL", "DM", "DN", "DO", "DP", "DQ", "DR", "DS", "DT", "DU", "DV", "DX", "DY", "DZ", "EA", "EB", "EC", "ED", "EE", "EF", "EG", "EH", "EI", "EJ", "EK", "EL", "EM", "EN", "EO", "EP", "EQ", "ER", "ES", "ET", "EU", "EV", "EX", "EY", "EZ"});
				}
				{
					Label lblSheetColumns_1 = new Label(composite, SWT.NONE);
					lblSheetColumns_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
					lblSheetColumns_1.setText("Sheet 2 Columns to Exlude:");
				}
				{
					Label lblSheetColumns_2 = new Label(composite, SWT.NONE);
					lblSheetColumns_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
					lblSheetColumns_2.setText("Sheets 3 (and higher) Columns to Exlude:");
				}
				{
					listExcelSheet2ColumnsToExclude = new List(composite, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
					GridData gd_listExcelSheet2ColumnsToExclude = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
					gd_listExcelSheet2ColumnsToExclude.heightHint = 40;
					listExcelSheet2ColumnsToExclude.setLayoutData(gd_listExcelSheet2ColumnsToExclude);
					listExcelSheet2ColumnsToExclude.setItems(new String[] {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "X", "Y", "Z", "AA", "AB", "AC", "AD", "AE", "AF", "AG", "AH", "AI", "AJ", "AK", "AL", "AM", "AN", "AO", "AP", "AQ", "AR", "AS", "AT", "AU", "AV", "AX", "AY", "AZ", "BA", "BB", "BC", "BD", "BE", "BF", "BG", "BH", "BI", "BJ", "BK", "BL", "BM", "BN", "BO", "BP", "BQ", "BR", "BS", "BT", "BU", "BV", "BX", "BY", "BZ", "CA", "CB", "CC", "CD", "CE", "CF", "CG", "CH", "CI", "CJ", "CK", "CL", "CM", "CN", "CO", "CP", "CQ", "CR", "CS", "CT", "CU", "CV", "CX", "CY", "CZ", "DA", "DB", "DC", "DD", "DE", "DF", "DG", "DH", "DI", "DJ", "DK", "DL", "DM", "DN", "DO", "DP", "DQ", "DR", "DS", "DT", "DU", "DV", "DX", "DY", "DZ", "EA", "EB", "EC", "ED", "EE", "EF", "EG", "EH", "EI", "EJ", "EK", "EL", "EM", "EN", "EO", "EP", "EQ", "ER", "ES", "ET", "EU", "EV", "EX", "EY", "EZ"});
				}
				{
					listExcelSheet3ColumnsToExclude = new List(composite, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
					GridData gd_listExcelSheet3ColumnsToExclude = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
					gd_listExcelSheet3ColumnsToExclude.heightHint = 40;
					listExcelSheet3ColumnsToExclude.setLayoutData(gd_listExcelSheet3ColumnsToExclude);
					listExcelSheet3ColumnsToExclude.setItems(new String[] {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "X", "Y", "Z", "AA", "AB", "AC", "AD", "AE", "AF", "AG", "AH", "AI", "AJ", "AK", "AL", "AM", "AN", "AO", "AP", "AQ", "AR", "AS", "AT", "AU", "AV", "AX", "AY", "AZ", "BA", "BB", "BC", "BD", "BE", "BF", "BG", "BH", "BI", "BJ", "BK", "BL", "BM", "BN", "BO", "BP", "BQ", "BR", "BS", "BT", "BU", "BV", "BX", "BY", "BZ", "CA", "CB", "CC", "CD", "CE", "CF", "CG", "CH", "CI", "CJ", "CK", "CL", "CM", "CN", "CO", "CP", "CQ", "CR", "CS", "CT", "CU", "CV", "CX", "CY", "CZ", "DA", "DB", "DC", "DD", "DE", "DF", "DG", "DH", "DI", "DJ", "DK", "DL", "DM", "DN", "DO", "DP", "DQ", "DR", "DS", "DT", "DU", "DV", "DX", "DY", "DZ", "EA", "EB", "EC", "ED", "EE", "EF", "EG", "EH", "EI", "EJ", "EK", "EL", "EM", "EN", "EO", "EP", "EQ", "ER", "ES", "ET", "EU", "EV", "EX", "EY", "EZ"});
				}
			}
		}
		{
			TabItem tbtmPowerpointOptions = new TabItem(tabFolder, SWT.NONE);
			tbtmPowerpointOptions.setText("Powerpoint Options");
			{
				Composite composite = new Composite(tabFolder, SWT.NONE);
				tbtmPowerpointOptions.setControl(composite);
				composite.setLayout(new GridLayout(1, false));
				{
					btnTranslateNotes = new Button(composite, SWT.CHECK);
					btnTranslateNotes.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
					btnTranslateNotes.setSelection(true);
					btnTranslateNotes.setText("Translate Notes");
				}
				{
					btnTranslateMasters = new Button(composite, SWT.CHECK);
					btnTranslateMasters.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
					btnTranslateMasters.setSelection(true);
					btnTranslateMasters.setText("Translate Masters");
				}
				{
					btnIgnorePlaceholdersInMasters = new Button(composite, SWT.CHECK);
					btnIgnorePlaceholdersInMasters
							.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
					btnIgnorePlaceholdersInMasters.setSelection(true);
					btnIgnorePlaceholdersInMasters.setText("Ignore Placeholder Text in Masters");
				}
				{
					btnIncludedSlideNumbersOnly = new Button(composite, SWT.CHECK);
					btnIncludedSlideNumbersOnly
							.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
					btnIncludedSlideNumbersOnly.setSelection(false);
					btnIncludedSlideNumbersOnly.setText("Translate included slide numbers only");
				}
				{
					listPowerpointIncludedSlideNumbers = new List(composite, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
					GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
					gridData.heightHint = 40;
					listPowerpointIncludedSlideNumbers.setLayoutData(gridData);
					java.util.List<String> slideNumbers = new ArrayList<>();
					for (int i = 1; i < 100; i++) {
						slideNumbers.add(String.valueOf(i));
					}
					listPowerpointIncludedSlideNumbers.setItems(slideNumbers.toArray(new String[slideNumbers.size()]));
				}
			}
		}
				
		//--- Dialog-level buttons

		SelectionAdapter OKCancelActions = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				result = false;
				if ( e.widget.getData().equals("h") ) {
					if ( help != null ) help.showWiki("OpenOffice Filter");
					return;
				}
				if ( e.widget.getData().equals("o") ) {
					if ( !saveData() ) return;
					result = true;
				}
				shell.close();
			};
		};
		
		OKCancelPanel pnlActions = new OKCancelPanel(shell, SWT.NONE, OKCancelActions, true);
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		pnlActions.setLayoutData(gdTmp);
		pnlActions.btOK.setEnabled(!readOnly);
		if ( !readOnly ) {
			shell.setDefaultButton(pnlActions.btOK);
		}

		shell.pack();
		Rectangle Rect = shell.getBounds();
		shell.setMinimumSize(Rect.width, Rect.height);
		Point startSize = shell.getMinimumSize();
		if ( startSize.x < 300 ) startSize.x = 300; 
		if ( startSize.y < 200 ) startSize.y = 200; 
		shell.setSize(new Point(541, 367));
		Dialogs.centerWindow(shell, parent);
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
	
	protected void setData ()
	{		
		Iterator it;
		String sYmphony;
		String sRGB;
		Excell eggshell;
		String sDuraCell;
		String sMulti[];
		TreeSet<String> tsColors;
		Object o[];
		int ndx;
		int siz;
		btnTranslateDocumentProperties.setSelection(params.getTranslateDocProperties());
		btnTranslateComments.setSelection(params.getTranslateComments());
		btnCleanAggressively.setSelection(params.getCleanupAggressively());
		btnTreatTabAsChar.setSelection(params.getAddTabAsCharacter());
		btnTreatLineBreakAsChar.setSelection(params.getAddLineSeparatorCharacter());
		btnTranslateHeadersAndFooters.setSelection(params.getTranslateWordHeadersFooters());
		btnTranslateHiddenText.setSelection(params.getTranslateWordHidden());
		btnExcludeGraphicMetadata.setSelection(params.getTranslateWordExcludeGraphicMetaData());
		btnAutomaticallyAcceptRevisions.setSelection(params.getAutomaticallyAcceptRevisions());
		btnIgnoreSoftHyphen.setSelection(params.getIgnoreSoftHyphenTag());
        btnReplaceNonBreakingHyphen.setSelection(params.getReplaceNoBreakHyphenTag());
		btnTranslateNotes.setSelection(params.getTranslatePowerpointNotes());
		btnTranslateMasters.setSelection(params.getTranslatePowerpointMasters());
		btnIgnorePlaceholdersInMasters.setSelection(params.getIgnorePlaceholdersInPowerpointMasters());
		btnTranslateSheetNames.setSelection(params.getTranslateExcelSheetNames());
		btnTranslateDiagramData.setSelection(params.getTranslateExcelDiagramData());
		btnTranslateDrawings.setSelection(params.getTranslateExcelDrawings());
		if (params.tsExcludeWordStyles!=null && !params.tsExcludeWordStyles.isEmpty())
		{
			it = params.tsExcludeWordStyles.iterator();
			siz = params.tsExcludeWordStyles.size();
			if (siz>0)
			{
				sMulti = new String[siz];
				ndx = 0;
				while(it.hasNext())
				{
					sMulti[ndx++] = (String)it.next();
				}
				listExcludedWordStyles.setSelection(sMulti);
			}
		}
		if (params.tsComplexFieldDefinitionsToExtract != null && !params.tsComplexFieldDefinitionsToExtract.isEmpty()) {
			it = params.tsComplexFieldDefinitionsToExtract.iterator();
			siz = params.tsComplexFieldDefinitionsToExtract.size();
			if (siz > 0)
			{
				sMulti = new String[siz];
				ndx = 0;
				while (it.hasNext())
				{
					sMulti[ndx++] = (String) it.next();
				}
				listTranslatableFields.setSelection(sMulti);
			}
		}
		if (params.getTranslateExcelExcludeColors() &&
			params.tsExcelExcludedColors!=null && !params.tsExcelExcludedColors.isEmpty())
		{
			tsColors = new TreeSet<String>();
			it = params.tsExcelExcludedColors.iterator();
			while(it.hasNext())
			{
				sRGB = (String)it.next();
				if (sRGB.equals("FF0000FF"))
					sRGB = "blue";
				else if (sRGB.equals("FF3366FF"))
					sRGB = "light blue";
				else if (sRGB.equals("FF008000"))
					sRGB = "green";
				else if (sRGB.equals("FF660066"))
					sRGB = "purple";
				else if (sRGB.equals("FFFF0000"))
					sRGB = "red";
				else if (sRGB.equals("FFFFFF00"))
					sRGB = "yellow";
				else if (sRGB.equals("FF800000"))
					sRGB = "dark red";
				else if (sRGB.equals("FFCCFFCC"))
					sRGB = "light green";
				else if (sRGB.equals("FFFF6600"))
					sRGB = "orange";
				else if (sRGB.equals("FF000090"))
					sRGB = "dark blue";
				tsColors.add(sRGB);
			}
			siz = tsColors.size();
			if (siz>0)
			{
				sMulti = new String[siz];
				it = tsColors.iterator();
				ndx = 0;
				while(it.hasNext())
					sMulti[ndx++] = (String)it.next();
				listExcelColorsToExclude.setSelection(sMulti);
			}
		}
		btnExcludeExcelColumns.setSelection(params.getTranslateExcelExcludeColumns());
		btnTranslateHiddenCells.setSelection(params.getTranslateExcelHidden());
		if (params.getTranslateExcelExcludeColumns() &&
			params.tsExcelExcludedColumns!=null && !params.tsExcelExcludedColumns.isEmpty())
		{
			siz = 0;
			for(it=params.tsExcelExcludedColumns.iterator();it.hasNext();)
			{
				sYmphony = (String)it.next();			
				eggshell = new Excell(sYmphony);
				sDuraCell = eggshell.getColumn();
				if (eggshell.getSheet().equals("1"))
					siz++;
			}
			if (siz>0)
			{
				ndx = 0;
				sMulti = new String[siz];
				for(it=params.tsExcelExcludedColumns.iterator();it.hasNext();)
				{
					sYmphony = (String)it.next();
					eggshell = new Excell(sYmphony);
					sDuraCell = eggshell.getColumn();
					if (eggshell.getSheet().equals("1"))
						sMulti[ndx++] = sDuraCell;
				}
				listExcelSheet1ColumnsToExclude.setSelection(sMulti);
			}

			siz = 0;
			for(it=params.tsExcelExcludedColumns.iterator();it.hasNext();)
			{
				sYmphony = (String)it.next();
				eggshell = new Excell(sYmphony);
				sDuraCell = eggshell.getColumn();
				if (eggshell.getSheet().equals("2"))
					siz++;
			}
			if (siz>0)
			{
				ndx = 0;
				sMulti = new String[siz];
				for(it=params.tsExcelExcludedColumns.iterator();it.hasNext();)
				{
					sYmphony = (String)it.next();
					eggshell = new Excell(sYmphony);
					sDuraCell = eggshell.getColumn();
					if (eggshell.getSheet().equals("2"))
						sMulti[ndx++] = sDuraCell;
				}
				listExcelSheet2ColumnsToExclude.setSelection(sMulti);
			}

			siz = 0;
			for(it=params.tsExcelExcludedColumns.iterator();it.hasNext();)
			{
				sYmphony = (String)it.next();
				eggshell = new Excell(sYmphony);
				sDuraCell = eggshell.getColumn();
				if (eggshell.getSheet().equals("3"))
					siz++;
			}
			if (siz>0)
			{
				ndx = 0;
				sMulti = new String[siz];
				for(it=params.tsExcelExcludedColumns.iterator();it.hasNext();)
				{
					sYmphony = (String)it.next();
					eggshell = new Excell(sYmphony);
					sDuraCell = eggshell.getColumn();
					if (eggshell.getSheet().equals("3"))
						sMulti[ndx++] = sDuraCell;
				}
				listExcelSheet3ColumnsToExclude.setSelection(sMulti);
			}
		}
		btnIncludedSlideNumbersOnly.setSelection(params.getPowerpointIncludedSlideNumbersOnly());
		java.util.List<String> selectedSlideNumbers = new ArrayList<>();
		for (Integer slideNumber : params.tsPowerpointIncludedSlideNumbers) {
			selectedSlideNumbers.add(slideNumber.toString());
		}
		listPowerpointIncludedSlideNumbers.setSelection(selectedSlideNumbers.toArray(new String[selectedSlideNumbers.size()]));
	}
	
	private boolean saveData () {
		String sColor;
		String sArray[];
		String sRGB;
		int len;
		params.setTranslateDocProperties(btnTranslateDocumentProperties.getSelection());
		params.setTranslateComments(btnTranslateComments.getSelection());
		params.setCleanupAggressively(btnCleanAggressively.getSelection());
		params.setAddTabAsCharacter(btnTreatTabAsChar.getSelection());
		params.setAddLineSeparatorCharacter(btnTreatLineBreakAsChar.getSelection());
		params.setTranslateWordHeadersFooters(btnTranslateHeadersAndFooters.getSelection());
		params.setTranslateWordHidden(btnTranslateHiddenText.getSelection());
		params.setTranslateWordExcludeGraphicMetaData(btnExcludeGraphicMetadata.getSelection());
		params.setAutomaticallyAcceptRevisions(btnAutomaticallyAcceptRevisions.getSelection());
		params.setIgnoreSoftHyphenTag(btnIgnoreSoftHyphen.getSelection());
        params.setReplaceNoBreakHyphenTag(btnReplaceNonBreakingHyphen.getSelection());
		params.setTranslatePowerpointNotes(btnTranslateNotes.getSelection());
		params.setTranslatePowerpointMasters(btnTranslateMasters.getSelection());
		params.setIgnorePlaceholdersInPowerpointMasters(
				btnIgnorePlaceholdersInMasters.getSelection());
		params.setTranslateExcelSheetNames(btnTranslateSheetNames.getSelection());
		params.setTranslateExcelDiagramData(btnTranslateDiagramData.getSelection());
		params.setTranslateExcelDrawings(btnTranslateDrawings.getSelection());
		params.setExtractExternalHyperlinks(btnExtractExternalHyperlinks.getSelection());

		// Exclude text in certain styles from translation in Word
		sArray = listExcludedWordStyles.getSelection(); // selected items
		if (params.tsExcludeWordStyles==null)
			params.tsExcludeWordStyles = new TreeSet<String>();
		else
			params.tsExcludeWordStyles.clear();
		len = sArray.length;
		if (len>0)
		{
			for(int i=0;i<len;i++)
			params.tsExcludeWordStyles.add(sArray[i]);
		}

		// Translate specific field types in Word
		sArray = listTranslatableFields.getSelection(); // selected items
		if (params.tsComplexFieldDefinitionsToExtract == null)
			params.tsComplexFieldDefinitionsToExtract = new TreeSet<String>();
		else
			params.tsComplexFieldDefinitionsToExtract.clear();
		len = sArray.length;
		if (len>0)
		{
			for(int i=0;i<len;i++)
			params.tsComplexFieldDefinitionsToExtract.add(sArray[i]);
		}

		// Exclude text in certain colors from translation in Excel
		sArray = listExcelColorsToExclude.getSelection(); // selected items
		if (params.tsExcelExcludedColors==null)
			params.tsExcelExcludedColors = new TreeSet<String>();
		else
			params.tsExcelExcludedColors.clear();
		len = sArray.length;
		if (len>0)
		{
			params.setTranslateExcelExcludeColors(true);
			for(int i=0;i<len;i++)
			{
				sColor = sArray[i];
				sRGB = null;
				// These are aligned with the "Standard Colors" in Excel 2011
				if (sColor.equals("blue"))
					sRGB = "FF0000FF";
				else if (sColor.equals("light blue"))
					sRGB = "FF3366FF";
				else if (sColor.equals("green"))
					sRGB = "FF008000";
				else if (sColor.equals("purple"))
					sRGB = "FF660066";
				else if (sColor.equals("red"))
					sRGB = "FFFF0000";
				else if (sColor.equals("yellow"))
					sRGB = "FFFFFF00";
				else if (sColor.equals("dark red"))
					sRGB = "FF800000";
				else if (sColor.equals("light green"))
					sRGB = "FFCCFFCC";
				else if (sColor.equals("orange"))
					sRGB = "FFFF6600";
				else if (sColor.equals("dark blue"))
					sRGB = "FF000090";
				if (sRGB!=null)
					params.tsExcelExcludedColors.add(sRGB);
			}
		}
		else
			params.setTranslateExcelExcludeColors(false);
		
		// Exclude text in certain columns in Excel in sheets 1, 2, or 3
		params.setTranslateExcelHidden(btnTranslateHiddenCells.getSelection());
		params.setTranslateExcelExcludeColumns(btnExcludeExcelColumns.getSelection());
		if (params.tsExcelExcludedColumns==null)
			params.tsExcelExcludedColumns = new TreeSet<String>();
		else
			params.tsExcelExcludedColumns.clear();
		params.setTranslateExcelExcludeColumns(btnExcludeExcelColumns.getSelection());
		if (params.getTranslateExcelExcludeColumns())
		{
			sArray = listExcelSheet1ColumnsToExclude.getSelection(); // selected items
			len = sArray.length;
			if (len>0)
			{
				for(int i=0;i<len;i++)
					params.tsExcelExcludedColumns.add("1"+sArray[i]);
			}
			sArray = listExcelSheet2ColumnsToExclude.getSelection(); // selected items
			len = sArray.length;
			if (len>0)
			{
				for(int i=0;i<len;i++)
					params.tsExcelExcludedColumns.add("2"+sArray[i]);
			}
			sArray = listExcelSheet3ColumnsToExclude.getSelection(); // selected items
			len = sArray.length;
			if (len>0)
			{
				for(int i=0;i<len;i++)
					params.tsExcelExcludedColumns.add("3"+sArray[i]);
			}
		}
		params.setPowerpointIncludedSlideNumbersOnly(btnIncludedSlideNumbersOnly.getSelection());
		if (params.tsPowerpointIncludedSlideNumbers == null) {
			params.tsPowerpointIncludedSlideNumbers = new TreeSet<>();
		}
		else {
			params.tsPowerpointIncludedSlideNumbers.clear();
		}
		if (btnIncludedSlideNumbersOnly.getSelection()) {
			java.util.List<Integer> slideNumbers = new ArrayList<>();
			for (String s : listPowerpointIncludedSlideNumbers.getSelection()) {
				slideNumbers.add(Integer.valueOf(s));
			}
			params.tsPowerpointIncludedSlideNumbers.addAll(slideNumbers);
		}
		params.nFileType = ParseType.MSWORD;
		return true;
	}
}

