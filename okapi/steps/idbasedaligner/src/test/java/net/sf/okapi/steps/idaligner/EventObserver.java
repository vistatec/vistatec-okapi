package net.sf.okapi.steps.idaligner;

import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.observer.IObservable;
import net.sf.okapi.common.observer.IObserver;

public class EventObserver implements IObserver {

    List<Event> eventList = new ArrayList<>();
	public EventObserver() {
	}
	
	public void update(IObservable o, Object arg) {
		eventList.add((Event) arg);
	}
	
	public List<Event> getResult() {
		return eventList;
	}
}
