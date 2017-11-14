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

/**
 * Annotation representing the XLIFF 1.2 note element.
 * The set of note elements should be contained within the XLIFFNoteAnnotation
 */
public class XLIFFNote {
	
	public enum Annotates {
		SOURCE("source"),
		TARGET("target"),
		GENERAL("general");
		
		private String value;

		private Annotates(String value) {
	        this.value = value;
	    }
		
		public String value() {
	        return value;
	    }
		
		public static Annotates fromString(String value) {
			for (Annotates a : Annotates.values()) {
				if (a.value.equals(value)) {
					return a;
				}
			}
			throw new IllegalArgumentException("Invalid annotates value: " + value);
		}
		
		@Override
		public String toString() {
			return value;
		}
	}
	
	public enum Priority {
		ONE(1),
		TWO(2),
		THREE(3),
		FOUR(4),
		FIVE(5),
		SIX(6),
		SEVEN(7),
		EIGHT(8),
		NINE(9),
		TEN(10);

		
		private int value;

		private Priority(int value) {
	        this.value = value;
	    }
		
		public int value() {
	        return value;
	    }
		
		@Override
		public String toString() {
			return Integer.toString(value);
		}
		
		public static Priority fromInt(int value) {
			for (Priority p : Priority.values()) {
				if (p.value == value) {
					return p;
				}
			}
			throw new IllegalArgumentException("Invalid priority value: " + value);
		}
	}
	
	// Required
	private String note = "";
	// optional
	private String xmlLang = null;
	private String from = null;
	private Priority priority = null;
	private Annotates annotates = null;
	
	public XLIFFNote() {	
	}
	
	public XLIFFNote(String note) {
		this.setNoteText(note);
	}

	public String getNoteText() {
		return note;
	}

	public void setNoteText(String note) {
		this.note = note;
	}

	public String getXmLang() {
		return xmlLang;
	}

	public void setXmlLang(String xmlLang) {
		this.xmlLang = xmlLang;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public Priority getPriority() {
		return priority;
	}

	public void setPriority(Priority priority) {
		this.priority = priority;
	}

	public Annotates getAnnotates() {
		return annotates;
	}

	public void setAnnotates(Annotates annotates) {
		this.annotates = annotates;
	}
}
