/*===========================================================================
  Copyright (C) 2009-2016 by the Okapi Framework contributors
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.icu.util.ULocale;

/**
 * Holds the normalized identifier for a given language/locale.
 */
public final class LocaleId implements Comparable<Object> {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(LocaleId.class);

	private final ULocale uLocale;

	/**
	 * An empty locale.
	 */
	static public final LocaleId EMPTY = LocaleId.fromString("");

	/**
	 * All the locales.
	 */
	static private final String LOCALE_ID_ALL = "*all*";
	static private final ULocale ICU_ALL = new ULocale(LOCALE_ID_ALL);
	static public final LocaleId ALL = new LocaleId(ICU_ALL);

	/**
	 * Same as EMPTY, but reminds the user that some components
	 * use EMPTY as a setting to enable locale auto detection.
	 * e.g., XlifffFilter, TableFilter and TmxFilter
	 */
	static public final LocaleId AUTODETECT = EMPTY;

	// Default for a few locales used often
	/**
	 * LocaleId constant for "ar".
	 */
	static public final LocaleId ARABIC = LocaleId.fromString("ar");
	/**
	 * LocaleId constant for "zh-cn".
	 */
	static public final LocaleId CHINA_CHINESE = LocaleId.fromString("zh-cn");
	/**
	 * LocaleId constant for "zh-tw".
	 */
	static public final LocaleId TAIWAN_CHINESE = LocaleId.fromString("zh-tw");
	/**
	 * LocaleId constant for "en".
	 */
	static public final LocaleId ENGLISH = LocaleId.fromString("en");
	/**
	 * LocaleId constant for "en-US".
	 */
	static public final LocaleId US_ENGLISH = LocaleId.fromString("en-US");
	/**
	 * LocaleId constant for "fr".
	 */
	static public final LocaleId FRENCH = LocaleId.fromString("fr");
	/**
	 * LocaleId constant for "de".
	 */
	static public final LocaleId GERMAN = LocaleId.fromString("de");
	/**
	 * LocaleId constant for "he".
	 */
	public static final LocaleId HEBREW = LocaleId.fromString("he");
	/**
	 * LocaleId constant for "it".
	 */
	static public final LocaleId ITALIAN = LocaleId.fromString("it");
	/**
	 * LocaleId constant for "ja".
	 */
	static public final LocaleId JAPANESE = LocaleId.fromString("ja");
	/**
	 * LocaleId constant for "ko".
	 */
	static public final LocaleId KOREAN = LocaleId.fromString("ko-kr");
	/**
	 * LocaleId constant for "pt".
	 */
	static public final LocaleId PORTUGUESE = LocaleId.fromString("pt");
	/**
	 * LocaleId constant for "ru".
	 */
	static public final LocaleId RUSSIAN = LocaleId.fromString("ru");
	/**
	 * LocaleId constant for "es".
	 */
	static public final LocaleId SPANISH = LocaleId.fromString("es");

	/**
	 * Pattern to match the BCP-47 codes of the locales that use bidirectional scripts.
	 * Note that this is not perfect as some languages use several scripts.
	 *
	 * @see <a href="https://phabricator.wikimedia.org/T2745#34767">RTL languages</a>
	 * @see <a href="https://meta.wikimedia.org/wiki/Template:List_of_language_names_ordered_by_code">Language names</a>
	 */
	private static final Pattern BIDILOCALES = Pattern.compile("(ar|arc|arz|bcc|bqi|ckb|dv|fa|glk|he|iw|ku|mzn|pnb|ps|sd|ug|ur|yi|syr|syc)(-.*)?", Pattern.CASE_INSENSITIVE);

	// original locId before normalization
	private String originalLocId;

	private static final int POSIX_LANGUAGE = 1;
	private static final int POSIX_REGION = 3;
	private static final int POSIX_VARIANT = 7;

