/*===========================================================================
  Copyright (C) 2009-2013 by the Okapi Framework contributors
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

package net.sf.okapi.steps.fullwidthconversion;

import java.text.Normalizer;
import java.text.Normalizer.Form;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.IResource;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
import net.sf.okapi.common.pipeline.annotations.StepParameterType;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextPart;

@UsingParameters(Parameters.class)
public class FullWidthConversionStep extends BasePipelineStep {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private Parameters params;
	private LocaleId targetLocale;
	private boolean wasModified;

	public FullWidthConversionStep () {
		params = new Parameters();
	}
	
	@SuppressWarnings("deprecation")
	@StepParameterMapping(parameterType = StepParameterType.TARGET_LOCALE)
	public void setTargetLocale (LocaleId targetLocale) {
		this.targetLocale = targetLocale;
	}
	
	public String getName () {
		return "Full-Width Conversion";
	}

	public String getDescription () {
		return "Convert the text units content of a document to or from full-width characters (zenkaku)."
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
	protected Event handleStartDocument (Event event) {
		wasModified = false;
		return event;
	}

	@Override
	protected Event handleEndDocument (Event event) {
		if ( wasModified ) {
			logger.info("At least one character was converted to {}.", params.getToHalfWidth() ? "half-width" : "full-width");
		}
		return event;
	}

	@Override
	protected Event handleTextUnit (Event event) {
		ITextUnit tu = event.getTextUnit();
		// Skip non-translatable
		if ( !tu.isTranslatable() ) return event;

		TextContainer tc = tu.createTarget(targetLocale, false, IResource.COPY_ALL);
		for ( TextPart part : tc ) {
			processFragment(part.getContent());
		}
		return event;
	}
	
	private void processFragment (TextFragment frag) {
		String text = frag.getCodedText();

		String result = changeWidth(text, params);
		
		// Record if we modified something
		wasModified = !text.equals(result);
		
		// Set back the modified text
		frag.setCodedText(result);
	}
	
	public static String changeWidth(final String text, Parameters params) {
		String initText = text;
		// Some characters are only convertible in decomposed form, e.g.
		// U+30AC KATAKANA LETTER GA
		// has no half-width form but it is expected to convert to
		// U+FF76 HALFWIDTH KATAKANA LETTER KA + U+FF9E HALFWIDTH KATAKANA VOICED SOUND MARK
		// so first we decompose with NFD, which yields the convertible
		// U+30AB KATAKANA LETTER KA + U+3099 COMBINING KATAKANA-HIRAGANA VOICED SOUND MARK
		if ( params.getToHalfWidth() && !Normalizer.isNormalized(text, Form.NFD) ) {
			initText = Normalizer.normalize(text, Form.NFD);
		}
		StringBuilder sb = new StringBuilder(initText);

		int ch;
		if ( params.getToHalfWidth() ) {
			for ( int i=0; i<sb.length(); i++ ) {
				ch = sb.charAt(i);
				if ( TextFragment.isMarker((char)ch) ) {
					i++; // Skip codes
					continue;
				}
				// ASCII
				if (( ch >= 0xFF01 ) && ( ch <= 0xFF5E )) {
					sb.setCharAt(i, (char)(ch-0xFEE0));
					continue;
				}
				if ( ch == 0x3000 ) {
					sb.setCharAt(i, ' ');
				}
				// Hangul
				if (( ch > 0x3131 ) && ( ch <= 0x314E )) {
					sb.setCharAt(i, (char)(ch+0xCE70));
					continue;
				}
				if ( params.getIncludeKatakana() ) {
					switch (ch) {
					// Katakana
					case 0x3002: sb.setCharAt(i, (char)0xFF61); break;
					case 0x300C: sb.setCharAt(i, (char)0xFF62); break;
					case 0x300D: sb.setCharAt(i, (char)0xFF63); break;
					case 0x3001: sb.setCharAt(i, (char)0xFF64); break;
					case 0x30FB: sb.setCharAt(i, (char)0xFF65); break;
					case 0x30F2: sb.setCharAt(i, (char)0xFF66); break;
					case 0x30A1: sb.setCharAt(i, (char)0xFF67); break;
					case 0x30A3: sb.setCharAt(i, (char)0xFF68); break;
					case 0x30A5: sb.setCharAt(i, (char)0xFF69); break;
					case 0x30A7: sb.setCharAt(i, (char)0xFF6A); break;
					case 0x30A9: sb.setCharAt(i, (char)0xFF6B); break;
					case 0x30E3: sb.setCharAt(i, (char)0xFF6C); break;
					case 0x30E5: sb.setCharAt(i, (char)0xFF6D); break;
					case 0x30E7: sb.setCharAt(i, (char)0xFF6E); break;
					case 0x30C3: sb.setCharAt(i, (char)0xFF6F); break;
					case 0x30FC: sb.setCharAt(i, (char)0xFF70); break;
					case 0x30A2: sb.setCharAt(i, (char)0xFF71); break;
					case 0x30A4: sb.setCharAt(i, (char)0xFF72); break;
					case 0x30A6: sb.setCharAt(i, (char)0xFF73); break;
					case 0x30A8: sb.setCharAt(i, (char)0xFF74); break;
					case 0x30AA: sb.setCharAt(i, (char)0xFF75); break;
					case 0x30AB: sb.setCharAt(i, (char)0xFF76); break;
					case 0x30AD: sb.setCharAt(i, (char)0xFF77); break;
					case 0x30AF: sb.setCharAt(i, (char)0xFF78); break;
					case 0x30B1: sb.setCharAt(i, (char)0xFF79); break;
					case 0x30B3: sb.setCharAt(i, (char)0xFF7A); break;
					case 0x30B5: sb.setCharAt(i, (char)0xFF7B); break;
					case 0x30B7: sb.setCharAt(i, (char)0xFF7C); break;
					case 0x30B9: sb.setCharAt(i, (char)0xFF7D); break;
					case 0x30BB: sb.setCharAt(i, (char)0xFF7E); break;
					case 0x30BD: sb.setCharAt(i, (char)0xFF7F); break;
					case 0x30BF: sb.setCharAt(i, (char)0xFF80); break;
					case 0x30C1: sb.setCharAt(i, (char)0xFF81); break;
					case 0x30C4: sb.setCharAt(i, (char)0xFF82); break;
					case 0x30C6: sb.setCharAt(i, (char)0xFF83); break;
					case 0x30C8: sb.setCharAt(i, (char)0xFF84); break;
					case 0x30CA: sb.setCharAt(i, (char)0xFF85); break;
					case 0x30CB: sb.setCharAt(i, (char)0xFF86); break;
					case 0x30CC: sb.setCharAt(i, (char)0xFF87); break;
					case 0x30CD: sb.setCharAt(i, (char)0xFF88); break;
					case 0x30CE: sb.setCharAt(i, (char)0xFF89); break;
					case 0x30CF: sb.setCharAt(i, (char)0xFF8A); break;
					case 0x30D2: sb.setCharAt(i, (char)0xFF8B); break;
					case 0x30D5: sb.setCharAt(i, (char)0xFF8C); break;
					case 0x30D8: sb.setCharAt(i, (char)0xFF8D); break;
					case 0x30DB: sb.setCharAt(i, (char)0xFF8E); break;
					case 0x30DE: sb.setCharAt(i, (char)0xFF8F); break;
					case 0x30DF: sb.setCharAt(i, (char)0xFF90); break;
					case 0x30E0: sb.setCharAt(i, (char)0xFF91); break;
					case 0x30E1: sb.setCharAt(i, (char)0xFF92); break;
					case 0x30E2: sb.setCharAt(i, (char)0xFF93); break;
					case 0x30E4: sb.setCharAt(i, (char)0xFF94); break;
					case 0x30E6: sb.setCharAt(i, (char)0xFF95); break;
					case 0x30E8: sb.setCharAt(i, (char)0xFF96); break;
					case 0x30E9: sb.setCharAt(i, (char)0xFF97); break;
					case 0x30EA: sb.setCharAt(i, (char)0xFF98); break;
					case 0x30EB: sb.setCharAt(i, (char)0xFF99); break;
					case 0x30EC: sb.setCharAt(i, (char)0xFF9A); break;
					case 0x30ED: sb.setCharAt(i, (char)0xFF9B); break;
					case 0x30EF: sb.setCharAt(i, (char)0xFF9C); break;
					case 0x30F3: sb.setCharAt(i, (char)0xFF9D); break;
					case 0x3099: sb.setCharAt(i, (char)0xFF9E); break;
					case 0x309A: sb.setCharAt(i, (char)0xFF9F); break;
					}
				}
				switch ( ch ) {
				// Hangul
				case 0x3164: sb.setCharAt(i, (char)0xFFA0); break;
				case 0x3161: sb.setCharAt(i, (char)0xFFDA); break;
				case 0x3162: sb.setCharAt(i, (char)0xFFDB); break;
				case 0x3163: sb.setCharAt(i, (char)0xFFDC); break;
				// Others
				case 0x2502: sb.setCharAt(i, (char)0xFFE8); break;
				case 0x2190: sb.setCharAt(i, (char)0xFFE9); break;
				case 0x2191: sb.setCharAt(i, (char)0xFFEA); break;
				case 0x2192: sb.setCharAt(i, (char)0xFFEB); break;
				case 0x2193: sb.setCharAt(i, (char)0xFFEC); break;
				case 0x25A0: sb.setCharAt(i, (char)0xFFED); break;
				case 0x25CB: sb.setCharAt(i, (char)0xFFEE); break;
				}

				// Process letter-like symbols
				if ( params.getIncludeLLS() ) {
					switch ( ch ) {
					case 0x2100: sb.setCharAt(i, 'a'); sb.insert(i+1, "/c"); i+=2; break;
					case 0x2101: sb.setCharAt(i, 'a'); sb.insert(i+1, "/s"); i+=2; break;
					case 0x2105: sb.setCharAt(i, 'c'); sb.insert(i+1, "/o"); i+=2; break;
					case 0x2103: sb.setCharAt(i, (char)0x00B0); sb.insert(i+1, "C"); i++; break;
					case 0x2109: sb.setCharAt(i, (char)0x00B0); sb.insert(i+1, "F"); i++; break;
					case 0x2116: sb.setCharAt(i, 'N'); sb.insert(i+1, "o"); i++; break;
					case 0x212A: sb.setCharAt(i, 'K'); break;
					case 0x212B: sb.setCharAt(i, (char)0x00C5); break;
					}
				}
				
				// Stop here if we don't convert Squared Latin Abbreviations 
				if ( !params.getIncludeSLA() ) continue;
				
				switch ( ch ) {
				// Squared Latin Abbreviations 1
				case 0x3371: sb.setCharAt(i, 'h'); sb.insert(i+1, "Pa"); i+=2; break;
				case 0x3372: sb.setCharAt(i, 'd'); sb.insert(i+1, "a"); i++; break;
				case 0x3373: sb.setCharAt(i, 'A'); sb.insert(i+1, "U"); i++; break;
				case 0x3374: sb.setCharAt(i, 'b'); sb.insert(i+1, "ar"); i+=2; break;
				case 0x3375: sb.setCharAt(i, 'o'); sb.insert(i+1, "V"); i++; break;
				case 0x3376: sb.setCharAt(i, 'p'); sb.insert(i+1, "c"); i++; break;
				case 0x3377: sb.setCharAt(i, 'd'); sb.insert(i+1, "m"); i++; break;
				case 0x3378: sb.setCharAt(i, 'd'); sb.insert(i+1, "m\u00B2"); i+=2; break;
				case 0x3379: sb.setCharAt(i, 'd'); sb.insert(i+1, "m\u00B3"); i+=2; break;
				case 0x337A: sb.setCharAt(i, 'I'); sb.insert(i+1, "U"); i++; break;
				// Squared Latin Abbreviations 2
				case 0x3380: sb.setCharAt(i, 'p'); sb.insert(i+1, "A"); i++; break;
				case 0x3381: sb.setCharAt(i, 'n'); sb.insert(i+1, "A"); i++; break;
				case 0x3382: sb.setCharAt(i, (char)0x03BC); sb.insert(i+1, "A"); i++; break;
				case 0x3383: sb.setCharAt(i, 'm'); sb.insert(i+1, "A"); i++; break;
				case 0x3384: sb.setCharAt(i, 'k'); sb.insert(i+1, "A"); i++; break;
				case 0x3385: sb.setCharAt(i, 'K'); sb.insert(i+1, "B"); i++; break;
				case 0x3386: sb.setCharAt(i, 'M'); sb.insert(i+1, "B"); i++; break;
				case 0x3387: sb.setCharAt(i, 'G'); sb.insert(i+1, "B"); i++; break;
				case 0x3388: sb.setCharAt(i, 'c'); sb.insert(i+1, "al"); i+=2; break;
				case 0x3389: sb.setCharAt(i, 'k'); sb.insert(i+1, "cal"); i+=3; break;
				case 0x338A: sb.setCharAt(i, 'p'); sb.insert(i+1, "F"); i++; break;
				case 0x338B: sb.setCharAt(i, 'n'); sb.insert(i+1, "F"); i++; break;
				case 0x338C: sb.setCharAt(i, (char)0x03BC); sb.insert(i+1, "F"); i++; break;
				case 0x338D: sb.setCharAt(i, (char)0x03BC); sb.insert(i+1, "g"); i++; break;
				case 0x338E: sb.setCharAt(i, 'm'); sb.insert(i+1, "g"); i++; break;
				case 0x338F: sb.setCharAt(i, 'k'); sb.insert(i+1, "g"); i++; break;
				case 0x3390: sb.setCharAt(i, 'H'); sb.insert(i+1, "z"); i++; break;
				case 0x3391: sb.setCharAt(i, 'k'); sb.insert(i+1, "Hz"); i+=2; break;
				case 0x3392: sb.setCharAt(i, 'M'); sb.insert(i+1, "Hz"); i+=2; break;
				case 0x3393: sb.setCharAt(i, 'G'); sb.insert(i+1, "Hz"); i+=2; break;
				case 0x3394: sb.setCharAt(i, 'T'); sb.insert(i+1, "Hz"); i+=2; break;
				case 0x3395: sb.setCharAt(i, (char)0x03BC); sb.insert(i+1, "\u2113"); i++; break;
				case 0x3396: sb.setCharAt(i, 'm'); sb.insert(i+1, "\u2113"); i++; break;
				case 0x3397: sb.setCharAt(i, 'd'); sb.insert(i+1, "\u2113"); i++; break;
				case 0x3398: sb.setCharAt(i, 'k'); sb.insert(i+1, "\u2113"); i++; break;
				case 0x3399: sb.setCharAt(i, 'f'); sb.insert(i+1, "m"); i++; break;
				case 0x339A: sb.setCharAt(i, 'n'); sb.insert(i+1, "m"); i++; break;
				case 0x339B: sb.setCharAt(i, (char)0x03BC); sb.insert(i+1, "m"); i++; break;
				case 0x339C: sb.setCharAt(i, 'm'); sb.insert(i+1, "m"); i++; break;
				case 0x339D: sb.setCharAt(i, 'c'); sb.insert(i+1, "m"); i++; break;
				case 0x339E: sb.setCharAt(i, 'k'); sb.insert(i+1, "m"); i++; break;
				case 0x339F: sb.setCharAt(i, 'm'); sb.insert(i+1, "m\u00B2"); i+=2; break;
				case 0x33A0: sb.setCharAt(i, 'c'); sb.insert(i+1, "m\u00B2"); i+=2; break;
				case 0x33A1: sb.setCharAt(i, 'm'); sb.insert(i+1, "\u00B2"); i++; break;
				case 0x33A2: sb.setCharAt(i, 'k'); sb.insert(i+1, "m\u00B2"); i+=2; break;
				case 0x33A3: sb.setCharAt(i, 'm'); sb.insert(i+1, "m\u00B3"); i+=2; break;
				case 0x33A4: sb.setCharAt(i, 'c'); sb.insert(i+1, "m\u00B3"); i+=2; break;
				case 0x33A5: sb.setCharAt(i, 'm'); sb.insert(i+1, "\u00B3"); i++; break;
				case 0x33A6: sb.setCharAt(i, 'k'); sb.insert(i+1, "m\u00B3"); i+=2; break;
				case 0x33A7: sb.setCharAt(i, 'm'); sb.insert(i+1, "/s"); i+=2; break;
				case 0x33A8: sb.setCharAt(i, 'm'); sb.insert(i+1, "/s\u00B2"); i+=3; break;
				case 0x33A9: sb.setCharAt(i, 'P'); sb.insert(i+1, "a"); i++; break;
				case 0x33AA: sb.setCharAt(i, 'k'); sb.insert(i+1, "Pa"); i+=2; break;
				case 0x33AB: sb.setCharAt(i, 'M'); sb.insert(i+1, "Pa"); i+=2; break;
				case 0x33AC: sb.setCharAt(i, 'G'); sb.insert(i+1, "Pa"); i+=2; break;
				case 0x33AD: sb.setCharAt(i, 'r'); sb.insert(i+1, "ad"); i+=2; break;
				case 0x33AE: sb.setCharAt(i, 'r'); sb.insert(i+1, "ad/s"); i+=4; break;
				case 0x33AF: sb.setCharAt(i, 'r'); sb.insert(i+1, "ad/s\u00B2"); i+=5; break;
				case 0x33B0: sb.setCharAt(i, 'p'); sb.insert(i+1, "s"); i++; break;
				case 0x33B1: sb.setCharAt(i, 'n'); sb.insert(i+1, "s"); i++; break;
				case 0x33B2: sb.setCharAt(i, (char)0x03BC); sb.insert(i+1, "s"); i++; break;
				case 0x33B3: sb.setCharAt(i, 'm'); sb.insert(i+1, "s"); i++; break;
				case 0x33B4: sb.setCharAt(i, 'p'); sb.insert(i+1, "V"); i++; break;
				case 0x33B5: sb.setCharAt(i, 'n'); sb.insert(i+1, "V"); i++; break;
				case 0x33B6: sb.setCharAt(i, (char)0x03BC); sb.insert(i+1, "V"); i++; break;
				case 0x33B7: sb.setCharAt(i, 'm'); sb.insert(i+1, "V"); i++; break;
				case 0x33B8: sb.setCharAt(i, 'k'); sb.insert(i+1, "V"); i++; break;
				case 0x33B9: sb.setCharAt(i, 'M'); sb.insert(i+1, "V"); i++; break;
				case 0x33BA: sb.setCharAt(i, 'p'); sb.insert(i+1, "W"); i++; break;
				case 0x33BB: sb.setCharAt(i, 'n'); sb.insert(i+1, "W"); i++; break;
				case 0x33BC: sb.setCharAt(i, (char)0x03BC); sb.insert(i+1, "W"); i++; break;
				case 0x33BD: sb.setCharAt(i, 'm'); sb.insert(i+1, "W"); i++; break;
				case 0x33BE: sb.setCharAt(i, 'k'); sb.insert(i+1, "W"); i++; break;
				case 0x33BF: sb.setCharAt(i, 'M'); sb.insert(i+1, "W"); i++; break;
				case 0x33C0: sb.setCharAt(i, 'k'); sb.insert(i+1, "\u03A9"); i++; break;
				case 0x33C1: sb.setCharAt(i, 'M'); sb.insert(i+1, "\u03A9"); i++; break;
				case 0x33C2: sb.setCharAt(i, 'a'); sb.insert(i+1, ".m."); i+=3; break;
				case 0x33C3: sb.setCharAt(i, 'B'); sb.insert(i+1, "q"); i++; break;
				case 0x33C4: sb.setCharAt(i, 'c'); sb.insert(i+1, "c"); i++; break;
				case 0x33C5: sb.setCharAt(i, 'c'); sb.insert(i+1, "d"); i++; break;
				case 0x33C6: sb.setCharAt(i, 'C'); sb.insert(i+1, "/kg"); i+=3; break;
				case 0x33C7: sb.setCharAt(i, 'C'); sb.insert(i+1, "o."); i+=2; break;
				case 0x33C8: sb.setCharAt(i, 'd'); sb.insert(i+1, "B"); i++; break;
				case 0x33C9: sb.setCharAt(i, 'G'); sb.insert(i+1, "y"); i++; break;
				case 0x33CA: sb.setCharAt(i, 'h'); sb.insert(i+1, "a"); i++; break;
				case 0x33CB: sb.setCharAt(i, 'H'); sb.insert(i+1, "P"); i++; break;
				case 0x33CC: sb.setCharAt(i, 'i'); sb.insert(i+1, "n"); i++; break;
				case 0x33CD: sb.setCharAt(i, 'K'); sb.insert(i+1, "K"); i++; break;
				case 0x33CE: sb.setCharAt(i, 'K'); sb.insert(i+1, "M"); i++; break;
				case 0x33CF: sb.setCharAt(i, 'K'); sb.insert(i+1, "t"); i++; break;
				case 0x33D0: sb.setCharAt(i, 'l'); sb.insert(i+1, "m"); i++; break;
				case 0x33D1: sb.setCharAt(i, 'l'); sb.insert(i+1, "n"); i++; break;
				case 0x33D2: sb.setCharAt(i, 'l'); sb.insert(i+1, "og"); i+=2; break;
				case 0x33D3: sb.setCharAt(i, 'l'); sb.insert(i+1, "x"); i++; break;
				case 0x33D4: sb.setCharAt(i, 'm'); sb.insert(i+1, "b"); i++; break;
				case 0x33D5: sb.setCharAt(i, 'm'); sb.insert(i+1, "il"); i+=2; break;
				case 0x33D6: sb.setCharAt(i, 'm'); sb.insert(i+1, "ol"); i+=2; break;
				case 0x33D7: sb.setCharAt(i, 'p'); sb.insert(i+1, "H"); i++; break;
				case 0x33D8: sb.setCharAt(i, 'p'); sb.insert(i+1, ".m."); i+=3; break;
				case 0x33D9: sb.setCharAt(i, 'P'); sb.insert(i+1, "PM"); i+=2; break;
				case 0x33DA: sb.setCharAt(i, 'P'); sb.insert(i+1, "R"); i++; break;
				case 0x33DB: sb.setCharAt(i, 's'); sb.insert(i+1, "r"); i++; break;
				case 0x33DC: sb.setCharAt(i, 'S'); sb.insert(i+1, "v"); i++; break;
				case 0x33DD: sb.setCharAt(i, 'W'); sb.insert(i+1, "b"); i++; break;
				case 0x33DE: sb.setCharAt(i, 'v'); sb.insert(i+1, "/m"); i+=2; break;
				case 0x33DF: sb.setCharAt(i, 'a'); sb.insert(i+1, "/m"); i+=2; break;
				// Squared Latin Abbreviations 3
				case 0x33FF: sb.setCharAt(i, 'g'); sb.insert(i+1, "al"); i+=2; break;
				}
			}
		}
		else { // To full-width
			for ( int i=0; i<text.length(); i++ ) {
				ch = text.charAt(i);
				if ( TextFragment.isMarker((char)ch) ) {
					i++; // Skip codes
					continue;
				}
				if ( !params.getKatakanaOnly() ) {
					// ASCII
					if (( ch >= 0x0021 ) && ( ch <= 0x007E )) {
						sb.setCharAt(i, (char)(ch+0xFEE0));
						continue;
					}
					if ( ch == ' ' ) {
						sb.setCharAt(i, (char)0x3000);
					}
				}
				// Stop here for ASCII only
				if ( params.getAsciiOnly() ) continue;

				switch ( ch ) {
				// Katakana
				case 0xFF61: sb.setCharAt(i, (char)0x3002); break;
				case 0xFF62: sb.setCharAt(i, (char)0x300C); break;
				case 0xFF63: sb.setCharAt(i, (char)0x300D); break;
				case 0xFF64: sb.setCharAt(i, (char)0x3001); break;
				case 0xFF65: sb.setCharAt(i, (char)0x30FB); break;
				case 0xFF66: sb.setCharAt(i, (char)0x30F2); break;
				case 0xFF67: sb.setCharAt(i, (char)0x30A1); break;
				case 0xFF68: sb.setCharAt(i, (char)0x30A3); break;
				case 0xFF69: sb.setCharAt(i, (char)0x30A5); break;
				case 0xFF6A: sb.setCharAt(i, (char)0x30A7); break;
				case 0xFF6B: sb.setCharAt(i, (char)0x30A9); break;
				case 0xFF6C: sb.setCharAt(i, (char)0x30E3); break;
				case 0xFF6D: sb.setCharAt(i, (char)0x30E5); break;
				case 0xFF6E: sb.setCharAt(i, (char)0x30E7); break;
				case 0xFF6F: sb.setCharAt(i, (char)0x30C3); break;
				case 0xFF70: sb.setCharAt(i, (char)0x30FC); break;
				case 0xFF71: sb.setCharAt(i, (char)0x30A2); break;
				case 0xFF72: sb.setCharAt(i, (char)0x30A4); break;
				case 0xFF73: sb.setCharAt(i, (char)0x30A6); break;
				case 0xFF74: sb.setCharAt(i, (char)0x30A8); break;
				case 0xFF75: sb.setCharAt(i, (char)0x30AA); break;
				case 0xFF76: sb.setCharAt(i, (char)0x30AB); break;
				case 0xFF77: sb.setCharAt(i, (char)0x30AD); break;
				case 0xFF78: sb.setCharAt(i, (char)0x30AF); break;
				case 0xFF79: sb.setCharAt(i, (char)0x30B1); break;
				case 0xFF7A: sb.setCharAt(i, (char)0x30B3); break;
				case 0xFF7B: sb.setCharAt(i, (char)0x30B5); break;
				case 0xFF7C: sb.setCharAt(i, (char)0x30B7); break;
				case 0xFF7D: sb.setCharAt(i, (char)0x30B9); break;
				case 0xFF7E: sb.setCharAt(i, (char)0x30BB); break;
				case 0xFF7F: sb.setCharAt(i, (char)0x30BD); break;
				case 0xFF80: sb.setCharAt(i, (char)0x30BF); break;
				case 0xFF81: sb.setCharAt(i, (char)0x30C1); break;
				case 0xFF82: sb.setCharAt(i, (char)0x30C4); break;
				case 0xFF83: sb.setCharAt(i, (char)0x30C6); break;
				case 0xFF84: sb.setCharAt(i, (char)0x30C8); break;
				case 0xFF85: sb.setCharAt(i, (char)0x30CA); break;
				case 0xFF86: sb.setCharAt(i, (char)0x30CB); break;
				case 0xFF87: sb.setCharAt(i, (char)0x30CC); break;
				case 0xFF88: sb.setCharAt(i, (char)0x30CD); break;
				case 0xFF89: sb.setCharAt(i, (char)0x30CE); break;
				case 0xFF8A: sb.setCharAt(i, (char)0x30CF); break;
				case 0xFF8B: sb.setCharAt(i, (char)0x30D2); break;
				case 0xFF8C: sb.setCharAt(i, (char)0x30D5); break;
				case 0xFF8D: sb.setCharAt(i, (char)0x30D8); break;
				case 0xFF8E: sb.setCharAt(i, (char)0x30DB); break;
				case 0xFF8F: sb.setCharAt(i, (char)0x30DE); break;
				case 0xFF90: sb.setCharAt(i, (char)0x30DF); break;
				case 0xFF91: sb.setCharAt(i, (char)0x30E0); break;
				case 0xFF92: sb.setCharAt(i, (char)0x30E1); break;
				case 0xFF93: sb.setCharAt(i, (char)0x30E2); break;
				case 0xFF94: sb.setCharAt(i, (char)0x30E4); break;
				case 0xFF95: sb.setCharAt(i, (char)0x30E6); break;
				case 0xFF96: sb.setCharAt(i, (char)0x30E8); break;
				case 0xFF97: sb.setCharAt(i, (char)0x30E9); break;
				case 0xFF98: sb.setCharAt(i, (char)0x30EA); break;
				case 0xFF99: sb.setCharAt(i, (char)0x30EB); break;
				case 0xFF9A: sb.setCharAt(i, (char)0x30EC); break;
				case 0xFF9B: sb.setCharAt(i, (char)0x30ED); break;
				case 0xFF9C: sb.setCharAt(i, (char)0x30EF); break;
				case 0xFF9D: sb.setCharAt(i, (char)0x30F3); break;
				case 0xFF9E: sb.setCharAt(i, (char)0x3099); break;
				case 0xFF9F: sb.setCharAt(i, (char)0x309A); break;
				}
				// Stop here for katakana only
				if ( params.getKatakanaOnly() ) continue;
				
				// Hangul
				if (( ch > 0xFFA1 ) && ( ch <= 0xFFBE )) {
					sb.setCharAt(i, (char)(ch-0xCE70));
					continue;
				}
				switch ( ch ) {
				// Hangul
				case 0xFFA0: sb.setCharAt(i, (char)0x3164); break;
				case 0xFFDA: sb.setCharAt(i, (char)0x3161); break;
				case 0xFFDB: sb.setCharAt(i, (char)0x3162); break;
				case 0xFFDC: sb.setCharAt(i, (char)0x3163); break;
				// Others
				case 0xFFE8: sb.setCharAt(i, (char)0x2502); break;
				case 0xFFE9: sb.setCharAt(i, (char)0x2190); break;
				case 0xFFEA: sb.setCharAt(i, (char)0x2191); break;
				case 0xFFEB: sb.setCharAt(i, (char)0x2192); break;
				case 0xFFEC: sb.setCharAt(i, (char)0x2193); break;
				case 0xFFED: sb.setCharAt(i, (char)0x25A0); break;
				case 0xFFEE: sb.setCharAt(i, (char)0x25CB); break;
				}
			}
		}

		String result = sb.toString();
		
		if ( initText.equals(result) ) {
			// No characters were changed. Return the original text so that
			// composition of unrelated characters is not affected.
			return text;
		}
		
		if ( params.getNormalizeOutput() && !Normalizer.isNormalized(result, Form.NFC) ) {
			result = Normalizer.normalize(result, Form.NFC);
		}
		
		return result;
	}

}
