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

package net.sf.okapi.lib.terminology;

import java.util.HashMap;
import java.util.Map;

import net.sf.okapi.common.LocaleId;

public class ConceptEntry extends BaseEntry {	
	private String id;
	private Map<LocaleId, LangEntry> langs;

	/**
	 * Creates a new ConceptEntry object.
	 */
	public ConceptEntry () {
		langs = new HashMap<LocaleId, LangEntry>();
	}
	
	public ConceptEntry(String source, String target, LocaleId srcLocale, LocaleId trgLocale) {
		this();
		addTerm(srcLocale, source);
		addTerm(trgLocale, target);
	}
	
	/**
	 * Gets the ID for this glossary entry.
	 * @return the ID for this glossary entry.
	 */
	public String getId () {
		return id;
	}
	
	/**
	 * Sets the ID for this glossary entry.
	 * @param id the ID for this glossary entry.
	 */
	public void setId (String id) {
		this.id = id;
	}

	/**
	 * Indicates if there is a set of terms defined for a given locale.
	 * @param locId the locale to query.
	 * @return true if there is a set of terms defined for the given locale.
	 */
	public boolean hasLocale (LocaleId locId) {
		return (langs.get(locId) != null);
	}

	public LangEntry getEntries (LocaleId locId) {
		return langs.get(locId);
	}

	public void addTerm (LocaleId locId,
		String term)
	{
		// Get the existing language entry if possible
		LangEntry lent = getEntries(locId);
		// Create one if there is none yet
		if ( lent == null ) {
			lent = new LangEntry(locId); 
			langs.put(locId, lent);
		}
		// Add the term
		lent.addTerm(term);
	}

	public void addLangEntry (LangEntry lent) {
		langs.put(lent.getLocale(), lent);
	}
	
	public void removeEntries (LocaleId locId) {
		langs.remove(locId);
	}

}
