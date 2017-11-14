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

import net.sf.okapi.common.ISkeleton;
import net.sf.okapi.common.IdGenerator;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextUnit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class StringItemTextUnitMapper extends TextUnitMapper {
    private StringItem stringItem;

    public StringItemTextUnitMapper(StringItem stringItem, IdGenerator idGenerator) {
        super(idGenerator);
        this.stringItem = stringItem;
    }

    public List<ITextUnit> getTextUnits() {
        if (textUnits == null) {
            if (stringItem.isStyled()) {
                textUnits = process();
            } else {
               textUnits = createTextUnit();
            }
        }
        return textUnits;
    }

    public List<ITextUnit> process() {
        // Since blocks typically start and end with markup, blocks with <= 2 chunks should
        // be empty.
        if (stringItem.getChunks().size() <= 2) {
            // Sanity check
            for (XMLEvents chunk : stringItem.getChunks()) {
                if (chunk instanceof Run || chunk instanceof StyledText) {
                    throw new IllegalStateException(ExceptionMessages.UNEXPECTED_STRUCTURE);
                }
            }
            return Collections.emptyList();
        }
        ITextUnit textUnit = new TextUnit(idGenerator.createId());
        textUnit.setPreserveWhitespaces(true);
        TextFragment tf = new TextFragment();
        textUnit.setSource(new TextContainer(tf));

        // The first and last chunks should always be markup.  We skip them.
        List<Chunk> chunks = stringItem.getChunks().subList(1, stringItem.getChunks().size() - 1);
        boolean runHasText = false;
        for (Chunk chunk : chunks) {
            if (chunk instanceof Run) {
                runHasText |= processRun(tf, (Run)chunk, textUnit);
            } else {
                addIsolatedCode(tf, chunk);
            }
        }
        popAllRunCodes(tf);
        List<ITextUnit> tus = new ArrayList<>();
        tus.addAll(referentTus);
        // Runs containing no text can be skipped, but only if they don't
        // contain a reference to an embedded TU.  (If they do, we need
        // to anchor the skeleton here.  It would be possible to fix this,
        // but would require this class to distinguish deferred TUs from real
        // TUs in its return value, so the part handler could make a decision.)
        if (runHasText || !referentTus.isEmpty()) {
            // Deferred TUs already have their own block skeletons set
            ISkeleton skel = new BlockSkeleton(stringItem.getBlock(), codeMap);
            skel.setParent(textUnit);
            textUnit.setSkeleton(skel);
            tus.add(textUnit);
        }
        return tus;
    }

    private List<ITextUnit> createTextUnit() {
        ITextUnit textUnit = new TextUnit(idGenerator.createId());
        textUnit.setPreserveWhitespaces(true);
        TextFragment tf = new TextFragment();
        textUnit.setSource(new TextContainer(tf));
        StyledText styledText = (StyledText) stringItem.getChunks().get(1);

        addText(tf, styledText.getText());

        ISkeleton skel = new StringItemSkeleton(stringItem, codeMap);
        skel.setParent(textUnit);
        textUnit.setSkeleton(skel);

        return Arrays.asList(textUnit);
    }

}
