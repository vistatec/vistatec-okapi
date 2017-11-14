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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.IdGenerator;
import net.sf.okapi.common.MimeTypeMapper;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.filters.FilterUtil;
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.StartGroup;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextUnit;
import net.sf.okapi.filters.xini.jaxb.Element;
import net.sf.okapi.filters.xini.jaxb.Element.ElementContent;
import net.sf.okapi.filters.xini.jaxb.Field;
import net.sf.okapi.filters.xini.jaxb.INITD;
import net.sf.okapi.filters.xini.jaxb.INITR;
import net.sf.okapi.filters.xini.jaxb.INITable;
import net.sf.okapi.filters.xini.jaxb.Page;
import net.sf.okapi.filters.xini.jaxb.Seg;
import net.sf.okapi.filters.xini.jaxb.TD;
import net.sf.okapi.filters.xini.jaxb.TR;
import net.sf.okapi.filters.xini.jaxb.Table;
import net.sf.okapi.filters.xini.jaxb.TextContent;
import net.sf.okapi.filters.xini.jaxb.Xini;

public class XINIReader {
	private Xini xini;
	private InputStream xiniStream;
	private URI xiniFilename;
	private IdGenerator idGen = new IdGenerator(null);
	private Parameters params;

	private InlineCodeTransformer transformer;

	public XINIReader() {
		transformer = new InlineCodeTransformer();
	}

	public XINIReader(Parameters params) {
		this();
		this.params = params;
	}

	@SuppressWarnings("unchecked")
	public void open(RawDocument input) {

		xiniStream = input.getStream();
		xiniFilename = input.getInputURI();

		// unmarshalling
		try {
			JAXBContext jc = JAXBContext.newInstance(Xini.class.getPackage().getName());
			Unmarshaller u = jc.createUnmarshaller();
			JAXBElement<Xini> jaxbXini = (JAXBElement<Xini>) u.unmarshal(xiniStream);
			xini = jaxbXini.getValue();
		}
		catch (JAXBException e) {
			throw new OkapiException(e);
		}
	}

	public void close() {
		try {
			if (xiniStream != null) {
				xiniStream.close();
				xiniStream = null;
			}
		}
		catch (IOException e) {
			throw new OkapiIOException(e);
		}
	}

	/**
	 * Creates {@link Event}s representing a {@link Xini}
	 *
	 * @return The events generated
	 */
	public LinkedList<Event> getFilterEvents() {
		LinkedList<Event> events = new LinkedList<Event>();

		String xiniName = null;
		if (xiniFilename != null)
			xiniName = xiniFilename.getPath();
		StartDocument startDoc = new StartDocument(xiniName);

		// set Properties
		startDoc.setFilterWriter(new XINIWriter(params));
		startDoc.setType(MimeTypeMapper.XINI_MIME_TYPE);
		startDoc.setMimeType(MimeTypeMapper.XINI_MIME_TYPE);
		startDoc.setMultilingual(false);
		startDoc.setName(xiniName);

		startDoc = processXini(startDoc);

		events.add(new Event(EventType.START_DOCUMENT, startDoc));
		
		// load simplifier rules and send as an event
		if (!Util.isEmpty(params.getSimplifierRules())) {			
			Event cs = FilterUtil.createCodeSimplifierEvent(params.getSimplifierRules());
			events.add(cs);
		}	

		for (Page page : xini.getMain().getPage()) {
			events.addAll(processPage(page));
		}

		Ending ending = new Ending(idGen.createId());
		events.add(new Event(EventType.END_DOCUMENT, ending));

		return events;
	}

	private StartDocument processXini(StartDocument startDoc) {

		if (xini.getSourceLanguage() != null)
			startDoc.setProperty(new Property(
					XINIProperties.SOURCE_LANGUAGE.value(), xini.getSourceLanguage()));

		if (xini.getTargetLanguages() != null){
			String languagesString ="";
			for (String targetLanguage : xini.getTargetLanguages().getLanguage()) {
				languagesString += targetLanguage + ",";
			}
			startDoc.setProperty(new Property(
					XINIProperties.TARGET_LANGUAGES.value(), languagesString));
		}

		return startDoc;
	}

