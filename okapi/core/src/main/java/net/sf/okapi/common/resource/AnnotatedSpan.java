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

package net.sf.okapi.common.resource;

import net.sf.okapi.common.Range;

/**
 * Span of text and its annotation data.
 */
public class AnnotatedSpan {

	/**
	 * Type of annotation.
	 */
	public String type;
	
	/**
	 * The annotation itself (can be null).
	 */
	public InlineAnnotation annotation;

	/**
	 * Copy of the fragment of text to which the annotation is applied.
	 */
	public TextFragment span;
	
	/**
	 * The start and end positions of the span of text in the original
	 * coded text.
	 */
	public Range range;
	
	/**
	 * Creates a new AnnotatedSpan object with a give type of annotation,
	 * its annotation and its fragment of text. 
	 * @param type the type of the annotation for this span of text.
	 * @param annotation the annotation associated with this span of text.
	 * @param span the span of text.
	 * @param start the start position of the span of text in the original
	 * coded text.
	 * @param end the end position of the span of text in the original
	 * coded text.
	 */
	public AnnotatedSpan (String type,
		InlineAnnotation annotation,
		TextFragment span,
		int start,
		int end)
	{
		this.type = type;
		this.annotation = annotation;
		this.span = span;
		range = new Range(start, end);
	}

}
