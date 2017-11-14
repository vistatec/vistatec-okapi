/*===========================================================================
  Copyright (C) 2012 by the Okapi Framework contributors
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

package net.sf.okapi.filters.transtable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.sf.okapi.common.BOMNewlineEncodingDetector;
import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.IResource;
import net.sf.okapi.common.IdGenerator;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.exceptions.OkapiUnsupportedEncodingException;
import net.sf.okapi.common.filters.FilterConfiguration;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filters.IFilterConfigurationMapper;
import net.sf.okapi.common.filterwriter.GenericContent;
import net.sf.okapi.common.filterwriter.GenericFilterWriter;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.resource.ISegments;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextUnit;
import net.sf.okapi.common.skeleton.GenericSkeletonWriter;
import net.sf.okapi.common.skeleton.ISkeletonWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransTableFilter implements IFilter {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private static String MIMETYPE = "text/x-transtable";
	
	private BufferedReader reader = null;
	private boolean canceled;
	private String encoding;
	private long line;
	private IdGenerator otherId;
	private String docName;
	private LinkedList<Event> queue;
	private LocaleId trgLoc;
	private String lineBreak;
	private boolean hasUTF8BOM;
	private EncoderManager encoderManager;
	private boolean hasNext;
	private ITextUnit tu;
	private ISegments srcSegs;
	private ISegments trgSegs;
	private String currentTuId;
	private boolean needToRead;
	private String[] parts;
	private String[] ids;

	private RawDocument input;
	
	public TransTableFilter () {
		// Need for inistantiation
	}

	public void cancel () {
		canceled = true;
	}

	public void close () {
		if (input != null) {
			input.close();
		}
		
		if ( reader != null ) {
			try {
				reader.close();
			}
			catch ( IOException e ) {
				// Let it go
			}
			reader = null;
		}
	}

	public String getName () {
		return "okf_transtable";
	}
	
	public String getDisplayName () {
		return "TransTable Filter";
	}

	public String getMimeType () {
		return MIMETYPE;
	}

	public List<FilterConfiguration> getConfigurations () {
		List<FilterConfiguration> list = new ArrayList<FilterConfiguration>();
		list.add(new FilterConfiguration(getName(),
			MIMETYPE,
			getClass().getName(),
			"Translation Table Default",
			"Default TransTable configuration."));
		return list;
	}

	public EncoderManager getEncoderManager () {
		if ( encoderManager == null ) {
			encoderManager = new EncoderManager();
			encoderManager.setAllKnownMappings();
		}
		return encoderManager;
	}
	
	public IParameters getParameters () {
		return null;
	}

	public boolean hasNext () {
		if ( !queue.isEmpty() ) return true;
		return hasNext;
	}

	public Event next () {
		try {
			// Cancel if requested
			if ( canceled ) {
				queue.clear();
				queue.add(new Event(EventType.CANCELED));
			}
			
			// Process queue if it's not empty yet
			if ( queue.size() > 0 ) {
				return queue.poll();
			}
			
			// Next entry in the table
			String tmp;
			while ( true ) {
				
				// Read and parse the line if needed
				if ( needToRead ) {
					try {
						tmp = reader.readLine();
					}
					catch ( IOException e ) {
						throw new OkapiIOException("Error reading the table.", e);
					}
					
					// Check for end of file
					if ( tmp == null ) {
						hasNext = false;
						// Finish the current text unit if needed
						if ( tu != null ) {
							queue.add(new Event(EventType.TEXT_UNIT, tu));
						}
						// Last even if end-of-document
						Ending ending = new Ending(otherId.createId());
						queue.add(new Event(EventType.END_DOCUMENT, ending));
						// Return whatever is in the queue
						return queue.poll();
					}
					
					// Else: process the line
					line++;
					// Check for empty or white-spaces-only lines (allowed)
					if ( tmp.trim().length() == 0 ) {
						continue;
					}
					// Parse the line
					parts = tmp.split("\t");
					if ( parts.length < 2 ) {
						throw new OkapiIOException(String.format("Not enough fields in line %d.", line));
					}
					ids = parseCrumbs(unescape(parts[0]));
				}
				needToRead = true;

				// Check if we are still in the same text unit (several segments)
				if ( !currentTuId.equals(ids[0]) ) {
					// This is a new text unit:
					// Return the current one if needed
					if ( tu != null ) {
						needToRead = false; // Make sure we don't re-read
						Event event = new Event(EventType.TEXT_UNIT, tu);
						// Reset the text unit work variable for next time
						tu = null;
						srcSegs = null;
						trgSegs = null;
						// Return the text unit
						return event;
					}
					// Start the new text unit
					currentTuId = ids[0];
					tu = new TextUnit(currentTuId);
				}

				// Store the text of this line
				// Note that segmented text unit are represented without any content
				// between segments as that information is not stored in the table
				
				// Process the source
				tmp = unescape(parts[1]);
				TextFragment srcFrag = GenericContent.fromLetterCodedToFragment(tmp, null, false, true);
				if ( srcSegs == null ) {
					srcSegs = tu.getSourceSegments();
				}
				if ( ids[1] == null ) ids[1] = "0"; // Set default segment id if none was provided
				srcSegs.append(new Segment(ids[1], srcFrag));
					
				// Process the target if there is one
				if ( parts.length > 2 ) {
					tmp = unescape(parts[2]);
					TextFragment trgFrag = GenericContent.fromLetterCodedToFragment(tmp, null, false, true);
					// Synchronizes source and target codes as much as possible
					trgFrag.alignCodeIds(srcFrag);
					// Set the content
					if ( trgSegs == null ) {
						// Create the target container first
						tu.createTarget(trgLoc, false, IResource.CREATE_EMPTY);
						// Then get the segments interface
						trgSegs = tu.getTargetSegments(trgLoc);
					}
					trgSegs.append(new Segment(ids[1], trgFrag));
				}

				// Move to the next line
			}
		}
		catch ( Throwable e ) {
			throw new OkapiIOException(String.format("Error parsing the table in line %d.", line));
		}
	}
	
	public void open (RawDocument input) {
		open(input, true);
	}
	
	@Override
	public void setFilterConfigurationMapper (IFilterConfigurationMapper fcMapper) {
	}

	public void setParameters (IParameters params) {
		// Nothing to do
	}

	public ISkeletonWriter createSkeletonWriter() {
		return new GenericSkeletonWriter();
	}

	public IFilterWriter createFilterWriter () {
		return new GenericFilterWriter(createSkeletonWriter(), getEncoderManager());
	}

	public void open (RawDocument input,
		boolean generateSkeleton)
	{
		this.input = input;
		
		BOMNewlineEncodingDetector detector = new BOMNewlineEncodingDetector(input.getStream(), input.getEncoding());
		detector.detectAndRemoveBom();
		input.setEncoding(detector.getEncoding());
		encoding = input.getEncoding();
		
		reader = null;		
		try {
			reader = new BufferedReader(new InputStreamReader(detector.getInputStream(), encoding));
		}
		catch ( UnsupportedEncodingException e ) {
			throw new OkapiUnsupportedEncodingException(
				String.format("The encoding '%s' is not supported.", encoding), e);
		}
		trgLoc = input.getTargetLocale();
		hasUTF8BOM = detector.hasUtf8Bom();
		lineBreak = detector.getNewlineType().toString();
		if ( input.getInputURI() != null ) {
			docName = input.getInputURI().getPath();
		}
		
		otherId = new IdGenerator(null);
		line = 0;
		hasNext = true;
		tu = null;
		currentTuId = "";
		needToRead = true;
		
		// Read the first line
		String tmp;
		try {
			tmp = reader.readLine();
		}
		catch ( IOException e ) {
			throw new OkapiIOException("Error reading header.", e);
		}
		if ( Util.isEmpty(tmp) ) {
			throw new OkapiIOException("Empty header line.");
		}
		line++;
		// First entry is just information
		String[] parts = tmp.split("\t");
		// Check signature
		if ( parts.length != 3 ) {
			throw new OkapiIOException("Unexpected header.");
		}
		parts[0] = unescape(parts[0]);
		parts[1] = unescape(parts[1]);
		parts[2] = unescape(parts[2]);
		if ( !parts[0].startsWith(TransTableWriter.SIGNATURE) ) {
			throw new OkapiIOException("Invalid signature. This may not be a TransTable file.");
		}
		// Source locale
		LocaleId srcLoc = LocaleId.fromString(parts[1]);
		if ( !srcLoc.equals(input.getSourceLocale()) ) {
			logger.warn("The source locale declared in the file ('{}') is not the expected one ('{}')\n{} will be used.",
				srcLoc.toString(), input.getSourceLocale().toString(), input.getSourceLocale().toString());
		}
		// Target locale
		trgLoc = LocaleId.fromString(parts[2]);
		if ( input.getTargetLocale() != null ) {
			if ( !trgLoc.equals(input.getTargetLocale()) ) {
				logger.warn("The target locale declared in the file ('{}') is not the expected one ('{}')\n{} will be used.",
					trgLoc.toString(), input.getTargetLocale().toString(), input.getTargetLocale().toString());
				trgLoc = input.getTargetLocale();
			}
		}
		
		// Set the start event
		queue = new LinkedList<Event>();
		StartDocument startDoc = new StartDocument(otherId.createId());
		startDoc.setName(docName);
		startDoc.setEncoding(encoding, hasUTF8BOM);
		startDoc.setLocale(input.getSourceLocale());
		startDoc.setLineBreak(lineBreak);
		startDoc.setFilterParameters(getParameters());
		startDoc.setFilterWriter(createFilterWriter());
		startDoc.setType(MIMETYPE);
		startDoc.setMimeType(MIMETYPE);
		startDoc.setMultilingual(true);
		queue.add(new Event(EventType.START_DOCUMENT, startDoc));
	}
	
	private String unescape (String text) {
		String tmp = text;
		if ( tmp.startsWith("\"") ) {
			tmp = tmp.substring(1);
		}
		if ( tmp.endsWith("\"") ) {
			tmp = tmp.substring(0, tmp.length()-1);
		}
		tmp = tmp.replace("\\t", "\t");
		tmp = tmp.replace("\\n", "\n");
		return tmp;
	}

	private String[] parseCrumbs (String text) {
		// Check if it is a crumbs-string or not
		if ( !text.startsWith(TransTableWriter.CRUMBS_PREFIX) ) {
			// Not an expected pattern
			throw new OkapiIOException(String.format("Error in ID pattern ('%s'", text));
		}

		// Get the text unit id and segment parts
		int n = text.indexOf(TransTableWriter.TEXTUNIT_CRUMB);
		if ( n == -1 ) {
			throw new OkapiIOException(String.format("Error in ID pattern ('%s'", text));
		}
		String tmp = text.substring(n+TransTableWriter.TEXTUNIT_CRUMB.length()).trim();
		if ( tmp.isEmpty() ) {
			// Something is not right
			throw new OkapiIOException(String.format("Error in ID pattern ('%s'", text));
		}
		
		String[] res = new String[2];
		// Check for segments
		n = tmp.indexOf(':');
		if ( n == -1 ) {
			res[0] = tmp;
		}
		else { // Has segment id
			res[0] = tmp.substring(0, n);
			res[1] = tmp.substring(n+1+TransTableWriter.SEGMENT_CRUMB.length()); // ":s=<id>"
		}
		return res;
	}
	
}
