/*===========================================================================
  Copyright (C) 2009-2011 by the Okapi Framework contributors
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

package net.sf.okapi.filters.icml.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.MimeTypeMapper;
import net.sf.okapi.common.TestUtil;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.filters.FilterConfiguration;
import net.sf.okapi.common.filters.FilterTestDriver;
import net.sf.okapi.common.filters.InputDocument;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.filters.icml.ICMLFilter;
import net.sf.okapi.filters.icml.Parameters;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ICMLFilterTest {
	
	private ICMLFilter testee;
	private String root;
	private LocaleId locEN = LocaleId.fromString("en");

	@Before
	public void setUp() {
		testee = new ICMLFilter();
		root = TestUtil.getParentDir(this.getClass(), "/Test01.wcml");
	}
	
	@Test
	public void createSkeletonWriter_ThenReturnNull()
	{
		// Arrange
		
		// Act & Assert
		assertEquals(true, testee.createSkeletonWriter() == null);
	}
	
	@Test
	public void createFilterWriter_ThenReturnICMLFilterWriter()
	{
		// Arrange
		
		// Act
		IFilterWriter result = testee.createFilterWriter();
		
		// Assert
		assertNotNull(result);
		assertEquals("net.sf.okapi.filters.icml.ICMLFilterWriter", result.getClass().getName());
	}
	
	@Test
	public void getEncoderManager_ThenReturnEncoderManager()
	{
		// Arrange
		
		// Act
		EncoderManager result = testee.getEncoderManager();
		
		// Assert
		assertNotNull(result);
	}
	
	@Test
	public void getName_ThenReturnName()
	{
		// Arrange
		
		// Act
		String result = testee.getName();
		
		// Assert
		assertEquals("okf_icml", result);
	}
	
	@Test
	public void getDisplayName_ThenReturnDisplayName()
	{
		// Arrange
		
		// Act
		String result = testee.getDisplayName();
		
		// Assert
		assertEquals("ICML Filter", result);
	}
	
	@Test
	public void getMimeType_ThenReturnMimeType()
	{
		// Arrange
		
		// Act
		String result = testee.getMimeType();
		
		// Assert
		assertEquals(MimeTypeMapper.ICML_MIME_TYPE, result);
	}
	
	@Test
	public void getConfigurations_ThenReturnDefaultSettings()
	{
		// Arrange
		
		// Act
		List<FilterConfiguration> list = testee.getConfigurations();
		
		// Assert
		FilterConfiguration config = list.get(0);
		assertNotNull(config);
		assertEquals("okf_icml", config.configId);
		assertEquals(".wcml;.icml", config.extensions);
		assertEquals(MimeTypeMapper.ICML_MIME_TYPE, config.mimeType);
		assertEquals("ICML", config.name);
		assertEquals("Adobe InDesign ICML documents", config.description);
	}
	
	@Test
	public void getParameters_WhenParametersSet_ThenReturnParametersWithSettings()
	{
		// Arrange
		Parameters params = new Parameters();
		params.setExtractMasterSpreads(false);
		params.setExtractNotes(false);
		params.setNewTuOnBr(false);
		params.setSimplifyCodes(false);
		params.setSkipThreshold(1);
		testee.setParameters(params);
		
		// Act
		Parameters result = (Parameters) testee.getParameters();
		
		// Assert
		assertEquals(false, result.getExtractMasterSpreads());
		assertEquals(false, result.getExtractNotes());
		assertEquals(false, result.getNewTuOnBr());
		assertEquals(false, result.getSimplifyCodes());
		assertEquals(1, result.getSkipThreshold());
	}
	
	@Test
	public void getParameters_WhenNoParametersSet_ThenReturnParametersWithDefaultSettings()
	{
		// Arrange
		
		// Act
		Parameters result = (Parameters) testee.getParameters();
		
		// Assert
		assertEquals(true, result.getExtractMasterSpreads());
		assertEquals(false, result.getExtractNotes());
		assertEquals(false, result.getNewTuOnBr());
		assertEquals(true, result.getSimplifyCodes());
		assertEquals(1000, result.getSkipThreshold());
	}
	
	@Test
	public void toString_WhenMultipleContent_ThenExtractInTranslationUnit()
	{
		// Arrange
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(root+"Test01.wcml"), 2);
		String expectedStructure = (
				"<Content>Corporate Governance der Siegfried Gruppe</Content>" +
				"</CharacterStyleRange>" +
				"<CharacterStyleRange AppliedCharacterStyle=\"CharacterStyle/hochgestellt\">" +
					"<Content>und </Content>" +
				"</CharacterStyleRange>" +
				"<CharacterStyleRange AppliedCharacterStyle=\"CharacterStyle/$ID/[No character style]\">" +
					"<Content>das Schweizerische Obligationenrechts (OR)</Content>" +
					"<Br/>");
		
		// Act
		String structure = tu.toString();
		
		// Assert
		assertNotNull(tu);
		assertEquals(expectedStructure, structure);
	}
	
	@Test
	public void toString_WhenBreak_ThenTranslationUnitIsEmpty() 
	{
		// Arrange
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(root+"Test01.wcml"), 3); // Break
		String expected = "";
		
		// Act
		String structure = tu.toString();
		
		// Assert
		assertNotNull(tu);
		assertEquals(expected, structure);
	}
	
	@Test
	public void toString_WhenContentInTableCell_ThenSeparateTranslationUnit() 
	{
		// Arrange
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(root+"Test01.wcml"), 7); // first table cell content
		String expectedStructure = (
						"<CharacterStyleRange AppliedCharacterStyle=\"CharacterStyle/$ID/[No character style]\">" +
							"<Content>Name </Content>" +
						"</CharacterStyleRange>" +
						"<CharacterStyleRange AppliedCharacterStyle=\"CharacterStyle/$ID/[No character style]\">" +
							"<Content>Vorname</Content>" +
						"</CharacterStyleRange>" +
					"</ParagraphStyleRange>" +
				"</Cell>" +
				"<Cell AppliedCellStyle=\"CellStyle/$ID/[None]\" AppliedCellStylePriority=\"974\" BottomEdgeStrokeColor=\"Color/C=100 M=0 Y=0 K=0\" BottomEdgeStrokePriority=\"74\" BottomEdgeStrokeWeight=\"0\" BottomInset=\"3.1181102362204727\" ColumnSpan=\"1\" LeftEdgeStrokePriority=\"14\" LeftEdgeStrokeWeight=\"0\" LeftInset=\"0\" Name=\"1:0\" RightEdgeStrokePriority=\"14\" RightEdgeStrokeWeight=\"0\" RightInset=\"0.7086614173228347\" RowSpan=\"1\" Self=\"u1a1ei49e4i1\" TopEdgeStrokeColor=\"Color/C=100 M=0 Y=0 K=0\" TopEdgeStrokePriority=\"56\" TopEdgeStrokeWeight=\"0\" TopInset=\"3.1181102362204727\" VerticalJustification=\"BottomAlign\">" +
					"<ParagraphStyleRange AppliedParagraphStyle=\"ParagraphStyle/TableStyles%3aHEAD\">");
		
		// Act
		String structure = tu.toString();
		
		// Assert
		assertNotNull(tu);
		assertEquals(expectedStructure, structure);
	}
	
	@Test
	public void open_WhenSuccessfull_ThenReturnTrue () 
	{
		// Act & Assert
		assertTrue("Several problems, while ", FilterTestDriver.testStartDocument(testee,
			new InputDocument(root+"Test01.wcml", null),
			"UTF-8", locEN, locEN));
	}
	
	@SuppressWarnings("resource")
	private ArrayList<Event> getEvents (String path) 
	{
		ArrayList<Event> list = new ArrayList<Event>();
		RawDocument document = new RawDocument(new File(path).toURI(), "UTF-8", locEN);
		
		Parameters params = new Parameters();
		params.setExtractMasterSpreads(true);
		params.setExtractNotes(false);
		params.setNewTuOnBr(true);
		params.setSimplifyCodes(true);
		params.setSkipThreshold(1000);
		testee.setParameters(params);
		
		testee.open(document);
		
		while (testee.hasNext()) {
			Event event = testee.next();
			list.add(event);
		}
		
		testee.close();
		
		return list;
	}
}
