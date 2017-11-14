/*===========================================================================
  Copyright (C) 2014-2017 by the Okapi Framework contributors
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

package net.sf.okapi.filters.xliff2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.FileLocation;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filters.FilterTestDriver;
import net.sf.okapi.common.filters.InputDocument;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.common.resource.TextUnit;
import net.sf.okapi.lib.xliff2.core.Unit;

@RunWith(JUnit4.class)
public class XLIFF2FilterTest {

	private XLIFF2Filter filter;
	private FileLocation fl;
	private LocaleId locEN = LocaleId.fromString("en");
	private LocaleId locFR = LocaleId.fromString("fr");

	@Before
	public void setUp() {
		filter = new XLIFF2Filter();
		fl = FileLocation.fromClass(this.getClass());
	}

	@Test
	public void testSimple() {
		String snippet = "<?xml version='1.0'?>\n"
			+ "<xliff xmlns='urn:oasis:names:tc:xliff:document:2.0' version='2.0' srcLang='en' trgLang='fr'>"
			+ "<file id='f1'>\n" + "<unit id='u1'>" + "<segment id='s1'>\n" + "<source>Text.</source>"
			+ "</segment>\n" + "<ignorable><source> </source></ignorable>" + "<segment id='s2'>\n"
			+ "<source>src2</source>" + "<target>trg2</target>" + "</segment>\n" + "</unit>" + "</file>\n"
			+ "</xliff>\n";
		List<Event> events = FilterTestDriver.getEvents(filter, snippet, locEN, locFR);
		ITextUnit tu = FilterTestDriver.getTextUnit(events, 1);
		assertNotNull(tu);
		TextContainer stc = tu.getSource();
		assertEquals("Text.", stc.getFirstContent().toText());
		TextContainer ttc = tu.getTarget(locFR);
		assertTrue(ttc.getParts().get(0).getContent().isEmpty());
		assertEquals(" ", stc.getParts().get(1).getContent().getCodedText());
		assertEquals("src2", tu.getSource().getSegments().get("s2").getContent().toString());
		assertEquals("trg2", tu.getTarget(locFR).getSegments().get("s2").getContent().toString());
	}

	@Test
	public void testInline() {
		String snippet = "<?xml version='1.0'?>\n"
			+ "<xliff xmlns='urn:oasis:names:tc:xliff:document:2.0' version='2.0' srcLang='en' trgLang='fr'>"
			+ "<file id='f1'>\n" + "<unit id='u1'>" + "<segment id='s1'>\n"
			+ "<source><pc id='1' canDelete='no' dispStart='SC' dispEnd='EC'>"
			+ "<ph id='ph1' canCopy='no'/></pc></source>"
			+ "<target><pc id='1' canDelete='no' dispStart='SC' dispEnd='EC'>"
			+ "<ph id='ph1' canCopy='no'/></pc></target>" + "</segment>\n" + "</unit>" + "</file>\n" + "</xliff>\n";
		List<Event> events = FilterTestDriver.getEvents(filter, snippet, locEN, locFR);
		ITextUnit tu = FilterTestDriver.getTextUnit(events, 1);
		TextFragment frag = tu.getSource().getFirstContent();
		assertEquals(frag.getCodes().size(), 3);
		// Start pc
		Code code = frag.getCode(0);
		assertEquals(1, code.getId());
		assertEquals(null, code.getOriginalId());
		assertEquals(TagType.OPENING, code.getTagType());
		assertEquals("SC", code.getDisplayText());
		assertTrue(code.isCloneable());
		assertFalse(code.isDeleteable());
		// Placeholder
		code = frag.getCode(1);
		assertEquals(110905, code.getId());
		assertEquals("ph1", code.getOriginalId());
		assertEquals(TagType.PLACEHOLDER, code.getTagType());
		assertFalse(code.isCloneable());
		assertTrue(code.isDeleteable());
		// End pc
		code = frag.getCode(2);
		assertEquals(1, code.getId());
		assertEquals(TagType.CLOSING, code.getTagType());
		assertEquals("EC", code.getDisplayText());
		assertTrue(code.isCloneable());
		assertFalse(code.isDeleteable());
	}

	@Test
	public void testFromFile() {
		ITextUnit tu = FilterTestDriver.getTextUnit(filter, new InputDocument(
			fl.in("/test01.xlf").toString(), null), "UTF-8", locEN, locFR, 4);
		assertNotNull(tu);

		TextFragment tf = tu.getSource().getFirstContent();
		// TODO: should the \n be there?
		assertEquals("special text and more\n.", tf.getText());
		Code c = tf.getCode(0);
		assertEquals(1, c.getId());
	}

	@Test
	public void testFromEscapedFile() {
		ITextUnit tu = FilterTestDriver.getTextUnit(filter, new InputDocument(
			fl.in("/escaped.xlf").toString(), null), "UTF-8", locEN, locFR, 1);
		assertNotNull(tu);
		TextFragment tf = tu.getSource().getFirstContent();
		assertEquals("<p>This is a value that <em>I want</em> to be correctly translated.</p>", tf.getText());
		@SuppressWarnings("resource")
		List<Event> events = FilterTestDriver.getEvents(filter, new RawDocument(
			fl.in("/escaped.xlf").asInputStream(), "UTF-8", locEN, locFR), null);
		String result = FilterTestDriver.generateOutput(events, locFR, filter.createSkeletonWriter(),
			filter.getEncoderManager());
		assertTrue(result.contains("&lt;p>"));
	}

	@Test
	public void simpleRoundTrip() {
		String snippet = "<?xml version=\"1.0\"?>\n"
			+ "<xliff xmlns=\"urn:oasis:names:tc:xliff:document:2.0\" version=\"2.0\" srcLang=\"en\" trgLang=\"fr\">\n"
			+ "<file id=\"f1\">\n" + "<unit id=\"u1\">" + "<segment id=\"s1\">\n" + "<source>Text.</source>"
			+ "</segment>\n" + "<ignorable><source> </source></ignorable>" + "<segment id=\"s2\">\n"
			+ "<source>src2</source>" + "<target>trg2</target>" + "</segment>\n" + "</unit>" + "</file>\n"
			+ "</xliff>\n";
		List<Event> events = FilterTestDriver.getEvents(filter, snippet, locEN, locFR);
		String result = FilterTestDriver.generateOutput(events, locFR, filter.createSkeletonWriter(),
			filter.getEncoderManager());
		// assertEquals(snippet, result);
	}

	@Ignore("Test conversion with an inline code, currently fails")
	@Test
	public void convertTextUnitToUnit() {
		Okp2X2Converter c = new Okp2X2Converter(true, LocaleId.FRENCH);
		TextFragment s = new TextFragment();
		TextFragment t = new TextFragment();
		Code code = new Code(TagType.PLACEHOLDER, "bold", "b");
		s.append(code);
		t.append(code);
		ITextUnit tu = new TextUnit("1");
		tu.setSource(new TextContainer(s));
		tu.setTarget(LocaleId.FRENCH, new TextContainer(t));
		Unit unit = c.convert(tu);
		assertEquals(1, unit.getSegmentCount());
	}

}
