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

package net.sf.okapi.steps.xliffkit;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextUnitUtil;
import net.sf.okapi.common.skeleton.ZipSkeleton;
import net.sf.okapi.lib.beans.sessions.OkapiJsonSession;
import net.sf.okapi.lib.beans.v1.EventBean;
import net.sf.okapi.lib.beans.v1.InputStreamBean;
import net.sf.okapi.lib.beans.v1.TextUnitBean;
import net.sf.okapi.lib.beans.v1.ZipSkeletonBean;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@RunWith(JUnit4.class)
public class TestJackson {

//	private static final String fileName = "test3.txt";
	private ObjectMapper mapper;
	private OkapiJsonSession session;
	
	@Before
	public void setUp() {
		mapper = new ObjectMapper();
		
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true); 
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		//mapper.configure(DeserializationConfig.Feature.USE_ANNOTATIONS, false);
		mapper.configure(Feature.AUTO_CLOSE_SOURCE, false);
		mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
		session = new OkapiJsonSession(false);
	}

	@Test // Make sure we have at least one test to avoid build errors
	public void testDescription () {
		session.setDescription("abc");
		assertEquals("abc", session.getDescription());
	}
	
	private void log(String str) {
		Logger localLogger = LoggerFactory.getLogger(getClass()); // loggers are cached
		localLogger.debug(str);
	}
	
	// DEBUG @Test
	public void testTextUnit() throws JsonGenerationException, JsonMappingException, IOException, URISyntaxException {
		Event event = new Event(EventType.TEXT_UNIT);
		//TextUnit tu = TextUnitUtil.buildTU("source", "skeleton");
		ITextUnit tu = TextUnitUtil.buildTU("source-text" + (char) 2 + '"' + " : " + '"' + 
				'{' + '"' + "ssssss " + ':' + '"' + "ddddd" + "}:" + '<' + '>' + "sssddd: <>dsdd");
		tu.setSkeleton(new ZipSkeleton(null, new ZipEntry("")));
		event.setResource(tu);
		tu.setTarget(LocaleId.FRENCH, new TextContainer("french-text"));
		tu.setTarget(LocaleId.TAIWAN_CHINESE, new TextContainer("chinese-text"));

		TextUnitBean tub = new TextUnitBean();
		tub.set(tu, session);
		
		EventBean evb = new EventBean();
		evb.set(event, session);
		String st = mapper.writeValueAsString(evb);
		log(st);
		
		st = mapper.writeValueAsString(tub);
		tub = mapper.readValue(st, TextUnitBean.class);
		tu = tub.get(ITextUnit.class, session);
	}
	
	
	
	// DEBUG @Test
	public void testRawDocument() throws JsonGenerationException, JsonMappingException, IOException {
		Event event = new Event(EventType.RAW_DOCUMENT);
		event.setResource(new RawDocument("raw doc", LocaleId.ENGLISH));
		EventBean evb = new EventBean();
		evb.set(event, session);
		String st = mapper.writeValueAsString(evb);
		log(st);
	}
	
	// DEBUG @Test
	public void testMultipleRead1() throws IOException {
		OkapiJsonSession skelSession = new OkapiJsonSession(false);
		
		File tempSkeleton = null;
		tempSkeleton = File.createTempFile("~okapi-58_aaa_", ".txt");
		tempSkeleton.deleteOnExit();
		
		skelSession.start(new FileOutputStream(tempSkeleton));
		String st1 = "string1";
		String st2 = "string2";
		skelSession.serialize(st1);
		skelSession.serialize(st2);
		skelSession.end();
		
		FileInputStream fis = new FileInputStream(tempSkeleton);
		skelSession.start(fis);
		
		skelSession.end();
	}
	
	// DEBUG @Test
	public void testMultipleRead2() throws IOException {
		OkapiJsonSession skelSession = new OkapiJsonSession(false);
		
		File tempSkeleton = null;
		tempSkeleton = File.createTempFile("~okapi-59_aaa_", ".txt");
		tempSkeleton.deleteOnExit();
		
		skelSession.start(new FileOutputStream(tempSkeleton));
		Object st1 = new Object();
		Object st2 = new Object();
		
		List<Object> list = new ArrayList<Object> ();
		list.add(st1);
		list.add(st2);
		
		skelSession.serialize(list);
		skelSession.end();
		
		skelSession.start(new FileInputStream(tempSkeleton));
		
		skelSession.end();
	}
	
	// DEBUG @Test
	public void testInputStream() {
		InputStream is = this.getClass().getResourceAsStream("test3.txt");
		log(is.markSupported()? "true" : "false");
		String st = "";
		try {
			st = mapper.writeValueAsString(is);
		} catch (JsonGenerationException e) {
			throw new OkapiException(e);
		} catch (JsonMappingException e) {
			throw new OkapiException(e);
		} catch (IOException e) {
			throw new OkapiException(e);
		}
		log(st);
	}
	
	
	// DEBUG @Test
	public void testZipSkeleton() throws URISyntaxException, IOException {
		ZipFile zf = null;
				zf = new ZipFile(new File(this.getClass().getResource("sample1.en.fr.zip").toURI()));
		ZipSkeleton zs = new ZipSkeleton(zf, null);
		ZipSkeletonBean zsb = new ZipSkeletonBean();
		zsb.set(zs, session);
		String st = mapper.writeValueAsString(zsb);
		log(st);
		zf.close();
	}

	// DEBUG @Test
	public void testInputStreamBean() throws URISyntaxException, JsonGenerationException, JsonMappingException, IOException {
		FileInputStream fis = new FileInputStream(new File(this.getClass().getResource("test3.txt").toURI()));
		InputStreamBean isb = new InputStreamBean();
		isb.set(fis, session);
		String st = mapper.writeValueAsString(isb);
		log(st);
	}
	
	// DEBUG @Test
	public void testPersistenceRoundtrip() throws IOException {
	
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
		
		tu1.getSource().append("part1");
		tu1.getSource().getSegments().append(new Segment("segId1", new TextFragment("seg1")));
		tu1.getSource().append("part2");
		tu1.getSource().getSegments().append(new Segment("segId2", new TextFragment("seg2")));
				
		OkapiJsonSession skelSession = new OkapiJsonSession(false);
		
		File tempSkeleton = null;
		tempSkeleton = File.createTempFile("~okapi-60_aaa_", ".txt");
		tempSkeleton.deleteOnExit();
		
		skelSession.start(new FileOutputStream(tempSkeleton));
		
		ArrayList<Event> events = new ArrayList<Event>();
		events.add(event1);
		events.add(event2);
		
		skelSession.serialize(events);
		skelSession.end();		
	}
	
	// DEBUG @Test
	public void testMultipleObject() throws IOException {
	
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
		
		tu1.getSource().append("part1");
		tu1.getSource().getSegments().append(new Segment("segId1", new TextFragment("seg1")));
		tu1.getSource().append("part2");
		tu1.getSource().getSegments().append(new Segment("segId2", new TextFragment("seg2")));
				
		OkapiJsonSession skelSession = new OkapiJsonSession(false);
		
		File tempSkeleton = null;
		tempSkeleton = File.createTempFile("~okapi-61_aaa_", ".txt");
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
		
		Event event11 = skelSession.deserialize(Event.class);
		Event event22 = skelSession.deserialize(Event.class);
		
		skelSession.end();
				
		ArrayList<Event> events2 = new ArrayList<Event>();
		events2.add(event11);
		events2.add(event22);
	}
	
	// DEBUG @Test
	public void testDeserialization() {
		
		// test1.txt -- created by old beans from new core, reading to new core
		OkapiJsonSession skelSession = new OkapiJsonSession(false);
		skelSession.start(this.getClass().getResourceAsStream("test1.txt"));		
				
		skelSession.end();
		
		// test2.txt -- created by new beans from new core, reading to new core
		skelSession.start(this.getClass().getResourceAsStream("test2.txt"));		
		
		skelSession.end();
		
		// test4.txt -- created by old beans from old core, reading to new core  
		skelSession.start(this.getClass().getResourceAsStream("test4.txt"));		
		
		skelSession.end();
	}
}
