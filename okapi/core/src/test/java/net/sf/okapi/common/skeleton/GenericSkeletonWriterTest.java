package net.sf.okapi.common.skeleton;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.filters.FilterTestDriver;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.DocumentPart;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.common.resource.TextUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class GenericSkeletonWriterTest {

	static final public LocaleId locEN = LocaleId.ENGLISH;
	static final public LocaleId locFR = LocaleId.FRENCH;
	static final public LocaleId locDE = LocaleId.GERMAN;
	
	private ISkeletonWriter gsw;
	private EncoderManager encMgt;

	@Before
	public void setUp () {
		gsw = new GenericSkeletonWriter();
		encMgt = new EncoderManager();
	}
	
	@Test
	public void testContentPlaceholder_NoTranslation () {
		// Start
		List<Event> events = TestUtil.createStartEvents(false, gsw, encMgt);
		// TU
		GenericSkeleton gs = new GenericSkeleton();
		gs.add("before [");
		ITextUnit tu = TestUtil.createSimpleTU();
		gs.addContentPlaceholder(tu);
		gs.add("] after");
		tu.setSkeleton(gs);
		events.add(new Event(EventType.TEXT_UNIT, tu));
		// End
		TestUtil.addEndEvents(events);
		events = processEvents(events, false, locFR);
		
		String expected = "before [text1] after";
		String result = FilterTestDriver.generateOutput(events, locFR, gsw, encMgt, false);
		assertEquals(expected, result);
	}
	
	@Test
	public void testContentPlaceholder_Translated () {
		// Start
		List<Event> events = TestUtil.createStartEvents(false, gsw, encMgt);
		// TU
		GenericSkeleton gs = new GenericSkeleton();
		gs.add("before [");
		ITextUnit tu = TestUtil.createTranslatedTU();
		gs.addContentPlaceholder(tu);
		gs.add("] after");
		tu.setSkeleton(gs);
		events.add(new Event(EventType.TEXT_UNIT, tu));
		// End
		TestUtil.addEndEvents(events);
		events = processEvents(events, false, locFR);
		
		String expected = "before [target1] after";
		String result = FilterTestDriver.generateOutput(events, locFR, gsw, encMgt, false);
		assertEquals(expected, result);
	}
	
	@Test
	public void testContentPlaceholder_Bilingual () {
		// Start
		List<Event> events = TestUtil.createStartEvents(true, gsw, encMgt);
		// TU
		GenericSkeleton gs = new GenericSkeleton();
		gs.add("lang1=[");
		ITextUnit tu = TestUtil.createTranslatedTU();
		gs.addContentPlaceholder(tu);
		gs.add("] lang2=[");
		gs.addContentPlaceholder(tu, locFR);
		gs.add("]");
		tu.setSkeleton(gs);
		events.add(new Event(EventType.TEXT_UNIT, tu));
		// End
		TestUtil.addEndEvents(events);
		events = processEvents(events, true, locFR);
		
		String expected = "lang1=[text1] lang2=[target1]";
		String result = FilterTestDriver.generateOutput(events, locFR, gsw, encMgt, false);
		assertEquals(expected, result);

		expected = "lang1=[text1] lang2=[TARGET1]";
		result = FilterTestDriver.generateOutput(events, locFR, gsw, encMgt, true);
		assertEquals(expected, result);
	}
	
	@Test
	public void testContentPlaceholder_Trilingual () {
		// Start
		List<Event> origEvents = TestUtil.createStartEvents(true, gsw, encMgt);
		// TU
		GenericSkeleton gs = new GenericSkeleton();
		gs.add("lang1=[");
		ITextUnit tu = TestUtil.createTranslatedTU();
		tu.setTarget(locDE, new TextContainer("target2"));
		gs.addContentPlaceholder(tu);
		gs.add("] lang2=[");
		gs.addContentPlaceholder(tu, locFR);
		gs.add("] lang3=[");
		gs.addContentPlaceholder(tu, locDE);
		gs.add("]");
		tu.setSkeleton(gs);
		origEvents.add(new Event(EventType.TEXT_UNIT, tu));
		// End
		TestUtil.addEndEvents(origEvents);
		List<Event> events = processEvents(origEvents, true, locFR);
		
		String expected = "lang1=[text1] lang2=[target1] lang3=[target2]";
		String result = FilterTestDriver.generateOutput(events, locFR, gsw, encMgt, false);
		assertEquals(expected, result);

		expected = "lang1=[text1] lang2=[TARGET1] lang3=[target2]";
		result = FilterTestDriver.generateOutput(events, locFR, gsw, encMgt, true);
		assertEquals(expected, result);
		
		
		events = processEvents(origEvents, true, locDE);
		tu.setTarget(locFR, new TextContainer("target1")); // Reset content
		expected = "lang1=[text1] lang2=[target1] lang3=[TARGET2]";
		result = FilterTestDriver.generateOutput(events, locDE, gsw, encMgt, true);
		assertEquals(expected, result);
	}
	
	@Test
	public void testValuePlaceholders () {
		// Start
		List<Event> events = TestUtil.createStartEvents(false, gsw, encMgt);
		// TU
		GenericSkeleton gs = new GenericSkeleton();
		gs.add("[");
		ITextUnit tu = TestUtil.createTranslatedTU();
		tu.setSourceProperty(new Property("srcProp", "val1", false));
		tu.setProperty(new Property("tuProp", "val2", false));
		tu.setTargetProperty(locFR, new Property("trgProp", "val3", false));
		gs.addContentPlaceholder(tu);
		gs.add("] srcProp={");
		gs.addValuePlaceholder(tu, "srcProp", null);
		gs.add("} tuProp={");
		gs.addValuePlaceholder(tu, "tuProp", LocaleId.EMPTY);
		gs.add("} trgProp={");
		gs.addValuePlaceholder(tu, "trgProp", locFR);
		gs.add("}");		
		tu.setSkeleton(gs);
		events.add(new Event(EventType.TEXT_UNIT, tu));
		// End
		TestUtil.addEndEvents(events);
		events = processEvents(events, false, locFR);
		
		String expected = "[target1] srcProp={val1} tuProp={val2} trgProp={val3}";
		String result = FilterTestDriver.generateOutput(events, locFR, gsw, encMgt, false);
		assertEquals(expected, result);
	}
	
	@Test
	public void testLanguagePlaceholder () {
		// Start
		List<Event> events = TestUtil.createStartEvents(false, gsw, encMgt);
		// TU
		GenericSkeleton gs = new GenericSkeleton();
		gs.add("[");
		ITextUnit tu = TestUtil.createTranslatedTU();
		tu.setSourceProperty(new Property(Property.LANGUAGE, locEN.toString(), false));
		gs.addContentPlaceholder(tu);
		gs.add("] language={");
		gs.addValuePlaceholder(tu, Property.LANGUAGE, null);
		gs.add("}");
		tu.setSkeleton(gs);
		events.add(new Event(EventType.TEXT_UNIT, tu));
		// End
		TestUtil.addEndEvents(events);
		events = processEvents(events, false, locFR);
		
		String expected = "[target1] language={fr}";
		String result = FilterTestDriver.generateOutput(events, locFR, gsw, encMgt, false);
		assertEquals(expected, result);
	}
	
	@Test
	public void testLanguagePlaceholderRegionInsensitive () {
		// Start
		List<Event> events = TestUtil.createStartEvents(false, gsw, encMgt);
		// TU
		GenericSkeleton gs = new GenericSkeleton();
		gs.add("[");
		ITextUnit tu = TestUtil.createTranslatedTU();
		tu.setSourceProperty(new Property(Property.LANGUAGE, "en-us", false));
		gs.addContentPlaceholder(tu);
		gs.add("] language={");
		gs.addValuePlaceholder(tu, Property.LANGUAGE, null);
		gs.add("}");
		tu.setSkeleton(gs);
		events.add(new Event(EventType.TEXT_UNIT, tu));
		// End
		TestUtil.addEndEvents(events);
		events = processEvents(events, false, locFR);
		
		String expected = "[target1] language={fr}";
		String result = FilterTestDriver.generateOutput(events, locFR, gsw, encMgt, false);
		assertEquals(expected, result);
	}
	
	@Test
	public void testReference () {
		// Start
		List<Event> events = TestUtil.createStartEvents(false, gsw, encMgt);
		// TU
		GenericSkeleton gs1 = new GenericSkeleton();
		gs1.add("{sub-block [");
		ITextUnit tu = TestUtil.createSimpleTU();
		tu.setIsReferent(true);
		gs1.addContentPlaceholder(tu);
		gs1.add("]}");
		tu.setSkeleton(gs1);
		events.add(new Event(EventType.TEXT_UNIT, tu));
		// TU parent
		GenericSkeleton gs2 = new GenericSkeleton();
		gs2.add("Start ");
		gs2.addReference(tu);
		gs2.add(" end.");
		DocumentPart dp = new DocumentPart("dp1", false);
		dp.setSkeleton(gs2);
		events.add(new Event(EventType.DOCUMENT_PART, dp));
		// End
		TestUtil.addEndEvents(events);
		events = processEvents(events, false, locFR);
		
		String expected = "Start {sub-block [text1]} end.";
		String result = FilterTestDriver.generateOutput(events, locFR, gsw, encMgt, false);
		assertEquals(expected, result);

		expected = "Start {sub-block [TEXT1]} end.";
		result = FilterTestDriver.generateOutput(events, locFR, gsw, encMgt, true);
		assertEquals(expected, result);
	}
	
	@Test
	public void testSegmentRef() {
		ITextUnit tu1 = new TextUnit("tu1");
		
		TextFragment tf = new TextFragment();
		tf.append(new Code(TagType.OPENING, Code.TYPE_BOLD, "<b>"));
		tf.append("Source segment 11");
		tf.append(new Code(TagType.CLOSING, Code.TYPE_BOLD, "</b>"));
		
		tu1.getSource().getSegments().append(new Segment("sseg11", tf));
		tu1.getSource().getSegments().append(new Segment("sseg12", new TextFragment("Source segment 12")));
		
		tu1.setTarget(locDE, new TextContainer());
		tu1.getTarget(locDE).getSegments().append(new Segment("tseg11", new TextFragment("Target segment 11")));
		tu1.getTarget(locDE).getSegments().append(new Segment("tseg12", new TextFragment("Target segment 12")));
		
		GenericSkeleton skel = new GenericSkeleton();
		tu1.setSkeleton(skel);
		
		skel.add("{0>");
		TestUtil.createSrcSegRefPart(skel, tu1, "sseg11");
		skel.add("<}10{>");			
		TestUtil.createTrgSegRefPart(skel, tu1, "tseg11", locDE);
		skel.add("<0}");
		skel.add(" ");
		skel.add("{0>");
		TestUtil.createSrcSegRefPart(skel, tu1, "sseg12");
		skel.add("<}90{>");			
		TestUtil.createTrgSegRefPart(skel, tu1, "tseg12", locDE);
		skel.add("<0}");
		
		assertEquals("{0>[#$sseg11@%$segment$]<}10{>[#$tseg11@%$segment$]<0} {0>[#$sseg12@%$segment$]<}90{>[#$tseg12@%$segment$]<0}",
				skel.toString());
		List<GenericSkeletonPart> parts = skel.getParts();
		assertEquals(11, parts.size());
		
		GenericSkeletonPart part;
		part = parts.get(1);
		assertEquals(tu1, part.getParent());
		assertNull(part.getLocale());
		
		part = parts.get(3);
		assertEquals(tu1, part.getParent());
		assertEquals(locDE, part.getLocale());
		
		part = parts.get(7);
		assertEquals(tu1, part.getParent());
		assertNull(part.getLocale());
		
		part = parts.get(9);
		assertEquals(tu1, part.getParent());
		assertEquals(locDE, part.getLocale());
		
		ITextUnit tu2 = new TextUnit("tu2");
		
		tu2.getSource().getSegments().append(new Segment("sseg21", new TextFragment("Source segment 21")));
		tu2.getSource().getSegments().append(new Segment("sseg22", new TextFragment("Source segment 22")));
		
		tu2.setTarget(locDE, new TextContainer());
		tu2.getTarget(locDE).getSegments().append(new Segment("tseg21", new TextFragment("Target segment 21")));
		tu2.getTarget(locDE).getSegments().append(new Segment("tseg22", new TextFragment("Target segment 22")));
		
		skel = new GenericSkeleton();
		tu2.setSkeleton(skel);
		
		skel.add("{0>");
		TestUtil.createSrcSegRefPart(skel, tu2, "sseg21");
		skel.add("<}10{>");			
		TestUtil.createTrgSegRefPart(skel, tu2, "tseg21", locDE);
		skel.add("<0}");
		skel.add(" ");
		skel.add("{0>");
		TestUtil.createSrcSegRefPart(skel, tu2, "sseg22");
		skel.add("<}90{>");			
		TestUtil.createTrgSegRefPart(skel, tu2, "tseg22", locDE);
		skel.add("<0}");
		
		assertEquals("{0>[#$sseg21@%$segment$]<}10{>[#$tseg21@%$segment$]<0} {0>[#$sseg22@%$segment$]<}90{>[#$tseg22@%$segment$]<0}", 
				skel.toString());
		parts = skel.getParts();
		assertEquals(11, parts.size());
		
		part = parts.get(1);
		assertEquals(tu2, part.getParent());
		assertNull(part.getLocale());
		
		part = parts.get(3);
		assertEquals(tu2, part.getParent());
		assertEquals(locDE, part.getLocale());
		
		part = parts.get(7);
		assertEquals(tu2, part.getParent());
		assertNull(part.getLocale());
		
		part = parts.get(9);
		assertEquals(tu2, part.getParent());
		assertEquals(locDE, part.getLocale());
				
		List<Event> events = TestUtil.createStartEvents(true, gsw, encMgt);
		events.add(new Event(EventType.TEXT_UNIT, tu1));
		events.add(new Event(EventType.TEXT_UNIT, tu2));
		TestUtil.addEndEvents(events);
		events = processEvents(events, true, locDE);
		
		String expected = "{0><b>Source segment 11</b><}10{>Target segment 11<0} {0>Source segment 12<}90{>Target segment 12<0}" +
				"{0>Source segment 21<}10{>Target segment 21<0} {0>Source segment 22<}90{>Target segment 22<0}";
		String result = FilterTestDriver.generateOutput(events, locDE, gsw, encMgt, false);
		assertEquals(expected, result);
	}
	
	@Test
	public void testSegmentRef2() {
		ITextUnit tu1 = new TextUnit("tu1");
		
		TextFragment tf = new TextFragment();
		tf.append(new Code(TagType.OPENING, Code.TYPE_BOLD, "<b>"));
		tf.append("Source segment 1");
		tf.append(new Code(TagType.CLOSING, Code.TYPE_BOLD, "</b>"));
		
		tu1.getSource().getSegments().append(new Segment("sseg1", tf));
		tu1.getSource().getSegments().append(new Segment("sseg2", new TextFragment("Source segment 2")));
		
		tu1.setTarget(locDE, new TextContainer());
		tu1.getTarget(locDE).getSegments().append(new Segment("tseg1", new TextFragment("Target segment 1")));
		tu1.getTarget(locDE).getSegments().append(new Segment("tseg2", new TextFragment("Target segment 2")));
		
		GenericSkeleton skel = new GenericSkeleton();
		tu1.setSkeleton(skel);
		
		skel.add("{0>");
		TestUtil.createSrcSegRefPart(skel, tu1, "sseg1");
		skel.add("<}10{>");			
		TestUtil.createTrgSegRefPart(skel, tu1, "tseg1", locDE);
		skel.add("<0}");
		skel.add(" ");
		skel.add("{0>");
		TestUtil.createSrcSegRefPart(skel, tu1, "sseg2");
		skel.add("<}90{>");			
		TestUtil.createTrgSegRefPart(skel, tu1, "tseg2", locDE);
		skel.add("<0}");
		
		assertEquals("{0>[#$sseg1@%$segment$]<}10{>[#$tseg1@%$segment$]<0} {0>[#$sseg2@%$segment$]<}90{>[#$tseg2@%$segment$]<0}", 
				skel.toString());
		List<GenericSkeletonPart> parts = skel.getParts();
		assertEquals(11, parts.size());
		
		GenericSkeletonPart part;
		part = parts.get(1);
		assertEquals(tu1, part.getParent());
		assertNull(part.getLocale());
		
		part = parts.get(3);
		assertEquals(tu1, part.getParent());
		assertEquals(locDE, part.getLocale());
		
		part = parts.get(7);
		assertEquals(tu1, part.getParent());
		assertNull(part.getLocale());
		
		part = parts.get(9);
		assertEquals(tu1, part.getParent());
		assertEquals(locDE, part.getLocale());
		
		ITextUnit tu2 = new TextUnit("tu2");
		
		tu2.getSource().getSegments().append(new Segment("sseg21", new TextFragment("Source segment 21")));
		tu2.getSource().getSegments().append(new Segment("sseg22", new TextFragment("Source segment 22")));
		
		tu2.setTarget(locDE, new TextContainer());
		tu2.getTarget(locDE).getSegments().append(new Segment("tseg21", new TextFragment("Target segment 21")));
		
		TextFragment tf2 = new TextFragment();
		tf2.append("Target segment 22.1");
		Code code = new Code(TagType.PLACEHOLDER, null, TextFragment.makeRefMarker("tu1"));
		code.setReferenceFlag(true);
		tf2.append(code);
		tu1.setIsReferent(true);
		
		tf2.append("Target segment 22.2");
		tu2.getTarget(locDE).getSegments().append(new Segment("tseg22", tf2));
		
		skel = new GenericSkeleton();
		tu2.setSkeleton(skel);
		
		skel.add("{0>");
		TestUtil.createSrcSegRefPart(skel, tu1, "sseg1");
		skel.add("<}10{>");			
		TestUtil.createTrgSegRefPart(skel, tu1, "tseg1", locDE);
		skel.add("<0}");
		skel.add(" ");
		skel.add("{0>");
		TestUtil.createSrcSegRefPart(skel, tu2, "sseg22");
		skel.add("<}90{>");			
		TestUtil.createTrgSegRefPart(skel, tu2, "tseg22", locDE);
		skel.add("<0}");
		
		assertEquals("{0>[#$sseg1@%$segment$]<}10{>[#$tseg1@%$segment$]<0} {0>[#$sseg22@%$segment$]<}90{>[#$tseg22@%$segment$]<0}", 
				skel.toString());
		parts = skel.getParts();
		assertEquals(11, parts.size());
		
		part = parts.get(1);
		assertEquals(tu1, part.getParent());
		assertNull(part.getLocale());
		
		part = parts.get(3);
		assertEquals(tu1, part.getParent());
		assertEquals(locDE, part.getLocale());
		
		part = parts.get(7);
		assertEquals(tu2, part.getParent());
		assertNull(part.getLocale());
		
		part = parts.get(9);
		assertEquals(tu2, part.getParent());
		assertEquals(locDE, part.getLocale());
				
		List<Event> events = TestUtil.createStartEvents(true, gsw, encMgt);
		events.add(new Event(EventType.TEXT_UNIT, tu1));
		events.add(new Event(EventType.TEXT_UNIT, tu2));
		TestUtil.addEndEvents(events);
		events = processEvents(events, true, locDE);
		
		String expected = "{0><b>Source segment 1</b><}10{>Target segment 1<0} " +
				"{0>Source segment 22<}90{>Target segment 22.1" +
				"{0><b>Source segment 1</b><}10{>Target segment 1<0} {0>Source segment 2<}90{>Target segment 2<0}" + // tu1 ref
				"Target segment 22.2<0}";
		String result = FilterTestDriver.generateOutput(events, locDE, gsw, encMgt, false);
		assertEquals(expected, result);
	}

	protected Event processEvent(Event event, GenericSkeletonSimplifier rs) {
		return event;
	}
	
	private List<Event> processEvents(List<Event> events, boolean isMultilingual, LocaleId trgLoc) {
		GenericSkeletonSimplifier rs = new GenericSkeletonSimplifier(isMultilingual, null, trgLoc);
		List<Event> newEvents = new ArrayList<Event>();
		for (Event event : events) {
			Event me = processEvent(event, rs);
			if (me.isMultiEvent()) {
				for (Event e : me.getMultiEvent()) {
					newEvents.add(e);
				}
			} else {
				newEvents.add(me);
			}
		}
		return newEvents;
	}
}
