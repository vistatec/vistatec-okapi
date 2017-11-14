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

package net.sf.okapi.filters.html;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
public class HtmlConfigurationTest {
	private FileLocation location = FileLocation.fromClass(HtmlConfigurationTest.class);

	@Before
	public void setUp() throws Exception {

	}

	@Test
	public void defaultConfiguration() {
		URL url = HtmlFilter.class.getResource("nonwellformedConfiguration.yml");
		TaggedFilterConfiguration rules = new TaggedFilterConfiguration(url);
		assertTrue(rules.isRuleType("title", TaggedFilterConfiguration.RULE_TYPE.ATTRIBUTE_TRANS));
		assertEquals(TaggedFilterConfiguration.RULE_TYPE.TEXT_UNIT_ELEMENT, rules.getElementRuleTypeCandidate("title"));
		assertEquals(TaggedFilterConfiguration.RULE_TYPE.INLINE_EXCLUDED_ELEMENT, rules.getElementRuleTypeCandidate("abbr"));
		assertEquals(TaggedFilterConfiguration.RULE_TYPE.ATTRIBUTES_ONLY, rules.getElementRuleTypeCandidate("area"));
		assertEquals(TaggedFilterConfiguration.RULE_TYPE.EXCLUDED_ELEMENT, rules.getElementRuleTypeCandidate("script"));
		assertTrue(rules.isRuleType("pre", TaggedFilterConfiguration.RULE_TYPE.PRESERVE_WHITESPACE));
		assertEquals(TaggedFilterConfiguration.RULE_TYPE.ATTRIBUTES_ONLY, rules.getElementRuleTypeCandidate("meta"));
	}

	@Test
	public void metaTag() {
		URL url = HtmlFilter.class.getResource("nonwellformedConfiguration.yml");
		TaggedFilterConfiguration rules = new TaggedFilterConfiguration(url);

		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("name", "keywords");
		assertTrue(rules.isTranslatableAttribute("meta", "content", attributes));
		assertFalse(rules.isTranslatableAttribute("dummy", "content", attributes));
		attributes.put("name", "description");
		assertTrue(rules.isTranslatableAttribute("meta", "content", attributes));
		assertFalse(rules.isTranslatableAttribute("dummy", "content", attributes));

		attributes.clear();
		attributes.put("http-equiv", "content-language");
		assertTrue(rules.isWritableLocalizableAttribute("meta", "content", attributes));

		attributes.clear();
		attributes.put("http-equiv", "content-type");
		assertFalse(rules.isReadOnlyLocalizableAttribute("meta", "content", attributes));

		attributes.clear();
		attributes.put("name", "generator");
		assertTrue(rules.isReadOnlyLocalizableAttribute("meta", "content", attributes));
	}

	@Test
	public void preserveWhiteSpace() {
		URL url = HtmlFilter.class.getResource("nonwellformedConfiguration.yml");
		TaggedFilterConfiguration rules = new TaggedFilterConfiguration(url);

		assertTrue(rules.isRuleType("pre", TaggedFilterConfiguration.RULE_TYPE.PRESERVE_WHITESPACE));
		assertEquals(rules.getElementRuleTypeCandidate("style"),
				TaggedFilterConfiguration.RULE_TYPE.EXCLUDED_ELEMENT);
		assertTrue(rules.isRuleType("pre", TaggedFilterConfiguration.RULE_TYPE.PRESERVE_WHITESPACE));
		assertFalse(rules.isRuleType("stylesheet",
				TaggedFilterConfiguration.RULE_TYPE.PRESERVE_WHITESPACE));
		assertFalse(rules.isRuleType("p", TaggedFilterConfiguration.RULE_TYPE.PRESERVE_WHITESPACE));
	}

	@Test
	public void langAndXmlLang() {
		URL url = HtmlFilter.class.getResource("nonwellformedConfiguration.yml");
		TaggedFilterConfiguration rules = new TaggedFilterConfiguration(url);

		Map<String, String> attributes = new HashMap<String, String>();
		attributes.clear();
		attributes.put("lang", "en");
		assertTrue(rules.isWritableLocalizableAttribute("x", "lang", attributes));
		assertFalse(rules.isReadOnlyLocalizableAttribute("x", "lang", attributes));
		assertFalse(rules.isTranslatableAttribute("x", "lang", attributes));

		assertTrue(rules.isWritableLocalizableAttribute("p", "lang", attributes));
		assertFalse(rules.isReadOnlyLocalizableAttribute("p", "lang", attributes));
		assertFalse(rules.isTranslatableAttribute("p", "lang", attributes));

		attributes.clear();
		attributes.put("xml:lang", "en");
		assertTrue(rules.isWritableLocalizableAttribute("x", "xml:lang", attributes));
	}

