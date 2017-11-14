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

import java.util.Hashtable;

/**
 * Helper class to handle HTML character entities.
 */
public class HTMLCharacterEntities {

	private Hashtable<String, Character> charEntities;

	/**
	 * Ensures the lookup table is initialized. You must call this method before
	 * using others.
	 * @param includeSpecialChars true if XML-pre-defined entities should be
	 * included in the list of supported entities.
	 */
	public void ensureInitialization (boolean includeSpecialChars) {
		if ( charEntities == null ) {
			createCharEntitiesTable(includeSpecialChars);
		}
	}
	
	/**
	 * Gets the character for a given character entity reference (e.g. "&aacute;").
	 * @param ref the reference to lookup.
	 * @return the unicode value for the given reference, or -1 if it was not found.
	 */
	public int lookupReference (String ref) {
		if (( ref == null ) || ( ref.length() < 3 )) {
			return -1;
		}
		return lookupName(ref.substring(1, ref.length()-1));
	}
	
	/**
	 * Gets the character for a given entity name (e.g. "aacute").
	 * @param name the name to lookup.
	 * @return the unicode value for the given name, or -1 if it was not found.
	 */
	public int lookupName (String name) {
		if ( charEntities.containsKey(name) ) {
			return (int)charEntities.get(name);
		}
		// Else: un-known name
		return -1;
	}

	/**
	 * Gets the entity name for the given character.
	 * @param value the character to lookup.
	 * @return the name for the given character, or null if it was not found.
	 */
	public String getName (char value) {
		for ( String key : charEntities.keySet() ) {
			if ( value == charEntities.get(key) ) {
				return key;
			}
		}
		return null;
	}
	
