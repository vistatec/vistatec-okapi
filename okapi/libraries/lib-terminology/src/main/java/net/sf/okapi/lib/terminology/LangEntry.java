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

import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.LocaleId;

public class LangEntry extends BaseEntry {
	
	private LocaleId locId;
	private List<TermEntry> terms;

	public LangEntry (LocaleId locId) {
		this.locId = locId;
		terms = new ArrayList<TermEntry>();
	}
	
	public LocaleId getLocale () {
		return locId;
	}

	public LangEntry (TermEntry term) {
		terms = new ArrayList<TermEntry>();
		terms.add(term);
	}
	
	public void addTerm (String term) {
		terms.add(new TermEntry(term));
	}
	
	public void addTerm (TermEntry term) {
		terms.add(term);
	}
	
	public boolean hasTerm () {
		return (terms.size() > 0);
	}

	public TermEntry getTerm (int index) {
		return terms.get(index);
	}

}
