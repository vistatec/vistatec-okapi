/*===========================================================================
  Copyright (C) 2008-2011 by the Okapi Framework contributors
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

package net.sf.okapi.filters.versifiedtxt;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.okapi.common.BOMNewlineEncodingDetector;
import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.IResource;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.exceptions.OkapiBadFilterInputException;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.filters.AbstractFilter;
import net.sf.okapi.common.filters.EventBuilder;
import net.sf.okapi.common.filters.FilterConfiguration;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filterwriter.GenericFilterWriter;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.AlignmentStatus;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.StartSubDocument;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.common.skeleton.GenericSkeleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link IFilter} for a Versified text file.
 * 
 * @author HARGRAVEJE
 * @author HiginbothamDW
 */
@UsingParameters()
// No parameters
public class VersifiedTextFilter extends AbstractFilter {
	// custom placeholders
	private static final Map<String, String> REPLACABLES = new HashMap<String, String>();
    static {
    	REPLACABLES.put("{tab}", "\t");
    	REPLACABLES.put("{nb}", "\u00a0");
    	REPLACABLES.put("{em}", "\u2014");
    	REPLACABLES.put("{en}", "\u2013");
    	REPLACABLES.put("{emsp}", "\u2003");
    	REPLACABLES.put("{ensp}", "\u2002");
    }
               
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	private static final int BUFFER_SIZE = 2800;

	private static final String VERSIFIED_ID = "^([0-9]+)$";
	private static final Pattern VERSIFIED_ID_COMPILED = Pattern.compile(VERSIFIED_ID);
	
	public static final String VERSIFIED_TXT_MIME_TYPE = "text/x-versified-txt";
	private static final String VERSE = "^[ \\t]*\\|v([^ ]+)[ \\t]*(\\(([^()]+)\\))?(\\+\\|)?[ \\t]*$";
	private static final Pattern VERSE_COMPILED = Pattern.compile(VERSE);
	
	private static final String TRADOS_SEGMENTS = "\\{0>(.*?)<\\}[0-9]+\\{>(.*?)<0\\}";
	private static final Pattern TRADOS_SEGMENTS_COMPILED = Pattern.compile(TRADOS_SEGMENTS, 
			Pattern.DOTALL|Pattern.MULTILINE|Pattern.UNICODE_CASE);
	private static final String TRADOS_LEAVINGS = "(\\{0>)|(<0\\})|(<\\}[0-9]+\\{>)|(<\\})|(\\{>)";
	private static final Pattern TRADOS_LEAVINGS_COMPILED = Pattern.compile(TRADOS_LEAVINGS, 
			Pattern.DOTALL|Pattern.MULTILINE|Pattern.UNICODE_CASE);		
	
	private static final String CHAPTER = "^[ \t]*\\|c.+[ \t]*$";
	private static final String BOOK = "^[ \t]*\\|b.+[ \t]*$";
	private static final String TARGET = "^[ \t]*<TARGET>[ \t]*$";
	private static final String PLACEHOLDER = "(\\{|</?)([0-9]+)(\\}|>)";
	private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile(PLACEHOLDER);

	private String newline = "\n";
	private String currentChapter;
	private String currentBook;
	private int currentChar;
	private EventBuilder eventBuilder;
	private EncoderManager encoderManager;
	private boolean hasUtf8Bom;
	private boolean hasUtf8Encoding;
	private BufferedReader versifiedFileReader;
	private RawDocument currentRawDocument;
	private BOMNewlineEncodingDetector detector;
	private StartSubDocument startSubDocument;
	private Parameters params;
	private StringBuilder filterBuffer;
	private boolean foundVerse;
	private boolean foundBook;
	private boolean trados;

