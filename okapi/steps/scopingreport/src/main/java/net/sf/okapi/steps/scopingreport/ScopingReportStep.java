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

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IResource;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.pipeline.IPipelineStep;
import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
import net.sf.okapi.common.pipeline.annotations.StepParameterType;
import net.sf.okapi.common.query.MatchType;
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.resource.IWithAnnotations;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.lib.extra.steps.CompoundStep;
import net.sf.okapi.lib.reporting.ReportGenerator;
import net.sf.okapi.steps.wordcount.categorized.CategoryGroup;
import net.sf.okapi.steps.wordcount.categorized.gmx.GMXAlphanumericOnlyTextUnitCharacterCountStep;
import net.sf.okapi.steps.wordcount.categorized.gmx.GMXAlphanumericOnlyTextUnitWordCountStep;
import net.sf.okapi.steps.wordcount.categorized.gmx.GMXExactMatchedCharacterCountStep;
import net.sf.okapi.steps.wordcount.categorized.gmx.GMXExactMatchedWordCountStep;
import net.sf.okapi.steps.wordcount.categorized.gmx.GMXFuzzyMatchCharacterCountStep;
import net.sf.okapi.steps.wordcount.categorized.gmx.GMXFuzzyMatchWordCountStep;
import net.sf.okapi.steps.wordcount.categorized.gmx.GMXLeveragedMatchedCharacterCountStep;
import net.sf.okapi.steps.wordcount.categorized.gmx.GMXLeveragedMatchedWordCountStep;
import net.sf.okapi.steps.wordcount.categorized.gmx.GMXMeasurementOnlyTextUnitCharacterCountStep;
import net.sf.okapi.steps.wordcount.categorized.gmx.GMXMeasurementOnlyTextUnitWordCountStep;
import net.sf.okapi.steps.wordcount.categorized.gmx.GMXNumericOnlyTextUnitCharacterCountStep;
import net.sf.okapi.steps.wordcount.categorized.gmx.GMXNumericOnlyTextUnitWordCountStep;
import net.sf.okapi.steps.wordcount.categorized.gmx.GMXProtectedCharacterCountStep;
import net.sf.okapi.steps.wordcount.categorized.gmx.GMXProtectedWordCountStep;
import net.sf.okapi.steps.wordcount.categorized.gmx.GMXRepetitionMatchedCharacterCountStep;
import net.sf.okapi.steps.wordcount.categorized.gmx.GMXRepetitionMatchedWordCountStep;
import net.sf.okapi.steps.wordcount.categorized.okapi.ConcordanceCharacterCountStep;
import net.sf.okapi.steps.wordcount.categorized.okapi.ConcordanceWordCountStep;
import net.sf.okapi.steps.wordcount.categorized.okapi.ExactDocumentContextMatchCharacterCountStep;
import net.sf.okapi.steps.wordcount.categorized.okapi.ExactDocumentContextMatchWordCountStep;
import net.sf.okapi.steps.wordcount.categorized.okapi.ExactLocalContextMatchCharacterCountStep;
import net.sf.okapi.steps.wordcount.categorized.okapi.ExactLocalContextMatchWordCountStep;
import net.sf.okapi.steps.wordcount.categorized.okapi.ExactMatchCharacterCountStep;
import net.sf.okapi.steps.wordcount.categorized.okapi.ExactMatchWordCountStep;
import net.sf.okapi.steps.wordcount.categorized.okapi.ExactPreviousVersionMatchCharacterCountStep;
import net.sf.okapi.steps.wordcount.categorized.okapi.ExactPreviousVersionMatchWordCountStep;
import net.sf.okapi.steps.wordcount.categorized.okapi.ExactRepairedCharacterCountStep;
import net.sf.okapi.steps.wordcount.categorized.okapi.ExactRepairedWordCountStep;
import net.sf.okapi.steps.wordcount.categorized.okapi.ExactStructuralMatchCharacterCountStep;
import net.sf.okapi.steps.wordcount.categorized.okapi.ExactStructuralMatchWordCountStep;
import net.sf.okapi.steps.wordcount.categorized.okapi.ExactTextOnlyCharacterCountStep;
import net.sf.okapi.steps.wordcount.categorized.okapi.ExactTextOnlyPreviousVersionMatchCharacterCountStep;
import net.sf.okapi.steps.wordcount.categorized.okapi.ExactTextOnlyPreviousVersionMatchWordCountStep;
import net.sf.okapi.steps.wordcount.categorized.okapi.ExactTextOnlyUniqueIdMatchCharacterCountStep;
import net.sf.okapi.steps.wordcount.categorized.okapi.ExactTextOnlyUniqueIdMatchWordCountStep;
import net.sf.okapi.steps.wordcount.categorized.okapi.ExactTextOnlyWordCountStep;
import net.sf.okapi.steps.wordcount.categorized.okapi.ExactUniqueIdMatchCharacterCountStep;
import net.sf.okapi.steps.wordcount.categorized.okapi.ExactUniqueIdMatchWordCountStep;
import net.sf.okapi.steps.wordcount.categorized.okapi.FuzzyMatchCharacterCountStep;
import net.sf.okapi.steps.wordcount.categorized.okapi.FuzzyMatchWordCountStep;
import net.sf.okapi.steps.wordcount.categorized.okapi.FuzzyPreviousVersionMatchCharacterCountStep;
import net.sf.okapi.steps.wordcount.categorized.okapi.FuzzyPreviousVersionMatchWordCountStep;
import net.sf.okapi.steps.wordcount.categorized.okapi.FuzzyRepairedCharacterCountStep;
import net.sf.okapi.steps.wordcount.categorized.okapi.FuzzyRepairedWordCountStep;
import net.sf.okapi.steps.wordcount.categorized.okapi.FuzzyUniqueIdMatchCharacterCountStep;
import net.sf.okapi.steps.wordcount.categorized.okapi.FuzzyUniqueIdMatchWordCountStep;
import net.sf.okapi.steps.wordcount.categorized.okapi.MTCharacterCountStep;
import net.sf.okapi.steps.wordcount.categorized.okapi.MTWordCountStep;
import net.sf.okapi.steps.wordcount.categorized.okapi.PhraseAssembledCharacterCountStep;
import net.sf.okapi.steps.wordcount.categorized.okapi.PhraseAssembledWordCountStep;
import net.sf.okapi.steps.wordcount.common.BaseCounter;
import net.sf.okapi.steps.wordcount.common.GMX;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@UsingParameters(Parameters.class)
public class ScopingReportStep extends CompoundStep {

	/**
	 * General report fields
	 */
	public static final String PROJECT_NAME = "PROJECT_NAME";
	public static final String PROJECT_DATE = "PROJECT_DATE";
	public static final String PROJECT_SOURCE_LOCALE = "PROJECT_SOURCE_LOCALE";
	public static final String PROJECT_TARGET_LOCALE = "PROJECT_TARGET_LOCALE";	
	public static final String PROJECT_TOTAL_WORD_COUNT = "PROJECT_TOTAL_WORD_COUNT";
	public static final String PROJECT_TOTAL_CHARACTER_COUNT = "PROJECT_TOTAL_CHARACTER_COUNT";
	public static final String PROJECT_WHITESPACE_CHARACTER_COUNT = "PROJECT_WHITESPACE_CHARACTER_COUNT";
	public static final String PROJECT_PUNCTUATION_CHARACTER_COUNT = "PROJECT_PUNCTUATION_CHARACTER_COUNT";
	public static final String PROJECT_OVERALL_CHARACTER_COUNT = "PROJECT_OVERALL_CHARACTER_COUNT";
	
	public static final String ITEM_NAME = "ITEM_NAME";
	public static final String ITEM_SOURCE_LOCALE = "ITEM_SOURCE_LOCALE";
	public static final String ITEM_TARGET_LOCALE = "ITEM_TARGET_LOCALE";
	public static final String ITEM_TOTAL_WORD_COUNT = "ITEM_TOTAL_WORD_COUNT";	
	public static final String ITEM_TOTAL_CHARACTER_COUNT = "ITEM_TOTAL_CHARACTER_COUNT";
	public static final String ITEM_WHITESPACE_CHARACTER_COUNT = "ITEM_WHITESPACE_CHARACTER_COUNT";
	public static final String ITEM_PUNCTUATION_CHARACTER_COUNT = "ITEM_PUNCTUATION_CHARACTER_COUNT";
	public static final String ITEM_OVERALL_CHARACTER_COUNT = "ITEM_OVERALL_CHARACTER_COUNT";
	
	/**
	 * Report fields for word counts of the entire project (GMX categories)
	 */	
	public static final String PROJECT_GMX_PROTECTED_WORD_COUNT = "PROJECT_GMX_PROTECTED_WORD_COUNT";
	public static final String PROJECT_GMX_EXACT_MATCHED_WORD_COUNT = "PROJECT_GMX_EXACT_MATCHED_WORD_COUNT";
	public static final String PROJECT_GMX_LEVERAGED_MATCHED_WORD_COUNT = "PROJECT_GMX_LEVERAGED_MATCHED_WORD_COUNT";
	public static final String PROJECT_GMX_REPETITION_MATCHED_WORD_COUNT = "PROJECT_GMX_REPETITION_MATCHED_WORD_COUNT";
	public static final String PROJECT_GMX_FUZZY_MATCHED_WORD_COUNT = "PROJECT_GMX_FUZZY_MATCHED_WORD_COUNT";
	public static final String PROJECT_GMX_ALPHANUMERIC_ONLY_TEXT_UNIT_WORD_COUNT = "PROJECT_GMX_ALPHANUMERIC_ONLY_TEXT_UNIT_WORD_COUNT";
	public static final String PROJECT_GMX_NUMERIC_ONLY_TEXT_UNIT_WORD_COUNT = "PROJECT_GMX_NUMERIC_ONLY_TEXT_UNIT_WORD_COUNT";
	public static final String PROJECT_GMX_MEASUREMENT_ONLY_TEXT_UNIT_WORD_COUNT = "PROJECT_GMX_MEASUREMENT_ONLY_TEXT_UNIT_WORD_COUNT";
	public static final String PROJECT_GMX_NONTRANSLATABLE_WORD_COUNT = "PROJECT_GMX_NONTRANSLATABLE_WORD_COUNT"; 
	public static final String PROJECT_GMX_TRANSLATABLE_WORD_COUNT = "PROJECT_GMX_TRANSLATABLE_WORD_COUNT";
	public static final String PROJECT_GMX_NOCATEGORY = "PROJECT_GMX_NOCATEGORY";
	
