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

package net.sf.okapi.common.filters;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.RawDocument;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class AbstractBaseFilterTests {

	private LocaleId locEN = LocaleId.fromString("en");
	
	@Test
	public void testMultilingual () {
		try (DummyBaseFilter filter = new DummyBaseFilter()) {
			FilterTestDriver testDriver = new FilterTestDriver();
			testDriver.setDisplayLevel(0);
			testDriver.setShowSkeleton(true);

			filter.open(new RawDocument("1", locEN, locEN));
			if ( !testDriver.process(filter) ) Assert.fail();
			filter.close();

			filter.open(new RawDocument("2", locEN, locEN));
			if ( !testDriver.process(filter) ) Assert.fail();
			filter.close();
		}
		catch ( Throwable e ) {
			e.printStackTrace();
			Assert.fail("Exception occured");
		}
	}
}
