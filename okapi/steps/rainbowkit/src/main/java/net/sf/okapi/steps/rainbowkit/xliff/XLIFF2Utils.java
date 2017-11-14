/*===========================================================================
  Copyright (C) 2010-2011 by the Okapi Framework contributors
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

package net.sf.okapi.steps.rainbowkit.xliff;

import net.sf.okapi.common.annotation.GlossEntry;
import net.sf.okapi.common.annotation.GlossEntry.Definition;
import net.sf.okapi.common.annotation.GlossEntry.Term;
import net.sf.okapi.common.annotation.GlossEntry.Translation;

/**
 * Class that provides useful methods for converting XLIFF elements
 * defined in <a href='http://docs.oasis-open.org/xliff/xliff-core/v2.0/xliff-core-v2.0.html#glossary-module'>Glossary module</a>
 * from Okapi internal presentation to XLIFF library internal presentation (package {@link net.sf.okapi.lib.xliff2.glossary})
 *
 * Methods:
 *
 * - {@link XLIFF2Utils#toXliffGlossEntry(GlossEntry)} converts <gls:glossEntry/> element
 * - {@link XLIFF2Utils#toXliffTranslation(Translation)} converts <gls:translation/> element
 * - {@link XLIFF2Utils#toXliffDefinition(Definition)} converts <gls:definition/> element
 * - {@link XLIFF2Utils#toXliffTerm(Term)}} converts <gls:term/> element
 *
 * @author Vladyslav Mykhalets
 */
public class XLIFF2Utils {

    public static net.sf.okapi.lib.xliff2.glossary.GlossEntry toXliffGlossEntry(GlossEntry glossEntry) {
        net.sf.okapi.lib.xliff2.glossary.GlossEntry xliffGlossEntry = new net.sf.okapi.lib.xliff2.glossary.GlossEntry();

        xliffGlossEntry.setId(glossEntry.getId());
        xliffGlossEntry.setRef(glossEntry.getRef());
        xliffGlossEntry.setTerm(toXliffTerm(glossEntry.getTerm()));
        xliffGlossEntry.setDefinition(toXliffDefinition(glossEntry.getDefinition()));

        for (GlossEntry.Translation translation : glossEntry) {
            xliffGlossEntry.getTranslations().add(toXliffTranslation(translation));
        }

        return xliffGlossEntry;
    }


    public static net.sf.okapi.lib.xliff2.glossary.Definition toXliffDefinition(GlossEntry.Definition definition) {
        net.sf.okapi.lib.xliff2.glossary.Definition xliffDefinition = new net.sf.okapi.lib.xliff2.glossary.Definition(
                definition != null ? definition.getText() : null);
        if (definition != null) {
            xliffDefinition.setSource(definition.getSource());
        }
        return xliffDefinition;
    }

    public static net.sf.okapi.lib.xliff2.glossary.Term toXliffTerm(GlossEntry.Term term) {
        net.sf.okapi.lib.xliff2.glossary.Term xliffTerm = new net.sf.okapi.lib.xliff2.glossary.Term(term.getText());
        xliffTerm.setSource(term.getSource());
        return xliffTerm;
    }

    public static net.sf.okapi.lib.xliff2.glossary.Translation toXliffTranslation(GlossEntry.Translation translation) {
        net.sf.okapi.lib.xliff2.glossary.Translation xliffTranslation = new net.sf.okapi.lib.xliff2.glossary.Translation(translation.getText());
        xliffTranslation.setId(translation.getId());
        xliffTranslation.setRef(translation.getRef());
        return xliffTranslation;
    }
}
