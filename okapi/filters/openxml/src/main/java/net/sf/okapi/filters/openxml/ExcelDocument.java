/*===========================================================================
  Copyright (C) 2016-2017 by the Okapi Framework contributors
-----------------------------------------------------------------------------
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
===========================================================================*/

package net.sf.okapi.filters.openxml;

import net.sf.okapi.common.exceptions.OkapiBadFilterInputException;
import net.sf.okapi.filters.openxml.ContentTypes.Types.Common;
import net.sf.okapi.filters.openxml.ContentTypes.Types.Drawing;
import net.sf.okapi.filters.openxml.ContentTypes.Types.Excel;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;

import static net.sf.okapi.filters.openxml.ParseType.MSEXCEL;
import static net.sf.okapi.filters.openxml.ParseType.MSEXCELCOMMENT;
import static net.sf.okapi.filters.openxml.ParseType.MSWORDDOCPROPERTIES;

class ExcelDocument extends DocumentType {

	private static final String COMMENTS_REL =
			Namespaces.DocumentRelationships.getDerivedURI("/comments");

	private static final String DRAWINGS_REL =
			Namespaces.DocumentRelationships.getDerivedURI("/drawing");

	private static final String CHART_REL =
			Namespaces.DocumentRelationships.getDerivedURI("/chart");

	private static final String DIAGRAM_DATA_REL =
			Namespaces.DocumentRelationships.getDerivedURI("/diagramData");

	private final Map<String, String> sharedStrings;

	private SharedStringMap sharedStringMap = new SharedStringMap();

	private List<String> worksheetEntryNames = null;

	private ExcelStyles styles;

	private Relationships workbookRels;

	private Map<String, ExcelWorkbook.Sheet> worksheets = new HashMap<>();

	private Map<String, Boolean> tableVisibility = new HashMap<>();

	private static final String SHARED_STRING_TABLE_REL =
			Namespaces.DocumentRelationships.getDerivedURI("/sharedStrings");

	private static final String STYLES_REL =
			Namespaces.DocumentRelationships.getDerivedURI("/styles");

	private Map<String, String> sheetsByComment = new HashMap<>();

	private Map<String, String> sheetsByDrawing = new HashMap<>();

	private Map<String, String> drawingsByChart = new HashMap<>();

	private Map<String, String> drawingsByDiagramData = new HashMap<>();


	ExcelDocument(OpenXMLZipFile zipFile, ConditionalParameters params, Map<String, String> sharedStrings) {
		super(zipFile, params);
		this.sharedStrings = sharedStrings;
	}

	@Override
	boolean isClarifiablePart(String contentType) {
		return Excel.STYLES_TYPE.equals(contentType)
				|| Excel.WORKSHEET_TYPE.equals(contentType);
	}

	@Override
	boolean isStyledTextPart(String entryName, String type) {
	    switch (type) {
            case Excel.SHARED_STRINGS_TYPE:
            case Drawing.CHART_TYPE:
            case Drawing.DIAGRAM_TYPE:
            case Excel.DRAWINGS_TYPE:
			    return true;
            default:
                return false;
		}
	}

	@Override
	void initialize() throws IOException, XMLStreamException {
		String mainDocumentPart = getZipFile().getMainDocumentTarget();
		workbookRels = getZipFile().getRelationshipsForTarget(mainDocumentPart);
		worksheetEntryNames = findWorksheets();
		styles = parseStyles();

		sheetsByComment = findComments(worksheetEntryNames);
		sheetsByDrawing = findDrawings(worksheetEntryNames);
		drawingsByChart = findCharts(sheetsByDrawing.keySet());
		drawingsByDiagramData = findDiagramData(sheetsByDrawing.keySet());
	}

