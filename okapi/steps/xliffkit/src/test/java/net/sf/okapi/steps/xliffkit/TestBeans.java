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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.BaseNameable;
import net.sf.okapi.common.resource.BaseReferenceable;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextUnitUtil;
import net.sf.okapi.common.skeleton.GenericSkeleton;
import net.sf.okapi.lib.beans.sessions.OkapiJsonSession;
import net.sf.okapi.lib.persistence.IPersistenceBean;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestBeans {

	@Test
	public void test1() {
		OkapiJsonSession session = new OkapiJsonSession(false);
		IPersistenceBean<BaseNameable> bean = session.createBean(BaseNameable.class);
		BaseNameable bn = new BaseNameable();
		bn.setId("the id");
		bn.setName("the name");
		bn.setType("the type");
		bean.set(bn, session);
		
		BaseNameable bn2 = bean.get(BaseNameable.class, session);
		assertEquals("the id", bn2.getId());
		assertEquals("the name", bn2.getName());
		assertEquals("the type", bn2.getType());
		
		IPersistenceBean<BaseReferenceable> bean2 = session.createBean(BaseReferenceable.class);
		BaseReferenceable br = new BaseReferenceable();
		br.setId("the id");
		br.setName("the name");
		br.setType("the type");
		bean2.set(br, session);
		
//		JSONPersistenceSession session = new JSONPersistenceSession(BaseReferenceableBean.class);
//		session.start((InputStream) null);
		BaseReferenceable br2 = bean2.get(BaseReferenceable.class, session);

		assertEquals("the id", br2.getId());
		assertEquals("the name", br2.getName());
		assertEquals("the type", br2.getType());		
	}
	
	// DEBUG @Test
	public void testObjectStream() throws FileNotFoundException, IOException {		
		File tempF = File.createTempFile("~okapi-56_temp_", null);
		tempF.deleteOnExit();
		ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(tempF));
		
		File outF = File.createTempFile("~okapi-57_temp_", null);
		outF.deleteOnExit();
		OutputStream outStream = new FileOutputStream(outF);
				
		ITextUnit tu1 = TextUnitUtil.buildTU("source-text1" + (char) 2 + '"' + " : " + '"' + 
				'{' + '"' + "ssssss " + ':' + '"' + "ddddd" + "}:" + '<' + '>' + "sssddd: <>dsdd");
		tu1.setSkeleton(new GenericSkeleton());
		tu1.setTarget(LocaleId.FRENCH, new TextContainer("french-text1"));
		tu1.setTarget(LocaleId.TAIWAN_CHINESE, new TextContainer("chinese-text1"));
		
		OkapiJsonSession session = new OkapiJsonSession(false);		
		IPersistenceBean<ITextUnit> tuBean = session.createBean(ITextUnit.class);
		
		os.writeObject(tuBean);
		
		session.start(outStream);
		session.end();
		os.close();
	}
	

}
