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

package net.sf.okapi.steps.wordcount;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.steps.wordcount.common.GMX;
import net.sf.okapi.steps.wordcount.common.Parameters;
import net.sf.okapi.steps.wordcount.common.TokenCountStep;

/**
 * Word Counter pipeline step. The counter counts a number of words in translatable text units. 
 * The count results are placed in a MetricsAnnotation structure (with the GMX TotalWordCount 
 * metric set), attached to the respective event's resource (TEXT_UNIT, END_DOCUMENT, END_BATCH, 
 * END_BATCH_ITEM, END_SUBDOCUMENT, END_GROUP).  
 * 
 * @version 0.1 06.07.2009
 */
@UsingParameters(Parameters.class)
public class WordCountStep extends TokenCountStep {
	
	public static final String METRIC = GMX.TotalWordCount; 
	
	@Override
	public String getMetric() {
		return METRIC;
	}

	@Override
	protected String[] getTokenNames() {
		return new String[] {WordCounter.getTokenName()};
	}

	@Override
	protected boolean countOnlyTranslatable() {
		return false;
	}

	@Override
	public String getDescription() {
		return "Count the number of words in the text units of a set of documents or/and in its parts."
		+ " Expects: filter events. Sends back: filter events.";
	}

	@Override
	public String getName() {
		return "Word Count";
	}
	
	@Override
	protected long count(Segment segment, LocaleId locale) {
		if (GMX.isLogographicScript(locale)) {
			return WordCounter.countLogographicScript(segment, locale);
		} else {
			return super.count(segment, locale);
		}
	}
	
	@Override
	protected long count(TextContainer textContainer, LocaleId locale) {
		if (GMX.isLogographicScript(locale)) {
			return WordCounter.countLogographicScript(textContainer, locale);
		} else {
			return super.count(textContainer, locale);
		}
	}
}
