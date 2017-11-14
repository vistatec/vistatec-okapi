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
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.resource.MultiEvent;
import net.sf.okapi.common.resource.PipelineParameters;
import net.sf.okapi.common.resource.RawDocument;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;

@UsingParameters(ParametersMSWordResaver.class)
public class MSWordResaverStep extends BasePipelineStep{

	private ParametersMSWordResaver params;
	
	ActiveXComponent word;
	
	public MSWordResaverStep () {
		params = new ParametersMSWordResaver();
	}

	@Override
	protected Event handleRawDocument (Event event) {

		RawDocument rawDoc = event.getRawDocument();
		
		String inputPath = new File(rawDoc.getInputURI()).getPath();
		String outputPath = inputPath.replace(Util.getExtension(inputPath),getExtension(params.getFormat()));
		
		Dispatch oDocuments = word.getProperty("Documents").toDispatch(); 
		Dispatch oDocument = Dispatch.call(oDocuments, "Open", inputPath).toDispatch(); 
		Dispatch.call(oDocument, "SaveAs", outputPath, new Variant(params.getFormat()));
		Dispatch.call(oDocument, "Close"); 

		if ( params.getSendNew() ) {
			List<Event> list = new ArrayList<Event>();
			// Change the pipeline parameters for the raw-document-related data
			PipelineParameters pp = new PipelineParameters();
			rawDoc = new RawDocument(new File(outputPath).toURI(), rawDoc.getEncoding(), rawDoc.getSourceLocale(), rawDoc.getTargetLocale());
			pp.setOutputURI(rawDoc.getInputURI()); // Use same name as this output for now
			pp.setSourceLocale(rawDoc.getSourceLocale());
			pp.setTargetLocale(rawDoc.getTargetLocale());
			pp.setOutputEncoding(rawDoc.getEncoding()); // Use same as the output document
			pp.setInputRawDocument(rawDoc);
			pp.setBatchInputCount(1);
			// Add the event to the list
			list.add(new Event(EventType.PIPELINE_PARAMETERS, pp));
			// Add raw-document related events
			list.add(new Event(EventType.RAW_DOCUMENT, rawDoc));
			// Return the list as a multiple-event event
			return new Event(EventType.MULTI_EVENT, new MultiEvent(list));
		}
		else {
			return event;
		}
	}

	@Override
	protected Event handleStartBatch(final Event event) {
		TradosUtils.verifyJavaLibPath(new File(getClass().getProtectionDomain().getCodeSource().getLocation().getFile()));
		
		word = new ActiveXComponent("Word.Application");
		word.setProperty("Visible", new Variant(true));
		return event;
	}
	
	@Override
	protected Event handleEndBatch(final Event event) {
	    word.invoke("Quit");
		word = null;
		return event;
	}
	
	@Override
	public String getName() {
		return "MS Word Resaver";
	}

	@Override
	public String getDescription() {
		return "Open a file in MS Word and Save As selected output format."
				+ " Expects: raw document. Sends back: raw document.";
	}
	
	@Override
	public IParameters getParameters () {
		return params;
	}

	@Override
	public void setParameters (IParameters params) {
		this.params = (ParametersMSWordResaver)params;
	}
	
	/**
	 * Get extension based on the position in the list (Hardcoded)
	 * @return the selected extension.
	 */
	private String getExtension (int selection){
		switch (selection) {
		case 16: 
			return ".docx";
		case 6:
			return ".rtf";
		case 0:
			return ".doc";
		default:
			return "";
		}
	}
	
}
