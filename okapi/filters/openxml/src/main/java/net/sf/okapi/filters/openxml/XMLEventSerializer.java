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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import net.sf.okapi.common.encoder.EncoderContext;
import net.sf.okapi.common.encoder.QuoteMode;
import net.sf.okapi.common.encoder.XMLEncoder;

/**
 * XMLEventWriter is great, but is limited in that it expects to be
 * serializing whole XML documents.  This is not always the case in
 * this filter -- we may be serializing an incomplete snippet of XML.
 *
 * This class will serialize merely the events that are passed, without
 * any assumptions about document validity.
 */
public class XMLEventSerializer {
	private StringBuilder sb = new StringBuilder();
	private XMLEncoder elementEncoder, attrEncoder;
	private StartElement pendingStartElement;

	public XMLEventSerializer() {
		elementEncoder = new XMLEncoder("UTF-8", "\n", true, true, false, QuoteMode.UNESCAPED);
		attrEncoder = new XMLEncoder("UTF-8", "\n", true, true, false, QuoteMode.ALL);
	}

	public static String serialize(XMLEvent event) {
		return serialize(Collections.singletonList(event));
	}

	public static String serialize(XMLEvents events) {
		return serialize(events.getEvents());
	}

	public static String serialize(List<XMLEvent> events) {
		return new XMLEventSerializer().addAll(events).toString();
	}

	public XMLEventSerializer add(XMLEvents events) {
		return addAll(events.getEvents());
	}

	public XMLEventSerializer addAll(List<XMLEvent> events) {
		for (XMLEvent e : events) {
			add(e);
		}
		return this;
	}

	public XMLEventSerializer add(XMLEvent e) {
		// Handle pending start serialization
		if (pendingStartElement != null) {
			if (e.isEndElement() && pendingStartElement.getName().equals(e.asEndElement().getName())) {
				flushPendingStartElement(true);
				return this; // Skip this end element
			}
			else {
				flushPendingStartElement(false);
			}
		}
		switch (e.getEventType()) {
		case XMLStreamConstants.START_ELEMENT:
			addStartElement(e.asStartElement());
			break;
		case XMLStreamConstants.CHARACTERS:
			addCharacters(e.asCharacters());
			break;
		case XMLStreamConstants.END_ELEMENT:
			addEndElement(e.asEndElement());
			break;
		case XMLStreamConstants.DTD:
		case XMLStreamConstants.COMMENT:
		case XMLStreamConstants.START_DOCUMENT:
		case XMLStreamConstants.END_DOCUMENT:
			break;
		}
		return this;
	}

	private void addStartElement(StartElement e) {
		pendingStartElement = e;
	}

	private void writeStartElement(StartElement e) {
		Iterator<?> attrs = e.getAttributes();
		Iterator<?> ns = e.getNamespaces();
		QName name = e.getName();
		sb.append('<');
		addName(name, sb);
		while (ns.hasNext()) {
			Namespace namespace = (Namespace)ns.next();
			sb.append(" xmlns");
			if (!"".equals(namespace.getPrefix())) {
				sb.append(":").append(namespace.getPrefix());
			}
			sb.append("=\"").append(namespace.getNamespaceURI()).append("\"");
		}
		while (attrs.hasNext()) {
			Attribute attr = (Attribute)attrs.next();
			sb.append(' ');
			addName(attr.getName(), sb);
			sb.append("=\"").append(escapeAttrValue(attr.getValue())).append("\"");
		}
	}

	private static void addName(QName name, StringBuilder buffer) {
		String prefix = name.getPrefix();
		if (prefix != null && !"".equals(prefix)) {
			buffer.append(prefix).append(":");
		}
		buffer.append(name.getLocalPart());
	}

	private String escapeAttrValue(String v) {
		return attrEncoder.encode(v, EncoderContext.INLINE);
	}

	private void flushPendingStartElement(boolean isSingleton) {
		if (pendingStartElement == null) {
			return;
		}
		writeStartElement(pendingStartElement);
		if (isSingleton) {
			sb.append("/>");
		}
		else {
			sb.append(">");
		}
		pendingStartElement = null;
	}

	private void addEndElement(EndElement e) {
		QName name = e.getName();
		sb.append("</");
		addName(name, sb);
		sb.append(">");
	}

	private void addCharacters(Characters e) {
		sb.append(elementEncoder.encode(e.getData(), EncoderContext.TEXT));
	}

	@Override
	public String toString() {
		flushPendingStartElement(false);
		return sb.toString();
	}
}
