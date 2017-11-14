/*===========================================================================
  Copyright (C) 2015 by the Okapi Framework contributors
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

package net.sf.okapi.steps.wordcount.categorized.gmx;

import net.sf.okapi.steps.wordcount.CharacterCounter;
import net.sf.okapi.steps.wordcount.categorized.CategoryGroup;
import net.sf.okapi.steps.wordcount.common.BaseCounter;
import net.sf.okapi.steps.wordcount.common.GMX;

public class GMXRepetitionMatchedCharacterCountStep extends GMXRepetitionMatchedWordCountStep {
	@Override
	public String getMetric() {
		return GMX.RepetitionMatchedCharacterCount;
	}

	@Override
	public String getDescription() {
		return "An accumulation of the character count for repeating text units that have not been matched in any " +
				"other form. Repetition matching is deemed to take precedence over fuzzy matching."		
		+ " Expects: filter events. Sends back: filter events.";
	}

	@Override
	public String getName() {
		return "GMX Repetition Matched Character Count";
	}
	
	@Override
	public CategoryGroup getCategoryGroup() {
		return CategoryGroup.GMX_CHARACTER_COUNTS;
	}
	
	@Override
	protected Class<? extends BaseCounter> getCounterClass() {
		return CharacterCounter.class;
	}
}
