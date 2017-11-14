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

package net.sf.okapi.filters.markdown;

import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.filters.InlineCodeFinder;

public class Parameters extends StringParameters {
    public static final String HTML_RULE = "</?(\\w+)\\b[^>]*/?>";

    private static final String USECODEFINDER = "useCodeFinder";
    private static final String CODEFINDERRULES = "codeFinderRules";
    private static final String TRANSLATE_URLS = "translateUrls";
    private static final String TRANSLATE_CODE_BLOCKS = "translateCodeBlocks";
    private static final String TRANSLATE_HEADER_METADATA = "translateHeaderMetadata";
    private static final String TRANSLATE_IMAGE_ALTTEXT = "translateImageAltText";

    private InlineCodeFinder codeFinder; // Initialized in reset()

    public boolean getUseCodeFinder() {
        return getBoolean(USECODEFINDER);
    }

    public void setUseCodeFinder(boolean useCodeFinder) {
        setBoolean(USECODEFINDER, useCodeFinder);
    }

    public boolean getTranslateUrls() {
        return getBoolean(TRANSLATE_URLS);
    }

    public void setTranslateUrls(boolean translateUrls) {
        setBoolean(TRANSLATE_URLS, translateUrls);
    }

    public boolean getTranslateCodeBlocks() {
        return getBoolean(TRANSLATE_CODE_BLOCKS);
    }

    public void setTranslateCodeBlocks(boolean translateCodeBlocks) {
        setBoolean(TRANSLATE_CODE_BLOCKS, translateCodeBlocks);
    }

    public boolean getTranslateHeaderMetadata() {
        return getBoolean(TRANSLATE_HEADER_METADATA);
    }

    public void setTranslateHeaderMetadata(boolean translateHeaderMetadata) {
        setBoolean(TRANSLATE_HEADER_METADATA, translateHeaderMetadata);
    }

    public boolean getTranslateImageAltText() {
        return getBoolean(TRANSLATE_IMAGE_ALTTEXT);
    }

    public void setTranslateImageAltText(boolean translateImageAltText) {
        setBoolean(TRANSLATE_IMAGE_ALTTEXT, translateImageAltText);
    }

    public InlineCodeFinder getCodeFinder() {
        return codeFinder;
    }

    @Override
    public void reset() {
        super.reset();
        setUseCodeFinder(true);
        setTranslateUrls(false);
        setTranslateCodeBlocks(true);
        setTranslateHeaderMetadata(false);
        setTranslateImageAltText(true);

        codeFinder = new InlineCodeFinder();
        codeFinder.setSample("<tag></at><tag/> <tag attr='val'> </tag=\"val\">");
        codeFinder.setUseAllRulesWhenTesting(true);
        codeFinder.addRule(HTML_RULE); // Replace HTML tags with code
        codeFinder.addRule("#+"); // Treat words starting with '#'s as headers and convert the '#'s to code
        codeFinder.addRule("~{1,2}"); // Replace strikethrough and subscript with code
    }

    public void fromString (String data) {
        super.fromString(data);
        codeFinder.fromString(buffer.getGroup(CODEFINDERRULES, ""));
    }

    @Override
    public String toString () {
        buffer.setGroup(CODEFINDERRULES, codeFinder.toString());
        return super.toString();
    }
}
