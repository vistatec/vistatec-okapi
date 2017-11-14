/*===========================================================================
  Copyright (C) 2009-2012 by the Okapi Framework contributors
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

package net.sf.okapi.filters.html;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.util.LinkedHashMap;

import net.htmlparser.jericho.Attribute;
import net.htmlparser.jericho.Attributes;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.StartTag;
import net.htmlparser.jericho.StartTagType;
import net.htmlparser.jericho.StreamedSource;
import net.htmlparser.jericho.Tag;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.UnicodeBOMWriter;
import net.sf.okapi.common.resource.RawDocument;

/**
 * Copy HTML/XML files using Jericho and perform transformations on the copied content.
 * @author HargraveJE
 *
 */
class StreamedSourceCopy {
	/**
	 * Rewrite input and add quotes to any attributes that don't have them. Also add missing META tags
	 * @param input - {@link RawDocument} input
	 * @param documentEncoding - does the document have a hard coded encoding?
	 * @param encoding - detected or set encoding for the content.
	 * @param hasBOM - does the content contain a Byte Order Mark?
	 * @return the transformed {@link RawDocument}
	 * @throws IOException
	 */
	public static RawDocument htmlTidiedRewrite(RawDocument input,
			boolean documentEncoding, String encoding, boolean hasBOM) throws IOException {
		LocaleId locale = input.getSourceLocale();
		File t = File.createTempFile("~okapi-html-filter-preprocess", ".sourceTemp");
		URI tempUri = t.toURI();

		boolean needEncodingDeclaration = !documentEncoding;
		boolean isXHTML = false;
		
		// make a new source copy with tidied tags and add any missing meta tags
		Writer writer = null;
		StreamedSource streamedSource = null;
		try {
			if (input.getEncoding() == RawDocument.UNKOWN_ENCODING) {
				// set detected encoding (this could be incorrect)
				input.setEncoding(encoding);
			}

			streamedSource = new StreamedSource(input.getReader());
						
			// output BOM if needed
			if (hasBOM) {
				writer = new UnicodeBOMWriter(new FileOutputStream(new File(
						tempUri.getPath())), encoding);
			} else {
				writer = new OutputStreamWriter(new FileOutputStream(new File(
						tempUri.getPath())), encoding);
			}
			
			int lastSegmentEnd = 0;
			for (Segment segment : streamedSource) {				
				if (segment.getEnd() <= lastSegmentEnd)
					continue; // if this tag is inside the previous tag (e.g. a
								// server tag) then ignore it as it was already
								// output along with the previous tag.
				lastSegmentEnd = segment.getEnd();
				if (segment instanceof Tag) {
					Tag tag = (Tag) segment;
					if (tag instanceof StartTag) {
						StartTag st = (StartTag) tag;						
						if (tagHasUnquotedAttribute(st)) {
							LinkedHashMap<String, String> attributesMap = new LinkedHashMap<String, String>();
							// rewrite tag with quoted attributes
							writer.write(StartTag.generateHTML(
									st.getNameSegment().toString(),
									st.getAttributes().populateMap(attributesMap, false),
									st.isSyntacticalEmptyElementTag()));
						} else {
							// rewrite tag as-is
							writer.write(st.toString());
						}
						
						// If needed: add the encoding declaration just after <head>
						// (If there is a <head> in the file, this is not triggered.
						if ( needEncodingDeclaration ) {
							// Check for XHTML files
							if ( st.getName() == HTMLElementName.HTML ) {
								// If this is an XHTML file it does not need an added meta declaration
								Attributes attrs = st.getAttributes();
								for ( int i=0; i<attrs.getCount(); i++ ) {
									String name = attrs.get(i).getName();
									if ( name.equals("xmlns") || name.startsWith("xmlns:") ) {
										String xmlns = st.getAttributeValue(name);
										if ( xmlns == null ) {
											// It's likely HTML
											// We can add <meta> (not <meta/>)
										}
										else if ( xmlns.equals("http://www.w3.org/1999/xhtml") ) {
											// It's XHTML
											// We can add <meta/>. It should be ok with both strict and transitional XHTML
											isXHTML = true;
										}
										else {
											// It's some other XML format
											// We shouldn't add <meta/>
											needEncodingDeclaration = false;
										}
									}
								}
							}
							else if ( st.getName() == HTMLElementName.HEAD ) {
								// Insert the encoding declaration
								writer.write(String.format( // Use <meta /> for HTML and XHTML
									"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=%s\"%s>",
									(input.getEncoding()==RawDocument.UNKOWN_ENCODING ? encoding : input.getEncoding()),
									(isXHTML ? " /" : "") ));
								needEncodingDeclaration = false;
							}
						}
						
						continue;
					}
				}
				writer.write(segment.toString());
			}			
			return new RawDocument(tempUri, encoding, locale);
		}
		finally {
			input.close();			
			if (streamedSource != null) {
				streamedSource.close();
			}
			if (writer != null)
				try {
					writer.close();
				} catch (IOException ex) {
				}
		}
	}
	
	private static boolean tagHasUnquotedAttribute(StartTag tag) {
		if (tag.getAttributes() != null
				&& !tag.getAttributes().isEmpty()
				&& tag.getTagType() != StartTagType.XML_DECLARATION
				&& tag.getTagType() != StartTagType.DOCTYPE_DECLARATION
				&& tag.getTagType() != StartTagType.XML_PROCESSING_INSTRUCTION) {
			
			for (Attribute att : tag.getAttributes()) {
				if (att.getQuoteChar() == ' ') {
					return true;
				}
			}
		}
		
		return false;
	}
}
