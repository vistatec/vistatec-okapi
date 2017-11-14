package net.sf.okapi.common.annotation;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TermsAnnotationTest {

	@Test
	public void testSimple () {
		TermsAnnotation ann = new TermsAnnotation();
		ann.add("term1", "info1");
		assertEquals(1, ann.size());
		assertEquals("term1", ann.getTerm(0));
		assertEquals("info1", ann.getInfo(0));
	}
	
	@Test
	public void testNullInfo () {
		TermsAnnotation ann = new TermsAnnotation();
		ann.add("term1", null);
		// Null info is changed to empty
		assertEquals("", ann.getInfo(0));
	}

}
