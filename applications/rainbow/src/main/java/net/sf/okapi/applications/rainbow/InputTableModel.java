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

package net.sf.okapi.applications.rainbow;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

class InputTableModel {
	
	Table               table;
	ArrayList<Input>    inputList;

	void linkTable (Table newTable) {
		table = newTable;
		TableColumn col = new TableColumn(table, SWT.NONE);
		col.setText(Res.getString("INPTAB_RELPATH")); //$NON-NLS-1$
		col = new TableColumn(table, SWT.NONE);
		col.setText(Res.getString("INPTAB_FSETTINGS")); //$NON-NLS-1$
	}

	void setProject (ArrayList<Input> inputList) {
		this.inputList = inputList;
	}
	
	/**
	 * Refresh the items in the table, and optionally, select some of them.
	 * @param selection The list of the indices of the items to select after refresh,
	 * or null to use the specified index.
	 * @param index The index to select.
	 */
	void updateTable (int[] selection,
		int index)
	{
		table.removeAll();
		for ( Input inp : inputList ) {
			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(0, inp.relativePath);
			item.setText(1, inp.filterConfigId);
		}
		if ( selection == null ) {
			if ( table.getItemCount() > 0 ) {
				if ( index > -1 ) {
					if ( index > table.getItemCount()-1 ) {
						index = table.getItemCount()-1;
					}
				}
				else index = 0;
				table.setSelection(index);
			}
			// Else: nothing to select	
		}
		else table.setSelection(selection);
	}

}
