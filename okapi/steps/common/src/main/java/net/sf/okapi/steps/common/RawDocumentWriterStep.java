/*===========================================================================
  Copyright (C) 2009-2010 by the Okapi Framework contributors
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

package net.sf.okapi.steps.common;

import java.io.File;
import java.net.URI;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.StreamUtil;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
import net.sf.okapi.common.pipeline.annotations.StepParameterType;
import net.sf.okapi.common.resource.RawDocument;

/**
 * Writes a {@link RawDocument} to an output file.
 * <p>
 * WARNING: This step eats the {@link RawDocument} {@link Event} after writing its contents.
 * This step should normally be used as the last step in a pipeline when an output file is needed.
 * <p>
 * This class implements the {@link net.sf.okapi.common.pipeline.IPipelineStep}
 * interface for a step that takes a {@link RawDocument} and creates an output 
 * file from it. The generated file is passed on through a new {@link RawDocument}.
 */
@UsingParameters() // No parameters
public class RawDocumentWriterStep extends BasePipelineStep {
	private URI outputURI;
	
	/**
	 * Creates a new RawDocumentWriterStep object.
	 * This constructor is needed to be able to instantiate an object from newInstance()
	 */
	public RawDocumentWriterStep () {
	}
	
	@StepParameterMapping(parameterType = StepParameterType.OUTPUT_URI)
	public void setOutputURI (URI outputURI) {
		this.outputURI = outputURI;
	}
	
	@Override
	public String getDescription() {
		return "Write a RawDocument to an output file.";
	}

	@Override
	public String getName () {
		return "RawDocument Writer";
	}

	@Override
	public Event handleRawDocument (Event event) {
		RawDocument rawDoc = null;
		try {
			rawDoc = (RawDocument)event.getResource();
			File outFile = new File(outputURI);
			Util.createDirectories(outFile.getAbsolutePath());				
			StreamUtil.copy(rawDoc.getStream(), outFile);				
		}
		catch ( Throwable e ) {
			throw new OkapiIOException("Error writing or copying a RawDocument.", e);
		} finally {
			if (rawDoc != null) {
				rawDoc.close();
			}
		}
		// this steps writes RawDocument then eats the event
		return Event.NOOP_EVENT;
	}

}
