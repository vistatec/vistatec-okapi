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

package net.sf.okapi.steps.xliffkit.reader;

import java.net.URI;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.annotation.IAnnotation;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
import net.sf.okapi.common.pipeline.annotations.StepParameterType;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.lib.persistence.PersistenceSession;
import net.sf.okapi.lib.tkit.merge.TextUnitMerger;
import net.sf.okapi.steps.xliffkit.opc.OPCPackageReader;

/**
 * Reads XLIFF translation kit.
 * The step cannot be used after XLIFFKitWriterStep, as it cannot open a not yet existing T-kit file,
 * but it can be used to open XLIFF T-kit, modify events for an XLIFFKitWriterStep to write them out.
 */
@UsingParameters()
public class XLIFFKitReaderStep extends BasePipelineStep {

	private OPCPackageReader reader;
	private boolean isDone = true;
	private URI inputURI;
	private URI outputURI;
	//private boolean writeTargets = false;
	//private LocaleId targetLocale; 
	//private IFilterWriter filterWriter;
	private String outputEncoding;
	private Parameters params;
	private TextUnitMerger merger;
	private RawDocument rd;

	public XLIFFKitReaderStep() {
		super();
		params = new Parameters();
		merger = new TextUnitMerger();
		reader = new OPCPackageReader(merger);
	}
	
	public String getDescription () {
		return "Reads XLIFF translation kit. Expects: Raw document for T-kit. Sends back: filter events.";
	}

	public String getName () {
		return "XLIFF Kit Reader";
	}

//	@StepParameterMapping(parameterType = StepParameterType.INPUT_URI)
	// Commented out, we need inputURI be set from outside, it should point at
	// an XLIFF T-kit, not the pipeline's input file
	public void setInputURI (URI inputURI) {
		this.inputURI = inputURI;
	}
	
	@StepParameterMapping(parameterType = StepParameterType.OUTPUT_URI)
	public void setOutputURI (URI outputURI) {
		this.outputURI = outputURI;
		//writeTargets = !Util.isEmpty(outputPath);
	}
	
	public URI getOutputURI() {
		return outputURI;
	}
	
	@StepParameterMapping(parameterType = StepParameterType.TARGET_LOCALE)
	public void setTargetLocale (LocaleId targetLocale) {
		reader.setTrgLoc(targetLocale);
	}
	
	@StepParameterMapping(parameterType = StepParameterType.OUTPUT_ENCODING)
	public void setOutputEncoding (String outputEncoding) {
		this.outputEncoding = outputEncoding;
	}
	
	public String getOutputEncoding() {
		return outputEncoding;
	}
	
