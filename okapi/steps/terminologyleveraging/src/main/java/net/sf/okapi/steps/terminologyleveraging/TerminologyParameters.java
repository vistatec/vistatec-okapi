/*===========================================================================
  Copyright (C) 2017 by the Okapi Framework contributors
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

package net.sf.okapi.steps.terminologyleveraging;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;

/**
 * Provides parameters for terminology connectors.
 *
 * @author Vladyslav Mykhalets
 */
@EditorFor(TerminologyParameters.class)
public class TerminologyParameters extends StringParameters implements IEditorDescriptionProvider {

    private static final String LEVERAGE = "leverage";
    private static final String ANNOTATE_SOURCE = "annotateSource";
    private static final String ANNOTATE_TARGET = "annotateTarget";
    private static final String CONNECTOR_CLASSNAME = "connectorClassName";
    private static final String CONNECTOR_PARAMETERS = "connectorParameters";

    public TerminologyParameters() {
        super();
    }

    public boolean getLeverage() {
        return getBoolean(LEVERAGE);
    }

    public void setLeverage(boolean leverage) {
        setBoolean(LEVERAGE, leverage);
    }

    public boolean getAnnotateSource() {
        return getBoolean(ANNOTATE_SOURCE);
    }

    public void setAnnotateSource(boolean annotateSource) {
        setBoolean(ANNOTATE_SOURCE, annotateSource);
    }

    public boolean getAnnotateTarget() {
        return getBoolean(ANNOTATE_TARGET);
    }

    public void setAnnotateTarget(boolean annotateTarget) {
        setBoolean(ANNOTATE_TARGET, annotateTarget);
    }

    public String getConnectorClassName() {
        return getString(CONNECTOR_CLASSNAME);
    }

    public void setConnectorClassName(String connectorClassName) {
        setString(CONNECTOR_CLASSNAME, connectorClassName);
    }

    public String getConnectorParameters() {
        return getGroup(CONNECTOR_PARAMETERS);
    }

    public void setConnectorParameters(String connectorParameters) {
        setGroup(CONNECTOR_PARAMETERS, connectorParameters);
    }

    @Override
    public ParametersDescription getParametersDescription() {
        ParametersDescription desc = new ParametersDescription(this);
        desc.add(LEVERAGE, "Leverage status:", "Indication that leveraging should be done");
        desc.add(ANNOTATE_SOURCE, "Annotate source:", "Indication that source segment annotation should be done");
        desc.add(ANNOTATE_TARGET, "Annotate target", "Indication that target segment annotation should be done");
        desc.add(CONNECTOR_CLASSNAME, "Connector class", "Full class name of the terminology connector");
        desc.add(CONNECTOR_PARAMETERS, "Connector parameters",
                "String representation of parameters specific to terminology connector");
        return desc;
    }

    @Override
    public EditorDescription createEditorDescription(ParametersDescription parametersDescription) {
        EditorDescription desc = new EditorDescription("Terminology Leveraging", true, false);
        desc.addCheckboxPart(parametersDescription.get(LEVERAGE));
        desc.addCheckboxPart(parametersDescription.get(ANNOTATE_SOURCE));
        desc.addCheckboxPart(parametersDescription.get(ANNOTATE_TARGET));
        desc.addTextInputPart(parametersDescription.get(CONNECTOR_CLASSNAME));
        desc.addTextInputPart(parametersDescription.get(CONNECTOR_PARAMETERS));
        return desc;
    }
}
