/*===========================================================================
  Copyright (C) 2009-2012 by the Okapi Framework contributors
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

package net.sf.okapi.steps.leveraging;

import net.sf.okapi.common.StringParameters;

public class Parameters extends StringParameters {

	private static final String LEVERAGE = "leverage";
	private static final String NOQUERYTHRESHOLD = "noQueryThreshold";
	private static final String THRESHOLD = "threshold";
	private static final String FILLTARGET = "fillTarget";
	private static final String FILLTARGETTHRESHOLD = "fillTargetThreshold";
	private static final String FILLIFTARGETISEMPTY = "fillIfTargetIsEmpty";
	private static final String FILLIFTARGETISSAMEASSOURCE = "fillIfTargetIsSameAsSource";
	private static final String DOWNGRADEIDENTICALBESTMATCHES = "downgradeIdenticalBestMatches";
	private static final String MAKETMX = "makeTMX";
	private static final String TMXPATH = "tmxPath";
	private static final String USEMTPREFIX = "useMTPrefix";
	private static final String USETARGETPREFIX = "useTargetPrefix";
	private static final String TARGETPREFIX = "targetPrefix";
	private static final String TARGETPREFIXTHRESHOLD = "targetPrefixThreshold";
	private static final String COPYSOURCEONNOTEXT = "copySourceOnNoText";
	private static final String RESOURCECLASSNAME = "resourceClassName";
	private static final String RESOURCEPARAMETERS = "resourceParameters";

	public Parameters () {
		super();
	}
	
	public boolean getFillIfTargetIsEmpty () {
		return getBoolean(FILLIFTARGETISEMPTY);
	}
	
	public void setFillIfTargetIsEmpty (boolean fillIfTargetIsEmpty) {
		setBoolean(FILLIFTARGETISEMPTY, fillIfTargetIsEmpty);
	}
	
	public boolean getFillIfTargetIsSameAsSource () {
		return getBoolean(FILLIFTARGETISSAMEASSOURCE);
	}
	
	public void setFillIfTargetIsSameAsSource (boolean fillIfTargetIsSameAsSource) {
		setBoolean(FILLIFTARGETISSAMEASSOURCE, fillIfTargetIsSameAsSource);
	}

	public boolean getLeverage () {
		return getBoolean(LEVERAGE);
	}
	
	public void setLeverage (boolean leverage) {
		setBoolean(LEVERAGE, leverage);
	}

	public String getResourceClassName () {
		return getString(RESOURCECLASSNAME);
	}

	public void setResourceClassName (String resourceClassName) {
		setString(RESOURCECLASSNAME, resourceClassName);
	}

	public String getResourceParameters () {
		return getGroup(RESOURCEPARAMETERS);
	}

	public void setResourceParameters (String resourceParameters) {
		setGroup(RESOURCEPARAMETERS, resourceParameters);
	}

	public int getNoQueryThreshold () {
		return getInteger(NOQUERYTHRESHOLD);
	}

	public void setNoQueryThreshold (int noQuerythreshold) {
		setInteger(NOQUERYTHRESHOLD, noQuerythreshold);
	}

	public int getThreshold () {
		return getInteger(THRESHOLD);
	}

	public void setThreshold (int threshold) {
		setInteger(THRESHOLD, threshold);
	}

	public boolean getFillTarget () {
		return getBoolean(FILLTARGET);
	}

	public void setFillTarget (boolean fillTarget) {
		setBoolean(FILLTARGET, fillTarget);
	}

	public int getFillTargetThreshold () {
		return getInteger(FILLTARGETTHRESHOLD);
	}

	public void setFillTargetThreshold (int fillTargetThreshold) {
		setInteger(FILLTARGETTHRESHOLD, fillTargetThreshold);
	}

	public boolean getDowngradeIdenticalBestMatches () {
		return getBoolean(DOWNGRADEIDENTICALBESTMATCHES);
	}

	public void setDowngradeIdenticalBestMatches (boolean downgradeIdenticalBestMatches) {
		setBoolean(DOWNGRADEIDENTICALBESTMATCHES, downgradeIdenticalBestMatches);
	}

	public boolean getMakeTMX () {
		return getBoolean(MAKETMX);
	}

	public void setMakeTMX (boolean makeTMX) {
		setBoolean(MAKETMX, makeTMX);
	}

	public boolean getCopySourceOnNoText () {
		return getBoolean(COPYSOURCEONNOTEXT);
	}

	public void setCopySourceOnNoText (boolean copySourceOnNoText) {
		setBoolean(COPYSOURCEONNOTEXT, copySourceOnNoText);
	}

	public String getTMXPath () {
		return getString(TMXPATH);
	}

	public void setTMXPath (String tmxPath) {
		setString(TMXPATH, tmxPath);
	}

	public boolean getUseMTPrefix () {
		return getBoolean(USEMTPREFIX);
	}
	
	public void setUseMTPrefix (boolean useMTPrefix) {
		setBoolean(USEMTPREFIX, useMTPrefix);
	}

	public boolean getUseTargetPrefix () {
		return getBoolean(USETARGETPREFIX);
	}
	
	public void setUseTargetPrefix (boolean useTargetPrefix) {
		setBoolean(USETARGETPREFIX, useTargetPrefix);
	}

	public String getTargetPrefix () {
		return getString(TARGETPREFIX);
	}
	
	public void setTargetPrefix (String targetPrefix) {
		setString(TARGETPREFIX, targetPrefix);
	}

	public int getTargetPrefixThreshold () {
		return getInteger(TARGETPREFIXTHRESHOLD);
	}

	public void setTargetPrefixThreshold (int targetPrefixThreshold) {
		setInteger(TARGETPREFIXTHRESHOLD, targetPrefixThreshold);
	}
	
	@Override
	public void reset() {
		super.reset();
		setLeverage(true);
		setResourceClassName("net.sf.okapi.connectors.pensieve.PensieveTMConnector");
		setResourceParameters(null);
		setNoQueryThreshold(101);
		setThreshold(95);
		setFillTarget(true);
		setFillTargetThreshold(95);
		setFillIfTargetIsEmpty(false);
		setFillIfTargetIsSameAsSource(false);
		setDowngradeIdenticalBestMatches(false);
		setMakeTMX(false);
		setTMXPath("");
		setUseMTPrefix(true);
		setUseTargetPrefix(false);
		setTargetPrefix("FUZZY__");
		setTargetPrefixThreshold(99);
		setCopySourceOnNoText(false);
	}

}
