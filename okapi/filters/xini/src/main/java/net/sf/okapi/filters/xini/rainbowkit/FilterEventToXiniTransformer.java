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

package net.sf.okapi.filters.xini.rainbowkit;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.StartGroup;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextPart;
import net.sf.okapi.filters.xini.InlineCodeTransformer;
import net.sf.okapi.filters.xini.jaxb.Element;
import net.sf.okapi.filters.xini.jaxb.Field;
import net.sf.okapi.filters.xini.jaxb.Fields;
import net.sf.okapi.filters.xini.jaxb.Main;
import net.sf.okapi.filters.xini.jaxb.ObjectFactory;
import net.sf.okapi.filters.xini.jaxb.Page;
import net.sf.okapi.filters.xini.jaxb.Page.Elements;
import net.sf.okapi.filters.xini.jaxb.Seg;
import net.sf.okapi.filters.xini.jaxb.Trans;
import net.sf.okapi.filters.xini.jaxb.Xini;
import net.sf.okapi.steps.wordcount.common.BaseCounter;
import net.sf.okapi.steps.wordcount.common.GMX;

public class FilterEventToXiniTransformer {

	private static final Pattern ALL_WHITESPACE_PATTERN = Pattern.compile("[\\s\\u0009\\u000A\\u000B\\u000C\\u000D\\u0020\\u0085" +
			"\\u00A0\\u1680\\u180E\\u2000\\u2001\\u2002\\u2003\\u2004\\u2005\\u2006\\u2007\\u2008\\u2009\\u200A\\u2028\\u2029" +
			"\\u202F\\u205F\\u3000]+");

	protected static final String FIELD_LABEL_PROPERTY = "field_label";
	private static ObjectFactory objectFactory = new ObjectFactory();
	private Marshaller m;
	private JAXBContext jc;
	private InlineCodeTransformer transformer;

	private Xini xini;
	private Main main;
	private Page currentPage;

	private int currentPageId;
	private int currentElementId;
	private int currentFieldId;
	private Long additionalWordsCount = 0L;
	private List<StartGroup> groups = new ArrayList<StartGroup>();

	public void init() {
		transformer = new InlineCodeTransformer();
		try {

			jc = JAXBContext.newInstance(ObjectFactory.class);
			m = jc.createMarshaller();
			m.setProperty("jaxb.noNamespaceSchemaLocation",
					"http://www.ontram.com/xsd/xini.xsd");

		}
		catch (JAXBException e) {
			throw new OkapiException(e);
		}

		currentPageId = 0;
		currentElementId = 10;
		currentFieldId = 0;

		xini = objectFactory.createXini();
		xini.setSchemaVersion("1.0");
		main = objectFactory.createMain();
		xini.setMain(main);
		xini.setStatisticInfo(objectFactory.createStatisticInfo());
	}

	public void startPage(String name) {
		currentPage = new Page();
		currentPageId += 1;
		currentElementId = 10;
		currentFieldId = 0;

		currentPage.setPageID(currentPageId);
		currentPage.setPageName(name);

		currentPage.setElements(new Elements());
		xini.getMain().getPage().add(currentPage);
	}