	// Pattern to parse/validate POSIX locale identifiers
	private static final Pattern POSIX_PATTERN = Pattern.compile("\\A(\\p{Alpha}{2,3})"
		+ "(_(\\p{Alpha}*?))?(\\.([\\p{Alnum}_-]*?))?(@([\\p{Alnum}_-]*?))?\\z");

	/**
	 * Replaces the locale/language variables in a given input string by their runtime values.
	 * If one of the locale passed is null, its corresponding variables are replaced by an empty string.
	 * @param input the string with the variables.
	 * @param srcLoc the source locale code (can be null).
	 * @param trgLoc the target locale code (can be null).
	 * @return the modified string.
	 */
	static public String replaceVariables(String input, LocaleId srcLoc, LocaleId trgLoc) {
		// No variables: no changes
		if (input.indexOf("${") == -1) return input;

		String result = input;
		// Make the variables backward compatible
		result = result.replace("${Src", "${src");
		result = result.replace("${Trg", "${trg");

		if (srcLoc == null) srcLoc = LocaleId.EMPTY;
		if (trgLoc == null) trgLoc = LocaleId.EMPTY;

		final String srcTmp = srcLoc.toIcuLocale().getBaseName().replace('_', '-');
		result = result.replace("${srcLangU}", srcTmp.toUpperCase(Locale.US));
		result = result.replace("${srcLangL}", srcTmp.toLowerCase(Locale.US));
		result = result.replace("${srcLang}", srcTmp);

		final String trgTmp = trgLoc.toIcuLocale().getBaseName().replace('_', '-');
		result = result.replace("${trgLangU}", trgTmp.toUpperCase(Locale.US));
		result = result.replace("${trgLangL}", trgTmp.toLowerCase(Locale.US));
		result = result.replace("${trgLang}", trgTmp);

		if (result.indexOf("${srcLoc") != -1) {
			final ULocale uLoc = srcLoc.toIcuLocale();
			result = result.replace("${srcLoc}", uLoc.getBaseName());
			result = result.replace("${srcLocLang}", uLoc.getLanguage());
			result = result.replace("${srcLocScript}", uLoc.getScript());
			result = result.replace("${srcLocVariant}", uLoc.getVariant());
			result = result.replace("${srcLocReg}", uLoc.getCountry());
		}

		if (result.indexOf("${trgLoc") != -1) {
			final ULocale uLoc = trgLoc.toIcuLocale();
			result = result.replace("${trgLoc}", uLoc.getBaseName());
			result = result.replace("${trgLocLang}", uLoc.getLanguage());
			result = result.replace("${trgLocScript}", uLoc.getScript());
			result = result.replace("${trgLocVariant}", uLoc.getVariant());
			result = result.replace("${trgLocReg}", uLoc.getCountry());
		}

		return result;
	}

	/**
	 * Replaces the locale/language variables in a given input string by their runtime values.
	 * If one of the locale passed is null, its corresponding variables are replaced by an empty string.
	 * @param input the string with the variables.
	 * @param srcLoc the source locale code (can be null).
	 * @param trgLoc the target locale code (can be null).
	 * @return the modified string.
	 */
	static public String replaceVariables(String input, String srcLoc, String trgLoc) {
		return replaceVariables(input,
				LocaleId.fromString(srcLoc == null ? "" : srcLoc),
				LocaleId.fromString(trgLoc == null ? "" : trgLoc));
	}

	static private ULocale okapiLocaleToULocale(String locId) {
		if (locId == null) { // Empty is OK
			throw new IllegalArgumentException("The locale identifier cannot be null.");
		}
		switch (locId.toLowerCase(Locale.US)) {
			case "":
				return ULocale.ROOT;
			case LOCALE_ID_ALL:
				return ICU_ALL;
			case "ja-jp-x-calja":
				return new ULocale("ja-JP-u-ca-japanese");
			case "th-th-x-numth":
				return new ULocale("th-TH-u-nu-thai");
		}
		return new ULocale(locId);
	}

