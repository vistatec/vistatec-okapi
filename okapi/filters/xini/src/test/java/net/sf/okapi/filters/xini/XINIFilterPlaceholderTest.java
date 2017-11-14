package net.sf.okapi.filters.xini;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.filters.FilterTestDriver;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.filters.xini.jaxb.Field;
import net.sf.okapi.filters.xini.jaxb.Xini;

@RunWith(JUnit4.class)
public class XINIFilterPlaceholderTest {

	private static XINIFilterTestHelper helper;

	@BeforeClass
	public static void prepare(){
		helper = new XINIFilterTestHelper();
	}

	@Test
	public void placeholdersBecomeCodes() {
		String snippet = helper.getStartSnippet() +
							"	<Fields>" +
							"		<Field FieldID=\"0\">" +
							"			<Seg SegID=\"0\"><ph ID=\"1\"><ph ID=\"2\" type=\"link\">a</ph>b</ph>cc<ph ID=\"3\"/></Seg>" +
							"		</Field>" +
							"	</Fields>" +
							helper.getEndSnippet();

		//tu Content: phphabc
		ITextUnit tu = FilterTestDriver.getTextUnit(helper.toEvents(snippet), 1);
		TextContainer tuSrc = tu.getSource();
		assertTrue(tuSrc.contentIsOneSegment());
		TextFragment tf = tuSrc.getFirstContent();

		List<Code> codes = tf.getClonedCodes();
		assertEquals(TagType.OPENING, codes.get(0).getTagType());
		assertEquals(1, codes.get(0).getId());

		assertEquals(TagType.OPENING, codes.get(1).getTagType());
		assertEquals(2, codes.get(1).getId());

		assertEquals(TagType.CLOSING, codes.get(2).getTagType());
		assertEquals(2, codes.get(2).getId());

		assertEquals(TagType.CLOSING, codes.get(3).getTagType());
		assertEquals(1, codes.get(3).getId());

		assertEquals(TagType.PLACEHOLDER, codes.get(4).getTagType());
		assertEquals(3, codes.get(4).getId());
	}

	@Test
	public void isolatedPlaceholdersBecomeCodes() {
		String snippet = helper.getStartSnippet() +
							"	<Fields>" +
							"		<Field FieldID=\"0\">" +
							"			<Seg SegID=\"0\">"+
							"				Inline placeholders <sph ID=\"1\" type=\"style\"/> must become codes<sph type=\"ph\" ID=\"2\"/>." +
							"				Has to work<eph ID=\"2\"/> with various types.<eph ID=\"1\" type=\"style\"/>" +
							"			</Seg>" +
							"		</Field>" +
							"	</Fields>" +
						helper.getEndSnippet();

		ITextUnit tu1 = FilterTestDriver.getTextUnit(helper.toEvents(snippet), 1);
		TextContainer tuSrc = tu1.getSource();
		assertTrue(tuSrc.contentIsOneSegment());
		TextFragment tf = tuSrc.getFirstContent();

		List<Code> codes = tf.getClonedCodes();

		assertEquals(TagType.OPENING, codes.get(0).getTagType());
		assertEquals(1, codes.get(0).getId());

		assertEquals(TagType.OPENING, codes.get(1).getTagType());
		assertEquals(2, codes.get(1).getId());

		assertEquals(TagType.CLOSING, codes.get(2).getTagType());
		assertEquals(2, codes.get(2).getId());
		assertEquals(TagType.CLOSING, codes.get(3).getTagType());
		assertEquals(1, codes.get(3).getId());
	}

	@Test
	public void placeholdersBecomePreserved() throws Exception {
		String snippet = helper.getStartSnippet() +
							"	<Fields>" +
							"		<Field FieldID=\"0\">" +
							"			<Seg SegID=\"0\">Sentence <sph ID=\"1\" type=\"style\"/> one. <sph type=\"ph\" ID=\"2\"/>Two.</Seg>" +
							"			<Seg SegID=\"1\">Three <eph ID=\"2\"/></Seg>" +
							"           <Seg SegID=\"2\">Four <eph ID=\"1\" type=\"link\"/></Seg>" +
							"			<Seg SegID=\"3\">A line <br/> break</Seg>" +
				"			<Seg SegID=\"4\"><ph ID=\"3\"><ph ID=\"4\">a</ph>b</ph>cc</Seg>" +
							"		</Field>" +
							"	</Fields>" +
							helper.getEndSnippet();

		List<Event> eventsSnippet = helper.toEvents(snippet);
		Xini xini = helper.toXini(eventsSnippet);
		List<Field> field = helper.getFieldsByPageIdAndElementId(xini, 1, 10);

		String segContent = helper.getSegContentBySegId(field.get(0), 0);
		helper.assertXMLEqual("Sentence <sph ID=\"1\" type=\"style\"/> one. <sph type=\"ph\" ID=\"2\"/>Two.", segContent);

		segContent = helper.getSegContentBySegId(field.get(0), 1);
		helper.assertXMLEqual("Three <eph ID=\"2\"/>", segContent);

		segContent = helper.getSegContentBySegId(field.get(0), 2);
		helper.assertXMLEqual("Four <eph ID=\"1\" type=\"link\"/>", segContent);

		segContent = helper.getSegContentBySegId(field.get(0), 3);
		helper.assertXMLEqual("A line <br/> break", segContent);

		segContent = helper.getSegContentBySegId(field.get(0), 4);
		assertEquals("<ph ID=\"3\" type=\"ph\"><ph ID=\"4\" type=\"ph\">a</ph>b</ph>cc", segContent);
	}

	@Test
	public void phTypeMemory100Preserved() throws Exception {
		String snippet = preparePhTypeTest("memory100");
		String segContent = doStringToEventsAndEventsToXiniForPhTypeTest(snippet);
		helper.assertXMLEqual("<ph type=\"memory100\" ID=\"1\">test content</ph>", segContent);
	}

	@Test
	public void phTypeUpdatedPreserved() throws Exception {
		String snippet = preparePhTypeTest("updated");
		String segContent = doStringToEventsAndEventsToXiniForPhTypeTest(snippet);
		helper.assertXMLEqual("<ph type=\"updated\" ID=\"1\">test content</ph>", segContent);
	}

	@Test
	public void phTypeInsertedPreserved() throws Exception {
		String snippet = preparePhTypeTest("inserted");
		String segContent = doStringToEventsAndEventsToXiniForPhTypeTest(snippet);
		helper.assertXMLEqual("<ph type=\"inserted\" ID=\"1\">test content</ph>", segContent);
	}

	@Test
	public void phTypeDeletedPreserved() throws Exception {
		String snippet = preparePhTypeTest("deleted");
		String segContent = doStringToEventsAndEventsToXiniForPhTypeTest(snippet);
		helper.assertXMLEqual("<ph type=\"deleted\" ID=\"1\">test content</ph>", segContent);
	}

	private String doStringToEventsAndEventsToXiniForPhTypeTest(String snippet){
		Xini xini = helper.toXini(helper.toEvents(snippet));
		List<Field> field = helper.getFieldsByPageIdAndElementId(xini, 1, 10);
		String segContent = helper.getSegContentBySegId(field.get(0), 0);
		return segContent;
	}

	private String preparePhTypeTest(String phType){
		String snippet = helper.getStartSnippet() +
		"<Fields>" +
			"<Field FieldID=\"0\">" +
				"<Seg SegID=\"0\">" +
					"<ph ID=\"1\" type=\"" + phType + "\">test content</ph>" +
				"</Seg>" +
			"</Field>" +
		"</Fields>" +
		helper.getEndSnippet();
		return snippet;
	}

}