	public void transformTextUnit(ITextUnit tu) {

		// Get the source container
		TextContainer textContainer = tu.getSource();

		// Skip non-translatable TextUnits
		if (!tu.isTranslatable()) {
			if (!tu.getTargetLocales().isEmpty()) {
				additionalWordsCount += BaseCounter.getCount(tu, GMX.TotalWordCount);
			}
			return;
		}

		Field field = prepareXiniStructure(tu);
		String fieldLabel = getFieldLabel(tu);
		field.setLabel(fieldLabel);

		int currentSegmentId = 0;
		StringBuilder emptySegsFlags = new StringBuilder();
		Seg xiniSegment = null;
		String whitespaces = null;

		for (TextPart part : textContainer) {

			if (part.isSegment()) {

				Segment okapiSegment = (Segment) part;

				TextFragment textFragment = okapiSegment.getContent();

				if (!textFragmentIsEmpty(textFragment)) {

					xiniSegment = objectFactory.createSeg();
					xiniSegment.setSegID(currentSegmentId);
					field.getSegAndTrans().add(xiniSegment);

					List<Code> codes = textFragment.getCodes();

					if (codes.size() > 0) {
						xiniSegment.getContent().addAll(
								transformer.codesToJAXBForTKit(textFragment.getCodedText(),
										codes));
					}
					else
						xiniSegment.getContent().add(textFragment.getText());

					emptySegsFlags.append("0");
				}
				else if (containsOnlyWhitespace(textFragment.getCodedText())) {
					xiniSegment = objectFactory.createSeg();
					xiniSegment.setSegID(currentSegmentId);
					field.getSegAndTrans().add(xiniSegment);
					xiniSegment.setEmptyTranslation(true);
					xiniSegment.setTrailingSpacer(textFragment.getText());
					emptySegsFlags.append("0");
				}
				else {
					emptySegsFlags.append("1");
				}

				if (whitespaces != null && xiniSegment != null) {
					xiniSegment.setLeadingSpacer(whitespaces);
					whitespaces = null;
				}

				addTransElements(tu, field, currentSegmentId, okapiSegment);
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
		}

		field.setEmptySegmentsFlags(emptySegsFlags.toString());
		currentElementId += 10;

	}

	private String getFieldLabel(ITextUnit tu) {
		String label = null;
		Property labelProperty = tu.getProperty(FIELD_LABEL_PROPERTY);
		if (labelProperty != null) {
			label = labelProperty.getValue();
		}
		else {
			label = getFieldLabelFromGroups();
		}
		return label;
	}

	/**
	 * Pushes a {@link StartGroup} to the groups stack. The groups stack can be used to collect properties from
	 * StartGroup events.
	 * 
	 * @param startGroup
	 */
	public void pushGroupToStack(StartGroup startGroup) {
		groups.add(0, startGroup);
	}

	/**
	 * gets the field label from a surrounding group element. If group elements are nested the first found field element
	 * is returned. <code>null</code> if no field label is found.
	 * 
	 * @return
	 */
	private String getFieldLabelFromGroups() {
		String fieldLabel = null;
		for (StartGroup startGroup : groups) {
			Property fieldLabelProperty = startGroup.getProperty(FIELD_LABEL_PROPERTY);
			if (fieldLabelProperty != null) {
				fieldLabel = fieldLabelProperty.getValue();
				break;
			}
		}
		return fieldLabel;
	}

	/**
	 * pops a group from the groupStack. This method is called by an end group event.
	 */
	public void popGroupFromStack() {
		if (!groups.isEmpty()) {
			groups.remove(0);
		}
	}

	/**
	 * Adds the translations to the XINI field if any are available
	 * 
	 * @param tu The TU containing translation data
	 * @param field The XINI field to add the translations to
	 * @param currentSegmentId The current counter for the segments
	 * @param okapiSegment The source segment from Okapi
	 */
	private void addTransElements(ITextUnit tu, Field field, int currentSegmentId, Segment okapiSegment) {
		Set<LocaleId> targetLocals = tu.getTargetLocales();

		for (LocaleId trgLoc : targetLocals) {
			Segment trgSegment = tu.getTargetSegment(trgLoc,
					okapiSegment.id, false);
			if (trgSegment != null) {
				TextFragment trgTextFragment = trgSegment.getContent();
				if (!trgTextFragment.isEmpty()) {
					Trans xiniTrans = objectFactory.createTrans();
					xiniTrans.setSegID(currentSegmentId);
					xiniTrans.setLanguage(trgLoc.toBCP47());
					field.getSegAndTrans().add(xiniTrans);

					List<Code> codes = trgTextFragment.getCodes();
					if (codes.size() > 0) {
						xiniTrans.getContent().addAll(
								transformer.codesToJAXBForTKit(
										trgTextFragment.getCodedText(), codes));
					}
					else
						xiniTrans.getContent().add(trgTextFragment.getText());
				}
			}
		}
	}

	/**
	 * @param textFragment
	 * @return false if textFragment contains any Placeholders or any characters but whitespaces. Otherwise true.
	 */
	private static boolean textFragmentIsEmpty(final TextFragment textFragment) {
		if (textFragment.isEmpty()) {
			return true;
		}
		else {
			return containsOnlyWhitespace(textFragment.getCodedText());
		}
	}

	/**
	 * @param text
	 * @return true if the parameter string contains only whitespace characters
	 */
	private static boolean containsOnlyWhitespace(final String text) {
		return ALL_WHITESPACE_PATTERN.matcher(text).matches();
	}

	private Field prepareXiniStructure(ITextUnit tu) {
		// Create XML elements
		Element element = objectFactory.createElement();
		Element.ElementContent elementContent = objectFactory
				.createElementElementContent();
		Fields fields = objectFactory.createFields();
		Field field = objectFactory.createField();

		// Connect XML elements
		currentPage.getElements().getElement().add(element);
		element.setElementContent(elementContent);
		elementContent.setFields(fields);
		fields.getField().add(field);

		// Set IDs and add meta-data
		element.setElementID(currentElementId);
		field.setFieldID(currentFieldId);
		field.setExternalID(tu.getId());
		field.setLabel(tu.getName());
		return field;
	}

	public void marshall(OutputStream os) {
		xini.getStatisticInfo().setAditionalWords(additionalWordsCount.floatValue());
		try {
			m.marshal(xini, os);
		}
		catch (JAXBException e) {
			throw new OkapiException(e);
		}
	}

	public long getAdditionalWordsCount() {
		return additionalWordsCount;
	}

}
