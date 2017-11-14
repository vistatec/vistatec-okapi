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

package net.sf.okapi.steps.wordcount.categorized.gmx;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.ISegments;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.steps.wordcount.WordCounter;
import net.sf.okapi.steps.wordcount.categorized.CategoryGroup;
import net.sf.okapi.steps.wordcount.categorized.CategoryHandler;
import net.sf.okapi.steps.wordcount.common.BaseCountStep;
import net.sf.okapi.steps.wordcount.common.BaseCounter;
import net.sf.okapi.steps.wordcount.common.GMX;

public class GMXProtectedWordCountStep extends BaseCountStep implements CategoryHandler {
	
	public static final String METRIC = GMX.ProtectedWordCount;
	private final WordCounter counter = new WordCounter();
	
	@Override
	public String getName() {
		return "GMX Protected Word Count";
	}

	@Override
	public String getDescription() {
		return "An accumulation of the word count for text that has been marked as 'protected', or otherwise " +
				"not translatable (XLIFF text enclosed in <mrk mtype=\"protected\"> elements)."
		+ " Expects: filter events. Sends back: filter events.";
	}

	@Override
	public String getMetric() {
		return METRIC;
	}

	protected BaseCounter getCounter() {
		return counter;
	}
	
	@Override
	protected long count(TextContainer textContainer, LocaleId locale) {
		long count = getCounter().doGetCount(getSource());
		if (count == 0) // No metrics found on the container
			count = getCounter().doCount(getSource(), locale); // Word Count metrics are based on counting in source
		return count;
	}

	@Override
	protected long count(Segment segment, LocaleId locale) {
		long count = getCounter().doGetCount(segment);
		if (count == 0) // No metrics found on the container
			count = getCounter().doCount(segment, locale); // Word Count metrics are based on counting in source
		return count;
	}

	@Override
	protected long countInTextUnit(ITextUnit textUnit) {
		if (textUnit == null) return 0;
		if (textUnit.isTranslatable()) { // Count only in non-translatable TUs
			removeMetric(textUnit);
			return 0; 
		}
		
		LocaleId srcLocale = getSourceLocale();
		TextContainer source = textUnit.getSource();
		
		// Individual segments metrics
		long segCount = 0;
		long segmentsCount = 0;
		long textContainerCount = 0;
		
		ISegments segs = source.getSegments();
		if (segs != null) {
			for (Segment seg : segs) {
				segCount = count(seg, srcLocale);
				segmentsCount += segCount;
				saveToMetrics(seg, segCount);
			}
		}
		// TC metrics
		textContainerCount = count(source, srcLocale);
		saveToMetrics(source, textContainerCount);
		
		if (textContainerCount > 0) return textContainerCount;  
		if (segmentsCount > 0) return segmentsCount;
		return 0;
	}

	private void removeMetric (ITextUnit textUnit) {
		TextContainer source = textUnit.getSource();
		
		ISegments segs = source.getSegments();
		if (segs != null) {
			for (Segment seg : segs) {
				removeFromMetrics(seg, getMetric());
			}
		}
		removeFromMetrics(source, getMetric());
		removeFromMetrics(textUnit, getMetric());
	}

	@Override
	protected boolean countOnlyTranslatable() {
		return false;
	}

	@Override
	public CategoryGroup getCategoryGroup() {
		return CategoryGroup.GMX_WORD_COUNTS;
	}
}
