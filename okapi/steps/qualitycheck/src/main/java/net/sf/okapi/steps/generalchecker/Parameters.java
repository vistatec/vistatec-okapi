/*===========================================================================
  Copyright (C) 2016 by the Okapi Framework contributors
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

package net.sf.okapi.steps.generalchecker;

import net.sf.okapi.common.StringParameters;

public class Parameters extends StringParameters {
	  private static final String LEADINGWS = "leadingWS";
	  private static final String TRAILINGWS = "trailingWS";
	  private static final String EMPTYTARGET = "emptyTarget";
	  private static final String EMPTYSOURCE = "emptySource";
	  private static final String TARGETSAMEASSOURCE = "targetSameAsSource";
	  private static final String TARGETSAMEASSOURCE_FORSAMELANGUAGE = "targetSameAsSourceForSameLanguage";
	  private static final String TARGETSAMEASSOURCE_WITHCODES = "targetSameAsSourceWithCodes";
	  private static final String DOUBLEDWORD = "doubledWord";
	  private static final String DOUBLEDWORDEXCEPTIONS = "doubledWordExceptions";

	public Parameters() {
		super();
	}	

	public boolean getDoubledWord() {
		return getBoolean(DOUBLEDWORD);
	}

	public void setDoubledWord(boolean doubledWord) {
		setBoolean(DOUBLEDWORD, doubledWord);
	}

	public String getDoubledWordExceptions() {
		return getString(DOUBLEDWORDEXCEPTIONS);
	}

	public void setDoubledWordExceptions(String doubledWordExceptions) {
		setString(DOUBLEDWORDEXCEPTIONS, doubledWordExceptions);
	}

	public boolean getLeadingWS() {
		return getBoolean(LEADINGWS);
	}

	public void setLeadingWS(boolean leadingWS) {
		setBoolean(LEADINGWS, leadingWS);
	}

	public boolean getTrailingWS() {
		return getBoolean(TRAILINGWS);
	}

	public void setTrailingWS(boolean trailingWS) {
		setBoolean(TRAILINGWS, trailingWS);
	}

	public boolean getEmptyTarget() {
		return getBoolean(EMPTYTARGET);
	}

	public void setEmptyTarget(boolean emptyTarget) {
		setBoolean(EMPTYTARGET, emptyTarget);
	}

	public boolean getEmptySource() {
		return getBoolean(EMPTYSOURCE);
	}

	public void setEmptySource(boolean emptySource) {
		setBoolean(EMPTYSOURCE, emptySource);
	}

	public boolean getTargetSameAsSource() {
		return getBoolean(TARGETSAMEASSOURCE);
	}

	public void setTargetSameAsSource(boolean targetSameAsSource) {
		setBoolean(TARGETSAMEASSOURCE, targetSameAsSource);
	}

	public boolean getTargetSameAsSourceForSameLanguage() {
		return getBoolean(TARGETSAMEASSOURCE_FORSAMELANGUAGE);
	}

	public void setTargetSameAsSourceForSameLanguage(boolean targetSameAsSourceForSameLanguage) {
		setBoolean(TARGETSAMEASSOURCE_FORSAMELANGUAGE, targetSameAsSourceForSameLanguage);
	}

	public boolean getTargetSameAsSourceWithCodes() {
		return getBoolean(TARGETSAMEASSOURCE_WITHCODES);
	}

	public void setTargetSameAsSourceWithCodes(boolean targetSameAsSourceWithCodes) {
		setBoolean(TARGETSAMEASSOURCE_WITHCODES, targetSameAsSourceWithCodes);
	}
	
	@Override
	public void reset() {
		super.reset();	
		setLeadingWS(true);
		setTrailingWS(true);
		setEmptyTarget(true);
		setEmptySource(true);
		setTargetSameAsSource(true);
		setTargetSameAsSourceForSameLanguage(true);
		setTargetSameAsSourceWithCodes(true);
		setDoubledWord(true);
		setDoubledWordExceptions("sie;vous;nous");
	}

	@Override
	public void fromString(String data) {
		super.fromString(data);
	}

	@Override
	public String toString() {		
		return super.toString();
	}
}