	static private String okapiLocaleFromULocale(ULocale locale) {
		if (locale == null) { // Empty is OK
			throw new IllegalArgumentException("The locale cannot be null.");
		}
		if (locale == ULocale.ROOT) {
			return "";
		}
		if (locale == ICU_ALL) {
			return LOCALE_ID_ALL;
		}

		final String langTag = locale.toLanguageTag();
		switch (langTag) {
			case "ja-JP-u-ca-japanese":
				return "ja-jp-x-calja";
			case "th-TH-u-nu-thai":
				return "th-th-x-numth";
		}
		return langTag;
	}

	/**
	 * Creates a new LocaleId object from a locale identifier.
	 * @param locId a LocaleId string
	 * @param normalize true if it needs to be normalized the string,
	 * false to use as-it. When use as-it, the identifier is expected to be in lower-cases and use '-'
	 * for separator.
	 * @throws IllegalArgumentException if the argument in invalid.
	 * @deprecated normalize is ignored. Use {@link #fromString(String)} to create a LocaleId from a code.
	 */
	@Deprecated
	public LocaleId(String locId, boolean normalize) {
		originalLocId = locId;
		uLocale = okapiLocaleToULocale(locId);
	}

	/**
	 * Creates a new LocaleId for a given language code.
	 * This constructor does not take a locale identifier as argument, just a language identifier.
	 * Use {@link #LocaleId(String, boolean)} to create a new LocaleId from a locale identifier.
	 * @param language the language code (e.g. "de" for German).
	 * @throws IllegalArgumentException if the argument in invalid.
	 */
	public LocaleId(String language) {
		//TODO: should be prevent to pass language tag with more than the language?
		if (Util.isEmpty(language)) {
			throw new IllegalArgumentException("The language cannot be null or empty.");
		}
		if (!language.matches("^[a-zA-Z]{2,3}$")) {
			throw new IllegalArgumentException("The language should be a 2 or 3 character ISO code.");
		}
		uLocale = okapiLocaleToULocale(language);
	}

	/**
	 * Creates a new LocaleId for a given language code and region code.
	 * @param language the language code (e.g. "es" for Spanish).
	 * @param region the region code (e.g. "es" for Spain or "005" for South America.
	 * This parameter is ignored if null or empty.
	 * @throws IllegalArgumentException if the argument in invalid.
	 */
	public LocaleId(String language, String region) {
		if (Util.isEmpty(language)) {
			throw new IllegalArgumentException("The language cannot be null or empty.");
		}
		uLocale = new ULocale.Builder()
				.setLanguage(language)
				.setRegion(region)
				.build();
	}

	/**
	 * Creates a new LocaleId for a given language code, region code, and a user part.
	 * @param language the language code (e.g. "es" for Spanish).
	 * @param region the region code (e.g. "es" for Spain or "005" for South America.
	 * @param userPart the user part of locale.
	 * The latter two parameters are ignored if null or empty.
	 * @throws IllegalArgumentException if the argument in invalid.
	 */
	public LocaleId(String language, String region, String userPart) {
		if (Util.isEmpty(language)) {
			throw new IllegalArgumentException("The language cannot be null or empty.");
		}
		uLocale = new ULocale.Builder()
				.setLanguage(language)
				.setRegion(region)
				.setExtension(ULocale.PRIVATE_USE_EXTENSION, userPart)
				.build();
	}

	/**
	 * Creates a new LocaleId for the given ICU Locale.
	 * @param uLoc the ICU Locale object to use.
	 * @throws IllegalArgumentException if the argument in invalid.
	 */
	public LocaleId(ULocale uLoc) {
		if (uLoc == null) {
			throw new IllegalArgumentException("The locale cannot be null.");
		}
		uLocale = (ULocale) uLoc.clone();
	}

