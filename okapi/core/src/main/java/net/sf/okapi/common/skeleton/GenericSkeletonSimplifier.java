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

package net.sf.okapi.common.skeleton;

import java.security.InvalidParameterException;
import java.util.List;

import net.sf.okapi.common.ClassUtil;
import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.IResource;
import net.sf.okapi.common.ISkeleton;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.ResourceUtil;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.encoder.EncoderContext;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.resource.BaseNameable;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.DocumentPart;
import net.sf.okapi.common.resource.IReferenceable;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.MultiEvent;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextPart;
import net.sf.okapi.common.resource.TextUnitUtil;
import net.sf.okapi.common.resource.simplifier.AbstractResourceSimplifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simplifies events, i.e. splits the generic skeleton of a given event resource into parts to contain no references.
 * The skeleton parts are attached to newly created DOCUMENT_PART events.
 * Original references are converted either to skeleton parts, or TEXT_UNIT events.
 * The sequence of DOCUMENT_PART and TEXT_UNIT events is packed into a single MULTI_EVENT event.
 * This class can handle only GenericSkeletonWriter and its subclasses.
 */
public class GenericSkeletonSimplifier extends AbstractResourceSimplifier {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private GenericSkeletonWriter skelWriter;
	private GenericSkeleton newSkel;
	private boolean isBlocked;
	
	public GenericSkeletonSimplifier(LocaleId trgLoc) {
		this(null, trgLoc);
	}
	
	public GenericSkeletonSimplifier(String outEncoding, LocaleId trgLoc) {
		this(false, null, outEncoding, trgLoc);
	}

	public GenericSkeletonSimplifier(boolean isMultilingual, GenericSkeletonWriter skelWriter, LocaleId trgLoc) {
		this(isMultilingual, skelWriter, null, trgLoc);
	}
	
	public GenericSkeletonSimplifier(boolean isMultilingual, GenericSkeletonWriter skelWriter, String outEncoding, LocaleId trgLoc) {
		super();
		setMultilingual(isMultilingual);
		setSkeletonWriter(skelWriter);
		setOutputEncoding(outEncoding);
		setTargetLocale(trgLoc);
		initialize(); // Explicit call when used stand-alone, otherwise ResourceSimplifier calls it after this class instantiation
	}
	
	public GenericSkeletonSimplifier() {
		super();
	}

	@Override
	public void initialize() {
		if (getSkeletonWriter() != null && !(getSkeletonWriter() instanceof GenericSkeletonWriter)) {
			logger.warn("GenericSkeletonSimplifier handles only GenericSkeletonWriter and its subclasses, {} is not supported.", ClassUtil.getShortClassName(getSkeletonWriter()));
			isBlocked = true;
			return;
		}
		if (getSkeletonWriter() == null) {
			skelWriter = new GenericSkeletonWriter();
			setSkeletonWriter(skelWriter);
			logger.warn("GenericSkeletonWriter instance was created as no skeleton writer was passed in.");			
		}
		else {
			skelWriter = (GenericSkeletonWriter) getSkeletonWriter();
		}
		
		if (Util.isEmpty(getOutputEncoding())) setOutputEncoding("UTF-8");
		
		// If the writer was passed in from outside, then we wait for START_DOCUMENT to bring in the encoder manager,
		// otherwise we set defaults
		if (getSkeletonWriter() == null) {			
			this.skelWriter.setOutputEncoding(getOutputEncoding());
			this.skelWriter.setOutputLoc(getTargetLocale());
			EncoderManager em = new EncoderManager();
			em.setDefaultOptions(null, getOutputEncoding(), "\n");
			em.setAllKnownMappings();
			em.updateEncoder(getOutputEncoding());
		}
		newSkel = new GenericSkeleton();
		
		setOutputEncoding(getOutputEncoding());
		setTargetLocale(getTargetLocale());		
	}	
	
	/**
	 * Merges adjacent document parts into one. Will work for simple resources only.
	 * @param me the multi-event containing the events to merge.
	 * @return the resulting multi-event.
	 */	
	public static MultiEvent packMultiEvent(MultiEvent me) {
		Event prevEvent = null;
		MultiEvent newME = new MultiEvent();
		newME.setId(me.getId());
		
		for (Event event : me) {
			if (event.isNoop()) continue;
			
			if (prevEvent != null && 
				event != null &&
				prevEvent.getEventType() == EventType.DOCUMENT_PART && 
				event.getEventType() == EventType.DOCUMENT_PART) {
				
				// Append to prev event's skeleton
				IResource res = event.getResource(); 
				ISkeleton skel = res.getSkeleton();
				if (skel instanceof GenericSkeleton) {
					IResource prevRes = prevEvent.getResource(); 
					ISkeleton prevSkel = prevRes.getSkeleton();
					if (prevSkel instanceof GenericSkeleton)
						((GenericSkeleton) prevSkel).add((GenericSkeleton) skel);
				}				
			}
			else {
				newME.addEvent(event);
				prevEvent = event;
			}			
		}
		return newME;
	}
	
