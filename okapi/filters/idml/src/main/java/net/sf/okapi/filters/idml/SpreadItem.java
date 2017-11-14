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

/**
 * Represents a spread item (or page item as it is called in the specification).
 */
class SpreadItem {

    private final String id;
    private final String layerId;
    private final boolean visible;
    private final String transformation;
    private final List<Property> properties;

    SpreadItem(String id, String layerId, boolean visible, String transformation, List<Property> properties) {
        this.id = id;
        this.layerId = layerId;
        this.visible = visible;
        this.transformation = transformation;
        this.properties = properties;
    }

    String getId() {
        return id;
    }

    String getLayerId() {
        return layerId;
    }

    boolean isVisible() {
        return visible;
    }

    String getTransformation() {
        return transformation;
    }

    List<Property> getProperties() {
        return properties;
    }

    static abstract class SpreadItemBuilder implements Builder<SpreadItem> {

        protected String id;
        protected String layerId;
        protected boolean visible;
        protected String transformation;
        protected List<Property> properties = new ArrayList<>();

        SpreadItemBuilder setId(String id) {
            this.id = id;
            return this;
        }

        SpreadItemBuilder setLayerId(String layerId) {
            this.layerId = layerId;
            return this;
        }

        SpreadItemBuilder setVisible(boolean visible) {
            this.visible = visible;
            return this;
        }

        SpreadItemBuilder setTransformation(String transformation) {
            this.transformation = transformation;
            return this;
        }

        SpreadItemBuilder addProperty(Property property) {
            properties.add(property);
            return this;
        }
    }

    static class TextualSpreadItem extends SpreadItem {

        private final List<TextPath> textPaths;

        TextualSpreadItem(String id, String layerId, boolean visible, String transformation, List<Property> properties, List<TextPath> textPaths) {
            super(id, layerId, visible, transformation, properties);
            this.textPaths = textPaths;
        }

        List<TextPath> getTextPaths() {
            return textPaths;
        }

        static class TextualSpreadItemBuilder extends SpreadItemBuilder {

            protected List<TextPath> textPaths = new ArrayList<>();

            SpreadItemBuilder addTextPath(TextPath textPath) {
                textPaths.add(textPath);
                return this;
            }

            @Override
            public TextualSpreadItem build() {
                return new TextualSpreadItem(id, layerId, visible, transformation, properties, textPaths);
            }
        }
    }

    static class TextFrame extends TextualSpreadItem {

        private final String storyId;
        private final String previousTextFrameId;
        private final String nextTextFrameId;

        TextFrame(String id, String layerId, boolean visible, String transformation, List<Property> properties, List<TextPath> textPaths,
                  String storyId, String previousTextFrameId, String nextTextFrameId) {
            super(id, layerId, visible, transformation, properties, textPaths);

            this.storyId = storyId;
            this.previousTextFrameId = previousTextFrameId;
            this.nextTextFrameId = nextTextFrameId;
        }

        String getStoryId() {
            return storyId;
        }

        String getPreviousTextFrameId() {
            return previousTextFrameId;
        }

        String getNextTextFrameId() {
            return nextTextFrameId;
        }

        static class TextFrameBuilder extends TextualSpreadItemBuilder {

            private String storyId;
            private String previousTextFrameId;
            private String nextTextFrameId;

            TextFrameBuilder setStoryId(String storyId) {
                this.storyId = storyId;
                return this;
            }

            TextFrameBuilder setPreviousTextFrameId(String previousTextFrameId) {
                this.previousTextFrameId = previousTextFrameId;
                return this;
            }

            TextFrameBuilder setNextTextFrameId(String nextTextFrameId) {
                this.nextTextFrameId = nextTextFrameId;
                return this;
            }

            @Override
            public TextFrame build() {
                return new TextFrame(id, layerId, visible, transformation, properties, textPaths,
                        storyId, previousTextFrameId, nextTextFrameId);
            }
        }
    }

    static class GraphicLine extends TextualSpreadItem {

        GraphicLine(String id, String layerId, boolean visible, String transformation, List<Property> properties, List<TextPath> textPaths) {
            super(id, layerId, visible, transformation, properties, textPaths);
        }

        static class GraphicLineBuilder extends TextualSpreadItemBuilder {

