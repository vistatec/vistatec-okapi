/*===========================================================================
  Copyright (C) 2017 by the Okapi Framework contributors
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

package net.sf.okapi.steps.terminologyleveraging;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Wrapper for terminology query results.
 *
 * @author Vladyslav Mykhalets
 */
public class TerminologyQueryResult {

    private Term term;

    private Set<Translation> translations = new LinkedHashSet<>();

    public Term getTerm() {
        return term;
    }

    public void setTerm(Term term) {
        this.term = term;
    }

    public Set<Translation> getTranslations() {
        return translations;
    }

    public void addTranslation(String id, String text) {
        Translation translation = new Translation();
        translation.setId(id);
        translation.setText(text);
        translations.add(translation);
    }

    @Override
    public String toString() {
        return "TerminologyQueryResult{" +
                "term=" + term +
                ", translations=" + translations +
                '}';
    }

    public static class Term {

        private String id;

        private String source;

        private String termText;

        public Term() {
        }

        public Term(String id, String source, String termText) {
            this.id = id;
            this.source = source;
            this.termText = termText;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public String getTermText() {
            return termText;
        }

        public void setTermText(String termText) {
            this.termText = termText;
        }

        @Override
        public String toString() {
            return "Term{" +
                    "id='" + id + '\'' +
                    ", source='" + source + '\'' +
                    ", termText='" + termText + '\'' +
                    '}';
        }
    }

    public static class Translation {

        private String id;

        private String text;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            Translation that = (Translation) o;

            if (!id.equals(that.id))
                return false;
            return text.equals(that.text);
        }

        @Override
        public int hashCode() {
            int result = id.hashCode();
            result = 31 * result + text.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "Translation{" +
                    "id='" + id + '\'' +
                    ", text='" + text + '\'' +
                    '}';
        }
    }
}
