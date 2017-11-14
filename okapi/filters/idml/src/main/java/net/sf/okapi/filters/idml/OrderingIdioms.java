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
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

class OrderingIdioms {

    private static final String UNEXPECTED_STORY_PART_NAME = "Unexpected story part name";
    private static final String STORY_PART_NAME_DOES_NOT_EXIST = "Story part name does not exist";

    private static String storyPartNameStart = "Stories/Story_";
    private static String storyPartNameEnd = ".xml";

    private static final String NO_VALUE = "n";

    static List<PasteboardItem> getOrderedPasteboardItems(List<Spread> spreads, String direction) {
        List<PasteboardItem> pasteboardItems = new ArrayList<>();
        Deque<TransformationMatrix> transformationMatrices = new LinkedList<>();

        List<Spread> sortedSpreads = getSortedSpreads(spreads);

        for (Spread spread : sortedSpreads) {
            transformationMatrices.add(TransformationMatrix.fromString(spread.getTransformation()));

            List<PasteboardItem> pasteboardItemsPerSpread = getOrderedPasteboardItems(spread.getSpreadItems(), transformationMatrices);
            sortPasteboardItems(pasteboardItemsPerSpread, Direction.fromString(direction));

            pasteboardItems.addAll(pasteboardItemsPerSpread);

            transformationMatrices.removeLast();
        }

        return pasteboardItems;
    }

    private static List<Spread> getSortedSpreads(List<Spread> spreads) {
        List<Spread> sortedSpreads = new ArrayList<>(spreads);

        Collections.sort(sortedSpreads, new Spread.SpreadComparator());

        return sortedSpreads;
    }

    private static List<PasteboardItem> getOrderedPasteboardItems(List<SpreadItem> spreadItems, Deque<TransformationMatrix> transformationMatrices) {
        List<PasteboardItem> pasteboardItems = new ArrayList<>();

        for (SpreadItem spreadItem : spreadItems) {
            transformationMatrices.add(TransformationMatrix.fromString(spreadItem.getTransformation()));

            if (spreadItem instanceof SpreadItem.TextualSpreadItem) {
                pasteboardItems.add(PasteboardItem.fromTextualSpreadItemAndParentTransformations((SpreadItem.TextualSpreadItem) spreadItem, transformationMatrices));
                transformationMatrices.removeLast();
                continue;
            }

            if (spreadItem instanceof SpreadItem.MultiStateObject) {
                for (State state : ((SpreadItem.MultiStateObject) spreadItem).getStates()) {
                    if (state.isActive()) {
                        pasteboardItems.addAll(getOrderedPasteboardItems(state.getSpreadItems(), transformationMatrices));
                    }
                }
                transformationMatrices.removeLast();
                continue;
            }

            if (spreadItem instanceof SpreadItem.Group) {
                pasteboardItems.addAll(getOrderedPasteboardItems(((SpreadItem.Group) spreadItem).getSpreadItems(), transformationMatrices));
                transformationMatrices.removeLast();
                continue;
            }

            if (spreadItem instanceof SpreadItem.TextBox) {
                pasteboardItems.addAll(getOrderedPasteboardItems(((SpreadItem.TextBox) spreadItem).getSpreadItems(), transformationMatrices));
                transformationMatrices.removeLast();
            }
        }

        return pasteboardItems;
    }

    private static void sortPasteboardItems(List<PasteboardItem> pasteboardItems, Direction direction) {
        Comparator<PasteboardItem> pasteboardItemComparator = new PasteboardItem.PasteboardItemComparator(direction);

        Collections.sort(pasteboardItems, pasteboardItemComparator);
    }

    static List<String> getOrderedStoryIds(List<PasteboardItem> pasteboardItems) {
        List<String> storyIds = new ArrayList<>(pasteboardItems.size());

        for (PasteboardItem pasteboardItem : pasteboardItems) {
            if (pasteboardItem.getTextualSpreadItem() instanceof SpreadItem.TextFrame) {
                if (NO_VALUE.equals(((SpreadItem.TextFrame) pasteboardItem.getTextualSpreadItem()).getPreviousTextFrameId())) {
                    storyIds.add(((SpreadItem.TextFrame) pasteboardItem.getTextualSpreadItem()).getStoryId());
                }
            }

            if (pasteboardItem.getTextualSpreadItem().getTextPaths().isEmpty()) {
                continue;
            }

            storyIds.addAll(getTextPathsStoryIds(pasteboardItem.getTextualSpreadItem().getTextPaths()));
        }

        return storyIds;
    }

