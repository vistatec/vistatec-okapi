/*===========================================================================
  Copyright (C) 2009-2012 by the Okapi Framework contributors
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

package net.sf.okapi.steps.textmodification;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.IResource;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
import net.sf.okapi.common.pipeline.annotations.StepParameterType;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextPart;
import net.sf.okapi.common.resource.TextUnitUtil;

@UsingParameters(Parameters.class)
public class TextModificationStep extends BasePipelineStep {

	private static final char STARTSEG = '[';
	private static final char ENDSEG = ']';
	private static final int SCRIPT_MAX = 3;

	private Parameters params;
	private LocaleId targetLocale;
	private String[] oldChars = new String[3];
	private String[] newChars = new String[3];

	public TextModificationStep () {
		params = new Parameters();
		
		oldChars = new String[4];
		newChars = new String[4];
		// Latin extended characters
		oldChars[0] = "AaEeIiOoUuYyCcDdNn";
		newChars[0] = "\u00c2\u00e5\u00c9\u00e8\u00cf\u00ec\u00d8\u00f5\u00db\u00fc\u00dd\u00ff\u00c7\u00e7\u00d0\u00f0"
			+ "\u00d1\u00f1";
		// Cyrillic characters
		oldChars[1] = "AaEeIiOoUuYyBbVvPpKkSsNnDdFfGgHhJjLlMmQqRrTtWwZzCcXx";
		newChars[1] = "\u0410\u0430\u0415\u0435\u0418\u0438\u041e\u043e\u0423\u0443\u042e\u044e\u0411\u0431\u0412\u0432"
			+ "\u041f\u043f\u041a\u043a\u0421\u0441\u041d\u043d\u0414\u0434\u0424\u0444\u0413\u0433\u0425\u0445\u0419\u0439"
			+ "\u041b\u043b\u041c\u043c\u0428\u0448\u0420\u0440\u0422\u0442\u042f\u044f\u0417\u0437\u0426\u0446\u0429\u0449";
		// Arabic characters
		oldChars[2] = "AaBbTtGgHhDdRrMmNnLlQqKkSsVvWwXxYyZzCcFfJjPpEeIiOoUu0123456789%?;,";
		newChars[2] = "\u0627\u0627\u0628\u0628\u062a\u062a\u062c\u062c\u062d\u062d\u062f\u062f\u0631\u0631\u0645\u0645"
			+ "\u0646\u0646\u0644\u0644\u0642\u0642\u0643\u0643\u0633\u0633\u062e\u062e\u0648\u0648\u0632\u0632\u064a\u064a"
			+ "\u0638\u0638\u0635\u0635\u0641\u0641\u063a\u063a\u0630\u0630\u0647\u0647\u0639\u0639\u0636\u0636\u0634\u0634"
			+ "\u0660\u0661\u0662\u0663\u0664\u0665\u0666\u0667\u0668\u0669\u066a\u061f\u061b\u060c";
		// Han characters (Simplified)
		oldChars[3] = "AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz";
		newChars[3] = "\u35F2\u35F2\u3737\u3737\u3DFF\u3DFF\u4039\u4039\u4150\u4150\u42D4\u42D4\u6E26\u6E26\u6E88\u6E88"
			+ "\u6EB3\u6EB3\u6F38\u6F38\u6F70\u6F70\u6FAE\u6FAE\u6FF0\u6FF0\u7121\u7121\u7189\u7189\u71D2\u71D2\u721B\u721B"
			+ "\u7258\u7258\u7372\u7372\u73FC\u73FC\u74DA\u74DA\u7587\u7587\u760D\u760D\u93C7\u93C7\u93F7\u93F7\u9F7E\u9F7E";
	}
	
	@StepParameterMapping(parameterType = StepParameterType.TARGET_LOCALE)
	public void setTargetLocale (LocaleId targetLocale) {
		this.targetLocale = targetLocale;
	}
	
	@Override
	public String getName () {
		return "Text Modification";
	}

	@Override
	public String getDescription () {
		return "Apply various modifications to the text units content of a document."
			+ " Expects: filter events. Sends back: filter events.";
	}

	@Override
	public IParameters getParameters () {
		return params;
	}

	@Override
	public void setParameters (IParameters params) {
		this.params = (Parameters)params;
	}
 
	@Override
	protected Event handleTextUnit (Event event) {
		ITextUnit tu = event.getTextUnit();
		// Skip non-translatable
		if ( !tu.isTranslatable() ) return event;
		// Skip if already translate (only if required)
		if ( !params.getApplyToExistingTarget() && tu.hasTarget(targetLocale) ) return event;
		// Check if we need to apply to blank entries
		if ( !params.getApplyToBlankEntries() ) {
			TextContainer tc = tu.getTarget(targetLocale);
			if ( tc == null ) tc = tu.getSource();
			if ( !tc.hasText() ) return event;
		}

		// Create the target if needed
		tu.createTarget(targetLocale, false, IResource.COPY_ALL);
		// If the target is empty we use the source
		if ( tu.getTarget(targetLocale).isEmpty() ) {
			tu.createTarget(targetLocale, true, IResource.COPY_ALL);
		}
		else if ( !tu.getTarget(targetLocale).hasText() ) {
			// Not empty but has no text: it's likely a copy of the inline codes only
			// use the source in that case
			tu.createTarget(targetLocale, true, IResource.COPY_ALL);
		}

		// Perform the main modification
		switch ( params.getType()) {
		case Parameters.TYPE_XNREPLACE:
			replaceWithXN(tu);
			break;
		case Parameters.TYPE_EXTREPLACE:
			replaceWithExtendedChars(tu);
			break;
		case Parameters.TYPE_KEEPINLINE:
			removeText(tu);
			break;
		}
		
		// Expand if needed
		if ( params.getExpand()) {
			expand(tu);
		}
		
		// Add segment marks if needed
		if ( params.getMarkSegments()) {
			addSegmentMarks(tu);
		}

		// Add prefixes and suffixes to the paragraph if needed
		if ( params.getAddPrefix() || params.getAddSuffix() || params.getAddName() || params.getAddID() ) {
			addText(tu);
		}
		
		return event;
	}

	/**
	 * Removes the text but leaves the inline code.
	 * @param tu the text unit to process.
	 */
	private void removeText (ITextUnit tu) {
		for ( TextPart part : tu.getTarget(targetLocale) ) {
			StringBuilder sb = new StringBuilder();
			// Remove the text inside the part
			String text = part.text.getCodedText();
			for ( int i=0; i<text.length(); i++ ) {
				if ( TextFragment.isMarker(text.charAt(i)) ) {
					// Add the code markers
					sb.append(text.charAt(i));
					sb.append(text.charAt(++i));
				}
				// Else: text, so do nothing
			}
			part.text.setCodedText(sb.toString());
		}
	}	
	
	/**
	 * Replaces letters with Xs and digits with Ns.
	 * @param tu the text unit to process.
	 */
	private void replaceWithXN (ITextUnit tu) {
		String tmp = null;
		for ( TextPart part : tu.getTarget(targetLocale) ) {
			tmp = part.text.getCodedText().replaceAll("\\p{Lu}|\\p{Lo}", "X");
			tmp = tmp.replaceAll("\\p{Ll}", "x");
			tmp = tmp.replaceAll("\\d", "N");
			part.text.setCodedText(tmp);
		}
	}
	
	private void replaceWithExtendedChars (ITextUnit tu) {
		int n;
		int charDest = params.getScript();
		if ( charDest > SCRIPT_MAX ) {
			charDest = 0; // Just making sure
		}
		
		for ( TextPart part : tu.getTarget(targetLocale) ) {
			StringBuilder sb = new StringBuilder(part.text.getCodedText());
			for ( int i=0; i<sb.length(); i++ ) {
				if ( TextFragment.isMarker(sb.charAt(i)) ) {
					i++; // Skip codes
				}
				else {
					if ( (n = oldChars[charDest].indexOf(sb.charAt(i))) > -1 ) {
						sb.setCharAt(i, newChars[charDest].charAt(n));
					}
				}
			}
			part.text.setCodedText(sb.toString());
		}
	}

	private void addSegmentMarks (ITextUnit tu) {
		for ( Segment seg : tu.getTarget(targetLocale).getSegments() ) {
			seg.text.setCodedText(STARTSEG+seg.text.getCodedText()+ENDSEG);
		}
	}
	
	/**
	 * Adds prefix and/or suffix to the target. This method assumes that
	 * the item has gone through the first transformation already.
	 * @param tu The text unit to process.
	 */
	private void addText (ITextUnit tu) {
		if ( params.getAddPrefix() ) {
			TextFragment firstFrag = tu.getTarget(targetLocale).getFirstContent();
			firstFrag.setCodedText(params.getPrefix() + firstFrag.getCodedText());
		}
		TextFragment lastFrag = tu.getTarget(targetLocale).getLastContent();
		if ( params.getAddName() ) {
			String name = tu.getName();
			if ( !Util.isEmpty(name) ) {
				lastFrag.setCodedText(lastFrag.getCodedText() + "_"+name);
			}
			else {
				lastFrag.setCodedText(lastFrag.getCodedText() + "_"+tu.getId());
			}
		}
		if ( params.getAddID() ) {
			lastFrag.setCodedText(lastFrag.getCodedText() + "_"+tu.getId());
		}
		if ( params.getAddSuffix() ) {
			lastFrag.setCodedText(lastFrag.getCodedText() + params.getSuffix());
		}
	}

	private void expand (ITextUnit tu) {
		// Get the total length of the original
		int length = getLength(tu.getSource());
		// Calculate the number of characters to add
		int addition = length; // 100% for long strings
		if ( length <= 20 ) { // 50% (or at least 1 char) for short strings
			addition = (addition+1) / 2; 
		}
		
		// Create the string to add
		StringBuilder extra = new StringBuilder();
		for ( int i=0; i<addition; i++ ) {
			if (( i % 6 == 0 ) && ( i != addition-1 )) {
				extra.append(' ');
			}
			else {
				extra.append('z');
			}
		}
		
		// Add the expansion
		TextFragment frag = tu.getTarget(targetLocale).getLastContent();
		// Add the expansion after the last text (keep inline codes after expansion)
		String ct = frag.getCodedText();
		int p = -1;
		for ( int i=0; i<ct.length(); i++ ) {
			if ( TextFragment.isMarker(ct.charAt(i)) ) {
				p = i;
			}
		}
		if ( p > -1 ) {
			StringBuilder tmp = new StringBuilder(ct);
			tmp.insert(p, extra);
			frag.setCodedText(tmp.toString());
		}
		else {
			frag.setCodedText(ct+extra);
		}
	}
	
	private int getLength (TextContainer tc) {
		TextFragment tf;
		if ( tc.contentIsOneSegment() ) {
			tf = tc.getFirstContent();
		}
		else {
			tf = tc.getUnSegmentedContentCopy();
		}
		return TextUnitUtil.getText(tf).length();
	}

}
