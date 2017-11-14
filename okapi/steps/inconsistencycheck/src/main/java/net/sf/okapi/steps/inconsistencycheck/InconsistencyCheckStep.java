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

package net.sf.okapi.steps.inconsistencycheck;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
import net.sf.okapi.common.pipeline.annotations.StepParameterType;
import net.sf.okapi.common.resource.ISegments;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Segment;

@UsingParameters(Parameters.class)
public class InconsistencyCheckStep extends BasePipelineStep {

    private InconsistencyCheck checker;
    private int docIdValue;
    private String docId;
    private String tuId;
    private String segId;
    private int subDocIdValue;
    private String subDocId;
    private String rootDir;
    private String finalPath;
    private String inputRootDir;
    private LocaleId targetLocale;

    public InconsistencyCheckStep() {
        this.checker = new InconsistencyCheck();
    }

    @Override
    public String getName() {
        return "Inconsistency Check";
    }

    @Override
    public String getDescription() {
        return "Checks for source entries that are the same but have different translations or "
                + "target entries that are the same but have different sources. "
                + "Expects: filter events. Sends back: filter events.";
    }

    @Override
    public IParameters getParameters() {
        return checker.getParameters();
    }

    @Override
    public void setParameters(IParameters params) {
        checker.setParameters((Parameters) params);
    }

    @StepParameterMapping(parameterType = StepParameterType.ROOT_DIRECTORY)
    public void setRootDirectory(String rootDir) {
        this.rootDir = rootDir;
    }
    
    @StepParameterMapping(parameterType = StepParameterType.INPUT_ROOT_DIRECTORY)
    public void setInputRootDirectory(String inputRootDir) {
        this.inputRootDir = inputRootDir;
    }

	@StepParameterMapping(parameterType = StepParameterType.TARGET_LOCALE)
    public void setTargetLocale(LocaleId targetLocale) {
        this.targetLocale = targetLocale;
    }

    @Override
    public Event handleStartBatch(Event event) {
        docIdValue = 0;
    	// Compute the full path for the report
        finalPath = Util.fillRootDirectoryVariable(checker.getParameters().getOutputPath(), rootDir);
        finalPath = Util.fillInputRootDirectoryVariable(finalPath, inputRootDir);
        return event;
    }

    @Override
    public Event handleStartDocument(Event event) {
        docIdValue++;
        String docName = event.getStartDocument().getName();
        docId = (docName == null) ? Integer.toString(docIdValue) : docName;
        subDocIdValue = 0;
        return event;
    }

    @Override
    public Event handleStartSubDocument(Event event) {
        subDocIdValue++;
        subDocId = Integer.toString(subDocIdValue);
        return event;
    }

    @Override
    public Event handleTextUnit(Event event) {
        ITextUnit tu = event.getTextUnit();
        tuId = tu.getId();
        ISegments srcSegs = tu.getSourceSegments();
        // Check if there is a target. If not: skip this entry
        if ( !tu.hasTarget(targetLocale) ) {
        	return event;
        }
        // If we have a target: use it.
        ISegments trgSegs = tu.getTargetSegments(targetLocale);
        for (Segment srcSeg : srcSegs) {
            segId = srcSeg.getId();
            Segment trgSeg = trgSegs.get(srcSeg.getId());
            // If there is no target segment: skip the segment
            if ( trgSeg == null ) continue;
            // Else: send the segments to the checker
            checker.store(docId, subDocId, tuId, segId, srcSeg.getContent(), trgSeg.getContent());
        }
        return event;
    }

    @Override
    public Event handleEndDocument(Event event) {
        if (checker.getParameters().getCheckPerFile()) {
            checker.generateReport(finalPath, false);
        }
        return event;
    }
    
    @Override
    public Event handleEndBatch(Event event) {
        // Generates the report
        checker.generateReport(finalPath, true);
        return event;
    }

}
