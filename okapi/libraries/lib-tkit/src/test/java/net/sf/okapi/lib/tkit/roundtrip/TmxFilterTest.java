package net.sf.okapi.lib.tkit.roundtrip;

import static net.sf.okapi.lib.tkit.roundtrip.RoundTripUtils.roundTripSerilaizedEvents;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.TestUtil;
import net.sf.okapi.common.filters.FilterTestDriver;
import net.sf.okapi.common.filterwriter.GenericContent;
import net.sf.okapi.common.resource.AlignmentStatus;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.filters.tmx.Parameters;
import net.sf.okapi.filters.tmx.TmxFilter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TmxFilterTest {

	private TmxFilter filter;
	private FilterTestDriver testDriver;
	private LocaleId locEN = LocaleId.fromString("en");
	private LocaleId locFR = LocaleId.fromString("fr");
	private LocaleId locDE = LocaleId.fromString("de");
	private LocaleId locIT = LocaleId.fromString("it");
	private GenericContent fmt = new GenericContent();
	
	String simpleSnippetWithDTD = "<?xml version=\"1.0\"?>\r"
		+ "<!DOCTYPE tmx SYSTEM \"tmx14.dtd\"><tmx version=\"1.4\"><header creationtool=\"undefined_creationtool\" creationtoolversion=\"undefined_creationversion\" segtype=\"undefined_segtype\" o-tmf=\"undefined_unknown\" adminlang=\"undefined_adminlang\" srclang=\"en\" datatype=\"unknown\"></header><body><tu tuid=\"tuid_1\"><note>hello world note</note><tuv xml:lang=\"en\"><seg>Hello World!</seg></tuv></tu><tu tuid=\"tuid_2\"><tuv xml:lang=\"en\"><seg>Hello Universe!</seg></tuv></tu></body></tmx>\r";

	String simpleSnippet = "<?xml version=\"1.0\"?>\r"
		+ "<!-- document level comment --><tmx version=\"1.4\"><header creationtool=\"undefined_creationtool\" creationtoolversion=\"undefined_creationversion\" segtype=\"undefined_segtype\" o-tmf=\"undefined_unknown\" adminlang=\"undefined_adminlang\" srclang=\"en\" datatype=\"unknown\"></header><body><tu tuid=\"tuid_1\"><note>hello world note</note><tuv xml:lang=\"en\"><seg>Hello World!</seg></tuv></tu><tu tuid=\"tuid_1\"><tuv xml:lang=\"en\"><seg>Hello Universe!</seg></tuv></tu></body></tmx>\r";

	String simpleBilingualSnippet = "<?xml version=\"1.0\"?>\r"
		+ "<tmx version=\"1.4\"><header creationtool=\"undefined_creationtool\" creationtoolversion=\"undefined_creationversion\" segtype=\"undefined_segtype\" o-tmf=\"undefined_unknown\" adminlang=\"undefined_adminlang\" srclang=\"en\" datatype=\"unknown\"></header><body><tu tuid=\"tuid_1\"><tuv xml:lang=\"en\"><seg>Hello World!</seg></tuv><tuv xml:lang=\"fr\"><seg>Bonjour le monde!</seg></tuv></tu></body></tmx>\r";
	
	String simpleBilingualSnippetWithSentence = "<?xml version=\"1.0\"?>\r"
			+ "<tmx version=\"1.4\"><header creationtool=\"undefined_creationtool\" creationtoolversion=\"undefined_creationversion\" segtype=\"undefined_segtype\" o-tmf=\"undefined_unknown\" adminlang=\"undefined_adminlang\" srclang=\"en\" datatype=\"unknown\"></header><body><tu tuid=\"tuid_1\" segtype=\"sentence\"><tuv xml:lang=\"en\"><seg>Hello World!</seg></tuv><tuv xml:lang=\"fr\"><seg>Bonjour le monde!</seg></tuv></tu></body></tmx>\r";

	String simpleBilingualSnippetWithParagraph = "<?xml version=\"1.0\"?>\r"
			+ "<tmx version=\"1.4\"><header creationtool=\"undefined_creationtool\" creationtoolversion=\"undefined_creationversion\" segtype=\"undefined_segtype\" o-tmf=\"undefined_unknown\" adminlang=\"undefined_adminlang\" srclang=\"en\" datatype=\"unknown\"></header><body><tu tuid=\"tuid_1\" segtype=\"paragraph\"><tuv xml:lang=\"en\"><seg>Hello World!</seg></tuv><tuv xml:lang=\"fr\"><seg>Bonjour le monde!</seg></tuv></tu></body></tmx>\r";

	String simpleBilingualSnippetWithUnknown = "<?xml version=\"1.0\"?>\r"
			+ "<tmx version=\"1.4\"><header creationtool=\"undefined_creationtool\" creationtoolversion=\"undefined_creationversion\" segtype=\"undefined_segtype\" o-tmf=\"undefined_unknown\" adminlang=\"undefined_adminlang\" srclang=\"en\" datatype=\"unknown\"></header><body><tu tuid=\"tuid_1\" segtype=\"undefined\"><tuv xml:lang=\"en\"><seg>Hello World!</seg></tuv><tuv xml:lang=\"fr\"><seg>Bonjour le monde!</seg></tuv></tu></body></tmx>\r";

	String simpleBilingualSnippetHeaderSentence = "<?xml version=\"1.0\"?>\r"
			+ "<tmx version=\"1.4\"><header creationtool=\"undefined_creationtool\" creationtoolversion=\"undefined_creationversion\" segtype=\"sentence\" o-tmf=\"undefined_unknown\" adminlang=\"undefined_adminlang\" srclang=\"en\" datatype=\"unknown\"></header><body><tu tuid=\"tuid_1\"><tuv xml:lang=\"en\"><seg>Hello World!</seg></tuv><tuv xml:lang=\"fr\"><seg>Bonjour le monde!</seg></tuv></tu></body></tmx>\r";

	String simpleBilingualSnippetHeaderParagraph = "<?xml version=\"1.0\"?>\r"
			+ "<tmx version=\"1.4\"><header creationtool=\"undefined_creationtool\" creationtoolversion=\"undefined_creationversion\" segtype=\"paragraph\" o-tmf=\"undefined_unknown\" adminlang=\"undefined_adminlang\" srclang=\"en\" datatype=\"unknown\"></header><body><tu tuid=\"tuid_1\"><tuv xml:lang=\"en\"><seg>Hello World!</seg></tuv><tuv xml:lang=\"fr\"><seg>Bonjour le monde!</seg></tuv></tu></body></tmx>\r";	
	
	String biHeaderSentenceTuPara = "<?xml version=\"1.0\"?>\r"
			+ "<tmx version=\"1.4\"><header creationtool=\"undefined_creationtool\" creationtoolversion=\"undefined_creationversion\" segtype=\"sentence\" o-tmf=\"undefined_unknown\" adminlang=\"undefined_adminlang\" srclang=\"en\" datatype=\"unknown\"></header><body><tu tuid=\"tuid_1\" segtype=\"paragraph\"><tuv xml:lang=\"en\"><seg>Hello World!</seg></tuv><tuv xml:lang=\"fr\"><seg>Bonjour le monde!</seg></tuv></tu></body></tmx>\r";

	String biHeaderParaTuSentence = "<?xml version=\"1.0\"?>\r"
			+ "<tmx version=\"1.4\"><header creationtool=\"undefined_creationtool\" creationtoolversion=\"undefined_creationversion\" segtype=\"paragraph\" o-tmf=\"undefined_unknown\" adminlang=\"undefined_adminlang\" srclang=\"en\" datatype=\"unknown\"></header><body><tu tuid=\"tuid_1\" segtype=\"sentence\"><tuv xml:lang=\"en\"><seg>Hello World!</seg></tuv><tuv xml:lang=\"fr\"><seg>Bonjour le monde!</seg></tuv></tu></body></tmx>\r";	
	
	String tuMissingXmlLangSnippet = "<?xml version=\"1.0\"?>\r"
		+ "<tmx version=\"1.4\"><header creationtool=\"undefined_creationtool\" creationtoolversion=\"undefined_creationversion\" segtype=\"undefined_segtype\" o-tmf=\"undefined_unknown\" adminlang=\"undefined_adminlang\" srclang=\"en\" datatype=\"unknown\"></header><body><tu tuid=\"tuid_1\"><tuv><seg>Hello World!</seg></tuv></tu></body></tmx>\r";

	String invalidXmlSnippet = "<?xml version=\"1.0\"?>\r"
		+ "<tmx version=\"1.4\"><header creationtool=\"undefined_creationtool\" creationtoolversion=\"undefined_creationversion\" segtype=\"undefined_segtype\" o-tmf=\"undefined_unknown\" adminlang=\"undefined_adminlang\" srclang=\"en\" datatype=\"unknown\"></header><body><tu tuid=\"tuid_1\"><tuv xml:lang=\"en\"><seg>Hello World!</seg></tu></body></tmx>\r";

	String emptyTuSnippet = "<?xml version=\"1.0\"?>\r"
		+ "<tmx version=\"1.4\"><header creationtool=\"undefined_creationtool\" creationtoolversion=\"undefined_creationversion\" segtype=\"undefined_segtype\" o-tmf=\"undefined_unknown\" adminlang=\"undefined_adminlang\" srclang=\"en\" datatype=\"unknown\"></header><body><tu tuid=\"tuid_1\"></tu></body></tmx>\r";

	String invalidElementsInsideTuSnippet = "<?xml version=\"1.0\"?>\r"
		+ "<tmx version=\"1.4\"><header creationtool=\"undefined_creationtool\" creationtoolversion=\"undefined_creationversion\" segtype=\"undefined_segtype\" o-tmf=\"undefined_unknown\" adminlang=\"undefined_adminlang\" srclang=\"en\" datatype=\"unknown\"></header><body><tu tuid=\"tuid_1\"><tuv xml:lang=\"en\"><seg>Hello World!</seg></tuv></tu><tu tuid=\"tuid_1\"><tuv xml:lang=\"en\"><InvalidTag>Invalid Tag Content</InvalidTag><seg>Hello Universe!</seg></tuv></tu></body></tmx>\r";

	String invalidElementInsidePlaceholderSnippet = "<?xml version=\"1.0\"?>\r"
		+ "<!-- document level comment --><tmx version=\"1.4\"><header creationtool=\"undefined_creationtool\" creationtoolversion=\"undefined_creationversion\" segtype=\"undefined_segtype\" o-tmf=\"undefined_unknown\" adminlang=\"undefined_adminlang\" srclang=\"en\" datatype=\"unknown\"></header><body><tu tuid=\"tuid_1\"><note>hello world note</note><tuv xml:lang=\"en\"><seg>Hello World!</seg></tuv></tu><tu tuid=\"tuid_1\"><tuv xml:lang=\"en\"><seg>Hello <ph type=\"fnote\">Before Sub\"<sub>Hello Subflow. </sub>After <invalid> test invalid placeholder element </invalid> Sub</ph>Universe!</seg></tuv></tu></body></tmx>\r";
	
	String invalidElementInsideSubSnippet = "<?xml version=\"1.0\"?>\r"
		+ "<!-- document level comment --><tmx version=\"1.4\"><header creationtool=\"undefined_creationtool\" creationtoolversion=\"undefined_creationversion\" segtype=\"undefined_segtype\" o-tmf=\"undefined_unknown\" adminlang=\"undefined_adminlang\" srclang=\"en\" datatype=\"unknown\"></header><body><tu tuid=\"tuid_1\"><note>hello world note</note><tuv xml:lang=\"en\"><seg>Hello World!</seg></tuv></tu><tu tuid=\"tuid_1\"><tuv xml:lang=\"en\"><seg>Hello <ph type=\"fnote\">Before Sub\"<sub>Hello <invalid> test invalid sub element </invalid> Subflow. </sub>After Sub</ph>Universe!</seg></tuv></tu></body></tmx>\r";

	String multiTransSnippet = "<?xml version=\"1.0\"?>"
		+ "<tmx version=\"1.4\"><header creationtool=\"x\" creationtoolversion=\"1\" segtype=\"sentence\" o-tmf=\"x\" adminlang=\"en\" srclang=\"en\" datatype=\"plaintext\"></header><body><tu>"
		+ "<tuv xml:lang=\"en\"><seg>Hello</seg>s</tuv>"
		+ "<tuv xml:lang=\"fr\"><seg>Bonjour</seg></tuv>"
		+ "<tuv xml:lang=\"fr\"><seg>Salut</seg></tuv>"
		+ "<tuv xml:lang=\"de\"><seg>Hallo</seg></tuv>"
		+ "<tuv xml:lang=\"it\"><seg>Buongiorno</seg></tuv>"
		+ "</tu></body></tmx>\r";
	
	String utSnippetInSeg = "<?xml version=\"1.0\"?>\r"
		+ "<tmx version=\"1.4\"><header creationtool=\"undefined_creationtool\" creationtoolversion=\"undefined_creationversion\" segtype=\"undefined_segtype\" o-tmf=\"undefined_unknown\" adminlang=\"undefined_adminlang\" srclang=\"en\" datatype=\"unknown\"></header><body><tu tuid=\"tuid_1\"><tuv xml:lang=\"en\"><seg>Hello World!</seg></tuv></tu><tu tuid=\"tuid_1\"><tuv xml:lang=\"en\"><seg>Hello <ut>Ut Content</ut> Universe!</seg></tuv></tu></body></tmx>\r";

	String utSnippetInSub = "<?xml version=\"1.0\"?>\r"
		+ "<!-- document level comment --><tmx version=\"1.4\"><header creationtool=\"undefined_creationtool\" creationtoolversion=\"undefined_creationversion\" segtype=\"undefined_segtype\" o-tmf=\"undefined_unknown\" adminlang=\"undefined_adminlang\" srclang=\"en\" datatype=\"unknown\"></header><body><tu tuid=\"tuid_1\"><note>hello world note</note><tuv xml:lang=\"en\"><seg>Hello World!</seg></tuv></tu><tu tuid=\"tuid_1\"><tuv xml:lang=\"en\"><seg>Hello <ph type=\"fnote\">Before Sub\"<sub>Hello <ut> ut content </ut> Subflow. </sub>After Sub</ph>Universe!</seg></tuv></tu></body></tmx>\r";

	String utSnippetInHi = "<?xml version=\"1.0\"?>\r"
		+ "<!-- document level comment --><tmx version=\"1.4\"><header creationtool=\"undefined_creationtool\" creationtoolversion=\"undefined_creationversion\" segtype=\"undefined_segtype\" o-tmf=\"undefined_unknown\" adminlang=\"undefined_adminlang\" srclang=\"en\" datatype=\"unknown\"></header><body><tu tuid=\"tuid_1\"><note>hello world note</note><tuv xml:lang=\"en\"><seg>Hello World!</seg></tuv></tu><tu tuid=\"tuid_1\"><tuv xml:lang=\"en\"><seg>Hello <hi type=\"fnote\">Start hi <ut> ut content </ut> End hi.</hi>Universe!</seg></tuv></tu></body></tmx>\r";
	
	@Before
	public void setUp() {
		filter = new TmxFilter();
		testDriver = new FilterTestDriver();
		testDriver.setDisplayLevel(0);
		testDriver.setShowSkeleton(true);
		RoundTripUtils.path = TestUtil.getParentDir(this.getClass(), "/dummy.txt")+"test_tmx.json";
	}

	@Test
	public void testTUProperties () {
		String snippet = "<?xml version=\"1.0\"?>\r"
			+ "<tmx version=\"1.4\"><header creationtool=\"z\" creationtoolversion=\"z\" segtype=\"block\" o-tmf=\"z\" adminlang=\"en\" srclang=\"en\" datatype=\"unknown\"></header>"
			+ "<body><tu tuid=\"tuid_1\">"
			+ "<prop type=\"p1\">val1</prop>"
			+ "<tuv xml:lang=\"en\"><seg>Hello World!</seg></tuv></tu></body></tmx>\r";
		ITextUnit tu = FilterTestDriver.getTextUnit(roundTripSerilaizedEvents(getEvents(snippet)), 1);
		assertNotNull(tu);
		Property prop = tu.getProperty("p1");
		assertNotNull(prop);
		assertEquals("val1", prop.getValue());
	}
	
	@Test
	public void testLang11 () {
		String snippet = "<?xml version=\"1.0\"?>\r"
			+ "<tmx version=\"1.1\"><header creationtool=\"undefined_creationtool\" creationtoolversion=\"undefined_creationversion\" segtype=\"undefined_segtype\" o-tmf=\"undefined_unknown\" adminlang=\"undefined_adminlang\" srclang=\"en\" datatype=\"unknown\"></header><body>"
			+ "<tu tuid=\"tuid_1\"><tuv lang=\"en\"><seg>Hello World!</seg></tuv><tuv lang=\"fr\"><seg>Bonjour le monde!</seg></tuv></tu></body></tmx>\r";
		ITextUnit tu = FilterTestDriver.getTextUnit(roundTripSerilaizedEvents(getEvents(snippet)), 1);
		assertNotNull(tu);
		assertTrue(tu.hasTarget(locFR));
	}

	@Test
	public void testSpecialChars () {
		String snippet = "<?xml version=\"1.0\"?>\r"
			+ "<tmx version=\"1.4\"><header creationtool=\"undefined_creationtool\" creationtoolversion=\"undefined_creationversion\" segtype=\"undefined_segtype\" o-tmf=\"undefined_unknown\" adminlang=\"undefined_adminlang\" srclang=\"en\" datatype=\"unknown\"></header>\r<body>\r"
			+ "<tu tuid=\"tuid_1\">\r"
			+ "<prop type=\"p2\">val2</prop>\r"
			+ "<prop type=\"p1\">val1</prop>\r"
			+ "<prop type=\"p4\">val4</prop>\r"
			+ "<prop type=\"p3\">val3</prop>\r"
			+ "<tuv xml:lang=\"en\">\r<seg>&amp;&lt;&quot;&apos;</seg>\r</tuv>\r<tuv xml:lang=\"fr\">\r<seg>&amp;&lt;&quot;&apos;</seg>\r</tuv>\r</tu>\r"
			+ "<tu>\r<tuv xml:lang=\"en\">\r<seg>t2</seg>\r</tuv>\r<tuv xml:lang=\"fr\">\r<seg>t2-fr</seg>\r</tuv>\r</tu>"
			+ "</body>\r</tmx>";
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<tmx version=\"1.4\"><header creationtool=\"undefined_creationtool\" creationtoolversion=\"undefined_creationversion\" segtype=\"undefined_segtype\" o-tmf=\"undefined_unknown\" adminlang=\"undefined_adminlang\" srclang=\"en\" datatype=\"unknown\"></header>\r<body>\r"
			+ "<tu tuid=\"tuid_1\">\r"
			+ "<prop type=\"p2\">val2</prop>\r"
			+ "<prop type=\"p1\">val1</prop>\r"
			+ "<prop type=\"p4\">val4</prop>\r"
			+ "<prop type=\"p3\">val3</prop>\r"
			+ "<tuv xml:lang=\"en\">\r<seg>&amp;&lt;&quot;&apos;</seg>\r</tuv>\r<tuv xml:lang=\"fr\">\r<seg>&amp;&lt;&quot;&apos;</seg>\r</tuv>\r</tu>\r"
			+ "<tu>\r<tuv xml:lang=\"en\">\r<seg>t2</seg>\r</tuv>\r<tuv xml:lang=\"fr\">\r<seg>t2-fr</seg>\r</tuv>\r</tu>"
			+ "</body>\r</tmx>";
		
		assertEquals(expected, FilterTestDriver.generateOutput(roundTripSerilaizedEvents(getEvents(snippet)),
			filter.getEncoderManager(), locFR));
	}
	
	@Test
	public void testLineBreaks () {
		String snippet = "<?xml version=\"1.0\"?>\r"
			+ "<tmx version=\"1.4\"><header creationtool=\"undefined_creationtool\" creationtoolversion=\"undefined_creationversion\" segtype=\"undefined_segtype\" o-tmf=\"undefined_unknown\" adminlang=\"undefined_adminlang\" srclang=\"en\" datatype=\"unknown\"></header>\r<body>\r"
			+ "<tu tuid=\"tuid_1\">\r<tuv xml:lang=\"en\">\r<seg>Hello World!</seg>\r</tuv>\r<tuv xml:lang=\"fr\">\r<seg>Bonjour le monde!</seg>\r</tuv>\r</tu>\r"
			+ "<tu>\r<tuv xml:lang=\"en\">\r<seg>t2</seg>\r</tuv>\r<tuv xml:lang=\"fr\">\r<seg>t2-fr</seg>\r</tuv>\r</tu>\r"
			+ "</body>\r</tmx>";
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<tmx version=\"1.4\"><header creationtool=\"undefined_creationtool\" creationtoolversion=\"undefined_creationversion\" segtype=\"undefined_segtype\" o-tmf=\"undefined_unknown\" adminlang=\"undefined_adminlang\" srclang=\"en\" datatype=\"unknown\"></header>\r<body>\r"
			+ "<tu tuid=\"tuid_1\">\r<tuv xml:lang=\"en\">\r<seg>Hello World!</seg>\r</tuv>\r<tuv xml:lang=\"fr\">\r<seg>Bonjour le monde!</seg>\r</tuv>\r</tu>\r"
			+ "<tu>\r<tuv xml:lang=\"en\">\r<seg>t2</seg>\r</tuv>\r<tuv xml:lang=\"fr\">\r<seg>t2-fr</seg>\r</tuv>\r</tu>\r"
			+ "</body>\r</tmx>";
		
		assertEquals(expected, FilterTestDriver.generateOutput(roundTripSerilaizedEvents(getEvents(snippet)),
			filter.getEncoderManager(), locFR));
	}
	
	@Test
	public void testXmlLangOverLang () {
		String snippet = "<?xml version=\"1.0\"?>\r"
			+ "<tmx version=\"1.1\"><header creationtool=\"undefined_creationtool\" creationtoolversion=\"undefined_creationversion\" segtype=\"undefined_segtype\" o-tmf=\"undefined_unknown\" adminlang=\"undefined_adminlang\" srclang=\"en\" datatype=\"unknown\"></header><body>"
			+ "<tu tuid=\"tuid_1\"><tuv xml:lang=\"en\" lang=\"de-de\"><seg>Hello World!</seg></tuv><tuv lang=\"it-it\" xml:lang=\"fr\"><seg>Bonjour le monde!</seg></tuv></tu></body></tmx>\r";
		ITextUnit tu = FilterTestDriver.getTextUnit(roundTripSerilaizedEvents(getEvents(snippet)), 1);
		assertNotNull(tu);
		assertTrue(tu.hasTarget(locFR));
	}
	
	@Test
	public void testEscapes () {
		String snippet = "<?xml version=\"1.0\"?>\r"
			+ "<tmx version=\"1.1\"><header creationtool=\"undefined_creationtool\" creationtoolversion=\"undefined_creationversion\" segtype=\"undefined_segtype\" o-tmf=\"undefined_unknown\" adminlang=\"undefined_adminlang\" srclang=\"en\" datatype=\"unknown\"></header><body>"
			+ "<tu tuid=\"tuid_1\"><tuv xml:lang=\"en\"><seg>\"\'&amp;&lt;></seg></tuv><tuv xml:lang=\"fr\"><seg>\"\'&amp;&lt;></seg></tuv></tu></body></tmx>\r";
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><tmx version=\"1.1\">"
			+ "<header creationtool=\"undefined_creationtool\" creationtoolversion=\"undefined_creationversion\" segtype=\"undefined_segtype\" o-tmf=\"undefined_unknown\" adminlang=\"undefined_adminlang\" srclang=\"en\" datatype=\"unknown\"></header><body>"
			+ "<tu tuid=\"tuid_1\"><tuv xml:lang=\"en\"><seg>&quot;&apos;&amp;&lt;></seg></tuv><tuv xml:lang=\"fr\"><seg>&quot;&apos;&amp;&lt;></seg></tuv></tu></body></tmx>";
		assertEquals(expected, FilterTestDriver.generateOutput(roundTripSerilaizedEvents(getEvents(snippet)),
			filter.getEncoderManager(), locFR));
	}
	
	@Test
	public void testOutputWithLT () {
		String snippet = "<?xml version=\"1.0\"?>\r"
			+ "<tmx version=\"1.1\"><header creationtool=\"undefined_creationtool\" creationtoolversion=\"undefined_creationversion\" segtype=\"undefined_segtype\" o-tmf=\"undefined_unknown\" adminlang=\"undefined_adminlang\" srclang=\"en\" datatype=\"unknown\"></header><body>"
			+ "<tu tuid=\"1\" creationtool=\"abc&lt;&apos;>\">"
			+ "<tuv xml:lang=\"en\"><seg>a<ph id='1' x=\"&lt;codeph class=&quot;+ topic/ph pr-d/codeph &quot;&gt;\">&lt;code></ph>b</seg></tuv></tu>"
			+ "</body></tmx>";
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<tmx version=\"1.1\"><header creationtool=\"undefined_creationtool\" creationtoolversion=\"undefined_creationversion\" segtype=\"undefined_segtype\" o-tmf=\"undefined_unknown\" adminlang=\"undefined_adminlang\" srclang=\"en\" datatype=\"unknown\"></header><body>"
			+ "<tu tuid=\"1\" creationtool=\"abc&lt;'>\">"
			+ "<tuv xml:lang=\"en\"><seg>a<ph id=\"1\" x=\"&lt;codeph class=&quot;+ topic/ph pr-d/codeph &quot;>\">&lt;code></ph>b</seg></tuv>"
			+ "<tuv xml:lang=\"fr\"><seg>a<ph id=\"1\" x=\"&lt;codeph class=&quot;+ topic/ph pr-d/codeph &quot;>\">&lt;code></ph>b</seg></tuv>\r"
			+ "</tu></body></tmx>";
		assertEquals(expected, FilterTestDriver.generateOutput(roundTripSerilaizedEvents(getEvents(snippet)),
			filter.getEncoderManager(), locFR));
	}		
	
	@Test
	public void testSegTypeSentence () {		
		Parameters params = new Parameters();
		params.setSegType(TmxFilter.SEGTYPE_SENTENCE);
		filter.setParameters(params);
		
		ITextUnit tu = FilterTestDriver.getTextUnit(roundTripSerilaizedEvents(getEvents(simpleBilingualSnippetWithSentence)), 1);
		assertEquals(tu.getSource().getSegments().getAlignmentStatus(), AlignmentStatus.ALIGNED);
		assertTrue(tu.getSource().hasBeenSegmented());
		assertEquals(tu.getTarget(locFR).getSegments().getAlignmentStatus(), AlignmentStatus.ALIGNED);
		assertTrue(tu.getTarget(locFR).hasBeenSegmented());
	}
	
	@Test
	public void testSegTypePara () {		
		Parameters params = new Parameters();
		params.setSegType(TmxFilter.SEGTYPE_PARA);
		filter.setParameters(params);
		
		ITextUnit tu = FilterTestDriver.getTextUnit(roundTripSerilaizedEvents(getEvents(simpleBilingualSnippetWithParagraph)), 1);
		assertEquals(tu.getSource().getSegments().getAlignmentStatus(), AlignmentStatus.NOT_ALIGNED);
		assertTrue(!tu.getSource().hasBeenSegmented());
		assertEquals(tu.getTarget(locFR).getSegments().getAlignmentStatus(), AlignmentStatus.NOT_ALIGNED);
		assertTrue(!tu.getTarget(locFR).hasBeenSegmented());

	}
	
	@Test
	public void testSegTypeOrSentence() {
		
		Parameters params = new Parameters();
		params.setSegType(TmxFilter.SEGTYPE_OR_SENTENCE);
		filter.setParameters(params);
		
		ITextUnit tu = FilterTestDriver.getTextUnit(roundTripSerilaizedEvents(getEvents(simpleBilingualSnippetWithSentence)), 1);
		assertEquals(tu.getSource().getSegments().getAlignmentStatus(), AlignmentStatus.ALIGNED);
		assertTrue(tu.getSource().hasBeenSegmented());
		assertEquals(tu.getTarget(locFR).getSegments().getAlignmentStatus(), AlignmentStatus.ALIGNED);
		assertTrue(tu.getTarget(locFR).hasBeenSegmented());
	}
	
	@Test
	public void testSegTypeOrParagraph() {
		
		Parameters params = new Parameters();
		params.setSegType(TmxFilter.SEGTYPE_OR_PARA);
		filter.setParameters(params);
		
		ITextUnit tu = FilterTestDriver.getTextUnit(roundTripSerilaizedEvents(getEvents(simpleBilingualSnippetWithParagraph)), 1);
		assertEquals(tu.getSource().getSegments().getAlignmentStatus(), AlignmentStatus.NOT_ALIGNED);
		assertTrue(!tu.getSource().hasBeenSegmented());
		assertEquals(tu.getTarget(locFR).getSegments().getAlignmentStatus(), AlignmentStatus.NOT_ALIGNED);
		assertTrue(!tu.getTarget(locFR).hasBeenSegmented());
	}
	
	@Test
	public void testSegTypeOrSentenceDefault() {
		
		Parameters params = new Parameters();
		params.setSegType(TmxFilter.SEGTYPE_OR_SENTENCE);
		filter.setParameters(params);
		
		ITextUnit tu = FilterTestDriver.getTextUnit(roundTripSerilaizedEvents(getEvents(simpleBilingualSnippet)), 1);
		assertEquals(tu.getSource().getSegments().getAlignmentStatus(), AlignmentStatus.NOT_ALIGNED);
		assertTrue(!tu.getSource().hasBeenSegmented());
		assertEquals(tu.getTarget(locFR).getSegments().getAlignmentStatus(), AlignmentStatus.NOT_ALIGNED);
		assertTrue(!tu.getTarget(locFR).hasBeenSegmented());
	}
	
	@Test
	public void testSegTypeOrParagraphDefault() {
		
		Parameters params = new Parameters();
		params.setSegType(TmxFilter.SEGTYPE_OR_PARA);
		filter.setParameters(params);
		
		ITextUnit tu = FilterTestDriver.getTextUnit(roundTripSerilaizedEvents(getEvents(simpleBilingualSnippet)), 1);
		assertEquals(tu.getSource().getSegments().getAlignmentStatus(), AlignmentStatus.NOT_ALIGNED);
		assertTrue(!tu.getSource().hasBeenSegmented());
		assertEquals(tu.getTarget(locFR).getSegments().getAlignmentStatus(), AlignmentStatus.NOT_ALIGNED);
		assertTrue(!tu.getTarget(locFR).hasBeenSegmented());
	}
	
	@Test
	public void testSegTypeOrSentenceUnknown() {
		
		Parameters params = new Parameters();
		params.setSegType(TmxFilter.SEGTYPE_OR_SENTENCE);
		filter.setParameters(params);
		
		ITextUnit tu = FilterTestDriver.getTextUnit(roundTripSerilaizedEvents(getEvents(simpleBilingualSnippetWithUnknown)), 1);
		assertEquals(tu.getSource().getSegments().getAlignmentStatus(), AlignmentStatus.NOT_ALIGNED);
		assertTrue(!tu.getSource().hasBeenSegmented());
		assertEquals(tu.getTarget(locFR).getSegments().getAlignmentStatus(), AlignmentStatus.NOT_ALIGNED);
		assertTrue(!tu.getTarget(locFR).hasBeenSegmented());
	}
	
	@Test
	public void testSegTypeOrParagraphUnknown() {
		
		Parameters params = new Parameters();
		params.setSegType(TmxFilter.SEGTYPE_OR_PARA);
		filter.setParameters(params);
		
		ITextUnit tu = FilterTestDriver.getTextUnit(roundTripSerilaizedEvents(getEvents(simpleBilingualSnippet)), 1);
		assertEquals(tu.getSource().getSegments().getAlignmentStatus(), AlignmentStatus.NOT_ALIGNED);
		assertTrue(!tu.getSource().hasBeenSegmented());
		assertEquals(tu.getTarget(locFR).getSegments().getAlignmentStatus(), AlignmentStatus.NOT_ALIGNED);
		assertTrue(!tu.getTarget(locFR).hasBeenSegmented());
	}
	
	@Test
	public void testSegTypeHeaderSentence() {
		
		Parameters params = new Parameters();
		params.setSegType(TmxFilter.SEGTYPE_OR_PARA);
		filter.setParameters(params);
		
		ITextUnit tu = FilterTestDriver.getTextUnit(roundTripSerilaizedEvents(getEvents(simpleBilingualSnippetHeaderSentence)), 1);
		assertEquals(tu.getSource().getSegments().getAlignmentStatus(), AlignmentStatus.ALIGNED);
		assertTrue(tu.getSource().hasBeenSegmented());
		assertEquals(tu.getTarget(locFR).getSegments().getAlignmentStatus(), AlignmentStatus.ALIGNED);
		assertTrue(tu.getTarget(locFR).hasBeenSegmented());
	}
	
	@Test
	public void testSegTypeHeaderParagraph() {
		
		Parameters params = new Parameters();
		params.setSegType(TmxFilter.SEGTYPE_OR_SENTENCE);
		filter.setParameters(params);
		
		ITextUnit tu = FilterTestDriver.getTextUnit(roundTripSerilaizedEvents(getEvents(simpleBilingualSnippetHeaderParagraph)), 1);
		assertEquals(tu.getSource().getSegments().getAlignmentStatus(), AlignmentStatus.NOT_ALIGNED);
		assertTrue(!tu.getSource().hasBeenSegmented());
		assertEquals(tu.getTarget(locFR).getSegments().getAlignmentStatus(), AlignmentStatus.NOT_ALIGNED);
		assertTrue(!tu.getTarget(locFR).hasBeenSegmented());
	}
	
	@Test
	public void testSegTypeHeaderSentenceOverwrite() {
		
		Parameters params = new Parameters();
		params.setSegType(TmxFilter.SEGTYPE_OR_SENTENCE);
		filter.setParameters(params);
		
		ITextUnit tu = FilterTestDriver.getTextUnit(roundTripSerilaizedEvents(getEvents(biHeaderSentenceTuPara)), 1);
		assertEquals(tu.getSource().getSegments().getAlignmentStatus(), AlignmentStatus.NOT_ALIGNED);
		assertTrue(!tu.getSource().hasBeenSegmented());
		assertEquals(tu.getTarget(locFR).getSegments().getAlignmentStatus(), AlignmentStatus.NOT_ALIGNED);
		assertTrue(!tu.getTarget(locFR).hasBeenSegmented());
	}
	
	@Test
	public void testSegTypeHeaderParagraphOverwrite() {
		
		Parameters params = new Parameters();
		params.setSegType(TmxFilter.SEGTYPE_OR_PARA);
		filter.setParameters(params);
		
		ITextUnit tu = FilterTestDriver.getTextUnit(roundTripSerilaizedEvents(getEvents(biHeaderParaTuSentence)), 1);
		assertEquals(tu.getSource().getSegments().getAlignmentStatus(), AlignmentStatus.ALIGNED);
		assertTrue(tu.getSource().hasBeenSegmented());
		assertEquals(tu.getTarget(locFR).getSegments().getAlignmentStatus(), AlignmentStatus.ALIGNED);
		assertTrue(tu.getTarget(locFR).hasBeenSegmented());
	}
	
	
	@Test
	public void testSimpleTransUnit () {
		ITextUnit tu = FilterTestDriver.getTextUnit(roundTripSerilaizedEvents(getEvents(simpleSnippet)), 1);
		assertNotNull(tu);
		assertEquals("Hello World!", tu.getSource().toString());
		assertEquals("tuid_1", tu.getName());
	}
	
	@Test
	public void testMulipleTargets () {
		List<Event> events = roundTripSerilaizedEvents(getEvents(multiTransSnippet));

		ITextUnit tu = FilterTestDriver.getTextUnit(events, 1);
		assertNotNull(tu);
		assertEquals("Hello", tu.getSource().toString());
		assertEquals(3, tu.getTargetLocales().size());
		assertTrue(tu.hasTarget(locFR));
		assertEquals("Bonjour", tu.getTarget(locFR).toString());
		assertTrue(tu.hasTarget(locDE));
		assertEquals("Hallo", tu.getTarget(locDE).toString());
		assertTrue(tu.hasTarget(locIT));
		assertEquals("Buongiorno", tu.getTarget(locIT).toString());

		tu = FilterTestDriver.getTextUnit(events, 2);
		assertNotNull(tu);
		assertEquals("Hello", tu.getSource().toString());
		assertEquals(1, tu.getTargetLocales().size());
		assertTrue(tu.hasTarget(locFR));
		assertEquals("Salut", tu.getTarget(locFR).toString());
	}
	
	@Test
	public void testUtInSeg () {
		FilterTestDriver.getStartDocument(roundTripSerilaizedEvents(getEvents(utSnippetInSeg)));
	}

	@Test
	public void testUtInSub () {
		FilterTestDriver.getStartDocument(roundTripSerilaizedEvents(getEvents(utSnippetInSub)));
	}

	@Test
	public void testUtInHi () {
		FilterTestDriver.getStartDocument(roundTripSerilaizedEvents(getEvents(utSnippetInHi)));
	}

	@Test
	public void testIsolatedCodes () {
		String snippet = "<?xml version=\"1.0\"?>\r"
			+ "<tmx version=\"1.4\"><header creationtool=\"z\" creationtoolversion=\"z\" segtype=\"block\" o-tmf=\"z\" adminlang=\"en\" srclang=\"en\" datatype=\"unknown\"></header>"
			+ "<body><tu tuid=\"tu1\"><tuv xml:lang=\"en\">"
			+ "<seg><it pos='end'>[/i]</it> a <ph>[x/]</ph> b <it pos='begin'>[b]</it></seg>"
			+ "</tuv></tu></body></tmx>\r";
		ITextUnit tu = FilterTestDriver.getTextUnit(roundTripSerilaizedEvents(getEvents(snippet)), 1);
		assertNotNull(tu);
		List<Code> codes = tu.getSource().getFirstContent().getCodes();
		assertEquals(3, codes.size());
		assertEquals(TagType.CLOSING, codes.get(0).getTagType());
		assertEquals(TagType.PLACEHOLDER, codes.get(1).getTagType());
		assertEquals(TagType.OPENING, codes.get(2).getTagType());
		assertEquals("<e3/> a <1/> b <b2/>", fmt.setContent(tu.getSource().getFirstContent()).toString());
	}
				
	@Test
	public void testTUTUVAttrEscaping () {
		String snippet = "<?xml version=\"1.0\"?>\r"
			+ "<tmx version=\"1.4\"><header creationtool=\"z\" creationtoolversion=\"z\" segtype=\"block\" o-tmf=\"z\" adminlang=\"en\" srclang=\"en\" datatype=\"unknown\"></header>"
			+ "<body><tu tuid=\"tuid_1\" foo=\"&lt;VALUE1&amp;VALUE2\">"
			+ "<prop type=\"p1\">val1</prop>"
			+ "<tuv xml:lang=\"en\" foo=\"&lt;VALUE1&amp;VALUE2;\"><seg>Hello World!</seg></tuv></tu></body></tmx>\r";
		List<Event> events = getEvents(snippet);		
		ITextUnit tu = FilterTestDriver.getTextUnit(events, 1);
		assertNotNull(tu);
		// We should not find invalid skeleton content.  The skeleton content 
		// includes both the TU and TUV attribute values, so the easiest way to
		// check this is by making sure unescaped stuff didn't sneak in.
		assertTrue(tu.getSkeleton().toString().indexOf("<VALUE1") == -1);
		assertTrue(tu.getSkeleton().toString().indexOf("&VALUE2") == -1);
		
		List<Event> simplifiedEvents = roundTripSerilaizedEvents(events);
		tu = FilterTestDriver.getTextUnit(simplifiedEvents, 1);
		assertNotNull(tu);
		assertNotNull(tu.getSkeleton());
		assertNotNull(tu);
		// We should not find invalid skeleton content.  The skeleton content 
		// includes both the TU and TUV attribute values, so the easiest way to
		// check this is by making sure unescaped stuff didn't sneak in.
		assertTrue(tu.getSkeleton().toString().indexOf("<VALUE1") == -1);
		assertTrue(tu.getSkeleton().toString().indexOf("&VALUE2") == -1);		
	}
	
	private List<Event> getEvents(String snippet){
		return FilterTestDriver.getEvents(filter, new RawDocument(snippet, locEN, locFR), null);
	}
}
