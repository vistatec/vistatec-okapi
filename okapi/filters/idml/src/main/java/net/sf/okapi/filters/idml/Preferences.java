/*
 * =============================================================================
 *   Copyright (C) 2010-2013 by the Okapi Framework contributors
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

class Preferences {

    private final XMLPreference xmlPreference;
    private final StoryPreference storyPreference;

    Preferences(XMLPreference xmlPreference, StoryPreference storyPreference) {
        this.xmlPreference = xmlPreference;
        this.storyPreference = storyPreference;
    }

    XMLPreference getXmlPreference() {
        return xmlPreference;
    }

    StoryPreference getStoryPreference() {
        return storyPreference;
    }

    static class XMLPreference {

        private final String defaultStoryTagName;
        private final String defaultTableTagName;
        private final String defaultCellTagName;

        XMLPreference(String defaultStoryTagName, String defaultTableTagName, String defaultCellTagName) {
            this.defaultStoryTagName = defaultStoryTagName;
            this.defaultTableTagName = defaultTableTagName;
            this.defaultCellTagName = defaultCellTagName;
        }

        String getDefaultStoryTagName() {
            return defaultStoryTagName;
        }

        String getDefaultTableTagName() {
            return defaultTableTagName;
        }

        String getDefaultCellTagName() {
            return defaultCellTagName;
        }
    }

    static class StoryPreference {

        private final String storyDirection;

        StoryPreference(String storyDirection) {
            this.storyDirection = storyDirection;
        }

        String getStoryDirection() {
            return storyDirection;
        }
    }

    static class PreferencesBuilder {

        private XMLPreference xmlPreference;
        private StoryPreference storyPreference;

        PreferencesBuilder setXmlPreference(XMLPreference xmlPreference) {
            this.xmlPreference = xmlPreference;
            return this;
        }

        PreferencesBuilder setStoryPreference(StoryPreference storyPreference) {
            this.storyPreference = storyPreference;
            return this;
        }

        Preferences build() {
            return new Preferences(xmlPreference, storyPreference);
        }
    }
}
