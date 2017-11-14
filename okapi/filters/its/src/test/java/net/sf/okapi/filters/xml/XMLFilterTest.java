/*===========================================================================
  Copyright (C) 2008-2017 by the Okapi Framework contributors
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

package net.sf.okapi.filters.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.w3c.its.ITSException;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.FileLocation;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Namespaces;
import net.sf.okapi.common.annotation.GenericAnnotation;
import net.sf.okapi.common.annotation.GenericAnnotationType;
import net.sf.okapi.common.annotation.GenericAnnotations;
import net.sf.okapi.common.filters.FilterConfiguration;
import net.sf.okapi.common.filters.FilterConfigurationMapper;
import net.sf.okapi.common.filters.FilterTestDriver;
import net.sf.okapi.common.filters.IFilterConfigurationMapper;
import net.sf.okapi.common.filters.InputDocument;
import net.sf.okapi.common.filters.RoundTripComparison;
import net.sf.okapi.common.filterwriter.GenericContent;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment.TagType;

@RunWith(DataProviderRunner.class)
public class XMLFilterTest {

	private XMLFilter filter;
	private GenericContent fmt;
	private FileLocation root;
	private LocaleId locEN = LocaleId.fromString("en");

	@Before
	public void setUp() {
		filter = new XMLFilter();
		fmt = new GenericContent();
		root = FileLocation.fromClass(this.getClass());
	}

	@Test
	public void testSpecialEntities () {
		String snippet = "<?xml version=\"1.0\"?>\n"
			+ "<doc>"
			+ "<p>&lt;=lt &gt;=gt &quot;=quot &apos;=apos, &#x00a0;=nbsp</p>"
			+ "</doc>";
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<doc>"
			+ "<p>&lt;=lt &gt;=gt &quot;=quot '=apos, &#x00a0;=nbsp</p>"
			+ "</doc>";
		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(snippet),
			filter.getEncoderManager(), locEN));
	}
	
	@Test
	public void testSpecialEntitiesWithOptions () {
		String snippet = "<?xml version=\"1.0\"?>\n"
			+ "<doc>"
			+ "<p>&lt;=lt &gt;=gt &quot;=quot &apos;=apos, &#x00a0;=nbsp</p>"
			+ "</doc>";
		// No translatable -> allow to not escape apos and quot.
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<doc>"
			+ "<p>&lt;=lt >=gt \"=quot '=apos, \u00a0=nbsp</p>"
			+ "</doc>";
		String paramData = "<?xml version=\"1.0\"?>\n"
			+ "<its:rules version=\"1.0\" xmlns:its=\"http://www.w3.org/2005/11/its\""
			+ " xmlns:zzz=\"okapi-framework:xmlfilter-options\">"
			+ "<zzz:options escapeQuotes='no' escapeGT='no' escapeNbsp='no'/>"
			+ "</its:rules>";
		filter.getParameters().fromString(paramData);
		
		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(snippet),
			filter.getEncoderManager(), locEN));
	}

	@Test
	public void testDeclaredEntities () {
		String snippet = "<?xml version=\"1.0\"?>\n"
				+ "<!DOCTYPE root [\n"
				+ "<!ENTITY abcdef \"AB'C'DEF\">\n"
				+ "]>\n"
				+ "<root><p>[&abcdef;]</p></root>";
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
				+ "<!DOCTYPE root [\n"
				+ "<!ENTITY abcdef \"AB'C'DEF\">\n"
				+ "]>\n"
				+ "<root><p>[&abcdef;AB'C'DEF]</p></root>";
//TODO: This is a bug: we should not have the expanded entity in the output
		// But this is an issue at the XML parsing level.
		// See issue https://code.google.com/p/okapi/issues/detail?id=395
		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(snippet),
			filter.getEncoderManager(), locEN));
	}
	
	@Test
	public void testLocaleFilter1 () {
		String snippet = "<?xml version=\"1.0\"?>\n"
			+ "<doc><its:rules version=\"2.0\" xmlns:its=\"http://www.w3.org/2005/11/its\">"
			+ "<its:localeFilterRule selector=\"//para1\" localeFilterList='de'/>"
			+ "<its:localeFilterRule selector=\"//para2\" localeFilterList='*'/>"
			+ "</its:rules>"
			+ "<para1>text1</para1>"
			+ "<para2>text2</para2>"
			+ "</doc>";
		ArrayList<Event> list = getEvents(snippet);
		ITextUnit tu = FilterTestDriver.getTextUnit(list, 1);
		assertNotNull(tu);
		assertEquals("text2", tu.getSource().toString());
	}
	
	@Test
	public void testLocaleFilter2 () {
		String snippet = "<?xml version=\"1.0\"?>\n"
			+ "<doc><its:rules version=\"2.0\" xmlns:its=\"http://www.w3.org/2005/11/its\">"
			+ "<its:localeFilterRule selector=\"//para1\" localeFilterList='de'/>"
			+ "<its:localeFilterRule selector=\"//para2\" localeFilterList='fr-CH'/>"
			+ "</its:rules>"
			+ "<para1>text1</para1>"
			+ "<para2>text2</para2>"
			+ "<para3>text3</para3>"
			+ "</doc>";
		ArrayList<Event> list = getEvents(snippet);
		ITextUnit tu = FilterTestDriver.getTextUnit(list, 1);
		assertNotNull(tu);
		assertEquals("text3", tu.getSource().toString());
	}

	@Test
	public void testLocaleFilter3 () {
		String snippet = "<?xml version=\"1.0\"?>\n"
			+ "<doc><its:rules version=\"2.0\" xmlns:its=\"http://www.w3.org/2005/11/its\">"
			+ "<its:localeFilterRule selector=\"//para1\" localeFilterList=''/>"
			+ "<its:localeFilterRule selector=\"//para2\" localeFilterList='en-CA, fr-*'/>"
			+ "</its:rules>"
			+ "<para1>text1</para1>"
			+ "<para2>text2</para2>"
			+ "</doc>";
		ArrayList<Event> list = getEvents(snippet);
		ITextUnit tu = FilterTestDriver.getTextUnit(list, 1);
		assertNotNull(tu);
		assertEquals("text2", tu.getSource().toString());
	}
	
	@Test
	public void testLocaleFilter4 () {
		String snippet = "<?xml version=\"1.0\"?>\n"
			+ "<doc><its:rules version=\"2.0\" xmlns:its=\"http://www.w3.org/2005/11/its\">"
			+ "<its:localeFilterRule selector=\"//para1\" localeFilterList='fr-CH, fr-*-CH'/>"
			+ "<its:localeFilterRule selector=\"//para2\" localeFilterList='fr-CA, fr'/>"
			+ "</its:rules>"
			+ "<para1>text1</para1>"
			+ "<para2>text2</para2>"
			+ "</doc>";
		ArrayList<Event> list = getEvents(snippet);
		ITextUnit tu = FilterTestDriver.getTextUnit(list, 1);
		assertNotNull(tu);
		assertEquals("text2", tu.getSource().toString());
	}
	
	@Test
	public void testLocaleFilter5 () {
		String snippet = "<?xml version=\"1.0\"?>\n"
			+ "<doc xmlns:its=\"http://www.w3.org/2005/11/its\"><its:rules version=\"2.0\">"
			+ "<its:localeFilterRule selector=\"/doc\" localeFilterList='fr'/>"
			+ "</its:rules>"
			+ "<para1 its:localeFilterList='fr-CH'>text1</para1>"
			+ "<para2>text2</para2>"
			+ "</doc>";
		ArrayList<Event> list = getEvents(snippet);
		ITextUnit tu = FilterTestDriver.getTextUnit(list, 1);
		assertNotNull(tu);
		assertEquals("text2", tu.getSource().toString());
	}
	
	@Test
	public void testLocaleFilter6 () {
		String snippet = "<?xml version=\"1.0\"?>\n"
			+ "<doc xmlns:its=\"http://www.w3.org/2005/11/its\"><its:rules version=\"2.0\">"
			+ "<its:localeFilterRule selector=\"//para[@scope='GER']\" localeFilterList='de'/>"
			+ "</its:rules>"
			+ "<para scope='GER'>text1</para>"
			+ "<para scope='ZZZ'>text2</para>"
			+ "<para scope='ZZZ' its:localeFilterList='fr-*'>text3</para>"
			+ "</doc>";
		ArrayList<Event> list = getEvents(snippet);
		ITextUnit tu = FilterTestDriver.getTextUnit(list, 1);
		assertNotNull(tu);
		assertEquals("text2", tu.getSource().toString());
		tu = FilterTestDriver.getTextUnit(list, 2);
		assertNotNull(tu);
		assertEquals("text3", tu.getSource().toString());
	}
	
	@Test
	public void testStack () {
		String snippet = "<?xml version=\"1.0\"?>"
			+ "<!DOCTYPE set PUBLIC \"-//OASIS//DTD DocBook XML V4.5//EN\" \"../docbook/docbookx.dtd\">"
			+ "<set lang=\"en\">"
			+ "<its:rules xmlns:its=\"http://www.w3.org/2005/11/its\" version=\"1.0\">"
			+ "<its:translateRule selector=\"//set/@lang\" translate=\"yes\"/>"
			+ "</its:rules>"
			+ "<title>Test</title>"
			+ "</set>"; 
		ArrayList<Event> list = getEvents(snippet);
		ITextUnit tu = FilterTestDriver.getTextUnit(list, 1);
		assertEquals("en", tu.getSource().toString());
	}

	@Test
	public void testComplexIdValue () {
		String snippet = "<?xml version=\"1.0\"?>\n"
			+ "<doc><its:rules version=\"1.0\" xmlns:its=\"http://www.w3.org/2005/11/its\""
			+ " xmlns:itsx=\"http://www.w3.org/2008/12/its-extensions\">"
			+ "<its:translateRule selector=\"//doc\" translate=\"no\"/>"
			+ "<its:translateRule selector=\"//src\" translate=\"yes\" itsx:idValue=\"../../name/@id\"/>"
			+ "</its:rules>"
			+ "<grp><name id=\"id1\" /><u><src>text 1</src></u></grp>"
			+ "<grp><name id=\"id1\" /><u><src xml:id=\"xid2\">text 2</src></u></grp>"
			+ "</doc>";
		ArrayList<Event> list = getEvents(snippet);
		ITextUnit tu = FilterTestDriver.getTextUnit(list, 1);
		assertNotNull(tu);
		assertEquals("id1", tu.getName());
		tu = FilterTestDriver.getTextUnit(list, 2);
		assertNotNull(tu);
		assertEquals("xid2", tu.getName()); // xml:id overrides global rule
	}

	@Test
	public void testIdValueV2 () {
		String snippet = "<?xml version=\"1.0\"?>\n"
			+ "<doc><its:rules version=\"2.0\" xmlns:its=\"http://www.w3.org/2005/11/its\""
			+ " >"
			+ "<its:translateRule selector=\"//doc\" translate=\"no\"/>"
			+ "<its:translateRule selector=\"//src\" translate=\"yes\"/>"
			+ "<its:idValueRule selector=\"//src\" idValue=\"../../name/@id\"/>"
			+ "</its:rules>"
			+ "<grp><name id=\"id1\" /><u><src>text 1</src></u></grp>"
			+ "<grp><name id=\"id1\" /><u><src xml:id=\"xid2\">text 2</src></u></grp>"
			+ "</doc>";
		ArrayList<Event> list = getEvents(snippet);
		ITextUnit tu = FilterTestDriver.getTextUnit(list, 1);
		assertNotNull(tu);
		assertEquals("id1", tu.getName());
		tu = FilterTestDriver.getTextUnit(list, 2);
		assertNotNull(tu);
		assertEquals("xid2", tu.getName()); // xml:id overrides global rule
	}

	@Test (expected = ITSException.class)
	public void testITSVersionAttribute () {
		String snippet = "<?xml version=\"1.0\"?>\n"
			+ "<doc><its:rules badversionattribute=\"1.0\" xmlns:its=\"http://www.w3.org/2005/11/its\">"
			+ "<its:translateRule selector=\"//doc\" translate=\"yes\"/>"
			+ "</its:rules>"
			+ "<p>data</p>"
			+ "</doc>";
		ArrayList<Event> list = getEvents(snippet);
		FilterTestDriver.getTextUnit(list, 1);
	}

	@Test
	public void testPreserveSpace1 () {
		String snippet = "<?xml version=\"1.0\"?>\n"
			+ "<doc><its:rules version=\"2.0\" xmlns:its=\"http://www.w3.org/2005/11/its\">"
			+ "<its:preserveSpaceRule selector=\"//grp\" space='preserve'/>"
			+ "<its:preserveSpaceRule selector=\"//grp/p\" space='default'/>"
			+ "<its:translateRule selector=\"//@text\" translate='yes'/>"
			+ "<its:preserveSpaceRule selector=\"//@text\" space='preserve'/>"
			+ "<its:withinTextRule selector=\"//span\" withinText='yes'/>"
			+ "</its:rules>"
			+ "<p text=\"  A  B  \">  a  b  c  </p>"
			+ "<grp>"
			+ "<p>  a  b  c  </p>"
			+ "<p xml:space='default'>  Z  <span>b  c</span>  </p>"
			+ "</grp>"
			+ "</doc>";
		ArrayList<Event> list = getEvents(snippet);
		ITextUnit tu = FilterTestDriver.getTextUnit(list, 1);
		assertNotNull(tu);
		assertEquals("  A  B  ", tu.getSource().toString());
		tu = FilterTestDriver.getTextUnit(list, 2);
		assertNotNull(tu);
		assertEquals(" a b c ", tu.getSource().toString());
		assertFalse(tu.preserveWhitespaces());
		tu = FilterTestDriver.getTextUnit(list, 3);
		assertNotNull(tu);
		assertEquals("  a  b  c  ", tu.getSource().toString());
		assertTrue(tu.preserveWhitespaces());
		tu = FilterTestDriver.getTextUnit(list, 4);
		assertNotNull(tu);
		// Note: no content-based test for inline space='preserve' because we don't support it 
		assertEquals(" Z <span>b c</span> ", tu.getSource().toString());
		assertFalse(tu.preserveWhitespaces());
	}
	
	@Test
	public void testITSVersion1 () {
		String snippet = "<?xml version=\"1.0\"?>\n"
			+ "<doc><its:rules version=\"1.0\" xmlns:its=\"http://www.w3.org/2005/11/its\">"
			+ "<its:translateRule selector=\"//doc\" translate=\"yes\"/>"
			+ "</its:rules>"
			+ "<p>data</p>"
			+ "</doc>";
		ArrayList<Event> list = getEvents(snippet);
		assertNotNull(FilterTestDriver.getTextUnit(list, 1));
	}
	
	@Test
	public void testITSVersion2 () {
		String snippet = "<?xml version=\"1.0\"?>\n"
			+ "<doc><its:rules version=\"2.0\" xmlns:its=\"http://www.w3.org/2005/11/its\">"
			+ "<its:translateRule selector=\"//doc\" translate=\"yes\"/>"
			+ "</its:rules>"
			+ "<p>data</p>"
			+ "</doc>";
		ArrayList<Event> list = getEvents(snippet);
		assertNotNull(FilterTestDriver.getTextUnit(list, 1));
	}
	
	@Test
	public void testIdValue () {
		String snippet = "<?xml version=\"1.0\"?>\n"
			+ "<doc><its:rules version=\"1.0\" xmlns:its=\"http://www.w3.org/2005/11/its\""
			+ " xmlns:itsx=\"http://www.w3.org/2008/12/its-extensions\">"
			+ "<its:translateRule selector=\"//p\" translate=\"yes\" itsx:idValue=\"@name\"/>"
			+ "</its:rules>"
			+ "<p name=\"id1\">text 1</p>"
			+ "<p xml:id=\"xid2\">text 2</p>"
			+ "<p xml:id=\"xid3\" name=\"id3\">text 3</p></doc>";
		ArrayList<Event> list = getEvents(snippet);
		ITextUnit tu = FilterTestDriver.getTextUnit(list, 1);
		assertNotNull(tu);
		assertEquals("id1", tu.getName());
		tu = FilterTestDriver.getTextUnit(list, 2);
		assertNotNull(tu);
		assertEquals("xid2", tu.getName()); // No 'name' attribute
		tu = FilterTestDriver.getTextUnit(list, 3);
		assertNotNull(tu);
		assertEquals("xid3", tu.getName()); // xml:id overrides global rule
	}
	
	@Test
	public void testDomain1 () {
		String snippet = "<?xml version=\"1.0\"?>\n"
			+ "<doc><its:rules version=\"1.0\" xmlns:its=\"http://www.w3.org/2005/11/its\">"
			+ "<its:translateRule selector=\"//head\" translate=\"no\"/>"
			+ "<its:domainRule selector=\"/doc\" domainPointer=\"//domSet/topic|//domSet/subject\" domainMapping=\"dom1 domA, dom2 domB\"/>"
			+ "<its:domainRule selector=\"/doc\" domainPointer=\"//domain\" domainMapping=\"dom3 domC, dom4 domD\"/>"
			+ "</its:rules>"
			+ "<head><domain>domZ,dom3,   dom4</domain><domain>domZ</domain>"
			+ "<domSet><topic>dom1</topic><subject>dom2, domy, domY</subject></domSet>"
			+ "</head>"
			+ "<p>text</p>"
			+ "</doc>";
		ArrayList<Event> list = getEvents(snippet);
		ITextUnit tu = FilterTestDriver.getTextUnit(list, 1);
		GenericAnnotation ga = tu.getAnnotation(GenericAnnotations.class).getFirstAnnotation(GenericAnnotationType.DOMAIN);
		assertEquals("domZ, domC, domD", ga.getString(GenericAnnotationType.DOMAIN_VALUE));		
	}
	
	@Test
	public void testDomain2 () {
		String snippet = "<?xml version=\"1.0\"?>\n"
			+ "<doc><its:rules version=\"1.0\" xmlns:its=\"http://www.w3.org/2005/11/its\">"
			+ "<its:translateRule selector=\"//head\" translate=\"no\"/>"
			// Inverse the rules compared to testDomain1
			+ "<its:domainRule selector=\"/doc\" domainPointer=\"//domain\" domainMapping=\"dom3 domC, dom4 domD\"/>"
			+ "<its:domainRule selector=\"/doc\" domainPointer=\"//domSet/topic|//domSet/subject\" domainMapping=\"dom1 domA, dom2 domB\"/>"
			+ "</its:rules>"
			+ "<head><domain>domZ,dom3,   dom4</domain><domain>domZ</domain>"
			+ "<domSet><topic>dom1</topic><subject>dom2, domY</subject></domSet>"
			+ "</head>"
			+ "<p>text</p>"
			+ "</doc>";
		ArrayList<Event> list = getEvents(snippet);
		ITextUnit tu = FilterTestDriver.getTextUnit(list, 1);
		GenericAnnotation ga = tu.getAnnotation(GenericAnnotations.class).getFirstAnnotation(GenericAnnotationType.DOMAIN);
		assertEquals("domA, domB, domY", ga.getString(GenericAnnotationType.DOMAIN_VALUE));		
	}
	
	@Test
	public void testAllowedCharsAndStorageSize () {
		String snippet = "<?xml version=\"1.0\"?>\n"
			+ "<doc xmlns:its=\"http://www.w3.org/2005/11/its\"><its:rules version=\"2.0\" "
			+ " xmlns:itsx=\"http://www.w3.org/2008/12/its-extensions\">"
			+ "<its:translateRule selector=\"//p/@title\" translate='yes'/>"
			+ "<its:withinTextRule selector=\"//span\" withinText='yes'/>"
			+ "<its:allowedCharactersRule selector=\"//p\" allowedCharacters='[a-z]'/>"
			+ "<its:allowedCharactersRule selector=\"//p/@title\" allowedCharacters='[A-Z]'/>"
			+ "</its:rules>"
			+ "<p title='ABC'>text1</p>"
			+ "<r>text <span its:allowedCharacters='[tex]' its:storageSize='10' its:lineBreakType='crlf'>text</span></r>"
			+ "</doc>";
		ArrayList<Event> list = getEvents(snippet);
		ITextUnit tu = FilterTestDriver.getTextUnit(list, 1);
		GenericAnnotation ga = tu.getSource().getAnnotation(GenericAnnotations.class).getFirstAnnotation(GenericAnnotationType.ALLOWEDCHARS);
		assertEquals("[A-Z]", ga.getString(GenericAnnotationType.ALLOWEDCHARS_VALUE));
		tu = FilterTestDriver.getTextUnit(list, 2);
		ga = tu.getSource().getAnnotation(GenericAnnotations.class).getFirstAnnotation(GenericAnnotationType.ALLOWEDCHARS);
		assertEquals("[a-z]", ga.getString(GenericAnnotationType.ALLOWEDCHARS_VALUE));
		tu = FilterTestDriver.getTextUnit(list, 3);
		assertEquals(null, tu.getAnnotation(GenericAnnotations.class));
		Code code = tu.getSource().getFirstContent().getCode(0);
		GenericAnnotations anns = (GenericAnnotations)code.getAnnotation(GenericAnnotationType.GENERIC);
		assertNotNull(anns);
		assertEquals("[tex]", anns.getFirstAnnotation(GenericAnnotationType.ALLOWEDCHARS).getString(GenericAnnotationType.ALLOWEDCHARS_VALUE));
		ga = anns.getFirstAnnotation(GenericAnnotationType.STORAGESIZE);
		assertEquals(10, (int)ga.getInteger(GenericAnnotationType.STORAGESIZE_SIZE));
		assertEquals("UTF-8", ga.getString(GenericAnnotationType.STORAGESIZE_ENCODING));
		assertEquals("crlf", ga.getString(GenericAnnotationType.STORAGESIZE_LINEBREAK));
	}

	@Test
	public void testStorageSize () {
		String snippet = "<?xml version=\"1.0\"?>\n"
			+ "<doc><its:rules version=\"2.0\" xmlns:its=\"http://www.w3.org/2005/11/its\""
			+ " xmlns:itsx=\"http://www.w3.org/2008/12/its-extensions\">"
			+ "<its:translateRule selector=\"//p/@title\" translate='yes'/>"
			+ "<its:storageSizeRule selector=\"//p\" storageSize='10' storageEncoding='UTF-16'/>"
			+ "<its:storageSizeRule selector=\"//p/@title\" storageSize='5' storageEncoding='Shift-JIS'/>"
			+ "</its:rules>"
			+ "<p title='abc'>text</p>"
			+ "</doc>";
		ArrayList<Event> list = getEvents(snippet);
		ITextUnit tu = FilterTestDriver.getTextUnit(list, 1);
		GenericAnnotation ga = tu.getSource().getAnnotation(GenericAnnotations.class).getFirstAnnotation(GenericAnnotationType.STORAGESIZE);
		assertEquals(5, (int)ga.getInteger(GenericAnnotationType.STORAGESIZE_SIZE));
		assertEquals("Shift-JIS", ga.getString(GenericAnnotationType.STORAGESIZE_ENCODING));
		assertEquals("lf", ga.getString(GenericAnnotationType.STORAGESIZE_LINEBREAK));
		tu = FilterTestDriver.getTextUnit(list, 2);
		ga = tu.getSource().getAnnotation(GenericAnnotations.class).getFirstAnnotation(GenericAnnotationType.STORAGESIZE);
		assertEquals(10, (int)ga.getInteger(GenericAnnotationType.STORAGESIZE_SIZE));
		assertEquals("UTF-16", ga.getString(GenericAnnotationType.STORAGESIZE_ENCODING));
		assertEquals("lf", ga.getString(GenericAnnotationType.STORAGESIZE_LINEBREAK));
	}
	
	@Test
	public void testIdComplexValue () {
		String snippet = "<?xml version=\"1.0\"?>\n"
			+ "<doc><its:rules version=\"1.0\" xmlns:its=\"http://www.w3.org/2005/11/its\""
			+ " xmlns:itsx=\"http://www.w3.org/2008/12/its-extensions\">"
			+ "<its:translateRule selector=\"//text\" translate=\"yes\" itsx:idValue=\"concat(../@name, '_t')\"/>"
			+ "<its:translateRule selector=\"//desc\" translate=\"yes\" itsx:idValue=\"concat(../@name, '_d')\"/>"
			+ "</its:rules>"
			+ "<msg name=\"id1\">"
			+ "<text>Value of text</text>"
			+ "<desc>Value of desc</desc>"
			+ "</msg></doc>";
		ArrayList<Event> list = getEvents(snippet);
		ITextUnit tu = FilterTestDriver.getTextUnit(list, 1);
		assertNotNull(tu);
		assertEquals("id1_t", tu.getName());
		tu = FilterTestDriver.getTextUnit(list, 2);
		assertNotNull(tu);
		assertEquals("id1_d", tu.getName());
	}

	@Test
	public void testLocalWithinText () { // ITS 2.0
		String snippet = "<?xml version=\"1.0\"?>\n"
			+ "<doc xmlns:its=\"http://www.w3.org/2005/11/its\"><its:rules version=\"2.0\" >"
			+ "<its:withinTextRule selector=\"//c\" withinText=\"yes\"/>"
			+ "</its:rules>"
			+ "<p>t1<c its:withinText='no'/>t2</p>"
			+ "<p>t3<c />t4</p>"
			+ "<p>t5<c></c>t6</p>"
			+ "</doc>";
		ArrayList<Event> list = getEvents(snippet);
		ITextUnit tu = FilterTestDriver.getTextUnit(list, 1);
		assertEquals("t1", fmt.setContent(tu.getSource().getFirstContent()).toString());
		tu = FilterTestDriver.getTextUnit(list, 2);
		assertEquals("t2", fmt.setContent(tu.getSource().getFirstContent()).toString());
		tu = FilterTestDriver.getTextUnit(list, 3);
		Code code = tu.getSource().getFirstContent().getCodes().get(0);
		assertEquals(TagType.PLACEHOLDER, code.getTagType());
		assertEquals("t3<1/>t4", fmt.setContent(tu.getSource().getFirstContent()).toString());
		tu = FilterTestDriver.getTextUnit(list, 4);
		assertEquals("t5<1/>t6", fmt.setContent(tu.getSource().getFirstContent()).toString());
	}

	@Test
	public void testLocalWithinTextOnRoot () { // ITS 2.0
		String snippet = "<?xml version=\"1.0\"?>\n"
			+ "<doc><link xmlns:its='http://www.w3.org/2005/11/its' its:version='2.0' its:withinText='yes'>Hello world</link></doc>";
		ArrayList<Event> list = getEvents(snippet);
		ITextUnit tu = FilterTestDriver.getTextUnit(list, 1);
		assertEquals("<1>Hello world</1>", fmt.setContent(tu.getSource().getFirstContent()).toString());
		//TODO: If we remove the doc element we get a null on the TU. We need to fix that rare case.
	}

	//TODO: implement it properly
	@Test @Ignore
	public void testLocalWithinTextNested () { // ITS 2.0
		String snippet = "<?xml version=\"1.0\"?>\n"
			+ "<doc xmlns:its=\"http://www.w3.org/2005/11/its\"><its:rules version=\"2.0\" >"
			+ "<its:withinTextRule selector=\"//n\" withinText=\"nested\"/>"
			+ "</its:rules>"
			+ "<p>t1 <n>t2</n> t3</p>"
			+ "</doc>";
		ArrayList<Event> list = getEvents(snippet);
		ITextUnit tu = FilterTestDriver.getTextUnit(list, 1);
		assertEquals("t1 <1/> t3", fmt.setContent(tu.getSource().getFirstContent()).toString());
		tu = FilterTestDriver.getTextUnit(list, 2);
		assertEquals("t2", fmt.setContent(tu.getSource().getFirstContent()).toString());
	}

	@Test
	public void testEmptyElements () {
		String snippet = "<?xml version=\"1.0\"?>\n"
			+ "<doc><its:rules version=\"1.0\" xmlns:its=\"http://www.w3.org/2005/11/its\">"
			+ "<its:withinTextRule selector=\"//c\" withinText=\"yes\"/>"
			+ "</its:rules>"
			+ "<p>t1<c/>t2</p>"
			+ "<p>t1<c />t2</p>"
			+ "<p>t1<c></c>t2</p>"
			+ "</doc>";
		ArrayList<Event> list = getEvents(snippet);
		ITextUnit tu = FilterTestDriver.getTextUnit(list, 1);
		Code code = tu.getSource().getFirstContent().getCodes().get(0);
		assertEquals(TagType.PLACEHOLDER, code.getTagType());
		assertEquals("t1<1/>t2", fmt.setContent(tu.getSource().getFirstContent()).toString());
		tu = FilterTestDriver.getTextUnit(list, 2);
		assertEquals("t1<1/>t2", fmt.setContent(tu.getSource().getFirstContent()).toString());
		tu = FilterTestDriver.getTextUnit(list, 3);
		assertEquals("t1<1/>t2", fmt.setContent(tu.getSource().getFirstContent()).toString());
	}

	@Test
	public void testOutputEmptyElements () {
		String snippet = "<?xml version=\"1.0\"?>\n"
			+ "<doc><its:rules version=\"1.0\" xmlns:its=\"http://www.w3.org/2005/11/its\">"
			+ "<its:withinTextRule selector=\"//c\" withinText=\"yes\"/>"
			+ "</its:rules>"
			+ "<p>t1<c/>t2</p>"
			+ "<p>t1<c />t2</p>"
			+ "<p>t1<c></c>t2</p>"
			+ "</doc>";
		// All empty elements are re-written as <elem/>
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<doc><its:rules version=\"1.0\" xmlns:its=\"http://www.w3.org/2005/11/its\">"
			+ "<its:withinTextRule selector=\"//c\" withinText=\"yes\"/>"
			+ "</its:rules>"
			+ "<p>t1<c/>t2</p>"
			+ "<p>t1<c/>t2</p>"
			+ "<p>t1<c/>t2</p>"
			+ "</doc>";
		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(snippet),
			filter.getEncoderManager(), locEN));
	}
	
	@Test
	public void testOutputAttributesAndQuotes () {
		String snippet = "<?xml version=\"1.0\"?>\n"
			+ "<doc><its:rules version=\"1.0\" xmlns:its=\"http://www.w3.org/2005/11/its\">"
			+ "<its:translateRule selector=\"//*/@alt\" translate=\"yes\"/>"
			+ "</its:rules>"
			+ "<p alt='It&apos;s done' notrans='&apos;'>It&apos;s done</p>"
			+ "</doc>";
		// All empty elements are re-written as <elem/>
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<doc><its:rules version=\"1.0\" xmlns:its=\"http://www.w3.org/2005/11/its\">"
			+ "<its:translateRule selector=\"//*/@alt\" translate=\"yes\"/>"
			+ "</its:rules>"
			+ "<p alt=\"It's done\" notrans=\"'\">It's done</p>"
			+ "</doc>";
		ArrayList<Event> list = getEvents(snippet);
		ITextUnit tu = FilterTestDriver.getTextUnit(list, 1);
		assertEquals("It's done", tu.getSource().toString());
		assertEquals(expected, FilterTestDriver.generateOutput(list,
			filter.getEncoderManager(), locEN));
	}
