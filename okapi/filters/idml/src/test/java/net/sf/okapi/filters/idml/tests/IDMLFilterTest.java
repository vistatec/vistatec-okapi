/*
 * =============================================================================
 *   Copyright (C) 2010-2017 by the Okapi Framework contributors
 * -----------------------------------------------------------------------------
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * =============================================================================
 */

package net.sf.okapi.filters.idml.tests;

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
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextUnit;
import net.sf.okapi.filters.idml.IDMLFilter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(DataProviderRunner.class)
public class IDMLFilterTest {

	private IDMLFilter filter;
	private FileLocation root;
	private LocaleId locEN = LocaleId.fromString("en");

	@Before
	public void setUp() {
		filter = new IDMLFilter();
		root = FileLocation.fromClass(this.getClass());
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
	public void testSimpleEntry () {
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents("/helloworld-1.idml"), 1);
		assertNotNull(tu);
		String text = TextFragment.getText(tu.getSource().getFirstContent().getCodedText());
		assertEquals("Hello World!", text);
	}

	@Test
	public void testSimpleEntry2 () {
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents("/Test00.idml"), 1);
		assertThat(tu, notNullValue());

		assertThat(tu.toString(), equalTo("<content-1>Hello <content-2>World!</content-2></content-1>Hello again <content-3>World!</content-3>"));

		String codedText = tu.getSource().getFirstContent().getCodedText();
		assertThat(codedText, equalTo("\uE101\uE110Hello \uE101\uE111World!\uE102\uE112\uE102\uE113Hello again \uE101\uE114World!\uE102\uE115"));

