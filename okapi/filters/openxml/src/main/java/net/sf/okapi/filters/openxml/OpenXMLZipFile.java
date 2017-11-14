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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import com.ctc.wstx.api.WstxInputProperties;

import net.sf.okapi.common.Util;
import net.sf.okapi.common.exceptions.OkapiBadFilterInputException;
import net.sf.okapi.filters.openxml.ContentTypes.Types.Excel;
import net.sf.okapi.filters.openxml.ContentTypes.Types.Powerpoint;
import net.sf.okapi.filters.openxml.ContentTypes.Types.Word;

/**
 * Wrapper around a regular ZipFile to provide additional
 * functionality.
 */
public class OpenXMLZipFile {
	private ZipFile zipFile;
	private String encoding;
	private XMLInputFactory inputFactory;
	private XMLOutputFactory outputFactory;
	private XMLEventFactory eventFactory;
	protected ContentTypes contentTypes;
	protected String mainDocumentTarget;

	public static final String CONTENT_TYPES_PART = "[Content_Types].xml";
	public static final String ROOT_RELS_PART = "_rels/.rels";

	private static final String OFFICE_DOCUMENT_SOURCE_TYPE = Namespaces.DocumentRelationships.getDerivedURI("/officeDocument");
	private static final String VISIO_DOCUMENT_SOURCE_TYPE = Namespaces.VisioDocumentRelationships.getDerivedURI("/document");


	// The largest attribute I've ever seen in the wild is an o:gfxdata attribute that was just under
	// 1024*1024 characters.  We will double this to be safe.
	private static final int MAX_ATTRIBUTE_SIZE = 2 * 1024 * 1024;

	// Encoding is passed in for legacy reasons, it might be
	// better to determine it ourselve
	public OpenXMLZipFile(ZipFile zipFile, XMLInputFactory inputFactory, XMLOutputFactory outputFactory,
						  XMLEventFactory eventFactory, String encoding) {
		this.zipFile = zipFile;
		this.inputFactory = inputFactory;
		if (inputFactory.isPropertySupported(WstxInputProperties.P_MAX_ATTRIBUTE_SIZE)) {
            inputFactory.setProperty(WstxInputProperties.P_MAX_ATTRIBUTE_SIZE, MAX_ATTRIBUTE_SIZE);
        }
		this.outputFactory = outputFactory;
		this.eventFactory = eventFactory;
		this.encoding = encoding;
	}

	protected void initializeContentTypes() throws XMLStreamException, IOException {
		if (contentTypes == null) {
			contentTypes = new ContentTypes(inputFactory);
			contentTypes.parseFromXML(getPartReader(CONTENT_TYPES_PART));
		}
	}

	/**
	 * Determine the main part from the officeDocument relationship in the root
	 * rels file, then use its content type to figure out what kind of document
	 * this is.
	 * @param params parameters
	 * @return the document type 
	 * @throws IOException if any error is encountered while reading the stream
	 * @throws XMLStreamException if any error is encountered while parsing the XML
	 */
	public DocumentType createDocument(ConditionalParameters params) throws XMLStreamException, IOException {
		initializeContentTypes();

		mainDocumentTarget = getRelationshipTarget();

		DocumentType doc;

		switch (contentTypes.getContentType(mainDocumentTarget)) {
			case Word.MAIN_DOCUMENT_TYPE:
			case Word.MACRO_ENABLED_MAIN_DOCUMENT_TYPE:
			case Word.TEMPLATE_DOCUMENT_TYPE:
			case Word.MACRO_ENABLED_TEMPLATE_DOCUMENT_TYPE:
				doc = new WordDocument(this, params);
				break;

			case Powerpoint.MAIN_DOCUMENT_TYPE:
			case Powerpoint.MACRO_ENABLED_MAIN_DOCUMENT_TYPE:
			case Powerpoint.SLIDE_SHOW_DOCUMENT_TYPE:
			case Powerpoint.MACRO_ENABLED_SLIDE_SHOW_DOCUMENT_TYPE:
			case Powerpoint.TEMPLATE_DOCUMENT_TYPE:
			case Powerpoint.MACRO_ENABLED_TEMPLATE_DOCUMENT_TYPE:
				doc = new PowerpointDocument(this, params);
				break;

			case Excel.MAIN_DOCUMENT_TYPE:
			case Excel.MACRO_ENABLED_MAIN_DOCUMENT_TYPE:
			case Excel.TEMPLATE_DOCUMENT_TYPE:
			case Excel.MACRO_ENABLED_TEMPLATE_DOCUMENT_TYPE:
				doc = new ExcelDocument(this, params, null);
				break;

			case ContentTypes.Types.Visio.MAIN_DOCUMENT_TYPE:
			case ContentTypes.Types.Visio.MACRO_ENABLED_MAIN_DOCUMENT_TYPE:
				doc = new VisioDocument(this, params);
				break;

			default:
				throw new OkapiBadFilterInputException("Unrecognized main document target: " + mainDocumentTarget);
		}
		doc.initialize();

		return doc;
	}

