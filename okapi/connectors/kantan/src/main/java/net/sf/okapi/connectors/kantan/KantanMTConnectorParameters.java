/*===========================================================================
  Copyright (C) 2015 by the Okapi Framework contributors
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

package net.sf.okapi.connectors.kantan;

import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;
import net.sf.okapi.common.uidescription.TextInputPart;

public class KantanMTConnectorParameters extends StringParameters implements IEditorDescriptionProvider {
    private static final String PROFILE_NAME = "profileName";
    private static final String API_TOKEN = "apiToken";

    public KantanMTConnectorParameters() {
        super();
    }

    public void setProfileName(String profileName) {
        setString(PROFILE_NAME, profileName);
    }

    public String getProfileName() {
        return getString(PROFILE_NAME);
    }

    public void setApiToken(String apiToken) {
        setString(API_TOKEN, apiToken);
    }

    public String getApiToken() {
        return getString(API_TOKEN);
    }

    @Override
    public void reset() {
        super.reset();
        setProfileName("");
        setApiToken("");
    }

    @Override
    public ParametersDescription getParametersDescription() {
        ParametersDescription desc = new ParametersDescription(this);
        desc.add(PROFILE_NAME,
                "KantanMT Client Profile",
                "Name of the KantanMT Client Profile"
        );
        desc.add(API_TOKEN,
                "KantanMT Authorization Token",
                "KantanMT API Authorization Token"
        );

        return desc;
    }

    @Override
    public EditorDescription createEditorDescription(ParametersDescription parametersDescription) {
        EditorDescription desc = new EditorDescription("KantanMT Connector Settings", true, false);
        desc.addTextInputPart(parametersDescription.get(PROFILE_NAME));
        TextInputPart tipSecret = desc.addTextInputPart(parametersDescription.get(API_TOKEN));
        tipSecret.setPassword(true);

        return desc;
    }
}