	/**
	 * Report fields for character counts of the entire project (GMX categories)
	 */	
	public static final String PROJECT_GMX_PROTECTED_CHARACTER_COUNT = "PROJECT_GMX_PROTECTED_CHARACTER_COUNT";
	public static final String PROJECT_GMX_EXACT_MATCHED_CHARACTER_COUNT = "PROJECT_GMX_EXACT_MATCHED_CHARACTER_COUNT";
	public static final String PROJECT_GMX_LEVERAGED_MATCHED_CHARACTER_COUNT = "PROJECT_GMX_LEVERAGED_MATCHED_CHARACTER_COUNT";
	public static final String PROJECT_GMX_REPETITION_MATCHED_CHARACTER_COUNT = "PROJECT_GMX_REPETITION_MATCHED_CHARACTER_COUNT";
	public static final String PROJECT_GMX_FUZZY_MATCHED_CHARACTER_COUNT = "PROJECT_GMX_FUZZY_MATCHED_CHARACTER_COUNT";
	public static final String PROJECT_GMX_ALPHANUMERIC_ONLY_TEXT_UNIT_CHARACTER_COUNT = "PROJECT_GMX_ALPHANUMERIC_ONLY_TEXT_UNIT_CHARACTER_COUNT";
	public static final String PROJECT_GMX_NUMERIC_ONLY_TEXT_UNIT_CHARACTER_COUNT = "PROJECT_GMX_NUMERIC_ONLY_TEXT_UNIT_CHARACTER_COUNT";
	public static final String PROJECT_GMX_MEASUREMENT_ONLY_TEXT_UNIT_CHARACTER_COUNT = "PROJECT_GMX_MEASUREMENT_ONLY_TEXT_UNIT_CHARACTER_COUNT";
	public static final String PROJECT_GMX_NONTRANSLATABLE_CHARACTER_COUNT = "PROJECT_GMX_NONTRANSLATABLE_CHARACTER_COUNT"; 
	public static final String PROJECT_GMX_TRANSLATABLE_CHARACTER_COUNT = "PROJECT_GMX_TRANSLATABLE_CHARACTER_COUNT";
	public static final String PROJECT_GMX_NOCATEGORY_CHARACTERS = "PROJECT_GMX_NOCATEGORY_CHARACTERS";
	
	/**
	 * Report fields for word counts of individual items (GMX categories)
	 */	
	public static final String ITEM_GMX_PROTECTED_WORD_COUNT = "ITEM_GMX_PROTECTED_WORD_COUNT";
	public static final String ITEM_GMX_EXACT_MATCHED_WORD_COUNT = "ITEM_GMX_EXACT_MATCHED_WORD_COUNT";
	public static final String ITEM_GMX_LEVERAGED_MATCHED_WORD_COUNT = "ITEM_GMX_LEVERAGED_MATCHED_WORD_COUNT";
	public static final String ITEM_GMX_REPETITION_MATCHED_WORD_COUNT = "ITEM_GMX_REPETITION_MATCHED_WORD_COUNT";
	public static final String ITEM_GMX_FUZZY_MATCHED_WORD_COUNT = "ITEM_GMX_FUZZY_MATCHED_WORD_COUNT";
	public static final String ITEM_GMX_ALPHANUMERIC_ONLY_TEXT_UNIT_WORD_COUNT = "ITEM_GMX_ALPHANUMERIC_ONLY_TEXT_UNIT_WORD_COUNT";
	public static final String ITEM_GMX_NUMERIC_ONLY_TEXT_UNIT_WORD_COUNT = "ITEM_GMX_NUMERIC_ONLY_TEXT_UNIT_WORD_COUNT";
	public static final String ITEM_GMX_MEASUREMENT_ONLY_TEXT_UNIT_WORD_COUNT = "ITEM_GMX_MEASUREMENT_ONLY_TEXT_UNIT_WORD_COUNT";
	public static final String ITEM_GMX_NONTRANSLATABLE_WORD_COUNT = "ITEM_GMX_NONTRANSLATABLE_WORD_COUNT"; 
	public static final String ITEM_GMX_TRANSLATABLE_WORD_COUNT = "ITEM_GMX_TRANSLATABLE_WORD_COUNT";
	public static final String ITEM_GMX_NOCATEGORY = "ITEM_GMX_NOCATEGORY";
	
	/**
	 * Report fields for character counts of individual items (GMX categories)
	 */	
	public static final String ITEM_GMX_PROTECTED_CHARACTER_COUNT = "ITEM_GMX_PROTECTED_CHARACTER_COUNT";
	public static final String ITEM_GMX_EXACT_MATCHED_CHARACTER_COUNT = "ITEM_GMX_EXACT_MATCHED_CHARACTER_COUNT";
	public static final String ITEM_GMX_LEVERAGED_MATCHED_CHARACTER_COUNT = "ITEM_GMX_LEVERAGED_MATCHED_CHARACTER_COUNT";
	public static final String ITEM_GMX_REPETITION_MATCHED_CHARACTER_COUNT = "ITEM_GMX_REPETITION_MATCHED_CHARACTER_COUNT";
	public static final String ITEM_GMX_FUZZY_MATCHED_CHARACTER_COUNT = "ITEM_GMX_FUZZY_MATCHED_CHARACTER_COUNT";
	public static final String ITEM_GMX_ALPHANUMERIC_ONLY_TEXT_UNIT_CHARACTER_COUNT = "ITEM_GMX_ALPHANUMERIC_ONLY_TEXT_UNIT_CHARACTER_COUNT";
	public static final String ITEM_GMX_NUMERIC_ONLY_TEXT_UNIT_CHARACTER_COUNT = "ITEM_GMX_NUMERIC_ONLY_TEXT_UNIT_CHARACTER_COUNT";
	public static final String ITEM_GMX_MEASUREMENT_ONLY_TEXT_UNIT_CHARACTER_COUNT = "ITEM_GMX_MEASUREMENT_ONLY_TEXT_UNIT_CHARACTER_COUNT";
	public static final String ITEM_GMX_NONTRANSLATABLE_CHARACTER_COUNT = "ITEM_GMX_NONTRANSLATABLE_CHARACTER_COUNT"; 
	public static final String ITEM_GMX_TRANSLATABLE_CHARACTER_COUNT = "ITEM_GMX_TRANSLATABLE_CHARACTER_COUNT";
	public static final String ITEM_GMX_NOCATEGORY_CHARACTERS = "ITEM_GMX_NOCATEGORY_CHARACTERS";
	
	/**
	 * Report fields for word counts of the entire project (Okapi categories)
	 */	
	public static final String PROJECT_EXACT_UNIQUE_ID = "PROJECT_EXACT_UNIQUE_ID";	
	public static final String PROJECT_EXACT_PREVIOUS_VERSION = "PROJECT_EXACT_PREVIOUS_VERSION";
	public static final String PROJECT_EXACT_LOCAL_CONTEXT = "PROJECT_EXACT_LOCAL_CONTEXT";
	public static final String PROJECT_EXACT_DOCUMENT_CONTEXT = "PROJECT_EXACT_DOCUMENT_CONTEXT";
	public static final String PROJECT_EXACT_STRUCTURAL = "PROJECT_EXACT_STRUCTURAL";
	public static final String PROJECT_EXACT = "PROJECT_EXACT";
	public static final String PROJECT_EXACT_TEXT_ONLY_PREVIOUS_VERSION = "PROJECT_EXACT_TEXT_ONLY_PREVIOUS_VERSION";
	public static final String PROJECT_EXACT_TEXT_ONLY_UNIQUE_ID = "PROJECT_EXACT_TEXT_ONLY_UNIQUE_ID";
	public static final String PROJECT_EXACT_TEXT_ONLY = "PROJECT_EXACT_TEXT_ONLY";	
	public static final String PROJECT_EXACT_REPAIRED = "PROJECT_EXACT_REPAIRED";
	public static final String PROJECT_FUZZY_PREVIOUS_VERSION = "PROJECT_FUZZY_PREVIOUS_VERSION";
	public static final String PROJECT_FUZZY_UNIQUE_ID = "PROJECT_FUZZY_UNIQUE_ID";
	public static final String PROJECT_FUZZY = "PROJECT_FUZZY";
	public static final String PROJECT_FUZZY_REPAIRED = "PROJECT_FUZZY_REPAIRED";
	public static final String PROJECT_PHRASE_ASSEMBLED = "PROJECT_PHRASE_ASSEMBLED";
	public static final String PROJECT_MT = "PROJECT_MT";
	public static final String PROJECT_CONCORDANCE = "PROJECT_CONCORDANCE";
	public static final String PROJECT_NONTRANSLATABLE_WORD_COUNT = "PROJECT_NONTRANSLATABLE_WORD_COUNT"; 
	public static final String PROJECT_TRANSLATABLE_WORD_COUNT = "PROJECT_TRANSLATABLE_WORD_COUNT";
	public static final String PROJECT_NOCATEGORY = "PROJECT_NOCATEGORY";
	
