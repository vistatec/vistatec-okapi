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

package net.sf.okapi.tm.pensieve.writer;

import static net.sf.okapi.tm.pensieve.common.TranslationUnitField.SOURCE;
import static net.sf.okapi.tm.pensieve.common.TranslationUnitField.SOURCE_CODES;
import static net.sf.okapi.tm.pensieve.common.TranslationUnitField.SOURCE_EXACT;
import static net.sf.okapi.tm.pensieve.common.TranslationUnitField.SOURCE_LANG;
import static net.sf.okapi.tm.pensieve.common.TranslationUnitField.TARGET;
import static net.sf.okapi.tm.pensieve.common.TranslationUnitField.TARGET_CODES;
import static net.sf.okapi.tm.pensieve.common.TranslationUnitField.TARGET_LANG;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.tm.pensieve.Helper;
import net.sf.okapi.tm.pensieve.common.Metadata;
import net.sf.okapi.tm.pensieve.common.MetadataType;
import net.sf.okapi.tm.pensieve.common.TranslationUnit;
import net.sf.okapi.tm.pensieve.common.TranslationUnitField;
import net.sf.okapi.tm.pensieve.common.TranslationUnitVariant;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * User: Christian Hargraves Date: Aug 11, 2009 Time: 6:35:45 AM
 */
@RunWith(JUnit4.class)
public class PensieveWriterTest {

	PensieveWriter tmWriter;
	IndexWriter writer;
	static final File GOOD_DIR = new File("../data/");
	static final File GOOD_FILE = new File(GOOD_DIR, "apache1.0.txt");
	RAMDirectory dir;
	LocaleId locEN = LocaleId.fromString("EN");
	LocaleId locFR = LocaleId.fromString("FR");
	LocaleId locKR = LocaleId.fromString("KR");

	@Before
	public void init() throws IOException {
		dir = new RAMDirectory();
		tmWriter = new PensieveWriter(dir, true);
		writer = tmWriter.getIndexWriter();
	}

	@Test
	public void constructorCreateNew() throws IOException {
		tmWriter.indexTranslationUnit(Helper.createTU(locEN, locKR, "Joe",
				"Jo", "1"));
		tmWriter.close();
		tmWriter = new PensieveWriter(dir, true);
		tmWriter.indexTranslationUnit(Helper.createTU(locEN, locKR, "Joseph",
				"Yosep", "2"));
		tmWriter.close();
		assertEquals("# of docs in tm", 1, tmWriter.getIndexWriter().numDocs());
	}

	@Test
	public void constructorCreateNew2() throws IOException {
		tmWriter.indexTranslationUnit(Helper.createTU(locEN, locKR, "Joe",
				"Jo", "1"));
		tmWriter.close();
		tmWriter = new PensieveWriter(dir, true);
		tmWriter.indexTranslationUnit(Helper.createTU(locEN, locKR, "Joseph",
				"Yosep", "2"));
		tmWriter.close();
		assertEquals("# of docs in tm", 1, tmWriter.getIndexWriter().numDocs());
	}

	@Test
	public void constructorAppend() throws IOException {
		tmWriter.indexTranslationUnit(Helper.createTU(locEN, locKR, "Joe",
				"Jo", "1"));
		tmWriter.close();
		tmWriter = new PensieveWriter(dir, false);
		tmWriter.indexTranslationUnit(Helper.createTU(locEN, locKR, "Joseph",
				"Yosep", "2"));
		tmWriter.close();
		assertEquals("# of docs in tm", 2, tmWriter.getIndexWriter().numDocs());
	}

	@Test
	public void getIndexWriterSameDirectory() {
		assertSame("ram directory", dir, tmWriter.getIndexWriter()
				.getDirectory());
	}

