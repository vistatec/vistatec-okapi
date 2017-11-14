/*===========================================================================
Copyright (C) 2009-2011 by the Okapi Framework contributors
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

import static net.sf.okapi.common.TestUtil.getFileAsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import net.sf.okapi.common.FileLocation;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.XMLWriter;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @author HaslamJD
 */
@RunWith(JUnit4.class)
public class TMXWriterTest {
    final static File TMX_File = FileLocation.fromClass(TMXWriterTest.class)
            .out("tmxwritertest_tmxfile.tmx").asFile();

    TMXWriter tmxWriter;
    StringWriter strWriter;
	private LocaleId locEN = LocaleId.fromString("en");
	private LocaleId locFR = LocaleId.fromString("fr");
	private LocaleId locKR = LocaleId.fromString("kr");

    @Before
    public void setUp() {
    	strWriter = new StringWriter();
    	XMLWriter xmlWriter = new XMLWriter(strWriter);
    	tmxWriter = new TMXWriter(xmlWriter);
    	createTmxHeader();
    }

    @Test
    public void constructorStringPath() {
    	TMX_File.delete();
    	tmxWriter = new TMXWriter(TMX_File.getPath());
    	assertTrue("tmx file should have been created", TMX_File.exists());
    }

    @Test
    public void writeStartDocumentWithFile() throws IOException {
    	TMX_File.delete();
    	tmxWriter = new TMXWriter(TMX_File.getPath());
    	createTmxHeader();
    	tmxWriter.close();
    	String tmx = getFileAsString(TMX_File);
    	testHeader(tmx);
    }

    @Test
    public void constructorWithXmlWriter() {
    	tmxWriter.close();
    	String tmx = strWriter.toString();
    	testHeader(tmx);
    }

    @Test
    public void emptyDocument() {
    	tmxWriter.writeEndDocument();
    	String tmx = stripNewLinesAndReturns(strWriter.toString());
    	assertEquals("Header and Footer Only", expectedHeaderTmx + expectedFooterTmx, tmx);
    }

    @Test
    public void testTmxTuNoAttributes() {
    	ITextUnit tu = createTextUnit("id", "SourceContent", "TargetContent", null);
    	tmxWriter.writeTUFull(tu);
    	testTu(tu, strWriter.toString());
    }

    @Test
    public void testTmxTuNoAttributesWithSrcLangAll() {
    	ITextUnit tu = createTextUnit("id", "SourceContent", "TargetContent", null);
    	LocaleId srcLocAll = LocaleId.ALL;
    	tmxWriter.writeTUFull(tu, srcLocAll);
    	
    	String tmx = strWriter.toString();
    	tmx = stripNewLinesAndReturns(tmx);
    	String properties = getProps(tu);
    	String targetTuvs = getTargetTuvs(tu);
    	String expectedTMX = 
    		"<tu tuid=\"" + tu.getName() + "\">" +
    		properties +
    		"<tuv xml:lang=\"*all*\">" +
    		"<seg>" + tu.getSource().getSegments().getFirstContent().toText() + "</seg>" +
    		"</tuv>" +
    		targetTuvs +
    		"</tu>";
    	assertEquals("TU Element", expectedHeaderTmx + expectedTMX, tmx);
    }

    @Test
    public void tmxWithTargetAttributes() {
    	ITextUnit tu = createTextUnitWithTransAttributes("id", "SourceContent", "TargetContent", 
    			new String[][]{{"creationdate", "20120822T110210Z"}, {"changeid", "0"}, {"changedate", "20130501T065729Z"}});
    	LocaleId srcLocAll = LocaleId.ALL;
    	
    	TextContainer tc = tu.getTarget(locFR);
    	tc.setProperty(new Property("myprop", "myvalue"));
    	
    	XMLWriter xmlWriter = new XMLWriter(strWriter);
    	TMXWriter w = new TMXWriter(xmlWriter);
    	w.setWriteAllPropertiesAsAttributes(true);
    	w.writeTUFull(tu, srcLocAll);
    	
    	String tmx = strWriter.toString();
    	tmx = stripNewLinesAndReturns(tmx);
    	String targetTuvs = getTargetTuvsWithAttributes(tu);
    	String expectedTMX = 
    		"<tu tuid=\"" + tu.getName() + "\">" +
    		"<tuv xml:lang=\"*all*\">" +
    		"<seg>" + tu.getSource().getSegments().getFirstContent().toText() + "</seg>" +
    		"</tuv>" +
    		targetTuvs +
    		"</tu>";
    	assertEquals("TU Element", expectedTMX, tmx);
    }
    
