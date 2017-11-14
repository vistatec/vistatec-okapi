/*===========================================================================
  Copyright (C) 2009-2011 by the Okapi Framework contributors
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

package net.sf.okapi.steps.common.tufiltering;

import net.sf.okapi.common.resource.ITextUnit;

/**
 * Instances of classes that implement this interface are used to
 * filter text units that need to be processed by a filtering pipeline step. 
 */
public interface ITextUnitFilter {

	/**
	 * Tests if a given text unit should be processed by a filtering pipeline step.
	 * If a text unit is not accepted, then it is not processed by the step and
	 * sent unmodified further along the pipeline.
	 *  
	 * @param tu the given text unit
	 * @return <code>true</code> if the given text unit should be processed by the step
	 */
	boolean accept(ITextUnit tu);
}
