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
============================================================================*/

package net.sf.okapi.filters.ts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.MimeTypeMapper;
import net.sf.okapi.common.TestUtil;
import net.sf.okapi.common.filters.FilterTestDriver;
import net.sf.okapi.common.filters.InputDocument;
import net.sf.okapi.common.filters.RoundTripComparison;
import net.sf.okapi.common.filterwriter.GenericFilterWriter;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.DocumentPart;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.StartGroup;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.xml.sax.SAXException;

@RunWith(JUnit4.class)
public class TsFilterTest {

	private TsFilter filter;
	private String root;
	private LocaleId locENUS = LocaleId.fromString("en-us");
	private LocaleId locFRFR = LocaleId.fromString("fr-fr");
	private LocaleId locEN = LocaleId.fromString("en");
	private LocaleId locFR = LocaleId.fromString("fr");
	
	private FilterTestDriver testDriver;
	
	String completeTs = "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\"?>\r" +
	"<!DOCTYPE TS>\r" +
	"<!-- comment -->\r" +
	"<TS version=\"4.5.1\" sourcelanguage=\"en-us\" language=\"fr-fr\">\r" +
	"<defaultcodec>hello defaultcodec</defaultcodec>\r" +
	"<extra-loc-blank>hello extra-loc-blank</extra-loc-blank>\r" +
	"<context encoding=\"utf-8\">\r" +
	"<name>context name 1</name>\r" +
	"<comment>context comment 1</comment>\r" +
	"<context encoding=\"utf-8\">\r" +
	"<name>context name 2</name>\r" +
	"<comment>context comment 2</comment>\r" +
	"<message id=\"1\" encoding=\"utf-8\" numerus=\"no\">\r" +
	"<location filename=\"test.ts\" line=\"55\"/>\r" +
	"<source>hello <byte value=\"79\"/>world</source>\r" +
	"<oldsource>old hello world</oldsource>\r" +
	"<comment>old hello <byte value=\"79\"/>comment</comment>\r" +
	"<oldcomment>old hello old comment</oldcomment>\r" +
	"<extracomment>old hello extra comment</extracomment>\r" +
	"<translatorcomment>old hello translator comment</translatorcomment>\r" +
	"<translation type=\"unfinished\" variants=\"no\">hejsan <byte value=\"79\"/>varlden</translation>\r" +
	"<userdata>hello userdata</userdata>\r" +
	"<extra-loc-blank>hello extra-loc-blank</extra-loc-blank>\r" +
	"</message>\r" +
	"<message id=\"2\" encoding=\"utf-8\" numerus=\"no\">\r" +
	"<location filename=\"test.ts\" line=\"55\"/>\r" +
	"<source>hello <byte value=\"79\"/>world</source>\r" +
	"<oldsource>old hello world</oldsource>\r" +
	"<comment>old hello <byte value=\"79\"/>comment</comment>\r" +
	"<oldcomment>old hello old comment</oldcomment>\r" +
	"<extracomment>old hello extra comment</extracomment>\r" +
	"<translatorcomment>old hello translator comment</translatorcomment>\r" +
	"<translation type=\"obsolete\" variants=\"no\">hejsan <byte value=\"79\"/>varlden</translation>\r" +
	"<userdata>hello userdata</userdata>\r" +
	"<extra-loc-blank>hello extra-loc-blank</extra-loc-blank>\r" +
	"</message>\r" +
	"<![CDATA[hello cdata]]>\r"+
	"</context>\r" +
	"</context>\r" +
	"</TS>";
	String simpleSnippet = "<TS><context><name>AlarmAddLogDlg</name><message><source>Add Entry To System Log</source><translation type=\"unfinished\">Lagg till i system Loggen</translation></message></context></TS>";

	@Before
	public void setUp() throws ParserConfigurationException, SAXException, IOException {
		filter = new TsFilter();
		root = TestUtil.getParentDir(this.getClass(), "/Complete_valid_utf8_bom_crlf.ts");
		
		testDriver = new FilterTestDriver();
		testDriver.setDisplayLevel(0);
		testDriver.setShowSkeleton(true);
	}
	
	@Test
	public void StartDocument() {
		StartDocument sd = FilterTestDriver.getStartDocument(getEvents(completeTs, locENUS, locFRFR));
		
		assertEquals("Incorrect id","o1", sd.getId());
		assertEquals("Incorrect mimeType",MimeTypeMapper.TS_MIME_TYPE, sd.getMimeType());
		assertNull("Name should be null", sd.getName());
		assertEquals("Incorrect encoding", "utf-8", sd.getEncoding());
		assertEquals("Incorrect src language", locENUS, sd.getLocale());
		assertEquals("Incorrect linebreak", "\r", sd.getLineBreak());
		assertEquals("Incorrect multilingual", true, sd.isMultilingual());
		assertEquals("Incorrect utf8bom", false, sd.hasUTF8BOM());
		assertNotNull(sd.getFilterParameters());
		assertTrue(sd.getFilterWriter() instanceof GenericFilterWriter);
		assertEquals("utf-8", sd.getProperty("encoding").getValue());
		assertEquals(
				"<?xml version=\"1.0\" encoding=\"[#$$self$@%encoding]\"?>", 
				sd.getSkeleton().toString());
	}
	
	@Test
	public void DocumentPartTsPart() {
		DocumentPart dp = FilterTestDriver.getDocumentPart(getEvents(completeTs, locENUS, locFRFR), 1);
		
		assertEquals("o2", dp.getId());
		assertEquals("4.5.1", dp.getProperty("version").getValue());
		assertEquals(locENUS, dp.getProperty("sourcelanguage").getValue());
		assertEquals(locFRFR, dp.getProperty("language").getValue());
		
		assertEquals( 
				"\r<!DOCTYPE TS []>\r" +
				"<!-- comment -->\r" +
				"<TS version=\"4.5.1\" sourcelanguage=\"en-us\" language=\"fr-fr\">\r" +
				"<defaultcodec>hello defaultcodec</defaultcodec>\r" +
				"<extra-loc-blank>hello extra-loc-blank</extra-loc-blank>\r",
				dp.getSkeleton().toString());

		//q. should all iresources have mimetype set, or only the ones with text?		
	}
	