    private static List<String> getTextPathsStoryIds(List<TextPath> textPaths) {
        List<String> storyIds = new ArrayList<>(textPaths.size());

        for (TextPath textPath : textPaths) {
            if (NO_VALUE.equals(textPath.getPreviousTextFrameId())) {
                storyIds.add(textPath.getStoryId());
            }
        }

        return storyIds;
    }

    static List<String> getOrderedStoryPartNames(List<String> storyPartNames, List<String> storyIds) {
        List<String> orderedStoryPartNames = new ArrayList<>(storyIds.size());

        for (String storyId : storyIds) {
            orderedStoryPartNames.add(getStoryPartNameByStoryId(storyPartNames, storyId));
        }

        return orderedStoryPartNames;
    }

    private static String getStoryPartNameByStoryId(List<String> storyPartNames, String storyId) {

        for (String storyPartName : storyPartNames) {
            int storyIdEndIndex = storyPartName.indexOf(storyPartNameEnd, storyPartNameStart.length());

            if (-1 == storyIdEndIndex) {
                throw new IllegalStateException(UNEXPECTED_STORY_PART_NAME);
            }

            if (storyId.equals(storyPartName.substring(storyPartNameStart.length(), storyIdEndIndex))) {
                return storyPartName;
            }
        }

        throw new IllegalStateException(STORY_PART_NAME_DOES_NOT_EXIST);
    }

    static class TransformationMatrix {

        private static final String UNEXPECTED_NUMBER_OF_TRANSFORMATION_ATTRIBUTES = "Unexpected number of transformation attributes";

        private static final int NUMBER_OF_TRANSFORMATION_ATTRIBUTES = 6;

        private final double a;
        private final double b;
        private final double c;
        private final double d;
        private final double tx;
        private final double ty;

        TransformationMatrix(double a, double b, double c, double d, double tx, double ty) {
            this.a = a;
            this.b = b;
            this.c = c;
            this.d = d;
            this.tx = tx;
            this.ty = ty;
        }

        static TransformationMatrix fromString(String string) {
            String[] attributes = string.split(" ");

            if (NUMBER_OF_TRANSFORMATION_ATTRIBUTES != attributes.length) {
                throw new IllegalStateException(UNEXPECTED_NUMBER_OF_TRANSFORMATION_ATTRIBUTES);
            }

            return new TransformationMatrix(
                    Double.valueOf(attributes[0]),
                    Double.valueOf(attributes[1]),
                    Double.valueOf(attributes[2]),
                    Double.valueOf(attributes[3]),
                    Double.valueOf(attributes[4]),
                    Double.valueOf(attributes[5])
            );
        }

        double getA() {
            return a;
        }

        double getB() {
            return b;
        }

        double getC() {
            return c;
        }

        double getD() {
            return d;
        }

        double getTx() {
            return tx;
        }

        double getTy() {
            return ty;
        }

        PasteboardItem.AnchorPoint transformAnchorPoint(PasteboardItem.AnchorPoint anchorPoint) {
            double x = a * anchorPoint.getX() + c * anchorPoint.getY() + tx;
            double y = b * anchorPoint.getX() + d * anchorPoint.getY() + ty;

            return new PasteboardItem.AnchorPoint(x, y);
        }
    }

    enum Direction {

        LEFT_TO_RIGHT("LeftToRightDirection"),
        RIGHT_TO_LEFT("RightToLeftDirection");

        String value;

        Direction(String value) {
            this.value = value;
        }

        static Direction fromString(String string) {

            for (Direction direction : values()) {
                if (direction.getValue().equals(string)) {
                    return direction;
                }
            }

            return LEFT_TO_RIGHT;
        }

        String getValue() {
            return value;
        }
    }
}
