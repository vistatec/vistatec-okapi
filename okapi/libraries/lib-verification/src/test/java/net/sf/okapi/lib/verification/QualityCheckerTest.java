/*===========================================================================
  Copyright (C) 2010-2011 by the Okapi Framework contributors
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.okapi.common.FileLocation;
import net.sf.okapi.common.IResource;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.annotation.GenericAnnotation;
import net.sf.okapi.common.annotation.GenericAnnotationType;
import net.sf.okapi.common.annotation.GenericAnnotations;
import net.sf.okapi.common.annotation.IssueType;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.common.resource.TextUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class QualityCheckerTest {

	private QualityCheckSession session;
	private LocaleId locEN = LocaleId.ENGLISH;
	private LocaleId locFR = LocaleId.FRENCH;
	private LocaleId locJA = LocaleId.JAPANESE;
	private LocaleId locTR = LocaleId.fromString("tr");
	private FileLocation location;

	public QualityCheckerTest () {
		location = FileLocation.fromClass(QualityCheckerTest.class);
	}

	@Before
	public void setUp() {
		session = new QualityCheckSession();
		session.startProcess(locEN, locFR);
	}

	@Test
	public void testMISSING_TARGETTU () {
		// Create source with non-empty content
		// but no target
		ITextUnit tu = new TextUnit("id", "source");

		session.processTextUnit(tu);
		List<Issue> issues = session.getIssues();
		assertEquals(1, issues.size());
		assertEquals(IssueType.MISSING_TARGETTU, issues.get(0).getIssueType());
	}

	@Test
	public void testEMPTY_TARGETSEG () {
		// Create TU with source of non-empty segment
		// and target of empty segment
		ITextUnit tu = new TextUnit("id", "source");
		tu.setTarget(locFR, new TextContainer());

		session.processTextUnit(tu);
		List<Issue> issues = session.getIssues();
		assertEquals(1, issues.size());
		assertEquals(IssueType.EMPTY_TARGETSEG, issues.get(0).getIssueType());
	}

	@Test
	public void testEMPTY_SOURCESEG () {
		// Create TU with source of non-empty segment
		// and target of empty segment
		ITextUnit tu = new TextUnit("id", "");
		tu.setTarget(locFR, new TextContainer("target"));

		session.processTextUnit(tu);
		List<Issue> issues = session.getIssues();
		assertEquals(2, issues.size());
		assertEquals(IssueType.EMPTY_SOURCESEG, issues.get(0).getIssueType());
	}

	@Test
	public void testMISSING_TARGETSEG () {
		// Create TU with source of two segments
		// and target of one segment
		TextContainer tc = new TextContainer("srctext1");
		tc.getSegments().append(new Segment("s2", new TextFragment("srctext2")));
		ITextUnit tu = new TextUnit("id");
		tu.setSource(tc);
		tu.setTarget(locFR, new TextContainer("trgext1"));

		session.processTextUnit(tu);
		List<Issue> issues = session.getIssues();
		assertEquals(1, issues.size());
		assertEquals(IssueType.MISSING_TARGETSEG, issues.get(0).getIssueType());
	}

	@Test
	public void testEMPTY_TARGETSEG2 () {
		// Create TU with source of two segments
		// and target of two segments but one empty
		TextContainer tc = new TextContainer("srctext1");
		tc.getSegments().append(new Segment("s2", new TextFragment("srctext2")));
		ITextUnit tu = new TextUnit("id");
		tu.setSource(tc);
		tc = new TextContainer("trgtext1");
		tc.getSegments().append(new Segment("s2", new TextFragment()));
		tu.setTarget(locFR, tc);

		session.processTextUnit(tu);
		List<Issue> issues = session.getIssues();
		assertEquals(1, issues.size());
		assertEquals(IssueType.EMPTY_TARGETSEG, issues.get(0).getIssueType());
	}

	@Test
	public void testMISSINGORDIFF_LEADINGWS () {
		ITextUnit tu = new TextUnit("id", "  srctext");
		tu.setTarget(locFR, new TextContainer("trgext"));

		session.processTextUnit(tu);
		List<Issue> issues = session.getIssues();
		assertEquals(1, issues.size());
		assertEquals(IssueType.MISSINGORDIFF_LEADINGWS, issues.get(0).getIssueType());
	}

	@Test
	public void testMISSINGORDIFF_TRAILINGWS () {
		ITextUnit tu = new TextUnit("id", " srctext ");
		tu.setTarget(locFR, new TextContainer(" trgext"));

		session.processTextUnit(tu);
		List<Issue> issues = session.getIssues();
		assertEquals(1, issues.size());
		assertEquals(IssueType.MISSINGORDIFF_TRAILINGWS, issues.get(0).getIssueType());
	}

	@Test
	public void testEXTRAORDIFF_LEADINGWS () {
		ITextUnit tu = new TextUnit("id", "  srctext");
		tu.setTarget(locFR, new TextContainer("   trgext"));

		session.processTextUnit(tu);
		List<Issue> issues = session.getIssues();
		assertEquals(1, issues.size());
		assertEquals(IssueType.EXTRAORDIFF_LEADINGWS, issues.get(0).getIssueType());
	}

	@Test
	public void testEXTRAORDIFF_TRAILINGWS () {
		ITextUnit tu = new TextUnit("id", "srctext  ");
		tu.setTarget(locFR, new TextContainer("trgtext   "));

		session.processTextUnit(tu);
		List<Issue> issues = session.getIssues();
		assertEquals(1, issues.size());
		assertEquals(IssueType.EXTRAORDIFF_TRAILINGWS, issues.get(0).getIssueType());
	}

	@Test
	public void testTARGET_SAME_AS_SOURCE () {
		ITextUnit tu = new TextUnit("id", "src text");
		tu.setTarget(locFR, new TextContainer("src text"));

		session.processTextUnit(tu);
		List<Issue> issues = session.getIssues();
		assertEquals(1, issues.size());
		assertEquals(IssueType.TARGET_SAME_AS_SOURCE, issues.get(0).getIssueType());
	}

	@Test
	public void testTARGET_SAME_AS_SOURCE_withoutWords () {
		ITextUnit tu = new TextUnit("id", ":?%$#@#_~`()[]{}=+-");
		tu.setTarget(locFR, new TextContainer(":?%$#@#_~`()[]{}=+-"));

		session.processTextUnit(tu);
		List<Issue> issues = session.getIssues();
		assertEquals(0, issues.size());
	}

	@Test
	public void testTARGET_SAME_AS_SOURCE_WithSameCodes () {
		ITextUnit tu = new TextUnit("id", "src text");
		tu.getSource().getSegments().get(0).text.append(TagType.PLACEHOLDER, "codeType", "<code/>");
		tu.setTarget(locFR, new TextContainer("src text"));
		tu.getTarget(locFR).getSegments().get(0).text.append(TagType.PLACEHOLDER, "codeType", "<code/>");

		session.processTextUnit(tu);
		List<Issue> issues = session.getIssues();
		assertEquals(1, issues.size());
		assertEquals(IssueType.TARGET_SAME_AS_SOURCE, issues.get(0).getIssueType());
	}

	@Test
	public void testTARGET_SAME_AS_SOURCE_WithDiffCodes () {
		ITextUnit tu = new TextUnit("id", "src text");
		tu.getSource().getSegments().get(0).text.append(TagType.PLACEHOLDER, "codeType", "<code/>");
		tu.setTarget(locFR, new TextContainer("src text"));
		tu.getTarget(locFR).getSegments().get(0).text.append(TagType.PLACEHOLDER, "codeType", "<etc/>");

		session.processTextUnit(tu);
		List<Issue> issues = session.getIssues();
		// We have code difference warnings but no target==source warning
		assertEquals(2, issues.size());
		assertEquals(IssueType.MISSING_CODE, issues.get(0).getIssueType());
		assertEquals(IssueType.EXTRA_CODE, issues.get(1).getIssueType());
	}

	@Test
	public void testTARGET_SAME_AS_SOURCE_WithDiffCodesTurnedOff () {
		ITextUnit tu = new TextUnit("id", "src text");
		tu.getSource().getSegments().get(0).text.append(TagType.PLACEHOLDER, "codeType", "<code/>");
		tu.setTarget(locFR, new TextContainer("src text"));
		tu.getTarget(locFR).getSegments().get(0).text.append(TagType.PLACEHOLDER, "codeType", "<etc/>");

		session.getParameters().setTargetSameAsSourceWithCodes(false);
		session.processTextUnit(tu);
		List<Issue> issues = session.getIssues();
		// We have code difference and target==source warnings
		assertEquals(3, issues.size());
		assertEquals(IssueType.TARGET_SAME_AS_SOURCE, issues.get(0).getIssueType());
		assertEquals(IssueType.MISSING_CODE, issues.get(1).getIssueType());
		assertEquals(IssueType.EXTRA_CODE, issues.get(2).getIssueType());
	}

	@Test
	public void testCODE_DIFFERENCE () {
		ITextUnit tu = new TextUnit("id", "src ");
		tu.getSource().getSegments().get(0).text.append(TagType.PLACEHOLDER, "codeType", "<code/>");
		tu.setTarget(locFR, new TextContainer("trg "));
		tu.getTarget(locFR).getSegments().get(0).text.append(TagType.PLACEHOLDER, "codeType", "<CODE />");

		session.processTextUnit(tu);
		List<Issue> issues = session.getIssues();
		assertEquals(2, issues.size());
		assertEquals(IssueType.MISSING_CODE, issues.get(0).getIssueType());
		assertEquals(IssueType.EXTRA_CODE, issues.get(1).getIssueType());
	}

	@Test
	public void testCODE_OCSEQUENCE () {
		ITextUnit tu = new TextUnit("id", "src ");
		tu.getSource().getSegments().get(0).text.append(TagType.OPENING, "b", "<b>");
		tu.getSource().getSegments().get(0).text.append("text");
		tu.getSource().getSegments().get(0).text.append(TagType.CLOSING, "b", "</b>");
		tu.setTarget(locFR, new TextContainer("trg "));
		tu.getTarget(locFR).getSegments().get(0).text.append(TagType.CLOSING, "b", "</b>");
		tu.getTarget(locFR).getSegments().get(0).text.append("text");
		tu.getTarget(locFR).getSegments().get(0).text.append(TagType.OPENING, "b", "<b>");

		session.processTextUnit(tu);
		List<Issue> issues = session.getIssues();
		assertEquals(1, issues.size());
		assertEquals(IssueType.SUSPECT_CODE, issues.get(0).getIssueType());
	}

	@Test
	public void testCODE_OCSequenceNoError () {
		ITextUnit tu = new TextUnit("id", "src ");
		tu.getSource().getSegments().get(0).text.append(TagType.OPENING, "i", "<i>");
		tu.getSource().getSegments().get(0).text.append(TagType.CLOSING, "i", "</i>");
		tu.getSource().getSegments().get(0).text.append(TagType.OPENING, "b", "<b>");
		tu.getSource().getSegments().get(0).text.append("text");
		tu.getSource().getSegments().get(0).text.append(TagType.CLOSING, "b", "</b>");
		tu.getSource().getSegments().get(0).text.append(TagType.PLACEHOLDER, "br", "<br/>");
		// target with moved codes (no parent changes)
		tu.setTarget(locFR, new TextContainer("trg "));
		tu.getTarget(locFR).getSegments().get(0).text.append(TagType.OPENING, "b", "<b>");
		tu.getTarget(locFR).getSegments().get(0).text.append(TagType.PLACEHOLDER, "br", "<br/>");
		tu.getTarget(locFR).getSegments().get(0).text.append("text");
		tu.getTarget(locFR).getSegments().get(0).text.append(TagType.CLOSING, "b", "</b>");
		tu.getTarget(locFR).getSegments().get(0).text.append(TagType.OPENING, "i", "<i>");
		tu.getTarget(locFR).getSegments().get(0).text.append(TagType.CLOSING, "i", "</i>");

		session.processTextUnit(tu);
		List<Issue> issues = session.getIssues();
		assertEquals(0, issues.size());
	}

	@Test
	public void testCODE_DIFFERENCE_OrderDiffIsOK () {
		ITextUnit tu = new TextUnit("id", "src ");
		tu.getSource().getSegments().get(0).text.append(TagType.PLACEHOLDER, "codeType", "<code1/>");
		tu.getSource().getSegments().get(0).text.append(" and ");
		tu.getSource().getSegments().get(0).text.append(TagType.PLACEHOLDER, "codeType", "<code2/>");
		tu.setTarget(locFR, new TextContainer("trg "));
		tu.getTarget(locFR).getSegments().get(0).text.append(TagType.PLACEHOLDER, "codeType", "<code2/>");
		tu.getTarget(locFR).getSegments().get(0).text.append(" et ");
		tu.getTarget(locFR).getSegments().get(0).text.append(TagType.PLACEHOLDER, "codeType", "<code1/>");

		session.processTextUnit(tu);
		List<Issue> issues = session.getIssues();
		assertEquals(0, issues.size());
	}

	@Test
	public void testTARGET_SAME_AS_SOURCE_WithDifferentCodes () {
		ITextUnit tu = new TextUnit("id", "src text");
		tu.getSource().getSegments().get(0).text.append(TagType.PLACEHOLDER, "codeType", "<code/>");
		tu.setTarget(locFR, new TextContainer("src text"));
		tu.getTarget(locFR).getSegments().get(0).text.append(TagType.PLACEHOLDER, "codeType", "<CODE/>");

		session.getParameters().setCodeDifference(false);
		session.startProcess(locEN, locFR);

		session.processTextUnit(tu);
		List<Issue> issues = session.getIssues();
		// Codes are different, since the option is code-sensitive: no issue (target not the same as source)
		assertEquals(0, issues.size());
	}

	@Test
	public void testTARGET_SAME_AS_SOURCE_WithDifferentCodes_CodeInsensitive () {
		ITextUnit tu = new TextUnit("id", "src text");
		tu.getSource().getSegments().get(0).text.append(TagType.PLACEHOLDER, "codeType", "<code/>");
		tu.setTarget(locFR, new TextContainer("src text"));
		tu.getTarget(locFR).getSegments().get(0).text.append(TagType.PLACEHOLDER, "codeType", "<CODE/>");

		session.getParameters().setCodeDifference(false);
		session.getParameters().setTargetSameAsSourceWithCodes(false);
		session.startProcess(locEN, locFR);

		session.processTextUnit(tu);
		List<Issue> issues = session.getIssues();
		// Codes are different, since the option is NOT code-sensitive: issue raised (target = source)
		assertEquals(1, issues.size());
		assertEquals(IssueType.TARGET_SAME_AS_SOURCE, issues.get(0).getIssueType());
	}

	@Test
	public void testTARGET_SAME_AS_SOURCE_NoIssue () {
		ITextUnit tu = new TextUnit("id", "  \t\n ");
		tu.setTarget(locFR, new TextContainer("  \t\n "));

		session.processTextUnit(tu);
		List<Issue> issues = session.getIssues();
		assertEquals(0, issues.size());
	}

	@Test
	public void testMISSING_PATTERN () {
		ITextUnit tu = new TextUnit("id", "src text !? %s");
		tu.setTarget(locFR, new TextContainer("trg text"));
		ArrayList<PatternItem> list = new ArrayList<PatternItem>();
		list.add(new PatternItem("[!\\?]", PatternItem.SAME, true, Issue.DISPSEVERITY_LOW));
		list.add(new PatternItem("%s", PatternItem.SAME, true, Issue.DISPSEVERITY_HIGH));

		session.getParameters().setPatterns(list);
		session.startProcess(locEN, locFR); // Make sure we re-initialize

		session.processTextUnit(tu);
		List<Issue> issues = session.getIssues();
		assertEquals(3, issues.size());
		assertEquals(IssueType.UNEXPECTED_PATTERN, issues.get(0).getIssueType());
		assertEquals(9, issues.get(0).getSourceStart());
		assertEquals(IssueType.UNEXPECTED_PATTERN, issues.get(1).getIssueType());
		assertEquals(10, issues.get(1).getSourceStart());
		assertEquals(IssueType.UNEXPECTED_PATTERN, issues.get(2).getIssueType());
		assertEquals(12, issues.get(2).getSourceStart());
	}

	@Test
	public void testMISSING_PATTERN_ForURL () {
		ITextUnit tu = new TextUnit("id", "test: http://thisisatest.com.");
		tu.setTarget(locFR, new TextContainer("test: http://thisBADtest.com"));
		session.startProcess(locEN, locFR); // Make sure we re-initialize
		session.processTextUnit(tu);
		List<Issue> issues = session.getIssues();
		assertEquals(1, issues.size());
		assertEquals(IssueType.UNEXPECTED_PATTERN, issues.get(0).getIssueType());
		assertEquals(6, issues.get(0).getSourceStart());
		assertEquals(28, issues.get(0).getSourceEnd());
	}

	@Test
	public void testNoIssues () {
		ITextUnit tu = new TextUnit("id", "  Text {with} (123). ");
		tu.setTarget(locFR, new TextContainer("  Texte {avec} (123). "));

		session.processTextUnit(tu);
		List<Issue> issues = session.getIssues();
		assertEquals(0, issues.size());
	}

	@Test
	public void testMaxLength () {
		session.getParameters().setMaxCharLengthBreak(9);
		session.getParameters().setMaxCharLengthAbove(149);
		session.getParameters().setMaxCharLengthBelow(200);

		ITextUnit tu = new TextUnit("id", "abcdefghij"); // 10 chars -> use above
		tu.setTarget(locFR, new TextContainer("123456789012345")); // 15 chars
		session.startProcess(locEN, locFR);
		session.processTextUnit(tu);
		List<Issue> issues = session.getIssues();
		assertEquals(1, issues.size());
		assertEquals(IssueType.TARGET_LENGTH, issues.get(0).getIssueType());

		tu = new TextUnit("id", "abcdefghi"); // 9 chars -> use below
		tu.setTarget(locFR, new TextContainer("123456789012345678")); // 18 chars (==200% of src)
		session.getIssues().clear();
		session.startProcess(locEN, locFR);
		session.processTextUnit(tu);
		issues = session.getIssues();
		assertEquals(0, issues.size());

		tu.setTarget(locFR, new TextContainer("1234567890123456789")); // 19 chars (>200% of src)
		session.processTextUnit(tu);
		issues = session.getIssues();
		assertEquals(1, issues.size());
		assertEquals(IssueType.TARGET_LENGTH, issues.get(0).getIssueType());
	}

	@Test
	public void testMinLength () {
		session.getParameters().setMinCharLengthBreak(9);
		session.getParameters().setMinCharLengthAbove(100);
		session.getParameters().setMinCharLengthBelow(50);

		ITextUnit tu = new TextUnit("id", "abcdefghij"); // 10 chars -> use above
		tu.setTarget(locFR, new TextContainer("123456789")); // 10 chars (<100% of src)
		session.startProcess(locEN, locFR);
		session.processTextUnit(tu);
		List<Issue> issues = session.getIssues();
		assertEquals(1, issues.size());
		assertEquals(IssueType.TARGET_LENGTH, issues.get(0).getIssueType());

		tu = new TextUnit("id", "abcdefghi"); // 9 chars -> use below
		tu.setTarget(locFR, new TextContainer("12345")); // 5 chars (==50% of src)
		session.getIssues().clear();
		session.startProcess(locEN, locFR);
		session.processTextUnit(tu);
		issues = session.getIssues();
		assertEquals(0, issues.size());

		tu.setTarget(locFR, new TextContainer("123")); // 4 chars (<50% of src)
		session.processTextUnit(tu);
		issues = session.getIssues();
		assertEquals(1, issues.size());
		assertEquals(IssueType.TARGET_LENGTH, issues.get(0).getIssueType());
	}

	@Test
	public void testTERMINOLOGY () {
		ITextUnit tu = new TextUnit("id", "summer and WINTER");
		tu.setTarget(locFR, new TextContainer("\u00e9T\u00e9 et printemps"));

		session.getParameters().setCheckTerms(true);
		session.getParameters().setTermsPath(location.in("/test01.tsv").toString());
		session.startProcess(locEN, locFR); // Make sure we re-initialize

		session.processTextUnit(tu);
		List<Issue> issues = session.getIssues();
		assertEquals(1, issues.size());
		assertEquals(IssueType.TERMINOLOGY, issues.get(0).getIssueType());
	}

	@Test
	public void testStorageSizeInvalidChar () {
		ITextUnit tu = new TextUnit("id", "abc");
		tu.setTarget(locFR, new TextContainer("abcXYZ"));
		tu.getTarget(locFR).setAnnotation(new GenericAnnotations(new GenericAnnotation(GenericAnnotationType.STORAGESIZE,
			GenericAnnotationType.STORAGESIZE_SIZE, 4,
			GenericAnnotationType.STORAGESIZE_LINEBREAK, "lf",
			GenericAnnotationType.STORAGESIZE_ENCODING, "iso-8859-1"))); // cannot handle Japanese \u3027
		session.startProcess(locEN, locFR); // Make sure we re-initialize
		session.processTextUnit(tu);
		List<Issue> issues = session.getIssues();
		assertEquals(1, issues.size());
		assertEquals(IssueType.TARGET_LENGTH, issues.get(0).getIssueType());
	}

	@Test
	public void testStorageSizeUTF8 () {
		ITextUnit tu = new TextUnit("id", "1234567890\n"); // UTF-8: 11 bytes + 1 for additional CR = 12
		tu.setTarget(locFR, new TextContainer("+1234567890\n")); // UTF-8: 12 bytes + 1 for additional CR = 13
		tu.getTarget(locFR).setAnnotation(new GenericAnnotations(new GenericAnnotation(GenericAnnotationType.STORAGESIZE,
			GenericAnnotationType.STORAGESIZE_SIZE, 12,
			GenericAnnotationType.STORAGESIZE_LINEBREAK, "crlf",
			GenericAnnotationType.STORAGESIZE_ENCODING, "UTF-8")));
		session.startProcess(locEN, locFR); // Make sure we re-initialize
		session.processTextUnit(tu);
		List<Issue> issues = session.getIssues();
		assertEquals(1, issues.size());
		assertEquals(IssueType.TARGET_LENGTH, issues.get(0).getIssueType());
	}

	@Test
	public void testStorageSizeUTF16 () {
		ITextUnit tu = new TextUnit("id", "1234567890\n"); // UTF-16: 22 bytes + 2 for additional CR = 24
		tu.setTarget(locFR, new TextContainer("+1234567890\n")); // UTF-16: 24 bytes + 2 for additional CR = 26
		tu.getTarget(locFR).setAnnotation(new GenericAnnotations(new GenericAnnotation(GenericAnnotationType.STORAGESIZE,
			GenericAnnotationType.STORAGESIZE_SIZE, 24,
			GenericAnnotationType.STORAGESIZE_LINEBREAK, "crlf",
			GenericAnnotationType.STORAGESIZE_ENCODING, "UTF-16")));
		session.startProcess(locEN, locFR); // Make sure we re-initialize
		session.processTextUnit(tu);
		List<Issue> issues = session.getIssues();
		assertEquals(1, issues.size());
		assertEquals(IssueType.TARGET_LENGTH, issues.get(0).getIssueType());
	}

	@Test
	public void testStorageSizeUTF32 () {
		ITextUnit tu = new TextUnit("id", "1234567890\n"); // UTF-32: 44 bytes + 4 bytes for additional CR = 48
		tu.setTarget(locFR, new TextContainer("+1234567890\n")); // UTF-32: 48 bytes + 4 for additional CR = 52
		tu.getTarget(locFR).setAnnotation(new GenericAnnotations(new GenericAnnotation(GenericAnnotationType.STORAGESIZE,
			GenericAnnotationType.STORAGESIZE_SIZE, 48,
			GenericAnnotationType.STORAGESIZE_LINEBREAK, "crlf",
			GenericAnnotationType.STORAGESIZE_ENCODING, "UTF-32")));
		session.startProcess(locEN, locFR); // Make sure we re-initialize
		session.processTextUnit(tu);
		List<Issue> issues = session.getIssues();
		assertEquals(1, issues.size());
		assertEquals(IssueType.TARGET_LENGTH, issues.get(0).getIssueType());
	}

	@Test
	public void testAllowedCharacters () {
		ITextUnit tu = new TextUnit("id", "Summer and\nspring");
		tu.setTarget(locFR, new TextContainer("\u00e9t\u00e9 et printemps"));
		tu.getTarget(locFR).setAnnotation(new GenericAnnotations(new GenericAnnotation(GenericAnnotationType.ALLOWEDCHARS,
			GenericAnnotationType.ALLOWEDCHARS_VALUE, "[a-z ]")));
		session.startProcess(locEN, locFR); // Make sure we re-initialize
		session.processTextUnit(tu);
		List<Issue> issues = session.getIssues();
		assertEquals(1, issues.size());
		assertEquals(IssueType.ALLOWED_CHARACTERS, issues.get(0).getIssueType());
	}

	static private void assertAllGood(List<BlackTerm> res) {
		assertEquals(9, res.size());
		assertEquals("BlackTerm1", res.get(0).text);
		assertEquals("BLACKTERM1", res.get(0).searchTerm);
		assertEquals("Suggestion1", res.get(0).suggestion);
		assertEquals("", res.get(0).comment);
		assertFalse(res.get(0).doCaseSensitiveMatch);
		assertEquals("BlackTerm2", res.get(1).text);
		assertEquals("BLACKTERM2", res.get(1).searchTerm);
		assertEquals("", res.get(1).suggestion);
		assertEquals("", res.get(1).comment);
		assertFalse(res.get(1).doCaseSensitiveMatch);
		assertEquals("BlackTerm3", res.get(2).text);
		assertEquals("BLACKTERM3", res.get(2).searchTerm);
		assertEquals("", res.get(2).suggestion);
		assertEquals("", res.get(2).comment);
		assertFalse(res.get(2).doCaseSensitiveMatch);
		assertEquals("BlackTerm4", res.get(3).text);
		assertEquals("BLACKTERM4", res.get(3).searchTerm);
		assertEquals("Suggestion4", res.get(3).suggestion);
		assertEquals("", res.get(3).comment);
		assertFalse(res.get(3).doCaseSensitiveMatch);
		assertEquals("BlackTerm5", res.get(4).text);
		assertEquals("BlackTerm5", res.get(4).searchTerm);
		assertEquals("BLACKTERM5", res.get(4).suggestion);
		assertEquals("", res.get(4).comment);
		assertTrue(res.get(4).doCaseSensitiveMatch);
		assertEquals("blackterm6", res.get(5).text);
		assertEquals("blackterm6", res.get(5).searchTerm);
		assertEquals("BlackTerm6", res.get(5).suggestion);
		assertEquals("", res.get(5).comment);
		assertTrue(res.get(5).doCaseSensitiveMatch);
		assertEquals("BLACKTERM7", res.get(6).text);
		assertEquals("BLACKTERM7", res.get(6).searchTerm);
		assertEquals("Blackterm7", res.get(6).suggestion);
		assertEquals("", res.get(6).comment);
		assertTrue(res.get(6).doCaseSensitiveMatch);
		assertEquals("BlackTerm8", res.get(7).text);
		assertEquals("BLACKTERM8", res.get(7).searchTerm);
		assertEquals("Suggestion8", res.get(7).suggestion);
		assertEquals("Wrong terminology", res.get(7).comment);
		assertFalse(res.get(7).doCaseSensitiveMatch);
		assertEquals("BlackTerm9", res.get(8).text);
		assertEquals("BLACKTERM9", res.get(8).searchTerm);
		assertEquals("", res.get(8).suggestion);
		assertEquals("Wrong terminology", res.get(8).comment);
		assertFalse(res.get(8).doCaseSensitiveMatch);
	}

	@Test
	public void testImportBlacklist() {
		File file = location.in("/black_tsv_simple.txt").asFile();
		BlacklistTB tb = new BlacklistTB();
		tb.guessAndImport(file);
		assertAllGood(tb.getBlacklistStrings());
	}

	@Test
	public void testImportBlacklistForLocale_EN() {
		File file = location.in("/black_tsv_simple.txt").asFile();
		BlacklistTB tb = new BlacklistTB(locEN);
		tb.guessAndImport(file);
		assertAllGood(tb.getBlacklistStrings());
	}

	@Test
	public void testLoadBlacklist() {
		final String content = ""
				+ "BlackTerm1\tSuggestion1\n"
				+ "BlackTerm2\t\n"
				+ "BlackTerm3\t\n"
				+ "BlackTerm4\tSuggestion4\n"
				+ "BlackTerm5\tBLACKTERM5\n"
				+ "blackterm6\tBlackTerm6\n"
				+ "BLACKTERM7\tBlackterm7\n"
				+ "BlackTerm8\tSuggestion8\tWrong terminology\n"
				+ "BlackTerm9\t\tWrong terminology";

		BlacklistTB tb = new BlacklistTB();
		tb.loadBlacklistStream(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
		assertAllGood(tb.getBlacklistStrings());
	}

	@Test
	public void testLoadBlacklistForLocale_EN() {
		final String content = ""
				+ "Item1\titem1\n"
				+ "Item2\tıtem2\n"
				+ "İtem3\titem3\n"
				+ "ıtem4\tItem4\n"
				+ "item5\tItem5\n"
				+ "item6\tİtem6\n"
				+ "ıtem7\tİtem7\n"
				+ "İTEM8\tİtem8";

		BlacklistTB tb = new BlacklistTB(locEN);
		tb.loadBlacklistStream(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
		List<BlackTerm> res = tb.getBlacklistStrings();

		assertEquals(8, res.size());
		assertEquals("Item1", res.get(0).text);
		assertEquals("Item1", res.get(0).searchTerm);
		assertEquals("item1", res.get(0).suggestion);
		assertTrue(res.get(0).doCaseSensitiveMatch);
		assertEquals("Item2", res.get(1).text);
		assertEquals("Item2", res.get(1).searchTerm);
		assertEquals("ıtem2", res.get(1).suggestion);
		assertTrue(res.get(1).doCaseSensitiveMatch);
		assertEquals("İtem3", res.get(2).text);
		assertEquals("İTEM3", res.get(2).searchTerm);
		assertEquals("item3", res.get(2).suggestion);
		assertFalse(res.get(2).doCaseSensitiveMatch);
		assertEquals("ıtem4", res.get(3).text);
		assertEquals("ıtem4", res.get(3).searchTerm);
		assertEquals("Item4", res.get(3).suggestion);
		assertTrue(res.get(3).doCaseSensitiveMatch);
		assertEquals("item5", res.get(4).text);
		assertEquals("item5", res.get(4).searchTerm);
		assertEquals("Item5", res.get(4).suggestion);
		assertTrue(res.get(4).doCaseSensitiveMatch);
		assertEquals("item6", res.get(5).text);
		assertEquals("ITEM6", res.get(5).searchTerm);
		assertEquals("İtem6", res.get(5).suggestion);
		assertFalse(res.get(5).doCaseSensitiveMatch);
		assertEquals("ıtem7", res.get(6).text);
		assertEquals("ITEM7", res.get(6).searchTerm);
		assertEquals("İtem7", res.get(6).suggestion);
		assertFalse(res.get(6).doCaseSensitiveMatch);
		assertEquals("İTEM8", res.get(7).text);
		assertEquals("İTEM8", res.get(7).searchTerm);
		assertEquals("İtem8", res.get(7).suggestion);
		assertTrue(res.get(7).doCaseSensitiveMatch);
	}

	@Test
	public void testLoadBlacklistForLocale_TR() {
		final String content = ""
				+ "Item1\titem1\n"
				+ "Item2\tıtem2\n"
				+ "İtem3\titem3\n"
				+ "ıtem4\tItem4\n"
				+ "item5\tItem5\n"
				+ "item6\tİtem6\n"
				+ "ıtem7\tİtem7\n"
				+ "İTEM8\tİtem8";

		BlacklistTB tb = new BlacklistTB(locTR);
		tb.loadBlacklistStream(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
		List<BlackTerm> res = tb.getBlacklistStrings();

		assertEquals(8, res.size());
		assertEquals("Item1", res.get(0).text);
		assertEquals("ITEM1", res.get(0).searchTerm);
		assertEquals("item1", res.get(0).suggestion);
		assertFalse(res.get(0).doCaseSensitiveMatch);
		assertEquals("Item2", res.get(1).text);
		assertEquals("Item2", res.get(1).searchTerm);
		assertEquals("ıtem2", res.get(1).suggestion);
		assertTrue(res.get(1).doCaseSensitiveMatch);
		assertEquals("İtem3", res.get(2).text);
		assertEquals("İtem3", res.get(2).searchTerm);
		assertEquals("item3", res.get(2).suggestion);
		assertTrue(res.get(2).doCaseSensitiveMatch);
		assertEquals("ıtem4", res.get(3).text);
		assertEquals("ıtem4", res.get(3).searchTerm);
		assertEquals("Item4", res.get(3).suggestion);
		assertTrue(res.get(3).doCaseSensitiveMatch);
		assertEquals("item5", res.get(4).text);
		assertEquals("İTEM5", res.get(4).searchTerm);
		assertEquals("Item5", res.get(4).suggestion);
		assertFalse(res.get(4).doCaseSensitiveMatch);
		assertEquals("item6", res.get(5).text);
		assertEquals("item6", res.get(5).searchTerm);
		assertEquals("İtem6", res.get(5).suggestion);
		assertTrue(res.get(5).doCaseSensitiveMatch);
		assertEquals("ıtem7", res.get(6).text);
		assertEquals("ITEM7", res.get(6).searchTerm);
		assertEquals("İtem7", res.get(6).suggestion);
		assertFalse(res.get(6).doCaseSensitiveMatch);
		assertEquals("İTEM8", res.get(7).text);
		assertEquals("İTEM8", res.get(7).searchTerm);
		assertEquals("İtem8", res.get(7).suggestion);
		assertTrue(res.get(7).doCaseSensitiveMatch);
	}

	@Test
	public void testBlacklistChecker() {
		// Setup
		String inPath = location.in("/black_tsv_simple.txt").toString();
		Parameters params = session.getParameters();
		params.setCheckBlacklist(true);
		params.setblacklistPath(inPath);

		verifyBlacklistChecker_Target();
	}

	@Test
	public void testBlacklistCheckerWithStream() {
		// Setup
		Parameters params = session.getParameters();
		params.setCheckBlacklist(true);
		File file = location.in("/black_tsv_simple.txt").asFile();
		try {
			params.setBlacklistStream(new FileInputStream(file));
		} catch (IOException e) {
			fail("IOException occured.");
		}

		verifyBlacklistChecker_Target();
	}

	private void verifyBlacklistChecker_Target() {
		// Configure data
		ITextUnit tu = new TextUnit("id", "Srcwrd srcwrd srcwrd srcwrd");
		tu.setTarget(locFR, new TextContainer("BlackTerm1 followed by BlackTerm1"));

		// Reinitialize
		session.startProcess(locEN, locFR);
		session.processTextUnit(tu);

		// Get results
		List<Issue> issues = session.getIssues();
		assertEquals(2, issues.size());
	}

	@Test
	public void testBlacklistChecker_CaseSensitive() {
		// Setup
		String inPath = location.in("/black_tsv_simple.txt").toString();
		Parameters params = session.getParameters();
		params.setCheckBlacklist(true);
		params.setblacklistPath(inPath);

		verifyBlacklistChecker_Target_CaseSensitive();
	}

	@Test
	public void testBlacklistCheckerWithStream_CaseSensitive() {
		// Setup
		Parameters params = session.getParameters();
		params.setCheckBlacklist(true);
		File file = location.in("/black_tsv_simple.txt").asFile();
		try {
			params.setBlacklistStream(new FileInputStream(file));
		} catch (IOException e) {
			fail("IOException occured.");
		}

		verifyBlacklistChecker_Target_CaseSensitive();
	}

	private void verifyBlacklistChecker_Target_CaseSensitive() {
		// Configure data
		ITextUnit tu = new TextUnit("id", "Srcwrd srcwrd srcwrd srcwrd srcwrd srcwrd.");
		tu.setTarget(locFR, new TextContainer(
				"BlackTerm1 BLACKTerm1 BlackTerm5 BLACKTerm5 blackterm6 blackterm7."));

		// Reinitialize
		session.startProcess(locEN, locFR);
		session.processTextUnit(tu);

		// Get results
		List<Issue> issues = session.getIssues();
		assertEquals(4, issues.size());
	}

	@Test
	public void testBlacklistChecker_JA() {
		// Setup
		String inPath = location.in("/black_tsv_simple_JA.txt").toString();
		Parameters params = session.getParameters();
		params.setCheckBlacklist(true);
		params.setAllowBlacklistSub(true);
		params.setblacklistPath(inPath);

		verifyBlacklistChecker_Target_JA();
	}

	@Test
	public void testBlacklistCheckerWithTermList_JA() {
		// Setup
		Parameters params = session.getParameters();
		params.setCheckBlacklist(true);
		params.setAllowBlacklistSub(true);
		File file = location.in("/black_tsv_simple_JA.txt").asFile();
		try {
			params.setBlacklistStream(new FileInputStream(file));
		} catch (IOException e) {
			fail("IOException occured.");
		}
		verifyBlacklistChecker_Target_JA();
	}

	private void verifyBlacklistChecker_Target_JA() {
		// Configure data
		ITextUnit tu = new TextUnit("id", "Srcwrd srcwrd srcwrd srcwrd");
		tu.setTarget(locJA, new TextContainer("私はあなただけを愛しています。"));

		// Reinitialize
		session.startProcess(locEN, locJA);
		session.processTextUnit(tu);

		// Get results
		List<Issue> issues = session.getIssues();
		assertEquals(1, issues.size());
	}

	@Test
	public void testBlacklistChecker_Src() {
		// Setup
		String inPath = location.in("/black_tsv_simple_JA.txt").toString();
		Parameters params = session.getParameters();
		params.setCheckBlacklist(true);
		params.setAllowBlacklistSub(true);
		params.setBlacklistSrc(true);
		params.setblacklistPath(inPath);

		verifyBlacklistChecker_Src();
	}

	@Test
	public void testBlacklistCheckerWithTermList_Src() {
		// Setup
		Parameters params = session.getParameters();
		params.setCheckBlacklist(true);
		params.setAllowBlacklistSub(true);
		params.setBlacklistSrc(true);
		File file = location.in("/black_tsv_simple_JA.txt").asFile();
		try {
			params.setBlacklistStream(new FileInputStream(file));
		} catch (IOException e) {
			fail("IOException occured.");
		}
		verifyBlacklistChecker_Src();
	}

	private void verifyBlacklistChecker_Src(){
		// Configure data
		ITextUnit tu = new TextUnit("id", "私はあなただけを愛しています。");
		tu.setTarget(locJA, new TextContainer("I love you only."));

		// Reinitialize
		session.startProcess(locEN, locJA);
		session.processTextUnit(tu);

		// Get results
		List<Issue> issues = session.getIssues();
		assertEquals(1, issues.size());
	}

	@Test
	public void testSimpleWithCode() {
		String inPath = location.in("/black_tsv_simple.txt").toString();
		Parameters params = session.getParameters();
		params.setCheckBlacklist(true);
		params.setblacklistPath(inPath);

		// Source text <b>text</b> text <span><b>text</b> text</span>.
		TextFragment srcTf1 = new TextFragment();
		srcTf1.append("Source text ");
		srcTf1.append(TagType.OPENING, "bold","<b>");
		srcTf1.append("text");
		srcTf1.append(TagType.CLOSING, "bold","</b>");
		srcTf1.append(" text ");
		srcTf1.append(TagType.OPENING, "span","<span>");
		srcTf1.append(TagType.OPENING, "bold","<b>");
		srcTf1.append("text");
		srcTf1.append(TagType.CLOSING, "bold","</b>");
		srcTf1.append(" text");
		srcTf1.append(TagType.CLOSING, "span","</span>");
		srcTf1.append(".");

		// Target text <b>BlackTerm1</b> text <span><b>BlackTerm1</b> text</span>.
		// Target text ##BlackTerm1## text ####BlackTerm1## text##.
		TextFragment trgTf1 = new TextFragment();
		trgTf1.append("Target text ");
		trgTf1.append(TagType.OPENING, "bold","<b>");
		trgTf1.append("BlackTerm1");
		trgTf1.append(TagType.CLOSING, "bold","</b>");
		trgTf1.append(" text ");
		trgTf1.append(TagType.OPENING, "span", "<span>");
		trgTf1.append(TagType.OPENING, "bold","<b>");
		trgTf1.append("BlackTerm1");
		trgTf1.append(TagType.CLOSING, "bold","</b>");
		trgTf1.append(" text");
		trgTf1.append(TagType.CLOSING, "span", "</span>");
		trgTf1.append(".");

		ITextUnit tu = new TextUnit("id");
		tu.setSourceContent(srcTf1);
		tu.setTargetContent(locFR, trgTf1);

		session.startProcess(locEN, locFR);
		session.processTextUnit(tu);

		List<Issue> issues = session.getIssues();
		assertEquals(2, issues.size());
		assertEquals(15, issues.get(0).getTargetStart());
		assertEquals(25, issues.get(0).getTargetEnd());
		assertEquals(44, issues.get(1).getTargetStart());
		assertEquals(54, issues.get(1).getTargetEnd());
	}

	@Test
	public void testBlacklistWithCode() {
		String inPath = location.in("/black_tsv_simple.txt").toString();
		Parameters params = session.getParameters();
		params.setCheckBlacklist(true);
		params.setblacklistPath(inPath);

		verifyBlacklistWithCode();
	}

	@Test
	public void testBlacklistWithCodeWithTermList() {
		Parameters params = session.getParameters();
		params.setCheckBlacklist(true);
		File file = location.in("/black_tsv_simple.txt").asFile();
		try {
			params.setBlacklistStream(new FileInputStream(file));
		} catch (IOException e) {
			fail("IOException occured.");
		}
		verifyBlacklistWithCode();
	}

	private void verifyBlacklistWithCode() {

		TextFragment srcTf1 = new TextFragment();
		srcTf1.append("I like source on my pasta.");

		TextFragment trgTf1 = new TextFragment();
		trgTf1.append("Texte de l'attribute avec BlackTerm4.");

		TextFragment srcTf2 = new TextFragment();
		srcTf2.append("Source ");
		srcTf2.append(TagType.OPENING, "span", "<span>");
		srcTf2.append("sentence");
		srcTf2.append(TagType.CLOSING, "span", "</span>");
		srcTf2.append(" with words.");

		TextFragment trgTf2 = new TextFragment();
		trgTf2.append("Target ");
		trgTf2.append(TagType.OPENING, "span", "<span>");
		trgTf2.append("BlackTerm1");
		trgTf2.append(TagType.CLOSING, "span", "</span>");
		trgTf2.append(" also with BlackTerm1.");

		ITextUnit tu = new TextUnit("id");
		tu.getSource().append(new Segment("seg1", srcTf1));
		tu.createTarget(locFR, true, IResource.CREATE_EMPTY).append(new Segment("seg1", trgTf1));
		tu.getSource().append(new Segment("seg2", srcTf2));
		tu.getTarget(locFR).append(new Segment("seg2", trgTf2));

		session.startProcess(locEN, locFR);
		session.processTextUnit(tu);

		List<Issue> issues = session.getIssues();
		assertEquals(3, issues.size());
		assertEquals("Texte de l'attribute avec BlackTerm4.", issues.get(0).getTarget());
		assertEquals("Target <span>BlackTerm1</span> also with BlackTerm1.", issues.get(1).getTarget());
		assertEquals("Target <span>BlackTerm1</span> also with BlackTerm1.", issues.get(2).getTarget());
	}

	@Test
	public void testCheckBoundaries() {
		String inPath = location.in("/black_tsv_simple.txt").toString();
		Parameters params = session.getParameters();
		params.setCheckBlacklist(true);
		params.setblacklistPath(inPath);

		TextFragment srcTf1 = new TextFragment("Source");
		TextFragment srcTf2 = new TextFragment("This source rules.");
		TextFragment srcTf3 = new TextFragment("Source magic.");
		TextFragment srcTf4 = new TextFragment("My source");
		TextFragment srcTf5 = new TextFragment("Fancy source beans");
		TextFragment srcTf6 = new TextFragment("Special source cocktail");
		TextFragment srcTf7 = new TextFragment("Fresh java.");
		TextFragment srcTf8 = new TextFragment("Coding boogie.");
		TextFragment srcTf9 = new TextFragment("Single malt");
		TextFragment srcTf10 = new TextFragment("GreenTea Latte");

		TextFragment trgTf1 = new TextFragment("BlackTerm1");
		TextFragment trgTf2 = new TextFragment("This BlackTerm1 rules.");
		TextFragment trgTf3 = new TextFragment("BlackTerm1 magic.");
		TextFragment trgTf4 = new TextFragment("My BlackTerm1");
		TextFragment trgTf5 = new TextFragment("Caught RedBlackTerm1 here.");
		TextFragment trgTf6 = new TextFragment("Project BlackTerm1B.");
		TextFragment trgTf7 = new TextFragment("BlackTerm1B here.");
		TextFragment trgTf8 = new TextFragment("BBlackTerm1 boogie.");
		TextFragment trgTf9 = new TextFragment("Single BlackTerm1B");
		TextFragment trgTf10 = new TextFragment("Mocha BBlackTerm1");

		ITextUnit tu = new TextUnit("id");
		tu.getSource().append(new Segment("seg1", srcTf1));
		tu.createTarget(locFR, true, IResource.CREATE_EMPTY).append(new Segment("seg1", trgTf1));
		tu.getSource().append(new Segment("seg2", srcTf2));
		tu.getTarget(locFR).append(new Segment("seg2", trgTf2));
		tu.getSource().append(new Segment("seg3", srcTf3));
		tu.getTarget(locFR).append(new Segment("seg3", trgTf3));
		tu.getSource().append(new Segment("seg4", srcTf4));
		tu.getTarget(locFR).append(new Segment("seg4", trgTf4));
		tu.getSource().append(new Segment("seg5", srcTf5));
		tu.getTarget(locFR).append(new Segment("seg5", trgTf5));
		tu.getSource().append(new Segment("seg6", srcTf6));
		tu.getTarget(locFR).append(new Segment("seg6", trgTf6));
		tu.getSource().append(new Segment("seg7", srcTf7));
		tu.getTarget(locFR).append(new Segment("seg7", trgTf7));
		tu.getSource().append(new Segment("seg8", srcTf8));
		tu.getTarget(locFR).append(new Segment("seg8", trgTf8));
		tu.getSource().append(new Segment("seg9", srcTf9));
		tu.getTarget(locFR).append(new Segment("seg9", trgTf9));
		tu.getSource().append(new Segment("seg10", srcTf10));
		tu.getTarget(locFR).append(new Segment("seg10", trgTf10));

		session.startProcess(locEN, locFR);
		session.processTextUnit(tu);

		List<Issue> issues = session.getIssues();

		assertEquals(4, issues.size());
		assertEquals("BlackTerm1", issues.get(0).getTarget());
		assertEquals("This BlackTerm1 rules.", issues.get(1).getTarget());
		assertEquals("BlackTerm1 magic.", issues.get(2).getTarget());
		assertEquals("My BlackTerm1", issues.get(3).getTarget());
	}

	@Test
	public void testCheckBlacklist_fail() {
		Parameters params = session.getParameters();
		params.setCheckBlacklist(true);

		TextFragment srcTf1 = new TextFragment("Source");
		TextFragment srcTf2 = new TextFragment("This source rules.");

		TextFragment trgTf1 = new TextFragment("BlackTerm1");
		TextFragment trgTf2 = new TextFragment("This BlackTerm1 rules.");

		ITextUnit tu = new TextUnit("id");
		tu.getSource().append(new Segment("seg1", srcTf1));
		tu.createTarget(locFR, true, IResource.CREATE_EMPTY).append(new Segment("seg1", trgTf1));
		tu.getSource().append(new Segment("seg2", srcTf2));
		tu.getTarget(locFR).append(new Segment("seg2", trgTf2));

		try {
			session.startProcess(locEN, locFR);
		} catch (OkapiIOException e) {
			// OkapiIOException: Error opening the URI. (No such file or directory)
			// exception expected as neither blacklistPath nor BlacklistTerms are set in session params
		}
	}

	@Test
	public void testLineFeedErrorMessage() {
		Parameters params = session.getParameters();
		params.setPatterns(Collections.singletonList(
				new PatternItem("\\x0a", "<same>", true,
						Issue.DISPSEVERITY_HIGH, true, "Missing line break")));
		ITextUnit tu = new TextUnit("id");
		tu.getSource().append(new Segment("seg1", new TextFragment("Contains a\nline break")));
		tu.createTarget(locFR, true, IResource.CREATE_EMPTY).append(
				new Segment("seg1", new TextFragment("Doesn't contain a line break")));
		session.startProcess(locEN, locFR);
		session.processTextUnit(tu);

		List<Issue> issues = session.getIssues();
		assertEquals(1, issues.size());
		assertEquals("The source part \"\\n\" is not in the target (from rule: Missing line break).",
				issues.get(0).getMessage());
	}

	@Test
	public void testCRLFErrorMessage() {
		Parameters params = session.getParameters();
		params.setPatterns(Collections.singletonList(
				new PatternItem("\\x0d\\x0a", "<same>", true,
						Issue.DISPSEVERITY_HIGH, true, "Missing CRLF")));
		ITextUnit tu = new TextUnit("id");
		tu.getSource().append(new Segment("seg1", new TextFragment("Contains a\r\nline break")));
		tu.createTarget(locFR, true, IResource.CREATE_EMPTY).append(
				new Segment("seg1", new TextFragment("Doesn't contain a line break")));
		session.startProcess(locEN, locFR);
		session.processTextUnit(tu);

		List<Issue> issues = session.getIssues();
		assertEquals(1, issues.size());
		assertEquals("The source part \"\\r\\n\" is not in the target (from rule: Missing CRLF).",
				issues.get(0).getMessage());
	}

	@Test
	public void testRULES_PRIORITYLOW () {
		ITextUnit tu = new TextUnit("id", "Srctext: (brackets)");
		tu.setTarget(locFR, new TextContainer("Trgtext: (brackets"));

		session.processTextUnit(tu);
		List<Issue> issues = session.getIssues();
		assertEquals(1, issues.size());
		assertEquals(IssueType.UNEXPECTED_PATTERN, issues.get(0).getIssueType());
		assertEquals(Issue.DISPSEVERITY_LOW, issues.get(0).getDisplaySeverity());
	}

	@Test
	public void testRULES_PRIORITYMEDIUM () {
		ITextUnit tu = new TextUnit("id", "Srctext: foo@bar.com");
		tu.setTarget(locFR, new TextContainer("Trgtext: foo@barbar.fr"));

		session.processTextUnit(tu);
		List<Issue> issues = session.getIssues();
		assertEquals(1, issues.size());
		assertEquals(IssueType.UNEXPECTED_PATTERN, issues.get(0).getIssueType());
		assertEquals(Issue.DISPSEVERITY_MEDIUM, issues.get(0).getDisplaySeverity());
	}

	@Test
	public void testRULES_PRIORITYHIGH () {
		ITextUnit tu = new TextUnit("id", "Srctext: %s");
		tu.setTarget(locFR, new TextContainer("Trgtext: %d"));

		session.processTextUnit(tu);
		List<Issue> issues = session.getIssues();
		assertEquals(1, issues.size());
		assertEquals(IssueType.UNEXPECTED_PATTERN, issues.get(0).getIssueType());
		assertEquals(Issue.DISPSEVERITY_HIGH, issues.get(0).getDisplaySeverity());
	}

	/*
	 * Before the fix for issue #594 the word splitting used the JDK regex.
	 * And that did not recognize modifiers (in this case we needed Mc)
	 *
	 * Input:
	 *   U+0915  DEVANAGARI LETTER KA :: Lo
	 *   U+0949  DEVANAGARI VOWEL SIGN CANDRA O :: Mc
	 *   U+0932  DEVANAGARI LETTER LA :: Lo
	 *   U+0020  SPACE
	 *   U+0932  DEVANAGARI LETTER LA :: Lo
	 *   U+0949  DEVANAGARI VOWEL SIGN CANDRA O :: Mc
	 *   U+0917  DEVANAGARI LETTER GA :: Lo
	 *
	 * Wrong output (Double word: "ल ल" found in the target.):
	 *   U+0932  DEVANAGARI LETTER LA
	 *   U+0020  SPACE
	 *   U+0932  DEVANAGARI LETTER LA
	 *
	 * Happened because markers (VOWEL SIGN CANDRA O) break the work (when using regexp)
	 * So "KaOLa LaOGa" would break into ["Ka", "O", "La", " ", "La", "O", "Ga"]
	 * instead of  ["KaOLa", " ", "LaOGa"]
	 *
	 * Using a BreakIterator might be a bit slower, but it is correct in more cases
	 * (can even split Thai, which does not use spaces between words)
	 */

	@Test
	public void testDoubleWordsIssue594() {
		String[] testData = {
				"hi", "\u0915\u0949\u0932 \u0932\u0949\u0917",
				"bn", "\u099a\u09be\u09b2\u09be\u09ac\u09c7\u09a8 \u09a8\u09be!",
				"gu", "\u0ab2\u0abf\u0a82\u0a95 \u0a95\u0ac9\u0aaa\u0abf",
				"kn", "\u0c92\u0cb3\u0cac\u0cb0\u0cc1\u0cb5 \u0cb5\u0cc0\u0ca1\u0cbf\u0caf\u0cca \u0c95\u0cb0\u0cc6",
				"mr", "\u0938\u093e\u0907\u0928 \u0907\u0928 \u0915\u0930\u093e",
				"ne", "\u0921\u094d\u0930\u093e\u0907\u092d\u092c\u093e\u091f \u091f\u093f\u092e",
				"pa", "\u0a15\u0a32\u0a3f\u0a71\u0a15 \u0a15\u0a40\u0a24\u0a40",
				"si", "\u0d94\u0db6 \u0d94\u0db6\u0dda"
		};
		for (int i = 0; i < testData.length; i += 2) {
			final LocaleId trgLocale = LocaleId.fromBCP47(testData[i]);
			final String trgText = testData[i + 1];

			ITextUnit tu = new TextUnit("id" + i, "Indic scripts");
			tu.setTarget(trgLocale, new TextContainer(trgText));

			session.startProcess(locEN, trgLocale); // the default session is French
			session.processTextUnit(tu);

			final List<Issue> issues = session.getIssues();
			assertEquals(0, issues.size());
		}
	}

	@Test
	public void testDoubleWordForReal() {
		final LocaleId trgLocale = LocaleId.fromBCP47("hi");

		final ITextUnit tu = new TextUnit("id", "Double word");
		tu.setTarget(trgLocale, new TextContainer("कॉल लॉग  \t\n लॉग")); // The word is really doubled

		session.startProcess(locEN, trgLocale); // the default session is French
		session.processTextUnit(tu);

		final List<Issue> issues = session.getIssues();
		assertEquals(1, issues.size());
		assertEquals(IssueType.SUSPECT_PATTERN, issues.get(0).getIssueType());
		assertEquals("Double word: \"लॉग\" found in the target.", issues.get(0).getMessage());
	}

	@Test
	public void testDoubleWordThai() { // Interesting because Thai does not use spaces between words
		final LocaleId trgLocale = LocaleId.fromBCP47("th");

		final String doubleWord = "\u0e1b\u0e23\u0e30\u0e15\u0e394\u0e2b\u0e25\u0e31\u0e07";
		final String targetString = "\u0e01\u0e33\u0e25\u0e31\u0e07" + doubleWord + doubleWord + "\u0e1e\u0e23\u0e30";
		final ITextUnit tu = new TextUnit("id", "Double word");
		tu.setTarget(trgLocale, new TextContainer(targetString));

		session.startProcess(locEN, trgLocale); // the default session is French
		session.processTextUnit(tu);

		final List<Issue> issues = session.getIssues();
		assertEquals(1, issues.size());
		assertEquals(IssueType.SUSPECT_PATTERN, issues.get(0).getIssueType());
		assertEquals("Double word: \"" + doubleWord + "\" found in the target.",
				issues.get(0).getMessage());
	}

	@Test
	public void testDoubleWordJapanese() { // Interesting because Japanese does not use spaces between words
		final LocaleId trgLocale = LocaleId.fromBCP47("ja");

		final String doubleWord = "\u516b\u738b\u5b50";
		final String targetString = "\u6771\u4eac\u90fd" + doubleWord + doubleWord + "\u5e02\u5317\u91ce\u53f0";
		final ITextUnit tu = new TextUnit("id", "Double word");
		tu.setTarget(trgLocale, new TextContainer(targetString));

		session.startProcess(locEN, trgLocale); // the default session is French
		session.processTextUnit(tu);

		final List<Issue> issues = session.getIssues();
		assertEquals(1, issues.size());
		assertEquals(IssueType.SUSPECT_PATTERN, issues.get(0).getIssueType());
		assertEquals("Double word: \"" + doubleWord + "\" found in the target.",
				issues.get(0).getMessage());
	}

	@Test
	public void testDoubleWordExceptions() {
		final ITextUnit tu = new TextUnit("id", "If you remember");
		tu.setTarget(locFR, new TextContainer("si vous vous souvenez"));
		session.processTextUnit(tu);
		final List<Issue> issues = session.getIssues();
		// Because "vous" is in the double word allowed list (for French)
		assertEquals(0, issues.size());
	}
}
