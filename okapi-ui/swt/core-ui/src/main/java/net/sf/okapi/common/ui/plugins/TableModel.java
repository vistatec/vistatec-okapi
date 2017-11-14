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

package net.sf.okapi.common.ui.plugins;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

class TableModel {
	
	private Table table;
	private boolean maxMode;

	TableModel (Table newTable,
		boolean maxMode)
	{
		table = newTable;
		this.maxMode = maxMode;
		TableColumn col = new TableColumn(table, SWT.NONE);
		col.setText("Name");
		
		if ( maxMode ) {
			col = new TableColumn(table, SWT.NONE);
			col.setText("Provider");
		}
		else {
			col = new TableColumn(table, SWT.NONE);
			col.setText("Locked?");
		}
		col.pack();
	}

	void updateTable (List<PluginInfo> list,
		List<String> lockedPlugins,
		int index)
	{
		table.removeAll();
		for ( PluginInfo info : list) {
			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(0, info.getName());
			if ( maxMode ) {
				item.setText(1, info.getProvider());
			}
			else {
				if ( lockedPlugins.contains(info.getName()) ) {
					item.setText(1, "Locked");
				}
			}
			item.setData(info);
		}
		if ( table.getItemCount() > 0 ) {
			if ( index > -1 ) {
				if ( index > table.getItemCount()-1 ) {
					index = table.getItemCount()-1;
				}
			}
			else index = 0;
			table.setSelection(index);
		}
	}

}
