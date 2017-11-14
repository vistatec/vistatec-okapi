/*===========================================================================
  Copyright (C) 2011 by the Okapi Framework contributors
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

package net.sf.okapi.filters.doxygen;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.exceptions.OkapiFileNotFoundException;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.filterwriter.GenericContent;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.DocumentPart;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.StartGroup;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.skeleton.GenericSkeleton;
import net.sf.okapi.common.skeleton.GenericSkeletonPart;
import net.sf.okapi.common.skeleton.ISkeletonWriter;


/**
 * @author aaron.madlon-kay
 *
 */
public class DoxygenWriter implements IFilterWriter {
	
	private static final String SELF_REF = "$self$";
	private static final String DUMMY_SKELETON = String.format(
			"%s%s%s",
			TextFragment.REFMARKER_START,
			SELF_REF,
			TextFragment.REFMARKER_END);
	
	private OutputStream output;
	private String outputPath;
	private OutputStreamWriter writer;
	private LocaleId language;
	private String encoding;
	private String linebreak;
	private File tempFile;
	private GenericContent formatter;
	
	/**
	 * Accumulate the comment body of a Group here.
	 * Write it out upon END_GROUP.
	 */
	private StringBuilder commentBuffer;
	
	private HashMap<String, String> referencedText;
	
	/**
	 * Store the skeleton of the current Group here.
	 * Write it out interleaved with the contents of commentBuffer
	 * upon END_GROUP.
	 */
	private List<GenericSkeletonPart> skeletonParts;
	
	private static String LINEBREAK_PATTERN_TEMPLATE = "(?<=%s)";
	private Pattern linebreakPattern;
	private int numLines;

	public DoxygenWriter() {
		formatter = new GenericContent();	
		commentBuffer = new StringBuilder();
		referencedText = new HashMap<String, String>();
	}

	@Override
	public String getName() {
		return "DoxygenWriter";
	}

	@Override
	public void setOptions(LocaleId language, String defaultEncoding) {
		this.language = language;
		this.encoding = defaultEncoding;
	}

	@Override
	public void setOutput(String path) {
		close(); // Make sure previous is closed
		this.outputPath = path;
	}

	@Override
	public void setOutput(OutputStream output) {
		close(); // Make sure previous is closed
		this.outputPath = null; // If we use the stream, we can't use the path
		this.output = output; // then assign the new stream
	}

	@Override
	public Event handleEvent(Event event) {
		
		switch (event.getEventType()) {
		case START_DOCUMENT:
			processStartDocument(event);
			break;
		case END_DOCUMENT:
			processEndDocument();
			break;
		case START_GROUP:
			processStartGroup(event);
			break;
		case END_GROUP:
			processEndGroup();
			break;
		case TEXT_UNIT:
			processTextUnit(event);
			break;
		case DOCUMENT_PART:
			processDocumentPart(event);
			break;
		default:
			event = Event.NOOP_EVENT;
			break;
		}
		return event;
	}

	private void processEndGroup() {
		
		String[] commentLines = linebreakPattern.split(commentBuffer.toString());
		commentBuffer = new StringBuilder();
		
		try {
			for (int i = 0; i < commentLines.length || i < skeletonParts.size(); i++) {
				if (i < skeletonParts.size()) writer.write(skeletonParts.get(i).toString());
				if (i < commentLines.length) writer.write(commentLines[i]);
				if (i >= commentLines.length && i < skeletonParts.size())
					writer.write(linebreak);
			}
			
			int wroteLines = commentLines.length > skeletonParts.size() ? commentLines.length : skeletonParts.size();
			for (int i = wroteLines; i < numLines; i++)
				writer.write(linebreak);
			
		} catch (IOException e) {
			throw new OkapiIOException("Error writing a Doxygen-commented file.", e);
		}
	}

	private void processStartGroup(Event event) {
		StartGroup group = event.getStartGroup();
		skeletonParts = ((GenericSkeleton) group.getSkeleton()).getParts();
		
		Property prop = group.getProperty(DoxygenFilter.NUMLINES_PROPERTY);
		numLines = prop != null ? Integer.parseInt(prop.getValue()) : -1;
	}

	@Override
	public void close() {
		if (writer == null) {
			return;
		}
		IOException err = null;
		InputStream orig = null;
		OutputStream dest = null;
		try {
			// Close the output
			writer.close();
			writer = null;
			output.close();
			output = null;

			// If it was in a temporary file, copy it over the existing one
			// If the IFilter.close() is called before IFilterWriter.close()
			// this should allow to overwrite the input.
			if (tempFile != null) {
				dest = new FileOutputStream(outputPath);
				orig = new FileInputStream(tempFile);
				byte[] buffer = new byte[2048];
				int len;
				while ((len = orig.read(buffer)) > 0) {
					dest.write(buffer, 0, len);
				}
			}
		} catch (IOException e) {
			err = e;
		} finally {
			// Make sure we close both files
			if (dest != null) {
				try {
					dest.close();
				} catch (IOException e) {
					err = e;
				}
				dest = null;
			}
			if (orig != null) {
				try {
					orig.close();
				} catch (IOException e) {
					err = e;
				}
				orig = null;
				if (err != null)
					throw new OkapiException(err);
				else {
					if (tempFile != null) {
						tempFile.delete();
						tempFile = null;
					}
				}
			}
		}
	}

