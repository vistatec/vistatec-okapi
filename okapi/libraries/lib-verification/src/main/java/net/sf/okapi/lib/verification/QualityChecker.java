/*===========================================================================
  Copyright (C) 2010 by the Okapi Framework contributors
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
import java.util.Scanner;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.annotation.GenericAnnotation;
import net.sf.okapi.common.annotation.GenericAnnotationType;
import net.sf.okapi.common.annotation.GenericAnnotations;
import net.sf.okapi.common.annotation.IssueAnnotation;
import net.sf.okapi.common.annotation.IssueType;
import net.sf.okapi.common.resource.ISegments;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.StartSubDocument;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.lib.terminology.simpletb.SimpleTB;

class QualityChecker extends AbstractChecker {

	private LocaleId srcLoc;
	private LocaleId trgLoc;
	private LanguageToolConnector ltConn;
	private TermChecker termChecker;
	private BlacklistChecker blacklistChecker;
	private Parameters params;
	private URI currentDocId;
	private String currentSubDocId;
	private GeneralChecker generalChecker;
	private LengthChecker lengthChecker;
	private InlineCodesChecker inlineCodesChecker;
	private PatternsChecker patternsChecker;
	private CharactersChecker charactersChecker;
	
	@Override
	public void startProcess(LocaleId sourceLocale,
			LocaleId targetLocale,
			Parameters params,
			List<Issue> issues) {
		
		this.srcLoc = sourceLocale;
		this.trgLoc = targetLocale;
		this.params = params;
		generalChecker = new GeneralChecker();
		lengthChecker = new LengthChecker();
		inlineCodesChecker = new InlineCodesChecker();
		patternsChecker = new PatternsChecker();
		charactersChecker = new CharactersChecker();
		
		super.startProcess(sourceLocale, targetLocale, params, issues);
		generalChecker.startProcess(sourceLocale, targetLocale, params, issues);
		lengthChecker.startProcess(sourceLocale, targetLocale, params, issues);
		inlineCodesChecker.startProcess(sourceLocale, targetLocale, params, issues); 
		patternsChecker.startProcess(sourceLocale, targetLocale, params, issues); 
		charactersChecker.startProcess(sourceLocale, targetLocale, params, issues); 

		ltConn = null;
		if (params.getCheckWithLT()) {
			ltConn = new LanguageToolConnector();
			ltConn.initialize(targetLocale, sourceLocale, params.getServerURL(), params.getTranslateLTMsg(),
					params.getLtBilingualMode(), params.getLtTranslationSource(), params.getLtTranslationTarget(),
					params.getLtTranslationServiceKey());
		}

		// Terminology check
		termChecker = null;
		if (params.getCheckTerms()) {
			// Direct use of SimpleTB for now
			termChecker = new TermChecker();
			SimpleTB ta = new SimpleTB(srcLoc, trgLoc);
			ta.guessAndImport(new File(params.getTermsPath()));
			termChecker.initialize(ta, srcLoc, trgLoc, params.getStringMode(), params.getBetweenCodes());
		}

		// Blacklist check
		blacklistChecker = null;
		if (params.getCheckBlacklist()) {
			blacklistChecker = new BlacklistChecker();
			LocaleId termsLocale = params.getBlacklistSrc() ? srcLoc : trgLoc;
			BlacklistTB ta = new BlacklistTB(termsLocale);
			if (params.getBlacklistStream() != null) {
				ta.loadBlacklistStream(params.getBlacklistStream());
			} else {
				ta.guessAndImport(new File(params.getBlacklistPath()));
			}
			blacklistChecker.initialize(ta, termsLocale);
		}

	}

	@Override
	public void processStartDocument (StartDocument sd,
		List<String> sigList)
	{
		currentDocId = (new File(sd.getName())).toURI();
		currentSubDocId = null;
		
		super.processStartDocument(sd, sigList);
		generalChecker.processStartDocument(sd, sigList);
		lengthChecker.processStartDocument(sd, sigList);
		inlineCodesChecker.processStartDocument(sd, sigList); 
		patternsChecker.processStartDocument(sd, sigList); 
		charactersChecker.processStartDocument(sd, sigList); 
	}

	@Override
	public void processStartSubDocument (StartSubDocument ssd) {
		currentSubDocId = ssd.getName();
		if (currentSubDocId == null) {
			currentSubDocId = ssd.getId();
		}
		
		super.processStartSubDocument(ssd);
		generalChecker.processStartSubDocument(ssd);
		lengthChecker.processStartSubDocument(ssd);
		inlineCodesChecker.processStartSubDocument(ssd); 
		patternsChecker.processStartSubDocument(ssd); 
		charactersChecker.processStartSubDocument(ssd); 
	}

	@Override	
	public void processTextUnit(ITextUnit tu) {
		// Skip non-translatable entries
		if (!tu.isTranslatable()) {
			return;
		}
		
		// Get the containers
		TextContainer srcCont = tu.getSource();
		TextContainer trgCont = tu.getTarget(trgLoc);

		harvestExistingAnnotations(srcCont, tu, false);
		harvestExistingAnnotations(trgCont, tu, true);			

		// Skip non-approved entries if requested
		if (params.getScope() != Parameters.SCOPE_ALL) {
			Property prop = trgCont.getProperty(Property.APPROVED);
			if ((prop != null) && prop.getValue().equals("yes")) { // Approved
				if (params.getScope() == Parameters.SCOPE_NOTAPPROVEDONLY) {
					return;
				}
			} else { // Not approved
				if (params.getScope() == Parameters.SCOPE_APPROVEDONLY) {
					return;
				}
			}
		}

		generalChecker.processTextUnit(tu);
		lengthChecker.processTextUnit(tu);
		inlineCodesChecker.processTextUnit(tu); 
		patternsChecker.processTextUnit(tu); 
		charactersChecker.processTextUnit(tu); 
		
		// Check if we have a target
		if (trgCont == null) {			
			return;
		}
		
		ISegments srcSegs = srcCont.getSegments();
		ISegments trgSegs = trgCont.getSegments();

		// Check hidden text (e.g. RTF)
		Property prop = trgCont.getProperty("hashiddentext");
		if (prop != null) {
			// There is a hidden section
			Scanner scan = null;
			try {
				scan = new Scanner(prop.getValue());
				scan.useDelimiter(";");
				TextFragment tf = trgCont.getUnSegmentedContentCopy();
				int start = TextFragment.fromFragmentToString(tf, scan.nextInt());
				int end = TextFragment.fromFragmentToString(tf, scan.nextInt());
				addAnnotationAndReportIssue(IssueType.SUSPECT_PATTERN, tu, trgCont, null,
						"Target content has at least one hidden part.",
						0, -1, start, end, Issue.SEVERITY_HIGH, srcCont.toString(), trgCont.toString(), null);
			} finally {
				if (scan != null) {
					scan.close();
				}
			}
		}

		for (Segment srcSeg : srcSegs) {
			Segment trgSeg = trgSegs.get(srcSeg.getId());			
			if (termChecker != null) {
				if (termChecker.verify(currentDocId, currentSubDocId, tu, srcSeg, trgSeg) > 0) {
					for (Issue issue : termChecker.getIssues()) {
						addAnnotationAndReportIssue(issue.getIssueType(), tu, srcCont, issue.getSegId(), issue.getMessage(),
								issue.getSourceStart(), issue.getSourceEnd(), issue.getTargetStart(), issue.getTargetEnd(),
								issue.getSeverity(), srcSeg.toString(), trgSeg.toString(), issue.getCodes());
					}
				}
			}

			// Check for black listed terms
			if (blacklistChecker != null) {
				if (blacklistChecker.verify(currentDocId, currentSubDocId, tu, trgSeg, params.getAllowBlacklistSub(), false) > 0) {
					for (Issue issue : blacklistChecker.getIssues()) {
						addAnnotationAndReportIssue(issue.getIssueType(), tu, srcCont, issue.getSegId(), issue.getMessage(),
								issue.getSourceStart(), issue.getSourceEnd(), issue.getTargetStart(), issue.getTargetEnd(),
								issue.getSeverity(), srcSeg.toString(), trgSeg.toString(), issue.getCodes());
					}
				}
				if (params.getBlacklistSrc() && blacklistChecker.verify(currentDocId, currentSubDocId, tu, srcSeg, params.getAllowBlacklistSub(), true) > 0) {
					for (Issue issue : blacklistChecker.getIssues()) {
						addAnnotationAndReportIssue(issue.getIssueType(), tu, srcCont, issue.getSegId(), issue.getMessage(),
								issue.getSourceStart(), issue.getSourceEnd(), issue.getTargetStart(), issue.getTargetEnd(),
								issue.getSeverity(), srcSeg.toString(), trgSeg.toString(), issue.getCodes());
					}
				}
			}			

			// Run a check with LanguageTool connector
			if (ltConn != null) {
				if (ltConn.checkSegment(currentDocId, currentSubDocId, srcSeg, trgSeg, tu) > 0) {
					for (Issue issue : ltConn.getIssues()) {
						addAnnotationAndReportIssue(issue.getIssueType(), tu, srcCont, issue.getSegId(), issue.getMessage(),
								issue.getSourceStart(), issue.getSourceEnd(), issue.getTargetStart(), issue.getTargetEnd(),
								issue.getSeverity(), srcSeg.toString(), trgSeg.toString(), issue.getCodes(),
								issue.getString(GenericAnnotationType.LQI_TYPE));
						if (issue.getSourceEnd() == -99) {
							// Special marker indicating a server error
							ltConn = null; // Do not check it again until next re-processing
						}
					}
				}
			}

		}

		setAnnotationIds(srcCont, trgCont);
	}
	
	private void harvestExistingAnnotations(TextContainer tc,
			ITextUnit tu,
			boolean isTarget) {
		if (tc == null) {
			return;
		}
		GenericAnnotations anns = (GenericAnnotations) tc.getAnnotation(GenericAnnotations.class);
		if (anns == null) {
			return;
		}
		List<GenericAnnotation> list = anns.getAnnotations(GenericAnnotationType.LQI);

		for (GenericAnnotation ann : list) {
			IssueAnnotation issue;
			if (ann instanceof IssueAnnotation) {
				issue = (IssueAnnotation) ann;
			} else {
				issue = new IssueAnnotation(ann);
			}
			// Report the issue
			addAnnotationAndReportIssue(issue.getIssueType(), tu, tc, issue.getSegId(),
					issue.getComment(),
					issue.getSourceStart(), issue.getSourceEnd(), issue.getTargetStart(), issue.getTargetEnd(),
					((Double) issue.getSeverity()).intValue(),
					(isTarget ? "" : tc.toString()), (isTarget ? tc.toString() : ""),
					issue.getCodesAsList());
		}
	}
}
