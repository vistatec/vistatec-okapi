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

package net.sf.okapi.filters.xini;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.exceptions.OkapiBadFilterParametersException;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.StartGroup;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.common.resource.TextPart;
import net.sf.okapi.common.skeleton.ISkeletonWriter;
import net.sf.okapi.filters.xini.jaxb.Element;
import net.sf.okapi.filters.xini.jaxb.ElementType;
import net.sf.okapi.filters.xini.jaxb.Field;
import net.sf.okapi.filters.xini.jaxb.Fields;
import net.sf.okapi.filters.xini.jaxb.INITD;
import net.sf.okapi.filters.xini.jaxb.INITR;
import net.sf.okapi.filters.xini.jaxb.INITable;
import net.sf.okapi.filters.xini.jaxb.Main;
import net.sf.okapi.filters.xini.jaxb.ObjectFactory;
import net.sf.okapi.filters.xini.jaxb.Page;
import net.sf.okapi.filters.xini.jaxb.Seg;
import net.sf.okapi.filters.xini.jaxb.TD;
import net.sf.okapi.filters.xini.jaxb.TR;
import net.sf.okapi.filters.xini.jaxb.Table;
import net.sf.okapi.filters.xini.jaxb.TargetLanguages;
import net.sf.okapi.filters.xini.jaxb.TextContent;
import net.sf.okapi.filters.xini.jaxb.Xini;

public class XINIWriter implements IFilterWriter {

	private EncoderManager encodingManager;
	private IParameters params;
	private ObjectFactory objectFactory = new ObjectFactory();
	private String outputPath;
	private InlineCodeTransformer transformer;

	private Xini xini;
	private Page currentPage;
	private Element currentElement;
	private Fields currentFields;
	private Field currentField;
	private Table currentTable;
	private TR currentTr;
	private TD currentTd;
	private INITable currentIniTable;
	private INITR currentIniTr;
	private INITD currentIniTd;
	private int currentSegmentId;
    private OutputStream outputStream;    

	public XINIWriter() {
		transformer = new InlineCodeTransformer();
	}

	public XINIWriter(IParameters params) {
		this();
		this.params = params;
	}

	@Override
	public void cancel() {
	}

	@Override
	public void close() {
	}

	@Override
	public String getName() {
		return "XINIWriter";
	}

	@Override
	public void setOptions(LocaleId locale, String defaultEncoding) {
	}

	@Override
	public void setOutput(String path) {
		outputPath = path;
	}

	@Override
	public void setOutput(OutputStream output) {
	    outputStream = output;
	}

	@Override
	public Event handleEvent(Event event) {
		switch ( event.getEventType() ) {
		case START_DOCUMENT:
			processStartDocument((StartDocument)event.getResource());
			break;
		case END_DOCUMENT:
			processEndDocument();
			break;
		case START_GROUP:
		case START_SUBFILTER:
			processStartGroup((StartGroup)event.getResource());
			break;
		case TEXT_UNIT:
		case END_SUBFILTER:
			processTextUnit(event.getTextUnit());
			break;
		default:
			break;
		}
		return event;
	}

