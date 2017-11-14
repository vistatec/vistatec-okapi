/*===========================================================================
 Copyright (C) 2008-2009 by the Okapi Framework contributors
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

package net.sf.okapi.lib.search.lucene.analysis;

import org.apache.lucene.analysis.Token;

/**
 * 
 * @author HargraveJE
 */
@SuppressWarnings("serial")
public class SortableToken extends Token implements Comparable<SortableToken> {	
	
	@Override
	public int compareTo(SortableToken o) {
		SortableToken t = (SortableToken) o;
		if (t.startOffset() < startOffset()) {
			return -1;
		} else if (t.startOffset() > startOffset()) {
			return 1;
		}
		// must be equal
		return 0;
	}
}
