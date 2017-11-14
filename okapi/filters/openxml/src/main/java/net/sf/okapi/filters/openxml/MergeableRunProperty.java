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

package net.sf.okapi.filters.openxml;

/**
 * Provides a mergeable run property interface.
 */
public interface MergeableRunProperty {

    /**
     * Checks whether a run property can be merged with another.
     *
     * @param runProperty A run property to merge with
     *
     * @return {@code true} if a property can be merged
     *         {@code false} otherwise
     */
    boolean canBeMerged(MergeableRunProperty runProperty);

    /**
     * Merges a run property with another.
     *
     * @param runProperty A run property to merge with
     *
     * @return A merged run property
     */
    MergeableRunProperty merge(MergeableRunProperty runProperty);
}
