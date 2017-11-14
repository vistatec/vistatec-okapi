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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.Reader;
import java.io.StringReader;
import java.util.Locale;

import net.sf.okapi.lib.search.Helper;

import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 *
 * @author HaslamJD
 */
@RunWith(JUnit4.class)
public class AlphabeticNgramTokenizerTest {

    AlphabeticNgramTokenizer ngramTk;

    @Before
    public void setUp() {
        Reader r = new StringReader("123456");
        ngramTk = new AlphabeticNgramTokenizer(r, 5, null);
    }

    @Test
    public void Constructor() throws Exception {
        Reader r = new StringReader("123456");
        ngramTk = new AlphabeticNgramTokenizer(r, 5, Locale.CANADA);
        assertEquals("ngram length", 5, ngramTk.getNgramLength());
        assertEquals("locale", Locale.CANADA, ngramTk.getLocale());
        assertNotNull("Term Attribute should initialized", Helper.getPrivateMember(ngramTk, "termAttribute"));
        assertNotNull("Offset Attribute should initialized", Helper.getPrivateMember(ngramTk, "offsetAttribute"));
        assertNotNull("Type Attribute should initialized", Helper.getPrivateMember(ngramTk, "typeAttribute"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void InvalidNGramLengthConstructor() {
        ngramTk = new AlphabeticNgramTokenizer(null, 0, Locale.CANADA);
    }

    @Test(expected = IllegalArgumentException.class)
    public void NullReaderConstructor() {
        ngramTk = new AlphabeticNgramTokenizer(null, 10, Locale.CANADA);
    }

    @Test
    public void IncrementToken() throws Exception {
        assertTrue("should have first token", ngramTk.incrementToken());
        assertTrue("should have second token", ngramTk.incrementToken());
        assertTrue("should have third token", ngramTk.incrementToken());
        assertFalse("should not have fourth token", ngramTk.incrementToken());
    }

    @Test
    public void IncrementTokenTermValue() throws Exception {
        ngramTk.incrementToken();
        assertEquals("First Token Value", "12345", getTermString());
        ngramTk.incrementToken();
        assertEquals("Second Token Value", "23456", getTermString());
        ngramTk.incrementToken();
        assertEquals("ThirdToken Value", "3456", getTermString());
        ngramTk.incrementToken();
        assertEquals("non-existent Token Value", "", getTermString());
    }

    @Test
    public void IncrementTokenTypeValue() throws Exception {
        ngramTk.incrementToken();
        assertEquals("First Token Type", "ngram(5)", getTypeString());
        
        ngramTk.incrementToken();
        assertEquals("Second Token Type", "ngram(5)", getTypeString());
        
        ngramTk.incrementToken();
        assertEquals("Third Token Type", "ngram(5)", getTypeString());
        
        ngramTk.incrementToken();
        assertEquals("non-existent Token Type (default)", "word", getTypeString());
    }

    @Test
    public void IncrementTokenOffsetValue() throws Exception {
        ngramTk.incrementToken();
        assertEquals("First Token Offset Start", 0, getOffsetStart());
        assertEquals("First Token Offset End", 5, getOffsetEnd());
        ngramTk.incrementToken();
        assertEquals("First Token Offset Start", 1, getOffsetStart());
        assertEquals("First Token Offset End", 6, getOffsetEnd());
        ngramTk.incrementToken();       
        assertEquals("First Token Offset Start", 2, getOffsetStart());
        assertEquals("First Token Offset End", 6, getOffsetEnd());
        ngramTk.incrementToken();
        assertEquals("non-existent Token Offset Start", 0, getOffsetStart());
        assertEquals("non-existent Token Offset End", 0, getOffsetEnd());
    }

    @Test
    public void IncrementTokenTypeValueNoLocaleNoLowerCase() throws Exception {
        Reader r = new StringReader("THIS SHOULD NOT BE LOWERCASE");
        ngramTk = new AlphabeticNgramTokenizer(r, 27, null);
        ngramTk.incrementToken();
        assertEquals("First Token Value", "THIS SHOULD NOT BE LOWERCAS", getTermString());
    }

    @Test
    public void IncrementTokenTypeValueNonArmenianLocaleLowerCase() throws Exception {
        Reader r = new StringReader("THIS SHOULD ALL BE LOWERCASE");
        ngramTk = new AlphabeticNgramTokenizer(r, 27, Locale.CANADA);
        ngramTk.incrementToken();
        assertEquals("First Token Value", "this should all be lowercas", getTermString());
    }

    // we now attempt to lowercase every language and let icu4j decide what to do @Test
    public void IncrementTokenTypeValueArmenianLocaleNoLowerCase() throws Exception {
        Reader r = new StringReader("THIS SHOULD NOT BE LOWERCASE");
        ngramTk = new AlphabeticNgramTokenizer(r, 27, new Locale("hy"));
        ngramTk.incrementToken();
        assertEquals("First Token Value", "THIS SHOULD NOT BE LOWERCAS", getTermString());
        r = new StringReader("THIS SHOULD NOT BE LOWERCASE");
        ngramTk = new AlphabeticNgramTokenizer(r, 27, new Locale("si"));
        ngramTk.incrementToken();
        assertEquals("First Token Value", "THIS SHOULD NOT BE LOWERCAS", getTermString());
    }

    @Test
    public void ResetNewReader() throws Exception {
        ngramTk.incrementToken();
        ngramTk.incrementToken();
        Reader r = new StringReader("Holy Reset Batman");
        ngramTk.reset(r);
        ngramTk.incrementToken();
        assertEquals("First Token Value", "Holy ", getTermString());
        assertEquals("First Token Offset Start", 0, getOffsetStart());
        assertEquals("First Token Offset End", 5, getOffsetEnd());
    }

    private String getTermString() throws Exception {
        return ((CharTermAttribute) Helper.getPrivateMember(ngramTk, "termAttribute")).toString();
    }

    private String getTypeString() throws Exception {
        return ((TypeAttribute) Helper.getPrivateMember(ngramTk, "typeAttribute")).type();
    }

    private int getOffsetStart() throws Exception {
        return ((OffsetAttribute) Helper.getPrivateMember(ngramTk, "offsetAttribute")).startOffset();
    }

    private int getOffsetEnd() throws Exception {
        return ((OffsetAttribute) Helper.getPrivateMember(ngramTk, "offsetAttribute")).endOffset();
    }
}
