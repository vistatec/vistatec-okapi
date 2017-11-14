package net.sf.okapi.filters.openxml;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class TestPowerpointDocument {

	@Test
	public void testSlides() throws Exception {
		PowerpointDocument doc = getPowerpointDocument("/slideLayouts.pptx", new ConditionalParameters());
		doc.initialize();
		List<String> expected = new ArrayList<>();
		expected.add("ppt/slides/slide1.xml");
		expected.add("ppt/slides/slide2.xml");
		assertEquals(expected, doc.findSlides());
	}

	@Test
	public void testSlideLayouts() throws Exception {
		PowerpointDocument doc = getPowerpointDocument("/slideLayouts.pptx", new ConditionalParameters());
		doc.initialize();
		List<String> expected = new ArrayList<>();
		expected.add("ppt/slideLayouts/slideLayout1.xml");
		expected.add("ppt/slideLayouts/slideLayout2.xml");
		assertEquals(expected, doc.findSlideLayouts(doc.findSlides()));
	}

	private PowerpointDocument getPowerpointDocument(String resource, ConditionalParameters params) throws Exception {
		OpenXMLZipFile ooxml = new OpenXMLZipFile(
				new ZipFile(new File(getClass().getResource(resource).toURI())), XMLInputFactory.newInstance(),
										XMLOutputFactory.newInstance(), XMLEventFactory.newInstance(), "UTF-8");
		return (PowerpointDocument)ooxml.createDocument(params);
	}
}
