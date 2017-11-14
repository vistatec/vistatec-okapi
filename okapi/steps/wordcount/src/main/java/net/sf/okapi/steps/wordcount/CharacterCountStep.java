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

package net.sf.okapi.steps.wordcount;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IResource;
import net.sf.okapi.common.IdGenerator;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.resource.ISegments;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.IWithAnnotations;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextUnit;
import net.sf.okapi.lib.extra.steps.AbstractPipelineStep;
import net.sf.okapi.steps.wordcount.CharacterCounter.Counts;
import net.sf.okapi.steps.wordcount.common.GMX;
import net.sf.okapi.steps.wordcount.common.Metrics;
import net.sf.okapi.steps.wordcount.common.MetricsAnnotation;
import net.sf.okapi.steps.wordcount.common.Parameters;

/**
 * Character Counter pipeline step. The counter counts a number of characters in translatable text units. 
 * The count results are placed in a MetricsAnnotation structure (with the GMX TotalCharacterCount,
 * WhiteSpaceCharacterCount, and PunctuationCharacterCount metrics set), attached to the respective
 * event's resource (TEXT_UNIT, END_DOCUMENT, END_BATCH, END_BATCH_ITEM, END_SUBDOCUMENT, END_GROUP).  
 * 
 */
@UsingParameters(Parameters.class)
public class CharacterCountStep extends AbstractPipelineStep {

	private Parameters params;
	private IdGenerator gen = new IdGenerator("ending");
	private TextContainer source;
	private StringBuilder sb;
	
	private Counts batchCount = new Counts();
	private Counts batchItemCount = new Counts();
	private Counts documentCount = new Counts();
	private Counts subDocumentCount = new Counts();
	private Counts groupCount = new Counts();	
	
	public CharacterCountStep() {
		super();
		params = new Parameters();
		setParameters(params);
		setName(getName());
		setDescription(getName());
	}
	
	@Override
	protected void component_init() {
		params = getParameters(Parameters.class);
		
		// Reset counters
		batchCount = new Counts();
		batchItemCount = new Counts();
		documentCount = new Counts();
		subDocumentCount = new Counts();
		groupCount = new Counts();
	}

	@Override
	public String getDescription() {
		return "Count the number of characters in the text units of a set of documents or/and in its parts."
		+ " Expects: filter events. Sends back: filter events.";
	}

	@Override
	public String getName() {
		return "Character Count";
	}

	private Counts count(TextContainer textContainer, LocaleId locale) {
		return CharacterCounter.fullCount(textContainer, locale);
	}
			
	private Counts count(Segment segment, LocaleId locale) {
		return CharacterCounter.fullCount(segment, locale);		
	}
	
	private Counts countInTextUnit(ITextUnit textUnit) {
		if (textUnit == null) return new Counts();
		
		LocaleId srcLocale = getSourceLocale();		
		TextContainer source = textUnit.getSource();
		
		// Individual segments metrics
		Counts segCount = new Counts();
		Counts segmentsCount = new Counts();
		Counts textContainerCount = new Counts();
		
		ISegments segs = source.getSegments();
		if (segs != null) {
			for (Segment seg : segs) {
				segCount = count(seg, srcLocale);
				segmentsCount = segmentsCount.add(segCount);
				saveToMetrics(seg, segCount);
			}
		}
		// TC metrics
		textContainerCount = count(source, srcLocale);
		saveToMetrics(source, textContainerCount);
		
		if (!textContainerCount.isAllZeros()) return textContainerCount;  
		if (!segmentsCount.isAllZeros()) return segmentsCount;
		return new Counts();
	}
	
	protected void saveCount(Metrics metrics, Counts count) {
		if (metrics == null || count == null) return;
		
		metrics.setMetric(GMX.TotalCharacterCount, count.total);
		metrics.setMetric(GMX.PunctuationCharacterCount, count.punctuation);
		metrics.setMetric(GMX.WhiteSpaceCharacterCount, count.whiteSpace);
		metrics.setMetric(GMX.OverallCharacterCount, count.total + count.punctuation + count.whiteSpace);
	}

