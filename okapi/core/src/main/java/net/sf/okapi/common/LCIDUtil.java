/*===========================================================================
  Copyright (C) 2010 by the Okapi Framework contributors
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
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts Microsoft's LCID to Okapi LocaleId back and forth.
 * @see <a href="http://msdn.microsoft.com/en-us/library/cc233968%28PROT.10%29.aspx">Microsoft LCID Structure</a>
 * @see <a href="http://msdn.microsoft.com/en-us/library/a9eac961-e77d-41a6-90a5-ce1a8b0cdb9c%28v=PROT.10%29#id8">LCID List</a>
 * @see <a href="http://msdn.microsoft.com/en-us/library/cc233982%28PROT.10%29.aspx">Microsoft LCID Structure</a> 
 */
public class LCIDUtil {

	class LCIDDescr {
		String language;
		String region;
		int lcid;
		String tag;
		
		public LCIDDescr(String language,						
				String region,
				int lcid,
				String tag) {
			super();
			
			this.language = language;
			this.region = region;
			this.lcid = lcid;
			this.tag = tag;	
		}
	}

	private static List<LCIDDescr> descriptors = new ArrayList<LCIDDescr> ();
	private static HashMap<Integer, LCIDDescr> tagLookup = new HashMap<Integer, LCIDDescr>();  
	private static HashMap<String, LCIDDescr> lcidLookup = new HashMap<String, LCIDDescr>();
	private static LCIDUtil inst; 

	/**
	 * Registers an LCID for a given language and region.
	 * @param language the language code.
	 * @param region the region code.
	 * @param lcid the LCID value.
	 */
	private static void registerLCID (String language, String region, int lcid) {
		LCIDDescr descr0 = tagLookup.get(lcid);
		if (descr0 != null) {
			String lang0 = descr0.language;
			String reg0 = descr0.region;
			String st = Util.isEmpty(reg0) ? lang0 : String.format("%s (%s)", lang0, reg0);
			if (language.equals(lang0) && region.equals(reg0)) {
				Logger localLogger = LoggerFactory.getLogger(LCIDUtil.class);
				localLogger.warn(String.format("Already registered LCID: (0x%04x) %s", lcid, st));
			}
		}
		
		if (inst == null) inst = new LCIDUtil();
		LCIDDescr descr = inst.new LCIDDescr(language, region, lcid, null);
		
		descriptors.add(descr);
//		String tag = getTag(lcid);
//		//if (tagLookup.get(lcid) != null)
//		if (!Util.isEmpty(tag))
//			logger.warn(String.format("Already registered LCID: 0x%04x",
//					lcid));
//		
		tagLookup.put(lcid, descr);
	}
	
	/**
	 * Registers an LCID for a language tag.
	 * @param lcid the LCID value. 
	 * @param tag the language tag.
	 */
	private static void registerTag(int lcid, String tag) {
		LCIDDescr descr;
		descr = tagLookup.get(lcid);
		
		if (descr == null) {
			Logger localLogger = LoggerFactory.getLogger(LCIDUtil.class);
//			logger.warn(String.format("Unregistered LCID: 0x%04x %s -- %s\n" +
//					"registerLCID(\"%s\", \"\", 0x%04x);\n", lcid, tag,
//					LanguageList.getDisplayName(tag), LanguageList.getDisplayName(tag), lcid));
			localLogger.warn(String.format("Unregistered LCID: 0x%04x %s\n", lcid, tag));
			return;
		}
		tag = LocaleId.fromString(tag).toString();
		descr.tag = tag;
		lcidLookup.put(tag, descr);
	}
	
