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

package net.sf.okapi.lib.tkit.roundtrip;

import static net.sf.okapi.lib.tkit.roundtrip.RoundTripUtils.roundTripSerilaizedEvents;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.TestUtil;
import net.sf.okapi.common.resource.DocumentPart;
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.StartGroup;
import net.sf.okapi.filters.openxml.ConditionalParameters;
import net.sf.okapi.filters.openxml.OpenXMLContentFilter;
import net.sf.okapi.filters.openxml.OpenXMLContentSkeletonWriter;
import net.sf.okapi.filters.openxml.ParseType;
import net.sf.okapi.filters.openxml.XMLFactoriesForTest;
import static net.sf.okapi.filters.openxml.ParseType.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a test that tests OpenXMLContentFilter for short spans of tags.
 */

@RunWith(JUnit4.class)
public class RoundtripOpenXMLSnippetsTest {
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	private OpenXMLContentFilter openXMLContentFilter;
	private String snappet;
	private LocaleId locENUS = LocaleId.fromString("en-us");
	
	@Before
	public void setUp()  {
		openXMLContentFilter = new OpenXMLContentFilter(new ConditionalParameters(), "/dummy.xml");
		RoundTripUtils.path = TestUtil.getParentDir(this.getClass(), "/dummy.txt")+"test_openxml.json";
	}

	@After
	public void tearDown() {
	}

	@Test
	public void testInlineTranslatable() {
		String snippet = "<w:p><wp:docPr id=\"2\" name=\"Picture 1\"></w:p>";
		snappet = generateOutput(roundTripSerilaizedEvents(getEvents(snippet, MSWORD)), snippet);
		assertEquals(snappet, snippet);
	}

	@Test
	public void testAuthor() {
		String snippet = "<comments><author>Dan Higinbotham</author></comments>";
		snappet = generateOutput(roundTripSerilaizedEvents(getEvents(snippet, MSEXCEL)), snippet);
		assertEquals(snappet, snippet);
	}


	private ArrayList<Event> getEvents(String snippet, ParseType filetype) {
		ArrayList<Event> list = new ArrayList<Event>();
		openXMLContentFilter.setUpConfig(filetype);
		openXMLContentFilter.open(new RawDocument(snippet, locENUS));
		while (openXMLContentFilter.hasNext()) {
			Event event = openXMLContentFilter.next();
			openXMLContentFilter.displayOneEvent(event);
			list.add(event);
		}
		openXMLContentFilter.close();
		return list;
	}

	@SuppressWarnings("incomplete-switch")
	private String generateOutput(List<Event> list, String original) {
		ParseType configurationType=openXMLContentFilter.getConfigurationType();
		OpenXMLContentSkeletonWriter writer = new OpenXMLContentSkeletonWriter(configurationType);
		StringBuilder tmp = new StringBuilder();
		for (Event event : list) {
			switch (event.getEventType()) {
			case START_DOCUMENT:
				writer.processStartDocument(locENUS, "utf-8", null,
					openXMLContentFilter.getEncoderManager(),
					(StartDocument)event.getResource());
				break;
			case TEXT_UNIT:
				ITextUnit tu = event.getTextUnit();
				tmp.append(writer.processTextUnit(tu));
				break;
			case DOCUMENT_PART:
				DocumentPart dp = (DocumentPart) event.getResource();
				tmp.append(writer.processDocumentPart(dp));
				break;
			case START_GROUP:
			case START_SUBFILTER:
				StartGroup startGroup = (StartGroup) event.getResource();
				tmp.append(writer.processStartGroup(startGroup));
				break;
			case END_GROUP:
			case END_SUBFILTER:
				Ending ending = (Ending) event.getResource();
				tmp.append(writer.processEndGroup(ending));
				break;
			}
		}		

		LOGGER.debug("nOriginal: {}", original);
		LOGGER.debug("Output:    {}", tmp.toString());
		writer.close();
		return tmp.toString();
	}
}
