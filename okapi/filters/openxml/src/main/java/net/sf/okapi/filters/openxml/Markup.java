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

import javax.xml.stream.events.XMLEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides a markup.
 */
class Markup implements Chunk {
    private List<MarkupComponent> components = new ArrayList<>();

    @Override
    public List<XMLEvent> getEvents() {
        List<XMLEvent> events = new ArrayList<>();

        for (MarkupComponent component : components) {
            events.addAll(component.getEvents());
        }

        return events;
    }

    Markup addComponent(MarkupComponent component) {
        components.add(component);

        return this;
    }

    List<MarkupComponent> getComponents() {
        return components;
    }

    Nameable getNameableMarkupComponent() {
        for (MarkupComponent markupComponent : components) {
            if (markupComponent instanceof Nameable) {
                return (Nameable) markupComponent;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + components.size() + ") " + components;
    }
}