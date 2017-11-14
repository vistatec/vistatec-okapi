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
============================================================================*/

package net.sf.okapi.filters.ts;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.Comment;
import javax.xml.stream.events.DTD;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import net.sf.okapi.common.BOMNewlineEncodingDetector;
import net.sf.okapi.common.ClassUtil;
import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.IResource;
import net.sf.okapi.common.IdGenerator;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.MimeTypeMapper;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.exceptions.OkapiBadFilterInputException;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.filters.FilterConfiguration;
import net.sf.okapi.common.filters.FilterUtil;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filters.IFilterConfigurationMapper;
import net.sf.okapi.common.filterwriter.GenericFilterWriter;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.DocumentPart;
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.resource.INameable;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.StartGroup;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.common.resource.TextUnit;
import net.sf.okapi.common.skeleton.GenericSkeleton;
import net.sf.okapi.common.skeleton.GenericSkeletonWriter;
import net.sf.okapi.common.skeleton.ISkeletonWriter;

import org.codehaus.stax2.XMLInputFactory2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@UsingParameters(Parameters.class)
public class TsFilter implements IFilter {

	class TS {

		DocumentLocation currentDocumentLocation = DocumentLocation.TS;
		MessageLocation currentMessageLocation = MessageLocation.RESOURCE;

		TranslationStatus status = TranslationStatus.UNDETERMINED;
		boolean sourceExists = false;
		boolean targetExists = false;
		boolean sourceIsEmpty = true;
		boolean targetIsEmpty = true;
		String elemBeforeTrg = null;
		int numerusFormCount = 0;
		String messageId = null;

		boolean procTransComment = false;

		int obsoletes = 0;
		int approved = 0;
		int unfinished = 0;
		int other = 0;

		private IdGenerator otherId = new IdGenerator(null, "o");
		int tuId = 0;

		Stack<String> contextStack = new Stack<String>();

		public void resetAll() {
			currentDocumentLocation = DocumentLocation.TS;
			currentMessageLocation = MessageLocation.RESOURCE;
			status = TranslationStatus.UNDETERMINED;
			sourceExists = false;
			targetExists = false;
			elemBeforeTrg = null;
			sourceIsEmpty = true;
			targetIsEmpty = true;
			numerusFormCount = 0;
			messageId = null;

			obsoletes = 0;
			approved = 0;
			unfinished = 0;
			other = 0;

			otherId = new IdGenerator(null, "o");
			tuId = 0;

			contextStack.clear();
		}

		TranslationStatus getTranslationStatus() {
			return status;
		}

		boolean isObsolete() {
			return status == TranslationStatus.OBSOLETE;
		}

		boolean isUnfinished() {
			return status == TranslationStatus.UNFINISHED;
		}

		boolean sourceIsEmpty() {
			return sourceIsEmpty;
		}

		boolean sourceIsMissing() {
			return !sourceExists;
		}

		boolean noSource() {
			return sourceIsEmpty() || sourceIsMissing();
		}

		boolean targetIsEmpty() {
			return targetIsEmpty;
		}

		boolean targetIsMissing() {
			return !targetExists;
		}

		boolean missingSourceAndTarget() {
			return !sourceExists && !targetExists;
		}

		boolean missingSourceNotTarget() {
			return !sourceExists && targetExists;
		}

		public void reset() {
			this.status = TranslationStatus.UNDETERMINED;
			sourceExists = false;
			targetExists = false;
			elemBeforeTrg = null;
			sourceIsEmpty = true;
			targetIsEmpty = true;
			numerusFormCount = 0;
			messageId = null;
		}

		boolean isApproved() {
			return status == TranslationStatus.APPROVED;
		}
		
		boolean isNumerus () {
			return numerusFormCount>0;
		}

		public void analyzeMessage() {

			String validBefore = ",source,oldsource,comment,oldcomment,extracomment,translatorcomment,";
			boolean hasContent = false;

			for (XMLEvent event : eventList) {

				if (event.getEventType() == XMLEvent.START_ELEMENT) {

					StartElement startElem = event.asStartElement();
					String startElemName = event.asStartElement().getName()
							.getLocalPart();

					if (startElemName.equals("source")) {
						sourceExists = true;
						hasContent = false;

					} else if (startElemName.equals("translation")) {
						Attribute attr = startElem
								.getAttributeByName(new QName("type"));
						if (attr == null) {
							status = TranslationStatus.APPROVED;
						} else if (attr.getValue().equals("obsolete")) {
							status = TranslationStatus.OBSOLETE;
						} else if (attr.getValue().equals("unfinished")) {
							status = TranslationStatus.UNFINISHED;
						} else {
							status = TranslationStatus.OTHER;
						}
						targetExists = true;
						hasContent = false;

					} else if (startElemName.equals("numerusform")) {
						numerusFormCount++;

					} else if (startElemName.equals("byte")) {
						hasContent = true;
					}
				} else if (event.getEventType() == XMLEvent.END_ELEMENT) {

					EndElement endElem = event.asEndElement();
					String endElemName = endElem.getName().getLocalPart();

					if (endElemName.equals("source")) {
						if (hasContent) {
							sourceIsEmpty = false;
						}
					} else if (endElemName.equals("translation")) {
						if (hasContent) {
							targetIsEmpty = false;
						}
					}

					if (validBefore.contains("," + endElemName + ",")) {
						elemBeforeTrg = endElemName;
					}
				} else if (event.getEventType() == XMLEvent.CHARACTERS) {

					Characters chars = event.asCharacters();
					if (!chars.isWhiteSpace())
						hasContent = true;
				}
			}
		}

		public boolean msgIsObsolete() {
			return status == TranslationStatus.OBSOLETE;
		}
	}

	TS ts = new TS();

	static enum DocumentLocation {
		TS, CONTEXT, MESSAGE
	};

	static enum MessageLocation {
		RESOURCE, SOURCE, TARGET, NUMERUS
	};

