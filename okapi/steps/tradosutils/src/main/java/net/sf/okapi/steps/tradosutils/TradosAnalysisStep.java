/*===========================================================================
  Copyright (C) 2012-2013 by the Okapi Framework contributors
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

package net.sf.okapi.steps.tradosutils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
import net.sf.okapi.common.pipeline.annotations.StepParameterType;
import net.sf.okapi.common.resource.RawDocument;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Variant;

@UsingParameters(ParametersAnalysis.class)
public class TradosAnalysisStep extends BasePipelineStep{

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private ParametersAnalysis params;
	private ArrayList<String> inputFiles = new ArrayList<String>();
	private String logToOpen;
	private LocaleId sourceLocale;
	private LocaleId targetLocale;
	private String rootDir;
	private String inputRootDir;
	private boolean sendTmx;
	private int batchInputCount;
	private int count;
	
	public TradosAnalysisStep () {
		params = new ParametersAnalysis();
	}
	
	@StepParameterMapping(parameterType = StepParameterType.SOURCE_LOCALE)
	public void setSourcetLocale (LocaleId sourceLocale) {
		this.sourceLocale = sourceLocale;
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
	
	@StepParameterMapping(parameterType = StepParameterType.INPUT_ROOT_DIRECTORY)
	public void setInputRootDirectory (String inputRootDir) {
		this.inputRootDir = inputRootDir;
	}
	
	@StepParameterMapping(parameterType = StepParameterType.BATCH_INPUT_COUNT)
	public void setBatchInputCount (int batchInputCount) {
		this.batchInputCount = batchInputCount;
	}
	
	@Override
	protected Event handleRawDocument (Event event) {
		RawDocument rawDoc = event.getRawDocument();
		inputFiles.add(new File(rawDoc.getInputURI()).getPath());
		count++;
		if ( count >= batchInputCount ) {
			Event newEvent = execute();
			if ( sendTmx ) return newEvent;
			else return event;
		}
		else {
			if ( sendTmx ) return Event.NOOP_EVENT;
			else return event;
		}
	}

	@Override
	protected Event handleStartBatch (final Event event) {
		inputFiles.clear();
		logToOpen = null;
		count = 0;
		TradosUtils.verifyJavaLibPath(new File(getClass().getProtectionDomain().getCodeSource().getLocation().getFile()));
		// If we create a TMX file of unknown segments and the send-tmx option is on
		// then this step should send only one useful event to the next step 
		sendTmx = params.getSendTmx() && params.getExportUnknown();
		return event;
	}

	@Override
	protected Event handleStartBatchItem (Event event) {
		if ( sendTmx ) return Event.NOOP_EVENT;
		else return event;
	}

	@Override
	protected Event handleEndBatchItem (Event event) {
		return Event.NOOP_EVENT;
	}

	private Event execute () {
		StringBuffer job = new StringBuffer();
		job.append("[Analyse]\n");
		
		logToOpen = Util.fillRootDirectoryVariable(params.getLogPath(), rootDir);
		logToOpen = Util.fillInputRootDirectoryVariable(logToOpen, inputRootDir);
		logToOpen = LocaleId.replaceVariables(logToOpen, sourceLocale, targetLocale);
		
		// Delete existing log and csv file if requested
		TradosUtils.deleteLogIfRequested( (!params.getAppendToLog()), logToOpen);
		Util.createDirectories(logToOpen);
		
		int taskCount=1;
		if (params.getExportUnknown()) 
			taskCount++;
		if (params.isCreatePrjTm()) 
			taskCount++;		
		
		job.append("LogFile="+logToOpen+"\n");
		job.append("Tasks=" + taskCount + "\n");
		job.append("[Task1]\n");
		job.append("Task=Analyse\n");
		job.append("Files="+inputFiles.size()+"\n");

		int i = 1;
		for (String file : inputFiles) {
			job.append("File"+i+"="+file+"\n");
			i++;
		}

		//--add ExportUnknown task if selected--
		String tmxOutput = Util.fillRootDirectoryVariable(params.getTmxPath(), rootDir);
		tmxOutput = Util.fillInputRootDirectoryVariable(tmxOutput, inputRootDir);
		tmxOutput = LocaleId.replaceVariables(tmxOutput, sourceLocale, targetLocale);
		Util.createDirectories(tmxOutput);
		
		if ( params.getExportUnknown() ) {

			job.append("[Task2]\n");
			job.append("Task=ExportUnknown\n");
			job.append("MaxMatch="+params.getMaxMatch()+"\n");
			job.append("File="+tmxOutput+"\n");
			job.append("FileType=5\n");					
		}
		
		if ( params.isCreatePrjTm() ) {

			String tmOutput = Util.fillRootDirectoryVariable(params.getPrjTmPath(), rootDir);
			tmOutput = Util.fillInputRootDirectoryVariable(tmOutput, inputRootDir);
			tmOutput = LocaleId.replaceVariables(tmOutput, sourceLocale, targetLocale);
			Util.createDirectories(tmOutput);
			
			if (params.getExportUnknown()){
				job.append("[Task3]\n");				
			}else{
				job.append("[Task2]\n");
			}
			job.append("Task=CreateProjectTM\n");
			job.append("File="+tmOutput+"\n");
		}
		
		File jobFile;
		try {
			jobFile = File.createTempFile("~okapi-51_tradosjobfile_", ".tmp");

			// Write job file
		    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(jobFile), "UTF-16LE"));
			try {
				//--insert 
				out.write("\uFEFF");
				out.write(job.toString());
			} finally {
			    out.close();
			}
		}
		catch ( Throwable e ) {
			throw new OkapiIOException("Cannot create temporary output.", e);
		}
		
		// Get name of temporary tm
		if ( !params.getUseExisting() ) {
			String tempName = TradosUtils.generateTempTmName(); 
			analyzeWithTM(tempName,jobFile.getPath(), true);
			TradosUtils.deleteTM(tempName);
		}
		else {
			String tmPath = Util.fillRootDirectoryVariable(params.getExistingTm(), rootDir);
			tmPath = Util.fillInputRootDirectoryVariable(tmPath, inputRootDir);
			tmPath = LocaleId.replaceVariables(tmPath, sourceLocale, targetLocale);
			analyzeWithTM(tmPath,jobFile.getPath(), false);
		}
		
		if ( sendTmx ) {
			return TradosUtils.generateAltOutput(tmxOutput, "UTF-8", sourceLocale, targetLocale, "okf_tmx");
		}

		return null;
	}
	
	@Override
	public String getName () {
		return "Trados Analysis";
	}

	@Override
	public String getDescription () {
		return "Analyses a set of input files with a Trados TM."
			+ " Expects: raw document. Sends back: raw document.";
	}
	
	@Override
	public IParameters getParameters () {
		return params;
	}

	@Override
	public void setParameters (IParameters params) {
		this.params = (ParametersAnalysis)params;
	}
	
	/**
	 * TM specific analysis.
	 * @param tm The path and filename for the TM.
	 * @param job the path of the job file
	 * @param createTm true to create the TM
	 */    
    private void analyzeWithTM (String tm,
    	String job,
    	boolean createTm)
    {
    	ActiveXComponent xl = new ActiveXComponent("TW4Win.Application");
    	try {
    		//--create a new tm--
    		if ( createTm ) {
    			TradosUtils.createTM(xl, tm, sourceLocale, targetLocale, logger);
    		}
    		
    		ActiveXComponent o = xl.getPropertyAsComponent("TranslationMemory");
    		
    		if ( !params.getPass().isEmpty() ) {
            	o.invoke("Open", new Variant(tm), new Variant(params.getUser()), new Variant(params.getPass()));
    		}
    		else {
            	o.invoke("Open", new Variant(tm), new Variant(params.getUser()));    			
    		}

        	o.invoke("AnalyseFiles", new Variant(job));
        	xl.invoke("quit", new Variant[]{});

    		if ( params.getAutoOpenLog() && ( logToOpen != null )) {
    			Util.openURL((new File(logToOpen)).getAbsolutePath());
    		}

    	}
    	catch ( Exception e ) {
        	throw new OkapiIOException("Trados Analysis failed.", e);
        }
    }

}
