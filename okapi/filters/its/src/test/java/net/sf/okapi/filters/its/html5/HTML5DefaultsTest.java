package net.sf.okapi.filters.its.html5;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filters.FilterTestDriver;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.ITextUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class HTML5DefaultsTest {

	private HTML5Filter filter;
	private LocaleId locEN = LocaleId.ENGLISH;
	private LocaleId locFR = LocaleId.FRENCH;

	@Before
	public void setUp () {
		filter = new HTML5Filter();
	}

	@Test
	public void testWinthinText () {
		String snippet = "<!DOCTYPE html><html lang=\"en\"><head><meta charset=utf-8><title>Title</title></head><body>"
			+ "<p>Text in <span>bold</span>. Text in <i its-within-text='no'>italics</i>."
			+ "</body></html>";
		ArrayList<Event> list = getEvents(snippet);
		ITextUnit tu = FilterTestDriver.getTextUnit(list, 2);
		assertNotNull(tu);
		List<Code> codes = tu.getSource().getFirstContent().getCodes();
		assertEquals(2, codes.size());
	}

	@Test
	public void testTranslateOverrides () {
		String snippet = "<!DOCTYPE html><html lang=\"en\" translate='no'><head><meta charset=utf-8>"
			+ "<meta name='keywords' content='t1'>"
			+ "<title>t2</title></head><body>"
			+ "<p title='t3'>t5<img src=demo.png alt='t4'></p>"
			+ "</body></html>";
		ArrayList<Event> list = getEvents(snippet);
//TODO:		assertEquals(true, null == FilterTestDriver.getTextUnit(list, 1));
	}

	private ArrayList<Event> getEvents (String snippet) {
		return FilterTestDriver.getEvents(filter, snippet, locEN, locFR);
	}

}
