package net.sf.okapi.filters.openxml;

import static org.junit.Assert.assertEquals;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.FileLocation;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.exceptions.OkapiUnexpectedRevisionException;

import org.custommonkey.xmlunit.Diff;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@RunWith(JUnit4.class)
public class TestStyledTextUnitWriter {
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	private XMLFactories factories = new XMLFactoriesForTest();

	@Test
	public void testSimpleStyles() throws Exception {
		checkFile("document-simplestyles.xml", new ConditionalParameters());
	}

	@Test
	public void testComplexStyles() throws Exception {
		checkFile("document-complexstyles.xml", new ConditionalParameters());
	}

	@Test
	public void testComplexStyles2() throws Exception {
		checkFile("document-complexstyles2.xml", new ConditionalParameters());
	}

	@Test
	public void testTab() throws Exception {
		checkFile("document-tab.xml", new ConditionalParameters());
	}

	@Test
	public void testHyperlink() throws Exception {
		checkFile("document-hyperlink.xml", new ConditionalParameters());
	}

	@Test
	public void testSmartTag() throws Exception {
		checkFile("document-smarttag.xml", new ConditionalParameters());
	}

	@Test
	public void testEmpty() throws Exception {
		checkFile("document-empty.xml", new ConditionalParameters());
	}

	@Test
	public void testOverlapping() throws Exception {
		checkFile("document-overlapping.xml", new ConditionalParameters());
	}

	@Test
	public void testHidden() throws Exception {
		checkFile("document-hidden.xml", new ConditionalParameters());
	}

	@Test
	public void testTextbox() throws Exception {
		checkFile("document-textbox.xml", new ConditionalParameters());
	}

	@Test
	public void testTextbox2() throws Exception {
		checkFile("document-textboxes.xml", new ConditionalParameters());
	}

	@Test
	public void testEscaping() throws Exception {
		checkFile("document-escaping.xml", new ConditionalParameters());
	}

	@Test
	public void testTextpath() throws Exception {
		checkFile("document-textpath.xml", new ConditionalParameters());
	}

	@Test
	public void testNoBreakHyphenToCharacterConversion() throws Exception {
		checkFile("document-no-break-hyphen.xml", new ConditionalParametersBuilder().replaceNoBreakHyphenTag(true).build());
	}

	@Test
	public void testSoftHyphenIgnoration() throws Exception {
		checkFile("document-soft-hyphen.xml", new ConditionalParametersBuilder().ignoreSoftHyphenTag(true).build());
	}

	@Test
	public void testLineBreakToCharacterConversion() throws Exception {
		checkFile("document-br.xml", new ConditionalParametersBuilder().addLineSeparatorCharacter(true).build());
	}

	@Test
	public void testBcsSkip() throws Exception {
		checkFile("document-complex-script-skip.xml", new ConditionalParametersBuilder().cleanupAggressively(true).build());
	}

	@Test
	public void testHyperlinkComplexFieldCharacters() throws Exception {
		checkFile("document-hyperlink-fldChar.xml", new ConditionalParameters());
	}

	@Test
	public void testNestedComplexFieldCharacters() throws Exception {
		checkFile("document-nested-fldChar.xml", new ConditionalParameters());
	}

	@Test
	public void testAlternateContent() throws Exception {
		checkFile("document-alternate-content.xml", new ConditionalParameters());
	}

	@Test
	public void testEmptyRunIgnoration() throws Exception {
		checkFile("document-empty-run.xml", new ConditionalParameters());
	}

	@Test
	public void testAttributesStripping() throws Exception {
		checkFile("slide-strippable-attributes.xml", new ConditionalParameters());
	}