	private void processStartGroup(StartGroup startGroup) {
		String type = startGroup.getType();
		GroupType gt = GroupType.fromValue(type);
		currentSegmentId = 0;

		switch (gt) {
		case PAGE:
			Page newPage = objectFactory.createPage();
			Property pageIdProperty = startGroup.getProperty(XINIProperties.PAGE_ID.value());
			Property contextInformationUrlProperty = startGroup.getProperty(XINIProperties.CONTEXT_INFORMATION_URL.value());
			int pageId = Integer.valueOf(pageIdProperty.getValue()).intValue();
			String contextInformationUrl = contextInformationUrlProperty.getValue();
			newPage.setPageID(pageId);
			newPage.setPageName(startGroup.getName());
			newPage.setContextInformationURL(contextInformationUrl);
			xini.getMain().getPage().add(newPage);
			currentPage = newPage;
			break;
		case ELEMENT:
			if(currentPage.getElements() == null) {
				currentPage.setElements(new Page.Elements());
			}
			Element newElement = objectFactory.createElement();
			addGroupPropertiesToElement(newElement, startGroup);
			currentPage.getElements().getElement().add(newElement);
			currentElement = newElement;
			break;
		case FIELDS:
			currentElement.setElementContent(new Element.ElementContent());
			Fields newFields = objectFactory.createFields();
			currentElement.getElementContent().setFields(newFields);
			currentFields = newFields;
			break;
		case FIELD:
			Field newField = objectFactory.createField();
			addGroupPropertiesToField(newField, startGroup);
			currentFields.getField().add(newField);
			currentField = newField;
			currentIniTd = null;
			currentTd = null;
			break;
		case TABLE:
			currentElement.setElementContent(new Element.ElementContent());
			Table newTable = objectFactory.createTable();
			currentElement.getElementContent().setTable(newTable);
			currentTable = newTable;
			break;
		case TR:
			TR newTr = objectFactory.createTR();
			currentTable.getTR().add(newTr);
			currentTr = newTr;
			break;
		case TD:
			TD newTd = objectFactory.createTD();
			addGroupPropertiesToTD(newTd, startGroup);
			currentTr.getTD().add(newTd);
			currentTd = newTd;
			currentField = null;
			currentIniTd = null;
			break;
		case INITABLE:
			currentElement.setElementContent(new Element.ElementContent());
			INITable newIniTable = objectFactory.createINITable();
			currentElement.getElementContent().setINITable(newIniTable);
			currentIniTable = newIniTable;
			break;
		case INITR:
			INITR newIniTr = objectFactory.createINITR();
			currentIniTable.getTR().add(newIniTr);
			currentIniTr = newIniTr;
			break;
		case INITD:
			INITD newIniTd = objectFactory.createINITD();
			addGroupPropertiesToINITD(newIniTd, startGroup);
			currentIniTr.getTD().add(newIniTd);
			currentIniTd = newIniTd;
			currentField = null;
			currentTd = null;
			break;
		}
	}

	private void addGroupPropertiesToTD(TD newTd, StartGroup startGroup) {

		for (String propertyName : startGroup.getPropertyNames()) {
			XINIProperties propertyType = XINIProperties.fromValue(propertyName);

			if (propertyType != null) {
				String propertyValue = startGroup.getProperty(propertyName).getValue();
				switch(propertyType) {
				case TABLE_CUSTOMER_TEXT_ID:
					newTd.setCustomerTextID(propertyValue);
					break;
				case TABLE_EMPTY_SEGMENTS_FLAGS:
					newTd.setEmptySegmentsFlags(propertyValue);
					break;
				case TABLE_EXTERNAL_ID:
					newTd.setExternalID(propertyValue);
					break;
				case TABLE_LABEL:
					newTd.setLabel(propertyValue);
					break;
				case TABLE_NO_CONTENT:
					newTd.setNoContent(Boolean.valueOf(propertyValue));
					break;
				default:
					break;
				}
			}
		}
	}

	private void addGroupPropertiesToINITD(INITD newIniTd, StartGroup startGroup) {

		for (String propertyName : startGroup.getPropertyNames()) {
			XINIProperties propertyType = XINIProperties.fromValue(propertyName);

			if (propertyType != null) {
				String propertyValue = startGroup.getProperty(propertyName).getValue();
				switch(propertyType) {
				case INITABLE_CUSTOMER_TEXT_ID:
					newIniTd.setCustomerTextID(propertyValue);
					break;
				case INITABLE_EMPTY_SEGMENTS_FLAGS:
					newIniTd.setEmptySegmentsFlags(propertyValue);
					break;
				case INITABLE_EXTERNAL_ID:
					newIniTd.setExternalID(propertyValue);
					break;
				case INITABLE_LABEL:
					newIniTd.setLabel(propertyValue);
					break;
				case INITABLE_NO_CONTENT:
					newIniTd.setNoContent(Boolean.valueOf(propertyValue));
					break;
				default:
					break;
				}
			}
		}
	}

