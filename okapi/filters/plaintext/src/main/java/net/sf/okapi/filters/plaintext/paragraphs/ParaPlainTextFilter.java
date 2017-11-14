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

package net.sf.okapi.filters.plaintext.paragraphs;

import java.util.LinkedList;

import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.filters.plaintext.base.BasePlainTextFilter;
import net.sf.okapi.lib.extra.filters.AbstractLineFilter;
import net.sf.okapi.lib.extra.filters.TextProcessingResult;

/**
 *  The filter breaks text into paragraphs and sends them as text units.
 *  The filter is aware of the following line terminators:
 * <ul><li>Carriage return character followed immediately by a newline character ("\r\n")
 * <li>Newline (line feed) character ("\n")
 * <li>Stand-alone carriage return character ("\r")</ul><p> 
 * 
 * @version 0.1, 09.06.2009
 */
public class ParaPlainTextFilter extends BasePlainTextFilter{

	public static final String FILTER_NAME			= "okf_plaintext_paragraphs";	
	public static final String FILTER_CONFIG		= "okf_plaintext_paragraphs";
	public static final String FILTER_CONFIG_LINES	= "okf_plaintext_paragraphs_lines";
	
	private Parameters params; // Plain Text Filter's parameters
	private LinkedList<TextContainer> bufferedLines;
	private boolean merging = false;
	
//	public void component_create() {
//		
//		super.component_create();
		
	public ParaPlainTextFilter() {
		
		setName(FILTER_NAME);
		setParameters(new Parameters());	// Para Plain Text Filter parameters

		addConfiguration(true,
				FILTER_CONFIG,
				"Plain Text (Paragraphs)",
				"Text files extracted by paragraphs (separated by 1 or more empty lines).", 
				"okf_plaintext_paragraphs.fprm");
		
		addConfiguration(false,
				FILTER_CONFIG_LINES,
				"Plain Text (Lines)",
				"Text files extracted by lines (each line is a text unit).", 
				"okf_plaintext_paragraphs_lines.fprm");
	}

	@Override
	protected void component_init() {
		
		// Commons, should be included in all descendants introducing own params
		params = getParameters(Parameters.class);	// Throws OkapiBadFilterParametersException		
		super.component_init();		// Have the ancestor initialize its part in params  
		
		// Initialization		
		if (!params.extractParagraphs) return;
			
		if (bufferedLines == null) 
			bufferedLines = new LinkedList<TextContainer>();
		else
			bufferedLines.clear();
	}
	
	@Override
	protected TextProcessingResult component_exec(TextContainer lineContainer) {
		
		if (bufferedLines == null || !params.extractParagraphs) return super.component_exec(lineContainer);

		if ( !lineContainer.isEmpty() ) {
//ys		if (!TextUnitUtil.isEmpty(lineContainer)) {
			
			merging = true;
			
			bufferedLines.add(lineContainer);
			return TextProcessingResult.DELAYED_DECISION;
		}
		else {
			// Empty line, merge accumulated lines and send out
			
			if (merging) {
				
				merging = false;
				return (mergeLines(true)) ? TextProcessingResult.ACCEPTED : TextProcessingResult.REJECTED;
			}
			
			return super.component_exec(lineContainer);
		}				
	}

	
	@Override
	protected void component_idle(boolean lastChance) {
		
		if (merging) mergeLines(false);
		
		super.component_idle(lastChance);
	}

	@Override
	protected void component_done() {
		
//		if (merging) mergeLines();
		
		if (bufferedLines != null && params.extractParagraphs) 
			bufferedLines.clear();
		
		merging = false;
		
		super.component_done();
	}

	private boolean mergeLines(boolean addLinebreak) {
		
		if (!params.extractParagraphs) return false; 			
		if (bufferedLines == null) return false; 
		if (bufferedLines.isEmpty()) return false;
						
		TextContainer mergedLine = new TextContainer();
		// We can use getFirstPartContent() because nothing is segmented
		TextFragment tf = mergedLine.getFirstContent();
		
		while (bufferedLines.size() > 0) {
			
			TextContainer curLine = bufferedLines.poll();
					
			if ( mergedLine.isEmpty() ) { // Paragraph's first line
				mergedLine.setProperty(curLine.getProperty(AbstractLineFilter.LINE_NUMBER));
			}
			else {
		
				switch (params.wrapMode) {
				
				case PLACEHOLDERS:
					tf.append(new Code(TagType.PLACEHOLDER, "line break", getLineBreak()));
					break;
					
				case SPACES:
					tf.append(' ');
					break;
					
				case NONE:
				default:
					tf.append('\n');
				}				
			}
			
			// We can use getFirstPartContent() because nothing is segmented
			tf.append(curLine.getFirstContent());
		}
		
		sendAsSource(mergedLine);
		
		if (addLinebreak) 
			//sendSkeletonPart(getLineBreak());
			sendAsSkeleton(getLineBreak());
				
		return true;		
	}
	
}
