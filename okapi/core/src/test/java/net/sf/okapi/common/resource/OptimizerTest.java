/*===========================================================================
  Copyright (C) 2009-2010 by the Okapi Framework contributors
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

import static org.junit.Assert.assertEquals;

import java.util.Stack;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import net.sf.okapi.common.filterwriter.XLIFFContent;
import net.sf.okapi.common.resource.TextFragment.TagType;

@RunWith(JUnit4.class)
public class OptimizerTest {

	@Test
	public void testOptimizer () {
		TextFragment tf = new TextFragment();
		XLIFFContent fmt = new XLIFFContent();

		tf.append("Hello ");
		tf.append(TagType.OPENING, "b1", "<b1>");
		tf.append(TagType.OPENING, "b2", "<b2>");
		tf.append(TagType.PLACEHOLDER, "x1", "<x1/>");
		tf.append("bold");
		tf.append(TagType.CLOSING, "b2", "</b2>");
		tf.append(TagType.CLOSING, "b1", "</b1>");
		tf.append("world");
		assertEquals(5, tf.getCodes().size()); 
		OptimizerTest.optimizeCodes(tf);
		assertEquals(3, tf.getCodes().size()); 
		fmt.setContent(tf);
	}
	
	static private void optimizeCodes (TextFragment frag) {
		Stack<Code> stack = new Stack<Code>();
		String text = frag.getCodedText();;
		char ch;
		Code code;
		int i = 0;
	
		while ( i<text.length() ) {
			int start = -1;
			int end = -1;
			boolean stop = false;
			stack.clear();
			
			while ( i<text.length() ) {
				ch = text.charAt(i);
				if ( TextFragment.isMarker(ch) ) {
					code = frag.getCode(text.charAt(++i));
					switch ( code.getTagType() ) {
					case OPENING:
						stack.push(code);
						if ( start == -1 ) start = i-1;
						else end = i+1;
						break;
					case CLOSING:
						if ( stack.size() == 0 ) {
							if ( start == -1 ) start = i-1;
							else end = i+1;
						}
						else {
							if ( stack.peek().getTagType() == TagType.OPENING ) {
								if ( stack.peek().getType().equals(code.getType()) ) {
									stack.pop();
									if ( start == -1 ) start = i-1;
									else end = i+1;
								}
							}
							// Else: stop here
							stop = true;
						}
						break;
					case PLACEHOLDER:
						stack.push(code);
						if ( start == -1 ) start = i-1;
						// But not yet a end opportunity
						break;
					}
				}
				else {
					if ( start > -1 ) break;
				}
				if ( stop ) break;
				i++;
			}
			
			if (( start > -1 ) && ( end-start > 2 )) {
				i += frag.changeToCode(start, end, TagType.PLACEHOLDER, "group");
				text = frag.getCodedText();
			}
		}
		
		frag.renumberCodes();
	}
}
