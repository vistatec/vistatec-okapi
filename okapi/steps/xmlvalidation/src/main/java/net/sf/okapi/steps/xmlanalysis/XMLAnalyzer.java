/*===========================================================================
  Copyright (C) 2010 by the Okapi Framework contributors
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

package net.sf.okapi.steps.xmlanalysis;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.sf.okapi.common.BOMNewlineEncodingDetector;
import net.sf.okapi.common.DefaultEntityResolver;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.XMLWriter;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.resource.RawDocument;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.TreeWalker;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Analyzer to gather information about the elements and attributes of a set of documents:
 * Which one has text content, which one are apparently within text, etc.
 */
public class XMLAnalyzer {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	class Info {
		boolean isRoot;
		boolean hasText;
		boolean withinText;
		boolean hasCDATA;
	}

	private Parameters params;
	private LinkedHashMap<String, Info> elements;
	
	public XMLAnalyzer () {
		params = new Parameters();
		reset();
	}

	public void reset () {
		elements = new LinkedHashMap<String, Info>();
	}
	
	public Parameters getParameters () {
		return params;
	}
	
	public void setParameters (Parameters params) {
		this.params = params;
	}
	
	public void analyzeDocument (RawDocument rd) {
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
			//fact.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			 
		} catch (ParserConfigurationException e) {
			// Tried an unsupported feature. This may indicate that a different XML processor is being
			// used. If so, then its features need to be researched and applied correctly.
			// For example, using the Xerces 2 feature above on a Xerces 1 processor will throw this
			// exception.
			logger.warn("Unsupported DocumentBuilderFactory feature. Possible security vulnerabilities.", e);
		}
		
		// Create the document builder
		DocumentBuilder docBuilder;
		try {
			docBuilder = fact.newDocumentBuilder();
		}
		catch (ParserConfigurationException e) {
			throw new OkapiIOException(e);
		}
		// Avoid DTD declaration
		docBuilder.setEntityResolver(new DefaultEntityResolver());

		rd.setEncoding("UTF-8"); // Default for XML, other should be auto-detected
		BOMNewlineEncodingDetector detector = new BOMNewlineEncodingDetector(rd.getStream(), rd.getEncoding());
		detector.detectBom();
		
		if ( detector.isAutodetected() ) {
			String encoding = detector.getEncoding();
			//--Start workaround issue with XML Parser
			// "UTF-16xx" are not handled as expected, using "UTF-16" alone 
			// seems to resolve the issue.
			if (( encoding.equals("UTF-16LE") ) || ( encoding.equals("UTF-16BE") )) {
				encoding = "UTF-16";
			}
			//--End workaround
			rd.setEncoding(encoding);
		}
		
		Document doc = null;
		try {
			InputSource is = new InputSource(rd.getStream());
			doc = docBuilder.parse(is);
		}
		catch ( SAXException e ) {
			throw new OkapiIOException("Error when parsing the document.", e);
		}
		catch ( IOException e ) {
			throw new OkapiIOException("Error when reading the document.", e);
		}

		Node node = doc.getDocumentElement();
        DocumentTraversal traversal = (DocumentTraversal)doc;
        TreeWalker walker = traversal.createTreeWalker(node, NodeFilter.SHOW_ALL, null, false);

        String lastElem = node.getNodeName();
        getInfo(lastElem).isRoot = true;

