package net.sf.okapi.steps.segmentation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Arrays;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.FileLocation;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filterwriter.GenericContent;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextPart;
import net.sf.okapi.common.resource.TextUnit;
import net.sf.okapi.filters.xml.XMLFilter;
import net.sf.okapi.lib.extra.pipelinebuilder.XBatch;
import net.sf.okapi.lib.extra.pipelinebuilder.XBatchItem;
import net.sf.okapi.lib.extra.pipelinebuilder.XParameter;
import net.sf.okapi.lib.extra.pipelinebuilder.XPipeline;
import net.sf.okapi.lib.extra.pipelinebuilder.XPipelineStep;
import net.sf.okapi.lib.extra.steps.EventLogger;
import net.sf.okapi.steps.common.RawDocumentToFilterEventsStep;
import net.sf.okapi.steps.segmentation.Parameters.SegmStrategy;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class SegmentationStepTest {

	private SegmentationStep segStep;
	private Parameters params;
	private GenericContent fmt;
	private FileLocation root;
	
	@Before
	public void startUp() throws URISyntaxException {
		fmt = new GenericContent();
		segStep = new SegmentationStep();
		segStep.setSourceLocale(LocaleId.ENGLISH);
		segStep.setTargetLocales(Arrays.asList(LocaleId.FRENCH, LocaleId.GERMAN));
		params = (Parameters) segStep.getParameters();
		root = FileLocation.fromClass(this.getClass());
		String srxFile = root.in("/Test01.srx").toString();
		params.setSourceSrxPath(srxFile);
		params.setTargetSrxPath(srxFile);
		params.setSegmentTarget(true);
		segStep.handleStartBatchItem(new Event(EventType.START_BATCH_ITEM));
	}

	@Test
	public void testSegmentationStrategy() {
		ITextUnit tu1 = new TextUnit("tu1");
		TextContainer source = tu1.getSource();
		source.append(new Segment("seg1", new TextFragment("Sentence1. Sentence2.")));
		source.append(new TextPart(" Text part 1. "));
		source.append(new Segment("seg1", new TextFragment("Sentence3.")));
		
		assertEquals(2, source.getSegments().count());
		
		assertEquals("Sentence1. Sentence2.", source.get(0).toString());
		assertTrue(source.get(0).isSegment());
		
		assertEquals(" Text part 1. ", source.get(1).toString());
		assertFalse(source.get(1).isSegment());
		
		assertEquals("Sentence3.", source.get(2).toString());
		assertTrue(source.get(2).isSegment());
		
		// 1
		params.setSegmentationStrategy(SegmStrategy.KEEP_EXISTING);
		segStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu1));
		
		// Check if not changed
		assertEquals(2, source.getSegments().count());
		assertEquals(3, source.count());
		
		assertEquals("Sentence1. Sentence2.", source.get(0).toString());
		assertTrue(source.get(0).isSegment());
		
		assertEquals(" Text part 1. ", source.get(1).toString());
		assertFalse(source.get(1).isSegment());
		
		assertEquals("Sentence3.", source.get(2).toString());
		assertTrue(source.get(2).isSegment());
		
		// 2
		params.setSegmentationStrategy(SegmStrategy.DEEPEN_EXISTING);
		segStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu1));
		
		// Check if did change
		assertEquals(3, source.getSegments().count());
		assertEquals(5, source.count());
		
		assertEquals("Sentence1.", source.get(0).toString());
		assertTrue(source.get(0).isSegment());
		
		assertEquals(" ", source.get(1).toString());
		assertFalse(source.get(1).isSegment());
		
		assertEquals("Sentence2.", source.get(2).toString());
		assertTrue(source.get(2).isSegment());
		
		assertEquals(" Text part 1. ", source.get(3).toString());
		assertFalse(source.get(3).isSegment());
		
		assertEquals("Sentence3.", source.get(4).toString());
		assertTrue(source.get(4).isSegment());
		
		// 3
		params.setSegmentationStrategy(SegmStrategy.OVERWRITE_EXISTING);
		segStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu1));
		
		// Check if did change
		assertEquals(4, source.getSegments().count());
		assertEquals(7, source.count());
		
		assertEquals("Sentence1.", source.get(0).toString());
		assertTrue(source.get(0).isSegment());
		
		assertEquals(" ", source.get(1).toString());
		assertFalse(source.get(1).isSegment());
		
		assertEquals("Sentence2.", source.get(2).toString());
		assertTrue(source.get(2).isSegment());
		
		assertEquals(" ", source.get(3).toString());
		assertFalse(source.get(3).isSegment());
		
		assertEquals("Text part 1.", source.get(4).toString());
		assertTrue(source.get(4).isSegment());
		
		assertEquals(" ", source.get(5).toString());
		assertFalse(source.get(5).isSegment());
		
		assertEquals("Sentence3.", source.get(6).toString());
		assertTrue(source.get(6).isSegment());
	}
	
	@Test
	public void testWhiteSpacesInSingleSegment() throws URISyntaxException {
		ITextUnit tu1 = new TextUnit("tu1");
		TextContainer source = tu1.getSource();
		source.append(new Segment("seg1", new TextFragment("  Text ")));
		
		assertEquals(1, source.getSegments().count());
		assertEquals(1, source.count());
		
		assertEquals("  Text ", source.get(0).toString());
		assertTrue(source.get(0).isSegment());
		
		params.setSegmentationStrategy(SegmStrategy.DEEPEN_EXISTING);
		segStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu1));
		
		assertEquals(1, source.getSegments().count());
		assertEquals(3, source.count());
		
		assertEquals("  ", source.get(0).toString());
		assertFalse(source.get(0).isSegment());
		
		assertEquals("Text", source.get(1).toString());
		assertTrue(source.get(1).isSegment());
		
		assertEquals(" ", source.get(2).toString());
		assertFalse(source.get(2).isSegment());
	}
	
	@Test
	public void testAllTargetsBecomeSegmented() {
		ITextUnit tu1 = new TextUnit("tu1");
		TextContainer source = tu1.getSource();
		source.append(new Segment("seg1", new TextFragment("Part 1. Part 2.")));
		TextContainer targetFr = tu1.createTarget(LocaleId.FRENCH, true, TextUnit.COPY_ALL);
		TextContainer targetDe = tu1.createTarget(LocaleId.GERMAN, true, TextUnit.COPY_ALL);

		params.setSegmentationStrategy(SegmStrategy.OVERWRITE_EXISTING);
		segStep.handleTextUnit(new Event(EventType.TEXT_UNIT, tu1));

		assertEquals("[Part 1.] [Part 2.]", fmt.printSegmentedContent(source, true));
		assertEquals("[Part 1.] [Part 2.]", fmt.printSegmentedContent(targetFr, true));
		assertEquals("[Part 1.] [Part 2.]", fmt.printSegmentedContent(targetDe, true));
	}
	
	@Test
	public void testEvents() throws URISyntaxException, MalformedURLException {
		new XPipeline(
				"Test pipeline for CodeSimplifierStep",
				new XBatch(
						new XBatchItem(
								root.in("/test.xml").asUri(),
								"UTF-8",
								LocaleId.ENGLISH)
						),
						
				new RawDocumentToFilterEventsStep(new XMLFilter()),
				new XPipelineStep(
						new SegmentationStep(),
						new XParameter("sourceSrxPath",
								root.in("/Segmentation.srx").toString())),
				new EventLogger()
		).execute();
	}

}
