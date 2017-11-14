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

package net.sf.okapi.steps.scopingreport;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.ParametersString;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.uidescription.CheckListPart;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;
import net.sf.okapi.common.uidescription.PathInputPart;
import net.sf.okapi.lib.extra.AbstractParameters;

@EditorFor(Parameters.class)
public class Parameters extends AbstractParameters implements IEditorDescriptionProvider {

	private static final String PROJECT_NAME = "projectName";
	private static final String CUSTOM_TEMPLATE_URI = "customTemplateURI";
	private static final String CUSTOM_TEMPLATE_STRING = "customTemplateString";
	private static final String OUTPUT_PATH = "outputPath";
	private static final String EMPTY_URI = "";
	private static final String EMPTY_TEMPLATE = "";

	private static final String COUNT_AS_NONTRANSLATABLE_GMX_PROTECTED = "countAsNonTranslatable_GMXProtected";
	private static final String COUNT_AS_NONTRANSLATABLE_GMX_EXACT_MATCHED = "countAsNonTranslatable_GMXExactMatched";
	private static final String COUNT_AS_NONTRANSLATABLE_GMX_LEVERAGED_MATCHED = "countAsNonTranslatable_GMXLeveragedMatched";
	private static final String COUNT_AS_NONTRANSLATABLE_GMX_REPETITION_MATCHED = "countAsNonTranslatable_GMXRepetitionMatched";
	private static final String COUNT_AS_NONTRANSLATABLE_GMX_FUZZY_MATCHED = "countAsNonTranslatable_GMXFuzzyMatch";
	private static final String COUNT_AS_NONTRANSLATABLE_GMX_ALPHANUMERIC_ONLY_TEXT_UNIT = "countAsNonTranslatable_GMXAlphanumericOnlyTextUnit";
	private static final String COUNT_AS_NONTRANSLATABLE_GMX_NUMERIC_ONLY_TEXT_UNIT = "countAsNonTranslatable_GMXNumericOnlyTextUnit";
	private static final String COUNT_AS_NONTRANSLATABLE_GMX_MEASUREMENT_ONLY_TEXT_UNIT = "countAsNonTranslatable_GMXMeasurementOnlyTextUnit";
	
	private static final String COUNT_AS_NONTRANSLATABLE_EXACT_UNIQUE_ID = "countAsNonTranslatable_ExactUniqueIdMatch";
	private static final String COUNT_AS_NONTRANSLATABLE_EXACT_PREVIOUS_VERSION = "countAsNonTranslatable_ExactPreviousVersionMatch";
	private static final String COUNT_AS_NONTRANSLATABLE_EXACT_LOCAL_CONTEXT = "countAsNonTranslatable_ExactLocalContextMatch";
	private static final String COUNT_AS_NONTRANSLATABLE_EXACT_DOCUMENT_CONTEXT = "countAsNonTranslatable_ExactDocumentContextMatch";
	private static final String COUNT_AS_NONTRANSLATABLE_EXACT_STRUCTURAL = "countAsNonTranslatable_ExactStructuralMatch";
	private static final String COUNT_AS_NONTRANSLATABLE_EXACT = "countAsNonTranslatable_ExactMatch";
	private static final String COUNT_AS_NONTRANSLATABLE_EXACT_TEXT_ONLY_PREVIOUS_VERSION = "countAsNonTranslatable_ExactTextOnlyPreviousVersionMatch";
	private static final String COUNT_AS_NONTRANSLATABLE_EXACT_TEXT_ONLY_UNIQUE_ID = "countAsNonTranslatable_ExactTextOnlyUniqueIdMatch";
	private static final String COUNT_AS_NONTRANSLATABLE_EXACT_TEXT_ONLY = "countAsNonTranslatable_ExactTextOnly";
	private static final String COUNT_AS_NONTRANSLATABLE_EXACT_REPAIRED = "countAsNonTranslatable_ExactRepaired";
	private static final String COUNT_AS_NONTRANSLATABLE_FUZZY_PREVIOUS_VERSION = "countAsNonTranslatable_FuzzyPreviousVersionMatch";
	private static final String COUNT_AS_NONTRANSLATABLE_FUZZY_UNIQUE_ID = "countAsNonTranslatable_FuzzyUniqueIdMatch";
	private static final String COUNT_AS_NONTRANSLATABLE_FUZZY = "countAsNonTranslatable_FuzzyMatch";
	private static final String COUNT_AS_NONTRANSLATABLE_FUZZY_REPAIRED = "countAsNonTranslatable_FuzzyRepaired";
	private static final String COUNT_AS_NONTRANSLATABLE_PHRASE_ASSEMBLED = "countAsNonTranslatable_PhraseAssembled";
	private static final String COUNT_AS_NONTRANSLATABLE_MT = "countAsNonTranslatable_MT";
	private static final String COUNT_AS_NONTRANSLATABLE_CONCORDANCE = "countAsNonTranslatable_Concordance";

	private String projectName;
	private String outputPath;
	private String customTemplateURI;
	private String customTemplateString;
	
	private boolean countAsNonTranslatable_GMXProtected;
	private boolean countAsNonTranslatable_GMXExactMatched;
	private boolean countAsNonTranslatable_GMXLeveragedMatched;
	private boolean countAsNonTranslatable_GMXRepetitionMatched;
	private boolean countAsNonTranslatable_GMXFuzzyMatch;
	private boolean countAsNonTranslatable_GMXAlphanumericOnlyTextUnit;
	private boolean countAsNonTranslatable_GMXNumericOnlyTextUnit;
	private boolean countAsNonTranslatable_GMXMeasurementOnlyTextUnit;
	