// Cannot test with internal rules, works with external only
//	@Test
//	public void testLineBreakAsCode () {
//		String snippet = "<?xml version=\"1.0\"?>\n"
//			+ "<doc><its:rules version=\"1.0\" xmlns:its=\"http://www.w3.org/2005/11/its\""
//			+ " xmlns:okp=\"okapi-framework:xmlfilter-options\">"
//			+ "<okp:options lineBreakAsCode=\"yes\"/>"
//			+ "</its:rules>"
//			+ "<p>line 1&#10;line 2.</p>"
//			+ "</doc>";
//		ArrayList<Event> list = getEvents(snippet);
//		ITextUnit tu = FilterTestDriver.getTextUnit(list, 1);
//		assertNotNull(tu);
//		assertEquals("line 1.&#10;line 2.", tu.toString());
//	}
	
//TODO: Must fix entity handing for all XML parser
//	@Test
//	public void testSimpleEntities () {
//		String snippet = "<?xml version=\"1.0\" ?>\r"
//			+ "<!DOCTYPE doc ["
//			+ "<!ENTITY aWithRingAndAcute '&#x01fb;'>\r"
//			+ "<!ENTITY text 'TEXT'>\r"
//			+ "]>\r"
//			+ "<doc>"
//			+ "<p>&aWithRingAndAcute;=e1</p>"
//			+ "<p>&text;=e2</p>"
//			+ "</doc>";
//		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r"
//			+ "<!DOCTYPE doc ["
//			+ "<!ENTITY aWithRingAndAcute '&#x01fb;'>\r"
//			+ "<!ENTITY text 'TEXT'>\r"
//			+ "]>\r"
//			+ "<doc>"
//			+ "<p>&aWithRingAndAcute;=e1</p>"
//			+ "<p>&text;=e2</p>"
//			+ "</doc>";
//		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
//		assertNotNull(tu);
//		assertEquals("<1/>=e1", fmt.setContent(tu.getSourceContent()).toString());
//		
//		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(snippet),
//			filter.getEncoderManager(), locEN));
//	}
//
//	@Test
//	public void testComplexEntities () {
//		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r"
//			+ "<!DOCTYPE doc ["
//			+ "<!ENTITY entity1 '[&entity2;]'>\r"
//			+ "<!ENTITY entity2 'TEXT'>\r"
//			+ "]>\r"
//			+ "<doc>"
//			+ "<p>&entity1;=[TEXT]</p>"
//			+ "<p>&entity2;=TEXT</p>"
//			+ "</doc>";
//		assertEquals(snippet, FilterTestDriver.generateOutput(getEvents(snippet), locEN));
//	}
	
	@Test
	public void testDefaultInfo () {
		assertNotNull(filter.getParameters());
		assertNotNull(filter.getName());
		List<FilterConfiguration> list = filter.getConfigurations();
		assertNotNull(list);
		assertTrue(list.size()>0);
	}

	@Test
	public void testSubFilter () {
		String snippet = "<?xml version=\"1.0\"?>\n"
			+ "<doc><its:rules version=\"1.0\" xmlns:its=\"http://www.w3.org/2005/11/its\" "
			+ "xmlns:ix=\""+Namespaces.ITSX_NS_URI+"\">"
			+ "<ix:subFilterRule selector=\"//sf\" subFilter=\"okf_xml\"/>"
			+ "</its:rules>"
			+ "<p>Text1</p>"
			+ "<sf>&lt;doc2>"
			+ "&lt;its:rules version=\"1.0\" xmlns:its=\"http://www.w3.org/2005/11/its\">"
			+ "&lt;its:translateRule selector=\"//nt\" translate=\"no\"/>"
			+ "&lt;/its:rules>"
			+ "&lt;nt>no-trans&lt;/nt>&lt;p>Text2&lt;/p>&lt;/doc2>\n</sf>"
			+ "</doc>";
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<doc><its:rules version=\"1.0\" xmlns:its=\"http://www.w3.org/2005/11/its\" "
			+ "xmlns:ix=\""+Namespaces.ITSX_NS_URI+"\">"
			+ "<ix:subFilterRule selector=\"//sf\" subFilter=\"okf_xml\"/>"
			+ "</its:rules>"
			+ "<p>Text1</p>"
			+ "<sf>&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt;\n&lt;doc2&gt;"
			+ "&lt;its:rules version=&quot;1.0&quot; xmlns:its=&quot;http://www.w3.org/2005/11/its&quot;&gt;"
			+ "&lt;its:translateRule selector=&quot;//nt&quot; translate=&quot;no&quot;/&gt;"
			+ "&lt;/its:rules&gt;"
			+ "&lt;nt&gt;no-trans&lt;/nt&gt;&lt;p&gt;Text2&lt;/p&gt;&lt;/doc2&gt;</sf>"
			+ "</doc>";

		ArrayList<Event> list = getEvents(snippet);
		ITextUnit tu = FilterTestDriver.getTextUnit(list, 1);
		assertEquals("Text1", tu.getSource().toString());
		tu = FilterTestDriver.getTextUnit(list, 2);
		assertEquals("Text2", tu.getSource().toString());

//TODO: Resolve this issue		
//		assertEquals(expected, FilterTestDriver.generateOutput(list,
//			filter.getEncoderManager(), locEN));
	}

    @Test
    public void testSubFilterIds() {
        String snippet = "<?xml version=\"1.0\"?>\n"
                + "<doc><its:rules version=\"1.0\" xmlns:its=\"http://www.w3.org/2005/11/its\" "
                + "xmlns:ix=\"" + Namespaces.ITSX_NS_URI + "\">"
                + "<ix:subFilterRule selector=\"//sf\" subFilter=\"okf_xml\"/>"
                + "</its:rules>" + "<sf>&lt;p>Text2&lt;/p></sf>" 
                + "<sf>&lt;p>Text3&lt;/p></sf>" + "</doc>";

        ArrayList<Event> list = getEvents(snippet);
        List<ITextUnit> tus = FilterTestDriver.filterTextUnits(list);
        Set<String> ids = new HashSet<>();
        for (ITextUnit tu : tus) {
            ids.add(tu.getId());
        }
        assertEquals(tus.size(), ids.size());
    }

	@Test
	public void testStartDocument () {
		assertTrue("Problem in StartDocument", FilterTestDriver.testStartDocument(filter,
			new InputDocument(root.in("/test01.xml").toString(), null),
			"UTF-8", locEN, locEN));
	}
	
	@Test
	public void testStartDocumentFromList () {
		String snippet = "<?xml version=\"1.0\"?>\r"
			+ "<doc>text</doc>";
		StartDocument sd = FilterTestDriver.getStartDocument(getEvents(snippet));
		assertNotNull(sd);
		assertNotNull(sd.getEncoding());
		assertNotNull(sd.getType());
		assertNotNull(sd.getMimeType());
		assertNotNull(sd.getLocale());
		assertEquals("\r", sd.getLineBreak());
	}
	
	@Test
	public void testOutputBasic_Comment () {
		String snippet = "<?xml version=\"1.0\"?>\n"
			+ "<doc><!--c--></doc>";
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<doc><!--c--></doc>";
		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(snippet),
			filter.getEncoderManager(), locEN));
	}
	
	@Test
	public void testOutputBasic_PI () {
		String snippet = "<?xml version=\"1.0\"?>\n"
			+ "<doc><?pi ?></doc>";
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<doc><?pi ?></doc>";
		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(snippet),
			filter.getEncoderManager(), locEN));
	}
	
	@Test
	public void testOutputBasic_OneChar () {
		String snippet = "<?xml version=\"1.0\"?>\n"
			+ "<doc>T</doc>";
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<doc>T</doc>";
		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(snippet),
			filter.getEncoderManager(), locEN));
	}
	
	@Test
	public void testOutputBasic_EmptyRoot () {
		String snippet = "<?xml version=\"1.0\"?>\n"
			+ "<doc/>";
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<doc/>";
		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(snippet),
			filter.getEncoderManager(), locEN));
	}
	
	@Test
	public void testOutputSimpleContent () {
		String snippet = "<?xml version=\"1.0\"?>\n"
			+ "<doc><p>test</p></doc>";
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<doc><p>test</p></doc>";
		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(snippet),
			filter.getEncoderManager(), locEN));
	}

	@Test
	public void testOutputSimpleContent_WithEscapes () {
		String snippet = "<?xml version=\"1.0\"?>\n"
			+ "<doc><p>&amp;=amp, &lt;=lt, &quot;=quot..</p></doc>";
		// No translatable attributes -> allow not escaped apos and quote
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<doc><p>&amp;=amp, &lt;=lt, &quot;=quot..</p></doc>";
		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(snippet),
			filter.getEncoderManager(), locEN));
	}
	
	//TODO: Implement language handling
