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

package net.sf.okapi.connectors.moses;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.query.IQuery;
import net.sf.okapi.common.query.QueryResult;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class MosesMTConnectorTest {
	private static IQuery conn;

	@BeforeClass
	public static void setUp() throws Exception {
		conn = new MosesMTConnector();
		conn.setLanguages(LocaleId.FRENCH, LocaleId.ENGLISH);
		conn.open();
	}

	@Test
	public void dummyTest() {			
	}
	
	/*************************************************************
	 Start mosesserver with FR-EN sample model to run these tests
	 http://www.statmt.org/moses_steps.html
	 ************************************************************/
	
	//@Test
	public void plainTextQueryTest() {
		conn.query("Est une petite maison.");
		QueryResult qr = conn.next();
		Assert.assertEquals("Is a small house.", qr.target.toText());
		Assert.assertEquals(95, qr.getFuzzyScore());
		Assert.assertTrue(qr.fromMT());

		conn.query("Les forces armées du Pakistan -- cibles répétées d'attentats suicide -- sont démoralisées.");
		qr = conn.next();
		// FIXME: moses is going to require more cleanup than currently provided
		Assert.assertEquals(
				"Pakistan's armed forces -- repeated targets d'attentats suicide -- have become demoralized.",
				qr.target.toText());
	}

	//@Test
	public void textFragmentQueryTest() {
		TextFragment tf = new TextFragment("Est une ");
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append("petite maison");
		tf.append(TagType.CLOSING, "b", "</b>");
		tf.append(".");
		conn.query(tf);
		QueryResult qr = conn.next();
		Assert.assertEquals("Is a small house.<b></b>", qr.target.toText());
	}
}
