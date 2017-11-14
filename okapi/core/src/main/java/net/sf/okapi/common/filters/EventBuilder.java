/*===========================================================================
  Copyright (C) 2009-2011 by the Okapi Framework contributors
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

import java.util.Collections;
import java.util.EmptyStackException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Stack;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.IdGenerator;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.exceptions.OkapiIllegalFilterOperationException;
import net.sf.okapi.common.filters.PropertyTextUnitPlaceholder.PlaceholderAccessType;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.DocumentPart;
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.resource.INameable;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.StartGroup;
import net.sf.okapi.common.resource.StartSubDocument;
import net.sf.okapi.common.resource.StartSubfilter;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.common.resource.TextUnit;
import net.sf.okapi.common.skeleton.GenericSkeleton;
import net.sf.okapi.common.skeleton.GenericSkeletonPart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EventBuilder provides a simplified API for filter writers that hides the low level resource API.
 * <p>
 * EventBuilder allows filter writers to think in terms of start and end calls. For example, to produce a
 * non-translatable {@link Event} you would use startDocumentPart() and endDocumentPart(). For a text-based
 * {@link Event} you would use startTextUnit() and endTextUnit().
 * <p>
 * More complex cases such as tags with embedded translatable text can also be handled. See the AbstractMarkupFilter,
 * HtmlFilter and OpenXmlFilter for examples of using EventBuilder.
 */
public class EventBuilder {
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	private String mimeType;
	private IdGenerator groupId;
	private IdGenerator textUnitId;
	private IdGenerator subDocumentId;
	private IdGenerator documentPartId;
	
	private Stack<Event> tempFilterEventStack;
	private Stack<Code> codeStack;
	private List<Event> filterEvents;
	private List<Event> referencableFilterEvents;
	private boolean done = false;
	private boolean preserveWhitespace;
	private GenericSkeleton currentSkeleton;
	private DocumentPart currentDocumentPart;
	private String rootId;
	private boolean subFilter;
	// null target means we are processing source
	private LocaleId targetLocale;

	/**
	 * Instantiates a new EventBuilder.
	 */
	public EventBuilder() {
		reset(null, null);
	}

	/**
	 * Instantiates a new EventBuilder with a root ID.
	 * @param rootId new root id (can be null or empty)
	 * @param filter subfilter to use (can be null)
	 */
	public EventBuilder(String rootId, IFilter filter) {
		reset(rootId, filter);
	}

	/**
	 * Are we finished parsing the input document?
	 * 
	 * @return true of the END_DOCUMENT event was received.
	 */
	public boolean hasNext() {
		return !done;
	}

	/**
	 * Return the next filter event waiting in the event queue.
	 * 
	 * @return the current {@link Event}
	 */
	public Event next() {
		Event event;

		if (hasNext()) {
			if (!referencableFilterEvents.isEmpty()) {
				return referencableFilterEvents.remove(0);
			} else if (!filterEvents.isEmpty()) {
				event = filterEvents.remove(0);
				if (event.getEventType() == EventType.END_DOCUMENT) {
					done = true;
				}				
				// handle case if called by a subfilter
				if (subFilter && event.isEndSubfilter()) {					
						done = true;
				}
				return event;
			}
		}

		return null;
	}

	/**
	 * Add an {@link Event} at the end of the current {@link Event} queue.
	 * 
	 * @param event
	 *            The {@link Event} to be added
	 */
	public void addFilterEvent(Event event) {		
		switch (event.getEventType()) {
//		case START_SUBFILTER:
//			if (isCurrentTextUnit()) {
//				StartSubfilter ssf = event.getStartSubfilter();
//				if (ssf == null) break;
//				
//				SubFilter sf = ssf.getSubfilter();
//				if (sf == null) break;
//				
////				ssf.setIsReferent(true);
////				Code c = new Code(TagType.PLACEHOLDER, ssf.getName(), TextFragment.makeRefMarker(ssf.getId()));
////				c.setReferenceFlag(true);				
//				
//				Code c = ssf.getSubfilter().createRefCode();
//				startCode(c);
//				endCode();
//				referencableFilterEvents.add(event);
//			} else {
//				filterEvents.add(event);
//			}
//			break;
		
		case START_GROUP:
		case START_SUBFILTER:
			if (isCurrentTextUnit()) {
				StartGroup sg = event.getStartGroup();
				sg.setIsReferent(true);
				Code c = new Code(TagType.PLACEHOLDER, sg.getName(), TextFragment.makeRefMarker(sg.getId()));
				c.setReferenceFlag(true);
				startCode(c);
				endCode();
				referencableFilterEvents.add(event);
			} else {
				filterEvents.add(event);
			}
			break;
		case TEXT_UNIT:
			// JEH not sure why we had this line of code here, commenting out for now.
			//event.getTextUnit().getSource().getFirstContent().renumberCodes();
			filterEvents.add(event);
			break;
		default:
			filterEvents.add(event);
			break;
		}		
	}
	
	public void addFilterEvents(List<Event> events) {
		for (Event event : events) {
			// assume text unit events have already gone through decoding
			filterEvents.add(event);
		}
	}
	
	/**
	 * Cancel current processing and add the CANCELED {@link Event} to the event queue.
	 */
	public void cancel() {
		// flush out all pending events
		filterEvents.clear();
		referencableFilterEvents.clear();

		Event event = new Event(EventType.CANCELED);
		filterEvents.add(event);
	}

	/*
	 * Return the current buffered Event without removing it.
	 */
	public Event peekTempEvent() {
		if (tempFilterEventStack.isEmpty()) {
			return null;
		}
		return tempFilterEventStack.peek();
	}

	/*
	 * Return the current buffered Event and removes it from the buffer.
	 */
	public Event popTempEvent() {
		if (tempFilterEventStack.isEmpty()) {
			return null;
		}
		return tempFilterEventStack.pop();
	}

	/**
	 * Flush all remaining events from the {@link Event} queues
	 */
	public void flushRemainingTempEvents() {
		if (hasUnfinishedSkeleton()) {
			endDocumentPart();
		} else if (!tempFilterEventStack.isEmpty()) {
			// go through filtered object stack and close them one by one
			while (!tempFilterEventStack.isEmpty()) {
				Event fe = tempFilterEventStack.peek();
				if (fe.getEventType() == EventType.START_GROUP) {
					StartGroup sg = (StartGroup) fe.getResource();
					endGroup((GenericSkeleton) sg.getSkeleton());
				}
				else if (fe.getEventType() == EventType.START_SUBFILTER) {
					StartSubfilter sf = (StartSubfilter) fe.getResource();
					endGroup((GenericSkeleton) sf.getSkeleton());
				}
				else if (fe.getEventType() == EventType.TEXT_UNIT) {
					endTextUnit();
				}
			}
		}
	}

