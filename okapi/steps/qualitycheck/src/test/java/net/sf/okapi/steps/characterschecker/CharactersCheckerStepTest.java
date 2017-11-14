package net.sf.okapi.steps.characterschecker;
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
public class CharactersCheckerStepTest {
	private CharactersCheckerStep charactersCheckerStep;

	@Before
	public void setUp() throws Exception {
		charactersCheckerStep = new CharactersCheckerStep();
		charactersCheckerStep.setSourceLocale(LocaleId.ENGLISH);
		charactersCheckerStep.setTargetLocale(LocaleId.FRENCH);
		charactersCheckerStep.setParameters(new Parameters());
		charactersCheckerStep.handleStartBatch(Event.START_BATCH_EVENT);
		StartDocument sd = new StartDocument("id");
		sd.setMultilingual(true);
		sd.setName("default SD");
		charactersCheckerStep.handleStartDocument(new Event(EventType.START_DOCUMENT, sd));		
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testNoIssues () {
		ITextUnit tu = new TextUnit("id", "  Text {with} (123). ");
		tu.setTarget(LocaleId.FRENCH, new TextContainer("  Texte {avec} (123). "));

		charactersCheckerStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu));
		List<Issue> issues = charactersCheckerStep.getIssues();
		assertEquals(0, issues.size());
	}
	
	@Test
	public void testAllowedCharacters () {
		ITextUnit tu = new TextUnit("id", "Summer and\nspring");
		tu.setTarget(LocaleId.FRENCH, new TextContainer("\u00e9t\u00e9 et printemps"));
		tu.getTarget(LocaleId.FRENCH).setAnnotation(new GenericAnnotations(new GenericAnnotation(GenericAnnotationType.ALLOWEDCHARS,
			GenericAnnotationType.ALLOWEDCHARS_VALUE, "[a-z ]")));
		charactersCheckerStep.handleStartBatch(Event.START_BATCH_EVENT); // Make sure we re-initialize
		charactersCheckerStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu));
		List<Issue> issues = charactersCheckerStep.getIssues();
		assertEquals(1, issues.size());
		assertEquals(IssueType.ALLOWED_CHARACTERS, issues.get(0).getIssueType());
	}
}
