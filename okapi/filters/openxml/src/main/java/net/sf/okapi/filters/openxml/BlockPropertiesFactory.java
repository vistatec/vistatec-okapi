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

import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import java.util.Collections;
import java.util.List;

import static net.sf.okapi.filters.openxml.XMLEventHelpers.LOCAL_PARAGRAPH_PROPERTIES;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.LOCAL_TABLE_PROPERTIES;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.LOCAL_TEXT_BODY_PROPERTIES;

/**
 * Provides a block properties factory.
 */
class BlockPropertiesFactory {

    /**
     * Creates paragraph properties.
     *
     * @param creationalParameters Creational parameters
     * @param blockProperties      Block properties
     *
     * @return Paragraph properties
     */
    static BlockProperties createParagraphProperties(CreationalParameters creationalParameters, List<BlockProperty> blockProperties) {
        return createBlockProperties(creationalParameters, LOCAL_PARAGRAPH_PROPERTIES, Collections.<Attribute>emptyList(), blockProperties);
    }

    /**
     * Creates text body properties.
     *
     * @param creationalParameters   Creational parameters
     * @param startElementAttributes Start element attributes
     *
     * @return StyledText body properties
     */
    static BlockProperties createTextBodyProperties(CreationalParameters creationalParameters, List<Attribute> startElementAttributes) {
        return createBlockProperties(creationalParameters, LOCAL_TEXT_BODY_PROPERTIES, startElementAttributes, Collections.<BlockProperty>emptyList());
    }

    /**
     * Creates paragraph properties.
     *
     * @param creationalParameters   Creational parameters
     * @param startElementAttributes Start element attributes
     * @param blockProperties        Block properties
     *
     * @return Table properties
     */
    static BlockProperties createTableProperties(CreationalParameters creationalParameters, List<Attribute> startElementAttributes, List<BlockProperty> blockProperties) {
        return createBlockProperties(creationalParameters, LOCAL_TABLE_PROPERTIES, startElementAttributes, blockProperties);
    }

    /**
     * Creates block properties.
     *
     * @param creationalParameters   Creational parameters
     * @param startElementAttributes Start element attributes
     * @param blockProperties        Block properties
     *
     * @return Block properties
     */
    private static BlockProperties createBlockProperties(CreationalParameters creationalParameters, String startElementLocalName, List<Attribute> startElementAttributes, List<BlockProperty> blockProperties) {
        StartElement startElement = creationalParameters.getEventFactory().createStartElement(
                creationalParameters.getPrefix(), creationalParameters.getNamespaceUri(), startElementLocalName, startElementAttributes.iterator(), null);
        EndElement endElement = creationalParameters.getEventFactory().createEndElement(
                creationalParameters.getPrefix(), creationalParameters.getNamespaceUri(), startElementLocalName);

        return new BlockProperties(creationalParameters.getEventFactory(), startElement, endElement, blockProperties);
    }

    static BlockProperties createBlockProperties(XMLEventFactory eventFactory, StartElement startElement, EndElement endElement, List<BlockProperty> properties) {
        return new BlockProperties(eventFactory, startElement, endElement, properties);
    }
}