    @Test
    public void tmxWithTuvTargetAttributes() {
    	ITextUnit tu = createTextUnitWithTransAttributes("id", "SourceContent", "TargetContent", 
    			new String[][]{{"creationdate", "20120822T110210Z"}, {"changeid", "0"}, {"changedate", "20130501T065729Z"}});
    	
    	TextContainer tc = tu.getTarget(locFR);
    	tc.setProperty(new Property("myprop", "myvalue"));
    	
    	XMLWriter xmlWriter = new XMLWriter(strWriter);
    	TMXWriter w = new TMXWriter(xmlWriter);
    	w.setWriteAllPropertiesAsAttributes(false);
    	w.writeTUFull(tu, locEN);
    	
    	String tmx = strWriter.toString();
    	tmx = stripNewLinesAndReturns(tmx);
    	String expectedTMX = 
    		"<tu tuid=\"" + tu.getName() + "\">" +
    		"<tuv xml:lang=\"en\">" +
    		"<seg>" + tu.getSource().getSegments().getFirstContent().toText() + "</seg>" +
    		"</tuv>" +
    		"<tuv xml:lang=\"fr\">" +
    		"<prop type=\"myprop\">myvalue</prop>" +
    		"<seg>TargetContent</seg></tuv>" +
    		"</tu>";
    	assertEquals("TU Element", expectedTMX, tmx);
    }
    
    @Test
    public void testTmxTuSingleProp() {
    	ITextUnit tu = createTextUnit("id", "SourceContent", "TargetContent", new String[][]{{"prop1", "value1"}});
    	tmxWriter.writeTUFull(tu);
    	testTu(tu, strWriter.toString());
    }

    @Test
    public void testTmxTuMultiProp() {
    	ITextUnit tu = createTextUnit("id", "SourceContent", "TargetContent", new String[][]{{"prop1", "value1"}, {"prop2", "value2"}});
    	tmxWriter.writeTUFull(tu);
    	testTu(tu, strWriter.toString());
    }

    @Test
    public void testTmxTuMultiPropExpand() {
    	ITextUnit tu = createTextUnit("id", "SourceContent", "TargetContent", new String[][]{{"prop1", "value1, value2"}});
    	tmxWriter.setExpandDuplicateProps(true);
    	tmxWriter.writeTUFull(tu);
    	tmxWriter.setExpandDuplicateProps(false);
    	String targetTuvs = getTargetTuvs(tu);
    	String expectedTMX = 
        		"<tu tuid=\"" + tu.getName() + "\">" +
        		"<prop type=\"prop1\">value1</prop>" +
        		"<prop type=\"prop1\">value2</prop>" +
        		"<tuv xml:lang=\"en\">" +
        		"<seg>" + tu.getSource().getSegments().getFirstContent().toText() + "</seg>" +
        		"</tuv>" +
        		targetTuvs +
        		"</tu>";
        assertEquals("TU Element with Dup Props", expectedHeaderTmx + expectedTMX, stripNewLinesAndReturns(strWriter.toString()));
    }
    
    @Test
    public void testTmxTuMultiLang() {
    	ITextUnit tu = createTextUnit("id", "SourceContent", "TargetContent", new String[][]{{"prop1", "value1"}, {"prop2", "value2"}});
    	tu.setTargetContent(locKR, new TextFragment("KoreanTarget"));
    	tmxWriter.writeTUFull(tu);
    	testTu(tu, strWriter.toString());
    }
    
    @Test
    public void tmxWithHeaderPropAndNote() {        
        XMLWriter xmlWriter = new XMLWriter(strWriter);
        TMXWriter w = new TMXWriter(xmlWriter);
        w.setWriteAllPropertiesAsAttributes(false);
        StartDocument sd = new StartDocument("1");
        sd.setProperty(new Property("note", "nutbar", true));
        sd.setProperty(new Property("x-foo", "foobar", true));
        w.writeStartDocument(sd, LocaleId.ENGLISH, LocaleId.SPANISH, null, null, "sentence", "plaintext", null);
        w.close();
        String tmx = strWriter.toString();
        tmx = stripNewLinesAndReturns(tmx);
        String expectedTMX = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<tmx version=\"1.4\">"
                + "<header creationtool=\"unknown\" creationtoolversion=\"unknown\" segtype=\"sentence\" o-tmf=\"plaintext\" adminlang=\"en\" srclang=\"en\" datatype=\"unknown\">"
                        + "<note>nutbar</note>"
                        + "<prop type=\"x-foo\">foobar</prop>"
                + "</header>"
                + "<body>";
        assertEquals("Header prop and notes", expectedTMX, tmx);
    }

