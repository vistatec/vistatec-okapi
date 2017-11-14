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

package net.sf.okapi.applications.rainbow.utilities.extraction;

import java.util.UUID;

import net.sf.okapi.applications.rainbow.packages.xliff.Options;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.StringParameters;

public class Parameters extends StringParameters {
	private static final String PKGTYPE = "pkgType";
	private static final String CREATEZIP = "createZip";
	private static final String PKGNAME = "pkgName";
	private static final String OUTPUTFOLDER = "outputFolder";
	private static final String PRESEGMENT = "preSegment";
	private static final String SOURCESRX = "sourceSRX";
	private static final String TARGETSRX = "targetSRX";
	private static final String PRETRANSLATE = "preTranslate";
	private static final String USEFILENAME = "useFileName";
	private static final String USEGROUPNAME = "useGroupName";
	private static final String PROTECTACCEPTED = "protectAccepted";
	private static final String THRESHOLD = "threshold";
	private static final String TRANSRESCLASS = "transResClass";
	private static final String USETRANSRES2 = "useTransRes2";
	private static final String TRANSRESCLASS2 = "transResClass2";
	private static final String TRANSRESPARAMS = "transResParams";
	private static final String TRANSRESPARAMS2 = "transResParams2";

	private IParameters xliffOptions;

	public Parameters () {
		super();
	}
	
	public void reset() {
		super.reset();
		setPkgType("xliff");
		setCreateZip(false);
		setPkgName("pack1");
		setOutputFolder("${ProjDir}");
		setPreSegment(false);
		setSourceSRX("");
		setTargetSRX("");
		setPreTranslate(false);
		setUseFileName(false);
		setUseGroupName(false);
		setProtectAccepted(true);
		setThreshold(95);
		setTransResClass("net.sf.okapi.connectors.simpletm.SimpleTMConnector");
		setUseTransRes2(false);
		setTransResClass2("net.sf.okapi.connectors.apertium.ApertiumMTConnector");
		
		xliffOptions = new Options();
		setTransResParams(null);
		setTransResParams2(null);
	}

	public String getPkgType() {
		return getString(PKGTYPE);
	}

	public void setPkgType(String pkgType) {
		setString(PKGTYPE, pkgType);
	}

	public boolean getCreateZip() {
		return getBoolean(CREATEZIP);
	}

	public void setCreateZip(boolean createZip) {
		setBoolean(CREATEZIP, createZip);
	}

	public String getPkgName() {
		return getString(PKGNAME);
	}

	public void setPkgName(String pkgName) {
		setString(PKGNAME, pkgName);
	}

	public String getOutputFolder() {
		return getString(OUTPUTFOLDER);
	}

	public void setOutputFolder(String outputFolder) {
		setString(OUTPUTFOLDER, outputFolder);
	}

	public boolean getPreSegment() {
		return getBoolean(PRESEGMENT);
	}

	public void setPreSegment(boolean preSegment) {
		setBoolean(PRESEGMENT, preSegment);
	}

	public String getSourceSRX() {
		return getString(SOURCESRX);
	}

	public void setSourceSRX(String sourceSRX) {
		setString(SOURCESRX, sourceSRX);
	}

	public String getTargetSRX() {
		return getString(TARGETSRX);
	}

	public void setTargetSRX(String targetSRX) {
		setString(TARGETSRX, targetSRX);
	}

	public boolean getPreTranslate() {
		return getBoolean(PRETRANSLATE);
	}

	public void setPreTranslate(boolean preTranslate) {
		setBoolean(PRETRANSLATE, preTranslate);
	}

	public boolean getUseFileName() {
		return getBoolean(USEFILENAME);
	}

	public void setUseFileName(boolean useFileName) {
		setBoolean(USEFILENAME, useFileName);
	}

	public boolean getUseGroupName() {
		return getBoolean(USEGROUPNAME);
	}

	public void setUseGroupName(boolean useGroupName) {
		setBoolean(USEGROUPNAME, useGroupName);
	}

	public boolean getProtectAccepted() {
		return getBoolean(PROTECTACCEPTED);
	}

	public void setProtectAccepted(boolean protectAccepted) {
		setBoolean(PROTECTACCEPTED, protectAccepted);
	}

	public int getThreshold() {
		return getInteger(THRESHOLD);
	}

	public void setThreshold(int threshold) {
		setInteger(THRESHOLD, threshold);
	}

	public String getTransResClass() {
		return getString(TRANSRESCLASS);
	}

	public void setTransResClass(String transResClass) {
		setString(TRANSRESCLASS, transResClass);
	}

	public boolean getUseTransRes2() {
		return getBoolean(USETRANSRES2);
	}

	public void setUseTransRes2(boolean useTransRes2) {
		setBoolean(USETRANSRES2, useTransRes2);
	}

	public String getTransResClass2() {
		return getString(TRANSRESCLASS2);
	}

	public void setTransResClass2(String transResClass2) {
		setString(TRANSRESCLASS2, transResClass2);
	}

	public IParameters getXliffOptions() {
		return xliffOptions;
	}

	public void setXliffOptions(IParameters xliffOptions) {
		this.xliffOptions = xliffOptions;
	}

	public String getTransResParams() {
		return getGroup(TRANSRESPARAMS);
	}

	public void setTransResParams(String transResParams) {
		setGroup(TRANSRESPARAMS, transResParams);
	}

	public String getTransResParams2() {
		return getGroup(TRANSRESPARAMS2);
	}

	public void setTransResParams2(String transResParams2) {
		setGroup(TRANSRESPARAMS2, transResParams2);
	}

	public void fromString(String data) {
		super.fromString(data);
		
		xliffOptions.fromString(buffer.getGroup("xliffOptions"));
	}

	public String toString() {
		buffer.setGroup("xliffOptions", xliffOptions.toString());
		return buffer.toString();
	}
	
	public String makePackageID () {
		return UUID.randomUUID().toString();
	}

}
