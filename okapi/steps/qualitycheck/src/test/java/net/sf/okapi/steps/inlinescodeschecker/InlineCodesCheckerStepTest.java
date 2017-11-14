package net.sf.okapi.steps.inlinescodeschecker;
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
import net.sf.okapi.common.annotation.IssueType;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextUnit;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.lib.verification.Issue;

@RunWith(JUnit4.class)
public class InlineCodesCheckerStepTest {
	private InlineCodesCheckerStep inlineCodesCheckStep;

	@Before
	public void setUp() throws Exception {
		inlineCodesCheckStep = new InlineCodesCheckerStep();
		inlineCodesCheckStep.setSourceLocale(LocaleId.ENGLISH);
		inlineCodesCheckStep.setTargetLocale(LocaleId.FRENCH);
		inlineCodesCheckStep.setParameters(new Parameters());
		inlineCodesCheckStep.handleStartBatch(Event.START_BATCH_EVENT);
		StartDocument sd = new StartDocument("id");
		sd.setMultilingual(true);
		sd.setName("default SD");
		inlineCodesCheckStep.handleStartDocument(new Event(EventType.START_DOCUMENT, sd));		
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testNoIssues () {
		ITextUnit tu = new TextUnit("id", "  Text {with} (123). ");
		tu.setTarget(LocaleId.FRENCH, new TextContainer("  Texte {avec} (123). "));

		inlineCodesCheckStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu));
		List<Issue> issues = inlineCodesCheckStep.getIssues();
		assertEquals(0, issues.size());
	}
	
	@Test
	public void testCODE_DIFFERENCE () {
		ITextUnit tu = new TextUnit("id", "src ");
		tu.getSource().getSegments().get(0).text.append(TagType.PLACEHOLDER, "codeType", "<code/>");
		tu.setTarget(LocaleId.FRENCH, new TextContainer("trg "));
		tu.getTarget(LocaleId.FRENCH).getSegments().get(0).text.append(TagType.PLACEHOLDER, "codeType", "<CODE />");

		inlineCodesCheckStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu));
		List<Issue> issues = inlineCodesCheckStep.getIssues();
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
		tu.setTarget(LocaleId.FRENCH, new TextContainer("trg "));
		tu.getTarget(LocaleId.FRENCH).getSegments().get(0).text.append(TagType.CLOSING, "b", "</b>");
		tu.getTarget(LocaleId.FRENCH).getSegments().get(0).text.append("text");
		tu.getTarget(LocaleId.FRENCH).getSegments().get(0).text.append(TagType.OPENING, "b", "<b>");

		inlineCodesCheckStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu));
		List<Issue> issues = inlineCodesCheckStep.getIssues();
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
		tu.setTarget(LocaleId.FRENCH, new TextContainer("trg "));
		tu.getTarget(LocaleId.FRENCH).getSegments().get(0).text.append(TagType.OPENING, "b", "<b>");
		tu.getTarget(LocaleId.FRENCH).getSegments().get(0).text.append(TagType.PLACEHOLDER, "br", "<br/>");
		tu.getTarget(LocaleId.FRENCH).getSegments().get(0).text.append("text");
		tu.getTarget(LocaleId.FRENCH).getSegments().get(0).text.append(TagType.CLOSING, "b", "</b>");
		tu.getTarget(LocaleId.FRENCH).getSegments().get(0).text.append(TagType.OPENING, "i", "<i>");
		tu.getTarget(LocaleId.FRENCH).getSegments().get(0).text.append(TagType.CLOSING, "i", "</i>");

		inlineCodesCheckStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu));
		List<Issue> issues = inlineCodesCheckStep.getIssues();
		assertEquals(0, issues.size());
	}

	@Test
	public void testCODE_DIFFERENCE_OrderDiffIsOK () {
		ITextUnit tu = new TextUnit("id", "src ");
		tu.getSource().getSegments().get(0).text.append(TagType.PLACEHOLDER, "codeType", "<code1/>");
		tu.getSource().getSegments().get(0).text.append(" and ");
		tu.getSource().getSegments().get(0).text.append(TagType.PLACEHOLDER, "codeType", "<code2/>");
		tu.setTarget(LocaleId.FRENCH, new TextContainer("trg "));
		tu.getTarget(LocaleId.FRENCH).getSegments().get(0).text.append(TagType.PLACEHOLDER, "codeType", "<code2/>");
		tu.getTarget(LocaleId.FRENCH).getSegments().get(0).text.append(" et ");
		tu.getTarget(LocaleId.FRENCH).getSegments().get(0).text.append(TagType.PLACEHOLDER, "codeType", "<code1/>");

		inlineCodesCheckStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu));
		List<Issue> issues = inlineCodesCheckStep.getIssues();
		assertEquals(0, issues.size());
	}
}
