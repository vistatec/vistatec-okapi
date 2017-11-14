/*===========================================================================
  Copyright (C) 2016-2017 by the Okapi Framework contributors
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

package net.sf.okapi.filters.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.FileLocation;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filters.FilterTestDriver;
import net.sf.okapi.common.resource.RawDocument;

@RunWith(JUnit4.class)
public class BundledConfigsTest {

	private XMLFilter filter;
	private FileLocation root;
	private LocaleId locEN = LocaleId.fromString("en");

	@Before
	public void setUp() {
		filter = new XMLFilter();
		root = FileLocation.fromClass(this.getClass());
	}

	@Test
	public void testAndroidUntranslatable() throws Exception {
		IParameters parameters = filter.getParameters();
		parameters.load(root.in("/okf_xml@AndroidStrings.fprm").asUrl(), false);
		RawDocument rd = new RawDocument(root.in("/AndroidTest2.xml").asUri(), "UTF-8", locEN);
		List<Event> events = FilterTestDriver.getEvents(filter, rd, parameters);

		// Test file has 3 TUs but one is marked translatable=false, so make
		// sure it isn't extracted.
		assertTrue(events.get(0).isStartDocument());
		assertEquals("Hello, Android! I am a string resource!", events.get(1).getTextUnit().getSource().toString());
		assertEquals("Hello, Android", events.get(2).getTextUnit().getSource().toString());
		assertTrue(events.get(3).isEndDocument());
		assertEquals(4, events.size());
	}
}
