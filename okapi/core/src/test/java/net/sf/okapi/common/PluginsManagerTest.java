package net.sf.okapi.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import net.sf.okapi.common.plugins.PluginsManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class PluginsManagerTest {

	@Test
	public void testEmptyManager() {
		PluginsManager pm = new PluginsManager();
		assertNotNull(pm.getList());
		assertEquals(0, pm.getList().size());
		assertNotNull(pm.getURLs());
		assertEquals(0, pm.getURLs().size());
	}
}
