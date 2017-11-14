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

package net.sf.okapi.steps.leveraging;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.filterwriter.TMXWriter;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
import net.sf.okapi.common.pipeline.annotations.StepParameterType;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.lib.translation.QueryManager;
import net.sf.okapi.lib.translation.ResourceItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@UsingParameters(Parameters.class)
public class LeveragingStep extends BasePipelineStep {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private Parameters params;
	private LocaleId sourceLocale;
	private LocaleId targetLocale;
	private QueryManager qm;
	private TMXWriter tmxWriter;
	private String rootDir;
	private String inputRootDir;
	private boolean initDone;
	private int totalCount;
	private int exactCount;
	private int fuzzyCount;
	private int iQueryId;
	private ClassLoader connectorContext;

	public LeveragingStep () {
		params = new Parameters();
	}
	
	@StepParameterMapping(parameterType = StepParameterType.SOURCE_LOCALE)
	public void setSourceLocale (LocaleId sourceLocale) {
		this.sourceLocale = sourceLocale;
	}
	
	public LocaleId getSourceLocale() {
		return sourceLocale;
	}
	
	@SuppressWarnings("deprecation")
	@StepParameterMapping(parameterType = StepParameterType.TARGET_LOCALE)
	public void setTargetLocale (LocaleId targetLocale) {
		this.targetLocale = targetLocale;
	}
	
	public LocaleId getTargetLocale() {
		return targetLocale;
	}
	
	@StepParameterMapping(parameterType = StepParameterType.ROOT_DIRECTORY)
	public void setRootDirectory (String rootDir) {
		this.rootDir = rootDir;
	}
	
	public String getRootDirectory() {
		return rootDir;
	}
	
	@StepParameterMapping(parameterType = StepParameterType.INPUT_ROOT_DIRECTORY)
	public void setInputRootDirectory (String inputRootDir) {
		this.inputRootDir = inputRootDir;
	}

	public String getInputRootDirectory() {
		return inputRootDir;
	}
	
	public String getName () {
		return "Leveraging";
	}

	public String getDescription () {
		return "Leverage existing translation into the text units content of a document."
			+ " Expects: filter events. Sends back: filter events.";
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
		totalCount = 0;
		exactCount = 0;
		fuzzyCount = 0;
		initDone = false;
		return event;
	}
	
	@Override
	protected Event handleEndBatch (Event event) {
		if ( qm != null ) {
			qm.close();
			qm = null;
		}
		if ( tmxWriter != null ) {
			tmxWriter.writeEndDocument();
			tmxWriter.close();
			tmxWriter = null;
		}
		
		if ( !params.getLeverage() ) return event;
		logger.info("\nTotals:\nProcessed segments = {}", totalCount);
		logger.info("Best matches that are exact = {}", exactCount);
		logger.info("Best matches that are fuzzy = {}", fuzzyCount);		
		
		return event;
	}
	
	@Override
	protected Event handleStartDocument (Event event) {
		if ( !params.getLeverage() ) return event;
		if ( !initDone ) {			
			initialize();
		}
		qm.setLanguages(sourceLocale, targetLocale);
		qm.resetCounters();
		return event;
	}

	@Override
	protected Event handleEndDocument (Event event) {
		if ( !params.getLeverage() ) return event;
		totalCount += qm.getTotalSegments();
		exactCount += qm.getExactBestMatches();
		fuzzyCount += qm.getFuzzyBestMatches();
		logger.info("Processeed segments = {}", qm.getTotalSegments());
		logger.info("Best matches that are exact = {}", qm.getExactBestMatches());
		logger.info("Best matches that are fuzzy = {}", qm.getFuzzyBestMatches());
		return event;
	}

	@Override
	protected Event handleTextUnit (Event event) {
		if ( !params.getLeverage() ) return event;
		ITextUnit tu = event.getTextUnit();

		// Do not leverage non-translatable entries
		if ( !tu.isTranslatable() ) return event;

    	boolean approved = false;
    	Property prop = tu.getTargetProperty(targetLocale, Property.APPROVED);
    	if ( prop != null ) {
    		if ( "yes".equals(prop.getValue()) ) approved = true;
    	}
    	// Do not leverage pre-approved entries
    	if ( approved ) return event;
    	    	
    	// Leverage
    	qm.leverage(tu);
    	
    	// Optionally write out this TU
		if ( tmxWriter != null ) {
			tmxWriter.writeAlternates(tu, targetLocale);
		}
		
		return event;
	}

	private void initialize () {
		// If we don't really use this step, just move on
		if ( !params.getLeverage() ) {
			initDone = true;
			return;
		}
		
		// Else: initialize the global variables
		qm = new QueryManager();
		qm.setNoQueryThreshold(params.getNoQueryThreshold());
		qm.setThreshold(params.getThreshold());
		qm.setRootDirectory(rootDir);
		qm.setLanguages(sourceLocale, targetLocale);
		
		if (connectorContext == null || connectorContext == Thread.currentThread().getContextClassLoader()) {
			iQueryId = qm.addAndInitializeResource(params.getResourceClassName(), null,
					params.getResourceParameters());
		} else {
			iQueryId = qm.addAndInitializeResource(params.getResourceClassName(), null, connectorContext,
					params.getResourceParameters());
		}
		
		ResourceItem res = qm.getResource(iQueryId);
		logger.info("Leveraging settings: {}", res.name);
		logger.info(res.query.getSettingsDisplay());

		// Options
		String targetPrefix = (params.getUseTargetPrefix() ? params.getTargetPrefix() : null);
		qm.setOptions(params.getFillTarget() ? params.getFillTargetThreshold() : Integer.MAX_VALUE,
			true, true, params.getDowngradeIdenticalBestMatches(),
			targetPrefix, params.getTargetPrefixThreshold(), params.getCopySourceOnNoText());
			
		if ( params.getMakeTMX() ) {
			// Resolve the variables
			String realPath = Util.fillRootDirectoryVariable(params.getTMXPath(), rootDir);
			realPath = Util.fillInputRootDirectoryVariable(realPath, inputRootDir);
			realPath = LocaleId.replaceVariables(realPath, sourceLocale, targetLocale);
			// Create the output
			tmxWriter = new TMXWriter(realPath);
			tmxWriter.setUseMTPrefix(params.getUseMTPrefix());
			tmxWriter.writeStartDocument(sourceLocale, targetLocale,
				getClass().getName(), "1", // Version is irrelevant here
				"sentence", "undefined", "undefined");
		}
		initDone = true;
	}
	
	public void setConnectorContext(ClassLoader connectorContext) {
		this.connectorContext = connectorContext;
	}
	
	protected QueryManager getQueryManager() {
		return qm;
	}
	
	protected TMXWriter getTMXWriter() {
		return tmxWriter;
	}
}