	private boolean countAsNonTranslatable_ExactUniqueIdMatch;
	private boolean countAsNonTranslatable_ExactPreviousVersionMatch;
	private boolean countAsNonTranslatable_ExactLocalContextMatch;
	private boolean countAsNonTranslatable_ExactDocumentContextMatch;
	private boolean countAsNonTranslatable_ExactStructuralMatch;
	private boolean countAsNonTranslatable_ExactMatch;
	private boolean countAsNonTranslatable_ExactTextOnlyPreviousVersionMatch;
	private boolean countAsNonTranslatable_ExactTextOnlyUniqueIdMatch;
	private boolean countAsNonTranslatable_ExactTextOnly;
	private boolean countAsNonTranslatable_ExactRepaired;
	private boolean countAsNonTranslatable_FuzzyPreviousVersionMatch;
	private boolean countAsNonTranslatable_FuzzyUniqueIdMatch;
	private boolean countAsNonTranslatable_FuzzyMatch;
	private boolean countAsNonTranslatable_FuzzyRepaired;
	private boolean countAsNonTranslatable_PhraseAssembled;
	private boolean countAsNonTranslatable_MT;
	private boolean countAsNonTranslatable_Concordance;
	
	public String getCustomTemplateURI() {
		return customTemplateURI;
	}

	public void setCustomTemplateURI(String customTemplateURI) {
		this.customTemplateURI = customTemplateURI;
	}
	
	public String getCustomTemplateString() {
		return customTemplateString;
	}
	
	public void setCustomTemplateString(String customTemplateString) {
		this.customTemplateString = customTemplateString;
	}

	@Override
	protected void parameters_load(ParametersString buffer) {
		parameters_reset();
		projectName = buffer.getString(PROJECT_NAME, projectName);
		//customTemplateURI = Util.toURI(buffer.getString(CUSTOM_TEMPLATE_URI, EMPTY_URI));
		customTemplateURI = buffer.getString(CUSTOM_TEMPLATE_URI, customTemplateURI);
		customTemplateString = buffer.getString(CUSTOM_TEMPLATE_STRING, customTemplateString);
		outputPath = buffer.getString(OUTPUT_PATH, outputPath);
		
		countAsNonTranslatable_GMXProtected = buffer.getBoolean(COUNT_AS_NONTRANSLATABLE_GMX_PROTECTED, countAsNonTranslatable_GMXProtected);
		countAsNonTranslatable_GMXExactMatched = buffer.getBoolean(COUNT_AS_NONTRANSLATABLE_GMX_EXACT_MATCHED, countAsNonTranslatable_GMXExactMatched);
		countAsNonTranslatable_GMXLeveragedMatched = buffer.getBoolean(COUNT_AS_NONTRANSLATABLE_GMX_LEVERAGED_MATCHED, countAsNonTranslatable_GMXLeveragedMatched);
		countAsNonTranslatable_GMXRepetitionMatched = buffer.getBoolean(COUNT_AS_NONTRANSLATABLE_GMX_REPETITION_MATCHED, countAsNonTranslatable_GMXRepetitionMatched);
		countAsNonTranslatable_GMXFuzzyMatch = buffer.getBoolean(COUNT_AS_NONTRANSLATABLE_GMX_FUZZY_MATCHED, countAsNonTranslatable_GMXFuzzyMatch);
		countAsNonTranslatable_GMXAlphanumericOnlyTextUnit = buffer.getBoolean(COUNT_AS_NONTRANSLATABLE_GMX_ALPHANUMERIC_ONLY_TEXT_UNIT, countAsNonTranslatable_GMXAlphanumericOnlyTextUnit);
		countAsNonTranslatable_GMXNumericOnlyTextUnit = buffer.getBoolean(COUNT_AS_NONTRANSLATABLE_GMX_NUMERIC_ONLY_TEXT_UNIT, countAsNonTranslatable_GMXNumericOnlyTextUnit);
		countAsNonTranslatable_GMXMeasurementOnlyTextUnit = buffer.getBoolean(COUNT_AS_NONTRANSLATABLE_GMX_MEASUREMENT_ONLY_TEXT_UNIT, countAsNonTranslatable_GMXMeasurementOnlyTextUnit);
		
		countAsNonTranslatable_ExactUniqueIdMatch = buffer.getBoolean(COUNT_AS_NONTRANSLATABLE_EXACT_UNIQUE_ID, countAsNonTranslatable_ExactUniqueIdMatch);
		countAsNonTranslatable_ExactPreviousVersionMatch = buffer.getBoolean(COUNT_AS_NONTRANSLATABLE_EXACT_PREVIOUS_VERSION, countAsNonTranslatable_ExactPreviousVersionMatch);
		countAsNonTranslatable_ExactLocalContextMatch = buffer.getBoolean(COUNT_AS_NONTRANSLATABLE_EXACT_LOCAL_CONTEXT, countAsNonTranslatable_ExactLocalContextMatch);
		countAsNonTranslatable_ExactDocumentContextMatch = buffer.getBoolean(COUNT_AS_NONTRANSLATABLE_EXACT_DOCUMENT_CONTEXT, countAsNonTranslatable_ExactDocumentContextMatch);
		countAsNonTranslatable_ExactStructuralMatch = buffer.getBoolean(COUNT_AS_NONTRANSLATABLE_EXACT_STRUCTURAL, countAsNonTranslatable_ExactStructuralMatch);
		countAsNonTranslatable_ExactMatch = buffer.getBoolean(COUNT_AS_NONTRANSLATABLE_EXACT, countAsNonTranslatable_ExactMatch);
		countAsNonTranslatable_ExactTextOnlyPreviousVersionMatch = buffer.getBoolean(COUNT_AS_NONTRANSLATABLE_EXACT_TEXT_ONLY_PREVIOUS_VERSION, countAsNonTranslatable_ExactTextOnlyPreviousVersionMatch);
		countAsNonTranslatable_ExactTextOnlyUniqueIdMatch = buffer.getBoolean(COUNT_AS_NONTRANSLATABLE_EXACT_TEXT_ONLY_UNIQUE_ID, countAsNonTranslatable_ExactTextOnlyUniqueIdMatch);
		countAsNonTranslatable_ExactTextOnly = buffer.getBoolean(COUNT_AS_NONTRANSLATABLE_EXACT_TEXT_ONLY, countAsNonTranslatable_ExactTextOnly);
		countAsNonTranslatable_ExactRepaired = buffer.getBoolean(COUNT_AS_NONTRANSLATABLE_EXACT_REPAIRED, countAsNonTranslatable_ExactRepaired);
		countAsNonTranslatable_FuzzyPreviousVersionMatch = buffer.getBoolean(COUNT_AS_NONTRANSLATABLE_FUZZY_PREVIOUS_VERSION, countAsNonTranslatable_FuzzyPreviousVersionMatch);
		countAsNonTranslatable_FuzzyUniqueIdMatch = buffer.getBoolean(COUNT_AS_NONTRANSLATABLE_FUZZY_UNIQUE_ID, countAsNonTranslatable_FuzzyUniqueIdMatch);
		countAsNonTranslatable_FuzzyMatch = buffer.getBoolean(COUNT_AS_NONTRANSLATABLE_FUZZY, countAsNonTranslatable_FuzzyMatch);
		countAsNonTranslatable_FuzzyRepaired = buffer.getBoolean(COUNT_AS_NONTRANSLATABLE_FUZZY_REPAIRED, countAsNonTranslatable_FuzzyRepaired);
		countAsNonTranslatable_PhraseAssembled = buffer.getBoolean(COUNT_AS_NONTRANSLATABLE_PHRASE_ASSEMBLED, countAsNonTranslatable_PhraseAssembled);
		countAsNonTranslatable_MT = buffer.getBoolean(COUNT_AS_NONTRANSLATABLE_MT, countAsNonTranslatable_MT);
		countAsNonTranslatable_Concordance = buffer.getBoolean(COUNT_AS_NONTRANSLATABLE_CONCORDANCE, countAsNonTranslatable_Concordance);
	}

