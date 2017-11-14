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

import java.util.ArrayList;

import net.sf.okapi.lib.segmentation.Rule;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

class RulesTableModel {
	
	Table table;
	ArrayList<Rule> list;

	void linkTable (Table newTable) {
		table = newTable;
		TableColumn col = new TableColumn(table, SWT.NONE);
		col.setText(Res.getString("ruleTable.type")); //$NON-NLS-1$
		col = new TableColumn(table, SWT.NONE);
		col.setText(Res.getString("ruleTable.beforeBreak")); //$NON-NLS-1$
		col = new TableColumn(table, SWT.NONE);
		col.setText(Res.getString("ruleTable.afterBreak")); //$NON-NLS-1$
	}
	
	void setLanguageRules (ArrayList<Rule> list) {
		this.list = list;
	}

	void updateTable (int selection) {
		table.removeAll();
		if ( list == null ) return;
		for ( Rule rule : list ) {
			TableItem item = new TableItem(table, SWT.NONE);
			item.setChecked(rule.isActive());
			item.setText(0, rule.isBreak() ? Res.getString("ruleTable.break") : Res.getString("ruleTable.noBreak")); //$NON-NLS-1$ //$NON-NLS-2$
			item.setText(1, rule.getBefore());
			item.setText(2, rule.getAfter());
		}
		
		if (( selection < 0 ) || ( selection > table.getItemCount()-1 )) {
			selection = table.getItemCount()-1;
		}
		if ( table.getItemCount() > 0 ) {
			table.setSelection(selection);
		}
	}

}
