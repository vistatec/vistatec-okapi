package net.sf.okapi.filters.xini.rainbowkit;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.resource.StartGroup;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;

@RunWith(JUnit4.class)
public class XINIRainbowkitWriterTest {

	private XINIRainbowkitWriter writerUnderTest;

	@Before
	public void beforeMethod() {
		writerUnderTest = PowerMockito.spy(new XINIRainbowkitWriter());
	}

	@Test
	public void writerUnderTestSavesGroupProperties() {
		Event event = new Event(EventType.START_GROUP);
		StartGroup startGroup = new StartGroup("1");
		event.setResource(startGroup);
		writerUnderTest.handleEvent(event);

		Mockito.verify(writerUnderTest).pushGroupToStack(startGroup);
	}

	@Test
	public void writerUnderTestDeletesGroupValueWhenHandlingEndGroupEvent() {
		Event event = new Event(EventType.END_GROUP);
		writerUnderTest.handleEvent(event);
		Mockito.verify(writerUnderTest).popGroupFromStack();
	}
}
