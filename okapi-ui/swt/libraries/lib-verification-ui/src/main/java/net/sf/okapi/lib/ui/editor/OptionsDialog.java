/*===========================================================================
  Copyright (C) 2010 by the Okapi Framework contributors
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

package net.sf.okapi.lib.ui.editor;

import net.sf.okapi.common.IHelp;
import net.sf.okapi.common.ui.Dialogs;
import net.sf.okapi.common.ui.OKCancelPanel;
import net.sf.okapi.common.ui.TextOptions;
import net.sf.okapi.common.ui.UIUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

class OptionsDialog {

	private Shell dialog;
	private boolean result = false;
	private OKCancelPanel pnlActions;
	private IHelp help;

	private final String exampleText = "{Marker}This is an <Code>example</Code>{/Marker}";

	private TextOptions opt;
	private TextStyle codeStyle;
	private TextStyle markStyle;
	private StyledText edExample;
//	private Button chkBidi;
	private Device device;
	
	public OptionsDialog (Shell parent,
		IHelp paramHelp)
	{
		help = paramHelp;
		dialog = new Shell(parent, SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.APPLICATION_MODAL);
		dialog.setText("Options");
		dialog.setLayout(new GridLayout());
		dialog.setLayoutData(new GridData(GridData.FILL_BOTH));
		UIUtil.inheritIcon(dialog, parent);
		device = dialog.getDisplay();

		Group grpTmp = new Group(dialog, SWT.NONE);
		grpTmp.setText("Font and Colors");
		grpTmp.setLayout(new GridLayout(4, false));
		grpTmp.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		edExample = new StyledText(grpTmp, SWT.BORDER | SWT.SINGLE);
		edExample.setText(exampleText);
		GridData gdTmp = new GridData(GridData.FILL_BOTH);
		gdTmp.horizontalSpan = 4;
		gdTmp.heightHint = 50;
		gdTmp.widthHint = 500;
		edExample.setLayoutData(gdTmp);
		
		Label label = new Label(grpTmp, SWT.NONE);
		label.setText("Text:");

		Button btTextColor = UIUtil.createGridButton(grpTmp, SWT.PUSH, "Text Color...", UIUtil.BUTTON_DEFAULT_WIDTH, 1);
		btTextColor.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				selectColor(false, null);
			};
		});
		
		Button btBackColor = UIUtil.createGridButton(grpTmp, SWT.PUSH, "Background...", UIUtil.BUTTON_DEFAULT_WIDTH, 1);
		btBackColor.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				selectColor(true, null);
			};
		});

		Button btFont = UIUtil.createGridButton(grpTmp, SWT.PUSH, "Font...", UIUtil.BUTTON_DEFAULT_WIDTH, 1);
		btFont.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				selectFont();
			};
		});
		
//		chkBidi = new Button(grpTmp, SWT.CHECK);
//		chkBidi.setText("Right to left script");
//		chkBidi.addSelectionListener(new SelectionAdapter() {
//			public void widgetSelected(SelectionEvent e) {
//				opt.isBidirectional = chkBidi.getSelection();
//				edExample.setOrientation(opt.isBidirectional ? SWT.RIGHT_TO_LEFT : SWT.LEFT_TO_RIGHT);
//			};
//		});
		
		label = new Label(grpTmp, SWT.NONE);
		label.setText("Codes:");
		
		btTextColor = UIUtil.createGridButton(grpTmp, SWT.PUSH, "Text Color...", UIUtil.BUTTON_DEFAULT_WIDTH, 1);
		btTextColor.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				selectColor(false, codeStyle);
			};
		});
		
		btBackColor = UIUtil.createGridButton(grpTmp, SWT.PUSH, "Background...", UIUtil.BUTTON_DEFAULT_WIDTH, 1);
		btBackColor.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				selectColor(true, codeStyle);
			};
		});
		
		new Label(grpTmp, SWT.NONE); // Empty place-holder

		label = new Label(grpTmp, SWT.NONE);
		label.setText("Markers:");
		
		btTextColor = UIUtil.createGridButton(grpTmp, SWT.PUSH, "Text Color...", UIUtil.BUTTON_DEFAULT_WIDTH, 1);
		btTextColor.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				selectColor(false, markStyle);
			};
		});
		
		btBackColor = UIUtil.createGridButton(grpTmp, SWT.PUSH, "Background...", UIUtil.BUTTON_DEFAULT_WIDTH, 1);
		btBackColor.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				selectColor(true, markStyle);
			};
		});

		new Label(grpTmp, SWT.NONE); // Empty place-holder
		
		//--- Dialog-level buttons

		SelectionAdapter OKCancelActions = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				result = false;
				if ( e.widget.getData().equals("h") ) {
					if ( help != null ) help.showTopic(this, "options");
					return;
				}
				if ( e.widget.getData().equals("o") ) {
					result = true;
				}
				dialog.close();
			};
		};
		pnlActions = new OKCancelPanel(dialog, SWT.NONE, OKCancelActions, (help!=null));
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		gdTmp.horizontalSpan = 2;
		pnlActions.setLayoutData(gdTmp);

		dialog.pack();
		Point size = dialog.getSize();
		dialog.setMinimumSize(size);
		Dialogs.centerWindow(dialog, parent);
	}

	@Override
	protected void finalize () {
		if ( opt != null ) opt.dispose();
		UIUtil.disposeTextStyle(codeStyle);
		UIUtil.disposeTextStyle(markStyle);
	}

	public void setData (TextOptions textOptions,
		TextStyle paramCodeStyle,
		TextStyle paramMarkStyle)
	{
		// Copy-construct the resources
		opt = new TextOptions(device, textOptions);
		codeStyle = UIUtil.cloneTextStyle(device, paramCodeStyle);
		markStyle = UIUtil.cloneTextStyle(device, paramMarkStyle);
		
//		chkBidi.setSelection(opt.isBidirectional);
		updateExample();
	}
	
	public boolean showDialog () {
		dialog.open();
		while ( !dialog.isDisposed() ) {
			if ( !dialog.getDisplay().readAndDispatch() )
				dialog.getDisplay().sleep();
		}
		return result;
	}

	public TextOptions getTextOptions () {
		// Create a set of options based on the current options
		// Do not return directly the local TextOption object so it can be disposed of with conditions.
		return new TextOptions(device, opt);
	}

	public TextStyle getCodeStyle () {
		// Returns a copy of the style
		return UIUtil.cloneTextStyle(device, codeStyle);
	}
	
	public TextStyle getMarkStyle () {
		// Returns a copy of the style
		return UIUtil.cloneTextStyle(device, markStyle);
	}
	
	private void updateExample () {
		edExample.setText(exampleText);
		opt.applyTo(edExample);
		
		// Marker 1
		StyleRange sr = new StyleRange(markStyle);
		sr.start = 0;
		sr.length = 8;
		edExample.setStyleRange(sr);
		// Marker 2
		sr = new StyleRange(markStyle);
		sr.start = 39;
		sr.length = 9;
		edExample.setStyleRange(sr);
		// Code 1
		sr = new StyleRange(codeStyle);
		sr.start = 19;
		sr.length = 6;
		edExample.setStyleRange(sr);
		// Code 2
		sr = new StyleRange(codeStyle);
		sr.start = 32;
		sr.length = 7;
		edExample.setStyleRange(sr);
	}

	private void selectColor (boolean background,
		TextStyle ts)
	{
		try {
			ColorDialog dlg = new ColorDialog(dialog);
			if ( background ) {
				if ( ts == null ) {
					dlg.setRGB(opt.background.getRGB());
				}
				else if ( ts.background != null ) {
					dlg.setRGB(ts.background.getRGB());
				}
				dlg.setText("Select Background Color");
			}
			else {
				if ( ts == null ) {
					dlg.setRGB(opt.foreground.getRGB());
				}
				else if ( ts.foreground != null ) {
					dlg.setRGB(ts.foreground.getRGB());
				}
				dlg.setText("Select Text Color");
			}
			// Open the selection dialog
			RGB rgb = dlg.open();
			if ( rgb == null ) return;
	
			// If no user cancellation we set the new color
			if ( ts == null ) {
				if ( background ) {
					opt.background.dispose();
					opt.background = new Color(device, rgb);
				}
				else {
					opt.foreground.dispose();
					opt.foreground = new Color(device, rgb);
				}
			}
			else {
				if ( background ) {
					if ( ts.background != null ) ts.background.dispose();
					ts.background = new Color(device, rgb);
				}
				else {
					if ( ts.foreground != null ) ts.foreground.dispose();
					ts.foreground = new Color(device, rgb);
				}
			}
			updateExample();
		}
		catch ( Throwable e ) {
			Dialogs.showError(dialog, e.getLocalizedMessage(), null);
		}
	}
	
	private void selectFont () {
		try {
			FontDialog dlg = new FontDialog(dialog);
			dlg.setText("Select Font");
			// Set current font and color info
			dlg.setFontList(opt.font.getFontData());
			dlg.setRGB(opt.foreground.getRGB());
			// Open the dialog
			FontData fontData = dlg.open();
			if ( fontData == null) return;
			
			// If not canceled by user: We assign the new font
			// Work around: For some reason disposing of the font before calling edExample.setFont()
			// with the new font is causing an invalid argument exception. So we defer the dispose.
			Font tmp = opt.font; 
			opt.font = new Font(device, fontData);
			edExample.setFont(opt.font);
			tmp.dispose();
			// And the new new color
			opt.foreground.dispose();
			opt.foreground = new Color(device, dlg.getRGB());
			updateExample();
		}
		catch ( Throwable e ) {
			Dialogs.showError(dialog, e.getLocalizedMessage(), null);
		}
	}

}
