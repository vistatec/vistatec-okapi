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

package net.sf.okapi.common.ui.abstracteditor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import net.sf.okapi.common.ListUtil;
import net.sf.okapi.common.Util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * A wrapper around SWT table for manipulations with table data
 * 
 * @version 0.1 30.06.2009
 */

public class TableAdapter {

	public static final int DUPLICATE_ALLOW = 1;
	public static final int DUPLICATE_REJECT = 2;
	public static final int DUPLICATE_REPLACE = 3;
	
	Table table;
	double[] columnPoints = null;
	TableItem saveSelItem = null;
	
//	private Button addButton;
//	private Button modifyButton;
//	private Button removeButton;
	
	public TableAdapter(Table table) {
		super();
		
		this.table = table;
	}
	
//	public TableAdapter(Table table, Button addButton, Button modifyButton, Button removeButton) {
//		
//		this(table);
//		
//		this.addButton = addButton;
//		this.modifyButton = modifyButton;
//		this.removeButton = removeButton;
//	}

	public void updateColumnWidths(boolean blockRedraw) {
		
		if (columnPoints == null) return;
		
		float pointsWidth = 0;
		
		for (int i = 0; i < table.getColumnCount(); i++)
			//pointsWidth += ((i < columnPoints.length - 1) ? columnPoints[i]: 1);
			pointsWidth += columnPoints[i];
			
		float coeff = table.getClientArea().width / pointsWidth;
		
		if (blockRedraw) table.setRedraw(false);

		try {
			for (int i = 0; i < table.getColumnCount(); i++)
				//table.getColumn(i).setWidth((int)(((i < columnPoints.length - 1) ? columnPoints[i]: 1) * coeff));		
				table.getColumn(i).setWidth((int)(columnPoints[i] * coeff));
		}
		
		finally {				
			if (blockRedraw) table.setRedraw(true);
		}
	}

	/**
	 * @param columnPoints
	 */
	public void setRelColumnWidths(double[] columnPoints) {
		
		this.columnPoints = columnPoints;
		
		table.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				updateColumnWidths(false);
			}			
		});
	}

	public void addRow(String[] rowData) {
		
		TableItem item = new TableItem (table, SWT.NONE);
		item.setText(rowData);	
		table.select(table.indexOf(item));
	}
	
	public void addRow(String[] rowData, boolean selectRow) {
		
		TableItem item = new TableItem (table, SWT.NONE);
		item.setText(rowData);	
		
		if (selectRow)
			table.select(table.indexOf(item));
	}
	
	public void addRow(int intValue, int columnNumber) {
		
		addRows(Util.intToStr(intValue), columnNumber);
	}
	
	public void modifyRow(TableItem item, String[] rowData) {
		
		if (item == null) return;
		
		item.setText(rowData);
		table.select(table.indexOf(item));
	}
	
	/**
	 * 
	 * @param rowData
	 * @param keyColNumber
	 * @param dupMode
	 */
	public void addModifyRow(String[] rowData, int keyColNumber, int dupMode) {
		
		addModifyRow(null, rowData, keyColNumber, dupMode);
	}
	
	/**
	 * 
	 * @param item
	 * @param rowData
	 * @param keyColNumber
	 * @param dupMode
	 */	
	public void addModifyRow(TableItem item, String[] rowData, int keyColNumber, int dupMode) {
				
		
		String st = Util.get(rowData, keyColNumber - 1);
		
		TableItem item2 = findValue(st, keyColNumber);
		
		if (item2 != null) { // Already exists
		
			switch (dupMode) {
			
			case DUPLICATE_ALLOW:
				
				addRow(rowData);
				break;
				
			case DUPLICATE_REJECT:
				
				break;
				
			case DUPLICATE_REPLACE:
				
				modifyRow(item2, rowData);
				break;
			}
		}
		else
			addRow(rowData);
		
		// table.select(table.indexOf(item));
	}
	
	/**
	 * 
	 */
	public void unselect() {
		
		storeSelection();
		table.setSelection(-1);
	}

//	/**
//	 * If the current row exists, replaces its data with the given rowData, otherwise creates a new row and fills it up with rowData.
//	 * @param keyColNumber -- number (1-based) of the column which value should not be duplicated
//	 */
//	public void addModifyCurRow(String[] rowData, int keyColNumber, boolean allowReplace) {
//		
//		String st = 
//		//if (!valueExists(st, columnNumber));
//	}
//	
//	/**
//	 * If the given row exists, replaces its data with the given rowData, otherwise creates a new row and fills it up with rowData.
//	 * @param keyColNumber -- number (1-based) of the column which value should not be duplicated
//	 */
//	public void addModifyRow(TableItem item, String[] rowData, int keyColNumber, boolean allowReplace) {		
//		
//		
//	}

	/**
	 * @param values
	 * @param i
	 */
	public void addRows(String values, int columnNumber) {
		
		List<String> valList = ListUtil.stringAsList(values);
		
		for (String st : valList) {	

			if (Util.isEmpty(st)) continue;
			addModifyRow(new String[] {st}, columnNumber, DUPLICATE_REJECT);
		}
	}
	
	public TableItem findValue(String value, int columnNumber) {
		
		if (Util.isEmpty(value)) return null;
		
		for (TableItem item : table.getItems()) {
			
			if (item == null) continue;
			
			if (value.equalsIgnoreCase(item.getText(columnNumber - 1)))
				return item;
		}
		
		return null;
	}
	
	/**
	 * Returns a list of values in a given column.
	 * @param columnNumber 1-based column number
	 * @return a list of values
	 */
	public List<String> getColumnValues(int columnNumber) {
		List<String> list = new ArrayList<String>(); 
		if (!SWTUtil.checkColumnIndex(table, columnNumber - 1)) return list;
		
		for (TableItem item : table.getItems()) {		
			list.add(item.getText(columnNumber - 1));
		}
		return list;
	}
	
	public boolean valueExists(String value, int columnNumber) {
	
		return findValue(value, columnNumber) != null;
	}
		
    // String Comparator
	private int colIndex = 0;
    private boolean ascending = true;
    private boolean treatAsInt = false;
    
