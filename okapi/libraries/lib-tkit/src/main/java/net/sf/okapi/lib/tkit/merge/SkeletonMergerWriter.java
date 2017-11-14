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

package net.sf.okapi.lib.tkit.merge;

import java.io.File;
import java.io.OutputStream;
import java.net.URISyntaxException;

import javax.xml.stream.events.EndDocument;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.exceptions.OkapiFileNotFoundException;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.exceptions.OkapiMergeException;
import net.sf.okapi.common.exceptions.OkapiUnexpectedResourceTypeException;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.TextUnit;
import net.sf.okapi.common.skeleton.ISkeletonWriter;
import net.sf.okapi.filters.xliff.XLIFFFilter;
import net.sf.okapi.lib.tkit.filter.BeanEventFilter;

/**
 * Basic class for skeleton-based merging. <b>Override for specific behaviors.</b>
 * <p>
 * Takes a skeleton file serialized as json with lib-persistence and
 * {@link Event}s from a translated document. Translated segments are merged
 * into the skeleton {@link TextUnit} events and written out using the default
 * {@link IFilterWriter}
 * 
 * @author jimh
 *
 */
public class SkeletonMergerWriter implements IFilterWriter {
	private IFilter skeletonFilter;
	private String outputPath;
	private OutputStream outputStream;
	private LocaleId targetLocale;
	private String encoding;
	private IFilterWriter writer;
	private Parameters params;
	private ITextUnitMerger textUnitMerger;

	/**
	 * Use default {@link TextUnitMerger}, {@link IFilter} 
	 * and {@link IFilterWriter}
	 */
	public SkeletonMergerWriter() {
		skeletonFilter = new BeanEventFilter();
		params = new Parameters();
		textUnitMerger = new TextUnitMerger();
	}
	
	/**
	 * Use specific {@link IFilter} and {@link IFilterWriter} implementations
	 * @param skeletonFilter - {@link IFilter} used to read skeleton, can be serialized
	 * events or original source file. <b>ASSUME FILTER OPEN ALREADY CALLED</b>
	 * @param writer - override the writer specified in the skeleton {@link StartDocument} 
	 * event. Can use null value for writer to use the default writer.
	 */
	public SkeletonMergerWriter(IFilter skeletonFilter, IFilterWriter writer) {
		this.skeletonFilter = skeletonFilter;
		this.writer = writer;
		params = new Parameters();
		textUnitMerger = new TextUnitMerger();
	}

	@Override
	public String getName() {
		return getClass().getName();
	}

	@Override
	public void setOptions(LocaleId locale, String defaultEncoding) {
		this.targetLocale = locale;
		this.encoding = defaultEncoding;
	}

	@Override
	public void setOutput(String path) {
		this.outputPath = path;
	}

	@Override
	public void setOutput(OutputStream output) {
		this.outputStream = output;
	}

	/*
	 * Return the passed in event unaltered. 
	 */
	@Override
	public Event handleEvent(Event event) {
		switch (event.getEventType()) {
		case START_DOCUMENT:
			processStartDocument(event);
			break;
		case TEXT_UNIT:
			processTextUnit(event);
			break;
		case END_DOCUMENT:
			processEndDocument(event);
			break;
		case NO_OP:
		case START_SUBDOCUMENT:
		case END_SUBDOCUMENT:
		case CANCELED:
		case CUSTOM:
		case DOCUMENT_PART:
		case END_BATCH:
		case END_BATCH_ITEM:
		case END_GROUP:
		case END_SUBFILTER:
		case MULTI_EVENT:
		case PIPELINE_PARAMETERS:
		case RAW_DOCUMENT:
		case START_BATCH:
		case START_BATCH_ITEM:
		case START_GROUP:
		case START_SUBFILTER:
		default:
			break;
		}
		return event;
	}

	@Override
	public void close() {
		if (writer != null) writer.close();
		// must null so new writer type will be 
		// instantiated on next use
		writer = null;
		skeletonFilter.close();
	}

