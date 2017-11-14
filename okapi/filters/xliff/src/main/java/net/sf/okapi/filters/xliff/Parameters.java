/*===========================================================================
  Copyright (C) 2008-2014 by the Okapi Framework contributors
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
============================================================================*/

package net.sf.okapi.filters.xliff;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.ISimplifierRulesParameters;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.encoder.XMLEncoder;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.uidescription.CheckboxPart;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;
import net.sf.okapi.common.uidescription.ListSelectionPart;
import net.sf.okapi.core.simplifierrules.ParseException;
import net.sf.okapi.core.simplifierrules.SimplifierRules;

@EditorFor(Parameters.class)
public class Parameters extends StringParameters implements IEditorDescriptionProvider, ISimplifierRulesParameters {

	/**
	 * Use segmented target if the original trans-unit was segmented.
	 */
	public static final int SEGMENTATIONTYPE_ORIGINAL = 0;

	/**
	 * Always use segmented target regardless of the original trans-unit.
	 */
	public static final int SEGMENTATIONTYPE_SEGMENTED = 1;

	/**
	 * Never use segmentation in the output.
	 */
	public static final int SEGMENTATIONTYPE_NOTSEGMENTED = 2;

	/**
	 * Only use segments if we have more than one segment in the translation.
	 */
	public static final int SEGMENTATIONTYPE_ASNEEDED = 3;

	public static final int TARGETSTATEMODE_IGNORE = 0;
	public static final int TARGETSTATEMODE_EXTRACT = 1;
	public static final int TARGETSTATEMODE_DONOTEXTRACT = 2;

	public static final String ADDALTTRANS = "addAltTrans";
	public static final String ADDALTTRANSGMODE = "addAltTransGMode";
	public static final String EDITALTTRANS = "editAltTrans";
	
	private static final String USECUSTOMPARSER = "useCustomParser";
	private static final String FACTORYCLASS = "factoryClass";
	private static final String FALLBACKTOID = "fallbackToID";
	private static final String ADDTARGETLANGUAGE = "addTargetLanguage";
	private static final String OVERRIDETARGETLANGUAGE = "overrideTargetLanguage";
	private static final String ALLOWEMPTYTARGETS = "allowEmptyTargets";
	private static final String OUTPUTSEGMENTATIONTYPE = "outputSegmentationType";
	private static final String IGNOREINPUTSEGMENTATION = "ignoreInputSegmentation";
	private static final String INCLUDEEXTENSIONS = "includeExtensions";
	private static final String TARGETSTATEMODE = "targetStateMode";
	private static final String TARGETSTATEVALUE = "targetStateValue";
	private static final String INCLUDEITS = "includeIts";
	private static final String BALANCECODES = "balanceCodes";
	private static final String ALWAYSUSESEGSOURCE = "alwaysUseSegSource";
	private static final String PRESERVESPACEBYDEFAULT = "preserveSpaceByDefault";
	private static final String USESDLXLIFFWRITER = "useSdlXliffWriter";
	// SDLSEGLOCKEDVALUE could be a boolean but maybe there are other values besides true or false?
	private static final String SDLSEGLOCKEDVALUE = "sdlSegLockedValue";
	private static final String SDLSEGCONFVALUE = "sdlSegConfValue";
	private static final String SDLSEGORIGINVALUE = "sdlSegOriginValue";
	private static final String SKIPNOMRKSEGSOURCE = "skipNoMrkSegSource";

	private static final String INLINECDATA = "inlineCdata";

	public Parameters () {
		super();
	}

	public boolean getUseCustomParser() {
		return getBoolean(USECUSTOMPARSER);
	}

	public void setUseCustomParser(boolean useCustomParser) {
		setBoolean(USECUSTOMPARSER, useCustomParser);
	}

	public String getFactoryClass() {
		return getString(FACTORYCLASS);
	}

	public void setFactoryClass(String factoryClass) {
		setString(FACTORYCLASS, factoryClass);
	}
	
	public boolean getEscapeGT () {
		return getBoolean(XMLEncoder.ESCAPEGT);
	}

	public void setEscapeGT (boolean escapeGT) {
		setBoolean(XMLEncoder.ESCAPEGT, escapeGT);
	}

	public boolean getFallbackToID() {
		return getBoolean(FALLBACKTOID);
	}

	public void setFallbackToID(boolean fallbackToID) {
		setBoolean(FALLBACKTOID, fallbackToID);
	}

	public boolean getAddTargetLanguage () {
		return getBoolean(ADDTARGETLANGUAGE);
	}

	public void setAddTargetLanguage (boolean addTargetLanguage) {
		setBoolean(ADDTARGETLANGUAGE, addTargetLanguage);
	}
	
