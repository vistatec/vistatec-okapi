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
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static net.sf.okapi.filters.openxml.AttributeStripper.GeneralAttributeStripper.stripGeneralAttributes;
import static net.sf.okapi.filters.openxml.AttributeStripper.RevisionAttributeStripper.stripRunRevisionAttributes;
import static net.sf.okapi.filters.openxml.ElementSkipper.RevisionPropertySkippableElement.RUN_PROPERTIES_CHANGE;
import static net.sf.okapi.filters.openxml.ElementSkipper.RunPropertySkippableElement.RUN_PROPERTY_LANGUAGE;
import static net.sf.okapi.filters.openxml.ElementSkipper.RunPropertySkippableElement.RUN_PROPERTY_NO_SPELLING_OR_GRAMMAR;
import static net.sf.okapi.filters.openxml.StartElementContextFactory.createStartElementContext;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.isEndElement;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.isWhitespace;

class RunPropertiesParser implements Parser<RunProperties>{
	protected StartElementContext startElementContext;
	private EndElement runPropsEndElement;
	private List<RunProperty> runProperties = new ArrayList<>();
	private ElementSkipper generalElementSkipper;

	RunPropertiesParser(StartElementContext startElementContext) {
		this.startElementContext = createStartElementContext(stripGeneralAttributes(
				createStartElementContext(stripRunRevisionAttributes(startElementContext), startElementContext)), startElementContext);

		generalElementSkipper = ElementSkipperFactory.createGeneralElementSkipper(startElementContext.getConditionalParameters(),
				RUN_PROPERTIES_CHANGE,
				RUN_PROPERTY_LANGUAGE,
				RUN_PROPERTY_NO_SPELLING_OR_GRAMMAR);
	}

	public RunProperties parse() throws XMLStreamException {
		// DrawingML properties contain inline property attributes that we must parse out
		if (Namespaces.DrawingML.containsName(startElementContext.getStartElement().getName())) {

			@SuppressWarnings("rawtypes") Iterator attrs = startElementContext.getStartElement().getAttributes();
			while (attrs.hasNext()) {
				// XXX Don't support hidden styles in DrawingML yet
				runProperties.add(RunPropertyFactory.createRunProperty((Attribute)attrs.next()));
			}
		}

		while (startElementContext.getEventReader().hasNext()) {
			XMLEvent e = startElementContext.getEventReader().nextEvent();
			if (isEndElement(e, startElementContext.getStartElement())) {
				endRunProperties(e.asEndElement());
				return buildRunProperties();
			}
			if (e.isStartElement()) {
				if (generalElementSkipper.canSkip(e.asStartElement(), startElementContext.getStartElement())) {
					generalElementSkipper.skip(createStartElementContext(e.asStartElement(), startElementContext));
				} else {
					// This gathers the whole event.
					addRunProperty(e.asStartElement());
				}
			}
			// Discard -- make sure we're not discarding meaningful data
			else if (e.isCharacters() && !isWhitespace(e)) {
				throw new IllegalStateException(
						"Discarding non-whitespace rPr characters " + e.asCharacters().getData());
			}
		}
		throw new IllegalStateException("Invalid content? Unterminated run properties");
	}

	private void addRunProperty(StartElement startElement) throws XMLStreamException {
		// Gather elements up to the end
		StartElementContext runPropertiesElementContext = createStartElementContext(startElement, startElementContext);
		runProperties.add(RunPropertyFactory.createRunProperty(runPropertiesElementContext));
	}

	private void endRunProperties(EndElement e) {
		runPropsEndElement = e;
	}

	private RunProperties buildRunProperties() {
		final StartElement startElement = startElementContext.getStartElement();
		if (startElement == null) {
			return RunProperties.emptyRunProperties();
		}
		if (runPropsEndElement == null) {
			throw new IllegalStateException("Incomplete run property markup: " +
						startElement.getName());
		}
		return new RunProperties.DefaultRunProperties(
				startElement, runPropsEndElement, runProperties);
	}
}
