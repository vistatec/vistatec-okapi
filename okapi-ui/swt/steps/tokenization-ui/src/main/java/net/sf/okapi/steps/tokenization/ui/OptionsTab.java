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

package net.sf.okapi.steps.tokenization.ui;

import net.sf.okapi.common.ListUtil;
import net.sf.okapi.common.LocaleFilter;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.ui.Dialogs;
import net.sf.okapi.common.ui.abstracteditor.IDialogPage;
import net.sf.okapi.common.ui.abstracteditor.SWTUtil;
import net.sf.okapi.steps.tokenization.Parameters;
import net.sf.okapi.steps.tokenization.ui.locale.LanguageSelector;
import net.sf.okapi.steps.tokenization.ui.locale.LanguageSelectorPePage;
import net.sf.okapi.steps.tokenization.ui.tokens.TokenSelector;
import net.sf.okapi.steps.tokenization.ui.tokens.TokenSelectorPePage;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

public class OptionsTab extends Composite implements IDialogPage {
	private Group grpTokenizeInThe;
	private Button source;
	private Button targets;
	private Button langC;
	private Button tokensC;
//	private LanguageAndTokenParameters filterParams = new LanguageAndTokenParameters();
	private Group grpLanguagesToTokenize;
	private Text langE;
	private Text tokensE;
	private Label label_2;
	private Label languages;
	private Label tokens;

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public OptionsTab(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(1, false));
		
		grpTokenizeInThe = new Group(this, SWT.NONE);
		grpTokenizeInThe.setText("General");
		grpTokenizeInThe.setToolTipText("");
		grpTokenizeInThe.setLayout(new GridLayout(3, false));
		grpTokenizeInThe.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		grpTokenizeInThe.setData("name", "grpTokenizeInThe");
		
		source = new Button(grpTokenizeInThe, SWT.CHECK);
		source.setData("name", "source");
		source.setText("Tokenize source");
		
		label_2 = new Label(grpTokenizeInThe, SWT.NONE);
		GridData gridData_4 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gridData_4.widthHint = 100;
		label_2.setLayoutData(gridData_4);
		label_2.setData("name", "label_2");
		
		targets = new Button(grpTokenizeInThe, SWT.CHECK);
		targets.setData("name", "targets");
		targets.setText("Tokenize targets");
		
		grpLanguagesToTokenize = new Group(this, SWT.NONE);
		grpLanguagesToTokenize.setText("Languages");
		grpLanguagesToTokenize.setLayout(new GridLayout(2, false));
		grpLanguagesToTokenize.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
				
				languages = new Label(grpLanguagesToTokenize, SWT.NONE);
				languages.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
				languages.setData("name", "languages");
				languages.setText("Select languages, or specify a locale filter string (empty = all languages):");
				
				langE = new Text(grpLanguagesToTokenize, SWT.BORDER);
				GridData gridData_2 = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
				gridData_2.widthHint = 311;
				langE.setLayoutData(gridData_2);
				langE.setData("name", "langE");
				langE.addVerifyListener(new VerifyListener() {
					public void verifyText(VerifyEvent e) {
						
						e.text = e.text.toLowerCase();
					}
				});
				
				langC = new Button(grpLanguagesToTokenize, SWT.NONE);
				{
					GridData gridData_1 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
					gridData_1.widthHint = 70;
					langC.setLayoutData(gridData_1);
				}
				langC.setData("name", "langC");
				langC.setText("Select...");
		{
			Group grpTokensToCapture = new Group(this, SWT.NONE);
			grpTokensToCapture.setText("Tokens");
			grpTokensToCapture.setLayout(new GridLayout(2, false));
			GridData gridData_3 = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
			gridData_3.heightHint = 100;
			gridData_3.widthHint = 500;
			grpTokensToCapture.setLayoutData(gridData_3);
			
			tokens = new Label(grpTokensToCapture, SWT.NONE);
			tokens.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
			tokens.setData("name", "tokens");
			tokens.setText("Select tokens to extract (empty = all tokens):");
			
			tokensE = new Text(grpTokensToCapture, SWT.BORDER);
			GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
			gridData.widthHint = 296;
			tokensE.setLayoutData(gridData);
			tokensE.setData("name", "tokensE");
			tokensE.addVerifyListener(new VerifyListener() {
				public void verifyText(VerifyEvent e) {
					
					e.text = e.text.toUpperCase();
				}
			});
			
			tokensC = new Button(grpTokensToCapture, SWT.NONE);
			{
				GridData gridData_1 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
				gridData_1.widthHint = 70;
				tokensC.setLayoutData(gridData_1);
			}
			tokensC.setData("name", "tokensC");
			tokensC.setText("Select...");
		}
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	public boolean canClose(boolean isOK) {

		return true;
	}

