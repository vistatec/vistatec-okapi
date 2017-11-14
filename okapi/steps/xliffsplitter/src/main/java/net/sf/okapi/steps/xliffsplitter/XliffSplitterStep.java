/*===========================================================================
  Copyright (C) 2010 by the Okapi Framework contributors
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

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import net.htmlparser.jericho.Attributes;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.OutputDocument;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;
import net.sf.okapi.common.BOMNewlineEncodingDetector;
import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.exceptions.OkapiBadStepInputException;
import net.sf.okapi.common.exceptions.OkapiFileNotFoundException;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.exceptions.OkapiUnsupportedEncodingException;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
import net.sf.okapi.common.pipeline.annotations.StepParameterType;
import net.sf.okapi.common.resource.RawDocument;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Splits a single XLIFF file into multiple files, split on the file element. All other content (outside the file
 * element) is copied as-is to each split file. Expects a {@link RawDocument} as input and sends the {@link RawDocument}
 * {@link Event} unaltered. Will output multiple split XLIFF files in the set output path.
 * An XLIFF file with only one file element is written out unaltered.
 *
 * @author Greg Perkins
 * @author HargraveJE
 * 
 */
@UsingParameters(XliffSplitterParameters.class)
public class XliffSplitterStep extends BasePipelineStep {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private XliffSplitterParameters params;
	private boolean done = false;
	private URI outputURI;
	
	public XliffSplitterStep() {
		params = new XliffSplitterParameters();
	}

	@StepParameterMapping(parameterType = StepParameterType.OUTPUT_URI)
	public void setOutputURI(final URI outputURI) {
		this.outputURI = outputURI;
	}
	
	public URI getOutputURI() {
		return outputURI;
	}
	
	@Override
	public String getDescription() {
		return "Split an XLIFF document into separate files for each <file> element. Expects: raw document. Sends back: raw document.";
	}

	@Override
	public String getName() {
		return "XLIFF Splitter";
	}

	@Override
	public IParameters getParameters() {
		return params;
	}

	@Override
	public void setParameters(final IParameters params) {
		this.params = (XliffSplitterParameters) params;
	}

	@Override
	protected Event handleStartBatch(final Event event) {
		done = true;
		return event;
	}

	@Override
	protected Event handleStartBatchItem(final Event event) {
		done = false;
		return event;
	}

	/* TODO: Send the splitted files down the pipeline once the context functionality is added	
	@Override
	protected Event handleEndBatchItem(final Event event) {
		//done = false;
		return event;
	}*/

	@Override
	public boolean isDone() {
		return done;
	}

