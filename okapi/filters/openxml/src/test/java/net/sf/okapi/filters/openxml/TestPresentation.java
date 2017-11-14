package net.sf.okapi.filters.openxml;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.xml.stream.XMLInputFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestPresentation {
	private XMLInputFactory factory = XMLInputFactory.newInstance();

	@Test
	public void testRels() throws Exception {
		Relationships rels = new Relationships(factory);
		rels.parseFromXML("/ppt/_rels/presentation.xml.rels", getReader("/presentation.xml.rels"));
		
		Presentation pres = new Presentation(factory, rels);
		pres.parseFromXML(getReader("/presentation.xml"));
		
		List<String> slideParts = pres.getSlidePartNames();
		assertEquals(4, slideParts.size());
		assertEquals("/ppt/slides/slide1.xml", slideParts.get(0));
		assertEquals("/ppt/slides/slide2.xml", slideParts.get(1));
		assertEquals("/ppt/slides/slide3.xml", slideParts.get(2));
		assertEquals("/ppt/slides/slide4.xml", slideParts.get(3));
	}
	
	private Reader getReader(String resource) throws UnsupportedEncodingException {
		InputStream input = getClass().getResourceAsStream(resource);
		return new InputStreamReader(input, "UTF-8");
	}
}
