/*===========================================================================
  Copyright (C) 2009 by the Okapi Framework contributors
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

package net.sf.okapi.steps.tokenization.common;

import net.sf.okapi.common.Range;
import net.sf.okapi.common.annotation.Annotations;
import net.sf.okapi.common.annotation.IAnnotation;

public class Lexem {

	/**
	 * Lexem ID. Unique within the producing lexer. Either hard-coded by the lexer, or 
	 * defined by one and only one of its external rules.
	 * Serializable. 
	 */
	private int id;
	
	/**
	 * Text of lexem.
	 */
	private String value;
	
	/**
	 * Range of text extracted as the given lexem.
	 */
	private Range range;
	
	/**
	 * ID of the lexer that extracted this lexem. Set by a lexers manager when processing the lexem. 
	 * !!! Non-serializable.
	 */
	private int lexerId;
	
	private Annotations annotations;
	
	/**
	 * True if the lexem is considered deleted.
	 */
	private boolean deleted;
	
	/**
	 * True if the lexem cannot be deleted.
	 */
	private boolean immutable;
	
	//public Lexem(int id, String value, Range range, int lexerId) {
	public Lexem(int id, String value, Range range) {
		
		super();
				
		this.id = id;
		this.value = value;
		this.range = range;
	}
	
	//public Lexem(int id, String value, int start, int end, int lexerId) {
	public Lexem(int id, String value, int start, int end) {
		
		this(id, value, new Range(start, end));
	}

	/**
	 * Gets lexem ID. 
	 * !!! Non-serializable.
	 */
	public int getId() {
		
		return id;
	}
	
	public String getValue() {
		return value;
	}

	public Range getRange() {
		
		return range;
	}

	/**
	 * Gets ID of the lexer that extracted this lexem. 
	 * !!! Non-serializable.
	 */
	public int getLexerId() {
		
		return lexerId;
	}

	public void setLexerId(int lexerId) {
		
		this.lexerId = lexerId;
	}
	
	public <A extends IAnnotation> A getAnnotation (Class<A> type) {
		
		if (annotations == null) return null;
		
		return annotations.get(type);
	}

	public void setAnnotation (IAnnotation annotation) {
		
		if (annotations == null) 
			annotations = new Annotations();
		
		annotations.set(annotation);
	}

	public <A extends IAnnotation> A removeAnnotation (Class<A> type) {
		
		if (annotations == null) return null;
		
		return annotations.remove(type);
	}
	
	@Override
	public String toString() {
		
		return String.format("%-20s%4d\t%4d, %4d\t%4d", 
				value, id, range.start, range.end, lexerId);
	}

	public boolean isDeleted() {
		
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		
		this.deleted = deleted;
	}

	public boolean isImmutable() {
		
		return immutable;
	}

	public void setImmutable(boolean immutable) {
		
		this.immutable = immutable;
	}

	public Annotations getAnnotations() {
		return annotations;
	}
}
