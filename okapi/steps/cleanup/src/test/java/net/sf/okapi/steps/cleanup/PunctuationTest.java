/*===========================================================================
  Copyright (C) 2013 by the Okapi Framework contributors
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

package net.sf.okapi.steps.cleanup;

import static org.junit.Assert.assertEquals;
import net.sf.okapi.common.IResource;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filterwriter.GenericContent;
import net.sf.okapi.common.resource.ISegments;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextPart;
import net.sf.okapi.common.resource.TextUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class PunctuationTest {

	private final LocaleId locFR = LocaleId.FRENCH;
	private final LocaleId locDE = LocaleId.GERMAN;
	
	private GenericContent fmt;
	private Cleaner cleaner;
	private Parameters params;
	
	@Before
	public void setup() {
		
		params = new Parameters();
		params.setNormalizeQuotes(false);
		fmt = new GenericContent();
		cleaner = new Cleaner(params);
	}
	
	@Test
	public void testSimpleQuotation() {

		TextFragment srcTf = new TextFragment("t1 \u201Ct2\u201D t3");
		TextFragment trgTf = new TextFragment("t1 \u00AB\u00A0t2\u00A0\u00BB t3");
		
		ITextUnit tu = new TextUnit("tu1");
		TextContainer srcTc = tu.getSource();
		srcTc.append(new Segment("seg1", srcTf));
		
		TextContainer frTc = tu.createTarget(locFR, true, IResource.CREATE_EMPTY);
		frTc.append(new Segment("seg1", trgTf));
		
		if (!tu.isEmpty()) {
			ISegments srcSegs = tu.getSourceSegments();
			for (Segment srcSeg : srcSegs) {
				Segment trgSeg = tu.getTargetSegment(locFR, srcSeg.getId(), false);
				if (trgSeg != null) {
					cleaner.normalizeQuotation(tu, srcSeg, locFR);
				}
			}
		}
		
		assertEquals("[t1 \"t2\" t3]", fmt.printSegmentedContent(tu.getSource(), true, false));
		assertEquals("[t1 \"t2\" t3]", fmt.printSegmentedContent(tu.getSource(), true, true));
		assertEquals("[t1 \"t2\" t3]", fmt.printSegmentedContent(tu.getTarget(locFR), true, false));
		assertEquals("[t1 \"t2\" t3]", fmt.printSegmentedContent(tu.getTarget(locFR), true, true));
	}
	
	@Test
	public void testUnitQuotation() {
		
		// “t1” t2 “‘t3’ t4”
		TextFragment srcTf1 = new TextFragment("\u201Ct1\u201D t2 \u201C\u2018t3\u2019 t4\u201D");
		// t1 “t2”
		TextFragment srcTf2 = new TextFragment("t1 \u201Ct2\u201D");
		// t1 ‘t2 ’ ““t3””
		TextFragment srcTf3 = new TextFragment("t1 \u2018t2 \u2019 \u201C\u201Ct3\u201D\u201D");

		// « t1 » l’t2 « t3 t4 »		
		TextFragment frTf1 = new TextFragment("\u00AB\u00A0t1\u00A0\u00BB l\u2019t2 \u00AB\u00A0t3 t4\u00A0\u00BB");
		// t1 «  t2»
		TextFragment frTf2 = new TextFragment("t1 \u00AB\u00A0 t2\u00BB");
		// t1 ‘t2’ « t3 »
		TextFragment frTf3 = new TextFragment("t1 \u2018t2\u2019 \u00AB\u00A0t3\u00A0\u00BB");
		
		ITextUnit tu = new TextUnit("tu1");
		TextContainer srcTc = tu.getSource();
		srcTc.append(new Segment("seg1", srcTf1));
		srcTc.append(new TextPart(" "));
		srcTc.append(new Segment("seg2", srcTf2));
		srcTc.append(new TextPart(" "));
		srcTc.append(new Segment("seg3", srcTf3));
		
		TextContainer frTc = tu.createTarget(locFR, true, IResource.CREATE_EMPTY);
		frTc.append(new Segment("seg1", frTf1));
		frTc.append(new TextPart(" "));
		frTc.append(new Segment("seg2", frTf2));
		frTc.append(new TextPart(" "));
		frTc.append(new Segment("seg3", frTf3));
		
		if (!tu.isEmpty()) {
			ISegments srcSegs = tu.getSourceSegments();
			for (Segment srcSeg : srcSegs) {
				Segment trgSeg = tu.getTargetSegment(locFR, srcSeg.getId(), false);
				if (trgSeg != null) {
					cleaner.normalizeQuotation(tu, srcSeg, locFR);
				}
			}
		}
		
		assertEquals("[\"t1\" t2 \"\'t3\' t4\"] [t1 \"t2\"] [t1 \'t2 \' \"\"t3\"\"]", fmt.printSegmentedContent(tu.getSource(), true, false));
		assertEquals("[\"t1\" t2 \"\'t3\' t4\"] [t1 \"t2\"] [t1 \'t2 \' \"\"t3\"\"]", fmt.printSegmentedContent(tu.getSource(), true, true));
		assertEquals("[\"t1\" l\'t2 \"t3 t4\"] [t1 \"t2\"] [t1 \'t2\' \"t3\"]", fmt.printSegmentedContent(tu.getTarget(locFR), true, false));
		assertEquals("[\"t1\" l\'t2 \"t3 t4\"] [t1 \"t2\"] [t1 \'t2\' \"t3\"]", fmt.printSegmentedContent(tu.getTarget(locFR), true, true));
	}

//	@Test
//	public void testSimplePunctuation() {
//		
//		TextFragment srcTf = new TextFragment("t1, 0 . 235:t2, t3, t4 ; t5 .");
//		TextFragment trgTf = new TextFragment("t1 ,235 : t2 ,t3 , t4 ;t5 . ");
//		
//		ITextUnit tu = new TextUnit("tu1");
//		TextContainer srcTc = tu.getSource();
//		srcTc.append(new Segment("seg1", srcTf));
//		
//		TextContainer trgTc = tu.createTarget(locFR, true, IResource.CREATE_EMPTY);
//		trgTc.append(new Segment("seg1", trgTf));
//		
//		if (!tu.isEmpty()) {
//			ISegments srcSegs = tu.getSourceSegments();
//			for (Segment srcSeg : srcSegs) {
//				Segment trgSeg = tu.getTargetSegment(locFR, srcSeg.getId(), false);
//				if (trgSeg != null) {
//					cleaner.normalizeMarks(tu, srcSeg, locFR);
//				}
//			}
//		}		
//		
//		assertEquals("[t1, 0.235: t2, t3, t4; t5.]", fmt.printSegmentedContent(tu.getSource(), true, false));
//		assertEquals("[t1, 0.235: t2, t3, t4; t5.]", fmt.printSegmentedContent(tu.getSource(), true, true));
//	}
	
//	@Test
//	public void testSimplePunctuationNearQuote() {
//		
//		TextFragment srcTf = new TextFragment("\u201Ct1 . \u201D . 235:t2\" . t3, t4 ; t5\" .");
//		TextFragment trgTf = new TextFragment("« t1 », ,235 : t2 ,t3 , t4 ;t5 . ");
//		
//		ITextUnit tu = new TextUnit("tu1");
//		TextContainer srcTc = tu.getSource();
//		srcTc.append(new Segment("seg1", srcTf));
//		
//		TextContainer trgTc = tu.createTarget(locFR, true, IResource.CREATE_EMPTY);
//		trgTc.append(new Segment("seg1", trgTf));
//		
//		if (!tu.isEmpty()) {
//			ISegments srcSegs = tu.getSourceSegments();
//			for (Segment srcSeg : srcSegs) {
//				Segment trgSeg = tu.getTargetSegment(locFR, srcSeg.getId(), false);
//				if (trgSeg != null) {
//					cleaner.normalizeMarks(tu, srcSeg, locFR);			
//				}
//			}
//		}		
//		
//		assertEquals("[\u201Ct1.\u201D .235: t2.\" t3, t4; t5.\"]", fmt.printSegmentedContent(tu.getSource(), true, false));
//		assertEquals("[\u201Ct1.\u201D .235: t2.\" t3, t4; t5.\"]", fmt.printSegmentedContent(tu.getSource(), true, true));
//	}
}
