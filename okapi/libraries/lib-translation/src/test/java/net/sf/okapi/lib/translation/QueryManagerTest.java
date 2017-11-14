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

package net.sf.okapi.lib.translation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.query.IQuery;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class QueryManagerTest {

	private QueryManager qm;
	private LocaleId locSrc = LocaleId.fromString("src");
	private LocaleId locTrg = LocaleId.fromString("trg");
	
	@Before
	public void setUp() {
		qm = new QueryManager();
	}

	@Test
	public void testLanguages () {
		qm.setLanguages(locSrc, locTrg);
		assertEquals(locSrc, qm.getSourceLanguage());
		assertEquals(locTrg, qm.getTargetLanguage());
	}

	@Test
	public void testResources () {
		DummyConnector conn = new DummyConnector();
		int resId = qm.addResource(conn, "ResNameTest");
		assertEquals("ResNameTest", qm.getName(resId));
		ResourceItem item = qm.getResource(resId);
		assertNotNull(item);
		IQuery q = qm.getInterface(resId);
		assertNotNull(q);
	}

}
