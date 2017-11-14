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
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

import static net.sf.okapi.filters.openxml.ElementSkipper.RunPropertySkippableElement.RUN_PROPERTY_VERTICAL_ALIGNMENT;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.startElementEquals;

/**
 * Representation of the parsed properties of a text run.  Immutable.
 */
public abstract class RunProperties implements XMLEvents {

	abstract List<RunProperty> getProperties();

	public int count() {
		return getProperties().size();
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof RunProperties)) return false;
		return equalsProperties((RunProperties) o);
	}

	protected abstract boolean equalsProperties(RunProperties rp);

	@Override
	public abstract int hashCode();

	@Override
	public abstract List<XMLEvent> getEvents();

	public abstract RunProperties combineDistinct(RunProperties otherProperties, StyleDefinitions.TraversalStage traversalStage);

	/**
	 * Gets the run style property.
	 *
	 * @return The run style proprety
	 */
	public RunProperty.RunStyleProperty getRunStyleProperty() {
		for (RunProperty property : getProperties()) {
			if (property instanceof RunProperty.RunStyleProperty) {
				return (RunProperty.RunStyleProperty) property;
			}
		}

		return null;
	}

	/**
	 * Gets mergeable run properties.
	 *
	 * @return Mergeable run properties
	 */
	public List<RunProperty> getMergeableRunProperties() {
		List<RunProperty> properties = new ArrayList<>(getProperties().size());

		for (RunProperty property : getProperties()) {
			if (property instanceof MergeableRunProperty) {
				properties.add(property);
			}
		}

		return properties;
	}

	/**
	 * Create a copy of an exiting RunProperties object, optionally stripping the 
	 * <w:vertAlign> or <w:rStyle> or toggle property.
	 *
	 * @param existingProperties Existing properties
	 * @param stripVerticalAlign Strip vertical align property flag
	 * @param stripRunStyle      Strip run style property flag
	 * @param stripToggle        Strip toggle property flag
	 *
	 * @return A possibly stripped copy of run properties
	 */
	public static RunProperties copiedRunProperties(RunProperties existingProperties, boolean stripVerticalAlign, boolean stripRunStyle, boolean stripToggle) {
		if (existingProperties instanceof EmptyRunProperties) {
			return existingProperties;
		}

		List<RunProperty> newRunProperties = new ArrayList<>();

		for (RunProperty p : existingProperties.getProperties()) {
			// Ack!
			if (stripToggle && p instanceof RunProperty.WpmlToggleRunProperty) {
				continue;
			}
			if (stripRunStyle && p instanceof RunProperty.RunStyleProperty) {
				continue;
			}
			if (stripVerticalAlign && p instanceof RunProperty.GenericRunProperty) {
				if (RUN_PROPERTY_VERTICAL_ALIGNMENT.getValue().equals(p.getName().getLocalPart())) {
					continue; // skip it!
				}
			}
			newRunProperties.add(p);
		}

		return new DefaultRunProperties(((DefaultRunProperties)existingProperties).startElement,
										((DefaultRunProperties)existingProperties).endElement,
										newRunProperties);
	}

	/**
	 * Creates copied run properties.
	 *
	 * @param runProperties Run properties
	 *
	 * @return Copied run properties
	 */
	public static RunProperties copiedRunProperties(RunProperties runProperties) {
		if (runProperties instanceof EmptyRunProperties) {
			return runProperties;
		}

		List<RunProperty> properties = new ArrayList<>(runProperties.getProperties());

		return new DefaultRunProperties(((DefaultRunProperties) runProperties).startElement,
				((DefaultRunProperties) runProperties).endElement,
				properties);
	}

	/**
	 * Creates copied toggle run properties.
	 *
	 * @param runProperties Run properties
	 *
	 * @return Copied toggle run properties
	 */
	public static RunProperties copiedToggleRunProperties(RunProperties runProperties) {
		if (runProperties instanceof EmptyRunProperties) {
			return runProperties;
		}

		List<RunProperty> properties = new ArrayList<>(runProperties.count());

		for (RunProperty property : runProperties.getProperties()) {
			if (property instanceof RunProperty.WpmlToggleRunProperty) {
				properties.add(property);
			}
		}

		return new DefaultRunProperties(((DefaultRunProperties) runProperties).startElement,
				((DefaultRunProperties) runProperties).endElement,
				properties);
	}

	/**
	 * Creates empty run properties.
	 *
	 * @return Empty run properties
	 */
	public static RunProperties emptyRunProperties() {
		return new EmptyRunProperties();
	}

	/**
	 * Creates default run properties.
	 *
	 * @return Default run properties
	 */
	static RunProperties defaultRunProperties(StartElement startElement, EndElement endElement, RunProperty... properties) {
		return new DefaultRunProperties(startElement, endElement, new ArrayList<>(Arrays.asList(properties)));
	}

	/**
	 * Checks whether current run properties are the subset of others.
	 *
	 * Empty run properties are not a subset of non-empty others.
	 *
	 * @param other Other run properties
	 *
	 * @return {@code true} - if current run properties are the subset of others
	 *         {@code false} - otherwise
	 */
	public boolean isSubsetOf(RunProperties other) {
		if (getProperties().isEmpty() && !other.getProperties().isEmpty()) {
			return false;
		}

		// Algorithmically inefficient, but the number of properties in play is
		// generally so small that it should be fine.
outer:	for (RunProperty myProperty : getProperties()) {
			for (RunProperty otherProperty : other.getProperties()) {
				if (otherProperty.equalsProperty(myProperty)) {
					continue outer;
				}
			}
			return false;
		}
		return true;
	}

	/**
	 * Represents empty run properties.
	 *
	 * They are equal to the empty default run properties.
	 */
	static class EmptyRunProperties extends RunProperties {
		@Override
		public int hashCode() {
			return 1;
		}

		@Override
		protected boolean equalsProperties(RunProperties rp) {
			if (rp instanceof DefaultRunProperties) {
				return this.count() == rp.count();
			}

			return (rp instanceof EmptyRunProperties);
		}

		@Override
		public List<XMLEvent> getEvents() {
			return Collections.emptyList();
		}

		@Override
		public List<RunProperty> getProperties() {
			return Collections.emptyList();
		}

		/**
		 * Combines current properties with other properties.
		 *
		 * @param otherProperties Other properties to match against
		 * @param traversalStage  The traversal stage
		 *
		 * @return Other run properties
		 */
		@Override
		public RunProperties combineDistinct(RunProperties otherProperties, StyleDefinitions.TraversalStage traversalStage) {
			return otherProperties;
		}

		@Override
		public String toString() {
			return "(No properties)";
		}
	}

	static class DefaultRunProperties extends RunProperties {
		private StartElement startElement;
		private EndElement endElement;
		private List<RunProperty> properties = new ArrayList<>();

		DefaultRunProperties(StartElement startElement, EndElement endElement, List<RunProperty> properties) {
			this.startElement = startElement;
			this.endElement = endElement;
			this.properties.addAll(properties);
		}

		@Override
		public List<RunProperty> getProperties() {
			return properties;
		}

		@Override
		public List<XMLEvent> getEvents() {
			List<XMLEvent> events = new ArrayList<>();
			events.add(startElement);
			for (RunProperty property : properties) {
				events.addAll(property.getEvents());
			}
			events.add(endElement);
			return events;
		}

		/**
		 * Combines current properties with other properties.
		 *
		 * If a property is found in the list of others, it is replaced by the found one and the found one is removed.
		 * All non-matched other properties are added to the current list of properties.
		 *
		 * @param otherProperties Other properties to match against
		 * @param traversalStage  The traversal stage
		 *
		 * @return Current run properties
		 */
		@Override
		public RunProperties combineDistinct(RunProperties otherProperties, StyleDefinitions.TraversalStage traversalStage) {
			ListIterator<RunProperty> runPropertyIterator = properties.listIterator();

			while (runPropertyIterator.hasNext()) {
				RunProperty runProperty = runPropertyIterator.next();
				// cache start element name in order not to reconstruct it for some properties (e.g. FontsRunProperty)
				QName runPropertyStartElementName = runProperty.getName();

				Iterator<RunProperty> otherRunPropertyIterator = otherProperties.getProperties().iterator();

				while (otherRunPropertyIterator.hasNext()) {
					RunProperty otherRunProperty = otherRunPropertyIterator.next();

					if (runPropertyStartElementName.equals(otherRunProperty.getName())) {
						replace(runPropertyIterator, otherRunPropertyIterator, runProperty, otherRunProperty, traversalStage);
						break;
					}
				}

				if (otherProperties.getProperties().isEmpty()) {
					break;
				}
			}

			if (!otherProperties.getProperties().isEmpty()) {
				properties.addAll(otherProperties.getProperties());
			}

			return this;
		}

		private void replace(ListIterator<RunProperty> runPropertyIterator,
							 Iterator<RunProperty> otherRunPropertyIterator,
							 RunProperty runProperty,
							 RunProperty otherRunProperty,
							 StyleDefinitions.TraversalStage traversalStage) {

			if (runProperty instanceof RunProperty.WpmlToggleRunProperty) {

				if (StyleDefinitions.TraversalStage.VERTICAL == traversalStage) {
					boolean runPropertyValue = ((RunProperty.WpmlToggleRunProperty) runProperty).getToggleValue();
					boolean otherRunPropertyValue = ((RunProperty.WpmlToggleRunProperty) otherRunProperty).getToggleValue();

					if (!(runPropertyValue ^ otherRunPropertyValue)) {
						// exclusive OR resulted to "false", which means that the property can be removed, as it is the default value
						runPropertyIterator.remove();
						otherRunPropertyIterator.remove();
						return;
					}

					if (runPropertyValue) {
						// run property value is equal to "true" and other is "false", as the previous condition happens
						// only if both values are "true" or "false"
						otherRunPropertyIterator.remove();
						return;
					}

					// run property value is equal to "false" and other is "true",
					// move on to the default processing of all other types of properties
				}

				if (StyleDefinitions.TraversalStage.DOCUMENT_DEFAULT == traversalStage) {
					boolean runPropertyValue = ((RunProperty.WpmlToggleRunProperty) runProperty).getToggleValue();
					boolean otherRunPropertyValue = ((RunProperty.WpmlToggleRunProperty) otherRunProperty).getToggleValue();

					if (runPropertyValue && otherRunPropertyValue
							|| runPropertyValue) {
						// if run property value is equal to "true" and other run property value is equal to "whatever" value
						otherRunPropertyIterator.remove();
						return;
					}
					// run property value is equal to "false" and other is "true",
					// move on to the default processing of all other types of properties
				}

				// The MS Word does not follow up the flow of toggle properties processing and does substitute ANY value
				// in the styles hierarchy by ANY value if it has been specified later.
				// So, StyleDefinitions.TraversalStage.HORIZONTAL case is processed as all other types of properties.

				// StyleDefinitions.TraversalStage.DIRECT case is processed as all other types of properties
			}

			runPropertyIterator.set(otherRunProperty);
			otherRunPropertyIterator.remove();
		}

		@Override
		protected boolean equalsProperties(RunProperties o) {
			if (o instanceof EmptyRunProperties) {
				return this.count() == o.count();
			}
			if (!(o instanceof DefaultRunProperties)) return false;
			// Compare start events - this ensures the element/namespace is the same,
			// and also will compare attributes in the DrawingML case.
			DefaultRunProperties rp = (DefaultRunProperties)o;
			if (!startElementEquals(startElement, rp.startElement)) {
				return false;
			}
			// TODO handle out of order properties
			return properties.equals(rp.properties);
		}

		@Override
		public int hashCode() {
			return Objects.hash(startElement, endElement, properties);
		}

		@Override
		public String toString() {
			return "rPr(" + properties.size() + ")[" + properties + "]";
		}
	}
}
