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

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
import net.sf.okapi.common.pipeline.annotations.StepParameterType;
import net.sf.okapi.common.resource.ITextUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Vladyslav Mykhalets
 */
@UsingParameters(TerminologyParameters.class)
public class TerminologyLeveragingStep extends BasePipelineStep {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private TerminologyParameters params;

    private LocaleId sourceLocale;
    private LocaleId targetLocale;

    private boolean initDone;
    private ITerminologyQuery connector;

    public TerminologyLeveragingStep() {
        params = new TerminologyParameters();
    }

    @StepParameterMapping(parameterType = StepParameterType.SOURCE_LOCALE)
    public void setSourceLocale (LocaleId sourceLocale) {
        this.sourceLocale = sourceLocale;
    }

    public LocaleId getSourceLocale() {
        return sourceLocale;
    }

    @SuppressWarnings("deprecation")
    @StepParameterMapping(parameterType = StepParameterType.TARGET_LOCALE)
    public void setTargetLocale (LocaleId targetLocale) {
        this.targetLocale = targetLocale;
    }

    public LocaleId getTargetLocale() {
        return targetLocale;
    }

    @Override
    public IParameters getParameters() {
        return params;
    }

    @Override
    public void setParameters(IParameters params) {
        this.params = (TerminologyParameters) params;
    }

    @Override
    public String getName() {
        return "Terminology Leveraging";
    }

    @Override
    public String getDescription() {
        return "Leverage terminology into the text units content of a document. " +
                "Expects: filter events. Sends back: filter events.";
    }

    @Override
    public void cancel() {
        closeConnector();
    }

    @Override
    public void destroy() {
        closeConnector();
    }

    @Override
    protected Event handleStartBatch (Event event) {
        initDone = false;
        return event;
    }

    @Override
    protected Event handleEndBatch (Event event) {
        if ( !params.getLeverage() ) return event;

        return event;
    }

    @Override
    protected Event handleStartDocument (Event event) {
        if ( !params.getLeverage() ) return event;
        logger.info("Starting processing document");

        if ( !initDone ) {
            init();
        }
        return event;
    }

    @Override
    protected Event handleEndDocument (Event event) {
        if ( !params.getLeverage() ) return event;
        logger.info("Ended processing document");

        return event;
    }

    @Override
    protected Event handleTextUnit (Event event) {
        if ( !params.getLeverage() ) return event;
        ITextUnit tu = event.getTextUnit();

        // Do not leverage non-translatable entries
        if ( !tu.isTranslatable() ) return event;

        // Leverage
        connector.leverage(tu);

        return event;
    }

    private void init() {

        // If we don't really use this step, just move on
        if ( !params.getLeverage() ) {
            initDone = true;
            return;
        }

        try {
            connector = (ITerminologyQuery) Class.forName(params.getConnectorClassName()).newInstance();
        } catch (InstantiationException e) {
            throw new OkapiException("Error creating connector.", e);
        } catch (IllegalAccessException e) {
            throw new OkapiException("Error creating connector.", e);
        } catch (ClassNotFoundException e) {
            throw new OkapiException("Error creating connector.", e);
        }

        IParameters connectorParams = connector.getParameters();
        if (connectorParams != null) { // Set the parameters only if the connector takes them
            connectorParams.fromString(params.getConnectorParameters());
            connector.setParameters(connectorParams);
        }

        if ((sourceLocale != null) && (targetLocale != null)) {
            connector.setLanguages(sourceLocale, targetLocale);
        }

        connector.setAnnotateSource(params.getAnnotateSource());
        connector.setAnnotateTarget(params.getAnnotateTarget());
        connector.open();

        logger.info("Terminology leveraging settings: {} {}", connector.getName(), connector.getSettingsDisplay());

        initDone = true;
    }

    private void closeConnector() {
        if (connector != null) {
            try {
                connector.close();
                connector = null;
            }
            catch (Exception e) {
                logger.error("Error closing connector", e);
            }
        }
    }
}
