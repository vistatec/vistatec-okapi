/*===========================================================================
  Copyright (C) 2008-2014 by the Okapi Framework contributors
-----------------------------------------------------------------------------
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
===========================================================================*/

package net.sf.okapi.lib.tkit.jarswitcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;

import net.sf.okapi.common.FileCompare;
import net.sf.okapi.common.FileUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestVersionManager {
	
	private VersionManager manager;
	private FileCompare fc = new FileCompare();
	
	@Before
	public void setUp() {
		manager = new VersionManager();
		
		manager.add("m13", "/D:/git_local_repo/okapi_master/m13/okapi/assemblies/tikal/target/okapi-assembly-tikal-0.13-classes.jar", "2011-7-29");
		manager.add("m14", "/D:/git_local_repo/okapi_master/m14/okapi/assemblies/tikal/target/okapi-assembly-tikal-0.14-classes.jar", "2011-10-1");
		manager.add("m12", "/D:/git_local_repo/okapi_master/m12/okapi/assemblies/tikal/target/okapi-assembly-tikal-0.12-classes.jar", "2011-5-31");
		manager.add("m18", "/D:/git_local_repo/okapi_master/m18/okapi/assemblies/tikal/target/okapi-assembly-tikal-0.18-classes.jar", "2012-9-8");
		manager.add("m17", "/D:/git_local_repo/okapi_master/m17/okapi/assemblies/tikal/target/okapi-assembly-tikal-0.17-classes.jar", "2012-7-8");
		manager.add("m15", "/D:/git_local_repo/okapi_master/m15/okapi/assemblies/tikal/target/okapi-assembly-tikal-0.15-classes.jar", "2012-1-13");
		manager.add("m11", "/D:/git_local_repo/okapi_master/m11/okapi/assemblies/tikal/target/okapi-assembly-tikal-0.11-classes.jar", "2011-4-2");
		manager.add("m16", "/D:/git_local_repo/okapi_master/m16/okapi/assemblies/tikal/target/okapi-assembly-tikal-0.16-classes.jar", "2012-4-15");
		manager.add("m21", "/D:/git_local_repo/okapi_master/m21/okapi/assemblies/tikal/target/okapi-assembly-tikal-0.21-classes.jar", "2013-4-16");
		manager.add("m20", "/D:/git_local_repo/okapi_master/m20/okapi/assemblies/tikal/target/okapi-assembly-tikal-0.20-classes.jar", "2013-2-17");		
		manager.add("m24", "/D:/git_local_repo/okapi_master/m24/okapi/assemblies/tikal/target/okapi-assembly-tikal-0.24-classes.jar", "2014-1-6");
		manager.add("m22", "/D:/git_local_repo/okapi_master/m22/okapi/assemblies/tikal/target/okapi-assembly-tikal-0.22-classes.jar", "2013-7-19");
		manager.add("m19", "/D:/git_local_repo/okapi_master/m19/okapi/assemblies/tikal/target/okapi-assembly-tikal-0.19-classes.jar", "2012-11-24");
		manager.add("m23", "/D:/git_local_repo/okapi_master/m23/okapi/assemblies/tikal/target/okapi-assembly-tikal-0.23-classes.jar", "2013-9-27");				
	}
	
	@Test
	public void testSorting() {
		VersionInfo[] arr = manager.getVersions().toArray(new VersionInfo[0]);
		assertEquals("m11", arr[0].getLabel());
		assertEquals("m12", arr[1].getLabel());
		assertEquals("m13", arr[2].getLabel());
		assertEquals("m14", arr[3].getLabel());
		assertEquals("m15", arr[4].getLabel());
		assertEquals("m16", arr[5].getLabel());
		assertEquals("m17", arr[6].getLabel());
		assertEquals("m18", arr[7].getLabel());
		assertEquals("m19", arr[8].getLabel());
		assertEquals("m20", arr[9].getLabel());
		assertEquals("m21", arr[10].getLabel());
		assertEquals("m22", arr[11].getLabel());
		assertEquals("m23", arr[12].getLabel());
		assertEquals("m24", arr[13].getLabel());
	}
	
	@Test
	public void testGetLabelByDate() {
		assertEquals(null, manager.getLabelByDate("2010-01-02"));
		
		assertEquals("m19", manager.getLabelByDate("2012-11-24"));
		assertEquals("m20", manager.getLabelByDate("2013-02-17"));
		assertEquals("m21", manager.getLabelByDate("2013-04-16"));
		assertEquals("m22", manager.getLabelByDate("2013-07-19"));
		assertEquals("m23", manager.getLabelByDate("2013-09-27"));
		assertEquals("m24", manager.getLabelByDate("2014-01-06"));
		
		assertEquals("m19", manager.getLabelByDate("2013-02-16"));
		assertEquals("m20", manager.getLabelByDate("2013-04-15"));
		assertEquals("m21", manager.getLabelByDate("2013-07-18"));
		assertEquals("m22", manager.getLabelByDate("2013-09-26"));
		assertEquals("m23", manager.getLabelByDate("2014-01-05"));
		assertEquals("m24", manager.getLabelByDate("2014-04-08"));
	}

	@Test
	public void testGetPathByLabel() {
		assertEquals("/D:/git_local_repo/okapi_master/m19/okapi/assemblies/tikal/target/okapi-assembly-tikal-0.19-classes.jar", manager.getPathByLabel("m19"));
		assertEquals("/D:/git_local_repo/okapi_master/m20/okapi/assemblies/tikal/target/okapi-assembly-tikal-0.20-classes.jar", manager.getPathByLabel("m20"));
		assertEquals("/D:/git_local_repo/okapi_master/m21/okapi/assemblies/tikal/target/okapi-assembly-tikal-0.21-classes.jar", manager.getPathByLabel("m21"));
		assertEquals("/D:/git_local_repo/okapi_master/m22/okapi/assemblies/tikal/target/okapi-assembly-tikal-0.22-classes.jar", manager.getPathByLabel("m22"));
		assertEquals("/D:/git_local_repo/okapi_master/m23/okapi/assemblies/tikal/target/okapi-assembly-tikal-0.23-classes.jar", manager.getPathByLabel("m23"));
		assertEquals("/D:/git_local_repo/okapi_master/m24/okapi/assemblies/tikal/target/okapi-assembly-tikal-0.24-classes.jar", manager.getPathByLabel("m24"));
	}
	
	@Test
	public void testLoad() throws IOException, URISyntaxException {
		// store(String)		
		File file = FileUtil.createTempFile("~okapi-35_tests_");
		String path = file.getAbsolutePath();
		InputStream fis = new FileInputStream(path);			
		try {
			manager.store(path);			
			assertTrue(fc.compareFilesPerLines(fis, getClass().getResourceAsStream("gold.json"), "UTF-8"));
			
			// load(String)
			manager.getVersions().clear();
			assertEquals(0, manager.getVersions().size());
			manager.load(path);
			assertEquals(14, manager.getVersions().size());
			
			// load(URL)
			manager.getVersions().clear();
			assertEquals(0, manager.getVersions().size());
			manager.load(this.getClass().getResource("gold.json"));
			assertEquals(14, manager.getVersions().size());
			
			// load(URI)
			manager.getVersions().clear();
			assertEquals(0, manager.getVersions().size());
			manager.load(this.getClass().getResource("gold.json").toURI());
			assertEquals(14, manager.getVersions().size());
			
			// load(InputStream)
			manager.getVersions().clear();
			assertEquals(0, manager.getVersions().size());
			manager.load(this.getClass().getResourceAsStream("gold.json"));
			assertEquals(14, manager.getVersions().size());
		}
		finally {
			fis.close();
			file.delete();
		}						
	}

	@Test
	public void testLoadVersion() throws IOException {
		// loadVersion(String)
		manager = new VersionManager();
		manager.loadVersion("m20");
		assertEquals(14, manager.getVersions().size());
		assertEquals("m20", manager.getActiveVersionLabel());

		// store(OutputStream)
		File file = FileUtil.createTempFile("~okapi-36_okapi-lib-tkit_testLoadVersion_");
		String path = file.getAbsolutePath();
		InputStream fis = new FileInputStream(path);
		OutputStream fos = new FileOutputStream(path);
		try {			
			manager.store(fos);
			assertTrue(fc.compareFilesPerLines(fis, getClass().getResourceAsStream("gold.json"), "UTF-8"));
			
			// loadVersion(URL)		
			manager.getVersions().clear();
			assertEquals(0, manager.getVersions().size());
			manager.loadVersion(this.getClass().getResource("dir1/ratel.jar"));
			assertEquals(14, manager.getVersions().size());
			
			// loadVersion(File)
			manager.getVersions().clear();
			assertEquals(0, manager.getVersions().size());
			manager.loadVersion(FileUtil.urlToFile(this.getClass().getResource("dir2/")));
			assertEquals(14, manager.getVersions().size());
		}				
		finally {
			fos.close();
			fis.close();
			file.delete();
		}		
	}
	
	@Test
	public void testClassLoader() throws ClassNotFoundException {
		// 1. Single jar in a dir
		ClassLoader cl = manager.setClassLoader(this.getClass().getResource("dir1/ratel.jar"));
		assertNotNull(cl);
		
		// There's no dependency to net.sf.okapi.applications in this project, so we load it from outside
		Class<?> cls = Class.forName("net.sf.okapi.applications.ratel.Main", false, cl);
		
		// 2. Single exploded jar in a dir
		cl = manager.setClassLoader(this.getClass().getResource("dir2/"));
		assertNotNull(cl);
		
		cls = cl.loadClass("net.sf.okapi.applications.ratel.Main");
		assertNotNull(cls);
		
		cls = Class.forName("net.sf.okapi.applications.ratel.Main", false, cl);
		assertNotNull(cls);
		
		// 3. Mixture of exploded and normal jars in a dir
		cl = manager.setClassLoader(this.getClass().getResource("dir3/"));
		assertNotNull(cl);
		
//		cls = Class.forName("net.sf.okapi.applications.tikal.Main$Timer", false, cl);
//		assertNotNull(cls);
		
//		cls = Class.forName("net.sf.okapi.applications.tikal.logger.LogHandlerJDK", false, cl);
//		assertNotNull(cls);
		
		cls = Class.forName("org.slf4j.impl.StaticLoggerBinder", false, cl);
		assertNotNull(cls);
		
		cls = Class.forName("net.sf.okapi.filters.html.Parameters", false, cl);
		assertNotNull(cls);
		
		cls = Class.forName("net.sf.okapi.connectors.mymemory.MyMemoryTMConnector", false, cl);
		assertNotNull(cls);
		
		// 4. Old loader (the parent loader) is still able to load classes
		cls = Class.forName("net.sf.okapi.filters.regex.RegexFilter", false, cl);
		assertNotNull(cls);
	}
}
