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

import net.sf.okapi.common.Event;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

interface SubDocument {

    /**
     * Opens this part and performs any initial processing.
     *
     * @return First event for this part
     *
     * @throws IOException if any problem is encountered
     */
    Event open() throws IOException, XMLStreamException;

    ZipFile getZipFile();

    ZipEntry getZipEntry();

    String getId();

    boolean hasNextEvent();

    Event nextEvent();

    void close();

    void logEvent(Event e);
}
