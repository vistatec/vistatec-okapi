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

import net.sf.okapi.filters.openxml.ElementSkipper.BookmarkElementSkipper;
import net.sf.okapi.filters.openxml.ElementSkipper.GeneralElementSkipper;
import net.sf.okapi.filters.openxml.ElementSkipper.SkippableElement;
import net.sf.okapi.filters.openxml.ElementSkipperStrategy.BookmarkElementSkipperStrategy;
import net.sf.okapi.filters.openxml.ElementSkipperStrategy.GeneralElementSkipperStrategy;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static net.sf.okapi.filters.openxml.ElementSkipper.RunPropertySkippableElement.RUN_PROPERTY_CHARACTER_SPACING;
import static net.sf.okapi.filters.openxml.ElementSkipper.RunPropertySkippableElement.RUN_PROPERTY_CHARACTER_WIDTH;
import static net.sf.okapi.filters.openxml.ElementSkipper.RunPropertySkippableElement.RUN_PROPERTY_COMPLEX_SCRIPT_BOLD;
import static net.sf.okapi.filters.openxml.ElementSkipper.RunPropertySkippableElement.RUN_PROPERTY_COMPLEX_SCRIPT_FONT_SIZE;


/**
 * Provides an element skipper factory.
 */
class ElementSkipperFactory {

    private static final String SKIPPABLE_BOOKMARK_NAME = "_GoBack";

    static ElementSkipper createGeneralElementSkipper(ConditionalParameters conditionalParameters, SkippableElement... skippableElements) {
        Set<String> skippableElementSet = getSkippableElementValues(skippableElements);

        if (conditionalParameters.getCleanupAggressively()) {
            skippableElementSet.add(RUN_PROPERTY_COMPLEX_SCRIPT_BOLD.getValue());
            skippableElementSet.add(RUN_PROPERTY_CHARACTER_SPACING.getValue());
            skippableElementSet.add(RUN_PROPERTY_COMPLEX_SCRIPT_FONT_SIZE.getValue());
            skippableElementSet.add(RUN_PROPERTY_CHARACTER_WIDTH.getValue());
        }

        return new GeneralElementSkipper(new GeneralElementSkipperStrategy(skippableElementSet, conditionalParameters));
    }

    static ElementSkipper createBookmarkElementSkipper(SkippableElement... skippableElements) {
        return new BookmarkElementSkipper(new BookmarkElementSkipperStrategy(getSkippableElementValues(skippableElements), SKIPPABLE_BOOKMARK_NAME));
    }

    private static Set<String> getSkippableElementValues(SkippableElement... skippableElements) {
        Set<String> skippableElementValues = new HashSet<>(skippableElements.length);

        for (SkippableElement skippableElement : skippableElements) {
            skippableElementValues.add(skippableElement.getValue());
        }

        return skippableElementValues;
    }
}
