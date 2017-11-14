package net.sf.okapi.steps.rainbowkit.ontram;

import net.sf.okapi.common.Event;
import net.sf.okapi.filters.xini.rainbowkit.XINIRainbowkitWriter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;
import org.powermock.reflect.Whitebox;

@RunWith(JUnit4.class)
public class OntramPackageWriterTest {

	private OntramPackageWriter ontramPackageWriter;
	private XINIRainbowkitWriter writerMock;

	@Before
	public void prepareMethod() {
		ontramPackageWriter = new OntramPackageWriter();
		writerMock = Mockito.mock(XINIRainbowkitWriter.class);
		Whitebox.setInternalState(ontramPackageWriter, XINIRainbowkitWriter.class, writerMock);
	}

	@Test
	public void startGroupIsHandled() {
		Event eventMock = Mockito.mock(Event.class);
		ontramPackageWriter.processStartGroup(eventMock);
		Mockito.verify(writerMock).handleEvent(eventMock);
	}

	@Test
	public void endGroupIsHandeled() {
		Event eventMock = Mockito.mock(Event.class);
		ontramPackageWriter.processEndGroup(eventMock);
		Mockito.verify(writerMock).handleEvent(eventMock);
	}
}
