/*===========================================================================
  Copyright (C) 2014 by the Okapi Framework contributors
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

package net.sf.okapi.steps.ttxsplitter;

import java.text.BreakIterator;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;

public class WordCounter {

	private final BreakIterator breaker;
	
	public WordCounter (LocaleId locId) {
		breaker = BreakIterator.getWordInstance(locId.toJavaLocale());
	}
	public long getWordCount (String text) {
		if ( Util.isEmpty(text) ) return 0;
		long res = 0;
		breaker.setText(text);
		int start = breaker.first();
		for ( int end=breaker.next(); end!=BreakIterator.DONE; start=end, end=breaker.next() ) {
			for ( int i=start; i<end; i++) {
                if ( Character.isLetterOrDigit(text.codePointAt(i)) ) {
                	res++;
                	break;
                }
            }
		}
		return res;
	}

}
