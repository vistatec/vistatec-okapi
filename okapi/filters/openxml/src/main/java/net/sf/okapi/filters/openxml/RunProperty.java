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

import javax.annotation.Generated;
import javax.xml.namespace.QName;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static net.sf.okapi.filters.openxml.Relationships.ATTR_REL_ID;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.DEFAULT_BOOLEAN_ATTRIBUTE_TRUE_VALUE;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.DML_HYPERLINK_ACTION;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.WPML_VAL;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.eventEquals;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.getAttributeValue;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.getBooleanAttributeValue;

import net.sf.okapi.filters.openxml.RunPropertyFactory.SmlPropertyName;

public abstract class RunProperty implements Property, ReplaceableRunProperty {

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof RunProperty)) return false;
		return equalsProperty((RunProperty)o);
	}

	protected abstract boolean equalsProperty(RunProperty rp);

	@Override
	public abstract int hashCode();

	@Override
	public abstract List<XMLEvent> getEvents();

	@Override
	public boolean canBeReplaced(ReplaceableRunProperty runProperty) {
		return equalsProperty((RunProperty) runProperty);
	}

	@Override
	public ReplaceableRunProperty replace(ReplaceableRunProperty runProperty) {
		return runProperty;
	}

	@Override
	public abstract QName getName();

	abstract String getValue();

	static class GenericRunProperty extends RunProperty {
		private List<XMLEvent> events = new ArrayList<>();

		GenericRunProperty(List<XMLEvent> events) {
			this.events.addAll(events);
		}

		@Override
		protected boolean equalsProperty(RunProperty rp) {
			if (!(rp instanceof GenericRunProperty)) return false;
			return eventEquals(events, ((GenericRunProperty) rp).events);
		}

		@Override
		public int hashCode() {
			return events.hashCode();
		}

		@Override
		public List<XMLEvent> getEvents() {
			return events;
		}

		@Override
		public QName getName() {
			return events.get(0).asStartElement().getName();
		}

		@Override
		String getValue() {
			StartElement startElement = events.get(0).asStartElement();
			QName valQname = new QName(startElement.getName().getNamespaceURI(), "val");
			return getAttributeValue(startElement, valQname);
		}

		@Override
		public String toString() {
			return "GenericRunProperty(" + XMLEventSerializer.serialize(getEvents()) + ")";
		}
	}

	static class RunStyleProperty extends GenericRunProperty {
		private String value;

		RunStyleProperty(List<XMLEvent> events) {
			super(events);
			value = getAttributeValue(events.get(0).asStartElement(), WPML_VAL);
		}

		@Override
		public String getValue() {
			return value;
		}

		@Override
		protected boolean equalsProperty(RunProperty runProperty) {
			if (!(runProperty instanceof RunStyleProperty)) return false;
			return Objects.equals(value, ((RunStyleProperty) runProperty).value);
		}

		@Override
		public int hashCode() {
			return value.hashCode();
		}

		@Override
		public String toString() {
			return RunStyleProperty.class.getSimpleName() + "(" + XMLEventSerializer.serialize(getEvents()) + ")";
		}
	}

	static class WpmlToggleRunProperty extends GenericRunProperty {
		private boolean value;

		WpmlToggleRunProperty(List<XMLEvent> events) {
			super(events);
			value = getBooleanAttributeValue(events.get(0).asStartElement(), WPML_VAL, DEFAULT_BOOLEAN_ATTRIBUTE_TRUE_VALUE);
		}

		public boolean getToggleValue() {
			return value;
		}

		@Override
		protected boolean equalsProperty(RunProperty runProperty) {
			if (!(runProperty instanceof WpmlToggleRunProperty)) return false;

			return eventEquals(getEvents().get(0), runProperty.getEvents().get(0))
					&& Objects.equals(value, ((WpmlToggleRunProperty) runProperty).value);
		}

		@Override
		public int hashCode() {
			return Objects.hash(value);
		}

		@Override
		public String toString() {
			return WpmlToggleRunProperty.class.getSimpleName() + "(" + XMLEventSerializer.serialize(getEvents()) + ")";
		}
	}

	static class SmlRunProperty extends GenericRunProperty {

		private String value;

		SmlRunProperty(List<XMLEvent> events) {
			super(events);
			value = super.getValue();
		}

		@Override
		protected boolean equalsProperty(RunProperty runProperty) {
			if (!(runProperty instanceof SmlRunProperty)) return false;

			return eventEquals(getEvents().get(0), runProperty.getEvents().get(0))
					&& Objects.equals(value, ((SmlRunProperty) runProperty).value);
		}

		@Override
		public int hashCode() {
			return Objects.hash(value);
		}

		@Override
		public String toString() {
			return SmlRunProperty.class.getSimpleName() + "(" + XMLEventSerializer.serialize(getEvents()) + ")";
		}

		/**
		 * Get the defined default value of the SpreadsheetML run property.
		 *
		 * @return the default value
		 */
		public String getDefaultValue() {
			return SmlPropertyName.fromValue(getEvents().get(0).asStartElement().getName())
					.getDefaultValue();
		}
	}

	// DrawingML may have run properties as embedded attributes on the
	// run property start element. (eg, <a:rPr lang="fr-FR"/>)
	static class AttributeRunProperty extends RunProperty {
		private QName name;
		private String value;

		AttributeRunProperty(QName name, String value) {
			this.name = name;
			this.value = value;
		}

		@Override
		protected boolean equalsProperty(RunProperty rp) {
			if (!(rp instanceof AttributeRunProperty)) return false;
			AttributeRunProperty other = (AttributeRunProperty) rp;

			return Objects.equals(getName(), other.getName())
					&& Objects.equals(getValue(), other.getValue());
		}

		@Override
		public int hashCode() {
			return Objects.hash(name, value);
		}

		@Override
		public List<XMLEvent> getEvents() {
			// There are no events associated with this, since they are part of the
			// RunProperties start element
			return Collections.emptyList();
		}

		@Override
		public QName getName() {
			return name;
		}

		@Override
		String getValue() {
			return value;
		}

		@Override
		public String toString() {
			return "AttributeRunProperty(" + name + "=" + value + ")";
		}
	}

	/**
	 * This run property handles one of {@link RunPropertyFactory#DRAWINGML_HYPERLINK_NAMES}.
	 * <p>
	 * Full example of a paragraph that has a {@code hlinkClick}:
	 *
	 * <pre>
 	 * {@code <a:r>
     * 		<a:rPr lang="de-DE" dirty="0" smtClean="0"/>
     * 		<a:t>This is a </a:t>
	 * </a:r>
	 * <a:r>
	 *     <a:rPr lang="de-DE" dirty="0" smtClean="0">
	 *         <a:hlinkClick r:id="rId2" action="ppaction://hlinkpres?slideindex=1&amp;slidetitle="/>
	 *     </a:rPr>
	 *     <a:t>link</a:t>
	 * </a:r>
	 * <a:r>
	 *     	<a:rPr lang="de-DE" dirty="0" smtClean="0"/>
	 *     	<a:t>.</a:t>
	 * </a:r>}
	 * </pre>
	 * </p>
	 */
	static class HyperlinkRunProperty extends GenericRunProperty {

		HyperlinkRunProperty(List<XMLEvent> events) {
			super(events);
		}

		@Override
		String getValue() {
			StartElement startElement = getEvents().get(0).asStartElement();
			// the link target may be set directly as "action"
			if (startElement.getAttributeByName(DML_HYPERLINK_ACTION) != null) {
				return startElement.getAttributeByName(DML_HYPERLINK_ACTION).getValue();
			}
			// or it is added as relation id to be found as "Target" in the matching slideX.xml.rels
			if (startElement.getAttributeByName(ATTR_REL_ID) != null) {
				// we do not need the exact link value, but we know, we have a link, so we need a
				// value so the link is not skipped
				return startElement.getAttributeByName(ATTR_REL_ID).getValue();
			}
			return null;
		}
	}

	static class FontsRunProperty extends RunProperty implements MergeableRunProperty {
		private RunFonts runFonts;

		public FontsRunProperty(RunFonts runFonts) {
			this.runFonts = runFonts;
		}

		RunFonts getRunFonts() {
			return runFonts;
		}

		@Override
		public List<XMLEvent> getEvents() {
			return runFonts.getEvents();
		}

		@Override
		public QName getName() {
			return getEvents().get(0).asStartElement().getName();
		}

		@Override
		String getValue() {
			return null;
		}

		@Override
		public boolean canBeMerged(MergeableRunProperty runProperty) {
			if (!(runProperty instanceof FontsRunProperty)) {
				return false;
			}

			return runFonts.canBeMerged(((FontsRunProperty) runProperty).runFonts);
		}

		@Override
		public MergeableRunProperty merge(MergeableRunProperty runProperty) {
			runFonts = runFonts.merge(((FontsRunProperty) runProperty).runFonts);

			return this;
		}

		@Override
		protected boolean equalsProperty(RunProperty runProperty) {
			if (!(runProperty instanceof FontsRunProperty)) {
				return false;
			}

			return runFonts.equals(((FontsRunProperty) runProperty).runFonts);
		}

		@Override
		public int hashCode() {
			return runFonts.hashCode();
		}

		@Override
		public String toString() {
			return FontsRunProperty.class.getSimpleName() + "(" + XMLEventSerializer.serialize(getEvents()) + ")";
		}
	}
}
