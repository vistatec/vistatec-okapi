/*
 * =============================================================================
 *   Copyright (C) 2010-2017 by the Okapi Framework contributors
 * -----------------------------------------------------------------------------
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * =============================================================================
 */

package net.sf.okapi.filters.openxml;

/**
 * Provides an empty style definitions.
 */
class EmptyStyleDefinitions implements StyleDefinitions {

    @Override
    public RunProperties getCombinedRunProperties(String paragraphStyle, String runStyle, RunProperties runProperties) {

        // copy run properties with the exclusion of the RunStyleProperty
        return RunProperties.copiedRunProperties(runProperties, false, true, false);
    }
}