	/**
	 * Converts a given event into a multi-event if it contains references in its source's codes or in skeleton, or passes it on if 
	 * either the skeleton is no instance of GenericSkeleton, contains no references, or the resource is referent.
	 * @param event the given event
	 * @return the given event or a newly created multi-event
	 */
	@Override
	protected Event convertEvent(Event event) {
		if (isBlocked) return event;
		
		if (event == null)
			throw new InvalidParameterException("Event cannot be null");

		if (event.isNoop()) return event;
		
		IResource res = event.getResource();
		if (res == null)
			return event;
		
		if (res instanceof IReferenceable && !event.isStartSubfilter() && !event.isStartGroup()) {			
			if  (((IReferenceable) res).isReferent()) {
				if (!event.isStartSubfilter() && !event.isStartGroup()) {
					skelWriter.addToReferents(event);
				}				
				// Referents (but SSF and SG) will go to the skeleton or inline codes, so no stand-alone events are needed anymore
				return event.isStartSubfilter() || event.isStartGroup() ? event : Event.NOOP_EVENT;
			}
		}
		
		switch (event.getEventType()) {
		case START_DOCUMENT:
			StartDocument sd = (StartDocument) res;
			isBlocked = false;
			if (sd != null && 
				sd.getFilterWriter() != null && 
				!(sd.getFilterWriter().getSkeletonWriter() instanceof GenericSkeletonWriter)) {
					logger.warn("GenericSkeletonSimplifier handles only GenericSkeletonWriter and its subclasses, {} is not supported.", ClassUtil.getShortClassName(sd.getFilterWriter()));
					isBlocked = true;
					return event;
			}
			
			EncoderManager em = sd.getFilterWriter() != null ? sd.getFilterWriter().getEncoderManager() : null;
			if (em == null)	em = new EncoderManager();
			if (Util.isEmpty(em.getEncoding())) {
				em.setDefaultOptions(sd.getFilterParameters(), sd.getEncoding(), sd.getLineBreak());
				em.updateEncoder(sd.getMimeType());
			}			
			this.skelWriter.processStartDocument(getTargetLocale(), getOutputEncoding(), null, em, sd); // Sets skelWriter fields + activates ref tracking mechanism of GSW
			break;			
		default:
			break;
		}
				
		if (!isComplex(res)) {
			switch (event.getEventType()) {
			case END_DOCUMENT:
				skelWriter.close(); // Clears the referents cache
				break;				
			default:
				break;
			}
			
			return event;
		}
		
		// Process the resource's skeleton
		MultiEvent me = new MultiEvent();		
		processResource(res, me);
		
		// Different event types are processed differently
		switch (event.getEventType()) {
		case END_DOCUMENT:
			if (me.size() == 0) {
				skelWriter.close(); // Clears the referents cache
			}
			else {
				res.setSkeleton(null);
				me.addEvent(event);
			}			  
			break;
			// No break here
		case START_DOCUMENT:
		case START_SUBDOCUMENT:
		case END_SUBDOCUMENT:
		case START_GROUP:
		case END_GROUP:
		case START_SUBFILTER:
		case END_SUBFILTER:
			// Referents are sent to the GSW cache and are accessed there as refs are processed.
			// Here we deal with non-referents only.
			// The original event (the skeleton should be deleted) precedes in the resulting multi-event DPs/TUs 
			// created from its original skeleton parts
			res.setSkeleton(null);
			me.addEvent(event, 0);  
			break;
		case TEXT_UNIT:
		case DOCUMENT_PART:
			break;
		default:
			return event;
		}

		if (me.size() == 0) 
			return event;
		else if (me.size() == 1)
			return assignIDs(me, res).iterator().next();
		else
			return new Event(EventType.MULTI_EVENT, assignIDs(packMultiEvent(me), res));
	}
		
