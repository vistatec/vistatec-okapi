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

package org.w3c.its;

import static org.junit.Assert.assertEquals;

import javax.xml.namespace.QName;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class VariableResolverTest {

	@Test
	public void testQuotation () {
		VariableResolver vr = new VariableResolver();
		vr.add(new QName("var1"), "VAR1", false);
		vr.add(new QName("var11"), "Eleven", false);
		vr.add(new QName("var2"), "\"v2\"", false);
		vr.add(new QName("var3"), "'v3'", false);
		assertEquals("\\*[@attr='VAR1']", vr.replaceVariables("\\*[@attr=$var1]"));
		// Test variable with a sub-string name and values with quotes
		assertEquals("a='VAR1' b='Eleven' c='\"v2\"'", vr.replaceVariables("a=$var1 b=$var11 c=$var2"));
		assertEquals("a='Eleven' b='VAR1' c=\"'v3'\"", vr.replaceVariables("a=$var11 b=$var1 c=$var3"));
	}

}
