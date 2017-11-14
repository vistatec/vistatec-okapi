/*===========================================================================
  Copyright (C) 2013 by the Okapi Framework contributors
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

package net.sf.okapi.steps.cleanup;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;
import net.sf.okapi.common.uidescription.TextInputPart;

@EditorFor(Parameters.class)
public class Parameters extends StringParameters implements IEditorDescriptionProvider {

    private static final String NORMALIZEQUOTES = "normalizeQuotes";
    private static final String CHECKCHARACTERS = "checkCharacters";
    private static final String MATCHREGEXEXPRESSIONS = "matchRegexExpressions";
    private static final String MATCHUSERREGEX = "matchUserRegex";
    private static final String USERREGEX = "userRegex";
    private static final String PRUNETEXTUNIT = "pruneTextUnit";
    
    public Parameters() {
    	super();
	}

    public void reset() {
		super.reset();
    	setNormalizeQuotes(true);
    	setCheckCharacters(true);
    	setMatchRegexExpressions(true);
    	setMatchUserRegex(true);
    	setUserRegex(null);
    	setPruneTextUnit(true);
    }

    public boolean getNormalizeQuotes() {
    	return getBoolean(NORMALIZEQUOTES);
    }

    public void setNormalizeQuotes(boolean normalizeQuotes) {
    	setBoolean(NORMALIZEQUOTES, normalizeQuotes);
	}

    public boolean getCheckCharacters() {
    	return getBoolean(CHECKCHARACTERS);
    }

    public void setCheckCharacters(boolean checkCharacters) {
    	setBoolean(CHECKCHARACTERS, checkCharacters);
    }

    public boolean getMatchRegexExpressions() {
    	return getBoolean(MATCHREGEXEXPRESSIONS);
    }

    public void setMatchRegexExpressions(boolean matchRegexExpressions) {
    	setBoolean(MATCHREGEXEXPRESSIONS, matchRegexExpressions);
    }

    public boolean getMatchUserRegex() {
    	return getBoolean(MATCHUSERREGEX);
    }

    public void setMatchUserRegex(boolean matchUserRegex) {
    	setBoolean(MATCHUSERREGEX, matchUserRegex);
    }

    public String getUserRegex() {
    	return getString(USERREGEX);
    }

    public void setUserRegex(String userRegex) {
    	setString(USERREGEX, userRegex);
    }

    public boolean getPruneTextUnit() {
    	return getBoolean(PRUNETEXTUNIT);
    }

    public void setPruneTextUnit(boolean pruneTextUnit) {
    	setBoolean(PRUNETEXTUNIT, pruneTextUnit);
    }

    @Override
    public ParametersDescription getParametersDescription() {

        ParametersDescription desc = new ParametersDescription(this);

        desc.add(NORMALIZEQUOTES, "Normalize quotation marks", null);
        desc.add(CHECKCHARACTERS, "Check for corrupt or unexpected characters", null);
        desc.add(MATCHREGEXEXPRESSIONS, "Mark segments matching default regular expressions for removal", null);
        desc.add(MATCHUSERREGEX, "Mark segments matching user defined regular expressions for removal", null);
        desc.add(USERREGEX, "User defined regex string", null);
        desc.add(PRUNETEXTUNIT, "Remove unnecessary segments from text unit", null);

        return desc;
    }

    public EditorDescription createEditorDescription(ParametersDescription paramDesc) {

        EditorDescription desc = new EditorDescription("Cleanup", true, false);

        desc.addCheckboxPart(paramDesc.get(NORMALIZEQUOTES));
        desc.addCheckboxPart(paramDesc.get(MATCHREGEXEXPRESSIONS));
        desc.addCheckboxPart(paramDesc.get(MATCHUSERREGEX));
        TextInputPart tip = desc.addTextInputPart(paramDesc.get(USERREGEX));
        tip.setAllowEmpty(true);
        desc.addCheckboxPart(paramDesc.get(CHECKCHARACTERS));
        desc.addCheckboxPart(paramDesc.get(PRUNETEXTUNIT));

        return desc;
    }
}
