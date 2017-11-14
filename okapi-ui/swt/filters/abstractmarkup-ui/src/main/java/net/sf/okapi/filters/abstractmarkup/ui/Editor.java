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

package net.sf.okapi.filters.abstractmarkup.ui;

import java.util.ArrayList;
import java.util.Map;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.IContext;
import net.sf.okapi.common.IHelp;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.IParametersEditor;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.filters.IFilterConfigurationListEditor;
import net.sf.okapi.common.filters.IFilterConfigurationMapper;
import net.sf.okapi.common.ui.Dialogs;
import net.sf.okapi.common.ui.InputDialog;
import net.sf.okapi.common.ui.OKCancelPanel;
import net.sf.okapi.common.ui.UIUtil;
import net.sf.okapi.common.ui.filters.FilterConfigurationEditor;
import net.sf.okapi.common.ui.filters.InlineCodeFinderPanel;
import net.sf.okapi.filters.abstractmarkup.AbstractMarkupParameters;
import net.sf.okapi.filters.abstractmarkup.config.TaggedFilterConfiguration;
import net.sf.okapi.filters.abstractmarkup.config.TaggedFilterConfiguration.RULE_TYPE;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

@EditorFor(AbstractMarkupParameters.class)
public class Editor implements IParametersEditor {

	private static final int ATTRULES_TRANS = 0;
	private static final int ATTRULES_WRITABLE = 1;
	private static final int ATTRULES_READONLY = 2;
	private static final int ATTRULES_ID = 3;
	private static final int ATTRULES_PRESERVE_WHITESPACE = 4;
	
	private static final int ELEMENTS_TEXTUNIT = 0;
	private static final int ELEMENTS_EXCLUDED = 1;
	private static final int ELEMENTS_INCLUDED = 2;
	private static final int ELEMENTS_INLINE = 3;
	private static final int ELEMENTS_ATTRIBUTESONLY = 4;
	private static final int ELEMENTS_GROUP = 5;
	private static final int ELEMENTS_SCRIPT = 6;
	private static final int ELEMENTS_SERVER = 7;
	
	private static final int TAB_ELEMENTS = 0;
	private static final int TAB_ATTRIBUTES = 1;
	
	private IFilterConfigurationMapper fcMapper;
	private Shell shell;
	private boolean result = false;
	private AbstractMarkupParameters params;
	private TabFolder tabs;
	private IHelp help;
	private Button chkWellformed;
	private Button chkGlobalPreserveWS;
	private List lbAtt;
	private Table tblAttRules;
	private Button rdAttrAllElements;
	private Button rdAttOnlyThese;
	private Button rdAttExceptThese;
	private Text edAttScopeElements;
	private Element currentElem;
	private Attribute currentAtt;
	private Button btRemoveAtt;
	private Button btRemoveElem;
	private Group grpWS;
	private Group grpAttCond;
	private Text edAttConditions;
	private Text edPreserveWS;
	private Text edDefaultWS;
	private Button chkUseCodeFinder;
	private InlineCodeFinderPanel pnlCodeFinder;
	private Text edCDATAFilterConfig;
	private Button btGetCDATAFilterConfig;
	private List lbElem;
	private Table tblElemRules;
	private Group grpElemContentFilter;
	private Text edElemFilterConfig;
	private Button btGetElemFilterConfig;
	private Group grpElemCond;
	private Text edElemConditions;
	private Label stElemTransAtt;
	private Text edElemTransAtt;
	private Label stElemWriteableAtt;
	private Text edElemWriteableAtt;
	private Label stElemReadOnlyAtt;
	private Text edElemReadOnlyAtt;

	@Override
	public boolean edit (IParameters options,
		boolean readOnly,
		IContext context)
	{
		help = (IHelp)context.getObject("help");
		fcMapper = (IFilterConfigurationMapper)context.getObject("fcMapper");
		
		boolean bRes = false;
		shell = null;
		params = (AbstractMarkupParameters)options;
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
		return new AbstractMarkupParameters();
	}
	