	private void createCharEntitiesTable (boolean includeSpecialChars) {
		charEntities = new Hashtable<String, Character>();
		// XML pre-defined
		if ( includeSpecialChars ) {
			charEntities.put("amp", '&');
			charEntities.put("lt", '<');
			charEntities.put("apos", '\'');
			charEntities.put("gt", '>');
			charEntities.put("quot", '"');
		}
		// Others
		charEntities.put("nbsp", '\u00a0');
		charEntities.put("iexcl", '\u00a1');
		charEntities.put("cent", '\u00a2');
		charEntities.put("pound", '\u00a3');
		charEntities.put("curren", '\u00a4');
		charEntities.put("yen", '\u00a5');
		charEntities.put("brvbar", '\u00a6');
		charEntities.put("sect", '\u00a7');
		charEntities.put("uml", '\u00a8');
		charEntities.put("copy", '\u00a9');
		charEntities.put("ordf", '\u00aa');
		charEntities.put("laquo", '\u00ab');
		charEntities.put("not", '\u00ac');
		charEntities.put("shy", '\u00ad');
		charEntities.put("reg", '\u00ae');
		charEntities.put("macr", '\u00af');
		charEntities.put("deg", '\u00b0');
		charEntities.put("plusmn", '\u00b1');
		charEntities.put("sup2", '\u00b2');
		charEntities.put("sup3", '\u00b3');
		charEntities.put("acute", '\u00b4');
		charEntities.put("micro", '\u00b5');
		charEntities.put("para", '\u00b6');
		charEntities.put("middot", '\u00b7');
		charEntities.put("cedil", '\u00b8');
		charEntities.put("sup1",'\u00b9');
		charEntities.put("ordm", '\u00ba');
		charEntities.put("raquo", '\u00bb');
		charEntities.put("frac14", '\u00bc');
		charEntities.put("frac12", '\u00bd');
		charEntities.put("frac34", '\u00be');
		charEntities.put("iquest", '\u00bf');
		charEntities.put("Agrave", '\u00c0');
		charEntities.put("Aacute", '\u00c1');
		charEntities.put("Acirc", '\u00c2');
		charEntities.put("Atilde", '\u00c3');
		charEntities.put("Auml", '\u00c4');
		charEntities.put("Aring", '\u00c5');
		charEntities.put("AElig", '\u00c6');
		charEntities.put("Ccedil", '\u00c7');
		charEntities.put("Egrave", '\u00c8');
		charEntities.put("Eacute", '\u00c9');
		charEntities.put("Ecirc", '\u00ca');
		charEntities.put("Euml", '\u00cb');
		charEntities.put("Igrave", '\u00cc');
		charEntities.put("Iacute", '\u00cd');
		charEntities.put("Icirc", '\u00ce');
		charEntities.put("Iuml", '\u00cf');
		charEntities.put("ETH", '\u00d0');
		charEntities.put("Ntilde", '\u00d1');
		charEntities.put("Ograve", '\u00d2');
		charEntities.put("Oacute", '\u00d3');
		charEntities.put("Ocirc", '\u00d4');
		charEntities.put("Otilde", '\u00d5');
		charEntities.put("Ouml", '\u00d6');
		charEntities.put("times", '\u00d7');
		charEntities.put("Oslash", '\u00d8');
		charEntities.put("Ugrave", '\u00d9');
		charEntities.put("Uacute", '\u00da');
		charEntities.put("Ucirc", '\u00db');
		charEntities.put("Uuml", '\u00dc');
		charEntities.put("Yacute", '\u00dd');
		charEntities.put("THORN", '\u00de');
		charEntities.put("szlig", '\u00df');
		charEntities.put("agrave", '\u00e0');
		charEntities.put("aacute", '\u00e1');
		charEntities.put("acirc", '\u00e2');
		charEntities.put("atilde", '\u00e3');
		charEntities.put("auml", '\u00e4');
		charEntities.put("aring", '\u00e5');
		charEntities.put("aelig", '\u00e6');
		charEntities.put("ccedil", '\u00e7');
		charEntities.put("egrave", '\u00e8');
		charEntities.put("eacute", '\u00e9');
		charEntities.put("ecirc", '\u00ea');
		charEntities.put("euml", '\u00eb');
		charEntities.put("igrave", '\u00ec');
		charEntities.put("iacute", '\u00ed');
		charEntities.put("icirc", '\u00ee');
		charEntities.put("iuml", '\u00ef');
		charEntities.put("eth", '\u00f0');
		charEntities.put("ntilde", '\u00f1');
		charEntities.put("ograve", '\u00f2');
		charEntities.put("oacute", '\u00f3');
		charEntities.put("ocirc", '\u00f4');
		charEntities.put("otilde", '\u00f5');
		charEntities.put("ouml", '\u00f6');
		charEntities.put("divide", '\u00f7');
		charEntities.put("oslash", '\u00f8');
		charEntities.put("ugrave", '\u00f9');
		charEntities.put("uacute", '\u00fa');
		charEntities.put("ucirc", '\u00fb');
		charEntities.put("uuml", '\u00fc');
		charEntities.put("yacute", '\u00fd');
		charEntities.put("thorn", '\u00fe');
		charEntities.put("yuml", '\u00ff');
		charEntities.put("OElig", '\u0152');
		charEntities.put("oelig", '\u0153');
		charEntities.put("Scaron", '\u0160');
		charEntities.put("scaron", '\u0161');
		charEntities.put("Yuml", '\u0178');
		charEntities.put("circ", '\u02c6');
		charEntities.put("tilde", '\u02dc');
		charEntities.put("ensp", '\u2002');
		charEntities.put("emsp", '\u2003');
		charEntities.put("thinsp", '\u2009');
		charEntities.put("zwnj", '\u200c');
		charEntities.put("zwj", '\u200d');
		charEntities.put("lrm", '\u200e');
		charEntities.put("rlm", '\u200f');
		charEntities.put("ndash", '\u2013');
		charEntities.put("mdash", '\u2014');
		charEntities.put("lsquo", '\u2018');
		charEntities.put("rsquo", '\u2019');
		charEntities.put("sbquo", '\u201a');
		charEntities.put("ldquo", '\u201c');
		charEntities.put("rdquo", '\u201d');
		charEntities.put("bdquo", '\u201e');
		charEntities.put("dagger", '\u2020');
		charEntities.put("Dagger", '\u2021');
		charEntities.put("permil", '\u2030');
		charEntities.put("lsaquo", '\u2039');
		charEntities.put("rsaquo", '\u203a');
		charEntities.put("euro", '\u20ac');
		charEntities.put("fnof", '\u0192');
		charEntities.put("Alpha", '\u0391');
		charEntities.put("Beta", '\u0392');
		charEntities.put("Gamma", '\u0393');
		charEntities.put("Delta", '\u0394');
		charEntities.put("Epsilon", '\u0395');
		charEntities.put("Zeta", '\u0396');
		charEntities.put("Eta", '\u0397');
		charEntities.put("Theta", '\u0398');
		charEntities.put("Iota", '\u0399');
		charEntities.put("Kappa", '\u039a');
		charEntities.put("Lambda", '\u039b');
		charEntities.put("Mu", '\u039c');
		charEntities.put("Nu", '\u039d');
		charEntities.put("Xi", '\u039e');
		charEntities.put("Omicron", '\u039f');
		charEntities.put("Pi", '\u03a0');
		charEntities.put("Rho", '\u03a1');
		charEntities.put("Sigma", '\u03a3');
		charEntities.put("Tau", '\u03a4');
		charEntities.put("Upsilon", '\u03a5');
		charEntities.put("Phi", '\u03a6');
		charEntities.put("Chi", '\u03a7');
		charEntities.put("Psi", '\u03a8');
		charEntities.put("Omega", '\u03a9');
		charEntities.put("alpha", '\u03b1');
		charEntities.put("beta", '\u03b2');
		charEntities.put("gamma", '\u03b3');
		charEntities.put("delta", '\u03b4');
		charEntities.put("epsilon", '\u03b5');
		charEntities.put("zeta", '\u03b6');
		charEntities.put("eta", '\u03b7');
		charEntities.put("theta", '\u03b8');
		charEntities.put("iota", '\u03b9');
		charEntities.put("kappa", '\u03ba');
		charEntities.put("lambda", '\u03bb');
		charEntities.put("mu", '\u03bc');
		charEntities.put("nu", '\u03bd');
		charEntities.put("xi", '\u03be');
		charEntities.put("omicron", '\u03bf');
		charEntities.put("pi", '\u03c0');
		charEntities.put("rho", '\u03c1');
		charEntities.put("sigmaf", '\u03c2');
		charEntities.put("sigma", '\u03c3');
		charEntities.put("tau", '\u03c4');
		charEntities.put("upsilon", '\u03c5');
		charEntities.put("phi", '\u03c6');
		charEntities.put("chi", '\u03c7');
		charEntities.put("psi", '\u03c8');
		charEntities.put("omega", '\u03c9');
		charEntities.put("thetasym", '\u03d1');
		charEntities.put("upsih", '\u03d2');
		charEntities.put("piv", '\u03d6');
		charEntities.put("bull", '\u2022');
		charEntities.put("hellip", '\u2026');
		charEntities.put("prime", '\u2032');
		charEntities.put("Prime", '\u2033');
		charEntities.put("oline", '\u203e');
		charEntities.put("frasl", '\u2044');
		charEntities.put("weierp", '\u2118');
		charEntities.put("image", '\u2111');
		charEntities.put("real", '\u211c');
		charEntities.put("trade", '\u2122');
		charEntities.put("alefsym", '\u2135');
		charEntities.put("larr", '\u2190');
		charEntities.put("uarr", '\u2191');
		charEntities.put("rarr", '\u2192');
		charEntities.put("darr", '\u2193');
		charEntities.put("harr", '\u2194');
		charEntities.put("crarr", '\u21b5');
		charEntities.put("lArr", '\u21d0');
		charEntities.put("uArr", '\u21d1');
		charEntities.put("rArr", '\u21d2');
		charEntities.put("dArr", '\u21d3');
		charEntities.put("hArr", '\u21d4');
		charEntities.put("forall", '\u2200');
		charEntities.put("part", '\u2202');
		charEntities.put("exist", '\u2203');
		charEntities.put("empty", '\u2205');
		charEntities.put("nabla", '\u2207');
		charEntities.put("isin", '\u2208');
		charEntities.put("notin", '\u2209');
		charEntities.put("ni", '\u220b');
		charEntities.put("prod", '\u220f');
		charEntities.put("sum", '\u2211');
		charEntities.put("minus", '\u2212');
		charEntities.put("lowast", '\u2217');
		charEntities.put("radic", '\u221a');
		charEntities.put("prop", '\u221d');
		charEntities.put("infin", '\u221e');
		charEntities.put("ang", '\u2220');
		charEntities.put("and", '\u2227');
		charEntities.put("or", '\u2228');
		charEntities.put("cap", '\u2229');
		charEntities.put("cup", '\u222a');
		charEntities.put("int", '\u222b');
		charEntities.put("there4", '\u2234');
		charEntities.put("sim", '\u223c');
		charEntities.put("cong", '\u2245');
		charEntities.put("asymp", '\u2248');
		charEntities.put("ne", '\u2260');
		charEntities.put("equiv", '\u2261');
		charEntities.put("le", '\u2264');
		charEntities.put("ge", '\u2265');
		charEntities.put("sub", '\u2282');
		charEntities.put("sup", '\u2283');
		charEntities.put("nsub", '\u2284');
		charEntities.put("sube", '\u2286');
		charEntities.put("supe", '\u2287');
		charEntities.put("oplus", '\u2295');
		charEntities.put("otimes", '\u2297');
		charEntities.put("perp", '\u22a5');
		charEntities.put("sdot", '\u22c5');
		charEntities.put("lceil", '\u2308');
		charEntities.put("rceil", '\u2309');
		charEntities.put("lfloor", '\u230a');
		charEntities.put("rfloor", '\u230b');
		charEntities.put("lang", '\u2329');
		charEntities.put("rang", '\u232a');
		charEntities.put("loz", '\u25ca');
		charEntities.put("spades", '\u2660');
		charEntities.put("clubs", '\u2663');
		charEntities.put("hearts", '\u2665');
		charEntities.put("diams", '\u2666');
	}
	
}
