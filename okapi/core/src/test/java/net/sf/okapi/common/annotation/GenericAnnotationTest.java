/*===========================================================================
  Copyright (C) 2012 by the Okapi Framework contributors
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.common.resource.TextUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class GenericAnnotationTest {

	@Test
	public void testString () {
		GenericAnnotation ann = new GenericAnnotation("type1");
		assertEquals("type1", ann.getType());
		ann.setString("f1", "v1");
		assertEquals("v1", ann.getString("f1"));
		ann.setString("f1", "v2");
		assertEquals("v2", ann.getString("f1"));
	}

	@Test
	public void testBoolean () {
		GenericAnnotation ann = new GenericAnnotation("type1");
		assertEquals("type1", ann.getType());
		ann.setBoolean("f1", true);
		assertTrue(ann.getBoolean("f1"));
		ann.setBoolean("f1", false);
		assertFalse(ann.getBoolean("f1"));
	}

	@Test
	public void testDouble () {
		GenericAnnotation ann = new GenericAnnotation("type1");
		assertEquals("type1", ann.getType());
		ann.setDouble("f1", 1.234);
		assertEquals(1.234, ann.getDouble("f1"), 0.0);
	}

	@Test
	public void testInteger () {
		GenericAnnotation ann = new GenericAnnotation("type1");
		assertEquals("type1", ann.getType());
		ann.setInteger("f1", 123);
		assertEquals(123, (int)ann.getInteger("f1"));
	}

	@Test
	public void testStorage () {
		GenericAnnotation ann1 = new GenericAnnotation("type1");
		ann1.setBoolean("fb1", true);
		ann1.setString("fs1", "string1");
		ann1.setBoolean("fb2", false);
		ann1.setString("fs2", "");
		ann1.setString("fs3", " \t ");
		ann1.setDouble("ff1", 1.234);
		ann1.setInteger("fi1", 123);
		String buf = ann1.toString();
		
		GenericAnnotation ann2 = new GenericAnnotation("tmp");
		ann2.fromString(buf);
		assertEquals("type1", ann2.getType());
		assertTrue(ann2.getBoolean("fb1"));
		assertFalse(ann2.getBoolean("fb2"));
		assertEquals("string1", ann2.getString("fs1"));
		assertEquals("", ann2.getString("fs2"));
		assertEquals(" \t ", ann2.getString("fs3"));
		assertEquals(1.234, ann2.getDouble("ff1"), 0.0);
		assertEquals(123, (int)ann2.getInteger("fi1"));
	}

	@Test
	public void testClone () {
		GenericAnnotation ann1 = new GenericAnnotation("type1");
		ann1.setString("f1", "v1");
		ann1.setBoolean("f2", true);
		ann1.setDouble("ff1", 1.234);
		ann1.setInteger("fi1", 543);
		
		GenericAnnotation ann2 = ann1.clone();
		assertEquals(ann1.getType(), ann2.getType());
		assertFalse(ann1.getType()==ann2.getType());
		assertEquals(ann1.getString("f1"), ann2.getString("f1"));
		assertFalse(ann1.getString("f1")==ann2.getString("f1"));
		assertEquals(ann1.getBoolean("f2"), ann2.getBoolean("f2"));
		assertEquals(ann1.getDouble("ff1"), ann2.getDouble("ff1"), 0.0);
		assertEquals(ann1.getInteger("fi1"), ann2.getInteger("fi1"));
	}

	@Test
	public void testSetFields () {
		GenericAnnotation ann1 = new GenericAnnotation("type1", 
			"fs1", "v1",
			"fb2", true,
			"ff3", 1.234,
			"fi4", 543);
		
		assertEquals("type1", ann1.getType());
		assertEquals("v1", ann1.getString("fs1"));
		assertTrue(ann1.getBoolean("fb2"));
		assertEquals(1.234, ann1.getDouble("ff3"), 0.0);
		assertEquals(543, (int)ann1.getInteger("fi4"));
	}
	
	@Test
	public void testAddAnnotationOnTU () {
		ITextUnit tu = new TextUnit("id");
		GenericAnnotation.addAnnotation(tu, new GenericAnnotation("type1", "name1", "value1"));
		GenericAnnotations anns = tu.getAnnotation(GenericAnnotations.class);
		GenericAnnotation res = anns.getFirstAnnotation("type1");
		assertEquals("value1", res.getString("name1"));
	}

	@Test
	public void testAddAnnotationOnTC () {
		TextContainer tc = new TextContainer();
		GenericAnnotation.addAnnotation(tc, new GenericAnnotation("type1", "name1", "value1"));
		GenericAnnotations anns = tc.getAnnotation(GenericAnnotations.class);
		GenericAnnotation res = anns.getFirstAnnotation("type1");
		assertEquals("value1", res.getString("name1"));
	}

	@Test
	public void testAddAnnotationOnCode () {
		Code code = new Code(TagType.PLACEHOLDER, "z");
		GenericAnnotation.addAnnotation(code, new GenericAnnotation("type1", "name1", "value1"));
		GenericAnnotations anns = (GenericAnnotations)code.getAnnotation(GenericAnnotationType.GENERIC);
		GenericAnnotation res = anns.getFirstAnnotation("type1");
		assertEquals("value1", res.getString("name1"));
	}
}
