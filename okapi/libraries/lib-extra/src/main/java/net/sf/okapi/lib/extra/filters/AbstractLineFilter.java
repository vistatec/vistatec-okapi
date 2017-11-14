/*===========================================================================
  Copyright (C) 2008-2009 by the Okapi Framework contributors
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

package net.sf.okapi.lib.extra.filters;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;

import net.sf.okapi.common.BOMNewlineEncodingDetector;
import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.IResource;
import net.sf.okapi.common.ISimplifierRulesParameters;
import net.sf.okapi.common.ISkeleton;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.exceptions.OkapiBadFilterInputException;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.filters.FilterUtil;
import net.sf.okapi.common.filters.IFilterConfigurationMapper;
import net.sf.okapi.common.filterwriter.GenericFilterWriter;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.DocumentPart;
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.skeleton.GenericSkeleton;
import net.sf.okapi.common.skeleton.GenericSkeletonWriter;
import net.sf.okapi.common.skeleton.ISkeletonWriter;

/**
 * Base class for the filters which input is processed line-upon-line. Provides low-level skeleton, events, 
 * and configuration handling mechanisms.  
 * 
 * @version 0.1, 09.06.2009
 */

public abstract class AbstractLineFilter extends AbstractBaseFilter {

	public static final String LINE_NUMBER = "line_number";	
	
	private BufferedReader reader;
	private boolean canceled;
	private String encoding;
	
	private LinkedList<Event> queue;
	private String lineRead;
	private long lineNumber = 0;
	private TextProcessingResult lastResult;
	private int tuId;
	private int otherId;
	private String lineBreak;
	private int parseState = 0;	
	private String docName;
	protected LocaleId srcLang;
	protected LocaleId trgLang;
	private boolean hasUTF8BOM;
	private IFilterWriter filterWriter;
	private boolean multilingual;
	protected StartDocument startDoc;
	protected RawDocument input;
	
	public void cancel() {
		
		canceled = true;
	}

	public final void close() {
		if (input != null) {
			input.close();
		}
		
		try {
			if (reader != null) {
				reader.close();
				reader = null;
				docName = null;
			}
			parseState = 0;
		}
		catch (IOException e) {
			throw new OkapiIOException(e);
		}
	}

	@Override
	public void setFilterConfigurationMapper (IFilterConfigurationMapper fcMapper) {
		//TODO: Implement if derived filters need sub-filters
	}

	public IFilterWriter createFilterWriter() {
		
		return new GenericFilterWriter(createSkeletonWriter(), getEncoderManager());
	}

	public ISkeletonWriter createSkeletonWriter() {
		
		return new GenericSkeletonWriter();
	}

	public IFilterWriter getFilterWriter() {
		
		return filterWriter;
	}
		
	public void setFilterWriter(IFilterWriter filterWriter) {
		
		this.filterWriter = filterWriter;
	}
	
	protected void setMultilingual(boolean multilingual) {
		
		this.multilingual = multilingual;
	}

	public boolean isMultilingual() {
		
		return multilingual;
	}
	
	@Override
	protected void component_init() {
				
		setFilterWriter(createFilterWriter());
	}
	
	/**
	 * Called by the filter for every line read from the input
	 * @param lineContainer
	 * @return
	 */
	protected TextProcessingResult component_exec(TextContainer lineContainer) {
		// To be overridden in descendant classes
		
		return TextProcessingResult.REJECTED;		
	}

	/**
	 * Called by the filter when there are no input lines (the input has been read).
	 * Used to control implementation-specific internal buffers.
	 * 
	 * @param lastChance True if there are no events in the queue, and if the method will not produce events, the filter will be finished.
	 */
	protected void component_idle(boolean lastChance) {
		// To be implemented in descendant classes
	}
	
	public boolean notify(String notification, Object info) {
		// Can be overridden in descendant classes, but will the call of superclass filter_notify()
		
		if (FilterNotification.FILTER_LINE_BEFORE_PROCESSING.equals(notification)) {
			
			if (lastResult == TextProcessingResult.DELAYED_DECISION) 
				addLineBreak();
			
			return true; // Processed 
		}
		
		return false;				
	}
	
	public boolean hasNext() {
		
		return (parseState > 0);
	}

	public void open (RawDocument input) {
		
		open(input, true);
	}
	