	@Override
	public Event handleEvent(Event event) {
		event = super.handleEvent(event); // to allow access to handleStartDocument() etc. 
		
		switch (event.getEventType()) {
		case START_BATCH:
			isDone = true;
			break;

		case START_BATCH_ITEM:
			isDone = false;
			return event;
			
		case END_DOCUMENT:
			if (rd != null) {
				rd.close();
				rd = null;
			}
			
//		case START_DOCUMENT:
		case RAW_DOCUMENT:
			isDone = false;
			if (event.isRawDocument()) {
				rd = event.getRawDocument();
			}
			else if (event.isStartDocument()) {
				// When the step is not at the head of pipeline, then START_DOCUMENT is called,
				// we need to have inputURI set from outside.				 
				StartDocument sd = event.getStartDocument();
				rd = new RawDocument(inputURI, sd.getEncoding(), sd.getLocale());
			}
			//targetLocale = rd.getTargetLocale();
			//merger.setTrgLoc(targetLocale);
//			merger.setUseApprovedOnly(params.isUseApprovedOnly());
//			merger.setUpdateApprovedFlag(params.isUpdateApprovedFlag());
			net.sf.okapi.lib.tkit.merge.Parameters mParams =
					new net.sf.okapi.lib.tkit.merge.Parameters();
			mParams.setApprovedOnly(params.getBoolean(Parameters.USE_APPROVED_ONLY));
			merger.setParameters(mParams);
			if (params.isGenerateTargets())
				reader.setGeneratorOptions(outputEncoding, outputURI.getPath(), 
                                           params.isGroupByPackagePath());
			
			//reader.setGenerateTargets(params.isGenerateTargets());
			reader.open(rd); // Annotations are deserialized here
			
			Event e = reader.next(); 
			if (e.isStartDocument()) {
				StartDocument sd = e.getStartDocument();
				for (IAnnotation annotation : getSession().getAnnotations()) {
					sd.setAnnotation(annotation);
				}
			}
			return e;
			
		default:
			break;
		}
		
		if (isDone) {
			return event;
		} else {
//			if (event.getEventType() == EventType.START_DOCUMENT) {
//				RefsAnnotation ra = new RefsAnnotation(
//						getSession().getAnnotation());
//			}
			
			
//			if (writeTargets) {
//				switch (event.getEventType()) {				
//				case START_DOCUMENT:
//					processStartDocument(event);
//					break;
//
//				case END_DOCUMENT:
//					processEndDocument(event);
//					break;
//				
//				case TEXT_UNIT:								
//				case START_SUBDOCUMENT:
//				case START_GROUP:
//				case END_SUBDOCUMENT:
//				case END_GROUP:
//				case DOCUMENT_PART:
//					if (params.isGenerateTargets())
//						filterWriter.handleEvent(event);
//				}
//			}
			
			Event e = reader.next();
			isDone = !reader.hasNext();			
//			if (isDone && e.getEventType() == EventType.END_DOCUMENT)
//				processEndDocument(e);
			
			return e;
		}			
	}
	
	@Override
	public boolean isDone() {
		return isDone;
	}

	@Override
	public void destroy() {
		reader.close();
	}

	@Override
	public void cancel() {
		reader.cancel();
	}
	
	@Override
	public IParameters getParameters() {
		return params;
	}
	
	protected PersistenceSession getSession() {
		return reader.getSession();
	}
	
//	@Override
//	protected Event handleRawDocument(Event event) {
//		event = super.handleRawDocument(event); 
//		
//		RawDocument rd = event.getRawDocument();
//		for (IAnnotation annotation : getSession().getAnnotations()) {
//			rd.setAnnotation(annotation);
//		}
//		return event;
//	}
	
//	@Override
//	protected Event handleStartBatchItem(Event event) {
//		StartBatchItem sbi = event.getStartBatchItem();
//		for (IAnnotation annotation : getSession().getAnnotations()) {
//			sbi.setAnnotation(annotation);
//		}
//		return super.handleStartBatchItem(event);
//	}
	
	protected OPCPackageReader getReader() {
		return reader;
	}
	
	protected TextUnitMerger getMerger() {
		return merger;
	}
	
//	private void processStartDocument (Event event) {
//		StartDocument startDoc = (StartDocument)event.getResource();
//		if ( outputEncoding == null ) outputEncoding = startDoc.getEncoding();
//		
////		if (params.isGenerateTargets()) {
////			filterWriter = startDoc.getFilterWriter();
////			//filterWriter.setOptions(targetLocale, outputEncoding);
////		}		
//		
//		String srcName = startDoc.getName();
//		String outFileName = outputPath + srcName;
//		
//		File outputFile = new File(outFileName);
//		Util.createDirectories(outputFile.getAbsolutePath());
//		
//		if (params.isGenerateTargets()) {
//			filterWriter.setOutput(outputFile.getAbsolutePath());
//			filterWriter.handleEvent(event);
//		}
//	}
//	
//	private void processEndDocument (Event event) {
//		if (params.isGenerateTargets()) {
//			filterWriter.handleEvent(event);
//			filterWriter.close();
//		}
//	}
}