	/** Creates a new instance of VersifiedCodeNgramIndexer */
	public VersifiedTextFilter() {
		super();		

		this.currentChapter = "";
		this.currentBook = "";
		
		this.foundVerse = false;
		this.foundBook = false;
		this.trados = false;

		setMimeType(VERSIFIED_TXT_MIME_TYPE);
		setMultilingual(false); // default value, could be multilingual we check below
		setFilterWriter(new GenericFilterWriter(createSkeletonWriter(), getEncoderManager()));	
		// Cannot use '_' or '-' in name: conflicts with other filters (e.g. plaintext, table)
		// for defining different configurations
		setName("okf_versifiedtxt"); //$NON-NLS-1$
		setDisplayName("Versified Text Filter"); //$NON-NLS-1$
		addConfiguration(new FilterConfiguration(getName(), VERSIFIED_TXT_MIME_TYPE, getClass()
				.getName(), "Versified Text", "Versified Text Documents"));
		setParameters(new Parameters());
	}

	@Override
	public IFilterWriter createFilterWriter() {
		return super.createFilterWriter();
	}

	@Override
	public void open(RawDocument input) {
		this.foundVerse = false;
		this.foundBook = false;
		open(input, true);
	}

	@Override
	public void open(RawDocument input, boolean generateSkeleton) {
		// close any previous streams we opened
		close();

		this.currentRawDocument = input;
		this.currentChapter = "";
		this.currentBook = "";
		this.currentChar = -2;
		filterBuffer = new StringBuilder(BUFFER_SIZE - 1);
		
		if (input.getInputURI() != null) {
			setDocumentName(input.getInputURI().getPath());
		}

		detector = new BOMNewlineEncodingDetector(input.getStream(), input.getEncoding());
		detector.detectAndRemoveBom();

		setEncoding(input.getEncoding());
		hasUtf8Bom = detector.hasUtf8Bom();
		hasUtf8Encoding = detector.hasUtf8Encoding();
		newline = detector.getNewlineType().toString();
		setNewlineType(newline);

		// set encoding to the user setting
		String detectedEncoding = getEncoding();

		// may need to override encoding based on what we detect
		if (detector.isDefinitive()) {
			detectedEncoding = detector.getEncoding();
			LOGGER.debug("Overridding user set encoding (if any). Setting auto-detected encoding ({}).",
					detectedEncoding);
		} else if (!detector.isDefinitive() && getEncoding().equals(RawDocument.UNKOWN_ENCODING)) {
			detectedEncoding = detector.getEncoding();
			LOGGER.debug("Default encoding and detected encoding not found. Using best guess encoding ({})",
							detectedEncoding);
		}

		input.setEncoding(detectedEncoding);
		setEncoding(detectedEncoding);
		setOptions(input.getSourceLocale(), input.getTargetLocale(), detectedEncoding,
				generateSkeleton);

		versifiedFileReader = new BufferedReader(input.getReader());
		
		// is the format multilingual?
		String line = "";		
		int bufferCount = 0;
		try {
			versifiedFileReader.mark(BUFFER_SIZE);
			while ((line = versifiedFileReader.readLine()) != null) {
				bufferCount += (line.length()+2);
				if (bufferCount >= BUFFER_SIZE) {
					break;
				}
				if (line.matches(TARGET)) {
					setMultilingual(true);
					trados = false;
					break;
				}
				if (line.matches(TRADOS_SEGMENTS)) {
					setMultilingual(true);
					trados = true;
					break;
				}
			}
			versifiedFileReader.reset();
		} catch (IOException e) {
			throw new OkapiIOException("IO error detecting if file is multilingual: "
					+ (line == null ? "unkown line" : line), e);
		}		
		
		// create EventBuilder with document name as rootId
		if (eventBuilder == null) {
			eventBuilder = new EventBuilder();
		} else {
			eventBuilder.reset(null, this);
		}
	}

	@Override
	public void close() {
		if (currentRawDocument != null) {
			currentRawDocument.close();
		}

		if (versifiedFileReader != null) {
			try {
				versifiedFileReader.close();
			} catch (IOException e) {
				LOGGER.warn("Error closing the versified text buffered reader.", e);

			}
		}
	}

	@Override
	public EncoderManager getEncoderManager() {
		if (encoderManager == null) {
			encoderManager = new EncoderManager();
			encoderManager.setMapping(VERSIFIED_TXT_MIME_TYPE,
					"net.sf.okapi.common.encoder.DefaultEncoder");
		}
		return encoderManager;
	}

	@Override
	public IParameters getParameters() {
		return params;
	}

