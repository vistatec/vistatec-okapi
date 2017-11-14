/*===========================================================================
  Copyright (C) 2013 by the Okapi Framework contributors
-----------------------------------------------------------------------------
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

package net.sf.okapi.common.annotation;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.Util;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.TextFragment.TagType;

public class IssueAnnotation extends GenericAnnotation {

	private static final String CODES_SEP = "\u2628";
	
	private IssueType issueType = IssueType.OTHER;
	
	public IssueAnnotation () {
		super(GenericAnnotationType.LQI);
		setBoolean(GenericAnnotationType.LQI_ENABLED, true);
	}
	
	public IssueAnnotation (IssueType issueType,
		String comment,
		double severity,
		String segId,
		int srcStart, 
		int srcEnd, 
		int trgStart, 
		int trgEnd,
		List<Code> codes)
	{
		this();
		setIssueType(issueType);
		setString(GenericAnnotationType.LQI_COMMENT, comment);
		setSeverity(severity);
		setSourcePosition(srcStart, srcEnd);
		setTargetPosition(trgStart, trgEnd);
		setString(GenericAnnotationType.LQI_XSEGID, segId);
		setCodes(codes);
	}

	public IssueAnnotation (GenericAnnotation ann) {
		super(GenericAnnotationType.LQI);
		setBoolean(GenericAnnotationType.LQI_ENABLED, ann.getBoolean(GenericAnnotationType.LQI_ENABLED));
		String xtype = ann.getString(GenericAnnotationType.LQI_XTYPE);
		if ( xtype == null ) {
			setString(GenericAnnotationType.LQI_TYPE, ann.getString(GenericAnnotationType.LQI_TYPE));
		}
		else {
			this.issueType = IssueType.valueOf(xtype);
			setString(GenericAnnotationType.LQI_TYPE, IssueType.mapToITS(issueType));
		}
		setComment(ann.getString(GenericAnnotationType.LQI_COMMENT));
		setSeverity(ann.getDouble(GenericAnnotationType.LQI_SEVERITY));
		setProfileRef(ann.getString(GenericAnnotationType.LQI_PROFILEREF));
		
		setSourcePosition(ann.getInteger(GenericAnnotationType.LQI_XSTART), ann.getInteger(GenericAnnotationType.LQI_XEND));
		setTargetPosition(ann.getInteger(GenericAnnotationType.LQI_XTRGSTART), ann.getInteger(GenericAnnotationType.LQI_XTRGEND));
		setString(GenericAnnotationType.LQI_XSEGID, ann.getString(GenericAnnotationType.LQI_XSEGID));
		setString(GenericAnnotationType.LQI_XCODES, ann.getString(GenericAnnotationType.LQI_XCODES));
	}
	
	public IssueType getIssueType () {
		return issueType;
	}
	
	/**
	 * Sets the issue type and its corresponding ITS type mapping.
	 * To override the default mapping, use {@link #setITSType(String)}.
	 * @param issueType the issue type to set.
	 */
	public void setIssueType (IssueType issueType) {
		this.issueType = issueType;
		setString(GenericAnnotationType.LQI_XTYPE, issueType.toString());
		setString(GenericAnnotationType.LQI_TYPE, IssueType.mapToITS(issueType));
	}
		
	public String getITSType () {
		return getString(GenericAnnotationType.LQI_TYPE);
	}
	
	public void setITSType (String itsType) {
		setString(GenericAnnotationType.LQI_TYPE, itsType);
	}
	
	public String getSegId () {
		return getString(GenericAnnotationType.LQI_XSEGID);
	}

	public void setSegId (String segId) {
		setString(GenericAnnotationType.LQI_XSEGID, segId);
	}
	
	public int getSourceStart () {
		Integer res = getInteger(GenericAnnotationType.LQI_XSTART);
		return (res==null) ? 0 : res;
	}
		
	public int getSourceEnd () {
		Integer res = getInteger(GenericAnnotationType.LQI_XEND);
		return (res==null) ? -1 : res;
	}
		
	public void setSourcePosition (Integer start,
		Integer end)
	{
		setInteger(GenericAnnotationType.LQI_XSTART, (start == null ? 0 : start));
		setInteger(GenericAnnotationType.LQI_XEND, (end == null ? -1 : end));
	}
		
	public int getTargetStart () {
		Integer res = getInteger(GenericAnnotationType.LQI_XTRGSTART);
		return (res==null) ? 0 : res;
	}
		
	public int getTargetEnd () {
		Integer res = getInteger(GenericAnnotationType.LQI_XTRGEND);
		return (res==null) ? -1 : res;
	}
	
	public void setTargetPosition (Integer start,
		Integer end)
	{
		setInteger(GenericAnnotationType.LQI_XTRGSTART, (start == null ? 0 : start));
		setInteger(GenericAnnotationType.LQI_XTRGEND, (end == null ? -1 : end));
	}
	
	public boolean getEnabled () {
		Boolean res = getBoolean(GenericAnnotationType.LQI_ENABLED);
		return (res==null) ? false : res;
	}
	
	public void setEnabled (boolean enabled) {
		setBoolean(GenericAnnotationType.LQI_ENABLED, enabled);
	}
	
	public double getSeverity () {
		Double res = getDouble(GenericAnnotationType.LQI_SEVERITY);
		return (res==null) ? 0.0 : res;
	}

	public void setSeverity (double severity) {
		if (( severity < 0.0 ) || ( severity > 100.0 )) {
			throw new InvalidParameterException("Invalid severity value.");
		}
		setDouble(GenericAnnotationType.LQI_SEVERITY, severity);
	}
	
	public String getComment () {
		return getString(GenericAnnotationType.LQI_COMMENT);
	}
	
	public void setComment (String comment) {
		setString(GenericAnnotationType.LQI_COMMENT, comment);
	}
	
	/**
	 * Gets the string list of the codes for this issue.
	 * @return the string list of the codes for this issue.
	 */
	public String getCodes () {
		return getString(GenericAnnotationType.LQI_XCODES);
	}
	
	public String[] getCodesAsArray () {
		String tmp = getString(GenericAnnotationType.LQI_XCODES);
		if ( tmp == null ) return null;
		return tmp.split(CODES_SEP, 0);
	}
	
	public List<Code> getCodesAsList () {
		String tmp = getString(GenericAnnotationType.LQI_XCODES);
		if ( tmp == null ) return null;
		String[] dataArray = tmp.split(CODES_SEP, 0);
		ArrayList<Code> list = new ArrayList<Code>();
		for ( String data : dataArray ) {
			// We create fake codes, only the data matter
			list.add(new Code(TagType.PLACEHOLDER, "none", data));
		}
		return list;
	}
	
	public void setCodes (List<Code> codes) {
		if ( Util.isEmpty(codes) ) {
			setString(GenericAnnotationType.LQI_XCODES, null);
		}
		else {
			StringBuilder values = new StringBuilder();
			for ( Code code : codes ) {
				values.append(CODES_SEP);
				values.append(code.getData());
			}
			// Final string is like this: "data1<sep>data2<sep>data3"
			setString(GenericAnnotationType.LQI_XCODES, values.toString().substring(1));
		}
	}
	
	public void setCodes (String codes) {
		setString(GenericAnnotationType.LQI_XCODES, codes);
	}
	
	public String getProfileRef () {
		return getString(GenericAnnotationType.LQI_PROFILEREF);
	}
	
	public void setProfileRef (String profileRef) {
		setString(GenericAnnotationType.LQI_PROFILEREF, profileRef);
	}
	
	@Override
	public String toString () {
		setString("lqiXIssueType", issueType.toString());
		return super.toString();
	}
	
	@Override
	public void fromString (String storage) {
		super.fromString(storage);
		issueType = IssueType.valueOf(getString("lqiXIssueType"));
		setString("lqiXIssueType", null);
	}

}
