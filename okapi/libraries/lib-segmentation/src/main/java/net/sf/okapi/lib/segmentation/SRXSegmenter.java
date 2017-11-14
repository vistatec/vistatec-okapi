/*===========================================================================
  Copyright (C) 2008-2012 by the Okapi Framework contributors
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

package net.sf.okapi.lib.segmentation;

import static net.sf.okapi.common.resource.TextFragment.Marker.CLOSING;
import static net.sf.okapi.common.resource.TextFragment.Marker.ISOLATED;
import static net.sf.okapi.common.resource.TextFragment.Marker.OPENING;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.icu.text.BreakIterator;
import com.ibm.icu.text.RuleBasedBreakIterator;
import com.ibm.icu.util.ULocale;

import net.sf.okapi.common.ISegmenter;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Range;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.Marker;
import net.sf.okapi.common.resource.TextUnitUtil;

/**
 * Implements the {@link ISegmenter} interface for SRX rules.
 */
public class SRXSegmenter implements ISegmenter {

	/**
	 * The isolated code replacement text.
	 */
	private static final String ISOLATED_CODE_REPLACEMENT_TEXT = " ";

	/**
	 * The code marker length.
	 */
	private static final int CODE_MARKER_LENGTH = 2;

	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	
	private boolean segmentSubFlows;
	private boolean cascade;
	private boolean includeStartCodes;
	private boolean includeEndCodes;
	private boolean includeIsolatedCodes;
	private LocaleId currentLanguageCode;
	private boolean oneSegmentIncludesAll; // Extension
	private boolean trimLeadingWS; // Extension
	private boolean trimTrailingWS; // Extension
	private boolean useJavaRegex; // Deprecated Extension, always true.
	private boolean useIcu4JBreakRules = false;
	private boolean trimCodes; // Extension
	private boolean treatIsolatedCodesAsWhitespace; // Extension
	private ArrayList<CompiledRule> rules;
	private Pattern maskRule; // Extension
	private TreeMap<Integer, Boolean> splits;
	private List<Integer> finalSplits;
	private ArrayList<Integer> starts;
	private ArrayList<Integer> ends;
	private BreakIterator icu4jBreakIterator;

	/**
	 * Creates a new SRXSegmenter object.
	 */
	public SRXSegmenter () {
		reset();
	}

	@Override
	public void reset () {
		currentLanguageCode = null;
		rules = new ArrayList<CompiledRule>();
		maskRule = null;
		splits = null;
		segmentSubFlows = true; // SRX default
		cascade = false; // There is no SRX default for this
		includeStartCodes = false; // SRX default
		includeEndCodes = true; // SRX default
		includeIsolatedCodes = false; // SRX default
		oneSegmentIncludesAll = false; // Extension
		trimLeadingWS = false; // Extension IN TEST (was true for StringInfo)
		trimTrailingWS = false; // Extension IN TEST (was true for StringInfo)
		useJavaRegex = true; // Deprecated Extension
		trimCodes = false; // Extension IN TEST (was false for StringInfo) NOT USED for now
		treatIsolatedCodesAsWhitespace = false;
		useIcu4JBreakRules = false; // Extension
	}

	/**
	 * Sets the options for this segmenter.
	 * @param segmentSubFlows true to segment sub-flows, false to no segment them.
	 * @param includeStartCodes true to include start codes just before a break in the 'left' segment,
	 * false to put them in the next segment. 
	 * @param includeEndCodes  true to include end codes just before a break in the 'left' segment,
	 * false to put them in the next segment.
	 * @param includeIsolatedCodes true to include isolated codes just before a break in the 'left' segment,
	 * false to put them in the next segment.
	 * @param oneSegmentIncludesAll true to include everything in segments that are alone.
	 * @param trimLeadingWS true to trim leading white-spaces from the segments, false to keep them.
	 * @param trimTrailingWS true to trim trailing white-spaces from the segments, false to keep them.
	 * @param useJavaRegex true if the rules are for the Java regular expression engine, false if they are for ICU. 
	 * @param treatIsolatedCodesAsWhitespace if true then the isolated code markers in codedText get converted
	 * to spaces, so that they don't get in the way of the rules. If false, the codes are simply removed.
	 */
	public void setOptions (boolean segmentSubFlows,
		boolean includeStartCodes,
		boolean includeEndCodes,
		boolean includeIsolatedCodes,
		boolean oneSegmentIncludesAll,
		boolean trimLeadingWS,
		boolean trimTrailingWS,
		boolean useJavaRegex,
		boolean useIcu4JBreakRules,
		boolean treatIsolatedCodesAsWhitespace)
	{
		this.segmentSubFlows = segmentSubFlows;
		this.includeStartCodes = includeStartCodes;
		this.includeEndCodes = includeEndCodes;
		this.includeIsolatedCodes = includeIsolatedCodes;
		this.oneSegmentIncludesAll = oneSegmentIncludesAll;
		this.trimLeadingWS = trimLeadingWS;
		this.trimTrailingWS = trimTrailingWS;
		this.useJavaRegex = useJavaRegex;
		this.useIcu4JBreakRules = useIcu4JBreakRules;
		this.treatIsolatedCodesAsWhitespace = treatIsolatedCodesAsWhitespace;
		if (!useJavaRegex) LOGGER.error("Use of ICU regex has been removed.");
	}
	
