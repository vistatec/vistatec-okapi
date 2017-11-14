/*
 * =============================================================================
 *   Copyright (C) 2010-2017 by the Okapi Framework contributors
 * -----------------------------------------------------------------------------
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * =============================================================================
 */

package net.sf.okapi.filters.idml;

import net.sf.okapi.common.Event;

import java.util.ArrayList;
import java.util.List;

class ReferenceableEvent {

    private final String eventId;
    private final Event event;
    private final List<ReferenceableEvent> referentEvents;

    ReferenceableEvent(String eventId, Event event) {
        this.eventId = eventId;
        this.event = event;
        referentEvents = new ArrayList<>();
    }

    String getEventId() {
        return eventId;
    }

    Event getEvent() {
        return event;
    }

    List<ReferenceableEvent> getReferentEvents() {
        return referentEvents;
    }

    void addReferentEvent(ReferenceableEvent referentEvent) {
        referentEvents.add(referentEvent);
    }
}
