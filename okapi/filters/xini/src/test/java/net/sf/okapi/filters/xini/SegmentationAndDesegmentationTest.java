package net.sf.okapi.filters.xini;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.filters.xini.jaxb.Seg;
import net.sf.okapi.filters.xini.jaxb.Xini;
import net.sf.okapi.steps.segmentation.Parameters;
import net.sf.okapi.steps.segmentation.SegmentationStep;

/**
 * segmentation rules of the defaultSegmentation.srx:
 * 1. first sentence has to end with a period
 * 2. second segment has to start with a capital letter
 * 3. between the sentences has to be a whitespace character
 *
 * ...otherwise the default-segmentation-rule is not working correct
 *
 * correct example: 'Sentence1. Sentence2.' -> 2 segments after segmentation
 * wrong example: 'Sentence1. sentence2.' -> 1 segment after segmentation
 */
@RunWith(JUnit4.class)
public class SegmentationAndDesegmentationTest {

	private XINIFilter filter = new XINIFilter();
	private LocaleId locEN = LocaleId.fromString("en");
	private LocaleId locDE = LocaleId.fromString("de");
	private StepHelper segmentizer;
	private XINIFilterTestHelper xiniHelper = new XINIFilterTestHelper();
	private String startSnippetForTable =
		"<?xml version=\"1.0\" ?>" +
		"<Xini SchemaVersion=\"1.0\" xsi:noNamespaceSchemaLocation=\"http://www.ontram.com/xsd/xini.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
		"	<Main>" +
		"		<Page PageID=\"1\">" +
		"			<Elements>" +
		"				<Element ElementID=\"10\" Size=\"50\">" +
		"					<ElementContent>";
	private String endSnippetForTable =
		"					</ElementContent>" +
		"				</Element>" +
		"			</Elements>" +
		"		</Page>" +
		"	</Main>" +
		"</Xini>";
	private String startSnippet =
		startSnippetForTable +
		"						<Fields>" +
		"							<Field FieldID=\"0\">";
	private String endSnippet =
		"							</Field>" +
		"						</Fields>" +
		endSnippetForTable;

	@Before
	public void prepare() {
		URL url =  ClassLoader.getSystemResource("defaultSegmentation.srx");
		String path = new File(Util.URLtoURI(url)).getPath();
		((net.sf.okapi.filters.xini.Parameters) filter.getParameters()).setUseOkapiSegmentation(true);
		Parameters params = new Parameters();
		params.setSourceSrxPath(path);
		SegmentationStep segmentationStep = new SegmentationStep();
		segmentationStep.setParameters(params);
		segmentationStep.setSourceLocale(locDE);
		segmentationStep.setTargetLocales(Arrays.asList(locEN));
		segmentizer = new StepHelper(segmentationStep);
	}

	@Test
	public void formattingsAreNotBreakingApart() throws Exception {
		String segSnippet = "<Seg SegID=\"0\"><sub>Don't</sub><sub> break these sentences. Apart.</sub></Seg>";
		String xiniSnippet = startSnippet + segSnippet + endSnippet;

		List<Seg> segsOfFirstField = doSegmentation(xiniSnippet);

		assertEquals(1, segsOfFirstField.size());

		Seg firstSeg = segsOfFirstField.get(0);

		xiniHelper.checkContent(firstSeg, "<sub>Don't</sub><sub> break these sentences. Apart.</sub>");
	}

	@Test
	public void formattingsAreNotBreakingApart2() throws Exception {
		String segSnippet = "<Seg SegID=\"0\"><sub>Don't</sub><i> break these sentences. Apart.</i></Seg>";
		String xiniSnippet = startSnippet + segSnippet + endSnippet;

		List<Seg> segsOfFirstField = doSegmentation(xiniSnippet);

		assertEquals(1, segsOfFirstField.size());

		Seg firstSeg = segsOfFirstField.get(0);

		xiniHelper.checkContent(firstSeg, "<sub>Don't</sub><i> break these sentences. Apart.</i>");
	}

	@Test
	public void formattingsAreNotBreakingApart3() throws Exception {
		String segSnippet = "<Seg SegID=\"0\"><sub>Don't break. Don't</sub><i> break these sentences. Apart.</i></Seg>";
		String xiniSnippet = startSnippet + segSnippet + endSnippet;

		List<Seg> segsOfFirstField = doSegmentation(xiniSnippet);

		assertEquals(1, segsOfFirstField.size());

		Seg firstSeg = segsOfFirstField.get(0);

		xiniHelper.checkContent(firstSeg, "<sub>Don't break. Don't</sub><i> break these sentences. Apart.</i>");
	}

