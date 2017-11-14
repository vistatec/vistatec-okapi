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

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.exceptions.OkapiBadStepInputException;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.resource.RawDocument;

@UsingParameters(TTXSplitterParameters.class)
public class TTXSplitterStep extends BasePipelineStep {

	private TTXSplitterParameters params;
//	private boolean done = false;
	private TTXSplitter splitter;
	
	public TTXSplitterStep () {
		params = new TTXSplitterParameters();
	}

	@Override
	public String getDescription () {
		return "Splits a TTX document into several ones of roughly equal word count. "
			+ "Expects: raw document. Sends back: raw document.";
	}

	@Override
	public String getName() {
		return "TTX Splitter";
	}

	@Override
	public IParameters getParameters () {
		return params;
	}

	@Override
	public void setParameters (final IParameters params) {
		this.params = (TTXSplitterParameters)params;
	}

	@Override
	protected Event handleStartBatch (final Event event) {
		splitter = new TTXSplitter(params);
//		done = true;
		return event;
	}

	@Override
	protected Event handleStartBatchItem (final Event event) {
//		done = false;
		return event;
	}

//	@Override
//	public boolean isDone () {
//		return done;
//	}

	@Override
	protected Event handleRawDocument (final Event event) {
		final RawDocument rawDoc = event.getRawDocument();
		//TODO: change this to support input stream instead
		URI uri = rawDoc.getInputURI();
		if ( uri == null ) {
			throw new OkapiBadStepInputException("TTX Splitter expects a URI input.");
		}
		splitter.split(uri);
		return event;
	}

}
