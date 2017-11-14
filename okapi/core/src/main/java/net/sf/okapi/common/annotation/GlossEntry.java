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

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Wrapper class for XLIFF glossary entry element <gls:glossEntry/> of the
 * <a href='http://docs.oasis-open.org/xliff/xliff-core/v2.0/xliff-core-v2.0.html#glossary-module'>Glossary module</a>.
 *
 * @author Vladyslav Mykhalets
 */
public class GlossEntry implements Iterable<GlossEntry.Translation> {

    private String id;
    private String ref;

    private Term term;
    private Definition definition;
    private Set<Translation> translations = new LinkedHashSet<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public Definition getDefinition() {
        return definition;
    }

    public void setDefinition(Definition definition) {
        this.definition = definition;
    }

    public Term getTerm() {
        return term;
    }

    public void setTerm(Term term) {
        this.term = term;
    }

    public Set<Translation> getTranslations() {
        return translations;
    }

    public void addTranslation(Translation translation) {
        this.translations.add(translation);
    }

    public Iterator<Translation> iterator() {
        return this.translations.iterator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        GlossEntry that = (GlossEntry) o;

        return id.equals(that.id);
    }

    @Override
    public String toString() {
        return "GlossEntry{" +
                "id='" + id + '\'' +
                ", ref='" + ref + '\'' +
                ", term=" + term +
                ", definition=" + definition +
                ", translations=" + translations +
                '}';
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    /**
     * Wrapper class for XLIFF glossary term element <gls:term/> of the
     * <a href='http://docs.oasis-open.org/xliff/xliff-core/v2.0/xliff-core-v2.0.html#glossary-module'>Glossary module</a>.
     *
     */
    public static class Term extends BaseField {
    }

    /**
     * Wrapper class for XLIFF glossary definition element <gls:definition/> of the
     * <a href='http://docs.oasis-open.org/xliff/xliff-core/v2.0/xliff-core-v2.0.html#glossary-module'>Glossary module</a>.
     *
     */
    public static class Definition extends BaseField {
    }

    /**
     * Wrapper class for XLIFF glossary translation element <gls:translation/> of the
     * <a href='http://docs.oasis-open.org/xliff/xliff-core/v2.0/xliff-core-v2.0.html#glossary-module'>Glossary module</a>.
     *
     */
    public static class Translation extends BaseField {

        private String id;
        private String ref;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getRef() {
            return ref;
        }

        public void setRef(String ref) {
            this.ref = ref;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            Translation that = (Translation) o;

            return id.equals(that.id);
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }

        @Override
        public String toString() {
            return "Translation{" +
                    "id='" + this.id + '\'' +
                    ", ref='" + this.ref + '\'' +
                    ", text='" + this.getText() + '\'' +
                    ", source='" + this.getSource() + '\'' +
                    '}';
        }
    }

    /**
     * Base wrapper class for fields of {@link net.sf.okapi.common.annotation.GlossEntry}.
     *
     */
    private static class BaseField {
        private String text;
        private String source;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        @Override
        public String toString() {
            return "BaseField{" +
                    "text='" + text + '\'' +
                    ", source='" + source + '\'' +
                    '}';
        }
    }
}
