package net.sf.okapi.steps.xmlanalysis;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.steps.xmlanalysis.XMLAnalyzer.Info;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class XMLAnalyzerTest {
	
	private XMLAnalyzer xan;
	
	public XMLAnalyzerTest () {
		xan = new XMLAnalyzer();
	}

	@Test
	public void testSimpleWSRoot () {
		xan.reset();
		RawDocument rd = new RawDocument("<doc>  </doc>", LocaleId.ENGLISH);
		xan.analyzeDocument(rd);
		HashMap<String, Info> res = xan.getResults();
		assertTrue(res.get("doc").isRoot);
		assertFalse(res.get("doc").withinText);
		assertFalse(res.get("doc").hasText);
	}
	
	@Test
	public void testSimpleRootWithText () {
		xan.reset();
		RawDocument rd = new RawDocument("<doc>text</doc>", LocaleId.ENGLISH);
		xan.analyzeDocument(rd);
		HashMap<String, Info> res = xan.getResults();
		assertTrue(res.get("doc").isRoot);
		assertFalse(res.get("doc").withinText);
		assertTrue(res.get("doc").hasText);
	}
	
	@Test
	public void testSimpleRootWithOneElement () {
		xan.reset();
		RawDocument rd = new RawDocument("<doc><p>text</p></doc>", LocaleId.ENGLISH);
		xan.analyzeDocument(rd);
		HashMap<String, Info> res = xan.getResults();
		assertTrue(res.get("doc").isRoot);
		assertFalse(res.get("doc").withinText);
		assertFalse(res.get("doc").hasText);
		assertFalse(res.get("p").withinText);
		assertTrue(res.get("p").hasText);
	}
	
	@Test
	public void testSimpleElementWithin () {
		xan.reset();
		RawDocument rd = new RawDocument("<doc><p>text<b>text</b></p></doc>", LocaleId.ENGLISH);
		xan.analyzeDocument(rd);
		HashMap<String, Info> res = xan.getResults();
		assertTrue(res.get("doc").isRoot);
		assertFalse(res.get("doc").withinText);
		assertFalse(res.get("doc").hasText);
		assertFalse(res.get("p").withinText);
		assertTrue(res.get("p").hasText);
		assertTrue(res.get("b").withinText);
		assertTrue(res.get("b").hasText);
	}
	
}
