/*===========================================================================
  Copyright (C) 2010-2011 by the Okapi Framework contributors
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

package net.sf.okapi.steps.termextraction;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.ReferenceParameter;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.uidescription.CheckboxPart;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;
import net.sf.okapi.common.uidescription.PathInputPart;
import net.sf.okapi.common.uidescription.SpinInputPart;

@EditorFor(Parameters.class)
public class Parameters extends StringParameters implements IEditorDescriptionProvider {
	
	private static final String OUTPUTPATH = "outputPath";
	private static final String AUTOOPEN = "autoOpen";
	private static final String MINWORDSPERTERM = "minWordsPerTerm";
	private static final String MAXWORDSPERTERM = "maxWordsPerTerm";
	private static final String MINOCCURRENCES = "minOccurrences";
	private static final String STOPWORDSPATH = "stopWordsPath";
	private static final String NOTSTARTWORDSPATH = "notStartWordsPath";
	private static final String NOTENDWORDSPATH = "notEndWordsPath";
	private static final String KEEPCASE = "keepCase";
	private static final String REMOVESUBTERMS = "removeSubTerms";
	private static final String SORTBYOCCURRENCE = "sortByOccurrence";
	private static final String USETERMINOLOGYANNOTATIONS = "useTerminologyAnnotations";
	private static final String USETEXTANALYSISANNOTATIONS = "useTextAnalysisAnnotations";
	private static final String USESTATISTICS = "useStatistics";
	
	public Parameters () {
		super();
	}
	
	public String getOutputPath () {
		return getString(OUTPUTPATH);
	}

	public void setOutputPath (String outputPath) {
		setString(OUTPUTPATH, outputPath);
	}

	public int getMinWordsPerTerm () {
		return getInteger(MINWORDSPERTERM);
	}

	public void setMinWordsPerTerm (int minWordsPerTerm) {
		setInteger(MINWORDSPERTERM, minWordsPerTerm);
	}

	public int getMaxWordsPerTerm () {
		return getInteger(MAXWORDSPERTERM);
	}

	public void setMaxWordsPerTerm (int maxWordsPerTerm) {
		setInteger(MAXWORDSPERTERM, maxWordsPerTerm);
	}

	public int getMinOccurrences () {
		return getInteger(MINOCCURRENCES);
	}

	public void setMinOccurrences (int minOccurrences) {
		setInteger(MINOCCURRENCES, minOccurrences);
	}

	@ReferenceParameter
	public String getStopWordsPath () {
		return getString(STOPWORDSPATH);
	}

	public void setStopWordsPath (String stopWordsPath) {
		setString(STOPWORDSPATH, stopWordsPath);
	}

	@ReferenceParameter
	public String getNotStartWordsPath () {
		return getString(NOTSTARTWORDSPATH);
	}

	public void setNotStartWordsPath (String notStartWordsPath) {
		setString(NOTSTARTWORDSPATH, notStartWordsPath);
	}

	@ReferenceParameter
	public String getNotEndWordsPath () {
		return getString(NOTENDWORDSPATH);
	}

	public void setNotEndWordsPath (String notEndWordsPath) {
		setString(NOTENDWORDSPATH, notEndWordsPath);
	}

	public boolean getKeepCase () {
		return getBoolean(KEEPCASE);
	}

	public void setKeepCase (boolean keepCase) {
		setBoolean(KEEPCASE, keepCase);
	}

	public boolean getRemoveSubTerms () {
		return getBoolean(REMOVESUBTERMS);
	}

	public void setRemoveSubTerms (boolean removeSubTerms) {
		setBoolean(REMOVESUBTERMS, removeSubTerms);
	}

	public boolean getSortByOccurrence () {
		return getBoolean(SORTBYOCCURRENCE);
	}

	public void setSortByOccurrence (boolean sortByOccurrence) {
		setBoolean(SORTBYOCCURRENCE, sortByOccurrence);
	}

	public boolean getAutoOpen () {
		return getBoolean(AUTOOPEN);
	}

	public void setAutoOpen (boolean autoOpen) {
		setBoolean(AUTOOPEN, autoOpen);
	}

	public boolean getUseTextAnalysisAnnotations () {
		return getBoolean(USETEXTANALYSISANNOTATIONS);
	}

	public void setUseTextAnalysisAnnotations (boolean useTextAnalysisAnnotations) {
		setBoolean(USETEXTANALYSISANNOTATIONS, useTextAnalysisAnnotations);
	}

	public boolean getUseTerminologyAnnotations () {
		return getBoolean(USETERMINOLOGYANNOTATIONS);
	}

	public void setUseTerminologyAnnotations (boolean useTerminologyAnnotations) {
		setBoolean(USETERMINOLOGYANNOTATIONS, useTerminologyAnnotations);
	}

	public boolean getUseStatistics () {
		return getBoolean(USESTATISTICS);
	}

	public void setUseStatistics (boolean useStatistics) {
		setBoolean(USESTATISTICS, useStatistics);
	}

	@Override
	public void reset () {
		super.reset();
		setOutputPath(Util.ROOT_DIRECTORY_VAR+"/terms.txt");
		setAutoOpen(false);
		setMinWordsPerTerm(1);
		setMaxWordsPerTerm(3);
		setMinOccurrences(2);
		setStopWordsPath("");
		setNotStartWordsPath("");
		setNotEndWordsPath("");
		setKeepCase(false);
		setRemoveSubTerms(false);
		setSortByOccurrence(false);
		setUseTerminologyAnnotations(true);
		setUseTextAnalysisAnnotations(true);
		setUseStatistics(true);
	}
	
	@Override
	public ParametersDescription getParametersDescription () {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(OUTPUTPATH, "Output path", "Full path of the output file");
		desc.add(MINWORDSPERTERM, "Minimum number of words per term", "A term will be made up at least of that many words");
		desc.add(MAXWORDSPERTERM, "Maximum number of words per term", "A term will be made up at the most of that many words");
		desc.add(MINOCCURRENCES, "Minimum number of occurrences per term", "A term will have at least that many occurrences");
		desc.add(STOPWORDSPATH, "Path of the file with stop words (leave empty for default)", "Full path of the file containing stop words");
		desc.add(NOTSTARTWORDSPATH, "Path of the file with not-start words (leave empty for default)", "Full path of the file containing not-start words");
		desc.add(NOTENDWORDSPATH, "Path of the file with not-end words (leave empty for default)", "Full path of the file containing not-end words");
		desc.add(KEEPCASE, "Preserve case differences", null);
		desc.add(REMOVESUBTERMS, "Remove entries that seem to be sub-strings of longer entries", null);
		desc.add(SORTBYOCCURRENCE, "Sort the results by the number of occurrences", null);
		desc.add(SORTBYOCCURRENCE, "Sort the results by the number of occurrences", null);
		desc.add(AUTOOPEN, "Open the result file after completion", null);
		desc.add(USETERMINOLOGYANNOTATIONS, "Use Terminology annotations", null);
		desc.add(USETEXTANALYSISANNOTATIONS, "Use Text Analysis annotations", null);
		desc.add(USESTATISTICS, "Use tokens-grouping statistics", null);
		return desc;
	}
	
	@Override
	public EditorDescription createEditorDescription (ParametersDescription paramsDesc) {
		EditorDescription desc = new EditorDescription("Term Extraction", true, false);

		PathInputPart pip = desc.addPathInputPart(paramsDesc.get(OUTPUTPATH), "Output File to Generate", true);
		pip.setBrowseFilters("Text Files (*.txt)\tAll Files (*.*)", "*.txt\t*.*");
		desc.addCheckboxPart(paramsDesc.get(AUTOOPEN));

		desc.addCheckboxPart(paramsDesc.get(SORTBYOCCURRENCE));

		desc.addSeparatorPart();
		
		desc.addCheckboxPart(paramsDesc.get(USETERMINOLOGYANNOTATIONS));
		desc.addCheckboxPart(paramsDesc.get(USETEXTANALYSISANNOTATIONS));

		CheckboxPart cbpUseStats = desc.addCheckboxPart(paramsDesc.get(USESTATISTICS));
		
		SpinInputPart sip = desc.addSpinInputPart(paramsDesc.get(MINWORDSPERTERM));
		sip.setRange(1, 999);
		sip.setVertical(false);
		sip.setMasterPart(cbpUseStats, true);
		
		sip = desc.addSpinInputPart(paramsDesc.get(MAXWORDSPERTERM));
		sip.setRange(1, 999);
		sip.setVertical(false);
		sip.setMasterPart(cbpUseStats, true);
		
		sip = desc.addSpinInputPart(paramsDesc.get(MINOCCURRENCES));
		sip.setRange(1, 999);
		sip.setVertical(false);
		sip.setMasterPart(cbpUseStats, true);
		
		CheckboxPart cbp = desc.addCheckboxPart(paramsDesc.get(KEEPCASE));
		cbp.setMasterPart(cbpUseStats, true);
		
		cbp = desc.addCheckboxPart(paramsDesc.get(REMOVESUBTERMS));
		cbp.setMasterPart(cbpUseStats, true);
		
		pip = desc.addPathInputPart(paramsDesc.get(STOPWORDSPATH), "Stop Words File", false);
		pip.setBrowseFilters("Text Files (*.txt)\tAll Files (*.*)", "*.txt\t*.*");
		pip.setAllowEmpty(true);
		pip.setMasterPart(cbpUseStats, true);

		pip = desc.addPathInputPart(paramsDesc.get(NOTSTARTWORDSPATH), "Not-Start Words File", false);
		pip.setBrowseFilters("Text Files (*.txt)\tAll Files (*.*)", "*.txt\t*.*");
		pip.setAllowEmpty(true);
		pip.setMasterPart(cbpUseStats, true);

		pip = desc.addPathInputPart(paramsDesc.get(NOTENDWORDSPATH), "Not-End Words File", false);
		pip.setBrowseFilters("Text Files (*.txt)\tAll Files (*.*)", "*.txt\t*.*");
		pip.setAllowEmpty(true);
		pip.setMasterPart(cbpUseStats, true);

		return desc;
	}

}
