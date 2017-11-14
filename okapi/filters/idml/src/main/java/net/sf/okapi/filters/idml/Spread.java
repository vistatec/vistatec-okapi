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
import java.util.Comparator;
import java.util.List;

import static net.sf.okapi.filters.idml.OrderingIdioms.TransformationMatrix.fromString;

class Spread {

    private final String id;
    private final String activeLayerId;
    private final String transformation;
    private final List<SpreadItem> spreadItems;

    Spread(String id, String activeLayerId, String transformation, List<SpreadItem> spreadItems) {
        this.id = id;
        this.activeLayerId = activeLayerId;
        this.transformation = transformation;
        this.spreadItems = spreadItems;
    }

    String getId() {
        return id;
    }

    String getActiveLayerId() {
        return activeLayerId;
    }

    String getTransformation() {
        return transformation;
    }

    List<SpreadItem> getSpreadItems() {
        return spreadItems;
    }

    static class SpreadBuilder implements Builder<Spread>, SpreadItemHolder {

        private String id;
        private String activeLayerId;
        private String transformation;
        private List<SpreadItem> spreadItems = new ArrayList<>();;

        SpreadBuilder setId(String id) {
            this.id = id;
            return this;
        }

        @Override
        public String getActiveLayerId() {
            return activeLayerId;
        }

        SpreadBuilder setActiveLayerId(String activeLayerId) {
            this.activeLayerId = activeLayerId;
            return this;
        }

        SpreadBuilder setTransformation(String transformation) {
            this.transformation = transformation;
            return this;
        }

        @Override
        public SpreadBuilder addSpreadItem(SpreadItem spreadItem) {
            spreadItems.add(spreadItem);
            return this;
        }

        @Override
        public Spread build() {
            return new Spread(id, activeLayerId, transformation, spreadItems);
        }
    }

    static class SpreadComparator implements Comparator<Spread> {

        @Override
        public int compare(Spread spread, Spread anotherSpread) {
            OrderingIdioms.TransformationMatrix spreadTransformationMatrix = fromString(spread.getTransformation());
            OrderingIdioms.TransformationMatrix anotherSpreadTransformationMatrix = fromString(anotherSpread.getTransformation());

            return Double.compare(spreadTransformationMatrix.getTy(), anotherSpreadTransformationMatrix.getTy());
        }
    }
}
