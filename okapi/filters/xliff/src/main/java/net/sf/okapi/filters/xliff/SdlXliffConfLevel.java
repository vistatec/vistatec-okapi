/*===========================================================================
  Copyright (C) 2017 by the Okapi Framework contributors
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
============================================================================*/

package net.sf.okapi.filters.xliff;

/**
 * Confirmation level values used in SDLXLIFF:
 * http://producthelp.sdl.com/SDK/FileTypeSupport/2017/html/06ecb14d-fe67-4387-bb9d-a25d242424f1.htm
 */
public enum SdlXliffConfLevel {
	APPROVED_SIGN_OFF("ApprovedSignOff", "x-sdl-ApprovedSignOff"),
	APPROVED_TRANSLATION("ApprovedTranslation", "x-sdl-ApprovedTranslation"),
	DRAFT("Draft", "x-sdl-Draft"),
	REJECTED_SIGN_OFF("RejectedSignOff", "x-sdl-RejectedSignOff"),
	REJECTED_TRANSLATION("RejectedTranslation", "x-sdl-RejectedTranslation"),
	TRANSLATED("Translated", "x-sdl-Translated"),
	UNSPECIFIED("Unspecified", "x-sdl-Unspecified");

	private final String confValue;
	private final String stateValue;

	private SdlXliffConfLevel(String confValue, String stateValue) {
		this.confValue = confValue;
		this.stateValue = stateValue;
	}

	/**
	 * Returns this confirmation level as a value that can be used in the 'conf' attribute
	 * of an &lt;sdl:seg&gt; element.
	 */
	public String getConfValue() {
		return confValue;
	}

	/**
	 * Returns this confirmation level as a state value that can be used in the 'state'
	 * attribute of a &lt;target&gt; or &lt;bin-target&gt; element.
	 */
	public String getStateValue() {
		return stateValue;
	}

	/**
	 * Returns true if there is a confirmation level for the given value.
	 */
	public static boolean isValidConfValue(String confValue) {
		for (SdlXliffConfLevel confLevel: values()) {
			if (confLevel.confValue.equals(confValue)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the confirmation level associated with the given value. Throws a
	 * {@link java.lang.IllegalArgumentException} if the value cannot be
	 * mapped to a confirmation level.
	 */
	public static SdlXliffConfLevel fromConfValue(String confValue) {
		for (SdlXliffConfLevel confLevel: values()) {
			if (confLevel.confValue.equals(confValue)) {
				return confLevel;
			}
		}
		throw new IllegalArgumentException("No valid conf level for conf value: " + confValue);
	}

	/**
	 * Returns true if there is a confirmation level for the given state value.
	 */
	public static boolean isValidStateValue(String stateValue) {
		for (SdlXliffConfLevel confLevel: values()) {
			if (confLevel.stateValue.equals(stateValue)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the confirmation level associated with the given state value. Throws a
	 * {@link java.lang.IllegalArgumentException} if the state value cannot be
	 * mapped to a confirmation level.
	 */
	public static SdlXliffConfLevel fromStateValue(String stateValue) {
		for (SdlXliffConfLevel confLevel: values()) {
			if (confLevel.stateValue.equals(stateValue)) {
				return confLevel;
			}
		}
		throw new IllegalArgumentException("No valid conf level for state value: " + stateValue);
	}

}
