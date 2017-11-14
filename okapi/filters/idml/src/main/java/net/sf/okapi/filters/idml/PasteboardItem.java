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

import javax.xml.namespace.QName;
import javax.xml.stream.events.Attribute;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

class PasteboardItem {

    private static final String PATH_GEOMETRY_PROPERTY_DOES_NOT_EXIST = "Path geometry property does not exist";

    private final SpreadItem.TextualSpreadItem textualSpreadItem;
    private final List<AnchorPoint> anchorPoints;

    PasteboardItem(SpreadItem.TextualSpreadItem textualSpreadItem, List<AnchorPoint> anchorPoints) {
        this.textualSpreadItem = textualSpreadItem;
        this.anchorPoints = anchorPoints;
    }

    static PasteboardItem fromTextualSpreadItemAndParentTransformations(SpreadItem.TextualSpreadItem spreadItem, Deque<OrderingIdioms.TransformationMatrix> transformationMatrices) {
        Property.PathGeometryProperty property = Properties.getPathGeometryProperty(spreadItem.getProperties());

        if (null == property) {
            throw new IllegalStateException(PATH_GEOMETRY_PROPERTY_DOES_NOT_EXIST);
        }

        PasteboardItemBuilder pasteboardItemBuilder = new PasteboardItemBuilder();
        pasteboardItemBuilder.setSpreadItem(spreadItem);

        for (GeometryPath geometryPath : property.getGeometryPaths()) {

            for (GeometryPath.PathPoint pathPoint : geometryPath.getPathPoints()) {
                pasteboardItemBuilder.addAnchorPoint(AnchorPoint.fromPathPoint(pathPoint, transformationMatrices));
            }
        }

        return pasteboardItemBuilder.build();
    }

    SpreadItem.TextualSpreadItem getTextualSpreadItem() {
        return textualSpreadItem;
    }

    List<AnchorPoint> getAnchorPoints() {
        return anchorPoints;
    }

    AnchorPoint getMinAnchorPointByDirection(OrderingIdioms.Direction direction) {
        Comparator<AnchorPoint> anchorPointComparator = new AnchorPoint.AnchorPointComparator(direction);

        return Collections.min(anchorPoints, anchorPointComparator);
    }

    static class PasteboardItemBuilder implements Builder<PasteboardItem> {

        private SpreadItem.TextualSpreadItem spreadItem;
        private List<AnchorPoint> anchorPoints = new ArrayList<>();

        PasteboardItemBuilder setSpreadItem(SpreadItem.TextualSpreadItem spreadItem) {
            this.spreadItem = spreadItem;
            return this;
        }

        PasteboardItemBuilder addAnchorPoint(AnchorPoint anchorPoint) {
            anchorPoints.add(anchorPoint);
            return this;
        }

        @Override
        public PasteboardItem build() {
            return new PasteboardItem(spreadItem, anchorPoints);
        }
    }

    static class PasteboardItemComparator implements Comparator<PasteboardItem> {

        private final OrderingIdioms.Direction direction;

        PasteboardItemComparator(OrderingIdioms.Direction direction) {
            this.direction = direction;
        }

        @Override
        public int compare(PasteboardItem pasteboardItem, PasteboardItem anotherPasteboardItem) {
            int result;

            if (pasteboardItem.getAnchorPoints().isEmpty() && anotherPasteboardItem.getAnchorPoints().isEmpty()) {
                return 0;
            }

            if (pasteboardItem.getAnchorPoints().isEmpty()) {
                return -1;
            }

            if (anotherPasteboardItem.getAnchorPoints().isEmpty()) {
                return 1;
            }

            result = Double.compare(pasteboardItem.getMinAnchorPointByDirection(direction).getY(),
                    anotherPasteboardItem.getMinAnchorPointByDirection(direction).getY());

            if (0 != result) {
                return result;
            }

            if (OrderingIdioms.Direction.RIGHT_TO_LEFT == direction) {
                return Double.compare(anotherPasteboardItem.getMinAnchorPointByDirection(direction).getX(),
                        pasteboardItem.getMinAnchorPointByDirection(direction).getX());
            }

            return Double.compare(pasteboardItem.getMinAnchorPointByDirection(direction).getX(),
                    anotherPasteboardItem.getMinAnchorPointByDirection(direction).getX());
        }
    }

