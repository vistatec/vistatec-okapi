package net.sf.okapi.lib.tkit.step;

import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.exceptions.OkapiMergeException;
import net.sf.okapi.common.filters.IFilterConfigurationMapper;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
import net.sf.okapi.common.pipeline.annotations.StepParameterType;
import net.sf.okapi.common.pipelinedriver.PipelineDriver;
import net.sf.okapi.common.resource.RawDocument;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Step that combines different merging approaches. Combines skeleton-based,
 * legacy and version switching merges in sequence until a successful merge is
 * obtained.
 * 
 * @author jimh
 * 
 */
public class CombinedXliffMergerStep extends BasePipelineStep {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private List<LocaleId> trgLocs;
	private RawDocument skeleton;
	private RawDocument originalDocument;
	private IFilterConfigurationMapper fcMapper;
	private String outputEncoding;
	private SkeletonXliffMergerStep skeletonMerger;
	private OriginalDocumentXliffMergerStep originalDocumentMerger;
	
	public CombinedXliffMergerStep() {
		skeletonMerger = new SkeletonXliffMergerStep();
		originalDocumentMerger = new OriginalDocumentXliffMergerStep();
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
		this.trgLocs = targetLocales;
	}

	/**
	 * This is the skeleton document
	 * 
	 * @param secondInput
	 */
	@StepParameterMapping(parameterType = StepParameterType.SECOND_INPUT_RAWDOC)
	public void setSecondInput(final RawDocument secondInput) {
		this.skeleton = secondInput;		
	}

	/**
	 * This is the skeleton document
	 * 
	 * @param secondInput
	 */
	@StepParameterMapping(parameterType = StepParameterType.THIRD_INPUT_RAWDOC)
	public void setThirdInput(final RawDocument thirdInput) {
		this.originalDocument = thirdInput;		
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
	protected Event handleRawDocument(final Event event) {
		Event me = null;
		Throwable skeletonCuase = null;
		Throwable originalDocumentCuase = null;
		Throwable previousVersionCuase = null;
		try {			
			// first try skeleton merge
			try {
				me = skeletonMerge(event);
			} catch (Exception e) {
				skeletonCuase = ExceptionUtils.getRootCause(e);
				if (skeletonCuase == null) skeletonCuase = e;
				logger.error("Combined merge. Error during skeleton merge root cuase: {}", skeletonCuase.getMessage());
				logger.error("Combined merge. Error during skeleton merge, full trace", e);
			}
			
			// if skeleton merge fails then try original document merge
			if (me == null) {
				try {
					me = originalDocumentMerge(event);
				} catch (Exception e) {
					originalDocumentCuase = ExceptionUtils.getRootCause(e);
					if (originalDocumentCuase == null) originalDocumentCuase = e;
					logger.error("Combined merge. Error during original document merge root cuase: {}", originalDocumentCuase.getMessage());				   			    		
					logger.error("Combined merge. Error during original document merge. Full trace.", e);
				}
			}
		} finally {
			if (skeleton != null) skeleton.close(); 
			if (originalDocument != null) originalDocument.close(); 
			event.getRawDocument().close();		
		}
		
		// if we reached here there is no hope, throw an exception
		if (me == null) {
			StringBuilder c = new StringBuilder();
			c.append(skeletonCuase == null ? "" : skeletonCuase.getMessage()+"\n");
			c.append(originalDocumentCuase == null ? "" : originalDocumentCuase.getMessage()+"\n");
			c.append(previousVersionCuase == null ? "" : previousVersionCuase.getMessage());
			logger.error("Combined merge. Cannot merge using availible methods: {}", c.toString());
			throw new OkapiMergeException(String.format("Combined merge. Cannot merge using availible methods: %s", c.toString()));
		}
		
		// Writer step closes the RawDocument
		return me;
	}

	@Override
	public void cancel() {
	}

	@Override
	public void destroy() {
		if (skeletonMerger != null) skeletonMerger.destroy();
		if (originalDocumentMerger != null) originalDocumentMerger.destroy();
	}
	
	@SuppressWarnings("resource")
	private Event skeletonMerge(Event event) {
		if (this.skeleton == null) {
			return null;
		}
		
		RawDocument originalXliff = event.getRawDocument();
		RawDocument xliffCopy = new RawDocument(originalXliff.getStream(), originalXliff.getEncoding(), 
				originalXliff.getSourceLocale(), originalXliff.getTargetLocale());
		
		Event me = null;
		try {
			skeletonMerger.setOutputEncoding(outputEncoding);
			skeletonMerger.setSecondInput(skeleton);
			skeletonMerger.setTargetLocales(trgLocs);
			me = skeletonMerger.handleRawDocument(new Event(EventType.RAW_DOCUMENT, xliffCopy));
		} finally {
			if (xliffCopy != null) xliffCopy.close();
		}

		return me;
	}
	
	@SuppressWarnings("resource")
	private Event originalDocumentMerge(Event event) {
		if (this.originalDocument == null) {
			return null;
		}
		
		RawDocument originalXliff = event.getRawDocument();
		RawDocument xliffCopy = new RawDocument(originalXliff.getStream(), originalXliff.getEncoding(), 
				originalXliff.getSourceLocale(), originalXliff.getTargetLocale());
		
		Event me = null;
		try {
			originalDocumentMerger.setOutputEncoding(outputEncoding);
			originalDocumentMerger.setSecondInput(originalDocument);
			originalDocumentMerger.setTargetLocales(trgLocs);
			originalDocumentMerger.setFilterConfigurationMapper(fcMapper);
			me = originalDocumentMerger.handleRawDocument(new Event(EventType.RAW_DOCUMENT, xliffCopy));
		} finally {
			if (xliffCopy != null) xliffCopy.close();
		}
		
		return me;
	}
}
