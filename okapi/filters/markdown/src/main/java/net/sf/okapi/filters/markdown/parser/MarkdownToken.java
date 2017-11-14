/*===========================================================================
  Copyright (C) 2017 by the Okapi Framework contributors
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

package net.sf.okapi.filters.markdown.parser;

public class MarkdownToken {
    private String content;
    private boolean isTranslatable;
    private MarkdownTokenType type;

    public MarkdownToken(String content, boolean isTranslatable, MarkdownTokenType type) {
        this.content = content;
        this.isTranslatable = isTranslatable;
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isTranslatable() {
        return isTranslatable;
    }

    public void setTranslatable(boolean isTranslatable) {
        this.isTranslatable = isTranslatable;
    }

    public MarkdownTokenType getType() {
        return type;
    }

    public void setType(MarkdownTokenType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "[" + content + ", " + isTranslatable +  ", " + type + "]";
    }
}
