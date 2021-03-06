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

package net.sf.okapi.filters.versifiedtxt;

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
import java.util.regex.Matcher;
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
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.StartSubDocument;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.skeleton.ISkeletonWriter;

public class VersifiedTextWriter implements IFilterWriter {
	private static final String VERSIFIED_ID = "[^:]*:?[^:]+:([0-9]+)$";
	private static final Pattern VERSIFIED_ID_COMPILED = Pattern.compile(VERSIFIED_ID);
	
	private OutputStream output;
	private String outputPath;
	private OutputStreamWriter writer;
	private LocaleId language;
	private String encoding;
	private String linebreak;
	private File tempFile;
	private GenericContent formatter;	

	public VersifiedTextWriter() {
		formatter = new GenericContent();		
	}

	@Override
	public String getName() {
		return "VersifiedTextWriter";
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
		case START_SUBDOCUMENT:
			processStartSubDocument(event);
			break;
		case TEXT_UNIT:
			processTextUnit(event);
			break;
		default:
			event = Event.NOOP_EVENT;
			break;
		}
		return event;
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
					tempFile = File.createTempFile("~okapi-29_vrszwTmp_", null);
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
		try {
			StartDocument sd = event.getStartDocument();
			
			// Create the output
			createWriter(sd);

			// Writer header
			writer.write("|b" + sd.getName() + linebreak);
		} catch (IOException e) {
			throw new OkapiIOException("Error writing the versified text file header.", e);
		}
	}

	private void processEndDocument() {
		close();
	}

	private void processStartSubDocument(Event event) {
		try {
			StartSubDocument ssd = (StartSubDocument) event.getResource();
			writer.write("|c" + ssd.getName() + linebreak);
		} catch (IOException e) {
			throw new OkapiIOException("Error writing subdocument (|cName).", e);
		}
	}

	private void processTextUnit(Event event) {
		try {
			ITextUnit tu = event.getTextUnit();
			if (tu.isEmpty()) {
				return; // Do not write out entries with empty source
			}

			String verseId = tu.getId();
			Matcher m = VERSIFIED_ID_COMPILED.matcher(verseId);
			// if this is from an original versified file with book:chapter:verse 
			// then we just want to output the verse part (a number)
			if (m.matches()) {
				verseId = m.group(1);
			}
			writer.write("|v" + verseId + linebreak);

			// source
			writeTextContainer(tu.getSource());
			// <TARGET> tag			
			writer.write(linebreak + "<TARGET>" + linebreak);
			writeTextContainer(tu.getTarget(language));
			// there is always two newlines after the target text as formatting unless the target is empty
			if (tu.getTarget(language) == null || tu.getTarget(language).isEmpty()) {
				writer.write(linebreak); 
			} else {
				writer.write(linebreak + linebreak);
			}
		} catch (IOException e) {
			throw new OkapiIOException("Error writing a versified text unit.", e);
		}
	}

	private boolean writeTextContainer(TextContainer tc) {
		try {
			if (tc == null) {				
				return false;
			}

			String tmp;
			tmp = formatter.printSegmentedContent(tc, false, false);
			
			// replace with original format linefeeds
			tmp = tmp.replace("\n", linebreak);
			
			writer.write(tmp); // No wrapping needed
			if (tmp.endsWith("\n")) {
				return true;
			}
			return false;
		} catch (IOException e) {
			throw new OkapiIOException("Error writing TextContainer.", e);
		}
	}
}