	@Override
	public void setOptions (boolean segmentSubFlows,
		boolean includeStartCodes,
		boolean includeEndCodes,
		boolean includeIsolatedCodes,
		boolean oneSegmentIncludesAll,
		boolean trimLeadingWS,
		boolean trimTrailingWS)
	{
		this.segmentSubFlows = segmentSubFlows;
		this.includeStartCodes = includeStartCodes;
		this.includeEndCodes = includeEndCodes;
		this.includeIsolatedCodes = includeIsolatedCodes;
		this.oneSegmentIncludesAll = oneSegmentIncludesAll;
		this.trimLeadingWS = trimLeadingWS;
		this.trimTrailingWS = trimTrailingWS;		
	}
	
	@Override
	public boolean oneSegmentIncludesAll () {
		return oneSegmentIncludesAll;
	}

	@Override
	public boolean segmentSubFlows () {
		return segmentSubFlows;
	}
	
	/**
	 * Indicates if cascading must be applied when selecting the rules for 
	 * a given language pattern.
	 * @return true if cascading must be applied, false otherwise.
	 */
	public boolean cascade () {
		return cascade;
	}
	
	@Override
	public boolean trimLeadingWhitespaces () {
		return trimLeadingWS;
	}
	
	@Override
	public boolean trimTrailingWhitespaces () {
		return trimTrailingWS;
	}
	
	/**
	 * Indicates if this document has rules that are defined for the Java regular expression engine (vs ICU).
	 * @return true if the rules are for the Java regular expression engine, false if they are for ICU.
	 */
	public boolean useJavaRegex () {
		return useJavaRegex;
	}

	@Override
	public boolean treatIsolatedCodesAsWhitespace () {
		return treatIsolatedCodesAsWhitespace;
	}

	/**
	 * Sets the indicator that tells if this document has rules that are defined for the Java regular expression engine (vs ICU).
	 * @param useJavaRegex true if the rules should be treated as Java regular expression, false for ICU.
	 */
	public void setUseJavaRegex (boolean useJavaRegex) {
		this.useJavaRegex = useJavaRegex;
		if (!useJavaRegex) LOGGER.warn("Use of ICU regex is deprecated and may be removed in the future."); 
	}
	
	@Override
	public boolean includeStartCodes () {
		return includeStartCodes;
	}
	
	@Override
	public boolean includeEndCodes () {
		return includeEndCodes;
	}
	
	@Override
	public boolean includeIsolatedCodes () {
		return includeIsolatedCodes;
	}
	
	@Override
	public int computeSegments (String text) {
		TextContainer tmp = new TextContainer(text);
		return computeSegments(tmp);
	}

