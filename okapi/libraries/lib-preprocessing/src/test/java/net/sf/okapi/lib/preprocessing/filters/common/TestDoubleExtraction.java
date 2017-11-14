package net.sf.okapi.lib.preprocessing.filters.common;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filters.InputDocument;
import net.sf.okapi.common.filters.RoundTripComparison;
import net.sf.okapi.filters.html.HtmlFilter;
import net.sf.okapi.steps.common.ResourceSimplifierStep;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestDoubleExtraction {

	private static final LocaleId ENUS = new LocaleId("en", "us");
	
	@Test
	public void testDoubleExtraction () throws URISyntaxException, IOException {
		IFilter filter = new PreprocessingFilter(new HtmlFilter(), new ResourceSimplifierStep());
		
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();				
		String pathBase = Util.getDirectoryName(this.getClass().getResource("/aa324.html").toURI().getPath()) + "/";
		
		list.add(new InputDocument(pathBase + "aa324.html", null));
		list.add(new InputDocument(pathBase + "form.html", null));
		list.add(new InputDocument(pathBase + "W3CHTMHLTest1.html", null));
		list.add(new InputDocument(pathBase + "msg00058.html", null));
		
		RoundTripComparison rtc = new RoundTripComparison();
		assertTrue(rtc.executeCompare(filter, list, "UTF-8", ENUS, ENUS, "out"));
	}
}