	public Counts getBatchCount() {
		return batchCount;
	}

	public Counts getBatchItemCount() {
		return batchItemCount;
	}

	public Counts getDocumentCount() {
		return documentCount;
	}

	public Counts getSubDocumentCount() {
		return subDocumentCount;
	}

	public Counts getGroupCount() {
		return groupCount;
	}

	protected void saveToMetrics(Event event, Counts count) {
		if (event == null || count == null) return;
		if (count.isAllZeros()) return;
		
		IWithAnnotations res = (IWithAnnotations) event.getResource();
		if (res == null) {
			res = (IWithAnnotations) createResource(event);
		}
		if (res == null) return;
		
		MetricsAnnotation ma = res.getAnnotation(MetricsAnnotation.class);
		
		if (ma == null) {			
			ma = new MetricsAnnotation();
			res.setAnnotation(ma);
		}
		
		Metrics m = ma.getMetrics();		
		if (m == null) return;
					
		saveCount(m, count);
	}
	
	protected void removeFromMetrics(IWithAnnotations res, String metricName) {
		MetricsAnnotation ma = res.getAnnotation(MetricsAnnotation.class);
		
		if (ma == null) {			
			ma = new MetricsAnnotation();
			res.setAnnotation(ma);
		}
		
		Metrics m = ma.getMetrics();		
		if (m == null) return;
		m.unregisterMetric(metricName);
	}
	
	protected void removeFromMetrics(TextContainer textContainer, String metricName) {
		if (textContainer == null) return;
		
		MetricsAnnotation ma = textContainer.getAnnotation(MetricsAnnotation.class);
		
		if (ma == null) {			
			ma = new MetricsAnnotation();
			textContainer.setAnnotation(ma);
		}
		
		Metrics m = ma.getMetrics();		
		if (m == null) return;
					
		m.unregisterMetric(metricName);
	}
	
	protected void removeFromMetrics(Segment seg, String metricName) {		
		if (seg == null) return;
		
		MetricsAnnotation ma = seg.getAnnotation(MetricsAnnotation.class);
		
		if (ma == null) {			
			ma = new MetricsAnnotation();
			seg.setAnnotation(ma);
		}
		
		Metrics m = ma.getMetrics();		
		if (m == null) return;
					
		m.unregisterMetric(metricName);
	}
	
	private IResource createResource(Event event) {
		if (event == null) return null;
		
		IResource res = event.getResource();
		if (res != null) return res;
		
		switch (event.getEventType()) {
		case END_BATCH:
		case END_BATCH_ITEM:
		case END_DOCUMENT:
		case END_SUBDOCUMENT:
		case END_GROUP:
			res = new Ending(gen.createId());
			event.setResource(res);
			break;
		default:
			break;			
		}
		
		return res;
	}

	protected void saveToMetrics(TextContainer textContainer, Counts count) {
		if (textContainer == null) return;
		//if (count == 0) return;
				
		MetricsAnnotation ma = textContainer.getAnnotation(MetricsAnnotation.class);
		
		if (ma == null) {			
			ma = new MetricsAnnotation();
			textContainer.setAnnotation(ma);
		}
		
		Metrics m = ma.getMetrics();		
		if (m == null) return;
					
		saveCount(m, count);
	}
	
	protected void saveToMetrics(Segment seg, Counts count) {		
		if (seg == null || count == null) return;
		if (count.isAllZeros()) return;
		
		MetricsAnnotation ma = seg.getAnnotation(MetricsAnnotation.class);
		
		if (ma == null) {			
			ma = new MetricsAnnotation();
			seg.setAnnotation(ma);
		}
		
		Metrics m = ma.getMetrics();		
		if (m == null) return;
					
		saveCount(m, count);
	}
	
