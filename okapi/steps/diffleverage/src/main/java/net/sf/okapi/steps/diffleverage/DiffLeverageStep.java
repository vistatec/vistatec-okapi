/*===========================================================================
  Copyright (C) 2010-2011 by the Okapi Framework contributors
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

package net.sf.okapi.steps.diffleverage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.IResource;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.annotation.AltTranslation;
import net.sf.okapi.common.annotation.AltTranslationsAnnotation;
import net.sf.okapi.common.annotation.Annotations;
import net.sf.okapi.common.exceptions.OkapiBadStepInputException;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filters.IFilterConfigurationMapper;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
import net.sf.okapi.common.pipeline.annotations.StepParameterType;
import net.sf.okapi.common.query.MatchType;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.MultiEvent;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextUnitUtil;
import net.sf.okapi.lib.extra.diff.incava.DiffLists;
import net.sf.okapi.lib.search.lucene.analysis.AlphabeticNgramTokenizer;
import net.sf.okapi.lib.search.lucene.scorer.Util;

/**
 * Contextually match source "paragraphs" (full content of the TextUnit source) between two documents using a standard diff algorithm
 * (http://en.wikipedia.org/wiki/Diff). The result is a new document with the translations from the old document copied
 * into it. This allows translations between different document versions to be preserved while still maintaining the
 * newer source document modifications.
 * 
 * </br></br>Adds these {@link Annotations}:
 * <li> {@link AltTranslationsAnnotation} on the target container.
 * <li> {@link DiffMatchAnnotation} on the target container (only applied if diffOnly is true)</br>
 * 
 * @author HARGRAVEJE
 * 
 */
@UsingParameters(Parameters.class)
public class DiffLeverageStep extends BasePipelineStep {
	private static final int NGRAM_SIZE = 3;

	private Parameters params;	
	private IFilterConfigurationMapper fcMapper;
	private RawDocument oldSource;
	private RawDocument oldTarget;
	private List<ITextUnit> newTextUnits;
	private List<ITextUnit> oldTextUnits;
	private List<Event> newDocumentEvents;
	private LocaleId sourceLocale;
	private LocaleId targetLocale;
	private boolean done = true;
	private Comparator<ITextUnit> sourceComparator;
	private AlphabeticNgramTokenizer tokenizer;

	public DiffLeverageStep() {
		params = new Parameters();
	}

	/**
	 * 
	 * @param fcMapper
	 */
	@StepParameterMapping(parameterType = StepParameterType.FILTER_CONFIGURATION_MAPPER)
	public void setFilterConfigurationMapper(final IFilterConfigurationMapper fcMapper) {
		this.fcMapper = fcMapper;
	}

	/**
	 * 
	 * @param sourceLocale
	 */
	@StepParameterMapping(parameterType = StepParameterType.SOURCE_LOCALE)
	public void setSourceLocale(final LocaleId sourceLocale) {
		this.sourceLocale = sourceLocale;
	}

	/**
	 * Target locale.
	 * @param targetLocale
	 */
	@SuppressWarnings("deprecation")
	@StepParameterMapping(parameterType = StepParameterType.TARGET_LOCALE)
	public void setTargetLocale(final LocaleId targetLocale) {
		this.targetLocale = targetLocale;
	}

	/**
	 * This is the old document (previously translated)
	 * 
	 * @param secondInput
	 */
	@StepParameterMapping(parameterType = StepParameterType.SECOND_INPUT_RAWDOC)
	public void setSecondInput(final RawDocument secondInput) {
		this.oldSource = secondInput;
	}
	
	/**
	 * If set this is the old target that will be paragraph aligned with the  old source.
	 * @param tertiaryInput
	 */
	@StepParameterMapping(parameterType = StepParameterType.THIRD_INPUT_RAWDOC)
	public void setTertiaryInput (RawDocument tertiaryInput) {
		this.oldTarget = tertiaryInput;
	}

	@Override
	public String getDescription() {
		return "Compare two source documents (i.e., different versions) and " +
				"copy the old target content when we find a match. Can be a monolingual " +
				"and bi-lingual input or three monolingual inputs. Paragraphs (TextUnits) " +
				"must align in all cases";
	}

