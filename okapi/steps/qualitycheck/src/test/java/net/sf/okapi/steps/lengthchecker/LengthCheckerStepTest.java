package net.sf.okapi.steps.lengthchecker;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.annotation.GenericAnnotation;
import net.sf.okapi.common.annotation.GenericAnnotationType;
import net.sf.okapi.common.annotation.GenericAnnotations;
import net.sf.okapi.common.annotation.IssueType;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextUnit;
import net.sf.okapi.lib.verification.Issue;

@RunWith(JUnit4.class)
public class LengthCheckerStepTest {
	private LengthCheckerStep lengthCheckerStep;

	@Before
	public void setUp() throws Exception {
		lengthCheckerStep = new LengthCheckerStep();
		lengthCheckerStep.setSourceLocale(LocaleId.ENGLISH);
		lengthCheckerStep.setTargetLocale(LocaleId.FRENCH);
		lengthCheckerStep.setParameters(new Parameters());
		lengthCheckerStep.handleStartBatch(Event.START_BATCH_EVENT);
		StartDocument sd = new StartDocument("id");
		sd.setMultilingual(true);
		sd.setName("default SD");
		lengthCheckerStep.handleStartDocument(new Event(EventType.START_DOCUMENT, sd));		
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testNoIssues () {
		ITextUnit tu = new TextUnit("id", "  Text {with} (123). ");
		tu.setTarget(LocaleId.FRENCH, new TextContainer("  Texte {avec} (123). "));

		lengthCheckerStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu));
		List<Issue> issues = lengthCheckerStep.getIssues();
		assertEquals(0, issues.size());
	}
	
	@Test
	public void testMaxLength () {
		((Parameters)lengthCheckerStep.getParameters()).setMaxCharLengthBreak(9);
		((Parameters)lengthCheckerStep.getParameters()).setMaxCharLengthAbove(149);
		((Parameters)lengthCheckerStep.getParameters()).setMaxCharLengthBelow(200);

		ITextUnit tu = new TextUnit("id", "abcdefghij"); // 10 chars -> use above
		tu.setTarget(LocaleId.FRENCH, new TextContainer("123456789012345")); // 15 chars
		lengthCheckerStep.handleStartBatch(Event.START_BATCH_EVENT);
		lengthCheckerStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu));
		List<Issue> issues = lengthCheckerStep.getIssues();
		assertEquals(1, issues.size());
		assertEquals(IssueType.TARGET_LENGTH, issues.get(0).getIssueType());

		tu = new TextUnit("id", "abcdefghi"); // 9 chars -> use below
		tu.setTarget(LocaleId.FRENCH, new TextContainer("123456789012345678")); // 18 chars (==200% of src)
		lengthCheckerStep.getIssues().clear();
		lengthCheckerStep.handleStartBatch(Event.START_BATCH_EVENT);
		lengthCheckerStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu));
		issues = lengthCheckerStep.getIssues();
		assertEquals(0, issues.size());

		tu.setTarget(LocaleId.FRENCH, new TextContainer("1234567890123456789")); // 19 chars (>200% of src)
		lengthCheckerStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu));
		issues = lengthCheckerStep.getIssues();
		assertEquals(1, issues.size());
		assertEquals(IssueType.TARGET_LENGTH, issues.get(0).getIssueType());
	}

	@Test
	public void testMinLength () {
		((Parameters)lengthCheckerStep.getParameters()).setMinCharLengthBreak(9);
		((Parameters)lengthCheckerStep.getParameters()).setMinCharLengthAbove(100);
		((Parameters)lengthCheckerStep.getParameters()).setMinCharLengthBelow(50);

		ITextUnit tu = new TextUnit("id", "abcdefghij"); // 10 chars -> use above
		tu.setTarget(LocaleId.FRENCH, new TextContainer("123456789")); // 10 chars (<100% of src)
		lengthCheckerStep.handleStartBatch(Event.START_BATCH_EVENT);
		lengthCheckerStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu));
		List<Issue> issues = lengthCheckerStep.getIssues();
		assertEquals(1, issues.size());
		assertEquals(IssueType.TARGET_LENGTH, issues.get(0).getIssueType());

		tu = new TextUnit("id", "abcdefghi"); // 9 chars -> use below
		tu.setTarget(LocaleId.FRENCH, new TextContainer("12345")); // 5 chars (==50% of src)
		lengthCheckerStep.getIssues().clear();
		lengthCheckerStep.handleStartBatch(Event.START_BATCH_EVENT);
		lengthCheckerStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu));
		issues = lengthCheckerStep.getIssues();
		assertEquals(0, issues.size());

		tu.setTarget(LocaleId.FRENCH, new TextContainer("123")); // 4 chars (<50% of src)
		lengthCheckerStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu));
		issues = lengthCheckerStep.getIssues();
		assertEquals(1, issues.size());
		assertEquals(IssueType.TARGET_LENGTH, issues.get(0).getIssueType());
	}

	@Test
	public void testStorageSizeInvalidChar () {
		ITextUnit tu = new TextUnit("id", "abc");
		tu.setTarget(LocaleId.FRENCH, new TextContainer("abcXYZ"));
		tu.getTarget(LocaleId.FRENCH).setAnnotation(new GenericAnnotations(new GenericAnnotation(GenericAnnotationType.STORAGESIZE,
			GenericAnnotationType.STORAGESIZE_SIZE, 4,
			GenericAnnotationType.STORAGESIZE_LINEBREAK, "lf",
			GenericAnnotationType.STORAGESIZE_ENCODING, "iso-8859-1"))); // cannot handle Japanese \u3027
		lengthCheckerStep.handleStartBatch(Event.START_BATCH_EVENT);
		lengthCheckerStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu));
		List<Issue> issues = lengthCheckerStep.getIssues();
		assertEquals(1, issues.size());
		assertEquals(IssueType.TARGET_LENGTH, issues.get(0).getIssueType());
	}

	@Test
	public void testStorageSizeUTF8 () {
		ITextUnit tu = new TextUnit("id", "1234567890\n"); // UTF-8: 11 bytes + 1 for additional CR = 12
		tu.setTarget(LocaleId.FRENCH, new TextContainer("+1234567890\n")); // UTF-8: 12 bytes + 1 for additional CR = 13
		tu.getTarget(LocaleId.FRENCH).setAnnotation(new GenericAnnotations(new GenericAnnotation(GenericAnnotationType.STORAGESIZE,
			GenericAnnotationType.STORAGESIZE_SIZE, 12,
			GenericAnnotationType.STORAGESIZE_LINEBREAK, "crlf",
			GenericAnnotationType.STORAGESIZE_ENCODING, "UTF-8")));
		lengthCheckerStep.handleStartBatch(Event.START_BATCH_EVENT);
		lengthCheckerStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu));
		List<Issue> issues = lengthCheckerStep.getIssues();
		assertEquals(1, issues.size());
		assertEquals(IssueType.TARGET_LENGTH, issues.get(0).getIssueType());
	}

	@Test
	public void testStorageSizeUTF16 () {
		ITextUnit tu = new TextUnit("id", "1234567890\n"); // UTF-16: 22 bytes + 2 for additional CR = 24
		tu.setTarget(LocaleId.FRENCH, new TextContainer("+1234567890\n")); // UTF-16: 24 bytes + 2 for additional CR = 26
		tu.getTarget(LocaleId.FRENCH).setAnnotation(new GenericAnnotations(new GenericAnnotation(GenericAnnotationType.STORAGESIZE,
			GenericAnnotationType.STORAGESIZE_SIZE, 24,
			GenericAnnotationType.STORAGESIZE_LINEBREAK, "crlf",
			GenericAnnotationType.STORAGESIZE_ENCODING, "UTF-16")));
		lengthCheckerStep.handleStartBatch(Event.START_BATCH_EVENT);
		lengthCheckerStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu));
		List<Issue> issues = lengthCheckerStep.getIssues();
		assertEquals(1, issues.size());
		assertEquals(IssueType.TARGET_LENGTH, issues.get(0).getIssueType());
	}

	@Test
	public void testStorageSizeUTF32 () {
		ITextUnit tu = new TextUnit("id", "1234567890\n"); // UTF-32: 44 bytes + 4 bytes for additional CR = 48
		tu.setTarget(LocaleId.FRENCH, new TextContainer("+1234567890\n")); // UTF-32: 48 bytes + 4 for additional CR = 52
		tu.getTarget(LocaleId.FRENCH).setAnnotation(new GenericAnnotations(new GenericAnnotation(GenericAnnotationType.STORAGESIZE,
			GenericAnnotationType.STORAGESIZE_SIZE, 48,
			GenericAnnotationType.STORAGESIZE_LINEBREAK, "crlf",
			GenericAnnotationType.STORAGESIZE_ENCODING, "UTF-32")));
		lengthCheckerStep.handleStartBatch(Event.START_BATCH_EVENT);
		lengthCheckerStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu));
		List<Issue> issues = lengthCheckerStep.getIssues();
		assertEquals(1, issues.size());
		assertEquals(IssueType.TARGET_LENGTH, issues.get(0).getIssueType());
	}
}
