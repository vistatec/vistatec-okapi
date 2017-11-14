/*===========================================================================
  Copyright (C) 2016-2017 by the Okapi Framework contributors
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

package net.sf.okapi.common.filters;

import java.util.Iterator;
//jdk8 import java.util.stream.Stream;
//jdk8 import java.util.stream.StreamSupport;

import net.sf.okapi.common.Event;

/**
 * Wrapper class that takes an IFilter and makes it Iterable. 
 */
public class FilterIterable implements Iterable<Event> {
	private final IFilter filter;

	public FilterIterable(final IFilter filter) {
		this.filter = filter;
	}

	@Override
	public final Iterator<Event> iterator() {
		return new Iterator<Event>() {

			@Override
			public boolean hasNext() {
				return filter.hasNext();
			}

			@Override
			public Event next() {
				return filter.next();
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException(
						"Removing Event(s) from IFilter is not supported");
			}
		};
	}

/*
 * TODO(jdk8): we can't do this until we stop supporting JDK 7
	public final Stream<Event> stream() {
		return StreamSupport.stream(spliterator(), false);
	}
*/
}
