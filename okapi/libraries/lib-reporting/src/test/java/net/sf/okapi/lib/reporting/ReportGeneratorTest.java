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

package net.sf.okapi.lib.reporting;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
@RunWith(JUnit4.class)
public class ReportGeneratorTest {

	@Test
	public void reportTest() {
		ReportGenerator gen = new ReportGenerator(this.getClass().getResourceAsStream("test_report.txt"));
		
		gen.setField("A1", "<a1>");
		gen.setField("A2", "<a2>");
		gen.setField("A3", "<a3>");
		gen.setField("A4", "<a4>");
		gen.setField("A5", "<a5>");
		gen.setField("A6", "<a6>");
		
		String report = gen.generate();
		assertEquals("Test report\n\n#Creation Date: <a1>\n#Project Name: <a2>\n#Target Locale: <a3>\n\n\n" +
				"Total,<a4>,<a5>,<a6>\n\nThe report was created on <a1>\n", report);
		
		report = gen.generate();
		assertEquals("Test report\n\n#Creation Date: <a1>\n#Project Name: <a2>\n#Target Locale: <a3>\n\n\n" +
				"Total,<a4>,<a5>,<a6>\n\nThe report was created on <a1>\n", report);
	}
	
	@Test
	public void tableReportTest() {
		ReportGenerator gen = new ReportGenerator(this.getClass().getResourceAsStream("test_table_report.txt"));
		
		gen.setField("A1", "<a1>");
		gen.setField("A2", "<a2>");
		gen.setField("A3", "<a3>");
		gen.setField("A4", "<a4>");
		gen.setField("A5", "<a5>");
		gen.setField("A6", "<a6>");
		
		gen.setField("A7", "<a7.1>");
		gen.setField("A8", "<a8.1>");
		gen.setField("A9", "<a9.1>");
		gen.setField("A10", "<a10.1>");
		
		gen.setField("A7", "<a7.2>");
		gen.setField("A8", "<a8.2>");
		gen.setField("A9", "<a9.2>");
		gen.setField("A10", "<a10.2>");
		
		gen.setField("A7", "<a7.3>");
		gen.setField("A8", "<a8.3>");
		gen.setField("A9", "<a9.3>");
		gen.setField("A10", "<a10.3>");
		
		gen.setField("A7", "<a7.4>");
		gen.setField("A8", "<a8.4>");
		gen.setField("A9", "<a9.4>");
		gen.setField("A10", "<a10.4>");
		
		gen.setField("A7", "<a7.5>");
		gen.setField("A8", "<a8.5>");
		gen.setField("A9", "<a9.5>");
		gen.setField("A10", "<a10.5>");
		
		String report = gen.generate();
		assertEquals("Test report\n\n#Creation Date: <a1>\n#Project Name: <a2>\n#Target Locale: <a3>\n\n" +
				"<a7.1>,<a8.1>,<a9.1>,<a10.1>,\n<a7.2>,<a8.2>,<a9.2>,<a10.2>,\n<a7.3>,<a8.3>,<a9.3>,<a10.3>,\n" +
				"<a7.4>,<a8.4>,<a9.4>,<a10.4>,\n<a7.5>,<a8.5>,<a9.5>,<a10.5>,\n\n" +
				"Total,<a4>,<a5>,<a6>\n\nThe report was created on <a1>\n", report);
		
		report = gen.generate();
		assertEquals("Test report\n\n#Creation Date: <a1>\n#Project Name: <a2>\n#Target Locale: <a3>\n\n" +
				"<a7.1>,<a8.1>,<a9.1>,<a10.1>,\n<a7.2>,<a8.2>,<a9.2>,<a10.2>,\n<a7.3>,<a8.3>,<a9.3>,<a10.3>,\n" +
				"<a7.4>,<a8.4>,<a9.4>,<a10.4>,\n<a7.5>,<a8.5>,<a9.5>,<a10.5>,\n\n" +
				"Total,<a4>,<a5>,<a6>\n\nThe report was created on <a1>\n", report);
	}
	
