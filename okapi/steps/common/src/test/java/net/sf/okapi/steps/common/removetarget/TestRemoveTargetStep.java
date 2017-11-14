package net.sf.okapi.steps.common.removetarget;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestRemoveTargetStep {
	
	@Test
	public void testRemoveInAll() {
		RemoveTargetStep rts = new RemoveTargetStep();
		
		TextUnit tu1 = new TextUnit("tu1");
		tu1.setSourceContent(new TextFragment("Source"));
		tu1.setTargetContent(LocaleId.GERMAN, new TextFragment("target de-de"));
		tu1.setTargetContent(LocaleId.FRENCH, new TextFragment("target fr-fr"));
		tu1.setTargetContent(LocaleId.ITALIAN, new TextFragment("target it-it"));
		
		Event sbe = new Event(EventType.START_BATCH); 
		Event tue = new Event(EventType.TEXT_UNIT, tu1);

		assertNotNull(tu1.getTarget(LocaleId.GERMAN));
		assertFalse(tu1.getTarget(LocaleId.GERMAN).isEmpty());
		
		assertNotNull(tu1.getTarget(LocaleId.FRENCH));
		assertFalse(tu1.getTarget(LocaleId.FRENCH).isEmpty());
		
		assertNotNull(tu1.getTarget(LocaleId.ITALIAN));
		assertFalse(tu1.getTarget(LocaleId.ITALIAN).isEmpty());
		
		rts.handleEvent(sbe);
		rts.handleEvent(tue);
		
		assertTrue(tu1.getTarget(LocaleId.GERMAN)==null);
		
		assertTrue(tu1.getTarget(LocaleId.FRENCH)==null);
		
		assertTrue(tu1.getTarget(LocaleId.ITALIAN)==null);
	}
	
	@Test
	public void testRemoveInSelected() {
		RemoveTargetStep rts = new RemoveTargetStep();
		
		TextUnit tu1 = new TextUnit("tu1");
		tu1.setSourceContent(new TextFragment("Source"));
		tu1.setTargetContent(LocaleId.GERMAN, new TextFragment("target de-de"));
		tu1.setTargetContent(LocaleId.FRENCH, new TextFragment("target fr-fr"));
		tu1.setTargetContent(LocaleId.ITALIAN, new TextFragment("target it-it"));
		
		TextUnit tu2 = new TextUnit("tu2");
		tu2.setSourceContent(new TextFragment("Source"));
		tu2.setTargetContent(LocaleId.GERMAN, new TextFragment("target de-de"));
		tu2.setTargetContent(LocaleId.FRENCH, new TextFragment("target fr-fr"));
		tu2.setTargetContent(LocaleId.ITALIAN, new TextFragment("target it-it"));
		
		TextUnit tu3 = new TextUnit("tu3");
		tu3.setSourceContent(new TextFragment("Source"));
		tu3.setTargetContent(LocaleId.GERMAN, new TextFragment("target de-de"));
		tu3.setTargetContent(LocaleId.FRENCH, new TextFragment("target fr-fr"));
		tu3.setTargetContent(LocaleId.ITALIAN, new TextFragment("target it-it"));
		
		Event sbe = new Event(EventType.START_BATCH); 
		Parameters params = rts.getParameters();
		params.setTusForTargetRemoval("tu1, tu3");
		
		Event tue1 = new Event(EventType.TEXT_UNIT, tu1);
		Event tue2 = new Event(EventType.TEXT_UNIT, tu2);
		Event tue3 = new Event(EventType.TEXT_UNIT, tu3);

		assertNotNull(tu1.getTarget(LocaleId.GERMAN));
		assertFalse(tu1.getTarget(LocaleId.GERMAN).isEmpty());
		
		assertNotNull(tu1.getTarget(LocaleId.FRENCH));
		assertFalse(tu1.getTarget(LocaleId.FRENCH).isEmpty());
		
		assertNotNull(tu1.getTarget(LocaleId.ITALIAN));
		assertFalse(tu1.getTarget(LocaleId.ITALIAN).isEmpty());
		
		rts.handleEvent(sbe);
		rts.handleEvent(tue1);
		rts.handleEvent(tue2);
		rts.handleEvent(tue3);
		
		// tu1
		assertTrue(tu1.getTarget(LocaleId.GERMAN)==null);
		assertTrue(tu1.getTarget(LocaleId.FRENCH)==null);
		assertTrue(tu1.getTarget(LocaleId.ITALIAN)==null);
		
		// tu2
		assertNotNull(tu2.getTarget(LocaleId.GERMAN));
		assertFalse(tu2.getTarget(LocaleId.GERMAN).isEmpty());
		
		assertNotNull(tu2.getTarget(LocaleId.FRENCH));
		assertFalse(tu2.getTarget(LocaleId.FRENCH).isEmpty());
		
		assertNotNull(tu2.getTarget(LocaleId.ITALIAN));
		assertFalse(tu2.getTarget(LocaleId.ITALIAN).isEmpty());
		
		// tu3
		assertTrue(tu3.getTarget(LocaleId.GERMAN)==null);
		assertTrue(tu3.getTarget(LocaleId.FRENCH)==null);
		assertTrue(tu3.getTarget(LocaleId.ITALIAN)==null);
	}
}