	public boolean getOverrideTargetLanguage () {
		return getBoolean(OVERRIDETARGETLANGUAGE);
	}

	public void setOverrideTargetLanguage (boolean overrideTargetLanguage) {
		setBoolean(OVERRIDETARGETLANGUAGE, overrideTargetLanguage);
	}
	
	public int getOutputSegmentationType () {
		return getInteger(OUTPUTSEGMENTATIONTYPE);
	}
	
	public void setOutputSegmentationType (int segmentationType) {
		setInteger(OUTPUTSEGMENTATIONTYPE, segmentationType);
	}

	public boolean getIgnoreInputSegmentation () {
		return getBoolean(IGNOREINPUTSEGMENTATION);
	}
	
	public void setIgnoreInputSegmentation (boolean ignoreInputSegmentation) {
		setBoolean(IGNOREINPUTSEGMENTATION, ignoreInputSegmentation);
	}

	public boolean getAddAltTrans () {
		return getBoolean(ADDALTTRANS);
	}
	
	public void setAddAltTrans (boolean addAltTrans) {
		setBoolean(ADDALTTRANS, addAltTrans);
	}

	public boolean getAddAltTransGMode () {
		return getBoolean(ADDALTTRANSGMODE);
	}
	
	public void setAddAltTransGMode (boolean addAltTransGMode) {
		setBoolean(ADDALTTRANSGMODE, addAltTransGMode);
	}

	public boolean getEditAltTrans () {
		return getBoolean(EDITALTTRANS);
	}
	
	public void setEditAltTrans (boolean editAltTrans) {
		setBoolean(EDITALTTRANS, editAltTrans);
	}

	public boolean getIncludeExtensions () {
		return getBoolean(INCLUDEEXTENSIONS);
	}
	
	public void setIncludeExtensions (boolean includeExtensions) {
		setBoolean(INCLUDEEXTENSIONS, includeExtensions);
	}
	
	public boolean getIncludeIts () {
		return getBoolean(INCLUDEITS);
	}
	
	public void setIncludeIts (boolean includeIts) {
		setBoolean(INCLUDEITS, includeIts);
	}
	
	public boolean getBalanceCodes () {
		return getBoolean(BALANCECODES);
	}
	
	public void setBalanceCodes (boolean balanceCodes) {
		setBoolean(BALANCECODES, balanceCodes);
	}
	
	public boolean getAllowEmptyTargets () {
		return getBoolean(ALLOWEMPTYTARGETS);
	}
	
	public void setAllowEmptyTargets (boolean allowEmptyTargets) {
		setBoolean(ALLOWEMPTYTARGETS, allowEmptyTargets);
	}
	
	public int getTargetStateMode () {
		return getInteger(TARGETSTATEMODE);
	}
	
	public void setTargetStateMode (int targetStateMode) {
		setInteger(TARGETSTATEMODE, targetStateMode);
	}

	public String getTargetStateValue () {
		return getString(TARGETSTATEVALUE);
	}
	
	public void setTargetStateValue (String targetStateValue) {
		setString(TARGETSTATEVALUE, targetStateValue);
	}
	
	public boolean getQuoteModeDefined () {
		return getBoolean(XMLEncoder.QUOTEMODEDEFINED);
	}
	
	public boolean isAlwaysUseSegSource() {
		return getBoolean(ALWAYSUSESEGSOURCE);
	}
	
	public void setAlwaysUseSegSource(boolean alwaysUSeSegSource) {
		setBoolean(ALWAYSUSESEGSOURCE, alwaysUSeSegSource);
	}

	public boolean isPreserveSpaceByDefault() {
		return getBoolean(PRESERVESPACEBYDEFAULT);
	}

	public void setPreserveSpaceByDefault(boolean preserveSpaceByDefault) {
		setBoolean(PRESERVESPACEBYDEFAULT, preserveSpaceByDefault);
	}

	// Not normally writable
	protected void setQuoteModeDefined(boolean defined) {
		setBoolean(XMLEncoder.QUOTEMODEDEFINED, defined);
	}
	
	public int getQuoteMode () {
		return getInteger(XMLEncoder.QUOTEMODE);
	}

	// Not normally writable
	protected void setQuoteMode(int quoteMode) {
		setInteger(XMLEncoder.QUOTEMODE, quoteMode);
	}
	
	public boolean isUseSdlXliffWriter() {
		return getBoolean(USESDLXLIFFWRITER);
	}
	
	public void setUseSdlXliffWriter(boolean useSdlXliffWriter) {
		setBoolean(USESDLXLIFFWRITER, useSdlXliffWriter);
	}
	
	public String getSdlSegLockedValue () {
		return getString(SDLSEGLOCKEDVALUE);
	}
	
