/*===========================================================================
  Copyright (C) 2012 by the Okapi Framework contributors
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
import net.sf.okapi.common.pipeline.annotations.StepParameterType;
import net.sf.okapi.common.resource.MultiEvent;
import net.sf.okapi.common.resource.PipelineParameters;
import net.sf.okapi.common.resource.RawDocument;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Variant;

@UsingParameters(ParametersExport.class)
public class TradosExportStep extends BasePipelineStep{

	private ParametersExport params;
	private LocaleId sourceLocale;
	private LocaleId targetLocale;
	private String rootDir;
	private String inputRootDir;
	private ActiveXComponent trados;
	private ActiveXComponent tm;
	
	public TradosExportStep () {
		params = new ParametersExport();
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
	
	@Override
	protected Event handleRawDocument (Event event) {

		RawDocument rawDoc = event.getRawDocument();
		String inputPath = new File(rawDoc.getInputURI()).getPath();
		
		try {
			if ( !params.getPass().isEmpty() ) {
				tm.invoke("Open", new Variant(inputPath), new Variant(params.getUser()), new Variant(params.getPass()));
			}
			else {
				tm.invoke("Open", new Variant(inputPath), new Variant(params.getUser()));
			}

			String outputPath = inputPath.replace(Util.getExtension(inputPath), getExtension(params.getFormat()));
			
			Variant[] importOptions = new Variant[3];
			importOptions[0] = new Variant(outputPath);
			importOptions[1] = new Variant(params.getFormat());

			String path = Util.fillRootDirectoryVariable(params.getConstraintsFile(), rootDir);
			path = Util.fillInputRootDirectoryVariable(path, inputRootDir);
			path = LocaleId.replaceVariables(path, sourceLocale, targetLocale);
			importOptions[2] = new Variant(path);

			tm.invoke("Export", importOptions);
			tm.invoke("Close");
			
			if ( params.getSendExportedFile() ) {
				List<Event> list = new ArrayList<Event>();
				// Change the pipeline parameters for the raw-document-related data
				PipelineParameters pp = new PipelineParameters();
				rawDoc = new RawDocument(new File(outputPath).toURI(), "UTF-8", sourceLocale, targetLocale);
				rawDoc.setFilterConfigId(params.getFilterConfigurationForExportFormat(params.getFormat()));
				pp.setOutputURI(rawDoc.getInputURI()); // Use same name as this output for now
				pp.setSourceLocale(rawDoc.getSourceLocale());
				pp.setTargetLocale(rawDoc.getTargetLocale());
				pp.setOutputEncoding(rawDoc.getEncoding()); // Use same as the output document
				pp.setInputRawDocument(rawDoc);
				pp.setFilterConfigurationId(rawDoc.getFilterConfigId());
				pp.setBatchInputCount(1);
				// Add the event to the list
				list.add(new Event(EventType.PIPELINE_PARAMETERS, pp));
				// Add raw-document related events
				list.add(new Event(EventType.RAW_DOCUMENT, rawDoc));
				// Return the list as a multiple-event event
				return new Event(EventType.MULTI_EVENT, new MultiEvent(list));
			}else{
				return event;
			}
			
		}
		catch ( Exception e ) {
			throw new OkapiIOException("Trados Export failed.", e);
		}

	}

	@Override
	protected Event handleStartBatch(final Event event) {
		TradosUtils.verifyJavaLibPath(new File(getClass().getProtectionDomain().getCodeSource().getLocation().getFile()));
		
		trados = new ActiveXComponent("TW4Win.Application");
		tm = trados.getPropertyAsComponent("TranslationMemory");
		
		return event;
	}
	
	@Override
	protected Event handleEndBatch(final Event event) {
		trados.invoke("quit", new Variant[] {});
		trados = null;
		tm = null;
		return event;
	}
	
	@Override
	public String getName () {
		return "Trados Export";
	}

	@Override
	public String getDescription() {
		return "Export a Trados TM to a selected format."
				+ " Expects: raw document. Sends back: raw document.";
	}
	
	@Override
	public IParameters getParameters () {
		return params;
	}

	@Override
	public void setParameters (IParameters params) {
		this.params = (ParametersExport)params;
	}
	
	/**
	 * Get extension based on the position in the list (Hardcoded)
	 * @return the selected extension
	 */
	private String getExtension (int selection) {
		switch (selection) {
		case 10: 
			return ".txt";
		case 6:
		case 8:
		case 9:
			return ".tmx";
		case 2:
			return ".rtf";
		case 1:
			return ".sgm";
		default:
			return "";
		}
	}
	
}
