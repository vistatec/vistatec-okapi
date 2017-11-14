/*===========================================================================
  Copyright (C) 2010 by the Okapi Framework contributors
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

package net.sf.okapi.lib.ui.verification;

import java.util.ArrayList;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

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
import net.sf.okapi.lib.verification.Issue;
import net.sf.okapi.lib.verification.Parameters;
import net.sf.okapi.lib.verification.PatternItem;
import net.sf.okapi.lib.verification.QualityCheckSession;

@EditorFor(Parameters.class)
public class ParametersEditor implements IParametersEditor, ISWTEmbeddableParametersEditor {

	private static final int TAB_CHARACTERS = 4;
	private static final int TAB_LANGUAGETOOL = 5;
	private static final int TAB_TERMS = 6;
	private static final int TAB_OTHER = 7;
	
	private static final int INFOCOLWIDTH = 120;
	
	private static final String FROMSOURCE = "Src";
	private static final String FROMTARGET = "Trg";
	
	private static final String NODESC_LABEL = "<Enter description here>";
	private static final String[] severityNames = new String[]{
		"LOW", "MEDIUM", "HIGH"
	};
	
	private Shell shell;
	private boolean result = false;
	private OKCancelPanel pnlActions;
	private Parameters params;
	private IHelp help;
	private TabFolder tabs;
	private Button chkAutoOpen;
	private Button chkShowFullPath;
	private Button chkLeadingWS;
	private Button chkTrailingWS;
	private Button chkEmptyTarget;
	private Button chkEmptySource;
	private Button chkTargetSameAsSource;
	private Button chkTargetSameAsSourceForSameLanguage;
	private Button chkTargetSameAsSourceWithCodes;
	private Composite mainComposite;
	private TextAndBrowsePanel pnlOutputPath;
	private Combo cbOutputType;
	private Button chkXliffSchema;
	private Button chkPatterns;
	private Button chkDoubledWord;
	private Label stDoubledWordExceptions;
	private Text edDoubledWordExceptions;
	private Text edTypesToIgnore;
	private Button chkCorruptedChars;
	private Table table;
	private Button btAdd;
	private Button btEdit;
	private Button btRemove;
	private Button btMoveUp;
	private Button btMoveDown;
	private Button btImport;
	private Button btExport;
	private Text edSource;
	private Text edTarget;
	private Text edDescription;
	private Combo cbSeverity;
	private Button chkFromSource;
	private Shell dialog;
	private TableItem editItem;
	private boolean addMode;
	private Button chkCheckWithLT;
	private Text edServerURL;
	private Button chkTranslateLTMsg;
	private Button chkLTBilingualMode;
	private Text edLTTranslationSource;
	private Text edLTTranslationTarget;
	private Text edLTTranslationServiceKey;
	private Button chkStorageSize;
	private Button chkAbsoluteMaxCharLength;
	private Spinner spAbsoluteMaxCharLength;
	private Button chkMaxCharLength;
	private Composite cmpMaxCharLength;
	private Spinner spMaxCharLengthBreak;
	private Spinner spMaxCharLengthAbove;
	private Spinner spMaxCharLengthBelow;
	private Button chkMinCharLength;
	private Composite cmpMinCharLength;
	private Spinner spMinCharLengthBreak;
	private Spinner spMinCharLengthAbove;
	private Spinner spMinCharLengthBelow;
	private Button chkCheckAllowedCharacters;
	private Button chkCheckCharacters;
	private Text edCharset;
	private Label stExtraCharsAllowed;
	private Text edExtraCharsAllowed;
	private Button rdScopeAllEntries;
	private Button rdScopeApprovedOnly;
	private Button rdScopeNotApprovedOnly;
	private Button btStartLT;
	private Button chkCodeDifference;
	private Button chkGuessOpenClose;
	private StringListPanel pnlMissingCodesAllowed;
	private StringListPanel pnlExtraCodesAllowed;
	private Button chkCheckTerms;
	private Label stTermPath;
	private TextAndBrowsePanel pnlTermsPath;
	private Button chkStringMode;
	private Button chkBetweenCodes;
	private Button chkCheckBlacklist;
	private Label stBlacklistPath;
	private Button chkAllowBlacklistSub;	
	private Button chkBlacklistSrc;	
	private TextAndBrowsePanel pnlBlacklistPath;
	
	// Flag to indicate the editor is use for step parameters
	private boolean stepMode = true;
	
	// step-mode fields:
	private TextAndBrowsePanel pnlSessionPath;
	private Button chkSaveSession;

	/**
	 * Creates an editor (with step mode on).
	 */
	public ParametersEditor() {
		// Nothing to do
	}

	/**
	 * Creates an editor with the given step mode.
	 *
	 * @param stepMode true to display the step-specific options.
	 */
	public ParametersEditor(boolean stepMode) {
		this.stepMode = stepMode;
	}

	@Override
	public boolean edit(IParameters params,
			boolean readOnly,
			IContext context) {
		boolean bRes = false;
		try {
			shell = null;
			help = (IHelp) context.getObject("help");
			stepMode = context.getBoolean("stepMode");
			this.params = (Parameters) params;
			shell = new Shell((Shell) context.getObject("shell"), SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.APPLICATION_MODAL);
			create((Shell) context.getObject("shell"), readOnly);
			return showDialog();
		}
		catch (Exception e) {
			Dialogs.showError(shell, e.getLocalizedMessage(), null);
			bRes = false;
		}
		finally {
			// Dispose of the shell, but not of the display
			if (shell != null) {
				shell.dispose();
			}
		}
		return bRes;
	}

	public IParameters createParameters() {
		return new Parameters();
	}

	@Override
	public Composite getComposite() {
		return mainComposite;
	}

	@Override
	public void initializeEmbeddableEditor(Composite parent,
			IParameters paramsObject,
			IContext context) {
		params = (Parameters) paramsObject;
		shell = (Shell) context.getObject("shell");

		createComposite(parent);

		setData();
	}

	@Override
	public String validateAndSaveParameters() {
		if (!saveData()) {
			return null;
		}
		return params.toString();
	}

	private void create(Shell parent,
			boolean readOnly) {
		shell.setText("Quality Check Configuration");
		if (parent != null) {
			UIUtil.inheritIcon(shell, parent);
		}
		GridLayout layTmp = new GridLayout();
		layTmp.marginBottom = 0;
		layTmp.verticalSpacing = 0;
		shell.setLayout(layTmp);

		createComposite(shell);

		//--- Dialog-level buttons

		SelectionAdapter OKCancelActions = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				result = false;
				if (e.widget.getData().equals("h")) {
					if (help != null) {
						help.showWiki("CheckMate - Quality Check Configuration");
					}
					return;
				}
				if (e.widget.getData().equals("o")) {
					if (!saveData()) {
						return;
					}
				}
				shell.close();
			}
		};
		pnlActions = new OKCancelPanel(shell, SWT.NONE, OKCancelActions, true);
		pnlActions.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		pnlActions.btOK.setEnabled(!readOnly);
		if (!readOnly) {
			shell.setDefaultButton(pnlActions.btOK);
		}

		shell.pack();
		shell.setMinimumSize(shell.getSize());
		Dialogs.centerWindow(shell, parent);
		setData();
	}

	private void createComposite(Composite parent) {
		mainComposite = new Composite(parent, SWT.BORDER);
		mainComposite.setLayout(new GridLayout());
		mainComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		tabs = new TabFolder(mainComposite, SWT.NONE);
		tabs.setLayout(new GridLayout());
		GridData gdTmp = new GridData(GridData.FILL_BOTH);
		// Auto-size is too high, we need to fix it manually
		gdTmp.heightHint = 430;
		tabs.setLayoutData(gdTmp);

		//--- General tab

		Composite cmpTmp = new Composite(tabs, SWT.NONE);
		cmpTmp.setLayout(new GridLayout());

		Group grpTU = new Group(cmpTmp, SWT.NONE);
		grpTU.setText("Text unit verifications");
		grpTU.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		grpTU.setLayout(new GridLayout());

		Button chkTmp = new Button(grpTU, SWT.CHECK);
		chkTmp.setText("[Always On] Warn if an entry does not have a translation");
		chkTmp.setSelection(true);
		chkTmp.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				((Button) e.getSource()).setSelection(true);
			}
		});

		chkLeadingWS = new Button(grpTU, SWT.CHECK);
		chkLeadingWS.setText("Warn if a target entry has a difference in leading white spaces");

		chkTrailingWS = new Button(grpTU, SWT.CHECK);
		chkTrailingWS.setText("Warn if a target entry has a difference in trailing white spaces");

		Group grpSeg = new Group(cmpTmp, SWT.NONE);
		grpSeg.setText("Segment verifications");
		grpSeg.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		grpSeg.setLayout(new GridLayout());

		chkTmp = new Button(grpSeg, SWT.CHECK);
		chkTmp.setText("[Always On] Warn if a source segment does not have a corresponding target");
		chkTmp.setSelection(true);
		chkTmp.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				((Button) e.getSource()).setSelection(true);
			}
		});

		chkTmp = new Button(grpSeg, SWT.CHECK);
		chkTmp.setText("[Always On] Warn if there is an extra target segment");
		chkTmp.setSelection(true);
		chkTmp.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				((Button) e.getSource()).setSelection(true);
			}
		});

		chkEmptyTarget = new Button(grpSeg, SWT.CHECK);
		chkEmptyTarget.setText("Warn if a target segment is empty when its source is not empty");

		chkEmptySource = new Button(grpSeg, SWT.CHECK);
		chkEmptySource.setText("Warn if a target segment is not empty when its source is empty");

		chkTargetSameAsSource = new Button(grpSeg, SWT.CHECK);
		chkTargetSameAsSource.setText("Warn if a target segment is the same as its source (for segments with text)");
		chkTargetSameAsSource.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateTargetSameAsSourceForSameLanguage();
				updateTargetSameAsSourceWithCodes();
			}
		});
		chkTargetSameAsSource.setLayoutData(new GridData());

		final int horizIndent = 16;
		chkTargetSameAsSourceForSameLanguage = new Button(grpSeg, SWT.CHECK);
		chkTargetSameAsSourceForSameLanguage.setText("Verify for same language family (i.e. en vs. en-GB, or fr-FR vs. fr-CA)");
		gdTmp = new GridData();
		gdTmp.horizontalIndent = horizIndent;
		chkTargetSameAsSourceForSameLanguage.setLayoutData(gdTmp);

		chkTargetSameAsSourceWithCodes = new Button(grpSeg, SWT.CHECK);
		chkTargetSameAsSourceWithCodes.setText("Include the codes in the comparison");
		gdTmp = new GridData();
		gdTmp.horizontalIndent = horizIndent;
		chkTargetSameAsSourceWithCodes.setLayoutData(gdTmp);

		chkDoubledWord = new Button(grpSeg, SWT.CHECK);
		chkDoubledWord.setText("Warn on doubled words (e.g. \"is is\" in \"This is is an example\")");
		chkDoubledWord.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateDoubledWord();
			}
		});

		stDoubledWordExceptions = new Label(grpSeg, SWT.NONE);
		stDoubledWordExceptions.setText("Exceptions (words separated by ';' e.g. \"vous;nous\"):");
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		gdTmp.horizontalIndent = horizIndent;
		stDoubledWordExceptions.setLayoutData(gdTmp);

		edDoubledWordExceptions = new Text(grpSeg, SWT.BORDER);
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		gdTmp.horizontalIndent = horizIndent;
		edDoubledWordExceptions.setLayoutData(gdTmp);

		Group grpFile = new Group(cmpTmp, SWT.NONE);
		grpFile.setText("File verification");
		grpFile.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		grpFile.setLayout(new GridLayout());

		chkXliffSchema = new Button(grpFile, SWT.CHECK);
		chkXliffSchema.setText("Validate XLIFF documents against schema");

		TabItem tiTmp = new TabItem(tabs, SWT.NONE);
		tiTmp.setText("General");
		tiTmp.setControl(cmpTmp);


		//--- Length tab

		cmpTmp = new Composite(tabs, SWT.NONE);
		cmpTmp.setLayout(new GridLayout());
		gdTmp = new GridData(GridData.FILL_BOTH);
		cmpTmp.setLayoutData(gdTmp);

		chkStorageSize = new Button(cmpTmp, SWT.CHECK);
		chkStorageSize.setText("Warn if a source or target text unit does not fit its ITS Storage Size property");

		chkAbsoluteMaxCharLength = new Button(cmpTmp, SWT.CHECK);
		chkAbsoluteMaxCharLength.setText("Warn if a target is longer than:");
		chkAbsoluteMaxCharLength.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateAbsoluteMaxCharLength();
			}
		});

		spAbsoluteMaxCharLength = new Spinner(cmpTmp, SWT.BORDER);
		gdTmp = new GridData();
		gdTmp.widthHint = 70;
		gdTmp.horizontalIndent = horizIndent;
		spAbsoluteMaxCharLength.setLayoutData(gdTmp);
		spAbsoluteMaxCharLength.setMaximum(99999);
		spAbsoluteMaxCharLength.setMinimum(0);


		chkMaxCharLength = new Button(cmpTmp, SWT.CHECK);
		chkMaxCharLength.setText("Warn if a target is longer than the given percentage of the character length of its source:");
		chkMaxCharLength.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateMaxCharLength();
			}
		});

		cmpMaxCharLength = new Composite(cmpTmp, SWT.NONE);
		cmpMaxCharLength.setLayout(new GridLayout(2, false));

		Label stTmp = new Label(cmpMaxCharLength, SWT.NONE);
		stTmp.setText("Character length above which a text is considered \"long\":");
		stTmp.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

		spMaxCharLengthBreak = new Spinner(cmpMaxCharLength, SWT.BORDER);
		gdTmp = new GridData();
		gdTmp.widthHint = 70;
		gdTmp.horizontalIndent = horizIndent;
		spMaxCharLengthBreak.setLayoutData(gdTmp);
		spMaxCharLengthBreak.setMaximum(999);
		spMaxCharLengthBreak.setMinimum(1);

		stTmp = new Label(cmpMaxCharLength, SWT.NONE);
		stTmp.setText("Percentage for \"short\" text:");
		stTmp.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

		spMaxCharLengthBelow = new Spinner(cmpMaxCharLength, SWT.BORDER);
		gdTmp = new GridData();
		gdTmp.widthHint = 70;
		gdTmp.horizontalIndent = horizIndent;
		spMaxCharLengthBelow.setLayoutData(gdTmp);
		spMaxCharLengthBelow.setMaximum(999);
		spMaxCharLengthBelow.setMinimum(1);

		stTmp = new Label(cmpMaxCharLength, SWT.NONE);
		stTmp.setText("Percentage for \"long\" text:");
		stTmp.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

		spMaxCharLengthAbove = new Spinner(cmpMaxCharLength, SWT.BORDER);
		gdTmp = new GridData();
		gdTmp.widthHint = 70;
		gdTmp.horizontalIndent = horizIndent;
		spMaxCharLengthAbove.setLayoutData(gdTmp);
		spMaxCharLengthAbove.setMaximum(999);
		spMaxCharLengthAbove.setMinimum(1);


		chkMinCharLength = new Button(cmpTmp, SWT.CHECK);
		chkMinCharLength.setText("Warn if a target is shorter than the given percentage of the character length of its source:");
		chkMinCharLength.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateMinCharLength();
			}
		});

		cmpMinCharLength = new Composite(cmpTmp, SWT.NONE);
		cmpMinCharLength.setLayout(new GridLayout(2, false));

		stTmp = new Label(cmpMinCharLength, SWT.NONE);
		stTmp.setText("Character length above which a text is considered \"long\":");
		stTmp.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

		spMinCharLengthBreak = new Spinner(cmpMinCharLength, SWT.BORDER);
		gdTmp = new GridData();
		gdTmp.widthHint = 70;
		gdTmp.horizontalIndent = horizIndent;
		spMinCharLengthBreak.setLayoutData(gdTmp);
		spMinCharLengthBreak.setMaximum(999);
		spMinCharLengthBreak.setMinimum(1);

		stTmp = new Label(cmpMinCharLength, SWT.NONE);
		stTmp.setText("Percentage for \"short\" text:");
		stTmp.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

		spMinCharLengthBelow = new Spinner(cmpMinCharLength, SWT.BORDER);
		gdTmp = new GridData();
		gdTmp.widthHint = 70;
		gdTmp.horizontalIndent = horizIndent;
		spMinCharLengthBelow.setLayoutData(gdTmp);
		spMinCharLengthBelow.setMaximum(999);
		spMinCharLengthBelow.setMinimum(1);

		stTmp = new Label(cmpMinCharLength, SWT.NONE);
		stTmp.setText("Percentage for \"long\" text:");
		stTmp.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

		spMinCharLengthAbove = new Spinner(cmpMinCharLength, SWT.BORDER);
		gdTmp = new GridData();
		gdTmp.widthHint = 70;
		gdTmp.horizontalIndent = horizIndent;
		spMinCharLengthAbove.setLayoutData(gdTmp);
		spMinCharLengthAbove.setMaximum(999);
		spMinCharLengthAbove.setMinimum(1);

		tiTmp = new TabItem(tabs, SWT.NONE);
		tiTmp.setText("Length");
		tiTmp.setControl(cmpTmp);


		//--- Inline codes tab

		cmpTmp = new Composite(tabs, SWT.NONE);
		cmpTmp.setLayout(new GridLayout(2, true));
		gdTmp = new GridData(GridData.FILL_BOTH);
		cmpTmp.setLayoutData(gdTmp);

		chkCodeDifference = new Button(cmpTmp, SWT.CHECK);
		chkCodeDifference.setText("Warn if there is a code difference between source and target segments");
		gdTmp = new GridData();
		gdTmp.horizontalSpan = 2;
		chkCodeDifference.setLayoutData(gdTmp);

		chkGuessOpenClose = new Button(cmpTmp, SWT.CHECK);
		chkGuessOpenClose.setText("Try to guess opening/closing types for placeholder codes");
		gdTmp = new GridData();
		gdTmp.horizontalSpan = 2;
		chkGuessOpenClose.setLayoutData(gdTmp);

		Label label = new Label(cmpTmp, SWT.NONE);
		label.setText("List of the inline code types to ignore:");
		gdTmp = new GridData();
		gdTmp.horizontalSpan = 2;
		label.setLayoutData(gdTmp);

		edTypesToIgnore = new Text(cmpTmp, SWT.BORDER);
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		gdTmp.horizontalSpan = 2;
		edTypesToIgnore.setLayoutData(gdTmp);

		pnlMissingCodesAllowed = new StringListPanel(cmpTmp, SWT.NONE, "Codes allowed to be missing from the target:");
		pnlMissingCodesAllowed.setLayoutData(new GridData(GridData.FILL_BOTH));

		pnlExtraCodesAllowed = new StringListPanel(cmpTmp, SWT.NONE, "Codes allowed to be extra in the target:");
		pnlExtraCodesAllowed.setLayoutData(new GridData(GridData.FILL_BOTH));

		tiTmp = new TabItem(tabs, SWT.NONE);
		tiTmp.setText("Inline Codes");
		tiTmp.setControl(cmpTmp);


		//--- Patterns tab

		cmpTmp = new Composite(tabs, SWT.NONE);
		cmpTmp.setLayout(new GridLayout());
		cmpTmp.setLayoutData(new GridData(GridData.FILL_BOTH));

		chkPatterns = new Button(cmpTmp, SWT.CHECK);
		chkPatterns.setText("Verify that the following source patterns are translated as expected:");
		chkPatterns.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updatePatterns();
			}
		});

		table = new Table(cmpTmp, SWT.CHECK | SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		gdTmp = new GridData(GridData.FILL_BOTH);
		table.setLayoutData(gdTmp);
		// Update buttons when moving cursor
		table.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (event.detail != SWT.CHECK) {
					updateMoveButtons();
				}
			}
		});
		// Double-click is like edit
		table.addListener(SWT.MouseDoubleClick, new Listener() {
			public void handleEvent(Event event) {
				if (table.getSelectionIndex() != -1) {
					editPattern(false);
				}
			}
		});
		// Resizing the columns
		table.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				int tableWidth = table.getClientArea().width;
				table.getColumn(0).setWidth(INFOCOLWIDTH);
				int remaining = tableWidth - INFOCOLWIDTH;
				table.getColumn(1).setWidth(remaining / 3);
				table.getColumn(2).setWidth(remaining / 3);
				table.getColumn(3).setWidth(remaining / 3);
			}
		});

		String[] titles = {"Options", "Source Pattern", "Target Pattern", "Description"};
		for (int i = 0; i < titles.length; i++) {
			TableColumn column = new TableColumn(table, SWT.LEFT);
			column.setText(titles[i]);
			column.pack();
		}

		// Buttons
		Composite cmpTmp2 = new Composite(cmpTmp, SWT.NONE);
		GridLayout layTmp = new GridLayout(7, true);
		layTmp.marginHeight = layTmp.marginWidth = 0;
		cmpTmp2.setLayout(layTmp);

