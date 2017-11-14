/*===========================================================================
  Copyright (C) 2009-2011 by the Okapi Framework contributors
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

package net.sf.okapi.steps.segmentation;

import java.io.InputStream;

import net.sf.okapi.common.ReferenceParameter;
import net.sf.okapi.common.StringParameters;

public class Parameters extends StringParameters {
	
	public static int TRIM_DEFAULT = -1;
	public static int TRIM_NO = 0;
	public static int TRIM_YES = 1;
	
	public enum SegmStrategy {
		KEEP_EXISTING,
		
		OVERWRITE_EXISTING,
		
		DEEPEN_EXISTING
	}
	
	private static final String FORCESEGMENTEDOUTPUT = "forceSegmentedOutput";
	private static final String OVERWRITESEGMENTATION = "overwriteSegmentation";
	private static final String DEEPENSEGMENTATION = "deepenSegmentation";
	private static final String SOURCESRXPATH = "sourceSrxPath";
	private static final String TARGETSRXPATH = "targetSrxPath";
	private static final String RENUMBERCODES = "renumberCodes";
	private static final String SEGMENTSOURCE = "segmentSource";
	private static final String SEGMENTTARGET = "segmentTarget";
	private static final String COPYSOURCE = "copySource";
	private static final String CHECKSEGMENTS = "checkSegments";
	private static final String TRIMSRCLEADINGWS = "trimSrcLeadingWS";
	private static final String TRIMSRCTRAILINGWS = "trimSrcTrailingWS";
	private static final String TRIMTRGLEADINGWS = "trimTrgLeadingWS";
	private static final String TRIMTRGTRAILINGWS = "trimTrgTrailingWS";
	private static final String TREATISOLATEDCODESASWS = "treatIsolatedCodesAsWhitespace";
	private InputStream sourceSrxStream;
	private InputStream targetSrxStream;
	
	public Parameters () {
		super();
	}
	
	public void reset() {
		super.reset();
		setSegmentSource(true);
		setSegmentTarget(false);
		setRenumberCodes(false);
		setSourceSrxPath("");
		setTargetSrxPath("");
		setCopySource(true);
		setCheckSegments(false);
		setTrimSrcLeadingWS(TRIM_DEFAULT);
		setTrimSrcTrailingWS(TRIM_DEFAULT);
		setTrimTrgLeadingWS(TRIM_DEFAULT);
		setTrimTrgTrailingWS(TRIM_DEFAULT);
		setForcesegmentedOutput(true);
		setOverwriteSegmentation(false);
		setDeepenSegmentation(false);
		setTreatIsolatedCodesAsWhitespace(false);
	}
	
	public boolean getOverwriteSegmentation() {
		return getBoolean(OVERWRITESEGMENTATION);
	}
	
	public void setOverwriteSegmentation(boolean overwriteSegmentation) {
		setBoolean(OVERWRITESEGMENTATION, overwriteSegmentation);
	}

	public boolean getDeepenSegmentation() {
		return getBoolean(DEEPENSEGMENTATION);
	}
	
	public void setDeepenSegmentation(boolean deepenSegmentation) {
		setBoolean(DEEPENSEGMENTATION, deepenSegmentation);
	}

	public boolean getForcesegmentedOutput () {
		return getBoolean(FORCESEGMENTEDOUTPUT);
	}

	public void setForcesegmentedOutput (boolean forceSegmentedOutput) {
		setBoolean(FORCESEGMENTEDOUTPUT, forceSegmentedOutput);
	}
	
	public void setSourceSrxPath (String sourceSrxPath) {
		setString(SOURCESRXPATH, sourceSrxPath);
	}
		
	@ReferenceParameter
	public String getSourceSrxPath () {
		return getString(SOURCESRXPATH);
	}
	
	public void setTargetSrxPath (String targetSrxPath) {
		setString(TARGETSRXPATH, targetSrxPath);
	}
		
	@ReferenceParameter
	public String getTargetSrxPath () {
		return getString(TARGETSRXPATH);
	}

	public boolean getRenumberCodes() {
		return getBoolean(RENUMBERCODES);
	}
	
	public void setRenumberCodes(boolean renumberCodes) {
		setBoolean(RENUMBERCODES, renumberCodes);
	}

	public boolean getSegmentSource() {
		return getBoolean(SEGMENTSOURCE);
	}

	public void setSegmentSource(boolean segmentSource) {
		setBoolean(SEGMENTSOURCE, segmentSource);
	}

	public boolean getSegmentTarget() {
		return getBoolean(SEGMENTTARGET);
	}

	public void setSegmentTarget(boolean segmentTarget) {
		setBoolean(SEGMENTTARGET, segmentTarget);
	}

	public boolean getCopySource() {
		return getBoolean(COPYSOURCE);
	}

	public void setCopySource(boolean copySource) {
		setBoolean(COPYSOURCE, copySource);
	}

	public boolean getCheckSegments() {
		return getBoolean(CHECKSEGMENTS);
	}

	public void setCheckSegments(boolean checkSegments) {
		setBoolean(CHECKSEGMENTS, checkSegments);
	}

	public int getTrimSrcLeadingWS() {
		return getInteger(TRIMSRCLEADINGWS);
	}

	public void setTrimSrcLeadingWS(int trimSrcLeadingWS) {
		setInteger(TRIMSRCLEADINGWS, trimSrcLeadingWS);
	}

	public int getTrimSrcTrailingWS() {
		return getInteger(TRIMSRCTRAILINGWS);
	}

	public void setTrimSrcTrailingWS(int trimSrcTrailingWS) {
		setInteger(TRIMSRCTRAILINGWS, trimSrcTrailingWS);
	}

	public int getTrimTrgLeadingWS() {
		return getInteger(TRIMTRGLEADINGWS);
	}

	public void setTrimTrgLeadingWS(int trimTrgLeadingWS) {
		setInteger(TRIMTRGLEADINGWS, trimTrgLeadingWS);
	}

	public int getTrimTrgTrailingWS() {
		return getInteger(TRIMTRGTRAILINGWS);
	}

	public void setTrimTrgTrailingWS(int trimTrgTrailingWS) {
		setInteger(TRIMTRGTRAILINGWS, trimTrgTrailingWS);
	}

	public boolean isTreatIsolatedCodesAsWhitespace() {
		return getBoolean(TREATISOLATEDCODESASWS);
	}

	public void setTreatIsolatedCodesAsWhitespace(boolean value) {
		setBoolean(TREATISOLATEDCODESASWS, value);
	}
	
	public SegmStrategy getSegmentationStrategy() {
		if (!getOverwriteSegmentation() && getDeepenSegmentation())
			return SegmStrategy.DEEPEN_EXISTING;
		
		else if (getOverwriteSegmentation())
			return SegmStrategy.OVERWRITE_EXISTING;
		
		else
			return SegmStrategy.KEEP_EXISTING;
	}
	
	public void setSegmentationStrategy(SegmStrategy strategy) {
		if (strategy == SegmStrategy.DEEPEN_EXISTING) {
			setOverwriteSegmentation(false);
			setDeepenSegmentation(true);
		}
		else if (strategy == SegmStrategy.OVERWRITE_EXISTING) {
			setOverwriteSegmentation(true);
			setDeepenSegmentation(false);
		}
		else {
			setOverwriteSegmentation(false);
			setDeepenSegmentation(false);
		}
	}

	public InputStream getTargetSrxStream() {
		return targetSrxStream;
	}

	public void setTargetSrxStream(InputStream targetSrxStream) {
		this.targetSrxStream = targetSrxStream;
	}

	public InputStream getSourceSrxStream() {
		return sourceSrxStream;
	}

	public void setSourceSrxStream(InputStream sourceSrxStream) {
		this.sourceSrxStream = sourceSrxStream;
	}
}
