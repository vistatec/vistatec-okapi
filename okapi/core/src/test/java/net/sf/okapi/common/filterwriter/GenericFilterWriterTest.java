/*===========================================================================
  Copyright (C) 2009-2011 by the Okapi Framework contributors
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

package net.sf.okapi.common.filterwriter;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.filters.FilterTestDriver;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.DocumentPart;
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.StartGroup;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.common.resource.TextUnit;
import net.sf.okapi.common.skeleton.GenericSkeleton;
import net.sf.okapi.common.skeleton.GenericSkeletonWriter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class GenericFilterWriterTest {

	public Event startDocEvent;

	private LocaleId locEN = LocaleId.fromString("en");
	private LocaleId locFR = LocaleId.fromString("fr");
	
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testEmbeddedGroups () {
		String expected = "<p>Text before list:"
			+ "<ul>"
			+ "<li>Text of item 1</li>"
			+ "<li>Text of item 2</li>"
			+ "</ul>"
            + "and text after the list.</p>";
		ArrayList<Event> events = new ArrayList<Event>();

		StartDocument sd = new StartDocument("sd1");
		sd.setLineBreak("\n");
		events.add(new Event(EventType.START_DOCUMENT, sd));
		
		GenericSkeleton skel_tu3 = new GenericSkeleton("<p>");
		ITextUnit tu3 = new TextUnit("tu3", "Text before list:");
		tu3.setSkeleton(skel_tu3);
		skel_tu3.addContentPlaceholder(tu3);
	
		GenericSkeleton skel_sg1 = new GenericSkeleton("<ul>");
		StartGroup sg1 = new StartGroup(tu3.getId(), "sg1");
		sg1.setSkeleton(skel_sg1);
		sg1.setIsReferent(true);

		TextFragment tf_tu3 = tu3.getSource().getSegments().getFirstContent(); // Assume un-segmented content
		Code c = new Code(TagType.PLACEHOLDER, "ul", TextFragment.makeRefMarker(sg1.getId()));
		c.setReferenceFlag(true);
		tf_tu3.append(c);
		events.add(new Event(EventType.START_GROUP, sg1));

		GenericSkeleton skel_tu1 = new GenericSkeleton("<li>");
		ITextUnit tu1 = new TextUnit("tu1", "Text of item 1");		
		tu1.setSkeleton(skel_tu1);
		skel_tu1.addContentPlaceholder(tu1);
		skel_tu1.append("</li>");
//		tu1.setIsReferent(true);
//		skel_sg1.addReference(tu1);
		events.add(new Event(EventType.TEXT_UNIT, tu1));
				
		GenericSkeleton skel_tu2 = new GenericSkeleton("<li>");
		ITextUnit tu2 = new TextUnit("tu2", "Text of item 2");		
		tu2.setSkeleton(skel_tu2);
		skel_tu2.addContentPlaceholder(tu2);
		skel_tu2.append("</li>");
//		tu2.setIsReferent(true);
//		skel_sg1.addReference(tu2);
		events.add(new Event(EventType.TEXT_UNIT, tu2));
		
		GenericSkeleton skel_eg1 = new GenericSkeleton("</ul>");
		Ending eg1 = new Ending("eg1");
		eg1.setSkeleton(skel_eg1);
		events.add(new Event(EventType.END_GROUP, eg1));
		
		tf_tu3.append("and text after the list.");
		skel_tu3.append("</p>");
		
		events.add(new Event(EventType.TEXT_UNIT, tu3));

		Ending ed1 = new Ending("ed1"); 
		events.add(new Event(EventType.END_DOCUMENT, ed1));
		
		EncoderManager encoderManager = new EncoderManager();
		encoderManager.setAllKnownMappings();
		String result = FilterTestDriver.generateOutput(events, encoderManager, locFR);
		assertEquals(expected, result);
		
	}
	
	@Test
	public void testSourceTargetSkeleton () {
		ITextUnit tu = new TextUnit("tu1");
		tu.setSourceContent(new TextFragment("src"));
		tu.setTargetContent(locFR, new TextFragment("trg"));
		Event textUnitEvent = new Event(EventType.TEXT_UNIT, tu);
			
		GenericSkeleton skel = new GenericSkeleton();
		skel.add("[start]");
		skel.addContentPlaceholder(tu);
		skel.add("[middle]");
		skel.addContentPlaceholder(tu, locFR);
		skel.add("[end]");
		tu.setSkeleton(skel);
		
		EncoderManager encMgt = new EncoderManager();
		encMgt.setAllKnownMappings();
		GenericFilterWriter writer = new GenericFilterWriter(new GenericSkeletonWriter(), encMgt);
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		writer.setOptions(locFR, "UTF-8");
		writer.setOutput(output);
		writer.handleEvent(createStartDocument(true));
		writer.handleEvent(textUnitEvent);
		writer.close();
		
		assertEquals("[start]src[middle]trg[end]", output.toString());
	}

	@Test
	public void testTextUnitReferenceInDocumentPart () {
		ITextUnit tu = new TextUnit("tu1");
		tu.setSourceContent(new TextFragment("text"));
		tu.setIsReferent(true);
		Event textUnitEvent = new Event(EventType.TEXT_UNIT, tu);
			
		GenericSkeleton skel = new GenericSkeleton();
		skel.add("[bSkel]");
		skel.addReference(tu);
		skel.add("[eSkel]");
		DocumentPart dp = new DocumentPart("id1", false);
		dp.setSkeleton(skel);
		Event docPartEvent = new Event(EventType.DOCUMENT_PART, dp);
		
		EncoderManager encMgt = new EncoderManager();
		encMgt.setAllKnownMappings();
		GenericFilterWriter writer = new GenericFilterWriter(new GenericSkeletonWriter(), encMgt);
		writer.setOptions(locEN, "UTF-8");
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		writer.setOutput(output);
			
		writer.handleEvent(createStartDocument(false));
		writer.handleEvent(textUnitEvent);
		writer.handleEvent(docPartEvent);
		writer.close();
			
		assertEquals("[bSkel]text[eSkel]", output.toString());
	}

	private Event createStartDocument (boolean multilingual) {
		StartDocument sd = new StartDocument("sd");
		sd.setEncoding("UTF-8", false);
		sd.setLineBreak("\n");
		sd.setLocale(locEN);
		sd.setMultilingual(multilingual);
		startDocEvent = new Event(EventType.START_DOCUMENT, sd);
		return startDocEvent;
	}
}
