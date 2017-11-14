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

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.annotation.GenericAnnotations;
import net.sf.okapi.common.annotation.ITSLQIAnnotations;
import net.sf.okapi.common.annotation.IssueAnnotation;
import net.sf.okapi.common.annotation.IssueType;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.StartSubDocument;
import net.sf.okapi.common.resource.TextContainer;

public abstract class AbstractChecker {
	public final static Pattern WORDCHARS = Pattern.compile("[\\p{Ll}\\p{Lu}\\p{Lt}\\p{Lo}\\p{Nd}]");

	private LocaleId srcLoc;
	private LocaleId trgLoc;
	private URI currentDocId;
	private String currentSubDocId;
	private Parameters params;
	private List<Issue> issues;
	private List<String> sigList;
	private boolean monolingual;

	public void startProcess(LocaleId sourceLocale, LocaleId targetLocale, Parameters params, List<Issue> issues) {
		this.srcLoc = sourceLocale;
		this.trgLoc = targetLocale;
		this.params = params;
		this.issues = issues;
	}

	public void processStartDocument(StartDocument sd, List<String> sigList) {
		currentDocId = (new File(sd.getName())).toURI();
		this.sigList = sigList;
		monolingual = !sd.isMultilingual();
		currentSubDocId = null;
	}

	public void processStartSubDocument(StartSubDocument ssd) {
		currentSubDocId = ssd.getName();
		if (currentSubDocId == null) {
			currentSubDocId = ssd.getId();
		}
	}

	public abstract void processTextUnit(ITextUnit tu);

	public void addAnnotationAndReportIssue(IssueType issueType, ITextUnit tu, TextContainer tc, String segId,
			String comment, int srcStart, int srcEnd, int trgStart, int trgEnd,
			double severity, String srcOri, String trgOri, List<Code> codes) {
		addAnnotationAndReportIssue(issueType, tu, tc, segId, comment,
				srcStart, srcEnd, trgStart, trgEnd, severity, srcOri, trgOri, codes, null);
	}

	public void addAnnotationAndReportIssue(IssueType issueType, ITextUnit tu, TextContainer tc, String segId,
			String comment, int srcStart, int srcEnd, int trgStart, int trgEnd,
			double severity, String srcOri, String trgOri, List<Code> codes, String itsType) {
		reportIssue(issueType, tu, segId, comment, srcStart, srcEnd, trgStart, trgEnd, severity, srcOri, trgOri, codes);
		addAnnotation(tc, segId, issueType, comment, srcStart, srcEnd, trgStart, trgEnd, severity, codes, itsType);
	}

	public void addAnnotation(TextContainer tc, String segId, IssueType issueType, String comment, int srcStart,
			int srcEnd, int trgStart, int trgEnd, double severity, List<Code> codes) {
		IssueAnnotation ann = new IssueAnnotation(issueType, comment, severity, segId, srcStart, srcEnd, trgStart,
				trgEnd, codes);
		ITSLQIAnnotations.addAnnotations(tc, ann);
	}

	public void addAnnotation(TextContainer tc, String segId, IssueType issueType, String comment, int srcStart,
			int srcEnd, int trgStart, int trgEnd, double severity, List<Code> codes, String itsType) {
		IssueAnnotation ann = new IssueAnnotation(issueType, comment, severity, segId, srcStart, srcEnd, trgStart,
				trgEnd, codes);
		ITSLQIAnnotations.addAnnotations(tc, ann);
		if (itsType != null)
			ann.setITSType(itsType);
	}

	public void reportIssue(IssueType issueType, ITextUnit tu, String segId, String message, int srcStart,
			int srcEnd, int trgStart, int trgEnd, double severity, String srcOri, String trgOri, List<Code> codes) {
		Issue issue = new Issue(currentDocId, currentSubDocId, issueType, tu.getId(), segId, message, srcStart, srcEnd,
				trgStart, trgEnd, severity, tu.getName());
		issue.setCodes(codes);
		issues.add(issue);
		issue.setEnabled(true);
		issue.setSource(srcOri);
		issue.setTarget(trgOri);

		if (sigList != null) {
			// Disable any issue for which we have the signature in the list
			issue.setEnabled(!sigList.contains(issue.getSignature()));
		}
	}

	public void reportIssue(Issue init, ITextUnit tu, String srcOri, String trgOri, Object extra) {
		Issue issue = new Issue(currentDocId, currentSubDocId, init.getIssueType(), tu.getId(), init.getSegId(),
				init.getMessage(), init.getSourceStart(), init.getSourceEnd(), init.getTargetStart(),
				init.getTargetEnd(), init.getSeverity(), tu.getName());
		issue.setCodes(init.getCodes());
		issues.add(issue);
		issue.setEnabled(true);
		issue.setSource(srcOri);
		issue.setTarget(trgOri);

		if (sigList != null) {
			// Disable any issue for which we have the signature in the list
			issue.setEnabled(!sigList.contains(issue.getSignature()));
		}
	}

	public void setAnnotationIds(TextContainer srcCont, TextContainer trgCont) {
		// Make sure the annotation sets have IDs
		GenericAnnotations anns = srcCont.getAnnotation(GenericAnnotations.class);
		if (anns != null) {
			anns.setData(Util.makeId(UUID.randomUUID().toString()));
		}
		anns = trgCont.getAnnotation(GenericAnnotations.class);
		if (anns != null) {
			anns.setData(Util.makeId(UUID.randomUUID().toString()));
		}
	}

	public LocaleId getSrcLoc() {
		return srcLoc;
	}

	public LocaleId getTrgLoc() {
		return trgLoc;
	}

	public URI getCurrentDocId() {
		return currentDocId;
	}

	public String getCurrentSubDocId() {
		return currentSubDocId;
	}

	public Parameters getParams() {
		return params;
	}

	public List<Issue> getIssues() {
		return issues;
	}

	public List<String> getSigList() {
		return sigList;
	}

	public boolean isMonolingual() {
		return monolingual;
	}
}
