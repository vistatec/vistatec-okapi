/*===========================================================================
  Copyright (C) 2013 by the Okapi Framework contributors
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

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * Code to parse the [Content_Types].xml files present in Office OpenXML documents.
 */
class ContentTypes {

	static class Types {
		private static final String APPLICATION_PREFIX = "application/vnd.";
		private static final String DOCUMENT_PREFIX = APPLICATION_PREFIX + "openxmlformats-officedocument.";

		static class Common {
			static final String CORE_PROPERTIES_TYPE =
					APPLICATION_PREFIX + "openxmlformats-package.core-properties+xml";
			static final String PACKAGE_RELATIONSHIPS =
					APPLICATION_PREFIX + "openxmlformats-package.relationships+xml";
		}

		static class Word {
			static final String MAIN_DOCUMENT_TYPE =
					DOCUMENT_PREFIX + "wordprocessingml.document.main+xml";
			static final  String MACRO_ENABLED_MAIN_DOCUMENT_TYPE =
					APPLICATION_PREFIX + "ms-word.document.macroEnabled.main+xml";

			static final String TEMPLATE_DOCUMENT_TYPE =
					DOCUMENT_PREFIX + "wordprocessingml.template.main+xml";
			static final String MACRO_ENABLED_TEMPLATE_DOCUMENT_TYPE =
					APPLICATION_PREFIX + "ms-word.template.macroEnabledTemplate.main+xml";

			static final String SETTINGS_TYPE =
					DOCUMENT_PREFIX + "wordprocessingml.settings+xml";
			static final String STYLES_TYPE =
					DOCUMENT_PREFIX + "wordprocessingml.styles+xml";
			static final String FOOTER_TYPE =
					DOCUMENT_PREFIX + "wordprocessingml.footer+xml";
			static final String ENDNOTES_TYPE =
					DOCUMENT_PREFIX + "wordprocessingml.endnotes+xml";
			static final String HEADER_TYPE =
					DOCUMENT_PREFIX + "wordprocessingml.header+xml";
			static final String FOOTNOTES_TYPE =
					DOCUMENT_PREFIX + "wordprocessingml.footnotes+xml";
			static final String COMMENTS_TYPE =
					DOCUMENT_PREFIX + "wordprocessingml.comments+xml";
			static final String GLOSSARY_TYPE =
					DOCUMENT_PREFIX + "wordprocessingml.document.glossary+xml";
		}
		static class Drawing {
			static final String CHART_TYPE =
					DOCUMENT_PREFIX + "drawingml.chart+xml";
			static final String DIAGRAM_TYPE =
					DOCUMENT_PREFIX + "drawingml.diagramData+xml";
		}
		static class Powerpoint {
			static final String MAIN_DOCUMENT_TYPE =
					DOCUMENT_PREFIX + "presentationml.presentation.main+xml";
			static final String MACRO_ENABLED_MAIN_DOCUMENT_TYPE =
					APPLICATION_PREFIX + "ms-powerpoint.presentation.macroEnabled.main+xml";

			static final String SLIDE_SHOW_DOCUMENT_TYPE =
					DOCUMENT_PREFIX + "presentationml.slideshow.main+xml";
			static final String MACRO_ENABLED_SLIDE_SHOW_DOCUMENT_TYPE =
					APPLICATION_PREFIX + "ms-powerpoint.slideshow.macroEnabled.main+xml";

			static final String TEMPLATE_DOCUMENT_TYPE =
					DOCUMENT_PREFIX + "presentationml.template.main+xml";
			static final String MACRO_ENABLED_TEMPLATE_DOCUMENT_TYPE =
					APPLICATION_PREFIX + "ms-powerpoint.template.macroEnabled.main+xml";

			static final String SLIDE_TYPE =
					DOCUMENT_PREFIX + "presentationml.slide+xml";
			static final String COMMENTS_TYPE =
					DOCUMENT_PREFIX + "presentationml.comments+xml";
			static final String NOTES_TYPE =
					DOCUMENT_PREFIX + "presentationml.notesSlide+xml";
			static final String MASTERS_TYPE =
					DOCUMENT_PREFIX + "presentationml.slideMaster+xml";
			static final String LAYOUT_TYPE =
					DOCUMENT_PREFIX + "presentationml.slideLayout+xml";
		}
		static class Excel {
			static final String MAIN_DOCUMENT_TYPE =
					DOCUMENT_PREFIX + "spreadsheetml.sheet.main+xml";
			static final String MACRO_ENABLED_MAIN_DOCUMENT_TYPE =
					APPLICATION_PREFIX + "ms-excel.sheet.macroEnabled.main+xml";

