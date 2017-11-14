package net.sf.okapi.filters.xini;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.xml.bind.JAXBException;

import net.sf.okapi.filters.xini.jaxb.Element;
import net.sf.okapi.filters.xini.jaxb.Field;
import net.sf.okapi.filters.xini.jaxb.INITD;
import net.sf.okapi.filters.xini.jaxb.INITR;
import net.sf.okapi.filters.xini.jaxb.Page;
import net.sf.okapi.filters.xini.jaxb.Seg;
import net.sf.okapi.filters.xini.jaxb.TD;
import net.sf.okapi.filters.xini.jaxb.TR;
import net.sf.okapi.filters.xini.jaxb.TargetLanguages;
import net.sf.okapi.filters.xini.jaxb.Xini;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class XINIFilterMetainformationTest {

	private static XINIFilterTestHelper helper;

	@BeforeClass
	public static void initialize(){
		helper = new XINIFilterTestHelper();
	}

	@Test
	public void sourceAndTargetLanguagesPreserved() throws JAXBException {
		String snippet = "<?xml version=\"1.0\" ?>" +
		"<Xini SchemaVersion=\"1.0\" SourceLanguage=\"de\" xsi:noNamespaceSchemaLocation=\"http://www.ontram.com/xsd/xini.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
		"	<TargetLanguages>" +
		"		<Language>en</Language>" +
		"		<Language>en-US</Language>" +
		"	</TargetLanguages>" +
		"	<Main>" +
		"	</Main>" +
		"</Xini>";

		Xini xini = helper.toXini(helper.toEvents(snippet));

		assertEquals("1.0", xini.getSchemaVersion());
		assertEquals("de", xini.getSourceLanguage());

		TargetLanguages targetLangs = xini.getTargetLanguages();
		assertEquals(2,targetLangs.getLanguage().size());
		assertEquals("en", targetLangs.getLanguage().get(0));
		assertEquals("en-US", targetLangs.getLanguage().get(1));
	}

	@Test
	public void emptyPageDoesntCauseNullPointerException() throws JAXBException {
		String snippet =
			helper.getStartSnippetXiniMain() +
		"		<Page PageID=\"1\">" +
		"			<PageName>Page Title</PageName>" +
		"		</Page>" +
		"	</Main>" +
		"</Xini>";

		Xini xini = helper.toXini(helper.toEvents(snippet));

		Page page = xini.getMain().getPage().get(0);
		assertEquals(1, page.getPageID());
		assertEquals("Page Title", page.getPageName());

		assertEquals(null, page.getElements());
	}

	@Test
	public void emptyFieldDoesntCauseNullPointerException() throws JAXBException {
		String snippet =
			helper.getStartSnippetXiniMain() +
		"		<Page PageID=\"1\">" +
		"			<Elements>" +
		"				<Element ElementID=\"10\">" +
		"					<ElementContent>" +
		"						<Fields>" +
		"							<Field FieldID=\"0\">" +
		"							</Field>" +
		"						</Fields>" +
		"					</ElementContent>" +
		"				</Element>" +
		"			</Elements>" +
		"		</Page>" +
		"	</Main>" +
		"</Xini>";

		Xini xini = helper.toXini(helper.toEvents(snippet));

		Page page = xini.getMain().getPage().get(0);
		
		Field field = page.getElements().getElement().get(0).getElementContent().getFields().getField().get(0);

		assertEquals(0, field.getSegAndTrans().size());
	}

	@Test
	public void pageAndElementIsPreserved() throws JAXBException {
		String snippet =
			helper.getStartSnippetXiniMain() +
		"		<Page PageID=\"1\">" +
		"			<PageName>Page Title</PageName>" +
		"			<Elements>" +
		"				<Element ElementID=\"10\" Size=\"50\" CustomerTextID=\"123Test\" ElementType=\"r/o text\" AlphaList=\"false\"" +
		"						RawSourceBeforeElement=\"&lt;before/&gt;\" RawSourceAfterElement=\"&lt;after/&gt;\">" +
		"					<Style>Headline</Style>" +
		"					<Label>h1</Label>" +
		"					<ElementContent>" +
		"						<Fields>" +
		"							<Field FieldID=\"0\">" +
		"								<Seg SegID=\"0\">Segment</Seg>" +
		"							</Field>" +
		"						</Fields>" +
		"					</ElementContent>" +
		"				</Element>" +
		"			</Elements>" +
		"		</Page>" +
		"	</Main>" +
		"</Xini>";

		Xini xini = helper.toXini(helper.toEvents(snippet));

		Page page = xini.getMain().getPage().get(0);
		assertEquals(1, page.getPageID());
		assertEquals("Page Title", page.getPageName());

		Element elem = page.getElements().getElement().get(0);
		assertEquals(10, elem.getElementID());
		assertEquals((Integer)50, elem.getSize());
		assertEquals("Headline", elem.getStyle());
		assertEquals("h1", elem.getLabel());

		assertEquals("123Test",elem.getCustomerTextID());
		assertEquals("<before/>",elem.getRawSourceBeforeElement());
		assertEquals("<after/>",elem.getRawSourceAfterElement());
		assertEquals(false, elem.isAlphaList());
		assertEquals("r/o text",elem.getElementType().value());
	}

	@Test
	public void fieldIsPreserved() throws JAXBException {
		String snippet = helper.getStartSnippet() +
				"	<Fields>" +
				"		<Field FieldID=\"0\" NoContent=\"true\"/>" +
				"		<Field FieldID=\"1\" 	Label=\"Footnote\" CustomerTextID=\"321Test\" " +
				"								ExternalID=\"54321\" EmptySegmentsFlags=\"0\" " +
				"								RawSourceBeforeField=\"&lt;before/&gt;\" RawSourceAfterField=\"&lt;after/&gt;\">" +
				"			<Seg SegID=\"0\">Segment</Seg>" +
				"		</Field>" +
				"	</Fields>" +
				helper.getEndSnippet();

		Xini xini = helper.toXini(helper.toEvents(snippet));
		Page page = xini.getMain().getPage().get(0);
		Element elem = page.getElements().getElement().get(0);

		Field field = elem.getElementContent().getFields().getField().get(0);
		assertEquals(0, field.getFieldID());
		assertEquals(true, field.isNoContent());

		field = elem.getElementContent().getFields().getField().get(1);
		assertEquals("321Test",field.getCustomerTextID());
		assertEquals("Footnote", field.getLabel());
		assertEquals("54321", field.getExternalID());
		assertEquals("0", field.getEmptySegmentsFlags());
		assertEquals("<before/>",field.getRawSourceBeforeField());
		assertEquals("<after/>",field.getRawSourceAfterField());
	}

	@Test
	public void tableIsPreserved() throws JAXBException {
		String snippet = helper.getStartSnippet() +
							"	<Table>" +
							"		<TR>" +
							"			<TD ExternalID=\"EX-ID-123\" EmptySegmentsFlags=\"0\">" +
							"				<Seg SegID=\"0\">table cell 1</Seg>" +
							"			</TD>" +
							"			<TD Label=\"Test-Label\" CustomerTextID=\"CTiD-123\">" +
							"				<Seg SegID=\"0\">table cell 2</Seg>" +
							"			</TD>" +
							"		</TR>" +
							"		<TR>" +
							"			<TD NoContent=\"true\"/>" +
							"		</TR>" +
							"	</Table>" +
							helper.getEndSnippet();

		Xini xini = helper.toXini(helper.toEvents(snippet));

		List<TR> trsTable = helper.getTableRowsByPageIdAndElementId(xini, 1, 10);
		assertEquals(2, trsTable.size());

		TD tD = trsTable.get(0).getTD().get(0);
		assertEquals("EX-ID-123", tD.getExternalID());
		assertEquals("0", tD.getEmptySegmentsFlags());

		tD = trsTable.get(0).getTD().get(1);
		assertEquals("CTiD-123", tD.getCustomerTextID());
		assertEquals("Test-Label", tD.getLabel());

		tD = trsTable.get(1).getTD().get(0);
		assertTrue(tD.isNoContent());
	}

	@Test
	public void iniTableIsPreserved() throws JAXBException {
		String snippet = helper.getStartSnippet() +
							"	<INI_Table>" +
							"		<TR>" +
							"			<TD Label=\"Test-Label\" CustomerTextID=\"CTiD-123\">" +
							"				<Seg SegID=\"0\">Seg0</Seg>" +
							"			</TD>" +
							"			<TD ExternalID=\"EX-ID-123\" EmptySegmentsFlags=\"00\" >" +
							"				<Seg SegID=\"0\">Seg1</Seg>" +
							"				<Seg SegID=\"1\">Seg2</Seg>" +
							"			</TD>" +
							"		</TR>" +
							"		<TR>" +
							"			<TD NoContent=\"true\"/>" +
							"			<TD NoContent=\"true\"/>" +
							"		</TR>" +
							"	</INI_Table>" +
							helper.getEndSnippet();

		Xini xini = helper.toXini(helper.toEvents(snippet));

		List<INITR> trsINITable = helper.getINITableRowsByPageIdAndElementId(xini, 1, 10);
		assertEquals(2, trsINITable.size());

		String segContentINITable = helper.getSegContentBySegId(trsINITable.get(0).getTD().get(0), 0);
		assertEquals("Seg0", segContentINITable);
		INITD iniTd = trsINITable.get(0).getTD().get(0);
		assertEquals("CTiD-123", iniTd.getCustomerTextID());
		assertEquals("Test-Label", iniTd.getLabel());

		segContentINITable = helper.getSegContentBySegId(trsINITable.get(0).getTD().get(1), 0);
		assertEquals("Seg1", segContentINITable);
		segContentINITable = helper.getSegContentBySegId(trsINITable.get(0).getTD().get(1), 1);
		assertEquals("Seg2", segContentINITable);

		iniTd = trsINITable.get(0).getTD().get(1);
		assertEquals("EX-ID-123", iniTd.getExternalID());
		assertEquals("00", iniTd.getEmptySegmentsFlags());

		iniTd = trsINITable.get(1).getTD().get(0);
		assertTrue(iniTd.isNoContent());
		iniTd = trsINITable.get(1).getTD().get(1);
		assertTrue(iniTd.isNoContent());
	}

	@Test
	public void segmentIsPreserved() throws JAXBException {
		String snippet = helper.getStartSnippet() +
							"	<Table>" +
							"		<TR>" +
							"			<TD>" +
							"				<Seg SegID=\"0\" EmptyTranslation=\"true\"/>" +
							"			</TD>" +
							"		</TR>" +
							" </Table>" +
							helper.getEndSnippet();

		Xini xini = helper.toXini(helper.toEvents(snippet));

		List<TR> trsTable = helper.getTableRowsByPageIdAndElementId(xini, 1, 10);
		assertEquals(1, trsTable.size());

		Seg seg = helper.getSegBySegId(trsTable.get(0).getTD().get(0), 0);
		assertTrue(seg.isEmptyTranslation());
		String segContent = helper.getSegContentBySegId(trsTable.get(0).getTD().get(0), 0);
		assertEquals("", segContent);
	}
}