	private void create (Shell parent,
		boolean readOnly)
	{
		shell.setText(params.getEditorTitle());
		if ( parent != null ) UIUtil.inheritIcon(shell, parent);
		GridLayout layTmp = new GridLayout();
		layTmp.marginBottom = 0;
		layTmp.verticalSpacing = 0;
		shell.setLayout(layTmp);

		tabs = new TabFolder(shell, SWT.NONE);
		GridData gdTmp = new GridData(GridData.FILL_BOTH);
		tabs.setLayoutData(gdTmp);

		//=== Elements tab
		
		Composite cmpTmp = new Composite(tabs, SWT.NONE);
		layTmp = new GridLayout(2, false);
		cmpTmp.setLayout(layTmp);
		
		lbElem = new List(cmpTmp, SWT.BORDER | SWT.V_SCROLL);
		gdTmp = new GridData(GridData.FILL_BOTH);
		gdTmp.verticalSpan = 10;
		lbElem.setLayoutData(gdTmp);
		lbElem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateElement();
			};
		});
		
		Label label = new Label(cmpTmp, SWT.NONE);
		label.setText("Rules for the selected element:");
		
		tblElemRules = new Table(cmpTmp, SWT.BORDER | SWT.CHECK | SWT.FULL_SELECTION);
		tblElemRules.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL));
		TableColumn col1 = new TableColumn(tblElemRules, SWT.NONE);
		TableColumn col2 = new TableColumn(tblElemRules, SWT.NONE);
		TableItem item = new TableItem(tblElemRules, SWT.NONE);
		item.setData(ELEMENTS_TEXTUNIT);
		item.setText(0, "Translatable Text unit"); item.setText(1, RULE_TYPE.TEXT_UNIT_ELEMENT.toString());
		item = new TableItem(tblElemRules, SWT.NONE);
		item.setData(ELEMENTS_EXCLUDED);
		item.setText(0, "Not translatable"); item.setText(1, RULE_TYPE.EXCLUDED_ELEMENT.toString());
		item = new TableItem(tblElemRules, SWT.NONE);
		item.setData(ELEMENTS_INCLUDED);
		item.setText(0, "Translatable (inside non-translatable)"); item.setText(1, RULE_TYPE.INCLUDED_ELEMENT.toString());
		item = new TableItem(tblElemRules, SWT.NONE);
		item.setData(ELEMENTS_INLINE);
		item.setText(0, "In-line (internal tag)"); item.setText(1, RULE_TYPE.INLINE_ELEMENT.toString());
		item = new TableItem(tblElemRules, SWT.NONE);
		item.setData(ELEMENTS_ATTRIBUTESONLY);
		item.setText(0, "Some attributes need processing"); item.setText(1, RULE_TYPE.ATTRIBUTES_ONLY.toString());
		item = new TableItem(tblElemRules, SWT.NONE);
		item.setData(ELEMENTS_GROUP);
		item.setText(0, "Group"); item.setText(1, RULE_TYPE.GROUP_ELEMENT.toString());
		item = new TableItem(tblElemRules, SWT.NONE);
		item.setData(ELEMENTS_SCRIPT);
		item.setText(0, "Script"); item.setText(1, RULE_TYPE.SCRIPT_ELEMENT.toString());
		item = new TableItem(tblElemRules, SWT.NONE);
		item.setData(ELEMENTS_SERVER);
		item.setText(0, "Server-side"); item.setText(1, RULE_TYPE.SERVER_ELEMENT.toString());
		col1.pack();
		col2.pack();
		tblElemRules.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if ( event.detail == SWT.CHECK ) {
					updateElementRules((TableItem)event.item);
				}
            }
		});
		
		//--- Element content filter group 
		
		grpElemContentFilter = new Group(cmpTmp, SWT.NONE);
		grpElemContentFilter.setText("Process the content using the following filter configuration:");
		grpElemContentFilter.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		grpElemContentFilter.setLayout(new GridLayout(2, false));
		
		edElemFilterConfig = new Text(grpElemContentFilter, SWT.BORDER);
		edElemFilterConfig.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		btGetElemFilterConfig = new Button(grpElemContentFilter, SWT.PUSH);
		btGetElemFilterConfig.setText("...");
		btGetElemFilterConfig.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				browserFilterConfiguration(edElemFilterConfig);
			};
		});

		//--- Element conditions group
		
		grpElemCond = new Group(cmpTmp, SWT.NONE);
		grpElemCond.setLayout(new GridLayout(3, false));
		gdTmp = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
		grpElemCond.setLayoutData(gdTmp);
		grpElemCond.setText("Conditions:");
		
		edElemConditions = new Text(grpElemCond, SWT.BORDER);
		edElemConditions.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		edElemConditions.setEditable(false);
		
		Button btTmp = new Button(grpElemCond, SWT.PUSH);
		btTmp.setText("Edit...");
		btTmp.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if ( currentElem == null ) return;
				if ( currentElem.conditions == null ) {
					currentElem.conditions = new ArrayList<Condition>();
				}
				editConditions(edElemConditions, currentElem.conditions);
            }
		});

		stElemTransAtt = new Label(cmpTmp, SWT.NONE);
		stElemTransAtt.setText("Translatable attributes:");

		edElemTransAtt = new Text(cmpTmp, SWT.BORDER);
		edElemTransAtt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		stElemWriteableAtt = new Label(cmpTmp, SWT.NONE);
		stElemWriteableAtt.setText("Attributes that are modifiable properties:");

		edElemWriteableAtt = new Text(cmpTmp, SWT.BORDER);
		edElemWriteableAtt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		stElemReadOnlyAtt = new Label(cmpTmp, SWT.NONE);
		stElemReadOnlyAtt.setText("Attributes that are read-only properties:");
		
		edElemReadOnlyAtt = new Text(cmpTmp, SWT.BORDER);
		edElemReadOnlyAtt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING));
		
		//--- Add/Remove buttons for the list of elements
		
		Composite cmpButtons = new Composite(cmpTmp, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = layout.marginWidth = 0;
		cmpButtons.setLayout(layout);
		cmpButtons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_END));
		
		Button btAdd = UIUtil.createGridButton(cmpButtons, SWT.PUSH, "Add...", UIUtil.BUTTON_DEFAULT_WIDTH, 1);
		btAdd.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				addElement();
            }
		});
		
		btRemoveElem = UIUtil.createGridButton(cmpButtons, SWT.PUSH, "Remove", UIUtil.BUTTON_DEFAULT_WIDTH, 1);
		btRemoveElem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				removeElement();
            }
		});
		
		TabItem tiTmp = new TabItem(tabs, SWT.NONE);
		tiTmp.setText("Elements");
		tiTmp.setControl(cmpTmp);
		
		
		//=== Attributes tab
		
		cmpTmp = new Composite(tabs, SWT.NONE);
		layTmp = new GridLayout(2, false);
		cmpTmp.setLayout(layTmp);
		
		lbAtt = new List(cmpTmp, SWT.BORDER | SWT.V_SCROLL);
		gdTmp = new GridData(GridData.FILL_BOTH);
		gdTmp.verticalSpan = 5;
		lbAtt.setLayoutData(gdTmp);
		lbAtt.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateAttribute();
			};
		});

		label = new Label(cmpTmp, SWT.NONE);
		label.setText("Rules for the selected attribute:");
		
		tblAttRules = new Table(cmpTmp, SWT.BORDER | SWT.CHECK | SWT.FULL_SELECTION);
		tblAttRules.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL));
		col1 = new TableColumn(tblAttRules, SWT.NONE);
		col2 = new TableColumn(tblAttRules, SWT.NONE);
		item = new TableItem(tblAttRules, SWT.NONE);
		item.setData(ATTRULES_TRANS);
		item.setText(0, "Translatable text"); item.setText(1, RULE_TYPE.ATTRIBUTE_TRANS.toString());
		item = new TableItem(tblAttRules, SWT.NONE);
		item.setData(ATTRULES_WRITABLE);
		item.setText(0, "Modifiable property"); item.setText(1, RULE_TYPE.ATTRIBUTE_WRITABLE.toString());
		item = new TableItem(tblAttRules, SWT.NONE);
		item.setData(ATTRULES_READONLY);
		item.setText(0, "Read-only property"); item.setText(1, RULE_TYPE.ATTRIBUTE_READONLY.toString());
		item = new TableItem(tblAttRules, SWT.NONE);
		item.setData(ATTRULES_ID);
		item.setText(0, "Identifier"); item.setText(1, RULE_TYPE.ATTRIBUTE_ID.toString());
		item = new TableItem(tblAttRules, SWT.NONE);
		item.setData(ATTRULES_PRESERVE_WHITESPACE);
		item.setText(0, "Preserve element's whitespaces"); item.setText(1, RULE_TYPE.ATTRIBUTE_PRESERVE_WHITESPACE.toString());
		col1.pack();
		col2.pack();
		tblAttRules.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if ( event.detail == SWT.CHECK ) {
					updateAttributeRules((TableItem)event.item);
				}
            }
		});
		
		//--- Attribute conditions group
		
		grpAttCond = new Group(cmpTmp, SWT.NONE);
		grpAttCond.setLayout(new GridLayout(3, false));
		gdTmp = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
		grpAttCond.setLayoutData(gdTmp);
		grpAttCond.setText("Conditions");
		
		edAttConditions = new Text(grpAttCond, SWT.BORDER);
		edAttConditions.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		edAttConditions.setEditable(false);
		
		btTmp = new Button(grpAttCond, SWT.PUSH);
		btTmp.setText("Edit...");
		btTmp.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if ( currentAtt == null ) return;
				if ( currentAtt.conditions == null ) {
					currentAtt.conditions = new ArrayList<Condition>();
				}
				editConditions(edAttConditions, currentAtt.conditions);
            }
		});

		//--- Attribute scope group
		
		Group grpTmp = new Group(cmpTmp, SWT.NONE);
		grpTmp.setLayout(new GridLayout());
		gdTmp = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
		grpTmp.setLayoutData(gdTmp);
		grpTmp.setText("Scope");
		
		rdAttrAllElements = new Button(grpTmp, SWT.RADIO); 
		rdAttrAllElements.setText("Applies to all elements");
		rdAttrAllElements.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateScopeElements();
			};
		});
		
		rdAttOnlyThese = new Button(grpTmp, SWT.RADIO);
		rdAttOnlyThese.setText("Apply only for the following elements:");
		rdAttOnlyThese.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateScopeElements();
			};
		});
		
		rdAttExceptThese = new Button(grpTmp, SWT.RADIO);
		rdAttExceptThese.setText("Apply to all elements excepted for the following ones:");
		rdAttExceptThese.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateScopeElements();
			};
		});
		
		edAttScopeElements = new Text(grpTmp, SWT.BORDER);
		edAttScopeElements.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		//--- White spaces group
		
		grpWS = new Group(cmpTmp, SWT.NONE);
		grpWS.setLayout(new GridLayout(3, false));
		gdTmp = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
		gdTmp.verticalSpan = 2;
		grpWS.setLayoutData(gdTmp);
		grpWS.setText("White spaces conditions");
		
		Label stTmp = new Label(grpWS, SWT.NONE);
		stTmp.setText("Preserve:");
		
		edPreserveWS = new Text(grpWS, SWT.BORDER);
		edPreserveWS.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		edPreserveWS.setEditable(false);
		
		btTmp = new Button(grpWS, SWT.PUSH);
		btTmp.setText("Edit...");
		btTmp.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if ( currentAtt == null ) return;
				if ( currentAtt.wsPreserve == null ) {
					currentAtt.wsPreserve = new ArrayList<Condition>();
				}
				editConditions(edPreserveWS, currentAtt.wsPreserve);
            }
		});
		
		stTmp = new Label(grpWS, SWT.NONE);
		stTmp.setText("Default:");
		
		edDefaultWS = new Text(grpWS, SWT.BORDER);
		edDefaultWS.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		edDefaultWS.setEditable(false);
		
		btTmp = new Button(grpWS, SWT.PUSH);
		btTmp.setText("Edit...");
		btTmp.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if ( currentAtt == null ) return;
				if ( currentAtt.wsDefault == null ) {
					currentAtt.wsDefault = new ArrayList<Condition>();
				}
				editConditions(edDefaultWS, currentAtt.wsDefault);
            }
		});
		
		//--- Add/Remove buttons for the list of attributes
		
		cmpButtons = new Composite(cmpTmp, SWT.NONE);
		layout = new GridLayout(2, false);
		layout.marginHeight = layout.marginWidth = 0;
		cmpButtons.setLayout(layout);
		cmpButtons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_END));
		
		btAdd = UIUtil.createGridButton(cmpButtons, SWT.PUSH, "Add...", UIUtil.BUTTON_DEFAULT_WIDTH, 1);
		btAdd.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				addAttribute();
            }
		});
		
		btRemoveAtt = UIUtil.createGridButton(cmpButtons, SWT.PUSH, "Remove", UIUtil.BUTTON_DEFAULT_WIDTH, 1);
		btRemoveAtt.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				removeAttribute();
            }
		});
		
		tiTmp = new TabItem(tabs, SWT.NONE);
		tiTmp.setText("Attributes");
		tiTmp.setControl(cmpTmp);
		

		//=== Inline codes tab
		
		cmpTmp = new Composite(tabs, SWT.NONE);
		layTmp = new GridLayout();
		cmpTmp.setLayout(layTmp);
		
		chkUseCodeFinder = new Button(cmpTmp, SWT.CHECK);
		chkUseCodeFinder.setText("Has inline codes as defined below:");
		chkUseCodeFinder.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateInlineCodes();
			};
		});
		
		pnlCodeFinder = new InlineCodeFinderPanel(cmpTmp, SWT.NONE);
		pnlCodeFinder.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		tiTmp = new TabItem(tabs, SWT.NONE);
		tiTmp.setText("Inline Codes");
		tiTmp.setControl(cmpTmp);
		
		
		//=== General tab
		
		cmpTmp = new Composite(tabs, SWT.NONE);
		layTmp = new GridLayout();
		cmpTmp.setLayout(layTmp);

		chkWellformed = new Button(cmpTmp, SWT.CHECK);
		chkWellformed.setText("Assumes the documents are well-formed");
		gdTmp = new GridData();
		gdTmp.verticalIndent = 16;
		chkWellformed.setLayoutData(gdTmp);

		chkGlobalPreserveWS = new Button(cmpTmp, SWT.CHECK);
		chkGlobalPreserveWS.setText("Preserve white-spaces unless otherwise specified");
		
		grpTmp = new Group(cmpTmp, SWT.NONE);
		grpTmp.setText("Process CDATA sections using this filter configuration (leave empty for none):");
		grpTmp.setLayout(new GridLayout(2, false));
		grpTmp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		edCDATAFilterConfig = new Text(grpTmp, SWT.BORDER);
		edCDATAFilterConfig.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		btGetCDATAFilterConfig = new Button(grpTmp, SWT.PUSH);
		btGetCDATAFilterConfig.setText("...");
		btGetCDATAFilterConfig.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				browserFilterConfiguration(edCDATAFilterConfig);
			};
		});
		
		tiTmp = new TabItem(tabs, SWT.NONE);
		tiTmp.setText("General");
		tiTmp.setControl(cmpTmp);
		
		
		//--- Dialog-level buttons

		SelectionAdapter OKCancelActions = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				result = false;
				if ( e.widget.getData().equals("h") ) {
					if ( help != null ) help.showTopic(this, "index");
					return;
				}
				if ( e.widget.getData().equals("o") ) {
					if ( !saveData() ) return;
					result = true;
				}
				shell.close();
			};
		};
		OKCancelPanel pnlActions = new OKCancelPanel(shell, SWT.NONE, OKCancelActions, (help!=null));
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
		if ( startSize.x < 400 ) startSize.x = 400; 
		if ( startSize.y < 300 ) startSize.y = 300; 
		shell.setSize(startSize);
		Dialogs.centerWindow(shell, parent);
		setData();
	}
	
	private void browserFilterConfiguration (Text edField) {
		try {
			IFilterConfigurationListEditor fcEditor = new FilterConfigurationEditor();
			String res = fcEditor.editConfigurations(fcMapper, edField.getText());
			if ( res != null ) {
				edField.setText(res);
				edField.selectAll();
				edField.setFocus();
			}
		}
		catch ( Throwable e ) {
			Dialogs.showError(shell, e.getLocalizedMessage(), null);
		}
	}
	
	private void removeElement () {
		int n = lbElem.getSelectionIndex();
		if ( n < 0 ) return;
		lbElem.remove(n);
		if ( n >= lbElem.getItemCount() ) n = lbElem.getItemCount()-1;
		if ( n > -1 ) lbElem.setSelection(n);
		updateElementsButtons();
		updateElement();
	}
	
	private void removeAttribute () {
		int n = lbAtt.getSelectionIndex();
		if ( n < 0 ) return;
		lbAtt.remove(n);
		if ( n >= lbAtt.getItemCount() ) n = lbAtt.getItemCount()-1;
		if ( n > -1 ) lbAtt.setSelection(n);
		updateAttributesButtons();
		updateAttribute();
	}
	
	private void addElement () {
		try {
			InputDialog dlg = new InputDialog(shell, "Add Element", "Name of the element to add:", null, null, 0, -1, -1);
			String name = dlg.showDialog();
			if ( name == null ) return;
			name = ensureValidName(name);
			if ( name.isEmpty() ) return;
			
			for ( String tmp : lbElem.getItems() ) {
				if ( tmp.equals(name) ) {
					Dialogs.showError(shell,
						String.format("The element \"%s\" is already listed.", name), null);
					return;
				}
			}
			// Else: add the element
			Element elem = new Element();
			elem.name = name;
			lbElem.add(name);
			lbElem.setData(name, elem);
			lbElem.setSelection(lbElem.getItemCount()-1);
			updateElementsButtons();
			updateElement();
		}
		catch ( Throwable e ) {
			Dialogs.showError(shell, "Error when adding an attribute.\n"+e.getMessage(), null);
		}
	}
	
	private void addAttribute () {
		try {
			InputDialog dlg = new InputDialog(shell, "Add Attribute", "Name of the attribute to add:", null, null, 0, -1, -1);
			String name = dlg.showDialog();
			if ( name == null ) return;
			name = ensureValidName(name);
			if ( name.isEmpty() ) return;
			
			for ( String tmp : lbAtt.getItems() ) {
				if ( tmp.equals(name) ) {
					Dialogs.showError(shell,
						String.format("The attribute \"%s\" is already listed.", name), null);
					return;
				}
			}
			// Else: add the attribute
			Attribute att = new Attribute();
			att.name = name;
			lbAtt.add(name);
			lbAtt.setData(name, att);
			lbAtt.setSelection(lbAtt.getItemCount()-1);
			updateAttributesButtons();
			updateAttribute();
		}
		catch ( Throwable e ) {
			Dialogs.showError(shell, "Error when adding an attribute.\n"+e.getMessage(), null);
		}
	}
	
	private void updateInlineCodes () {
		pnlCodeFinder.setEnabled(chkUseCodeFinder.getSelection());
	}

	private void updateElementsButtons () {
		btRemoveElem.setEnabled(lbElem.getItemCount()>0);
	}
	
	private void updateAttributesButtons () {
		btRemoveAtt.setEnabled(lbAtt.getItemCount()>0);
	}
	
	// Actions to do when a rule type is being checked or un-checked
	private void updateElementRules (TableItem item) {
		// Other cases are affected only when checking the option
		if ( item.getChecked() ) {
			switch ( (Integer)item.getData() ) {
			case ELEMENTS_EXCLUDED:
				tblElemRules.getItem(ELEMENTS_INCLUDED).setChecked(false);
				break;
			case ELEMENTS_INCLUDED:
				tblElemRules.getItem(ELEMENTS_EXCLUDED).setChecked(false);
				break;
			}
		}
	}
	
	// Actions to do when a rule type is being checked or un-checked
	private void updateAttributeRules (TableItem item) {
		// Treat case for preserve white-spaces first
		if ( (Integer)item.getData() == ATTRULES_PRESERVE_WHITESPACE ) {
			updateWhiteSpaces();
			return;
		}
		// Other cases are affected only when checking the option
		if ( item.getChecked() ) {
			switch ( (Integer)item.getData() ) {
			case ATTRULES_TRANS:
				tblAttRules.getItem(ATTRULES_WRITABLE).setChecked(false);
				tblAttRules.getItem(ATTRULES_READONLY).setChecked(false);
				break;
			case ATTRULES_WRITABLE:
				tblAttRules.getItem(ATTRULES_TRANS).setChecked(false);
				tblAttRules.getItem(ATTRULES_READONLY).setChecked(false);
				break;
			case ATTRULES_READONLY:
				tblAttRules.getItem(ATTRULES_WRITABLE).setChecked(false);
				tblAttRules.getItem(ATTRULES_TRANS).setChecked(false);
				break;
			}
		}
	}
	
	private void updateWhiteSpaces () {
		boolean enabled = tblAttRules.getItem(ATTRULES_PRESERVE_WHITESPACE).getChecked();
		grpWS.setEnabled(enabled);
		for ( Control ctrl : grpWS.getChildren() ) {
			ctrl.setEnabled(enabled);
		}
	}
	
	private void updateScopeElements () {
		edAttScopeElements.setEnabled(!rdAttrAllElements.getSelection());
	}

	private boolean saveElement () {
		if ( currentElem == null ) return true;
		currentElem.rules.clear();
		if ( tblElemRules.getItem(ELEMENTS_TEXTUNIT).getChecked() ) currentElem.rules.add(RULE_TYPE.TEXT_UNIT_ELEMENT);
		if ( tblElemRules.getItem(ELEMENTS_EXCLUDED).getChecked() ) currentElem.rules.add(RULE_TYPE.EXCLUDED_ELEMENT);
		if ( tblElemRules.getItem(ELEMENTS_INCLUDED).getChecked() ) currentElem.rules.add(RULE_TYPE.INCLUDED_ELEMENT);
		if ( tblElemRules.getItem(ELEMENTS_INLINE).getChecked() ) currentElem.rules.add(RULE_TYPE.INLINE_ELEMENT);
		if ( tblElemRules.getItem(ELEMENTS_ATTRIBUTESONLY).getChecked() ) currentElem.rules.add(RULE_TYPE.ATTRIBUTES_ONLY);
		if ( tblElemRules.getItem(ELEMENTS_GROUP).getChecked() ) currentElem.rules.add(RULE_TYPE.GROUP_ELEMENT);
		if ( tblElemRules.getItem(ELEMENTS_SCRIPT).getChecked() ) currentElem.rules.add(RULE_TYPE.SCRIPT_ELEMENT);
		if ( tblElemRules.getItem(ELEMENTS_SERVER).getChecked() ) currentElem.rules.add(RULE_TYPE.SERVER_ELEMENT);
		currentElem.subFilter = edElemFilterConfig.getText().trim();
		// Conditions are saved when editing
		return true;
	}
	
	private boolean saveAttribute () {
		if ( currentAtt == null ) return true;
		currentAtt.rules.clear();
		if ( tblAttRules.getItem(ATTRULES_TRANS).getChecked() ) currentAtt.rules.add(RULE_TYPE.ATTRIBUTE_TRANS);
		if ( tblAttRules.getItem(ATTRULES_WRITABLE).getChecked() ) currentAtt.rules.add(RULE_TYPE.ATTRIBUTE_WRITABLE);
		if ( tblAttRules.getItem(ATTRULES_READONLY).getChecked() ) currentAtt.rules.add(RULE_TYPE.ATTRIBUTE_READONLY);
		if ( tblAttRules.getItem(ATTRULES_ID).getChecked() ) currentAtt.rules.add(RULE_TYPE.ATTRIBUTE_ID);
		if ( tblAttRules.getItem(ATTRULES_PRESERVE_WHITESPACE).getChecked() ) currentAtt.rules.add(RULE_TYPE.ATTRIBUTE_PRESERVE_WHITESPACE);
		// Scope
		if ( rdAttExceptThese.getSelection() ) currentAtt.scope = Attribute.SCOPE_ALLEXCEPT;
		else if ( rdAttOnlyThese.getSelection() ) currentAtt.scope = Attribute.SCOPE_ONLY;
		else currentAtt.scope = Attribute.SCOPE_ALL;
		currentAtt.scopeElements = edAttScopeElements.getText().trim();
		// Conditions are saved when editing
		return true;
	}
	
	private boolean updateElement () {
		if ( !saveElement() ) {
			return false;
		}
		int n = lbElem.getSelectionIndex();
		if ( n < 0 ) {
			for ( TableItem item : tblElemRules.getItems() ) {
				item.setChecked(false);
			}
			edElemConditions.setText("");
			edElemFilterConfig.setText("");
			currentElem = null;
		}
		else {
			Element elem = (Element)lbElem.getData(lbElem.getItem(n));
			tblElemRules.getItem(ELEMENTS_TEXTUNIT).setChecked(elem.rules.contains(RULE_TYPE.TEXT_UNIT_ELEMENT));
			tblElemRules.getItem(ELEMENTS_EXCLUDED).setChecked(elem.rules.contains(RULE_TYPE.EXCLUDED_ELEMENT));
			tblElemRules.getItem(ELEMENTS_INCLUDED).setChecked(elem.rules.contains(RULE_TYPE.INCLUDED_ELEMENT));
			tblElemRules.getItem(ELEMENTS_INLINE).setChecked(elem.rules.contains(RULE_TYPE.INLINE_ELEMENT));
			tblElemRules.getItem(ELEMENTS_ATTRIBUTESONLY).setChecked(elem.rules.contains(RULE_TYPE.ATTRIBUTES_ONLY));
			tblElemRules.getItem(ELEMENTS_GROUP).setChecked(elem.rules.contains(RULE_TYPE.GROUP_ELEMENT));
			tblElemRules.getItem(ELEMENTS_SCRIPT).setChecked(elem.rules.contains(RULE_TYPE.SCRIPT_ELEMENT));
			tblElemRules.getItem(ELEMENTS_SERVER).setChecked(elem.rules.contains(RULE_TYPE.SERVER_ELEMENT));
			edElemConditions.setText(formatConditions(elem.conditions));
			edElemFilterConfig.setText(elem.subFilter);
			currentElem = elem;
		}
		return true;
	}
	
	private boolean updateAttribute () {
		if ( !saveAttribute() ) {
			return false;
		}
		int n = lbAtt.getSelectionIndex();
		if ( n < 0 ) {
			for ( TableItem item : tblAttRules.getItems() ) {
				item.setChecked(false);
			}
			rdAttrAllElements.setSelection(true);
			rdAttExceptThese.setSelection(false);
			rdAttOnlyThese.setSelection(false);
			edAttScopeElements.setText("");
			edAttConditions.setText("");
			edPreserveWS.setText("");
			edDefaultWS.setText("");
			currentAtt = null;
		}
		else {
			Attribute att = (Attribute)lbAtt.getData(lbAtt.getItem(n));
			tblAttRules.getItem(ATTRULES_TRANS).setChecked(att.rules.contains(RULE_TYPE.ATTRIBUTE_TRANS));
			tblAttRules.getItem(ATTRULES_WRITABLE).setChecked(att.rules.contains(RULE_TYPE.ATTRIBUTE_WRITABLE));
			tblAttRules.getItem(ATTRULES_READONLY).setChecked(att.rules.contains(RULE_TYPE.ATTRIBUTE_READONLY));
			tblAttRules.getItem(ATTRULES_ID).setChecked(att.rules.contains(RULE_TYPE.ATTRIBUTE_ID));
			tblAttRules.getItem(ATTRULES_PRESERVE_WHITESPACE).setChecked(att.rules.contains(RULE_TYPE.ATTRIBUTE_PRESERVE_WHITESPACE));
			rdAttrAllElements.setSelection(att.scope==Attribute.SCOPE_ALL);
			rdAttExceptThese.setSelection(att.scope==Attribute.SCOPE_ALLEXCEPT);
			rdAttOnlyThese.setSelection(att.scope==Attribute.SCOPE_ONLY);
			edAttScopeElements.setText(att.scopeElements);
			edAttConditions.setText(formatConditions(att.conditions));
			edPreserveWS.setText(formatConditions(att.wsPreserve));
			edDefaultWS.setText(formatConditions(att.wsDefault));
			updateWhiteSpaces();
			currentAtt = att;
		}
		updateScopeElements();
		updateWhiteSpaces();
		return true;
	}
	
	// Formats a list of conditions for display
	private String formatConditions (java.util.List<Condition> list) {
		if ( Util.isEmpty(list) ) {
			return "";
		}
		if ( list.size() == 1 ) {
			return list.get(0).toString();
		}
		// Else:
		return list.toString();
	}

	private boolean showDialog () {
		shell.open();
		while ( !shell.isDisposed() ) {
			if ( !shell.getDisplay().readAndDispatch() )
				shell.getDisplay().sleep();
		}
		return result;
	}
	
	private boolean validate () {
		saveAttribute();
		saveElement();
		
		// Validate the elements
		for ( int i=0; i<lbElem.getItemCount(); i++ ) {
			String name = lbElem.getItem(i);
			if ( !name.equals(ensureValidName(name)) ) {
				Dialogs.showError(shell,
					String.format("The element name \"%s\" is invalid.", lbElem.getItem(i)), null);
				tabs.setSelection(TAB_ELEMENTS);
				lbElem.setSelection(i);
				updateElement();
				return false;
			}
			Element elem = (Element)lbElem.getData(name);
			// We need at least one rule
			if ( elem.rules.isEmpty() ) {
				Dialogs.showError(shell,
					String.format("The element \"%s\" has no rule defined.", name), null);
				tabs.setSelection(TAB_ELEMENTS);
				lbElem.setSelection(i);
				updateElement();
				return false;
			}
		}
		
		// Validate the attributes
		for ( int i=0; i<lbAtt.getItemCount(); i++ ) {
			String name = lbAtt.getItem(i);
			if ( !name.equals(ensureValidName(name)) ) {
				Dialogs.showError(shell,
					String.format("The attribute name \"%s\" is invalid.", lbAtt.getItem(i)), null);
				tabs.setSelection(TAB_ATTRIBUTES);
				lbAtt.setSelection(i);
				updateAttribute();
				return false;
			}
			Attribute att = (Attribute)lbAtt.getData(name);
			// We need at least one rule
			if ( att.rules.isEmpty() ) {
				Dialogs.showError(shell,
					String.format("The attribute \"%s\" has no rule defined.", name), null);
				tabs.setSelection(TAB_ATTRIBUTES);
				lbAtt.setSelection(i);
				updateAttribute();
				return false;
			}
			// Check the scope
			if (( att.scope != Attribute.SCOPE_ALL ) && att.scopeElements.isEmpty() ) {
				Dialogs.showError(shell,
					String.format("The attribute \"%s\" has no elements defined for its scope.", name), null);
				tabs.setSelection(TAB_ATTRIBUTES);
				lbAtt.setSelection(i);
				updateAttribute();
				return false;
			}
		}
		
		// check inline codes
		String tmp = pnlCodeFinder.getRules();
		if ( tmp == null ) return false;

		return true;
	}
	
	private void setData () {
		try {
			TaggedFilterConfiguration tfg = params.getTaggedConfig();

			chkWellformed.setSelection(tfg.isWellformed());
			chkGlobalPreserveWS.setSelection(tfg.isGlobalPreserveWhitespace());
			
			chkUseCodeFinder.setSelection(tfg.isUseCodeFinder());
			pnlCodeFinder.setRules(tfg.getCodeFinderRules());
			
			String sf = tfg.getGlobalCDATASubfilter();
			edCDATAFilterConfig.setText(sf==null ? "" : sf);
			
			//--- Read elements

			Map<String, Object> map = tfg.getElementRules();
			for ( String name : map.keySet() ) {
				Element elem = new Element();
				elem.name = name.toLowerCase();
				@SuppressWarnings("unchecked")
				Map<String, Object> items = (Map<String, Object>)map.get(name);
				for ( String itemName : items.keySet() ) {
					if ( itemName.equals(TaggedFilterConfiguration.RULETYPES) ) {
						@SuppressWarnings("unchecked")
						java.util.List<String> list = (java.util.List<String>)items.get(itemName);
						for ( String tmp : list ) {
							elem.rules.add(tfg.convertRuleAsStringToRuleType(tmp));
						}
					}
					else if ( itemName.equals(TaggedFilterConfiguration.CONDITIONS) ) {
						elem.conditions = parseConditions(items.get(itemName));
					}
					else if ( itemName.equals(TaggedFilterConfiguration.SUBFILTER) ) {
						elem.subFilter = (String)items.get(itemName);
					}
					
				}				
				// Attribute is read, add it
				lbElem.add(elem.name);
				lbElem.setData(elem.name, elem);
			}
			// Select default and update all
			if ( lbElem.getItemCount() > 0 ) lbElem.setSelection(0);
			updateElement();
			//updateElementsButtons();
			
			//--- Read the attributes
			
			map = tfg.getAttributeRules();
			for ( String attName : map.keySet() ) {
				Attribute att = new Attribute();
				att.name = attName.toLowerCase();
				@SuppressWarnings("unchecked")
				Map<String, Object> items = (Map<String, Object>)map.get(attName);
				for ( String itemName : items.keySet() ) {
					// Get the list of ruleTypes
					if ( itemName.equals(TaggedFilterConfiguration.RULETYPES) ) {
						@SuppressWarnings("unchecked")
						java.util.List<String> list = (java.util.List<String>)items.get(itemName);
						for ( String tmp : list ) {
							att.rules.add(tfg.convertRuleAsStringToRuleType(tmp));
						}
					}
					else if ( itemName.equals(TaggedFilterConfiguration.ALL_ELEMENTS_EXCEPT) ) {
						att.scope = Attribute.SCOPE_ALLEXCEPT;
						att.scopeElements = makeStringList(items.get(itemName).toString());
					}
					else if ( itemName.equals(TaggedFilterConfiguration.ONLY_THESE_ELEMENTS) ) {
						att.scope = Attribute.SCOPE_ONLY;
						att.scopeElements = makeStringList(items.get(itemName).toString());
					}
					else if ( itemName.equals(TaggedFilterConfiguration.PRESERVE_CONDITION) ) {
						att.wsPreserve = parseConditions(items.get(itemName));
					}
					else if ( itemName.equals(TaggedFilterConfiguration.DEFAULT_CONDITION) ) {
						att.wsDefault = parseConditions(items.get(itemName));
					}
					else if ( itemName.equals(TaggedFilterConfiguration.CONDITIONS) ) {
						att.conditions = parseConditions(items.get(itemName));
					}
				}
				// Attribute is read, add it
				lbAtt.add(att.name);
				lbAtt.setData(att.name, att);
			}
			// Select default and update all
			if ( lbAtt.getItemCount() > 0 ) lbAtt.setSelection(0);
			updateAttribute();
			updateAttributesButtons();
		}
		catch ( Throwable e ) {
			Dialogs.showError(shell, "Error when loading the configuration.\n"+e.getMessage(), null);
		}
	}
	
	private java.util.List<Condition> parseConditions (Object rawObject) {
		java.util.List<Condition> list = new ArrayList<Condition>(1);
		// The conditions entry is either a single condition statement or a list of conditions
		@SuppressWarnings("unchecked")
		java.util.List<Object> objs = (java.util.List<Object>)rawObject;
		if ( objs.get(0) instanceof String ) { // This is a condition statement
			parseCondition(list, objs);
		}
		else { // Otherwise it has to be a list of conditions
			for ( Object obj : objs ) {
				parseCondition(list, obj);
			}
		}
		return list;
	}
	
	private void parseCondition (java.util.List<Condition> conditions,
		Object rawObject)
	{
		// We should have three objects: name, operator, value(s)
		@SuppressWarnings("unchecked")
		java.util.List<Object> objs = (java.util.List<Object>)rawObject;
		Condition condition = new Condition();
		condition.part1 = (String)objs.get(0);
		condition.operator = (String)objs.get(1);
		// The value(s) can be one string or a list of strings
		if ( objs.get(2) instanceof String ) {
			condition.part2 = (String)objs.get(2);
		}
		else { // List of values
			condition.part2 = makeStringList(objs.get(2).toString());
		}
		// Add the condition
		conditions.add(condition);
	}

	// Converts a YAML representation of a list, or a string into a simple list
	private String makeStringList (String yamlList) {
		String res = yamlList.trim();
		// If it's a list: remove the brackets
		if (( res.length() > 2 ) && ( res.charAt(0) == '[' )) {
			res = res.substring(1, res.length()-1);
		}
		return res.trim();
	}
	
	private String ensureValidName (String name) {
		name = name.toLowerCase().trim();
		name = name.replaceAll("\\s", "");
		return name;
	}
	
	private boolean saveData () {
		if ( !validate() ) return false;
		
		StringBuilder tmp = new StringBuilder();
		
		//--- General
		tmp.append(String.format("%s: %s\n",
			TaggedFilterConfiguration.WELLFORMED,
			chkWellformed.getSelection()));
		tmp.append(String.format("%s: %s\n",
			TaggedFilterConfiguration.GLOBAL_PRESERVE_WHITESPACE,
			chkGlobalPreserveWS.getSelection()));
		tmp.append(String.format("%s: %s\n",
			TaggedFilterConfiguration.GLOBAL_CDATA_SUBFILTER,
			edCDATAFilterConfig.getText()));
		
		//--- Attribute
		tmp.append("\nattributes:\n");
		for ( int i=0; i<lbAtt.getItemCount(); i++ ) {
			Attribute att = (Attribute)lbAtt.getData(lbAtt.getItem(i));
			tmp.append("  '"+att.name+"':\n    ruleTypes: ");
			tmp.append(att.rules.toString());
			// White-spaces
			if ( att.rules.contains(RULE_TYPE.ATTRIBUTE_PRESERVE_WHITESPACE) ) {
				if ( !Util.isEmpty(att.wsPreserve) ) {
					tmp.append(String.format("\n    %s: ", TaggedFilterConfiguration.PRESERVE_CONDITION));
					tmp.append(att.wsPreserve.toString());
				}
				if ( !Util.isEmpty(att.wsDefault) ) {
					tmp.append(String.format("\n    %s: ", TaggedFilterConfiguration.DEFAULT_CONDITION));
					tmp.append(att.wsDefault.toString());
				}
			}
			// Conditions
			if ( !Util.isEmpty(att.conditions) ) {
				tmp.append(String.format("\n    %s: ", TaggedFilterConfiguration.CONDITIONS));
				tmp.append(att.conditions.toString());
			}
			tmp.append("\n");
		}
		
		//-- Elements
		tmp.append("\nelements:\n");
		for ( int i=0; i<lbElem.getItemCount(); i++ ) {
			Element elem = (Element)lbElem.getData(lbElem.getItem(i));
			tmp.append("  '"+elem.name+"':\n    ruleTypes: ");
			tmp.append(elem.rules.toString());
			// Conditions
			if ( !Util.isEmpty(elem.conditions) ) {
				tmp.append(String.format("\n    %s: ", TaggedFilterConfiguration.CONDITIONS));
				tmp.append(elem.conditions.toString());
			}
			if ( !Util.isEmpty(elem.subFilter) ) {
				tmp.append(String.format("\n    %s: '%s'",
					TaggedFilterConfiguration.SUBFILTER, elem.subFilter));
			}
			tmp.append("\n");
		}		
		
		//--- Inline codes
		tmp.append(String.format("\n%s: %s\n",
			TaggedFilterConfiguration.USECODEFINDER,
			chkUseCodeFinder.getSelection()));
		String rules = pnlCodeFinder.getRules().replace("\\", "\\\\");
		rules = rules.replace("\n", "\\\\n");
		tmp.append(String.format("%s: %s\n",
			TaggedFilterConfiguration.CODEFINDERRULES,
			"\""+rules+"\""));
		
		params.fromString(tmp.toString());
		params.save(params.getPath()+".outtest.txt");

		return true;
	}

	private void editConditions (Text ctrlDisplay,
		java.util.List<Condition> conditions)
	{
		try {
			ConditionsDialog dlg = new ConditionsDialog(shell, null, conditions);
			if ( !dlg.showDialog() ) return;
			// Else: update the display
			ctrlDisplay.setText(formatConditions(conditions));
		}
		catch ( Throwable e ) {
			Dialogs.showError(shell, "Error when editing conditions.\n"+e.getMessage(), null);
		}
	}

}
