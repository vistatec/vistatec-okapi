package net.sf.okapi.applications.serval;

import net.sf.okapi.common.query.IQuery;
import net.sf.okapi.common.ui.ClosePanel;
import net.sf.okapi.common.ui.Dialogs;
import net.sf.okapi.common.ui.UIUtil;
import net.sf.okapi.lib.translation.QueryManager;
import net.sf.okapi.lib.translation.ResourceItem;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

public class QueryManagerForm {

	private Shell shell;
	private Table table;
	private Button btRemove;
	private Button btMoveUp;
	private Button btMoveDown;
	private QueryManagerTableModel model;
	private QueryManager qm;
	private ClosePanel pnlActions;
	
	public QueryManagerForm (Shell parent,
		String captionText,
		QueryManager queryMgt)
	{
		qm = queryMgt;
		
		shell = new Shell(parent, SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.APPLICATION_MODAL);
		if ( captionText != null ) shell.setText(captionText);
		UIUtil.inheritIcon(shell, parent);
		shell.setLayout(new GridLayout(1, false));
		
		Group grpTmp = new Group(shell, SWT.NONE);
		grpTmp.setText("Translation resources");
		grpTmp.setLayout(new GridLayout(6, false));
		grpTmp.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		new Label(shell, SWT.NONE);
		
		table = new Table(grpTmp, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION | SWT.CHECK);
		GridData gdTmp = new GridData(GridData.FILL_BOTH);
		gdTmp.horizontalSpan = 6;
		table.setLayoutData(gdTmp);
		table.setHeaderVisible(true);
		table.addControlListener(new ControlAdapter() {
		    public void controlResized(ControlEvent e) {
		    	Table table = (Table)e.getSource();
		    	Rectangle rect = table.getClientArea();
				int nPart = (int)(rect.width / 100);
				int nRemain = (int)(rect.width % 100);
				table.getColumn(0).setWidth(10*nPart);
				table.getColumn(1).setWidth(15*nPart);
				table.getColumn(2).setWidth(15*nPart);
				table.getColumn(3).setWidth(30*nPart);
				table.getColumn(4).setWidth((30*nPart)+nRemain);
		    }
		});
		table.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if ( event.detail == SWT.CHECK ) updateEnabling((TableItem)event.item);
            }
		});
		
		model = new QueryManagerTableModel();
		model.linkTable(table);

		int stdWidth = 80;
		
		Button btAdd = UIUtil.createGridButton(grpTmp, SWT.PUSH, "Add...", stdWidth, 1);
		btAdd.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				editResource(true);
			}
		});

		Button btEdit = UIUtil.createGridButton(grpTmp, SWT.PUSH, "Edit...", stdWidth, 1);
		btEdit.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				editResource(false);
			}
		});
		
		btRemove = UIUtil.createGridButton(grpTmp, SWT.PUSH, "Remove", stdWidth, 1);
		btRemove.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				remove();
			}
		});
		
		btMoveUp = UIUtil.createGridButton(grpTmp, SWT.PUSH, "Move Up", stdWidth, 1);
		
		btMoveDown = UIUtil.createGridButton(grpTmp, SWT.PUSH, "Move Down", stdWidth, 1);
		
		// Load and Save 

		Composite cmpTmp = new Composite(shell, SWT.NONE);
		cmpTmp.setLayout(new GridLayout(2, false));
		gdTmp = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		cmpTmp.setLayoutData(gdTmp);
		
		Button btSave = UIUtil.createGridButton(cmpTmp, SWT.PUSH, "Save...", stdWidth, 1);
		btSave.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				save();
			}
		});
		
		Button btLoad = UIUtil.createGridButton(cmpTmp, SWT.PUSH, "Load...", stdWidth, 1);
		btLoad.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				load();
			}
		});

		SelectionAdapter CloseActions = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if ( e.widget.getData().equals("h") ) {
					//TODO: UIUtil.start(help);
					return;
				}
				if ( e.widget.getData().equals("c") ) {
					shell.close();
				}
			};
		};
		pnlActions = new ClosePanel(shell, SWT.NONE, CloseActions, true);
		pnlActions.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		shell.setDefaultButton(pnlActions.btClose);

		shell.pack();
		shell.setMinimumSize(shell.getSize());
		Point startSize = shell.getMinimumSize();
		if ( startSize.x < 750 ) startSize.x = 750; 
		if ( startSize.y < 300 ) startSize.y = 300; 
		shell.setSize(startSize);
		Dialogs.centerWindow(shell, parent);
	}

	private void updateEnabling (TableItem item) {
		if ( item == null ) return;
		ResourceItem ri = qm.getResource((Integer)item.getData());
		ri.enabled = item.getChecked();
	}
	
	private void updateButtons () {
		boolean enabled = (table.getItemCount() > 0);
		btRemove.setEnabled(enabled);
		btMoveUp.setEnabled(enabled);
		btMoveDown.setEnabled(enabled);
	}
	
	private void save () {
		//qm.save();
	}
	
	private void load () {
		
	}
	
	private void remove () {
		try {
			// Get the selected resource
			int n = table.getSelectionIndex();
			if ( n == -1 ) return;
			qm.remove((Integer)table.getSelection()[0].getData());
			table.remove(n);
			model.updateTable(qm);
			updateButtons();
		}		
		catch ( Throwable e ) {
			Dialogs.showError(shell, e.getLocalizedMessage(), null);
		}
	}
	
	private void editResource (boolean createNew) {
		try {
			if ( createNew ) {
				// Selection the type of resources and create it
				SelectionForm selectionDlg = new SelectionForm(shell);
				IQuery q = selectionDlg.showDialog();
				if ( q == null ) return; // Cancel
				// Set the initial parameters
				q.setLanguages(qm.getSourceLanguage(), qm.getTargetLanguage());
				int id = qm.addResource(q, q.getName());
				// Add the new resource to the list and select it
				model.addToTable(qm, id);
				table.setSelection(table.getItemCount()-1);
				updateButtons();
			}
			
			// Get the selected resource
			int n = table.getSelectionIndex();
			if ( n == -1 ) return;
			ResourceItem ri = qm.getResource((Integer)table.getSelection()[0].getData());
			
			// Edit its options
			ConnectorOptionsForm optionsDlg = new ConnectorOptionsForm(shell);
			if ( optionsDlg.showDialog(ri) ) {
				ri.query.open();
				model.updateTable(qm);
			}
		}
		catch ( Throwable e ) {
			Dialogs.showError(shell, e.getLocalizedMessage(), null);
		}
	}

	public void showDialog () {
		model.updateTable(qm);
		updateButtons();
		shell.open();
		while ( !shell.isDisposed() ) {
			if ( !shell.getDisplay().readAndDispatch() )
				shell.getDisplay().sleep();
		}
	}
}
