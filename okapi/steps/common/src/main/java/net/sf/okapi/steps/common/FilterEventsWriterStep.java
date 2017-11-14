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
import java.io.OutputStream;
import java.net.URI;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.ExecutionContext;
import net.sf.okapi.common.IUserPrompt;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.exceptions.OkapiFilterCreationException;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filters.IFilterConfigurationMapper;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
import net.sf.okapi.common.pipeline.annotations.StepParameterType;
import net.sf.okapi.common.resource.INameable;

/**
 * Outputs filters events into a document.
 * This class implements the {@link net.sf.okapi.common.pipeline.IPipelineStep}
 * interface for a step that takes filter events and creates an output document
 * using a provided {@link IFilterWriter} implementation. Each event and its 
 * resource are passed on to the next step.
 * @see RawDocumentToFilterEventsStep
 * @see FilterEventsToRawDocumentStep 
 */
@UsingParameters() // No parameters
public class FilterEventsWriterStep extends BasePipelineStep {

	private IFilterWriter filterWriter;
	private IFilterWriter customFilterWriter;
	private IFilterConfigurationMapper fcMapper;
	private String filterConfigId;
	private URI outputURI;
	private LocaleId targetLocale;
	private String outputEncoding;
	private String documentsRoot;
	private ExecutionContext context;
	private OutputStream outputStream;

	/**
	 * Creates a new FilterEventsWriterStep object.
	 * This constructor is needed to be able to instantiate an object from newInstance()
	 */
	public FilterEventsWriterStep () {
	}
	
	/**
	 * Creates a new FilterEventsWriterStep object.
	 * This constructor is needed to be able to instantiate an object from newInstance()
	 * 
	 * @param filterWriter the writer used to convert Events to a document
	 */
	public FilterEventsWriterStep (IFilterWriter filterWriter) {
		setFilterWriter(filterWriter);
	}

	@StepParameterMapping(parameterType = StepParameterType.FILTER_CONFIGURATION_MAPPER)
	public void setFilterConfigurationMapper (IFilterConfigurationMapper fcMapper) {
		this.fcMapper = fcMapper;
	}
	
	@StepParameterMapping(parameterType = StepParameterType.FILTER_CONFIGURATION_ID)
	public void setFilterConfigurationId (String filterConfigId) {
		this.filterConfigId = filterConfigId;
	}
	
	@StepParameterMapping(parameterType = StepParameterType.OUTPUT_URI)
	public void setOutputURI (URI outputURI) {
		this.outputURI = outputURI;
	}
	
    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    @SuppressWarnings("deprecation")
	@StepParameterMapping(parameterType = StepParameterType.TARGET_LOCALE)
	public void setTargetLocale (LocaleId targetLocale) {
		this.targetLocale = targetLocale;
	}

	@StepParameterMapping(parameterType = StepParameterType.OUTPUT_ENCODING)
	public void setOutputEncoding (String outputEncoding) {
		this.outputEncoding = outputEncoding;
	}

	@StepParameterMapping(parameterType = StepParameterType.EXECUTION_CONTEXT)
	public void setExecutionContext (ExecutionContext context) {
		this.context = context;
	}

	/**
	 * Sets the filter writer for this EventsWriterStep object.
	 * @param filterWriter the filter writer to use.
	 */
	public void setFilterWriter (IFilterWriter filterWriter) {
		customFilterWriter = filterWriter;
	}
	
	/**
	 * Sets the root of the documents to process. This is to be used when
	 * creating XLIFF output, as a temporary solution for the 'original' attribute.
	 * The value specified is used to fix-up the start document name.
	 * @param documentsRoot documents root.
	 */
	public void setDocumentRoots (String newDocumentsRoot) {
		// Set and normalize root
		File file = new File(newDocumentsRoot);
		documentsRoot = file.toURI().getPath();
		documentsRoot = documentsRoot.replace('\\', '/');
		// Make sure it ends with a '/'
		if ( !documentsRoot.endsWith("/") ) {
			documentsRoot += "/";
		}
	}

	@Override
	public String getName() {
		return "Filter Events Writer";
	}

	@Override
	public String getDescription () {
		return "Write out filter events into a document or stream.";
	}

	@Override
	public Event handleEvent(Event event) {
		switch ( event.getEventType() ) {
		case START_DOCUMENT:
			if ( customFilterWriter == null ) {
				// Create a writer from the filter information
				IFilter tmp = fcMapper.createFilter(filterConfigId);
				if ( tmp == null ) {
					throw new OkapiFilterCreationException("Error when creating writer from filter.");
				}
				filterWriter = tmp.createFilterWriter();
				filterWriter.setOptions(targetLocale, outputEncoding);
				filterWriter.setParameters(tmp.getParameters());
			}
			else { // If we have a custom writer, use it
				filterWriter = customFilterWriter;
				filterWriter.setOptions(targetLocale, outputEncoding);				
				normalizeResourceName(event);
			}
			
			// setup output, output stream has priority
			if (outputStream != null) {
				filterWriter.setOutput(outputStream);
			} else {
				filterWriter.setOutput(outputURI.getPath());
			}			
			
			if (outputStream == null) {
    			if ( new File(outputURI).exists() ) {
    				promptShouldOverwrite();
    			}
			}
			return filterWriter.handleEvent(event);

		// Filter events:
		case START_SUBDOCUMENT:
			normalizeResourceName(event);
			return filterWriter.handleEvent(event);
			
		case END_SUBDOCUMENT:
		case START_GROUP:
		case END_GROUP:
		case START_SUBFILTER:
		case END_SUBFILTER:
		case TEXT_UNIT:
		case DOCUMENT_PART:
			return filterWriter.handleEvent(event);
			
		case END_DOCUMENT:
			filterWriter.handleEvent(event);
			if ( filterWriter != null ) {
				filterWriter.close();
			}
			return event;

		default: // Any other event
			return event;
		}
	}	
	
	@Override
	public void destroy() {
		if ( filterWriter != null ) {
			filterWriter.close();
		}
	}

	private void normalizeResourceName (Event event) {
		if ( documentsRoot == null ) return; // Nothing to do
		INameable res = (INameable)event.getResource();
		String name = res.getName();
		if ( Util.isEmpty(name) ) return; // Nothing to do
		name = name.replace('\\', '/');
		if ( name.startsWith(documentsRoot) ) {
			name = name.substring(documentsRoot.length());
		}
		res.setName(name);
	}

	private void promptShouldOverwrite () {
		if (context == null || context.getIsNoPrompt()) return;
		
		String promptClass = context.getIsGui() ? "net.sf.okapi.common.ui.UserPrompt"
				: "net.sf.okapi.common.UserPrompt";
		
		IUserPrompt p;
		try {
			p = (IUserPrompt) Class.forName(promptClass).newInstance();
			p.initialize(context.getUiParent(), context.getApplicationName());
		}
		catch ( Throwable e ) {
			throw new InstantiationError("Could not instantiate user prompt.");
		}
		p.promptOKCancel("A file already exists in the target location.\nSelect \"OK\" to overwrite it.");
	}
}
