/*===========================================================================
  Copyright (C) 2009-2013 by the Okapi Framework contributors
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

package net.sf.okapi.steps.formatconversion;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.uidescription.CheckboxPart;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;
import net.sf.okapi.common.uidescription.ListSelectionPart;
import net.sf.okapi.common.uidescription.PathInputPart;

@EditorFor(Parameters.class)
public class Parameters extends StringParameters implements IEditorDescriptionProvider {

	public static final String FORMAT_TMX = "tmx";
	public static final String FORMAT_PO = "po";
	public static final String FORMAT_TABLE = "table";
	public static final String FORMAT_PENSIEVE = "pensieve";
	public static final String FORMAT_CORPUS = "corpus";
	public static final String FORMAT_WORDTABLE = "wordtable";
	
	public static final int TRG_TARGETOREMPTY = 0; 
	public static final int TRG_FORCESOURCE = 1; 
	public static final int TRG_FORCEEMPTY = 2; 

	private static final String SINGLEOUTPUT = "singleOutput";
	private static final String AUTOEXTENSIONS = "autoExtensions";
	private static final String OUTPUTPATH = "outputPath";
	private static final String TARGETSTYLE = "targetStyle";
	private static final String OUTPUTFORMAT = "outputFormat";
	private static final String FORMATOPTIONS = "formatOptions";
	private static final String USEGENERICCODES = "useGenericCodes";
	private static final String SKIPENTRIESWITHOUTTEXT = "skipEntriesWithoutText";
	private static final String APPROVEDENTRIESONLY = "approvedEntriesOnly";
	private static final String OVERWRITESAMESOURCE = "overwriteSameSource";
	
	private IFilterWriter writer;
	
	public Parameters () {
		super();
	}

	public int getTargetStyle () {
		return getInteger(TARGETSTYLE);
	}

	public void setTargetStyle (int targetStyle) {
		setInteger(TARGETSTYLE, targetStyle);
	}

	public boolean getSingleOutput () {
		return getBoolean(SINGLEOUTPUT);
	}

	public void setSingleOutput (boolean singleOutput) {
		setBoolean(SINGLEOUTPUT, singleOutput);
	}

	public boolean getAutoExtensions () {
		return getBoolean(AUTOEXTENSIONS);
	}

	public void setAutoExtensions (boolean autoExtensions) {
		setBoolean(AUTOEXTENSIONS, autoExtensions);
	}

	public String getOutputPath () {
		return getString(OUTPUTPATH);
	}

	public void setOutputPath (String outputPath) {
		setString(OUTPUTPATH, outputPath);
	}

	public String getOutputFormat () {
		return getString(OUTPUTFORMAT);
	}

	public void setOutputFormat (String outputFormat) {
		setString(OUTPUTFORMAT, outputFormat);
	}

	public boolean getUseGenericCodes () {
		return getBoolean(USEGENERICCODES);
	}

	public void setUseGenericCodes (boolean useGenericCodes) {
		setBoolean(USEGENERICCODES, useGenericCodes);
	}

	public String getFormatOptions () {
		return getGroup(FORMATOPTIONS);
	}

	public void setFormatOptions (String formatOptions) {
		setGroup(FORMATOPTIONS, formatOptions);
	}

	public boolean getSkipEntriesWithoutText () {
		return getBoolean(SKIPENTRIESWITHOUTTEXT);
	}

	public void setSkipEntriesWithoutText (boolean skipEntriesWithoutText) {
		setBoolean(SKIPENTRIESWITHOUTTEXT, skipEntriesWithoutText);
	}

	public boolean getApprovedEntriesOnly () {
		return getBoolean(APPROVEDENTRIESONLY);
	}

	public void setApprovedEntriesOnly (boolean approvedEntriesOnly) {
		setBoolean(APPROVEDENTRIESONLY, approvedEntriesOnly);
	}

	public boolean getOverwriteSameSource () {
		return getBoolean(OVERWRITESAMESOURCE);
	}
	
	public void setOverwriteSameSource (boolean overwriteSameSource) {
		setBoolean(OVERWRITESAMESOURCE, overwriteSameSource);
	}
	
	/**
	 * @return the writer
	 */
	public IFilterWriter getWriter() {
		return writer;
	}

	/**
	 * @param writer the writer to set
	 */
	public void setWriter(IFilterWriter writer) {
		this.writer = writer;
	}

	public void reset () {
		super.reset();
		setSingleOutput(true);
		setAutoExtensions(false);
		setTargetStyle(TRG_TARGETOREMPTY);
		setOutputPath("");
		setOutputFormat(FORMAT_TMX);
		setFormatOptions(null);
		setUseGenericCodes(false);
		setSkipEntriesWithoutText(true);
		setApprovedEntriesOnly(false);
		setOverwriteSameSource(false);

		writer = null;
	}

	@Override
	public ParametersDescription getParametersDescription () {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(SINGLEOUTPUT, "Create a single output document", null);
		desc.add(AUTOEXTENSIONS, "Output paths are the input paths plus the new format extension", null);
		desc.add(OUTPUTPATH, "Output path", "Full path of the single output document to generate");
		desc.add(OUTPUTFORMAT, "Output format", "Format to generate in output");
		desc.add(USEGENERICCODES, "Output generic inline codes", null);
		desc.add(TARGETSTYLE, "Target content", "Type of content to put in the target");
		desc.add(SKIPENTRIESWITHOUTTEXT, "Do not output entries without text", null);
		desc.add(APPROVEDENTRIESONLY, "Output only approved entries", null);
		desc.add(OVERWRITESAMESOURCE, "Overwrite if source is the same (for Pensieve TM)", null);
		desc.add(FORMATOPTIONS, "Format options", null); // Not used for display 
		return desc;
	}

	public EditorDescription createEditorDescription(ParametersDescription paramDesc) {
		EditorDescription desc = new EditorDescription("Format Conversion", true, false);

		String[] choices = {FORMAT_PO, FORMAT_TMX, FORMAT_TABLE,
			FORMAT_PENSIEVE, FORMAT_CORPUS, FORMAT_WORDTABLE};
		String[] choicesLabels = {"PO File", "TMX Document", "Tab-Delimited Table",
			"Pensieve TM", "Parallel Corpus Files", "Word Table"};
		ListSelectionPart lsp = desc.addListSelectionPart(paramDesc.get(OUTPUTFORMAT), choices);
		lsp.setChoicesLabels(choicesLabels);
		
		desc.addCheckboxPart(paramDesc.get(APPROVEDENTRIESONLY));
		desc.addCheckboxPart(paramDesc.get(USEGENERICCODES));
		desc.addCheckboxPart(paramDesc.get(OVERWRITESAMESOURCE));
		desc.addCheckboxPart(paramDesc.get(SKIPENTRIESWITHOUTTEXT));

		CheckboxPart cbp1 = desc.addCheckboxPart(paramDesc.get(SINGLEOUTPUT));
		PathInputPart pip = desc.addPathInputPart(paramDesc.get(OUTPUTPATH), "Output File", true);
		pip.setMasterPart(cbp1, true);
		
		CheckboxPart cbp2 = desc.addCheckboxPart(paramDesc.get(AUTOEXTENSIONS));
		cbp2.setMasterPart(cbp1, false);

		return desc;
	}

}