	/**
	 * Is the current buffered {@link Event} a {@link TextUnit}?
	 * 
	 * @return true if TextUnit, false otherwise.
	 */
	public boolean isCurrentTextUnit() {
		Event e = peekTempEvent();
		if (e != null && e.getEventType() == EventType.TEXT_UNIT) {
			return true;
		}
		return false;
	}

	/**
	 * Is the current buffered {@link Event} a complex {@link TextUnit}? A complex TextUnit is one which carries along
	 * with it it's surrounding formatting or skeleton such &lt;p&gt; text &lt;/p&gt; or &lt;title&gt; text &lt;/title&gt;
	 * 
	 * @return true, if current {@link Event} is a complex text unit, false otherwise.
	 */
	public boolean isCurrentComplexTextUnit() {
		Event e = peekTempEvent();
		if (e != null && e.getEventType() == EventType.TEXT_UNIT && e.getResource().getSkeleton() != null) {
			return true;
		}
		return false;
	}

	/**
	 * Is the current buffered {@link Event} a {@link StartGroup}?
	 * 
	 * @return true, if current {@link Event} is a {@link StartGroup}
	 */
	public boolean isCurrentGroup() {
		Event e = peekTempEvent();
		if (e != null && e.getEventType() == EventType.START_GROUP) {
			return true;
		}
		return false;
	}
	
	/**
	 * Is the current buffered {@link Event} a {@link StartSubfilter}?
	 * 
	 * @return true, if current {@link Event} is a {@link StartSubfilter}
	 */
	public boolean isCurrentSubfilter() {
		Event e = peekTempEvent();
		if (e != null && e.getEventType() == EventType.START_SUBFILTER) {
			return true;
		}
		return false;
	}

	/**
	 * Is the filter is inside text run?
	 * 
	 * @return true, if is inside text run
	 */
	public boolean isInsideTextRun() {
		return isCurrentTextUnit();
	}

	/**
	 * Can we start new {@link TextUnit}? A new {@link TextUnit} can only be started if the current one has been ended
	 * with endTextUnit. Or no {@link TextUnit} has been created yet.
	 * 
	 * @return true, if can start a new {@link TextUnit}
	 */
	public boolean canStartNewTextUnit() {
		if (isCurrentTextUnit()) {
			return false;
		}
		return true;
	}

	/**
	 * Are there any queued events? We queue events in the correct order as expected by the Okapi filter writers
	 * (IWriter).
	 * 
	 * @return true, if successful
	 */
	public boolean hasQueuedEvents() {
		if (filterEvents.isEmpty()) {
			return false;
		}
		return true;
	}

	public String findMostRecentParentId() {	
		if (isCurrentComplexTextUnit()) {
			ITextUnit tu = peekMostRecentTextUnit();
			if (tu != null) {
				return tu.getId();
			}			
		}
		
		if (isCurrentGroup()) {
			StartGroup parentGroup = peekMostRecentGroup();
			if (parentGroup != null) {
				return parentGroup.getId();
			}			
		}
		
		StartSubDocument parentSubDocument = peekMostRecentSubDocument();
		if (parentSubDocument != null) {
			return parentSubDocument.getId();
		}
		
		return null;	
	}
	
	public String findMostRecentParentName() {	
		if (isCurrentComplexTextUnit()) {
			ITextUnit tu = peekMostRecentTextUnit();
			if (tu != null) {
				return tu.getName();
			}			
		}
		
		if (isCurrentGroup()) {
			StartGroup parentGroup = peekMostRecentGroup();
			if (parentGroup != null) {
				return parentGroup.getName();
			}			
		}
		
		StartSubDocument parentSubDocument = peekMostRecentSubDocument();
		if (parentSubDocument != null) {
			return parentSubDocument.getName();
		}
		
		return null;	
	}

	/**
	 * Find in our buffered queue the most recent TextUnit
	 * that has an assigned name
	 * @return - the TextUnit name, null if not found
	 */
	public String findMostRecentTextUnitName() {	
		if (tempFilterEventStack.isEmpty()) {
			return null;
		}
		// skip current TextUnit - the one we are currently processing
		for (Event fe : tempFilterEventStack) {
			if (fe.getEventType() == EventType.TEXT_UNIT && fe.getTextUnit().getName() != null) {
				return fe.getTextUnit().getName();
			}
		}
		return null;	
	}
	
	/**
	 * Peek at the most recently created {@link StartGroup} or {@link StartSubfilter}.
	 * 
	 * @return the filter event
	 */
	public StartGroup peekMostRecentGroup() {
		if (tempFilterEventStack.isEmpty()) {
			return null;
		}
		// the normal stack iterator gives the elements in the wrong order
		int lastIndex = tempFilterEventStack.size() - 1;
		for (int i = lastIndex; i >= 0; i--) {
			Event fe = tempFilterEventStack.get(i);
			if (fe.getEventType() == EventType.START_GROUP ||
					fe.getEventType() == EventType.START_SUBFILTER) {
				StartGroup g = (StartGroup) fe.getResource();
				return g;
			}
		}
		return null;
	}
	
	/**
	 * Peek at the most recently created {@link StartSubDocument}
	 * 
	 * @return the filter event
	 */
	public StartSubDocument peekMostRecentSubDocument() {
		if (tempFilterEventStack.isEmpty()) {
			return null;
		}
		// the normal stack iterator gives the elements in the wrong order
		int lastIndex = tempFilterEventStack.size() - 1;
		for (int i = lastIndex; i >= 0; i--) {
			Event fe = tempFilterEventStack.get(i);
			if (fe.getEventType() == EventType.START_SUBDOCUMENT) {
				StartSubDocument sd = (StartSubDocument) fe.getResource();
				return sd;
			}
		}
		return null;
	}

	/**
	 * Peek At the most recently created {@link TextUnit}.
	 * 
	 * @return the filter event
	 */
	public ITextUnit peekMostRecentTextUnit() {
		if (tempFilterEventStack.isEmpty()) {
			return null;
		}
		// the normal stack iterator gives the elements in the
		int lastIndex = tempFilterEventStack.size() - 1;
		for (int i = lastIndex; i >= 0; i--) {
			Event fe = tempFilterEventStack.get(i);
			if (fe.getEventType() == EventType.TEXT_UNIT) {
				ITextUnit tu = (ITextUnit)fe.getResource();
				return tu;
			}
		}
		return null;
	}
	
	/**
	 * Peek at the most recently created {@link StartGroup}.
	 * 
	 * @return the filter event
	 */
	public Code peekMostRecentCode() {
		if (codeStack.isEmpty()) {
			return null;
		}
		
		Code c = null;
		try {
			c = codeStack.peek();
		} catch(EmptyStackException e) {
			// ignore exception, we return a null value
		}
		return c;
	}

