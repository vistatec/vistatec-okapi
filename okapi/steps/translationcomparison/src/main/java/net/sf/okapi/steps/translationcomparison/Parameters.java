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

package net.sf.okapi.steps.translationcomparison;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.uidescription.CheckboxPart;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;
import net.sf.okapi.common.uidescription.PathInputPart;
import net.sf.okapi.common.uidescription.TextInputPart;

@EditorFor(Parameters.class)
public class Parameters extends StringParameters implements IEditorDescriptionProvider {

	public static final String GENERATETMX = "generateTMX";
	public static final String TMXPATH = "tmxPath"; 
	public static final String GENERATEHTML = "generateHTML";
	public static final String GENERICCODES = "genericCodes"; 
	public static final String AUTOOPEN = "autoOpen"; 
	public static final String TARGET2SUFFIX = "target2Suffix";
	public static final String TARGET3SUFFIX = "target3Suffix";
	public static final String DOCUMENT1LABEL = "document1Label";
	public static final String DOCUMENT2LABEL = "document2Label";
	public static final String DOCUMENT3LABEL = "document3Label";
	public static final String CASESENSITIVE = "caseSensitive";
	public static final String WHITESPACESENSITIVE = "whitespaceSensitive";
	public static final String PUNCTUATIONSENSITIVE = "punctuationSensitive";
	public static final String USEDATALOG = "useDataLog";
	public static final String DATALOGPATH = "dataLogPath";
	public static final String ALTTRANSORIGIN = "altTransOrigin";
	public static final String USEALTTRANS = "useAltTrans";
	
	public Parameters () {
		super();
	}
	
	public boolean isGenerateTMX () {
		return getBoolean(GENERATETMX);
	}

	public void setGenerateTMX (boolean generateTMX) {
		setBoolean(GENERATETMX, generateTMX);
	}

	public String getTmxPath () {
		return getString(TMXPATH);
	}

	public void setTmxPath (String tmxPath) {
		setString(TMXPATH, tmxPath);
	}

	public boolean isGenerateHTML () {
		return getBoolean(GENERATEHTML);
	}

	public void setGenerateHTML (boolean generateHTML) {
		setBoolean(GENERATEHTML, generateHTML);
	}

	public boolean getGenericCodes () {
		return getBoolean(GENERICCODES);
	}
	
	public void setGenericCodes (boolean genericCodes) {
		setBoolean(GENERICCODES, genericCodes);
	}
	
	public boolean isAutoOpen () {
		return getBoolean(AUTOOPEN);
	}

	public void setAutoOpen (boolean autoOpen) {
		setBoolean(AUTOOPEN, autoOpen);
	}

	public boolean isCaseSensitive () {
		return getBoolean(CASESENSITIVE);
	}

	public void setCaseSensitive (boolean caseSensitive) {
		setBoolean(CASESENSITIVE, caseSensitive);
	}

	public boolean isWhitespaceSensitive () {
		return getBoolean(WHITESPACESENSITIVE);
	}

	public void setWhitespaceSensitive (boolean whitespaceSensitive) {
		setBoolean(WHITESPACESENSITIVE, whitespaceSensitive);
	}

	public boolean isPunctuationSensitive () {
		return getBoolean(PUNCTUATIONSENSITIVE);
	}

	public void setPunctuationSensitive (boolean punctuationSensitive) {
		setBoolean(PUNCTUATIONSENSITIVE, punctuationSensitive);
	}

	public String getTarget2Suffix () {
		return getString(TARGET2SUFFIX);
	}

	public void setTarget2Suffix (String target2Suffix) {
		setString(TARGET2SUFFIX, target2Suffix);
	}

	public String getTarget3Suffix () {
		return getString(TARGET3SUFFIX);
	}

	public void setTarget3Suffix (String target3Suffix) {
		setString(TARGET3SUFFIX, target3Suffix);
	}

	public String getDocument1Label () {
		return getString(DOCUMENT1LABEL);
	}

	public void setDocument1Label (String document1Label) {
		setString(DOCUMENT1LABEL, document1Label);
	}

	public String getDocument2Label () {
		return getString(DOCUMENT2LABEL);
	}

	public void setDocument2Label (String document2Label) {
		setString(DOCUMENT2LABEL, document2Label);
	}

	public String getDocument3Label () {
		return getString(DOCUMENT3LABEL);
	}

	public void setDocument3Label (String document3Label) {
		setString(DOCUMENT3LABEL, document3Label);
	}
	
	public boolean getUseAltTrans () {
		return getBoolean(USEALTTRANS);
	}

	public void setUseAltTrans (boolean useAltTrans) {
		setBoolean(USEALTTRANS, useAltTrans);
	}
	
	public String getAltTransOrigin () {
		return getString(ALTTRANSORIGIN);
	}

	public void setAltTransOrigin (String altTransOrigin) {
		setString(ALTTRANSORIGIN, altTransOrigin);
	}
	
	public boolean getUseDataLog () {
		return getBoolean(USEDATALOG);
	}

	public void setUseDataLog (boolean useDataLog) {
		setBoolean(USEDATALOG, useDataLog);
	}
	
	public String getDataLogPath () {
		return getString(DATALOGPATH);
	}

	public void setDataLogPath (String dataLogPath) {
		setString(DATALOGPATH, dataLogPath);
	}
	
