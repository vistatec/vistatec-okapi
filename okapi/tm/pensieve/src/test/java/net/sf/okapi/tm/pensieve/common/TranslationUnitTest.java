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

package net.sf.okapi.tm.pensieve.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.TextFragment;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * User: Christian Hargraves
 * Date: Aug 19, 2009
 * Time: 6:49:59 AM
 */
@RunWith(JUnit4.class)
public class TranslationUnitTest {

    TranslationUnit tu;
    final static TranslationUnitVariant SOURCE = new TranslationUnitVariant(LocaleId.fromString("EN"), new TextFragment("Joe McMac"));
    final static TranslationUnitVariant CONTENT = new TranslationUnitVariant(LocaleId.fromString("EN"), new TextFragment("Some content that isn't very long"));

    @Before
    public void setUp(){
        tu = new TranslationUnit();
    }


    @Test
    public void getMetadataValueNullKey(){
        tu.setMetadataValue(MetadataType.ID, "test");
        assertNull("The ID metadata should not exist", tu.getMetadataValue(null));
    }

    @Test
    public void getMetadataValueKey(){
        tu.setMetadataValue(MetadataType.ID, "test");
        assertEquals("The ID metadata", "test", tu.getMetadataValue(MetadataType.ID));
    }

    @Test
    public void setMetadataValueNull(){
        tu.setMetadataValue(MetadataType.ID, null);
        assertFalse("The ID metadata should not exist", tu.getMetadata().containsKey(MetadataType.ID));
    }

    @Test
    public void setMetadataValueEmpty(){
        tu.setMetadataValue(MetadataType.ID, "");
        assertFalse("The ID metadata should not exist", tu.getMetadata().containsKey(MetadataType.ID));
    }

    @Test
    public void setMetadataValue(){
        tu.setMetadataValue(MetadataType.ID, "yipee");
        assertEquals("The ID metadata", "yipee", tu.getMetadata().get(MetadataType.ID));
    }

    @Test
    public void noArgConstructor(){
        tu = new TranslationUnit();
        assertNull("source", tu.getSource());
        assertNull("content", tu.getTarget());
        assertEquals("metadata entries", 0, tu.getMetadata().size());
    }

    @Test
    public void constructor_allParamsPassed(){
        tu = new TranslationUnit(SOURCE, CONTENT);
        assertEquals("source", SOURCE, tu.getSource());
        assertEquals("content", CONTENT, tu.getTarget());
        assertEquals("metadata entries", 0, tu.getMetadata().size());
    }

    @Test
    public void metaDataSetter(){
        tu = new TranslationUnit(SOURCE, CONTENT);
        Metadata md = new Metadata();
        tu.setMetadata(md);
        assertSame("metadata should be the same", md, tu.getMetadata());
    }

    @Test
    public void isSourceEmptyNull(){
        assertTrue("source should be empty", tu.isSourceEmpty());
    }

    @Test
    public void isSourceEmptyEmpty(){
        tu.setSource(new TranslationUnitVariant(LocaleId.fromString("EN"), new TextFragment("")));
        assertTrue("source should be empty", tu.isSourceEmpty());
    }

    @Test
    public void isSourceEmptyNotEmpty(){
        tu.setSource(new TranslationUnitVariant(LocaleId.fromString("EN"), new TextFragment("this is not empty")));
        assertFalse("source should not be empty", tu.isSourceEmpty());
    }

    @Test
    public void isTargetEmptyNull(){
        assertTrue("target should be empty", tu.isTargetEmpty());
    }

    @Test
    public void isTargetEmptyEmpty(){
        tu.setTarget(new TranslationUnitVariant(LocaleId.fromString("EN"), new TextFragment("")));
        assertTrue("target should be empty", tu.isTargetEmpty());
    }

    @Test
    public void isTargetEmptyNotEmpty(){
        tu.setTarget(new TranslationUnitVariant(LocaleId.fromString("EN"), new TextFragment("this is not empty")));
        assertFalse("target should not be empty", tu.isTargetEmpty());
    }
}
