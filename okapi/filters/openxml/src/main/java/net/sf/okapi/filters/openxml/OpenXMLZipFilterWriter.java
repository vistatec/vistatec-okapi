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

package net.sf.okapi.filters.openxml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.ISkeleton;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.exceptions.OkapiBadFilterInputException;
import net.sf.okapi.common.exceptions.OkapiFileNotFoundException;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.exceptions.OkapiNotImplementedException;
import net.sf.okapi.common.filterwriter.GenericFilterWriter;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.DocumentPart;
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.StartSubDocument;
import net.sf.okapi.common.skeleton.GenericSkeletonWriter;
import net.sf.okapi.common.skeleton.ISkeletonWriter;
import net.sf.okapi.common.skeleton.ZipSkeleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.sf.okapi.filters.openxml.DocumentType.isMasterPart;
import static net.sf.okapi.filters.openxml.DocumentType.isPagePart;
import static net.sf.okapi.filters.openxml.DocumentType.isRelationshipsPart;

/**
 * <p>Implements the IFilterWriter interface for the OpenXMLFilter, which
 * filters Microsoft Office Word, Excel, and Powerpoint Documents. OpenXML 
 * is the format of these documents.
 * 
 * <p>Since OpenXML files are Zip files that contain XML documents,
 * this filter writer handles writing out the zip file, and
 * uses OpenXMLContentSkeletonWriter to output the XML documents.
 * 
 */

public class OpenXMLZipFilterWriter implements IFilterWriter {

	private String outputPath;
	private OpenXMLZipFile zipOriginal;
	private ZipOutputStream zipOut;
	private byte[] buffer;
	private LocaleId sourceLocale;
	private LocaleId targetLocale;
	private File tempFile;
	private File tempZip;
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	private EncoderManager encoderManager;
	private ZipEntry subDocEntry;
	private IFilterWriter subDocWriter;
	private ISkeletonWriter subSkelWriter;
	private TreeMap<Integer,OpenXMLSubDoc> tmSubDoc = new TreeMap<Integer,OpenXMLSubDoc>();
	private int ndxSubDoc = 0;
	private OutputStream outputStream;
	private XMLInputFactory inputFactory;
	private XMLOutputFactory outputFactory;
	private XMLEventFactory eventFactory;
	private DocumentType docType;
	private ConditionalParameters cparams;