            @Override
            public GraphicLine build() {
                return new GraphicLine(id, layerId, visible, transformation, properties, textPaths);
            }
        }
    }

    static class Rectangle extends TextualSpreadItem {

        Rectangle(String id, String layerId, boolean visible, String transformation, List<Property> properties, List<TextPath> textPaths) {
            super(id, layerId, visible, transformation, properties, textPaths);
        }

        static class RectangleBuilder extends TextualSpreadItemBuilder {

            @Override
            public Rectangle build() {
                return new Rectangle(id, layerId, visible, transformation, properties, textPaths);
            }
        }
    }

    static class Oval extends TextualSpreadItem {

        Oval(String id, String layerId, boolean visible, String transformation, List<Property> properties, List<TextPath> textPaths) {
            super(id, layerId, visible, transformation, properties, textPaths);
        }

        static class OvalBuilder extends TextualSpreadItemBuilder {

            @Override
            public Oval build() {
                return new Oval(id, layerId, visible, transformation, properties, textPaths);
            }
        }
    }

    static class Polygon extends TextualSpreadItem {

        Polygon(String id, String layerId, boolean visible, String transformation, List<Property> properties, List<TextPath> textPaths) {
            super(id, layerId, visible, transformation, properties, textPaths);
        }

        static class PolygonBuilder extends TextualSpreadItemBuilder {

            @Override
            public Polygon build() {
                return new Polygon(id, layerId, visible, transformation, properties, textPaths);
            }
        }
    }

    static class Group extends SpreadItem {

        private final List<SpreadItem> spreadItems;

        Group(String id, String layerId, boolean visible, String transformation, List<Property> properties, List<SpreadItem> spreadItems) {
            super(id, layerId, visible, transformation, properties);
            this.spreadItems = spreadItems;
        }

        List<SpreadItem> getSpreadItems() {
            return spreadItems;
        }

        static class GroupBuilder extends SpreadItemBuilder implements SpreadItemHolder {

            private List<SpreadItem> spreadItems = new ArrayList<>();

            @Override
            public GroupBuilder addSpreadItem(SpreadItem spreadItem) {
                spreadItems.add(spreadItem);
                return this;
            }

            @Override
            public String getActiveLayerId() {
                return layerId;
            }

            @Override
            public Group build() {
                return new Group(id, layerId, visible, transformation, properties, spreadItems);
            }
        }
    }

    static class MultiStateObject extends SpreadItem {

        private final List<State> states;

        MultiStateObject(String id, String layerId, boolean visible, String transformation, List<Property> properties, List<State> states) {

            super(id, layerId, visible, transformation, properties);
            this.states = states;
        }

        List<State> getStates() {
            return states;
        }

        static class MultiStateObjectBuilder extends SpreadItemBuilder {

            protected List<State> states = new ArrayList<>();

            MultiStateObjectBuilder addState(State state) {
                states.add(state);
                return this;
            }

            @Override
            public MultiStateObject build() {
                return new MultiStateObject(id, layerId, visible, transformation, properties, states);
            }
        }
    }

    static class Button extends MultiStateObject {

        Button(String id, String layerId, boolean visible, String transformation, List<Property> properties, List<State> states) {
            super(id, layerId, visible, transformation, properties, states);
        }

        static class ButtonBuilder extends MultiStateObjectBuilder {

            @Override
            public Button build() {
                return new Button(id, layerId, visible, transformation, properties, states);
            }
        }
    }

    static class TextBox extends SpreadItem {

        private final List<SpreadItem> spreadItems;

        TextBox(String id, String layerId, boolean visible, String transformation, List<Property> properties, List<SpreadItem> spreadItems) {
            super(id, layerId, visible, transformation, properties);
            this.spreadItems = spreadItems;
        }

        List<SpreadItem> getSpreadItems() {
            return spreadItems;
        }

        static class TextBoxBuilder extends SpreadItemBuilder implements SpreadItemHolder {

            private List<SpreadItem> spreadItems = new ArrayList<>();

            @Override
            public TextBoxBuilder addSpreadItem(SpreadItem spreadItem) {
                spreadItems.add(spreadItem);
                return this;
            }

            @Override
            public String getActiveLayerId() {
                return layerId;
            }

            @Override
            public TextBox build() {
                return new TextBox(id, layerId, visible, transformation, properties, spreadItems);
            }
        }
    }
}
