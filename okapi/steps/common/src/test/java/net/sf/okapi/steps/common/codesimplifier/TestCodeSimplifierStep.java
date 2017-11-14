package net.sf.okapi.steps.common.codesimplifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.FileLocation;
import net.sf.okapi.common.ISkeleton;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filters.InputDocument;
import net.sf.okapi.common.filters.RoundTripComparison;
import net.sf.okapi.common.filterwriter.GenericContent;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.common.resource.TextUnit;
import net.sf.okapi.common.skeleton.GenericSkeleton;
import net.sf.okapi.filters.html.HtmlFilter;
import net.sf.okapi.filters.idml.IDMLFilter;
import net.sf.okapi.filters.xliff.XLIFFFilter;
import net.sf.okapi.lib.extra.pipelinebuilder.XBatch;
import net.sf.okapi.lib.extra.pipelinebuilder.XBatchItem;
import net.sf.okapi.lib.extra.pipelinebuilder.XPipeline;
import net.sf.okapi.lib.extra.steps.DocumentPartLogger;
import net.sf.okapi.lib.extra.steps.EventListBuilderStep;
import net.sf.okapi.lib.extra.steps.EventLogger;
import net.sf.okapi.lib.extra.steps.TextUnitLogger;
import net.sf.okapi.steps.common.RawDocumentToFilterEventsStep;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(JUnit4.class)
public class TestCodeSimplifierStep {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private GenericContent fmt;
	private CodeSimplifierStep css;
	private static final LocaleId EN = new LocaleId("en", "us");
	private static final LocaleId ESES = new LocaleId("es", "es");
	private FileLocation pathBase;
	
	@Before
	public void setup() throws URISyntaxException {
		css = new CodeSimplifierStep(); 
		fmt = new GenericContent();
		pathBase = FileLocation.fromClass(this.getClass());
	}	
	
	@Test
	public void testDefaults () {
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

		ISkeleton skel = tu1.getSkeleton();
		assertNull(skel);
		
		Event tue1 = new Event(EventType.TEXT_UNIT, tu1);
		css.handleEvent(tue1);
		tf = tu1.getSource().getUnSegmentedContentCopy();
//		assertEquals("<1/>   <2/>T1<3>T2   </3>   <e5/>   <4/>", fmt.setContent(tf).toString());
		assertEquals("<1/>   <2/>T1<5>T2   </5>   <e8/>   <7/>", fmt.setContent(tf).toString());
		
		skel = tu1.getSkeleton();
		assertNull(skel);
	}
	
	@Test
	public void testDefaults2 () {
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

		ISkeleton skel = new GenericSkeleton();
		assertNotNull(skel);
		tu1.setSkeleton(skel);
		assertNotNull(tu1.getSkeleton());
		
		Event tue1 = new Event(EventType.TEXT_UNIT, tu1);
		css.handleEvent(tue1);
		tf = tu1.getSource().getUnSegmentedContentCopy();
//		assertEquals("T1<3>T2   </3>", fmt.setContent(tf).toString());
		assertEquals("T1<5>T2   </5>", fmt.setContent(tf).toString());
		
		skel = tu1.getSkeleton();
		assertEquals("<x1/>   <x2/>[#$$self$]   </b><x5/>   <x6/>", skel.toString());
	}
	
