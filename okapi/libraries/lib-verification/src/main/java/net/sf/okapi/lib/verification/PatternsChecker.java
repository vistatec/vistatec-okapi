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

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.annotation.IssueType;
import net.sf.okapi.common.resource.ISegments;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.StartSubDocument;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;

public class PatternsChecker extends AbstractChecker {
	private List<PatternItem> patterns;

	@Override
	public void startProcess(LocaleId sourceLocale, LocaleId targetLocale, Parameters params, List<Issue> issues) {
		super.startProcess(sourceLocale, targetLocale, params, issues);

		// Compile the patterns
		patterns = params.getPatterns();
		for (PatternItem item : patterns) {
			if (item.enabled) {
				item.compile();
			}
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
			return;
		}

		ISegments srcSegs = srcCont.getSegments();
		ISegments trgSegs = trgCont.getSegments();
		for (Segment srcSeg : srcSegs) {
			Segment trgSeg = trgSegs.get(srcSeg.getId());
			if (trgSeg == null) {
				// Cannot go further for that segment
				continue;
			}
			// Check for patterns, if requested
			if (getParams().getCheckPatterns()) {
				checkPatterns(srcSeg, trgSeg, tu);
			}
		}

		setAnnotationIds(srcCont, trgCont);
	}

	/**
	 * Handle newlines and other rules that aren't
	 */
	private String getPatternPartForDisplay(String part) {
		part = part.replace("\n", "\\n");
		part = part.replace("\r", "\\r");
		return part;
	}

	private void checkPatterns(Segment srcSeg, Segment trgSeg, ITextUnit tu) {
		// --- Source-based search
		// Get the source text
		String srcCText = srcSeg.text.getCodedText();
		// Search for any enabled pattern in the source
		for (PatternItem item : patterns) {
			// Skip disabled items and items that use the target as the base
			if (!item.enabled || !item.fromSource) {
				continue;
			}

			Matcher srcM = item.getSourcePattern().matcher(srcCText);

			// Use a copy for the target: it may get modified for the search
			StringBuilder trgCTextCopy = new StringBuilder(trgSeg.text.getCodedText());

			int from = 0;
			while (srcM.find(from)) {
				// Get the source text corresponding to the match
				String srcPart = srcCText.substring(srcM.start(), srcM.end());
				int start = 0;
				int end = 0;
				boolean found = false;
				boolean expectSame = item.target.equals(PatternItem.SAME);
				// Try to get the corresponding part in the target
				if (expectSame) {
					// If the target pattern is defined as being the same as the
					// source
					// Look for the same text in the source.
					found = ((start = trgCTextCopy.indexOf(srcPart)) != -1);
					if (found) {
						end = start + srcPart.length();
					}
				} else { // Target part has its own pattern
					Matcher trgM = item.getTargetPattern().matcher(trgCTextCopy);
					found = trgM.find();
					if (found) {
						start = trgM.start();
						end = trgM.end();
					}
				}
				// Process result
				if (found) { // Remove that match in case source has several
								// occurrences to match
					trgCTextCopy.delete(start, end);
				} else { // Generate an issue
					String msg;
					if (expectSame) {
						msg = String.format("The source part \"%s\" is not in the target",
								getPatternPartForDisplay(srcPart));
					} else {
						msg = String.format("The source part \"%s\" has no correspondance in the target",
								getPatternPartForDisplay(srcPart));
					}
					// Add the description of the rule triggering the warning
					if (!Util.isEmpty(item.description)) {
						msg += " (from rule: " + item.description + ").";
					}
					addAnnotationAndReportIssue(IssueType.UNEXPECTED_PATTERN, tu, tu.getSource(), srcSeg.getId(), msg,
							TextFragment.fromFragmentToString(srcSeg.text, srcM.start()),
							TextFragment.fromFragmentToString(srcSeg.text, srcM.end()), 0, -1,
							Issue.displaySeverityToSeverity(item.severity),
							srcSeg.toString(), trgSeg.toString(), null);
				}

				from = srcM.end();
			}
		}

		//--- Target-based search
		// Get the target text
		String trgCText = trgSeg.text.getCodedText();
		// Search for any enabled pattern in the source
		for (PatternItem item : patterns) {
			// Skip disabled items and items that use the source as the base
			if (!item.enabled || item.fromSource) {
				continue;
			}

			Matcher trgM = item.getTargetPattern().matcher(trgCText);

			// Use a copy for the source: it may get modified for the search
			StringBuilder srcCTextCopy = new StringBuilder(srcSeg.text.getCodedText());

			while (trgM.find()) {
				// Get the source text corresponding to the match
				String trgPart = trgCText.substring(trgM.start(), trgM.end());
				int start = 0;
				int end = 0;
				boolean found = false;
				boolean expectSame = item.source.equals(PatternItem.SAME);
				// Try to get the corresponding part in the source
				if (expectSame) {
					// If the source pattern is defined as being the same as the target
					// Look for the same text in the source.
					found = ((start = srcCTextCopy.indexOf(trgPart)) != -1);
					if (found) {
						end = start + trgPart.length();
					}
				} else { // Source part has its own pattern
					Matcher srcM = item.getSourcePattern().matcher(srcCTextCopy);
					found = srcM.find();
					if (found) {
						start = srcM.start();
						end = srcM.end();
					}
				}
				// Process result
				if (found) { // Remove that match in case target has several occurrences to match
					srcCTextCopy.delete(start, end);
				} else { // Generate an issue
					String msg;
					if (expectSame) {
						msg = String.format("The target part \"%s\" is not in the source.", trgPart);
					} else {
						msg = String.format("The target part \"%s\" has no correspondance in the source.", trgPart);
					}
					// Add the description of the rule triggering the warning
					if (!Util.isEmpty(item.description)) {
						msg += " (from rule: " + item.description + ").";
					}
					addAnnotationAndReportIssue(IssueType.UNEXPECTED_PATTERN, tu, tu.getTarget(getTrgLoc()), srcSeg.getId(), msg, 0, -1,
							TextFragment.fromFragmentToString(trgSeg.text, trgM.start()),
							TextFragment.fromFragmentToString(trgSeg.text, trgM.end()),
							item.severity,
							srcSeg.toString(), trgSeg.toString(), null);
				}
			}
		}
	}
}
