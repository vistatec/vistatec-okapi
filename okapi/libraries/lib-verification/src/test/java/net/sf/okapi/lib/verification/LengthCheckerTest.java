package net.sf.okapi.lib.verification;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import net.sf.okapi.common.IResource;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.annotation.GenericAnnotation;
import net.sf.okapi.common.annotation.GenericAnnotationType;
import net.sf.okapi.common.annotation.GenericAnnotations;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextUnit;

@RunWith(JUnit4.class)
public class LengthCheckerTest {
	private static final String SOURCE_TEXT = "Hello world.";
	private static final String TARGET_TEXT = "Hello world, but longer.";

	private static final LocaleId SOURCE_LOCALE = LocaleId.ENGLISH;
	private static final LocaleId TARGET_LOCALE = LocaleId.FRENCH;
	private LengthChecker checker;
	private List<Issue> issues;

	@Before
	public void setUp() {
		checker = new LengthChecker();
		issues = new ArrayList<>();
		final Parameters params = new Parameters();
		params.setCheckAbsoluteMaxCharLength(true);
		params.setAbsoluteMaxCharLength(25);
		checker.startProcess(SOURCE_LOCALE, TARGET_LOCALE, params, issues);
	}

	@Test
	public void testBadLength() {
		final TextContainer trgTc = new TextContainer(TARGET_TEXT);

		trgTc.setAnnotation(new GenericAnnotations(
				new GenericAnnotation(GenericAnnotationType.STORAGESIZE,
						GenericAnnotationType.STORAGESIZE_LINEBREAK, "crlf",
						GenericAnnotationType.STORAGESIZE_SIZE, TARGET_TEXT.length() - 5,
						GenericAnnotationType.STORAGESIZE_ENCODING, "utf-8")));

		final TextUnit tu = new TextUnit("tu1", SOURCE_TEXT);
		tu.createTarget(TARGET_LOCALE, true, IResource.COPY_ALL);
		tu.setTarget(TARGET_LOCALE, trgTc);
		checker.processTextUnit(tu);

		assertEquals(1, issues.size());
		final Issue issue = issues.get(0);
		assertEquals(Issue.DISPSEVERITY_HIGH, issue.getDisplaySeverity());
		assertEquals("Number of bytes in the target (using utf-8) is: 24. Number allowed: 19.", issue.getMessage());
		assertSourceAndTargetInIssue(tu, issue);
	}

	@Test
	public void testBadChar() {
		final String targetText = "Encode error: \u6565.";
		final TextContainer trgTc = new TextContainer(targetText);

		trgTc.setAnnotation(new GenericAnnotations(
				new GenericAnnotation(GenericAnnotationType.STORAGESIZE,
						GenericAnnotationType.STORAGESIZE_LINEBREAK, "crlf",
						GenericAnnotationType.STORAGESIZE_SIZE, 35,
						GenericAnnotationType.STORAGESIZE_ENCODING, "iso-8859-1")));

		final TextUnit tu = new TextUnit("tu1", SOURCE_TEXT);
		tu.createTarget(TARGET_LOCALE, true, IResource.COPY_ALL);
		tu.setTarget(TARGET_LOCALE, trgTc);
		checker.processTextUnit(tu);

		assertEquals(1, issues.size());
		final Issue issue = issues.get(0);
		assertEquals(Issue.DISPSEVERITY_HIGH, issue.getDisplaySeverity());
		assertEquals("Cannot encode one or more characters of the target using iso-8859-1.", issue.getMessage());
		assertSourceAndTargetInIssue(tu, issue);
	}

	@Test
	public void testAbsoluteMaxChar() {
		final String targetText = TARGET_TEXT + " And a bit more.";
		final TextContainer trgTc = new TextContainer(targetText);
		final TextUnit tu = new TextUnit("tu1", SOURCE_TEXT);
		tu.createTarget(TARGET_LOCALE, true, IResource.COPY_ALL);
		tu.setTarget(TARGET_LOCALE, trgTc);
		checker.processTextUnit(tu);

		assertEquals(1, issues.size());
		final Issue issue = issues.get(0);
		assertEquals(Issue.DISPSEVERITY_HIGH, issue.getDisplaySeverity());
		assertEquals("The target is longer than 25 (by 15).", issue.getMessage());
		assertSourceAndTargetInIssue(tu, issue);
	}

	@Test
	public void testAbsoluteMinChar() {
		final String targetText = TARGET_TEXT + " And a bit more.";
		final TextContainer trgTc = new TextContainer(targetText);
		final TextUnit tu = new TextUnit("tu1", SOURCE_TEXT);
		tu.createTarget(TARGET_LOCALE, true, IResource.COPY_ALL);
		tu.setTarget(TARGET_LOCALE, trgTc);
		checker.processTextUnit(tu);

		assertEquals(1, issues.size());
		final Issue issue = issues.get(0);
		assertEquals(Issue.DISPSEVERITY_HIGH, issue.getDisplaySeverity());
		assertEquals("The target is longer than 25 (by 15).", issue.getMessage());
		assertSourceAndTargetInIssue(tu, issue);
	}

	private void assertSourceAndTargetInIssue(TextUnit tu, Issue issue) {
		assertEquals(tu.getSource().toString(), issue.getSource());
		assertEquals(tu.getTarget(TARGET_LOCALE).toString(), issue.getTarget());
	}
}
