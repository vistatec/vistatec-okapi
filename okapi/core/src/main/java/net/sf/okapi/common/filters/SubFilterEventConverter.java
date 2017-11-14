/*===========================================================================
  Copyright (C) 2008-2012 by the Okapi Framework contributors
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

package net.sf.okapi.common.filters;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.IResource;
import net.sf.okapi.common.ISkeleton;
import net.sf.okapi.common.IdGenerator;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.RegexUtil;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.encoder.IEncoder;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.EndSubfilter;
import net.sf.okapi.common.resource.INameable;
import net.sf.okapi.common.resource.IReferenceable;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.MultiEvent;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.StartSubfilter;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextPart;

public class SubFilterEventConverter {

	private static Pattern REF = Pattern.compile( 			
		RegexUtil.escape(TextFragment.REFMARKER_START) + 
		"(.+?)" +							
		RegexUtil.escape(TextFragment.REFMARKER_END));
	private static int REF_GROUP = 1;
	
	private IdGenerator idGenerator;
	private SubFilter subFilter;
	private IEncoder parentEncoder;
	private Map<String, IResource> referents;
	
	public SubFilterEventConverter (SubFilter subFilter,
		IEncoder parentEncoder)
	{
		//this(parentId, null, null, idGenerator);
		this.subFilter = subFilter;
		this.parentEncoder = parentEncoder;
		this.idGenerator = new IdGenerator(null);
		referents = new HashMap<String, IResource>();
	}
	
	public void reset () {
		idGenerator.reset(null);
		referents.clear();
	}

	// public to be able to call from subclasses of SubFilter
	public String convertRefIds (String str) {
		Matcher m = REF.matcher(str);
		StringBuilder buf = new StringBuilder();	    
	    int start = 0;
	    int end = 0;
	    
	    while (m.find()) {	    
	        start = m.start(REF_GROUP);
	        if (start != -1) { // The group might not present in the match
	        	buf.append(str.substring(end, start));
	        	String id = m.group(REF_GROUP);
	        	IResource res = referents.get(id);	        	
		        buf.append(res == null ? id : subFilter.buildResourceId(id, res.getClass()));
		        end = m.end(REF_GROUP);
	        }	        
	    }
	    
	    buf.append(str.substring(end));
	    return buf.toString();		
	}
	
	private void convertTextContainer (TextContainer tc) {
		for ( TextPart textPart : tc ) {
			TextFragment tf = textPart.getContent();
			for (Code code : tf.getCodes()) {
				if (!code.hasReference()) continue;
				
				String data = code.getOuterData();
				String newData = convertRefIds(data);
				if ( code.hasOuterData() ) {
					code.setOuterData(newData);
				}
				else { 
					code.setData(newData);
				}
			}
		}
	}
	
	private void convertRefs (Event event) {
		if ( event.isMultiEvent() ) {
			MultiEvent me = event.getMultiEvent();
			for (Event e : me) {
				convertRefs(e);
			}
		}
		else {
			if ( event.isTextUnit() ) {
				ITextUnit tu = event.getTextUnit();
				convertTextContainer(tu.getSource());
				for (LocaleId locId : tu.getTargetLocales()) {
					convertTextContainer(tu.getTarget(locId));
				}				
			}
			
			ISkeleton skel = event.getResource().getSkeleton();
			subFilter.convertRefsInSkeleton(skel);			
		}
	}
	
	/**
	 * Converts an event.
	 * @param event the event coming from the sub-filter.
	 * @return the event after possible conversion.
	 */
	public Event convertEvent (Event event) {
		IResource res = event.getResource();
		if ( res instanceof IReferenceable ) {
			IReferenceable r = (IReferenceable)res;
			// referents are keyed by original Id
			if ( r.isReferent() ) referents.put(res.getId(), res);
		}
		
		// we convert START_DOCUMENT to START_SUBFILTER
		// and END_DOCUMENT to END_SUBFILTER
		switch ( event.getEventType() ) {
		case START_DOCUMENT:
			StartDocument sd = event.getStartDocument();
			IFilterWriter filterWriter = sd.getFilterWriter();
			// Initialize encoder manager and skeleton writer
			if ( filterWriter != null ) {
				EncoderManager em = filterWriter.getEncoderManager();
				if ( em != null ) {
					//TODO: the encoding of sd is the input encoding, that may not work in all cases
					em.setDefaultOptions(sd.getFilterParameters(), sd.getEncoding(), sd.getLineBreak());
					em.updateEncoder(sd.getMimeType());
				}
				// Configured by parent filter writer
//				ISkeletonWriter skelWriter = filterWriter.getSkeletonWriter();
//				if (skelWriter != null) {
//					skelWriter.processStartDocument(null, null, null, em, sd);
//				}
			}
			
			StartSubfilter startSubfilter =
				new StartSubfilter(
					subFilter.buildResourceId(null, StartSubfilter.class),
					sd,
					parentEncoder);			
			subFilter.startSubfilter = startSubfilter;
			//startSubFilter.setMimeType(((StartDocument) event.getResource()).getMimeType()); // TODO do we need it?
			//startSubFilter.setSkeleton(startGroupSkeleton); // TODO do we need it?
			
			//startSubFilter.setName(IFilter.SUB_FILTER + ((StartDocument) event.getResource()).getName());			
			startSubfilter.setName(subFilter.buildResourceName(null, false, StartSubfilter.class));
			event = new Event(EventType.START_SUBFILTER, startSubfilter);
			break;

		case END_DOCUMENT:
			EndSubfilter endSubfilter = new EndSubfilter(subFilter.buildResourceId(null, EndSubfilter.class));
			subFilter.endSubfilter = endSubfilter;
			//TODO: we need to get the skeleton from this event into the string output 
			endSubfilter.setSkeleton(event.getEnding().getSkeleton());
			event = new Event(EventType.END_SUBFILTER, endSubfilter);
			break;
			
		default: // TU, DP, etc.
			// Convert resource Id
			res.setId(subFilter.buildResourceId(res.getId(), res.getClass()));
			
			// Convert resource name
			if ( event.getResource() instanceof INameable ) {
				INameable nres = (INameable)event.getResource();
				String name = nres.getName();
				boolean isEmpty = Util.isEmpty(name);
				if ( isEmpty ) name = idGenerator.createId();
				nres.setName(subFilter.buildResourceName(name, isEmpty, nres.getClass()));
			}
			
			// Convert references in resources
			convertRefs(event);
			break;
			
//		case TEXT_UNIT:
//			ITextUnit tu = event.getTextUnit();
//			String name = tu.getName(); 
//			String tuIndex = idGenerator.createId(); 			
//			
//			if (name == null) {
//				name = tuIndex;
//			}
//			
//			tu.setName(SubFilter.makeId(namePrefix, name));
//			
////			if (parentName != null) {
////					String name = tu.getName(); 
////					
////					if (name == null) {
////						name = Integer.toString(++tuChildCount);
////					}
////
////					// Always prefix tu name with the parent name
////					name = parentName + "-" + name;				
////					tu.setName(name);
////			}
////			if (idGenerator != null) {
////				tu.setId(idGenerator.createId() + "_" + tu.getId());
////				//tu.setName(groupIdGenerator.createId() + "_" + tu.getName());
////			}			
//			tu.setId(SubFilter.makeId(idPrefix, tu.getId()));
//			break;
//
//		default:
//			break;
		}

		return event;
	}

}
