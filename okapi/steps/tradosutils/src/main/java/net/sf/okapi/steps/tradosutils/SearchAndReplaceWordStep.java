/*===========================================================================
  Copyright (C) 2012 by the Okapi Framework contributors
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

package net.sf.okapi.steps.tradosutils;

import java.io.File;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.resource.RawDocument;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;

@UsingParameters(ParametersSearchAndReplaceWord.class)
public class SearchAndReplaceWordStep extends BasePipelineStep{

	private ParametersSearchAndReplaceWord params;
	private String search[];
	private String replace[];
	
	ActiveXComponent oWord;
	
	public SearchAndReplaceWordStep () {
		params = new ParametersSearchAndReplaceWord();
	}

	@Override
	protected Event handleRawDocument (Event event) {

		RawDocument rawDoc = event.getRawDocument();
		
		String inputPath = new File(rawDoc.getInputURI()).getPath();
		
		Dispatch oDocuments = oWord.getProperty("Documents").toDispatch(); 
		Dispatch oDocument = Dispatch.call(oDocuments, "Open", inputPath).toDispatch(); 
	    
	    Dispatch oSelection = oWord.getProperty("Selection").toDispatch(); 
	    Dispatch oFind = Dispatch.call(oSelection, "Find").toDispatch();
	    Dispatch oReplacement = Dispatch.get(oFind, "Replacement").toDispatch(); 

	    Variant f = new Variant(false);
    	Variant t = new Variant(true);
	    
    	for ( int i=0; i<params.rules.size(); i++ ) {
			if ( params.rules.get(i)[0].equals("true") ) {
				
				Boolean hasFormatting = false;
				
				Dispatch.call(oFind, "ClearFormatting");
				String findStyle = params.rules.get(i)[3];
				if(findStyle != null && findStyle.length() > 0 ){
					Dispatch.put(oFind, "Style", findStyle);
					hasFormatting = true;
				}
				
				Dispatch.call(oReplacement, "ClearFormatting");
				String replacementStyle = params.rules.get(i)[4];
				if(replacementStyle != null && replacementStyle.length() > 0 ){
					Dispatch.put(oReplacement, "Style", replacementStyle);
					hasFormatting = true;
				}
				
		    	Dispatch.callN(oFind, "Execute",new Variant[]{
		    			new Variant(params.rules.get(i)[1]), 	/*FindText*/
		    			new Variant(params.getMatchCase()), 	/*MatchCase*/
		    			new Variant(params.getWholeWord()),		/*MatchWholeWord*/
		    			new Variant(params.getRegEx()), 		/*MatchWildcards*/
		    			f,										/*MatchSoundsLike*/
		    			f,										/*MatchAllWordForms*/
		    			t,										/*Forward*/
		    			new Variant(2),							/*Wrap*/
		    			new Variant(hasFormatting),				/*Format*/
		    			new Variant(replace[i]), 				/*ReplaceWith*/
		    			new Variant(2)});						/*Replace*/
			}
		}
    	
		//Dispatch.call(oDocument, "SaveAs", outputPath, new Variant(params.getFormat()));
    	Dispatch.call(oDocument, "Save");
    	Dispatch.call(oDocument, "Close"); 
    	
		return event;
	}

	@Override
	protected Event handleStartBatch(final Event event) {
		
		oWord = new ActiveXComponent("Word.Application");
		oWord.setProperty("Visible", new Variant(true));
		
		// Compile the replacement strings
		search = new String[params.rules.size()];
		for (int i = 0; i < params.rules.size(); i++) {
			search[i] = params.rules.get(i)[1];
		}
		
		// Compile the replacement strings
		replace = new String[params.rules.size()];
		for (int i = 0; i < params.rules.size(); i++) {
			replace[i] = params.rules.get(i)[2];
		}
		
		return event;
	}
	
	@Override
	protected Event handleEndBatch(final Event event) {
		oWord.invoke("Quit");
		oWord = null;
		return event;
	}
	
	@Override
	public String getName() {
		return "MS Word Search and Replace";
	}

	@Override
	public String getDescription() {
		return "Search and Replace in word document."
				+ " Expects: raw document. Sends back: raw document.";
	}
	
	@Override
	public IParameters getParameters () {
		return params;
	}

	@Override
	public void setParameters (IParameters params) {
		this.params = (ParametersSearchAndReplaceWord)params;
	}
}