	static {
		// LCID
		registerLCID("Afrikaans", "South Africa", 0x0436);
		registerLCID("Albanian", "Albania", 0x041c);
		registerLCID("Alsatian", "France", 0x0484);
		registerLCID("Amharic", "Ethiopia", 0x045e);
		registerLCID("Arabic", "Saudi Arabia", 0x0401);
		registerLCID("Arabic", "Iraq", 0x0801);
		registerLCID("Arabic", "Egypt", 0x0c01);
		registerLCID("Arabic", "Libya", 0x1001);
		registerLCID("Arabic", "Algeria", 0x1401);
		registerLCID("Arabic", "Morocco", 0x1801);
		registerLCID("Arabic", "Tunisia", 0x1c01);
		registerLCID("Arabic", "Oman", 0x2001);
		registerLCID("Arabic", "Yemen", 0x2401);
		registerLCID("Arabic", "Syria", 0x2801);
		registerLCID("Arabic", "Jordan", 0x2c01);
		registerLCID("Arabic", "Lebanon", 0x3001);
		registerLCID("Arabic", "Kuwait", 0x3401);
		registerLCID("Arabic", "U.A.E.", 0x3801);
		registerLCID("Arabic", "Kingdom of Bahrain", 0x3c01);
		registerLCID("Arabic", "Qatar", 0x4001);
		registerLCID("Armenian", "Armenia", 0x042b);
		registerLCID("Assamese", "India", 0x044d);
		registerLCID("Azeri (Cyrillic)", "Azerbaijan", 0x082c);
		registerLCID("Azeri (Latin)", "Azerbaijan", 0x042c);
		registerLCID("Bashkir", "Russia", 0x046d);
		registerLCID("Basque", "Spain", 0x042d);
		registerLCID("Belarusian", "Belarus", 0x0423);
		registerLCID("Bengali", "India", 0x0445);
		registerLCID("Bengali", "Bangladesh", 0x0845);
		registerLCID("Bosnian (Cyrillic)", "Bosnia and Herzegovina", 0x201a);
		registerLCID("Bosnian (Latin)", "Bosnia and Herzegovina", 0x141a);
		registerLCID("Breton", "France", 0x047e);
		registerLCID("Bulgarian", "Bulgaria", 0x0402);
		registerLCID("Catalan", "(Catalan)", 0x0403);
		registerLCID("Chinese", "Simplified", 0x0004);
		registerLCID("Chinese", "Taiwan", 0x0404);
		registerLCID("Chinese", "People's Republic of China", 0x0804);
		registerLCID("Chinese", "Hong Kong SAR", 0x0c04);
		registerLCID("Chinese", "Singapore", 0x1004);
		registerLCID("Chinese", "Macao SAR", 0x1404);
		registerLCID("Chinese", "Traditional", 0x7c04);
		registerLCID("Corsican", "France", 0x0483);
		registerLCID("Croatian", "Croatia", 0x041a);
		registerLCID("Croatian (Latin)", "Bosnia and Herzegovina", 0x101a);
		registerLCID("Czech", "Czech Republic", 0x0405);
		registerLCID("Danish", "Denmark", 0x0406);
		registerLCID("Dari", "Afghanistan", 0x048c);
		registerLCID("Divehi", "Maldives", 0x0465);
		registerLCID("Dutch", "Belgium", 0x0813);
		registerLCID("Dutch", "Netherlands", 0x0413);
		registerLCID("English", "Canada", 0x1009);
		registerLCID("English", "Jamaica", 0x2009);
		registerLCID("English", "Caribbean", 0x2409);
		registerLCID("English", "Belize", 0x2809);
		registerLCID("English", "Trinidad", 0x2c09);
		registerLCID("English", "United Kingdom", 0x0809);
		registerLCID("English", "Ireland", 0x1809);
		registerLCID("English", "India", 0x4009);
		registerLCID("English", "South Africa", 0x1c09);
		registerLCID("English", "Zimbabwe", 0x3009);
		registerLCID("English", "Australia", 0x0c09);
		registerLCID("English", "New Zealand", 0x1409);
		registerLCID("English", "Philippines", 0x3409);
		registerLCID("English", "United States", 0x0409);
		registerLCID("English", "Malaysia", 0x4409);
		registerLCID("English", "Singapore", 0x4809);
		registerLCID("Estonian", "Estonia", 0x0425);
		registerLCID("Faroese", "Faroe Islands", 0x0438);
		registerLCID("Filipino", "Philippines", 0x0464);
		registerLCID("Finnish", "Finland", 0x040b);
		registerLCID("French", "Canada", 0x0c0c);
		registerLCID("French", "France", 0x040c);
		registerLCID("French", "Monaco", 0x180c);
		registerLCID("French", "Switzerland", 0x100c);
		registerLCID("French", "Belgium", 0x080c);
		registerLCID("French", "Luxembourg", 0x140c);
		registerLCID("Frisian", "Netherlands", 0x0462);
		registerLCID("Galician", "Galician", 0x0456);
		registerLCID("Georgian", "Georgia", 0x0437);
		registerLCID("German", "Germany", 0x0407);
		registerLCID("German", "Switzerland", 0x0807);
		registerLCID("German", "Austria", 0x0c07);
		registerLCID("German", "Liechtenstein", 0x1407);
		registerLCID("German", "Luxembourg", 0x1007);
		registerLCID("Greek", "Greece", 0x0408);
		registerLCID("Greenlandic", "Greenland", 0x046f);
		registerLCID("Gujarati", "India", 0x0447);
		registerLCID("Hausa", "Nigeria", 0x0468);
		registerLCID("Hebrew", "Israel", 0x040d);
		registerLCID("Hindi", "India", 0x0439);
		registerLCID("Hungarian", "Hungary", 0x040e);
		registerLCID("Icelandic", "Iceland", 0x040f);
		registerLCID("Igbo", "Nigeria", 0x0470);
		registerLCID("Indonesian", "Indonesia", 0x0421);
		registerLCID("Inuktitut (Syllabics)", "Canada", 0x045d);
		registerLCID("Inuktitut (Latin)", "Canada", 0x085d);
		registerLCID("Irish", "Ireland", 0x083c);
		registerLCID("isiXhosa", "South Africa", 0x0434);
		registerLCID("isiZulu", "South Africa", 0x0435);
		registerLCID("Italian", "Italy", 0x0410);
		registerLCID("Italian", "Switzerland", 0x0810);
		registerLCID("Japanese", "Japan", 0x0411);
		registerLCID("Kannada", "India", 0x044b);
		registerLCID("Kazakh", "Kazakhstan", 0x043f);
		registerLCID("Khmer", "Cambodia", 0x0453);
		registerLCID("K'iche", "Guatemala", 0x0486);
		registerLCID("Kinyarwanda", "Rwanda", 0x0487);
		registerLCID("Kiswahili", "Kenya", 0x0441);
		registerLCID("Konkani", "India", 0x0457);
		registerLCID("Korean", "Korea", 0x0412);
		registerLCID("Kyrgyz", "Kyrgyzstan", 0x0440);
		registerLCID("Lao", "Lao PDR", 0x0454);
		registerLCID("Latvian", "Latvia", 0x0426);
		registerLCID("Lithuanian", "Lithuania", 0x0427);
		registerLCID("Lower Sorbian", "Germany", 0x082e);
		registerLCID("Luxembourgish", "Luxembourg", 0x046e);
		registerLCID("Macedonian (FYROM)", "Macedonia, Former Yugoslav Republic of", 0x042f);
		registerLCID("Malay", "Malaysia", 0x043e);
		registerLCID("Malay", "Brunei Darussalam", 0x083e);
		registerLCID("Malayalam", "India", 0x044c);
		registerLCID("Maltese", "Malta", 0x043a);
		registerLCID("Maori", "New Zealand", 0x0481);
		registerLCID("Mapudungun ", "Chile", 0x047a);
		registerLCID("Marathi", "India", 0x044e);
		registerLCID("Mohawk", "Mohawk", 0x047c);
		registerLCID("Mongolian (Cyrillic)", "Mongolia", 0x0450);
		registerLCID("Mongolian (Mongolian)", "People's Republic of China", 0x0850);
		registerLCID("Nepali", "Nepal", 0x0461);
		registerLCID("Norwegian (Bokmal)", "Norway", 0x0414);
		registerLCID("Norwegian (Nynorsk)", "Norway", 0x0814);
		registerLCID("Occitan", "France", 0x0482);
		registerLCID("Oriya", "India", 0x0448);
		registerLCID("Pashto", "Afghanistan", 0x0463);
		registerLCID("Persian", "Iran", 0x0429);
		registerLCID("Polish", "Poland", 0x0415);
		registerLCID("Portuguese", "Brazil", 0x0416);
		registerLCID("Portuguese", "Portugal", 0x0816);
		registerLCID("Punjabi (Gurmukhi)", "India", 0x0446);
		registerLCID("Quechua", "Bolivia", 0x046b);
		registerLCID("Quechua", "Ecuador", 0x086b);
		registerLCID("Quechua", "Peru", 0x0c6b);
		registerLCID("Romanian", "Romania", 0x0418);
		registerLCID("Romansh", "Switzerland", 0x0417);
		registerLCID("Russian", "Russia", 0x0419);
		registerLCID("Sakha", "Russia", 0x0485);
		registerLCID("Sami, Inari", "Finland", 0x243b);
		registerLCID("Sami, Lule", "Sweden", 0x143b);
		registerLCID("Sami, Lule", "Norway", 0x103b);
		registerLCID("Sami, Northern", "Norway", 0x043b);
		registerLCID("Sami, Northern", "Sweden", 0x083b);
		registerLCID("Sami, Northern", "Finland", 0x0c3b);
		registerLCID("Sami, Skolt", "Finland", 0x203b);
		registerLCID("Sami, Southern", "Norway", 0x183b);
		registerLCID("Sami, Southern", "Sweden", 0x1c3b);
		registerLCID("Sanskrit", "India", 0x044f);
		registerLCID("Scottish Gaelic", "United Kingdom", 0x0491);
		registerLCID("Serbian (Cyrillic) sr-Cyrl-CS", "Serbia and Montenegro (Former)", 0x0c1a);
		registerLCID("Serbian (Cyrillic) sr-Cyrl-RS", "Serbia", 0x281a);
		registerLCID("Serbian(Cyrillic) sr-Cyrl-ME", "Montenegro", 0x301a);
		registerLCID("Serbian (Cyrillic)", "Bosnia and Herzegovina", 0x1c1a);
		registerLCID("Serbian (Latin) sr-Latn-RS", "Serbia", 0x241a);
		registerLCID("Serbian (Latin) Sr-Latn-ME", "Montenegro", 0x2c1a);
		registerLCID("Serbian (Latin)", "Montenegro", 0x081a);
		registerLCID("Serbian (Latin)", "Bosnia and Herzegovina", 0x181a);
		registerLCID("Sesotho sa Leboa", "South Africa", 0x046c);
		registerLCID("Setswana", "South Africa", 0x0432);
		registerLCID("Sinhala", "Sri Lanka", 0x045b);
		registerLCID("Slovak", "Slovakia", 0x041b);
		registerLCID("Slovenian", "Slovenia", 0x0424);
		registerLCID("Spanish", "Mexico", 0x080a);
		registerLCID("Spanish", "Guatemala", 0x100a);
		registerLCID("Spanish", "Costa Rica", 0x140a);
		registerLCID("Spanish", "Panama", 0x180a);
		registerLCID("Spanish", "Dominican Republic", 0x1c0a);
		registerLCID("Spanish", "Venezuela", 0x200a);
		registerLCID("Spanish", "Colombia", 0x240a);
		registerLCID("Spanish", "Peru", 0x280a);
		registerLCID("Spanish", "Argentina", 0x2c0a);
		registerLCID("Spanish", "Ecuador", 0x300a);
		registerLCID("Spanish", "Chile", 0x340a);
		registerLCID("Spanish", "Paraguay", 0x3c0a);
		registerLCID("Spanish", "Bolivia", 0x400a);
		registerLCID("Spanish", "El Salvador", 0x440a);
		registerLCID("Spanish", "Honduras", 0x480a);
		registerLCID("Spanish", "Nicaragua", 0x4c0a);
		registerLCID("Spanish", "Commonwealth of Puerto Rico", 0x500a);
		registerLCID("Spanish", "United States", 0x540a);
		registerLCID("Spanish", "Uruguay", 0x380a);
		registerLCID("Spanish (International Sort)", "Spain", 0x0c0a);
		registerLCID("Spanish (Traditional Sort)", "Spain", 0x040a);
		registerLCID("Sutu", "South Africa", 0x0430);
		registerLCID("Swedish", "Sweden", 0x041d);
		registerLCID("Swedish", "Finland", 0x081d);
		registerLCID("Syriac", "Syria", 0x045a);
		registerLCID("Tajik", "Tajikistan", 0x0428);
		registerLCID("Tamazight (Latin)", "Algeria", 0x085f);
		registerLCID("Tamil", "India", 0x0449);
		registerLCID("Tatar", "Russia", 0x0444);
		registerLCID("Telugu", "India", 0x044a);
		registerLCID("Thai", "Thailand", 0x041e);
		registerLCID("Tibetan", "People's Republic of China", 0x0451);
		registerLCID("Turkish", "Turkey", 0x041f);
		registerLCID("Turkmen", "Turkmenistan", 0x0442);
		registerLCID("Uighur", "People's Republic of China", 0x0480);
		registerLCID("Ukrainian", "Ukraine", 0x0422);
		registerLCID("Upper Sorbian", "Germany", 0x042e);
		registerLCID("Urdu", "Pakistan", 0x0420);
		registerLCID("Uzbek (Cyrillic)", "Uzbekistan", 0x0843);
		registerLCID("Uzbek (Latin)", "Uzbekistan", 0x0443);
		registerLCID("Vietnamese", "Vietnam", 0x042a);
		registerLCID("Welsh", "United Kingdom", 0x0452);
		registerLCID("Wolof", "Senegal", 0x0488);
		registerLCID("Yi", "People's Republic of China", 0x0478);
		registerLCID("Yoruba", "Nigeria", 0x046a);
		registerLCID("Arabic", "", 0x0001);
		registerLCID("Bulgarian", "", 0x0002);
		registerLCID("Catalan", "", 0x0003);
		registerLCID("Czech", "", 0x0005);
		registerLCID("Danish", "", 0x0006);
		registerLCID("German", "", 0x0007);
		registerLCID("Greek", "", 0x0008);
		registerLCID("English", "", 0x0009);
		registerLCID("Spanish", "", 0x000a);
		registerLCID("Finnish", "", 0x000b);
		registerLCID("French", "", 0x000c);
		registerLCID("Hebrew", "", 0x000d);
		registerLCID("Hungarian", "", 0x000e);
		registerLCID("Icelandic", "", 0x000f);
		registerLCID("Italian", "", 0x0010);
		registerLCID("Japanese", "", 0x0011);
		registerLCID("Korean", "", 0x0012);
		registerLCID("Dutch", "", 0x0013);
		registerLCID("", "", 0x0014);
		registerLCID("Polish", "", 0x0015);
		registerLCID("Portuguese", "", 0x0016);
		registerLCID("", "", 0x0017);
		registerLCID("Romanian", "", 0x0018);
		registerLCID("Russian", "", 0x0019);
		registerLCID("Croatian", "", 0x001a);
		registerLCID("Slovak", "", 0x001b);
		registerLCID("Albanian", "", 0x001c);
		registerLCID("Swedish", "", 0x001d);
		registerLCID("Thai", "", 0x001e);
		registerLCID("Turkish", "", 0x001f);
		registerLCID("Urdu", "", 0x0020);
		registerLCID("Indonesian", "", 0x0021);
		registerLCID("Ukrainian", "", 0x0022);
		registerLCID("Belarusian", "", 0x0023);
		registerLCID("Slovenian", "", 0x0024);
		registerLCID("Estonian", "", 0x0025);
		registerLCID("Latvian", "", 0x0026);
		registerLCID("Lithuanian", "", 0x0027);		
		registerLCID("Persian", "", 0x0029);
		registerLCID("Vietnamese", "", 0x002a);
		registerLCID("Armenian", "", 0x002b);
		registerLCID("Azerbaijani", "", 0x002c);
		registerLCID("Basque", "", 0x002d);		
		registerLCID("Macedonian", "", 0x002f);
		registerLCID("Afrikaans", "", 0x0036);
		registerLCID("Georgian", "", 0x0037);
		registerLCID("Faroese", "", 0x0038);
		registerLCID("Hindi", "", 0x0039);
		registerLCID("Maltese", "", 0x003a);
		registerLCID("Irish", "", 0x003c);
		registerLCID("Malay", "", 0x003e);
		registerLCID("Kazakh", "", 0x003f);
		registerLCID("Swahili", "", 0x0041);
		registerLCID("Uzbek", "", 0x0043);
		registerLCID("Bengali", "", 0x0045);
		registerLCID("Punjabi", "", 0x0046);
		registerLCID("Gujarati", "", 0x0047);
		registerLCID("Oriya", "", 0x0048);
		registerLCID("Tamil", "", 0x0049);
		registerLCID("Telugu", "", 0x004a);
		registerLCID("Kannada", "", 0x004b);
		registerLCID("Malayalam", "", 0x004c);
		registerLCID("Assamese", "", 0x004d);
		registerLCID("Marathi", "", 0x004e);
		registerLCID("Welsh", "", 0x0052);
		registerLCID("Khmer", "", 0x0053);
		registerLCID("Galician", "", 0x0056);
		registerLCID("Konkani", "", 0x0057);
		registerLCID("Sinhala", "", 0x005b);
		registerLCID("Amharic", "", 0x005e);
		registerLCID("Nepali", "", 0x0061);
		registerLCID("Pashto", "", 0x0063);
		registerLCID("Hausa", "", 0x0068);
		registerLCID("Kalaallisut", "", 0x006f);
		registerLCID("Sichuan yi", "", 0x0078);
		registerLCID("Tigrinya", "", 0x0473);
		registerLCID("Hawaiian", "", 0x0475);
		registerLCID("Somali", "", 0x0477);
		registerLCID("Urdu", "", 0x0820);
		registerLCID("Nepali", "", 0x0861);
		registerLCID("Tigrinya", "", 0x0873);
		registerLCID("French", "SN", 0x280c);
		registerLCID("English", "HK", 0x3c09);
		registerLCID("Serbian", "Cyrillic", 0x6c1a);
		registerLCID("Serbian", "Latin", 0x701a);
		registerLCID("Azerbaijani", "Cyrillic", 0x742c);
		registerLCID("Chinese", "", 0x7804);
		registerLCID("Norwegian", "nynorsk", 0x7814);
		registerLCID("Azerbaijani", "Latin", 0x782c);
		registerLCID("Uzbek", "Cyrillic", 0x7843);
		registerLCID("Norwegian", "bokmol", 0x7c14);
		registerLCID("Serbian", "", 0x7c1a);
		registerLCID("Uzbek", "Latin", 0x7c43);
		registerLCID("Hausa", "Latin", 0x7c68);
		registerLCID("Tajik", "", 0x0028);
		registerLCID("Sorbian", "", 0x002e);
		registerLCID("Setswana", "", 0x0032);
		registerLCID("isiXhosa", "", 0x0034);
		registerLCID("isiZulu", "", 0x0035);
		registerLCID("Sami", "", 0x003b);
		registerLCID("Kyrgyz", "", 0x0040);
		registerLCID("Turkmen", "", 0x0042);
		registerLCID("Tatar", "", 0x0044);
		registerLCID("Sanskrit", "", 0x004f);
		registerLCID("Mongolian", "", 0x0050);
		registerLCID("Tibetan", "", 0x0051);
		registerLCID("Lao", "", 0x0054);
		registerLCID("Syriac", "", 0x005a);
		registerLCID("Inuktitut", "", 0x005d);
		registerLCID("Tamazight", "", 0x005f);
		registerLCID("Frisian", "", 0x0062);
		registerLCID("Filipino", "", 0x0064);
		registerLCID("Divehi", "", 0x0065);
		registerLCID("Yoruba", "", 0x006a);
		registerLCID("Quechua", "", 0x006b);
		registerLCID("Northern Sotho", "", 0x006c);
		registerLCID("Bashkir", "", 0x006d);
		registerLCID("Luxembourgish", "", 0x006e);
		registerLCID("Igbo", "", 0x0070);
		registerLCID("Mapudungun", "", 0x007a);
		registerLCID("Mohawk", "", 0x007c);
		registerLCID("Breton", "", 0x007e);
		registerLCID("Uighur", "", 0x0080);
		registerLCID("Maori", "", 0x0081);
		registerLCID("Occitan", "", 0x0082);
		registerLCID("Corsican", "", 0x0083);
		registerLCID("Alsatian", "", 0x0084);
		registerLCID("Yakut", "", 0x0085);
		registerLCID("K'iche", "", 0x0086);
		registerLCID("Kinyarwanda", "", 0x0087);
		registerLCID("Wolof", "", 0x0088);
		registerLCID("Dari", "", 0x008c);
		registerLCID("Gaelic", "", 0x0091);
		registerLCID("Tsonga", "", 0x0431);
		registerLCID("Venda", "South Africa", 0x0433);
		registerLCID("Burmese", "Myanmar", 0x0455);
		registerLCID("Manipuri", "India", 0x0458);
		registerLCID("Sindhi", "India", 0x0459);
		registerLCID("Cherokee", "United States", 0x045c);
		registerLCID("Tamazight", "Morocco", 0x045f);
		registerLCID("Edo", "Nigeria", 0x0466);
		registerLCID("Fulfulde", "Nigeria", 0x0467);
		registerLCID("Ibibio", "Nigeria", 0x0469);
		registerLCID("Kanuri", "Nigeria", 0x0471);
		registerLCID("West Central Oromo", "Ethiopia", 0x0472);
		registerLCID("Guarani", "Paraguay", 0x0474);
		registerLCID("Papiamento", "Netherlands Antilles", 0x0479);
		registerLCID("Plateau Malagasy", "Madagascar", 0x048d);
		registerLCID("Romanian", "Macao", 0x0818);
		registerLCID("Russian", "Macao", 0x0819);
		registerLCID("Panjabi", "Pakistan", 0x0846);
		registerLCID("Tibetan", "Bhutan", 0x0851);
		registerLCID("Sindhi", "Pakistan", 0x0859);
		registerLCID("Tamanaku", "Morocco", 0x0c5f);
		registerLCID("French", "", 0x1c0c);
		registerLCID("French", "Reunion", 0x200c);
		registerLCID("French", "Congo", 0x240c);
		registerLCID("French", "Cameroon", 0x2c0c);
		registerLCID("French", "Cote d'Ivoire", 0x300c);
		registerLCID("French", "Mali", 0x340c);
		registerLCID("English", "Indonesia", 0x3809);
		registerLCID("French", "Morocco", 0x380c);
		registerLCID("French", "Haiti", 0x3c0c);
		registerLCID("Bosnian", "Cyrillic", 0x641a);
		registerLCID("Bosnian", "Latin", 0x681a);
		registerLCID("Inari Sami", "", 0x703b);
		registerLCID("Skolt Sami", "", 0x743b);
		registerLCID("Bosnian", "", 0x781a);
		registerLCID("Southern Sami", "", 0x783b);
		registerLCID("Mongolian", "Cyrillic", 0x7850);
		registerLCID("Inuktitut", "Unified Canadian Aboriginal Syllabics", 0x785d);
		registerLCID("Tajik", "Cyrillic", 0x7c28);
		registerLCID("Lower Sorbian", "", 0x7c2e);
		registerLCID("Lule Sami", "", 0x7c3b);
		registerLCID("Mongolian", "Mongolia", 0x7c50);
		registerLCID("Inuktitut", "Latin", 0x7c5d);
		registerLCID("Central Atlas Tamazight", "Latin", 0x7c5f);	
		registerLCID("Greek 2", "Greece", 0x2008);
		registerLCID("Lithuanian", "Lithuania", 0x0827);
		registerLCID("Sutu", "", 0x0030);
		registerLCID("Gaelic", "Scotland", 0x043c);
		registerLCID("Sindhi", "", 0x0059);
		registerLCID("Somali", "", 0x0077);
		
		// Language tags
		registerTag(0x0001, "ar");
		registerTag(0x0002, "bg");
		registerTag(0x0003, "ca");
		registerTag(0x0004, "zh");		
		registerTag(0x0005, "cs");
		registerTag(0x0006, "da");
		registerTag(0x0007, "de");		
		registerTag(0x0008, "el");
		registerTag(0x0009, "en");
		registerTag(0x000a, "es");
		registerTag(0x000b, "fi");
		registerTag(0x000c, "fr");
		registerTag(0x000d, "he");
		registerTag(0x000e, "hu");
		registerTag(0x000f, "is");
		registerTag(0x0010, "it");
		registerTag(0x0011, "ja");
		registerTag(0x0012, "ko");
		registerTag(0x0013, "nl");
		registerTag(0x0014, "nb");
		registerTag(0x0015, "pl");
		registerTag(0x0016, "pt");
		registerTag(0x0017, "rm");
		registerTag(0x0018, "ro");
		registerTag(0x0019, "ru");
		registerTag(0x001a, "hr");
		registerTag(0x001b, "sk");
		registerTag(0x001c, "sq");
		registerTag(0x001d, "sv");
		registerTag(0x001e, "th");
		registerTag(0x001f, "tr");
		registerTag(0x0020, "ur");
		registerTag(0x0021, "id");
		registerTag(0x0022, "uk");
		registerTag(0x0023, "be");
		registerTag(0x0024, "sl");
		registerTag(0x0025, "et");
		registerTag(0x0026, "lv");
		registerTag(0x0027, "lt");
		registerTag(0x0028, "tg");
		registerTag(0x0029, "fa");
		registerTag(0x002a, "vi");
		registerTag(0x002b, "hy");
		registerTag(0x002c, "az");
		registerTag(0x002d, "eu");
		registerTag(0x002e, "hsb");
		registerTag(0x002f, "mk");
		registerTag(0x0032, "tn");
		//registerTag(0x0033, "ven");
		registerTag(0x0034, "xh");
		registerTag(0x0035, "zu");
		registerTag(0x0036, "af");
		registerTag(0x0037, "ka");
		registerTag(0x0038, "fo");
		registerTag(0x0039, "hi");
		registerTag(0x003a, "mt");
		registerTag(0x003b, "se");
		registerTag(0x003c, "ga");
		registerTag(0x003e, "ms");
		registerTag(0x003f, "kk");
		registerTag(0x0040, "ky");
		registerTag(0x0041, "sw");
		registerTag(0x0042, "tk");
		registerTag(0x0043, "uz");
		registerTag(0x0044, "tt");
		registerTag(0x0045, "bn");
		registerTag(0x0046, "pa");
		registerTag(0x0047, "gu");
		registerTag(0x0048, "or");
		registerTag(0x0049, "ta");
		registerTag(0x004a, "te");
		registerTag(0x004b, "kn");
		registerTag(0x004c, "ml");
		registerTag(0x004d, "as");
		registerTag(0x004e, "mr");
		registerTag(0x004f, "sa");
		registerTag(0x0050, "mn");
		registerTag(0x0051, "bo");
		registerTag(0x0052, "cy");
		registerTag(0x0053, "km");
		registerTag(0x0054, "lo");
		registerTag(0x0056, "gl");
		registerTag(0x0057, "kok");
		registerTag(0x005a, "syr");
		registerTag(0x005b, "si");
		registerTag(0x005d, "iu");
		registerTag(0x005e, "am");
		registerTag(0x005f, "tzm");
		registerTag(0x0061, "ne");
		registerTag(0x0062, "fy");
		registerTag(0x0063, "ps");
		registerTag(0x0064, "fil");
		registerTag(0x0065, "dv");
		registerTag(0x0068, "ha");
		registerTag(0x006a, "yo");
		registerTag(0x006b, "quz");
		registerTag(0x006c, "nso");
		registerTag(0x006d, "ba");
		registerTag(0x006e, "lb");
		registerTag(0x006f, "kl");
		registerTag(0x0070, "ig");
		registerTag(0x0078, "ii");
		registerTag(0x007a, "arn");
		registerTag(0x007c, "moh");
		registerTag(0x007e, "br");
		registerTag(0x0080, "ug");
		registerTag(0x0081, "mi");
		registerTag(0x0082, "oc");
		registerTag(0x0083, "co");
		registerTag(0x0084, "gsw");
		registerTag(0x0085, "sah");
		registerTag(0x0086, "qut");
		registerTag(0x0087, "rw");
		registerTag(0x0088, "wo");
		registerTag(0x008c, "prs");
		registerTag(0x0091, "gd");
		registerTag(0x0401, "ar-SA");
		registerTag(0x0402, "bg-BG");
		registerTag(0x0403, "ca-ES");
		registerTag(0x0404, "zh-TW");
		//registerTag(0x0004, "zh-Hans");
		registerTag(0x7c04, "zh-Hant");
		registerTag(0x0405, "cs-CZ");
		registerTag(0x0406, "da-DK");		
		registerTag(0x0407, "de-DE");
		registerTag(0x0408, "el-GR");		
		registerTag(0x0409, "en-US");		
		registerTag(0x040A, "es-ES_tradnl");
		registerTag(0x040B, "fi-FI");
		registerTag(0x040C, "fr-FR");
		registerTag(0x040D, "he-IL");
		registerTag(0x040E, "hu-HU");
		registerTag(0x040F, "is-IS");
		registerTag(0x0410, "it-IT");
		registerTag(0x0411, "ja-JP");
		registerTag(0x0412, "ko-KR");
		registerTag(0x0413, "nl-NL");
		registerTag(0x0414, "nb-NO");
		registerTag(0x0415, "pl-PL");
		registerTag(0x0416, "pt-BR");
		registerTag(0x0417, "rm-CH");
		registerTag(0x0418, "ro-RO");
		registerTag(0x0419, "ru-RU");
		registerTag(0x041A, "hr-HR");
		registerTag(0x041B, "sk-SK");
		registerTag(0x041C, "sq-AL");
		registerTag(0x041D, "sv-SE");
		registerTag(0x041E, "th-TH");
		registerTag(0x041F, "tr-TR");
		registerTag(0x0420, "ur-PK");
		registerTag(0x0421, "id-ID");
		registerTag(0x0422, "uk-UA");
		registerTag(0x0423, "be-BY");
		registerTag(0x0424, "sl-SI");
		registerTag(0x0425, "et-EE");
		registerTag(0x0426, "lv-LV");
		registerTag(0x0427, "lt-LT");
		registerTag(0x0428, "tg-Cyrl-TJ");
		registerTag(0x0429, "fa-IR");
		registerTag(0x042A, "vi-VN");
		registerTag(0x042B, "hy-AM");
		registerTag(0x042C, "az-Latn-AZ");
		registerTag(0x042D, "eu-ES");
		registerTag(0x042E, "wen-DE");
		registerTag(0x042F, "mk-MK");
		registerTag(0x0430, "st-ZA");
		registerTag(0x0431, "ts-ZA");
		registerTag(0x0432, "tn-ZA");
		registerTag(0x0433, "ven-ZA");
		registerTag(0x0434, "xh-ZA");
		registerTag(0x0435, "zu-ZA");
		registerTag(0x0436, "af-ZA");
		registerTag(0x0437, "ka-GE");
		registerTag(0x0438, "fo-FO");
		registerTag(0x0439, "hi-IN");
		registerTag(0x043A, "mt-MT");
		registerTag(0x043B, "se-NO");
		registerTag(0x043E, "ms-MY");
		registerTag(0x043F, "kk-KZ");
		registerTag(0x0440, "ky-KG");
		registerTag(0x0441, "sw-KE");
		registerTag(0x0442, "tk-TM");
		registerTag(0x0443, "uz-Latn-UZ");
		registerTag(0x0444, "tt-RU");
		registerTag(0x0445, "bn-IN");
		registerTag(0x0446, "pa-IN");
		registerTag(0x0447, "gu-IN");
		registerTag(0x0448, "or-IN");
		registerTag(0x0449, "ta-IN");
		registerTag(0x044A, "te-IN");
		registerTag(0x044B, "kn-IN");
		registerTag(0x044C, "ml-IN");
		registerTag(0x044D, "as-IN");
		registerTag(0x044E, "mr-IN");
		registerTag(0x044F, "sa-IN");
		registerTag(0x0450, "mn-MN");
		registerTag(0x0451, "bo-CN");
		registerTag(0x0452, "cy-GB");
		registerTag(0x0453, "km-KH");
		registerTag(0x0454, "lo-LA");
		registerTag(0x0455, "my-MM");
		registerTag(0x0456, "gl-ES");
		registerTag(0x0457, "kok-IN");
		registerTag(0x0458, "mni");
		registerTag(0x0459, "sd-IN");
		registerTag(0x045A, "syr-SY");
		registerTag(0x045B, "si-LK");
		registerTag(0x045C, "chr-US");
		registerTag(0x045D, "iu-Cans-CA");
		registerTag(0x045E, "am-ET");
		registerTag(0x045F, "tmz");
		registerTag(0x0461, "ne-NP");
		registerTag(0x0462, "fy-NL");
		registerTag(0x0463, "ps-AF");
		registerTag(0x0464, "fil-PH");
		registerTag(0x0465, "dv-MV");
		registerTag(0x0466, "bin-NG");
		registerTag(0x0467, "fuv-NG");
		registerTag(0x0468, "ha-Latn-NG");
		registerTag(0x0469, "ibb-NG");
		registerTag(0x046A, "yo-NG");
		registerTag(0x046B, "quz-BO");
		registerTag(0x046C, "nso-ZA");
		registerTag(0x046D, "ba-RU");
		registerTag(0x046E, "lb-LU");
		registerTag(0x046F, "kl-GL");
		registerTag(0x0470, "ig-NG");
		registerTag(0x0471, "kr-NG");
		registerTag(0x0472, "gaz-ET");
		registerTag(0x0473, "ti-ER");
		registerTag(0x0474, "gn-PY");
		registerTag(0x0475, "haw-US");
		registerTag(0x0477, "so-SO");
		registerTag(0x0478, "ii-CN");
		registerTag(0x0479, "pap-AN");
		registerTag(0x047A, "arn-CL");
		registerTag(0x047C, "moh-CA");
		registerTag(0x047E, "br-FR");
		registerTag(0x0480, "ug-CN");
		registerTag(0x0481, "mi-NZ");
		registerTag(0x0482, "oc-FR");
		registerTag(0x0483, "co-FR");
		registerTag(0x0484, "gsw-FR");
		registerTag(0x0485, "sah-RU");
		registerTag(0x0486, "qut-GT");
		registerTag(0x0487, "rw-RW");
		registerTag(0x0488, "wo-SN");
		registerTag(0x048C, "prs-AF");
		registerTag(0x048D, "plt-MG");
		registerTag(0x0491, "gd-GB");
		registerTag(0x0801, "ar-IQ");
		registerTag(0x0804, "zh-CN");
		registerTag(0x0807, "de-CH");
		registerTag(0x0809, "en-GB");
		registerTag(0x080A, "es-MX");
		registerTag(0x080C, "fr-BE");
		registerTag(0x0810, "it-CH");
		registerTag(0x0813, "nl-BE");
		registerTag(0x0814, "nn-NO");
		registerTag(0x0816, "pt-PT");
		registerTag(0x0818, "ro-MO");
		registerTag(0x0819, "ru-MO");
		registerTag(0x081A, "sr-Latn-CS");
		registerTag(0x081D, "sv-FI");
		registerTag(0x0820, "ur-IN");
		registerTag(0x082C, "az-Cyrl-AZ");
		registerTag(0x082E, "dsb-DE");
		registerTag(0x083B, "se-SE");
		registerTag(0x083C, "ga-IE");
		registerTag(0x083E, "ms-BN");
		registerTag(0x0843, "uz-Cyrl-UZ");
		registerTag(0x0845, "bn-BD");
		registerTag(0x0846, "pa-PK");
		registerTag(0x0850, "mn-Mong-CN");
		registerTag(0x0851, "bo-BT");
		registerTag(0x0859, "sd-PK");
		registerTag(0x085D, "iu-Latn-CA");
		registerTag(0x085F, "tzm-Latn-DZ");
		registerTag(0x0861, "ne-IN");
		registerTag(0x086B, "quz-EC");
		registerTag(0x0873, "ti-ET");
		registerTag(0x0C01, "ar-EG");
		registerTag(0x0C04, "zh-HK");
		registerTag(0x0C07, "de-AT");
		registerTag(0x0C09, "en-AU");
		registerTag(0x0C0A, "es-ES");
		registerTag(0x0C0C, "fr-CA");
		registerTag(0x0C1A, "sr-Cyrl-CS");
		registerTag(0x0C3B, "se-FI");
		registerTag(0x0C5F, "tmz-MA");
		registerTag(0x0C6B, "quz-PE");
		registerTag(0x1001, "ar-LY");
		registerTag(0x1004, "zh-SG");
		registerTag(0x1007, "de-LU");
		registerTag(0x1009, "en-CA");
		registerTag(0x100A, "es-GT");
		registerTag(0x100C, "fr-CH");
		registerTag(0x101A, "hr-BA");
		registerTag(0x103B, "smj-NO");
		registerTag(0x1401, "ar-DZ");
		registerTag(0x1404, "zh-MO");
		registerTag(0x1407, "de-LI");
		registerTag(0x1409, "en-NZ");
		registerTag(0x140A, "es-CR");
		registerTag(0x140C, "fr-LU");
		registerTag(0x141A, "bs-Latn-BA");
		registerTag(0x143B, "smj-SE");
		registerTag(0x1801, "ar-MA");
		registerTag(0x1809, "en-IE");
		registerTag(0x180A, "es-PA");
		registerTag(0x180C, "fr-MC");
		registerTag(0x181A, "sr-Latn-BA");
		registerTag(0x183B, "sma-NO");
		registerTag(0x1C01, "ar-TN");
		registerTag(0x1C09, "en-ZA");
		registerTag(0x1C0A, "es-DO");
		registerTag(0x1C0C, "fr-West-Indies");
		registerTag(0x1C1A, "sr-Cyrl-BA");
		registerTag(0x1C3B, "sma-SE");
		registerTag(0x2001, "ar-OM");
		registerTag(0x2009, "en-JM");
		registerTag(0x200A, "es-VE");
		registerTag(0x200C, "fr-RE");
		registerTag(0x201A, "bs-Cyrl-BA");
		registerTag(0x203B, "sms-FI");
		registerTag(0x2401, "ar-YE");
		registerTag(0x2409, "en-CB");
		registerTag(0x240A, "es-CO");
		registerTag(0x240C, "fr-CG");
		registerTag(0x241a, "sr-Latn-RS");
		registerTag(0x243B, "smn-FI");
		registerTag(0x2801, "ar-SY");
		registerTag(0x2809, "en-BZ");
		registerTag(0x280A, "es-PE");
		registerTag(0x280C, "fr-SN");
		registerTag(0x281a, "sr-Cyrl-RS");
		registerTag(0x2C01, "ar-JO");
		registerTag(0x2C09, "en-TT");
		registerTag(0x2C0A, "es-AR");
		registerTag(0x2C0C, "fr-CM");
		registerTag(0x2c1a, "sr-Latn-ME");
		registerTag(0x3001, "ar-LB");
		registerTag(0x3009, "en-ZW");
		registerTag(0x300A, "es-EC");
		registerTag(0x300C, "fr-CI");
		registerTag(0x301a, "sr-Cyrl-ME");
		registerTag(0x3401, "ar-KW");
		registerTag(0x3409, "en-PH");
		registerTag(0x340A, "es-CL");
		registerTag(0x340C, "fr-ML");
		registerTag(0x3801, "ar-AE");
		registerTag(0x3809, "en-ID");
		registerTag(0x380A, "es-UY");
		registerTag(0x380C, "fr-MA");
		registerTag(0x3C01, "ar-BH");
		registerTag(0x3C09, "en-HK");
		registerTag(0x3c0a, "es-PY");
		registerTag(0x3C0C, "fr-HT");
		registerTag(0x4001, "ar-QA");
		registerTag(0x4009, "en-IN");
		registerTag(0x400A, "es-BO");
		registerTag(0x4409, "en-MY");
		registerTag(0x440A, "es-SV");
		registerTag(0x4809, "en-SG");
		registerTag(0x480A, "es-HN");
		registerTag(0x4C0A, "es-NI");
		registerTag(0x500A, "es-PR");
		registerTag(0x540A, "es-US");
		registerTag(0x641a, "bs-Cyrl");
		registerTag(0x681a, "bs-Latn");
		registerTag(0x6c1a, "sr-Cyrl");
		registerTag(0x701a, "sr-Latn");
		registerTag(0x703b, "smn");
		registerTag(0x742c, "az-Cyrl");
		registerTag(0x743b, "sms");
		registerTag(0x7804, "zh");
		registerTag(0x7814, "nn");
		registerTag(0x781a, "bs");
		registerTag(0x782c, "az-Latn");
		registerTag(0x783b, "sma");
		registerTag(0x7843, "uz-Cyrl");
		registerTag(0x7850, "mn-Cyrl");
		registerTag(0x785d, "iu-Cans");
		registerTag(0x7c14, "nb");
		registerTag(0x7c1a, "sr");
		registerTag(0x7c28, "tg-Cyrl");
		registerTag(0x7c2e, "dsb");
		registerTag(0x7c3b, "smj");
		registerTag(0x7c43, "uz-Latn");
		registerTag(0x7c50, "mn-Mong");
		registerTag(0x7c5d, "iu-Latn");
		registerTag(0x7c5f, "tzm-Latn");
		registerTag(0x7c68, "ha-Latn");		
		registerTag(0x2008, "el-GR");
		registerTag(0x0827, "lt-LT");
		registerTag(0x0030, "st");
		registerTag(0x043c, "gd");
		registerTag(0x0059, "sd");
		registerTag(0x0077, "so");		
	}