	@Override
	public IParameters getParameters() {
		return null;
	}

	@Override
	public void setParameters(IParameters params) {		
	}

	@Override
	public void cancel() {
		// TODO
	}

	@Override
	public EncoderManager getEncoderManager() {
		// This writer does not use skeleton
		return null;
	}

	@Override
	public ISkeletonWriter getSkeletonWriter() {
		return null;
	}
	
	private void createWriter(StartDocument startDoc) {
		linebreak = startDoc.getLineBreak();
		linebreakPattern = Pattern.compile(String.format(LINEBREAK_PATTERN_TEMPLATE, linebreak));
		try {
			tempFile = null;
			// If needed, create the output stream from the path provided
			if (output == null) {
				boolean useTemp = false;
				File f = new File(outputPath);
				if (f.exists()) {
					// If the file exists, try to remove
					useTemp = !f.delete();
				}
				if (useTemp) {
					// Use a temporary output if we can overwrite for now
					// If it's the input file, IFilter.close() will free it before we
					// call close() here (that is if IFilter.close() is called correctly
					tempFile = File.createTempFile("~okapi-19_vrszwTmp_", null);
					output = new BufferedOutputStream(new FileOutputStream(
							tempFile.getAbsolutePath()));
				} else { // Make sure the directory exists
					Util.createDirectories(outputPath);
					output = new BufferedOutputStream(new FileOutputStream(outputPath));
				}
			}

			// Get the encoding of the original document
			String originalEnc = startDoc.getEncoding();
			// If it's undefined, assume it's the default of the system
			if (originalEnc == null) {
				originalEnc = Charset.defaultCharset().name();
			}
			// Check if the output encoding is defined
			if (encoding == null) {
				// if not: Fall back on the encoding of the original
				encoding = originalEnc;
			}
			// Create the output
			writer = new OutputStreamWriter(output, encoding);
			// Set default UTF-8 BOM usage
			boolean useUTF8BOM = false; // On all platforms
			// Check if the output encoding is UTF-8
			if ("utf-8".equalsIgnoreCase(encoding)) {
				// If the original was UTF-8 too
				if ("utf-8".equalsIgnoreCase(originalEnc)) {
					// Check whether it had a BOM or not
					// Linux tools do not like BOM
					useUTF8BOM = false; // startDoc.hasUTF8BOM();
				}
			}
			// Write out the BOM if needed
			Util.writeBOMIfNeeded(writer, useUTF8BOM, encoding);
		} catch (FileNotFoundException e) {
			throw new OkapiFileNotFoundException(e);
		} catch (IOException e) {
			throw new OkapiIOException(e);
		}
	}

	private void processStartDocument(Event event) {
		StartDocument sd = event.getStartDocument();
		
		// Create the output
		createWriter(sd);
		
		referencedText.clear();
	}

	private void processEndDocument() {
		close();
	}

	private void processTextUnit(Event event) {
			
		ITextUnit tu = event.getTextUnit();
		
		GenericSkeleton skel = (GenericSkeleton) tu.getSkeleton();
		
		StringBuilder tuText = new StringBuilder();
		
		tuText.append(skel != null ? skel.toString() : DUMMY_SKELETON);
		
		
		// Body of TU
		TextContainer bodyContainer = null;
		if (tu.hasTarget(language)) bodyContainer = tu.getTarget(language);
		else bodyContainer = tu.getSource();
		
		referencedText.put(SELF_REF, formatter.printSegmentedContent(bodyContainer, false, true));
		
		// Replace all refmarkers with the appropriate content.
		Object[] refMarkInfo = TextFragment.getRefMarker(tuText);
		while (refMarkInfo != null) {
			assert(referencedText.containsKey(refMarkInfo[0]));
			tuText.replace(
					(Integer) refMarkInfo[1],
					(Integer) refMarkInfo[2],
					referencedText.get(refMarkInfo[0]));
			refMarkInfo = TextFragment.getRefMarker(tuText);
		}
		
		if (tu.getReferenceCount() > 0) {
			referencedText.put(tu.getId(), tuText.toString());
			return;
		}
		
		commentBuffer.append(tuText);
	}
	
	private void processDocumentPart(Event event) {
		try {
			DocumentPart part = event.getDocumentPart();
			writer.write(part.toString());
			
		} catch (IOException e) {
			throw new OkapiIOException("Error writing a Doxygen-commented file.", e);
		}
		
	}

// Unused
//	/**
//	 * Split a single line of text into multiple lines of equal length.
//	 * It is not safe to break lines that contain Doxygen commands with
//	 * arguments longer than WORD-length.
//	 * TODO: Implement metadata tracking of lines that are safe to split.
//	 * @param s The string to split
//	 * @param lines The number of lines to split into
//	 * @return A list of strings
//	 */
//	private List<String> splitText(String s, int lines) {
//		// TODO: Redo this with tokenizers (with CJK support!) so we don't blindly chop
//		// everything.
//		List<String> parts = new ArrayList<String>();
//		
//		int stride = s.length() / lines + s.length() % lines;
//		
//		for (int i = 0; i < lines; i++) {
//			// Make sure we don't overrun the string bounds
//			int start = i * stride;
//			int end = Math.min((i+1) * stride, s.length());
//			parts.add(s.substring(start, end));
//		}
//		
//		return parts;
//	}
	
}
