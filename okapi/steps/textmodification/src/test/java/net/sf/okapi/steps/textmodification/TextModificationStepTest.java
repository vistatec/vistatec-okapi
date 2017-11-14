/*===========================================================================
  Copyright (C) 2009-2011 by the Okapi Framework contributors
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

package net.sf.okapi.steps.textmodification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.steps.tests.StepTestDriver;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TextModificationStepTest {

	private StepTestDriver driver;
	private LocaleId locEN = LocaleId.fromString("EN");
	private LocaleId locFR = LocaleId.fromString("Fr");

	//TODO: Fix those test using a dummy filter, when we have stable step/pipeline architecture.
	
	@Before
	public void setUp() {
		driver = new StepTestDriver();
	}

	@Test
	public void testTargetDefaults () {
		String original = "This is the content #1 with %s.";
		driver.prepareFilterEventsStep(original, original, locEN, locFR);
		TextModificationStep step = new TextModificationStep(); // Defaults
		Parameters params = (Parameters)step.getParameters();
		params.setType(Parameters.TYPE_XNREPLACE);
		driver.testFilterEventsStep(step);
		ITextUnit res = driver.getResult();
		assertNotNull(res);
		assertTrue(res.hasTarget(locFR));
		assertEquals(original, res.getTarget(locFR).toString());
	}

	@Test
	public void testDefaults () {
		String original = "This is the content.";
		driver.prepareFilterEventsStep(original, null, locEN, locFR);
		TextModificationStep step = new TextModificationStep(); // Defaults
		driver.testFilterEventsStep(step);
		ITextUnit res = driver.getResult();
		assertNotNull(res);
		assertTrue(res.hasTarget(locFR));
		assertEquals(original, res.getTarget(locFR).toString());
	}

	@Test
	public void testWithPrefixSuffixMarkers () {
		String original = "This is the content.";
		String expected = "{_[This is the content.]_id1_}";
		driver.prepareFilterEventsStep(original, null, locEN, locFR);
		TextModificationStep step = new TextModificationStep();
		Parameters params = (Parameters)step.getParameters();
		params.setAddPrefix(true);
		params.setPrefix("{_");
		params.setAddSuffix(true);
		params.setSuffix("_}");
		params.setMarkSegments(true);
		params.setAddID(true);
		driver.testFilterEventsStep(step);
		ITextUnit res = driver.getResult();
		assertNotNull(res);
		assertTrue(res.hasTarget(locFR));
		assertEquals(expected, res.getTarget(locFR).toString());
	}

	@Test
	public void testWithXandNs () {
		String original = "This is the content #1 with %s.";
		String expected = "Xxxx xx xxx xxxxxxx #N xxxx %x.";
		driver.prepareFilterEventsStep(original, null, locEN, locFR);
		TextModificationStep step = new TextModificationStep();
		Parameters params = (Parameters)step.getParameters();
		params.setType(Parameters.TYPE_XNREPLACE);
		driver.testFilterEventsStep(step);
		ITextUnit res = driver.getResult();
		assertNotNull(res);
		assertTrue(res.hasTarget(locFR));
		assertEquals(expected, res.getTarget(locFR).toString());
	}

	@Test
	public void testWithPseudoTrans () {
		// "A     a     E     e     I     i     O     o     U     u     Y     y     C     c     D     d     N     n";
		// "\u00c2\u00e5\u00c9\u00e8\u00cf\u00ec\u00d8\u00f5\u00db\u00fc\u00dd\u00ff\u00c7\u00e7\u00d0\u00f0\u00d1\u00f1";
		String original = "This is the content #1 with %s.";
		String expected = "Th\u00ecs \u00ecs th\u00e8 \u00e7\u00f5\u00f1t\u00e8\u00f1t #1 w\u00ecth %s.";
		driver.prepareFilterEventsStep(original, null, locEN, locFR);
		TextModificationStep step = new TextModificationStep();
		Parameters params = (Parameters)step.getParameters();
		params.setType(Parameters.TYPE_EXTREPLACE);
		driver.testFilterEventsStep(step);
		ITextUnit res = driver.getResult();
		assertNotNull(res);
		assertTrue(res.hasTarget(locFR));
		assertEquals(expected, res.getTarget(locFR).toString());
	}

	@Test
	public void testKeepInlineCodes () {
		String original = "This is the content #1 with '@#$0'.";
		String expected = "@#$0";
		driver.prepareFilterEventsStep(original, null, locEN, locFR);
		TextModificationStep step = new TextModificationStep();
		Parameters params = (Parameters)step.getParameters();
		params.setType(Parameters.TYPE_KEEPINLINE);
		driver.testFilterEventsStep(step);
		ITextUnit res = driver.getResult();
		assertNotNull(res);
		assertTrue(res.hasTarget(locFR));
		assertEquals(expected, res.getTarget(locFR).toString());
	}

	@Test
	public void testExpansion () {
		TextModificationStep step = new TextModificationStep(); // Defaults
		Parameters params = (Parameters)step.getParameters();
		params.setType(Parameters.TYPE_KEEPORIGINAL);
		params.setExpand(true); // Overwrite existing target

		String original = "Original.";
		String expected = "Original. zzzz";
		driver.prepareFilterEventsStep(original, null, locEN, locFR);
		driver.testFilterEventsStep(step);
		ITextUnit res = driver.getResult();
		assertEquals(expected, res.getTarget(locFR).toString());

		original = "O";
		expected = "Oz";
		driver.prepareFilterEventsStep(original, null, locEN, locFR);
		driver.testFilterEventsStep(step);
		res = driver.getResult();
		assertEquals(expected, res.getTarget(locFR).toString());

		original = "";
		expected = "";
		driver.prepareFilterEventsStep(original, null, locEN, locFR);
		driver.testFilterEventsStep(step);
		res = driver.getResult();
		assertEquals(expected, res.getTarget(locFR).toString());

		original = "This is a longer text with a lot more words and characters.";
		expected = "This is a longer text with a lot more words and characters. zzzzz zzzzz zzzzz zzzzz zzzzz zzzzz zzzzz zzzzz zzzzz zzzz";
		driver.prepareFilterEventsStep(original, null, locEN, locFR);
		driver.testFilterEventsStep(step);
		res = driver.getResult();
		assertEquals(expected, res.getTarget(locFR).toString());
	}

	@Test
	public void testTargetOverwriting () {
		String original = "This is the content #1 with @#$0.";
		String expected = "Xxxx xx xxx xxxxxxx #N xxxx @#$0.";
		driver.prepareFilterEventsStep(original, original, locEN, locFR);
		TextModificationStep step = new TextModificationStep(); // Defaults
		Parameters params = (Parameters)step.getParameters();
		params.setType(Parameters.TYPE_XNREPLACE);
		params.setApplyToExistingTarget(true); // Overwrite existing target
		driver.testFilterEventsStep(step);
		ITextUnit res = driver.getResult();
		assertNotNull(res);
		assertTrue(res.hasTarget(locFR));
		assertEquals(expected, res.getTarget(locFR).toString());
	}

}
