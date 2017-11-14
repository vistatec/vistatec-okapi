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

package net.sf.okapi.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import net.sf.okapi.common.uidescription.CheckboxPart;
import net.sf.okapi.common.uidescription.ListSelectionPart;
import net.sf.okapi.common.uidescription.PathInputPart;
import net.sf.okapi.common.uidescription.TextInputPart;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class UIDescriptionTest {
	
	private TestClass ts;
	private ParametersDescription desc;

	class TestClass {
		private String text;
		private boolean flag;
		public String getText() {
			return text;
		}
		public void setText(String text) {
			this.text = text;
		}
		public boolean isFlag() {
			return flag;
		}
		public void setFlag(boolean flag) {
			this.flag = flag;
		}
	}
	
	@Before
	public void setUp() throws Exception {
		ts = new TestClass();
		desc = new ParametersDescription(ts);
		desc.add("text", "displayName", "shortDescription");
		desc.add("flag", "Flag", "A flag");
	}

	@After
	public void tearDown() {
	}

	@Test
	public void testCheckboxPart () {
		// Using a boolean
		CheckboxPart part = new CheckboxPart(desc.get("flag"));
		assertEquals("flag", part.getName());
		assertEquals("Flag", part.getDisplayName());
		assertEquals("A flag", part.getShortDescription());
		// Using a String
		part = new CheckboxPart(desc.get("text"));
		assertEquals("text", part.getName());
		assertEquals("displayName", part.getDisplayName());
		assertEquals("shortDescription", part.getShortDescription());
	}
	
	@Test
	public void testTextInputPart () {
		TextInputPart part = new TextInputPart(desc.get("text"));
		part.setPassword(true);
		assertEquals("text", part.getName());
		assertEquals("displayName", part.getDisplayName());
		assertEquals("shortDescription", part.getShortDescription());
		assertFalse(part.isAllowEmpty());
		assertTrue(part.isPassword());
	}

	@Test
	public void testPathInputPart () {
		PathInputPart part = new PathInputPart(desc.get("text"), "title", true);
		assertEquals("text", part.getName());
		assertEquals("displayName", part.getDisplayName());
		assertEquals("shortDescription", part.getShortDescription());
		assertEquals("title", part.getBrowseTitle());
		assertTrue(part.isForSaveAs());
	}

	@Test
	public void testListSelectionPart () {
		String[] items = {"selection1", "selection2"};
		ListSelectionPart part = new ListSelectionPart(desc.get("text"), items);
		assertEquals("text", part.getName());
		assertEquals("displayName", part.getDisplayName());
		assertEquals("shortDescription", part.getShortDescription());
		String[] choices = part.getChoicesValues();
		assertNotNull(choices);
		assertEquals(2, choices.length);
		assertEquals("selection2", choices[1]);
	}

}
