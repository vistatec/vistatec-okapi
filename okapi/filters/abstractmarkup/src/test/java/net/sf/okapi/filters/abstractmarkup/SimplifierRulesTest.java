package net.sf.okapi.filters.abstractmarkup;

/*===========================================================================
Copyright (C) 2015 by the Okapi Framework contributors
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

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import net.sf.okapi.common.FileLocation;
import net.sf.okapi.common.filterwriter.GenericContent;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.CodeSimplifier;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.core.simplifierrules.SimplifierRules;
import net.sf.okapi.filters.abstractmarkup.config.TaggedFilterConfiguration;

@RunWith(JUnit4.class)
public class SimplifierRulesTest {
	GenericContent fmt;
	CodeSimplifier simplifier;
	TaggedFilterConfiguration yamlRules;

	@Before
	public void setUp() throws Exception {
		simplifier = new CodeSimplifier();
		fmt = new GenericContent();
		FileLocation location = FileLocation.fromClass(this.getClass()).in("/net/sf/okapi/filters/abstractmarkup/simplifierrules1.yml");
		yamlRules = new TaggedFilterConfiguration(location.asUrl());
		SimplifierRules.validate(yamlRules.getSimplifierRules());
		simplifier.setRules(yamlRules.getSimplifierRules());
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCodeReduction01 () {		
		TextFragment tf = new TextFragment();
		tf.append(TagType.OPENING, "a", "<a>");
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append("T1");
		tf.append(TagType.CLOSING, "b", "</b>");
		tf.append(TagType.CLOSING, "a", "</a>");

		assertEquals("<1><2>T1</2></1>", fmt.setContent(tf).toString());
		// 1 + 2 -> 1
		// /2 + /1 -> /1
		simplifier.simplifyAll(tf, false);		
		assertEquals("<1><2>T1</2></1>", fmt.setContent(tf).toString());
	}

	@Test
	public void testCodeReduction02 () {
		TextFragment tf = new TextFragment();
		tf.append(TagType.OPENING, "a", "<a>");
		tf.append(TagType.PLACEHOLDER, "x", "<x/>");
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append("T1");
		tf.append(TagType.CLOSING, "b", "</b>");
		tf.append(TagType.CLOSING, "a", "</a>");
		
		assertEquals("<1><2/><3>T1</3></1>", fmt.setContent(tf).toString());
		// 1 + 2/ + 3 -> 1
		// /3 + /1 -> /1
		simplifier.simplifyAll(tf, false);		
		assertEquals("<1><2/><3>T1</3></1>", fmt.setContent(tf).toString());
	}

	@Test
	public void testCodeReduction03 () {
		TextFragment tf = new TextFragment();
		tf.append(TagType.OPENING, "a", "<a>");
		tf.append(TagType.PLACEHOLDER, "x1", "<x1/>");
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append("T1");
		tf.append(TagType.CLOSING, "b", "</b>");
		tf.append(TagType.PLACEHOLDER, "x2", "<x2/>");
		tf.append(TagType.CLOSING, "a", "</a>");
		
		assertEquals("<1><2/><3>T1</3><4/></1>", fmt.setContent(tf).toString());
		// 1 + 2/ + 3 -> 1
		// /3 + 4/ + /1 -> /1 
		simplifier.simplifyAll(tf, false);
		assertEquals("<1><2/><3>T1</3><4/></1>", fmt.setContent(tf).toString());
	}

	@Test
	public void testCodeReduction04 () {
		TextFragment tf = new TextFragment();
		tf.append(TagType.OPENING, "a", "<a>");
		tf.append(TagType.PLACEHOLDER, "x1", "<x1/>");
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append("T1");
		tf.append(TagType.CLOSING, "b", "</b>");
		tf.append("T2");
		tf.append(TagType.CLOSING, "a", "</a>");

		assertEquals("<1><2/><3>T1</3>T2</1>", fmt.setContent(tf).toString());
		// 1 + 2/ -> 1
		// 3 -> 2
		// /3 -> /2
		// /1 -> /1
		simplifier.simplifyAll(tf, false);		
		assertEquals("<1><2/><3>T1</3>T2</1>", fmt.setContent(tf).toString());
	}

	@Test
	public void testCodeReduction05 () {
		TextFragment tf = new TextFragment();
		tf.append(TagType.OPENING, "a", "<a>");
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append(TagType.OPENING, "c", "<c>");
		tf.append("T1");
		tf.append(TagType.CLOSING, "c", "</c>");
		tf.append(TagType.CLOSING, "b", "</b>");
		tf.append("T2");
		tf.append(TagType.CLOSING, "a", "</a>");

		assertEquals("<1><2><3>T1</3></2>T2</1>", fmt.setContent(tf).toString());
		// 1 -> 1
		// 2 + 3 -> 2
		// /3 + /2 -> /2
		// /1 -> /1
		simplifier.simplifyAll(tf, false);		
		assertEquals("<1><2><3>T1</3></2>T2</1>", fmt.setContent(tf).toString());
	}

	@Test
	public void testCodeReduction06 () {
		TextFragment tf = new TextFragment();
		tf.append(TagType.OPENING, "a", "<a>");
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append(TagType.OPENING, "c", "<c>");
		tf.append(TagType.PLACEHOLDER, "x1", "<x1/>");
		tf.append("T1");
		tf.append(TagType.CLOSING, "c", "</c>");
		tf.append(TagType.CLOSING, "b", "</b>");
		tf.append("T2");
		tf.append(TagType.CLOSING, "a", "</a>");
		tf.append(TagType.PLACEHOLDER, "x2", "<x2/>");

		assertEquals("<1><2><3><4/>T1</3></2>T2</1><5/>", fmt.setContent(tf).toString());
		// 1 -> 1
		// 2 + 3 + 4/ -> 2
		// /3 + /2 -> /2
		// /1 + 5/ -> /1
		simplifier.simplifyAll(tf, false);		
		assertEquals("<1><2><3><4/>T1</3></2>T2</1><5/>", fmt.setContent(tf).toString());
	}

	@Test
	public void testCodeReduction07 () {
		TextFragment tf = new TextFragment();
		tf.append(TagType.OPENING, "a", "<a>");
		tf.append(TagType.PLACEHOLDER, "x1", "<x1/>");
		tf.append("T1");
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append(TagType.OPENING, "c", "<c>");
		tf.append(TagType.PLACEHOLDER, "x2", "<x2/>");
		tf.append(TagType.CLOSING, "c", "</c>");
		tf.append(TagType.CLOSING, "b", "</b>");
		tf.append("T2");
		tf.append(TagType.CLOSING, "a", "</a>");
		tf.append(TagType.PLACEHOLDER, "x3", "<x3/>");

		assertEquals("<1><2/>T1<3><4><5/></4></3>T2</1><6/>", fmt.setContent(tf).toString());
		// 1 + 2/ -> 1
		// 3 + 4 + 5/ + /4 + /3 -> 2/
		// /1 + 6/ -> /1
		simplifier.simplifyAll(tf, false);				
		simplifier.simplifyEmptyOpeningClosing(tf);		
		assertEquals("<1><2/>T1<3><4><5/></4></3>T2</1><6/>", fmt.setContent(tf).toString());
	}

	@Test
	public void testCodeReduction08 () {
		TextFragment tf = new TextFragment();
		tf.append(TagType.OPENING, "a", "<a>");
		tf.append(TagType.PLACEHOLDER, "x1", "<x1/>");
		tf.append("T1");
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append(TagType.OPENING, "c", "<c>");
		tf.append(TagType.PLACEHOLDER, "x2", "<x2/>");
		tf.append(TagType.CLOSING, "c", "</c>");
		tf.append("T2");
		tf.append(TagType.CLOSING, "b", "</b>");
		tf.append("T3");
		tf.append(TagType.CLOSING, "a", "</a>");
		tf.append(TagType.PLACEHOLDER, "x3", "<x3/>");

		assertEquals("<1><2/>T1<3><4><5/></4>T2</3>T3</1><6/>", fmt.setContent(tf).toString());
		// 1 + 2/ -> 1
		// 3 + 4 + 5/ + /4 -> 2
		// /3 -> /2
		// /1 + 6/ -> /1
		simplifier.simplifyAll(tf, false);
		assertEquals("<1><2/>T1<3><4><5/></4>T2</3>T3</1><6/>", fmt.setContent(tf).toString());
	}
			
	@Test
	public void testCodeReduction09 () {
		TextFragment tf = new TextFragment();
		tf.append(TagType.OPENING, "a", "<a>");
		tf.append(TagType.PLACEHOLDER, "x1", "<x1/>");
		tf.append("T1");
		tf.append(TagType.CLOSING, "a", "</a>");
		tf.append(TagType.OPENING, "b", "<b>");		
		tf.append(TagType.PLACEHOLDER, "x2", "<x2/>");
		tf.append("T2");		
		tf.append(TagType.CLOSING, "b", "</b>");
		tf.append(TagType.OPENING, "c", "<c>");
		tf.append("T3");
		tf.append(TagType.CLOSING, "c", "</c>");			
		tf.append(TagType.PLACEHOLDER, "x3", "<x3/>");

		assertEquals("<1><2/>T1</1><3><4/>T2</3><5>T3</5><6/>", fmt.setContent(tf).toString());
		
		simplifier.simplifyAll(tf, false);
		assertEquals("<1><2/>T1</1><3><4/>T2</3><5>T3</5><6/>", fmt.setContent(tf).toString());
	}
	
	@Test
	public void testCodeReduction10 () {
		TextFragment tf = new TextFragment();
		tf.append(TagType.OPENING, "a", "<a>");
		tf.append(TagType.PLACEHOLDER, "x1", "<x1/>");
		tf.append("T1");
		tf.append(TagType.CLOSING, "a", "</a>");				
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append(TagType.OPENING, "c", "<c>");
		tf.append(TagType.PLACEHOLDER, "x2", "<x2/>");
		tf.append("T2");
		tf.append(TagType.CLOSING, "c", "</c>");
		tf.append("T3");
		tf.append(TagType.CLOSING, "b", "</b>");		
		tf.append(TagType.PLACEHOLDER, "x3", "<x3/>");

		assertEquals("<1><2/>T1</1><3><4><5/>T2</4>T3</3><6/>", fmt.setContent(tf).toString());
		
		simplifier.simplifyAll(tf, false);
		assertEquals("<1><2/>T1</1><3><4><5/>T2</4>T3</3><6/>", fmt.setContent(tf).toString());
	}

	@Test
	public void testCodeReduction11 () {
		TextFragment tf = new TextFragment();
		tf.append(TagType.OPENING, "a", "<a>");
		tf.append(TagType.OPENING, "d", "<d>");
		tf.append(TagType.PLACEHOLDER, "x1", "<x1/>");
		tf.append("T1");
		tf.append(TagType.CLOSING, "d", "</d>");
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append(TagType.OPENING, "c", "<c>");
		tf.append(TagType.PLACEHOLDER, "x2", "<x2/>");
		tf.append(TagType.CLOSING, "c", "</c>");
		tf.append("T2");
		tf.append(TagType.CLOSING, "b", "</b>");
		tf.append("T3");
		tf.append(TagType.CLOSING, "a", "</a>");
		tf.append(TagType.PLACEHOLDER, "x3", "<x3/>");

		assertEquals("<1><2><3/>T1</2><4><5><6/></5>T2</4>T3</1><7/>", fmt.setContent(tf).toString());
		
		simplifier.simplifyAll(tf, false);
		assertEquals("<1><2><3/>T1</2><4><5><6/></5>T2</4>T3</1><7/>", fmt.setContent(tf).toString());
	}
	
	@Test
	public void testCodeReduction12 () {
		TextFragment tf = new TextFragment();
		tf.append(TagType.OPENING, "a", "<a>");
		assertEquals("<b1/>", fmt.setContent(tf).toString());
	}
	
	@Test
	public void testCodeReduction13 () {
		// Hanging opening code d
		TextFragment tf = new TextFragment();
		tf.append(TagType.OPENING, "a", "<a>");
		tf.append(TagType.OPENING, "d", "<d>"); // no corresp. closing marker
		tf.append(TagType.PLACEHOLDER, "x1", "<x1/>");
		tf.append("T1");
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append(TagType.OPENING, "c", "<c>");
		tf.append(TagType.PLACEHOLDER, "x2", "<x2/>");
		tf.append(TagType.CLOSING, "c", "</c>");
		tf.append("T2");
		tf.append(TagType.CLOSING, "b", "</b>");
		tf.append("T3");
		tf.append(TagType.CLOSING, "a", "</a>");
		tf.append(TagType.PLACEHOLDER, "x3", "<x3/>");

		assertEquals("<b1/><b2/><3/>T1<4><5><6/></5>T2</4>T3<e1/><7/>", fmt.setContent(tf).toString());
		
		simplifier.simplifyAll(tf, false);
		assertEquals("<1><b2/><3/>T1<4><5><6/></5>T2</4>T3</1><7/>", fmt.setContent(tf).toString());
	}
	
	@Test
	public void testCodeReduction14 () {
		// Hanging closing code d
		TextFragment tf = new TextFragment();
		tf.append(TagType.OPENING, "a", "<a>");		
		tf.append(TagType.PLACEHOLDER, "x1", "<x1/>");
		tf.append("T1");
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append(TagType.OPENING, "c", "<c>");		
		tf.append(TagType.PLACEHOLDER, "x2", "<x2/>");
		tf.append(TagType.CLOSING, "c", "</c>");
		tf.append("T2");
		tf.append(TagType.CLOSING, "b", "</b>");
		tf.append("T3");
		tf.append(TagType.CLOSING, "a", "</a>");
		tf.append(TagType.CLOSING, "d", "<d>"); // no corresp. opening marker
		tf.append(TagType.PLACEHOLDER, "x3", "<x3/>");

		assertEquals("<1><2/>T1<3><4><5/></4>T2</3>T3</1><e7/><6/>", fmt.setContent(tf).toString());
		
		simplifier.simplifyAll(tf, false);
		assertEquals("<1><2/>T1<3><4><5/></4>T2</3>T3</1><e7/><6/>", fmt.setContent(tf).toString());
	}
	
	@Test
	public void testCodeReduction15 () {
		// Hanging closing code d
		TextFragment tf = new TextFragment();
		tf.append(TagType.OPENING, "a", "<a>");		
		tf.append(TagType.PLACEHOLDER, "x1", "<x1/>");
		tf.append("T1");
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append(TagType.OPENING, "c", "<c>");		
		tf.append(TagType.PLACEHOLDER, "x2", "<x2/>");
		tf.append(TagType.CLOSING, "c", "</c>");
		tf.append("T2");
		tf.append(TagType.CLOSING, "b", "</b>");
		tf.append("T3");
		tf.append(TagType.CLOSING, "d", "<d>"); // no corresp. opening marker
		tf.append(TagType.CLOSING, "a", "</a>");		
		tf.append(TagType.PLACEHOLDER, "x3", "<x3/>");

		assertEquals("<b1/><2/>T1<3><4><5/></4>T2</3>T3<e7/><e1/><6/>", fmt.setContent(tf).toString());
		
		simplifier.simplifyAll(tf, false);
		assertEquals("<1><2/>T1<3><4><5/></4>T2</3>T3<e7/></1><6/>", fmt.setContent(tf).toString());
	}
	
	@Test
	public void testCodeReduction16 () {
		// Hanging closing code d
		TextFragment tf = new TextFragment();
		tf.append(TagType.OPENING, "a", "<a>");		
		tf.append(TagType.PLACEHOLDER, "x1", "<x1/>");
		tf.append("T1");
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append(TagType.OPENING, "c", "<c>");
		tf.append(TagType.CLOSING, "d", "<d>"); // no corresp. opening marker
		tf.append(TagType.PLACEHOLDER, "x2", "<x2/>");
		tf.append(TagType.CLOSING, "c", "</c>");
		tf.append("T2");
		tf.append(TagType.CLOSING, "b", "</b>");
		tf.append("T3");
		tf.append(TagType.CLOSING, "a", "</a>");
		tf.append(TagType.PLACEHOLDER, "x3", "<x3/>");

		assertEquals("<b1/><2/>T1<b3/><b4/><e7/><5/><e4/>T2<e3/>T3<e1/><6/>", fmt.setContent(tf).toString());
		
		simplifier.simplifyAll(tf, false);
		assertEquals("<1><2/>T1<3><4><e7/><5/></4>T2</3>T3</1><6/>", fmt.setContent(tf).toString());
	}
	
	@Test
	public void testCodeReduction17 () {
		TextFragment tf = new TextFragment();
		tf.append(TagType.PLACEHOLDER, "x1", "<x1/>");
		tf.append("T1");
		tf.append(TagType.PLACEHOLDER, "x2", "<x2/>");
		
		assertEquals("<1/>T1<2/>", fmt.setContent(tf).toString());
			
		TextFragment[] res = simplifier.simplifyAll(tf, true);
		assertNull(res);
		assertEquals("<1/>T1<2/>", fmt.setContent(tf).toString());
	}

	@Test
	public void testCodeReduction18 () {
		TextFragment tf = new TextFragment("T1");
		tf.append(TagType.PLACEHOLDER, "x1", "<x1/>");
		tf.append("T2");
		tf.append(TagType.PLACEHOLDER, "x2", "<x2/>");
		
		assertEquals("T1<1/>T2<2/>", fmt.setContent(tf).toString());
			
		TextFragment[] res = simplifier.simplifyAll(tf, true);
		assertNull(res);		
		assertEquals("T1<1/>T2<2/>", fmt.setContent(tf).toString());
	}

	@Test
	public void testCodeReduction19 () {
		TextFragment tf = new TextFragment();
		tf.append(TagType.PLACEHOLDER, "x1", "<x1/>");
		tf.append("T1");
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append(TagType.OPENING, "c", "<c>");
		tf.append(TagType.PLACEHOLDER, "x2", "<x2/>");
		tf.append(TagType.CLOSING, "c", "</c>");
		tf.append("T2");
		tf.append(TagType.CLOSING, "b", "</b>");
		tf.append("T3");
		tf.append(TagType.PLACEHOLDER, "x3", "<x3/>");

		assertEquals("<1/>T1<2><3><4/></3>T2</2>T3<5/>", fmt.setContent(tf).toString());
		
		TextFragment[] res = simplifier.simplifyAll(tf, true);
		assertNull(res);
		assertEquals("<1/>T1<2><3><4/></3>T2</2>T3<5/>", fmt.setContent(tf).toString());
	}
	
	@Test
	public void testCodeReduction20 () {
		TextFragment tf = new TextFragment();
		tf.append(TagType.PLACEHOLDER, "x1", "<x1/>");
		tf.append(TagType.PLACEHOLDER, "x2", "<x2/>");
		tf.append("T1");
		
		assertEquals("<1/><2/>T1", fmt.setContent(tf).toString());
		
		TextFragment[] res = simplifier.simplifyAll(tf, true);
		assertNull(res);
		assertEquals("<1/><2/>T1", fmt.setContent(tf).toString());
	}
	
	@Test
	public void testCodeReduction21 () {
		TextFragment tf = new TextFragment();
		tf.append(TagType.OPENING, "a", "<a>");
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append("T1");
		
		assertEquals("<b1/><b2/>T1", fmt.setContent(tf).toString());
		
		TextFragment[] res = simplifier.simplifyAll(tf, true);
		assertNull(res);
		assertEquals("<b1/><b2/>T1", fmt.setContent(tf).toString());
	}
	
	@Test
	public void testCodeReduction22 () {
		// Spaces in-between codes
		TextFragment tf = new TextFragment();
		tf.append(TagType.PLACEHOLDER, "x1", "<x1/>");
		tf.append(" ");
		tf.append(TagType.PLACEHOLDER, "x2", "<x2/>");
		tf.append(" ");
		tf.append("T1");
		
		assertEquals("<1/> <2/> T1", fmt.setContent(tf).toString());
		
		TextFragment[] res = simplifier.simplifyAll(tf, true);
		assertNull(res);
		assertEquals("<1/> <2/> T1", fmt.setContent(tf).toString());
	}
	
	@Test
	public void testCodeReduction23 () {
		TextFragment tf = new TextFragment();
		tf.append(TagType.OPENING, "a", "<a>");
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append("T1");
		tf.append(TagType.CLOSING, "b", "</b>");
		tf.append(TagType.CLOSING, "a", "</a>");
		
		assertEquals("<1><2>T1</2></1>", fmt.setContent(tf).toString());
		
		TextFragment[] res = simplifier.simplifyAll(tf, true);
		assertNull(res);
		assertEquals("<1><2>T1</2></1>", fmt.setContent(tf).toString());
	}
	
	@Test
	public void testCodeReduction24 () {
		// Wrong sequence of closing tags (malformed tags)
		TextFragment tf = new TextFragment();
		tf.append(TagType.OPENING, "a", "<a>");
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append("T1");
		tf.append(TagType.CLOSING, "a", "</a>");
		tf.append(TagType.CLOSING, "b", "</b>");
				
		assertEquals("<b1/><b2/>T1<e1/><e2/>", fmt.setContent(tf).toString());
		
		TextFragment[] res = simplifier.simplifyAll(tf, true);
		assertNull(res);
		assertEquals("<1><2>T1</1></2>", fmt.setContent(tf).toString());
	}
	
	@Test
	public void testCodeReduction25 () {
		TextFragment tf = new TextFragment();
		tf.append(TagType.OPENING, "a", "<a>");
		tf.append(" ");
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append("T1");		
		tf.append(TagType.CLOSING, "b", "</b>");
		tf.append(" ");
		tf.append(TagType.CLOSING, "a", "</a>");
				
		assertEquals("<1> <2>T1</2> </1>", fmt.setContent(tf).toString());
		
		TextFragment[] res = simplifier.simplifyAll(tf, true);
		assertNull(res);
		assertEquals("<1> <2>T1</2> </1>", fmt.setContent(tf).toString());
	}
	
	@Test
	public void testCodeReduction26 () {
		// Wrong sequence of closing tags (malformed tags), nothing is removed
		TextFragment tf = new TextFragment();
		tf.append(TagType.OPENING, "a", "<a>");
		tf.append(" ");
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append(" ");
		tf.append("T1");
		tf.append(" ");
		tf.append(TagType.CLOSING, "a", "</a>");
		tf.append(" ");
		tf.append(TagType.CLOSING, "b", "</b>");
				
		assertEquals("<b1/> <b2/> T1 <e1/> <e2/>", fmt.setContent(tf).toString());
		
		TextFragment[] res = simplifier.simplifyAll(tf, true);
		assertEquals("<1> <2> T1 </1> </2>", fmt.setContent(tf).toString());
		assertNull(res);
	}
	
	@Test
	public void testCodeReduction27 () {
		// Spaces in-between codes
		TextFragment tf = new TextFragment();
		tf.append(TagType.PLACEHOLDER, "x1", "<x1/>");
		tf.append(" ");
		tf.append(TagType.PLACEHOLDER, "x2", "<x2/>");
		tf.append(" ");
		tf.append("T1");
		
		assertEquals("<1/> <2/> T1", fmt.setContent(tf).toString());
		
		TextFragment[] res = simplifier.simplifyAll(tf, true);
		assertNull(res);
		assertEquals("<1/> <2/> T1", fmt.setContent(tf).toString());
	}

	@Test
	public void testCodeReduction28 () {
		TextFragment tf = new TextFragment();
		tf.append(TagType.OPENING, "a", "<a>");
		tf.append(" ");
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append("T1");		
		tf.append(TagType.CLOSING, "b", "</b>");
				
		assertEquals("<b1/> <2>T1</2>", fmt.setContent(tf).toString());
		
		TextFragment[] res = simplifier.simplifyAll(tf, true);
		assertNull(res);
		assertEquals("<b1/> <2>T1</2>", fmt.setContent(tf).toString());
	}
	
	@Test
	public void testCodeReduction29 () {
		TextFragment tf = new TextFragment();
		tf.append(TagType.OPENING, "a", "<a>");
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append("T1");		
		tf.append(TagType.CLOSING, "b", "</b>");
				
		assertEquals("<b1/><2>T1</2>", fmt.setContent(tf).toString());
		
		TextFragment[] res = simplifier.simplifyAll(tf, true);
		assertNull(res);
		assertEquals("<b1/><2>T1</2>", fmt.setContent(tf).toString());
	}
	
	@Test
	public void testCodeReduction30 () {
		TextFragment tf = new TextFragment();
		tf.append(TagType.OPENING, "a", "<a>");
		tf.append("T1");
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append("T2");		
		tf.append(TagType.CLOSING, "b", "</b>");
				
		assertEquals("<b1/>T1<2>T2</2>", fmt.setContent(tf).toString());
		
		TextFragment[] res = simplifier.simplifyAll(tf, true);
		assertEquals("<b1/>T1<2>T2</2>", fmt.setContent(tf).toString());
		assertNull(res);
	}
	
	@Test
	public void testCodeReduction31 () {
		TextFragment tf = new TextFragment();		
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append("T1");		
		tf.append(TagType.CLOSING, "b", "</b>");
		tf.append("T2");
		tf.append(TagType.CLOSING, "a", "</a>");
				
		assertEquals("<1>T1</1>T2<e2/>", fmt.setContent(tf).toString());
		
		TextFragment[] res = simplifier.simplifyAll(tf, true);
		assertEquals("<1>T1</1>T2<e2/>", fmt.setContent(tf).toString());
		assertNull(res);
	}
	
	@Test
	public void testCodeReduction32 () {
		TextFragment tf = new TextFragment();
		tf.append(TagType.OPENING, "a", "<a>");
		tf.append(" ");
		tf.append("T1");
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append("T2");		
		tf.append(TagType.CLOSING, "b", "</b>");
				
		assertEquals("<b1/> T1<2>T2</2>", fmt.setContent(tf).toString());
		
		TextFragment[] res = simplifier.simplifyAll(tf, true);
		assertEquals("<b1/> T1<2>T2</2>", fmt.setContent(tf).toString());
		assertNull(res);
	}
	
	@Test
	public void testCodeReduction33 () {
		TextFragment tf = new TextFragment();		
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append("T1");		
		tf.append(TagType.CLOSING, "b", "</b>");
		tf.append("T2");
		tf.append(" ");
		tf.append(TagType.CLOSING, "a", "</a>");
				
		assertEquals("<1>T1</1>T2 <e2/>", fmt.setContent(tf).toString());
		
		TextFragment[] res = simplifier.simplifyAll(tf, true);
		assertEquals("<1>T1</1>T2 <e2/>", fmt.setContent(tf).toString());
		assertNull(res);
	}
	
	@Test
	public void testCodeReduction34 () {
		TextFragment tf = new TextFragment();
		tf.append("T1");
		tf.append(TagType.OPENING, "a", "<a>");
		tf.append("T2");
		tf.append(TagType.CLOSING, "a", "</a>");
		tf.append("T3");		
						
		assertEquals("T1<1>T2</1>T3", fmt.setContent(tf).toString());
		
		TextFragment[] res = simplifier.simplifyAll(tf, true);
		assertEquals("T1<1>T2</1>T3", fmt.setContent(tf).toString());
		assertNull(res);
	}
	
	@Test
	public void testCodeReduction35 () {
		TextFragment tf = new TextFragment();		
		tf.append(TagType.OPENING, "a", "<a>");
		tf.append("T1");		
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append(TagType.CLOSING, "b", "</b>");
				
		assertEquals("<b1/>T1<2></2>", fmt.setContent(tf).toString());
		
		TextFragment[] res = simplifier.simplifyAll(tf, true);
		assertNull(res);
		assertEquals("<b1/>T1<2></2>", fmt.setContent(tf).toString());
	}
	
	@Test
	public void testCodeReduction36 () {
		// Adjacent codes are not removed, because there's a space in-between
		TextFragment tf = new TextFragment();		
		tf.append(TagType.OPENING, "a", "<a>");
		tf.append("T1");		
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append(" ");
		tf.append(TagType.CLOSING, "b", "</b>");
				
		assertEquals("<b1/>T1<2> </2>", fmt.setContent(tf).toString());
		
		TextFragment[] res = simplifier.simplifyAll(tf, true);
		assertEquals("<b1/>T1<2> </2>", fmt.setContent(tf).toString());
		assertNull(res);
	}
	
	@Test
	public void testCodeReduction37 () {
		// String is not trimmed by simplification
		TextFragment tf = new TextFragment();
		tf.append("   ");
		tf.append("T1");		
		tf.append("   ");
				
		assertEquals("   T1   ", fmt.setContent(tf).toString());
		
		TextFragment[] res = simplifier.simplifyAll(tf, true);
		assertEquals("   T1   ", fmt.setContent(tf).toString());
		assertNull(res);
	}
	
	@Test
	public void testCodeReduction38 () {
		TextFragment tf = new TextFragment();		
		tf.append(TagType.PLACEHOLDER, "x1", "<x1/>");
		tf.append(TagType.PLACEHOLDER, "x2", "<x2/>");
		tf.append("T1");		
		tf.append(TagType.PLACEHOLDER, "x3", "<x3/>");
		tf.append(TagType.PLACEHOLDER, "x4", "<x4/>");
		tf.append(TagType.OPENING, "a", "<a>");
		tf.append("T2");
		tf.append(TagType.CLOSING, "a", "</a>");
				
		assertEquals("<1/><2/>T1<3/><4/><5>T2</5>", fmt.setContent(tf).toString());
		
		TextFragment[] res = simplifier.simplifyAll(tf, true);
		assertEquals("<1/><2/>T1<3/><4/><5>T2</5>", fmt.setContent(tf).toString());
		assertNull(res);
	}
	
	@Test
	public void testCodeReduction39 () {
		TextFragment tf = new TextFragment();		
		tf.append("T1");		
		tf.append(TagType.PLACEHOLDER, "x1", "<x1/>");
		tf.append(TagType.PLACEHOLDER, "x2", "<x2/>");
		tf.append(TagType.OPENING, "c", "<c>");
		tf.append("T2");
		tf.append(TagType.CLOSING, "c", "</c>");
				
		assertEquals("T1<1/><2/><3>T2</3>", fmt.setContent(tf).toString());
		
		TextFragment[] res = simplifier.simplifyAll(tf, true);
		assertEquals("T1<1/><2/><3>T2</3>", fmt.setContent(tf).toString());
		assertNull(res);
	}
	
	@Test
	public void testCodeReduction40 () {
		TextFragment tf = new TextFragment();		
		tf.append("T1");		
		tf.append(TagType.OPENING, "c", "<c>");
		tf.append("T2");
		tf.append(TagType.CLOSING, "c", "</c>");
				
		assertEquals("T1<1>T2</1>", fmt.setContent(tf).toString());
		
		TextFragment[] res = simplifier.simplifyAll(tf, true);
		assertEquals("T1<1>T2</1>", fmt.setContent(tf).toString());
		assertNull(res);
	}
	
	@Test
	public void testCodeReduction41 () {
		TextFragment tf = new TextFragment();		
		tf.append("T1");		
		tf.append(TagType.PLACEHOLDER, "x1", "<x1/>");
		tf.append(TagType.OPENING, "c", "<c>");
		tf.append("T2");
		tf.append(TagType.CLOSING, "c", "</c>");
				
		assertEquals("T1<1/><2>T2</2>", fmt.setContent(tf).toString());
		
		TextFragment[] res = simplifier.simplifyAll(tf, true);
		assertEquals("T1<1/><2>T2</2>", fmt.setContent(tf).toString());
		assertNull(res);
	}
	
	@Test
	public void testCodeReduction42 () {
		TextFragment tf = new TextFragment();		
		tf.append(TagType.OPENING, "a", "<a>");
		tf.append("T1");
		tf.append(TagType.CLOSING, "a", "</a>");
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append("T2");
		tf.append(TagType.CLOSING, "b", "</b>");
		tf.append(TagType.OPENING, "c", "<c>");
		tf.append("T3");
		tf.append(TagType.CLOSING, "c", "</c>");		
				
		assertEquals("<1>T1</1><2>T2</2><3>T3</3>", fmt.setContent(tf).toString());
		
		TextFragment[] res = simplifier.simplifyAll(tf, true);
		assertEquals("<1>T1</1><2>T2</2><3>T3</3>", fmt.setContent(tf).toString());
		assertNull(res);
	}
	
	@Test
	public void testCodeReduction43 () {
		TextFragment tf = new TextFragment();		
		tf.append(TagType.PLACEHOLDER, "x1", "<x1/>");
		tf.append(TagType.PLACEHOLDER, "x2", "<x2/>");
		tf.append("T1");		
		tf.append(TagType.PLACEHOLDER, "x3", "<x3/>");
		tf.append(TagType.PLACEHOLDER, "x4", "<x4/>");
		tf.append(TagType.OPENING, "a", "<a>");
		tf.append("T2");
		tf.append(TagType.CLOSING, "a", "</a>");
		tf.append(TagType.CLOSING, "b", "</b>");
		tf.append(TagType.PLACEHOLDER, "x5", "<x5/>");
		tf.append(TagType.PLACEHOLDER, "x6", "<x6/>");
				
		assertEquals("<1/><2/>T1<3/><4/><5>T2</5><e8/><6/><7/>", fmt.setContent(tf).toString());
		
		TextFragment[] res = simplifier.simplifyAll(tf, true);
		assertEquals("<1/><2/>T1<3/><4/><5>T2</5><e8/><6/><7/>", fmt.setContent(tf).toString());
		assertNull(res);
	}
	
	@Test
	public void testCodeReduction44 () {
		TextFragment tf = new TextFragment();		
		tf.append(TagType.PLACEHOLDER, "x1", "<x1/>");
		tf.append("   ");
		tf.append(TagType.PLACEHOLDER, "x2", "<x2/>");
		tf.append("T1");		
		tf.append(TagType.PLACEHOLDER, "x3", "<x3/>");
		tf.append(TagType.PLACEHOLDER, "x4", "<x4/>");
		tf.append(TagType.OPENING, "a", "<a>");
		tf.append("T2");
		tf.append("   ");
		tf.append(TagType.CLOSING, "a", "</a>");
		tf.append("   ");
		tf.append(TagType.CLOSING, "b", "</b>");
		tf.append(TagType.PLACEHOLDER, "x5", "<x5/>");
		tf.append("   ");
		tf.append(TagType.PLACEHOLDER, "x6", "<x6/>");
				
		assertEquals("<1/>   <2/>T1<3/><4/><5>T2   </5>   <e8/><6/>   <7/>", fmt.setContent(tf).toString());
		
		TextFragment[] res = simplifier.simplifyAll(tf, true);
		assertEquals("<1/>   <2/>T1<3/><4/><5>T2   </5>   <e8/><6/>   <7/>", fmt.setContent(tf).toString());
		assertNull(res);
	}
	
	@Test
	public void testCodeReduction45 () {
		TextFragment tf = new TextFragment();		
		tf.append(TagType.OPENING, "a", "<a>");
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append(" ");
		tf.append(TagType.OPENING, "c", "<c>");
		tf.append("T1");
		tf.append("   ");
		tf.append(TagType.PLACEHOLDER, "x1", "<x1/>");
		tf.append("T2");
		tf.append(TagType.CLOSING, "c", "</c>");
		tf.append("   ");
		tf.append(TagType.CLOSING, "b", "</b>");
		tf.append(TagType.CLOSING, "a", "</a>");
				
		assertEquals("<1><2> <3>T1   <4/>T2</3>   </2></1>", fmt.setContent(tf).toString());
		
		TextFragment[] res = simplifier.simplifyAll(tf, true);
		assertEquals("<1><2> <3>T1   <4/>T2</3>   </2></1>", fmt.setContent(tf).toString());
		assertNull(res);
	}
	
	@Test
	public void testCodeReduction46 () {
		TextFragment tf = new TextFragment();
		tf.append(TagType.OPENING, "a", "<a>");		
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append("T1");				
		tf.append(TagType.CLOSING, "b", "</b>");
		tf.append("T2");
		tf.append(TagType.CLOSING, "a", "</a>");
				
		assertEquals("<1><2>T1</2>T2</1>", fmt.setContent(tf).toString());
		
		TextFragment[] res = simplifier.simplifyAll(tf, true);
		assertEquals("<1><2>T1</2>T2</1>", fmt.setContent(tf).toString());
		assertNull(res);
	}
	
	@Test
	public void testCodeReduction47 () {
		TextFragment tf = new TextFragment();		
		tf.append(TagType.PLACEHOLDER, "x1", "<x1/>");
		tf.append(TagType.PLACEHOLDER, "x2", "<x2/>");
		tf.append(TagType.PLACEHOLDER, "x3", "<x3/>");
		tf.append(TagType.PLACEHOLDER, "x4", "<x4/>");
				
		assertEquals("<1/><2/><3/><4/>", fmt.setContent(tf).toString());
		
		TextFragment[] res = simplifier.simplifyAll(tf, false);
		assertEquals("<1/><2/><3/><4/>", fmt.setContent(tf).toString());
		assertNull(res);
	}
	
	@Test
	public void testCodeReduction48 () {
		TextFragment tf = new TextFragment();		
		tf.append(TagType.PLACEHOLDER, "x1", "<x1/>");
		tf.append(" ");
		tf.append(TagType.PLACEHOLDER, "x2", "<x2/>");
				
		assertEquals("<1/> <2/>", fmt.setContent(tf).toString());
		assertEquals("<x1/> <x2/>", tf.toText());
		
		TextFragment[] res = simplifier.simplifyAll(tf, false);
		assertNull(res);
		assertEquals("<1/> <2/>", fmt.setContent(tf).toString());
		assertEquals("<x1/> <x2/>", tf.toText());
	}
	
	@Test
	public void testCodeReduction49 () {
		TextFragment tf = new TextFragment();		
		tf.append(TagType.OPENING, "a", "<a>");
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append(" ");
		tf.append(TagType.OPENING, "c", "<c>");
		tf.append("   ");
		tf.append("T1");
		tf.append("   ");
		tf.append(TagType.PLACEHOLDER, "x1", "<x1/>");
		tf.append("T2");
		tf.append(TagType.CLOSING, "c", "</c>");
		tf.append("   ");
		tf.append(TagType.CLOSING, "b", "</b>");
		tf.append(TagType.CLOSING, "a", "</a>");
				
		assertEquals("<1><2> <3>   T1   <4/>T2</3>   </2></1>", fmt.setContent(tf).toString());
		
		TextFragment[] res = simplifier.simplifyAll(tf, true);
		assertEquals("<1><2> <3>   T1   <4/>T2</3>   </2></1>", fmt.setContent(tf).toString());
		assertNull(res);
	}

	@Test
	public void testCodeReduction50 () {
		TextFragment tf = new TextFragment(".");
		tf.append(TagType.PLACEHOLDER, "x1", "<x/>");
		tf.append(TagType.PLACEHOLDER, "x2", "<x/>");
		assertEquals(".<1/><2/>", fmt.setContent(tf).toString());
		TextFragment[] res = simplifier.simplifyAll(tf, true);		
		assertEquals(".<1/><2/>", fmt.setContent(tf).toString());
		assertNull(res);
	}
	
	@Test
	public void testCodeReduction51 () {
		TextFragment tf = new TextFragment();
		tf.append(TagType.OPENING, "a", "<a>");		
		tf.append("The plan of");
		tf.append(TagType.CLOSING, "a", "</a>");
		
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append(TagType.PLACEHOLDER, "x1", "<x/>");
		tf.append(TagType.CLOSING, "b", "</b>");
		
		tf.append(TagType.OPENING, "c", "<c>");
		tf.append("happiness");
		tf.append(TagType.CLOSING, "c", "</c>");
		
		tf.append(TagType.OPENING, "d", "<d>");
		tf.append(TagType.PLACEHOLDER, "x2", "<x/>");
		tf.append(TagType.CLOSING, "d", "</d>");
		
		assertEquals("<1>The plan of</1><2><3/></2><4>happiness</4><5><6/></5>", fmt.setContent(tf).toString());
		simplifier.simplifyAll(tf, true);		
		assertEquals("<1>The plan of</1><2><3/></2><4>happiness</4><5><6/></5>", fmt.setContent(tf).toString());
	}

	@Test
	public void testCodeReduction52 () {
		TextFragment tf = new TextFragment();
		tf.append(TagType.PLACEHOLDER, "x1", "<x/>");
		tf.append(TagType.PLACEHOLDER, "x2", "<x/>");
		tf.append(TagType.OPENING, "a", "<a>");
		tf.append(TagType.PLACEHOLDER, "x3", "<x/>");
		tf.append("The plan of");
		tf.append(TagType.CLOSING, "a", "</a>");
		
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append(TagType.PLACEHOLDER, "x4", "<x/>");
		tf.append(TagType.CLOSING, "b", "</b>");
		
		tf.append(TagType.OPENING, "c", "<c>");
		tf.append("happiness");
		tf.append(TagType.CLOSING, "c", "</c>");
		
		tf.append(TagType.OPENING, "d", "<d>");
		tf.append(TagType.PLACEHOLDER, "x5", "<x/>");
		tf.append(TagType.CLOSING, "d", "</d>");
		tf.append(TagType.PLACEHOLDER, "x6", "<x/>");
		
		assertEquals("<1/><2/><3><4/>The plan of</3><5><6/></5><7>happiness</7><8><9/></8><10/>", fmt.setContent(tf).toString());
		TextFragment[] res = simplifier.simplifyAll(tf, true);		
		assertEquals("<1/><2/><3><4/>The plan of</3><5><6/></5><7>happiness</7><8><9/></8><10/>", fmt.setContent(tf).toString());
		assertNull(res);
	}
	
	@Test
	public void testCodeReduction53 () {
		TextFragment tf = new TextFragment();
		tf.append(TagType.OPENING, "a", "<a>");
		tf.append("The plan of");
		tf.append(TagType.CLOSING, "a", "</a>");
		
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append(TagType.PLACEHOLDER, "x4", "<x/>");
		tf.append(TagType.CLOSING, "b", "</b>");
		
		tf.append(TagType.OPENING, "c", "<c>");
		tf.append("happiness");
		tf.append(TagType.CLOSING, "c", "</c>");
		
		tf.append(TagType.OPENING, "d", "<d>");
		tf.append(TagType.PLACEHOLDER, "x5", "<x/>");
		tf.append(TagType.CLOSING, "d", "</d>");
		
		assertEquals("<1>The plan of</1><2><3/></2><4>happiness</4><5><6/></5>", fmt.setContent(tf).toString());
		TextFragment[] res = simplifier.simplifyAll(tf, true);		
		assertEquals("<1>The plan of</1><2><3/></2><4>happiness</4><5><6/></5>", fmt.setContent(tf).toString());
		assertNull(res);
	}
	
	@Test
	public void testCodeReduction54 () {
		TextFragment tf = new TextFragment();
		tf.append("The plan of");
		tf.append(TagType.CLOSING, "a", "</a>");
		
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append(TagType.PLACEHOLDER, "x4", "<x/>");
		tf.append(TagType.CLOSING, "b", "</b>");
		
		tf.append(TagType.OPENING, "c", "<c>");
		tf.append("happiness");
		tf.append(TagType.CLOSING, "c", "</c>");
		
		tf.append(TagType.OPENING, "d", "<d>");
		tf.append(TagType.PLACEHOLDER, "x5", "<x/>");
		tf.append(TagType.CLOSING, "d", "</d>");
		
		assertEquals("The plan of<e6/><1><2/></1><3>happiness</3><4><5/></4>", fmt.setContent(tf).toString());
		TextFragment[] res = simplifier.simplifyAll(tf, true);		
		assertEquals("The plan of<e6/><1><2/></1><3>happiness</3><4><5/></4>", fmt.setContent(tf).toString());
		assertNull(res);
	}
	
	@Test
	public void testCodeReduction55 () {		
		TextFragment tf = new TextFragment();
		tf.append(TagType.OPENING, "a", "<a>");
		tf.append(TagType.PLACEHOLDER, "x4", "<x/>");
		tf.append(TagType.CLOSING, "a", "</a>");
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append("The plan of");
		tf.append(TagType.CLOSING, "b", "</b>");
				
		tf.append(TagType.OPENING, "c", "<c>");
		tf.append(TagType.PLACEHOLDER, "x5", "<x/>");
		tf.append(TagType.CLOSING, "c", "</c>");
		
		tf.append(TagType.OPENING, "d", "<d>");
		tf.append("happiness");
		
		assertEquals("<1><2/></1><3>The plan of</3><4><5/></4><b6/>happiness", fmt.setContent(tf).toString());
		TextFragment[] res = simplifier.simplifyAll(tf, true);		
		assertEquals("<1><2/></1><3>The plan of</3><4><5/></4><b6/>happiness", fmt.setContent(tf).toString());
		assertNull(res);
	}
	
	@Test
	public void testCodeReduction56 () {		
		TextFragment tf = new TextFragment();
		tf.append("The plan of");
		tf.append(TagType.CLOSING, "b", "</b>");
				
		tf.append(TagType.OPENING, "c", "<c>");
		tf.append(TagType.PLACEHOLDER, "x5", "<x/>");
		tf.append(TagType.CLOSING, "c", "</c>");
		
		tf.append(TagType.OPENING, "d", "<d>");
		tf.append("happiness");
		
		assertEquals("The plan of<e4/><1><2/></1><b3/>happiness", fmt.setContent(tf).toString());
		TextFragment[] res = simplifier.simplifyAll(tf, true);		
		assertEquals("The plan of<e4/><1><2/></1><b3/>happiness", fmt.setContent(tf).toString());
		assertNull(res);
	}
	
	@Test
	public void testCodeWithGandXCodes() {		
		TextFragment tf = new TextFragment();			
		tf.append(TagType.OPENING, "g", "");
		tf.append("text");
		tf.append(TagType.PLACEHOLDER, "x", "");
		tf.append("\n");
		tf.append(TagType.CLOSING, "/g", "");
		List<Code> codes = tf.getCodes();
		Code first = codes.get(0);
		first.setOuterData("<g id=\"15-15\" ctype=\"x-html-td\">");
		Code second = codes.get(1);
		second.setOuterData("<x id=\"15-17\" ctype=\"lb\"/>");
		Code third = codes.get(2);
		third.setOuterData("</g>");
		
		assertEquals("<b1/>text<2/>\n<e3/>", fmt.setContent(tf).toString());
		TextFragment[] res = simplifier.simplifyAll(tf, true);		
		assertNull(res);
		assertEquals("<b1/>text<2/>\n<e3/>", fmt.setContent(tf).toString());
	}
}
