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

package net.sf.okapi.core.simplifierrules;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.TextFragment.TagType;

@RunWith(JUnit4.class)
public class SimplifierRulesEvaluateTest {
	private Code defaultCode;
	
	@Before
	public void setUp() {
		defaultCode = new Code();
		defaultCode.setAdded(true);
		defaultCode.setCloneable(true);
		defaultCode.setDeleteable(true);
		defaultCode.setData("test");
		defaultCode.setOuterData("test");
		defaultCode.setOriginalId("test");
		defaultCode.setType("test");
	}
	
	@Test
	public void flagRuleOnly() throws ParseException {
		SimplifierRules parser = new SimplifierRules("if ADDABLE or DELETABLE or CLONEABLE;", defaultCode);
		boolean r = parser.parse();
		assertTrue(r);
	}
	
	@Test
	public void flagRuleAndData() throws ParseException {
		SimplifierRules parser = new SimplifierRules("if ADDABLE and DELETABLE and CLONEABLE and DATA = \"test\";", defaultCode);
		boolean r = parser.parse();
		assertTrue(r);
	}
	
	@Test
	public void flagRuleAndDataWithParens() throws ParseException {
		SimplifierRules parser = new SimplifierRules("if (ADDABLE and DELETABLE and CLONEABLE) and (DATA = \"test\");", defaultCode);
		boolean r = parser.parse();
		assertTrue(r);
	}
	
	@Test
	public void allFields() throws ParseException {
		SimplifierRules parser = new SimplifierRules("if DATA = \"test\" and OUTER_DATA = \"test\" and ORIGINAL_ID = \"test\" and TYPE = \"test\";", defaultCode);
		parser.parse();
	}
	
	@Test
	public void allFieldsAndTagType() throws ParseException {
		SimplifierRules parser = new SimplifierRules("if (DATA = \"test\" and OUTER_DATA = \"test\" and ORIGINAL_ID = \"test\" and TYPE = \"test\") or (TAG_TYPE = OPENING);", defaultCode);		
		boolean r = parser.parse();
		assertTrue(r);
		
		Code code = new Code();
		code.setTagType(TagType.OPENING);
		parser = new SimplifierRules("if (DATA = \"test\" and OUTER_DATA = \"test\" and ORIGINAL_ID = \"test\" and TYPE = \"test\") or (TAG_TYPE = OPENING);", code);
		r = parser.parse();
		assertTrue(r);
	}
	
	@Test
	public void simpleRule() throws ParseException {
		SimplifierRules parser = new SimplifierRules("if ADDABLE;", defaultCode);
		boolean r = parser.parse();
		assertTrue(r);
	}
	
	@Test
	public void match() throws ParseException {
		SimplifierRules parser = new SimplifierRules("if DATA ~ \"x.*\";", defaultCode);
		boolean r = parser.parse();
		assertFalse(r);
	}
	
	@Test
	public void notMatch() throws ParseException {
		Code code = new Code();
		code.setData("test");
		SimplifierRules parser = new SimplifierRules("if DATA !~ \"t.*\";", code);
		boolean r = parser.parse();
		assertFalse(r);
	}
	
	@Test
	public void equals() throws ParseException {
		SimplifierRules parser = new SimplifierRules("if TYPE = \"test\";", defaultCode);
		boolean r = parser.parse();
		assertTrue(r);
	}
	
	@Test
	public void notEqual() throws ParseException {
		Code code = new Code();
		code.setType("not test");
		SimplifierRules parser = new SimplifierRules("if TYPE != \"test\";", code);
		boolean r = parser.parse();
		assertTrue(r);
	}
	
	@Test
	public void embeddedExpressions() throws ParseException {
		Code code = new Code();
		code.setType("not test");
		code.setData("one");
		SimplifierRules parser = new SimplifierRules("if TYPE != \"test\" and (DATA = \"one\" or (DATA = \"two\" and ORIGINAL_ID = \"test\"));", code);
		boolean r = parser.parse();
		assertTrue(r);		
		
		code = new Code();
		code.setType("not test");
		code.setData("two");
		code.setOriginalId("test");
		parser = new SimplifierRules("if TYPE != \"test\" and (DATA = \"one\" or (DATA = \"two\" and ORIGINAL_ID = \"test\"));", code);
		r = parser.parse();
		assertTrue(r);		
	}
	
	@Test
	public void manyRules() throws ParseException {
		SimplifierRules parser = new SimplifierRules("if TYPE != \"test\";"
				+ "\nif DATA ~ \"t.+\";", defaultCode);
		boolean r = parser.parse();
		assertTrue(r);
		
		parser = new SimplifierRules("if TYPE = \"not test\";"
				+ "\nif DATA !~ \"t.+\";", defaultCode);
		r = parser.parse();
		assertFalse(r);
	}
}
