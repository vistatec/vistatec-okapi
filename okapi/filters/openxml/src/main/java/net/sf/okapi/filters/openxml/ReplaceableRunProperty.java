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
 * Provides a replaceable run property interface.
 */
public interface ReplaceableRunProperty {

    /**
     * Checks whether a run property can be replaced by another.
     *
     * @param runProperty A run property to check against
     *
     * @return {@code true} if a property can be replaced
     *         {@code false} otherwise
     */
    boolean canBeReplaced(ReplaceableRunProperty runProperty);

    /**
     * Replaces a run property by another.
     *
     * @param runProperty A run property to replace by
     *
     * @return A replaced run property
     */
    ReplaceableRunProperty replace(ReplaceableRunProperty runProperty);
}
