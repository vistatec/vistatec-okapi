package net.sf.okapi.applications.serval;

import net.sf.okapi.common.filterwriter.GenericContent;
import net.sf.okapi.common.query.QueryResult;
import net.sf.okapi.lib.translation.QueryManager;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class TableModel {

	private Table table;
	private GenericContent fmt;
	
	public TableModel () {
		fmt = new GenericContent();
	}
	
	void linkTable (Table newTable) {
		table = newTable;
		TableColumn col = new TableColumn(table, SWT.NONE);
		col.setText("Score");
		col = new TableColumn(table, SWT.NONE);
		col.setText("Origin");
		col = new TableColumn(table, SWT.NONE);
		col.setText("Source");
		col = new TableColumn(table, SWT.NONE);
		col.setText("Target");
	}

	void clearTable () {
		table.removeAll();
	}
	
	void updateTable (QueryManager qm) {
		table.removeAll();
		QueryResult qr;
		while ( qm.hasNext() ) {
			qr = qm.next();
			String mt = (qr.fromMT() ? "MT! " : "");
			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(0, String.format("%d", qr.getCombinedScore()));
			item.setText(1, (qr.origin==null ? mt : mt+qr.origin));
			item.setText(2, fmt.setContent(qr.source).toString());
			item.setText(3, fmt.setContent(qr.target).toString());
		}
		if ( table.getItemCount() > 0 ) {
			table.setSelection(0);
		}
	}
}
