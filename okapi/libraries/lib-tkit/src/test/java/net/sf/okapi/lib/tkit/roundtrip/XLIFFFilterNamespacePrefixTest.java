package net.sf.okapi.lib.tkit.roundtrip;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filters.FilterTestDriver;
import net.sf.okapi.common.filterwriter.XLIFFWriter;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.filters.xliff.XLIFFFilter;
import net.sf.okapi.lib.tkit.merge.SkeletonMergerWriter;
import org.apache.commons.io.output.WriterOutputStream;
import org.junit.Before;
import org.junit.Test;

/**
 * @author jsiebahn
 * @since 07.03.2017
 */
public class XLIFFFilterNamespacePrefixTest {

    private XLIFFFilter filter;
    private LocaleId locEN = LocaleId.fromString("en");
    private LocaleId locFR = LocaleId.fromString("fr");

    @Before
    public void setUp () {
        filter = new XLIFFFilter();
    }

    @Test
    public void shouldKeepNamespacePrefixOfXTag() throws Exception {
        ensureResultContainsNamespacePrefixesAsInput("Foo<xlf:x id=\"1\"/>Bar");
    }

    @Test
    public void shouldKeepNamespacePrefixOfGTag() throws Exception {
        ensureResultContainsNamespacePrefixesAsInput("<xlf:g id=\"1\">Foo Bar</xlf:g>");
    }

    @Test
    public void shouldKeepNamespacePrefixOfBxExTags() throws Exception {
        ensureResultContainsNamespacePrefixesAsInput("<xlf:bx id=\"1\"/>Foo Bar<xlf:ex id=\"1\"/>");
    }

    @Test
    public void shouldKeepNamespacePrefixOfBptEptTags() throws Exception {
        ensureResultContainsNamespacePrefixesAsInput("<xlf:bpt id=\"1\">bpt-data</xlf:bpt>Foo Bar<xlf:ept id=\"1\">ept-data</xlf:ept>");
    }

    private void ensureResultContainsNamespacePrefixesAsInput(String content) throws Exception {
        String input = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r"
                + "<xliff xmlns:xlf=\"urn:oasis:names:tc:xliff:document:1.2\" version=\"1.2\">\r"
                + "<xlf:file source-language=\"en\" target-language=\"fr\" datatype=\"x-test\" original=\"file.ext\">"
                + "<xlf:body>"
                + "<xlf:trans-unit id=\"1\">"
                + "<xlf:source>" + content + "</xlf:source>"
                + "<xlf:target>" + content + "</xlf:target>"
                + "</xlf:trans-unit>"
                + "</xlf:body>"
                + "</xlf:file>"
                + "</xliff>";

        String translation = createXliffTranslation(input);
        String actual = mergeTranslation(input, translation);

        assertXMLEqual(input, actual);
    }

    private String createXliffTranslation(String input) throws IOException {
        ArrayList<Event> events = getEvents(input);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        XLIFFWriter xliffWriter = new XLIFFWriter();
        xliffWriter.getParameters().setPlaceholderMode(true);
        xliffWriter.getParameters().setIncludeCodeAttrs(true);
        xliffWriter.setOutput(outputStream);
        for (Event event : events) {
            xliffWriter.handleEvent(event);
        }
        xliffWriter.close();
        outputStream.close();

        return new String(outputStream.toByteArray(), "UTF-8");
    }

    private String mergeTranslation(String input, String translation) {
        StringWriter stringWriter = new StringWriter();
        OutputStream resultStream = new WriterOutputStream(stringWriter, StandardCharsets.UTF_8);

        XLIFFFilter originalFilter = new XLIFFFilter();
        originalFilter.open(new RawDocument(input, LocaleId.GERMAN, LocaleId.ENGLISH));

        try (SkeletonMergerWriter skelMergerWriter = new SkeletonMergerWriter(originalFilter,
                null)) {
            skelMergerWriter.setOptions(LocaleId.ENGLISH, "UTF-8");
            skelMergerWriter.setOutput(resultStream);

            try (XLIFFFilter translationFilter = new XLIFFFilter()) {
                translationFilter
                        .open(new RawDocument(translation, LocaleId.GERMAN, LocaleId.ENGLISH));
                while (translationFilter.hasNext()) {
                    Event next = translationFilter.next();
                    skelMergerWriter.handleEvent(next);
                }
            }
        }

        return stringWriter.toString();
    }

    private ArrayList<Event> getEvents(String snippet) {
        return getEvents(snippet, filter);
    }

    private ArrayList<Event> getEvents (String snippet,
            XLIFFFilter filterToUse) {
        return FilterTestDriver.getEvents(filterToUse, snippet, locEN, locFR);
    }
}