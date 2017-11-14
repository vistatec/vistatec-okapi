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

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.ibm.icu.text.SimpleDateFormat;

@RunWith(JUnit4.class)
public class TestCreationDate {

	@Test
	public void testNioFileAttrs() throws URISyntaxException, IOException {
		Path file = Paths.get(this.getClass().getResource("gold.json").toURI());
		BasicFileAttributes attr = Files.readAttributes(file, BasicFileAttributes.class);

		System.out.println("creationTime: " + attr.creationTime());
		System.out.println("lastAccessTime: " + attr.lastAccessTime());
		System.out.println("lastModifiedTime: " + attr.lastModifiedTime());

		System.out.println("isDirectory: " + attr.isDirectory());
		System.out.println("isOther: " + attr.isOther());
		System.out.println("isRegularFile: " + attr.isRegularFile());
		System.out.println("isSymbolicLink: " + attr.isSymbolicLink());
		System.out.println("size: " + attr.size());
	}
	
	@Test
	public void testCreationDate() throws URISyntaxException, IOException {
		Path file = Paths.get(this.getClass().getResource("gold.json").toURI());
		BasicFileAttributes attr = Files.readAttributes(file, BasicFileAttributes.class);

		FileTime creationTime = attr.creationTime();
		long mils = creationTime.toMillis();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		Date d = new Date(mils);
		String date = df.format(d);
		System.out.println(date);
	}
	
	@Test
	public void testCreationDate2() {
		String date = VersionInfo.getFileCreationDate(this.getClass().getResource("gold.json"));
		System.out.println(date);
	}
	
}
