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

package net.sf.okapi.steps.common.tufiltering;

import net.sf.okapi.common.ClassUtil;
import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.resource.ITextUnit;

public class TuFilteringStep extends BasePipelineStep {
	
	private ITextUnitFilter tuFilter;
	private Parameters params;
	
	public TuFilteringStep() {
		params = new Parameters();
	}
	
	public TuFilteringStep(ITextUnitFilter tuFilter) {
		this();
		this.tuFilter = tuFilter;
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
	public String getName() {
		return "Text Unit Filtering";
	}

	@Override
	public String getDescription() {
		return "Set the non-translatable flag to the text units accepted by the text unit filter specified in step's parameters."
			+ " Expects: filter events. Sends back: filter events.";
	}

	private void initFilter() {
		if (tuFilter == null) {
			if (Util.isEmpty(params.getTuFilterClassName())) {
				throw new OkapiException("Text Unit filter class is not specified in step parameters.");
			}
			try {
				tuFilter = (ITextUnitFilter) ClassUtil.instantiateClass(params.getTuFilterClassName());
			} catch (Exception e) {
				throw new OkapiException(String.format("Cannot instantiate the specified Text Unit filter (%s)", e.toString()));
			}
		}
	}
	
	/**
	 * Process a given text unit event. This method can modify the event's text unit resource,
	 * can drop the event and return NO_OP, can create and return a new event (for example, a DOCUMENT_PART event),
	 * or it can produce several events and return them packed in a MULTI_EVENT's resource.
	 * <p>
	 * This method can be overridden in subclasses to change the way text unit events are processed if accepted.
	 * <p>
	 * If not overridden, clears the "translatable" flag of accepted text units, thus marking the text units
	 * non-translatable.
	 * 
	 * @param tuEvent the text unit event which resource can be modified.
	 * @return the modified event
	 */
	protected Event processFiltered(Event tuEvent) {
		ITextUnit tu = tuEvent.getTextUnit();
		if (tu != null) tu.setIsTranslatable(false);
		return tuEvent;
	}
	
	@Override
	protected Event handleStartBatch(Event event) {
		initFilter();
		return super.handleStartBatch(event);
	}

	@Override
	protected Event handleTextUnit(Event event) {
		initFilter();
		if (tuFilter.accept(event.getTextUnit())) {
			return processFiltered(event);
		}
		else
			return event;
	}	
}
