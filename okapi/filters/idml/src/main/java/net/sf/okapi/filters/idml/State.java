/*
 * =============================================================================
 *   Copyright (C) 2010-2013 by the Okapi Framework contributors
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

import java.util.ArrayList;
import java.util.List;

class State {

    private final String id;
    private final boolean active;
    private final List<SpreadItem> spreadItems;

    State(String id, boolean active, List<SpreadItem> spreadItems) {
        this.id = id;
        this.active = active;
        this.spreadItems = spreadItems;
    }

    String getId() {
        return id;
    }

    boolean isActive() {
        return active;
    }

    List<SpreadItem> getSpreadItems() {
        return spreadItems;
    }

    static class StateBuilder implements Builder<State>, SpreadItemHolder {

        private String id;
        private boolean active;
        private String activeLayerId;
        private List<SpreadItem> spreadItems = new ArrayList<>();

        StateBuilder setId(String id) {
            this.id = id;
            return this;
        }

        StateBuilder setActive(boolean active) {
            this.active = active;
            return this;
        }

        @Override
        public String getActiveLayerId() {
            return activeLayerId;
        }

        StateBuilder setActiveLayerId(String activeLayerId) {
            this.activeLayerId = activeLayerId;
            return this;
        }

        @Override
        public StateBuilder addSpreadItem(SpreadItem spreadItem) {
            spreadItems.add(spreadItem);
            return this;
        }

        @Override
        public State build() {
            return new State(id, active, spreadItems);
        }
    }
}
