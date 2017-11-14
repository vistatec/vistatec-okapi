/*===========================================================================
  Copyright (C) 2013-2014 by the Okapi Framework contributors
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

package net.sf.okapi.steps.rainbowkit.postprocess;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.IResource;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.MimeTypeMapper;
import net.sf.okapi.common.Range;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.exceptions.OkapiFileNotFoundException;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.MultiEvent;
import net.sf.okapi.common.resource.PipelineParameters;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextUnitUtil;
import net.sf.okapi.filters.rainbowkit.Manifest;
import net.sf.okapi.filters.rainbowkit.MergingInfo;
import net.sf.okapi.lib.tkit.filter.BeanEventFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides merging based on the skeleton file for a given extracted document (XLIFF).
 */
public class SkeletonMerger {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private IFilterWriter writer;
	private Manifest manifest;
	private LocaleId trgLoc;
	private boolean skipEmptySourceEntries;
	private boolean useSource;
	private boolean preserveSegmentation;
	private boolean forceSegmentationInMerge;
	private boolean returnRawDocument;
	private String overrideOutputPath;
	private RawDocument rawDoc;
	private boolean useSubDoc;
	private BeanEventFilter eventReader;
	private int errorCount;
	
	/**
	 * Creates a Merger object. 
	 * @param manifest The manifest to process.
	 * @param preserveSegmentation true to preserve the segmentation after the merge is done.
	 * @param forcedTargetLocale null to use the target locale in the manifest, otherwise: the target locale to merge.
	 * @param returnRawDocument true to return a raw document rather than filter events.
	 * @param overrideOutputPath path of the output path to use instead of the default one, use null to use the default.
	 */
	public SkeletonMerger (Manifest manifest,
		boolean preserveSegmentation,
		LocaleId forcedTargetLocale,
		boolean returnRawDocument,
		String overrideOutputPath)
	{
		this.manifest = manifest;
		if ( forcedTargetLocale != null ) {
			trgLoc = forcedTargetLocale;
		}
		else {
			trgLoc = manifest.getTargetLocale();
		}
		this.preserveSegmentation = preserveSegmentation;
		this.returnRawDocument = returnRawDocument;
		this.overrideOutputPath = overrideOutputPath;
		eventReader = new BeanEventFilter();
	}
	
	public void close () {
		if ( writer != null ) {
			writer.close();
			writer = null;
		}
		if ( eventReader != null ) {
			eventReader.close();
			eventReader = null;
		}
	}

	public Event handleEvent (Event event) {
		switch ( event.getEventType() ) {
		case TEXT_UNIT:
			processTextUnit(event);
			if ( returnRawDocument ) {
				event = Event.NOOP_EVENT;
			}
			break;
		case START_SUBDOCUMENT:
			if ( returnRawDocument ) {
				useSubDoc = true;
			}
			break;
		case END_SUBDOCUMENT:
			if ( returnRawDocument ) {
				flushFilterEvents();
				event = createMultiEvent();
			}
			break;
		case END_DOCUMENT:
			flushFilterEvents();
			close();
			if ( returnRawDocument && !useSubDoc ) {
				event = createMultiEvent();
			}
			break;
		default:
			if ( returnRawDocument ) {
				event = Event.NOOP_EVENT;
			}
		}
		
		return event;
	}
	
//	private void processStartDocument (StartDocument sd) {
//		if ( sd.getMimeType().equals(MimeTypeMapper.XLIFF_MIME_TYPE) ) {
//			net.sf.okapi.filters.xliff.Parameters prm = (net.sf.okapi.filters.xliff.Parameters)sd.getFilterParameters();
//			forceSegmentationInMerge = (prm.getOutputSegmentationType() 
//				== net.sf.okapi.filters.xliff.Parameters.SEGMENTATIONTYPE_SEGMENTED);
//		}
//		else {
//			forceSegmentationInMerge = false;
//		}
//		
//		// Create the writer
//		writer = sd.getFilterWriter();
//	}
	
