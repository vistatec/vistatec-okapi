/*===========================================================================
  Copyright (C) 2009-2013 by the Okapi Framework contributors
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.ResourceUtil;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.resource.DocumentPart;
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.StartSubDocument;
import net.sf.okapi.common.skeleton.GenericSkeletonWriter;
import net.sf.okapi.common.skeleton.ISkeletonWriter;
import net.sf.okapi.common.skeleton.ZipSkeleton;

/**
 * Implements the {@link IFilterWriter} interface for filters that handle formats made of
 * a ZIP package with embedded extractable documents, such as IDML or
 * OpenOffice.org files (ODT, ODS, ODP, etc.)
 */
public class ZipFilterWriter implements IFilterWriter {

	private String outputPath;
	private OutputStream outputStream;
	private ZipFile zipOriginal;
	private ZipOutputStream zipOut;
	private byte[] buffer;
	private LocaleId outLoc;
	private String entryName;
	private IFilterWriter subDocWriter;
	private File tempFile;
	private File tempZip;
	private EncoderManager encoderManager;
	private int subDocLevel;

	public ZipFilterWriter (EncoderManager encoderManager) {
		this.encoderManager = encoderManager;
	}
	
	public void cancel () {
		//TODO: implement cancel()
		zipOriginal = null;
	}
	
