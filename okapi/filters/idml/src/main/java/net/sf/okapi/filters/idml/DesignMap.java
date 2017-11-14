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

class DesignMap {

    private final String id;
    private final List<String> storyIds;
    private final String activeLayerId;
    private final String graphicPartName;
    private final String fontsPartName;
    private final String stylesPartName;
    private final String preferencesPartName;
    private final String tagsPartName;
    private final List<Layer> layers;
    private final List<String> masterSpreadPartNames;
    private final List<String> spreadPartNames;
    private final String backingStoryPartName;
    private final List<String> storyPartNames;

    DesignMap(String id, List<String> storyIds, String activeLayerId, String graphicPartName, String fontsPartName, String stylesPartName,
              String preferencesPartName, String tagsPartName, List<Layer> layers, List<String> masterSpreadPartNames,
              List<String> spreadPartNames, String backingStoryPartName, List<String> storyPartNames) {
        this.id = id;
        this.storyIds = storyIds;
        this.activeLayerId = activeLayerId;
        this.graphicPartName = graphicPartName;
        this.fontsPartName = fontsPartName;
        this.stylesPartName = stylesPartName;
        this.preferencesPartName = preferencesPartName;
        this.tagsPartName = tagsPartName;
        this.layers = layers;
        this.masterSpreadPartNames = masterSpreadPartNames;
        this.spreadPartNames = spreadPartNames;
        this.backingStoryPartName = backingStoryPartName;
        this.storyPartNames = storyPartNames;
    }

    String getId() {
        return id;
    }

    List<String> getStoryIds() {
        return storyIds;
    }

    String getActiveLayerId() {
        return activeLayerId;
    }

    String getGraphicPartName() {
        return graphicPartName;
    }

    String getFontsPartName() {
        return fontsPartName;
    }

    String getStylesPartName() {
        return stylesPartName;
    }

    String getPreferencesPartName() {
        return preferencesPartName;
    }

    String getTagsPartName() {
        return tagsPartName;
    }

    List<Layer> getLayers() {
        return layers;
    }

    List<String> getMasterSpreadPartNames() {
        return masterSpreadPartNames;
    }

    List<String> getSpreadPartNames() {
        return spreadPartNames;
    }

    String getBackingStoryPartName() {
        return backingStoryPartName;
    }

    List<String> getStoryPartNames() {
        return storyPartNames;
    }

    static class DesignMapBuilder {

        private String id;
        private List<String> storyIds;
        private String activeLayerId;
        private String graphicPartName;
        private String fontsPartName;
        private String stylesPartName;
        private String preferencesPartName;
        private String tagsPartName;
        private List<Layer> layers;
        private List<String> masterSpreadPartNames;
        private List<String> spreadPartNames;
        private String backingStoryPartName;
        private List<String> storyPartNames;

        DesignMapBuilder setId(String id) {
            this.id = id;
            return this;
        }

        DesignMapBuilder setStoryIds(List<String> storyIds) {
            this.storyIds = storyIds;
            return this;
        }

        DesignMapBuilder setActiveLayerId(String activeLayerId) {
            this.activeLayerId = activeLayerId;
            return this;
        }

        DesignMapBuilder setGraphicPartName(String graphicPartName) {
            this.graphicPartName = graphicPartName;
            return this;
        }

        DesignMapBuilder setFontsPartName(String fontsPartName) {
            this.fontsPartName = fontsPartName;
            return this;
        }

        DesignMapBuilder setStylesPartName(String stylesPartName) {
            this.stylesPartName = stylesPartName;
            return this;
        }

        DesignMapBuilder setPreferencesPartName(String preferencesPartName) {
            this.preferencesPartName = preferencesPartName;
            return this;
        }

        DesignMapBuilder setTagsPartName(String tagsPartName) {
            this.tagsPartName = tagsPartName;
            return this;
        }

        DesignMapBuilder addLayer(Layer layer) {
            if (null == layers) {
                layers = new ArrayList<>();
            }

            layers.add(layer);
            return this;
        }

        DesignMapBuilder addMasterSpreadPartName(String masterSpreadPartName) {
            if (null == masterSpreadPartNames) {
                masterSpreadPartNames = new ArrayList<>();
            }

            masterSpreadPartNames.add(masterSpreadPartName);
            return this;
        }

        DesignMapBuilder addSpreadPartName(String spreadPartName) {
            if (null == spreadPartNames) {
                spreadPartNames = new ArrayList<>();
            }

            spreadPartNames.add(spreadPartName);
            return this;
        }

        DesignMapBuilder setBackingStoryPartName(String backingStoryPartName) {
            this.backingStoryPartName = backingStoryPartName;
            return this;
        }

        DesignMapBuilder addStoryPartName(String storyPartName) {
            if (null == storyPartNames) {
                storyPartNames = new ArrayList<>();
            }

            storyPartNames.add(storyPartName);
            return this;
        }

        DesignMap build() {
            return new DesignMap(id, storyIds, activeLayerId, graphicPartName, fontsPartName, stylesPartName, preferencesPartName,
                    tagsPartName, layers, masterSpreadPartNames, spreadPartNames, backingStoryPartName, storyPartNames);
        }
    }
}