	/**
	 * Creates a new LocaleId for the given Java Locale.
	 * @param loc the Java Locale object to use.
	 * @throws IllegalArgumentException if the argument in invalid.
	 */
	public LocaleId(Locale loc) {
		if (loc == null) {
			throw new IllegalArgumentException("The locale cannot be null.");
		}
		uLocale = ULocale.forLocale(loc);
	}

	/**
	 * Gets the string representation of the LocaleId.
	 * @return the string representation of the LocaleId.
	 */
	@Override
	public String toString() {
		return okapiLocaleFromULocale(uLocale);
	}

	/**
	 * Get the original locale id before any normalization
	 * @return the original locale id
	 * @deprecated this will go away. Locales are case insensitive.
	 */
	@Deprecated
	public String getOriginalLocId() {
		return originalLocId;
	}

	/**
	 * Returns a clone of this LocaleId. Because LocaleId are immutable objects
	 * this method returns the same LocaleId.
	 * @return the same LocaleId (because it is immutable).
	 */
	@Override
	public LocaleId clone() {
		// No need to duplicate since the object is immutable
		return this;
	}

	/**
	 * Returns a hash code value for this LocaleId.
	 * @return the hash code value for this LocaleId.
	 */
	@Override
	public int hashCode() {
		return uLocale.hashCode();
	}

	/**
	 * Indicates if a given object is equal to this localeId object.
	 * @param arg the object to compare. This can be a LocaleId object
	 * or a string. Any other object will always return false. If the parameter
	 * is a string it is normalized before being compared.
	 * @return true if the parameter is the same object,
	 * or if it is a LocaleId with the same identifier,
	 * or if it is a string equals to the identifier. False otherwise.
	 */
	@Override
	public boolean equals(Object arg) {
		return compareTo(arg) == 0;
	}

	/**
	 * Compares this LocaleId with a given object.
	 * @param arg the object to compare. If the parameter is a string it is normalized
	 * before being compared.
	 * @return 1 if the parameter is null. If the parameter is a LocaleId or
	 * a string, the return is the same as the return of a comparison between
	 * the identifier of this LocaleId and the string representation of the argument.
	 * Otherwise the return is 1;
	 */
	public int compareTo(Object arg) {
		if (arg == null) {
			return 1;
		}
		if (arg instanceof LocaleId) {
			final String locId1 = toBCP47();
			final String locId2 = ((LocaleId) arg).toBCP47();
			return locId1.compareTo(locId2);
		}
		if (arg instanceof String) {
			final String locId1 = toBCP47();
			final String locId2 = LocaleId.fromString((String) arg).toBCP47();
			return locId1.compareTo(locId2);
		}
		return 1;
	}

	/**
	 * Creates a new LocaleId from a locale identifier (and validate it).
	 * Calling this method is the same as calling <code>new LocaleId(locId, true);</code>
	 * @param locId the locale identifier to use (it will be normalized).
	 * @return a new localeId object from the given identifier.
	 * @throws IllegalArgumentException if the argument is invalid.
	 */
	static public LocaleId fromString(String locId) {
		final LocaleId result = new LocaleId(okapiLocaleToULocale(locId));
		result.originalLocId = locId;
		return result;
	}

