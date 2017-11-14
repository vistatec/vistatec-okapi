package net.sf.okapi.filters.openxml;

import static net.sf.okapi.filters.openxml.CodePeekTranslator.locENUS;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.filters.FilterTestDriver;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.RawDocument;
import org.assertj.core.api.iterable.Extractor;
import org.junit.Test;

/**
 * @author jpmaas
 * @since 17.08.2017
 */
public class OpenXmlXlsxTest {

    @Test
    public void testTextFields() throws Exception {
        ConditionalParameters parameters = new ConditionalParameters();
        parameters.setTranslateExcelDrawings(true);
        parameters.setTranslateDocProperties(false);

        OpenXMLFilter filter = new OpenXMLFilter();
        filter.setParameters(parameters);

        URL given = getClass().getResource("/textfield.xlsx");
        RawDocument doc = new RawDocument(given.toURI(), "UTF-8", locENUS);

        ArrayList<Event> actual = getEvents(filter, doc);
        List<ITextUnit> textUnits = FilterTestDriver.filterTextUnits(actual);
        assertThat(textUnits).extracting(new Extractor<ITextUnit, Object>() {
            @Override
            public Object extract(ITextUnit input) {
                return input.getSource().toString();
            }
        }).containsExactly(
                "Hallo Welt!",
                "<run1>Ich bin ein Textfeld!</run1>");
    }

    @Test
    public void testSmartArt() throws Exception {
        ConditionalParameters parameters = new ConditionalParameters();
        parameters.setTranslateExcelDiagramData(true);
        parameters.setTranslateDocProperties(false);

        OpenXMLFilter filter = new OpenXMLFilter();
        filter.setParameters(parameters);

        URL given = getClass().getResource("/smartart.xlsx");
        RawDocument doc = new RawDocument(given.toURI(), "UTF-8", locENUS);

        ArrayList<Event> actual = getEvents(filter, doc);
        List<ITextUnit> textUnits = FilterTestDriver.filterTextUnits(actual);
        assertThat(textUnits).extracting(new Extractor<ITextUnit, Object>() {
            @Override
            public Object extract(ITextUnit input) {
                return input.getSource().toString();
            }
        }).containsExactly(
                "Hallo Welt!",
                "<run1>Ich</run1>",
                "<run1>bin</run1>",
                "<run1>ein</run1>",
                "<run1>Smart</run1>",
                "<run1>Art</run1>"
        );
    }

    @Test
    public void testSmartArtHidden() throws Exception {
        ConditionalParameters parameters = new ConditionalParameters();
        parameters.setTranslateExcelDiagramData(true);
        parameters.setTranslateDocProperties(false);
        parameters.setTranslateExcelHidden(false);

        OpenXMLFilter filter = new OpenXMLFilter();
        filter.setParameters(parameters);

        URL given = getClass().getResource("/SmartArt3Sheets.xlsx");
        RawDocument doc = new RawDocument(given.toURI(), "UTF-8", locENUS);

        ArrayList<Event> actual = getEvents(filter, doc);
        List<ITextUnit> textUnits = FilterTestDriver.filterTextUnits(actual);
        assertThat(textUnits).extracting(new Extractor<ITextUnit, Object>() {
            @Override
            public Object extract(ITextUnit input) {
                return input.getSource().toString();
            }
        }).containsExactly(
                "Zelle 1",
                "Zelle 3",
                "<run1>Smart Art 1</run1>",
                "<run1>Smart Art 3</run1>"
        );
    }

    @Test
    public void testTextFieldsHidden() throws Exception {
        ConditionalParameters parameters = new ConditionalParameters();
        parameters.setTranslateExcelDrawings(true);
        parameters.setTranslateDocProperties(false);
        parameters.setTranslateExcelHidden(false);

        OpenXMLFilter filter = new OpenXMLFilter();
        filter.setParameters(parameters);

        URL given = getClass().getResource("/Textfeld3Sheets.xlsx");
        RawDocument doc = new RawDocument(given.toURI(), "UTF-8", locENUS);

        ArrayList<Event> actual = getEvents(filter, doc);
        List<ITextUnit> textUnits = FilterTestDriver.filterTextUnits(actual);
        assertThat(textUnits).extracting(new Extractor<ITextUnit, Object>() {
            @Override
            public Object extract(ITextUnit input) {
                return input.getSource().toString();
            }
        }).containsExactly(
                "Zelle 1",
                "Zelle 3",
                "<run1>Textfeld 1</run1>",
                "<run1>Textfeld 3</run1>");
    }

