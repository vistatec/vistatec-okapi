/*===========================================================================
  Copyright (C) 2010 by the Okapi Framework contributors
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

package net.sf.okapi.lib.terminology.simpletb;

public class Entry implements Comparable<Entry> {

	String srcTerm;
	String trgTerm;
	String definition;

	public Entry (String srcTerm) {
		this.srcTerm = srcTerm;
	}

	@Override
	public String toString() {
		return srcTerm + ":" + trgTerm;
	}
	public void setSourceTerm (String term) {
		srcTerm = term;
	}

	public String getSourceTerm () {
		return srcTerm;
	}
	
	public void setTargetTerm (String term) {
		trgTerm = term;
	}

	public String getTargetTerm () {
		return trgTerm;
	}
	
	public void setdefinition (String definition) {
		this.definition = definition;
	}

	public String getDefinition () {
		return definition;
	}

	@Override
	/**
	 * This method compare by length then by character, always in reverse order.
	 */
	public int compareTo (Entry other) {
		if ( srcTerm.length() > other.srcTerm.length() ) return -1;
		if ( srcTerm.length() == other.srcTerm.length() ) {
			return other.srcTerm.compareTo(srcTerm);
		}
		return 1;
	}
	
}
