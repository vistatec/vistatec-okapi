/*===========================================================================
  Copyright (C) 2010-2013 by the Okapi Framework contributors
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

package net.sf.okapi.common.filterwriter;

import static org.junit.Assert.assertEquals;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;

import net.sf.okapi.common.FileLocation;
import net.sf.okapi.common.IResource;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.annotation.AltTranslationsAnnotation;
import net.sf.okapi.common.annotation.GenericAnnotation;
import net.sf.okapi.common.annotation.GenericAnnotationType;
import net.sf.okapi.common.annotation.GenericAnnotations;
import net.sf.okapi.common.annotation.IssueAnnotation;
import net.sf.okapi.common.annotation.IssueType;
import net.sf.okapi.common.annotation.XLIFFNote;
import net.sf.okapi.common.annotation.XLIFFNote.Annotates;
import net.sf.okapi.common.annotation.XLIFFNoteAnnotation;
import net.sf.okapi.common.query.MatchType;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.InlineAnnotation;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.StartGroup;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.common.resource.TextUnit;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class XLIFFWriterTest {
	
	private XLIFFWriter writer;
	private String root;
	private LocaleId locEN = LocaleId.fromString("en");
	private LocaleId locFR = LocaleId.fromString("fr");
	
	@Before
	public void setUp() {
		writer = new XLIFFWriter();
		root = FileLocation.fromClass(this.getClass()).out("/").toString();
	}

	@Test
	public void testMinimal ()
		throws IOException
	{
		writer.create(root+"out.xlf", null, locEN, null, null, "original.ext", null);
		writer.writeStartFile("newOriginal.txt", null, null);
		writer.writeEndFile();
		writer.close();
		
		String result = readFile(root+"out.xlf");
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<xliff version=\"1.2\" xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" xmlns:okp=\"okapi-framework:xliff-extensions\"" +
			" xmlns:its=\"http://www.w3.org/2005/11/its\" xmlns:itsxlf=\"http://www.w3.org/ns/its-xliff/\" its:version=\"2.0\">\n"
			+ "<file original=\"newOriginal.txt\" source-language=\"en\" datatype=\"x-undefined\">\n"
			+ "<body>\n</body>\n</file>\n</xliff>\n", result);
	}

	@Test
	public void testVeryMinimal ()
		throws IOException
	{
		writer.create(root+"out.xlf", null, locEN, null, null, "original.ext", null);
		writer.close();
		
		String result = readFile(root+"out.xlf");
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<xliff version=\"1.2\" xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" xmlns:okp=\"okapi-framework:xliff-extensions\"" +
			" xmlns:its=\"http://www.w3.org/2005/11/its\" xmlns:itsxlf=\"http://www.w3.org/ns/its-xliff/\" its:version=\"2.0\">\n"
			+ "<file original=\"original.ext\" source-language=\"en\" datatype=\"x-undefined\">\n"
			+ "<body>\n</body>\n</file>\n</xliff>\n", result);
	}

	@Test
	public void testWithExtra ()
		throws IOException
	{
		writer.create(root+"out.xlf", null, locEN, null, null, "original.ext", null);
		writer.writeStartFile(null, null, null, "<phase-group phase-name=\"a\" process-name=\"b\"/>");
		writer.writeEndFile();
		writer.close();
		
		String result = readFile(root+"out.xlf");
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<xliff version=\"1.2\" xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" xmlns:okp=\"okapi-framework:xliff-extensions\"" +
			" xmlns:its=\"http://www.w3.org/2005/11/its\" xmlns:itsxlf=\"http://www.w3.org/ns/its-xliff/\" its:version=\"2.0\">\n"
			+ "<file original=\"unknown\" source-language=\"en\" datatype=\"x-undefined\">\n"
			+ "<header><phase-group phase-name=\"a\" process-name=\"b\"/></header>\n"
			+ "<body>\n</body>\n</file>\n</xliff>\n", result);
	}

	@Test
	public void testBasicSourceOnly ()
		throws IOException
	{
		writer.create(root+"out.xlf", null, locEN, null, null, "original.ext", null);
		ITextUnit tu = new TextUnit("tu1", "src1 with <>");
		writer.writeTextUnit(tu);
		writer.close();

		String result = readFile(root+"out.xlf");
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<xliff version=\"1.2\" xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" xmlns:okp=\"okapi-framework:xliff-extensions\"" +
			" xmlns:its=\"http://www.w3.org/2005/11/its\" xmlns:itsxlf=\"http://www.w3.org/ns/its-xliff/\" its:version=\"2.0\">\n"
			+ "<file original=\"original.ext\" source-language=\"en\" datatype=\"x-undefined\">\n"
			+ "<body>\n"
			+ "<trans-unit id=\"tu1\">\n"
			+ "<source xml:lang=\"en\">src1 with &lt;></source>\n"
			+ "</trans-unit>\n"
			+ "</body>\n</file>\n</xliff>\n", result);
	}
	
	@Test
	public void testXliffNote()
		throws IOException
	{
		writer.create(root+"out.xlf", null, locEN, null, null, "original.ext", null);
		ITextUnit tu = new TextUnit("tu1", "src1 with <>");
		XLIFFNote n = new XLIFFNote("A Note");
		n.setAnnotates(Annotates.SOURCE);
		n.setFrom("reviewer");
		XLIFFNoteAnnotation notes = new XLIFFNoteAnnotation();
		notes.add(n);
		tu.setAnnotation(notes);
		writer.writeTextUnit(tu);
		writer.close();

		String result = readFile(root+"out.xlf");
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<xliff version=\"1.2\" xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" xmlns:okp=\"okapi-framework:xliff-extensions\"" +
			" xmlns:its=\"http://www.w3.org/2005/11/its\" xmlns:itsxlf=\"http://www.w3.org/ns/its-xliff/\" its:version=\"2.0\">\n"
			+ "<file original=\"original.ext\" source-language=\"en\" datatype=\"x-undefined\">\n"
			+ "<body>\n"
			+ "<trans-unit id=\"tu1\">\n"
			+ "<source xml:lang=\"en\">src1 with &lt;></source>\n"
			+ "<note annotates=\"source\" from=\"reviewer\">A Note</note>\n"
			+ "</trans-unit>\n"
			+ "</body>\n</file>\n</xliff>\n", result);
	}
	
	/*
	 * remove when PROPERTY.NOT/TRANSNOTE are removed
	 */
	@Deprecated
	@Test
	public void testPropertyNote()
		throws IOException
	{
		writer.create(root+"out.xlf", null, locEN, null, null, "original.ext", null);
		ITextUnit tu = new TextUnit("tu1", "src1 with <>");
		tu.setProperty(new Property(Property.TRANSNOTE, "A Note"));
		XLIFFNote n = new XLIFFNote("A Note2");
		n.setAnnotates(Annotates.SOURCE);
		n.setFrom("reviewer");
		XLIFFNoteAnnotation notes = new XLIFFNoteAnnotation();
		notes.add(n);
		tu.setAnnotation(notes);
		writer.writeTextUnit(tu);
		writer.close();

		String result = readFile(root+"out.xlf");
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<xliff version=\"1.2\" xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" xmlns:okp=\"okapi-framework:xliff-extensions\"" +
			" xmlns:its=\"http://www.w3.org/2005/11/its\" xmlns:itsxlf=\"http://www.w3.org/ns/its-xliff/\" its:version=\"2.0\">\n"
			+ "<file original=\"original.ext\" source-language=\"en\" datatype=\"x-undefined\">\n"
			+ "<body>\n"
			+ "<trans-unit id=\"tu1\">\n"
			+ "<source xml:lang=\"en\">src1 with &lt;></source>\n"
			+ "<note from=\"translator\">A Note</note>\n"
			+ "<note annotates=\"source\" from=\"reviewer\">A Note2</note>\n"
			+ "</trans-unit>\n"
			+ "</body>\n</file>\n</xliff>\n", result);
	}
	
	/*
	 * remove when PROPERTY.NOT/TRANSNOTE are removed
	 */
	@Deprecated
	@Test
	public void testPropertyNoteWithXliffNote()
		throws IOException
	{
		writer.create(root+"out.xlf", null, locEN, null, null, "original.ext", null);
		ITextUnit tu = new TextUnit("tu1", "src1 with <>");
		tu.setProperty(new Property(Property.TRANSNOTE, "A Note"));
		writer.writeTextUnit(tu);
		writer.close();

		String result = readFile(root+"out.xlf");
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<xliff version=\"1.2\" xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" xmlns:okp=\"okapi-framework:xliff-extensions\"" +
			" xmlns:its=\"http://www.w3.org/2005/11/its\" xmlns:itsxlf=\"http://www.w3.org/ns/its-xliff/\" its:version=\"2.0\">\n"
			+ "<file original=\"original.ext\" source-language=\"en\" datatype=\"x-undefined\">\n"
			+ "<body>\n"
			+ "<trans-unit id=\"tu1\">\n"
			+ "<source xml:lang=\"en\">src1 with &lt;></source>\n"
			+ "<note from=\"translator\">A Note</note>\n"
			+ "</trans-unit>\n"
			+ "</body>\n</file>\n</xliff>\n", result);
	}

	@Test
	public void testSourceOnlyWithSimpleAnnotation ()
		throws IOException
	{
		// Create a text fragment with a couple of inline codes
		TextFragment tf = new TextFragment("Source text in ");
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append("bold");
		tf.append(TagType.CLOSING, "b", "</b>");
		// Create a text unit with the text fragment as its source content
		ITextUnit tu = new TextUnit("tu1");
		tu.setSourceContent(tf);
		// Annotate the bold content with some ITS information
		// "Source text in ##bold##"
		//  01234567890123456789012
		tf.annotate(17, 21, GenericAnnotationType.GENERIC, new GenericAnnotations(
			new GenericAnnotation(GenericAnnotationType.TERM,
				GenericAnnotationType.TERM_INFO, "http://dbpedia.org/page/Emphasis_(typography)")));
		
		// Create the output XLIFF document
		writer.create(root+"out.xlf", null, locEN, null, "html", "original.html", null);
		// Output the text unit
		writer.writeTextUnit(tu);
		// Close the XLIFF output
		writer.close();

		String result = readFile(root+"out.xlf");
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<xliff version=\"1.2\" xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" xmlns:okp=\"okapi-framework:xliff-extensions\"" +
			" xmlns:its=\"http://www.w3.org/2005/11/its\" xmlns:itsxlf=\"http://www.w3.org/ns/its-xliff/\" its:version=\"2.0\">\n"
			+ "<file original=\"original.html\" source-language=\"en\" datatype=\"html\">\n"
			+ "<body>\n"
			+ "<trans-unit id=\"tu1\">\n"
			+ "<source xml:lang=\"en\">Source text in <g id=\"1\">"
			+ "<mrk itsxlf:termInfo=\"http://dbpedia.org/page/Emphasis_(typography)\" mtype=\"term\">bold</mrk>"
			+ "</g></source>\n"
			+ "</trans-unit>\n"
			+ "</body>\n</file>\n</xliff>\n", result);
	}

	@Test
	public void testAnnotations ()
		throws IOException
	{
		writer.create(root+"out.xlf", null, locEN, locFR, null, "original.ext", null);
		writer.writeStartFile(null, null, null);
		ITextUnit tu = new TextUnit("tu1");
		TextFragment tf = tu.getSource().getFirstSegment().getContent();
		tf.append("t1 t2");
		GenericAnnotations anns1 = new GenericAnnotations();
		anns1.setData("lqi1");
		IssueAnnotation ia1 = new IssueAnnotation(IssueType.LANGUAGETOOL_ERROR, "msg1", 1, "s1", -1, -1, 0, 2, null);
		ia1.setITSType("misspelling");
		ia1.setBoolean(GenericAnnotationType.LQI_ENABLED, false);
		ia1.setString(GenericAnnotationType.LQI_PROFILEREF, "uri");
		anns1.add(ia1);
		IssueAnnotation ia2 = new IssueAnnotation(IssueType.LANGUAGETOOL_ERROR, "msg2", 1, "s1", -1, -1, 0, 2, null);
		ia2.setITSType("grammar");
		anns1.add(ia2);
		tf.annotate(0, 2, GenericAnnotationType.GENERIC, anns1);
		tf = tu.createTarget(locFR, false, IResource.COPY_ALL).getFirstContent();
		GenericAnnotation ann3 = ia2.clone();
		ann3.setString(GenericAnnotationType.LQI_COMMENT, "rem3");
		ann3.setDouble(GenericAnnotationType.LQI_SEVERITY, 99.0);
		GenericAnnotations anns3 = new GenericAnnotations();
		anns3.setData("lqi2");
		anns3.add(ann3);
		anns3.add(new GenericAnnotation(GenericAnnotationType.LQI,
			GenericAnnotationType.LQI_COMMENT, "rem4"));
		tf.annotate(3+4, 5+4, GenericAnnotationType.GENERIC, anns3); // +4 is for the markers of the previous annotation
		writer.writeTextUnit(tu);
		writer.writeEndFile();
		writer.close();		

		String result = readFile(root+"out.xlf");
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<xliff version=\"1.2\" xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" xmlns:okp=\"okapi-framework:xliff-extensions\"" +
			" xmlns:its=\"http://www.w3.org/2005/11/its\" xmlns:itsxlf=\"http://www.w3.org/ns/its-xliff/\" its:version=\"2.0\">\n"
			+ "<file original=\"unknown\" source-language=\"en\" target-language=\"fr\" datatype=\"x-undefined\">\n"
			+ "<body>\n<trans-unit id=\"tu1\">\n"
			+ "<source xml:lang=\"en\"><mrk its:locQualityIssuesRef=\"#VARID\" mtype=\"x-its\">t1</mrk> t2</source>\n"
			+ "<target xml:lang=\"fr\"><mrk its:locQualityIssuesRef=\"#VARID\" mtype=\"x-its\">t1</mrk> "
			+ "<mrk its:locQualityIssuesRef=\"#VARID\" mtype=\"x-its\">t2</mrk></target>\n"
			+ "<its:locQualityIssues xmlns:its=\"http://www.w3.org/2005/11/its\" xmlns:okp=\"okapi-framework:xliff-extensions\" version=\"2.0\" xml:id=\"VARID\">\n"
			+ "<its:locQualityIssue locQualityIssueComment=\"msg1\" locQualityIssueEnabled=\"no\" locQualityIssueProfileRef=\"uri\" locQualityIssueSeverity=\"1\" locQualityIssueType=\"misspelling\" okp:lqiType=\"LANGUAGETOOL_ERROR\" okp:lqiPos=\"-1 -1 0 2\" okp:lqiSegId=\"s1\"/>\n"
			+ "<its:locQualityIssue locQualityIssueComment=\"msg2\" locQualityIssueSeverity=\"1\" locQualityIssueType=\"grammar\" okp:lqiType=\"LANGUAGETOOL_ERROR\" okp:lqiPos=\"-1 -1 0 2\" okp:lqiSegId=\"s1\"/>\n"
			+ "</its:locQualityIssues>\n"
			+ "<its:locQualityIssues xmlns:its=\"http://www.w3.org/2005/11/its\" version=\"2.0\" xml:id=\"VARID\">\n"
			+ "<its:locQualityIssue locQualityIssueComment=\"msg1\" locQualityIssueEnabled=\"no\" locQualityIssueProfileRef=\"uri\" locQualityIssueSeverity=\"1\" locQualityIssueType=\"misspelling\" okp:lqiPos=\"-1 -1 0 2\"/>\n"
			+ "<its:locQualityIssue locQualityIssueComment=\"msg2\" locQualityIssueSeverity=\"1\" locQualityIssueType=\"grammar\" okp:lqiPos=\"-1 -1 0 2\"/>\n"
			+ "</its:locQualityIssues>\n"
			+ "<its:locQualityIssues xmlns:its=\"http://www.w3.org/2005/11/its\" version=\"2.0\" xml:id=\"VARID\">\n"
			+ "<its:locQualityIssue locQualityIssueComment=\"rem3\" locQualityIssueSeverity=\"99\" locQualityIssueType=\"grammar\" okp:lqiPos=\"-1 -1 0 2\"/>\n"
			+ "<its:locQualityIssue locQualityIssueComment=\"rem4\"/>\n"
			+ "</its:locQualityIssues>\n"
			+ "</trans-unit>\n"
			+ "</body>\n</file>\n</xliff>\n", stripVariableID(result));
	}

	@Test
	public void testSingleLQIAnnotation ()
		throws IOException
	{
		writer.create(root+"out.xlf", null, locEN, locFR, null, "original.ext", null);
		writer.writeStartFile(null, null, null);
		ITextUnit tu = new TextUnit("tu1");
		TextFragment tf = tu.getSource().getFirstSegment().getContent();
		tf.append("t1 t2");
		IssueAnnotation ia1 = new IssueAnnotation(IssueType.LANGUAGETOOL_ERROR, "msg1", 1, "s1", -1, -1, 0, 2, null);
		ia1.setITSType("misspelling");
		ia1.setBoolean(GenericAnnotationType.LQI_ENABLED, false);
		ia1.setString(GenericAnnotationType.LQI_PROFILEREF, "uri");
		GenericAnnotation.addAnnotation(tu.getSource(), ia1);
		writer.writeTextUnit(tu);
		writer.writeEndFile();
		writer.close();		

		String result = readFile(root+"out.xlf");
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<xliff version=\"1.2\" xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" xmlns:okp=\"okapi-framework:xliff-extensions\"" +
			" xmlns:its=\"http://www.w3.org/2005/11/its\" xmlns:itsxlf=\"http://www.w3.org/ns/its-xliff/\" its:version=\"2.0\">\n"
			+ "<file original=\"unknown\" source-language=\"en\" target-language=\"fr\" datatype=\"x-undefined\">\n"
			+ "<body>\n<trans-unit id=\"tu1\">\n"
			+ "<source xml:lang=\"en\" its:locQualityIssueComment=\"msg1\" its:locQualityIssueEnabled=\"no\" its:locQualityIssueProfileRef=\"uri\" its:locQualityIssueSeverity=\"1\" its:locQualityIssueType=\"misspelling\" okp:lqiType=\"LANGUAGETOOL_ERROR\" okp:lqiPos=\"-1 -1 0 2\" okp:lqiSegId=\"s1\">t1 t2</source>\n"
			+ "<target xml:lang=\"fr\" its:locQualityIssueComment=\"msg1\" its:locQualityIssueEnabled=\"no\" its:locQualityIssueProfileRef=\"uri\" its:locQualityIssueSeverity=\"1\" its:locQualityIssueType=\"misspelling\" okp:lqiType=\"LANGUAGETOOL_ERROR\" okp:lqiPos=\"-1 -1 0 2\" okp:lqiSegId=\"s1\">t1 t2</target>\n"
			+ "</trans-unit>\n"
			+ "</body>\n</file>\n</xliff>\n", stripVariableID(result));
	}

	@Test
	public void testLQRAnnotations ()
		throws IOException
	{
		writer.create(root+"out.xlf", null, locEN, locFR, null, "original.ext", null);
		writer.writeStartFile(null, null, null);
		ITextUnit tu = new TextUnit("tu1", "Text1");
		tu.getSource().getSegments().create(0, 5);
		TextContainer tc = tu.createTarget(LocaleId.FRENCH, false, IResource.COPY_ALL);
		// LQR on <target>
		GenericAnnotation.addAnnotation(tc, new GenericAnnotation(GenericAnnotationType.LQR,
			GenericAnnotationType.LQR_SCORE, 88.0,
			GenericAnnotationType.LQR_SCORETHRESHOLD, 90.0,
			GenericAnnotationType.LQR_PROFILEREF, "uri1"));
		// LQR on the segment
		tc.getFirstSegment().setAnnotation(new GenericAnnotations(
			new GenericAnnotation(GenericAnnotationType.LQR,
				GenericAnnotationType.LQR_SCORE, 77.7,
				GenericAnnotationType.LQR_SCORETHRESHOLD, 70.0,
				GenericAnnotationType.LQR_PROFILEREF, "uri2")));
		// LQR on a span
		tc.getFirstContent().annotate(0, 5, GenericAnnotationType.GENERIC,
			new GenericAnnotations(new GenericAnnotation(GenericAnnotationType.LQR,
				GenericAnnotationType.LQR_VOTE, 1,
				GenericAnnotationType.LQR_VOTETHRESHOLD, 100,
				GenericAnnotationType.LQR_PROFILEREF, "uri3")));
		writer.writeTextUnit(tu);
		
		tu = new TextUnit("tu2", "Text2");
		tc = tu.createTarget(LocaleId.FRENCH, false, IResource.COPY_ALL);
		GenericAnnotation.addAnnotation(tc, new GenericAnnotation(GenericAnnotationType.LQR,
			GenericAnnotationType.LQR_VOTE, 12345678,
			GenericAnnotationType.LQR_VOTETHRESHOLD, 50000,
			GenericAnnotationType.LQR_PROFILEREF, "uri4"));
		writer.writeTextUnit(tu);
		writer.writeEndFile();
		writer.close();

		String result = readFile(root+"out.xlf");
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<xliff version=\"1.2\" xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" xmlns:okp=\"okapi-framework:xliff-extensions\"" +
			" xmlns:its=\"http://www.w3.org/2005/11/its\" xmlns:itsxlf=\"http://www.w3.org/ns/its-xliff/\" its:version=\"2.0\">\n"
			+ "<file original=\"unknown\" source-language=\"en\" target-language=\"fr\" datatype=\"x-undefined\">\n"
			+ "<body>\n"
			+ "<trans-unit id=\"tu1\">\n"
			+ "<source xml:lang=\"en\">Text1</source>\n"
			+ "<seg-source><mrk mid=\"0\" mtype=\"seg\">Text1</mrk></seg-source>\n"
			+ "<target xml:lang=\"fr\" its:locQualityRatingScore=\"88\" its:locQualityRatingScoreThreshold=\"90\" its:locQualityRatingProfileRef=\"uri1\">"
			+ "<mrk mid=\"0\" mtype=\"seg\" its:locQualityRatingScore=\"77.7\" its:locQualityRatingScoreThreshold=\"70\" its:locQualityRatingProfileRef=\"uri2\">"
			+ "<mrk its:locQualityRatingVote=\"1\" its:locQualityRatingVoteThreshold=\"100\" its:locQualityRatingProfileRef=\"uri3\" mtype=\"x-its\">"
			+ "Text1</mrk></mrk></target>\n"
			+ "</trans-unit>\n"
			+ "<trans-unit id=\"tu2\">\n"
			+ "<source xml:lang=\"en\">Text2</source>\n"
			+ "<target xml:lang=\"fr\" its:locQualityRatingVote=\"12345678\" its:locQualityRatingVoteThreshold=\"50000\" its:locQualityRatingProfileRef=\"uri4\">"
			+ "Text2</target>\n"
			+ "</trans-unit>\n"
			+ "</body>\n</file>\n</xliff>\n", stripVariableID(result));
	}

	@Test
	public void testTerminologyAnnotations ()
		throws IOException
	{
		writer.create(root+"out.xlf", null, locEN, locFR, null, "original.ext", null);
		writer.writeStartFile(null, null, null);
		ITextUnit tu = new TextUnit("tu1");
		TextFragment tf = tu.getSource().getFirstSegment().getContent();
		tf.append("t1");
		
		TextContainer tc = tu.getSource();
		GenericAnnotations anns = new GenericAnnotations();
		anns.add(new GenericAnnotation(GenericAnnotationType.TERM,
			GenericAnnotationType.TERM_CONFIDENCE, 0.5,
			GenericAnnotationType.TERM_INFO, "REF:info"));
		tc.setAnnotation(anns);
		
		anns = new GenericAnnotations();
		anns.add(new GenericAnnotation(GenericAnnotationType.TERM,
			GenericAnnotationType.TERM_CONFIDENCE, 0.7,
			GenericAnnotationType.TERM_INFO, "REF:info2"));
		tc.get(0).getContent().annotate(0, 2, GenericAnnotationType.GENERIC, anns);
		
		writer.writeTextUnit(tu);
		writer.writeEndFile();
		writer.close();		
//TODO: Term at TU level not OK
		String result = readFile(root+"out.xlf");
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<xliff version=\"1.2\" xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" xmlns:okp=\"okapi-framework:xliff-extensions\"" +
			" xmlns:its=\"http://www.w3.org/2005/11/its\" xmlns:itsxlf=\"http://www.w3.org/ns/its-xliff/\" its:version=\"2.0\">\n"
			+ "<file original=\"unknown\" source-language=\"en\" target-language=\"fr\" datatype=\"x-undefined\">\n"
			+ "<body>\n<trans-unit id=\"tu1\">\n"
			+ "<source xml:lang=\"en\" its:term=\"yes\" itsxlf:termConfidence=\"0.5\" itsxlf:termInfoRef=\"info\">"
			+ "<mrk itsxlf:termConfidence=\"0.7\" itsxlf:termInfoRef=\"info2\" mtype=\"term\">t1</mrk></source>\n"
			+ "<target xml:lang=\"fr\" its:term=\"yes\" itsxlf:termConfidence=\"0.5\" itsxlf:termInfoRef=\"info\">"
			+ "<mrk itsxlf:termConfidence=\"0.7\" itsxlf:termInfoRef=\"info2\" mtype=\"term\">t1</mrk></target>\n"
			+ "</trans-unit>\n"
			+ "</body>\n</file>\n</xliff>\n", stripVariableID(result));
	}

	@Test
	public void testmultipleLQI ()
		throws IOException
	{
		writer.create(root+"out.xlf", null, locEN, locFR, null, "original.ext", null);
		writer.writeStartFile(null, null, null);
		ITextUnit tu = new TextUnit("tu1");
		
		TextFragment tf = tu.getSource().getFirstSegment().getContent();
		tf.setCodedText("Span 1 Span 2");
		//               0123456789012
		// First LQI
		GenericAnnotations anns = new GenericAnnotations();
		anns.add(new GenericAnnotation(GenericAnnotationType.LQI,
			GenericAnnotationType.LQI_COMMENT, "comment-1a"));
		anns.add(new GenericAnnotation(GenericAnnotationType.LQI,
			GenericAnnotationType.LQI_COMMENT, "comment-1b"));
		tf.annotate(0, 6, GenericAnnotationType.GENERIC, anns);
		// second LQI
		anns = new GenericAnnotations();
		anns.add(new GenericAnnotation(GenericAnnotationType.LQI,
			GenericAnnotationType.LQI_COMMENT, "comment-2a"));
		anns.add(new GenericAnnotation(GenericAnnotationType.LQI,
			GenericAnnotationType.LQI_COMMENT, "comment-2b"));
		tf.annotate(11, 17, GenericAnnotationType.GENERIC, anns); // +4 is for first marker

		tu.createTarget(locFR, false, IResource.COPY_ALL);

		writer.writeTextUnit(tu);
		writer.writeEndFile();
		writer.close();
		
		String result = readFile(root+"out.xlf");
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<xliff version=\"1.2\" xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" xmlns:okp=\"okapi-framework:xliff-extensions\"" +
			" xmlns:its=\"http://www.w3.org/2005/11/its\" xmlns:itsxlf=\"http://www.w3.org/ns/its-xliff/\" its:version=\"2.0\">\n"
			+ "<file original=\"unknown\" source-language=\"en\" target-language=\"fr\" datatype=\"x-undefined\">\n"
			+ "<body>\n<trans-unit id=\"tu1\">\n"
			+ "<source xml:lang=\"en\"><mrk its:locQualityIssuesRef=\"#VARID\" mtype=\"x-its\">Span 1</mrk> "
			+ "<mrk its:locQualityIssuesRef=\"#VARID\" mtype=\"x-its\">Span 2</mrk></source>\n"
			+ "<target xml:lang=\"fr\"><mrk its:locQualityIssuesRef=\"#VARID\" mtype=\"x-its\">Span 1</mrk> "
			+ "<mrk its:locQualityIssuesRef=\"#VARID\" mtype=\"x-its\">Span 2</mrk></target>\n"
			+ "<its:locQualityIssues xmlns:its=\"http://www.w3.org/2005/11/its\" version=\"2.0\" xml:id=\"VARID\">\n"
			+ "<its:locQualityIssue locQualityIssueComment=\"comment-1a\"/>\n"
			+ "<its:locQualityIssue locQualityIssueComment=\"comment-1b\"/>\n"
			+ "</its:locQualityIssues>\n"
			+ "<its:locQualityIssues xmlns:its=\"http://www.w3.org/2005/11/its\" version=\"2.0\" xml:id=\"VARID\">\n"
			+ "<its:locQualityIssue locQualityIssueComment=\"comment-2a\"/>\n"
			+ "<its:locQualityIssue locQualityIssueComment=\"comment-2b\"/>\n"
			+ "</its:locQualityIssues>\n"
			+ "<its:locQualityIssues xmlns:its=\"http://www.w3.org/2005/11/its\" version=\"2.0\" xml:id=\"VARID\">\n"
			+ "<its:locQualityIssue locQualityIssueComment=\"comment-1a\"/>\n"
			+ "<its:locQualityIssue locQualityIssueComment=\"comment-1b\"/>\n"
			+ "</its:locQualityIssues>\n"
			+ "<its:locQualityIssues xmlns:its=\"http://www.w3.org/2005/11/its\" version=\"2.0\" xml:id=\"VARID\">\n"
			+ "<its:locQualityIssue locQualityIssueComment=\"comment-2a\"/>\n"
			+ "<its:locQualityIssue locQualityIssueComment=\"comment-2b\"/>\n"
			+ "</its:locQualityIssues>\n"
			+ "</trans-unit>\n"
			+ "</body>\n</file>\n</xliff>\n", stripVariableID(result));
	}
	
	@Test
	public void testTextWithDefaultCodes ()
		throws IOException
	{
		writer.create(root+"out.xlf", null, locEN, null, null, "original.ext", null);
		ITextUnit tu = new TextUnit("tu1");
		tu.getSource().getFirstSegment().getContent().append(TagType.OPENING, "z", "<z>");
		tu.getSource().getFirstSegment().getContent().append("s1");
		tu.getSource().getFirstSegment().getContent().append(TagType.CLOSING, "z", "</z>");
		tu.getSource().getFirstSegment().getContent().append(TagType.PLACEHOLDER, "br", "<br/>");
		writer.writeTextUnit(tu);
		writer.close();

		String result = readFile(root+"out.xlf");
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<xliff version=\"1.2\" xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" xmlns:okp=\"okapi-framework:xliff-extensions\"" +
			" xmlns:its=\"http://www.w3.org/2005/11/its\" xmlns:itsxlf=\"http://www.w3.org/ns/its-xliff/\" its:version=\"2.0\">\n"
			+ "<file original=\"original.ext\" source-language=\"en\" datatype=\"x-undefined\">\n"
			+ "<body>\n"
			+ "<trans-unit id=\"tu1\">\n"
			+ "<source xml:lang=\"en\"><g id=\"1\">s1</g><x id=\"2\"/></source>\n"
			+ "</trans-unit>\n"
			+ "</body>\n</file>\n</xliff>\n", result);
	}

	@Test
	public void testTextWithEncapsulatedCodes ()
		throws IOException
	{
		((XLIFFWriterParameters)writer.getParameters()).setPlaceholderMode(false);
		writer.create(root+"out.xlf", null, locEN, null, null, "original.ext", null);
		ITextUnit tu = new TextUnit("tu1");
		tu.getSource().getFirstSegment().getContent().append(TagType.OPENING, "z", "<z>");
		tu.getSource().getFirstSegment().getContent().append("s1");
		tu.getSource().getFirstSegment().getContent().append(TagType.CLOSING, "z", "</z>");
		tu.getSource().getFirstSegment().getContent().append(TagType.PLACEHOLDER, "br", "<br/>");
		writer.writeTextUnit(tu);
		writer.close();

		String result = readFile(root+"out.xlf");
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<xliff version=\"1.2\" xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" xmlns:okp=\"okapi-framework:xliff-extensions\"" +
			" xmlns:its=\"http://www.w3.org/2005/11/its\" xmlns:itsxlf=\"http://www.w3.org/ns/its-xliff/\" its:version=\"2.0\">\n"
			+ "<file original=\"original.ext\" source-language=\"en\" datatype=\"x-undefined\">\n"
			+ "<body>\n"
			+ "<trans-unit id=\"tu1\">\n"
			+ "<source xml:lang=\"en\"><bpt id=\"1\">&lt;z></bpt>s1<ept id=\"1\">&lt;/z></ept><ph id=\"2\">&lt;br/></ph></source>\n"
			+ "</trans-unit>\n"
			+ "</body>\n</file>\n</xliff>\n", result);
	}

	@Test
	public void testBasicSourceOnlyGtEscaped ()
		throws IOException
	{
		writer.create(root+"out.xlf", null, locEN, null, null, "original.ext", null);
		ITextUnit tu = new TextUnit("tu1", "src1 with <>");
		((XLIFFWriterParameters)writer.getParameters()).setEscapeGt(true);
		writer.writeTextUnit(tu);
		writer.close();

		String result = readFile(root+"out.xlf");
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<xliff version=\"1.2\" xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" xmlns:okp=\"okapi-framework:xliff-extensions\"" +
			" xmlns:its=\"http://www.w3.org/2005/11/its\" xmlns:itsxlf=\"http://www.w3.org/ns/its-xliff/\" its:version=\"2.0\">\n"
			+ "<file original=\"original.ext\" source-language=\"en\" datatype=\"x-undefined\">\n"
			+ "<body>\n"
			+ "<trans-unit id=\"tu1\">\n"
			+ "<source xml:lang=\"en\">src1 with &lt;&gt;</source>\n"
			+ "</trans-unit>\n"
			+ "</body>\n</file>\n</xliff>\n", result);
	}

	@Test
	public void testBasicSourceAndTarget ()
		throws IOException
	{
		writer.create(root+"out.xlf", null, locEN, locFR, null, "original.ext", null);
		ITextUnit tu = new TextUnit("tu1", "src1<&\"\'>");
		tu.setSourceProperty(new Property(Property.COORDINATES, "1;2;3;4"));
		tu.setTarget(locFR, new TextContainer("trg1"));
		tu.setTargetProperty(locFR, new Property(Property.COORDINATES, "11;22;33;44"));
		tu.setTargetProperty(locFR, new Property(Property.STATE_QUALIFIER, "id-match"));
		writer.writeTextUnit(tu);
		writer.close();

		String result = readFile(root+"out.xlf");
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<xliff version=\"1.2\" xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" xmlns:okp=\"okapi-framework:xliff-extensions\"" +
			" xmlns:its=\"http://www.w3.org/2005/11/its\" xmlns:itsxlf=\"http://www.w3.org/ns/its-xliff/\" its:version=\"2.0\">\n"
			+ "<file original=\"original.ext\" source-language=\"en\" target-language=\"fr\" datatype=\"x-undefined\">\n"
			+ "<body>\n"
			+ "<trans-unit id=\"tu1\">\n"
			+ "<source xml:lang=\"en\" coord=\"1;2;3;4\">src1&lt;&amp;\"\'></source>\n"
			+ "<target xml:lang=\"fr\" state-qualifier=\"id-match\" coord=\"11;22;33;44\">trg1</target>\n"
			+ "</trans-unit>\n"
			+ "</body>\n</file>\n</xliff>\n", result);
	}

	@Test
	public void testCompleteSourceAndTarget ()
		throws IOException
	{
		writer.create(root+"out.xlf", "skel.skl", locEN, locFR, "dtValue", "original.ext", "messageValue");
		ITextUnit tu = new TextUnit("tu1", "src1<&\"\'>");
		tu.setTarget(locFR, new TextContainer("trg1"));
		writer.writeTextUnit(tu);
		writer.close();

		String result = readFile(root+"out.xlf");
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<xliff version=\"1.2\" xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" xmlns:okp=\"okapi-framework:xliff-extensions\"" +
			" xmlns:its=\"http://www.w3.org/2005/11/its\" xmlns:itsxlf=\"http://www.w3.org/ns/its-xliff/\" its:version=\"2.0\">\n"
			+ "<!--messageValue-->\n"
			+ "<file original=\"original.ext\" source-language=\"en\" target-language=\"fr\" datatype=\"x-dtValue\">\n"
			+ "<header><skl><external-file href=\"skel.skl\"></external-file></skl></header>\n"
			+ "<body>\n"
			+ "<trans-unit id=\"tu1\">\n"
			+ "<source xml:lang=\"en\">src1&lt;&amp;\"\'></source>\n"
			+ "<target xml:lang=\"fr\">trg1</target>\n"
			+ "</trans-unit>\n"
			+ "</body>\n</file>\n</xliff>\n", result);
	}

	@Test
	public void testCompleteSourceAndTargetWithTool ()
		throws IOException
	{
		XLIFFWriter w = new XLIFFWriter();
		XLIFFWriterParameters p = new XLIFFWriterParameters();
		p.setToolId("okapi");
		p.setToolName("okapi-test");
		p.setToolVersion("version-1.1.1");
		p.setToolCompany("okapi");
		w.setParameters(p);

		w.create(root+"out.xlf", "skel.skl", locEN, locFR, "dtValue", "original.ext", "messageValue");
		ITextUnit tu = new TextUnit("tu1", "src1<&\"\'>");
		tu.setTarget(locFR, new TextContainer("trg1"));
		w.writeTextUnit(tu);
		w.close();

		String result = readFile(root+"out.xlf");
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<xliff version=\"1.2\" xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" xmlns:okp=\"okapi-framework:xliff-extensions\"" +
			" xmlns:its=\"http://www.w3.org/2005/11/its\" xmlns:itsxlf=\"http://www.w3.org/ns/its-xliff/\" its:version=\"2.0\">\n"
			+ "<!--messageValue-->\n"
			+ "<file original=\"original.ext\" source-language=\"en\" target-language=\"fr\" datatype=\"x-dtValue\">\n"
			+ "<header><skl>"
			+ "<external-file href=\"skel.skl\"></external-file></skl>"
			+ "<tool tool-id=\"okapi\" tool-name=\"okapi-test\" tool-version=\"version-1.1.1\" tool-company=\"okapi\"></tool>"
			+ "</header>\n"
			+ "<body>\n"
			+ "<trans-unit id=\"tu1\">\n"
			+ "<source xml:lang=\"en\">src1&lt;&amp;\"\'></source>\n"
			+ "<target xml:lang=\"fr\">trg1</target>\n"
			+ "</trans-unit>\n"
			+ "</body>\n</file>\n</xliff>\n", result);
	}
	
	@Test
	public void testCompleteSourceAndTargetWithoutTool ()
		throws IOException
	{
		XLIFFWriter w = new XLIFFWriter();
		XLIFFWriterParameters p = new XLIFFWriterParameters();
		// tool-name not defined but is required, tool element not written!!
		p.setToolId("okapi");		
		p.setToolVersion("version-1.1.1");
		p.setToolCompany("okapi");
		w.setParameters(p);

		w.create(root+"out.xlf", "skel.skl", locEN, locFR, "dtValue", "original.ext", "messageValue");
		ITextUnit tu = new TextUnit("tu1", "src1<&\"\'>");
		tu.setTarget(locFR, new TextContainer("trg1"));
		w.writeTextUnit(tu);
		w.close();

		String result = readFile(root+"out.xlf");
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<xliff version=\"1.2\" xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" xmlns:okp=\"okapi-framework:xliff-extensions\"" +
			" xmlns:its=\"http://www.w3.org/2005/11/its\" xmlns:itsxlf=\"http://www.w3.org/ns/its-xliff/\" its:version=\"2.0\">\n"
			+ "<!--messageValue-->\n"
			+ "<file original=\"original.ext\" source-language=\"en\" target-language=\"fr\" datatype=\"x-dtValue\">\n"
			+ "<header><skl>"
			+ "<external-file href=\"skel.skl\"></external-file></skl>"
			+ "</header>\n"
			+ "<body>\n"
			+ "<trans-unit id=\"tu1\">\n"
			+ "<source xml:lang=\"en\">src1&lt;&amp;\"\'></source>\n"
			+ "<target xml:lang=\"fr\">trg1</target>\n"
			+ "</trans-unit>\n"
			+ "</body>\n</file>\n</xliff>\n", result);

		p = new XLIFFWriterParameters();
		// tool-id not defined but is required, tool element not written!!
		p.setToolName("okapi-test");
		p.setToolVersion("version-1.1.1");
		p.setToolCompany("okapi");
		w.setParameters(p);
		w.create(root+"out.xlf", "skel.skl", locEN, locFR, "dtValue", "original.ext", "messageValue");
		tu = new TextUnit("tu1", "src1<&\"\'>");
		tu.setTarget(locFR, new TextContainer("trg1"));
		w.writeTextUnit(tu);
		w.close();

		result = readFile(root+"out.xlf");
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<xliff version=\"1.2\" xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" xmlns:okp=\"okapi-framework:xliff-extensions\"" +
			" xmlns:its=\"http://www.w3.org/2005/11/its\" xmlns:itsxlf=\"http://www.w3.org/ns/its-xliff/\" its:version=\"2.0\">\n"
			+ "<!--messageValue-->\n"
			+ "<file original=\"original.ext\" source-language=\"en\" target-language=\"fr\" datatype=\"x-dtValue\">\n"
			+ "<header><skl>"
			+ "<external-file href=\"skel.skl\"></external-file></skl>"
			+ "</header>\n"
			+ "<body>\n"
			+ "<trans-unit id=\"tu1\">\n"
			+ "<source xml:lang=\"en\">src1&lt;&amp;\"\'></source>\n"
			+ "<target xml:lang=\"fr\">trg1</target>\n"
			+ "</trans-unit>\n"
			+ "</body>\n</file>\n</xliff>\n", result);

	}
	
	@Test
	public void testBasicWithITSProperties ()
		throws IOException
	{
		writer.create(root+"out.xlf", null, locEN, null, null, "original.ext", null);
		ITextUnit tu = new TextUnit("tu1", "text ");
		TextFragment tf = tu.getSource().getFirstContent();
		Code code = tf.append(TagType.PLACEHOLDER, "img", "[img]");
		GenericAnnotation.addAnnotation(tu, new GenericAnnotation(GenericAnnotationType.DOMAIN,
			GenericAnnotationType.DOMAIN_VALUE, "dom1, dom2"));
		GenericAnnotation.addAnnotation(tu, new GenericAnnotation(GenericAnnotationType.EXTERNALRES,
			GenericAnnotationType.EXTERNALRES_VALUE, "http://example.com/res"));
		GenericAnnotation.addAnnotation(code, new GenericAnnotation(GenericAnnotationType.EXTERNALRES,
				GenericAnnotationType.EXTERNALRES_VALUE, "http://example.com/res2"));
		writer.writeTextUnit(tu);
		writer.close();

		String result = readFile(root+"out.xlf");
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<xliff version=\"1.2\" xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" xmlns:okp=\"okapi-framework:xliff-extensions\"" +
			" xmlns:its=\"http://www.w3.org/2005/11/its\" xmlns:itsxlf=\"http://www.w3.org/ns/its-xliff/\" its:version=\"2.0\">\n"
			+ "<file original=\"original.ext\" source-language=\"en\" datatype=\"x-undefined\">\n"
			+ "<body>\n"
			+ "<trans-unit id=\"tu1\" itsxlf:domains=\"dom1, dom2\" itsxlf:externalResourceRef=\"http://example.com/res\">\n"
			+ "<source xml:lang=\"en\">text <x id=\"1\" itsxlf:externalResourceRef=\"http://example.com/res2\"/></source>\n"
			+ "</trans-unit>\n"
			+ "</body>\n</file>\n</xliff>\n", result);
	}

	@Test
	public void testAnnotatorsRef ()
		throws IOException
	{
		writer.create(root+"out.xlf", null, locEN, locFR, null, "original.ext", null);

		ITextUnit tu = new TextUnit("tu1", "source");
		TextContainer tc = tu.createTarget(LocaleId.FRENCH, false, IResource.CREATE_EMPTY);
		tc.setContent(new TextFragment("target"));
		GenericAnnotations anns = new GenericAnnotations(
			new GenericAnnotation(GenericAnnotationType.MTCONFIDENCE,
				GenericAnnotationType.MTCONFIDENCE_VALUE, 0.95));
		anns.add(new GenericAnnotation(GenericAnnotationType.ANNOT,
			GenericAnnotationType.ANNOT_VALUE, "mt-confidence|ABC"));
		GenericAnnotations.addAnnotations(tc, anns);
		
		AltTranslationsAnnotation alts = new AltTranslationsAnnotation();
		alts.add(LocaleId.ENGLISH, LocaleId.FRENCH, null, null,
			new TextFragment("alt target"), MatchType.FUZZY, 90, "origin", 90, 100);
		tc.setAnnotation(alts);
		anns = new GenericAnnotations(
			new GenericAnnotation(GenericAnnotationType.MTCONFIDENCE,
				GenericAnnotationType.MTCONFIDENCE_VALUE, 0.88));
		anns.add(new GenericAnnotation(GenericAnnotationType.ANNOT,
			GenericAnnotationType.ANNOT_VALUE, "mt-confidence|QWERTY"));
		GenericAnnotations.addAnnotations(alts.getFirst().getTarget(), anns);
		writer.writeTextUnit(tu);
		
		// Second TU
		tu = new TextUnit("tu2", "source2");
		tc = tu.createTarget(LocaleId.FRENCH, false, IResource.CREATE_EMPTY);
		tc.setContent(new TextFragment("target2"));
		tc.getSegments().create(0, 7);
		Segment seg = tc.getFirstSegment();
		anns = new GenericAnnotations(
			new GenericAnnotation(GenericAnnotationType.MTCONFIDENCE,
				GenericAnnotationType.MTCONFIDENCE_VALUE, 0.77));
		anns.add(new GenericAnnotation(GenericAnnotationType.ANNOT,
			GenericAnnotationType.ANNOT_VALUE, "mt-confidence|XYZ"));
		seg.setAnnotation(anns);

		// Segment-level alt-tran
		alts = new AltTranslationsAnnotation();
		alts.add(LocaleId.ENGLISH, LocaleId.FRENCH, null, null,
			new TextFragment("alt target2"), MatchType.FUZZY, 97, "origin", 90, 100);
		seg.setAnnotation(alts);
		anns = new GenericAnnotations(
			new GenericAnnotation(GenericAnnotationType.MTCONFIDENCE,
				GenericAnnotationType.MTCONFIDENCE_VALUE, 0.66));
		anns.add(new GenericAnnotation(GenericAnnotationType.ANNOT,
			GenericAnnotationType.ANNOT_VALUE, "mt-confidence|Extra"));
		GenericAnnotations.addAnnotations(alts.getFirst().getTarget(), anns);
		
		writer.writeTextUnit(tu);
		
		writer.close();
		
		String result = readFile(root+"out.xlf");
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<xliff version=\"1.2\" xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" xmlns:okp=\"okapi-framework:xliff-extensions\"" +
			" xmlns:its=\"http://www.w3.org/2005/11/its\" xmlns:itsxlf=\"http://www.w3.org/ns/its-xliff/\" its:version=\"2.0\">\n"
			+ "<file original=\"original.ext\" source-language=\"en\" target-language=\"fr\" datatype=\"x-undefined\">\n"
			+ "<body>\n"
			+ "<trans-unit id=\"tu1\">\n"
			+ "<source xml:lang=\"en\">source</source>\n"
			+ "<target xml:lang=\"fr\" its:annotatorsRef=\"mt-confidence|ABC\" its:mtConfidence=\"0.95\">target</target>\n"
			+ "<alt-trans match-quality=\"90\" origin=\"origin\" okp:matchType=\"FUZZY\">"
			+ "<target xml:lang=\"fr\" its:annotatorsRef=\"mt-confidence|QWERTY\" its:mtConfidence=\"0.88\">alt target</target>\n"
			+ "</alt-trans>\n"
			+ "</trans-unit>\n"
			+ "<trans-unit id=\"tu2\">\n"
			+ "<source xml:lang=\"en\">source2</source>\n"
			+ "<target xml:lang=\"fr\"><mrk mid=\"0\" mtype=\"seg\" its:annotatorsRef=\"mt-confidence|XYZ\" its:mtConfidence=\"0.77\">target2</mrk></target>\n"
			+ "<alt-trans mid=\"0\" match-quality=\"97\" origin=\"origin\" okp:matchType=\"FUZZY\">"
			+ "<target xml:lang=\"fr\" its:annotatorsRef=\"mt-confidence|Extra\" its:mtConfidence=\"0.66\">alt target2</target>\n"
			+ "</alt-trans>\n"
			+ "</trans-unit>\n"
			+ "</body>\n</file>\n</xliff>\n", result);
	}
	
	@Test
	public void testVariousITSAnnotations ()
		throws IOException
	{
		writer.create(root+"out.xlf", null, locEN, locFR, null, "original.ext", null);
		writer.setAnnotatorsRef("terminology|myTool");
		writer.writeStartFile(null, null, null);
		
		ITextUnit tu = new TextUnit("tu1");
		TextFragment tf = tu.getSource().getFirstSegment().getContent();
		tf.append("Text ");
		
		// Translate:
		// There is no pre-defined annotation for Translate because the filters usually convert the not-transltable content into code
		// But you can add an mrk element for this this way:
		// Add the starting code for the original starting tag
		Code code = tf.append(TagType.OPENING, "span", "<its:span translate='no'>");
		// Set a general annotation of type 'protected', this will get you an mrk with mtype='protected'
		code.setAnnotation("protected", new InlineAnnotation("protected"));
		// Add the text not to translate
		tf.append("DO-NOT-TRANSLATE");
		// Add the original closing tag 
		tf.append(TagType.CLOSING, "span", "</its:span>");
		
		// Terminology:
		// There is a pre-define annotation for Terminology: GenericAnnotationType.TERM 
		tf.append(" term");
		GenericAnnotations anns = new GenericAnnotations(
			new GenericAnnotation(GenericAnnotationType.TERM,
				GenericAnnotationType.TERM_INFO, "Definition of 'term'",
				GenericAnnotationType.TERM_CONFIDENCE, 0.98));
		// Annotate using offsets
		// Each inline code takes 2 chars. so:
		// Text ##DO-NOT-TRANSLATE## term
		// 0123456789012345678901234567890
		tf.annotate(26, 30, GenericAnnotationType.GENERIC, anns);
		
		// Localization Note
		tf.append(" etc.");
		anns = new GenericAnnotations(
			new GenericAnnotation(GenericAnnotationType.LOCNOTE,
				GenericAnnotationType.LOCNOTE_TYPE, "alert",
				GenericAnnotationType.LOCNOTE_VALUE, "Text of the localization note."));
		// Text ##DO-NOT-TRANSLATE## ##term## etc.
		// 0123456789012345678901234567890123456789
		tf.annotate(35, 39, GenericAnnotationType.GENERIC, anns);
		
		writer.writeTextUnit(tu);
		writer.writeEndFile();
		writer.close();		

		String result = readFile(root+"out.xlf");
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<xliff version=\"1.2\" xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" xmlns:okp=\"okapi-framework:xliff-extensions\"" +
			" xmlns:its=\"http://www.w3.org/2005/11/its\" xmlns:itsxlf=\"http://www.w3.org/ns/its-xliff/\" its:version=\"2.0\">\n"
			+ "<file original=\"unknown\" source-language=\"en\" target-language=\"fr\" datatype=\"x-undefined\" its:annotatorsRef=\"terminology|myTool\">\n"
			+ "<body>\n<trans-unit id=\"tu1\">\n"
			+ "<source xml:lang=\"en\">Text <g id=\"1\"><mrk mtype=\"protected\">DO-NOT-TRANSLATE</g> <mrk itsxlf:termConfidence=\"0.98\" itsxlf:termInfo=\"Definition of 'term'\"" +
			" mtype=\"term\">term</mrk> <mrk comment=\"Text of the localization note.\" itsxlf:locNoteType=\"alert\" mtype=\"x-its\">etc.</mrk></source>\n"
			+ "<target xml:lang=\"fr\">Text <g id=\"1\"><mrk mtype=\"protected\">DO-NOT-TRANSLATE</g> <mrk itsxlf:termConfidence=\"0.98\" itsxlf:termInfo=\"Definition of 'term'\"" +
			" mtype=\"term\">term</mrk> <mrk comment=\"Text of the localization note.\" itsxlf:locNoteType=\"alert\" mtype=\"x-its\">etc.</mrk></target>\n"
			+ "</trans-unit>\n"
			+ "</body>\n</file>\n</xliff>\n", result);
	}

	@Test
	public void testLocaleFilterWithoutTarget () throws IOException {
		testLocaleFilter(null);
	}
	
	@Test
	public void testLocaleFilterWithTarget () throws IOException {
		testLocaleFilter(locFR);
	}
	
	private void testLocaleFilter (LocaleId trgLocId)
		throws IOException
	{
		writer.create(root+"out.xlf", null, locEN, trgLocId, null, "original.ext", null);
		// TU to translate - for fr
		ITextUnit tu = new TextUnit("1", "text1");
		tu.setAnnotation(new GenericAnnotations(
			new GenericAnnotation(GenericAnnotationType.LOCFILTER,
				GenericAnnotationType.LOCFILTER_VALUE, "fr")) // Included
		);
		writer.writeTextUnit(tu);
		// TU to translate - not for fr
		tu = new TextUnit("2", "text2");
		tu.setAnnotation(new GenericAnnotations(
			new GenericAnnotation(GenericAnnotationType.LOCFILTER,
				GenericAnnotationType.LOCFILTER_VALUE, "!fr")) // Excluded
		);
		writer.writeTextUnit(tu);
		// TU not to translate - for fr
		tu = new TextUnit("3", "text3"); tu.setIsTranslatable(false);
		tu.setAnnotation(new GenericAnnotations(
			new GenericAnnotation(GenericAnnotationType.LOCFILTER,
				GenericAnnotationType.LOCFILTER_VALUE, "fr")) // Included
		);
		writer.writeTextUnit(tu);
		// TU not to translate - not for fr
		tu = new TextUnit("4", "text4"); tu.setIsTranslatable(false);
		tu.setAnnotation(new GenericAnnotations(
			new GenericAnnotation(GenericAnnotationType.LOCFILTER,
				GenericAnnotationType.LOCFILTER_VALUE, "!fr")) // Excluded
		);
		writer.writeTextUnit(tu);
		writer.writeEndFile();
		writer.close();
		
		String result = readFile(root+"out.xlf");
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<xliff version=\"1.2\" xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" xmlns:okp=\"okapi-framework:xliff-extensions\"" +
			" xmlns:its=\"http://www.w3.org/2005/11/its\" xmlns:itsxlf=\"http://www.w3.org/ns/its-xliff/\" its:version=\"2.0\">\n"
			+ "<file original=\"original.ext\" source-language=\"en\""
			+ ((trgLocId == null) ? "" : " target-language=\"fr\"")
			+ " datatype=\"x-undefined\">\n"
			+ "<body>\n"
			+ "<trans-unit id=\"1\" its:localeFilterList=\"fr\">\n"
			+ "<source xml:lang=\"en\">text1</source>\n"
			+ ((trgLocId == null) ? "" : "<target xml:lang=\"fr\">text1</target>\n")
			+ "</trans-unit>\n"
			+ "<trans-unit id=\"2\""
			+ ((trgLocId == null) ? "" : " translate=\"no\"")
			+ " its:localeFilterList=\"fr\" its:localeFilterType=\"exclude\">\n"
			+ "<source xml:lang=\"en\">text2</source>\n"
			+ ((trgLocId == null) ? "" : "<target xml:lang=\"fr\">text2</target>\n")
			+ "</trans-unit>\n"
			+ "<trans-unit id=\"3\" translate=\"no\" its:localeFilterList=\"fr\">\n"
			+ "<source xml:lang=\"en\">text3</source>\n"
			+ ((trgLocId == null) ? "" : "<target xml:lang=\"fr\">text3</target>\n")
			+ "</trans-unit>\n"
			+ "<trans-unit id=\"4\" translate=\"no\" its:localeFilterList=\"fr\" its:localeFilterType=\"exclude\">\n"
			+ "<source xml:lang=\"en\">text4</source>\n"
			+ ((trgLocId == null) ? "" : "<target xml:lang=\"fr\">text4</target>\n")
			+ "</trans-unit>\n"
			+ "</body>\n</file>\n</xliff>\n", result);
	}

	@Test
	public void testLocaleFilterInlineWithoutTarget () throws IOException {
		testLocaleFilterInline(null);
	}
	
	@Test
	public void testLocaleFilterInlineWithTarget () throws IOException {
		testLocaleFilterInline(locFR);
	}

	private void testLocaleFilterInline (LocaleId trgLocId)
		throws IOException
	{
		writer.create(root+"out.xlf", null, locEN, trgLocId, null, "original.ext", null);
		
		// TU to translate - for fr
		TextFragment tf = new TextFragment("t1 ");
		Code oc = tf.append(TagType.OPENING, "span", "[span]");
		tf.append("text");
		Code cc = tf.append(TagType.CLOSING, "span", "[/span]");
		ITextUnit tu = new TextUnit("1");
		tu.setSourceContent(tf);
		GenericAnnotations anns = new GenericAnnotations(
			new GenericAnnotation(GenericAnnotationType.LOCFILTER,
				GenericAnnotationType.LOCFILTER_VALUE, "fr")); // Included
		GenericAnnotations.addAnnotations(oc, anns);
		GenericAnnotations.addAnnotations(cc, anns);
		writer.writeTextUnit(tu);

		// TU to translate - for fr
		tf = new TextFragment("t2 ");
		oc = tf.append(TagType.OPENING, "span", "[span]");
		tf.append("text");
		cc = tf.append(TagType.CLOSING, "span", "[/span]");
		tu = new TextUnit("2");
		tu.setSourceContent(tf);
		anns = new GenericAnnotations(
			new GenericAnnotation(GenericAnnotationType.LOCFILTER,
				GenericAnnotationType.LOCFILTER_VALUE, "!fr")); // Excluded
		GenericAnnotations.addAnnotations(oc, anns);
		GenericAnnotations.addAnnotations(cc, anns);
		writer.writeTextUnit(tu);

		writer.writeEndFile();
		writer.close();
		
		String result = readFile(root+"out.xlf");
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<xliff version=\"1.2\" xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" xmlns:okp=\"okapi-framework:xliff-extensions\"" +
			" xmlns:its=\"http://www.w3.org/2005/11/its\" xmlns:itsxlf=\"http://www.w3.org/ns/its-xliff/\" its:version=\"2.0\">\n"
			+ "<file original=\"original.ext\" source-language=\"en\""
			+ ((trgLocId == null) ? "" : " target-language=\"fr\"")
			+ " datatype=\"x-undefined\">\n"
			+ "<body>\n"
			+ "<trans-unit id=\"1\">\n"
			+ "<source xml:lang=\"en\">t1 <g id=\"1\"><mrk its:localeFilterList=\"fr\" mtype=\""
			+ ((trgLocId == null) ? "x-its" : (trgLocId.equals("fr") ? "x-its-translate-yes" : "protected"))
			+ "\">text</mrk></g></source>\n"
			+ ((trgLocId == null) ? "" :
				("<target xml:lang=\"fr\">t1 <g id=\"1\"><mrk its:localeFilterList=\"fr\" mtype=\""
					+ (trgLocId.equals("fr") ? "x-its-translate-yes" : "protected"))
			+ "\">text</mrk></g></target>\n")
			+ "</trans-unit>\n"
			+ "<trans-unit id=\"2\">\n"
			+ "<source xml:lang=\"en\">t2 <g id=\"1\"><mrk its:localeFilterList=\"fr\" its:localeFilterType=\"exclude\" mtype=\""
			+ ((trgLocId == null) ? "x-its" : (trgLocId.equals("fr") ? "protected" : "x-its-translate-yes"))
			+ "\">text</mrk></g></source>\n"
			+ ((trgLocId == null) ? "" : "<target xml:lang=\"fr\">t2 <g id=\"1\"><mrk its:localeFilterList=\"fr\" its:localeFilterType=\"exclude\" mtype=\""
			+ ((trgLocId == null) ? "x-its" : (trgLocId.equals("fr") ? "protected" : "x-its-translate-yes"))
			+ "\">text</mrk></g></target>\n")
			+ "</trans-unit>\n"
			+ "</body>\n</file>\n</xliff>\n", result);
	}

	@Test
	public void testEmptyTarget() throws Exception {
		writer.getParameters().setUseSourceForTranslated(false);
		writer.getParameters().setCopySource(false);
		writer.create(root + "out.xlf", null, locEN, locFR, null, "original.ext", null);
		ITextUnit tu = new TextUnit("1");
		tu.setSourceContent(new TextFragment("hello"));
		tu.setTarget(locFR, new TextContainer());
		writer.writeTextUnit(tu);
		writer.writeEndFile();
		writer.close();

		String result = readFile(root+"out.xlf");
		final String EXPECTED = 
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<xliff version=\"1.2\" xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" " +
				"xmlns:okp=\"okapi-framework:xliff-extensions\" xmlns:its=\"http://www.w3.org/2005/11/its\" " +
				"xmlns:itsxlf=\"http://www.w3.org/ns/its-xliff/\" its:version=\"2.0\">\n" +
			"<file original=\"original.ext\" source-language=\"en\" target-language=\"fr\" " +
				"datatype=\"x-undefined\">\n" +
			"<body>\n" +
			"<trans-unit id=\"1\">\n" +
			"<source xml:lang=\"en\">hello</source>\n" +
			"<target xml:lang=\"fr\"></target>\n" +
			"</trans-unit>\n" +
			"</body>\n" +
			"</file>\n" +
			"</xliff>";
		assertXMLEqual(EXPECTED, result);
	}

	@Test
	public void testEmptyTargetWithCustomState() throws Exception {
		writer.getParameters().setUseSourceForTranslated(false);
		writer.getParameters().setCopySource(false);
		writer.create(root + "out.xlf", null, locEN, locFR, null, "original.ext", null);
		ITextUnit tu = new TextUnit("1");
		tu.setSourceContent(new TextFragment("hello"));
		TextContainer tcTarget = new TextContainer();
		tcTarget.setProperty(new Property(Property.STATE, "needs-review"));
		tu.setTarget(locFR, tcTarget);
		writer.writeTextUnit(tu);
		writer.writeEndFile();
		writer.close();

		String result = readFile(root+"out.xlf");
		final String EXPECTED = 
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<xliff version=\"1.2\" xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" " +
				"xmlns:okp=\"okapi-framework:xliff-extensions\" xmlns:its=\"http://www.w3.org/2005/11/its\" " +
				"xmlns:itsxlf=\"http://www.w3.org/ns/its-xliff/\" its:version=\"2.0\">\n" +
			"<file original=\"original.ext\" source-language=\"en\" target-language=\"fr\" " +
				"datatype=\"x-undefined\">\n" +
			"<body>\n" +
			"<trans-unit id=\"1\">\n" +
			"<source xml:lang=\"en\">hello</source>\n" +
			"<target xml:lang=\"fr\" state=\"needs-review\"></target>\n" +
			"</trans-unit>\n" +
			"</body>\n" +
			"</file>\n" +
			"</xliff>";
		assertXMLEqual(EXPECTED, result);
	}

	@Test
	public void testWriteHeightWidthSizeOnGroup ()
		throws IOException
	{
		writer.create(root+"out.xlf", null, locEN, locFR, null, "original.ext", null);
		StartGroup sg = new StartGroup();
		sg.setId("sg1");
		sg.setProperty(new Property(Property.MAX_WIDTH, "10"));
		sg.setProperty(new Property(Property.MAX_HEIGHT, "1"));
		sg.setProperty(new Property(Property.SIZE_UNIT, "char"));
		writer.writeStartGroup(sg);
		ITextUnit tu = new TextUnit("tu1", "The text");
		tu.setTarget(locFR, new TextContainer("trg1"));
		writer.writeTextUnit(tu);
		writer.writeEndGroup();
		writer.close();

		String result = readFile(root+"out.xlf");
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<xliff version=\"1.2\" xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" xmlns:okp=\"okapi-framework:xliff-extensions\"" +
			" xmlns:its=\"http://www.w3.org/2005/11/its\" xmlns:itsxlf=\"http://www.w3.org/ns/its-xliff/\" its:version=\"2.0\">\n"
			+ "<file original=\"original.ext\" source-language=\"en\" target-language=\"fr\" datatype=\"x-undefined\">\n"
			+ "<body>\n"
			+ "<group id=\"sg1\" maxwidth=\"10\" maxheight=\"1\" size-unit=\"char\">\n"
			+ "<trans-unit id=\"tu1\">\n"
			+ "<source xml:lang=\"en\">The text</source>\n"
			+ "<target xml:lang=\"fr\">trg1</target>\n"
			+ "</trans-unit>\n"
			+ "</group>\n"
			+ "</body>\n</file>\n</xliff>\n", result);
	}

	@Test
	public void testWriteHeightWidthSizeOnTU ()
		throws IOException
	{
		writer.create(root+"out.xlf", null, locEN, locFR, null, "original.ext", null);
		ITextUnit tu = new TextUnit("tu1", "The text");
		tu.setProperty(new Property(Property.MAX_WIDTH, "10"));
		tu.setProperty(new Property(Property.MAX_HEIGHT, "1"));
		tu.setProperty(new Property(Property.SIZE_UNIT, "char"));
		tu.setTarget(locFR, new TextContainer("trg1"));
		writer.writeTextUnit(tu);
		writer.close();

		String result = readFile(root+"out.xlf");
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<xliff version=\"1.2\" xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" xmlns:okp=\"okapi-framework:xliff-extensions\"" +
			" xmlns:its=\"http://www.w3.org/2005/11/its\" xmlns:itsxlf=\"http://www.w3.org/ns/its-xliff/\" its:version=\"2.0\">\n"
			+ "<file original=\"original.ext\" source-language=\"en\" target-language=\"fr\" datatype=\"x-undefined\">\n"
			+ "<body>\n"
			+ "<trans-unit id=\"tu1\" maxwidth=\"10\" maxheight=\"1\" size-unit=\"char\">\n"
			+ "<source xml:lang=\"en\">The text</source>\n"
			+ "<target xml:lang=\"fr\">trg1</target>\n"
			+ "</trans-unit>\n"
			+ "</body>\n</file>\n</xliff>\n", result);
	}

	@Test
	public void testCoordinatesOnTU()
		throws IOException
	{
		writer.create(root+"out.xlf", null, locEN, locFR, null, "original.ext", null);
		ITextUnit tu = new TextUnit("tu1", "The text");
		tu.setProperty(new Property(Property.COORDINATES, "1;2;3;4"));
		tu.setProperty(new Property(Property.MAX_WIDTH, "10"));
		// Omit size-unit to verify default behavior of 'pixel'
		tu.setTarget(locFR, new TextContainer("trg1"));
		writer.writeTextUnit(tu);
		writer.close();

		String result = readFile(root+"out.xlf");
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<xliff version=\"1.2\" xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" xmlns:okp=\"okapi-framework:xliff-extensions\"" +
			" xmlns:its=\"http://www.w3.org/2005/11/its\" xmlns:itsxlf=\"http://www.w3.org/ns/its-xliff/\" its:version=\"2.0\">\n"
			+ "<file original=\"original.ext\" source-language=\"en\" target-language=\"fr\" datatype=\"x-undefined\">\n"
			+ "<body>\n"
			+ "<trans-unit id=\"tu1\" maxwidth=\"10\" size-unit=\"pixel\" coord=\"1;2;3;4\">\n"
			+ "<source xml:lang=\"en\">The text</source>\n"
			+ "<target xml:lang=\"fr\">trg1</target>\n"
			+ "</trans-unit>\n"
			+ "</body>\n</file>\n</xliff>\n", result);
	}

	private String readFile (String path)
		throws IOException
	{
		BufferedInputStream bis = null;
		try {
			byte[] buffer = new byte[1024];
			int count = 0;
			bis = new BufferedInputStream(new FileInputStream(path));
			StringBuilder sb = new StringBuilder();
			while ((count = bis.read(buffer)) != -1) {
				sb.append(new String(buffer, 0, count));
			}
			String tmp = sb.toString().replace("\r\n", "\n");
			tmp = tmp.replace("\r", "\n");
			return tmp;
		}
		finally {
			if ( bis != null ) bis.close();
		}
	}

	private String stripVariableID (String text) {
		text = text.replaceAll("locQualityIssuesRef=\"#(.*?)\"", "locQualityIssuesRef=\"#VARID\""); 
		text = text.replaceAll("xml:id=\"(.*?)\"", "xml:id=\"VARID\""); 
		return text;
	}
}