	/**
	 * Report fields for character counts of the entire project (Okapi categories)
	 */	
	public static final String PROJECT_EXACT_UNIQUE_ID_CHARACTERS = "PROJECT_EXACT_UNIQUE_ID_CHARACTERS";	
	public static final String PROJECT_EXACT_PREVIOUS_VERSION_CHARACTERS = "PROJECT_EXACT_PREVIOUS_VERSION_CHARACTERS";
	public static final String PROJECT_EXACT_LOCAL_CONTEXT_CHARACTERS = "PROJECT_EXACT_LOCAL_CONTEXT_CHARACTERS";
	public static final String PROJECT_EXACT_DOCUMENT_CONTEXT_CHARACTERS = "PROJECT_EXACT_DOCUMENT_CONTEXT_CHARACTERS";
	public static final String PROJECT_EXACT_STRUCTURAL_CHARACTERS = "PROJECT_EXACT_STRUCTURAL_CHARACTERS";
	public static final String PROJECT_EXACT_CHARACTERS = "PROJECT_EXACT_CHARACTERS";
	public static final String PROJECT_EXACT_TEXT_ONLY_PREVIOUS_VERSION_CHARACTERS = "PROJECT_EXACT_TEXT_ONLY_PREVIOUS_VERSION_CHARACTERS";
	public static final String PROJECT_EXACT_TEXT_ONLY_UNIQUE_ID_CHARACTERS = "PROJECT_EXACT_TEXT_ONLY_UNIQUE_ID_CHARACTERS";
	public static final String PROJECT_EXACT_TEXT_ONLY_CHARACTERS = "PROJECT_EXACT_TEXT_ONLY_CHARACTERS";	
	public static final String PROJECT_EXACT_REPAIRED_CHARACTERS = "PROJECT_EXACT_REPAIRED_CHARACTERS";
	public static final String PROJECT_FUZZY_PREVIOUS_VERSION_CHARACTERS = "PROJECT_FUZZY_PREVIOUS_VERSION_CHARACTERS";
	public static final String PROJECT_FUZZY_UNIQUE_ID_CHARACTERS = "PROJECT_FUZZY_UNIQUE_ID_CHARACTERS";
	public static final String PROJECT_FUZZY_CHARACTERS = "PROJECT_FUZZY_CHARACTERS";
	public static final String PROJECT_FUZZY_REPAIRED_CHARACTERS = "PROJECT_FUZZY_REPAIRED_CHARACTERS";
	public static final String PROJECT_PHRASE_ASSEMBLED_CHARACTERS = "PROJECT_PHRASE_ASSEMBLED_CHARACTERS";
	public static final String PROJECT_MT_CHARACTERS = "PROJECT_MT_CHARACTERS";
	public static final String PROJECT_CONCORDANCE_CHARACTERS = "PROJECT_CONCORDANCE_CHARACTERS";
	public static final String PROJECT_NONTRANSLATABLE_CHARACTER_COUNT = "PROJECT_NONTRANSLATABLE_CHARACTER_COUNT"; 
	public static final String PROJECT_TRANSLATABLE_CHARACTER_COUNT = "PROJECT_TRANSLATABLE_CHARACTER_COUNT";
	public static final String PROJECT_NOCATEGORY_CHARACTERS = "PROJECT_NOCATEGORY_CHARACTERS";
	
	/**
	 * Report fields for word counts of individual items (Okapi categories)
	 */	
	public static final String ITEM_EXACT_UNIQUE_ID = "ITEM_EXACT_UNIQUE_ID";	
	public static final String ITEM_EXACT_PREVIOUS_VERSION = "ITEM_EXACT_PREVIOUS_VERSION";
	public static final String ITEM_EXACT_LOCAL_CONTEXT = "ITEM_EXACT_LOCAL_CONTEXT";
	public static final String ITEM_EXACT_DOCUMENT_CONTEXT = "ITEM_EXACT_DOCUMENT_CONTEXT";
	public static final String ITEM_EXACT_STRUCTURAL = "ITEM_EXACT_STRUCTURAL";
	public static final String ITEM_EXACT = "ITEM_EXACT";
	public static final String ITEM_EXACT_TEXT_ONLY_PREVIOUS_VERSION = "ITEM_EXACT_TEXT_ONLY_PREVIOUS_VERSION";
	public static final String ITEM_EXACT_TEXT_ONLY_UNIQUE_ID = "ITEM_EXACT_TEXT_ONLY_UNIQUE_ID";
	public static final String ITEM_EXACT_TEXT_ONLY = "ITEM_EXACT_TEXT_ONLY";	
	public static final String ITEM_EXACT_REPAIRED = "ITEM_EXACT_REPAIRED";
	public static final String ITEM_FUZZY_PREVIOUS_VERSION = "ITEM_FUZZY_PREVIOUS_VERSION";
	public static final String ITEM_FUZZY_UNIQUE_ID = "ITEM_FUZZY_UNIQUE_ID";
	public static final String ITEM_FUZZY = "ITEM_FUZZY";
	public static final String ITEM_FUZZY_REPAIRED = "ITEM_FUZZY_REPAIRED";
	public static final String ITEM_PHRASE_ASSEMBLED = "ITEM_PHRASE_ASSEMBLED";
	public static final String ITEM_MT = "ITEM_MT";
	public static final String ITEM_CONCORDANCE = "ITEM_CONCORDANCE";
	public static final String ITEM_NONTRANSLATABLE_WORD_COUNT = "ITEM_NONTRANSLATABLE_WORD_COUNT"; 
	public static final String ITEM_TRANSLATABLE_WORD_COUNT = "ITEM_TRANSLATABLE_WORD_COUNT";
	public static final String ITEM_NOCATEGORY = "ITEM_NOCATEGORY";
	
	/**
	 * Report fields for character counts of individual items (Okapi categories)
	 */	
	public static final String ITEM_EXACT_UNIQUE_ID_CHARACTERS = "ITEM_EXACT_UNIQUE_ID_CHARACTERS";	
	public static final String ITEM_EXACT_PREVIOUS_VERSION_CHARACTERS = "ITEM_EXACT_PREVIOUS_VERSION_CHARACTERS";
	public static final String ITEM_EXACT_LOCAL_CONTEXT_CHARACTERS = "ITEM_EXACT_LOCAL_CONTEXT_CHARACTERS";
	public static final String ITEM_EXACT_DOCUMENT_CONTEXT_CHARACTERS = "ITEM_EXACT_DOCUMENT_CONTEXT_CHARACTERS";
	public static final String ITEM_EXACT_STRUCTURAL_CHARACTERS = "ITEM_EXACT_STRUCTURAL_CHARACTERS";
	public static final String ITEM_EXACT_CHARACTERS = "ITEM_EXACT_CHARACTERS";
	public static final String ITEM_EXACT_TEXT_ONLY_PREVIOUS_VERSION_CHARACTERS = "ITEM_EXACT_TEXT_ONLY_PREVIOUS_VERSION_CHARACTERS";
	public static final String ITEM_EXACT_TEXT_ONLY_UNIQUE_ID_CHARACTERS = "ITEM_EXACT_TEXT_ONLY_UNIQUE_ID_CHARACTERS";
	public static final String ITEM_EXACT_TEXT_ONLY_CHARACTERS = "ITEM_EXACT_TEXT_ONLY_CHARACTERS";	
	public static final String ITEM_EXACT_REPAIRED_CHARACTERS = "ITEM_EXACT_REPAIRED_CHARACTERS";
	public static final String ITEM_FUZZY_PREVIOUS_VERSION_CHARACTERS = "ITEM_FUZZY_PREVIOUS_VERSION_CHARACTERS";
	public static final String ITEM_FUZZY_UNIQUE_ID_CHARACTERS = "ITEM_FUZZY_UNIQUE_ID_CHARACTERS";
	public static final String ITEM_FUZZY_CHARACTERS = "ITEM_FUZZY_CHARACTERS";
	public static final String ITEM_FUZZY_REPAIRED_CHARACTERS = "ITEM_FUZZY_REPAIRED_CHARACTERS";
	public static final String ITEM_PHRASE_ASSEMBLED_CHARACTERS = "ITEM_PHRASE_ASSEMBLED_CHARACTERS";
	public static final String ITEM_MT_CHARACTERS = "ITEM_MT_CHARACTERS";
	public static final String ITEM_CONCORDANCE_CHARACTERS = "ITEM_CONCORDANCE_CHARACTERS";
	public static final String ITEM_NONTRANSLATABLE_CHARACTER_COUNT = "ITEM_NONTRANSLATABLE_CHARACTER_COUNT"; 
	public static final String ITEM_TRANSLATABLE_CHARACTER_COUNT = "ITEM_TRANSLATABLE_CHARACTER_COUNT";
	public static final String ITEM_NOCATEGORY_CHARACTERS = "ITEM_NOCATEGORY_CHARACTERS";
		
	private static final String DEFAULT_TEMPLATE = "scoping_report.html";
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private Parameters params;
	private ReportGenerator gen;
	private String rootDir;
//@	private CategoryResolver resolver;
	
	public ScopingReportStep() {
		super();
		params = new Parameters();
		setParameters(params);		
		setName("Scoping Report");
		setDescription("Create a template-based scoping report based on word count and leverage annotations."
			+" Expects: filter events. Sends back: filter events.");		
	}
	
	@StepParameterMapping(parameterType = StepParameterType.ROOT_DIRECTORY)
	public void setRootDirectory (String rootDir) {
		this.rootDir = rootDir;
	}
	
