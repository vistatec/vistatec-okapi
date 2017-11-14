/*===========================================================================
  Copyright (C) 2009 by the Okapi Framework contributors
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

package net.sf.okapi.common.filterwriter;

import static org.junit.Assert.assertEquals;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TMXContentTest {

	private TMXContent fmt;

	@Before
	public void setUp() throws Exception {
		fmt = new TMXContent();
	}
	
	@Test
	public void testSimple_Default () {
		TextFragment tf = createTextUnit();
		assertEquals(5, tf.getCodes().size());
		assertEquals("t1<bpt i=\"1\">&lt;b1&gt;</bpt><bpt i=\"2\">&lt;b2&gt;</bpt><ph x=\"3\">{\\x1\\}</ph>t2<ept i=\"2\">&lt;/b2&gt;</ept><ept i=\"1\">&lt;/b1&gt;</ept>t3",
			fmt.setContent(tf).toString());
	}
	
	@Test
	public void testSimple_OmegaT () {
		TextFragment tf = createTextUnit();
		assertEquals(5, tf.getCodes().size());
		fmt.setLetterCodedMode(true, true);
		fmt.setContent(tf);
		assertEquals("t1<bpt i=\"1\">&lt;g0&gt;</bpt><bpt i=\"2\">&lt;g1&gt;</bpt><ph x=\"3\">&lt;x2/&gt;</ph>t2<ept i=\"2\">&lt;/g1&gt;</ept><ept i=\"1\">&lt;/g0&gt;</ept>t3",
			fmt.toString());
	}

	@Test
	public void testSimple_OmegaTUsingCodeMode () {
		TextFragment tf = createTextUnit();
		assertEquals(5, tf.getCodes().size());
		fmt.setCodeMode(TMXContent.CODEMODE_LETTERCODED);
		fmt.setContent(tf);
		assertEquals("t1<bpt i=\"1\">&lt;g1&gt;</bpt><bpt i=\"2\">&lt;g2&gt;</bpt><ph x=\"3\">&lt;x3/&gt;</ph>t2<ept i=\"2\">&lt;/g2&gt;</ept><ept i=\"1\">&lt;/g1&gt;</ept>t3",
			fmt.toString());
	}

	@Test
	public void testSimple_EmptyCodes () {
		TextFragment tf = createTextUnit();
		assertEquals(5, tf.getCodes().size());
		fmt.setCodeMode(TMXContent.CODEMODE_EMPTY);
		fmt.setContent(tf);
		assertEquals("t1<bpt i=\"1\"></bpt><bpt i=\"2\"></bpt><ph x=\"3\"></ph>t2<ept i=\"2\"></ept><ept i=\"1\"></ept>t3",
			fmt.toString());
	}

	@Test
	public void testSimple_GenericCodes () {
		TextFragment tf = createTextUnit();
		assertEquals(5, tf.getCodes().size());
		fmt.setCodeMode(TMXContent.CODEMODE_GENERIC);
		fmt.setContent(tf);
		assertEquals("t1<bpt i=\"1\">&lt;1&gt;</bpt><bpt i=\"2\">&lt;2&gt;</bpt><ph x=\"3\">&lt;3/&gt;</ph>t2<ept i=\"2\">&lt;/2&gt;</ept><ept i=\"1\">&lt;/1&gt;</ept>t3",
				fmt.toString());
	}

	@Test
	public void testSimple_OriginalCodes () {
		TextFragment tf = createTextUnit();
		assertEquals(5, tf.getCodes().size());
		fmt.setCodeMode(TMXContent.CODEMODE_ORIGINAL);
		fmt.setContent(tf);
		// Same as default
		assertEquals("t1<bpt i=\"1\">&lt;b1&gt;</bpt><bpt i=\"2\">&lt;b2&gt;</bpt><ph x=\"3\">{\\x1\\}</ph>t2<ept i=\"2\">&lt;/b2&gt;</ept><ept i=\"1\">&lt;/b1&gt;</ept>t3",
			fmt.setContent(tf).toString());
	}

	@Test
	public void testSimple_Trados () {
		TextFragment tf = createTextUnit();
		assertEquals(5, tf.getCodes().size());
		fmt.setTradosWorkarounds(true);
		fmt.setContent(tf);
		assertEquals("t1<bpt i=\"1\">&lt;b1&gt;</bpt><bpt i=\"2\">&lt;b2&gt;</bpt><ut>{\\cs6\\f1\\cf6\\lang1024 </ut>{\\x1\\}<ut>}</ut>t2<ept i=\"2\">&lt;/b2&gt;</ept><ept i=\"1\">&lt;/b1&gt;</ept>t3",
			fmt.toString());
	}
	
	private TextFragment createTextUnit () {
		TextFragment tf = new TextFragment();
		tf.append("t1");
		tf.append(TagType.OPENING, "", "<b1>");
		tf.append(TagType.OPENING, "", "<b2>");
		tf.append(TagType.PLACEHOLDER, "", "{\\x1\\}");
		tf.append("t2");
		tf.append(TagType.CLOSING, "", "</b2>");
		tf.append(TagType.CLOSING, "", "</b1>");
		tf.append("t3");
		return tf;
	}
	
}
