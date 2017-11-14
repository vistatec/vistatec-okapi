/*===========================================================================
  Copyright (C) 2010-2011 by the Okapi Framework contributors
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

package net.sf.okapi.steps.paraaligner;

import java.util.Comparator;
import java.util.regex.Pattern;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.resource.DocumentPart;

/**
 * Compare two {@link DocumentPart}s. Remove whitespace before comparison. If the strings are less
 * than a minimum length then do not use them for matching 
 * 
 * @author HARGRAVEJE
 * 
 */
public class EventComparator implements Comparator<Event> {
	private static final int MIN_LENGTH = 20;
	private static final String WHITESPACE_REGEX = "[ \t\r\n\f\u200B]+";
	private static final Pattern WHITESPACE_PATTERN = Pattern.compile(WHITESPACE_REGEX);
	
	private int minLength;
	
	public EventComparator() {
		this.minLength = MIN_LENGTH;
	}
	
	public EventComparator(int minLength) {
		this.minLength = minLength;
	}
	
	@Override
	public int compare(final Event srcEvent, final Event trgEvent) {	
		if (srcEvent == null || trgEvent == null) {
			return -1;
		}
		
		if (srcEvent.getEventType() != trgEvent.getEventType()) {
			return -1;
		}
		
		if (srcEvent.getEventType() == EventType.DOCUMENT_PART && 
				trgEvent.getEventType() == EventType.DOCUMENT_PART) {
			String src = WHITESPACE_PATTERN.matcher(srcEvent.getDocumentPart().toString()).replaceAll("");
			String trg = WHITESPACE_PATTERN.matcher(trgEvent.getDocumentPart().toString()).replaceAll("");
			if (src.length() <= minLength || trg.length() <= minLength) {
				return -1;
			}
			return src.compareTo(trg);
		}
		
		// any other Event types never match
		return -1;
	}
}
