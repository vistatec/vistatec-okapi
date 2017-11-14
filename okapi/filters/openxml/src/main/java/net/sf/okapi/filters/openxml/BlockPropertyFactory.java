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
import javax.xml.stream.events.XMLEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Provides a block property factory.
 */
class BlockPropertyFactory {

    private final static int DEFAULT_EVENTS_SIZE = 2;

    /**
     * Creates a block property.
     *
     * @param events XML events
     *
     * @return A block property
     */
    static BlockProperty createBlockProperty(List<XMLEvent> events) {
        return new BlockProperty(events);
    }

    /**
     * Creates a block property.
     *
     * @param creationalParameters Creational parameters
     * @param localName            A local name
     * @param attributes           Attributes
     *
     * @return A block property
     */
    static BlockProperty createBlockProperty(CreationalParameters creationalParameters, String localName, Map<String, String> attributes) {
        List<XMLEvent> events = new ArrayList<>(DEFAULT_EVENTS_SIZE);

        List<Attribute> attributeList = new ArrayList<>(attributes.size());

        for (Map.Entry<String, String> attribute : attributes.entrySet()) {
            attributeList.add(creationalParameters.getEventFactory().createAttribute(
                    creationalParameters.getPrefix(), creationalParameters.getNamespaceUri(), attribute.getKey(), attribute.getValue()));
        }

        events.add(creationalParameters.getEventFactory().createStartElement(
                creationalParameters.getPrefix(), creationalParameters.getNamespaceUri(), localName, attributeList.iterator(), null));
        events.add(creationalParameters.getEventFactory().createEndElement(
                creationalParameters.getPrefix(), creationalParameters.getNamespaceUri(), localName));

        return new BlockProperty(events);
    }
}
