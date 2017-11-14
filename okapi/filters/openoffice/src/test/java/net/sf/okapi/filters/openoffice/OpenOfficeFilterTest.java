/*===========================================================================
  Copyright (C) 2008-2011 by the Okapi Framework contributors
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

package net.sf.okapi.filters.openoffice;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import net.sf.okapi.common.Event;
import net.sf.okapi.common.FileLocation;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filters.FilterConfiguration;
import net.sf.okapi.common.filters.FilterTestDriver;
import net.sf.okapi.common.filters.InputDocument;
import net.sf.okapi.common.filters.RoundTripComparison;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.ITextUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(DataProviderRunner.class)
public class OpenOfficeFilterTest {

	private OpenOfficeFilter filter;
	private FileLocation root = FileLocation.fromClass(getClass());
	private LocaleId locEN = LocaleId.fromString("en");

	@Before
	public void setUp() {
		filter = new OpenOfficeFilter();
	}

	@Test
	public void testDefaultInfo () {
		assertNotNull(filter.getParameters());
		assertNotNull(filter.getName());
		List<FilterConfiguration> list = filter.getConfigurations();
		assertNotNull(list);
		assertTrue(list.size()>0);
	}

	@Test
	public void testFirstTextUnit () {
		ITextUnit tu = getTextUnit(1, "TestDocument01.odt");
		assertNotNull(tu);
		assertEquals("Heading 1", tu.getSource().toString());
	}

	private ITextUnit getTextUnit(int i, String fileName) {
		return FilterTestDriver.getTextUnit(filter,
				new InputDocument(root.in("/" + fileName).toString(), null),
				"UTF-8", locEN, locEN, i);
	}

	@DataProvider
	public static Object[][] testMetadataExtractionProvider () {
		return new Object[][] {
				{
						new ParametersBuilder().extractMetadata(true).build(),
						new String[] {
								"Text on the first page.",
								"Text on the second page.",
								"Author: Test",
								"Page <text:page-number text:select-page=\"current\">2</text:page-number> of <text:page-count>2</text:page-count>",
								"Test document meta comments",
								"met keywod1",
								"keyword2",
								"Test document meta description",
								"Test document meta title",
								"Test custom property's value",
						},
				},
				{
						new ParametersBuilder().extractMetadata(false).build(),
						new String[] {
								"Text on the first page.",
								"Text on the second page.",
								"Author: Test",
								"Page <text:page-number text:select-page=\"current\">2</text:page-number> of <text:page-count>2</text:page-count>",
						},
				},
		};
	}

	@Test
	@UseDataProvider("testMetadataExtractionProvider")
	public void testMetadataExtraction (Parameters params, String[] expectedTexts) {
		List<ITextUnit> textUnits = FilterTestDriver.filterTextUnits(
				getEvents(root.in("/TestDocumentWithMetadata.odt").asUri(), params));
		assertThat(textUnits.size(), is(expectedTexts.length));
		for (int i = 0; i < textUnits.size(); i++) {
			Assert.assertThat(textUnits.get(i).getSource().toString(), equalTo(expectedTexts[i]));
		}
	}

	@DataProvider
	public static Object[][] testNumberTagProvider () {
		return new Object[][] {
				{
						new ParametersBuilder().encodeCharacterEntityReferenceGlyphs(true).build(),
						new String[] {
								"There will be a lot of them",
								"<presentation:header></presentation:header>",
								"<presentation:date-time></presentation:date-time>",
								"<presentation:footer></presentation:footer>",
								"<text:page-number>&lt;number&gt;</text:page-number>",
								"<presentation:date-time></presentation:date-time>",
								"<presentation:footer></presentation:footer>",
								"<text:page-number>&lt;number&gt;</text:page-number>",
								"<presentation:header></presentation:header>",
								"<presentation:date-time></presentation:date-time>",
								"<presentation:footer></presentation:footer>",
								"<text:page-number>&lt;number&gt;</text:page-number>",
								"<text:span text:style-name=\"MT1\"><presentation:date-time></presentation:date-time></text:span>",
								"<text:span text:style-name=\"MT1\"><presentation:footer></presentation:footer></text:span>",
								"<text:span text:style-name=\"MT1\"><text:page-number>&lt;number&gt;</text:page-number></text:span>",
								"<text:span text:style-name=\"MT1\"><presentation:header></presentation:header></text:span>",
								"<text:span text:style-name=\"MT1\"><presentation:date-time></presentation:date-time></text:span>",
								"<text:span text:style-name=\"MT1\"><presentation:footer></presentation:footer></text:span>",
								"<text:span text:style-name=\"MT1\"><text:page-number>&lt;number&gt;</text:page-number></text:span>",
						},
				},
				{
						new ParametersBuilder().encodeCharacterEntityReferenceGlyphs(false).build(),
						new String[] {
								"There will be a lot of them",
								"<presentation:header></presentation:header>",
								"<presentation:date-time></presentation:date-time>",
								"<presentation:footer></presentation:footer>",
								"<text:page-number><number></text:page-number>",
								"<presentation:date-time></presentation:date-time>",
								"<presentation:footer></presentation:footer>",
								"<text:page-number><number></text:page-number>",
								"<presentation:header></presentation:header>",
								"<presentation:date-time></presentation:date-time>",
								"<presentation:footer></presentation:footer>",
								"<text:page-number><number></text:page-number>",
								"<text:span text:style-name=\"MT1\"><presentation:date-time></presentation:date-time></text:span>",
								"<text:span text:style-name=\"MT1\"><presentation:footer></presentation:footer></text:span>",
								"<text:span text:style-name=\"MT1\"><text:page-number><number></text:page-number></text:span>",
								"<text:span text:style-name=\"MT1\"><presentation:header></presentation:header></text:span>",
								"<text:span text:style-name=\"MT1\"><presentation:date-time></presentation:date-time></text:span>",
								"<text:span text:style-name=\"MT1\"><presentation:footer></presentation:footer></text:span>",
								"<text:span text:style-name=\"MT1\"><text:page-number><number></text:page-number></text:span>",
						},
				},
		};
	}

	@Test
	@UseDataProvider("testNumberTagProvider")
	public void testNumberTag (Parameters params, String[] expectedTexts) {
		List<ITextUnit> textUnits = FilterTestDriver.filterTextUnits(
				getEvents(root.in("/TestDocumentWithNumberTag.odp").asUri(), params));
		assertThat(textUnits.size(), is(expectedTexts.length));
		for (int i = 0; i < textUnits.size(); i++) {
			Assert.assertThat(textUnits.get(i).getSource().toString(), equalTo(expectedTexts[i]));
		}
	}

	@DataProvider
	public static Object[][] testFormulaResultExtractionProvider () {
		return new Object[][] {
				{
						new String[] {
								"Sheet1",
								"Max 1000 signs (Do not translate)",
								".",
								"Description (Do not translate)",
								".",
								"This is the first sentence (short one).",
								"We need a lot of lovely characters. A b c d e f g h i j k l m n o p q r s t u v w x y z.",
								"One. Two. Three. Four.",
								"One. Two. Three. Four.",
								"This line contains ten whitespaces at the end          ",
								"Sheet2",
								"Sheet3",
								"<text:sheet-name>???</text:sheet-name>",
								"Page <text:page-number>1</text:page-number>",
								"<text:sheet-name>???</text:sheet-name> (<text:title>???</text:title>)",
								"<text:date style:data-style-name=\"N2\" text:date-value=\"2016-01-29\">00.00.0000</text:date>, <text:time style:data-style-name=\"N2\" text:time-value=\"0000-00-00\">00:00:00</text:time>",
								"Page <text:page-number>1</text:page-number> / <text:page-count>99</text:page-count>",
						},
				},
		};
	}

	@Test
	@UseDataProvider("testFormulaResultExtractionProvider")
	public void testFormulaResultExtraction (String[] expectedTexts) {
		List<ITextUnit> textUnits = FilterTestDriver.filterTextUnits(
				getEvents(root.in("/TestDocumentWithFormulaResults.ods").asUri(), new Parameters()));
		assertThat(textUnits.size(), is(expectedTexts.length));
		for (int i = 0; i < textUnits.size(); i++) {
			Assert.assertThat(textUnits.get(i).getSource().toString(), equalTo(expectedTexts[i]));
		}
	}

	@DataProvider
	public static Object[][] testBookmarkReferencesHandlingProvider () {
		return new Object[][] {
				{
						new ParametersBuilder().extractReferences(true),
						"</text:bookmark-ref>",
				},
				{
						new ParametersBuilder(),
						"<text:bookmark-ref text:reference-format=\"text\" text:ref-name=\"__RefHeading___Toc1166_1491481279\">1 Heading &amp; title</text:bookmark-ref>",
				},
				{
						new ParametersBuilder().encodeCharacterEntityReferenceGlyphs(false),
						"<text:bookmark-ref text:reference-format=\"text\" text:ref-name=\"__RefHeading___Toc1166_1491481279\">1 Heading & title</text:bookmark-ref>",
				},
		};
	}

	@Test
	@UseDataProvider("testBookmarkReferencesHandlingProvider")
	public void testBookmarkReferencesHandling(ParametersBuilder parametersBuilder, String expectedCodeData) throws Exception {
		List<ITextUnit> textUnits = FilterTestDriver.filterTextUnits(
		        getEvents(root.in("/bookmark-reference.odt").asUri(), parametersBuilder.build()));
		assertThat(textUnits.size(), is(3));

		assertThat(textUnits.get(0).getSource().getFirstContent().getCode(1).getData(), equalTo(expectedCodeData));
	}

	private ArrayList<Event> getEvents (URI uri, Parameters params) {
		return FilterTestDriver.getEvents(filter, new RawDocument(uri, "UTF-8", locEN), params);
	}

	@Test
	public void testStartDocument () {
		assertTrue("Problem in StartDocument", FilterTestDriver.testStartDocument(filter,
			new InputDocument(root.in("/TestDocument01.odt").toString(), null),
			"UTF-8", locEN, locEN));
	}

	@Test
	public void testDoubleExtraction () throws URISyntaxException {
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
		list.add(new InputDocument(root.in("/TestSpreadsheet01.ods").toString(), null));
		list.add(new InputDocument(root.in("/TestDocument01.odt").toString(), null));
		list.add(new InputDocument(root.in("/TestDocument02.odt").toString(), null));
		list.add(new InputDocument(root.in("/TestDocument03.odt").toString(), null));
		list.add(new InputDocument(root.in("/TestDocument04.odt").toString(), null));
		list.add(new InputDocument(root.in("/TestDocument05.odt").toString(), null));
		list.add(new InputDocument(root.in("/TestDocument06.odt").toString(), null));
		list.add(new InputDocument(root.in("/TestDrawing01.odg").toString(), null));
		list.add(new InputDocument(root.in("/TestPresentation01.odp").toString(), null));
		list.add(new InputDocument(root.in("/TestDocument_WithITS.odt").toString(), null));
		list.add(new InputDocument(root.in("/TestDocumentWithMetadata.odt").toString(), null));
		list.add(new InputDocument(root.in("/TestDocumentWithNumberTag.odp").toString(), null));
		list.add(new InputDocument(root.in("/TestDocumentWithFormulaResults.ods").toString(), null));
		list.add(new InputDocument(root.in("/TestDocumentWithTableWrappingAboutTable.odt").toString(), null));
		RoundTripComparison rtc = new RoundTripComparison();
		assertTrue(rtc.executeCompare(filter, list, "UTF-8", locEN, locEN, "out"));
	}

}