	@Override
	public void setParameters(IParameters params) {
		this.params = (Parameters) params;
	}

	@Override
	public boolean hasNext() {
		return eventBuilder.hasNext();
	}

	@Override
	public Event next() {
		String currentLine = null;

		// process queued up events before we produce more
		while (eventBuilder.hasQueuedEvents()) {
			return eventBuilder.next();
		}

		// loop over versified file one character at a time
		while (currentChar != -1 && !isCanceled()) {
			try {
				currentChar = versifiedFileReader.read();
				filterBuffer.append((char)currentChar);
				if (currentChar == '\r' || currentChar == '\n' || currentChar == -1) {
					filterBuffer.setLength(filterBuffer.length() - 1);
					currentLine = filterBuffer.toString();
					currentLine = Util.trimEnd(currentLine, "\r\n");
					filterBuffer = new StringBuilder(BUFFER_SIZE-1);

					// break early if we have no more text
					if (currentChar == -1 && currentLine.isEmpty()) {
						break;
					}

					// don't output newline if this is the last text in the file
					newline = handleNewline();
					if (currentChar == -1) {
						newline = "";
					}

					if (currentLine.matches(VERSE)) {
						handleDocumentPart(currentLine + newline);		
						Matcher m = VERSE_COMPILED.matcher(currentLine);
						String verseId = "";
						String sid = null;
						if (m.matches()) {
							verseId = m.group(1);
							sid = m.group(3);
						}
						handleVerse(versifiedFileReader, currentLine, verseId, sid);
						this.foundVerse = true;
					} else if (currentLine.matches(BOOK)) {
						currentBook = currentLine.substring(2);
						setDocumentName(currentBook);
						eventBuilder.addFilterEvent(createStartFilterEvent());
						handleDocumentPart(currentLine + newline);
						this.foundBook = true;
					} else if (currentLine.matches(CHAPTER)) {
						currentChapter = currentLine.substring(2);
						if (startSubDocument != null) {
							eventBuilder.endSubDocument();
						}
						handleSubDocument(currentChapter);
						handleDocumentPart(currentLine + newline);
					} else {
						handleDocumentPart(currentLine + newline);
					}

					// break if we have produced at least one event
					if (eventBuilder.hasQueuedEvents()) {
						break;
					}
				}
			} catch (IOException e) {
				throw new OkapiIOException("IO error reading versified file at: "
						+ (currentLine == null ? "unkown line" : currentLine), e);
			}
		} 

		if (currentChar == -1) {
			// reached the end of the file
			if (startSubDocument != null) {
				eventBuilder.endSubDocument();
			}
			eventBuilder.flushRemainingTempEvents();
			
			if (!foundBook) {
				eventBuilder.addFilterEvent(createStartFilterEvent());
				LOGGER.warn("Missing book marker at start of document: |b");
			}
			eventBuilder.addFilterEvent(createEndFilterEvent());
			
			if (!foundVerse) {
				throw new OkapiBadFilterInputException("There are no verse codes in this document");
			}			
		}

		return eventBuilder.next();
	}

	@Override
	protected boolean isUtf8Bom() {
		return hasUtf8Bom;
	}

	@Override
	protected boolean isUtf8Encoding() {
		return hasUtf8Encoding;
	}
	
	@SuppressWarnings("incomplete-switch")
	private String handleNewline() throws IOException {
		String newline = "\n";
		switch (detector.getNewlineType()) {
		case CR:
			newline = "\r";
			break;
		case CRLF:
			newline = "\r\n";
			// eat the \n
			versifiedFileReader.read();
			break;
		case LF:
			newline = "\n";
			break;
		}
		return newline;
	}

	private void handleSubDocument(String chapter) {
		startSubDocument = eventBuilder.startSubDocument();
		startSubDocument.setName(chapter);
	}

