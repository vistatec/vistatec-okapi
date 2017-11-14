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

package net.sf.okapi.lib.search.lucene.analysis;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.Reader;
import java.io.StringReader;
import java.util.Locale;

import net.sf.okapi.lib.search.Helper;

import org.apache.lucene.analysis.TokenStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 *
 * @author HaslamJD
 */
@RunWith(JUnit4.class)
public class NgramAnalyzerTest {

    @Test
    public void testConstructor() throws Exception {
        NgramAnalyzer nga = new NgramAnalyzer(Locale.CANADA, 5);
        assertEquals("Locale", Locale.CANADA, (Locale) Helper.getPrivateMember(nga, "locale"));
        assertEquals("Ngram length", 5, (int) (Integer) Helper.getPrivateMember(nga, "ngramLength"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConstructorInvalidNgramLength() throws Exception {
        @SuppressWarnings("unused")
		NgramAnalyzer nga = new NgramAnalyzer(Locale.CANADA, 0);
		nga.close();
    }

    @Test
    public void getTokenizer() throws Exception {
        NgramAnalyzer nga = new NgramAnalyzer(Locale.CANADA, 5);

        Reader r = new StringReader("Blah!");
        TokenStream ts = nga.tokenStream("fieldName", r);
        assertTrue("Valid reader and token", ts.incrementToken());
        nga.close();
    }

}