	@Test
	public void testBidirectionality() throws Exception {
		checkFile("document-bidi-rtl-1.xml", new ConditionalParameters(), LocaleId.ARABIC);
		checkFile("document-bidi-rtl-2.xml", new ConditionalParameters(), LocaleId.ENGLISH);
		checkFile("document-bidi-rtl-lang.xml", new ConditionalParameters(), LocaleId.HEBREW);
		checkFile("document-bidi-table-properties-1.xml", new ConditionalParameters(), LocaleId.ENGLISH);
		checkFile("document-bidi-table-properties-2.xml", new ConditionalParameters(), LocaleId.ARABIC);
		checkFile("slide-bidi-table-and-text-body-attributes-en.xml", new ConditionalParameters(), LocaleId.ENGLISH);
		checkFile("slide-bidi-table-and-text-body-attributes-ar.xml", new ConditionalParameters(), LocaleId.ARABIC);
	}

	@Test
	public void testRevisionInformationStripping() throws Exception {
		checkFile("document-revision-information-stripping.xml", new ConditionalParameters());
	}

	@Test(expected=OkapiUnexpectedRevisionException.class)
	public void testRevisionInformationIsNotStripped() throws Exception {
		checkFile("document-revision-information-stripping.xml", new ConditionalParametersBuilder().automaticallyAcceptRevisions(false).build());
	}

	private void checkFile(String name, ConditionalParameters params) throws Exception {
		checkFile(name, params, LocaleId.FRENCH);
	}

	private void checkFile(String name, ConditionalParameters params, LocaleId localeId) throws Exception {
		List<Event> events = parseEvents(getReaderBlockPart(name), params);

		StyledTextSkeletonWriter skelWriter = new StyledTextSkeletonWriter(factories.getEventFactory(), "test", params);
		Path temp = Files.createTempFile("openxml", ".xml");
		//System.out.println("Checking " + name + " against " + temp);
		Writer w = Files.newBufferedWriter(temp, StandardCharsets.UTF_8);
		for (Event event : events) {
			w.write(handleEvent(skelWriter, event, localeId));
		}
		w.close();

		Path goldPath = FileLocation.fromClass(getClass()).in("/gold/parts/block/" + name).asPath();
		String goldContent = new String(Files.readAllBytes(goldPath), StandardCharsets.UTF_8);
		String tempContent = new String(Files.readAllBytes(temp), StandardCharsets.UTF_8);

		Diff diff = new Diff(goldContent, tempContent);
		if (!diff.similar()) {
			StringBuffer sb = new StringBuffer("'" + name + "' gold file does not match " + temp + ":");
			diff.appendMessage(sb);
			LOGGER.warn(sb.toString());
			assertEquals(goldContent, tempContent);
		}

		Files.delete(temp);
	}

	private String handleEvent(StyledTextSkeletonWriter skelWriter, Event event, LocaleId localeId) {
		switch ( event.getEventType() ) {
		case START_DOCUMENT:
			return skelWriter.processStartDocument(localeId, "UTF-8", null, null, event.getStartDocument());
		case END_DOCUMENT:
			return skelWriter.processEndDocument(event.getEnding());
		case START_SUBDOCUMENT:
			skelWriter.setTargetLocale(localeId);
			return skelWriter.processStartSubDocument(event.getStartSubDocument());
		case END_SUBDOCUMENT:
			return skelWriter.processEndSubDocument(event.getEnding());
		case START_GROUP:
			return skelWriter.processStartGroup(event.getStartGroup());
		case END_GROUP:
			return skelWriter.processEndGroup(event.getEnding());
		case TEXT_UNIT:
			return skelWriter.processTextUnit(event.getTextUnit());
		case DOCUMENT_PART:
			return skelWriter.processDocumentPart(event.getDocumentPart());
		default:
			return "";
		}
	}

	private XMLEventReader getReaderBlockPart(String resource) throws Exception {
		final InputStream inputStream = FileLocation.fromClass(getClass()).in("/parts/block/" + resource).asInputStream();
		return factories.getInputFactory().createXMLEventReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
	}

	private List<Event> parseEvents(XMLEventReader xmlReader, ConditionalParameters params) throws XMLStreamException {
		List<Event> events = new ArrayList<>();
		StyledTextPartHandler handler = new StyledTextPartHandler(params, factories.getEventFactory(), "part", new EmptyStyleDefinitions());
		events.add(handler.open("testDoc", "testSubDoc", xmlReader));
		while (handler.hasNext()) {
			events.add(handler.next());
		}
		return events;
	}
}
