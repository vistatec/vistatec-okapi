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

package net.sf.okapi.common.ui.filters;

import java.text.Collator;
import java.util.Iterator;
import java.util.Locale;

import net.sf.okapi.common.filters.FilterConfiguration;
import net.sf.okapi.common.filters.IFilterConfigurationMapper;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * Internal class used by FilterConfigurationPanel.
 */
class FilterConfigurationsTableModel {

	static final int ID_COLINDEX = 1;
	
	private Table table;
	private IFilterConfigurationMapper mapper;

	void linkTable (Table newTable) {
		table = newTable;
		TableColumn col = new TableColumn(table, SWT.NONE);
		col.setText(Res.getString("FilterConfigurationsTableModel.name")); //$NON-NLS-1$
		Listener sortListener = new SortListener();
		col.addListener(SWT.Selection, sortListener);
		table.setSortColumn(col);
		col = new TableColumn(table, SWT.NONE);
		col.setText(Res.getString("FilterConfigurationsTableModel.id")); //$NON-NLS-1$
		col.addListener(SWT.Selection, sortListener);
		col = new TableColumn(table, SWT.NONE);
		col.setText(Res.getString("FilterConfigurationsTableModel.mimeType")); //$NON-NLS-1$
		col.addListener(SWT.Selection, sortListener);
		col = new TableColumn(table, SWT.NONE);
		col.setText(Res.getString("FilterConfigurationsTableModel.custom")); //$NON-NLS-1$
		col.addListener(SWT.Selection, sortListener);
	}

	void setMapper (IFilterConfigurationMapper mapper) {
		this.mapper = mapper;
	}

	/**
	 * Refill the table with the configurations in the mapper.
	 * The method also tries to select the provided configuration identifier.
	 * @param selection index of the configuration to select (if selectedConfigId
	 * is null or not found). If the value is out of range the last configuration
	 * is selected. 
	 * @param selectedConfigId identifier of the configuration to select, or zero
	 * to select by index. If the configuration is not found, the index selection
	 * is used instead.
	 */
	void updateTable (int selection, String selectedConfigId)
	{
		table.removeAll();
		if ( mapper == null ) return;
		Iterator<FilterConfiguration> iter = mapper.getAllConfigurations();
		FilterConfiguration config;
		while ( iter.hasNext() ) {
			config = iter.next();
			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(0, config.name);
			item.setText(ID_COLINDEX, config.configId);
			item.setText(2, config.mimeType);
			item.setText(3, config.custom ?
					Res.getString("FilterConfigurationsTableModel.customFlag") :
					Res.getString("FilterConfigurationsTableModel.predefinedFlag")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		// Re-sort the table
		sortTableByColumn(table.getSortColumn());

		// First check for a selection by name, then fall back
		// on selecting by index
		if ( restoreSelection(selectedConfigId) ) {
			return;
		}
		if ( (selection < 0) || ( selection > table.getItemCount()-1 ) ) {
			selection = table.getItemCount()-1;
		}
		if ( table.getItemCount() > 0 ) {
			table.setSelection(selection);
		}
	}

	/**
	 * We are unlikely to scale to a number of filter configs where this
	 * algorithm becomes a problem.
	 */
	private void sortTableByColumn(TableColumn column) {
		Collator collator = Collator.getInstance(Locale.getDefault());
		int index = getColumnIndex(column);
		table.setSortColumn(column);
		TableItem[] tableItems = table.getItems();
		for ( int i = 1; i < tableItems.length; i++ ) {
			String text1 = tableItems[i].getText(index);
			for ( int j = 0; j < i; j++ ) {
				String text2 = tableItems[j].getText(index);
				if ( collator.compare(text1, text2) < 0 ) {
					String[] fcData = {tableItems[i].getText(0), tableItems[i].getText(1),
							tableItems[i].getText(2), tableItems[i].getText(3)};
					tableItems[i].dispose();
					TableItem item = new TableItem(table, SWT.NONE, j);
					item.setText(fcData);
					tableItems = table.getItems();
					break;
				}
			}
		}
	}


	private int getColumnIndex(TableColumn col) {
		TableColumn[] cols = table.getColumns();
		for ( int i = 0; i < cols.length; i++ ) {
			if ( col == cols[i] ) {
				return i;
			}
		}
		throw new IllegalStateException("Unknown table column: " + col);
	}

	private boolean restoreSelection(String selectedConfigId) {
		if ( selectedConfigId != null ) {
			for ( TableItem item : table.getItems() ) {
				if ( item.getText(1).equals(selectedConfigId) ) {
					table.setSelection(item);
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Listener to sort table in place based on the current selected
	 * column, then restore the previous selection (if there was one).
	 */
	class SortListener implements Listener {
		@Override
		public void handleEvent(Event e) {
			TableColumn column = (TableColumn)e.widget;
			TableItem[] selections = table.getSelection();
			String selectedConfigId = (selections.length > 0) ?
				selections[0].getText(1) : null;

			sortTableByColumn(column);
			restoreSelection(selectedConfigId);
		}
	};
}
