/*===========================================================================
  Copyright (C) 2009-2011 by the Okapi Framework contributors
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

package net.sf.okapi.common.pipelinedriver;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.sf.okapi.common.ExecutionContext;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filters.IFilterConfigurationMapper;
import net.sf.okapi.common.pipeline.IPipeline;
import net.sf.okapi.common.pipeline.IPipelineStep;
import net.sf.okapi.common.pipeline.IWorkQueueStep;
import net.sf.okapi.common.pipeline.Pipeline;
import net.sf.okapi.common.pipeline.annotations.ConfigurationParameter;
import net.sf.okapi.common.pipeline.annotations.StepIntrospector;
import net.sf.okapi.common.pipeline.annotations.StepParameterType;
import net.sf.okapi.common.resource.RawDocument;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of the {@link IPipelineDriver} interface.
 */
public class PipelineDriver implements IPipelineDriver {

	/**
	 * Logger for this driver.
	 */
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private IPipeline pipeline;
	private List<IBatchItemContext> batchItems;
	private LinkedList<List<ConfigurationParameter>> paramList;
	private IPipelineStep lastOutputStep;
	private int maxInputCount;
	private IFilterConfigurationMapper fcMapper;
	private String rootDir;
	private String inputRootDir;
	private String outputDir;
	private Object uiParent;
	private ExecutionContext context;
	
	/**
	 * Creates an new PipelineDriver object with an empty pipeline.
	 */
	public PipelineDriver () {
		pipeline = new Pipeline();
		batchItems = new ArrayList<IBatchItemContext>();
		paramList = new LinkedList<List<ConfigurationParameter>>();
		maxInputCount = 1;
	}

	@Override
	public void setFilterConfigurationMapper (IFilterConfigurationMapper fcMapper) {
		this.fcMapper = fcMapper;
	}
	
	@Override
	public void setRootDirectories (String rootDir,
		String inputRootDir)
	{
		this.rootDir = rootDir;
		this.inputRootDir = inputRootDir;
	}
	
	@Override
	public void setOutputDirectory (String outputDir) {
		this.outputDir = outputDir;
	}

	@Override
	public void setUIParent (Object uiParent) {
		this.uiParent = uiParent;
	}
	
	@Override
	public void setExecutionContext (ExecutionContext context) {
		this.context = context;
	}

	@Override
	public void setPipeline (IPipeline pipeline) {
		if ( this.pipeline != null ) {
			this.pipeline.cancel(); // Cancel processing of the current pipeline
		}
		
		if ( pipeline != null ) {
			pipeline.cancel(); // Cancel processing of the new pipeline
		}
				
		this.pipeline = new Pipeline();
		clearSteps();
		
		if ( pipeline != null ) {
			for (IPipelineStep step : pipeline.getSteps()) { // Steps are added to the newly created pipeline to be later 
				addStep(step); // lost, but the goal is to populate paramList.
			}
		}
		this.pipeline = pipeline; 	// The newly created pipeline with added steps is lost, but the 
									// references in paramList are valid (referencing the given pipline's steps).
									// Also observers of the given pipeline are in place.
	}

