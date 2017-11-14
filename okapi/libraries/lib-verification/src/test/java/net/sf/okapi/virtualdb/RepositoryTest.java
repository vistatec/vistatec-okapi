/*===========================================================================
  Copyright (C) 2010-2011 by the Okapi Framework contributors
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

package net.sf.okapi.virtualdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;

import net.sf.okapi.common.FileLocation;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filters.FilterConfigurationMapper;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.virtualdb.jdbc.Repository;
import net.sf.okapi.virtualdb.jdbc.h2.H2Access;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class RepositoryTest {
	
	private FilterConfigurationMapper fcMapper;
	private LocaleId locEN = LocaleId.fromBCP47("en");
	private LocaleId locFR = LocaleId.fromBCP47("fr");
	private String outputDir;
	private FileLocation location;

	public RepositoryTest () {
		location = FileLocation.fromClass(RepositoryTest.class).out("/");
		outputDir = location.asUri().getPath();
	}
	
	@Test
	public void testImportTwoFiles () {
		fcMapper = new FilterConfigurationMapper();
		fcMapper.addConfigurations("net.sf.okapi.filters.xliff.XLIFFFilter");
		// Create the repository object
		IVRepository repo = new Repository(new H2Access(outputDir, fcMapper));
		// Create the repository database
		repo.create("myRepo");

		// Import file 1
		RawDocument rd = new RawDocument(location.in("/test01.xlf").asUri(), "UTF-8", locEN, locFR);
		rd.setFilterConfigId("okf_xliff");
		repo.importDocument(rd);
		rd.close();
		// Import file 2
		rd = new RawDocument(location.in("/test02.xlf").asUri(), "UTF-8", locEN, locFR);
		rd.setFilterConfigId("okf_xliff");
		repo.importDocument(rd);
		rd.close();
		// Import file 3
		rd = new RawDocument(location.in("/test03.xlf").asUri(), "UTF-8", locEN, locFR);
		rd.setFilterConfigId("okf_xliff");
		repo.importDocument(rd);
		rd.close();
		
		// Check first document
		IVDocument vdoc1 = repo.getFirstDocument();
		IVTextUnit vtu = vdoc1.getTextUnit("1");
		assertEquals("Texte de l'attribute", vtu.getTextUnit().getTarget(locFR).toString());
		
		// Check next document
		IVDocument vdoc2 = (IVDocument)vdoc1.getNextSibling();
		vtu = vdoc2.getTextUnit("1");
		assertNotNull(vtu);
		assertEquals("test02 - Texte de l'attribute", vtu.getTextUnit().getTarget(locFR).toString());
		// Test previous doc
//		assertEquals(vdoc1, vdoc2.getPreviousSibling());
		
		// Check third document
		IVDocument vdoc3 = (IVDocument)vdoc2.getNextSibling();
		vtu = vdoc3.getTextUnit("1");
		assertNotNull(vtu);
		assertEquals("test03 - Texte de l'attribute", vtu.getTextUnit().getTarget(locFR).toString());
		// Test previous doc
//		assertEquals(vdoc2, vdoc3.getPreviousSibling());
		
		// Delete the second document
		repo.removeDocument(vdoc2);
		
		// First document is the same
		IVDocument nvdoc1 = repo.getFirstDocument();
		vtu = nvdoc1.getTextUnit("1");
		assertNotNull(vtu);
		assertEquals("Texte de l'attribute", vtu.getTextUnit().getTarget(locFR).toString());
		// Next document should be the test03 one
		IVDocument nvdoc2 = (IVDocument)nvdoc1.getNextSibling();
		vtu = nvdoc2.getTextUnit("1");
		assertNotNull(vtu);
		assertEquals("test03 - Texte de l'attribute", vtu.getTextUnit().getTarget(locFR).toString());
		// Test previous doc
//		assertEquals(nvdoc1, nvdoc2.getPreviousSibling());

		repo.close();
	}
	
	@Test
	public void testCreate () {
		fcMapper = new FilterConfigurationMapper();
		fcMapper.addConfigurations("net.sf.okapi.filters.xliff.XLIFFFilter");
		// Create the repository object
		IVRepository repo = new Repository(new H2Access(outputDir, fcMapper));
		// Create the repository database
		repo.create("myRepo");
		// Import data
		RawDocument rd = new RawDocument(location.in("/test01.xlf").asUri(), "UTF-8", locEN, locFR);
		rd.setFilterConfigId("okf_xliff");
		repo.importDocument(rd);
		rd.close();

		// Get the documents
		ArrayList<IVDocument> docs = new ArrayList<IVDocument>();
		for ( IVDocument doc : repo.documents() ) {
			docs.add(doc);
		}
		assertEquals(1, docs.size());
		
		IVDocument doc = repo.getFirstDocument();
		assertNotNull(doc);
		
		ArrayList<IVTextUnit> vtus = new ArrayList<IVTextUnit>();
		for ( IVTextUnit vtu : doc.textUnits() ) {
			vtus.add(vtu);
		}
		assertEquals(8, vtus.size());
		
		ITextUnit tu = vtus.get(0).getTextUnit();
		assertEquals("1", tu.getId());
		assertEquals("Texte de l'attribute", tu.getTarget(locFR).toString());
		
		repo.close();
	}

	@Test
	public void testRetrieve () {
		fcMapper = new FilterConfigurationMapper();
		fcMapper.addConfigurations("net.sf.okapi.filters.xliff.XLIFFFilter");
		// Create the repository object
		IVRepository repo = new Repository(new H2Access(outputDir, fcMapper));
		// Create the repository database
		repo.create("myRepo");
		// Import data
		RawDocument rd = new RawDocument(location.in("/test01.xlf").asUri(), "UTF-8", locEN, locFR);
		rd.setFilterConfigId("okf_xliff");
		repo.importDocument(rd);
		rd.close();
		
		// Get the documents
		ArrayList<IVDocument> docs = new ArrayList<IVDocument>();
		for ( IVDocument doc : repo.documents() ) {
			docs.add(doc);
		}
		assertEquals(1, docs.size());
		IVDocument doc = repo.getFirstDocument();
		
		ArrayList<IVTextUnit> vtus = new ArrayList<IVTextUnit>();
		for ( IVTextUnit vtu : doc.textUnits() ) {
			vtus.add(vtu);
		}
		assertEquals(8, vtus.size());
		
		ITextUnit tu = vtus.get(0).getTextUnit();
		assertEquals("1", tu.getId());
		assertEquals("Texte de l'attribute", tu.getTarget(locFR).toString());

		repo.close();
	}
	
	@Test
	public void testSaveAndRetrieve () {
		fcMapper = new FilterConfigurationMapper();
		fcMapper.addConfigurations("net.sf.okapi.filters.xliff.XLIFFFilter");
		// Create the repository object
		IVRepository repo = new Repository(new H2Access(outputDir, fcMapper));
		// Create the repository database
		repo.create("myRepo");
		// Import data
		RawDocument rd = new RawDocument(location.in("/test01.xlf").asUri(), "UTF-8", locEN, locFR);
		rd.setFilterConfigId("okf_xliff");
		repo.importDocument(rd);
		rd.close();

		IVDocument doc = repo.getFirstDocument();

		IVTextUnit vtu = (IVTextUnit)doc.getItem("1");
		ITextUnit tu = vtu.getTextUnit();
		assertEquals("Texte de l'attribute", tu.getTarget(locFR).toString());
		
		tu.setTarget(locFR, new TextContainer("new target text"));
		vtu.save();

		vtu = (IVTextUnit)doc.getItem("1");
		tu = vtu.getTextUnit();
		assertEquals("new target text", tu.getTarget(locFR).toString());

		repo.close();
	}

}
