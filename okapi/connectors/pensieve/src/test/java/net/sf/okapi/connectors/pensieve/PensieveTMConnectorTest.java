/*===========================================================================
  Copyright (C) 2009-2014 by the Okapi Framework contributors
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

package net.sf.okapi.connectors.pensieve;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.query.QueryResult;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.lib.translation.ITMQuery;
import net.sf.okapi.tm.pensieve.common.TranslationUnit;
import net.sf.okapi.tm.pensieve.common.TranslationUnitVariant;
import net.sf.okapi.tm.pensieve.writer.ITmWriter;
import net.sf.okapi.tm.pensieve.writer.TmWriterFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(JUnit4.class)
public class PensieveTMConnectorTest {
	
	private ITMQuery connector;
	private LocaleId locENUS = LocaleId.fromString("EN-US");
	private LocaleId locFRFR = LocaleId.fromString("FR-FR");
	
	@Before
	public void setUp() throws URISyntaxException {
		URL url = PensieveTMConnectorTest.class.getResource("/testtm/segments.gen");
		connector = null;
		if ( url == null ) return; // Creation case
		connector = new PensieveTMConnector();
		Parameters params = new Parameters();
		params.setDbDirectory(Util.getDirectoryName(url.toURI().getPath()));
		connector.setParameters(params);
		connector.setLanguages(locENUS, locFRFR);
		connector.open();
	}
	
	@After
	public void tearDown () {
		if ( connector != null ) {
			connector.close();
			connector = null;
		}
	}

	@Test
	// This method must be run first
	public void aaaTestCreateTM () throws IOException, URISyntaxException {
		if ( connector != null ) {
			connector.close(); // Close the connector created by setUp()
		}
		URL url = PensieveTMConnectorTest.class.getResource("/mytm.tmx");
		String dir =  Util.getDirectoryName(url.toURI().getPath())+File.separator+"testtm"+File.separator;
		Util.createDirectories(dir);
		ITmWriter writer = TmWriterFactory.createFileBasedTmWriter(dir, true);
		
		// TU 1
		TranslationUnit tu = new TranslationUnit();
		TranslationUnitVariant tuv = new TranslationUnitVariant(locENUS,
			new TextFragment("Elephants cannot fly."));
		tu.setSource(tuv);
		tuv = new TranslationUnitVariant(locFRFR,
			new TextFragment("Les \u00e9l\u00e9phants ne peuvent pas voler."));
		tu.setTarget(tuv);
		writer.indexTranslationUnit(tu);

		// TU 2
		tu = new TranslationUnit();
		TextFragment tf = new TextFragment("Elephants ");
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append("cannot");
		tf.append(TagType.CLOSING, "b", "</b>");
		tf.append(" fly.");
		tuv = new TranslationUnitVariant(locENUS, tf);
		tu.setSource(tuv);
		tf = new TextFragment("Les \u00e9l\u00e9phants ");
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append("ne peuvent pas");
		tf.append(TagType.CLOSING, "b", "</b>");
		tf.append(" voler.");
		tuv = new TranslationUnitVariant(locFRFR, tf);
		tu.setTarget(tuv);
		writer.indexTranslationUnit(tu);

		// TU 3
		tu = new TranslationUnit();
		tf = new TextFragment("Elephants ");
		tf.append(TagType.OPENING, "g0", "<g0>");
		tf.append("cannot");
		tf.append(TagType.CLOSING, "g0", "</g0>");
		tf.append(" fly.");
		tuv = new TranslationUnitVariant(locENUS, tf);
		tu.setSource(tuv);
		tf = new TextFragment("Les \u00e9l\u00e9phants ");
		tf.append(TagType.OPENING, "g0", "<g0>");
		tf.append("ne peuvent pas");
		tf.append(TagType.CLOSING, "g0", "</g0>");
		tf.append(" voler.");
		tuv = new TranslationUnitVariant(locFRFR, tf);
		tu.setTarget(tuv);
		writer.indexTranslationUnit(tu);

		writer.close();
	}

	@Test
	public void testGetMatches () {
		String input = "Elephants cannot fly.";
		connector.setThreshold(75);
		assertTrue(connector.query(input) > 0);
		assertTrue(connector.hasNext());
		QueryResult qr = connector.next();
		assertNotNull(qr);
		assertEquals(input, qr.source.toText());
		assertEquals("Les \u00e9l\u00e9phants ne peuvent pas voler.", qr.target.toText());
	}

	@Test
	public void testGetNoMatch () {
		connector.setThreshold(1);
		int n = connector.query("Otters can swim.");
		assertTrue(n == 0);
		assertTrue(connector.query("") == 0);
		String tmp = null;
		assertTrue(connector.query(tmp) == 0);
	}

	@Test
	public void testGetNoMatchWithCodes () {
		connector.setThreshold(1);
		assertTrue(connector.query(createOttersFragment()) == 0);
		TextFragment tf = new TextFragment();
		assertTrue(connector.query(tf) == 0);
	}

	@Test
	public void testGetExactMatch () {
		String input = "Elephants cannot fly.";
		connector.setThreshold(100);
		assertTrue(connector.query(input) > 0);
		QueryResult qr = connector.next();
		assertEquals(input, qr.source.toText());
		assertEquals("Les \u00e9l\u00e9phants ne peuvent pas voler.", qr.target.toText());
		assertEquals(100, qr.getFuzzyScore());
	}

	@Test
	public void testGetExactMatchWithCodes () {
		TextFragment tf = createElephantsFragment();
		connector.setThreshold(100);
		assertTrue(connector.query(tf) > 0);
		QueryResult qr = connector.next();
		assertEquals(tf.toText(), qr.source.toText());
		assertEquals("Les \u00e9l\u00e9phants <b>ne peuvent pas</b> voler.", qr.target.toText());
		assertEquals(100, qr.getFuzzyScore());
	}

	@Test
	public void testGetAlmostExactMatchWithCodes () {
		TextFragment tf = createElephantsFragment();
		connector.setThreshold(98);
		assertEquals(3, connector.query(tf));

		// First exact
		QueryResult qr = connector.next();
		assertEquals(100, qr.getFuzzyScore());
		
		// Second exact
		qr = connector.next();
		assertEquals(100, qr.getFuzzyScore());
		
		// Fuzzy
		qr = connector.next();
		assertTrue(qr.getFuzzyScore()!=100);
	}

	private TextFragment createElephantsFragment () {
		TextFragment tf = new TextFragment("Elephants ");
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append("cannot");
		tf.append(TagType.CLOSING, "b", "</b>");
		tf.append(" fly.");
		return tf;
	}

	private TextFragment createOttersFragment () {
		TextFragment tf = new TextFragment("Otters ");
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append("can");
		tf.append(TagType.CLOSING, "b", "</b>");
		tf.append(" swim.");
		return tf;
	}

}
