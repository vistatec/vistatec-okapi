package net.sf.okapi.steps.desegmentation;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.FileLocation;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextPart;
import net.sf.okapi.common.resource.TextUnit;
import net.sf.okapi.steps.segmentation.Parameters.SegmStrategy;
import net.sf.okapi.steps.segmentation.SegmentationStep;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class DesegmentationStepTest {

	private SegmentationStep segStep;
	private DesegmentationStep desegStep;
	private Parameters params;
	private List<LocaleId> targetLocales = Arrays.asList(LocaleId.FRENCH, LocaleId.GERMAN);
	
	@Before
	public void startUp() throws URISyntaxException {
		segStep = new SegmentationStep();
		segStep.setSourceLocale(LocaleId.ENGLISH);
		segStep.setTargetLocales(targetLocales);
		net.sf.okapi.steps.segmentation.Parameters segParams = 
				(net.sf.okapi.steps.segmentation.Parameters) segStep.getParameters();
		String srxFile = FileLocation.fromClass(this.getClass()).in("/Test01.srx").toString();
		segParams.setSourceSrxPath(srxFile);
		segParams.setTargetSrxPath(srxFile);
		segParams.setSegmentTarget(true);
		segParams.setCopySource(true);
		segParams.setSegmentationStrategy(SegmStrategy.OVERWRITE_EXISTING);
		segStep.handleEvent(new Event(EventType.START_BATCH_ITEM));
		desegStep = new DesegmentationStep();
		desegStep.setTargetLocales(targetLocales);
		desegStep.handleEvent(new Event(EventType.START_BATCH_ITEM));
		params = (Parameters)desegStep.getParameters();
	}
	
	@Test
	public void testDesegmentation() {
		ITextUnit tu = new TextUnit("tu1");
		TextContainer source = tu.getSource();
		source.append(new TextPart("Sentence 1. Sentence 2. Sentence 3."));
		segStep.handleEvent(new Event(EventType.TEXT_UNIT, tu));
		// Verify that we're at a known starting point
		assertSegmentedSource(tu);
		assertSegmentedTarget(tu);

		// 1 - desegment source and target
		ITextUnit tu1 = tu.clone();
		params.setDesegmentSource(true);
		params.setDesegmentTarget(true);
		desegStep.handleEvent(new Event(EventType.TEXT_UNIT, tu1));
		assertDesegmentedSource(tu1);
		assertDesegmentedTarget(tu1);
		
		// 2 - desegment nothing
		tu1 = tu.clone();
		params.setDesegmentSource(false);
		params.setDesegmentTarget(false);
		desegStep.handleEvent(new Event(EventType.TEXT_UNIT, tu1));
		// should be unchanged
		assertSegmentedSource(tu1);
		assertSegmentedTarget(tu1);
		
		// 3 - desegment target, not source
		tu1 = tu.clone();
		source = tu1.getSource();
		params.setDesegmentSource(false);
		params.setDesegmentTarget(true);
		desegStep.handleEvent(new Event(EventType.TEXT_UNIT, tu1));
		assertSegmentedSource(tu1);
		assertDesegmentedTarget(tu1);
		
		// 4 - desegment source, not target
		tu1 = tu.clone();
		source = tu1.getSource();
		params.setDesegmentSource(true);
		params.setDesegmentTarget(false);
		desegStep.handleEvent(new Event(EventType.TEXT_UNIT, tu1));
		assertDesegmentedSource(tu1);
		assertSegmentedTarget(tu1);
	}
	
	private void assertSegmentedSource(ITextUnit tu) {
		TextContainer source = tu.getSource();
		assertEquals(3, source.getSegments().count());
		assertEquals("Sentence 1.", source.getSegments().get(0).toString());
		assertEquals("Sentence 2.", source.getSegments().get(1).toString());
		assertEquals("Sentence 3.", source.getSegments().get(2).toString());
	}
	
	private void assertSegmentedTarget(ITextUnit tu) {
		for (LocaleId locale : targetLocales) {
			TextContainer target = tu.getTarget(locale);
			assertEquals(3, target.getSegments().count());
			assertEquals("Sentence 1.", target.getSegments().get(0).toString());
			assertEquals("Sentence 2.", target.getSegments().get(1).toString());
			assertEquals("Sentence 3.", target.getSegments().get(2).toString());
		}
	}
	
	private void assertDesegmentedSource(ITextUnit tu) {
		TextContainer source = tu.getSource();
		assertEquals("Sentence 1. Sentence 2. Sentence 3.", source.getFirstContent().toString());
	}
	
	private void assertDesegmentedTarget(ITextUnit tu) {
		for (LocaleId locale : targetLocales) {
			TextContainer target = tu.getTarget(locale);
			assertEquals(1, target.getSegments().count());
			assertEquals("Sentence 1. Sentence 2. Sentence 3.", target.getSegments().get(0).toString());
		}
	}
}
	