	@Test
	public void tableReportTest2() {
		ReportGenerator gen = new ReportGenerator(this.getClass().getResourceAsStream("test_table_report2.txt"));
		
		gen.setField("A1", "<a1>");
		gen.setField("A2", "<a2>");
		gen.setField("A3", "<a3>");
		gen.setField("A4", "<a4>");
		gen.setField("A5", "<a5>");
		gen.setField("A6", "<a6>");
		
		gen.setField("A7", "<a7.1>");
		gen.setField("A8", "<a8.1>");
		gen.setField("A9", "<a9.1>");
		gen.setField("A10", "<a10.1>");
		
		gen.setField("A7", "<a7.2>");
		gen.setField("A8", "<a8.2>");
		gen.setField("A9", "<a9.2>");
		gen.setField("A10", "<a10.2>");
		
		gen.setField("A7", "<a7.3>");
		gen.setField("A8", "<a8.3>");
		gen.setField("A9", "<a9.3>");
		gen.setField("A10", "<a10.3>");
		
		gen.setField("A7", "<a7.4>");
		gen.setField("A8", "<a8.4>");
		gen.setField("A9", "<a9.4>");
		gen.setField("A10", "<a10.4>");
		
		gen.setField("A7", "<a7.5>");
		gen.setField("A8", "<a8.5>");
		gen.setField("A9", "<a9.5>");
		gen.setField("A10", "<a10.5>");
		
		String report = gen.generate();
		assertEquals("Test report\n\n#Creation Date: <a1>\n#Project Name: <a2>\n#Target Locale: <a3>\n\n" +
				"<a7.1>\n<a7.2>\n<a7.3>\n" +
				"<a7.4>\n<a7.5>\n\n" +
				"Total,<a4>,<a5>,<a6>\n\nThe report was created on <a1>\n", report);
	}
	
	@Test
	public void tableReportTest3() {
		ReportGenerator gen = new ReportGenerator(this.getClass().getResourceAsStream("test_table_report2.txt"));
		
		gen.setField("A1", "<a1>");
		gen.setField("A2", "<a2>");
		gen.setField("A3", "<a3>");
		gen.setField("A4", "<a4>");
		gen.setField("A5", "<a5>");
		//gen.setField("A6", "<a6>");
		
		gen.setField("A7", "<a7.1>");
		gen.setField("A8", "<a8.1>");
		gen.setField("A9", "<a9.1>");
		gen.setField("A10", "<a10.1>");
		
		gen.setField("A7", "<a7.2>");
		gen.setField("A8", "<a8.2>");
		gen.setField("A9", "<a9.2>");
		gen.setField("A10", "<a10.2>");
		
		gen.setField("A7", "<a7.3>");
		gen.setField("A8", "<a8.3>");
		gen.setField("A9", "<a9.3>");
		gen.setField("A10", "<a10.3>");
		
		gen.setField("A7", "<a7.4>");
		gen.setField("A8", "<a8.4>");
		gen.setField("A9", "<a9.4>");
		gen.setField("A10", "<a10.4>");
		
		gen.setField("A7", "<a7.5>");
		gen.setField("A8", "<a8.5>");
		gen.setField("A9", "<a9.5>");
		gen.setField("A10", "<a10.5>");
		
		String report = gen.generate();
		assertEquals("Test report\n\n#Creation Date: <a1>\n#Project Name: <a2>\n#Target Locale: <a3>\n\n" +
				"<a7.1>\n<a7.2>\n<a7.3>\n" +
				"<a7.4>\n<a7.5>\n\n" +
				"Total,<a4>,<a5>,[?A6]\n\nThe report was created on <a1>\n", report);
	}
	
	@Test
	public void tableReportTest4() {
		ReportGenerator gen = new ReportGenerator(this.getClass().getResourceAsStream("test_table_report2.txt"));
		
		gen.setField("A1", "<a1>");
		gen.setField("A2", "<a2>");
		gen.setField("A3", "<a3>");
		gen.setField("A4", "<a4>");
		gen.setField("A5", "<a5>");
		gen.setField("A6", "<a6>");
		
//		gen.setField("A7", "<a7.1>");
//		gen.setField("A8", "<a8.1>");
//		gen.setField("A9", "<a9.1>");
//		gen.setField("A10", "<a10.1>");
//		
//		gen.setField("A7", "<a7.2>");
//		gen.setField("A8", "<a8.2>");
//		gen.setField("A9", "<a9.2>");
//		gen.setField("A10", "<a10.2>");
//		
//		gen.setField("A7", "<a7.3>");
//		gen.setField("A8", "<a8.3>");
//		gen.setField("A9", "<a9.3>");
//		gen.setField("A10", "<a10.3>");
//		
//		gen.setField("A7", "<a7.4>");
//		gen.setField("A8", "<a8.4>");
//		gen.setField("A9", "<a9.4>");
//		gen.setField("A10", "<a10.4>");
//		
//		gen.setField("A7", "<a7.5>");
//		gen.setField("A8", "<a8.5>");
//		gen.setField("A9", "<a9.5>");
//		gen.setField("A10", "<a10.5>");
		
		String report = gen.generate();
		assertEquals("Test report\n\n#Creation Date: <a1>\n#Project Name: <a2>\n#Target Locale: <a3>\n\n" +
				"{?[?A7]}\n\n" +
				"Total,<a4>,<a5>,<a6>\n\nThe report was created on <a1>\n", report);
	}
	
	@Test
	public void tableReportTest5() {
		ReportGenerator gen = new ReportGenerator(this.getClass().getResourceAsStream("scoping_report.html"));
		assertTrue(gen.isHtmlReport());
		assertTrue(gen.isMultiItemReport());
	}
	
