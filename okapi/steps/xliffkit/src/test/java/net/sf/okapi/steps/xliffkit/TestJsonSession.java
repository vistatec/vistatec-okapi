/*===========================================================================
  Copyright (C) 2008-2011 by the Okapi Framework contributors
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
import static org.junit.Assert.fail;

import java.io.UnsupportedEncodingException;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.InlineAnnotation;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextUnit;
import net.sf.okapi.common.resource.TextUnitUtil;
import net.sf.okapi.common.skeleton.GenericSkeleton;
import net.sf.okapi.lib.beans.sessions.OkapiJsonSession;
import net.sf.okapi.lib.beans.v1.OkapiBeans;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(JUnit4.class)
public class TestJsonSession {

//	private String toJsonString(IAnnotation annotation) throws UnsupportedEncodingException {
//		OkapiJsonSession session = new OkapiJsonSession();
//		session.setItemClass(IAnnotation.class);
//		session.setItemLabel("annotation");
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//		session.start(baos);
//		session.serialize(annotation);
//		session.end();
//		
//		return new String(baos.toByteArray(), "UTF-8");		
//	}
	
	private void log(String str) {
		Logger localLogger = LoggerFactory.getLogger(getClass()); // loggers are cached
		localLogger.debug(str);
	}
	
	// DEBUG 
	@Test
	public void testReadWriteObject() throws UnsupportedEncodingException {
		OkapiJsonSession session = new OkapiJsonSession(false);		
		session.setVersion(OkapiBeans.VERSION);
		
		log("===== Annotation");
		InlineAnnotation annot1 = new InlineAnnotation();
		annot1.setData("test inline annotation");
		String st1 = session.writeObject(annot1);
		log(st1 + "\n\n");
		
		log("===== TextUnit");
		ITextUnit tu1 = TextUnitUtil.buildTU("source-text1" + (char) 2 + '"' + " : " + '"' + 
				'{' + '"' + "ssssss " + ':' + '"' + "ddddd" + "}:" + '<' + '>' + "sssddd: <>dsdd");
		//tu1.setSkeleton(new GenericSkeleton());
		//------------
		GenericSkeleton gs = new GenericSkeleton("before");
		//--ClassCastException if using addContentPlaceholder
		gs.addContentPlaceholder(tu1);
		gs.append("after");
		
		log(gs.toString());
		
		tu1.setSkeleton(gs);
		//------------
		tu1.setTarget(LocaleId.FRENCH, new TextContainer("french-text1"));
		tu1.setTarget(LocaleId.TAIWAN_CHINESE, new TextContainer("chinese-text1"));
		String st2 = session.writeObject(tu1);
		log(st2);
		
		
		session.setVersion(OkapiBeans.VERSION);
		InlineAnnotation annot2 = session.readObject(st1, InlineAnnotation.class);
		assertEquals(annot1.getData(), annot2.getData());
		
		ITextUnit tu2 = session.readObject(st2, TextUnit.class);
		assertEquals(tu1.getSource().toString(), tu2.getSource().toString());
		
		// Wrong version
		session.setVersion("OKAPI 0.0");
		try {
			annot2 = session.readObject(st1, InlineAnnotation.class);
			assertEquals(annot1.getData(), annot2.getData());
			
			tu2 = session.readObject(st2, TextUnit.class);
			assertEquals(tu1.getSource().toString(), tu2.getSource().toString());
		} catch (RuntimeException e) {
			return;
		}
		fail("RuntimeException should have been thrown");
	}
	
}