	static enum TranslationStatus {
		UNDETERMINED, UNFINISHED, OBSOLETE, APPROVED, OTHER
	};

	Stack<String> elementStack = new Stack<String>();
	ArrayList<XMLEvent> eventList = new ArrayList<XMLEvent>();
	GenericSkeleton skel;
	XMLEventReader eventReader;

	private LocaleId srcLang;
	private LocaleId trgLang;
	private boolean hasNext;
	private String docName;
	private boolean canceled;
	private LinkedList<Event> queue;
	private String lineBreak;
	private String encoding;
	private boolean hasUTF8BOM;
	private Parameters params;
	private EncoderManager encoderManager;
	private String contextName;

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private RawDocument input;

	public TsFilter() {
		params = new Parameters();
	}

	public void cancel() {
		canceled = true;
	}

	public void close() {
		if (input != null) {
			input.close();
		}
		
		try {
			if (eventReader != null) {
				eventReader.close();
				eventReader = null;
				docName = null;
			}
			hasNext = false;
		} catch (XMLStreamException e) {
			throw new OkapiIOException(e);
		}
	}

	public IFilterWriter createFilterWriter() {
		return new GenericFilterWriter(createSkeletonWriter(),
				getEncoderManager());
	}

	public ISkeletonWriter createSkeletonWriter() {
		return new GenericSkeletonWriter();
	}

	public List<FilterConfiguration> getConfigurations() {
		List<FilterConfiguration> list = new ArrayList<FilterConfiguration>();
		list.add(new FilterConfiguration(getName(),
				MimeTypeMapper.TS_MIME_TYPE, getClass().getName(), "TS",
				"Configuration for Qt TS files.", null, ".ts;"));
		return list;
	}

	public EncoderManager getEncoderManager() {
		if (encoderManager == null) {
			encoderManager = new EncoderManager();
			encoderManager.setMapping(MimeTypeMapper.TS_MIME_TYPE,
					"net.sf.okapi.common.encoder.TSEncoder");
		}
		return encoderManager;
	}

	public String getMimeType() {
		return MimeTypeMapper.TS_MIME_TYPE;
	}

	public String getName() {
		return "okf_ts";
	}

	public String getDisplayName() {
		return "TS Filter";
	}

	public Parameters getParameters() {
		return this.params;
	}

	public boolean hasNext() {
		return hasNext;
	}

	public Event next() {
		try {
			// Check for cancellation first
			if (canceled) {
				queue.clear();
				queue.add(new Event(EventType.CANCELED));
				hasNext = false;
			}

			// Parse next if nothing in the queue
			if (queue.isEmpty()) {
				if (!read()) {
					Ending ending = new Ending(ts.otherId.createId());
					ending.setSkeleton(skel);
					queue.add(new Event(EventType.END_DOCUMENT, ending));
				}
			}

			// Return the head of the queue
			if (queue.peek().getEventType() == EventType.END_DOCUMENT) {
				hasNext = false;
			}
			return queue.poll();
		} catch (XMLStreamException e) {
			throw new OkapiIOException(e);
		}
	}

	public void open(RawDocument input) {
		open(input, true);
	}

	public void open(RawDocument input, boolean generateSkeleton) {
		try {

			this.input = input;
			
			canceled = false;

			XMLInputFactory fact = null;
			Class<?> factClass = ClassUtil
					.getClass("com.ctc.wstx.stax.WstxInputFactory");
			fact = (XMLInputFactory) factClass.newInstance();
			// XMLInputFactory2 fact = (XMLInputFactory2)
			// XMLInputFactory2.newInstance();
			// XMLInputFactory fact = XMLInputFactory.newInstance();

			// fact.setProperty(XMLInputFactory.IS_COALESCING, false);
			
			// security concern. Turn off DTD processing
			// https://www.owasp.org/index.php/XML_External_Entity_%28XXE%29_Processing			
			fact.setProperty(XMLInputFactory.SUPPORT_DTD, false);

			// fact.setProperty(XMLInputFactory.IS_COALESCING, true);
			fact.setProperty(XMLInputFactory.IS_COALESCING, false);

			// In TS files, there are many cases where we have whitespace before
			// the root element
			fact.setProperty(XMLInputFactory2.P_REPORT_PROLOG_WHITESPACE, true);

			// Determine encoding based on BOM, if any
			input.setEncoding("UTF-8"); // Default for XML, other should be
										// auto-detected
			BOMNewlineEncodingDetector detector = new BOMNewlineEncodingDetector(
					input.getStream(), input.getEncoding());
			detector.detectBom();

			XMLStreamReader reader;
			if (detector.isAutodetected()) {
				input.setEncoding(detector.getEncoding());
				reader = fact.createXMLStreamReader(input.getStream(),
						detector.getEncoding());
			} else {
				reader = fact.createXMLStreamReader(input.getStream());
			}

			String realEnc = reader.getCharacterEncodingScheme();
			if (realEnc != null)
				encoding = realEnc;
			else
				encoding = input.getEncoding();

			eventReader = fact.createXMLEventReader(reader);

			srcLang = input.getSourceLocale();
			if (srcLang == null)
				throw new NullPointerException("Source language not set.");
			trgLang = input.getTargetLocale();
			if (trgLang == null)
				throw new NullPointerException("Target language not set.");
			hasUTF8BOM = detector.hasUtf8Bom();
			lineBreak = detector.getNewlineType().toString();
			if (input.getInputURI() != null) {
				docName = input.getInputURI().getPath();
			}

			ts.resetAll();
			contextName = null;

			// Set the start event
			hasNext = true;
			queue = new LinkedList<Event>();

			// Compile code finder rules
			if (params.getUseCodeFinder()) {
				params.getCodeFinder().compile();
			}

			StartDocument startDoc = new StartDocument(ts.otherId.createId());
			startDoc.setName(docName);
			startDoc.setEncoding(encoding, hasUTF8BOM);
			startDoc.setLocale(srcLang);
			startDoc.setFilterParameters(getParameters());
			startDoc.setFilterWriter(createFilterWriter());

			startDoc.setType(MimeTypeMapper.TS_MIME_TYPE);
			startDoc.setMimeType(MimeTypeMapper.TS_MIME_TYPE);
			startDoc.setMultilingual(true);
			startDoc.setLineBreak(lineBreak);
			queue.add(new Event(EventType.START_DOCUMENT, startDoc));
			
			// load simplifier rules and send as an event
			if (!Util.isEmpty(((Parameters)getParameters()).getSimplifierRules())) {			
				Event cs = FilterUtil.createCodeSimplifierEvent(((Parameters)getParameters()).getSimplifierRules());
				queue.add(cs);
			}	

			// The XML declaration is not reported by the parser, so we need to
			// create it as a document part when starting
			skel = new GenericSkeleton();
			startDoc.setProperty(new Property(Property.ENCODING, encoding,
					false));
			skel.append("<?xml version=\"1.0\" encoding=\"");
			skel.addValuePlaceholder(startDoc, Property.ENCODING,
					LocaleId.EMPTY);
			// skel.append("\"?>"+lineBreak);
			skel.append("\"?>");
			startDoc.setSkeleton(skel);
		} catch (XMLStreamException e) {
			throw new OkapiIOException("Cannot open XML document.\n"
					+ e.getMessage(), e);
		} catch (InstantiationException e) {
			throw new OkapiIOException("Cannot open XML document.\n"
					+ e.getMessage(), e);
		} catch (IllegalAccessException e) {
			throw new OkapiIOException("Cannot open XML document.\n"
					+ e.getMessage(), e);
		}
	}

