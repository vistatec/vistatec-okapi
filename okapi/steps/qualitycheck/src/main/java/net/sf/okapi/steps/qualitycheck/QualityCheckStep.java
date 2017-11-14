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

package net.sf.okapi.steps.qualitycheck;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.exceptions.OkapiEditorCreationException;
import net.sf.okapi.common.filters.IFilterConfigurationMapper;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
import net.sf.okapi.common.pipeline.annotations.StepParameterType;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.lib.verification.IQualityCheckEditor;
import net.sf.okapi.lib.verification.Parameters;
import net.sf.okapi.lib.verification.QualityCheckSession;

@UsingParameters(Parameters.class)
public class QualityCheckStep extends BasePipelineStep {

	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	private QualityCheckSession session;
	private IQualityCheckEditor editor;
	private LocaleId sourceLocale;
	private LocaleId targetLocale;
	private boolean isDone;
	private boolean initDone;
	private String rootDir;
	private IFilterConfigurationMapper fcMapper;
	private boolean RawDocumentMode;
	private Object uiParent;

	public QualityCheckStep() {
		session = new QualityCheckSession();
		// Initialization is done when getting either a RawDocument or a StartDocument event.
	}

	@Override
	public String getName() {
		return "Quality Check";
	}

	@Override
	public String getDescription() {
		return "Compare source and target for quality. "
				+ "Expects: filter events or raw documents. Sends back: filter events or raw document.";
	}

	@Override
	public IParameters getParameters() {
		return session.getParameters();
	}

	@Override
	public void setParameters(IParameters params) {
		session.setParameters((Parameters) params);
	}

	@StepParameterMapping(parameterType = StepParameterType.ROOT_DIRECTORY)
	public void setRootDirectory(String rootDir) {
		this.rootDir = rootDir;
	}

	@StepParameterMapping(parameterType = StepParameterType.TARGET_LOCALE)
	public void setTargetLocale(LocaleId targetLocale) {
		this.targetLocale = targetLocale;
	}

	@StepParameterMapping(parameterType = StepParameterType.SOURCE_LOCALE)
	public void setSourceLocale(LocaleId sourceLocale) {
		this.sourceLocale = sourceLocale;
	}

	@StepParameterMapping(parameterType = StepParameterType.FILTER_CONFIGURATION_MAPPER)
	public void setFilterConfigurationMapper(IFilterConfigurationMapper fcMapper) {
		this.fcMapper = fcMapper;
	}

	@SuppressWarnings("deprecation")
	@StepParameterMapping(parameterType = StepParameterType.UI_PARENT)
	public void setUIParent(Object uiParent) {
		this.uiParent = uiParent;
	}

	@Override
	public boolean isDone() {
		return isDone;
	}

	@Override
	protected Event handleStartBatch(Event event) {
		isDone = true;
		initDone = false;
		return event;
	}

	@Override
	protected Event handleStartBatchItem(Event event) {
		// To get the raw document
		isDone = false;
		return event;
	}

	@Override
	protected Event handleRawDocument(Event event) {
		RawDocumentMode = true;
		if (editor == null) {
			try {
				// Hard code UI class for now
				editor = (IQualityCheckEditor) Class.forName("net.sf.okapi.lib.ui.verification.QualityCheckEditor").newInstance();
				editor.initialize(uiParent, true, null, fcMapper, session);
			} catch (Throwable e) {
				throw new OkapiEditorCreationException("Could not create an instance of IQualityCheckEditor.\n" + e.getMessage(), e);
			}
		}
		editor.addRawDocument(event.getRawDocument());
		isDone = true;
		return event;
	}

	@Override
	protected Event handleStartDocument(Event event) {
		RawDocumentMode = false;
		isDone = true;
		if (!initDone) {
			session.startProcess(sourceLocale, targetLocale);
			initDone = true;
		}
		// No pre-existing disabled issues: sigList = null
		session.processStartDocument((StartDocument) event.getResource(), null);
		return event;
	}

	@Override
	protected Event handleTextUnit(Event event) {
		session.processTextUnit(event.getTextUnit());
		return event;
	}

	@Override
	protected Event handleEndBatch(Event event) {
		if (RawDocumentMode) {
			editor.edit(true);
			// Make sure next batch will be re-initialized
			editor = null;
		}
		else {
			// Generate the report
			session.generateReport(rootDir);
			String finalPath = Util.fillRootDirectoryVariable(session.getParameters().getOutputPath(), rootDir);

			// save the session if requested
			if (session.getParameters().getSaveSession()) {
				String sessionPath = Util.fillRootDirectoryVariable(session.getParameters().getSessionPath(), rootDir);
				session.saveSession(sessionPath);
			}

			// Log the info
			LOGGER.info("\nOutput: {}", finalPath);
			int count = session.getIssues().size();
			if (count == 0) {
				LOGGER.info("No issue found.");
			} else {
				LOGGER.warn("Number of issues found = {}", count);
			}

			// Open the report if requested
			if (session.getParameters().getAutoOpen()) {
				Util.openURL((new File(finalPath)).getAbsolutePath());
			}
		}
		return event;
	}
}