	public ContentTypes getContentTypes() throws XMLStreamException, IOException {
		return contentTypes;
	}

	public XMLInputFactory getInputFactory() {
		return inputFactory;
	}

	public XMLOutputFactory getOutputFactory() {
		return outputFactory;
	}

	public XMLEventFactory getEventFactory() {
		return eventFactory;
	}

	public String getMainDocumentTarget() {
		return mainDocumentTarget;
	}

	protected String getRelationshipTarget() throws IOException, XMLStreamException {
		List<Relationships.Rel> docRels = getRelationships(ROOT_RELS_PART).getRelByType(OFFICE_DOCUMENT_SOURCE_TYPE);

		if (null != docRels) {
			return docRels.get(0).target;
		}

		docRels = getRelationships(ROOT_RELS_PART).getRelByType(VISIO_DOCUMENT_SOURCE_TYPE);

		if (null != docRels) {
			return docRels.get(0).target;
		}

		return null;
	}

	/**
	 * Return a reader for the named document part. The encoding passed to
	 * the constructor will be used to decode the content.  Bad things will
	 * happen if you call this on a binary part.
	 * @param partName name of the part. Should not contain a leading '/'.
	 * @return Reader
	 * @throws IOException if any error is encountered while reading the from the zip file
	 */
	public Reader getPartReader(String partName) throws IOException {
		ZipEntry entry = zipFile.getEntry(partName);
		if (entry == null) {
			throw new OkapiBadFilterInputException("File is missing " + partName);
		}
		// OpenXML documents produced by Office generally don't include BOMs, but
		// they may appear in documents produced by other sources
		return Util.skipBOM(new InputStreamReader(zipFile.getInputStream(entry), encoding));
	}

	private boolean isDocumentPartAvailable(String partName) {
		return zipFile.getEntry(partName) != null;
	}

	/**
	 * Parse the named document part as a relationships file and return the parsed
	 * relationships data.
	 * @param relsPartName name of the part. Should not contain a leading '/'.
	 * @return {@link Relationships} instance
	 * @throws IOException if any error is encountered while reading the stream
	 * @throws XMLStreamException if any error is encountered while parsing the XML
	 */
	public Relationships getRelationships(String relsPartName) throws IOException, XMLStreamException {
		Relationships rels = new Relationships(inputFactory);
		if (isDocumentPartAvailable(relsPartName)) {
			rels.parseFromXML(relsPartName, getPartReader(relsPartName));
		}
		return rels;
	}

	/**
	 * Find the relationships file for the named part and then parse the relationships.
	 * If no relationships file exists for the specified part, an empty Relationships
	 * object is returned.
	 * @param target
	 * @return
	 * @throws IOException
	 * @throws XMLStreamException
	 */
	public Relationships getRelationshipsForTarget(String target) throws IOException, XMLStreamException {
		int lastSlash = target.lastIndexOf("/");
		if (lastSlash == -1) {
			return getRelationships("_rels/" + target + ".rels");
		}
		String relPart = target.substring(0, lastSlash) + "/_rels" + target.substring(lastSlash) + ".rels";
		return getRelationships(relPart);
	}

	public InputStream getInputStream(ZipEntry entry) throws IOException {
		return zipFile.getInputStream(entry);
	}

	public ZipFile getZip() {
		return zipFile;
	}

	public void close() throws IOException {
		zipFile.close();
	}

	public Enumeration<? extends ZipEntry> entries() {
		return zipFile.entries();
	}
}
