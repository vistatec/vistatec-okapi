/*===========================================================================
  Copyright (C) 2009-2011 by the Okapi Framework contributors
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

package net.sf.okapi.steps.codesremoval;

import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextPart;

public class CodesRemover {

	private Parameters params;
	private LocaleId targetLocale;

	public CodesRemover (Parameters params,
		LocaleId targetLocale)
	{
		this.params = params;
		this.targetLocale = targetLocale;
	}

	public void processTextUnit (ITextUnit tu) {
		// Skip non-translatable if requested
		if ( !tu.isTranslatable() ) {
			if ( !params.getIncludeNonTranslatable() ) return;
		}

		// Process source if needed
		if ( params.getStripSource() ) {
			processContainer(tu.getSource());
		}
		
		// Process target if needed
		if ( params.getStripTarget() ) {
			if ( tu.hasTarget(targetLocale) ) {
				processContainer(tu.getTarget(targetLocale));
			}
		}
	}

	public void processContainer (TextContainer tc) {
		for ( TextPart part : tc ) {
			processFragment(part.text);
		}
	}
	
	public void processFragment (TextFragment tf) {
		String text = tf.getCodedText();
		List<Code> codes = tf.getCodes();
		StringBuilder tmp = new StringBuilder();
		ArrayList<Code> remaining = new ArrayList<Code>();

		// Go through the content
		Code code;
		for ( int i=0; i<text.length(); i++) {
			switch ( text.charAt(i) ) {
			case TextFragment.MARKER_OPENING:
			case TextFragment.MARKER_CLOSING:
			case TextFragment.MARKER_ISOLATED:
				// Process codes
				switch ( params.getMode() ) {
				case Parameters.KEEPCODE_REMOVECONTENT:
					code = codes.get(TextFragment.toIndex(text.charAt(++i)));
					code.setData(""); // Remove the code's content
					remaining.add(code); // But keep the code
					tmp.append(text.charAt(i-1));
					tmp.append(TextFragment.toChar(remaining.size()-1));
					break;
				case Parameters.REMOVECODE_KEEPCONTENT:
					code = codes.get(TextFragment.toIndex(text.charAt(++i)));
					tmp.append(code.getData()); // Keep the code's content
					// But remove the code
					break;
				case Parameters.REMOVECODE_REMOVECONTENT:
				default:
					if(params.getReplaceWithSpace()){
						code = codes.get(TextFragment.toIndex(text.charAt(++i)));
						if (isSpacingCandidate(code))
							tmp.append(" ");
					}else{
						i++; // Just skip over index
					}
					break;
				}
				break;
			default:
				// Always preserve other characters
				tmp.append(text.charAt(i));
				break;
			}
		}
		// Set the fragment with the new content
		tf.setCodedText(tmp.toString(), remaining);
	}

	boolean isSpacingCandidate(Code c){
		if(c.getType()!=null && c.getType().equals(Code.TYPE_LB))
			return true;

		String data = c.getData().toLowerCase();
		
		if (data !=null && (data.contains("<br>") 
				|| data.contains("<br />") 
				|| data.contains("<br/>") 
				|| data.contains("\n") 
				|| data.contains("\r")
				|| data.contains("\u0085")     //A next-line character
				|| data.contains("\u2028")		//A line-separator character
				|| data.contains("\u2029")		//A paragraph-separator character
				)) 
			return true;

		return false;
	} 
	
}
