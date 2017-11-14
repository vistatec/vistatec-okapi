/*===========================================================================
  Copyright (C) 2010-2013 by the Okapi Framework contributors
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

package net.sf.okapi.steps.termextraction;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.annotation.GenericAnnotationType;
import net.sf.okapi.common.annotation.TermsAnnotation;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.resource.AnnotatedSpan;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextUnitUtil;
import net.sf.okapi.steps.tokenization.common.Token;
import net.sf.okapi.steps.tokenization.common.TokensAnnotation;
import net.sf.okapi.steps.tokenization.tokens.Tokens;

import com.ibm.icu.text.BreakIterator;

public class SimpleTermExtractor {

	private Parameters params;
	private Map<String, Boolean> stopWords;
	private Map<String, Boolean> notStartWords;
	private Map<String, Boolean> notEndWords;
	private Map<String, Integer> terms;
	private Map<String, Integer> termsFromAnnotations;
	private Locale srcLocale;
	private BreakIterator breaker;
	private String rootDir;
	private String inputRootDir;

	/**
	 * Initializes this extractor. This must be called before starting to process the input files.
	 * @param params the options to use.
	 * @param sourceLocaleId the source locale.
	 * @param rootDir the value to use for the ${rootDir} variable (can be null).
	 * @param inputRootDir the value to use for the ${inputRootDir} variable (can be null).
	 */
	public void initialize (Parameters params,
		LocaleId sourceLocaleId,
		String rootDir,
		String inputRootDir)
	{
		this.srcLocale = sourceLocaleId.toJavaLocale();
		this.params = params;
		this.rootDir = rootDir;
		this.inputRootDir = inputRootDir;
		
		stopWords = loadList(params.getStopWordsPath(), "stopWords_en.txt");
		notStartWords = loadList(params.getNotStartWordsPath(), "notStartWords_en.txt");
		notEndWords = loadList(params.getNotEndWordsPath(), "notEndWords_en.txt");
		terms = new LinkedHashMap<String, Integer>();
		termsFromAnnotations = new LinkedHashMap<String, Integer>();
		breaker = null;
	}
	
	/**
	 * Processes a text unit for term extraction.
	 * @param tu the text unit to process.
	 */
	public void processTextUnit (ITextUnit tu) {
		// Skip non-translatable
		if ( !tu.isTranslatable() ) return;

		// Use basic statistics
		if ( params.getUseStatistics() ) {
			gathertermsFromStatistics(tu);
		}
		
		// Extract from the TermsAnnotation
		if ( params.getUseTerminologyAnnotations() ) {
			TermsAnnotation ann = tu.getSource().getAnnotation(TermsAnnotation.class);
			if ( ann != null ) {
				for ( int i=0; i<ann.size(); i++ ) {
					String term = ann.getTerm(i);
					if ( termsFromAnnotations.containsKey(term) ) {
						termsFromAnnotations.put(term, termsFromAnnotations.get(term)+1);
					}
					else {
						termsFromAnnotations.put(term, 1);
					}
				}
			}
		}
		
		// Extract from the ITS Text-Analysis annotations
		if ( params.getUseTextAnalysisAnnotations() ) {
			harvestTextAnalysisAnnotations(tu);
		}
		
	}
	
	private void gathertermsFromStatistics (ITextUnit tu) {
		// Get the list of words
		TokensAnnotation annot = tu.getAnnotation(TokensAnnotation.class);
		List<String> words = null;
		
		// Get the "words" to use for the extraction.
		// First try to use the TokensAnnotation if one is present
		if ( annot != null ) {
			Tokens tokens = annot.getFilteredList("WORD", "KANA", "IDEOGRAM");
			words = new ArrayList<String>();
			for ( Token token : tokens ) {
				addWord(words, token.getValue());
			}
		}
		else { // If no annotation is available: use the default word-breaker
			words = getWordsFromDefaultBreaker(tu.getSource());
		}

		// Gather the term candidates
		String term;
		for ( int i=0; i<words.size(); i++ ) {
			// Skip stop words
			if ( stopWords.containsKey(words.get(i)) ) continue;
			// Start term candidate
			term = "";
			for ( int j=0; j<params.getMaxWordsPerTerm(); j++ ) {
				// Check we don't go outside the array
				if ( i+j >= words.size() ) continue;
				String word = words.get(i+j);
				// Not needed, no word should be empty at this point: if ( word.length() == 0 ) continue;

				// Stop at stop words
				if ( stopWords.containsKey(word) ) {
					j = params.getMaxWordsPerTerm()+1; // Stop here
					continue;
				}

				// Do not include terms starting on a no-start word
				if ( j == 0 ) {
					if ( notStartWords.containsKey(word) ) {
						j = params.getMaxWordsPerTerm()+1; // Stop here
						continue;
					}
				}
				// Add separator if needed
				if ( j > 0 ) {
					term += getWordSeparator(term.charAt(term.length()-1));
				}
				term += word;

				// Do not include term with less than m_nMinWords
				if ( j+1 < params.getMinWordsPerTerm() ) continue;
				// But continue to build the term with more words

				// Do not include terms ending on a no-end word
				if ( notEndWords.containsKey(word) ) continue;
				// But continue to build the term with more words

				// Add or increment the term
				if ( terms.containsKey(term) ) {
					terms.put(term, terms.get(term)+1);
				}
				else {
					terms.put(term, 1);
				}
			}
		}
	}
	
	private void harvestTextAnalysisAnnotations (ITextUnit tu) {
		for ( Segment seg : tu.getSource().getSegments() ) {
			if ( !seg.getContent().hasAnnotation(GenericAnnotationType.GENERIC) ) continue;
			// Else: check if it's a TA annotation
			List<AnnotatedSpan> aspans = seg.getContent().getAnnotatedSpans(GenericAnnotationType.GENERIC);
			for ( AnnotatedSpan aspan : aspans ) {
				String term = aspan.span.toText();
				if ( termsFromAnnotations.containsKey(term) ) {
					termsFromAnnotations.put(term, termsFromAnnotations.get(term)+1);
				}
				else {
					termsFromAnnotations.put(term, 1);
				}
			}
		}
	}
	
	/**
	 * Gets the string that is the separator to use between two words.
	 * @param prevChar the last character of the string where the separator needs to be added.
	 * @return the separator (a space or empty)
	 */
	private String getWordSeparator (char prevChar) {
		// Check the last character of the term
		if ( prevChar > 0x0700 ) {
			// If it is OTHER_LETTER above U+700 it's likely to be CJK, Thai, Devanagari, etc.
			switch ( Character.getType(prevChar) ) {
			case Character.OTHER_LETTER:
				return ""; // No space separation for those
			}
		}
		return " ";
	}

	/**
	 * Performs the post-processing clean-up. this must be called once all files
	 * have been processed.  
	 */
	public void completeExtraction () {
		// Remove entries with less occurrences than allowed
		// Do this first so there is less items to go through if we clean up the sub-strings
		cleanupLowCounts(terms);
		// Remove sub-string entries if requested
		if ( params.getRemoveSubTerms() ) {
			// Create the new cleaned-up map, make sure to assign it properly
			terms = cleanupSubStrings(terms);
			// Then re-cleanup entries with less occurrences than allowed
			cleanupLowCounts(terms);
		}
		
		// Then, add the terms coming from annotations:
		terms.putAll(termsFromAnnotations);

		// Sort alphabetically
		terms = new TreeMap<String, Integer>(terms);

		// Then sort by number of occurrences if requested
		if ( params.getSortByOccurrence() ) {
			terms = sortByValues(terms);
		}
		
		// Create the results file
		generateReport();
	}

	/**
	 * Generates the report file with the results.
	 */
	private void generateReport () {
		// Output the report
		PrintWriter writer = null;
		try {
			String finalPath = Util.fillRootDirectoryVariable(params.getOutputPath(), rootDir);
			finalPath = Util.fillInputRootDirectoryVariable(finalPath, inputRootDir);
			Util.createDirectories(finalPath);
			writer = new PrintWriter(finalPath, "UTF-8");
			for ( Entry<String, Integer> entry : terms.entrySet() ) {
				writer.println(String.format("%d\t%s", entry.getValue(), entry.getKey()));
			}
		}
		catch ( IOException e ) {
			throw new OkapiException("Error when writing output file.", e);
		}
		finally {
			if ( writer != null ) {
				writer.close();
				writer = null;
			}
		}
	}
	
	/**
	 * Gets the list of term candidates.
	 * @return the list of term candidates.
	 */
	public Map<String, Integer> getTerms () {
		return terms;
	}

	/**
	 * Removes entries that have less occurrences than the minimum allowed.
	 * The modifications are done directly in mapToClean. 
	 * @param mapToClean the map to clean up
	 */
	private void cleanupLowCounts (Map<String, Integer> mapToClean) {
		Iterator<Entry<String, Integer>> iter = mapToClean.entrySet().iterator();
		while ( iter.hasNext() ) {
			Entry<String, Integer> entry = iter.next();
			if ( entry.getValue() < params.getMinOccurrences() ) {
				iter.remove();
			}
		}
	}

	/**
	 * Removes entries that look like sub-sequences of a longer entry.
	 * @param mapToClean the map to clean up
	 * @return a new map with only the entries left.
	 */
	private Map<String, Integer> cleanupSubStrings (Map<String, Integer> mapToClean) {
		// Sort by text of the term candidate, the longest first.
		// This allows longest sub-string to be eliminated first, so shorter sub-strings
		// that should be removed are not preserved because the count is off dur to the
		// presence of the longer sub-strings.
		Map<String, Integer> sortedTerms = new TreeMap<String, Integer>(Collections.reverseOrder());
		sortedTerms.putAll(mapToClean);
		
		// Prepare the iteration through all entries
		Iterator<Entry<String, Integer>> iter1 = sortedTerms.entrySet().iterator();
		Entry<String, Integer> entry1;
		// Go through all entries
		while ( iter1.hasNext() ) {
			// Get the next entry1 to see if it is a sub-string to remove
			entry1 = iter1.next();
			Iterator<Entry<String, Integer>> iter2 = sortedTerms.entrySet().iterator();
			Entry<String, Integer> entry2;
			int count = 0;
			String sub = entry1.getKey();
			// Add separator to avoid real text sub-string (e.g. "word" part of "wording")
			sub += getWordSeparator(sub.charAt(sub.length()-1));
			// Go through all (other) entries
			while ( iter2.hasNext() ) {
				entry2 = iter2.next();
				// If the entry2 contains entry1 (but is not entry1) then it's a super-string
				// Note that we do need to check entry2!=entry1 as CJK terms may have no extra ending
				if ( entry2.getKey().startsWith(sub) && !entry2.equals(entry1) ) {
					// Count its occurrences
					count += entry2.getValue();
				}
			}
			// If entry1 occurs as many time as all its super-strings occur, then we remove it
			if ( entry1.getValue() == count ) {
				iter1.remove();
			}
			else { // Adjust the number of occurrences to reflect when the entry
				// is found alone (not in a one of the super-string)
				entry1.setValue(entry1.getValue()-count);
			}
		}
		
		return sortedTerms;
	}
	
	/**
	 * Adds a word in a given list of words. 
	 * @param list the list where to add the word.
	 * @param token the word/token to add.
	 */
	private void addWord (List<String> list,
		String token)
	{
		// No empty words and keep only extended single-char
		if (( token.length() == 0 )
			|| (( token.length() == 1 ) && ( token.codePointAt(0) < 126 ))) return;
		// Keep only "letters" (includes CJK characters)
		if ( !Character.isLetterOrDigit(token.codePointAt(0)) ) return;
		
		// Add the word (and preserve or not the case)
		if ( params.getKeepCase() ) {
			list.add(token);
		}
		else {
			list.add(token.toLowerCase(srcLocale));
		}
	}

	/**
	 * Gets a list of words/tokens from a text container.
	 * @param tc the text container to process.
	 * @return the list of words/tokens.
	 */
	private List<String> getWordsFromDefaultBreaker (TextContainer tc) {
		// Get the plain text to process
		String content;
		if ( tc.contentIsOneSegment() ) {
			content = TextUnitUtil.getText(tc.getFirstContent()); 
		}
		else {
			content = TextUnitUtil.getText(tc.getUnSegmentedContentCopy());
		}
		if ( content.length() == 0 ) {
			return Collections.emptyList();
		}

		// Break down the text into "words"
		if ( breaker == null ) {
			breaker = BreakIterator.getWordInstance(srcLocale);
		}
		breaker.setText(content);
		ArrayList<String> words = new ArrayList<String>();
		int start = breaker.first();
		for ( int end=breaker.next(); end!=BreakIterator.DONE; start=end, end=breaker.next()) {
			addWord(words, content.substring(start, end));
		}

		return words;
	}

	/**
	 * Loads a list of words into a hash map.
	 * @param path the path of the file to load or null/empty to load the default.
	 * @param defaultFile the default file name to use if the provided path is null or empty.
	 * @return a map of the words in the loaded file.
	 */
	private HashMap<String, Boolean> loadList (String path,
		String defaultFile)
	{
		HashMap<String, Boolean> map = new HashMap<String, Boolean>();
		BufferedReader reader = null;
		try {
			InputStream is;
			// Use the default resource if no path is provided
			if ( Util.isEmpty(path) ) {
				is = SimpleTermExtractor.class.getResourceAsStream(defaultFile);
			}
			else {
				is = new FileInputStream(path);
			}
			reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String line;
			while ((line = reader.readLine()) != null ) {
				line = line.trim();
				if ( line.length() == 0 ) continue;
				if ( line.charAt(0) == '#' ) continue;
				// Add the word to the list, make sure we skip duplicate to avoid error
				if ( !map.containsKey(line) ) map.put(line, false);
			}
		}
		catch ( IOException e ) {
			throw new OkapiException("Error reading word list.", e);
		}
		finally {
			if ( reader != null ) {
				try {
					reader.close();
				}
				catch ( IOException e ) {
					throw new OkapiException("Error reading word list.", e);
				}
			}
		}
		return map;
	}

	/**
	 * Sorts a given map by its values.
	 * @param map the map to sort.
	 * @return the sorted map.
	 */
	private <K, V extends Comparable<V>> Map<K, V> sortByValues (final Map<K, V> map) { 
		Comparator<K> valueComparator =  new Comparator<K> () { 
			public int compare (K k1, K k2) { 
		    	int res = map.get(k2).compareTo(map.get(k1)); 
		        if (res == 0) return 1; 
		        else return res; 
		    } 
		};
		Map<K, V> sortedMap = new TreeMap<K, V>(valueComparator); 
		sortedMap.putAll(map); 
		return sortedMap;
	}
	
}
