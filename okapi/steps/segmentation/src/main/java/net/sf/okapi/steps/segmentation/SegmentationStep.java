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

package net.sf.okapi.steps.segmentation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.IResource;
import net.sf.okapi.common.ISegmenter;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.RenumberingUtil;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.annotation.DeepenSegmentationAnnotaton;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
import net.sf.okapi.common.pipeline.annotations.StepParameterType;
import net.sf.okapi.common.resource.Custom;
import net.sf.okapi.common.resource.ISegments;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextPart;
import net.sf.okapi.lib.segmentation.SRXDocument;
import net.sf.okapi.steps.segmentation.Parameters.SegmStrategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@UsingParameters(Parameters.class)
public class SegmentationStep extends BasePipelineStep {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private Parameters params;
	private ISegmenter srcSeg;
	private Map<LocaleId, ISegmenter> trgSegs;
	private LocaleId sourceLocale;
	private List<LocaleId> targetLocales;
	private boolean initDone;
	private String rootDir;
	private String inputRootDir;

	public SegmentationStep () {
		params = new Parameters();
		srcSeg = null;
		trgSegs = new HashMap<>();
	}
	
	@StepParameterMapping(parameterType = StepParameterType.SOURCE_LOCALE)
	public void setSourceLocale (LocaleId sourceLocale) {
		this.sourceLocale = sourceLocale;
	}
	
	@StepParameterMapping(parameterType = StepParameterType.TARGET_LOCALES)
	public void setTargetLocales (List<LocaleId> targetLocales) {
		this.targetLocales = targetLocales;
	}
	
	@StepParameterMapping(parameterType = StepParameterType.ROOT_DIRECTORY)
	public void setRootDirectory (String rootDir) {
		this.rootDir = rootDir;
	}
	
	@StepParameterMapping(parameterType = StepParameterType.INPUT_ROOT_DIRECTORY)
	public void setInputRootDirectory (String inputRootDir) {
		this.inputRootDir = inputRootDir;
	}
	
	public LocaleId getSourceLocale() {
		return sourceLocale;
	}
	
	public List<LocaleId> getTargetLocales() {
		return targetLocales;
	}
	
	public String getRootDirectory() {
		return rootDir;
	}
	
	public String getInputRootDirectory() {
		return inputRootDir;
	}

	public String getName () {
		return "Segmentation";
	}

	public String getDescription () {
		return "Apply SRX segmentation to the text units content of a document. "
			+ "Expects: filter events. Sends back: filter events.";
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
		initDone = false;
		return event;
	}
	
	@Override
	protected Event handleStartBatchItem (Event event) {
		if ( initDone ) return event; // Initialize once per batch
		SRXDocument srxDoc = new SRXDocument();
		String src = null;
		if ( params.getSegmentSource() ) {
			// stream always has priority
			if (params.getSourceSrxStream() == null) {
				src = Util.fillRootDirectoryVariable(params.getSourceSrxPath(), rootDir);
				src = Util.fillInputRootDirectoryVariable(src, inputRootDir);			
				srxDoc.loadRules(src);
			} else {
				srxDoc.loadRules(params.getSourceSrxStream());
			}
			
			if ( srxDoc.hasWarning() ) {
				logger.warn(srxDoc.getWarning());
			}
			// Change trimming options if requested
			if ( params.getTrimSrcLeadingWS() != Parameters.TRIM_DEFAULT ) {
				srxDoc.setTrimLeadingWhitespaces(params.getTrimSrcLeadingWS()==Parameters.TRIM_YES);
			}
			if ( params.getTrimSrcTrailingWS() != Parameters.TRIM_DEFAULT ) {
				srxDoc.setTrimTrailingWhitespaces(params.getTrimSrcTrailingWS()==Parameters.TRIM_YES);
			}

			// treat isolated codes as whitespace?
			srxDoc.setTreatIsolatedCodesAsWhitespace(params.isTreatIsolatedCodesAsWhitespace());

			// Instantiate the segmenter
			srcSeg = srxDoc.compileLanguageRules(sourceLocale, null);
		}
		if ( params.getSegmentTarget() ) {
			// stream always has priority
			if (params.getTargetSrxStream() == null) {
				String trg = Util.fillRootDirectoryVariable(params.getTargetSrxPath(), rootDir);
				trg = Util.fillInputRootDirectoryVariable(trg, inputRootDir);
				// Load target SRX only if different from sources
				if ( Util.isEmpty(src) || !src.equals(trg) ) {
					srxDoc.loadRules(trg);					
				}
			} else {
				srxDoc.loadRules(params.getTargetSrxStream());
			}
			
			if ( srxDoc.hasWarning() ) {
				logger.warn(srxDoc.getWarning());
			}
			
			// Change trimming options if requested
			if ( params.getTrimTrgLeadingWS() != Parameters.TRIM_DEFAULT ) {
				srxDoc.setTrimLeadingWhitespaces(params.getTrimTrgLeadingWS()==Parameters.TRIM_YES);
			}
			if ( params.getTrimTrgTrailingWS() != Parameters.TRIM_DEFAULT ) {
				srxDoc.setTrimTrailingWhitespaces(params.getTrimTrgTrailingWS()==Parameters.TRIM_YES);
			}

			// treat isolated codes as whitespace?
			srxDoc.setTreatIsolatedCodesAsWhitespace(params.isTreatIsolatedCodesAsWhitespace());

			// Instantiate the segmenter
			for(LocaleId targetLocale : targetLocales) {
				ISegmenter trgSeg = srxDoc.compileLanguageRules(targetLocale, null);
				trgSegs.put(targetLocale, trgSeg);
			}
		}

		return event;
	}
	