	public void open (RawDocument input,
		boolean generateSkeleton)
	{
		if (input == null) throw new OkapiBadFilterInputException("RawDocument is not defined in open(RawDocument, boolean).");
					
		this.input = input;
		
		BOMNewlineEncodingDetector detector = new BOMNewlineEncodingDetector(input.getStream(), input.getEncoding());
		//detector.detectBom();
		detector.detectAndRemoveBom();
		input.setEncoding(detector.getEncoding());
		encoding = detector.getEncoding();
		srcLang = input.getSourceLocale();
		trgLang = input.getTargetLocale();
		hasUTF8BOM = detector.hasUtf8Bom();
		lineBreak = detector.getNewlineType().toString();
		if ( input.getInputURI() != null ) {
			docName = input.getInputURI().getPath();
		}
		
		commonOpen(input.getReader());
	}
		
	private void commonOpen (Reader inputReader) {
		
		parseState = 1;
		canceled = false;

		// Open the input reader from the provided reader
		
		if (inputReader instanceof BufferedReader ) {
			reader = (BufferedReader) inputReader;
		}
		else {
			reader = new BufferedReader(inputReader);
		}
				
		try {
			reader.mark(1024); // Mark line start (needed for one-line files, reader.ready() returns false before the first read)
		} 
		catch (IOException e) {
		} 
		
		// Initialize variables
		tuId = 0;
		otherId = 0;
		lineNumber = 0;
		
		//lineToProcess = "";		
		lastResult = TextProcessingResult.NONE;
		
		queue = new LinkedList<Event>();
		
		component_init(); // Initialize the filter with implementation-specific parameters (protected method)
				
		startDoc = new StartDocument(String.valueOf(++otherId));
		startDoc.setName(docName);
		startDoc.setEncoding(encoding, hasUTF8BOM);
		startDoc.setLocale(srcLang);
		startDoc.setFilterParameters(getParameters());	// this.getClass()	this.getParametersClassName()
		startDoc.setFilterWriter(getFilterWriter());		
		startDoc.setLineBreak(lineBreak);
		startDoc.setType(getMimeType());
		startDoc.setMimeType(getMimeType());
		startDoc.setSkeleton(new GenericSkeleton());
		startDoc.setMultilingual(isMultilingual());
		
		sendEvent(EventType.START_DOCUMENT, startDoc);	
		
		// load simplifier rules and send as an event
		if (!Util.isEmpty(((ISimplifierRulesParameters)getParameters()).getSimplifierRules())) {			
			Event cs = FilterUtil.createCodeSimplifierEvent(((ISimplifierRulesParameters)getParameters()).getSimplifierRules());
			queue.add(cs);
		}	
	}

	private Event getQueueEvent() {
		
		Event event = queue.poll();

		return event;		
	}
	
	protected ITextUnit getFirstTextUnit() {
		
		for (Event event : queue) {
			
			if (event.getEventType() == EventType.TEXT_UNIT)
				return event.getTextUnit();
		}
		
		return null;
	}
	
	protected DocumentPart getFirstDocumentPart() {
		
		for (Event event : queue) {
			
			if (event.getEventType() == EventType.DOCUMENT_PART)
				return (DocumentPart) event.getResource();
		}
		
		return null;
	}
	
	protected LocaleId getTargetLocale() {
	
		return trgLang;
	}
	
	public Event next() {
				
		// Cancel if requested
		if (canceled) {
			parseState = 0;
			queue.clear();
			
			sendEvent(EventType.CANCELED, null);
			return getQueueEvent(); 
		}
		
		// At least one event should always remain in the queue to provide a skeleton
		if (queue.size() > 1) { // 1Y
			return getQueueEvent();				
		}
		else { // 1N
			try {
				
				while (true) {
					if (getNextLine()) { // Sets lineRead // 2Y
						// 3 
						// Process read line
					
						switch (lastResult) { // Result of previous processing
						case REJECTED:
						case ACCEPTED:						
							// Add line break to skeleton
							addLineBreak();					
							break;
						case DELAYED_DECISION:
							break;
						case NONE:
							break;
						default:
							break;
						}
						
						TextContainer lineContainer = new TextContainer(lineRead);
							
						lineContainer.setProperty(new Property(LINE_NUMBER, String.valueOf(lineNumber), true)); // Prop of source, not TU
						lastResult = component_exec(lineContainer); // Can modify text and codes of lineContainer  
						
						switch (lastResult) { 
						
							case REJECTED:
								// Add the whole line to the skeleton
								GenericSkeleton skel = getActiveSkeleton();
								if (skel == null) break;
								skel.append(lineContainer.toString());						
								break;
								
							case ACCEPTED:						
								break;
								
							case DELAYED_DECISION:
								break;
								
                            case NONE:
                                break;
                            default:
                                break;
						}						
						
						if (queue.size() > 1) { // 4Y
							return getQueueEvent();
						}				
						else { // 4N
							continue; 
						}
					} 
					else { // 2N
						break;
					}	
				}		
						
						if (queue.size() > 1) { // 10Y							
							return getQueueEvent();
						}
						else { // 10N							
							// 5
							component_idle(queue.size() == 0);
							
							// 6
							if (queue.size() > 0) { // 6Y							
								return getQueueEvent();
							}
							else { // 6N
								component_done(); // 7
																
									// The ending event
									Ending ending = new Ending(String.valueOf(++otherId));
									GenericSkeleton skel = new GenericSkeleton();
									ending.setSkeleton(skel);
									
									String tail = getFileTail();					
									if (!Util.isEmpty(tail)) 
										if (skel != null) skel.append(tail);
									
									parseState = 0;
									return new Event(EventType.END_DOCUMENT, ending);
							}
						}
					}
				
			catch ( IOException e ) {
				throw new OkapiIOException(e);
			}
		} 
		
	}
	