	/**
	 * Is there an unfinished {@link DocumentPart} (aka skeleton)?
	 * 
	 * @return true, if successful
	 */
	public boolean hasUnfinishedSkeleton() {
		if (currentSkeleton == null) {
			return false;
		}
		return true;
	}

	/**
	 * Does the current {@link TextUnit} have a parent?
	 * 
	 * @return true, if successful
	 */
	public boolean hasParentTextUnit() {
		if (tempFilterEventStack.isEmpty()) {
			return false;
		}
		boolean first = true;
		// skip current TextUnit - the one we are currently processing
		for (Event fe : tempFilterEventStack) {
			if (fe.getEventType() == EventType.TEXT_UNIT && !first) {
				return true;
			}
			first = false;
		}
		return false;
	}

	/**
	 * @param rootId new root id (can be null or empty)
	 * @param filter subfilter to use (can be null)
	 * Reset {@link IFilter} for a new input. Callers should reset the EventBuilder for each input.
	 */
	public void reset(String rootId, IFilter filter) {
		this.rootId = rootId;
		this.subFilter = filter != null && 
				filter instanceof SubFilter; // All sub-filters subclass from this class
		groupId = new IdGenerator(rootId, IdGenerator.START_GROUP);		
		textUnitId = new IdGenerator(rootId, IdGenerator.TEXT_UNIT);
		documentPartId = new IdGenerator(rootId, IdGenerator.DOCUMENT_PART);
		subDocumentId = new IdGenerator(rootId, IdGenerator.START_SUBDOCUMENT);		
		
		done = false;
		this.preserveWhitespace = true;

		referencableFilterEvents = new LinkedList<Event>();
		filterEvents = new LinkedList<Event>();

		tempFilterEventStack = new Stack<Event>();

		codeStack = new Stack<Code>();
		currentSkeleton = null;
		currentDocumentPart = null;
		targetLocale = null;
	}

	// ////////////////////////////////////////////////////////////////////////
	// Start and Finish Methods
	// ////////////////////////////////////////////////////////////////////////

	/**
	 * Add the START_SUBDOCUMENT {@link Event} to the event queue.
	 * @return the {@link StartSubDocument} created and added,
	 *         or null if there is no <code>subFilter</code>
	 */
	public StartSubDocument startSubDocument() {
		StartSubDocument startSubDocument = null;
		if (!subFilter) {
			if (hasUnfinishedSkeleton()) {
				endDocumentPart();
			}
	
			startSubDocument = new StartSubDocument(subDocumentId.createId());
			Event event = new Event(EventType.START_SUBDOCUMENT, startSubDocument);
			filterEvents.add(event);
			LOGGER.debug("Start Sub-Document for " + startSubDocument.getId());
		}
		return startSubDocument;
	}

	/**
	 * Add the END_SUBDOCUMENT {@link Event} to the event queue.
	 */
	public void endSubDocument() {
		if (!subFilter) {
			Ending endDocument = new Ending(subDocumentId.createId(IdGenerator.END_SUBDOCUMENT));
			Event event = new Event(EventType.END_SUBDOCUMENT, endDocument);
			filterEvents.add(event);
			LOGGER.debug("End Sub-Document for " + endDocument.getId());
		}
	}

	// ////////////////////////////////////////////////////////////////////////
	// Private methods used for processing properties and text embedded within
	// tags
	// ////////////////////////////////////////////////////////////////////////

	private ITextUnit embeddedTextUnit(PropertyTextUnitPlaceholder propOrText, String tag) {
		ITextUnit tu = new TextUnit(textUnitId.createId(), propOrText.getValue());
		tu.setPreserveWhitespaces(this.preserveWhitespace);

		tu.setMimeType(propOrText.getMimeType());
		tu.setIsReferent(true);
		tu.setType(propOrText.getElementType() == null ? propOrText.getName() : propOrText.getElementType());

		GenericSkeleton skel = new GenericSkeleton();

		skel.add(tag.substring(propOrText.getMainStartPos(), propOrText.getValueStartPos()));
		skel.addContentPlaceholder(tu);
		skel.add(tag.substring(propOrText.getValueEndPos(), propOrText.getMainEndPos()));
		tu.setSkeleton(skel);

		postProcessTextUnit(tu);
		return tu;
	}

	private void embeddedWritableProp(INameable resource, PropertyTextUnitPlaceholder propOrText, String tag,
			LocaleId locale) {
		setPropertyBasedOnLocale(resource, locale, new Property(propOrText.getName(), propOrText.getValue(), false));
		currentSkeleton.add(tag.substring(propOrText.getMainStartPos(), propOrText.getValueStartPos()));
		currentSkeleton.addValuePlaceholder(resource, propOrText.getName(), locale);
		currentSkeleton.add(tag.substring(propOrText.getValueEndPos(), propOrText.getMainEndPos()));
	}

	private void embeddedReadonlyProp(INameable resource, PropertyTextUnitPlaceholder propOrText, String tag,
			LocaleId locId) {
		
		setPropertyBasedOnLocale(resource, locId, new Property(propOrText.getName(), propOrText.getValue(), true));
		currentSkeleton.add(tag.substring(propOrText.getMainStartPos(), propOrText.getMainEndPos()));
	}

	private INameable setPropertyBasedOnLocale(INameable resource, LocaleId locale, Property property) {
		if (locale == null) {
			resource.setSourceProperty(property);
		} else if (locale.equals(LocaleId.EMPTY)) {
			resource.setProperty(property);
		} else {
			resource.setTargetProperty(locale, property);
		}

		return resource;
	}

	private boolean processAllEmbedded(String tag, LocaleId locale,
			List<PropertyTextUnitPlaceholder> propertyTextUnitPlaceholders, boolean inlineCode) {
		return processAllEmbedded(tag, locale, propertyTextUnitPlaceholders, inlineCode, null);
	}

	private boolean isTextPlaceHoldersOnly(List<PropertyTextUnitPlaceholder> propertyTextUnitPlaceholders) {
		boolean text = false;
		boolean nontext = false;
		for (PropertyTextUnitPlaceholder propOrText : propertyTextUnitPlaceholders) {
			if (propOrText.getAccessType() == PlaceholderAccessType.TRANSLATABLE) {
				text = true;
			} else {
				nontext = true;
			}
		}

		return (text && !nontext);

	}

