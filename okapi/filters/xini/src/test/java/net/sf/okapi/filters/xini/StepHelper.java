package net.sf.okapi.filters.xini;

import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.pipeline.IPipelineStep;

/**
 * Enables usage of a step without a pipeline.
 * @author Josef
 *
 */
public class StepHelper {

	private IPipelineStep step;

	public StepHelper(IPipelineStep step) {
		this.step = step;
	}

	/**
	 * Passes all events to the internal steps and returns the resulting events.
	 * @param eventsToProcess
	 * @return
	 */
	public List<Event> process(List<Event> eventsToProcess) {
		eventsToProcess = addBatchEvents(eventsToProcess);
		List<Event> result = new ArrayList<Event>();
		for (Event eventToProcess : eventsToProcess) {
			Event resultingEvent = step.handleEvent(eventToProcess);
			result.add(resultingEvent);
		}
		result = removeBatchEvents(result);
		return result;
	}

	private List<Event> addBatchEvents(List<Event> events) {
		events.add(0, new Event(EventType.START_BATCH));
		events.add(1, new Event(EventType.START_BATCH_ITEM));
		events.add(new Event(EventType.END_BATCH_ITEM));
		events.add(new Event(EventType.END_BATCH));
		return events;
	}

	private List<Event> removeBatchEvents(List<Event> events) {
		return events.subList(2, events.size() - 2);
	}
}
