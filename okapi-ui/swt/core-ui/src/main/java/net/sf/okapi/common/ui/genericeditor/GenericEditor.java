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

package net.sf.okapi.common.ui.genericeditor;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import net.sf.okapi.common.IContext;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.ParameterDescriptor;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.exceptions.OkapiEditorCreationException;
import net.sf.okapi.common.ui.Dialogs;
import net.sf.okapi.common.ui.OKCancelPanel;
import net.sf.okapi.common.ui.TextAndBrowsePanel;
import net.sf.okapi.common.ui.UIUtil;
import net.sf.okapi.common.ui.filters.InlineCodeFinderPanel;
import net.sf.okapi.common.uidescription.AbstractPart;
import net.sf.okapi.common.uidescription.CheckListPart;
import net.sf.okapi.common.uidescription.CheckboxPart;
import net.sf.okapi.common.uidescription.CodeFinderPart;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.FolderInputPart;
import net.sf.okapi.common.uidescription.IContainerPart;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;
import net.sf.okapi.common.uidescription.ListSelectionPart;
import net.sf.okapi.common.uidescription.PathInputPart;
import net.sf.okapi.common.uidescription.SeparatorPart;
import net.sf.okapi.common.uidescription.SpinInputPart;
import net.sf.okapi.common.uidescription.TextInputPart;
import net.sf.okapi.common.uidescription.TextLabelPart;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class GenericEditor {

	protected Shell shell;
	protected IParameters params;
	protected Composite mainComposite;

	private boolean result = false;
	private EditorDescription description;
	private Hashtable<String, Control> controls;
	private Hashtable<Control, MasterItem> masters;
	private boolean hasLongField;
	
	/**
	 * Internal class to store master control and its slaves.
	 */
	private class MasterItem {
		
		public ArrayList<AbstractPart> slaves;
		
		public MasterItem () {
			slaves = new ArrayList<AbstractPart>();
		}
		
		public void addSlave (AbstractPart part) {
			slaves.add(part);
		}
	}
	
	/**
	 * Internal class to handle the master/slaves enabling/disabling.
	 */
	private class CtrlSelectionListener implements Listener {
		
		private Button masterCtrl;

		public CtrlSelectionListener (Control masterCtrl) {
			this.masterCtrl = (Button)masterCtrl;
		}
		
		public void handleEvent (Event event) {
			// Propagate to the slaves
			propagate(masterCtrl, masterCtrl.getSelection(), masterCtrl.getEnabled());
		}
		
		private void propagate (Control ctrl,
			boolean isCallerSelected,
			boolean isCallerEnabled)
		{
			Button button = (Button)ctrl;
			MasterItem mi = masters.get(button);
			for ( AbstractPart part : mi.slaves ) {
				Control slaveCtrl = controls.get(part.getName());
				boolean slaveEnabled = false;
				// Enabled/disable the slave
				if ( !isCallerEnabled ) { // If master is disabled: slave is always disabled
					slaveCtrl.setEnabled(false);
				}
				else {
					// Else: slave is enabled/disabled based on the master's selection
					// and the slave's isEnabledOnSelection() option
					if ( isCallerSelected ) {
						slaveEnabled = part.isEnabledOnSelection();
					}
					else {
						slaveEnabled = !part.isEnabledOnSelection();
					}
					slaveCtrl.setEnabled(slaveEnabled);
				}
				// Look if the slave is also a master
				if ( masters.containsKey(slaveCtrl) ) {
					// If it is: propagate the info down the sub-slaves
					// If the slave is disabled; always disable the sub-slaves
					// Otherwise, masters are always Button-based so get the selection from there
					propagate(slaveCtrl, ((Button)slaveCtrl).getSelection(), slaveEnabled);
				}
			}
		}
	}

	public boolean edit (IParameters paramsObject,
		IEditorDescriptionProvider descProvider,
		boolean readOnly,
		IContext context)
	{
		boolean bRes = false;
		shell = null;
		try {
			params = paramsObject; 
			shell = new Shell((Shell)context.getObject("shell"), SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.APPLICATION_MODAL);
			create((Shell)context.getObject("shell"), descProvider, readOnly);
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
	
	protected void createComposite (Composite parent,
		IEditorDescriptionProvider descProv)
	{
		// Get the UI description
		ParametersDescription pd = params.getParametersDescription();
		if ( pd == null ) {
			throw new OkapiEditorCreationException(
				"This configuration cannot be edited with the generic editor because it does not provide a description of its parameters.");
		}
		description = descProv.createEditorDescription(pd);
		if ( description == null ) {
			throw new OkapiEditorCreationException(
				"This configuration cannot be edited with the generic editor because the UI description could not be created.");
		}
	
		controls = new Hashtable<String, Control>();
		masters = new Hashtable<Control, MasterItem>();

		mainComposite = new Composite(parent, SWT.BORDER);
		mainComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layTmp = new GridLayout(2, false);
		mainComposite.setLayout(layTmp);

		// Create the UI parts
		hasLongField = false;
		Composite cmp;
		GridData gdTmp;
		
		for ( AbstractPart part : description.getDescriptors().values() ) {
			// Create the control for the given part
			if ( part instanceof TextInputPart ) {
				TextInputPart d = (TextInputPart)part;
				cmp = lookupParent(d.getContainer());
				if ( d.isWithLabel() ) setLabel(cmp, d, 0);
				Text text;
				if ( d.getHeight() > -1 ) {
					text = new Text(cmp, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
					gdTmp = new GridData(GridData.FILL_BOTH);
					gdTmp.heightHint = d.getHeight();
				}
				else {
					text = new Text(cmp, SWT.BORDER);
					gdTmp = new GridData(GridData.FILL_HORIZONTAL);
				}
				controls.put(d.getName(), text);
				if ( part.isVertical() || !part.isWithLabel() ) gdTmp.horizontalSpan = 2;
				text.setLayoutData(gdTmp);
				text.setEditable(d.getWriteMethod()!=null);
				if ( d.isPassword() ) text.setEchoChar('*');
			}
			else if ( part instanceof CheckboxPart ) {
				CheckboxPart d = (CheckboxPart)part;
				cmp = lookupParent(d.getContainer());
				if ( !part.isVertical() ) new Label(cmp, SWT.NONE);
				Button button = new Button(cmp, SWT.CHECK);
				button.setToolTipText(d.getShortDescription());
				controls.put(d.getName(), button);
				if ( part.isVertical() || !part.isWithLabel() ) {
					gdTmp = new GridData();
					gdTmp.horizontalSpan = 2;
					button.setLayoutData(gdTmp);
				}
				button.setText(d.getDisplayName());
				button.setEnabled(d.getWriteMethod()!=null);
			}
			else if ( part instanceof PathInputPart ) {
				PathInputPart d = (PathInputPart)part;
				cmp = lookupParent(d.getContainer());
				if ( d.isWithLabel() ) setLabel(cmp, d, 0);
				TextAndBrowsePanel ctrl = new TextAndBrowsePanel(cmp, SWT.NONE, false);
				ctrl.setSaveAs(d.isForSaveAs());
				ctrl.setTitle(d.getBrowseTitle());
				ctrl.setBrowseFilters(d.getFilterNames(), d.getFilterExtensions());
				gdTmp = new GridData(GridData.FILL_HORIZONTAL);
				if (( part.isVertical() && !part.isLabelNextToInput() ) || !part.isWithLabel() ) {
					gdTmp.horizontalSpan = 2;
				}
				ctrl.setLayoutData(gdTmp);
				controls.put(d.getName(), ctrl);
				ctrl.setEditable(d.getWriteMethod()!=null);
				hasLongField = true;
			}
			else if ( part instanceof FolderInputPart ) {
				FolderInputPart d = (FolderInputPart)part;
				cmp = lookupParent(d.getContainer());
				if ( d.isWithLabel() ) setLabel(cmp, d, 0);
				TextAndBrowsePanel ctrl = new TextAndBrowsePanel(cmp, SWT.NONE, true);
				ctrl.setTitle(d.getBrowseTitle());
				gdTmp = new GridData(GridData.FILL_HORIZONTAL);
				if (( part.isVertical() && !part.isLabelNextToInput() ) || !part.isWithLabel() ) {
					gdTmp.horizontalSpan = 2;
				}
				ctrl.setLayoutData(gdTmp);
				controls.put(d.getName(), ctrl);
				ctrl.setEditable(d.getWriteMethod()!=null);
				hasLongField = true;
			}
			else if ( part instanceof ListSelectionPart ) {
				ListSelectionPart d = (ListSelectionPart)part;
				cmp = lookupParent(d.getContainer());
				if ( d.getListType() == ListSelectionPart.LISTTYPE_DROPDOWN ) {
					if ( d.isWithLabel() ) setLabel(cmp, d, 0);
					Combo combo = new Combo(cmp, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
					controls.put(d.getName(), combo);
					gdTmp = new GridData(GridData.FILL_HORIZONTAL);
					if ( part.isVertical() || !part.isWithLabel() ) gdTmp.horizontalSpan = 2;
					combo.setLayoutData(gdTmp);
					combo.setEnabled(d.getWriteMethod()!=null);
				}
				else {
					if ( d.isWithLabel() ) setLabel(cmp, d, GridData.VERTICAL_ALIGN_BEGINNING);
					List list = new List(cmp, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
					controls.put(d.getName(), list);
					gdTmp = new GridData(GridData.FILL_BOTH);
					if ( part.isVertical() || !part.isWithLabel() ) gdTmp.horizontalSpan = 2;
					list.setLayoutData(gdTmp);
					list.setEnabled(d.getWriteMethod()!=null);
				}
			}
			else if ( part instanceof CheckListPart ) {
				CheckListPart d = (CheckListPart)part;
				cmp = lookupParent(d.getContainer());
				if ( d.isWithLabel() ) setLabel(cmp, d, GridData.VERTICAL_ALIGN_BEGINNING);
				Table table = new Table (cmp, SWT.CHECK | SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
				controls.put(d.getName(), table);
				gdTmp = new GridData(GridData.FILL, GridData.FILL, true, true);
				gdTmp.heightHint = d.getHeightHint();
				if ( part.isVertical() || !part.isWithLabel() ) gdTmp.horizontalSpan = 2;
				table.setLayoutData(gdTmp);
			}
			else if ( part instanceof CodeFinderPart ) {
				CodeFinderPart d = (CodeFinderPart)part;
				cmp = lookupParent(d.getContainer());
				if ( !part.isVertical() ) new Label(cmp, SWT.NONE);
				InlineCodeFinderPanel panel = new InlineCodeFinderPanel(cmp, SWT.NONE);
				controls.put(d.getName(), panel);
				if ( part.isVertical() || !part.isWithLabel() ) {
					gdTmp = new GridData();
					gdTmp.horizontalSpan = 2;
					panel.setLayoutData(gdTmp);
				}
				panel.setEnabled(d.getWriteMethod()!=null);
			}
			else if ( part instanceof SpinInputPart ) {
				SpinInputPart d = (SpinInputPart)part;
				cmp = lookupParent(d.getContainer());
				if ( d.isWithLabel() ) setLabel(cmp, d, 0);
				Spinner spinner = new Spinner(cmp, SWT.BORDER);
				spinner.setMinimum(d.getMinimumValue());
				spinner.setMaximum(d.getMaximumValue());
				spinner.setIncrement(1);
				spinner.setPageIncrement(10);
				controls.put(d.getName(), spinner);
				if ( part.isVertical() || !part.isWithLabel() ) {
					gdTmp = new GridData();
					gdTmp.horizontalSpan = 2;
					spinner.setLayoutData(gdTmp);
				}
				spinner.setEnabled(d.getWriteMethod()!=null);
			}
			else if ( part instanceof SeparatorPart ) {
				SeparatorPart d = (SeparatorPart)part;
				cmp = lookupParent(d.getContainer());
				Label separator = new Label(cmp, SWT.BORDER);
				controls.put(d.getName(), separator);
				gdTmp = new GridData(GridData.FILL_HORIZONTAL);
				gdTmp.heightHint = 1;
				if ( part.isVertical() || !part.isWithLabel() ) {
					gdTmp.horizontalSpan = 2;
				}
				separator.setLayoutData(gdTmp);
			}
			else if ( part instanceof TextLabelPart ) {
				TextLabelPart d = (TextLabelPart)part;
				cmp = lookupParent(d.getContainer());
				Label label = new Label(cmp, SWT.NONE);
				label.setText(d.getDisplayName());
				if ( !Util.isEmpty(d.getShortDescription()) ) {
					label.setToolTipText(d.getShortDescription());
				}
				controls.put(d.getName(), label);
				gdTmp = new GridData(GridData.FILL_HORIZONTAL);
				if ( part.isVertical() || !part.isWithLabel() ) {
					gdTmp.horizontalSpan = 2;
				}
				label.setLayoutData(gdTmp);
			}

			// Update the list of observers if needed
			if ( part.getMasterPart() != null ) {
				addObserver(part);
			}
		}
		
	}
	
	private void create (Shell parent,
		IEditorDescriptionProvider descProv,
		boolean readOnly)
	{
		if ( parent != null ) UIUtil.inheritIcon(shell, parent);
		GridLayout layTmp = new GridLayout(1, false);
		layTmp.marginBottom = 0;
		layTmp.verticalSpacing = 0;
		shell.setLayout(layTmp);

		createComposite(shell, descProv);
		
		// Set caption is it is provided
		if ( description.getCaption() != null ) {
			shell.setText(description.getCaption());
		}
		else { // Default caption
			shell.setText("Parameters");
		}

		//--- Dialog-level buttons

		SelectionAdapter okCancelActions = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if ( e.widget.getData().equals("h") ) { //$NON-NLS-1$
					//if ( help != null ) help.showTopic(this, "index");
					return;
				}
				if ( e.widget.getData().equals("o") ) { //$NON-NLS-1$
					if ( !saveData() ) return;
					result = true;
				}
				shell.close();
			};
		};
		OKCancelPanel pnlActions = new OKCancelPanel(shell, SWT.NONE, okCancelActions, false);
		GridData gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		pnlActions.setLayoutData(gdTmp);
		pnlActions.btOK.setEnabled(!readOnly);
		if ( !readOnly ) {
			shell.setDefaultButton(pnlActions.btOK);
		}

		shell.pack();
		shell.setMinimumSize(shell.getSize());
		if ( hasLongField ) {
			Point startSize = shell.getMinimumSize();
			if ( startSize.x < 600 ) startSize.x = 600; 
			shell.setSize(startSize);
		}
		Dialogs.centerWindow(shell, parent);
		setData();
	}
		
	private void addObserver (AbstractPart part) {
		// Add a listener to the master control
		AbstractPart masterPart = part.getMasterPart();
		Control masterCtrl = controls.get(masterPart.getName());
		if ( !(masterCtrl instanceof Button) ) {
			throw new OkapiEditorCreationException(String.format(
				"The master UI part for the part '%s' cannot be used as a toggle switch.", part.getName()));
		}
		masterCtrl.addListener(SWT.Selection, new CtrlSelectionListener(masterCtrl));
		
		// Add the master control to the list of masters
		// This will be used later to cascade setEnabled()
		MasterItem mi = masters.get(masterCtrl);
		if ( mi == null ) {
			mi = new MasterItem();
			masters.put(masterCtrl, mi);
		}
		// Update the list of slaves for the given master
		mi.addSlave(part);
	}

	private void setLabel (Composite parent,
		AbstractPart part,
		int flag)
	{
		Label label = new Label(parent, SWT.NONE);
		String tmp = part.getDisplayName();
		if ( tmp != null ) {
			if ( !tmp.endsWith(":") ) tmp += ":";
			label.setText(tmp);
		}
		label.setToolTipText(part.getShortDescription());
		if ( part.isLabelFlushed() ) flag |= GridData.HORIZONTAL_ALIGN_END;
		GridData gdTmp = new GridData(flag);
		if ( part.isVertical() && !part.isLabelNextToInput() ) {
			gdTmp.horizontalSpan = 2; 
		}
		label.setLayoutData(gdTmp);
	}
	
	protected void setData () {
		// Create list to enumerate all bound parts
		ArrayList<AbstractPart> list = new ArrayList<AbstractPart>();
		
		for ( AbstractPart part : description.getDescriptors().values() ) {
			if ( part instanceof TextInputPart ) {
				TextInputPart d = (TextInputPart)part;
				setInputControl((Text)controls.get(d.getName()), d);
			}
			else if ( part instanceof CheckboxPart ) {
				CheckboxPart d = (CheckboxPart)part;
				setCheckboxControl((Button)controls.get(d.getName()), d);
			}
			else if ( part instanceof PathInputPart ) {
				PathInputPart d = (PathInputPart)part;
				setPathControl((TextAndBrowsePanel)controls.get(d.getName()), d);
			}
			else if ( part instanceof FolderInputPart ) {
				FolderInputPart d = (FolderInputPart)part;
				setFolderControl((TextAndBrowsePanel)controls.get(d.getName()), d);
			}
			else if ( part instanceof ListSelectionPart ) {
				ListSelectionPart d = (ListSelectionPart)part;
				if ( d.getListType() == ListSelectionPart.LISTTYPE_DROPDOWN ) {
					setComboControl((Combo)controls.get(d.getName()), d);
				}
				else {
					setListControl((List)controls.get(d.getName()), d);
				}
			}
			else if ( part instanceof CheckListPart ) {
				CheckListPart d = (CheckListPart)part;
				setCheckListControl((Table)controls.get(d.getName()), d);
			}
			else if ( part instanceof CodeFinderPart ) {
				CodeFinderPart d = (CodeFinderPart)part;
				setCodeFinderControl((InlineCodeFinderPanel)controls.get(d.getName()), d);
			}
			else if ( part instanceof SpinInputPart ) {
				SpinInputPart d = (SpinInputPart)part;
				setSpinnerControl((Spinner)controls.get(d.getName()), d);
			}
			
			if ( part.getMasterPart() != null ) list.add(part);
		}

		for ( int i=list.size()-1; i>=0; i-- ) {
			AbstractPart slavePart = list.get(i);
			AbstractPart masterPart = slavePart.getMasterPart();
			Button masterCtrl = (Button)controls.get(masterPart.getName());
			Control slaveCtrl = controls.get(list.get(i).getName());
			if ( masterCtrl.isEnabled() ) {
				if ( slavePart.isEnabledOnSelection() ) slaveCtrl.setEnabled(masterCtrl.getSelection());
				else slaveCtrl.setEnabled(!masterCtrl.getSelection());
			}
			
		}
	}

	/**
	 * Saves the current data to the parameters object.
	 * @return true if there was no error, false if there was an error.
	 */
	protected boolean saveData () {
		Control ctrl;
		for ( String name : controls.keySet() ) {
			ctrl = controls.get(name);
			if ( ctrl instanceof Text ) {
				if ( !saveInputControl((Text)ctrl, (TextInputPart)description.getDescriptor(name)) ) {
					return false;
				}
			}
			else if ( ctrl instanceof Button ) {
				if (( ctrl.getStyle() & SWT.CHECK) == SWT.CHECK ) {
					if ( !saveCheckboxControl((Button)ctrl, (CheckboxPart)description.getDescriptor(name)) ) {
						return false;
					}
				}
			}
			else if ( ctrl instanceof TextAndBrowsePanel ) {
				if ( description.getDescriptor(name) instanceof PathInputPart ) {
					if ( !saveTextAndBrowseControl((TextAndBrowsePanel)ctrl, (PathInputPart)description.getDescriptor(name)) ) {
						return false;
					}
				}
				else if ( description.getDescriptor(name) instanceof FolderInputPart ) {
					if ( !saveFolderControl((TextAndBrowsePanel)ctrl, (FolderInputPart)description.getDescriptor(name)) ) {
						return false;
					}
				}
			}
			else if ( ctrl instanceof List ) {
				if ( description.getDescriptor(name) instanceof ListSelectionPart ) {
					if ( !saveListControl((List)ctrl, (ListSelectionPart)description.getDescriptor(name)) ) {
						return false;
					}
				}
			}
			else if ( ctrl instanceof Table ) {
				if ( description.getDescriptor(name) instanceof CheckListPart ) {
					if ( !saveCheckListControl((Table)ctrl, (CheckListPart)description.getDescriptor(name)) ) {
						return false;
					}
				}
			}
			else if ( ctrl instanceof Combo ) {
				if ( description.getDescriptor(name) instanceof ListSelectionPart ) {
					if ( !saveComboControl((Combo)ctrl, (ListSelectionPart)description.getDescriptor(name)) ) {
						return false;
					}
				}
			}
			else if ( ctrl instanceof InlineCodeFinderPanel ) {
				if ( description.getDescriptor(name) instanceof CodeFinderPart ) {
					if ( !saveCodeFinderControl((InlineCodeFinderPanel)ctrl, (CodeFinderPart)description.getDescriptor(name)) ) {
						return false;
					}
				}
			}
			else if ( ctrl instanceof Spinner ) {
				if ( description.getDescriptor(name) instanceof SpinInputPart ) {
					if ( !saveSpinnerControl((Spinner)ctrl, (SpinInputPart)description.getDescriptor(name)) ) {
						return false;
					}
				}
			}
		}
		return true;
	}
	
	private boolean saveInputControl (Text text,
		TextInputPart desc)
	{
		try {
			if ( !text.isEnabled() ) return true; // Don't save disabled input
			if ( !desc.isAllowEmpty() ) {
				if ( text.getText().length() == 0 ) {
					Dialogs.showError(shell,
						String.format("Empty entry not allowed for '%s'.", desc.getDisplayName()), null);
					text.setFocus();
					return false;
				}
			}
			if ( desc.getType().equals(String.class) ) {
				desc.getWriteMethod().invoke(desc.getParent(), text.getText());
			}
			else if ( desc.getType().equals(int.class) ) {
				try {
					int n = 0;
					if ( text.getText().length() > 0 ) { 
						n = Integer.valueOf(text.getText());
					}
					if (( n < desc.getMinimumValue() ) || ( n > desc.getMaximumValue() )) {
						Dialogs.showError(shell,
							String.format("The value must be between %d and %d (both included) for '%s'.",
								desc.getMinimumValue(), desc.getMaximumValue(), desc.getDisplayName()), null);
						text.setFocus();
						text.selectAll();
						return false;
					}
					desc.getWriteMethod().invoke(desc.getParent(), n);
				}
				catch ( NumberFormatException e ) {
					Dialogs.showError(shell,
						String.format("Invalid integer value for '%s'.\n", desc.getDisplayName())+e.getMessage(), null);
					text.setFocus();
					text.selectAll();
					return false;
				}
			}
			else {
				throw new OkapiEditorCreationException(String.format(
					"Invalid type for the parameter '%s'.", desc.getName()));
			}
		}
		catch ( IllegalArgumentException e ) {
			Dialogs.showError(shell, e.getMessage(), null);
			return false;
		}
		catch ( IllegalAccessException e ) {
			Dialogs.showError(shell, e.getMessage(), null);
			return false;
		}
		catch ( InvocationTargetException e ) {
			Dialogs.showError(shell, e.getMessage(), null);
			return false;
		}
		return true;
	}
	
	private boolean saveCheckboxControl (Button button,
		CheckboxPart desc)
	{
		try {
			if ( !button.isEnabled() ) return true; // Don't save disabled input
			if ( desc.getType().equals(boolean.class) ) {
				desc.getWriteMethod().invoke(desc.getParent(), button.getSelection());
			}
			else if ( desc.getType().equals(String.class) ) {
				desc.getWriteMethod().invoke(desc.getParent(), (button.getSelection() ? "1" : "0"));
			}
			else if ( desc.getType().equals(int.class) ) {
				desc.getWriteMethod().invoke(desc.getParent(), (button.getSelection() ? 1 : 0));
			}
			else {
				throw new OkapiEditorCreationException(String.format(
					"Invalid type for the parameter '%s'.", desc.getName()));
			}
		}
		catch ( IllegalArgumentException e ) {
			Dialogs.showError(shell, e.getMessage(), null);
			return false;
		}
		catch ( IllegalAccessException e ) {
			Dialogs.showError(shell, e.getMessage(), null);
			return false;
		}
		catch ( InvocationTargetException e ) {
			Dialogs.showError(shell, e.getMessage(), null);
			return false;
		}
		return true;
	}
	
	private boolean saveTextAndBrowseControl (TextAndBrowsePanel ctrl,
		PathInputPart desc)
	{
		try {
			if ( !ctrl.isEnabled() ) return true; // Don't save disabled input
			if ( desc.getType().equals(String.class) || desc.getType().equals(URI.class) ) {
				if ( !desc.isAllowEmpty() && ( ctrl.getText().length() == 0 )) {
					Dialogs.showError(shell, String.format("You must specify a path for '%s'.",
						desc.getDisplayName()), null);
					ctrl.setFocus();
					return false;
				}
				if ( desc.getType().equals(URI.class) ) {
					String tmp = ctrl.getText();
					URI uri = null;
					try {
						uri = Util.toURI(tmp);
					}
					catch ( Throwable e ) {
						Dialogs.showError(shell, String.format("You must specify a valid URI '%s'.",
							desc.getDisplayName()), null);
						ctrl.setFocus();
						return false;
					}
					desc.getWriteMethod().invoke(desc.getParent(), uri);
				}
				else { // String
					desc.getWriteMethod().invoke(desc.getParent(), ctrl.getText());
				}
			}
			else {
				throw new OkapiEditorCreationException(String.format(
					"Invalid type for the parameter '%s'.", desc.getName()));
			}
		}
		catch ( IllegalArgumentException e ) {
			Dialogs.showError(shell, e.getMessage(), null);
			return false;
		}
		catch ( IllegalAccessException e ) {
			Dialogs.showError(shell, e.getMessage(), null);
			return false;
		}
		catch ( InvocationTargetException e ) {
			Dialogs.showError(shell, e.getMessage(), null);
			return false;
		}
		return true;
	}
	
	private boolean saveFolderControl (TextAndBrowsePanel ctrl,
		FolderInputPart desc)
	{
		try {
			if ( !ctrl.isEnabled() ) return true; // Don't save disabled input
			if ( desc.getType().equals(String.class) ) {
				if ( ctrl.getText().length() == 0 ) {
					Dialogs.showError(shell, String.format("You must specify a directory for '%s'.",
						desc.getDisplayName()), null);
					ctrl.setFocus();
					return false;
				}
				desc.getWriteMethod().invoke(desc.getParent(), ctrl.getText());
			}
			else {
				throw new OkapiEditorCreationException(String.format(
					"Invalid type for the parameter '%s'.", desc.getName()));
			}
		}
		catch ( IllegalArgumentException e ) {
			Dialogs.showError(shell, e.getMessage(), null);
			return false;
		}
		catch ( IllegalAccessException e ) {
			Dialogs.showError(shell, e.getMessage(), null);
			return false;
		}
		catch ( InvocationTargetException e ) {
			Dialogs.showError(shell, e.getMessage(), null);
			return false;
		}
		return true;
	}
	
	private boolean saveCodeFinderControl (InlineCodeFinderPanel ctrl,
		CodeFinderPart desc)
	{
		try {
			if ( !ctrl.isEnabled() ) return true; // Don't save disabled input
			if ( desc.getType().equals(String.class) ) {
				String tmp = ctrl.getRules();
				desc.getWriteMethod().invoke(desc.getParent(), tmp);
			}
			else {
				throw new OkapiEditorCreationException(String.format(
					"Invalid type for the parameter '%s'.", desc.getName()));
			}
		}
		catch ( IllegalArgumentException e ) {
			Dialogs.showError(shell, e.getMessage(), null);
			return false;
		}
		catch ( IllegalAccessException e ) {
			Dialogs.showError(shell, e.getMessage(), null);
			return false;
		}
		catch ( InvocationTargetException e ) {
			Dialogs.showError(shell, e.getMessage(), null);
			return false;
		}
		return true;
	}
	
	private boolean saveCheckListControl (Table table,
		CheckListPart part)
	{
		try {
			if ( !table.isEnabled() ) return true; // Don't save disabled input
			Map<String, ParameterDescriptor> map = part.getEntries();
			int i = 0;
			for ( String name : map.keySet() ) {
				ParameterDescriptor desc = map.get(name);
				TableItem ti = table.getItem(i);
				desc.getWriteMethod().invoke(desc.getParent(), ti.getChecked());
				i++;
			}
		}
		catch ( Throwable e ) {
			Dialogs.showError(shell, e.getMessage(), null);
			return false;
		}
		return true;
	}
	
	private boolean saveListControl (List list,
		ListSelectionPart desc)
	{
		try {
			if ( !list.isEnabled() ) return true; // Don't save disabled input
			int n = list.getSelectionIndex();
			if ( n > -1 ) {
				if ( desc.getType().equals(String.class) ) {
					desc.getWriteMethod().invoke(desc.getParent(), ((String[])list.getData())[n]);
				}
				else if ( desc.getType().equals(int.class) ) {
					try {
						int value = Integer.valueOf(((String[])list.getData())[n]);
						desc.getWriteMethod().invoke(desc.getParent(), value);
					}
					catch ( NumberFormatException  e ) {
						throw new OkapiEditorCreationException(String.format(
							"Invalid integer value '%s' for the parameter '%s'.", ((String[])list.getData())[n], desc.getName()));
					}
				}
				else {
					throw new OkapiEditorCreationException(String.format(
						"Invalid type for the parameter '%s'.", desc.getName()));
				}
			}
		}
		catch ( IllegalArgumentException e ) {
			Dialogs.showError(shell, e.getMessage(), null);
			return false;
		}
		catch ( IllegalAccessException e ) {
			Dialogs.showError(shell, e.getMessage(), null);
			return false;
		}
		catch ( InvocationTargetException e ) {
			Dialogs.showError(shell, e.getMessage(), null);
			return false;
		}
		return true;
	}

	private boolean saveComboControl (Combo combo,
		ListSelectionPart desc)
	{
		try {
			if ( !combo.isEnabled() ) return true; // Don't save disabled input
			int n = combo.getSelectionIndex();
			if ( n > -1 ) {
				// Get the value from the user-data list
				if ( desc.getType().equals(String.class) ) {
					desc.getWriteMethod().invoke(desc.getParent(), ((String[])combo.getData())[n]);
				}
				else if ( desc.getType().equals(int.class) ) {
					try {
						int value = Integer.valueOf(((String[])combo.getData())[n]);
						desc.getWriteMethod().invoke(desc.getParent(), value);
					}
					catch ( NumberFormatException  e ) {
						throw new OkapiEditorCreationException(String.format(
							"Invalid integer value '%s' for the parameter '%s'.", ((String[])combo.getData())[n], desc.getName()));
					}
				}
				else {
					throw new OkapiEditorCreationException(String.format(
						"Invalid type for the parameter '%s'.", desc.getName()));
				}
			}
			
		}
		catch ( IllegalArgumentException e ) {
			Dialogs.showError(shell, e.getMessage(), null);
			return false;
		}
		catch ( IllegalAccessException e ) {
			Dialogs.showError(shell, e.getMessage(), null);
			return false;
		}
		catch ( InvocationTargetException e ) {
			Dialogs.showError(shell, e.getMessage(), null);
			return false;
		}
		return true;
	}
	
	private void setInputControl (Text text,
		TextInputPart desc)
	{
		try {
			String tmp = "";
			if ( desc.getType().equals(String.class) ) {
				tmp = (String)desc.getReadMethod().invoke(desc.getParent());
			}
			else if ( desc.getType().equals(int.class) ) {
				tmp = ((Integer)desc.getReadMethod().invoke(desc.getParent())).toString();
			}
			else {
				throw new OkapiEditorCreationException(String.format(
					"Invalid type for the parameter '%s'.", desc.getName()));
			}
			if ( tmp == null ) text.setText("");
			else text.setText(tmp);
		}
		catch ( IllegalArgumentException e ) {
			Dialogs.showError(shell, e.getMessage(), null);
		}
		catch ( IllegalAccessException e ) {
			Dialogs.showError(shell, e.getMessage(), null);
		}
		catch ( InvocationTargetException e ) {
			Dialogs.showError(shell, e.getMessage(), null);
		}
	}
	
	private void setCodeFinderControl (InlineCodeFinderPanel
		panel, CodeFinderPart desc)
	{
		try {
			String tmp = "";
			if ( desc.getType().equals(String.class) ) {
				tmp = (String)desc.getReadMethod().invoke(desc.getParent());
			}
			else {
				throw new OkapiEditorCreationException(String.format(
					"Invalid type for the parameter '%s'.", desc.getName()));
			}
			if ( tmp == null ) panel.setRules("");
			else panel.setRules(tmp);
		}
		catch ( IllegalArgumentException e ) {
			Dialogs.showError(shell, e.getMessage(), null);
		}
		catch ( IllegalAccessException e ) {
			Dialogs.showError(shell, e.getMessage(), null);
		}
		catch ( InvocationTargetException e ) {
			Dialogs.showError(shell, e.getMessage(), null);
		}
	}
	
	private boolean saveSpinnerControl (Spinner ctrl,
		SpinInputPart desc)
	{
		try {
			if ( !ctrl.isEnabled() ) return true; // Don't save disabled input
			if ( desc.getType().equals(int.class) ) {
				desc.getWriteMethod().invoke(desc.getParent(), ctrl.getSelection());
			}
			else {
				throw new OkapiEditorCreationException(String.format(
					"Invalid type for the parameter '%s'.", desc.getName()));
			}
		}
		catch ( IllegalArgumentException e ) {
			Dialogs.showError(shell, e.getMessage(), null);
			return false;
		}
		catch ( IllegalAccessException e ) {
			Dialogs.showError(shell, e.getMessage(), null);
			return false;
		}
		catch ( InvocationTargetException e ) {
			Dialogs.showError(shell, e.getMessage(), null);
			return false;
		}
		return true;
	}

	private void setCheckListControl (Table table,
		CheckListPart part)
	{
		try {
			Map<String, ParameterDescriptor> map = part.getEntries();
			for ( String name : map.keySet() ) {
				ParameterDescriptor desc = map.get(name);
				TableItem ti = new TableItem (table, SWT.NONE);
				ti.setText(desc.getDisplayName());
				ti.setChecked((Boolean)desc.getReadMethod().invoke(desc.getParent()));
			}
		}
		catch ( IllegalArgumentException e ) {
			Dialogs.showError(shell, e.getMessage(), null);
		}
		catch ( IllegalAccessException e ) {
			Dialogs.showError(shell, e.getMessage(), null);
		}
		catch ( InvocationTargetException e ) {
			Dialogs.showError(shell, e.getMessage(), null);
		}
	}
	
	private void setListControl (List list,
		ListSelectionPart desc)
	{
		try {
			String[] labels = desc.getChoicesLabels();
			String[] values = desc.getChoicesValues();

			// If we have labels: check for matching list
			if ( labels == null ) {
				labels = values; // Use the values as labels
			}
			else { // Labels available
				if ( labels.length != values.length ) {
					throw new OkapiEditorCreationException(String.format(
						"The number of values and labels must be the same for the parameter '%s'.", desc.getName()));
				}
			}

			// Set the control
			String current;
			if ( desc.getType().equals(String.class) ) {
				current = (String)desc.getReadMethod().invoke(desc.getParent());
			}
			else if ( desc.getType().equals(int.class) ) {
				current = String.valueOf((Integer)desc.getReadMethod().invoke(desc.getParent()));
			}
			else {
				throw new OkapiEditorCreationException(String.format(
					"Invalid type for the parameter '%s'.", desc.getName()));
			}
			// Now lookup the value
			list.setData(values); // Store the list of values in the user-data
			if ( current == null ) current = "";
			int found = -1;
			int n = 0;
			for ( String item : values ) {
				list.add(labels[n]);
				if ( item.equals(current) ) found = n;
				n++;
			}
			if ( found > -1 ) {
				list.select(found);
				list.showSelection();
			}
		}
		catch ( IllegalArgumentException e ) {
			Dialogs.showError(shell, e.getMessage(), null);
		}
		catch ( IllegalAccessException e ) {
			Dialogs.showError(shell, e.getMessage(), null);
		}
		catch ( InvocationTargetException e ) {
			Dialogs.showError(shell, e.getMessage(), null);
		}
	}
	
	private void setComboControl (Combo combo,
		ListSelectionPart desc)
	{
		try {
			String[] labels = desc.getChoicesLabels();
			String[] values = desc.getChoicesValues();

			// If we have labels: check for matching list
			if ( labels == null ) {
				labels = values; // Use the values as labels
			}
			else { // Labels available
				if ( labels.length != values.length ) {
					throw new OkapiEditorCreationException(String.format(
						"The number of values and labels must be the same for the parameter '%s'.", desc.getName()));
				}
			}
			
			// Set the control
			String current;
			if ( desc.getType().equals(String.class) ) {
				current = (String)desc.getReadMethod().invoke(desc.getParent());
			}
			else if ( desc.getType().equals(int.class) ) {
				current = String.valueOf((Integer)desc.getReadMethod().invoke(desc.getParent()));
			}
			else {
				throw new OkapiEditorCreationException(String.format(
					"Invalid type for the parameter '%s'.", desc.getName()));
			}
			
			combo.setData(values); // Store the list of values in the user-data
			if ( current == null ) current = "";
			int found = -1;
			int n = 0;
			for ( String item : values ) {
				combo.add(labels[n]);
				if ( item.equals(current) ) found = n;
				n++;
			}
			if ( found > -1 ) {
				combo.select(found);
			}
		}
		catch ( IllegalArgumentException e ) {
			Dialogs.showError(shell, e.getMessage(), null);
		}
		catch ( IllegalAccessException e ) {
			Dialogs.showError(shell, e.getMessage(), null);
		}
		catch ( InvocationTargetException e ) {
			Dialogs.showError(shell, e.getMessage(), null);
		}
	}
	
	private void setCheckboxControl (Button button,
		CheckboxPart desc)
	{
		try {
			if ( desc.getType().equals(boolean.class) ) {
				button.setSelection((Boolean)desc.getReadMethod().invoke(desc.getParent()));
			}
			else if ( desc.getType().equals(int.class) ) {
				int n = (Integer)desc.getReadMethod().invoke(desc.getParent());
				button.setSelection(n!=0);
			}
			else if ( desc.getType().equals(String.class) ) {
				String tmp = (String)desc.getReadMethod().invoke(desc.getParent());
				button.setSelection((tmp!=null) && !tmp.equals("0"));
			}
			else {
				throw new OkapiEditorCreationException(String.format(
					"Invalid type for the parameter '%s'.", desc.getName()));
			}
		}
		catch ( IllegalArgumentException e ) {
			Dialogs.showError(shell, e.getMessage(), null);
		}
		catch ( IllegalAccessException e ) {
			Dialogs.showError(shell, e.getMessage(), null);
		}
		catch ( InvocationTargetException e ) {
			Dialogs.showError(shell, e.getMessage(), null);
		}
	}
	
	private void setPathControl (TextAndBrowsePanel ctrl,
		PathInputPart desc)
	{
		try {
			if ( desc.getType().equals(String.class) ) {
				String tmp = (String)desc.getReadMethod().invoke(desc.getParent());
				ctrl.setText((tmp==null) ? "" : tmp);
			}
			else if ( desc.getType().equals(URI.class) ) {
				URI uri = (URI)desc.getReadMethod().invoke(desc.getParent());
				ctrl.setText(uri.toString());
			}
			else {
				throw new OkapiEditorCreationException(String.format(
					"Invalid type for the parameter '%s'.", desc.getName()));
			}
		}
		catch ( IllegalArgumentException e ) {
			Dialogs.showError(shell, e.getMessage(), null);
		}
		catch ( IllegalAccessException e ) {
			Dialogs.showError(shell, e.getMessage(), null);
		}
		catch ( InvocationTargetException e ) {
			Dialogs.showError(shell, e.getMessage(), null);
		}
	}
	
	private void setSpinnerControl (Spinner spinner,
		SpinInputPart desc)
	{
		try {
			if ( desc.getType().equals(int.class) ) {
				int n = (Integer)desc.getReadMethod().invoke(desc.getParent());
				spinner.setSelection(n);
			}
			else {
				throw new OkapiEditorCreationException(String.format(
					"Invalid type for the parameter '%s'.", desc.getName()));
			}
		}
		catch ( IllegalArgumentException e ) {
			Dialogs.showError(shell, e.getMessage(), null);
		}
		catch ( IllegalAccessException e ) {
			Dialogs.showError(shell, e.getMessage(), null);
		}
		catch ( InvocationTargetException e ) {
			Dialogs.showError(shell, e.getMessage(), null);
		}
	}
	
	private void setFolderControl (TextAndBrowsePanel ctrl,
		FolderInputPart desc)
	{
		try {
			if ( desc.getType().equals(String.class) ) {
				String tmp = (String)desc.getReadMethod().invoke(desc.getParent());
				ctrl.setText((tmp==null) ? "" : tmp);
			}
			else {
				throw new OkapiEditorCreationException(String.format(
					"Invalid type for the parameter '%s'.", desc.getName()));
			}
		}
		catch ( IllegalArgumentException e ) {
			Dialogs.showError(shell, e.getMessage(), null);
		}
		catch ( IllegalAccessException e ) {
			Dialogs.showError(shell, e.getMessage(), null);
		}
		catch ( InvocationTargetException e ) {
			Dialogs.showError(shell, e.getMessage(), null);
		}
	}
	
	private Composite lookupParent (IContainerPart desc) {
		if ( desc == null ) return mainComposite;
		//TODO
		return mainComposite;
	}
	
	private boolean showDialog () {
		shell.open();
		while ( !shell.isDisposed() ) {
			if ( !shell.getDisplay().readAndDispatch() )
				shell.getDisplay().sleep();
		}
		return result;
	}

}