	@Override
	public int computeSegments (TextContainer container) {
		if ( currentLanguageCode == null ) {
			// Need to call selectLanguageRule()
			throw new SegmentationRuleException("No language defined for the segmenter.");
		}
		
		// Do we have codes?
		// Avoid to create an un-segmented copy if we can
		boolean hasCode;
		if ( container.contentIsOneSegment() ) hasCode = container.getSegments().getFirstContent().hasCode();
		else hasCode = container.getUnSegmentedContentCopy().hasCode();
		
		// Set the flag for trimming or not the in-line codes
		boolean isSCWS = trimCodes && !includeStartCodes;
		boolean isECWS = trimCodes && !includeEndCodes;
		boolean isICWS = trimCodes && !includeIsolatedCodes;

		// Build the list of split positions
		// Get the coded text for the whole content
		String codedText = container.getCodedText();
		List<Integer> origCodePositions = storeOriginalCodePositions(codedText);	
		List<Integer> codePositions = storeCodePositions(codedText);

		// Remove code markers from codedText not to get in the way of the rules
		codedText = treatIsolatedCodesAsWhitespace ?
					TextUnitUtil.removeAndReplaceCodes(codedText, ISOLATED_CODE_REPLACEMENT_TEXT) :
					TextUnitUtil.removeCodes(codedText);

		// ICU4J rules are generated for each segment, must add with the normal SRX rules
		ArrayList<CompiledRule> combinedRules = null;
		if (useIcu4JBreakRules) {
			combinedRules = new ArrayList<>();
			combinedRules.addAll(rules);
			// ICU4J break rules are always added last as we want previous "exception" SRX rules to override them if found.	
			combinedRules.addAll(getIcu4jBreakRules(codedText));
		} else {
			combinedRules = rules;
		}
		
		splits = new TreeMap<>();
		Matcher m;
		for ( CompiledRule rule : combinedRules ) {			
			m = rule.pattern.matcher(codedText);
			// FIXME: I think transparentbounds is what we want so that regex can peek 
			// behind and ahead to better give context outside the matching region. 			
			m.useTransparentBounds(true);
			int start = 0;
			int prevStart = -1;
			while (( start != prevStart ) && m.find(start) ) {
				int n = m.start()+m.group(1).length();

				// Set next start
				prevStart = start; // Comparing with previous start avoid infinite loop for non-capturing patterns
				start = n; // We search starting at each character (to make sure we cover the previous match too)
				
				// Match the end
				if ( n > codedText.length() ) continue;
				// Already a match: Per SRX algorithm, we use the first one only
				// see http://www.gala-global.org/oscarStandards/srx/srx20.html#Struct_classdefinitions
				if ( splits.containsKey(n) ) continue;
				
				// Else add a split marker
				splits.put(n, rule.isBreak);
			}
		}

		codedText = container.getCodedText(); // restore codedText after word breaks

		// Adjust split positions minding the removed original codes
		TreeMap<Integer, Boolean> oldSplits = splits;
		splits = new TreeMap<>();
		
		for (Integer pos : oldSplits.keySet()) {
			int newPos = recalcPos(codedText, pos, codePositions, origCodePositions);
			splits.put(newPos, oldSplits.get(pos));
		}
		
		// Set the additional split positions for mask-rules
		if ( maskRule != null ) {
			m = maskRule.matcher(codedText);
			while ( m.find() ) {
				// Remove any existing marker inside the range
				for ( int n=m.start(); n<m.end(); n++ ) {
					if ( splits.containsKey(n) ) {
						splits.remove(n);
					}
				}
				// Then set the start and end of the range as breaks
				// Don't include a split at 0 because it's an implicit one
				if ( m.start() > 0 ) splits.put(m.start(), true);
				splits.put(m.end(), true);
			}
		}
		
		// Adjust the split positions for in-line codes inclusion/exclusion options
		// And create the list of final splits at the same time
		finalSplits = new ArrayList<Integer>();
		// Do this only if we have in-line codes
		if ( hasCode ) {
			
			// setup start, end and isolated code settings as an EnumSet to make it easier to check
			// include code options
			EnumSet<TextFragment.Marker> includeCodeSettings = EnumSet.noneOf(TextFragment.Marker.class);
			if (includeStartCodes) {
				includeCodeSettings.add(OPENING);
			}			
			if (includeEndCodes) {
				includeCodeSettings.add(CLOSING);
			}
			if (includeIsolatedCodes) {
				includeCodeSettings.add(ISOLATED);
			}
						
			// All breaks are before codes, as we restore a code at its original pos, and if 
			// there's a break at that pos, the code will always find itself after the break 
			for (int pos : splits.keySet()) {
				if (!splits.get(pos)) continue; // Skip non-break positions
				// FIXME: Out of bounds error should never happen, but we are seeing it for Chinese
				// this fix prevents a index out of bounds exception, but may be masking a
				// bigger problem.
				if (pos >= codedText.length()) continue;

				// keep processing any consecutive code that has include = true
				// we stop when we hit a code with include = false
				Marker codeMarkerType = Marker.asEnum(codedText.charAt(pos));
				switch (codeMarkerType) {
					case OPENING:
					case CLOSING:
					case ISOLATED:
						// if include code setting = true for this code
						// Move pos forward adding any codes that also have their settings = true
						// stop when we hit a code that has include = false
						// otherwise (include code setting = false) leave the position as-is (in the following segment)
						if (includeCodeSettings.contains(codeMarkerType)) {
							do {
								pos += CODE_MARKER_LENGTH;
							}
							while (pos < codedText.length() - 1 && includeCodeSettings.contains(Marker.asEnum(codedText.charAt(pos))));
						}
						break;
					default:
						break;
				}
				// Store the updated position
				finalSplits.add(pos);
			}
		}
		else { // Just copy the real splits
			for ( int pos : splits.keySet() ) {
				if ( splits.get(pos) ) finalSplits.add(pos);
			}
		}
		
		// Now build the lists of start and end of each segment
		// but trim them of any white-spaces.
		// Deal also with including or not the in-line codes.
		starts = new ArrayList<Integer>();
		ends = new ArrayList<Integer>();
		int textEnd;
		int textStart = 0;
		int trimmedTextStart;
		for ( int pos : finalSplits ) {
			// FIXME: This condition should never happen, but we are seeing it for Chinese
			// this fix prevents a index out of bounds exception, but may be masking a
			// bigger problem.
			if (pos >= codedText.length()) continue;
			// Trim white-spaces and codes as required at the front
			trimmedTextStart = TextFragment.indexOfFirstNonWhitespace(codedText,
				textStart, pos-1, isSCWS, isECWS, isICWS, trimLeadingWS);
			if ( trimmedTextStart == -1 ) { //pos-1 ) {
				// Only spaces in the segment: Continue with the next position
				continue;
			}
			if ( trimLeadingWS || trimCodes ) textStart = trimmedTextStart;
			// Trim white-spaces and codes as required at the back
			if ( trimTrailingWS || trimCodes ) {
				textEnd = TextFragment.indexOfLastNonWhitespace(codedText,
					pos-1, 0, isSCWS, isECWS, isICWS, trimTrailingWS);
			}
			else textEnd = pos-1;
			if ( textEnd >= textStart ) { // Only if there is something // was > only
				if ( textEnd < pos ) textEnd++; // Adjust for +1 position
				starts.add(textStart);
				ends.add(textEnd);
			}
			textStart = pos;
		}
		// Last one
		int lastPos = codedText.length();
		if ( textStart < lastPos ) {
			// Trim white-spaces and codes as required at the front
			trimmedTextStart = TextFragment.indexOfFirstNonWhitespace(codedText, textStart,
				lastPos-1, isSCWS, isECWS, isICWS, trimLeadingWS);
			if ( trimLeadingWS || trimCodes  ) {
				if ( trimmedTextStart != -1 ) textStart = trimmedTextStart;
			}
			if (( trimmedTextStart != -1 ) && ( trimmedTextStart < lastPos )) {
				// Trim white-spaces and code as required at the back
				if ( trimTrailingWS || trimCodes ) {
					textEnd = TextFragment.indexOfLastNonWhitespace(codedText, lastPos-1,
						textStart, isSCWS, isECWS, isICWS, trimTrailingWS);
				}
				else textEnd = lastPos-1;
				if ( textEnd >= textStart ) { // Only if there is something
					if ( textEnd < lastPos ) textEnd++; // Adjust for +1 position
					starts.add(textStart);
					ends.add(textEnd);
				}
			}
		}

		// Check for single-segment text case
		if (( starts.size() == 1 ) && ( oneSegmentIncludesAll )) {
			starts.set(0, 0);
			ends.clear(); // lastPos is added just after
		}

		// Add an extra value in ends to hold the total length of the coded text
		// to avoid having to re-create it when segmenting.
		ends.add(lastPos);
		
		// Return the number of segment found
		// (ends contains one extra value, so make sure to use starts for this)
		return starts.size();
	}

