/*===========================================================================
  Copyright (C) 2017 by the Okapi Framework contributors
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
============================================================================*/

package net.sf.okapi.filters.xliff;

import static org.junit.Assert.*;
import static net.sf.okapi.filters.xliff.SdlXliffConfLevel.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class SdlXliffConfLevelTest {

	@Test
	public void testIsValidConfValue() {
		assertTrue(SdlXliffConfLevel.isValidConfValue("ApprovedSignOff"));
		assertTrue(SdlXliffConfLevel.isValidConfValue("ApprovedTranslation"));
		assertTrue(SdlXliffConfLevel.isValidConfValue("Draft"));
		assertTrue(SdlXliffConfLevel.isValidConfValue("RejectedSignOff"));
		assertTrue(SdlXliffConfLevel.isValidConfValue("RejectedTranslation"));
		assertTrue(SdlXliffConfLevel.isValidConfValue("Translated"));
		assertTrue(SdlXliffConfLevel.isValidConfValue("Unspecified"));
		assertFalse(SdlXliffConfLevel.isValidConfValue("x-sdl-Unspecified"));
	}

	@Test
	public void testFromConfValue() {
		assertEquals(APPROVED_SIGN_OFF, SdlXliffConfLevel.fromConfValue("ApprovedSignOff"));
		assertEquals(APPROVED_TRANSLATION, SdlXliffConfLevel.fromConfValue("ApprovedTranslation"));
		assertEquals(DRAFT, SdlXliffConfLevel.fromConfValue("Draft"));
		assertEquals(REJECTED_SIGN_OFF, SdlXliffConfLevel.fromConfValue("RejectedSignOff"));
		assertEquals(REJECTED_TRANSLATION, SdlXliffConfLevel.fromConfValue("RejectedTranslation"));
		assertEquals(TRANSLATED, SdlXliffConfLevel.fromConfValue("Translated"));
		assertEquals(UNSPECIFIED, SdlXliffConfLevel.fromConfValue("Unspecified"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testFromConfValueInvalid() {
		SdlXliffConfLevel.fromConfValue("x-sdl-Unspecified");
	}

	@Test
	public void testIsValidStateValue() {
		assertTrue(SdlXliffConfLevel.isValidStateValue("x-sdl-ApprovedSignOff"));
		assertTrue(SdlXliffConfLevel.isValidStateValue("x-sdl-ApprovedTranslation"));
		assertTrue(SdlXliffConfLevel.isValidStateValue("x-sdl-Draft"));
		assertTrue(SdlXliffConfLevel.isValidStateValue("x-sdl-RejectedSignOff"));
		assertTrue(SdlXliffConfLevel.isValidStateValue("x-sdl-RejectedTranslation"));
		assertTrue(SdlXliffConfLevel.isValidStateValue("x-sdl-Translated"));
		assertTrue(SdlXliffConfLevel.isValidStateValue("x-sdl-Unspecified"));
		assertFalse(SdlXliffConfLevel.isValidStateValue("Unspecified"));
	}

	@Test
	public void testFromStateValue() {
		assertEquals(APPROVED_SIGN_OFF, SdlXliffConfLevel.fromStateValue("x-sdl-ApprovedSignOff"));
		assertEquals(APPROVED_TRANSLATION, SdlXliffConfLevel.fromStateValue("x-sdl-ApprovedTranslation"));
		assertEquals(DRAFT, SdlXliffConfLevel.fromStateValue("x-sdl-Draft"));
		assertEquals(REJECTED_SIGN_OFF, SdlXliffConfLevel.fromStateValue("x-sdl-RejectedSignOff"));
		assertEquals(REJECTED_TRANSLATION, SdlXliffConfLevel.fromStateValue("x-sdl-RejectedTranslation"));
		assertEquals(TRANSLATED, SdlXliffConfLevel.fromStateValue("x-sdl-Translated"));
		assertEquals(UNSPECIFIED, SdlXliffConfLevel.fromStateValue("x-sdl-Unspecified"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testFromStateValueInvalid() {
		SdlXliffConfLevel.fromStateValue("Unspecified");
	}

}
