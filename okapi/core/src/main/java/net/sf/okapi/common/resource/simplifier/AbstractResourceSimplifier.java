/*===========================================================================
  Copyright (C) 2008-2014 by the Okapi Framework contributors
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

package net.sf.okapi.common.resource.simplifier;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.MultiEvent;
import net.sf.okapi.common.resource.StartSubfilter;
import net.sf.okapi.common.skeleton.ISkeletonWriter;

public abstract class AbstractResourceSimplifier implements IResourceSimplifier {

//	private final Logger logger = LoggerFactory.getLogger(getClass());

	private IResourceSimplifier parent;
	private boolean multilingual;
	private LocaleId targetLocale;
	private String outputEncoding;
	private IFilterWriter filterWriter;
	private ISkeletonWriter skeletonWriter;
	private boolean isCollectingEvents;
	private boolean isReferentGroup;
	private Stack<Boolean> refGroupStack = new Stack<Boolean>();
	
//	private Map<String, MultiEvent> groups =
//			new HashMap<String, MultiEvent>();
	
	private String groupId;
	private MultiEvent group;
	
//	private Stack<MultiEvent> groupStack =
//			new Stack<MultiEvent>();
		
	abstract protected Event convertEvent(Event event);	
	
//	private Event processEvent(Event event) {
//		if (groupStack.isEmpty()) {			
//			return convertEvent(event);
//		}
//		
//		Event me = convertEvent(event); // Subclass invocation
//		
//		// Add events to all groups on the stack of groups, so that parent groups also contained events of nested groups
//		for (MultiEvent group : groupStack) {
//			if (me.isMultiEvent()) {
//				for (Event e : me.getMultiEvent()) {
//					group.addEvent(e);
//				}
//			} else {
//				group.addEvent(me);
//			}
//		}
//		
//		return me;
//	}
	
	private Event processEvent(Event event) {
		if (!isCollectingEvents || isReferentGroup)
			return convertEvent(event);
		
		Event ev = convertEvent(event); // Subclass invocation
		if (ev.isMultiEvent()) {
			for (Event e : ev.getMultiEvent()) {
				if (e.isNoop()) continue;
				group.addEvent(e);
			}
		} else {
			group.addEvent(ev);
		}
	
		return ev;
	}
	
	@Override
	public Event convert(Event event) {		
//		// Simplifiers always receive FW from SD, here we are protected from invalid SD
//		if (filterWriter == null) {
//			logger.warn("Filter writer is not set.");
////			return event; // No conversion performed, return origina event
//		}
		
		// Some of FWs don't have SW, in which case we rely on the simplifier subclass to do the job
		if (skeletonWriter == null) 
			return processEvent(event);
		
		// Here we employ the SW 
		switch (event.getEventType()) {
//		case START_DOCUMENT:
//		case END_DOCUMENT:
//			clear();
//			event = convertEvent(event);
//			break;
			
		case START_GROUP:
//			if (inRefGroupStack.isEmpty()) {
//				// Level 0
//				inRefGroupStack.push(false);
//				isInReferentGroup = false;
//			}
			isReferentGroup = event.getStartGroup().isReferent();
			if (isReferentGroup) {
				// Only SG of referent groups are passed to the simplifier's skel writer, possible skel simplification happens in processEvent() below 
				skeletonWriter.processStartGroup(event.getStartGroup());
			}				
//			isInReferentGroup = event.getStartGroup().isReferent();
//			isInReferentGroup |= event.getStartGroup().isReferent(); // If a nested group is not referent, but the parent group is, the nested is considered referent too
			refGroupStack.push(isReferentGroup);			
//			if (isInReferentGroup) event.getStartGroup().setReferenceCount(1);
//			startGroup(event.getStartGroup());
			event = processEvent(event);
			break;
			
		case START_SUBFILTER:
			StartSubfilter ssf = event.getStartSubfilter();
//			StartDocument sd = ssf.getStartDoc();
//			IFilterWriter fw = sd.getFilterWriter();
//			EncoderManager em = fw.getEncoderManager();
//			skeletonWriter.processStartDocument(getTargetLocale(), getOutputEncoding(), null, em, sd);
			// Not processStartSubfilter() to avoid creation of subfilter writer
//			skeletonWriter.processStartGroup(event.getStartSubfilter());
//			startGroup(event.getStartSubfilter());
			isCollectingEvents = ssf.isReferent();
			if (isCollectingEvents) {
				groupId = ssf.getId();
				group = new MultiEvent();
			}							
			else {
				groupId = null;
				group = null;
			}				
			
			event = processEvent(event); // Add SSF to the group
			break;
			
		case END_GROUP:
			if (isReferentGroup)
				skeletonWriter.processEndGroup(event.getEndGroup());
			event = processEvent(event);
//			isInReferentGroup = false;
			refGroupStack.pop();
			isReferentGroup = refGroupStack.size() > 0 ? refGroupStack.peek() : false;
//			endGroup();
//			event = Event.NOOP_EVENT;
 			break;
			
		case END_SUBFILTER:
//			skeletonWriter.processEndGroup(event.getEndSubfilter());
//			processEvent(event);
//			endGroup();
//			event = Event.NOOP_EVENT;
			if (isCollectingEvents) {								
				processEvent(event); // Add ESF to the group
//				event = new Event(EventType.MULTI_EVENT, group);
//				group = null;
				setGroup(groupId, group);
				return Event.NOOP_EVENT;
			}
			isCollectingEvents = false;
			break;

		case TEXT_UNIT:			
			if (isReferentGroup) {
				skeletonWriter.processTextUnit(event.getTextUnit());
				return Event.NOOP_EVENT;
			}
			event = processEvent(event);
			break;
			
		case DOCUMENT_PART:
			if (isReferentGroup) {
				skeletonWriter.processDocumentPart(event.getDocumentPart());
				return Event.NOOP_EVENT;
			}			
			event = processEvent(event);
			break;
			
		default:			
			event = processEvent(event); // convert the event and add to the group if is collecting
			break;			
		}
		
		// We return the converted event for the root layer, and no event for subfilter/subgroup layers
//		return groupStack.isEmpty() ? event : Event.NOOP_EVENT;
		return isCollectingEvents ? Event.NOOP_EVENT : event;
	}
	
//	private void startGroup(StartGroup startGroup) {
//		String groupId = startGroup.getId();
//		MultiEvent group = new MultiEvent();
//		
//		groups.put(groupId, group);
//		groupStack.push(group);
//	}
//
//	private void endGroup() {
//		groupStack.pop();
//	}

//	private void clear() {
//		//groupStack.clear();
//		group = new MultiEvent();
//		isCollectingEvents = false;
//	}

	@Override
	public List<Event> convertToList(Event event) {
		List<Event> list = new LinkedList<Event>();
		Event me = convert(event);
		if (me.isMultiEvent()) {
			for (Event e : me.getMultiEvent()) {
				if (e.isNoop()) continue;
				list.add(e);
			}
		} else {
			list.add(me);
		}
		
		return list;
	}

	public IResourceSimplifier getParent() {
		return parent;
	}

	@Override
	public void setParent(IResourceSimplifier parent) {
		this.parent = parent;
	}
	
	public boolean isMultilingual() {
		return multilingual;
	}

	@Override
	public void setMultilingual(boolean multilingual) {
		this.multilingual = multilingual;
	}

	public LocaleId getTargetLocale() {
		return targetLocale;
	}
	
	@Override
	public void setTargetLocale(LocaleId targetLocale) {
		this.targetLocale = targetLocale;
	}
	
	public String getOutputEncoding() {
		return outputEncoding;
	}

	@Override
	public void setOutputEncoding(String outputEncoding) {
		this.outputEncoding = outputEncoding;
	}

	public IFilterWriter getFilterWriter() {
		return filterWriter;
	}

	@Override
	public void setFilterWriter(IFilterWriter filterWriter) {
		this.filterWriter = filterWriter;
	}

	public ISkeletonWriter getSkeletonWriter() {
		return skeletonWriter;
	}

	@Override
	public void setSkeletonWriter(ISkeletonWriter skeletonWriter) {
		this.skeletonWriter = skeletonWriter;
	}

	@Override
	public void setGroup(String groupId, MultiEvent group) {
		if (parent != null) {
			parent.setGroup(groupId, group);
		}
	}
	
	@Override
	public MultiEvent getGroup(String groupId) {
		return parent == null ? null : parent.getGroup(groupId);
	}
	
}