	// Convert ICU4J break positions to rules so they work as any other SRX rule
	private Collection<CompiledRule> getIcu4jBreakRules(String text) {
		LinkedList<CompiledRule> rules = new LinkedList<>(); 	
		// icu4jBreakIterator created when locale is set (setLanguage)
		icu4jBreakIterator.setText(text);
		// only needed to call generateRuleRegex method
		SRXDocument d = new SRXDocument();
		String pattern = null;
		for (int boundary = icu4jBreakIterator.next(); boundary != BreakIterator.DONE; boundary = icu4jBreakIterator.next()) {
			// ICU always puts a boundary at the end of the string: skip it
			if (boundary == text.length())
				continue;
			
			// Boundary is "the zero-based index of the character following the boundary"
            // (see http://userguide.icu-project.org/boundaryanalysis)
            // Moreover, if there are lots of whitespace between a sentence and the following,
            // ICU puts the boundary after all the whitespace, just before the first char of
            // the next sentence. We want the boundary before these whitespaces, and so this loop.
            while (Character.isWhitespace(text.codePointAt(boundary - 1)) && boundary > 0) {
                boundary--;
            }
            
			// match the number of characters that ICU4J specifies as the break position
			// starting from the beginning of the string
			Rule r = new Rule(String.format("^(.|\\s){%d}", boundary), "", true);
			pattern = d.generateRuleRegex(r);
			pattern = pattern.replace(SRXDocument.ANYCODE, SRXDocument.INLINECODE_PATTERN);
			CompiledRule cr = new CompiledRule(pattern, true);
			rules.add(cr);
		}
		
		return rules;
	}

