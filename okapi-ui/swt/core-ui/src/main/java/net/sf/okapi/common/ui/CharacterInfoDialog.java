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

import net.sf.okapi.common.IHelp;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Dialog box for getting general information about a given Unicode character.
 */
public class CharacterInfoDialog {
	
	private Shell shell;
	private int codePoint;
	private CLabel stRendering;
	private Text edCharacter;
	private Text edCodePoint;
	private Text edType;
	private Text edNumValue;
	private Text edIsJavaSpace;
	private Text edIsUnicodeSpace;
	private boolean settingCodePoint = false;
	private Font sampleFont;
	private IHelp help;

	@Override
	protected void finalize () {
		dispose();
	}

	/**
	 * Creates a new CharacterInfoDialog object.
	 * @param parent the parent Shell.
	 * @param captionText the text of the caption.
	 * @param helpParam the {@link IHelp} object to use with this dialog.
	 */
	public CharacterInfoDialog (Shell parent,
		String captionText,
		IHelp helpParam)
	{
		help = helpParam;
		shell = new Shell(parent, SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.APPLICATION_MODAL);
		if ( captionText != null ) shell.setText(captionText);
		UIUtil.inheritIcon(shell, parent);
		shell.setLayout(new GridLayout());
		
		Composite cmpTmp = new Composite(shell, SWT.BORDER);
		cmpTmp.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layTmp = new GridLayout(3, false);
		cmpTmp.setLayout(layTmp);
		
		stRendering = new CLabel(cmpTmp, SWT.BORDER | SWT.CENTER);
		GridData gdTmp = new GridData();
		gdTmp.widthHint = 60;
		gdTmp.heightHint = 60;
		gdTmp.verticalSpan = 2;
		stRendering.setLayoutData(gdTmp);

		Font font = stRendering.getFont();
		FontData[] fontData = font.getFontData();
		fontData[0].setHeight(20);
		sampleFont = new Font(font.getDevice(), fontData[0]);
		stRendering.setFont(sampleFont);

		Label label = new Label(cmpTmp, SWT.NONE);
		label.setText(Res.getString("charInfoDlg.codePoint")); //$NON-NLS-1$
		edCodePoint = new Text(cmpTmp, SWT.BORDER);
		gdTmp = new GridData();
		gdTmp.widthHint = 60;
		edCodePoint.setLayoutData(gdTmp);
		edCodePoint.addModifyListener(new ModifyListener () {
			public void modifyText(ModifyEvent e) {
				updateCodePoint();
			}
		});
		
		label = new Label(cmpTmp, SWT.NONE);
		label.setText(Res.getString("charInfoDlg.char")); //$NON-NLS-1$
		edCharacter = new Text(cmpTmp, SWT.BORDER);
		gdTmp = new GridData();
		gdTmp.widthHint = 30;
		edCharacter.setLayoutData(gdTmp);
		edCharacter.addModifyListener(new ModifyListener () {
			public void modifyText(ModifyEvent e) {
				updateCharacter();
			}
		});
		
		label = new Label(cmpTmp, SWT.NONE);
		label.setText(Res.getString("charInfoDlg.charType")); //$NON-NLS-1$
		edType = new Text(cmpTmp, SWT.BORDER);
		edType.setEditable(false);
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		gdTmp.horizontalSpan = 2;
		edType.setLayoutData(gdTmp);
		
		label = new Label(cmpTmp, SWT.NONE);
		label.setText(Res.getString("charInfoDlg.numValue")); //$NON-NLS-1$
		edNumValue = new Text(cmpTmp, SWT.BORDER);
		edNumValue.setEditable(false);
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		gdTmp.horizontalSpan = 2;
		edNumValue.setLayoutData(gdTmp);
		
		label = new Label(cmpTmp, SWT.NONE);
		label.setText(Res.getString("charInfoDlg.isWhitespace")); //$NON-NLS-1$
		edIsJavaSpace = new Text(cmpTmp, SWT.BORDER);
		edIsJavaSpace.setEditable(false);
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		gdTmp.horizontalSpan = 2;
		edIsJavaSpace.setLayoutData(gdTmp);

		label = new Label(cmpTmp, SWT.NONE);
		label.setText(Res.getString("charInfoDlg.isUniWhitespace")); //$NON-NLS-1$
		edIsUnicodeSpace = new Text(cmpTmp, SWT.BORDER);
		edIsUnicodeSpace.setEditable(false);
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		gdTmp.horizontalSpan = 2;
		edIsUnicodeSpace.setLayoutData(gdTmp);

		//--- Dialog-level buttons

		SelectionAdapter CloseActions = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if ( e.widget.getData().equals("h") ) { //$NON-NLS-1$
					if ( help != null ) help.showWiki("Character Information");
					return;
				}
				shell.close();
			};
		};
		ClosePanel pnlActions = new ClosePanel(shell, SWT.NONE, CloseActions, (help!=null));
		pnlActions.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		shell.setDefaultButton(pnlActions.btClose);

		shell.pack();
		shell.setMinimumSize(shell.getSize());
		Point startSize = shell.getMinimumSize();
		if ( startSize.x < 450 ) startSize.x = 450;
		shell.setSize(startSize);
		Dialogs.centerWindow(shell, parent);
	}
	
	/**
	 * Dispose of all internal resources.
	 */
	public void dispose () {
		if ( sampleFont != null ) {
			sampleFont.dispose();
			sampleFont = null;
		}
	}

	private void updateCodePoint () {
		try {
			if ( settingCodePoint ) return;
			String tmp = edCodePoint.getText();
			if ( tmp.length() != 4 ) return;
			int cp = Integer.valueOf(tmp, 16);
			setCodePoint(cp);
			edCodePoint.setSelection(4, 4);
		}
		catch ( Throwable e ) {
			Dialogs.showError(shell, Res.getString("charInfoDlg.invalidValue")+e.getMessage(), null); //$NON-NLS-1$
		}
	}
	
	private void updateCharacter () {
		try {
			if ( settingCodePoint ) return;
			String tmp = edCharacter.getText();
			if ( tmp.length() < 1 ) return;
			setCodePoint(tmp.codePointAt(0));
		}
		catch ( Throwable e ) {
			Dialogs.showError(shell, Res.getString("charInfoDlg.invalidValue")+e.getMessage(), null); //$NON-NLS-1$
		}
	}
	
	private void setCodePoint (int value) {
		settingCodePoint = true;
		codePoint = value;
		stRendering.setText(String.format("%c", codePoint)); //$NON-NLS-1$
		edCharacter.setText(stRendering.getText());
		edCodePoint.setText(String.format("%04X", codePoint)); //$NON-NLS-1$
		
		int type = Character.getType(codePoint);
		switch ( type ) {
		case Character.COMBINING_SPACING_MARK:
			edType.setText("Mc : COMBINING_SPACING_MARK"); //$NON-NLS-1$
			break;
		case Character.CONNECTOR_PUNCTUATION:
			edType.setText("Pc : CONNECTOR_PUNCTUATION"); //$NON-NLS-1$
			break;
		case Character.CONTROL:
			edType.setText("Cc : CONTROL"); //$NON-NLS-1$
			break;
		case Character.CURRENCY_SYMBOL:
			edType.setText("Sc : CURRENCY_SYMBOL"); //$NON-NLS-1$
			break;
		case Character.DASH_PUNCTUATION:
			edType.setText("Pd : DASH_PUNCTUATION"); //$NON-NLS-1$
			break;
		case Character.DECIMAL_DIGIT_NUMBER:
			edType.setText("Nd : DECIMAL_DIGIT_NUMBER"); //$NON-NLS-1$
			break;
		case Character.ENCLOSING_MARK:
			edType.setText("Me : ENCLOSING_MARK"); //$NON-NLS-1$
			break;
		case Character.END_PUNCTUATION:
			edType.setText("Pe : END_PUNCTUATION"); //$NON-NLS-1$
			break;
		case Character.FINAL_QUOTE_PUNCTUATION:
			edType.setText("Pf : FINAL_QUOTE_PUNCTUATION"); //$NON-NLS-1$
			break;
		case Character.FORMAT:
			edType.setText("Cf : FORMAT"); //$NON-NLS-1$
			break;
		case Character.INITIAL_QUOTE_PUNCTUATION:
			edType.setText("Pi : INITIAL_QUOTE_PUNCTUATION"); //$NON-NLS-1$
			break;
		case Character.LETTER_NUMBER:
			edType.setText("Nl : LETTER_NUMBER"); //$NON-NLS-1$
			break;
		case Character.UPPERCASE_LETTER:
			edType.setText("Lu : UPPERCASE_LETTER"); //$NON-NLS-1$
			break;
 		case Character.LINE_SEPARATOR:
 			edType.setText("Zl : LINE_SEPARATOR"); //$NON-NLS-1$
 			break;
 		case Character.LOWERCASE_LETTER:
	 		edType.setText("Ll : LOWERCASE_LETTER"); //$NON-NLS-1$
			break;
		case Character.MATH_SYMBOL:
			edType.setText("Sm : MATH_SYMBOL"); //$NON-NLS-1$
			break;
		case Character.MODIFIER_LETTER:
			edType.setText("Lm : MODIFIER_LETTER"); //$NON-NLS-1$
			break;
		case Character.MODIFIER_SYMBOL:
			edType.setText("Sk : MODIFIER_SYMBOL"); //$NON-NLS-1$
			break;
		case Character.NON_SPACING_MARK:
			edType.setText("Mn : NON_SPACING_MARK"); //$NON-NLS-1$
			break;
		case Character.OTHER_LETTER:
			edType.setText("Lo : OTHER_LETTER"); //$NON-NLS-1$
			break;
		case Character.OTHER_NUMBER:
			edType.setText("No : OTHER_NUMBER"); //$NON-NLS-1$
			break;
		case Character.OTHER_PUNCTUATION:
			edType.setText("Po : OTHER_PUNCTUATION"); //$NON-NLS-1$
			break;
		case Character.OTHER_SYMBOL:
			edType.setText("So : OTHER_SYMBOL"); //$NON-NLS-1$
			break;
		case Character.PARAGRAPH_SEPARATOR:
			edType.setText("Zp : PARAGRAPH_SEPARATOR"); //$NON-NLS-1$
			break;
		case Character.PRIVATE_USE:
			edType.setText("Co : PRIVATE_USE"); //$NON-NLS-1$
			break;
		case Character.SPACE_SEPARATOR:
			edType.setText("Zs : SPACE_SEPARATOR"); //$NON-NLS-1$
			break;
		case Character.START_PUNCTUATION:
			edType.setText("Ps : START_PUNCTUATION"); //$NON-NLS-1$
			break;
		case Character.SURROGATE:
			edType.setText("Cs : SURROGATE"); //$NON-NLS-1$
			break;
		case Character.TITLECASE_LETTER:
			edType.setText("Lt : TITLECASE_LETTER"); //$NON-NLS-1$
			break;
		case Character.UNASSIGNED:
			edType.setText("Cn : UNASSIGNED"); //$NON-NLS-1$
			break;
		}

		edIsJavaSpace.setText(Character.isWhitespace(codePoint)? Res.getString("charInfoDlg.yes") : Res.getString("charInfoDlg.no")); //$NON-NLS-1$ //$NON-NLS-2$
		edIsUnicodeSpace.setText(Character.isSpaceChar(codePoint)? Res.getString("charInfoDlg.yes") : Res.getString("charInfoDlg.no")); //$NON-NLS-1$ //$NON-NLS-2$
		edNumValue.setText(String.valueOf(Character.getNumericValue(codePoint)));
		settingCodePoint = false;
	}
		
	/**
	 * Calls the dialog.
	 * @param codePoint the code point of the Unicode character to start with.
	 */
	public void showDialog (int codePoint) {
		setCodePoint(codePoint);
		shell.open();
		while ( !shell.isDisposed() ) {
			if ( !shell.getDisplay().readAndDispatch() )
				shell.getDisplay().sleep();
		}
	}

}
