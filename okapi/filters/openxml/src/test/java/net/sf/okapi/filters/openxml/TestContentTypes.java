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
package net.sf.okapi.filters.openxml;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.xml.stream.XMLInputFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestContentTypes {

	@Test
	public void testRels() throws Exception {
		InputStream input = getClass().getResourceAsStream("/Content_Types.xml");
		Reader reader = new InputStreamReader(input, "UTF-8");
		ContentTypes ct = new ContentTypes(XMLInputFactory.newInstance());
		ct.parseFromXML(reader);
		
		// Test defaults
		assertEquals("application/vnd.openxmlformats-package.relationships+xml", 
					 ct.getContentType("/a/b.xml.rels"));
		assertEquals("application/xml", ct.getContentType("/a/b.xml"));

		// Test overrides
		assertEquals("application/vnd.openxmlformats-officedocument.presentationml.slideLayout+xml",
					 ct.getContentType("/ppt/slideLayouts/slideLayout1.xml"));
		assertEquals("application/vnd.openxmlformats-officedocument.presentationml.slideLayout+xml",
				 ct.getContentType("/ppt/slideLayouts/slideLayout2.xml"));
		assertEquals("application/vnd.openxmlformats-officedocument.presentationml.slideLayout+xml",
				 ct.getContentType("/ppt/slideLayouts/slideLayout3.xml"));
		assertEquals("application/vnd.openxmlformats-officedocument.presentationml.slideLayout+xml",
				 ct.getContentType("/ppt/slideLayouts/slideLayout4.xml"));
		assertEquals("application/vnd.openxmlformats-officedocument.presentationml.slideLayout+xml",
				 ct.getContentType("/ppt/slideLayouts/slideLayout5.xml"));
		assertEquals("application/vnd.openxmlformats-officedocument.presentationml.slideLayout+xml",
				 ct.getContentType("/ppt/slideLayouts/slideLayout6.xml"));
		
		assertEquals("application/vnd.openxmlformats-officedocument.presentationml.notesSlide+xml",
					 ct.getContentType("/ppt/notesSlides/notesSlide1.xml"));
		assertEquals("application/vnd.openxmlformats-officedocument.presentationml.notesSlide+xml",
				 ct.getContentType("/ppt/notesSlides/notesSlide2.xml"));
		assertEquals("application/vnd.openxmlformats-officedocument.presentationml.notesSlide+xml",
				 ct.getContentType("/ppt/notesSlides/notesSlide3.xml"));
		assertEquals("application/vnd.openxmlformats-officedocument.presentationml.notesSlide+xml",
				 ct.getContentType("/ppt/notesSlides/notesSlide4.xml"));
		
		assertEquals("application/vnd.openxmlformats-officedocument.presentationml.slideMaster+xml",
				ct.getContentType("/ppt/slideMasters/slideMaster1.xml"));

		assertEquals("application/vnd.openxmlformats-officedocument.presentationml.slide+xml",
				ct.getContentType("/ppt/slides/slide1.xml"));
		assertEquals("application/vnd.openxmlformats-officedocument.presentationml.slide+xml",
				ct.getContentType("/ppt/slides/slide2.xml"));
		assertEquals("application/vnd.openxmlformats-officedocument.presentationml.slide+xml",
				ct.getContentType("/ppt/slides/slide3.xml"));
		assertEquals("application/vnd.openxmlformats-officedocument.presentationml.slide+xml",
				ct.getContentType("/ppt/slides/slide4.xml"));
		assertEquals("application/vnd.openxmlformats-officedocument.presentationml.tableStyles+xml",
				ct.getContentType("/ppt/tableStyles.xml"));

		// Try a couple without the leading slash
		assertEquals("application/vnd.openxmlformats-officedocument.presentationml.presentation.main+xml",
					 ct.getContentType("ppt/presentation.xml"));
		assertEquals("application/vnd.openxmlformats-officedocument.presentationml.presProps+xml",
					 ct.getContentType("ppt/presProps.xml"));
		assertEquals("application/vnd.openxmlformats-officedocument.theme+xml",
					 ct.getContentType("ppt/theme/theme1.xml"));
		assertEquals("application/vnd.openxmlformats-officedocument.theme+xml",
				 ct.getContentType("ppt/theme/theme2.xml"));
		assertEquals("application/vnd.openxmlformats-officedocument.theme+xml",
				 ct.getContentType("ppt/theme/theme3.xml"));

	}
}
