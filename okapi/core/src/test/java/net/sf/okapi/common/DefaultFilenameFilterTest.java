/*===========================================================================
  Copyright (C) 2012 by the Okapi Framework contributors
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

package net.sf.okapi.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class DefaultFilenameFilterTest {

	private File root;
	
	@Before
	public void setUp() throws URISyntaxException {
		File file = FileLocation.fromClass(DefaultFilenameFilterTest.class).in("/test.txt").asFile();
		root = file.getParentFile();
	}

	@Test
	public void testPattern1 () {
		File[] files = root.listFiles(new DefaultFilenameFilter("t*.txt", false));
		assertEquals(6, files.length);
		for ( File file : files ) {
			assertTrue(file.getName().startsWith("t") || file.getName().startsWith("T"));
			assertTrue(file.getName().endsWith(".txt"));
		}
	}
	
	@Test
	public void testPattern2 () {
		File[] files = root.listFiles(new DefaultFilenameFilter("test?.t?t", false));
		assertEquals(2, files.length);
		for ( File file : files ) {
			assertTrue(file.getName().startsWith("test") || file.getName().startsWith("Test"));
			assertTrue(file.getName().endsWith(".txt") || file.getName().endsWith(".tzt"));
		}
	}
	
	@Test
	public void testPattern3 () {
		File[] files = root.listFiles(new DefaultFilenameFilter("testE*.t?t", false));
		assertEquals(2, files.length);
		for ( File file : files ) {
			assertTrue(file.getName().startsWith("TestE"));
			assertTrue(file.getName().endsWith(".txt") || file.getName().endsWith(".tzt"));
		}
	}
	
	@Test
	public void testPattern4 () {
		File[] files = root.listFiles(new DefaultFilenameFilter("test.txt", false));
		assertEquals(1, files.length);
		assertEquals("test.txt", files[0].getName());
	}
	
	@Test
	public void testPattern5 () {
		File[] files = root.listFiles(new DefaultFilenameFilter("TestEtc.*", false));
		assertEquals(2, files.length);
		for ( File file : files ) {
			assertTrue(file.getName().startsWith("TestEt"));
		}
	}
	
	@Test
	public void testPattern6 () {
		File[] files = root.listFiles(new DefaultFilenameFilter("*.tzt", false));
		assertEquals(1, files.length);
		assertEquals("testB.tzt", files[0].getName());
		// Backward compatible constructor
		files = root.listFiles(new DefaultFilenameFilter(".tzt"));
		assertEquals(1, files.length);
		assertEquals("testB.tzt", files[0].getName());
	}
	
	@Test
	public void testPattern7 () {
		File[] files = root.listFiles(new DefaultFilenameFilter("*.htm", false));
		assertEquals(0, files.length); // We have test.html not test.htm
		// Backward compatible constructor
		files = root.listFiles(new DefaultFilenameFilter(".htm"));
		assertEquals(0, files.length); // We have test.html not test.htm
	}
	
	@Test
	public void testPattern8 () {
		// Case-sensitive call
		File[] files = root.listFiles(new DefaultFilenameFilter("t*.txt", true));
		assertEquals(2, files.length);
		for ( File file : files ) {
			assertTrue(file.getName().startsWith("t"));
			assertTrue(file.getName().endsWith(".txt"));
		}
	}
	
}
