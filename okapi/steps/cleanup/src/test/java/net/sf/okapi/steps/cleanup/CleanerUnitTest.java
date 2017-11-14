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

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filterwriter.GenericContent;

import org.junit.Before;

public class CleanerUnitTest {

	private final LocaleId locFR = LocaleId.FRENCH;
	private final LocaleId locDE = LocaleId.GERMAN;

	private GenericContent fmt;
	private Cleaner cleaner;

	@Before
	public void setup() {

		fmt = new GenericContent();
		cleaner = new Cleaner();
	}
	
//	@Test
//	public void testCleanUnit() {
//
//		// “t1, ” t2 ,“‘t3 , ’ t4 :”
//		TextFragment srcTf1 = new TextFragment("\u201Ct1\u002C \u201D t2 \u002C\u201C\u2018t3 \u002C\u2019 t4 \u003A\u201D");
//		// t1 “t2” ; . 352
//		TextFragment srcTf2 = new TextFragment("t1 \u201Ct2\u201D \u003B \u002E 352");
//		// t1 ‘t2 ’ ““t3 ! ””
//		TextFragment srcTf3 = new TextFragment("t1 \u2018t2 \u2019 \u201C\u201Ct3 \u0021 \u201D\u201D");
//
//		// « t1 » ,l’t2,« t3 , t4 » :
//		TextFragment frTf1 = new TextFragment("\u00AB\u00A0t1\u00A0\u00BB \u002Cl\u2019t2\u002C\u00AB\u00A0t3 \u002C t4\u00A0\u00BB \u003A");
//		// t1 «  t2» ;0, 352
//		TextFragment frTf2 = new TextFragment("t1 \u00AB\u00A0 t2\u00BB\u003B0\u002C 352");
//		// t1 ‘t2’ « t3 ! »
//		TextFragment frTf3 = new TextFragment("t1 \u2018t2\u2019 \u00AB\u00A0t3\u00A0\u0021\u00A0\u00BB");
//
//		ITextUnit tu = new TextUnit("tu1");
//		TextContainer srcTc = tu.getSource();
//		srcTc.append(new Segment("seg1", srcTf1));
//		srcTc.append(new TextPart(" "));
//		srcTc.append(new Segment("seg2", srcTf2));
//		srcTc.append(new TextPart(" "));
//		srcTc.append(new Segment("seg3", srcTf3));
//
//		TextContainer frTc = tu.createTarget(locFR, true, IResource.CREATE_EMPTY);
//		frTc.append(new Segment("seg1", frTf1));
//		frTc.append(new TextPart(" "));
//		frTc.append(new Segment("seg2", frTf2));
//		frTc.append(new TextPart(" "));
//		frTc.append(new Segment("seg3", frTf3));
//
//		if (!tu.isEmpty()) {
//			ISegments srcSegs = tu.getSourceSegments();
//			for (Segment srcSeg : srcSegs) {
//				Segment trgSeg = tu.getTargetSegment(locFR, srcSeg.getId(), false);
//				if (trgSeg != null) {
//					cleaner.normalizeQuotation(srcSeg.text, trgSeg.text);
//					cleaner.normalizePunctuation(srcSeg.text, trgSeg.text);
//					cleaner.normalizeMarks(tu, srcSeg, locFR, false);
//				}
//			}
//		}
//
//		assertEquals("[\"t1,\" t2, \"\'t3,\' t4:\"]", fmt.printSegmentedContent(tu.getSource(), true, false));
//		assertEquals("[\"t1,\" t2, \"\'t3,\' t4:\"] [t1 \"t2\"; .352] [t1 \'t2 \' \"\"t3!\"\"]", fmt.printSegmentedContent(tu.getSource(), true, false));
//		assertEquals("[\"t1,\" t2, \"\'t3,\' t4:\"] [t1 \"t2\"; .352] [t1 \'t2 \' \"\"t3!\"\"]", fmt.printSegmentedContent(tu.getSource(), true, true));
//		assertEquals("[\"t1\", l\'t2, \"t3, t4\":] [t1 \"t2\"; 0,352] [t1 \'t2\' \"t3!\"]", fmt.printSegmentedContent(tu.getTarget(locFR), true, false));
//		assertEquals("[\"t1\", l\'t2, \"t3, t4\":] [t1 \"t2\"; 0,352] [t1 \'t2\' \"t3!\"]", fmt.printSegmentedContent(tu.getTarget(locFR), true, true));
//	}
}
