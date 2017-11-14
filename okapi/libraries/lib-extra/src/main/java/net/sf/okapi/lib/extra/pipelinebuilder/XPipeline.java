/*===========================================================================
  Copyright (C) 2009 by the Okapi Framework contributors
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

package net.sf.okapi.lib.extra.pipelinebuilder;

import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.filters.DefaultFilters;
import net.sf.okapi.common.filters.FilterConfigurationMapper;
import net.sf.okapi.common.observer.IObservable;
import net.sf.okapi.common.observer.IObserver;
import net.sf.okapi.common.pipeline.IPipeline;
import net.sf.okapi.common.pipeline.IPipelineStep;
import net.sf.okapi.common.pipeline.Pipeline;
import net.sf.okapi.common.pipeline.PipelineReturnValue;
import net.sf.okapi.common.pipelinedriver.IBatchItemContext;
import net.sf.okapi.common.pipelinedriver.PipelineDriver;

public class XPipeline extends net.sf.okapi.common.pipeline.Pipeline implements IPipelineStep {

	private XPipelineAsStepImpl stepImpl = new XPipelineAsStepImpl();
	private XPipelineType type;
	private XBatch batch;
	private PipelineDriver pd;
	private FilterConfigurationMapper fcMapper;
	private IObservable delegatedObservable;
	
	public XPipeline(String description, IPipeline pipeline) {
		this(description, pipeline.getSteps().toArray(new IPipelineStep[] {}));
		this.setId(pipeline.getId());
	}
	
	public XPipeline(String description, IPipelineStep... steps) {		
		this(description, XPipelineType.SEQUENTIAL, steps);
	}
	
	public XPipeline(String description, XPipelineType type, IPipelineStep... steps) {
		this(description, type, true, steps);
	}
	
	private XPipeline(String description, XPipelineType type, boolean buildPipeline, 
			IPipelineStep... steps) {
		stepImpl.setDescription(description);
		this.type = type;
		
		for (IPipelineStep step : steps) {
			if (step instanceof XPipelineStep) {
				if (((XPipelineStep) step).getStep() == null) 
					continue; // Do not insert an empty step
				else
					this.addStep(step);	
			}
			else if (step instanceof IPipeline) {
				if (step instanceof XPipeline) {
					XPipeline pl = (XPipeline) step;
					if (pl.type == XPipelineType.PARALLEL) {
						this.addStep(step); // Parallel pipeline is inserted as one step, regardless of how many steps its branches contain 
						continue;
					}
					else {
						for (IPipelineStep s : pl.getSteps()) {
							this.addStep(s);
						}	
					}
				}
				else { // Either a sequential XPipeline, or another IPipeline 
					IPipeline pl = (IPipeline) step; 
					for (IPipelineStep s : pl.getSteps()) {
						this.addStep(s);
					}
				}
			}
			else
				this.addStep(step);
			
		}
		
		if (buildPipeline)
			recreatePipeline();
	}
	
	public XPipeline(String description, XBatch batch, IPipelineStep... steps) {		
		this(description, batch, XPipelineType.SEQUENTIAL, steps);
	}
	
	public XPipeline(String description, XFilters filters, XBatch batch, IPipelineStep... steps) {		
		this(description, XPipelineType.SEQUENTIAL, false, steps);
		this.fcMapper = filters.getFcMapper();
		setBatch(batch);
	}
	
	public XPipeline(String description, XFilters filters, String rootDir, XBatch batch, IPipelineStep... steps) {		
		this(description, filters, batch, steps);
		pd.setRootDirectories(Util.getDirectoryName(rootDir), Util.getDirectoryName(rootDir));
	}
	
	public XPipeline(String description, XBatch batch, XPipelineType type, IPipelineStep... steps) {
		this(description, type, false, steps);
		setBatch(batch);		
	}
	
	private void recreatePipeline(){
		pd = new PipelineDriver();
		IPipeline pl = pd.getPipeline();
		if (pl instanceof Pipeline) {
			delegatedObservable = (IObservable) pl;
		}
		//pd.setPipeline(this); // Commented, need to handle PipelineStep class to get annotations of the internal class, not the wraper's
		
		for (IPipelineStep step : this.getSteps())
			if (step instanceof XPipelineStep)
				pd.addStep(((XPipelineStep) step).getStep());
			else
				if (step != null) pd.addStep(step);
		
		if (fcMapper == null) {
			fcMapper = new FilterConfigurationMapper();
			DefaultFilters.setMappings(fcMapper, true, true);
		}
		pd.setFilterConfigurationMapper(fcMapper);
		
		if (batch == null) return;
		for (IBatchItemContext item : batch.getItems())
			pd.addBatchItem(item);
	}
	
	public PipelineReturnValue execute() {
		if (batch == null) return getState();
		if (pd == null) return getState();
		
		pd.processBatch();
		PipelineReturnValue state = pd.getPipeline().getState();
		pd.getPipeline().destroy();
		return state;
	}
	
	public void stop() {
		pd.getPipeline().cancel();
	}
	
	public String getHelpLocation() {
		return stepImpl.getHelpLocation();
	}

	public String getName() {
		return stepImpl.getName();
	}

	public IParameters getParameters() {
		return stepImpl.getParameters();
	}

	public Event handleEvent(Event event) {
		if (type == XPipelineType.SEQUENTIAL) {}
		return stepImpl.handleEvent(event);
	}

	public boolean isDone() {
		return stepImpl.isDone();
	}

	public boolean isLastOutputStep() {
		return stepImpl.isLastOutputStep();
	}

	public void setLastOutputStep(boolean isLastStep) {
		stepImpl.setLastOutputStep(isLastStep);
	}

	public void setParameters(IParameters params) {
		stepImpl.setParameters(params);
	}

	public XBatch getBatch() {
		return batch;
	}

	public void setBatch(XBatch batch) {
		this.batch = batch;
		recreatePipeline();
	}

	public String getDescription() {
		return stepImpl.getDescription();
	}
	
	public void addObserver(IObserver observer) {
		delegatedObservable.addObserver(observer);
	}

	public int countObservers() {
		return delegatedObservable.countObservers();
	}

	public void deleteObserver(IObserver observer) {
		delegatedObservable.deleteObserver(observer);
	}

	public void notifyObservers() {
		delegatedObservable.notifyObservers();
	}

	public void notifyObservers(Object arg) {
		delegatedObservable.notifyObservers(arg);
	}

	public void deleteObservers() {
		delegatedObservable.deleteObservers();
	}

	public List<IObserver> getObservers() {
		return delegatedObservable.getObservers();
	}
}
