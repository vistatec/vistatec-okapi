/*===========================================================================
  Copyright (C) 2010-2011 by the Okapi Framework contributors
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

package net.sf.okapi.steps.moses;

import java.io.File;
import java.net.URI;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.IResource;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.annotation.AltTranslationsAnnotation;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
import net.sf.okapi.common.pipeline.annotations.StepParameterType;
import net.sf.okapi.common.query.MatchType;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextUnitUtil;
import net.sf.okapi.filters.mosestext.MosesTextFilter;
import net.sf.okapi.filters.xliff.Parameters;

@UsingParameters(MergingParameters.class)
public class MergingStep extends BasePipelineStep {

	final static String NAME = "Moses InlineText Leveraging";
	
	private LocaleId sourceLocale;
	private LocaleId targetLocale;
	private URI inputURI;
	private RawDocument mosesDoc;
	private MosesTextFilter filter;
	private MergingParameters params;
	
	public MergingStep () {
		params = new MergingParameters();
	}
	
	@Override
	public String getDescription () {
		return "Leverages an original source document with its corresponding Moses InlineText file."
			+ " Expects: filter events. Sends back: filter events.";
	}

	@Override
	public String getName () {
		return NAME;
	}

	@StepParameterMapping(parameterType = StepParameterType.SOURCE_LOCALE)
	public void setSourceLocale (LocaleId sourceLocale) {
		this.sourceLocale = sourceLocale;
	}

	@SuppressWarnings("deprecation")
	@StepParameterMapping(parameterType = StepParameterType.TARGET_LOCALE)
	public void setTargetLocale (LocaleId targetLocale) {
		this.targetLocale = targetLocale;
	}

	@StepParameterMapping(parameterType = StepParameterType.INPUT_URI)
	public void setInputURI (URI inputURI) {
		this.inputURI = inputURI;
	}
	
	@StepParameterMapping(parameterType = StepParameterType.SECOND_INPUT_RAWDOC)
	public void setSecondInput(final RawDocument secondInput) {
		mosesDoc = secondInput;
	}

	@Override
	public IParameters getParameters() {
		return params;
	}

	@Override
	public void setParameters(IParameters params) {
		params = (MergingParameters)params;
	}

	@Override
	public Event handleEvent (Event event) {
		switch ( event.getEventType() ) {
		case START_DOCUMENT:
			processStartDocument(event.getStartDocument());
			break;
		case END_DOCUMENT:
			processEndDocument();
			break;
		case TEXT_UNIT:
			processTextUnit(event.getTextUnit());
		default:
			break;
		}
		return event;
	}
	
	private void processStartDocument (StartDocument sd) {
		if ( params.getForceAltTransOutput() ) {
			// Overwrite the option for the filters where it's possible.
			IParameters prm = sd.getFilterParameters();
			if ( prm != null ) {
				prm.setBoolean(Parameters.ADDALTTRANS, true);
				prm.setBoolean(Parameters.ADDALTTRANSGMODE, params.getUseGModeInAltTrans());
			}
		}
		
		// Open the corresponding Moses file
		// First try to get it from the secondary input
		RawDocument rd = mosesDoc;
		if ( rd == null ) {
			// If not available: guess the path from the input document
			// = same path plus a target extra extension
			String path = inputURI.getPath() + "." + targetLocale.toString();
			rd = new RawDocument(new File(path).toURI(), "UTF-8", sourceLocale);
		}
		// Make sure the Moses file filter configuration is set
		if ( Util.isEmpty(rd.getFilterConfigId()) ) {
			rd.setFilterConfigId("okf_mosestext");
		}
		
		// Open the Moses file
		filter = new MosesTextFilter();
		filter.open(rd);
		// Get the START_DOCUMENT event out of the way
		filter.hasNext();
		filter.next();
	}

	private void processEndDocument () {
		// Close the Moses filter
		if ( filter != null ) {
			filter.close();
			filter = null;
		}
	}

	private void processTextUnit (ITextUnit tu) {
		// Skip same text unit as for extraction
		if ( !tu.isTranslatable() ) return;
		
		// This is the text unit of the original file
		// We need to gather as many segments there is in this text unit in the Moses file
		
		// Create a target entry with empty segments
		TextContainer tc;
		if ( tu.hasTarget(targetLocale) ) {
			tc = tu.getTarget(targetLocale);
		}
		else { // Create a copy with empty segments
			tc = tu.createTarget(targetLocale, true, IResource.COPY_SEGMENTATION);
		}

		// Leverage on the container or on each segment
		// and make sure to add to any existing annotation rather than obliterate it 
		AltTranslationsAnnotation ann;
		if ( tc.contentIsOneSegment() ) {
			ann = tc.getAnnotation(AltTranslationsAnnotation.class);
			ann = leverage(ann, tu.getSource().getFirstContent(), tu.getId());
			tc.setAnnotation(ann); // Attach or re-attach the annotation
			// Copy also to target if needed
			copyToTarget(tc, null, ann);
		}
		else { // By segments
			Segment srcSeg;
			for ( Segment seg : tc.getSegments() ) {
				srcSeg = tu.getSource().getSegments().get(seg.id);
				
				ann = seg.getAnnotation(AltTranslationsAnnotation.class);
				ann = leverage(ann, srcSeg.text, tu.getId());
				seg.setAnnotation(ann); // Attach or re-attach the annotation
				// Copy also to target if needed
				copyToTarget(null, seg, ann);
			}
		}
		
	}

	private void copyToTarget (TextContainer tc,
		Segment seg,
		AltTranslationsAnnotation ann)
	{
		// Do we copy to the target?
		if ( !params.getCopyToTarget() ) return;
		// Get the existing content to check if it's empty
		if ( tc != null ) {
			if ( params.getOverwriteExistingTarget() || tc.isEmpty() ) {
				tc.setContent(ann.getLast().getTarget().getFirstContent());
			}
		}
		else { // Segment
			if ( params.getOverwriteExistingTarget() || seg.text.isEmpty() ) {
				seg.text = ann.getLast().getTarget().getFirstContent();
			}
		}
	}

	private AltTranslationsAnnotation leverage (AltTranslationsAnnotation ann,
		TextFragment srcFrag,
		String tuId)
	{
		// Next text unit in the Moses file should be the corresponding data
		// to this text fragment
		if ( !filter.hasNext() ) {
			throw new OkapiIOException(String.format(
				"The InlineText file is missing entries for text unit id='%s'", tuId));
		}
		Event event = filter.next();
		if ( event.getEventType() != EventType.TEXT_UNIT ) {
			throw new OkapiIOException(String.format(
				"The InlineText file is de-synchronized for text unit id='%s'", tuId));
		}
		
		// Retrieve the translated text
		ITextUnit mtu = event.getTextUnit();
		TextFragment trgFrag = mtu.getSource().getFirstContent();
		// Substitute the codes (and complete them if needed)
		TextUnitUtil.copySrcCodeDataToMatchingTrgCodes(srcFrag, trgFrag, true, true, null, null);
		
		// Create a new annotation if one does not exists already
		if ( ann == null ) {
			ann = new AltTranslationsAnnotation();
		}
		// Add the translation
		ann.add(sourceLocale, targetLocale, srcFrag,
			null, trgFrag,
			MatchType.MT, 10, "Moses-MT"); //TODO: make info parameters
		
		return ann;
	}

}
