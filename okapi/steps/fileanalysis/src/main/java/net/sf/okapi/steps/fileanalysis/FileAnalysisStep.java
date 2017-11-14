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

package net.sf.okapi.steps.fileanalysis;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.pipeline.BasePipelineStep;

@UsingParameters(Parameters.class)
public class FileAnalysisStep extends BasePipelineStep {

	private Parameters params;

	public FileAnalysisStep () {
		params = new Parameters();
	}

	public String getDescription () {
		return "Analyzes a file properties such as BOM, line-break, encoding, etc."
			+ " Expects: raw document. Sends back: raw document.";
	}

	public String getName () {
		return "File Analysis";
	}

	@Override
	public IParameters getParameters () {
		return params;
	}

	@Override
	public void setParameters (IParameters params) {
		this.params = (Parameters)params;
	}

	@Override
	protected Event handleStartBatch (Event event) {
		//TODO: any task before the first file of the batch
		return event;
	}
	
	@Override
	protected Event handleEndBatch (Event event) {
		//TODO: any tasks at the end of the batch
		return event;
	}
	
	@Override
	protected Event handleRawDocument (Event event) {
//		RawDocument rawDoc = event.getRawDocument();
		//TODO: analyze the file
		return event;
	}

}
