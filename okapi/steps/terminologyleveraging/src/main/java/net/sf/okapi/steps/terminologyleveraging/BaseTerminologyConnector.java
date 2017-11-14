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

package net.sf.okapi.steps.terminologyleveraging;

import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.annotation.GlossEntry;
import net.sf.okapi.common.annotation.GlossaryAnnotation;
import net.sf.okapi.common.resource.ISegments;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.steps.terminologyleveraging.TerminologyQueryResult.Term;
import net.sf.okapi.steps.terminologyleveraging.TerminologyQueryResult.Translation;

/**
 * Base terminology connector class implementing {@link ITerminologyQuery} and {@link IGlossaryElementIdGenerator}.
 *
 * Specific connectors would implement the following methods:
 * - {@link ITerminologyQuery#getName()}
 * - {@link ITerminologyQuery#open()}
 * - {@link ITerminologyQuery#close()}
 * - {@link IGlossaryElementIdGenerator#generateGlossEntryId(int, int, Term)}
 * - {@link IGlossaryElementIdGenerator#generateGlossEntryTranslationId(int, int, Term, Translation)}
 * - {@link BaseTerminologyConnector#query(List)}
 *
 * To completely override how glossary is being formed for each segment, connector would
 * override {@link BaseTerminologyConnector#populateSegmentsWithGlossary(int, Segment, Segment, List)} method.
 *
 * Also, it may be useful to override default behaviour of {@link ITerminologyQuery#getSettingsDisplay()} method.
 *
 * @author Vladyslav Mykhalets
 */
public abstract class BaseTerminologyConnector implements ITerminologyQuery, IGlossaryElementIdGenerator {
    
	//private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public static String GLOSS_ENTRY_SEPARATOR_CHAR = "-";
    public static String GLOSS_ENTRY_TRANSLATION_ID_SEPARATOR_CHAR = "T";

    protected LocaleId srcLoc;
    protected LocaleId trgLoc;

    protected boolean annotateSource = false;
    protected boolean annotateTarget = false;

    protected abstract List<List<TerminologyQueryResult>> query(List<String> sourceSegments);

    @Override
    public LocaleId getSourceLanguage() {
        return srcLoc;
    }

    @Override
    public LocaleId getTargetLanguage() {
        return trgLoc;
    }

    @Override
    public boolean getAnnotateSource() {
        return annotateSource;
    }

    @Override
    public boolean getAnnotateTarget() {
        return annotateTarget;
    }

    @Override
    public void setAnnotateSource(boolean annotateSource) {
        this.annotateSource = annotateSource;
    }

    @Override
    public void setAnnotateTarget(boolean annotateTarget) {
        this.annotateTarget = annotateTarget;
    }

    @Override
    public String getSettingsDisplay() {
        return String.format("\nSource Locale: %s"
                        + "\nTarget Locale: %s"
                        + "\nAnnotate Source Segment: %s"
                        + "\nAnnotate Target Segment: %s",
                srcLoc, trgLoc, annotateSource, annotateTarget);
    }

    @Override
    public void setLanguages(LocaleId sourceLocale, LocaleId targetLocale) {
        // We keep a copy of the original locale so getSource/TargetLocale() return an unaltered value.
        srcLoc = sourceLocale;
        trgLoc = targetLocale;
    }

    @Override
    public IParameters getParameters() {
        // No parameters by default
        return null;
    }

    @Override
    public void setParameters(IParameters params) {
        // No parameters by default
    }

    @Override
    public void leverage(ITextUnit unit) {
        if ((unit == null) || !unit.getSource().hasText() || !unit.isTranslatable()) {
            return; // No need to query
        }

        List<String> sourceSegments = new ArrayList<>();

        for (Segment segment : unit.getSourceSegments()) {
            sourceSegments.add(segment.text.getText());
        }

        List<List<TerminologyQueryResult>> termResults = query(sourceSegments);

        addGlossaryAnnotations(unit, termResults);
    }

    protected void populateSegmentsWithGlossary(int segmentIndex, Segment sourceSegment, Segment targetSegment, List<TerminologyQueryResult> queryResults) {
        GlossaryAnnotation glossary = new GlossaryAnnotation();

        glossary.setAnnotateSourceSegment(getAnnotateSource());
        glossary.setAnnotateTargetSegment(getAnnotateTarget());

        int termIndexInSegment = -1;
        for (TerminologyQueryResult result : queryResults) {
            termIndexInSegment++;

            TerminologyQueryResult.Term term = result.getTerm();
            String termText = term.getTermText();

            // Avoid fuzzy terminology matches
            if (!sourceSegment.text.getText().contains(termText)) {
                continue;
            }

            GlossEntry existingGlossEntry = findExistingGlossEntry(glossary, segmentIndex, termIndexInSegment, term);
            if (existingGlossEntry != null) {
                for (TerminologyQueryResult.Translation translation : result.getTranslations()) {
                    existingGlossEntry.addTranslation(toGlossEntryTranslation(segmentIndex, termIndexInSegment, term, translation));
                }
            } else {
                glossary.add(toGlossEntry(segmentIndex, termIndexInSegment, result));
            }
        }

        if (!glossary.isEmpty()) {
            sourceSegment.setAnnotation(glossary);
        }
    }

    private void addGlossaryAnnotations(ITextUnit unit, List<List<TerminologyQueryResult>> termResults) {

        ISegments sourceSegments = unit.getSourceSegments();
        ISegments targetSegments = unit.getTargetSegments(trgLoc);

        int segmentIndex = -1;

        for (List<TerminologyQueryResult> results : termResults) {
            segmentIndex++;
            Segment sourceSegment = sourceSegments.get(segmentIndex);
            Segment targetSegment = targetSegments.get(segmentIndex);

            // Skip segments with no text
            if (!sourceSegment.text.hasText(false /* whiteSpacesAreText */)) {
                continue;
            }

            populateSegmentsWithGlossary(segmentIndex, sourceSegment, targetSegment, results);
        }
    }

    private GlossEntry toGlossEntry(int segmentIndex, int termIndexInSegment, TerminologyQueryResult result) {
        GlossEntry glossEntry = new GlossEntry();

        GlossEntry.Term term = new GlossEntry.Term();
        term.setText(result.getTerm().getTermText());
        term.setSource(result.getTerm().getSource());

        glossEntry.setId(generateGlossEntryId(segmentIndex, termIndexInSegment, result.getTerm()));
        glossEntry.setTerm(term);

        for (TerminologyQueryResult.Translation translation : result.getTranslations()) {
            glossEntry.addTranslation(toGlossEntryTranslation(segmentIndex, termIndexInSegment, result.getTerm(), translation));
        }

        return glossEntry;
    }

    private GlossEntry.Translation toGlossEntryTranslation(int segmentIndex, int termIndexInSegment,
            TerminologyQueryResult.Term term, TerminologyQueryResult.Translation result) {
        GlossEntry.Translation translation = new GlossEntry.Translation();

        translation.setId(generateGlossEntryTranslationId(segmentIndex, termIndexInSegment, term, result));
        translation.setText(result.getText());

        return translation;
    }

    private GlossEntry findExistingGlossEntry(GlossaryAnnotation glossary, int segmentIndex, int termIndexInSegment, TerminologyQueryResult.Term term) {
        for (GlossEntry glossEntry : glossary) {
            if (glossEntry.getId().equals(generateGlossEntryId(segmentIndex, termIndexInSegment, term))) {
                return glossEntry;
            }
        }
        return null;
    }
}
