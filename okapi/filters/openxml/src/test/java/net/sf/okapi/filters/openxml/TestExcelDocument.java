package net.sf.okapi.filters.openxml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;

import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

/**
 * Misc tests related to XLSX processing.
 */
@RunWith(JUnit4.class)
public class TestExcelDocument {
	private XMLInputFactory factory = XMLInputFactory.newInstance();

	@Test
	public void testGetWorksheets() throws Exception {
		// Use a file with multiple sheets that appear out-of-order in the zip
		ExcelDocument doc = getExcelDocument("/ordering.xlsx", new ConditionalParameters());
		List<String> worksheets = doc.findWorksheets();
		List<String> expected = new ArrayList<String>();
		expected.add("xl/worksheets/sheet1.xml");
		expected.add("xl/worksheets/sheet2.xml");
		expected.add("xl/worksheets/sheet3.xml");
		doc.getZipFile().close();
		assertEquals(expected, worksheets);
	}

	@Test
	public void testGetSharedStrings() throws Exception {
		ExcelDocument doc = getExcelDocument("/ordering.xlsx", new ConditionalParameters());
		String sstPart = doc.findSharedStrings();
		doc.getZipFile().close();
		assertEquals("xl/sharedStrings.xml", sstPart);
	}

	private ZipFile getZipFile(String resource) throws ZipException, IOException, URISyntaxException {
		return new ZipFile(new File(getClass().getResource(resource).toURI()));
	}

	private ExcelDocument getExcelDocument(String resource, ConditionalParameters params) throws Exception {
		OpenXMLZipFile ooxml = new OpenXMLZipFile(getZipFile(resource), factory, 
										XMLOutputFactory.newInstance(), XMLEventFactory.newInstance(), "UTF-8");
		ExcelDocument doc = (ExcelDocument)ooxml.createDocument(params);
		doc.initialize();
		return doc;
	}
}
