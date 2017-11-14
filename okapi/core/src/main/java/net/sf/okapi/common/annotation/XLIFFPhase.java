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
 * Annotation representing the &lt;phase&gt; element within an XLIFF &lt;file&gt;&lt;header&gt;.
 * The set of phase elements should be contained within the XLIFFPhaseAnnotation
 * on a StartSubDocument Event.
 */
public class XLIFFPhase {
	// Required
	private String phase_name, process_name;
	// TODO: Optional attribute handling
//	private String company_name, date, job_id, contact_name, contact_email, contact_phone;
//	private XLIFFTool tool;
	// TODO: Handle <note> elements as well.
	private StringBuilder skel = new StringBuilder();

	public XLIFFPhase(String phaseName, String processName) {
		this.phase_name = phaseName;
		this.process_name = processName;
	}

	public String getPhaseName() {
		return this.phase_name;
	}

	public String getProcessName() {
		return this.process_name;
	}

	public void addSkeletonContent(String text) {
		skel.append(text);
	}

	public String toXML() {
		StringBuilder sb = new StringBuilder();
		sb.append("<phase");
		sb.append(" phase-name=\"")
		  .append(Util.escapeToXML(phase_name, 1, false, null)).append("\"");
		sb.append(" process-name=\"")
		  .append(Util.escapeToXML(process_name, 1, false, null)).append("\"");
		sb.append(">");
		sb.append(skel);
		sb.append("</phase>");
		return sb.toString();
	}
}
