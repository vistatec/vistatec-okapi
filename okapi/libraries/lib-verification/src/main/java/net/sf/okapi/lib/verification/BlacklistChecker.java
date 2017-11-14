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

package net.sf.okapi.lib.verification;

import com.ibm.icu.lang.UCharacter;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.annotation.IssueType;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextUnitUtil;

public class BlacklistChecker {

	private ArrayList<Issue> issues;
	private BlacklistTB ta;
	private LocaleId loc = new LocaleId(Locale.getDefault());

	public void initialize (BlacklistTB termAccess) {
		this.issues = new ArrayList<Issue>();
		this.ta = termAccess;
	}

	public void initialize (BlacklistTB termAccess, LocaleId loc) {
		this.initialize(termAccess);
		this.loc = loc;
	}

	public int verify (URI docId,
		String subDocId,
		ITextUnit tu,
		Segment segment,
		boolean allowBlacklistSub,
		boolean isSrc)
	{
		issues.clear();

		// iterate over text in segment
		List<BlackTerm> termList = ta.getBlacklistStrings();
		List<Integer> mrkPositions = new ArrayList<Integer>();
		String searchTxtAsIs = TextUnitUtil.getText(segment.getContent(), mrkPositions);
		String searchTxtUpperCase = UCharacter.toUpperCase(loc.toIcuLocale(), searchTxtAsIs);
		mrkPositions = updateMarkerPositions(mrkPositions);

		for (BlackTerm bterm : termList) {
			String search = bterm.doCaseSensitiveMatch ? searchTxtAsIs : searchTxtUpperCase;
			int idx = 0;
			while ((idx = search.indexOf(bterm.searchTerm, idx)) != -1) {
				// check boundaries
				if ((bterm.searchTerm.length()) != search.length() && !allowBlacklistSub) {
					if ((idx > 0) && (idx < search.length() - bterm.searchTerm.length())) {
						if ((Character.isLetter(search.charAt(idx - 1))) || (Character.isLetter(search.charAt(idx + bterm.searchTerm.length())))) {
							// BlackTerm is a substring
							break;
						}
					}
					else if (idx == 0) { // check end
						if (Character.isLetter(search.charAt(idx + bterm.searchTerm.length()))) {
							// BlackTerm is a substring
							break;
						}
					}
					else { // check start
						if (Character.isLetter(search.charAt(idx - 1))) {
							// BlackTerm is a substring
							break;
						}
					}
				}
				
				// Calculate offset
				int rangeStart;
				int rangeEnd;
				int offset;
				if (!mrkPositions.isEmpty()) {
					offset = getOffset(idx, mrkPositions, true);
					rangeStart = idx + offset;
					rangeEnd = idx + bterm.searchTerm.length();
					offset = getOffset(rangeEnd, mrkPositions, false);
					rangeEnd += offset;
				}
				else {
					rangeStart = idx;
					rangeEnd = rangeStart + bterm.searchTerm.length();
				}
				
				// Create issue
				String message;
				if (!bterm.suggestion.isEmpty()) {
					message = String.format("The term \"%s\" is a blacklisted term. Consider using \"%s\".", bterm.text, bterm.suggestion);
				}
				else {
					message = String.format("The term \"%s\" is a blacklisted term. Consider revising.", bterm.text);
				}
				if (bterm.comment != null && !bterm.comment.isEmpty()) {
					message += " More details: " + bterm.comment;
				}
				Issue issue;
				if(isSrc)
					issue = new Issue(docId, subDocId, IssueType.TERMINOLOGY, tu.getId(), segment.getId(), message, TextFragment.fromFragmentToString(segment.text, rangeStart), TextFragment.fromFragmentToString(segment.text, rangeEnd), 0, -1, Issue.DISPSEVERITY_LOW, tu.getName());
				else
					issue = new Issue(docId, subDocId, IssueType.TERMINOLOGY, tu.getId(), segment.getId(), message, 0, -1, TextFragment.fromFragmentToString(segment.text, rangeStart), TextFragment.fromFragmentToString(segment.text, rangeEnd), Issue.DISPSEVERITY_LOW, tu.getName());
				issues.add(issue);

				// iterate
				idx++;
			}
		}
		return issues.size();
	}

	public List<Issue> getIssues() {
		return issues;
	}

	/***
	 * Recalculates the marker positions based on the string without in-line code.
	 * Basing the marker positions on the string with the in-line code removed
	 * makes the calculation of marker offset from the start of the string easier.
	 * 
	 * @param mrkPositions is the list indices for all the in-line code
	 * @return an updated list of all the indices
	 */
	private List<Integer> updateMarkerPositions (List<Integer> mrkPositions) {
		for (int i = 0; i < mrkPositions.size(); i++) {
			int value = mrkPositions.get(i);
			value = value - (i * 2);
			mrkPositions.set(i, value);
		}
		return mrkPositions;
	}
	
	/**
	 * Calculates the offset of the term hit.
	 * As the search is performed on a string where the in-line code has been
	 * removed, the rangeStart and rangeEnd have to be recalculated to take into
	 * account any in-line code that may be present in the original string.
	 * 
	 * @param index is the position of the BlackTerm in the search string
	 * @param mrkPositions is the list of in-line code indices
	 * @param isStart determines if the offset is for the rangeStart or the rangeEnd
	 * @return the offset from the beginning of the string
	 */
	private int getOffset (int index,
		List<Integer> mrkPositions,
		boolean isStart)
	{		
		int offset = 0;
		for (int i = 0; i < mrkPositions.size(); i++) {
			if (mrkPositions.get(i) < index) {
				// count instances
				offset++;
				continue;
			}
			if (mrkPositions.get(i) == index) {
				// if start count instances
				if (isStart) {
					// count instances
					offset++;
					continue;
				}
			}
			if (mrkPositions.get(i) > index) {
				return (offset*2);
			}
		}
		return (offset*2);
	}
}
