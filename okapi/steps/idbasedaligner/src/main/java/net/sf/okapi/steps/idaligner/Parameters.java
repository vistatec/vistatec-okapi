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

package net.sf.okapi.steps.idaligner;

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
	private static final String REPLACEWITHSOURCE = "replaceWithSource";
	private static final String COPYTOTARGET = "copyToTarget";
	private static final String STOREASALTTRANSLATION = "storeAsAltTranslation";
	private static final String SUPPRESSTUSWITHNOTARGET = "suppressTusWithNoTarget";
	private static final String USETEXTUNITIDS = "useTextUnitIds";

	public Parameters() {
		super();
	}

	public boolean getGenerateTMX() {
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
	
	public boolean getReplaceWithSource() {
		return getBoolean(REPLACEWITHSOURCE);
	}
	
	public void setReplaceWithSource(boolean replaceWithSource) {
		setBoolean(REPLACEWITHSOURCE, replaceWithSource);
	}

	public void setCopyToTarget(boolean copyToTarget) {
		setBoolean(COPYTOTARGET, copyToTarget);
	}

	public boolean isCopyToTarget() {
		return getBoolean(COPYTOTARGET);
	}
	
	/**
	 * @return the storeAsAltTranslation
	 */
	public boolean isStoreAsAltTranslation() {
		return getBoolean(STOREASALTTRANSLATION);
	}

	/**
	 * @param storeAsAltTranslation the storeAsAltTranslation to set
	 */
	public void setStoreAsAltTranslation(boolean storeAsAltTranslation) {
		setBoolean(STOREASALTTRANSLATION, storeAsAltTranslation);
	}

	public boolean isSuppressTusWithNoTarget() {
		return getBoolean(SUPPRESSTUSWITHNOTARGET);
	}

	public void setSuppressTusWithNoTarget(boolean suppressTusWithNoTarget) {
		setBoolean(SUPPRESSTUSWITHNOTARGET, suppressTusWithNoTarget);
	}

	public boolean isUseTextUnitIds() {
	    return getBoolean(USETEXTUNITIDS);
	}

	public void setUseTextUnitIds(boolean useTextUnitIds) {
	    setBoolean(USETEXTUNITIDS, useTextUnitIds);
	}

	@Override
	public void reset() {
		super.reset();
		setTmxOutputPath("aligned.tmx");
		setGenerateTMX(false);
		setReplaceWithSource(false);
		setCopyToTarget(false);
		setStoreAsAltTranslation(true);
		setSuppressTusWithNoTarget(false);
		setUseTextUnitIds(false);
	}

	@Override
	public ParametersDescription getParametersDescription() {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(GENERATETMX, "Generate a TMX file:",
				"If generateTMX is false generate bilingual TextUnits, otherwise (true) output a TMX file");
		desc.add(TMXOUTPUTPATH, "TMX output path", "Full path of the output TMX file");
		desc.add(REPLACEWITHSOURCE, "Fall back to source text", "If no target text is available, use the source text");
		desc.add(COPYTOTARGET, 
				"Copy to/over the target (WARNING: Copied target will not be segmented)", "Copy to/over the target (an annotation " +
				"will still be created if requested). WARNING: Copied target will not be segmented and any exisiting target will be lost.");
		desc.add(STOREASALTTRANSLATION, "Create an alternate translation annotation", "Store the matched target as an alternate translation "
			+ "so that subsequent steps can see it.");
		desc.add(SUPPRESSTUSWITHNOTARGET, "Suppress TUs with no target", "Do not pass on any TUs for which no targets could be found.");
		desc.add(USETEXTUNITIDS, "Align based on TextUnit IDs", "Align based on TextUnit IDs, rather than resource names.");
		return desc;
	}

	@Override
	public EditorDescription createEditorDescription(ParametersDescription paramsDesc) {
		EditorDescription desc = new EditorDescription("Id-based Aligner", true, false);
		CheckboxPart cbp = desc.addCheckboxPart(paramsDesc.get(GENERATETMX));
		PathInputPart pip = desc.addPathInputPart(paramsDesc.get(TMXOUTPUTPATH),
				"TMX Document to Generate", true);
		pip.setBrowseFilters("TMX Documents (*.tmx)\tAll Files (*.*)", "*.tmx\t*.*");
		pip.setWithLabel(false);
		pip.setMasterPart(cbp, true);

		desc.addSeparatorPart();
		desc.addCheckboxPart(paramsDesc.get(REPLACEWITHSOURCE));
		desc.addCheckboxPart(paramsDesc.get(COPYTOTARGET));
		desc.addCheckboxPart(paramsDesc.get(STOREASALTTRANSLATION));
		desc.addCheckboxPart(paramsDesc.get(SUPPRESSTUSWITHNOTARGET));
		desc.addCheckboxPart(paramsDesc.get(USETEXTUNITIDS));
		return desc;
	}
}
