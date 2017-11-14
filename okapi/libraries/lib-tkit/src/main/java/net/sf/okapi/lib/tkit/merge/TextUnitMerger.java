/*===========================================================================
  Copyright (C) 2013 by the Okapi Framework contributors
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

import java.util.List;

import net.sf.okapi.common.IResource;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.MimeTypeMapper;
import net.sf.okapi.common.Range;
import net.sf.okapi.common.StringUtil;
import net.sf.okapi.common.annotation.IAnnotation;
import net.sf.okapi.common.exceptions.OkapiMergeException;
import net.sf.okapi.common.resource.CodeAnomalies;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragmentUtil;
import net.sf.okapi.common.resource.TextPart;
import net.sf.okapi.common.resource.TextUnitUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextUnitMerger implements ITextUnitMerger {
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());	
	private LocaleId trgLoc;
	private Parameters params;
	private List<Range> srcRanges;
	private List<Range> trgRanges;
	
	public ITextUnit mergeTargets(ITextUnit tuFromSkel, ITextUnit tuFromTran) {
		if ( !tuFromSkel.getId().equals(tuFromTran.getId()) ) {
			String s = String.format("Text Unit id mismatch during merger: Original id=\"%s\" target id=\"%s\"", tuFromSkel.getId(), tuFromTran.getId());
			LOGGER.error(s);
			if (params.isThrowSegmentIdException()) {
				throw new OkapiMergeException(s);
			}
		}

		// since segmentation will likely collapse whitespace in the xliff file
		// we need to do the same to avoid false positives
		// Use StringUtil.removeWhiteSpace as it covers all Unicode
		// whitespace defined by "\s" and trims.
		// This seems to cover all the differences between source in the
		// original and source in the xliff
		String originalSource = StringUtil.removeWhiteSpace(tuFromSkel.getSource().createJoinedContent().getText());
		String sourceFromTkit = StringUtil.removeWhiteSpace(tuFromTran.getSource().createJoinedContent().getText());
		if (!originalSource.equals(sourceFromTkit)) {
			String s = String.format(
					"Text Unit source mismatch during merge: Original id=\"%s\" target id=\"%s\"\n"
							+ "Original Source=\"%s\"\n"
							+ "Tkit Source=\"%s\"", tuFromSkel.getId(),
					tuFromTran.getId(), originalSource, sourceFromTkit);
			LOGGER.error(s);
			if (params.isThrowSegmentSourceException()) {
				throw new OkapiMergeException(s);
			}
		}
		
		// Get the container from the skeleton where we will
        // place the translated segment(s)
        // the skeleton may be multilingual
        // FIXME: If it is multilingual do we use the target?
        TextContainer sourceFromSkel = tuFromSkel.getSource();
        TextContainer sourceFromTarget = tuFromTran.getSource();
        
        // must clone as some operations remove annotations
        TextContainer targetFromSkel =  
                tuFromSkel.hasTarget(trgLoc) ? tuFromSkel.getTarget(trgLoc).clone() : null;
                
       // sourceFromSkel is not always the source of codes, if there is an original target, use its codes instead
       // mergeFromSkel is the "template" or primary source of merge information such as codes, segmentation etc..                
       TextContainer mergeFromSkel = targetFromSkel == null || targetFromSkel.isEmpty() ? sourceFromSkel: 
                    // clone to keep original segment annotations destroyed by joinAll()
                    targetFromSkel.clone();  
                
		// Check if we have a translation
		// if not we assume the source is the translation
		// for example, a mono-lingual translation format
		// We clone the container because the writer should not modify Events
		TextContainer targetFromTran = null;
		if ( tuFromTran.hasTarget(trgLoc) ) {
			targetFromTran = tuFromTran.getTarget(trgLoc).clone();
		} else {
			// FIXME: The target might have been left empty by the translator, should we copy the source here?
			// Or maybe this should be an optional error?
			targetFromTran = tuFromTran.getSource().clone();
		}

		if ( targetFromTran == null ) {
			if ( tuFromSkel.getSource().hasText() ) {
				// Warn only if there source is not empty
				LOGGER.error("No translation found for TU id='{}'. Using source instead.", tuFromTran.getId());
			}
			return tuFromSkel;
		}

		// Process the "approved" property
		boolean isTransApproved = false;
		Property traProp = targetFromTran.getProperty(Property.APPROVED);
		if ( traProp != null ) {
			isTransApproved = traProp.getValue().equals("yes");
		}
		if ( params != null && params.isApprovedOnly() && !isTransApproved ) {
			// Not approved: use the source
			LOGGER.warn("Item id='{}': Target is not approved; using source instead.", tuFromSkel.getId());
			return tuFromSkel;
		}
		
		// We need to preserve the segmentation for merging
		boolean mergeAsSegments = MimeTypeMapper.isSegmentationSupported(tuFromSkel.getMimeType());

		// Remember the ranges to set them back after merging
		srcRanges = null;
		trgRanges = null;
		boolean simplified = false;
		
		// Merge the segments together for the code transfer
		// This allows to move codes anywhere in the text unit, 
		// not just each part. We do remember the ranges because 
		// some formats will required to be merged by segments
		// FIXME: joining segments destroys segment annotations
		if ( !mergeFromSkel.contentIsOneSegment() ) {
			srcRanges = mergeFromSkel.getSegments().getRanges();
			mergeFromSkel.joinAll();
		}
		
		if ( !targetFromTran.contentIsOneSegment() ) {
			for (TextPart tp : targetFromTran.getSegments()) {
				if (TextUnitUtil.hasMergedCode(tp.text)) {
					tp.text = TextUnitUtil.expandCodes(tp.text);
					simplified = true;
				}	
			}
			trgRanges = targetFromTran.getSegments().getRanges();
			targetFromTran.joinAll();
		} else {			
			if (TextUnitUtil.hasMergedCode(targetFromTran.getFirstContent())) {
				targetFromTran.setContent(TextUnitUtil.expandCodes(targetFromTran.getFirstContent()));
				simplified = true;
			}		
		}
				
		// copy code metadata from source. Only copy data if the target is empty
		TextFragmentUtil.copyCodes(mergeFromSkel.getFirstContent(), targetFromTran.getFirstContent(), simplified);
		
		// check for code errors
		CodeAnomalies codeAnomalies = TextFragmentUtil.catalogCodeAnomalies(
		        mergeFromSkel.getFirstContent(), targetFromTran.getFirstContent());
		if (codeAnomalies != null) {		
			StringBuilder e = new StringBuilder();			
			e.append(String.format("Text Unit id: %s", tuFromTran.getId()));			
			if (codeAnomalies.hasAddedCodes()) {
				e.append(String.format("\nAdded Codes in target='%s'", codeAnomalies.addedCodesAsString()));
			}
			if (codeAnomalies.hasMissingCodes()) {
				e.append(String.format("\nMissing Codes in target='%s'", codeAnomalies.missingCodesAsString()));
			}
			if (e.length() >= 0) {
				e.append("\nTarget Text Unit:\n");
				e.append(targetFromTran.getUnSegmentedContentCopy().toText());
				LOGGER.error(e.toString());				
				if (params.isThrowCodeException()) {
					throw new OkapiMergeException(e.toString());
				}
			}
		}
		
		// Re-apply segmentation ranges
		if ( mergeAsSegments ) {
			if ( srcRanges != null ) {
			    mergeFromSkel.getSegments().create(srcRanges, true);
			}
			if ( trgRanges != null ) {
				targetFromTran.getSegments().create(trgRanges, true);
			}
		}
	
		// Check if the target has more segments
		// FIXME: shouldn't we just test for fromSkelCont.getSegments().count() != fromTranCont.getSegments().count()?
		// FIXME: isn't this only an issue for segmented formats? Otherwise we merge
		// on the "paragraph".
		if (mergeFromSkel.getSegments().count() < targetFromTran.getSegments().count()) {
			LOGGER.error("Item id='{}': There is at least one extra segment in the translation file.",
				tuFromTran.getId());
		}

		if (!tuFromSkel.hasTarget(trgLoc)) {			
			// There's no target (monolingual document original) then we create one
			tuFromSkel.createTarget(trgLoc, false, IResource.COPY_ALL);
		}
		// Overwrite the skeleton target with the translated version
		tuFromSkel.setTarget(trgLoc, targetFromTran);

		if ( targetFromSkel == null ) return tuFromSkel;
			
		// Transfer annotations on original target segments
		if ( mergeAsSegments ) {
			for (Segment seg : targetFromSkel.getSegments()) {
				Segment tseg = targetFromTran.getSegments().get(seg.id);
				if ( tseg == null ) {
					continue;
				}
				if ( seg.getAnnotations() == null ) {
					continue;
				}			
				for (IAnnotation ann : seg.getAnnotations()) {
					tseg.setAnnotation(ann);
				}
			}
		}

		// transfer annotations on containers
		for (IAnnotation ann : targetFromSkel.getAnnotations()) {
			targetFromTran.setAnnotation(ann);
		}
		// Transfer original (skeleton) target properties
		for (String propName : targetFromSkel.getPropertyNames()) {
			// Don't overwrite the state property of tran if it exists
			if (propName.equals(Property.STATE) && targetFromTran.hasProperty(Property.STATE)) {
				continue;
			}
			targetFromTran.setProperty(targetFromSkel.getProperty(propName));
		}
		
		// special case for XLIFF2. We may deepen segmentation depending on the
		// settings so need to grab the segmented target TU ("non-skeleton") source 
		// so it matches the target
		if (tuFromSkel.getMimeType() != null && tuFromSkel.getMimeType().equals(MimeTypeMapper.XLIFF2_MIME_TYPE)) {
			tuFromSkel.setSource(sourceFromTarget);
		}
		
		return tuFromSkel;
	}

	public void setTargetLocale(LocaleId trgLoc) {
		this.trgLoc = trgLoc;
	}
	
	public LocaleId getTargetLocale() {
		return trgLoc;
	}

	public Parameters getParameters() {
		return params;
	}

	public void setParameters(Parameters params) {
		this.params = params;
	}
}