	@Override
	public String getName() {
		return "Diff Leverage";
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
	protected Event handleStartBatch(final Event event) {
		done = true;
		if (params.getFuzzyThreshold() >= 100) {
			// exact match
			sourceComparator = new TextUnitComparator(params.isCodesensitive());
		} else {
			// fuzzy match
			tokenizer = Util.createNgramTokenizer(NGRAM_SIZE, sourceLocale);
			sourceComparator = new FuzzyTextUnitComparator(params.isCodesensitive(), params
					.getFuzzyThreshold(), sourceLocale);
		}
		return event;
	}

	@Override
	protected Event handleEndBatch(final Event event) {
		return event;
	}

	@Override
	protected Event handleRawDocument(final Event event) {
		throw new OkapiBadStepInputException(
				"Encountered a RAW_DOCUMENT event. Expected a filtered event stream.");
	}

	@Override
	protected Event handleStartDocument(final Event event) {
		// test if we have an alignment at the document level
		if (oldSource != null) {
			done = false;
			// intialize buffers for a new document
			newTextUnits = new ArrayList<ITextUnit>();
			oldTextUnits = new ArrayList<ITextUnit>();
			newDocumentEvents = new LinkedList<Event>();

			// open of the secondary input file (this is our old document)
			getOldDocumentTextUnits();
		}
		return event;
	}

	@Override
	protected Event handleEndDocument(final Event event) {
		done = true;
		if (oldSource != null) {
			// diff and leverage (copy target segments) the old and new lists of TextUnits
			diffLeverage();

			// the diff leverage is over now send the cached events down the
			// pipeline as a MULTI_EVENT
			// add the end document event so its not eaten
			newDocumentEvents.add(event);

			// create a multi event and pass it on to the other steps
			Event multi_event = new Event(EventType.MULTI_EVENT, new MultiEvent(newDocumentEvents));

			// help java gc
			newTextUnits = null;
			oldTextUnits = null;
			newDocumentEvents = null;
			return multi_event;
		} else {
			return event;
		}
	}

	@Override
	protected Event handleStartSubDocument(final Event event) {
		if (oldSource != null) {
			newDocumentEvents.add(event);
			return Event.NOOP_EVENT;
		} else {
			return event;
		}
	}

	@Override
	protected Event handleEndSubDocument(final Event event) {
		if (oldSource != null) {
			newDocumentEvents.add(event);
			return Event.NOOP_EVENT;
		} else {
			return event;
		}
	}

	@Override
	protected Event handleStartGroup(final Event event) {
		if (oldSource != null) {
			newDocumentEvents.add(event);
			return Event.NOOP_EVENT;
		} else {
			return event;
		}
	}

	@Override
	protected Event handleEndGroup(final Event event) {
		if (oldSource != null) {
			newDocumentEvents.add(event);
			return Event.NOOP_EVENT;
		} else {
			return event;
		}
	}

	@Override
	protected Event handleTextUnit(final Event event) {
		if (event.getTextUnit().getSource().hasBeenSegmented()) {
			throw new OkapiBadStepInputException("DiffLeverageStep only aligns unsegmented TextUnits");
		}
		
		if (oldSource != null) {
			newTextUnits.add(event.getTextUnit());
			newDocumentEvents.add(event);
			return Event.NOOP_EVENT;
		} else {
			return event;
		}
	}

	@Override
	protected Event handleDocumentPart(final Event event) {
		if (oldSource != null) {
			newDocumentEvents.add(event);
			return Event.NOOP_EVENT;
		} else {
			return event;
		}
	}

	@Override
	public boolean isDone() {
		return done;
	}

	private void getOldDocumentTextUnits() {
		IFilter trgFilter = null;
		// Initialize the filter to read the translation to compare
		try (IFilter srcFilter = fcMapper.createFilter(oldSource.getFilterConfigId(), null)) {
			
			if (oldTarget != null) {
				trgFilter = fcMapper.createFilter(oldSource.getFilterConfigId(), null);
				// Open the tertiary input for this batch item (old target)
				trgFilter.open(oldTarget);
			}			
			
			// Open the second input for this batch item (old source)
			srcFilter.open(oldSource);

			while (srcFilter.hasNext()) {
				final Event event = srcFilter.next();
				if (event.getEventType() == EventType.TEXT_UNIT) {
					ITextUnit tu = event.getTextUnit();
					if (oldTarget != null) {
						Event e = synchronize(trgFilter, EventType.TEXT_UNIT);							
						tu.setTarget(targetLocale, e.getTextUnit().getSource());
					}
					oldTextUnits.add(tu);
				}
			}
		} finally {
			if (trgFilter != null) {
				trgFilter.close();
			}
		}
	}
	
	private Event synchronize(IFilter filter, EventType untilType) {
		boolean found = false;
		Event event = null;
		while (!found && filter.hasNext()) {
			event = filter.next();
			found = (event.getEventType() == untilType);
			if (event.isTextUnit()) {
				if (event.getTextUnit().getSource().hasBeenSegmented()) {
					throw new OkapiBadStepInputException("DiffLeverageStep only aligns unsegmented TextUnits");
				}
			}
		}
		if (!found) {
			throw new OkapiException(
					"Different number of source or target TextUnits. " +
					"The source and target documents are not paragraph aligned.");
		}
		return event;
	}

	private void diffLeverage() {
		DiffLists<ITextUnit> diffTextUnits;

		diffTextUnits = new DiffLists<ITextUnit>(oldTextUnits, newTextUnits, sourceComparator);

		// diff the two TextUnit lists based on the provided Comparator
		diffTextUnits.diff();

		// loop through the matches and copy over the old target to the new TextUnit
		for (Map.Entry<Integer, Integer> m : diffTextUnits.getMatches().entrySet()) {
			ITextUnit oldTu = oldTextUnits.get(m.getKey());
			ITextUnit newTu = newTextUnits.get(m.getValue());
			int score = 100;

			// copy the old translation to the new TextUnit
			TextContainer otc = null;
			if ((otc = oldTu.getTarget(targetLocale)) != null) {
				// only copy the old target if diffOnly is false
				if (!params.isDiffOnly()) {
					if (params.getFuzzyThreshold() < 100) {
						score = (int) Util.calculateNgramDiceCoefficient(
								oldTu.getSource().getFirstContent().toString(), 
								newTu.getSource().getFirstContent().toString(), tokenizer);
					}
					
					// We force the source to be a paragraph!! We use getUnSegmentedContentCopy
					// to make sure we get *all* TextParts (just in case segmentation has been applied
					// or  somehow extra TextParts were added in an external process)
					
					// align codes and copy source code data to target										
					newTu.getSource().getFirstContent().alignCodeIds(otc.getFirstContent());					
					TextFragment atf = TextUnitUtil.copySrcCodeDataToMatchingTrgCodes(						
							newTu.getSource().getFirstContent(),
							otc.getFirstContent(), 
							true, false, null, newTu);
					otc.setContent(atf);
				
					if (params.isCopyToTarget()) {
						newTu.setTarget(targetLocale, otc);
					}

					// make an AltTranslation and attach to the target container
					AltTranslation alt = new AltTranslation(sourceLocale, targetLocale, 
							newTu.getSource().getUnSegmentedContentCopy(), 
							oldTu.getSource().getUnSegmentedContentCopy(), 
							otc.getUnSegmentedContentCopy(), 
							params.getFuzzyThreshold() >= 100 ? MatchType.EXACT_PREVIOUS_VERSION
									: MatchType.FUZZY_PREVIOUS_VERSION, score, getName());
									
					// add the annotation to the target container since we are diffing paragraphs only
					// we may need to create the target if it doesn't exist
					TextContainer ntc = newTu.createTarget(targetLocale, false, IResource.COPY_PROPERTIES);
					AltTranslationsAnnotation alta = TextUnitUtil.addAltTranslation(ntc, alt);
					// resort AltTranslation in case we already had some in  the list
					alta.sort();
				}
				
				// set the DiffLeverageAnnotation
				// we may need to create the target if it doesn't exist
				TextContainer tc = newTu.createTarget(targetLocale, false, IResource.COPY_PROPERTIES);
				tc.setAnnotation(new DiffMatchAnnotation());
			}
		}
	}
}
