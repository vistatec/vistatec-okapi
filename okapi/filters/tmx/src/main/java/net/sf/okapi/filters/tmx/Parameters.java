/*===========================================================================
  Copyright (C) 2008-2010 by the Okapi Framework contributors
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
============================================================================*/

package net.sf.okapi.filters.tmx;

import net.sf.okapi.common.ISimplifierRulesParameters;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.encoder.XMLEncoder;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.core.simplifierrules.ParseException;
import net.sf.okapi.core.simplifierrules.SimplifierRules;

public class Parameters extends StringParameters implements ISimplifierRulesParameters {

	final static String SEGTYPE= "segType";
	final static String PROCESSALLTARGETS = "processAllTargets";
	final static String CONSOLIDATEDPSKELETON = "consolidateDpSkeleton";
	final static String EXITONINVALID = "exitOnInvalid";
	static final String PROPVALUESEP = "propValueSep";


	public Parameters () {
		super();
	}	

	public void reset() {
		super.reset();
		setEscapeGT(false);
		setProcessAllTargets(true);
		setConsolidateDpSkeleton(true);
		setExitOnInvalid(false);
		setSegType(TmxFilter.SEGTYPE_OR_SENTENCE);
		setPropValueSep(", ");
		setSimplifierRules(null);
	}
	
	public boolean getProcessAllTargets () {
		return getBoolean(PROCESSALLTARGETS);
	}

	public void setProcessAllTargets (boolean processAllTargets) {
		setBoolean(PROCESSALLTARGETS, processAllTargets);
	}

	public boolean getConsolidateDpSkeleton() {
		return getBoolean(CONSOLIDATEDPSKELETON);
	}

	public void setConsolidateDpSkeleton (boolean consolidateDpSkeleton) {
		setBoolean(CONSOLIDATEDPSKELETON, consolidateDpSkeleton);
	}

	public boolean getEscapeGT () {
		return getBoolean(XMLEncoder.ESCAPEGT);
	}

	public void setEscapeGT (boolean escapeGT) {
		setBoolean(XMLEncoder.ESCAPEGT, escapeGT);
	}

	public boolean getExitOnInvalid () {
		return getBoolean(EXITONINVALID);
	}

	public void setExitOnInvalid  (boolean exitOnInvalid) {
		setBoolean(EXITONINVALID, exitOnInvalid);
	}

	public int getSegType () {
		return getInteger(SEGTYPE);
	}
	
	public void setSegType (int segType) {
		setInteger(SEGTYPE, segType);
	}
	
	public String getPropValueSep() {
		return getString(PROPVALUESEP);
	}
	
	public void setPropValueSep(String sep) {
		setString(PROPVALUESEP, sep);
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
	
	@Override
	public ParametersDescription getParametersDescription () {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(XMLEncoder.ESCAPEGT, "Escape the greater-than characters", null);
		desc.add(PROCESSALLTARGETS, "Read all target entries", null);
		desc.add(CONSOLIDATEDPSKELETON, "Group all document parts skeleton into one", null);
		desc.add(EXITONINVALID, "Exit when encountering invalid <tu>s (default is to skip invalid <tu>s).", null);
		desc.add(SEGTYPE, "Creates or not a segment for the extracted <Tu>", null);
		desc.add(PROPVALUESEP, "String used to delimit property values when there are duplicate properties", null);
		return desc;
	}

}
