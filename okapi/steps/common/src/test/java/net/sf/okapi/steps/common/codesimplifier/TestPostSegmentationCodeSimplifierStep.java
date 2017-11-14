package net.sf.okapi.steps.common.codesimplifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.URISyntaxException;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.ISegmenter;
import net.sf.okapi.common.ISkeleton;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filterwriter.GenericContent;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.common.resource.TextUnit;
import net.sf.okapi.common.skeleton.GenericSkeleton;
import net.sf.okapi.lib.segmentation.LanguageMap;
import net.sf.okapi.lib.segmentation.Rule;
import net.sf.okapi.lib.segmentation.SRXDocument;

@RunWith(JUnit4.class)
public class TestPostSegmentationCodeSimplifierStep {
	private GenericContent fmt;
	private PostSegmentationCodeSimplifierStep css;
	private static final LocaleId EN = new LocaleId("en", "us");
	private ISegmenter segmenter;

	@Before
	public void setup() throws URISyntaxException {
		css = new PostSegmentationCodeSimplifierStep();
		fmt = new GenericContent();
		segmenter = createSegmenterWithRules(EN);
	}

	@Test
	public void testDefaults() {
		TextFragment tf = new TextFragment();
		tf.append(TagType.PLACEHOLDER, "x1", "<x1/>");
		tf.append("   ");
		tf.append(TagType.PLACEHOLDER, "x2", "<x2/>");
		tf.append("T1");
		tf.append(TagType.PLACEHOLDER, "x3", "<x3/>");
		tf.append(TagType.PLACEHOLDER, "x4", "<x4/>");
		tf.append(TagType.OPENING, "a", "<a>");
		tf.append("T2");
		tf.append("   ");
		tf.append(TagType.CLOSING, "a", "</a>");
		tf.append("   ");
		tf.append(TagType.CLOSING, "b", "</b>");
		tf.append(TagType.PLACEHOLDER, "x5", "<x5/>");
		tf.append("   ");
		tf.append(TagType.PLACEHOLDER, "x6", "<x6/>");

		assertEquals("<1/>   <2/>T1<3/><4/><5>T2   </5>   <e8/><6/>   <7/>", fmt.setContent(tf).toString());

		TextUnit tu1 = new TextUnit("tu1");
		tu1.setSourceContent(tf);
		tu1.createSourceSegmentation(segmenter);

		ISkeleton skel = tu1.getSkeleton();
		assertNull(skel);

		Event tue1 = new Event(EventType.TEXT_UNIT, tu1);
		css.handleEvent(tue1);
		tf = tu1.getSource().getUnSegmentedContentCopy();
		// remember that trimmed codes are now expanded so when all parts are joined
		// only the "inside segment" codes are merged.
//		assertEquals("<1/>   <2/>T1<3>T2   </3>   <e7/><6/>   <4/>", fmt.setContent(tf).toString());
		assertEquals("<1/>   <2/>T1<5>T2   </5>   <e8/><6/>   <7/>", fmt.setContent(tf).toString());
		skel = tu1.getSkeleton();
		assertNull(skel);
	}

	@Test
	public void testDefaults2() {
		TextFragment tf = new TextFragment();
		tf.append(TagType.PLACEHOLDER, "x1", "<x1/>");
		tf.append("   ");
		tf.append(TagType.PLACEHOLDER, "x2", "<x2/>");
		tf.append("T1");
		tf.append(TagType.PLACEHOLDER, "x3", "<x3/>");
		tf.append(TagType.PLACEHOLDER, "x4", "<x4/>");
		tf.append(TagType.OPENING, "a", "<a>");
		tf.append("T2");
		tf.append("   ");
		tf.append(TagType.CLOSING, "a", "</a>");
		tf.append("   ");
		tf.append(TagType.CLOSING, "b", "</b>");
		tf.append(TagType.PLACEHOLDER, "x5", "<x5/>");
		tf.append("   ");
		tf.append(TagType.PLACEHOLDER, "x6", "<x6/>");

		assertEquals("<1/>   <2/>T1<3/><4/><5>T2   </5>   <e8/><6/>   <7/>", fmt.setContent(tf).toString());

		TextUnit tu1 = new TextUnit("tu1");
		tu1.setSourceContent(tf);
		tu1.createSourceSegmentation(segmenter);

		ISkeleton skel = new GenericSkeleton();
		assertNotNull(skel);
		tu1.setSkeleton(skel);
		assertNotNull(tu1.getSkeleton());

		Event tue1 = new Event(EventType.TEXT_UNIT, tu1);
		css.handleEvent(tue1);
		tf = tu1.getSource().getSegments().getFirstContent();
//		assertEquals("T1<3>T2   </3>", fmt.setContent(tf).toString());
		assertEquals("T1<5>T2   </5>", fmt.setContent(tf).toString());
		skel = tu1.getSkeleton();
		assertTrue(skel.toString().isEmpty());
	}

	@Test
	public void testNoRemoval() {
		Parameters params = (Parameters) css.getParameters();
		params.setRemoveLeadingTrailingCodes(false);

		TextFragment tf = new TextFragment();
		tf.append(TagType.PLACEHOLDER, "x1", "<x1/>");
		tf.append("   ");
		tf.append(TagType.PLACEHOLDER, "x2", "<x2/>");
		tf.append("T1");
		tf.append(TagType.PLACEHOLDER, "x3", "<x3/>");
		tf.append(TagType.PLACEHOLDER, "x4", "<x4/>");
		tf.append(TagType.OPENING, "a", "<a>");
		tf.append("T2");
		tf.append("   ");
		tf.append(TagType.CLOSING, "a", "</a>");
		tf.append("   ");
		tf.append(TagType.CLOSING, "b", "</b>");
		tf.append(TagType.PLACEHOLDER, "x5", "<x5/>");
		tf.append("   ");
		tf.append(TagType.PLACEHOLDER, "x6", "<x6/>");

		assertEquals("<1/>   <2/>T1<3/><4/><5>T2   </5>   <e8/><6/>   <7/>", fmt.setContent(tf).toString());

		TextUnit tu1 = new TextUnit("tu1");
		tu1.setSourceContent(tf);
		tu1.createSourceSegmentation(segmenter);

		ISkeleton skel = tu1.getSkeleton();
		assertNull(skel);

		Event tue1 = new Event(EventType.TEXT_UNIT, tu1);
		css.handleEvent(tue1);
		tf = tu1.getSource().getUnSegmentedContentCopy();
		// 1/ + 2/ -> 1/
		// 3/ + 4/ + 5 -> 2
		// /5 -> /2
		// e8/ + 6/ + 7/ -> e8/
//		assertEquals("<1/>   <2/>T1<3>T2   </3>   <e5/>   <4/>", fmt.setContent(tf).toString());
		assertEquals("<1/>   <2/>T1<5>T2   </5>   <e8/>   <7/>", fmt.setContent(tf).toString());

		skel = tu1.getSkeleton();
		assertNull(skel);
	}

	private ISegmenter createSegmenterWithRules(LocaleId locId) {
		SRXDocument doc = new SRXDocument();
		LanguageMap langMap = new LanguageMap(".*", "default");
		doc.addLanguageMap(langMap);
		// Add the rules
		ArrayList<Rule> langRules = new ArrayList<Rule>();
		langRules.add(new Rule("\\.", "\\s", true));
		// Add the ruls to the document
		doc.addLanguageRule("default", langRules);
		// Create the segmenter
		return doc.compileLanguageRules(locId, null);
	}
}
