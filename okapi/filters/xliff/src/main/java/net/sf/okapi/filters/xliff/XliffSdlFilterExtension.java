/*===========================================================================
  Copyright (C) 2013 by the Okapi Framework contributors
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
=========================================================================== */

package net.sf.okapi.filters.xliff;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import net.sf.okapi.common.Util;
import net.sf.okapi.common.exceptions.OkapiBadFilterInputException;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;

/**
 * Extension to the XLIFFFilter to handle SDL specific metadata in the XLIFF document.
 */
public class XliffSdlFilterExtension {
	public static final String SDL_NAMESPACE_PREFIX = "xmlns:sdl";
	public static final String SDL_NAMESPACE_URI = "http://sdl.com/FileTypes/SdlXliff/1.0";
	public static final QName XLIFF = new QName("", "xliff");
	public static final QName TAG_DEFS = new QName(SDL_NAMESPACE_URI, "tag-defs");
	public static final QName TAG = new QName(SDL_NAMESPACE_URI, "tag");
	public static final QName TAG_ID = new QName("", "id");
	public static final QName TAG_BPT = new QName(SDL_NAMESPACE_URI, "bpt");
	public static final QName TAG_EPT = new QName(SDL_NAMESPACE_URI, "ept");
	public static final QName TAG_PH = new QName(SDL_NAMESPACE_URI, "ph");
	public static final QName TAG_IT = new QName(SDL_NAMESPACE_URI, "it");
	public static final QName TAG_ST = new QName(SDL_NAMESPACE_URI, "st");
	public static final QName NAME = new QName("", "name");
	public static final QName EQUIV_TEXT = new QName("", "equiv-text");

	//private final Logger logger = LoggerFactory.getLogger(getClass());

	private XMLStreamReader reader;
	private XLIFFFilter xliffFilter;
	private Parameters params;
	private int extraId = Integer.MAX_VALUE;

	public XliffSdlFilterExtension()
	{
	}

	public Map<String, SdlTagDef> parse(XMLStreamReader reader, Parameters params) throws XMLStreamException
	{
		this.reader = reader;
		this.xliffFilter = new XLIFFFilter();
		this.params = params;
		return parseXLIFF();
	}

	/**
	 * Additional parsing for sdl:fmt-defs only
	 * 
	 * @throws XMLStreamException
	 */
	public Map<String, SdlTagDef> parseXLIFF() throws XMLStreamException
	{
		Map<String, SdlTagDef> sdlTagdefs = new HashMap<>();
		boolean inTagDefs = false;
		try {
			while (reader.hasNext()) {
				reader.next();
				QName name = null;
				if (reader.isStartElement()) {
					// test for sdl namespace, if not found we are done
					name = reader.getName();

					if (name.getLocalPart().equals("xliff")) {
						NamespaceContext nsc = reader.getNamespaceContext();
						String sdl = nsc.getPrefix(SDL_NAMESPACE_URI);
						if (sdl == null) {
							return null;
						}
						continue;
					}

					if (name.equals(TAG_DEFS)) {
						inTagDefs = true;
						continue;
					}

					if (inTagDefs && name.equals(TAG)) {
						SdlTagDef t = processTag();
						sdlTagdefs.put(t.id, t);
						continue;
					}
				}

				if (reader.isEndElement()) {
					name = reader.getName();
					if (name.equals(TAG_DEFS)) {
						return sdlTagdefs;
					}
				}
			}
			return sdlTagdefs;
		} catch (XMLStreamException ex) {
			throw new OkapiBadFilterInputException(
					"Failed to parse XLIFF for sdl:fmt-defs\n" + ex.getLocalizedMessage(), ex);
		} finally {
			if (reader != null)
				reader.close();
		}
	}

	private SdlTagDef processTag() throws XMLStreamException {
		SdlTagDef sdlTagDef = new SdlTagDef();
		sdlTagDef.id = reader.getAttributeValue(TAG_ID.getNamespaceURI(), TAG_ID.getLocalPart());
		int numberId = -1;
		try {
			numberId = Util.fastParseInt(sdlTagDef.id);
		}
		catch ( NumberFormatException e ) {
			// Falls back to the hash-code			
			numberId = sdlTagDef.id.hashCode();
		}
				
		while (reader.hasNext()) {
			reader.next();
			if (reader.isStartElement()) {
				QName name = reader.getName();
				if (name.equals(TAG_BPT)) {
					sdlTagDef.name = reader.getAttributeValue(NAME.getNamespaceURI(), NAME.getLocalPart());
					sdlTagDef.bpt = createCode(TextFragment.TagType.OPENING, numberId, TAG_BPT.getLocalPart(),
							sdlTagDef.name);
					sdlTagDef.equiv_text = sdlTagDef.bpt.getDisplayText();  
				} else if (name.equals(TAG_EPT)) {
					sdlTagDef.name = reader.getAttributeValue(NAME.getNamespaceURI(), NAME.getLocalPart());
					sdlTagDef.ept = createCode(TextFragment.TagType.CLOSING, numberId, TAG_EPT.getLocalPart(),
							sdlTagDef.name, sdlTagDef.bpt);
					sdlTagDef.equiv_text = sdlTagDef.ept.getDisplayText();
				} else if (name.equals(TAG_PH)) {
					sdlTagDef.name = reader.getAttributeValue(NAME.getNamespaceURI(), NAME.getLocalPart());
					sdlTagDef.ph = createCode(TextFragment.TagType.PLACEHOLDER, numberId, TAG_PH.getLocalPart(),
							sdlTagDef.name);
					sdlTagDef.equiv_text = sdlTagDef.ph.getDisplayText();
				} else if (name.equals(TAG_IT)) {
					sdlTagDef.name = reader.getAttributeValue(NAME.getNamespaceURI(), NAME.getLocalPart());
					sdlTagDef.it = createCode(TextFragment.TagType.PLACEHOLDER, numberId, TAG_IT.getLocalPart(),
							sdlTagDef.name);
					sdlTagDef.equiv_text = sdlTagDef.it.getDisplayText();
				} else if (name.equals(TAG_ST)) {
					sdlTagDef.name = reader.getAttributeValue(NAME.getNamespaceURI(), NAME.getLocalPart());
					sdlTagDef.st = createCode(TextFragment.TagType.PLACEHOLDER, numberId, TAG_ST.getLocalPart(),
							sdlTagDef.name);
					sdlTagDef.equiv_text = sdlTagDef.st.getDisplayText();
				}
			}

			if (reader.isEndElement()) {
				QName name = reader.getName();
				if (name.equals(TAG)) {
					return sdlTagDef;
				}
			}
		}
		return sdlTagDef;
	}

