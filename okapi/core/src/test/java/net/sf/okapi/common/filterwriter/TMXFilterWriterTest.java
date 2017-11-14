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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filters.DummyFilter;
import net.sf.okapi.common.filters.FilterTestDriver;
import net.sf.okapi.common.resource.RawDocument;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TMXFilterWriterTest {
	
	private DummyFilter filter;
	private LocaleId locEN = LocaleId.fromString("en");
	private LocaleId locFR = LocaleId.fromString("fr");

	@Before
	public void setUp() {
		filter = new DummyFilter();
	}

	@Test
	public void testSimpleOutput () {
		String result = rewrite(FilterTestDriver.getEvents(filter, "##def##", locEN, locFR), locFR);
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<tmx version=\"1.4\"><header creationtool=\"unknown\" creationtoolversion=\"unknown\" segtype=\"paragraph\" o-tmf=\"unknown\" adminlang=\"en\" srclang=\"en\" datatype=\"text\"></header><body>"
			+ "<tu tuid=\"autoID1\">"
			+ "<tuv xml:lang=\"en\"><seg>Source text</seg></tuv>"
			+ "<tuv xml:lang=\"fr\"><seg>Target text</seg></tuv>"
			+ "</tu>"
			+ "<tu tuid=\"autoID2\">"
			+ "<tuv xml:lang=\"en\"><seg>Source text 2</seg></tuv>"
			+ "</tu>"
			+ "</body>"
			+ "</tmx>";
		assertEquals(expected, result.replaceAll("[\\r\\n]", ""));
	}
		
	@Test
	public void testSegmentedOutput () {
		String result = rewrite(FilterTestDriver.getEvents(filter, "##seg##", locEN, locFR), locFR);
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<tmx version=\"1.4\"><header creationtool=\"unknown\" creationtoolversion=\"unknown\" segtype=\"paragraph\" o-tmf=\"unknown\" adminlang=\"en\" srclang=\"en\" datatype=\"text\"></header><body>"
			+ "<tu tuid=\"autoID1_0\">"
			+ "<tuv xml:lang=\"en\"><seg>First segment for SRC.</seg></tuv>"
			+ "<tuv xml:lang=\"fr\"><seg>First segment for TRG.</seg></tuv>"
			+ "</tu>"
			+ "<tu tuid=\"autoID1_1\">"
			+ "<tuv xml:lang=\"en\"><seg>Second segment for SRC</seg></tuv>"
			+ "<tuv xml:lang=\"fr\"><seg>Second segment for TRG</seg></tuv>"
			+ "</tu>"
			+ "</body>"
			+ "</tmx>";
		assertEquals(expected, result.replaceAll("[\\r\\n]", ""));
	}

	private String rewrite (ArrayList<Event> list,
		LocaleId trgLang)
	{
		TMXFilterWriter writer = new TMXFilterWriter();
		writer.setOptions(trgLang, null);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		writer.setOutput(output);
		for (Event event : list) {
			writer.handleEvent(event);
		}
		writer.close();
		return output.toString();
	}

}
