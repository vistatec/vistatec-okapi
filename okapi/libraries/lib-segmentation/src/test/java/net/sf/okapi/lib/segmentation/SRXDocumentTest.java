/*===========================================================================
  Copyright (C) 2008-2010 by the Okapi Framework contributors
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

package net.sf.okapi.lib.segmentation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.FileLocation;
import net.sf.okapi.common.ISegmenter;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Range;
import net.sf.okapi.common.filterwriter.GenericContent;
import net.sf.okapi.common.resource.ISegments;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.filters.html.HtmlFilter;
import net.sf.okapi.filters.plaintext.PlainTextFilter;

@RunWith(JUnit4.class)
public class SRXDocumentTest {
	private FileLocation location = FileLocation.fromClass(SRXDocumentTest.class);

	@Before
	public void setUp() {
	}

	@Test
	public void testGetSet () {
		SRXDocument doc = new SRXDocument();
		
		// Check defaults
		assertFalse(doc.cascade());
		assertFalse(doc.includeStartCodes());
		assertTrue(doc.includeEndCodes());
		assertFalse(doc.includeIsolatedCodes());
		assertTrue(doc.segmentSubFlows());
		assertFalse(doc.oneSegmentIncludesAll());
		assertFalse(doc.trimLeadingWhitespaces());
		assertFalse(doc.trimTrailingWhitespaces());
		assertFalse(doc.treatIsolatedCodesAsWhitespace());
		
		// Check changing options
		doc.setCascade(true);
		assertTrue(doc.cascade());
		doc.setIncludeStartCodes(true);
		assertTrue(doc.includeStartCodes());
		doc.setIncludeEndCodes(false);
		assertFalse(doc.includeEndCodes());
		doc.setIncludeIsolatedCodes(true);
		assertTrue(doc.includeIsolatedCodes());
		doc.setSegmentSubFlows(false);
		assertFalse(doc.segmentSubFlows());
		doc.setOneSegmentIncludesAll(true);
		assertTrue(doc.oneSegmentIncludesAll());
		doc.setTrimLeadingWhitespaces(true);
		assertTrue(doc.trimLeadingWhitespaces());
		doc.setTrimTrailingWhitespaces(true);
		assertTrue(doc.trimTrailingWhitespaces());
		doc.setTreatIsolatedCodesAsWhitespace(true);
		assertTrue(doc.treatIsolatedCodesAsWhitespace());
	}

	@Test
	public void testObjects () {
		// Check rule
		Rule rule = new Rule();
		assertEquals(rule.getBefore(), "");
		assertEquals(rule.getAfter(), "");
		assertTrue(rule.isBreak());
		assertTrue(rule.isActive());
		String tmp = "regex";
		rule.setAfter(tmp);
		assertEquals(rule.getAfter(), tmp);
		rule.setBefore(tmp);
		assertEquals(rule.getBefore(), tmp);
		rule.setBreak(false);
		assertFalse(rule.isBreak());
		rule.setActive(false);
		assertFalse(rule.isActive());
		
		// Check LanguageMap
		String pattern = "pattern";
		String ruleName = "ruleName";
		LanguageMap lm = new LanguageMap(pattern, ruleName);
		assertEquals(lm.getPattern(), pattern);
		assertEquals(lm.getRuleName(), ruleName);
	}
	
	@Test
	public void testRules () {
		SRXDocument doc = new SRXDocument();
		doc.setCascade(true);

		ArrayList<Rule> list = new ArrayList<Rule>();
		list.add(new Rule("Mr\\.", "\\s", false));
		doc.addLanguageRule("english", list);
		ArrayList<Rule> rules = doc.getLanguageRules("english");
		assertEquals(rules.size(), 1);
		
		list = new ArrayList<Rule>();
		list.add(new Rule("\\.+", "\\s", true));
		doc.addLanguageRule("default", list);

		doc.addLanguageMap(new LanguageMap("en.*", "english"));
		doc.addLanguageMap(new LanguageMap(".*", "default"));

		SRXSegmenter seg = (SRXSegmenter)doc.compileLanguageRules(
			LocaleId.fromString("en"), null);
		assertNotNull(seg);
		assertEquals(seg.getLanguage(), "en");
		assertNull(seg.getRanges()); // Null set yet
		seg.computeSegments("Mr. Holmes. The detective.");
		assertNotNull(seg.getRanges());
		assertEquals(seg.getRanges().size(), 2);
		seg.computeSegments("MR. Holmes. The detective.");
		assertEquals(seg.getRanges().size(), 3);
		
		TextFragment tf = new TextFragment("One.");
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append(" Two.");
		tf.append(TagType.CLOSING, "b", "</b>");
		TextContainer tc = new TextContainer(tf);
		seg.setOptions(true, true, true, false, false, false, false, true, false, false);
		seg.computeSegments(tc);
		// "One.XX Two.YY" --> "[One.XX][ Two.YY]"
		List<Range> ranges = seg.getRanges();
		assertNotNull(ranges);
		assertEquals(ranges.size(), 2);
		assertEquals(6, ranges.get(0).end);
		assertEquals(6, ranges.get(1).start);
		seg.setOptions(true, false, true, false, false, false, false, true, false, false);
		seg.computeSegments(tc);
		// "One.XX Two.YY" --> "[One.][XX Two.YY]"
		ranges = seg.getRanges();
		assertNotNull(ranges);
		assertEquals(2, ranges.size());
		assertEquals(4, ranges.get(0).end);
		assertEquals(4, ranges.get(1).start);
	}

	@Test
	public void testComments () {
		SRXDocument doc = createDocument();
		assertNotNull(doc.getComments());
		assertEquals("Main comment", doc.getComments());
		assertNotNull(doc.getHeaderComments());
		assertEquals("Header comment", doc.getHeaderComments());
		Map<String, ArrayList<Rule>> list = doc.getAllLanguageRules();
		assertNotNull(list);
		ArrayList<Rule> rules = list.get("default");
		assertNotNull(rules);
		assertEquals(1, rules.size());
		assertNotNull(rules.get(0).getComment());
		assertEquals("Rule comment", rules.get(0).getComment());
	}
	
	@Test
	public void testSimpleRule () {
		SRXDocument doc = createDocument();
		Map<String, ArrayList<Rule>> list = doc.getAllLanguageRules();
		assertNotNull(list);
		ArrayList<Rule> rules = list.get("default");
		assertNotNull(rules);
		assertEquals(1, rules.size());
		assertEquals("([A-Z]\\.){2,}", rules.get(0).getBefore());
		assertEquals("\\s", rules.get(0).getAfter());
		assertFalse(rules.get(0).isBreak());
	}
	
	@Test
	public void testLoadRulesFromStream () {
		SRXDocument doc = createDocument();
		// Use the SRX in the package tree
		doc.loadRules(location.in("Test02.srx").asInputStream());
		Map<String, ArrayList<Rule>> list = doc.getAllLanguageRules();
		assertNotNull(list);
		ArrayList<Rule> rules = list.get("default");
		assertNotNull(rules);
		assertEquals(2, rules.size());
	}
	
	@Test
	public void testLoadRulesFromPath () {
		SRXDocument doc = createDocument();
		// Use the Test01.srx at the root level (not in the package tree)
		doc.loadRules(location.in("/Test01.srx").toString());
		Map<String, ArrayList<Rule>> list = doc.getAllLanguageRules();
		assertNotNull(list);
		ArrayList<Rule> rules = list.get("default");
		assertNotNull(rules);
		assertEquals(2, rules.size());
	}

	@Test
	public void testTradosCompatibility ()
		throws URISyntaxException, IOException
	{
		String testFile = "TradosTest";
		GenericContent fmt = new GenericContent();

		// Prepare the gold file
		BufferedReader reader = new BufferedReader(new InputStreamReader(
			location.in("/" + testFile + ".tmx.txt").asInputStream(), "UTF-8"));
		
		// Prepare the rules
		SRXDocument srxDoc = new SRXDocument();
		srxDoc.loadRules(location.in("/tradosSegmentation.srx").toString());
		ISegmenter segmenter = srxDoc.compileLanguageRules(LocaleId.ENGLISH, null);
		
		// Prepare the HTML input
		RawDocument rd = new RawDocument(location.in("/" + testFile + ".html").asUri(), "UTF-8", LocaleId.ENGLISH);
		HtmlFilter filter = new HtmlFilter();

		// Process the HTML file
		try {
			filter.open(rd);
			while ( filter.hasNext() ) {
				// Get the next text unit
				Event event = filter.next();
				if ( !event.isTextUnit() ) continue; // Skip other events
				ITextUnit tu = event.getTextUnit();
				
				// Apply the segmentation to the source content of the text unit
				TextContainer tc = tu.getSource();
				segmenter.computeSegments(tc);
				tc.getSegments().create(segmenter.getRanges());
				
				// Loop through the segments
				for ( Segment seg : tc.getSegments() ) {
					// Renumber in-line code
					TextFragment tf = seg.getContent();
					tf.renumberCodes(1);
					// Format the segment
					String okapiText = fmt.setContent(tf).toString();
					// Get the next line in the golf file
					String goldText = reader.readLine();
					// Compare with the two strings
					assertEquals(goldText, okapiText);
				}
			}
		}
		finally {
			if ( filter != null ) filter.close();
			if ( reader != null ) reader.close();
		}
	}
	
	@Test
	public void testChinese ()
		throws URISyntaxException, IOException
	{
		// Prepare the rules
		SRXDocument srxDoc = new SRXDocument();
		srxDoc.loadRules(location.in("/defaultSegmentation.srx").toString());
		ISegmenter segmenter = srxDoc.compileLanguageRules(LocaleId.ENGLISH, null);
		
		try (PlainTextFilter filter = new PlainTextFilter();
			 RawDocument rd = new RawDocument(location.in("/chinese.txt").asUri(), "UTF-16LE", LocaleId.CHINA_CHINESE);
			 BufferedReader reader = new BufferedReader(new InputStreamReader(
								location.in("/chinese.txt").asInputStream(), "UTF-16LE"))) {
			filter.open(rd);
			while ( filter.hasNext() ) {
				// Get the next text unit
				Event event = filter.next();
				if ( !event.isTextUnit() ) continue; // Skip other events
				ITextUnit tu = event.getTextUnit();
				
				// Apply the segmentation to the source content of the text unit
				TextContainer tc = tu.getSource();
				segmenter.computeSegments(tc);
				tc.getSegments().create(segmenter.getRanges());
				
				// Loop through the segments
				for ( Segment seg : tc.getSegments() ) {
					// Renumber in-line code
					TextFragment tf = seg.getContent();
					tf.renumberCodes(1);
					// Format the segment
//					String okapiText = fmt.setContent(tf).toString();
//					// Get the next line in the golf file
//					String goldText = reader.readLine();
//					// Compare with the two strings
//					assertEquals(goldText, okapiText);
				}
			}
		}
	}

	@Test
	public void testLoadIsolatedCodeRules () {
		SRXDocument doc = createDocument();
		// Use the SRX in the package tree
		doc.loadRules(location.in("/isolated_code.srx").asInputStream());
		Map<String, ArrayList<Rule>> list = doc.getAllLanguageRules();
		assertNotNull(list);
		ArrayList<Rule> rules = list.get("default");
		assertNotNull(rules);

		SRXSegmenter seg = (SRXSegmenter)doc.compileLanguageRules(
				LocaleId.fromString("en"), null);
		TextFragment tf = new TextFragment("One.");
		tf.append(TagType.PLACEHOLDER, "x", "<x/>");
		tf.append("Two.");
		TextContainer tc = new TextContainer(tf);
		ISegments segments = tc.getSegments();
		int n = seg.computeSegments(tc);
		assertEquals(2, n);
		segments.create(seg.getRanges());
		assertEquals(2, segments.count());
		assertEquals("One.", segments.get(0).toString());
		assertEquals("<x/>Two.", segments.get(1).toString());
	}

	private SRXDocument createDocument () {
		SRXDocument doc = new SRXDocument();
		String srx = "<!--Main comment-->"
			+ "<srx xmlns='http://www.lisa.org/srx20' version='2.0'>"
			+ "<!--Header comment-->"
			+ "<header segmentsubflows='yes' cascade='no'>"
			+ "<formathandle type='start' include='no'/>"
			+ "<formathandle type='end' include='yes'/>"
			+ "<formathandle type='isolated' include='no'/>"
			+ "</header>"
			+ "<body>"
			+ "<languagerules>"
			+ "<languagerule languagerulename='default'>"
			+ "<!--Rule comment-->"
			+ "<rule break='no'>"
			+ "<beforebreak>([A-Z]\\.){2,}</beforebreak>"
			+ "<afterbreak>\\s</afterbreak>"
			+ "</rule>"
			+ "</languagerule>"
			+ "</languagerules>"
			+ "<maprules>"
			+ "<languagemap languagepattern='.*' languagerulename='default'/>"
			+ "</maprules>"
			+ "</body></srx>";
		doc.loadRules((CharSequence)srx);
		return doc;
	}
}
