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
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * Class to parse ppt/presentations.xml (or other files of content type
 * application/vnd.openxmlformats-officedocument.presentationml.presentation.main+xml)
 * and resolve the embedded slide IDs to usable part names.
 */
public class Presentation {
	private XMLInputFactory factory;
	private Relationships rels;
	
	static final QName SLIDE_ID = Namespaces.PresentationML.getQName("sldId"); 
	
	private List<String> slidePartNames = new ArrayList<String>();
	
	public Presentation(XMLInputFactory factory, Relationships rels) {
		this.factory = factory;
		this.rels = rels;
	}
	
	public List<String> getSlidePartNames() {
		return slidePartNames;
	}
	
	public void parseFromXML(Reader reader) throws XMLStreamException {
		XMLEventReader eventReader = factory.createXMLEventReader(reader);
		
		while (eventReader.hasNext()) {
			XMLEvent e = eventReader.nextEvent();
			
			if (e.isStartElement()) {
				StartElement el = e.asStartElement();
				if (el.getName().equals(SLIDE_ID)) {
					Attribute id = el.getAttributeByName(Relationships.ATTR_REL_ID);
					if (id != null) {
						Relationships.Rel rel = rels.getRelById(id.getValue());
						if (rel == null) {
							throw new IllegalStateException(
									"Presentation refers to non-existent slide ID " + id.getValue());
						}
						slidePartNames.add(rel.target);
					}
				}
			}
		}
	}
}
