/*===========================================================================
  Copyright (C) 2010-2011 by the Okapi Framework contributors
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

package net.sf.okapi.filters.mosestext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.okapi.common.BOMNewlineEncodingDetector;
import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.IdGenerator;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.exceptions.OkapiUnsupportedEncodingException;
import net.sf.okapi.common.filters.FilterConfiguration;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filters.IFilterConfigurationMapper;
import net.sf.okapi.common.filterwriter.GenericFilterWriter;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.common.resource.TextUnit;
import net.sf.okapi.common.skeleton.GenericSkeleton;
import net.sf.okapi.common.skeleton.GenericSkeletonWriter;
import net.sf.okapi.common.skeleton.ISkeletonWriter;

/**
 * Implements the IFilter interface for Moses Text files.
 * One line per segment, normally in UTF-8. No text unit separator.
 */
@UsingParameters() // No parameters are used
public class MosesTextFilter implements IFilter {

	public static final String MOSESTEXT_MIME_TYPE = "text/x-mosestext";
	
	private static final String ENDSEGMENT = "</mrk>"; 

	private static final Pattern STARTSEGMENT = Pattern.compile("<mrk\\s+mtype\\s*=\\s*?[\"']seg[\"'].*?>");
	private static final Pattern OPENCLOSE = Pattern.compile("(\\<g(\\s+)id=['\"](.*?)['\"]>)|(\\</g\\>)");
	private static final Pattern ISOLATED = Pattern.compile("\\<(bx|ex|x)(\\s+)id=['\"](.*?)['\"](\\s*?)/>");
	private static final Pattern LINEBREAK = Pattern.compile("(\\<lb\\s*?/>)");

	private BufferedReader reader;
	private String lineBreak;
	private Event event;
	private IdGenerator tuIdGen;
	private EncoderManager encoderManager;
	private GenericSkeleton skel;
	private RawDocument input;
	
	public MosesTextFilter () {
	}
	
	public void cancel () {
	}

	public void close () {
		if (input != null) {
			input.close();
		}
		try {
			if ( reader != null ) {
				reader.close();
				reader = null;
			}
		}
		catch ( IOException e) {
			throw new OkapiIOException(e);
		}
	}

	public ISkeletonWriter createSkeletonWriter() {
		return new GenericSkeletonWriter();
	}

	public IFilterWriter createFilterWriter () {
		return new GenericFilterWriter(createSkeletonWriter(), getEncoderManager());
	}

	public List<FilterConfiguration> getConfigurations () {
		List<FilterConfiguration> list = new ArrayList<FilterConfiguration>();
		list.add(new FilterConfiguration(getName(),
			MOSESTEXT_MIME_TYPE,
			getClass().getName(),
			"Moses Text Default",
			"Default Moses Text configuration.",
			null,
			".txt;"));
		return list;
	}

	public EncoderManager getEncoderManager () {
		if ( encoderManager == null ) {
			encoderManager = new EncoderManager();
			encoderManager.setMapping(MOSESTEXT_MIME_TYPE, "net.sf.okapi.filters.mosestext.MosesTextEncoder");
		}
		return encoderManager;
	}
	
	public String getDisplayName () {
		return "Moses Text Filter";
	}

	public String getMimeType () {
		return MOSESTEXT_MIME_TYPE;
	}

	public String getName () {
		return "okf_mosestext";
	}

	public IParameters getParameters () {
		return null; // Not used
	}

	public boolean hasNext () {
		return (event != null);
	}

	public Event next () {
		// The current event is ready, now get the next one
		Event eventToSend = event;
		event = null; // Next one is reset to none

		// Stop the process after the end of document
		if ( eventToSend.getEventType() == EventType.END_DOCUMENT ) {
			return eventToSend;
		}
		
		// Else: compute the next event
		try {
			skel = new GenericSkeleton();
			StringBuilder sb = new StringBuilder();
			boolean inSeg = false;
			while ( true ) {
				String line = reader.readLine();
				if ( line == null ) {
					if ( inSeg ) {
						throw new OkapiIOException("End of segment expected before the end of the document.");
					}
					// Else: normal end of document
					event = new Event(EventType.END_DOCUMENT, new Ending("ed"));
				}
				else {
					// Detect start of segment
					Matcher m = STARTSEGMENT.matcher(line);
					if ( m.lookingAt() ) {
						if ( inSeg ) {
							throw new OkapiIOException("End of segment expected before a new segment.");
						}
						line = line.substring(m.group().length());
						inSeg = true;
						skel.append(m.group());
					}
					else if ( !inSeg ) {
						// Not starting with a segment marker: If not in segment already we assume the line is the segment.
						sb.append(line);
						event = processBuffer(sb);
						return eventToSend;
					}
					// Look for the ending of the segment
					if ( line.endsWith(ENDSEGMENT) ) {
						line = line.substring(0, line.length()-ENDSEGMENT.length());
						sb.append(line);
						event = processBuffer(sb);
					}
					else { // Not the end of the segment yet
						sb.append(line+"\n");
						continue; // Continue onto the next line
					}
				}
				
				// We are done
				return eventToSend;
			}
		}
		catch ( IOException e ) {
			throw new OkapiIOException(e);
		}
	}

	public void open (RawDocument input) {
		open(input, true);
	}

