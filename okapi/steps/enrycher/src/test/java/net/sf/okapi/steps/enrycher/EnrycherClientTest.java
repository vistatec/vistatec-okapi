package net.sf.okapi.steps.enrycher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.LinkedList;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.annotation.GenericAnnotationType;
import net.sf.okapi.common.filterwriter.GenericContent;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.common.resource.TextUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class EnrycherClientTest {

	private GenericContent fmt = new GenericContent();
	
	@Test
	public void parametersTest () {
		EnrycherClient ec = new EnrycherClient();
		assertNotNull(ec.getParameters());
	}
	
	@Test
	public void testList () {
		LinkedList<ITextUnit> list = new LinkedList<ITextUnit>();
		// TU 1: 1 segment
		list.add(new TextUnit("1", "The Lake Bled. "));
		// TU 2: 2 segments
		ITextUnit tu = new TextUnit("2");
		TextContainer tc = tu.getSource();
		tc.append(new Segment("s1", new TextFragment("The lake Bled.")));
		tc.append(new Segment("s2", new TextFragment("The lake bled.")));
		list.add(tu);
		// TU 3: 1 segment with inline codes
		tu = new TextUnit("3");
		TextFragment tf = new TextFragment();
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append("Lake Bled");
		tf.append(TagType.CLOSING, "b", "</b>");
		tf.append(TagType.PLACEHOLDER, "br", "<br>");
		tu.setSourceContent(tf);
		list.add(tu);
		
		EnrycherClient ec = new EnrycherClient();
		ec.setLocale(LocaleId.ENGLISH);

		//ec.processList(list);
		
		// Note: has extra whitespace in segment 1_0
		String fromEc = "<div\nits-annotators-ref=\"text-analysis|http://enrycher.ijs.si/mlw/toolinfo.xml#enrycher\">\n"
			+ "<p\nid='1_0'>The\n\n    <span\nits-ta-ident-ref=\"http://dbpedia.org/resource/Lake_Bled\"\nits-ta-class-ref=\"http://schema.org/Place\">Lake Bled</span>.</p>\n"
			+ "<p\nid='2_0'>The <span\nits-ta-ident-ref=\"http://dbpedia.org/resource/Lake_Bled\"\nits-ta-class-ref=\"http://schema.org/Place\">lake Bled</span>.</p>\n"
			+ "<p\nid='2_1'>The lake bled.</p>\n"
			+ "<p\nid='3_0'><u\nid='1'><span\nits-ta-ident-ref=\"http://dbpedia.org/resource/Lake_Bled\"\nits-ta-class-ref=\"http://schema.org/Place\">Lake Bled</span></u><br\nid='2'>\n</p>\n"
			+ "</div>\n";
		
		ec.parseHTML(fromEc, list);
		
		tf = list.get(0).getSource().getFirstContent();
		Code code = tf.getCode(0);
		assertEquals("REF:http://dbpedia.org/resource/Lake_Bled",
			code.getGenericAnnotationString(GenericAnnotationType.TA, GenericAnnotationType.TA_IDENT));
		assertEquals("REF:http://schema.org/Place",
			code.getGenericAnnotationString(GenericAnnotationType.TA, GenericAnnotationType.TA_CLASS));
		assertEquals("The <1>Lake Bled</1>.", fmt.setContent(tf).toString());
		
		tf = list.get(1).getSource().getSegments().get(0).getContent();
		code = tf.getCode(0);
		assertEquals("REF:http://dbpedia.org/resource/Lake_Bled",
			code.getGenericAnnotationString(GenericAnnotationType.TA, GenericAnnotationType.TA_IDENT));
		assertEquals("REF:http://schema.org/Place",
			code.getGenericAnnotationString(GenericAnnotationType.TA, GenericAnnotationType.TA_CLASS));
		assertEquals("The <1>lake Bled</1>.", fmt.setContent(tf).toString());
		
		tf = list.get(1).getSource().getSegments().get(1).getContent();
		assertFalse(tf.hasCode());
		
		tf = list.get(2).getSource().getFirstContent();
		assertEquals(3, tf.getCodes().size());
		code = tf.getCode(0);
		assertEquals("<b>", code.getData()); // Annotation attached to existing code
		assertEquals("REF:http://dbpedia.org/resource/Lake_Bled",
			code.getGenericAnnotationString(GenericAnnotationType.TA, GenericAnnotationType.TA_IDENT));
		assertEquals("REF:http://schema.org/Place",
			code.getGenericAnnotationString(GenericAnnotationType.TA, GenericAnnotationType.TA_CLASS));
		assertEquals("<1>Lake Bled</1><2/>", fmt.setContent(tf).toString());
	}

	@Test
	public void testNestedSpans () {
		LinkedList<ITextUnit> list = new LinkedList<ITextUnit>();
		// TU 1: 1 segment
		list.add(new TextUnit("1", "aaa bbb ccc ddd"));
		EnrycherClient ec = new EnrycherClient();
		ec.setLocale(LocaleId.ENGLISH);
		// Annotated result
		String fromEc = "<div>\n"
			+ "<p\nid='1_0'>\n"
			+ "<span\n"
			+ "its-ta-ident-ref=\"ident1\"\n"
			+ "its-ta-class-ref=\"class1\">aaa "
			+ "<span\n"
			+ "its-ta-ident-ref=\"ident2\"\n"
			+ "its-ta-class-ref=\"class2\">bbb</span> "
			+ "<span its-ta-ident-ref='ident3' its-ta-class-ref='class3'>ccc</span></span> "
			+ "<span\n"
			+ "its-ta-ident-ref=\"ident4\"\n"
			+ "its-ta-class-ref=\"class4\">ddd</span> </p>\n"
			+ "</div>\n";
		ec.parseHTML(fromEc, list);

		TextFragment tf = list.get(0).getSource().getFirstContent();
		assertEquals("<3>aaa <1>bbb</1> <2>ccc</2></3> <4>ddd</4>", fmt.setContent(tf).toString());
		Code code = tf.getCode(0);
		assertEquals("REF:ident2",
			code.getGenericAnnotationString(GenericAnnotationType.TA, GenericAnnotationType.TA_IDENT));
		assertEquals("REF:class2",
			code.getGenericAnnotationString(GenericAnnotationType.TA, GenericAnnotationType.TA_CLASS));
		code = tf.getCode(2);
		assertEquals("REF:ident3",
			code.getGenericAnnotationString(GenericAnnotationType.TA, GenericAnnotationType.TA_IDENT));
		assertEquals("REF:class3",
			code.getGenericAnnotationString(GenericAnnotationType.TA, GenericAnnotationType.TA_CLASS));
		code = tf.getCode(4);
		assertEquals("REF:ident1",
			code.getGenericAnnotationString(GenericAnnotationType.TA, GenericAnnotationType.TA_IDENT));
		assertEquals("REF:class1",
			code.getGenericAnnotationString(GenericAnnotationType.TA, GenericAnnotationType.TA_CLASS));
		code = tf.getCode(6);
		assertEquals("REF:ident4",
			code.getGenericAnnotationString(GenericAnnotationType.TA, GenericAnnotationType.TA_IDENT));
		assertEquals("REF:class4",
			code.getGenericAnnotationString(GenericAnnotationType.TA, GenericAnnotationType.TA_CLASS));
	}

}
