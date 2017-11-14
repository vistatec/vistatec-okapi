/*===========================================================================
  Copyright (C) 2008-2012 by the Okapi Framework contributors
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

package org.w3c.its;

import net.sf.okapi.common.annotation.GenericAnnotations;

class ITSTrace {
	
	boolean isChildDone;
	boolean translate;
	int dir;
	int withinText;
	GenericAnnotations termino;
	String locNote;
	String locNoteType;
	boolean preserveWS;
	String language;
	String targetPointer;
	String externalRes;
	String localeFilter = "*";
	String idValue;
	String domains;
	String allowedChars;
	String subFilter;
	String lqIssuesRef;
	GenericAnnotations lqIssues;
	String annotatorsRef;
	Double mtConfidence;
	GenericAnnotations storageSize;
	GenericAnnotations ta;
	GenericAnnotations lqRating;
	GenericAnnotations prov;

	ITSTrace () {
		// Default constructor
	}
	
	ITSTrace (ITSTrace initialTrace,
		boolean isChildDone)
	{
		// translate: Inheritance for child elements but not attributes
		translate = initialTrace.translate;
		
		// dir: Inheritance for child element including attributes
		dir = initialTrace.dir;
		
		// withinText: No inheritance
		
		// term: No inheritance
		
		// target: No inheritance
		
		// locNote: Inheritance for child elements including attributes
		locNote = initialTrace.locNote;
		locNoteType = initialTrace.locNoteType;
		
		// preserveWS: Inheritance for child elements including attributes
		preserveWS = initialTrace.preserveWS;
	
		// language: Inheritance for child element including attributes 
		language = initialTrace.language;
		
		// idValue: No inheritance
		
		// external resource reference: No inheritance
		
		//MT Confidence: Inheritance for child element including attributes
		mtConfidence = initialTrace.mtConfidence;
		
		// locale filter: Inheritance for child element including attributes
		localeFilter = initialTrace.localeFilter;
		
		// domain: Inheritance for child elements including attributes
		domains = initialTrace.domains;
		
		// Text analysis: no inheritance 
		
		// localization quality issue:
		lqIssuesRef = initialTrace.lqIssuesRef;
		lqIssues = initialTrace.lqIssues;
		
		// localization quality rating
		lqRating = initialTrace.lqRating;
		
		// Allowed chars: Inheritance for child elements but not attributes
		allowedChars = initialTrace.allowedChars;
		
		// Store size: No inheritance
		
		// sub-filter: No inheritance
		
		// Provenance: Inheritance for child elements including attributes
		this.prov = initialTrace.prov;
		
		this.isChildDone = isChildDone; // From parameter
		
		this.annotatorsRef = initialTrace.annotatorsRef;
	}

}
