/*===========================================================================
  Copyright (C) 2013 by the Okapi Framework contributors
-----------------------------------------------------------------------------
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
===========================================================================*/

package net.sf.okapi.lib.tkit.writer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.ISegmenter;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.TestUtil;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.filters.FilterTestDriver;
import net.sf.okapi.common.resource.IAlignedSegments;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.TextUnit;
import net.sf.okapi.filters.html.HtmlFilter;
import net.sf.okapi.lib.segmentation.LanguageMap;
import net.sf.okapi.lib.segmentation.Rule;
import net.sf.okapi.lib.segmentation.SRXDocument;
import net.sf.okapi.lib.tkit.filter.BeanEventFilter;
import net.sf.okapi.lib.tkit.merge.SkeletonMergerWriter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class SkeletonMergerWriterTest {

	private HtmlFilter htmlFilter;
	private String root;
	private LocaleId locEN = LocaleId.fromString("en");
	private LocaleId locFR = LocaleId.fromString("fr");
	private BeanEventWriter eventWriter;
	private BeanEventFilter eventReader;
	private SkeletonMergerWriter skeletonWriter;

	@Before
	public void setUp() {
		htmlFilter = new HtmlFilter();
		root = TestUtil.getParentDir(this.getClass(), "/dummy.txt");
		eventWriter = new BeanEventWriter();
		eventWriter.setOptions(locFR, null);
		Parameters p = new Parameters();
		p.setRemoveTarget(false);
		p.setMessage("Hello!");
		eventWriter.setParameters(p);
		eventReader = new BeanEventFilter();
		skeletonWriter = new SkeletonMergerWriter();
	}

	@After
	public void tearDown() {
		htmlFilter.close();
		eventWriter.close();
		eventReader.close();
		skeletonWriter.close();
	}

	@Test
	public void simpleMergeBilingual() {
		String input = "simple.html";

		// skeleton merger options
		skeletonWriter.setOptions(locFR, "UTF-8");
		skeletonWriter.setOutput(root + input + ".out");
		IParameters p = skeletonWriter.getParameters();
		((net.sf.okapi.lib.tkit.merge.Parameters) p).setSkeletonUri(Util
				.toURI(root + input + ".json"));

		// Serialize the source file
		writeEvents(FilterTestDriver.getEvents(htmlFilter,
				new RawDocument(Util.toURI(root + input), "UTF-8",
						LocaleId.FRENCH), null), root + input + ".json");

		// simulate a bilingual file as input
		for (Event e : FilterTestDriver.getEvents(htmlFilter, new RawDocument(
				Util.toURI(root + input), "UTF-8", LocaleId.FRENCH), null)) {
			if (e.isStartDocument()) {
				e.getStartDocument().setMultilingual(true);
				e.getStartDocument().setLocale(locFR);
			}
			if (e.isTextUnit()) {
				// make the TU bilingual
				e.getTextUnit().createTarget(locFR, true, TextUnit.COPY_ALL);
				e.getTextUnit().getTarget(locFR).clear();
				e.getTextUnit().getTarget(locFR).append("XXXJEHIIIXXX");
			}
			Event mergedEvent = skeletonWriter.handleEvent(e);
			if (mergedEvent.isTextUnit()) {
				assertEquals("XXXJEHIIIXXX", mergedEvent.getTextUnit()
						.getTarget(locFR).toString());
			}
		}
	}

	@Test
	public void simpleMergeMonolingual() {
		String input = "simple.html";

		// skeleton merger options
		skeletonWriter.setOptions(locFR, "UTF-8");
		skeletonWriter.setOutput(root + input + ".out");
		IParameters p = skeletonWriter.getParameters();
		((net.sf.okapi.lib.tkit.merge.Parameters) p).setSkeletonUri(Util.toURI(root + input + ".json"));

		// Serialize the source file
		writeEvents(FilterTestDriver.getEvents(htmlFilter,
				new RawDocument(Util.toURI(root + input), "UTF-8",
						LocaleId.FRENCH), null), root + input + ".json");

		// simulate a monlingual translated file as input
		for (Event e : FilterTestDriver.getEvents(htmlFilter, new RawDocument(
				Util.toURI(root + input), "UTF-8", LocaleId.FRENCH), null)) {
			if (e.isStartDocument()) {
				e.getStartDocument().setMultilingual(true);
				e.getStartDocument().setLocale(locFR);
			}
			if (e.isTextUnit()) {
				e.getTextUnit().getSource().clear();
				e.getTextUnit().getSource().append("XXXJEHIIIXXX");
			}
			Event mergedEvent = skeletonWriter.handleEvent(e);
			if (mergedEvent.isTextUnit()) {
				assertEquals("XXXJEHIIIXXX", mergedEvent.getTextUnit()
						.getSource().toString());
			}
		}
	}

	@Test
	public void simpleSegmentedMerge() {
		String input = "simple.html";

		// skeleton merger options
		skeletonWriter.setOptions(locFR, "UTF-8");
		skeletonWriter.setOutput(root + input + ".out");
		IParameters p = skeletonWriter.getParameters();
		((net.sf.okapi.lib.tkit.merge.Parameters) p).setSkeletonUri(Util
				.toURI(root + input + ".json"));

		// Serialize the source file
		writeEvents(FilterTestDriver.getEvents(htmlFilter,
				new RawDocument(Util.toURI(root + input), "UTF-8",
						LocaleId.FRENCH), null), root + input + ".json");

		// simulate a bilingual file as input
		for (Event e : FilterTestDriver.getEvents(htmlFilter, new RawDocument(
				Util.toURI(root + input), "UTF-8", LocaleId.FRENCH), null)) {
			if (e.isStartDocument()) {
				e.getStartDocument().setMultilingual(true);
			}
			if (e.isTextUnit()) {
				e.getTextUnit().createSourceSegmentation(
						createSegmenterWithRules(locEN));
				// make the TU bilingual and copy source segments
				e.getTextUnit().createTarget(locFR, true, TextUnit.COPY_ALL);
			}
			Event mergedEvent = skeletonWriter.handleEvent(e);
			if (mergedEvent.isTextUnit()) {
				assertTrue(mergedEvent.getTextUnit().getTarget(locFR)
						.hasBeenSegmented());
				IAlignedSegments segments = mergedEvent.getTextUnit()
						.getAlignedSegments();
				Segment src = segments.getSource(0, locFR);
				Segment trg = segments.getCorrespondingTarget(src, locFR);
				assertEquals(src.text.toString(), trg.text.toString());
			}
		}
	}

	private void writeEvents(List<Event> events, String path) {
		// Serialize all the events
		eventWriter.setOutput(path);
		for (Event event : events) {
			eventWriter.handleEvent(event);
		}
		eventWriter.close();
	}

	private ISegmenter createSegmenterWithRules(LocaleId locId) {
		SRXDocument doc = new SRXDocument();
		LanguageMap langMap = new LanguageMap(".*", "default");
		doc.addLanguageMap(langMap);
		// Add the rules
		ArrayList<Rule> langRules = new ArrayList<Rule>();
		langRules.add(new Rule("\\.", "\\s", true));
		// Add the rules to the document
		doc.addLanguageRule("default", langRules);
		// Create the segmenter
		return doc.compileLanguageRules(locId, null);
	}
}