	@Override
	public IParameters getParameters() {
		return params;
	}

	@Override
	public void setParameters(IParameters params) {
		this.params = (Parameters)params;
	}

	@Override
	public void cancel() {
		close();
	}

	@Override
	public EncoderManager getEncoderManager() {
		return null;
	}

	@Override
	public ISkeletonWriter getSkeletonWriter() {
		return null;
	}

	/**
	 * Use the skeleton {@link StartDocument} event to initialize the
	 * {@link IFilterWriter}. Initialize the {@link ITextUnitMerger}
	 * @param event - the translated version of the {@link StartDocument} event
	 */
	protected void processStartDocument(Event event) {
		try {
			// if not SerializedEventFilter instance then the filter's open as already been called
			if (skeletonFilter instanceof BeanEventFilter) { 
				// InputStream always has precedence				
				if (params.getSkeletonInputStream() != null) {
					skeletonFilter.open(new RawDocument(params.getSkeletonInputStream(), "UTF-8", targetLocale));
				} else {
					skeletonFilter.open(new RawDocument(params.getSkeletonUri(), "UTF-8", targetLocale));
				}
			}
		} catch (URISyntaxException e) {
			throw new OkapiFileNotFoundException("Skeleton file not found", e);
		}

		Event skelEvent = skeletonFilter.next();

		// Process the start document in the document we just open
		if ((skelEvent == null) || (!skelEvent.isStartDocument())) {
			throw new OkapiUnexpectedResourceTypeException(
					"The start document event can't be found in the skeleton file.");
		} else {
			StartDocument sd = skelEvent.getStartDocument();
			
//			// update the skeleton metadata for new 
//			// encoding defined by the writer
//			sd.setEncoding(encoding, sd.hasUTF8BOM());
			// MIF filter tests fail if its original SD encoding is changed
			
			// Create and setup the writer, unless one has already been passed to us
			if (writer == null) {
				writer = sd.getFilterWriter();
			}
			writer.setOptions(targetLocale, encoding);
			if (outputStream != null) {
				writer.setOutput(outputStream);
			} else if (!Util.isEmpty(outputPath)) {
				writer.setOutput(outputPath);
			} else {
				throw new OkapiIOException("Output path or stream not defined for filter writer");
			}

			// Write the initial event
			writer.handleEvent(skelEvent);
			
			textUnitMerger.setParameters(params);
			textUnitMerger.setTargetLocale(targetLocale);
		}
	}

	/**
	 * Take the translated {@link TextUnit} and match it up with its corresponding
	 * skeleton version. Call {@link ITextUnitMerger} to merge the translated segments 
	 * into the skeleton {@link TextUnit}
	 * @param event - the translated version of the {@link ITextUnit} event
	 */
	protected void processTextUnit(Event event) {
		// Get the unit from the translation file
		ITextUnit tuFromTrans = event.getTextUnit();

		// search for the corresponding event in the original
		Event oriEvent = processUntilTextUnit();
		if (oriEvent == null) {
			throw new OkapiMergeException(String.format(
					"No corresponding text unit for id='%s' in the skeleton file.", tuFromTrans.getId()));
		}

		// Get the actual text unit of the skeleton
		ITextUnit tuFromSkel = oriEvent.getTextUnit();
		if (!tuFromSkel.isTranslatable()) {
			// if not translatable write out skeleton version
			writer.handleEvent(new Event(EventType.TEXT_UNIT, tuFromSkel));
			return;
		}

		// even if the skeleton source is empty the target may not be
		// so merge it. Also the tuFromTrans may have skeleton we 
		// don't want. Merge will take care if this too
		
		// return the (possibly) merged TextUnit
		ITextUnit mergedTu = textUnitMerger
				.mergeTargets(tuFromSkel, tuFromTrans);

		// write out (possibly) merged TextUnit
		writer.handleEvent(new Event(EventType.TEXT_UNIT, mergedTu));		
	}