	@Override
	OpenXMLPartHandler getHandlerForFile(ZipEntry entry, String contentType) {
		if (isPartHidden(entry.getName(), contentType)) {
			return new NonTranslatablePartHandler(getZipFile(), entry);
		}

		// find content handler based on content type
		if (!isTranslatablePart(entry.getName(), contentType)) {
			if (contentType.equals(Excel.WORKSHEET_TYPE)) {
				// Check to see if it's visible
				return new ExcelWorksheetPartHandler(getZipFile(), entry, sharedStringMap, styles, tableVisibility,
						findWorksheetNumber(entry.getName()), getParams(), isSheetHidden(entry.getName()));
			}
			else if (isClarifiablePart(contentType)) {
				return new ClarifiablePartHandler(getZipFile(), entry);
			}
			return new NonTranslatablePartHandler(getZipFile(), entry);
		}

		switch (contentType) {
			case Excel.SHARED_STRINGS_TYPE:
				return new SharedStringsPartHandler(getParams(), getZipFile(), entry, new EmptyStyleDefinitions(), sharedStringMap);
			case Excel.DRAWINGS_TYPE:
			case Drawing.CHART_TYPE:
			case Drawing.DIAGRAM_TYPE:
				return new StyledTextPartHandler(getParams(), getZipFile(), entry, new EmptyStyleDefinitions());
			default:
				break;
		}

		// find content handler based on parseType
		ParseType parseType = null;
		switch (contentType) {
			case Excel.COMMENT_TYPE:
				parseType = ParseType.MSEXCELCOMMENT;
				break;
			case Common.CORE_PROPERTIES_TYPE:
				parseType = MSWORDDOCPROPERTIES;
				break;
			case Excel.MAIN_DOCUMENT_TYPE:
				parseType = MSEXCEL;
				break;
		}

		if (MSWORDDOCPROPERTIES.equals(parseType) || MSEXCELCOMMENT.equals(parseType) || MSEXCEL.equals(parseType)) {
			OpenXMLContentFilter openXMLContentFilter = new OpenXMLContentFilter(getParams(), entry.getName());
			openXMLContentFilter.setUpConfig(parseType);
			return new StandardPartHandler(openXMLContentFilter, getParams(), getZipFile(), entry);
		}

		return new ExcelFormulaPartHandler(getParams(), getZipFile(), entry, sharedStrings);
	}

	private boolean isSheetHidden(String entryName) {
		ExcelWorkbook.Sheet sheet = worksheets.get(entryName);

		return sheet != null && !sheet.visible;
	}

	private boolean isTranslatablePart(String entryName, String contentType) {
		if (Excel.TABLE_TYPE.equals(contentType)) {
			Boolean b = tableVisibility.get(entryName);
			// There should always be a value, but default to hiding tables we don't know about
			return (b != null) ? b : false;
		}
		if (!entryName.endsWith(".xml")) {
			return false;
		}
		switch (contentType) {
			case Excel.SHARED_STRINGS_TYPE:
			case Drawing.CHART_TYPE:
				return true;
			case Excel.MAIN_DOCUMENT_TYPE:
			case Excel.MACRO_ENABLED_MAIN_DOCUMENT_TYPE:
				return getParams().getTranslateExcelSheetNames();
			case Common.CORE_PROPERTIES_TYPE:
				return getParams().getTranslateDocProperties();
			case Excel.COMMENT_TYPE:
				return getParams().getTranslateComments();
			case Excel.DRAWINGS_TYPE:
				return getParams().getTranslateExcelDrawings();
			case Drawing.DIAGRAM_TYPE:
				return getParams().getTranslateExcelDiagramData();
			default:
				return false;
		}
	}

	/**
	 * Do additional reordering of the entries for XLSX files to make
	 * sure that worksheets are parsed in order, followed by the shared
	 * strings table.
	 * @return the sorted enum of ZipEntry
	 * @throws IOException if any error is encountered while reading the stream
	 * @throws XMLStreamException if any error is encountered while parsing the XML
	 */
	@Override
	Enumeration<? extends ZipEntry> getZipFileEntries() throws IOException, XMLStreamException {
		Enumeration<? extends ZipEntry> entries = getZipFile().entries();
		List<? extends ZipEntry> entryList = Collections.list(entries);
		List<String> worksheetsAndSharedStrings = new ArrayList<>();
		worksheetsAndSharedStrings.addAll(worksheetEntryNames);
		worksheetsAndSharedStrings.add(findSharedStrings());
		Collections.sort(entryList, new ZipEntryComparator(worksheetsAndSharedStrings));
		return Collections.enumeration(entryList);
	}

	ExcelWorkbook parseWorkbook(String partName) throws IOException, XMLStreamException {
		XMLEventReader r = getZipFile().getInputFactory().createXMLEventReader(getZipFile().getPartReader(partName));
		return ExcelWorkbook.parseFrom(r, getParams());
	}

	ExcelStyles parseStyles() throws IOException, XMLStreamException {
		Relationships.Rel stylesRel = workbookRels.getRelByType(STYLES_REL).get(0);
		ExcelStyles styles = new ExcelStyles();
		styles.parse(getZipFile().getInputFactory().createXMLEventReader(
					 getZipFile().getPartReader(stylesRel.target)));
		return styles;
	}

