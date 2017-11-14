package net.sf.okapi.steps.searchandreplace;

import java.util.LinkedList;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.observer.IObservable;
import net.sf.okapi.common.observer.IObserver;

public class EventObserver implements IObserver {

List<Event> eventList;	
	public EventObserver() {
		eventList = new LinkedList<Event>();
	}
	
	public void update(IObservable o, Object arg) {
		eventList.add((Event) arg);
	}
	
	public List<Event> getResult() {
		return eventList;
	}
}