	/**
	 * We must have a no-arg constructor for the kit serialization will work.  In this
	 * case, we create local factory instances.
	 */
	public OpenXMLZipFilterWriter() {
		this.inputFactory = XMLInputFactory.newInstance();
		this.outputFactory = XMLOutputFactory.newInstance();
		this.eventFactory = XMLEventFactory.newInstance();
		
		// security concern. Turn off DTD processing
		// https://www.owasp.org/index.php/XML_External_Entity_%28XXE%29_Processing
		inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);			
	}

	public OpenXMLZipFilterWriter(ConditionalParameters cparams, XMLInputFactory inputFactory,
					XMLOutputFactory outputFactory, XMLEventFactory eventFactory) {
		this.inputFactory = inputFactory;
		this.outputFactory = outputFactory;
		this.eventFactory = eventFactory;
		this.cparams = cparams;
	}
	/**
	 * Cancels processing of a filter; yet to be implemented.
	 */
	public void cancel () {
		//TODO: implement cancel()
	}
	
	/**
	 * Closes the zip file.
	 */
	public void close () {
		if ( zipOut == null ) return;
		IOException err = null;
		InputStream orig = null;
		OutputStream dest = null;
		try {
			// Closing reference to the original input stream 
			if (zipOriginal != null){
				zipOriginal.close();
				zipOriginal = null;
			}
			
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
				if ( err != null ) {
					throw new OkapiIOException("Error closing MS Office 2007 file.");
				} else {
					if ( tempZip != null ) {
						tempZip.delete();
						tempZip = null;
					}
				}
			}
		}
	}

	/**
	 * Gets the name of the filter writer.
	 */
	public String getName () {
		return "OpenXMLZipFilterWriter"; 
	}

	public EncoderManager getEncoderManager () {
		if ( encoderManager == null ) {
			encoderManager = new EncoderManager();
			//TOFIX: set only the needed mappings
			encoderManager.setAllKnownMappings();
		}
		return encoderManager;
	}
	
	@Override
	public ISkeletonWriter getSkeletonWriter () {
		return subSkelWriter;
	}

	/**
	 * Handles an event.  Passes all but START_DOCUMENT, END_DOCUMENT,
               * and DOCUMENT_PART to subdocument processing.
	 * @param event the event to process
	 */
	@Override
	public Event handleEvent (Event event) {
		switch ( event.getEventType() ) {
		case START_DOCUMENT:
			processStartDocument((StartDocument)event.getResource());
			break;
		case DOCUMENT_PART:
			processDocumentPart(event);
			break;
		case END_DOCUMENT:
			processEndDocument();
			break;
		case START_SUBDOCUMENT:
			processStartSubDocument((StartSubDocument)event.getResource());
			break;
		case END_SUBDOCUMENT:
			processEndSubDocument((Ending)event.getResource());
			break;
		case TEXT_UNIT:
		case START_GROUP:
		case END_GROUP:
		case START_SUBFILTER:
		case END_SUBFILTER:
			try {
				subDocWriter.handleEvent(event);
			} catch(Throwable e) {
				String mess = e.getMessage();
				throw new OkapiNotImplementedException(mess, e); // kludge
			}
			break;
		case CANCELED:
			break;
		}
		return event;
	}

	public void setOptions (LocaleId language,
		String defaultEncoding)
	{
		targetLocale = language;
	}

	public void setOutput (String path) {
		outputPath = path;
	}

	public void setOutput (OutputStream output) {
		this.outputStream = output;
	}

	/**
	 * Processes the start document for the whole zip file by
               * initializing a temporary output file, and and output stream.
	 * @param res a resource for the start document
	 */

	private void processStartDocument (StartDocument res) {
		try {
			buffer = new byte[2048];
			sourceLocale = res.getLocale();
			ZipSkeleton skel = (ZipSkeleton)res.getSkeleton();
			ZipFile zipTemp = skel.getOriginal(); // if OpenXML filter was closed, this ZipFile has been marked for close
			File fZip = new File(zipTemp.getName()); // so get its name
			zipOriginal = new OpenXMLZipFile(new ZipFile(fZip,ZipFile.OPEN_READ),
					inputFactory, outputFactory, eventFactory, "UTF-8"); // and re-open it
              // *** this might not work if the ZipFile was from a URI that was not a normal file path ***
			docType = zipOriginal.createDocument(cparams);
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
					tempZip = File.createTempFile("~okapi-24_zfwTmpZip_", null);
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
			throw new OkapiFileNotFoundException("Existing file could not be overwritten.", e);
		}
		catch ( IOException | XMLStreamException e) {
			throw new OkapiIOException("File could not be written.", e);
		}
	}
	
	private void processEndDocument () {
		close();
	}
	
	/**
	 * This passes a file that doesn't need processing from the input zip file to the output zip file.
	 *
	 * @param event corresponding to the file to be passed through
	 */
	private void processDocumentPart (Event event) {
		DocumentPart documentPart = (DocumentPart) event.getResource();

		if ( documentPart.getSkeleton() instanceof ZipSkeleton ) {
			ZipSkeleton skeleton = (ZipSkeleton) documentPart.getSkeleton();

			if (skeleton instanceof MarkupZipSkeleton) {
				clarifyMarkup(((MarkupZipSkeleton) skeleton).getMarkup());
			}

			// Copy the entry data
			try {
				zipOut.putNextEntry(new ZipEntry(skeleton.getEntry().getName()));

				// If the contents were modified by the filter, write out the new data
				String modifiedContents = skeleton.getModifiedContents();

				if (modifiedContents != null) {
					zipOut.write(modifiedContents.getBytes(StandardCharsets.UTF_8));
				}
				else {
					InputStream input = zipOriginal.getInputStream(skeleton.getEntry());
					int len;
					while ( (len = input.read(buffer)) > 0 ) {
						zipOut.write(buffer, 0, len);
					}
					input.close();
				}
				zipOut.closeEntry();
			}
			catch ( IOException e ) {
				throw new OkapiIOException("Error writing zip file entry.");
			}
		}
		else { // Otherwise it's a normal skeleton event
			subDocWriter.handleEvent(event);
		}
	}

	private void clarifyMarkup(Markup markup) {
		Nameable nameableMarkupComponent = markup.getNameableMarkupComponent();

		if (null != nameableMarkupComponent) {
            BidirectionalityClarifier bidirectionalityClarifier = new BidirectionalityClarifier(
                    new CreationalParameters(
                            eventFactory,
							nameableMarkupComponent.getName().getPrefix(),
							nameableMarkupComponent.getName().getNamespaceURI()),
                    new ClarificationParameters(LocaleId.isBidirectional(targetLocale),
                            LocaleId.hasCharactersAsNumeralSeparators(targetLocale),
                            targetLocale.toString()));

            bidirectionalityClarifier.clarifyMarkup(markup);
        }
	}

	/**
	 * Starts processing a new file withing the zip file.  It looks for the 
               * element type of "filetype" in the yaml parameters which need to
               * be set before handleEvent is called, and need to be the same as
               * the parameters on the START_SUBDOCUMENT event from the
               * OpenXMLFilter (by calling setParameters).  Once the type of the
               * file is discovered from the Parameters, a subdoc writer is 
               * created from OpenXMLContentSkeletonWriter, and a temporary
               * output file is created.
	 * @param res resource of the StartSubDocument
	 */
	private void processStartSubDocument (StartSubDocument res) {
		ndxSubDoc++; // DWH 1-10-2013 subDoc map
		OpenXMLSubDoc openXMLSubDoc = new OpenXMLSubDoc(); // DWH 1-10-2013 subDoc map
		tmSubDoc.put(Integer.valueOf(ndxSubDoc), openXMLSubDoc); // DWH 1-10-2013 subDoc map

		// Set the temporary path and create it
		try {
			tempFile = File.createTempFile("~okapi-25_zfwTmp"+ndxSubDoc+"_", null);
		}
		catch ( IOException e ) {
			throw new OkapiIOException("Error opening temporary zip output file.");
		}

		ISkeleton skel = res.getSkeleton();
		ConditionalParameters conditionalParameters = (ConditionalParameters) res.getFilterParameters();
		if (skel instanceof ZipSkeleton) {
			subDocEntry = ((ZipSkeleton) res.getSkeleton()).getEntry();
			String contentType = getContentTypeForPart(subDocEntry);
			if (docType.isStyledTextPart(subDocEntry.getName(), contentType)) {
				subSkelWriter = new StyledTextSkeletonWriter(eventFactory, subDocEntry.getName(), conditionalParameters);
			} else if (isRelationshipsPart(contentType) || isMasterPart(contentType) || isPagePart(contentType)) {
				subSkelWriter = new GenericSkeletonWriter();
			} else {
				ParseType nFileType = conditionalParameters.nFileType;
				subSkelWriter = new OpenXMLContentSkeletonWriter(nFileType);
			}
		} else {
			subDocEntry = new ZipEntry(res.getName());
			ParseType nFileType = conditionalParameters.nFileType;
			subSkelWriter = new OpenXMLContentSkeletonWriter(nFileType);
		}

		subDocWriter = new GenericFilterWriter(subSkelWriter, getEncoderManager()); // YS 12-20-09
		subDocWriter.setOptions(targetLocale, "UTF-8");
		subDocWriter.setOutput(tempFile.getAbsolutePath());
		
		StartDocument sd = new StartDocument("sd");
		sd.setLineBreak("\n");
		sd.setSkeleton(res.getSkeleton());
		sd.setLocale(sourceLocale);
		subDocWriter.handleEvent(new Event(EventType.START_DOCUMENT, sd));
		openXMLSubDoc.setSubDocSkelWriter(subSkelWriter); // DWH 1-10-2013 subDoc map
		openXMLSubDoc.setSubDocEntry(subDocEntry); // DWH 1-10-2013 subDoc map
		openXMLSubDoc.setSubDocWriter(subDocWriter); // DWH 1-10-2013 subDoc map
		openXMLSubDoc.setSubTempFile(tempFile);
	}

	private String getContentTypeForPart(ZipEntry entry) {
		try {
			return zipOriginal.getContentTypes().getContentType("/" + entry.getName());
		}
		catch (IOException | XMLStreamException e) {
			throw new OkapiBadFilterInputException(e);
		}
	}

	/**
	 * Finishes writing the subdocument temporary file, then adds it as an
               * entry in the temporary zip output file.
	 * @param res resource of the end subdocument
	 */
	private void processEndSubDocument (Ending res) {
		try {
			OpenXMLSubDoc openXMLSubDoc = tmSubDoc.get(Integer.valueOf(ndxSubDoc--));
			subDocWriter = openXMLSubDoc.getSubDocWriter(); // DWH 1-10-2013 subDoc map
			subDocEntry = openXMLSubDoc.getSubDocEntry(); // DWH 1-10-2013 subDoc map
			subSkelWriter = openXMLSubDoc.getSubSkelWriter(); // DWH 1-10-2013 subDoc map
			tempFile = openXMLSubDoc.getSubDocTempFile();
			// Finish writing the sub-document
			subDocWriter.handleEvent(new Event(EventType.END_DOCUMENT, res));
			subDocWriter.close();

			// Create the new entry from the temporary output file
			zipOut.putNextEntry(new ZipEntry(subDocEntry.getName()));
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
			throw new OkapiIOException("Error closing zip output file.");
		}
	}
	public void setParameters(IParameters params) // DWH 7-16-09
	{
		this.cparams = (ConditionalParameters)params;
	}
	public ConditionalParameters getParameters() // DWH 7-16-09
	{
		return cparams;
	}
}
