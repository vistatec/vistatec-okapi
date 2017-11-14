package net.sf.okapi.common.filters;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class SubFilterTest {

	@Test
	public void testResourceIdMatching() {
		assertTrue(SubFilter.resourceIdsMatch("tu1_ssf1", "tu1_esf1"));
		assertFalse(SubFilter.resourceIdsMatch("tu1_ssf1", "tu1_ssf1"));
		assertFalse(SubFilter.resourceIdsMatch("tu1_ssf1", "tu1_esf2"));
		assertTrue(SubFilter.resourceIdsMatch("tu1_sg2_ssf3", "tu1_sg2_esf3"));
		assertFalse(SubFilter.resourceIdsMatch("tu1_sg2_ssf3", "tu1_sg1_esf3"));
		assertFalse(SubFilter.resourceIdsMatch("tu1_sg2_ssf3", "tu1_sg2_esf1"));
		assertFalse(SubFilter.resourceIdsMatch("tu1_sg2_ssf3", "tu2_sg2_esf3"));
	}
}