	@Test
	public void StartGroupContextPart() {
		StartGroup sg = FilterTestDriver.getGroup(getEvents(completeTs, locENUS, locFRFR), 1);

		assertEquals("o3", sg.getId());
		assertEquals("utf-8", sg.getProperty("encoding").getValue());
		//assertEquals("context name 1", sg.getProperty("name").getValue());
		//assertEquals("context comment 1", sg.getProperty("comment").getValue());
		
		assertEquals( 
				"<context encoding=\"utf-8\">\r" +
				"<name>context name 1</name>\r" +
				"<comment>context comment 1</comment>\r",
				sg.getSkeleton().toString());
		
		sg = FilterTestDriver.getGroup(getEvents(completeTs, locENUS, locFRFR), 2);

		assertEquals("o4", sg.getId());
		assertEquals("utf-8", sg.getProperty("encoding").getValue());
		//assertEquals("context name 2", sg.getProperty("name").getValue());
		//assertEquals("context comment 2", sg.getProperty("comment").getValue());
		assertEquals( 
				"<context encoding=\"utf-8\">\r" +
				"<name>context name 2</name>\r" +
				"<comment>context comment 2</comment>\r", 
				sg.getSkeleton().toString());
	}
	
	@Test
	public void TextUnitMessageUnfinished() {
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(completeTs, locENUS, locFRFR), 1);

		assertEquals("1", tu.getId());
		assertEquals(MimeTypeMapper.TS_MIME_TYPE, tu.getMimeType());
		assertEquals("1", tu.getName());
		assertFalse(tu.isEmpty());
		
		//--translator comments--
		assertTrue(tu.hasProperty(Property.TRANSNOTE));
		Property prop = tu.getProperty(Property.TRANSNOTE);
		assertEquals("old hello translator comment", prop.getValue());
		assertTrue(prop.isReadOnly());
		
		assertEquals("1", tu.getProperty("id").getValue());
		assertEquals("utf-8", tu.getProperty("encoding").getValue());
		assertEquals("no", tu.getProperty("numerus").getValue());
		assertEquals(0, tu.getSourcePropertyNames().size());
		assertEquals("no", tu.getTargetProperty(locFRFR, "variants").getValue());
		assertEquals("no", tu.getTargetProperty(locFRFR, "approved").getValue());
		assertEquals( 
				"<message id=\"1\" encoding=\"utf-8\" numerus=\"no\">\r" +
				"<location filename=\"test.ts\" line=\"55\"/>\r" +
				"<source>[#$$self$]</source>\r" +
				"<oldsource>old hello world</oldsource>\r" +
				"<comment>old hello <byte value=\"79\"/>comment</comment>\r" +
				"<oldcomment>old hello old comment</oldcomment>\r" +
				"<extracomment>old hello extra comment</extracomment>\r" +
				"<translatorcomment>old hello translator comment</translatorcomment>\r" +
				"<translation[#$$self$@%approved] variants=\"no\">[#$$self$]</translation>\r" +
				"<userdata>hello userdata</userdata>\r" +
				"<extra-loc-blank>hello extra-loc-blank</extra-loc-blank>\r" +
				"</message>", 
				tu.getSkeleton().toString());
	}
	
	@Test
	public void TestDecodeByteFalse() {
		
		Parameters params = (Parameters) filter.getParameters();
		params.setDecodeByteValues(false);
		
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(completeTs, locENUS, locFRFR), 1);

		assertEquals("hello <byte value=\"79\"/>world", tu.getSource().getFirstContent().toText());
		assertEquals("hejsan <byte value=\"79\"/>varlden", tu.getTarget(locFRFR).getFirstContent().toText());
	}
	
	@Test
	public void TestDecodeByteTrueDec() {
		
		String snippet = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r" +
		"<TS version=\"4.5.1\" sourcelanguage=\"en-us\" language=\"fr-fr\">\r" +
		"<defaultcodec>hello defaultcodec</defaultcodec>\r" +
		"<extra-loc-blank>hello extra-loc-blank</extra-loc-blank>\r" +
		"<context encoding=\"utf-8\">\r" +
		"<message id=\"1\" encoding=\"utf-8\" numerus=\"no\">\r" +
		"<source>hello <byte value=\"48\"/>world</source>\r" +
		"<translation variants=\"no\">hejsan <byte value=\"48\"/>varlden</translation>\r" +
		"</message>\r" +
		"</context>\r" +
		"</TS>";		
		
		Parameters params = (Parameters) filter.getParameters();
		params.setDecodeByteValues(true);
		
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet, locENUS, locFRFR), 1);

		assertEquals("hello 0world", tu.getSource().getFirstContent().toText());
		assertEquals("hejsan 0varlden", tu.getTarget(locFRFR).getFirstContent().toText());
	}
	
	@Test
	public void TestDecodeByteTrueHex() {
		
		String snippet = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r" +
		"<TS version=\"4.5.1\" sourcelanguage=\"en-us\" language=\"fr-fr\">\r" +
		"<defaultcodec>hello defaultcodec</defaultcodec>\r" +
		"<extra-loc-blank>hello extra-loc-blank</extra-loc-blank>\r" +
		"<context encoding=\"utf-8\">\r" +
		"<message id=\"1\" encoding=\"utf-8\" numerus=\"no\">\r" +
		"<source>hello <byte value=\"x31\"/>world</source>\r" +
		"<translation variants=\"no\">hejsan <byte value=\"x31\"/>varlden</translation>\r" +
		"</message>\r" +
		"</context>\r" +
		"</TS>";		
		
		Parameters params = (Parameters) filter.getParameters();
		params.setDecodeByteValues(true);
		
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet, locENUS, locFRFR), 1);

		assertEquals("hello 1world", tu.getSource().getFirstContent().toText());
		assertEquals("hejsan 1varlden", tu.getTarget(locFRFR).getFirstContent().toText());
	}

	@Test
	public void testTranslationStatus () {
		String snippet = "<?xml version='1.0' encoding='UTF-16BE'?>" +
		"<TS version=\"4.5.1\" sourcelanguage=\"en-us\" language=\"fr-fr\">\r" +
		"<context>\r" +
		"<name>contextName</name>\r" +
		"<message id=\"1\">\r" +
		"<source>source 1</source>\r" +
		"<translation >target 1</translation>\r" +
		"</message>\r" +
		"<message id=\"2\">\r" +
		"<source>source 2</source>\r" +
		"<translation type='unfinished'>target 2</translation>\r" +
		"</message>\r" +
		"<message id=\"3\">\r" +
		"<source>source 3</source>\r" +
		"<translation type='obsolete'>target 3</translation>\r" +
		"</message>\r" +
		"</context>\r" +
		"</TS>";
		
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet, locENUS, locFRFR), 1);
		assertNotNull(tu);
		Property prop = tu.getTargetProperty(locFRFR, Property.APPROVED);
		assertNotNull(prop);
		assertEquals("yes", prop.getValue());
		
		tu = FilterTestDriver.getTextUnit(getEvents(snippet, locENUS, locFRFR), 2);
		assertNotNull(tu);
		prop = tu.getTargetProperty(locFRFR, Property.APPROVED);
		assertNotNull(prop);
		assertEquals("no", prop.getValue());
		
		tu = FilterTestDriver.getTextUnit(getEvents(snippet, locENUS, locFRFR), 3);
		assertNull(tu); // entries with obsolete translation are not extracted (based on Qt manual)
	}

	@Test
	public void testInlineCodes () {
		String snippet = "<?xml version='1.0' encoding='UTF-16BE'?>" +
		"<TS version=\"4.5.1\" sourcelanguage=\"en-us\" language=\"fr-fr\">\r" +
		"<context>\r" +
		"<name>contextName</name>\r" +
		"<message id=\"1\">\r" +
		"<source>%s = %d <byte value=\"79\"/></source>\r" +
		"<translation>%s = %d <byte value=\"79\"/></translation>\r" +
		"</message>\r" +
		"</context>\r" +
		"</TS>";
		
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet, locENUS, locFRFR), 1);
		assertNotNull(tu);
		List<Code> list = tu.getSource().getFirstContent().getCodes();
		assertEquals(3, list.size());
		assertEquals("%s", list.get(0).getData());
		assertEquals("<byte value=\"79\"/>", list.get(1).getData());
		assertEquals("%d", list.get(2).getData());
	}		
	
	@Test
	public void testInlineCodesOutput () {
		String snippet = "<?xml version='1.0' encoding='UTF-16BE'?>\r" +
		"<TS sourcelanguage=\"en-us\" language=\"fr-fr\" version=\"4.5.1\">\r" +
		"<context>\r" +
		"<name>contextName</name>\r" +
		"<message id=\"1\">\r" +
		"<source>%s = %d <byte value=\"3\"/></source>\r" +
		"<translation>%s = %d <byte value=\"3\"/></translation>\r" +
		"</message>\r" +
		"</context>\r" +
		"</TS>";
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r" +
		"<TS sourcelanguage=\"en-us\" language=\"fr-fr\" version=\"4.5.1\">\r" +
		"<context>\r" +
		"<name>contextName</name>\r" +
		"<message id=\"1\">\r" +
		"<source>%s = %d <byte value=\"3\"/></source>\r" +
		"<translation>%s = %d <byte value=\"3\"/></translation>\r" +
		"</message>\r" +
		"</context>\r" +
		"</TS>";
		
		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(snippet,locENUS,locFRFR),
			filter.getEncoderManager(), locFR));
	}		
	
