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

package net.sf.okapi.filters.rainbowkit.ui;

import net.sf.okapi.filters.rainbowkit.Manifest;
import net.sf.okapi.filters.rainbowkit.MergingInfo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

class ManifestTableModel {
	
	private Table table;
	private Manifest manifest;

	public void linkTable (Table newTable) {
		table = newTable;
		TableColumn col = new TableColumn(table, SWT.NONE);
		col.setText("Documents to Post-Process");
		col = new TableColumn(table, SWT.NONE);
		col.setText("Missing?");
		col = new TableColumn(table, SWT.NONE);
		col.setText("Output");
	}
	
	public void setManifest (Manifest newManifest) {
		manifest = newManifest;
		updateTable(null, 0);
	}

	void updateTable (int[] selection,
		int index)
	{
		table.removeAll();
		MergingInfo info;
		for ( int i : manifest.getItems().keySet() ) {
			info = manifest.getItem(i);
			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(0, info.getRelativeInputPath());
//			if ( !info.exists() ) item.setText(1, "missing");
			item.setText(2, info.getRelativeTargetPath());
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
		MergingInfo info;
		for ( int i=0; i<table.getItemCount(); i++ ) {
			info = manifest.getItem(i+1); // docID are 1-based
			if ( info == null ) continue; // Could be a non-extractable file
			info.setSelected(table.getItem(i).getChecked());
		}
	}
}
