/*===========================================================================
  Copyright (C) 2010-2013 by the Okapi Framework contributors
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

package net.sf.okapi.lib.verification;

import java.net.URI;
import java.util.List;

import net.sf.okapi.common.Util;
import net.sf.okapi.common.annotation.GenericAnnotation;
import net.sf.okapi.common.annotation.GenericAnnotationType;
import net.sf.okapi.common.annotation.IssueType;
import net.sf.okapi.common.resource.Code;

public class Issue extends GenericAnnotation {

	public static final int DISPSEVERITY_LOW = 0;
	public static final int DISPSEVERITY_MEDIUM = 1;
	public static final int DISPSEVERITY_HIGH = 2;

	public static final double SEVERITY_LOW = 1.0;
	public static final double SEVERITY_MEDIUM = 50.0;
	public static final double SEVERITY_HIGH = 100.0;
	
	private URI docURI;
	private String subDocId;
	private IssueType issueType;
	private String tuId;
	private String segId;
	private String tuName;
	private int trgStart;
	private int trgEnd;
	private List<Code> codes;
	private String source;
	private String target;
	private int dispSeverity;
	
	public Issue (URI docId,
		String subDocId,
		IssueType issueType,
		String tuId,
		String segId,
		String message, 
		int srcStart, 
		int srcEnd, 
		int trgStart, 
		int trgEnd,
		double severity,
		String tuName)
	{
		super(GenericAnnotationType.LQI);
		this.docURI = docId;
		this.subDocId = subDocId;
		this.issueType = issueType;
		this.tuId = tuId;
		this.segId = segId;
		setString(GenericAnnotationType.LQI_COMMENT, message);
		setInteger(GenericAnnotationType.LQI_XSTART, srcStart);
		setInteger(GenericAnnotationType.LQI_XEND, srcEnd);
		this.trgStart = trgStart;
		this.trgEnd = trgEnd;
		this.dispSeverity = severityToDisplaySeverity(severity);
		setDouble(GenericAnnotationType.LQI_SEVERITY, severity);
		this.tuName = tuName;
	}
		
	public URI getDocumentURI () {
		return docURI;
	}
	
	public String getSubDocumentId () {
		return subDocId;
	}

	public IssueType getIssueType () {
		return issueType;
	}
		
	public String getITSType () {
		return getString(GenericAnnotationType.LQI_TYPE);
	}
		
	public String getTuId () {
		return tuId;
	}
	
	public String getTuName () {
		return tuName;
	}
	
	public String getSegId () {
		return segId;
	}
	
	public int getSourceStart () {
		return getInteger(GenericAnnotationType.LQI_XSTART);
	}
		
	public int getSourceEnd () {
		return getInteger(GenericAnnotationType.LQI_XEND);
	}
		
	public int getTargetStart () {
		return trgStart;
	}
		
	public int getTargetEnd () {
		return trgEnd;
	}
	
	public boolean getEnabled () {
		return getBoolean(GenericAnnotationType.LQI_ENABLED);
	}
	
	public void setEnabled (boolean enabled) {
		setBoolean(GenericAnnotationType.LQI_ENABLED, enabled);
	}
	
	public int getDisplaySeverity () {
		return dispSeverity;
	}
	
	public double getSeverity () {
		return getDouble(GenericAnnotationType.LQI_SEVERITY);
	}
	
	public String getMessage () {
		return getString(GenericAnnotationType.LQI_COMMENT);
	}
	
	public List<Code> getCodes () {
		return codes;
	
	}
	public void setCodes (List<Code> codes) {
		this.codes = codes;
	}
	
	public String getSource () {
		return source;
	}
		
	public void setSource (String source) {
		this.source = source;
	}
		
	public String getTarget () {
		return target;
	}
		
	public void setTarget (String target) {
		this.target = target;
	}
		
	String getSignature () {
		return String.format("%s-%s-%s-%s-%d-%s", docURI, subDocId, tuId,
			(segId==null) ? "" : segId, getInteger(GenericAnnotationType.LQI_XSTART), issueType);
	}

	/**
	 * Gets the string representation of the issue.
	 * <p><b>TEST ONLY</b>: The representation in raw XML (ITS 2.0 QA error element).
	 */
	@Override
	public String toString () {
		StringBuilder tmp = new StringBuilder();
		tmp.append("<qaItem");
		tmp.append(" docUri=\""+Util.escapeToXML(docURI.getPath(), 3, false, null)+"\"");
		tmp.append(" tuId=\""+Util.escapeToXML(tuId, 3, false, null)+"\"");
		tmp.append(" segId=\""+Util.escapeToXML(segId, 3, false, null)+"\"");
		tmp.append(" tuName=\""+(tuName!=null ? Util.escapeToXML(tuName, 3, false, null) : "")+"\"");
		tmp.append(String.format(" srcStart=\"%d\" srcEnd=\"%d\"", getInteger(GenericAnnotationType.LQI_XSTART), getInteger(GenericAnnotationType.LQI_XEND)));
		tmp.append(String.format(" trgStart=\"%d\" trgEnd=\"%d\"", trgStart, trgEnd));
		tmp.append(String.format(" severity=\"%d\"", dispSeverity));
		tmp.append("><qaNote>"+Util.escapeToXML(getString(GenericAnnotationType.LQI_COMMENT), 0, false, null)+"<qaNote>");
		tmp.append("</qaItem>");
		return tmp.toString();
	}

	public static int severityToDisplaySeverity (double value) {
		if ( value < 33.33 ) return DISPSEVERITY_LOW;
		if (( value >= 33.33 ) && ( value < 66.33 )) return DISPSEVERITY_MEDIUM;
		return DISPSEVERITY_HIGH;
	}

	public static double displaySeverityToSeverity (int value) {
		if (value == DISPSEVERITY_LOW) return SEVERITY_LOW;
		if (value == DISPSEVERITY_MEDIUM) return SEVERITY_MEDIUM;
		return SEVERITY_HIGH;
	}

}
