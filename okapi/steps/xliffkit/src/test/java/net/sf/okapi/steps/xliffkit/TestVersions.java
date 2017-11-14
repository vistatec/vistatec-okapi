/*===========================================================================
  Copyright (C) 2010-2011 by the Okapi Framework contributors
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

package net.sf.okapi.steps.xliffkit;

import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filters.FilterTestDriver;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextUnitUtil;
import net.sf.okapi.common.skeleton.ZipSkeleton;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestVersions {

	@Test // Make sure we have at least one test to avoid build errors
	public void testDescription () {
		net.sf.okapi.lib.beans.v0.JSONPersistenceSession skelSession = 
			new net.sf.okapi.lib.beans.v0.JSONPersistenceSession(Event.class);
		assertFalse(skelSession.isActive());
	}
	
	// DEBUG @Test
	public void testOldPersistenceRoundtrip() throws IOException{
		Event event1 = new Event(EventType.TEXT_UNIT);
		ITextUnit tu1 = TextUnitUtil.buildTU("source-text1" + (char) 2 + '"' + " : " + '"' + 
				'{' + '"' + "ssssss " + ':' + '"' + "ddddd" + "}:" + '<' + '>' + "sssddd: <>dsdd");
		String zipName = this.getClass().getResource("sample1.en.fr.zip").getFile();
		tu1.setSkeleton(new ZipSkeleton(new ZipFile(new File(zipName)), null));
		event1.setResource(tu1);
		tu1.setTarget(LocaleId.FRENCH, new TextContainer("french-text1"));
		tu1.setTarget(LocaleId.TAIWAN_CHINESE, new TextContainer("chinese-text1"));
				
		Event event2 = new Event(EventType.TEXT_UNIT);
		ITextUnit tu2 = TextUnitUtil.buildTU("source-text2" + (char) 2 + '"' + " : " + '"' + 
				'{' + '"' + "ssssss " + ':' + '"' + "ddddd" + "}:" + '<' + '>' + "sssddd: <>dsdd");
		tu2.setSkeleton(new ZipSkeleton(null, new ZipEntry("aa1/content/content.gmx")));
		event2.setResource(tu2);
		tu2.setTarget(LocaleId.FRENCH, new TextContainer("french-text2"));
		tu2.setTarget(LocaleId.TAIWAN_CHINESE, new TextContainer("chinese-text2"));
						
		net.sf.okapi.lib.beans.v0.JSONPersistenceSession skelSession = 
			new net.sf.okapi.lib.beans.v0.JSONPersistenceSession(Event.class);
		
		File tempSkeleton = null;
		tempSkeleton = File.createTempFile("~okapi-62_aaa_", ".txt");
		tempSkeleton.deleteOnExit();
		
		skelSession.start(new FileOutputStream(tempSkeleton));
		
		ArrayList<Event> events = new ArrayList<Event>();
		events.add(event1);
		events.add(event2);
		
		skelSession.serialize(event1);
		skelSession.serialize(event2);
		skelSession.end();
		
		FileInputStream fis = new FileInputStream(tempSkeleton);
		skelSession.start(fis);		
		
		Event event11 = (Event) skelSession.deserialize();
		Event event22 = (Event) skelSession.deserialize();
		
		skelSession.end();
				
		ArrayList<Event> events2 = new ArrayList<Event>();
		events2.add(event11);
		events2.add(event22);
		
		FilterTestDriver.compareEvents(events, events2);
		FilterTestDriver.laxCompareEvents(events, events2);
	}

}
