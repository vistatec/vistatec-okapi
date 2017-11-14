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

package net.sf.okapi.steps.termextraction;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.TestUtil;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class SimpleTermExtractorTest {
	
	private SimpleTermExtractor extr = new SimpleTermExtractor();
	private Parameters params;
	
	@Before
	public void setUp() {
		params = new Parameters();
		String root = TestUtil.getParentDir(this.getClass(), "/");
		params.setOutputPath(root + "/terms.txt");
	}

	@Test
	public void testSimpleCase () {
		extr.initialize(params, LocaleId.ENGLISH, null, null);
		extr.processTextUnit(new TextUnit("id", "This is a test, a rather simple test."));
		extr.completeExtraction();
		Map<String, Integer> res = extr.getTerms();
		assertEquals("{test=2}", res.toString());
	}

	@Test
	public void testLongTextCaseWithMinOcc3 () {
		params.setMinOccurrences(3);
		extr.initialize(params, LocaleId.ENGLISH, null, null);
		extr.processTextUnit(createLongTU());
		extr.completeExtraction();
		Map<String, Integer> res = extr.getTerms();
		assertEquals("{complex=4, complex expression=3, expression=3}", res.toString());
	}

	private ITextUnit createLongTU () {
		ITextUnit tu = new TextUnit("id");
		tu.setSourceContent(new TextFragment("This is a test with a complex expression. A complex expression that "
			+ "occurs often. This is important for this test. A complex term like [complex expression] is also "
			+ "a term with several words. Things like $#@ or & should not be seen as words."
		));
		return tu;
	}
}
