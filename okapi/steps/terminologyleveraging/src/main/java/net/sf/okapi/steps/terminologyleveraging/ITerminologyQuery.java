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

import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.ITextUnit;

/**
 * Provides methods common for all terminology query connectors.
 *
 * @author Vladyslav Mykhalets
 */
public interface ITerminologyQuery extends AutoCloseable {

    /**
     * Gets the name of the connector.
     *
     * @return the name of the connector.
     */
    String getName();

    /**
     * Gets a display representation of the current settings for this connector.
     * This can be a display of some of the parameters for example, or some explanations
     * about default non-modifiable settings.
     *
     * @return a display representation of the current settings.
     */
    String getSettingsDisplay();

    LocaleId getSourceLanguage();

    LocaleId getTargetLanguage();

    boolean getAnnotateSource();

    boolean getAnnotateTarget();

    void setAnnotateSource(boolean annotateSource);

    void setAnnotateTarget(boolean annotateTarget);

    void setLanguages(LocaleId sourceLocale, LocaleId targetLocale);

    IParameters getParameters();

    void setParameters(IParameters params);

    void open();

    void leverage(ITextUnit tu);
}