	private ReportGenerator makeReportGenerator() {
		if (params.useTemplateFile()) {
			return new ReportGenerator(getTemplateStream());
		} else {
			return new ReportGenerator(params.getCustomTemplateString());
		}
	}

	protected InputStream getTemplateStream() {
		if (params.useDefaultTemplate()) {
			return this.getClass().getResourceAsStream(DEFAULT_TEMPLATE);
		}
		else {
			try {
				return new BufferedInputStream(new FileInputStream(new File(params.getCustomTemplateURI())));
			} catch (IOException e) {
				logger.warn("Error opening the custom template, default one is used. {}", e.toString());
				return this.getClass().getResourceAsStream(DEFAULT_TEMPLATE);
			} 
		}
	}

	/**
	 * Adds internal steps providing counts against single categories. 
	 * The sequence of internal steps in this list defines priorities of categories within their groups (GMX and Okapi).
	 * A category group is assigned to a step by having it *implement* the category group's marker interface.
	 */
	@Override
	protected void addStepsToList(List<IPipelineStep> list) {
		
		// GMX word count categories
		list.add(new GMXProtectedWordCountStep());
		list.add(new GMXExactMatchedWordCountStep());
		list.add(new GMXLeveragedMatchedWordCountStep());
		list.add(new GMXRepetitionMatchedWordCountStep());
		list.add(new GMXFuzzyMatchWordCountStep());		
		list.add(new GMXAlphanumericOnlyTextUnitWordCountStep());
		list.add(new GMXNumericOnlyTextUnitWordCountStep());
		list.add(new GMXMeasurementOnlyTextUnitWordCountStep());
		
		// GMX character count categories
		list.add(new GMXProtectedCharacterCountStep());
		list.add(new GMXExactMatchedCharacterCountStep());
		list.add(new GMXLeveragedMatchedCharacterCountStep());
		list.add(new GMXRepetitionMatchedCharacterCountStep());
		list.add(new GMXFuzzyMatchCharacterCountStep());
		list.add(new GMXAlphanumericOnlyTextUnitCharacterCountStep());
		list.add(new GMXNumericOnlyTextUnitCharacterCountStep());
		list.add(new GMXMeasurementOnlyTextUnitCharacterCountStep());
		
		// Okapi word count categories
		list.add(new ExactUniqueIdMatchWordCountStep());
		list.add(new ExactPreviousVersionMatchWordCountStep());
		list.add(new ExactLocalContextMatchWordCountStep());
		list.add(new ExactDocumentContextMatchWordCountStep());
		list.add(new ExactStructuralMatchWordCountStep());
		list.add(new ExactMatchWordCountStep());
		list.add(new ExactTextOnlyUniqueIdMatchWordCountStep());
		list.add(new ExactTextOnlyPreviousVersionMatchWordCountStep());		
		list.add(new ExactTextOnlyWordCountStep());
		list.add(new ExactRepairedWordCountStep());
		list.add(new FuzzyUniqueIdMatchWordCountStep());
		list.add(new FuzzyPreviousVersionMatchWordCountStep());		
		list.add(new FuzzyMatchWordCountStep());
		list.add(new FuzzyRepairedWordCountStep());
		list.add(new PhraseAssembledWordCountStep());
		list.add(new MTWordCountStep());
		list.add(new ConcordanceWordCountStep());
		
		// Okapi character count categories
		list.add(new ExactUniqueIdMatchCharacterCountStep());
		list.add(new ExactPreviousVersionMatchCharacterCountStep());
		list.add(new ExactLocalContextMatchCharacterCountStep());
		list.add(new ExactDocumentContextMatchCharacterCountStep());
		list.add(new ExactStructuralMatchCharacterCountStep());
		list.add(new ExactMatchCharacterCountStep());
		list.add(new ExactTextOnlyUniqueIdMatchCharacterCountStep());
		list.add(new ExactTextOnlyPreviousVersionMatchCharacterCountStep());		
		list.add(new ExactTextOnlyCharacterCountStep());
		list.add(new ExactRepairedCharacterCountStep());
		list.add(new FuzzyUniqueIdMatchCharacterCountStep());
		list.add(new FuzzyPreviousVersionMatchCharacterCountStep());		
		list.add(new FuzzyMatchCharacterCountStep());
		list.add(new FuzzyRepairedCharacterCountStep());
		list.add(new PhraseAssembledCharacterCountStep());
		list.add(new MTCharacterCountStep());
		list.add(new ConcordanceCharacterCountStep());
		
//@		resolver = new CategoryResolver(list); // List should be filled up at this point
	}
	
	@Override
	protected Event handleStartBatch(Event event) {
		//@		resolver.reset();
		params = getParameters(Parameters.class);
		gen = makeReportGenerator();
		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);
		gen.setField(PROJECT_NAME, params.getProjectName());
		gen.setField(PROJECT_DATE, df.format(new Date()));
		
