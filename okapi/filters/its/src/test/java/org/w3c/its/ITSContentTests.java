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
===========================================================================*/

package org.w3c.its;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import net.sf.okapi.common.filterwriter.ITSContent;

public class ITSContentTests {

	@Test
	public void testAnnotatorsRef () {
		assertEquals("abc", ITSContent.annotatorsRefToMap("translate|abc").get("translate"));
		assertEquals("abc", ITSContent.annotatorsRefToMap("   \t\n\rtranslate|abc").get("translate"));
		assertEquals("abc", ITSContent.annotatorsRefToMap("translate|abc  \t\n").get("translate"));

		assertEquals(null, ITSContent.annotatorsRefToMap("translate \n\r\t |abc").get("translate"));
		assertEquals(null, ITSContent.annotatorsRefToMap("translate| \n\n\r abc").get("translate"));
	}

}