	@Override
	protected Event handleRawDocument(final Event event) {
		final RawDocument rawDoc = event.getRawDocument();
		
		/* TODO: Send the splitted files down the pipeline once the context functionality is added
		List<Event> tempEvents = new ArrayList<Event>();*/
		
		if(!params.isBigFile()){
		
			final List<Element> fileElements = new ArrayList<Element>();

			Source source;
			try {
				source = new Source(rawDoc.getReader());
			} catch (final IOException e) {
				throw new OkapiIOException("Error creating Jericho Source object", e);
			}

			// Find all <file> elements
			// TODO: Are there any <file> elements that aren't children of <xliff>? Don't want to get too many.
			fileElements.addAll(source.getAllElements("file"));

			// Mark the insertion point
			final int insertPosition = fileElements.get(0).getBegin();

			// Write out a separate xliff file for each <file>
			int count = 1;
			for (final Element element : fileElements) {
				// Create an output document for modification and writing
				final OutputDocument skeletonDocument = new OutputDocument(source);

				// Remove all <file> elements from the document
				skeletonDocument.remove(fileElements);

				// Update the translation status in the current <file>
				String file;
				if (params.isUpdateSDLTranslationStatus()) {
					file = updateTranslationStatus(element.toString());
				} else {
					file = element.toString();
				}

				// Add the <file> element
				skeletonDocument.insert(insertPosition, file);

				String filename = Util.getDirectoryName(outputURI.getPath()) + File.separator
				+ Util.getFilename(outputURI.getPath(), false);
				filename += "." + String.valueOf(count++) + Util.getExtension(outputURI.getPath());

				PrintWriter writer = null;
				try {
					// Open a writer for the new file
					Util.createDirectories(filename);
					final OutputStream output = new BufferedOutputStream(new FileOutputStream(filename));
					writer = new PrintWriter(new OutputStreamWriter(output, "UTF-8"));
					skeletonDocument.writeTo(writer);
				} catch (final IOException e) {
					throw new OkapiIOException(e);
				} catch (final NullPointerException e) {
					throw new OkapiFileNotFoundException(e);
				} finally {
					done = true;
					if (writer != null) {
						writer.close();
						writer = null;
					}
				}
			}

			return event;
		
		}else{

			//--file properties--
			String encoding;
			String lineBreak;
			boolean hasUTF8BOM;
			
			//--for output filename--
			String outputDir = Util.getDirectoryName(outputURI.getPath());
			String inputFileName = Util.getFilename(rawDoc.getInputURI().getPath(), false);
			String inputFileExtension = Util.getExtension(rawDoc.getInputURI().getPath());

			//--detect file properties-
			BOMNewlineEncodingDetector detector = new BOMNewlineEncodingDetector(rawDoc.getStream(),"utf-8");
			detector.detectBom();
			
			encoding = detector.getEncoding();
			hasUTF8BOM = detector.hasUtf8Bom();
			lineBreak = detector.getNewlineType().toString();

			XMLInputFactory inputFactory = XMLInputFactory.newInstance();
			XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
			XMLEventFactory  eventFactory = XMLEventFactory.newInstance();
			
			// security concern. Turn off DTD processing
			// https://www.owasp.org/index.php/XML_External_Entity_%28XXE%29_Processing
			inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);			

			XMLEventReader eventReader;

			try {
				if ( detector.isAutodetected() ) {
					eventReader = inputFactory.createXMLEventReader(rawDoc.getStream(), encoding);
				}
				else {
					logger.info("Encoding could not be auto-detected. Using default encoding: {}", encoding);
					eventReader = inputFactory.createXMLEventReader(rawDoc.getStream());
				}
			} catch (XMLStreamException e) {
				throw new OkapiBadStepInputException(e);
			}

			boolean collectBeforeFirstFileElem = true;
			int fileCount = 0;
			ArrayList<XMLEvent> elemsBeforeFirstFileElem = new ArrayList<XMLEvent>();
		
			XMLEvent xmlEvent;
			
			while (eventReader.hasNext()) {

				try {
					xmlEvent = eventReader.nextEvent();
				} catch (XMLStreamException e) {
					throw new OkapiBadStepInputException(e);
				}

				/*-intercept start_document to add a linebreak
				 * optionally create a custom StartDocument 
				 * and/or check StartDocuments getCharacterEncodingScheme() */
				if (xmlEvent.getEventType() == XMLEvent.START_DOCUMENT){
					
					elemsBeforeFirstFileElem.add(xmlEvent);
					elemsBeforeFirstFileElem.add(eventFactory.createSpace(lineBreak));
					
					continue;
				}

				//--process the file element--
				if (xmlEvent.getEventType() == XMLEvent.START_ELEMENT && xmlEvent.asStartElement().getName().getLocalPart().equals("file")){

					fileCount++;

					String outputFileUri = outputDir + File.separator + inputFileName 
					    + params.getFileMarker() + String.format("%04d",fileCount) + inputFileExtension;

					collectBeforeFirstFileElem = false;

					writeFilePart(xmlEvent, eventReader, fileCount, outputFileUri, elemsBeforeFirstFileElem,outputFactory, eventFactory, lineBreak, encoding, hasUTF8BOM);

					/* TODO: Send the splitted files down the pipeline once the context functionality is added
					if(fileCount > 1){
						Event e = new Event(EventType.START_BATCH_ITEM);
						tempEvents.add(e);
					}
					
					Event e = new Event(EventType.RAW_DOCUMENT, new RawDocument(new File(outputFileUri).toURI(), encoding, LocaleId.ENGLISH));
					tempEvents.add(e);
					
					e = new Event(EventType.END_BATCH_ITEM);
					tempEvents.add(e);*/
				}

				//--collects any content before the first <file> elem but nothing in between or after following ones--
				if(collectBeforeFirstFileElem){
					elemsBeforeFirstFileElem.add(xmlEvent);
				}
			
				done = true;
			}
			
			return event;
			
			/* TODO: Send the splitted files down the pipeline once the context functionality is added
			  tempEvents.remove(tempEvents.size()-1);
			
			//Event e = new Event(EventType.NO_OP);
			//me.addEvent(e);

			//Event e = new Event(EventType.END_DOCUMENT);
			//me.addEvent(e);

			MultiEvent me = new MultiEvent(tempEvents);

			return new Event(EventType.MULTI_EVENT, me);*/
		}
	}

	private String updateTranslationStatus(final String file) {
		// lets now take the XLIFF with a single file element and update some attributes
		// TODO: is there an easier way than to re-parse the new document?
		final Source s = new Source(file);
		// Create an output document for modification and writing
		final OutputDocument outputFile = new OutputDocument(s);

		// change all translation_type to <iws:status translation_type="manual_translation">
		final List<Element> segment_metadataElements = s.getAllElements("iws:segment-metadata");
		for (final Element segment_metadata : segment_metadataElements) {
			final List<StartTag> statusTags = segment_metadata.getAllStartTags("iws:status");
			// should only be one status tag - but just in case
			for (final StartTag statusTag : statusTags) {
				final Attributes attributes = statusTag.getAttributes();
				final Map<String, String> attributesMap = outputFile.replace(attributes, true);
				//Before, hard-coded: attributesMap.put("translation_type", "manual_translation");
				//Before, hard-coded: attributesMap.put("translation_status", "finished");
				attributesMap.put(XliffSplitterParameters.TRANSLATIONTYPE, params.getTranslationTypeValue());
				attributesMap.put(XliffSplitterParameters.TRANSLATIONSTATUS, params.getTranslationStatusValue());
				attributesMap.remove("target_content");
			}
		}

		return outputFile.toString();
	}
	
	
	
	private void writeFilePart(XMLEvent startFileEvent, XMLEventReader eventReader, int fileCount, String outputFileUri, ArrayList<XMLEvent> elemsBeforeFirstFileElem, XMLOutputFactory outputFactory, XMLEventFactory eventFactory, String lineBreak, String encoding, boolean hasUTF8BOM){
        
        XMLEventWriter eventWriter = null;
        BufferedWriter bw = null;
        
		try {
			
			//--this section is for writing the bom--
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFileUri),encoding));
			Util.writeBOMIfNeeded(bw, hasUTF8BOM, encoding);
			
			eventWriter = outputFactory.createXMLEventWriter(bw);

			boolean insideSegmentMetadata = false;
			
			//--write the collected elems-- 
			for (XMLEvent ev : elemsBeforeFirstFileElem){
				eventWriter.add(ev);
			}

			eventWriter.add(startFileEvent);

			while (eventReader.hasNext()) {
				XMLEvent xmlEvent = eventReader.nextEvent();

				//--remove the target_content attributes and set the update attributes 
				if(params.isUpdateSDLTranslationStatus()){

					if (xmlEvent.getEventType() == XMLEvent.START_ELEMENT && xmlEvent.asStartElement().getName().getLocalPart().equals("segment-metadata") && xmlEvent.asStartElement().getName().getPrefix().equals("iws")){
						insideSegmentMetadata = true;
					}else if (xmlEvent.getEventType() == XMLEvent.END_ELEMENT && xmlEvent.asEndElement().getName().getLocalPart().equals("segment-metadata") && xmlEvent.asEndElement().getName().getPrefix().equals("iws")){
						insideSegmentMetadata = false;
					}else if (xmlEvent.getEventType() == XMLEvent.START_ELEMENT && xmlEvent.asStartElement().getName().getLocalPart().equals("status") && xmlEvent.asStartElement().getName().getPrefix().equals("iws")){

						if(insideSegmentMetadata){

							List<Attribute> modifiedList = new ArrayList<Attribute>();

							StartElement se = xmlEvent.asStartElement();
							Iterator<?> attributes = se.getAttributes();

							while ( attributes.hasNext() ){
								Attribute attr = (Attribute) attributes.next();
								
								if(!attr.getName().getLocalPart().equals("target_content") && !attr.getName().getLocalPart().equals("translation_type") && !attr.getName().getLocalPart().equals("translation_status")){
									modifiedList.add(attr);
								}
							}

							modifiedList.add(eventFactory.createAttribute("translation_type", params.getTranslationTypeValue()));
							modifiedList.add(eventFactory.createAttribute("translation_status", params.getTranslationStatusValue()));

							xmlEvent = eventFactory.createStartElement(se.getName().getPrefix(), se.getName().getNamespaceURI(), se.getName().getLocalPart(), modifiedList.iterator(), se.getNamespaces());
						}
					}
				}
				eventWriter.add(xmlEvent);

				if (xmlEvent.getEventType() == XMLEvent.END_ELEMENT && xmlEvent.asEndElement().getName().getLocalPart().equals("file")){

					eventWriter.add(eventFactory.createSpace(lineBreak));
					eventWriter.add(eventFactory.createEndDocument());
					eventWriter.flush();
					eventWriter.close();

					return;
				}
			}
		}catch (UnsupportedEncodingException e) {
			throw new OkapiUnsupportedEncodingException(e);
		}catch (FileNotFoundException e) {
			throw new OkapiFileNotFoundException(e);
		} catch (XMLStreamException e) {
			throw new OkapiBadStepInputException(e);			
		}finally{
			if (eventWriter != null) {
				try {
					eventWriter.close();
				} catch (XMLStreamException e) {
					throw new OkapiBadStepInputException(e);
				}finally{
					eventWriter = null;	
				}
			}
		}
	}
}
