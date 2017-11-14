/*===========================================================================
  Copyright (C) 2010 by the Okapi Framework contributors
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

package net.sf.okapi.filters.vignette;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.ISimplifierRulesParameters;
import net.sf.okapi.common.ListUtil;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.uidescription.CheckboxPart;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;
import net.sf.okapi.common.uidescription.TextInputPart;
import net.sf.okapi.core.simplifierrules.ParseException;
import net.sf.okapi.core.simplifierrules.SimplifierRules;

@EditorFor(Parameters.class)
public class Parameters extends StringParameters implements IEditorDescriptionProvider, ISimplifierRulesParameters {
	
	static final String PARTSCONFIGURATIONS = "partsConfigurations";
	static final String PARTSNAMES = "partsNames";
	static final String SOURCEID = "sourceId";
	static final String LOCALEID = "localeId";
	static final String QUOTEMODEDEFINED = "quoteModeDefined";
	static final String QUOTEMODE = "quoteMode";
	static final String MONOLINGUAL = "monolingual";
	static final String USECDATA = "useCDATA";

	public String getPartsNames () {
		return getString(PARTSNAMES);
	}

	public String[] getPartsNamesAsList () {
		return ListUtil.stringAsArray(getPartsNames());
	}

	public void setPartsNames (String partsNames) {
		setString(PARTSNAMES, partsNames);
	}

	public String getPartsConfigurations () {
		return getString(PARTSCONFIGURATIONS);
	}

	public String[] getPartsConfigurationsAsList () {
		return ListUtil.stringAsArray(getPartsConfigurations());
	}

	public void setPartsConfigurations (String partsConfigurations) {
		setString(PARTSCONFIGURATIONS, partsConfigurations);
	}
	
	public String getSourceId () {
		return getString(SOURCEID);
	}

	public void setSourceId (String sourceId) {
		setString(SOURCEID, sourceId);
	}

	public String getLocaleId () {
		return getString(LOCALEID);
	}

	public void setLocaleId (String localeId) {
		setString(LOCALEID, localeId);
	}

	public boolean getMonolingual () {
		return getBoolean(MONOLINGUAL);
	}

	public void setMonolingual (boolean monolingual) {
		setBoolean(MONOLINGUAL, monolingual);
	}
	
	public boolean getUseCDATA() {
		return getBoolean(USECDATA);
	}

	public void setUseCDATA(boolean useCDATA) {
		setBoolean(USECDATA, useCDATA);
	}
	
	@Override
	public String getSimplifierRules() {
		return getString(SIMPLIFIERRULES);
	}

	@Override
	public void setSimplifierRules(String rules) {
		setString(SIMPLIFIERRULES, rules);		
	}

	@Override
	public void validateSimplifierRules() throws ParseException {
		SimplifierRules r = new SimplifierRules(getSimplifierRules(), new Code());
		r.parse();
	}

	public boolean checkData () {
		String[] tmp1 = ListUtil.stringAsArray(getPartsNames());
		String[] tmp2 = ListUtil.stringAsArray(getPartsConfigurations());
		return (( tmp1.length > 0 ) && ( tmp1.length == tmp2.length ));
	}

	public Parameters () {
		super();
	}
	
	@Override
	public void reset () {
		super.reset();
		setPartsNames("SMCCONTENT-TITLE, SMCCONTENT-ABSTRACT, SMCCONTENT-BODY, SMCCONTENT-ALT, "
			+ "SMCCHANNELDESCRIPTOR-TITLE, SMCCHANNELDESCRIPTOR-ABSTRACT, SMCCHANNELDESCRIPTOR-ALT, "
			+ "SMCLINKCOLLECTIONS-LINKCOLLECTION-TITLE, SMCLINKCOLLECTIONS-LINKCOLLECTION-DESCRIPTION, "
			+ "SMCLINKS-TITLE, SMCLINKS-ABSTRACT, SMCLINKS-BODY, SMCLINKS-ALT");
		setPartsConfigurations("default, okf_html, okf_html, default, "
			+ "default, okf_html, default, "
			+ "default, okf_html, "
			+ "default, okf_html, okf_html, default");
		setSourceId("SOURCE_ID");
		setLocaleId("LOCALE_ID");
		setMonolingual(false);
		setUseCDATA(true);
		setSimplifierRules(null);
	}

	@Override
	public String toString () {
		// Plus two *write-only* parameters: always set to true and 0
		// This is used by the encoder to know how it needs to escape the quotes
		// It must not be 0 if one of the data part to extract is an attribute
		// here we can use 0 because all extracted text comes from elements.
		buffer.setBoolean(QUOTEMODEDEFINED, true);
		buffer.setInteger(QUOTEMODE, 0);
		return super.toString();
	}
	
	@Override
	public ParametersDescription getParametersDescription () {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(PARTSNAMES, "Names of the <attribute> elements to extract",
			"Comma-separated list of the names of the <attribute> elements to extract.");
		desc.add(PARTSCONFIGURATIONS, "Corresponding filter configurations (or 'default')",
			"Comma-separated list of the filter configurations to use, use 'default' for none");
		desc.add(MONOLINGUAL, "Monolingual mode", null);
		desc.add(USECDATA, "Use CDATA", 
				"Create CDATA sections in the output file");
		desc.add(SOURCEID, "Name for source ID element",
			"Name of the <attribute> element containing the source ID");
		desc.add(LOCALEID, "Name for locale ID element",
			"Name of the <attribute> element containing the locale ID");
		return desc;
	}

	@Override
	public EditorDescription createEditorDescription (ParametersDescription paramDesc) {
		EditorDescription desc = new EditorDescription("Vignette Filter Parameters", true, false);
		
		TextInputPart tip = desc.addTextInputPart(paramDesc.get(Parameters.PARTSNAMES));
		tip.setHeight(60);
		
		tip = desc.addTextInputPart(paramDesc.get(Parameters.PARTSCONFIGURATIONS));
		tip.setHeight(60);

		desc.addCheckboxPart(paramDesc.get(USECDATA));
		
		CheckboxPart mono = desc.addCheckboxPart(paramDesc.get(MONOLINGUAL));
		
		tip = desc.addTextInputPart(paramDesc.get(Parameters.SOURCEID));
		tip.setVertical(false);
		tip.setMasterPart(mono, false);
		
		tip = desc.addTextInputPart(paramDesc.get(Parameters.LOCALEID));
		tip.setVertical(false);
		tip.setMasterPart(mono, false);
		
		return desc;
	}

	
	
}