	protected final boolean sendEvent(EventType eventType, IResource res) {
		
		return sendEvent(-1, eventType, res);
	}
	
	protected final GenericSkeleton getActiveSkeleton() {
		// Return a skeleton of the last event in the queue
		
		if (queue == null) return null;
		if (Util.isEmpty(queue)) return null;
		
		Event event = queue.getLast();
		if (event == null) return null;
		
		// skip over PipelineParamets event - it has no skeleton
		// and is for information purposes only
		if (event.getEventType() == EventType.PIPELINE_PARAMETERS) {
		    return null;
		}

		IResource res = event.getResource();
		if (res == null) return null;
		
		ISkeleton skel = res.getSkeleton();
		
		if (skel == null) {
			
		// Force skeleton
			skel = new GenericSkeleton();
			res.setSkeleton(skel);
		}
			
		if (!(skel instanceof GenericSkeleton)) return null; 
		
		return (GenericSkeleton) skel;
	}
	
	protected final GenericSkeleton getHeadSkeleton() {
		// Return a skeleton of the first event in the queue
		
		if (queue == null) return null;
		if (Util.isEmpty(queue)) return null;
		
		Event event = queue.getFirst();
		if (event == null) return null;

		IResource res = event.getResource();
		if (res == null) return null;
		
		ISkeleton skel = res.getSkeleton();
		
		if (skel == null) {
			
		// Force skeleton
			skel = new GenericSkeleton();
			res.setSkeleton(skel);
		}
			
		if (!(skel instanceof GenericSkeleton)) return null; 
		
		return (GenericSkeleton) skel;
	}
	
	protected final int getQueueSize() {
		
		return queue.size();
	}

	/**
	 * 
	 * @param index
	 * @param eventType
	 * @param res
	 */
	protected final boolean sendEvent(int index, EventType eventType, IResource res) {
		// Called from descendant classes if they want to put events in the queue themselves, not to be overridden
		
		Event event = new Event(eventType, res);
		int saveSize = queue.size();
		boolean result = false;
		
		if (res != null && Util.isEmpty(res.getId())) {
			if (event.getEventType() == EventType.TEXT_UNIT)			
				res.setId(String.valueOf(++tuId));
			else
				res.setId(String.valueOf(++otherId));
		}
		
		if (index == -1) {
			queue.add(event);
			result = (queue.size() > saveSize); // Is event added?
			
			return result;
		}		
		else {
			queue.add(index, event);
			result = (queue.size() > saveSize); // Is event added?
			
			return result;
		}
	}
		
	/**
	 * Gets the next line of the string or file input.
	 * @return True if there was a line to read,
	 * false if this is the end of the input.
	 */
	private boolean getNextLine() throws IOException {
		
		if (reader.markSupported())
			if (reader.ready())
				reader.mark(1024); // Mark line start
		
		lineRead = reader.readLine();
		if ( lineRead != null ) {
			lineNumber++;
		}
		return (lineRead != null);
	}
	
	private String getFileTail() throws IOException {
		
		if (!reader.markSupported()) return "";
		
		String st = null;
		try {
			reader.reset(); // Seek the last marked position
			st = reader.readLine();
		} catch (Exception e) {
			return "";
		}
				
		if (st == null) return "";
		
		reader.reset(); // Seek the last marked position
		
		char[] buf = new char[st.length() + 100];
		int count = reader.read(buf);
		
		if (count != -1)
			return (new String(buf)).substring(st.length(), count);
		else
			return "";
	}

	protected final String getLineBreak() {
		
		return lineBreak;
	}

	protected final void addLineBreak() {

		GenericSkeleton skel = getActiveSkeleton();
		
		if (skel != null /*&& lineNumber > 1*/) 
			skel.append(lineBreak);
	}
}
