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
import java.util.List;
import java.util.Objects;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import net.sf.okapi.common.exceptions.OkapiBadFilterInputException;

/**
 * Currently unsupported:
 * - theme colors
 * - indexed colors (legacy only)
 */
public class ExcelStyles {
	private List<Fill> fills = new ArrayList<>();
	private List<CellStyle> cellStyles = new ArrayList<>();

	public interface Fill {
		boolean matchesColor(String argbColor);
	}

	public static class CellStyle {
		Fill fill;
		CellStyle(Fill fill) {
			this.fill = fill;
		}
		@Override
		public String toString() {
			return "CellStyle(" + fill + ")";
		}
		@Override
		public boolean equals(Object o) {
			if (o == this) return true;
			if (o == null || !(o instanceof CellStyle)) return false;
			return Objects.equals(fill,  ((CellStyle)o).fill);
		}
		@Override
		public int hashCode() {
			return Objects.hash(fill);
		}
	}

	public List<Fill> getFills() {
		return fills;
	}

	public List<CellStyle> getCellStyles() {
		return cellStyles;
	}

	public CellStyle getCellStyle(int styleIndex) {
		if (styleIndex < 0 || styleIndex >= cellStyles.size()) {
			throw new IllegalArgumentException("Invalid style index: " + styleIndex);
		}
		return cellStyles.get(styleIndex);
	}

	// fills
	//	 /fill
	//		/patternFill
	//			/fgColor [@rgb]
	static final QName FILL = Namespaces.SpreadsheetML.getQName("fill");
	static final QName FGCOLOR = Namespaces.SpreadsheetML.getQName("fgColor");
	static final QName RGB_ATTR = new QName("rgb");
	static final QName CELLXFS = Namespaces.SpreadsheetML.getQName("cellXfs");
	static final QName XF = Namespaces.SpreadsheetML.getQName("xf");
	static final QName FILLID_ATTR = new QName("fillId");
	void parse(XMLEventReader reader) throws IOException, XMLStreamException {
		boolean inFill = false;
		boolean inCellXfs = false;
		Fill currentFill = null;
		while (reader.hasNext()) {
			XMLEvent e = reader.nextEvent();
			if (e.isStartElement()) {
				StartElement el = e.asStartElement();
				if (el.getName().equals(FILL)) {
					inFill = true;
					currentFill = new EmptyFill();
				}
				else if (el.getName().equals(FGCOLOR) && inFill) {
					Attribute rgbAttr = el.getAttributeByName(RGB_ATTR);
					if (rgbAttr != null) {
						currentFill = new PatternFill(rgbAttr.getValue());
					}
					// TODO: handle @theme (sampleMore.xlsx), @indexed
				}
				else if (el.getName().equals(CELLXFS)) {
					inCellXfs = true;
				}
				else if (el.getName().equals(XF) && inCellXfs) {
					// fillId is optional; treat missing as 0
					int fillId = el.getAttributeByName(FILLID_ATTR) != null ?
							Integer.parseInt(el.getAttributeByName(FILLID_ATTR).getValue()) : 0;
					if (fillId < 0 || fillId >= fills.size()) {
						throw new OkapiBadFilterInputException("Invalid fillId reference in styles: " + fillId);
					}
					cellStyles.add(new CellStyle(fills.get(fillId)));
				}
			}
			else if (e.isEndElement()) {
				EndElement el = e.asEndElement();
				if (el.getName().equals(FILL)) {
					inFill = false;
					fills.add(currentFill);
				}
				else if (el.getName().equals(CELLXFS)) {
					inCellXfs = false;
				}
			}
		}
		reader.close();
	}

	static class EmptyFill implements Fill {
		@Override
		public boolean matchesColor(String argbColor) {
			return false;
		}
		@Override
		public String toString() {
			return "EmptyFill";
		}
		@Override
		public boolean equals(Object o) {
			return (o != null && o instanceof EmptyFill);
		}
		@Override
		public int hashCode() {
			return Objects.hash(false);
		}
	}

	static class PatternFill implements Fill {
		private String fgColor;
		PatternFill(String fgColor) {
			this.fgColor = fgColor;
		}

		@Override
		public boolean matchesColor(String argbColor) {
			return fgColor.equals(argbColor);
		}

		@Override
		public String toString() {
			return "PatternFill(" + fgColor + ")";
		}

		@Override
		public boolean equals(Object o) {
			if (o == this) return true;
			if (o == null || !(o instanceof PatternFill)) return false;
			return Objects.equals(fgColor, ((PatternFill)o).fgColor);
		}
		@Override
		public int hashCode() {
			return Objects.hash(fgColor);
		}
	}
}
