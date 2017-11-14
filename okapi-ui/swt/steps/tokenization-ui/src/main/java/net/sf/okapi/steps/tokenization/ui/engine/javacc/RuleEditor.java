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

package net.sf.okapi.steps.tokenization.ui.engine.javacc;

import net.sf.okapi.common.IParameters;
import net.sf.okapi.steps.tokenization.ui.engine.AbstractRuleEditor;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Widget;

public class RuleEditor extends AbstractRuleEditor {

	@Override
	protected Class<? extends Composite> getRuleClass() {
		
		return RuleTab.class;
	}

	@Override
	public IParameters createParameters() {
		
		return null;
	}

	@Override
	protected String getCaption() {
		
		return "JavaCC-based tokenization rule";
	}

	@Override
	protected void interop(Widget speaker) {
		
		
	}

}