    @Test
    public void testSheetNamesHiddenExclude() throws Exception {
        ConditionalParameters parameters = new ConditionalParameters();
        parameters.setTranslateExcelSheetNames(true);

        OpenXMLFilter filter = new OpenXMLFilter();
        filter.setParameters(parameters);

        URL given = getClass().getResource("/SheetNameHidden.xlsx");
        RawDocument doc = new RawDocument(given.toURI(), "UTF-8", locENUS);

        ArrayList<Event> actual = getEvents(filter, doc);
        List<ITextUnit> textUnits = FilterTestDriver.filterTextUnits(actual);
        assertThat(textUnits).extracting(new Extractor<ITextUnit, Object>() {
            @Override
            public Object extract(ITextUnit input) {
                return input.getSource().toString();
            }
        }).containsExactly(
                "Cell Visible",
                "Sheet Visible");
    }

    @Test
    public void testSheetNamesHiddenInclude() throws Exception {
        ConditionalParameters parameters = new ConditionalParameters();
        parameters.setTranslateExcelSheetNames(true);
        parameters.setTranslateExcelHidden(true);

        OpenXMLFilter filter = new OpenXMLFilter();
        filter.setParameters(parameters);

        URL given = getClass().getResource("/SheetNameHidden.xlsx");
        RawDocument doc = new RawDocument(given.toURI(), "UTF-8", locENUS);

        ArrayList<Event> actual = getEvents(filter, doc);
        List<ITextUnit> textUnits = FilterTestDriver.filterTextUnits(actual);
        assertThat(textUnits).extracting(new Extractor<ITextUnit, Object>() {
            @Override
            public Object extract(ITextUnit input) {
                return input.getSource().toString();
            }
        }).containsExactly(
                "Cell Visible",
                "Cell Hidden",
                "Sheet Visible",
                "Sheet Hidden");
    }

    @Test
    public void testFormattings() throws Exception {
        ConditionalParameters params = new ConditionalParameters();
        params.setTranslateDocProperties(false);
        params.setTranslatePowerpointMasters(false);

        OpenXMLFilter filter = new OpenXMLFilter();
        filter.setParameters(params);

        URL url = getClass().getResource("/Formattings.xlsx");

        RawDocument doc = new RawDocument(url.toURI(), "UTF-8", locENUS);
        ArrayList<Event> events = getEvents(filter, doc);

        List<ITextUnit> textUnits = FilterTestDriver.filterTextUnits(events);
        assertThat(textUnits).extracting(textUnitSourceExtractor()).containsExactlyInAnyOrder(
                "This is a <run1>bold formatting</run1>",
                "This is an <run1>italics formatting</run1>",
                "This is an <run1>underlined formatting</run1>",
                "This is a hyperlink"
        );

        assertThat(
                textUnits.get(0).getSource().getParts().get(0).getContent().getCodes()
        ).hasSize(2).extracting("type").containsExactly(
                "x-bold;",
                "x-bold;"
        );
        assertThat(
                textUnits.get(1).getSource().getParts().get(0).getContent().getCodes()
        ).hasSize(2).extracting("type").containsExactly(
                "x-italic;",
                "x-italic;"
        );
        assertThat(
                textUnits.get(2).getSource().getParts().get(0).getContent().getCodes()
        ).hasSize(2).extracting("type").containsExactly(
                "x-underline:single;",
                "x-underline:single;"
        );
        assertThat(
                textUnits.get(3).getSource().getParts().get(0).getContent().getCodes()
        ).hasSize(0);
    }

    private Extractor<ITextUnit, Object> textUnitSourceExtractor() {
        return new Extractor<ITextUnit, Object>() {
            @Override
            public Object extract(ITextUnit input) {
                return input.getSource().toString();
            }
        };
    }

    @SuppressWarnings("Duplicates")
    private ArrayList<Event> getEvents(OpenXMLFilter filter, RawDocument doc) {
        ArrayList<Event> list = new ArrayList<>();
        filter.open(doc, false);
        while (filter.hasNext()) {
            Event event = filter.next();
            list.add(event);
        }
        filter.close();
        return list;
    }
}
