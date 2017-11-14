/*===========================================================================
  Copyright (C) 2009 by the Okapi Framework contributors
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;


@RunWith(JUnit4.class)
public class LocaleFilterTest {

	private LocaleId locENUS = LocaleId.fromString("en-us");
	private LocaleId locENGB = LocaleId.fromString("en-gb");
	private LocaleId locENNZ = LocaleId.fromString("en-nz");
	private LocaleId locESEQ = LocaleId.fromString("es-eq");
	private LocaleId locESUS = LocaleId.fromString("es-us");	
	private LocaleId locFRFR = LocaleId.fromString("fr-fr");
	private LocaleId locFRCA = LocaleId.fromString("fr-ca");
	private LocaleId locFRCH = LocaleId.fromString("fr-ch");
	private LocaleId locFRBE = LocaleId.fromString("fr-be");
	private LocaleId locDEDE = LocaleId.fromString("de-de");
	private LocaleId locDECH = LocaleId.fromString("de-ch");
	private LocaleId locENUS_WIN = new LocaleId("en", "us", "win");
	private LocaleId locDECH_WIN = new LocaleId("de", "ch", "win");
	private LocaleId locDECH_MAC = new LocaleId("de", "ch", "mac");
	
	@Test
	public void testMatches() {
		
		LocaleFilter filter = LocaleFilter.any();
		assertTrue(filter.matches(LocaleId.EMPTY));
		assertTrue(filter.matches(locENUS));
		
		assertFalse(LocaleFilter.anyExcept(locFRCA, locENUS).matches(locFRCA));
		assertFalse(LocaleFilter.anyExcept(locFRCA, locENUS).matches(locENUS));
		assertTrue(LocaleFilter.anyExcept(locFRCA, locENUS).matches(locESUS));
		
		assertTrue(LocaleFilter.anyOf(locFRCA, locENUS).matches(locFRCA));
		assertFalse(LocaleFilter.anyOf(locFRCA, locENUS).matches(locESUS));
		
		assertFalse(LocaleFilter.none().matches(locESUS));
		assertFalse(LocaleFilter.none().matches(locENUS));
		
		filter = LocaleFilter.none().include(locENUS, locDECH_WIN).includeLanguage("fr");
		assertFalse(filter.matches(locDEDE));
		
		filter = LocaleFilter.any().include(locENUS, locDECH_WIN).includeLanguage("fr");
		assertTrue(filter.matches(locDEDE));
	}
	
	@Test
	public void testFilter() {
		
		Set<LocaleId> filtered = LocaleFilter.anyOf(locFRFR, locFRCA, locFRCH).filter(locFRCA, locFRBE, locENUS);
		assertEquals(1, filtered.size());
		assertTrue(filtered.contains(locFRCA));
		assertFalse(filtered.contains(locFRBE));
	}
	
	@Test
	public void testConstructor() {
		
		LocaleFilter filter = new LocaleFilter();
		assertTrue(filter.matches(locENUS));
		assertTrue(filter.matches(locFRCA));
	}
	
	@Test
	public void testInclude() {
		
		LocaleFilter filter = LocaleFilter.none();
		
		assertFalse(filter.matches(locENUS));		
		filter.include(locENUS);
		assertTrue(filter.matches(locENUS));
		
		assertFalse(filter.matches(locFRCA));		
		filter.include(locFRFR, locFRCA, locESUS);
		assertTrue(filter.matches(locFRFR));
		assertFalse(filter.matches(locFRCH));
		
		assertFalse(filter.matches(locFRBE));
		Set<LocaleId> set = new HashSet<LocaleId>();
		set.add(locFRBE);
		filter.include(set);
		assertTrue(filter.matches(locFRBE));
		
		filter.reset();
		
		filter.includePattern("en-.*");
		assertTrue(filter.matches(locENUS));		
		assertTrue(filter.matches(locENGB));
		assertFalse(filter.matches(locFRCA));
		assertFalse(filter.matches(locESUS));
		assertFalse(filter.matches(locESEQ));
		assertEquals(1, filter.getPatternIncludes().size());
		
		filter.includePattern(".*-US");
		assertTrue(filter.matches(locENUS));
		assertTrue(filter.matches(locESUS));
		assertTrue(filter.matches(locENGB)); // From the previous regex
		assertFalse(filter.matches(locESEQ));
		assertEquals(2, filter.getPatternIncludes().size());
		
		filter.reset();
		assertEquals(0, filter.getPatternIncludes().size());
		
		filter.includePattern("e.*-US");
		assertTrue(filter.matches(locENUS));
		assertTrue(filter.matches(locESUS));
		assertFalse(filter.matches(locENGB));
		assertFalse(filter.matches(locESEQ));
		assertFalse(filter.matches(locFRCA));
		assertEquals(1, filter.getPatternIncludes().size());
		
		filter.reset();
		filter.includeLanguage("en");
		assertTrue(filter.matches(locENUS));
		assertFalse(filter.matches(locESUS));
		assertTrue(filter.matches(locENGB));
		assertFalse(filter.matches(locESEQ));
		assertFalse(filter.matches(locFRCA));
		assertEquals(1, filter.getLanguageIncludes().size());
		
		filter.includeLanguage("es", "fr");
		assertTrue(filter.matches(locENUS));
		assertTrue(filter.matches(locESUS));
		assertTrue(filter.matches(locENGB));
		assertTrue(filter.matches(locESEQ));
		assertTrue(filter.matches(locFRCA));
		assertFalse(filter.matches(locDEDE));
		assertFalse(filter.matches(locDECH));
		assertEquals(3, filter.getLanguageIncludes().size());
		
		filter.reset();
		filter.includeRegion("us");
		assertEquals(1, filter.getRegionIncludes().size());
		assertTrue(filter.matches(locENUS));
		assertTrue(filter.matches(locESUS));
		assertFalse(filter.matches(locENGB));
		assertFalse(filter.matches(locESEQ));
		assertFalse(filter.matches(locFRCA));
		
		filter.includeRegion("ch", "gb");
		assertEquals(3, filter.getRegionIncludes().size());
		assertTrue(filter.matches(locFRCH));
		assertTrue(filter.matches(locDECH));
		assertTrue(filter.matches(locENUS)); // From includeRegion("us") 
		assertTrue(filter.matches(locESUS)); // From includeRegion("us")
		assertTrue(filter.matches(locENGB));
		assertFalse(filter.matches(locESEQ));		
		
		filter.reset();
		filter.includeUserPart("ats");
		assertEquals(1, filter.getUserPartIncludes().size());
		// assertTrue(filter.matches(LocaleId.fromPOSIXLocale("de_AT.UTF-8@ATS")));
		assertFalse(filter.matches(LocaleId.fromPOSIXLocale("sr@latin")));
		
		filter.includeUserPart("mac", "latin");
		assertEquals(3, filter.getUserPartIncludes().size());
		assertTrue(filter.matches(LocaleId.fromPOSIXLocale("es_us@mac")));
		// assertTrue(filter.matches(LocaleId.fromPOSIXLocale("sr@latin")));
		assertFalse(filter.matches(LocaleId.fromPOSIXLocale("en_us@win")));
		
		filter.reset();
		LocaleFilter filter2 = LocaleFilter.none();
		filter2.include(locENUS);
		assertFalse(filter.matches(locENUS));
		filter.include(filter2);
		assertTrue(filter.matches(locENUS));
	}
	
	@Test
	public void testExclude() {
		
		LocaleFilter filter = LocaleFilter.any();
		
		assertTrue(filter.matches(locENUS));		
		filter.exclude(locENUS);
		assertFalse(filter.matches(locENUS));
		
		assertTrue(filter.matches(locFRCA));		
		filter.exclude(locFRFR, locFRCA, locESUS);
		assertFalse(filter.matches(locFRFR));
		assertTrue(filter.matches(locFRCH));
		
		assertTrue(filter.matches(locFRBE));
		Set<LocaleId> set = new HashSet<LocaleId>();
		set.add(locFRBE);
		filter.exclude(set);
		assertFalse(filter.matches(locFRBE));
		
		filter.reset();
		
		filter.excludePattern("en-.*");
		assertFalse(filter.matches(locENUS));		
		assertFalse(filter.matches(locENGB));
		assertTrue(filter.matches(locFRCA));
		assertTrue(filter.matches(locESUS));
		assertTrue(filter.matches(locESEQ));
		assertEquals(1, filter.getPatternExcludes().size());
		
		filter.excludePattern(".*-US");
		assertFalse(filter.matches(locENUS));
		assertFalse(filter.matches(locESUS));
		assertFalse(filter.matches(locENGB)); // From the previous regex
		assertTrue(filter.matches(locESEQ));
		assertEquals(2, filter.getPatternExcludes().size());
		
		filter.reset();
		assertEquals(0, filter.getPatternExcludes().size());
		
		filter.excludePattern("e.*-US");
		assertFalse(filter.matches(locENUS));
		assertFalse(filter.matches(locESUS));
		assertTrue(filter.matches(locENGB));
		assertTrue(filter.matches(locESEQ));
		assertTrue(filter.matches(locFRCA));
		assertEquals(1, filter.getPatternExcludes().size());
		
		filter.reset();
		filter.excludeLanguage("en");
		assertFalse(filter.matches(locENUS));
		assertTrue(filter.matches(locESUS));
		assertFalse(filter.matches(locENGB));
		assertTrue(filter.matches(locESEQ));
		assertTrue(filter.matches(locFRCA));
		assertEquals(1, filter.getLanguageExcludes().size());
		
		filter.excludeLanguage("es", "fr");
		assertFalse(filter.matches(locENUS));
		assertFalse(filter.matches(locESUS));
		assertFalse(filter.matches(locENGB));
		assertFalse(filter.matches(locESEQ));
		assertFalse(filter.matches(locFRCA));
		assertTrue(filter.matches(locDEDE));
		assertTrue(filter.matches(locDECH));
		assertEquals(3, filter.getLanguageExcludes().size());
		
		filter.reset();
		filter.excludeRegion("us");
		assertEquals(1, filter.getRegionExcludes().size());
		assertFalse(filter.matches(locENUS));
		assertFalse(filter.matches(locESUS));
		assertTrue(filter.matches(locENGB));
		assertTrue(filter.matches(locESEQ));
		assertTrue(filter.matches(locFRCA));
		
		filter.excludeRegion("ch", "gb");
		assertEquals(3, filter.getRegionExcludes().size());
		assertFalse(filter.matches(locFRCH));
		assertFalse(filter.matches(locDECH));
		assertFalse(filter.matches(locENUS)); // From excludeRegion("us") 
		assertFalse(filter.matches(locESUS)); // From excludeRegion("us")
		assertFalse(filter.matches(locENGB));
		assertTrue(filter.matches(locESEQ));		
		
		filter.reset();
		filter.excludeUserPart("ats");
		assertEquals(1, filter.getUserPartExcludes().size());
		// assertFalse(filter.matches(LocaleId.fromPOSIXLocale("de_AT.UTF-8@ATS")));
		assertTrue(filter.matches(LocaleId.fromPOSIXLocale("sr@latin")));
		
		filter.excludeUserPart("mac", "latin");
		assertEquals(3, filter.getUserPartExcludes().size());
		assertFalse(filter.matches(LocaleId.fromPOSIXLocale("es_us@mac")));
		// assertFalse(filter.matches(LocaleId.fromPOSIXLocale("sr@latin")));
		assertTrue(filter.matches(LocaleId.fromPOSIXLocale("en_us@win")));
		
		filter.reset();
		LocaleFilter filter2 = LocaleFilter.none();
		filter2.include(locENUS);
		assertTrue(filter.matches(locENUS));
		filter.exclude(filter2);
		assertFalse(filter.matches(locENUS));
	}
	
	@Test
	public void testFromString() {
		
		LocaleFilter filter = new LocaleFilter();
		//filter.fromString("item1, @item{2,5}+, !item3");
		
		// Masks
		filter.fromString("*");
		assertTrue(filter.matches(locENUS));
		
		filter.fromString("!*");
		assertFalse(filter.matches(locENUS));
		
		filter.fromString("en");
		assertTrue(filter.matches(locENUS));
		assertTrue(filter.matches(locENGB));
		assertFalse(filter.matches(locESUS));
		
		filter.fromString("*-*");
		assertTrue(filter.matches(locENUS));
		
		filter.fromString("!*-*");
		assertFalse(filter.matches(locENUS));
		
		filter.fromString("*-us");
		assertTrue(filter.matches(locENUS));
		assertTrue(filter.matches(locESUS));
		assertFalse(filter.matches(locENGB));
		
		filter.fromString("en-*");
		assertTrue(filter.matches(locENUS));
		assertTrue(filter.matches(locENGB));
		assertFalse(filter.matches(locESUS));		
		
		filter.fromString("en-us");
		assertTrue(filter.matches(locENUS));
		assertFalse(filter.matches(locENGB));
		
		filter.fromString("*-*-*");
		assertTrue(filter.matches(locENUS));
		assertTrue(filter.matches(locDECH_WIN));
		assertTrue(filter.matches(locDECH_MAC));
		
		filter.fromString("!*-*-*");
		assertFalse(filter.matches(locENUS));
		
		filter.fromString("*-*-win");
		assertTrue(filter.matches(locDECH_WIN));
		assertTrue(filter.matches(locENUS_WIN));
		assertFalse(filter.matches(locDECH_MAC));
		
		filter.fromString("*-ch-*");
		assertTrue(filter.matches(locDECH_WIN));
		assertTrue(filter.matches(locDECH_MAC));
		assertFalse(filter.matches(locENUS_WIN));
		
		filter.fromString("*-CH-win");
		assertTrue(filter.matches(locDECH_WIN));
		assertFalse(filter.matches(locDECH_MAC));
		
		filter.fromString("de-*-*");
		assertTrue(filter.matches(locDECH_WIN));
		assertTrue(filter.matches(locDECH_MAC));
		assertFalse(filter.matches(locENUS_WIN));
		
		filter.fromString("de-*-win");
		assertTrue(filter.matches(locDECH_WIN));
		assertFalse(filter.matches(locDECH_MAC));
		assertFalse(filter.matches(locENUS_WIN));
		
		filter.fromString("de-CH-*");
		assertTrue(filter.matches(locDECH_WIN));
		assertTrue(filter.matches(locDECH_MAC));
		assertFalse(filter.matches(locENUS_WIN));
		
		filter.fromString("de-ch-win");
		assertTrue(filter.matches(locDECH_WIN));
		assertFalse(filter.matches(locDECH_MAC));
		assertFalse(filter.matches(locENUS_WIN));
		
		// Chains
		filter.fromString("en !en-nz");
		assertTrue(filter.matches(locENUS));
		assertTrue(filter.matches(locENGB));
		assertTrue(filter.matches(locENUS_WIN));
		assertFalse(filter.matches(locENNZ));
		assertFalse(filter.matches(locDECH));
		
		filter.fromString("!en-nz");
		assertTrue(filter.matches(locENUS));
		assertTrue(filter.matches(locENGB));
		assertTrue(filter.matches(locENUS_WIN));
		assertFalse(filter.matches(locENNZ));
		assertTrue(filter.matches(locDECH));
		assertTrue(filter.matches(locDEDE));
		assertTrue(filter.matches(locDECH_WIN));
		
		filter.fromString("en !en-nz en-nz");
		assertTrue(filter.matches(locENUS));
		assertTrue(filter.matches(locENGB));
		assertTrue(filter.matches(locENUS_WIN));
		assertTrue(filter.matches(locENNZ));
		assertFalse(filter.matches(locDECH));
		assertFalse(filter.matches(locDEDE));
		assertFalse(filter.matches(locDECH_WIN));
		
		filter.fromString("!en-nz en-nz");
		assertTrue(filter.matches(locENUS));
		assertTrue(filter.matches(locENGB));
		assertTrue(filter.matches(locENUS_WIN));
		assertTrue(filter.matches(locENNZ));
		assertTrue(filter.matches(locDECH));
		assertTrue(filter.matches(locDEDE));
		assertTrue(filter.matches(locDECH_WIN));
		
		// Regex
		filter.fromString("@e[ns]-.+");
		assertTrue(filter.matches(locENUS));
		assertTrue(filter.matches(locENGB));
		assertTrue(filter.matches(locENUS_WIN));
		assertTrue(filter.matches(locESUS));
		assertTrue(filter.matches(locENNZ));
		assertFalse(filter.matches(locDECH));
		assertFalse(filter.matches(locDEDE));
		assertFalse(filter.matches(locDECH_WIN));
		
		filter.fromString("!@e[ns]-.+");
		assertFalse(filter.matches(locENUS));
		assertFalse(filter.matches(locENGB));
		assertFalse(filter.matches(locENUS_WIN));
		assertFalse(filter.matches(locESUS));
		assertFalse(filter.matches(locENNZ));
		assertTrue(filter.matches(locDECH));
		assertTrue(filter.matches(locDEDE));
		assertTrue(filter.matches(locDECH_WIN));
		
		filter.fromString("@en-.+ @es-.+");
		assertTrue(filter.matches(locENUS));
		assertTrue(filter.matches(locENGB));
		assertTrue(filter.matches(locENUS_WIN));
		assertTrue(filter.matches(locESUS));
		assertTrue(filter.matches(locENNZ));
		assertFalse(filter.matches(locDECH));
		assertFalse(filter.matches(locDEDE));
		assertFalse(filter.matches(locDECH_WIN));
		
		filter.fromString("!@en-.+ !@es-.+");
		assertFalse(filter.matches(locENUS));
		assertFalse(filter.matches(locENGB));
		assertFalse(filter.matches(locENUS_WIN));
		assertFalse(filter.matches(locESUS));
		assertFalse(filter.matches(locENNZ));
		assertTrue(filter.matches(locDECH));
		assertTrue(filter.matches(locDEDE));
		assertTrue(filter.matches(locDECH_WIN));
	}
	
	@Test
	public void testToString() {
		
		assertEquals("", LocaleFilter.any().toString());
		assertEquals("!*", LocaleFilter.none().toString());
		assertEquals("* en-US", LocaleFilter.any().include(locENUS).toString());
		
		assertEquals("!en-US !de-CH-win", LocaleFilter.anyExcept(locENUS, locDECH_WIN).toString());
		assertEquals("en-US de-CH-win", LocaleFilter.anyOf(locENUS, locDECH_WIN).toString());
		assertEquals("en-US de-CH-win", LocaleFilter.build("en-us !de-ch-win de-ch-win").toString());
		assertEquals("en-US de-CH-win !de-CH", LocaleFilter.build("en-us !de-ch de-ch-win").toString());
		
		assertEquals("en-US de-CH-win fr", LocaleFilter.none().include(locENUS, locDECH_WIN).includeLanguage("fr").toString());
		assertEquals("* en-US de-CH-win fr", LocaleFilter.any().include(locENUS, locDECH_WIN).includeLanguage("fr").toString());
		assertEquals("!en-US !de-CH-win fr", LocaleFilter.any().exclude(locENUS, locDECH_WIN).includeLanguage("fr").toString());
		assertEquals("fr !en-US !de-CH-win", LocaleFilter.none().exclude(locENUS, locDECH_WIN).includeLanguage("fr").toString());
		assertEquals("!* !en-US !de-CH-win", LocaleFilter.none().exclude(locENUS, locDECH_WIN).toString());
		
		assertEquals("@pattern", LocaleFilter.none().includePattern("pattern").toString());
		assertEquals("@pattern ^8", LocaleFilter.none().includePattern("pattern", 8).toString());
		assertEquals("!@pattern", LocaleFilter.any().excludePattern("pattern").toString());
		assertEquals("!@pattern ^8", LocaleFilter.any().excludePattern("pattern", 8).toString());
		assertEquals("!@pattern1 ^8 !@pattern2 !@pattern3 ^2", LocaleFilter.any().excludePattern("pattern1", 8).excludePattern("pattern2").excludePattern("pattern3", 2).toString());
	}
	
	@Test
	public void testIncludeAfterExclude() {
		
		LocaleFilter filter = new LocaleFilter();
		
		assertTrue(filter.matches(locENUS));
		filter.exclude(locENUS);
		assertFalse(filter.matches(locENUS));
		filter.include(locENUS);
		assertTrue(filter.matches(locENUS));
		
		filter.excludeLanguage("en");
		assertFalse(filter.matches(locENUS));
		filter.includeLanguage("en");
		assertTrue(filter.matches(locENUS));
		
		filter.excludeRegion("us");
		assertFalse(filter.matches(locENUS));
		filter.includeRegion("us");
		assertTrue(filter.matches(locENUS));
		
		filter.excludeUserPart("mac");
		assertFalse(filter.matches(LocaleId.fromPOSIXLocale("es_us@mac")));
		filter.includeUserPart("mac");
		assertTrue(filter.matches(LocaleId.fromPOSIXLocale("es_us@mac")));
		
		filter.excludePattern("en-.*");
		assertFalse(filter.matches(locENUS));
		filter.includePattern("en-.*");
		assertTrue(filter.matches(locENUS));
	}
	
}
