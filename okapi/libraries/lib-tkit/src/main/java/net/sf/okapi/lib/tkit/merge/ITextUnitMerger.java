/*===========================================================================
  Copyright (C) 2008-2017 by the Okapi Framework contributors
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

package net.sf.okapi.lib.tkit.merge;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.ITextUnit;

public interface ITextUnitMerger {

	/**
	 * Merges the translated text unit to the one from the skeleton. Merges the text unit's target
	 * (under certain conditions).
	 *
	 * @param tuFromSkeleton text unit from the skeleton
	 * @param tuFromTranslation text unit from the translation
	 * @return the merged text unit
	 */
	ITextUnit mergeTargets(ITextUnit tuFromSkeleton, ITextUnit tuFromTranslation);

	void setTargetLocale(LocaleId trgLoc);

	Parameters getParameters();

	void setParameters(Parameters params);
}
