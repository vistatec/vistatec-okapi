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

package net.sf.okapi.common.filterwriter;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.exceptions.OkapiFileNotFoundException;
import net.sf.okapi.common.resource.DocumentPart;
import net.sf.okapi.common.resource.EndSubfilter;
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.StartGroup;
import net.sf.okapi.common.resource.StartSubDocument;
import net.sf.okapi.common.resource.StartSubfilter;
import net.sf.okapi.common.skeleton.ISkeletonWriter;

/**
 * Implements the IFilterWriter interface for filters that use the
 * GenericSkeleton skeleton.
 */
public class GenericFilterWriter implements IFilterWriter {

	protected OutputStreamWriter writer;

	private LocaleId locale;
	private String encoding;
	//private ISkeletonWriter parentSkelWriter;
	//private IEncoder parentEncoder;
	//private EncoderManager nativeEncoderManager;
	private ISkeletonWriter skelWriter;
	private OutputStream output;
	private String outputPath;
	private EncoderManager encoderManager;
	private File tempFile;
	
	public GenericFilterWriter() {
	}
	
	public GenericFilterWriter (ISkeletonWriter skelWriter,
		EncoderManager encoderManager)
	{
		//parentSkelWriter = skelWriter;		
		//nativeEncoderManager = encoderManager;
		
		this.skelWriter = skelWriter;
		this.encoderManager = encoderManager;
	}

	@Override
	public void cancel () {
		//TODO: implement cancel()
	}
	
	@Override
	public void close () {
		if ( writer == null ) return;
		if ( skelWriter != null ) skelWriter.close();
		IOException err = null;
		InputStream orig = null;
		OutputStream dest = null;
		try {
			// Close the output
			writer.close();
			writer = null;
			// Nullify the output stream
			output = null;

			// If it was in a temporary file, copy it over the existing one
			// If the IFilter.close() is called before IFilterWriter.close()
			// this should allow to overwrite the input.
			if ( tempFile != null ) {
				dest = new FileOutputStream(outputPath);
				orig = new FileInputStream(tempFile); 
				byte[] buffer = new byte[2048];
				int len;
				while ( (len = orig.read(buffer)) > 0 ) {
					dest.write(buffer, 0, len);
				}
			}
		}
		catch ( IOException e ) {
			err = e;
		}
		finally {
			// Make sure we close both files
			if ( dest != null ) {
				try {
					dest.close();
				}
				catch ( IOException e ) {
					err = e;
				}
				dest = null;
			}
			if ( orig != null ) {
				try {
					orig.close();
				} catch ( IOException e ) {
					err = e;
				}
				orig = null;
				if ( err != null ) throw new OkapiException(err);
				else {
					if ( tempFile != null ) {
						tempFile.delete();
						tempFile = null;
					}
				}
			}
		}
	}

	@Override
	public String getName () {
		return "GenericFilterWriter";
	}

	@Override
	public EncoderManager getEncoderManager () {
		return encoderManager;
	}
	
	@Override
	public ISkeletonWriter getSkeletonWriter () {
		return skelWriter;
	}

	/*
	 * For serialization
	 */
	protected void setSkelWriter(ISkeletonWriter skelWriter) {
		this.skelWriter = skelWriter;
	}

	@Override
	public IParameters getParameters () {
		return null;
	}

	@Override
	public Event handleEvent (Event event) {
		try {
			switch ( event.getEventType() ) {
			case START_DOCUMENT:
				processStartDocument(locale, encoding, event.getStartDocument());
				break;
			case END_DOCUMENT:
				processEndDocument(event.getEnding());
				close();
				break;
			case START_SUBDOCUMENT:
				processStartSubDocument(event.getStartSubDocument());
				break;
			case END_SUBDOCUMENT:
				processEndSubDocument(event.getEnding());
				break;
			case START_GROUP:
				processStartGroup(event.getStartGroup());
				break;
			case END_GROUP:
				processEndGroup(event.getEnding());
				break;
			case TEXT_UNIT:
				processTextUnit(event.getTextUnit());
				break;
			case DOCUMENT_PART:
				processDocumentPart(event.getDocumentPart());
				break;
			case MULTI_EVENT:
				for (Event e : event.getMultiEvent()) {
					handleEvent(e);
				}
				break;
			case START_SUBFILTER:
				processStartSubfilter(event.getStartSubfilter());
				break;
			case END_SUBFILTER:
				processEndSubfilter(event.getEndSubfilter());
				break;
			default:
				break;
			}
		}
		catch ( FileNotFoundException e ) {
			throw new OkapiFileNotFoundException("File not found.", e);
		}
		catch ( UnsupportedEncodingException e ) {
			throw new OkapiException(e);
		}
		catch ( IOException e ) {
			throw new OkapiException(e);
		}
		return event;
	}

	protected void processStartDocument(LocaleId outputLocale,
		String outputEncoding,
		StartDocument resource) throws IOException
	{
		// Create the output
		createWriter(resource);
		// Try to set the outputEncoding if it's null
		// (may have been set to the input in createWriter()) 
		if ( outputEncoding == null ) {
			outputEncoding = encoding;
		}
		writer.write(skelWriter.processStartDocument(outputLocale,
			outputEncoding, null, encoderManager, resource));
		//parentEncoder = encoderManager.getEncoder(); // The encoder used to write StartDocument of the parent filter
	}

	protected void processEndDocument(Ending resource) throws IOException {
		writer.write(skelWriter.processEndDocument(resource));
	}

