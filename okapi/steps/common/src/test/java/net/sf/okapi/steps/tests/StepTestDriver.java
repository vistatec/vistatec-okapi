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

package net.sf.okapi.steps.tests;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filters.FilterConfigurationMapper;
import net.sf.okapi.common.pipeline.IPipelineStep;
import net.sf.okapi.common.pipelinedriver.PipelineDriver;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.steps.common.RawDocumentToFilterEventsStep;

/**
 * Generic driver class to simplify executing steps based on filter events.
 */
public class StepTestDriver {

	private PipelineDriver driver;
	private RawDocumentToFilterEventsStep filterStep;
	private CaptureStep captureStep;
	private FilterConfigurationMapper fcMapper;
	private RawDocument rawDoc;
	
	public StepTestDriver () {
		fcMapper = new FilterConfigurationMapper();
		fcMapper.addConfigurations("net.sf.okapi.common.filters.DummyFilter");
		driver = new PipelineDriver();
		driver.setFilterConfigurationMapper(fcMapper);
		captureStep = new CaptureStep();
		filterStep = new RawDocumentToFilterEventsStep();
	}

	/**
	 * Prepares the data to process.
	 * @param srcText the source text to process
	 * @param trgText the optional target text to process (can be null)
	 * @param srcLoc the source locale.
	 * @param trgLoc the target locale.
	 */
	public void prepareFilterEventsStep (String srcText,
		String trgText,
		LocaleId srcLoc,
		LocaleId trgLoc)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(srcText);
		if ( trgText != null ) {
			sb.append("\n");
			sb.append(trgText);
		}
		rawDoc = new RawDocument(sb.toString(), srcLoc, trgLoc);
		rawDoc.setFilterConfigId("okf_dummy");
	}
	
	/**
	 * Gets the last text unit after the process is done.
	 * @return the last text unit processed.
	 */
	public ITextUnit getResult () {
		return captureStep.getLastTextUnit();
	}

	/**
	 * Executes a simple pipeline to test a filter-events based step.
	 * @param step the step to test.
	 */
	public void testFilterEventsStep (IPipelineStep step) {
		driver.clearItems();
		driver.getPipeline().getSteps().clear();
		driver.addStep(filterStep);
		driver.addStep(step);
		captureStep.reset(); // Reset result
		driver.addStep(captureStep);
		driver.addBatchItem(rawDoc, null, null);
		driver.processBatch();
	}

}
