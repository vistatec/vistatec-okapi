/*===========================================================================
  Copyright (C) 2009-2011 by the Okapi Framework contributors
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

package net.sf.okapi.steps.paraaligner;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.exceptions.OkapiBadStepInputException;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filters.IFilterConfigurationMapper;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
import net.sf.okapi.common.pipeline.annotations.StepParameterType;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.MultiEvent;
import net.sf.okapi.common.resource.PipelineParameters;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.simplifier.ResourceSimplifier;
import net.sf.okapi.lib.extra.diff.incava.DiffLists;
import net.sf.okapi.steps.gcaligner.AlignmentScorer;
import net.sf.okapi.steps.gcaligner.GaleAndChurch;
import net.sf.okapi.steps.sentencealigner.SentenceAlignerStep;

/**
 * Align paragraphs (TextUnits) between a source and target document. Uses inter-paragraph
 * formatting and other heuristics to align paragraphs. TextUnits from this step can be sent the the
 * {@link SentenceAlignerStep} for more fine grained alignment. <b>TextUnits should not be
 * segmented.</b>
 * 
 * @author HARGRAVEJE
 */
@UsingParameters(Parameters.class)
public class ParagraphAlignerStep extends BasePipelineStep {
//	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	private Parameters params;
	private IFilter filter = null;
	private IFilterConfigurationMapper fcMapper;
	private LocaleId targetLocale;
	private LocaleId sourceLocale;
	private List<Event> srcEvents;
	private List<Event> trgEvents;
	private List<Event> textUnitEvents;
	private RawDocument targetInput = null;
	private EventComparator comparator;
	private ParagraphAligner paragraphAligner;
	ResourceSimplifier sourceSimplifier; 

	public ParagraphAlignerStep() {
		params = new Parameters();
		List<AlignmentScorer<ITextUnit>> scorerList = new LinkedList<AlignmentScorer<ITextUnit>>();
		scorerList.add(new GaleAndChurch<ITextUnit>());
		paragraphAligner = new ParagraphAligner(scorerList);
	}

	@StepParameterMapping(parameterType = StepParameterType.FILTER_CONFIGURATION_MAPPER)
	public void setFilterConfigurationMapper(IFilterConfigurationMapper fcMapper) {
		this.fcMapper = fcMapper;
	}

	@StepParameterMapping(parameterType = StepParameterType.SOURCE_LOCALE)
	public void setSourceLocale(LocaleId sourceLocale) {
		this.sourceLocale = sourceLocale;
	}

	@SuppressWarnings("deprecation")
	@StepParameterMapping(parameterType = StepParameterType.TARGET_LOCALE)
	public void setTargetLocale(LocaleId targetLocale) {
		this.targetLocale = targetLocale;
	}

	@StepParameterMapping(parameterType = StepParameterType.SECOND_INPUT_RAWDOC)
	public void setSecondInput(RawDocument secondInput) {
		this.targetInput = secondInput;
	}

	@Override
	public String getName() {
		return "Paragraph Alignment";
	}

	@Override
	public String getDescription() {
		return "Align paragraphs (text units) between a source and a target document. Only TextUnit events are passed along - all other events are lost";
	}

	@Override
	public IParameters getParameters() {
		return params;
	}

	@Override
	public void setParameters(IParameters params) {
		this.params = (Parameters) params;
	}

	@Override
	protected Event handleStartBatch(Event event) {
		return event;
	}

	@Override
	protected Event handleEndBatch(Event event) {
		return event;
	}

	@Override
	protected Event handleStartDocument(Event event) {
		if (targetInput != null) {
			trgEvents = new ArrayList<Event>();
			initializeFilter();
		}
		srcEvents = new ArrayList<Event>();
		textUnitEvents = new ArrayList<Event>();
		comparator = new EventComparator();
		sourceSimplifier = new ResourceSimplifier(sourceLocale);
		return eventIndicatingTargetWasConsumed(event);
	}

	@Override
	protected Event handleEndDocument(Event event) {
		srcEvents.addAll(sourceSimplifier.convertToList(event));	
		
		// align skeleton chunks
		DiffLists<Event> skeletonAlignments = skeletonAlign();
		
		// align paragraphs (TextUnits) between aligned skeleton using G&C
		paragraphAlign(skeletonAlignments);

		// the diff leverage is over now send the cached events down the
		// pipeline as a MULTI_EVENT
		// add the end document event so its not eaten		

		// create a multi event and pass it on to the other steps
		textUnitEvents.add(event);
		Event multiEvent = new Event(EventType.MULTI_EVENT, new MultiEvent(textUnitEvents));

		if (filter != null) {
			filter.close();
		}

		srcEvents.clear();
		srcEvents = null;
		trgEvents.clear();
		trgEvents = null;

		return multiEvent;
	}

	@Override
	protected Event handleDocumentPart(final Event event) {
		srcEvents.addAll(sourceSimplifier.convertToList(event));
		return Event.NOOP_EVENT;
	}

	@Override
	protected Event handleStartSubDocument(final Event event) {
		srcEvents.addAll(sourceSimplifier.convertToList(event));
		return Event.NOOP_EVENT;
	}

	@Override
	protected Event handleEndSubDocument(final Event event) {
		srcEvents.addAll(sourceSimplifier.convertToList(event));
		return Event.NOOP_EVENT;
	}

	@Override
	protected Event handleStartGroup(final Event event) {
		srcEvents.addAll(sourceSimplifier.convertToList(event));
		return Event.NOOP_EVENT;
	}

	@Override
	protected Event handleEndGroup(final Event event) {
		srcEvents.addAll(sourceSimplifier.convertToList(event));
		return Event.NOOP_EVENT;
	}

