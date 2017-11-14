package net.sf.okapi.steps.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.IResource;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.AlignmentStatus;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.common.resource.TextUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ConvertSegmentsToTextUnitTest {
	private ConvertSegmentsToTextUnitsStep converter;
	private ITextUnit segmentedTu;
	private ITextUnit sourceOnlySegmentedTu;
	private ITextUnit segmentedTuWithCodes;
	private ITextUnit nonSegmentedTu;
	
	
	@Before
	public void setUp() {
		converter = new ConvertSegmentsToTextUnitsStep();

		// set up segmentedTU
		segmentedTu = new TextUnit("segmentedTU");
		segmentedTu.createTarget(LocaleId.SPANISH, true, IResource.CREATE_EMPTY);
		
		for (int j = 0; j < 3; j++) {			
			Segment srcSeg = new Segment(Integer.toString(j), new TextFragment("a segment. "));
			Segment trgSeg = new Segment(Integer.toString(j), new TextFragment("A SEGMENT. "));
			segmentedTu.getSource().append(srcSeg);
			segmentedTu.getTarget(LocaleId.SPANISH).append(trgSeg);
		}
		segmentedTu.getTarget(LocaleId.SPANISH).getSegments().setAlignmentStatus(AlignmentStatus.ALIGNED);

		// set up sourceOnlySegmentedTu
		sourceOnlySegmentedTu = new TextUnit("sourceOnlysegmentedTU");
		
		for (int j = 0; j < 3; j++) {			
			Segment srcSeg = new Segment(Integer.toString(j), new TextFragment("a segment. "));
			sourceOnlySegmentedTu.getSource().append(srcSeg);
		}		

		// set up segmentedTUWithCodes
		segmentedTuWithCodes = new TextUnit("segmentedTUWithCodes");
		segmentedTuWithCodes.createTarget(LocaleId.SPANISH, true, IResource.CREATE_EMPTY);
		
		for (int j = 0; j < 3; j++) {			
			TextFragment stf = new TextFragment("a segment. ");
			Code sc = new Code(TagType.PLACEHOLDER, "type", "data");
			sc.setId(15);
			stf.append(sc);
			Segment srcSeg = new Segment(Integer.toString(j), stf);
			
			TextFragment ttf = new TextFragment("A SEGMENT. ");
			Code tc = new Code(TagType.PLACEHOLDER, "type", "data");
			tc.setId(15);
			ttf.append(tc);
			Segment trgSeg = new Segment(Integer.toString(j), ttf);
			
			segmentedTuWithCodes.getSource().append(srcSeg);
			segmentedTuWithCodes.getTarget(LocaleId.SPANISH).append(trgSeg);
		}
		segmentedTuWithCodes.getTarget(LocaleId.SPANISH).getSegments().setAlignmentStatus(AlignmentStatus.ALIGNED);
				
		// set up non segmented TU
		nonSegmentedTu = new TextUnit("NON-SEGMENTED");
		nonSegmentedTu.setSourceContent(new TextFragment("a segment. "));
		nonSegmentedTu.setTargetContent(LocaleId.SPANISH, new TextFragment("A SEGMENT. "));
	}
		
	@Test
	public void convertSegmentedTuToMultiple() {
		Event event = converter.handleTextUnit(new Event(EventType.TEXT_UNIT, segmentedTu));
		int count = 0;
		for (Event e : event.getMultiEvent()) {
			count++;
			ITextUnit tu = e.getTextUnit();
			assertEquals("a segment. ", tu.getSource().toString());
			assertEquals("A SEGMENT. ", tu.getTarget(LocaleId.SPANISH).toString());
		}
		
		assertEquals(3, count);
	}
	
	@Test
	public void convertSourceOnlySegmentedTuToMultiple() {
		Event event = converter.handleTextUnit(new Event(EventType.TEXT_UNIT, sourceOnlySegmentedTu));
		int count = 0;
		for (Event e : event.getMultiEvent()) {
			count++;
			ITextUnit tu = e.getTextUnit();
			assertEquals("a segment. ", tu.getSource().toString());
			assertEquals(false, tu.hasTarget(LocaleId.SPANISH));
		}
		
		assertEquals(3, count);
	}
	
	@Test
	public void convertSegmentedTuToMultipleWithCodes() {
		Event event = converter.handleTextUnit(new Event(EventType.TEXT_UNIT, segmentedTuWithCodes));
		int count = 0;
		for (Event e : event.getMultiEvent()) {
			count++;
			ITextUnit tu = e.getTextUnit();
			Code sc = tu.getSource().getFirstContent().getCode(0);
			assertEquals(15, sc.getId());
			assertEquals("a segment. data", tu.getSource().toString());
			Code tc = tu.getSource().getFirstContent().getCode(0);
			assertEquals(15, tc.getId());
			assertEquals("A SEGMENT. data", tu.getTarget(LocaleId.SPANISH).toString());
		}
		
		assertEquals(3, count);
	}
	
	@Test
	public void convertNonSegmentedTuToMultiple() {
		Event event = converter.handleTextUnit(new Event(EventType.TEXT_UNIT, nonSegmentedTu));
		int count = 0;
		for (Event e : event.getMultiEvent()) {
			count++;
			ITextUnit tu = e.getTextUnit();
			assertEquals("a segment. ", tu.getSource().toString());
			assertEquals("A SEGMENT. ", tu.getTarget(LocaleId.SPANISH).toString());
		}
		
		assertEquals(1, count);
	}
	
	@Test
	public void convertNull() {
		Event event = converter.handleTextUnit(new Event(EventType.TEXT_UNIT, null));
		assertNull(event.getTextUnit());
	}
	
	@Test
	public void convertEmptyNonNull() {
		Event event = converter.handleTextUnit(new Event(EventType.TEXT_UNIT, new TextUnit("NON-NULL")));
		assertNotNull(event.getTextUnit());
	}
}
