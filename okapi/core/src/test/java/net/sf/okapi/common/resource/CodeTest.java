/*===========================================================================
  Copyright (C) 2008-2013 by the Okapi Framework contributors
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

package net.sf.okapi.common.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.annotation.GenericAnnotation;
import net.sf.okapi.common.annotation.GenericAnnotationType;
import net.sf.okapi.common.annotation.GenericAnnotations;
import net.sf.okapi.common.resource.TextFragment.TagType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class CodeTest {

    @Before
    public void setUp(){
    }

    @Test
    public void testAccess () {
    	Code code = new Code(TagType.OPENING, "ctype", "data");
    	assertEquals("data", code.getData());
    	assertEquals("ctype", code.getType());
    	assertEquals(TagType.OPENING, code.getTagType());
    	assertEquals("data", code.getOuterData()); // default
    	code.setOuterData("outerData");
    	assertEquals("outerData", code.getOuterData());
    	assertEquals("data", code.getData());
    }

    @Test
    public void testSimpleAnnotations () {
    	Code code = new Code(TagType.OPENING, "ctype", "data");
    	code.setAnnotation("displayText", new InlineAnnotation("[display]"));
    	assertEquals("[display]", code.getAnnotation("displayText").getData());
    	GenericAnnotation.addAnnotation(code, new GenericAnnotation("disp", "disp_value", "[display]"));
    	assertEquals("[display]", code.getGenericAnnotationString("disp", "disp_value"));
    }

    @Test
    public void testFlags () {
    	Code code = new Code(TagType.OPENING, "ctype", "data");
    	assertFalse(code.isCloneable());
    	assertFalse(code.isDeleteable());
    	assertFalse(code.hasReference());
    	code.setDeleteable(true);
    	code.setCloneable(true);
    	code.setReferenceFlag(true);
    	assertTrue(code.isCloneable());
    	assertTrue(code.isDeleteable());
    	assertTrue(code.hasReference());
    }
    
    @Test
    public void testClone () {
    	Code code = new Code(TagType.OPENING, "ctype", "data");
    	code.setOuterData("out1");
    	Code c2 = code.clone();
    	assertNotSame(code, c2);
    	assertEquals(code.getId(), c2.getId());
    	assertEquals(code.getData(), c2.getData());
    	assertNotSame(code.data, c2.data);
    	assertEquals(code.getTagType(), c2.getTagType());
    	assertEquals(code.getType(), c2.getType());
    	assertEquals(code.getOuterData(), c2.getOuterData());
    	assertNotSame(code.outerData, c2.outerData);
    }

    @Test
    public void testStrings () {
    	ArrayList<Code> codes = new ArrayList<Code>();
    	codes.add(new Code(TagType.OPENING, "bold", "<b>"));
    	codes.add(new Code(TagType.PLACEHOLDER, "break", "<br/>"));
    	codes.add(new Code(TagType.CLOSING, "bold", "</b>"));
    	String tmp = Code.codesToString(codes);
    	
    	assertNotNull(tmp);
    	List<Code> codesAfter = Code.stringToCodes(tmp);
    	assertEquals(3, codesAfter.size());
    	
    	Code code = codesAfter.get(0);
    	assertEquals("<b>", code.getData());
    	assertEquals(TagType.OPENING, code.getTagType());
    	assertEquals("bold", code.getType());
    	
    	code = codesAfter.get(1);
    	assertEquals("<br/>", code.getData());
    	assertEquals(TagType.PLACEHOLDER, code.getTagType());
    	assertEquals("break", code.getType());
    	
    	code = codesAfter.get(2);
    	assertEquals("</b>", code.getData());
    	assertEquals(TagType.CLOSING, code.getTagType());
    	assertEquals("bold", code.getType());
    }
    
    @Test
    public void testCodeGenericAnnotations () {
    	GenericAnnotations anns1 = new GenericAnnotations();
		GenericAnnotation ann11 = anns1.add("type1");
		ann11.setString("name1", "v1");
		GenericAnnotation ann12 = anns1.add("type1");
		ann12.setString("name2", "v2-not-over");
		Code code = new Code(TagType.PLACEHOLDER, "z");
		GenericAnnotations.addAnnotations(code, anns1);

		GenericAnnotations res = (GenericAnnotations)code.getAnnotation(GenericAnnotationType.GENERIC);
		assertNotNull(res);
		assertEquals(anns1, res);
		
		GenericAnnotations anns2 = new GenericAnnotations();
		GenericAnnotation ann21 = anns2.add("type1");
		ann21.setString("name3", "v3");
		GenericAnnotation ann22 = anns2.add("type1");
		ann22.setString("name2", "another name2");
		
		GenericAnnotations.addAnnotations(code, anns2);
		res = (GenericAnnotations)code.getAnnotation(GenericAnnotationType.GENERIC);
		assertNotNull(res);
		assertEquals(anns1, res);
		List<GenericAnnotation> list = res.getAnnotations("type1");
		assertEquals(4, list.size());
		GenericAnnotation ann = list.get(1);
		assertEquals("v2-not-over", ann.getString("name2"));		
    	
    	ArrayList<Code> codes = new ArrayList<Code>();
    	codes.add(code);
    	assertTrue(code.getGenericAnnotations() instanceof GenericAnnotations);
    	
    	String tmp = Code.codesToString(codes);
    	
    	assertNotNull(tmp);
    	List<Code> codesAfter = Code.stringToCodes(tmp);
    	assertEquals(1, codesAfter.size());
    	
    	code = codesAfter.get(0);
    	assertTrue(code.getGenericAnnotations() instanceof GenericAnnotations);
    }

    @Test
    public void testCodeData () {
    	Code code = new Code(TagType.PLACEHOLDER, "type", null);
    	assertEquals("", code.toString());
    	
    	code = new Code(TagType.PLACEHOLDER, "type", null);
    	code.setOuterData("<x id=\"1\">");
    	assertEquals("", code.toString());
    	code.setOuterData(null);
    	assertEquals("", code.toString());
    }

}
