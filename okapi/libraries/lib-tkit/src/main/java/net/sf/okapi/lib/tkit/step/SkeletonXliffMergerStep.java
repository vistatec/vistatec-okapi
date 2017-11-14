package net.sf.okapi.lib.tkit.step;

import java.io.OutputStream;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.exceptions.OkapiMergeException;
import net.sf.okapi.common.io.InputStreamFromOutputStream;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
import net.sf.okapi.common.pipeline.annotations.StepParameterType;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.filters.xliff.XLIFFFilter;
import net.sf.okapi.lib.tkit.merge.SkeletonMergerWriter;
/**
 * An XLIFF merger which uses a JSON-based skeleton file instead of the 
 * original source document.
 * 
 * @author jimh
 *
 */
public class SkeletonXliffMergerStep extends BasePipelineStep {
	private LocaleId trgLoc;
	private RawDocument skeleton;
	private SkeletonMergerWriter skelMergerWriter;
	private String outputEncoding;

	public SkeletonXliffMergerStep() {
		skelMergerWriter = new SkeletonMergerWriter();
	}

	@Override
	public String getName() {
		return "Skeleton Merger Step";
	}

	@Override
	public String getDescription() {
		return "XLIFF merger which uses a JSON-based skeleton file instead of the original source document.";
	}

	/**
	 * 
	 * @param outputEncoding
	 */
	@StepParameterMapping(parameterType = StepParameterType.OUTPUT_ENCODING)
	public void setOutputEncoding(final String outputEncoding) {
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
	 * This is the skeleton document
	 * 
	 * @param secondInput
	 */
	@StepParameterMapping(parameterType = StepParameterType.SECOND_INPUT_RAWDOC)
	public void setSecondInput(final RawDocument secondInput) {
		this.skeleton = secondInput;
		((net.sf.okapi.lib.tkit.merge.Parameters) skelMergerWriter.getParameters())
				.setSkeletonInputStream(this.skeleton.getStream());
	}

	@SuppressWarnings("resource")
	@Override
	protected Event handleRawDocument(final Event event) {
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
					throw new OkapiMergeException("Error merging skeleton file", e);
				} finally {
					if (xlfFilter != null) xlfFilter.close();
					skelMergerWriter.close();
					skeleton.close();
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
		if (skelMergerWriter != null) skelMergerWriter.close();
	}
}