	private MultiEvent assignIDs(MultiEvent me, IResource resource) {
		int counter = 0;
		for (Event event : me) {
			if (event.isNoop()) continue;
			
			IResource res = event.getResource();
			String resId = resource.getId();
			
			if (res instanceof DocumentPart && !(resource instanceof DocumentPart)) {
				String id = "";
				if (counter++ == 0) id = resId;
				else
					id = String.format("%s_%d", resId, counter++);
				
				res.setId("" + String.format("dp_%s", id));
			}
			else {
				res.setId(resId);
				if (res instanceof BaseNameable && resource instanceof BaseNameable) {
					ResourceUtil.copyProperties((BaseNameable) resource, (BaseNameable) res);
				}
			}							
		}
		return me;
	}

	private boolean isComplex(IResource res) {
		if (res == null)
			return false;
		
		if (res instanceof ITextUnit) {
			ITextUnit tu = (ITextUnit)res;
			TextFragment tf = tu.getSource().getUnSegmentedContentCopy();
			for (Code code : tf.getCodes()) {
				if (code.hasReference()) return true;
			}
		}
		
		ISkeleton skel = res.getSkeleton();
		if (skel == null) {
			return false;
		}		
		if (!(skel instanceof GenericSkeleton)) {
			return false;
		}
		
		List<GenericSkeletonPart> parts = ((GenericSkeleton) skel).getParts();
		for (GenericSkeletonPart part : parts)
			if (!SkeletonUtil.isText(part)) 
				return true;
		
		return false;
	}
	
	private void flushSkeleton(String resId, int dpIndex, MultiEvent me) {
		if (newSkel.isEmpty()) return;
			
		//me.addEvent(new Event(EventType.DOCUMENT_PART, new DocumentPart(String.format("%s_%d", resId, dpIndex), false, newSkel)));
		me.addEvent(new Event(EventType.DOCUMENT_PART, new DocumentPart("", false, newSkel))); // IDs are set in packMultiEvent()
		newSkel = new GenericSkeleton(); // newSkel.clear() would damage an already sent skeleton
	}
	
	private void addTU(MultiEvent me, String resId, int tuIndex, ITextUnit tu) {
		String id = null;		
		if (tuIndex == 1)
			id = resId;
		else {
			logger.warn("Duplicate TU: {}", resId);
			id = String.format("%s_%d", resId, tuIndex);
		}
		
		ITextUnit newTU = tu.clone();
		newTU.setId(id);
		newTU.setSkeleton(null);
		newTU.setIsReferent(false); //!!! to have GSW write it out
		
		if (tu.isEmpty()) return;
		me.addEvent(new Event(EventType.TEXT_UNIT, newTU));
	}
	
