/*===========================================================================
  Copyright (C) 2009-2013 by the Okapi Framework contributors
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

package net.sf.okapi.lib.tkit.step;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.IResource;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.MimeTypeMapper;
import net.sf.okapi.common.Range;
import net.sf.okapi.common.exceptions.OkapiFilterCreationException;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filters.IFilterConfigurationMapper;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.io.InputStreamFromOutputStream;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
import net.sf.okapi.common.pipeline.annotations.StepParameterType;
import net.sf.okapi.common.pipelinedriver.PipelineDriver;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.filters.xliff.XLIFFFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <b>Legacy: Only used in integration tests. New code will migrate to lib-tkit classes</b>
 * Tkit merger which re-filters the original source file to provide the
 * skeleton for merging. This is the legacy, standalone version which does not
 * use lib-tkit.
 * 
 * @author jimh
 * @author yvess
 * 
 */
public class LegacyXliffMergerStep extends BasePipelineStep {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private IFilter filter;
	private IFilterWriter writer;
	private IFilterConfigurationMapper fcMapper;
	private XLIFFFilter xlfReader;
	private String outputEncoding;
	private LocaleId trgLoc;
	private RawDocument originalDocument;

	public LegacyXliffMergerStep() {
	}
	
	@Override
	public String getName() {
		return "Legacy Original Document Xliff Merger";
	}

	@Override
	public String getDescription() {
		return "Legacy Tkit merger which re-filters the original source file to provide the skeleton for merging, "
				+ "but does not use the newer lib-tkit.";
	}

	@StepParameterMapping(parameterType = StepParameterType.OUTPUT_ENCODING)
	public void setOutputEncoding(String outputEncoding) {
		this.outputEncoding = outputEncoding;
	}
	
	/**
	 * Target locales. Currently only the first locale in the list is used.
	 * 
	 * @param targetLocales
	 */
	@StepParameterMapping(parameterType = StepParameterType.TARGET_LOCALES)
	public void setTargetLocales(final List<LocaleId> targetLocales) {
		this.trgLoc = targetLocales.get(0);
	}
	
	/**
	 * This is the original source document
	 * 
	 * @param secondInput Original source document
	 */
	@StepParameterMapping(parameterType = StepParameterType.SECOND_INPUT_RAWDOC)
	public void setSecondInput(final RawDocument secondInput) {
		this.originalDocument = secondInput;
	}

	/**
	 * The {@link IFilterConfigurationMapper} set in the {@link PipelineDriver}
	 * 
	 * @param fcMapper
	 */
	@StepParameterMapping(parameterType = StepParameterType.FILTER_CONFIGURATION_MAPPER)
	public void setFilterConfigurationMapper(final IFilterConfigurationMapper fcMapper) {
		this.fcMapper = fcMapper;
	}
	
	/*
	 * For now, take all the info from argument rather than directly the XLIFF
	 * file.
	 */
	@SuppressWarnings("resource")
	@Override
	protected Event handleRawDocument(final Event event) {
		final InputStreamFromOutputStream<Void> is = new InputStreamFromOutputStream<Void>() {
			@Override
			protected Void produce(OutputStream sink) throws Exception {
				try {
					xlfReader = new XLIFFFilter();
					xlfReader.open(event.getRawDocument());

					filter = fcMapper.createFilter(originalDocument.getFilterConfigId(), filter);
					if (filter == null) {
						throw new OkapiFilterCreationException(String.format(
								"Cannot create the filter or load the configuration for '%s'",
								originalDocument.getFilterConfigId()));
					}
					filter.open(originalDocument);

					writer = filter.createFilterWriter();
					writer.setOptions(trgLoc, outputEncoding);
					writer.setOutput(sink);					
					while (filter.hasNext()) {
						Event e = filter.next();
						if (e.getEventType() == EventType.TEXT_UNIT) {
							processTextUnit(e.getTextUnit());
						}
						writer.handleEvent(e);
					}
				} finally {
					if (xlfReader != null) xlfReader.close();
					if (filter != null) filter.close();
					if (writer != null) writer.close();
				}
					
				return null;
			}
		};
		
		// Writer step closes the RawDocument
		return new Event(EventType.RAW_DOCUMENT, new RawDocument(is, outputEncoding, trgLoc));
	}
	