	@Override
	public void setFilterConfigurationMapper(IFilterConfigurationMapper fcMapper) {
	}

	@Override
	public void setParameters(IParameters params) {
		this.params = (Parameters) params;
	}

	private boolean read() throws XMLStreamException {

		while (eventReader.hasNext()) {

			XMLEvent event = eventReader.nextEvent();
			eventList.add(event);

			if (event.getEventType() == XMLEvent.START_ELEMENT) {
				StartElement startElem = event.asStartElement();
				String startElemName = startElem.getName().getLocalPart();

				// --thinking the element stack could be used for custom
				// validation at some point
				if (!startElemName.equals("byte")) {
					elementStack.push(startElemName);
					// TODO various validation
				}

				if (tsPartReady(startElemName)) {
					eventList.remove(eventList.size() - 1); // remove the
															// <context> or
															// <message> element
															// and generate TS
															// "part"
					IResource resource = generateTsPart(true);
					eventList.clear();
					eventList.add(event); // remove the <context> or <message>
											// element
					queue.add(new Event(EventType.DOCUMENT_PART, resource));

				} else if (contextPartReady(startElemName)) {
					eventList.remove(eventList.size() - 1); // remove the
															// <context> or
															// <message> element
															// and generate
															// Context "part"
					IResource resource = generateContextPart(true);
					eventList.clear();
					eventList.add(event); // remove the <context> or <message>
											// element and generate Context
											// "part"
					queue.add(new Event(EventType.START_GROUP, resource));
				}

				if (startElemName.equals("name")) {
					readContextName();
				}

				// --are we processing context or message--
				if (startElemName.equals("context")) {
					ts.currentDocumentLocation = DocumentLocation.CONTEXT;
				} else if (startElemName.equals("message")) {
					ts.currentDocumentLocation = DocumentLocation.MESSAGE;
				}

			} else if (event.getEventType() == XMLEvent.END_ELEMENT) {
				EndElement endElem = event.asEndElement();
				String endElemName = endElem.getName().getLocalPart();

				// --thinking the element stack could be used for custom
				// validation at some point
				if (!endElemName.equals("byte")) {
					elementStack.pop();
					// TODO various validation
				}

				if (endElemName.equals("message")) {

					ts.analyzeMessage();

					if (ts.msgIsObsolete() || ts.noSource()) {

						StartElement se = getStartElement("message");
						if (ts.noSource()) {
							logger.warn(
									"Message (Line {} contains no <source>. Message will be ignored.",
									se.getLocation().getLineNumber());
						}

						DocumentPart dp = generateObsoleteTu();
						queue.add(new Event(EventType.DOCUMENT_PART, dp));
						eventList.clear();
						ts.reset();
						skel = new GenericSkeleton();

					} else if (ts.numerusFormCount > 0) {

						generateNumerusFormTu();
						eventList.clear();
						ts.reset();
						skel = new GenericSkeleton();

						return true;

					} else {
						ITextUnit tu = generateTu();
						queue.add(new Event(EventType.TEXT_UNIT, tu));
						eventList.clear();
						ts.reset();
						skel = new GenericSkeleton();

						return true;
					}

				} else if (endElemName.equals("context")) {

					// --Generate Context EndGroup--
					IResource resource = generateContextPart(false);
					eventList.clear();
					queue.add(new Event(EventType.END_GROUP, resource));

				}
			}
		}

		IResource resource = generateTsPart(false);
		queue.add(new Event(EventType.DOCUMENT_PART, resource));
		eventList.clear();
		skel = new GenericSkeleton("");
		return false;
	}

	private void readContextName() throws XMLStreamException {
		while (true) {
			XMLEvent event = eventReader.nextEvent();
			eventList.add(event);
			// Should be the content
			switch (event.getEventType()) {
			case XMLStreamConstants.CHARACTERS:
				contextName = event.asCharacters().getData();
				break;
			case XMLStreamConstants.SPACE:
				break;
			default:
				return;
			}
		}
	}

	/**
	 * 
	 * @param pElemName
	 *            Name of start element to find
	 * @param n
	 *            The n:th instance of the element to find
	 * @return Returns the start element
	 */
	private StartElement getStartElement(String pElemName, int n) {

		int count = 0;

		for (XMLEvent event : eventList) {
			if (event.getEventType() == XMLEvent.START_ELEMENT) {
				StartElement startElement = event.asStartElement();
				if (startElement.getName().getLocalPart().equals(pElemName)) {
					count++;
					if (count == n) {
						return startElement;
					}
				}
			}
		}
		return null;
	}

