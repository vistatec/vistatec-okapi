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

package net.sf.okapi.common.filters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.IResource;
import net.sf.okapi.common.ISkeleton;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.StringUtil;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.DocumentPart;
import net.sf.okapi.common.resource.EndSubfilter;
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.resource.INameable;
import net.sf.okapi.common.resource.ISegments;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.StartGroup;
import net.sf.okapi.common.resource.StartSubDocument;
import net.sf.okapi.common.resource.StartSubfilter;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.common.skeleton.GenericSkeletonWriter;
import net.sf.okapi.common.skeleton.ISkeletonWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Driver to test filter output.
 */
public class FilterTestDriver {
	private final static Logger LOGGER = LoggerFactory.getLogger(FilterTestDriver.class);

	private boolean showSkeleton = true;
	private int displayLevel = 0;
	private boolean ok;

	/**
	 * Process a RawDocument with the specified filter and return a list of
	 * all events it produces.
	 * @param filter filter to use
	 * @param rd RawDocument to process
	 * @param parameters parameters to use with this filter (may be null)
	 * @return list of events
	 */
	static public ArrayList<Event> getEvents(IFilter filter, RawDocument rd, 
			IParameters parameters) {
		ArrayList<Event> list = new ArrayList<Event>();
		if (parameters != null) {
			filter.setParameters(parameters);
		}
		filter.open(rd);
		while (filter.hasNext()) {
			list.add(filter.next());
		}
		filter.close();
		return list;
	}
	
	/**
	 * Process String content with the specified filter and return a list of
	 * all events it produces.
	 * @param filter filter to use
	 * @param snippet text to process
	 * @param parameters parameters to use with this filter (may be null)
	 * @param srcLocale source locale for the content (may be null)
	 * @param tgtLocale target locale for the content (may be null)
	 * @return list of events
	 */
	static public ArrayList<Event> getEvents(IFilter filter, String snippet, 
			IParameters parameters, LocaleId srcLocale, LocaleId tgtLocale) {
		return getEvents(filter, new RawDocument(snippet, srcLocale, tgtLocale),
					     parameters);
	}
	
	/**
	 * Process String content with the specified filter and return a list of
	 * all events it produces.
	 * @param filter filter to use
	 * @param snippet text to process
	 * @param srcLocale source locale for the content (may be null)
	 * @param tgtLocale target locale for the content (may be null)
	 * @return list of events
	 */
	static public ArrayList<Event> getEvents(IFilter filter, String snippet, 
			LocaleId srcLocale, LocaleId tgtLocale) {
		return getEvents(filter, snippet, null, srcLocale, tgtLocale);
	}
	
	/**
	 * Process String content with the specified filter and return a list of
	 * all events it produces.
	 * @param filter filter to use
	 * @param snippet text to process
	 * @param srcLocale source locale for the content (may be null)
	 * @return list of events
	 */
	static public ArrayList<Event> getEvents(IFilter filter, String snippet, 
			LocaleId srcLocale) {
		return getEvents(filter, snippet, srcLocale, null);
	}

	/**
	 * Return the provided list of events, with any MultiEvents expanded into
	 * multiple individual events. 
	 * @param manual
	 * @param generated
	 * @return
	 */
	static public ArrayList<Event> expandMultiEvents(List<Event> events) {
		ArrayList<Event> expanded = new ArrayList<>();
		for (Event event : events) {
			if (event.isMultiEvent()) {
				for (Event e : event.getMultiEvent()) {
					expanded.add(e);
				}
			}
			expanded.add(event);
		}
		return expanded;
	}

	static public boolean laxCompareEvent(Event manual, Event generated) {
		if (generated.getEventType() != manual.getEventType()) {
			return false;
		}
		IResource mr = manual.getResource();
		IResource gr = generated.getResource();

		if (mr != null && gr != null && mr.getSkeleton() != null && gr.getSkeleton() != null) {
			if (!(mr.getSkeleton().toString().equals(gr.getSkeleton().toString()))) {
				return false;
			}
		}

		switch (generated.getEventType()) {
		case DOCUMENT_PART:
			DocumentPart mdp = (DocumentPart) mr;
			DocumentPart gdp = (DocumentPart) gr;
			if (mdp.isReferent() != gdp.isReferent()) {
				return false;
			}
			if (mdp.isTranslatable() != gdp.isTranslatable()) {
				return false;
			}
			if (!(mdp.getPropertyNames().equals(gdp.getPropertyNames()))) {
				return false;
			}
			for (String propName : gdp.getPropertyNames()) {
				Property gdpProp = gdp.getProperty(propName);
				Property mdpProp = mdp.getProperty(propName);
				if (gdpProp.isReadOnly() != mdpProp.isReadOnly()) {
					return false;
				}
				if (!gdpProp.getValue().equals(mdpProp.getValue())) {
					return false;
				}
			}

			if (!(mdp.getSourcePropertyNames().equals(gdp.getSourcePropertyNames()))) {
				return false;
			}
			for (String propName : gdp.getSourcePropertyNames()) {
				Property gdpProp = gdp.getSourceProperty(propName);
				Property mdpProp = mdp.getSourceProperty(propName);
				if (gdpProp.isReadOnly() != mdpProp.isReadOnly()) {
					return false;
				}
				if (!gdpProp.getValue().equals(mdpProp.getValue())) {
					return false;
				}
			}
			break;

		case TEXT_UNIT:
			ITextUnit mtu = (ITextUnit)mr;
			ITextUnit gtu = (ITextUnit)gr;

			// Resource-level properties
			if (!(mtu.getPropertyNames().equals(gtu.getPropertyNames()))) {
				return false;
			}
			for (String propName : gtu.getPropertyNames()) {
				Property gtuProp = gtu.getProperty(propName);
				Property mtuProp = mtu.getProperty(propName);
				if (gtuProp.isReadOnly() != mtuProp.isReadOnly()) {
					return false;
				}
				if (!gtuProp.getValue().equals(mtuProp.getValue())) {
					return false;
				}
			}

			// Source properties
			if (!(mtu.getSourcePropertyNames().equals(gtu.getSourcePropertyNames()))) {
				return false;
			}
			for (String propName : gtu.getSourcePropertyNames()) {
				Property gtuProp = gtu.getSourceProperty(propName);
				Property mtuProp = mtu.getSourceProperty(propName);
				if (gtuProp.isReadOnly() != mtuProp.isReadOnly()) {
					return false;
				}
				if (!gtuProp.getValue().equals(mtuProp.getValue())) {
					return false;
				}
			}

			String tmp = mtu.getName();
			if (tmp == null) {
				if (gtu.getName() != null) {
					return false;
				}
			} else if (!tmp.equals(gtu.getName())) {
				return false;
			}

			tmp = mtu.getType();
			if (tmp == null) {
				if (gtu.getType() != null) {
					return false;
				}
			} else if (!tmp.equals(gtu.getType())) {
				return false;
			}

			if (mtu.isTranslatable() != gtu.isTranslatable()) {
				return false;
			}
			if (mtu.isReferent() != gtu.isReferent()) {
				return false;
			}
			if (mtu.preserveWhitespaces() != gtu.preserveWhitespaces()) {
				return false;
			}
			if (!(mtu.toString().equals(gtu.toString()))) {
				return false;
			}

			if (mtu.getSource().getUnSegmentedContentCopy().getCodes().size() != gtu.getSource()
					.getUnSegmentedContentCopy().getCodes().size()) {
				return false;
			}
			int i = -1;
			for (Code c : mtu.getSource().getUnSegmentedContentCopy().getCodes()) {
				i++;
				if (c.getType() != null) {
					if (!c.getType().equals(
							gtu.getSource().getUnSegmentedContentCopy().getCode(i).getType())) {
						return false;
					}
				}
			}
			break;
		default:
			break;
		}

		return true;
	}

