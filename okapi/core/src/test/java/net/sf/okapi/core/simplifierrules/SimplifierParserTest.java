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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import net.sf.okapi.common.resource.Code;

@RunWith(JUnit4.class)
public class SimplifierParserTest {
	private Code code;
	
	@Before
	public void setUp() {
		code = new Code();
	}
	
	@Test
	public void flagRuleOnly() throws ParseException {
		SimplifierRules parser = new SimplifierRules("if ADDABLE or DELETABLE or CLONEABLE;", code);
		parser.parse();
	}
	
	@Test
	public void flagRuleAndData() throws ParseException {
		SimplifierRules parser = new SimplifierRules("if ADDABLE and DELETABLE and CLONEABLE and DATA = \"test\";", code);
		parser.parse();
	}
	
	@Test
	public void flagRuleAndDataWithParens() throws ParseException {
		SimplifierRules parser = new SimplifierRules("if (ADDABLE and DELETABLE and CLONEABLE) and (DATA = \"test\");", code);
		parser.parse();
	}
	
	@Test
	public void allFields() throws ParseException {
		SimplifierRules parser = new SimplifierRules("if DATA = \"test\" and OUTER_DATA = \"test\" and ORIGINAL_ID = \"test\" and TYPE = \"test\";", code);
		parser.parse();
	}
	
	@Test
	public void allFieldsAndTagType() throws ParseException {
		SimplifierRules parser = new SimplifierRules("if (DATA = \"test\" and OUTER_DATA = \"test\" and ORIGINAL_ID = \"test\" and TYPE = \"test\") or (TAG_TYPE = OPENING);", code);
		parser.parse();
	}
	
	@Test
	public void simpleRule() throws ParseException {
		SimplifierRules parser = new SimplifierRules("if ADDABLE;", code);
		parser.parse();
	}
	
	@Test
	public void match() throws ParseException {
		SimplifierRules parser = new SimplifierRules("if DATA ~ \"xtest\";", code);
		parser.parse();
	}
	
	@Test
	public void notMatch() throws ParseException {
		SimplifierRules parser = new SimplifierRules("if DATA !~ \"test\";", code);
		parser.parse();
	}
	
	@Test
	public void equals() throws ParseException {
		SimplifierRules parser = new SimplifierRules("if TYPE = \"test\";", code);
		parser.parse();
	}
	
	@Test
	public void notEqual() throws ParseException {
		SimplifierRules parser = new SimplifierRules("if TYPE != \"test\";", code);
		parser.parse();
	}
	
	@Test
	public void embeddedExpressions() throws ParseException {
		SimplifierRules parser = new SimplifierRules("if TYPE != \"test\" and (DATA = \"one\" or (DATA = \"two\" and DATA = \"three\"));", code);
		parser.parse();		
	}
	
	@Test
	public void comments() throws ParseException {
		SimplifierRules parser = new SimplifierRules("/* test\n line two */ #TAG != \"test\"\nif TYPE != \"test\";", code);
		parser.parse();
	}
	
	@Test
	public void manyRules() throws ParseException {
		SimplifierRules parser = new SimplifierRules("if TYPE != \"test\";"
				+ "\nif DATA ~ \"x\" or (ADDABLE);"
				+ "\nif ADDABLE and CLONEABLE and DELETABLE;", code);
		parser.parse();
	}
	
	@Test(expected=ParseException.class)
	public void emptyRule() throws ParseException {
		SimplifierRules parser = new SimplifierRules("if ();", code);
		parser.parse();
	}
	
	@Test(expected=ParseException.class)
	public void withoutSemiColon() throws ParseException {
		SimplifierRules parser = new SimplifierRules("if ADDABLE", code);
		parser.parse();
	}
	
	@Test(expected=ParseException.class)
	public void missingParen() throws ParseException {
		SimplifierRules parser = new SimplifierRules("if ADDABLE);", code);
		parser.parse();
	}
	
	@Test(expected=ParseException.class)
	public void extraParen() throws ParseException {
		SimplifierRules parser = new SimplifierRules("(if ADDABLE));", code);
		parser.parse();
	}
	
	@Test(expected=ParseException.class)
	public void mispelledField() throws ParseException {
		SimplifierRules parser = new SimplifierRules("(if DATAA = \"null\");", code);
		parser.parse();
	}
}