	@Override
	public void cancel() {
	}

	@Override
	public void destroy() {
	}

	/**
	 * Gets the next text unit in the XLIFF document.
	 * 
	 * @return the next text unit or null.
	 */
	private ITextUnit getTextUnitFromXLIFF() {
		Event event;
		while (xlfReader.hasNext()) {
			event = xlfReader.next();
			if (event.getEventType() == EventType.TEXT_UNIT) {
				return event.getTextUnit();
			}
		}
		return null;
	}

	private void processTextUnit(ITextUnit tu) {
		// Skip the non-translatable
		// This means the translate attributes must be the same
		// in the original and the merging files
		if (!tu.isTranslatable()) return;

		// Try to get the corresponding translated item
		// They are expected to be in the same order with the same id
		ITextUnit tuFromTrans;
		while (true) {
			tuFromTrans = getTextUnitFromXLIFF();
			if (tuFromTrans == null) {
				// Problem:
				logger.warn("There is no more items in the package to merge with id=\"{}\".", tu.getId());
				// Keep the source
				return;
			}
			if (!tu.getId().equals(tuFromTrans.getId())) {
				// This should be a case where the original TU is translatable
				// but the XLIFF is flagged as not-translatable because of
				// leveraging or other
				// manipulation
				// If it's a case of bad original file, they will desynchronize
				// fast anyway
				// and we will get a warning.
				continue;
			}
			else break;
		}

		if (!tuFromTrans.hasTarget(trgLoc)) {
			// No translation in package
			if (!tu.getSource().isEmpty()) {
				logger.warn("Item id=\"{}\": No translation provided; using source instead.", tu.getId());
				return; // Use the source
			}
		}

		// boolean isTransApproved = false;
		// Property prop = tuFromTrans.getTargetProperty(trgLang,
		// Property.APPROVED);
		// if ( prop != null ) {
		// isTransApproved = prop.getValue().equals("yes");
		// }
		// if ( manifest.useApprovedOnly() && !isTransApproved ) {
		// // Not approved: use the source
		// logger.warn(
		// String.format("Item id='%s': Target is not approved; using source instead.",
		// tu.getId()));
		// return; // Use the source
		// }

		// Get the translated target
		TextContainer fromTrans = tuFromTrans.getTarget(trgLoc);
		if (fromTrans == null) {
			if (tuFromTrans.getSource().isEmpty()) return;
			// Else: Missing target in the XLIFF
			logger.warn("Item id='{}': No target in XLIFF; using source instead.", tu.getId());
			return; // Use the source
		}

		// Do we need to preserve the segmentation for merging (e.g. TTX case)
		boolean mergeAsSegments = false;
		if (tu.getMimeType() != null) {
			if (tu.getMimeType().equals(MimeTypeMapper.TTX_MIME_TYPE)
					|| tu.getMimeType().equals(MimeTypeMapper.XLIFF_MIME_TYPE)) {
				mergeAsSegments = true;
			}
		}

		// Un-segment if needed (and remember the ranges if we will need to
		// re-split after)
		// Merging the segments allow to check/transfer the codes at the text
		// unit level
		List<Range> trgRanges = null;
		List<Range> srcRanges = null;
		if (mergeAsSegments) {
			trgRanges = new ArrayList<Range>();
			srcRanges = tuFromTrans.getSourceSegments().getRanges();// .saveCurrentSourceSegmentation();
		}
		if (!fromTrans.contentIsOneSegment()) {
			fromTrans.getSegments().joinAll(trgRanges);
		}

		// Get the source (as a clone if we need to change the segments)
		TextContainer srcCont;
		if (!tu.getSource().contentIsOneSegment()) {
			srcCont = tu.getSource().clone();
			srcCont.getSegments().joinAll();
		}
		else {
			srcCont = tu.getSource();
		}

		// Adjust the codes to use the appropriate ones
		List<Code> transCodes = transferCodes(fromTrans, srcCont, tu);

		// We create a new target if needed
		TextContainer trgCont = tu.createTarget(trgLoc, false, IResource.COPY_ALL);
		if (!trgCont.contentIsOneSegment()) {
			trgCont.getSegments().joinAll();
		}

		// // Update 'approved' flag is requested
		// if ( manifest.updateApprovedFlag() ) {
		// prop = trgCont.getProperty(Property.APPROVED);
		// if ( prop == null ) {
		// prop = trgCont.setProperty(new Property(Property.APPROVED, "no"));
		// }
		// //TODO: Option to set the flag based on isTransApproved
		// prop.setValue("yes");
		// }

		// Now set the target coded text and the target codes
		try {
			// trgCont is un-segmented at this point and will be re-segmented if
			// needed
			trgCont.getFirstContent().setCodedText(fromTrans.getCodedText(), transCodes, false);
			// Re-set the ranges on the translated entry
			if (mergeAsSegments) {
				trgCont.getSegments().create(trgRanges);
				tu.getSource().getSegments().create(srcRanges);
				// tu.setSourceSegmentationForTarget(trgLoc, srcRanges);
				// tu.synchronizeSourceSegmentation(trgLoc);
			}
		} catch (RuntimeException e) {
			logger.error("Inline code error with item id=\"{}\".\n{}", e.getLocalizedMessage(), tu.getId());
			// Use the source instead, continue the merge
			tu.setTarget(trgLoc, tu.getSource());
		}

	}

