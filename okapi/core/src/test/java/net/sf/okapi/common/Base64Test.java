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

import java.io.UnsupportedEncodingException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class Base64Test {

	@Test
	public void testSimpleConversion () {
		String text = "This is a test.";
		String res = Base64.encodeString(text);
		assertEquals(text, Base64.decodeString(res));
	}
	
	@Test
	public void testExtendedCharacters () {
		String text = "\u00a0\u00c6\u9151\uff3b\uffed\u2605";
		String res = Base64.encodeString(text);
		assertEquals(text, Base64.decodeString(res));
	}

	@Test
	public void testMixedCharacters () {
		String text = "\u00a0\u00c6<>&%$#@!Abxz\u9151[]\uff3b\uffed\u2605";
		String res = Base64.encodeString(text);
		assertEquals(text, Base64.decodeString(res));
	}

	@Test
	public void testLineBreaks () {
		String text = "\u00a0\n\u00c6\rABC\r\nEnd";
		String res = Base64.encodeString(text);
		assertEquals(text, Base64.decodeString(res));
	}

	@Test
	public void testUTF8Bytes () throws UnsupportedEncodingException {
		String text = "\u00a0\u00c6";
		String res = Base64.encodeString(text);
		byte[] expected = text.getBytes("UTF8");
		byte[] output  = Base64.decode(res);
		assertEquals(expected.length, output.length);
		for ( int i=0; i<expected.length; i++ ) {
			assertEquals(expected[i], output[i]);
		}
	}

	@Test
	public void testLongBlock () {
		String text = "[This is a long text that may need to be wrapped onto several lines.]";
		//String res = Base64.encodeString(text);
		String wrapped = "W1RoaXMgaXMg\r\nYSBsb25nIHRleHQgdGhhdC\rBtYXkgb\n\n\nmVlZCB0byBiZSB3cmFwcGVkIG9udG8gc2V\n"
			+ "2ZXJhbCBsaW5lcy5d";
		assertEquals(text, Base64.decodeString(wrapped));
	}

    @Test(expected = RuntimeException.class)
	public void testBadInput () {
    	// Input not in a length multiple of 4
		Base64.decode("123");
	}

}
