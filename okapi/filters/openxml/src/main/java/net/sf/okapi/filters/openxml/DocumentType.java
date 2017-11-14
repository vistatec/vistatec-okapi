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
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;

import javax.xml.stream.XMLStreamException;

import static net.sf.okapi.filters.openxml.ContentTypes.Types.Common.PACKAGE_RELATIONSHIPS;
import static net.sf.okapi.filters.openxml.ContentTypes.Types.Visio.MASTER_TYPE;
import static net.sf.okapi.filters.openxml.ContentTypes.Types.Visio.PAGE_TYPE;

abstract class DocumentType {

	protected static final String UNEXPECTED_NUMBER_OF_RELATIONSHIPS = "Unexpected number of relationships";

	private OpenXMLZipFile zipFile;
	private ConditionalParameters params;

	DocumentType(OpenXMLZipFile zipFile, ConditionalParameters params) {
		this.zipFile = zipFile;
		this.params = params;
	}

	static boolean isRelationshipsPart(String contentType) {
		return PACKAGE_RELATIONSHIPS.equals(contentType);
	}

	static boolean isMasterPart(String type) {
		return MASTER_TYPE.equals(type);
	}

	static boolean isPagePart(String type) {
		return PAGE_TYPE.equals(type);
	}

	protected OpenXMLZipFile getZipFile() {
		return zipFile;
	}

	protected ConditionalParameters getParams() {
		return params;
	}

	protected String getRelationshipTarget(String relationshipType) throws IOException, XMLStreamException {
		String mainDocumentTarget = getZipFile().getMainDocumentTarget();
		Relationships relationships = getZipFile().getRelationshipsForTarget(mainDocumentTarget);
		List<Relationships.Rel> rels = relationships.getRelByType(relationshipType);

		if (null == rels) {
			return null;
		}

		return rels.get(0).target;
	}

	abstract OpenXMLPartHandler getHandlerForFile(ZipEntry entry, String mediaType);

	abstract void initialize() throws IOException, XMLStreamException;

	abstract boolean isClarifiablePart(String contentType);

	abstract boolean isStyledTextPart(String entryName, String type);

	/**
	 * Return the zip file entries for this document in the order they should be processed.
	 * @return the zip file entries
	 * @throws IOException if any error is encountered while reading the stream
	 * @throws XMLStreamException if any error is encountered while parsing the XML
	 */
	abstract Enumeration<? extends ZipEntry> getZipFileEntries()
		 throws IOException, XMLStreamException;
}
