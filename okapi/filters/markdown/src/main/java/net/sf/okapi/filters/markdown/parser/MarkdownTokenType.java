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

public enum MarkdownTokenType {
    /* Core types */
    AUTO_LINK,
    BLANK_LINE,
    BLOCK_QUOTE,
    BULLET_LIST,
    BULLET_LIST_ITEM,
    CODE,
    EMPHASIS,
    FENCED_CODE_BLOCK,
    FENCED_CODE_BLOCK_INFO,
    HARD_LINE_BREAK,
    HEADING_PREFIX,
    HEADING_UNDERLINE,
    HTML_BLOCK,
    HTML_COMMENT_BLOCK,
    HTML_ENTITY,
    HTML_INLINE,
    HTML_INLINE_COMMENT,
    HTML_INNER_BLOCK,
    HTML_INNER_BLOCK_COMMENT,
    IMAGE,
    IMAGE_REF,
    INDENTED_CODE_BLOCK,
    LINK,
    LINK_REF,
    MAIL_LINK,
    ORDERED_LIST,
    ORDERED_LIST_ITEM,
    REFERENCE,
    SOFT_LINE_BREAK,
    STRONG_EMPHASIS,
    TEXT,
    THEMATIC_BREAK,
    WHITE_SPACE,

    /* Table types */
    TABLE_PIPE,

    /* YAML Header types */
    YAML_METADATA_HEADER
}
