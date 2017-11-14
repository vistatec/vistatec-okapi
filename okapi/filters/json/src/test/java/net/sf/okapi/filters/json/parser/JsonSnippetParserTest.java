/*===========================================================================
  Copyright (C) 2009-2017 by the Okapi Framework contributors
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

package net.sf.okapi.filters.json.parser;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class JsonSnippetParserTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSingleObject() throws IOException, ParseException {
		String snippet = "/*comment*/ //comment\n#comment\n         \"zerokey:\"  {    \n               \"key\":\"value\u0067\"}";
		JsonParser jp = new JsonParser(snippet);
		Token t;
		do {
			t = jp.getNextToken();			
		} while (t.kind != 0);
	}

// Not used
//	@Test
//	public void testTwoObjects() throws IOException, ParseException {
//	}
	
// Not used
//	 private String getSpecialTokenBefore(Token t)
//	  {
//		    List<Token> tl = new ArrayList<Token>();
//
//	    StringBuilder b = new StringBuilder();
//	    if (t.specialToken == null) return null;
//	    // The above statement determines that there are no special tokens
//	    // and returns control to the caller.
//	    Token tmp_t = t.specialToken;
//	    while (tmp_t.specialToken != null) tmp_t = tmp_t.specialToken;
//	    // The above line walks back the special token chain until it
//	    // reaches the first special token after the previous regular
//	    // token.
//	    while (tmp_t != null)
//	    {
//	      b.append(tmp_t.kind+tmp_t.image);
//	      tmp_t = tmp_t.next;
//	    }
//	    // The above loop now walks the special token chain in the forward
//	    // direction printing them in the process.
//	    return b.toString();
//	  }
}
