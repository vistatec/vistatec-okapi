/*===========================================================================
  Copyright (C) 2011 by the Okapi Framework contributors
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

package net.sf.okapi.steps.idbasedcopy;

import java.util.HashMap;
import java.util.Map;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filters.IFilterConfigurationMapper;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
import net.sf.okapi.common.pipeline.annotations.StepParameterType;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.TextContainer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This step copies into a destination file (first input file) the text of a
 * reference file (second input file) for text units that have the same id. 
 * The ids are taken from the name (TextUnit.getName()) of each text unit.
 */
@UsingParameters(Parameters.class)
public class IdBasedCopyStep extends BasePipelineStep {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private Parameters params;
	private IFilterConfigurationMapper fcMapper;
	private LocaleId targetLocale;
	private RawDocument toCopyInput = null;
	private Map<String, ITextUnit> toCopy;
	private boolean useTargetText;

	public IdBasedCopyStep () {
		params = new Parameters();
	}

	@StepParameterMapping(parameterType = StepParameterType.FILTER_CONFIGURATION_MAPPER)
	public void setFilterConfigurationMapper (IFilterConfigurationMapper fcMapper) {
		this.fcMapper = fcMapper;
	}

	@SuppressWarnings("deprecation")
	@StepParameterMapping(parameterType = StepParameterType.TARGET_LOCALE)
	public void setTargetLocale (LocaleId targetLocale) {
		this.targetLocale = targetLocale;
	}

	@StepParameterMapping(parameterType = StepParameterType.SECOND_INPUT_RAWDOC)
	public void setSecondInput (RawDocument secondInput) {
		this.toCopyInput = secondInput;
	}

	@Override
	public String getDescription () {
		return "Copies the source text of the second input into the target of the first input based on matching id."
			+ "\nExpects: filter events. Sends back: filter events.";
	}

	@Override
	public String getName () {
		return "Id-Based Copy";
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
	protected Event handleStartDocument (Event event) {
		// Use target text if reference file is bilingual, otherwise use the source
		useTargetText = event.getStartDocument().isMultilingual();
		// Create the table if possible
		if ( toCopyInput == null ) {
			logger.warn("Second input file is not specified.");
			toCopy = null;
		}
		else {
			// Else: read the file
			readEntriesToCopy();
		}
		return event;
	}
	
	@Override
	protected Event handleEndDocument (Event event) {
		// Warn if we have any entries that were in the delat file, but not the input
		if ( toCopy != null ) {
			if ( toCopy.size() > 0 ) {
				for ( String id : toCopy.keySet() ) {
					logger.warn("Id '{}' is in the second file, but not in the main input.", id);
				}
			}
		}
		return event;
	}

	@Override
	protected Event handleTextUnit (Event event) {
		// No file to copy from
		if ( toCopy == null ) {
			return event;
		}
		
		ITextUnit tu = event.getTextUnit();
		// Skip non-translatable and empty
		if ( !tu.isTranslatable() ) {
			return event; // No change
		}

		// Find the matching id in the entries to copy
		ITextUnit toCopyTu = toCopy.get(tu.getName());
		TextContainer tc;
		if ( toCopyTu != null ) {
			if ( useTargetText ) tc = toCopyTu.getTarget(targetLocale);
			else tc = toCopyTu.getSource();
			if ( tc != null ) {
				tu.setTarget(targetLocale, tc);
				toCopy.remove(tu.getName());
				if ( params.getMarkAsTranslateNo() ) {
					tu.setIsTranslatable(false);
				}
				if ( params.getMarkAsApproved() ) {
					tu.setTargetProperty(targetLocale, new Property(Property.APPROVED, "yes"));
				}
			}
		}

		return event;
	}

	private void readEntriesToCopy () {
		toCopy = new HashMap<String, ITextUnit>();
		try (IFilter filter = fcMapper.createFilter(toCopyInput.getFilterConfigId(), null)) {
			// Open the second input for this batch item
			filter.open(toCopyInput);

			while ( filter.hasNext() ) {
				final Event event = filter.next();
				if ( event.getEventType() == EventType.TEXT_UNIT ) {
					ITextUnit tu = event.getTextUnit();
					String id = tu.getName();
					if ( Util.isEmpty(id) ) {
						logger.warn("Entry without id detected in second file.");
						continue;
					}
					// Else: put in the hash table
					if ( toCopy.get(id) != null ) {
						logger.warn("Duplicate id detected: {}", id);
						continue;
					}
					toCopy.put(id, tu);
				}
			}
		}
	}
}
