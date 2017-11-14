package net.sf.okapi.lib.extra.steps;

import net.sf.okapi.common.ClassUtil;
import net.sf.okapi.common.Event;
import net.sf.okapi.common.ISkeleton;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.annotation.IAnnotation;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.StartGroup;
import net.sf.okapi.common.skeleton.GenericSkeleton;
import net.sf.okapi.common.skeleton.GenericSkeletonPart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GroupLogger extends BasePipelineStep {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private StringBuilder sb;
	private LocaleId srcLoc;
	
	@Override
	public String getName() {
		return "Group Logger";
	}

	@Override
	public String getDescription() {
		return "Logs Start/End Group resources going through the pipeline.";
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
	protected Event handleStartGroup(Event event) {
		StartGroup sg = event.getStartGroup();
		fillSB(sb, sg, srcLoc);
		return super.handleDocumentPart(event);
	}
	
	@Override
	protected Event handleEndBatch(Event event) {
		logger.trace(sb.toString());
		return super.handleEndBatch(event);
	}
	
	private static void listSkeletonParts(ISkeleton skel, StringBuilder sb, String descr) {
		if (skel instanceof GenericSkeleton) {
			for (GenericSkeletonPart part : ((GenericSkeleton) skel).getParts()) {
				sb.append("          ");
				sb.append(descr);
				sb.append(String.format("loc=%s, parent=%s [%s]",
						part.getLocale() != null ? part.getLocale().toString() : "null", 
						part.getParent() != null ? part.getParent().getId() : "null",
						part.getParent() != null ? ClassUtil.getShortClassName(part.getParent().getClass()) : ""
						));
				sb.append("\n");
			}
		}
	}
	
	private static void fillSB(StringBuilder sb, StartGroup sg, LocaleId srcLoc) {
		sb.append(String.format("sg [id=%s name=%s]", sg.getId(), sg.getName()));		
		sb.append(":");
		if (sg.isReferent()) sb.append(" referent");
		sb.append("\n");
		
		if (sg.getAnnotations() != null) {
//			sb.append("             ");
//			sb.append("Source annotations:");
//			sb.append("\n");
			for (IAnnotation annot : sg.getAnnotations()) {
				sb.append("                    ");
				sb.append(annot.getClass().getName());
				sb.append(" ");
				sb.append(annot.toString());
				sb.append("\n");
			}		
		}
		
		if (sg.getPropertyNames() != null && sg.getPropertyNames().size() > 0) {
			sb.append("             ");
			sb.append("Resource properties:");
			sb.append("\n");
			for (String name : sg.getPropertyNames()) {
				sb.append("                    ");
				sb.append(name);
				sb.append("=");
				sb.append(sg.getProperty(name).toString());
				sb.append("\n");
			}		
		}
		
		if (sg.getSourcePropertyNames() != null && sg.getSourcePropertyNames().size() > 0) {
			sb.append("             ");
			sb.append("Source properties:");
			sb.append("\n");
			
			for (String name : sg.getSourcePropertyNames()) {
				sb.append("                    ");
				sb.append(name);
				sb.append("=");
				sb.append(sg.getSourceProperty(name).toString());
				sb.append("\n");
			}		
		}
				
		for (LocaleId locId : sg.getTargetLocales()) {
			if (sg.getTargetPropertyNames(locId) != null && sg.getTargetPropertyNames(locId).size() > 0) {
				sb.append("             ");
				sb.append("Target properties:");
				sb.append("\n");
				
				for (String name : sg.getTargetPropertyNames(locId)) {
					sb.append("                    ");
					sb.append(name);
					sb.append("=");
					sb.append(sg.getTargetProperty(locId, name).toString());
					sb.append("\n");
				}		
			}
		}		
		
		sb.append("\n");
		
		if (sg.getSkeleton() != null) {
			sb.append(String.format("      Skeleton: %s", sg.getSkeleton().toString()));
			sb.append("\n");
			listSkeletonParts(sg.getSkeleton(), sb, "Skel part: ");
		}		
	}
	
	private static void fillSB2(StringBuilder sb, Ending eg, LocaleId srcLoc) {
		sb.append(String.format("eg [id=%s]", eg.getId()));		
		sb.append("\n");
		
		if (eg.getAnnotations() != null) {
//			sb.append("             ");
//			sb.append("Source annotations:");
//			sb.append("\n");
			for (IAnnotation annot : eg.getAnnotations()) {
				sb.append("                    ");
				sb.append(annot.getClass().getName());
				sb.append(" ");
				sb.append(annot.toString());
				sb.append("\n");
			}		
		}		
		
		if (eg.getSkeleton() != null) {
			sb.append(String.format("      Skeleton: %s", eg.getSkeleton().toString()));
			sb.append("\n");
		}
	}
	
	public static String getSgInfo(StartGroup sg, LocaleId srcLoc) {
		StringBuilder sb = new StringBuilder();
		fillSB(sb, sg, srcLoc);
		return sb.toString();
	}
	
	public static String getEgInfo(Ending eg, LocaleId srcLoc) {
		StringBuilder sb = new StringBuilder();
		fillSB2(sb, eg, srcLoc);
		sb.append("\n");
		return sb.toString();
	}
}