	/**
	 * Creates {@link Event}s representing a XINI {@link Page}
	 */
	private LinkedList<Event> processPage(Page page) {
		LinkedList<Event> events = new LinkedList<Event>();

		StartGroup startGroup = new StartGroup(null, idGen.createId());
		startGroup.setType(GroupType.PAGE.value());

		// set Properties
		int pageId = page.getPageID();
		startGroup.setName(page.getPageName());
		startGroup.setProperty(new Property(XINIProperties.PAGE_ID.value(), pageId + ""));
		startGroup.setProperty(new Property(XINIProperties.CONTEXT_INFORMATION_URL.value(), page.getContextInformationURL()));

		events.add(new Event(EventType.START_GROUP, startGroup));
		
		if (page.getElements() != null) {

			for (Element element : page.getElements().getElement()) {
				events.addAll(processElement(element));
			}
		}

		Ending ending = new Ending(idGen.createId());
		events.add(new Event(EventType.END_GROUP, ending));

		return events;
	}

	/**
	 * Creates {@link Event}s representing a XINI {@link Element}
	 */
	private LinkedList<Event> processElement(Element element) {
		LinkedList<Event> events = new LinkedList<Event>();

		int elementId = element.getElementID();

		StartGroup startGroup = new StartGroup(null, idGen.createId());
		startGroup.setType(GroupType.ELEMENT.value());
		startGroup.setProperty(new Property(
				XINIProperties.ELEMENT_ID.value(), elementId + ""));
		if (element.getCustomerTextID() != null)
			startGroup.setProperty(new Property(
					XINIProperties.ELEMENT_CUSTOMER_TEXT_ID.value(), element.getCustomerTextID()));
		if (element.getSize() != null)
			startGroup.setProperty(new Property(
					XINIProperties.ELEMENT_SIZE.value(), element.getSize() + ""));
		if (element.isAlphaList() != null)
			startGroup.setProperty(new Property(
					XINIProperties.ELEMENT_ALPHA_LIST.value(), element.isAlphaList().toString()));
		if (element.getElementType() != null)
			startGroup.setProperty(new Property(
					XINIProperties.ELEMENT_ELEMENT_TYPE.value(), element.getElementType().value()));
		if (element.getRawSourceBeforeElement() != null)
			startGroup.setProperty(new Property(
					XINIProperties.ELEMENT_RAW_SOURCE_BEFORE_ELEMENT.value(), element.getRawSourceBeforeElement()));
		if (element.getRawSourceAfterElement() != null)
			startGroup.setProperty(new Property(
					XINIProperties.ELEMENT_RAW_SOURCE_AFTER_ELEMENT.value(), element.getRawSourceAfterElement()));
		if (element.getLabel() != null)
			startGroup.setProperty(new Property(
					XINIProperties.ELEMENT_LABEL.value(), element.getLabel()));
		if (element.getStyle() != null)
			startGroup.setProperty(new Property(
					XINIProperties.ELEMENT_STYLE.value(), element.getStyle()));
		events.add(new Event(EventType.START_GROUP, startGroup));

		ElementContent elContent = element.getElementContent();

		events.addAll(processElementContent(elContent));

		Ending ending = new Ending(idGen.createId());
		events.add(new Event(EventType.END_GROUP, ending));

		return events;
	}

	/**
	 * Creates {@link Event}s representing a XINI {@link ElementContent}
	 */
	private LinkedList<Event> processElementContent(ElementContent elContent) {
		LinkedList<Event> events = new LinkedList<Event>();

		if (elContent.getFields() != null) {

			StartGroup startFieldGroup = new StartGroup(null, idGen.createId());
			startFieldGroup.setType(GroupType.FIELDS.value());
			events.add(new Event(EventType.START_GROUP, startFieldGroup));

			for (Field field : elContent.getFields().getField()) {
				events.addAll(processField(field));
			}

			Ending ending = new Ending(idGen.createId());
			events.add(new Event(EventType.END_GROUP, ending));
		}
		else if (elContent.getTable() != null) {

			events.addAll(processTable(elContent.getTable()));
		}
		else if (elContent.getINITable() != null) {

			events.addAll(processINITable(elContent.getINITable()));
		}

		return events;
	}

