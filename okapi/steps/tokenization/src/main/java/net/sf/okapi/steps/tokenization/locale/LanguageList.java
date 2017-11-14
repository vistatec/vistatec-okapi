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
import java.util.TreeMap;

import com.ibm.icu.util.ULocale;

public class LanguageList {

	// The map keys are ICU locale ID's 
	private static TreeMap<String, ULocale> map = new TreeMap<String, ULocale>();
	private static final ULocale EN = new ULocale("en");
	
	static {
		ULocale[] locales = ULocale.getAvailableLocales();
		
		for (ULocale locale : locales)			
			if (locale != null)
				map.put(locale.getName(), locale);
	}
	
	protected static String formatLanguageInfo(ULocale locale) {		
		if (locale == null) return "";
		
		return locale.getDisplayName(EN);
	}
	
	public static String[] getLanguages() {
		
		ArrayList<String> res = new ArrayList<String> ();

		for (ULocale locale : map.values()) {
			
			res.add(formatLanguageInfo(locale));
		}
		
		return res.toArray(new String[] {});
	}
	
	public static List<String> getAllLanguages() {
		
		ArrayList<String> res = new ArrayList<String> ();

		for (ULocale locale : map.values()) {
			
			res.add(LocaleUtil.normalizeLanguageCode_Okapi(locale.getName()));
		}
		
		return res;
	}

	public static String[] getLanguageCodes_Okapi() {
		
//		ArrayList<String> res = new ArrayList<String> ();
//
//		for (ULocale locale : map.values()) {
//			
//			res.add(LocaleUtil.normalizeLanguageCode_Okapi(locale.getName()));
//		}
		
		List<String> res = getAllLanguages();
		if (res == null) return new String[] {};
		
		return res.toArray(new String[] {});
	}
	
	public static String[] getLanguageCodes_ICU() {
		
		ArrayList<String> res = new ArrayList<String> ();

		for (ULocale locale : map.values()) {
			
			res.add(locale.getName());
		}
		
		return res.toArray(new String[] {});
	}
	
	public static String getDisplayName(String code_Okapi) {
		
		String code_ICU = LocaleUtil.normalizeLanguageCode_ICU(code_Okapi);
		
		return formatLanguageInfo(map.get(code_ICU));
	}
	
}
