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

package net.sf.okapi.steps.xmlanalysis;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.pipeline.BasePipelineStep;

@UsingParameters(Parameters.class)
public class XMLAnalysisStep extends BasePipelineStep {

	private XMLAnalyzer xmlan;

	public XMLAnalysisStep () {
		xmlan = new XMLAnalyzer();
	}
	
	@Override
	public String getDescription () {
		return "Generate an analysis report of a set of XML documents."
			+ " Expects: raw document. Sends back: raw document.";
	}

	@Override
	public String getName () {
		return "XML Analysis";
	}

	@Override
	public IParameters getParameters () {
		return xmlan.getParameters();
	}

	@Override
	public void setParameters (IParameters params) {
		xmlan.setParameters((Parameters)params);
	}
 
	@Override
	protected Event handleStartBatch (Event event) {
		xmlan.reset();
		return event;
	}
	
	@Override
	protected Event handleEndBatch (Event event) {
		// Generate the report
		xmlan.generateOutput();
		return event;
	}

	@Override
	protected Event handleRawDocument (Event event) {
		xmlan.analyzeDocument(event.getRawDocument());
		return event;
	}

	protected XMLAnalyzer getAnalyzer() {
		return xmlan;
	}
}
