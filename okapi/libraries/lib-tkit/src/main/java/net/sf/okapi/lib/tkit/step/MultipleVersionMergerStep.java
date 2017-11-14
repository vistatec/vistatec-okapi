package net.sf.okapi.lib.tkit.step;

import java.io.OutputStream;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filters.IFilterConfigurationMapper;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.io.InputStreamFromOutputStream;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
import net.sf.okapi.common.pipeline.annotations.StepParameterType;
import net.sf.okapi.common.pipelinedriver.PipelineDriver;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.filters.xliff.XLIFFFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tkit merger which re-filters the original source file to provide the skeleton
 * using previous versions of Okapi. Merges are tried until a merge successful.
 * Normally we assume the latest version of Okapi has already been tried and
 * failed, but this step can begin with any Okapi version.
 * 
 * @author jimh
 */

public class MultipleVersionMergerStep extends BasePipelineStep {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private IFilter filter;
	private IFilterWriter writer;
	private IFilterConfigurationMapper fcMapper;
	private XLIFFFilter xlfReader;
	private String outputEncoding;
	private LocaleId trgLoc;
	private RawDocument originalDocument;
	private MultipleVersionsParameters params;

	public MultipleVersionMergerStep() {
		params = new MultipleVersionsParameters();
	}

	@Override
	public String getName() {
		return "Refilter Tkit Merger";
	}

	@Override
	public String getDescription() {
		return "Tkit merger which re-filters the original source file to provide the skeleton.";
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
	 * @param secondInput
	 *            Original source document
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

	@Override
	public IParameters getParameters() {
		return params;
	}

	@Override
	public void setParameters(IParameters params) {
		this.params = (MultipleVersionsParameters) params;
	}

	/*
	 * For now, take all the info from the argument rather than directly the
	 * XLIFF file.
	 */
	@SuppressWarnings("resource")
	@Override
	protected Event handleRawDocument(final Event event) {
		final RawDocument xlfRawDoc = event.getRawDocument();
		final InputStreamFromOutputStream<Void> is = new InputStreamFromOutputStream<Void>() {
			@Override
			protected Void produce(OutputStream sink) throws Exception {
				try {
				} finally {
				}

				return null;
			}
		};

		// Writer step closes the RawDocument
		//return new Event(EventType.RAW_DOCUMENT, new RawDocument(is, outputEncoding, trgLoc));
		return null;
	}

	@Override
	public void cancel() {
	}

	@Override
	public void destroy() {
	}
}
