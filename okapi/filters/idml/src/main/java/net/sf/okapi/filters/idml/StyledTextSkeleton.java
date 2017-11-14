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

import net.sf.okapi.common.IResource;
import net.sf.okapi.common.ISkeleton;

import java.util.List;
import java.util.Map;

class StyledTextSkeleton implements ISkeleton {

    private final List<StoryChildElement> storyChildElements;
    private final Map<Integer, Object> codeMap;

    private IResource parent;

    StyledTextSkeleton(List<StoryChildElement> storyChildElements, Map<Integer, Object> codeMap) {
        this.storyChildElements = storyChildElements;
        this.codeMap = codeMap;
    }

    List<StoryChildElement> getStoryChildElements() {
        return storyChildElements;
    }

    Map<Integer, Object> getCodeMap() {
        return codeMap;
    }

    @Override
    public ISkeleton clone() {
        StyledTextSkeleton styledTextSkeleton = new StyledTextSkeleton(storyChildElements, codeMap);
        styledTextSkeleton.setParent(parent);

        return styledTextSkeleton;
    }

    @Override
    public void setParent(IResource parent) {
        this.parent = parent;
    }

    @Override
    public IResource getParent() {
        return parent;
    }
}