	@Test
	public void testNoRemoval () {
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
	

	@Test
	public void testDoubleExtraction () {
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
		
		list.add(new InputDocument(pathBase.in("test1.html").toString(), null));
		
		RoundTripComparison rtc = new RoundTripComparison();
		assertTrue(rtc.executeCompare(new HtmlFilter(), list, "UTF-8", EN, ESES, "out", new CodeSimplifierStep()));
	}
	
	@Test
	public void testDoubleExtraction2 () {
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
		
		list.add(new InputDocument(pathBase.in("aa324.html").toString(), null));
		
		RoundTripComparison rtc = new RoundTripComparison();
		assertTrue(rtc.executeCompare(new HtmlFilter(), list, "UTF-8", EN, ESES, "out", new CodeSimplifierStep()));
	}
	
	@Test
	public void testEvents() throws MalformedURLException {
		//EventListBuilderStep elbs1 = new EventListBuilderStep();
		EventListBuilderStep elbs2 = new EventListBuilderStep();
		
		logger.debug(pathBase.in("").toString());
		
		new XPipeline(
				"Test pipeline for CodeSimplifierStep",
				new XBatch(
						new XBatchItem(
								pathBase.in("aa324.html").asUri(),
								"UTF-8",
								EN)
						),
						
				new RawDocumentToFilterEventsStep(new HtmlFilter()),
				new EventLogger(),
				elbs2
		).execute();
		
		for (Event event : elbs2.getList()) {
			if (event.isTextUnit()) {
				logger.debug(TextUnitLogger.getTuInfo(event.getTextUnit(), EN));
			}
			else if (event.isDocumentPart()) {
				logger.debug(DocumentPartLogger.getDpInfo(event.getDocumentPart(), EN));
			}
		}		
	}
	
	@Test
	public void testEvents2() throws MalformedURLException {
		EventListBuilderStep elbs2 = new EventListBuilderStep();
		
		new XPipeline(
				"Test pipeline for CodeSimplifierStep",
				new XBatch(
						new XBatchItem(
								pathBase.in("out/aa324.html").asUri(), 
								"UTF-8",
								EN)
						),
						
				new RawDocumentToFilterEventsStep(new HtmlFilter()),
				new EventLogger(),
				elbs2
		).execute();
		
		for (Event event : elbs2.getList()) {
			if (event.isTextUnit()) {
				logger.debug(TextUnitLogger.getTuInfo(event.getTextUnit(), EN));
			}
			else if (event.isDocumentPart()) {
				logger.debug(DocumentPartLogger.getDpInfo(event.getDocumentPart(), EN));
			}
		}		
	}
	
	@Test
	public void testEvents3() throws MalformedURLException {
		new XPipeline(
				"Test pipeline for CodeSimplifierStep",
				new XBatch(
						new XBatchItem(
								pathBase.in("aa324.html").asUri(),
								"UTF-8",
								EN)
						),
						
				new RawDocumentToFilterEventsStep(new HtmlFilter()),
				new TextUnitLogger()
		).execute();
	}
	
	@Test
	public void testEvents4() throws MalformedURLException {
		new XPipeline(
				"Test pipeline for CodeSimplifierStep",
				new XBatch(
						new XBatchItem(
								pathBase.in("out/aa324.html").asUri(),
								"UTF-8",
								EN)
						),
						
				new RawDocumentToFilterEventsStep(new HtmlFilter()),
				new TextUnitLogger()
		).execute();
	}
	
	@Test
	public void testEvents5() throws MalformedURLException {
		new XPipeline(
				"Test pipeline for CodeSimplifierStep",
				new XBatch(
						new XBatchItem(
								pathBase.in("idmltest.idml").asUri(),
								"UTF-8",
								EN)
						),
						
				new RawDocumentToFilterEventsStep(new IDMLFilter()),
				new TextUnitLogger()
		).execute();
	}
	
	@Test
	public void testDoubleExtraction3 () {
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
		
		list.add(new InputDocument(pathBase.in("form.html").toString(), null));
		
		RoundTripComparison rtc = new RoundTripComparison();
		assertTrue(rtc.executeCompare(new HtmlFilter(), list, "UTF-8", EN, ESES, "out", new CodeSimplifierStep()));
	}		
	
	@Test
	public void testDoubleExtraction4 () {
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
		
		list.add(new InputDocument(pathBase.in("BinUnitTest01.xlf").toString(), null));
		list.add(new InputDocument(pathBase.in("JMP-11-Test01.xlf").toString(), null));
		list.add(new InputDocument(pathBase.in("Manual-12-AltTrans.xlf").toString(), null));
		list.add(new InputDocument(pathBase.in("test1.xlf").toString(), null));

		RoundTripComparison rtc = new RoundTripComparison();
		assertTrue(rtc.executeCompare(new XLIFFFilter(), list, "UTF-8", EN, ESES, "out", new CodeSimplifierStep()));
	}
	
	@Test
	public void testDoubleExtraction5 () {
		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
		
		list.add(new InputDocument(pathBase.in("idmltest.idml").toString(), null));
		
		RoundTripComparison rtc = new RoundTripComparison(false);
		assertTrue(rtc.executeCompare(new IDMLFilter(), list, "UTF-8", EN, EN, "out", new CodeSimplifierStep()));
	}
}
