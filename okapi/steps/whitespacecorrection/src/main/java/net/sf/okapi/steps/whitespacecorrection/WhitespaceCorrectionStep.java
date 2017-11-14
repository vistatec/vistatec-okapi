/*===========================================================================
  Copyright (C) 2016 by the Okapi Framework contributors
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

package net.sf.okapi.steps.whitespacecorrection;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
import net.sf.okapi.common.pipeline.annotations.StepParameterType;

/**
 * Provides a general whitespace correction step.
 */
@UsingParameters(WhitespaceCorrectionStepParameters.class)
public class WhitespaceCorrectionStep extends BasePipelineStep {
    protected LocaleId sourceLocale;
    protected LocaleId targetLocale;
    private WhitespaceCorrectionStepParameters params = new WhitespaceCorrectionStepParameters();

    @Override
    public String getName() {
        return "Whitespace Correction";
    }

    @Override
    public String getDescription() {
        return "Correct whitespace following segment-ending punctuation when translating from " +
               " a space-delimited language (such as English) to a non-space-delimited language " +
               "(Chinese or Japanese), or vice-versa. " +
               "Expects: filter events. Sends back: filter events.";
    }

    @Override
    public WhitespaceCorrectionStepParameters getParameters() {
        return params;
    }

    @Override
    public void setParameters (IParameters params) {
        this.params = (WhitespaceCorrectionStepParameters)params;
    }

    @StepParameterMapping(parameterType = StepParameterType.SOURCE_LOCALE)
    public void setSourceLocale(LocaleId sourceLocale) {
        this.sourceLocale = sourceLocale;
    }

    @StepParameterMapping(parameterType = StepParameterType.TARGET_LOCALE)
    public void setTargetLocale(LocaleId targetLocale) {
        this.targetLocale = targetLocale;
    }

    @Override
    protected Event handleTextUnit(Event event) {
        new WhitespaceCorrector(sourceLocale, targetLocale,
                        getParameters().getPunctuation()).correctWhitespace(event.getTextUnit());
        return event;
    }
}
