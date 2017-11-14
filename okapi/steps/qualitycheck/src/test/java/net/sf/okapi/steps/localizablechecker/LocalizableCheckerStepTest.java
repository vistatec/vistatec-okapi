package net.sf.okapi.steps.localizablechecker;
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
public class LocalizableCheckerStepTest {
	private LocalizableCheckerStep localizableCheckerStep;

	@Before
	public void setUp() throws Exception {
		localizableCheckerStep = new LocalizableCheckerStep();
		localizableCheckerStep.setSourceLocale(LocaleId.ENGLISH);
		localizableCheckerStep.setTargetLocale(LocaleId.fromString("fr-FR"));
		localizableCheckerStep.setParameters(new Parameters());
		localizableCheckerStep.handleStartBatch(Event.START_BATCH_EVENT);
		StartDocument sd = new StartDocument("id");
		sd.setMultilingual(true);
		sd.setName("default SD");
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testGroupedDecimal() {
		ITextUnit tu = new TextUnit("id", "  123,456.789 ");
		tu.setTarget(LocaleId.fromString("fr-FR"), new TextContainer("  123 456,789 "));

		localizableCheckerStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu));
		List<Issue> issues = localizableCheckerStep.getIssues();
		assertEquals(0, issues.size());
	}
	
	@Test
	public void testGroupedCurrencyPercent() {
		ITextUnit tu = new TextUnit("id", "This is a $2,000 currency and 100%");
		tu.setTarget(LocaleId.fromString("fr-FR"), new TextContainer("This is a $2 000 currency and 100 %"));

		localizableCheckerStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu));
		List<Issue> issues = localizableCheckerStep.getIssues();
		assertEquals(0, issues.size());
	}
	
	@Test
	public void testGroupedDecimalWithIssue() {
		ITextUnit tu = new TextUnit("id", "4,294,967,295.00 ");
		tu.setTarget(LocaleId.fromString("fr-FR"), new TextContainer("4,294,967,295.00"));

		localizableCheckerStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu));
		List<Issue> issues = localizableCheckerStep.getIssues();
		assertEquals(1, issues.size());
		assertEquals(IssueType.SUSPECT_NUMBER, issues.get(0).getIssueType());
	}
	
	@Test
	public void testDatesWithIssues() {
		ITextUnit tu = new TextUnit("id", "A date: Mar 12, 1962");
		tu.setTarget(LocaleId.fromString("fr-FR"), new TextContainer("A date: 13 mars 1962"));
		localizableCheckerStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu));
		List<Issue> issues = localizableCheckerStep.getIssues();
		assertEquals(1, issues.size());
		assertEquals(IssueType.SUSPECT_DATE_TIME, issues.get(0).getIssueType());
		// handlStartBatch will reset issues list
		localizableCheckerStep.handleStartBatch(Event.START_BATCH_EVENT);

		tu = new TextUnit("id", "A date:  3/12/62");
		tu.setTarget(LocaleId.fromString("fr-FR"), new TextContainer("A date: 13/03/1962"));
		localizableCheckerStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu));
		issues = localizableCheckerStep.getIssues();
		assertEquals(1, issues.size());
		assertEquals(IssueType.SUSPECT_DATE_TIME, issues.get(0).getIssueType());
		localizableCheckerStep.handleStartBatch(Event.START_BATCH_EVENT);

		tu = new TextUnit("id", "A date: March 12, 1962");
		tu.setTarget(LocaleId.fromString("fr-FR"), new TextContainer("A date: 13 mars 1962"));
		localizableCheckerStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu));
		issues = localizableCheckerStep.getIssues();
		assertEquals(1, issues.size());
		assertEquals(IssueType.SUSPECT_DATE_TIME, issues.get(0).getIssueType());
		localizableCheckerStep.handleStartBatch(Event.START_BATCH_EVENT);

		tu = new TextUnit("id", "A date:  Tuesday, April 12, 1962");
		tu.setTarget(LocaleId.fromString("fr-FR"), new TextContainer("A date: Mardi, 13 Avril 1962"));
		localizableCheckerStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu));
		issues = localizableCheckerStep.getIssues();
		assertEquals(1, issues.size());
		assertEquals(IssueType.SUSPECT_DATE_TIME, issues.get(0).getIssueType());
	}
	
	@Test
	public void testTimeNoIssues() {
		ITextUnit tu = new TextUnit("id", "A time: 3:30 pm");
		tu.setTarget(LocaleId.fromString("fr-FR"), new TextContainer("A time: 15:30"));
		localizableCheckerStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu));
		List<Issue> issues = localizableCheckerStep.getIssues();
		assertEquals(0, issues.size());
	}
	
	@Test
	public void testDatesNoIssues() {	
		ITextUnit tu = new TextUnit("id", "A date: Mar 12, 1962");
		tu.setTarget(LocaleId.fromString("fr-FR"), new TextContainer("A date: 12 mars 1962"));
		localizableCheckerStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu));
		List<Issue> issues = localizableCheckerStep.getIssues();
		assertEquals(0, issues.size());
		
		tu = new TextUnit("id", "A date:  3/12/62");
		tu.setTarget(LocaleId.fromString("fr-FR"), new TextContainer("A date: 12/03/1962"));
		localizableCheckerStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu));
		issues = localizableCheckerStep.getIssues();
		assertEquals(0, issues.size());
		
		tu = new TextUnit("id", "A date: March 12, 1962");
		tu.setTarget(LocaleId.fromString("fr-FR"), new TextContainer("A date: 12 mars 1962"));
		localizableCheckerStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu));
		issues = localizableCheckerStep.getIssues();
		assertEquals(0, issues.size());
				
		tu = new TextUnit("id", "A date:  Tuesday, April 12, 1962");
		tu.setTarget(LocaleId.fromString("fr-FR"), new TextContainer("A date: Mardi, 12 Avril 1962"));
		localizableCheckerStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu));
		issues = localizableCheckerStep.getIssues();
		assertEquals(0, issues.size());
	}
	
	@Test
	public void testDatesAndNumbersNoIssues() {	
		ITextUnit tu = new TextUnit("id", "1,000 A date: Mar 12, 1962");
		tu.setTarget(LocaleId.fromString("fr-FR"), new TextContainer("1 000A date: 12 mars 1962"));
		localizableCheckerStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu));
		List<Issue> issues = localizableCheckerStep.getIssues();
		assertEquals(0, issues.size());
		
		tu = new TextUnit("id", "A 1,000 date:  3/12/62");
		tu.setTarget(LocaleId.fromString("fr-FR"), new TextContainer("A 1 000 date: 12/03/1962"));
		localizableCheckerStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu));
		issues = localizableCheckerStep.getIssues();
		assertEquals(0, issues.size());
		
		tu = new TextUnit("id", "A date: March 12, 1962 after 1,000");
		tu.setTarget(LocaleId.fromString("fr-FR"), new TextContainer("A date: 12 mars 1962 after 1 000"));
		localizableCheckerStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu));
		issues = localizableCheckerStep.getIssues();
		assertEquals(0, issues.size());
				
		tu = new TextUnit("id", "A date:  Tuesday, April 12, 1962 after 1,000");
		tu.setTarget(LocaleId.fromString("fr-FR"), new TextContainer("A date: Mardi, 12 Avril 1962 after 1 000"));
		localizableCheckerStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu));
		issues = localizableCheckerStep.getIssues();
		assertEquals(0, issues.size());
	}
	
	@Test
	public void testDatesAndNumbersWithIssues() {	
		ITextUnit tu = new TextUnit("id", "1,000 A date: Mar 12, 1962");
		tu.setTarget(LocaleId.fromString("fr-FR"), new TextContainer("10 000 A date: 12 mars 1962"));
		localizableCheckerStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu));
		List<Issue> issues = localizableCheckerStep.getIssues();
		assertEquals(1, issues.size());
		assertEquals(IssueType.SUSPECT_NUMBER, issues.get(0).getIssueType());
		// handlStartBatch will reset issues list
		localizableCheckerStep.handleStartBatch(Event.START_BATCH_EVENT);
		
		tu = new TextUnit("id", "A 1,000 date:  3/12/62");
		tu.setTarget(LocaleId.fromString("fr-FR"), new TextContainer("A 2 000 date: 12/03/1962"));
		localizableCheckerStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu));
		issues = localizableCheckerStep.getIssues();
		assertEquals(1, issues.size());
		assertEquals(IssueType.SUSPECT_NUMBER, issues.get(0).getIssueType());
		// handlStartBatch will reset issues list
		localizableCheckerStep.handleStartBatch(Event.START_BATCH_EVENT);
				
		tu = new TextUnit("id", "A date: March 12, 1962 after 1,000");
		tu.setTarget(LocaleId.fromString("fr-FR"), new TextContainer("A date: 12 mars 1962 after 10 000"));
		localizableCheckerStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu));
		issues = localizableCheckerStep.getIssues();
		assertEquals(1, issues.size());
		assertEquals(IssueType.SUSPECT_NUMBER, issues.get(0).getIssueType());
		// handlStartBatch will reset issues list
		localizableCheckerStep.handleStartBatch(Event.START_BATCH_EVENT);
				
		tu = new TextUnit("id", "A date:  Tuesday, April 12, 1962 after 2,000");
		tu.setTarget(LocaleId.fromString("fr-FR"), new TextContainer("A date: Mardi, 12 Avril 1962 after 1 000"));
		localizableCheckerStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu));
		issues = localizableCheckerStep.getIssues();
		assertEquals(1, issues.size());
		assertEquals(IssueType.SUSPECT_NUMBER, issues.get(0).getIssueType());
	}
	
	@Test
	public void testWithNumbersInlineCodes() {
		ITextUnit tu = new TextUnit("id", "A time: 3:30 pm");
		tu.setTarget(LocaleId.fromString("fr-FR"), new TextContainer("A time: 15:30"));
		
		tu.getSource().getSegments().get(0).text.append(TagType.PLACEHOLDER, "1", "<1/>");
		tu.getTarget(LocaleId.fromString("fr-FR")).getSegments().get(0).text.append(TagType.PLACEHOLDER, "2", "<2/>");
		
		localizableCheckerStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu));
		List<Issue> issues = localizableCheckerStep.getIssues();
		assertEquals(0, issues.size());
	}
	
	@Test
	public void testEmptySource() {
		ITextUnit tu = new TextUnit("id", "");
		tu.setTarget(LocaleId.fromString("fr-FR"), new TextContainer("A time: 15:30"));
		
		tu.getSource().getSegments().get(0).text.append(TagType.PLACEHOLDER, "1", "<1/>");
		tu.getTarget(LocaleId.fromString("fr-FR")).getSegments().get(0).text.append(TagType.PLACEHOLDER, "2", "<2/>");
		
		localizableCheckerStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu));
		List<Issue> issues = localizableCheckerStep.getIssues();
		assertEquals(0, issues.size());
	}
	
	@Test
	public void testEmptyTarget() {
		ITextUnit tu = new TextUnit("id", "A time: 3:30 pm");
		tu.setTarget(LocaleId.fromString("fr-FR"), new TextContainer(""));
		
		tu.getSource().getSegments().get(0).text.append(TagType.PLACEHOLDER, "1", "<1/>");
		tu.getTarget(LocaleId.fromString("fr-FR")).getSegments().get(0).text.append(TagType.PLACEHOLDER, "2", "<2/>");
		
		localizableCheckerStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu));
		List<Issue> issues = localizableCheckerStep.getIssues();
		assertEquals(0, issues.size());
	}
	
	@Test
	public void testDateAtEnd() {
		ITextUnit tu = new TextUnit("id", "3:30 pm");
		tu.setTarget(LocaleId.fromString("fr-FR"), new TextContainer("15:30"));
		
		localizableCheckerStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu));
		List<Issue> issues = localizableCheckerStep.getIssues();
		assertEquals(0, issues.size());
	}
	
	@Test
	public void testDateWithTrailWhiteSpace() {
		ITextUnit tu = new TextUnit("id", "Mar 12, 1962  ");
		tu.setTarget(LocaleId.fromString("fr-FR"), new TextContainer("12 mars 1962  "));
		
		localizableCheckerStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu));
		List<Issue> issues = localizableCheckerStep.getIssues();
		assertEquals(0, issues.size());
	}

}
