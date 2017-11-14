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

import javax.xml.namespace.QName;
import javax.xml.stream.events.XMLEvent;
import java.util.ArrayList;
import java.util.List;

class StringItem implements XMLEvents {
    private QName runName;
    private QName textName;
    private List<Chunk> chunks;


    StringItem(List<Chunk> chunks, QName runName, QName textName) {
        this.chunks = chunks;
        this.runName = runName;
        this.textName = textName;
    }

    /**
     * Return the QName of the element that contains run data in this block.
     */
    QName getRunName() {
        return runName;
    }

    /**
     * Return the QName of the element that contains text data in this block.
     */
    QName getTextName() {
        return textName;
    }

    @Override
    public List<XMLEvent> getEvents() {
        List<XMLEvent> events = new ArrayList<>();
        for (XMLEvents chunk : chunks) {
            events.addAll(chunk.getEvents());
        }
        return events;
    }

    public List<Chunk> getChunks() {
        return chunks;
    }

    Block getBlock() {
        return new Block(chunks, runName, textName, false);
    }

    public boolean isStyled() {
        for (Chunk chunk: chunks) {
            if (chunk instanceof Run) {
                return true;
            }
        }
        return false;
    }
}