	static public boolean laxCompareEvents(List<Event> manual, List<Event> generated) {
		if (manual.size() != generated.size()) {
			return false;
		}

		Iterator<Event> manualIt = manual.iterator();
		for (Event ge : generated) {
			Event me = manualIt.next();
			if (!laxCompareEvent(me, ge)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Compares the codes of two text fragments so in-lines codes that have the same IDs have also the same content
	 * (getData()), except for opening/closing cases.
	 * 
	 * @param tf1
	 *            the base fragment.
	 * @param tf2
	 *            the fragment to compare with the base fragment.
	 */
	static public void checkCodeData(TextFragment tf1, TextFragment tf2) {
		List<Code> srcCodes = tf1.getCodes();
		List<Code> trgCodes = tf2.getCodes();
		for (Code srcCode : srcCodes) {
			for (Code trgCode : trgCodes) {
				// Same ID must have the same content, except for open/close
				if (srcCode.getId() == trgCode.getId()) {
					switch (srcCode.getTagType()) {
					case OPENING:
						if (trgCode.getTagType() == TagType.CLOSING)
							break;
						assertEquals(srcCode.getData(), trgCode.getData());
						break;
					case CLOSING:
						if (trgCode.getTagType() == TagType.OPENING)
							break;
						assertEquals(srcCode.getData(), trgCode.getData());
						break;
					default:
						assertEquals(srcCode.getData(), trgCode.getData());
						break;
					}
				}
			}
		}
	}

	static public boolean compareEvent(Event manual,
		Event generated,
		boolean includeSkeleton, boolean ignoreSkelWhitespace, boolean ignoreSegmentation, boolean ignoreFragmentWhitespace)
	{
		if (generated.getEventType() != manual.getEventType()) {
			LOGGER.debug("Event type difference: " + generated.getEventType().toString()
					+ " and " + manual.getEventType().toString());
			return false;
		}

		switch (generated.getEventType()) {
		case DOCUMENT_PART:
			DocumentPart mdp = (DocumentPart) manual.getResource();
			DocumentPart gdp = (DocumentPart) generated.getResource();
			if (!compareIResource(mdp, gdp, includeSkeleton, ignoreSkelWhitespace)) {
				return false;
			}
			if (!compareINameable(mdp, gdp, ignoreSegmentation, ignoreFragmentWhitespace)) {
				return false;
			}
			if (mdp.isReferent() != gdp.isReferent()) {
				LOGGER.debug("isReferent difference");
				return false;
			}
			break;

		case START_GROUP:
			StartGroup sg1 = (StartGroup) manual.getResource();
			StartGroup sg2 = (StartGroup) generated.getResource();
			if (!compareIResource(sg1, sg2, includeSkeleton, ignoreSkelWhitespace)) {
				return false;
			}
			if (!compareINameable(sg1, sg2, ignoreSegmentation, ignoreFragmentWhitespace)) {
				return false;
			}
			if (sg1.isReferent() != sg2.isReferent()) {
				LOGGER.debug("isReferent difference");
				return false;
			}
			break;

		case END_GROUP:
			if (!compareIResource(manual.getResource(), generated.getResource(), includeSkeleton, ignoreSkelWhitespace)) {
				return false;
			}
			break;

		case START_SUBFILTER:
			StartSubfilter ssf1 = (StartSubfilter) manual.getResource();
			StartSubfilter ssf2 = (StartSubfilter) generated.getResource();
			if (!compareIResource(ssf1, ssf2, includeSkeleton, ignoreSkelWhitespace)) {
				return false;
			}
			if (!compareINameable(ssf1, ssf2, ignoreSegmentation, ignoreFragmentWhitespace)) {
				return false;
			}
			if (ssf1.isReferent() != ssf2.isReferent()) {
				LOGGER.debug("isReferent difference");
				return false;
			}
			break;

		case END_SUBFILTER:
			EndSubfilter esf1 = (EndSubfilter) manual.getResource();
			EndSubfilter esf2 = (EndSubfilter) generated.getResource();
			if (!compareIResource(esf1, esf2, includeSkeleton, ignoreSkelWhitespace)) {
				return false;
			}
			break;
		case TEXT_UNIT:
			ITextUnit tu = manual.getTextUnit();
			if (!compareTextUnit(tu, generated.getTextUnit(), ignoreSegmentation, ignoreFragmentWhitespace)) {
				LOGGER.debug("Text unit difference, tu id=" + tu.getId());
				return false;
			}
			break;
		default:
			break;
		}

		return true;
	}

	static public boolean compareEvents (List<Event> list1,
		List<Event> list2)
	{
		return compareEvents(list1, list2, true);
	}
	
	static public boolean compareEvents (List<Event> list1, List<Event> list2,
			boolean includeSkeleton) {
		return compareEvents(list1, list2, includeSkeleton, false, false, false);
	}
	
	static public boolean compareEvents (List<Event> list1, List<Event> list2,
		boolean includeSkeleton, boolean ignoreSkelWhitespace, boolean ignoreSegmentation, boolean ignoreFragmentWhitespace)
	{
		int i = 0;
		Event event1, event2;
		while (i < list1.size()) {
			event1 = list1.get(i);
			if (i >= list2.size()) {
				LOGGER.debug("Less events in second list");
				return false;
			}
			event2 = list2.get(i);
			if (!compareEvent(event1, event2, includeSkeleton, ignoreSkelWhitespace, ignoreSegmentation, ignoreFragmentWhitespace)) {
				return false;
			}
			i++;
		}

		if (list1.size() != list2.size()) {
			LOGGER.debug("Less events in first list");
			return false;
		}

		return true;
	}
	
	static public boolean compareEvents (List<Event> list1,
		List<Event> list2,
		List<Event> subDocEvents,
		boolean includeSkeleton, boolean ignoreSkelWhitespace, boolean ignoreSegmentation, boolean ignoreFragmentWhitespace)
	{
		int i = 0;
		Event event1, event2;
		while (i < list1.size()) {
			event1 = list1.get(i);
			if (i >= list2.size()) {
				LOGGER.debug("Less events in second list");
				return false;
			}
			event2 = list2.get(i);
			if ( !compareEvent(event1, event2, includeSkeleton, ignoreSkelWhitespace, ignoreSegmentation, ignoreFragmentWhitespace) ) {
				Event subDocEvent = subDocEvents.get(i);
				if (subDocEvent != null) {
					StartSubDocument ssd = (StartSubDocument) subDocEvent.getResource();
					if (ssd != null)
						LOGGER.debug("Sub-document: " + ssd.getName());
				}				
				return false;
			}
			i++;
		}

		if (list1.size() != list2.size()) {
			LOGGER.debug("Less events in first list");
			return false;
		}

		return true;
	}

	static public boolean compareEventTypesOnly (List<Event> manual,
		List<Event> generated)
	{
		if (manual.size() != generated.size()) {
			return false;
		}

		Iterator<Event> manualIt = manual.iterator();
		for (Event ge : generated) {
			Event me = manualIt.next();
			if (ge.getEventType() != me.getEventType()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Indicates to this driver to display the skeleton data.
	 * 
	 * @param value
	 *            True to display the skeleton, false to not display the skeleton.
	 */
	public void setShowSkeleton(boolean value) {
		showSkeleton = value;
	}

	/**
	 * Indicates what to display.
	 * 
	 * @param value
	 *            0=display nothing, 1=display TU only, >1=display all.
	 */
	public void setDisplayLevel(int value) {
		displayLevel = value;

	}

	/**
	 * Process the input document. You must have called the setOptions() and open() methods of the filter before calling
	 * this method.
	 * 
	 * @param filter
	 *            Filter to process.
	 * @return False if an error occurred, true if all was OK.
	 */
	@SuppressWarnings("unused")
	public boolean process(IFilter filter) {
		ok = true;
		int startDoc = 0;
		int endDoc = 0;
		int startGroup = 0;
		int endGroup = 0;
		int startSubDoc = 0;
		int endSubDoc = 0;
		int startSubfilter = 0;
		int endSubfilter = 0;

		Event event;
		while (filter.hasNext()) {
			event = filter.next();
			switch (event.getEventType()) {
			case START_DOCUMENT:
				startDoc++;
				checkStartDocument((StartDocument) event.getResource());
				if (displayLevel < 2)
					break;
				LOGGER.trace("---Start Document");
				printSkeleton(event.getResource());
				break;
			case END_DOCUMENT:
				endDoc++;
				if (displayLevel < 2)
					break;
				LOGGER.trace("---End Document");
				printSkeleton(event.getResource());
				break;
			case START_SUBDOCUMENT:
				startSubDoc++;
				if (displayLevel < 2)
					break;
				LOGGER.trace("---Start Sub Document");
				printSkeleton(event.getResource());
				break;
			case END_SUBDOCUMENT:
				endSubDoc++;
				if (displayLevel < 2)
					break;
				LOGGER.trace("---End Sub Document");
				printSkeleton(event.getResource());
				break;
			case START_GROUP:
				startGroup++;
				if (displayLevel < 2)
					break;
				LOGGER.trace("---Start Group");
				printSkeleton(event.getResource());
				break;
			case END_GROUP:
				endGroup++;
				if (displayLevel < 2)
					break;
				LOGGER.trace("---End Group");
				printSkeleton(event.getResource());
				break;
			case TEXT_UNIT:
				ITextUnit tu = event.getTextUnit();
				if (displayLevel < 1)
					break;
				printTU(tu);
				if (displayLevel < 2)
					break;
				printResource(tu);
				printSkeleton(tu);
				break;
			case DOCUMENT_PART:
				if (displayLevel < 2)
					break;
				LOGGER.trace("---Document Part");
				printResource((INameable) event.getResource());
				printSkeleton(event.getResource());
				break;
			case START_SUBFILTER:
				startSubfilter++;
				if (displayLevel < 2)
					break;
				LOGGER.trace("---Start Subfilter");
				printSkeleton(event.getResource());
				break;
			case END_SUBFILTER:
				endSubfilter++;
				if (displayLevel < 2)
					break;
				LOGGER.trace("---End Subfilter");
				printSkeleton(event.getResource());
				break;
			default:
				break;
			}
		}

		if (startDoc != 1) {
			LOGGER.debug(String.format("ERROR: START_DOCUMENT = %d", startDoc));
			ok = false;
		}
		if (endDoc != 1) {
			LOGGER.debug(String.format("ERROR: END_DOCUMENT = %d", endDoc));
			ok = false;
		}
		if (startSubDoc != endSubDoc) {
			LOGGER.debug(String.format("ERROR: START_SUBDOCUMENT=%d, END_SUBDOCUMENT=%d",
					startSubDoc, endSubDoc));
			ok = false;
		}
		if (startGroup != endGroup) {
			LOGGER.trace(String.format("ERROR: START_GROUP=%d, END_GROUP=%d", startGroup,
					endGroup));
			ok = false;
		}
		return ok;
	}

	private void printTU (ITextUnit tu) {
		LOGGER.trace("---Text Unit");
		LOGGER.trace("S=[" + tu.toString() + "]");
		for (LocaleId lang : tu.getTargetLocales()) {
			LOGGER.trace("T(" + lang + ")=[" + tu.getTarget(lang).toString() + "]");
		}
	}

	private void printResource(INameable res) {
		if (res == null) {
			LOGGER.trace("NULL resource.");
			ok = false;
		} else {
			LOGGER.trace("  id='" + res.getId() + "'");
			LOGGER.trace(" name='" + res.getName() + "'");
			LOGGER.trace(" type='" + res.getType() + "'");
			LOGGER.trace(" mimeType='" + res.getMimeType() + "'");
		}
	}

	private void printSkeleton(IResource res) {
		if (!showSkeleton)
			return;
		ISkeleton skel = res.getSkeleton();
		if (skel != null) {
			LOGGER.trace("---");
			LOGGER.trace(skel.toString());
			LOGGER.trace("---");
		}
	}

	private void checkStartDocument(StartDocument startDoc) {
		if (displayLevel < 1)
			return;
		String tmp = startDoc.getEncoding();
		if ((tmp == null) || (tmp.length() == 0)) {
			LOGGER.debug("WARNING: No encoding specified in StartDocument.");
		} else if (displayLevel > 1)
			LOGGER.trace("StartDocument encoding = " + tmp);

		LocaleId locId = startDoc.getLocale();
		if (Util.isNullOrEmpty(locId)) {
			LOGGER.debug("WARNING: No language specified in StartDocument.");
		} else if (displayLevel > 1)
			LOGGER.trace("StartDocument language = " + locId.toString());

		tmp = startDoc.getName();
		if ((tmp == null) || (tmp.length() == 0)) {
			LOGGER.debug("WARNING: No name specified in StartDocument.");
		} else if (displayLevel > 1)
			LOGGER.trace("StartDocument name = " + tmp);

		if (displayLevel < 2)
			return;
		LOGGER.debug("StartDocument MIME type = " + startDoc.getMimeType());
		LOGGER.debug("StartDocument Type = " + startDoc.getType());
	}

	/**
	 * Creates a string output from a list of events.
	 * 
	 * @param list
	 *            The list of events.
	 * @param encoderManager
	 *            the encoder manager.
	 * @param trgLang
	 *            Code of the target (output) language.
	 * @return The generated output string
	 */
	public static String generateOutput(List<Event> list, EncoderManager encoderManager,
			LocaleId trgLang) {
		GenericSkeletonWriter writer = new GenericSkeletonWriter();
		return generateOutput(list, trgLang, writer, encoderManager, false);
	}

	/**
	 * Creates a string output from a list of events and upper-case the content of the target TUs.
	 * 
	 * @param list
	 *            The list of events.
	 * @param encoderManager
	 *            the encoder manager.
	 * @param trgLang
	 *            Code of the target (output) language.
	 * @return The generated output string
	 */
	public static String generateChangedOutput(List<Event> list,
			EncoderManager encoderManager, LocaleId trgLang) {
		GenericSkeletonWriter writer = new GenericSkeletonWriter();
		return generateOutput(list, trgLang, writer, encoderManager, true);
	}

	/**
	 * Creates a string output from a list of events, using a given ISkeletonWriter.
	 * 
	 * @param list
	 *            the list of events.
	 * @param trgLang
	 *            code of the target (output) language.
	 * @param skelWriter
	 *            the ISkeletonWriter to use.
	 * @param encoderManager the encoder manager.
	 * @return The generated output string.
	 */
	public static String generateOutput(List<Event> list, LocaleId trgLang,
			ISkeletonWriter skelWriter, EncoderManager encoderManager) {
		return generateOutput(list, trgLang, skelWriter, encoderManager, false);
	}

	/**
	 * Creates a string output from a list of events, using a given ISkeletonWriter.
	 * 
	 * @param list
	 *            the list of events.
	 * @param trgLang
	 *            code of the target (output) language.
	 * @param skelWriter
	 *            the ISkeletonWriter to use.
	 * @param encoderManager
	 *            the encoder manager.
	 * @param changeTarget
	 *            true to change the content of the target TU.
	 * @return The generated output string.
	 */
	public static String generateOutput(List<Event> list, LocaleId trgLang,
			ISkeletonWriter skelWriter, EncoderManager encoderManager, boolean changeTarget) {
		StringBuilder tmp = new StringBuilder();
		for (Event event : list) {
			switch (event.getEventType()) {
			case START_DOCUMENT:
				tmp.append(skelWriter.processStartDocument(trgLang, "UTF-8", null, encoderManager,
						(StartDocument) event.getResource()));
				break;
			case END_DOCUMENT:
				tmp.append(skelWriter.processEndDocument((Ending) event.getResource()));
				break;
			case START_SUBDOCUMENT:
				tmp.append(skelWriter.processStartSubDocument((StartSubDocument) event
						.getResource()));
				break;
			case END_SUBDOCUMENT:
				tmp.append(skelWriter.processEndSubDocument((Ending) event.getResource()));
				break;
			case TEXT_UNIT:
				ITextUnit tu = event.getTextUnit();
				if (changeTarget) {
					TextContainer tc = tu.createTarget(trgLang, false, IResource.COPY_ALL);
					TextFragment tf = tc.getFirstContent();
					tf.setCodedText(tf.getCodedText().toUpperCase());
				}
				tmp.append(skelWriter.processTextUnit(tu));
				break;
			case DOCUMENT_PART:
				DocumentPart dp = (DocumentPart) event.getResource();
				tmp.append(skelWriter.processDocumentPart(dp));
				break;
			case START_GROUP:
				StartGroup startGroup = (StartGroup) event.getResource();
				tmp.append(skelWriter.processStartGroup(startGroup));
				break;
			case END_GROUP:
				tmp.append(skelWriter.processEndGroup((Ending) event.getResource()));
				break;
			case START_SUBFILTER:
				StartSubfilter startSubfilter = (StartSubfilter) event.getResource();
				tmp.append(skelWriter.processStartSubfilter(startSubfilter));
				break;
			case END_SUBFILTER:
				tmp.append(skelWriter.processEndSubfilter((EndSubfilter) event.getResource()));
				break;
			default:
				break;
			}
		}
		skelWriter.close();
		return tmp.toString();
	}

	public static String generateOutput(IFilter filter, List<Event> events, LocaleId locale, Charset encoding) {
		IFilterWriter fw = filter.createFilterWriter();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        fw.setOutput(baos);
        fw.setOptions(locale, encoding.toString());
        for (Event e : events) {
            fw.handleEvent(e);
        }
        return new String(baos.toByteArray(), encoding);
	}

	public static ITextUnit getTextUnit(IFilter filter, InputDocument doc, String defaultEncoding,
			LocaleId srcLang, LocaleId trgLang, int tuNumber) {
		return getTextUnit(filter, doc, defaultEncoding, srcLang, trgLang, tuNumber, false);
	}

	public static ITextUnit getTextUnitFromInputStream(IFilter filter, InputDocument doc, String defaultEncoding,
			LocaleId srcLang, LocaleId trgLang, int tuNumber) {
		return getTextUnit(filter, doc, defaultEncoding, srcLang, trgLang, tuNumber, true);
	}

	private static ITextUnit getTextUnit(IFilter filter, InputDocument doc, String defaultEncoding,
		LocaleId srcLang, LocaleId trgLang, int tuNumber, boolean inputStream) {
		try {
			// Load parameters if needed
			if (doc.paramFile == null || doc.paramFile.isEmpty()) {
				IParameters params = filter.getParameters();
				if (params != null) {
					// FIXME: shouldn't we take parameters as they are given?
					//params.reset();
				}
			} else {
				String root = Util.getDirectoryName(doc.path);
				IParameters params = filter.getParameters();
				if (params != null) {
					params.load(Util.toURL(root + "/" + doc.paramFile), false);
				}
			}
			// Open the input
			int num = 0;
			if (inputStream) {
				FileInputStream fileInputStream = new FileInputStream(new File(doc.path));
				filter.open(new RawDocument(fileInputStream, defaultEncoding, srcLang,
					trgLang));
			} else {
				filter.open(new RawDocument((new File(doc.path)).toURI(), defaultEncoding, srcLang,
					trgLang));
			}
			// Process the document
			Event event;
			while (filter.hasNext()) {
				event = filter.next();
				if ( event.getEventType() == EventType.TEXT_UNIT ) {
					if ( ++num == tuNumber ) {
						return event.getTextUnit();
					}
				}
			}
		} catch (FileNotFoundException ex) {
			LOGGER.debug("File not found: "+doc.path);
		} finally {
			if (filter != null) {
				filter.close();
			}
		}
		return null;
	}

	public static boolean testStartDocument(IFilter filter, InputDocument doc,
			String defaultEncoding, LocaleId srcLang, LocaleId trgLang) {
		try {
			// Load parameters if needed
			if (doc.paramFile == null || doc.paramFile.isEmpty()) {
				IParameters params = filter.getParameters();
				if (params != null)
					params.reset();
			} else {
				String root = Util.getDirectoryName(doc.path);
				IParameters params = filter.getParameters();
				if (params != null)
					params.load(Util.toURL(root + File.separator + doc.paramFile), false);
			}

			// Open the input
			filter.open(new RawDocument((new File(doc.path)).toURI(), defaultEncoding, srcLang,
					trgLang));
			// Process the document
			Event event;
			while (filter.hasNext()) {
				event = filter.next();
				assertTrue("First event is not a StartDocument event.",
						event.getEventType() == EventType.START_DOCUMENT);
				StartDocument sd = (StartDocument) event.getResource();
				assertNotNull("No StartDocument", sd);
				// Name may be null if not URL input assertNotNull("Name is null", sd.getName());
				assertNotNull("Encoding is null", sd.getEncoding());
				assertNotNull("ID is null", sd.getId());
				assertNotNull("Language is null", sd.getLocale());
				assertNotNull("Linebreak is null", sd.getLineBreak());
				assertNotNull("FilterWriter is null", sd.getFilterWriter());
				assertNotNull("Mime type is null", sd.getMimeType());
				return true;
			}
		} finally {
			if (filter != null)
				filter.close();
		}
		return false;
	}

	/**
	 * Gets the Nth text unit found in the given list of events.
	 * 
	 * @param list
	 *            The list of events
	 * @param tuNumber
	 *            The number of the unit to return: 1 for the first one, 2 for the second, etc.
	 * @return The text unit found, or null.
	 */
	public static ITextUnit getTextUnit (List<Event> list, int tuNumber) {
		int n = 0;
		for (Event event : list) {
			if (event.getEventType() == EventType.TEXT_UNIT) {
				if (++n == tuNumber) {
					return event.getTextUnit();
				}
			}
		}
		return null;
	}

	/**
	 * Filter out and return only the ITextUnits from the provided events.
	 * @param events list of events to filter
	 * @return text units that were present in the filtered events
	 */
	public static List<ITextUnit> filterTextUnits(List<Event> events) {
		List<ITextUnit> tus = new ArrayList<>();
		for (Event e : events) {
			if (e.isTextUnit()) {
				tus.add(e.getTextUnit());
			}
		}
		return tus;
	}

	/**
	 * Return the number of events with the specified type.
	 * @param events
	 * @param type
	 * @return
	 */
	public static int countEventsByType(List<Event> events, EventType type) {
		int count = 0;
		for (Event e : events) {
			if (type == e.getEventType()) {
				count++;
			}
		}
		return count;
	}
	
	/**
	 * Gets the Nth group found in the given list of events.
	 * 
	 * @param list
	 *            The list of events
	 * @param groupNumber
	 *            The number of the group to return: 1 for the first one, 2 for the second, etc.
	 * @return The group found, or null.
	 */
	public static StartGroup getGroup(List<Event> list,
		int groupNumber)
	{
		int n = 0;
		for (Event event : list) {
			if (event.getEventType() == EventType.START_GROUP) {
				if (++n == groupNumber) {
					return (StartGroup) event.getResource();
				}
			}
		}
		return null;
	}

	/**
	 * Gets the start document in the given list of events.
	 * 
	 * @param list
	 *            The list of events
	 * @return The start document found, or null.
	 */
	public static StartDocument getStartDocument(List<Event> list) {
		for (Event event : list) {
			if (event.getEventType() == EventType.START_DOCUMENT) {
				return (StartDocument) event.getResource();
			}
		}
		return null;
	}

	/**
	 * Gets the Nth start sub-document event in a given list of events.
	 * 
	 * @param list
	 *            the list of events.
	 * @param subDocNumber
	 *            the number of the sub-document to return 1 for first, 2 for second, etc.
	 * @return the sub-document found or null.
	 */
	public static StartSubDocument getStartSubDocument(List<Event> list, int subDocNumber) {
		int n = 0;
		for (Event event : list) {
			if (event.getEventType() == EventType.START_SUBDOCUMENT) {
				if (++n == subDocNumber) {
					return (StartSubDocument) event.getResource();
				}
			}
		}
		return null;
	}

	/**
	 * Gets the Nth document part found in the given list of events.
	 * 
	 * @param list
	 *            The list of events
	 * @param tuNumber
	 *            The number of the document part to return: 1 for the first one, 2 for the second, etc.
	 * @return The document part found, or null.
	 */
	public static DocumentPart getDocumentPart(List<Event> list, int dpNumber) {
		int n = 0;
		for (Event event : list) {
			if (event.getEventType() == EventType.DOCUMENT_PART) {
				if (++n == dpNumber) {
					return (DocumentPart) event.getResource();
				}
			}
		}
		return null;
	}

	public static boolean compareTextUnit(ITextUnit tu1, ITextUnit tu2, boolean ignoreSegmentation, boolean ignoreFragmentWhitespace) {
		if (!compareINameable(tu1, tu2, ignoreSegmentation, ignoreFragmentWhitespace)) {
			LOGGER.debug("Difference in INameable");
			return false;
		}
		if (tu1.isReferent() != tu2.isReferent()) {
			LOGGER.debug("isReferent difference");
			return false;
		}
		// TextUnit tests
		if (tu1.preserveWhitespaces() != tu2.preserveWhitespaces()) {
			LOGGER.debug("preserveWhitespaces difference");
			return false;
		}
		if (!compareTextContainer(tu1.getSource(), tu2.getSource(), ignoreSegmentation, ignoreFragmentWhitespace)) {
			LOGGER.debug("TextContainer difference");
			return false;
		}
		// TODO: target, but we have to take re-writing of source as target in account
		return true;
	}

	public static boolean compareIResource(IResource item1,
		IResource item2,
		boolean includeSkeleton, boolean ignoreSkelWhitespace)
	{
		if (item1 == null && item2 == null) {
			return true;
		}
		if (item1 == null || item2 == null) {
			LOGGER.debug("only one IResource defined");
			return false;
		}

		// ID
		String tmp1 = item1.getId();
		String tmp2 = item2.getId();
		if (tmp1 == null && tmp2 == null) {
			return true;
		}
		if (tmp1 == null || tmp2 == null) {
			LOGGER.debug("only one IResource has an ID");
			return false;
		}
		if (!tmp1.equals(tmp2)) {
			LOGGER.debug("IResource ID difference: 1='" + tmp1 + "'\n2='" + tmp2 + "'");
			return false;
		}

		// Skeleton
		if ( !includeSkeleton ) {
			return true;
		}
		
		String id1 = item1.getId();
		
		ISkeleton skl1 = item1.getSkeleton();
		ISkeleton skl2 = item2.getSkeleton();
		if (skl1 == null && skl2 == null) {
			return true;
		}
		if (skl1 == null || skl2 == null) {
			LOGGER.debug("only one IResource has a skeleton ()");
			return false;
		}
		tmp1 = skl1.toString();
		tmp2 = skl2.toString();
		if (tmp1 == null && tmp2 == null) {
			return true;
		}
		if (tmp1 == null || tmp2 == null) {
			LOGGER.debug("only one IResource has a skeleton");
			return false;
		}
		
		if (ignoreSkelWhitespace) {
			if (!normalize(tmp1).equals(normalize(tmp2))) {
				LOGGER.debug(String.format("Skeleton differences in %s: 1='%s'\n2='%s'", id1, normalize(tmp1), normalize(tmp2)));
				return false;
			}
		} else {
			if (!tmp1.equals(tmp2)) {
				LOGGER.debug(String.format("Skeleton differences in %s: 1='%s'\n2='%s'", id1, tmp1, tmp2));
				return false;
			}
		}

		return true;
	}

	public static boolean compareINameable(INameable item1, INameable item2, boolean ignoreSegmentation, boolean ignoreFragmentWhitespace) {
		if (item1 == null && item2 == null) {
			return true;
		}
		if (item1 == null || item2 == null) {
			LOGGER.debug("only one INameable defined");
			return false;
		}

		// Resource-level properties
		Set<String> names1 = item1.getPropertyNames();
		Set<String> names2 = item2.getPropertyNames();
		if (ignoreSegmentation) {
			names1.remove("wassegmented");
			names2.remove("wassegmented");
		}
		
		if (names1.size() != names2.size()) {
			LOGGER.debug("Resource-level property names difference: 1='" + names1 + "'\n2='" + names2 + "'");
			return false;
		}
		for (String name : item1.getPropertyNames()) {
			Property p1 = item1.getProperty(name);
			Property p2 = item2.getProperty(name);
			if (ignoreFragmentWhitespace) {
				p1.setValue(normalize(p1.getValue()));
				p2.setValue(normalize(p2.getValue()));
			}
			
			if (!compareProperty(p1, p2)) {
				return false;
			}
		}

		// Source properties
		names1 = item1.getSourcePropertyNames();
		names2 = item2.getSourcePropertyNames();
		if (ignoreSegmentation) {
			names1.remove("wassegmented");
			names2.remove("wassegmented");
		}
		
		if (names1.size() != names2.size()) {
			LOGGER.debug("Source property names difference: 1='" + names1 + "'\n2='" + names2 + "'");
			return false;
		}
		for (String name : item1.getSourcePropertyNames()) {
			Property p1 = item1.getSourceProperty(name);
			Property p2 = item2.getSourceProperty(name);
			if (!compareProperty(p1, p2)) {
				return false;
			}
		}

		// Target properties
		// TODO: Target properties

		// Name
		String tmp1 = item1.getName();
		String tmp2 = item2.getName();
		if (tmp1 == null) {
			if (tmp2 != null) {
				LOGGER.debug("Name null difference");
				return false;
			}
		} else {
			if (tmp2 == null) {
				LOGGER.debug("Name null difference");
				return false;
			}
			if (!tmp1.equals(tmp2)) {
				LOGGER.debug("Name difference: 1='" + tmp1 + "'\n2='" + tmp2 + "'");
				return false;
			}
		}

		// Type
		tmp1 = item1.getType();
		tmp2 = item2.getType();
		if (tmp1 == null) {
			if (tmp2 != null) {
				LOGGER.debug("Type null difference");
				return false;
			}
		} else {
			if (tmp2 == null) {
				LOGGER.debug("Type null difference");
				return false;
			}
			if (!tmp1.equals(tmp2)) {
				LOGGER.debug("Type difference 1='" + tmp1 + "'\n2='" + tmp2 + "'");
				return false;
			}
		}

		// MIME type
		tmp1 = item1.getMimeType();
		tmp2 = item2.getMimeType();
		if (tmp1 == null) {
			if (tmp2 != null) {
				LOGGER.debug("Mime-type null difference");
				return false;
			}
		} else {
			if (tmp2 == null) {
				LOGGER.debug("Mime-type null difference");
				return false;
			}
			if (!tmp1.equals(tmp2)) {
				LOGGER.debug("Mime-type difference: 1='" + tmp1 + "'\n2='" + tmp2 + "'");
				return false;
			}
		}

		// Is translatable
		if (item1.isTranslatable() != item2.isTranslatable()) {
			LOGGER.debug("isTranslatable difference");
			return false;
		}

		return true;
	}

	public static boolean compareProperty(Property p1, Property p2) {
		if (p1 == null) {
			if (p2 != null) {
				LOGGER.debug("Property name null difference");
				return false;
			}
			return true;
		}
		if (p2 == null) {
			LOGGER.debug("Property name null difference");
			return false;
		}

		if (!p1.getName().equals(p2.getName())) {
			LOGGER.debug("Property name difference");
			return false;
		}
		if (p1.isReadOnly() != p2.isReadOnly()) {
			LOGGER.debug("Property isReadOnly difference");
			return false;
		}
		if (p1.getValue() == null) {
			if (p2.getValue() != null) {
				LOGGER.debug("Property value null difference");
				return false;
			}
			return true;
		}
		
		// we don't care about encoding and locale differences these change often in target files
		// "Parent Scalar Indent" can change for Yaml files
		// "Scalar Type" can change
		if (!"encoding".equals(p1.getName()) && !"language".equals(p1.getName()) && !"Parent Scalar Indent".equals(p1.getName())
				&& !"Scalar Type".equals(p1.getName())) {
			if (!p1.getValue().equals(p2.getValue())) {
				if (!p1.getName().equals("start") && !p1.getName().equals("Quote Char")) { // In double-extraction 'start' can be different
					LOGGER.debug("Property value null difference {}: {} vs {}", p1.getName(), p1.getValue(), p2.getValue());
					return false;
				}
			}
		}
		return true;
	}
	
	public static boolean compareTextContainer(TextContainer t1, TextContainer t2) {
		return compareTextContainer(t1, t2, false, false);
	}

	public static boolean compareTextContainer(TextContainer t1, TextContainer t2, boolean ignoreSegmentation, boolean ignoreFragmentWhitespace) {
		if (t1 == null) {
			if (t2 != null) {
				LOGGER.debug("Text container null difference");
				return false;
			}
			return true;
		}
		if (t2 == null) {
			LOGGER.debug("Text container null difference");
			return false;
		}

		if (!compareTextFragment(t1.getUnSegmentedContentCopy(), t2.getUnSegmentedContentCopy(), ignoreSegmentation, ignoreFragmentWhitespace)) {
			LOGGER.debug("Fragment difference");
			return false;
		}
		
		if (ignoreSegmentation) {
			return true;
		}

		if (t1.hasBeenSegmented()) {
			if (!t2.hasBeenSegmented()) {
				LOGGER.debug("isSegmented difference");
				return false;
			}
			ISegments t1Segments = t1.getSegments();
			ISegments t2Segments = t2.getSegments();
			if (t1Segments.count() != t2Segments.count()) {
				LOGGER.debug("Number of segments difference");
				return false;
			}

			for (Segment seg1 : t1Segments) {
				Segment seg2 = t2Segments.get(seg1.id);
				if (seg2 == null) {
					LOGGER.debug("Segment in t2 not found.");
					return false;
				}
				if (!compareTextFragment(seg1.text, seg2.text, ignoreSegmentation, ignoreFragmentWhitespace)) {
					LOGGER.debug("Text fragment difference");
					return false;
				}
			}
		} else {
			if (t2.hasBeenSegmented()) {
				LOGGER.debug("Segmentation difference");
				return false;
			}
		}
		
//		if (t1.count() != t2.count()) {
//			LOGGER.debug("Number of text parts difference");
//			return false;
//		}

		return true;
	}

	public static boolean compareTextFragment(TextFragment tf1, TextFragment tf2, boolean ignoreSegmentation, boolean ignoreFragmentWhitespace) {
		if (tf1 == null) {
			if (tf2 != null) {
				LOGGER.debug("Fragment null difference");
				return false;
			}
			return true;
		}
		if (tf2 == null) {
			LOGGER.debug("Fragment null difference");
			return false;
		}

		List<Code> codes1 = tf1.getCodes();
		List<Code> codes2 = tf2.getCodes();
		if (codes1.size() != codes2.size()) {
			LOGGER.debug("Number of codes difference");
			LOGGER.debug("original codes=" + codes1.toString());
			LOGGER.debug("     new codes=" + codes2.toString());
			return false;
		}
		for (int i = 0; i < codes1.size(); i++) {
			Code code1 = codes1.get(i);
			Code code2 = codes2.get(i);
			
			// FIXME: apparently segmentation can change code ids
			// for example TXML segmented roundtrip fails here
			if (!ignoreSegmentation) {
				if (code1.getId() != code2.getId()) {
					LOGGER.debug("Code ID difference");
					return false;
				}
			}
			// Data
			String tmp1 = code1.getData();
			String tmp2 = code2.getData();
			if (tmp1 == null) {
				if (tmp2 != null) {
					LOGGER.debug("Data null difference");
					return false;
				}
			} else {
				if (tmp2 == null) {
					LOGGER.debug("Data null difference");
					return false;
				}
				
				if (ignoreFragmentWhitespace) {
					if (!normalize(tmp1).equals(normalize(tmp2))) {
						LOGGER.debug("Data difference: 1=[" + tmp1 + "] and 2=[" + tmp2 + "]");					
						return false;
					}
				} else {
					if (!tmp1.equals(tmp2)) {
						LOGGER.debug("Data difference: 1=[" + tmp1 + "] and 2=[" + tmp2 + "]");					
						return false;
					}			
				}
			}
			// Outer data
			tmp1 = code1.getOuterData();
			tmp2 = code2.getOuterData();
			if (tmp1 == null) {
				if (tmp2 != null) {
					LOGGER.debug("Outer data null difference");
					return false;
				}
			} else {
				if (tmp2 == null) {
					LOGGER.debug("Outer data null difference");
					return false;
				}
				if (ignoreFragmentWhitespace) {
					if (!normalize(tmp1).equals(normalize(tmp2))) {
						LOGGER.debug("Outer data difference");					
						return false;
					}
				} else {
					if (!tmp1.equals(tmp2)) {
						LOGGER.debug("Outer data difference");					
						return false;
					}			
				}
			}
			// Type
			tmp1 = code1.getType();
			tmp2 = code2.getType();
			if (tmp1 == null) {
				if (tmp2 != null) {
					LOGGER.debug("Type null difference");
					return false;
				}
			} else {
				if (tmp2 == null) {
					LOGGER.debug("Type null difference");
					return false;
				}
				if (!tmp1.equals(tmp2)) {
					LOGGER.debug("Type difference 1='" + tmp1 + "'\n2='" + tmp2 + "'");
					return false;
				}
			}
			// Tag type
			if (code1.getTagType() != code2.getTagType()) {
				LOGGER.debug("Tag-type difference");
				return false;
			}
			if (code1.hasReference() != code2.hasReference()) {
				LOGGER.debug("hasReference difference");
				return false;
			}
			if (code1.isCloneable() != code2.isCloneable()) {
				LOGGER.debug("isCloenable difference");
				return false;
			}
			if (code1.isDeleteable() != code2.isDeleteable()) {
				LOGGER.debug("isDeleteable difference");
				return false;
			}
			if (code1.hasAnnotation() != code2.hasAnnotation()) {
				LOGGER.debug("annotation difference");
				return false;
			}
			// TODO: compare annotations
		}
		
		if (ignoreFragmentWhitespace) {
			// Coded text
			if (!normalize(tf1.getCodedText()).equals(normalize(tf2.getCodedText()))) {
				LOGGER.debug("Coded text difference:\n1=\"" + tf1.getCodedText() + "\"\n2=\""
						+ tf2.getCodedText() + "\"");
				return false;
			}
		} else {
			// Coded text
			if (!tf1.getCodedText().equals(tf2.getCodedText())) {
				LOGGER.debug("Coded text difference:\n1=\"" + tf1.getCodedText() + "\"\n2=\""
						+ tf2.getCodedText() + "\"");
				return false;
			}

		}
		return true;
	}
	
	/*
	 * Remove all whitespace, including newlines.
	 * Also remove xml:space=\"preserve\" and <w:r><w:t> as some
	 * filter writers (openxml) introduce these in the
	 * inline codes.
	 */
	public static String normalize(String string) {
		String res = StringUtil.normalizeLineBreaks(string);
		res = res.replaceAll("\\s", "");
		res = res.replaceAll("\\n", "");		
		res = res.replaceAll("xml:space=\"preserve\"", "");
		res = res.replaceAll("<w:r>|<w:t>", "");	
		res = res.replaceAll("</w:t>|</w:r>", "");	       
		return res;
	}
	
	/**
	 * Remove all empty TextUnits. Makes comparison easier for filters
	 * that generate empty TextUnits.
	 * @param full events
	 * @return events without empty TextUnits
	 */
	static public ArrayList<Event> removeEmptyTextUnits(List<Event> events) {
		ArrayList<Event> list = new ArrayList<Event>();
		for (Event event : events) {
			if (event.isTextUnit()) {
				ITextUnit tu = event.getTextUnit();
				if (tu.isEmpty()) {
					continue;
				}
			}
			
			list.add(event);
		}
		
		return list;
	}
	
	static public ArrayList<Event> getTextUnitEvents(IFilter filter, RawDocument rd) {
		ArrayList<Event> list = new ArrayList<Event>();
		try {
			filter.open(rd);
			while (filter.hasNext()) {
				Event e = filter.next();
				if (e.isTextUnit()) {
					list.add(e);
				}			
			}
		} finally {
			filter.close();
		}
		return list;
	}
}
