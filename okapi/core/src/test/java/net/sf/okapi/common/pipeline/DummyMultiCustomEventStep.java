package net.sf.okapi.common.pipeline;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.resource.MultiEvent;

public class DummyMultiCustomEventStep extends BasePipelineStep {

	private boolean isDone = false;

	public String getDescription() {
		return "Dummy step for testing";
	}

	public String getName() {
		return "DummyMultiEventStep";
	}
	
	protected Event handleCustom(Event event) {
		MultiEvent me = new MultiEvent();
		me.addEvent(new Event(EventType.CUSTOM));
		me.addEvent(new Event(EventType.CUSTOM));
		event = new Event(EventType.MULTI_EVENT, me);
		isDone = true;
		return event;
	}
	
	public boolean isDone() {
		return isDone;
	}
}
