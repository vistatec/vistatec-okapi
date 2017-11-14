package net.sf.okapi.filters.xini;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import net.sf.okapi.common.filters.FilterTestDriver;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.filters.xini.jaxb.Field;
import net.sf.okapi.filters.xini.jaxb.Xini;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class XINIFilterFormattingTest {

	private static XINIFilterTestHelper helper;

	@BeforeClass
	public static void initialize(){
		helper = new XINIFilterTestHelper();
	}

	@Test
	public void tagsBecomeCodes() {
		String snippet = helper.getStartSnippet() +
							"	<Fields>" +
							"		<Field FieldID=\"0\">" +
							"			<Seg SegID=\"0\"><b><i>a<sup>bb</sup>ccc</i>d</b></Seg>" +
							"			<Seg SegID=\"1\">e<sub>ff<br/>ggg</sub>hh<u>jj</u>k</Seg>" +
							"		</Field>" +
							"	</Fields>" +
							helper.getEndSnippet();

		ITextUnit tu1 = FilterTestDriver.getTextUnit(helper.toEvents(snippet), 1);
		TextContainer tuSrc1 = tu1.getSource();
		assertTrue(tuSrc1.contentIsOneSegment());
		TextFragment tf = tuSrc1.getFirstContent();
		assertEquals("abbcccd", tf.toText());

		List<Code> codes = tf.getClonedCodes();

		assertEquals(TagType.OPENING, codes.get(0).getTagType());
		assertEquals(Code.TYPE_BOLD, codes.get(0).getType());

		assertEquals(TagType.OPENING, codes.get(1).getTagType());
		assertEquals(Code.TYPE_ITALIC, codes.get(1).getType());

		assertEquals(TagType.OPENING, codes.get(2).getTagType());
		assertEquals("superscript", codes.get(2).getType());

		assertEquals(TagType.CLOSING, codes.get(3).getTagType());
		assertEquals("superscript", codes.get(3).getType());

		assertEquals(TagType.CLOSING, codes.get(4).getTagType());
		assertEquals(Code.TYPE_ITALIC, codes.get(4).getType());

		assertEquals(TagType.CLOSING, codes.get(5).getTagType());
		assertEquals(Code.TYPE_BOLD, codes.get(5).getType());

		ITextUnit tu2 = FilterTestDriver.getTextUnit(helper.toEvents(snippet), 2);
		TextContainer tuSrc2 = tu2.getSource();
		tf = tuSrc2.getFirstContent();
		assertEquals("effggghhjjk", tf.toText());

		codes = tf.getClonedCodes();

		assertEquals(TagType.OPENING, codes.get(0).getTagType());
		assertEquals("subscript", codes.get(0).getType());

		assertEquals(TagType.PLACEHOLDER, codes.get(1).getTagType());
		assertEquals(Code.TYPE_LB, codes.get(1).getType());

		assertEquals(TagType.CLOSING, codes.get(2).getTagType());
		assertEquals("subscript", codes.get(2).getType());

		assertEquals(TagType.OPENING, codes.get(3).getTagType());
		assertEquals(Code.TYPE_UNDERLINED, codes.get(3).getType());

		assertEquals(TagType.CLOSING, codes.get(4).getTagType());
		assertEquals(Code.TYPE_UNDERLINED, codes.get(4).getType());
	}

	@Test
	public void formattingsBecomePreserved() {
		String snippet = helper.getStartSnippet() +
							"	<Fields>" +
							"		<Field FieldID=\"0\">" +
							"			<Seg SegID=\"0\"><b><i>a<sup>bb</sup>ccc</i>d</b></Seg>" +
							"			<Seg SegID=\"1\">e<sub>ff<br/>ggg</sub>hh<u>jj</u>k</Seg>" +
							"		</Field>" +
							"	</Fields>" +
							helper.getEndSnippet();

		Xini xini = helper.toXini(helper.toEvents(snippet));
		List<Field> field = helper.getFieldsByPageIdAndElementId(xini, 1, 10);

		String segContent = helper.getSegContentBySegId(field.get(0), 0);
		assertEquals("<b><i>a<sup>bb</sup>ccc</i>d</b>", segContent);

		segContent = helper.getSegContentBySegId(field.get(0), 1);
		assertEquals("e<sub>ff<br/>ggg</sub>hh<u>jj</u>k", segContent);
	}

}
