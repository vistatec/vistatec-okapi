/*===========================================================================
  Copyright (C) 2010 by the Okapi Framework contributors
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

package net.sf.okapi.common;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Create a reversed iterator for a list compatible with foreach. Credit: Nat on
 * http://stackoverflow.com/questions/1098117/can-one-do-a-for-each-loop-in-java-in-reverse-orders
 * 
 * @author HARGRAVEJE
 * 
 * @param <T>
 *            type of the list element
 */
public class ReversedIterator<T> implements Iterable<T> {
	private final List<T> original;

	public ReversedIterator(final List<T> original) {
		this.original = original;
	}

	public Iterator<T> iterator() {
		final ListIterator<T> i = original.listIterator(original.size());

		return new Iterator<T>() {
			public boolean hasNext() {
				return i.hasPrevious();
			}

			public T next() {
				return i.previous();
			}

			public void remove() {
				i.remove();
			}
		};
	}
	
	public static <T> ReversedIterator<T> reverse(List<T> original) {
        return new ReversedIterator<T>(original);
    }

}
