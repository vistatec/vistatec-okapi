/*===========================================================================
  Copyright (C) 2011 by the Okapi Framework contributors
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

package net.sf.okapi.steps.common.removetarget;

import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.ListUtil;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.resource.ITextUnit;

@UsingParameters(Parameters.class)
public class RemoveTargetStep extends BasePipelineStep {

	private List<String> tuIds;
	private List<LocaleId> targetLocales;
	private Parameters params;

	public RemoveTargetStep() {
		super();
		params = new Parameters();
	}

	@Override
	public String getDescription() {
		return "Remove targets in all or a given set of text units."
				+ " Expects: filter events. Sends back: filter events.";
	}

	@Override
	public String getName() {
		return "Remove Target";
	}

	@Override
	public void setParameters(IParameters params) {
		this.params = (Parameters) params;
	}

	@Override
	public Parameters getParameters() {
		return params;
	}

	@Override
	protected Event handleStartBatch(Event event) {
		tuIds = ListUtil.stringAsList(params.getTusForTargetRemoval());
		targetLocales = ListUtil.stringAsLanguageList(params.getTargetLocalesToKeep());
		return super.handleStartBatch(event);
	}

	@Override
	protected Event handleTextUnit(Event event) {
		String id = event.getResource().getId();

		// are we filtering on ids or locales?
		if (params.isFilterBasedOnIds()) {
			// If there're no TU Ids in the list, remove targets in all TUs
			if (Util.isEmpty(tuIds) || tuIds.contains(id)) {
				ITextUnit tu = event.getTextUnit();
				for (LocaleId locId : tu.getTargetLocales()) {
					tu.removeTarget(locId);
				}
			}
		} else {
			if (!Util.isEmpty(targetLocales)) {
				ITextUnit tu = event.getTextUnit();
				for (LocaleId locId : tu.getTargetLocales()) {
					if (!targetLocales.contains(locId)) {
						tu.removeTarget(locId);
					}					
				}
			}			
		}
		
		if (params.isRemoveTUIfNoTarget()) {
			if (event.getTextUnit().getTargetLocales().isEmpty()) {
				return Event.NOOP_EVENT;
			}
		}
		
		return event;
	}
}
