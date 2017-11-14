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

import javax.xml.namespace.QName;

enum Namespaces {
	UNSUPPORTED(""),
	XML("http://www.w3.org/XML/1998/namespace"),
	Relationships("http://schemas.openxmlformats.org/package/2006/relationships"),
	ContentTypes("http://schemas.openxmlformats.org/package/2006/content-types"),
	WordProcessingML("http://schemas.openxmlformats.org/wordprocessingml/2006/main"),
	SpreadsheetML("http://schemas.openxmlformats.org/spreadsheetml/2006/main"),
	PresentationML("http://schemas.openxmlformats.org/presentationml/2006/main"),
	DrawingML("http://schemas.openxmlformats.org/drawingml/2006/main"),
	Math("http://schemas.openxmlformats.org/officeDocument/2006/math"),
	Chart("http://schemas.openxmlformats.org/drawingml/2006/chart"),
	VML("urn:schemas-microsoft-com:vml"),
	WordProcessingDrawingML("http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing"),
	// Seperate namespace that is used, somewhat strangely, to refer to rel data
	// (e.g., rel IDs) from non-rel sources.
	DocumentRelationships("http://schemas.openxmlformats.org/officeDocument/2006/relationships"),
	VisioDocumentRelationships("http://schemas.microsoft.com/visio/2010/relationships");

	private String nsURI;

	Namespaces(String nsURI) {
		this.nsURI = nsURI;
	}

	/**
	 * Gets a namespace by a namespace URI.
	 *
	 * @param namespaceURI A namespace URI
	 *
	 * @return A namespace
	 */
	static Namespaces fromNamespaceURI(String namespaceURI) {
		if (null == namespaceURI) {
			return UNSUPPORTED;
		}

		for (Namespaces namespace : values()) {
			if (namespaceURI.equals(namespace.getURI())) {
				return namespace;
			}
		}

		return UNSUPPORTED;
	}

	String getURI() {
		return nsURI;
	}

	QName getQName(String localPart) {
		return new QName(nsURI, localPart);
	}

	String getDerivedURI(String path) {
		StringBuilder sb = new StringBuilder(nsURI);
		if (!path.startsWith("/")) {
			sb.append("/");
		}
		sb.append(path);
		return sb.toString();
	}

	boolean containsName(QName name) {
		return nsURI.equals(name.getNamespaceURI());
	}
}