	@Override
	public void reset() {
		super.reset();
		setGenerateTMX(false);
		setTmxPath("comparison.tmx");
		setGenerateHTML(true);
		setAutoOpen(true);
		setGenericCodes(true);
		setCaseSensitive(true);
		setWhitespaceSensitive(true);
		setPunctuationSensitive(true);
		setTarget2Suffix("-t2");
		setTarget3Suffix("-t3");
		setDocument1Label("Trans1");
		setDocument2Label("Trans2");
		setDocument3Label("Trans3");
		setUseDataLog(false);
		setDataLogPath("");
		setUseAltTrans(false);
		setAltTransOrigin("BING");
	}

	@Override
	public ParametersDescription getParametersDescription () {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(GENERATETMX,
			"Generate a TMX output document", "Generates an output document in TMX");
		desc.add(TMXPATH,
			"TMX output path", "Full path of the output TMX file");
		desc.add(GENERATEHTML,
			"Generate output tables in HTML", "Generates output tables in HTML");
		desc.add(GENERICCODES,
			"Use generic representation (e.g. <1>...</1>) for the inline codes", null);
		desc.add(AUTOOPEN,
			"Opens the first HTML output after completion", null);
		
		desc.add(TARGET2SUFFIX,
			"Suffix for target language code of document 2", null);
		desc.add(TARGET3SUFFIX,
			"Suffix for target language code of document 3", null);
		
		desc.add(USEALTTRANS, "Use alt-trans for document 1",
			"Use the XLIFF alt-trans of the trans-unit");
		desc.add(ALTTRANSORIGIN, "Value in origin attribute", "Value of the origin attribute of the alt-trans to use");
			
		desc.add(DOCUMENT1LABEL,
			"Label for the document 1", null);
		desc.add(DOCUMENT2LABEL,
			"Label for the document 2", null);
		desc.add(DOCUMENT3LABEL,
			"Label for the document 3", null);
			
		desc.add(CASESENSITIVE,
			"Take into account case differences", "Takes into account case differences");
		desc.add(WHITESPACESENSITIVE,
			"Take into account whitespace differences", "Takes into account whitespace differences");
		desc.add(PUNCTUATIONSENSITIVE,
			"Take into account punctuation differences", "Takes into account punctuation differences");
		
		desc.add(USEDATALOG, "Append the average results to a log", null);
		desc.add(DATALOGPATH, "Log file", "Full path for the log file");
		return desc;
	}

	@Override
	public EditorDescription createEditorDescription(ParametersDescription paramsDesc) {
		EditorDescription desc = new EditorDescription("Translation Comparison", true, false);
		
		//TODO: "HTML Output" group
		CheckboxPart cbpHTML = desc.addCheckboxPart(paramsDesc.get(GENERATEHTML));
		desc.addCheckboxPart(paramsDesc.get(GENERICCODES)).setMasterPart(cbpHTML, true);
		desc.addCheckboxPart(paramsDesc.get(AUTOOPEN)).setMasterPart(cbpHTML, true);
		
		//TODO: "TMX Output" group
		CheckboxPart cbpTMX = desc.addCheckboxPart(paramsDesc.get(GENERATETMX));
		PathInputPart pip = desc.addPathInputPart(paramsDesc.get(TMXPATH), "TMX Document", true);
		pip.setBrowseFilters("TMX Documents (*.tmx)\tAll Files (*.*)", "*.tmx\t*.*");
		pip.setMasterPart(cbpTMX, true);
		pip.setWithLabel(false);
		TextInputPart tip = desc.addTextInputPart(paramsDesc.get(TARGET2SUFFIX));
		tip.setMasterPart(cbpTMX, true);
		tip = desc.addTextInputPart(paramsDesc.get(TARGET3SUFFIX));
		tip.setMasterPart(cbpTMX, true);
		
		CheckboxPart cbpUseAlt = desc.addCheckboxPart(paramsDesc.get(USEALTTRANS));
		TextInputPart tipAlt = desc.addTextInputPart(paramsDesc.get(ALTTRANSORIGIN));
		tipAlt.setMasterPart(cbpUseAlt, true);
		tipAlt.setVertical(false);
		
		// HTML group
		tip = desc.addTextInputPart(paramsDesc.get(DOCUMENT1LABEL));
		tip.setMasterPart(cbpHTML, true);
		tip.setVertical(false);
		tip = desc.addTextInputPart(paramsDesc.get(DOCUMENT2LABEL));
		tip.setMasterPart(cbpHTML, true);
		tip.setVertical(false);
		tip = desc.addTextInputPart(paramsDesc.get(DOCUMENT3LABEL));
		tip.setMasterPart(cbpHTML, true);
		tip.setVertical(false);
		
		//TODO: "Comparison Options" group
		desc.addCheckboxPart(paramsDesc.get(CASESENSITIVE));
		desc.addCheckboxPart(paramsDesc.get(WHITESPACESENSITIVE));
		desc.addCheckboxPart(paramsDesc.get(PUNCTUATIONSENSITIVE));
		
		CheckboxPart cbp = desc.addCheckboxPart(paramsDesc.get(USEDATALOG));
		pip = desc.addPathInputPart(paramsDesc.get(DATALOGPATH), "Data Log File", true);
		pip.setBrowseFilters("Tab-Delimited Files (*.txt;*.log)\tAll Files (*.*)", "*.txt|*.log\t*.*");
		pip.setMasterPart(cbp, true);
		pip.setWithLabel(false);
		
		return desc;
	}

}
