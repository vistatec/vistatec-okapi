/*===========================================================================
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

package net.sf.okapi.steps.tokenization.ui.tokens;

import java.util.ArrayList;

import net.sf.okapi.common.ui.abstracteditor.IDialogPage;
import net.sf.okapi.common.ui.abstracteditor.SWTUtil;
import net.sf.okapi.common.ui.abstracteditor.TableAdapter;
import net.sf.okapi.steps.tokenization.tokens.Parameters;
import net.sf.okapi.steps.tokenization.tokens.TokenItem;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;

public class TokenSelectorPage extends Composite implements IDialogPage {
	protected Label listDescr;
	private Table table;
	private TableColumn colName;
	private TableColumn colDescr;
	protected Button add;
	protected Button modify;
	protected Button remove;
	private TableAdapter adapter;
	protected boolean modified;

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public TokenSelectorPage(Composite parent, int style) {
		super(parent, style);
		
		
		setLayout(new GridLayout(2, false));

		listDescr = new Label(this, SWT.NONE);
		listDescr.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		listDescr.setText("Select tokens in the table below using the check-boxes:");
		listDescr.setData("name", "listDescr");
		
		table = new Table(this, SWT.BORDER | SWT.FULL_SELECTION);
		
		// Recreate table, SWT Designer cannot handle *else* here
		if (hasCheckBoxes()) {
			
			table.dispose(); //!!! Otherwise layout gets broken
			table = new Table(this, SWT.BORDER | SWT.CHECK | SWT.FULL_SELECTION);
		}
					
		table.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				interop(e.widget);
			}
		});
		
		table.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
				
				addModifyRow(table.getItem(new Point(e.x, e.y)));
			}
		});
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 4);
		gridData.widthHint = 500;
		gridData.heightHint = 400;
		table.setLayoutData(gridData);
		table.setData("name", "table");
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		colName = new TableColumn(table, SWT.NONE);
		colName.setData("name", "colName");
		colName.setWidth(150);
		colName.setText("Name");
		
		colDescr = new TableColumn(table, SWT.LEFT);
		colDescr.setData("name", "colDescr");
		colDescr.setWidth(100);
		colDescr.setText("Description");

		add = new Button(this, SWT.NONE);
		add.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				
				addModifyRow(null);
			}
		});
		GridData gridData_1 = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gridData_1.widthHint = 70;
		add.setLayoutData(gridData_1);
		add.setData("name", "add");
		add.setText("Add...");
		
		modify = new Button(this, SWT.NONE);
		modify.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				
				addModifyRow(table.getItem(table.getSelectionIndex()));
			}
		});
		modify.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		modify.setData("name", "modify");
		modify.setText("Modify...");
		
		remove = new Button(this, SWT.NONE);
		remove.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				
				adapter.removeSelected();
				modified = true;
				interop(e.widget);
			}
		});
		remove.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
		remove.setData("name", "remove");
		remove.setText("Remove");
		
		adapter = new TableAdapter(table);
		new Label(this, SWT.NONE);
		adapter.setRelColumnWidths(new double [] {1, 3});		
	}

	protected boolean hasCheckBoxes() {

		return false;
	}

	protected void addModifyRow(TableItem item) {
		
		if (item == null) { // Add new item			
			adapter.unselect();
			
			Object res = SWTUtil.inputQuery(AddModifyTokenPage.class, getShell(), "Add token type", 
					new String[] {"", ""}, null);
			
			if (res != null) {
				
				modified = true;
				adapter.addModifyRow((String []) res, 1, TableAdapter.DUPLICATE_REPLACE);
			}
			else
				adapter.restoreSelection();
		}
		else {
			
			Object res = SWTUtil.inputQuery(AddModifyTokenPage.class, getShell(), "Modify token type", 
					SWTUtil.getText(item), null); 
			
			if (res != null) {					
				
				modified = true;
				adapter.modifyRow(item, (String []) res);
			}
		}
		
		adapter.sort(1, false);
		interop(table);  // Selection changes
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	public boolean canClose(boolean isOK) {

		return true;
	}

	public void interop(Widget speaker) {
		
		if (SWTUtil.checkControl(modify))
			modify.setEnabled(table.getItemCount() > 0 && table.getSelectionIndex() != -1);
		
		if (SWTUtil.checkControl(remove))
			remove.setEnabled(modify.getEnabled());
	}

	@SuppressWarnings("unchecked")
	public boolean load(Object data) {

//		if (data == null) {
//			
//			Object d = getData("dialog");
//			
//			if (d instanceof AbstractBaseDialog) {
//				
//				data = new Parameters();
//				((AbstractBaseDialog) d).setData(data);
//			}			
//		}
//		
			Parameters params = new Parameters();
			if (!params.loadItems()) return false;
			
			adapter.clear();
			
			for (TokenItem item : params.getItems())					
				adapter.addRow(new String[] {item.getName(), item.getDescription()}, false);

			adapter.sort(1, false);
			modified = false;				
		
			if (data instanceof ArrayList<?>) {
				
				ArrayList<String> list = (ArrayList<String>) data;
				
				for (String string : list) {
					
					TableItem item = adapter.findValue(string, 1);
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
					list.add(item.getText(0));
		}

		return true;
	}

	protected TableAdapter getAdapter() {
		
		return adapter;
	}
}
