/*===========================================================================
  Copyright (C) 2008-2011 by the Okapi Framework contributors
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

package net.sf.okapi.applications.rainbow.lib;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import net.sf.okapi.common.ListUtil;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.filters.FilterConfiguration;
import net.sf.okapi.common.filters.IFilterConfigurationMapper;

public class FormatManager {

	Map<String, String> pairs;
	
	public void load (String p_sPath) {
		//TODO: Load format manager data from external file
		pairs = new Hashtable<String, String>();
		pairs.put(".xlf", "okf_xliff");
		pairs.put(".xml", "okf_xml");
		pairs.put(".html", "okf_html");
		pairs.put(".htm", "okf_html");
		pairs.put(".properties", "okf_properties");
		pairs.put(".lang", "okf_properties-skypeLang");
		pairs.put(".tmx", "okf_tmx");
		pairs.put(".mif", "okf_mif");
		pairs.put(".rtf", "okf_tradosrtf");
		pairs.put(".idml", "okf_idml");
		pairs.put(".pdf", "okf_pdf");
		pairs.put(".po", "okf_po");
		pairs.put(".pot", "okf_po");
		pairs.put(".docx", "okf_openxml");
		pairs.put(".docm", "okf_openxml");
		pairs.put(".dotx", "okf_openxml");
		pairs.put(".dotm", "okf_openxml");
		pairs.put(".pptx", "okf_openxml");
		pairs.put(".pptm", "okf_openxml");
		pairs.put(".ppsx", "okf_openxml");
		pairs.put(".ppsm", "okf_openxml");
		pairs.put(".potx", "okf_openxml");
		pairs.put(".potm", "okf_openxml");
		pairs.put(".xlsx", "okf_openxml");
		pairs.put(".xlsm", "okf_openxml");
		pairs.put(".xltx", "okf_openxml");
		pairs.put(".xltm", "okf_openxml");
		pairs.put(".vsdx", "okf_openxml");
		pairs.put(".vsdm", "okf_openxml");
		pairs.put(".odt", "okf_openoffice");
		pairs.put(".ott", "okf_openoffice");
		pairs.put(".sxw", "okf_openoffice");
		pairs.put(".stw", "okf_openoffice");
		pairs.put(".odp", "okf_openoffice");
		pairs.put(".otp", "okf_openoffice");
		pairs.put(".sxi", "okf_openoffice");
		pairs.put(".sti", "okf_openoffice");
		pairs.put(".ods", "okf_openoffice");
		pairs.put(".ots", "okf_openoffice");
		pairs.put(".sxc", "okf_openoffice");
		pairs.put(".stc", "okf_openoffice");
		pairs.put(".odg", "okf_openoffice");
		pairs.put(".otg", "okf_openoffice");
		pairs.put(".sxd", "okf_openoffice");
		pairs.put(".std", "okf_openoffice");
		pairs.put(".sdlxliff", "okf_xliff-sdl");
		pairs.put(".mqxliff", "okf_xliff");
		pairs.put(".xliff", "okf_xliff");
		pairs.put(".dtd", "okf_dtd");
		pairs.put(".ts", "okf_ts");
		pairs.put(".txt", "okf_plaintext");
		pairs.put(".srt", "okf_regex-srt");
		pairs.put(".json", "okf_json");
		pairs.put(".ttx", "okf_ttx");
		pairs.put(".pentm", "okf_pensieve");
		pairs.put(".yml", "okf_yaml");
		pairs.put(".yaml", "okf_yaml");
		pairs.put(".vrsz", "okf_versifiedtxt");
		pairs.put(".rkm", "okf_rainbowkit");
		pairs.put(".rkp", "okf_rainbowkit-package");
		pairs.put(".txp", "okf_transifex");
		pairs.put(".txml", "okf_txml");
		pairs.put(".strings", "okf_regex-macStrings");
		pairs.put(".h", "okf_doxygen");
		pairs.put(".c", "okf_doxygen");
		pairs.put(".cpp", "okf_doxygen");
		pairs.put(".java", "okf_doxygen");
		pairs.put(".py", "okf_doxygen");
		pairs.put(".m", "okf_doxygen");
		pairs.put(".wcml", "okf_icml");
		pairs.put(".md", "okf_markdown");
	}
	
	/**
	 * Tries to guess the format and the encoding of a give document.
	 * @param p_sPath Full path of the document to process.
	 * @return An array of string: 0=guessed encoding or null,
	 * 1=guessed filter settings or null,
	 */
	public String[] guessFormat (String p_sPath) {
		String[] aRes = new String[2];
		String sExt = Util.getExtension(p_sPath).toLowerCase();
		aRes[0] = null; // Encoding not detected
		aRes[1] = pairs.get(sExt);
		return aRes;
	}

	public void addExtensionMapping (FilterConfiguration config) {
		for (String ext: ListUtil.stringAsList(config.extensions, ";")) {
			if (Util.isEmpty(ext)) continue;
			if (pairs.containsKey(ext)) continue; // not to override explicitly set ones
			pairs.put(ext, config.configId);
		}
	}
	
	public void addConfigurations (IFilterConfigurationMapper fcMapper) {
		for (Iterator<FilterConfiguration> iterator = fcMapper.getAllConfigurations(); iterator.hasNext();) {
			FilterConfiguration config = iterator.next();
			addExtensionMapping(config);
		}
	}
	
}
