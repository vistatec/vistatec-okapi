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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import net.sf.okapi.common.exceptions.OkapiBadFilterInputException;
import net.sf.okapi.common.exceptions.OkapiException;

/**
 * Class to parse an individual worksheet and update the shared string
 * data based on worksheet cells and exclusion information.
 */
public class ExcelWorksheet {

	private static final char COLUMN_INDEX_PART_MINIMUM = 'A';
	private static final char COLUMN_INDEX_PART_MAXIMUM = 'Z';

	private static final Pattern cellCoordinatePattern = Pattern.compile("[A-Z]{1,3}(\\d+)");

	private XMLEventFactory eventFactory;
	private SharedStringMap stringTable;
	private Set<String> excludedColumns;
	private Set<Integer> excludedRows;
	private Set<String> excludedColors;
	private List<MergeArea> mergeAreas;
	private ExcelStyles styles;
	private boolean excludeHiddenRowsAndColumns;
	private boolean isSheetHidden;
	private Relationships worksheetRels;
	private Map<String, Boolean> tableVisibilityMap;

	public ExcelWorksheet(XMLEventFactory eventFactory, SharedStringMap stringTable,
						  ExcelStyles styles, Relationships worksheetRels, Map<String, Boolean> tableVisibilityMap,
						  boolean isSheetHidden, Set<String> excludedColumns, Set<String> excludedColors,
						  boolean excludeHiddenRowsAndColumns) {
		this.eventFactory = eventFactory;
		this.stringTable = stringTable;
		this.styles = styles;
		this.worksheetRels = worksheetRels;
		this.tableVisibilityMap = tableVisibilityMap;
		this.isSheetHidden = isSheetHidden;
		this.excludedColumns = new HashSet<>(excludedColumns); // We may need to modify this locally
		this.excludedRows = new HashSet<>();
		this.mergeAreas = new ArrayList<>();
		this.excludedColors = excludedColors;
		this.excludeHiddenRowsAndColumns = excludeHiddenRowsAndColumns;
	}

	static final QName ROW = Namespaces.SpreadsheetML.getQName("row");
	static final QName COL = Namespaces.SpreadsheetML.getQName("col");
	static final QName CELL = Namespaces.SpreadsheetML.getQName("c");
	static final QName VALUE = Namespaces.SpreadsheetML.getQName("v");
	static final QName TABLE = Namespaces.SpreadsheetML.getQName("tablePart");
	static final QName MERGE_CELL = Namespaces.SpreadsheetML.getQName("mergeCell");
	static final QName CELL_LOCATION = new QName("r");
	static final QName ROW_NUMBER = CELL_LOCATION;
	static final QName CELL_TYPE = new QName("t");
	static final QName CELL_STYLE = new QName("s");
	static final QName HIDDEN = new QName("hidden");
	static final QName MIN = new QName("min");
	static final QName MAX = new QName("max");
	static final QName REF = new QName("ref");

	void parse(XMLEventReader reader, XMLEventWriter writer) throws IOException, XMLStreamException {
		boolean excluded = false;
		boolean inValue = false;
		boolean isSharedString = false;
		XmlEventCollector collector = collectMergeHiddenData(reader);
		Iterator<XMLEvent> iterator = collector.getEvents().iterator();

		while (iterator.hasNext()) {
			XMLEvent e = iterator.next();
			if (e.isStartElement()) {
				StartElement el = e.asStartElement();
				if (el.getName().equals(CELL)) {
					// We only care about cells with @t="s", indicating a shared string
					Attribute typeAttr = el.getAttributeByName(CELL_TYPE);
					if (typeAttr != null && typeAttr.getValue().equals("s")) {
						excluded = isCellHidden(el.getAttributeByName(CELL_LOCATION).getValue());
						isSharedString = true;
					}
					Attribute styleAttr = el.getAttributeByName(CELL_STYLE);
					if (styleAttr != null) {
						int styleIndex = Integer.parseInt(styleAttr.getValue());
						ExcelStyles.CellStyle style = styles.getCellStyle(styleIndex);
						// I'm going to start with a naive implementation that should
						// basically be fine, but not ideal if we're excluding large numbers
						// of colors.
						for (String excludedColor : excludedColors) {
							if (style.fill.matchesColor(excludedColor)) {
								excluded = true;
								break;
							}
						}
					}
				}
				else if (el.getName().equals(VALUE)) {
					inValue = true;
				}
				else if (el.getName().equals(TABLE)) {
					String relId = XMLEventHelpers.getAttributeValue(el, Relationships.ATTR_REL_ID);
					Relationships.Rel tableRel = worksheetRels.getRelById(relId);
					tableVisibilityMap.put(tableRel.target, !isSheetHidden);
				}
			}
			else if (e.isEndElement()) {
				EndElement el = e.asEndElement();
				if (el.getName().equals(CELL)) {
					excluded = false;
					isSharedString = false;
				}
				else if (el.getName().equals(VALUE)) {
					inValue = false;
				}
			}
			else if (e.isCharacters() && inValue && isSharedString) {
				int origIndex = getSharedStringIndex(e.asCharacters().getData());
				int newIndex = stringTable.createEntryForString(origIndex, excluded).getNewIndex();
				// Replace the event with one that contains the new index
				e = eventFactory.createCharacters(String.valueOf(newIndex));
			}
			writer.add(e);
		}
	}


