/*===========================================================================
  Copyright (C) 2008-2010 by the Okapi Framework contributors
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

package net.sf.okapi.filters.plaintext.spliced;

import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.Util;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.common.resource.TextUnitUtil;
import net.sf.okapi.filters.plaintext.base.BasePlainTextFilter;
import net.sf.okapi.lib.extra.filters.AbstractLineFilter;
import net.sf.okapi.lib.extra.filters.TextProcessingResult;

/**
 * 
 * 
 * @version 0.1, 09.06.2009
 */
public class SplicedLinesFilter extends BasePlainTextFilter {

	public static final String FILTER_NAME				= "okf_plaintext_spliced";
	
	public static final String FILTER_CONFIG			= "okf_plaintext_spliced";
	public static final String FILTER_CONFIG_UNDERSCORE	= "okf_plaintext_spliced_underscore";
	public static final String FILTER_CONFIG_BACKSLASH	= "okf_plaintext_spliced_backslash";
	public static final String FILTER_CONFIG_CUSTOM		= "okf_plaintext_spliced_custom";
	
	private Parameters params; 	
	private List<TextContainer> splicedLines;
	private boolean merging = false;

//	public void component_create() {
//		
//		super.component_create();
	public SplicedLinesFilter() {	
	
		setName(FILTER_NAME);
		setParameters(new Parameters());	// Spliced Lines Filter parameters

		addConfiguration(true, 
				FILTER_CONFIG,
				"Spliced Lines",
				"Extracts as one line the consecutive lines with a predefined splicer character at the end.", 
				"okf_plaintext_spliced.fprm"); // Default, the same as FILTER_CONFIG_BACKSLASH
		
		addConfiguration(false, 
				FILTER_CONFIG_BACKSLASH,
				"Spliced Lines (Backslash)",
				"Spliced lines filter with the backslash character (\\) used as the splicer.", 
				"okf_plaintext_spliced_backslash.fprm");

		addConfiguration(false, 
				FILTER_CONFIG_UNDERSCORE,
				"Spliced Lines (Underscore)",
				"Spliced lines filter with the underscore character (_) used as the splicer.", 
				"okf_plaintext_spliced_underscore.fprm");
		
		addConfiguration(false, 
				FILTER_CONFIG_CUSTOM,
				"Spliced Lines (Custom)",
				"Spliced lines filter with a user-defined splicer.", 
				"okf_plaintext_spliced_custom.fprm");
	}
	
	@Override
	protected void component_init() {
		
		// Commons, should be included in all descendants introducing own params
		params = getParameters(Parameters.class);	// Throws OkapiBadFilterParametersException		
		super.component_init();		// Have the ancestor initialize its part in params 

		// Specifics		
		if (splicedLines == null) 
			splicedLines = new ArrayList<TextContainer>();
		else
			splicedLines.clear();		
	}

	@Override
	protected TextProcessingResult component_exec(TextContainer lineContainer) {
	
		if (lineContainer == null) return super.component_exec(lineContainer);
		if (splicedLines == null) return super.component_exec(lineContainer);
		
		//if (TextUnitUtil.getLastChar(lineContainer) == params.splicer) {
		// We can use getFirstPartContent() because nothing is segmented
		if (TextUnitUtil.endsWith(lineContainer.getFirstContent(), params.splicer)) {
			
			merging = true;
			splicedLines.add(lineContainer);
			
			return TextProcessingResult.DELAYED_DECISION;
		}
		else {			
			if (merging) {
				
				merging = false;
				splicedLines.add(lineContainer);
				
				return (mergeLines()) ? TextProcessingResult.ACCEPTED : TextProcessingResult.REJECTED;
			}
				
			return super.component_exec(lineContainer); // Plain text filter's line processing
		}								 								
	}
	
	@Override
	protected void component_idle(boolean lastChance) {
		
		if (merging) mergeLines();
				
		super.component_idle(lastChance);
	}

	@Override
	protected void component_done() {
		
		if (splicedLines != null) 
			splicedLines.clear();
			
		merging = false;
		
		super.component_done();
	}

	private boolean mergeLines() {
		
		if (splicedLines == null) return false; 
		if (splicedLines.isEmpty()) return false;
		if (params == null) return false;
		if (Util.isEmpty(params.splicer)) return false;
						
		TextContainer mergedLine = new TextContainer();
		TextFragment mergedTF = mergedLine.getFirstContent();
		int len = params.splicer.length();
		
		for (TextContainer curLine : splicedLines) {
			// We can use getFirstPartContent() because nothing is segmented
			TextFragment curTF = curLine.getFirstContent();
			
			//TextContainer curLine = splicedLines.poll();
					
//			String s = "";
						
//			int pos = TextUnitUtil.lastIndexOf(curLine, s+= params.splicer);
			// We can use getFirstPartContent() because it is not segmented
			int pos = TextUnitUtil.lastIndexOf(curLine.getFirstContent(), params.splicer);
			if (pos > -1)
				if (params.createPlaceholders) 
					curTF.changeToCode(pos, pos + len, TagType.PLACEHOLDER, "line splicer");
				else
					curTF.remove(pos, pos + len);
			
			if (mergedLine.isEmpty())  // Paragraph's first line
				mergedLine.setProperty(curLine.getProperty(AbstractLineFilter.LINE_NUMBER));
			else 
				if (params.createPlaceholders)
					mergedTF.append(new Code(TagType.PLACEHOLDER, "line break", getLineBreak()));
			
			mergedTF.append(curTF);
		}
		
		sendAsSource(mergedLine);
		splicedLines.clear();
				
		return true;		
	}	
}