	private boolean processAllEmbedded(String tag, LocaleId locale,
			List<PropertyTextUnitPlaceholder> propertyTextUnitPlaceholders, boolean inlineCode, ITextUnit parentTu) {
		int propOrTextId = -1;
		boolean textPlaceholdersOnly = isTextPlaceHoldersOnly(propertyTextUnitPlaceholders);
		INameable resource = null;

		// we need to clear out the current Code data as we will append the new
		// skeleton below
		Code c = peekMostRecentCode();
		if (c != null) {
			c.setData("");
		}

		// set the resource that will hold all the references
		if (inlineCode) {
			if (textPlaceholdersOnly) {
				resource = parentTu;
			} else {
				resource = new DocumentPart(documentPartId.createId(), inlineCode);
			}
		} else {
			if (parentTu != null) {
				resource = parentTu;
			} else {
				resource = currentDocumentPart;
			}
		}

		// sort to make sure we do the Properties or Text in order
		Collections.sort(propertyTextUnitPlaceholders);

		// add the part up to the first prop or text
		PropertyTextUnitPlaceholder pt = propertyTextUnitPlaceholders.get(0);
		currentSkeleton.add(tag.substring(0, pt.getMainStartPos()));

		for (PropertyTextUnitPlaceholder propOrText : propertyTextUnitPlaceholders) {
			propOrTextId++;

			// add the markup between the props or text
			if (propOrTextId >= 1 && propOrTextId < propertyTextUnitPlaceholders.size()) {
				PropertyTextUnitPlaceholder pt1 = propertyTextUnitPlaceholders.get(propOrTextId - 1);
				PropertyTextUnitPlaceholder pt2 = propertyTextUnitPlaceholders.get(propOrTextId);
				currentSkeleton.add(tag.substring(pt1.getMainEndPos(), pt2.getMainStartPos()));
			}

			if (propOrText.getAccessType() == PlaceholderAccessType.TRANSLATABLE) {
				ITextUnit tu = embeddedTextUnit(propOrText, tag);
				currentSkeleton.addReference(tu);
				referencableFilterEvents.add(new Event(EventType.TEXT_UNIT, tu));
			} else if (propOrText.getAccessType() == PlaceholderAccessType.WRITABLE_PROPERTY) {
				embeddedWritableProp(resource, propOrText, tag, locale);
			} else if (propOrText.getAccessType() == PlaceholderAccessType.READ_ONLY_PROPERTY) {
				embeddedReadonlyProp(resource, propOrText, tag, locale);
			} else if (propOrText.getAccessType() == PlaceholderAccessType.NAME) {
				resource.setName(propOrText.getValue() + "-" + propOrText.getName());
				embeddedReadonlyProp(resource, propOrText, tag, locale);
			} else {
				throw new OkapiIllegalFilterOperationException("Unkown Property or TextUnit type");
			}
		}

		// add the remaining markup after the last prop or text
		pt = propertyTextUnitPlaceholders.get(propertyTextUnitPlaceholders.size() - 1);
		currentSkeleton.add(tag.substring(pt.getMainEndPos()));

		// setup references based on type
		if (inlineCode) {
			Code code = peekMostRecentCode();
			if (!textPlaceholdersOnly) {
				if (code != null) {
					code.appendReference(resource.getId());
				}				
				resource.setSkeleton(currentSkeleton);
				// we needed to create a document part to hold the
				// writable/localizables
				referencableFilterEvents.add(new Event(EventType.DOCUMENT_PART, resource));
			} else {
				// all text - the parent TU hold the references instead of a
				// DocumentPart
				if (code != null) {
					code.append(currentSkeleton.toString());
					code.setReferenceFlag(true);
				}				
			}
		}

		return textPlaceholdersOnly;
	}

	// ////////////////////////////////////////////////////////////////////////
	// TextUnit Methods
	// ////////////////////////////////////////////////////////////////////////
	/**
	 * Start and end a {@link TextUnit}. Also create a TextUnit {@link Event} and add it to the event queue.
	 * 
	 * @param text
	 *            the text used to prime the {@link TextUnit}
	 */
	public void addTextUnit(String text) {
		startTextUnit(text, null, null, null);
		endTextUnit();
	}

	/**
	 * Start a {@link TextUnit}. Also create a TextUnit {@link Event} and add it to the event queue.
	 * 
	 * @param text
	 *            the text used to prime the {@link TextUnit}
	 */
	public void startTextUnit(String text) {
		startTextUnit(text, null, null, null);
	}

	/**
	 * Start a {@link TextUnit}. Also create a TextUnit {@link Event} and add it to the event queue.
	 */
	public void startTextUnit() {
		startTextUnit(null, null, null, null);
	}

	/**
	 * Start a complex {@link TextUnit}. Also create a TextUnit {@link Event} and add it to the event queue.
	 * 
	 * @param startMarker
	 *            the tag that begins the complex {@link TextUnit}
	 */
	public void startTextUnit(GenericSkeleton startMarker) {
		startTextUnit(null, startMarker, null, null);
	}

	/**
	 * Start a complex {@link TextUnit} with actionable (translatable, writable or read-only) attributes. Also create a
	 * TextUnit {@link Event} and add it to the event queue.
	 * 
	 * @param startMarker
	 *            the tag that begins the complex {@link TextUnit}
	 * @param propertyTextUnitPlaceholders
	 *            the list of actionable {@link TextUnit} or {@link Properties} with offset information into the tag.
	 */
	public void startTextUnit(GenericSkeleton startMarker,
			List<PropertyTextUnitPlaceholder> propertyTextUnitPlaceholders) {
		startTextUnit(null, startMarker, null, propertyTextUnitPlaceholders);
	}

	/**
	 * Start a complex {@link TextUnit} with actionable (translatable, writable or read-only) attributes. Also create a
	 * TextUnit {@link Event} and add it to the event queue.
	 * 
	 * @param startMarker
	 *            the tag that begins the complex {@link TextUnit}
	 * @param propertyTextUnitPlaceholders
	 *            the list of actionable {@link TextUnit} or {@link Properties} with offset information into the tag.
	 * @param text
	 *            the text used to prime the {@link TextUnit}
	 */
	public void startTextUnit(String text, GenericSkeleton startMarker,
			List<PropertyTextUnitPlaceholder> propertyTextUnitPlaceholders) {
		startTextUnit(text, startMarker, null, propertyTextUnitPlaceholders);
	}