	@Test
	public void sentencesAreSegmentedAndWhitespaceIsSavedInAttribute() throws Exception {
		String segSnippet = "<Seg SegID=\"0\">T1. T2.</Seg>";
		String xiniSnippet = startSnippet + segSnippet + endSnippet;

		List<Seg> segsOfFirstField = doSegmentation(xiniSnippet);

		assertEquals(2, segsOfFirstField.size());

		Seg firstSeg = segsOfFirstField.get(0);
		Seg secondSeg = segsOfFirstField.get(1);

		xiniHelper.checkContent(firstSeg, "T1.");
		xiniHelper.checkContent(secondSeg, "T2.");
	}

	@Test
	public void newSegmentsHaveIncreasingIDs() {
		String segSnippet = "<Seg SegID=\"0\">T1. T2.</Seg>";
		String xiniSnippet = startSnippet + segSnippet + endSnippet;

		List<Seg> segsOfFirstField = doSegmentation(xiniSnippet);

		Seg firstSeg = segsOfFirstField.get(0);
		Seg secondSeg = segsOfFirstField.get(1);

		assertEquals(2, segsOfFirstField.size());

		assertEquals(0, firstSeg.getSegID());
		assertEquals(1, secondSeg.getSegID());

		assertEquals(0, firstSeg.getSegmentIDBeforeSegmentation().intValue());
		assertEquals(0, secondSeg.getSegmentIDBeforeSegmentation().intValue());
	}

	@Test
	public void originalSegmentIdIsSavedInAttribute() {
		String segSnippet = "<Seg SegID=\"0\">T1. T2.</Seg>";
		String xiniSnippet = startSnippet + segSnippet + endSnippet;

		List<Seg> segsOfFirstField = doSegmentation(xiniSnippet);

		assertEquals(2, segsOfFirstField.size());

		Seg firstSeg = segsOfFirstField.get(0);
		Seg secondSeg = segsOfFirstField.get(1);

		assertEquals(0, firstSeg.getSegmentIDBeforeSegmentation().intValue());
		assertEquals(0, secondSeg.getSegmentIDBeforeSegmentation().intValue());
	}

	@Test
	public void placeholderDoesntChange() throws Exception {
		String segSnippet = "<Seg SegID=\"0\"><ph ID=\"1\" type=\"style\">A Sentence.</ph></Seg>";
		String xiniSnippet = startSnippet + segSnippet + endSnippet;


		List<Seg> segsOfFirstField = doSegmentation(xiniSnippet);
		Seg firstSeg = segsOfFirstField.get(0);

		assertEquals(1, segsOfFirstField.size());
		xiniHelper.checkContent(firstSeg, "<ph ID=\"1\" type=\"style\">A Sentence.</ph>");
	}

	@Test
	public void placeholderDoesntChangeWithDifferentPlacholderType() throws Exception {
		String segSnippet = "<Seg SegID=\"0\"><ph type=\"memory100\" ID=\"1\">A Sentence.</ph></Seg>";
		String xiniSnippet = startSnippet + segSnippet + endSnippet;

		List<Seg> segsOfFirstField = doSegmentation(xiniSnippet);
		Seg firstSeg = segsOfFirstField.get(0);

		assertEquals(1, segsOfFirstField.size());
		xiniHelper.checkContent(firstSeg, "<ph ID=\"1\" type=\"memory100\">A Sentence.</ph>");
	}
	