	public void setSdlSegLockedValue (String sdlSegLockedvalue) {
		setString(SDLSEGLOCKEDVALUE, sdlSegLockedvalue);
	}
	
	public String getSdlSegConfValue () {
		return getString(SDLSEGCONFVALUE);
	}
	
	public void setSdlSegConfValue (String sdlSegConfvalue) {
		setString(SDLSEGCONFVALUE, sdlSegConfvalue);
	}
	
	public String getSdlSegOriginValue () {
		return getString(SDLSEGORIGINVALUE);
	}
	
	public void setSdlSegOriginValue (String sdlSegOriginvalue) {
		setString(SDLSEGORIGINVALUE, sdlSegOriginvalue);
	}

	public boolean isInlineCdata() {
		return getBoolean(INLINECDATA);
	}

	public void setInlineCdata(boolean inlineCdata) {
		setBoolean(INLINECDATA, inlineCdata);
	}

	public boolean getSkipNoMrkSegSource() {
		return getBoolean(SKIPNOMRKSEGSOURCE);
	}

	public void setSkipNoMrkSegSource(boolean skipNoMrkSegSource) {
		setBoolean(SKIPNOMRKSEGSOURCE, skipNoMrkSegSource);
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
	
	public void reset () {
		super.reset();
		setUseCustomParser(true);
		setFactoryClass("com.ctc.wstx.stax.WstxInputFactory"); // Woodstox XML parser
		setFallbackToID(false);
		setEscapeGT(false);
		setAddTargetLanguage(true);
		setOverrideTargetLanguage(false);
		setOutputSegmentationType(SEGMENTATIONTYPE_ORIGINAL);
		setIgnoreInputSegmentation(false);
		setAddAltTrans(false);
		setAddAltTransGMode(true);
		setEditAltTrans(false);
		setIncludeExtensions(true);
		setIncludeIts(true);
		setBalanceCodes(true);
		setAllowEmptyTargets(false);
		setTargetStateMode(TARGETSTATEMODE_IGNORE);
		setTargetStateValue("needs-translation");
		setAlwaysUseSegSource(false);
		
		setQuoteModeDefined(true);
		setQuoteMode(0); // no double or single quotes escaped
		setUseSdlXliffWriter(false);
		setPreserveSpaceByDefault(false);
		setSdlSegLockedValue(null); // default is use the original value
		setSdlSegConfValue(null);
		setSdlSegOriginValue(null);

		setInlineCdata(false);

		setSkipNoMrkSegSource(false);
		setSimplifierRules(null);
	}

	public void fromString (String data) {
		super.fromString(data);
	}

	@Override
	public String toString () {
		return buffer.toString();
	}
	
	@Override
	public ParametersDescription getParametersDescription () {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(USECUSTOMPARSER, "Use a custom XML stream parser", null);
		desc.add(FACTORYCLASS, "Factory class for the custom XML stream parser", null);
		desc.add(FALLBACKTOID, "Use the trans-unit id attribute for the text unit name if there is no resname", null);
		desc.add(IGNOREINPUTSEGMENTATION, "Ignore the segmentation information in the input", null);
		desc.add(ALWAYSUSESEGSOURCE, "Always use the content of seg-source even if there are differences between source", null);
		desc.add(USESDLXLIFFWRITER, "Use SDLXLIFF Writer", "If set: Update SDL non-standard metadata (e.g., draft to translated)");
		desc.add(XMLEncoder.ESCAPEGT, "Escape the greater-than characters", null);
		desc.add(ADDTARGETLANGUAGE, "Add the target-language attribute if not present", null);
		desc.add(OVERRIDETARGETLANGUAGE, "Override the target language of the XLIFF document", null);
		desc.add(ALLOWEMPTYTARGETS, "Allow empty <target> elements in XLIFF document", "If set: leave the <target> element empty, do not copy source text into it");
		desc.add(OUTPUTSEGMENTATIONTYPE, "Type of output segmentation", "Indicates wether to segment or not the text content in output");
		desc.add(ADDALTTRANS, "Allow addition of new <alt-trans> elements", "Indicates wether or not to adding new <alt-trans> elements is allowed");
		desc.add(ADDALTTRANSGMODE, "Use the <g> notation in new <alt-trans> elements", "Indicates wether or not to use the <g> notation in new <alt-trans> elements");
		desc.add(EDITALTTRANS, "Allow modification of existing <alt-trans> elements", "Indicates wether or not existing <alt-trans> elements can be modified");
		desc.add(INCLUDEEXTENSIONS, "Include extra information", "If set: non-standard information are included in the added <alt-trans>");
		desc.add(INCLUDEITS, "Include ITS markup", "If set: ITS markup is included");
		desc.add(BALANCECODES, "Balance codes", "If set: inline codes are balanced");		
		desc.add(TARGETSTATEMODE, "Action to do when the value of the state attribute matches the specified pattern", null);
		desc.add(TARGETSTATEVALUE, "Pattern for the state attribute value", null);
		desc.add(PRESERVESPACEBYDEFAULT, "Preserve whitespace by default", "When set, whitespace will be preserved even in the 'xml:space' attribute is not set.");
		desc.add(SDLSEGLOCKEDVALUE, "Default sdl:seg locked value for SDL XLIFF segments", "SDL XLIFF ONLY. Lock values are true, false or null to keep original value");
		desc.add(SDLSEGCONFVALUE, "Default sdl:seg conf value for SDL XLIFF segments", "SDL XLIFF ONLY. null value means keep the original value. Example conf values: \"Draft\", \"Translated\", \"ApprovedTranslation\" ");
		desc.add(INLINECDATA, "Preserve CDATA sections", "When set, CDATA sections will be preserved as inline codes.");
		desc.add(SKIPNOMRKSEGSOURCE, "Skip seg-sources with no marked segments", null);
		return desc;
	}

	public EditorDescription createEditorDescription (ParametersDescription paramDesc) {
		EditorDescription desc = new EditorDescription("XLIFF Filter Parameters", true, false);
		
		desc.addCheckboxPart(paramDesc.get(FALLBACKTOID));
		desc.addCheckboxPart(paramDesc.get(IGNOREINPUTSEGMENTATION));
		desc.addCheckboxPart(paramDesc.get(ALWAYSUSESEGSOURCE));
		desc.addCheckboxPart(paramDesc.get(USESDLXLIFFWRITER));

//Not implemented yet		
//		desc.addTextInputPart(paramDesc.get(TARGETSTATEVALUE));
//		
//		String[] values = {
//			String.valueOf(TARGETSTATEMODE_IGNORE),
//			String.valueOf(TARGETSTATEMODE_EXTRACT),
//			String.valueOf(TARGETSTATEMODE_DONOTEXTRACT)};
//		String[] labels = {
//			"Ignore the state attribute",
//			"Extract the matching entries (and only those entries)",
//			"Do not extract the matching entries",
//		};
//		ListSelectionPart lsp = desc.addListSelectionPart(paramDesc.get(TARGETSTATEMODE), values);
//		lsp.setChoicesLabels(labels);
		
		desc.addSeparatorPart();
		
		desc.addCheckboxPart(paramDesc.get(XMLEncoder.ESCAPEGT));
		desc.addCheckboxPart(paramDesc.get(ADDTARGETLANGUAGE));
		desc.addCheckboxPart(paramDesc.get(OVERRIDETARGETLANGUAGE));
		desc.addCheckboxPart(paramDesc.get(ALLOWEMPTYTARGETS));
		desc.addCheckboxPart(paramDesc.get(PRESERVESPACEBYDEFAULT));
		desc.addCheckboxPart(paramDesc.get(INLINECDATA));
		desc.addCheckboxPart(paramDesc.get(SKIPNOMRKSEGSOURCE));

		String[] values2 = {
			String.valueOf(SEGMENTATIONTYPE_ORIGINAL),
			String.valueOf(SEGMENTATIONTYPE_SEGMENTED),
			String.valueOf(SEGMENTATIONTYPE_NOTSEGMENTED),
			String.valueOf(SEGMENTATIONTYPE_ASNEEDED)};
		String[] labels2 = {
			"Segment only if the input text unit is segmented",
			"Always segment (even if the input text unit is not segmented)",
			"Never segment (even if the input text unit is segmented)",
			"Segment only if the entry is segmented and regardless how the input was"
		};
		ListSelectionPart lsp = desc.addListSelectionPart(paramDesc.get(OUTPUTSEGMENTATIONTYPE), values2);
		lsp.setChoicesLabels(labels2);
		
		desc.addCheckboxPart(paramDesc.get(INCLUDEITS));
		desc.addCheckboxPart(paramDesc.get(BALANCECODES));

		CheckboxPart cbp = desc.addCheckboxPart(paramDesc.get(ADDALTTRANS));
		desc.addCheckboxPart(paramDesc.get(INCLUDEEXTENSIONS)).setMasterPart(cbp, true);
		desc.addCheckboxPart(paramDesc.get(ADDALTTRANSGMODE)).setMasterPart(cbp, true);
		desc.addCheckboxPart(paramDesc.get(EDITALTTRANS)).setMasterPart(cbp, true);

		desc.addSeparatorPart();
		
		cbp = desc.addCheckboxPart(paramDesc.get(USECUSTOMPARSER));
		desc.addTextInputPart(paramDesc.get(FACTORYCLASS)).setMasterPart(cbp, true);
		
		return desc;
	}

}
