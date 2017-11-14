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

package net.sf.okapi.filters.wiki;

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
public class WikiWriterTest {
	
	private WikiFilter filter;
		
	@Before
	public void setUp() {
		filter = new WikiFilter();
		filter.setOptions(LocaleId.ENGLISH, LocaleId.SPANISH, "UTF-8", true);
	}
	
	@Test
	public void testOutput() throws IOException {
		String snippet = "=== Headline ===\n"
				   + "Some multiline \n"
				   + "text with **decoration**.";
		String expected = "=== Headline ===\n"
						+ "Some multiline text with **decoration**.\n";
		String result = eventWriter(snippet);
		assertEquals(expected, result);
	}
	
	@Test
	public void testOutputTable() throws IOException {
		String snippet = "^ Header 1 ^ Header 2 |\n"
				   + "| Cell 1 | Cell 2 |\n"
				   + "\n"
				   + "Paragraph.";
		String expected = "^ Header 1 ^ Header 2 |\n"
						+ "| Cell 1 | Cell 2 |\n"
						+ "\n"
						+ "Paragraph.\n";
		String result = eventWriter(snippet);
		assertEquals(expected, result);
	}

	@Test
	public void testWhitespaces() throws IOException {
		String snippet = " white    space!  \n";
		String expected = " white space!  \n";
		String result = eventWriter(snippet);
		assertEquals(expected, result);
		
		filter.getParameters().fromString("{preserve_whitespace: true}");
		result = eventWriter(snippet);
		assertEquals(snippet, result);
		
		filter.getParameters().reset();
	}
	
	private String eventWriter(String input) throws IOException {
		try (IFilterWriter writer = new WikiWriter()) {
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
		}
	}
}
