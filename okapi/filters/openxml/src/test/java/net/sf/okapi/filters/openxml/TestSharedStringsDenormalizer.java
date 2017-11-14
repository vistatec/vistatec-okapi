package net.sf.okapi.filters.openxml;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;

import org.custommonkey.xmlunit.Diff;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import net.sf.okapi.common.FileLocation;

import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class TestSharedStringsDenormalizer {
	private XMLInputFactory inputFactory = XMLInputFactory.newInstance();
	private XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
	private XMLEventFactory eventFactory = XMLEventFactory.newInstance();

	@Test
	public void testDenormalize() throws Exception {
		StringWriter sw = new StringWriter();
		XMLEventReader reader = inputFactory.createXMLEventReader(
				new InputStreamReader(getClass().getResourceAsStream("/xlsx_parts/sharedStrings.xml"),
									  StandardCharsets.UTF_8));
		XMLEventWriter writer = outputFactory.createXMLEventWriter(sw);
		SharedStringMap ssm = new SharedStringMap();
		// Original content of the string table is, in order:
		// 0   1   2   3		// original index
		// A2, A1, B1, B2		// cell content
		// We are going to assume that the actual worksheet content looks like
		// 0   1   2   3   4   5   6   7   // new index
		// A1, A2, A1, A2, B1, B2, B2, B1  // cell content
		ssm.createEntryForString(1, false);
		ssm.createEntryForString(0, false);
		ssm.createEntryForString(1, false);
		ssm.createEntryForString(0, false);
		ssm.createEntryForString(2, false);
		ssm.createEntryForString(3, false);
		ssm.createEntryForString(3, false);
		ssm.createEntryForString(2, false);
		SharedStringsDenormalizer deno = new SharedStringsDenormalizer(eventFactory, ssm);
		deno.process(reader, writer);
		FileLocation location = FileLocation.fromClass(getClass()).in("/xlsx_parts/gold/Denormalized_sharedStrings.xml");
		try (Reader control = new InputStreamReader(location.asInputStream(), StandardCharsets.UTF_8);
			 Reader test = new StringReader(sw.toString())) {
			Diff diff = new Diff(control, test);
			assertTrue(diff.similar());
		}
	}
}
