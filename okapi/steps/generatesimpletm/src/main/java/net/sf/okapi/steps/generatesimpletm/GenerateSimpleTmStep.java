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

package net.sf.okapi.steps.generatesimpletm;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.exceptions.OkapiBadStepInputException;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
import net.sf.okapi.common.pipeline.annotations.StepParameterType;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.tm.simpletm.Database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@UsingParameters(Parameters.class)
public class GenerateSimpleTmStep extends BasePipelineStep {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private Database simpleTm = null;
	private Parameters params;
	private LocaleId targetLocale;
	private String fileName;
	private int countIsNotTranslatable;
	private int countTuNotAdded;
	private int countTusAdded;
	private int countSegsAdded;
	private boolean isMultilingual;
	private String rootDir;

	public GenerateSimpleTmStep () {
		params = new Parameters();
	}
	
	@SuppressWarnings("deprecation")
	@StepParameterMapping(parameterType = StepParameterType.TARGET_LOCALE)
	public void setTargetLocale (LocaleId targetLocale) {
		this.targetLocale = targetLocale;
	}
	
	@StepParameterMapping(parameterType = StepParameterType.ROOT_DIRECTORY)
	public void setRootDirectory (String rootDir) {
		this.rootDir = rootDir;
	}
	
	public String getName () {
		return "Generate SimpleTM";
	}

	public String getDescription () {
		return "Generates a SimpleTM translation memory from multilingual input files. "
			+ "Expects filter events. Sends back: filter events.";
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
		if ( Util.isEmpty(params.getTmPath()) ) {
			throw new OkapiBadStepInputException("Please provide a valid path and name for the TM.");
		}
		return event;
	}
	
	@Override
	protected Event handleStartBatchItem (Event event) {
		if(simpleTm == null){
			simpleTm = new Database();
			simpleTm.create(Util.fillRootDirectoryVariable(params.getTmPath(), rootDir),
				true, targetLocale);
		}		
		return event;
	}
	
	@Override
	protected Event handleEndBatchItem (Event event) {
		logger.info("\nSimpleTM output: {}", fileName );
		logger.info("Untranslatable text units = {}",countIsNotTranslatable);
		logger.info("Translatable text units but failed to add = {}", countTuNotAdded);
		logger.info("Text units added = {}", countTusAdded);
		logger.info("Segments added = {}",countSegsAdded);
		return event;
	}

	@Override
	protected Event handleEndBatch (Event event) {
		logger.info("Total untranslatable text units = {}",countIsNotTranslatable);
		logger.info("Total text units (Translatable) that failed to add = {}", countTuNotAdded);
		logger.info("Total text units added = {}", countTusAdded);
		logger.info("Total segments added = {}",countSegsAdded);
		logger.info("Total entries in generated simpleTm = {}", simpleTm.getEntryCount());
		simpleTm.close();
		return event;		
	}

	@Override
	protected Event handleStartDocument (Event event) {
		StartDocument sd = (StartDocument)event.getResource();
		fileName = Util.getFilename(sd.getName(), true);
		isMultilingual = sd.isMultilingual();
		if(!isMultilingual){
			logger.warn("File {} is not processed as a multiLingual file and cannot be used to populate the SimpleTm.", fileName);
		} 
		
		return event;
	}
	
	@Override
	protected Event handleTextUnit (Event event) {
		
		//--skip file if not multilingual.
		if ( !isMultilingual ) {
			countTuNotAdded++;
			return event;
		} 
		
		ITextUnit tu = event.getTextUnit();
		// Skip non-translatable
		if ( !tu.isTranslatable() ){
			countIsNotTranslatable++;
			return event;
		} 
		
		if ( tu.getSource() == null ) {
			logger.warn("TextUnit is missing source content.");
			countTuNotAdded++;
			return event;
		}
		
		if( !tu.hasTarget(targetLocale) || ( tu.getTarget(targetLocale)==null )){
			logger.warn("TextUnit is missing '{}' target.", targetLocale);
			countTuNotAdded++;
			return event;
		}
		
		// Check if the attributes for GroupName and FileName are available from the input
		Property propGName = tu.getProperty(Database.NGRPNAME);
		if ( propGName == null ) propGName = tu.getProperty("Txt::"+Database.NGRPNAME);
		Property propFName = tu.getProperty(Database.NFILENAME);
		if ( propFName == null ) propFName = tu.getProperty("Txt::"+Database.NFILENAME);

		// Add the entry
		int added = simpleTm.addEntry(tu,
			(propGName==null) ? tu.getName() : propGName.getValue(),
			(propFName==null) ? fileName : propFName.getValue());

		if ( added==0 ) {
			countTuNotAdded++;
		}
		else if( added>0 ) {
			countTusAdded++;
			countSegsAdded+=added;
		}
		
		return event;
	}
}
