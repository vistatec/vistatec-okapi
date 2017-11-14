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
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.exceptions.OkapiFilterCreationException;
import net.sf.okapi.common.exceptions.OkapiMergeException;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filters.IFilterConfigurationMapper;
import net.sf.okapi.common.io.InputStreamFromOutputStream;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
import net.sf.okapi.common.pipeline.annotations.StepParameterType;
import net.sf.okapi.common.pipelinedriver.PipelineDriver;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.filters.xliff.XLIFFFilter;
import net.sf.okapi.lib.tkit.merge.SkeletonMergerWriter;
import net.sf.okapi.lib.tkit.merge.TextUnitMerger;

/**
 * Tkit merger which re-filters the original source file to provide the
 * skeleton for merging. Uses lib-tkit's {@link SkeletonMergerWriter} and {@link TextUnitMerger}.
 * 
 * @author jimh
 * 
 */
public class OriginalDocumentXliffMergerStep extends BasePipelineStep {
	private IFilter filter;
	private IFilterConfigurationMapper fcMapper;
	private String outputEncoding;
	private LocaleId trgLoc;
	private RawDocument originalDocument;
	
	public OriginalDocumentXliffMergerStep() {
	}
	
	@Override
	public String getName() {
		return "Original Document Xliff Merger";
	}

	@Override
	public String getDescription() {
		return "Tkit merger which re-filters the original source file to provide the skeleton for merging.";
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
	
	@SuppressWarnings("resource")
	@Override
	protected Event handleRawDocument(final Event event) {
		filter = fcMapper.createFilter(originalDocument.getFilterConfigId(), filter);
		if (filter == null) {
			throw new OkapiFilterCreationException(String.format(
					"Cannot create the filter or load the configuration for '%s'",
					originalDocument.getFilterConfigId()));
		}
		filter.open(originalDocument);
		final SkeletonMergerWriter skelMergerWriter = new SkeletonMergerWriter(filter, null);
		final XLIFFFilter xlfFilter = new XLIFFFilter();
		skelMergerWriter.setOptions(trgLoc, outputEncoding);

		final InputStreamFromOutputStream<Void> is = new InputStreamFromOutputStream<Void>() {
			@Override
			protected Void produce(OutputStream sink) throws Exception {
				try {
					skelMergerWriter.setOutput(sink);
					xlfFilter.open(event.getRawDocument());
					while (xlfFilter.hasNext()) {
						skelMergerWriter.handleEvent(xlfFilter.next());
					}					
				} catch (Exception e) {
					close();
					throw new OkapiMergeException("Error merging from original file", e);
				} finally {
					if (xlfFilter != null) xlfFilter.close();
					skelMergerWriter.close();
					originalDocument.close();
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
}