	private void addGroupPropertiesToField(Field newField, StartGroup startGroup) {

		for (String propertyName : startGroup.getPropertyNames()) {
			XINIProperties propertyType = XINIProperties.fromValue(propertyName);

			if (propertyType != null) {
				String propertyValue = startGroup.getProperty(propertyName).getValue();
				switch(propertyType) {
				case FIELD_ID:
					newField.setFieldID(Integer.valueOf(propertyValue).intValue());
					break;
				case FIELD_CUSTOMER_TEXT_ID:
					newField.setCustomerTextID(propertyValue);
					break;
				case FIELD_EMPTY_SEGMENTS_FLAGS:
					newField.setEmptySegmentsFlags(propertyValue);
					break;
				case FIELD_EXTERNAL_ID:
					newField.setExternalID(propertyValue);
					break;
				case FIELD_LABEL:
					newField.setLabel(propertyValue);
					break;
				case FIELD_RAW_SOURCE_BEFORE_FIELD:
					newField.setRawSourceBeforeField(propertyValue);
					break;
				case FIELD_RAW_SOURCE_AFTER_FIELD:
					newField.setRawSourceAfterField(propertyValue);
					break;
				case FIELD_NO_CONTENT:
					newField.setNoContent(Boolean.valueOf(propertyValue));
					break;
				default:
					break;
				}
			}
		}
	}

	private void addGroupPropertiesToElement(Element newElement, StartGroup startGroup) {

		for (String propertyName : startGroup.getPropertyNames()) {
			XINIProperties propertyType = XINIProperties.fromValue(propertyName);

			if (propertyType != null) {
				String propertyValue = startGroup.getProperty(propertyName).getValue();
				switch(propertyType) {
				case ELEMENT_ID:
					newElement.setElementID(Integer.valueOf(propertyValue).intValue());
					break;
				case ELEMENT_CUSTOMER_TEXT_ID:
					newElement.setCustomerTextID(propertyValue);
					break;
				case ELEMENT_SIZE:
					newElement.setSize(Integer.valueOf(propertyValue));
					break;
				case ELEMENT_ALPHA_LIST:
					newElement.setAlphaList(Boolean.valueOf(propertyValue));
					break;
				case ELEMENT_ELEMENT_TYPE:
					newElement.setElementType(ElementType.fromValue(propertyValue));
					break;
				case ELEMENT_RAW_SOURCE_BEFORE_ELEMENT:
					newElement.setRawSourceBeforeElement(propertyValue);
					break;
				case ELEMENT_RAW_SOURCE_AFTER_ELEMENT:
					newElement.setRawSourceAfterElement(propertyValue);
					break;
				case ELEMENT_LABEL:
					newElement.setLabel(propertyValue);
					break;
				case ELEMENT_STYLE:
					newElement.setStyle(propertyValue);
					break;
				default:
					break;
				}
			}
		}
	}

	private void processTextUnit(ITextUnit tu) {

		List<TextContent> segList = null;
		if (currentField != null)
			segList = currentField.getSegAndTrans();
		else if (currentTd != null)
			segList = currentTd.getSegAndTrans();
		else if (currentIniTd != null)
			segList = currentIniTd.getSegAndTrans();

		Property segIdProperty = tu.getProperty(XINIProperties.SEGMENT_ID.value());
		Integer originalSegmentId = Integer.valueOf(segIdProperty.getValue());

		if (params.getBoolean(Parameters.USE_OKAPI_SEGMENTATION)) {
			segList.addAll(createXINISegments(tu, originalSegmentId));
		}
		else {
			segList.add(createSingleXINISegment(tu, originalSegmentId));
		}
	}

	private TextContent createSingleXINISegment(ITextUnit tu, Integer originalSegmentId) {
		Seg xiniSegment = objectFactory.createSeg();
		xiniSegment.setSegID(originalSegmentId);

		TextContainer textContainer = tu.getSource();
		for (TextPart part : textContainer) {

			if (part.isSegment()) {

				Segment okapiSegment = (Segment)part;

				TextFragment textFragment = okapiSegment.getContent();

				addSegmentContentToXiniSeg(xiniSegment, textFragment);
			}
			else {
				String whitespaceContent = part.getContent().getText();
				xiniSegment.getContent().add(whitespaceContent);
			}
		}
		return xiniSegment;
	}