	/**
	 * Creates events from references of a given resource, adds created events to a given multi-event resource.
	 */
	private void processResource(IResource resource, MultiEvent me) {
		if (resource == null)
			throw new InvalidParameterException("Resource parameter cannot be null");
		if (me == null)
			throw new InvalidParameterException("MultiEvent parameter cannot be null");
		
		int dpCounter = 0;
		int tuCounter = 0;
		String resId = resource.getId();
		ISkeleton skel = resource.getSkeleton();		
		boolean hasGenericSkeleton = skel instanceof GenericSkeleton; 
		
		if (resource instanceof ITextUnit) {
			ITextUnit tu = (ITextUnit)resource;
			if (tu.isReferent()) {
				// Referenced TU, we got here from recursion (see *** below)
				if (!hasGenericSkeleton) {
					newSkel.add(skelWriter.getString(tu, getTargetLocale(), EncoderContext.SKELETON));
					return;
				}
				// Otherwise the skeleton is analyzed below
			}
			else {
				// Regular TU
				// We process only the part of the TU that will be later output (targetLocale defines what will be the output locale)
				// We cannot simplify all parts, because references from the source codes can be copied to targets, but GSW referents are removed after the first ref resolution 
				TextContainer tc = null;
				if (getTargetLocale() == null) {
					tc = tu.getSource();
				}
				else {
					tc = tu.getTarget(getTargetLocale());
					if (tc == null)
						tc = tu.getSource();
				}
				
				for (TextPart part : tc) {
					TextFragment tf = part.getContent();
					for (Code code : tf.getCodes()) {
						
// References in codes are replaced with the target plain text, generated for the resources on the reference for the target locale.
// Because the ref is in the inline code, it shouldn't be resolved as a subflow of resources.
// Inline codes are skeleton parts in the middle of translatable text.
						if (code.hasReference()) {
							// Resolve reference(s) with GSW, replace the original data
							//if (resolveCodeRefs)
							if (code.hasOuterData()) 
								code.setOuterData(skelWriter.expandCodeContent(code, getTargetLocale(), EncoderContext.TEXT));
							else
								code.setData(skelWriter.expandCodeContent(code, getTargetLocale(), EncoderContext.TEXT));
						}
					}
				}
				
				
//				for (Iterator<TextPart> iter = tc.iterator(); iter.hasNext();) {
//					TextPart part = iter.next();
//					TextFragment tf = part.getContent();
//					for (Code code : tf.getCodes()) {
//						if (code.hasReference()) {
//							// Resolve reference(s) with GSW, replace the original data
//							if (resolveCodeRefs)
//								code.setData(skelWriter.expandCodeContent(code, getTargetLocale(), 0));
//						}
//					}
//				}			
				if (!hasGenericSkeleton)
					addTU(me, resId, ++tuCounter, tu);
			}
		}
		
		if (!hasGenericSkeleton) return;
		
		List<GenericSkeletonPart> parts = ((GenericSkeleton) skel).getParts();				
//		if (resource instanceof INameable)
//			mimeType = ((INameable) resource).getMimeType();
		
		for (GenericSkeletonPart part : parts) {
			if (SkeletonUtil.isText(part)) {
				//newSkel.add(part.toString());
				newSkel.add(skelWriter.getString(part, EncoderContext.SKELETON));
			}
			else if (SkeletonUtil.isSegmentPlaceholder(part, resource)) {
				processSegmentPlaceholder(part, resource, me, resId, tuCounter, dpCounter);
			}
			else if (SkeletonUtil.isExtSegmentPlaceholder(part, resource)) {
				checkExtParent(part.getParent(), resId, false);
				processSegmentPlaceholder(part, part.getParent(), me, resId, tuCounter, dpCounter); 
			}
			else if (SkeletonUtil.isReference(part)) {
				flushSkeleton(resId, ++dpCounter, me);				
				
//				String refId = SkeletonUtil.getRefId(part);
				Object[] marker = TextFragment.getRefMarker(part.getData());
				
				String refId = (String) marker[0];
				int start = (int) marker[1];
				int end = (int) marker[2];
				
				// If the reference is in the middle of a skel part, like in
				// <![CDATA[[#$sd1_ssf1]]]></p></doc>
				if (start > 0) {
					newSkel.add(part.getData().substring(0, start));
					flushSkeleton(resId, ++dpCounter, me);
				}
				
				IReferenceable referent = skelWriter.getReference(refId);
				
//				if (referent instanceof StorageList) {
//					newSkel.add(skelWriter.getString((StorageList)referent, getTargetLocale(), EncoderContext.SKELETON));
//				}
//				else 
				if (referent == null) {
					MultiEvent group = getGroup(refId);
					if (group != null)
						for (Event event : group) {
							if (event.isNoop()) continue;
							
							IResource res = event.getResource();
							if (res instanceof IReferenceable) {
								IReferenceable r = (IReferenceable) res;
								r.setIsReferent(false);
							}
							me.addEvent(event);
						}
				}
				else if (referent instanceof IResource)
					processResource((IResource) referent, me); // ***
				
				// If the reference is in the middle of a skel part, like in
				// <![CDATA[[#$sd1_ssf1]]]></p></doc>
				if (end < part.getData().length()) {
					newSkel.add(part.getData().substring(end, part.getData().length()));
					flushSkeleton(resId, ++dpCounter, me);
				}
			}
			else if (SkeletonUtil.isSourcePlaceholder(part, resource)) {
				processSourcePlaceholder(part, resource, me, resId, tuCounter, dpCounter);
			}
			else if (SkeletonUtil.isTargetPlaceholder(part, resource)) {
				processTargetPlaceholder(part, resource, me, resId, tuCounter, dpCounter);
			}
			else if (SkeletonUtil.isPropValuePlaceholder(part, resource)) {
				// For both isMultilingual true/false
				newSkel.add(skelWriter.getString(part, EncoderContext.SKELETON));
			}
			else if (SkeletonUtil.isExtSourcePlaceholder(part, resource)) {
				checkExtParent(part.getParent(), resId, true);
				processSourcePlaceholder(part, part.getParent(), me, resId, tuCounter, dpCounter);
			}
			else if (SkeletonUtil.isExtTargetPlaceholder(part, resource)) {
				checkExtParent(part.getParent(), resId, true);
				processTargetPlaceholder(part, part.getParent(), me, resId, tuCounter, dpCounter);
			}
			else if (SkeletonUtil.isExtPropValuePlaceholder(part, resource)) {
				// For both isMultilingual true/false
				checkExtParent(part.getParent(), resId, false);
				newSkel.add(skelWriter.getString(part, EncoderContext.SKELETON));
			}
			else {
				logger.error("Unknown ref type: \"{}\" - {}", resource.getId(), part.toString());
			}
		}
		flushSkeleton(resId, ++dpCounter, me); // Flush remaining skeleton tail
	}

