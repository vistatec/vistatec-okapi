package net.sf.okapi.filters.xml;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.FileLocation;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.StreamUtil;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.RawDocument;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @author ccudennec
 * @since 09.02.2017
 */
@RunWith(JUnit4.class)
public class XMLFilterEncodingTest {

    private XMLFilter filter;
    private LocaleId locEN = LocaleId.fromString("en");

    @Before
    public void setUp() {
        filter = new XMLFilter();
    }

    @Test
    public void utf8ToUtf16le() throws Exception {
        String input = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<doc>hello</doc>";
        String expected = "\ufeff<?xml version=\"1.0\" encoding=\"UTF-16\"?>\n"
                + "<doc>hello</doc>";
        testEncoding(input, expected, "UTF-8", "UTF-16LE");
    }

    @Test
    public void utf16WithBom() throws Exception {
        String input = "\ufeff<?xml version=\"1.0\" encoding=\"UTF-16\"?>\n"
                + "<doc>hello</doc>";
        String expected = input;
        testEncoding(input, expected, "UTF-16LE", "UTF-16LE");
    }

    @Test
    public void utf16WithoutBom() throws Exception {
        String input = "<?xml version=\"1.0\" encoding=\"UTF-16LE\"?>\n"
                + "<doc>hello</doc>";
        String expected = "\ufeff<?xml version=\"1.0\" encoding=\"UTF-16\"?>\n"
                + "<doc>hello</doc>";
        testEncoding(input, expected, "UTF-16LE", "UTF-16LE");
    }

    @Test
    public void utf16leWithBomFromFile() throws Exception {
    	FileLocation root = FileLocation.fromClass(getClass());
        String source = "/test10_utf16le-with-bom.xml";
        RawDocument rawDocument = new RawDocument(root.in(source).asInputStream(), null,
                LocaleId.GERMAN);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try ( XMLFilter xmlFilter = new XMLFilter() ) {
	        IFilterWriter writer = xmlFilter.createFilterWriter();
	        writer.setOutput(outputStream);
	        xmlFilter.open(rawDocument);
	        while (xmlFilter.hasNext()) {
	            Event event = xmlFilter.next();
	            writer.handleEvent(event);
	        }
        }
        byte[] actual = outputStream.toByteArray();
        byte[] expected = StreamUtil.inputStreamToBytes(root.in(source).asInputStream());

        String actualContent = new String(actual, "UTF-16LE");
        String expectedContent = new String(expected, "UTF-16LE");

        // compare BOM
        assertArrayEquals(Arrays.copyOf(expected, 2), Arrays.copyOf(actual, 2));
        // compare XML header and content
        assertEquals(expectedContent, actualContent);
    }

    private void testEncoding (String input,
    	String expected, String inputEncoding,
    	String outputEncoding)
    	throws IOException
    {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes(inputEncoding));

        RawDocument rd = new RawDocument(inputStream, inputEncoding, locEN);
        rd.setEncoding(inputEncoding);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        IFilterWriter filterWriter = filter.createFilterWriter();
        filterWriter.setOutput(outputStream);
        filterWriter.setOptions(LocaleId.fromString("de"), outputEncoding);

        filter.open(rd);
        while (filter.hasNext()) {
            Event event = filter.next();
            filterWriter.handleEvent(event);
        }

        String actual = new String(outputStream.toByteArray(), outputEncoding);
        assertEquals(expected, actual);
    }
}
