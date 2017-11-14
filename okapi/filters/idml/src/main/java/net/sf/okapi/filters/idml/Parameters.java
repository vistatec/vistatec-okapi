/*
 * =============================================================================
 *   Copyright (C) 2009-2017 by the Okapi Framework contributors
 * -----------------------------------------------------------------------------
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * =============================================================================
 */

package net.sf.okapi.filters.idml;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;

@EditorFor(Parameters.class)
public class Parameters extends StringParameters implements IEditorDescriptionProvider {

    /**
     * As there might be present attribute values which go beyond any limit (e.g. Preferences > PrintPreference > PrintRecord),
     * providing a parameter to adjust the default maximum attribute size.
     */
    private static final String MAX_ATTRIBUTE_SIZE = "maxAttributeSize";

    private static final String UNTAG_XML_STRUCTURES = "untagXmlStructures";
    private static final String EXTRACT_NOTES = "extractNotes";
    private static final String EXTRACT_MASTER_SPREADS = "extractMasterSpreads";
    private static final String EXTRACT_HIDDEN_LAYERS = "extractHiddenLayers";
    private static final String SKIP_DISCRETIONARY_HYPHENS = "skipDiscretionaryHyphens";

    public Parameters() {
        super();
    }

    public void reset() {
        super.reset();
        setMaxAttributeSize(4 * 1024 * 1024); // 4MB
        setUntagXmlStructures(true);
        setExtractNotes(false);
        setExtractMasterSpreads(true);
        setExtractHiddenLayers(false);
        setSkipDiscretionaryHyphens(false);
    }

    public int getMaxAttributeSize() {
        return getInteger(MAX_ATTRIBUTE_SIZE);
    }

    public void setMaxAttributeSize(int maxAttributeSize) {
        setInteger(MAX_ATTRIBUTE_SIZE, maxAttributeSize);
    }

    public boolean getUntagXmlStructures() {
        return getBoolean(UNTAG_XML_STRUCTURES);
    }

    public void setUntagXmlStructures(boolean untagXmlStructures) {
        setBoolean(UNTAG_XML_STRUCTURES, untagXmlStructures);
    }

    public boolean getExtractNotes() {
        return getBoolean(EXTRACT_NOTES);
    }

    public void setExtractNotes(boolean extractNotes) {
        setBoolean(EXTRACT_NOTES, extractNotes);
    }

    public boolean getExtractMasterSpreads() {
        return getBoolean(EXTRACT_MASTER_SPREADS);
    }

    public void setExtractMasterSpreads(boolean extractMasterSpreads) {
        setBoolean(EXTRACT_MASTER_SPREADS, extractMasterSpreads);
    }

    public boolean getExtractHiddenLayers() {
        return getBoolean(EXTRACT_HIDDEN_LAYERS);
    }

    public void setExtractHiddenLayers(boolean extractHiddenLayers) {
        setBoolean(EXTRACT_HIDDEN_LAYERS, extractHiddenLayers);
    }

    public void setSkipDiscretionaryHyphens(boolean skipDiscretionaryHyphens) {
        setBoolean(SKIP_DISCRETIONARY_HYPHENS, skipDiscretionaryHyphens);
    }

    public boolean getSkipDiscretionaryHyphens() {
        return getBoolean(SKIP_DISCRETIONARY_HYPHENS);
    }

    @Override
    public ParametersDescription getParametersDescription() {
        ParametersDescription desc = new ParametersDescription(this);

        desc.add(UNTAG_XML_STRUCTURES, "Untag XML structures", null);
        desc.add(EXTRACT_NOTES, "Extract notes", null);
        desc.add(EXTRACT_MASTER_SPREADS, "Extract master spreads", null);
        desc.add(EXTRACT_HIDDEN_LAYERS, "Extract hidden layers", null);
        desc.add(SKIP_DISCRETIONARY_HYPHENS, "Skip discretionary hyphens", null);

        return desc;
    }

    @Override
    public EditorDescription createEditorDescription(ParametersDescription paramsDesc) {
        EditorDescription desc = new EditorDescription("IDML Filter", true, false);

        desc.addCheckboxPart(paramsDesc.get(UNTAG_XML_STRUCTURES));
        desc.addCheckboxPart(paramsDesc.get(EXTRACT_NOTES));
        desc.addCheckboxPart(paramsDesc.get(EXTRACT_MASTER_SPREADS));
        desc.addCheckboxPart(paramsDesc.get(EXTRACT_HIDDEN_LAYERS));
        desc.addCheckboxPart(paramsDesc.get(SKIP_DISCRETIONARY_HYPHENS));

        return desc;
    }
}
