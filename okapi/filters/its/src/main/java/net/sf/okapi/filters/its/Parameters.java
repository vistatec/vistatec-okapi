/*===========================================================================
  Copyright (C) 2008-2013 by the Okapi Framework contributors
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

package net.sf.okapi.filters.its;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import net.sf.okapi.common.DefaultEntityResolver;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.Namespaces;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.encoder.XMLEncoder;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.filters.InlineCodeFinder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.its.NSContextManager;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class Parameters implements IParameters {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private static final String PROTECTENTITYREF = "protectEntityRef";
	private static final String LINEBREAKASCODE = "lineBreakAsCode";
	private static final String USECODEFINDER = "useCodeFinder";
	private static final String OMITXMLDECLARATION = "omitXMLDeclaration";
	private static final String ESCAPEQUOTES = "escapeQuotes";
	private static final String EXTRACTIFONLYCODES = "extractIfOnlyCodes";
	private static final String MAPANNOTATIONS = "mapAnnotations";
	private static final String INLINECDATA = "inlineCdata";

	private static final String OKP_NS_PREFIX = "okp";
	private static final String OKP_NS_URI = "okapi-framework:xmlfilter-options";
	
	private final static String DEFAULTS = "<?xml version='1.0' ?>\n"
		+ "<its:rules version='1.0'\n"
		+ " xmlns:its='"+Namespaces.ITS_NS_URI+"'\n"
		+ " xmlns:"+Namespaces.XLINK_NS_PREFIX+"='"+Namespaces.XLINK_NS_URI+"'\n"
		+ " xmlns:"+Namespaces.ITSX_NS_PREFIX+"='"+Namespaces.ITSX_NS_URI+"'\n"
		+ " xmlns:"+OKP_NS_PREFIX+"='"+OKP_NS_URI+"'\n"
		+ ">\n"
		+ "<!-- See ITS specification at: http://www.w3.org/TR/its/ -->\n"
		+ "</its:rules>\n";
	
	private String path;
	private URI docURI;
	private Document doc;
	private DocumentBuilder docBuilder;
	private XPath xpath;

	public boolean useCodeFinder;
	public InlineCodeFinder codeFinder;
	public boolean escapeGT;
	public boolean escapeNbsp;
	public boolean protectEntityRef;
	public boolean escapeLineBreak;
	public boolean lineBreakAsCode;
	public boolean omitXMLDeclaration;
	public boolean escapeQuotes;
	public boolean extractIfOnlyCodes;
	public boolean mapAnnotations;
	public boolean inlineCdata;
	// Write-only parameters
	public boolean quoteModeDefined;
	public int quoteMode;
	public String simplifierRules;
	
	public Parameters () {
		reset();
		// Create the document builder factory
		DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
		fact.setNamespaceAware(true);
		fact.setValidating(false);
		
		// security concern. Turn off DTD processing
		// https://www.owasp.org/index.php/XML_External_Entity_%28XXE%29_Processing
		try {
			// Xerces 1 - http://xerces.apache.org/xerces-j/features.html#external-general-entities
			// Xerces 2 - http://xerces.apache.org/xerces2-j/features.html#external-general-entities
			fact.setFeature("http://xml.org/sax/features/external-general-entities", false);
			 
			// Xerces 2 only - http://xerces.apache.org/xerces-j/features.html#external-general-entities
			fact.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			 
			} catch (ParserConfigurationException e) {
				// Tried an unsupported feature. This may indicate that a different XML processor is being
				// used. If so, then its features need to be researched and applied correctly.
				// For example, using the Xerces 2 feature above on a Xerces 1 processor will throw this
				// exception.
				logger.warn("Unsupported DocumentBuilderFactory feature. Possible security vulnerabilities.", e);
			}
		// Create the document builder
		try {
			docBuilder = fact.newDocumentBuilder();
		}
		catch ( ParserConfigurationException e ) {
			throw new OkapiIOException(e);
		}
		
		docBuilder.setEntityResolver(new DefaultEntityResolver());

		// Macintosh work-around
		// When you use -XstartOnFirstThread as a java -Xarg on Leopard, your ContextClassloader gets set to null.
		// That is not the case on 10.4 or with Windows or Linux flavors
		// This allows XPathFactory.newInstance() to have a non-null context
		//Removed because not needed any more (1.7 not supported by 10.5)
		//Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
		// end work-around
		XPathFactory xpFact = Util.createXPathFactory();
		xpath = xpFact.newXPath();
		NSContextManager nsContext = new NSContextManager();
		nsContext.addNamespace(OKP_NS_PREFIX, OKP_NS_URI);
		xpath.setNamespaceContext(nsContext);
	}

	@Override
	public String toString () {
		if ( doc == null ) {
			return DEFAULTS;
		}
		StringWriter sw = new StringWriter();
		Result result = new StreamResult(sw);
		// Write the DOM document to the file
		Transformer trans;
		try {
			trans = TransformerFactory.newInstance().newTransformer();
			trans.transform(new DOMSource(doc), result);
		}
		catch ( TransformerConfigurationException e ) {
			throw new OkapiIOException(e);
        }
		catch ( TransformerException e ) {
			throw new OkapiIOException(e);
		}
		return sw.toString();
	}
	
	@Override
	public void fromString (String data) {
		docURI = null;
		path = null;
		try {
			doc = docBuilder.parse(new InputSource(new StringReader(data)));
			getFilterOptions();
		}
		catch ( SAXException e ) {
			throw new OkapiIOException(e);
		}
		catch ( IOException e ) {
			throw new OkapiIOException(e);
		}
		catch ( XPathExpressionException e ) {
			throw new OkapiIOException(e);
		}
	}

	@Override
	public String getPath () {
		return path;
	}
	
	@Override
	public void setPath (String filePath) {
		path = filePath;
	}

	@Override
	public void load (URL inputURL,
			boolean ignoreErrors) {
		try {
			String tmp = inputURL.toString();
			if ( tmp.startsWith("jar:") ) {
				doc = docBuilder.parse(inputURL.openStream());
			}
			else {
				doc = docBuilder.parse(inputURL.toURI().toString());
			}
			path = inputURL.toURI().getPath();
			docURI = inputURL.toURI();
			getFilterOptions();
		}
		catch ( URISyntaxException e ) {
			throw new OkapiIOException(e);
		}
		catch ( UnsupportedEncodingException e ) {
			throw new OkapiIOException(e);
		}
		catch ( SAXException e ) {
			throw new OkapiIOException(e);
		}
		catch ( IOException e ) {
			throw new OkapiIOException(e);
		}
		catch ( XPathExpressionException e ) {
			throw new OkapiIOException(e);
		}
	}
	
	@Override
	public void load(InputStream inStream, boolean ignoreErrors) {
		try {
			doc = docBuilder.parse(inStream);
			getFilterOptions();
		}
		catch ( MalformedURLException e ) {
			throw new OkapiIOException(e);
		}
		catch ( UnsupportedEncodingException e ) {
			throw new OkapiIOException(e);
		}
		catch ( SAXException e ) {
			throw new OkapiIOException(e);
		}
		catch ( IOException e ) {
			throw new OkapiIOException(e);
		}
		catch ( XPathExpressionException e ) {
			throw new OkapiIOException(e);
		}		
	}

	@Override
	public void reset () {
		doc = null;
		docURI = null;
		codeFinder = new InlineCodeFinder();
		useCodeFinder = false;
		codeFinder.reset();
		escapeGT = true;
		escapeNbsp = true;
		protectEntityRef = true;
		escapeLineBreak = false;
		lineBreakAsCode = false;
		omitXMLDeclaration = false;
		// Forced write-only default options
		quoteModeDefined = true;
		quoteMode = 3; // escape quot, not apos
		// Quote escaping option
		escapeQuotes = true;
		extractIfOnlyCodes = true;
		mapAnnotations = true;
		inlineCdata = false;
		simplifierRules = null;
	}

	@Override
	public void save (String filePath) {
		Result result = null;
		Transformer trans = null;
		try {
			if ( doc == null ) {
				fromString(DEFAULTS);
			}
			// Prepare the output file
			File f = new File(filePath);
			result = new StreamResult(f);
			// Write the DOM document to the file
			try {
				trans = TransformerFactory.newInstance().newTransformer();
				trans.transform(new DOMSource(doc), result);
			}
			catch ( TransformerConfigurationException e ) {
				throw new OkapiIOException(e);
	        }
			catch ( TransformerException e ) {
				throw new OkapiIOException(e);
			}
			// Update path and URI
			path = filePath;
			docURI = f.toURI();
		}
		finally {
			// Try to make sure the file is not locked
			trans = null;
			result = null;
			System.gc();
		}
	}

	public Document getDocument () {
		return doc;
	}
	
	public URI getURI () {
		return docURI;
	}

	@Override
	public boolean getBoolean (String name) {
		if ( name.equals(XMLEncoder.ESCAPEGT) ) return escapeGT;
		if ( name.equals(XMLEncoder.ESCAPENBSP) ) return escapeNbsp;
		if ( name.equals(PROTECTENTITYREF) ) return protectEntityRef;
		if ( name.equals(XMLEncoder.ESCAPELINEBREAK) ) return escapeLineBreak;
		if ( name.equals(LINEBREAKASCODE) ) return lineBreakAsCode;
		if ( name.equals(OMITXMLDECLARATION) ) return omitXMLDeclaration;
		if ( name.equals(XMLEncoder.QUOTEMODEDEFINED) ) return quoteModeDefined;
		if ( name.equals(ESCAPEQUOTES) ) return escapeQuotes;
		if ( name.equals(EXTRACTIFONLYCODES) ) return extractIfOnlyCodes;
		if ( name.equals(MAPANNOTATIONS) ) return mapAnnotations;
		if ( name.equals(INLINECDATA) ) return inlineCdata;

		return false;
	}

	@Override
	public void setBoolean (String name, 
		boolean value)
	{
		if ( name.equals(XMLEncoder.ESCAPEGT) ) escapeGT = value;
		else if ( name.equals(XMLEncoder.ESCAPENBSP) ) escapeNbsp = value;
		else if ( name.equals(PROTECTENTITYREF) ) protectEntityRef = value;
		else if ( name.equals(XMLEncoder.ESCAPELINEBREAK) ) escapeLineBreak = value;
		else if ( name.equals(LINEBREAKASCODE) ) lineBreakAsCode = value;
		else if ( name.equals(OMITXMLDECLARATION) ) omitXMLDeclaration = value;
		else if ( name.equals(XMLEncoder.QUOTEMODEDEFINED) ) quoteModeDefined = value;
		else if ( name.equals(ESCAPEQUOTES) ) escapeQuotes = value;
		else if ( name.equals(EXTRACTIFONLYCODES) ) extractIfOnlyCodes = value;
		else if ( name.equals(MAPANNOTATIONS) ) mapAnnotations = value;
		else if ( name.equals(INLINECDATA) ) inlineCdata = value;
	}

	@Override
	public String getString (String name) {
		return null;
	}

	@Override
	public void setString (String name,
		String value)
	{
		// No string options
	}

	@Override
	public int getInteger (String name) {
		if ( name.equals(XMLEncoder.QUOTEMODE) ) return quoteMode;
		return 0;
	}

	@Override
	public void setInteger (String name,
		int value)
	{
		if ( name.equals(XMLEncoder.QUOTEMODE) ) quoteMode = value;
	}

	@Override
	public ParametersDescription getParametersDescription () {
		return null;
	}

	private void getFilterOptions () throws XPathExpressionException {
		// Read the options element
		NodeList nl = (NodeList)xpath.evaluate("//"+OKP_NS_PREFIX+":options", doc, XPathConstants.NODESET);
		if ( nl.getLength() > 0 ) {
		// One element only
			Element elem = (Element)nl.item(0);
			String tmp = elem.getAttribute(XMLEncoder.ESCAPELINEBREAK);
			if ( !Util.isEmpty(tmp) ) {
				escapeLineBreak = tmp.equals("yes");
			}
			tmp = elem.getAttribute(XMLEncoder.ESCAPENBSP);
			if ( !Util.isEmpty(tmp) ) {
				escapeNbsp = tmp.equals("yes");
			}
			tmp = elem.getAttribute(LINEBREAKASCODE);
			if ( !Util.isEmpty(tmp) ) {
				lineBreakAsCode = tmp.equals("yes");
			}
			tmp = elem.getAttribute(XMLEncoder.ESCAPEGT);
			if ( !Util.isEmpty(tmp) ) {
				escapeGT = tmp.equals("yes");
			}
			tmp = elem.getAttribute(ESCAPEQUOTES);
			if ( !Util.isEmpty(tmp) ) {
				escapeQuotes = tmp.equals("yes");
			}
			tmp = elem.getAttribute(EXTRACTIFONLYCODES);
			if ( !Util.isEmpty(tmp) ) {
				extractIfOnlyCodes = tmp.equals("yes");
			}
			tmp = elem.getAttribute(PROTECTENTITYREF);
			if ( !Util.isEmpty(tmp) ) {
				protectEntityRef = tmp.equals("yes");
			}
			tmp = elem.getAttribute(OMITXMLDECLARATION);
			if ( !Util.isEmpty(tmp) ) {
				omitXMLDeclaration = tmp.equals("yes");
			}
			tmp = elem.getAttribute(MAPANNOTATIONS);
			if ( !Util.isEmpty(tmp) ) {
				mapAnnotations = tmp.equals("yes");
			}
			tmp = elem.getAttribute(INLINECDATA);
			if ( !Util.isEmpty(tmp) ) {
				inlineCdata = tmp.equals("yes");
			}
		}
		// Get the code finder data
		nl = (NodeList)xpath.evaluate("//"+OKP_NS_PREFIX+":codeFinder", doc, XPathConstants.NODESET);
		if ( nl.getLength() > 0 ) {
			// One element only
			Element elem = (Element)nl.item(0);
			String tmp = elem.getAttribute(USECODEFINDER);
			if ( !Util.isEmpty(tmp) ) {
				useCodeFinder = tmp.equals("yes");
			}
			tmp = Util.getTextContent(elem);
			if ( tmp == null ) tmp = "";
			codeFinder.fromString(tmp);
		}
		
		// Get the code simplifier rule data
		nl = (NodeList)xpath.evaluate("//"+OKP_NS_PREFIX+":simplifierRules", doc, XPathConstants.NODESET);
		if ( nl.getLength() > 0 ) {
			// One element only
			Element elem = (Element)nl.item(0);
			simplifierRules = Util.getTextContent(elem);
		}
	}

	public String getSimplifierRules() {
		return simplifierRules;
	}

	public void setSimplifierRules(String simplifierRules) {
		this.simplifierRules = simplifierRules;
	}
}
