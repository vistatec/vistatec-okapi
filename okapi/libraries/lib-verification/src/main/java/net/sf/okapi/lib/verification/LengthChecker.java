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

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.List;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.annotation.GenericAnnotation;
import net.sf.okapi.common.annotation.GenericAnnotationType;
import net.sf.okapi.common.annotation.GenericAnnotations;
import net.sf.okapi.common.annotation.IssueType;
import net.sf.okapi.common.resource.ISegments;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.StartSubDocument;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextUnitUtil;

public class LengthChecker extends AbstractChecker {
	private CharsetEncoder encoder2 = null;

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

		// Check ITS Storage size for target
		if (getParams().getCheckStorageSize()) {
			checkStorageSize(tu, trgCont);
		}

		ISegments srcSegs = srcCont.getSegments();
		ISegments trgSegs = trgCont.getSegments();

		for (Segment srcSeg : srcSegs) {
			Segment trgSeg = trgSegs.get(srcSeg.getId());
			if (trgSeg == null) {
				// Cannot go further for that segment
				continue;
			}
			// Check segment length
			if (getParams().getCheckMaxCharLength() || getParams().getCheckMinCharLength()
					|| getParams().getCheckAbsoluteMaxCharLength()) {
				// don't check length if target is empty as missing translation will cover
				// the issue
				if (!trgSeg.text.isEmpty()) {
					checkLengths(srcSeg, trgSeg, tu);
				}
			}
		}

		setAnnotationIds(srcCont, trgCont);
	}

	private void checkStorageSize(ITextUnit tu, TextContainer trgTc) {

		if (trgTc == null) {
			return;
		}
		GenericAnnotations anns = trgTc.getAnnotation(GenericAnnotations.class);
		if (anns == null) {
			return;
		}

		GenericAnnotation ga = anns.getFirstAnnotation(GenericAnnotationType.STORAGESIZE);
		if (ga == null) {
			return;
		}
		try {
			int max = ga.getInteger(GenericAnnotationType.STORAGESIZE_SIZE);
			String enc = ga.getString(GenericAnnotationType.STORAGESIZE_ENCODING);
			if ((encoder2 == null) || !encoder2.charset().name().equals(enc)) {
				encoder2 = Charset.forName(enc).newEncoder();
			}
			String lb = ga.getString(GenericAnnotationType.STORAGESIZE_LINEBREAK);

			// Get the plain text
			TextFragment tf;
			if (trgTc.contentIsOneSegment()) {
				tf = trgTc.getFirstContent();
			} else {
				tf = trgTc.getUnSegmentedContentCopy();
			}
			String tmp = TextUnitUtil.getText(tf);
			// Convert the line breaks if needed
			if (lb.equals("crlf")) {
				tmp = tmp.replaceAll("\n", "\r\n");
			}
			// Else: all other values are a single byte, like the default lf, no
			// need to replace

			// Compute the byte length
			CharBuffer cbuf = CharBuffer.wrap(tmp);
			if (!encoder2.canEncode(cbuf)) {
				addAnnotationAndReportIssue(IssueType.TARGET_LENGTH, tu, trgTc, null,
						String.format("Cannot encode one or more characters of the target using %s.", enc),
						0, -1, 0, -1, Issue.SEVERITY_HIGH, tu.getSource().toString(), trgTc.toString(), null);
				return;
			}
			ByteBuffer buf = encoder2.encode(cbuf);
			int len = buf.limit();
			// Correct BOM if needed
			// It seems only UTF-16 gets a BOM prefix
			String bom = "";
			if (len > 2) {
				bom += buf.get(0);
				bom += buf.get(1);
				if (bom.equals("-2-1") || bom.equals("-1-2")) {
					len -= 2;
				}
				if (len > 3) {
					bom += buf.get(2);
					bom += buf.get(3);
					if (bom.endsWith("00-2-1")) {
						len -= 4;
					} else if (bom.equals("-1-200")) {
						len -= 2; // Complements previous -2
					}
				}
			}
			// Check the length
			if (len > max) {
				addAnnotationAndReportIssue(IssueType.TARGET_LENGTH, tu, trgTc, null,
						String.format("Number of bytes in the target (using %s) is: %d. Number allowed: %d.", enc, len, max),
						0, -1, 0, -1, Issue.SEVERITY_HIGH, tu.getSource().toString(), trgTc.toString(), null);
			}
		} catch (Throwable e) {
			addAnnotationAndReportIssue(IssueType.TARGET_LENGTH, tu, trgTc, null,
					"Problem when trying use use ITS storage size property: " + e.getMessage(), 0, -1, 0, -1,
					Issue.SEVERITY_HIGH, tu.getSource().toString(), trgTc.toString(), null);
		}
	}

	private void checkLengths(Segment srcSeg, Segment trgSeg, ITextUnit tu) {
		// Strip inline code markers to look at text only
		int srcLen = TextUnitUtil.getText(srcSeg.text, null).length();
		int trgLen = TextUnitUtil.getText(trgSeg.text, null).length();
		int n;

		// Check absolute character length
		if (getParams().getCheckAbsoluteMaxCharLength()) {
			if (trgLen > getParams().getAbsoluteMaxCharLength()) {
				n = trgLen - getParams().getAbsoluteMaxCharLength();
				addAnnotationAndReportIssue(IssueType.TARGET_LENGTH, tu, tu.getTarget(getTrgLoc()), srcSeg.getId(),
						String.format("The target is longer than %d (by %d).", getParams().getAbsoluteMaxCharLength(),
								n),
						0, -1, getParams().getAbsoluteMaxCharLength(), trgLen, Issue.SEVERITY_HIGH, srcSeg.toString(),
						trgSeg.toString(), null);
			}
		}

		if (getParams().getCheckMaxCharLength()) {
			if (srcLen <= getParams().getMaxCharLengthBreak()) {
				n = (srcLen == 0 ? 0 : (int) ((srcLen * getParams().getMaxCharLengthBelow()) / 100));
			} else {
				n = (srcLen == 0 ? 0 : (int) ((srcLen * getParams().getMaxCharLengthAbove()) / 100));
			}
			if (trgLen > n) {
				double d = (((float) trgLen) / (srcLen == 0 ? 1.0 : ((float) srcLen))) * 100.0;
				addAnnotationAndReportIssue(IssueType.TARGET_LENGTH, tu, tu.getTarget(getTrgLoc()), srcSeg.getId(),
						String.format("The target is suspiciously longer than its source (%.2f%% of the source).", d),
						0, -1, 0, -1, Issue.SEVERITY_LOW, srcSeg.toString(), trgSeg.toString(), null);
			}
		}

		if (getParams().getCheckMinCharLength()) {
			if (srcLen <= getParams().getMinCharLengthBreak()) {
				n = (srcLen == 0 ? 0 : (int) ((srcLen * getParams().getMinCharLengthBelow()) / 100));
			} else {
				n = (srcLen == 0 ? 0 : (int) ((srcLen * getParams().getMinCharLengthAbove()) / 100));
			}
			if (trgSeg.text.getCodedText().length() < n) {
				double d = (((float) trgLen) / (srcLen == 0 ? 1.0 : ((float) srcLen))) * 100.0;
				addAnnotationAndReportIssue(IssueType.TARGET_LENGTH, tu, tu.getTarget(getTrgLoc()), srcSeg.getId(),
						String.format("The target is suspiciously shorter than its source (%.2f%% of the source).", d),
						0, -1, 0, -1, Issue.SEVERITY_LOW, srcSeg.toString(), trgSeg.toString(), null);
			}
		}
	}
}
