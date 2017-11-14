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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;

import net.sf.okapi.common.ClassUtil;
import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.MultiEvent;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.StartSubDocument;
import net.sf.okapi.common.resource.StartSubfilter;
import net.sf.okapi.common.skeleton.GenericSkeletonSimplifier;
import net.sf.okapi.common.skeleton.GenericSkeletonWriter;
import net.sf.okapi.common.skeleton.ISkeletonWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceSimplifier extends AbstractResourceSimplifier {
	
	private final Map<Class<?>, Class<? extends IResourceSimplifier>> 
		FW_TO_RS_MAP = new LinkedHashMap<Class<?>, Class<? extends IResourceSimplifier>>() {
			private static final long serialVersionUID = 293052660643251017L; {
				
			// TODO Insert a new filter writer to resource simplifier mapping right above this line
	}};
	
	private final Map<Class<?>, Class<? extends IResourceSimplifier>> 
		SW_TO_RS_MAP = new LinkedHashMap<Class<?>, Class<? extends IResourceSimplifier>>() {
			private static final long serialVersionUID = 293052660643251018L; {
				
			// GenericSkeletonWriter and its subclasses are mapped to GenericSkeletonSimplifier
			put(GenericSkeletonWriter.class, GenericSkeletonSimplifier.class);
			// TODO Insert a new skeleton writer to resource simplifier mapping right above this line
	}};	
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private Stack<IResourceSimplifier> sStack;
	private Stack<StartDocument> sdStack;
	private Map<String, MultiEvent> groups;
	private IResourceSimplifier simplifier;
	private StartDocument startDocument;
	private boolean updateLayer;
	
	public ResourceSimplifier(LocaleId trgLoc) {
		super();
		super.setTargetLocale(trgLoc);
		sStack = new Stack<IResourceSimplifier>();
		sdStack = new Stack<StartDocument>();
		groups = new HashMap<String, MultiEvent>();
	}
	
	@Override
	public void initialize() {}
	
	private void clear() {
		sStack.clear();
		sdStack.clear();
		groups.clear();
		startDocument = null;
		simplifier = null;		
	}
	
	private Class<? extends IResourceSimplifier> fuzzyLookup(Map<Class<?>, Class<? extends IResourceSimplifier>> map, Class<?> classRef) {
		Class<? extends IResourceSimplifier> res = map.get(classRef);
		if (res == null) {
			// We iterate backwards to fall back to more generic classes closer to the top of the map
			LinkedList<Class<?>> keys = new LinkedList<Class<?>>(map.keySet()); 
			for(Iterator<Class<?>> it = keys.descendingIterator(); it.hasNext();) {
				Class<?> k = it.next();
				
				if (k.isAssignableFrom(classRef)) {
					res = map.get(k);
					break;
				}
			}
		}
		return res;
	}
	
	private IResourceSimplifier createSimplifier(StartDocument startDocument) {
		if (startDocument == null) return null;
		
		IFilterWriter filterWriter = getFilterWriter() == null ? 
				startDocument.getFilterWriter() : getFilterWriter();
		if (filterWriter == null) return null;
		
		ISkeletonWriter skelWriter = getSkeletonWriter() == null ?
				filterWriter.getSkeletonWriter() : getSkeletonWriter();
				
		// If there is no RS for the given filter writer, we try to look up one for the skeleton writer
		//Class<? extends IResourceSimplifier> rsClass = FW_TO_RS_MAP.get(filterWriter.getClass());
		Class<? extends IResourceSimplifier> rsClass = fuzzyLookup(FW_TO_RS_MAP, filterWriter.getClass());
		if (rsClass == null && skelWriter != null) {
			//rsClass = SW_TO_RS_MAP.get(filterWriter.getSkeletonWriter().getClass());
			rsClass = fuzzyLookup(SW_TO_RS_MAP, skelWriter.getClass());
		}
		if (rsClass == null) return null;
		
		IResourceSimplifier rs = null;
		try {
			rs = ClassUtil.instantiateClass(rsClass);
			rs.setParent(this);
			rs.setMultilingual(startDocument.isMultilingual());
//TODO check if correct			startDocument.setMultilingual(false); // All simplifiers convert events as if were coming from a monolingual format (src + single trgLoc)
			rs.setTargetLocale(getTargetLocale());
			rs.setOutputEncoding(getOutputEncoding());
			rs.setFilterWriter(filterWriter);
			rs.setSkeletonWriter(skelWriter);
			rs.initialize();
//			rs.convert(new Event(EventType.START_DOCUMENT, startDocument));
			if (filterWriter != null && skelWriter != null) {
				EncoderManager em = filterWriter.getEncoderManager();
				skelWriter.processStartDocument(getTargetLocale(), getOutputEncoding(), null, em, startDocument);
			}				
		} catch (InstantiationException | IllegalAccessException e) {
			logger.error("Unable to instantiate a resource simplifier for {} (mapped to {}).", 
					ClassUtil.getClassName(filterWriter), 
					ClassUtil.getClassName(rsClass));
			return null;
		}
				
		return rs;
	}
	
	@Override
	public Event convert(Event event) {
		EventType eventType = event.getEventType();
		
		if (updateLayer) {			
//			IFilterWriter filterWriter = startDocument.getFilterWriter();
//			if (filterWriter != null) {
//				// Ask the filter writer stored in SD/SSF for skel writer (previously set by the filter writer in its SSD handler)
//				ISkeletonWriter sw = filterWriter.getSkeletonWriter();
//				if (sw instanceof GenericSkeletonWriter) {
//					// We don't need to push the previous simplifier here
//					simplifier = new GenericResourceSimplifier(startDocument.isMultilingual(), (GenericSkeletonWriter) sw,
//						outputEncoding,	getTargetLocale());				
//				}
//			}
			simplifier = createSimplifier(startDocument);
			updateLayer = false;
		}
		
		switch (eventType) {
		case START_DOCUMENT:
			clear();
			updateLayer = false;
			break;
					
		case START_SUBFILTER:
//			if (simplifier != null) {
//				simplifier.convert(event);
//			}
			sStack.push(simplifier);
			simplifier = null; // Default for the new layer, will be figured out below
			
			sdStack.push(startDocument);
			startDocument = null; // Default for the new layer, will be figured out below			
		break;
			
		case START_SUBDOCUMENT:
			sStack.push(simplifier);
			simplifier = null; // Default for the new layer, will be figured out below
			break;
			
		default:
			break;
		}
		
		switch (eventType) {
		case START_DOCUMENT:						
			// If the filter uses an instance of GSW or its subclass, pass it on to RS			
			startDocument = event.getStartDocument();
			if (startDocument == null) break;
//			
//			IFilterWriter filterWriter = startDocument.getFilterWriter();
//			if (filterWriter == null) break;
			
			if (getTargetLocale() == null) {
				setTargetLocale(startDocument.getLocale());
			}
			
			if (Util.isEmpty(getOutputEncoding())) {
				setOutputEncoding(startDocument.getEncoding());
			}
			
//			filterWriter.setOptions(targetLocale, outputEncoding);
//			filterWriter.setOutput(Util.buildPath(Util.getTempDirectory(), "~okapi-resource-simplifier.tmp"));
//			filterWriter.handleEvent(event);
			
//			ISkeletonWriter sw = filterWriter.getSkeletonWriter();
//			if (sw instanceof GenericSkeletonWriter) {
//				simplifier = new GenericResourceSimplifier(startDocument.isMultilingual(), (GenericSkeletonWriter) sw,
//					outputEncoding,	getTargetLocale());
//				event = simplifier.convert(event);
//			}
			simplifier = createSimplifier(startDocument);
			if (simplifier != null)
				event = simplifier.convert(event);
			break;
			
		case START_SUBDOCUMENT:
			StartSubDocument ssd = event.getStartSubDocument();
			if (ssd == null) break;
			if (startDocument == null) {
				logger.error("StartDocument resource is not found for StartSubDocument id = {}.", ssd.getId());
				break;
			}
			// The filter writer of OpenXML and similar updates the skeleton writer in the SSD handler,
			// at this point it's not set yet, so we set the flag and update the sw when the next event comes in
			updateLayer = true;
			
//			filterWriter = startDocument.getFilterWriter();
//			if (filterWriter != null) {				
//				// Ask the filter writer stored in SD/SSF for skel writer (previously set by the filter writer in its SSD handler)
//				sw = filterWriter.getSkeletonWriter();
//				if (sw instanceof GenericSkeletonWriter) {
//					simplifier = new ResourceSimplifier(startDocument.isMultilingual(), (GenericSkeletonWriter) sw,
//						outputEncoding,	targetLocale);	
////					simplifier.convert(event);
////					return Event.NOOP_EVENT; // As we made filterWriter handle the event, we swallow it up
//				}
//				else {
//					filterWriter.handleEvent(event); // Try to switch the content sw
//					sw = filterWriter.getSkeletonWriter();
//					if (sw instanceof GenericSkeletonWriter) {
//						simplifier = new ResourceSimplifier(startDocument.isMultilingual(), (GenericSkeletonWriter) sw,
//							outputEncoding,	targetLocale);	
//						simplifier.convert(event);
//						return Event.NOOP_EVENT; // As we made filterWriter handle the event, we swallow it up
//					}
//				}
//				filterWriter.setOptions(targetLocale, outputEncoding);
//				filterWriter.getEncoderManager().updateEncoder(startDocument.getMimeType());
//				filterWriter.getEncoderManager().setOptions(startDocument.getFilterParameters(), outputEncoding, startDocument.getLineBreak());
//				filterWriter.handleEvent(new Event(EventType.START_DOCUMENT, startDocument));
				
//				sw = filterWriter.getSkeletonWriter();
//				if (sw == null) {
//					try {
//						filterWriter.handleEvent(event); // Try to switch the content SW in OpenXML and alike
//						// Ask the filter writer stored in SD/SSF for skel writer (previously set by the filter writer in its SSD handler)
//						sw = filterWriter.getSkeletonWriter();
//					} catch (Exception e) {
//						// TODO: handle exception
//					}					
//				}				
//				if (sw instanceof GenericSkeletonWriter) {
//					simplifier = new ResourceSimplifier(startDocument.isMultilingual(), (GenericSkeletonWriter) sw,
//						outputEncoding,	targetLocale);
//					simplifier.convert(event);
//					return Event.NOOP_EVENT; // As we made filterWriter handle the event, we swallow it up
//				}
//			}
			break;
			
		case START_SUBFILTER:
			StartSubfilter ssf = event.getStartSubfilter();
			if (ssf == null) break;
			
			startDocument = ssf.getStartDoc();
			if (startDocument == null) break;
			
//			filterWriter = startDocument.getFilterWriter();
//			if (filterWriter == null) break;
//			
//			sw = filterWriter.getSkeletonWriter();
//			if (sw instanceof GenericSkeletonWriter) {
//				simplifier = new GenericResourceSimplifier(startDocument.isMultilingual(), (GenericSkeletonWriter) sw,
//					outputEncoding,	getTargetLocale());
//				// Activate the encoder manager of the subfilter writer
//				simplifier.convert(new Event(EventType.START_DOCUMENT, startDocument));
//			}
			
			simplifier = createSimplifier(startDocument);
			if (simplifier != null) {
//				simplifier.convert(new Event(EventType.START_DOCUMENT, startDocument));
				event = simplifier.convert(event);
			}				
			break;
		
		case END_SUBFILTER:
			// Block simplifier.convert()
			break;
			
		default:
			// If the simplifier is not instantiated, don't change events
			if (simplifier != null) {
				event = simplifier.convert(event);
			}
			break;
		}
		
		switch (eventType) {
		case END_DOCUMENT:
			clear();
			break;
		
		case END_SUBFILTER:				
			event = simplifier.convert(event);
			startDocument = sdStack.pop();
			simplifier = sStack.pop();			
			break;
			
		case END_SUBDOCUMENT:		
			simplifier = sStack.pop();
			break;
			
		default:
			break;
		}
		
		return event;
	}

	public void mapFilterWriter(Class<? extends IFilterWriter> filterWriter, Class<? extends IResourceSimplifier> simplifier) {
		FW_TO_RS_MAP.put(filterWriter, simplifier);
	}
	
	public void mapSkeletonWriter(Class<? extends ISkeletonWriter> skeletonWriter, Class<? extends IResourceSimplifier> simplifier) {
		SW_TO_RS_MAP.put(skeletonWriter, simplifier);
	}

	@Override
	protected Event convertEvent(Event event) {
		return null;
	}

	@Override
	public MultiEvent getGroup(String groupId) {
		return groups.get(groupId);
	}
	
	@Override
	public void setGroup(String groupId, MultiEvent group) {
		groups.put(groupId, group);
	}
}
