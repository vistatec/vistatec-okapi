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

package net.sf.okapi.common.resource;

import java.util.LinkedList;
import java.util.List;

import net.sf.okapi.common.LocaleId;

/**
 * An aligned pair consists of a source and target list of {@link TextPart}s or
 * {@link Segment}s, along with a {@link LocaleId} specifying the locale of the
 * target.
 * <p> 
 * An AlignedPair is read-only.
 * 
 * @author HARGRAVEJE
 * 
 */
public final class AlignedPair {
	private final List<TextPart> sourceParts;
	private final List<TextPart> targetParts;
	private final LocaleId localeId;

	/**
	 * Creates an AlignedPair from source and target {@link TextPart}s
	 * 
	 * @param sourceParts
	 *            List source inter-segment and segment parts
	 * @param targetParts
	 *            List target inter-segment and segment parts
	 * @param localeId
	 *            {@link LocaleId} of the target parts
	 */
	public AlignedPair(final List<TextPart> sourceParts,
			final List<TextPart> targetParts, final LocaleId localeId) {
		this.sourceParts = sourceParts;
		this.targetParts = targetParts;
		this.localeId = localeId;
	}

	/**
	 * Creates an AlignedPair from source and target {@link Segment}s
	 * 
	 * @param sourceSegment
	 *            - the source {@link Segment}
	 * @param targetSegment
	 *            - the target {@link Segment}
	 * @param localeId
	 *            - {@link LocaleId} of the target {@link Segment}
	 */
	public AlignedPair(final Segment sourceSegment,
			final Segment targetSegment, final LocaleId localeId) {
		List<TextPart> sourceParts = new LinkedList<TextPart>();
		if (sourceSegment != null) {
			sourceParts.add(sourceSegment);
		}

		List<TextPart> targetParts = new LinkedList<TextPart>();
		if (targetSegment != null) {
			targetParts.add(targetSegment);
		}

		this.sourceParts = sourceParts;
		this.targetParts = targetParts;
		this.localeId = localeId;
	}

	/**
	 * Gets the source {@link TextPart}s
	 * 
	 * @return list of {@link TextPart}s
	 */
	public List<TextPart> getSourceParts() {
		return sourceParts;
	}

	/**
	 * Gets the target {@link TextPart}s
	 * 
	 * @return list of {@link TextPart}s
	 */
	public List<TextPart> getTargetParts() {
		return targetParts;
	}

	/**
	 * Gets the {@link LocaleId} of the target parts
	 * 
	 * @return a {@link LocaleId}
	 */
	public LocaleId getLocaleId() {
		return localeId;
	}
}