	private List<TextContent> createXINISegments(ITextUnit tu, Integer originalSegmentId) {
		List<TextContent> newSegments = new ArrayList<TextContent>();

		Seg xiniSegment = null;
		String whitespaces = null;
		//int currentTextPartIndex = 0;
		TextContainer textContainer = tu.getSource();
		
		undoSegmentationIfCodeEndsInOtherSegment(textContainer);
		
		for (TextPart part : textContainer) {

			if (part.isSegment()) {
				Segment okapiSegment = (Segment)part;
				TextFragment textFragment = okapiSegment.getContent();

				xiniSegment = objectFactory.createSeg();
				xiniSegment.setSegID(currentSegmentId);
				xiniSegment.setSegmentIDBeforeSegmentation(originalSegmentId);

				Property segEmptyTranslationProperty = tu.getProperty(XINIProperties.EMPTY_TRANSLATION.value());
				if (segEmptyTranslationProperty != null){
					Boolean emptyTranslation = Boolean.valueOf(segEmptyTranslationProperty.getValue());
					xiniSegment.setEmptyTranslation(emptyTranslation);
				}

				addSegmentContentToXiniSeg(xiniSegment, textFragment);

				if (whitespaces != null && xiniSegment != null) {
					xiniSegment.setLeadingSpacer(whitespaces);
					whitespaces = null;
				}
				
				newSegments.add(xiniSegment);
				currentSegmentId++;
			}
			else {
				// save whitespaces in previous segment, if there is one. Save it in next segment otherwise
				String whitespacePart = part.getContent().getText();
				if (xiniSegment == null) {
					// for whitespaces before the first segment
					whitespaces = whitespacePart;
				}
				else {
					// for whitespaces between segments: save in previous segment
					xiniSegment.setTrailingSpacer(whitespacePart);
				}
			}
			//currentTextPartIndex++;
		}
		return newSegments;
	}

	private void addSegmentContentToXiniSeg(Seg xiniSegment, TextFragment textFragment) {
		List<Code> codes = textFragment.getCodes();
		
		ArrayList<Serializable> newSerializedContent = new ArrayList<Serializable>();

		if (codes.size() > 0){
			newSerializedContent.addAll(
					transformer.codesToJAXBForFilter(textFragment.getCodedText(), codes));
		}
		else {
			newSerializedContent.add(textFragment.getText());
		}
		
		// Concatenate with last content element, if it was a string, too
		// The marshaller adds a whitespace between two strings otherwise
		Serializable lastElementInSeg = null;
		
		if (xiniSegment.getContent().size() > 0)
			lastElementInSeg = xiniSegment.getContent().get(xiniSegment.getContent().size() - 1);
		
		for (Serializable newPart : newSerializedContent) {
			if (newPart instanceof String && lastElementInSeg != null && lastElementInSeg instanceof String) {
				newPart = (String)lastElementInSeg + newPart;
				xiniSegment.getContent().remove(lastElementInSeg);
			}
			
			xiniSegment.getContent().add(newPart);
			
			lastElementInSeg = newPart;
		}
	}
	
	private void undoSegmentationIfCodeEndsInOtherSegment(TextContainer textContainer) {
		undoSegmentationIfCodeEndsInOtherSegment(textContainer, 0);
	}

	private void undoSegmentationIfCodeEndsInOtherSegment(TextContainer textContainer, int textPartStartIndex) {
		
		for (int textPartIndex = textPartStartIndex; textPartIndex < textContainer.count(); textPartIndex++) {
			
			int mergeTo = textPartIndex;
			TextPart part = textContainer.get(textPartIndex);
			TextFragment textFragment = part.getContent();
			
			for (Code code : textFragment.getCodes()) {
				
				// only look at opening codes and check where they end
				if (code.getTagType() == TagType.OPENING) {
					
					int codeId = code.getId();
					if(!isClosingCodeInTextPart(part, codeId)) {
						
						if (textPartIndex+1 >= textContainer.count())
							// An opening code in the last text part has no matching closing code in the same text part
							break;
						
						// Code ends somewhere else
						// Check following text parts for end code
						for (int i = textPartIndex+1; i < textContainer.count(); i++) {
							
							TextPart followingTp = textContainer.get(i);
							if(isClosingCodeInTextPart(followingTp, codeId)) {
								if (i > mergeTo)
									mergeTo = i;
								break;
							}
						}
					}
				}
			}

			if (mergeTo > textPartIndex) {
				this.joinTextContainerWithNextParts(textContainer, textPartIndex, mergeTo - textPartIndex);

				// problem fixed here: codes from the text parts that are merged into the current text part are not checked
				undoSegmentationIfCodeEndsInOtherSegment(textContainer, textPartIndex);
				// all following text parts are merged by the recursive call
				return;
			}
		}
	}
	
