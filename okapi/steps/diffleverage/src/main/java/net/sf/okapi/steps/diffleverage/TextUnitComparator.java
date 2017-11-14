/*===========================================================================
  Copyright (C) 2010-2011 by the Okapi Framework contributors
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

package net.sf.okapi.steps.diffleverage;

import java.util.Comparator;

import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.TextUnit;

/**
 * Compare two source {@link TextUnit}s. Text, isReferent() and (optionally) codes must all be same in order to be a
 * match.
 * 
 * @author HARGRAVEJE
 * 
 */
public class TextUnitComparator implements Comparator<ITextUnit> {
	private boolean codeSensitive;

	public TextUnitComparator(final boolean codeSensitive) {
		this.codeSensitive = codeSensitive;
	}

	public void setCodeSensitive(final boolean codeSensitive) {
		this.codeSensitive = codeSensitive;
	}

	public boolean isCodeSensitive() {
		return codeSensitive;
	}

	@Override
	public int compare(final ITextUnit oldTextUnit, final ITextUnit newTextUnit) {
		if (oldTextUnit.isReferent() && !newTextUnit.isReferent()) {
			return -1; // old is greater than new
			// (not sure what greater means in this case but we have to return something)
		} else if (!oldTextUnit.isReferent() && newTextUnit.isReferent()) {
			return 1; // new is greater than old
			// (not sure what greater means in this case but we have to return something)
		} else {
			// both are either referents or not
			return oldTextUnit.getSource().compareTo(newTextUnit.getSource(), codeSensitive);
		}
	}
}