	private XmlEventCollector collectMergeHiddenData(XMLEventReader xmlEventReader) throws XMLStreamException {
		XmlEventCollector collector = new XmlEventCollector();
		while (xmlEventReader.hasNext()) {
			XMLEvent e = xmlEventReader.nextEvent();
			collector.addEvent(e);

			if (!e.isStartElement()) {
				continue;
			}
			StartElement el = e.asStartElement();
			if (e.isStartElement() && e.asStartElement().getName().equals(MERGE_CELL)) {
				mergeAreas.add(new MergeArea(e.asStartElement().getAttributeByName(REF).getValue()));
			} else if (el.getName().equals(ROW)) {
				if (isHidden(el)) {
					Integer numberOfHiddenRow = Integer.parseInt(el.getAttributeByName(ROW_NUMBER).getValue());
					excludedRows.add(numberOfHiddenRow);
				}
			} else if (el.getName().equals(COL)) {
				if (isHidden(el)) {
					// Column info blocks span one or more columns, which are referred to
					// via 1-indexed min/max values.
					excludedColumns.addAll(extractColumnNames(el));
				}
			}
		}
		return collector;
	}

	private boolean isHidden(StartElement el) {
		return excludeHiddenRowsAndColumns &&
				(isSheetHidden || parseOptionalBooleanAttribute(el, HIDDEN, false));
	}

	/**
	 * Check for an attribute that conforms to the XML Schema boolean datatype.  If it is present
	 * (and the value conforms), return the value.  If it is not present, or the value is
	 * non-conforming, return the specified default value.
	 * @param el
	 * @param attrName
	 * @param defaultValue
	 * @return
	 */
	private boolean parseOptionalBooleanAttribute(StartElement el, QName attrName, boolean defaultValue) {
		Attribute a = el.getAttributeByName(attrName);
		if (a == null) return defaultValue;
		String v = a.getValue();
		if ("true".equals(v) || "1".equals(v)) return true;
		if ("false".equals(v) || "0".equals(v)) return false;
		return defaultValue;
	}

	/**
	 * Convert the min and max attributes of a &lt;col&gt; element into a list
	 * of column names.  For example, "min=2; max=2" => [ "B" ].
	 * @param el
	 * @return
	 */
	List<String> extractColumnNames(StartElement el) {
		try {
			List<String> names = new ArrayList<>();
			int min = Integer.parseInt(el.getAttributeByName(MIN).getValue());
			int max = Integer.parseInt(el.getAttributeByName(MAX).getValue());
			for (int i = min; i <= max; i++) {
				names.add(indexToColumnName(i));
			}
			return names;
		}
		catch (NumberFormatException | NullPointerException e) {
			throw new OkapiBadFilterInputException("Invalid <col> element", e);
		}
	}

	static String indexToColumnName(int index) {
		StringBuilder sb = new StringBuilder();

	    while (index > 0) {
	        int modulo = (index - 1) % 26;
	        sb.insert(0, (char)(65 + modulo));
	        index = (index - modulo) / 26;
	    }

	    return sb.toString();
	}

	private static int getSharedStringIndex(String value) {
		try {
			return Integer.parseInt(value);
		}
		catch (NumberFormatException e) {
			throw new IllegalStateException("Unexpected shared string index '" + value + "'");
		}
	}
	private static String getColumn(String location) {
		char buf[] = location.toCharArray();
		for (int i = 0; i < buf.length; i++) {
			if (Character.isDigit(buf[i])) {
				return location.substring(0, i);
			}
		}
		// I don't think this should never happen, so fail fast
		throw new IllegalStateException("Unexpected worksheet cell location '" + location + "'");
	}

	private String getRow(String location) {
		Matcher matcher = cellCoordinatePattern.matcher(location);
		matcher.find();
		return matcher.group(1);
	}

	private boolean isCellHidden(String location) {
		String currentColumn = getColumn(location);
		String currentRow = getRow(location);

		boolean excluded = excludedColumns.contains(currentColumn) || excludedRows.contains(Integer.parseInt(currentRow));

		if (!excluded) {
			return false;
		}

		MergeArea mergedArea = getMergedArea(currentRow, currentColumn);
		if (mergedArea == null) {
			return true;
		}

		Intersection intersection = getIntersectionWithHiddenArea(mergedArea);

		return Intersection.PARTIAL != intersection;
	}

