/*===========================================================================
  Copyright (C) 2008-2009 by the Okapi Framework contributors
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

import java.net.URISyntaxException;
import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ParametersTest {
	
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
	}

	@After
	public void tearDown() {
	}

	@Test
	public void testLoadParametersFromString () {
		String snippet = "#v1\nparamBool1.b=false\nparamInt1.i=456";
		StringParameters params = new StringParameters();
		params.fromString(snippet);
		assertFalse(params.getBoolean("paramBool1"));
		assertEquals(456, params.getInteger("paramInt1"));
		assertEquals("", params.getString("paramStr1")); // Default
		assertEquals("", params.getString("password1")); // Default
	}

	@Test
	public void testMergeParametersFromString () {
		String snippet1 = "#v1\n"
				+ "paramBool1.b=false\n"
				+ "paramInt1.i=456\n"
				+ "paramString=Initial";
		String snippet2 = "#v1\n"
				+ "paramInt1.i=12345\n"
				+ "paramString=Override\n"
				+ "paramStringAdd=Added\n";
		StringParameters params = new StringParameters();

		params.fromString(snippet1);
		assertFalse(params.getBoolean("paramBool1"));
		assertEquals(456, params.getInteger("paramInt1"));
		assertEquals("Initial", params.getString("paramString"));
		assertEquals("", params.getString("paramStringAdd")); // Default
		assertEquals("", params.getString("password1")); // Default

		params.fromString(snippet2, false);
		assertFalse(params.getBoolean("paramBool1")); // Unchanged
		assertEquals(12345, params.getInteger("paramInt1")); // Override
		assertEquals("Override", params.getString("paramString")); // Override
		assertEquals("Added", params.getString("paramStringAdd")); // Added
		assertEquals("", params.getString("password1")); // Default
	}

	@Test
	public void testWhitespaces () {
		String snippet = "#v1\nparamBool1.b  =  true  \nparamInt1.i  =  456 \nparamStr1  = AB  C  \npassword1=psw";
		StringParameters params = new StringParameters();
		params.fromString(snippet);
		assertTrue(params.getBoolean("paramBool1"));
		assertEquals(456, params.getInteger("paramInt1"));
		assertEquals(" AB  C  ", params.getString("paramStr1"));
		assertEquals("psw", params.getString("password1"));
	}

	@Test
	public void testLoadParametersFromWindowsFile () throws URISyntaxException {
		StringParameters params = new StringParameters();
		URL url = FileLocation.fromClass(ParametersTest.class).in("/ParamTest01.txt").asUrl();
		params.load(url, false);
		assertFalse(params.getBoolean("paramBool1"));
		assertEquals(789, params.getInteger("paramInt1"));
		assertEquals("TestOK", params.getString("paramStr1"));

	}

	@Test
	public void testParameterDescriptor () {
		TestClass ts = new TestClass();
		ParameterDescriptor pd = new ParameterDescriptor("text", ts,
			"displayName", "shortDescription");
		assertEquals("displayName", pd.getDisplayName());
		assertEquals("shortDescription", pd.getShortDescription());
		assertEquals(String.class, pd.getType());
		assertEquals("text", pd.getName());
		assertEquals(ts, pd.getParent());
		assertNotNull(pd.getReadMethod());
		assertNotNull(pd.getWriteMethod());
	}

	@Test
	public void testParametersDescription () {
		TestClass ts = new TestClass();
		ParametersDescription desc = new ParametersDescription(ts);
		desc.add("text", "displayName", "shortDescription");
		desc.add("flag", "Flag", "A flag");
		ParameterDescriptor pd = desc.get("text");
		assertEquals(2, desc.getDescriptors().size());
		assertEquals(pd, desc.getDescriptors().get("text"));
		pd = desc.get("flag");
		assertEquals(pd, desc.getDescriptors().get("flag"));
		assertEquals(boolean.class, pd.getType());
	}
	
	@Test
	public void testGetFromName () {
		IParameters params = new StringParameters();
		params.setString("paramStr1", "qwerty");
		params.setBoolean("paramBool1", false);
		params.setInteger("paramInt1", 98765);
		assertEquals("qwerty", params.getString("paramStr1"));
		assertFalse(params.getBoolean("paramBool1"));
		assertEquals(98765, params.getInteger("paramInt1"));
	}

	@Test
	public void testSetFromName () {
		IParameters params = new StringParameters();
		params.setString("paramStr1", "qwerty");
		params.setBoolean("paramBool1", false);
		params.setInteger("paramInt1", 98765);
		
		assertEquals("qwerty", params.getString("paramStr1"));
		assertFalse(params.getBoolean("paramBool1"));

		params.setString("paramStr1", "newValue");
		params.setBoolean("paramBool1", true);
		
		assertEquals("newValue", params.getString("paramStr1"));
		assertTrue(params.getBoolean("paramBool1"));

		assertEquals(98765, params.getInteger("paramInt1"));
		params.setInteger("paramInt1", 12345678);
		assertEquals(12345678, params.getInteger("paramInt1"));
	}
	
	@Test
	public void testPreserveUnaffectedDefaults() {
		IParameters params = new TestParameters();
		assertTrue(params.getBoolean(TestParameters.FOO));
		assertTrue(params.getBoolean(TestParameters.BAR));
		params.fromString("#v1\nfoo.b = false");
		assertFalse(params.getBoolean(TestParameters.FOO));
		assertTrue(params.getBoolean(TestParameters.BAR));
	}
	
	@Test
	public void testInitializeFromData() {
		IParameters params = new TestParameters("#v1\nfoo.b = false");
		assertFalse(params.getBoolean(TestParameters.FOO));
		assertTrue(params.getBoolean(TestParameters.BAR));
	}

	@Test
	public void testSidesEffect () {
		TestParameters params = new TestParameters();
		
		assertTrue(params.getBoolean(TestParameters.FOO));
		assertTrue(params.getBoolean(TestParameters.BAR));
		params.setBoolean(TestParameters.FOO, false);
		assertFalse(params.getBoolean(TestParameters.FOO));
		assertTrue(params.getBoolean(TestParameters.BAR));
		params.setBoolean(TestParameters.BAR, false);
		assertFalse(params.getBoolean(TestParameters.FOO));
		assertFalse(params.getBoolean(TestParameters.BAR));
	}

	@Test
	public void testClearBufferBeforeLoadingFromString() {
		IParameters params = new TestParameters();
		params.setBoolean("TEST", true);
		params.fromString("#v1\nfoo.b = false");
		assertFalse(params.getBoolean("TEST"));
	}
	
	class TestParameters extends StringParameters {
		static final String FOO = "foo";
		static final String BAR = "bar";
		
		TestParameters() {
			super();
		}
		
		TestParameters(String data) {
			super(data);
		}
		
		@Override
		public void reset() {
			super.reset();
			setBoolean(FOO, true);
			setBoolean(BAR, true);
		}
	}
}
