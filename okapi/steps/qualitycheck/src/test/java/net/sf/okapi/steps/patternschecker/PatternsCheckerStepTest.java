package net.sf.okapi.steps.patternschecker;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.annotation.IssueType;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextUnit;
import net.sf.okapi.lib.verification.Issue;
import net.sf.okapi.lib.verification.PatternItem;

@RunWith(JUnit4.class)
public class PatternsCheckerStepTest {
	private PatternsCheckerStep patternsCheckerStep;

	@Before
	public void setUp() throws Exception {
		patternsCheckerStep = new PatternsCheckerStep();
		patternsCheckerStep.setSourceLocale(LocaleId.ENGLISH);
		patternsCheckerStep.setTargetLocale(LocaleId.FRENCH);
		patternsCheckerStep.setParameters(new Parameters());
		patternsCheckerStep.handleStartBatch(Event.START_BATCH_EVENT);
		StartDocument sd = new StartDocument("id");
		sd.setMultilingual(true);
		sd.setName("default SD");
		patternsCheckerStep.handleStartDocument(new Event(EventType.START_DOCUMENT, sd));		
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testNoIssues () {
		ITextUnit tu = new TextUnit("id", "  Text {with} (123). ");
		tu.setTarget(LocaleId.FRENCH, new TextContainer("  Texte {avec} (123). "));

		patternsCheckerStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu));
		List<Issue> issues = patternsCheckerStep.getIssues();
		assertEquals(0, issues.size());
	}
	
	@Test
	public void testMISSING_PATTERN () {
		ITextUnit tu = new TextUnit("id", "src text !? %s");
		tu.setTarget(LocaleId.FRENCH, new TextContainer("trg text"));
		ArrayList<PatternItem> list = new ArrayList<PatternItem>();
		list.add(new PatternItem("[!\\?]", PatternItem.SAME, true, Issue.DISPSEVERITY_LOW));
		list.add(new PatternItem("%s", PatternItem.SAME, true, Issue.DISPSEVERITY_HIGH));

		((Parameters)patternsCheckerStep.getParameters()).setPatterns(list);
		patternsCheckerStep.handleStartBatch(Event.START_BATCH_EVENT); // Make sure we re-initialize

		patternsCheckerStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu));
		List<Issue> issues = patternsCheckerStep.getIssues();
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
		tu.setTarget(LocaleId.FRENCH, new TextContainer("test: http://thisBADtest.com"));
		patternsCheckerStep.handleStartBatch(Event.START_BATCH_EVENT); // Make sure we re-initialize
		patternsCheckerStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu));
		List<Issue> issues = patternsCheckerStep.getIssues();
		assertEquals(1, issues.size());
		assertEquals(IssueType.UNEXPECTED_PATTERN, issues.get(0).getIssueType());
		assertEquals(6, issues.get(0).getSourceStart());
		assertEquals(28, issues.get(0).getSourceEnd());
	}
}