        while ( node != null ) {
        	// Analyze this node
        	switch ( node.getNodeType() ) {
        	case Node.ELEMENT_NODE:
        		lastElem = node.getNodeName();
        		Info info = getInfo(lastElem);
        		if ( isTextContent(node.getPreviousSibling()) ) {
        			info.withinText = true;
        		}
        		else if ( isTextContent(node.getNextSibling()) ) {
        			info.withinText = true;
        		}
        		if ( hasTextContent(node) ) {
        			info.hasText = true;
        		}
        		break;

        	case Node.CDATA_SECTION_NODE:
                getInfo(lastElem).hasCDATA = true;
                break;
        	}
        	// Move to the next node
        	node = walker.nextNode();
        }
	}
	
	public HashMap<String, Info> getResults () {
		return elements;
	}
	
	private boolean hasText (String text) {
		for ( int i=0; i<text.length(); i++ ) {
			if ( !Character.isWhitespace(text.charAt(i)) ) {
				return true;
			}
		}
		return false;
	}
	
	private boolean hasTextContent (Node parent) {
		if ( parent == null ) return false;
		Node node = parent.getFirstChild();
		while ( node != null ) {
			if ( isTextContent(node) ) return true;
			node = node.getNextSibling();
		}
		return false;
	}
	
	private boolean isTextContent (Node node) {
		if ( node == null ) return false;
		if (( node.getNodeType() == Node.TEXT_NODE ) || ( node.getNodeType() == Node.CDATA_SECTION_NODE )) {
			if ( hasText(node.getNodeValue()) ) return true;
		}
		return false;
	}
	
	private Info getInfo (String key) {
		if ( !elements.containsKey(key) ) {
			elements.put(key, new Info());
		}
		return elements.get(key);
	}
	
	public void generateOutput () {
		XMLWriter xw = null;
		try {
			xw = new XMLWriter(params.getOutputPath());
			xw.writeStartHTMLDocument("XML Analyis");
			xw.writeElementString("h1", "XML Analyis Results");
			
			xw.writeRawXML("<table cellspacing=\"0\" cellpadding=\"5\" border=\"1\">");

			xw.writeStartElement("tr");
			xw.writeRawXML(String.format("<td valign=\"top\">%s</td>", "All elements:"));
			xw.writeRawXML("<td valign=\"top\"><code>");
			for ( String name : elements.keySet() ) {
				xw.writeString(name+" ");
			}
			xw.writeRawXML("\u00a0</code></td>");
			xw.writeEndElementLineBreak(); // tr

			xw.writeStartElement("tr");
			xw.writeRawXML(String.format("<td valign=\"top\">%s</td>", "Root elements:"));
			xw.writeRawXML("<td valign=\"top\"><code>");
			for ( String name : elements.keySet() ) {
				if ( elements.get(name).isRoot ) {
					xw.writeString(name+" ");
				}
			}
			xw.writeRawXML("\u00a0</code></td>");
			xw.writeEndElementLineBreak(); // tr
			
			xw.writeStartElement("tr");
			xw.writeRawXML(String.format("<td valign=\"top\">%s</td>", "Elements with text content:"));
			xw.writeRawXML("<td valign=\"top\"><code>");
			for ( String name : elements.keySet() ) {
				if ( elements.get(name).hasText ) {
					xw.writeString(name+" ");
				}
			}
			xw.writeRawXML("\u00a0</code></td>");
			xw.writeEndElementLineBreak(); // tr

			xw.writeStartElement("tr");
			xw.writeRawXML(String.format("<td valign=\"top\">%s</td>", "Elements within text (inline):"));
			xw.writeRawXML("<td valign=\"top\"><code>");
			for ( String name : elements.keySet() ) {
				if ( elements.get(name).withinText ) {
					xw.writeString(name+" ");
				}
			}
			xw.writeRawXML("\u00a0</code></td>");
			xw.writeEndElementLineBreak(); // tr
			
			xw.writeStartElement("tr");
			xw.writeRawXML(String.format("<td valign=\"top\">%s</td>", "Elements with CDATA:"));
			xw.writeRawXML("<td valign=\"top\"><code>");
			for ( String name : elements.keySet() ) {
				if ( elements.get(name).hasCDATA ) {
					xw.writeString(name+" ");
				}
			}
			xw.writeRawXML("\u00a0</code></td>");
			xw.writeEndElementLineBreak(); // tr
			
			xw.writeRawXML("</table>");
		}
		finally {
			if ( xw != null ) {
				xw.writeEndDocument();
				xw.close();
			}
			// Open the output if requested
			if ( params.getAutoOpen() ) {
				Util.openURL((new File(params.getOutputPath())).getAbsolutePath());
			}
		}
	}

}
