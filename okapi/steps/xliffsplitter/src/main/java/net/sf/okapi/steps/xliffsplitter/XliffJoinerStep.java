/*===========================================================================
  Copyright (C) 2009-2010 by the Okapi Framework contributors
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

package net.sf.okapi.steps.xliffsplitter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import net.sf.okapi.common.BOMNewlineEncodingDetector;
import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.exceptions.OkapiBadFilterParametersException;
import net.sf.okapi.common.exceptions.OkapiBadStepInputException;
import net.sf.okapi.common.exceptions.OkapiFileNotFoundException;
import net.sf.okapi.common.exceptions.OkapiUnsupportedEncodingException;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
import net.sf.okapi.common.pipeline.annotations.StepParameterType;
import net.sf.okapi.common.resource.RawDocument;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@UsingParameters(XliffJoinerParameters.class)
public class XliffJoinerStep extends BasePipelineStep {

	/**
	 * Help class for merging xliff files with different base filenames   
	 */
	class BaseXliffFile{
		
		ArrayList<XMLEvent> firstFileTempElems = new ArrayList<XMLEvent>();
		ArrayList<String> filesUsed = new ArrayList<String>();
		int fileCount;
		XMLEventWriter eventWriter = null;
		
		
		/**
		 * Initiates the writer for the first instance of a base filename
		 */
        void initiateWriter(String pOutputFileUri, String pEncoding, boolean pHasUTF8BOM){

        	try {
        		//--this section is for writing the bom--
        		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pOutputFileUri),pEncoding));
        		Util.writeBOMIfNeeded(bw, pHasUTF8BOM, pEncoding);

        		eventWriter = outputFactory.createXMLEventWriter(bw);

        	}catch (UnsupportedEncodingException e) {
        		throw new OkapiUnsupportedEncodingException(e);
        	}catch (FileNotFoundException e) {
        		throw new OkapiFileNotFoundException(e);
        	}catch (XMLStreamException e) {
        		throw new OkapiBadStepInputException(e);
        	}
        }
		
		
		/**
		 * Writes the part after the last file for the first instance of a base filename and closes the writer
		 * @throws XMLStreamException
		 */
		void writeAndClose() throws XMLStreamException{
			
			if (eventWriter != null){
				for (XMLEvent ev : firstFileTempElems){
					eventWriter.add(ev);
				}
				eventWriter.flush();
				eventWriter.close();
			}
		}
	}
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private HashMap<String, BaseXliffFile> baseXliffFiles = new HashMap<String, BaseXliffFile>();
	
	private XliffJoinerParameters params;
	private URI outputURI;

	XMLInputFactory inputFactory = XMLInputFactory.newInstance();
	XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
	XMLEventFactory  eventFactory = XMLEventFactory.newInstance();
	

	public XliffJoinerStep () {
		params = new XliffJoinerParameters();
		// security concern. Turn off DTD processing
		// https://www.owasp.org/index.php/XML_External_Entity_%28XXE%29_Processing
		inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);			
	}
	
	@StepParameterMapping(parameterType = StepParameterType.OUTPUT_URI)
	public void setOutputURI(final URI outputURI) {
		this.outputURI = outputURI;
	}
	
	public URI getOutputURI() {
		return outputURI;
	}
	
	@Override
	public String getDescription () {
		return "Join multiple XLIFF documents into one. Expects: raw document. Sends back: raw document.";
	}

	@Override
	public String getName () {
		return "XLIFF Joiner";
	}

	@Override
	public IParameters getParameters () {
		return params;
	}

	@Override
	public void setParameters (IParameters params) {
		this.params = (XliffJoinerParameters)params;
	}
 
	@Override
	protected Event handleStartBatch(Event event) {
		
		if(params.getInputFileMarker().trim().length() == 0){
			throw new OkapiBadFilterParametersException("The input file marker cannot be empty");
		}else if(params.getOutputFileMarker().trim().length() == 0){
			logger.warn("Leaving output file marker empty your original file(s) could be overwritten");
		}
		
		return event;
	}
	
	@Override
	protected Event handleEndBatch (Event event) {
		
		for (Map.Entry<String, BaseXliffFile> entry : baseXliffFiles.entrySet()) { 

		    BaseXliffFile baseFile = entry.getValue(); 

			try {
				baseFile.writeAndClose();
			} catch (XMLStreamException e) {
				throw new OkapiBadStepInputException(e);
			}
		} 
		return event;
	}

	@Override
	protected Event handleRawDocument(final Event event) {

		BaseXliffFile baseFile;
		int fileElemNo = 0;
		XMLEvent xmlEvent;
		
		ArrayList<XMLEvent> elemsBetweenOrAfterLastFileElem = new ArrayList<XMLEvent>();
		
		final RawDocument rawDoc = event.getRawDocument();		
		
		//--for output filename--
		String outputDir = Util.getDirectoryName(outputURI.getPath());
		String inputFileName = Util.getFilename(rawDoc.getInputURI().getPath(), false);
		String inputFileExtension = Util.getExtension(rawDoc.getInputURI().getPath());
		
		String baseFilename = getBaseFilename(inputFileName, params.getInputFileMarker());
		
		if ( baseFilename == null){
			logger.warn("This file is skipped: Input marker not found in its name.");
			return event;
		}

		//--TODO validate base filename and marker--

		//--detect file properties for each file-
		BOMNewlineEncodingDetector detector = new BOMNewlineEncodingDetector(rawDoc.getStream(),"utf-8");
		detector.detectBom();

		String encoding = detector.getEncoding();
		boolean hasUTF8BOM = detector.hasUtf8Bom();
		String lineBreak = detector.getNewlineType().toString();

		
		//--check if this is the first file
		if(baseXliffFiles.containsKey(baseFilename)){
			baseFile = baseXliffFiles.get(baseFilename);
			
			baseFile.fileCount++;
			baseFile.filesUsed.add(Util.getFilename(rawDoc.getInputURI().getPath(), true));

		}else{
        	String outputFileUri = outputDir + File.separator + baseFilename 
        	+ params.getOutputFileMarker() + inputFileExtension;

			baseFile = new BaseXliffFile();
			baseFile.initiateWriter(outputFileUri, encoding, hasUTF8BOM);			

			baseXliffFiles.put(baseFilename, baseFile);
			
			baseFile.fileCount=1;
			baseFile.filesUsed.add(Util.getFilename(rawDoc.getInputURI().getPath(), true));
		}
		
	
        //--initiate the reader for each file--
		XMLEventReader eventReader = initiateReader(detector, encoding, rawDoc.getStream());

		while (eventReader.hasNext()) {

			try {
				xmlEvent = eventReader.nextEvent();
				
				if ( baseFile.fileCount == 1){

					if (xmlEvent.getEventType() == XMLEvent.START_DOCUMENT){
						
						baseFile.eventWriter.add(xmlEvent);
						baseFile.eventWriter.add(eventFactory.createSpace(lineBreak));
							
						continue;
					}

					if (xmlEvent.getEventType() == XMLEvent.START_ELEMENT && xmlEvent.asStartElement().getName().getLocalPart().equals("file")){
						fileElemNo++;
						writeFilePart(xmlEvent, eventReader, baseFile.eventWriter, fileElemNo, baseFile.firstFileTempElems);
					}else{
						if(fileElemNo == 0){
							//--writing anything before file
							baseFile.eventWriter.add(xmlEvent);
						}else{
							//--otherwise storing it as either between or after content
							baseFile.firstFileTempElems.add(xmlEvent);
						}
					}
					
				} else {
					
					//--for following files write only the content between the start and closing file--
					if (xmlEvent.getEventType() == XMLEvent.START_ELEMENT && xmlEvent.asStartElement().getName().getLocalPart().equals("file")){

						baseFile.eventWriter.add(eventFactory.createSpace(lineBreak));
						
						fileElemNo++;
						writeFilePart(xmlEvent, eventReader, baseFile.eventWriter, fileElemNo, elemsBetweenOrAfterLastFileElem);
					}else{
						if(fileElemNo > 0){
							//--otherwise storing it as either between or after content
							elemsBetweenOrAfterLastFileElem.add(xmlEvent);
						}
					}
				}
				
			} catch (XMLStreamException e) {
				throw new OkapiBadStepInputException(e);
			}
		}
		
		return event;
	}
	
	
	private XMLEventReader initiateReader(BOMNewlineEncodingDetector detector,
			String encoding, InputStream inputStream) {
		
		XMLEventReader eventReader;

		try {
    		if ( detector.isAutodetected() ) {
    			eventReader = inputFactory.createXMLEventReader(inputStream, encoding);
    		}
    		else {
    			logger.info("Encoding could not be auto-detected. Using default encoding: {}", encoding);
    			eventReader = inputFactory.createXMLEventReader(inputStream);
    		}
    	} catch (XMLStreamException e) {
    		throw new OkapiBadStepInputException(e);
    	}
    	
    	return eventReader;
	}
	
	/**
	 * Return the base filename
	 * @param fileName
	 * @param fileMarker
	 * @return
	 */
	private String getBaseFilename(String fileName, String fileMarker) {

		int index = fileName.lastIndexOf(fileMarker);
		
		if(index == -1)
			return null;
		else 
			return fileName.substring(0,index);
	}

	/**
	 * Writes a <file> section including content between previous ending </file> and current <file>
	 * @param startFileEvent The <file> element
	 * @param eventReader The eventReader for the current file
	 * @param eventWriter The eventWriter for the current base file
	 * @param pFileElemIndex The <file> element index in the current file, supporting multiple file elements
	 * @param pTempElems The content that has been collected since the last </file>
	 * @throws XMLStreamException
	 */
	private void writeFilePart(XMLEvent startFileEvent, XMLEventReader eventReader, XMLEventWriter eventWriter, int pFileElemIndex, ArrayList<XMLEvent> pTempElems) throws XMLStreamException{
        
		//--write pre <file> content if needed--
		if(pFileElemIndex > 1){
			
			for (XMLEvent tempElem : pTempElems){
				eventWriter.add(tempElem);
			}
			
			pTempElems.clear();
		}
		
		//--write start <file>--
		eventWriter.add(startFileEvent);

		//--write remaining <file> content
		while (eventReader.hasNext()) {
			XMLEvent xmlEvent = eventReader.nextEvent();

			eventWriter.add(xmlEvent);

			if (xmlEvent.getEventType() == XMLEvent.END_ELEMENT && xmlEvent.asEndElement().getName().getLocalPart().equals("file")){
				return;
			}
		}
	}
	
	/*private int getFileNo(String fileName, String fileMarker) {

		int index = fileName.indexOf(fileMarker);

		String number = fileName.substring(index + fileMarker.length());

		return Integer.parseInt(number); 
	}
	
	
	private boolean hasFileMarker(String fileName, String fileMarker) {

		if (fileName.indexOf(fileMarker) == -1)
			return false;
		else 
			return true;
	}*/
	
}
