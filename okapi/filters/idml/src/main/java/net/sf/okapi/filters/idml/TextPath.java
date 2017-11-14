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

class TextPath {

    private final String storyId;
    private final String previousTextFrameId;
    private final String nextTextFrameId;

    TextPath(String storyId, String previousTextFrameId, String nextTextFrameId) {
        this.storyId = storyId;
        this.previousTextFrameId = previousTextFrameId;
        this.nextTextFrameId = nextTextFrameId;
    }

    String getStoryId() {
        return storyId;
    }

    String getPreviousTextFrameId() {
        return previousTextFrameId;
    }

    String getNextTextFrameId() {
        return nextTextFrameId;
    }

    static class TextPathBuilder {

        private String storyId;
        private String previousTextFrameId;
        private String nextTextFrameId;

        TextPathBuilder setStoryId(String storyId) {
            this.storyId = storyId;
            return this;
        }

        TextPathBuilder setPreviousTextFrameId(String previousTextFrameId) {
            this.previousTextFrameId = previousTextFrameId;
            return this;
        }

        TextPathBuilder setNextTextFrameId(String nextTextFrameId) {
            this.nextTextFrameId = nextTextFrameId;
            return this;
        }

        TextPath build() {
            return new TextPath(storyId, previousTextFrameId, nextTextFrameId);
        }
    }
}