//	private Collator col = Collator.getInstance(Locale.getDefault());
	
    private Comparator<Object> strComparator = new Comparator<Object>()
    {
        public int compare(Object arg0, Object arg1) {

            TableItem t1 = (TableItem)arg0;
            TableItem t2 = (TableItem)arg1;

            if (treatAsInt) {
            	
            	int v1 = Util.strToInt(t1.getText(colIndex), 0);
                int v2 = Util.strToInt(t2.getText(colIndex), 0);
                
                return ((ascending && (v1 > v2)) || (!ascending && (v1 < v2)) ? 1: -1);
            }
            
            String v1 = t1.getText(colIndex);
            String v2 = t2.getText(colIndex);
                        
            return ((ascending && (v1.compareTo(v2) > 0)) || (!ascending && (v1.compareTo(v2) < 0)) ? 1: -1);
        }    
    };

    private String[] getData(TableItem t)
    {
        Table table = t.getParent();
        
        int colCount = table.getColumnCount();
        String [] s = new String[colCount];
        
        for (int i = 0; i < colCount;i++)
            s[i] = t.getText(i);
                
        return s;
        
    }
		
	public void sort(int sortColNum, boolean ascending, boolean treatAsInt) {
		
		if (sortColNum == 0) return;

		//ArrayList<TableItem> items = (ArrayList<TableItem>) Arrays.asList(table.getItems());
		
		TableItem[] items = table.getItems();
		this.colIndex = sortColNum - 1;
		this.ascending = ascending;
		this.treatAsInt = treatAsInt;
		
		storeSelection();		
		table.setRedraw(false);

		try{
			Arrays.sort(items, strComparator);
			
	        for (int i = 0; i < items.length; i++)
	        {   
	        	TableItem item = new TableItem(table, SWT.NONE, i);
	            item.setText(getData(items[i]));
	            
	            if (saveSelItem != null && saveSelItem.equals(items[i]))
	            		saveSelItem = item;
	            
	            items[i].dispose();
	        }
		} 
		finally {			
			
			table.setRedraw(true);
			restoreSelection();
		}        
	}
	public void sort(int sortColNum, boolean treatAsInt) {
		
		sort(sortColNum, true, treatAsInt);
	}
	
	public void sort(TableColumn sortColumn, boolean treatAsInt) {
		
		sort(SWTUtil.getColumnIndex(sortColumn) + 1, true, treatAsInt);
	}

	/**
	 * 
	 */
	public boolean removeSelected() {
		
		int index = table.getSelectionIndex();
		if (index == -1) return false;
		
		table.remove(index);
		
		if (index > table.getItemCount() - 1) index = table.getItemCount() - 1;
		if (index > -1)	table.select(index);
		
		return true;
	}

	public void storeSelection() {
		
		if (table.getSelection().length > 0)
			saveSelItem = table.getSelection()[0];
		else
			saveSelItem = null;
	}
	
	public void restoreSelection() {
		
		if (saveSelItem == null)
			table.select(-1);
		else
			table.setSelection(saveSelItem);
	}

	/**
	 * 
	 * @param item
	 * @param colNum
	 * @param value
	 */	
	public void setValue(TableItem item, int colNum, String value) {
		
		if (item == null) return;
		
		item.setText(colNum - 1, value);
	}
	
	public void setValue(int rowNum, int colNum, String value) {
		
		if (!SWTUtil.checkRowIndex(table, rowNum - 1)) return;
		
		TableItem item = table.getItem(rowNum - 1);
		if (item == null) return;
		
		item.setText(colNum - 1, value);
	}

	public int getNumRows() {
		
		return table.getItemCount();
	}
	
	public int getNumColumns() {
		
		return table.getColumnCount();
	}

	/**
	 * 
	 * @param rowNum 1-based
	 * @param colNum 1-based
	 * @return
	 */
	public String getValue(int rowNum, int colNum) {
		
		if (!SWTUtil.checkRowIndex(table, rowNum - 1)) return "";
		TableItem item = table.getItem(rowNum - 1);
		if (item == null) return "";
			
		return item.getText(colNum - 1);
	}

	/**
	 * 
	 */
	public void clear() {
		
		table.removeAll();
	}

	/**
	 * Sets column alignment.
	 * @param colNum 1-based column number
	 * @param alignment one of SWT.LEFT, SWT.CENTER, SWT.RIGHT
	 */
	public void setColumnAlignment(int colNum, int alignment) {
		TableColumn tc = table.getColumn(colNum - 1);
		tc.setAlignment(alignment);
	}

	
}
