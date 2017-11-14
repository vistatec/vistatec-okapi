package net.sf.okapi.lib.extra.steps;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.annotation.IAnnotation;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.resource.EndSubfilter;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.StartSubfilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubfilterLogger extends BasePipelineStep {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private StringBuilder sb;
	private LocaleId srcLoc;
	
	@Override
	public String getName() {
		return "Subfilter Logger";
	}

	@Override
	public String getDescription() {
		return "Logs Start/End Subfilter resources going through the pipeline.";
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
	protected Event handleStartSubfilter(Event event) {
		StartSubfilter ssf = event.getStartSubfilter();
		fillSB(sb, ssf, srcLoc);
		return super.handleDocumentPart(event);
	}
	
	@Override
	protected Event handleEndBatch(Event event) {
		logger.trace(sb.toString());
		return super.handleEndBatch(event);
	}
	
	private static void fillSB(StringBuilder sb, StartSubfilter ssf, LocaleId srcLoc) {
		sb.append(String.format("ssf [id=%s name=%s]", ssf.getId(), ssf.getName()));		
		sb.append(":");
		if (ssf.isReferent()) sb.append(" referent");
		sb.append("\n");
		
		if (ssf.getAnnotations() != null) {
//			sb.append("             ");
//			sb.append("Source annotations:");
//			sb.append("\n");
			for (IAnnotation annot : ssf.getAnnotations()) {
				sb.append("                    ");
				sb.append(annot.getClass().getName());
				sb.append(" ");
				sb.append(annot.toString());
				sb.append("\n");
			}		
		}
		
		if (ssf.getPropertyNames() != null && ssf.getPropertyNames().size() > 0) {
			sb.append("             ");
			sb.append("Properties:");
			sb.append("\n");
			for (String name : ssf.getPropertyNames()) {
				sb.append("                    ");
				sb.append(name);
				sb.append(" ");
				sb.append(ssf.getProperty(name).toString());
				sb.append("\n");
			}		
		}
		
		if (ssf.getSourcePropertyNames() != null && ssf.getSourcePropertyNames().size() > 0) {
			sb.append("             ");
			sb.append("Source properties:");
			sb.append("\n");
			
			for (String name : ssf.getSourcePropertyNames()) {
				sb.append("                    ");
				sb.append(name);
				sb.append(" ");
				sb.append(ssf.getSourceProperty(name).toString());
				sb.append("\n");
			}		
		}
				
		for (LocaleId locId : ssf.getTargetLocales()) {
			if (ssf.getTargetPropertyNames(locId) != null && ssf.getTargetPropertyNames(locId).size() > 0) {
				sb.append("             ");
				sb.append("Target properties:");
				sb.append("\n");
				
				for (String name : ssf.getTargetPropertyNames(locId)) {
					sb.append("                    ");
					sb.append(name);
					sb.append(" ");
					sb.append(ssf.getTargetProperty(locId, name).toString());
					sb.append("\n");
				}		
			}
		}		
		
		sb.append("             locale: " + ssf.getLocale());
		sb.append("\n             encoding: " + ssf.getEncoding());
		sb.append("\n             isMultilingual: " + (ssf.isMultilingual() ? "true" : "false"));
		sb.append("\n             params: " + (ssf.getFilterParameters() != null ? ssf.getFilterParameters().getClass().getName() : "null"));
		sb.append("\n             filterWriter: " + (ssf.getFilterWriter() != null ? ssf.getFilterWriter().getClass().getName() : "null"));
		sb.append("\n             hasUTF8BOM: " + (ssf.hasUTF8BOM() ? "true" : "false"));
		//sb.append("\n             lineBreak: " + ssf.getLineBreak().replaceAll("\\n", "\\\\n".replaceAll("\\r", "\\\\r")));
		sb.append("\n             lineBreak: " + logLinebreak(ssf.getLineBreak()));
		sb.append("\n");
		
		if (ssf.getSkeleton() != null) {
			sb.append(String.format("      Skeleton: %s", ssf.getSkeleton().toString()));
			sb.append("\n");
		}
	}
	
	private static void fillSB2(StringBuilder sb, EndSubfilter esf, LocaleId srcLoc) {
		sb.append(String.format("esf [id=%s]", esf.getId()));		
		sb.append("\n");
		
		if (esf.getAnnotations() != null) {
//			sb.append("             ");
//			sb.append("Source annotations:");
//			sb.append("\n");
			for (IAnnotation annot : esf.getAnnotations()) {
				sb.append("                    ");
				sb.append(annot.getClass().getName());
				sb.append(" ");
				sb.append(annot.toString());
				sb.append("\n");
			}		
		}		
		
		if (esf.getSkeleton() != null) {
			sb.append(String.format("      Skeleton: %s", esf.getSkeleton().toString()));
			sb.append("\n");
		}
	}
	
	private static String logLinebreak(String lineBreak) {
		if ((Util.LINEBREAK_DOS).equals(lineBreak)) return "\\r\\n";
		else if ((Util.LINEBREAK_MAC).equals(lineBreak)) return "\\r";
		else if ((Util.LINEBREAK_UNIX).equals(lineBreak)) return "\\n";
		return null;
	}

	public static String getSsfInfo(StartSubfilter ssf, LocaleId srcLoc) {
		StringBuilder sb = new StringBuilder("--------------------\n");
		fillSB(sb, ssf, srcLoc);
		return sb.toString();
	}
	
	public static String getEsfInfo(EndSubfilter esf, LocaleId srcLoc) {
		StringBuilder sb = new StringBuilder();
		fillSB2(sb, esf, srcLoc);
		sb.append("--------------------\n\n");
		return sb.toString();
	}
}