	private void handleVerse(BufferedReader verse, String currentVerse, String verseId, String sid)
			throws IOException {
		String currentLine = null;
		StringBuilder source = new StringBuilder(BUFFER_SIZE);
		StringBuilder target = new StringBuilder(BUFFER_SIZE);
		boolean targetTag = false;
		
		verse.mark(BUFFER_SIZE);
		while (currentChar != -1) {			
			try {
				currentChar = versifiedFileReader.read();				
				filterBuffer.append((char)currentChar);
				if (currentChar == '\r' || currentChar == '\n' || currentChar == -1) {
					filterBuffer.setLength(filterBuffer.length() - 1);
					currentLine = filterBuffer.toString();					
					currentLine = Util.trimEnd(currentLine, "\r\n");					
					filterBuffer = new StringBuilder(BUFFER_SIZE - 1);
					
					// newline is always normalized to \n inside TextUnit except for skeleton
					newline = handleNewline();

					if (currentLine.matches(VERSE) || currentLine.matches(BOOK) || currentLine.matches(CHAPTER)) {						
						verse.reset();
						break;
					}

					if (currentLine.matches(TARGET)) {
						targetTag = true;			
						continue;
					}
					
					if (targetTag) {
						target.append(currentLine + "\n");
					} else {
						source.append(currentLine + "\n");
					}
					verse.mark(BUFFER_SIZE);
				}
			} catch (IOException e) {
				throw new OkapiIOException("IO error reading versified file at: "
						+ (currentLine == null ? "unkown line" : currentLine), e);
			}
		}

		// assume any newlines after the final content goes with the string
		// but we have to at least remove the extra newline added above
		String modifiedSource = chopNewline(source.toString());
		String modifiedTarget = chopNewline(target.toString());		
		if (currentChar != -1) {
			if (targetTag) {
				modifiedSource = chopNewline(modifiedSource);
				modifiedTarget = chopNewline(modifiedTarget);
			} else {
				modifiedSource = chopNewline(chopNewline(modifiedSource));
			}
		} else {
			// last entry trim *all* training newlines
			if (targetTag) {
				modifiedTarget = Util.trimEnd(modifiedTarget, "\n");
			} else {
				modifiedSource = Util.trimEnd(modifiedSource, "\n");
			}
		}
		
		if (targetTag) {
			// if this is the last target and there is no text then we don't want any newlines			
			if (currentChar == -1 && chopNewline(modifiedTarget).isEmpty()) {
				modifiedTarget = "";
			}
		}

		// build the TextUnit based on format
		eventBuilder.startTextUnit();
		ITextUnit tu = buildTextUnit(modifiedSource, modifiedTarget, targetTag, trados);
		
		// if this was a bilingual verse then setup the <TARGET> tag
		// as skeleton
		GenericSkeleton skel = new GenericSkeleton();
		skel.addContentPlaceholder(tu);			
		if (targetTag) { // bilingual case			 						 
			skel.add(newline + "<TARGET>" + newline);
			skel.addContentPlaceholder(tu, getTrgLoc());			 						
		} 		
		// always two newlines after final string of the verse no matter mono or bilingual
		// but not if its the final string
		if (currentChar != -1) {			
			skel.add(newline + newline); 
		}
		tu.setSkeleton(skel);
		
		// see if the verse id is a number only - if so it it not unique so we
		// add book and chapter ids 
		Matcher m = VERSIFIED_ID_COMPILED.matcher(verseId);
		if (sid != null) {
			tu.setName(sid);
			tu.setId(verseId);
		} else {
			if (m.matches()) {
				tu.setName(currentBook + ":" + currentChapter + ":" + m.group(1));
				tu.setId(currentChapter + (currentChapter != null && currentChapter.isEmpty() ? "" : ":") + m.group(1));
			} else {
				// else id from some other source just copy the id
				tu.setName(verseId);
				tu.setId(verseId);
			}
		}
				
		tu.setMimeType(getMimeType());
		eventBuilder.endTextUnit();
	}
	
	private String replacePlacebles(String text) {
		if (text == null || text.isEmpty()) {
			return text;
		}
		
		// search and replace "REPLACABLES" with raw character
		for (String r : REPLACABLES.keySet()) {
			text = text.replace(r, REPLACABLES.get(r));
		}		
		return text;
	}