	private int calculatePosition(int position, int numberOfNonIsolatedCodes, int numberOfIsolatedCodes, boolean increase) {
		int nonIsolatedCodesLength = numberOfNonIsolatedCodes * CODE_MARKER_LENGTH;
		int isolatedCodesLength = numberOfIsolatedCodes
				* (treatIsolatedCodesAsWhitespace()
					? ISOLATED_CODE_REPLACEMENT_TEXT.length()
					: CODE_MARKER_LENGTH);

		return increase
				? position + nonIsolatedCodesLength + isolatedCodesLength
				: position - nonIsolatedCodesLength - isolatedCodesLength;
	}

	private int calculateIncreasedPosition(int position, int numberOfNonIsolatedCodes, int numberOfIsolatedCodes) {
		return calculatePosition(position, numberOfNonIsolatedCodes, numberOfIsolatedCodes, true);
	}

	private int calculateDecreasedPosition(int position, int numberOfNonIsolatedCodes, int numberOfIsolatedCodes) {
		return calculatePosition(position, numberOfNonIsolatedCodes, numberOfIsolatedCodes, false);
	}

	// Package scope for tests.
	int recalcPos(String codedText, int pos, List<Integer> codePositions, List<Integer> origCodePositions) {
		int numberOfNonIsolatedCodes = 0;
		int numberOfIsolatedCodes = 0;

		for (int codeIndex = 0; codeIndex < codePositions.size(); codeIndex++) {

			if (codePositions.get(codeIndex) >= pos) {
				return calculateIncreasedPosition(pos, numberOfNonIsolatedCodes, numberOfIsolatedCodes);
			} else {
				switch (Marker.asEnum(codedText.charAt(origCodePositions.get(codeIndex)))) {
					case OPENING:
					case CLOSING:
						numberOfNonIsolatedCodes++;
						break;
					case ISOLATED:
						numberOfIsolatedCodes++;
						break;
					default:
						// skip UNKNOWN
						break;
				}
			}
		}

		return calculateIncreasedPosition(pos, numberOfNonIsolatedCodes, numberOfIsolatedCodes);
	}
	
	int recalcPosBack(String codedText, int pos, List<Integer> origCodePositions) {
		int numberOfNonIsolatedCodes = 0;
		int numberOfIsolatedCodes = 0;

		for (Integer origCodePosition : origCodePositions) {

			if (origCodePosition >= pos) {
				return calculateDecreasedPosition(pos, numberOfNonIsolatedCodes, numberOfIsolatedCodes);
			} else {
				switch (Marker.asEnum(codedText.charAt(origCodePosition))) {
					case OPENING:
					case CLOSING:
						numberOfNonIsolatedCodes++;
						break;
					case ISOLATED:
						numberOfIsolatedCodes++;
						break;
					default:
						// skip UNKNOWN
						break;
				}
			}
		}

		return calculateDecreasedPosition(pos, numberOfNonIsolatedCodes, numberOfIsolatedCodes);
	}

