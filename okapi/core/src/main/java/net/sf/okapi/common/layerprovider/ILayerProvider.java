/*===========================================================================
  Copyright (C) 2008-2009 by the Okapi Framework contributors
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

package net.sf.okapi.common.layerprovider;

import net.sf.okapi.common.encoder.IEncoder;

/**
 * Provides common methods for encoding a layer on top of a text
 * extracted with a filter.
 */
public interface ILayerProvider extends IEncoder {

	/**
	 * Gets the string denoting the start of external codes.
	 * @return The string denoting the start of external codes.
	 */
	public String startCode ();
	
	/**
	 * Gets the string denoting the end of external codes.
	 * @return The string denoting the end of external codes.
	 */
	public String endCode ();
	
	/**
	 * Gets the string denoting the start of inline codes.
	 * @return The string denoting the start of inlinel codes.
	 */
	public String startInline ();
	
	/**
	 * Gets the string denoting the end of inline codes.
	 * @return The string denoting the end of inlinel codes.
	 */
	public String endInline ();
	
	/**
	 * Gets the string denoting the start of a bilingual segment.
	 * @return The string denoting the start of a bilingual segment.
	 */
	public String startSegment ();
	
	/**
	 * Gets the string denoting the end of a bilingual segment.
	 * @return The string denoting the end of a bilingual segment.
	 */
	public String endSegment ();
	
	/**
	 * Gets the string for the separator at the middle of a bilingual segment.
	 * @param leverage Indicator of leverage to optionally place in the separator.
	 * @return The string for the separator at the middle of a bilingual segment.
	 */
	public String midSegment (int leverage);
	
}