	/**
	 * Creates a new LocaleId from a POSIX locale identifier.
	 * @param locId the POSIX locale identifier (e.g. "de-at.UTF-8@EURO")
	 * @return a new LocaleId or null if an error occurred.
	 * @throws IllegalArgumentException if the argument is invalid.
	 */
	static public LocaleId fromPOSIXLocale(String locId) {
		// POSIX syntax: language[_territory][.encoding][@modifier]
		if (Util.isEmpty(locId)) {
			throw new IllegalArgumentException("The locale identifier cannot be null or empty.");
		}
		final Matcher m = POSIX_PATTERN.matcher(locId);
		if (m.find()) {
			// debug
			if (LOGGER.isTraceEnabled()) {
				for (int i = 1; i < m.groupCount(); i++) {
					LOGGER.trace(String.format("g=%d [%s]", i, m.group(i)));
				}
			}
			final ULocale.Builder builder = new ULocale.Builder();
			final String lang = m.group(POSIX_LANGUAGE);
			if (!Util.isEmpty(lang)) {
				builder.setLanguage(lang);
			}
			final String region = m.group(POSIX_REGION);
			if (!Util.isEmpty(region)) {
				builder.setRegion(region);
			}
			final String variant = m.group(POSIX_VARIANT);
			if (!Util.isEmpty(variant)) {
				switch (variant.toLowerCase(Locale.US)) {
					case "euro":
						builder.setUnicodeLocaleKeyword("cu", "EUR");
						break;
					case "ats":
						builder.setUnicodeLocaleKeyword("cu", "ATS");
						break;
					case "latin":
						builder.setScript("Latn");
						break;
					default:
						builder.setExtension(ULocale.PRIVATE_USE_EXTENSION, variant); // -x-
						break;
				}
			}
			final LocaleId result = new LocaleId(builder.build());
			result.originalLocId = locId;
			return result;
		} else {
			throw new IllegalArgumentException(String.format(
				"The POSIX locale '%s' is invalid.", locId));
		}
	}

	/**
	 * Gets a POSIX locale identifier for this LocaleId.
	 * For example: "af-za" returns "af_ZA".
	 * @return the corresponding POSIX locale identifier for this LocaleId.
	 */
	public String toPOSIXLocaleId() {
		//TODO: Make it simpler, and complete it
		String tmp = getLanguage();
		if (getRegion() != null) {
			tmp += ("_" + getRegion().toUpperCase());
		}
		return tmp;
	}

	/**
	 * Creates a new LocaleId from a BCP-47 language tag.
	 *
	 * <p>If {@code strict} is {@code true} the language tag must be well-formed
	 * (see ICU4J ULocale) or an exception is thrown.
	 * It rejects language tags that use '_' instead of '-', or have ill-formed "parts".</p>
	 * <p>If {@code strict} is {@code false} then it accepts both '_' and '-', converts invalid
	 * language id to "und", and discards ill-formed and following portions of the tag.</p>
	 *
	 * @see <a href="http://unicode.org/reports/tr35/#BCP_47_Conformance">BCP 47 Conformance</a>
	 *
	 * @param langtag the language tag to use (e.g. "fr-CA")
	 * @param strict the language tag parsing is stricter
	 * @return a new LocaleId.
	 * @throws IllegalArgumentException if the argument in invalid.
	 */
	static public LocaleId fromBCP47(String langtag, boolean strict) {
		if (langtag == null) {
			throw new IllegalArgumentException("The language tag cannot be null.");
		}

		final ULocale ulocale;
		if (strict) {
			try {
				ulocale = new ULocale.Builder().setLanguageTag(langtag).build();
			} catch (com.ibm.icu.util.IllformedLocaleException e) {
				throw new IllegalArgumentException(e.getMessage());
			}
		} else {
			if (langtag.isEmpty()) {
				return LocaleId.EMPTY;
			}
			ulocale = ULocale.forLanguageTag(langtag);
		}

		final LocaleId result = new LocaleId(ulocale);
		result.originalLocId = langtag;
		return result;
	}

	/**
	 * Creates a new LocaleId from a BCP-47 language tag.
	 * The parsing of the language tag is not strict.
	 * @see #fromBCP47(String langtag, boolean strict)
	 *
	 * @param langtag the language tag to use (e.g. "fr-CA")
	 * @return a new LocaleId.
	 * @throws IllegalArgumentException if the argument in invalid.
	 */
	static public LocaleId fromBCP47(String langtag) {
		return fromBCP47(langtag, false);
	}

