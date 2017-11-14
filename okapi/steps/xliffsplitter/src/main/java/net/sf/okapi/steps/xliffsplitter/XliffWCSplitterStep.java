/*===========================================================================
  Copyright (C) 2015 by the Okapi Framework contributors
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

package net.sf.okapi.steps.xliffsplitter;

import java.net.URI;
import java.security.InvalidParameterException;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
import net.sf.okapi.common.pipeline.annotations.StepParameterType;
import net.sf.okapi.common.resource.RawDocument;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@UsingParameters(XliffWCSplitterParameters.class)
public class XliffWCSplitterStep extends BasePipelineStep {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private XliffWCSplitterParameters params;
	
	public XliffWCSplitterStep() {
		params = new XliffWCSplitterParameters();
	}

//	@StepParameterMapping(parameterType = StepParameterType.OUTPUT_URI)
//	public void setOutputURI(final URI outputURI) {
//		this.outputURI = outputURI;
//	}
//	
//	public URI getOutputURI() {
//		return outputURI;
//	}
	
	@Override
	public String getDescription() {
		return "Split an XLIFF document into separate documents based on word count."
			+"Expects: raw document. Sends back: raw document.";
	}

	@Override
	public String getName() {
		return "XLIFF Word-Count Splitter";
	}

	@Override
	public IParameters getParameters() {
		return params;
	}

	@Override
	public void setParameters(final IParameters params) {
		this.params = (XliffWCSplitterParameters) params;
	}

	@Override
	protected Event handleRawDocument(final Event event) {
		final RawDocument rawDoc = event.getRawDocument();
		XliffWCSplitter splitter = new XliffWCSplitter(params);
		splitter.process(rawDoc);
		return event;
	}

}
