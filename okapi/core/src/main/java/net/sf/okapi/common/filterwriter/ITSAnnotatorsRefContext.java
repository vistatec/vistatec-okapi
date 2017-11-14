/*===========================================================================
  Copyright (C) 2013 by the Okapi Framework contributors
-----------------------------------------------------------------------------
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

package net.sf.okapi.common.filterwriter;

import java.util.Stack;

import javax.xml.stream.XMLStreamReader;

import net.sf.okapi.common.Namespaces;
import net.sf.okapi.common.annotation.GenericAnnotation;
import net.sf.okapi.common.annotation.GenericAnnotationType;

/**
 * Provides a set of generic functions to retrieve the ITS annotators reference information.
 */
public class ITSAnnotatorsRefContext {

	private XMLStreamReader reader;
	private Stack<String> annotatorsRef;
	
	public ITSAnnotatorsRefContext (XMLStreamReader reader) {
		this.reader = reader;
		annotatorsRef = new Stack<String>();
		annotatorsRef.push(null);
	}
	
	/**
	 * Reads and pushes the annotatorsRef information in the context stack.
	 * <p>The method looks if there is an ITS annotatorsRef attribute in the current node 
	 * of the provided reader (the node is assumed to be an element)
	 * and uses it if present. It uses the parent context otherwise. 
	 */
	public void readAndPush () {
		String val = reader.getAttributeValue(Namespaces.ITS_NS_URI, "annotatorsRef");
		if ( val != null ) {
			// Update the existing values if needed
			val = ITSContent.updateAnnotatorsRef(annotatorsRef.peek(), val);
		}
		else {
			val = annotatorsRef.peek();
		}
		annotatorsRef.push(val);
	}
	
	/**
	 * Pops the current annotatorsRef from the context stack. 
	 */
	public void pop () {
		annotatorsRef.pop();
	}

	/**
	 * Gets the current annotatorsRef string.
	 * @return the current annotatorsRef string (can be null).
	 */
	public String peek () {
		return annotatorsRef.peek();
	}
	
	/**
	 * Gets an annotation for the current annotatorsRef string.
	 * @return a new annotation for the current annotatorsRef string or null.
	 */
	public GenericAnnotation getAnnotation () {
		String tmp = annotatorsRef.peek(); 
		if ( tmp == null ) return null;
		return new GenericAnnotation(GenericAnnotationType.ANNOT,
			GenericAnnotationType.ANNOT_VALUE, tmp);
	}

	/**
	 * Gets the IRI for a given data category, for the current content.
	 * @param dataCategory the data category to look up.
	 * @return the IRI for the given data, or null.
	 */
	public String getAnnotatorRef (String dataCategory) {
		String tmp = annotatorsRef.peek();
		if ( tmp == null ) return null;
		return ITSContent.annotatorsRefToMap(tmp).get(dataCategory);
	}

}