	/*
	 * Checks the codes in the translated entry, uses the original data if there
	 * is none in the code coming from XLIFF, and generates a non-stopping error
	 * if a non-deletable code is missing. Assumes the containers are NOT
	 * segmented.
	 */
	private List<Code> transferCodes(TextContainer fromTrans,
			TextContainer srcCont, // Can be a clone of the original content
			ITextUnit tu)
	{
		// We assume the container are NOT segmented
		List<Code> transCodes = fromTrans.getFirstContent().getCodes();
		List<Code> oriCodes = srcCont.getFirstContent().getCodes();

		// Check if we have at least one code
		if (transCodes.size() == 0) {
			if (oriCodes.size() == 0) return transCodes;
			// Else: fall thru and get missing codes errors
		}

		int[] oriIndices = new int[oriCodes.size()];
		for (int i = 0; i < oriIndices.length; i++)
			oriIndices[i] = i;
		int done = 0;

		Code transCode, oriCode;
		for (int i = 0; i < transCodes.size(); i++) {
			transCode = transCodes.get(i);
			transCode.setOuterData(null); // Remove XLIFF outer codes

			// Get the data from the original code (match on id)
			oriCode = null;
			for (int j = 0; j < oriIndices.length; j++) {
				if (oriIndices[j] == -1) continue; // Used already
				if (oriCodes.get(oriIndices[j]).getId() == transCode.getId()) {
					oriCode = oriCodes.get(oriIndices[j]);
					oriIndices[j] = -1;
					done++;
					break;
				}
			}
			if (oriCode == null) { // Not found in original (extra in target)
				if (!transCode.hasData()) {
					// Leave it like that
					logger.warn(
							"The extra target code id='{}' does not have corresponding data (item id='{}', name='{}')",
							transCode.getId(), tu.getId(), (tu.getName() == null ? "" : tu.getName()));
				}
			}
			else { // Get the data from the original
				if (oriCode.hasOuterData()) {
					transCode.setOuterData(oriCode.getOuterData());
				}
				else if (!transCode.hasData()) {
					transCode.setData(oriCode.getData());
				}
				transCode.setReferenceFlag(oriCode.hasReference());
			}
		}

		// If needed, check for missing codes in translation
		if (oriCodes.size() > done) {
			// Any index > -1 in source means it was was deleted in target
			for (int i = 0; i < oriIndices.length; i++) {
				if (oriIndices[i] != -1) {
					Code code = oriCodes.get(oriIndices[i]);
					if (!code.isDeleteable()) {
						logger.warn("The code id='{}' ({}) is missing in target (item id='{}', name='{}')",
								code.getId(), code.getData(), tu.getId(), (tu.getName() == null ? "" : tu.getName()));
						logger.info("Source='{}'", tu.getSource().toString());
					}
				}
			}
		}

		return transCodes;
	}
}
