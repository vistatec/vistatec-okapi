/*===========================================================================
  Copyright (C) 2008 by the Okapi Framework contributors
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

package net.sf.okapi.applications.rainbow.utilities.alignment;

import net.sf.okapi.common.StringParameters;

public class Parameters extends StringParameters {
	
	private static final String CREATETMX = "createTMX";
	private static final String TMXPATH = "tmxPath";
	private static final String USETRADOSWORKAROUNDS = "useTradosWorkarounds";
	private static final String CREATETM = "createTM";
	private static final String SEGMENT = "segment";
	private static final String SIMPLETMPATH = "simpletmPath";
	private static final String SOURCESRXPATH = "sourceSrxPath";
	private static final String TARGETSRXPATH = "targetSrxPath";
	private static final String CHECKSINGLESEGUNIT = "checkSingleSegUnit";
	private static final String USEAUTOCORRECTION = "useAutoCorrection";
	private static final String CREATEATTRIBUTES = "createAttributes";
	private static final String ATTRIBUTES = "attributes";
	private static final String USEEXCLUSION = "useExclusion";
	private static final String EXCLUSION = "exclusion";
	private static final String CREATETMXFORUNKNOWN = "createTMXForUnknown";
	private static final String TMXFORUNKNOWNPATH = "tmxForUnknownPath";
	private static final String MTKEY = "mtKey";

	public boolean getSegment() {
		return getBoolean(SEGMENT);
	}

	public void setSegment(boolean segment) {
		setBoolean(SEGMENT, segment);
	}

	public String getSourceSrxPath() {
		return getString(SOURCESRXPATH);
	}

	public void setSourceSrxPath(String sourceSrxPath) {
		setString(SOURCESRXPATH, sourceSrxPath);
	}

	public String getTargetSrxPath() {
		return getString(TARGETSRXPATH);
	}

	public void setTargetSrxPath(String targetSrxPath) {
		setString(TARGETSRXPATH, targetSrxPath);
	}

	public boolean getCheckSingleSegUnit() {
		return getBoolean(CHECKSINGLESEGUNIT);
	}

	public void setCheckSingleSegUnit(boolean checkSingleSegUnit) {
		setBoolean(CHECKSINGLESEGUNIT, checkSingleSegUnit);
	}

	public boolean getUseAutoCorrection() {
		return getBoolean(USEAUTOCORRECTION);
	}

	public void setUseAutoCorrection(boolean useAutoCorrection) {
		setBoolean(USEAUTOCORRECTION, useAutoCorrection);
	}

	public boolean getCreateTMX() {
		return getBoolean(CREATETMX);
	}

	public void setCreateTMX(boolean createTMX) {
		setBoolean(CREATETMX, createTMX);
	}

	public String getTmxPath() {
		return getString(TMXPATH);
	}

	public void setTmxPath(String tmxPath) {
		setString(TMXPATH, tmxPath);
	}

	public boolean getUseTradosWorkarounds() {
		return getBoolean(USETRADOSWORKAROUNDS);
	}

	public void setUseTradosWorkarounds(boolean useTradosWorkarounds) {
		setBoolean(USETRADOSWORKAROUNDS, useTradosWorkarounds);
	}

	public boolean getCreateTM() {
		return getBoolean(CREATETM);
	}

	public void setCreateTM(boolean createTM) {
		setBoolean(CREATETM, createTM);
	}

	public String getTmPath() {
		return getString(SIMPLETMPATH);
	}

	public void setTmPath(String tmPath) {
		setString(SIMPLETMPATH, tmPath);
	}

	public boolean getCreateAttributes() {
		return getBoolean(CREATEATTRIBUTES);
	}

	public void setCreateAttributes(boolean createAttributes) {
		setBoolean(CREATEATTRIBUTES, createAttributes);
	}

	public String getAttributes() {
		return getString(ATTRIBUTES);
	}

	public void setAttributes(String attributes) {
		setString(ATTRIBUTES, attributes);
	}

	public boolean getUseExclusion() {
		return getBoolean(USEEXCLUSION);
	}

	public void setUseExclusion(boolean useExclusion) {
		setBoolean(USEEXCLUSION, useExclusion);
	}

	public String getExclusion() {
		return getString(EXCLUSION);
	}

	public void setExclusion(String exclusion) {
		setString(EXCLUSION, exclusion);
	}

	public boolean getCreateTMXForUnknown() {
		return getBoolean(CREATETMXFORUNKNOWN);
	}

	public void setCreateTMXForUnknown(boolean createTMXForUnknown) {
		setBoolean(CREATETMXFORUNKNOWN, createTMXForUnknown);
	}

	public String getTmxForUnknownPath() {
		return getString(TMXFORUNKNOWNPATH);
	}

	public void setTmxForUnknownPath(String tmxForUnknownPath) {
		setString(TMXFORUNKNOWNPATH, tmxForUnknownPath);
	}

	public String getMtKey() {
		return getString(MTKEY);
	}

	public void setMtKey(String mtKey) {
		setString(MTKEY, mtKey);
	}

	public Parameters () {
		super();
	}
	
	public void reset () {
		super.reset();
		setCreateTMX(true);
		setTmxPath("");
		setCreateTMXForUnknown(false);
		setTmxForUnknownPath("");
		setUseTradosWorkarounds(true);
		setCreateTM(false);
		setTmPath("");
		setSegment(false);
		setSourceSrxPath("");
		setTargetSrxPath("");
		setCheckSingleSegUnit(true);
		setUseAutoCorrection(true);
		setCreateAttributes(true);
		setAttributes("Txt::FileName=${filename}\nTxt::GroupName=${resname}");
		setUseExclusion(false);
		setExclusion("");
		setMtKey("");
	}
}