	@Test
	public void openingTagsPreservedInPlaceholders() throws Exception {
		String segSnippet = "<Seg SegID=\"0\"><ph type=\"ph\" ID=\"1\" opening=\"test\">A Sentence.</ph></Seg>";
		String xiniSnippet = startSnippet + segSnippet + endSnippet;

		List<Seg> segsOfFirstField = doSegmentation(xiniSnippet);
		Seg firstSeg = segsOfFirstField.get(0);

		assertEquals(1, segsOfFirstField.size());
		xiniHelper.checkContent(firstSeg, "<ph ID=\"1\" type=\"ph\" opening=\"test\">A Sentence.</ph>");
	}
	
	
	@Test
	public void openingTagsPreservedInSinglePlaceholders() throws Exception {
		String segSnippet = "<Seg SegID=\"0\">A <ph type=\"ph\" ID=\"1\" opening=\"test\"/>Sentence.</Seg>";
		String xiniSnippet = startSnippet + segSnippet + endSnippet;

		List<Seg> segsOfFirstField = doSegmentation(xiniSnippet);
		Seg firstSeg = segsOfFirstField.get(0);

		assertEquals(1, segsOfFirstField.size());
		xiniHelper.checkContent(firstSeg, "A <ph type=\"ph\" ID=\"1\" opening=\"test\"/>Sentence.");
	}

	@Test
	public void placeholdersAreNotBrokenApart() throws Exception {
		String segSnippet = "<Seg SegID=\"0\">Sentence1<ph ID=\"1\" type=\"ph\"> with ph. Sentence2</ph> with closing ph.</Seg>";
		String xiniSnippet = startSnippet + segSnippet + endSnippet;

		List<Seg> segsOfFirstField = doSegmentation(xiniSnippet);
		Seg firstSeg = segsOfFirstField.get(0);

		assertEquals(1, segsOfFirstField.size());
		xiniHelper.checkContent(firstSeg, "Sentence1<ph ID=\"1\" type=\"ph\"> with ph. Sentence2</ph> with closing ph.");
	}

	@Test
	public void formattingsAreNotBrokenApart() throws Exception {
		String segSnippet = "<Seg SegID=\"0\">Sentence1<b> with b. Sentence2</b> with closing b.</Seg>";
		String xiniSnippet = startSnippet + segSnippet + endSnippet;

		List<Seg> segsOfFirstField = doSegmentation(xiniSnippet);
		Seg firstSeg = segsOfFirstField.get(0);

		assertEquals(1, segsOfFirstField.size());
		xiniHelper.checkContent(firstSeg, "Sentence1<b> with b. Sentence2</b> with closing b.");
	}

	@Test
	public void formattingTagsAndPlaceholdersDontChange(){
		String segSnippet = "<Seg SegID=\"0\">Sentence1<b><i><u><sup><sub><ph ID=\"1\" type=\"ph\"> with many formatting. Sentence2</ph></sub></sup></u></i></b> with closing formatting.</Seg>";
		String xiniSnippet = startSnippet + segSnippet + endSnippet;

		List<Seg> segsOfFirstField = doSegmentation(xiniSnippet);
		Seg firstSeg = segsOfFirstField.get(0);

		assertEquals(1, segsOfFirstField.size());
		checkContentWithoutReorderAttributes(firstSeg, "Sentence1<b><i><u><sup><sub><ph ID=\"1\" type=\"ph\"> with many formatting. Sentence2</ph></sub></sup></u></i></b> with closing formatting.");
	}

	@Test
	public void lineBreaksArePreserved() throws Exception {
		String segSnippet = "<Seg SegID=\"0\">Sentence 1. A new <br/> sentence with <br/> line break.</Seg>";
		String xiniSnippet = startSnippet + segSnippet + endSnippet;

		List<Seg> segsOfFirstField = doSegmentation(xiniSnippet);
		Seg firstSeg = segsOfFirstField.get(0);
		Seg secondSeg = segsOfFirstField.get(1);

		assertEquals(2, segsOfFirstField.size());
		xiniHelper.checkContent(firstSeg, "Sentence 1.");
		xiniHelper.checkContent(secondSeg, "A new <br/> sentence with <br/> line break.");
	}

	@Test
	public void surroundingWhitespacesAreMovedIntoAttributes() throws Exception {
		String segSnippet = "<Seg SegID=\"0\"> Sentence1. </Seg>";
		String xiniSnippet = startSnippet + segSnippet + endSnippet;

		List<Seg> segsOfFirstField = doSegmentation(xiniSnippet);
		Seg firstSeg = segsOfFirstField.get(0);

		assertEquals(1, segsOfFirstField.size());
		xiniHelper.checkContent(firstSeg, "Sentence1.");
		checkLeadingSpacer(firstSeg, " ");
		checkTrailingSpacer(firstSeg, " ");
	}