	protected void processStartSubDocument (StartSubDocument resource) throws IOException {
		writer.write(skelWriter.processStartSubDocument(resource));
	}

	protected void processEndSubDocument (Ending resource) throws IOException {
		writer.write(skelWriter.processEndSubDocument(resource));
	}

	protected void processStartGroup (StartGroup resource) throws IOException {
		writer.write(skelWriter.processStartGroup(resource));
	}

	protected void processEndGroup (Ending resource) throws IOException {
		writer.write(skelWriter.processEndGroup(resource));
	}

	protected void processTextUnit (ITextUnit resource) throws IOException {
		writer.write(skelWriter.processTextUnit(resource));
	}

	protected void processDocumentPart (DocumentPart resource) throws IOException {
		writer.write(skelWriter.processDocumentPart(resource));
	}
	
	protected void processStartSubfilter (StartSubfilter resource) throws IOException {
		writer.write(skelWriter.processStartSubfilter(resource)); // Stores a ref to SSF
		// When skelWriter refers to a SubFilterSkeletonWriter, writer.write() is called for empty strings and does nothing  
		//skelWriter = new SubFilterSkeletonWriter(resource, parentEncoder, locale, encoding);		
		//skelWriter = new SubFilterSkeletonWriter(resource, parentEncoder);
		//skelWriter = resource.createSkeletonWriter(resource, locale, encoding);
	}

	protected void processEndSubfilter (EndSubfilter resource) throws IOException {
		//skelWriter = parentSkelWriter; // Restore the parent skeleton writer
		writer.write(skelWriter.processEndSubfilter(resource));
	}

	@Override
	public void setOptions (LocaleId locale,
		String defaultEncoding)
	{
		this.locale = locale;
		this.encoding = defaultEncoding;
	}

	@Override
	public void setOutput (String path) {
		close(); // Make sure previous is closed
		this.outputPath = path;
	}

	@Override
	public void setOutput (OutputStream output) {
		close(); // Make sure previous is closed
		this.output = output; // then assign the new stream
	}

	@Override
	public void setParameters (IParameters params) {
	}

	/**
	 * Provides sub-classes an opportunity to creates the character set encoder for the output.
	 * @param encodingtoUse the name of the encoding to use.
	 * @return the decoder to use for the output or null. When null is returned (default)
	 * the output uses the encoding.
	 */
	protected CharsetEncoder createCharsetEncoder (String encodingtoUse) {
		return null; // Default is to use the encoding not the encoder
	}
	
	private void createWriter (StartDocument resource) {
		try {
			tempFile = null;
			// If needed, create the output stream from the path provided
			if ( output == null ) {
				boolean useTemp = false;
				File f = new File(outputPath);
				if ( f.exists() ) {
					// If the file exists, try to remove
					useTemp = !f.delete();
				}
				if ( useTemp ) {
					// Use a temporary output if we can overwrite for now
					// If it's the input file, IFilter.close() will free it before we
					// call close() here (that is if IFilter.close() is called correctly
					tempFile = File.createTempFile("~okapi-7_", null);
					output = new BufferedOutputStream(new FileOutputStream(tempFile.getAbsolutePath()));
				}
				else { // Make sure the directory exists
					Util.createDirectories(outputPath);
					output = new BufferedOutputStream(new FileOutputStream(outputPath));
				}
			}
			
			// Get the encoding of the original document
			String originalEnc = resource.getEncoding();
			// If it's undefined, assume it's the default of the system
			if ( originalEnc == null ) {
				originalEnc = Charset.defaultCharset().name();
			}
			// Check if the output encoding is defined
			if ( encoding == null ) {
				// if not: Fall back on the encoding of the original
				encoding = originalEnc;
			}

			// Get the decoder to used
			CharsetEncoder csEncoder = createCharsetEncoder(encoding);
			// Create the output
			if ( csEncoder != null ) { // Use the encoder if not null
				writer = new OutputStreamWriter(output, csEncoder);
			}
			else { // But by default use the encoding
				// The behavior is different: we get ? for unknown characters with the encoding
				// but we get an exception with the encoder
				writer = new OutputStreamWriter(output, encoding);
			}
			
			// Set default UTF-8 BOM usage
			boolean useUTF8BOM = false; // On all platforms
			// Check if the output encoding is UTF-8
			if ( "utf-8".equalsIgnoreCase(encoding) ) {
				// If the original was UTF-8 too
				if ( "utf-8".equalsIgnoreCase(originalEnc) ) {
					// Check whether it had a BOM or not
					useUTF8BOM = resource.hasUTF8BOM();
				}
			}
			// Write out the BOM if needed
			Util.writeBOMIfNeeded(writer, useUTF8BOM, encoding);
		}
		catch ( FileNotFoundException e ) {
			throw new OkapiException(e);
		}
		catch ( IOException e ) {
			throw new OkapiException(e);
		}
	}

	public final LocaleId getLocale() {
		return locale;
	}

	public final String getDefEncoding() {
		return encoding;
	}
	
	//////////////////////////////////////////
	// For serialization
	//////////////////////////////////////////
	protected String getEncoding() {
		return encoding;
	}

	protected void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	protected String getOutputPath() {
		return outputPath;
	}

	protected void setLocale(LocaleId locale) {
		this.locale = locale;
	}

	protected void setEncoderManager(EncoderManager encoderManager) {
		this.encoderManager = encoderManager;
	}
}
