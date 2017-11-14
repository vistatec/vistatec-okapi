/*===========================================================================
  Copyright (C) 2015 by the Okapi Framework contributors
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

package net.sf.okapi.filters.icml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.StreamUtil;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.resource.IReferenceable;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.StartGroup;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.skeleton.ISkeletonWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
public class ICMLFilterWriter implements IFilterWriter {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final DocumentBuilder docBuilder;
	private Document docOriginal;
	private String outputPath;
	private LocaleId trgLoc;
	private File tempFile;
	private byte[] buffer;
	private Transformer xformer;
	private Document doc;
	private OutputStream xmlOutStream;
	private int group;
	private Stack<IReferenceable> referents;
	private ArrayList<String> storiesLeft;
	private StringBuilder xml;
	private boolean reconstructing; // Re-building a full element that was forced in several TUs
	private OutputStream outputStream;
	
	public ICMLFilterWriter () {
        try {
        	DocumentBuilderFactory docFact = DocumentBuilderFactory.newInstance();
    		docFact.setValidating(false);
    		
    		// security concern. Turn off DTD processing
			// https://www.owasp.org/index.php/XML_External_Entity_%28XXE%29_Processing
			try {
				// Xerces 1 - http://xerces.apache.org/xerces-j/features.html#external-general-entities
				// Xerces 2 - http://xerces.apache.org/xerces2-j/features.html#external-general-entities
				docFact.setFeature("http://xml.org/sax/features/external-general-entities", false);
				 
				// Xerces 2 only - http://xerces.apache.org/xerces-j/features.html#external-general-entities
				docFact.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
				 
				} catch (ParserConfigurationException e) {
					// Tried an unsupported feature. This may indicate that a different XML processor is being
					// used. If so, then its features need to be researched and applied correctly.
					// For example, using the Xerces 2 feature above on a Xerces 1 processor will throw this
					// exception.
					logger.warn("Unsupported DocumentBuilderFactory feature. Possible security vulnerabilities.", e);
				}
    					
    		docBuilder = docFact.newDocumentBuilder();
			xformer = TransformerFactory.newInstance().newTransformer();
		}
		catch ( Throwable e ) {
			throw new OkapiIOException("Error initializing.\n"+e.getMessage(), e);
		}
	}
	
	@Override
	public void cancel () {
		// TODO
	}

	@Override
	public void close () 
	{
		try 
		{
			docOriginal = null;
			if ( xmlOutStream == null ) return; // Was closed already
			
			// Close the output
			xmlOutStream.close();
			xmlOutStream = null;
			buffer = null;

			// If it was in a temporary file, copy it over the existing one
			// If the IFilter.close() is called before IFilterWriter.close()
			// this should allow to overwrite the input.
			if ( tempFile != null ) 
			{
				StreamUtil.copy(new FileInputStream(tempFile), outputPath);
			}
		}
		catch ( IOException e ) {
			throw new OkapiIOException("Error closing ICML outpiut.\n"+e.getMessage(), e);
		}
	}

	@Override
	public EncoderManager getEncoderManager () {
		return null;
	}

	@Override
	public ISkeletonWriter getSkeletonWriter () {
		return null;
	}

	@Override
	public String getName () {
		return "ICMLFilterWriter";
	}

	@Override
	public IParameters getParameters () {
		return null; // Not used
	}

	@Override
	public Event handleEvent (Event event) {
		switch ( event.getEventType() ) {
		case START_DOCUMENT:
			processStartDocument(event.getStartDocument());
			break;
		case END_DOCUMENT:
			processEndDocument();
			break;
		case START_GROUP:
		case START_SUBFILTER:
			processStartGroup(event.getStartGroup());
			break;
		case END_GROUP:
		case END_SUBFILTER:
			processEndGroup(event.getEndGroup());
			break;
		case TEXT_UNIT:
			processTextUnit(event.getTextUnit());
			break;
		default:
			break;
		}
		return event;
	}

	@Override
	public void setOptions (LocaleId locale,
		String defaultEncoding)
	{
		trgLoc = locale;
		// Ignore encoding. We always use UTF-8
	}

	@Override
	public void setOutput (String path) {
		outputPath = path;
	}

	@Override
	public void setOutput (OutputStream output) {
		this.outputStream = output;
	}

	@Override
	public void setParameters (IParameters params) {
		// Not used
	}

	private void processStartDocument (StartDocument res) {
		try {
			// This will be used throughout the writting
			ICMLSkeleton skel = (ICMLSkeleton)res.getSkeleton();
			docOriginal = skel.getOriginal();
			group = 0;
			referents = new Stack<IReferenceable>();
			storiesLeft = new ArrayList<String>();
			reconstructing = false;
		
			// Create the output stream from the path provided
			tempFile = null;			
			boolean useTemp = false;
			xmlOutStream = outputStream;
			if (xmlOutStream == null) {							
				File f = new File(outputPath);
				if ( f.exists() ) {
					// If the file exists, try to remove
					useTemp = !f.delete();
				}
				if ( useTemp ) {
					// Use a temporary output if we cannot overwrite for now
					// If it's the input file, IFilter.close() will free it before we
					// call close() here (that is if IFilter.close() is called correctly!)
					tempFile = File.createTempFile("icmlTmp", null);
					xmlOutStream = new FileOutputStream(tempFile.getAbsolutePath());
				}
				else { // Make sure the directory exists
					Util.createDirectories(outputPath);
					xmlOutStream = new FileOutputStream(outputPath);
				}
			}
			
			// Create buffer for transfer
			buffer = new byte[2048];
			
			storiesLeft.add(docOriginal.getBaseURI());
		}
		catch ( IOException e ) {
			throw new OkapiIOException("Error creating output ICML.\n"+e.getMessage(), e);
		}
	}
	
	private InputStream document2InputStream(Document document)    throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		Source xmlSource = new DOMSource(document);
		Result outputTarget = new StreamResult(outputStream);
		try {
			TransformerFactory.newInstance().newTransformer().transform(xmlSource, outputTarget);
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			e.printStackTrace();
		}
		InputStream is = new ByteArrayInputStream(outputStream.toByteArray());
		return is;
	 }

	private void processEndDocument () {
		try {
			if ( storiesLeft != null ) 
			{
				if ( storiesLeft.contains(docOriginal.getBaseURI()) ) 
				{
					InputStream input = document2InputStream(docOriginal); 
					
					int len;
					while ( (len = input.read(buffer)) > 0 ) {
						xmlOutStream.write(buffer, 0, len);
					}
					input.close();
					storiesLeft.remove(docOriginal.getBaseURI());
				}
			}
		}
		catch ( IOException e ) {
			throw new OkapiIOException("Error writting out non-extracted stories.", e);
		}
		close();
	}

	private void processTextUnit (ITextUnit tu) {
		// If it's a referent, just store it for now.
		// It'll be merged when the inline code with the reference to it is merged
		if ( tu.isReferent() ) {
			referents.push(tu);
		}
		else { // Otherwise: merge the text unit now
			while ( !referents.isEmpty() ) {
				mergeTextUnit((ITextUnit)referents.pop());
			}
			mergeTextUnit(tu);
		}
	}
	
	private void mergeTextUnit (ITextUnit tu) {
		ICMLSkeleton skel = (ICMLSkeleton)tu.getSkeleton();
		
		// Get the target content, or fall back to the source
		// Make a copy to not change the original in the resource
		TextContainer tc = tu.getTarget(trgLoc);
		if ( tc == null ) tc = tu.getSource();
		TextFragment tf = tc.getUnSegmentedContentCopy();
		
		// Escape the text of the content to XML
		// inline codes are still in XML so we don't touch them
		String ctext = tf.getCodedText();
		ctext = Util.escapeToXML(ctext, 3, false, null);
		// Set the modified coded text
		tf.setCodedText(ctext);
		
		// Now the whole content is true XML, it can be parsed as a fragment
		if ( reconstructing ) {
			// Append to the existing XML buffer 
		}
		else {
			xml = new StringBuilder("<r>");
		}
		reconstructing = skel.getForced();
		
		// If there were moved inline codes: we put them back, so we have valid XML
		TextFragment[] res = skel.getMovedParts();
		if (( res != null ) && ( res[0] != null )) {
			xml.append(res[0].toText());
		}
		xml.append(tf.toText());
		if (( res != null ) && ( res[1] != null )) {
			xml.append(res[1].toText());
		}

		if ( reconstructing ) {
			return; // Done for now
			// Needs the next TU(s) to finish the reconstruction
		}
		else {
			xml.append("</r>");
		}
		
		try {
			Document tmpDoc =  docBuilder.parse(new InputSource(new StringReader(xml.toString())));
			Document docWhereToImport = skel.getScopeNode().getOwnerDocument();
			DocumentFragment docFrag = docWhereToImport.createDocumentFragment();
			
			Node imp = docWhereToImport.importNode(tmpDoc.getDocumentElement(), true);
			while ( imp.hasChildNodes() ) {
				docFrag.appendChild(imp.removeChild(imp.getFirstChild()));
			}

			// Get live nodes
			HashMap<String, Node> map = collectLiveReferents(skel); 
			
			// Remove the old content
			// (Reference nodes have been cloned in the skeleton)
			Node node = skel.getScopeNode();
			while( node.hasChildNodes() ) {
				node.removeChild(node.getFirstChild());  
			}
			
			// Attach the new content
			node.appendChild(docFrag);
			
			// If needed, re-inject the nodes referenced in the inline codes.
			if ( map != null ) {
				// Re-inject the reference nodes using the skeleton copies
				NodeList list = ((Element)node).getElementsByTagName(ICMLSkeleton.NODEREMARKER);
				// The list is dynamic, so replacing the node, decrease the list
				while ( list.getLength() > 0 ) {
					Element marker = (Element)list.item(0);
					String key = marker.getAttribute("id");
					Node original = map.get(key);
					if ( original == null ) {
						logger.error(String.format("Missing original node for a reference in text unit id='%s'.", tu.getId()));
						break; // Break now or we'll be in an infinite loop
					}
					Element parent = (Element)marker.getParentNode();
					parent.replaceChild(original, marker);
				}
			}
		}
		catch ( Throwable e ) {
			throw new OkapiIOException(String.format(
				"Error when parsing XML of text unit id='%s'.\n" + e.getMessage(), tu.getId()), e);
		}
	}
	
	private HashMap<String, Node> collectLiveReferents (ICMLSkeleton skel) {
		if ( !skel.hasReferences() ) return null;
		// Create an empty list to store the live nodes
		HashMap<String, Node> map = new HashMap<String, Node>();
		Element elem = (Element)skel.getTopNode();
		// Get the list of references
		HashMap<String, NodeReference> refs = skel.getReferences();
		for ( String key : refs.keySet() ) {
			NodeReference ref = refs.get(key);
			NodeList list = elem.getElementsByTagName(ref.name);
			Node ori = list.item(ref.position).cloneNode(true);
			map.put(key, ori);
		}
		return map;
	}
	
	private void processStartGroup (StartGroup res) {
		reconstructing = false;
		xml = null;
		group++;
		ICMLSkeleton skel = (ICMLSkeleton)res.getSkeleton();
		if ( skel == null ) return; // Not a story group
		// Store the entry data to process the text units
		// Everything will be written at the end group.
		doc = skel.getDocument();
	}
	
	private void processEndGroup (Ending ending) {
		// Merge any remaining TU
		while ( !referents.isEmpty() ) {
			mergeTextUnit((ITextUnit)referents.pop());
		}

		group--;
		try {
			if ( group != 1 ) {
				// Not a story group
				return;
			}
		}
		catch ( TransformerFactoryConfigurationError e ) {
			throw new OkapiIOException("Transform configuration error.\n"+e.getMessage(), e);
		}
	}

}
