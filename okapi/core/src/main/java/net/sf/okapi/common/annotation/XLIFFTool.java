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

package net.sf.okapi.common.annotation;

import net.sf.okapi.common.Util;

/**
 * Annotation representing the %lt;tool&gt; element within an XLIFF
 * %lt;file&gt;%lt;header&gt;. The set of tool elements should be contained within
 * the XLIFFToolAnnotation on a StartSubDocument Event.
 */
public class XLIFFTool {
	// Required
	private String id, name;
	// Optional
	private String version, company;
	// Content
	private StringBuilder skel = new StringBuilder();

	public XLIFFTool(String toolId, String toolName) {
		this.id = toolId;
		this.name = toolName;
	}

	public String getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public void setVersion(String toolVersion) {
		this.version = toolVersion;
	}

	public void setCompany(String toolCompany) {
		this.company = toolCompany;
	}

	public void addSkeletonContent(String text) {
		skel.append(text);
	}

	public String getSkel() {
		return skel.toString();
	}

	public String getVersion() {
		return version;
	}

	public String getCompany() {
		return company;
	}
	
	public String toXML() {
		StringBuilder sb = new StringBuilder();
		sb.append("<tool");
		sb.append(" tool-id=\"")
		  .append(Util.escapeToXML(id, 1, false, null)).append("\"");
		sb.append(" tool-name=\"")
		  .append(Util.escapeToXML(name, 1, false, null)).append("\"");
		if (version != null) {
			sb.append(" tool-version=\"")
			  .append(Util.escapeToXML(version, 1, false, null)).append("\"");
		}
		if (company != null) {
			sb.append(" tool-company=\"")
			  .append(Util.escapeToXML(company, 1, false, null)).append("\"");
		}
		sb.append(">");
		sb.append(skel);
		sb.append("</tool>");
		return sb.toString();
	}
	
}
