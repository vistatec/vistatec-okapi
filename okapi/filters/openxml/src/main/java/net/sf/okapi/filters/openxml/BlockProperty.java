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

import javax.xml.namespace.QName;
import javax.xml.stream.events.XMLEvent;
import java.util.List;

import static net.sf.okapi.filters.openxml.XMLEventHelpers.LOCAL_BIDIRECTIONAL;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.eventEquals;

/**
 * Provides a block property.
 */
class BlockProperty implements Property {

    private static final QName DEFAULT_NAME = new QName("");

    private List<XMLEvent> events;

    BlockProperty(List<XMLEvent> events) {
        this.events = events;
    }

    @Override
    public List<XMLEvent> getEvents() {
        return events;
    }

    @Override
    public QName getName() {
        return null == events.get(0)
                ? DEFAULT_NAME
                : events.get(0).asStartElement().getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockProperty that = (BlockProperty) o;

        return eventEquals(events, that.events);
    }

    @Override
    public int hashCode() {
        return events.hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + XMLEventSerializer.serialize(getEvents()) + ")";
    }
}
