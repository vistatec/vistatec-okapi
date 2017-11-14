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

package net.sf.okapi.filters.openxml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import net.sf.okapi.common.FileLocation;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filters.FilterTestDriver;
import net.sf.okapi.common.filters.InputDocument;
import net.sf.okapi.filters.abstractmarkup.config.TaggedFilterConfiguration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * This tests the Office 2007 configuration files that drive the OpenXMLFilter.
 */
@RunWith(JUnit4.class)
public class OpenXMLConfigurationTest {
	
	private OpenXMLFilter filter;
	private LocaleId locEN = LocaleId.fromString("en");

	@Before
	public void setUp() throws Exception {
		filter = new OpenXMLFilter();
	}
	
	@Test
	public void defaultConfiguration() {		
		URL url = OpenXMLFilter.class.getResource("/net/sf/okapi/filters/openxml/wordConfiguration.yml");
		TaggedFilterConfiguration rules = new TaggedFilterConfiguration(url);	
		assertEquals(rules.getElementRuleTypeCandidate("w:p"), TaggedFilterConfiguration.RULE_TYPE.TEXT_UNIT_ELEMENT);
		assertEquals(rules.getElementRuleTypeCandidate("wp:docpr"), TaggedFilterConfiguration.RULE_TYPE.ATTRIBUTES_ONLY);
		
		Map<String, String> attributes = new HashMap<String, String>();
		assertTrue(rules.isTranslatableAttribute("wp:docpr", "name", attributes));
		assertTrue(rules.isTranslatableAttribute("pic:cnvpr", "name", attributes));
		assertFalse(rules.isTranslatableAttribute("w:p", "name", attributes));
		
		attributes.clear();
		attributes.put("w:val", "content-language");
		assertTrue(rules.isWritableLocalizableAttribute("w:lang", "w:val", attributes));

/*	
		attributes.clear();
		attributes.put("http-equiv", "content-type");
		assertFalse(rules.isReadOnlyLocalizableAttribute("meta", "content", attributes));
		
		attributes.clear();
		attributes.put("name", "generator");
		assertTrue(rules.isReadOnlyLocalizableAttribute("meta", "content", attributes));
*/
		url = OpenXMLFilter.class.getResource("/net/sf/okapi/filters/openxml/excelConfiguration.yml");

		url = OpenXMLFilter.class.getResource("/net/sf/okapi/filters/openxml/excelCommentConfiguration.yml");
		rules = new TaggedFilterConfiguration(url);	
		assertEquals(rules.getElementRuleTypeCandidate("t"), TaggedFilterConfiguration.RULE_TYPE.TEXT_MARKER_ELEMENT);

		url = OpenXMLFilter.class.getResource("/net/sf/okapi/filters/openxml/powerpointConfiguration.yml");
		rules = new TaggedFilterConfiguration(url);	
		assertEquals(rules.getElementRuleTypeCandidate("a:p"), TaggedFilterConfiguration.RULE_TYPE.TEXT_UNIT_ELEMENT);
		attributes = new HashMap<String, String>();
		attributes.put("w:val", "content-language");
		assertTrue(rules.isWritableLocalizableAttribute("a:rpr", "lang", attributes));
	}

	@Test
	public void testStartDocument () throws URISyntaxException {
		URL url = FileLocation.fromClass(getClass()).in("/BoldWorld.docx").asUrl();
		assertTrue("Problem in StartDocument", FilterTestDriver.testStartDocument(filter,
			new InputDocument(url.toURI().getPath(), null),
			"UTF-8", locEN, locEN));
	}
	
}
