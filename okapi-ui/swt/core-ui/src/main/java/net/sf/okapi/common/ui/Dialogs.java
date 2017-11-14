/*===========================================================================
  Copyright (C) 2008-2011 by the Okapi Framework contributors
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

package net.sf.okapi.common.ui;

import java.io.File;

import net.sf.okapi.common.Util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;

/**
 * Helpers methods to wrap commonly used dialog boxes.
 */
public class Dialogs {
	
	/**
	 * Browses for one or more files.
	 * @param parent Parent of the dialog.
	 * @param title Title of the dialog box.
	 * @param allowMultiple True if the user can select more than one file.
	 * @param root Directory where to start. Use null for default directory.
	 * @param filterNames List of filter names (tab-separated) to use. Can be null.
	 * Example: <code>"Rainbow Projects (*.rnb)\tAll Files (*.*)"</code>
	 * @param filterExtensions List of filter extensions (tab-separated) to use.
	 * Can be null. Must be the same number of items as for filterNames.
	 * Example: <code>"*.rnb\t*.*"</code>
	 * @return An array of strings, where each string is the full path for one 
	 * of the selected files. Returns null if the user canceled the command or
	 * an error occurred.
	 */
	static public String[] browseFilenames (Shell parent,
		String title,
		boolean allowMultiple,
		String root,
		String filterNames,
		String filterExtensions)
	{
		FileDialog dlg = new FileDialog(parent, SWT.OPEN | (allowMultiple ? SWT.MULTI : 0));
		dlg.setFilterPath(root); // Can be null
		if ( filterNames != null ) {
			String[] aNames = filterNames.split("\t", -2); //$NON-NLS-1$
			dlg.setFilterNames(aNames);
		}
		if ( filterExtensions != null ) {
			String[] aExts = filterExtensions.split("\t", -2); //$NON-NLS-1$
			dlg.setFilterExtensions(aExts);
		}
		dlg.setText(title);
		if ( dlg.open() == null ) return null;
		String[] aPaths = dlg.getFileNames();
		if (!Util.isEmpty(dlg.getFilterPath())) {
			for ( int i=0; i<aPaths.length; i++ ) {
				aPaths[i] = dlg.getFilterPath() + File.separator + aPaths[i];
			}
		}		
		return aPaths;
	}

	/**
	 * Browse to select a file to save to. If the selected file exists, the user is
	 * prompted to confirm overwrite.
	 * @param parent Parent of the dialog.
	 * @param title Title of the dialog box.
	 * @param root Directory where to start. Use null for default directory.
	 * @param fileName the default filename. Use null for none.
	 * @param filterNames List of filter names (tab-separated) to use. Can be null.
	 * @param filterExtensions List of filter extensions (tab-separated) to use.
	 * Can be null.
	 * Must be the same number of items as for filterNames.
	 * @return The full path of the selected file or null if the user canceled
	 * the command or an error occurred.
	 */
	static public String browseFilenamesForSave (Shell parent,
			String title,
			String root,
			String fileName,
			String filterNames,
			String filterExtensions)
		{
			String[] aExts = null;
			String[] aNames = null;
			if ( filterExtensions != null ) aExts = filterExtensions.split("\t", -2); //$NON-NLS-1$
			if ( filterNames != null ) aNames = filterNames.split("\t", -2); //$NON-NLS-1$
			String path = null;
			// Call the SaveAs dialog until we are done
			while ( true ) {
				FileDialog dlg = new FileDialog(parent, SWT.SAVE);
				dlg.setFilterPath(root); // Can be null
				dlg.setFileName(fileName); // Can be null
				if ( filterNames != null ) dlg.setFilterNames(aNames);
				if ( filterExtensions != null ) dlg.setFilterExtensions(aExts);
				dlg.setText(title);
				path = dlg.open();
				if ( path == null ) return null; // Canceled by user
				// Else: Confirm overwriting if needed
				File file = new File(path);
				if ( file.exists() ) {
					// Asks for confirmation
					MessageBox mb = new MessageBox(dlg.getParent(), SWT.ICON_WARNING
						| SWT.YES | SWT.NO | SWT.CANCEL);
					mb.setText(title);
					mb.setMessage(path + Res.getString("dialogs.browseForSave")); //$NON-NLS-1$
					int result = mb.open();
					if ( result == SWT.YES ) return path;
					if ( result == SWT.CANCEL ) return null;
					// If NO: ask the path again
				}
				else return path;
			}
		}
	
	/**
	 * Centers a given window on a given window.
	 * @param target The window to center.
	 * @param centerOn The window where to center target.
	 * If p_CenterOn is null, the window is centered on the screen. If the x or y coordinates are
	 * off screen after centering, they are forced to 0.
	 */
	static public void centerWindow (Shell target,
		Shell centerOn)
	{
		Rectangle parentRect;
		Rectangle winRect = target.getBounds();
		if ( centerOn == null ) {
			// Handle the case of multiple monitors
			Monitor monitor = target.getDisplay().getPrimaryMonitor();
			parentRect = monitor.getClientArea();
		}
		else parentRect = centerOn.getBounds();
		
		// Compute the position and set the window
		int x = parentRect.x + (parentRect.width - winRect.width) / 2;
		int y = parentRect.y + (parentRect.height - winRect.height) / 2;
		// Make sure it's at least set to (0,0)
		target.setLocation(((x<0) ? 0 : x), ((y<0)? 0 : y));
	}

	/**
	 * Moves a given window to the south-east corner of a given parent.
	 * @param target the window to move.
	 * @param parent the parent, or null to use the whole screen.
	 */
	static public void placeWindowInSECorner (Shell target,
		Shell parent)
	{
		Rectangle parentRect;
		Rectangle winRect = target.getBounds();
		if ( parent == null ) {
			// Handle the case of multiple monitors
			Monitor monitor = target.getDisplay().getPrimaryMonitor();
			parentRect = monitor.getClientArea();
		}
		else {
			parentRect = parent.getBounds();
		}
		
		// Compute the position and set the window
		int x = parentRect.x + (parentRect.width - winRect.width);
		int y = parentRect.y + (parentRect.height - winRect.height);
		// Make sure it's at least set to (0,0)
		target.setLocation(((x<0) ? 0 : x), ((y<0)? 0 : y));
	}

	/**
	 * Shows a simple error dialog box.
	 * @param shell the parent shell.
	 * @param message the message to display.
	 * @param details additional details information (may be null).
	 */
	static public void showError (Shell shell,
		String message,
		String details)
	{
		MessageBox dlg = new MessageBox(shell, SWT.ICON_ERROR);
		dlg.setMessage((message==null) ? "The error message is null." : message); //$NON-NLS-1$
		dlg.setText(Res.getString("dialogs.errorCaption")); //$NON-NLS-1$
		dlg.open();
	}
	
	/**
	 * Shows a simple warning dialog box.
	 * @param shell the parent shell.
	 * @param message the message to display.
	 * @param details additional details information (may be null).
	 */
	static public void showWarning (Shell shell,
		String message,
		String details)
	{
		MessageBox dlg = new MessageBox(shell, SWT.ICON_WARNING);
		dlg.setMessage((message==null) ? "The warning is null." : message); //$NON-NLS-1$
		dlg.setText(Res.getString("dialogs.warningCaption")); //$NON-NLS-1$
		dlg.open();
	}
		
}