	@Override
	protected Event handleStartDocument (Event event) {
		if ( params.getSegmentSource() || params.getSegmentTarget() ) {
			// Possibly force the output segmentation, but only if we do any segmentation
			if ( params.getForcesegmentedOutput() ) {
				// Force to show the segments when possible
				IParameters prm = event.getStartDocument().getFilterParameters();
				if ( prm != null ) {
					prm.setInteger("outputSegmentationType", 3);
				}
			}
		}
		return event;
	}
	
	@Override
	protected Event handleCustom (Event event) {
		Custom r = (Custom)event.getResource();
		if (r.getAnnotation(DeepenSegmentationAnnotaton.class) != null) {
			params.setOverwriteSegmentation(false);
			params.setDeepenSegmentation(true);
		}
		return event;
	}
	
	@Override
	protected Event handleTextUnit (Event event) {
		ITextUnit tu = event.getTextUnit();
		// currently only XLIFF2 stores this property		
		if (tu.getProperty("canResegment") != null && tu.getProperty("canResegment").getValue().equals("no")) {
			return event;
		}
		
		// Skip non-translatable
		if ( !tu.isTranslatable() ) return event;
		// Nothing to do
		if ( !params.getSegmentSource() && !params.getSegmentTarget() ) return event;  

		// Segment source if requested
		if ( params.getSegmentSource() ) {
			if ( params.getSegmentationStrategy() == SegmStrategy.OVERWRITE_EXISTING || 
					!tu.getSource().hasBeenSegmented() ) {
				tu.createSourceSegmentation(srcSeg);
			}
			else if (params.getSegmentationStrategy() == SegmStrategy.DEEPEN_EXISTING) {
				// Has been segmented or not (if unsegmented, it's still 1 segment)
				deepenSegmentation(tu.getSource(), srcSeg);
			}
			
			if ( params.getRenumberCodes() ) {
				RenumberingUtil.renumberCodesForSegmentation(tu.getSource());
			}
		}
		
		if (targetLocales != null) {
			for(LocaleId targetLocale : targetLocales) {
				TextContainer trgCont = tu.getTarget(targetLocale);
				ISegmenter trgSeg = trgSegs.get(targetLocale);
		
				// Segment target if requested
				if ( params.getSegmentTarget() && ( trgCont != null )) {
					if ( params.getSegmentationStrategy() == SegmStrategy.OVERWRITE_EXISTING ||
							!trgCont.hasBeenSegmented() ) {
						trgSeg.computeSegments(trgCont);
						trgCont.getSegments().create(trgSeg.getRanges());
					}
					else if (params.getSegmentationStrategy() == SegmStrategy.DEEPEN_EXISTING) {
						// Has been segmented or not (if unsegmented, it's still 1 segment)
						deepenSegmentation(trgCont, trgSeg);
					}
					if ( params.getRenumberCodes() ) {
						RenumberingUtil.renumberCodesForSegmentation(trgCont);
					}
				}
				
				// Make sure we have target content if needed, segmentation is incurred by the variant source 
				if ( params.getCopySource() ) {
					trgCont = tu.createTarget(targetLocale, false, IResource.COPY_ALL);
				}
		
				// If requested, verify that we have one-to-one match
				// This is needed only if we do have a target
				if ( params.getCheckSegments() && ( trgCont != null)) {
					if ( trgCont.getSegments().count() != tu.getSource().getSegments().count() ) {
						// Not the same number of segments
						logger.warn("Text unit id='{}': Source ({}) and target ({}) do not have the same number of segments.",
							tu.getId(), sourceLocale, targetLocale);
					}
					// Otherwise make sure we have matches
					else {
						ISegments trgSegs = trgCont.getSegments();
						for ( Segment seg : tu.getSource().getSegments() ) {
							if ( trgSegs.get(seg.id) == null ) {
								// No target segment matching source segment seg.id
								logger.warn("Text unit id='{}': No match found for source segment id='{}' in target language '{}'",
									tu.getId(), seg.id, targetLocale);
							}
						}
					}
				}
			}
		}
		
		return event;
	}

	/**
	 * Iterates a given TextContainer's segments to apply segmentation rules to them.
	 * @param tc the given TextContainer
	 * @param segmenter the segmenter to perform additional segmentation for existing segments
	 */
	private void deepenSegmentation(TextContainer tc, ISegmenter segmenter) {
		if (tc == null || segmenter == null) {
			logger.error("Parameter cannot be null");
			return;
		}
		
		// Reverse order so we can insert parts in the loop
		for (int i = tc.count() - 1; i >= 0; i--) {
			TextPart part = tc.get(i);
			if (!part.isSegment()) continue;
			
			// Part is always a segment here
			TextContainer segTc = new TextContainer(part);
			segmenter.computeSegments(segTc);
			
			// Apply segmentation, replace segment with the new list of parts
			segTc.getSegments().create(segmenter.getRanges());
			replacePart(tc, i, segTc);
		}
	}
	
	private void replacePart(TextContainer oldPartContainer, int index, 
			TextContainer newPartsContainer) {
		for (int i = newPartsContainer.count() - 1; i >= 0; i--) {
			oldPartContainer.insert(index, newPartsContainer.get(i));
		}
		// Remove the old (unsegmented) segment.
		// Do it after inserting the new segments, because if the segment is the only one in 
		// its container, it won't be removed (TC always contains at least one segment) 
		oldPartContainer.remove(index+newPartsContainer.count());
	}

}