	/**
	 * Creates {@link Event}s representing a XINI {@link Table}
	 */
	private LinkedList<Event> processTable(Table table) {
		LinkedList<Event> events = new LinkedList<Event>();

		StartGroup startTableGroup = new StartGroup(null, idGen.createId());
		startTableGroup.setType(GroupType.TABLE.value());
		events.add(new Event(EventType.START_GROUP, startTableGroup));

		//int trCount = 0;
		for (TR tr : table.getTR()) {
			//int tdCount = 0;

			StartGroup startTrGroup = new StartGroup(null, idGen.createId());
			startTrGroup.setType(GroupType.TR.value());
			events.add(new Event(EventType.START_GROUP, startTrGroup));

			for (TD td : tr.getTD()) {

				StartGroup startTdGroup = new StartGroup(null, idGen.createId());
				startTdGroup.setType(GroupType.TD.value());

				if (td.getCustomerTextID() != null)
					startTdGroup.setProperty(new Property(
							XINIProperties.TABLE_CUSTOMER_TEXT_ID.value(), td.getCustomerTextID()));
				if (td.getEmptySegmentsFlags() != null)
					startTdGroup.setProperty(new Property(
							XINIProperties.TABLE_EMPTY_SEGMENTS_FLAGS.value(), td.getEmptySegmentsFlags()));
				if (td.getExternalID() != null)
					startTdGroup.setProperty(new Property(
							XINIProperties.TABLE_EXTERNAL_ID.value(), td.getExternalID()));
				if (td.getLabel() != null)
					startTdGroup.setProperty(new Property(
							XINIProperties.TABLE_LABEL.value(), td.getLabel()));
				if (td.isNoContent() != null)
					startTdGroup.setProperty(new Property(
							XINIProperties.TABLE_NO_CONTENT.value(), td.isNoContent().toString()));

				events.add(new Event(EventType.START_GROUP, startTdGroup));

				events.addAll(processSegments(td.getSeg()));

				Ending ending = new Ending(idGen.createId());
				events.add(new Event(EventType.END_GROUP, ending));

				//tdCount++;
			}

			Ending ending = new Ending(idGen.createId());
			events.add(new Event(EventType.END_GROUP, ending));

			//trCount++;
		}

		Ending ending = new Ending(idGen.createId());
		events.add(new Event(EventType.END_GROUP, ending));

		return events;
	}

	/**
	 * Creates {@link Event}s representing a XINI {@link INITable}
	 */
	private LinkedList<Event> processINITable(INITable table) {
		LinkedList<Event> events = new LinkedList<Event>();

		StartGroup startTableGroup = new StartGroup(null, idGen.createId());
		startTableGroup.setType(GroupType.INITABLE.value());
		events.add(new Event(EventType.START_GROUP, startTableGroup));

		//int trCount = 0;
		for (INITR tr : table.getTR()) {
			//int tdCount = 0;

			StartGroup startTrGroup = new StartGroup(null, idGen.createId());
			startTrGroup.setType(GroupType.INITR.value());
			events.add(new Event(EventType.START_GROUP, startTrGroup));

			for (INITD td : tr.getTD()) {

				StartGroup startTdGroup = new StartGroup(null, idGen.createId());
				startTdGroup.setType(GroupType.INITD.value());

				if (td.getCustomerTextID() != null)
					startTdGroup.setProperty(new Property(
							XINIProperties.INITABLE_CUSTOMER_TEXT_ID.value(), td.getCustomerTextID()));
				if (td.getEmptySegmentsFlags() != null)
					startTdGroup.setProperty(new Property(
							XINIProperties.INITABLE_EMPTY_SEGMENTS_FLAGS.value(), td.getEmptySegmentsFlags()));
				if (td.getExternalID() != null)
					startTdGroup.setProperty(new Property(
							XINIProperties.INITABLE_EXTERNAL_ID.value(), td.getExternalID()));
				if (td.getLabel() != null)
					startTdGroup.setProperty(new Property(
							XINIProperties.INITABLE_LABEL.value(), td.getLabel()));
				if (td.isNoContent() != null)
					startTdGroup.setProperty(new Property(
							XINIProperties.INITABLE_NO_CONTENT.value(), td.isNoContent().toString()));

				events.add(new Event(EventType.START_GROUP, startTdGroup));

				events.addAll(processSegments(td.getSeg()));

				Ending ending = new Ending(idGen.createId());
				events.add(new Event(EventType.END_GROUP, ending));

				//tdCount++;
			}

			Ending ending = new Ending(idGen.createId());
			events.add(new Event(EventType.END_GROUP, ending));

			//trCount++;
		}

		Ending ending = new Ending(idGen.createId());
		events.add(new Event(EventType.END_GROUP, ending));

		return events;
	}

