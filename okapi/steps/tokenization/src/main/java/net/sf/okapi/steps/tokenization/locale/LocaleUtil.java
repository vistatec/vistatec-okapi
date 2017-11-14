/*===========================================================================
  Copyright (C) 2008-2009 by the Okapi Framework contributors
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

package net.sf.okapi.steps.tokenization.locale;

import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;

import com.ibm.icu.util.ULocale;

public class LocaleUtil {
	
	/**
	 * Converts the language tag (language ID and an optional region/country part) to the Okapi format: lower-case, delimited with a dash.
	 * @param languageCode
	 * @return languageCode in Okapi format
	 * @deprecated don't use this. It only "understands" language + region,
	 *     but that is just a small part of what BCP 47 supports.
	 *     Also, the LocaleId does case normalization out of the box now.
	 */
	@Deprecated
	public static String normalizeLanguageCode_Okapi(String languageCode) {
		if (Util.isEmpty(languageCode))
			return null;
		
		String[] parts = LocaleId.splitLanguageCode(languageCode);		
		StringBuilder res = new StringBuilder();
		
		res.append(parts[0].toLowerCase());  
		if (!Util.isEmpty(parts[1])) {
			res.append("-");
			res.append(parts[1].toLowerCase());
		}
		return res.toString();
	}
	
	/**
	 * Converts a list of language tags (language ID and an optional region/country part) to the Okapi format: upper case, delimited with a dash.
	 * @param languageCodes List of language codes to normalize.
	 * @return A new list, containing language codes in Okapi format.
	 * @deprecated don't use this. It only "understands" language + region,
	 *     but that is just a small part of what BCP 47 supports.
	 *     Also, the LocaleId does case normalization out of the box now.
	 */
	@Deprecated
	public static List<String> normalizeLanguageCodes_Okapi(List<String> languageCodes) {
		
		if (languageCodes == null) return null;
		
		List<String> res = new ArrayList<String>();
		
		for (String languageCode : languageCodes)
			res.add(normalizeLanguageCode_Okapi(languageCode));
		
		return res;
	}
	
	/**
	 * Converts the language tag (language ID and an optional region/country part) to the ICU format: lower case for the language,
	 * upper case for the region, parts are delimited with an underscore.
	 * @param languageCode
	 * @return languageCode in ICU format
	 */
	public static String normalizeLanguageCode_ICU(String languageCode) {		
		if (Util.isEmpty(languageCode))
			return null;
		
//		String[] parts = Util.splitLanguageCode(languageCode);		
//		StringBuilder res = new StringBuilder();
//		
//		res.append(parts[0].toLowerCase());  
//		if (!Util.isEmpty(parts[1])) {
//			res.append("_");
//			res.append(parts[1].toUpperCase());
//		}
//		return res.toString();
		
		return ULocale.canonicalize(languageCode);
	}

}
