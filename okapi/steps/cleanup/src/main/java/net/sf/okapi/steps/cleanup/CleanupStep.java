/*===========================================================================
  Copyright (C) 2013 by the Okapi Framework contributors
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

package net.sf.okapi.steps.cleanup;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
import net.sf.okapi.common.pipeline.annotations.StepParameterType;
import net.sf.okapi.common.resource.ITextUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@UsingParameters(Parameters.class)
public class CleanupStep extends BasePipelineStep {

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    private Parameters params;
    private Cleaner cleaner;
    private LocaleId sourceLocale;
    private LocaleId targetLocale;

    public CleanupStep() {

        this.params = new Parameters();
        this.cleaner = new Cleaner(params);
    }

    @Override
    public String getName() {

        return "Cleanup";
    }

    @Override
    public String getDescription() {

        return "Cleans strings by normalizing quotes, punctuation, etc. ready for further processing. "
                + "Expects: filter events. Sends back: filter events.";
    }

    @Override
    public IParameters getParameters() {

        return params;
    }

    @Override
    public void setParameters(IParameters params) {

        this.params = (Parameters) params;
    }

    @StepParameterMapping(parameterType = StepParameterType.TARGET_LOCALE)
    public void setTargetLocale(LocaleId targetLocale) {

        this.targetLocale = targetLocale;
    }

    @StepParameterMapping(parameterType = StepParameterType.SOURCE_LOCALE)
    public void setSourceLocale(LocaleId sourceLocale) {

        this.sourceLocale = sourceLocale;
    }

    @Override
    protected Event handleTextUnit(Event event) {

        // TODO: move to cleaner. create run method

        ITextUnit tu = event.getTextUnit();

        // return event iff tu has text, else remove tu
        if (cleaner.run(tu, targetLocale) == true) {
            return Event.NOOP_EVENT;
        } else {
            return event;
        }
    }
}
