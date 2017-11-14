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

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;

import net.sf.okapi.common.LocaleId;

/**
 * Provides a start element context factory.
 */
class StartElementContextFactory {

    static StartElementContext createStartElementContext(StartElement startElement,
                                                         StartElement parentStartElement,
                                                         XMLEventReader eventReader,
                                                         XMLEventFactory eventFactory,
                                                         ConditionalParameters conditionalParameters,
                                                         Class<? extends ElementSkipper.SkippableElement> skippableElementType,
                                                         LocaleId sourceLanguage) {

        return new StartElementContext(startElement, parentStartElement, eventReader, eventFactory,
                conditionalParameters, skippableElementType, sourceLanguage);
    }

    static StartElementContext createStartElementContext(StartElement startElement,
                                                         StartElement parentStartElement,
                                                         XMLEventReader eventReader,
                                                         XMLEventFactory eventFactory,
                                                         ConditionalParameters conditionalParameters,
                                                         Class<? extends ElementSkipper.SkippableElement> skippableElementType) {

        return new StartElementContext(startElement, parentStartElement, eventReader, eventFactory,
                conditionalParameters, skippableElementType, null);
    }

    static StartElementContext createStartElementContext(StartElement startElement,
                                                         XMLEventReader eventReader,
                                                         XMLEventFactory eventFactory,
                                                         ConditionalParameters conditionalParameters) {

        return createStartElementContext(startElement, null, eventReader, eventFactory, conditionalParameters, null);
    }

    static StartElementContext createStartElementContext(StartElement startElement,
                                                         XMLEventReader eventReader,
                                                         XMLEventFactory eventFactory,
                                                         ConditionalParameters conditionalParameters,
                                                         LocaleId sourceLanguage) {

        return createStartElementContext(startElement, null, eventReader, eventFactory, conditionalParameters, null, sourceLanguage);
    }

    static StartElementContext createStartElementContext(StartElement startElement,
                                                         StartElementContext startElementContext,
                                                         Class<? extends ElementSkipper.SkippableElement> skippableElementType) {

        return createStartElementContext(startElement, startElementContext.getStartElement(),
                startElementContext.getEventReader(), startElementContext.getEventFactory(),
                startElementContext.getConditionalParameters(), skippableElementType,
                startElementContext.getSourceLanguage());
    }

    static StartElementContext createStartElementContext(StartElement startElement, StartElementContext startElementContext) {

        return createStartElementContext(startElement, startElementContext, null);
    }
}