			static final String TEMPLATE_DOCUMENT_TYPE =
					DOCUMENT_PREFIX + "spreadsheetml.template.main+xml";
			static final String MACRO_ENABLED_TEMPLATE_DOCUMENT_TYPE =
					APPLICATION_PREFIX + "ms-excel.template.macroEnabled.main+xml";

			static final String SHARED_STRINGS_TYPE =
					DOCUMENT_PREFIX + "spreadsheetml.sharedStrings+xml";
			static final String WORKSHEET_TYPE =
					DOCUMENT_PREFIX + "spreadsheetml.worksheet+xml";
			static final String COMMENT_TYPE =
					DOCUMENT_PREFIX + "spreadsheetml.comments+xml";
			static final String TABLE_TYPE =
					DOCUMENT_PREFIX + "spreadsheetml.table+xml";
			static final String STYLES_TYPE =
					DOCUMENT_PREFIX + "spreadsheetml.styles+xml";
			static final String DRAWINGS_TYPE =
					DOCUMENT_PREFIX + "drawing+xml";
		}
		static class Visio {
			static final String MAIN_DOCUMENT_TYPE = APPLICATION_PREFIX + "ms-visio.drawing.main+xml";
			static final String MACRO_ENABLED_MAIN_DOCUMENT_TYPE = APPLICATION_PREFIX + "ms-visio.drawing.macroEnabled.main+xml";

			static final String MASTER_TYPE = APPLICATION_PREFIX + "ms-visio.master+xml";
			static final String PAGE_TYPE = APPLICATION_PREFIX + "ms-visio.page+xml";
		}
	}

	static final QName DEFAULT = Namespaces.ContentTypes.getQName("Default");
	static final QName OVERRIDE = Namespaces.ContentTypes.getQName("Override");
	static final QName PARTNAME_ATTR = new QName("PartName");
	static final QName CONTENTTYPE_ATTR = new QName("ContentType");
	static final QName EXTENSION_ATTR = new QName("Extension");
	
	private XMLInputFactory factory;
	private Map<String, String> defaults = new HashMap<String, String>();
	private Map<String, String> overrides = new HashMap<String, String>();
	
	ContentTypes(XMLInputFactory factory) {
		this.factory = factory;
	}
	
	String getContentType(String partName) {
		partName = ensureWellformedPath(partName);
		if (overrides.containsKey(partName)) {
			return overrides.get(partName);
		}
		String suffix = getSuffix(partName);
		if (defaults.containsKey(suffix)) {
			return defaults.get(suffix);
		}
		
		// Unknown file - this shouldn't ever happen.  We 
		// report this as arbitrary data.
		return "application/octet-stream";
	}
	
	void parseFromXML(Reader reader) throws XMLStreamException {
		XMLEventReader eventReader = factory.createXMLEventReader(reader);
		
		while (eventReader.hasNext()) {
			XMLEvent e = eventReader.nextEvent();
			
			if (e.isStartElement()) {
				StartElement el = e.asStartElement();
				if (el.getName().equals(DEFAULT)) {
					Attribute ext = el.getAttributeByName(EXTENSION_ATTR);
					Attribute type = el.getAttributeByName(CONTENTTYPE_ATTR);
					if (ext != null && type != null) {
						defaults.put(ext.getValue(), type.getValue());
					}
				}
				else if (el.getName().equals(OVERRIDE)) {
					Attribute part = el.getAttributeByName(PARTNAME_ATTR);
					Attribute type = el.getAttributeByName(CONTENTTYPE_ATTR);
					if (part != null && type != null) {
						overrides.put(ensureWellformedPath(part.getValue()),
									  type.getValue());
					}
				}
			}
		}
	}
	
	private String getSuffix(String partName) {
		String suffix = partName;
		int i = suffix.lastIndexOf('.');
		if (i != -1) {
			suffix = suffix.substring(i + 1);
		}
		return suffix;
	}
	
	private String ensureWellformedPath(String p) {
		return p.startsWith("/") ? p : "/" + p;
	}
}
