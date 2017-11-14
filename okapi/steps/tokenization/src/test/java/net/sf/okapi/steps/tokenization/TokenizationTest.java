/*===========================================================================
  Copyright (C) 2008-2011 by the Okapi Framework contributors
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

// This test case uses parts of the code presented by Sujit Pal at http://sujitpal.blogspot.com/2008/05/tokenizing-text-with-icu4js.html

package net.sf.okapi.steps.tokenization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.FileLocation;
import net.sf.okapi.common.LocaleFilter;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Range;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.TextUnitUtil;
import net.sf.okapi.steps.tokenization.common.ILexer;
import net.sf.okapi.steps.tokenization.common.Token;
import net.sf.okapi.steps.tokenization.common.TokensAnnotation;
import net.sf.okapi.steps.tokenization.engine.RbbiLexer;
import net.sf.okapi.steps.tokenization.engine.javacc.ParseException;
import net.sf.okapi.steps.tokenization.engine.javacc.SimpleCharStream;
import net.sf.okapi.steps.tokenization.engine.javacc.WordTokenizer;
import net.sf.okapi.steps.tokenization.engine.javacc.WordTokenizerTokenManager;
import net.sf.okapi.steps.tokenization.locale.LocaleUtil;
import net.sf.okapi.steps.tokenization.tokens.Tokens;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.icu.text.BreakIterator;
import com.ibm.icu.text.RuleBasedBreakIterator;

@SuppressWarnings("unused")
@RunWith(JUnit4.class)
public class TokenizationTest {

	private String text = "Jaguar will sell its new XJ-6 model in the U.S. for " +
    "a small fortune :-). Expect to pay around USD 120ks ($120,000.00 on 05/30/2007 at 12.30PM). Custom options " +
    "can set you back another few 10,000 dollars. For details, go to " +
    "<a href=\"http://www.jaguar.com/sales\" alt=\"Click here\">" +
    "Jaguar Sales</a> or contact xj-6@jaguar.com."+
    " See http://www.jaguar.com/sales, www.jaguar.com, AT&T, P&G, Johnson&Johnson, 192.168.0.5 for info 3.5pct.";

//	private String text = "U.S.";
	private LocaleId locENUS = LocaleId.fromString("en-us");
	private LocaleId locENGB = LocaleId.fromString("en-gb");
	private LocaleId locDEDE = LocaleId.fromString("de-de");
	private LocaleId locDECH = LocaleId.fromString("de-ch");	
	private LocaleId locFR = LocaleId.fromString("fr");

	//private String text = "The quick (\"brown\") fox can't jump 32.3 feet, right?";
//	private String text = "$120,000.00 on 05/30/2007 at 12.30PM is much better than $10.00 on 05/30/2007 at 22:30:15";
		
	//private String text = "http://www.jaguar.com/sales";
	//private String text = "<a href=\"http://www.jaguar.com/sales\" alt=\"Click here\">";
	
	private TokenizationStep ts;
	private Tokens tokens;

	private String streamAsString(InputStream input) throws IOException {
		BufferedReader reader = null;
		reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));

		StringBuilder tmp = new StringBuilder();
		char[] buf = new char[2048];
		int count = 0;
		while (( count = reader.read(buf)) != -1 ) {
			tmp.append(buf, 0, count);
		}
		
        return tmp.toString();
    }
	
	private Tokens tokenizeText() {
		
		Tokens res = new Tokens();
		ts.handleEvent(new Event(EventType.START_BATCH)); // Calls component_init();
		
		StartDocument startDoc = new StartDocument("tokenization");
		startDoc.setLocale(locENUS);
		startDoc.setMultilingual(false);		
		Event event = new Event(EventType.START_DOCUMENT, startDoc);		
		ts.handleEvent(event);
				
		ITextUnit tu = TextUnitUtil.buildTU(text);
		event = new Event(EventType.TEXT_UNIT, tu);		
		ts.handleEvent(event);
		
		// Move tokens from the event's annotation to result
		TokensAnnotation ta = TextUnitUtil.getSourceAnnotation(tu, TokensAnnotation.class);
		if (ta != null)
			res.addAll(ta.getTokens());
		
		ts.handleEvent(new Event(EventType.END_BATCH)); // Calls component_done();
		return res;
	}
	
	@Before
	public void setUp() {

		ts = new TokenizationStep();		
	}

	@Test
	public void testDefRules() {
		
		RuleBasedBreakIterator iterator = (RuleBasedBreakIterator) BreakIterator.getWordInstance();
	}
	
	@Test
	public void testLocaleUtil() {
	
		assertEquals("en-us", LocaleUtil.normalizeLanguageCode_Okapi("en_US"));
		assertEquals("en_US", LocaleUtil.normalizeLanguageCode_ICU("EN-US"));
	}
	
	@Test
	public void testTS() {
		
		ts = new TokenizationStep();
		
		ITextUnit tu = TextUnitUtil.buildTU(text);
		Event event = new Event(EventType.TEXT_UNIT, tu);
		
		ts.handleEvent(new Event(EventType.START_BATCH));
		ts.handleEvent(event);
		ts.handleEvent(new Event(EventType.END_BATCH));
	}
	
	private void listTokens(Tokens tokens) {
		
		if (tokens == null) return;
		StringBuilder sb = new StringBuilder(); 
		for (Token token : tokens) {	
			
			sb.append(token.toString() + "\n");
		}
		Logger localLogger = LoggerFactory.getLogger(getClass()); // loggers are cached
		localLogger.debug(sb.toString());
	}
	
	
	@Test
	public void listTokenizerOutput() {
		Tokens tokens = Tokenizer.tokenize("NASDAQ is a U.S. stock exchange.", locENUS); // All tokens						
		listTokens(tokens);
	}
	
	@Test
	public void testFilters() {

		Parameters params = new Parameters();
		ts.setParameters(params);
		
		LocaleFilter languageFilter = params.getLocaleFilter();
		assertNotNull(languageFilter);
		assertTrue(params.supportsLanguage(locENUS));
		
		assertTrue(params.supportsToken("FAKE_TOKEN"));
		assertTrue(params.supportsToken(Integer.MAX_VALUE));
				
		params.setLocaleFilter("");
		assertTrue(params.supportsLanguage(locENUS));
		assertTrue(params.supportsLanguage(locENGB));
		assertTrue(params.supportsLanguage(locDEDE));
		assertTrue(params.supportsLanguage(locDECH));
		
		params.setLocaleFilter("en !en-gb de-*-* !de-ch");
		assertTrue(params.supportsLanguage(locENUS));
		assertFalse(params.supportsLanguage(locENGB));
		assertTrue(params.supportsLanguage(locDEDE));
		assertFalse(params.supportsLanguage(locDECH));
		
		params.setTokenNames((String[]) null);
		assertTrue(params.supportsToken("FAKE_TOKEN"));
		assertTrue(params.supportsToken(Integer.MAX_VALUE));
		
		params.setTokenNames("WORD", "PUNKTUATION");
		assertFalse(params.supportsToken("FAKE_TOKEN"));
		assertFalse(params.supportsToken(Integer.MAX_VALUE));
		assertTrue(params.supportsToken("WORD"));
		
		params = (Parameters) ts.getParameters();
		ts.handleEvent(new Event(EventType.START_BATCH));
		
		assertTrue(params.supportsLanguage(locENUS));
		assertFalse(params.supportsLanguage(locENGB));
		assertTrue(params.supportsLanguage(locDEDE));
		assertFalse(params.supportsLanguage(locDECH));
		
		assertFalse(params.supportsToken("FAKE_TOKEN"));
		assertFalse(params.supportsToken(Integer.MAX_VALUE));
		assertTrue(params.supportsToken("WORD"));
		
		ts.handleEvent(new Event(EventType.END_BATCH));
	}
	
	@Test
	public void testTokenizer1() {
		
		ts.setConfiguration(this.getClass(), "test_config1.tprm");
		Parameters params = (Parameters) ts.getParameters();
		
		assertTrue(params.supportsToken("WORD"));
		
		List<ILexer> lexers = ts.getLexers();
		assertEquals(1, lexers.size());
		
		tokens = tokenizeText();
		assertEquals(183, tokens.size());
		
		// DEBUG listTokens(tokens);
	}
	
	@Test
	public void testTokenizer2() {
		Tokens tokens = Tokenizer.tokenize("word1 word2 word3", locENUS, "WORD");
		assertEquals(3, tokens.size());
		assertEquals("word1", tokens.get(0).getValue());
		assertEquals("word2", tokens.get(1).getValue());
		assertEquals("word3", tokens.get(2).getValue());
	}
	
	@Test
	public void testJavaCC() {
		
		StringReader sr = new StringReader("This is a 1248-th test. U.S.A.F. read-through\n didn't AT&T, P&G, Johnson&Johnson \n\nadmin@yahoo.com 192.168.0.7");
		SimpleCharStream stream = new SimpleCharStream(sr);
		
		WordTokenizer tokenizer = new WordTokenizer(new WordTokenizerTokenManager(stream));
		
		net.sf.okapi.steps.tokenization.engine.javacc.Token token = null;
		
		do {
			try {
			token = tokenizer.nextToken();
			} catch (ParseException e) {

				e.printStackTrace();
				break;
				
			} catch (IOException e) {

				e.printStackTrace();
				break;
			}
			
			if (token == null) break;			
		} while (token != null);
	}
	
	
	@Test
	public void testRetainRemove() {
		
		List<String> list = new ArrayList<String> ();
		list.add("A");
		list.add("B");
		list.add("C");
		
		List<String> whiteList = new ArrayList<String> ();
		whiteList.add("A");
		whiteList.add("B");
						
		List<String> blackList = new ArrayList<String> ();
		blackList.add("B");

		assertEquals(3, list.size());
		assertEquals("A", list.get(0));
		assertEquals("B", list.get(1));
		assertEquals("C", list.get(2));
		
		list.retainAll(whiteList);
		assertEquals(2, list.size());
		assertEquals("A", list.get(0));
		assertEquals("B", list.get(1));
		
		list.removeAll(blackList);
		assertEquals(1, list.size());
		assertEquals("A", list.get(0));
	}
	
	@Test
	public void testFormRbbiRules() {
		
		String rules = null;
		String expected = null;

		FileLocation root = FileLocation.fromClass(this.getClass());
		try {
			expected = Util.normalizeNewlines(streamAsString(root.in("rbbi_custom.txt").asInputStream()));
			rules = streamAsString(root.in("rbbi_default.txt").asInputStream());
			
			rules = RbbiLexer.formatRule(rules, 
					"Abbreviation", 
					"Abbreviation: Uppercase alpha chars separated by period and optionally followed by a period", 
					"[A-Z0-9](\\.[A-Z0-9])+(\\.)*",
					500);
			rules = RbbiLexer.formatRule(rules, 
					"HyphenatedWord", 
					"Hyphenated Word : sequence of letter or digit, (punctuated by - or _, with following letter or digit sequence)+", 
					"[A-Za-z0-9]+([\\-_][A-Za-z0-9]+)+", 
					501);
			rules = RbbiLexer.formatRule(rules, 
					"EmailAddress", 
					"Email address: sequence of letters, digits and punctuation followed by @ and followed by another sequence", 
					"[A-Za-z0-9_\\-\\.]+\\@[A-Za-z][A-Za-z0-9_]+\\.[a-z]+",
					502);
			rules = RbbiLexer.formatRule(rules, 
					"InternetAddress", 
					"Internet Addresses: http://www.foo.com(/bar)", 
					"[a-z]+\\:\\/\\/[a-z0-9]+(\\.[a-z0-9]+)+(\\/[a-z0-9][a-z0-9\\.]+)", 
					503);
			rules = RbbiLexer.formatRule(rules, 
					"XmlMarkup", 
					"XML markup: A run begins with < and ends with the first matching >", 
					"\\<[^\\>]+\\>", 
					504);
			rules = RbbiLexer.formatRule(rules, 
					"Emoticon", 
					"Emoticon: A run that starts with :;B8{[ and contains only one or more of the following -=/{})(", 
					"[B8\\:\\;\\{\\[][-=\\/\\{\\}\\)\\(]+",
					505);
			
		} catch (IOException e) {

			e.printStackTrace();
		}
		
		assertEquals(expected, rules);
	}
	
	@Test
	public void testRange() {
	
		Range r1 = new Range(1, 5);
		Range r2 = new Range(1, 5);
		
		assertFalse(r1 == r2);
		assertFalse(r1.equals(r2));
		assertFalse(r1.hashCode() == r2.hashCode());
		assertFalse(r1.toString() == r2.toString());
	}
}