	/**
	 * Start a complex {@link TextUnit} with actionable (translatable, writable or read-only) attributes. Also create a
	 * TextUnit {@link Event} and add it to the event queue.
	 * 
	 * @param startMarker
	 *            the tag that begins the complex {@link TextUnit}
	 * @param text
	 *            the text used to prime the {@link TextUnit}
	 * @param locale
	 *            the locale of the text
	 * @param propertyTextUnitPlaceholders
	 *            the list of actionable {@link TextUnit} or {@link Properties} with offset information into the tag.
	 */
	public void startTextUnit(String text, GenericSkeleton startMarker, LocaleId locale,
			List<PropertyTextUnitPlaceholder> propertyTextUnitPlaceholders) {
		if (hasUnfinishedSkeleton()) {
			endDocumentPart();
		}

		ITextUnit tu;
		tu = new TextUnit(textUnitId.createId(), text);
		tu.setMimeType(this.mimeType);
		tu.setPreserveWhitespaces(this.preserveWhitespace);

		// test for pre-existing parent TextUnit and set the current TextUnit as a child
		Event e = peekTempEvent();
		if (e != null && e.getEventType() == EventType.TEXT_UNIT) {
			ITextUnit parentTu = e.getTextUnit();
			if (parentTu.getSource().isEmpty()) {
				convertTempTextUnitToDocumentPart();
			}
			else {
				tu.setIsReferent(true);
				GenericSkeleton skel = (GenericSkeleton) parentTu.getSkeleton();
				if (skel == null) {
					skel = new GenericSkeleton();
				}
				skel.addReference(tu);
				parentTu.setSkeleton(skel);
			}
		}

		if (startMarker != null && propertyTextUnitPlaceholders != null) {
			currentSkeleton = new GenericSkeleton();
			processAllEmbedded(startMarker.toString(), locale, propertyTextUnitPlaceholders, false, tu);
			tu.setSkeleton(currentSkeleton);
			currentSkeleton.addContentPlaceholder(tu);
			tempFilterEventStack.push(new Event(EventType.TEXT_UNIT, tu, currentSkeleton));
			currentSkeleton = null;
			return;
		} else if (startMarker != null) {
			GenericSkeleton skel = new GenericSkeleton((GenericSkeleton) startMarker);
			skel.addContentPlaceholder(tu);
			tempFilterEventStack.push(new Event(EventType.TEXT_UNIT, tu, skel));
			return;
		} else {
			tempFilterEventStack.push(new Event(EventType.TEXT_UNIT, tu));
		}
	}
	
