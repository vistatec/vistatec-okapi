package net.sf.okapi.common;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class XmlInputStreamReaderTest {

    String charset = "UTF-8";
    private FileLocation location;

    @Before
    public void setUp() {
        location = FileLocation.fromClass(XmlInputStreamReaderTest.class);
    }

    @Test
    public void validXmlWithOffsets() throws IOException {
        Reader xmlReader = new XmlInputStreamReader(getStreamFromFile("simpleFile.xml"), charset);
        Reader simpleReader = new InputStreamReader(getStreamFromFile("simpleFile.xml"), charset);
        char[] cbuf = new char[1000];
        char[] cbuf1 = new char[1000];
        int ptr = 0;
        int ptr1 = 0;
        while(-1 != (ptr += xmlReader.read(cbuf, ptr, 5)));
        while(-1 != (ptr1 += simpleReader.read(cbuf1, ptr1, 5)));
        xmlReader.close();
        simpleReader.close();
        assertArrayEquals(cbuf1, cbuf);

        String doubleCheck = "<?xml version='1.0' encoding='UTF-8'?><body></body>";
        for(int i = 0; i < doubleCheck.length(); i++)
            assertEquals(doubleCheck.charAt(i), cbuf[i]);
    }

    @Test
    public void invalidXml() throws IOException {
        Reader xmlReader = new XmlInputStreamReader(getStreamFromFile("invalid_xml_entity.xlf"), charset);
        Reader simpleReader = new InputStreamReader(getStreamFromFile("invalid_xml_entity_corrected.xlf"), charset);
        char[] cbuf = new char[1000];
        char[] cbuf1 = new char[1000];
        xmlReader.read(cbuf, 0, 1000);
        simpleReader.read(cbuf1, 0, 1000);
        xmlReader.close();
        simpleReader.close();
        String expected = new String(cbuf1);
        String actual = new String(cbuf);
        assertEquals(expected, actual);
    }

    // This test reads 10 bytes at a time to make sure we "land" in the middle
    // of the entity boundary
    @Test
    public void invalidXmlWithOffsetsAndPartialEntity() throws IOException {
        Reader xmlReader = new XmlInputStreamReader(getStreamFromString("12345&#x03;234&#x1F;1234567890"), charset);
        Reader simpleReader = new InputStreamReader(getStreamFromString("123452341234567890"), charset);
        char[] cbuf = new char[1000];
        char[] cbuf1 = new char[1000];
        int ptr = 0;
        int ptr1 = 0;
        while(-1 != (ptr += xmlReader.read(cbuf, ptr, 10)));
        while(-1 != (ptr1 += simpleReader.read(cbuf1, ptr1, 10)));
        xmlReader.close();
        simpleReader.close();
        assertArrayEquals(cbuf1, cbuf);
    }

    @Test
    public void invalidXmlPartialEntity() throws IOException {
        assertEquals("&#x03 some text ", readXmlSnippet("&#x03 some text &#x1F;"));
    }

    @Test
    public void validXmlPartialEntity() throws IOException {
        assertEquals(" some text &#xA; some text &#xD some text ",
                readXmlSnippet("&#x03; some text &#xA; some text &#xD some text "));
    }

    @Test
    public void invalidXmlEntityEndOfString() throws IOException {
        assertEquals("some text ", readXmlSnippet("some text &#03;"));
    }

    @Test
    public void validXmlPartialEntityEndOfString() throws IOException {
        assertEquals("&aaa; some text &#10; some text &#13", readXmlSnippet("&aaa; some text &#10; some text &#13"));
    }

    @Test
    public void partialDecimalEntity() throws IOException {
        assertEquals("&#100 hello", readXmlSnippet("&#100 hello"));
    }

    @Test
    public void validDecimalEntity() throws IOException {
        assertEquals("&#12345;", readXmlSnippet("&#12345;"));
    }

    private String readXmlSnippet(String raw) throws IOException {
        Reader xmlReader = new XmlInputStreamReader(getStreamFromString(raw), charset);
        char[] buf = new char[1000];
        int len = xmlReader.read(buf, 0, buf.length);
        xmlReader.close();
        return new String(buf, 0, len);
    }

    @Test
    public void resolveEntity() throws IOException {
        XmlInputStreamReader.Entity entity = XmlInputStreamReader.resolveEntity("aaa".toCharArray(), 4, 3);
        assertNull(entity);

        entity = XmlInputStreamReader.resolveEntity("#abc;".toCharArray(), 0, 10);
        assertNull(entity);

        entity = XmlInputStreamReader.resolveEntity("amp;".toCharArray(), 0, 10);
        assertTrue(!entity.invalid);
        assertEquals(4, entity.size);
        assertEquals('&', entity.value);
        entity = XmlInputStreamReader.resolveEntity("ama;".toCharArray(), 0, 10);
        assertNull(entity);
        entity = XmlInputStreamReader.resolveEntity("ampa;".toCharArray(), 0, 10);
        assertNull(entity);

        entity = XmlInputStreamReader.resolveEntity("apos;".toCharArray(), 0, 10);
        assertTrue(!entity.invalid);
        assertEquals(5, entity.size);
        assertEquals('\'', entity.value);
        entity = XmlInputStreamReader.resolveEntity("apot;".toCharArray(), 0, 10);
        assertNull(entity);
        entity = XmlInputStreamReader.resolveEntity("aros;".toCharArray(), 0, 10);
        assertNull(entity);
        entity = XmlInputStreamReader.resolveEntity("apat;".toCharArray(), 0, 10);
        assertNull(entity);
        entity = XmlInputStreamReader.resolveEntity("aposa;".toCharArray(), 0, 10);
        assertNull(entity);

        entity = XmlInputStreamReader.resolveEntity("gt;".toCharArray(), 0, 10);
        assertTrue(!entity.invalid);
        assertEquals(3, entity.size);
        assertEquals('>', entity.value);
        entity = XmlInputStreamReader.resolveEntity("gta;".toCharArray(), 0, 10);
        assertNull(entity);
        entity = XmlInputStreamReader.resolveEntity("gl;".toCharArray(), 0, 10);
        assertNull(entity);

        entity = XmlInputStreamReader.resolveEntity("lt;".toCharArray(), 0, 10);
        assertTrue(!entity.invalid);
        assertEquals(3, entity.size);
        assertEquals('<', entity.value);
        entity = XmlInputStreamReader.resolveEntity("la;".toCharArray(), 0, 10);
        assertNull(entity);
        entity = XmlInputStreamReader.resolveEntity("ltg;".toCharArray(), 0, 10);
        assertNull(entity);

        entity = XmlInputStreamReader.resolveEntity("quot;".toCharArray(), 0, 10);
        assertTrue(!entity.invalid);
        assertEquals(5, entity.size);
        assertEquals('"', entity.value);
        entity = XmlInputStreamReader.resolveEntity("quote;".toCharArray(), 0, 10);
        assertNull(entity);
        entity = XmlInputStreamReader.resolveEntity("euot;".toCharArray(), 0, 10);
        assertNull(entity);
        entity = XmlInputStreamReader.resolveEntity("qaot;".toCharArray(), 0, 10);
        assertNull(entity);
        entity = XmlInputStreamReader.resolveEntity("quat;".toCharArray(), 0, 10);
        assertNull(entity);
        entity = XmlInputStreamReader.resolveEntity("quoa;".toCharArray(), 0, 10);
        assertNull(entity);

        entity = XmlInputStreamReader.resolveEntity("#10;".toCharArray(), 0, 10);
        assertTrue(!entity.invalid);
        assertEquals(4, entity.size);
        assertEquals(10, entity.value);

        entity = XmlInputStreamReader.resolveEntity("#10".toCharArray(), 0, 3);
        assertNull(entity);
        entity = XmlInputStreamReader.resolveEntity("#x10".toCharArray(), 0, 4);
        assertNull(entity);

        entity = XmlInputStreamReader.resolveEntity("#xabcd;".toCharArray(), 0, 10);
        assertTrue(!entity.invalid);
        assertEquals("#xabcd;".length(), entity.size);
        assertEquals(((10*16+11)*16+12)*16+13, entity.value);

        entity = XmlInputStreamReader.resolveEntity("#1234567890ABCDEF;".toCharArray(), 0, 20);
        assertTrue(entity.invalid);
        assertEquals("#1234567890ABCDEF;".length(), entity.size);

        entity = XmlInputStreamReader.resolveEntity("#x1234567890;".toCharArray(), 0, 20);
        assertTrue(entity.invalid);
        assertEquals("#x1234567890;".length(), entity.size);

        entity = XmlInputStreamReader.resolveEntity("#x100000".toCharArray(), 0, "#x100000".length());
        assertNull(entity);
        entity = XmlInputStreamReader.resolveEntity("#100000".toCharArray(), 0, "#100000".length());
        assertNull(entity);
    }

    InputStream getStreamFromFile(String fileName) {
        InputStream stream = null;
        try {
            stream = new FileInputStream(location.in("/xml/" + fileName).toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return stream;
    }

    InputStream getStreamFromString(String str) {
        InputStream stream = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8)); 
        return stream;
    }
}
