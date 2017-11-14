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

import java.util.Map;
import java.util.TreeMap;

import net.sf.okapi.common.Util;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.StartSubDocument;

/**
 * Annotation containing the set of %lt;tool&gt; elements in an XLIFF %lt;file&gt;%lt;header&gt;.
 * Should be attached to a StartSubDocument Event.
 */
public class XLIFFToolAnnotation implements IAnnotation {
	private Map<String, XLIFFTool> tools = new TreeMap<String, XLIFFTool>(Util.createComparatorHandlingNullKeys(String.class));

	public void add(XLIFFTool tool, StartSubDocument startSubDoc) {
		tools.put(tool.getId(), tool);
		updateToolProperty(startSubDoc);
	}

	public XLIFFTool get(String toolId) {
		return tools.get(toolId);
	}

	/**
	 * Synchronize the value of the tool property on the StartSubDocument
	 * event with the XML representation of this annotation.
	 * @param startSubDoc the {@link StartSubDocument} event
	 */
	public void updateToolProperty(StartSubDocument startSubDoc) {
		Property toolPlaceholder = (startSubDoc.getProperty(Property.XLIFF_TOOL) == null) ?
			new Property(Property.XLIFF_TOOL, "") : startSubDoc.getProperty(Property.XLIFF_TOOL);
		toolPlaceholder.setValue(toXML());
	}

	public String toXML() {
		StringBuilder sb = new StringBuilder();
		for (XLIFFTool tool : tools.values()) {
			sb.append(tool.toXML());
		}
		return sb.toString();
	}
}
