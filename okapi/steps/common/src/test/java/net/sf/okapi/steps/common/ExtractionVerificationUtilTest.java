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
===========================================================================*/

package net.sf.okapi.steps.common;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import net.sf.okapi.common.IResource;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.BaseNameable;
import net.sf.okapi.common.resource.BaseReferenceable;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.DocumentPart;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.InlineAnnotation;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.common.resource.TextPart;
import net.sf.okapi.common.resource.TextUnit;
import net.sf.okapi.common.skeleton.GenericSkeleton;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ExtractionVerificationUtilTest {

	ExtractionVerificationUtil util;
	
	@Before
	public void setUp() throws Exception {

		util = new ExtractionVerificationUtil();
	}
	
	
	@Test
	public void testBaseReferenceable() {
		DocumentPart dp1 = null;
		DocumentPart dp2 = null;
		
		//--test both null--
		assertTrue(util.compareBaseReferenceable(dp1, dp2));
		
		dp1 = new DocumentPart("dp1", false);

		//--test one null--
		assertFalse(util.compareBaseReferenceable(dp1, dp2));
		
		dp2 = new DocumentPart("dp1", false);

		assertTrue(util.compareBaseReferenceable(dp1, dp2));

		dp1.setParentId("p1");
		assertFalse(util.compareBaseReferenceable(dp1, dp2));
		
		dp2.setParentId("p2");
		assertFalse(util.compareBaseReferenceable(dp1, dp2));
		
		dp2.setParentId("p1");
		assertTrue(util.compareBaseReferenceable(dp1, dp2));
		
	}
	
	@Test
	public void testTextUnits() {

		ITextUnit tu1 = null;
		ITextUnit tu2 = null;
		
		//--test both null--
		assertTrue(util.compareTextUnits(tu1, tu2));
		
		tu1 = new TextUnit("tu1");

		//--test one null--
		assertFalse(util.compareTextUnits(tu1, tu2));
		
		tu2 = new TextUnit("tu1");

		assertTrue(util.compareTextUnits(tu1, tu2));

		tu1.setName("tu name 1");
		
		assertFalse(util.compareTextUnits(tu1, tu2));
		
		tu2.setName("tu name 2");
		
		assertFalse(util.compareTextUnits(tu1, tu2));
		
		tu2.setName("tu name 1");

		assertTrue(util.compareTextUnits(tu1, tu2));
		
		tu2.setId("tu 1b");
		assertFalse(util.compareTextUnits(tu1, tu2));
	
		tu1 = new TextUnit("tu1", "src 1");
		tu2 = new TextUnit("tu1");
		assertFalse(util.compareTextUnits(tu1, tu2));
		
		tu2 = new TextUnit("tu1", "src 2");
		assertFalse(util.compareTextUnits(tu1, tu2));

		//--TESTING TARGETS--
		tu1 = new TextUnit("tu", "src");
		tu2 = new TextUnit("tu", "src");

		tu1.createTarget(LocaleId.FRENCH, true, IResource.COPY_ALL);
		assertFalse(util.compareTextUnits(tu1, tu2));
		
		tu2.createTarget(LocaleId.FRENCH, true, IResource.COPY_ALL);
		assertTrue(util.compareTextUnits(tu1, tu2));
		
	}

	
	@Test
	public void testTextContainers() {

		TextContainer tc1 = null;
		TextContainer tc2 = null;
		
		//--test both null--
		assertTrue(util.compareTextContainers(tc1, tc2));
		
		tc1 = new TextContainer();

		//--test one null--
		assertFalse(util.compareTextContainers(tc1, tc2));
		
		tc2 = new TextContainer();

		//--test one null--
		assertTrue(util.compareTextContainers(tc1, tc2));
		
		tc1.setHasBeenSegmentedFlag(true);
		tc2.setHasBeenSegmentedFlag(false);
		
		//--test hasBeenSegmented--
		assertFalse(util.compareTextContainers(tc1, tc2));

		tc2.setHasBeenSegmentedFlag(true);
		tc1.setProperty(new Property("name","value1"));
		
		//--test property names--
		assertFalse(util.compareTextContainers(tc1, tc2));
		
		tc2.setProperty(new Property("name","value2"));
		
		//--test property value--
		assertFalse(util.compareTextContainers(tc1, tc2));
		
		tc2.setProperty(new Property("name","value1"));
		
		//--test textPart count--
		assertTrue(util.compareTextContainers(tc1, tc2));

		tc1.append(new Segment("seg1", new TextFragment("Hello")));
		tc1.append(new Segment("seg2", new TextFragment(" world")));
		
		assertFalse(util.compareTextContainers(tc1, tc2));

		tc2.append(new Segment("seg1", new TextFragment("Hello 2")));
		tc2.append(new Segment("seg2", new TextFragment(" world 2")));

		assertFalse(util.compareTextContainers(tc1, tc2));
	}
	
	@Test
	public void testCompareSegments() {

		Segment seg1 = null;
		Segment seg2 = null;
		
		//--test both null--
		assertTrue(util.compareSegments(seg1, seg2));
		
		seg1 = new Segment();

		//--test one null--
		assertFalse(util.compareSegments(seg1, seg2));
		
		seg2 = new Segment();

		//--test one null--
		assertTrue(util.compareSegments(seg1, seg2));
		
		seg1 = new Segment("id1");
		
		//--test one id null--
		assertFalse(util.compareSegments(seg1, seg2));
		
		seg2 = new Segment("id2");
		
		//--test different seg id--
		assertFalse(util.compareSegments(seg1, seg2));
		
		seg2 = new Segment("id1");

		assertTrue(util.compareSegments(seg1, seg2));
		
		seg1 = new Segment("id1", new TextFragment("Text 1"));
		
		//--test different seg id--
		assertFalse(util.compareSegments(seg1, seg2));
		
		seg2 = new Segment("id1", new TextFragment("Text 2"));

		assertFalse(util.compareSegments(seg1, seg2));

		
	}
	
	@Test
	public void testCompareTextParts() {

		TextPart tp1 = null;
		TextPart tp2 = null;
		
		//--test both null--
		assertTrue(util.compareTextParts(tp1, tp2));
		
		tp1 = new TextPart();

		//--test one null--
		assertFalse(util.compareTextParts(tp1, tp2));
		
		tp2 = new TextPart();

		//--test one null--
		assertTrue(util.compareTextParts(tp1, tp2));
	}

	
	@Test
	public void testCompareTextFragments() {

		TextFragment tf1 = null;
		TextFragment tf2 = null;
		
		//--test both null--
		assertTrue(util.compareTextFragments(tf1, tf2));
		
		tf1 = new TextFragment();

		//--test one null--
		assertFalse(util.compareTextFragments(tf1, tf2));

		tf2 = new TextFragment();
		
		//--test both ids  not null--
		assertTrue(util.compareTextFragments(tf1, tf2));
		
		tf1.append("text");
		
		//--test text difference--
		assertFalse(util.compareTextFragments(tf1, tf2));
		
		tf2.append("text");
		
		//--test text same--
		assertTrue(util.compareTextFragments(tf1, tf2));
		
		Code code1 = new Code(TagType.PLACEHOLDER,"br","data");
		Code code2 = new Code(TagType.PLACEHOLDER,"br","data");
		
		tf1.append(code1);
		
		//--test code num difference--
		assertFalse(util.compareTextFragments(tf1, tf2));
		
		tf2.append(code2);
		
		//--test code num difference--
		assertTrue(util.compareTextFragments(tf1, tf2));
		
		code1.setId(2);
		
		assertFalse(util.compareTextFragments(tf1, tf2));

		code2.setId(2);
		
		assertTrue(util.compareTextFragments(tf1, tf2));
		
		code1.setData("data2");
		
		assertFalse(util.compareTextFragments(tf1, tf2));
		
		code2.setData("data2");

		assertTrue(util.compareTextFragments(tf1, tf2));
		
		code1.setOuterData("outdata2");

		assertFalse(util.compareTextFragments(tf1, tf2));
		
		code2.setOuterData("outdata2");

		assertTrue(util.compareTextFragments(tf1, tf2));
		
		code1.setType("it");
		assertFalse(util.compareTextFragments(tf1, tf2));
		
		code2.setType("it");
		assertTrue(util.compareTextFragments(tf1, tf2));
		
		code1.setTagType(TagType.OPENING);
		assertFalse(util.compareTextFragments(tf1, tf2));
		
		code2.setTagType(TagType.OPENING);
		assertTrue(util.compareTextFragments(tf1, tf2));
		
		code1.setReferenceFlag(true);
		code2.setReferenceFlag(false);
		assertFalse(util.compareTextFragments(tf1, tf2));
		
		code2.setReferenceFlag(true);
		assertTrue(util.compareTextFragments(tf1, tf2));
		
		code1.setCloneable(true);
		code2.setCloneable(false);
		assertFalse(util.compareTextFragments(tf1, tf2));
		
		code2.setCloneable(true);
		assertTrue(util.compareTextFragments(tf1, tf2));
		
		code1.setDeleteable(true);
		code2.setDeleteable(false);
		assertFalse(util.compareTextFragments(tf1, tf2));
		
		code2.setDeleteable(true);
		assertTrue(util.compareTextFragments(tf1, tf2));
		
		code1.setAnnotation("annotation", new InlineAnnotation("inlineAnnotation"));
		assertFalse(util.compareTextFragments(tf1, tf2));
		
		code2.setAnnotation("annotation", new InlineAnnotation("inlineAnnotation two"));
		assertFalse(util.compareTextFragments(tf1, tf2));
	}
	
	@Test
	public void testCompareINameables() {

		BaseNameable bn1 = null;
		BaseNameable bn2 = null;
		
		//--test both null--
		assertTrue(util.compareINameables(bn1, bn2));
		
		bn1 = new BaseNameable();

		//--test one null--
		assertFalse(util.compareINameables(bn1, bn2));

		bn2 = new BaseNameable();

		//--test both names null--
		assertTrue(util.compareINameables(bn1, bn2));
		
		bn1.setName("name");

		//--test one name null--
		assertFalse(util.compareINameables(bn1, bn2));
		
		bn2.setName("name");

		//--test both types null--
		assertTrue(util.compareINameables(bn1, bn2));
		
		bn2.setType("type");

		//--test one type null--
		assertFalse(util.compareINameables(bn1, bn2));
		
		bn1.setType("type");

		//--test both type null--
		assertTrue(util.compareINameables(bn1, bn2));
		
		bn1.setMimeType("mimetype");
		
		//--test one mimetype null --
		assertFalse(util.compareINameables(bn1, bn2));
		
		bn2.setMimeType("mimetype");
		
		//--test one mimetype null --
		assertTrue(util.compareINameables(bn1, bn2));
		
		bn2.setIsTranslatable(false);
		
		//--test one not translatable --
		assertFalse(util.compareINameables(bn1, bn2));
		
		bn1.setIsTranslatable(false);
		
		//--test both not translatable --
		assertTrue(util.compareINameables(bn1, bn2));
		
		bn2.setPreserveWhitespaces(true);
		
		//--test both not translatable --
		assertFalse(util.compareINameables(bn1, bn2));
		
		bn1.setPreserveWhitespaces(true);
		
		//--test both preserve --
		assertTrue(util.compareINameables(bn1, bn2));
		
		
		//RESOURCE LEVEL PROPS
		bn1.setProperty(new Property("name1","value1"));

		//--test prop count difference--
		assertFalse(util.compareINameables(bn1, bn2));
		
		bn2.setProperty(new Property("name1","value1"));

		//--test prop count difference--
		assertTrue(util.compareINameables(bn1, bn2));
		
		bn1.setProperty(new Property("name2","value2"));
		bn2.setProperty(new Property("name3","value3"));

		//--test prop name difference--
		assertFalse(util.compareINameables(bn1, bn2));
		
		bn2.setProperty(new Property("name2","value2"));
		bn1.setProperty(new Property("name3","value3"));
		
		bn2.setProperty(new Property("name4","value4a"));
		bn1.setProperty(new Property("name4","value4b"));

		//--test prop value difference--
		assertFalse(util.compareINameables(bn1, bn2));
		
		bn1.setProperty(new Property("name4","value4a"));
		
		//--test prop equal --
		assertTrue(util.compareINameables(bn1, bn2));
		
		
		//SOURCE LEVEL PROPS
		bn2.setSourceProperty(new Property("name1","value1"));

		//--test prop count difference--
		assertFalse(util.compareINameables(bn1, bn2));
		
		bn1.setSourceProperty(new Property("name1","value1"));

		//--test prop count difference--
		assertTrue(util.compareINameables(bn1, bn2));
		
		bn2.setSourceProperty(new Property("name2","value2"));
		bn1.setSourceProperty(new Property("name3","value3"));

		//--test prop name difference--
		assertFalse(util.compareINameables(bn1, bn2));
		
		bn1.setSourceProperty(new Property("name2","value2"));
		bn2.setSourceProperty(new Property("name3","value3"));
		
		bn1.setSourceProperty(new Property("name4","value4a"));
		bn2.setSourceProperty(new Property("name4","value4b"));

		//--test prop value difference--
		assertFalse(util.compareINameables(bn1, bn2));
		
		bn2.setSourceProperty(new Property("name4","value4a"));
		
		//--test prop equal --
		assertTrue(util.compareINameables(bn1, bn2));
		
		
		//TARGET LEVEL PROPS
		bn1.createTargetProperty(LocaleId.FRENCH, "name", true, 0);

		//--test prop count difference--
		assertFalse(util.compareINameables(bn1, bn2));

		bn2.createTargetProperty(LocaleId.FRENCH, "name", true, 0);

		//--test prop count same--
		assertTrue(util.compareINameables(bn1, bn2));

		bn1.createTargetProperty(LocaleId.GERMAN, "name", true, 0);

		//--test prop lang difference--
		assertFalse(util.compareINameables(bn1, bn2));

		bn2.createTargetProperty(LocaleId.GERMAN, "name", true, 0);

		//--test prop count same--
		assertTrue(util.compareINameables(bn1, bn2));

		
		bn1.setTargetProperty(LocaleId.FRENCH, new Property("name1","value1"));

		//--test prop name difference--
		assertFalse(util.compareINameables(bn1, bn2));
		
		bn2.setTargetProperty(LocaleId.FRENCH, new Property("name1","value1"));

		//--test prop name difference--
		assertTrue(util.compareINameables(bn1, bn2));
		
		bn2.setTargetProperty(LocaleId.FRENCH, new Property("name2","value2"));
		bn1.setTargetProperty(LocaleId.FRENCH, new Property("name3","value3"));

		//--test prop name difference--
		assertFalse(util.compareINameables(bn1, bn2));
		
		bn1.setTargetProperty(LocaleId.FRENCH, new Property("name2","value2"));
		bn2.setTargetProperty(LocaleId.FRENCH, new Property("name3","value3"));
		
		bn1.setTargetProperty(LocaleId.FRENCH, new Property("name4","value4a"));
		bn2.setTargetProperty(LocaleId.FRENCH, new Property("name4","value4b"));

		//--test prop value difference--
		assertFalse(util.compareINameables(bn1, bn2));
		
		bn2.setTargetProperty(LocaleId.FRENCH, new Property("name4","value4a"));
		
		//--test prop equal --
		assertTrue(util.compareINameables(bn1, bn2));
	}
	
	@Test
	public void testCompareIResources() {

		BaseNameable bn1 = null;
		BaseNameable bn2 = null;
		GenericSkeleton gs1 = new GenericSkeleton();
		GenericSkeleton gs2 = new GenericSkeleton();
		
		//--test both null--
		assertTrue(util.compareIResources(bn1, bn2));
		
		bn1 = new BaseNameable();

		//--test one null--
		assertFalse(util.compareIResources(bn1, bn2));

		bn2 = new BaseNameable();
		
		//--test both ids null--
		assertTrue(util.compareIResources(bn1, bn2));
		
		bn1.setId("bn");
		
		//--test one id null--
		assertFalse(util.compareIResources(bn1, bn2));
		
		bn2.setId("bn");
		
		//--test both skeleton null--
		assertTrue(util.compareIResources(bn1, bn2));
		
		bn1.setSkeleton(gs1);

		//--test one skeleton null--
		assertFalse(util.compareIResources(bn1, bn2));

		bn2.setSkeleton(gs2);
		
		//--test no skeleton null--
		assertTrue(util.compareIResources(bn1, bn2));
		
		gs1.add("gs");
		
		//--test skeleton toString null difference--
		assertFalse(util.compareIResources(bn1, bn2));
		
		gs2.add("gs different");
		
		//--test skeleton to string content difference--
		assertFalse(util.compareIResources(bn1, bn2));
		
		gs1.add(" different");
		
		//--test skeleton to string content difference--
		assertTrue(util.compareIResources(bn1, bn2));		
	}
	
	@Test
	public void testCompareIReferenceables() {

		BaseReferenceable br1 = null;
		BaseReferenceable br2 = null;
		
		//--test both null--
		assertTrue(util.compareIReferenceables(br1, br2));
		
		br1 = new BaseReferenceable();

		//--test one null--
		assertFalse(util.compareIReferenceables(br1, br2));

		br1.setIsReferent(true);
		
		br2 = new BaseReferenceable();
		br2.setIsReferent(false);

		//--test isReferent difference--
		assertFalse(util.compareIReferenceables(br1, br2));
		
		br2.setIsReferent(true);
		
		//--test isReferent difference-- 
		assertTrue(util.compareIReferenceables(br1, br2));

		br1.setReferenceCount(2);

		//--test referenceCount difference--		
		assertFalse(util.compareIReferenceables(br1, br2));
		
		br2.setReferenceCount(2);
		
		//--test referenceCount difference--
		assertTrue(util.compareIReferenceables(br1, br2));
	}
	
	@Test
	public void testCompareProperties() {

		Property p1 = null;
		Property p2 = null;
		
		//--test both null--
		assertTrue(util.compareProperties(p1, p2));
		
		p1 = new Property(null, null);

		//--test one null--
		assertFalse(util.compareProperties(p1, p2));

		p2 = new Property(null, null);
		
		//--test both name and value null--
		assertTrue(util.compareProperties(p1, p2));

		p2 = new Property("name", null);
		
		//--test both name and value null--
		assertFalse(util.compareProperties(p1, p2));
		
		p1 = new Property("name", null);
		
		//--test both same name and value null--
		assertTrue(util.compareProperties(p1, p2));
		
		p1 = new Property("name", "value");
		
		//--test both same name and value different--
		assertFalse(util.compareProperties(p1, p2));
		
		p2 = new Property("name", "value");
		
		//--test both same name and value different--
		assertTrue(util.compareProperties(p1, p2));
		
		p1 = new Property("name", "value", false);
		
		//--test both same name and value different isReferent --
		assertFalse(util.compareProperties(p1, p2));
		
		p2 = new Property("name", "value", false);
		
		//--test both same name and value different isReferent --
		assertTrue(util.compareProperties(p1, p2));
	}
	
	@Test
	public void testBothNull() {

		Object obj1 = null;
		Object obj2 = null;
		Object obj3 = new String();
		Object obj4 = new String();
		
		assertTrue(util.bothAreNull(obj1, obj2));
		assertFalse(util.bothAreNull(obj1, obj3));
		assertFalse(util.bothAreNull(obj3, obj4));
	}
	
	@Test
	public void testNullDifference () {

		Object obj1 = null;
		Object obj2 = null;
		Object obj3 = new String();
		Object obj4 = new String();
		
		assertFalse(util.oneIsNulll(obj1, obj2, "Function", "Type"));
		assertFalse(util.oneIsNulll(obj3, obj4, "Function", "Type"));
		assertTrue(util.oneIsNulll(obj2, obj3, "Function", "Type"));
	}
}
