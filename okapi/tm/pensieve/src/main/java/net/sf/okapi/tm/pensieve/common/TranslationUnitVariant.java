/*===========================================================================
  Copyright (C) 2008-2009 by the Okapi Framework contributors
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

package net.sf.okapi.tm.pensieve.common;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.TextFragment;

/**
 * Represents a Unit of Translation's Variant. This is used for both the source and targets
 * @author HaslamJD
 */
public class TranslationUnitVariant {
    private LocaleId language;
    private TextFragment content;

    /**
     * Creates an empty TUV
     */
    public TranslationUnitVariant() {}

    /**
     * Creates a TUV with the given language and content
     * @param language the language of the TUV
     * @param content the content of the TUV
     */
    public TranslationUnitVariant(LocaleId language,
    	TextFragment content)
    {
        this.language = language;
        this.content = content;
    }

    public TextFragment getContent() {
        return content;
    }

    public void setContent(TextFragment content) {
        this.content = content;
    }

    public LocaleId getLanguage () {
        return language;
    }

    public void setLocale (LocaleId locale) {
        this.language = locale;
    }

}