	@Override
	protected void parameters_reset() {
		// Default values
		projectName = "My Project";
//		try {
//			customTemplateURI = new URI(EMPTY_URI); 
//		} catch (URISyntaxException e) {
//			new OkapiException(e);
//		}
		customTemplateURI = EMPTY_URI;
		customTemplateString = EMPTY_TEMPLATE;
		outputPath = Util.ROOT_DIRECTORY_VAR+"/scoping_report.html";
		
		countAsNonTranslatable_GMXProtected = true;
		countAsNonTranslatable_GMXExactMatched = true;
		countAsNonTranslatable_GMXLeveragedMatched = false;
		countAsNonTranslatable_GMXRepetitionMatched = false;
		countAsNonTranslatable_GMXFuzzyMatch = false;
		countAsNonTranslatable_GMXAlphanumericOnlyTextUnit = true;
		countAsNonTranslatable_GMXNumericOnlyTextUnit = true;
		countAsNonTranslatable_GMXMeasurementOnlyTextUnit = true;
		
		countAsNonTranslatable_ExactUniqueIdMatch = true;
		countAsNonTranslatable_ExactPreviousVersionMatch = true;
		countAsNonTranslatable_ExactLocalContextMatch = false;
		countAsNonTranslatable_ExactDocumentContextMatch = false;
		countAsNonTranslatable_ExactStructuralMatch = false;
		countAsNonTranslatable_ExactMatch = false;
		countAsNonTranslatable_ExactTextOnlyPreviousVersionMatch = false;
		countAsNonTranslatable_ExactTextOnlyUniqueIdMatch = false;
		countAsNonTranslatable_ExactTextOnly = false;
		countAsNonTranslatable_ExactRepaired = false;
		countAsNonTranslatable_FuzzyPreviousVersionMatch = false;
		countAsNonTranslatable_FuzzyUniqueIdMatch = false;
		countAsNonTranslatable_FuzzyMatch = false;
		countAsNonTranslatable_FuzzyRepaired = false;
		countAsNonTranslatable_PhraseAssembled = false;
		countAsNonTranslatable_MT = false;
		countAsNonTranslatable_Concordance = false;
	}

