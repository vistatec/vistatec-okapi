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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.annotation.IssueType;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.ISegments;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.StartSubDocument;
import net.sf.okapi.common.resource.TextContainer;

public class InlineCodesChecker extends AbstractChecker {

	@Override
	public void startProcess(LocaleId sourceLocale, LocaleId targetLocale, Parameters params, List<Issue> issues) {
		super.startProcess(sourceLocale, targetLocale, params, issues);
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
			// Check code differences, if requested
			if (getParams().getCodeDifference()) {
				checkInlineCodes(srcSeg, trgSeg, tu, trgCont);
			}
		}

		setAnnotationIds(srcCont, trgCont);
	}

	// Create a copy of the codes and strip out any that has empty data.
	// They correspond to process-only codes like <df> in TTX or <mrk> in XLIFF
	private ArrayList<Code> stripNoiseCodes(Segment seg) {
		ArrayList<Code> list = new ArrayList<Code>(seg.text.getCodes());
		Iterator<Code> iter = list.iterator();
		while (iter.hasNext()) {
			Code code = iter.next();
			if (getParams().getTypesToIgnore().indexOf(code.getType() + ";") != -1) {
				iter.remove();
			}
		}
		return list;
	}

	private String buildCodeList(List<Code> list) {
		StringBuilder tmp = new StringBuilder();
		for (Code code : list) {
			if (tmp.length() > 0) {
				tmp.append(", ");
			}
			if (code.getData().isEmpty()) {
				tmp.append(code.getOuterData().replaceAll("></x>", "/>"));
			} else { // Show the content
				tmp.append("\"" + code.getData() + "\"");
			}
		}
		return tmp.toString();
	}

	private String buildOpenCloseSequence(ArrayList<Code> list) {
		StringBuilder sb = new StringBuilder();
		for (Code code : list) {
			switch (code.getTagType()) {
			case OPENING:
				sb.append("o");
				break;
			case CLOSING:
				sb.append("c");
				break;
			case PLACEHOLDER:
				if (true) {
					String tmp = code.getData();
					char ch = 'p';
					if (!Util.isEmpty(tmp) && getParams().getGuessOpenClose()) {
						if (tmp.startsWith("</")) {
							ch = 'c';
						} else if (tmp.startsWith("<")) {
							ch = 'o';
						}
						// Make sure the open is not an empty
						if (tmp.endsWith("/>")) {
							ch = 'p';
						}
					}
					// Now add only if it's an open or close
					sb.append(ch);
				}
			}
		}
		return sb.toString();
	}

	private void checkInlineCodes(Segment srcSeg, Segment trgSeg, ITextUnit tu, TextContainer trgCont) {
		ArrayList<Code> srcList = stripNoiseCodes(srcSeg);
		ArrayList<Code> trgList = stripNoiseCodes(trgSeg);

		// If no codes: don't check
		if ((srcList.size() == 0) && (trgList.size() == 0)) {
			return;
		}

		// Prepare the verification of the open-close sequence
		String srcOC = buildOpenCloseSequence(srcList);
		String trgOC = buildOpenCloseSequence(trgList);
		boolean checkOC = true;

		// Check codes missing in target
		Iterator<Code> srcIter = srcList.iterator();
		while (srcIter.hasNext()) {
			Code srcCode = srcIter.next();
			Iterator<Code> trgIter = trgList.iterator();
			while (trgIter.hasNext()) {
				Code trgCode = trgIter.next();
				if (trgCode.getData().isEmpty() && srcCode.getData().isEmpty()) {
					if ((trgCode.getId() == srcCode.getId()) && trgCode.getType().equals(srcCode.getType())) {
						// Found: remove them from lists
						trgIter.remove();
						srcIter.remove();
						break;
					}
				} else if (trgCode.getData().equals(srcCode.getData())) {
					// Found: remove them from lists
					trgIter.remove();
					srcIter.remove();
					break;
				}
			}
		}

		// --- Missing codes
		// Check if any of the missing code is one of the code allowed to be
		// missing
		if (!srcList.isEmpty()) {
			Iterator<Code> iter = srcList.iterator();
			while (iter.hasNext()) {
				if (getParams().missingCodesAllowed.contains(iter.next().getData())) {
					iter.remove();
				}
			}
		}
		// What is left in the source list are the codes missing in the target
		if (!srcList.isEmpty()) {
			addAnnotationAndReportIssue(IssueType.MISSING_CODE, tu, trgCont, srcSeg.getId(),
					"Missing placeholders in the target: " + buildCodeList(srcList), 0, -1, 0, -1, Issue.SEVERITY_MEDIUM,
					srcSeg.toString(), trgSeg.toString(), srcList);
			checkOC = false;
		}

		// --- Extra codes
		// Check if any of the extra code is one of the code allowed to be extra
		if (!trgList.isEmpty()) {
			Iterator<Code> iter = trgList.iterator();
			while (iter.hasNext()) {
				if (getParams().extraCodesAllowed.contains(iter.next().getData())) {
					iter.remove();
				}
			}
		}
		// What is left in the target list are the extra codes in the target
		if (!trgList.isEmpty()) {
			addAnnotationAndReportIssue(IssueType.EXTRA_CODE, tu, trgCont, srcSeg.getId(),
					"Extra placeholders in the target: " + buildCodeList(trgList), 0, -1, 0, -1, Issue.SEVERITY_MEDIUM,
					srcSeg.toString(), trgSeg.toString(), trgList);
			checkOC = false;
		}

		// Check sequence issue in open-close codes
		// This is checked only if we did not found already an error
		if (checkOC) {
			int j = 0;
			boolean done = false;
			for (int i = 0; i < srcOC.length(); i++) {
				if (srcOC.charAt(i) == 'p') {
					continue;
				}
				// Else it's 'o' or 'c'
				while (true) {
					if (trgOC.length() <= j) {
						// No more code of this type
						addAnnotationAndReportIssue(IssueType.SUSPECT_CODE, tu, trgCont, srcSeg.getId(),
								"Suspect sequence of opening and closing target placeholders.", 0, -1, 0, -1,
								Issue.SEVERITY_MEDIUM, srcSeg.toString(), trgSeg.toString(), trgList);
						done = true;
						break;
					}
					// If it's a placeholder, move to the next code
					if (trgOC.charAt(j) == 'p') {
						j++;
						continue;
					}
					// Else: it's a 'o' or 'c'
					if (trgOC.charAt(j) != srcOC.charAt(i)) {
						// Error in sequence
						addAnnotationAndReportIssue(IssueType.SUSPECT_CODE, tu, trgCont, srcSeg.getId(),
								String.format("Suspect sequence of opening and closing placeholders in the target (placeholder %d).",
										i + 1),
								0, -1, 0, -1, Issue.SEVERITY_MEDIUM, srcSeg.toString(), trgSeg.toString(), trgList);
						done = true;
						break;
					}
					j++;
					break; // This code has been checked
				}
				if (done) {
					break;
				}
			}
		}

	}
}
