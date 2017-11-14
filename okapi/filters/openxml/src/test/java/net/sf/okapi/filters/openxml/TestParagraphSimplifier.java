package net.sf.okapi.filters.openxml;

import static org.junit.Assert.assertEquals;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;

import org.custommonkey.xmlunit.Diff;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.okapi.common.FileLocation;

@RunWith(JUnit4.class)
public class TestParagraphSimplifier {
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	private XMLInputFactory inputFactory = XMLInputFactory.newInstance();
	private XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
	private XMLEventFactory eventFactory = XMLEventFactory.newInstance();

	@Test
	public void testSimplifier() throws Exception {
		simplifyAndCheckFile("document-simple.xml");
		simplifyAndCheckFileAggressive("document-simple.xml");
	}

	@Test
	public void testDontMergeWhenPropertiesDontMatch() throws Exception {
		simplifyAndCheckFile("document-prop_mismatch.xml");
	}

	@Test
	public void testWithTabs() throws Exception {
		simplifyAndCheckFile("document-multiple_tabs.xml");
	}

	@Test
	public void testHeaderWithConsecutiveTabs() throws Exception {
		simplifyAndCheckFile("header-tabs.xml");
	}

	@Test
	public void testTextBoxes() throws Exception {
		simplifyAndCheckFile("document-textboxes.xml");
	}

	@Test
	public void testRuby() throws Exception {
		simplifyAndCheckFile("document-ruby.xml");
	}

	@Test
	public void testSlide() throws Exception {
		simplifyAndCheckFile("slide-sample.xml");
	}

	@Test
	public void testInstrText() throws Exception {
		simplifyAndCheckFile("document-instrText.xml");
	}

	@Test
	public void testAltContent() throws Exception {
		simplifyAndCheckFile("document-altcontent.xml");
	}

	@Test
	public void testPreserveSpaceReset() throws Exception {
		simplifyAndCheckFile("document-preserve.xml");
	}

	@Test
	public void testStripLastRenderedPagebreak() throws Exception {
		simplifyAndCheckFile("document-pagebreak.xml");
	}

	@Test
	public void testStripSpellingGrammarError() throws Exception {
		simplifyAndCheckFile("document-spelling.xml");
	}

	@Test
	public void testLangAttributeAndEmptyRunPropertyMerging() throws Exception {
		simplifyAndCheckFile("document-lang.xml");
	}

	@Test
	public void testDontConsolidateMathRuns() throws Exception {
		simplifyAndCheckFile("slide-formulas.xml");
	}

	@Test
	public void testAggressiveSpacingTrimming() throws Exception {
		simplifyAndCheckFile("document-spacing.xml");
		simplifyAndCheckFileAggressive("document-spacing.xml");
	}

	@Test
	public void testAggressiveVertAlignTrimming() throws Exception {
		simplifyAndCheckFileAggressive("document-vertAlign.xml");
	}

	@Test
	public void testGoBackBookmark() throws Exception {
		simplifyAndCheckFile("document-goback.xml");
	}

	@Test
	public void testTab() throws Exception {
		simplifyAndCheckFileTabAsChar("document-tab.xml");
	}

	@Test
	public void testFonts() throws Exception {
		simplifyAndCheckFile("document-fonts.xml");
	}

	@Test
	public void testLineSeparatorSlide() throws Exception {
		simplifyAndCheckFileLineSeparatorAsChar("slide-linebreak.xml", '\n');
	}

	@Test
	public void testLineSeparatorSlide2028() throws Exception {
		simplifyAndCheckFileLineSeparatorAsChar("slide-linebreak-2028.xml", '\u2028');
	}

	public Path simplifyFile(String name) throws Exception {
		return simplifyFile(name, new ConditionalParametersBuilder()
				.cleanupAggressively(false)
				.addTabAsCharacter(false)
				.lineSeparatorAsChar(false)
				.build());
	}

	public Path simplifyFileAggressively(String name) throws Exception {
		return simplifyFile(name, new ConditionalParametersBuilder()
				.cleanupAggressively(true)
				.addTabAsCharacter(false)
				.lineSeparatorAsChar(false)
				.build());
	}

	public Path simplifyFile(String name, ConditionalParameters params) throws Exception {
		XMLEventReader xmlReader = inputFactory.createXMLEventReader(
				getClass().getResourceAsStream("/parts/simplifier/" + name), "UTF-8");
		Path temp = Files.createTempFile("simplify", ".xml");
		//System.out.println("Writing simplified " + name + " (aggressive=" + aggressiveTrimming + ") to " + temp);
		XMLEventWriter xmlWriter = outputFactory.createXMLEventWriter(
				Files.newBufferedWriter(temp, StandardCharsets.UTF_8));

		ParagraphSimplifier simplifier = new ParagraphSimplifier(xmlReader, xmlWriter, eventFactory, params, new EmptyStyleDefinitions());

		simplifier.process();
		xmlReader.close();
		xmlWriter.close();
		return temp;
	}

	// Simplify
	//   src/test/resources/parts/simplifier/[name]
	// And compare to
	//   src/test/resources/gold/parts/simplifier/[name]
	public void simplifyAndCheckFile(String name) throws Exception {
		simplifyAndCheckFile(name, "/gold/parts/simplifier/", new ConditionalParametersBuilder()
				.cleanupAggressively(false)
				.addTabAsCharacter(false)
				.lineSeparatorAsChar(false)
				.build());
	}
	public void simplifyAndCheckFileAggressive(String name) throws Exception {
		simplifyAndCheckFile(name, "/gold/parts/simplifier/aggressive/", new ConditionalParametersBuilder()
				.cleanupAggressively(true)
				.addTabAsCharacter(false)
				.lineSeparatorAsChar(false)
				.build());
	}
	public void simplifyAndCheckFileTabAsChar(String name) throws Exception {
		simplifyAndCheckFile(name, "/gold/parts/simplifier/tabAsChar/", new ConditionalParametersBuilder()
				.cleanupAggressively(false)
				.addTabAsCharacter(true)
				.lineSeparatorAsChar(false)
				.build());
	}

	private void simplifyAndCheckFileLineSeparatorAsChar(String name, char lineSeparatorReplacement) throws Exception {
		simplifyAndCheckFile(name, "/gold/parts/simplifier/lbAsChar/", new ConditionalParametersBuilder()
				.cleanupAggressively(true)
				.addTabAsCharacter(false)
				.lineSeparatorAsChar(true)
				.lineSeparatorReplacement(lineSeparatorReplacement)
				.build());
	}

	public void simplifyAndCheckFile(String name, String goldDir, ConditionalParameters params) throws Exception {
		final Path temp = simplifyFile(name, params);

		final Path goldFile = FileLocation.fromClass(getClass()).in(goldDir + name).asPath();
		final String goldContent = new String(Files.readAllBytes(goldFile), StandardCharsets.UTF_8);
		final String tempContent = new String(Files.readAllBytes(temp), StandardCharsets.UTF_8);

		final Diff diff = new Diff(goldContent, tempContent);
		if (!diff.similar()) {
			StringBuffer sb = new StringBuffer("'" + name + "' gold file does not match " + temp + ":");
			diff.appendMessage(sb);
			LOGGER.warn(sb.toString());
			assertEquals(goldContent, tempContent);
		}

		Files.delete(temp);
	}
}