	@Test
	public void genericCodeTypes() {
		URL url = HtmlFilter.class.getResource("nonwellformedConfiguration.yml");
		TaggedFilterConfiguration rules = new TaggedFilterConfiguration(url);

		assertEquals(getElementType(rules, "b"), "bold");
		assertEquals(getElementType(rules, "i"), "italic");
		assertEquals(getElementType(rules, "u"), "underlined");
		assertEquals(getElementType(rules, "img"), "image");
		assertEquals(getElementType(rules, "a"), "link");
		assertEquals(getElementType(rules, "x"), "x");
	}

	@Test
	public void textUnitCodeTypes() {
		URL url = HtmlFilter.class.getResource("wellformedConfiguration.yml");
		TaggedFilterConfiguration rules = new TaggedFilterConfiguration(url);
		assertEquals(getElementType(rules, "p"), "paragraph");
	}

	@Test
	public void collapseWhitespace() {
		URL url = HtmlFilter.class.getResource("nonwellformedConfiguration.yml");
		TaggedFilterConfiguration rules = new TaggedFilterConfiguration(url);
		assertFalse(rules.isGlobalPreserveWhitespace());

		url = location.in("/collapseWhitespaceOff.yml").asUrl();
		rules = new TaggedFilterConfiguration(url);
		assertTrue(rules.isGlobalPreserveWhitespace());
	}

	@Test
	public void testCodeFinderRules() {
		URL url = location.in("/withCodeFinderRules.yml").asUrl();
		TaggedFilterConfiguration rules = new TaggedFilterConfiguration(url);
		assertTrue(rules.isUseCodeFinder());
		InlineCodeFinder cf = new InlineCodeFinder();
		cf.fromString(rules.getCodeFinderRules());
		cf.compile();
		ArrayList<String> list = cf.getRules();
		assertNotNull(list);
		assertEquals(1, list.size());
		assertEquals("\\bVAR\\d\\b", list.get(0));
	}

	@Test
	public void inputAttributes() {
		URL url = HtmlFilter.class.getResource("nonwellformedConfiguration.yml");
		TaggedFilterConfiguration rules = new TaggedFilterConfiguration(url);

		Map<String, String> attributes = new HashMap<String, String>();

		attributes.clear();
		attributes.put("type", "hidden");
		assertFalse(rules.isTranslatableAttribute("input", "alt", attributes));
		assertFalse(rules.isTranslatableAttribute("input", "value", attributes));
		assertFalse(rules.isTranslatableAttribute("input", "accesskey", attributes));
		assertFalse(rules.isTranslatableAttribute("input", "title", attributes));

		attributes.clear();
		attributes.put("type", "image");
		assertFalse(rules.isTranslatableAttribute("input", "alt", attributes));
		assertFalse(rules.isTranslatableAttribute("input", "value", attributes));
		assertFalse(rules.isTranslatableAttribute("input", "accesskey", attributes));
		assertFalse(rules.isTranslatableAttribute("input", "title", attributes));

		attributes.clear();
		attributes.put("type", "submit");
		assertTrue(rules.isTranslatableAttribute("input", "alt", attributes));
		assertTrue(rules.isTranslatableAttribute("input", "value", attributes));
		assertTrue(rules.isTranslatableAttribute("input", "accesskey", attributes));
		assertTrue(rules.isTranslatableAttribute("input", "title", attributes));

		attributes.clear();
		attributes.put("type", "button");
		assertTrue(rules.isTranslatableAttribute("input", "alt", attributes));
		assertTrue(rules.isTranslatableAttribute("input", "value", attributes));
		assertTrue(rules.isTranslatableAttribute("input", "accesskey", attributes));
		assertTrue(rules.isTranslatableAttribute("input", "title", attributes));
	}

	@Test
	public void attributeID() {
		URL url = HtmlFilter.class.getResource("wellformedConfiguration.yml");
		TaggedFilterConfiguration rules = new TaggedFilterConfiguration(url);

		Map<String, String> attributes = new HashMap<String, String>();
		attributes.clear();
		attributes.put("id", "value");
		assertTrue(rules.isIdAttribute("p", "id", attributes));
		assertFalse(rules.isIdAttribute("p", "foo", attributes));
	}

	@SuppressWarnings("unchecked")
	private String getElementType(TaggedFilterConfiguration rules, String elementName) {
		Map<String, Object> rule = rules.getConfigReader().getElementRule(elementName.toLowerCase());
		if (rule != null && rule.containsKey(TaggedFilterConfiguration.ELEMENT_TYPE)) {
			return (String) rule.get(TaggedFilterConfiguration.ELEMENT_TYPE);
		}
		return elementName;
	}
}
