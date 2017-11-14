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

import javax.xml.stream.events.XMLEvent;
import java.util.ArrayList;
import java.util.List;

class StoryChildElementsWriter {

    private final StyleRangeEventsGenerator styleRangeEventsGenerator;

    private StyleDefinitions currentStyleDefinitions;

    StoryChildElementsWriter(StyleRangeEventsGenerator styleRangeEventsGenerator) {
        this.styleRangeEventsGenerator = styleRangeEventsGenerator;
    }

    List<XMLEvent> write(List<StoryChildElement> storyChildElements) {
        List<XMLEvent> events = new ArrayList<>();

        for (StoryChildElement storyChildElement : storyChildElements) {

            if (!(storyChildElement instanceof StoryChildElement.StyledTextElement)) {
                if (null != currentStyleDefinitions) {
                    events.addAll(styleRangeEventsGenerator.generateCharacterStyleRangeEnd());
                    events.addAll(styleRangeEventsGenerator.generateParagraphStyleRangeEnd());
                }

                events.addAll(storyChildElement.getEvents());

                if (null != currentStyleDefinitions) {
                    events.addAll(styleRangeEventsGenerator.generateParagraphStyleRangeStart(currentStyleDefinitions));
                    events.addAll(styleRangeEventsGenerator.generateCharacterStyleRangeStart(currentStyleDefinitions));
                }
                continue;
            }

            if (null == currentStyleDefinitions) {
                currentStyleDefinitions = ((StoryChildElement.StyledTextElement) storyChildElement).getStyleDefinitions();

                events.addAll(styleRangeEventsGenerator.generateParagraphStyleRangeStart(currentStyleDefinitions));
                events.addAll(styleRangeEventsGenerator.generateCharacterStyleRangeStart(currentStyleDefinitions));
            }

            StyleDefinitions styleDefinitions = ((StoryChildElement.StyledTextElement) storyChildElement).getStyleDefinitions();

            if (!currentStyleDefinitions.getParagraphStyleRange().equals(styleDefinitions.getParagraphStyleRange())) {
                events.addAll(styleRangeEventsGenerator.generateCharacterStyleRangeEnd());
                events.addAll(styleRangeEventsGenerator.generateParagraphStyleRangeEnd());

                currentStyleDefinitions = styleDefinitions;

                events.addAll(styleRangeEventsGenerator.generateParagraphStyleRangeStart(currentStyleDefinitions));
                events.addAll(styleRangeEventsGenerator.generateCharacterStyleRangeStart(currentStyleDefinitions));
                events.addAll(storyChildElement.getEvents());

                continue;
            }

            if (!currentStyleDefinitions.getCharacterStyleRange().equals(styleDefinitions.getCharacterStyleRange())) {
                events.addAll(styleRangeEventsGenerator.generateCharacterStyleRangeEnd());

                currentStyleDefinitions = styleDefinitions;

                events.addAll(styleRangeEventsGenerator.generateCharacterStyleRangeStart(currentStyleDefinitions));
                events.addAll(storyChildElement.getEvents());

                continue;
            }

            events.addAll(storyChildElement.getEvents());
        }

        return events;
    }
}
