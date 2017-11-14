/*===========================================================================
  Copyright (C) 2008-2010 by the Okapi Framework contributors
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import net.sf.okapi.common.IResource;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filterwriter.GenericContent;
import net.sf.okapi.common.skeleton.GenericSkeleton;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;


@RunWith(JUnit4.class)
public class TextUnitTest {


    private static final LocaleId locFR = LocaleId.FRENCH;
    private static final LocaleId locES = LocaleId.SPANISH;
    // private static final LocaleId locDE = LocaleId.GERMAN;
    private static final String TU1 = "tu1";
    private TextContainer tc1;
    private ITextUnit tu1;


    @Before
    public void setUp(){
        tu1 = new TextUnit(TU1);
        tc1 = new TextContainer("fr text");
    }

    ////TODO verify that these tests adequately cover the ITextUnit interface


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
    public void getSetSource () {
        tu1.setSource(tc1);
        assertSame(tu1.getSource(), tc1);
    }

    @Test
    public void textUnitWithNoIdIsEmpty() {
        assertTrue(new TextUnit().isEmpty());
    }

    @Test
    public void createTargetReturnsNewEmptyOnNoMatch(){
        assertNotNull("When there is no match a empty should be returned", tu1.createTarget(locFR, false, IResource.COPY_SEGMENTATION));
        assertEquals("", tu1.getTarget(locFR).toString());
    }

    @Test
    public void getSetTarget () {
        tu1.setTarget(locFR, tc1);
        assertSame("The target should be TextContainer we just set", tc1, tu1.getTarget(locFR));
    }


    @Test
    public void removeTarget() {
        tu1.setTarget(locFR, tc1);
        tu1.removeTarget(locFR);
        assertFalse("TextUnit should no longer have a target", tu1.hasTarget(locFR));
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
    public void createTargetCase1 () {
    	tu1 = createSegmentedTU();
        tu1.createTarget(locFR, false, IResource.COPY_ALL);
        assertEquals(tu1.getSource().toString(), tu1.getTarget(locFR).toString());
        assertEquals(tu1.getSource().getSegments().count(), tu1.getTarget(locFR).getSegments().count());
    }

    @Test
    public void createTargetCase2 () {
    	tu1 = createSegmentedTU();
        tu1.createTarget(locFR, false, IResource.COPY_SEGMENTATION);
        assertEquals(tu1.getSource().getSegments().count(), tu1.getTarget(locFR).getSegments().count());
        assertEquals("[] a []", fmt.printSegmentedContent(tu1.getTarget(locFR), true));
        assertEquals(" a ", tu1.getTarget(locFR).toString());
    }

    @Test
    public void createTargetCase3 () {
    	tu1 = createSegmentedTU();
        tu1.createTarget(locFR, false, IResource.COPY_CONTENT);
        assertEquals(1, tu1.getTarget(locFR).getSegments().count());
        assertEquals("Part 1. a Part 2.", tu1.getTarget(locFR).toString());
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

    //TODO test setContent(TextFragment)
    //TODO test setTargetContent(LocaleId, TextFragment)
    //TODO test getAlignedSegments()

    @Test
    public void getSegments() {
        ITextUnit tu = createSegmentedTUAndTarget();
        IAlignedSegments as = tu.getAlignedSegments();
        assertNotNull("getSegments() should return a non-null IAlignedSegments instance",
                      as);

        
    }

    @Test
    public void loopThroughSegments () {
    	tu1 = createSegmentedTUAndTarget();
    	Segment trgSeg;
    	for ( Segment srcSeg : tu1.getSourceSegments() ) {
    		if ( srcSeg.id.equals("0") ) {
    			assertEquals("Part 1.", srcSeg.text.toString());
    			trgSeg = tu1.getTargetSegment(locFR, srcSeg.id, false);
    			assertEquals("Trg 1.", trgSeg.text.toString());
    		}
    		else {
    			assertEquals("Part 2.", srcSeg.text.toString());
    			trgSeg = tu1.getTargetSegment(locFR, srcSeg.id, false);
    			assertEquals("Trg 2.", trgSeg.text.toString());
    		}
    	}
    }

    @Test
    public void loopThroughSegmentsWithoutTargets () {
    	ITextUnit tu = createSegmentedTU();
    	tu.createTarget(locES, true, IResource.CREATE_EMPTY); // Not a copy of the source
    	Segment trgSeg;
    	for ( Segment srcSeg : tu.getSourceSegments() ) {
    		if ( srcSeg.id.equals("0") ) {
    			assertEquals("Part 1.", srcSeg.text.toString());
    			trgSeg = tu.getTargetSegment(locFR, srcSeg.id, true); // FR
    			assertEquals("", trgSeg.text.toString());
    		}
    		else {
    			assertEquals("Part 2.", srcSeg.text.toString());
    			trgSeg = tu.getTargetSegment(locES, srcSeg.id, false); // ES
    			assertNull(trgSeg);
    		}
    	}
        assertEquals("[Part 1.] a [Part 2.]", fmt.printSegmentedContent(tu.getSource(), true));
        assertTrue(tu.hasTarget(locFR));
        assertEquals("[] a []", fmt.printSegmentedContent(tu.getTarget(locFR), true));
        assertTrue(tu.hasTarget(locES));
        assertEquals("[]", fmt.printSegmentedContent(tu.getTarget(locES), true));
    }

    @Test
    public void getSourceSegments () {
    	ITextUnit tu = createSegmentedTU();
    	ISegments segs = tu.getSourceSegments();
    	assertNotNull(segs);
    	assertEquals(2, segs.count());
    }


    @Test
    public void getExistingTargetSegments () {
    	ITextUnit tu = createSegmentedTUAndTarget();
    	ISegments segs = tu.getTargetSegments(locFR);
    	assertNotNull(segs);
    	assertEquals(2, segs.count());
    	assertEquals("Trg 1.", segs.get(0).toString());
    }

    @Test
    public void getNonExistingTargetSegments () {
    	ITextUnit tu = createSegmentedTU();
    	ISegments segs = tu.getTargetSegments(locES);
    	assertNotNull(segs);
    	assertEquals(2, segs.count());
    	assertEquals("", segs.get(0).toString());
    }


    //TODO test getSegment(String, boolean)




    private GenericContent fmt;

    public TextUnitTest () {
    	fmt = new GenericContent();
    }


    @Test
    public void toStringFromSource(){
        tu1.setSource(tc1);
        assertEquals("TextUnit.toString()",  "fr text", tu1.toString());
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

    @Test
    public void testTextUnitClone() {
    	TextUnit tu1 = new TextUnit("tu1");
    	GenericSkeleton skel = new GenericSkeleton();
    	skel.add("partBefore");
    	skel.addContentPlaceholder(tu1);
    	skel.add("partAfter");
    	assertEquals(3, skel.getParts().size());
    	assertEquals(tu1, skel.getParts().get(1).getParent());
    	tu1.setSkeleton(skel);
    	
    	TextUnit tu2 = tu1.clone();
    	assertTrue(tu2.getSkeleton() instanceof GenericSkeleton);
    	GenericSkeleton newSkel = (GenericSkeleton) tu2.getSkeleton();    	
    	assertEquals(3, newSkel.getParts().size());
    	assertEquals(tu2, newSkel.getParts().get(1).getParent());
    }
    
    @Test
    public void testExample () {
    	// Create a text unit
    	TextUnit tu = new TextUnit("id");
    	// Get the source
    	TextContainer srcTc = tu.getSource();
    	// the source always exists
    	assertNotNull(srcTc);
    	// But is initially empty
    	assertEquals("", srcTc.toString());
    	// And we always have at least one segment
    	assertEquals(1, srcTc.getSegments().count());
    	
    	// One can add segments
    	srcTc.append(new Segment("seg1", new TextFragment("Text of segment 1.")));
    	// (if previous is empty, it is collapsed by default) 
    	assertEquals(1, srcTc.getSegments().count());
    	// One can have inter-segment parts, like this space
    	srcTc.append(new TextPart(" "));
    	// and add another segment
    	srcTc.append(new Segment("seg2", new TextFragment("Text of segment 2.")));
    	// Noww we have two segments
    	assertEquals(2, srcTc.getSegments().count());
    	// and 3 parts (segments and non-segments)
    	assertEquals(3, srcTc.count());
    	// And here is the complete text
    	assertEquals("Text of segment 1. Text of segment 2.", srcTc.toString());
    	// We can look at it by parts too: (segments are between brackets)
    	GenericContent fmt = new GenericContent();
    	assertEquals("[Text of segment 1.] [Text of segment 2.]", fmt.printSegmentedContent(srcTc, true));
    	
    	// By default there are no targets
    	assertTrue(tu.getTargetLocales().isEmpty());
    	// One can add one with its own content
    	tu.setTarget(LocaleId.JAPANESE, new TextContainer("One segment"));
    	// Or one by cloning the source
    	tu.createTarget(LocaleId.FRENCH, true, IResource.COPY_ALL);

    	// We access the target by there locale-id
    	TextContainer jaTc = tu.getTarget(LocaleId.JAPANESE);
    	assertEquals("[One segment]", fmt.printSegmentedContent(jaTc, true));
    	TextContainer frTc = tu.getTarget(LocaleId.FRENCH);
    	assertEquals("[Text of segment 1.] [Text of segment 2.]", fmt.printSegmentedContent(frTc, true));
    	// Note that the source container was cloned: the target object is not the same as the source 
    	assertFalse(frTc==srcTc);
    	
    	// We can remove the targets too
    	tu.removeTarget(LocaleId.JAPANESE);
    	assertEquals(1, tu.getTargetLocales().size());
    	
    	// One can access the segmentsof both the source and target
    	ISegments srcSegs = tu.getSourceSegments();
    	for ( Segment srcSeg : srcSegs ) {
    		Segment trgSeg = tu.getTargetSegment(LocaleId.FRENCH, srcSeg.getId(), false);
    		// In this example, they have the same content
    		assertEquals(srcSeg.toString(), trgSeg.toString());
    		// But, obviously, they are not the same object
    		assertFalse(srcSeg==trgSeg);
    		// We can access and modify the segments' content as needed
    		String text = trgSeg.getContent().getCodedText();
    		text = text.toUpperCase();
    		trgSeg.getContent().setCodedText(text);
    	}
    	// The source
    	assertEquals("[Text of segment 1.] [Text of segment 2.]", fmt.printSegmentedContent(srcTc, true));
    	// The modified target
    	assertEquals("[TEXT OF SEGMENT 1.] [TEXT OF SEGMENT 2.]", fmt.printSegmentedContent(frTc, true));
    }

    //utility methods

    private ITextUnit createSegmentedTU () {
    	ITextUnit tu = new TextUnit("id", "Part 1.");
    	tu.getSource().getSegments().append(new Segment("s2", new TextFragment("Part 2.")), " a ");
    	return tu;
    }

    private ITextUnit createSegmentedTUAndTarget () {
    	ITextUnit tu = createSegmentedTU();
    	// Add the target segments
    	ISegments segs = tu.getTargetSegments(locFR);
    	segs.get(0).text.append("Trg 1.");
    	segs.get(1).text.append("Trg 2.");
    	segs = tu.getTargetSegments(locES);
    	segs.get(0).text.append("Objetivo 1.");
    	segs.get(1).text.append("Objetivo 2.");
    	return tu;
    }

}
