/*===========================================================================
  Copyright (C) 2012-2013 by the Okapi Framework contributors
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

package net.sf.okapi.filters.its.html5;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;

import net.sf.okapi.common.BOMNewlineEncodingDetector;
import net.sf.okapi.common.FileUtil;
import net.sf.okapi.common.MimeTypeMapper;
import net.sf.okapi.common.Namespaces;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.filters.FilterConfiguration;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.skeleton.GenericSkeleton;
import net.sf.okapi.common.skeleton.ISkeletonWriter;
import net.sf.okapi.filters.its.ITSFilter;
import net.sf.okapi.filters.its.Parameters;
import nu.validator.htmlparser.dom.HtmlDocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.its.IProcessor;
import org.w3c.its.ITSEngine;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@UsingParameters(Parameters.class)
public class HTML5Filter extends ITSFilter {

	public HTML5Filter () {
		super(true, MimeTypeMapper.HTML_MIME_TYPE, IProcessor.DC_ALL);
		InputStream inStream = getClass().getResourceAsStream("default.fprm");
		params.load(inStream, false);
	}

	@Override
	public String getName () {
		return "okf_itshtml5";
	}
	
	@Override
	public String getDisplayName () {
		return "HTML5-ITS Filter";
	}

	@Override
	public List<FilterConfiguration> getConfigurations () {
		List<FilterConfiguration> list = new ArrayList<FilterConfiguration>();
		list.add(new FilterConfiguration(getName(),
			MimeTypeMapper.HTML_MIME_TYPE,
			getClass().getName(),
			"Standard HTML5",
			"Configuration for standard HTML5 documents.",
			"default.fprm",
			".html;.htm;"));
		return list;
	}
	
	@Override
	public ISkeletonWriter createSkeletonWriter() {
		return new HTML5SkeletonWriter();
	}

	@Override
	public EncoderManager getEncoderManager () {
		if ( encoderManager == null ) {
			encoderManager = new EncoderManager();
			encoderManager.setMapping(getMimeType(), "net.sf.okapi.common.encoder.HtmlEncoder");
		}
		return encoderManager;
	}
	
	@Override
	protected void initializeDocument () {
		input.setEncoding("UTF-8"); // Default for HTML5, other should be auto-detected
		BOMNewlineEncodingDetector detector = new BOMNewlineEncodingDetector(input.getStream(), input.getEncoding());
		detector.detectBom();
		
		if ( detector.isAutodetected() ) {
			encoding = detector.getEncoding();
			//--Start workaround issue with XML Parser
			// "UTF-16xx" are not handled as expected, using "UTF-16" alone 
			// seems to resolve the issue.
			if (( encoding.equals("UTF-16LE") ) || ( encoding.equals("UTF-16BE") )) {
				encoding = "UTF-16";
			}
			//--End workaround
			input.setEncoding(encoding);
		}
		
		HtmlDocumentBuilder docBuilder = new HtmlDocumentBuilder();
		try {
			InputSource is = new InputSource(input.getStream());
			is.setEncoding(input.getEncoding());
			doc = docBuilder.parse(is);
		}
		catch ( SAXException e ) {
			throw new OkapiIOException("Error when parsing the document.\n"+e.getMessage(), e);
		}
		catch ( IOException e ) {
			throw new OkapiIOException("Error when reading the document.\n"+e.getMessage(), e);
		}

		encoding = doc.getXmlEncoding();
		if ( encoding == null ) {
			encoding = detector.getEncoding();
		}
		srcLang = input.getSourceLocale();
		if ( srcLang == null ) throw new NullPointerException("Source language not set.");
		hasUTF8BOM = detector.hasUtf8Bom();
		lineBreak = detector.getNewlineType().toString();
		if ( input.getInputURI() != null ) {
			docName = input.getInputURI().getPath();
		}

	}

	@Override
	protected void applyRules (ITSEngine itsEng) {
		// Check for links in the HTML5 document
		loadLinkedRules(doc, input.getInputURI(), itsEng);
		// Apply the rules (external and internal) to the document
		super.applyRules(itsEng);
	}

	@Override
	protected void createStartDocumentSkeleton (StartDocument startDoc) {
		// Add the XML declaration
		skel = new GenericSkeleton();
		skel.add("<!DOCTYPE html>"+lineBreak);
	}
	
	/**
	 * Loads the linked rules of an HTML document.
	 * @param doc the document to process.
	 * @param docURI the document URI.
	 * @param itsEng the engine to use.
	 */
	public static void loadLinkedRules (Document doc,
		URI docURI,
		ITSEngine itsEng)
	{
		String href = null;
		try {
			XPathExpression expr= itsEng.getXPath().compile("//"+Namespaces.HTML_NS_PREFIX+":link[@rel='its-rules']");
			NodeList list = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
			for ( int i=0; i<list.getLength(); i++ ) {
				Element elem = (Element)list.item(i);
				// get the HREF value (could be surrounded by spaces, so we trim)
				href = elem.getAttribute("href").trim();
				if (( href.indexOf('/') == -1 ) && ( href.indexOf('\\') == -1 )) {
					String base = FileUtil.getPartBeforeFile(docURI);
					href = base + href;
				}
				itsEng.addExternalRules(new URI(href));
			}
		}
		catch ( Throwable e ) {
			throw new OkapiIOException(String.format(
				"Error trying to load external rules (%s).\n"+e.getMessage(), href));
		}
	}

}
