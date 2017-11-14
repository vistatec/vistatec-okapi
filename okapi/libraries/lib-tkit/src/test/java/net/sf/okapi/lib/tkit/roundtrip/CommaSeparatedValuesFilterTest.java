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
===========================================================================*/

package net.sf.okapi.lib.tkit.roundtrip;

import static net.sf.okapi.lib.tkit.roundtrip.RoundTripUtils.roundTripSerilaizedEvents;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.TestUtil;
import net.sf.okapi.common.filters.FilterTestDriver;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.filters.table.csv.CommaSeparatedValuesFilter;
import net.sf.okapi.filters.table.csv.Parameters;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class CommaSeparatedValuesFilterTest {

	private CommaSeparatedValuesFilter filter;
	private LocaleId locEN = LocaleId.fromString("en");
	private LocaleId locFR = LocaleId.fromString("fr");
	private LocaleId locFRCA = LocaleId.fromString("fr-ca");

	@Before
	public void setUp() {
		filter = new CommaSeparatedValuesFilter();
		assertNotNull(filter);
		Parameters params = (Parameters) filter.getParameters();
		setDefaults(params);
		RoundTripUtils.path = TestUtil.getParentDir(this.getClass(),
				"/dummy.txt") + "test_table.json";
	}

	@Test
	public void testCatkeys() throws URISyntaxException {
		Parameters params = (Parameters) filter.getParameters();
		// col1=source, col3=comment, col4=target
		params.load(getClass().getResource("/okf_table@catkeys.fprm"),
				false);
		String snippet = "1\tfrench\tinfo\t1234\n"
				+ "Source 1\tContext 1\tComment 1\tTarget 1\n"
				+ "Source 2\tContext 2\t\tTarget 2\n";

		List<Event> events = getEvents(snippet, locEN, locFR);
		events = roundTripSerilaizedEvents(events);
		ITextUnit tu = FilterTestDriver.getTextUnit(events, 1);
		assertNotNull(tu);
		assertEquals("Source 1", tu.getSource().toString());
		assertEquals("Target 1", tu.getTarget(locFR).toString());
		assertEquals("Comment 1", tu.getProperty(Property.NOTE).getValue());

		tu = FilterTestDriver.getTextUnit(events, 2);
		assertNotNull(tu);
		assertEquals("Source 2", tu.getSource().toString());
		assertTrue(null == tu.getProperty(Property.NOTE));
		assertEquals("Target 2", tu.getTarget(locFR).toString());
	}

	@Test
	public void testThreeColumnsSrcTrgData() {
		String snippet = "\"src\",\"trg\",data\n"
				+ "\"source1\",\"target1\",data1\n"
				+ "\"source2\",\"target2\",data2";

		// Set the parameters
		Parameters params = new Parameters();
		params.fieldDelimiter = ",";
		params.textQualifier = "\"";
		params.sendColumnsMode = Parameters.SEND_COLUMNS_LISTED;
		params.sourceColumns = "1";
		params.targetColumns = "2";
		params.targetLanguages = locFRCA.toString();
		params.targetSourceRefs = "1";
		filter.setParameters(params);

		String result = FilterTestDriver.generateOutput(
				roundTripSerilaizedEvents(getEvents(snippet, locEN, locFRCA)),
				filter.getEncoderManager(), locFRCA);
		assertEquals(snippet, result);
	}

	@Test
	public void testThreeColumnsSrcTrgData_2() {
		String snippet = "\"src\",\"trg\",data\n"
				+ "\"source1\",         \"target1\",data1\n"
				+ "\"source2\"    ,\"target2\",data2";

		// Set the parameters
		Parameters params = new Parameters();
		params.fieldDelimiter = ",";
		params.textQualifier = "\"";
		params.sendColumnsMode = Parameters.SEND_COLUMNS_LISTED;
		params.sourceColumns = "1";
		params.targetColumns = "2";
		params.targetLanguages = locFRCA.toString();
		params.targetSourceRefs = "1";
		filter.setParameters(params);

		String result = FilterTestDriver.generateOutput(
				roundTripSerilaizedEvents(getEvents(snippet, locEN, locFRCA)),
				filter.getEncoderManager(), locFRCA);
		assertEquals(snippet, result);
	}

	public static void setDefaults(
			net.sf.okapi.filters.table.base.Parameters params) {

		if (params == null)
			return;

		params.columnNamesLineNum = 1;
		params.valuesStartLineNum = 2;
		params.sourceIdSuffixes = "";
		params.sourceIdSourceRefs = "";
		params.targetColumns = "";
		params.targetSourceRefs = "";
		params.targetLanguages = "";
		params.commentColumns = "";
		params.commentSourceRefs = "";
		params.columnNamesLineNum = 1;
		params.valuesStartLineNum = 2;
		params.sendHeaderMode = Parameters.SEND_HEADER_COLUMN_NAMES_ONLY;
		// params.detectColumnsMode = Parameters.DETECT_COLUMNS_COL_NAMES;
		params.sendColumnsMode = Parameters.SEND_COLUMNS_ALL;
		params.sourceColumns = "";
		params.targetColumns = "";
		params.targetSourceRefs = "";
	}

	private ArrayList<Event> getEvents(String snippet, LocaleId srcLang,
			LocaleId trgLang) {
		return FilterTestDriver.getEvents(filter, snippet, srcLang, trgLang);
	}
}