	public void interop(Widget speaker) {
		
		if (speaker == source && (SWTUtil.getNotSelected(targets) || SWTUtil.getDisabled(targets)) && SWTUtil.getNotSelected(source)) {
			Dialogs.showWarning(getShell(),
				"You cannot unselect this check-box, otherwise there's noting to tokenize.", null);
			
			SWTUtil.setSelected(source, true);
		}
		
		if (speaker == targets && (SWTUtil.getNotSelected(source) || SWTUtil.getDisabled(source)) && SWTUtil.getNotSelected(targets)) {
			Dialogs.showWarning(getShell(),
				"You cannot unselect this check-box, otherwise there's noting to tokenize.", null);
			
			SWTUtil.setSelected(targets, true);
		}

//		SWTUtil.setEnabled(langC, SWTUtil.getSelected(langW));
//		SWTUtil.setEnabled(langBC, SWTUtil.getSelected(langB));
//		SWTUtil.setEnabled(tokensC, SWTUtil.getSelected(tokens));
//		
//		SWTUtil.setEnabled(langE, SWTUtil.getSelected(langW));
//		SWTUtil.setEnabled(langBE, SWTUtil.getSelected(langB));
//		SWTUtil.setEnabled(tokensE, SWTUtil.getSelected(tokens));
		
		if (speaker == langC) {

//			filterParams.languageMode = LanguageAndTokenParameters.LANGUAGES_ONLY_WHITE_LIST;
//			filterParams.languageWhiteList = langWE.getText();
			
			// No result analysis needed, the LanguagesPage will change filterParams only if its dialog was OK-ed
			//SWTUtil.inputQuery(LanguagesPage.class, getShell(), "Languages to tokenize", filterParams, null);
			
			String explicitLocales = LocaleFilter.getExplicitLocaleIds(langE.getText());
			if (!Util.isEmpty(explicitLocales))
				SWTUtil.setText(langE, explicitLocales);
			
			String res = ListUtil.arrayAsString(LanguageSelector.select(getShell(), LanguageSelectorPePage.class, 
					explicitLocales), " ");
			//SWTUtil.setText(langWE, filterParams.languageWhiteList);
			
			// explicitLocales <> "" if there's a list of locale IDs in it, can be safely replaced with the result
			if (Util.isEmpty(explicitLocales) && !Util.isEmpty(langE.getText())) {
			
				if (!Util.isEmpty(res))
					SWTUtil.setText(langE, langE.getText() + " " + res);
			}							
			else 
				SWTUtil.setText(langE, res);
			
			langE.setFocus();
			langE.setSelection(langE.getText().length());
		}
		
//		if (speaker == langBC) {
//
////			filterParams.languageMode = LanguageAndTokenParameters.LANGUAGES_ALL_EXCEPT_BLACK_LIST;
////			filterParams.languageBlackList = langBE.getText();
//			
//			// No result analysis needed, the LanguagesPage will change filterParams only if its dialog was OK-ed
//			//SWTUtil.inputQuery(LanguagesPage.class, getShell(), "Languages NOT to tokenize (exceptions)", filterParams, null);
//			String[] res = LanguageSelector.select(getShell(), LanguageSelectorPePage.class, langBE.getText());
//			//SWTUtil.setText(langBE, filterParams.languageBlackList);
//			SWTUtil.setText(langBE, ListUtil.arrayAsString(res));
//		}
		
		if (speaker == tokensC) {
			
//			filterParams.tokenMode = LanguageAndTokenParameters.TOKENS_SELECTED;
//			filterParams.tokenNames = tokensE.getText();
			
			// No result analysis needed, the TokenNamesPage will change filterParams only if its dialog was OK-ed
			//SWTUtil.inputQuery(TokenNamesPage.class, getShell(), "Tokens to capture", filterParams, null);
			String[] res = TokenSelector.select(getShell(), TokenSelectorPePage.class, tokensE.getText());
			//SWTUtil.setText(tokensE, filterParams.tokenNames);
			SWTUtil.setText(tokensE, ListUtil.arrayAsString(res));
			tokensE.setFocus();
		}
		
//		if (speaker == langW)
//			langE.setFocus();
//		
//		if (speaker == langB)
//			langBE.setFocus();
//			
//		if (speaker == tokens)
//			tokensE.setFocus();
	}

	public boolean load(Object data) {

		if (data instanceof Parameters) {
			
			Parameters params = (Parameters) data;
			
			SWTUtil.setSelected(source, params.tokenizeSource);
			SWTUtil.setSelected(targets, params.tokenizeTargets);
			SWTUtil.setText(langE, params.getLanguages());
			SWTUtil.setText(tokensE, ListUtil.listAsString(params.getTokenNames()));
		}
		
		SWTUtil.addSpeakers(this, source, targets, langC, langE, tokensC, tokensE);
		langE.setFocus();
		
		return true;
	}

	public boolean save(Object data) {

		if (data instanceof Parameters) {
			
			Parameters params = (Parameters) data;
			
			params.tokenizeSource = SWTUtil.getSelected(source);
			params.tokenizeTargets = SWTUtil.getSelected(targets);
			params.setLocaleFilter(langE.getText());
			params.setTokenNames(ListUtil.stringAsArray(tokensE.getText(), " "));
		}

		return true;
	}
}