	List<Integer> storeCodePositions(String text) {
		List<Integer> res = new ArrayList<>();
		int numberOfNonIsolatedCodes = 0;
		int numberOfIsolatedCodes = 0;

		for (int i = 0; i < text.length(); i++) {

			switch (Marker.asEnum(text.charAt(i))) {
				case OPENING:
				case CLOSING:
					res.add(calculateDecreasedPosition(i, numberOfNonIsolatedCodes, numberOfIsolatedCodes));
					numberOfNonIsolatedCodes++;
					i++; // skip index marker
					break;
				case ISOLATED:
					// Position of the code after code removal
					res.add(calculateDecreasedPosition(i, numberOfNonIsolatedCodes, numberOfIsolatedCodes));
					numberOfIsolatedCodes++;
					i++; // skip index marker
					break;
				default:
					// skip UNKNOWN
					break;
			}
		}

		return res;
	}
	
	List<Integer> storeOriginalCodePositions(String text) {
		List<Integer> res = new ArrayList<Integer>();
		for (int i=0; i<text.length(); i++) {
			switch (text.charAt(i)) {
				case TextFragment.MARKER_OPENING:
				case TextFragment.MARKER_CLOSING:
				case TextFragment.MARKER_ISOLATED:
					// Position of the code after code removal
					res.add(i++); // skip index marker as well
					break;
				default:
					break;
			}
		}
		return res;
	}

	@Override
	public Range getNextSegmentRange (TextContainer container) {
		return null;		
	}

	@Override
	public List<Integer> getSplitPositions () {
		
		if ( finalSplits == null ) {
			finalSplits = new ArrayList<Integer>();
		}
		return Collections.unmodifiableList(finalSplits);
	}

	@Override
	public List<Range> getRanges () {
		ArrayList<Range> list = new ArrayList<Range>();
		if ( starts == null ) return null;
		for ( int i=0; i<starts.size(); i++ ) {
			list.add(new Range(starts.get(i), ends.get(i)));
		}
		return Collections.unmodifiableList(list);
	}
	
	@Override
	public LocaleId getLanguage () {
		return currentLanguageCode;
	}

	@Override
	public void setLanguage (LocaleId languageCode) {
		if (languageCode != null) {
			icu4jBreakIterator = RuleBasedBreakIterator.getSentenceInstance(ULocale.createCanonical(languageCode.toBCP47()));
		}
		currentLanguageCode = languageCode;
	}
	
	/**
	 * Sets the flag indicating if cascading must be applied when selecting the 
	 * rules for a given language pattern.
	 * @param value true if cascading must be applied, false otherwise.
	 */
	protected void setCascade (boolean value) {
		cascade = value;
	}

	/**
	 * Adds a compiled rule to this segmenter.
	 * @param compiledRule the compiled rule to add.
	 */
	protected void addRule (CompiledRule compiledRule) {
		rules.add(compiledRule);
	}
	
	/**
	 * Sets the pattern for the mask rule.
	 * @param pattern the new pattern to use for the mask rule.
	 */
	protected void setMaskRule (String pattern) {
		if (( pattern != null ) && ( pattern.length() > 0 ))
			maskRule = Pattern.compile(pattern, Pattern.UNICODE_CHARACTER_CLASS);
		else
			maskRule = null;
	}
	
	@Override
	public void setSegmentSubFlows(boolean segmentSubFlows) {
		this.segmentSubFlows = segmentSubFlows;
	}

	@Override
	public void setIncludeStartCodes(boolean includeStartCodes) {
		this.includeStartCodes = includeStartCodes;
	}

	@Override
	public void setIncludeEndCodes(boolean includeEndCodes) {
		this.includeEndCodes = includeEndCodes;
	}

	@Override
	public void setIncludeIsolatedCodes(boolean includeIsolatedCodes) {
		this.includeIsolatedCodes = includeIsolatedCodes;
	}

	@Override
	public void setOneSegmentIncludesAll(boolean oneSegmentIncludesAll) {
		this.oneSegmentIncludesAll = oneSegmentIncludesAll;
	}

	@Override
	public void setTrimLeadingWS(boolean trimLeadingWS) {
		this.trimLeadingWS = trimLeadingWS;
	}

	@Override
	public void setTrimTrailingWS(boolean trimTrailingWS) {
		this.trimTrailingWS = trimTrailingWS;
	}

	@Override
	public void setTrimCodes(boolean trimCodes) {
		this.trimCodes = trimCodes;
	}

	@Override
	public void setTreatIsolatedCodesAsWhitespace(boolean treatIsolatedCodesAsWhitespace) {
		this.treatIsolatedCodesAsWhitespace = treatIsolatedCodesAsWhitespace;
	}
}