	/**
	 * Gets a list of all descriptors for the registered LCID.
	 * @return a list of all descriptors for the registered LCID.
	 */
	public static List<LCIDDescr> getDescriptors () {
		return descriptors;
	}

	/**
	 * Gets the language tag for a given LCID.
	 * @param lcid the LCID value.
	 * @return the language tag found or an empty string
	 */
	public static String getTag (int lcid) {
		LCIDDescr descr = tagLookup.get(lcid); 
		return descr != null ? descr.tag : "";
	}
	
	/**
	 * Gets an LCID for a given language tag.
	 * @param tag the language tag to lookup.
	 * @return the LCID value found.
	 */
	public static int getLCID (String tag) {
		return getLCID(LocaleId.fromString(tag));
	}
	
	/**
	 * Gets the language tag for a given locale id.
	 * @param locId the locale id to lookup.
	 * @return the language tag found, or an empty string.
	 */
	public static String getTag (LocaleId locId) {
		LCIDDescr descr = lcidLookup.get(locId.toString());
		return descr != null ? descr.tag : "";
	}
	
	/**
	 * Gets the LCID for a given locale id.
	 * @param locId the locale id to lookup.
	 * @return the LCID value found, or 0 if nothing is found.
	 */
	public static int getLCID (LocaleId locId) {
		LCIDDescr descr = lcidLookup.get(locId.toString()); 
		return descr != null ? descr.lcid : 0;
	}
	
	/**
	 * Gets the LCID as a string, for a given locale id.
	 * @param locId the localie id to lookup.
	 * @return the LCID found, as a string.
	 */
	public static String getLCID_asString (LocaleId locId) {
		return Util.intToStr(getLCID(locId));
	}
	
	/**
	 * Creates a locale id for a given language tag.
	 * <p>This method is the same as calling <code>new LocaleId(tag)</code>.
	 * @param tag the language tag.
	 * @return a new locale id for the given language tag.
	 */
	public static LocaleId getLocaleId (String tag) {
		return new LocaleId(tag);
	}
	
	/**
	 * Creates a locale id for a given LCID value.
	 * @param lcid the LCID to lookup.
	 * @return a new locale id for the given LCID value.
	 */
	public static LocaleId getLocaleId (int lcid) {
		return new LocaleId(getTag(lcid));
	}

	public static HashMap<Integer, LCIDDescr> getTagLookup() {
		return tagLookup;
	}

	public static HashMap<String, LCIDDescr> getLcidLookup() {
		return lcidLookup;
	}
}