	private Event createMultiEvent () {
		List<Event> list = new ArrayList<Event>();
		
		// Change the pipeline parameters for the raw-document-related data
		PipelineParameters pp = new PipelineParameters();
		pp.setOutputURI(rawDoc.getInputURI()); // Use same name as this output for now
		pp.setSourceLocale(rawDoc.getSourceLocale());
		pp.setTargetLocale(rawDoc.getTargetLocale());
		pp.setOutputEncoding(rawDoc.getEncoding()); // Use same as the output document
		pp.setInputRawDocument(rawDoc);
		// Add the event to the list
		list.add(new Event(EventType.PIPELINE_PARAMETERS, pp));

		// Add raw-document related events
		list.add(new Event(EventType.RAW_DOCUMENT, rawDoc));
		
		// Return the list as a multiple-event event
		return new Event(EventType.MULTI_EVENT, new MultiEvent(list));
	}

	public Event startMerging (MergingInfo info,
		Event event)
	{
		errorCount = 0;
		useSubDoc = false;
		logger.info("Merging: {}", info.getRelativeInputPath());

		// Get the skeleton file
		String skelPath = manifest.getTempSkelDirectory() + info.getRelativeInputPath() + ".skl";
		try {
			eventReader.open(new RawDocument(new FileInputStream(skelPath), null, trgLoc));
		}
		catch ( FileNotFoundException e ) {
			throw new OkapiFileNotFoundException("Skeleton file not found.", e);
		}
		// Skip entries with empty source for PO
		skipEmptySourceEntries = ( info.getExtractionType().equals(Manifest.EXTRACTIONTYPE_PO)
			|| info.getExtractionType().equals(Manifest.EXTRACTIONTYPE_TRANSIFEX)
			|| info.getExtractionType().equals(Manifest.EXTRACTIONTYPE_TABLE) );
		// Use the source of the input as the translation for XINI, etc.
		useSource = info.getExtractionType().equals(Manifest.EXTRACTIONTYPE_ONTRAM);
		
		String outPath = this.getOutputPath(info);
		Event skelEvent = eventReader.next();
		
		// Process the start document in the document we just open
		if (( skelEvent == null ) || ( skelEvent.getEventType() != EventType.START_DOCUMENT )) {
			errorCount++;
			logger.error("The start document event is missing when parsing the skeleton file.");
		}
		else {
			StartDocument sd = skelEvent.getStartDocument();
			if ( sd.getMimeType().equals(MimeTypeMapper.XLIFF_MIME_TYPE) ) {
				net.sf.okapi.filters.xliff.Parameters prm = (net.sf.okapi.filters.xliff.Parameters)sd.getFilterParameters();
				forceSegmentationInMerge = (prm.getOutputSegmentationType() 
					== net.sf.okapi.filters.xliff.Parameters.SEGMENTATIONTYPE_SEGMENTED);
			}
			else {
				forceSegmentationInMerge = false;
			}
			// Create and setup the writer
			writer = sd.getFilterWriter();
			writer.setOptions(trgLoc, info.getTargetEncoding());
			writer.setOutput(outPath);
			IParameters fprm = writer.getParameters();
			if ( fprm != null ) {
				fprm.fromString(info.getFilterParameters());
			}
			// Write the initial event
			writer.handleEvent(skelEvent);
		}
		
		// Compute what event to return
		if ( returnRawDocument ) {
			if ( event.getStartDocument().isMultilingual() ) {
				rawDoc = new RawDocument(new File(outPath).toURI(), info.getTargetEncoding(),
					manifest.getSourceLocale(), manifest.getTargetLocale());
			}
			else {
				// Otherwise: the previous target is now the source (and still the target)
				rawDoc = new RawDocument(new File(outPath).toURI(), info.getTargetEncoding(),
					manifest.getTargetLocale(), manifest.getTargetLocale());
			}
			event = Event.NOOP_EVENT;
		}
		else {
			event = skelEvent;
		}
		
		return event;
	}
	
