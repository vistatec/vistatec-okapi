package net.sf.okapi.lib.preprocessing.filters.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.security.InvalidParameterException;

import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.filters.xmlstream.XmlStreamFilter;
import net.sf.okapi.steps.common.ResourceSimplifierStep;
import net.sf.okapi.steps.common.codesimplifier.CodeSimplifierStep;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestConstructor {

	private PreprocessingFilter filter;
	private IFilter internalFilter;

	@Test(expected = InvalidParameterException.class)
	public void testConstructor_filter_noSteps() {
		internalFilter = new XmlStreamFilter();
		filter = new PreprocessingFilter(internalFilter);
		assertNotNull(filter);
		assertEquals(internalFilter, filter.getFilter());
	}
	
	@Test
	public void testConstructor_filter_oneStep() {
		internalFilter = new XmlStreamFilter();
		filter = new PreprocessingFilter(internalFilter, new ResourceSimplifierStep());
		assertNotNull(filter);
		assertEquals(internalFilter, filter.getFilter());
	}
	
	@Test
	public void testConstructor_filter_twoSteps() {
		internalFilter = new XmlStreamFilter();
		filter = new PreprocessingFilter(internalFilter, new ResourceSimplifierStep(), new CodeSimplifierStep());
		assertNotNull(filter);
		assertEquals(internalFilter, filter.getFilter());
	}
	
	@Test(expected = InvalidParameterException.class)
	public void testConstructor_noFilter_noSteps() {
		internalFilter = null;
		filter = new PreprocessingFilter(internalFilter);
		assertNotNull(filter);
		assertNull(internalFilter);
		assertEquals(internalFilter, filter.getFilter());		
	}
	
	@Test(expected = InvalidParameterException.class)
	public void testConstructor_noFilter_oneStep() {
		internalFilter = null;
		filter = new PreprocessingFilter(internalFilter, new ResourceSimplifierStep());
		assertNotNull(filter);
		assertEquals(internalFilter, filter.getFilter());
	}
	
	@Test(expected = InvalidParameterException.class)
	public void testConstructor_noFilter_twoSteps() {
		internalFilter = null;
		filter = new PreprocessingFilter(internalFilter, new ResourceSimplifierStep(), new CodeSimplifierStep());
		assertNotNull(filter);
		assertEquals(internalFilter, filter.getFilter());
	}
	
}
