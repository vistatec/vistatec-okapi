package net.sf.okapi.lib.extra.steps;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.resource.DocumentPart;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.StartDocument;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TuDpLogger extends BasePipelineStep {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private StringBuilder sb;
	private LocaleId srcLoc;
	private int index;
	private boolean immediate;
	
	public TuDpLogger() {
		this(false);
	}
	
	public TuDpLogger(boolean immediate) {
		super();
		this.immediate = immediate;
	}
	
	@Override
	public String getName() {
		return "Text Unit and Document Part Logger";
	}

	@Override
	public String getDescription() {
		return "Logs Text Unit and Document Part resources going through the pipeline.";
	}
	
	@Override
	protected Event handleStartBatch(Event event) {
		sb = new StringBuilder("\n\n");
		return super.handleStartBatch(event);
	}

	@Override
	protected Event handleStartDocument(Event event) {
		StartDocument sd = (StartDocument) event.getResource();
		srcLoc = sd.getLocale();
		index = 0;
		return super.handleStartDocument(event);
	}
	
	@Override
	protected Event handleTextUnit(Event event) {
		sb.append(index++);
		sb.append("=========== \n");
		ITextUnit tu = event.getTextUnit();
		sb.append(TextUnitLogger.getTuInfo(tu, srcLoc));
		flushImmediate();
		return super.handleTextUnit(event);
	}
	
	private void flushImmediate() {
		if (!immediate) return;		
		sb = new StringBuilder();
	}

	@Override
	protected Event handleDocumentPart(Event event) {
		sb.append(index++);
		sb.append("=========== \n");
		DocumentPart dp = event.getDocumentPart();		
		sb.append(DocumentPartLogger.getDpInfo(dp, srcLoc));
		flushImmediate();
		return super.handleDocumentPart(event);
	}
	
	@Override
	protected Event handleEndBatch(Event event) {
		if (!immediate)
			logger.trace(sb.toString());
		return super.handleEndBatch(event);
	}
}
