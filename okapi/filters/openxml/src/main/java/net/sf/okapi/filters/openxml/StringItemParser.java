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


import net.sf.okapi.common.IdGenerator;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.ArrayList;
import java.util.List;

import static net.sf.okapi.filters.openxml.ElementSkipper.PhoneticInlineSkippableElement.PHONETIC_PROPERTY;
import static net.sf.okapi.filters.openxml.ElementSkipper.PhoneticInlineSkippableElement.PHONETIC_RUN;
import static net.sf.okapi.filters.openxml.MarkupComponentFactory.createEndMarkupComponent;
import static net.sf.okapi.filters.openxml.MarkupComponentFactory.createGeneralMarkupComponent;
import static net.sf.okapi.filters.openxml.MarkupComponentFactory.createStartMarkupComponent;
import static net.sf.okapi.filters.openxml.StartElementContextFactory.createStartElementContext;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.isRunStartEvent;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.isStringItemEndEvent;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.isTextStartEvent;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.isWhitespace;

class StringItemParser extends ChunkParser<StringItem> {
    private StringItemBuilder builder;
    private ElementSkipper phoneticRunAndPropertyElementSkipper;

    StringItemParser(StartElementContext startElementContext, IdGenerator nestedBlockIdGenerator, StyleDefinitions styleDefinitions) {
        super(startElementContext, nestedBlockIdGenerator, styleDefinitions);
        builder = new StringItemBuilder();

        phoneticRunAndPropertyElementSkipper = ElementSkipperFactory.createGeneralElementSkipper(
                startElementContext.getConditionalParameters(),
                PHONETIC_RUN,
                PHONETIC_PROPERTY);
    }

    @Override
    public StringItem parse() throws XMLStreamException {
        builder.addMarkupComponent(createStartMarkupComponent(startElementContext.getEventFactory(), startElementContext.getStartElement()));
        RunMerger runMerger = new RunMerger();
        XMLEvent e = null;
        do  {
            e = startElementContext.getEventReader().nextEvent();
            if (isRunStartEvent(e)) {
                processRun(builder, runMerger, e.asStartElement());
            } else if (isTextStartEvent(e)) {
                addRunsToBuilder(builder, runMerger);
                processText(e.asStartElement(), builder);
            } else {
                if (e.isStartElement() && phoneticRunAndPropertyElementSkipper.canSkip(e.asStartElement(), startElementContext.getStartElement())) {
                    phoneticRunAndPropertyElementSkipper.skip(createStartElementContext(e.asStartElement(), startElementContext));
                    continue;
                }

                if (!isWhitespace(e)) {
                    // Flush any outstanding run if there's any markup
                    addRunsToBuilder(builder, runMerger);

                    // Check for end of block
                    if (e.isEndElement() && startElementContext.getStartElement().getName().equals(e.asEndElement().getName())) {
                        builder.addMarkupComponent(createEndMarkupComponent(e.asEndElement()));

                        return builder.createStringItem();
                    } else {
                        builder.addEvent(e);
                    }
                }
            }

        } while (startElementContext.getEventReader().hasNext() && !isStringItemEndEvent(e));
        throw new IllegalStateException("Invalid content? Unterminated string item");
    }



    private void processRun(StringItemBuilder builder, RunMerger runMerger, StartElement startEl) throws XMLStreamException {
        StartElementContext runElementContext = createStartElementContext(startEl, startElementContext);
        RunBuilder runBuilder = new RunParser(runElementContext, nestedBlockIdGenerator, styleDefinitions, false).parse();

        builder.setRunName(startEl.getName());
        builder.setTextName(runBuilder.getTextName());

        runMerger.add(runBuilder);
    }

    private void processText(StartElement startElement, StringItemBuilder builder) throws XMLStreamException {
        XMLEvent event = startElementContext.getEventReader().nextEvent();
        Characters characters;
        EndElement endElement;

        if (event.isEndElement()) {
            characters = startElementContext.getEventFactory().createCharacters("");
            endElement = event.asEndElement();
        } else {
            characters = event.asCharacters();
            endElement = startElementContext.getEventReader().nextEvent().asEndElement();
        }

        StyledText text = new StyledText(startElement, characters, endElement);
        builder.addChunk(text);
    }

    static class StringItemBuilder {
        private QName name;
        private QName textName;
        private List<Chunk> chunks = new ArrayList<>();
        private List<XMLEvent> currentMarkupComponentEvents = new ArrayList<>();
        private Markup markup = new Block.BlockMarkup();

        public void setRunName(QName name) {
            this.name = name;
        }

        public void setTextName(QName textName) {
            this.textName = textName;
        }

        private void flushMarkup() {
            if (!currentMarkupComponentEvents.isEmpty()) {
                markup.addComponent(createGeneralMarkupComponent(currentMarkupComponentEvents));
                currentMarkupComponentEvents = new ArrayList<>();
            }
            if (!markup.getComponents().isEmpty()) {
                chunks.add(markup);
                markup = new Block.BlockMarkup();
            }
        }

        public void addChunk(Chunk chunk) {
            flushMarkup();
            chunks.add(chunk);
        }

        private StringItem createStringItem() {
            flushMarkup();
            return new StringItem(chunks, name, textName);
        }

        void addMarkupComponent(MarkupComponent markupComponent) {
            if (!currentMarkupComponentEvents.isEmpty()) {
                markup.addComponent(createGeneralMarkupComponent(currentMarkupComponentEvents));
                currentMarkupComponentEvents = new ArrayList<>();
            }
            markup.addComponent(markupComponent);
        }

        void addEvent(XMLEvent event) {
            currentMarkupComponentEvents.add(event);
        }
    }

    private void addRunsToBuilder(StringItemBuilder builder, RunMerger runMerger) throws XMLStreamException {
        for (Block.BlockChunk chunk : runMerger.getRuns()) {
            builder.addChunk(chunk);
        }
        runMerger.reset();
    }
}
