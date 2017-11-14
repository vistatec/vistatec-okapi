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

package net.sf.okapi.filters.idml;

import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

class TextUnitElementsAccumulator implements Accumulator<StoryChildElement> {

    private final ListIterator<StoryChildElement> storyChildElementIterator;

    TextUnitElementsAccumulator(ListIterator<StoryChildElement> storyChildElementIterator) {
        this.storyChildElementIterator = storyChildElementIterator;
    }

    @Override
    public List<StoryChildElement> accumulate() throws XMLStreamException {
        List<StoryChildElement> textUnitElements = new ArrayList<>();

        while (storyChildElementIterator.hasNext()) {
            StoryChildElement storyChildElement = storyChildElementIterator.next();

            if (storyChildElement instanceof StoryChildElement.StyledTextElement.Break) {
                textUnitElements.add(storyChildElement);
                break;
            }

            textUnitElements.add(storyChildElement);
        }

        return textUnitElements;
    }
}
