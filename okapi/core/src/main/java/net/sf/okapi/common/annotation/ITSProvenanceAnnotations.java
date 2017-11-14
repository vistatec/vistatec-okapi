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

import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.IWithAnnotations;
import net.sf.okapi.common.resource.StartGroup;
import net.sf.okapi.common.resource.StartSubDocument;
import net.sf.okapi.common.resource.TextContainer;

/**
 * Annotation for ITS Provenance meta-data.
 * Allows for a unique ID to associated with the ITSProvenanceAnnotations class
 * when attached as an IAnnotation in the Annotations class. This is necessary
 * for resolving standoff meta-data.
 */
public class ITSProvenanceAnnotations extends GenericAnnotations {

	public ITSProvenanceAnnotations() {}

	public static void addAnnotations (ITextUnit tu,
		ITSProvenanceAnnotations newSet)
	{
		addAnnotationsHelper(tu, newSet);
	}

	public static void addAnnotations (TextContainer tc,
		ITSProvenanceAnnotations newSet)
	{
		addAnnotationsHelper(tc, newSet);
	}

	public static void addAnnotations (StartGroup group,
		ITSProvenanceAnnotations newSet)
	{
		addAnnotationsHelper(group, newSet);
	}

	public static void addAnnotations (StartSubDocument startSubDoc,
		ITSProvenanceAnnotations newSet)
	{
		addAnnotationsHelper(startSubDoc, newSet);
	}

	private static <T extends IWithAnnotations> void addAnnotationsHelper(T resource,
		ITSProvenanceAnnotations newSet)
	{
		if ( newSet != null ) {
			ITSProvenanceAnnotations current = resource.getAnnotation(ITSProvenanceAnnotations.class);
			if ( current == null ) {
				resource.setAnnotation(newSet);
			}
			else {
				current.addAll(newSet);
			}
		}
	}
}
