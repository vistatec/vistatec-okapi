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
package net.sf.okapi.tm.pensieve.seeker;

import java.io.File;
import java.io.IOException;

import net.sf.okapi.common.exceptions.OkapiIOException;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * @author HARGRAVEJE
 *
 */
public final class TmSeekerFactory {

    private TmSeekerFactory(){}

    /**
     * @param indexDirectoryPath
     * @return a {@link ITmSeeker} initialized for searching a file based index
     */
    public static ITmSeeker createFileBasedTmSeeker(String indexDirectoryPath) {
        Directory dir;
        try{
            File f = new File(indexDirectoryPath);
            if (!f.exists()){
                throw new OkapiIOException(String.format("'%s' does not exist.", indexDirectoryPath));
            }
            dir = FSDirectory.open(f);
        }
        catch (IOException ioe) {
            throw new OkapiIOException(String.format("Trouble creating FSDirectory with the path '%s'.", indexDirectoryPath), ioe);
        }
        catch (NullPointerException npe) {
            throw new OkapiIOException("'indexDirectoryPath' cannot be null");
        }
        return new PensieveSeeker(dir);
    }
    
}
