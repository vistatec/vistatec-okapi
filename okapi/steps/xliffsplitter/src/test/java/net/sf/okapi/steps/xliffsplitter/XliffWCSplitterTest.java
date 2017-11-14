package net.sf.okapi.steps.xliffsplitter;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import net.sf.okapi.common.Util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class XliffWCSplitterTest {

	private String root;
	private String splitDir;
	
	@Before
	public void setUp() throws Exception {
		root = Util.getDirectoryName(getClass().getResource("input1.xlf").toURI().getPath())+"/";
		splitDir = root + "split-wc/";
		Util.createDirectories(splitDir);
	}
	
	@Test
	public void test5Words ()
		throws IOException
	{
		XliffWCSplitterParameters p = new XliffWCSplitterParameters();
		p.setThreshold(5);
		XliffWCSplitter spltr = new XliffWCSplitter(p);
		try ( InputStream is = getClass().getResourceAsStream("input1.xlf") ) {
			Util.deleteDirectory(splitDir, true);
			Map<String, Integer> res = spltr.process(
				getClass().getResourceAsStream("input1.xlf"), splitDir+"output5w", "en-US");
			assertEquals(3, res.size());
			assertEquals(7, (int)res.get("output5w_PART001.xlf"));
			assertEquals(8, (int)res.get("output5w_PART002.xlf"));
			assertEquals(0, (int)res.get("output5w_PART003.xlf"));
		}
	}

	@Test
	public void test3Words ()
		throws IOException
	{
		XliffWCSplitterParameters p = new XliffWCSplitterParameters();
		p.setThreshold(3);
		XliffWCSplitter spltr = new XliffWCSplitter(p);
		try ( InputStream is = getClass().getResourceAsStream("input1.xlf") ) {
			Util.deleteDirectory(splitDir, true);
			Map<String, Integer> res = spltr.process(
				getClass().getResourceAsStream("input1.xlf"), splitDir+"output3w", "en-US");
			assertEquals(5, res.size());
			assertEquals(4, (int)res.get("output3w_PART001.xlf"));
			assertEquals(3, (int)res.get("output3w_PART002.xlf"));
			assertEquals(4, (int)res.get("output3w_PART003.xlf"));
			assertEquals(4, (int)res.get("output3w_PART004.xlf"));
			assertEquals(0, (int)res.get("output3w_PART005.xlf"));
		}
	}

	@Test
	public void test9999Words ()
		throws IOException
	{
		XliffWCSplitterParameters p = new XliffWCSplitterParameters();
		p.setThreshold(9999);
		XliffWCSplitter spltr = new XliffWCSplitter(p);
		try ( InputStream is = getClass().getResourceAsStream("input1.xlf") ) {
			Util.deleteDirectory(splitDir, true);
			Map<String, Integer> res = spltr.process(
				getClass().getResourceAsStream("input1.xlf"), splitDir+"output9999w", "en-US");
			assertEquals(1, res.size());
			assertEquals(15, (int)res.get("output9999w_PART001.xlf"));
		}
	}

}
