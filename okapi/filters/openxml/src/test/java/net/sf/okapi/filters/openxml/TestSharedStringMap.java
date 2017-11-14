package net.sf.okapi.filters.openxml;

import java.util.ArrayList;
import java.util.List;

import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class TestSharedStringMap {

	@Test
	public void testReordering() {
		SharedStringMap ssm = new SharedStringMap();
		ssm.createEntryForString(4, true);
		ssm.createEntryForString(1, true);
		ssm.createEntryForString(3, true);
		ssm.createEntryForString(2, true);
		List<SharedStringMap.Entry> expected = new ArrayList<SharedStringMap.Entry>();
		expected.add(new SharedStringMap.Entry(4, 0, true));
		expected.add(new SharedStringMap.Entry(1, 1, true));
		expected.add(new SharedStringMap.Entry(3, 2, true));
		expected.add(new SharedStringMap.Entry(2, 3, true));
		assertEquals(expected, ssm.getEntries());
	}
}