/*	@Test
	public void testOutputSimpleContent_WithLang () {
		String snippet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<doc xml:lang='en'>test</doc>";
		String expect = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<doc xml:lang='FR'>test</doc>";
		//TODO: Implement replacement of the lang value
		//assertEquals(expect, FilterTestDriver.generateOutput(getEvents(snippet), snippet, "FR"));
	}*/

	@Test
	public void testOutputTargetPointer () {
		String snippet = "<?xml version=\"1.0\"?>\n"
			+ "<doc xmlns:its=\"http://www.w3.org/2005/11/its\"><its:rules version=\"2.0\">"
			+ "<its:translateRule selector=\"/doc\" translate=\"no\"/>"
			+ "<its:translateRule selector=\"//item/src\" translate=\"yes\"/>"
			+ "<its:targetPointerRule selector=\"//item/src\" targetPointer=\"../trg\"/>"
			+ "<its:translateRule selector=\"//entry/@src\" translate=\"yes\"/>"
			+ "<its:targetPointerRule selector=\"//entry/@src\" targetPointer=\"./@trg\"/>"
			+ "</its:rules>\n"
			+ "<item><src>Text1</src><trg/></item>\n"
			+ "<entry src=\"Text2\" trg=\"\"/>\n"
			+ "</doc>";
		String expect = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<doc xmlns:its=\"http://www.w3.org/2005/11/its\"><its:rules version=\"2.0\">"
			+ "<its:translateRule selector=\"/doc\" translate=\"no\"/>"
			+ "<its:translateRule selector=\"//item/src\" translate=\"yes\"/>"
			+ "<its:targetPointerRule selector=\"//item/src\" targetPointer=\"../trg\"/>"
			+ "<its:translateRule selector=\"//entry/@src\" translate=\"yes\"/>"
			+ "<its:targetPointerRule selector=\"//entry/@src\" targetPointer=\"./@trg\"/>"
			+ "</its:rules>\n"
			+ "<item><src>Text1</src><trg>TEXT1</trg></item>\n"
			+ "<entry src=\"Text2\" trg=\"TEXT2\"/>\n"
			+ "</doc>";
		
		// Check extraction
		ArrayList<Event> list = getEvents(snippet);
		
		ITextUnit tu = FilterTestDriver.getTextUnit(list, 1);
		assertEquals("Text1", tu.getSource().getCodedText());
		tu.setTarget(LocaleId.FRENCH, new TextContainer(
			tu.getSource().getFirstContent().getCodedText().toUpperCase()));
		assertEquals("TEXT1", tu.getTarget(LocaleId.FRENCH).getCodedText());
		
		tu = FilterTestDriver.getTextUnit(list, 2);
		assertEquals("Text2", tu.getSource().getCodedText());
		tu.setTarget(LocaleId.FRENCH, new TextContainer(
			tu.getSource().getFirstContent().getCodedText().toUpperCase()));
		assertEquals("TEXT2", tu.getTarget(LocaleId.FRENCH).getCodedText());
		
		// Check output
		assertEquals(expect, FilterTestDriver.generateOutput(list, filter.getEncoderManager(), LocaleId.FRENCH));
	}
	
	@Test
	public void testOutputTargetPointerWithExistingTarget () {
		String snippet = "<?xml version=\"1.0\"?>\n"
			+ "<doc xmlns:its=\"http://www.w3.org/2005/11/its\"><its:rules version=\"2.0\">"
			+ "<its:translateRule selector=\"/doc\" translate=\"no\"/>"
			+ "<its:translateRule selector=\"//item/src\" translate=\"yes\"/>"
			+ "<its:targetPointerRule selector=\"//item/src\" targetPointer=\"../trg\"/>"
			+ "<its:translateRule selector=\"//entry/@src\" translate=\"yes\"/>"
			+ "<its:targetPointerRule selector=\"//entry/@src\" targetPointer=\"./@trg\"/>"
			+ "</its:rules>"
			+ "<item><src>Text</src><trg>ORITRG</trg></item>"
			+ "<entry src=\"Text2\" trg=\"ORITRG2\"/>"
			+ "</doc>";
		String expect = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<doc xmlns:its=\"http://www.w3.org/2005/11/its\"><its:rules version=\"2.0\">"
			+ "<its:translateRule selector=\"/doc\" translate=\"no\"/>"
			+ "<its:translateRule selector=\"//item/src\" translate=\"yes\"/>"
			+ "<its:targetPointerRule selector=\"//item/src\" targetPointer=\"../trg\"/>"
			+ "<its:translateRule selector=\"//entry/@src\" translate=\"yes\"/>"
			+ "<its:targetPointerRule selector=\"//entry/@src\" targetPointer=\"./@trg\"/>"
			+ "</its:rules>"
			+ "<item><src>Text</src><trg>ORITRG</trg></item>"
			+ "<entry src=\"Text2\" trg=\"ORITRG2\"/>"
			+ "</doc>";
		
		// Check extraction
		ArrayList<Event> list = getEvents(snippet);
		ITextUnit tu = FilterTestDriver.getTextUnit(list, 1);
		assertEquals("Text", tu.getSource().getCodedText());
		assertEquals("ORITRG", tu.getTarget(LocaleId.FRENCH).getCodedText());
		
		tu = FilterTestDriver.getTextUnit(list, 2);
		assertEquals("Text2", tu.getSource().getCodedText());
		assertEquals("ORITRG2", tu.getTarget(LocaleId.FRENCH).getCodedText());
		// Check output
		assertEquals(expect, FilterTestDriver.generateOutput(list,
		filter.getEncoderManager(), LocaleId.FRENCH));
	}
	
	@Test
	public void testOutputTargetPointerWithInlineCodes () {
		String snippet = "<?xml version=\"1.0\"?>\n"
			+ "<doc xmlns:its=\"http://www.w3.org/2005/11/its\"><its:rules version=\"2.0\">"
			+ "<its:translateRule selector=\"/doc\" translate=\"no\"/>"
			+ "<its:translateRule selector=\"//item/src\" translate=\"yes\"/>"
			+ "<its:targetPointerRule selector=\"//item/src\" targetPointer=\"../trg\"/>"
			+ "<its:withinTextRule selector=\"//b|//z\" withinText=\"yes\"/>"
			+ "</its:rules>"
			+ "<item><src><b>Text1a</b><z/> Text1b</src><trg><b>Trg1a</b><z/> Trg1b</trg></item>"
			+ "<item><src><b>Text2a</b><z/> Text2b</src><trg/></item>"
			+ "</doc>";
		String expect = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<doc xmlns:its=\"http://www.w3.org/2005/11/its\"><its:rules version=\"2.0\">"
			+ "<its:translateRule selector=\"/doc\" translate=\"no\"/>"
			+ "<its:translateRule selector=\"//item/src\" translate=\"yes\"/>"
			+ "<its:targetPointerRule selector=\"//item/src\" targetPointer=\"../trg\"/>"
			+ "<its:withinTextRule selector=\"//b|//z\" withinText=\"yes\"/>"
			+ "</its:rules>"
			+ "<item><src><b>Text1a</b><z/> Text1b</src><trg><b>Trg1a</b><z/> Trg1b</trg></item>"
			+ "<item><src><b>Text2a</b><z/> Text2b</src><trg><b>Text2a</b><z/> Text2b</trg></item>"
			+ "</doc>";
		
		// Check extraction
		GenericContent fmt = new GenericContent();
		ArrayList<Event> list = getEvents(snippet);
		ITextUnit tu = FilterTestDriver.getTextUnit(list, 1);
		assertEquals("<1>Text1a</1><2/> Text1b", fmt.setContent(tu.getSource().getFirstContent()).toString());
//Not working yet
//TODO		assertEquals("<1>Trg1a</1><2/> Trg1b", fmt.setContent(tu.getTarget(LocaleId.FRENCH).getFirstContent()).toString());
		
		tu = FilterTestDriver.getTextUnit(list, 2);
		assertEquals("<1>Text2a</1><2/> Text2b", fmt.setContent(tu.getSource().getFirstContent()).toString());
//Not working yet
//TODO		assertEquals("<1>Text2a</1><2/> Text2b", fmt.setContent(tu.getTarget(LocaleId.FRENCH).getFirstContent()).toString());
		// Check output
//Not working yet
//TODO		assertEquals(expect, FilterTestDriver.generateOutput(list, filter.getEncoderManager(), LocaleId.FRENCH));
	}
	
	@Test
	public void testOutputSupplementalChars () {
		String snippet = "<?xml version=\"1.0\"?>\n"
			+ "<p>[&#x20000;]=U+D840,U+DC00</p>";
		String expect = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<p>[\uD840\uDC00]=U+D840,U+DC00</p>";
		assertEquals(expect, FilterTestDriver.generateOutput(getEvents(snippet),
			filter.getEncoderManager(), locEN));
	}

	@DataProvider
	public static Object[][] CDATAEntryDataProvider() {
		return new Object[][]{
				{
						false,
						"[t1. &=amp, <=lt, &#xaaa;=not-a-ncr t3.]",
						"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
								+ "<doc><p>t1. &amp;=amp, &lt;=lt, &amp;#xaaa;=not-a-ncr t3.</p></doc>"
				},
				{
						true,
						"[t1. <1>&=amp, <=lt, &#xaaa;=not-a-ncr</1> t3.]",
						"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
								+ "<doc><p>t1. <![CDATA[&=amp, <=lt, &#xaaa;=not-a-ncr]]> t3.</p></doc>"
				},
		};
	}

	@Test
	@UseDataProvider("CDATAEntryDataProvider")
	public void testCDATAEntry(boolean inlineCdata, String expectedContent, String expectedOutput) {
		String input = "<?xml version=\"1.0\"?>\n"
				+ "<doc><p>t1. <![CDATA[&=amp, <=lt, &#xaaa;=not-a-ncr]]> t3.</p></doc>";

		boolean originalInlineCdata = filter.getParameters().inlineCdata;
		filter.getParameters().inlineCdata = inlineCdata;

		List<Event> events = getEvents(input);

		ITextUnit tu = FilterTestDriver.getTextUnit(events, 1);
		assertNotNull(tu);

		assertEquals(expectedContent, fmt.printSegmentedContent(tu.getSource(), true));
		assertEquals(expectedOutput, FilterTestDriver.generateOutput(events, filter.getEncoderManager(), locEN));

		filter.getParameters().inlineCdata = originalInlineCdata;
	}

	@Test
	public void testCREntity () {
		String snippet = "<?xml version=\"1.0\"?>\n"
			+ "<doc><p>t1\n\n   &#xD;&#13;   t2</p></doc>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertNotNull(tu);
		String str = tu.getSource().toString();
		assertEquals("t1 \r\r t2", str);
	}

	@Test
	public void testCREntityOutput () {
		String snippet = "<?xml version=\"1.0\"?>\n"
			+ "<doc><p>t1\n\n   &#xD;&#13;   t2</p></doc>";
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<doc><p>t1 &#13;&#13; t2</p></doc>";
		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(snippet),
			filter.getEncoderManager(), locEN));
	}

	@Test
	public void testCommentParsing () {
		String snippet = "<?xml version=\"1.0\"?>\n"
			+ "<doc><p>t1 <!--comment--> t2</p></doc>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertNotNull(tu);
		assertEquals(fmt.setContent(tu.getSource().getFirstContent()).toString(), "t1 <1/> t2");
	}

	@Test
	public void testOutputComment () {
		String snippet = "<?xml version=\"1.0\"?>\n"
			+ "<doc><p>t1 <!--comment--> t2</p></doc>";
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<doc><p>t1 <!--comment--> t2</p></doc>";
		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(snippet),
			filter.getEncoderManager(), locEN));
	}

	@Test
	public void testPIParsing () {
		String snippet = "<?xml version=\"1.0\"?>\n"
			+ "<doc><p>t1 <?abc attr=\"value\"?> t2</p></doc>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertNotNull(tu);
		assertEquals(fmt.setContent(tu.getSource().getFirstContent()).toString(), "t1 <1/> t2");
	}
	
	@Test
	public void testOutputPI () {
		String snippet = "<?xml version=\"1.0\"?>\n"
			+ "<doc><p>t1 <?abc attr=\"value\"?> t2</p></doc>";
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<doc><p>t1 <?abc attr=\"value\"?> t2</p></doc>";
		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(snippet),
			filter.getEncoderManager(), locEN));
	}

	@Test
	public void testOutputWhitespacesPreserve () {
		String snippet = "<?xml version=\"1.0\"?>\n"
			+ "<doc><p>part 1\npart 2</p>"
			+ "<p xml:space=\"preserve\">part 1\npart 2</p></doc>";
		String expect = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<doc><p>part 1 part 2</p>"
			+ "<p xml:space=\"preserve\">part 1\npart 2</p></doc>";
		assertEquals(expect, FilterTestDriver.generateOutput(getEvents(snippet),
			filter.getEncoderManager(), locEN));
	}
	
	@Test
	public void testOutputWhitespacesDefault () {
		String snippet = "<?xml version=\"1.0\"?>\n"
			+ "<p>part 1\npart 2\n  part3\n\t part4</p>";
		String expect = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<p>part 1 part 2 part3 part4</p>";
		assertEquals(expect, FilterTestDriver.generateOutput(getEvents(snippet),
			filter.getEncoderManager(), locEN));
	}
	
	@Test
	public void testOutputWhitespacesITS () {
		String snippet = "<?xml version=\"1.0\"?>\n"
			+ "<doc><its:rules version=\"1.0\" xmlns:its=\"http://www.w3.org/2005/11/its\" xmlns:itsx=\"http://www.w3.org/2008/12/its-extensions\">"
			+ "<its:translateRule itsx:whiteSpaces=\"preserve\" selector=\"//pre\" translate=\"yes\"/></its:rules>"
			+ "<p>[  \t]</p><pre>[  \t]</pre></doc>";
		String expect = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<doc><its:rules version=\"1.0\" xmlns:its=\"http://www.w3.org/2005/11/its\" xmlns:itsx=\"http://www.w3.org/2008/12/its-extensions\">"
			+ "<its:translateRule itsx:whiteSpaces=\"preserve\" selector=\"//pre\" translate=\"yes\"/></its:rules>"
			+ "<p>[ ]</p><pre>[  \t]</pre></doc>";
		assertEquals(expect, FilterTestDriver.generateOutput(getEvents(snippet),
			filter.getEncoderManager(), locEN));
	}
	
	@Test
	public void testOutputStandaloneYes () {
		String snippet = "<?xml version=\"1.0\" standalone=\"yes\"?>\n"
			+ "<doc>text</doc>";
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
			+ "<doc>text</doc>";
		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(snippet),
			filter.getEncoderManager(), locEN));
	}

	@Test
	public void testSeveralUnits () {
		String snippet = "<?xml version=\"1.0\"?>\n"
			+ "<doc><p>text 1</p><p>text 2</p><p>text 3</p></doc>";
		ArrayList<Event> list = getEvents(snippet);
		ITextUnit tu = FilterTestDriver.getTextUnit(list, 1);
		assertNotNull(tu);
		assertEquals("text 1", tu.getSource().toString());
		tu = FilterTestDriver.getTextUnit(list, 2);
		assertNotNull(tu);
		assertEquals("text 2", tu.getSource().toString());
		tu = FilterTestDriver.getTextUnit(list, 3);
		assertNotNull(tu);
		assertEquals("text 3", tu.getSource().toString());
	}
	
	@Test
	public void testTranslatableAttributes () {
		String snippet = "<?xml version=\"1.0\"?>\n"
			+ "<doc><its:rules version=\"1.0\" xmlns:its=\"http://www.w3.org/2005/11/its\">"
			+ "<its:translateRule selector=\"//*/@text\" translate=\"yes\"/></its:rules>"
			+ "<p text=\"value 1\">text 1</p><p>text 2</p><p>text 3</p></doc>";
		ArrayList<Event> list = getEvents(snippet);
		ITextUnit tu = FilterTestDriver.getTextUnit(list, 1);
		assertNotNull(tu);
		assertEquals("value 1", tu.getSource().toString());
	}

	@Test
	public void testLocQualityRatingLocal () {
		String snippet = "<?xml version=\"1.0\"?>\n"
			+ "<doc xml:lang='nl' xmlns:its=\"http://www.w3.org/2005/11/its\" its:version=\"2.0\""
			+ " its:locQualityRatingScore='100' its:locQualityRatingScoreThreshold='95'"
			+ " its:locQualityRatingProfileRef='http://example.org/qaModel/v13'>"
			+ "<title>text 1</title>"
			+ "<p its:locQualityRatingVote='-2' its:locQualityRatingVoteThreshold='0'>text 2</p>"
			+ "</doc>";
		ArrayList<Event> list = getEvents(snippet);
		ITextUnit tu = FilterTestDriver.getTextUnit(list, 1);
		assertNotNull(tu);
		assertEquals("text 1", tu.getSource().toString());
		GenericAnnotations anns = tu.getSource().getAnnotation(GenericAnnotations.class);
		GenericAnnotation ga = anns.getFirstAnnotation(GenericAnnotationType.LQR);
		assertEquals(100.0, ga.getDouble(GenericAnnotationType.LQR_SCORE), 0.0);
		assertEquals(95.0, ga.getDouble(GenericAnnotationType.LQR_SCORETHRESHOLD), 0.0);
		assertEquals("http://example.org/qaModel/v13", ga.getString(GenericAnnotationType.LQR_PROFILEREF));
		tu = FilterTestDriver.getTextUnit(list, 2);
		assertEquals("text 2", tu.getSource().toString());
		anns = tu.getSource().getAnnotation(GenericAnnotations.class);
		ga = anns.getFirstAnnotation(GenericAnnotationType.LQR);
		assertEquals(-2, (int)ga.getInteger(GenericAnnotationType.LQR_VOTE));
		assertEquals(0, (int)ga.getInteger(GenericAnnotationType.LQR_VOTETHRESHOLD));
		assertEquals(null, ga.getString(GenericAnnotationType.LQR_PROFILEREF)); // No value because complete override
	}

	@Test
	public void testMTConfidence () {
		String snippet = "<?xml version=\"1.0\"?>\n"
			+ "<doc xml:lang='nl' xmlns:its=\"http://www.w3.org/2005/11/its\" its:version=\"2.0\""
			+ " its:annotatorsRef=\"mt-confidence|file:///tools.xml#T1\""
			+ " its:mtConfidence='0.56'>"
			+ "<title>text 1</title>"
			+ "<p its:mtConfidence='0.78' its:annotatorsRef=\"mt-confidence|T2\">text 2</p>"
			+ "</doc>";
		ArrayList<Event> list = getEvents(snippet);
		ITextUnit tu = FilterTestDriver.getTextUnit(list, 1);
		assertNotNull(tu);
		assertEquals("text 1", tu.getSource().toString());
		GenericAnnotations anns = tu.getAnnotation(GenericAnnotations.class);
		GenericAnnotation ga = anns.getFirstAnnotation(GenericAnnotationType.MTCONFIDENCE);
		assertEquals(0.56, ga.getDouble(GenericAnnotationType.MTCONFIDENCE_VALUE), 0.0);
		assertEquals("file:///tools.xml#T1", ga.getString(GenericAnnotationType.ANNOTATORREF));
		
		tu = FilterTestDriver.getTextUnit(list, 2);
		assertEquals("text 2", tu.getSource().toString());
		anns = tu.getAnnotation(GenericAnnotations.class);
		ga = anns.getFirstAnnotation(GenericAnnotationType.MTCONFIDENCE);
		assertEquals(0.78, ga.getDouble(GenericAnnotationType.MTCONFIDENCE_VALUE), 0.0);
		assertEquals("T2", ga.getString(GenericAnnotationType.ANNOTATORREF));
	}

	@Test
	public void testTextAnalysis () {
		String snippet = "<?xml version=\"1.0\"?>\n"
			+ "<doc xmlns:its=\"http://www.w3.org/2005/11/its\" its:version=\"2.0\" its:annotatorsRef=\"text-analysis|http://enrycher.ijs.si\">"
			+ "<its:rules version='2.0'>"
			+ "<its:withinTextRule selector='//its:span' withinText='yes'/>"
			+ "</its:rules>"
			+ "<p its:taConfidence='0.7' its:taClassRef='http://nerd.eurecom.fr/ontology#Place'"  
			+ " its:taIdentRef='http://dbpedia.org/resource/Dublin'>Dublin</p>"
			+ "<p>The <its:span taSource='Wordnet3.0' taIdent='301467919'"
			+ " taConfidence='0.5'>capital</its:span> of Ireland.</p>"
			+ "</doc>";
		ArrayList<Event> list = getEvents(snippet);
		ITextUnit tu = FilterTestDriver.getTextUnit(list, 1);
		assertNotNull(tu);
		assertEquals("Dublin", tu.getSource().toString());
		GenericAnnotations anns = tu.getSource().getAnnotation(GenericAnnotations.class);
		GenericAnnotation ga = anns.getFirstAnnotation(GenericAnnotationType.TA);
		assertEquals(0.7, ga.getDouble(GenericAnnotationType.TA_CONFIDENCE), 0.0);
		assertEquals("REF:http://dbpedia.org/resource/Dublin", ga.getString(GenericAnnotationType.TA_IDENT));
		assertEquals("REF:http://nerd.eurecom.fr/ontology#Place", ga.getString(GenericAnnotationType.TA_CLASS));
		assertEquals("http://enrycher.ijs.si", ga.getString(GenericAnnotationType.ANNOTATORREF));
		tu = FilterTestDriver.getTextUnit(list, 2);
		Code code = tu.getSource().getFirstContent().getCode(0);
		anns = (GenericAnnotations)code.getAnnotation(GenericAnnotationType.GENERIC);
		ga = anns.getFirstAnnotation(GenericAnnotationType.TA);
		assertEquals(0.5, ga.getDouble(GenericAnnotationType.TA_CONFIDENCE), 0.0);
		assertEquals("301467919", ga.getString(GenericAnnotationType.TA_IDENT));
		assertEquals("Wordnet3.0", ga.getString(GenericAnnotationType.TA_SOURCE));
	}

	@Test
	public void testLocQualityLocalOnUnit () {
		String snippet = "<?xml version=\"1.0\"?>\n"
			+ "<doc its:version=\"2.0\" xmlns:its=\"http://www.w3.org/2005/11/its\">"
			+ "<p text=\"value 1\" its:locQualityIssueComment='comment'>text 1</p></doc>";
		ArrayList<Event> list = getEvents(snippet);
		ITextUnit tu = FilterTestDriver.getTextUnit(list, 1);
		assertNotNull(tu);
		assertEquals("text 1", tu.getSource().toString());
		GenericAnnotations anns = tu.getSource().getAnnotation(GenericAnnotations.class);
		GenericAnnotation ga = anns.getFirstAnnotation(GenericAnnotationType.LQI);
		assertEquals("comment", ga.getString(GenericAnnotationType.LQI_COMMENT));
	}

	@Test
	public void testLocQualityLocalOnCodes () {
		String snippet = "<?xml version=\"1.0\"?>\n"
			+ "<doc its:version=\"2.0\" xmlns:its=\"http://www.w3.org/2005/11/its\">"
			+ "<its:rules version='2.0'>"
			+ "<its:withinTextRule selector='//s' withinText='yes'/>"
			+ "</its:rules>"
			+ "<p its:locQualityIssueComment='issue-1'>text 1"
			+ "<s its:allowedCharacters='[abc]' its:locQualityIssueComment='issue-2'>bad</s>"
			+ " and "
			+ "<s its:locQualityIssueComment='issue-3'>more</s>"
			+ "</p></doc>";
		ArrayList<Event> list = getEvents(snippet);
		ITextUnit tu = FilterTestDriver.getTextUnit(list, 1);
		assertNotNull(tu);
		assertEquals("text 1<1>bad</1> and <2>more</2>",
			fmt.setContent(tu.getSource().getFirstContent()).toString());
		GenericAnnotations anns = tu.getSource().getAnnotation(GenericAnnotations.class);
		List<GenericAnnotation> res = anns.getAnnotations(GenericAnnotationType.LQI);
		assertEquals(3, res.size());
		assertEquals("issue-1", res.get(0).getString(GenericAnnotationType.LQI_COMMENT));
		assertEquals("issue-2", res.get(1).getString(GenericAnnotationType.LQI_COMMENT));
		assertEquals(8, (int)res.get(1).getInteger(GenericAnnotationType.LQI_XSTART));
		assertEquals(11, (int)res.get(1).getInteger(GenericAnnotationType.LQI_XEND));
		assertEquals("issue-3", res.get(2).getString(GenericAnnotationType.LQI_COMMENT));
		assertEquals(20, (int)res.get(2).getInteger(GenericAnnotationType.LQI_XSTART));
		assertEquals(24, (int)res.get(2).getInteger(GenericAnnotationType.LQI_XEND));
		// Check inline ones
		anns = (GenericAnnotations)tu.getSource().getFirstContent().getCodes().get(0).getAnnotation(GenericAnnotationType.GENERIC);
		assertNotNull(anns);
		assertEquals("[abc]", anns.getFirstAnnotation(GenericAnnotationType.ALLOWEDCHARS).getString(GenericAnnotationType.ALLOWEDCHARS_VALUE));
	}

	@Test
	public void testTerms () {
		String snippet = "<?xml version=\"1.0\"?>\n"
			+ "<doc its:version=\"2.0\" xmlns:its=\"http://www.w3.org/2005/11/its\" its:annotatorsRef=\"terminology|ABC\">"
			+ "<gloss its:translate='no'>"
			+ "<termInfo id='ti1'>term info 1</termInfo>"
			+ "<termInfo id='ti2'>term info 2</termInfo></gloss>"
			+ "<p>One <its:span withinText='yes' term='yes' termInfoRef='#ti1'>term</its:span>, and more.</p>"
			+ "<p><span its:withinText='yes' its:term='no' its:termInfoRef='#ti1'>not a term</span></p>"
			+ "<p its:term='yes' its:termInfoRef='#ti2' its:termConfidence=\"0.885\">term2</p>"
			+ "<p>Last <span its:withinText='yes' its:term='yes' its:termInfoRef='#ti2'>term</span>, and more.</p>"
			+ "</doc>";
		ArrayList<Event> list = getEvents(snippet);
		ITextUnit tu = FilterTestDriver.getTextUnit(list, 1);
		assertNotNull(tu);
		assertEquals("One <1>term</1>, and more.", fmt.setContent(tu.getSource().getFirstContent()).toString());
		Code code = tu.getSource().getFirstContent().getCode(0);
		GenericAnnotation ann = code.getGenericAnnotations().getFirstAnnotation(GenericAnnotationType.TERM);
		//TODO??? add term_value info? assertTrue("yes", ann.getBoolean(GenericAnnotationType.TERM));
		assertEquals("REF:#ti1", ann.getString(GenericAnnotationType.TERM_INFO));
		assertEquals("ABC", ann.getString(GenericAnnotationType.ANNOTATORREF));
		// Second inline
		tu = FilterTestDriver.getTextUnit(list, 2);
		code = tu.getSource().getFirstContent().getCode(0);
//TODO: should we get annotation on term='no'???
//		ann = code.getGenericAnnotations().getFirstAnnotation(GenericAnnotationType.TERM);
		// Term on p
		tu = FilterTestDriver.getTextUnit(list, 3);
		assertEquals("term2", tu.getSource().toString());
		ann = tu.getSource().getAnnotation(GenericAnnotations.class).getFirstAnnotation(GenericAnnotationType.TERM);
		assertEquals("REF:#ti2", ann.getString(GenericAnnotationType.TERM_INFO));
		assertEquals(0.885, ann.getDouble(GenericAnnotationType.TERM_CONFIDENCE), 0.0);
		assertEquals("ABC", ann.getString(GenericAnnotationType.ANNOTATORREF));
		// Inline term with <span>
		tu = FilterTestDriver.getTextUnit(list, 4);
		assertEquals("Last <1>term</1>, and more.", fmt.setContent(tu.getSource().getFirstContent()).toString());
		code = tu.getSource().getFirstContent().getCode(0);
		ann = code.getGenericAnnotations().getFirstAnnotation(GenericAnnotationType.TERM);
		assertEquals("REF:#ti2", ann.getString(GenericAnnotationType.TERM_INFO));
	}
	
	@Test
	public void testTranslatableAttributes2 () {
		String snippet = "<?xml version=\"1.0\"?>\n"
			+ "<doc><its:rules version=\"2.0\" xmlns:its=\"http://www.w3.org/2005/11/its\">"
			+ "<its:translateRule selector=\"//*/@text\" translate=\"yes\"/></its:rules>"
			+ "<p text=\"value 1 &quot;=quot\">text 1</p><p>text 2 &quot;=quot</p><p>text 3</p></doc>";
		ArrayList<Event> list = getEvents(snippet);
		ITextUnit tu = FilterTestDriver.getTextUnit(list, 1);
		assertNotNull(tu);
		assertEquals("value 1 \"=quot", tu.getSource().toString());
	}
	
	@Test
	public void testTranslatableAttributesOutput () {
		String snippet = "<?xml version=\"1.0\"?>\n"
			+ "<doc><its:rules version=\"1.0\" xmlns:its=\"http://www.w3.org/2005/11/its\">"
			+ "<its:translateRule selector=\"//*/@text\" translate=\"yes\"/></its:rules>"
			+ "<p NOTtext=\"&apos;=apos\" text=\"value 1 &apos;=apos, &quot;=quot\">text 1</p><p>text 2 &quot;=quot</p><p>text 3 &apos;=apos</p></doc>";
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<doc><its:rules version=\"1.0\" xmlns:its=\"http://www.w3.org/2005/11/its\">"
			+ "<its:translateRule selector=\"//*/@text\" translate=\"yes\"/></its:rules>"
			+ "<p NOTtext=\"'=apos\" text=\"value 1 '=apos, &quot;=quot\">text 1</p><p>text 2 &quot;=quot</p><p>text 3 '=apos</p></doc>";
		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(snippet),
			filter.getEncoderManager(), locEN));
	}
	
	@Test
	public void testTranslatableAttributesOutputAllowUnescapedQuoteButEscape () {
		String snippet = "<?xml version=\"1.0\"?>\n"
			+ "<doc><its:rules version=\"2.0\" xmlns:its=\"http://www.w3.org/2005/11/its\">"
			+ "<its:translateRule selector=\"//*/@text\" translate=\"yes\"/></its:rules>"
			+ "<p NOTtext='value 1 &apos;=apos, &quot;=quot'>text 1</p><p>text 2 &quot;=quot</p><p>text 3 &apos;=apos</p></doc>";
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<doc><its:rules version=\"2.0\" xmlns:its=\"http://www.w3.org/2005/11/its\">"
			+ "<its:translateRule selector=\"//*/@text\" translate=\"yes\"/></its:rules>"
			+ "<p NOTtext=\"value 1 '=apos, &quot;=quot\">text 1</p><p>text 2 &quot;=quot</p><p>text 3 '=apos</p></doc>";
		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(snippet),
			filter.getEncoderManager(), locEN));
	}
	
	@Test
	public void testTranslatableAttributesOutputAllowUnescapedQuote () {
		String snippet = "<?xml version=\"1.0\"?>\n"
			+ "<doc><its:rules version=\"2.0\" xmlns:its=\"http://www.w3.org/2005/11/its\">"
			+ "<its:translateRule selector=\"//*/@text\" translate=\"yes\"/>"
			+ "</its:rules>"
			+ "<p NOTtext='value 1 &apos;=apos, &quot;=quot'>text 1</p><p>text 2 &quot;=quot</p><p>text 3 &apos;=apos</p></doc>";
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<doc><its:rules version=\"2.0\" xmlns:its=\"http://www.w3.org/2005/11/its\">"
			+ "<its:translateRule selector=\"//*/@text\" translate=\"yes\"/>"
			+ "</its:rules>"
			+ "<p NOTtext=\"value 1 '=apos, &quot;=quot\">text 1</p><p>text 2 \"=quot</p><p>text 3 '=apos</p></doc>";
		
		String paramData = "<?xml version=\"1.0\"?>\n"
			+ "<its:rules version=\"2.0\" xmlns:its=\"http://www.w3.org/2005/11/its\""
			+ " xmlns:zzz=\"okapi-framework:xmlfilter-options\">"
			+ "<zzz:options escapeQuotes=\"no\"/></its:rules>";
		filter.getParameters().fromString(paramData);
		
		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(snippet),
			filter.getEncoderManager(), locEN));
	}
	
	@Test
	public void testOpenTwice () throws URISyntaxException {
		RawDocument rawDoc = new RawDocument(root.in("/test01.xml").asUri(), "windows-1252", locEN);
		filter.open(rawDoc);
		filter.close();
		filter.open(rawDoc);
		filter.close();
	}
	
	@Test
	public void testDoubleExtraction () throws URISyntaxException {
		// Read all files in the data directory
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();

		list.add(new InputDocument(root.in("/test01.xml").toString(), null));
		list.add(new InputDocument(root.in("/test02.xml").toString(), null));
		list.add(new InputDocument(root.in("/test03.xml").toString(), null));
		list.add(new InputDocument(root.in("/test04.xml").toString(), null));
		list.add(new InputDocument(root.in("/test05.xml").toString(), null));
		list.add(new InputDocument(root.in("/test06.xml").toString(), null));
		list.add(new InputDocument(root.in("/LocNote-1.xml").toString(), null));
		list.add(new InputDocument(root.in("/LocNote-2.xml").toString(), null));
		list.add(new InputDocument(root.in("/LocNote-3.xml").toString(), null));
		list.add(new InputDocument(root.in("/LocNote-4.xml").toString(), null));
		list.add(new InputDocument(root.in("/LocNote-5.xml").toString(), null));
		list.add(new InputDocument(root.in("/LocNote-6.xml").toString(), null));
		list.add(new InputDocument(root.in("/AndroidTest1.xml").toString(), "okf_xml@AndroidStrings.fprm"));
		list.add(new InputDocument(root.in("/AndroidTest2.xml").toString(), "okf_xml@AndroidStrings.fprm"));
		list.add(new InputDocument(root.in("/AndroidTest3.xml").toString(), "okf_xml@AndroidStrings.fprm"));
		list.add(new InputDocument(root.in("/JavaProperties.xml").toString(), "okf_xml@JavaProperties.fprm"));
		list.add(new InputDocument(root.in("/TestMultiLang.xml").toString(), null));
		list.add(new InputDocument(root.in("/Test01.resx").toString(), "okf_xml@RESX.fprm"));
		list.add(new InputDocument(root.in("/test_with_placeholders.resx").toString(), "okf_xml@RESX.fprm"));	
		list.add(new InputDocument(root.in("/MozillaRDFTest01.rdf").toString(), "okf_xml@MozillaRDF.fprm"));
		list.add(new InputDocument(root.in("/XRTT-Source1.xml").toString(), null));
		list.add(new InputDocument(root.in("/TestCDATA1.xml").toString(), null));
		list.add(new InputDocument(root.in("/test07.xml").toString(), null));
		list.add(new InputDocument(root.in("/test08_utf8nobom.xml").toString(), null));
		list.add(new InputDocument(root.in("/test09.xml").toString(), null));
		list.add(new InputDocument(root.in("/AppleStringsdictTest.stringsdict").toString(), "okf_xml@AppleStringsdict.fprm"));

		RoundTripComparison rtc = new RoundTripComparison();
		assertTrue(rtc.executeCompare(filter, list, "UTF-8", locEN, locEN));
	}
	
	@Test
	public void testSingleTest() throws URISyntaxException {
		// Read all files in the data directory
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
		list.add(new InputDocument(root.in("/its_bug_470.xml").toString(), "okf_xml@470.fprm"));

		RoundTripComparison rtc = new RoundTripComparison();
		assertTrue(rtc.executeCompare(filter, list, "UTF-8", locEN, locEN));
	}
	
	@Test
	public void testCodeFinderOnRESX ()
		throws MalformedURLException
	{
		RawDocument rawDoc = new RawDocument(root.in("/test_with_placeholders.resx").asUri(), "UTF-8", locEN);
		IParameters params = filter.getParameters();
		params.load(root.in("/okf_xml@RESX.fprm").asUrl(), false);
		filter.open(rawDoc);
		List<Event>list = new ArrayList<>();
		while (filter.hasNext()) {
			list.add(filter.next());
		}
		filter.close();
		ITextUnit tu = FilterTestDriver.getTextUnit(list, 1);
		assertNotNull(tu);
		List<Code> codes = tu.getSource().getFirstContent().getCodes();
		assertEquals(6, codes.size());
		assertEquals("&lt;b&gt;", codes.get(0).getData());
		assertEquals("&lt;/b&gt;", codes.get(1).getData());
		assertEquals("{1}", codes.get(2).getData());
		assertEquals("&lt;a href=&quot;test&quot;&gt;", codes.get(3).getData());
		assertEquals("&lt;/a&gt;", codes.get(4).getData());
		assertEquals("{dbName}", codes.get(5).getData());
	}

	private ArrayList<Event> getEvents(String snippet) {
		IFilterConfigurationMapper fcMapper = new FilterConfigurationMapper();
		fcMapper.addConfigurations(XMLFilter.class.getCanonicalName());
		filter.setFilterConfigurationMapper(fcMapper);
		return FilterTestDriver.getEvents(filter, snippet, locEN, LocaleId.FRENCH);
	}

}
