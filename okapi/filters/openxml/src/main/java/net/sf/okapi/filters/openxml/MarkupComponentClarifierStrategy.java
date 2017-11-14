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

import javax.xml.stream.events.Attribute;
import java.util.List;
import java.util.ListIterator;

/**
 * Provides a markup component clarifier strategy.
 */
class MarkupComponentClarifierStrategy {

    protected CreationalParameters creationalParameters;
    protected ClarificationParameters clarificationParameters;
    protected ClarifiableAttribute clarifiableAttribute;

    MarkupComponentClarifierStrategy(CreationalParameters creationalParameters, ClarificationParameters clarificationParameters,
                                     ClarifiableAttribute clarifiableAttribute) {
        this.creationalParameters = creationalParameters;
        this.clarificationParameters = clarificationParameters;
        this.clarifiableAttribute = clarifiableAttribute;
    }

    void clarifyMarkupComponent(MarkupComponent markupComponent) {
        List<Attribute> attributes = getMarkupComponentAttributes(markupComponent);

        if (null == attributes) {
            return;
        }

        ListIterator<Attribute> attributeIterator = attributes.listIterator();

        while (attributeIterator.hasNext()) {
            Attribute attribute = attributeIterator.next();

            if (clarifiableAttribute.getName().equals(attribute.getName().getLocalPart())) {
                if (!clarificationParameters.shouldBeBidirectional()) {
                    attributeIterator.remove();
                    return;
                }
                if (clarifiableAttribute.getValues().contains(attribute.getValue())) {
                    return;
                }

                replaceAttribute(attributeIterator);
                return;
            }
        }

        if (clarificationParameters.shouldBeBidirectional()) {
            attributeIterator.add(createRequiredAttribute());
        }
    }

    private List<Attribute> getMarkupComponentAttributes(MarkupComponent markupComponent) {
        if (markupComponent instanceof MarkupComponent.StartMarkupComponent) {
            return ((MarkupComponent.StartMarkupComponent) markupComponent).getAttributes();
        }
        if (markupComponent instanceof MarkupComponent.EmptyElementMarkupComponent) {
            return ((MarkupComponent.EmptyElementMarkupComponent) markupComponent).getAttributes();
        }
        if (markupComponent instanceof BlockProperties) {
            return ((BlockProperties) markupComponent).getAttributes();
        }

        return null;
    }

    private void replaceAttribute(ListIterator<Attribute> attributeIterator) {
        attributeIterator.remove();
        attributeIterator.add(createRequiredAttribute());
    }

    protected Attribute createRequiredAttribute() {
        return creationalParameters.getEventFactory().createAttribute(
                clarifiableAttribute.getPrefix(),
                creationalParameters.getNamespaceUri(),
                clarifiableAttribute.getName(),
                clarifiableAttribute.getValues().get(0));
    }
}