	@Test
	public void whitespacesFromInBetweenAreMovedIntoAttributes() throws Exception {
		String segSnippet = "<Seg SegID=\"0\"> Sentence1. Sentence2. </Seg>";
		String xiniSnippet = startSnippet + segSnippet + endSnippet;

		List<Seg> segsOfFirstField = doSegmentation(xiniSnippet);
		assertEquals(2, segsOfFirstField.size());

		Seg firstSeg = segsOfFirstField.get(0);
		xiniHelper.checkContent(firstSeg, "Sentence1.");
		checkLeadingSpacer(firstSeg, " ");
		checkTrailingSpacer(firstSeg, " ");

		Seg secondSeg = segsOfFirstField.get(1);
		xiniHelper.checkContent(secondSeg, "Sentence2.");
		checkLeadingSpacer(secondSeg, null);
		checkTrailingSpacer(secondSeg, " ");
	}

	@Test
	public void codesAreNotMovedIntoAttributes() throws Exception {
		String segSnippet = "<Seg SegID=\"0\"> <ph ID=\"1\" type=\"ph\"/> Sentence1 <sph ID=\"2\" type=\"ph\"/> </Seg>";
		String xiniSnippet = startSnippet + segSnippet + endSnippet;

		List<Seg> segsOfFirstField = doSegmentation(xiniSnippet);
		assertEquals(1, segsOfFirstField.size());

		Seg firstSeg = segsOfFirstField.get(0);
		xiniHelper.checkContent(firstSeg, "<ph ID=\"1\" type=\"ph\"/> Sentence1 <sph ID=\"2\" type=\"ph\"/>");
		checkLeadingSpacer(firstSeg, " ");
		checkTrailingSpacer(firstSeg, " ");
	}

	@Test
	public void isolatedPlaceholdersArePreserved() throws Exception {
		String segSnippet = "<Seg SegID=\"0\">Sentence <sph ID=\"2\" type=\"ph\"/>one. Sentence <eph ID=\"3\" type=\"ph\"/>two.</Seg>";
		String xiniSnippet = startSnippet + segSnippet + endSnippet;

		List<Seg> segsOfFirstField = doSegmentation(xiniSnippet);
		assertEquals(2, segsOfFirstField.size());

		Seg firstSeg = segsOfFirstField.get(0);
		Seg secondSeg = segsOfFirstField.get(1);

		xiniHelper.checkContent(firstSeg, "Sentence <sph ID=\"2\" type=\"ph\"/>one.");
		xiniHelper.checkContent(secondSeg, "Sentence <eph ID=\"3\" type=\"ph\"/>two.");
	}

	@Test
	@Ignore("This it not implemented yet")
	public void placeholderIDsStartAt1InEachSegment() throws Exception {
		String segSnippet = "<Seg SegID=\"0\"><ph ID=\"1\">Sentence 1.</ph> <ph ID=\"2\">Sentence 2.</ph> <ph ID=\"3\">Sentence 3.</ph></Seg>";
		String xiniSnippet = startSnippet + segSnippet + endSnippet;

		List<Seg> segsOfFirstField = doSegmentation(xiniSnippet);
		Seg firstSeg = segsOfFirstField.get(0);
		Seg secondSeg = segsOfFirstField.get(1);
		Seg thirdSeg = segsOfFirstField.get(2);

		assertEquals(3, segsOfFirstField.size());

		xiniHelper.checkContent(firstSeg, "<ph ID=\"1\">Sentence 1.</ph>");
		xiniHelper.checkContent(secondSeg, "<ph ID=\"1\">Sentence 2.</ph>");
		xiniHelper.checkContent(thirdSeg, "<ph ID=\"1\">Sentence 3.</ph>");
	}

	@Test
	public void nestedPlaceholdersWithSameIdArePreservedUnchanged() throws Exception {
		String segSnippet = "<Seg SegID=\"0\">Click <ph ID=\"1\" type=\"style\"><ph ID=\"1\" type=\"link\">here</ph></ph> to read more.</Seg>";
		String xiniSnippet = startSnippet + segSnippet + endSnippet;

		List<Seg> segsOfFirstField = doSegmentation(xiniSnippet);
		assertEquals(1, segsOfFirstField.size());

		Seg firstSeg = segsOfFirstField.get(0);
		xiniHelper.checkContent(firstSeg, "Click <ph ID=\"1\" type=\"style\"><ph ID=\"1\" type=\"link\">here</ph></ph> to read more.");
	}

