/*===========================================================================
  Copyright (C) 2011-2012 by the Okapi Framework contributors
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

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
import net.sf.okapi.common.pipeline.annotations.StepParameterType;
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.steps.wordcount.common.GMX;
import net.sf.okapi.steps.wordcount.common.Metrics;
import net.sf.okapi.steps.wordcount.common.MetricsAnnotation;

import com.ibm.icu.text.BreakIterator;
import com.ibm.icu.text.RuleBasedBreakIterator;
import com.ibm.icu.util.ULocale;

// For now we don't use the parameters
// @UsingParameters(ParametersSimpleWordCountStep.class)
public class SimpleWordCountStep extends BasePipelineStep {
	
	private RuleBasedBreakIterator srcWordIterator = null;
	private LocaleId srcLoc;
	private long srcBatchItemWordCount;
	private long srcBatchWordCount;
//	private ParametersSimpleWordCountStep params;	
	
	public SimpleWordCountStep() {
//		params = new ParametersSimpleWordCountStep();
	}
	
	@StepParameterMapping(parameterType = StepParameterType.SOURCE_LOCALE)
	public void setSourceLocale (LocaleId sourceLocale) {
		this.srcLoc = sourceLocale;
		srcWordIterator = (RuleBasedBreakIterator) BreakIterator.getWordInstance(ULocale
				.createCanonical(srcLoc.toString()));
		RuleBasedBreakIterator.registerInstance(srcWordIterator, srcLoc.toJavaLocale(),
				BreakIterator.KIND_WORD);
	}
	
	@Override
	protected Event handleStartBatch (Event event) {
		srcBatchWordCount = 0;
		return super.handleStartBatch(event);
	}
	
	@Override
	protected Event handleStartBatchItem (Event event) {
		srcBatchItemWordCount = 0;
		return super.handleStartBatchItem(event);
	}
	
	@Override
	protected Event handleEndBatchItem (Event event) {
		Ending res = event.getEnding();
		if (res == null) {
			res = new Ending("");
			event.setResource(res);
		}
			
		MetricsAnnotation sma = res.getAnnotation(MetricsAnnotation.class);
		if ( sma == null ) {
			sma = new MetricsAnnotation();
			res.setAnnotation(sma);
		}
		Metrics m = sma.getMetrics();
		m.setMetric(GMX.TotalWordCount, srcBatchItemWordCount);

		srcBatchWordCount += srcBatchItemWordCount;
		
		return super.handleEndBatchItem(event);
	}
	
	@Override
	protected Event handleEndBatch (Event event) {
		Ending res = event.getEnding();
		if (res == null) {
			res = new Ending("");
			event.setResource(res);
		}
			
		MetricsAnnotation sma = res.getAnnotation(MetricsAnnotation.class);
		if (sma == null) {
			sma = new MetricsAnnotation();
			res.setAnnotation(sma);
		}
		Metrics m = sma.getMetrics();
		m.setMetric(GMX.TotalWordCount, srcBatchWordCount);
		
		return super.handleEndBatch(event);
	}
	
	@Override
	protected Event handleTextUnit (Event event) {
		ITextUnit tu = event.getTextUnit();
		long srcWordCount = 0;
		
		if ( tu.isEmpty() || !tu.isTranslatable() ) {
			return event;
		}

		if ( !tu.getSource().isEmpty() ) {
			srcWordCount = countWords(tu.getSource().getUnSegmentedContentCopy().getText());
			srcBatchItemWordCount += srcWordCount;
		}
		
		MetricsAnnotation sma = tu.getSource().getAnnotation(MetricsAnnotation.class);
		if ( sma == null ) {
			sma = new MetricsAnnotation();
			tu.getSource().setAnnotation(sma);
		}
		Metrics m = sma.getMetrics();
		m.setMetric(GMX.TotalWordCount, srcWordCount);
		
		return event;
	}
	
	@Override
	public String getName () {
		return "Simple Word Count";
	}

	@Override
	public String getDescription () {
		return "Annotates each text unit source with a total word count and gives total source word counts for batches"
			+ " Expects: filter events. Sends back: filter events.";
	}

	@Override
	public IParameters getParameters () {		
		return null; //params;
	}

	@Override
	public void setParameters (IParameters params) {	
		// this.params = (ParametersSimpleWordCountStep)params;
	}

	private long countWords (String text) {
		long totalWordCount = 0;
		int current = 0;
		RuleBasedBreakIterator wordIterator;

		if (Util.isEmpty(text)) {
			return totalWordCount;
		}
		
		wordIterator = srcWordIterator;		
		wordIterator.setText(text);

		while (true) {
			if (current == BreakIterator.DONE) {
				break;
			}

			current = wordIterator.next();
			// don't count various space and punctuation
			if ( wordIterator.getRuleStatus() != RuleBasedBreakIterator.WORD_NONE ) {
				totalWordCount++;
			}
		}

		return totalWordCount;
	}
}
