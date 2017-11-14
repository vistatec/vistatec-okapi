/*===========================================================================
  Copyright (C) 2009-2017 by the Okapi Framework contributors
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

package net.sf.okapi.filters.po;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.FileLocation;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.MimeTypeMapper;
import net.sf.okapi.common.filters.FilterConfiguration;
import net.sf.okapi.common.filters.FilterTestDriver;
import net.sf.okapi.common.filters.InputDocument;
import net.sf.okapi.common.filters.RoundTripComparison;
import net.sf.okapi.common.resource.DocumentPart;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.StartGroup;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.skeleton.GenericSkeletonWriter;

@RunWith(JUnit4.class)
public class POFilterTest {
	
	private POFilter filter;
	private FileLocation root;
	private LocaleId locEN = LocaleId.fromString("en");
	private LocaleId locFR = LocaleId.fromString("fr");
	
	@Before
	public void setUp() throws URISyntaxException {
		filter = new POFilter();
		root = FileLocation.fromClass(this.getClass());
	}

	@Test
	public void testPOTHeader () {
		String snippet = "msgid \"\"\n"
			+ "msgstr \"\"\n"
			+ "\"Project-Id-Version: PACKAGE VERSION\\n\"\n"
			+ "\"Report-Msgid-Bugs-To: \\n\"\n"
			+ "\"POT-Creation-Date: 2009-03-25 15:39-0700\\n\"\n"
			+ "\"PO-Revision-Date: YEAR-MO-DA HO:MI+ZONE\\n\"\n"
			+ "\"Last-Translator: FULL NAME <EMAIL@ADDRESS>\\n\"\n"
			+ "\"Language-Team: LANGUAGE <LL@li.org>\\n\"\n"
			+ "\"MIME-Version: 1.0\\n\"\n"
			+ "\"Content-Type: text/plain; charset=ENCODING\\n\"\n"
			+ "\"Content-Transfer-Encoding: 8bit\\n\"\n"
			+ "msgid \"Text\"\n"
			+ "msgstr \"\"\n";
		List<Event> events = getEvents(snippet, locEN, locFR);
		DocumentPart dp = FilterTestDriver.getDocumentPart(events, 1);
		assertNotNull(dp);

		Property prop = dp.getProperty(Property.ENCODING);
		assertNotNull(prop);
		assertEquals("ENCODING", prop.getValue());
		assertFalse(prop.isReadOnly());

		prop = dp.getProperty(POFilter.PROPERTY_PLURALFORMS);
		assertNull(prop);
	}

	@Test
	public void testPOHeader () {
		String snippet = "#, fuzzy\r"
			+ "msgid \"\"\r"
			+ "msgstr \"\"\r"
			+ "\"Project-Id-Version: PACKAGE VERSION\\n\"\r"
			+ "\"Report-Msgid-Bugs-To: \\n\"\r"
			+ "\"POT-Creation-Date: 2009-03-25 15:39-0700\\n\"\r"
			+ "\"PO-Revision-Date: YEAR-MO-DA HO:MI+ZONE\\n\"\r"
			+ "\"Last-Translator: FULL NAME <EMAIL@ADDRESS>\\n\"\r"
			+ "\"Language-Team: LANGUAGE <LL@li.org>\\n\"\r"
			+ "\"MIME-Version: 1.0\\n\"\r"
			+ "\"Content-Type: text/plain; charset=UTF-8\\n\"\r"
			+ "\"Content-Transfer-Encoding: 8bit\\n\"\r"
			+ "\"Plural-Forms: nplurals=2; plural=(n!=1);\\n\"\r\r"
			+ "msgid \"Text\"\r"
			+ "msgstr \"Texte\"\r";
		List<Event> events = getEvents(snippet, locEN, locFR);
		DocumentPart dp = FilterTestDriver.getDocumentPart(events, 1);
		assertNotNull(dp);

		Property prop = dp.getProperty(Property.ENCODING);
		assertNotNull(prop);
		assertEquals("UTF-8", prop.getValue());
		assertFalse(prop.isReadOnly());

		prop = dp.getProperty(POFilter.PROPERTY_PLURALFORMS);
		assertNotNull(prop);
		assertEquals("nplurals=2; plural=(n!=1);", prop.getValue());
		assertFalse(prop.isReadOnly());
		
		String result = FilterTestDriver.generateOutput(getEvents(snippet, locEN, locFR),
			filter.getEncoderManager(), locFR);
		assertEquals(snippet, result);
	}

	@Test
	public void testHeaderNoNPlurals () {
		String snippet = "msgid \"\"\n"
			+ "msgstr \"\"\n"
			+ "\"MIME-Version: 1.0\\n\"\n"
			+ "\"Content-Type: text/plain; charset=ENCODING\\n\"\n"
			+ "\"Content-Transfer-Encoding: 8bit\\n\"\n"
			+ "\"Plural-Forms: nplurzzzals=2; plural=(n!=1);\\n\"\n\n"
			+ "msgid \"Text\"\n"
			+ "msgstr \"\"\n";
		DocumentPart dp = FilterTestDriver.getDocumentPart(getEvents(snippet, locEN, locFR), 1);
		assertNotNull(dp);
		// We should also get a warning about the nplurals field missing
		Property prop = dp.getProperty(POFilter.PROPERTY_PLURALFORMS);
		assertNotNull(prop);
	}

	@Test
	public void testHeaderWithEmptyEntryAfter () {
		String snippet = "#, fuzzy\n"
			+ "msgid \"\"\n"
			+ "msgstr \"\"\n"
			+ "\"MIME-Version: 1.0\\n\"\n"
			+ "\"Content-Type: text/plain; charset=ENCODING\\n\"\n"
			+ "\"Content-Transfer-Encoding: 8bit\\n\"\n"
			+ "\"Plural-Forms: nplurals=2; plural=(n!=1);\\n\"\n\n"
			+ "#, fuzzy\n"
			+ "msgid \"\"\n"
			+ "msgstr \"\"\n";
		List<Event> events = getEvents(snippet, locEN, locFR);
		DocumentPart dp = FilterTestDriver.getDocumentPart(events, 1);
		assertNotNull(dp);
		// Empty entries are sent as DP
		assertNull(FilterTestDriver.getTextUnit(events, 1));
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
	public void testPluralFormAccess () {
		assertEquals("nplurals=2; plural= n==1 || n%10==1 ? 0 : 1;",
			PluralForms.getExpression(LocaleId.fromString("mk")));
		assertEquals(4, PluralForms.getNumber("cy"));
	}

	@Test
	public void testPluralFormDefaults () {
		assertEquals("nplurals=2; plural=(n!=1);",
			PluralForms.getExpression(LocaleId.fromString("not-a-valid-code")));
		assertEquals(2, PluralForms.getNumber("not-a-valid-code"));
	}

	@Test
	public void testStartDocument () {
		assertTrue("Problem in StartDocument", FilterTestDriver.testStartDocument(filter,
			new InputDocument(root.in("/Test01.po").toString(), null),
			"UTF-8", locEN, locEN));
	}
	
	@Test
	public void testOuputOptionLine_JustFormatWithMacLB () {
		String snippet = "#, c-format\r"
			+ "msgid \"Text 1\"\r"
			+ "msgstr \"Texte 1\"\r";
		String result = FilterTestDriver.generateOutput(getEvents(snippet, locEN, locFR),
			filter.getEncoderManager(), locFR);
		assertEquals(result, snippet);
	}
		
	@Test
	public void testOuputOptionLine_FormatFuzzy () {
		String snippet = "#, c-format, fuzzy\n"
			+ "msgid \"Text 1\"\n"
			+ "msgstr \"Texte 1\"\n";
		String result = FilterTestDriver.generateOutput(getEvents(snippet, locEN, locFR),
			filter.getEncoderManager(), locFR);
		assertEquals(result, snippet);
	}

	@Test
	public void testInlines () {
		String snippet = "msgid \"Text %s and %d and %f\"\n"
			+ "msgstr \"Texte %f et %d et %s\"\n";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet, locEN, locFR), 1);
		assertNotNull(tu);
		assertTrue(tu.hasTarget(locFR));
		TextFragment src = tu.getSource().getFirstContent();
		TextFragment trg = tu.getTarget(locFR).getFirstContent();
		assertEquals(3, src.getCodes().size());
		assertEquals(src.getCodes().size(), trg.getCodes().size());
		FilterTestDriver.checkCodeData(src, trg);
	}
		
	@Test
	public void testIDWithContext () {
		String snippet =
			  "msgid \"Text1\"\n"
			+ "msgstr \"Texte1\"\n\n"
			+ "msgctxt \"abc\"\n"
			+ "msgid \"Text1\"\n"
			+ "msgstr \"Texte1\"\n";
		ITextUnit tu1 = FilterTestDriver.getTextUnit(getEvents(snippet, locEN, locFR), 1);
		assertNotNull(tu1);
		assertEquals("P39E32278", tu1.getName());
		ITextUnit tu2 = FilterTestDriver.getTextUnit(getEvents(snippet, locEN, locFR), 2);
		assertNotNull(tu2);
		assertEquals("NE9257EEE", tu2.getName());
	}

	@Test
	public void testProtectApproved () {
		String snippet = "#, c-format\n"
			+ "msgid \"Text 1\"\n"
			+ "msgstr \"Texte 1\"\n\n"
			+ "msgid \"Text 2\"\n"
			+ "msgstr \"\"\n";
		// When approved entries are protected
		filter.getParameters().setBoolean("protectApproved", true);
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet, locEN, locFR), 1);
		assertTrue(tu!=null);
		assertFalse(tu.isTranslatable());
		tu = FilterTestDriver.getTextUnit(getEvents(snippet, locEN, locFR), 2);
		assertTrue(tu!=null);
		assertTrue(tu.isTranslatable());
		// When approved entries are not protected
		filter.getParameters().setBoolean("protectApproved", false);
		tu = FilterTestDriver.getTextUnit(getEvents(snippet, locEN, locFR), 1);
		assertTrue(tu!=null);
		assertTrue(tu.isTranslatable());
		tu = FilterTestDriver.getTextUnit(getEvents(snippet, locEN, locFR), 2);
		assertTrue(tu!=null);
		assertTrue(tu.isTranslatable());
	}

	@Test
	public void testOutputProtectApproved () {
		String snippet = "#, c-format\n"
			+ "msgid \"Text 1\"\n"
			+ "msgstr \"Texte 1\"\n\n";
		// When approved entries are protected
		filter.getParameters().setBoolean("protectApproved", true);
		String result = FilterTestDriver.generateOutput(getEvents(snippet, locEN, locFR),
			filter.getEncoderManager(), locFR);
		filter.getParameters().setBoolean("protectApproved", false); // Set back to original value
		assertEquals(result, snippet);
	}

	@Test
	public void testWithNoCodesLookingLikeCodes () {
		String snippet = "msgctxt \"okpCtx:tu=1\"\n"
			+ "msgid \"EN<x>...</x><x/>\"\n"
			+ "msgstr \"FR<x>...</x><x/>\"\n";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet, locEN, locFR), 1);
		assertNotNull(tu);
		assertEquals("EN<x>...</x><x/>", tu.getSource().toString());
	}

	@Test
	public void testWithLetterCodes () {
		String snippet = "msgctxt \"okpCtx:tu=1\"\n"
			+ "msgid \"EN<g1>...</g1><x2/>\"\n"
			+ "msgstr \"FR<g1>...</g1><x2/>\"\n";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet, locEN, locFR), 1);
		assertNotNull(tu);
		assertEquals(3, tu.getSource().getFirstContent().getCodes().size());
	}

	@Test
	public void testOuputOptionLine_FuzyFormat () {
		String snippet = "#, fuzzy, c-format\n"
			+ "msgid \"Text 1\"\n"
			+ "msgstr \"Texte 1\"\n";
		String result = FilterTestDriver.generateOutput(getEvents(snippet, locEN, locFR),
			filter.getEncoderManager(), locFR);
		assertEquals(result, snippet);
	}

	@Test
	public void testOuputWithAllowedEmpty () {
		String snippet = "msgid \"Text 1\"\n"
			+ "msgstr \"\"\n";
		filter.getParameters().setBoolean(GenericSkeletonWriter.ALLOWEMPTYOUTPUTTARGET, true);
		String result = FilterTestDriver.generateOutput(getEvents(snippet, locEN, locFR),
			filter.getEncoderManager(), locFR);
		filter.getParameters().setBoolean(GenericSkeletonWriter.ALLOWEMPTYOUTPUTTARGET, false); // Reset to default
		assertEquals(result, snippet);
	}

	@Test
	public void testOuputOptionLine_StuffFuzyFormat () {
		String snippet = "#, x-stuff, fuzzy, c-format\n"
			+ "msgid \"Text 1\"\n"
			+ "msgstr \"Text 1\"\n";
		String result = FilterTestDriver.generateOutput(getEvents(snippet, locEN, locFR),
			filter.getEncoderManager(), locFR);
		assertEquals(result, snippet);
	}
	
	@Test
	public void testOuputSimpleEntry () {
		String snippet = "msgid \"Text 1\"\n"
			+ "msgstr \"Texte 1\"\n";
		String expect = "msgid \"Text 1\"\n"
			+ "msgstr \"Texte 1\"\n";
		assertEquals(expect, FilterTestDriver.generateOutput(getEvents(snippet, locEN, locFR),
			filter.getEncoderManager(), locFR));
	}
	
	@Test
	public void testOuputEntryWithCTXT () {
		String snippet = "msgctxt \"Context\"\n"
			+ "msgid \"Text 1\"\n"
			+ "msgstr \"\"\n";
		String expect = "msgctxt \"Context\"\n"
			+ "msgid \"Text 1\"\n"
			+ "msgstr \"Text 1\"\n";
		assertEquals(expect, FilterTestDriver.generateOutput(getEvents(snippet, locEN, locFR),
			filter.getEncoderManager(), locFR));
	}
	
	@Test
	public void testOuputAddTranslation () {
		String snippet = "msgid \"Text 1\"\n"
			+ "msgstr \"\"\n";
		String expect = "msgid \"Text 1\"\n"
			+ "msgstr \"Text 1\"\n";
		assertEquals(expect, FilterTestDriver.generateOutput(getEvents(snippet, locEN, locFR),
			filter.getEncoderManager(), locFR));
	}
	
	@Test
	public void testTUEmptyIDEntry () {
		String snippet = "msgid \"\"\n"
			+ "msgstr \"Some stuff\"\n";
		assertEquals(null, FilterTestDriver.getTextUnit(getEvents(snippet, locEN, locFR), 1));
	}

	@Test
	public void testTUContextParsing () {
		String snippet = "msgctxt \""+POWriter.CRUMBS_PREFIX+":tu=123\"\n"
			+ "msgid \"Source\"\n"
			+ "msgstr \"Target\"\n";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet, locEN, locFR), 1);
		assertNotNull(tu);
		assertEquals("Source", tu.getSource().toString());
		assertEquals("123", tu.getId());
	}

	@Test
	public void testNoQuoteOnSameLinee () {
		String snippet = "msgctxt \n\""+POWriter.CRUMBS_PREFIX+":tu=123\"\n"
			+ "msgid \n\"Source\"\n"
			+ "msgstr \n\"Target\"\n";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet, locEN, locFR), 1);
		assertNotNull(tu);
		assertEquals("Source", tu.getSource().toString());
		assertEquals("Target", tu.getTarget(locFR).toString());
		assertEquals("123", tu.getId());
	}
	
	@Test
	public void testOuputNoQuoteOnSameLinee () {
		String snippet = "msgctxt \n\""+POWriter.CRUMBS_PREFIX+":tu=123\"\n"
			+ "msgid \n\"Source\"\n"
			+ "msgstr \n\"Target\"\n";
		String expect = "msgctxt \"\"\n\""+POWriter.CRUMBS_PREFIX+":tu=123\"\n"
			+ "msgid \"\"\n\"Source\"\n"
			+ "msgstr \"\"\n\"Target\"\n";
		assertEquals(expect, FilterTestDriver.generateOutput(getEvents(snippet, locEN, locFR),
			filter.getEncoderManager(), locFR));
	}
	
	@Test
	public void testTUCompleteEntry () {
		String snippet = "#, fuzzy\n"
			+ "#. Comment\n"
			+ "#: Reference\n"
			+ "# Translator note\n"
			+ "#| Context1\n"
			+ "msgctxt \"Context2\"\n"
			+ "msgid \"Source\"\n"
			+ "msgstr \"Target\"\n";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet, locEN, locFR), 1);

		assertNotNull(tu);
		assertEquals("Source", tu.getSource().toString());
		assertEquals("Target", tu.getTarget(locFR).toString());

		assertTrue(tu.hasTargetProperty(locFR, Property.APPROVED));
		Property prop = tu.getTargetProperty(locFR, Property.APPROVED);
		assertEquals("no", prop.getValue());
		assertFalse(prop.isReadOnly());
		
		assertTrue(tu.hasProperty(Property.NOTE));
		prop = tu.getProperty(Property.NOTE);
		assertEquals("Comment", prop.getValue());
		assertTrue(prop.isReadOnly());
		
		assertTrue(tu.hasProperty(POFilter.PROPERTY_REFERENCES));
		prop = tu.getProperty(POFilter.PROPERTY_REFERENCES);
		assertEquals("Reference", prop.getValue());
		assertTrue(prop.isReadOnly());

		assertTrue(tu.hasProperty(Property.TRANSNOTE));
		prop = tu.getProperty(Property.TRANSNOTE);
		assertEquals("Translator note", prop.getValue());
		assertTrue(prop.isReadOnly());

		assertTrue(tu.hasProperty(POFilter.PROPERTY_CONTEXT));
		prop = tu.getProperty(POFilter.PROPERTY_CONTEXT);
		assertEquals("Context2", prop.getValue());
		assertTrue(prop.isReadOnly());
	}
	
	@Test
	public void testTUPluralEntry_DefaultGroup () {
		StartGroup sg = FilterTestDriver.getGroup(getEvents(makePluralEntry(), locEN, locFR), 1);
		assertNotNull(sg);
		assertEquals("x-gettext-plurals", sg.getType());
	}

	@Test
	public void testTUPluralEntry_DefaultSingular () {
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(makePluralEntry(), locEN, locFR), 1);
		assertNotNull(tu);
		assertEquals("untranslated-singular", tu.getSource().toString());
		assertFalse(tu.hasTarget(locFR));
	}

	@Test
	public void testTUPluralEntry_DefaultPlural () {
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(makePluralEntry(), locEN, locFR), 2);
		assertNotNull(tu);
		assertEquals("untranslated-plural", tu.getSource().toString());
		assertFalse(tu.hasTarget(locFR));
	}
	
	@Test
	public void testOuputPluralEntry () {
		String snippet = makePluralEntry();
		String result = FilterTestDriver.generateOutput(getEvents(snippet, locEN, locFR),
			filter.getEncoderManager(), locFR);
		String expected = "msgid \"untranslated-singular\"\n"
			+ "msgid_plural \"untranslated-plural\"\n"
			+ "msgstr[0] \"untranslated-singular\"\n"
			+ "msgstr[1] \"untranslated-plural\"\n";
		assertEquals(expected, result);
	}
	
	@Test
	public void testTrailingSkeleton () {
		String snippet = "#. translators: this is used in the view menu\n" +
				"#: ../src/file-manager/fm-icon-view.c:3049\n" +
				"msgid \"_Icons\"\n" +
				"msgstr \"_Ikone\"\n\n" +
				"#: ../src/file-manager/fm-icon-view.c:3050\n" +
				"#, fuzzy\n" +
				"msgid \"The icon view encountered an error.\"\n" + 
				"msgstr \"Die %s-aangesig het 'n probleem ondervind terwyl dit gelaai is.\"\n";
		String result = FilterTestDriver.generateOutput(getEvents(snippet, locEN, locEN),
			filter.getEncoderManager(), locEN);		
		assertEquals(snippet, result);
	}
		
	@Test
	public void testPluralEntryFuzzy () {
		String snippet = makePluralEntryFuzzy();
		// First TU
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet, locEN, locFR), 1);
		assertNotNull(tu);
		assertEquals("translation-singular", tu.getTarget(locFR).toString());
		Property prop = tu.getTargetProperty(locFR, Property.APPROVED);
		assertNotNull(prop);
		assertEquals("no", prop.getValue());
		assertEquals(MimeTypeMapper.PO_MIME_TYPE, tu.getMimeType());
		// Second TU
		tu = FilterTestDriver.getTextUnit(getEvents(snippet, locEN, locFR), 2);
		assertNotNull(tu);
		assertEquals("translation-plural", tu.getTarget(locFR).toString());
		prop = tu.getTargetProperty(locFR, Property.APPROVED);
		assertNotNull(prop);
		assertEquals("no", prop.getValue());
	}
		
	@Test
	public void testOuputPluralEntryFuzzy () {
		String snippet = makePluralEntryFuzzy();
		String result = FilterTestDriver.generateOutput(getEvents(snippet, locEN, locFR),
			filter.getEncoderManager(), locFR);
		assertEquals(snippet, result);
	}

	@Test
	public void testDoubleExtraction () {
		// Read all files in the data directory
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
		list.add(new InputDocument(root.in("/Test01.po").toString(), null));
		list.add(new InputDocument(root.in("/Test02.po").toString(), null));
		list.add(new InputDocument(root.in("/Test03.po").toString(), null));
		list.add(new InputDocument(root.in("/Test04.po").toString(), null));
		list.add(new InputDocument(root.in("/Test05.po").toString(), null));
		list.add(new InputDocument(root.in("/TestMonoLingual_EN.po").toString(), "okf_po@Monolingual.fprm"));
		list.add(new InputDocument(root.in("/TestMonoLingual_FR.po").toString(), "okf_po@Monolingual.fprm"));
		list.add(new InputDocument(root.in("/AllCasesTest.po").toString(), null));
		list.add(new InputDocument(root.in("/potest.po").toString(), null));
		list.add(new InputDocument(root.in("/fail.po").toString(), null));
		list.add(new InputDocument(root.in("/Test_nautilus.af.po").toString(), null));
		list.add(new InputDocument(root.in("/Test_DrupalRussianCP1251.po").toString(), null));
		list.add(new InputDocument(root.in("/POT-Test01.pot").toString(), null));
	
		RoundTripComparison rtc = new RoundTripComparison();
		assertTrue(rtc.executeCompare(filter, list, "UTF-8", locEN, locFR));
	}

	@Test
	public void testOnePlural() throws Exception {
		RawDocument rd = new RawDocument(root.in("/plurals.po").asUri(), "UTF-8", locEN);
		List<Event> events = FilterTestDriver.getTextUnitEvents(filter, rd);
		assertEquals(2, events.size());
		assertEquals("1 gizmo", events.get(0).getTextUnit().getSource().toString());
		assertEquals("All doodads", events.get(1).getTextUnit().getSource().toString());
	}

	@Test
	public void testMsgCtxtAsNotes() throws Exception {
	    RawDocument rd = new RawDocument(root.in("/msgctxt_notes.po").asUri(), "UTF-8", locEN);
	    rd.setTargetLocale(locFR);
	    // Baseline behavior: option disabled
	    List<Event> events = FilterTestDriver.getTextUnitEvents(filter, rd);
        assertEquals(2, events.size());
        assertEquals("Hello world", events.get(0).getTextUnit().getSource().toString());
        assertEquals("This is the first string", events.get(0).getTextUnit().getProperty(Property.NOTE).getValue());
        assertEquals("Hello world", events.get(1).getTextUnit().getSource().toString());
        assertNull(events.get(1).getTextUnit().getProperty(Property.NOTE));

        // Now flip the switch
        filter.getParameters().setIncludeMsgContextInNote(true);
        events = FilterTestDriver.getTextUnitEvents(filter, rd);
        assertEquals(2, events.size());
        assertEquals("Hello world", events.get(0).getTextUnit().getSource().toString());
        assertNotNull(events.get(0).getTextUnit().getProperty(Property.NOTE));
        assertEquals("Context A: This is the first string", events.get(0).getTextUnit().getProperty(Property.NOTE).getValue());
        assertEquals("Hello world", events.get(1).getTextUnit().getSource().toString());
        assertNotNull(events.get(1).getTextUnit().getProperty(Property.NOTE));
        assertEquals("Context B", events.get(1).getTextUnit().getProperty(Property.NOTE).getValue());
	}

	private ArrayList<Event> getEvents(String snippet,
		LocaleId srcLang,
		LocaleId trgLang)
	{
		return FilterTestDriver.getEvents(filter, snippet, srcLang, trgLang);
	}

	private String makePluralEntry () {
		return "msgid \"untranslated-singular\"\n"
			+ "msgid_plural \"untranslated-plural\"\n"
			+ "msgstr[0] \"\"\n"
			+ "msgstr[1] \"\"\n";
	}

	private String makePluralEntryFuzzy () {
		return "#, fuzzy\n"
			+ "msgid \"untranslated-singular\"\n"
			+ "msgid_plural \"untranslated-plural\"\n"
			+ "msgstr[0] \"translation-singular\"\n"
			+ "msgstr[1] \"translation-plural\"\n";
	}

}
