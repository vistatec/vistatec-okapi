/*===========================================================================
  Copyright (C) 2008-2010 by the Okapi Framework contributors
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

package net.sf.okapi.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import net.sf.okapi.common.LCIDUtil.LCIDDescr;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class LCIDUtilTest {

	@Test
	public void testLCID() {
		assertEquals(0x0419, LCIDUtil.getLCID("ru-ru"));
		assertEquals(0x0419, LCIDUtil.getLCID("ru-RU"));
		assertEquals(0x0419, LCIDUtil.getLCID("ru_RU"));
		assertEquals(0x0409, LCIDUtil.getLCID("en-us"));
		assertEquals(0x044c, LCIDUtil.getLCID("ml-IN"));
		assertEquals("ml-IN", LCIDUtil.getTag(0x044c));
		assertEquals(0x0007, LCIDUtil.getLCID("de"));
		assertEquals(0x0009, LCIDUtil.getLCID("en"));		
		assertEquals(0x000a, LCIDUtil.getLCID("es"));
		assertEquals(0x000c, LCIDUtil.getLCID("fr"));
		assertEquals(0x0448, LCIDUtil.getLCID(new LocaleId("or", "in")));
		assertEquals("or-IN", LCIDUtil.getTag(new LocaleId("or", "in")));
	}
	
	@Test
	public void testLookups() {
//		for (LCIDDescr descr : LCIDUtil.getTagLookup().values()) {
//			if (!LCIDUtil.getLcidLookup().containsKey(descr.tag)) {
//				fail("LcidLookup has no entry for " + descr.tag);
//			}
//		}
		for (LCIDDescr descr : LCIDUtil.getTagLookup().values()) {
			int lcid = descr.lcid;
			String tag = descr.tag;
			if (!LCIDUtil.getLcidLookup().containsKey(tag)) {
				fail(String.format("LcidLookup has no entry for 0x%04x (%s)", lcid, descr.tag));
			}
		}
	}
	
//  DEBUG @Test
//	public void listLcidLookup() {
//		for (String tag : LCIDUtil.getLcidLookup().keySet()) {
//			LCIDDescr descr = LCIDUtil.getLcidLookup().get(tag);
//		}
//	}
	
//	private String getDescrStr(LCIDDescr descr) {
//		return String.format("Lang: %20s Reg: %20s lcid: 0x%04x tag: %s",
//				descr.language, descr.region, descr.lcid, descr.tag);		
//	}
}
