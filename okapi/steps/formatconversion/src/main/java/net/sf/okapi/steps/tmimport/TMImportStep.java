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

package net.sf.okapi.steps.tmimport;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
import net.sf.okapi.common.pipeline.annotations.StepParameterType;
import net.sf.okapi.filters.pensieve.PensieveFilterWriter;

@UsingParameters(Parameters.class)
public class TMImportStep extends BasePipelineStep {

	private Parameters params;
	private IFilterWriter writer;
	private LocaleId targetLocale;
	private String rootDir;

	public TMImportStep () {
		params = new Parameters();
	}

	@SuppressWarnings("deprecation")
	@StepParameterMapping(parameterType = StepParameterType.TARGET_LOCALE)
	public void setTargetLocale (LocaleId targetLocale) {
		this.targetLocale = targetLocale;
	}
	
	@StepParameterMapping(parameterType = StepParameterType.ROOT_DIRECTORY)
	public void setRootDirectory (String rootDir) {
		this.rootDir = rootDir;
	}
	
	@Override
	public String getDescription () {
		return "Import text into a new or existing Pensieve TM."
			+ " Expects: filter events. Sends back: filter events.";
	}

	@Override
	public String getName () {
		return "TM Import";
	}

	@Override
	public void setParameters (IParameters params) {
		this.params = (Parameters)params;
	}

	@Override
	public Parameters getParameters () {
		return params;
	}

	@Override
	public Event handleEvent (Event event) {
		switch (event.getEventType()) {
		case START_BATCH:
			// Nothing to do so far
			// We initialize on first START_DOCUMENT
			break;
			
		case END_BATCH:
			if ( writer != null ) {
				writer.close();
				writer = null;
			}
			break;
			
		case START_DOCUMENT:
			if ( writer == null ) {
				writer = new PensieveFilterWriter();
				writer.setOutput(Util.fillRootDirectoryVariable(params.getTmDirectory(), rootDir));
				writer.setOptions(targetLocale, "UTF-8");
				((PensieveFilterWriter)writer).setOverwriteSameSource(params.getOverwriteSameSource());
				writer.handleEvent(event);
			}
			break;
			
		case END_DOCUMENT:
			// Do nothing: all documents go to the same TM
			// TM will be closed on END_BATCH
			break;
			
		case START_SUBDOCUMENT:
		case END_SUBDOCUMENT:
		case START_GROUP:
		case END_GROUP:
		case START_SUBFILTER:
		case END_SUBFILTER:
			writer.handleEvent(event);
			break;

		case TEXT_UNIT:
			writer.handleEvent(event);
			break;
			
		case START_BATCH_ITEM:
		case END_BATCH_ITEM:
		case RAW_DOCUMENT:
		case DOCUMENT_PART:
		case CUSTOM:
		default:
			// Do nothing
			break;
		
		}
		return event;
	}

}