	public void close () {
		zipOriginal = null;
		if ( zipOut == null ) return;
		IOException err = null;
		InputStream orig = null;
		OutputStream dest = null;
		try {
			// Close the output
			zipOut.close();
			zipOut = null;

			// If it was in a temporary file, copy it over the existing one
			// If the IFilter.close() is called before IFilterWriter.close()
			// this should allow to overwrite the input.
			if ( tempZip != null ) {
				dest = new FileOutputStream(outputPath);
				orig = new FileInputStream(tempZip); 
				int len;
				while ( (len = orig.read(buffer)) > 0 ) {
					dest.write(buffer, 0, len);
				}
			}
			buffer = null;
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
					if ( tempZip != null ) {
						tempZip.delete();
						tempZip = null;
					}
				}
			}
		}
	}

	public String getName () {
		return "ZipFilterWriter";
	}

	public EncoderManager getEncoderManager () {
		return encoderManager;
	}
	
	@Override
	public ISkeletonWriter getSkeletonWriter () {
		return null;
	}

	public IParameters getParameters () {
		return null; // Not used
	}

	public Event handleEvent (Event event) {
		switch ( event.getEventType() ) {
		case START_DOCUMENT:
			subDocLevel = 0;
			processStartDocument((StartDocument)event.getResource());			
			break;
		case DOCUMENT_PART:
			processDocumentPart(event);
			break;
		case END_DOCUMENT:
			processEndDocument();
			subDocLevel = 0;
			break;
		case START_SUBDOCUMENT:
			if ( subDocLevel == 0 ) {
				processStartSubDocument((StartSubDocument)event.getResource());
			}
			else {
				subDocWriter.handleEvent(event);
			}
			subDocLevel++;
			break;
		case END_SUBDOCUMENT:
			subDocLevel--;
			if ( subDocLevel == 0 ) {
				processEndSubDocument((Ending)event.getResource());
			}
			else {
				subDocWriter.handleEvent(event);
			}
			break;
		case TEXT_UNIT:
		case START_GROUP:
		case END_GROUP:
		case START_SUBFILTER:
		case END_SUBFILTER:
			subDocWriter.handleEvent(event);
			break;
		default:
			break;
		}
		return event;
	}

	public void setOptions (LocaleId locale,
		String defaultEncoding)
	{
		outLoc = locale;
	}

	public void setOutput (String path) {
		outputPath = path;
	}

	public void setOutput (OutputStream output) {
		this.outputStream = output;
	}

	public void setParameters (IParameters params) {
		// Not used
	}

	protected void processStartDocument (StartDocument res) {
		try {
			buffer = new byte[2048];
			zipOriginal = null;
			
			ZipSkeleton skel = (ZipSkeleton)res.getSkeleton();
			if (skel != null)
				zipOriginal = skel.getOriginal();
			
			tempZip = null;			
			// Create the output stream from the path provided
			boolean useTemp = false;
			
			File f;
			OutputStream os = outputStream;
			if (outputStream == null) {							
				f = new File(outputPath);
				if ( f.exists() ) {
					// If the file exists, try to remove
					useTemp = !f.delete();				
				}
				if (useTemp) {
					// Use a temporary output if we can overwrite for now
					// If it's the input file, IFilter.close() will free it before we
					// call close() here (that is if IFilter.close() is called correctly!)
					tempZip = File.createTempFile("~okapi-8_", null);
					os = new FileOutputStream(tempZip.getAbsolutePath());
				} else {
					Util.createDirectories(outputPath);
					os = new FileOutputStream(outputPath);
				}
			} else {
				os = outputStream;
			}
			
			// create zip output
			zipOut = new ZipOutputStream(os);		
		}
		catch ( FileNotFoundException e ) {
    		throw new OkapiException(e);
		}
		catch ( IOException e ) {
    		throw new OkapiException(e);
		}
	}
	
	private void processEndDocument () {
		close();
	}
	
	protected void processDocumentPart (Event event) {
		// Treat top-level ZipSkeleton events
		DocumentPart res = (DocumentPart)event.getResource();
		if ( res.getSkeleton() instanceof ZipSkeleton ) {
			ZipSkeleton skel = (ZipSkeleton)res.getSkeleton();
			ZipFile original = skel.getOriginal();
			if (original == null)
				original = zipOriginal;
			ZipEntry entry = skel.getEntry();
			// Copy the entry data
			InputStream input = null;
			try {
				zipOut.putNextEntry(new ZipEntry(entry.getName()));
				input = original.getInputStream(entry); 
				int len;
				while ( (len = input.read(buffer)) > 0 ) {
					zipOut.write(buffer, 0, len);
				}				
			}
			catch ( IOException e ) {
				throw new OkapiIOException("Error processing ZipFile entry", e);
			} finally {				
				try {
					if (input != null) input.close();
					zipOut.closeEntry();
				} catch (IOException e) {
					throw new OkapiIOException("Error closing ZipFile", e);
				}
			}
		}
		else { // Otherwise it's a normal skeleton event
			subDocWriter.handleEvent(event);
		}
	}

	protected ISkeletonWriter createSubDocumentSkeletonWriter (StartSubDocument res) {
        return new GenericSkeletonWriter();
	}
	
	protected IFilterWriter createSubDocumentFilterWriter (StartSubDocument res) {
        IFilterWriter writer = new GenericFilterWriter(createSubDocumentSkeletonWriter(res), getEncoderManager());
        writer.setOptions(outLoc, "UTF-8");        
        return writer;
	}
	
	protected StartDocument convertToStartDocument(StartSubDocument res) {		
		return ResourceUtil.startDocumentFromStartSubDocument(res, "sd", "\n");
	}
	
	protected void processStartSubDocument (StartSubDocument res) {
		ZipSkeleton skel = (ZipSkeleton)res.getSkeleton();
		ZipEntry entry = skel.getEntry();
		if (entry != null)
			entryName = entry.getName();

		// Set the temporary path and create it
		try {
			tempFile = File.createTempFile("~okapi-9_", null);
		}
		catch ( IOException e ) {
			throw new OkapiException(e);
		}
		
		// Instantiate the filter writer for that entry if not set from outside with setSubDocWriter()
		if (subDocWriter == null) {
			subDocWriter = createSubDocumentFilterWriter(res);
		}			
		subDocWriter.setOutput(tempFile.getAbsolutePath());
				
		StartDocument sd = convertToStartDocument(res);
		subDocWriter.handleEvent(new Event(EventType.START_DOCUMENT, sd));
	}
	
	protected void processEndSubDocument (Ending res) {
		try {
			// Finish writing the sub-document
			subDocWriter.handleEvent(new Event(EventType.END_DOCUMENT, res));
			subDocWriter.close();
			
			// Reset subDocWriter, next sub-document might require a different writer
			// (a default writer will be created if not set from outside)
			subDocWriter = null;

			// Create the new entry from the temporary output file
			zipOut.putNextEntry(new ZipEntry(entryName));
			InputStream input = new FileInputStream(tempFile); 
			int len;
			while ( (len = input.read(buffer)) > 0 ) {
				zipOut.write(buffer, 0, len);
			}
			input.close();
			zipOut.closeEntry();
			// Delete the temporary file
			tempFile.delete();
		}
		catch ( IOException e ) {
			throw new OkapiException(e);
		}
	}

	public IFilterWriter getSubDocWriter() {
		return subDocWriter;
	}

	public void setSubDocWriter(IFilterWriter subDocWriter) {
		this.subDocWriter = subDocWriter;
		subDocWriter.setOptions(outLoc, "UTF-8");
	}
	
	public final LocaleId getLocale() {
		return outLoc;
	}

}
