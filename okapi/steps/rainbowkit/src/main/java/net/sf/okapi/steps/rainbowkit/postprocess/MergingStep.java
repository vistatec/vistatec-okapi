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

package net.sf.okapi.steps.rainbowkit.postprocess;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.exceptions.OkapiBadFilterInputException;
import net.sf.okapi.common.filters.IFilterConfigurationMapper;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
import net.sf.okapi.common.pipeline.annotations.StepParameterType;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.filters.rainbowkit.Manifest;
import net.sf.okapi.filters.rainbowkit.MergingInfo;

@UsingParameters(Parameters.class)
public class MergingStep extends BasePipelineStep {

	public static final String NAME = "Rainbow Translation Kit Merging";
	
	private Parameters params;
	private MergingInfo info;
	private Merger merger;
	private SkeletonMerger merger2;
	private boolean useSkel = false;
	private IFilterConfigurationMapper fcMapper;
	private LocaleId targetLocale;

	public MergingStep () {
		super();
		params = new Parameters();
	}

	@Override
	public String getDescription () {
		return "Post-process a Rainbow translation kit."
			+ " Expects: filter events. Sends back: filter events or raw documents.";
	}

	@Override
	public String getName () {
		return NAME;
	}

	@StepParameterMapping(parameterType = StepParameterType.FILTER_CONFIGURATION_MAPPER)
	public void setFilterConfigurationMapper (IFilterConfigurationMapper fcMapper) {
		this.fcMapper = fcMapper;
	}
	
	public IFilterConfigurationMapper getFilterConfigurationMapper() {
		return fcMapper;
	}
	
	@SuppressWarnings("deprecation")
	@StepParameterMapping(parameterType = StepParameterType.TARGET_LOCALE)
	public void setTargetLocale (LocaleId targetLocale) {
		this.targetLocale = targetLocale; 
	}
	
	public LocaleId getTargetLocale() {
		return targetLocale;
	}
	
	@Override
	public Event handleEvent (Event event) {
		switch ( event.getEventType() ) {
		case START_DOCUMENT:
			return handleStartDocument(event);
		default:
			if ( useSkel ) {
				if ( merger2 != null ) {
					return merger2.handleEvent(event);
				}
			}
			else {
				if ( merger != null ) {
					return merger.handleEvent(event);
				}
			}
		}
		return event;
	}

	@Override
	protected Event handleStartDocument (Event event) {
		// Initial document is expected to be a manifest
		StartDocument sd = event.getStartDocument();
		info = sd.getAnnotation(MergingInfo.class);
		if ( info == null ) {
			throw new OkapiBadFilterInputException("Start document is missing the merging info annotation.");
		}
		Manifest manifest = sd.getAnnotation(Manifest.class);
		if ( manifest == null ) {
			throw new OkapiBadFilterInputException("Start document is missing the manifest annotation.");
		}
		
		// Create the merger (for each new manifest)
		boolean alwaysForceTargetLocale = Manifest.EXTRACTIONTYPE_ONTRAM.equals(info.getExtractionType());
		LocaleId targetLocaleToUse;
		if ( params.getForceTargetLocale() || alwaysForceTargetLocale) {
			targetLocaleToUse = targetLocale;
		}
		else {
			targetLocaleToUse = null;
		}
		
		useSkel = info.getUseSkeleton();
		if ( useSkel ) {
			merger2 = new SkeletonMerger(manifest, params.getPreserveSegmentation(),
				targetLocaleToUse, params.getReturnRawDocument(), params.getOverrideOutputPath());
			return merger2.startMerging(info, event);
		}
		else {
			merger = new Merger(manifest, fcMapper, params.getPreserveSegmentation(),
				targetLocaleToUse, params.getReturnRawDocument(), params.getOverrideOutputPath());
			return merger.startMerging(info, event);
		}
	}

	@Override
	public IParameters getParameters () {
		return params;
	}
	
	@Override
	public void setParameters (IParameters params) {
		this.params = (Parameters)params;
	}

	protected int getErrorCount () {
		if ( merger != null ) return merger.getErrorCount();
		else if ( merger2 != null ) return merger2.getErrorCount();
		return -1;
	}

}
