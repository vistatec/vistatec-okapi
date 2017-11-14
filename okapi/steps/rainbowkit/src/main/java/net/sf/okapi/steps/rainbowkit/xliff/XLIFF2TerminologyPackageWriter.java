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

package net.sf.okapi.steps.rainbowkit.xliff;

import net.sf.okapi.common.annotation.GlossEntry;
import net.sf.okapi.common.annotation.GlossaryAnnotation;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.lib.xliff2.core.Fragment;
import net.sf.okapi.lib.xliff2.core.Unit;
import net.sf.okapi.lib.xliff2.glossary.Glossary;

/**
 * Class that adds functionality on writing XLIFF glossary element <gls:glossary/> of the
 * <a href='http://docs.oasis-open.org/xliff/xliff-core/v2.0/xliff-core-v2.0.html#glossary-module'>Glossary module</a>
 * to the XLIFF unit element.
 *
 * Glossary annotation {@link GlossaryAnnotation} should be present in source segment {@link Segment} in order
 * to perform the writing.
 *
 * Also it is capable to annotate source and target segments with references to generated glossary entry ids.
 * For this, the fields {@link GlossaryAnnotation#annotateSourceSegment} and {@link GlossaryAnnotation#annotateTargetSegment}
 * from {@link GlossaryAnnotation} are used to indicate if the annotation should be done.
 *
 * @author Vladyslav Mykhalets
 */
public class XLIFF2TerminologyPackageWriter extends XLIFF2PackageWriter {

    public static final String GLS_SEGMENT_REF_PREFIX = "#gls=";

    @Override
    protected Unit toXLIFF2Unit(ITextUnit unit) {
        Unit xliffUnit = super.toXLIFF2Unit(unit);

        int segmentIndex = 0;
        for (Segment segment : unit.getSourceSegments()) {
            GlossaryAnnotation glossaryAnnotation = segment.getAnnotation(GlossaryAnnotation.class);
            net.sf.okapi.lib.xliff2.core.Segment xliffSegment = xliffUnit.getSegment(segmentIndex);
            addGlossaryReferences(xliffSegment, glossaryAnnotation);
            segmentIndex++;
        }

        for (Segment segment : unit.getSourceSegments()) {
            GlossaryAnnotation glossaryAnnotation = segment.getAnnotation(GlossaryAnnotation.class);
            addGlossary(xliffUnit, glossaryAnnotation);
        }

        return xliffUnit;
    }

    /**
     * Method which annotates source and target XLIFF segments with references to generated glossary entry ids.
     * It is possible to override the default behavior.
     *
     * @param xliffSegment XLIFF segment where glossary entries annotation will be done
     * @param glossaryAnnotation glossary annotation for XLIFF segment
     */
    protected void addGlossaryReferences(net.sf.okapi.lib.xliff2.core.Segment xliffSegment, GlossaryAnnotation glossaryAnnotation) {
        if (glossaryAnnotation == null) {
            return;
        }
        for (GlossEntry glossEntry : glossaryAnnotation) {
            if (glossaryAnnotation.isAnnotateSourceSegment()) {
                annotateTerms(xliffSegment.getSource(), glossEntry.getId(), glossEntry.getTerm().getText());
            }
            if (glossaryAnnotation.isAnnotateTargetSegment()) {
                for (GlossEntry.Translation translation : glossEntry.getTranslations()) {
                    annotateTerms(xliffSegment.getTarget(), glossEntry.getId(), translation.getText());
                }
            }
        }
    }

    /**
     * Method which converts OKAPI glossary entries to XLIFF format and adds them to XLIFF glossary element.
     * It is possible to override the default behaviour.
     *
     * @param xliffUnit XLIFF unit
     * @param glossaryAnnotation okapi glossary annotation taken from source segment
     */
    protected void addGlossary(Unit xliffUnit, GlossaryAnnotation glossaryAnnotation) {
        if (glossaryAnnotation == null) {
            return;
        }
        Glossary xliffGlossary = xliffUnit.getGlossary();
        for (GlossEntry glossEntry : glossaryAnnotation) {
            xliffGlossary.add(XLIFF2Utils.toXliffGlossEntry(glossEntry));
        }
    }

    private void annotateTerms(Fragment textFragment, String glossEntryId, String term) {
        int start;
        int end;
        int offset = 0;

        String ref = GLS_SEGMENT_REF_PREFIX + glossEntryId;

        while (offset != -1) {
            start = textFragment.getPlainText().indexOf(term, offset);

            if (start != -1) {
                start = textFragment.getCodedTextPosition(start,false); // convert to text position for coded text (i.e. with tags)
                end = start + term.length(); // assumes no tags are contained in the term string itself, which is quite safe
                textFragment.annotate(start, end,"term",null /* value attr */, ref);
                offset = end;
            } else {
                offset = start;
            }
        }
    }


}
