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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.okapi.lib.search.lucene.analysis;

import java.io.Reader;
import java.util.Locale;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.util.Version;

/**
 * 
 * @author HaslamJD
 * @author HARGRAVEJE
 */
public final class NgramAnalyzer extends Analyzer {
	private static final String[] ENGLISH_STOP_NGRAMS = { "the ", " the",
			" to ", "tion", " you", "you ", " and", "and ", "ing ", "atio",
			" not", "t th", "ter ", " of ", "this", "he s", "ion ", "not ",
			" not", "r th", "ilys", "lyse", "n th", " in ", "e in", "hat ",
			" thi", "e th", "for ", " for", " tha", " ind", "er t",
			" con", "our ", " our", " or ", " can", "can ", " is ",
			"is l", "ment", " are", "are ", " wit", " pro", "ions",
			"in t", "lect ", " too", "too ", "of t", "ight", "ting", "ing,",
			" as ", "pro ", " an", " use", " onl", "f th", " com", "e to",
			" inc", "ent ", "ith ", " col", "s in", "age ", "ou c",
			"e an", "s th", "ick ", "s an", "he c", "u ca", "e on", "s to",
			"sing", "he p", "he a", "on t", " pho", "e pr", "opti",
			"use ", "ptio", "rom ", "ng t", "ine ", " fro", " opt", "usin",
			"comm", "ents", "lor ", "s of", "es a", "t to", "ayer",
			"inte", " lay", "an i", "on o", "ble ", "nd t", "to t", "oose",
			" pre", "tosh", " ele", "g th", "ated", "ons ", " res", "appl",
			"n im", "he i", "e im", " sel", "be o", "e re", "ers ", "e fo",
			"ecti", "nter", "d in", "ther", "ate ", "e of", "leme", " dis",
			"ctio", "es t", "ages", "all ", "to c", " tra", "elem", "info",
			" inf", "e ad", "s ar", "d to", "ed t", "s yo", "nd c", "nts ",
			"red ", "r co", "ding", "nfor", "he l", "or t", "t co", "ette",
			"sion", "ool ", "incl", "ion.", "ide ", "ed i", "o th", "pale",
			" sta", "alet", "s, a", " be ", "se t", " pal", "h th", "her ",
			"of a", " int", "e la", "e fi", "he r", "ess ", "ges ", "he t",
			"he f", "ght ", "op e", "ates", "nt t", "icat", "ppli", "rati",
			"p el", ", yo", ", or", "twar", "plic"};
	
	private Set<Object> stopNgrams;

	private Locale locale;
	private int ngramLength;

	public NgramAnalyzer(Locale locale, int ngramLength) {
		if (ngramLength <= 0) {
			throw new IllegalArgumentException(
					"'ngramLength' cannot be less than 0");
		}
		this.stopNgrams = StopFilter.makeStopSet(Version.LUCENE_31, ENGLISH_STOP_NGRAMS);
		this.locale = locale;
		this.ngramLength = ngramLength;
	}

	@Override
	public TokenStream tokenStream(String fieldName, Reader reader) {
		if (locale.getLanguage().equalsIgnoreCase("en")) {
			return new StopFilter(Version.LUCENE_31, new AlphabeticNgramTokenizer(reader,
					ngramLength, locale), stopNgrams);
		}
		return new AlphabeticNgramTokenizer(reader, ngramLength, locale);
	}
}