	/**
	 * Gets the BCP-47 language tag for this LocaleId.
	 * @return the BCP-47 language tag for the given LocaleId.
	 */
	public String toBCP47() {
		if (uLocale == ICU_ALL) {
			return LOCALE_ID_ALL;
		}
		return uLocale.toLanguageTag();
	}

	/** Creates a new Java Locale object from this LocaleId.
	 * @return a new Java Locale object based on the best match for the given LocaleId,
	 * or null if an error occurred.
	 */
	public Locale toJavaLocale() {
		return uLocale.toLocale();
	}

	/** Gets the ICU ULocale object wrapped by this LocaleId.
	 * @return ICU ULocale object wrapped by the given LocaleId.
	 */
	public ULocale toIcuLocale() {
		return uLocale;
	}

	/**
	 * Gets the language code for this LocaleId.
	 * @return the language code.
	 */
	public String getLanguage() {
		return uLocale.getLanguage();
	}

	/**
	 * Gets the region code for this LocaleId.
	 * @return the region code or null if there is none or if an error occurs.
	 */
	public String getRegion() {
		final String result = uLocale.getCountry();
		return result.isEmpty() ? null : result;
	}

	/**
	 * Gets the script code for this LocaleId.
	 * @return the script code or null if there is none or if an error occurs.
	 */
	public String getScript() {
		final String result = uLocale.getScript();
		return result.isEmpty() ? null : result;
	}

	/**
	 * Gets the variant for this LocaleId.
	 * @return the variant or null if there is none or if an error occurs.
	 */
	public String getVariant() {
		final String result = uLocale.getVariant();
		return result.isEmpty() ? null : result;
	}

	/**
	 * Gets the user part of this LocaleId.
	 * @return the user part or null if there is none or if an error occurs.
	 */
	public String getUserPart() {
		final String langTag = uLocale.toLanguageTag();
		switch (langTag) {
			case "ja-JP-u-ca-japanese":
				return "calja";
			case "th-TH-u-nu-thai":
				return "numth";
		}
		return uLocale.getExtension(ULocale.PRIVATE_USE_EXTENSION);
	}

	/**
	 * Indicates if the language of a given LocaleId is the same as the one of this LocaleId.
	 * For example: "en" and "en-us" returns true, "es-es" and "ca-es" return false.
	 * @param other the LocaleId object to compare.
	 * @return true if the languages of two given LocaleIds are the same.
	 */
	public boolean sameLanguageAs(LocaleId other) {
		if (other == null) {
			return false; // locId is not null
		}
		return this.getLanguage().equals(other.getLanguage());
	}

	/**
	 * Indicates if a given string has the same language as the one of this LocaleId.
	 * For example: "en" and "en-us" returns true, "es-es" and "ca-es" return false.
	 * @param langCode the string to compare.
	 * @return true if the languages of both objects are the same.
	 */
	public boolean sameLanguageAs(String langCode) {
		return this.getLanguage().equals(ULocale.getLanguage(langCode));
	}

	/**
	 * Indicates if the region of a given LocaleId is the same as the one of this LocaleId.
	 * For example: "es-us" and "en-us" returns true, "es-es" and "es-us" return false.
	 * @param other the LocaleId object to compare.
	 * @return true if the region parts of two given LocaleIds are the same.
	 */
	public boolean sameRegionAs(LocaleId other) {
		if (other == null) {
			return false; // locId is not null
		}
		return uLocale.getCountry().equals(other.uLocale.getCountry());
	}

	/**
	 * Indicates if a given string has the same region as the one of this LocaleId.
	 * For example: "es-us" and "en-us" returns true, "es-es" and "es-us" return false.
	 * @param locId the string to compare.
	 * @return true if the region parts of both objects are the same.
	 */
	public boolean sameRegionAs(String locId) {
		return uLocale.getCountry().equals(ULocale.getCountry(locId));
	}

