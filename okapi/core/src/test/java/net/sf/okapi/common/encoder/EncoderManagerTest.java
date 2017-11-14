/*===========================================================================
  Copyright (C) 2010 by the Okapi Framework contributors
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

package net.sf.okapi.common.encoder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import net.sf.okapi.common.MimeTypeMapper;
import net.sf.okapi.common.Util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class EncoderManagerTest {

	final private String XMLENCODER = "net.sf.okapi.common.encoder.XMLEncoder";
	final private String HTMLENCODER = "net.sf.okapi.common.encoder.HtmlEncoder";
	
	@Test
	public void testSimpleMapping () {
		EncoderManager em = new EncoderManager();
		em.setMapping("mimetype1", XMLENCODER);
		em.setDefaultOptions(null, "UTF-8", Util.LINEBREAK_UNIX);

		assertNull(em.getEncoder());
		em.updateEncoder("mimetype1");
		IEncoder enc = em.getEncoder();
		assertNotNull(enc);
		assertEquals(XMLENCODER, enc.getClass().getName());
	}
	
	@Test
	public void testMergeMappings () {
		EncoderManager em1 = new EncoderManager();
		em1.setMapping("mimetype1", XMLENCODER);
		em1.setDefaultOptions(null, "UTF-8", Util.LINEBREAK_UNIX);
		EncoderManager em2 = new EncoderManager();
		em2.setMapping("mimetype2", HTMLENCODER);
		em2.setDefaultOptions(null, "UTF-8", Util.LINEBREAK_UNIX);

		em1.mergeMappings(em2);
		em1.updateEncoder("mimetype2");
		IEncoder enc = em1.getEncoder();
		assertNotNull(enc);
		assertEquals(HTMLENCODER, enc.getClass().getName());
	}
	
	@Test
	public void testSetMapping() {
		EncoderManager em = new EncoderManager();
		em.setDefaultOptions(null, "UTF-16BE", "\r\n");
		
		IEncoder e1 = new XMLEncoder("UTF-8", "\n", true, true, false, QuoteMode.ALL);
		IEncoder e2 = new XMLEncoder("UTF-8", "\n", true, false, true, QuoteMode.NUMERIC_SINGLE_QUOTES);
		
		em.setMapping(MimeTypeMapper.XML_MIME_TYPE, e1);
		em.updateEncoder(MimeTypeMapper.XML_MIME_TYPE);
		assertEquals(e1, em.getEncoder());
		
		em.setMapping(MimeTypeMapper.XML_MIME_TYPE, e2);
		em.updateEncoder(MimeTypeMapper.XML_MIME_TYPE);
		assertEquals(e2, em.getEncoder());
	}
	
}