    private void testHeader(String tmx) {
    	tmx = stripNewLinesAndReturns(tmx);
    	assertEquals("TMX Header", expectedHeaderTmx, tmx);
    }

    private final static String expectedHeaderTmx = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><tmx version=\"1.4\"><header creationtool=\"pensieve\" creationtoolversion=\"0.0.1\" segtype=\"sentence\" o-tmf=\"pensieve_format\" adminlang=\"en\" srclang=\"en\" datatype=\"unknown\"></header><body>";
    private final static String expectedFooterTmx = "</body></tmx>";

    private void testTu(ITextUnit tu, String tmx) {
    	tmx = stripNewLinesAndReturns(tmx);
    	String properties = getProps(tu);
    	String targetTuvs = getTargetTuvs(tu);

    	String expectedTMX = 
    		"<tu tuid=\"" + tu.getName() + "\">" +
    		properties +
    		"<tuv xml:lang=\"en\">" +
    		"<seg>" + tu.getSource().getSegments().getFirstContent().toText() + "</seg>" +
    		"</tuv>" +
    		targetTuvs +
    		"</tu>";
    	assertEquals("TU Element", expectedHeaderTmx + expectedTMX, tmx);
    }

    private String getProps(ITextUnit tu) {
    	String properties = "";
    	for (String propName : tu.getPropertyNames()) {
    		properties += "<prop type=\"" + propName + "\">" + tu.getProperty(propName) + "</prop>";
    	}
    	return properties;
    }

    private String getTargetTuvs(ITextUnit tu) {
    	String targetTuvs = "";
    	for (LocaleId langName : tu.getTargetLocales()) {
    		targetTuvs += "<tuv xml:lang=\"" + langName + "\">" + "<seg>" + 
    			tu.getTargetSegments(langName).getFirstContent().toText() + "</seg>" + "</tuv>";
    	}
    	return targetTuvs;
    }

    private String getTargetTuvsWithAttributes(ITextUnit tu) {
    	String targetTuvs = "";
    	for (LocaleId langName : tu.getTargetLocales()) {
    		targetTuvs += "<tuv xml:lang=\"" + langName + "\"" + 
    			" creationdate=\"20120822T110210Z\" changeid=\"0\" changedate=\"20130501T065729Z\">"
    			+ "<seg>" + 
    			tu.getTargetSegments(langName).getFirstContent().toText() + "</seg>" + "</tuv>";
    	}
    	return targetTuvs;
    }
    
    private ITextUnit createTextUnit(String id, String sourceContent, String targetContent, String[][] attributes) {
    	ITextUnit tu = new TextUnit(id);
    	tu.setName(id);
    	tu.setSourceContent(new TextFragment(sourceContent));
    	tu.setTargetContent(locFR, new TextFragment(targetContent));
    	if (attributes != null) {
    		for (String[] kvp : attributes) {
    			tu.setProperty(new Property(kvp[0], kvp[1]));
    		}
    	}
    	return tu;
    }
    
    private ITextUnit createTextUnitWithTransAttributes(String id, String sourceContent, 
    		String targetContent, String[][] attributes) {
    	ITextUnit tu = new TextUnit(id);
    	tu.setName(id);
    	tu.setSourceContent(new TextFragment(sourceContent));
    	tu.setTargetContent(locFR, new TextFragment(targetContent));
    	TextContainer t = tu.getTarget(locFR);
    	if (attributes != null) {
    		for (String[] kvp : attributes) {
    			t.setProperty(new Property(kvp[0], kvp[1]));
    		}
    	}
    	return tu;
    }
    
    private void createTmxHeader() {
    	tmxWriter.writeStartDocument(locEN, locFR, "pensieve", "0.0.1", "sentence", "pensieve_format", "unknown");
    }

    private String stripNewLinesAndReturns(String tmx) {
    	return tmx.replaceAll("[\\n\\r]+", "");
    }
}
