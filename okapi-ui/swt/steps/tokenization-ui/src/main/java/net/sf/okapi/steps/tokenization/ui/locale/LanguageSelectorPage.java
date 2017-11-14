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

package net.sf.okapi.steps.tokenization.ui.locale;

import java.util.ArrayList;

import net.sf.okapi.common.ui.abstracteditor.AbstractBaseDialog;
import net.sf.okapi.common.ui.abstracteditor.IDialogPage;
import net.sf.okapi.common.ui.abstracteditor.TableAdapter;
import net.sf.okapi.lib.extra.INotifiable;
import net.sf.okapi.steps.tokenization.locale.LanguageList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;

public class LanguageSelectorPage extends Composite implements IDialogPage {
	protected Label listDescr;
	private Table table;
	private TableColumn col1;
	private TableColumn col2;
	private TableAdapter adapter;
	private TableColumn col3;

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public LanguageSelectorPage(final Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(1, false));
		
		listDescr = new Label(this, SWT.NONE);
		listDescr.setData("name", "listDescr");
		listDescr.setText("The program displays installed languages and their codes.");
		
		table = new Table(this, SWT.BORDER | SWT.FULL_SELECTION);
		// Recreate table, SWT Designer cannot handle *else* here
		if (hasCheckBoxes()) {
			
			table.dispose(); //!!! Otherwise layout gets broken
			table = new Table(this, SWT.BORDER | SWT.CHECK | SWT.FULL_SELECTION);
		}
				
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				
				Object dialog = getData("dialog");
				
				if (dialog instanceof AbstractBaseDialog) {
				
					// Double-click closes the dialog only if it has a parent, and is not a top-level window
					Shell shell = ((AbstractBaseDialog) dialog).getShell();
					Object parent = shell.getData("parent");
					if (parent != null)
						if (dialog instanceof INotifiable)
							((INotifiable) dialog).exec(this, AbstractBaseDialog.NOTIFICATION_OK, null);
				}				
			}
		});
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gridData.heightHint = 400;
		table.setLayoutData(gridData);
		table.setData("name", "table");
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		col1 = new TableColumn(table, SWT.NONE);
		col1.setData("name", "col1");
		col1.setWidth(348);
		col1.setText("Language");
		
		col2 = new TableColumn(table, SWT.NONE);
		col2.setData("name", "col2");
		col2.setWidth(150);
		col2.setText("Code Okapi");
		
		adapter = new TableAdapter(table);
		
		col3 = new TableColumn(table, SWT.NONE);
		col3.setData("name", "col3");
		col3.setWidth(150);
		col3.setText("Code CLDR");
		adapter.setRelColumnWidths(new double [] {6, 1.25, 1.25});
	}

	protected boolean hasCheckBoxes() {

		return false;
	}

	public boolean canClose(boolean isOK) {
		// TODO Auto-generated method stub
		return true;
	}

	public void interop(Widget speaker) {
		// TODO Auto-generated method stub
		
	}

	@SuppressWarnings("unchecked")
	public boolean load(Object data) {
				
//		list.setItems(LanguageList.getLanguages());
//		list.pack();
		
		String [] languages = LanguageList.getLanguages();
		String [] codes = LanguageList.getLanguageCodes_Okapi();
		String [] codes2 = LanguageList.getLanguageCodes_ICU();
		
		for (int i = 0; i < Math.min(languages.length, codes.length); i++) {
			
			adapter.addRow(new String[] {languages[i], codes[i], codes2[i]}, false);
		}
		
		adapter.sort(1, false);
		
		if (data instanceof ArrayList<?>) {
			
			ArrayList<String> list = (ArrayList<String>) data;
			
			for (String string : list) {
				
				TableItem item = adapter.findValue(string, 2);
				if (item == null) continue;
				
				item.setChecked(true);
			}
		}
		
		return true;
	}

	@SuppressWarnings("unchecked")
	public boolean save(Object data) {
		
		if (data instanceof ArrayList<?>) {
		
			ArrayList<String> list = (ArrayList<String>) data;
			
			list.clear();
			for (TableItem item : table.getItems())
				if (item.getChecked())
					list.add(item.getText(1));
		}
				
		return true;
	}

}
