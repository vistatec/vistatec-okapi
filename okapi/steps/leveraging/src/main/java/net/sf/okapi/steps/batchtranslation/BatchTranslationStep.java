/*===========================================================================
  Copyright (C) 2009-2012 by the Okapi Framework contributors
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

package net.sf.okapi.steps.batchtranslation;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.filters.IFilterConfigurationMapper;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
import net.sf.okapi.common.pipeline.annotations.StepParameterType;
import net.sf.okapi.common.resource.RawDocument;

@UsingParameters(Parameters.class)
public class BatchTranslationStep extends BasePipelineStep {

	private Parameters params;
	private BatchTranslator trans;
	private IFilterConfigurationMapper fcMapper;
	private String rootDir;
	private String inputRootDir;
	private int batchInputCount;
	private int itemCount;
	private boolean sendTMX;

	public BatchTranslationStep () {
		params = new Parameters();
	}
	
	@StepParameterMapping(parameterType = StepParameterType.FILTER_CONFIGURATION_MAPPER)
	public void setFilterConfigurationMapper (IFilterConfigurationMapper fcMapper) {
		this.fcMapper = fcMapper;
	}
	
	public IFilterConfigurationMapper getFilterConfigurationMapper() {
		return fcMapper;
	}
	
	@StepParameterMapping(parameterType = StepParameterType.ROOT_DIRECTORY)
	public void setRootDirectory (String rootDir) {
		this.rootDir = rootDir;
	}
	
	public String getRootDirectory() {
		return rootDir;
	}
	
	@StepParameterMapping(parameterType = StepParameterType.INPUT_ROOT_DIRECTORY)
	public void setInputRootDirectory (String inputRootDir) {
		this.inputRootDir = inputRootDir;
	}
	
	public String getInputRootDirectory() {
		return inputRootDir;
	}
	
	@StepParameterMapping(parameterType = StepParameterType.BATCH_INPUT_COUNT)
	public void setBatchInputCount (int batchInputCount) {
		this.batchInputCount = batchInputCount;
	}
	
	public int getBatchInputCount() {
		return batchInputCount;
	}
	
	public String getName () {
		return "Batch Translation";
	}

	public String getDescription () {
		return "Creates translations from an external program for a given input document."
			+ " Expects: raw document. Sends back: raw document.";
	}
	
	@Override
	protected Event handleStartBatch (Event event) {
		sendTMX = params.getMakeTMX() && params.getSendTMX();
		itemCount = 0;
		trans = new BatchTranslator(fcMapper, params, rootDir, inputRootDir);
		return event;
	}
	
	@Override
	protected Event handleStartBatchItem (Event event) {
		if ( sendTMX ) return Event.NOOP_EVENT;
		else return event;
	}

	@Override
	protected Event handleEndBatchItem (Event event) {
		if ( sendTMX ) return Event.NOOP_EVENT;
		else return event;
	}

	@Override
	protected Event handleRawDocument (Event event) {
		// Process this document
		trans.processDocument((RawDocument)event.getResource());
		
		// If this is the last document: execute the final process
		itemCount++;
		if ( itemCount >= batchInputCount ) {
			if ( sendTMX ) {
				return trans.endBatch();
			}
			else { // Don't use the multi-events
				// This returns null
				trans.endBatch();
			}
		}
		
		// Else: return the event
		if ( sendTMX ) return Event.NOOP_EVENT;
		else return event;
	}

	@Override
	public IParameters getParameters () {
		return params;
	}
	
	@Override
	public void setParameters (IParameters params) {
		this.params = (Parameters)params;
	}

}
