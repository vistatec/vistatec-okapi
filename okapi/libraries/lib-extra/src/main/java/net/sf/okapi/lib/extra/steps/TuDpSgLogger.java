package net.sf.okapi.lib.extra.steps;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.resource.DocumentPart;
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.StartGroup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TuDpSgLogger extends BasePipelineStep {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private StringBuilder sb;
	private LocaleId srcLoc;
	private int index;
	
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
		index = 0;
		return super.handleStartDocument(event);
	}
	
	@Override
	protected Event handleTextUnit(Event event) {
		sb.append(index++);
		sb.append("=========== \n");
		ITextUnit tu = event.getTextUnit();
		sb.append(TextUnitLogger.getTuInfo(tu, srcLoc));
		return super.handleTextUnit(event);
	}
	
	@Override
	protected Event handleDocumentPart(Event event) {
		sb.append(index++);
		sb.append("=========== \n");
		DocumentPart dp = event.getDocumentPart();
		sb.append(DocumentPartLogger.getDpInfo(dp, srcLoc));
		return super.handleDocumentPart(event);
	}
	
	@Override
	protected Event handleStartGroup(Event event) {
		sb.append(index++);
		sb.append("=========== \n");
		StartGroup sg = event.getStartGroup();
		sb.append(GroupLogger.getSgInfo(sg, srcLoc));
		return super.handleStartGroup(event);
	}
	
	@Override
	protected Event handleEndGroup(Event event) {
		sb.append(index++);
		sb.append("=========== \n");
		Ending eg = event.getEndGroup();
		sb.append(GroupLogger.getEgInfo(eg, srcLoc));
		return super.handleEndGroup(event);
	}
	
	@Override
	protected Event handleEndBatch(Event event) {
		logger.trace(sb.toString());
		return super.handleEndBatch(event);
	}
}
