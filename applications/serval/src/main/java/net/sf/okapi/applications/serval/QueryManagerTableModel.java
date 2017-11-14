package net.sf.okapi.applications.serval;

import java.util.Map;

import net.sf.okapi.lib.translation.QueryManager;
import net.sf.okapi.lib.translation.ResourceItem;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class QueryManagerTableModel {

	private Table table;
	
	void linkTable (Table newTable) {
		table = newTable;
		TableColumn col = new TableColumn(table, SWT.NONE);
		col.setText("ID");
		col = new TableColumn(table, SWT.NONE);
		col.setText("Name");
		col = new TableColumn(table, SWT.NONE);
		col.setText("Languages");
		col = new TableColumn(table, SWT.NONE);
		col.setText("Connection");
		col = new TableColumn(table, SWT.NONE);
		col.setText("Class");
	}

	void updateTable (QueryManager qm) {
		table.removeAll();
		Map<Integer, ResourceItem> list = qm.getResources();
		ResourceItem ri;
		for ( int id : list.keySet() ) {
			ri = list.get(id);
			TableItem item = new TableItem(table, SWT.NONE);
			item.setData(id);
			item.setChecked(ri.enabled);
			item.setText(0, String.format("%d", id));
			item.setText(1, ((ri.name==null) ? "" : ri.name));
			item.setText(2, String.format("%s --> %s",
				ri.query.getSourceLanguage(), ri.query.getTargetLanguage()));
			item.setText(3, "TODO");
			item.setText(4, ri.query.getClass().getCanonicalName());
		}
		if ( table.getItemCount() > 0 ) {
			table.setSelection(0);
		}
	}
	
	void addToTable (QueryManager qm, int id) {
		TableItem item = new TableItem(table, SWT.NONE);
		ResourceItem ri = qm.getResource(id);
		item.setData(id);
		item.setChecked(ri.enabled);
		item.setText(0, String.format("%d", id));
		item.setText(1, ((ri.name==null) ? "" : ri.name));
		item.setText(2, String.format("%s --> %s",
			ri.query.getSourceLanguage(), ri.query.getTargetLanguage()));
		item.setText(3, "TODO");
		item.setText(4, ri.query.getClass().getCanonicalName());
	}
}
