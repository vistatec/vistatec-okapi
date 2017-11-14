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

package net.sf.okapi.filters.doxygen;

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
public class DoxygenWriterTest {
	private DoxygenFilter filter;
	private IFilterWriter writer;
		
	@Before
	public void setUp() {
		filter = new DoxygenFilter();
		writer = new DoxygenWriter();
		filter.setOptions(LocaleId.ENGLISH, LocaleId.SPANISH, "UTF-8", true);
	}
	
	@Test
	public void testOutputMultilineComment() throws IOException {
		String snippet = "foo foo foo /// This is \n"
				   + "bar bar bar /// a test.\n"
				   + "baz baz baz /// ";
		String expected = "foo foo foo /// This is a test.\n"
						+ "bar bar bar /// \n"
						+ "baz baz baz ///\n";
		String result = eventWriter(snippet);
		assertEquals(expected, result);
	}
	
	@Test
	public void testOutputJavadocComment() throws IOException {
		String snippet = "/**\n"
				   + " * This is \n"
				   + " * a test.\n"
				   + " */\n"
				   + "baz baz baz";
		String expected = "/**\n"
						+ " * This is a test.\n"
						+ " * \n"
						+ " */\n"
						+ "baz baz baz\n";
		String result = eventWriter(snippet);
		assertEquals(expected, result);
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
