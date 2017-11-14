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

import net.sf.okapi.common.ui.Dialogs;
import net.sf.okapi.common.ui.TextOptions;
import net.sf.okapi.common.ui.UIUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Label;

public class TextOptionsPanel extends Composite {

	private TextOptions opt;
	private StyledText edExample;
	private Button chkBidi;
	
	public TextOptionsPanel (Composite parent,
		int flags,
		String caption,
		Font fontCopy)
	{
		super(parent, flags);
		GridLayout layTmp = new GridLayout(4, false);
		layTmp.marginHeight = 0;
		layTmp.marginWidth = 0;
		setLayout(layTmp);
		GridData gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		setLayoutData(gdTmp);

		Label label = new Label(this, SWT.NONE);
		label.setText(caption);
		gdTmp = new GridData();
		gdTmp.horizontalSpan = 4;
		label.setLayoutData(gdTmp);
		
		Button btFont = UIUtil.createGridButton(this, SWT.PUSH, "Font...", UIUtil.BUTTON_DEFAULT_WIDTH, 1);
		btFont.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				selectFont();
			};
		});
		
		Button btTextColor = UIUtil.createGridButton(this, SWT.PUSH, "Text Color...", UIUtil.BUTTON_DEFAULT_WIDTH, 1);
		btTextColor.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				selectColor(false);
			};
		});
		
		Button btBackColor = UIUtil.createGridButton(this, SWT.PUSH, "Background...", UIUtil.BUTTON_DEFAULT_WIDTH, 1);
		btBackColor.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				selectColor(true);
			};
		});

		chkBidi = new Button(this, SWT.CHECK);
		chkBidi.setText("Right to left script");
		chkBidi.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				edExample.setOrientation(chkBidi.getSelection() ? SWT.RIGHT_TO_LEFT : SWT.LEFT_TO_RIGHT);
			};
		});

		edExample = new StyledText(this, SWT.BORDER | SWT.SINGLE);
		edExample.setText("Example of text");
		StyleRange sr = new StyleRange();
		sr.background = getDisplay().getSystemColor(SWT.COLOR_YELLOW);
		sr.start = 11;
		sr.length = 4;
		edExample.setStyleRange(sr);
		
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		gdTmp.horizontalSpan = 4;
		gdTmp.heightHint = 50;
		edExample.setLayoutData(gdTmp);
	}

	@Override
	protected void finalize () {
		if ( opt != null ) opt.dispose();
	}

	/**
	 * Sets the text options to edit.
	 * @param options
	 */
	public void setOptions (TextOptions options) {
		// Copy-construct the resources
		opt = new TextOptions(getDisplay(), options);
		// Applies the options to the example.
		opt.applyTo(edExample);
		// And make sure the bi-directional flag is set too
		chkBidi.setSelection(opt.isBidirectional);
	}
	
	public TextOptions getOptions () {
		// Create a set of options based on the current options
		// Do not return directly the local TextOption object so it can be disposed of with conditions.
		return new TextOptions(getDisplay(), opt);
	}
	
	private void selectColor (boolean background) {
		try {
			ColorDialog dlg = new ColorDialog(getShell());
			if ( background ) {
				dlg.setRGB(opt.background.getRGB());
				dlg.setText("Select Background Color");
			}
			else {
				dlg.setRGB(opt.foreground.getRGB());
				dlg.setText("Select Text Color");
			}
			// Open the selection dialog
			RGB rgb = dlg.open();
			if ( rgb == null ) return;
			// If no user cancellation we set the new color
			if ( background ) {
				opt.background.dispose();
				opt.background = new Color(getDisplay(), rgb);
				edExample.setBackground(opt.background);
			}
			else {
				opt.foreground.dispose();
				opt.foreground = new Color(getDisplay(), rgb);
				edExample.setForeground(opt.foreground);
			}
		}
		catch ( Throwable e ) {
			Dialogs.showError(getShell(), e.getLocalizedMessage(), null);
		}
	}

	private void selectFont () {
		try {
			FontDialog dlg = new FontDialog(getShell());
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
			opt.font = new Font(getDisplay(), fontData);
			edExample.setFont(opt.font);
			tmp.dispose();
			// And the new new color
			opt.foreground.dispose();
			opt.foreground = new Color(getDisplay(), dlg.getRGB());
			edExample.setForeground(opt.foreground);
		}
		catch ( Throwable e ) {
			Dialogs.showError(getShell(), e.getLocalizedMessage(), null);
		}
	}

}