	@Test
	public void emptyPlaceholdersWithSameIdArePreservedUnchanged() throws Exception {
		String segSnippet = "<Seg SegID=\"0\"><ph ID=\"1\" type=\"ph\"/>List item 1 <br/><ph ID=\"1\" type=\"ph\"/>List item 2</Seg>";
		String xiniSnippet = startSnippet + segSnippet + endSnippet;

		List<Seg> segsOfFirstField = doSegmentation(xiniSnippet);
		assertEquals(1, segsOfFirstField.size());

		Seg firstSeg = segsOfFirstField.get(0);
		xiniHelper.checkContent(firstSeg, "<ph ID=\"1\" type=\"ph\"/>List item 1 <br/><ph ID=\"1\" type=\"ph\"/>List item 2");
	}

	@Test
	public void placeholdersWithSameIdArePreservedUnchanged() throws Exception {
		String segSnippet = "<Seg SegID=\"0\">Click <ph ID=\"1\" type=\"style\">save</ph> or <ph ID=\"1\" type=\"style\">save as...</ph>.</Seg>";
		String xiniSnippet = startSnippet + segSnippet + endSnippet;

		List<Seg> segsOfFirstField = doSegmentation(xiniSnippet);
		assertEquals(1, segsOfFirstField.size());

		Seg firstSeg = segsOfFirstField.get(0);
		xiniHelper.checkContent(firstSeg, "Click <ph ID=\"1\" type=\"style\">save</ph> or <ph ID=\"1\" type=\"style\">save as...</ph>.");
	}

	@Test
	public void desegmentizedXiniContainsTrailingWhitespaces() throws Exception {
		String segSnippet = "<Seg LeadingSpacer=\" \" TrailingSpacer=\" \" SegmentIDBeforeSegmentation=\"0\" SegID=\"0\">Sentence 1.</Seg>" +
                "<Seg TrailingSpacer=\" \" SegmentIDBeforeSegmentation=\"0\" SegID=\"1\">Sentence 2.</Seg>" +
                "<Seg TrailingSpacer=\" \" SegmentIDBeforeSegmentation=\"0\" SegID=\"2\">Sentence 3.</Seg>";
		String xiniSnippet = startSnippet + segSnippet + endSnippet;

		((net.sf.okapi.filters.xini.Parameters)filter.getParameters()).setUseOkapiSegmentation(false);

		List<Seg> segsOfFirstField = doDesegmentation(xiniSnippet);
		assertEquals(1, segsOfFirstField.size());

		Seg firstSeg = segsOfFirstField.get(0);
		xiniHelper.checkContent(firstSeg, " Sentence 1. Sentence 2. Sentence 3. ");
	}

	@Test
	public void desegmentizedXiniHasOriginalSegmentIDsRestored() throws Exception {
		String segSnippet = "<Seg SegmentIDBeforeSegmentation=\"0\" SegID=\"0\">Sentence 1.</Seg>" +
                "<Seg SegmentIDBeforeSegmentation=\"0\" SegID=\"1\">Sentence 2.</Seg>" +
                "<Seg SegmentIDBeforeSegmentation=\"1\" SegID=\"2\">Sentence 3.</Seg>";
		String xiniSnippet = startSnippet + segSnippet + endSnippet;

		((net.sf.okapi.filters.xini.Parameters)filter.getParameters()).setUseOkapiSegmentation(false);

		List<Seg> segsOfFirstField = doDesegmentation(xiniSnippet);
		assertEquals(2, segsOfFirstField.size());

		Seg firstSeg = segsOfFirstField.get(0);
		Seg secondSeg = segsOfFirstField.get(1);
		xiniHelper.checkContent(firstSeg, "Sentence 1.Sentence 2.");
		xiniHelper.checkContent(secondSeg, "Sentence 3.");
		assertEquals(0, firstSeg.getSegID());
		assertEquals(1, secondSeg.getSegID());
	}

	@Test
	public void segmentsMergedIfPreviousSegmentHasSurroundingTag() throws Exception {
		String segSnippet = 
			"<Seg SegmentIDBeforeSegmentation=\"0\" SegID=\"0\"><b>Part 1.</b></Seg>" +
			"<Seg SegmentIDBeforeSegmentation=\"0\" SegID=\"1\">Part 2.</Seg>";
		String xiniSnippet = startSnippet + segSnippet + endSnippet;

		((net.sf.okapi.filters.xini.Parameters)filter.getParameters()).setUseOkapiSegmentation(false);

		List<Seg> segsOfFirstField = doDesegmentation(xiniSnippet);

		Seg firstSeg = segsOfFirstField.get(0);
		xiniHelper.checkContent(firstSeg, "<b>Part 1.</b>Part 2.");

	}