	/**
	 * Creates {@link Event}s representing a XINI {@link Field}
	 */
	private LinkedList<Event> processField(Field field) {
		LinkedList<Event> events = new LinkedList<Event>();

		int fieldId = field.getFieldID();

		StartGroup startGroup = new StartGroup(null, idGen.createId());
		startGroup.setType(GroupType.FIELD.value());
		startGroup.setProperty(new Property(
				XINIProperties.FIELD_ID.value(), fieldId + ""));
		if (field.getCustomerTextID() != null)
			startGroup.setProperty(new Property(
					XINIProperties.FIELD_CUSTOMER_TEXT_ID.value(), field.getCustomerTextID()));
		if (field.getEmptySegmentsFlags() != null)
			startGroup.setProperty(new Property(
					XINIProperties.FIELD_EMPTY_SEGMENTS_FLAGS.value(), field.getEmptySegmentsFlags()));
		if (field.getExternalID() != null)
			startGroup.setProperty(new Property(
					XINIProperties.FIELD_EXTERNAL_ID.value(), field.getExternalID()));
		if (field.getLabel() != null)
			startGroup.setProperty(new Property(
					XINIProperties.FIELD_LABEL.value(), field.getLabel()));
		if (field.getRawSourceAfterField() != null)
			startGroup.setProperty(new Property(
					XINIProperties.FIELD_RAW_SOURCE_AFTER_FIELD.value(), field.getRawSourceAfterField()));
		if (field.getRawSourceBeforeField() != null)
			startGroup.setProperty(new Property(
					XINIProperties.FIELD_RAW_SOURCE_BEFORE_FIELD.value(), field.getRawSourceBeforeField()));
		if (field.isNoContent() != null)
			startGroup.setProperty(new Property(
					XINIProperties.FIELD_NO_CONTENT.value(), field.isNoContent().toString()));
		events.add(new Event(EventType.START_GROUP, startGroup));

		events.addAll(processSegments(field.getSeg()));

		Ending ending = new Ending(idGen.createId());
		events.add(new Event(EventType.END_GROUP, ending));

		return events;
	}

	/**
	 * Creates {@link Event}s representing a {@link List} of XINI {@link Seg}s
	 */
	private LinkedList<Event> processSegments(List<Seg> segments) {
		LinkedList<Event> events = new LinkedList<Event>();

		ITextUnit tu = null;
		TextContainer tc = null;

		Integer previousOriginalSegmentId = null;

		for(TextContent fieldContent : segments) {

			if (fieldContent instanceof Seg) {

				Seg seg = (Seg) fieldContent;
				TextFragment tf = processSegment(seg);

				Integer origSegId = seg.getSegmentIDBeforeSegmentation();
				boolean isUnsegmentedXini = origSegId == null;
				boolean hasSameOrigSegIdAsPreviousSegId = origSegId != null && origSegId.equals(previousOriginalSegmentId);

				if (isUnsegmentedXini || !hasSameOrigSegIdAsPreviousSegId) {
					if (tu != null)
						events.add(new Event(EventType.TEXT_UNIT, tu));
					tu = createNewTextUnit(seg);
					tc = new TextContainer(tf);
					tu.setSource(tc);
				}
				else
					tc.getSegments().append(tf);

				previousOriginalSegmentId = origSegId;
			}
		}
		if (tu != null)
			events.add(new Event(EventType.TEXT_UNIT, tu));

		return events;
	}

	private ITextUnit createNewTextUnit(Seg seg) {
		ITextUnit tu;
		tu = new TextUnit(idGen.createId());

		int segId;
		if (seg.getSegmentIDBeforeSegmentation() != null)
			segId = seg.getSegmentIDBeforeSegmentation();
		else
			segId = seg.getSegID();

		tu.setProperty(new Property(XINIProperties.SEGMENT_ID.value(), segId + ""));
		if (seg.isEmptyTranslation() != null)
			tu.setProperty(new Property(XINIProperties.EMPTY_TRANSLATION.value(), seg.isEmptyTranslation().toString()));
		return tu;
	}

	private TextFragment processSegment(Seg xiniSeg) {
		TextFragment fragment = new TextFragment();

		String leadingSpacer = xiniSeg.getLeadingSpacer();
		if(leadingSpacer != null){
			fragment.append(leadingSpacer);
		}

		fragment.insert(-1, transformer.serializeTextPartsForFilter(xiniSeg.getContent()), true);

		if(xiniSeg.getTrailingSpacer() != null){
			fragment.append(xiniSeg.getTrailingSpacer());
		}
		return fragment;
	}
}
