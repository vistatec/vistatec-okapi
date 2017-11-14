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

/**
 * Provides methods for glossary elements ids generation.
 *
 * @author Vladyslav Mykhalets
 */
public interface IGlossaryElementIdGenerator {

    /**
     * Provides id for XLIFF glossary entry element <gls:glossEntry/> of the
     * <a href='http://docs.oasis-open.org/xliff/xliff-core/v2.0/xliff-core-v2.0.html#glossary-module'>Glossary module</a>.
     *
     * Must be unique within <gls:glossary/> element.
     *
     * Implementation could be something which makes usage of input parameters. For example:
     *
     * <code>
     * return term.getId() + '-' + String.valueOf(segmentIndex) + '-' + String.valueOf(termIndexInSegment);
     * </code>
     *
     * Or implementations can just generate unique id.
     *
     * @return id
     */
    String generateGlossEntryId(int segmentIndex, int termIndexInSegment, TerminologyQueryResult.Term term);

    /**
     * Provides id for XLIFF glossary translation element <gls:translation/> of the
     * <a href='http://docs.oasis-open.org/xliff/xliff-core/v2.0/xliff-core-v2.0.html#glossary-module'>Glossary module</a>.
     *
     * Must be unique within <gls:glossary/> element.
     *
     * Implementation could be something which makes usage of input parameters. For example:
     *
     * <code>
     * return term.getId() + '-' + String.valueOf(segmentIndex) + '-' + String.valueOf(termIndexInSegment) + '-' + translation.getId();
     * </code>
     *
     * Or implementations can just generate unique id.
     *
     * @return id
     */
    String generateGlossEntryTranslationId(int segmentIndex, int termIndexInSegment, TerminologyQueryResult.Term term, TerminologyQueryResult.Translation translation);
}
