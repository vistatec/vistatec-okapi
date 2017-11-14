/*===========================================================================
  Copyright (C) 2016-2017 by the Okapi Framework contributors
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

import static net.sf.okapi.filters.openxml.XMLEventHelpers.LOCAL_BREAK;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.LOCAL_TAB;
import static net.sf.okapi.filters.openxml.XMLEventHelpers.createQName;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.events.Attribute;

import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;

class BlockTextUnitWriter implements TextUnitWriter {
	private XMLEventFactory eventFactory;
	private QName runName, textName;
	private Map<Integer, XMLEvents> codeMap;
	private XMLEventSerializer xmlWriter;
	private BidirectionalityClarifier bidirectionalityClarifier;
	private ConditionalParameters cparams;

	private Deque<RunProperties> currentRunProperties = new ArrayDeque<>();
	private StringBuilder textContent = new StringBuilder();

	public BlockTextUnitWriter(XMLEventFactory eventFactory, QName runName, QName textName,
			Map<Integer, XMLEvents> codeMap, XMLEventSerializer xmlWriter,
			BidirectionalityClarifier bidirectionalityClarifier, ConditionalParameters cparams) {
		this.eventFactory = eventFactory;
		this.runName = runName;
		this.textName = textName;
		this.codeMap = codeMap;
		this.xmlWriter = xmlWriter;
		this.bidirectionalityClarifier = bidirectionalityClarifier;
		this.cparams = cparams;
	}

	public void write(TextContainer tc) {
		for (Segment segment : tc.getSegments()) {
			writeSegment(segment);
		}
		flushText(true);
	}

	private void writeSegment(Segment segment) {
		TextFragment content = segment.getContent();
		String codedText = content.getCodedText();
		List<Code> codes = content.getCodes();
		for (int i = 0; i < codedText.length(); i++) {
			char c = codedText.charAt(i);
			if (TextFragment.isMarker(c)) {
				int codeIndex = TextFragment.toIndex(codedText.charAt(++i));
				writeCode(codes.get(codeIndex));
			}
			else {
				writeChar(c);
			}
		}
	}

	private void writeChar(char c) {
		textContent.append(c);
	}

	private void writeCode(Code code) {
		// Cases:
		// - Open
		//   - Terminate current run
		//   - Do something content-dependent:   
		//	    - If it's RunProperties, update run properties
		//	    - If it's a RunContainer, write opening tag
		// - Closed
		//   - Terminate current run
		//   - Handling this is actually optional in many cases
		//	 - If it's a RunContainer, write the closing tag
		// - Isolated
		//   - Terminate current run
		//   - Write out the corresponding markup (for Run, Run.RunMarkup, Block.BlockMarkup
		int id = code.getId();
		XMLEvents codeEvents = codeMap.get(id);
		switch (code.getTagType()) {
			case OPENING:
				flushText(true);
				if (codeEvents instanceof RunProperties) {
					currentRunProperties.push((RunProperties)codeEvents);
				}
				else if (codeEvents instanceof RunContainer) {
					RunContainer rc = (RunContainer)codeEvents;
					xmlWriter.add(rc.getStartElement());
					currentRunProperties.push(rc.getDefaultRunProperties());
				}
				else {
					throw new IllegalStateException("Unexpected code contents:" + codeEvents);
				}
				break;
			case PLACEHOLDER:
				// If this is RunMarkup (markup contained within a run), we should
				// keep the current run open.  Otherwise, close it.
				boolean isRunMarkup = (codeEvents instanceof Run.RunMarkup);
				if (isRunMarkup) {
					flushRunStart();
				}
				flushText(!isRunMarkup);
				xmlWriter.add(codeEvents);
				break;
			case CLOSING:
				flushText(true);
				if (codeEvents instanceof RunProperties) {
					// XXX What if it's not on the top of the stack?  It's probably a corrupt target.
					currentRunProperties.pop();
				}
				else if (codeEvents instanceof RunContainer) {
					xmlWriter.add(((RunContainer)codeEvents).getEndElement());
					currentRunProperties.pop(); // Pop RunContainer properties
				}
				else {
					throw new IllegalStateException("Unexpected code contents:" + codeEvents);
				}

				break;
		}
	}

	private boolean runIsOpen = false;

	private void flushRunStart() {
		if (!runIsOpen) {
			writeRunStart(currentRunProperties.peek());
			runIsOpen = true;
		}
	}

	private void flushText(boolean terminateRun) {
		if (textContent.length() > 0) {
			flushRunStart();
			String text = textContent.toString();
			writeRunText(text);
			textContent = new StringBuilder();
		}
		if (terminateRun && runIsOpen) {
			writeRunEnd();
			runIsOpen = false;
		}
	}

	private void writeRunStart(RunProperties properties) {
		if (runName == null) {
			throw new IllegalStateException("no run name set");
		}

		xmlWriter.add(eventFactory.createStartElement(runName, null, null));
		properties = bidirectionalityClarifier.clarifyRunProperties(properties);

		if (properties != null) {
			xmlWriter.add(properties);
		}
	}

	// Would be better to have a separate hierarcy for the MS Word BlockTextUnitWrite.java and for the Excel
	// BlockTextUnitWrite.java but...
	private void writeRunText(String text) {
		if (textName == null) {
			throw new IllegalStateException("no text name set");
		}

		// MS Excel doesn't support the line breaks inside text run
		// We should save a content as is
		// Current implementation of ms excel text run has <t> without prefix
		// We are using this fact to catch an excel text runs
		if (textName.getPrefix().isEmpty()) {
			writeText(text);
			return;
		}

		// MS Word text runs can contain the line breaks
		// The text run of ms word looks like <w:t>
		// The prefix "w" says us that is ms word text tun
		StringBuilder sb = new StringBuilder();
		for (char c : text.toCharArray()) {
			if (c == cparams.getLineSeparatorReplacement() && cparams.getAddLineSeparatorCharacter()) {
				writeTextIfNeeded(sb);
				sb.setLength(0);
				writeLineBreak();
			} else if (c == '\t' && cparams.getAddTabAsCharacter() &&
						Namespaces.WordProcessingML.containsName(textName)) {
				writeTextIfNeeded(sb);
				sb.setLength(0);
				writeTab();
			} else {
				sb.append(c);
			}
		}
		writeTextIfNeeded(sb);
	}

	private void writeTextIfNeeded(StringBuilder buffer) {
		if (buffer.length() > 0) {
			writeText(buffer.toString());
		}
	}

	private void writeTab() {
		QName br = createQName(LOCAL_TAB, textName);
		xmlWriter.add(eventFactory.createStartElement(br, null, null));
		xmlWriter.add(eventFactory.createEndElement(br, null));
	}

	private void writeLineBreak() {
		QName br = createQName(LOCAL_BREAK, textName);
		xmlWriter.add(eventFactory.createStartElement(br, null, null));
		xmlWriter.add(eventFactory.createEndElement(br, null));
	}

	private void writeText(String text) {
		boolean needsPreserveSpace = OpenXMLContentSkeletonWriter.needsXmlSpacePreserve(text);
		ArrayList<Attribute> attrs = new ArrayList<>();
		// DrawingML <a:t> does not use the xml:space="preserve" attribute
		if (needsPreserveSpace && !Namespaces.DrawingML.containsName(textName)) {
			attrs.add(eventFactory.createAttribute("xml", Namespaces.XML.getURI(), "space", "preserve"));
		}
		xmlWriter.add(eventFactory.createStartElement(textName, attrs.iterator(), null));
		xmlWriter.add(eventFactory.createCharacters(text));
		xmlWriter.add(eventFactory.createEndElement(textName, null));
	}

	private void writeRunEnd() {
		xmlWriter.add(eventFactory.createEndElement(runName, null));
	}
}