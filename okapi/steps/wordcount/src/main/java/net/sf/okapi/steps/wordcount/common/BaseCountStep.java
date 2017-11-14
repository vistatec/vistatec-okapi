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

package net.sf.okapi.steps.wordcount.common;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IResource;
import net.sf.okapi.common.IdGenerator;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.IWithAnnotations;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextUnit;
import net.sf.okapi.lib.extra.steps.AbstractPipelineStep;

/**
 * Base abstract class for different counter steps (word count step, character count step, etc.).
 * 
 * @version 0.1 08.07.2009
 */

public abstract class BaseCountStep extends AbstractPipelineStep {

//	protected enum CountContext {
//		CC_SOURCE,
//		CC_TARGET
//	}
	private Parameters params;
	private IdGenerator gen = new IdGenerator("ending");
	private TextContainer source;
	private StringBuilder sb;
	
	private long batchCount;
	private long batchItemCount;
	private long documentCount;
	private long subDocumentCount;
	private long groupCount;	
	
	public BaseCountStep() {
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
		batchCount = 0;
		batchItemCount = 0;
		documentCount = 0;
		subDocumentCount = 0;
		groupCount = 0;
	}

	//-------------------------
	abstract public String getName();
	abstract public String getDescription();
	abstract public String getMetric();
	abstract protected long count(TextContainer textContainer, LocaleId locale);
	abstract protected long count(Segment segment, LocaleId locale);
	abstract protected long countInTextUnit(ITextUnit textUnit);
	abstract protected boolean countOnlyTranslatable();
//	abstract protected CountContext getCountContext();

	protected void saveCount(Metrics metrics, long count) {
		if (metrics == null) return;
		
		metrics.setMetric(getMetric(), count);
	}

	public long getBatchCount() {
		return batchCount;
	}

	public long getBatchItemCount() {
		return batchItemCount;
	}

	public long getDocumentCount() {
		return documentCount;
	}

	public long getSubDocumentCount() {
		return subDocumentCount;
	}

	public long getGroupCount() {
		return groupCount;
	}

	protected void saveToMetrics(Event event, long count) {
		if (event == null) return;
		if (count == 0) return;
		
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

	protected void saveToMetrics(TextContainer textContainer, long count) {
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
	
	protected void saveToMetrics(Segment seg, long count) {		
		if (seg == null) return;
		if (count == 0) return;
		
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
		batchCount = 0;
		return event;
	}
	
	@Override
	protected Event handleEndBatch(Event event) {
		flushBuffer();
		
		if (!params.getCountInBatch()) return event;
		if (batchCount == 0) return event;
		
		saveToMetrics(event, batchCount);
		return event;
	}

	//-------------------------	
	@Override
	protected Event handleStartBatchItem(Event event) {
		batchItemCount = 0;
		return event;
	}
	
	@Override
	protected Event handleEndBatchItem(Event event) {
		flushBuffer();
		
		if (!params.getCountInBatchItems()) return event;
		if (batchItemCount == 0) return event;
		
		saveToMetrics(event, batchItemCount);
		return event;
	}

	//-------------------------	
	@Override
	protected Event handleStartDocument(Event event) {
		documentCount = 0;
		return super.handleStartDocument(event); // Sets language						
	}
	
	@Override
	protected Event handleEndDocument(Event event) {
		if (!params.getCountInDocuments()) return event;
		if (documentCount == 0) return event;
		
		saveToMetrics(event, documentCount);
		return event;
	}

	//-------------------------
	@Override
	protected Event handleStartSubDocument(Event event) {
		subDocumentCount = 0;
		return event;
	}
	
	@Override
	protected Event handleEndSubDocument(Event event) {
		if (!params.getCountInSubDocuments()) return event;
		if (subDocumentCount == 0) return event;
		
		saveToMetrics(event, subDocumentCount);
		return event;
	}
	
	//-------------------------
	@Override
	protected Event handleStartGroup(Event event) {
		groupCount = 0;
		return event;
	}
	
	@Override
	protected Event handleEndGroup(Event event) {		
		if (!params.getCountInGroups()) return event;
		if (groupCount == 0) return event;
		
		saveToMetrics(event, groupCount);
		return event;
	}

	//-------------------------
	
//	private long countInContainer(TextContainer tc) {
//		if (tc == null) return 0;
//		
//		// Individual segments metrics
//		long segmentCount;
//		long textContainerCount;
//		ISegments segs = tc.getSegments();
//		if (segs != null) {
//			for (Segment seg : segs) {
//				segmentCount = count(seg);
//				saveToMetrics(seg, segmentCount);
//			}
//		}
//		// TC metrics
//		textContainerCount = count(tc);
//		saveToMetrics(tc, textContainerCount);
//		return textContainerCount; 
//	}
	
//	private long countInSource(TextUnit tu) {
//		if (tu == null) return 0;
//		
//		// Individual segments metrics
//		long segmentCount;
//		long textContainerCount;
//		ISegments segs = tc.getSegments();
//		if (segs != null) {
//			for (Segment seg : segs) {
//				segmentCount = count(seg);
//				saveToMetrics(seg, segmentCount);
//			}
//		}
//		// TC metrics
//		textContainerCount = count(tc);
//		saveToMetrics(tc, textContainerCount);
//		return textContainerCount; 
//	}
//	
//	private long countInTarget(TextUnit tu, LocaleId targetLocale) {
//		if (tu == null) return 0;
//		
//		// Individual segments metrics
//		long segmentCount;
//		long textContainerCount;
//		ISegments segs = tc.getSegments();
//		if (segs != null) {
//			for (Segment seg : segs) {
//				segmentCount = count(seg);
//				saveToMetrics(seg, segmentCount);
//			}
//		}
//		// TC metrics
//		textContainerCount = count(tc);
//		saveToMetrics(tc, textContainerCount);
//		return textContainerCount; 
//	}
	
	
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
		long textUnitCount = countInTextUnit(tu);
		
		// Whole TU metrics
		if (textUnitCount != 0) {				
			saveToMetrics(event, textUnitCount); // Saves in annotations of the whole TU
					
			if (params.getCountInBatch()) batchCount += textUnitCount;
			if (params.getCountInBatchItems()) batchItemCount += textUnitCount;
			if (params.getCountInDocuments()) documentCount += textUnitCount;
			if (params.getCountInSubDocuments()) subDocumentCount += textUnitCount;
			if (params.getCountInGroups()) groupCount += textUnitCount;
		}
	}
	
	@Override
	protected Event handleTextUnit(Event event) {		
		ITextUnit tu = event.getTextUnit();
		
		if (tu.isEmpty()) return event;
		if (!tu.isTranslatable() && countOnlyTranslatable()) return event;
		
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
