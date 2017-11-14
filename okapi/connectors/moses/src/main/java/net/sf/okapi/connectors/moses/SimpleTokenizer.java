/*===========================================================================
 This code from the project: http://jtmt.sourceforge.net/
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

package net.sf.okapi.connectors.moses;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.StreamUtil;
import net.sf.okapi.common.Util;

import com.ibm.icu.text.BreakIterator;
import com.ibm.icu.text.RuleBasedBreakIterator;

public class SimpleTokenizer {
	private RuleBasedBreakIterator wordIterator = null;

	public SimpleTokenizer(LocaleId locale) {	
		wordIterator = new RuleBasedBreakIterator(StreamUtil.streamAsString(
				SimpleTokenizer.class.getResourceAsStream("/word_break_rules.txt"), "UTF-8"));
	}

	public String tokenize(String text) {
		if (Util.isEmpty(text)) {
			return text;
		}
		
		int index = 0;
		wordIterator.setText(text);
		StringBuffer b = new StringBuffer(text.length());
		for (;;) {
			int end = wordIterator.next();
			if (end == BreakIterator.DONE) {
				break;
			}
			b.append(text.substring(index, end) + " "); 			
			index = end;
		}
		// remove last whitespace
		b.setLength(b.length()-1);
		// remove double\triple white space
		String t = b.toString();
		return t.replaceAll("\\s+", " ");
	}
}