	@Test
	public void tableReportTest6() {
		ReportGenerator gen = new ReportGenerator(this.getClass().getResourceAsStream("scoping_report2.html"));
		assertTrue(gen.isHtmlReport());
		assertFalse(gen.isMultiItemReport());
	}
	
	@Test
	public void tableReportTest7() {
		ReportGenerator gen = new ReportGenerator(this.getClass().getResourceAsStream("test_table_report3.txt"));
		assertFalse(gen.isHtmlReport());
		assertTrue(gen.isMultiItemReport());
	}
	
	@Test
	public void tableReportTest8() {
		ReportGenerator gen = new ReportGenerator(this.getClass().getResourceAsStream("test_table_report4.txt"));
		assertFalse(gen.isHtmlReport());
		assertFalse(gen.isMultiItemReport());
	}
	
	@Test
	public void tableReportTest9() {
		ReportGenerator gen = new ReportGenerator(this.getClass().getResourceAsStream("test_template1.txt"));
		assertFalse(gen.isHtmlReport());
		assertTrue(gen.isMultiItemReport());
		
		gen.setField("IMPORT", "import1");
		gen.setField("IMPORT", "import2");
		gen.setField("IMPORT", "import3");
		
		assertEquals("import1;\nimport2;\nimport3;", gen.generate());
	}
	
	@Test
	public void tableReportTest10() {
		ReportGenerator gen = new ReportGenerator(this.getClass().getResourceAsStream("test_template2.txt"));
		assertFalse(gen.isHtmlReport());
		assertTrue(gen.isMultiItemReport());
		
		gen.setField("IMPORT", "import1");
		gen.setField("IMPORT", "import2");
		gen.setField("IMPORT", "import3");
		
		assertEquals("   import import1;\n   import import2;\n   import import3;", gen.generate());
	}
	
	@Test
	public void tableReportTest11() {
		ReportGenerator gen = new ReportGenerator(this.getClass().getResourceAsStream("test_template3.txt"));
		assertFalse(gen.isHtmlReport());
		assertTrue(gen.isMultiItemReport());
		
		gen.setField("IMPORT", "import1");
		gen.setField("IMPORT", "import2");
		gen.setField("IMPORT", "import3");
		
		gen.setField("IMPORT_DESCR", "description 1");
		gen.setField("IMPORT_DESCR", "description 2");
		gen.setField("IMPORT_DESCR", "description 3");
		
		assertEquals("   import import1; // description 1\n   import import2; " +
				"// description 2\n   import import3; // description 3", 
				gen.generate());
	}
	
	@Test
	public void tableReportTest11_missing_field_value() {
		ReportGenerator gen = new ReportGenerator(this.getClass().getResourceAsStream("test_template3.txt"));
		assertFalse(gen.isHtmlReport());
		assertTrue(gen.isMultiItemReport());
		
		gen.setField("IMPORT", "import1");
		gen.setField("IMPORT", "import2");
		gen.setField("IMPORT", "import3");
		
		// IMPORT_DESCR values are missing
		
		assertEquals("   import import1; // [?IMPORT_DESCR]\n   import import2; " +
				"// [?IMPORT_DESCR]\n   import import3; // [?IMPORT_DESCR]", 
				gen.generate());
	}
	
	@Test
	public void tableReportTest11_missing_field_value2() {
		ReportGenerator gen = new ReportGenerator(this.getClass().getResourceAsStream("test_template3.txt"));
		assertFalse(gen.isHtmlReport());
		assertTrue(gen.isMultiItemReport());

		// IMPORT values are missing
		
		gen.setField("IMPORT_DESCR", "description 1");
		gen.setField("IMPORT_DESCR", "description 2");
		gen.setField("IMPORT_DESCR", "description 3");
		
		assertEquals("   import [?IMPORT]; // description 1\n   import [?IMPORT]; " +
				"// description 2\n   import [?IMPORT]; // description 3", 
				gen.generate());
	}
	
	@Test
	public void tableReportTest12() {
		ReportGenerator gen = new ReportGenerator(this.getClass().getResourceAsStream("test_template4.txt"));
		assertFalse(gen.isHtmlReport());
		assertTrue(gen.isMultiItemReport());
		
		gen.setField("IMPORT", "import1");
		gen.setField("IMPORT", "import2");
		gen.setField("IMPORT", "import3");
		
		gen.setField("IMPORT_DESCR", "description 1");
		gen.setField("IMPORT_DESCR", "description 2");
		gen.setField("IMPORT_DESCR", "description 3");
		
		assertEquals("     line 1\n  line 2\n   import import1; // description 1\n   import import2; " +
				"// description 2\n   import import3; // description 3", 
				gen.generate());
	}
}