	@Test
	public void indexTranslationUnitMetaData() throws IOException,
			ParseException {
		tmWriter.indexTranslationUnit(Helper.createTU(locEN, locKR, "Joe",
				"Jo", "1"));
		tmWriter.indexTranslationUnit(Helper.createTU(locEN, locKR, "Jane",
				"Jaen", "2"));
		writer.commit();

		assertEquals("# of docs found for id=1", 1,
				getNumOfHitsFor(MetadataType.ID.fieldName(), "1"));
		assertEquals("# of docs found for id=2", 1,
				getNumOfHitsFor(MetadataType.ID.fieldName(), "2"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void updateNullTu() throws IOException, ParseException {
		tmWriter.update(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void updateEmptyId() throws IOException, ParseException {
		tmWriter.update(new TranslationUnit());
	}

	@Test
	public void update() throws IOException, ParseException {
		TranslationUnit tu1 = Helper.createTU(locEN, locKR, "Joe", "Jo", "1");
		TranslationUnit tu2 = Helper
				.createTU(locEN, locKR, "Jane", "Jaen", "2");
		tmWriter.indexTranslationUnit(tu1);
		tmWriter.indexTranslationUnit(tu2);
		writer.commit();

		tu1.getTarget().setContent(new TextFragment("Ju"));
		tmWriter.update(tu1);
		writer.commit();
		Document doc1 = findDocument(MetadataType.ID.fieldName(), "1");
		Document doc2 = findDocument(MetadataType.ID.fieldName(), "2");
		assertEquals("source text", tu1.getSource().getContent().toText(), doc1
				.getField(TranslationUnitField.SOURCE_EXACT.name())
				.stringValue());
		assertEquals("target text", tu1.getTarget().getContent().toText(), doc1
				.getField(TranslationUnitField.TARGET.name()).stringValue());
		assertEquals("target text", tu2.getTarget().getContent().toText(), doc2
				.getField(TranslationUnitField.TARGET.name()).stringValue());
	}

	@Test
	public void indexTranslationUnitWithOverwriteOption() throws IOException,
			ParseException {
		// Start TM
		TranslationUnit tu1 = Helper.createTU(locEN, locKR, "Joe", "Jo", "1");
		TranslationUnit tu2 = Helper
				.createTU(locEN, locKR, "Jane", "Jaen", "2");
		tmWriter.indexTranslationUnit(tu1);
		tmWriter.indexTranslationUnit(tu2);
		writer.commit();

		// Overwrite the first document
		TranslationUnit tu1new = Helper.createTU(locEN, locKR, "Joe", "NewJo",
				"3");
		tmWriter.indexTranslationUnit(tu1new, true);
		writer.commit();
		// New document should be in
		Document doc = findDocument(MetadataType.ID.fieldName(), "3");
		assertEquals("source text", "Joe",
				doc.getField(TranslationUnitField.SOURCE_EXACT.name())
						.stringValue());
		assertEquals("target text", "NewJo",
				doc.getField(TranslationUnitField.TARGET.name()).stringValue());
		// Old document should not be in
		doc = findDocument(MetadataType.ID.fieldName(), "1");
		assertNull(doc);

		// Add without overwriting
		tu1new = Helper.createTU(locEN, locKR, "Joe", "NewJo2", "4");
		tmWriter.indexTranslationUnit(tu1new, false);
		writer.commit();
		// New document should be in
		doc = findDocument(MetadataType.ID.fieldName(), "4");
		assertEquals("source text", "Joe",
				doc.getField(TranslationUnitField.SOURCE_EXACT.name())
						.stringValue());
		assertEquals("target text", "NewJo2",
				doc.getField(TranslationUnitField.TARGET.name()).stringValue());
		// Previous document should be in
		doc = findDocument(MetadataType.ID.fieldName(), "3");
		assertEquals("source text", "Joe",
				doc.getField(TranslationUnitField.SOURCE_EXACT.name())
						.stringValue());
		assertEquals("target text", "NewJo",
				doc.getField(TranslationUnitField.TARGET.name()).stringValue());
	}

	@Test
	public void indexTranslationUnitWithOverwriteOptionAndCodes()
			throws IOException, ParseException {
		// Start TM
		TranslationUnit tu1 = Helper.createTU(locEN, locKR, "Joe", "Jo", "1");
		tu1.getSource().getContent()
				.append(TagType.PLACEHOLDER, "code", "data1");
		tmWriter.indexTranslationUnit(tu1);
		writer.commit();

		// Overwrite the first document
		TranslationUnit tu1new = Helper.createTU(locEN, locKR, "Joe", "NewJo",
				"2");
		tu1new.getSource().getContent()
				.append(TagType.PLACEHOLDER, "code", "data2");
		tmWriter.indexTranslationUnit(tu1new, true);
		writer.commit();

		// New document should be in
		Document doc = findDocument(MetadataType.ID.fieldName(), "2");
		assertEquals("target text", "NewJo",
				doc.getField(TranslationUnitField.TARGET.name()).stringValue());
		// Old document should also be in (not overwritten because the code data
		// are not the same)
		doc = findDocument(MetadataType.ID.fieldName(), "1");
		assertEquals("target text", "Jo",
				doc.getField(TranslationUnitField.TARGET.name()).stringValue());
	}

	@Test(expected = IllegalArgumentException.class)
	public void deleteNullId() throws IOException, ParseException {
		tmWriter.delete(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void deleteEmptyId() throws IOException, ParseException {
		tmWriter.delete("");
	}

	@Test
	public void deleteWithId() throws IOException, ParseException {
		tmWriter.indexTranslationUnit(Helper.createTU(locEN, locKR, "Joe",
				"Jo", "1"));
		tmWriter.indexTranslationUnit(Helper.createTU(locEN, locKR, "Jane",
				"Jaen", "2"));
		writer.commit();

		tmWriter.delete("1");
		writer.commit();
		assertEquals("# of docs found for id=1", 0,
				getNumOfHitsFor(MetadataType.ID.fieldName(), "1"));
		assertEquals("# of docs found for id=2", 1,
				getNumOfHitsFor(MetadataType.ID.fieldName(), "2"));
	}

	@Test
	public void addMetadataToDocument() {
		Metadata md = new Metadata();
		md.put(MetadataType.FILE_NAME, "some/file");
		md.put(MetadataType.GROUP_NAME, "some group");
		md.put(MetadataType.ID, "someId");
		md.put(MetadataType.TYPE, "someType");
		Document doc = new Document();
		tmWriter.addMetadataToDocument(doc, md);
		assertEquals("Document's file name field", "some/file",
				getFieldValue(doc, MetadataType.FILE_NAME.fieldName()));
		assertEquals("Document's group name field", "some group",
				getFieldValue(doc, MetadataType.GROUP_NAME.fieldName()));
		assertEquals("Document's id field", "someId",
				getFieldValue(doc, MetadataType.ID.fieldName()));
		assertEquals("Document's type field", "someType",
				getFieldValue(doc, MetadataType.TYPE.fieldName()));
	}

	@Test
	public void constructorCreatesWriter() {
		assertNotNull("the tmWriter tmWriter was not created as expected",
				tmWriter);
	}

	@Test
	public void constructorUsesExpectedDirectory() {
		assertTrue("The index directory should end with 'target/test-classes'",
				writer.getDirectory() instanceof RAMDirectory);
	}

	// @SuppressWarnings({"ThrowableInstanceNeverThrown"})
	// TODO: Fix me please
	// @Test(expected = OkapiIOException.class)
	public void endIndexHandlesIOException() throws Exception {
		// IndexWriter spyWriter = spy(writer);
		// doThrow(new IOException("some text")).when(spyWriter).commit();
		// Helper.setPrivateMember(tmWriter, "indexWriter", spyWriter);
		// tmWriter.endIndex();
	}

	@Test(expected = AlreadyClosedException.class)
	public void endIndexClosesWriter() throws IOException {
		tmWriter.close();
		tmWriter.getIndexWriter().commit();
	}

	@Test
	public void endIndexThrowsNoException() throws IOException {
		tmWriter.close();
		tmWriter.close();
	}

	public void endIndexCommits() throws IOException {
		tmWriter.indexTranslationUnit(new TranslationUnit(
				new TranslationUnitVariant(locEN, new TextFragment("dax")),
				new TranslationUnitVariant(locKR, new TextFragment(
						"is funny (sometimes)"))));
		tmWriter.close();
		IndexReader reader = IndexReader.open(dir, true);
		assertEquals("num of docs indexed after endIndex", 1, reader.maxDoc());
	}

	public void getDocumentNoSourceContent() {
		assertNull(tmWriter.createDocument(new TranslationUnit(null,
				new TranslationUnitVariant(locEN, new TextFragment(
						"some target")))));
	}

	public void getDocumentEmptySourceContent() {
		assertNull(tmWriter.createDocument(new TranslationUnit(
				new TranslationUnitVariant(locEN, new TextFragment("")),
				new TranslationUnitVariant(locEN, new TextFragment(
						"some target")))));
	}

	@Test(expected = NullPointerException.class)
	public void getDocumentNullTU() {
		tmWriter.createDocument(null);
	}

	@Test
	public void getDocumentValues() {
		String text = "blah blah blah";
		TranslationUnit tu = new TranslationUnit(new TranslationUnitVariant(
				locEN, new TextFragment(text)), new TranslationUnitVariant(
				locFR, new TextFragment("someone")));
		Metadata md = tu.getMetadata();
		md.put(MetadataType.ID, "someId");
		Document doc = tmWriter.createDocument(tu);
		assertEquals("Document's content field", "blah blah blah",
				getFieldValue(doc, SOURCE.name()));
		assertEquals("Document's content exact field", "blah blah blah",
				getFieldValue(doc, SOURCE_EXACT.name()));
		assertEquals("Document's target field", "someone",
				getFieldValue(doc, TARGET.name()));
		assertEquals("Document's source lang field", locEN,
				getFieldValue(doc, SOURCE_LANG.name()));
		assertEquals("Document's target lang field", locFR,
				getFieldValue(doc, TARGET_LANG.name()));
		assertEquals("Document's id field", "someId",
				getFieldValue(doc, MetadataType.ID.fieldName()));
	}

	@Test
	public void testCreateDocument() {
		TextFragment srcFrag = new TextFragment("blah ");
		srcFrag.append(TagType.OPENING, "b", "<b>");
		srcFrag.append("bold");
		srcFrag.append(TagType.CLOSING, "b", "</b>");
		String srcCT = srcFrag.getCodedText();
		String srcCodes = Code.codesToString(srcFrag.getCodes());
		TextFragment trgFrag = new TextFragment("blah ");
		trgFrag.append(TagType.OPENING, "i", "<i>");
		trgFrag.append("gras");
		trgFrag.append(TagType.CLOSING, "i", "</i>");
		String trgCT = trgFrag.getCodedText();
		String trgCodes = Code.codesToString(trgFrag.getCodes());

		TranslationUnit tu = new TranslationUnit(new TranslationUnitVariant(
				locEN, srcFrag), new TranslationUnitVariant(locFR, trgFrag));
		Metadata md = tu.getMetadata();
		md.put(MetadataType.ID, "someId");
		Document doc = tmWriter.createDocument(tu);
		assertEquals("Document's content field", srcCT,
				getFieldValue(doc, SOURCE_EXACT.name()));
		assertEquals("Document's content exact field", srcCT,
				getFieldValue(doc, SOURCE_EXACT.name()));
		assertEquals("Document's target field", trgCT,
				getFieldValue(doc, TARGET.name()));
		assertEquals("Document's source lang field", locEN,
				getFieldValue(doc, SOURCE_LANG.name()));
		assertEquals("Document's target lang field", locFR,
				getFieldValue(doc, TARGET_LANG.name()));
		assertEquals("Document's id field", "someId",
				getFieldValue(doc, MetadataType.ID.fieldName()));
		assertEquals("Document's source codes", srcCodes,
				getFieldValue(doc, SOURCE_CODES.name()));
		assertEquals("Document's target codes", trgCodes,
				getFieldValue(doc, TARGET_CODES.name()));
	}

	@Test
	public void getDocumentNoTarget() {
		Document doc = tmWriter.createDocument(new TranslationUnit(
				new TranslationUnitVariant(locEN, new TextFragment(
						"blah blah blah")), null));
		assertNull("Document's target field should be null",
				doc.getField(TARGET.name()));
	}

	@Test(expected = NullPointerException.class)
	public void indexTranslationUnitNull() throws IOException {
		tmWriter.indexTranslationUnit(null);
	}

	@Test(expected = NullPointerException.class)
	public void indexTranslationUnitNull2() throws IOException {
		tmWriter.indexTranslationUnit(null);
	}

	@Test
	public void indexTranslationUnitNoIndexedDocsBeforeCall()
			throws IOException {
		assertEquals("num of docs indexed", 0, tmWriter.getIndexWriter()
				.numDocs());
	}

	@Test(expected = org.apache.lucene.index.IndexNotFoundException.class)
	public void indexTranslationUnitBeforeCommit() throws IOException {
		// there must be at least one commit or else we get
		// IndexNotFoundException
		tmWriter.indexTranslationUnit(new TranslationUnit(
				new TranslationUnitVariant(locEN, new TextFragment("dax")),
				new TranslationUnitVariant(locEN, new TextFragment(
						"is funny (sometimes)"))));
		IndexReader.open(dir, true);
	}

	@Test
	public void indexTextUnit() throws IOException {
		tmWriter.indexTranslationUnit(new TranslationUnit(
				new TranslationUnitVariant(locEN, new TextFragment("joe")),
				new TranslationUnitVariant(locEN, new TextFragment("schmoe"))));
		assertEquals("num of docs indexed", 1, tmWriter.getIndexWriter()
				.numDocs());
	}

	@Test
	public void indexTextUnit2() throws IOException {
		tmWriter.indexTranslationUnit(new TranslationUnit(
				new TranslationUnitVariant(locEN, new TextFragment("joe")),
				new TranslationUnitVariant(locEN, new TextFragment("schmoe"))));
		assertEquals("num of docs indexed", 1, tmWriter.getIndexWriter()
				.numDocs());
	}

	private String getFieldValue(Document doc, String fieldName) {
		return doc.getField(fieldName).stringValue();
	}

	private int getNumOfHitsFor(String fieldName, String fieldValue)
			throws IOException {
		IndexSearcher is = new IndexSearcher(dir, true);
		PhraseQuery q = new PhraseQuery();
		q.add(new Term(fieldName, fieldValue));
		int numOfHits = is.search(q, 10).scoreDocs.length;
		is.close();
		return numOfHits;
	}

	private Document findDocument(String fieldName, String fieldValue)
			throws IOException {
		IndexSearcher is = new IndexSearcher(dir, true);
		PhraseQuery q = new PhraseQuery();
		q.add(new Term(fieldName, fieldValue));
		TopDocs hits = is.search(q, 1);
		if (hits.totalHits == 0) {
			is.close();
			return null;
		}
		ScoreDoc scoreDoc = hits.scoreDocs[0];
		Document documentFound = is.doc(scoreDoc.doc);
		is.close();
		return documentFound;
	}

}
