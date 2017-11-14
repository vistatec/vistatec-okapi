/*===========================================================================
  Copyright (C) 2016 by the Okapi Framework contributors
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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ibm.icu.text.BreakIterator;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.annotation.IssueType;
import net.sf.okapi.common.resource.ISegments;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.StartSubDocument;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;

public class GeneralChecker extends AbstractChecker {
	private String doubledWordExceptions;
	private BreakIterator breakIterator;
	  // The exact regex equivalent of UCharacter.isUWhiteSpace.
	  // Used to test if the stuff between words is spaces only.
	private final static Pattern UWHITESPACE = Pattern.compile("[\\t\\v\\n\\f\\r\\p{Z}]+");

	@Override
	public void startProcess(LocaleId sourceLocale, LocaleId targetLocale, Parameters params, List<Issue> issues) {
		super.startProcess(sourceLocale, targetLocale, params, issues);

		if (params.getDoubledWord()) {
			breakIterator = BreakIterator.getWordInstance(targetLocale.toIcuLocale());
			// Construct the string of doubled-words that are not errors
			// The working patter is the list like this: ";word1;word2;word3;"
			doubledWordExceptions = ";" + params.getDoubledWordExceptions().toLowerCase() + ";";
		}
	}

	@Override
	public void processStartDocument(StartDocument sd, List<String> sigList) {
		super.processStartDocument(sd, sigList);
	}

	@Override
	public void processStartSubDocument(StartSubDocument ssd) {
		super.processStartSubDocument(ssd);
	}

	@Override
	public void processTextUnit(ITextUnit tu) {
		// Skip non-translatable entries
		if (!tu.isTranslatable()) {
			return;
		}

		// Get the containers
		TextContainer srcCont = tu.getSource();
		TextContainer trgCont = tu.getTarget(getTrgLoc());

		// Check if we have a target
		if (trgCont == null) {
			if (!isMonolingual()) { // No report as error for monolingual files
				// No translation available
				addAnnotationAndReportIssue(IssueType.MISSING_TARGETTU, tu, tu.getSource(), null, "Missing translation.", 0, -1, 0, -1,
						Issue.SEVERITY_HIGH, srcCont.toString(), "", null);
			}
			return;
		}

		ISegments srcSegs = srcCont.getSegments();
		ISegments trgSegs = trgCont.getSegments();

		for (Segment srcSeg : srcSegs) {
			Segment trgSeg = trgSegs.get(srcSeg.getId());
			if (trgSeg == null) {
				addAnnotationAndReportIssue(IssueType.MISSING_TARGETSEG, tu, srcCont, srcSeg.getId(),
						"The source segment has no corresponding target segment.", 0, -1, 0, -1, Issue.SEVERITY_HIGH,
						srcSeg.toString(), "", null);
				continue; // Cannot go further for that segment
			}

			// Check for empty target, if requested
			if (getParams().getEmptyTarget()) {
				if (trgSeg.text.isEmpty() && !srcSeg.text.isEmpty()) {
					addAnnotationAndReportIssue(IssueType.EMPTY_TARGETSEG, tu, srcCont, srcSeg.getId(),
							"The target segment is empty, but its source is not empty.", 0, -1, 0, -1,
							Issue.SEVERITY_HIGH, srcSeg.toString(), "", null);
					continue; // No need to check more if it's empty
				}
			}
			// Check for empty source when target is not empty, if requested
			if (getParams().getEmptySource()) {
				if (srcSeg.text.isEmpty() && !trgSeg.text.isEmpty()) {
					addAnnotationAndReportIssue(IssueType.EMPTY_SOURCESEG, tu, srcCont, srcSeg.getId(),
							"The target segment is not empty, but its source is empty.", 0, -1, 0, -1,
							Issue.SEVERITY_HIGH, srcSeg.toString(), "", null);
					continue; // No need to check more if the source is empty
				}
			}

			// Compile the patterns
			List<PatternItem> patterns = getParams().getPatterns();
			for (PatternItem item : patterns) {
				if (item.enabled) {
					item.compile();
				}
			}

			// Check for target is the same as source, if requested
			if (getParams().getTargetSameAsSource()) {
				if (getParams().getTargetSameAsSourceForSameLanguage() || !getSrcLoc().sameLanguageAs(getTrgLoc())) {
					if (hasMeaningfullText(srcSeg.text)) {
						if (srcSeg.text.compareTo(trgSeg.text, getParams().getTargetSameAsSourceWithCodes()) == 0) {
							// Is the string of the cases where target should be
							// the
							// same? (URL, etc.)
							boolean warn = true;
							if (patterns != null) {
								for (PatternItem item : patterns) {
									String ctext = srcSeg.text.getCodedText();
									if (item.enabled && item.target.equals(PatternItem.SAME)) {
										Matcher m = item.getSourcePattern().matcher(ctext);
										if (m.find()) {
											warn = !ctext.equals(m.group());
											break;
										}
									}
								}
							}
							if (warn) {
								addAnnotationAndReportIssue(IssueType.TARGET_SAME_AS_SOURCE, tu, srcCont, srcSeg.getId(),
										"Translation is the same as the source.", 0, -1, 0, -1, Issue.SEVERITY_MEDIUM,
										srcSeg.toString(), trgSeg.toString(), null);
							}
						}
					}
				}
			}

			// Check all suspect patterns
			checkSuspectPatterns(srcSeg, trgSeg, tu);
		}
		
		// Check for orphan target segments
		for (Segment trgSeg : trgSegs) {
			Segment srcSeg = srcSegs.get(trgSeg.getId());
			if (srcSeg == null) {
				addAnnotationAndReportIssue(IssueType.EXTRA_TARGETSEG, tu, trgCont, trgSeg.getId(),
						String.format("Extra target segment (id=%s).", trgSeg.getId()),
						0, -1, 0, -1, Issue.SEVERITY_HIGH, "", trgSeg.toString(), null);
				continue; // Cannot go further for that segment
			}
		}

		String srcOri = null;
		if (srcCont.contentIsOneSegment()) {
			srcOri = srcCont.toString();
		} else {
			srcOri = srcCont.getUnSegmentedContentCopy().toText();
		}

		String trgOri = null;
		if (trgCont.contentIsOneSegment()) {
			trgOri = trgCont.toString();
		} else {
			trgOri = trgCont.getUnSegmentedContentCopy().toText();
		}
		checkWhiteSpaces(srcOri, trgOri, tu);
		
		setAnnotationIds(srcCont, trgCont);
	}

	/**
	 * Indicates if we have at least one character that is part of the character
	 * set for a "word". digits are considered part of a "word".
	 * 
	 * @param frag
	 *            the text fragment to look at.
	 * @return true if a "word" is detected.
	 */
	private boolean hasMeaningfullText(TextFragment frag) {
		return WORDCHARS.matcher(frag.getCodedText()).find();
	}

	private boolean isSpaceWeCareAbout(char c) {
		return Character.isWhitespace(c) || Character.isSpaceChar(c);
	}

	private void checkWhiteSpaces(String srcOri, String trgOri, ITextUnit tu) {
		// Check for leading whitespaces
		if (getParams().getLeadingWS()) {

			// Missing ones
			for (int i = 0; i < srcOri.length(); i++) {
				if (isSpaceWeCareAbout(srcOri.charAt(i))) {
					if (srcOri.length() > i) {
						if ((trgOri.length() - 1 < i) || (trgOri.charAt(i) != srcOri.charAt(i))) {
							addAnnotationAndReportIssue(IssueType.MISSINGORDIFF_LEADINGWS, tu, tu.getSource(), null,
									String.format("Missing or different leading white space at position %d.", i), i,
									i + 1, 0, -1, Issue.SEVERITY_LOW, srcOri, trgOri, null);							
							break;
						}
					} else {
						addAnnotationAndReportIssue(IssueType.MISSING_LEADINGWS, tu, tu.getSource(), null,
								String.format("Missing leading white space at position %d.", i), i, i + 1, 0, -1,
								Issue.SEVERITY_LOW, srcOri, trgOri, null);
					}
				} else {
					break;
				}
			}

			// Extra ones
			for (int i = 0; i < trgOri.length(); i++) {
				if (isSpaceWeCareAbout(trgOri.charAt(i))) {
					if (srcOri.length() > i) {
						if ((srcOri.length() - 1 < i) || (srcOri.charAt(i) != trgOri.charAt(i))) {
							addAnnotationAndReportIssue(IssueType.EXTRAORDIFF_LEADINGWS, tu, tu.getTarget(getTrgLoc()), null,
									String.format("Extra or different leading white space at position %d.", i), 0, -1,
									i, i + 1, Issue.SEVERITY_LOW, srcOri, trgOri, null);
							break;
						}
					} else {
						addAnnotationAndReportIssue(IssueType.EXTRA_LEADINGWS, tu, tu.getTarget(getTrgLoc()), null,
								String.format("Extra leading white space at position %d.", i), 0, -1, i, i + 1,
								Issue.SEVERITY_LOW, srcOri, trgOri, null);
					}
				} else {
					break;
				}
			}
		}

		// Check for trailing whitespaces
		if (getParams().getTrailingWS()) {
			// Missing ones
			int j = trgOri.length() - 1;
			for (int i = srcOri.length() - 1; i >= 0; i--) {
				if (isSpaceWeCareAbout(srcOri.charAt(i))) {
					if (j >= 0) {
						if ((trgOri.length() - 1 < j) || (trgOri.charAt(j) != srcOri.charAt(i))) {
							addAnnotationAndReportIssue(IssueType.MISSINGORDIFF_TRAILINGWS, tu, tu.getSource(), null,
									String.format("Missing or different trailing white space at position %d", i), i,
									i + 1, 0, -1, Issue.SEVERITY_LOW, srcOri, trgOri, null);
							break;
						}
					} else {
						addAnnotationAndReportIssue(IssueType.MISSING_TRAILINGWS, tu, tu.getSource(), null,
								String.format("Missing trailing white space at position %d.", i), i, i + 1, 0, -1,
								Issue.SEVERITY_LOW, srcOri, trgOri, null);
					}
				} else {
					break;
				}
				j--;
			}

			// Extra ones
			j = srcOri.length() - 1;
			for (int i = trgOri.length() - 1; i >= 0; i--) {
				if (isSpaceWeCareAbout(trgOri.charAt(i))) {
					if (j >= 0) {
						if ((srcOri.length() - 1 < j) || (srcOri.charAt(j) != trgOri.charAt(i))) {
							addAnnotationAndReportIssue(IssueType.EXTRAORDIFF_TRAILINGWS, tu, tu.getTarget(getTrgLoc()), null,
									String.format("Extra or different trailing white space at position %d.", i), 0, -1,
									i, i + 1, Issue.SEVERITY_LOW, srcOri, trgOri, null);
							break;
						}
					} else {
						addAnnotationAndReportIssue(IssueType.EXTRA_TRAILINGWS, tu, tu.getTarget(getTrgLoc()), null,
								String.format("Extra white trailing space at position %d.", i), 0, -1, i, i + 1,
								Issue.SEVERITY_LOW, srcOri, trgOri, null);
					}
				} else {
					break;
				}
				j--;
			}
		}
	}

	private void checkSuspectPatterns(Segment srcSeg, Segment trgSeg, ITextUnit tu) {
		String trgCText = trgSeg.text.getCodedText();

		if (getParams().getDoubledWord()) {
			breakIterator.setText(trgCText);

			int previousPosition = breakIterator.first();
			int currentPosition = breakIterator.next();
			String previousWord = null;
			while (currentPosition != BreakIterator.DONE) {
				final String currentWord = trgCText.substring(previousPosition, currentPosition);
				// Real word, not "in between words"
				if (breakIterator.getRuleStatus() > BreakIterator.WORD_NONE_LIMIT) {
					if (currentWord.equals(previousWord)) {
						if (doubledWordExceptions.indexOf(";" + currentWord.toLowerCase() + ";") == -1) {
							addAnnotationAndReportIssue(IssueType.SUSPECT_PATTERN, tu, tu.getTarget(getTrgLoc()), srcSeg.getId(),
									String.format("Double word: \"%s\" found in the target.", currentWord), 0, -1,
									TextFragment.fromFragmentToString(trgSeg.text, previousPosition),
									TextFragment.fromFragmentToString(trgSeg.text, currentPosition), Issue.SEVERITY_HIGH,
									srcSeg.toString(), trgSeg.toString(), null);
						}
					} else {
						previousWord = currentWord;
					}
				} else {
					// We should have only spaces between words to consider them repeated.
					// Here there is "stuff" in between (think "many, many files"), they are not doubles.
					if (!UWHITESPACE.matcher(currentWord).matches()) {
						previousWord = null;
					}
				}
				previousPosition = currentPosition;
				currentPosition = breakIterator.next();
			}
		}
	}
}