	public void open (RawDocument input,
		boolean generateSkeleton)
	{	
		this.input = input;
		
		BOMNewlineEncodingDetector detector = new BOMNewlineEncodingDetector(input.getStream(), "UTF-8");
		detector.detectAndRemoveBom();
		input.setEncoding(detector.getEncoding());
		String encoding = input.getEncoding();
		
		try {
			reader = new BufferedReader(new InputStreamReader(detector.getInputStream(), encoding));
		}
		catch ( UnsupportedEncodingException e ) {
			throw new OkapiUnsupportedEncodingException(
				String.format("The encoding '%s' is not supported.", encoding), e);
		}
		lineBreak = detector.getNewlineType().toString();
		boolean hasUTF8BOM = detector.hasUtf8Bom();
		String docName = null;
		if ( input.getInputURI() != null ) {
			docName = input.getInputURI().getPath();
		}
		
		tuIdGen = new IdGenerator(null);
		
		// Set the start event
		StartDocument startDoc = new StartDocument("sd");
		startDoc.setName(docName);
		startDoc.setEncoding(encoding, hasUTF8BOM);
		startDoc.setLocale(input.getSourceLocale());
		startDoc.setLineBreak(lineBreak);
		startDoc.setFilterParameters(getParameters());
		startDoc.setFilterWriter(createFilterWriter());
		startDoc.setType(MOSESTEXT_MIME_TYPE);
		startDoc.setMimeType(MOSESTEXT_MIME_TYPE);
		startDoc.setMultilingual(false);
		event = new Event(EventType.START_DOCUMENT, startDoc);
	}

	@Override
	public void setFilterConfigurationMapper (IFilterConfigurationMapper fcMapper) {
		// Not used
	}

	@Override
	public void setParameters (IParameters params) {
		// Not used
	}

	private Event processBuffer (StringBuilder sb) {
		// Convert to normal text fragment
		TextFragment tf = fromPseudoXLIFF(sb.toString());
		// Create the text unit and the skeleton
		ITextUnit tu = new TextUnit(tuIdGen.createId());
		tu.setSourceContent(tf);
		tu.setPreserveWhitespaces(true);
		
		boolean add = !skel.isEmpty();
		skel.addContentPlaceholder(tu);
		if ( add ) skel.append(ENDSEGMENT);
		
		skel.add(lineBreak);
		tu.setSkeleton(skel);
		
		return new Event(EventType.TEXT_UNIT, tu);
	}

	/**
	 * Converts a pseudoXLIFF string into a TextFragment. It assumes there is no
	 * escaped characters in attribute values.
	 * @param text the string to convert.
	 * @return the new text fragment.
	 */
	public TextFragment fromPseudoXLIFF (String text) {
		TextFragment tf = new TextFragment();
		// Empty?
		if ( Util.isEmpty(text) ) {
			return tf;
		}
		// Has code?
		if (( text.indexOf('<') == -1 ) && ( text.indexOf('&') == -1 )) {
			// Plain text
			tf.append(text);
			return tf;
		}

		text = text.replaceAll("(&#13;)|(&#x0*?[dD];)", "\r");
		text = text.replace("&apos;", "'");
		text = text.replace("&lt;", "<");
		text = text.replace("&gt;", ">");
		text = text.replace("&quot;", "\"");
		StringBuilder sb = new StringBuilder(text.replace("&amp;", "&"));
		
//TODO: MRK, bx, ex, etc.		
		// Otherwise: process the codes
		Code code;
		Matcher m;
		ArrayList<Code> codes = new ArrayList<Code>();

		// Opening/closing markers
		// This assume no-overlapping tags and no empty elements
		m = OPENCLOSE.matcher(sb.toString());
		Stack<Integer> stack = new Stack<Integer>();
		String markers;
		while ( m.find() ) {
			if (m.group(1) != null) {
				// It's an opening tag
				int id = Util.strToInt(m.group(3), -1);
				code = new Code(TagType.OPENING, "g", m.group(1));
				code.setId(id);
				codes.add(code);
				markers = String.format("%c%c", TextFragment.MARKER_OPENING,
					TextFragment.toChar(codes.size()-1));
				sb.replace(m.start(), m.end(), markers);
				stack.push(id);
			}
			else {
				// It's a closing tag
				codes.add(new Code(TagType.CLOSING, "g", m.group(4)));
				markers = String.format("%c%c", TextFragment.MARKER_CLOSING,
					TextFragment.toChar(codes.size()-1));
				sb.replace(m.start(), m.end(), markers);
			}
			m = OPENCLOSE.matcher(sb.toString());
		}

		m = ISOLATED.matcher(sb.toString());
		while ( m.find() ) {
			int id = Util.strToInt(m.group(3), -1);
			String name = m.group(1);
			if ( name.equals("bx") ) {
				// Match on IDs
				code = new Code(TagType.OPENING, "Xpt"+id, m.group());;
			}
			else if ( name.equals("ex") ) {
				// Match on IDs
				code = new Code(TagType.CLOSING, "Xpt"+id, m.group());;
			}
			else {
				code = new Code(TagType.PLACEHOLDER, "x", m.group());;
			}
			code.setId(id);
			codes.add(code);
			markers = String.format("%c%c", TextFragment.MARKER_ISOLATED,
				TextFragment.toChar(codes.size()-1));
			sb.replace(m.start(), m.end(), markers);
			m = ISOLATED.matcher(sb.toString());
		}

		m = LINEBREAK.matcher(sb.toString());
		while ( m.find() ) {
			sb.replace(m.start(), m.end(), "\n");
			m = LINEBREAK.matcher(sb.toString());
		}

		tf.setCodedText(sb.toString(), codes);
		return tf;
	}
	
}
