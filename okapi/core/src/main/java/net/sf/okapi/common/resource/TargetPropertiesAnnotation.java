/*===========================================================================
  Copyright (C) 2008-2009 by the Okapi Framework contributors
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

package net.sf.okapi.common.resource;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.annotation.IAnnotation;
import net.sf.okapi.common.annotation.IterableEnumeration;

/**
 * The target properties associated to a set of source properties
 * in a resource.
 */
public class TargetPropertiesAnnotation implements IAnnotation, Iterable<LocaleId> {

	private ConcurrentHashMap<LocaleId, Map<String, Property>> targets;

	/**
	 * Creates a new TargetPropertiesAnnotation object.
	 */
	public TargetPropertiesAnnotation () {
		targets = new ConcurrentHashMap<LocaleId, Map<String, Property>>();
	}

	/**
	 * Sets properties for a given target locale.
	 * @param locId Code of the target locale for this property.
	 * @param properties The properties to set.
	 */
	public void set (LocaleId locId,
		Map<String, Property> properties)
	{
		targets.put(locId, properties);
	}

	/**
	 * Gets the properties for a given target locale.
	 * @param locId Code of the target locale of the properties to retrieve. 
	 * @return The properties, or null if none has been found.
	 */
	public Map<String, Property> get (LocaleId locId) {
		return targets.get(locId);
	}

	/**
	 * Indicates if this annotation has any properties.
	 * @return True if this annotation counts at least one property.
	 */
	public boolean isEmpty () {
		return targets.isEmpty();
	}

	/**
	 * Gets a new iterator for this annotation.
	 */
	public Iterator<LocaleId> iterator () {
		IterableEnumeration<LocaleId> iterableLocales = new IterableEnumeration<LocaleId>(targets.keys());
		return iterableLocales.iterator();
	}

	/**
	 * Gets a set of the target locales available in this annotation.
	 * @return A set of the target locales in this annotation.
	 */
	public Set<LocaleId> getLocales () {
		return targets.keySet();
	}

}
