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

package net.sf.okapi.common.encoder;

/**
 * Specifies the behavior for the escaping of single and double quotes. 
 */
public enum QuoteMode {
	
	/**
	 * Do not escape single or double quotes.
	 */
	UNESCAPED(0),
	/**
	 * Escape single and double quotes to a named entity.
	 */
	ALL(1),
	/**
	 * Escape double quotes to a named entity, and single quotes to a numeric entity.
	 */
	NUMERIC_SINGLE_QUOTES(2),
	/**
	 * Escape double quotes only.
	 */
	DOUBLE_QUOTES_ONLY(3);

	// This is identical to ordinal, but protects us from accident rearrangement.
	private int value;
	QuoteMode(int value) {
		this.value = value;
	}

	public static QuoteMode fromValue(int value) {
		for (QuoteMode qm : values()) {
			if (value == qm.value) return qm;
		}
		throw new IllegalArgumentException("Invalid quote mode: " + value);
	}
}