	private void processSourcePlaceholder(GenericSkeletonPart part, IResource resource, 
			MultiEvent me, String resId, int tuCounter, int dpCounter) {
		if (isMultilingual()) {
			if (part.parent instanceof ITextUnit)
				newSkel.add(skelWriter.getContent((ITextUnit)part.parent, null, EncoderContext.TEXT)); // Source goes to skeleton
			else {
				logger.warn("The self-reference must be a text-unit: {}", resId);
				newSkel.add(part.parent.toString());
			}
		}
		else {
			flushSkeleton(resId, ++dpCounter, me);
			addTU(me, resId, ++tuCounter, (ITextUnit)resource);
		}
	}	
	
	private void processTargetPlaceholder(GenericSkeletonPart part, IResource resource, 
			MultiEvent me, String resId, int tuCounter, int dpCounter) {
		// For both isMultilingual true/false
//		if (part.getLocale() == getTargetLocale()) {
		if (part.getLocale().equals(getTargetLocale())) {
			flushSkeleton(resId, ++dpCounter, me);
			addTU(me, resId, ++tuCounter, (ITextUnit)resource); // The target placeholder's content should become a TU
		}
		else {
			//newSkel.add(skelWriter.getContent((TextUnit) resource, getTargetLocale(), 1));
			newSkel.add(skelWriter.getContent((ITextUnit)resource, part.getLocale(), EncoderContext.SKELETON));			
		}
	}
	
	private void processSegmentPlaceholder(GenericSkeletonPart part, IResource resource, 
			MultiEvent me, String resId, int tuCounter, int dpCounter) {
		if (resource instanceof ITextUnit) {
			ITextUnit tu = (ITextUnit)resource;
			TextContainer src = tu.getSource();
			TextContainer trg = part.getLocale() == null ? null : tu.getTarget(part.getLocale());

			// Get the seg ref info
			StringBuilder sb = new StringBuilder(part.data);						
			Object[] marker = TextFragment.getRefMarker(sb);
			String segId = (String) marker[0];
			
			Segment srcSeg = src.getSegments().get(segId);
			Segment trgSeg = trg == null ? null : trg.getSegments().get(segId);
			
			if (srcSeg == null && trgSeg != null && src != null) {
				int segIndex = trg.getSegments().getIndex(segId);
				srcSeg = (Segment) src.get(segIndex);
			}
			
			if (trgSeg == null && srcSeg != null && trg != null) {
				int segIndex = src.getSegments().getIndex(segId);
				trgSeg = (Segment) trg.get(segIndex);
			}
			
			if (part.getLocale() == null) { // Source segment ref
				newSkel.add(skelWriter.getContent(srcSeg.getContent(), null, EncoderContext.TEXT)); // Source goes to skeleton
			}
			else { // Target segment ref
//				if (part.getLocale() == getTargetLocale()) {
				if (part.getLocale().equals(getTargetLocale())) {
					// Create a separate TU for the segment, take src and the target segment from the original multi-segment TU 
					flushSkeleton(resId, ++dpCounter, me);					
					
					TextContainer s = new TextContainer(skelWriter.getContent(srcSeg.getContent(), null, EncoderContext.TEXT));
					TextContainer t = new TextContainer(skelWriter.getContent(trgSeg.getContent(), part.getLocale(), EncoderContext.TEXT));
					ITextUnit segTu = TextUnitUtil.buildTU(null, null, 
							s, t, part.getLocale(), null);
					addTU(me, resId, ++tuCounter, segTu);
				}
				else {
					// Output the target segment of a non-target locale as skeleton
					newSkel.add(skelWriter.getContent(trgSeg.getContent(), part.getLocale(), EncoderContext.TEXT)); // Target goes to skeleton
				}
			}			
		}		
	}
	
	private boolean checkExtParent(IResource parent, String resId, boolean checkReferentFlag) {
		if (parent instanceof IReferenceable) {
			IReferenceable r = (IReferenceable) parent;
			if (checkReferentFlag && !r.isReferent()) {
				logger.warn("Referent flag is not set in parent: {}", resId);
				return false;
			}
			return true;
		}
//		else {
//			logger.warn("Invalid parent type: {}", resId);
//			return false;
//		}
		return true;
	}

}
