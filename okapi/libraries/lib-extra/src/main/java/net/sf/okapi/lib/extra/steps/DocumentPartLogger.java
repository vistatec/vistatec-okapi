package net.sf.okapi.lib.extra.steps;

import net.sf.okapi.common.ClassUtil;
import net.sf.okapi.common.Event;
import net.sf.okapi.common.ISkeleton;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.annotation.IAnnotation;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.resource.DocumentPart;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.skeleton.GenericSkeleton;
import net.sf.okapi.common.skeleton.GenericSkeletonPart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DocumentPartLogger extends BasePipelineStep {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private StringBuilder sb;
	private LocaleId srcLoc;
	
	@Override
	public String getName() {
		return "Document Part Logger";
	}

	@Override
	public String getDescription() {
		return "Logs Document Part resources going through the pipeline.";
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
	protected Event handleDocumentPart(Event event) {
		DocumentPart dp = event.getDocumentPart();
		fillSB(sb, dp, srcLoc);
		return super.handleDocumentPart(event);
	}
	
	@Override
	protected Event handleEndBatch(Event event) {
		logger.trace(sb.toString());
		return super.handleEndBatch(event);
	}
	
	private static void listProperties(DocumentPart dp, StringBuilder sb, String descr) {
		for (String propName : dp.getPropertyNames()) {
			sb.append("                    ");
			sb.append(descr);
			sb.append(propName);
			sb.append("=");
			sb.append(dp.getProperty(propName));
			sb.append("\n");
		}
	}
	
	private static void listSkeletonParts(ISkeleton skel, StringBuilder sb, String descr) {
		if (skel instanceof GenericSkeleton) {
			for (GenericSkeletonPart part : ((GenericSkeleton) skel).getParts()) {
				sb.append("          ");
				sb.append(descr);
				sb.append(String.format("loc=%s, parent=%s [%s] - %s",
						part.getLocale() != null ? part.getLocale().toString() : "null", 
						part.getParent() != null && !(part.getParent() instanceof TextContainer) ? part.getParent().getId() : "null",
						part.getParent() != null ? ClassUtil.getShortClassName(part.getParent().getClass()) : "",
						part.toString()
						));
				sb.append("\n");
			}
		}
	}
	
	private static void fillSB(StringBuilder sb, DocumentPart dp, LocaleId srcLoc) {
		sb.append("dp [" + dp.getId() + "]");		
		sb.append(":");
		if (dp.isReferent()) sb.append(" referent");
		sb.append("\n");
		
		listProperties(dp, sb, "DP property: ");
		
		if (dp.getAnnotations() != null) {
//			sb.append("             ");
//			sb.append("Source annotations:");
//			sb.append("\n");
			for (IAnnotation annot : dp.getAnnotations()) {
				sb.append("                    ");
				sb.append(annot.getClass().getName());
				sb.append(" ");
				sb.append(annot.toString());
				sb.append("\n");
			}		
		}
		
//		if (dp.getPropertyNames() != null && dp.getPropertyNames().size() > 0) {
//			sb.append("             ");
//			sb.append("Properties:");
//			sb.append("\n");
//			for (String name : dp.getPropertyNames()) {
//				sb.append("                    ");
//				sb.append(name);
//				sb.append(" ");
//				sb.append(dp.getProperty(name).toString());
//				sb.append("\n");
//			}		
//		}
		
		if (dp.getSourcePropertyNames() != null && dp.getSourcePropertyNames().size() > 0) {
			sb.append("             ");
			sb.append("Source properties:");
			sb.append("\n");
			
			for (String name : dp.getSourcePropertyNames()) {
				sb.append("                    ");
				sb.append(name);
				sb.append(" ");
				sb.append(dp.getSourceProperty(name).toString());
				sb.append("\n");
			}		
		}
				
		for (LocaleId locId : dp.getTargetLocales()) {
			if (dp.getTargetPropertyNames(locId) != null && dp.getTargetPropertyNames(locId).size() > 0) {
				sb.append("             ");
				sb.append("Target properties:");
				sb.append("\n");
				
				for (String name : dp.getTargetPropertyNames(locId)) {
					sb.append("                    ");
					sb.append(name);
					sb.append(" ");
					sb.append(dp.getTargetProperty(locId, name).toString());
					sb.append("\n");
				}		
			}
		}		
		
		if (dp.getSkeleton() != null) {
			sb.append(String.format("      Skeleton: %s", dp.getSkeleton().toString()));
			sb.append("\n");
			
			listSkeletonParts(dp.getSkeleton(), sb, "Skel part: ");
		}
	}
	
	public static String getDpInfo(DocumentPart dp, LocaleId srcLoc) {
		StringBuilder sb = new StringBuilder();
		fillSB(sb, dp, srcLoc);
		return sb.toString();
	}
}
