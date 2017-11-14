/*===========================================================================
  Copyright (C) 2010-2013 by the Okapi Framework contributors
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

package net.sf.okapi.steps.copyormove;

import java.io.File;
import java.net.URI;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.StreamUtil;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
import net.sf.okapi.common.pipeline.annotations.StepParameterType;

public class CopyOrMoveStep extends BasePipelineStep {

	private Parameters params;
	private URI outputURI;

	public CopyOrMoveStep() {
		reset();
	}

	@Override
	public String getName() {
		return "Copy Or Move";
	}

	@Override
	public String getDescription() {
		return "Copies or moves the listed files to the specified location. "
				+ "Expects: raw documents. Sends back: raw documents.";
	}

	@Override
	public IParameters getParameters() {
		return params;
	}

	@Override
	public void setParameters(IParameters params) {
		this.params = (Parameters) params;
	}

	@StepParameterMapping(parameterType = StepParameterType.OUTPUT_URI)
	public void setOutputURI(URI outputURI) {
		this.outputURI = outputURI;
	}

	@Override
	public Event handleRawDocument (Event event) {
		File file = new File(event.getRawDocument().getInputURI());
		File output = new File(outputURI);

		if ( params.getCopyOption().equals("overwrite") ) {
			StreamUtil.copy(file.getPath(), output.getPath(), params.isMove());
		}
		else if ( params.getCopyOption().equals("backup") ) {
			if ( output.exists() ) {
				if ( !output.renameTo(new File(outputURI.toString() + ".bak")) ) {
					StreamUtil.copy(output.getPath(), output.getPath().replace(".txt", ".txt.bak"), true);
				}
			}
			StreamUtil.copy(file.getPath(), output.getPath(), params.isMove());
		}
		else { // skip copy/move file
			if ( !output.exists() ) {
				StreamUtil.copy(file.getPath(), output.getPath(), params.isMove());
			}
		}
		return event;
	}

	private void reset() {
		this.params = new Parameters();
	}
}
