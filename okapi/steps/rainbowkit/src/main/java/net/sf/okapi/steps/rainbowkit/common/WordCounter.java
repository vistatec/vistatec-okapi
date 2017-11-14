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

package net.sf.okapi.steps.rainbowkit.common;

import java.text.BreakIterator;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;

public class WordCounter {

	private final BreakIterator breaker;
	private final boolean replaceApos;
	
	public WordCounter (LocaleId locId) {
		breaker = BreakIterator.getWordInstance(locId.toJavaLocale());
		String lng = locId.getLanguage();
		replaceApos = ( lng.equals("fr") || lng.equals("it") );
	}

	public long getWordCount (ITextUnit tu) {
		return getWordCount(tu.getSource());
	}
	
	public long getWordCount (ITextUnit tu,
		LocaleId trgId)
	{
		if ( !tu.hasTarget(trgId) ) {
			return 0;
		}
		return getWordCount(tu.getTarget(trgId));
	}
	
	public long getWordCount (TextContainer cont) {
		long res = 0;
		for ( Segment seg : cont.getSegments() ) {
			res += getWordCount(prepareFragment(seg.text));
		}
		return res;
	}
	
	public long getWordCount (Segment seg) {
		return getWordCount(prepareFragment(seg.text));
	}
	
	public long getWordCount (TextFragment frag) {
		return getWordCount(prepareFragment(frag));
	}
	
	public long getWordCount (String text) {
		if ( Util.isEmpty(text) ) return 0;
		long res = 0;
		if ( replaceApos ) {
			text = text.replace('\'', ' ');
			text = text.replace('\u2019', ' ');
		}
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
	
	private String prepareFragment (TextFragment tf) {
		String text = tf.getCodedText();
		if ( !tf.hasCode() ) {
			return text;
		}
		// Else: strip the inline codes
		StringBuilder sb = new StringBuilder();
		int startPos = -1;
		for ( int i=0; i<text.length(); i++ ) {
			if ( TextFragment.isMarker(text.charAt(i)) ) {
				if ( i > startPos && startPos >= 0) {
					sb.append(text.substring(startPos, i));
				}
				if ( tf.getCode(text.charAt(++i)).getType().equals(Code.TYPE_LB) ) {
					sb.append('\n');
				}
				startPos = -1;
			}
			else {
				if ( startPos < 0 ) {
					startPos = i;
				}
			}
		}

		if ( startPos < 0 && sb.length() == 0 ) { // Whole string 
			startPos = 0;
		}
		else if ( startPos > -1 && startPos < text.length() ) {
			sb.append(text.substring(startPos));
		}
		
		return sb.toString();
	}
	
}
