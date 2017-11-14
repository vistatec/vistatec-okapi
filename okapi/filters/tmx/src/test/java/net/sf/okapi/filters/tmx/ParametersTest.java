package net.sf.okapi.filters.tmx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ParametersTest {

	Parameters p;
	
	@Before
	public void setUp() throws Exception {
		p = new Parameters();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testParameters() {
		assertFalse("escapeGT should default to false", p.getEscapeGT());
		assertTrue("processAllTargets should default to true", p.getProcessAllTargets());
		assertTrue("consolidateDpSkeleton should default to true", p.getConsolidateDpSkeleton());
		assertFalse("exitOnInvalid should default to false", p.getExitOnInvalid());
		assertEquals("SegType default to 2", p.getSegType(), TmxFilter.SEGTYPE_OR_SENTENCE);
	}

	@Test
	public void testReset() {
		p.fromString("#v1\nescapeGT.b=true\nprocessAllTargets.b=false\nconsolidateDpSkeleton.b=false\nexitOnInvalid.b=true\nsegType.i=1");
		p.reset();
		assertFalse("escapeGT should be false", p.getEscapeGT());
		assertTrue("processAllTargets should be true", p.getProcessAllTargets());
		assertTrue("consolidateDpSkeleton should be true", p.getConsolidateDpSkeleton());
		assertFalse("exitOnInvalid should default to false", p.getExitOnInvalid());
		assertEquals("SegType default to 2", p.getSegType(), TmxFilter.SEGTYPE_OR_SENTENCE);
	}

	@Test
	public void testFromString() {
		p.fromString("#v1\nescapeGT.b=true\nprocessAllTargets.b=false\nconsolidateDpSkeleton.b=false\nexitOnInvalid.b=true\nsegType.i=1");
		assertTrue("escapeGT should be true", p.getEscapeGT());
		assertFalse("processAllTargets should be false", p.getProcessAllTargets());
		assertFalse("consolidateDpSkeleton should be false", p.getConsolidateDpSkeleton());
		assertTrue("exitOnInvalid should to true", p.getExitOnInvalid());
		assertEquals("SegType changed to 1", p.getSegType(), TmxFilter.SEGTYPE_PARA);
	}

	@Test
	public void testToString() {
		assertEquals("Incorrect format","#v1\nescapeGT.b=false\nprocessAllTargets.b=true\nconsolidateDpSkeleton.b=true\nexitOnInvalid.b=false\nsegType.i=2\npropValueSep=, ",p.toString());
	}
}
