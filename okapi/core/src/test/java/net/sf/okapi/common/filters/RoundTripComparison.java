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

package net.sf.okapi.common.filters;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.FileLocation;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.pipeline.IPipelineStep;
import net.sf.okapi.common.resource.RawDocument;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoundTripComparison {
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	private IFilter filter;
	private ArrayList<Event> extraction1Events;
	private ArrayList<Event> extraction2Events;
	private ArrayList<Event> subDocEvents;
	private Event subDocEvent;
	private ByteArrayOutputStream writerBuffer;
	private String defaultEncoding;
	private LocaleId srcLoc;
	private LocaleId trgLoc;
	private boolean includeSkeleton;

	public RoundTripComparison (boolean includeSkeleton) {
		extraction1Events = new ArrayList<Event>();
		extraction2Events = new ArrayList<Event>();
		subDocEvents = new ArrayList<Event>();
		this.includeSkeleton = includeSkeleton;
	}

	public RoundTripComparison () {
		this(true);
	}

	public boolean executeCompare (IFilter filter, List<InputDocument> inputDocs,
			String defaultEncoding, LocaleId srcLoc, LocaleId trgLoc) {
		return executeCompare(filter, inputDocs,
				defaultEncoding, srcLoc, trgLoc, true);
	}

	public boolean executeCompare (IFilter filter, List<InputDocument> inputDocs,
			String defaultEncoding, LocaleId srcLoc, LocaleId trgLoc, boolean failFirst) {
		this.filter = filter;
		this.defaultEncoding = defaultEncoding;
		this.srcLoc = srcLoc;
		this.trgLoc = trgLoc;
		boolean res = true;

		for (InputDocument doc : inputDocs) {
			LOGGER.trace("Processing Document: " + doc.path);
			
			// Reset the event lists
			extraction1Events.clear();
			extraction2Events.clear();
			subDocEvents.clear();
			// Load parameters if needed
			if (doc.paramFile != null && !doc.paramFile.equals("")) {
				String root = Util.getDirectoryName(doc.path);
				IParameters params = filter.getParameters();
				if (params != null) {
					params.load(Util.toURL(root + File.separator + doc.paramFile), false);
				}
			}
			// Execute the first extraction and the re-writing
			executeFirstExtraction(doc);
			// Execute the second extraction from the output of the first
			LOGGER.trace("Processing Second Document: " + doc.path);
			executeSecondExtraction();
			// Compare the events
			if (failFirst) {
				if ( !FilterTestDriver.compareEvents(extraction1Events, extraction2Events, includeSkeleton) ) {
					throw new OkapiException("Events are different for " + doc.path);
				}
			}	
			else {
				try {
					if ( !FilterTestDriver.compareEvents(extraction1Events, extraction2Events, includeSkeleton) ) {
						throw new OkapiException("Events are different for " + doc.path);
					}
				} catch (Exception e) {
					LOGGER.debug("Events are different for {}", doc.path);
					res = false;
					continue;
				}
			}
		}
		return res;
	}
	
	public boolean executeCompare (IFilter filter, List<InputDocument> inputDocs,
			String defaultEncoding, LocaleId srcLoc, LocaleId trgLoc, String dirSuffix) {
		return executeCompare(filter, inputDocs,
				defaultEncoding, srcLoc, trgLoc, dirSuffix, true);
	}

	public boolean executeCompare (IFilter filter,
		List<InputDocument> inputDocs,
		String defaultEncoding,
		LocaleId srcLoc,
		LocaleId trgLoc,
		String dirSuffix, boolean failFirst)
	{
		if (Util.isEmpty(dirSuffix)) throw new InvalidParameterException("dirSuffix cannot be empty - an attempt to override the source file will be rejected and source file will be compared with itself returning always true");
		//return executeCompare(filter, inputDocs, defaultEncoding, srcLoc, trgLoc, dirSuffix, (IPipelineStep[]) null);
		this.filter = filter;
		this.defaultEncoding = defaultEncoding;
		this.srcLoc = srcLoc;
		this.trgLoc = trgLoc;
		boolean res = true;

		for (InputDocument doc : inputDocs) {
			LOGGER.trace("Processing Document: " + doc.path);
			// Reset the event lists
			extraction1Events.clear();
			extraction2Events.clear();
			subDocEvents.clear();
			// Load parameters if needed, !!! no reset is called for parameters
			if (doc.paramFile != null && !doc.paramFile.equals("")) {
				String root = Util.getDirectoryName(doc.path);
				IParameters params = filter.getParameters();
				if (params != null) {
					params.load(Util.toURL(root + File.separator + doc.paramFile), false);
				}
			}
			// Execute the first extraction and the re-writing
			String outPath = executeFirstExtractionToFile(doc, dirSuffix, (IPipelineStep[]) null);
			// Execute the second extraction from the output of the first
			executeSecondExtractionFromFile(outPath);
			// Compare the events
//			if (!FilterTestDriver.compareEvents(extraction1Events, extraction2Events, subDocEvents, includeSkeleton)) {
//				throw new OkapiException("Events are different for " + doc.path);
//			}
			if (failFirst) {
				if ( !FilterTestDriver.compareEvents(extraction1Events, extraction2Events, includeSkeleton) ) {
					throw new OkapiException("Events are different for " + doc.path);
				}
			}	
			else {
				try {
					if ( !FilterTestDriver.compareEvents(extraction1Events, extraction2Events, includeSkeleton) ) {
						throw new OkapiException("Events are different for " + doc.path);
					}
				} catch (Exception e) {
					LOGGER.debug("Events are different for {}", doc.path);
					res = false;
					continue;
				}
			}
		}
		return res;
	}

	public boolean executeCompare(IFilter filter, List<InputDocument> inputDocs,
			String defaultEncoding, LocaleId srcLoc, LocaleId trgLoc, String dirSuffux, IPipelineStep... steps) {
		return executeCompare(filter, inputDocs,
				defaultEncoding, srcLoc, trgLoc, dirSuffux, false, steps);
	}
	
	public boolean executeCompare(IFilter filter, List<InputDocument> inputDocs,
			String defaultEncoding, LocaleId srcLoc, LocaleId trgLoc, String dirSuffux, boolean failFirst, IPipelineStep... steps) {
		
		this.filter = filter;
		this.defaultEncoding = defaultEncoding;
		this.srcLoc = srcLoc;
		this.trgLoc = trgLoc;
		boolean res = true;
		
//		Pipeline pipeline = new Pipeline();
//		for (IPipelineStep step : steps) {
//			pipeline.addStep(step);
//		}
		
		for (InputDocument doc : inputDocs) {
			LOGGER.trace("Processing Document: " + doc.path);
			// Reset the event lists
			extraction1Events.clear();
			extraction2Events.clear();
			subDocEvents.clear();
			// Load parameters if needed
			if (doc.paramFile == null) {
				IParameters params = filter.getParameters();
				if (params != null)
					params.reset();
			} else {
				String root = Util.getDirectoryName(doc.path);
				IParameters params = filter.getParameters();
				if (params != null)
					params.load(Util.toURL(root + File.separator + doc.paramFile), false);
			}
			// Execute the first extraction and the re-writing
			String outPath = executeFirstExtractionToFile(doc, dirSuffux, steps);
			// Execute the second extraction from the output of the first
			executeSecondExtractionFromFile(outPath, steps);
			// Compare the events
//			if ( !FilterTestDriver.compareEvents(extraction1Events, extraction2Events, includeSkeleton) ) {
//				throw new OkapiException("Events are different for " + doc.path);
//			}
			if (failFirst) {
				if ( !FilterTestDriver.compareEvents(extraction1Events, extraction2Events, includeSkeleton) ) {
					throw new OkapiException("Events are different for " + doc.path);
				}
			}	
			else {
				try {
					if ( !FilterTestDriver.compareEvents(extraction1Events, extraction2Events, includeSkeleton) ) {
						throw new OkapiException("Events are different for " + doc.path);
					}
				} catch (Exception e) {
					LOGGER.debug("Events are different for {}", doc.path);
					res = false;
					continue;
				}
			}
		}
		return res;
	}
	
	private void executeFirstExtraction(InputDocument doc) {
		try (IFilterWriter writer = filter.createFilterWriter()) {
			// Open the input
			filter.open(new RawDocument(Util.toURI(doc.path), defaultEncoding, srcLoc,
					trgLoc));

			// Prepare the output
			writer.setOptions(trgLoc, "UTF-16");
			writerBuffer = new ByteArrayOutputStream();
			writer.setOutput(writerBuffer);

			// Process the document
			Event event;
			while (filter.hasNext()) {
				event = filter.next();
				switch (event.getEventType()) {
				case START_DOCUMENT:
				case END_DOCUMENT:				
				case END_SUBDOCUMENT:
					break;
				case START_SUBDOCUMENT:
					subDocEvent = event;
					break;
				case START_GROUP:
				case END_GROUP:
				case START_SUBFILTER:
				case END_SUBFILTER:
				case TEXT_UNIT:
					extraction1Events.add(event);
					subDocEvents.add(subDocEvent);
					break;
				default:
					break;
				}
				writer.handleEvent(event);
		}
		} finally {
			if (filter != null)
				filter.close();
		}
	}

	private void executeSecondExtraction() {
		try {
			// Set the input (from the output of first extraction)
			String input;
			try {
				input = new String(writerBuffer.toByteArray(), "UTF-16");
			} catch (UnsupportedEncodingException e) {
				throw new OkapiException(e);
			}
			// DEBUG System.out.println(input);
			filter.open(new RawDocument(input, srcLoc, trgLoc));

			// Process the document
			Event event;
			while (filter.hasNext()) {
				event = filter.next();
				switch (event.getEventType()) {
				case START_DOCUMENT:
				case END_DOCUMENT:
				case START_SUBDOCUMENT:
				case END_SUBDOCUMENT:
					break;
				case START_GROUP:
				case END_GROUP:
				case START_SUBFILTER:
				case END_SUBFILTER:
				case TEXT_UNIT:
					extraction2Events.add(event);
					break;
				default:
					break;
				}
			}
		} finally {
			if (filter != null)
				filter.close();
		}
	}

	public String executeFirstExtractionToFile(InputDocument doc, String outputDir, IPipelineStep... steps) {
		String outPath = null;
		try (IFilterWriter writer = filter.createFilterWriter()) {
			// Open the input
			filter.open(new RawDocument(Util.toURI(doc.path), defaultEncoding, srcLoc,
					trgLoc));

			// Prepare the output
			writer.setOptions(trgLoc, "UTF-8");

			FileLocation location = FileLocation.fromClass(this.getClass());
			outPath = location.out("/" + outputDir).toString()
				+ (File.separator + Util.getFilename(doc.path, true));

			Util.createDirectories(outPath);
			writer.setOutput(Util.fixPath(outPath));
			
			// Process the document
			Event event;
			while (filter.hasNext()) {
				event = filter.next();
				switch (event.getEventType()) {
				case START_DOCUMENT:
				case END_DOCUMENT:
				case END_SUBDOCUMENT:
					break;
				case START_SUBDOCUMENT:
					subDocEvent = event;
					break;
				case START_GROUP:
				case END_GROUP:
				case START_SUBFILTER:
				case END_SUBFILTER:
				case TEXT_UNIT:
					if (event.isTextUnit()) {
						// Steps can modify the event, but we need to compare events as were from the filter, so we are cloning 
						extraction1Events.add(new Event(EventType.TEXT_UNIT, event.getTextUnit().clone()));
					}
					else {
						extraction1Events.add(event);
					}
					
					subDocEvents.add(subDocEvent);
					break;
				default:
					break;
				}
				if (steps != null) {
					for (IPipelineStep step : steps) {
						event = step.handleEvent(event);						
					}
				}					
				writer.handleEvent(event);
			}
		} finally {
			if (filter != null)
				filter.close();
		}
		return outPath;
	}

	private void executeSecondExtractionFromFile(String input, IPipelineStep... steps) {
		try {
			// Set the input (from the output of first extraction)
			filter.open(new RawDocument(Util.toURI(input), "UTF-8", srcLoc, trgLoc));

			// Process the document
			Event event;
			while (filter.hasNext()) {
				event = filter.next();
				switch (event.getEventType()) {
				case START_DOCUMENT:
				case END_DOCUMENT:
				case START_SUBDOCUMENT:
				case END_SUBDOCUMENT:
					break;
				case START_GROUP:
				case END_GROUP:
				case START_SUBFILTER:
				case END_SUBFILTER:
				case TEXT_UNIT:
					extraction2Events.add(event);
					break;
				default:
					break;
				}
			}
		} finally {
			if (filter != null)
				filter.close();
		}
	}
	
	private void executeSecondExtractionFromFile(String input) {
		try {
			// Set the input (from the output of first extraction)
			filter.open(new RawDocument(Util.toURI(input), "UTF-8", srcLoc, trgLoc));

			// Process the document
			Event event;
			while (filter.hasNext()) {
				event = filter.next();
				switch (event.getEventType()) {
				case START_DOCUMENT:
				case END_DOCUMENT:
				case START_SUBDOCUMENT:
				case END_SUBDOCUMENT:
					break;
				case START_GROUP:
				case END_GROUP:
				case START_SUBFILTER:
				case END_SUBFILTER:
				case TEXT_UNIT:
					extraction2Events.add(event);
					break;
				default:
					break;
				}
			}
		} finally {
			if (filter != null)
				filter.close();
		}
	}
}
