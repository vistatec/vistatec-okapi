package net.sf.okapi.filters.xini;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import net.sf.okapi.common.filters.FilterTestDriver;
import net.sf.okapi.common.filterwriter.GenericContent;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class XINIFilterReaderTest {

	private static XINIFilterTestHelper helper;
	private static GenericContent fmt;

	@BeforeClass
	public static void initialize(){
		helper = new XINIFilterTestHelper();
		fmt = new GenericContent();
	}

	@Test
	public void segmentBecomesTU () {
		String snippet = helper.getStartSnippet() +
							"	<Fields>" +
							"		<Field FieldID=\"0\"><Seg SegID=\"0\">tester</Seg></Field>" +
							"	</Fields>" +
							helper.getEndSnippet();

		ITextUnit tu = FilterTestDriver.getTextUnit(helper.toEvents(snippet), 1);
		assertNotNull(tu);
		TextContainer tuSrc = tu.getSource();
		assertTrue(tuSrc.contentIsOneSegment());

		TextFragment tf = tuSrc.getFirstContent();
		String firstTextFragmentContent = tf.getCodedText();
		assertEquals("tester", firstTextFragmentContent);
		List<Code> codes = tf.getCodes();
		assertTrue(codes.isEmpty());

		List<Segment> segments = tuSrc.getSegments().asList();
		assertEquals(1, segments.size());

		Segment segment = segments.get(0);
		assertEquals("0", segment.id);
		assertEquals("tester", segment.text.toText());
	}

	@Test
	public void segmentsAreGroupedInTUsByOriginalSegmentId() {
		String snippet = helper.getStartSnippet() +
							"	<Fields>" +
							"		<Field FieldID=\"0\">" +
							"			<Seg SegID=\"0\" SegmentIDBeforeSegmentation=\"0\">t1.</Seg>" +
							"			<Seg SegID=\"1\" SegmentIDBeforeSegmentation=\"0\">t2.</Seg>" +
							"			<Seg SegID=\"2\" SegmentIDBeforeSegmentation=\"1\">t3.</Seg>" +
							"		</Field>" +
							"	</Fields>" +
							helper.getEndSnippet();

		ITextUnit tu1 = FilterTestDriver.getTextUnit(helper.toEvents(snippet), 1);
		TextContainer tuSrc1 = tu1.getSource();
		assertFalse(tuSrc1.contentIsOneSegment());

		List<Segment> segments1 = tuSrc1.getSegments().asList();
		assertTrue(segments1.size() == 2);
		assertEquals("[t1.][t2.]", fmt.printSegmentedContent(tuSrc1, true));
		assertEquals("0", segments1.get(0).id);
		assertEquals("t1.", segments1.get(0).text.toText());
		assertEquals("1", segments1.get(1).id);
		assertEquals("t2.", segments1.get(1).text.toText());

		ITextUnit tu2 = FilterTestDriver.getTextUnit(helper.toEvents(snippet), 2);
		TextContainer tuSrc2 = tu2.getSource();
		assertTrue(tuSrc2.contentIsOneSegment());

		List<Segment> segments2 = tuSrc2.getSegments().asList();
		assertTrue(segments2.size() == 1);
		assertEquals("[t3.]", fmt.printSegmentedContent(tuSrc2, true));
		assertEquals("0", segments2.get(0).id);
		assertEquals("t3.", segments2.get(0).text.toText());
	}

}
