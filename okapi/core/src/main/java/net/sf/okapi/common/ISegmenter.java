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

import java.util.List;

import net.sf.okapi.common.resource.TextContainer;

/**
 * Common methods to provide segmentation facility to extracted content. 
 */
public interface ISegmenter {

	/**
	 * Calculate the segmentation of a given plain text string.
	 * @param text plain text to segment.
	 * @return the number of segments calculated.
	 */
	public int computeSegments (String text);
	
	/**
	 * Calculates the segmentation of a given TextContainer object.
	 * If the content is already segmented, it is un-segmented automatically before
	 * being processed.
	 * @param container the object to segment.
	 * @return the number of segments calculated.
	 */
	public int computeSegments (TextContainer container);

	/**
	 * Compute the range of the next segment for a given TextContainer object.
	 * The next segment is searched from the first character after the last
	 * segment marker found in the container.
	 * @param container the text container where to look for the next segment. 
	 * @return a range corresponding to the start and end position of the found
	 * segment, or null if no more segments are found.
	 */
	public Range getNextSegmentRange (TextContainer container);

	/**
	 * Gets the list of all the split positions in the text
	 * that was last segmented. You must call {@link #computeSegments(TextContainer)}
	 * or {@link #computeSegments(String)} before calling this method.
	 * A split position is the first character position of a new segment.
	 * <p><b>IMPORTANT: The position returned here are the position WITHOUT taking
	 * in account any options for trimming or not leading and trailing white-spaces.</b>
	 * @return An array of integers where each value is a split position
	 * in the coded text that was segmented.
	 */
	public List<Integer> getSplitPositions ();

	/**
	 * Gets the list off all segments ranges calculated when
	 * calling {@link #computeSegments(String)}, or
	 * {@link #computeSegments(TextContainer)}.
	 * @return the list of all segments ranges. each range is stored in
	 * a {@link Range} object where start is the start and end the end of the range.
	 * Returns null if no ranges have been defined yet.
	 */
	public List<Range> getRanges ();

	/**
	 * Gets the language used to apply the rules.
	 * @return the language code used to apply the rules, or null, if none has
	 * been specified.
	 */
	public LocaleId getLanguage ();
		
	/**
	 * Indicates if end codes should be included (See SRX implementation notes).
	 * @return true if they should be included, false otherwise.
	 */
	public boolean includeEndCodes();
	
	/**
	 * Indicates if isolated codes should be included (See SRX implementation notes).
	 * @return true if they should be included, false otherwise.
	 */
	public boolean includeIsolatedCodes();
	
	/**
	 * Indicates if start codes should be included (See SRX implementation notes).
	 * @return true if they should be included, false otherwise.
	 */
	public boolean includeStartCodes();
	
	/**
	 * Resets the options to their defaults, and the compiled rules
	 * to nothing.
	 */
	public void reset();
	
	/**
	 * Indicates if sub-flows must be segmented.
	 * @return true if sub-flows must be segmented, false otherwise.
	 */
	public boolean segmentSubFlows();
	
	/**
	 * Indicates if leading white-spaces should be left outside the segments.
	 * @return true if the leading white-spaces should be trimmed.
	 */
	public boolean trimLeadingWhitespaces();
	
	/**
	 * Indicates if trailing white-spaces should be left outside the segments.
	 * @return true if the trailing white-spaces should be trimmed.
	 */
	public boolean trimTrailingWhitespaces();
	
	/**
	 * Indicates if, when there is a single segment in a text, it should include
	 * the whole text (no spaces or codes trim left/right)
	 * @return true if a text with a single segment should include the whole
	 * text.
	 */
	public boolean oneSegmentIncludesAll();

	/**
	 * Indicate if the segmenter should treat each isolated code as a single
	 * whitespace character (U+0020) when applying segmentation.
	 * @return true if the segmenter should treat isolated codes as whitespace
	 */
	public boolean treatIsolatedCodesAsWhitespace();
	
	/**
	 * Sets the locale used to apply the rules.
	 * @param locale Code of the language to use to apply the rules.
	 */
	public void setLanguage(LocaleId locale);
	
	public void setIncludeEndCodes(boolean includeEndCodes);
	
	public void setIncludeIsolatedCodes(boolean includeIsolatedCodes);
	
	public void setIncludeStartCodes(boolean includeStartCodes);
	
	public void setOneSegmentIncludesAll(boolean oneSegmentIncludesAll);
	
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
	 */
	public void setOptions(boolean segmentSubFlows, boolean includeStartCodes,
			boolean includeEndCodes, boolean includeIsolatedCodes,
			boolean oneSegmentIncludesAll, boolean trimLeadingWS,
			boolean trimTrailingWS);
	
	public void setSegmentSubFlows(boolean segmentSubFlows);
	
	public void setTrimCodes(boolean trimCodes);
	
	public void setTrimLeadingWS(boolean trimLeadingWS);
	
	public void setTrimTrailingWS(boolean trimTrailingWS);

	public void setTreatIsolatedCodesAsWhitespace(boolean treatIsolatedCodesAsWhitespace);

}