    static class AnchorPoint {

        private static final QName ANCHOR = Namespaces.getDefaultNamespace().getQName("Anchor");
        private static final String ANCHOR_ATTRIBUTE_DOES_NOT_EXIST = "Anchor attribute does not exist";

        private static final int NUMBER_OF_COORDINATES = 2;
        private static final String UNEXPECTED_NUMBER_OF_COORDINATES = "Unexpected number of coordinates";

        private final double x;
        private final double y;

        AnchorPoint(double x, double y) {
            this.x = x;
            this.y = y;
        }

        static AnchorPoint fromPathPoint(GeometryPath.PathPoint pathPoint, Deque<OrderingIdioms.TransformationMatrix> transformationMatrices) {
            Attribute attribute = pathPoint.getStartElement().getAttributeByName(ANCHOR);

            if (null == attribute) {
                throw new IllegalStateException(ANCHOR_ATTRIBUTE_DOES_NOT_EXIST);
            }

            String[] coordinates = attribute.getValue().split(" ");

            if (NUMBER_OF_COORDINATES != coordinates.length) {
                throw new IllegalStateException(UNEXPECTED_NUMBER_OF_COORDINATES);
            }

            AnchorPoint anchorPoint = new AnchorPoint(Double.valueOf(coordinates[0]), Double.valueOf(coordinates[1]));

            Iterator<OrderingIdioms.TransformationMatrix> transformationMatrixIterator = transformationMatrices.descendingIterator();

            while (transformationMatrixIterator.hasNext()) {
                OrderingIdioms.TransformationMatrix transformationMatrix = transformationMatrixIterator.next();

                anchorPoint = transformationMatrix.transformAnchorPoint(anchorPoint);
            }

            return anchorPoint;
        }

        double getX() {
            return x;
        }

        double getY() {
            return y;
        }

        static class AnchorPointComparator implements Comparator<AnchorPoint> {

            private final OrderingIdioms.Direction direction;

            private AnchorPointComparator(OrderingIdioms.Direction direction) {
                this.direction = direction;
            }

            @Override
            public int compare(AnchorPoint anchorPoint, AnchorPoint anotherAnchorPoint) {
                int result;

                if (OrderingIdioms.Direction.RIGHT_TO_LEFT == direction) {
                    result = Double.compare(anotherAnchorPoint.getX(), anchorPoint.getX());

                    if (0 != result) {
                        return result;
                    }

                    return Double.compare(anotherAnchorPoint.getY(), anchorPoint.getY());
                }

                result = Double.compare(anchorPoint.getX(), anotherAnchorPoint.getX());

                if (0 != result) {
                    return result;
                }

                return Double.compare(anchorPoint.getY(), anotherAnchorPoint.getY());
            }
        }
    }

    static class VisibilityFilter {

        private static final String LAYER_DOES_NOT_EXIST = "Layer does not exist";

        private final List<Layer> layers;
        private final boolean extractHiddenLayers;

        VisibilityFilter(List<Layer> layers, boolean extractHiddenLayers) {
            this.layers = layers;
            this.extractHiddenLayers = extractHiddenLayers;
        }

        List<PasteboardItem> filterVisible(List<PasteboardItem> pasteboardItems) {

            List<PasteboardItem> visiblePasteboardItems = new ArrayList<>(pasteboardItems.size());

            for (PasteboardItem pasteboardItem : pasteboardItems) {
                Layer layer = getLayerById(pasteboardItem.getTextualSpreadItem().getLayerId(), layers);

                if (!extractHiddenLayers && !layer.isVisible()) {
                    continue;
                }

                if (!pasteboardItem.getTextualSpreadItem().isVisible()) {
                    continue;
                }

                visiblePasteboardItems.add(pasteboardItem);
            }

            return visiblePasteboardItems;
        }

        private Layer getLayerById(String layerId, List<Layer> layers) {

            for (Layer layer : layers) {
                if (layerId.equals(layer.getId())) {
                    return layer;
                }
            }

            throw new IllegalStateException(LAYER_DOES_NOT_EXIST);
        }
    }
}