	/**
	 * Indicates if the user part of a given LocaleId is the same as the one of this LocaleId.
	 * For example: "es-us-x-win" and "en_us@win" returns true, "es_us@mac" and "es_us@ats" return false.
	 * @param other the LocaleId object to compare.
	 * @return true if the region parts of two given LocaleIds are the same.
	 */
	public boolean sameUserPartAs(LocaleId other) {
		if (other == null) {
			return false; // locId is not null
		}

		final String userPart1 = this.getUserPart();
		final String userPart2 = other.getUserPart();

		if (userPart1 == null && userPart2 == null) {
			return true;
		}

		if (userPart1 == null) return false;
		return userPart1.equals(userPart2);
	}

	/**
	 * Indicates if a given string has the same user part as the one of this LocaleId.
	 * For example: "es-us-x-win" and "en-us@win" returns true, "es-us@mac" and "es-us@ats" return false.
	 * @param langCode the string to compare.
	 * @return true if the region parts of both objects are the same.
	 */
	public boolean sameUserPartAs(String langCode) {
		return sameUserPartAs(LocaleId.fromString(langCode));
	}

	/**
	 * Gets an array of the LocaleId objects for all the Java locales installed on the system.
	 * @return an array of the LocaleId objects for all the available Java locales.
	 */
	static public LocaleId[] getAvailableLocales() {
		ULocale[] jlocales = ULocale.getAvailableLocales();
		LocaleId[] locIds = new LocaleId[jlocales.length];
		for (int i = 0; i < jlocales.length; i++) {
			locIds[i] = LocaleId.fromString(jlocales[i].toLanguageTag());
		}
		return locIds;
	}

	/**
	 * Splits a given locale tag (e.g. "fr-ca") into its components.
	 * Use this method when working directly with an {@link LocaleId} object is not desirable.
	 * This method supports only simple ISO codes (not complex BCP-47 tags).
	 * @param locId the locale code to process.
	 * @return an array of two strings: 0=language, 1=region/country (or empty)
	 * @deprecated don't use this. It only "understands" language + region,
	 *     but that is just a small part of what BCP 47 supports.
	 */
	@Deprecated
	public static String[] splitLanguageCode(String locId) {
		final String[] parts = {"", ""};

		if ((locId == null) || (locId.length() == 0)) {
			return parts;
		}

		final LocaleId loc = LocaleId.fromString(locId);
		final String language = loc.getLanguage();
		final String region = loc.getRegion();

		if (language != null) parts[0] = language;
		if (region != null) parts[1] = region;

		return parts;
	}

	/**
	 * Indicates if a given locale usually uses a bi-directional script.
	 * <p>Note that this is not perfect as some languages use several scripts.
	 * @param locId the locale to check.
	 * @return true if the locale uses a bi-directional script.
	 */
	public static boolean isBidirectional(LocaleId locId) {
		if (locId.uLocale.isRightToLeft()) {
			return true;
		}
		// We keep this, as ICU does not seem to know about ancient locales
		return BIDILOCALES.matcher(locId.toBCP47()).matches();
	}

	/**
	 * Checks whether the specified locale has characters as numeral separators.
	 *
	 * @param localeId The locale to check against
	 *
	 * @return {@code true} - if the specified locale has characters as numeral separators
	 *         {@code false} - otherwise
	 */
	public static boolean hasCharactersAsNumeralSeparators(LocaleId localeId) {
		return HEBREW.getLanguage().equals(localeId.getLanguage());
	}

	/**
	 * Converts a list of language codes into {@link LocaleId}s
	 * @param languageCodes the list of codes.
	 * @return the list of {@link LocaleId} objects created.
	 */
	public static List<LocaleId> convertToLocaleIds(List<String> languageCodes) {
		List<LocaleId> locales = new ArrayList<LocaleId>();
		for (String languageCode : languageCodes) {
			locales.add(LocaleId.fromString(languageCode));
		}
		return locales;
	}

}
