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

package net.sf.okapi.filters.openxml;

import static net.sf.okapi.filters.openxml.CodePeekTranslator.locENUS;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.filters.FilterTestDriver;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.RawDocument;
import org.assertj.core.api.iterable.Extractor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class OpenXmlPptxTest{

	@Test
	public void testMaster() throws Exception {
		ConditionalParameters params = new ConditionalParameters();
		params.setTranslateDocProperties(false);
		params.setTranslatePowerpointMasters(true);
		params.setIgnorePlaceholdersInPowerpointMasters(true);

		OpenXMLFilter filter = new OpenXMLFilter();
		filter.setParameters(params);

		URL url = getClass().getResource("/textbox-on-master.pptx");

		RawDocument doc = new RawDocument(url.toURI(),"UTF-8", locENUS);
		ArrayList<Event> events = getEvents(filter, doc);

		List<ITextUnit> textUnits = FilterTestDriver.filterTextUnits(events);
		assertThat(textUnits).extracting(textUnitSourceExtractor()).containsExactly(
				"<run1>My title</run1>",
				"<run1>My subtitle</run1>",
				"<run1>Textbox on layout 1</run1>",
				"<run1>Textbox on master</run1>");
	}

	@Test
	public void testIncludeSlidesYes() throws Exception {
		ConditionalParameters params = new ConditionalParameters();
		params.setTranslateDocProperties(false);
		params.setTranslatePowerpointMasters(false);
		params.setPowerpointIncludedSlideNumbersOnly(true);
		params.tsPowerpointIncludedSlideNumbers = new TreeSet<>();
		params.tsPowerpointIncludedSlideNumbers.add(1);
		params.tsPowerpointIncludedSlideNumbers.add(3);

		OpenXMLFilter filter = new OpenXMLFilter();
		filter.setParameters(params);

		URL url = getClass().getResource("/include-slides.pptx");

		RawDocument doc = new RawDocument(url.toURI(), "UTF-8", locENUS);
		ArrayList<Event> events = getEvents(filter, doc);

		List<ITextUnit> textUnits = FilterTestDriver.filterTextUnits(events);
		assertThat(textUnits).extracting(textUnitSourceExtractor()).containsExactlyInAnyOrder(
				"<run1>Slide 1</run1>",
				"<run1>Slide 3</run1>",
				"comment 1",
				"comment 3",
				"<run1>Note 1</run1>",
				"<run1>Note 3</run1>"
		);
	}

	@Test
	public void testIncludeSlidesCharts() throws Exception {
		ConditionalParameters params = new ConditionalParameters();
		params.setTranslateDocProperties(false);
		params.setTranslatePowerpointMasters(false);
		params.setPowerpointIncludedSlideNumbersOnly(true);
		params.tsPowerpointIncludedSlideNumbers = new TreeSet<>();
		params.tsPowerpointIncludedSlideNumbers.add(1);

		OpenXMLFilter filter = new OpenXMLFilter();
		filter.setParameters(params);

		URL url = getClass().getResource("/include-slides-w-chart.pptx");

		RawDocument doc = new RawDocument(url.toURI(), "UTF-8", locENUS);
		ArrayList<Event> events = getEvents(filter, doc);

		List<ITextUnit> textUnits = FilterTestDriver.filterTextUnits(events);
		assertThat(textUnits)
				.hasSize(1)
				.extracting(textUnitSourceExtractor()).containsExactlyInAnyOrder(
				"<run1>Title 1</run1>"
		);

		params.tsPowerpointIncludedSlideNumbers.add(2);

		events = getEvents(filter, doc);

		textUnits = FilterTestDriver.filterTextUnits(events);
		assertThat(textUnits)
				.hasSize(3)
				.extracting(textUnitSourceExtractor()).containsExactlyInAnyOrder(
				"<run1>Title 1</run1>",
				"<run1>Title 2</run1>",
				"<run1>Chart title</run1>"
		);
	}

	@Test
	public void testIncludeSlidesSmartArt() throws Exception {
		ConditionalParameters params = new ConditionalParameters();
		params.setTranslateDocProperties(false);
		params.setTranslatePowerpointMasters(false);
		params.setPowerpointIncludedSlideNumbersOnly(true);
		params.tsPowerpointIncludedSlideNumbers = new TreeSet<>();
		params.tsPowerpointIncludedSlideNumbers.add(1);

		OpenXMLFilter filter = new OpenXMLFilter();
		filter.setParameters(params);

		URL url = getClass().getResource("/include-slides-w-smartart.pptx");

		RawDocument doc = new RawDocument(url.toURI(), "UTF-8", locENUS);
		ArrayList<Event> events = getEvents(filter, doc);

		List<ITextUnit> textUnits = FilterTestDriver.filterTextUnits(events);
		assertThat(textUnits)
				.hasSize(1)
				.extracting(textUnitSourceExtractor()).containsExactlyInAnyOrder(
				"<run1>Title 1</run1>"
		);

		params.tsPowerpointIncludedSlideNumbers.add(2);

		events = getEvents(filter, doc);

		textUnits = FilterTestDriver.filterTextUnits(events);
		assertThat(textUnits)
				.hasSize(7)
				.extracting(textUnitSourceExtractor()).containsExactlyInAnyOrder(
				"<run1>Title 1</run1>",
				"<run1>Title 2</run1>",
				"<run1>Smart</run1>",
				"<run1>Art</run1>",
				"<run1>Foo</run1>",
				"<run1>Bar</run1>",
				"<run1>Baz</run1>"
		);
	}

	@Test
	public void testIncludeSlidesNo() throws Exception {
		ConditionalParameters params = new ConditionalParameters();
		params.setTranslateDocProperties(false);
		params.setTranslatePowerpointMasters(false);
		params.setPowerpointIncludedSlideNumbersOnly(false);
		params.tsPowerpointIncludedSlideNumbers = new TreeSet<>();
		params.tsPowerpointIncludedSlideNumbers.add(1);
		params.tsPowerpointIncludedSlideNumbers.add(3);

		OpenXMLFilter filter = new OpenXMLFilter();
		filter.setParameters(params);

		URL url = getClass().getResource("/include-slides.pptx");

		RawDocument doc = new RawDocument(url.toURI(), "UTF-8", locENUS);
		ArrayList<Event> events = getEvents(filter, doc);

		List<ITextUnit> textUnits = FilterTestDriver.filterTextUnits(events);
		assertThat(textUnits).extracting(textUnitSourceExtractor()).containsExactlyInAnyOrder(
				"<run1>Slide 1</run1>",
				"<run1>Slide 2</run1>",
				"<run1>Slide 3</run1>",
				"<run1>Slide 4</run1>",
				"comment 1",
				"comment 2",
				"comment 3",
				"comment 4",
				"<run1>Note 1</run1>",
				"<run1>Note 2</run1>",
				"<run1>Note 3</run1>",
				"<run1>Note 4</run1>"
		);
	}

	@Test
	public void testFormattingsPptx() throws Exception {
		ConditionalParameters params = new ConditionalParameters();
		params.setTranslateDocProperties(false);
		params.setTranslatePowerpointMasters(false);

		OpenXMLFilter filter = new OpenXMLFilter();
		filter.setParameters(params);

		URL url = getClass().getResource("/The tomato is formatted.pptx");

		RawDocument doc = new RawDocument(url.toURI(), "UTF-8", locENUS);
		ArrayList<Event> events = getEvents(filter, doc);

		List<ITextUnit> textUnits = FilterTestDriver.filterTextUnits(events);
		assertThat(textUnits).extracting(textUnitSourceExtractor()).containsExactlyInAnyOrder(
				"<run1>The <run2>tomato</run2> is <run3>formatted</run3></run1>",
				"<run1>The <run2>cucumber</run2> is <run3>linked</run3></run1>"
		);

		assertThat(
				textUnits.get(0).getSource().getParts().get(0).getContent().getCodes()
		).hasSize(6).extracting("type").containsExactly(
				"",
				"x-italic;",
				"x-italic;",
				"x-bold;",
				"x-bold;",
				""
		);
		assertThat(
				textUnits.get(1).getSource().getParts().get(0).getContent().getCodes()
		).hasSize(6).extracting("type").containsExactly(
				"",
				"x-underline:sng;",
				"x-underline:sng;",
				"x-link;",
				"x-link;",
				""
		);
	}

	@Test
	public void testFormattedHyperlinkPptx() throws Exception {
		ConditionalParameters params = new ConditionalParameters();
		params.setTranslateDocProperties(false);
		params.setTranslatePowerpointMasters(false);

		OpenXMLFilter filter = new OpenXMLFilter();
		filter.setParameters(params);

		URL url = getClass().getResource("/FormattedHyperlink.pptx");

		RawDocument doc = new RawDocument(url.toURI(), "UTF-8", locENUS);
		ArrayList<Event> events = getEvents(filter, doc);

		List<ITextUnit> textUnits = FilterTestDriver.filterTextUnits(events);
		assertThat(textUnits).extracting(textUnitSourceExtractor()).containsExactlyInAnyOrder(
				"<run1>The <run2>hyperlink</run2></run1>"
		);

		assertThat(
				textUnits.get(0).getSource().getParts().get(0).getContent().getCodes()
		).hasSize(4).extracting("type").containsExactly(
				"",
				"x-link;",
				"x-link;",
				""
		);
	}

	/**
	 * The test document has some cluttered runs with identical properties. Example:
	 * <pre>{@code
	 * <a:r>
	 *   <a:rPr lang="de-DE" baseline="0" dirty="0" err="1" smtClean="0"/>
	 *   <a:t>first</a:t>
	 * </a:r>}</pre>
	 * <p>
	 * We make sure that all runs are merged into one.
	 */
	@Test
	public void testRunMergingWithBaselineAttribute() throws Exception {
		ConditionalParameters params = new ConditionalParameters();
		params.setTranslateDocProperties(false);
		params.setTranslatePowerpointMasters(false);
		params.setTranslatePowerpointNotes(true);

		OpenXMLFilter filter = new OpenXMLFilter();
		filter.setParameters(params);

		URL url = getClass().getResource("/slide-with-note-and-baseline.pptx");

		RawDocument doc = new RawDocument(url.toURI(), "UTF-8", locENUS);
		ArrayList<Event> events = getEvents(filter, doc);

		List<ITextUnit> textUnits = FilterTestDriver.filterTextUnits(events);
		assertThat(textUnits).extracting(textUnitSourceExtractor()).containsExactlyInAnyOrder(
				"<run1>This is my first slide.</run1>",
				"<run1>This is my first note.</run1>"
		);
	}

	/**
	 * The document at hand contains a set baseline in the notes master and a baseline reset in the
	 * actual note.
	 */
	@Test
	public void testRunMergingWithBaselineAttributeFromMaster() throws Exception {
		ConditionalParameters params = new ConditionalParameters();
		params.setTranslateDocProperties(false);
		params.setTranslatePowerpointMasters(false);
		params.setTranslatePowerpointNotes(true);

		OpenXMLFilter filter = new OpenXMLFilter();
		filter.setParameters(params);

		URL url = getClass().getResource("/baseline-on-master.pptx");

		RawDocument doc = new RawDocument(url.toURI(), "UTF-8", locENUS);
		ArrayList<Event> events = getEvents(filter, doc);

		List<ITextUnit> textUnits = FilterTestDriver.filterTextUnits(events);
		assertThat(textUnits).extracting(textUnitSourceExtractor()).containsExactlyInAnyOrder(
				"<run1>This is my first slide.</run1>",
				"<run1>This is my first <run2>note.</run2></run1>"
		);
	}

	@Test
	public void testExternalRelationships() throws Exception {
		ConditionalParameters params = new ConditionalParameters();
		params.setTranslateDocProperties(false);
		params.setTranslatePowerpointMasters(false);

		OpenXMLFilter filter = new OpenXMLFilter();
		filter.setParameters(params);

		URL url = getClass().getResource("/Link-to-movie.pptx");

		RawDocument doc = new RawDocument(url.toURI(), "UTF-8", locENUS);
		ArrayList<Event> events = getEvents(filter, doc);

		List<ITextUnit> textUnits = FilterTestDriver.filterTextUnits(events);
		assertThat(textUnits).extracting(textUnitSourceExtractor()).containsExactlyInAnyOrder(
				"<run1>Click <run2>here</run2>.</run1>"
		);
	}

	private Extractor<ITextUnit, Object> textUnitSourceExtractor() {
		return new Extractor<ITextUnit, Object>() {
			@Override
			public Object extract(ITextUnit input) {
				return input.getSource().toString();
			}
		};
	}

	private ArrayList<Event> getEvents(OpenXMLFilter filter, RawDocument doc) {
		ArrayList<Event> list = new ArrayList<>();
		filter.open(doc, false);
		while (filter.hasNext()) {
			Event event = filter.next();
			list.add(event);
		}
		filter.close();
		return list;
	}
}
