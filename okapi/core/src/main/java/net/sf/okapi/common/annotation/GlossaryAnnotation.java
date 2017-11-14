/*===========================================================================
  Copyright (C) 2010-2011 by the Okapi Framework contributors
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

package net.sf.okapi.common.annotation;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Wrapper class for XLIFF glossary element <gls:glossary/> of the
 * <a href='http://docs.oasis-open.org/xliff/xliff-core/v2.0/xliff-core-v2.0.html#glossary-module'>Glossary module</a>.
 *
 * Also contains additional information if to annotate source and target segments
 * with references to generated glossary entry ids.
 *
 * @author Vladyslav Mykhalets
 */
public class GlossaryAnnotation implements IAnnotation, Iterable<GlossEntry> {

    private boolean annotateSourceSegment = false;

    private boolean annotateTargetSegment = false;

    private ArrayList<GlossEntry> list = new ArrayList<>();

    public void add(GlossEntry alt) {
        list.add(alt);
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public GlossEntry get(int index) {
        return list.get(index);
    }

    @Override
    public Iterator<GlossEntry> iterator() {
        return list.iterator();
    }

    public boolean isAnnotateSourceSegment() {
        return annotateSourceSegment;
    }

    public void setAnnotateSourceSegment(boolean annotateSourceSegment) {
        this.annotateSourceSegment = annotateSourceSegment;
    }

    public boolean isAnnotateTargetSegment() {
        return annotateTargetSegment;
    }

    public void setAnnotateTargetSegment(boolean annotateTargetSegment) {
        this.annotateTargetSegment = annotateTargetSegment;
    }
}
