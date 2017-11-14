/*===========================================================================
  Copyright (C) 2011 by the Okapi Framework contributors
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

package net.sf.okapi.steps.repetitionanalysis;

import static org.junit.Assert.assertEquals;

import java.util.List;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.tm.pensieve.common.MetadataType;
import net.sf.okapi.tm.pensieve.common.TmHit;
import net.sf.okapi.tm.pensieve.common.TranslationUnit;
import net.sf.okapi.tm.pensieve.common.TranslationUnitVariant;
import net.sf.okapi.tm.pensieve.seeker.ITmSeeker;
import net.sf.okapi.tm.pensieve.seeker.TmSeekerFactory;
import net.sf.okapi.tm.pensieve.writer.ITmWriter;
import net.sf.okapi.tm.pensieve.writer.TmWriterFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestPensieveWriterSeeker {
	private String tmDir;
	private ITmWriter tmWriter;
	private ITmSeeker currentTm;
	
	@Before
	public void setup() {
		//pathBase = Util.ensureSeparator(ClassUtil.getTargetPath(this.getClass()), true);
		//tmDir = pathBase + "tm/";
		//tmDir = pathBase;
		tmDir = Util.ensureSeparator(Util.getTempDirectory(), true) + "tm/";
		Util.createDirectories(tmDir);
		tmWriter = TmWriterFactory.createFileBasedTmWriter(tmDir, true);
		currentTm = TmSeekerFactory.createFileBasedTmSeeker(tmDir);
	}
	
	@After
	public void shutdown() {
		currentTm.close();
		tmWriter.close();
		Util.deleteDirectory(tmDir, false);
	}
	
	@Test
	public void testTmReadWrite() {
		TranslationUnit unit1 = new TranslationUnit(				
				new TranslationUnitVariant(LocaleId.ENGLISH, new TextFragment("source1")),
				new TranslationUnitVariant(LocaleId.GERMAN, new TextFragment("target1")));
		unit1.setMetadataValue(MetadataType.ID, "seg1");
		tmWriter.indexTranslationUnit(unit1);		
				
		TranslationUnit unit2 = new TranslationUnit(				
				new TranslationUnitVariant(LocaleId.ENGLISH, new TextFragment("source2")),
				new TranslationUnitVariant(LocaleId.GERMAN, new TextFragment("target2")));
		unit2.setMetadataValue(MetadataType.ID, "seg2");
		tmWriter.indexTranslationUnit(unit2);		
		
		tmWriter.commit();// Called once
		
		List<TmHit> hits = currentTm.searchExact(new TextFragment("source1"), null);
		assertEquals(1, hits.size());
		TmHit hit = hits.get(0);
		assertEquals("seg1", hit.getTu().getMetadataValue(MetadataType.ID));
		
		hits = currentTm.searchExact(new TextFragment("source2"), null); 
		assertEquals(1, hits.size());
		hit = hits.get(0);
		assertEquals("seg2", hit.getTu().getMetadataValue(MetadataType.ID));
	}
	
	@Test
	public void testTmReadWriteExact() {
		// Create and commit tu1
		TranslationUnit unit1 = new TranslationUnit(				
				new TranslationUnitVariant(LocaleId.ENGLISH, new TextFragment("source1")),
				new TranslationUnitVariant(LocaleId.GERMAN, new TextFragment("target1")));
		unit1.setMetadataValue(MetadataType.ID, "seg1");
		tmWriter.indexTranslationUnit(unit1);
		tmWriter.commit();

		// Seek tu1
		List<TmHit> hits = currentTm.searchExact(new TextFragment("source1"), null);
		assertEquals(1, hits.size());
		TmHit hit = hits.get(0);
		assertEquals("seg1", hit.getTu().getMetadataValue(MetadataType.ID));
		
		// Create and commit tu2
		TranslationUnit unit2 = new TranslationUnit(				
				new TranslationUnitVariant(LocaleId.ENGLISH, new TextFragment("source2")),
				new TranslationUnitVariant(LocaleId.GERMAN, new TextFragment("target2")));
		unit2.setMetadataValue(MetadataType.ID, "seg2");
		tmWriter.indexTranslationUnit(unit2);		
		tmWriter.commit();
				
		currentTm.close();
		currentTm = TmSeekerFactory.createFileBasedTmSeeker(tmDir);
		
		// Seek tu1
		hits = currentTm.searchExact(new TextFragment("source1"), null);
		assertEquals(1, hits.size());
		hit = hits.get(0);
		assertEquals("seg1", hit.getTu().getMetadataValue(MetadataType.ID));
		
		// Seek tu2
		hits = currentTm.searchExact(new TextFragment("source2"), null);
		assertEquals(1, hits.size());
		hit = hits.get(0);
		assertEquals("seg2", hit.getTu().getMetadataValue(MetadataType.ID));
	}
	
	@Test
	public void testTmReadWriteFuzzy() {
		TranslationUnit unit1 = new TranslationUnit(				
				new TranslationUnitVariant(LocaleId.ENGLISH, new TextFragment("source1")),
				new TranslationUnitVariant(LocaleId.GERMAN, new TextFragment("target1")));
		unit1.setMetadataValue(MetadataType.ID, "seg1");
		tmWriter.indexTranslationUnit(unit1);
		tmWriter.commit();

		List<TmHit> hits = currentTm.searchFuzzy(new TextFragment("source1"), 95, 1, null); 
		if (hits.size() > 0) {
			TmHit hit = hits.get(0);
			assertEquals("seg1", hit.getTu().getMetadataValue(MetadataType.ID));
		}
		
		TranslationUnit unit2 = new TranslationUnit(				
				new TranslationUnitVariant(LocaleId.ENGLISH, new TextFragment("source2")),
				new TranslationUnitVariant(LocaleId.GERMAN, new TextFragment("target2")));
		unit2.setMetadataValue(MetadataType.ID, "seg2");
		tmWriter.indexTranslationUnit(unit2);		
		tmWriter.commit();
		
		hits = currentTm.searchFuzzy(new TextFragment("source2"), 95, 1, null); 
		if (hits.size() > 0) {
			TmHit hit = hits.get(0);
			assertEquals("seg2", hit.getTu().getMetadataValue(MetadataType.ID));
		}
	}
	
	@Test
	public void testTmReadWriteSentenceExact() {
		TranslationUnit unit1 = new TranslationUnit(				
				new TranslationUnitVariant(LocaleId.ENGLISH, new TextFragment("Elephants cannot fly.")),
				new TranslationUnitVariant(LocaleId.GERMAN, new TextFragment("Elefanten können nicht fliegen.")));
		unit1.setMetadataValue(MetadataType.ID, "seg1");
		tmWriter.indexTranslationUnit(unit1);
		tmWriter.commit();

		List<TmHit> hits = currentTm.searchExact(new TextFragment("source1"), null); 
		if (hits.size() > 0) {
			TmHit hit = hits.get(0);
			assertEquals("seg1", hit.getTu().getMetadataValue(MetadataType.ID));
		}
		
		TranslationUnit unit2 = new TranslationUnit(				
				new TranslationUnitVariant(LocaleId.ENGLISH, new TextFragment("Elephants can fly.")),
				new TranslationUnitVariant(LocaleId.GERMAN, new TextFragment("Elefanten können fliegen.")));
		unit2.setMetadataValue(MetadataType.ID, "seg2");
		tmWriter.indexTranslationUnit(unit2);		
		tmWriter.commit();
		
		hits = currentTm.searchExact(new TextFragment("Elephants can fly."), null); 
		if (hits.size() > 0) {
			TmHit hit = hits.get(0);
			assertEquals("seg2", hit.getTu().getMetadataValue(MetadataType.ID));
		}
	}
	
	@Test
	public void testTmReadWriteSentenceTfExact() {
		TextFragment tf = new TextFragment("Elephants cannot fly.");
		TextFragment ttf = new TextFragment("Elefanten können nicht fliegen.");
		
		TranslationUnit unit1 = new TranslationUnit(				
				new TranslationUnitVariant(LocaleId.ENGLISH, tf),
				new TranslationUnitVariant(LocaleId.GERMAN, ttf));
		unit1.setMetadataValue(MetadataType.ID, "seg1");
		tmWriter.indexTranslationUnit(unit1);
		tmWriter.commit();

		List<TmHit> hits = currentTm.searchExact(tf, null); 
		if (hits.size() > 0) {
			TmHit hit = hits.get(0);
			assertEquals("seg1", hit.getTu().getMetadataValue(MetadataType.ID));
		}
	}
	
	@Test
	public void testTmReadWriteSentenceFuzzy() {
		TranslationUnit unit1 = new TranslationUnit(				
				new TranslationUnitVariant(LocaleId.ENGLISH, new TextFragment("Elephants cannot fly.")),
				new TranslationUnitVariant(LocaleId.GERMAN, new TextFragment("Elefanten können nicht fliegen.")));
		unit1.setMetadataValue(MetadataType.ID, "seg1");
		tmWriter.indexTranslationUnit(unit1);
		tmWriter.commit();

		List<TmHit> hits = currentTm.searchFuzzy(new TextFragment("Elephants cannot fly."), 95, 1, null); 
		if (hits.size() > 0) {
			TmHit hit = hits.get(0);
			assertEquals("seg1", hit.getTu().getMetadataValue(MetadataType.ID));
		}
		
		TranslationUnit unit2 = new TranslationUnit(				
				new TranslationUnitVariant(LocaleId.ENGLISH, new TextFragment("Elephants can fly.")),
				new TranslationUnitVariant(LocaleId.GERMAN, new TextFragment("Elefanten können fliegen.")));
		unit2.setMetadataValue(MetadataType.ID, "seg2");
		tmWriter.indexTranslationUnit(unit2);		
		tmWriter.commit();
		
		hits = currentTm.searchFuzzy(new TextFragment("Elephants can fly."), 95, 1, null); 
		if (hits.size() > 0) {
			TmHit hit = hits.get(0);
			assertEquals("seg2", hit.getTu().getMetadataValue(MetadataType.ID));
		}
	}			
}
