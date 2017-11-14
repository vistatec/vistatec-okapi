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
import java.util.List;

import net.sf.okapi.common.IHelp;
import net.sf.okapi.common.Util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

/**
 * 
 * 
 * @version 0.1, 13.06.2009
 */

public class SWTUtil {

	public static final String GET_CAPTION = "SWT_GET_CAPTION";
	private static Listener listener = null;
	private static Event lastEvent = null;
	
// Enabling/disabling
	
	/**
	 * Sets the enabled state for all of a Composite's child Controls,
	 * including those of nested Composites.
	 *
	 * @param container
	 *            The Composite whose children are to have their
	 *            enabled state set.
	 * @param enabled
	 *            True if the Controls are to be enabled, false if
	 *            disabled.
	 * @param excludedControls
	 *            An optional array of Controls that should be excluded from
	 *            having their enabled state set. This is useful, for example,
	 *            to have a single widget enable/disable all of its siblings
	 *            other than itself.
	 */
	public static void setAllEnabled(Composite container, boolean enabled, boolean clearSelection,
	        Control... excludedControls) {

		List<Control> excludes = null;
		
		if (excludedControls != null)
			excludes = Arrays.asList(excludedControls);
		else {
			excludes = new ArrayList<Control>();
			excludes.add(container);
		}
									
	    Control[] children = container.getChildren();
	    for (Control aChild : children) {
	        if (excludes.contains(aChild)) {
	        	aChild.setEnabled(!enabled);
	        }
	        else {
	        	if (clearSelection && aChild instanceof Button && !enabled)
	        		((Button) aChild).setSelection(false); // Clear radio and check-boxes
	        			            
	            if (aChild instanceof Composite && ((Composite) aChild).getChildren().length > 0) {
	                setAllEnabled((Composite) aChild, enabled, clearSelection, excludedControls);
	            }
	            else
	            	aChild.setEnabled(enabled);
	        }
	    }
	}
	
	public static void setAllEnabled(Composite container, boolean enabled, boolean clearSelection) {

		setAllEnabled(container, enabled, clearSelection, (Control[])null);
	}
	
	public static void setAllEnabled(Composite container, boolean enabled) {

		setAllEnabled(container, enabled, false, (Control[])null);
	}
	
	public static void setEnabled(Control control, boolean enabled) {
		// Clears radio and check-boxes when they are disabled

		if (control == null) return;
		
		if (control instanceof Composite)
			setAllEnabled((Composite) control, enabled);
		
		if (control instanceof Button && !enabled)
    		((Button) control).setSelection(false); 
		
		control.setEnabled(enabled);
	}
	
	public static boolean getEnabled(Control control) {
		
		if (control == null) return false;
		
		return control.getEnabled();		
	}
	
	public static boolean getDisabled(Control control) {
				
		return !getEnabled(control);		
	}
	
// Visibility	
	
	public static void setVisible(Control control, boolean visible) {

		if (control == null) return;
		
		control.setVisible(visible);
	}
	
	public static boolean getVisible(Control control) {
		
		if (control == null) return false;
		
		return control.getVisible();		
	}
	
// Selection / unselection

	public static boolean getSelected(Control control) {
	
		if (!(control instanceof Button)) return false;
		
		return ((Button) control).getSelection();		
	}
	
	public static boolean getNotSelected(Control control) {
		
		return !getSelected(control);		
	}
	
	public static void setSelected(Control control, boolean selected) {
		
		if (!(control instanceof Button)) return;
		
		if (selected)
			unselectAll(control.getParent()); // SWT feature: several radio-buttons of the same group can get selected at the same time
		
		((Button) control).setSelection(selected);
	}
	
	public static void setGrayed(Control control, boolean grayed) {
		
		if (!(control instanceof Button)) return;
		
		((Button) control).setGrayed(grayed);
	}
	
	public static int getSelection(Control control) {
		
		if (!(control instanceof org.eclipse.swt.widgets.List)) return -1;
		
		return ((org.eclipse.swt.widgets.List) control).getSelectionIndex();
	}
	
	public static int getNumItems(Control control) {
		
		if (!(control instanceof org.eclipse.swt.widgets.List)) return 0;
		
		return ((org.eclipse.swt.widgets.List) control).getItemCount();
	}
	
	
// Input query
	public static int inputQuery(Shell parent, String caption, String prompt, int initialValue, int minValue, int MaxValue,
			IHelp help) {

		InputQueryDialog dlg = new InputQueryDialog(false);
		Integer[] intArray = new Integer[] {initialValue, minValue, MaxValue};
		boolean res = dlg.run(parent, InputQueryPageInt.class, caption, prompt, intArray, help);
		return (res)? intArray[0] : initialValue;
	}
	
