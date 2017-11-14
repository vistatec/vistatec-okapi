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

import java.util.regex.PatternSyntaxException;

import net.sf.okapi.common.IResource;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filterwriter.GenericContent;
import net.sf.okapi.common.resource.ISegments;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.rules.ExpectedException;

@RunWith(JUnit4.class)
public class RegexTest {

	private final LocaleId locFR = LocaleId.FRENCH;
	private final LocaleId locDE = LocaleId.GERMAN;

	private GenericContent fmt;
	private Cleaner cleaner;
	private Parameters params;

	@Before
	public void setup() {

		params = new Parameters();
		params.setMatchUserRegex(true);
		fmt = new GenericContent();
		cleaner = new Cleaner(params);
	}
	
	@Test
	public void testSimpleUserRegex() {
		
		TextFragment srcTf = new TextFragment("t1 &amp;gt; t2.");
		TextFragment trgTf = new TextFragment("t1 &gt; t2. ");
		
		ITextUnit tu = new TextUnit("tu1");
		TextContainer srcTc = tu.getSource();
		srcTc.append(new Segment("seg1", srcTf));
		
		TextContainer trgTc = tu.createTarget(locFR, true, IResource.CREATE_EMPTY);
		trgTc.append(new Segment("seg1", trgTf));
		
		params.setUserRegex("(?:(&(?:amp;)?[a-zA-Z]{2,4};))");
		
		if (!tu.isEmpty()) {
			ISegments srcSegs = tu.getSourceSegments();
			for (Segment srcSeg : srcSegs) {
				Segment trgSeg = tu.getTargetSegment(locFR, srcSeg.getId(), false);
				if (trgSeg != null) {
					cleaner.matchRegexExpressions(tu, srcSeg, locFR);			
				}
			}
		}		
		
		assertEquals("[t1 &amp;gt; t2.]", fmt.printSegmentedContent(tu.getSource(), true, false));
		assertEquals("[t1 &amp;gt; t2.]", fmt.printSegmentedContent(tu.getSource(), true, true));
		assertEquals("[]", fmt.printSegmentedContent(tu.getTarget(locFR), true, false));
		assertEquals("[]", fmt.printSegmentedContent(tu.getTarget(locFR), true, true));
	}
	
	@Test
	public void testSimpleFailUserRegex() {
		
		ExpectedException thrown = ExpectedException.none();
		
		TextFragment srcTf = new TextFragment("t1 &amp;gt; t2.");
		TextFragment trgTf = new TextFragment("t1 &gt; t2. ");
		
		ITextUnit tu = new TextUnit("tu1");
		TextContainer srcTc = tu.getSource();
		srcTc.append(new Segment("seg1", srcTf));
		
		TextContainer trgTc = tu.createTarget(locFR, true, IResource.CREATE_EMPTY);
		trgTc.append(new Segment("seg1", trgTf));
		
		params.setUserRegex("(?:(&(?:amp;)?[a-zA-Z}{2,4};))");
		
		if (!tu.isEmpty()) {
			ISegments srcSegs = tu.getSourceSegments();
			for (Segment srcSeg : srcSegs) {
				Segment trgSeg = tu.getTargetSegment(locFR, srcSeg.getId(), false);
				if (trgSeg != null) {
					cleaner.matchRegexExpressions(tu, srcSeg, locFR);			
				}
			}
		}		
		
		thrown.expect(PatternSyntaxException.class);
		thrown.expectMessage("The following error occured");
	}
	
}