	/**
	 * Examine relationship information to find all worksheets in the package.
	 * Return a list of their entry names, in order.
	 * @return list of entry names.
	 * @throws XMLStreamException
	 * @throws IOException
	 */
	List<String> findWorksheets() throws IOException, XMLStreamException {
		List<String> worksheetNames = new ArrayList<>();
		ExcelWorkbook workbook = parseWorkbook(getZipFile().getMainDocumentTarget());

		List<ExcelWorkbook.Sheet> sheets = workbook.getSheets();
		for (ExcelWorkbook.Sheet sheet : sheets) {
			Relationships.Rel sheetRel = workbookRels.getRelById(sheet.relId);
			worksheetNames.add(sheetRel.target);
			worksheets.put(sheetRel.target, sheet);
		}
		return worksheetNames;
	}

	/**
	 * Parse relationship information to find the shared strings table.  Return
	 * its entry name.
	 * @throws IOException
	 * @throws XMLStreamException
	 */
	String findSharedStrings() throws IOException, XMLStreamException {
		String mainDocumentPart = getZipFile().getMainDocumentTarget();
		Relationships rels = getZipFile().getRelationshipsForTarget(mainDocumentPart);
		List<Relationships.Rel> r = rels.getRelByType(SHARED_STRING_TABLE_REL);
		if (r == null || r.size() != 1) {
			throw new OkapiBadFilterInputException(UNEXPECTED_NUMBER_OF_RELATIONSHIPS);
		}
		return r.get(0).target;
	}

	private int findWorksheetNumber(String worksheetEntryName) {
		for (int i = 0; i < worksheetEntryNames.size(); i++) {
			if (worksheetEntryName.equals(worksheetEntryNames.get(i))) {
				return i + 1; // 1-indexed
			}
		}
		throw new IllegalStateException("No worksheet entry with name " +
						worksheetEntryName + " in " + worksheetEntryNames);
	}

	private boolean isPartHidden(String entryName, String contentType) {
	    switch (contentType) {
	        case Excel.COMMENT_TYPE:
                return isCommentHidden(entryName);
	        case Excel.DRAWINGS_TYPE:
                return isDrawingHidden(entryName);
	        case Drawing.CHART_TYPE:
                return isChartHidden(entryName);
	        case Drawing.DIAGRAM_TYPE:
                return isDiagramDataHidden(entryName);
            default:
                return false;
        }
	}

	private boolean isCommentHidden(String entryName) {
		if (!sheetsByComment.containsKey(entryName)) {
			return false;
		}

		String sheetEntryName = sheetsByComment.get(entryName);
		return isSheetHidden(sheetEntryName);
	}

	private boolean isDrawingHidden(String entryName) {
		if (!sheetsByDrawing.containsKey(entryName)) {
			return false;
		}

		String sheetEntryName = sheetsByDrawing.get(entryName);
		return isSheetHidden(sheetEntryName);
	}

	private boolean isChartHidden(String entryName) {
		if (!drawingsByChart.containsKey(entryName)) {
			return false;
		}

		String drawingEntryName = drawingsByChart.get(entryName);
		return isDrawingHidden(drawingEntryName);
	}

	private boolean isDiagramDataHidden(String entryName) {
		if (!drawingsByDiagramData.containsKey(entryName)) {
			return false;
		}

		String drawingEntryName = drawingsByDiagramData.get(entryName);
		return isDrawingHidden(drawingEntryName);
	}

	private Map<String, String> findComments(List<String> sheetEntryNames)
			throws IOException, XMLStreamException {
		return initializeRelsByEntry(sheetEntryNames, COMMENTS_REL);
	}

	private Map<String, String> findDrawings(List<String> sheetEntryNames)
			throws IOException, XMLStreamException {
		return initializeRelsByEntry(sheetEntryNames, DRAWINGS_REL);
	}

	private Map<String, String> findCharts(Set<String> drawingEntryNames)
			throws IOException, XMLStreamException {
		return initializeRelsByEntry(new ArrayList<>(drawingEntryNames), CHART_REL);
	}

	private Map<String, String> findDiagramData(Set<String> drawingEntryNames)
			throws IOException, XMLStreamException {
		return initializeRelsByEntry(new ArrayList<>(drawingEntryNames), DIAGRAM_DATA_REL);
	}

	/**
	 * Initializes the relationships of the given type.
	 */
	private Map<String, String> initializeRelsByEntry(List<String> entryNames, String relType)
			throws IOException, XMLStreamException {
		Map<String, String> result = new HashMap<>();
		OpenXMLZipFile zipFile = getZipFile();
		for (String entryName : entryNames) {
			List<Relationships.Rel> rels =
					zipFile.getRelationshipsForTarget(entryName).getRelByType(relType);
			if (rels != null && !rels.isEmpty()) {
				for (Relationships.Rel rel : rels) {
					result.put(rel.target, entryName);
				}
			}
		}
		return result;
	}
}
