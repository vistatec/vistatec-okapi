package net.sf.okapi.common.pipeline;

import net.sf.okapi.common.Event;

public class DummyCustomEventStep extends BasePipelineStep {
	public String getDescription() {
		return "Dummy step for testing";
	}

	public String getName() {
		return "DummyStep";
	}
	
	protected Event handleCustom(Event event) {					
		return event;
	}
}
