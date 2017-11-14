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

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.TextFragment;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 *
 * @author HaslamJD
 */
@RunWith(JUnit4.class)
public class TranslationUnitVariantTest {

    private final TextFragment content = new TextFragment();
    private final LocaleId locale = LocaleId.fromString("fb");

    @Test
    public void constructorNoArg() {
        TranslationUnitVariant tuv = new TranslationUnitVariant();
        assertNull("text fragment should be null", tuv.getContent());
        assertNull("lang should be null", tuv.getLanguage());
    }

    @Test
    public void constructorTwoArgs() {
        TranslationUnitVariant tuv = new TranslationUnitVariant(locale, content);
        assertSame("content", content, tuv.getContent());
        assertSame("lang", locale, tuv.getLanguage());
    }

    @Test
    public void setContent() {
        TranslationUnitVariant tuv = new TranslationUnitVariant();
        tuv.setContent(content);
        assertSame("content", content, tuv.getContent());
    }

    @Test
    public void setLang() {
        TranslationUnitVariant tuv = new TranslationUnitVariant();
        tuv.setLocale(locale);
        assertSame("content", locale, tuv.getLanguage());
    }
}
