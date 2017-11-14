/*===========================================================================
  Copyright (C) 2013 by the Okapi Framework contributors
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

package net.sf.okapi.common.annotation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Annotation used to expose xliff 1.2 note element
 */
public class XLIFFNoteAnnotation implements IAnnotation, Iterable<XLIFFNote> {
	private List<XLIFFNote> notes = new ArrayList<>();

	/**
	 * Add a XLIFFNote to the annotation.
	 * @param note - XLIFFNote from the xliff document.
	 */
	public void add(XLIFFNote note) {
		this.notes.add(note);
	}
	
	public XLIFFNote getNote(int index) {
		return notes.get(index);
	}

	@Override
	public Iterator<XLIFFNote> iterator() {
		return notes.iterator();
	}
}
