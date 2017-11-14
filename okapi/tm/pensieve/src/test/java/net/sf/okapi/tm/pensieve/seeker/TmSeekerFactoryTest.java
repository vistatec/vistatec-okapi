/*===========================================================================
  Copyright (C) 2009 by the Okapi Framework contributors
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

import static org.junit.Assert.assertTrue;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.tm.pensieve.Helper;

import org.apache.lucene.store.FSDirectory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TmSeekerFactoryTest {

    @Test
    public void createFileBasedTmSeeker() {
        PensieveSeeker pensieveSeeker = (PensieveSeeker) TmSeekerFactory.createFileBasedTmSeeker("target/test-classes/");
        assertTrue("indexDir should be filebased", pensieveSeeker.getIndexDir() instanceof FSDirectory);
    }

    @Test(expected = OkapiIOException.class)
    public void createFileBasedTmSeekerNotDirectory() {
        TmSeekerFactory.createFileBasedTmSeeker("pom.xml");
    }

    @Test(expected = OkapiIOException.class)
    public void createFileBasedTmSeekerBadDirectory() {
        TmSeekerFactory.createFileBasedTmSeeker("prettymuch/a/non/existent/directory");
    }

    @Test(expected = OkapiIOException.class)
    public void createFileBasedTmSeekerNullDirectory() {
        TmSeekerFactory.createFileBasedTmSeeker(null);
    }

    @Test
    public void stupidCoberturaPrivateConstructorTest() throws Exception {
        Helper.genericTestConstructor(TmSeekerFactory.class);
    }
}
