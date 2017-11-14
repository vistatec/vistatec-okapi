package net.sf.okapi.common.filters;

import static org.junit.Assert.assertEquals;
import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class FilterEventTest {

	@Test
	public void testGenericEventTypes() {	
		Event event = new Event(EventType.END_DOCUMENT, null);
		assertEquals(EventType.END_DOCUMENT, event.getEventType());		
	}
}
