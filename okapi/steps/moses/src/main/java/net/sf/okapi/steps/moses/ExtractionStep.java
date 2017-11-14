/*===========================================================================
  Copyright (C) 2010 by the Okapi Framework contributors
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

package net.sf.okapi.steps.moses;

import java.io.File;
import java.net.URI;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
import net.sf.okapi.common.pipeline.annotations.StepParameterType;
import net.sf.okapi.filters.mosestext.FilterWriterParameters;
import net.sf.okapi.filters.mosestext.MosesTextFilterWriter;

@UsingParameters(FilterWriterParameters.class)
public class ExtractionStep extends BasePipelineStep {

	private LocaleId targetLocale;
	private URI inputURI;
	private URI outputURI;
	private MosesTextFilterWriter writer;
	private FilterWriterParameters params;
	
	public ExtractionStep () {
		params = new FilterWriterParameters();
	}
	
	@Override
	public String getDescription () {
		return "Creates a Moses InlineText file from the input document."
			+ " Expects: filter events. Sends back: filter events.";
	}

	@Override
	public String getName () {
		return FilterWriterParameters.NAME;
	}

	@SuppressWarnings("deprecation")
	@StepParameterMapping(parameterType = StepParameterType.TARGET_LOCALE)
	public void setTargetLocale (LocaleId targetLocale) {
		this.targetLocale = targetLocale;
	}

	@StepParameterMapping(parameterType = StepParameterType.INPUT_URI)
	public void setInputURI (URI inputURI) {
		this.inputURI = inputURI;
	}

	@StepParameterMapping(parameterType = StepParameterType.OUTPUT_URI)
	public void setOutputURI (URI outputURI) {
		this.outputURI = outputURI;
	}

	@Override
	public IParameters getParameters() {
		return params;
	}

	@Override
	public void setParameters(IParameters params) {
		params = (FilterWriterParameters)params;
	}

	@Override
	public Event handleEvent (Event event) {
		switch ( event.getEventType() ) {
		case START_DOCUMENT:
			writer = new MosesTextFilterWriter();
			writer.setOptions(targetLocale, "UTF-8");
			if ( outputURI == null ) {
				File f = new File(inputURI.getPath() + "."+event.getStartDocument().getLocale().toString());
				outputURI = f.toURI();
			}
			writer.setOutput(outputURI.getPath());
			writer.setParameters(params);
			return writer.handleEvent(event);
			
		case END_DOCUMENT:
			if ( writer != null ) {
				event = writer.handleEvent(event);
				writer.close();
			}
			return event;
			
		default:
			// The writer creates and closes the files
			if ( writer != null ) {
				return writer.handleEvent(event);
			}
			return event;
		}
	}
	
}
