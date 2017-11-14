/*===========================================================================
  Copyright (C) 2010-2017 by the Okapi Framework contributors
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

package org.w3c.its;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import net.sf.okapi.common.FileLocation;
import net.sf.okapi.common.Namespaces;
import net.sf.okapi.common.annotation.GenericAnnotation;
import net.sf.okapi.common.annotation.GenericAnnotationType;
import net.sf.okapi.common.annotation.GenericAnnotations;
import nu.validator.htmlparser.dom.HtmlDocumentBuilder;

@RunWith(JUnit4.class)
public class TraversalTest {

	private FileLocation root;
	//private LocaleId locEN = LocaleId.fromString("en");
	private DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
	private HtmlDocumentBuilder htmlDocBuilder = new HtmlDocumentBuilder();

	@Before
	public void setUp() {
		root = FileLocation.fromClass(this.getClass());
		fact.setNamespaceAware(true);
		fact.setValidating(false);
	}

	@Test
	public void testSimple () throws SAXException, IOException, ParserConfigurationException {
		Document doc = fact.newDocumentBuilder().parse(root.in("/input.xml").toString());
		ITraversal trav = applyITSRules(doc, root.in("/input.xml").asUri(), false, null);
		Element elem = getElement(trav, "p", 1);
		assertNotNull(elem);
		assertTrue(trav.getTranslate(null));
		elem = getElement(trav, "term", 1);
		assertNotNull(elem);
		assertFalse(trav.getTranslate(null));
	}

	@Test
	public void testTerm () throws SAXException, IOException, ParserConfigurationException {
		Document doc = fact.newDocumentBuilder().parse(root.in("/input.xml").toString());
		ITraversal trav = applyITSRules(doc, root.in("/input.xml").asUri(), false, null);
		Element elem = getElement(trav, "p", 1);
		assertNotNull(elem);
		assertFalse(trav.getTerm(null));
		elem = getElement(trav, "term", 1);
		assertNotNull(elem);
		assertTrue(trav.getTerm(null));
		assertEquals("the relationship, expressed through discourse\n"
			+ "structure, between the implied author or some other addresser,\n"
			+ "and the fiction.", trav.getTermInfo(null));
	}

	@Test
	public void testTermPointerwithID () throws SAXException, IOException, ParserConfigurationException {
		InputSource is = new InputSource(new StringReader("<text>"
			+ "<its:rules xmlns:its=\"http://www.w3.org/2005/11/its\" version=\"2.0\">"
			+ "<its:termRule selector=\"//term\" term=\"yes\" termInfoPointer=\"id(@def)\"/>"
			+ "</its:rules>"
			+ "<p>We may define	<term def=\"TDPV\">discoursal point of view</term> as "
			+ "<gloss xml:id=\"TDPV\">the relationship, etc.</gloss>.</p></text>"));
		Document doc = fact.newDocumentBuilder().parse(is);
		ITraversal trav = applyITSRules(doc, null, false, null);
		Element elem = getElement(trav, "term", 1);
		assertNotNull(elem);
		assertTrue(trav.getTerm(null));
		assertEquals("the relationship, etc.", trav.getTermInfo(null));
	}

	@Test
	public void testTermPointer () throws SAXException, IOException, ParserConfigurationException {
		InputSource is = new InputSource(new StringReader("<doc>"
			+ "<its:rules xmlns:its=\"http://www.w3.org/2005/11/its\" version=\"2.0\">"
			+ "<its:param name=\"termInfoRefPointer\">target</its:param>"
			+ "<its:termRule selector=\"//term\" term=\"yes\" "
			+ " termInfoRefPointer=\"@*[local-name() = $termInfoRefPointer]\"/>"
			+ "</its:rules>"
			+ "<p>We may define<term target=\"#TDPV\">discoursal point of view</term> as <gloss xml:id=\"TDPV\">the "
			+ "relationship, etc.</gloss></p>"
			+ "</doc>"));
		Document doc = fact.newDocumentBuilder().parse(is);
		ITraversal trav = applyITSRules(doc, null, false, null);
		Element elem = getElement(trav, "term", 1);
		assertNotNull(elem);
		assertTrue(trav.getTerm(null));
		assertEquals("REF:#TDPV", trav.getTermInfo(null));
	}

	@Test
	public void testTermLocally () throws SAXException, IOException, ParserConfigurationException {
		InputSource is = new InputSource(new StringReader("<doc xmlns:its=\"http://www.w3.org/2005/11/its\" its:version=\"2.0\">"
			+ "<p><span its:term='yes' its:termInfoRef='ref1' its:termConfidence='0.5'>"
			+ "<its:span term='yes' termInfoRef='ref2' termConfidence='1'>Capital</its:span> city</span></p>"
			+ "</doc>"));
		Document doc = fact.newDocumentBuilder().parse(is);
		ITraversal trav = applyITSRules(doc, null, false, null);
		Element elem = getElement(trav, "span", 1);
		assertEquals("Capital city", elem.getTextContent());
		assertTrue(trav.getTerm(null));
		assertEquals("REF:ref1", trav.getTermInfo(null));
		assertEquals(0.5, trav.getTermConfidence(null), 0.0);
		elem = getElement(trav, "its:span", 1);
		assertEquals("Capital", elem.getTextContent());
		assertTrue(trav.getTerm(null));
		assertEquals("REF:ref2", trav.getTermInfo(null));
		assertEquals(1.0, trav.getTermConfidence(null), 0.0);
	}
	
	@Test
	public void testXmlId () throws SAXException, IOException, ParserConfigurationException {
		Document doc = fact.newDocumentBuilder().parse(root.in("/input.xml").toString());
		ITraversal trav = applyITSRules(doc, root.in("/input.xml").asUri(), false, null);
		Element elem = getElement(trav, "gloss", 1);
		assertNotNull(elem);
		assertEquals("TDPV", trav.getIdValue(null));
	}

	@Test
	public void testLocNote () throws SAXException, IOException, ParserConfigurationException {
		InputSource is = new InputSource(new StringReader("<doc>"
			+ "<i:rules xmlns:i='"+Namespaces.ITS_NS_URI+"' version='2.0'>"
			+ "<i:locNoteRule selector='//p' locNoteType='description' locNotePointer='@note'/>"
			+ "</i:rules>"
			+ "<p note='some note'>text</p></doc>"));
		Document doc = fact.newDocumentBuilder().parse(is);
		ITraversal trav = applyITSRules(doc, null, false, null);
		Element elem = getElement(trav, "p", 1);
		assertNotNull(elem);
		assertEquals("some note", trav.getLocNote(null));
		assertEquals("description", trav.getLocNoteType(null));
	}

	@Test
	public void testWithinText () throws SAXException, IOException, ParserConfigurationException {
		Document doc = fact.newDocumentBuilder().parse(root.in("/Translate1.xml").toString());
		ITraversal trav = applyITSRules(doc, root.in("/Translate1.xml").asUri(), false, null);
		Element elem = getElement(trav, "verbatim", 1);
		assertNotNull(elem);
		assertFalse(trav.getTranslate(null));
		assertTrue(trav.getWithinText()==ITraversal.WITHINTEXT_YES);
	}

	@Test
	public void testWithinTextLocalSpan () throws SAXException, IOException, ParserConfigurationException {
		InputSource is = new InputSource(new StringReader("<doc xmlns:i='"+Namespaces.ITS_NS_URI+"' i:version='2.0'>"
			+ "<p>Text <i:span withinText='yes'>span</i:span> text</p></doc>"));
		Document doc = fact.newDocumentBuilder().parse(is);
		ITraversal trav = applyITSRules(doc, null, false, null);
		getElement(trav, "p", 1);
		assertTrue(trav.getWithinText()==ITraversal.WITHINTEXT_NO);
		getElement(trav, "i:span", 1);
		assertTrue(trav.getWithinText()==ITraversal.WITHINTEXT_YES);
	}
	
	@Test
	public void testDomainGlobal () throws SAXException, IOException, ParserConfigurationException {
		InputSource is = new InputSource(new StringReader("<doc>"
			+ "<i:rules xmlns:i='"+Namespaces.ITS_NS_URI+"' version='2.0'>"
			+ "<i:domainRule selector='//doc' domainPointer='head/subject' "
			+ " domainMapping=\"dom1 finalDom1, 'dom2 VAL' 'final dom2'\"/>"
			+ "</i:rules>"
			+ "<head><subject>dom1, dom3</subject><subject>'dom2 val', \"Dom4\"</subject></head><p id='abc'>text</p></doc>"));
		Document doc = fact.newDocumentBuilder().parse(is);
		ITraversal trav = applyITSRules(doc, null, false, null);
		Element elem = getElement(trav, "doc", 1);
		assertNotNull(elem);
		assertEquals("finalDom1, dom3, dom2 val, Dom4", trav.getDomains(null));
		elem = getElement(trav, "head", 1);
		assertNotNull(elem);
		assertEquals("finalDom1, dom3, dom2 val, Dom4", trav.getDomains(null));
		elem = getElement(trav, "p", 1);
		assertNotNull(elem);
		assertEquals("finalDom1, dom3, dom2 val, Dom4", trav.getDomains(null));
		Attr attr = elem.getAttributeNode("id");
		assertEquals("finalDom1, dom3, dom2 val, Dom4", trav.getDomains(attr));
	}

	@Test
	public void testMtConfidenceLocal () throws SAXException, IOException, ParserConfigurationException {
		InputSource is = new InputSource(new StringReader("	<text xmlns:its='http://www.w3.org/2005/11/its' its:version='2.0' "
			+ " its:annotatorsRef='mt-confidence|file:///tools.xml#T1'><body><p>"
			+ "<span its:mtConfidence='0.8982'>Dublin is the "
			+ "<its:span mtConfidence='1' annotatorsRef='mt-confidence|uri2'>capital city</its:span> of Ireland.</span></p></body></text>"));
		Document doc = fact.newDocumentBuilder().parse(is);
		ITraversal trav = applyITSRules(doc, null, false, null);
		Element elem = getElement(trav, "span", 1);
		assertNotNull(elem);
		assertEquals(0.8982, trav.getMtConfidence(null), 0.0);
		assertEquals("mt-confidence|file:///tools.xml#T1", trav.getAnnotatorsRef());
		elem = getElement(trav, "its:span", 1);
		assertNotNull(elem);
		assertEquals("capital city", elem.getTextContent());
		assertEquals(1.0, trav.getMtConfidence(null), 0.0);
		assertEquals("mt-confidence|uri2", trav.getAnnotatorsRef());
	}

	@Test
	public void testMtConfidenceLocalHtml () throws SAXException, IOException, ParserConfigurationException {
		InputSource is = new InputSource(new StringReader("<!DOCTYPE html><html lang=en><head>"
			+ "<meta charset=utf-8><title>Title</title></head>"
			+ "<body its-annotators-ref='MT-ConFiDeNce|file:///tools.xml#T1'><p>"
			+ "<span its-mt-confidence=0.8982>Dublin is the capital of Ireland.</span> "
			+ "<span its-mt-confidence=0.8536 its-annotators-ref='mt-Confidence|t2'>The capital of the Czech Republic is Prague.</span>"
			+ "</p></body>/html>"));
		Document doc = htmlDocBuilder.parse(is);
		ITraversal trav = applyITSRules(doc, null, true, null);
		Element elem = getElement(trav, "span", 1);
		assertNotNull(elem);
		assertEquals(0.8982, trav.getMtConfidence(null), 0.0);
		assertEquals("mt-confidence|file:///tools.xml#T1", trav.getAnnotatorsRef());
		assertEquals("file:///tools.xml#T1", trav.getAnnotatorRef("mt-confidence"));
		elem = getElement(trav, "span", 2);
		assertNotNull(elem);
		assertEquals(0.8536, trav.getMtConfidence(null), 0.0);
		assertEquals("mt-confidence|t2", trav.getAnnotatorsRef());
		assertEquals("t2", trav.getAnnotatorRef("mt-confidence"));
	}

	@Test
	public void testMtConfidenceGlobalHtml () throws SAXException, IOException, ParserConfigurationException {
		InputSource is = new InputSource(new StringReader("<!DOCTYPE html><html lang=en><head><meta charset=utf-8>"
			+ "<script type='application/its+xml'>"
			+ "<its:rules xmlns:its='http://www.w3.org/2005/11/its' version='2.0' "
			+ "xmlns:h='http://www.w3.org/1999/xhtml'>"
			+ " <its:mtConfidenceRule mtConfidence='0.785' selector=\"//h:img[@src='src1']/@title\"/>"        
			+ " <its:mtConfidenceRule mtConfidence='0.805' selector=\"//h:img[@src='src2']/@title\"/>"        
			+ "</its:rules>"
			+ "</script>"
			+ "<title>TM confidence</title></head>"
			+ "<body its-annotators-ref='mt-conFIDENCE|file:///tools.xml#T1'><p>"
			+ "<img src='src1' title='Front gate of Trinity College Dublin'/>"
			+ "<img src='src2' title='A tart with a cart'/></p></body></html>"));
		Document doc = htmlDocBuilder.parse(is);
		ITraversal trav = applyITSRules(doc, null, true, null);
		Element elem = getElement(trav, "img", 1);
		assertNotNull(elem);
		Attr attr = elem.getAttributeNode("title");
		assertEquals(0.785, trav.getMtConfidence(attr), 0.0);
		assertEquals("mt-confidence|file:///tools.xml#T1", trav.getAnnotatorsRef());
		assertEquals("file:///tools.xml#T1", trav.getAnnotatorRef("mt-confidence"));
		elem = getElement(trav, "img", 2);
		assertNotNull(elem);
		attr = elem.getAttributeNode("title");
		assertEquals(0.805, trav.getMtConfidence(attr), 0.0);
		assertEquals("mt-confidence|file:///tools.xml#T1", trav.getAnnotatorsRef());
		assertEquals("file:///tools.xml#T1", trav.getAnnotatorRef("mt-confidence"));
	}
	
	@Test
	public void testTextAnalysisPointerHtml () throws SAXException, IOException, ParserConfigurationException {
		InputSource is = new InputSource(new StringReader("<!DOCTYPE html><html lang=en><head><meta charset=utf-8>"
			+ "<title>Title</title>"
			+ "<script type='application/its+xml'>"
			+ "<its:rules xmlns:its='http://www.w3.org/2005/11/its' version='2.0'>"
			+ "<its:textAnalysisRule selector='//*[@typeof and @about]' "
			+ " taClassRefPointer='@typeof' taIdentRefPointer='@about'/>"
			+ "</its:rules>"
			+ "</script>"
			+ "</head><body>"
			+ "<p><span property='http://xmlns.com/foaf/0.1/name' about='http://dbpedia.org/resource/Dublin' "
			+ "typeof='http:/nerd.eurecom.fr/ontology#Place'>Dublin</span> is the capital of Ireland.</p>"
			+ "</body>/html>"));
		Document doc = htmlDocBuilder.parse(is);
		ITSEngine trav = applyITSRules(doc, null, true, null);
		Element elem = getElement(trav, "span", 1);
		assertNotNull(elem);
		GenericAnnotations anns = trav.getTextAnalysisAnnotation(null);
		assertNotNull(anns);
		GenericAnnotation ann = anns.getAnnotations(GenericAnnotationType.TA).get(0);
		assertEquals(GenericAnnotationType.REF_PREFIX+"http:/nerd.eurecom.fr/ontology#Place", ann.getString(GenericAnnotationType.TA_CLASS));
		assertEquals(GenericAnnotationType.REF_PREFIX+"http://dbpedia.org/resource/Dublin", ann.getString(GenericAnnotationType.TA_IDENT));
	}

	@Test
	public void testTextAnalysisSimpleHtml () throws SAXException, IOException, ParserConfigurationException {
		InputSource is = new InputSource(new StringReader("<p>hello "
			+ "<span its-ta-ident-ref=\"http://purl.org/vocabularies/princeton/wn30/synset-sweet-adjective-1.rdf\" "
			+ ">sweet</span> "
			+ "<span its-ta-ident-ref=\"http://dbpedia.org/resource/Paris\" "
			+ "its-ta-class-ref=\"http://schema.org/Place\">Paris</span> summer</p>"));
		Document doc = htmlDocBuilder.parse(is);
		ITSEngine trav = applyITSRules(doc, null, true, null);
		Element elem = getElement(trav, "span", 1);
		assertEquals("sweet", elem.getTextContent());
		assertEquals(GenericAnnotationType.REF_PREFIX+"http://purl.org/vocabularies/princeton/wn30/synset-sweet-adjective-1.rdf", trav.getTextAnalysisIdent(null));
		elem = getElement(trav, "span", 2);
		assertEquals("Paris", elem.getTextContent());
		assertEquals(GenericAnnotationType.REF_PREFIX+"http://dbpedia.org/resource/Paris", trav.getTextAnalysisIdent(null));
		assertEquals(GenericAnnotationType.REF_PREFIX+"http://schema.org/Place", trav.getTextAnalysisClass(null));
	}

	@Test
	public void testTextAnalysisOnAttribute () throws SAXException, IOException, ParserConfigurationException {
		InputSource is = new InputSource(new StringReader("<doc>"
			+ "<i:rules xmlns:i='"+Namespaces.ITS_NS_URI+"' version='2.0'>"
			+ "<i:textAnalysisRule selector='//entry/@text' taSourcePointer='../@attSource' "
			+ " taIdentPointer='../@attIdent' />"
			+ "</i:rules>"
			+ "<entry text='Some text' attIdent='ident1' attSource='src1'>Content</entry></doc>"));
		Document doc = fact.newDocumentBuilder().parse(is);
		ITraversal trav = applyITSRules(doc, null, false, null);
		Element elem = getElement(trav, "entry", 1);
		Attr attr = elem.getAttributeNode("text");
		assertNotNull(attr);
		assertEquals("src1", trav.getTextAnalysisSource(attr));
		assertEquals("ident1", trav.getTextAnalysisIdent(attr));
	}
	
	@Test
	public void testLocQualityRatingHtml () throws SAXException, IOException, ParserConfigurationException {
		InputSource is = new InputSource(new StringReader("<!DOCTYPE html><html lang=en><head><meta charset=utf-8>"
			+ "<title>Title</title></head><body>"
			+ "<p><span its-loc-quality-rating-score='5.4321' its-loc-quality-rating-score-threshold='5.0'>text1 "
			+ "<span its-loc-quality-rating-vote='-12' its-loc-quality-rating-vote-threshold='0'"
			+ "its-loc-quality-rating-profile-ref='uri1'>text2</span></span>"));
		Document doc = htmlDocBuilder.parse(is);
		ITSEngine trav = applyITSRules(doc, null, true, null);
		Element elem = getElement(trav, "span", 1);
		assertNotNull(elem);
		assertEquals(5.4321, trav.getLocQualityRatingScore(null), 0.0);
		assertEquals(5.0, trav.getLocQualityRatingScoreThreshold(null), 0.0);
		assertEquals(null, trav.getLocQualityRatingVote(null));
		assertEquals(null, trav.getLocQualityRatingVoteThreshold(null));
		assertEquals(null, trav.getLocQualityRatingProfileRef(null));
		elem = getElement(trav, "span", 2);
		assertNotNull(elem);
		assertEquals(null, trav.getLocQualityRatingScore(null));
		assertEquals(null, trav.getLocQualityRatingScoreThreshold(null));
		assertEquals(-12, (int)trav.getLocQualityRatingVote(null));
		assertEquals(0, (int)trav.getLocQualityRatingVoteThreshold(null));
		assertEquals("uri1", trav.getLocQualityRatingProfileRef(null));
	}

	@Test
	public void testLocQualityRatingXml () throws SAXException, IOException, ParserConfigurationException {
		InputSource is = new InputSource(new StringReader("<doc xmlns:i='"+Namespaces.ITS_NS_URI+"' i:version='2.0'>"
			+ "<p><mrk i:locQualityRatingVote='7' i:locQualityRatingVoteThreshold='95' i:locQualityRatingProfileRef='u1'>text1 "
			+ "<i:span locQualityRatingScore='88.22' locQualityRatingScoreThreshold='100.0' locQualityRatingProfileRef='u2'>text2</i:span></mrk>"
			+ "</p></doc>"));
		Document doc = fact.newDocumentBuilder().parse(is);
		ITraversal trav = applyITSRules(doc, null, false, null);
		Element elem = getElement(trav, "mrk", 1);
		assertNotNull(elem);
		assertEquals(null, trav.getLocQualityRatingScore(null));
		assertEquals(null, trav.getLocQualityRatingScoreThreshold(null));
		assertEquals(7, (int)trav.getLocQualityRatingVote(null));
		assertEquals(95, (int)trav.getLocQualityRatingVoteThreshold(null));
		assertEquals("u1", trav.getLocQualityRatingProfileRef(null));
		elem = getElement(trav, "i:span", 1);
		assertNotNull(elem);
		assertEquals(88.22, trav.getLocQualityRatingScore(null), 0.0);
		assertEquals(100.0, trav.getLocQualityRatingScoreThreshold(null), 0.0);
		assertEquals(null, trav.getLocQualityRatingVote(null));
		assertEquals(null, trav.getLocQualityRatingVoteThreshold(null));
		assertEquals("u2", trav.getLocQualityRatingProfileRef(null));
	}

	@Test
	public void testTargetPointerGlobal () throws SAXException, IOException, ParserConfigurationException {
		InputSource is = new InputSource(new StringReader("<doc>"
			+ "<i:rules xmlns:i='"+Namespaces.ITS_NS_URI+"' version='2.0'>"
			+ "<i:targetPointerRule selector='//entry/src' targetPointer='../trg'/>"
			+ "</i:rules>"
			+ "<entry><src>source</src><trg></trg></entry></doc>"));
		Document doc = fact.newDocumentBuilder().parse(is);
		ITraversal trav = applyITSRules(doc, null, false, null);
		Element elem = getElement(trav, "entry", 1);
		assertNotNull(elem);
		assertTrue(trav.getTranslate(null));
		assertEquals(null, trav.getTargetPointer(null));
		elem = getElement(trav, "src", 1);
		assertNotNull(elem);
		assertTrue(trav.getTranslate(null));
		assertEquals("../trg", trav.getTargetPointer(null));
	}

	@Test
	public void testTargetPointerAttributes () throws SAXException, IOException, ParserConfigurationException {
		InputSource is = new InputSource(new StringReader("<doc>"
			+ "<i:rules xmlns:i='"+Namespaces.ITS_NS_URI+"' version='2.0'>"
			+ "<i:targetPointerRule selector='//entry/@src' targetPointer='../@trg'/>"
			+ "</i:rules>"
			+ "<entry src='source' trg='' /></doc>"));
		Document doc = fact.newDocumentBuilder().parse(is);
		ITraversal trav = applyITSRules(doc, null, false, null);
		Element elem = getElement(trav, "entry", 1);
		assertNotNull(elem);
		Attr attr = elem.getAttributeNode("src");
		assertEquals("../@trg", trav.getTargetPointer(attr));
	}

	@Test
	public void testLocaleFilter () throws SAXException, IOException, ParserConfigurationException {
		InputSource is = new InputSource(new StringReader("<book xmlns:its=\"http://www.w3.org/2005/11/its\">"
			+ "<info>"
			+ "<its:rules version=\"2.0\">"
			+ "<its:localeFilterRule selector=\"//legalnotice[@role='Canada']\" localeFilterList=\"en-CA, fr-CA\"/>"
			+ "</its:rules>"
			+ "<legalnotice role=\"Canada\">"
			+ "<para>This legal notice is only for Canadian locales.</para>"
			+ "<para its:localeFilterList='*'>This text is for all locales.</para>"
			+ "</legalnotice>"
			+ "</info></book>"));
		Document doc = fact.newDocumentBuilder().parse(is);
		ITraversal trav = applyITSRules(doc, null, false, null);
		Element elem = getElement(trav, "para", 1);
		assertNotNull(elem);
		assertEquals("en-CA, fr-CA", trav.getLocaleFilter());
		elem = getElement(trav, "para", 2);
		assertNotNull(elem);
		assertEquals("*", trav.getLocaleFilter());
	}

	@Test
	public void testPreserveSpaces () throws SAXException, IOException, ParserConfigurationException {
		InputSource is = new InputSource(new StringReader("<book xmlns:its=\"http://www.w3.org/2005/11/its\">"
			+ "<info>"
			+ "<its:rules version=\"2.0\">"
			+ "<its:preserveSpaceRule selector=\"//pre\" space=\"preserve\"/>"
			+ "</its:rules>"
			+ "<p> a  b  c  </p>"
			+ "<pre> a  b  c  </pre>"
			+ "</info></book>"));
		Document doc = fact.newDocumentBuilder().parse(is);
		ITraversal trav = applyITSRules(doc, null, false, null);
		Element elem = getElement(trav, "p", 1);
		assertNotNull(elem);
		assertFalse(trav.preserveWS(null));
		elem = getElement(trav, "pre", 1);
		assertTrue(trav.preserveWS(null));
	}

	@Test
	public void testAllowedCharsGlobal () throws SAXException, IOException, ParserConfigurationException {
		InputSource is = new InputSource(new StringReader("<doc>"
			+ "<i:rules xmlns:i='"+Namespaces.ITS_NS_URI+"' version='2.0'>"
			+ "<i:allowedCharactersRule selector='//p' allowedCharacters='[a-zA-Z]'/>"
			+ "<i:allowedCharactersRule selector='//p/@abc' allowedCharacters='[^*+]'/>"
			+ "</i:rules>"
			+ "<p abc='123'>text</p></doc>"));
		Document doc = fact.newDocumentBuilder().parse(is);
		ITraversal trav = applyITSRules(doc, null, false, null);
		Element elem = getElement(trav, "p", 1);
		assertEquals("[a-zA-Z]", trav.getAllowedCharacters(null));
		Attr attr = elem.getAttributeNode("abc");
		assertEquals("[^*+]", trav.getAllowedCharacters(attr));
	}

	@Test
	public void testAllowedCharsLocal () throws SAXException, IOException, ParserConfigurationException {
		InputSource is = new InputSource(new StringReader("<doc xmlns:i=\"http://www.w3.org/2005/11/its\" version='2.0'>"
			+ "<p i:allowedCharacters='[abc]'>ab</p></doc>"));
		Document doc = fact.newDocumentBuilder().parse(is);
		ITraversal trav = applyITSRules(doc, null, false, null);
		getElement(trav, "p", 1);
		assertEquals("[abc]", trav.getAllowedCharacters(null));
	}

	@Test
	public void testStorageSizeGlobal1 () throws SAXException, IOException, ParserConfigurationException {
		InputSource is = new InputSource(new StringReader("<doc>"
			+ "<i:rules xmlns:i='"+Namespaces.ITS_NS_URI+"' version='2.0'>"
			+ "<i:storageSizeRule selector='//p' storageSize='255' lineBreakType='crlf'/>"
			+ "</i:rules>"
			+ "<p>Text</p><q>text</q></doc>"));
		Document doc = fact.newDocumentBuilder().parse(is);
		ITraversal trav = applyITSRules(doc, null, false, null);
		getElement(trav, "p", 1);
		assertEquals(255, (int)trav.getStorageSize(null));
		assertEquals("UTF-8", trav.getStorageEncoding(null));
		assertEquals("crlf", trav.getLineBreakType(null));
		getElement(trav, "q", 1); // Not inherited
		assertEquals(null, trav.getStorageSize(null));
		assertEquals("UTF-8", trav.getStorageEncoding(null));
		assertEquals("lf", trav.getLineBreakType(null));
	}

	@Test
	public void testStorageSizeGlobal2 () throws SAXException, IOException, ParserConfigurationException {
		InputSource is = new InputSource(new StringReader("<doc>"
			+ "<i:rules xmlns:i='"+Namespaces.ITS_NS_URI+"' version='2.0'>"
			+ "<i:storageSizeRule selector='//p' lineBreakType='cr' storageSizePointer='@max' storageEncodingPointer='@enc'/>"
			+ "</i:rules>"
			+ "<p max='222' enc='UTF-16'>Text<b>text</b></p></doc>"));
		Document doc = fact.newDocumentBuilder().parse(is);
		ITraversal trav = applyITSRules(doc, null, false, null);
		getElement(trav, "p", 1);
		assertEquals(222, (int)trav.getStorageSize(null));
		assertEquals("UTF-16", trav.getStorageEncoding(null));
		assertEquals("cr", trav.getLineBreakType(null));
		getElement(trav, "b", 1);
		assertEquals(null, trav.getStorageSize(null));
		assertEquals("UTF-8", trav.getStorageEncoding(null));
		assertEquals("lf", trav.getLineBreakType(null));
	}
	
	@Test
	public void testStorageSizeLocal1 () throws SAXException, IOException, ParserConfigurationException {
		InputSource is = new InputSource(new StringReader("<doc xmlns:i=\"http://www.w3.org/2005/11/its\" version='2.0'>"
			+ "<p i:storageEncoding='Shift-JIS' i:storageSize='111'>text</p></doc>"));
		Document doc = fact.newDocumentBuilder().parse(is);
		ITraversal trav = applyITSRules(doc, null, false, null);
		getElement(trav, "p", 1);
		assertEquals(111, (int)trav.getStorageSize(null));
		assertEquals("Shift-JIS", trav.getStorageEncoding(null));
		assertEquals("lf", trav.getLineBreakType(null));
	}

	@Test
	public void testLQIssueGlobal1 () throws SAXException, IOException, ParserConfigurationException {
		InputSource is = new InputSource(new StringReader("<doc>"
			+ "<i:rules xmlns:i='"+Namespaces.ITS_NS_URI+"' version='2.0'>"
			+ "<i:locQualityIssueRule selector='//z' locQualityIssueTypePointer='@type' locQualityIssueCommentPointer='@comment'"
			+ " locQualityIssueSeverityPointer='@score' locQualityIssueProfileRefPointer='@pref'/>"
			+ "</i:rules>"
			+ "<p>Text with <z type='other' comment='comment' pref='uri' score='1'>error</z></p></doc>"));
		Document doc = fact.newDocumentBuilder().parse(is);
		ITraversal trav = applyITSRules(doc, null, false, null);
		getElement(trav, "z", 1);
		assertEquals("other", trav.getLocQualityIssueType(null, 0));
		assertEquals("comment", trav.getLocQualityIssueComment(null, 0));
		assertEquals(1.0, trav.getLocQualityIssueSeverity(null, 0), 0);
		assertEquals("uri", trav.getLocQualityIssueProfileRef(null, 0));
		assertEquals(true, trav.getLocQualityIssueEnabled(null, 0));
	}

	@Test
	public void testLQIssueGlobal2 () throws SAXException, IOException, ParserConfigurationException {
		InputSource is = new InputSource(new StringReader("<doc xmlns:i='"+Namespaces.ITS_NS_URI+"' i:version='2.0'>"
			+ "<i:rules version='2.0'>"
			+ "<i:locQualityIssueRule selector='//z' locQualityIssuesRefPointer='@ref' />"
			+ "</i:rules>"
			+ "<p>Text with <z ref='#id1'>error</z></p>"
			+ "<i:locQualityIssues xml:id='id1'>"
			+ "<i:locQualityIssue locQualityIssueEnabled='no' locQualityIssueComment='comment1'/>"
			+ "<i:locQualityIssue locQualityIssueProfileRef='pref2' locQualityIssueSeverity='50' locQualityIssueComment='comment2'/>"
			+ "</i:locQualityIssues>"
			+ "</doc>"));
		Document doc = fact.newDocumentBuilder().parse(is);
		ITraversal trav = applyITSRules(doc, null, false, null);
		getElement(trav, "z", 1);
		assertEquals("#id1", trav.getLocQualityIssuesRef(null));
		assertEquals(2, trav.getLocQualityIssueCount(null));
		// Values
		assertEquals(null, trav.getLocQualityIssueType(null, 0));
		assertEquals(null, trav.getLocQualityIssueType(null, 1));
		assertEquals("comment1", trav.getLocQualityIssueComment(null, 0));
		assertEquals("comment2", trav.getLocQualityIssueComment(null, 1));
		assertEquals(null, trav.getLocQualityIssueSeverity(null, 0));
		assertEquals(50.0, trav.getLocQualityIssueSeverity(null, 1), 0);
		assertEquals(null, trav.getLocQualityIssueProfileRef(null, 0));
		assertEquals("pref2", trav.getLocQualityIssueProfileRef(null, 1));
		assertEquals(false, trav.getLocQualityIssueEnabled(null, 0));
		assertEquals(true, trav.getLocQualityIssueEnabled(null, 1));
	}

	@Test
	public void testLQIssueGlobalAndLocal1 () throws SAXException, IOException, ParserConfigurationException {
		InputSource is = new InputSource(new StringReader("<doc xmlns:i='"+Namespaces.ITS_NS_URI+"' i:version='2.0'>"
			+ "<i:rules xmlns:i='"+Namespaces.ITS_NS_URI+"' version='2.0'>"
			+ "<i:locQualityIssueRule selector='//z' locQualityIssueTypePointer='@type' locQualityIssueCommentPointer='@comment'"
			+ " locQualityIssueSeverityPointer='@score' locQualityIssueProfileRefPointer='@pref'/>"
			+ "</i:rules>"
			+ "<p>Text with <z type='other' comment='comment' pref='uri' score='1'"
			+ " i:locQualityIssueType='terminology' i:locQualityIssueProfileRef='thisUri'>error</z></p></doc>"));
		Document doc = fact.newDocumentBuilder().parse(is);
		ITraversal trav = applyITSRules(doc, null, false, null);
		getElement(trav, "z", 1);
		// Local markup overrides everything, even undefined data (complete overriding rule in ITS)
		assertEquals(null, trav.getLocQualityIssuesRef(null));
		assertEquals(null, trav.getLocQualityIssueComment(null, 0));
		assertEquals(null, trav.getLocQualityIssueSeverity(null, 0));
		assertEquals("terminology", trav.getLocQualityIssueType(null, 0));
		assertEquals("thisUri", trav.getLocQualityIssueProfileRef(null, 0));
		assertEquals(true, trav.getLocQualityIssueEnabled(null, 0));
	}

	@Test
	public void testLQIssueOnAttributes () throws SAXException, IOException, ParserConfigurationException {
		InputSource is = new InputSource(new StringReader("<doc xmlns:i='"+Namespaces.ITS_NS_URI+"' i:version='2.0'>"
			+ "<i:rules xmlns:i='"+Namespaces.ITS_NS_URI+"' version='2.0'>"
			+ "<i:locQualityIssueRule selector='//p/@abc' locQualityIssuesRef='#id1'/>"
			+ "</i:rules>"
			+ "<p abc='some text'>Text</p>"
			+ "<i:locQualityIssues xml:id='id1'>"
			+ "<i:locQualityIssue locQualityIssueEnabled='no' locQualityIssueComment='comment1'/>"
			+ "<i:locQualityIssue locQualityIssueProfileRef='pref2' locQualityIssueSeverity='50' locQualityIssueComment='comment2'/>"
			+ "</i:locQualityIssues>"
			+ "</doc>"));
		Document doc = fact.newDocumentBuilder().parse(is);
		ITraversal trav = applyITSRules(doc, null, false, null);
		Element elem = getElement(trav, "p", 1);
		Attr attr = elem.getAttributeNode("abc");
		assertEquals("#id1", trav.getLocQualityIssuesRef(attr));
		assertEquals(2, trav.getLocQualityIssueCount(attr));
		assertEquals(null, trav.getLocQualityIssueType(attr, 0));
		assertEquals(null, trav.getLocQualityIssueType(attr, 1));
		assertEquals("comment1", trav.getLocQualityIssueComment(attr, 0));
		assertEquals("comment2", trav.getLocQualityIssueComment(attr, 1));
		assertEquals(null, trav.getLocQualityIssueSeverity(attr, 0));
		assertEquals(50.0, trav.getLocQualityIssueSeverity(attr, 1), 0);
		assertEquals(null, trav.getLocQualityIssueProfileRef(attr, 0));
		assertEquals("pref2", trav.getLocQualityIssueProfileRef(attr, 1));
		assertEquals(false, trav.getLocQualityIssueEnabled(attr, 0));
		assertEquals(true, trav.getLocQualityIssueEnabled(attr, 1));
	}

	@Test
	public void testProvenanceLocal () throws SAXException, IOException, ParserConfigurationException {
		InputSource is = new InputSource(new StringReader("<doc xmlns:i='"+Namespaces.ITS_NS_URI+"' i:version='2.0'>"
			+ "<p attr='data' i:person='p1' i:orgRef='oRef1' i:tool='t1' i:revPersonRef='rpRef1' i:revOrg='ro1' i:revToolRef='rtRef1' i:provRef='provref1'>Text</p>"
			+ "</doc>"));
		Document doc = fact.newDocumentBuilder().parse(is);
		ITraversal trav = applyITSRules(doc, null, false, null);
		Element elem = getElement(trav, "p", 1);
		assertEquals(null, trav.getProvRecordsRef(null));
		assertEquals("p1", trav.getProvPerson(null, 0));
		assertEquals("REF:oRef1", trav.getProvOrg(null, 0));
		assertEquals("t1", trav.getProvTool(null, 0));
		assertEquals("REF:rpRef1", trav.getProvRevPerson(null, 0));
		assertEquals("ro1", trav.getProvRevOrg(null, 0));
		assertEquals("REF:rtRef1", trav.getProvRevTool(null, 0));
		assertEquals("provref1", trav.getProvRef(null, 0));
		// Attribute:
		Attr attr = elem.getAttributeNode("attr");
		assertEquals("p1", trav.getProvPerson(attr, 0));
	}

	@Test
	public void testAnnotatorsRef () throws SAXException, IOException, ParserConfigurationException {
		InputSource is = new InputSource(new StringReader("<doc xmlns:i='"+Namespaces.ITS_NS_URI+"' i:version='2.0'>"
			+ "<group i:annotatorsRef='terminology|uri2 mt-confidence|uri1'>"
			+ "<p i:annotatorsRef='text-analysis|uriDisamb'>Text with <z i:annotatorsRef='terminology|uri3'"
			+ " i:term='yes'>a term</z></p></group></doc>"));
		Document doc = fact.newDocumentBuilder().parse(is);
		ITraversal trav = applyITSRules(doc, null, false, null);
		getElement(trav, "group", 1);
		assertEquals("mt-confidence|uri1 terminology|uri2", trav.getAnnotatorsRef());
		getElement(trav, "p", 1);
		assertEquals("mt-confidence|uri1 terminology|uri2 text-analysis|uriDisamb", trav.getAnnotatorsRef());
		getElement(trav, "z", 1);
		assertEquals("mt-confidence|uri1 terminology|uri3 text-analysis|uriDisamb", trav.getAnnotatorsRef());
	}
	
	@Test
	public void testAnnotatorsRefBadValue () throws SAXException, IOException, ParserConfigurationException {
		// Should pass without exception, but generate an error in the log.
		InputSource is = new InputSource(new StringReader("<doc xmlns:i='"+Namespaces.ITS_NS_URI+"' i:version='2.0'>"
			+ "<group i:annotatorsRef='Invalid-value-for-test|uri1'>"
			+ "<p>Text with</p></group></doc>"));
		Document doc = fact.newDocumentBuilder().parse(is);
		ITraversal trav = applyITSRules(doc, null, false, null);
		getElement(trav, "group", 1);
		assertEquals("invalid-value-for-test|uri1", trav.getAnnotatorsRef());
	}
	
	@Test
	public void testQueryLanguage () throws SAXException, IOException, ParserConfigurationException {
		InputSource is = new InputSource(new StringReader("<doc>"
			+ "<i:rules xmlns:i='"+Namespaces.ITS_NS_URI+"' queryLanguage='xpath2' version='2.0'>"
			+ "<i:translateRule selector='//par/@title' translate='yes' />"
			+ "<i:translateRule selector='//par/@alt' translate='yes' />"
			+ "</i:rules>"
			+ "<par title='title text' test='test' alt='alt text'>Text</par></doc>"));
		// Passes but should generate a warning in the log
		Document doc = fact.newDocumentBuilder().parse(is);
		ITraversal trav = applyITSRules(doc, null, false, null);
		Element elem = getElement(trav, "par", 1);
		assertTrue(trav.getTranslate(elem.getAttributeNode("title")));
	}
	
	@Test
	public void testlangVsXmlLangInXHtml () throws SAXException, IOException, ParserConfigurationException {
		InputSource is = new InputSource(new StringReader("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\""
			+ " http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">"
			+ "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\">"
			+ "<head><title>title</title>"
			+ "</head>"
			+ "<body>"
			+ "<p lang='es' xml:lang='pt'>t1</p>"
			+ "</body></html>"));
		Document doc = htmlDocBuilder.parse(is);
		ITraversal trav = applyITSRules(doc, null, true, null);
		Element elem = getElement(trav, "html", 1);
		assertNotNull(elem);
		assertEquals("en", trav.getLanguage());
		elem = getElement(trav, "p", 1);
		assertNotNull(elem);
		assertEquals("pt", trav.getLanguage());
	}

	

	@Test (expected=ITSException.class)
	public void testBadQueryLanguage () throws SAXException, IOException, ParserConfigurationException {
		InputSource is = new InputSource(new StringReader("<doc>"
			+ "<i:rules xmlns:i='"+Namespaces.ITS_NS_URI+"' queryLanguage='invalid-value' version='2.0'>"
			+ "<i:translateRule selector='//par/@title' translate='yes' />"
			+ "<i:translateRule selector='//par/@alt' translate='yes' />"
			+ "</i:rules>"
			+ "<par title='title text' test='test' alt='alt text'>Text</par></doc>"));
		Document doc = fact.newDocumentBuilder().parse(is);
		// Just trigger the error
		applyITSRules(doc, null, false, null);
	}
	
	@Test
	public void testIdValueOnAttribute () throws SAXException, IOException, ParserConfigurationException {
		InputSource is = new InputSource(new StringReader("<doc xmlns:i='"+Namespaces.ITS_NS_URI+"' i:version='2.0'>"
			+ "<i:rules xmlns:i='"+Namespaces.ITS_NS_URI+"' version='2.0'>"
			+ "<i:translateRule selector='//elem/@myText' translate='yes' />"
			+ "<i:idValueRule selector='//elem/@myText' idValue='../@myId' />"
			+ "</i:rules>"
			+ "<elem myText='text' myId='id1'/>"
			+ "</doc>"));
		Document doc = fact.newDocumentBuilder().parse(is);
		ITraversal trav = applyITSRules(doc, null, false, null);
		Element elem = getElement(trav, "elem", 1);
		assertTrue(trav.getTranslate(elem.getAttributeNode("myText")));
		assertEquals("id1", trav.getIdValue(elem.getAttributeNode("myText")));
	}

	@Test
	public void testTermOnAttribute () throws SAXException, IOException, ParserConfigurationException {
		InputSource is = new InputSource(new StringReader("<doc xmlns:i='"+Namespaces.ITS_NS_URI+"' i:version='2.0'>"
			+ "<i:rules xmlns:i='"+Namespaces.ITS_NS_URI+"' version='2.0'>"
			+ "<i:translateRule selector='//elem/@myText' translate='yes' />"
			+ "<i:termRule selector='//elem/@myText' term='yes' termInfoPointer='../@myInfo' />"
			+ "</i:rules>"
			+ "<elem myText='text' myInfo='some info'/>"
			+ "</doc>"));
		Document doc = fact.newDocumentBuilder().parse(is);
		ITraversal trav = applyITSRules(doc, null, false, null);
		Element elem = getElement(trav, "elem", 1);
		assertTrue(trav.getTerm(elem.getAttributeNode("myText")));
		assertEquals("some info", trav.getTermInfo(elem.getAttributeNode("myText")));
	}
	
	@Test
	public void testLQIssueLocal1 () throws SAXException, IOException, ParserConfigurationException {
		InputSource is = new InputSource(new StringReader("<doc xmlns:i='"+Namespaces.ITS_NS_URI+"' i:version='2.0'>"
			+ "<p>Text with <z i:locQualityIssueType='other' i:locQualityIssueComment='comment'"
			+ " i:locQualityIssueProfileRef='uri' i:locQualityIssueSeverity='1.0'>error</z></p></doc>"));
		Document doc = fact.newDocumentBuilder().parse(is);
		ITraversal trav = applyITSRules(doc, null, false, null);
		getElement(trav, "z", 1);
		assertEquals("other", trav.getLocQualityIssueType(null, 0));
		assertEquals("comment", trav.getLocQualityIssueComment(null, 0));
		assertEquals(1.0, trav.getLocQualityIssueSeverity(null, 0), 0);
		assertEquals("uri", trav.getLocQualityIssueProfileRef(null, 0));
	}
	
	@Test
	public void testLQIssueLocalWithSpan () throws SAXException, IOException, ParserConfigurationException {
		InputSource is = new InputSource(new StringReader("<doc xmlns:i='"+Namespaces.ITS_NS_URI+"' i:version='2.0'>"
			+ "<p>Text with <i:span locQualityIssueType='other' locQualityIssueComment='comment'"
			+ " locQualityIssueProfileRef='uri' locQualityIssueSeverity='1'>error</i:span></p></doc>"));
		Document doc = fact.newDocumentBuilder().parse(is);
		ITraversal trav = applyITSRules(doc, null, false, null);
		getElement(trav, "i:span", 1);
		assertEquals("other", trav.getLocQualityIssueType(null, 0));
		assertEquals("comment", trav.getLocQualityIssueComment(null, 0));
		assertEquals(1.0, trav.getLocQualityIssueSeverity(null, 0), 0);
		assertEquals("uri", trav.getLocQualityIssueProfileRef(null, 0));
	}
	
	@Test
	public void testExternalResourceRefGlobal () throws SAXException, IOException, ParserConfigurationException {
		InputSource is = new InputSource(new StringReader("<doc>"
			+ "<i:rules xmlns:i='"+Namespaces.ITS_NS_URI+"' version='2.0'>"
			+ "<i:externalResourceRefRule selector='//video/@src' externalResourceRefPointer='.' />"
			+ "<i:externalResourceRefRule selector='//video/@poster' externalResourceRefPointer='.' />"
			+ "</i:rules>"
			+ "<p>Text with <video src=\"http://www.example.com/v2.mp\" poster=\"video-image.png\" /></p></doc>"));
		Document doc = fact.newDocumentBuilder().parse(is);
		ITraversal trav = applyITSRules(doc, null, false, null);
		Element elem = getElement(trav, "video", 1);
		assertEquals("http://www.example.com/v2.mp", trav.getExternalResourceRef(elem.getAttributeNode("src")));
		assertEquals("video-image.png", trav.getExternalResourceRef(elem.getAttributeNode("poster")));
	}

	@Test
	public void testTranslateGlobal () throws SAXException, IOException, ParserConfigurationException {
		InputSource is = new InputSource(new StringReader("<doc>"
			+ "<i:rules xmlns:i='"+Namespaces.ITS_NS_URI+"' version='2.0'>"
			+ "<i:translateRule selector='//par/@title' translate='yes' />"
			+ "<i:translateRule selector='//par/@alt' translate='yes' />"
			+ "</i:rules>"
			+ "<par title='title text' test='test' alt='alt text'>Text</par></doc>"));
		Document doc = fact.newDocumentBuilder().parse(is);
		ITraversal trav = applyITSRules(doc, null, false, null);
		Element elem = getElement(trav, "par", 1);
		assertTrue(trav.getTranslate(elem.getAttributeNode("title")));
		assertTrue(trav.getTranslate(elem.getAttributeNode("alt")));
		assertFalse(trav.getTranslate(elem.getAttributeNode("test")));
	}

	@Test
	public void testLocalLanguageInfo () throws SAXException, IOException, ParserConfigurationException {
		InputSource is = new InputSource(new StringReader("<!DOCTYPE html><html lang=en><head>"
			+ "<meta charset=utf-8><title>Title</title></head>"
			+ "<body><p>t1<span lang=\"fr\">tf</span></p></body>/html>"));
		Document doc = htmlDocBuilder.parse(is);
		ITraversal trav = applyITSRules(doc, null, true, null);
		getElement(trav, "head", 1);
		assertEquals("en", trav.getLanguage());
		getElement(trav, "span", 1);
		assertEquals("fr", trav.getLanguage());
	}
	
	@Test
	public void testGlobalAndLocalLanguageInfo () throws SAXException, IOException, ParserConfigurationException {
		InputSource is = new InputSource(new StringReader("<!DOCTYPE html><html lang=en><head>"
			+ "<meta charset=utf-8><title>Title</title>"
			+ "<script type='application/its+xml'>"
			+ "<its:rules xmlns:its='http://www.w3.org/2005/11/its' version='2.0' xmlns:h='http://www.w3.org/1999/xhtml'>"
			+ " <its:langRule selector='/h:*' langPointer='//h:html/@lang'/>"        
//			+ " <its:langRule selector='//h:*' langPointer='@lang'/>"        
			+ "</its:rules>"
			+ "</script>"
			+ "</head>"
			+ "<body><p>t1<span lang=\"fr\">tf</span></p></body>/html>"));
		Document doc = htmlDocBuilder.parse(is);
		ITraversal trav = applyITSRules(doc, null, true, null);
		getElement(trav, "head", 1);
		assertEquals("en", trav.getLanguage());
		getElement(trav, "span", 1);
		assertEquals("fr", trav.getLanguage());
	}

	private static Element getElement (ITraversal trav,
		String name,
		int number)
	{
		trav.startTraversal();
		Node node;
		int count = 0;
		while ( (node = trav.nextNode()) != null ) {
			switch ( node.getNodeType() ) {
			case Node.ELEMENT_NODE:
				if ( !trav.backTracking() ) {
					if ( node.getNodeName().equals(name) ) {
						if ( ++count == number ) return (Element)node;
					}
				}
				break;
			}
		}
		return null;
	}
	
	private static ITSEngine applyITSRules (Document doc,
		URI docURI,
		boolean isHTML5,
		File rulesFile)
	{
		// Create the ITS engine
		ITSEngine itsEng = new ITSEngine(doc, docURI, isHTML5, null);
		// Add any external rules file(s)
		if ( rulesFile != null ) {
			itsEng.addExternalRules(rulesFile.toURI());
		}
		// Apply the all rules (external and internal) to the document
		itsEng.applyRules(ITSEngine.DC_ALL);
		return itsEng;
	}

}
