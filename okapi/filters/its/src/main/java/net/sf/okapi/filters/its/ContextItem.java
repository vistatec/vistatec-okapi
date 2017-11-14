/*===========================================================================
  Copyright (C) 2009-2013 by the Okapi Framework contributors
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

package net.sf.okapi.filters.its;

import net.sf.okapi.common.annotation.GenericAnnotations;

import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.w3c.its.ITSEngine;

class ContextItem {
	
	Node node;
	boolean translate;
	String trgPointer;
	String idValue;
	String locNote;
	boolean preserveWS;
	String domains;
	String externalRes;
	String allowedChars;
	GenericAnnotations lqIssues;
	GenericAnnotations storageSize;
	GenericAnnotations lqRating;
	GenericAnnotations ta;
	GenericAnnotations terminology;
	Double mtConfidence;
	GenericAnnotations prov;

	public ContextItem (Node node,
		ITSEngine trav)
	{
		this(node, trav, null);
	}
	
	public ContextItem (Node node,
		ITSEngine trav,
		Attr attribute)
	{
		this.node = node;
		// Context is always an element node
		this.translate = trav.getTranslate(attribute);
		this.trgPointer = trav.getTargetPointer(attribute);
		this.idValue = trav.getIdValue(attribute);
		this.locNote = trav.getLocNote(attribute);
		this.preserveWS = trav.preserveWS(attribute);
		this.domains = trav.getDomains(attribute);
		this.externalRes = trav.getExternalResourceRef(attribute);
		this.allowedChars = trav.getAllowedCharacters(attribute);
		this.storageSize = trav.getStorageSizeAnnotation(attribute);
		this.lqIssues = trav.getLocQualityIssueAnnotations(attribute);
		this.lqRating = trav.getLocQualityRatingAnnotation();
		this.mtConfidence = trav.getMtConfidence(attribute);
		this.ta = trav.getTextAnalysisAnnotation(attribute);
		this.prov = trav.getProvenanceAnnotations(attribute);
		this.terminology = trav.getTerminologyAnnotation(attribute);
	}

}
