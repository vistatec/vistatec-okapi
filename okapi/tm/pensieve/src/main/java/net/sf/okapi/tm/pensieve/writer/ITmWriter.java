/*===========================================================================
  Copyright (C) 2008-2009 by the Okapi Framework contributors
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

package net.sf.okapi.tm.pensieve.writer;

import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.tm.pensieve.common.TranslationUnit;

/**
 *
 * @author HaslamJD
 * @author HARGRAVEJE
 */
public interface ITmWriter {

    /**
     * Closes the index and forces a commit against the index.
     * @throws OkapiIOException if the commit can not happen
     */
    void close();

    /**
     * Indexes a given translation unit.
     * @param tu the translation unit to index.
     * @throws OkapiIOException if the indexing cannot happen.
     */
    void indexTranslationUnit(TranslationUnit tu);

    /**
     * Indexes a given translation unit, possibly overwriting an existing one with the same source.
     * @param tu the translation unit to index.
     * @param overwrite true to overwrite any existing unit that has the same source content (codes excluded)
     */
    public void indexTranslationUnit (TranslationUnit tu,
    	boolean overwrite);
    
    /**
     * Deletes a TranslationUnit based on the id.
     * @param id The Unique ID of the TU to delete
     * @throws OkapiIOException if the delete can not happen
     */
    void delete(String id);

    /**
     * Updates a TranslationUnit.
     * @param tu The TranslationUnit to update
     * @throws OkapiIOException if the update can not happen
     */
    public void update(TranslationUnit tu);
    
    /**
     * Forces a commit against the index.
     * @throws OkapiIOException if the commit can not happen
     */
    void commit();
}
