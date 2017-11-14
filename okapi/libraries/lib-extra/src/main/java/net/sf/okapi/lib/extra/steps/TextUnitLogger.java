/*===========================================================================
  Copyright (C) 2010-2011 by the Okapi Framework contributors
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

package net.sf.okapi.lib.extra.steps;

import net.sf.okapi.common.ClassUtil;
import net.sf.okapi.common.Event;
import net.sf.okapi.common.ISkeleton;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.annotation.IAnnotation;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextPart;
import net.sf.okapi.common.skeleton.GenericSkeleton;
import net.sf.okapi.common.skeleton.GenericSkeletonPart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextUnitLogger extends BasePipelineStep {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private StringBuilder sb;
	private LocaleId srcLoc;
	
	@Override
	public String getName() {
		return "Text Unit Logger";
	}

	@Override
	public String getDescription() {
		return "Logs Text Unit resources going through the pipeline.";
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
		sb.append("---------------------------------\n");
		fillSB(sb, tu, srcLoc);
		return super.handleTextUnit(event);
	}
	
	@Override
	protected Event handleEndBatch(Event event) {
		logger.trace(sb.toString());
		return super.handleEndBatch(event);
	}
	
	private static void listProperties(ITextUnit tu, StringBuilder sb, String descr) {
		for (String propName : tu.getPropertyNames()) {
			sb.append("                    ");
			sb.append(descr);
			sb.append(propName);
			sb.append("=");
			sb.append(tu.getProperty(propName));
			sb.append("\n");
		}
	}
	
	private static void listProperties(TextContainer tc, StringBuilder sb, String descr) {
		for (String propName : tc.getPropertyNames()) {
			sb.append("                    ");
			sb.append(descr);
			sb.append(propName);
			sb.append("=");
			sb.append(tc.getProperty(propName));
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
	
	private static void fillSB(StringBuilder sb, ITextUnit tu, LocaleId srcLoc) {
		sb.append(String.format("tu [id=%s name=%s type=%s]", tu.getId(), tu.getName(), tu.getType()));
		sb.append(":");
		if (tu.isReferent()) sb.append(" referent");
		sb.append("\n");
		
		listProperties(tu, sb, "TU property: ");
		
		if (tu.getAnnotations() != null) {
//			sb.append("             ");
//			sb.append("Source annotations:");
//			sb.append("\n");
			for (IAnnotation annot : tu.getAnnotations()) {
				sb.append("                    ");
				sb.append(annot.getClass().getName());
				sb.append(" ");
				sb.append(annot.toString());
				sb.append("\n");
			}		
		}
		
		if (tu.getSkeleton() != null) {
			sb.append(String.format("      Skeleton: %s", tu.getSkeleton().toString()));
			sb.append("\n");
			
			listSkeletonParts(tu.getSkeleton(), sb, "Skel part: ");
		}		
		
		sb.append(String.format("      Source (%s): %s", srcLoc, tu.getSource()));
		sb.append("\n");
		
		TextContainer source = tu.getSource();
		listProperties(source, sb, "Source property: ");
		
		if (source.getAnnotations() != null) {
//			sb.append("             ");
//			sb.append("Source annotations:");
//			sb.append("\n");
			for (IAnnotation annot : source.getAnnotations()) {
				sb.append("                    ");
				sb.append(annot.getClass().getName());
				sb.append(" ");
				sb.append(annot.toString());
				sb.append("\n");
			}		
		}
		
		//ISegments segs = source.getSegments();

		int stpIndex = -1;		
		for (TextPart tp : source) {
			Segment seg = null;
			if (tp.isSegment()) {
				seg = (Segment) tp;
			}
			else
				stpIndex++;
			
			if (tp.isSegment()) {
				sb.append(String.format("         %s: %s\n            %s", seg.getId(), 
						tp.getContent().toText(), tp.getContent().toString()));
			}
			else {
				sb.append(String.format("         tp%d: %s\n            %s", stpIndex, 
						tp.getContent().toText(), tp.getContent().toString()));
			}			
			sb.append("\n");
			if (tp.getContent().getCodes() != null) {
				if (tp.isSegment()) {
					sb.append(String.format("         %s codes (%d): %s", seg.getId(), 
							tp.getContent().getCodes().size(), tp.getContent().getCodes().toString()));
				}
				else {					
					sb.append(String.format("         tp%d codes (%d): %s", stpIndex, 
							tp.getContent().getCodes().size(), tp.getContent().getCodes().toString()));
				}		
				sb.append("\n");
			}
			
			if (seg != null && seg.getAnnotations() != null) {
//				sb.append("Source annotations:");
//				sb.append("\n");
				for (IAnnotation annot : seg.getAnnotations()) {
					sb.append("                    ");
					sb.append(annot.getClass().getName());
					sb.append(" ");
					sb.append(annot.toString());
					sb.append("\n");
				}		
			}
		}
		
		for (LocaleId locId : tu.getTargetLocales()) {
			sb.append(String.format("      Target (%s): %s", locId.toString(), tu.getTarget(locId)));
			sb.append("\n");
			
			TextContainer target = tu.getTarget(locId);		
			listProperties(target, sb, "Target property: ");
			
			if (target.getAnnotations() != null) {
//				sb.append("             ");
//				sb.append("Target annotations:");
//				sb.append("\n");
				for (IAnnotation annot : target.getAnnotations()) {
					sb.append("                    ");
					sb.append(annot.getClass().getName());
					sb.append(" ");
					sb.append(annot.toString());
					sb.append("\n");
				}		
			}
			
//			segs = target.getSegments(); 
			int ttpIndex = -1;
			for (TextPart tp : target) {
				Segment seg = null;
				if (tp.isSegment()) {
					seg = (Segment) tp;
				}
				else
					ttpIndex++;
				if (tp.isSegment()) {
					sb.append(String.format("         %s: %s\n            %s", seg.getId(), 
							tp.getContent().toText(), tp.getContent().toString()));
				}
				else {
					sb.append(String.format("         tp%d: %s\n            %s", ttpIndex, 
							tp.getContent().toText(), tp.getContent().toString()));
				}				
				sb.append("\n");
				
				if (tp.getContent().getCodes() != null) {
					if (tp.isSegment()) {
						sb.append(String.format("         %s codes (%d): %s", seg.getId(), 
								tp.getContent().getCodes().size(), tp.getContent().getCodes().toString()));
					}
					else {
						sb.append(String.format("         tp%d codes (%d): %s", ttpIndex, 
								tp.getContent().getCodes().size(), tp.getContent().getCodes().toString()));
					}					
					sb.append("\n");
				}
				
				if (seg != null && seg.getAnnotations() != null) {
//					sb.append("Target annotations:");
//					sb.append("\n");
					for (IAnnotation annot : seg.getAnnotations()) {
						sb.append("                    ");
						sb.append(annot.getClass().getName());
						sb.append(" ");
						sb.append(annot.toString());
						sb.append("\n");
					}		
				}
			}
		}
	}
	
	public static String getTuInfo(ITextUnit tu, LocaleId srcLoc) {
		StringBuilder sb = new StringBuilder();
		fillSB(sb, tu, srcLoc);
		return sb.toString();
	}
}