	@Override
	public IPipeline getPipeline () {
		return pipeline;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void addStep (IPipelineStep step) {
		pipeline.addStep(step);
		
		List<ConfigurationParameter> pList;
		if (step instanceof IWorkQueueStep) {		
			pList = StepIntrospector.getStepParameters(((IWorkQueueStep) step).getMainStep());
			// we need to update each parameter with the original IWorkQueueStep rather
			// than the main step of the workqueue
			for ( ConfigurationParameter p : pList ) {
				p.setStep(step);
			}			
		} else {
			pList = StepIntrospector.getStepParameters(step);
		}
		paramList.add(pList);

		for ( ConfigurationParameter p : pList ) {
			if ( p.getParameterType() == StepParameterType.OUTPUT_URI ) {
				if ( lastOutputStep != null ) {
					lastOutputStep.setLastOutputStep(false);
				}
				lastOutputStep = step;
				lastOutputStep.setLastOutputStep(true);
			}
			else if ( p.getParameterType() == StepParameterType.SECOND_INPUT_RAWDOC ) {
				maxInputCount = 2;
			}
			else if ( p.getParameterType() == StepParameterType.THIRD_INPUT_RAWDOC ) {
				maxInputCount = 3;
			}
		}
	}

	@Override
	public void processBatch (List<IBatchItemContext> batchItems) {
		this.batchItems = batchItems;
		processBatch();
	}
	
	@Override
	public void processBatch () {
		// Set the runtime parameters for the START_BATCH events
		// Especially source and target languages
		if ( batchItems.size() > 0 ) {
			PipelineDriverUtils.assignRuntimeParameters(this, paramList, batchItems.get(0));
		}
		pipeline.startBatch();
		// Run each item in the batch
		for ( IBatchItemContext item : batchItems ) {
			displayInput(item);
			// Set the runtime parameters
			PipelineDriverUtils.assignRuntimeParameters(this, paramList, item);
			// Process this input
			pipeline.process(item.getRawDocument(0));
		}
		pipeline.endBatch();
	}
	
	@Override
	public void addBatchItem (IBatchItemContext item) {
		batchItems.add(item);
	}

	@Override
	public void addBatchItem (RawDocument... rawDocs) {
		BatchItemContext item = new BatchItemContext();
		for ( RawDocument rawDoc : rawDocs ) {
			DocumentData ddi = new DocumentData();
			ddi.rawDocument = rawDoc;
			item.add(ddi);
		}
		batchItems.add(item);
	}
	
	@Override
	public void addBatchItem (RawDocument rawDoc,
		URI outputURI,
		String outputEncoding)
	{
		DocumentData ddi = new DocumentData();
		ddi.rawDocument = rawDoc;
		ddi.outputURI = outputURI;
		ddi.outputEncoding = outputEncoding;
		BatchItemContext item = new BatchItemContext();
		item.add(ddi);
		batchItems.add(item);
	}
	
	@Override
	public void addBatchItem (URI inputURI,
		String defaultEncoding,
		String filterConfigId,
		LocaleId srcLoc,
		LocaleId trgLoc)
	{
		DocumentData ddi = new DocumentData();
		ddi.rawDocument = new RawDocument(inputURI, defaultEncoding, srcLoc, trgLoc);
		ddi.rawDocument.setFilterConfigId(filterConfigId);
		BatchItemContext item = new BatchItemContext();
		item.add(ddi);
		batchItems.add(item);
	}
	
	@Override
	public void clearItems () {
		batchItems.clear();
	}

	/**
	 * Logs the information about which batch item is about to be processed. This
	 * method is called inside the loop that process the batch.
	 * @param item the batch item that is about to be processed.
	 */
	protected void displayInput (IBatchItemContext item) {
		if ( item.getRawDocument(0).getInputURI() != null ) {
			logger.info("Input: {}",item.getRawDocument(0).getInputURI().getPath());
		}
		else {
			logger.info("Input (No path available)");
		}
	}

	@Override
	public void clearSteps () {
		pipeline.clearSteps();		
		paramList.clear();
		lastOutputStep = null;
		maxInputCount = 1;
	}

	@Override
	public int getRequestedInputCount () {
		return maxInputCount;
	}
	
	@Override
	public void destroy() {
		pipeline.destroy();
	}
	
	////////////////////////////////////
	// Used for subclasses
	////////////////////////////////////
	protected List<IBatchItemContext> getBatchItems() {
		return batchItems;
	}

	protected LinkedList<List<ConfigurationParameter>> getParamList() {
		return paramList;
	}

	protected IFilterConfigurationMapper getFcMapper() {
		return fcMapper;
	}

	protected String getRootDir() {
		return rootDir;
	}

	protected String getInputRootDir() {
		return inputRootDir;
	}

	protected String getOutputDir() {
		return outputDir;
	}

	protected Object getUiParent() {
		return uiParent;
	}

	protected ExecutionContext getContext() {
		return context;
	}
}
