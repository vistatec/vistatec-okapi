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

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;

import static net.sf.okapi.filters.openxml.ContentTypes.Types.Common.PACKAGE_RELATIONSHIPS;
import static net.sf.okapi.filters.openxml.ContentTypes.Types.Common.CORE_PROPERTIES_TYPE;
import static net.sf.okapi.filters.openxml.ContentTypes.Types.Drawing;
import static net.sf.okapi.filters.openxml.ContentTypes.Types.Word;
import static net.sf.okapi.filters.openxml.ParseType.MSWORDDOCPROPERTIES;

class WordDocument extends DocumentType {
	private static final String STYLE_DEFINITIONS_SOURCE_TYPE = Namespaces.DocumentRelationships.getDerivedURI("/styles");
	private static final String TARGET_REL_FILE = "document.xml.rels";

	private StyleDefinitions styleDefinitions;

	WordDocument(OpenXMLZipFile zipFile, ConditionalParameters params) {
		super(zipFile, params);
	}

	@Override
	void initialize() throws IOException, XMLStreamException {
		styleDefinitions = parseStyleDefinitions();
	}

	private StyleDefinitions parseStyleDefinitions() throws IOException, XMLStreamException {
		String relationshipTarget = getRelationshipTarget(STYLE_DEFINITIONS_SOURCE_TYPE);

		if (null == relationshipTarget) {
			return new EmptyStyleDefinitions();
		}

		Reader reader = getZipFile().getPartReader(relationshipTarget);

		return new WordProcessingStylesParser(
				getZipFile().getEventFactory(),
				getZipFile().getInputFactory(),
				reader,
				getParams()
		).parse();
	}

	@Override
	OpenXMLPartHandler getHandlerForFile(ZipEntry entry,
			String contentType) {

		if (PACKAGE_RELATIONSHIPS.equals(contentType) && entry.getName().endsWith(TARGET_REL_FILE) && getParams().getExtractExternalHyperlinks()) {
			return new RelationshipsPartHandler(getParams(), getZipFile(), entry);
		}

		// Check to see if this is non-translatable
		if (!isTranslatableType(entry.getName(), contentType)) {
			return new NonTranslatablePartHandler(getZipFile(), entry);
		}

		if (isStyledTextPart(entry.getName(), contentType)) {
			return new StyledTextPartHandler(getParams(), getZipFile(), entry, styleDefinitions);
		}

		OpenXMLContentFilter openXMLContentFilter = new OpenXMLContentFilter(getParams(), entry.getName());
		ParseType parseType = ParseType.MSWORD;
		if (Word.SETTINGS_TYPE.equals(contentType)) {
			openXMLContentFilter.setBInSettingsFile(true);
		}
		else if (CORE_PROPERTIES_TYPE.equals(contentType)) {
			parseType = MSWORDDOCPROPERTIES;
		}
		openXMLContentFilter.setUpConfig(parseType);

		// From openSubDocument
		if (null != getParams().tsExcludeWordStyles && !getParams().tsExcludeWordStyles.isEmpty()) {
			openXMLContentFilter.setTsExcludeWordStyles(getParams().tsExcludeWordStyles);
		}
		return new StandardPartHandler(openXMLContentFilter, getParams(), getZipFile(), entry);
	}

	@Override
	boolean isClarifiablePart(String contentType) {
		return false;
	}

	@Override
	boolean isStyledTextPart(String entryName, String type) {
		return (type.equals(Word.MAIN_DOCUMENT_TYPE) ||
				type.equals(Word.MACRO_ENABLED_MAIN_DOCUMENT_TYPE) ||
				type.equals(Word.HEADER_TYPE) ||
				type.equals(Word.FOOTER_TYPE) ||
				type.equals(Word.ENDNOTES_TYPE) ||
				type.equals(Word.FOOTNOTES_TYPE) ||
				type.equals(Word.GLOSSARY_TYPE)) ||
				type.equals(Word.COMMENTS_TYPE) ||
				type.equals(Drawing.DIAGRAM_TYPE) ||
				type.equals(Drawing.CHART_TYPE);
	}

	private boolean isTranslatableType(String entryName, String type) {
		if (!entryName.endsWith(".xml")) return false;
		if (type.equals(Word.MAIN_DOCUMENT_TYPE)) return true;
		if (type.equals(Word.MACRO_ENABLED_MAIN_DOCUMENT_TYPE)) return true;
		if (type.equals(Word.STYLES_TYPE)) return true;
		if (getParams().getTranslateDocProperties() && type.equals(CORE_PROPERTIES_TYPE)) return true;
		if (type.equals(Word.HEADER_TYPE) || type.equals(Word.FOOTER_TYPE)) {
			return getParams().getTranslateWordHeadersFooters();
		}
		if (type.equals(Word.COMMENTS_TYPE)) {
			return getParams().getTranslateComments();
		}
		if (type.equals(Word.SETTINGS_TYPE)) return true;
		if (type.equals(Drawing.CHART_TYPE)) return true;
		if (isStyledTextPart(entryName, type)) return true;
		return false;
	}

	@Override
	Enumeration<? extends ZipEntry> getZipFileEntries() {
		List<? extends ZipEntry> list = Collections.list(getZipFile().entries());
		List<String> additionalParts = new ArrayList<String>();
		additionalParts.add("word/styles.xml");
		additionalParts.add("word/_rels/document.xml.rels");
		additionalParts.add("word/document.xml");
		Collections.sort(list, new ZipEntryComparator(additionalParts));
		return Collections.enumeration(list);
	}
}