//TODO: Fix resizing of buttons!!!		
		btAdd = UIUtil.createGridButton(cmpTmp2, SWT.PUSH, "Add...", UIUtil.BUTTON_DEFAULT_WIDTH, 1);
		btAdd.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				editPattern(true);
			}
		});

		btEdit = UIUtil.createGridButton(cmpTmp2, SWT.PUSH, "Edit...", UIUtil.BUTTON_DEFAULT_WIDTH, 1);
		btEdit.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				editPattern(false);
			}
		});

		btRemove = UIUtil.createGridButton(cmpTmp2, SWT.PUSH, "Remove", UIUtil.BUTTON_DEFAULT_WIDTH, 1);
		btRemove.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				removePattern();
			}
		});

		btMoveUp = UIUtil.createGridButton(cmpTmp2, SWT.PUSH, "Move Up", UIUtil.BUTTON_DEFAULT_WIDTH, 1);
		btMoveUp.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				moveItem(-1);
			}
		});

		btMoveDown = UIUtil.createGridButton(cmpTmp2, SWT.PUSH, "Move Down", UIUtil.BUTTON_DEFAULT_WIDTH, 1);
		btMoveDown.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				moveItem(+1);
			}
		});

		btImport = UIUtil.createGridButton(cmpTmp2, SWT.PUSH, "Import...", UIUtil.BUTTON_DEFAULT_WIDTH, 1);
		btImport.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				importPatterns();
			}
		});

		btExport = UIUtil.createGridButton(cmpTmp2, SWT.PUSH, "Export...", UIUtil.BUTTON_DEFAULT_WIDTH, 1);
		btExport.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				exportPatterns();
			}
		});

		tiTmp = new TabItem(tabs, SWT.NONE);
		tiTmp.setText("Patterns");
		tiTmp.setControl(cmpTmp);


		//--- Characters tab

		cmpTmp = new Composite(tabs, SWT.NONE);
		cmpTmp.setLayout(new GridLayout());
		cmpTmp.setLayoutData(new GridData(GridData.FILL_BOTH));

		chkCheckAllowedCharacters = new Button(cmpTmp, SWT.CHECK);
		chkCheckAllowedCharacters.setText("Verify the ITS Allowed Characters property");

		chkCorruptedChars = new Button(cmpTmp, SWT.CHECK);
		chkCorruptedChars.setText("Warn if some possibly corrupted characters are found in the target entry");

		chkCheckCharacters = new Button(cmpTmp, SWT.CHECK);
		chkCheckCharacters.setText("Warn if a character is not included in the following character set encoding:");
		chkCheckCharacters.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateCharacters();
			}
		});

		Composite cmpChars = new Composite(cmpTmp, SWT.NONE);
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		gdTmp.horizontalIndent = 8;
		cmpChars.setLayoutData(gdTmp);
		cmpChars.setLayout(new GridLayout());

		edCharset = new Text(cmpChars, SWT.BORDER);
		edCharset.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		stExtraCharsAllowed = new Label(cmpChars, SWT.NONE);
		stExtraCharsAllowed.setText("Allow the characters matching the following regular expression pattern:");

		edExtraCharsAllowed = new Text(cmpChars, SWT.BORDER);
		edExtraCharsAllowed.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		tiTmp = new TabItem(tabs, SWT.NONE);
		tiTmp.setText("Characters");
		tiTmp.setControl(cmpTmp);


		//--- Language Tool tab

		cmpTmp = new Composite(tabs, SWT.NONE);
		cmpTmp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		cmpTmp.setLayout(new GridLayout());

		chkCheckWithLT = new Button(cmpTmp, SWT.CHECK);
		chkCheckWithLT.setText("Perform the verifications provided by the LanguageTool server");
		chkCheckWithLT.setLayoutData(new GridData());
		chkCheckWithLT.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateLTOptions();
			}
		});

		label = new Label(cmpTmp, SWT.NONE);
		label.setText("Server URL (e.g. http://localhost:8081/):");
		edServerURL = new Text(cmpTmp, SWT.BORDER);
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		edServerURL.setLayoutData(gdTmp);

		chkLTBilingualMode = new Button(cmpTmp, SWT.CHECK);
		chkLTBilingualMode.setText("Use bilingual mode");
		gdTmp = new GridData();
		gdTmp.verticalIndent = 16;
		chkLTBilingualMode.setLayoutData(gdTmp);
		chkLTBilingualMode.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateTranslateLTMsg();
			}
		});

		chkTranslateLTMsg = new Button(cmpTmp, SWT.CHECK);
		chkTranslateLTMsg.setText("Auto-translate the messages from the LanguageTool checker (using Google Translate)");
		chkTranslateLTMsg.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateTranslateLTMsg();
			}
		});

		Composite cmpLTTrans = new Composite(cmpTmp, SWT.NONE);
		cmpLTTrans.setLayout(new GridLayout(2, false));
		cmpLTTrans.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		label = new Label(cmpLTTrans, SWT.NONE);
		label.setText("API Key:");

		edLTTranslationServiceKey = new Text(cmpLTTrans, SWT.BORDER);
		edLTTranslationServiceKey.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		edLTTranslationServiceKey.setEchoChar('*');

		label = new Label(cmpLTTrans, SWT.NONE);
		label.setText("From:");

		edLTTranslationSource = new Text(cmpLTTrans, SWT.BORDER);
		gdTmp = new GridData();
		gdTmp.widthHint = 100;
		edLTTranslationSource.setLayoutData(gdTmp);

		label = new Label(cmpLTTrans, SWT.NONE);
		label.setText("Into:");

		edLTTranslationTarget = new Text(cmpLTTrans, SWT.BORDER);
		gdTmp = new GridData();
		gdTmp.widthHint = 100;
		edLTTranslationTarget.setLayoutData(gdTmp);

		btStartLT = new Button(cmpTmp, SWT.PUSH);
		gdTmp = new GridData();
		gdTmp.verticalIndent = 16;
		btStartLT.setLayoutData(gdTmp);
		btStartLT.setText("Start LanguageTool from the Web");
		btStartLT.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				startLT();
			}
		});

		tiTmp = new TabItem(tabs, SWT.NONE);
		tiTmp.setText("LanguageTool");
		tiTmp.setControl(cmpTmp);


		//--- Terms tab

		cmpTmp = new Composite(tabs, SWT.NONE);
		layTmp = new GridLayout();
		cmpTmp.setLayout(layTmp);

		Group grpTerms = new Group(cmpTmp, SWT.NONE);
		grpTerms.setText("Terminology");
		grpTerms.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		grpTerms.setLayout(new GridLayout());

		label = new Label(grpTerms, SWT.NONE);
		label.setText("*** THIS FEATURE IS EXPERIMENTAL AND UNDER CONSTRUCTION ***");

		chkCheckTerms = new Button(grpTerms, SWT.CHECK);
		chkCheckTerms.setText("Verify terminology");
		chkCheckTerms.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateCheckTerms();
			}
		});

		stTermPath = new Label(grpTerms, SWT.NONE);
		stTermPath.setText("Full path of the glossary file to use:");

		pnlTermsPath = new TextAndBrowsePanel(grpTerms, SWT.NONE, false);
		pnlTermsPath.setBrowseFilters("TBX Documents (*.tbx)\tCSV Files (*.csv)\tTab-Delimited Files (*.txt)\tAll Files (*.*)",
				"*.tbx\t*.csv\t*.txt\t*.*");
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		pnlTermsPath.setLayoutData(gdTmp);

		chkStringMode = new Button(grpTerms, SWT.CHECK);
		chkStringMode.setText("Verify using strings matching");
		chkStringMode.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateStringMode();
			}
		});

		chkBetweenCodes = new Button(grpTerms, SWT.CHECK);
		chkBetweenCodes.setText("Strings must be between inline codes to match");

		Group grpBlacklist = new Group(cmpTmp, SWT.NONE);
		grpBlacklist.setText("Blacklist");
		grpBlacklist.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		grpBlacklist.setLayout(new GridLayout());

		chkCheckBlacklist = new Button(grpBlacklist, SWT.CHECK);
		chkCheckBlacklist.setText("Check for blacklisted terms");
		chkCheckBlacklist.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateBlacklistTerms();
			}
		});
		
		chkAllowBlacklistSub = new Button(grpBlacklist, SWT.CHECK);
		chkAllowBlacklistSub.setText("Detect substrings");
		
		chkBlacklistSrc = new Button(grpBlacklist, SWT.CHECK);
		chkBlacklistSrc.setText("Check source");

		stBlacklistPath = new Label(grpBlacklist, SWT.NONE);
		stBlacklistPath.setText("Full path of the blacklist file to use:");

		pnlBlacklistPath = new TextAndBrowsePanel(grpBlacklist, SWT.NONE, false);
		pnlBlacklistPath.setBrowseFilters("Tab-Delimited Files (*.txt)\tAll Files (*.*)",
				"*.txt\t*.*");
		pnlBlacklistPath.setLayoutData(gdTmp);

		tiTmp = new TabItem(tabs, SWT.NONE);
		tiTmp.setText("Terms");
		tiTmp.setControl(cmpTmp);


		//--- Other Settings tab

		cmpTmp = new Composite(tabs, SWT.NONE);
		layTmp = new GridLayout();
		cmpTmp.setLayout(layTmp);

		Group grpScope = new Group(cmpTmp, SWT.NONE);
		grpScope.setText("Scope");
		grpScope.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		grpScope.setLayout(new GridLayout());

		rdScopeAllEntries = new Button(grpScope, SWT.RADIO);
		rdScopeAllEntries.setText("Process all entries");

		rdScopeApprovedOnly = new Button(grpScope, SWT.RADIO);
		rdScopeApprovedOnly.setText("Process only approved entries (e.g. \"approved\" entries in TS files)");

		rdScopeNotApprovedOnly = new Button(grpScope, SWT.RADIO);
		rdScopeNotApprovedOnly.setText("Process only entries not approved (e.g. \"fuzzy\" entries in PO files)");

		Group grpTmp = new Group(cmpTmp, SWT.NONE);
		grpTmp.setText("Report output");
		grpTmp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		grpTmp.setLayout(new GridLayout(2, false));

		label = new Label(grpTmp, SWT.NONE);
		label.setText("Path of the report file:");
		gdTmp = new GridData();
		gdTmp.horizontalSpan = 2;
		label.setLayoutData(gdTmp);

		pnlOutputPath = new TextAndBrowsePanel(grpTmp, SWT.NONE, false);
		pnlOutputPath.setSaveAs(true);
		pnlOutputPath.setTitle("Quality Check Report");
		pnlOutputPath.setBrowseFilters("HTML Files (*.html;*.htm)\tAll Files (*.*)", "*.html;*.htm\t*.*");
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		gdTmp.horizontalSpan = 2;
		pnlOutputPath.setLayoutData(gdTmp);

		label = new Label(grpTmp, SWT.NONE);
		label.setText("Format of the report:");

		cbOutputType = new Combo(grpTmp, SWT.READ_ONLY | SWT.DROP_DOWN);
		cbOutputType.add("HTML file");
		cbOutputType.add("Tab-delimited file");
		cbOutputType.add("XML file");
		cbOutputType.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateOutputPathExtension();
			}
		});

		chkAutoOpen = new Button(grpTmp, SWT.CHECK);
		chkAutoOpen.setText("Open the report after completion");
		gdTmp = new GridData();
		gdTmp.horizontalSpan = 2;
		chkAutoOpen.setLayoutData(gdTmp);
		
		chkShowFullPath = new Button(grpTmp, SWT.CHECK);
		chkShowFullPath.setText("Show full paths on the report (otherwise the report shows relative paths)");
		gdTmp = new GridData();
		gdTmp.horizontalSpan = 2;
		chkShowFullPath.setLayoutData(gdTmp);

		// Save/Load buttons

		grpTmp = new Group(cmpTmp, SWT.NONE);
		grpTmp.setText("Overall Configuration");
		grpTmp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		grpTmp.setLayout(new GridLayout(3, false));

		Button btTmp = UIUtil.createGridButton(grpTmp, SWT.PUSH, "Import...", UIUtil.BUTTON_DEFAULT_WIDTH, 1);
		btTmp.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				importConfiguration();
			}
		});

		btTmp = UIUtil.createGridButton(grpTmp, SWT.PUSH, "Export...", UIUtil.BUTTON_DEFAULT_WIDTH, 1);
		btTmp.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				exportConfiguration();
			}
		});

		btTmp = UIUtil.createGridButton(grpTmp, SWT.PUSH, "Reset to Defaults...", UIUtil.BUTTON_DEFAULT_WIDTH * 2, 1);
		btTmp.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				resetToDefaults();
			}
		});

		if (stepMode) {
			chkSaveSession = new Button(cmpTmp, SWT.CHECK);
			chkSaveSession.setText("Save the session using the following path:");
			chkSaveSession.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					pnlSessionPath.setEnabled(chkSaveSession.getSelection());
				}
			});

			pnlSessionPath = new TextAndBrowsePanel(cmpTmp, SWT.NONE, false);
			pnlSessionPath.setSaveAs(true);
			pnlSessionPath.setTitle("Quality Check Session");
			pnlSessionPath.setBrowseFilters(
					String.format("Quality Check Sessions (*%s)\tAll Files (*.*)", QualityCheckSession.FILE_EXTENSION),
					String.format("*%s\t*.*", QualityCheckSession.FILE_EXTENSION));
			gdTmp = new GridData();
			gdTmp = new GridData(GridData.FILL_HORIZONTAL);
			pnlSessionPath.setLayoutData(gdTmp);
		}

		tiTmp = new TabItem(tabs, SWT.NONE);
		tiTmp.setText("Other Settings");
		tiTmp.setControl(cmpTmp);

		mainComposite.pack();
	}

	private boolean showDialog() {
		shell.open();
		while (!shell.isDisposed()) {
			if (!shell.getDisplay().readAndDispatch()) {
				shell.getDisplay().sleep();
			}
		}
		return result;
	}

	private void updateOutputPathExtension() {
		String tmp = pnlOutputPath.getText();
		String ext = ".html";
		if (cbOutputType.getSelectionIndex() == 1) {
			ext = ".txt";
		}
		if (cbOutputType.getSelectionIndex() == 2) {
			ext = ".xml";
		}
		if (tmp.endsWith(ext)) {
			return;
		}

		// Change the extension
		int n = tmp.lastIndexOf('.');
		if (n > -1) {
			tmp = tmp.substring(0, n);
		}
		tmp += ext;
		pnlOutputPath.setText(tmp);
	}

	private void exportConfiguration() {
		try {
			if (!saveData()) {
				return;
			}
			String path = Dialogs.browseFilenamesForSave(shell, "Export Configuration", null, null,
					String.format("Quality Check Configurations (*%s)\tAll Files (*.*)", Parameters.FILE_EXTENSION),
					String.format("*%s\t*.*", Parameters.FILE_EXTENSION));
			if (path == null) {
				return;
			}
			params.save(path);
		} catch (Throwable e) {
			Dialogs.showError(shell, "Error while saving configuration.\n" + e.getMessage(), null);
		}
	}

	private void importConfiguration() {
		try {
			String[] paths = Dialogs.browseFilenames(shell, "Import Configuration", false, null,
					String.format("Quality Check Configurations (*%s)\tAll Files (*.*)", Parameters.FILE_EXTENSION),
					String.format("*%s\t*.*", Parameters.FILE_EXTENSION));
			if (paths == null) {
				return;
			}
			params.load(Util.toURL(paths[0]), false);
			setData();
		} catch (Throwable e) {
			Dialogs.showError(shell, "Error while saving configuration.\n" + e.getMessage(), null);
		}
	}

	private void resetToDefaults() {
		try {
			// Ask confirmation
			MessageBox msgDlg = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO | SWT.CANCEL);
			msgDlg.setMessage("This command will reset all the configuration settings to their defaults.\n"
					+ "Do you want to proceed?");
			msgDlg.setText("Reset to Defaults");
			switch (msgDlg.open()) {
				case SWT.CANCEL:
				case SWT.NO:
					return; // Stop here
			}
			params.fromString("");
			setData();
		} catch (Throwable e) {
			Dialogs.showError(shell, "Error while resetting configuration.\n" + e.getMessage(), null);
		}
	}

	private void startLT() {
		try {
			UIUtil.start("http://www.languagetool.org/webstart/web/LanguageTool.jnlp"); //$NON-NLS-1$
			// Note: Bug on Windows-7: the security dialog box prompt for launching the
			// WebStart application causes the set of controls for the languageTool tab
			// to shift down out of the tab. It comes back when the tab is re-activated.
		} catch (Throwable e) {
			Dialogs.showError(shell, "Error starting languageTool from the Web.\n" + e.getMessage(), null);
		}
	}

	private void updateCharacters() {
		edCharset.setEnabled(chkCheckCharacters.getSelection());
		stExtraCharsAllowed.setEnabled(chkCheckCharacters.getSelection());
		edExtraCharsAllowed.setEnabled(chkCheckCharacters.getSelection());
	}

	private void updateCheckTerms() {
		stTermPath.setEnabled(chkCheckTerms.getSelection());
		pnlTermsPath.setEnabled(chkCheckTerms.getSelection());
		chkStringMode.setEnabled(chkCheckTerms.getSelection());
		updateStringMode();
	}

	private void updateStringMode() {
		chkBetweenCodes.setEnabled(chkStringMode.isEnabled() ? chkStringMode.getSelection() : false);
	}

	private void updateBlacklistTerms() {
		chkAllowBlacklistSub.setEnabled(chkCheckBlacklist.getSelection());
		chkBlacklistSrc.setEnabled(chkCheckBlacklist.getSelection());		
		stBlacklistPath.setEnabled(chkCheckBlacklist.getSelection());
		pnlBlacklistPath.setEnabled(chkCheckBlacklist.getSelection());
	}

	private void editPattern(boolean add) {
		addMode = add;
		dialog = new Shell(shell, SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.APPLICATION_MODAL);
		dialog.setText(add ? "Add Pattern Entry" : "Edit Pattern Entry");
		dialog.setMinimumSize(600, 200);
		dialog.setSize(dialog.getMinimumSize());
		dialog.setLayout(new GridLayout());
		dialog.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Source pattern
		Label label = new Label(dialog, SWT.NONE | SWT.WRAP);
		label.setText(String.format("Pattern for the source (use '%s' if expecting the same as the target):", PatternItem.SAME));

		edSource = new Text(dialog, SWT.BORDER);
		GridData gdTmp = new GridData(GridData.FILL_BOTH);
		edSource.setLayoutData(gdTmp);

		// Target correspondence
		label = new Label(dialog, SWT.NONE);
		label.setText(String.format("Pattern for the target (use '%s' if expecting the same as the source):", PatternItem.SAME));

		edTarget = new Text(dialog, SWT.BORDER);
		gdTmp = new GridData(GridData.FILL_BOTH);
		edTarget.setLayoutData(gdTmp);

		// Description
		label = new Label(dialog, SWT.NONE);
		label.setText(String.format("Description:", PatternItem.SAME));

		edDescription = new Text(dialog, SWT.BORDER);
		gdTmp = new GridData(GridData.FILL_BOTH);
		edDescription.setLayoutData(gdTmp);

		// Severity
		label = new Label(dialog, SWT.NONE);
		label.setText(String.format("Severity:", PatternItem.SAME));

		cbSeverity = new Combo(dialog, SWT.DROP_DOWN | SWT.READ_ONLY);
		for (String string : severityNames) {
			cbSeverity.add(string);
		}

		chkFromSource = new Button(dialog, SWT.CHECK);
		chkFromSource.setText("Perform the comparison from the source to the target");

		// Set the text in the edit fields
		if (add) {
			edTarget.setText(PatternItem.SAME);
			cbSeverity.select(Issue.DISPSEVERITY_MEDIUM);
			chkFromSource.setSelection(true);
		} else {
			int index = table.getSelectionIndex();
			if (index < 0) {
				return;
			}
			editItem = table.getItem(index);
			edSource.setText(editItem.getText(1));
			edTarget.setText(editItem.getText(2));
			edDescription.setText(editItem.getText(3));
			cbSeverity.select(getSeverityFromString(editItem.getText(0)));
			chkFromSource.setSelection(isFromSource(editItem.getText(0)));
		}

		//  Dialog buttons
		SelectionAdapter OKCancelActions = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if (event.widget.getData().equals("o")) {
					// Validate entries
					if (edSource.getText().trim().length() < 1) {
						Dialogs.showError(shell, "You must enter a pattern for the source.", null);
						edSource.selectAll();
						edSource.setFocus();
						return;
					}
					if (edTarget.getText().trim().length() < 1) {
						Dialogs.showError(shell, "You must enter a corresponding part for the target.", null);
						edTarget.selectAll();
						edTarget.setFocus();
						return;
					}
					if (edDescription.getText().trim().length() < 1) {
						Dialogs.showError(shell, "You must enter a description for the pattern.", null);
						edDescription.selectAll();
						edDescription.setFocus();
						return;
					}
					// Try patterns
					try {
						if (!edSource.getText().equals(PatternItem.SAME)) {
							Pattern.compile(edSource.getText());
						}
					} catch (Exception e) {
						Dialogs.showError(shell, "Pattern error:\n" + e.getLocalizedMessage(), null);
						edSource.selectAll();
						edSource.setFocus();
						return;
					}
					try {
						if (!edTarget.getText().equals(PatternItem.SAME)) {
							Pattern.compile(edTarget.getText());
						}
					} catch (Exception e) {
						Dialogs.showError(shell, "Pattern error:\n" + e.getLocalizedMessage(), null);
						edTarget.selectAll();
						edTarget.setFocus();
						return;
					}

					// Update the table
					if (addMode) { // Add a new item if needed
						editItem = new TableItem(table, SWT.NONE);
						editItem.setChecked(true);
						table.setSelection(table.getItemCount() - 1);
					}
					editItem.setText(0, (chkFromSource.getSelection() ? FROMSOURCE : FROMTARGET) + "/" + cbSeverity.getText());
					editItem.setText(1, edSource.getText());
					editItem.setText(2, edTarget.getText());
					editItem.setText(3, edDescription.getText());
					updatePatternsButtons();
				}
				// Close
				dialog.close();
			}
		};

		OKCancelPanel pnlActionsDialog = new OKCancelPanel(dialog, SWT.NONE, OKCancelActions, false);
		pnlActionsDialog.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		dialog.setDefaultButton(pnlActionsDialog.btOK);

		dialog.pack();
		Dialogs.centerWindow(dialog, shell);
		dialog.open();
		while (!dialog.isDisposed()) {
			if (!dialog.getDisplay().readAndDispatch()) {
				dialog.getDisplay().sleep();
			}
		}
	}

	private void removePattern() {
		int index = table.getSelectionIndex();
		if (index < 0) {
			return;
		}
		table.remove(index);
		int count = table.getItemCount();
		if (index > count - 1) {
			table.setSelection(table.getItemCount() - 1);
		} else {
			table.setSelection(index);
		}
		updatePatternsButtons();
	}

	private void moveItem(int offset) {
		int index = table.getSelectionIndex();
		int count = table.getItemCount();
		if (offset < 0) {
			if (index < 1) {
				return;
			}
		} else {
			if (index >= count - 1) {
				return;
			}
		}

		// Get the selected entry
		TableItem ti = table.getItem(index);
		boolean isChecked = ti.getChecked();
		String[] data = {ti.getText(0), ti.getText(1), ti.getText(2),
			(ti.getText(3)==null ? NODESC_LABEL : ti.getText(3))};
		ti.dispose();

		// Add the new item at the new place
		ti = new TableItem(table, SWT.NONE, index + offset);
		ti.setChecked(isChecked);
		ti.setText(data);

		// Update cursor and buttons
		table.select(index + offset);
		updateMoveButtons();
	}

	private void importPatterns() {
		try {
			String[] paths = Dialogs.browseFilenames(shell, "Import Patterns", false, null,
					"Patterns Files (*.txt)\tAll Files (*.*)", "*.txt\t*.*");
			if (paths == null) {
				return;
			}
			setPatternsData(PatternItem.loadFile(paths[0]));
			updatePatternsButtons();
		} catch (Throwable e) {
			Dialogs.showError(shell, e.getMessage(), null);
		}
	}

	private void exportPatterns() {
		try {
			String path = Dialogs.browseFilenamesForSave(shell, "Export Patterns", null, null,
					"Patterns Files (*.txt)\tAll Files (*.*)", "*.txt\t*.*");
			if (path == null) {
				return;
			}
			PatternItem.saveFile(path, savePatternsData());
		} catch (Throwable e) {
			Dialogs.showError(shell, e.getMessage(), null);
		}
	}

	private void updateLTOptions() {
		boolean enabled = chkCheckWithLT.getSelection();
		edServerURL.setEnabled(enabled);
		chkLTBilingualMode.setEnabled(enabled);
		chkTranslateLTMsg.setEnabled(enabled);
		if (enabled) {
			updateTranslateLTMsg();
		} else {
			edLTTranslationServiceKey.setEnabled(false);
			edLTTranslationSource.setEnabled(false);
			edLTTranslationTarget.setEnabled(false);
		}
	}

	private void updateTranslateLTMsg() {
		edLTTranslationServiceKey.setEnabled(chkTranslateLTMsg.getSelection());
		edLTTranslationSource.setEnabled(chkTranslateLTMsg.getSelection());
		edLTTranslationTarget.setEnabled(chkTranslateLTMsg.getSelection());
	}

	private void updateDoubledWord() {
		stDoubledWordExceptions.setEnabled(chkDoubledWord.getSelection());
		edDoubledWordExceptions.setEnabled(chkDoubledWord.getSelection());
	}

	private void updateTargetSameAsSourceForSameLanguage() {
		chkTargetSameAsSourceForSameLanguage.setEnabled(chkTargetSameAsSource.getSelection());
	}

	private void updateTargetSameAsSourceWithCodes() {
		chkTargetSameAsSourceWithCodes.setEnabled(chkTargetSameAsSource.getSelection());
	}

	private void updateAbsoluteMaxCharLength() {
		boolean enabled = chkAbsoluteMaxCharLength.getSelection();
		spAbsoluteMaxCharLength.setEnabled(enabled);
	}

	private void updateMaxCharLength() {
		boolean enabled = chkMaxCharLength.getSelection();
		for (Control ctrl : cmpMaxCharLength.getChildren()) {
			ctrl.setEnabled(enabled);
		}
	}

	private void updateMinCharLength() {
		boolean enabled = chkMinCharLength.getSelection();
		for (Control ctrl : cmpMinCharLength.getChildren()) {
			ctrl.setEnabled(enabled);
		}
	}

	private void updatePatterns() {
		boolean enabled = chkPatterns.getSelection();
		table.setEnabled(enabled);
		btAdd.setEnabled(enabled);
		btImport.setEnabled(enabled);
		if (enabled) {
			updatePatternsButtons();
		} else {
			btEdit.setEnabled(false);
			btRemove.setEnabled(false);
			btMoveUp.setEnabled(false);
			btMoveDown.setEnabled(false);
			btExport.setEnabled(false);
		}
	}

	private void updatePatternsButtons() {
		int index = table.getSelectionIndex();
		int count = table.getItemCount();
		btEdit.setEnabled(index != -1);
		btRemove.setEnabled(index != -1);
		updateMoveButtons();
		btExport.setEnabled(count > 0);
	}

	private void updateMoveButtons() {
		int index = table.getSelectionIndex();
		int count = table.getItemCount();
		btMoveUp.setEnabled(index > 0);
		btMoveDown.setEnabled(index < count - 1);
	}

	private void setData() {
		pnlOutputPath.setText(params.getOutputPath());
		cbOutputType.select(params.getOutputType());
		chkAutoOpen.setSelection(params.getAutoOpen());
		chkShowFullPath.setSelection(params.getShowFullPath());

		chkCodeDifference.setSelection(params.getCodeDifference());
		chkGuessOpenClose.setSelection(params.getGuessOpenClose());
		chkLeadingWS.setSelection(params.getLeadingWS());
		chkTrailingWS.setSelection(params.getTrailingWS());
		chkEmptyTarget.setSelection(params.getEmptyTarget());
		chkEmptySource.setSelection(params.getEmptySource());
		chkTargetSameAsSource.setSelection(params.getTargetSameAsSource());
		chkTargetSameAsSourceForSameLanguage.setSelection(params.getTargetSameAsSourceForSameLanguage());
		chkTargetSameAsSourceWithCodes.setSelection(params.getTargetSameAsSourceWithCodes());
		chkCheckWithLT.setSelection(params.getCheckWithLT());
		edServerURL.setText(params.getServerURL());
		chkTranslateLTMsg.setSelection(params.getTranslateLTMsg());
		chkLTBilingualMode.setSelection(params.getLtBilingualMode());
		edLTTranslationSource.setText(params.getLtTranslationSource());
		edLTTranslationTarget.setText(params.getLtTranslationTarget());
		edLTTranslationServiceKey.setText(params.getLtTranslationServiceKey());
		chkXliffSchema.setSelection(params.getCheckXliffSchema());
		chkPatterns.setSelection(params.getCheckPatterns());
		chkDoubledWord.setSelection(params.getDoubledWord());
		edDoubledWordExceptions.setText(params.getDoubledWordExceptions());
		chkCorruptedChars.setSelection(params.getCorruptedCharacters());

		chkStorageSize.setSelection(params.getCheckStorageSize());

		chkAbsoluteMaxCharLength.setSelection(params.getCheckAbsoluteMaxCharLength());
		spAbsoluteMaxCharLength.setSelection(params.getAbsoluteMaxCharLength());

		chkMaxCharLength.setSelection(params.getCheckMaxCharLength());
		spMaxCharLengthBreak.setSelection(params.getMaxCharLengthBreak());
		spMaxCharLengthAbove.setSelection(params.getMaxCharLengthAbove());
		spMaxCharLengthBelow.setSelection(params.getMaxCharLengthBelow());
		chkMinCharLength.setSelection(params.getCheckMinCharLength());
		spMinCharLengthBreak.setSelection(params.getMinCharLengthBreak());
		spMinCharLengthAbove.setSelection(params.getMinCharLengthAbove());
		spMinCharLengthBelow.setSelection(params.getMinCharLengthBelow());

		rdScopeAllEntries.setSelection(params.getScope() == Parameters.SCOPE_ALL);
		rdScopeApprovedOnly.setSelection(params.getScope() == Parameters.SCOPE_APPROVEDONLY);
		rdScopeNotApprovedOnly.setSelection(params.getScope() == Parameters.SCOPE_NOTAPPROVEDONLY);

		chkCheckAllowedCharacters.setSelection(params.getCheckAllowedCharacters());
		chkCheckCharacters.setSelection(params.getCheckCharacters());
		edCharset.setText(params.getCharset());
		edExtraCharsAllowed.setText(params.getExtraCharsAllowed());

		pnlMissingCodesAllowed.fillList(params.getMissingCodesAllowed());
		pnlExtraCodesAllowed.fillList(params.getExtraCodesAllowed());
		edTypesToIgnore.setText(params.getTypesToIgnore());

		chkCheckTerms.setSelection(params.getCheckTerms());
		pnlTermsPath.setText(params.getTermsPath());
		chkStringMode.setSelection(params.getStringMode());
		chkBetweenCodes.setSelection(params.getBetweenCodes());
		chkCheckBlacklist.setSelection(params.getCheckBlacklist());
		chkAllowBlacklistSub.setSelection(params.getAllowBlacklistSub());
		chkBlacklistSrc.setSelection(params.getBlacklistSrc());
		pnlBlacklistPath.setText(params.getBlacklistPath());

		setPatternsData(params.getPatterns());
		updateTargetSameAsSourceForSameLanguage();
		updateTargetSameAsSourceWithCodes();
		updatePatterns();
		updateDoubledWord();
		updateLTOptions();
		updateAbsoluteMaxCharLength();
		updateMaxCharLength();
		updateMinCharLength();
		updateCharacters();
		updateCheckTerms();
		updateBlacklistTerms();
		// Step-mode fields
		if (stepMode) {
			chkSaveSession.setSelection(params.getSaveSession());
			pnlSessionPath.setText(params.getSessionPath());
			pnlSessionPath.setEnabled(chkSaveSession.getSelection());
		}
	}

	private void setPatternsData(java.util.List<PatternItem> list) {
		table.removeAll();
		for (PatternItem item : list) {
			TableItem row = new TableItem(table, SWT.NONE);
			row.setChecked(item.enabled);
			row.setText(0, (item.fromSource ? FROMSOURCE : FROMTARGET) + "/" + severityNames[item.severity]);
			row.setText(1, item.source);
			row.setText(2, item.target);
			// Handle null for old sessions
			row.setText(3, item.description == null ? NODESC_LABEL : item.description);
		}
		if (table.getItemCount() > 0) {
			table.setSelection(0);
		}
	}

	private boolean saveData() {
		if (pnlOutputPath.getText().trim().length() == 0) {
			Dialogs.showError(shell, "Please, enter a path for the report.", null);
			tabs.setSelection(TAB_OTHER);
			pnlOutputPath.setFocus();
			return false;
		}
		if (chkCheckWithLT.getSelection()) {
			if (edServerURL.getText().trim().length() == 0) {
				Dialogs.showError(shell, "Please, enter a server URL.", null);
				tabs.setSelection(TAB_LANGUAGETOOL);
				edServerURL.setFocus();
				return false;
			}
			if (chkTranslateLTMsg.getSelection()) {
				if (edLTTranslationServiceKey.getText().trim().length() == 0) {
					Dialogs.showError(shell, "Please, enter the API key to access the MT service.", null);
					edLTTranslationServiceKey.setFocus();
					tabs.setSelection(TAB_LANGUAGETOOL);
					return false;

				}
				if (edLTTranslationSource.getText().trim().length() == 0) {
					Dialogs.showError(shell, "Please, enter the language code of the messages returned by LanguageTool (e.g. fr).", null);
					edLTTranslationSource.setFocus();
					tabs.setSelection(TAB_LANGUAGETOOL);
					return false;

				}
				if (edLTTranslationTarget.getText().trim().length() == 0) {
					Dialogs.showError(shell, "Please, enter the language to translate the LanguageTool messages into (e.g. en).", null);
					edLTTranslationTarget.setFocus();
					tabs.setSelection(TAB_LANGUAGETOOL);
					return false;
				}
			}
		}
		// Characters
		if (chkCheckCharacters.getSelection()) {
			String tmp = edExtraCharsAllowed.getText();
			if (tmp.isEmpty() && edCharset.getText().trim().isEmpty()) {
				Dialogs.showError(shell, "You must defined a character set encoding, or a list of allowed characters, or both.", null);
				tabs.setSelection(TAB_CHARACTERS);
				edCharset.setFocus();
				return false;
			}
			if (!tmp.isEmpty()) {
				try {
					Pattern.compile(tmp);
				} catch (Throwable e) {
					Dialogs.showError(shell, "Regular expression error:\n" + e.getMessage(), null);
					tabs.setSelection(TAB_CHARACTERS);
					edExtraCharsAllowed.setFocus();
					return false;
				}
			}
		}

		if (chkCheckTerms.getSelection()) {
			String tmp = pnlTermsPath.getText().trim();
			if (tmp.isEmpty()) {
				Dialogs.showError(shell, "You must specify a glossary file.", null);
				tabs.setSelection(TAB_TERMS);
				pnlTermsPath.setFocus();
				return false;
			}
		}

		if (chkCheckBlacklist.getSelection()) {
			String tmp = pnlBlacklistPath.getText().trim();
			if (tmp.isEmpty()) {
				Dialogs.showError(shell, "You must specify a blacklist file.", null);
				tabs.setSelection(TAB_TERMS);
				pnlBlacklistPath.setFocus();
				return false;
			}
		}

		if (stepMode) {
			if (chkSaveSession.getSelection()) {
				if (pnlSessionPath.getText().trim().length() == 0) {
					Dialogs.showError(shell, "Please, enter a path for the session.", null);
					pnlSessionPath.setFocus();
					return false;
				}
			}
		}

		params.setCheckStorageSize(chkStorageSize.getSelection());

		params.setCheckAbsoluteMaxCharLength(chkAbsoluteMaxCharLength.getSelection());
		if (chkAbsoluteMaxCharLength.getSelection()) {
			params.setAbsoluteMaxCharLength(spAbsoluteMaxCharLength.getSelection());
		}

		params.setCheckMaxCharLength(chkMaxCharLength.getSelection());
		if (chkMaxCharLength.getSelection()) {
			params.setMaxCharLengthBreak(spMaxCharLengthBreak.getSelection());
			params.setMaxCharLengthAbove(spMaxCharLengthAbove.getSelection());
			params.setMaxCharLengthBelow(spMaxCharLengthBelow.getSelection());
		}

		params.setCheckMinCharLength(chkMinCharLength.getSelection());
		if (chkMinCharLength.getSelection()) {
			params.setMinCharLengthBreak(spMinCharLengthBreak.getSelection());
			params.setMinCharLengthAbove(spMinCharLengthAbove.getSelection());
			params.setMinCharLengthBelow(spMinCharLengthBelow.getSelection());
		}

		java.util.List<String> list = params.getMissingCodesAllowed();
		list.clear();
		list.addAll(pnlMissingCodesAllowed.getList());

		list = params.getExtraCodesAllowed();
		list.clear();
		list.addAll(pnlExtraCodesAllowed.getList());

		String tmp = edTypesToIgnore.getText().trim();
		if (!tmp.isEmpty() && (Util.getLastChar(tmp) != ';')) {
			tmp = tmp.replace(" ", "");
			tmp += ";";
		}
		params.setTypesToIgnore(tmp);

		if (rdScopeApprovedOnly.getSelection()) {
			params.setScope(Parameters.SCOPE_APPROVEDONLY);
		} else if (rdScopeNotApprovedOnly.getSelection()) {
			params.setScope(Parameters.SCOPE_NOTAPPROVEDONLY);
		} else {
			params.setScope(Parameters.SCOPE_ALL);
		}

		params.setOutputPath(pnlOutputPath.getText());
		params.setOutputType(cbOutputType.getSelectionIndex());
		params.setCodeDifference(chkCodeDifference.getSelection());
		params.setGuessOpenClose(chkGuessOpenClose.getSelection());
		params.setAutoOpen(chkAutoOpen.getSelection());
		params.setShowFullPath(chkShowFullPath.getSelection());
		params.setLeadingWS(chkLeadingWS.getSelection());
		params.setTrailingWS(chkTrailingWS.getSelection());
		params.setEmptyTarget(chkEmptyTarget.getSelection());
		params.setEmptySource(chkEmptySource.getSelection());
		params.setTargetSameAsSource(chkTargetSameAsSource.getSelection());
		params.setDoubledWord(chkDoubledWord.getSelection());
		params.setDoubledWordExceptions(edDoubledWordExceptions.getText());
		params.setCorruptedCharacters(chkCorruptedChars.getSelection());
		if (chkTargetSameAsSourceForSameLanguage.isEnabled()) {
			params.setTargetSameAsSourceForSameLanguage(chkTargetSameAsSourceForSameLanguage.getSelection());
		}
		if (chkTargetSameAsSourceWithCodes.isEnabled()) {
			params.setTargetSameAsSourceWithCodes(chkTargetSameAsSourceWithCodes.getSelection());
		}
		params.setCheckWithLT(chkCheckWithLT.getSelection());
		if (chkCheckWithLT.getSelection()) {
			params.setServerURL(edServerURL.getText());
			params.setLtBilingualMode(chkLTBilingualMode.getSelection());
			params.setTranslateLTMsg(chkTranslateLTMsg.getSelection());
			if (chkTranslateLTMsg.getSelection()) {
				params.setLtTranslationServiceKey(edLTTranslationServiceKey.getText());
				params.setLtTranslationSource(edLTTranslationSource.getText());
				params.setLtTranslationTarget(edLTTranslationTarget.getText());
			}
		}

		params.setCheckAllowedCharacters(chkCheckAllowedCharacters.getSelection());

		params.setCheckCharacters(chkCheckCharacters.getSelection());
		if (chkCheckCharacters.getSelection()) {
			params.setCharset(edCharset.getText().trim());
			params.setExtraCharsAllowed(edExtraCharsAllowed.getText());
		}

		params.setCheckTerms(chkCheckTerms.getSelection());
		params.setTermsPath(pnlTermsPath.getText());
		params.setStringMode(chkStringMode.getSelection());
		params.setBetweenCodes(chkBetweenCodes.getSelection());

		params.setCheckBlacklist(chkCheckBlacklist.getSelection());
		params.setAllowBlacklistSub(chkAllowBlacklistSub.getSelection());		
		params.setBlacklistSrc(chkBlacklistSrc.getSelection());		
		params.setblacklistPath(pnlBlacklistPath.getText());

		if (stepMode) {
			params.setSaveSession(chkSaveSession.getSelection());
			if (chkSaveSession.getSelection()) {
				params.setSessionPath(pnlSessionPath.getText());
			}
		}
		params.setCheckXliffSchema(chkXliffSchema.getSelection());
		params.setCheckPatterns(chkPatterns.getSelection());
		params.setPatterns(savePatternsData());
		result = true;
		return result;
	}

	private java.util.List<PatternItem> savePatternsData() {
		java.util.List<PatternItem> list = new ArrayList<PatternItem>();
		for (int i = 0; i < table.getItemCount(); i++) {
			// Get the info
			int severity = getSeverityFromString(table.getItem(i).getText(0));
			PatternItem pattern = new PatternItem(table.getItem(i).getText(1), table.getItem(i).getText(2),
					table.getItem(i).getChecked(), severity, table.getItem(i).getText(3));
			pattern.fromSource = isFromSource(table.getItem(i).getText(0));
			// Add the pattern
			list.add(pattern);
		}
		return list;
	}

	private boolean isFromSource(String value) {
		// "SRC/SEVERITY" direction==source if start text==FROMSOURCE 
		return value.subSequence(0, 3).equals(FROMSOURCE);
	}

	private int getSeverityFromString(String value) {
		// "SRC/SEVERITY" severity start at index 4;
		String tmp = value.substring(4);
		if (tmp.equals(severityNames[Issue.DISPSEVERITY_LOW])) {
			return Issue.DISPSEVERITY_LOW;
		}
		if (tmp.equals(severityNames[Issue.DISPSEVERITY_MEDIUM])) {
			return Issue.DISPSEVERITY_MEDIUM;
		}
		return Issue.DISPSEVERITY_HIGH;
	}
}