	/**
	 * Convert the skeleton attached to an in-progress {@link TextUnit} into
	 * a {@link DocumentPart}.  This is used when a TextUnit has been started
	 * but then is immediately ended without any content.  The final skeleton
	 * part in the TU is always a self-reference, which is stripped during
	 * the conversion.
	 */
	public void convertTempTextUnitToDocumentPart() {
		if (peekTempEvent().getEventType() != EventType.TEXT_UNIT) {
			return;
		}
		Event e = popTempEvent();
		ITextUnit textUnit = (e.getTextUnit());
		GenericSkeleton oldTuSkeleton = (GenericSkeleton)textUnit.getSkeleton();
		if (oldTuSkeleton != null) {
			List<GenericSkeletonPart> parts = oldTuSkeleton.getParts();
			// Skip the trailing [$$self$] reference
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < parts.size() - 1; i++) {
				sb.append(parts.get(i).toString());
			}
			addToDocumentPart(sb.toString());
			endDocumentPart();
		}
	}

	/**
	 * End the current {@link TextUnit} and place the {@link Event} on the event queue.
	 * 
	 * @return the ended {@link TextUnit}
	 */
	public ITextUnit endTextUnit() {
		return endTextUnit(null, null, null);
	}

	/**
	 * End the current {@link TextUnit} and place the {@link Event} on the event queue.
	 * 
	 * @param endMarker
	 *            the tag that ends the complex {@link TextUnit}
	 * @return the ended {@link TextUnit}
	 */
	public ITextUnit endTextUnit(GenericSkeleton endMarker) {
		return endTextUnit(endMarker, null, null);
	}

	/**
	 * End the current {@link TextUnit} and place the {@link Event} on the event queue.
	 * 
	 * @param endMarker
	 *            the tag that ends the complex {@link TextUnit}
	 * @param locale
	 *            the locale of the text
	 * @param propertyTextUnitPlaceholders
	 *            the list of actionable {@link TextUnit} or {@link Properties} with offset information into the tag.
	 * @return the ended {@link TextUnit}
	 * 
	 * @throws OkapiIllegalFilterOperationException if we try to perform an invalid operation.
	 */
	public ITextUnit endTextUnit(GenericSkeleton endMarker, LocaleId locale,
			List<PropertyTextUnitPlaceholder> propertyTextUnitPlaceholders) {
		Event tempTextUnit;

		if (!isCurrentTextUnit()) {
			if (endMarker != null) {
				addDocumentPart(endMarker.toString());
			}
			LOGGER.debug("Trying to end a TextUnit that does not exist.");
			return null;
		}

		tempTextUnit = popTempEvent();

		if (endMarker != null) {
			GenericSkeleton skel = (GenericSkeleton) tempTextUnit.getResource().getSkeleton();
			// need this to handle non-wellformed cases such as a
			// TextUnit started with text but ending with a TextUnit tag
			if (skel == null) {
				skel = new GenericSkeleton();
			}
			skel.add((GenericSkeleton) endMarker);
		}
		
		tempTextUnit.setResource(postProcessTextUnit(tempTextUnit.getTextUnit()));
		filterEvents.add(tempTextUnit);
		
		// before we close this TextUnit correctly set the code ids 
		ITextUnit tu = tempTextUnit.getTextUnit();
		tu.getSource().getFirstContent().renumberCodes();
		for (LocaleId t : tu.getTargetLocales()) {
			// align the code ids with the target
			tu.getTarget(t).getFirstContent().renumberCodes();
			TextFragment targetFrag = tu.getTarget(t).getFirstContent();
			tu.getSource().getFirstContent().alignCodeIds(targetFrag);			
		}
		return (ITextUnit)tempTextUnit.getResource();
	}

	/**
	 * Adds text to the current {@link TextUnit}
	 * 
	 * @param text
	 *            the text
	 * 
	 * @throws OkapiIllegalFilterOperationException if we try to perform an invalid operation.
	 */
	public void addToTextUnit(String text) {
		if (!isCurrentTextUnit()) {
			throw new OkapiIllegalFilterOperationException("Trying to add text to a TextUnit that does not exist.");
		}

		ITextUnit tu = peekMostRecentTextUnit();
		if (tu == null) return;
		
		// We can use the first part as nothing is segment at this point
		LocaleId trgLoc = getTargetLocale(); 
		if (trgLoc == null) {
			tu.getSource().getFirstContent().append(text);
		} else {
			if (tu.getTarget(trgLoc) == null) {
				tu.createTarget(trgLoc, true, ITextUnit.CREATE_EMPTY);
			}
			tu.getTarget(trgLoc).getFirstContent().append(text);
		}
	}
	
	/**
	 * Adds a child TextUnit to the current (parent) {@link TextUnit}
	 * 
	 * @param textUnit the {@link ITextUnit} to add
	 * 
	 * @throws OkapiIllegalFilterOperationException if we try to perform an invalid operation.
	 */
	public void addToTextUnit (ITextUnit textUnit) {
		if (!isCurrentTextUnit()) {
			throw new OkapiIllegalFilterOperationException(
					"Trying to add a TextUnit to a TextUnit that does not exist.");
		}

		ITextUnit tu = new TextUnit(textUnitId.createId());
		tu.setPreserveWhitespaces(this.preserveWhitespace);
		tu.setMimeType(this.mimeType);
		tu.setIsReferent(true);

		ITextUnit parentTU = peekMostRecentTextUnit();
		if (parentTU == null) return;

		GenericSkeleton skel = (GenericSkeleton) parentTU.getSkeleton();
		if (skel == null) {
			skel = new GenericSkeleton();
		}
		skel.addReference(tu);
	}

	/**
	 * Add a {@link Code} to the current {@link TextUnit}. Nothing is actionable within the tag (i.e., no properties or
	 * translatable, localizable text)
	 * 
	 * @param code
	 *            the code type
	 * 
	 * @throws OkapiIllegalFilterOperationException if we try to perform an invalid operation.
	 */
	public void addToTextUnit(Code code) {
		addToTextUnit(code, true);
	}

	/**
	 * Add a {@link Code} to the current {@link TextUnit}. Nothing is actionable within the tag (i.e., no properties or
	 * translatable, localizable text)
	 * 
	 * @param code
	 *            the code type
	 * 
	 * @param endCodeNow
	 *            do we end the code now or delay?
	 * 
	 * @throws OkapiIllegalFilterOperationException if we try to perform an invalid operation.
	 */
	public void addToTextUnit(Code code, boolean endCodeNow) {
		if (!isCurrentTextUnit()) {
			throw new OkapiIllegalFilterOperationException("Trying to add a Code to a TextUnit that does not exist.");
		}

		startCode(code);
		if (endCodeNow) {
			endCode();
		}
	}
	
	/**
	 * Add a {@link Code} to the current {@link TextUnit}. The Code contains actionable (i.e., translatable,
	 * localizable) attributes.
	 * 
	 * @param code
	 *            the code
	 * @param endCodeNow
	 *            do we end the code now or delay?
	 * @param propertyTextUnitPlaceholders
	 *            the list of actionable {@link TextUnit} or {@link Properties} with offset information into the tag.
	 */
	public void addToTextUnit(Code code, boolean endCodeNow, List<PropertyTextUnitPlaceholder> propertyTextUnitPlaceholders) {
		addToTextUnit(code, endCodeNow, null, propertyTextUnitPlaceholders);
	}

	/**
	 * Add a {@link Code} to the current {@link TextUnit}. The Code contains actionable (i.e., translatable,
	 * localizable) attributes.
	 * 
	 * @param code
	 *            the code
	 * @param endCodeNow
	 *            do we end the code now or delay?
	 * @param locale
	 *            the language of the text
	 * @param propertyTextUnitPlaceholders
	 *            the list of actionable {@link TextUnit} or {@link Properties} with offset information into the tag.
	 * @throws OkapiIllegalFilterOperationException if we try to perform an invalid operation.
	 */
	public void addToTextUnit(Code code, boolean endCodeNow, LocaleId locale, List<PropertyTextUnitPlaceholder> propertyTextUnitPlaceholders) {

		if (!isCurrentTextUnit()) {
			throw new OkapiIllegalFilterOperationException("Trying to add Codes to a TextUnit that does not exist.");
		}

		currentSkeleton = new GenericSkeleton();
		ITextUnit tu = peekMostRecentTextUnit();
		if (tu != null) {
			startCode(code);
			processAllEmbedded(code.toString(), locale, propertyTextUnitPlaceholders, true, tu);
			if (endCodeNow) {
				endCode();
			}
		}
		currentSkeleton = null;
	}

	/**
	 * Appends text to the first data part of the skeleton.
	 * 
	 * @param text
	 *            the text to happend.
	 * 
	 * @throws OkapiIllegalFilterOperationException if we try to perform an invalid operation.
	 */
	public void appendToFirstSkeletonPart(String text) {
		Event tempTextUnit = peekTempEvent();
		GenericSkeleton skel = (GenericSkeleton) tempTextUnit.getResource().getSkeleton();
		skel.appendToFirstPart(text);
	}

	// ////////////////////////////////////////////////////////////////////////
	// Group Methods
	// ////////////////////////////////////////////////////////////////////////

	/**
	 * Create a {@link StartGroup} {@link Event} and add it to the event queue.
	 * 
	 * @param startMarker
	 *            the tag which starts the group
	 * @param commonTagType
	 *            the tag name or abstract type for this group.
	 * @return the newly created StartGroup
	 */
	public StartGroup startGroup(GenericSkeleton startMarker, String commonTagType) {
		return startGroup(startMarker, commonTagType, null, null);
	}

	/**
	 * Create a {@link StartGroup} {@link Event} and add it to the event queue.
	 * 
	 * @param startMarker
	 *            the tag which starts the group.
	 * @param commonTagType
	 *            the tag name or abstract type for this group.
	 * @param locale
	 *            the language of any actionable items
	 * @param propertyTextUnitPlaceholders
	 *            the list of actionable {@link TextUnit} or {@link Properties} with offset information into the tag.
	 * @return the newly created StartGroup
	 */
	public StartGroup startGroup(GenericSkeleton startMarker, String commonTagType, LocaleId locale,
			List<PropertyTextUnitPlaceholder> propertyTextUnitPlaceholders) {
		if (startMarker == null) {
			throw new OkapiIllegalFilterOperationException("startMarker for Group is null");
		}

		if (hasUnfinishedSkeleton()) {
			endDocumentPart();
		}

		if (startMarker != null && propertyTextUnitPlaceholders != null) {
			processAllEmbedded(startMarker.toString(), locale, propertyTextUnitPlaceholders, false);
		}

		String gid = groupId.createId();
		StartGroup g = new StartGroup(findMostRecentParentId(), gid);

		GenericSkeleton skel = new GenericSkeleton((GenericSkeleton) startMarker);

		Event fe = new Event(EventType.START_GROUP, g, skel);

		if (isCurrentTextUnit()) {
			// add this group as a code of the complex TextUnit
			g.setIsReferent(true);
			Code c = new Code(TagType.PLACEHOLDER, commonTagType, TextFragment.makeRefMarker(gid));
			c.setReferenceFlag(true);
			startCode(c);
			endCode();
			referencableFilterEvents.add(fe);
		} else {
			filterEvents.add(fe);
		}

		tempFilterEventStack.push(fe);
		return g;
	}

	/**
	 * Create a {@link Ending} {@link Event} of type END_GROUP and add it to the event queue.
	 * 
	 * @param endMarker
	 *            the tags that ends the group.
	 * @return the newly created Ending
	 */
	public Ending endGroup(GenericSkeleton endMarker) {
		return endGroup(endMarker, null, null);
	}

	/**
	 * Create a {@link Ending} {@link Event} of type END_GROUP and add it to the event queue.
	 * 
	 * @param endMarker
	 *            the tags that ends the group.
	 * @param locale
	 *            the language of any actionable items
	 * @param propertyTextUnitPlaceholders
	 *            the list of actionable {@link TextUnit} or {@link Properties} with offset information into the tag.
	 * @return the newly created Ending
	 * @throws OkapiIllegalFilterOperationException if we try to perform an invalid operation.
	 */
	public Ending endGroup(GenericSkeleton endMarker, LocaleId locale,
			List<PropertyTextUnitPlaceholder> propertyTextUnitPlaceholders) {
		if (!isCurrentGroup()) {
			if (endMarker != null) {
				addDocumentPart(endMarker.toString());
			}
			LOGGER.debug("Trying to end a Group that does not exist.  Possible unbalanced Group tags.");
			return null;
		}

		GenericSkeleton skel = new GenericSkeleton();
		if (currentSkeleton != null) {
			// Consolidate any outstanding skeleton from an in-progress 
			// document part.
			skel.add(currentSkeleton);
			currentSkeleton = null;
			currentDocumentPart = null;
		}
		skel.add(endMarker);

		if (endMarker != null && propertyTextUnitPlaceholders != null) {
			processAllEmbedded(endMarker.toString(), locale, propertyTextUnitPlaceholders, false);
		}

		popTempEvent();
		Ending eg = new Ending(groupId.getLastId());
		filterEvents.add(new Event(EventType.END_GROUP, eg, skel));
		return eg;
	}

	// ////////////////////////////////////////////////////////////////////////
	// Code Methods
	// ////////////////////////////////////////////////////////////////////////

	/*
	 * Create a Code and store it for later processing.
	 */
	public void startCode(Code code) {
		codeStack.push(code);
	}

	/*
	 * End the Code and add it to the TextUnit.
	 */
	public void endCode() {
		Code c = null;
		
		try {
			c = codeStack.pop();
		} catch (EmptyStackException e) {
			throw new OkapiIllegalFilterOperationException(
			"Trying to end a Code that does not exist. Did you call startCode?", e);
		}

		ITextUnit tu = peekMostRecentTextUnit();
		if (tu == null) return;
		// We can use the first part as nothing is segment at this point
		LocaleId trgLoc = getTargetLocale();
		if (trgLoc == null) {
			tu.getSource().getFirstContent().append(c);
		} else {
			if (tu.getTarget(trgLoc) == null) {
				tu.createTarget(trgLoc, true, ITextUnit.CREATE_EMPTY);
			}
			tu.getTarget(trgLoc).getFirstContent().append(c);
		}
	}
	
	/*
	 * End the Code and add it to the TextUnit.
	 * @param tag - the final part of the original native tag 
	 */
	public void endCode(String tag) {
		Code c = null;
		
		try {
			c = codeStack.pop();
		} catch (EmptyStackException e) {
			throw new OkapiIllegalFilterOperationException(
			"Trying to end a Code that does not exist. Did you call startCode?", e);
		}

		c.appendOuterData(tag);
		ITextUnit tu = peekMostRecentTextUnit();
		if (tu == null) return;
		
		// We can use the first part as nothing is segment at this point
		tu.getSource().getFirstContent().append(c);	
	}
	
	/**
	 * append to the current {@link Code}'s outerData
	 * @param outerData the outerData to append
	 */
	public void appendCodeOuterData(String outerData) {
		Code c = peekMostRecentCode();
		if (c != null) {
			c.appendOuterData(outerData);
		}		
	}
	
	/**
	 * append to the current {@link Code}'s data
	 * @param data the data to append
	 */
	public void appendCodeData(String data) {
		Code c = peekMostRecentCode();
		if (c != null) {
			c.append(data);
		}		
	}

	// ////////////////////////////////////////////////////////////////////////
	// DocumentPart Methods
	// ////////////////////////////////////////////////////////////////////////

	/**
	 * Create a single {@link DocumentPart} and add a {@link DocumentPart} {@link Event} to the queue.
	 * 
	 * @param part
	 *            the {@link DocumentPart} (aka skeleton)
	 * @return the newly created DocumentPart
	 */
	public DocumentPart addDocumentPart(String part) {
		DocumentPart dp = startDocumentPart(part);
		endDocumentPart();
		return dp;
	}

	/**
	 * Create a {@link DocumentPart} and store it for later processing.
	 * 
	 * @param part
	 *            the {@link DocumentPart} (aka skeleton)
	 * @return the newly created DocumentPart
	 */
	public DocumentPart startDocumentPart(String part) {

		if (hasUnfinishedSkeleton()) {
			endDocumentPart();
		} else if (isCurrentTextUnit()) {
			endTextUnit();
		}

		currentSkeleton = new GenericSkeleton(part);
		currentDocumentPart = new DocumentPart(documentPartId.createId(), false);
		currentDocumentPart.setSkeleton(currentSkeleton);
		return currentDocumentPart;
	}

	/**
	 * Create a {@link DocumentPart} that references actionable (i.e., translatable, localizable) properties and store
	 * it for later processing.
	 * 
	 * @param part
	 *            the {@link DocumentPart} (aka skeleton)
	 * @param name
	 *            the name
	 * @param propertyTextUnitPlaceholders
	 *            the list of actionable {@link TextUnit} or {@link Properties} with offset information into the tag.
	 */
	public void startDocumentPart(String part, String name,
			List<PropertyTextUnitPlaceholder> propertyTextUnitPlaceholders) {
		startDocumentPart(part, name, null, propertyTextUnitPlaceholders);
	}

	/**
	 * Create a {@link DocumentPart} that references actionable (i.e., translatable, localizable) properties and store
	 * it for later processing.
	 * 
	 * @param part
	 *            the {@link DocumentPart} (aka skeleton)
	 * @param name
	 *            the name
	 * @param locale
	 *            the language of any actionable items
	 * @param propertyTextUnitPlaceholders
	 *            the list of actionable {@link TextUnit} or {@link Properties} with offset information into the tag.
	 */
	public void startDocumentPart(String part, String name, LocaleId locale,
			List<PropertyTextUnitPlaceholder> propertyTextUnitPlaceholders) {
		if (hasUnfinishedSkeleton()) {
			endDocumentPart();
		} else if (isCurrentTextUnit()) {
			endTextUnit();
		}

		currentSkeleton = new GenericSkeleton();
		currentDocumentPart = new DocumentPart(documentPartId.createId(), false);
		currentDocumentPart.setSkeleton(currentSkeleton);

		processAllEmbedded(part, locale, propertyTextUnitPlaceholders, false);
	}

	/**
	 * End the current {@link DocumentPart} and finalize the {@link Event}. Place the {@link Event} on the event queue.
	 * 
	 * @param part
	 *            the {@link DocumentPart} (aka skeleton)
	 */
	public void endDocumentPart(String part) {
		if (part != null) {
			currentSkeleton.append(part);
		}
		filterEvents.add(new Event(EventType.DOCUMENT_PART, currentDocumentPart));
		currentSkeleton = null;
		currentDocumentPart = null;
	}

	/**
	 * End the {@link DocumentPart} and finalize the {@link Event}. Place the {@link Event} on the event queue.
	 */
	public void endDocumentPart() {
		endDocumentPart(null);
	}

	/**
	 * Add to the current {@link DocumentPart}.
	 * 
	 * @param part
	 *            the {@link DocumentPart} as a String.
	 */
	public void addToDocumentPart(String part) {
		if (currentSkeleton == null) {
			startDocumentPart(part);
			return;
		}
		currentSkeleton.append(part);
	}

	/**
	 * Sets the input document mime type.
	 * 
	 * @param mimeType
	 *            the new mime type
	 */
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	
	/**
	 * Set the mime type on the current {@link TextUnit}
	 * @param mimeType - mime type
	 */
	public void setTextUnitMimeType(String mimeType) {
		ITextUnit tu = peekMostRecentTextUnit();
		if (tu != null) {
			tu.setMimeType(mimeType);
		}
	}

	/**
	 * Tell the {@link IFilter} what to do with whitespace.
	 * 
	 * @param preserveWhitespace
	 *            the preserveWhitespace as boolean.	
	 */	
	public void setPreserveWhitespace(boolean preserveWhitespace) {
		this.preserveWhitespace = preserveWhitespace;
	}
	
	/**
	 * Set preserve whitespace flag on current {@link TextUnit}
	 * 
	 * @param preserveWhitespace
	 *            the preserveWhitespace as boolean.
	 */
	public void setTextUnitPreserveWhitespace(boolean preserveWhitespace) {
		ITextUnit tu = peekMostRecentTextUnit();
		if (tu != null) {
			tu.setPreserveWhitespaces(preserveWhitespace);
		}
	}
	
	/**
	 * What is the current preserve whitespace  state?
	 * @return true if whitespace is to be preserved, false otherwise
	 */
	public boolean isPreserveWhitespace() {
		return this.preserveWhitespace;
	}

	/**
	 * Gets the custom textUnitId set by the caller.
	 * 
	 * @return the textUnitId
	 */
	public long getTextUnitId() {
		return textUnitId.getSequence();
	}
	
	/**
	 * Get the current {@link Code}. COuld be null.
	 * @return the {@link Code}
	 */
	public Code getCurrentCode() {
		return peekMostRecentCode();
	}

	/**
	 * Set the current textUnitId. Note that using this method overrides the built-in id creation algorithm. Useful for
	 * some callers that wish to create custom ids.
	 * 
	 * @param id
	 *            the initial value for the textUnitId
	 */
	public void setTextUnitId(long id) {
		this.textUnitId.setSequence(id);
	}

	/**
	 * Set the current {@link TextUnit} name.
	 * 
	 * @param name
	 *            the name (resname in XLIFF) of the {@link TextUnit}
	 * @throws NullPointerException
	 *             if there is no current {@link TextUnit}
	 */
	public void setTextUnitName(String name) {
		ITextUnit tu = peekMostRecentTextUnit();
		if (tu != null) {			
			tu.setName(name);
		}
	}

	/**
	 * Set the current {@link TextUnit} type. If there is no defined type the type is the element name.
	 * 
	 * @param type
	 *            - the TextUnit type.
	 */
	public void setTextUnitType(String type) {
		ITextUnit tu = peekMostRecentTextUnit();
		if (tu != null) {
			tu.setType(type);
		}
	}
	
	/**
	 * Set the current {@link TextUnit} translatable flag. 
	 * 
	 * @param translatable
	 *            - the TextUnit translatable flag.
	 */
	public void setTextUnitTranslatable(boolean translatable) {
		ITextUnit tu = peekMostRecentTextUnit();
		if (tu != null) {
			tu.setIsTranslatable(translatable);
		}
	}

	/**
	 * Gets the current custom {@link DocumentPart} id.
	 * 
	 * @return the id
	 */
	public long getDocumentPartId() {
		return documentPartId.getSequence();
	}

	/**
	 * Set the current custom {@link DocumentPart} id. Note that using this method overrides the built-in id creation
	 * algorithm. Useful for some callers that wish to create custom ids.
	 * 
	 * @param id
	 *            the initial value for the textUnitId
	 */
	public void setDocumentPartId(long id) {
		documentPartId.setSequence(id);
	}

	/**
	 * Get the current root id
	 * @return the rootId
	 */
	public String getRootId() {
		return rootId;
	}

	/**
	 * Do any required post-processing on the TextUnit after endTextUnit is called. Default implementation leaves
	 * TextUnit unchanged. Override this method if you need to do format specific handing such as collapsing whitespace.
	 * @param textUnit the {@link ITextUnit} to post-process.
	 * @return the post-processed {@link ITextUnit}.
	 */
	protected ITextUnit postProcessTextUnit(ITextUnit textUnit) {
		return textUnit;
	}

	/**
	 * Is the current TextUnit of the specified type?
	 * 
	 * @param type
	 *            a {@link TextUnit} type.
	 * @return true if the current {@link TextUnit} type is the same as the parameter type.
	 */
	public boolean isTextUnitWithSameType(String type) {
		Event e = peekTempEvent();
		if (e != null && e.getEventType() == EventType.TEXT_UNIT) {
			ITextUnit tu = e.getTextUnit();
			if (tu != null && tu.getType() != null && tu.getType().equals(type)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Get the {@link IdGenerator} for Groups
	 * @return the {@link IdGenerator}
	 */
	public IdGenerator getGroupId() {
		return groupId;
	}
	
	public long getGroupIdSequence() { // added DWH 4-12-2014 
		return groupId.getSequence();
	}

	
	public void setGroupIdSequence(long id) { // added DWH 4-12-2014
		this.groupId.setSequence(id);
	}

	/**
	 * Get the {@link IdGenerator} for SubDocuments
	 * @return the {@link IdGenerator}
	 */
	public IdGenerator getSubDocumentId() {
		return subDocumentId;
	}
	
	/**
	 * Set the target locale. <b>Implies we are now processing a target, not source</b>
	 * @param targetLocale - the target {@link LocaleId}
	 */
	public void setTargetLocale(LocaleId targetLocale) {
		this.targetLocale = targetLocale;
	}
	
	/**
	 * Get the current target locale. If not null implies we are processing a target
	 * @return target locale or null if we are processing the source.
	 */
	public LocaleId getTargetLocale() {
		return targetLocale;
	}
}
