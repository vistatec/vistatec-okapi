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
import java.io.StringWriter;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.exceptions.OkapiBadFilterInputException;
import net.sf.okapi.common.skeleton.GenericSkeleton;

class ExcelWorksheetPartHandler extends NonTranslatablePartHandler {
	private ConditionalParameters cparams;
	private int sheetNumber;
	private SharedStringMap ssm;
	private ExcelStyles styles;
	private boolean isSheetHidden;
	private Map<String, Boolean> tableVisibility;

	ExcelWorksheetPartHandler(OpenXMLZipFile zipFile, ZipEntry entry, SharedStringMap ssm, ExcelStyles styles,
							  Map<String, Boolean> tableVisibility, int sheetNumber, ConditionalParameters cparams,
							  boolean isSheetHidden) {
		super(zipFile, entry);
		this.sheetNumber = sheetNumber;
		this.ssm = ssm;
		this.cparams = cparams;
		this.styles = styles;
		this.tableVisibility = tableVisibility;
		this.isSheetHidden = isSheetHidden;
	}

	@Override
	protected String getModifiedContent() {
		try {
			StringWriter sw = new StringWriter();
			Set<String> excludedColumns = cparams.findExcludedColumnsForSheetNumber(sheetNumber);
			XMLEventReader r = getZipFile().getInputFactory().createXMLEventReader(
					getZipFile().getPartReader(getEntry().getName()));
			XMLEventWriter w = XMLOutputFactory.newInstance().createXMLEventWriter(sw);
			Relationships worksheetRels = getZipFile().getRelationshipsForTarget(getEntry().getName());
			new ExcelWorksheet(getZipFile().getEventFactory(), ssm, styles, worksheetRels,
					tableVisibility, isSheetHidden, excludedColumns, cparams.tsExcelExcludedColors,
					!cparams.getTranslateExcelHidden()).parse(r, w);
			return sw.toString();
		}
		catch (IOException e) {
			throw new OkapiBadFilterInputException(e);
		} catch (XMLStreamException e) {
			throw new OkapiBadFilterInputException(e);
		}
	}

	@Override
	public Event open(String documentId, String subDocumentId, LocaleId srcLang) throws IOException, XMLStreamException {
		return new Event(
				EventType.CUSTOM,
				new PostponedDocumentPart(new GenericSkeleton(getModifiedContent()), getEntry(), isSheetHidden));

	}

}
