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

import net.sf.okapi.common.resource.ISegments;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;

import javax.xml.stream.XMLEventFactory;

public class StringItemTextUnitWriter implements TextUnitWriter {
    private final XMLEventFactory eventFactory;
    private final StringItem stringItem;
    private final XMLEventSerializer xmlWriter;

    public StringItemTextUnitWriter(XMLEventFactory eventFactory, StringItem stringItem, XMLEventSerializer xmlWriter) {
        this.eventFactory = eventFactory;
        this.stringItem = stringItem;
        this.xmlWriter = xmlWriter;
    }

    public void write(TextContainer tc) {
        StyledText styledText = ((StyledText) stringItem.getChunks().get(1));
        xmlWriter.add(styledText.startElement);

        String textContent = new TextContentAssembler(tc.getSegments()).assemble();

        if (!textContent.isEmpty()) {
            xmlWriter.add(eventFactory.createCharacters(textContent));
        }

        xmlWriter.add(styledText.endElement);
    }

    private static class TextContentAssembler {
        private ISegments segments;

        TextContentAssembler(ISegments segments) {
            this.segments = segments;
        }

        String assemble() {
            StringBuilder textContentBuilder = new StringBuilder();

            for (Segment segment : segments) {
                assembleSegmentContent(segment, textContentBuilder);
            }

            return textContentBuilder.toString();
        }

        private void assembleSegmentContent(Segment segment, StringBuilder textContentBuilder) {
            TextFragment content = segment.getContent();
            String codedText = content.getCodedText();

            for (int i = 0; i < codedText.length(); i++) {
                char c = codedText.charAt(i);
                textContentBuilder.append(c);
            }
        }
    }
}
