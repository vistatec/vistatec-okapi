package net.sf.okapi.filters.openxml;

import org.custommonkey.xmlunit.Diff;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.xml.sax.InputSource;

import net.sf.okapi.common.FileLocation;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

@RunWith(JUnit4.class)
public class TestXMLSerializer {
	private XMLInputFactory inputFactory = XMLInputFactory.newInstance();
	private XMLEventFactory eventFactory = XMLEventFactory.newInstance();
	private XMLEventSerializer s;

	@Before
	public void setup() {
		s = new XMLEventSerializer();
	}

	@Test
	public void test() throws Exception {
		for (XMLEvent e : getEvents("/parts/simplifier/document-spelling.xml")) {
			s.add(e);
		}
		Diff diff = new Diff(new InputSource(getGoldReader("document-spelling.xml")),
				new InputSource(new StringReader(s.toString())));
		assertTrue(diff.similar());
	}

	@Test
	public void testChars() throws Exception {
		s.add(eventFactory.createCharacters("ABC><&'\"!"));
		assertEquals("ABC&gt;&lt;&amp;'\"!", s.toString());
	}

	@Test
	public void testAttrQuoting() throws Exception {
		for (XMLEvent e : getEvents("/serializer/attrquoting.xml")) {
			s.add(e);
		}
		Diff diff = new Diff(new InputSource(getGoldReader("attrquoting.xml")),
				new InputSource(new StringReader(s.toString())));
		assertTrue(diff.similar());
	}

	private Reader getGoldReader(String name) throws IOException {
		final FileLocation location = FileLocation.fromClass(getClass()).in("/gold/serializer/" + name);
		return new InputStreamReader(location.asInputStream(), StandardCharsets.UTF_8);
	}

	private List<XMLEvent> getEvents(String name) throws XMLStreamException {
		final FileLocation location = FileLocation.fromClass(getClass()).in(name);
		XMLEventReader xmlReader = inputFactory.createXMLEventReader(location.asInputStream(), "UTF-8");
		List<XMLEvent> events = new ArrayList<>();
		while (xmlReader.hasNext()) {
			events.add(xmlReader.nextEvent());
		}
		return events;
	}

}