	/**
	 * Get events in the original document until the next text unit. Any event
	 * before is passed to the writer.
	 * 
	 * @return the event of the next text unit, or null if no next text unit is
	 *         found.
	 */
	protected Event processUntilTextUnit() {
		Event event = null;
		while (skeletonFilter.hasNext()) {
			event = skeletonFilter.next();

			// No more events
			if (event == null) {
				return event;
			}

			// Process that event
			if (event.isTextUnit()) {
				return event;
			}

			// write out the non-TextUnit event
			writer.handleEvent(event);
		}

		return event;
	}

	/**
	 * There are no more {@link TextUnit}s. Read the remaining skeleton
	 * events and write them out.
	 *  
	 * @param event - the translated version of the {@link EndDocument} event
	 */
	protected void processEndDocument(Event event) {
		flushFilterEvents();
	}

	private void flushFilterEvents() {
		try {
			// Finish the skeleton events
			Event event = null;
			while (skeletonFilter.hasNext()) {
				event = skeletonFilter.next();
				if (event.isTextUnit()) {
					throw new OkapiMergeException(String.format(
							"No corresponding text unit for id='%s' in the skeleton file.", event.getTextUnit().getId()));
				}
				writer.handleEvent(event);
			} 
		} finally {
			writer.close();
		}
	}

	/**
	 * Set the {@link IFilterWriter} used to write out the skeleton events.
	 * This will override the internal writer as defined by {@link StartDocument}
	 * in the skeleton.
	 * <p>
	 * <b>Must be called immediately after construction!!</b>
	 * 
	 * @param writer - {@link IFilterWriter} used to write out the skeleton events
	 */
	public void setWriter(IFilterWriter writer) {
		this.writer = writer;
	}

	public void setTextUnitMerger(ITextUnitMerger textUnitMerger) {
		this.textUnitMerger = textUnitMerger;
	}

	/**
	 * Helper method to merge a document.
	 * @param skelPath the path of the skeleton file.
	 * @param xlfPath the path of the translated XLIFF document
	 * (or null to infer it from the name of the skeleton file) 
	 * @param outputPath the path of the output file.
	 * @param srcLoc the source locale.
	 * @param trgLoc the target locale.
	 * @param outputEncoding the target encoding.
	 */
	public static void mergeFromSkeleton (String skelPath,		
		String xlfPath,
		String outputPath,
		LocaleId srcLoc,
		LocaleId trgLoc,
		String outputEncoding)
	{
		XLIFFFilter xlfFilter = null;
		SkeletonMergerWriter merger = null;
		try {		
			// Determine the XLIFF document input		
			if ( xlfPath == null ) { // No preset path: guess it from the skeleton
				xlfPath = skelPath;
				int n = xlfPath.lastIndexOf('.');
				if ( n > -1 )
					xlfPath = xlfPath.substring(0, n);
				xlfPath += ".xlf";
			}
			// Create and set up the merger writer
			merger = new SkeletonMergerWriter();
			File skelFile = new File(skelPath);
			((net.sf.okapi.lib.tkit.merge.Parameters)merger.getParameters()).setSkeletonUri(skelFile.toURI());
			merger.setOptions(trgLoc, outputEncoding);
			merger.setOutput(outputPath);
			// Create and set up the XLIFF filter
			File xlfFile = new File(xlfPath);
			@SuppressWarnings("resource")
			// xlfRawDoc closed in XliffFilter
			RawDocument xlfRawDoc = new RawDocument(xlfFile.toURI(), "UTF-8", srcLoc, trgLoc);
			xlfFilter = new XLIFFFilter();
			xlfFilter.open(xlfRawDoc);
			while (xlfFilter.hasNext()) {
				merger.handleEvent(xlfFilter.next());
			}
		}
		finally {
			if ( merger != null ) merger.close();
			if ( xlfFilter != null ) xlfFilter.close();
		}
	}

}
