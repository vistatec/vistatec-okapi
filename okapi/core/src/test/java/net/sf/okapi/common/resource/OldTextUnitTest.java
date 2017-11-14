/*===========================================================================
  Copyright (C) 2008-2011 by the Okapi Framework contributors
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
============================================================================*/

package net.sf.okapi.common.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import net.sf.okapi.common.IResource;
import net.sf.okapi.common.LocaleId;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class OldTextUnitTest {

    private static final LocaleId locFR = LocaleId.fromString("fr");
    private static final String TU1 = "tu1";
    private TextContainer tc1;
    ITextUnit tu1;

    @Before
    public void setUp(){
        tu1 = new TextUnit(TU1);
        tc1 = new TextContainer("fr text");
    }

    @Test
    public void isEmptyTrue(){
        assertTrue("The TextUnit should be empty", tu1.isEmpty());
    }

    @Test
    public void isEmptyFalse(){
        tu1.setSource(tc1);
        assertFalse("The TextUnit should not be empty", tu1.isEmpty());
    }

    @Test
    public void toStringFromSource(){
        tu1.setSource(tc1);
        assertEquals("TextUnit.toString()",  "fr text", tu1.toString());
    }

	@Test
	public void getSetSource () {
		tu1.setSource(tc1);
		assertSame(tu1.getSource(), tc1);
	}

    @Test
    public void getTargetReturnsNullOnNoMatch(){
        assertNull("When there is no match a null should be returned", tu1.getTarget(locFR));
    }

	@Test
	public void getSetTarget () {
		tu1.setTarget(locFR, tc1);
		assertSame("The target should be TextContainer we just set", tc1, tu1.getTarget(locFR));
	}

    @Test
    public void hasTargetNo(){
        assertFalse("No target should exist", tu1.hasTarget(locFR));
    }

    @Test
	public void hasTargetYes () {
		tu1.setTarget(locFR, tc1);
		assertTrue("TextUnit should now have a target", tu1.hasTarget(locFR));
	}

    @Test
	public void hasTargetCaseSensitive () {
		tu1.setTarget(locFR, tc1);
		// Language is now *not* case sensitive
		assertTrue(tu1.hasTarget(LocaleId.fromString("FR")));
		// Still: "fr" different from "fr-fr"
		assertTrue( ! tu1.hasTarget(LocaleId.fromString("fr-fr")));
	}

    @Test
    public void removeTarget(){
        tu1.setTarget(locFR, tc1);
        tu1.removeTarget(locFR);
        assertFalse("TextUnit should no longer have a target", tu1.hasTarget(locFR));
    }

    @Test
    public void createTargetSourceContentAndTargetContentSame(){
        tu1.setSource(tc1);
        tu1.createTarget(locFR, false, IResource.COPY_ALL);
        assertEquals("Target text vs Source Text", tu1.getSource().toString(), tu1.getTarget(locFR).toString());
    }

    @Test
	public void createTargetDoesntAlreadyExist () {
		tu1.setSource(tc1);
		TextContainer tc2 = tu1.createTarget(locFR, false, IResource.COPY_ALL);
		assertSame("Target should be the same as returned from createTarget", tc2, tu1.getTarget(locFR));
		assertNotSame("Target should have been cloned", tu1.getTarget(locFR), tu1.getSource());
    }

    @Test
    public void createTargetAlreadyExistsDontOverwriteExisting () {
		// Do not override existing target
		tu1.setSource(tc1);
		TextContainer tc2 = new TextContainer("unique fr text");
		tu1.setTarget(locFR, tc2);
		tu1.createTarget(locFR, false, IResource.COPY_ALL);
		assertSame("Target should not have been modified", tc2, tu1.getTarget(locFR));
    }

    @Test
    public void createTargetAlreadyExistsOverwriteExisting () {
        tu1.setSource(tc1);
        TextContainer tc2 = new TextContainer("unique fr text");
        tu1.setTarget(locFR, tc2);
        tu1.createTarget(locFR, true, IResource.COPY_ALL);
        assertNotSame("Target should not have been modified", tc2, tu1.getTarget(locFR));
	}

    @Test
    public void createTargetEmptyOption () {
        tu1.setSource(tc1);
        tu1.createTarget(locFR, false, IResource.CREATE_EMPTY);
        assertTrue(tu1.hasTarget(locFR));
        assertEquals("Empty target created", "", tu1.getTarget(locFR).toString());
	}
    
    @Test
    public void createTargetEmptyOptionOverwriteExisting () {
        tu1.setSource(tc1);
        TextContainer tc2 = new TextContainer("unique fr text");
        tu1.setTarget(locFR, tc2);
        tu1.createTarget(locFR, true, IResource.CREATE_EMPTY);
        assertTrue(tu1.hasTarget(locFR));
        assertEquals("Empty target created", "", tu1.getTarget(locFR).toString());
	}
    
    @Test
    public void createTargetPropertiesOption () {
        tu1.setSource(tc1);
        tu1.getSource().setProperty(new Property("test", "value"));
        tu1.createTarget(locFR, false, IResource.COPY_PROPERTIES);
        assertTrue(tu1.hasTarget(locFR));
        assertEquals("Empty target created", "", tu1.getTarget(locFR).toString());
        assertTrue(tu1.getTarget(locFR).getProperty("test") != null);
	}
    
    @Test
    public void createTargetPropertiesOptionOverwriteExisting () {
        tu1.setSource(tc1);
        tu1.getSource().setProperty(new Property("test", "value"));
        TextContainer tc2 = new TextContainer("unique fr text");
        tu1.setTarget(locFR, tc2);
        tu1.createTarget(locFR, true, IResource.COPY_PROPERTIES);
        assertTrue(tu1.hasTarget(locFR));
        assertEquals("Empty target created", "", tu1.getTarget(locFR).toString());
        assertTrue(tu1.getTarget(locFR).getProperty("test") != null);
	}
    
    @Test
    public void createTargetPropertiesOptionNotOverwriteExisting () {
        tu1.setSource(tc1);
        tu1.getSource().setProperty(new Property("test", "value"));
        TextContainer tc2 = new TextContainer("unique fr text");
        tu1.setTarget(locFR, tc2);
        tu1.createTarget(locFR, false, IResource.COPY_PROPERTIES);
        assertTrue(tu1.hasTarget(locFR));
        assertEquals("unique fr text", tu1.getTarget(locFR).toString());
        assertTrue(tu1.getTarget(locFR).getProperty("test") == null);
	}
    
	@Test
	public void getSetId () {
		assertEquals(TU1, tu1.getId());
		tu1.setId("id2");
		assertEquals("id2", tu1.getId());
	}
	
	@Test
	public void getSetMimeType () {
		assertNull(tu1.getMimeType());
		tu1.setMimeType("test");
		assertEquals("test", tu1.getMimeType());
	}
	
	@Test
	public void propertiesInitialization() {
		assertEquals("Should be empty", 0, tu1.getPropertyNames().size());
    }

    @Test
    public void getPropertyReturnsDoesntExist() {
		assertNull("returns null when no property exists", tu1.getProperty("NAME"));
    }

    @Test
    public void getSetProperty() {
		Property p1 = new Property("name", "value", true);
		tu1.setProperty(p1);
		assertSame("should return the same property", p1, tu1.getProperty("name"));
	}

	@Test
	public void sourcePropertiesInitialization () {
        assertEquals("Should be empty", 0, tu1.getSourcePropertyNames().size());
    }

    @Test
    public void getSourcePropertyDoesntExist() {
		assertNull("returns null when no property exists", tu1.getSourceProperty("NAME"));
    }

    @Test
    public void getSetSourcePropertyFound() {
		Property p1 = new Property("name", "value", true);
		tu1.setSourceProperty(p1);
		assertSame("Should be the same object", p1, tu1.getSourceProperty("name"));
    }

	@Test
	public void targetPropertiesInitialization() {
		assertEquals(0, tu1.getTargetPropertyNames(locFR).size());
    }

    @Test
    public void getTargetPropertyNotFound() {
		tu1.setTarget(locFR, tc1);
        assertNull("Target shoudln't be found", tu1.getTargetProperty(locFR, "NAME"));
    }

    @Test
    public void getSetTargetProperty() {
        tu1.setTarget(locFR, tc1);
		Property p1 = new Property("name", "value", true);
		tu1.setTargetProperty(locFR, p1);
        assertSame("Properties should be the same", p1, tu1.getTargetProperty(locFR, "name"));
	}

//	@Test
//	public void testGetSetSourceContent () {
//		TextFragment tf1 = new TextFragment("source text");
//		tu1.setSourceContent(tf1);
//		TextFragment tf2 = ((TextContainer)tu1.getSourceContent()).getContent();
//		//TODO: the tc is actually not the same!, because it uses insert()
//		// Do we need to 'fix' this? Probably.
//		//assertSame(tf1, tf2);
//        assertEquals("source content", tf1, tf2);
//    }
//
//	@Test
//	public void testGetSetTargetContent () {
//		TextFragment tf1 = new TextFragment("fr text");
//		tu1.setTargetContent(locFR, tf1);
//		TextFragment tf2 = tu1.getTargetContent(locFR);
//		//TODO: the tc is actually not the same!, because it uses insert()
//		// Do we need to 'fix' this? Probably.
//		//assertSame(tf1, tf2);
//        assertEquals("target content", tf1, tf2);
//	}

}
