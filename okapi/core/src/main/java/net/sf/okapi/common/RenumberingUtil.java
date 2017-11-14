/*===========================================================================
  Copyright (C) 2013 by the Okapi Framework contributors
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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.IAlignedSegments;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;

public class RenumberingUtil {
	
	/**
	 * Renumber the codes in a TextContainer for segmentation.  As much
	 * as possible, this will renumber the code IDs in each TextFragment to 
	 * begin at 1.  In cases where a tag pair is split across one or more
	 * TextFragments, this is not possible.  In this case, these "runs" of
	 * connected TextFragments will be treated as a single logical ID-space
	 * and collectively renumbered to start at 1.
	 * <br>
	 * Note that this can cause problems when code IDs are not consecutive
	 * or non-numeric.
	 * 
	 * @param tc TextContainer to renumber
	 */
	public static void renumberCodesForSegmentation(TextContainer tc) {
		if ( tc == null ) {
			return;
		}
		
		// tc should already be segmented by this point.
		boolean inUnmatched = false;
		int unmatchedMinId = 0;
		for ( Segment seg : tc.getSegments() ) {
			TextFragment tf = seg.getContent();
			if (RenumberingUtil.containsOnlyMatchingCodes(tf)) {
				inUnmatched = false;
				reduceCodeIdsByOffset(tf, calculateCodeOffset(tf));
			}
			else if (!inUnmatched) {
				// Start of an unmatched run
				inUnmatched = true;
				unmatchedMinId = calculateCodeOffset(tf);
				reduceCodeIdsByOffset(tf, unmatchedMinId);
			}
			else {
				// Already in an unmatched run
				reduceCodeIdsByOffset(tf, unmatchedMinId);
			}
		}
	}

	/**
	 * Renumber the codes in the TextUnit's source and target containers, this 
	 * will renumber the code IDs in each TextFragment to begin at 1.
	 * 
	 * @param tu {@link ITextUnit} to renumber
	 * @param trgLocale the target locale.
	 */
	public static void renumberTextUnitCodes(ITextUnit tu, LocaleId trgLocale) {
		if ( tu == null ) {
			return;
		}
		
		IAlignedSegments bilingualSegs = tu.getAlignedSegments();
		Iterator<Segment> srcSegIterator = bilingualSegs.iterator(trgLocale);
		List<Code> allSrcCodes = new LinkedList<>();
		List<Code> allTrgCodes = new LinkedList<>();
		LinkedList<Integer> oldSrcCodeIds = new LinkedList<>();

		while(srcSegIterator.hasNext()) {
			Segment srcSegment = srcSegIterator.next();
			Segment trgSegment = bilingualSegs.getCorrespondingTarget(srcSegment, trgLocale);
			List<Code> srcCodes = srcSegment.getContent().getCodes();
			List<Code> trgCodes = trgSegment.getContent().getCodes();
			
			// store original code id values
			for (Code sc : srcCodes) {
				int srcId = sc.getId();
				oldSrcCodeIds.add(srcId);
			}
			
			// normalize code ids
			srcSegment.text.renumberCodes(1, true);
			
			// accumulate segment codes
			allSrcCodes.addAll(srcCodes);
			allTrgCodes.addAll(trgCodes);
		}
		
		// update corresponding target codes
		// across all segments
		int i = 0;
		for (Integer id : oldSrcCodeIds) {
			Code sc = allSrcCodes.get(i++);
			for (Code tc : allTrgCodes) {
				if (id == tc.getId()) {
					tc.setId(sc.getId());
					break;
				}
			}
		}
	}
	
	/**
	 * Find the minimum ID of any code in the TextFragment, then
	 * use that to calculate the offset/delta by which all code
	 * IDs in the TextFragment need to be adjusted.
	 * 
	 * @param tf TextFragment to examine
	 * @return code ID offset for the TextFragment, which is one 
	 * 		   less than the minimum code ID
	 */
	public static int calculateCodeOffset(TextFragment tf) {
		if (tf.getCodes().size() == 0) {
			return 0;
		}
		int minId = Integer.MAX_VALUE;
		for (Code code : tf.getCodes()) {
			if (minId > code.getId()) {
				minId = code.getId();
			}
		}
		return minId - 1;
	}

	/**
	 * Reduce the IDs for all codes in a TextFragment by a fixed amount.
	 * @param tf
	 * @param offset
	 */
	private static void reduceCodeIdsByOffset(TextFragment tf, int offset) {
		for (Code code : tf.getCodes()) {
			code.setId(code.getId() - offset);
		}
	}
	
	
	/**
	 * Check to see if this text fragment contains either open or closed
	 * tags that do not have a corresponding paired tag within the same
	 * TextFragment.
	 * @param tf the text fragment.
	 * @return true if unmatched/unpaired codes are present
	 */
	public static boolean containsOnlyMatchingCodes(TextFragment tf) {
		List<Code> codes = tf.getCodes();
		
		// Find min, max ID values
		int minId = Integer.MAX_VALUE, maxId = Integer.MIN_VALUE;
		for (Code code : codes) {
			int id = code.getId();
			if (id < minId) minId = id;
			if (id > maxId) maxId = id;
		}
		int size = (maxId - minId + 1);
		
		int[] values = new int[size];
		for (Code c : codes) {
			int id = c.getId();
			int index = id - minId;
			values[index] += codeVal(c);
		}
		for (int i = 0; i < size; i++) {
			if (values[i] != 0) return false;
		}
		return true;
	}
	
	private static int codeVal(Code c) {
		switch (c.getTagType()) {
		case OPENING:
			return 1;
		case CLOSING:
			return -1;
		case PLACEHOLDER:
			return 0;
		}
		return 0;
	}
	
	/**
	 * Reverse the renumbering process that was performed during segmentation.
	 * @param tc the text container to renumber.
	 */
	public static void renumberCodesForDesegmentation(TextContainer tc) {
		if ( tc == null ) {
			return;
		}

		int nextId = 1;
		boolean inUnmatched = false;
		int unmatchedDelta = 0;
		for ( Segment seg : tc.getSegments() ) {
			TextFragment tf = seg.getContent();
			int count = tf.getCodes().size();
			if ( count == 0 ) continue;
			
			if (RenumberingUtil.containsOnlyMatchingCodes(tf)) {
				inUnmatched = false;
				nextId += incrementCodeIdsByOffset(tf, nextId - 1);
			}
			else if (!inUnmatched) {
				// Start of an unmatched run
				inUnmatched = true;
				unmatchedDelta = nextId - 1;
				nextId += incrementCodeIdsByOffset(tf, unmatchedDelta);
			}
			else {
				// Already in an unmatched run
				nextId += incrementCodeIdsByOffset(tf, unmatchedDelta);
			}
		}
	}
	
	// Return the number of codes whos IDs were updated
	private static int incrementCodeIdsByOffset(TextFragment tf, int delta) {
		int count = 0;
		for ( Code code : tf.getCodes() ) {
			code.setId(code.getId() + delta);
			if (code.getTagType() != TagType.CLOSING)
				count++;
		}
		return count;
	}
}
