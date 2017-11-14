/*===========================================================================
  Copyright (C) 2010-2013 by the Okapi Framework contributors
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

package net.sf.okapi.steps.rainbowkit.xliff;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.filterwriter.XLIFFWriter;
import net.sf.okapi.common.filterwriter.XLIFFWriterParameters;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.filters.rainbowkit.Manifest;
import net.sf.okapi.filters.rainbowkit.MergingInfo;
import net.sf.okapi.lib.tkit.writer.BeanEventWriter;
import net.sf.okapi.lib.tkit.writer.Parameters;
import net.sf.okapi.steps.rainbowkit.common.BasePackageWriter;

public class XLIFFPackageWriter extends BasePackageWriter {

	protected XLIFFWriter writer;
	private Options options;
	private boolean preSegmented = false;
	private boolean forOmegat = false;
	private String rawDocPath;
	
	private String skelPath;
	private BeanEventWriter skeletonWriter = null;
	private Parameters skeletonWriterParams = null;

	public XLIFFPackageWriter () {
		super(Manifest.EXTRACTIONTYPE_XLIFF);
	}

	/**
	 * Indicates if at least one text unit so far has been segmented.
	 * @return true if at least one text unit so far has been segmented
	 */
	public boolean getPreSegmented () {
		return preSegmented;
	}
	
	public void setForOmegat (boolean forOmegat) {
		this.forOmegat = forOmegat;
	}
	
	@Override
	public Event handleEvent (Event event) {
		switch ( event.getEventType() ) {
		case START_BATCH:
			processStartBatch();
			break;
		case END_BATCH:
			processEndBatch();
			break;
		case START_BATCH_ITEM:
			processStartBatchItem();
			break;
		case END_BATCH_ITEM:
			processEndBatchItem();
			break;
		case START_DOCUMENT:
			processStartDocument(event);
			break;
		case END_DOCUMENT:
			processEndDocument(event);
			break;
		case START_SUBDOCUMENT:
			processStartSubDocument(event);
			break;
		case END_SUBDOCUMENT:
			processEndSubDocument(event);
			break;
		case DOCUMENT_PART:
			processDocumentPart(event);
			break;
		case START_GROUP:
			processStartGroup(event);
			break;
		case END_GROUP:
			processEndGroup(event);
			break;
		case TEXT_UNIT:
			processTextUnit(event);
			break;
		case NO_OP:
			// skip NO_OP event
			return event;
		case START_SUBFILTER:
		case END_SUBFILTER:
		case PIPELINE_PARAMETERS:
		case RAW_DOCUMENT:
		case CANCELED:
		case CUSTOM:
		case MULTI_EVENT:
			event = super.handleEvent(event);
			break;
		}
		if ( skeletonWriter != null ) {
			skeletonWriter.handleEvent(event);
		}
		return event;
	}
	
	@Override
	protected void processStartBatch () {
		skelPath = null; // Used as a flag for when we use the skeleton or not
		manifest.setSubDirectories("original", "work", "work", "done", null, "skeleton", false);
		
		options = new Options();
		if ( !Util.isEmpty(params.getWriterOptions()) ) {
			options.fromString(params.getWriterOptions());
		}
		if ( forOmegat ) {
			// XLIFF options for OmegaT
			options.setCopySource(true);
			options.setIncludeAltTrans(false);
			options.setIncludeCodeAttrs(true);
			options.setUseSkeleton(false);
		}
		
		setTMXInfo(true, null, options.getPlaceholderMode(), true, false);
		super.processStartBatch();
	}
	
	@Override
	protected void processStartDocument (Event event) {
		super.processStartDocument(event);
		
		writer = new XLIFFWriter();
		writer.setOptions(manifest.getTargetLocale(), "UTF-8");
		MergingInfo item = manifest.getItem(docId);
		rawDocPath = manifest.getTempSourceDirectory() + item.getRelativeInputPath() + ".xlf";
		writer.setOutput(rawDocPath); // Not really used, but doesn't hurt just in case

		XLIFFWriterParameters paramsXliff = (XLIFFWriterParameters)writer.getParameters();
		// Set the writer's options
		if ( forOmegat ) {
			// Direct setting for the writer (not an XLIFF option)
			paramsXliff.setUseSourceForTranslated(true);
		}
		paramsXliff.setPlaceholderMode(options.getPlaceholderMode());
		paramsXliff.setCopySource(options.getCopySource());
		paramsXliff.setIncludeAltTrans(options.getIncludeAltTrans());
		paramsXliff.setSetApprovedAsNoTranslate(options.getSetApprovedAsNoTranslate());
		paramsXliff.setIncludeNoTranslate(options.getIncludeNoTranslate());
		paramsXliff.setIncludeCodeAttrs(options.getIncludeCodeAttrs());
		paramsXliff.setIncludeIts(options.getIncludeIts());

		// Initialize the skeleton variables if needed
		if ( options.getUseSkeleton() ) {
			skeletonWriterParams = new Parameters();
			skeletonWriterParams.setMessage(params.getMessage());
			// Remove targets only for monolingual formats
			skeletonWriterParams.setRemoveTarget(!event.getStartDocument().isMultilingual());
			skelPath = manifest.getTempSkelDirectory() + item.getRelativeInputPath() + ".skl";
			item.setUseSkeleton(true);
			skeletonWriter = new BeanEventWriter();
			skeletonWriter.setParameters(skeletonWriterParams);
			skeletonWriter.setOptions(manifest.getTargetLocale(), "UTF-8");
			skeletonWriter.setOutput(skelPath);
		}
		
		StartDocument sd = event.getStartDocument();
		writer.create(rawDocPath, null, manifest.getSourceLocale(), manifest.getTargetLocale(),
			sd.getMimeType(), item.getRelativeInputPath(), null);
	}
	
	@Override
	protected Event processEndDocument (Event event) {
		if ( writer != null ) {
			writer.handleEvent(event);
			writer.close();
			writer = null;
		}
		
		if ( params.getSendOutput() ) {
			return super.creatRawDocumentEventSet(rawDocPath, "UTF-8",
				manifest.getSourceLocale(), manifest.getTargetLocale());
		}
		else {
			return event;
		}
	}

	@Override
	protected void processStartSubDocument (Event event) {
		writer.handleEvent(event);
	}
	
	@Override
	protected void processEndSubDocument (Event event) {
		writer.handleEvent(event);
	}
	
	@Override
	protected void processStartGroup (Event event) {
		writer.handleEvent(event);
	}
	
	@Override
	protected void processEndGroup (Event event) {
		writer.handleEvent(event);
	}
	
	@Override
	protected void processTextUnit (Event event) {
		// XLIFF
		event = writer.handleEvent(event);
		// TMX files
		writeTMXEntries(event.getTextUnit());
		// Check if it has been segmented (if not set already)
		if ( !preSegmented ) {
			preSegmented = event.getTextUnit().getSource().hasBeenSegmented();
		}
	}

	@Override
	public void close () {
		if ( writer != null ) {
			writer.close();
			writer = null;
		}
		if (skeletonWriter != null) {
			skeletonWriter.close();
			skeletonWriter = null;
		}
	}

	@Override
	public String getName () {
		return getClass().getName();
	}
}
