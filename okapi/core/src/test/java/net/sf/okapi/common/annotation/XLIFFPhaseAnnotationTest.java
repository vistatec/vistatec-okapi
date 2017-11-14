package net.sf.okapi.common.annotation;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;

@RunWith(JUnit4.class)
public class XLIFFPhaseAnnotationTest {
	private XLIFFPhaseAnnotation anno;

	@Before
	public void setup() {
		anno = new XLIFFPhaseAnnotation();
		anno.add(new XLIFFPhase("A", "Process 1"));
		anno.add(new XLIFFPhase("B", "Process 2"));
		anno.add(new XLIFFPhase("C", "Process 3"));
		anno.add(new XLIFFPhase("D", "Process 4"));
	}

	@Test
	public void testPreserveOrdering() throws Exception {
		final String EXPECTED =
			"<phase-group>" +
				"<phase phase-name=\"A\" process-name=\"Process 1\"/>" +
				"<phase phase-name=\"B\" process-name=\"Process 2\"/>" +
				"<phase phase-name=\"C\" process-name=\"Process 3\"/>" +
				"<phase phase-name=\"D\" process-name=\"Process 4\"/>" +
			"</phase-group>";
		assertXMLEqual(EXPECTED, anno.toXML());
	}

	@Test
	public void testLookup() {
		XLIFFPhase phase = anno.get("B");
		assertNotNull(phase);
		assertEquals("B", phase.getPhaseName());
		assertEquals("Process 2", phase.getProcessName());
	}
}
