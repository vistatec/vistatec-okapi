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

package net.sf.okapi.filters.openoffice;

import net.sf.okapi.common.ISimplifierRulesParameters;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.core.simplifierrules.ParseException;
import net.sf.okapi.core.simplifierrules.SimplifierRules;

public class Parameters extends StringParameters implements ISimplifierRulesParameters {
	
	private static final String EXTRACTNOTES = "extractNotes";
	private static final String EXTRACTREFERENCES = "extractReferences";
	private static final String EXTRACTMETADATA = "extractMetadata";
	private static final String ENCODECHARACTERENTITYREFERENCEGLYPHS = "encodeCharacterEntityReferenceGlyphs";
	
	public Parameters () {
		super();
	}
	
	public void reset () {
		super.reset();
		setExtractNotes(false);
		setExtractReferences(false);
		setExtractMetadata(true);
		setEncodeCharacterEntityReferenceGlyphs(true);
		setSimplifierRules(null);
	}

	public boolean getExtractNotes() {
		return getBoolean(EXTRACTNOTES);
	}

	public void setExtractNotes(boolean extractNotes) {
		setBoolean(EXTRACTNOTES, extractNotes);
	}

	public boolean getExtractReferences() {
		return getBoolean(EXTRACTREFERENCES);
	}

	public void setExtractReferences(boolean extractReferences) {
		setBoolean(EXTRACTREFERENCES, extractReferences);
	}

	public boolean getExtractMetadata() {
		return getBoolean(EXTRACTMETADATA);
	}

	public void setExtractMetadata(boolean extractMetadata) {
		setBoolean(EXTRACTMETADATA, extractMetadata);
	}

	public boolean getEncodeCharacterEntityReferenceGlyphs() {
		return getBoolean(ENCODECHARACTERENTITYREFERENCEGLYPHS);
	}

	public void setEncodeCharacterEntityReferenceGlyphs(boolean encodeCharacterEntityReferenceGlyphs) {
		setBoolean(ENCODECHARACTERENTITYREFERENCEGLYPHS, encodeCharacterEntityReferenceGlyphs);
	}
	
	@Override
	public String getSimplifierRules() {
		return getString(SIMPLIFIERRULES);
	}

	@Override
	public void setSimplifierRules(String rules) {
		setString(SIMPLIFIERRULES, rules);		
	}

	@Override
	public void validateSimplifierRules() throws ParseException {
		SimplifierRules r = new SimplifierRules(getSimplifierRules(), new Code());
		r.parse();
    }
}