	public static String inputQuery(Shell parent, String caption, String prompt, String initialValue, IHelp help) {
		
		InputQueryDialog dlg = new InputQueryDialog(false);
		String[] strArray = new String[] {initialValue};
		boolean res = dlg.run(parent, InputQueryPageString.class, caption, prompt, strArray, help);
		
		return (res)? strArray[0] : initialValue;
	}
	
	public static Object inputQuery(boolean sizeable, Class<? extends Composite> pageClass, Shell parent, String caption, Object initialData, IHelp help) {

		InputQueryDialog dlg = new InputQueryDialog(sizeable);
		boolean res = dlg.run(parent, pageClass, caption, null, initialData, help);
		
		return (res)? dlg.getResult() : null;
	}
	
	public static <T> T inputQuery(boolean sizeable, Class<T> resClass, Class<? extends Composite> pageClass, Shell parent, String caption, T initialData, IHelp help) {

		InputQueryDialog dlg = new InputQueryDialog(sizeable);
		boolean res = dlg.run(parent, pageClass, caption, null, initialData, help);
		
		return resClass.cast((res)? dlg.getResult() : null);
	}
	
	public static Object inputQuery(Class<? extends Composite> pageClass, Shell parent, String caption, Object initialData, IHelp help) {

		InputQueryDialog dlg = new InputQueryDialog(false);
		boolean res = dlg.run(parent, pageClass, caption, null, initialData, help);
		
		return (res)? dlg.getResult() : null;
	}
	
	public static <T> T inputQuery(Class<T> resClass, Class<? extends Composite> pageClass, Shell parent, String caption, T initialData, IHelp help) {

		InputQueryDialog dlg = new InputQueryDialog(false);
		boolean res = dlg.run(parent, pageClass, caption, null, initialData, help);
		
		return resClass.cast((res)? dlg.getResult() : null);
	}

// Radio group
	
	public static Button getRadioGroupSelection(Composite container) {
		
		if (container == null) return null;
		
	    for (Control aChild : container.getChildren())
	    	if (aChild instanceof Composite) {
                return getRadioGroupSelection((Composite) aChild);
            }
	    	else
	    	if (aChild instanceof Button &&
	    			Util.checkFlag(((Button) aChild).getStyle(), SWT.RADIO) &&
	    			((Button) aChild).getSelection())
	    		return (Button) aChild;
	    
		return null;
	}
	
	public static void unselectAll(Composite container) {
		
		if (container == null) return;
		
	    for (Control aChild : container.getChildren())
	    	if (aChild instanceof Composite) {
                unselectAll((Composite) aChild);
            }
	    	else
	    	if (aChild instanceof Button &&
	    			Util.checkFlag(((Button) aChild).getStyle(), SWT.RADIO))
	    		((Button) aChild).setSelection(false);
	    
		return;
	}

	public static void setRadioGroupSelection(Composite container, String selCaption) {
		
		if (container == null) return;
		
	    for (Control aChild : container.getChildren())
	    	if (aChild instanceof Composite) {
                setRadioGroupSelection((Composite) aChild, selCaption);
            }
	    	else
	    	if (aChild instanceof Button &&	Util.checkFlag(((Button) aChild).getStyle(), SWT.RADIO))
	    		((Button) aChild).setSelection(((Button) aChild).getText().equalsIgnoreCase(selCaption));
	}

// Table	
	
	public static String [] getText(TableItem item) {
		
		if (item == null) return null;
		
		Table table = item.getParent();
		if (table == null) return null;
		
		int numCol = table.getColumnCount();
		
		String res [] = new String [numCol];
		
		for (int i = 0; i < numCol; i++)
			res[i] = item.getText(i);
		
		return res;
	}
	
	public static boolean checkRowIndex(Table table, int rowIndex) {
		
		if (table == null) return false;
		
		return rowIndex >= 0 && rowIndex < table.getItemCount();
	}
	
	public static boolean checkColumnIndex(Table table, int colIndex) {
		
		if (table == null) return false;
		
		return colIndex >= 0 && colIndex < table.getColumnCount();
	}
	
	public static boolean checkListIndex(org.eclipse.swt.widgets.List list, int index) {
		
		if (list == null) return false;
		
		return index >= 0 && index < list.getItemCount();
	}
	
	public static int getColumnMaxValue(Table table, int colIndex) {
				
		if (table == null) return 0;
		if (!checkColumnIndex(table, colIndex)) return 0;
		
		int res = 0;
		
		for (TableItem item : table.getItems()) {
			
			String st = item.getText(colIndex);
			int val = Util.strToInt(st, 0); 
			if (val > res) res = val; 
		}
		
		return res;
	}

	public static int getColumnIndex(TableColumn col) {
		
		if (col == null) return -1;
		
		Table table = col.getParent();
		if (table == null) return -1;
	
		TableColumn[] cols = table.getColumns();
		if (cols == null) return -1;
		
		return Arrays.asList(cols).indexOf(col);
	}
	
// Search	
	
