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

import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.StartSubDocument;

/**
 * Annotation used to expose the %lt;phase-group&gt; element containing the multiple
 * phases in a %lt;file&gt; element when attached to a StartSubDocument event or
 * resolve the phase-name reference otherwise.
 */
public class XLIFFPhaseAnnotation implements IAnnotation {
	private List<XLIFFPhase> phases = new ArrayList<>();

	/**
	 * Add a dereferenced phase element to the annotation.
	 * @param phase - XLIFFPhase pulled from a StartSubDocument using the phase-name attribute.
	 */
	public void add(XLIFFPhase phase) {
		this.phases.add(phase);
	}

	/**
	 * Add a parsed %lt;phase&gt; element to a StartSubDocument Event.
	 * @param phase the phase element
	 * @param startSubDoc the {@link StartSubDocument} Event
	 */
	public void add(XLIFFPhase phase, StartSubDocument startSubDoc) {
		this.phases.add(phase);
		updatePhaseAnnotation(startSubDoc);
	}

	public XLIFFPhase get(String phaseName) {
		for (XLIFFPhase phase : phases) {
			if (phase.getPhaseName().equals(phaseName)) {
				return phase;
			}
		}
		return null;
	}

	public XLIFFPhase getReferencedPhase() {
		if (phases.size() == 1) {
			return phases.get(0);
		}
		return null;
	}

	public void updatePhaseAnnotation(StartSubDocument startSubDoc) {
		Property phasePlaceholder = (startSubDoc.getProperty(Property.XLIFF_PHASE) == null) ?
			new Property(Property.XLIFF_PHASE, "") : startSubDoc.getProperty(Property.XLIFF_PHASE);
		phasePlaceholder.setValue(toXML());
	}

	public String toXML() {
		StringBuilder sb = new StringBuilder();
		sb.append("<phase-group>");
		for (XLIFFPhase phase : phases) {
			sb.append(phase.toXML());
		}
		sb.append("</phase-group>");
		return sb.toString();
	}
}
