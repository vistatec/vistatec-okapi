/*===========================================================================
  Copyright (C) 2008-2011 by the Okapi Framework contributors
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

package net.sf.okapi.common.pipeline;

import java.lang.reflect.Method;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.pipeline.annotations.ConfigurationParameter;
import net.sf.okapi.common.pipeline.annotations.StepIntrospector;
import net.sf.okapi.common.resource.PipelineParameters;

/**
 * Abstract implementation of the {@link IPipelineStep} interface.
 */
public abstract class BasePipelineStep implements IPipelineStep {

	private boolean isLastOutputStep = false;
	private IParameters params = null;

	@Override
	public IParameters getParameters() {
		return params;
	}

	@Override
	public void setParameters(IParameters params) {
		this.params = params;
	}

	@Override
	public boolean isDone() {
		return true;
	}

	@Override
	public String getHelpLocation() {
		// Title of the wiki help page is the name of the step + " Step"
		return getName() + " Step";
		//return ".." + File.separator + "help" + File.separator + "steps";
	}

	@Override
	public Event handleEvent(Event event) {
		switch (event.getEventType()) {
		case START_BATCH:
			event = handleStartBatch(event);
			break;
		case END_BATCH:
			event = handleEndBatch(event);
			break;
		case START_BATCH_ITEM:
			event = handleStartBatchItem(event);
			break;
		case END_BATCH_ITEM:
			event = handleEndBatchItem(event);
			break;
		case RAW_DOCUMENT:
			event = handleRawDocument(event);
			break;
		case START_DOCUMENT:
			event = handleStartDocument(event);
			break;
		case END_DOCUMENT:
			event = handleEndDocument(event);
			break;
		case START_SUBDOCUMENT:
			event = handleStartSubDocument(event);
			break;
		case END_SUBDOCUMENT:
			event = handleEndSubDocument(event);
			break;
		case START_GROUP:
			event = handleStartGroup(event);
			break;
		case END_GROUP:
			event = handleEndGroup(event);
			break;
		case TEXT_UNIT:
			event = handleTextUnit(event);
			break;
		case DOCUMENT_PART:
			event = handleDocumentPart(event);
			break;
		case CUSTOM:
			event = handleCustom(event);
			break;
		case MULTI_EVENT:
			event = handleMultiEvent(event);
			break;
		case PIPELINE_PARAMETERS:
			event = handlePipelineParameters(event);
			break;
		case START_SUBFILTER:
			event = handleStartSubfilter(event);
			break;
		case END_SUBFILTER:
			event = handleEndSubfilter(event);
			break;
		default:
			break;
		}
		return event;
	}

	public void cancel() {
	}

	@Override
	public void destroy() {
	}

	@Override
	public boolean isLastOutputStep() {
		return isLastOutputStep;
	}

	@Override
	public void setLastOutputStep(boolean isLastStep) {
		this.isLastOutputStep = isLastStep;
	}

	// By default we simply pass the event on to the next step.
	// Override these methods if you need to process the event

	/**
	 * Handles the {@link net.sf.okapi.common.EventType#START_BATCH} event.
	 * @param event event to handle.
	 * @return the event returned.
	 */
	protected Event handleStartBatch(Event event) {
		return event;
	}

	/**
	 * Handles the {@link net.sf.okapi.common.EventType#END_BATCH} event.
	 * @param event event to handle.
	 * @return the event returned.
	 */
	protected Event handleEndBatch(Event event) {
		return event;
	}

	/**
	 * Handles the {@link net.sf.okapi.common.EventType#START_BATCH_ITEM} event.
	 * @param event event to handle.
	 * @return the event returned.
	 */
	protected Event handleStartBatchItem(Event event) {
		return event;
	}

	/**
	 * Handles the {@link net.sf.okapi.common.EventType#END_BATCH_ITEM} event.
	 * @param event event to handle.
	 * @return the event returned.
	 */
	protected Event handleEndBatchItem(Event event) {
		return event;
	}

	/**
	 * Handles the {@link net.sf.okapi.common.EventType#RAW_DOCUMENT} event.
	 * @param event event to handle.
	 * @return the event returned.
	 */
	protected Event handleRawDocument(Event event) {
		return event;
	}

	/**
	 * Handles the {@link net.sf.okapi.common.EventType#START_DOCUMENT} event.
	 * @param event event to handle.
	 * @return the event returned.
	 */
	protected Event handleStartDocument(Event event) {
		return event;
	}

	/**
	 * Handles the {@link net.sf.okapi.common.EventType#END_DOCUMENT} event.
	 * @param event event to handle.
	 * @return the event returned.
	 */
	protected Event handleEndDocument(Event event) {
		return event;
	}

	/**
	 * Handles the {@link net.sf.okapi.common.EventType#START_SUBDOCUMENT} event.
	 * @param event event to handle.
	 * @return the event returned.
	 */
	protected Event handleStartSubDocument(Event event) {
		return event;
	}

	/**
	 * Handles the {@link net.sf.okapi.common.EventType#END_SUBDOCUMENT} event.
	 * @param event event to handle.
	 * @return the event returned.
	 */
	protected Event handleEndSubDocument(Event event) {
		return event;
	}

	/**
	 * Handles the {@link net.sf.okapi.common.EventType#START_GROUP} event.
	 * @param event event to handle.
	 * @return the event returned.
	 */
	protected Event handleStartGroup(Event event) {
		return event;
	}

