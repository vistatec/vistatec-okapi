/*===========================================================================
  Copyright (C) 2011 by the Okapi Framework contributors
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

package net.sf.okapi.filters.transifex.ui;

import java.util.List;

import net.sf.okapi.filters.transifex.Project;
import net.sf.okapi.lib.transifex.ResourceInfo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

class ProjectTableModel {
	
	private Table table;
	private Project project;

	public void linkTable (Table newTable) {
		table = newTable;
		TableColumn col = new TableColumn(table, SWT.NONE);
		col.setText("Resource to Process");
		col = new TableColumn(table, SWT.NONE);
		col.setText("Resource ID");
	}
	
	public void setProject (Project project) {
		this.project = project;
		updateTable(null, 0);
	}

	void updateTable (int[] selection,
		int index)
	{
		table.removeAll();
		for ( ResourceInfo info : project.getResources() ) {
			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(0, info.getName());
			item.setText(1, info.getId());
			item.setChecked(info.getSelected());
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

	public void saveData () {
		ResourceInfo info;
		List<ResourceInfo> list = project.getResources();
		for ( int i=0; i<table.getItemCount(); i++ ) {
			info = list.get(i);
			info.setSelected(table.getItem(i).getChecked());
		}
	}
}
