/*===========================================================================
  Copyright (C) 2009-2013 by the Okapi Framework contributors
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

package net.sf.okapi.steps.leveraging;

import java.util.LinkedList;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.annotation.AltTranslation;
import net.sf.okapi.common.annotation.AltTranslationsAnnotation;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
import net.sf.okapi.common.pipeline.annotations.StepParameterType;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.MultiEvent;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.lib.translation.ITMQuery;
import net.sf.okapi.steps.diffleverage.DiffMatchAnnotation;

public class BatchTmLeveragingStep extends BasePipelineStep {
	private static final int BATCH_LEVERAGE_MAX = 30;

	private List<Event> batchedEvents;
	private int tuEventCount;
	private LocaleId sourceLocale;
	private LocaleId targetLocale;
	private Parameters params;
	private ITMQuery connector;

	private String rootDir;

	public BatchTmLeveragingStep() {
		params = new Parameters();
		batchedEvents = new LinkedList<Event>();
	}

	@StepParameterMapping(parameterType = StepParameterType.SOURCE_LOCALE)
	public void setSourceLocale(LocaleId sourceLocale) {
		this.sourceLocale = sourceLocale;
	}
	
	public LocaleId getSourceLocale() {
		return sourceLocale;
	}

	@SuppressWarnings("deprecation")
	@StepParameterMapping(parameterType = StepParameterType.TARGET_LOCALE)
	public void setTargetLocale(LocaleId targetLocale) {
		this.targetLocale = targetLocale;
	}

	public LocaleId getTargetLocale() {
		return targetLocale;
	}
	
	@StepParameterMapping(parameterType = StepParameterType.ROOT_DIRECTORY)
	public void setRootDirectory(String rootDir) {
		this.rootDir = rootDir;
	}
	
	public String getRootDirectory() {
		return rootDir;
	}

	@Override
	public String getName() {
		return "Simple Batch Leveraging Step (Beta)";
	}

	@Override
	public String getDescription() {
		return "Simple and fast batch leveraging step that delegates to connectors";
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
	public Event handleEvent(Event event) {
		switch (event.getEventType()) {
		case TEXT_UNIT:
			ITextUnit tu = event.getTextUnit();
			batchedEvents.add(event);

			if (!canLeverageTu(tu)) {
				return Event.NOOP_EVENT;
			}

			handleTextUnit(event);
			break;
		case START_BATCH_ITEM:
			event = handleStartBatchItem(event);
			return event;
		case END_BATCH_ITEM:
			event = handleEndBatchItem(event);
			return event;
		case START_BATCH:
			event = handleStartBatch(event);
			return event;
		case END_BATCH:
			event = handleEndBatch(event);
			return event;
		case END_DOCUMENT:
			event = handleEndDocument(event);
			return event;
		case START_DOCUMENT:
			event = handleEndDocument(event);
			return event;
		default:
			batchedEvents.add(event);
			break;
		}
		return Event.NOOP_EVENT;
	}

	@Override
	protected Event handleTextUnit(Event event) {
		// if we get here then it really is a TU we care to leverage
		tuEventCount++;
		if (tuEventCount >= BATCH_LEVERAGE_MAX) {
			tuEventCount = 0;
			batchLeverage();
			MultiEvent me = new MultiEvent();
			for (Event e : batchedEvents) {
				me.addEvent(e);
			}
			batchedEvents.clear();
			return new Event(EventType.MULTI_EVENT, me);
		}

		return Event.NOOP_EVENT;
	}

	@Override
	protected Event handleStartBatch(Event event) {
		tuEventCount = 0;

		try {
			connector = (ITMQuery) Class.forName(params.getResourceClassName()).newInstance();
		} catch (InstantiationException e) {
			throw new OkapiException("Error creating connector.", e);
		} catch (IllegalAccessException e) {
			throw new OkapiException("Error creating connector.", e);
		} catch (ClassNotFoundException e) {
			throw new OkapiException("Error creating connector.", e);
		}

		IParameters connectorParams = connector.getParameters();
		if (connectorParams != null) { // Set the parameters only if the connector takes them
			connectorParams.fromString(params.getResourceParameters());
		}

		connector.setRootDirectory(rootDir); // Before open()
		connector.setParameters(connectorParams);
		connector.open();
		if ((sourceLocale != null) && (targetLocale != null)) {
			connector.setLanguages(sourceLocale, targetLocale);
		}

		connector.setThreshold(params.getThreshold());
		connector.setMaximumHits(5);

		return event;
	}

	@Override
	protected Event handleEndDocument(Event event) {
		tuEventCount = 0;

		// leverage any remaining batched TextUnits for this document
		if (!batchedEvents.isEmpty()) {
			batchLeverage();
			MultiEvent me = new MultiEvent();
			for (Event e : batchedEvents) {
				me.addEvent(e);
			}
			batchedEvents.clear();

			// add END DOCUMENT event
			me.addEvent(event);
			return new Event(EventType.MULTI_EVENT, me);
		}

		return event;
	}

	private boolean canLeverageTu(ITextUnit tu) {
		// Do not leverage non-translatable entries
		if (!tu.isTranslatable()) {
			return false;
		}

		boolean approved = false;
		Property prop = tu.getTargetProperty(targetLocale, Property.APPROVED);
		if (prop != null) {
			if ("yes".equals(prop.getValue()))
				approved = true;
		}

		// Do not leverage entries without text
		if ( !tu.getSource().hasText() ) {
			return false;
		}
		
		// Do not leverage pre-approved entries
		if (approved) {
			return false;
		}

		// do not leverage if has been Diff Leveraged
		if (wasDiffLeveraged(tu)) {
			return false;
		}

		return true;
	}

	private void batchLeverage() {
		List<ITextUnit> tus = new LinkedList<ITextUnit>();
		for (Event e : batchedEvents) {
			if (e.getEventType() == EventType.TEXT_UNIT) {
				ITextUnit tu = e.getTextUnit();
				if (canLeverageTu(tu)) {
					tus.add(e.getTextUnit());
				}
			}
		}
		
		if (tus.isEmpty()) {
			return;
		}
		
		connector.batchLeverage(tus);

		// now copy any matches above our threshold
		if (params.getFillTarget()) {
			AltTranslationsAnnotation ata;

			for (ITextUnit tu : tus) {
				// only copy if there is no existing target 
				if (!tu.getTarget(targetLocale).hasText()) {															
					if (tu.getSource().hasBeenSegmented()) {
						for (Segment srcSeg : tu.getSourceSegments()) {
							if (!srcSeg.text.hasText()) {
								continue;
							}
	
							Segment trgSeg = tu.getTarget(targetLocale).getSegments().get(srcSeg.id);
							if (trgSeg != null) {																					
								ata = trgSeg.getAnnotation(AltTranslationsAnnotation.class);
								if (ata != null) {
									AltTranslation at = ata.getFirst(); // first should be best
									if (at.getCombinedScore() >= params.getFillTargetThreshold()) {
										TextFragment tf = new TextFragment(
												at.getTarget().getCodedText(), 
												at.getTarget().getFirstContent().getClonedCodes());
										trgSeg.text = tf;
									}
								}
							}
						}
					} else {
						ata = tu.getTarget(targetLocale).getAnnotation(AltTranslationsAnnotation.class);
						if (ata != null) {
							AltTranslation at = ata.getFirst(); // first should be best
							if (at.getCombinedScore() >= params.getFillTargetThreshold()) {
								tu.setTargetContent(targetLocale, at.getTarget().getFirstContent());
							}
						}
					}
				}
			}
		}
	}

	private boolean wasDiffLeveraged(ITextUnit tu) {
		if (tu.getTarget(targetLocale) == null) {
			return false;
		}

		if (tu.getTarget(targetLocale).getAnnotation(DiffMatchAnnotation.class) == null) {
			return false;
		}

		return true;
	}
}
