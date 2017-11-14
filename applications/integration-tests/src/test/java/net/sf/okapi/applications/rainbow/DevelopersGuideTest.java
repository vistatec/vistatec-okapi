/*===========================================================================
  Copyright (C) 2017 by the Okapi Framework contributors
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

package net.sf.okapi.applications.rainbow;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.FileLocation;
import net.sf.okapi.common.IResource;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.ISegments;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextUnit;
import net.sf.okapi.filters.html.HtmlFilter;
import net.sf.okapi.filters.properties.PropertiesFilter;

//Code examples for the Developers' Guide Web pages.
@RunWith(JUnit4.class)
public class DevelopersGuideTest {

	private FileLocation location = FileLocation.fromClass(getClass());
	
	@Test
	public void testRawDoc () {
		try ( // Creates the RawDocument object
			RawDocument res = new RawDocument("key1=Text1\nkey2=Text2", LocaleId.fromString("en"));
			// Create the filter
			PropertiesFilter filter = new PropertiesFilter() )
		{
			// Opens the document
			filter.open(res);
			// Get the events from the input document
			while ( filter.hasNext() ) {
			   Event event = filter.next();
			   // do something with the event...
			   // Here, if the event is TEXT_UNIT, we display the key and the extracted text
			   if ( event.getEventType() == EventType.TEXT_UNIT ) {
			      TextUnit tu = (TextUnit)event.getResource();
			      System.out.println("--");
			      System.out.println("key=["+tu.getName()+"]");
			      System.out.println("text=["+tu.getSource()+"]");
			   }
			}
			// Close the input document
			// No need to call filter.close(); if within a try-with-resource block
		}
	}
	
	@Test
	public void testModify () {
		try ( // Create a filter object
			IFilter filter = new HtmlFilter() )
		{
			// Set the filter writer's options
			LocaleId trgLoc = LocaleId.fromString("fr");
			// Open the input from a CharSequence
			filter.open(new RawDocument("<html><head>\n"
			   + "<meta http-equiv='Content-Language' content='en'></head>\n"
			   + "<body>\n"
			   + "<p>Text in <b>bold</b>.</p>"
			   + "</body></html>",
			   LocaleId.fromString("en"), trgLoc));
			// Create the filter writer
			IFilterWriter writer = filter.createFilterWriter();
			writer.setOptions(trgLoc, "iso-8859-1");
			// Set the output
			location.out("/myFile_fr.html");
			writer.setOutput(location.asFile().getAbsolutePath());
			
			// Processing the input document
			while ( filter.hasNext() ) {
				Event event = filter.next();
				if ( event.getEventType() == EventType.TEXT_UNIT ) {
					changeTU(event.getTextUnit());
				}
				writer.handleEvent(event);
			}
			// Closing the filter and the filter writer
			// No need to call filter.close();
			// No need to call writer.close();
		}
		assertTrue(location.asFile().exists());
	}

	private void changeTU (ITextUnit tu) {
		// Check if this unit can be modified
		if ( !tu.isTranslatable() ) return; // If not, return without changes
		TextContainer tc = tu.createTarget(LocaleId.fromString("fr"), false, IResource.COPY_ALL);
		ISegments segs = tc.getSegments();
		for ( Segment seg : segs ) {
			TextFragment tf = seg.getContent();
			tf.setCodedText(tf.getCodedText().toUpperCase());
		}
	}

}
