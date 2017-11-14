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

package net.sf.okapi.common.annotation;

import java.util.UUID;

import net.sf.okapi.common.Util;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.IWithAnnotations;
import net.sf.okapi.common.resource.IWithProperties;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.TextContainer;

/**
 * Annotation for ITS Language Quality Issue meta-data.
 * Allows for a unique ID to associated with the ITSLQIAnnotations class when
 * attached as an IAnnotation in the Annotations class. This is necessary
 * for resolving standoff meta-data.
 */
public class ITSLQIAnnotations extends GenericAnnotations {

	public ITSLQIAnnotations() {}

	/**
	 * Adds an ITSLQIAnnotations to a text unit.
	 * @param tu the text unit where to add the annotations.
	 * @param newSet the set of annotations to add.
	 */
	public static void addAnnotations (ITextUnit tu,
		ITSLQIAnnotations newSet)
	{
		addAnnotationsHelper(tu, newSet);
	}

	/**
	 * Adds an ITSLQIAnnotations to a text container.
	 * @param tc the text container where to add the annotations.
	 * @param newSet the set of annotations to add.
	 */
	public static void addAnnotations(TextContainer tc,
		ITSLQIAnnotations newSet)
	{
		addAnnotationsHelper(tc, newSet);
	}

	private static <T extends IWithAnnotations> void addAnnotationsHelper (T resource, ITSLQIAnnotations newSet) {
		if ( newSet != null ) {
			ITSLQIAnnotations current = resource.getAnnotation(ITSLQIAnnotations.class);
			if ( current == null ) {
				resource.setAnnotation(newSet);
			}
			else {
				current.addAll(newSet);
			}
		}
	}
	
	/**
	 * Adds an LQI annotation to a text container.
	 * @param tc the text container where to add the annotations.
	 * @param issue the annotation to add.
	 */
	public static void addAnnotations (TextContainer tc,
		GenericAnnotation issue)
	{
		addAnnotationsHelper(tc, issue);
	}

	private static <T extends IWithAnnotations & IWithProperties> void addAnnotationsHelper (T resource,
		GenericAnnotation issue)
	{
		if ( issue == null ) return;
		ITSLQIAnnotations current = resource.getAnnotation(ITSLQIAnnotations.class);
		if ( current == null ) {
			ITSLQIAnnotations anns = new ITSLQIAnnotations();
			String id = Util.makeId(UUID.randomUUID().toString());
			resource.setProperty(new Property(Property.ITS_LQI,
				" its:locQualityIssuesRef=\"#"+id+"\""));
			anns.setData(id);
			anns.add(issue);
			resource.setAnnotation(anns);
		}
		else {
			current.add(issue);
		}
	}

}
