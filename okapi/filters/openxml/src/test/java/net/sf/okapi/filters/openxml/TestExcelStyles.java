package net.sf.okapi.filters.openxml;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class TestExcelStyles {
	private XMLInputFactory inputFactory = XMLInputFactory.newInstance();

	@Test
	public void testRGBColorFills() throws Exception {
		ExcelStyles styles = new ExcelStyles();
		XMLEventReader xmlReader = inputFactory.createXMLEventReader(new InputStreamReader(
					getClass().getResourceAsStream("/xlsx_parts/rgb_styles.xml"), StandardCharsets.UTF_8));
		styles.parse(xmlReader);
		List<ExcelStyles.Fill> expected = new ArrayList<>(); 
		expected.add(new ExcelStyles.EmptyFill());
		expected.add(new ExcelStyles.EmptyFill());
		expected.add(new ExcelStyles.PatternFill("FF800000"));
		expected.add(new ExcelStyles.PatternFill("FFFF0000"));
		expected.add(new ExcelStyles.PatternFill("FFFF6600"));
		expected.add(new ExcelStyles.PatternFill("FFFFFF00"));
		expected.add(new ExcelStyles.PatternFill("FFCCFFCC"));
		expected.add(new ExcelStyles.PatternFill("FF008000"));
		expected.add(new ExcelStyles.PatternFill("FF3366FF"));
		expected.add(new ExcelStyles.PatternFill("FF0000FF"));
		expected.add(new ExcelStyles.PatternFill("FF000090"));
		expected.add(new ExcelStyles.PatternFill("FF660066"));
		assertEquals(expected, styles.getFills());

		List<ExcelStyles.CellStyle> expectedCellStyles = new ArrayList<>();
		expectedCellStyles.add(new ExcelStyles.CellStyle(expected.get(0)));
		expectedCellStyles.add(new ExcelStyles.CellStyle(expected.get(2)));
		expectedCellStyles.add(new ExcelStyles.CellStyle(expected.get(3)));
		expectedCellStyles.add(new ExcelStyles.CellStyle(expected.get(4)));
		expectedCellStyles.add(new ExcelStyles.CellStyle(expected.get(5)));
		expectedCellStyles.add(new ExcelStyles.CellStyle(expected.get(6)));
		expectedCellStyles.add(new ExcelStyles.CellStyle(expected.get(7)));
		expectedCellStyles.add(new ExcelStyles.CellStyle(expected.get(8)));
		expectedCellStyles.add(new ExcelStyles.CellStyle(expected.get(9)));
		expectedCellStyles.add(new ExcelStyles.CellStyle(expected.get(10)));
		expectedCellStyles.add(new ExcelStyles.CellStyle(expected.get(11)));
		assertEquals(expectedCellStyles, styles.getCellStyles());
	}

	@Test
	public void testOptionalFillId() throws Exception {
		ExcelStyles styles = new ExcelStyles();
		XMLEventReader xmlReader = inputFactory.createXMLEventReader(new InputStreamReader(
				getClass().getResourceAsStream("/xlsx_parts/missing_fillId.xml"), StandardCharsets.UTF_8));
		styles.parse(xmlReader);
		List<ExcelStyles.CellStyle> expectedCellStyles = new ArrayList<>();
		expectedCellStyles.add(new ExcelStyles.CellStyle(new ExcelStyles.EmptyFill()));
		expectedCellStyles.add(new ExcelStyles.CellStyle(new ExcelStyles.PatternFill("FF3366FF")));
		expectedCellStyles.add(new ExcelStyles.CellStyle(new ExcelStyles.EmptyFill()));
		assertEquals(expectedCellStyles, styles.getCellStyles());
	}
}
