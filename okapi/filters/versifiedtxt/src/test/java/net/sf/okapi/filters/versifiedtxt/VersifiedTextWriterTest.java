/*===========================================================================
  Copyright (C) 2011 by the Okapi Framework contributors
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

package net.sf.okapi.filters.versifiedtxt;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.RawDocument;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class VersifiedTextWriterTest {
	private VersifiedTextFilter filter;
	private IFilterWriter writer;
		
	@Before
	public void setUp() {
		filter = new VersifiedTextFilter();
		writer = new VersifiedTextWriter();
		filter.setOptions(LocaleId.ENGLISH, LocaleId.SPANISH, "UTF-8", true);
	}
	
	@Test
	public void testBilingual() throws IOException {
		String snippet = "|bbook\n|cchapter\n|v1\nsource\n<TARGET>\ntarget\n\n|v2\nsource2\n<TARGET>\ntarget2";
		String expected = "|bbook\n|cchapter\n|v1\nsource\n<TARGET>\ntarget\n\n|v2\nsource2\n<TARGET>\ntarget2\n\n";
		String result = eventWriter(snippet);
		assertEquals(expected, result);
	}
	
	@Test
	public void testOutputSimpleBookChapterVerse() throws IOException {
		String snippet = "|bbook\n|cchapter\n|v1\nThis is a test.";
		String expected = "|bbook\n|cchapter\n|v1\nThis is a test.\n<TARGET>\n\n"; 
		String result = eventWriter(snippet);
		assertEquals(expected, result);
	}

	@Test
	public void testBilingualWithNewlinesAfterSource() throws IOException {
		String snippet2 = "|bbook\r\n|v1\r\nsource\r\n\r\n<TARGET>\r\n\r\n";
		String result = eventWriter(snippet2);
		String finalResult = eventWriter(result);
		assertEquals(result, finalResult);
		// here to catch a possible filter exception - no exception = green
	}
	
	private String eventWriter(String input) throws IOException {
		try {
			// Open the input
			filter.open(new RawDocument(input, LocaleId.ENGLISH, LocaleId.SPANISH));

			// Prepare the output
			writer.setOptions(LocaleId.SPANISH, "UTF-8");
			ByteArrayOutputStream writerBuffer = new ByteArrayOutputStream();
			writer.setOutput(writerBuffer);

			// Process the document
			Event event;
			while (filter.hasNext()) {
				event = filter.next();
				writer.handleEvent(event);
			}			
			writerBuffer.close();
			return new String(writerBuffer.toByteArray(), "UTF-8");
		} finally {
			if (filter != null)
				filter.close();
			if (writer != null)
				writer.close();
		}
	}
}
