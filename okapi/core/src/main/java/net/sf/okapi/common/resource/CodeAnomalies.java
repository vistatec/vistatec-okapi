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

package net.sf.okapi.common.resource;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Container class for each type of possible code anomaly. 
 * Normal use case is missing or added codes in a target 
 * {@link TextFragment} as compared to its source equivalent.
 * More list types can be added if needed.
 * <p>
 * Returned by {@link TextFragment#alignCodeIds(TextFragment)}
 * @author jimh
 */
public class CodeAnomalies {
	private List<Code> addedCodes;
	private List<Code> missingCodes;

	public CodeAnomalies() {
		addedCodes = new LinkedList<>();
		missingCodes = new LinkedList<>();
	}
	
	public void addMissingCode(Code code) {
		// we don't care if its only an annotation
		if (code.hasOnlyAnnotation()) return;
		missingCodes.add(code);
	}
	
	public void addAddedCode(Code code) {
		// we don't care if its only an annotation
		if (code.hasOnlyAnnotation()) return;
		addedCodes.add(code);
	}
		
	public boolean hasMissingCodes() {
		return !missingCodes.isEmpty();
	}
	
	public boolean hasAddedCodes() {
		return !addedCodes.isEmpty();
	}
	
	public Iterator<Code> getMissingCodesIterator() {
		return missingCodes.listIterator();
	}
	
	public Iterator<Code> getAddedCodesIterator() {
		return addedCodes.listIterator();
	}
	
	public String addedCodesAsString() {
		StringBuffer b = new StringBuffer();
		for (Code c : addedCodes) {
			b.append(c.getData());
			b.append(',');
		}
		if (b.length() > 0) {
			// remove last comma
			b.deleteCharAt(b.length()-1);
		}
		return b.toString();
	}
	
	public String missingCodesAsString() {
		StringBuffer b = new StringBuffer();
		for (Code c : missingCodes) {
			b.append(c.getData());
			b.append(',');
		}
		if (b.length() > 0) {
			// remove last comma
			b.deleteCharAt(b.length()-1);
		}
		return b.toString();
	}
}