		String text = TextFragment.getText(codedText);
		assertThat(text, equalTo("Hello World!Hello again World!"));
	}

    @Test
    public void testWhitespaces() {
		List<ITextUnit> iTextUnits = FilterTestDriver.filterTextUnits(getEvents("/tabsAndWhitespaces.idml"));
		assertThat(iTextUnits, notNullValue());
        assertThat(iTextUnits.size(), is(14));

        TextUnit tu = (TextUnit) iTextUnits.get(0);
        String text = TextFragment.getText(tu.getSource().getFirstContent().getCodedText());
		assertThat(text, equalTo("Hello World."));

        tu = (TextUnit) iTextUnits.get(1);
        assertThat(tu.preserveWhitespaces(), is(true));
        text = TextFragment.getText(tu.getSource().getFirstContent().getText());
		assertThat(text, equalTo("Hello\tWorld with a Tab."));

        tu = (TextUnit) iTextUnits.get(2);
		assertThat(tu.preserveWhitespaces(), is(true));
        text = TextFragment.getText(tu.getSource().getFirstContent().getCodedText());
		assertThat(text, equalTo("Hello \tWorld with a Tab and a white space."));

        tu = (TextUnit) iTextUnits.get(3);
		assertThat(tu.preserveWhitespaces(), is(true));
        text = TextFragment.getText(tu.getSource().getFirstContent().getCodedText());
		assertThat(text, equalTo(" Hello World\t."));

        tu = (TextUnit) iTextUnits.get(4);
		assertThat(tu.preserveWhitespaces(), is(true));
        text = TextFragment.getText(tu.getSource().getFirstContent().getCodedText());
		assertThat(text, equalTo("Hello World."));

        tu = (TextUnit) iTextUnits.get(5);
		assertThat(tu.preserveWhitespaces(), is(true));
        text = TextFragment.getText(tu.getSource().getFirstContent().getCodedText());
		assertThat(text, equalTo("Hello      World."));

		tu = (TextUnit) iTextUnits.get(6);
		assertThat(tu.preserveWhitespaces(), is(true));
		text = TextFragment.getText(tu.getSource().getFirstContent().getCodedText());
		assertThat(text, equalTo(""));

		tu = (TextUnit) iTextUnits.get(7);
		assertThat(tu.preserveWhitespaces(), is(true));
		text = TextFragment.getText(tu.getSource().getFirstContent().getCodedText());
		assertThat(text, equalTo(" Hello World\t."));

		tu = (TextUnit) iTextUnits.get(8);
		assertThat(tu.preserveWhitespaces(), is(true));
		text = TextFragment.getText(tu.getSource().getFirstContent().getCodedText());
		assertThat(text, equalTo("HelloWorldwithout."));

		tu = (TextUnit) iTextUnits.get(9);
		assertThat(tu.preserveWhitespaces(), is(true));
		text = TextFragment.getText(tu.getSource().getFirstContent().getCodedText());
		assertThat(text, equalTo("Hello \tWorld with a Tab and a white space."));

		tu = (TextUnit) iTextUnits.get(10);
		assertThat(tu.preserveWhitespaces(), is(true));
		text = TextFragment.getText(tu.getSource().getFirstContent().getCodedText());
		assertThat(text, equalTo("m-space here."));

		tu = (TextUnit) iTextUnits.get(11);
		assertThat(tu.preserveWhitespaces(), is(true));
		text = TextFragment.getText(tu.getSource().getFirstContent().getCodedText());
		assertThat(text, equalTo("n-space here."));

		tu = (TextUnit) iTextUnits.get(12);
		assertThat(tu.preserveWhitespaces(), is(true));
		text = TextFragment.getText(tu.getSource().getFirstContent().getCodedText());
		assertThat(text, equalTo("another m-spacehere."));

		tu = (TextUnit) iTextUnits.get(13);
		assertThat(tu.preserveWhitespaces(), is(true));
		text = TextFragment.getText(tu.getSource().getFirstContent().getCodedText());
		assertThat(text, equalTo("another one here."));
    }

    @Test
    public void testNewline() {
		List<Event> events = getEvents("/newline.idml");

        ITextUnit tu = FilterTestDriver.getTextUnit(events, 1);
        assertThat(tu, notNullValue());

        TextFragment firstContent = tu.getSource().getFirstContent();
        String text = TextFragment.getText(firstContent.getCodedText());
        assertThat(text, equalTo("32"));

		tu = FilterTestDriver.getTextUnit(events, 2);
		assertThat(tu, notNullValue());
		firstContent = tu.getSource().getFirstContent();
		text = TextFragment.getText(firstContent.getCodedText());
		assertThat(text, equalTo("Hello World"));
    }

	@Test
	public void testStartDocument () {
		assertTrue("Problem in StartDocument", FilterTestDriver.testStartDocument(filter,
			new InputDocument(root.in("/Test01.idml").toString(), null),
			"UTF-8", locEN, locEN));
	}

	@Test
	public void testObjectsWithoutPathPointsAndText() {
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents("/618-objects-without-path-points-and-text.idml"), 1);
		assertThat(tu, is(nullValue()));
	}

	@Test
	public void testAnchoredFrameWithoutPathPoints() {
		List<Event> events = getEvents("/618-anchored-frame-without-path-points.idml");
		ITextUnit tu = FilterTestDriver.getTextUnit(events, 5);
		assertThat(tu, notNullValue());
		assertThat(tu.getSource().getFirstContent().getCodedText(), equalTo("Anchored"));
	}

	@Test
	public void testDocumentWithoutPathPoints() {
		List<Event> events = getEvents("/618-MBE3.idml");

		ITextUnit tu = FilterTestDriver.getTextUnit(events, 1);
		assertThat(tu, notNullValue());
		String text = tu.getSource().getFirstContent().getCodedText();
		assertThat(text, equalTo("\uE101\uE110Fashion Industry In Colombia\uE102\uE111"));

		tu = FilterTestDriver.getTextUnit(events, 2);
		assertThat(tu, notNullValue());
		text = tu.getSource().getFirstContent().getCodedText();
		assertThat(text, equalTo("\uE101\uE110\uE103\uE111\uE102\uE112"));
	}

	@DataProvider
	public static Object[][] testDoubleExtractionProvider() {
		return new Object[][]{
				{"Test00.idml", "okf_idml@ExtractAll.fprm"},
				{"Test01.idml", "okf_idml@ExtractAll.fprm"},
				{"Test02.idml", "okf_idml@ExtractAll.fprm"},
				{"Test03.idml", "okf_idml@ExtractAll.fprm"},

				{"helloworld-1.idml", "okf_idml@ExtractAll.fprm"},
				{"ConditionalText.idml", "okf_idml@ExtractAll.fprm"},

				{"testWithSpecialChars.idml", "okf_idml@ExtractAll.fprm"},

				{"TextPathTest01.idml", "okf_idml@ExtractAll.fprm"},
				{"TextPathTest02.idml", "okf_idml@ExtractAll.fprm"},
				{"TextPathTest03.idml", "okf_idml@ExtractAll.fprm"},
				{"TextPathTest04.idml", "okf_idml@ExtractAll.fprm"},

				{"idmltest.idml", "okf_idml@ExtractAll.fprm"},
				{"idmltest.idml", null},

				{"01-pages-with-text-frames.idml", null},
				{"01-pages-with-text-frames-2.idml", null},
				{"01-pages-with-text-frames-3.idml", null},
				{"01-pages-with-text-frames-4.idml", null},
				{"01-pages-with-text-frames-5.idml", null},
				{"01-pages-with-text-frames-6.idml", null},

				{"02-island-spread-and-threaded-text-frames.idml", null},
				{"03-hyperlink-and-table-content.idml", null},
				{"04-complex-formatting.idml", null},
				{"05-complex-ordering.idml", null},

				{"06-hello-world-12.idml", null},
				{"06-hello-world-13.idml", null},
				{"06-hello-world-14.idml", null},

				{"07-paragraph-breaks.idml", null},

				{"08-conditional-text-and-tracked-changes.idml", null},
				{"08-direct-story-content.idml", null},

				{"09-footnotes.idml", null},
				{"10-tables.idml", null},

				{"11-xml-structures.idml", "okf_idml@ExtractAll.fprm"},
				{"11-xml-structures.idml", null},

				{"618-objects-without-path-points-and-text.idml", null},
				{"618-anchored-frame-without-path-points.idml", null},
				{"618-MBE3.idml", null},
		};
	}

	@Test
	@UseDataProvider("testDoubleExtractionProvider")
	public void testDoubleExtraction(String inputDocumentName, String parametersFileName) {
		List<InputDocument> list = new ArrayList<>();
		list.add(new InputDocument(root.in("/" + inputDocumentName).toString(), parametersFileName));


		RoundTripComparison rtc = new RoundTripComparison(false); // Do not compare skeleton
		assertTrue(rtc.executeCompare(filter, list, "UTF-8", locEN, locEN, "output"));
	}

	@Test
	public void testSkipDiscretionaryHyphens() throws Exception {
		filter.getParameters().setSkipDiscretionaryHyphens(true);

		List<Event> events = getEvents("/Bindestrich.idml");
		ITextUnit tu = FilterTestDriver.getTextUnit(events, 1);
		assertThat(tu, notNullValue());
		String text = tu.getSource().getFirstContent().getCodedText();
		assertThat(text, equalTo("Ich bin ein bedingter Bindestrich."));
	}

	private ArrayList<Event> getEvents (String testFileName) {
		return FilterTestDriver.getEvents(filter, new RawDocument(root.in(testFileName).asUri(), "UTF-8", locEN), null);
	}
}
