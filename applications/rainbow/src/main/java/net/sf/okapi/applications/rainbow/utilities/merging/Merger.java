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

package net.sf.okapi.applications.rainbow.utilities.merging;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.applications.rainbow.packages.IReader;
import net.sf.okapi.applications.rainbow.packages.Manifest;
import net.sf.okapi.applications.rainbow.packages.ManifestItem;
import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.IResource;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.MimeTypeMapper;
import net.sf.okapi.common.Range;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.filters.DefaultFilters;
import net.sf.okapi.common.filters.FilterConfigurationMapper;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.filters.rtf.RTFFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Merger {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private Manifest manifest;
	private IReader reader;
	private FilterConfigurationMapper mapper;
	private IFilter inpFilter;
	private IFilterWriter outFilter;
	private RTFFilter rtfFilter;
	private LocaleId trgLoc;

	public Merger () {
		// Load the filter configurations
		mapper = new FilterConfigurationMapper();
		DefaultFilters.setMappings(mapper, false, true);
		// No need to load custom configuration because we are loading the parameters ourselves
	}

	public void initialize (Manifest manifest) {
		// Close any previous reader
		if ( reader != null ) {
			reader.closeDocument();
			reader = null;
		}
		// Set the manifest and the options
		this.manifest = manifest;
		trgLoc = manifest.getTargetLanguage();
	}
	
	public void execute (int docId) {
		ManifestItem item = manifest.getItem(docId);
		// Skip items not selected for merge
		if ( !item.selected() ) return;

		// Merge or convert depending on the post-processing selected
		if ( item.getPostProcessingType().equals(ManifestItem.POSPROCESSING_TYPE_RTF) ) {
			convertFromRTF(docId, item);
		}
		else { // Default: use the reader-driven process
			merge(docId, item);
		}
	}
	
	private void convertFromRTF (int docId,
		ManifestItem item)
	{
		OutputStreamWriter writer = null;
		try {
			// File to convert
			String fileToConvert = manifest.getFileToMergePath(docId);

			// Instantiate the reader if needed
			if ( rtfFilter == null ) {
				rtfFilter = new RTFFilter();
			}

			logger.info("\nConverting: {}", fileToConvert);
			
			//TODO: get LB info from original
			String lineBreak = Util.LINEBREAK_DOS;
			
			// Open the RTF input
			File f = new File(fileToConvert);
			//TODO: guess encoding based on language
			rtfFilter.open(new RawDocument(f.toURI(), "windows-1252", manifest.getTargetLanguage()));
				
			// Open the output document
			// Initializes the output
			String outputFile = manifest.getFileToGeneratePath(docId);
			Util.createDirectories(outputFile);
			writer = new OutputStreamWriter(new BufferedOutputStream(
				new FileOutputStream(outputFile)), item.getOutputEncoding());
			//TODO: check BOM option from original
			Util.writeBOMIfNeeded(writer, false, item.getOutputEncoding());
				
			// Process
			StringBuilder buf = new StringBuilder();
			while ( rtfFilter.getTextUntil(buf, -1, 0) == 0 ) {
				writer.write(buf.toString());
				writer.write(lineBreak);
			}
			
		}		
		catch ( Exception e ) {
			// Log and move on to the next file
			Throwable e2 = e.getCause();
			logger.error("Conversion error. {}", ((e2!=null) ? e2.getMessage() : e.getMessage()), e);
		}
		finally {
			if ( rtfFilter != null ) {
				rtfFilter.close();
			}
			if ( writer != null ) {
				try {
					writer.close();
				}
				catch ( IOException e ) {
					logger.error("Conversion error when closing file. {}", e.getMessage(), e);
				}
			}
		}
	}
	
	private void merge (int docId,
		ManifestItem item)
	{
		Event event;
		try {
			// File to merge
			String fileToMerge = manifest.getFileToMergePath(docId);
			// Instantiate a package reader of the proper type
			if ( reader == null ) {
				reader = (IReader)Class.forName(manifest.getReaderClass()).newInstance();
			}
			logger.info("\nMerging: {}", fileToMerge);

			// Original and parameters files
			String originalFile = manifest.getRoot() + File.separator + manifest.getOriginalLocation()
				+ File.separator + String.format("%d.ori", docId);
			String paramsFile = manifest.getRoot() + File.separator + manifest.getOriginalLocation()
				+ File.separator + String.format("%d.fprm", docId);
			// Load the relevant filter
			inpFilter = mapper.createFilter(item.getFilterID(), inpFilter);
			IParameters params = inpFilter.getParameters();
			// Load them only if the filter has parameters
			if ( params != null ) {
				File file = new File(paramsFile);
				params.load(Util.URItoURL(file.toURI()), false);
			}

			reader.openDocument(fileToMerge, manifest.getSourceLanguage(), manifest.getTargetLanguage());
			
			// Initializes the input
			File f = new File(originalFile);
			inpFilter.open(new RawDocument(f.toURI(), item.getInputEncoding(),
				manifest.getSourceLanguage(), trgLoc));
			
			// Initializes the output
			String outputFile = manifest.getFileToGeneratePath(docId);
			Util.createDirectories(outputFile);
			outFilter = inpFilter.createFilterWriter();
			outFilter.setOptions(trgLoc, item.getOutputEncoding());
			outFilter.setOutput(outputFile);
			
			// Process the document
			while ( inpFilter.hasNext() ) {
				event = inpFilter.next();
				if ( event.getEventType() == EventType.TEXT_UNIT ) {
					processTextUnit(event.getTextUnit());
				}
				outFilter.handleEvent(event);
			}
		}
		catch ( Exception e ) {
			// Log and move on to the next file
			Throwable e2 = e.getCause();
			logger.error("Merging error. {}", ((e2!=null) ? e2.getMessage() : e.getMessage()), e);
		}
		finally {
			if ( reader != null ) {
				reader.closeDocument();
				reader = null;
			}
			if ( inpFilter != null ) {
				inpFilter.close();
				inpFilter = null;
			}
			if ( outFilter != null ) {
				outFilter.close();
				outFilter = null;
			}
		}
	}

	private void processTextUnit (ITextUnit tu) {
		// Skip the non-translatable
		// This means the translate attributes must be the same
		// in the original and the merging files
		if ( !tu.isTranslatable() ) return;

		// Try to get the corresponding translated item
		// They are expected to be in the same order with the same id
		ITextUnit tuFromTrans;
		while ( true ) {
			if ( !reader.readItem() ) {
				// Problem: 
				logger.warn("There is no more items in the package to merge with id=\"{}\".", tu.getId());
				// Keep the source
				return;
			}
			tuFromTrans = reader.getItem();
			if ( !tu.getId().equals(tuFromTrans.getId()) ) {
				// This should be a case where the original TU is translatable 
				// but the XLIFF is flagged as not-translatable because of leveraging or other
				// manipulation
				// If it's a case of bad original file, they will desynchronize fast anyway
				// and we will get a warning.
				continue;
			}
			else break;
		}
		
//		// Get item from the package document
//		// Skip also the read-only ones
//		TextUnit tuFromTrans;
//		while ( true ) {
//			if ( !reader.readItem() ) {
//				// Problem: 
//				logger.warn(
//					String.format("There is no more items in the package to merge with id=\"%s\".", tu.getId()));
//				// Keep the source
//				return;
//			}
//			tuFromTrans = reader.getItem();
//			if ( !tuFromTrans.isTranslatable() ) continue;
//			else break; // Found next translatable (and likely translated) item
//		}
//			
//		if ( !tu.getId().equals(tuFromTrans.getId()) ) {
//			// Problem: different IDs
//			logger.warn(String.format("ID mismatch: Original item id=\"%s\" package item id=\"%s\".",
//				tu.getId(), tuFromTrans.getId()));
//			return; // Use the source
//		}

		if ( !tuFromTrans.hasTarget(trgLoc) ) {
			// No translation in package
			if ( !tu.getSource().isEmpty() ) {
				logger.warn("Item id=\"{}\": No translation provided; using source instead.", tu.getId());
				return; // Use the source
			}
		}

		// Process the "approved" property
		boolean isTransApproved = false;
		Property prop = tuFromTrans.getTargetProperty(trgLoc, Property.APPROVED);
		if ( prop != null ) {
			isTransApproved = prop.getValue().equals("yes");
		}
		if ( manifest.useApprovedOnly() && !isTransApproved ) {
			// Not approved: use the source
			logger.warn("Item id='{}': Target is not approved; using source instead.", tu.getId());
			return; // Use the source
		}

		// Get the translated target
		TextContainer fromTrans = tuFromTrans.getTarget(trgLoc);
		if ( fromTrans == null ) {
			if ( tuFromTrans.getSource().isEmpty() ) return;
			// Else: Missing target in the XLIFF
			logger.warn("Item id='{}': No target in XLIFF; using source instead.", tu.getId());
			return; // Use the source
		}

		// Do we need to preserve the segmentation for merging (e.g. TTX case)
		boolean mergeAsSegments = false;
		if ( tu.getMimeType() != null ) { 
			if ( tu.getMimeType().equals(MimeTypeMapper.TTX_MIME_TYPE)
				|| tu.getMimeType().equals(MimeTypeMapper.XLIFF_MIME_TYPE) ) {
				mergeAsSegments = true;
			}
		}
		
		// Un-segment if needed (and remember the ranges if we will need to re-split after)
		// Merging the segments allow to check/transfer the codes at the text unit level
		List<Range> ranges = null;
		List<Range> srcRanges = null;
		if ( mergeAsSegments ) {
			ranges = new ArrayList<Range>();
			srcRanges = tuFromTrans.getSourceSegments().getRanges();//.saveCurrentSourceSegmentation();
		}
		if ( !fromTrans.contentIsOneSegment() ) {
			fromTrans.getSegments().joinAll(ranges);
		}
		
		// Get the source (as a clone if we need to change the segments)
		TextContainer srcCont;
		if ( !tu.getSource().contentIsOneSegment() ) {
			srcCont  = tu.getSource().clone();
			srcCont.getSegments().joinAll();
		}
		else {
			srcCont = tu.getSource();
		}

		// Adjust the codes to use the appropriate ones
		List<Code> transCodes = transferCodes(fromTrans, srcCont, tu);
		
		// We create a new target if needed
		TextContainer trgCont = tu.createTarget(trgLoc, false, IResource.COPY_ALL);
		if ( !trgCont.contentIsOneSegment() ) {
			trgCont.getSegments().joinAll();
		}

		// Create or overwrite 'approved' flag is requested
		if ( manifest.updateApprovedFlag() ) {
			trgCont.setProperty(new Property(Property.APPROVED, "yes"));
		}

		// Now set the target coded text and the target codes
		try {
			// trgCont is un-segmented at this point and will be re-segmented if needed
			trgCont.getFirstContent().setCodedText(fromTrans.getCodedText(), transCodes, false);
			// Re-set the ranges on the translated entry
			if ( mergeAsSegments ) {
				trgCont.getSegments().create(ranges);
				tu.getSource().getSegments().create(srcRanges);
				//tu.setSourceSegmentationForTarget(trgLoc, srcRanges);
				//tu.synchronizeSourceSegmentation(trgLoc);
			}
		}
		catch ( RuntimeException e ) {
			logger.error("Inline code error with item id=\"{}\".\n{}", tu.getId(), e.getLocalizedMessage());
			// Use the source instead, continue the merge
			tu.setTarget(trgLoc, tu.getSource());
		}
	}

//	private void mergeAsTextUnit (TextContainer fromTrans,
//		TextUnit original,
//		Property prop)
//	{
//		//TODO: handle case of empty or non-existent target		
//		if ( fromTrans.isSegmented() ) {
//			fromTrans.mergeAllSegments();
//		}
//
//		TextContainer srcCont;
//		if ( original.getSource().isSegmented() ) {
//			srcCont  = original.getSource().clone();
//			srcCont.mergeAllSegments();
//		}
//		else {
//			srcCont = original.getSource();
//		}
//		
//		// Adjust the codes to use the appropriate ones
//		List<Code> transCodes = transferCodes(fromTrans, srcCont, original);
//		
//		// We create a new target if needed
//		TextContainer trgCont = original.createTarget(trgLang, false, IResource.COPY_ALL);
//		// Update 'approved' flag is requested
//		if ( manifest.updateApprovedFlag() ) {
//			prop = trgCont.getProperty(Property.APPROVED);
//			if ( prop == null ) {
//				prop = trgCont.setProperty(new Property(Property.APPROVED, "no"));
//			}
//			//TODO: Option to set the flag based on isTransApproved
//			prop.setValue("yes");
//		}
//
//		// Now set the target coded text and the target codes
//		try {
//			trgCont.setCodedText(fromTrans.getCodedText(), transCodes, false);
//		}
//		catch ( RuntimeException e ) {
//			logger.error(
//				String.format("Inline code error with item id=\"%s\".\n" + e.getLocalizedMessage(), original.getId()));
//			// Use the source instead, continue the merge
//			original.setTarget(trgLang, original.getSource());
//		}
//	}
//	
//	private void mergeAsSegments (TextContainer fromTrans,
//		TextUnit original,
//		Property prop)
//	{
//		//TODO: handle case of empty or non-existent target
//		ArrayList<Range> ranges = new ArrayList<Range>();
//		if ( fromTrans.isSegmented() ) {
//			fromTrans.mergeAllSegments(ranges);
//		}
//
//		TextContainer srcCont;
//		if ( original.getSource().isSegmented() ) {
//			srcCont  = original.getSource().clone();
//			srcCont.mergeAllSegments();
//		}
//		else {
//			srcCont = original.getSource();
//		}
//
//		// Adjust the codes to use the appropriate ones
//		List<Code> transCodes = transferCodes(fromTrans, srcCont, original);
//		
//		// We create a new target if needed
//		TextContainer trgCont = original.createTarget(trgLang, false, IResource.COPY_ALL);
//		// Update 'approved' flag is requested
//		if ( manifest.updateApprovedFlag() ) {
//			prop = trgCont.getProperty(Property.APPROVED);
//			if ( prop == null ) {
//				prop = trgCont.setProperty(new Property(Property.APPROVED, "no"));
//			}
//			//TODO: Option to set the flag based on isTransApproved
//			prop.setValue("yes");
//		}
//
//		// Now set the target coded text and the target codes
//		try {
//			trgCont.setCodedText(fromTrans.getCodedText(), transCodes, false);
//			// Re-set the ranges on the translated entry
//			trgCont.createSegments(ranges);
//		}
//		catch ( RuntimeException e ) {
//			logger.error(
//				String.format("Inline code error with item id=\"%s\".\n" + e.getLocalizedMessage(), original.getId()));
//			// Use the source instead, continue the merge
//			original.setTarget(trgLang, original.getSource());
//		}
//	}
	
	/*
	 * Checks the codes in the translated entry, uses the original data if there is
	 * none in the code coming from XLIFF, and generates a non-stopping error if
	 * a non-deletable code is missing.
	 */
	private List<Code> transferCodes (TextContainer fromTrans,
		TextContainer srcCont, // Can be a clone of the original content
		ITextUnit tu)
	{
		List<Code> transCodes = fromTrans.getFirstContent().getCodes();
		List<Code> oriCodes = srcCont.getFirstContent().getCodes();
		
		// Check if we have at least one code
		if ( transCodes.size() == 0 ) {
			if ( oriCodes.size() == 0 ) return transCodes;
			// Else: fall thru and get missing codes errors
		}
		
		int[] oriIndices = new int[oriCodes.size()];
		for ( int i=0; i<oriIndices.length; i++ ) oriIndices[i] = i;
		int done = 0;
		
		Code transCode, oriCode;
		for ( int i=0; i<transCodes.size(); i++ ) {
			transCode = transCodes.get(i);
			transCode.setOuterData(null); // Remove XLIFF outer codes

			// Get the data from the original code (match on id)
			oriCode = null;
			for ( int j=0; j<oriIndices.length; j++ ) {
				if ( oriIndices[j] == -1) continue; // Used already
				if ( oriCodes.get(oriIndices[j]).getId() == transCode.getId() ) {
					oriCode = oriCodes.get(oriIndices[j]);
					oriIndices[j] = -1;
					done++;
					break;
				}
			}
			if ( oriCode == null ) { // Not found in original (extra in target)
				if ( !transCode.hasData() ) {
					// Leave it like that
					logger.warn("The extra target code id='{}' does not have corresponding data (item id='{}', name='{}')",
						transCode.getId(), tu.getId(), (tu.getName()==null ? "" : tu.getName()));
				}
			}
			else { // Get the data from the original
				if ( oriCode.hasOuterData() ) {
					transCode.setOuterData(oriCode.getOuterData());
				}
				else if ( !transCode.hasData() ) {
					transCode.setData(oriCode.getData());
				}
				transCode.setReferenceFlag(oriCode.hasReference());
			}
		}
		
		// If needed, check for missing codes in translation
		if ( oriCodes.size() > done ) {
			// Any index > -1 in source means it was was deleted in target
			for ( int i=0; i<oriIndices.length; i++ ) {
				if ( oriIndices[i] != -1 ) {
					Code code = oriCodes.get(oriIndices[i]);
					if ( !code.isDeleteable() ) {
						logger.warn("The code id='{}' ({}) is missing in target (item id='{}', name='{}')",
							code.getId(), code.getData(), tu.getId(), (tu.getName()==null ? "" : tu.getName()));
						logger.info("Source='{}'", tu.getSource().toString());
					}
				}
			}
		}
		
		return transCodes;
	}
	
}
