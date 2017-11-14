package net.sf.okapi.filters.rtf.integration;

//import static org.junit.Assert.assertTrue;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import net.sf.okapi.common.Util;
import net.sf.okapi.common.filters.InputDocument;
import net.sf.okapi.filters.rtf.RtfTestUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
//import net.sf.okapi.filters.rtf.RTFFilter;

@RunWith(JUnit4.class)
public class ExtractionComparisionTest {
//	private RTFFilter rtfFilter;
	private String[] testFileList;	
	private String root;

	@Before
	public void setUp() throws Exception {
//		rtfFilter = new RTFFilter();		
		testFileList = RtfTestUtils.getTestFiles();
		
		URL url = ExtractionComparisionTest.class.getResource("/AddComments.rtf");
		root = Util.getDirectoryName(url.getPath());
		root = Util.getDirectoryName(root) + "/data/";
	}

	@After
	public void tearDown() throws Exception {		
	}
	
	@Test
	public void testDoubleExtraction () throws URISyntaxException {		
		//RoundTripComparison rtc = new RoundTripComparison();
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
		for (String f : testFileList) {
			list.add(new InputDocument(root + f, null));
		}
//TODO: implement RTF Filter as a filter		assertTrue(rtc.executeCompare(rtfFilter, list, "UTF-8", "en", "en"));
	}
}
