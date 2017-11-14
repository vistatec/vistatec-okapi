/*===========================================================================
  Copyright (C) 2009-2012 by the Okapi Framework contributors
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

package net.sf.okapi.steps.sentencealigner;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.uidescription.CheckboxPart;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;
import net.sf.okapi.common.uidescription.PathInputPart;

@EditorFor(Parameters.class)
public class Parameters extends StringParameters implements IEditorDescriptionProvider {

	private static final String GENERATETMX = "generateTMX";
	private static final String TMXOUTPUTPATH = "tmxOutputPath";
	private static final String COLLAPSEWHITESPACE = "collapseWhitespace"; 
	private static final String OUTPUT_ONE_TO_ONE_MATCHES_ONLY = "outputOneTOneMatchesOnly";
	private static final String SEGMENTSOURCE = "segmentSource";
	private static final String USECUSTOMSOURCERULES = "useCustomSourceRules";
	private static final String CUSTOMSOURCERULESPATH = "customSourceRulesPath";
	private static final String SEGMENTTARGET = "segmentTarget";
	private static final String USECUSTOMTARGETRULES = "useCustomTargetRules";
	private static final String CUSTOMTARGETRULESPATH = "customTargetRulesPath";
	private static final String FORCESIMPLEONETOONEALIGNMENT = "forceSimpleOneToOneAlignment";

	public Parameters() {
		super();
	}

	public boolean isGenerateTMX() {
		return getBoolean(GENERATETMX);
	}

	public void setGenerateTMX(boolean generateTMX) {
		setBoolean(GENERATETMX, generateTMX);
	}

	public String getTmxOutputPath() {
		return getString(TMXOUTPUTPATH);
	}

	public void setTmxOutputPath(String tmxOutputPath) {
		setString(TMXOUTPUTPATH, tmxOutputPath);
	}

	public boolean isSegmentSource() {
		return getBoolean(SEGMENTSOURCE);
	}

	public void setSegmentSource(boolean segmentSource) {
		setBoolean(SEGMENTSOURCE, segmentSource);
	}

	public boolean isUseCustomSourceRules() {
		return getBoolean(USECUSTOMSOURCERULES);
	}

	public void setUseCustomSourceRules(boolean useCustomSourceRules) {
		setBoolean(USECUSTOMSOURCERULES, useCustomSourceRules);
	}

	public String getCustomSourceRulesPath() {
		return getString(CUSTOMSOURCERULESPATH);
	}

	public void setCustomSourceRulesPath(String customSourceRulesPath) {
		setString(CUSTOMSOURCERULESPATH, customSourceRulesPath);
	}

	public boolean isSegmentTarget() {
		return getBoolean(SEGMENTTARGET);
	}

	public void setSegmentTarget(boolean segmentTarget) {
		setBoolean(SEGMENTTARGET, segmentTarget);
	}

	public boolean isUseCustomTargetRules() {
		return getBoolean(USECUSTOMTARGETRULES);
	}

	public void setUseCustomTargetRules(boolean useCustomTargetRules) {
		setBoolean(USECUSTOMTARGETRULES, useCustomTargetRules);
	}

	public String getCustomTargetRulesPath() {
		return getString(CUSTOMTARGETRULESPATH);
	}

	public void setCustomTargetRulesPath(String customTargetRulesPath) {
		setString(CUSTOMTARGETRULESPATH, customTargetRulesPath);
	}

	public boolean isCollapseWhitespace() {
		return getBoolean(COLLAPSEWHITESPACE);
	}

	public void setCollapseWhitespace(boolean collapseWhitespace) {
		setBoolean(COLLAPSEWHITESPACE, collapseWhitespace);
	}

	public boolean isOutputOneTOneMatchesOnly() {
		return getBoolean(OUTPUT_ONE_TO_ONE_MATCHES_ONLY);
	}

	public void setOutputOneTOneMatchesOnly(boolean outputOneTOneMatchesOnly) {
		setBoolean(OUTPUT_ONE_TO_ONE_MATCHES_ONLY, outputOneTOneMatchesOnly);
	}
	
	public boolean isForceSimpleOneToOneAlignment() {
		return getBoolean(FORCESIMPLEONETOONEALIGNMENT);
	}

	public void setForceSimpleOneToOneAlignment(boolean forceSimpleOneToOneAlignment) {
		setBoolean(FORCESIMPLEONETOONEALIGNMENT, forceSimpleOneToOneAlignment);
	}

	@Override
	public void reset() {
		super.reset();
		setTmxOutputPath("aligned.tmx");
		setGenerateTMX(true);
		setSegmentSource(true);
		setUseCustomSourceRules(false);
		setCustomSourceRulesPath("");
		setSegmentTarget(true);
		setUseCustomTargetRules(false);
		setCustomTargetRulesPath("");
		setCollapseWhitespace(false);
		setOutputOneTOneMatchesOnly(false);
		setForceSimpleOneToOneAlignment(false);
	}

	@Override
	public ParametersDescription getParametersDescription() {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(GENERATETMX, "Generate the following TMX document:",
				"If generateTMX is false generate bilingual TextUnits, otherwise (true) output a TMX file");
		desc.add(TMXOUTPUTPATH, "TMX output path", "Full path of the output TMX file");

		desc.add("segmentSource",
				"Segment the source content (overriding possible existing segmentation)", null);
		desc.add("useCustomSourceRules",
				"Use custom source segmentation rules (instead of the default ones)", null);
		desc.add("customSourceRulesPath", "SRX path for the source",
				"Full path of the SRX document to use for the source");
		desc.add("segmentTarget",
				"Segment the target content (overriding possible existing segmentation)", null);
		desc.add("useCustomTargetRules",
				"Use custom target segmentation rules (instead of the default ones)", null);
		desc.add("customTargetRulesPath", "SRX path for the target",
				"Full path of the SRX document to use for the target");
		desc.add(COLLAPSEWHITESPACE, "Collapse whitspace", 
				"Collapse whitespace (space, newline etc.) to a single space before segmentation and alignment");
		desc.add(OUTPUT_ONE_TO_ONE_MATCHES_ONLY, "Output 1-1 matches only", 
			"Ouput only 1-1 sentence aligned matches");
		desc.add("forceSimpleOneToOneAlignment", "Force Simple One to One Alignment", 
				"If the number of sentences are the same then align one to one. Otherwise collapse the sentences and align");
		return desc;
	}

	@Override
	public EditorDescription createEditorDescription(ParametersDescription paramsDesc) {
		EditorDescription desc = new EditorDescription("Sentence Aligner", true, false);
		CheckboxPart cbp = desc.addCheckboxPart(paramsDesc.get(GENERATETMX));
		PathInputPart pip = desc.addPathInputPart(paramsDesc.get(TMXOUTPUTPATH),
				"TMX Document to Generate", true);
		pip.setBrowseFilters("TMX Documents (*.tmx)\tAll Files (*.*)", "*.tmx\t*.*");
		pip.setWithLabel(false);
		pip.setMasterPart(cbp, true);

		desc.addSeparatorPart();

		CheckboxPart cbp1 = desc.addCheckboxPart(paramsDesc.get("segmentSource"));
		CheckboxPart cbp2 = desc.addCheckboxPart(paramsDesc.get("useCustomSourceRules"));
		cbp2.setMasterPart(cbp1, true);
		pip = desc.addPathInputPart(paramsDesc.get("customSourceRulesPath"),
				"Segmentation Rules for Source", false);
		pip.setBrowseFilters("SRX Documents (*.srx)\tAll Files (*.*)", "*.srx\t*.*");
		pip.setWithLabel(false);
		pip.setMasterPart(cbp2, true);

		desc.addSeparatorPart();

		cbp1 = desc.addCheckboxPart(paramsDesc.get("segmentTarget"));
		cbp2 = desc.addCheckboxPart(paramsDesc.get("useCustomTargetRules"));
		cbp2.setMasterPart(cbp1, true);
		pip = desc.addPathInputPart(paramsDesc.get("customTargetRulesPath"),
				"Segmentation Rules for Target", false);
		pip.setBrowseFilters("SRX Documents (*.srx)\tAll Files (*.*)", "*.srx\t*.*");
		pip.setWithLabel(false);
		pip.setMasterPart(cbp2, true);
		
		desc.addSeparatorPart();
		desc.addCheckboxPart(paramsDesc.get(COLLAPSEWHITESPACE));		
		desc.addSeparatorPart();
		desc.addCheckboxPart(paramsDesc.get(OUTPUT_ONE_TO_ONE_MATCHES_ONLY));
		desc.addSeparatorPart();
		desc.addCheckboxPart(paramsDesc.get("forceSimpleOneToOneAlignment"));
		return desc;
	}
}
