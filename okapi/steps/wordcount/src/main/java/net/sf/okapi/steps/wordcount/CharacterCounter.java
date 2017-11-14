/*===========================================================================
  Copyright (C) 2015 by the Okapi Framework contributors
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

package net.sf.okapi.steps.wordcount;

import java.text.Normalizer;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.resource.ISegments;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.IWithAnnotations;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextUnitUtil;
import net.sf.okapi.steps.wordcount.common.BaseCounter;
import net.sf.okapi.steps.wordcount.common.GMX;
import net.sf.okapi.steps.wordcount.common.Metrics;
import net.sf.okapi.steps.wordcount.common.MetricsAnnotation;

/**
 * Character Count engine. Contains static methods to calculate number of characters in a given text fragment. 
 * 
 */

public class CharacterCounter extends BaseCounter {
	
	private static final CharacterCounter counter = new CharacterCounter();
	
	public static class Counts {
		public final long total;
		public final long whiteSpace;
		public final long punctuation;
		
		public static Counts of(long total, long whiteSpace, long punctuation) {
			return new Counts(total, whiteSpace, punctuation);
		}
		
		public Counts() {
			this.total = 0;
			this.whiteSpace = 0;
			this.punctuation = 0;
		}
		
		public Counts(long total, long whiteSpace, long punctuation) {
			this.total = total;
			this.whiteSpace = whiteSpace;
			this.punctuation = punctuation;
		}
		
		public Counts add(Counts other) {
			return new Counts(this.total + other.total,
					this.whiteSpace + other.whiteSpace,
					this.punctuation + other.punctuation);
		}
		
		public boolean isAllZeros() {
			return this.total == 0L &&
					this.whiteSpace == 0L &&
					this.punctuation == 0L;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (obj.getClass() != this.getClass()) {
				return false;
			}
			Counts o = (Counts) obj;
			return this.total == o.total &&
					this.whiteSpace == o.whiteSpace &&
					this.punctuation == o.punctuation;
		}
		
		@Override
		public String toString() {
			return String.format("%d total, %d whitespace, %d punctuation",
					this.total, this.whiteSpace, this.punctuation);
		}
	}
	
	public Counts doFullCount(Object text, LocaleId language) {
		
		if (text == null) return new Counts();
		if (Util.isNullOrEmpty(language)) return new Counts();
		
		if (text instanceof ITextUnit) {		
			ITextUnit tu = (ITextUnit)text;
			return doFullCount(tu.getSource(), language);
		} 
		else if (text instanceof Segment) {
			Segment seg = (Segment) text;
			return doFullCount(seg.getContent(), language);
		}
		else if (text instanceof TextContainer) {
			// This work on segments' content (vs. parts' content)
			TextContainer tc = (TextContainer) text;
			Counts res = new Counts();
			for ( Segment seg : tc.getSegments() ) {
				res = res.add(doFullCount(seg, language));
			}
			return res;
		}
		else if (text instanceof TextFragment) {			
			TextFragment tf = (TextFragment) text;
			
			return doFullCount(TextUnitUtil.getText(tf), language);
		}
		else if (text instanceof String) {						
			return doFullCountImpl((String) text, language);
		}
		
		return new Counts();		
	}
	
	@Override
	protected long doCountImpl(String text, LocaleId language) {
		if (text == null) {
			return 0L;
		}
		return doFullCount(text, language).total;
	}
	
	protected Counts doFullCountImpl(String text, LocaleId language) {
		if (text == null) {
			return new Counts();
		}
		
		if (!Normalizer.isNormalized(text, Normalizer.Form.NFC)) {
            text = Normalizer.normalize(text, Normalizer.Form.NFC);
		}
		
		long total = 0;
		long whiteSpace = 0;
		long punctuation = 0;
		for (int cp, cc, i = 0, len = text.length(); i < len; i += cc) {
			cp = text.codePointAt(i);
			cc = Character.charCount(cp);
			// GMX TotalCharacterCount excludes whitespace. Definition is same as Java's.
			if (Character.isWhitespace(cp)) {
				whiteSpace++;
				continue;
			}
			boolean isInWord = false;
			if (i > 0 && i < len - cc) {
				int prev = text.codePointBefore(i);
				int next = text.codePointAt(i + cc);
				isInWord = Character.isLetterOrDigit(prev) && Character.isLetterOrDigit(next);
			}
			// Punctuation characters are excluded, but hyphens and apostrophes are included
			// if they appear inside of a word.
			if (isPunctuation(cp) && !(isInWord && (isHyphen(cp) || isApostrophe(cp)))) {
				punctuation++;
				continue;
			}
			total++;
		}
		
		return new Counts(total, whiteSpace, punctuation);
	}
	
	private static boolean isApostrophe(int codePoint) {
		return codePoint == '\'' || codePoint == '\u2019';
	}
	
	private static boolean isHyphen(int codePoint) {
		return codePoint == '\u002D' || codePoint == '\u2010' ||
				codePoint == '\u058A' || codePoint == '\u30A0';
	}
	
	private static boolean isPunctuation(int codePoint) {
		return (codePoint >= '\u0021' && codePoint <= '\u002F') ||
				(codePoint >= '\u003A' && codePoint <= '\u0040') ||
				(codePoint >= '\u005B' && codePoint <= '\u0060') ||
				(codePoint >= '\u007B' && codePoint <= '\u007E') ||
				(codePoint >= '\u2000' && codePoint <= '\u206F') ||
				(codePoint >= '\u3000' && codePoint <= '\u303F') ||
				"\u00F7\u00D7\u00A1\u00BF\u0589\u05C3\u05BE\u05C0\u061B".indexOf(codePoint) != -1;
	}
	
	public static void setCount(IWithAnnotations res, long count) {
		MetricsAnnotation ma = res.getAnnotation(MetricsAnnotation.class);
		
		if (ma == null) {			
			ma = new MetricsAnnotation();
			res.setAnnotation(ma);
		}
		
		Metrics m = ma.getMetrics();		
		m.setMetric(GMX.TotalCharacterCount, count);
	}
	
	@Override
	protected String getMetricNameForRetrieval() {
		return GMX.TotalCharacterCount;
	}
	
	public static long count(Object text, LocaleId language) {
		return counter.doCount(text, language);
	}
	
	public static Counts fullCount(Object text, LocaleId language) {
		return counter.doFullCount(text, language);
	}
	
	/**
	 * Returns the character count information stored by CharacterCountStep in the source part of a given text unit. 
	 * @param tu the given text unit
	 * @return number of characters (0 if no character count information found)
	 */
	public static long getCount(ITextUnit tu) {
		return counter.doGetCount(tu);
	}
	
	/**
	 * Returns the character count information stored by CharacterCountStep in a given segment of the source part of a given text unit.
	 * @param tu the given tu
	 * @param segIndex index of the segment in the source
	 * @return number of characters (0 if no character count information found)
	 */
	public static long getCount(ITextUnit tu, int segIndex) {
		ISegments segments = tu.getSource().getSegments();
		return getCount(segments.get(segIndex));		
	}
	
	/**
	 * Returns the character count information stored by CharacterCountStep in a given segment of the source part of a given text unit.
	 * @param segment the given segment
	 * @return number of characters (0 if no character count information found)
	 */
	public static long getCount(Segment segment) {
		return counter.doGetCount(segment);
	}
}
