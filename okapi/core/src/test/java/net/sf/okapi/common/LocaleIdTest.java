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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.ibm.icu.util.ULocale;

@RunWith(JUnit4.class)
public class LocaleIdTest {

	@Test
	public void testIdentity () {
		LocaleId locId1 = LocaleId.ENGLISH;
		LocaleId locId2 = new LocaleId("en", true);
		assertEquals(0, locId1.compareTo(locId2));
		assertEquals(locId1.hashCode(), locId2.hashCode());
	}

	@Test
	public void testSerializeEmptyLocale () {
		String s = LocaleId.EMPTY.toString();
		assertEquals(LocaleId.EMPTY, LocaleId.fromString(s));
	}

	@Test
	public void testConstructorFromIdentifier () {
		LocaleId locId = new LocaleId("en-CA", true);
		assertEquals("en", locId.getLanguage());
		assertEquals("CA", locId.getRegion());

		locId = new LocaleId("EN_CA", true);
		assertEquals("en", locId.getLanguage());
		assertEquals("CA", locId.getRegion());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorFromNullIdentifier () {
		new LocaleId((String)null, true);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorFromBadLanguageCode () {
		new LocaleId("foo_bar");
	}

	@Test
	public void testConstructorFromEmptyIdentifier () {
		new LocaleId("", true);
	}

	@Test
	public void testConstructorFromBadXIdentifier () {
		new LocaleId("z-test", true);
	}

	@Test
	public void testConstructorFromGoodXIdentifier () {
		new LocaleId("x-custom", true);
	}

	@Test
	public void testTMXAll () {
		LocaleId locId = new LocaleId("*all*", false);
		assertEquals("*all*", locId.toString());
		assertEquals("*all*", locId.toBCP47());
	}

	@Test
	public void testEmptyLocale () {
		LocaleId locId = new LocaleId("", false);
		assertEquals("", locId.toString());
		assertEquals("und", locId.toBCP47());
	}

	@Test //(expected = IllegalArgumentException.class)
	public void testConstructorFromBadIdentifier () {
		// Try without normalization
		LocaleId locId = new LocaleId("EN_CA", false);
		assertEquals("en", locId.getLanguage());
		assertEquals("CA", locId.getRegion());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorFromNullLanguage () {
		new LocaleId((String)null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorFromEmptyLanguage () {
		new LocaleId("");
	}

	@Test
	public void testConstructorFromLanguage () {
		LocaleId locId = new LocaleId("en");
		assertEquals("en", locId.getLanguage());
		assertNull(locId.getRegion());

		locId = new LocaleId("EN");
		assertEquals("en", locId.getLanguage());
		assertNull(locId.getRegion());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorFromNullLanguageAndRegion () {
		new LocaleId((String)null, "CA");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorFromEmptyLanguageAndRegion () {
		new LocaleId("", "CA");
	}

	@Test
	public void testConstructorFromLanguageAndRegion () {
		LocaleId locId = new LocaleId("de", "CH");
		assertEquals("de", locId.getLanguage());
		assertEquals("CH", locId.getRegion());

		locId = new LocaleId("DE", null);
		assertEquals("de", locId.getLanguage());
		assertNull(locId.getRegion());

		locId = new LocaleId("DE", "");
		assertEquals("de", locId.getLanguage());
		assertNull(locId.getRegion());
	}

	@Test
	public void testConstructorFromLanguageRegionUserPart () {
		LocaleId locId = new LocaleId("de", "CH", "win");
		assertEquals("de", locId.getLanguage());
		assertEquals("CH", locId.getRegion());
		assertEquals("win", locId.getUserPart());
		assertEquals("de-CH-x-win", locId.toString());

		locId = new LocaleId("de", "CH", "WIN");
		assertEquals("de", locId.getLanguage());
		assertEquals("CH", locId.getRegion());
		assertEquals("win", locId.getUserPart());
		assertEquals("de-CH-x-win", locId.toString());

		locId = new LocaleId("DE", null, null);
		assertEquals("de", locId.getLanguage());
		assertNull(locId.getRegion());
		assertNull(locId.getUserPart());

		locId = new LocaleId("DE", null, "win");
		assertEquals("de", locId.getLanguage());
		assertNull(locId.getRegion());
		assertEquals("win", locId.getUserPart());

		locId = new LocaleId("DE", "", "");
		assertEquals("de", locId.getLanguage());
		assertNull(locId.getRegion());
		assertNull(locId.getUserPart());

		locId = new LocaleId("DE", "", "win");
		assertEquals("de", locId.getLanguage());
		assertNull(locId.getRegion());
		assertEquals("win", locId.getUserPart());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorFromNullJavaLocale () {
		new LocaleId((Locale)null);
	}

	@Test
	public void testConstructorFromJavaLocale () {
		LocaleId locId = new LocaleId(Locale.CANADA_FRENCH);
		assertEquals("fr", locId.getLanguage());
		assertEquals("CA", locId.getRegion());

		locId = new LocaleId(Locale.SIMPLIFIED_CHINESE);
		assertEquals("zh", locId.getLanguage());
		assertEquals("CN", locId.getRegion());

		locId = new LocaleId(Locale.CHINESE);
		assertEquals("zh", locId.getLanguage());
		assertNull(locId.getRegion());

		locId = new LocaleId(Locale.TRADITIONAL_CHINESE);
		assertEquals("zh", locId.getLanguage());
		assertEquals("TW", locId.getRegion());

		// Java pre-defined th_TH_TH
		locId = new LocaleId(new Locale("th", "TH", "TH"));
		assertEquals("th", locId.getLanguage());
		assertEquals("TH", locId.getRegion());
	}

	@Test
	public void testFromBCP () {
		LocaleId locId = LocaleId.fromBCP47("en-us");
		assertEquals("en", locId.getLanguage());
		assertEquals("US", locId.getRegion());

		locId = LocaleId.fromBCP47("kok");
		assertEquals("kok", locId.getLanguage());

		locId = LocaleId.fromBCP47("ar-Latn-EG");
		assertEquals("ar", locId.getLanguage());
		assertEquals("EG", locId.getRegion());

		locId = LocaleId.fromBCP47("az-latn");
		assertEquals("az", locId.getLanguage());
		assertNull(locId.getRegion());

		locId = LocaleId.fromBCP47("zh-Hant-TW");
		assertEquals("zh", locId.getLanguage());
		assertEquals("TW", locId.getRegion());

		locId = LocaleId.fromBCP47("zh-Latn-TW-pinyin");
		assertEquals("zh", locId.getLanguage());
		assertEquals("TW", locId.getRegion());

		locId = LocaleId.fromBCP47("es-419");
		assertEquals("es", locId.getLanguage());
		assertEquals("419", locId.getRegion());

		locId = LocaleId.fromBCP47("de-CH-1996");
		assertEquals("de", locId.getLanguage());
		assertEquals("CH", locId.getRegion());

		locId = LocaleId.fromBCP47("ja-Latn-hepburn");
		assertEquals("ja", locId.getLanguage());
	}

	@Test
	public void testFromBCPNotStrictDefault() {
		assertEquals("und", LocaleId.fromBCP47("").toBCP47());
		assertEquals("und", LocaleId.fromBCP47("es_US").toBCP47());
		assertEquals("und", LocaleId.fromBCP47("qwertyuiop").toBCP47());
		assertEquals("und", LocaleId.fromBCP47("qwertyuiop-Latn").toBCP47());
		assertEquals("und", LocaleId.fromBCP47("qwertyuiop-CH").toBCP47());
		assertEquals("fr", LocaleId.fromBCP47("fr-qwertyuiop").toBCP47());
		assertEquals("fr", LocaleId.fromBCP47("fr-qwertyuiop-asdfghjkl").toBCP47());
	}

	@Test
	public void testFromBCPNotStrict() {
		assertEquals("und", LocaleId.fromBCP47("", false).toBCP47());
		assertEquals("und", LocaleId.fromBCP47("es_US", false).toBCP47());
		assertEquals("und", LocaleId.fromBCP47("qwertyuiop", false).toBCP47());
		assertEquals("und", LocaleId.fromBCP47("qwertyuiop-Latn", false).toBCP47());
		assertEquals("und", LocaleId.fromBCP47("qwertyuiop-CH", false).toBCP47());
		assertEquals("fr", LocaleId.fromBCP47("fr-qwertyuiop", false).toBCP47());
		assertEquals("fr", LocaleId.fromBCP47("fr-qwertyuiop-asdfghjkl", false).toBCP47());
	}

	@Test(expected=IllegalArgumentException.class)
	public void testFromBCPStrictEmpty() {
		LocaleId.fromBCP47("", true);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testFromBCPStrictUnderscore() {
		LocaleId.fromBCP47("es_US", true);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testFromBCPStrictLongLang() {
		LocaleId.fromBCP47("qwertyuiop", true);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testFromBCPStrictLongLangAndScript() {
		LocaleId.fromBCP47("qwertyuiop-Latn", true);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testFromBCPStrictLongLangAndRegion() {
		LocaleId.fromBCP47("qwertyuiop-CH", true);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testFromBCPStrictLongSecondPart() {
		LocaleId.fromBCP47("fr-qwertyuiop", true);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testFromBCPStrictLongSecondAndThirdParts() {
		LocaleId.fromBCP47("fr-qwertyuiop-asdfghjkl", true);
	}

	@Test
	public void testFromPOSIX () {
		LocaleId locId = LocaleId.fromPOSIXLocale("zu");
		assertEquals("zu", locId.getLanguage());

		locId = LocaleId.fromPOSIXLocale("kok");
		assertEquals("kok", locId.getLanguage());

		locId = LocaleId.fromPOSIXLocale("de_AT");
		assertEquals("de", locId.getLanguage());
		assertEquals("AT", locId.getRegion());
		assertNull(locId.getUserPart());

		locId = LocaleId.fromPOSIXLocale("de_AT.UTF-8");
		assertEquals("de", locId.getLanguage());
		assertEquals("AT", locId.getRegion());
		assertNull(locId.getUserPart());

		locId = LocaleId.fromPOSIXLocale("de_AT.UTF-8@ATS");
		assertEquals("de", locId.getLanguage());
		assertEquals("AT", locId.getRegion());
		assertNull(locId.getUserPart());

		locId = LocaleId.fromPOSIXLocale("sr@latin");
		assertEquals("sr", locId.getLanguage());
		assertEquals("Latn", locId.getScript());
		assertNull(locId.getUserPart());
	}

	@Test
	public void testToPOSIX () {
		LocaleId locId = LocaleId.fromPOSIXLocale("en_US.UTF-8");
		String res = locId.toPOSIXLocaleId();
		assertNotNull(res);
		assertEquals("en_US", res);

		locId = LocaleId.fromPOSIXLocale("DE");
		res = locId.toPOSIXLocaleId();
		assertNotNull(res);
		assertEquals("de", res);

//		locId = LocaleID.fromPOSIXLocale("ca@valencia");
//		res = LocaleID.toPOSIXLocale(locId);
//		assertNotNull(res);
//		assertEquals("ca@valencia", res);
	}

	@Test
	public void testToJavaLocale () {
		LocaleId locId = new LocaleId(Locale.CANADA_FRENCH);
		Locale loc = locId.toJavaLocale();
		assertNotNull(loc);
		assertEquals(Locale.CANADA_FRENCH.toString(), loc.toString());

		Locale jloc = new Locale("th", "TH", "TH");
		locId = new LocaleId(jloc);
		loc = locId.toJavaLocale();
		assertNotNull(loc);
		assertEquals(jloc.toString(), loc.toString());

		locId = new LocaleId(Locale.CHINESE);
		loc = locId.toJavaLocale();
		assertNotNull(loc);
		assertEquals(Locale.CHINESE.toString(), loc.toString());
	}

	@Test
	public void testEqualsWithLocaleId () {
		LocaleId locId1 = new LocaleId("fi-fi", false);
		LocaleId locId2 = new LocaleId("fi-se", false);
		assertFalse(locId1.equals(locId2));

		locId1 = new LocaleId("kok-abc", false);
		locId2 = new LocaleId("KOK_aBc", true);
		assertTrue(locId1.equals(locId2));

		locId1 = new LocaleId("br");
		locId2 = new LocaleId("br");
		assertTrue(locId1.equals(locId2));
	}

	@Test
	public void testEqualsWithString () {
		LocaleId locId1 = new LocaleId("fi-fi", false);
		assertFalse(locId1.equals("fi-se"));

		locId1 = new LocaleId("kok-abc", false);
		assertTrue(locId1.equals("KOK-aBc"));

		locId1 = new LocaleId("br");
		assertTrue(locId1.equals("BR"));
	}

	@Test
	public void testUsage () {
		assertEquals("Austria", new LocaleId("de-at", false).toJavaLocale().getDisplayCountry(Locale.ENGLISH));
		assertEquals("French", new LocaleId("fr-ca", false).toJavaLocale().getDisplayLanguage(Locale.ENGLISH));
	}

	@Test
	public void testSameLanguageWithLocaleId () {
		LocaleId locId1 = new LocaleId("fi-fi", false);
		LocaleId locId2 = new LocaleId("fi-se", false);
		assertTrue(locId1.sameLanguageAs(locId2));

		locId1 = new LocaleId("kok", false);
		locId2 = new LocaleId("KOK_id", true);
		assertTrue(locId1.sameLanguageAs(locId2));

		locId1 = new LocaleId("br");
		locId2 = new LocaleId("br");
		assertTrue(locId1.sameLanguageAs(locId2));
	}

	@Test
	public void testSameLanguageWithString () {
		LocaleId locId = new LocaleId("fi-fi", false);
		assertTrue(locId.sameLanguageAs("fi-se"));

		locId = new LocaleId("kok", false);
		assertTrue(locId.sameLanguageAs("KoK_id"));

		locId = new LocaleId("br");
		assertTrue(locId.sameLanguageAs("br"));
	}

	@Test
	public void testDifferentLanguages () {
		LocaleId locId1 = new LocaleId("fi-fi", false);
		LocaleId locId2 = new LocaleId("sv-fi", true);
		assertFalse(locId1.sameLanguageAs(locId2));

		locId1 = new LocaleId("nn", false);
		assertFalse(locId1.sameLanguageAs("no"));
	}

	@Test
	public void testSameRegionWithLocaleId () {
		LocaleId locId1 = new LocaleId("fi-fi", false);
		LocaleId locId2 = new LocaleId("sv-fi", false);
		assertTrue(locId1.sameRegionAs(locId2));

		locId1 = new LocaleId("fi-fi", false);
		locId2 = new LocaleId("sv_FI", true);
		assertTrue(locId1.sameRegionAs(locId2));

		locId1 = new LocaleId("fi-fi", false);
		locId2 = new LocaleId("sv_FI", false);
		assertTrue(locId1.sameRegionAs(locId2));
	}

	@Test
	public void testSameRegionWithString () {
		LocaleId locId = new LocaleId("fi-fi", false);
		assertTrue(locId.sameRegionAs("sv-fi"));

		locId = new LocaleId("fi-fi", false);
		assertTrue(locId.sameRegionAs("sv_FI"));

		locId = new LocaleId("sv_FI", false);
		assertTrue(locId.sameRegionAs("fi-fi"));
	}

	@Test
	public void testDifferentRegions () {
		LocaleId locId1 = new LocaleId("sv-se", false);
		LocaleId locId2 = new LocaleId("sv-fi", true);
		assertFalse(locId1.sameRegionAs(locId2));
	}

	@Test
	public void testSameUserPartWithLocaleId () {
		// No user parts
		LocaleId locId1 = new LocaleId("fi-fi", false);
		LocaleId locId2 = new LocaleId("sv-fi", false);
		assertTrue(locId1.sameUserPartAs(locId2));

		locId1 = new LocaleId("fi-fi", false);
		locId2 = new LocaleId("sv_FI", true);
		assertTrue(locId1.sameUserPartAs(locId2));

		locId1 = new LocaleId("fi-fi", false);
		locId2 = new LocaleId("sv_FI", false);
		assertTrue(locId1.sameUserPartAs(locId2));

		// Same user parts
		locId1 = new LocaleId("es-us-x-win", false);
		locId2 = LocaleId.fromPOSIXLocale("en_us@win");
		assertTrue(locId1.sameUserPartAs(locId2));

		// Different user parts
		locId1 = LocaleId.fromPOSIXLocale("es_us@mac");
		locId2 = LocaleId.fromPOSIXLocale("en_us@win");
		assertFalse(locId1.sameUserPartAs(locId2));
	}

	@Test
	public void testSameUserPartWithString () {
		// No user parts
		LocaleId locId = new LocaleId("fi-fi", false);
		assertTrue(locId.sameUserPartAs("sv-fi"));

		locId = new LocaleId("fi-fi", false);
		assertTrue(locId.sameUserPartAs("sv_FI"));

		locId = new LocaleId("sv_FI", false);
		assertTrue(locId.sameUserPartAs("fi-fi"));

		// Same user parts
		locId = new LocaleId("es-us-x-win", false);
		assertTrue(locId.sameUserPartAs("en-x-win"));

		// Different user parts
		locId = LocaleId.fromPOSIXLocale("es_us@mac");
		assertFalse(locId.sameUserPartAs("es_us-x-win"));
	}

	@Test
	public void testDifferentUserParts () {
		// No user parts
		LocaleId locId1 = new LocaleId("sv-se", false);
		LocaleId locId2 = new LocaleId("sv-fi", true);
		assertTrue(locId1.sameUserPartAs(locId2));

		// Different user parts
		locId1 = new LocaleId("es-us-x-win", false);
		locId2 = new LocaleId("es-us-x-mac", false);
		assertFalse(locId1.sameUserPartAs(locId2));
	}

	@Test
	public void testSplitLanguageCode () {
		String in = "en";
		String[] res = LocaleId.splitLanguageCode(in);
		assertEquals("en", res[0]);
		assertEquals("", res[1]);
	}

	@Test
	public void testSplitLanguageCode_4Letters () {
		String in = "en-BZ";
		String[] res = LocaleId.splitLanguageCode(in);
		assertEquals("en", res[0]);
		assertEquals("BZ", res[1]);
	}

	@Test
	public void testSplitLanguageCode_Underline () {
		String in = "en_BZ";
		String[] res = LocaleId.splitLanguageCode(in);
		assertEquals("en", res[0]);
		assertEquals("BZ", res[1]);
	}

	@Test
	public void testRegionAndUserPart () {
		LocaleId locId = new LocaleId("ja-jp-x-calja", true);
		assertEquals("ja", locId.getLanguage());
		assertEquals("JP", locId.getRegion());
		assertEquals("calja", locId.getUserPart());

		locId = new LocaleId("th-th-x-numth", true);
		assertEquals("th", locId.getLanguage());
		assertEquals("TH", locId.getRegion());
		assertEquals("numth", locId.getUserPart());

		locId = new LocaleId("ar-Latn-EG", true);
		assertEquals("EG", locId.getRegion());
		assertNull(locId.getUserPart());

		locId = new LocaleId("zh-Hant-TW", true);
		assertEquals("TW", locId.getRegion());
		assertNull(locId.getUserPart());

		locId = new LocaleId("zh-Latn-TW-pinyin", true);
		assertEquals("TW", locId.getRegion());
		assertNull(locId.getUserPart());

		locId = new LocaleId("de-CH-1996", true);
		assertEquals("CH", locId.getRegion());
		assertNull(locId.getUserPart());

		locId = new LocaleId("ja-Latn-hepburn", true);
		assertNull(locId.getRegion());
		assertNull(locId.getUserPart());
	}

	@Test
	public void testIsBidirectional () {
		// True
		assertTrue(LocaleId.isBidirectional(LocaleId.fromBCP47("ar")));
		assertTrue(LocaleId.isBidirectional(LocaleId.fromBCP47("he")));
		assertTrue(LocaleId.isBidirectional(LocaleId.fromBCP47("ar-SA")));
		assertTrue(LocaleId.isBidirectional(LocaleId.fromBCP47("ur-pk")));
		assertTrue(LocaleId.isBidirectional(LocaleId.fromBCP47("syc")));
		assertTrue(LocaleId.isBidirectional(LocaleId.fromBCP47("dv")));
		assertTrue(LocaleId.isBidirectional(LocaleId.fromPOSIXLocale("ar_EG")));
		// False
		assertFalse(LocaleId.isBidirectional(LocaleId.fromBCP47("en-ar")));
		assertFalse(LocaleId.isBidirectional(LocaleId.fromBCP47("arn")));
		assertFalse(LocaleId.isBidirectional(LocaleId.fromBCP47("tr")));
	}

	@Test
	public void testVariablesWithString () {
		String srcLoc = new LocaleId("de", "CH").toString();
		String trgLoc = new LocaleId("en", "IE").toString();
		assertEquals("", LocaleId.replaceVariables("", srcLoc, trgLoc));
		assertEquals("de-CH,en-IE", LocaleId.replaceVariables("${srcLang},${trgLang}", srcLoc, trgLoc));
		assertEquals("DE-CH,EN-IE", LocaleId.replaceVariables("${srcLangU},${trgLangU}", srcLoc, trgLoc));
		assertEquals("de-ch,en-ie", LocaleId.replaceVariables("${srcLangL},${trgLangL}", srcLoc, trgLoc));
		assertEquals("de_CH,en_IE", LocaleId.replaceVariables("${srcLoc},${trgLoc}", srcLoc, trgLoc));
		assertEquals("de,en", LocaleId.replaceVariables("${srcLocLang},${trgLocLang}", srcLoc, trgLoc));
		assertEquals("CH,IE", LocaleId.replaceVariables("${srcLocReg},${trgLocReg}", srcLoc, trgLoc));
		// With null
		assertEquals("CH,", LocaleId.replaceVariables("${srcLocReg},${trgLocReg}", srcLoc, null));
		assertEquals(",en-IE", LocaleId.replaceVariables("${srcLang},${trgLang}", null, trgLoc));
	}

	@Test
	public void testVariablesWithLocaleId () {
		LocaleId srcLoc = new LocaleId("de", "CH");
		LocaleId trgLoc = new LocaleId("en", "IE");
		assertEquals("", LocaleId.replaceVariables("", srcLoc, trgLoc));
		assertEquals("de-CH,en-IE", LocaleId.replaceVariables("${srcLang},${trgLang}", srcLoc, trgLoc));
		assertEquals("DE-CH,EN-IE", LocaleId.replaceVariables("${srcLangU},${trgLangU}", srcLoc, trgLoc));
		assertEquals("de-ch,en-ie", LocaleId.replaceVariables("${srcLangL},${trgLangL}", srcLoc, trgLoc));
		assertEquals("de_CH,en_IE", LocaleId.replaceVariables("${srcLoc},${trgLoc}", srcLoc, trgLoc));
		assertEquals("de,en", LocaleId.replaceVariables("${srcLocLang},${trgLocLang}", srcLoc, trgLoc));
		assertEquals("CH,IE", LocaleId.replaceVariables("${srcLocReg},${trgLocReg}", srcLoc, trgLoc));
		// With null
		assertEquals("CH,", LocaleId.replaceVariables("${srcLocReg},${trgLocReg}", srcLoc, null));
		assertEquals(",en-IE", LocaleId.replaceVariables("${srcLang},${trgLang}", null, trgLoc));
	}

	@Test
	public void testVariablesWithAdvancedLocaleId () {
		final LocaleId srcLoc = LocaleId.fromString("de-CH-1996-u-co-phonebook");
		final LocaleId trgLoc = LocaleId.fromBCP47("zh-Hans-CN-u-co-stroke");

		assertEquals("", LocaleId.replaceVariables("", srcLoc, trgLoc));

		assertEquals("de-CH-1996,zh-Hans-CN", LocaleId.replaceVariables("${srcLang},${trgLang}", srcLoc, trgLoc));
		assertEquals("DE-CH-1996,ZH-HANS-CN", LocaleId.replaceVariables("${srcLangU},${trgLangU}", srcLoc, trgLoc));
		assertEquals("de-ch-1996,zh-hans-cn", LocaleId.replaceVariables("${srcLangL},${trgLangL}", srcLoc, trgLoc));
		assertEquals("de_CH_1996,zh_Hans_CN", LocaleId.replaceVariables("${srcLoc},${trgLoc}", srcLoc, trgLoc));

		assertEquals("de,zh", LocaleId.replaceVariables("${srcLocLang},${trgLocLang}", srcLoc, trgLoc));
		assertEquals("CH,CN", LocaleId.replaceVariables("${srcLocReg},${trgLocReg}", srcLoc, trgLoc));
		assertEquals(",Hans", LocaleId.replaceVariables("${srcLocScript},${trgLocScript}", srcLoc, trgLoc));
		assertEquals("1996,", LocaleId.replaceVariables("${srcLocVariant},${trgLocVariant}", srcLoc, trgLoc));
	}

	@Test
	public void testGetOriginalLocId () {
		LocaleId loc;

		// As expected
		loc = new LocaleId("dE-dE", false);
		assertEquals("dE-dE", loc.getOriginalLocId());

		loc = new LocaleId("dE-dE", true);
		assertEquals("dE-dE", loc.getOriginalLocId());

		loc = LocaleId.fromString("dE-dE");
		assertEquals("dE-dE", loc.getOriginalLocId());

		// Returning null
		loc = new LocaleId("de");
		assertNull(loc.getOriginalLocId());

		loc = new LocaleId("de", "DE");
		assertNull(loc.getOriginalLocId());

		loc = new LocaleId("tH", "tH", "tH");
		assertNull(loc.getOriginalLocId());

		loc = new LocaleId(Locale.FRANCE);
		assertNull(loc.getOriginalLocId());
	}

	@Test
	public void testHowJdkHandlesLegacyLocales() {
		Locale[] jdkLocales = {
				new Locale("hE"),
				Locale.forLanguageTag("He"),
				new Locale("iW"),
				Locale.forLanguageTag("Iw")
		};
		// No matter how we create a Locale, JDK consistently returns "iw" as string,
		// and consistent returns "he" as languageTag.
		for (Locale jdkLocale : jdkLocales) {
			assertEquals("iw", jdkLocale.toString());
			assertEquals("he", jdkLocale.toLanguageTag());
		}
		assertEquals("zh_TW_#Hant", Locale.forLanguageTag("zh-hant-tw").toString());
		assertEquals("zh-Hant-TW", Locale.forLanguageTag("zh-hant-tw").toLanguageTag());

		// This means that one should never compare a locale to a string,
		// because this is not guaranteed to be true:
		//     String localeId = "???"
		//     localeId.equals(new Locale(localeId));
		//     localeId.equals(Locale.forLanguageTag(localeId));
	}

	@Test
	public void testHowOkapiHandlesLegacyLocales() {
		// LocaleId also behaves consitently, but it is closer to BCP47 / the JDK languageTag.
		// Just because "sr-Hant-TW" looks better than "zh_TW_#Hant",
		// and "he" because "iw" was deprecated since 1989 :-)

		String[] heIw = { "hE", "iW" };
		for (String locId : heIw) {
			LocaleId[] variousLocaleIds = { // LocaleId created in all possible ways
					LocaleId.HEBREW,
					LocaleId.fromString(locId),
					LocaleId.fromBCP47(locId),
					LocaleId.fromBCP47(locId, false),
					LocaleId.fromBCP47(locId, true),
					LocaleId.fromPOSIXLocale(locId),
					new LocaleId(locId),
					new LocaleId(new Locale(locId)),
					new LocaleId(Locale.forLanguageTag(locId)),
					new LocaleId(new ULocale(locId)),
					new LocaleId(ULocale.forLanguageTag(locId))
			};

			for (LocaleId locToTest : variousLocaleIds) {
				// Make sure the results are consistent, no matter how the locale was created
				assertEquals("he", locToTest.toString());
				assertEquals("he", locToTest.toBCP47());
				// Make sure comparisons with string works
				assertEquals(0, locToTest.compareTo("He"));
				assertTrue(locToTest.equals("He"));
				assertEquals(0, locToTest.compareTo("Iw"));
				assertTrue(locToTest.equals("Iw"));
				// Make sure comparisons with other locale works, no matter how it was created
				for (LocaleId locToCompare : variousLocaleIds) {
					assertEquals(0, locToTest.compareTo(locToCompare));
					assertTrue(locToTest.equals(locToCompare));
				}
			}
		}

		final String zhTwLocaleInput = "zH-hANt-tW";
		final String zhTwLocaleExpected = "zh-Hant-TW";
		assertEquals(zhTwLocaleExpected, LocaleId.fromString(zhTwLocaleInput).toString());
		assertEquals(zhTwLocaleExpected, LocaleId.fromString(zhTwLocaleInput).toBCP47());
		assertEquals(zhTwLocaleExpected, LocaleId.fromBCP47(zhTwLocaleInput).toString());
		assertEquals(zhTwLocaleExpected, LocaleId.fromBCP47(zhTwLocaleInput).toBCP47());
		assertEquals(zhTwLocaleExpected, LocaleId.fromBCP47(zhTwLocaleInput, false).toString());
		assertEquals(zhTwLocaleExpected, LocaleId.fromBCP47(zhTwLocaleInput, false).toBCP47());
		assertEquals(zhTwLocaleExpected, LocaleId.fromBCP47(zhTwLocaleInput, true).toString());
		assertEquals(zhTwLocaleExpected, LocaleId.fromBCP47(zhTwLocaleInput, true).toBCP47());
		assertEquals(zhTwLocaleExpected, new LocaleId(Locale.forLanguageTag(zhTwLocaleInput)).toString());
		assertEquals(zhTwLocaleExpected, new LocaleId(Locale.forLanguageTag(zhTwLocaleInput)).toBCP47());
	}

	@Test
	public void testIssue505() {
		LocaleId srLatin = new LocaleId(Locale.forLanguageTag("sr-Latn-RS"));
        LocaleId srCyrl = new LocaleId(Locale.forLanguageTag("sr-Cyrl-RS"));
        assertEquals("sr-Latn-RS", srLatin.toString());
        assertEquals("sr-Cyrl-RS", srCyrl.toString());
	}

	@Test
	public void testIssue580() {
		// Before the update to ICU ULocale this was throwing under Java 8:
		//   Exception in thread "main" java.lang.IllegalArgumentException:
		//   The locale identifier cannot be null or empty.
		// But not anymore.
		LocaleId.getAvailableLocales();
	}
}
