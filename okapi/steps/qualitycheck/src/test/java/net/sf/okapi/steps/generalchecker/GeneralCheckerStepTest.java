package net.sf.okapi.steps.generalchecker;
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
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextUnit;
import net.sf.okapi.lib.verification.Issue;

@RunWith(JUnit4.class)
public class GeneralCheckerStepTest {
	private GeneralCheckerStep generalCheckStep;

	@Before
	public void setUp() throws Exception {
		generalCheckStep = new GeneralCheckerStep();
		generalCheckStep.setSourceLocale(LocaleId.ENGLISH);
		generalCheckStep.setTargetLocale(LocaleId.FRENCH);
		generalCheckStep.setParameters(new Parameters());
		generalCheckStep.handleStartBatch(Event.START_BATCH_EVENT);
		StartDocument sd = new StartDocument("id");
		sd.setMultilingual(true);
		sd.setName("default SD");
		generalCheckStep.handleStartDocument(new Event(EventType.START_DOCUMENT, sd));		
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testNoIssues () {
		ITextUnit tu = new TextUnit("id", "  Text {with} (123). ");
		tu.setTarget(LocaleId.FRENCH, new TextContainer("  Texte {avec} (123). "));

		generalCheckStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu));
		List<Issue> issues = generalCheckStep.getIssues();
		assertEquals(0, issues.size());
	}

	@Test
	public void testMISSING_TARGETTU () {
		// Create source with non-empty content
		// but no target
		ITextUnit tu = new TextUnit("id", "source");

		generalCheckStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu));
		List<Issue> issues = generalCheckStep.getIssues();
		assertEquals(1, issues.size());
		assertEquals(IssueType.MISSING_TARGETTU, issues.get(0).getIssueType());
	}
	
	@Test
	public void testEMPTY_TARGETSEG () {
		// Create TU with source of non-empty segment
		// and target of empty segment
		ITextUnit tu = new TextUnit("id", "source");
		tu.setTarget(LocaleId.FRENCH, new TextContainer());

		generalCheckStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu));
		List<Issue> issues = generalCheckStep.getIssues();
		assertEquals(1, issues.size());
		assertEquals(IssueType.EMPTY_TARGETSEG, issues.get(0).getIssueType());
	}

	@Test
	public void testEMPTY_SOURCESEG () {
		// Create TU with source of non-empty segment
		// and target of empty segment
		ITextUnit tu = new TextUnit("id", "");
		tu.setTarget(LocaleId.FRENCH, new TextContainer("target"));

		generalCheckStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu));
		List<Issue> issues = generalCheckStep.getIssues();
		assertEquals(1, issues.size());
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
		tu.setTarget(LocaleId.FRENCH, new TextContainer("trgext1"));

		generalCheckStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu));
		List<Issue> issues = generalCheckStep.getIssues();
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
		tu.setTarget(LocaleId.FRENCH, tc);

		generalCheckStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu));
		List<Issue> issues = generalCheckStep.getIssues();
		assertEquals(1, issues.size());
		assertEquals(IssueType.EMPTY_TARGETSEG, issues.get(0).getIssueType());
	}

	@Test
	public void testMISSINGORDIFF_LEADINGWS () {
		ITextUnit tu = new TextUnit("id", "  srctext");
		tu.setTarget(LocaleId.FRENCH, new TextContainer("trgext"));

		generalCheckStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu));
		List<Issue> issues = generalCheckStep.getIssues();
		assertEquals(1, issues.size());
		assertEquals(IssueType.MISSINGORDIFF_LEADINGWS, issues.get(0).getIssueType());
	}

	@Test
	public void testMISSINGORDIFF_TRAILINGWS () {
		ITextUnit tu = new TextUnit("id", " srctext ");
		tu.setTarget(LocaleId.FRENCH, new TextContainer(" trgext"));

		generalCheckStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu));
		List<Issue> issues = generalCheckStep.getIssues();
		assertEquals(1, issues.size());
		assertEquals(IssueType.MISSINGORDIFF_TRAILINGWS, issues.get(0).getIssueType());
	}

	@Test
	public void testEXTRAORDIFF_LEADINGWS () {
		ITextUnit tu = new TextUnit("id", "  srctext");
		tu.setTarget(LocaleId.FRENCH, new TextContainer("   trgext"));

		generalCheckStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu));
		List<Issue> issues = generalCheckStep.getIssues();
		assertEquals(1, issues.size());
		assertEquals(IssueType.EXTRAORDIFF_LEADINGWS, issues.get(0).getIssueType());
	}

	@Test
	public void testEXTRAORDIFF_TRAILINGWS () {
		ITextUnit tu = new TextUnit("id", "srctext  ");
		tu.setTarget(LocaleId.FRENCH, new TextContainer("trgtext   "));

		generalCheckStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu));
		List<Issue> issues = generalCheckStep.getIssues();
		assertEquals(1, issues.size());
		assertEquals(IssueType.EXTRAORDIFF_TRAILINGWS, issues.get(0).getIssueType());
	}

	@Test
	public void testTARGET_SAME_AS_SOURCE () {
		ITextUnit tu = new TextUnit("id", "src text");
		tu.setTarget(LocaleId.FRENCH, new TextContainer("src text"));

		generalCheckStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu));
		List<Issue> issues = generalCheckStep.getIssues();
		assertEquals(1, issues.size());
		assertEquals(IssueType.TARGET_SAME_AS_SOURCE, issues.get(0).getIssueType());
	}

	@Test
	public void testTARGET_SAME_AS_SOURCE_withoutWords () {
		ITextUnit tu = new TextUnit("id", ":?%$#@#_~`()[]{}=+-");
		tu.setTarget(LocaleId.FRENCH, new TextContainer(":?%$#@#_~`()[]{}=+-"));

		generalCheckStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu));
		List<Issue> issues = generalCheckStep.getIssues();
		assertEquals(0, issues.size());
	}
}
