/*===========================================================================
  Copyright (C) 2014 by the Okapi Framework contributors
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

package net.sf.okapi.steps.ttxsplitter;

import java.net.URI;
import java.util.ArrayList;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.exceptions.OkapiBadStepInputException;
import net.sf.okapi.common.pipeline.BasePipelineStep;

@UsingParameters(TTXJoinerParameters.class)
public class TTXJoinerStep extends BasePipelineStep {

	private TTXJoinerParameters params;
//	private boolean done = false;
	private ArrayList<URI> inputList;
	
	public TTXJoinerStep () {
		params = new TTXJoinerParameters();
	}

	@Override
	public String getDescription () {
		return "Rebuilds previously split TTX documents into their original documents. "
			+ "Expects: raw document. Sends back: raw document.";
	}

	@Override
	public String getName() {
		return "TTX Joiner";
	}

	@Override
	public IParameters getParameters () {
		return params;
	}

	@Override
	public void setParameters (final IParameters params) {
		this.params = (TTXJoinerParameters)params;
	}

	@Override
	protected Event handleStartBatch (final Event event) {
//		done = true;
		inputList = new ArrayList<>();
		return event;
	}

//	@Override
//	protected Event handleStartBatchItem (final Event event) {
//		done = false;
//		return event;
//	}
	
	@Override
	protected Event handleRawDocument (final Event event) {
		URI uri = event.getRawDocument().getInputURI();
		if ( uri == null ) {
			throw new OkapiBadStepInputException("TTX Joiner expects URI inputs.");
		}
		inputList.add(uri);
		return event;
	}
	
	@Override
	protected Event handleEndBatch (final Event event) {
		TTXJoiner joiner = new TTXJoiner(params);
		joiner.process(inputList);
		return event;
	}

//	@Override
//	public boolean isDone () {
//		return done;
//	}

}
