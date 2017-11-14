package net.sf.okapi.steps.paraaligner;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.ISegments;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.TextContainer;

class AlignedParagraphs {
	private List<List<ITextUnit>> sourceParas;
	private List<List<ITextUnit>> targetParas;
	private List<ITextUnit> alignedParas;
	private LocaleId targetLocale;

	public AlignedParagraphs(LocaleId targetLocale) {
		this.sourceParas = new LinkedList<List<ITextUnit>>();
		this.targetParas = new LinkedList<List<ITextUnit>>();
		this.alignedParas = new LinkedList<ITextUnit>();
		this.targetLocale = targetLocale;
	}

	public void addAlignment(ITextUnit srcTu, ITextUnit trgTu) {
		List<ITextUnit> srcParas = new LinkedList<ITextUnit>();
		if (srcTu != null) {
			srcParas.add(srcTu);
		}
		List<ITextUnit> trgParas = new LinkedList<ITextUnit>();
		if (trgTu != null) {
			trgParas.add(trgTu);
		}
		sourceParas.add(srcParas);
		targetParas.add(trgParas);
	}

	public void addAlignment(List<ITextUnit> srcTus, List<ITextUnit> trgTus) {
		sourceParas.add(srcTus);
		targetParas.add(trgTus);
	}

	public List<ITextUnit> align() {
		// source and target lists are guaranteed to have the same number of elements, though some will be null
		for (List<ITextUnit> stus : sourceParas) {
			ITextUnit stu = null;
			List<ITextUnit> ttus = targetParas.remove(0);
			if (stus != null) {
				stu = combineTextUnits(stus);
				if (ttus != null) {
					stu = addTargetTextUnitAsTarget(stu, combineTextUnits(ttus), targetLocale);
				}
			} else {
				if (ttus != null) {
					// source is null, use target tu (target content is source in this TU)
					ITextUnit tempTarget = combineTextUnits(ttus);
					tempTarget.createTarget(targetLocale, true, ITextUnit.COPY_ALL);	
					// remove the source
					tempTarget.setSource(null);
					stu = tempTarget;
				}
			}
			alignedParas.add(stu);
		}
		
		return alignedParas;
	}

	private ITextUnit combineTextUnits(List<ITextUnit> tus) {		
		Iterator<ITextUnit> its = tus.iterator();
		ITextUnit tuNew = its.next().clone();
		TextContainer tcAligned = tuNew.getSource();
		while (its.hasNext()) {
			ITextUnit tu2combin = its.next();
			TextContainer tcSource = tu2combin.getSource();
			tcSource.joinAll(); // joins all data parts and segments into one
			ISegments segs2add = tcSource.getSegments();
			Iterator<Segment> itSeg = segs2add.iterator();
			while (itSeg.hasNext()) {
				String cLast;
				String sCurNoTrim = tcAligned.toString();
				String sCur = sCurNoTrim.trim();
				int lonny = sCur.length();
				if (lonny > 0) {
					cLast = sCur.substring(lonny - 1);
					if (sCur.equals(sCurNoTrim)) {
						if (cLast.equals("!") || cLast.equals(".") || cLast.equals("?")) {
							// append two spaces so previous punc will be a break point
							tcAligned.append(" "); 
						}
							
						else {
							// didn't end with whitespace, so force a sentence break
							tcAligned.append(". "); 
						}
					}
				}
				
				// this combines content from next text unit
				tcAligned.append(itSeg.next().getContent()); 
			}
		}
		tcAligned.joinAll(); // join new parts together as one
		return tuNew;
	}

	private ITextUnit addTargetTextUnitAsTarget(ITextUnit tuSource, ITextUnit tuTarget, LocaleId targetLocale) {
		tuSource.setTarget(targetLocale, tuTarget.getSource());
		return tuSource;
	}
}
