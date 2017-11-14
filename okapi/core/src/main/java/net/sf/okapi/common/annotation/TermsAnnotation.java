/*===========================================================================
  Copyright (C) 2012 by the Okapi Framework contributors
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

import java.util.ArrayList;

/**
 * Simple annotation for storing terms and their associated information.
 */
public class TermsAnnotation implements IAnnotation {

	private ArrayList<String> terms;
	private ArrayList<String> infos;

	/**
	 * Creates a new empty TermsAnnotation object.
	 */
	public TermsAnnotation () {
		terms = new ArrayList<String>();
		infos = new ArrayList<String>();
	}

	/**
	 * Adds a term to the annotation.
	 * @param term the term to add.
	 * @param info the associated into (can be null).
	 */
	public void add (String term,
		String info)
	{
		terms.add(term);
		infos.add(info==null ? "" : info);
	}

	/**
	 * Gets the number of terms in this annotation.
	 * @return the number of terms in this annotation.
	 */
	public int size () {
		return terms.size();
	}
	
	/**
	 * Gets the term for a given index.
	 * @param index the index of the term to retrieve.
	 * @return the term for a given index.
	 */
	public String getTerm (int index) {
		return terms.get(index);
	}
	
	/**
	 * Gets the term information for a given index,
	 * or an empty string if there is not associated information for the term.
	 * @param index the index of the term information to retrieve.
	 * @return the term information for a given index.
	 */
	public String getInfo (int index) {
		return infos.get(index);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for ( int i=0; i<terms.size(); i++ ) {
			sb.append(terms.get(i));
			if ( !infos.get(i).isEmpty() ) {
				sb.append(" ["+infos.get(i)+"]");
			}
			sb.append(";\n");
		}
		return sb.toString();
	}
}