	/*
	 * Join text fragments without changing the code IDs.
	 */
	private void joinTextContainerWithNextParts(TextContainer tc, int partIndex, int partCount) {
		if ( tc.count() == 1 ) {
			return; // Nothing to do
		}
		
		TextFragment tf = tc.get(partIndex).getContent();
		int max = (tc.count()-partIndex)-1;
		if (( partCount == -1 ) || ( partCount > max )) {
			partCount = max;
		}
		int i = 0;
		while ( i < partCount ) {
			tf.insert(-1, tc.get(partIndex+1).getContent(), true);
			tc.remove(partIndex+1);
			i++;
		}
	}

	private boolean isClosingCodeInTextPart(TextPart tp, int openingCodeId) {
		TextFragment tf = tp.getContent();
		if(tf.hasCode()){
			List<Code> codeList = tf.getCodes();
			for (Code code : codeList) {
				if(code.getTagType().equals(TagType.CLOSING) && code.getId() == openingCodeId){
					return true;
				}
			}
		}
		return false;
	}

	private void processStartDocument(StartDocument startDoc) {
		xini = objectFactory.createXini();
		addStartDocumentPropertiesToXini(xini, startDoc);
		xini.setSchemaVersion("1.0");
		Main main = objectFactory.createMain();
		xini.setMain(main);
	}

	private void addStartDocumentPropertiesToXini(Xini xini,
			StartDocument startDoc) {

		for (String propertyName : startDoc.getPropertyNames()) {
			XINIProperties propertyType = XINIProperties.fromValue(propertyName);

			if (propertyType != null) {
				String propertyValue = startDoc.getProperty(propertyName).getValue();


				switch(propertyType) {
				case SOURCE_LANGUAGE:
					xini.setSourceLanguage(propertyValue);
					break;
				case TARGET_LANGUAGES:
					TargetLanguages targetLanguages = objectFactory.createTargetLanguages();
					String[] languages = propertyValue.split(",");
					for ( int i=0 ; i < languages.length ; i++) {
					 targetLanguages.getLanguage().add(languages[i]);
					}
					xini.setTargetLanguages(targetLanguages);
					break;
				default:
					break;
				}
			}
		}

	}

	private void processEndDocument() {
	    OutputStream os = null;
	    
		try {		    
			JAXBContext jc = JAXBContext.newInstance(ObjectFactory.class);
			Marshaller m = jc.createMarshaller();
			m.setProperty("jaxb.noNamespaceSchemaLocation", "http://www.ontram.com/xsd/xini.xsd");
			if (outputPath != null) {
                os = new FileOutputStream(outputPath);
            } else if (outputStream != null) {
                os = new BufferedOutputStream(outputStream);
            } else {
                throw new OkapiBadFilterParametersException("Output path or stream must be set");
            }
			m.marshal(xini, os);
		}
		catch (PropertyException e) {
			throw new OkapiException(e);
		}
		catch (FileNotFoundException e) {
			throw new OkapiException(e);
		}
		catch (JAXBException e) {
			throw new OkapiException(e);
		}
		finally {
			if (os != null)
				try {
					os.close();
				}
				catch (IOException e) {
					throw new OkapiException(e);
				}
		}
	}

	@Override
	public IParameters getParameters() {
		return params;
	}

	@Override
	public void setParameters(IParameters params) {
		this.params = params;

	}

	@Override
	public EncoderManager getEncoderManager() {
		return encodingManager;
	}

	@Override
	public ISkeletonWriter getSkeletonWriter() {
		return null;
	}

	/**
	 * For the tests
	 *
	 * @return
	 */
	protected Xini getXini() {
		return xini;
	}

}
