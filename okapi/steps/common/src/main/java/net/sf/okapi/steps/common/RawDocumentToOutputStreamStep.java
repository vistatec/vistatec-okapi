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

package net.sf.okapi.steps.common;

import java.io.OutputStream;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.StreamUtil;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.resource.RawDocument;

/**
 * Writes a {@link RawDocument} to an {@link OutputStream}.
 * <p>
 * WARNING: This step eats the {@link RawDocument} {@link Event} after writing its contents.
 * This step should normally be used as the last step in a pipeline when an output file is needed.
 * <p>
 * This class implements the {@link net.sf.okapi.common.pipeline.IPipelineStep}
 * interface for a step that takes a {@link RawDocument} and creates an output 
 * file from it. The generated file is passed on through a new {@link RawDocument}.
 */
@UsingParameters() // No parameters
public class RawDocumentToOutputStreamStep extends BasePipelineStep {
	private OutputStream outStream;
	
	/**
	 * Creates a new RawDocumentToOutputStreamStep object.
	 * This constructor is needed to be able to instantiate an object from newInstance()
	 */
	public RawDocumentToOutputStreamStep () {
	}
	
	// FIXME: @StepParameterMapping(parameterType = StepParameterType.OUTPUT_STREAM)
	public void setOutputStream (OutputStream outStream) {
		this.outStream = outStream;
	}
	
	@Override
	public String getDescription() {
		return "Write a RawDocument to an output stream.";
	}

	@Override
	public String getName () {
		return "RawDocument To OutputStream";
	}

	@Override
	public Event handleRawDocument (Event event) {
		try (RawDocument rawDoc = (RawDocument)event.getResource();) {				
			StreamUtil.copy(rawDoc.getStream(), outStream);				
		}
		catch ( Throwable e ) {
			throw new OkapiIOException("Error copying a RawDocument to output stream.", e);
		}
		
		// this steps writes RawDocument then eats the event
		return Event.NOOP_EVENT;
	}
}
