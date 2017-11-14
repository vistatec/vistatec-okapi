/*===========================================================================
  Copyright (C) 2008-2010 by the Okapi Framework contributors
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

import net.sf.okapi.common.query.MatchType;
import net.sf.okapi.steps.wordcount.categorized.CategoryGroup;
import net.sf.okapi.steps.wordcount.categorized.CategoryHandler;
import net.sf.okapi.steps.wordcount.common.AltAnnotationBasedCountStep;
import net.sf.okapi.steps.wordcount.common.GMX;

public class GMXLeveragedMatchedWordCountStep extends AltAnnotationBasedCountStep implements CategoryHandler {

	public static final String METRIC = GMX.LeveragedMatchedWordCount; 
		
	@Override
	public String getMetric() {
		return METRIC;
	}

	@Override
	public String getDescription() {
		return "An accumulation of the word count for text units that have been matched against a leveraged translation memory database."
		+ " Expects: filter events. Sends back: filter events.";
	}

	@Override
	public String getName() {
		return "GMX Leveraged Word Count";
	}

	@Override
	protected boolean accept(MatchType type) {
		return (type == MatchType.EXACT_LOCAL_CONTEXT ||				
				type == MatchType.EXACT_STRUCTURAL ||
				type == MatchType.EXACT);
	}

	@Override
	public CategoryGroup getCategoryGroup() {
		return CategoryGroup.GMX_WORD_COUNTS;
	}
}