	//-------------------------	
	@Override
	protected Event handleStartBatch(Event event) {
		batchCount = new Counts();
		return event;
	}
	
	@Override
	protected Event handleEndBatch(Event event) {
		flushBuffer();
		
		if (!params.getCountInBatch()) return event;
		if (batchCount.isAllZeros()) return event;
		
		saveToMetrics(event, batchCount);
		return event;
	}

	//-------------------------	
	@Override
	protected Event handleStartBatchItem(Event event) {
		batchItemCount = new Counts();
		return event;
	}
	
	@Override
	protected Event handleEndBatchItem(Event event) {
		flushBuffer();
		
		if (!params.getCountInBatchItems()) return event;
		if (batchItemCount.isAllZeros()) return event;
		
		saveToMetrics(event, batchItemCount);
		return event;
	}

	//-------------------------	
	@Override
	protected Event handleStartDocument(Event event) {
		documentCount = new Counts();
		return super.handleStartDocument(event); // Sets language						
	}
	
	@Override
	protected Event handleEndDocument(Event event) {
		if (!params.getCountInDocuments()) return event;
		if (documentCount.isAllZeros()) return event;
		
		saveToMetrics(event, documentCount);
		return event;
	}

	//-------------------------
	@Override
	protected Event handleStartSubDocument(Event event) {
		subDocumentCount = new Counts();
		return event;
	}
	
	@Override
	protected Event handleEndSubDocument(Event event) {
		if (!params.getCountInSubDocuments()) return event;
		if (subDocumentCount.isAllZeros()) return event;
		
		saveToMetrics(event, subDocumentCount);
		return event;
	}
	
	//-------------------------
	@Override
	protected Event handleStartGroup(Event event) {
		groupCount = new Counts();
		return event;
	}
	
	@Override
	protected Event handleEndGroup(Event event) {		
		if (!params.getCountInGroups()) return event;
		if (groupCount.isAllZeros()) return event;
		
		saveToMetrics(event, groupCount);
		return event;
	}
	
	protected TextContainer getSource() {
		return source;
	}
	
	private void flushBuffer() {
		if (params.getBufferSize() == 0) return; 
		if (sb == null) return;
		
		ITextUnit tu = new TextUnit("temp", sb.toString());
		sb = null;
		updateCounts(tu, null);
	}
	
	private void updateCounts(ITextUnit tu, Event event) {
		Counts textUnitCount = countInTextUnit(tu);
		
		// Whole TU metrics
		if (!textUnitCount.isAllZeros()) {				
			saveToMetrics(event, textUnitCount); // Saves in annotations of the whole TU
					
			if (params.getCountInBatch()) batchCount = batchCount.add(textUnitCount);
			if (params.getCountInBatchItems()) batchItemCount = batchItemCount.add(textUnitCount);
			if (params.getCountInDocuments()) documentCount = documentCount.add(textUnitCount);
			if (params.getCountInSubDocuments()) subDocumentCount = subDocumentCount.add(textUnitCount);
			if (params.getCountInGroups()) groupCount = groupCount.add(textUnitCount);
		}
	}
	
	@Override
	protected Event handleTextUnit(Event event) {		
		ITextUnit tu = event.getTextUnit();
		
		if (tu.isEmpty()) return event;
		//if (!tu.isTranslatable() && countOnlyTranslatable()) return event;
		
		source = tu.getSource();
		
		if (params.getBufferSize() > 0) {
			if (sb == null) {
				sb = new StringBuilder(params.getBufferSize());
			}
			// Non-translatable text doesn't get here
			String srcText = tu.getSource().getUnSegmentedContentCopy().getText();
			sb.append(srcText);
			if (sb.length() >= params.getBufferSize()) {
				flushBuffer();
			}
			return event;
		}
		
		updateCounts(tu, event);
		return event;
	}		
}
