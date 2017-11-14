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

class StartElementContext {

    private StartElement startElement;
    private StartElement parentStartElement;
    private XMLEventReader eventReader;
    private XMLEventFactory eventFactory;
    private ConditionalParameters conditionalParameters;
    private Class<? extends ElementSkipper.SkippableElement> skippableElementType;
    private LocaleId sourceLanguage;

    StartElementContext(StartElement startElement,
            StartElement parentStartElement,
            XMLEventReader eventReader,
            XMLEventFactory eventFactory,
            ConditionalParameters conditionalParameters,
            Class<? extends ElementSkipper.SkippableElement> skippableElementType,
            LocaleId sourceLanguage) {

        this.startElement = startElement;
        this.parentStartElement = parentStartElement;
        this.eventReader = eventReader;
        this.eventFactory = eventFactory;
        this.conditionalParameters = conditionalParameters;
        this.skippableElementType = skippableElementType;
        this.sourceLanguage = sourceLanguage;
    }

    StartElement getStartElement() {
        return startElement;
    }

    StartElement getParentStartElement() {
        return parentStartElement;
    }

    XMLEventReader getEventReader() {
        return eventReader;
    }

    XMLEventFactory getEventFactory() {
        return eventFactory;
    }

    ConditionalParameters getConditionalParameters() {
        return conditionalParameters;
    }

    Class<? extends ElementSkipper.SkippableElement> getSkippableElementType() {
        return skippableElementType;
    }

    LocaleId getSourceLanguage() {
        return sourceLanguage;
    }
}
