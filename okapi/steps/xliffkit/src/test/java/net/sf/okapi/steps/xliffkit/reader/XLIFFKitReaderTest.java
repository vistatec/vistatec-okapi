/*===========================================================================
  Copyright (C) 2009 by the Okapi Framework contributors
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

package net.sf.okapi.steps.xliffkit.reader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.lib.beans.sessions.OkapiJsonSession;
import net.sf.okapi.lib.beans.v0.TestEvent;
import net.sf.okapi.lib.beans.v0.TestEventBean;
import net.sf.okapi.lib.beans.v0.TestEventBean2;
import net.sf.okapi.lib.extra.pipelinebuilder.XBatch;
import net.sf.okapi.lib.extra.pipelinebuilder.XBatchItem;
import net.sf.okapi.lib.extra.pipelinebuilder.XParameter;
import net.sf.okapi.lib.extra.pipelinebuilder.XPipeline;
import net.sf.okapi.lib.extra.pipelinebuilder.XPipelineStep;
import net.sf.okapi.lib.extra.steps.EventLogger;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@SuppressWarnings("unused")
@RunWith(JUnit4.class)
public class XLIFFKitReaderTest {
	
	private static final LocaleId ENUS = new LocaleId("en", "us");
	private static final LocaleId FRFR = new LocaleId("fr", "fr");
	private static final LocaleId DEDE = new LocaleId("de", "de");
//	private static final LocaleId ITIT = new LocaleId("it", "it");
	
	// DEBUG 		
	//@Test
	public void testReader() throws URISyntaxException {
		
		new XPipeline(
				"Test pipeline for XLIFFKitReaderStep",
				new XBatch(
						new XBatchItem(
								this.getClass().getResource("testPackageFormat.xliff.kit").toURI(),
								"UTF-8",
								new File(Util.getTempDirectory() + "/testPackageFormat").toURI(),
								"UTF-8",
								ENUS,
								ENUS)
						),
				new XLIFFKitReaderStep()
				,				
				new EventLogger()
//				,
//				
//				new FilterEventsToRawDocumentStep()
		).execute();
	}
	
	// DEBUG 		
	@Test
	public void testReader2() throws URISyntaxException {
		
		new XPipeline(
				"Test pipeline for XLIFFKitReaderStep",
				new XBatch(
						new XBatchItem(
								this.getClass().getResource("testPackageFormat2.xliff.kit").toURI(),
								"UTF-8",
								new File(Util.getTempDirectory() + "/testPackageFormat2").toURI(),
								"UTF-8",
								ENUS,
								DEDE)
						),
				new XLIFFKitReaderStep()
				,				
				new EventLogger()
//				,
//				
//				new FilterEventsToRawDocumentStep()
		).execute();
	}

	// DEBUG 		
	@Ignore("OkapiMergeException: Added Codes in target='</w:t></w:r>,<w:r><w:rPr><w:b/><w:bCs/></w:rPr><w:t>'")
	public void testReader4() throws URISyntaxException {
		
		new XPipeline(
				"Test pipeline for XLIFFKitReaderStep",
				new XBatch(
						new XBatchItem(
								this.getClass().getResource("testPackageFormat4.xliff.kit").toURI(),
								"UTF-8",
								new File(Util.getTempDirectory() + "/testPackageFormat4").toURI(),
								"UTF-8",
								ENUS,
								ENUS)
						),
				new XLIFFKitReaderStep()
				,				
				new EventLogger()
//				,
//				
//				new FilterEventsToRawDocumentStep()
		).execute();
	}

	// DEBUG 		
	@Test
	public void testReader5() throws URISyntaxException {
		
		new XPipeline(
				"Test pipeline for XLIFFKitReaderStep",
				new XBatch(
						new XBatchItem(
								this.getClass().getResource("testPackageFormat5.xliff.kit").toURI(),
								"UTF-8",
								new File(Util.getTempDirectory() + "/testPackageFormat4").toURI(),
								"UTF-8",
								ENUS,
								ENUS)
						),
				new XLIFFKitReaderStep()
				,				
				new EventLogger()
//				,
//				
//				new FilterEventsToRawDocumentStep()
		).execute();
	}

	// DEBUG 		
	@Test
	public void testReader6() throws URISyntaxException {
		
		new XPipeline(
				"Test pipeline for XLIFFKitReaderStep",
				new XBatch(
						new XBatchItem(
								this.getClass().getResource("testPackageFormat6.xliff.kit").toURI(),
								"UTF-8",
								new File(Util.getTempDirectory() + "/testPackageFormat2").toURI(),
								"UTF-8",
								ENUS,
								FRFR)
						),
				new XLIFFKitReaderStep()
				,				
				new EventLogger()
//				,
//				
//				new FilterEventsToRawDocumentStep()
		).execute();
	}
	
	// DEBUG 		
	@Test
	public void testReader7() throws URISyntaxException {
		
		new XPipeline(
				"Test pipeline for XLIFFKitReaderStep",
				new XBatch(
						new XBatchItem(
								this.getClass().getResource("testPackageFormat7.xliff.kit").toURI(),
								"UTF-8",
								new File(Util.getTempDirectory() + "/testPackageFormat2").toURI(),
								"UTF-8",
								ENUS,
								FRFR)
						),
				new XLIFFKitReaderStep()
				,				
				new EventLogger()
//					,
//					
//					new FilterEventsToRawDocumentStep()
		).execute();
	}
	
	@Test
	public void testReader8() throws URISyntaxException {
		
		new XPipeline(
				"Test pipeline for XLIFFKitReaderStep",
				new XBatch(
						new XBatchItem(
								this.getClass().getResource("test07-subfilter.json_en.fr.xliff.kit").toURI(),
								"UTF-8",
								new File(Util.getTempDirectory() + "/testPackageFormat8").toURI(),
								"UTF-8",
								ENUS,
								FRFR)
						),
				new XPipelineStep(
						new XLIFFKitReaderStep(),
						new XParameter("generateTargets", true),
						new XParameter("updateApprovedFlag", false))
				,				
				new EventLogger()
//				,
//				
//				new FilterEventsToRawDocumentStep()
		).execute();
	}
	
	@Test
	public void testReader9() throws URISyntaxException {
		
		new XPipeline(
				"Test pipeline for XLIFFKitReaderStep",
				new XBatch(
						new XBatchItem(
								this.getClass().getResource("Manual-12-AltTrans.xlf_en.fr.xliff.kit").toURI(),
								"UTF-8",
								new File(Util.getTempDirectory() + "/testPackageFormat9").toURI(),
								"UTF-8",
								ENUS,
								FRFR)
						),
				new XPipelineStep(
						new XLIFFKitReaderStep(),
						new XParameter("generateTargets", true),
						new XParameter("updateApprovedFlag", false))
				,				
				new EventLogger()
//				,
//				
//				new FilterEventsToRawDocumentStep()
		).execute();
	}
	
	// DEBUG 	@Test
	public void testReferences() {
		
		new XPipeline(
				"Test pipeline for XLIFFKitReaderStep",
				new XBatch(
						new XBatchItem(
								this.getClass().getResource("testReferences.xliff.kit"),
								"UTF-8",
								Util.getTempDirectory() + "/testReferences.xliff.kit",
								"UTF-8",
								ENUS,
								ENUS)
						),
				new XPipelineStep(
						new XLIFFKitReaderStep(),
						new XParameter("generateTargets", false))
				,				
				new EventLogger()
//				,
//				
//				new FilterEventsToRawDocumentStep()
		).execute();
	}

// DEBUG @Test
	public void testReferences2() {
		
		new XPipeline(
				"Test pipeline for XLIFFKitReaderStep",
				new XBatch(
						new XBatchItem(
								this.getClass().getResource("testReferences2.xliff.kit"),
								"UTF-8",
								Util.getTempDirectory() + "/testReferences2.xliff.kit",
								"UTF-8",
								ENUS,
								ENUS)
						),
				new XPipelineStep(
						new XLIFFKitReaderStep(),
						new XParameter("generateTargets", false))
				,				
				new EventLogger()
//				,
//				
//				new FilterEventsToRawDocumentStep()
		).execute();
	}
	
	// DEBUG 		
	@Test
	public void testReferences3() {
		
		OkapiJsonSession session = new OkapiJsonSession(false);
		session.setItemClass(TestEvent.class);
		session.setItemLabel("event");
				
		InputStream inStream = this.getClass().getResourceAsStream("test_refs3.txt.json"); 
		session.start(inStream);
		session.registerBean(TestEvent.class, TestEventBean.class);
		
		TestEvent sd = session.deserialize(TestEvent.class); // StartDocument
		
		TestEvent e1 = session.deserialize(TestEvent.class);
		TestEvent e2 = session.deserialize(TestEvent.class);
		
		assertTrue("e1".equals(e1.getId()));
		assertTrue("e2".equals(e2.getId()));
		
		assertTrue("e2".equals(e1.getParent().getId()));
		assertTrue("e1".equals(e2.getParent().getId()));
		
		TestEvent ed = session.deserialize(TestEvent.class); // Ending
		TestEvent e4 = session.deserialize(TestEvent.class);
		assertNull(e4);
		TestEvent e5 = session.deserialize(TestEvent.class);
		assertNull(e5);
		TestEvent e6 = session.deserialize(TestEvent.class);
		assertNull(e6);
		session.end();
	}
	
	@Test
	public void testReferences4() {
		
		OkapiJsonSession session = new OkapiJsonSession(false);
		session.setItemClass(TestEvent.class);
		session.setItemLabel("event");
				
		InputStream inStream = this.getClass().getResourceAsStream("test_refs4.txt.json"); 
		session.start(inStream);
		session.registerBean(TestEvent.class, TestEventBean.class);
		TestEvent sd = session.deserialize(TestEvent.class); // StartDocument
		
		TestEvent e1 = session.deserialize(TestEvent.class);
		TestEvent e2 = session.deserialize(TestEvent.class);
		TestEvent e3 = session.deserialize(TestEvent.class);
		TestEvent e4 = session.deserialize(TestEvent.class);
		TestEvent e5 = session.deserialize(TestEvent.class);
		TestEvent e6 = session.deserialize(TestEvent.class);
		TestEvent e7 = session.deserialize(TestEvent.class);
		
		assertTrue("e1".equals(e1.getId()));
		assertTrue("e2".equals(e2.getId()));
		assertTrue("e3".equals(e3.getId()));
		assertTrue("e4".equals(e4.getId()));
		assertTrue("e5".equals(e5.getId()));
		assertTrue("e6".equals(e6.getId()));
		assertTrue("e7".equals(e7.getId()));
		
		assertTrue("e3".equals(e1.getParent().getId()));
		assertTrue("e4".equals(e3.getParent().getId()));
		assertTrue("e6".equals(e2.getParent().getId()));
		assertTrue("e6".equals(e7.getParent().getId()));
		assertTrue("e2".equals(e5.getParent().getId()));
		
		TestEvent ed = session.deserialize(TestEvent.class); // Ending
		TestEvent e8 = session.deserialize(TestEvent.class);
		assertNull(e8);
		TestEvent e9 = session.deserialize(TestEvent.class);
		assertNull(e9);
		TestEvent e10 = session.deserialize(TestEvent.class);
		assertNull(e10);
		session.end();
	}
		
	@Test
	public void testReferences5() {
		
		OkapiJsonSession session = new OkapiJsonSession(false);
		session.setItemClass(TestEvent.class);
		session.setItemLabel("event");
				
		InputStream inStream = this.getClass().getResourceAsStream("test_refs5.txt.json"); 
		session.start(inStream);
		session.registerBean(TestEvent.class, TestEventBean2.class);
		TestEvent sd = session.deserialize(TestEvent.class); // StartDocument
		
		TestEvent e1 = session.deserialize(TestEvent.class);
		TestEvent e2 = session.deserialize(TestEvent.class);
		
		assertEquals("e1", e1.getId());
		assertEquals("e2", e2.getId());
		
		TestEvent p1 = e1.getParent();
		TestEvent p2 = e2.getParent();
		
		assertEquals(e2, p1);
		assertEquals(e1, p2);
		
		TestEvent ed = session.deserialize(TestEvent.class); // Ending
		TestEvent e8 = session.deserialize(TestEvent.class);
		assertNull(e8);
		TestEvent e9 = session.deserialize(TestEvent.class);
		assertNull(e9);
		TestEvent e10 = session.deserialize(TestEvent.class);
		assertNull(e10);
		session.end();
	}
	
	@Test
	public void testReferences6() {
		
		OkapiJsonSession session = new OkapiJsonSession(false);
		session.setItemClass(TestEvent.class);
		session.setItemLabel("event");
				
		InputStream inStream = this.getClass().getResourceAsStream("test_refs6.txt.json"); 
		session.start(inStream);
		session.registerBean(TestEvent.class, TestEventBean2.class);
		TestEvent sd = session.deserialize(TestEvent.class); // StartDocument
		
		TestEvent e1 = session.deserialize(TestEvent.class);
		TestEvent e2 = session.deserialize(TestEvent.class);
		TestEvent e3 = session.deserialize(TestEvent.class);
		TestEvent e4 = session.deserialize(TestEvent.class);
		TestEvent e5 = session.deserialize(TestEvent.class);
		TestEvent e6 = session.deserialize(TestEvent.class);
		TestEvent e7 = session.deserialize(TestEvent.class);
		
		assertEquals("e1", e1.getId());
		assertEquals("e2", e2.getId());
		assertEquals("e3", e3.getId());
		assertEquals("e4", e4.getId());
		assertEquals("e5", e5.getId());
		assertEquals("e6", e6.getId());
		assertEquals("e7", e7.getId());
		
		TestEvent p1 = e1.getParent();
		TestEvent p2 = e2.getParent();
		TestEvent p3 = e3.getParent();
		TestEvent p4 = e4.getParent();
		TestEvent p5 = e5.getParent();
		TestEvent p6 = e6.getParent();
		TestEvent p7 = e7.getParent();
		
		assertEquals(e3, p1);
		assertEquals(e6, p2);
		assertEquals(e4, p3);
		assertEquals(null, p4);
		assertEquals(e2, p5);
		assertEquals(null, p6);
		assertEquals(e6, p7);
		
		TestEvent ed = session.deserialize(TestEvent.class); // Ending
		TestEvent e8 = session.deserialize(TestEvent.class);
		assertNull(e8);
		TestEvent e9 = session.deserialize(TestEvent.class);
		assertNull(e9);
		TestEvent e10 = session.deserialize(TestEvent.class);
		assertNull(e10);
		session.end();
	}
}
