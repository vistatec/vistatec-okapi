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

package net.sf.okapi.steps.termextraction;

import java.io.File;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
import net.sf.okapi.common.pipeline.annotations.StepParameterType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@UsingParameters(Parameters.class)
public class TermExtractionStep extends BasePipelineStep {

	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	private Parameters params;
	private SimpleTermExtractor extractor;
	private LocaleId sourceLocale;
	private String rootDir;
	private String inputRootDir;

	public TermExtractionStep () {
		params = new Parameters();
		extractor = new SimpleTermExtractor();
	}
	
	@Override
	public String getName () {
		return "Term Extraction";
	}

	@Override
	public String getDescription () {
		return "Extract a list of possible terms found in the source content. "
			+ "Expects: filter events. Sends back: filter events.";
	}

	@Override
	public IParameters getParameters () {
		return params;
	}

	@Override
	public void setParameters (IParameters params) {
		this.params = (Parameters)params;
	}

	@StepParameterMapping(parameterType = StepParameterType.ROOT_DIRECTORY)
	public void setRootDirectory (String rootDir) {
		this.rootDir = rootDir;
	}

	@StepParameterMapping(parameterType = StepParameterType.INPUT_ROOT_DIRECTORY)
	public void setInputRootDirectory (String inputRootDir) {
		this.inputRootDir = inputRootDir;
	}

	@StepParameterMapping(parameterType = StepParameterType.SOURCE_LOCALE)
	public void setSourcetLocale (LocaleId sourceLocale) {
		this.sourceLocale = sourceLocale;
	}
	
	@Override
	protected Event handleStartBatch (Event event) {
		extractor.initialize(params, sourceLocale, rootDir, inputRootDir);
		return event;
	}
	
	@Override
	protected Event handleTextUnit (Event event) {
		extractor.processTextUnit(event.getTextUnit());
		return event;
	}
	
	@Override
	protected Event handleEndBatch (Event event) {
		extractor.completeExtraction();

		String finalPath = Util.fillRootDirectoryVariable(params.getOutputPath(), rootDir);
		LOGGER.info("Output: {}", finalPath);
		LOGGER.info("Candidate terms found = {}", extractor.getTerms().size());

		if ( params.getAutoOpen() ) {
			Util.openURL((new File(finalPath)).getAbsolutePath());
		}
		return event;
	}

}
