package net.sf.okapi.filters.openxml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(JUnit4.class)
public class TestConditionalParameters {

	private static final Logger log = LoggerFactory.getLogger(TestConditionalParameters.class);

	@Test
	public void testFindExcludedColumnsForSheetNumber() {
        String yaml =
				"#v1\n" +
				"bPreferenceTranslateDocProperties.b=false\n" +
				"bPreferenceTranslateComments.b=false\n" +
				"bPreferenceTranslatePowerpointNotes.b=true\n" +
				"bPreferenceTranslatePowerpointMasters.b=true\n" +
				"bPreferenceTranslateWordHeadersFooters.b=true\n" +
				"bPreferenceTranslateWordHidden.b=false\n" +
				"bPreferenceTranslateExcelExcludeColors.b=false\n" +
				"bPreferenceTranslateExcelExcludeColumns.b=true\n" +
				"tsExcelExcludedColors.i=0\n" +
				"tsExcelExcludedColumns.i=4\n" +
				"tsExcludeWordStyles.i=0\n" +
				"zzz0=1A\n" +
				"zzz1=1B\n" +
				"zzz2=3A\n" +
				"zzz3=3D\n";
        ConditionalParameters params = new ConditionalParameters();
        params.fromString(yaml);
        HashSet<String> expected1 = new HashSet<String>();
        expected1.add("A");
        expected1.add("B");
        HashSet<String> expected3 = new HashSet<String>();
        expected3.add("A");
        expected3.add("D");
        assertEquals(expected1, params.findExcludedColumnsForSheetNumber(1));
        assertEquals(Collections.emptySet(), params.findExcludedColumnsForSheetNumber(2));
        assertEquals(expected3, params.findExcludedColumnsForSheetNumber(3));
        // Sheets past 3 use the configuration for "sheet 3"
        assertEquals(expected3, params.findExcludedColumnsForSheetNumber(4));
	}

	@Test
	public void testDontExcludeColumnsIfOptionDisabled() {
        String yaml =
				"#v1\n" +
				"bPreferenceTranslateDocProperties.b=false\n" +
				"bPreferenceTranslateComments.b=false\n" +
				"bPreferenceTranslatePowerpointNotes.b=true\n" +
				"bPreferenceTranslatePowerpointMasters.b=true\n" +
				"bPreferenceTranslateWordHeadersFooters.b=true\n" +
				"bPreferenceTranslateWordHidden.b=false\n" +
				"bPreferenceTranslateExcelExcludeColors.b=false\n" +
				"bPreferenceTranslateExcelExcludeColumns.b=false\n" + // <--- don't actually exclude anything
				"tsExcelExcludedColors.i=0\n" +
				"tsExcelExcludedColumns.i=4\n" +
				"tsExcludeWordStyles.i=0\n" +
				"zzz0=1A\n" +
				"zzz1=1B\n" +
				"zzz2=3A\n" +
				"zzz3=3D\n";
        ConditionalParameters params = new ConditionalParameters();
        params.fromString(yaml);
        assertEquals(Collections.emptySet(), params.findExcludedColumnsForSheetNumber(1));
        assertEquals(Collections.emptySet(), params.findExcludedColumnsForSheetNumber(2));
        assertEquals(Collections.emptySet(), params.findExcludedColumnsForSheetNumber(3));
        assertEquals(Collections.emptySet(), params.findExcludedColumnsForSheetNumber(4));
	}

	@Test
	public void testFromStringForTsComplexFieldDefinitionsToExtract() {
		String yaml =
				"#v1\n" +
				"tsComplexFieldDefinitionsToExtract.i=3\n" +
				"cfd0=HYPERLINK\n" +
				"cfd1=FORMTEXT\n" +
				"cfd2=TOC\n";
		ConditionalParameters params = new ConditionalParameters();
		params.fromString(yaml);
		HashSet<String> expected1 = new HashSet<String>();
		expected1.add("HYPERLINK");
		expected1.add("FORMTEXT");
		expected1.add("TOC");
		assertEquals(expected1, params.tsComplexFieldDefinitionsToExtract);
	}

	@Test
	public void testToStringForTsComplexFieldDefinitionsToExtract() {
		ConditionalParameters params = new ConditionalParameters();
		params.tsComplexFieldDefinitionsToExtract.add("FORMTEXT");

		String yaml = params.toString();

		List<String> lines = Arrays.asList(yaml.split("\\\n"));
		assertTrue(lines.contains("tsComplexFieldDefinitionsToExtract.i=2"));
		assertTrue(lines.contains("cfd0=FORMTEXT"));
		assertTrue(lines.contains("cfd1=HYPERLINK"));
	}

	@Test
	public void testMigrateLegacyDefaultTranslatableField() {
		String yaml =
				"#v1\n" +
				"bPreferenceTranslateDocProperties.b=false\n" +
				"bPreferenceTranslateComments.b=false\n" +
				"bPreferenceTranslatePowerpointNotes.b=true\n" +
				"bPreferenceTranslatePowerpointMasters.b=true\n" +
				"bPreferenceTranslateWordHeadersFooters.b=true\n" +
				"bPreferenceTranslateWordHidden.b=false\n" +
				"bPreferenceTranslateExcelExcludeColors.b=false\n" +
				"bPreferenceTranslateExcelExcludeColumns.b=true\n" +
				"tsExcelExcludedColors.i=0\n" +
				"tsExcelExcludedColumns.i=4\n" +
				"tsExcludeWordStyles.i=0\n" +
				"zzz0=1A\n" +
				"zzz1=1B\n" +
				"zzz2=3A\n" +
				"zzz3=3D\n";
		ConditionalParameters params = new ConditionalParameters();
		params.fromString(yaml);
		HashSet<String> expected1 = new HashSet<String>();
		expected1.add("HYPERLINK");
		assertEquals(expected1, params.tsComplexFieldDefinitionsToExtract);
	}

	@Test
	public void testIncludedSlidesOnly() throws Exception {
		ConditionalParameters params = new ConditionalParameters();
		params.setPowerpointIncludedSlideNumbersOnly(true);
		params.tsPowerpointIncludedSlideNumbers = new TreeSet<>(Arrays.asList(1, 2, 3, 5, 7, 11));

		String paramsToString = params.toString();
		log.info("Params: {}", paramsToString);

		ConditionalParameters params2 = new ConditionalParameters();
		params2.fromString(paramsToString);

		assertThat(params2.getPowerpointIncludedSlideNumbersOnly()).isTrue();
		assertThat(params2.tsPowerpointIncludedSlideNumbers)
				.containsExactly(1, 2, 3, 5, 7, 11);
	}
}