	/**
	 * Handles the {@link net.sf.okapi.common.EventType#END_GROUP} event.
	 * @param event event to handle.
	 * @return the event returned.
	 */
	protected Event handleEndGroup(Event event) {
		return event;
	}

	/**
	 * Handles the {@link net.sf.okapi.common.EventType#TEXT_UNIT} event.
	 * @param event event to handle.
	 * @return the event returned.
	 */
	protected Event handleTextUnit(Event event) {
		return event;
	}

	/**
	 * Handles the {@link net.sf.okapi.common.EventType#DOCUMENT_PART} event.
	 * @param event event to handle.
	 * @return the event returned.
	 */
	protected Event handleDocumentPart(Event event) {
		return event;
	}

	/**
	 * Handles the {@link net.sf.okapi.common.EventType#CUSTOM} event.
	 * @param event event to handle.
	 * @return the event returned.
	 */
	protected Event handleCustom(Event event) {
		return event;
	}

	/**
	 * Handles the {@link EventType#MULTI_EVENT} event.
	 * @param event event to handle.
	 * @return the event returned.
	 */
	protected Event handleMultiEvent(Event event) {
		return event;
	}
	

	protected Event handleEndSubfilter(Event event) {
		return event;		
	}

	protected Event handleStartSubfilter(Event event) {
		return event;		
	}
	
	/**
	 * Handles the {@link EventType#PIPELINE_PARAMETERS} event.
	 * This method relies on the configuration parameters set in each step to set the corresponding values.
	 * @param event event to handle.
	 * @return the event returned.
	 */
	@SuppressWarnings("deprecation")
	protected Event handlePipelineParameters (Event event) {
		PipelineParameters pp = event.getPipelineParameters();
		List<ConfigurationParameter> pList = StepIntrospector.getStepParameters(this);
		try {
			for ( ConfigurationParameter param : pList ) {
				Method method = param.getMethod();
				if ( method == null ) continue;

				switch ( param.getParameterType() ) {
				case OUTPUT_URI:
					if ( pp.getOutputURI() != null ) {
						method.invoke(param.getStep(), pp.getOutputURI());
					}
					break;
				case FILTER_CONFIGURATION_ID:
					if ( pp.getFilterConfigurationId() != null ) {
						method.invoke(param.getStep(), pp.getFilterConfigurationId());
					}
					break;
				case FILTER_CONFIGURATION_MAPPER:
					if ( pp.getFilterConfigurationMapper() != null ) {
						method.invoke(param.getStep(), pp.getFilterConfigurationMapper());
					}
					break;
				case INPUT_RAWDOC:
					if ( pp.getInputRawDocument() != null ) {
						method.invoke(param.getStep(), pp.getInputRawDocument());
					}
					break;
				case INPUT_ROOT_DIRECTORY:
					if ( pp.getInputRootDirectory() != null ) {
						method.invoke(param.getStep(), pp.getInputRootDirectory());
					}
					break;
				case INPUT_URI:
					if ( pp.getThirdInputRawDocument() != null ) {
						method.invoke(param.getStep(), pp.getThirdInputRawDocument());
					}
					break;
				case OUTPUT_ENCODING:
					if ( pp.getOutputEncoding() != null ) {
						method.invoke(param.getStep(), pp.getOutputEncoding());
					}
					break;
				case ROOT_DIRECTORY:
					if ( pp.getRootDirectory() != null ) {
						method.invoke(param.getStep(), pp.getRootDirectory());
					}
					break;
				case SECOND_INPUT_RAWDOC:
					if ( pp.getSecondInputRawDocument() != null ) {
						method.invoke(param.getStep(), pp.getSecondInputRawDocument());
					}
					break;
				case SOURCE_LOCALE:
					if ( pp.getSourceLocale() != null ) {
						method.invoke(param.getStep(), pp.getSourceLocale());
					}
					break;
				case TARGET_LOCALE:
					if ( pp.getTargetLocale() != null ) {
						method.invoke(param.getStep(), pp.getTargetLocale());
					}
					break;
				case TARGET_LOCALES:
					if ( pp.getTargetLocales() != null ) {
						method.invoke(param.getStep(), pp.getTargetLocales());
					}
					break;
				case THIRD_INPUT_RAWDOC:
					if ( pp.getThirdInputRawDocument() != null ) {
						method.invoke(param.getStep(), pp.getThirdInputRawDocument());
					}
					break;
				case UI_PARENT:
					if ( pp.getUIParent() != null ) {
						method.invoke(param.getStep(), pp.getUIParent());
					}
					break;
				case EXECUTION_CONTEXT:
					if ( pp.getExecutionContext() != null ) {
						method.invoke(param.getStep(), pp.getExecutionContext());
					}
					break;
				case BATCH_INPUT_COUNT:
					if ( pp.getBatchInputCount() != -1 ) {
						method.invoke(param.getStep(), pp.getBatchInputCount());
					}
					break;
				case OUTPUT_DIRECTORY:
					//TODO: This is not handled apparently
					// (discovered when cleaning-up warnings)
					break;
				}
			}
		}
		catch ( Throwable e ) {
			throw new OkapiException("Error when setting pipeline parameter.\n"+e.getMessage(), e);
		}

		return event;
	}

}