	@Override
	protected void parameters_save(ParametersString buffer) {
		buffer.setString(PROJECT_NAME, projectName);
		buffer.setString(CUSTOM_TEMPLATE_URI, customTemplateURI == null ? EMPTY_URI : customTemplateURI.toString());
		buffer.setString(CUSTOM_TEMPLATE_STRING, customTemplateString == null ? EMPTY_TEMPLATE : customTemplateString);
		buffer.setString(OUTPUT_PATH, outputPath);
		
		buffer.setBoolean(COUNT_AS_NONTRANSLATABLE_GMX_PROTECTED, countAsNonTranslatable_GMXProtected);
		buffer.setBoolean(COUNT_AS_NONTRANSLATABLE_GMX_EXACT_MATCHED, countAsNonTranslatable_GMXExactMatched);
		buffer.setBoolean(COUNT_AS_NONTRANSLATABLE_GMX_LEVERAGED_MATCHED, countAsNonTranslatable_GMXLeveragedMatched);
		buffer.setBoolean(COUNT_AS_NONTRANSLATABLE_GMX_REPETITION_MATCHED, countAsNonTranslatable_GMXRepetitionMatched);
		buffer.setBoolean(COUNT_AS_NONTRANSLATABLE_GMX_FUZZY_MATCHED, countAsNonTranslatable_GMXFuzzyMatch);
		buffer.setBoolean(COUNT_AS_NONTRANSLATABLE_GMX_ALPHANUMERIC_ONLY_TEXT_UNIT, countAsNonTranslatable_GMXAlphanumericOnlyTextUnit);
		buffer.setBoolean(COUNT_AS_NONTRANSLATABLE_GMX_NUMERIC_ONLY_TEXT_UNIT, countAsNonTranslatable_GMXNumericOnlyTextUnit);
		buffer.setBoolean(COUNT_AS_NONTRANSLATABLE_GMX_MEASUREMENT_ONLY_TEXT_UNIT, countAsNonTranslatable_GMXMeasurementOnlyTextUnit);
		
		buffer.setBoolean(COUNT_AS_NONTRANSLATABLE_EXACT_UNIQUE_ID, countAsNonTranslatable_ExactUniqueIdMatch);
		buffer.setBoolean(COUNT_AS_NONTRANSLATABLE_EXACT_PREVIOUS_VERSION, countAsNonTranslatable_ExactPreviousVersionMatch);
		buffer.setBoolean(COUNT_AS_NONTRANSLATABLE_EXACT_LOCAL_CONTEXT, countAsNonTranslatable_ExactLocalContextMatch);
		buffer.setBoolean(COUNT_AS_NONTRANSLATABLE_EXACT_DOCUMENT_CONTEXT, countAsNonTranslatable_ExactDocumentContextMatch);
		buffer.setBoolean(COUNT_AS_NONTRANSLATABLE_EXACT_STRUCTURAL, countAsNonTranslatable_ExactStructuralMatch);
		buffer.setBoolean(COUNT_AS_NONTRANSLATABLE_EXACT, countAsNonTranslatable_ExactMatch);
		buffer.setBoolean(COUNT_AS_NONTRANSLATABLE_EXACT_TEXT_ONLY_PREVIOUS_VERSION, countAsNonTranslatable_ExactTextOnlyPreviousVersionMatch);
		buffer.setBoolean(COUNT_AS_NONTRANSLATABLE_EXACT_TEXT_ONLY_UNIQUE_ID, countAsNonTranslatable_ExactTextOnlyUniqueIdMatch);
		buffer.setBoolean(COUNT_AS_NONTRANSLATABLE_EXACT_TEXT_ONLY, countAsNonTranslatable_ExactTextOnly);
		buffer.setBoolean(COUNT_AS_NONTRANSLATABLE_EXACT_REPAIRED, countAsNonTranslatable_ExactRepaired);
		buffer.setBoolean(COUNT_AS_NONTRANSLATABLE_FUZZY_PREVIOUS_VERSION, countAsNonTranslatable_FuzzyPreviousVersionMatch);
		buffer.setBoolean(COUNT_AS_NONTRANSLATABLE_FUZZY_UNIQUE_ID, countAsNonTranslatable_FuzzyUniqueIdMatch);
		buffer.setBoolean(COUNT_AS_NONTRANSLATABLE_FUZZY, countAsNonTranslatable_FuzzyMatch);
		buffer.setBoolean(COUNT_AS_NONTRANSLATABLE_FUZZY_REPAIRED, countAsNonTranslatable_FuzzyRepaired);
		buffer.setBoolean(COUNT_AS_NONTRANSLATABLE_PHRASE_ASSEMBLED, countAsNonTranslatable_PhraseAssembled);
		buffer.setBoolean(COUNT_AS_NONTRANSLATABLE_MT, countAsNonTranslatable_MT);
		buffer.setBoolean(COUNT_AS_NONTRANSLATABLE_CONCORDANCE, countAsNonTranslatable_Concordance);
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getOutputPath () {
		return outputPath;
	}

	public void setOutputPath (String outputPath) {
		this.outputPath = outputPath;
	}

	@Override
	public ParametersDescription getParametersDescription () {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(PROJECT_NAME,
			"Name of the project", "Name of the project to be displayed in the report");
		desc.add(CUSTOM_TEMPLATE_URI,
				"Custom template URI:", "URI of the report template");
		desc.add(CUSTOM_TEMPLATE_STRING,
				"Custom template string:", "String for directly specifying the report template");
		desc.add(OUTPUT_PATH,
			"Output path:", "Full path of the report to generate");
		
		desc.add(COUNT_AS_NONTRANSLATABLE_GMX_PROTECTED,
				"GMX Protected Count",
				"Count as non-translatable the words/characters in PROJECT_GMX_PROTECTED_WORD_COUNT and ITEM_GMX_PROTECTED_WORD_COUNT categories");
		desc.add(COUNT_AS_NONTRANSLATABLE_GMX_EXACT_MATCHED,
				"GMX Exact Matched Count",
				"Count as non-translatable the words/characters in PROJECT_GMX_EXACT_MATCHED_WORD_COUNT and ITEM_GMX_EXACT_MATCHED_WORD_COUNT categories");
		desc.add(COUNT_AS_NONTRANSLATABLE_GMX_LEVERAGED_MATCHED,
				"GMX Leveraged Matched Count",
				"Count as non-translatable the words/characters in PROJECT_GMX_LEVERAGED_MATCHED_WORD_COUNT and ITEM_GMX_LEVERAGED_MATCHED_WORD_COUNT categories");
		desc.add(COUNT_AS_NONTRANSLATABLE_GMX_REPETITION_MATCHED,
				"GMX Repetition Matched Count",
				"Count as non-translatable the words/characters in PROJECT_GMX_REPETITION_MATCHED_WORD_COUNT and ITEM_GMX_REPETITION_MATCHED_WORD_COUNT categories");
		desc.add(COUNT_AS_NONTRANSLATABLE_GMX_FUZZY_MATCHED,
				"GMX Fuzzy Matched Count",
				"Count as non-translatable the words/characters in PROJECT_GMX_FUZZY_MATCHED_WORD_COUNT and ITEM_GMX_FUZZY_MATCHED_WORD_COUNT categories");
		desc.add(COUNT_AS_NONTRANSLATABLE_GMX_ALPHANUMERIC_ONLY_TEXT_UNIT,
				"GMX Alphanumeric Only Text Unit Count",
				"Count as non-translatable the words/characters in PROJECT_GMX_ALPHANUMERIC_ONLY_TEXT_UNIT_WORD_COUNT and ITEM_GMX_ALPHANUMERIC_ONLY_TEXT_UNIT_WORD_COUNT categories");
		desc.add(COUNT_AS_NONTRANSLATABLE_GMX_NUMERIC_ONLY_TEXT_UNIT,
				"GMX Numeric Only Text Unit Count",
				"Count as non-translatable the words/characters in PROJECT_GMX_NUMERIC_ONLY_TEXT_UNIT_WORD_COUNT and ITEM_GMX_NUMERIC_ONLY_TEXT_UNIT_WORD_COUNT categories");
		desc.add(COUNT_AS_NONTRANSLATABLE_GMX_MEASUREMENT_ONLY_TEXT_UNIT,
				"GMX Measurement Only Text Unit Count",
				"Count as non-translatable the words/characters in PROJECT_GMX_MEASUREMENT_ONLY_TEXT_UNIT_WORD_COUNT and ITEM_GMX_MEASUREMENT_ONLY_TEXT_UNIT_WORD_COUNT categories");
		
		desc.add(COUNT_AS_NONTRANSLATABLE_EXACT_UNIQUE_ID,
				"Exact Unique Id Match",
				"Count as non-translatable the words in PROJECT_EXACT_UNIQUE_ID and ITEM_EXACT_UNIQUE_ID categories");
		desc.add(COUNT_AS_NONTRANSLATABLE_EXACT_PREVIOUS_VERSION,
				"Exact Previous Version Match",
				"Count as non-translatable the words in PROJECT_EXACT_PREVIOUS_VERSION and ITEM_EXACT_PREVIOUS_VERSION categories");
		desc.add(COUNT_AS_NONTRANSLATABLE_EXACT_LOCAL_CONTEXT,
				"Exact Local Context Match",
				"Count as non-translatable the words in PROJECT_EXACT_LOCAL_CONTEXT and ITEM_EXACT_LOCAL_CONTEXT categories");
		desc.add(COUNT_AS_NONTRANSLATABLE_EXACT_DOCUMENT_CONTEXT,
				"Exact Document Context Match",
				"Count as non-translatable the words in PROJECT_EXACT_DOCUMENT_CONTEXT and ITEM_EXACT_DOCUMENT_CONTEXT categories");
		desc.add(COUNT_AS_NONTRANSLATABLE_EXACT_STRUCTURAL,
				"Exact Structural Match",
				"Count as non-translatable the words in PROJECT_EXACT_STRUCTURAL and ITEM_EXACT_STRUCTURAL categories");
		desc.add(COUNT_AS_NONTRANSLATABLE_EXACT,
				"Exact Match",
				"Count as non-translatable the words in PROJECT_EXACT and ITEM_EXACT categories");
		desc.add(COUNT_AS_NONTRANSLATABLE_EXACT_TEXT_ONLY_PREVIOUS_VERSION,
				"Exact Text Only Previous Version Match",
				"Count as non-translatable the words in PROJECT_EXACT_TEXT_ONLY_PREVIOUS_VERSION and ITEM_EXACT_TEXT_ONLY_PREVIOUS_VERSION categories");
		desc.add(COUNT_AS_NONTRANSLATABLE_EXACT_TEXT_ONLY_UNIQUE_ID,
				"Exact Text Only Unique Id Match",
				"Count as non-translatable the words in PROJECT_EXACT_TEXT_ONLY_UNIQUE_ID and ITEM_EXACT_TEXT_ONLY_UNIQUE_ID categories");
		desc.add(COUNT_AS_NONTRANSLATABLE_EXACT_TEXT_ONLY,
				"Exact Text Only",
				"Count as non-translatable the words in PROJECT_EXACT_TEXT_ONLY and ITEM_EXACT_TEXT_ONLY categories");
		desc.add(COUNT_AS_NONTRANSLATABLE_EXACT_REPAIRED,
				"Exact Repaired",
				"Count as non-translatable the words in PROJECT_EXACT_REPAIRED and ITEM_EXACT_REPAIRED categories");
		desc.add(COUNT_AS_NONTRANSLATABLE_FUZZY_PREVIOUS_VERSION,
				"Fuzzy Previous Version Match",
				"Count as non-translatable the words in PROJECT_FUZZY_PREVIOUS_VERSION and ITEM_FUZZY_PREVIOUS_VERSION categories");
		desc.add(COUNT_AS_NONTRANSLATABLE_FUZZY_UNIQUE_ID,
				"Fuzzy Unique Id Match",
				"Count as non-translatable the words in PROJECT_FUZZY_UNIQUE_ID and ITEM_FUZZY_UNIQUE_ID categories");
		desc.add(COUNT_AS_NONTRANSLATABLE_FUZZY,
				"Fuzzy Match",
				"Count as non-translatable the words in PROJECT_FUZZY and ITEM_FUZZY categories");
		desc.add(COUNT_AS_NONTRANSLATABLE_FUZZY_REPAIRED,
				"Fuzzy Repaired",
				"Count as non-translatable the words in PROJECT_FUZZY_REPAIRED and ITEM_FUZZY_REPAIRED categories");
		desc.add(COUNT_AS_NONTRANSLATABLE_PHRASE_ASSEMBLED,
				"Phrase Assembled",
				"Count as non-translatable the words in PROJECT_PHRASE_ASSEMBLED and ITEM_PHRASE_ASSEMBLED categories");
		desc.add(COUNT_AS_NONTRANSLATABLE_MT,
				"MT",
				"Count as non-translatable the words in PROJECT_MT and ITEM_MT categories");
		desc.add(COUNT_AS_NONTRANSLATABLE_CONCORDANCE,
				"Concordance",
				"Count as non-translatable the words in PROJECT_CONCORDANCE and ITEM_CONCORDANCE categories");
		
		return desc;
	}

	@Override
	public EditorDescription createEditorDescription (ParametersDescription paramsDesc) {
		EditorDescription desc = new EditorDescription("Scope Reporting", true, false);
		
		desc.addTextInputPart(paramsDesc.get(PROJECT_NAME));
		
		PathInputPart ctpip = desc.addPathInputPart(paramsDesc.get(CUSTOM_TEMPLATE_URI),
				"Custon Template", false);
		ctpip.setAllowEmpty(true);
		ctpip.setBrowseFilters("HTML Files (*.htm;*.html)\tAll Files (*.*)", "*.htm;*.html\t*.*");
		
		PathInputPart pip = desc.addPathInputPart(paramsDesc.get(OUTPUT_PATH),
			"Report to Generate", true);
		pip.setBrowseFilters("HTML Files (*.htm;*.html)\tAll Files (*.*)", "*.htm;*.html\t*.*");

//		desc.addSeparatorPart();
		CheckListPart clp = desc.addCheckListPart("GMX categories to count as non-translatable", 90);
		clp.addEntry(paramsDesc.get(COUNT_AS_NONTRANSLATABLE_GMX_PROTECTED));
		clp.addEntry(paramsDesc.get(COUNT_AS_NONTRANSLATABLE_GMX_EXACT_MATCHED));
		clp.addEntry(paramsDesc.get(COUNT_AS_NONTRANSLATABLE_GMX_LEVERAGED_MATCHED));
		clp.addEntry(paramsDesc.get(COUNT_AS_NONTRANSLATABLE_GMX_REPETITION_MATCHED));
		clp.addEntry(paramsDesc.get(COUNT_AS_NONTRANSLATABLE_GMX_FUZZY_MATCHED));
		clp.addEntry(paramsDesc.get(COUNT_AS_NONTRANSLATABLE_GMX_ALPHANUMERIC_ONLY_TEXT_UNIT));
		clp.addEntry(paramsDesc.get(COUNT_AS_NONTRANSLATABLE_GMX_NUMERIC_ONLY_TEXT_UNIT));
		clp.addEntry(paramsDesc.get(COUNT_AS_NONTRANSLATABLE_GMX_MEASUREMENT_ONLY_TEXT_UNIT));
		
//		desc.addCheckboxPart(paramsDesc.get(COUNT_AS_NONTRANSLATABLE_GMX_PROTECTED));
//		desc.addCheckboxPart(paramsDesc.get(COUNT_AS_NONTRANSLATABLE_GMX_EXACT_MATCHED));
//		desc.addCheckboxPart(paramsDesc.get(COUNT_AS_NONTRANSLATABLE_GMX_LEVERAGED_MATCHED));
//		desc.addCheckboxPart(paramsDesc.get(COUNT_AS_NONTRANSLATABLE_GMX_REPETITION_MATCHED));
//		desc.addCheckboxPart(paramsDesc.get(COUNT_AS_NONTRANSLATABLE_GMX_FUZZY_MATCHED));
//		desc.addCheckboxPart(paramsDesc.get(COUNT_AS_NONTRANSLATABLE_GMX_ALPHANUMERIC_ONLY_TEXT_UNIT));
//		desc.addCheckboxPart(paramsDesc.get(COUNT_AS_NONTRANSLATABLE_GMX_NUMERIC_ONLY_TEXT_UNIT));
//		desc.addCheckboxPart(paramsDesc.get(COUNT_AS_NONTRANSLATABLE_GMX_MEASUREMENT_ONLY_TEXT_UNIT));
		
//		desc.addSeparatorPart();
		clp = desc.addCheckListPart("Okapi categories to count as non-translatable", 120);
		clp.addEntry(paramsDesc.get(COUNT_AS_NONTRANSLATABLE_EXACT_UNIQUE_ID));
		clp.addEntry(paramsDesc.get(COUNT_AS_NONTRANSLATABLE_EXACT_PREVIOUS_VERSION));
		clp.addEntry(paramsDesc.get(COUNT_AS_NONTRANSLATABLE_EXACT_LOCAL_CONTEXT));
		clp.addEntry(paramsDesc.get(COUNT_AS_NONTRANSLATABLE_EXACT_DOCUMENT_CONTEXT));
		clp.addEntry(paramsDesc.get(COUNT_AS_NONTRANSLATABLE_EXACT_STRUCTURAL));
		clp.addEntry(paramsDesc.get(COUNT_AS_NONTRANSLATABLE_EXACT));
		clp.addEntry(paramsDesc.get(COUNT_AS_NONTRANSLATABLE_EXACT_TEXT_ONLY_PREVIOUS_VERSION));
		clp.addEntry(paramsDesc.get(COUNT_AS_NONTRANSLATABLE_EXACT_TEXT_ONLY_UNIQUE_ID));
		clp.addEntry(paramsDesc.get(COUNT_AS_NONTRANSLATABLE_EXACT_TEXT_ONLY));
		clp.addEntry(paramsDesc.get(COUNT_AS_NONTRANSLATABLE_EXACT_REPAIRED));
		clp.addEntry(paramsDesc.get(COUNT_AS_NONTRANSLATABLE_FUZZY_PREVIOUS_VERSION));
		clp.addEntry(paramsDesc.get(COUNT_AS_NONTRANSLATABLE_FUZZY_UNIQUE_ID));
		clp.addEntry(paramsDesc.get(COUNT_AS_NONTRANSLATABLE_FUZZY));
		clp.addEntry(paramsDesc.get(COUNT_AS_NONTRANSLATABLE_FUZZY_REPAIRED));
		clp.addEntry(paramsDesc.get(COUNT_AS_NONTRANSLATABLE_PHRASE_ASSEMBLED));
		clp.addEntry(paramsDesc.get(COUNT_AS_NONTRANSLATABLE_MT));
		clp.addEntry(paramsDesc.get(COUNT_AS_NONTRANSLATABLE_CONCORDANCE));
		
//		desc.addCheckboxPart(paramsDesc.get(COUNT_AS_NONTRANSLATABLE_EXACT_UNIQUE_ID));
//		desc.addCheckboxPart(paramsDesc.get(COUNT_AS_NONTRANSLATABLE_EXACT_PREVIOUS_VERSION));
//		desc.addCheckboxPart(paramsDesc.get(COUNT_AS_NONTRANSLATABLE_EXACT_LOCAL_CONTEXT));
//		desc.addCheckboxPart(paramsDesc.get(COUNT_AS_NONTRANSLATABLE_EXACT_DOCUMENT_CONTEXT));
//		desc.addCheckboxPart(paramsDesc.get(COUNT_AS_NONTRANSLATABLE_EXACT_STRUCTURAL));
//		desc.addCheckboxPart(paramsDesc.get(COUNT_AS_NONTRANSLATABLE_EXACT));
//		desc.addCheckboxPart(paramsDesc.get(COUNT_AS_NONTRANSLATABLE_EXACT_TEXT_ONLY_PREVIOUS_VERSION));
//		desc.addCheckboxPart(paramsDesc.get(COUNT_AS_NONTRANSLATABLE_EXACT_TEXT_ONLY_UNIQUE_ID));
//		desc.addCheckboxPart(paramsDesc.get(COUNT_AS_NONTRANSLATABLE_EXACT_TEXT_ONLY));
//		desc.addCheckboxPart(paramsDesc.get(COUNT_AS_NONTRANSLATABLE_EXACT_REPAIRED));
//		desc.addCheckboxPart(paramsDesc.get(COUNT_AS_NONTRANSLATABLE_FUZZY_PREVIOUS_VERSION));
//		desc.addCheckboxPart(paramsDesc.get(COUNT_AS_NONTRANSLATABLE_FUZZY_UNIQUE_ID));
//		desc.addCheckboxPart(paramsDesc.get(COUNT_AS_NONTRANSLATABLE_FUZZY));
//		desc.addCheckboxPart(paramsDesc.get(COUNT_AS_NONTRANSLATABLE_FUZZY_REPAIRED));
//		desc.addCheckboxPart(paramsDesc.get(COUNT_AS_NONTRANSLATABLE_PHRASE_ASSEMBLED));
//		desc.addCheckboxPart(paramsDesc.get(COUNT_AS_NONTRANSLATABLE_MT));
//		desc.addCheckboxPart(paramsDesc.get(COUNT_AS_NONTRANSLATABLE_CONCORDANCE));

		return desc;
	}
	
	public boolean useDefaultTemplate() {
		return EMPTY_URI.equalsIgnoreCase(customTemplateURI.toString()) &&
				EMPTY_TEMPLATE.equalsIgnoreCase(customTemplateString); 
	}
	
	public boolean useTemplateFile() {
		return EMPTY_TEMPLATE.equalsIgnoreCase(customTemplateString);
	}
	
	public boolean isCountAsNonTranslatable_GMXExactMatched() {
		return countAsNonTranslatable_GMXExactMatched;
	}

	public void setCountAsNonTranslatable_GMXExactMatched(boolean countAsNonTranslatable_GMXExactMatched) {
		this.countAsNonTranslatable_GMXExactMatched = countAsNonTranslatable_GMXExactMatched;
	}

	public boolean isCountAsNonTranslatable_GMXLeveragedMatched() {
		return countAsNonTranslatable_GMXLeveragedMatched;
	}

	public void setCountAsNonTranslatable_GMXLeveragedMatched(boolean countAsNonTranslatable_GMXLeveragedMatched) {
		this.countAsNonTranslatable_GMXLeveragedMatched = countAsNonTranslatable_GMXLeveragedMatched;
	}

	public boolean isCountAsNonTranslatable_GMXRepetitionMatched() {
		return countAsNonTranslatable_GMXRepetitionMatched;
	}

	public void setCountAsNonTranslatable_GMXRepetitionMatched(
			boolean countAsNonTranslatable_GMXRepetitionMatched) {
		this.countAsNonTranslatable_GMXRepetitionMatched = countAsNonTranslatable_GMXRepetitionMatched;
	}

	public boolean isCountAsNonTranslatable_GMXFuzzyMatch() {
		return countAsNonTranslatable_GMXFuzzyMatch;
	}

	public void setCountAsNonTranslatable_GMXFuzzyMatch(boolean countAsNonTranslatable_GMXFuzzyMatch) {
		this.countAsNonTranslatable_GMXFuzzyMatch = countAsNonTranslatable_GMXFuzzyMatch;
	}

	public boolean isCountAsNonTranslatable_GMXAlphanumericOnlyTextUnit() {
		return countAsNonTranslatable_GMXAlphanumericOnlyTextUnit;
	}

	public void setCountAsNonTranslatable_GMXAlphanumericOnlyTextUnit(
			boolean countAsNonTranslatable_GMXAlphanumericOnlyTextUnit) {
		this.countAsNonTranslatable_GMXAlphanumericOnlyTextUnit = countAsNonTranslatable_GMXAlphanumericOnlyTextUnit;
	}

	public boolean isCountAsNonTranslatable_GMXNumericOnlyTextUnit() {
		return countAsNonTranslatable_GMXNumericOnlyTextUnit;
	}

	public void setCountAsNonTranslatable_GMXNumericOnlyTextUnit(
			boolean countAsNonTranslatable_GMXNumericOnlyTextUnit) {
		this.countAsNonTranslatable_GMXNumericOnlyTextUnit = countAsNonTranslatable_GMXNumericOnlyTextUnit;
	}

	public boolean isCountAsNonTranslatable_GMXMeasurementOnlyTextUnit() {
		return countAsNonTranslatable_GMXMeasurementOnlyTextUnit;
	}

	public void setCountAsNonTranslatable_GMXMeasurementOnlyTextUnit(
			boolean countAsNonTranslatable_GMXMeasurementOnlyTextUnit) {
		this.countAsNonTranslatable_GMXMeasurementOnlyTextUnit = countAsNonTranslatable_GMXMeasurementOnlyTextUnit;
	}

	public boolean isCountAsNonTranslatable_ExactUniqueIdMatch() {
		return countAsNonTranslatable_ExactUniqueIdMatch;
	}

	public void setCountAsNonTranslatable_ExactUniqueIdMatch(boolean countAsNonTranslatable_ExactUniqueIdMatch) {
		this.countAsNonTranslatable_ExactUniqueIdMatch = countAsNonTranslatable_ExactUniqueIdMatch;
	}

	public boolean isCountAsNonTranslatable_ExactPreviousVersionMatch() {
		return countAsNonTranslatable_ExactPreviousVersionMatch;
	}

	public void setCountAsNonTranslatable_ExactPreviousVersionMatch(
			boolean countAsNonTranslatable_ExactPreviousVersionMatch) {
		this.countAsNonTranslatable_ExactPreviousVersionMatch = countAsNonTranslatable_ExactPreviousVersionMatch;
	}

	public boolean isCountAsNonTranslatable_ExactLocalContextMatch() {
		return countAsNonTranslatable_ExactLocalContextMatch;
	}

	public void setCountAsNonTranslatable_ExactLocalContextMatch(
			boolean countAsNonTranslatable_ExactLocalContextMatch) {
		this.countAsNonTranslatable_ExactLocalContextMatch = countAsNonTranslatable_ExactLocalContextMatch;
	}

	public boolean isCountAsNonTranslatable_ExactDocumentContextMatch() {
		return countAsNonTranslatable_ExactDocumentContextMatch;
	}

	public void setCountAsNonTranslatable_ExactDocumentContextMatch(
			boolean countAsNonTranslatable_ExactDocumentContextMatch) {
		this.countAsNonTranslatable_ExactDocumentContextMatch = countAsNonTranslatable_ExactDocumentContextMatch;
	}

	public boolean isCountAsNonTranslatable_ExactStructuralMatch() {
		return countAsNonTranslatable_ExactStructuralMatch;
	}

	public void setCountAsNonTranslatable_ExactStructuralMatch(
			boolean countAsNonTranslatable_ExactStructuralMatch) {
		this.countAsNonTranslatable_ExactStructuralMatch = countAsNonTranslatable_ExactStructuralMatch;
	}

	public boolean isCountAsNonTranslatable_ExactMatch() {
		return countAsNonTranslatable_ExactMatch;
	}

	public void setCountAsNonTranslatable_ExactMatch(boolean countAsNonTranslatable_ExactMatch) {
		this.countAsNonTranslatable_ExactMatch = countAsNonTranslatable_ExactMatch;
	}

	public boolean isCountAsNonTranslatable_ExactTextOnlyPreviousVersionMatch() {
		return countAsNonTranslatable_ExactTextOnlyPreviousVersionMatch;
	}

	public void setCountAsNonTranslatable_ExactTextOnlyPreviousVersionMatch(
			boolean countAsNonTranslatable_ExactTextOnlyPreviousVersionMatch) {
		this.countAsNonTranslatable_ExactTextOnlyPreviousVersionMatch = countAsNonTranslatable_ExactTextOnlyPreviousVersionMatch;
	}

	public boolean isCountAsNonTranslatable_ExactTextOnlyUniqueIdMatch() {
		return countAsNonTranslatable_ExactTextOnlyUniqueIdMatch;
	}

	public void setCountAsNonTranslatable_ExactTextOnlyUniqueIdMatch(
			boolean countAsNonTranslatable_ExactTextOnlyUniqueIdMatch) {
		this.countAsNonTranslatable_ExactTextOnlyUniqueIdMatch = countAsNonTranslatable_ExactTextOnlyUniqueIdMatch;
	}

	public boolean isCountAsNonTranslatable_ExactTextOnly() {
		return countAsNonTranslatable_ExactTextOnly;
	}

	public void setCountAsNonTranslatable_ExactTextOnly(boolean countAsNonTranslatable_ExactTextOnly) {
		this.countAsNonTranslatable_ExactTextOnly = countAsNonTranslatable_ExactTextOnly;
	}

	public boolean isCountAsNonTranslatable_ExactRepaired() {
		return countAsNonTranslatable_ExactRepaired;
	}

	public void setCountAsNonTranslatable_ExactRepaired(boolean countAsNonTranslatable_ExactRepaired) {
		this.countAsNonTranslatable_ExactRepaired = countAsNonTranslatable_ExactRepaired;
	}

	public boolean isCountAsNonTranslatable_FuzzyPreviousVersionMatch() {
		return countAsNonTranslatable_FuzzyPreviousVersionMatch;
	}

	public void setCountAsNonTranslatable_FuzzyPreviousVersionMatch(
			boolean countAsNonTranslatable_FuzzyPreviousVersionMatch) {
		this.countAsNonTranslatable_FuzzyPreviousVersionMatch = countAsNonTranslatable_FuzzyPreviousVersionMatch;
	}

	public boolean isCountAsNonTranslatable_FuzzyUniqueIdMatch() {
		return countAsNonTranslatable_FuzzyUniqueIdMatch;
	}

	public void setCountAsNonTranslatable_FuzzyUniqueIdMatch(boolean countAsNonTranslatable_FuzzyUniqueIdMatch) {
		this.countAsNonTranslatable_FuzzyUniqueIdMatch = countAsNonTranslatable_FuzzyUniqueIdMatch;
	}

	public boolean isCountAsNonTranslatable_FuzzyMatch() {
		return countAsNonTranslatable_FuzzyMatch;
	}

	public void setCountAsNonTranslatable_FuzzyMatch(boolean countAsNonTranslatable_FuzzyMatch) {
		this.countAsNonTranslatable_FuzzyMatch = countAsNonTranslatable_FuzzyMatch;
	}

	public boolean isCountAsNonTranslatable_FuzzyRepaired() {
		return countAsNonTranslatable_FuzzyRepaired;
	}

	public void setCountAsNonTranslatable_FuzzyRepaired(boolean countAsNonTranslatable_FuzzyRepaired) {
		this.countAsNonTranslatable_FuzzyRepaired = countAsNonTranslatable_FuzzyRepaired;
	}

	public boolean isCountAsNonTranslatable_PhraseAssembled() {
		return countAsNonTranslatable_PhraseAssembled;
	}

	public void setCountAsNonTranslatable_PhraseAssembled(boolean countAsNonTranslatable_PhraseAssembled) {
		this.countAsNonTranslatable_PhraseAssembled = countAsNonTranslatable_PhraseAssembled;
	}

	public boolean isCountAsNonTranslatable_MT() {
		return countAsNonTranslatable_MT;
	}

	public void setCountAsNonTranslatable_MT(boolean countAsNonTranslatable_MT) {
		this.countAsNonTranslatable_MT = countAsNonTranslatable_MT;
	}

	public boolean isCountAsNonTranslatable_Concordance() {
		return countAsNonTranslatable_Concordance;
	}

	public void setCountAsNonTranslatable_Concordance(boolean countAsNonTranslatable_Concordance) {
		this.countAsNonTranslatable_Concordance = countAsNonTranslatable_Concordance;
	}

	public boolean isCountAsNonTranslatable_GMXProtected() {
		return countAsNonTranslatable_GMXProtected;
	}

	public void setCountAsNonTranslatable_GMXProtected(boolean countAsNonTranslatable_GMXProtected) {
		this.countAsNonTranslatable_GMXProtected = countAsNonTranslatable_GMXProtected;
	}
}