	private ITextUnit buildTextUnit(String source, String target, boolean targetTag, boolean trados) {
		ITextUnit tu = eventBuilder.peekTempEvent().getTextUnit();
		source = replacePlacebles(source);
		target = replacePlacebles(target);
		
		if (trados) {
			tu = buildTextUnitForTrados(source);
		} else {
			buildTextUnitForNonTrados(source, true);
			if (targetTag) {
				buildTextUnitForNonTrados(target, false);
			}
		}
		
		return tu;
	}
	
	private ITextUnit buildTextUnitForTrados(String text) {
		ITextUnit tu = eventBuilder.peekTempEvent().getTextUnit();
		
		Matcher m = TRADOS_SEGMENTS_COMPILED.matcher(text);
		int i = 0;
		if (m.find()) {
			tu.createTarget(getTrgLoc(), true, IResource.CREATE_EMPTY);
			m.reset();
			while (m.find()) {
				i++;
				Segment srcSeg = new Segment(Integer.toString(i), buildTextFragment(m.group(1)));
				Segment trgSeg = new Segment(Integer.toString(i), buildTextFragment(m.group(2)));
				tu.getSource().append(srcSeg);
				tu.getTarget(getTrgLoc()).append(trgSeg);
			}
			tu.getTarget(getTrgLoc()).getSegments().setAlignmentStatus(AlignmentStatus.ALIGNED);
		} else {
			if (TRADOS_LEAVINGS_COMPILED.matcher(text).find()) {
				throw new OkapiBadFilterInputException("Trados segment markers found in source or target text: " +
						text);
			}
			
			// treat as monolingual paragraph and log a warning
			buildTextUnitForNonTrados(text, true);
			LOGGER.warn("In a Trados bilingual document but found no segment markers. " +
					"Treating as monlingual text: {}", text);
		}
		
		return tu;
	}
	
	private TextFragment buildTextFragment(String text) {
		// TODO: duplicated code alert! simplify later
		TextFragment tf = new TextFragment();
		
		Matcher m = PLACEHOLDER_PATTERN.matcher(text);
		if (m.find()) {
			m.reset();
			String[] chunks = PLACEHOLDER_PATTERN.split(text);
			for (int i = 0; i < chunks.length; i++) {
				tf.append(chunks[i]); // eventBuilder.addToTextUnit(chunks[i]);
				if (m.find()) {
					String ph = text.substring(m.start(), m.end());
					Code c = new Code(TagType.PLACEHOLDER, ph, ph);
					c.setId(Integer.parseInt(m.group(2)));
					tf.append(c); // eventBuilder.addToTextUnit(new Code(TagType.PLACEHOLDER, ph, ph));
				}
			}
		} else {
			// no placeholders found - treat is text only
			tf.append(text); // eventBuilder.addToTextUnit(text);
		}			
		return tf;
	}
	
	private void buildTextUnitForNonTrados(String text, boolean source) {
		if (TRADOS_LEAVINGS_COMPILED.matcher(text).find()) {
			throw new OkapiBadFilterInputException("Trados segment markers found in source or target text: " +
					text);
		}
		
		if (source) {
			eventBuilder.setTargetLocale(null);
		} else {
			eventBuilder.setTargetLocale(getTrgLoc());
		}
		
		Matcher m = PLACEHOLDER_PATTERN.matcher(text);
		if (m.find()) {
			m.reset();
			String[] chunks = PLACEHOLDER_PATTERN.split(text);
			for (int i = 0; i < chunks.length; i++) {
				eventBuilder.addToTextUnit(chunks[i]);
				if (m.find()) {
					String ph = text.substring(m.start(), m.end());
					Code c = new Code(TagType.PLACEHOLDER, ph, ph);
					c.setId(Integer.parseInt(m.group(2)));
					eventBuilder.addToTextUnit(c);
				}
			}
		} else {
			// no placeholders found - treat is text only
			eventBuilder.addToTextUnit(text);
		}			
	}

	private void handleDocumentPart(String part) {
		eventBuilder.addDocumentPart(part);
	}	
	
	/* 
	 * Remove one newline from the end of the string
	 */
	private String chopNewline(String text)
	{
		if ( text == null || text.isEmpty()) {
			return text;
		}
		
		if (text.charAt(text.length()-1) == '\n') {
			return text.substring(0, text.length()-1);
		}
		
		return text;
	}
}
