/*===========================================================================
  Copyright (C) 2008 Jim Hargrave
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

package net.sf.okapi.filters.xmlstream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.sf.okapi.common.FileLocation;
import net.sf.okapi.common.filters.InlineCodeFinder;
import net.sf.okapi.filters.abstractmarkup.config.TaggedFilterConfiguration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class XmlStreamConfigurationTest {
	final private FileLocation root = FileLocation.fromClass(getClass());

	@Before
	public void setUp() throws Exception {

	}

	@Test
	public void defaultConfiguration() {
		URL url = XmlStreamFilter.class.getResource("dita.yml");
		TaggedFilterConfiguration rules = new TaggedFilterConfiguration(url);
		assertTrue(rules.isRuleType("xml:id", TaggedFilterConfiguration.RULE_TYPE.ATTRIBUTE_ID));				
	}

	@Test
	public void preserveWhiteSpace() {
		URL url = XmlStreamFilter.class.getResource("default.yml");
		TaggedFilterConfiguration rules = new TaggedFilterConfiguration(url);
		assertTrue(rules.isRuleType("xml:space", TaggedFilterConfiguration.RULE_TYPE.ATTRIBUTE_PRESERVE_WHITESPACE));
	}

	@Test
	public void xmlLang() {
		URL url = XmlStreamFilter.class.getResource("default.yml");
		TaggedFilterConfiguration rules = new TaggedFilterConfiguration(url);

		Map<String, String> attributes = new HashMap<String, String>();
		attributes.clear();
		attributes.put("lang", "en");
		assertTrue(rules.isWritableLocalizableAttribute("x", "xml:lang", attributes));
		assertFalse(rules.isReadOnlyLocalizableAttribute("x", "xml:lang", attributes));
		assertFalse(rules.isTranslatableAttribute("x", "xml:lang", attributes));

		assertTrue(rules.isWritableLocalizableAttribute("p", "xml:lang", attributes));
		assertFalse(rules.isReadOnlyLocalizableAttribute("p", "xml:lang", attributes));
		assertFalse(rules.isTranslatableAttribute("p", "xml:lang", attributes));

		attributes.clear();
		attributes.put("xml:lang", "en");
		assertTrue(rules.isWritableLocalizableAttribute("x", "xml:lang", attributes));
	}

	@Test
	public void genericCodeTypes() {
		URL url = XmlStreamFilter.class.getResource("dita.yml");
		TaggedFilterConfiguration rules = new TaggedFilterConfiguration(url);
		assertNotNull(rules);
	}

	@Test
	public void textUnitCodeTypes() {
		URL url = XmlStreamFilter.class.getResource("dita.yml");
		TaggedFilterConfiguration rules = new TaggedFilterConfiguration(url);		
		assertNotNull(rules);
	}

	@Test
	public void collapseWhitespace() {
		URL url = XmlStreamFilter.class.getResource("default.yml");
		TaggedFilterConfiguration rules = new TaggedFilterConfiguration(url);
		assertFalse(rules.isGlobalPreserveWhitespace());

		url = root.in("/xml_collapseWhitespaceOff.yml").asUrl();
		rules = new TaggedFilterConfiguration(url);
		assertTrue(rules.isGlobalPreserveWhitespace());
	}
	
	@Test
	public void excludeByDefault() {
		URL url = XmlStreamFilter.class.getResource("default.yml");
		TaggedFilterConfiguration rules = new TaggedFilterConfiguration(url);
		assertFalse(rules.isGlobalExcludeByDefault()); 

		url = root.in("/excludeByDefault.yml").asUrl();
		rules = new TaggedFilterConfiguration(url);
		assertTrue(rules.isGlobalExcludeByDefault());
	}

	@Test
	public void testCodeFinderRules() {
		URL url = root.in("/xml_withCodeFinderRules.yml").asUrl();
		TaggedFilterConfiguration rules = new TaggedFilterConfiguration(url);
		assertTrue(rules.isUseCodeFinder());
		InlineCodeFinder cf = new InlineCodeFinder();
		cf.fromString(rules.getCodeFinderRules());
		cf.compile();
		ArrayList<String> list = cf.getRules();
		assertNotNull(list);
		assertEquals(2, list.size());
		assertEquals("[eE]", list.get(0));
		assertEquals("\\bVAR\\d\\b", list.get(1));
	}

	@Test
	public void attributeID() {
                URL url = XmlStreamFilter.class.getResource("default.yml");
		TaggedFilterConfiguration rules = new TaggedFilterConfiguration(url);

		Map<String, String> attributes = new HashMap<String, String>();
		attributes.clear();
		attributes.put("xml:id", "value");
		assertTrue(rules.isIdAttribute("p", "xml:id", attributes));
		assertFalse(rules.isIdAttribute("p", "foo", attributes));
	}
	
	@Test
	public void loadNonAsciiRuleFile() throws Exception {
		// nonAscii.yml contains some Japanese characters and it's
		// encoded in UTF-8. Loading the file shouldn't throw an
		// exception.
		URL url = root.in("/nonAscii.yml").asUrl();
		TaggedFilterConfiguration rules = new TaggedFilterConfiguration(url);
		assertNotNull(rules);

		File file = new File(url.toURI());
		rules = new TaggedFilterConfiguration(file);
		assertNotNull(rules);
	}
}