//	@Test
//	public void testComments () {
//		String snippet = "<?xml version='1.0' encoding='UTF-16BE'?>" +
//		"<TS version=\"4.5.1\" sourcelanguage=\"en-us\" language=\"fr-fr\">\r" +
//		"<context>\r" +
//		"<name>contextName</name>\r" +
//		"<message id=\"1\">\r" +
//		"<source>source 1</source>\r" +
//		"<comment>comment 1</comment>\r" +
//		"<translation >target 1</translation>\r" +
//		"</message>\r" +
//		"</context>\r" +
//		"</TS>";
//		
//		TextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet, locENUS, locFRFR), 1);
//		assertNotNull(tu);
//		Property prop = tu.getProperty(Property.NOTE);
//		assertNotNull(prop);
//		assertEquals("comment 1", prop.getValue());
//	}
	
	@Test
	public void TestDecodeByteTrueHex2() {
		
		String snippet = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r" +
		"<TS version=\"4.5.1\" sourcelanguage=\"en-us\" language=\"fr-fr\">\r" +
		"<defaultcodec>hello defaultcodec</defaultcodec>\r" +
		"<extra-loc-blank>hello extra-loc-blank</extra-loc-blank>\r" +
		"<context encoding=\"utf-8\">\r" +
		"<message id=\"1\" encoding=\"utf-8\" numerus=\"no\">\r" +
		"<source>hello " +
		"<byte value=\"x9\"/>" +
		"<byte value=\"xA\"/>" +
		"<byte value=\"xD\"/>" +
		"<byte value=\"x20\"/>" +
		"<byte value=\"xD7FF\"/>" +
		"<byte value=\"xE000\"/>" +
		"<byte value=\"xFFFD\"/>" +
		/*"<byte value=\"x10000\"/>" +
		"<byte value=\"x10FFFF\"/>" +*/
		"world</source>\r" +
		"<translation variants=\"no\">hejsan <byte value=\"x31\"/>varlden</translation>\r" +
		"</message>\r" +
		"</context>\r" +
		"</TS>";		
		
		Parameters params = (Parameters) filter.getParameters();
		params.setDecodeByteValues(true);
		
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet, locENUS, locFRFR), 1);
		
		String srcCheck = "hello " + 
		"\u0009" + 
		"\n" +
		"\r" +
		"\u0020" +
		"\ud7ff" +
		"\ue000" +
		"\ufffd" +
		"world"; 
		
		assertEquals(srcCheck, tu.getSource().getFirstContent().toText());
	}
	
	@Test
	public void TestEncodeIncludedChars() {
		
		Parameters params = (Parameters) filter.getParameters();
		params.setDecodeByteValues(true);
		
		String snippet = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r" +
		"<TS version=\"4.5.1\" sourcelanguage=\"en-us\" language=\"fr-fr\">\r" +
		"<defaultcodec>hello defaultcodec</defaultcodec>\r" +
		"<extra-loc-blank>hello extra-loc-blank</extra-loc-blank>\r" +
		"<context encoding=\"utf-8\">\r" +
		"<name>context name 1</name>\r" +
		"<comment>context comment 1</comment>\r" +
		"<message id=\"1\" encoding=\"utf-8\" numerus=\"no\">\r" +
		"<location filename=\"test.ts\" line=\"55\"/>\r" +

		"<source>hello " +
		"<byte value=\"x8\"/>" +
		"<byte value=\"xb\"/>" +
		"<byte value=\"xc\"/>" +
		"<byte value=\"xe\"/>" +
		"<byte value=\"x1f\"/>" +
		"<byte value=\"xd800\"/>" +
		"<byte value=\"xdfff\"/>" +
		"<byte value=\"xfffe\"/>" +
		"<byte value=\"xffff\"/>" +
		//"<byte value=\"x10FFFE\"/>" +

		"world</source>\r" +
		"<comment>old hello <byte value=\"79\"/>comment</comment>\r" +
		"<oldcomment>old hello old comment</oldcomment>\r" +
		"<extracomment>old hello extra comment</extracomment>\r" +
		"<translatorcomment>old hello translator comment</translatorcomment>\r" +
		"<translation variants=\"no\">hejsan <byte value=\"x31\"/>varlden</translation>\r" +
		"<userdata>hello userdata</userdata>\r" +
		"<extra-loc-blank>hello extra-loc-blank</extra-loc-blank>\r" +
		"</message>\r" +
		"</context>\r" +
		"</TS>";
		
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r" +
		"<TS version=\"4.5.1\" sourcelanguage=\"en-us\" language=\"fr-fr\">\r" +
		"<defaultcodec>hello defaultcodec</defaultcodec>\r" +
		"<extra-loc-blank>hello extra-loc-blank</extra-loc-blank>\r" +
		"<context encoding=\"utf-8\">\r" +
		"<name>context name 1</name>\r" +
		"<comment>context comment 1</comment>\r" +
		"<message id=\"1\" encoding=\"utf-8\" numerus=\"no\">\r" +
		"<location filename=\"test.ts\" line=\"55\"/>\r" +
		"<source>hello <byte value=\"x8\"><byte value=\"xb\"><byte value=\"xc\"><byte value=\"xe\"><byte value=\"x1f\"><byte value=\"xd800\"><byte value=\"xdfff\"><byte value=\"xfffe\"><byte value=\"xffff\">world</source>\r" +
		"<comment>old hello <byte value=\"79\"/>comment</comment>\r" +
		"<oldcomment>old hello old comment</oldcomment>\r" +
		"<extracomment>old hello extra comment</extracomment>\r" +
		"<translatorcomment>old hello translator comment</translatorcomment>\r" +
		"<translation variants=\"no\">hejsan 1varlden</translation>\r" +
		"<userdata>hello userdata</userdata>\r" +
		"<extra-loc-blank>hello extra-loc-blank</extra-loc-blank>\r" +
		"</message>\r" +
		"</context>\r" +
		"</TS>";
		
		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(snippet,locENUS,locFRFR),
			filter.getEncoderManager(), locFR));
	}
	
	@Test
	public void TestEncodeExcludedChars() {
		
		Parameters params = (Parameters) filter.getParameters();
		params.setDecodeByteValues(true);
		
		String snippet = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r" +
		"<TS version=\"4.5.1\" sourcelanguage=\"en-us\" language=\"fr-fr\">\r" +
		"<defaultcodec>hello defaultcodec</defaultcodec>\r" +
		"<extra-loc-blank>hello extra-loc-blank</extra-loc-blank>\r" +
		"<context encoding=\"utf-8\">\r" +
		"<name>context name 1</name>\r" +
		"<comment>context comment 1</comment>\r" +
		"<message id=\"1\" encoding=\"utf-8\" numerus=\"no\">\r" +
		"<location filename=\"test.ts\" line=\"55\"/>\r" +
		
		"<source>hello " +
		"<byte value=\"x9\"/>" +
		"<byte value=\"xA\"/>" +
		//"<byte value=\"xD\"/>" + normal literal
		"<byte value=\"x20\"/>" +
		"<byte value=\"xD7FF\"/>" +
		"<byte value=\"xE000\"/>" +
		"<byte value=\"xFFFD\"/>" +
		//"<byte value=\"x10000\"/>" +
		//"<byte value=\"x10FFFF\"/>" +

		"world</source>\r" +
		"<comment>old hello <byte value=\"79\"/>comment</comment>\r" +
		"<oldcomment>old hello old comment</oldcomment>\r" +
		"<extracomment>old hello extra comment</extracomment>\r" +
		"<translatorcomment>old hello translator comment</translatorcomment>\r" +
		"<translation variants=\"no\">hejsan <byte value=\"x31\"/>varlden</translation>\r" +
		"<userdata>hello userdata</userdata>\r" +
		"<extra-loc-blank>hello extra-loc-blank</extra-loc-blank>\r" +
		"</message>\r" +
		"</context>\r" +
		"</TS>";
		
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r" +
		"<TS version=\"4.5.1\" sourcelanguage=\"en-us\" language=\"fr-fr\">\r" +
		"<defaultcodec>hello defaultcodec</defaultcodec>\r" +
		"<extra-loc-blank>hello extra-loc-blank</extra-loc-blank>\r" +
		"<context encoding=\"utf-8\">\r" +
		"<name>context name 1</name>\r" +
		"<comment>context comment 1</comment>\r" +
		"<message id=\"1\" encoding=\"utf-8\" numerus=\"no\">\r" +
		"<location filename=\"test.ts\" line=\"55\"/>\r" +
		"<source>hello " +
		"\u0009" + 
		"\r" +
		//"\r" +
		"\u0020" +
		"\ud7ff" +
		"\ue000" +
		"\ufffd" +
		"world</source>\r" +
		"<comment>old hello <byte value=\"79\"/>comment</comment>\r" +
		"<oldcomment>old hello old comment</oldcomment>\r" +
		"<extracomment>old hello extra comment</extracomment>\r" +
		"<translatorcomment>old hello translator comment</translatorcomment>\r" +
		"<translation variants=\"no\">hejsan 1varlden</translation>\r" +
		"<userdata>hello userdata</userdata>\r" +
		"<extra-loc-blank>hello extra-loc-blank</extra-loc-blank>\r" +
		"</message>\r" +
		"</context>\r" +
		"</TS>";


		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(snippet,locENUS,locFRFR),
			filter.getEncoderManager(), locFR));
	}
	
	@Test
	public void AllEvents () {
		String snippet = "<?xml version=\"1.0\" encoding=\"[#$$self$@%encoding]\"?>\r" +
		"<TS version=\"4.5.1\" sourcelanguage=\"en-us\" language=\"fr-fr\">\r" +
		"<defaultcodec>hello defaultcodec</defaultcodec>\r" +
		"<extra-loc-blank>hello extra-loc-blank</extra-loc-blank>\r" +
		"<context encoding=\"utf-8\">\r" +
		"<name>context name 1</name>\r" +
		"<comment>context comment 1</comment>\r" +
		"<message id=\"1\" encoding=\"utf-8\" numerus=\"no\">\r" +
		"<location filename=\"test.ts\" line=\"55\"/>\r" +
		"<source>hello <byte value=\"79\"/>world</source>\r" +
		"<oldsource>old hello world</oldsource>\r" +
		"<comment>old hello <byte value=\"79\"/>comment</comment>\r" +
		"<oldcomment>old hello old comment</oldcomment>\r" +
		"<extracomment>old hello extra comment</extracomment>\r" +
		"<translatorcomment>old hello translator comment</translatorcomment>\r" +
		"<translation type=\"obsolete\" variants=\"no\">hejsan <byte value=\"79\"/>varlden</translation>\r" +
		"<userdata>hello userdata</userdata>\r" +
		"<extra-loc-blank>hello extra-loc-blank</extra-loc-blank>\r" +
		"</message>\r" +
		"</context>\r" +
		"</TS>";
		
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r" +
		"<TS version=\"4.5.1\" sourcelanguage=\"en-us\" language=\"fr-fr\">\r" +
		"<defaultcodec>hello defaultcodec</defaultcodec>\r" +
		"<extra-loc-blank>hello extra-loc-blank</extra-loc-blank>\r" +
		"<context encoding=\"utf-8\">\r" +
		"<name>context name 1</name>\r" +
		"<comment>context comment 1</comment>\r" +
		"<message id=\"1\" encoding=\"utf-8\" numerus=\"no\">\r" +
		"<location filename=\"test.ts\" line=\"55\"/>\r" +
		"<source>hello <byte value=\"79\"/>world</source>\r" +
		"<oldsource>old hello world</oldsource>\r" +
		"<comment>old hello <byte value=\"79\"/>comment</comment>\r" +
		"<oldcomment>old hello old comment</oldcomment>\r" +
		"<extracomment>old hello extra comment</extracomment>\r" +
		"<translatorcomment>old hello translator comment</translatorcomment>\r" +
		"<translation type=\"obsolete\" variants=\"no\">hejsan <byte value=\"79\"/>varlden</translation>\r" +
		"<userdata>hello userdata</userdata>\r" +
		"<extra-loc-blank>hello extra-loc-blank</extra-loc-blank>\r" +
		"</message>\r" +
		"</context>\r" +
		"</TS>";
		
		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(snippet,locENUS,locFRFR),
			filter.getEncoderManager(), locFR));
	}
	
	@Test
	public void StartDocument_FromFile() {
		StartDocument sd = FilterTestDriver.getStartDocument(getEventsFromFile("Complete_valid_utf8_bom_crlf.ts"));
		assertEquals("Incorrect id","o1", sd.getId());
		assertEquals("Incorrect mimeType",MimeTypeMapper.TS_MIME_TYPE, sd.getMimeType());
		// For comparison, perform manipulations of root to match URI style on Windows (preceding '/', etc.).
		assertTrue(sd.getName().startsWith(new File(root).toURI().getPath()));
		assertTrue(sd.getName().endsWith("/Complete_valid_utf8_bom_crlf.ts"));
		assertEquals("Incorrect encoding", "utf-8", sd.getEncoding());
		assertEquals("Incorrect src language", locENUS, sd.getLocale());
		assertEquals("Incorrect linebreak", "\r\n", sd.getLineBreak());
		assertEquals("Incorrect multilingual", true, sd.isMultilingual());
		assertEquals("Incorrect utf8bom", false, sd.hasUTF8BOM());
		assertNotNull(sd.getFilterParameters());
		assertTrue(sd.getFilterWriter() instanceof GenericFilterWriter);
		assertEquals("utf-8", sd.getProperty("encoding").getValue());
	
		assertEquals(
			"<?xml version=\"1.0\" encoding=\"[#$$self$@%encoding]\"?>", 
			sd.getSkeleton().toString());
		
	}	

	@Test
	public void StartGroupContextPart_FromFile() {
		
		StartGroup sg = FilterTestDriver.getGroup(getEventsFromFile("Complete_valid_utf8_bom_crlf.ts"), 1);

		assertEquals("o3", sg.getId());
		assertEquals("utf-8", sg.getProperty("encoding").getValue());
		//assertEquals("context name 1", sg.getProperty("name").getValue());
		//assertEquals("context comment 1", sg.getProperty("comment").getValue());
		
		assertEquals( 
				"<context encoding=\"utf-8\">\r\n" +
				"<name>context name 1</name>\r\n" +
				"<comment>context comment 1</comment>\r\n",
				sg.getSkeleton().toString());

		
		sg = FilterTestDriver.getGroup(getEventsFromFile("Complete_valid_utf8_bom_crlf.ts"), 2);

		assertEquals("o4", sg.getId());
		assertEquals("utf-8", sg.getProperty("encoding").getValue());
		//assertEquals("context name 2", sg.getProperty("name").getValue());
		//assertEquals("context comment 2", sg.getProperty("comment").getValue());
		assertEquals( 
				"<context encoding=\"utf-8\">\r\n" +
				"<name>context name 2</name>\r\n" +
				"<comment>context comment 2</comment>\r\n", 
				sg.getSkeleton().toString());
	}

	@Test
	public void TextUnitMessageUnfinished_FromFile() {
		ITextUnit tu = FilterTestDriver.getTextUnit(getEventsFromFile("Complete_valid_utf8_bom_crlf.ts"), 1);

		assertEquals("1", tu.getId());
		assertEquals(MimeTypeMapper.TS_MIME_TYPE, tu.getMimeType());
		assertEquals("1", tu.getName());
		assertFalse(tu.isEmpty());
		
		assertEquals("1", tu.getProperty("id").getValue());
		assertEquals("utf-8", tu.getProperty("encoding").getValue());
		assertEquals("no", tu.getProperty("numerus").getValue());
		assertEquals(0, tu.getSourcePropertyNames().size());
		assertEquals("no", tu.getTargetProperty(locFRFR, "variants").getValue());
		assertEquals("no", tu.getTargetProperty(locFRFR, "approved").getValue());
		assertEquals( 
				"<message id=\"1\" encoding=\"utf-8\" numerus=\"no\">\r\n" +
				"<location filename=\"test.ts\" line=\"55\"/>\r\n" +
				"<source>[#$$self$]</source>\r\n" +
				"<oldsource>old hello world</oldsource>\r\n" +
				"<comment>old hello <byte value=\"79\"/>comment</comment>\r\n" +
				"<oldcomment>old hello old comment</oldcomment>\r\n" +
				"<extracomment>old hello extra comment</extracomment>\r\n" +
				"<translatorcomment>old hello translator comment</translatorcomment>\r\n" +
				"<translation[#$$self$@%approved] variants=\"no\">[#$$self$]</translation>\r\n" +
				"<userdata>hello userdata</userdata>\r\n" +
				"<extra-loc-blank>hello extra-loc-blank</extra-loc-blank>\r\n" +
				"</message>", 
				tu.getSkeleton().toString());
	}
	@Test
	public void TextUnitMessageApproved_FromFile() {
		ITextUnit tu = FilterTestDriver.getTextUnit(getEventsFromFile("Complete_valid_utf8_bom_crlf.ts"), 2);

		assertEquals("2", tu.getId());
		assertEquals(MimeTypeMapper.TS_MIME_TYPE, tu.getMimeType());
		assertEquals("3", tu.getName());
		assertFalse(tu.isEmpty());
		
		assertEquals("3", tu.getProperty("id").getValue());
		assertEquals("utf-8", tu.getProperty("encoding").getValue());
		assertEquals("no", tu.getProperty("numerus").getValue());
		assertEquals(0, tu.getSourcePropertyNames().size());
		assertEquals("no", tu.getTargetProperty(locFRFR, "variants").getValue());
		assertEquals("yes", tu.getTargetProperty(locFRFR, "approved").getValue());
		
		assertEquals( 
				"\r\n<message id=\"3\" encoding=\"utf-8\" numerus=\"no\">\r\n" +
				"<location filename=\"test.ts\" line=\"55\"/>\r\n" +
				"<source>[#$$self$]</source>\r\n" +
				"<oldsource>old hello world</oldsource>\r\n" +
				"<comment>old hello <byte value=\"79\"/>comment</comment>\r\n" +
				"<oldcomment>old hello old comment</oldcomment>\r\n" +
				"<extracomment>old hello extra comment</extracomment>\r\n" +
				"<translatorcomment>old hello translator comment</translatorcomment>\r\n" +
				"<translation variants=\"no\"[#$$self$@%approved]>[#$$self$]</translation>\r\n" +
				"<userdata>hello userdata</userdata>\r\n" +
				"<extra-loc-blank>hello extra-loc-blank</extra-loc-blank>\r\n" +
				"</message>", 
				tu.getSkeleton().toString());
	}	
	@Test
	public void TextUnitMessageObsolete_FromFile() {
		
		DocumentPart dp = FilterTestDriver.getDocumentPart(getEventsFromFile("Complete_valid_utf8_bom_crlf.ts"), 2);
	
		assertEquals("o5", dp.getId());
		assertEquals(0, dp.getPropertyNames().size());
		assertEquals(0, dp.getSourcePropertyNames().size());
		assertEquals(0, dp.getTargetPropertyNames(locFRFR).size());
		assertEquals( 
				"\r\n<message id=\"2\" encoding=\"utf-8\" numerus=\"no\">\r\n" +
				"<location filename=\"test.ts\" line=\"55\"/>\r\n" +
				"<source>hello <byte value=\"79\"/>world</source>\r\n" +
				"<oldsource>old hello world</oldsource>\r\n" +
				"<comment>old hello <byte value=\"79\"/>comment</comment>\r\n" +
				"<oldcomment>old hello old comment</oldcomment>\r\n" +
				"<extracomment>old hello extra comment</extracomment>\r\n" +
				"<translatorcomment>old hello translator comment</translatorcomment>\r\n" +
				"<translation type=\"obsolete\" variants=\"no\">hejsan <byte value=\"79\"/>varlden</translation>\r\n" +
				"<userdata>hello userdata</userdata>\r\n" +
				"<extra-loc-blank>hello extra-loc-blank</extra-loc-blank>\r\n" +
				"</message>",
				dp.getSkeleton().toString());
	}	
	@Test
	public void TextUnitMessageMissingTranslation_FromFile() {
		ITextUnit tu = FilterTestDriver.getTextUnit(getEventsFromFile("Complete_valid_utf8_bom_crlf.ts"), 3);

		assertEquals("3", tu.getId());
		assertEquals(MimeTypeMapper.TS_MIME_TYPE, tu.getMimeType());
		assertEquals("4", tu.getName());
		assertFalse(tu.isEmpty());
		
		assertEquals("4", tu.getProperty("id").getValue());
		assertEquals("utf-8", tu.getProperty("encoding").getValue());
		assertEquals("no", tu.getProperty("numerus").getValue());
		assertEquals(0, tu.getSourcePropertyNames().size());
		assertEquals("no", tu.getTargetProperty(locFRFR, "variants").getValue());
		assertEquals("no", tu.getTargetProperty(locFRFR, "approved").getValue());
	
		assertEquals( 
				"\r\n<message id=\"4\" encoding=\"utf-8\" numerus=\"no\">\r\n" +
				"<location filename=\"test.ts\" line=\"55\"/>\r\n" +
				"<source>[#$$self$]</source>\r\n" +
				"<oldsource>old hello world</oldsource>\r\n" +
				"<comment>old hello <byte value=\"79\"/>comment</comment>\r\n" +
				"<oldcomment>old hello old comment</oldcomment>\r\n" +
				"<extracomment>old hello extra comment</extracomment>\r\n" +
				"<translatorcomment>old hello translator comment</translatorcomment>\r\n" +
				"<translation[#$$self$@%approved] variants=\"no\">[#$$self$]</translation>\r\n" +
				"<userdata>hello userdata</userdata>\r\n" +
				"<extra-loc-blank>hello extra-loc-blank</extra-loc-blank>\r\n" +
				"</message>", 
				tu.getSkeleton().toString());
	}	
	@Test
	public void TextUnitMessageMissingSourceAndTranslation_FromFile() {
		
		DocumentPart dp = FilterTestDriver.getDocumentPart(getEventsFromFile("Complete_valid_utf8_bom_crlf.ts"), 3);
	
		assertEquals("o6", dp.getId());
		assertEquals(0, dp.getPropertyNames().size());
		assertEquals(0, dp.getSourcePropertyNames().size());
		assertEquals(0, dp.getTargetPropertyNames(locFRFR).size());
		assertEquals( 
				"\r\n<message id=\"5\" encoding=\"utf-8\" numerus=\"no\">\r\n" +
				"<location filename=\"test.ts\" line=\"55\"/>\r\n" +
				"<oldsource>old hello world</oldsource>\r\n" +
				"<comment>old hello <byte value=\"79\"/>comment</comment>\r\n" +
				"<oldcomment>old hello old comment</oldcomment>\r\n" +
				"<extracomment>old hello extra comment</extracomment>\r\n" +
				"<translatorcomment>old hello translator comment</translatorcomment>\r\n" +
				"<userdata>hello userdata</userdata>\r\n" +
				"<extra-loc-blank>hello extra-loc-blank</extra-loc-blank>\r\n" +
				"</message>",
				dp.getSkeleton().toString());
	}	
	@Test
	public void TextUnitMessageMissingSourceNotTranslation_FromFile() {
		
		DocumentPart dp = FilterTestDriver.getDocumentPart(getEventsFromFile("Complete_valid_utf8_bom_crlf.ts"), 4);

		
		assertEquals("o7", dp.getId());
		assertEquals(0, dp.getPropertyNames().size());
		assertEquals(0, dp.getSourcePropertyNames().size());
		assertEquals(0, dp.getTargetPropertyNames(locFRFR).size());
		assertEquals( 
				"\r\n<message id=\"6\" encoding=\"utf-8\" numerus=\"no\">\r\n" +
				"<location filename=\"test.ts\" line=\"55\"/>\r\n" +
				"<oldsource>old hello world</oldsource>\r\n" +
				"<comment>old hello <byte value=\"79\"/>comment</comment>\r\n" +
				"<oldcomment>old hello old comment</oldcomment>\r\n" +
				"<extracomment>old hello extra comment</extracomment>\r\n" +
				"<translatorcomment>old hello translator comment</translatorcomment>\r\n" +
				"<translation variants=\"no\">hejsan <byte value=\"79\"/>varlden</translation>\r\n" +
				"<userdata>hello userdata</userdata>\r\n" +
				"<extra-loc-blank>hello extra-loc-blank</extra-loc-blank>\r\n" +
				"</message>",
				dp.getSkeleton().toString());
	}		
	@Test
	public void TextUnitMessageEmptySource_FromFile() {
		
		DocumentPart dp = FilterTestDriver.getDocumentPart(getEventsFromFile("Complete_valid_utf8_bom_crlf.ts"), 5);
			
		assertEquals("o8", dp.getId());
		assertEquals(0, dp.getPropertyNames().size());
		assertEquals(0, dp.getSourcePropertyNames().size());
		assertEquals(0, dp.getTargetPropertyNames(locFRFR).size());
		assertEquals( 
				"\r\n<message id=\"7\" encoding=\"utf-8\" numerus=\"no\">\r\n" +
				"<location filename=\"test.ts\" line=\"55\"/>\r\n" +
				"<source></source>\r\n" +
				"<oldsource>old hello world</oldsource>\r\n" +
				"<comment>old hello <byte value=\"79\"/>comment</comment>\r\n" +
				"<oldcomment>old hello old comment</oldcomment>\r\n" +
				"<extracomment>old hello extra comment</extracomment>\r\n" +
				"<translatorcomment>old hello translator comment</translatorcomment>\r\n" +
				"<userdata>hello userdata</userdata>\r\n" +
				"<extra-loc-blank>hello extra-loc-blank</extra-loc-blank>\r\n" +
				"</message>",
				dp.getSkeleton().toString());
	}		
	@Test
	public void TextUnitMessageEmptyTranslation_FromFile() {
		ITextUnit tu = FilterTestDriver.getTextUnit(getEventsFromFile("Complete_valid_utf8_bom_crlf.ts"), 4);
	
		assertEquals("4", tu.getId());
		assertEquals(MimeTypeMapper.TS_MIME_TYPE, tu.getMimeType());
		assertEquals("8", tu.getName());
		assertFalse(tu.isEmpty());
		
		assertEquals("8", tu.getProperty("id").getValue());
		assertEquals("utf-8", tu.getProperty("encoding").getValue());
		assertEquals("no", tu.getProperty("numerus").getValue());
		assertEquals(0, tu.getSourcePropertyNames().size());
		assertEquals("no", tu.getTargetProperty(locFRFR, "variants").getValue());
		assertEquals("no", tu.getTargetProperty(locFRFR, "approved").getValue());
	
		assertEquals( 
				"\r\n<message id=\"8\" encoding=\"utf-8\" numerus=\"no\">\r\n" +
				"<location filename=\"test.ts\" line=\"55\"/>\r\n" +
				"<source>[#$$self$]</source>\r\n" +
				"<oldsource>old hello world</oldsource>\r\n" +
				"<comment>old hello <byte value=\"79\"/>comment</comment>\r\n" +
				"<oldcomment>old hello old comment</oldcomment>\r\n" +
				"<extracomment>old hello extra comment</extracomment>\r\n" +
				"<translatorcomment>old hello translator comment</translatorcomment>\r\n" +
				"<translation variants=\"no\"[#$$self$@%approved]>[#$$self$]</translation>\r\n" +
				"<userdata>hello userdata</userdata>\r\n" +
				"<extra-loc-blank>hello extra-loc-blank</extra-loc-blank>\r\n" +
				"</message>", 
				tu.getSkeleton().toString());
	}	
		
	@Test
	public void StartGroupNumerusPart_FromFile() {
		StartGroup sg = FilterTestDriver.getGroup(getEventsFromFile("Complete_valid_utf8_bom_crlf.ts"), 3);
		
		assertEquals("o9", sg.getId());
		assertEquals("9", sg.getName());
		assertEquals("yes", sg.getTargetProperty(locFRFR, "approved").getValue());
	
		assertEquals( 
				"\r\n<message id=\"9\" encoding=\"utf-8\" numerus=\"yes\">\r\n" +
				"<location filename=\"test.ts\" line=\"55\"/>\r\n" +
				"<source>hello <byte value=\"79\"/>world</source>\r\n" +
				"<oldsource>old hello world</oldsource>\r\n" +
				"<comment>old hello <byte value=\"79\"/>comment</comment>\r\n" +
				"<oldcomment>old hello old comment</oldcomment>\r\n" +
				"<extracomment>old hello extra comment</extracomment>\r\n" +
				"<translatorcomment>old hello translator comment</translatorcomment>\r\n" +
				"<translation variants=\"yes\"[#$$self$@%approved]>\r\n",
				sg.getSkeleton().toString());
	}

	@Test
	public void TextUnitNumerus_FromFile() {
		ITextUnit tu = FilterTestDriver.getTextUnit(getEventsFromFile("Complete_valid_utf8_bom_crlf.ts"), 5);
		
		assertEquals("5", tu.getId());
		assertEquals(MimeTypeMapper.TS_MIME_TYPE, tu.getMimeType());
		assertFalse(tu.isEmpty());
		assertEquals("hello <byte value=\"79\"/>world", tu.getSource().getFirstContent().toText());
		assertEquals("Numerus<byte value=\"79\"/> 1", tu.getTarget(locFRFR).getFirstContent().toText());
		assertEquals( 
				"<numerusform variants=\"no\">[#$$self$]</numerusform>", 
				tu.getSkeleton().toString());
		
		tu = FilterTestDriver.getTextUnit(getEventsFromFile("Complete_valid_utf8_bom_crlf.ts"), 6);
		assertTrue(tu.hasProperty("numerus"));
		assertEquals("yes", tu.getProperty("numerus").getValue());
		
		assertEquals("6", tu.getId());
		assertEquals(MimeTypeMapper.TS_MIME_TYPE, tu.getMimeType());
		assertFalse(tu.isEmpty());
		assertEquals("hello <byte value=\"79\"/>world", tu.getSource().getFirstContent().toText());
		assertEquals("Numerus<byte value=\"79\"/> 2", tu.getTarget(locFRFR).getFirstContent().toText());
		assertEquals( 
				"\r\n<numerusform variants=\"no\">[#$$self$]</numerusform>", 
				tu.getSkeleton().toString());
	}	
	
	@Test
	public void testDoubleExtraction () {
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
		list.add(new InputDocument(root+"Complete_valid_utf8_bom_crlf.ts", null));
		RoundTripComparison rtc = new RoundTripComparison();
		assertTrue(rtc.executeCompare(filter, list, "UTF-8", locENUS, locFRFR));
	}
	
	//--methods--
	@Test
	public void testGetName() {
		assertEquals("okf_ts", filter.getName());
	}

	@Test
	public void testGetMimeType() {
		assertEquals("application/x-ts", filter.getMimeType());
	}	
	
	//--exceptions--
	@Test (expected=NullPointerException.class)
	public void testSourceLangNotSpecified() {
		FilterTestDriver.getStartDocument(getEvents(simpleSnippet, null));
	}

	@Test (expected=NullPointerException.class)
	public void testTargetLangNotSpecified() {
		FilterTestDriver.getStartDocument(getEvents(simpleSnippet, locENUS));
	}

	@Test (expected=NullPointerException.class)
	public void testTargetLangNotSpecified2() {
		FilterTestDriver.getStartDocument(getEvents(simpleSnippet, locENUS, null));
	}
	
	@Test (expected=NullPointerException.class)
	public void testSourceLangEmpty() {
		FilterTestDriver.getStartDocument(getEvents(simpleSnippet, null, locFRFR));
	}	
	
	@Test (expected=NullPointerException.class)
	public void testTargetLangEmpty() {
		FilterTestDriver.getStartDocument(getEvents(simpleSnippet, locENUS, null));
	}	
	
	@Test
	public void testInputStream() {
		InputStream tsStream = TsFilterTest.class.getResourceAsStream("/alarm_ro.ts");
		filter.open(new RawDocument(tsStream, "UTF-8", locENUS,locFRFR));
		if ( !testDriver.process(filter) ) Assert.fail();
		filter.close();
	}	

	@Test
	public void testConsolidatedStream() {
		filter.open(new RawDocument(simpleSnippet, locENUS,locFRFR));
		if ( !testDriver.process(filter) ) Assert.fail();
		filter.close();
	}	

	@Test
	public void testTu() {
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(simpleSnippet, locENUS, locFRFR), 1);
		assertNotNull(tu);
		assertEquals("Add Entry To System Log", tu.getSource().getFirstContent().getCodedText());
		assertEquals("Lagg till i system Loggen", tu.getTarget(locFRFR).getFirstContent().getCodedText());
		
		tu.setTargetProperty(locFRFR, new Property(Property.APPROVED, "no", false));
	}	
	
	@Test
	public void testStartDocument () throws URISyntaxException {
		URL url = TsFilterTest.class.getResource("/TSTest01.ts");
		assertTrue("Problem in StartDocument", FilterTestDriver.testStartDocument(filter,
			new InputDocument(url.toURI().getPath(), null),
			"UTF-8", locEN, locEN));
	}
	
	@Test
	public void runTest () {
		FilterTestDriver testDriver = new FilterTestDriver();
		try (TsFilter filter = new TsFilter()) {
			URL url = TsFilterTest.class.getResource("/TSTest01.ts");
			filter.open(new RawDocument(new URI(url.toString()), "UTF-8", locENUS, locFRFR));			
			if ( !testDriver.process(filter) ) Assert.fail();
			//process(filter);
			filter.close();
		}
		catch ( Throwable e ) {
			e.printStackTrace();
			Assert.fail("Exception occured");
		}
	}	

	private ArrayList<Event> getEvents(String snippet, LocaleId srcLang, LocaleId trgLang){
		return FilterTestDriver.getEvents(filter, snippet, srcLang, trgLang);
	}
	
	//--without specifying target language--
	private ArrayList<Event> getEvents(String snippet, LocaleId srcLang){
		return FilterTestDriver.getEvents(filter, snippet, srcLang);
	}	

	private ArrayList<Event> getEventsFromFile(String file){
		URL url = TsFilterTest.class.getResource("/"+file);
		try {
			return FilterTestDriver.getEvents(filter, new RawDocument(new URI(url.toString()), "utf-8", locENUS, locFRFR), null);
		}
		catch ( Throwable e ) {
			e.printStackTrace();
			Assert.fail("Exception occured");
			return null; // unreachable
		}
	}	
}