	/**
	 * 
	 * @param pElemName
	 *            Name of start element to find
	 * @param n
	 *            The n:th instance of the element to find
	 * @return Returns the start element
	 */
	private StartElement getStartElement(String pElemName) {
		return getStartElement(pElemName, 1);
	}

	/**
	 * Determines if the TsPart is ready to be written. TsPart is everything up
	 * to the first context or the first message.
	 * 
	 * @param elemName
	 *            Current element returned by the Stax parser
	 * @return
	 */
	private boolean tsPartReady(String elemName) {
		if (ts.currentDocumentLocation == DocumentLocation.TS) {
			if (elemName.equals("context") || elemName.equals("message")) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Determines if a ContextPart is ready to be written. ContextPart is
	 * everything up to the next context or the next message.
	 * 
	 * @param elemName
	 *            Current element returned by the Stax parser
	 * @return
	 */
	private boolean contextPartReady(String elemName) {
		if (ts.currentDocumentLocation == DocumentLocation.CONTEXT) {
			if (elemName.equals("context") || elemName.equals("message")) {
				return true;
			}
		}
		return false;
	}

	IResource generateTsPart(boolean start) {

		boolean nextIsSkippableEmpty = false;
		DocumentPart resource;

		resource = new DocumentPart(ts.otherId.createId(), false);
		skel = new GenericSkeleton();

		for (XMLEvent event : eventList) {

			if (nextIsSkippableEmpty) {
				nextIsSkippableEmpty = false;
				continue;
			}

			if (event.getEventType() == XMLEvent.START_DOCUMENT) {
				procStartDoc(event);
			} else if (event.getEventType() == XMLEvent.END_DOCUMENT) {

			} else if (event.getEventType() == XMLEvent.DTD) {
				procDTD(event);
			} else if (event.getEventType() == XMLEvent.CDATA) {
				procCDATA(event);
			} else if (event.getEventType() == XMLEvent.COMMENT) {
				procComment(event);

			} else if (event.getEventType() == XMLEvent.START_ELEMENT) {

				StartElement startElem = event.asStartElement();
				String startElemName = startElem.getName().getLocalPart();
				nextIsSkippableEmpty = nextIsSkippableEmpty(startElem);

				if (start && startElemName.equals("TS")) {
					procStartElemTS(startElem, resource);
				} else if (startElemName.equals("byte")) {
					procStartElemByte(startElem);
				} else {
					procStartElemGeneric(startElem, nextIsSkippableEmpty);
				}

			} else if (event.getEventType() == XMLEvent.END_ELEMENT) {

				EndElement endElem = event.asEndElement();
				procEndElem(endElem);

			} else if (event.getEventType() == XMLEvent.CHARACTERS) {

				Characters chars = event.asCharacters();
				procCharacters(chars);

			}
		}
		resource.setSkeleton(skel);
		return resource;
	}

	IResource generateContextPart(boolean start) {

		boolean nextIsSkippableEmpty = false;
		IResource resource;

		skel = new GenericSkeleton();

		String otherId = ts.otherId.createId();

		if (start) {

			ts.contextStack.push(otherId);
			resource = new StartGroup(null, String.valueOf(otherId));
			if (contextName != null) {
				((StartGroup) resource).setName(contextName);
				contextName = null;
			}
		} else {

			ts.contextStack.pop();
			resource = new Ending(String.valueOf(otherId));
		}

		for (XMLEvent event : eventList) {

			if (nextIsSkippableEmpty) {
				nextIsSkippableEmpty = false;
				continue;
			}

			if (event.getEventType() == XMLEvent.DTD) {
				procDTD(event);
			} else if (event.getEventType() == XMLEvent.COMMENT) {
				procComment(event);
			} else if (event.getEventType() == XMLEvent.START_ELEMENT) {

				StartElement startElem = event.asStartElement();
				String startElemName = startElem.getName().getLocalPart();
				nextIsSkippableEmpty = nextIsSkippableEmpty(startElem);

				if (start && startElemName.equals("context")) {
					procStartElemContext(startElem, resource);
				} else if (startElemName.equals("byte")) {
					procStartElemByte(startElem);
				} else {
					procStartElemGeneric(startElem, nextIsSkippableEmpty);
				}

			} else if (event.getEventType() == XMLEvent.END_ELEMENT) {

				EndElement endElem = event.asEndElement();
				procEndElem(endElem);

			} else if (event.getEventType() == XMLEvent.CHARACTERS) {

				Characters chars = event.asCharacters();
				procCharacters(chars);

			} else if (event.getEventType() == XMLEvent.CDATA) {
				procCDATA(event);
			}
		}
		resource.setSkeleton(skel);
		return resource;
	}

	ITextUnit generateTu() {

		boolean nextIsSkippableEmpty = false;

		skel = new GenericSkeleton();
		ITextUnit tu = new TextUnit(String.valueOf(++ts.tuId));
		tu.setPreserveWhitespaces(true);

		for (XMLEvent event : eventList) {

			if (nextIsSkippableEmpty) {
				nextIsSkippableEmpty = false;
				continue;
			}

			if (event.getEventType() == XMLEvent.START_ELEMENT) {

				StartElement startElem = event.asStartElement();
				String startElemName = startElem.getName().getLocalPart();
				nextIsSkippableEmpty = nextIsSkippableEmpty(startElem);

				// --begin translator comment--
				if (startElemName.equals("translatorcomment")) {
					ts.procTransComment = true;
				}

				if (startElemName.equals("message")) {
					procStartElemMessage(startElem, tu);

					Attribute msgId = startElem.getAttributeByName(new QName(
							"id"));
					if (msgId != null) {
						ts.messageId = msgId.getValue();
					}

				} else if (startElemName.equals("source")) {
					ts.currentMessageLocation = MessageLocation.SOURCE;
					procStartElemSource(startElem, tu);
				} else if (startElemName.equals("translation")) {
					ts.currentMessageLocation = MessageLocation.TARGET;
					procStartElemTarget(startElem, tu);
				} else if (startElemName.equals("numerusform")) {
					// TODO Handle numerusform
					procStartElemAddToTuContent(startElem, tu);
				} else if (startElemName.equals("lengthvariant")) {
					// TODO Handle lengthvariant
					procStartElemAddToTuContent(startElem, tu);
				} else if (startElemName.equals("byte")) {
					// TODO Handle byte within numberus and lengthvariant
					procStartElemByte(startElem, tu);
				} else {
					procStartElemGeneric(startElem, nextIsSkippableEmpty);
				}

			} else if (event.getEventType() == XMLEvent.END_ELEMENT) {

				EndElement endElem = event.asEndElement();
				String endElemName = endElem.getName().getLocalPart();

				// --end translator comment--
				if (endElemName.equals("translatorcomment")) {
					ts.procTransComment = false;
				}

				if (endElemName.equals("source")) {
					ts.currentMessageLocation = MessageLocation.RESOURCE;
					procEndElem(endElem);

				} else if (endElemName.equals("translation")) {
					ts.currentMessageLocation = MessageLocation.RESOURCE;

					if (ts.targetIsEmpty()) {

						skel.addContentPlaceholder(tu, trgLang);

						Property approvedProp = tu.getTargetProperty(trgLang,
								Property.APPROVED);
						if (approvedProp.getValue().equals("yes")) {
							approvedProp.setValue("no");
							tu.setTargetProperty(trgLang, approvedProp);
							logger.warn(
									"Translation (Line {}) is empty. type attribute was updated to unfinished.",
									endElem.getLocation().getLineNumber());
						}
					}
					procEndElem(endElem);
				} else if (endElemName.equals("numerusform")) {
					// TODO Handle numerusform
					procEndElemAddToTuContent(endElem, tu);
				} else if (endElemName.equals("lengthvariant")) {
					// TODO Handle lengthvariant
					procEndElemAddToTuContent(endElem, tu);
				} else {
					procEndElem(endElem);
				}

				if (needTargetSection()) {
					if (insertTargetAfterElem(endElemName)) {
						addTargetSection(tu);
					}
				}

			} else if (event.getEventType() == XMLEvent.CHARACTERS) {
				Characters chars = event.asCharacters();
				procCharacters(chars, tu);
			}
		}

		if (params.getUseCodeFinder()) {
			params.getCodeFinder().process(tu.getSource().getFirstContent());
			params.getCodeFinder().process(
					tu.getTarget(trgLang).getFirstContent());
			// TODO: new codes may need to be escaped!!!
		}

		tu.setSkeleton(skel);
		tu.setMimeType(MimeTypeMapper.TS_MIME_TYPE);
		if (ts.messageId != null) {
			tu.setName(ts.messageId);
		}
		return tu;
	}

	void generateNumerusFormTu() {

		boolean nextIsSkippableEmpty = false;

		TextFragment source = new TextFragment();
		StartGroup sg = new StartGroup(null, ts.otherId.createId());
		Ending end = new Ending(ts.otherId.createId());
		ITextUnit tf_target = null;

		int numerus_counter = 0;

		for (XMLEvent event : eventList) {

			if (nextIsSkippableEmpty) {
				nextIsSkippableEmpty = false;
				continue;
			}

			if (event.getEventType() == XMLEvent.START_ELEMENT) {

				StartElement startElem = event.asStartElement();
				String startElemName = startElem.getName().getLocalPart();

				nextIsSkippableEmpty = nextIsSkippableEmpty(startElem);

				if (startElemName.equals("message")) {
					procStartElemMessage(startElem, sg);

					Attribute msgId = startElem.getAttributeByName(new QName(
							"id"));
					if (msgId != null) {
						ts.messageId = msgId.getValue();
					}

				} else if (startElemName.equals("source")) {
					ts.currentMessageLocation = MessageLocation.SOURCE;
					addStartElemToSkel(startElem);
				} else if (startElemName.equals("translation")) {
					procStartElemTarget(startElem, sg);
				} else if (startElemName.equals("numerusform")) {
					ts.currentMessageLocation = MessageLocation.TARGET;

					// --send start group when encountering first numerus
					// instance
					if (numerus_counter == 0) {
						sg.setSkeleton(skel);

						if (ts.messageId != null) {
							sg.setName(ts.messageId);
						}

						queue.add(new Event(EventType.START_GROUP, sg));
						skel = new GenericSkeleton();
					}
					numerus_counter++;

					addStartElemToSkel(startElem);
					tf_target = new TextUnit(String.valueOf(++ts.tuId));
					tf_target.setPreserveWhitespaces(true);
					
					if ( ts.isNumerus() ) tf_target.setProperty(new Property("numerus", "yes"));
					tf_target.setSourceContent(source);
					tf_target.createTarget(trgLang, false,
							IResource.CREATE_EMPTY);

				} else if (startElemName.equals("lengthvariant")) {
					// TODO Handle lengthvariant
					if (ts.currentMessageLocation == MessageLocation.RESOURCE) {
						addStartElemToSkel(startElem);
					} else if (ts.currentMessageLocation == MessageLocation.TARGET) {
						procStartElemAddToTuContent(startElem, tf_target);
					}

				} else if (startElemName.equals("byte")) {
					// TODO Handle byte within numberus and lengthvariant
					Attribute attr = startElem.getAttributeByName(new QName(
							"value"));
					if (ts.currentMessageLocation == MessageLocation.RESOURCE) {
						procStartElemByte(startElem);
					} else if (ts.currentMessageLocation == MessageLocation.SOURCE) {
						procStartElemByte(startElem);
						if (params.getDecodeByteValues()) {
							source.append(decodeByteValue(attr.getValue()));
						} else { // Else: we make the element an inline code
									// We can use getFirstPartContent() because
									// nothing is segmented
							source.append(TagType.PLACEHOLDER, "byte",
									"<byte value=\"" + attr.getValue() + "\"/>");
						}
					} else if (ts.currentMessageLocation == MessageLocation.TARGET) {
						procStartElemByte(startElem, tf_target);
					}

				} else {
					procStartElemGeneric(startElem, nextIsSkippableEmpty);
				}

			} else if (event.getEventType() == XMLEvent.END_ELEMENT) {

				EndElement endElem = event.asEndElement();
				String endElemName = endElem.getName().getLocalPart();

				if (endElemName.equals("source")) {
					ts.currentMessageLocation = MessageLocation.RESOURCE;
					procEndElem(endElem);

				} else if (endElemName.equals("numerusform")) {

					ts.currentMessageLocation = MessageLocation.RESOURCE;

					// TODO: Put empty placeholder if the numerusform elem is
					// empty
					procEndElem(endElem);

					if (params.getUseCodeFinder()) {
						// We can use getFirstPartContent() because nothing is
						// segmented
						params.getCodeFinder().process(
								tf_target.getSource().getFirstContent());
						params.getCodeFinder().process(
								tf_target.getTarget(trgLang).getFirstContent());
						// TODO: new codes may need to be escaped!!!
					}

					tf_target.setSkeleton(skel);
					tf_target.setMimeType(MimeTypeMapper.TS_MIME_TYPE);
					queue.add(new Event(EventType.TEXT_UNIT, tf_target));
					skel = new GenericSkeleton();

				} else if (endElemName.equals("lengthvariant")) {
					// TODO Handle lengthvariant
					if (ts.currentMessageLocation == MessageLocation.RESOURCE) {
						procEndElem(endElem);
					} else if (ts.currentMessageLocation == MessageLocation.TARGET) {
						procEndElemAddToTuContent(endElem, tf_target);
					}
				} else {
					procEndElem(endElem);
				}
			} else if (event.getEventType() == XMLEvent.CHARACTERS) {
				Characters chars = event.asCharacters();
				if (ts.currentMessageLocation == MessageLocation.RESOURCE) {
					procCharacters(chars);
				} else if (ts.currentMessageLocation == MessageLocation.SOURCE) {
					source.append(chars.getData());
					procCharacters(chars);
				} else if (ts.currentMessageLocation == MessageLocation.TARGET) {
					TextContainer tc = tf_target.getTarget(trgLang);
					if (!tc.hasText()) {
						skel.addContentPlaceholder(tf_target, trgLang);
					}
					// We can use getFirstPartContent() because nothing is
					// segmented
					tc.getFirstContent().append(chars.getData());
				}
			}
		}

		end.setSkeleton(skel);
		queue.add(new Event(EventType.END_GROUP, end));
		skel = new GenericSkeleton();
	}

	private void addTargetSection(ITextUnit tu) {
		skel.append(lineBreak);
		skel.append("<translation");
		skel.addValuePlaceholder(tu, Property.APPROVED, trgLang);
		tu.setTargetProperty(trgLang, new Property(Property.APPROVED, "no",
				false));
		tu.setTargetProperty(trgLang, new Property("variants", "no"));
		skel.append(" variants=\"no\">");
		skel.addContentPlaceholder(tu, trgLang);
		skel.append("</translation>");
	}

	private boolean insertTargetAfterElem(String endElemName) {
		if (endElemName.equals(ts.elemBeforeTrg)) {
			return true;
		} else {
			return false;
		}
	}

	private boolean needTargetSection() {
		if (!ts.noSource() && !ts.targetExists) {
			return true;
		} else {
			return false;
		}
	}

	DocumentPart generateObsoleteTu() {

		boolean nextIsSkippableEmpty = false;

		skel = new GenericSkeleton();
		DocumentPart dp = new DocumentPart(ts.otherId.createId(), false);

		for (XMLEvent event : eventList) {

			if (nextIsSkippableEmpty) {
				nextIsSkippableEmpty = false;
				continue;
			}

			if (event.getEventType() == XMLEvent.START_ELEMENT) {

				StartElement startElem = event.asStartElement();

				nextIsSkippableEmpty = nextIsSkippableEmpty(startElem);

				if (startElem.getName().getLocalPart().equals("byte")) {
					procStartElemByte(startElem);
				} else {
					procStartElemGeneric(startElem, nextIsSkippableEmpty);
				}

			} else if (event.getEventType() == XMLEvent.END_ELEMENT) {

				EndElement endElement = event.asEndElement();
				procEndElem(endElement);

			} else if (event.getEventType() == XMLEvent.CHARACTERS) {

				Characters chars = event.asCharacters();
				procCharacters(chars);

			}
		}
		dp.setSkeleton(skel);
		return dp;
	}

	private void procCharacters(Characters chars, ITextUnit tu) {
		// --setting translator comment--
		if (ts.procTransComment) {
			tu.setProperty(new Property(Property.TRANSNOTE, chars.getData()));
		}

		if (ts.currentMessageLocation == MessageLocation.RESOURCE) {
			procCharacters(chars);
		} else if (ts.currentMessageLocation == MessageLocation.SOURCE) {
			TextContainer tc = tu.getSource();
			if (!tc.hasText()) {
				skel.addContentPlaceholder(tu);
			}
			// We can use getFirstPartContent() because nothing is segmented
			tc.getFirstContent().append(chars.getData());
		} else if (ts.currentMessageLocation == MessageLocation.TARGET) {
			TextContainer tc = tu.getTarget(trgLang);
			if (!tc.hasText()) {
				skel.addContentPlaceholder(tu, trgLang);
			}
			// We can use getFirstPartContent() because nothing is segmented
			tc.getFirstContent().append(chars.getData());
		}
	}

	private void procCharacters(Characters chars) {

		String escaped = Util.escapeToXML(
				chars.getData().replace("\n", lineBreak), 0, true, null);
		skel.append(escaped);
	}

	private void procDTD(XMLEvent event) {
		DTD dtd = (DTD) event;
		skel.append(dtd.getDocumentTypeDeclaration().replace("\n", lineBreak));
	}

	private void procCDATA(XMLEvent event) {
		Characters chars = event.asCharacters();
		skel.append(chars.getData().replace("\n", lineBreak));
	}

	private void procComment(XMLEvent event) {
		Comment comment = (Comment) event;
		skel.append("<!--" + comment.getText().replace("\n", lineBreak) + "-->");
	}

	private void procStartDoc(XMLEvent event) {

		javax.xml.stream.events.StartDocument startDoc = (javax.xml.stream.events.StartDocument) event;

		String xmlVersion = startDoc.getVersion();
		if (xmlVersion != null && !xmlVersion.equals("1.0")) {
			logger.warn("Filter will use xml version 1.0");
		}

		String xmlEnc = startDoc.getCharacterEncodingScheme();
		if (xmlEnc != null && !xmlEnc.equalsIgnoreCase(encoding)) {
			logger.warn(
					"The xml encoding attribute value {} is different from "
							+ "what was detected or specified in the settings. Encoding {} will be used.",
					xmlEnc, encoding);
		}
	}

	private void procStartElemTS(StartElement startElement, IResource resource) {
		addStartElemToSkelAddProps(startElement, resource);
	}

	private void procStartElemContext(StartElement startElement,
			IResource resource) {
		addStartElemToSkelAddProps(startElement, resource);
	}

	private void procStartElemMessage(StartElement startElement,
			IResource resource) {
		addStartElemToSkelAddProps(startElement, resource);
	}

	private void procStartElemSource(StartElement startElement, ITextUnit tu) {
		addStartElemToSkel(startElement);
	}

	@SuppressWarnings("unchecked")
	private void procStartElemTarget(StartElement startElement,
			IResource resource) {

		INameable nameable = (INameable) resource;

		boolean typeFound = false;

		skel.append("<" + startElement.getName().getLocalPart());

		Iterator<Attribute> attributes = startElement.getAttributes();
		while (attributes.hasNext()) {
			Attribute attribute = attributes.next();
			if (attribute.getName().getLocalPart().equals("type")) {

				typeFound = true;

				if (attribute.getValue().equals("unfinished")) {
					skel.addValuePlaceholder(nameable, Property.APPROVED,
							trgLang);
					nameable.setTargetProperty(trgLang, new Property(
							Property.APPROVED, "no", false));
				} else {
					skel.append(String.format(" %s=\"%s\"", attribute.getName()
							.getLocalPart(), attribute.getValue()));
					Property prop = new Property(attribute.getName()
							.getLocalPart(), attribute.getValue());
					nameable.setTargetProperty(trgLang, prop);
				}
			} else {
				skel.append(String.format(" %s=\"%s\"", attribute.getName()
						.getLocalPart(), attribute.getValue()));
				Property prop = new Property(
						attribute.getName().getLocalPart(),
						attribute.getValue());
				nameable.setTargetProperty(trgLang, prop);
			}
		}
		if (!typeFound) {
			skel.addValuePlaceholder(nameable, Property.APPROVED, trgLang);
			nameable.setTargetProperty(trgLang, new Property(Property.APPROVED,
					"yes", false));
		}
		skel.append(">");
	}

	@SuppressWarnings("unchecked")
	private void addStartElemToSkelAddProps(StartElement startElement,
			IResource resource) {

		skel.append("<" + startElement.getName().getLocalPart());
		Iterator<Attribute> attributes = startElement.getAttributes();

		while (attributes.hasNext()) {
			Attribute attribute = attributes.next();
			skel.append(String.format(" %s=\"%s\"", attribute.getName()
					.getLocalPart(), attribute.getValue()));
			Property prop = new Property(attribute.getName().getLocalPart(),
					attribute.getValue());
			if (resource instanceof DocumentPart) {
				((DocumentPart) resource).setProperty(prop);
			} else if (resource instanceof StartGroup) {
				((StartGroup) resource).setProperty(prop);
			} else if (resource instanceof ITextUnit) {
				((ITextUnit) resource).setProperty(prop);
			}
		}
		skel.append(">");
	}

	@SuppressWarnings("unchecked")
	private void addStartElemToSkel(StartElement startElement) {

		skel.append("<" + startElement.getName().getLocalPart());
		Iterator<Attribute> attributes = startElement.getAttributes();
		while (attributes.hasNext()) {
			Attribute attribute = attributes.next();
			skel.append(String.format(" %s=\"%s\"", attribute.getName()
					.getLocalPart(), attribute.getValue()));
		}
		skel.append(">");
	}

	@SuppressWarnings("unchecked")
	private void procStartElemAddToTuContent(StartElement startElement,
			ITextUnit tu) {
		StringBuilder sb = new StringBuilder();

		sb.append("<" + startElement.getName().getLocalPart());
		Iterator<Attribute> attributes = startElement.getAttributes();
		while (attributes.hasNext()) {
			Attribute attribute = attributes.next();
			sb.append(String.format(" %s=\"%s\"", attribute.getName()
					.getLocalPart(), attribute.getValue()));
		}
		sb.append(">");

		if (ts.currentMessageLocation == MessageLocation.SOURCE) {
			TextContainer tc = tu.getSource();
			if (!tc.hasText()) {
				skel.addContentPlaceholder(tu);
			}
			// We can use getFirstPartContent() because nothing is segmented
			tc.getFirstContent().append(sb.toString());
		} else if (ts.currentMessageLocation == MessageLocation.TARGET) {
			TextContainer tc = tu.getTarget(trgLang);
			if (!tc.hasText()) {
				skel.addContentPlaceholder(tu, trgLang);
			}
			// We can use getFirstPartContent() because nothing is segmented
			tc.getFirstContent().append(sb.toString());
		}
	}

	private void procEndElemAddToTuContent(EndElement endElement, ITextUnit tu) {
		StringBuilder sb = new StringBuilder();
		sb.append("</" + endElement.getName().getLocalPart() + ">");

		if (ts.currentMessageLocation == MessageLocation.SOURCE) {
			TextContainer tc = tu.getSource();
			if (!tc.hasText()) {
				skel.addContentPlaceholder(tu);
			}
			tc.getFirstContent().append(sb.toString());
		} else if (ts.currentMessageLocation == MessageLocation.TARGET) {
			TextContainer tc = tu.getTarget(trgLang);
			if (!tc.hasText()) {
				skel.addContentPlaceholder(tu, trgLang);
			}
			tc.getFirstContent().append(sb.toString());
		}
	}

	/**
	 * Skeleton helper method to append the StartElement as generic content.
	 * 
	 * @param elem
	 *            StartElement to append
	 * @param nextIsSkippableEmpty
	 */
	@SuppressWarnings("unchecked")
	private void procStartElemGeneric(StartElement elem,
			boolean nextIsSkippableEmpty) {

		skel.append("<" + elem.getName().getLocalPart());

		Iterator<Attribute> attributes = elem.getAttributes();
		while (attributes.hasNext()) {
			Attribute attribute = attributes.next();
			skel.append(String.format(" %s=\"%s\"", attribute.getName()
					.getLocalPart(), attribute.getValue()));
		}

		if (nextIsSkippableEmpty) {
			skel.append("/>");
		} else {
			skel.append(">");
		}
	}

	/**
	 * Skeleton helper method to append the byte (inside a message element) as
	 * skeleton. Decoded or not depending on the location. The decoded
	 * 
	 * @param elem
	 *            StartElement to append
	 * @param tu
	 *            The tu whose TextContainer to populate if needed.
	 */
	private void procStartElemByte(StartElement elem, ITextUnit tu) {
		Attribute attr = elem.getAttributeByName(new QName("value"));
		if (ts.currentMessageLocation == MessageLocation.RESOURCE) {
			procStartElemByte(elem);
		} else if (ts.currentMessageLocation == MessageLocation.SOURCE) {
			TextContainer tc = tu.getSource();
			// --This segment adds a tu placeholder assuming this is the first
			// content that is added to the container
			if (!tc.hasText()) {
				skel.addContentPlaceholder(tu);
			}

			if (params.getDecodeByteValues()) {
				// We can use getFirstPartContent() because nothing is segmented
				tc.getFirstContent().append(decodeByteValue(attr.getValue()));
			} else { // Else: we make the element an inline code
						// We can use getFirstPartContent() because nothing is
						// segmented
				tc.getFirstContent().append(TagType.PLACEHOLDER, "byte",
						"<byte value=\"" + attr.getValue() + "\"/>");
			}
		} else if (ts.currentMessageLocation == MessageLocation.TARGET) {
			TextContainer tc = tu.getTarget(trgLang);
			// --This segment adds a tu placeholder assuming this is the first
			// content that is added to the container
			if (!tc.hasText()) {
				skel.addContentPlaceholder(tu, trgLang);
			}

			if (params.getDecodeByteValues()) {
				// We can use getFirstPartContent() because nothing is segmented
				tc.getFirstContent().append(decodeByteValue(attr.getValue()));
			} else { // Else: we make the element an inline code
						// We can use getFirstPartContent() because nothing is
						// segmented
				tc.getFirstContent().append(TagType.PLACEHOLDER, "byte",
						"<byte value=\"" + attr.getValue() + "\"/>");
			}
		}
	}

	/**
	 * Skeleton helper method to append the byte as skeleton (as is without
	 * decoding). Generates both start and end part since it's a "skippable"
	 * empty.
	 * 
	 * @param elem
	 *            StartElement (and EndElement) to append
	 */
	private void procStartElemByte(StartElement elem) {
		skel.append("<byte value=\""
				+ elem.getAttributeByName(new QName("value")).getValue()
				+ "\"/>");
	}

	/**
	 * Skeleton helper method to append the EndElement to the skeleton.
	 * 
	 * @param elem
	 *            EndElement to append
	 */
	private void procEndElem(EndElement elem) {
		skel.append("</" + elem.getName().getLocalPart() + ">");
	}

	/**
	 * Helper method determining if an element should be empty.
	 * 
	 * @param name
	 *            Element name
	 * @return True if element should be empty
	 */
	private boolean elementShouldBeEmpty(String name) {
		if (name.equals("byte") || name.equals("location")) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Helper method to determine if an element is empty and "skippable" such as
	 * <byte/> and <location/>, but not <source/> and <target/> otherwise
	 * there's not place to put the tu placeholder.
	 * 
	 * @param elem
	 *            Element to check
	 * @return True if next element is a "skippable" empty
	 */
	boolean nextIsSkippableEmpty(StartElement elem) {
		String elemName = elem.getName().getLocalPart();
		if (elementShouldBeEmpty(elemName) && nextIsEmpty(elem)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Helper method to check if element is empty.
	 * 
	 * @param elem
	 *            Element to check
	 * @return True if element is empty
	 */
	private boolean nextIsEmpty(StartElement elem) {
		int index = eventList.indexOf(elem);
		XMLEvent nextEvent = eventList.get(index + 1);
		if (nextEvent != null) {
			if (nextEvent.isEndElement()) {
				EndElement nextEndElem = nextEvent.asEndElement();
				if (elem.getName().getLocalPart()
						.equals(nextEndElem.getName().getLocalPart())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Helper method to decode a byte value.
	 * 
	 * @param str
	 *            Byte value
	 * @return Character as String
	 */
	private String decodeByteValue(String str) {
		try {
			if (str.startsWith("x")) {
				str = str.substring(1, str.length());
				int i = Integer.parseInt(str, 16);
				char c = (char) i;
				return "" + c;
			} else {
				int i = Integer.parseInt(str);
				char c = (char) i;
				return "" + c;
			}
		} catch (NumberFormatException ne) {
			throw new OkapiBadFilterInputException("Invalid value (" + str
					+ " ) in byte element. ");
		}
	}

}
