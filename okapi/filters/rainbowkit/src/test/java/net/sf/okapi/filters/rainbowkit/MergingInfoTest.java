package net.sf.okapi.filters.rainbowkit;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@RunWith(JUnit4.class)
public class MergingInfoTest {
	
	@Test
	public void testSimpleWrite () {
		MergingInfo info = new MergingInfo(1, "et", "inpPath", "fi", "#v1\nfp", "inEnc", "outPath", "outEnc");
		String res = info.writeToXML("test", false);
		String expected1 = "<test xml:space=\"preserve\" docId=\"1\" extractionType=\"et\" relativeInputPath=\"inpPath\" filterId=\"fi\" inputEncoding=\"inEnc\" relativeTargetPath=\"outPath\" targetEncoding=\"outEnc\" selected=\"1\">"
			+ "#v1\nfp</test>";
		assertEquals(expected1, res);
	}
	
	@Test
	public void testSimpleWriteBase64 () {
		MergingInfo info = new MergingInfo(1, "et", "inpPath", "fi", "#v1\nfp", "inEnc", "outPath", "outEnc");
		String res = info.writeToXML("test", true);
		String expected1 = "<test xml:space=\"preserve\" docId=\"1\" extractionType=\"et\" relativeInputPath=\"inpPath\" filterId=\"fi\" inputEncoding=\"inEnc\" relativeTargetPath=\"outPath\" targetEncoding=\"outEnc\" selected=\"1\">"
			+ "I3YxCmZw</test>";
		assertEquals(expected1, res);
	}
	
	@Test
	public void testSimpleWriteAndRead () throws SAXException, IOException, ParserConfigurationException {
		MergingInfo info1 = new MergingInfo(1, "et", "inpPath", "fi", "#v1\nfp", "inEnc", "outPath", "outEnc");
		String res = info1.writeToXML("test", false);
		
		DocumentBuilderFactory Fact = DocumentBuilderFactory.newInstance();
		InputSource is = new InputSource(new StringReader(res));
		Document doc = Fact.newDocumentBuilder().parse(is);
		Element elem = doc.getDocumentElement();
		MergingInfo info2 = MergingInfo.readFromXML(elem);

		assertEquals(res, info2.writeToXML("test", false));
		assertEquals(info1.getDocId(), info2.getDocId());
		assertEquals(info1.getFilterId(), info2.getFilterId());
		assertEquals(info1.getExtractionType(), info2.getExtractionType());
		assertEquals(info1.getRelativeInputPath(), info2.getRelativeInputPath());
		assertEquals(info1.getFilterParameters(), info2.getFilterParameters());
		assertEquals(info1.getInputEncoding(), info2.getInputEncoding());
		assertEquals(info1.getTargetEncoding(), info2.getTargetEncoding());
		assertEquals(info1.getRelativeTargetPath(), info2.getRelativeTargetPath());
	}
	
	@Test
	public void testSimpleWriteAndReadBase64 () throws SAXException, IOException, ParserConfigurationException {
		MergingInfo info1 = new MergingInfo(1, "et", "inpPath", "fi", "#v1\nfp", "inEnc", "outPath", "outEnc");
		String res = info1.writeToXML("test", true);
		
		DocumentBuilderFactory Fact = DocumentBuilderFactory.newInstance();
		InputSource is = new InputSource(new StringReader(res));
		Document doc = Fact.newDocumentBuilder().parse(is);
		Element elem = doc.getDocumentElement();
		MergingInfo info2 = MergingInfo.readFromXML(elem);

		assertEquals(res, info2.writeToXML("test", true));
		assertEquals(info1.getDocId(), info2.getDocId());
		assertEquals(info1.getFilterId(), info2.getFilterId());
		assertEquals(info1.getExtractionType(), info2.getExtractionType());
		assertEquals(info1.getRelativeInputPath(), info2.getRelativeInputPath());
		assertEquals(info1.getFilterParameters(), info2.getFilterParameters());
		assertEquals(info1.getInputEncoding(), info2.getInputEncoding());
		assertEquals(info1.getTargetEncoding(), info2.getTargetEncoding());
		assertEquals(info1.getRelativeTargetPath(), info2.getRelativeTargetPath());
	}
	
}
