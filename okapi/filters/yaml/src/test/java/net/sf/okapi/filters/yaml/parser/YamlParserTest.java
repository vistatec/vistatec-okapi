/*===========================================================================
  Copyright (C) 2009-2014 by the Okapi Framework contributors
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

package net.sf.okapi.filters.yaml.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.okapi.common.FileUtil;
import net.sf.okapi.common.StreamUtil;

@RunWith(JUnit4.class)
public class YamlParserTest {
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void singleString() throws Exception {
			String snippet = "one # test";
			//String snippet = "\"[milk, pumpkin pie, eggs, juice]\"";
			//String snippet = "t: [v1: [v4, v5], v2, v3]";
			//String snippet = "one\n  %two\n  %three";
			//String snippet = "k: |-\n  one\n\n  two";
			YamlParser yp = new YamlParser(snippet);
			yp.setHandler(new DummyHandler());
			yp.parse();
	}
	
	@Test
	public void singleArrayNoSpace() throws Exception {
			String snippet = "t: ['v1','v4','v5']";
			YamlParser yp = new YamlParser(snippet);
			yp.setHandler(new DummyHandler());
			yp.parse();
	}
	
	@Test
	public void singleArrayWithSpace() throws Exception {
			String snippet = "t: ['v1', 'v4', 'v5']";
			YamlParser yp = new YamlParser(snippet);
			yp.setHandler(new DummyHandler());
			yp.parse();
	}
	
	@Test
	public void singleFile() throws Exception {
			String snippet = StreamUtil.streamUtf8AsString(YamlParserTest.class.getResourceAsStream("/yaml/issues/ios_emoji_surrogate.yaml"));
			//String snippet = StreamUtil.streamUtf8AsString(YamlParserTest.class.getResourceAsStream("/yaml/spec_test/example2_27.yaml"));
			YamlParser yp = new YamlParser(snippet);
			yp.setHandler(new DummyHandler());
			yp.parse();
	}

	@Test
	public void sanityCheck() throws Exception {
		for (File file : getTestFiles("/yaml/en.yml", Arrays.asList(".yml", ".yaml"), false)) {
			@SuppressWarnings("resource")
			String snippet = StreamUtil.streamUtf8AsString(new FileInputStream(file));
			YamlParser yp = new YamlParser(snippet);
			yp.setHandler(new DummyHandler());
			//System.out.println(file.getPath());
			try {
				yp.parse();
			} catch (ParseException|TokenMgrException e) {
				System.err.println("FAIL: " + file.getPath());
				System.err.println("Message: " + e.getMessage());
			}
		}
	}
	
	private Collection<File> getTestFiles(String resourcePath, final List<String> extensions, boolean isDirPath)
			throws URISyntaxException {
		File dir;
		if (isDirPath) {
			dir = new File(resourcePath);
		} else {
			URL url = YamlParserTest.class.getResource(resourcePath);
			dir = new File(url.toURI()).getParentFile();
		}

		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				for (String e : extensions) {
					if (name.endsWith(e)) {
						return true;
					}
				}
				return false;
			}
		};
		return FileUtil.getFilteredFiles(dir, filter, true);
	}
}
