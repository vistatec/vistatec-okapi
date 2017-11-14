/*===========================================================================
  Copyright (C) 2009 by the Okapi Framework contributors
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

package net.sf.okapi.common;

import static java.lang.System.lineSeparator;
import static net.sf.okapi.common.TestUtil.getFileAsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.EmptyStackException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class XMLWriterTest {

    XMLWriter writer;
    StringWriter sWriter;

    @Before
    public void setUp(){
        sWriter = new StringWriter();
        writer = new XMLWriter(sWriter);
    }

    //TODO: possibly move out into integration test since it touches the file system. For now it is still fast.
    @Test
    public void constructorWithPath() throws IOException {
        final String filename = FileLocation.fromClass(XMLWriterTest.class).out("/some/dir/to/be/created/some.xml").toString();
        File f = new File(filename);
        f.delete();
        writer = new XMLWriter(filename);
        assertTrue("A file should have been created along with the directory structure", f.exists());
        writer.writeStartDocument();
        writer.close();
        String xml = getFileAsString(f);
        assertEquals("writer's contents", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>", xml.trim());
        f.delete();
        //This doesn't pass on windows (file deletion is always a nightmare since win7)
        //assertFalse("Could not delete file " + f.getPath(), f.exists());
    }

    @Test
    public void constructorWithWriter(){
        assertEquals("writer's contents", "", sWriter.toString());
    }

    @Test
    public void writeStartDocument(){
        writer.writeStartDocument();
        writer.flush();
        assertEquals("writer's contents", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>", sWriter.toString().trim());
    }

    @Test
    public void writeEndDocumentNoStartTag(){
        writer.writeStartDocument();
        writer.writeEndDocument();
        assertEquals("writer's contents", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>", sWriter.toString().trim());
    }

    @Test
    public void writeStartElement(){
        writer.writeStartDocument();
        writer.writeStartElement("joe");
        writer.flush();
        String xml = sWriter.toString().trim();
        xml = xml.replaceAll("\n","");
        xml = xml.replaceAll("\r","");
        assertEquals("writer's contents", "<?xml version=\"1.0\" encoding=\"UTF-8\"?><joe", xml);
    }

    @Test
    public void writeStartElementWithPreviousStartElement(){
        writer.writeStartDocument();
        writer.writeStartElement("jack");
        writer.writeStartElement("diane");
        writer.flush();
        String xml = sWriter.toString().trim();
        xml = xml.replaceAll("\n","");
        xml = xml.replaceAll("\r","");
        assertEquals("writer's contents", "<?xml version=\"1.0\" encoding=\"UTF-8\"?><jack><diane", xml);
    }

    @Test
    public void writeEndElement(){
        writer.writeStartDocument();
        writer.writeStartElement("jack");
        writer.writeEndElement();
        writer.flush();
        String xml = sWriter.toString().trim();
        xml = xml.replaceAll("\n","");
        xml = xml.replaceAll("\r","");
        assertEquals("writer's contents", "<?xml version=\"1.0\" encoding=\"UTF-8\"?><jack></jack>", xml);
    }

    // Calling writeEndElement without a writeStartElement is not logical, and therefore not allowed 
    @Test(expected = EmptyStackException.class)
    public void writeEndElementNoStartElement(){
        writer.writeStartDocument();
        writer.writeEndElement();
        String xml = sWriter.toString().trim();
        xml = xml.replaceAll("\n","");
        xml = xml.replaceAll("\r","");
        assertEquals("writer's contents", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>", xml);
    }
    @Test
    public void writeEndElementLineBreakStartElement(){
        writer.writeStartDocument();
        writer.writeStartElement("mary");
        writer.writeEndElementLineBreak();
        writer.flush();
        String xml = sWriter.toString();
        Pattern p = Pattern.compile("<\\?xml version=\"1.0\" encoding=\"UTF-8\"\\?>[\\n\\r]+<mary></mary>[\\r\\n]+", Pattern.MULTILINE);
        Matcher m = p.matcher(xml);
        assertTrue("New lines were not written", m.matches());
    }

    // Calling writeEndElementLineBreak without a writeStartElement is not logical, and therefore not allowed 
    @Test(expected = EmptyStackException.class)
    public void writeEndElementLineBreakNoStartElement(){
        writer.writeStartDocument();
        writer.writeEndElementLineBreak();
        String xml = sWriter.toString().trim();
        xml = xml.replaceAll("\n","");
        xml = xml.replaceAll("\r","");
        assertEquals("writer's contents", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>", xml);
    }
    
    @Test
    public void writeLeadingSpaces() {
    	writer.writeStartElement("w:t");
		writer.writeAttributeString("xml:space", "preserve");
		writer.writeString(" (");
		writer.writeEndElement(); // w:t
		writer.flush();
		assertEquals("<w:t xml:space=\"preserve\"> (</w:t>", sWriter.toString());
    }

    @Test
    public void appendRawXML() throws Exception {
        String expectedString = "Unix" + lineSeparator() + " & Legacy Mac" + lineSeparator() + " & Windows" + lineSeparator();

        writer.appendRawXML("Unix\n & Legacy Mac\r & Windows\r\n");
        writer.flush();

        Assert.assertThat(sWriter.toString(), equalTo(expectedString));
    }
}
