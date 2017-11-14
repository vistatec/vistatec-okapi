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

package net.sf.okapi.common.annotation;

public enum IssueType {

	MISSING_TARGETTU,
	
	MISSING_TARGETSEG,
	EXTRA_TARGETSEG,
	
	EMPTY_TARGETSEG,
	EMPTY_SOURCESEG,
	
	MISSING_LEADINGWS,
	MISSINGORDIFF_LEADINGWS,
	EXTRA_LEADINGWS,
	EXTRAORDIFF_LEADINGWS,
	MISSING_TRAILINGWS,
	MISSINGORDIFF_TRAILINGWS,
	EXTRA_TRAILINGWS,
	EXTRAORDIFF_TRAILINGWS,

	TARGET_SAME_AS_SOURCE,
	
	MISSING_CODE,
	EXTRA_CODE,
	SUSPECT_CODE,
	
	UNEXPECTED_PATTERN,
	
	SUSPECT_PATTERN,
	
	SUSPECT_NUMBER,
	SUSPECT_DATE_TIME,
	
	SOURCE_LENGTH,
	TARGET_LENGTH,
	
	ALLOWED_CHARACTERS,
	
	TERMINOLOGY,
	
	LANGUAGETOOL_ERROR,
	
	OTHER;

	/**
	 * Maps a given Issue type to an ITS issue type.
	 * @param issueType the issue type.
	 * @return the ITS issue type.
	 */
	public static String mapToITS (IssueType issueType) {
		switch (issueType ) {
			case MISSING_TARGETTU:
			case MISSING_TARGETSEG:
			case EMPTY_TARGETSEG:
			case EMPTY_SOURCESEG:
				return "omission";
				
			case EXTRA_TARGETSEG:
				return "addition";
				
			case MISSING_LEADINGWS:
			case MISSINGORDIFF_LEADINGWS:
			case EXTRA_LEADINGWS:
			case EXTRAORDIFF_LEADINGWS:
			case MISSING_TRAILINGWS:
			case MISSINGORDIFF_TRAILINGWS:
			case EXTRA_TRAILINGWS:
			case EXTRAORDIFF_TRAILINGWS:
				return "whitespace";
				
			case TARGET_SAME_AS_SOURCE:
				return "untranslated";
				
			case MISSING_CODE:
			case EXTRA_CODE:
			case SUSPECT_CODE:
				return "markup";
				
			case UNEXPECTED_PATTERN:
				return "pattern-problem";
				
			case SUSPECT_PATTERN:
			case SOURCE_LENGTH:
			case TARGET_LENGTH:
				return "length";
				
			case ALLOWED_CHARACTERS:
				
			case TERMINOLOGY:
				return "terminology";
			
			case SUSPECT_DATE_TIME:
			case SUSPECT_NUMBER:
				return "locale-violation";
				
			case LANGUAGETOOL_ERROR:
				return "other";
			
				
			default:
				return "uncategorized";
		}
	}
}
