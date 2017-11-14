/*===========================================================================
Copyright (C) 2008-2009 by the Okapi Framework contributors
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

package net.sf.okapi.lib.search.lucene.analysis;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.sf.okapi.common.exceptions.OkapiIOException;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

import com.ibm.icu.lang.UCharacter;

/**
 * 
 * @author HaslamJD
 */
public final class AlphabeticNgramTokenizer extends Tokenizer {
	private static final int NO_CHAR = -1;
	private int ngramLength;
	private String ngramType;
	private int offset;
	private CharTermAttribute termAttribute;
	private OffsetAttribute offsetAttribute;
	private TypeAttribute typeAttribute;

	/**
	 * Instance variable for performance reasons ONLY
	 */
	private List<Character> ngramCache;

	private Locale locale;

	public AlphabeticNgramTokenizer(Reader reader, int ngramLength,
			Locale locale) {
		super(reader);
		if (ngramLength <= 0) {
			throw new IllegalArgumentException(
					"'ngramLength' must be greater than 0");
		}
		if (reader == null) {
			throw new IllegalArgumentException("'reader' cannot be null");
		}
		this.ngramLength = ngramLength;
		this.termAttribute = (CharTermAttribute) addAttribute(CharTermAttribute.class);
		this.offsetAttribute = (OffsetAttribute) addAttribute(OffsetAttribute.class);
		this.typeAttribute = (TypeAttribute) addAttribute(TypeAttribute.class);
		this.locale = locale;
		this.ngramCache = new ArrayList<Character>(ngramLength);
		initializeCache();
		this.ngramType = "ngram(" + getNgramLength() + ")";
		this.offset = 0;
	}

	public Locale getLocale() {
		return locale;
	}

	public int getNgramLength() {
		return ngramLength;
	}
	
	public CharTermAttribute getTermAttribute() {
		return termAttribute;		
	}

	private Reader getReader() {
		return input;
	}

	private void initializeCache() {
		// Clearing for when it's called by reset
		ngramCache.clear();
		
		// add a dummy character as we remove the first character of the cache on each iteration
		ngramCache.add(Character.MIN_VALUE);
		
		// create initial ngram
		int c;
		for (int i = 1; i < ngramLength; i++) {
			try {
				c = getReader().read();
			} catch (IOException ioe) {
				throw new OkapiIOException(ioe.getMessage(), ioe);
			}
			
			if (c == NO_CHAR) {
				break;
			}
		
			offset = 0;
			ngramCache.add((char)c);
		}
	}
	
	private void createToken() {
		// Populate Attributes
		termAttribute.copyBuffer(toLowerCase(ngramCache).toCharArray(), 0, toLowerCase(ngramCache).toCharArray().length);
		offsetAttribute.setOffset(offset, offset + ngramCache.size());
		typeAttribute.setType(ngramType);
	}

	@Override
	public boolean incrementToken() throws IOException {
		clearAttributes();
		int c = getReader().read();
		
		// remove the old intial character
		if (ngramCache.size() > 0) {
			ngramCache.remove(0);
		}
		
		if (c == NO_CHAR) {
			// remaining ngram (could be smaller than ngram size) 
			if (ngramCache.size() > 0)
			{
				createToken();			
				ngramCache.clear();
				return true;
			} else {
				offset = 0;
				return false;
			}
		}
		
		ngramCache.add((char)c);
		createToken();
		offset++;
		return true;
	}

	@Override
	public void reset(Reader input) throws IOException {
		super.reset(input);
		initializeCache();
		offset = 0;
	}

	private String toLowerCase(List<Character> ngram) {
		// TODO: Better way to do convert List<Character> to String
		StringBuilder sb = new StringBuilder();
		for (Character cha : ngram) {
			sb.append(cha);
		}
		String termValue = sb.toString();
		// lowercase term
		if (locale != null) {
			termValue = UCharacter.foldCase(termValue, true);
		}
		return termValue;
	}
}
