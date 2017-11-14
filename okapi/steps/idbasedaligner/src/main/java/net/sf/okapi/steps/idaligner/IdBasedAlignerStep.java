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

package net.sf.okapi.steps.idaligner;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.ExecutionContext;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.IResource;
import net.sf.okapi.common.IUserPrompt;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.annotation.AltTranslation;
import net.sf.okapi.common.annotation.AltTranslationsAnnotation;
import net.sf.okapi.common.exceptions.OkapiBadStepInputException;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filters.IFilterConfigurationMapper;
import net.sf.okapi.common.filterwriter.TMXWriter;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
import net.sf.okapi.common.pipeline.annotations.StepParameterType;
import net.sf.okapi.common.query.MatchType;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.MultiEvent;
import net.sf.okapi.common.resource.PipelineParameters;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextUnit;
import net.sf.okapi.common.resource.TextUnitUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Align two {@link TextUnit}s based on matching ids. The ids are taken from the name ({@link TextUnit#getName}) 
 * or id ({@link TextUnit#getId}) of each {@link TextUnit}, depending on configuration.
 * Any {@link IFilter} that produces a name for its {@link TextUnit}s will work with this aligner.
 * Expects filtered {@link Event}s as input and returns a new (aligned) bi-lingual {@link TextUnit} {@link Event}. Optionally
 * produce a TMX file in the specified output path.
 * 
 * @author Greg Perkins
 * @author HargraveJE
 * 
 */
@UsingParameters(Parameters.class)
public class IdBasedAlignerStep extends BasePipelineStep {

	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	private Parameters params;
	private IFilterConfigurationMapper fcMapper;
	private LocaleId sourceLocale;
	private LocaleId targetLocale;
	private RawDocument targetInput = null;
	private ExecutionContext context;
	private TMXWriter tmx;
	private Map<String, ITextUnit> targetTextUnitMap;
	private boolean useTargetText;
	private boolean docHasNoMatch;

	public IdBasedAlignerStep() {
		params = new Parameters();
	}

	@StepParameterMapping(parameterType = StepParameterType.FILTER_CONFIGURATION_MAPPER)
	public void setFilterConfigurationMapper(IFilterConfigurationMapper fcMapper) {
		this.fcMapper = fcMapper;
	}

	@StepParameterMapping(parameterType = StepParameterType.SOURCE_LOCALE)
	public void setSourceLocale(LocaleId sourceLocale) {
		this.sourceLocale = sourceLocale;
	}

	@SuppressWarnings("deprecation")
	@StepParameterMapping(parameterType = StepParameterType.TARGET_LOCALE)
	public void setTargetLocale(LocaleId targetLocale) {
		this.targetLocale = targetLocale;
	}

	@StepParameterMapping(parameterType = StepParameterType.SECOND_INPUT_RAWDOC)
	public void setSecondInput(RawDocument secondInput) {
		this.targetInput = secondInput;
	}

	@StepParameterMapping(parameterType = StepParameterType.EXECUTION_CONTEXT)
	public void setExecutionContext (ExecutionContext context) {
		this.context = context;
	}

	@Override
	public String getDescription() {
		return "Align text units in two id-based files (e.g. Java properties)."
			+ " Expects: filter events. Sends back: filter events.";
	}

	@Override
	public String getName() {
		return "Id-Based Aligner";
	}

	@Override
	public Parameters getParameters() {
		return params;
	}

	@Override
	public void setParameters(final IParameters params) {
		this.params = (Parameters) params;
	}

	@Override
	protected Event handleStartBatch(final Event event) {		
		// Start TMX writer (one for all input documents)
		if (tmx == null && params.getGenerateTMX()) {
			File outFile = new File(params.getTmxOutputPath());
			if ( outFile.exists() ) promptShouldOverwrite();
			tmx = new TMXWriter(outFile.getAbsolutePath());
			tmx.writeStartDocument(sourceLocale, targetLocale, getClass().getName(), null,
					"paragraph", null, null);
		}

		return event;
	}

	@Override
	protected Event handleEndBatch(final Event event) {
		if ( tmx != null ) {
			tmx.writeEndDocument();
			tmx.close();
			tmx = null;
			LOGGER.info("Wrote TMX to " + new File(params.getTmxOutputPath()).getAbsolutePath());
		}
		return event;
	}

	@Override
	protected Event handleStartDocument(Event event) {
		if (targetInput == null) {
			throw new OkapiBadStepInputException("Second input file (target) not configured.");
		}
		docHasNoMatch = false;
		getTargetTextUnits();
		return eventIndicatingTargetWasConsumed(event);
	}
	
	private Event eventIndicatingTargetWasConsumed(Event startDocEvent)
    {
		List<Event> list = new ArrayList<Event>();
		
		// Change the pipeline parameters for the raw-document-related data
		PipelineParameters pp = new PipelineParameters();
		pp.setSecondInputRawDocument(null);
		
		// Add the PipelineParameters event to the list
		list.add(new Event(EventType.PIPELINE_PARAMETERS, pp));
		
		// Add the original StartDocument event to the list
		list.add(startDocEvent);
		
		// Return the list as a multiple-event event
		// now the StartDocument event in subsequent steps won't try to read the target
		return new Event(EventType.MULTI_EVENT, new MultiEvent(list));
    }

	@Override
	protected Event handleEndDocument(Event event) {
		if ( docHasNoMatch ) {
			LOGGER.warn("One or more entries has no match.");
		}

		return event;
	}

	private String getAlignmentKey(ITextUnit tu) {
	    return params.isUseTextUnitIds() ? tu.getId() : tu.getName();
	}

	@Override
	protected Event handleTextUnit (Event sourceEvent) {
		int score = 100;
		
		if (sourceEvent.getTextUnit().getSource().hasBeenSegmented()) {
			throw new OkapiBadStepInputException("IdBasedAlignerStep only aligns unsegmented text units.");
		}

		ITextUnit sourceTu = sourceEvent.getTextUnit();

		// Skip non-translatable and empty
		if (!sourceTu.isTranslatable() || sourceTu.isEmpty() || 
				!sourceTu.getSource().hasText()) {
			return sourceEvent;
		}
		// Populate the target TU
		ITextUnit alignedTextUnit = sourceTu.clone();

		boolean tuHasNoMatch = false;

		TextContainer targetTC = alignedTextUnit.createTarget(targetLocale, false, IResource.COPY_PROPERTIES);

		// Get the target content from the reference (if possible)
		ITextUnit refTu = targetTextUnitMap.get(getAlignmentKey(sourceTu));
		TextContainer refTc = null;
		boolean missingReferenceMatch = true;
		if (( refTu != null ) && !refTu.isEmpty() ) {
			if ( useTargetText ) {
				// For bilingual reference file: check also the source of the reference
				TextContainer srcRefTc = refTu.getSource();
				// Use the target only if the source is the same
				if ( srcRefTc.compareTo(alignedTextUnit.getSource(), true) == 0 ) {
					refTc = refTu.getTarget(targetLocale);
				}
				else {
					// We had a match, but the source text is not the same
					missingReferenceMatch = false;
				}
			}
			else { // Monolingual files:
				// Use the source of the reference as the target of the aligned TU
				refTc = refTu.getSource();
			}
		}
		
		if (( refTc != null ) && refTc.hasText()) {			
			// align codes (assume filter as numbered them correctly)										
			alignedTextUnit.getSource().getFirstContent().alignCodeIds(refTc.getFirstContent());		
			
			// adjust codes to match new source
			TextFragment tf = TextUnitUtil.copySrcCodeDataToMatchingTrgCodes(
				sourceTu.getSource().getFirstContent(),
				refTc.getFirstContent(), 
				true, false, null, alignedTextUnit);									
			
			if ( params.isCopyToTarget() ) {				
				targetTC.setContent(tf);
				Property prop = refTc.getProperty(Property.APPROVED);
				if ( prop != null ) targetTC.setProperty(prop.clone());
			}			
			
			if ( params.isStoreAsAltTranslation() ) {
				// make an AltTranslation and attach to the target container
				AltTranslation alt = new AltTranslation(sourceLocale, targetLocale, 
					alignedTextUnit.getSource().getUnSegmentedContentCopy(), 
					null, tf, MatchType.EXACT_UNIQUE_ID, score, getName());
								
				// add the annotation to the target container since we are diffing paragraphs only
				// we may need to create the target if it doesn't exist
				AltTranslationsAnnotation alta = TextUnitUtil.addAltTranslation(targetTC, alt);
				// resort AltTranslation in case we already had some in the list
				alta.sort();
			}
		}
		else { // No match in the reference file
			if ( missingReferenceMatch ) {
				if ( getAlignmentKey(sourceTu) == null ) {
					LOGGER.info("Entry without original identifier (id='{}').", sourceTu.getId());
					tuHasNoMatch = true;
				}
				else {
					LOGGER.info("No match found for {}", getAlignmentKey(sourceTu));
					tuHasNoMatch = true;
				}

			}
			else {
				LOGGER.info("Source texts differ for {}", getAlignmentKey(sourceTu));
				tuHasNoMatch = true;
			}
			if ( params.getReplaceWithSource()) {
				// Use the source text if there is no target
				alignedTextUnit.setTarget(targetLocale, sourceTu.getSource());
			}
		}
		
		if ( !(params.isSuppressTusWithNoTarget() && tuHasNoMatch) ) {
			// Send the aligned TU to the TMX file or pass it on
			if ( params.getGenerateTMX() ) {
				tmx.writeTUFull(alignedTextUnit);
			}
			else { // Otherwise send each aligned TextUnit downstream
				return new Event(EventType.TEXT_UNIT, alignedTextUnit);
			}
		}

		docHasNoMatch = docHasNoMatch || tuHasNoMatch;

		return sourceEvent;
	}

	private void getTargetTextUnits() {
		try (IFilter filter = fcMapper.createFilter(targetInput.getFilterConfigId(), null)) {
			// Create the map
			targetTextUnitMap = new HashMap<String, ITextUnit>();
			
			// Open the second input for this batch item
			filter.open(targetInput);

			while (filter.hasNext()) {
				final Event event = filter.next();
				if ( event.getEventType() == EventType.TEXT_UNIT ) {
					ITextUnit tu = event.getTextUnit();
					
					// check if this is a TU without a target (probably a parent tu)
					if (!tu.getSource().hasText()) {
						continue;
					}

					// check if we have a name value
					if (getAlignmentKey(tu) == null) {
						LOGGER.info("Missing original identifier in target (id='{}').", tu.getId());
						docHasNoMatch = true;
						continue;
					}

					// check if we have a duplicate name
					ITextUnit target = targetTextUnitMap.get(getAlignmentKey(tu));
					if (target != null) {
						String message = "Duplicate entry for {}";
						if ( target.getSource().toString().equals(tu.getSource().toString()) ) {
							message += " (but entry text is identical)";
						}
						LOGGER.info(message, getAlignmentKey(tu));
						docHasNoMatch = true;
						continue;
					}
					
					// safe to continue storing the match
					targetTextUnitMap.put(getAlignmentKey(tu), tu);
				}
				else if ( event.getEventType() == EventType.START_DOCUMENT ) {
					// Use target text if reference file is bilingual, otherwise use the source
					useTargetText = event.getStartDocument().isMultilingual();
				}
			}
		}
	}

	private void promptShouldOverwrite() {
		if ( context == null || context.getIsNoPrompt() ) return;
		
		String promptClass = context.getIsGui() ? "net.sf.okapi.common.ui.UserPrompt"
			: "net.sf.okapi.common.UserPrompt";
		
		IUserPrompt p;
		try {
			p = (IUserPrompt)Class.forName(promptClass).newInstance();
			p.initialize(context.getUiParent(), context.getApplicationName());
		}
		catch ( Throwable e ) {
			throw new InstantiationError("Could not instantiate user prompt.");
		}
		p.promptOKCancel("A file already exists in the target location.\nSelect \"OK\" to overwrite it.");
	}
}