	public static Control findControl(Composite container, String controlName) {

		if (container == null) return null;
		if (Util.isEmpty(controlName)) return null;
		
	    for (Control aChild : container.getChildren()) {
	    	
	    	String name = (String) aChild.getData("name");
    		
    		if (!Util.isEmpty(name) && name.equalsIgnoreCase(controlName))
	    		return aChild;
    		
    		if (aChild instanceof Composite) {
	    		
                Control res = findControl((Composite) aChild, controlName);
                if (res == null) continue;
                	
                return res;
            }    		
	    }
	    
		return null;
	}
	
// State correlation (all 16 options)	
	
	public static void enableIfSelected(Control target, Control source) {
		
		if (getSelected(source))
			setEnabled(target, true);		
	}
	
	public static void enableIfNotSelected(Control target, Control source) {
		
		if (!getSelected(source))
			setEnabled(target, true);
	}
	
	public static void disableIfSelected(Control target, Control source) {
		
		if (getSelected(source))
			setEnabled(target, false);
	}
	
	public static void disableIfNotSelected(Control target, Control source) {
	
		if (!getSelected(source))
			setEnabled(target, false);
	}

	public static void selectIfSelected(Control target, Control source) {
		
		if (getSelected(source))
			setSelected(target, true);
	}
	
	public static void selectIfNotSelected(Control target, Control source) {
		
		if (!getSelected(source))
			setSelected(target, true);
	}
	
	public static void unselectIfSelected(Control target, Control source) {
		
		if (getSelected(source))
			setSelected(target, false);
	}
	
	public static void unselectIfNotSelected(Control target, Control source) {
		
		if (!getSelected(source))
			setSelected(target, false);
	}
	
	public static void selectIfEnabled(Control target, Control source) {
		
		if (getEnabled(source))
			setSelected(target, true);
	}
	
	public static void selectIfDisabled(Control target, Control source) {
		
		if (!getEnabled(source))
			setSelected(target, true);
	}
	
	public static void unselectIfEnabled(Control target, Control source) {
		
		if (getEnabled(source))
			setSelected(target, false);
	}
	
	public static void unselectIfDisabled(Control target, Control source) {
		
		if (!getEnabled(source))
			setSelected(target, false);
	}
			
	public static void enableIfEnabled(Control target, Control source) {
		
		if (getEnabled(source))
			setEnabled(target, true);
	}
	
	public static void enableIfDisabled(Control target, Control source) {
		
		if (!getEnabled(source))
			setEnabled(target, true);
	}
	
	public static void disableIfEnabled(Control target, Control source) {
		
		if (getEnabled(source))
			setEnabled(target, false);
	}
	
	public static void disableIfDisabled(Control target, Control source) {
		
		if (!getEnabled(source))
			setEnabled(target, false);
	}

	/**
	 * 	Sets text of the given control.
	 * @param control the control to set text
	 * @param text the text to set <p>
	 * <li>text = null - don't change existing control's text
	 * <li>text = "" - set empty text
	 */
	public static void setText(Control control, String text) {
		
		if (control == null) return;
		if (text == null) return;
		
		if (control instanceof Text)		
			((Text) control).setText(text);
		
		if (control instanceof Label)		
			((Label) control).setText(text);
		
		if (control instanceof Button)		
			((Button) control).setText(text);
	}
	
	protected static void addSpeaker(Composite page, Control control) {

		addSpeaker(page, control, SWT.Selection);	
	}
	
	public static Event getEvent() {
		
		return lastEvent;
	}
	
	public static int getEventType() {
		
		if (lastEvent == null) return SWT.None;
		
		return lastEvent.type;
	}
	
	public static void addSpeaker(Composite page, Control control, int eventType) {
	
		class LocalListener implements Listener {

			public void handleEvent(Event event) {
		
				lastEvent = event;
				Widget speaker = event.widget;
				if (speaker == null) return;
				
				IDialogPage page = (IDialogPage) speaker.getData("page");
				if (page == null) return;
				
				page.interop(speaker);
			}			
		}

		if (control == null) return;
		if (control.isDisposed()) return;
		if (!(page instanceof IDialogPage)) return;

		if (listener == null)
			listener = new LocalListener();
		
		control.setData("page", page);
		control.addListener(eventType, listener);
	}
	
	public static void addSpeaker(Composite page, String controlName) {

		addSpeaker(page, findControl(page, controlName));
	}
	
	public static void addSpeakers(Composite page, Control... controls) {
	
		for (Control speaker : Arrays.asList(controls))
			addSpeaker(page, speaker);
	}

	public static boolean checkControl(Control control) {
		
		return (control != null && !control.isDisposed());
	}
	
}
