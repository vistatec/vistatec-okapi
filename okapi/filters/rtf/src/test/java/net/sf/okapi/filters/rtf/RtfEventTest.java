/*===========================================================================
  Copyright (C) 2008 Jim Hargrave
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

package net.sf.okapi.filters.rtf;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filters.FilterTestDriver;
import net.sf.okapi.common.resource.StartDocument;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class RtfEventTest {

	private RTFFilter filter;
	private LocaleId locEN = LocaleId.ENGLISH;

	@Before
	public void setUp() throws Exception {
		filter = new RTFFilter();	
	}
	
	@Test
	public void testStartDoc () {
		String snippet = "{\\rtf1\\ansi\\ansicpg1252\\deff0\\deflang1033{\\fonttbl{\\f0\\fnil\\fcharset0 Courier New;}}"
			+ "\\uc1\\pard\\f0\\fs22 t\\b e\\b0 st\\par }";
		List<Event> list = getEvents(snippet);
		assertTrue(list.get(0).isStartDocument());
		StartDocument sd = list.get(0).getStartDocument();
		assertNotNull(sd.getFilterWriter());
	}
	
	private ArrayList<Event> getEvents(String snippet) {
		return FilterTestDriver.getEvents(filter,  snippet,  locEN);
	}
}