		// Values as they were set by the pipeline via step parameters 
		gen.setField(PROJECT_SOURCE_LOCALE, getSourceLocale().toString());
		gen.setField(PROJECT_TARGET_LOCALE, getTargetLocale().toString());
		return super.handleStartBatch(event);
	}
	
	@Override
	protected Event handleStartBatchItem(Event event) {
		//@		resolver.reset();
		return super.handleStartBatchItem(event);
	}
	
	/**
	 * Sets per-project fields
	 */
	@Override
	protected Event handleEndBatch(Event event) {
		Ending res = event.getEnding();		
		if (res != null) {
			//@			resolver.resolve(res, EventType.END_BATCH);
			
			gen.setField(PROJECT_TOTAL_WORD_COUNT, BaseCounter.getCount(res, GMX.TotalWordCount));
			gen.setField(PROJECT_TOTAL_CHARACTER_COUNT, BaseCounter.getCount(res, GMX.TotalCharacterCount));
			gen.setField(PROJECT_WHITESPACE_CHARACTER_COUNT, BaseCounter.getCount(res, GMX.WhiteSpaceCharacterCount));
			gen.setField(PROJECT_PUNCTUATION_CHARACTER_COUNT, BaseCounter.getCount(res, GMX.PunctuationCharacterCount));
			gen.setField(PROJECT_OVERALL_CHARACTER_COUNT, BaseCounter.getCount(res, GMX.OverallCharacterCount));
			
			long ntrCount = countNonTranslatable(res, CategoryGroup.OKAPI_WORD_COUNTS);
			gen.setField(PROJECT_TRANSLATABLE_WORD_COUNT, Math.max(0, BaseCounter.getCount(res, GMX.TotalWordCount) - ntrCount));
			gen.setField(PROJECT_NONTRANSLATABLE_WORD_COUNT, ntrCount);
			
			ntrCount = countNonTranslatable(res, CategoryGroup.OKAPI_CHARACTER_COUNTS);
			gen.setField(PROJECT_TRANSLATABLE_CHARACTER_COUNT, Math.max(0, BaseCounter.getCount(res, GMX.TotalCharacterCount) - ntrCount));
			gen.setField(PROJECT_NONTRANSLATABLE_CHARACTER_COUNT, ntrCount);
			
			ntrCount = countNonTranslatable(res, CategoryGroup.GMX_WORD_COUNTS);
			gen.setField(PROJECT_GMX_TRANSLATABLE_WORD_COUNT, Math.max(0, BaseCounter.getCount(res, GMX.TotalWordCount) - ntrCount));
			gen.setField(PROJECT_GMX_NONTRANSLATABLE_WORD_COUNT, ntrCount);
			
			ntrCount = countNonTranslatable(res, CategoryGroup.GMX_CHARACTER_COUNTS);
			gen.setField(PROJECT_GMX_TRANSLATABLE_CHARACTER_COUNT, Math.max(0, BaseCounter.getCount(res, GMX.TotalCharacterCount) - ntrCount));
			gen.setField(PROJECT_GMX_NONTRANSLATABLE_CHARACTER_COUNT, ntrCount);
			
			gen.setField(PROJECT_GMX_PROTECTED_WORD_COUNT, BaseCounter.getCount(res, GMX.ProtectedWordCount));
			gen.setField(PROJECT_GMX_EXACT_MATCHED_WORD_COUNT, BaseCounter.getCount(res, GMX.ExactMatchedWordCount));
			gen.setField(PROJECT_GMX_LEVERAGED_MATCHED_WORD_COUNT, BaseCounter.getCount(res, GMX.LeveragedMatchedWordCount));
			gen.setField(PROJECT_GMX_REPETITION_MATCHED_WORD_COUNT, BaseCounter.getCount(res, GMX.RepetitionMatchedWordCount));
			gen.setField(PROJECT_GMX_FUZZY_MATCHED_WORD_COUNT, BaseCounter.getCount(res, GMX.FuzzyMatchedWordCount));
			gen.setField(PROJECT_GMX_ALPHANUMERIC_ONLY_TEXT_UNIT_WORD_COUNT, BaseCounter.getCount(res, GMX.AlphanumericOnlyTextUnitWordCount));
			gen.setField(PROJECT_GMX_NUMERIC_ONLY_TEXT_UNIT_WORD_COUNT, BaseCounter.getCount(res, GMX.NumericOnlyTextUnitWordCount));
			gen.setField(PROJECT_GMX_MEASUREMENT_ONLY_TEXT_UNIT_WORD_COUNT, BaseCounter.getCount(res, GMX.MeasurementOnlyTextUnitWordCount));
			gen.setField(PROJECT_GMX_NOCATEGORY, BaseCounter.getCount(res, GMX.TotalWordCount) - countCategories(res, CategoryGroup.GMX_WORD_COUNTS));

			gen.setField(PROJECT_GMX_PROTECTED_CHARACTER_COUNT, BaseCounter.getCount(res, GMX.ProtectedCharacterCount));
			gen.setField(PROJECT_GMX_EXACT_MATCHED_CHARACTER_COUNT, BaseCounter.getCount(res, GMX.ExactMatchedCharacterCount));
			gen.setField(PROJECT_GMX_LEVERAGED_MATCHED_CHARACTER_COUNT, BaseCounter.getCount(res, GMX.LeveragedMatchedCharacterCount));
			gen.setField(PROJECT_GMX_REPETITION_MATCHED_CHARACTER_COUNT, BaseCounter.getCount(res, GMX.RepetitionMatchedCharacterCount));
			gen.setField(PROJECT_GMX_FUZZY_MATCHED_CHARACTER_COUNT, BaseCounter.getCount(res, GMX.FuzzyMatchedCharacterCount));
			gen.setField(PROJECT_GMX_ALPHANUMERIC_ONLY_TEXT_UNIT_CHARACTER_COUNT, BaseCounter.getCount(res, GMX.AlphanumericOnlyTextUnitCharacterCount));
			gen.setField(PROJECT_GMX_NUMERIC_ONLY_TEXT_UNIT_CHARACTER_COUNT, BaseCounter.getCount(res, GMX.NumericOnlyTextUnitCharacterCount));
			gen.setField(PROJECT_GMX_MEASUREMENT_ONLY_TEXT_UNIT_CHARACTER_COUNT, BaseCounter.getCount(res, GMX.MeasurementOnlyTextUnitCharacterCount));
			gen.setField(PROJECT_GMX_NOCATEGORY_CHARACTERS, BaseCounter.getCount(res, GMX.TotalCharacterCount) - countCategories(res, CategoryGroup.GMX_CHARACTER_COUNTS));
			
			gen.setField(PROJECT_EXACT_UNIQUE_ID, BaseCounter.getCount(res, MatchType.EXACT_UNIQUE_ID.name()));
			gen.setField(PROJECT_EXACT_PREVIOUS_VERSION, BaseCounter.getCount(res, MatchType.EXACT_PREVIOUS_VERSION.name()));
			gen.setField(PROJECT_EXACT_LOCAL_CONTEXT, BaseCounter.getCount(res, MatchType.EXACT_LOCAL_CONTEXT.name()));
			gen.setField(PROJECT_EXACT_DOCUMENT_CONTEXT, BaseCounter.getCount(res, MatchType.EXACT_DOCUMENT_CONTEXT.name()));
			gen.setField(PROJECT_EXACT_STRUCTURAL, BaseCounter.getCount(res, MatchType.EXACT_STRUCTURAL.name()));
			gen.setField(PROJECT_EXACT, BaseCounter.getCount(res, MatchType.EXACT.name()));
			gen.setField(PROJECT_EXACT_TEXT_ONLY_PREVIOUS_VERSION, BaseCounter.getCount(res, MatchType.EXACT_TEXT_ONLY_PREVIOUS_VERSION.name()));
			gen.setField(PROJECT_EXACT_TEXT_ONLY_UNIQUE_ID, BaseCounter.getCount(res, MatchType.EXACT_TEXT_ONLY_UNIQUE_ID.name()));
			gen.setField(PROJECT_EXACT_TEXT_ONLY, BaseCounter.getCount(res, MatchType.EXACT_TEXT_ONLY.name()));
			gen.setField(PROJECT_EXACT_REPAIRED, BaseCounter.getCount(res, MatchType.EXACT_REPAIRED.name()));
			gen.setField(PROJECT_FUZZY_PREVIOUS_VERSION, BaseCounter.getCount(res, MatchType.FUZZY_PREVIOUS_VERSION.name()));
			gen.setField(PROJECT_FUZZY_UNIQUE_ID, BaseCounter.getCount(res, MatchType.FUZZY_UNIQUE_ID.name()));
			gen.setField(PROJECT_FUZZY, BaseCounter.getCount(res, MatchType.FUZZY.name()));
			gen.setField(PROJECT_FUZZY_REPAIRED, BaseCounter.getCount(res, MatchType.FUZZY_REPAIRED.name()));
			gen.setField(PROJECT_PHRASE_ASSEMBLED, BaseCounter.getCount(res, MatchType.PHRASE_ASSEMBLED.name()));
			gen.setField(PROJECT_MT, BaseCounter.getCount(res, MatchType.MT.name()));
			gen.setField(PROJECT_CONCORDANCE, BaseCounter.getCount(res, MatchType.CONCORDANCE.name()));
			gen.setField(PROJECT_NOCATEGORY, BaseCounter.getCount(res, GMX.TotalWordCount) - countCategories(res, CategoryGroup.OKAPI_WORD_COUNTS));
			
			gen.setField(PROJECT_EXACT_UNIQUE_ID_CHARACTERS, BaseCounter.getCount(res, MatchType.EXACT_UNIQUE_ID.name() + "_CHARACTERS"));
			gen.setField(PROJECT_EXACT_PREVIOUS_VERSION_CHARACTERS, BaseCounter.getCount(res, MatchType.EXACT_PREVIOUS_VERSION.name() + "_CHARACTERS"));
			gen.setField(PROJECT_EXACT_LOCAL_CONTEXT_CHARACTERS, BaseCounter.getCount(res, MatchType.EXACT_LOCAL_CONTEXT.name() + "_CHARACTERS"));
			gen.setField(PROJECT_EXACT_DOCUMENT_CONTEXT_CHARACTERS, BaseCounter.getCount(res, MatchType.EXACT_DOCUMENT_CONTEXT.name() + "_CHARACTERS"));
			gen.setField(PROJECT_EXACT_STRUCTURAL_CHARACTERS, BaseCounter.getCount(res, MatchType.EXACT_STRUCTURAL.name() + "_CHARACTERS"));
			gen.setField(PROJECT_EXACT_CHARACTERS, BaseCounter.getCount(res, MatchType.EXACT.name() + "_CHARACTERS"));
			gen.setField(PROJECT_EXACT_TEXT_ONLY_PREVIOUS_VERSION_CHARACTERS, BaseCounter.getCount(res, MatchType.EXACT_TEXT_ONLY_PREVIOUS_VERSION.name() + "_CHARACTERS"));
			gen.setField(PROJECT_EXACT_TEXT_ONLY_UNIQUE_ID_CHARACTERS, BaseCounter.getCount(res, MatchType.EXACT_TEXT_ONLY_UNIQUE_ID.name() + "_CHARACTERS"));
			gen.setField(PROJECT_EXACT_TEXT_ONLY_CHARACTERS, BaseCounter.getCount(res, MatchType.EXACT_TEXT_ONLY.name() + "_CHARACTERS"));
			gen.setField(PROJECT_EXACT_REPAIRED_CHARACTERS, BaseCounter.getCount(res, MatchType.EXACT_REPAIRED.name() + "_CHARACTERS"));
			gen.setField(PROJECT_FUZZY_PREVIOUS_VERSION_CHARACTERS, BaseCounter.getCount(res, MatchType.FUZZY_PREVIOUS_VERSION.name() + "_CHARACTERS"));
			gen.setField(PROJECT_FUZZY_UNIQUE_ID_CHARACTERS, BaseCounter.getCount(res, MatchType.FUZZY_UNIQUE_ID.name() + "_CHARACTERS"));
			gen.setField(PROJECT_FUZZY_CHARACTERS, BaseCounter.getCount(res, MatchType.FUZZY.name() + "_CHARACTERS"));
			gen.setField(PROJECT_FUZZY_REPAIRED_CHARACTERS, BaseCounter.getCount(res, MatchType.FUZZY_REPAIRED.name() + "_CHARACTERS"));
			gen.setField(PROJECT_PHRASE_ASSEMBLED_CHARACTERS, BaseCounter.getCount(res, MatchType.PHRASE_ASSEMBLED.name() + "_CHARACTERS"));
			gen.setField(PROJECT_MT_CHARACTERS, BaseCounter.getCount(res, MatchType.MT.name() + "_CHARACTERS"));
			gen.setField(PROJECT_CONCORDANCE_CHARACTERS, BaseCounter.getCount(res, MatchType.CONCORDANCE.name() + "_CHARACTERS"));
			gen.setField(PROJECT_NOCATEGORY_CHARACTERS, BaseCounter.getCount(res, GMX.TotalCharacterCount) - countCategories(res, CategoryGroup.OKAPI_CHARACTER_COUNTS));
			
			setProjectFields(gen, res);
		}		
		// Generate report
		String outPath = params.getOutputPath();
		if ( outPath != null && !outPath.isEmpty() ) {
			String report = gen.generate();
			outPath = Util.fillRootDirectoryVariable(params.getOutputPath(), rootDir);
			outPath = LocaleId.replaceVariables(outPath, getSourceLocale(), getTargetLocale());
			File outFile = new File(outPath);
			Util.createDirectories(outFile.getAbsolutePath());
			
			BufferedWriter writer;
			try {
				logger.info("Outputting scoping report to: " + outPath);
				writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), "UTF-8"));
				writer.write(report);
				writer.close();
			} catch (UnsupportedEncodingException e) {
				throw new OkapiIOException(e);
			} catch (FileNotFoundException e) {
				throw new OkapiIOException(e);
			} catch (IOException e) {
				throw new OkapiIOException(e);
			}
		}
		
		return super.handleEndBatch(event);
	}

	protected long countNonTranslatable(IWithAnnotations res, CategoryGroup group) {		
		long count = 0;
		
		switch (group) {
		case GMX_WORD_COUNTS:
			if (params.isCountAsNonTranslatable_GMXProtected()) 
				count += BaseCounter.getCount(res, GMX.ProtectedWordCount);
			
			if (params.isCountAsNonTranslatable_GMXExactMatched()) 
				count += BaseCounter.getCount(res, GMX.ExactMatchedWordCount);
			
			if (params.isCountAsNonTranslatable_GMXLeveragedMatched()) 
				count += BaseCounter.getCount(res, GMX.LeveragedMatchedWordCount);
			
			if (params.isCountAsNonTranslatable_GMXRepetitionMatched()) 
				count += BaseCounter.getCount(res, GMX.RepetitionMatchedWordCount);
			
			if (params.isCountAsNonTranslatable_GMXFuzzyMatch()) 
				count += BaseCounter.getCount(res, GMX.FuzzyMatchedWordCount);
			
			if (params.isCountAsNonTranslatable_GMXAlphanumericOnlyTextUnit()) 
				count += BaseCounter.getCount(res, GMX.AlphanumericOnlyTextUnitWordCount);
			
			if (params.isCountAsNonTranslatable_GMXNumericOnlyTextUnit()) 
				count += BaseCounter.getCount(res, GMX.NumericOnlyTextUnitWordCount);
			
			if (params.isCountAsNonTranslatable_GMXMeasurementOnlyTextUnit()) 
				count += BaseCounter.getCount(res, GMX.MeasurementOnlyTextUnitWordCount);
			break;
			
		case GMX_CHARACTER_COUNTS:
			if (params.isCountAsNonTranslatable_GMXProtected()) 
				count += BaseCounter.getCount(res, GMX.ProtectedCharacterCount);
			
			if (params.isCountAsNonTranslatable_GMXExactMatched()) 
				count += BaseCounter.getCount(res, GMX.ExactMatchedCharacterCount);
			
			if (params.isCountAsNonTranslatable_GMXLeveragedMatched()) 
				count += BaseCounter.getCount(res, GMX.LeveragedMatchedCharacterCount);
			
			if (params.isCountAsNonTranslatable_GMXRepetitionMatched()) 
				count += BaseCounter.getCount(res, GMX.RepetitionMatchedCharacterCount);
			
			if (params.isCountAsNonTranslatable_GMXFuzzyMatch()) 
				count += BaseCounter.getCount(res, GMX.FuzzyMatchedCharacterCount);
			
			if (params.isCountAsNonTranslatable_GMXAlphanumericOnlyTextUnit()) 
				count += BaseCounter.getCount(res, GMX.AlphanumericOnlyTextUnitCharacterCount);
			
			if (params.isCountAsNonTranslatable_GMXNumericOnlyTextUnit()) 
				count += BaseCounter.getCount(res, GMX.NumericOnlyTextUnitCharacterCount);
			
			if (params.isCountAsNonTranslatable_GMXMeasurementOnlyTextUnit()) 
				count += BaseCounter.getCount(res, GMX.MeasurementOnlyTextUnitCharacterCount);
			break;

		case OKAPI_WORD_COUNTS:
			if (params.isCountAsNonTranslatable_ExactUniqueIdMatch()) 
				count += BaseCounter.getCount(res, MatchType.EXACT_UNIQUE_ID.name());
			
			if (params.isCountAsNonTranslatable_ExactPreviousVersionMatch()) 
				count += BaseCounter.getCount(res, MatchType.EXACT_PREVIOUS_VERSION.name());
			
			if (params.isCountAsNonTranslatable_ExactLocalContextMatch()) 
				count += BaseCounter.getCount(res, MatchType.EXACT_LOCAL_CONTEXT.name());
			
			if (params.isCountAsNonTranslatable_ExactDocumentContextMatch()) 
				count += BaseCounter.getCount(res, MatchType.EXACT_DOCUMENT_CONTEXT.name());
			
			if (params.isCountAsNonTranslatable_ExactStructuralMatch()) 
				count += BaseCounter.getCount(res, MatchType.EXACT_STRUCTURAL.name());
			
			if (params.isCountAsNonTranslatable_ExactMatch()) 
				count += BaseCounter.getCount(res, MatchType.EXACT.name());
			
			if (params.isCountAsNonTranslatable_ExactTextOnlyPreviousVersionMatch()) 
				count += BaseCounter.getCount(res, MatchType.EXACT_TEXT_ONLY_PREVIOUS_VERSION.name());
			
			if (params.isCountAsNonTranslatable_ExactTextOnlyUniqueIdMatch()) 
				count += BaseCounter.getCount(res, MatchType.EXACT_TEXT_ONLY_UNIQUE_ID.name());
			
			if (params.isCountAsNonTranslatable_ExactTextOnly()) 
				count += BaseCounter.getCount(res, MatchType.EXACT_TEXT_ONLY.name());
			
			if (params.isCountAsNonTranslatable_ExactRepaired()) 
				count += BaseCounter.getCount(res, MatchType.EXACT_REPAIRED.name());
			
			if (params.isCountAsNonTranslatable_FuzzyPreviousVersionMatch()) 
				count += BaseCounter.getCount(res, MatchType.FUZZY_PREVIOUS_VERSION.name());
			
			if (params.isCountAsNonTranslatable_FuzzyUniqueIdMatch()) 
				count += BaseCounter.getCount(res, MatchType.FUZZY_UNIQUE_ID.name());
			
			if (params.isCountAsNonTranslatable_FuzzyMatch()) 
				count += BaseCounter.getCount(res, MatchType.FUZZY.name());
			
			if (params.isCountAsNonTranslatable_FuzzyRepaired()) 
				count += BaseCounter.getCount(res, MatchType.FUZZY_REPAIRED.name());
			
			if (params.isCountAsNonTranslatable_PhraseAssembled()) 
				count += BaseCounter.getCount(res, MatchType.PHRASE_ASSEMBLED.name());
			
			if (params.isCountAsNonTranslatable_MT()) 
				count += BaseCounter.getCount(res, MatchType.MT.name());
			
			if (params.isCountAsNonTranslatable_Concordance()) 
				count += BaseCounter.getCount(res, MatchType.CONCORDANCE.name());
			break;
			
		case OKAPI_CHARACTER_COUNTS:
			if (params.isCountAsNonTranslatable_ExactUniqueIdMatch()) 
				count += BaseCounter.getCount(res, MatchType.EXACT_UNIQUE_ID.name() + "_CHARACTERS");
			
			if (params.isCountAsNonTranslatable_ExactPreviousVersionMatch()) 
				count += BaseCounter.getCount(res, MatchType.EXACT_PREVIOUS_VERSION.name() + "_CHARACTERS");
			
			if (params.isCountAsNonTranslatable_ExactLocalContextMatch()) 
				count += BaseCounter.getCount(res, MatchType.EXACT_LOCAL_CONTEXT.name() + "_CHARACTERS");
			
			if (params.isCountAsNonTranslatable_ExactDocumentContextMatch()) 
				count += BaseCounter.getCount(res, MatchType.EXACT_DOCUMENT_CONTEXT.name() + "_CHARACTERS");
			
			if (params.isCountAsNonTranslatable_ExactStructuralMatch()) 
				count += BaseCounter.getCount(res, MatchType.EXACT_STRUCTURAL.name() + "_CHARACTERS");
			
			if (params.isCountAsNonTranslatable_ExactMatch()) 
				count += BaseCounter.getCount(res, MatchType.EXACT.name() + "_CHARACTERS");
			
			if (params.isCountAsNonTranslatable_ExactTextOnlyPreviousVersionMatch()) 
				count += BaseCounter.getCount(res, MatchType.EXACT_TEXT_ONLY_PREVIOUS_VERSION.name() + "_CHARACTERS");
			
			if (params.isCountAsNonTranslatable_ExactTextOnlyUniqueIdMatch()) 
				count += BaseCounter.getCount(res, MatchType.EXACT_TEXT_ONLY_UNIQUE_ID.name() + "_CHARACTERS");
			
			if (params.isCountAsNonTranslatable_ExactTextOnly()) 
				count += BaseCounter.getCount(res, MatchType.EXACT_TEXT_ONLY.name() + "_CHARACTERS");
			
			if (params.isCountAsNonTranslatable_ExactRepaired()) 
				count += BaseCounter.getCount(res, MatchType.EXACT_REPAIRED.name() + "_CHARACTERS");
			
			if (params.isCountAsNonTranslatable_FuzzyPreviousVersionMatch()) 
				count += BaseCounter.getCount(res, MatchType.FUZZY_PREVIOUS_VERSION.name() + "_CHARACTERS");
			
			if (params.isCountAsNonTranslatable_FuzzyUniqueIdMatch()) 
				count += BaseCounter.getCount(res, MatchType.FUZZY_UNIQUE_ID.name() + "_CHARACTERS");
			
			if (params.isCountAsNonTranslatable_FuzzyMatch()) 
				count += BaseCounter.getCount(res, MatchType.FUZZY.name() + "_CHARACTERS");
			
			if (params.isCountAsNonTranslatable_FuzzyRepaired()) 
				count += BaseCounter.getCount(res, MatchType.FUZZY_REPAIRED.name() + "_CHARACTERS");
			
			if (params.isCountAsNonTranslatable_PhraseAssembled()) 
				count += BaseCounter.getCount(res, MatchType.PHRASE_ASSEMBLED.name() + "_CHARACTERS");
			
			if (params.isCountAsNonTranslatable_MT()) 
				count += BaseCounter.getCount(res, MatchType.MT.name() + "_CHARACTERS");
			
			if (params.isCountAsNonTranslatable_Concordance()) 
				count += BaseCounter.getCount(res, MatchType.CONCORDANCE.name() + "_CHARACTERS");
		}
		
		return count > 0 ? count : 0; // Negative values are not allowed
	}

	protected long countCategories(IWithAnnotations res, CategoryGroup group) {
		long count = 0;
		
		switch (group) {
		case GMX_WORD_COUNTS:
			count += BaseCounter.getCount(res, GMX.ProtectedWordCount);
			count += BaseCounter.getCount(res, GMX.ExactMatchedWordCount);
			count += BaseCounter.getCount(res, GMX.LeveragedMatchedWordCount);
			count += BaseCounter.getCount(res, GMX.RepetitionMatchedWordCount);
			count += BaseCounter.getCount(res, GMX.FuzzyMatchedWordCount);
			count += BaseCounter.getCount(res, GMX.AlphanumericOnlyTextUnitWordCount);
			count += BaseCounter.getCount(res, GMX.NumericOnlyTextUnitWordCount);
			count += BaseCounter.getCount(res, GMX.MeasurementOnlyTextUnitWordCount);
			break;
			
		case GMX_CHARACTER_COUNTS:
			count += BaseCounter.getCount(res, GMX.ProtectedCharacterCount);
			count += BaseCounter.getCount(res, GMX.ExactMatchedCharacterCount);
			count += BaseCounter.getCount(res, GMX.LeveragedMatchedCharacterCount);
			count += BaseCounter.getCount(res, GMX.RepetitionMatchedCharacterCount);
			count += BaseCounter.getCount(res, GMX.FuzzyMatchedCharacterCount);
			count += BaseCounter.getCount(res, GMX.AlphanumericOnlyTextUnitCharacterCount);
			count += BaseCounter.getCount(res, GMX.NumericOnlyTextUnitCharacterCount);
			count += BaseCounter.getCount(res, GMX.MeasurementOnlyTextUnitCharacterCount);
			break;

		case OKAPI_WORD_COUNTS:
			count += BaseCounter.getCount(res, MatchType.EXACT_UNIQUE_ID.name());
			count += BaseCounter.getCount(res, MatchType.EXACT_PREVIOUS_VERSION.name());
			count += BaseCounter.getCount(res, MatchType.EXACT_LOCAL_CONTEXT.name());
			count += BaseCounter.getCount(res, MatchType.EXACT_DOCUMENT_CONTEXT.name());
			count += BaseCounter.getCount(res, MatchType.EXACT_STRUCTURAL.name());
			count += BaseCounter.getCount(res, MatchType.EXACT.name());
			count += BaseCounter.getCount(res, MatchType.EXACT_TEXT_ONLY_PREVIOUS_VERSION.name());
			count += BaseCounter.getCount(res, MatchType.EXACT_TEXT_ONLY_UNIQUE_ID.name());
			count += BaseCounter.getCount(res, MatchType.EXACT_TEXT_ONLY.name());
			count += BaseCounter.getCount(res, MatchType.EXACT_REPAIRED.name());
			count += BaseCounter.getCount(res, MatchType.FUZZY_PREVIOUS_VERSION.name());
			count += BaseCounter.getCount(res, MatchType.FUZZY_UNIQUE_ID.name());
			count += BaseCounter.getCount(res, MatchType.FUZZY.name());
			count += BaseCounter.getCount(res, MatchType.FUZZY_REPAIRED.name());
			count += BaseCounter.getCount(res, MatchType.PHRASE_ASSEMBLED.name());
			count += BaseCounter.getCount(res, MatchType.MT.name());
			count += BaseCounter.getCount(res, MatchType.CONCORDANCE.name());
			break;

		case OKAPI_CHARACTER_COUNTS:
			count += BaseCounter.getCount(res, MatchType.EXACT_UNIQUE_ID.name() + "_CHARACTERS");
			count += BaseCounter.getCount(res, MatchType.EXACT_PREVIOUS_VERSION.name() + "_CHARACTERS");
			count += BaseCounter.getCount(res, MatchType.EXACT_LOCAL_CONTEXT.name() + "_CHARACTERS");
			count += BaseCounter.getCount(res, MatchType.EXACT_DOCUMENT_CONTEXT.name() + "_CHARACTERS");
			count += BaseCounter.getCount(res, MatchType.EXACT_STRUCTURAL.name() + "_CHARACTERS");
			count += BaseCounter.getCount(res, MatchType.EXACT.name() + "_CHARACTERS");
			count += BaseCounter.getCount(res, MatchType.EXACT_TEXT_ONLY_PREVIOUS_VERSION.name() + "_CHARACTERS");
			count += BaseCounter.getCount(res, MatchType.EXACT_TEXT_ONLY_UNIQUE_ID.name() + "_CHARACTERS");
			count += BaseCounter.getCount(res, MatchType.EXACT_TEXT_ONLY.name() + "_CHARACTERS");
			count += BaseCounter.getCount(res, MatchType.EXACT_REPAIRED.name() + "_CHARACTERS");
			count += BaseCounter.getCount(res, MatchType.FUZZY_PREVIOUS_VERSION.name() + "_CHARACTERS");
			count += BaseCounter.getCount(res, MatchType.FUZZY_UNIQUE_ID.name() + "_CHARACTERS");
			count += BaseCounter.getCount(res, MatchType.FUZZY.name() + "_CHARACTERS");
			count += BaseCounter.getCount(res, MatchType.FUZZY_REPAIRED.name() + "_CHARACTERS");
			count += BaseCounter.getCount(res, MatchType.PHRASE_ASSEMBLED.name() + "_CHARACTERS");
			count += BaseCounter.getCount(res, MatchType.MT.name() + "_CHARACTERS");
			count += BaseCounter.getCount(res, MatchType.CONCORDANCE.name() + "_CHARACTERS");
			break;
		}
		
		return count > 0 ? count : 0; // Negative values are not allowed
	}
	
	// To be overridden in subclasses
	protected void setProjectFields(ReportGenerator gen, IResource res) {
	}
	
	// To be overridden in subclasses
	protected void setItemFields(ReportGenerator gen, IWithAnnotations res) {
	}
	
	@Override
	protected Event handleEndBatchItem(Event event) {
		IWithAnnotations res = (IWithAnnotations) event.getResource();
		if (res != null) {
			//@			resolver.resolve(res, EventType.END_BATCH_ITEM);
			
			gen.setField(ITEM_TOTAL_WORD_COUNT, BaseCounter.getCount(res, GMX.TotalWordCount));
			gen.setField(ITEM_TOTAL_CHARACTER_COUNT, BaseCounter.getCount(res, GMX.TotalCharacterCount));
			gen.setField(ITEM_WHITESPACE_CHARACTER_COUNT, BaseCounter.getCount(res, GMX.WhiteSpaceCharacterCount));
			gen.setField(ITEM_PUNCTUATION_CHARACTER_COUNT, BaseCounter.getCount(res, GMX.PunctuationCharacterCount));
			gen.setField(ITEM_OVERALL_CHARACTER_COUNT, BaseCounter.getCount(res, GMX.OverallCharacterCount));
			
			long ntrCount = countNonTranslatable(res, CategoryGroup.OKAPI_WORD_COUNTS);
			gen.setField(ITEM_TRANSLATABLE_WORD_COUNT, Math.max(0, BaseCounter.getCount(res, GMX.TotalWordCount) - ntrCount));
			gen.setField(ITEM_NONTRANSLATABLE_WORD_COUNT, ntrCount);
			
			ntrCount = countNonTranslatable(res, CategoryGroup.OKAPI_CHARACTER_COUNTS);
			gen.setField(ITEM_TRANSLATABLE_CHARACTER_COUNT, Math.max(0, BaseCounter.getCount(res, GMX.TotalCharacterCount) - ntrCount));
			gen.setField(ITEM_NONTRANSLATABLE_CHARACTER_COUNT, ntrCount);
			
			ntrCount = countNonTranslatable(res, CategoryGroup.GMX_WORD_COUNTS);
			gen.setField(ITEM_GMX_TRANSLATABLE_WORD_COUNT, Math.max(0, BaseCounter.getCount(res, GMX.TotalWordCount) - ntrCount));
			gen.setField(ITEM_GMX_NONTRANSLATABLE_WORD_COUNT, ntrCount);
			
			ntrCount = countNonTranslatable(res, CategoryGroup.GMX_CHARACTER_COUNTS);
			gen.setField(ITEM_GMX_TRANSLATABLE_CHARACTER_COUNT, Math.max(0, BaseCounter.getCount(res, GMX.TotalCharacterCount) - ntrCount));
			gen.setField(ITEM_GMX_NONTRANSLATABLE_CHARACTER_COUNT, ntrCount);
			
			gen.setField(ITEM_GMX_PROTECTED_WORD_COUNT, BaseCounter.getCount(res, GMX.ProtectedWordCount));
			gen.setField(ITEM_GMX_EXACT_MATCHED_WORD_COUNT, BaseCounter.getCount(res, GMX.ExactMatchedWordCount));
			gen.setField(ITEM_GMX_LEVERAGED_MATCHED_WORD_COUNT, BaseCounter.getCount(res, GMX.LeveragedMatchedWordCount));
			gen.setField(ITEM_GMX_REPETITION_MATCHED_WORD_COUNT, BaseCounter.getCount(res, GMX.RepetitionMatchedWordCount));
			gen.setField(ITEM_GMX_FUZZY_MATCHED_WORD_COUNT, BaseCounter.getCount(res, GMX.FuzzyMatchedWordCount));
			gen.setField(ITEM_GMX_ALPHANUMERIC_ONLY_TEXT_UNIT_WORD_COUNT, BaseCounter.getCount(res, GMX.AlphanumericOnlyTextUnitWordCount));
			gen.setField(ITEM_GMX_NUMERIC_ONLY_TEXT_UNIT_WORD_COUNT, BaseCounter.getCount(res, GMX.NumericOnlyTextUnitWordCount));
			gen.setField(ITEM_GMX_MEASUREMENT_ONLY_TEXT_UNIT_WORD_COUNT, BaseCounter.getCount(res, GMX.MeasurementOnlyTextUnitWordCount));
			gen.setField(ITEM_GMX_NOCATEGORY, BaseCounter.getCount(res, GMX.TotalWordCount) - countCategories(res, CategoryGroup.GMX_WORD_COUNTS));
			
			gen.setField(ITEM_GMX_PROTECTED_CHARACTER_COUNT, BaseCounter.getCount(res, GMX.ProtectedCharacterCount));
			gen.setField(ITEM_GMX_EXACT_MATCHED_CHARACTER_COUNT, BaseCounter.getCount(res, GMX.ExactMatchedCharacterCount));
			gen.setField(ITEM_GMX_LEVERAGED_MATCHED_CHARACTER_COUNT, BaseCounter.getCount(res, GMX.LeveragedMatchedCharacterCount));
			gen.setField(ITEM_GMX_REPETITION_MATCHED_CHARACTER_COUNT, BaseCounter.getCount(res, GMX.RepetitionMatchedCharacterCount));
			gen.setField(ITEM_GMX_FUZZY_MATCHED_CHARACTER_COUNT, BaseCounter.getCount(res, GMX.FuzzyMatchedCharacterCount));
			gen.setField(ITEM_GMX_ALPHANUMERIC_ONLY_TEXT_UNIT_CHARACTER_COUNT, BaseCounter.getCount(res, GMX.AlphanumericOnlyTextUnitCharacterCount));
			gen.setField(ITEM_GMX_NUMERIC_ONLY_TEXT_UNIT_CHARACTER_COUNT, BaseCounter.getCount(res, GMX.NumericOnlyTextUnitCharacterCount));
			gen.setField(ITEM_GMX_MEASUREMENT_ONLY_TEXT_UNIT_CHARACTER_COUNT, BaseCounter.getCount(res, GMX.MeasurementOnlyTextUnitCharacterCount));
			gen.setField(ITEM_GMX_NOCATEGORY_CHARACTERS, BaseCounter.getCount(res, GMX.TotalCharacterCount) - countCategories(res, CategoryGroup.GMX_CHARACTER_COUNTS));
			
			gen.setField(ITEM_EXACT_UNIQUE_ID, BaseCounter.getCount(res, MatchType.EXACT_UNIQUE_ID.name()));
			gen.setField(ITEM_EXACT_PREVIOUS_VERSION, BaseCounter.getCount(res, MatchType.EXACT_PREVIOUS_VERSION.name()));
			gen.setField(ITEM_EXACT_LOCAL_CONTEXT, BaseCounter.getCount(res, MatchType.EXACT_LOCAL_CONTEXT.name()));
			gen.setField(ITEM_EXACT_DOCUMENT_CONTEXT, BaseCounter.getCount(res, MatchType.EXACT_DOCUMENT_CONTEXT.name()));
			gen.setField(ITEM_EXACT_STRUCTURAL, BaseCounter.getCount(res, MatchType.EXACT_STRUCTURAL.name()));
			gen.setField(ITEM_EXACT, BaseCounter.getCount(res, MatchType.EXACT.name()));
			gen.setField(ITEM_EXACT_TEXT_ONLY_PREVIOUS_VERSION, BaseCounter.getCount(res, MatchType.EXACT_TEXT_ONLY_PREVIOUS_VERSION.name()));
			gen.setField(ITEM_EXACT_TEXT_ONLY_UNIQUE_ID, BaseCounter.getCount(res, MatchType.EXACT_TEXT_ONLY_UNIQUE_ID.name()));
			gen.setField(ITEM_EXACT_TEXT_ONLY, BaseCounter.getCount(res, MatchType.EXACT_TEXT_ONLY.name()));
			gen.setField(ITEM_EXACT_REPAIRED, BaseCounter.getCount(res, MatchType.EXACT_REPAIRED.name()));
			gen.setField(ITEM_FUZZY_PREVIOUS_VERSION, BaseCounter.getCount(res, MatchType.FUZZY_PREVIOUS_VERSION.name()));
			gen.setField(ITEM_FUZZY_UNIQUE_ID, BaseCounter.getCount(res, MatchType.FUZZY_UNIQUE_ID.name()));
			gen.setField(ITEM_FUZZY, BaseCounter.getCount(res, MatchType.FUZZY.name()));
			gen.setField(ITEM_FUZZY_REPAIRED, BaseCounter.getCount(res, MatchType.FUZZY_REPAIRED.name()));
			gen.setField(ITEM_PHRASE_ASSEMBLED, BaseCounter.getCount(res, MatchType.PHRASE_ASSEMBLED.name()));
			gen.setField(ITEM_MT, BaseCounter.getCount(res, MatchType.MT.name()));
			gen.setField(ITEM_CONCORDANCE, BaseCounter.getCount(res, MatchType.CONCORDANCE.name()));
			gen.setField(ITEM_NOCATEGORY, BaseCounter.getCount(res, GMX.TotalWordCount) - countCategories(res, CategoryGroup.OKAPI_WORD_COUNTS));
			
			gen.setField(ITEM_EXACT_UNIQUE_ID_CHARACTERS, BaseCounter.getCount(res, MatchType.EXACT_UNIQUE_ID.name() + "_CHARACTERS"));
			gen.setField(ITEM_EXACT_PREVIOUS_VERSION_CHARACTERS, BaseCounter.getCount(res, MatchType.EXACT_PREVIOUS_VERSION.name() + "_CHARACTERS"));
			gen.setField(ITEM_EXACT_LOCAL_CONTEXT_CHARACTERS, BaseCounter.getCount(res, MatchType.EXACT_LOCAL_CONTEXT.name() + "_CHARACTERS"));
			gen.setField(ITEM_EXACT_DOCUMENT_CONTEXT_CHARACTERS, BaseCounter.getCount(res, MatchType.EXACT_DOCUMENT_CONTEXT.name() + "_CHARACTERS"));
			gen.setField(ITEM_EXACT_STRUCTURAL_CHARACTERS, BaseCounter.getCount(res, MatchType.EXACT_STRUCTURAL.name() + "_CHARACTERS"));
			gen.setField(ITEM_EXACT_CHARACTERS, BaseCounter.getCount(res, MatchType.EXACT.name() + "_CHARACTERS"));
			gen.setField(ITEM_EXACT_TEXT_ONLY_PREVIOUS_VERSION_CHARACTERS, BaseCounter.getCount(res, MatchType.EXACT_TEXT_ONLY_PREVIOUS_VERSION.name() + "_CHARACTERS"));
			gen.setField(ITEM_EXACT_TEXT_ONLY_UNIQUE_ID_CHARACTERS, BaseCounter.getCount(res, MatchType.EXACT_TEXT_ONLY_UNIQUE_ID.name() + "_CHARACTERS"));
			gen.setField(ITEM_EXACT_TEXT_ONLY_CHARACTERS, BaseCounter.getCount(res, MatchType.EXACT_TEXT_ONLY.name() + "_CHARACTERS"));
			gen.setField(ITEM_EXACT_REPAIRED_CHARACTERS, BaseCounter.getCount(res, MatchType.EXACT_REPAIRED.name() + "_CHARACTERS"));
			gen.setField(ITEM_FUZZY_PREVIOUS_VERSION_CHARACTERS, BaseCounter.getCount(res, MatchType.FUZZY_PREVIOUS_VERSION.name() + "_CHARACTERS"));
			gen.setField(ITEM_FUZZY_UNIQUE_ID_CHARACTERS, BaseCounter.getCount(res, MatchType.FUZZY_UNIQUE_ID.name() + "_CHARACTERS"));
			gen.setField(ITEM_FUZZY_CHARACTERS, BaseCounter.getCount(res, MatchType.FUZZY.name() + "_CHARACTERS"));
			gen.setField(ITEM_FUZZY_REPAIRED_CHARACTERS, BaseCounter.getCount(res, MatchType.FUZZY_REPAIRED.name() + "_CHARACTERS"));
			gen.setField(ITEM_PHRASE_ASSEMBLED_CHARACTERS, BaseCounter.getCount(res, MatchType.PHRASE_ASSEMBLED.name() + "_CHARACTERS"));
			gen.setField(ITEM_MT_CHARACTERS, BaseCounter.getCount(res, MatchType.MT.name() + "_CHARACTERS"));
			gen.setField(ITEM_CONCORDANCE_CHARACTERS, BaseCounter.getCount(res, MatchType.CONCORDANCE.name() + "_CHARACTERS"));
			gen.setField(ITEM_NOCATEGORY_CHARACTERS, BaseCounter.getCount(res, GMX.TotalCharacterCount) - countCategories(res, CategoryGroup.OKAPI_CHARACTER_COUNTS));
			
			setItemFields(gen, res);
		}			
		
		return super.handleEndBatchItem(event);
	}
		
	@Override
	protected Event handleStartDocument(Event event) {
		Event ev = super.handleStartDocument(event); // Sets srcLoc and other stuff
		
		StartDocument sd = (StartDocument) event.getResource();
		if (sd != null) {
			String fname = sd.getName();
			gen.setField(ITEM_NAME, new File(fname).getAbsolutePath());
		}		
		return ev;
	}
	
	@Override
	protected Event handleEndDocument (Event event) {
		// Src/target locale might have changed during processing of the document, so let's set them here and not in handleStartDocument()
		gen.setField(ITEM_SOURCE_LOCALE, getSourceLocale().toString());
		gen.setField(ITEM_TARGET_LOCALE, getTargetLocale().toString());
		return super.handleEndDocument(event);
	}
	
	@Override
	protected Event handleTextUnit (Event event) {
		//@		resolver.resolve(event.getTextUnit(), EventType.TEXT_UNIT);
		return super.handleTextUnit(event);
	}
	
	public ReportGenerator getReportGenerator () {
		return gen;
	}

}
