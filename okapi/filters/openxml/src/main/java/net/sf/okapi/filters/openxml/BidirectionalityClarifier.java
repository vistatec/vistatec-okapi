/*===========================================================================
  Copyright (C) 2016-2017 by the Okapi Framework contributors
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

package net.sf.okapi.filters.openxml;

import java.util.ListIterator;

import static net.sf.okapi.filters.openxml.MarkupComponent.isAlignmentEmptyElementMarkupComponent;
import static net.sf.okapi.filters.openxml.MarkupComponent.isParagraphStartMarkupComponent;
import static net.sf.okapi.filters.openxml.MarkupComponent.isPresentationStartMarkupComponent;
import static net.sf.okapi.filters.openxml.MarkupComponent.isSheetViewMarkupComponent;
import static net.sf.okapi.filters.openxml.MarkupComponent.isTableStartMarkupComponent;
import static net.sf.okapi.filters.openxml.MarkupComponent.isTextBodyStartMarkupComponent;

/**
 * Provides a bidirectionality clarifier.
 */
class BidirectionalityClarifier {

    private CreationalParameters creationalParameters;
    private ClarificationParameters clarificationParameters;

    /**
     * Constructs the bidirectionality clarifier.
     *
     * @param creationalParameters    Creational parameters
     * @param clarificationParameters Clarification parameters
     */
    BidirectionalityClarifier(CreationalParameters creationalParameters, ClarificationParameters clarificationParameters) {
        this.creationalParameters = creationalParameters;
        this.clarificationParameters = clarificationParameters;
    }

    /**
     * Clarifies a markup.
     */
    void clarifyMarkup(Markup markup) {
        ListIterator<MarkupComponent> markupComponentIterator = markup.getComponents().listIterator();

        while (markupComponentIterator.hasNext()) {
            MarkupComponent markupComponent = markupComponentIterator.next();

            if (isSheetViewMarkupComponent(markupComponent)) {
                clarifySheetViewMarkupComponent(markupComponent);
            } else if (isAlignmentEmptyElementMarkupComponent(markupComponent)) {
                clarifyAlignmentEmptyElementMarkupComponent(markupComponent);
            } else if (isPresentationStartMarkupComponent(markupComponent)) {
                clarifyPresentationMarkupComponent(markupComponent);
            } else if (isTableStartMarkupComponent(markupComponent)) {
                clarifyTableProperties(markupComponentIterator);
            } else if (isTextBodyStartMarkupComponent(markupComponent)) {
                clarifyTextBodyProperties(markupComponentIterator);
            } else if (isParagraphStartMarkupComponent(markupComponent)) {
                clarifyParagraphProperties(markupComponentIterator);
            }
        }
    }

    private void clarifySheetViewMarkupComponent(MarkupComponent markupComponent) {
        new MarkupComponentClarifier.SheetViewClarifier(creationalParameters, clarificationParameters)
                .clarify(markupComponent);
    }

    private void clarifyAlignmentEmptyElementMarkupComponent(MarkupComponent markupComponent) {
        new MarkupComponentClarifier.AlignmentClarifier(creationalParameters, clarificationParameters)
                .clarify(markupComponent);
    }

    private void clarifyPresentationMarkupComponent(MarkupComponent markupComponent) {
        new MarkupComponentClarifier.PresentationClarifier(creationalParameters, clarificationParameters)
                .clarify(markupComponent);
    }

    private void clarifyTableProperties(ListIterator<MarkupComponent> markupComponentIterator) {
        new BlockPropertiesClarifier.TablePropertiesClarifier(creationalParameters, clarificationParameters)
                .clarify(markupComponentIterator);
    }

    private void clarifyTextBodyProperties(ListIterator<MarkupComponent> markupComponentIterator) {
        new BlockPropertiesClarifier.TextBodyPropertiesClarifier(creationalParameters, clarificationParameters)
                .clarify(markupComponentIterator);
    }

    private void clarifyParagraphProperties(ListIterator<MarkupComponent> markupComponentIterator) {
        new BlockPropertiesClarifier.ParagraphPropertiesClarifier(creationalParameters, clarificationParameters)
                .clarify(markupComponentIterator);
    }

    /**
     * Clarifies run properties.
     *
     * @param runProperties Run properties
     *
     * @return Clarified run properties
     *
     * {@code null} if the resulted run properties are empty
     */
    RunProperties clarifyRunProperties(RunProperties runProperties) {
        return new RunPropertiesClarifier(creationalParameters, clarificationParameters)
                .clarify(runProperties);
    }
}
