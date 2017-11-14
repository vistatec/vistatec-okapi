package net.sf.okapi.lib.extra.steps;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.resource.EndSubfilter;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.StartSubfilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TuSsfLogger extends BasePipelineStep {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private StringBuilder sb;
	private LocaleId srcLoc;
	
	@Override
	public String getName() {
		return "Text Unit and Start Group Logger";
	}

	@Override
	public String getDescription() {
		return "Logs Text Unit and Start Group resources going through the pipeline.";
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
		return super.handleStartDocument(event);
	}
	
	@Override
	protected Event handleTextUnit(Event event) {
		ITextUnit tu = event.getTextUnit();
		sb.append(TextUnitLogger.getTuInfo(tu, srcLoc));
		return super.handleTextUnit(event);
	}
	
//	@Override
//	protected Event handleDocumentPart(Event event) {
//		DocumentPart dp = event.getDocumentPart();
//		sb.append(DocumentPartLogger.getDpInfo(dp, srcLoc));
//		return super.handleDocumentPart(event);
//	}
	
	@Override
	protected Event handleStartSubfilter(Event event) {
		StartSubfilter ssf = event.getStartSubfilter();
		sb.append(SubfilterLogger.getSsfInfo(ssf, srcLoc));
		return super.handleStartGroup(event);
	}
	
	@Override
	protected Event handleEndSubfilter(Event event) {
		EndSubfilter esf = event.getEndSubfilter();
		sb.append(SubfilterLogger.getEsfInfo(esf, srcLoc));
		return super.handleEndSubfilter(event);
	}
	
	@Override
	protected Event handleEndBatch(Event event) {
		logger.trace(sb.toString());
		return super.handleEndBatch(event);
	}
}