	private Code createCode(TagType tagType,
							int id,
							String tagName,
							String type) {
		return createCode(tagType, id, tagName, type, null);
	}

	private Code createCode(TagType tagType,
							int id,
							String tagName,
							String type,
							Code openingCode)
	{
		try {
			String equiv_text = null;
			TextFragment dummy = new TextFragment();
			int endStack = 1;
			StringBuilder innerCode = new StringBuilder();
			StringBuilder outerCode = null;
			outerCode = new StringBuilder();
			outerCode.append("<" + tagName);
			int count = reader.getAttributeCount();
			String prefix;
			
			equiv_text = reader.getAttributeValue(EQUIV_TEXT.getNamespaceURI(), EQUIV_TEXT.getLocalPart());
			
			for (int i = 0; i < count; i++) {
				if (!reader.isAttributeSpecified(i))
					continue; // Skip defaults
				prefix = reader.getAttributePrefix(i);
				outerCode.append(" ");
				if ((prefix != null) && (prefix.length() != 0))
					outerCode.append(prefix + ":");
				outerCode.append(reader.getAttributeLocalName(i));
				outerCode.append("=\"");
				outerCode.append(Util.escapeToXML(reader.getAttributeValue(i), 3, params.getEscapeGT(), null));
				outerCode.append("\"");				
			}
			
//			if (equiv_text != null) {
//				int x =1 ;			
//			}
				
			outerCode.append(">");
			boolean inSub = false;
			boolean hasSub = false;

			int eventType;
			while (reader.hasNext()) {
				eventType = reader.next();
				switch (eventType) {
				case XMLStreamConstants.START_ELEMENT:
					if (inSub) {
						// Should not occur
						throw new OkapiException("Unexpected state in processing sub.");
					}

					if (!inSub && reader.getLocalName().equals("sub")) {
						inSub = true;
					}
					else if (tagName.equals(reader.getLocalName())) {
						endStack++; // Take embedded elements into account
					}

					String tmpg = xliffFilter.buildStartCode(reader);
					if (!inSub)
						innerCode.append(tmpg.toString());
					outerCode.append(tmpg.toString());

					if (inSub) {
						// Store the inner/out codes before the subflow text
						Code code = dummy.append(tagType, type, innerCode.toString(), id);
						code.setOuterData(outerCode.toString());
						List<Object> chunks = xliffFilter.processSub(reader);
						for (Object obj : chunks) {
							if (obj instanceof String) {
								dummy.append((String) obj);
							}
							else if (obj instanceof Code) {
								dummy.append((Code) obj);
							}
						}
						innerCode.setLength(0);
						outerCode.append("</sub>");
						inSub = false;
						hasSub = true;
					}
					break;

				case XMLStreamConstants.END_ELEMENT:
					if (inSub) { // Should not occur
						throw new OkapiException("Unexpected state in processing sub.");
					}					
					if (tagName.equals(reader.getLocalName())) {
						if (--endStack == 0) {
							String codeType = createCodeType(innerCode.toString(), type, openingCode);

							// Use extraId if the code had a sub element
							Code code = dummy.append(tagType, codeType, innerCode.toString(),
									(hasSub ? --extraId : id));
							if (!hasSub && (innerCode.length() == 0)) {
								// Replace '>' by '/>'
								outerCode.insert(outerCode.length() - 1, '/');
							}
							else
								outerCode.append("</" + tagName + ">");
							
							// single code holds everything, including embedded codes
							// so far only seen sub tags (pissibly multiple)
							code.setOuterData(outerCode.toString());
							code.setData(dummy.toText());
							code.setDisplayText(equiv_text);
							return code;
						}
						// Else: fall thru
					}
					// Else store the close tag in the outer code
					prefix = reader.getPrefix();
					if ((prefix == null) || (prefix.length() == 0)) {
						innerCode.append("</" + reader.getLocalName() + ">");
						outerCode.append("</" + reader.getLocalName() + ">");
					}
					else {
						innerCode.append("</" + prefix + ":" + reader.getLocalName() + ">");
						outerCode.append("</" + prefix + ":" + reader.getLocalName() + ">");
					}
					break;

				case XMLStreamConstants.CHARACTERS:
				case XMLStreamConstants.CDATA:
				case XMLStreamConstants.SPACE:
					// TODO: escape unsupported chars
					innerCode.append(reader.getText());
					outerCode.append(Util.escapeToXML(reader.getText(), 0, params.getEscapeGT(), null));
					break;
				}
			}
		} catch (XMLStreamException e) {
			throw new OkapiIOException(e);
		}
		return null; // Not used as the exit is in the loop.
	}

	private String createCodeType(String data, String dataStartElementName, Code openingCode) throws XMLStreamException {

		return (null == openingCode)
				? CodeTypeFactory.createCodeType(data, dataStartElementName)
				: openingCode.getType();
	}
}