	private String getOutputPath (MergingInfo info) {
		if ( Util.isEmpty(this.overrideOutputPath) ) {
			return manifest.getMergeDirectory() + info.getRelativeTargetPath();
		}
		else {
			return Util.ensureSeparator(overrideOutputPath, false) + info.getRelativeTargetPath();
		}
	}

	private void flushFilterEvents () {
		// Finish to go through the skeleton
		Event event = null;
		while ( eventReader.hasNext() ) {
			event = eventReader.next();			
			writer.handleEvent(event);
		}
		writer.close();
	}
	
	private void processTextUnit (Event event) {
		// Get the unit from the translation file
		ITextUnit traTu = event.getTextUnit();
		// If it's not translatable:
		if ( !traTu.isTranslatable() ) {
			return; // Do nothing: the original will be handled by processUntilTextUnit()
		}
		// search for the corresponding event in the original
		Event oriEvent = processUntilTextUnit();
		if ( oriEvent == null ) {
			errorCount++;
			logger.error("No corresponding text unit for id='{}' in the original file.", traTu.getId());
			return;
		}
		// Get the actual text unit object of the original
		ITextUnit oriTu = oriEvent.getTextUnit();

		// Check the IDs
		if ( !traTu.getId().equals(oriTu.getId()) ) {
			errorCount++;
			logger.error("De-synchronized files: translated TU id='{}', Original TU id='{}'.",
				traTu.getId(), oriTu.getId());
			return;
		}
		
		// Check if we have a translation
		TextContainer trgTraCont;
		if ( useSource ) trgTraCont = traTu.getSource();
		else trgTraCont = traTu.getTarget(trgLoc);
		
		if ( trgTraCont == null ) {
			if ( oriTu.getSource().hasText() ) {
				// Warn only if there source is not empty
				logger.warn("No translation found for TU id='{}'. Using source instead.", traTu.getId());
			}
			writer.handleEvent(oriEvent); // Use the source
			return;
		}
		
		// Process the "approved" property
		boolean isTransApproved = false;
		Property traProp;
		if ( useSource ) traProp = traTu.getSourceProperty(Property.APPROVED);
		else traProp = traTu.getTargetProperty(trgLoc, Property.APPROVED);
		
		if ( traProp != null ) {
			isTransApproved = traProp.getValue().equals("yes");
		}
		if ( !isTransApproved && manifest.getUseApprovedOnly() ) {
			// Not approved: use the source
			logger.warn("Item id='{}': Target is not approved. Using source instead.", traTu.getId());
			writer.handleEvent(oriEvent); // Use the source
			return;
		}
		
		// Do we need to preserve the segmentation for merging (e.g. TTX case)
		boolean mergeAsSegments = false;
		if ( oriTu.getMimeType() != null ) { 
			if ( oriTu.getMimeType().equals(MimeTypeMapper.TTX_MIME_TYPE)
				|| oriTu.getMimeType().equals(MimeTypeMapper.XLIFF_MIME_TYPE) ) {
				mergeAsSegments = true;
			}
		}
		
		// Set the container for the source
		TextContainer srcOriCont = oriTu.getSource();
		TextContainer srcTraCont = traTu.getSource();
		
		if ( forceSegmentationInMerge ) {
			// Use the source of the target to get the proper segmentations
			if ( !srcOriCont.getUnSegmentedContentCopy().getCodedText().equals(
				srcTraCont.getUnSegmentedContentCopy().getCodedText()) ) {
				logger.warn("Item id='{}': Original source and source in the translated file are different.\n"
					+ "Cannot use the source of the translation as the new segmented source.",
					traTu.getId());
			}
		}

		// If we do not need to merge segments then we must join all for the merge
		// We also remember the ranges to set them back after merging
		List<Range> srcRanges = null;
		List<Range> trgRanges = null;
		
		// Merge the segments together for the code transfer
		// This allows to move codes anywhere in the text unit, not just each part.
		// We do remember the ranges because some formats will required to be merged by segments
		if ( !srcOriCont.contentIsOneSegment() ) {
			if ( mergeAsSegments ) {
				// Make sure we remember the source segment if we merge as segments later
				// Otherwise the segment id of the source and target will defer
				srcRanges = srcOriCont.getSegments().getRanges();
			}
			srcOriCont.joinAll();
		}
		if ( forceSegmentationInMerge ) {
			// Get the source segmentation from the translated file if it's 
			// a forced segmentation
			if ( !srcTraCont.contentIsOneSegment() ) {
				srcRanges = srcTraCont.getSegments().getRanges();
			}
		}
		else {
			// Else: take from the original source
			if ( !srcOriCont.contentIsOneSegment() ) {
				srcRanges = srcOriCont.getSegments().getRanges();
			}
		}
		if ( !trgTraCont.contentIsOneSegment() ) {
			trgRanges = trgTraCont.getSegments().getRanges();
			trgTraCont.joinAll();
		}

		// Perform the transfer of the inline codes
		// At this point we have a single segment/part
		TextUnitUtil.copySrcCodeDataToMatchingTrgCodes(srcOriCont.getFirstContent(),
			trgTraCont.getFirstContent(), true, true, null, oriTu);
		
		// Resegment for the special formats
		if ( mergeAsSegments ) {
			if ( srcRanges != null ) {
				srcOriCont.getSegments().create(srcRanges, true);
			}
			if ( trgRanges != null ) {
				trgTraCont.getSegments().create(trgRanges, true);
			}
		}

		// Check if the target has more segments
		if ( srcOriCont.getSegments().count() < trgTraCont.getSegments().count() ) {
			logger.warn("Item id='{}': There is at least one extra segment in the translation file.\n"
				+ "Extra segments are not merged into the translated output.",
				traTu.getId());
		}
		
		// Assign the translated target to the target text unit
		oriTu.setTarget(trgLoc, trgTraCont);
		
		// Update/add the 'approved' flag of the entry to merge if available
		// (for example to remove the fuzzy flag in POs or set the approved attribute in XLIFF)
		if ( manifest.getUpdateApprovedFlag() ) {
			Property oriProp = oriTu.createTargetProperty(trgLoc, Property.APPROVED, false, IResource.CREATE_EMPTY);
			if ( traProp != null ) {
				oriProp.setValue(traProp.getValue());
			}
			else {
				oriProp.setValue("yes");
			}
		}
		
		// Output the translation
		writer.handleEvent(oriEvent);
		
		// Set back the segmentation if we modified it for the merge
		// (Already done if it's a special format)
		if ( preserveSegmentation && !mergeAsSegments ) {
			if ( srcRanges != null ) {
				srcOriCont.getSegments().create(srcRanges, true);
			}
			if ( trgRanges != null ) {
				trgTraCont.getSegments().create(trgRanges, true);
			}
		}
	}

	/**
	 * Get events in the original document until the next text unit.
	 * Any event before is passed to the writer.
	 * @return the event of the next text unit, or null if no next text unit is found.
	 */
	private Event processUntilTextUnit () {
		Event event = null;
		while ( true ) {
			event = eventReader.next();
			if ( event == null ) break; // No more events
			// Process that event
			if ( event.getEventType() == EventType.TEXT_UNIT ) {
				ITextUnit tu = event.getTextUnit();
				if ( !tu.isTranslatable() ) {
					// Do not merge the translation for non-translatable
					writer.handleEvent(event);
					continue;
				}
				if ( skipEmptySourceEntries && tu.isEmpty() ) {
					// For some types of package: Do not merge the translation for non-translatable
					writer.handleEvent(event);
					continue;
				}
				return event;
			}
			// Else: write out the event
			writer.handleEvent(event);
		}
		// This text unit is extra in the translated file
		return null;
	}
	
	/**
	 * Gets the number of errors since the last call to {@link #startMerging(MergingInfo, Event)}.
	 * @return the number of errors. 
	 */
	public int getErrorCount () {
		return errorCount;
	}
	
}