	private Intersection getIntersectionWithHiddenArea(MergeArea mergedArea) {
		List<String> columnsRange = getColumnsRange(mergedArea.getLeftColumn(), mergedArea.getRightColumn());
		Iterator<String> columnIterator = columnsRange.iterator();

		int mergedColumnNumber = 0;
		int intersectedWithHiddenColumns = 0;

		while(columnIterator.hasNext()) {
			mergedColumnNumber++;
			if (excludedColumns.contains(columnIterator.next())) {
				intersectedWithHiddenColumns++;
			}
		}
		if (mergedColumnNumber == intersectedWithHiddenColumns) {
			return Intersection.FULL;
		}

		int mergedRowNumber = 0;
		int intersectedWithHiddenRows = 0;


		int topRow = Integer.parseInt(mergedArea.getTopRow());
		int bottomRow = Integer.parseInt(mergedArea.getBottomRow());
		for(int i = topRow; i <= bottomRow; i++) {
			mergedRowNumber++;
			if (excludedRows.contains(i)) {
				intersectedWithHiddenRows++;
			}
		}

		if (mergedRowNumber == intersectedWithHiddenRows) {
			return Intersection.FULL;
		}

		if (mergedColumnNumber > intersectedWithHiddenColumns
				&& mergedRowNumber > intersectedWithHiddenRows) {
			return Intersection.PARTIAL;
		}

		if (mergedColumnNumber != 0 && intersectedWithHiddenColumns == 0
				&& mergedRowNumber != 0 && intersectedWithHiddenRows == 0) {
			return Intersection.NONE;
		}

		throw new OkapiException("The merge area has a wrong configuration");
	}

	private List<String> getColumnsRange(String startColumnIndex, String endColumnIndex) {
		List<String> columns = new ArrayList<>();
		String columnIndex = startColumnIndex;

		columns.add(columnIndex);

		while (!columnIndex.equals(endColumnIndex)) {
			columnIndex = incrementColumnIndex(columnIndex);
			columns.add(columnIndex);
		}

		return columns;
	}

	private String incrementColumnIndex(String columnIndex) {
		return incrementColumnIndexPart(columnIndex.toCharArray(), columnIndex.length() - 1);
	}

	private String incrementColumnIndexPart(char[] columnIndexParts, int partPosition) {
		if (0 > partPosition) {
			return COLUMN_INDEX_PART_MINIMUM + new String(columnIndexParts);
		}

		char part = columnIndexParts[partPosition];

		if (COLUMN_INDEX_PART_MAXIMUM == part) {
			columnIndexParts[partPosition] = COLUMN_INDEX_PART_MINIMUM;

			return incrementColumnIndexPart(columnIndexParts, --partPosition);
		}

		columnIndexParts[partPosition] = ++part;

		return new String(columnIndexParts);
	}

	private MergeArea getMergedArea(String currentRow, String currentColumn) {
		for(MergeArea area: mergeAreas) {
			if (compareColumns(area.getLeftColumn(), currentColumn) <= 0
					&& compareColumns(currentColumn, area.getRightColumn()) <= 0
					&& Integer.parseInt(area.getTopRow()) <= Integer.parseInt(currentRow)
					&& Integer.parseInt(currentRow) <= Integer.parseInt(area.getBottomRow())) {
				return area;
			}
		}
		return null;
	}

	private int compareColumns(String column1, String column2) {
		if (column1.compareTo(column2) == 0) {
			return 0;
		} else if (column1.length() < column2.length()) {
			return -1;
		} else if (column1.length() == column2.length()) {
			return column1.compareTo(column2);
		} else if (column1.length() > column2.length()) {
			return 1;
		}
		throw new OkapiException("Matching columns have a wrong format");
	}

	private enum Intersection {
		FULL,
		PARTIAL,
		NONE
	};

	static class XmlEventCollector implements XMLEvents {
		private List<XMLEvent> xmlEvents;

		XmlEventCollector() {
			xmlEvents = new ArrayList<>();
		}

		@Override
		public List<XMLEvent> getEvents() {
			return xmlEvents;
		}

		public void addEvent(XMLEvent event) {
			xmlEvents.add(event);
		}
	}

	private class MergeArea {
		private String leftColumn;
		private String rightColumn;
		private String topRow;
		private String bottomRow;

		MergeArea(String area) {
			String cornerCoordinates[] = area.split(":");

			if (cornerCoordinates.length != 2) {
				return;
			}

			topRow = getRow(cornerCoordinates[0]);
			leftColumn = getColumn(cornerCoordinates[0]);

			bottomRow = getRow(cornerCoordinates[1]);
			rightColumn = getColumn(cornerCoordinates[1]);
		}

		public String getLeftColumn() {
			return leftColumn;
		}

		public String getRightColumn() {
			return rightColumn;
		}

		public String getTopRow() {
			return topRow;
		}

		public String getBottomRow() {
			return bottomRow;
		}
	}
}
