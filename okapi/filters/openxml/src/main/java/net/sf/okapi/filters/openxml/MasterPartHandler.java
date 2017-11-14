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

import java.util.zip.ZipEntry;

import javax.xml.stream.events.XMLEvent;

/**
 * Part handler for master slides (PPTX). We assume that
 * {@link ConditionalParameters#getTranslatePowerpointMasters()} is {@code true}.
 */
public class MasterPartHandler extends StyledTextPartHandler {

    /**
     * {@code true} if the current event is between {@code <p:sp>} and {@code </p:sp>}.
     */
    private boolean withinShape = false;

    /**
     * {@code true} if the current event is between {@code <p:nvPr>} and {@code </p:nvPr>}
     */
    private boolean withinNonVisualProperties = false;

    /**
     * {@code true} if the current shape has the element {@code <p:ph>} in a {@code <p:nvPr>}
     */
    private boolean withinPlaceholder = false;

    private boolean currentBlockTranslatable;

    public MasterPartHandler(ConditionalParameters params, OpenXMLZipFile zipFile, ZipEntry entry,
            StyleDefinitions styleDefinitions) {
        super(params, zipFile, entry, styleDefinitions);
    }

    /**
     * Sets the values of the fields that are needed for translating PPTX masters and slide layouts.
     * If both {@link ConditionalParameters#getTranslatePowerpointMasters()} and
     * {@link ConditionalParameters#getIgnorePlaceholdersInPowerpointMasters()} are set the
     * following rule applies: We only want to translate text in shapes({@code <p:sp>}} that not
     * have the non-visual property element "ph" ({@code <p:nvPr><p:ph .../></p:nvPr>}). These texts
     * are default texts of the master slides.
     *
     * @param e the event
     */
    protected void preHandleNextEvent(XMLEvent e) {
        if (e.isStartElement()) {
            String localPartOfStartElement = e.asStartElement().getName().getLocalPart();
            if (localPartOfStartElement.equals("sp")) {
                withinShape = true;
            }
            if (withinShape
                    && localPartOfStartElement.equals("nvPr")) {
                withinNonVisualProperties = true;
            }
            if (withinNonVisualProperties
                    && localPartOfStartElement.equals("ph")) {
                withinPlaceholder = true;
            }
        }

        if (e.isEndElement()) {
            String localPartOfEndElement = e.asEndElement().getName().getLocalPart();
            if (localPartOfEndElement.equals("sp")) {
                withinShape = false;
                withinNonVisualProperties = false;
                withinPlaceholder = false; // valid until the end of sp
            }
            if (localPartOfEndElement.equals("nvPr")) {
                withinNonVisualProperties = false;
            }
        }

        // Update state
        currentBlockTranslatable = true;
        if (params.getIgnorePlaceholdersInPowerpointMasters() && withinShape) {
            currentBlockTranslatable = !withinPlaceholder;
        }
    }

    /**
     * It depends on {@link ConditionalParameters#getIgnorePlaceholdersInPowerpointMasters()}
     * if a block is translatable.
     * <ul>
     * <li>{@code false}: All block are translatable</li>
     * <li>{@code true}: Only non-placeholder blocks are translatable</li>
     * </ul>.
     */
    protected boolean isCurrentBlockTranslatable() {
        return currentBlockTranslatable;
    }
}
