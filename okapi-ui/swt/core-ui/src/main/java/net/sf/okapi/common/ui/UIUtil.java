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

package net.sf.okapi.common.ui;

import java.io.IOException;

import net.sf.okapi.common.exceptions.OkapiException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class UIUtil {

	public static final int BUTTON_DEFAULT_WIDTH = 80;

	public static final int PFTYPE_WIN      = 0;
	public static final int PFTYPE_MAC      = 1;
	public static final int PFTYPE_UNIX     = 2;

	/**
	 * Starts a program or a command.
	 * @param command Command or program to launch. This can also be a 
	 * URL string (to open a browser), etc.
	 */
	static public void start (String command) {
		Program.launch(command);
	}

	// Use Util.openURL() instead: works better across platforms.
//	/**
//	 * Finds the browser for this system and invoke it with a given URL.
//	 * @param url URL of the resource to open with its associated program (can be a file) 
//	 */
//	static public void start (URL url) {
//		if ( url == null ) return;
//		Program p = Program.findProgram(".html");
//		if ( p!=null ) p.execute(url.toString());
//	}
	
	/**
	 * Executes a given program with a given parameter.
	 * @param program The program to execute.
	 * @param parameter The parameter of the program.
	 */
	static public void execute (String program,
		String parameter)
	{
		try {
			Runtime rt = Runtime.getRuntime();
			String[] command = new String[2];
			command[0] = program;
			command[1] = parameter;
			rt.exec(command);
		}
		catch ( IOException e ) {
			throw new OkapiException(e);
		}
	}
	
	/**
	 * Gets the type of platform the application is running on.
	 * @return -1 if the type could not be detected. Otherwise one of the PFTYPE_* values.
	 */
	public static int getPlatformType () {
		if ( "win32".equals(SWT.getPlatform()) ) return PFTYPE_WIN;
		if ( "gtk".equals(SWT.getPlatform()) ) return PFTYPE_UNIX;
		if ( "cocoa".equals(SWT.getPlatform()) || "carbon".equals(SWT.getPlatform()) ) return PFTYPE_MAC;
		if ( "motif".equals(SWT.getPlatform()) ) return PFTYPE_UNIX;
		return -1; // Unknown
	}

	/**
	 * Creates a new button on a GridLayout layout.
	 * @param parent Composite parent widget.
	 * @param style Style of the button.
	 * @param label Text of the button.
	 * @param width Width of the button. (Use -1 to not set it)
	 * @param horizontalSpan Span of the button (Use -1 to not set it)
	 * @return The new button.
	 */
	public static Button createGridButton (Composite parent,
		int style,
		String label,
		int width,
		int horizontalSpan)
	{
		Button newButton = new Button(parent, style);
		newButton.setText(label);
		GridData gdTmp = new GridData();
		if ( horizontalSpan > 0 ) gdTmp.horizontalSpan = horizontalSpan;
		newButton.setLayoutData(gdTmp);
		if ( width > -1 ) {
			ensureWidth(newButton, width);
		}
		return newButton;
	}
	
	/**
	 * Sets the icon of a dialog to the image(s) inherited from the parent
	 * shell. If the parent has several images available, they are passed to the
	 * dialog, otherwise the current result of parent.getImage() is used. 
	 * @param dialog The dialog where to set the icon.
	 * @param parent The parent to inherit from (if null nothing is done).
	 */
	public static void inheritIcon (Shell dialog,
		Shell parent)
	{
		if ( parent == null ) return;
		Image[] list = parent.getImages();
		if (( list == null ) || ( list.length < 2 )) dialog.setImage(parent.getImage()); 
		else dialog.setImages(list);
	}

	/**
	 * Converts a string representation of 4 integers into a Rectangle object.
	 * @param data The string to convert ("X,Y;W;H" or "X,Y,W,H" or "X Y W H").
	 * @return A new rectangle or null if an error occurred. 
	 */
	public static Rectangle StringToRectangle (String data) {
		try {
			if ( data == null ) return null;
			String[] parts = data.split("[,; ]");
			if ( parts.length != 4 ) return null;
			int x = Integer.valueOf(parts[0]);
			int y = Integer.valueOf(parts[1]);
			int w = Integer.valueOf(parts[2]);
			int h = Integer.valueOf(parts[3]);
			return new Rectangle(x, y, w, h);
		}
		catch ( Throwable e ) {
			return null;
		}
	}

	/**
	 * Ensures that a control in a GridData layout has a given minimum width.
	 * @param control the control to adjust.
	 * @param minimumWidth the minimum width to use.
	 */
	public static void ensureWidth (Control control,
		int minimumWidth)
	{
		control.pack();
		Rectangle rect = control.getBounds();
		if ( rect.width < minimumWidth ) {
			GridData gd = (GridData)control.getLayoutData(); 
			if ( gd == null ) {
				gd = new GridData();
				control.setLayoutData(gd);
			}
			gd.widthHint = minimumWidth;
		}
	}
	
	/**
	 * Checks the selected path against the project folder variable and set
	 * the edit field as appropriate.
	 * @param path the new selected path. 
	 * @param edField the edit field where to show the path.
	 * @param projectDir the project folder (can be null).
	 */
	public static void checkProjectFolderAfterPick (String path,
		Text edField,
		String projectDir)
	{
		if ( path == null ) return;
//TODO: Implement projectDir variable		
//		String oriPath = edField.getText().replace(BaseUtility.VAR_PROJDIR, projectDir);
//		if ( !path.equals(oriPath) ) {
			edField.setText(path);
//		}
		edField.selectAll();
		edField.setFocus();
	}

	/**
	 * Compute the minimum possible width for a button with two labels.
	 * @param minimumWidth basic minimum size.
	 * @param button button with the first text all. 
	 * @param alternateText alternate text to replace original.
	 * @return the new main minimum width.
	 */
	public static int getMinimumWidth (int minimumWidth,
		Button button,
		String alternateText)
	{
		int n = minimumWidth;
		// Get first text size
		button.pack();
		Point size = button.getSize();
		if ( size.x > n ) n = size.x;
		String tmp = button.getText();
		// Get second size
		button.setText(alternateText);
		button.pack();
		size = button.getSize();
		if ( size.x > n ) n = size.x;
		button.setText(tmp);
		return n;
	}
	
	/**
	 * Ensures that several controls using GridData layout have the same width
	 * that is either the minimum provided or the greatest width needed.
	 * @param minimumWidth the minimum width to use.
	 * @param controls the list of controls to set.
	 */
	public static void setSameWidth (int minimumWidth,
		Control... controls)
	{
		// Compute the width to use
		int min = minimumWidth;
		Rectangle rect;
		for ( Control control : controls ) {
			control.pack();
			rect = control.getBounds();
			if ( rect.width > min ) {
				min = rect.width;
			}
		}
		// Set the optimal width
		GridData gdTmp;
		for ( Control control : controls ) {
			gdTmp = (GridData)control.getLayoutData();
			if ( gdTmp == null ) {
				gdTmp = new GridData();
				control.setLayoutData(gdTmp);
			}
			gdTmp.widthHint = min;
		}
	}

	/**
	 * Disposes the font and colors of a given TextStyle object.
	 * @param ts the object to clear.
	 */
	public static void disposeTextStyle (TextStyle ts) {
		if ( ts != null ) {
			if ( ts.background != null ) ts.background.dispose();
			if ( ts.foreground != null ) ts.foreground.dispose();
			if ( ts.font != null ) ts.font.dispose();
		}
	}

	/**
	 * Clones a TextStyle object, creating new colors and font as needed.
	 * @param device the device where the text style is applied.
	 * @param ts the text style to clone (null will returns null).
	 * @return the new TextSyle object.
	 */
	public static TextStyle cloneTextStyle (Device device, TextStyle ts) {
		if ( ts == null ) return null;
		TextStyle newTS = new TextStyle(ts);
		// In addition: clone the font and color, which are just copied otherwise
		if ( ts.background != null ) newTS.background = new Color(device, ts.background.getRGB());
		if ( ts.foreground != null ) newTS.foreground = new Color(device, ts.foreground.getRGB());
		if ( ts.font != null ) newTS.font = new Font(device, ts.font.getFontData());
		return newTS;
	}

	/**
	 * Centers a given shell on its display. If the shell size exceeds the display size,
	 * the shell is resized to fit in.
	 * @param shell the given shell.
	 */
	public static void centerShell(Shell shell) {
		if (shell == null) return;
		Rectangle ar = shell.getBounds();
		Rectangle dr = shell.getDisplay().getBounds();
		if (ar.width > dr.width) {
			ar.width = dr.width;
		}
		if (ar.height > dr.height) {
			ar.height = dr.height;
		}
		shell.setBounds((dr.width - ar.width) / 2, (dr.height - ar.height) / 2,
				ar.width, ar.height);
	}
}
