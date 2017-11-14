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

package net.sf.okapi.common.filterwriter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import net.sf.okapi.common.annotation.GenericAnnotation;
import net.sf.okapi.common.annotation.GenericAnnotationType;
import net.sf.okapi.common.annotation.GenericAnnotations;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.TextUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ITSContentTest {

	@Test
	public void extendedMatchTest () {
		assertTrue(ITSContent.isExtendedMatch("*-CH", "de-CH"));
		assertTrue(ITSContent.isExtendedMatch("*-CH", "fr-ch"));
		assertTrue(ITSContent.isExtendedMatch("de-*-DE, fr", "de-de"));
		assertTrue(ITSContent.isExtendedMatch("fr, de-*-DE", "de-Latn-de"));
		assertTrue(ITSContent.isExtendedMatch("za ,  de-*-DE , po-pl  ", "de-DE-x-goethe"));
		assertTrue(ITSContent.isExtendedMatch("za, de-*-DE,   po-pl", "de-DE"));
		
		assertTrue(ITSContent.isExtendedMatch("za, de-*, po-pl", "de"));
		assertTrue(ITSContent.isExtendedMatch("de-*, de", "de"));
		
		assertTrue(ITSContent.isExtendedMatch("de-*-DE", "de-DE"));
		assertTrue(ITSContent.isExtendedMatch("de-*-DE", "de-de"));
		assertTrue(ITSContent.isExtendedMatch("de-*-DE", "de-Latn-DE"));
		assertTrue(ITSContent.isExtendedMatch("de-*-DE", "de-Latf-DE"));
		assertTrue(ITSContent.isExtendedMatch("de-*-DE", "de-DE-x-goethe"));
		assertTrue(ITSContent.isExtendedMatch("de-*-DE", "de-Latn-DE-1996"));
		assertTrue(ITSContent.isExtendedMatch("de-*-DE", "de-Deva-DE"));
		
		assertFalse(ITSContent.isExtendedMatch("de-*-DE", "de"));
		assertFalse(ITSContent.isExtendedMatch("de-*-DE", "de-x-DE"));
		assertFalse(ITSContent.isExtendedMatch("de-*-DE", "de-Deva"));

		assertFalse(ITSContent.isExtendedMatch("", "fr"));
		assertTrue(ITSContent.isExtendedMatch("*", "fr"));
	}

	@Test
	public void testGetAnnotatorRef () {
		ITextUnit tu = new TextUnit("id");
		GenericAnnotation ann = new GenericAnnotation(GenericAnnotationType.ANNOT,
			GenericAnnotationType.ANNOT_VALUE, "translate|uri1"); 
		GenericAnnotations.addAnnotations(tu, new GenericAnnotations(ann));
		assertEquals("uri1", ITSContent.getAnnotatorRef("translate", tu));
		assertNull(ITSContent.getAnnotatorRef("invalidDataCategory", tu));
		
		ann.setString(GenericAnnotationType.ANNOT_VALUE, "mt-confidence|uri2 ");
		assertEquals("uri2", ITSContent.getAnnotatorRef("mt-confidence", tu));

		ann.setString(GenericAnnotationType.ANNOT_VALUE, "mt-confidence|uri3 text-analysis|uri4 translate|uri5");
		assertEquals("uri3", ITSContent.getAnnotatorRef("mt-confidence", tu));
		assertEquals("uri4", ITSContent.getAnnotatorRef("text-analysis", tu));
		assertEquals("uri5", ITSContent.getAnnotatorRef("translate", tu));

		ann.setString(GenericAnnotationType.ANNOT_VALUE, "domain| text-analysis|");
		assertEquals("", ITSContent.getAnnotatorRef("domain", tu));
		assertEquals("", ITSContent.getAnnotatorRef("text-analysis", tu));
	}
}