	@Test
	public void segmentsMergedIfNextSegmentHasSurroundingTag() throws Exception {
		String segSnippet = 
			"<Seg SegmentIDBeforeSegmentation=\"0\" SegID=\"0\">Part 1.</Seg>" +
			"<Seg SegmentIDBeforeSegmentation=\"0\" SegID=\"1\"><b>Part 2.</b></Seg>";
		String xiniSnippet = startSnippet + segSnippet + endSnippet;

		((net.sf.okapi.filters.xini.Parameters)filter.getParameters()).setUseOkapiSegmentation(false);

		List<Seg> segsOfFirstField = doDesegmentation(xiniSnippet);

		Seg firstSeg = segsOfFirstField.get(0);
		xiniHelper.checkContent(firstSeg, "Part 1.<b>Part 2.</b>");

	}

	@Test
	public void segmentsMergedIfBothSegmentsHaveSurroundingTag() throws Exception {
		String segSnippet = 
			"<Seg SegmentIDBeforeSegmentation=\"0\" SegID=\"0\"><b>Part 1.</b></Seg>" +
			"<Seg SegmentIDBeforeSegmentation=\"0\" SegID=\"1\"><b>Part 2.</b></Seg>";
		String xiniSnippet = startSnippet + segSnippet + endSnippet;

		((net.sf.okapi.filters.xini.Parameters)filter.getParameters()).setUseOkapiSegmentation(false);

		List<Seg> segsOfFirstField = doDesegmentation(xiniSnippet);

		Seg firstSeg = segsOfFirstField.get(0);
		xiniHelper.checkContent(firstSeg, "<b>Part 1.</b><b>Part 2.</b>");

	}

	private List<Seg> getSegListOfFirstField(Xini segmentizedXini){
		List<Seg> segList = segmentizedXini.getMain().getPage().get(0)
		.getElements().getElement().get(0).getElementContent()
		.getFields().getField().get(0).getSeg();
		return segList;
	}

	private List<Seg> doSegmentation(String xiniSnippet) {
		Xini segmentizedXini = makeSegmentizedXiniFrom(xiniSnippet);
		List<Seg> segsOfFirstField = getSegListOfFirstField(segmentizedXini);
		return segsOfFirstField;
	}

	private List<Seg> doDesegmentation(String xiniSnippet) {
		Xini segmentizedXini = makeDesegmentizedXiniFrom(xiniSnippet);
		List<Seg> segsOfFirstField = getSegListOfFirstField(segmentizedXini);
		return segsOfFirstField;
	}

	private Xini makeSegmentizedXiniFrom(String xiniSnippet) {
		List<Event> before = xiniHelper.toEvents(xiniSnippet);
		List<Event> segmentized = segmentizer.process(before);
		Xini segmentizedXini = xiniHelper.toXini(segmentized);
		return segmentizedXini;
	}

	private Xini makeDesegmentizedXiniFrom(String xiniSnippet) {
		List<Event> events = xiniHelper.toEvents(xiniSnippet);
		Xini segmentizedXini = xiniHelper.toXini(events, filter);
		return segmentizedXini;
	}

	/**
	 * Compares the content of a segment with the expected content.
	 * For that compare, the segment will get serialized into a String.
	 *
	 * @param seg
	 * @param expectedContent
	 */
	private void checkContentWithoutReorderAttributes(Seg seg, String expectedContent){
		String segContent = xiniHelper.serializeTextContent(seg);
		assertEquals(expectedContent, segContent);
	}

	private void checkLeadingSpacer(Seg seg, String expectedLeadSpacer){
		String leadSpacerFirstSeg = seg.getLeadingSpacer();
		assertEquals(expectedLeadSpacer, leadSpacerFirstSeg);
	}

	private void checkTrailingSpacer(Seg seg, String expectedTrailSpacer){
		String trailSpacerFirstSeg = seg.getTrailingSpacer();
		assertEquals(expectedTrailSpacer, trailSpacerFirstSeg);
	}
}