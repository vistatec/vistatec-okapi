package net.sf.okapi.lib.preprocessing.filters.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import net.sf.okapi.common.Event;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filters.FilterUtil;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.filters.html.HtmlFilter;
import net.sf.okapi.filters.xmlstream.XmlStreamFilter;
import net.sf.okapi.lib.preprocessing.filters.simplification.Parameters;
import net.sf.okapi.lib.preprocessing.filters.simplification.SimplificationFilter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests of SimplificationFilter configurations.
 * Placed in this package to have access to protected filter.getFilter(). 
 */
@RunWith(JUnit4.class)
public class TestConfigurations {

	private static final LocaleId ENUS = new LocaleId("en", "us");
	
	@Test
	public void testConfigurations() {
		SimplificationFilter filter = (SimplificationFilter) FilterUtil.createFilter("okf_simplification");
		assertNotNull(filter);
		assertTrue(filter.getFilter() instanceof XmlStreamFilter);
		Parameters params = (Parameters) filter.getParameters();
		assertTrue(params.isSimplifyResources());
		assertTrue(params.isSimplifyCodes());
		
		filter = (SimplificationFilter) FilterUtil.createFilter("okf_simplification-xmlResources");
		assertNotNull(filter);
		assertTrue(filter.getFilter() instanceof XmlStreamFilter);
		params = (Parameters) filter.getParameters();
		assertTrue(params.isSimplifyResources());
		assertFalse(params.isSimplifyCodes());
		
		filter = (SimplificationFilter) FilterUtil.createFilter("okf_simplification-xmlCodes");
		assertNotNull(filter);
		assertTrue(filter.getFilter() instanceof XmlStreamFilter);
		params = (Parameters) filter.getParameters();
		assertFalse(params.isSimplifyResources());
		assertTrue(params.isSimplifyCodes());
	}
	
	@Test
	public void testStartDocument() {
		SimplificationFilter filter = new SimplificationFilter();
		assertNotNull(filter);
		assertTrue(filter.getFilter() instanceof XmlStreamFilter);
		Parameters params = (Parameters) filter.getParameters();
		assertEquals("okf_xmlstream", params.getFilterConfigId());
		assertTrue(params.isSimplifyResources());
		assertFalse(params.isSimplifyCodes());
		params.setFilterConfigId("okf_html");
		assertTrue(filter.getFilter() instanceof XmlStreamFilter); // Not yet changed
		assertEquals("okf_html", params.getFilterConfigId());
		
		filter.open(new RawDocument("test", ENUS));
		assertTrue(filter.getFilter() instanceof HtmlFilter); // Changed
		assertTrue(filter.hasNext());
		Event e = filter.next();
		assertTrue(e.isStartDocument());
		StartDocument sd = e.getStartDocument();
		assertNotNull(sd);
		assertEquals(params, sd.getFilterParameters()); // The wrapper has replaced parameters with its own
		filter.close();
	}
}
