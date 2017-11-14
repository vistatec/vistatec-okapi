/*===========================================================================
  Copyright (C) 2009 by the Okapi Framework contributors
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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

public abstract class AbstractListTab extends Composite implements IDialogPage {
	protected Label listDescr;
	protected List list;
	protected Text itemDescr;
	protected Button add;
	protected Button modify;
	protected Button remove;
	protected Button up;
	protected Button down;

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public AbstractListTab(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(2, false));

		listDescr = new Label(this, SWT.NONE);
		listDescr.setData("name", "listDescr");
		new Label(this, SWT.NONE);
		
		if (!getDisplayListDescr())
			listDescr.dispose();
				
		list = new List(this, SWT.BORDER | SWT.V_SCROLL);
		list.setItems(new String[] {});
		list.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 10));
		list.setData("name", "list");
		
		add = new Button(this, SWT.NONE);
		add.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		add.setData("name", "add");
		add.setText("Add...");
		
		modify = new Button(this, SWT.NONE);
		modify.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		modify.setData("name", "modify");
		modify.setText("Modify...");
		
		if (!getDisplayModify())
			modify.dispose();
		
		remove = new Button(this, SWT.NONE);
		remove.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		remove.setData("name", "remove");
		remove.setText("Remove");
		new Label(this, SWT.NONE);
		
		up = new Button(this, SWT.NONE);
		up.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		up.setData("name", "up");
		up.setText("Move Up");		
		
		down = new Button(this, SWT.NONE);
		GridData gridData_1 = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gridData_1.widthHint = 90;
		down.setLayoutData(gridData_1);
		down.setData("name", "down");
		down.setText("Move Down");
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
			
		itemDescr = new Text(this, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		gridData.heightHint = 50;
		gridData.widthHint = 500;
		itemDescr.setLayoutData(gridData);
		itemDescr.setData("name", "itemDescr");
		itemDescr.setVisible(true);
		
		if (!getDisplayItemDescr())
			itemDescr.dispose();
		
		// Configure interop
		SWTUtil.addSpeaker(this, listDescr);
		SWTUtil.addSpeaker(this, list, SWT.MouseDoubleClick);
		SWTUtil.addSpeaker(this, list, SWT.Selection);
		SWTUtil.addSpeaker(this, itemDescr);
		SWTUtil.addSpeaker(this, add);
		SWTUtil.addSpeaker(this, modify);
		SWTUtil.addSpeaker(this, remove);
		SWTUtil.addSpeaker(this, up);
		SWTUtil.addSpeaker(this, down);
	}
	
	protected void selectListItem(int index) {
		
		if (SWTUtil.checkListIndex(list, index)) {
			
			list.setSelection(index);
			SWTUtil.setText(itemDescr, getItemDescription(index));
		}
		else
			SWTUtil.setText(itemDescr, "");
		
		list.setFocus();
	}

	protected boolean getDisplayListDescr() {
		
		return true;
	}
	
	protected boolean getDisplayItemDescr() {
		
		return true;
	}
	
	protected boolean getDisplayModify() {
		
		return true;
	}
	
	protected String getItemDescription(int index) {
		
		return "";
	}

	protected void actionAdd(int afterIndex) {
		
	};
	
	protected void actionModify(int itemIndex) {
		
	};	
	
	protected void actionUp(int itemIndex) {
		
	};
	
	protected void actionDown(int itemIndex) {
		
	};
	
	public void interop(Widget speaker) {
		
		SWTUtil.setEnabled(add, true);
		
		int index = SWTUtil.getSelection(list);
		
		SWTUtil.setEnabled(modify, index != -1);
		SWTUtil.setEnabled(remove, index != -1);
		SWTUtil.setEnabled(up, index > 0);
		SWTUtil.setEnabled(down, index > -1 && index < SWTUtil.getNumItems(list) - 1);
		
		if (speaker == add) {
			
			actionAdd(index);			
			selectListItem(list.getItemCount() - 1);
			interop(null);
			//selectListItem(0);
		}
					
		else if (speaker == list && SWTUtil.getEventType() == SWT.MouseDoubleClick) {
			
			if (SWTUtil.getVisible(modify))
				actionModify(index);
			else
				actionAdd(index);
		}
		
		else if (speaker == list && SWTUtil.getEventType() == SWT.Selection) {
			
			SWTUtil.setText(itemDescr, getItemDescription(index));
		}
		
		else if (speaker == modify)
			actionModify(index);
		
		else if (speaker == remove) {
			
			list.remove(index);
			
			if (index > list.getItemCount() - 1) index = list.getItemCount() - 1;
			selectListItem(index);	
			interop(null);
		}
		
		else if (speaker == up)
			actionUp(index);
		
		else if (speaker == down)
			actionDown(index);
				
	}	
}