	@Override
	protected Event handleTextUnit(Event sourceEvent) {
		srcEvents.addAll(sourceSimplifier.convertToList(sourceEvent));
		return Event.NOOP_EVENT;
	}

	private void initializeFilter() {
		if (targetInput == null) {
			throw new OkapiBadStepInputException("No target document found.");
		}

		// Initialize the filter to read the translation to compare
		filter = fcMapper.createFilter(targetInput.getFilterConfigId(), null);
		// Open the second input for this batch item
		filter.open(targetInput);
		// populate target Event list
		filterTarget();
	}

	private void filterTarget() {
		Event event = null;
		ResourceSimplifier simplifier = new ResourceSimplifier(targetLocale); 
		while (filter.hasNext()) {
			event = filter.next();
			trgEvents.addAll(simplifier.convertToList(event));
		}
	}

	private DiffLists<Event> skeletonAlign() {
		DiffLists<Event> diffEvents = null;
		
		if (!params.isUseSkeletonAlignment()) {
			// don't use skeleton diffing to anchor alignments, return empty diffEvents
			return diffEvents;
		}

		// diff the two Event lists based on the provided Comparator
		// find matching skeleton pairs
		diffEvents = new DiffLists<Event>(srcEvents, trgEvents, comparator);
		diffEvents.diff();
		return diffEvents;
	}
	
	private void paragraphAlign(DiffLists<Event> skeletonAlignments) {
		// check if we have any matches
		Map<Integer, Integer> matches;
		Iterator<Map.Entry<Integer, Integer>> it = null;
		Map.Entry<Integer, Integer> m;
		if (skeletonAlignments != null) {
			matches = skeletonAlignments.getMatches();
			it = matches.entrySet().iterator();
		}
				
		int srcStartMatchIndex = 0;
		int trgStartMatchIndex = 0;
		int srcEndMatchIndex = srcEvents.size();
		int trgEndMatchIndex = trgEvents.size();
		
		// first skeleton alignment
		if (it != null && it.hasNext()) {
			m = it.next();
			srcEndMatchIndex = m.getKey();
			trgEndMatchIndex = m.getValue();
			
			addAlignedTextUnits(srcEvents.subList(srcStartMatchIndex, srcEndMatchIndex), 
					trgEvents.subList(trgStartMatchIndex, trgEndMatchIndex));
			
			srcStartMatchIndex = srcEndMatchIndex;
			trgStartMatchIndex = trgEndMatchIndex;
		}
		
		
		// iterate over remaining matches and align TU's between them
		while(it != null && it.hasNext()) {
			m = it.next();
			srcEndMatchIndex = m.getKey();
			trgEndMatchIndex = m.getValue();
			
			addAlignedTextUnits(srcEvents.subList(srcStartMatchIndex, srcEndMatchIndex), 
					trgEvents.subList(trgStartMatchIndex, trgEndMatchIndex));
			
			srcStartMatchIndex = srcEndMatchIndex;
			trgStartMatchIndex = trgEndMatchIndex;				
		}
				
		// handle the remaining TU's after the last skeleton match
		srcEndMatchIndex = srcEvents.size();
		trgEndMatchIndex = trgEvents.size();
		addAlignedTextUnits(srcEvents.subList(srcStartMatchIndex, srcEndMatchIndex), 
				trgEvents.subList(trgStartMatchIndex, trgEndMatchIndex));
	}
	
	private void addAlignedTextUnits(List<Event> ses, List<Event> tes) {
		List<ITextUnit> stus = filterOutNonTextUnit(ses); 
		List<ITextUnit> ttus = filterOutNonTextUnit(tes);
		
		if (!stus.isEmpty() && !ttus.isEmpty()) {
			textUnitEvents.addAll(textUnitsToEvents(alignTus(stus, ttus)));
		}
	}
	
	private List<ITextUnit> filterOutNonTextUnit(List<Event> events) {
		List<ITextUnit> tus = new LinkedList<ITextUnit>();		
		// pull out any ITextUnits into a separate list
		for (Event e : events) {
			if (e.isTextUnit()) {
				tus.add(e.getTextUnit());
			}
		}		
		return tus;
	}
	
	private List<ITextUnit> alignTus(List<ITextUnit> stus, List<ITextUnit> ttus) {
		AlignedParagraphs preAlignedTus = paragraphAligner.align(stus, ttus, sourceLocale, targetLocale, params.isOutputOneToOneMatchesOnly());
		List<ITextUnit> alignedTus = preAlignedTus.align();		
		
		return alignedTus;
	}
	
	private Event eventIndicatingTargetWasConsumed(Event startDocEvent)
    {
		List<Event> list = new ArrayList<Event>();
		// Change the pipeline parameters for the raw-document-related data
		PipelineParameters pp = new PipelineParameters();
		pp.setSecondInputRawDocument(null);
		// Add the PipelineParameters event to the list
		list.add(new Event(EventType.PIPELINE_PARAMETERS, pp));
		// Add the original StartDocument event to the list
		list.add(startDocEvent);
		srcEvents.addAll(sourceSimplifier.convertToList(startDocEvent));
		// Return the list as a multiple-event event
		// now the StartDocument event in subsequent steps won't try to read the target
		return new Event(EventType.MULTI_EVENT, new MultiEvent(list));
    }
	
	private List<Event> textUnitsToEvents(List<ITextUnit> tus) {
		Event event;
		List<Event>events = new ArrayList<Event>();
		Iterator<ITextUnit> it = tus.iterator();
		while(it.hasNext()) {
			event = new Event(EventType.TEXT_UNIT, it.next());
			events.add(event);
		}
		return events;
	}
}
