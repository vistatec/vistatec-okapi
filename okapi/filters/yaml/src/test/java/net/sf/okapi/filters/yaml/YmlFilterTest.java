/*===========================================================================
  Copyright (C) 2010-2011 by the Okapi Framework contributors
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

package net.sf.okapi.filters.yaml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.TestUtil;
import net.sf.okapi.common.filters.FilterConfiguration;
import net.sf.okapi.common.filters.FilterConfigurationMapper;
import net.sf.okapi.common.filters.FilterTestDriver;
import net.sf.okapi.common.filters.InputDocument;
import net.sf.okapi.common.filters.RoundTripComparison;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.RawDocument;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class YmlFilterTest {
	
	private YamlFilter filter;
	private String root;

	@Before
	public void setUp() {
		filter = new YamlFilter();
		root = TestUtil.getParentDir(this.getClass(), "/yaml/Test01.yml");
	}

	@Test
	public void testDefaultInfo() {
		assertNotNull(filter.getName());
		assertNotNull(filter.getDisplayName());
		assertNotNull(filter.getParameters());
		List<FilterConfiguration> list = filter.getConfigurations();
		assertNotNull(list);
		assertTrue(list.size() > 0);
	}

	@Test
	public void testStartDocument () {
		assertTrue("Problem in StartDocument", FilterTestDriver.testStartDocument(filter,
			new InputDocument(root + "Test01.yml", null),
			"UTF-8", LocaleId.ENGLISH, LocaleId.FRENCH));
	}

	@Test
	public void testSimpleYaml() {
		String snippet = "config:\n  title: \"My Rails Website\"";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet, null), 1);
		assertNotNull(tu);
		assertEquals("My Rails Website", tu.getSource().toString());
		assertEquals("config/title", tu.getName());
	}

	@Test
	public void testSimplePlaceholders() {
		String snippet = "config:\n  title: \"My {{count}} Rails Website\"";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet, null), 1);
		assertNotNull(tu);
		assertEquals("{{count}}", tu.getSource().getFirstContent().getCode(0).toString());
		assertEquals("My {{count}} Rails Website", tu.getSource().toString());
		assertEquals("config/title", tu.getName());
	}
	
	@Test
	public void emptyKey() {
		String snippet = "- test";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet, null), 1);
		assertNotNull(tu);
		assertEquals("test", tu.getSource().toString());
		assertEquals("", tu.getName());
	}
	
	@Test
	public void nonEmptyKey() {
		String snippet = "test: test";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet, null), 1);
		assertNotNull(tu);
		assertEquals("test", tu.getSource().toString());
		assertEquals("test", tu.getName());
		
		snippet = "test: 'test'";
		tu = FilterTestDriver.getTextUnit(getEvents(snippet, null), 1);
		assertNotNull(tu);
		assertEquals("test", tu.getSource().toString());
		assertEquals("test", tu.getName());
		
		snippet = "test: \"test\"";
		tu = FilterTestDriver.getTextUnit(getEvents(snippet, null), 1);
		assertNotNull(tu);
		assertEquals("test", tu.getSource().toString());
		assertEquals("test", tu.getName());
	}
	
	@Test
	public void list() {
		String snippet = "test: [\"test1\", \"test2\", \"test3\"]";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet, null), 1);
		assertNotNull(tu);
		assertEquals("test1", tu.getSource().toString());
		assertEquals("test", tu.getName());
		
		snippet = "test: [test1, test2, test3]";
		tu = FilterTestDriver.getTextUnit(getEvents(snippet, null), 1);
		assertNotNull(tu);
		assertEquals("test1", tu.getSource().toString());
		assertEquals("test", tu.getName());
		
		snippet = "- [test1, test2, test3]";
		tu = FilterTestDriver.getTextUnit(getEvents(snippet, null), 1);
		assertNotNull(tu);
		assertEquals("test1", tu.getSource().toString());
		assertEquals("", tu.getName());
	}
	
	@Test
	public void listSingleQuote() {
		String snippet = "test: ['test1','test2','test3']";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet, null), 1);
		assertNotNull(tu);
		assertEquals("test1", tu.getSource().toString());
		assertEquals("test", tu.getName());			
	}

	@Test
	public void map() {
		String snippet = "- {1: test1, 2: test2, 3: test3}";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet, null), 1);
		assertNotNull(tu);
		assertEquals("test1", tu.getSource().toString());
		assertEquals("1", tu.getName());
		
		snippet = "test: {1: test1, 2: test2, 3: test3}";
		tu = FilterTestDriver.getTextUnit(getEvents(snippet, null), 1);
		assertNotNull(tu);
		assertEquals("test1", tu.getSource().toString());
		assertEquals("test/1", tu.getName());
		
		snippet = "test: {1: \"test1\", 2:\"test2\", 3:\"test3\"}";
		tu = FilterTestDriver.getTextUnit(getEvents(snippet, null), 1);
		assertNotNull(tu);
		assertEquals("test1", tu.getSource().toString());
		assertEquals("test/1", tu.getName());
	}
	
	@Test
	public void mapWithEmptyKeys() {
		String snippet = "order: [ :day, :month, :year ]";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet, null), 1);
		assertNotNull(tu);
		assertEquals(":day", tu.getSource().toString());
		assertEquals("order", tu.getName());
	}
	
	@Test
	public void mapWithEmptyKeysQuoted() {
		String snippet = "order: [ \":day\", \":month\", \":year\" ]";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet, null), 1);
		assertNotNull(tu);
		assertEquals(":day", tu.getSource().toString());
		assertEquals("order", tu.getName());
	}
	
	@Test
	public void issue555() {
		String snippet = "test: \"'s house\"";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet, null), 1);
		assertNotNull(tu);
		assertEquals("'s house", tu.getSource().toString());
		
		snippet = "test: \"'hello'\"";
		tu = FilterTestDriver.getTextUnit(getEvents(snippet, null), 1);
		assertNotNull(tu);
		assertEquals("'hello'", tu.getSource().toString());
	}
	
	@Test
	public void issue556() {
		String snippet = "html: \"Visit <a href=\\\"http://www.google.com\\\">Google</a>\"";
		FilterConfigurationMapper mapper = new FilterConfigurationMapper();
		mapper.addConfigurations("net.sf.okapi.filters.html.HtmlFilter");
		Parameters params = filter.getParameters();
		params.setSubfilter("okf_html");
		params.setUseCodeFinder(false);
		filter.setParameters(params);
		filter.setFilterConfigurationMapper(mapper);
		RawDocument rd = new RawDocument(snippet, LocaleId.ENGLISH);
		List<ITextUnit> tus = FilterTestDriver.filterTextUnits(FilterTestDriver.getEvents(filter, rd, null));
		assertEquals(1, tus.size());
		assertEquals("Visit [#$sg1_sf1_dp1]Google</a>", tus.get(0).getSource().toString());
	}
	
	@Test
	public void testDoubleExtraction() throws URISyntaxException {
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
		list.add(new InputDocument(root + "Test02.yml", null));
		list.add(new InputDocument(root + "en.yml", null));
		list.add(new InputDocument(root + "Test01.yml", null));
		list.add(new InputDocument(root + "Test03.yml", null));
		list.add(new InputDocument(root + "big_config.yml", null));
		list.add(new InputDocument(root + "comment_issue.yml", null));
		RoundTripComparison rtc = new RoundTripComparison();
		assertTrue(rtc.executeCompare(filter, list, "UTF-8", LocaleId.ENGLISH,
				LocaleId.ENGLISH));
	}
	
	@Test
	public void testDoubleExtractionWithEscapes() throws URISyntaxException {
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
		list.add(new InputDocument(root + "escapes.yml", null));
		// FIXME: see snakeyaml issue https://code.google.com/p/snakeyaml/issues/detail?id=205
		// Generally surrogates are not allowed per YAML 1.1 spec in some cases
		//list.add(new InputDocument(root + "/issues/ios_emoji_surrogate.yaml", null));
		//list.add(new InputDocument(root + "emoji1.yaml", null));
		RoundTripComparison rtc = new RoundTripComparison();
		assertTrue(rtc.executeCompare(filter, list, "UTF-8", LocaleId.ENGLISH,
				LocaleId.ENGLISH));
	}
	
	@Test
	public void testFlow() throws URISyntaxException {
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
		list.add(new InputDocument(root + "flow_sample.yml", null));
		RoundTripComparison rtc = new RoundTripComparison();
		assertTrue(rtc.executeCompare(filter, list, "UTF-8", LocaleId.ENGLISH,
				LocaleId.ENGLISH));
	}
	
	@Test
	public void testScalars() throws URISyntaxException {
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
		list.add(new InputDocument(root + "line_continuation.yml", null));		
		list.add(new InputDocument(root + "literal.yml", null));
		list.add(new InputDocument(root + "plain_wrapped.yml", null));
		list.add(new InputDocument(root + "scalar_sample.yml", null));


		RoundTripComparison rtc = new RoundTripComparison();
		assertTrue(rtc.executeCompare(filter, list, "UTF-8", LocaleId.ENGLISH,
				LocaleId.ENGLISH));
	}
	
	@Test
	public void testDoubleExtractionWithMultilines() throws URISyntaxException {
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
		list.add(new InputDocument(root + "folded_indented.yml", null));		
		list.add(new InputDocument(root + "single_wrapped.yml", null));		
		RoundTripComparison rtc = new RoundTripComparison();
		assertTrue(rtc.executeCompare(filter, list, "UTF-8", LocaleId.ENGLISH,
				LocaleId.ENGLISH));
	}
	
	@Test
	public void testDoublePlainWithQuotes() throws URISyntaxException {
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
		list.add(new InputDocument(root + "plain_with_single_quotes.yaml", null));				
		RoundTripComparison rtc = new RoundTripComparison();
		assertTrue(rtc.executeCompare(filter, list, "UTF-8", LocaleId.ENGLISH,
				LocaleId.ENGLISH));
	}
	
	
	@Test
	public void testDoubleExtractionLongLine() throws URISyntaxException {
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
		list.add(new InputDocument(root + "Test01.yml", null));
		list.add(new InputDocument(root + "long_line.yml", null));
		RoundTripComparison rtc = new RoundTripComparison();
		assertTrue(rtc.executeCompare(filter, list, "UTF-8", LocaleId.ENGLISH,
				LocaleId.ENGLISH));
	}

	@Test
	public void testMultilineValue() throws Exception {
		String snippet = "long_line: |-\n    This is a\n   very long line.";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet, null), 1);
		assertEquals("This is a\n   very long line.", tu.getSource().toString());
	}

	@Test
	public void testDoubleExtractionNonStrings() throws URISyntaxException {
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
		list.add(new InputDocument(root + "non_strings.yaml", null));
		RoundTripComparison rtc = new RoundTripComparison();
		assertTrue(rtc.executeCompare(filter, list, "UTF-8", LocaleId.ENGLISH,
				LocaleId.ENGLISH));
	}
	
	@Test
	public void testRoundtripFailures() throws URISyntaxException {
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
		list.add(new InputDocument(root + "/ruby/ruby1.yaml", null));
		list.add(new InputDocument(root + "/issues/issue56-1.yaml", null));				
		list.add(new InputDocument(root + "/spec_test/example2_18.yaml", null));					
		RoundTripComparison rtc = new RoundTripComparison();
		assertTrue(rtc.executeCompare(filter, list, "UTF-8", LocaleId.ENGLISH,
				LocaleId.ENGLISH));
	}
	
	@Ignore("very strange even pyyaml doesn't like it")
	public void testAnchorsAndAlias() throws URISyntaxException {
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
		list.add(new InputDocument(root + "/recursive/with-children-pretty.yaml", null));
		RoundTripComparison rtc = new RoundTripComparison();
		assertTrue(rtc.executeCompare(filter, list, "UTF-8", LocaleId.ENGLISH,
				LocaleId.ENGLISH));
	}
	
	@Test
	public void testOpenTwiceWithString() {
		RawDocument rawDoc = new RawDocument("config:\n  title: \"My Rails Website\"", LocaleId.ENGLISH);
		filter.open(rawDoc);
		filter.open(rawDoc);
		filter.close();
	}

	@Test
	public void testDoubleSubfilter() throws URISyntaxException {
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
		list.add(new InputDocument(root + "Issue556.yml", null));				
		RoundTripComparison rtc = new RoundTripComparison();
		assertTrue(rtc.executeCompare(filter, list, "UTF-8", LocaleId.ENGLISH,
				LocaleId.ENGLISH));
	}

	@Test
	public void testSubfiltering() throws Exception {
		FilterConfigurationMapper mapper = new FilterConfigurationMapper();
		mapper.addConfigurations("net.sf.okapi.filters.html.HtmlFilter");
		Parameters params = filter.getParameters();
		params.setSubfilter("okf_html");
		params.setUseCodeFinder(false);
		filter.setParameters(params);
		filter.setFilterConfigurationMapper(mapper);
		RawDocument rd = new RawDocument(getClass().getResourceAsStream("/yaml/subfilter.yml"),
							"UTF-8", LocaleId.ENGLISH);
		List<ITextUnit> tus = FilterTestDriver.filterTextUnits(FilterTestDriver.getEvents(filter, rd, null));
		assertEquals(2, tus.size());
		assertEquals("Hello world.", tus.get(0).getSource().toString());
		assertEquals("Hello again, world.", tus.get(1).getSource().toString());
		assertNotEquals(tus.get(0).getId(), tus.get(1).getId());
	}

	private ArrayList<Event> getEvents(String snippet, IParameters params) {
		ArrayList<Event> list = new ArrayList<Event>();
		filter.open(new RawDocument(snippet, LocaleId.ENGLISH));
		while (filter.hasNext()) {
			Event event = filter.next();
			list.add(event);
		}
		filter.close();
		return list;
	}
}
