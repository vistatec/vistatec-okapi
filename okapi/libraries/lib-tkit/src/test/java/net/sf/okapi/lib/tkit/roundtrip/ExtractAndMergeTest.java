package net.sf.okapi.lib.tkit.roundtrip;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IResource;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.TestUtil;
import net.sf.okapi.common.filters.FilterTestDriver;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.ISegments;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.filters.html.HtmlFilter;
import net.sf.okapi.filters.xliff.XLIFFFilter;
import net.sf.okapi.lib.tkit.merge.SkeletonMergerWriter;
import net.sf.okapi.lib.tkit.writer.XLIFFAndSkeletonWriter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class ExtractAndMergeTest {

	private LocaleId locEN = LocaleId.ENGLISH;
	private LocaleId locFR = LocaleId.FRENCH;
	private String root = TestUtil.getParentDir(this.getClass(), "/simple.html");
	
	private void extract (List<Event> events,
		LocaleId srcLocId,
		LocaleId trgLocId,
		String xliffPath)
	{
		// Save the Skeleton and the XLIFF document
		XLIFFAndSkeletonWriter writer = null;
		try {
			// Setup the writer
			writer = new XLIFFAndSkeletonWriter();
			writer.setOptions(trgLocId, "UTF-8");
			writer.setOutput(xliffPath);
			// Save the events
			for ( Event event : events ) {
				writer.handleEvent(event);
			}
		}
		finally {
			if ( writer != null ) writer.close();
		}
	}
	
	private void translate (String inputPath,
		String outputPath,
		LocaleId srcLocId,
		LocaleId trgLocId)
	{
		XLIFFFilter filter = null;
		IFilterWriter writer = null;
		try {
			// Setup the filter
			filter = new XLIFFFilter();
			File file = new File(inputPath);
			RawDocument rd = new RawDocument(file.toURI(), "UTF-8", srcLocId, trgLocId);
			filter.open(rd);
			// Create the writer
			writer = filter.createFilterWriter();
			writer.setOptions(trgLocId, "UTF-8");
			writer.setOutput(outputPath);
			// Process
			while ( filter.hasNext() ) {
				Event event = filter.next();
				// pseudo-translate
				if ( event.isTextUnit() ) {
					ITextUnit unit = event.getTextUnit();
					TextContainer tc = unit.createTarget(trgLocId, true, IResource.COPY_SEGMENTED_CONTENT);
					ISegments segs = tc.getSegments();
					for ( Segment seg : segs ) {
						seg.getContent().append("TRANS");
					}
				}
				// Write out the event
				writer.handleEvent(event);
			}
		}
		finally {
			if ( filter != null ) filter.close();
			if ( writer != null ) writer.close();
		}
	}

	private void merge (String skelPath,
		String xliffPath,
		String mergedPath,
		LocaleId srcLocId,
		LocaleId trgLocId)
	{
		File file = new File(mergedPath);
		file.delete();
		SkeletonMergerWriter.mergeFromSkeleton(skelPath, xliffPath, mergedPath, srcLocId, trgLocId, "UTF-8");
	}
	
	@Test
	public void testExtracThenMerge () {
		HtmlFilter filter = new HtmlFilter();
		String snippet = "<p>Text <b>bold</b> more text.</p>";
		List<Event> events = FilterTestDriver.getEvents(filter, snippet, locEN, locFR);
		
		String file = root+"extract1.html";
		File outFile = new File(file+".merged");
		outFile.delete();
		
		extract(events, locEN, locFR, file+".xlf");
		translate(file+".xlf", file+".translated", locEN, locFR);
		merge(file+".skl", file+".translated", outFile.getAbsolutePath(), locEN, locFR);

		RawDocument rd = new RawDocument(new File(file+".merged").toURI(), "UTF-8", locFR);
		events = FilterTestDriver.getEvents(filter, rd, null);
		for ( Event event : events ) {
			if ( event.isTextUnit() ) {
				ITextUnit unit = event.getTextUnit();
				ISegments segs = unit.getSourceSegments();
				for ( Segment seg : segs ) {
					assertTrue(seg.getContent().getCodedText().endsWith("TRANS"));
				}
			}
		}
	}
}
