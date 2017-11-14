package net.sf.okapi.lib.extra.steps;

import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.pipeline.BasePipelineStep;

public class EventListBuilderStep extends BasePipelineStep {

	private List<Event> list;
	
	public List<Event> getList() {
		return list;
	}

	@Override
	public String getName() {
		return "Event List Builder";
	}

	@Override
	public String getDescription() {
		return "Helper step for use in pipeline roundtrip tests.";
	}

	@Override
	protected Event handleStartBatch(Event event) {
		list = new ArrayList<Event>();
		return super.handleStartBatch(event);
	}
	
	@Override
	public Event handleEvent(Event event) {
			switch (event.getEventType()) {
			case START_DOCUMENT:
			case END_DOCUMENT:				
			case END_SUBDOCUMENT:
				break;
			case START_SUBDOCUMENT:
				break;
			case START_GROUP:
			case END_GROUP:
			case START_SUBFILTER:
			case END_SUBFILTER:
			case DOCUMENT_PART:
			case TEXT_UNIT:
			{
//				ITextUnit tu = event.getTextUnit(); 
//				list.add(new Event(EventType.TEXT_UNIT, tu.clone()));
				list.add(event);
			}
				break;
			}
		return super.handleEvent(event);
	}
}
