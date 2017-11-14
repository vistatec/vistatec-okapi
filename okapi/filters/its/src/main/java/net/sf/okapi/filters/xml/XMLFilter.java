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

package net.sf.okapi.filters.xml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.sf.okapi.common.BOMNewlineEncodingDetector;
import net.sf.okapi.common.DefaultEntityResolver;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.MimeTypeMapper;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.filters.FilterConfiguration;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.skeleton.GenericSkeleton;
import net.sf.okapi.common.skeleton.GenericSkeletonWriter;
import net.sf.okapi.common.skeleton.ISkeletonWriter;
import net.sf.okapi.filters.its.ITSFilter;
import net.sf.okapi.filters.its.Parameters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DocumentType;
import org.w3c.its.IProcessor;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@UsingParameters(Parameters.class)
public class XMLFilter extends ITSFilter {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	public XMLFilter () {
		super(false, MimeTypeMapper.XML_MIME_TYPE, IProcessor.DC_ALL);
	}

	/**
	* Constructor for sub-classing with a different MIME type.
	* <p>Sub-classes that override {@link #getMimeType()} should call this constructor
	* with the same MIME type as defined in their {@link #getMimeType()} method.
	* @param mimeType The same MIME type as the one provided by {@link #getMimeType()}.
	*/
	protected XMLFilter (String mimeType) {
		super(false, mimeType, IProcessor.DC_ALL);
	}
	
	@Override
	public String getName () {
		return "okf_xml";
	}
	
	@Override
	public String getDisplayName () {
		return "XML Filter";
	}

	@Override
	public String getMimeType () {
		return MimeTypeMapper.XML_MIME_TYPE;
	}

	@Override
	public List<FilterConfiguration> getConfigurations () {
		List<FilterConfiguration> list = new ArrayList<FilterConfiguration>();
		list.add(new FilterConfiguration(getName(),
			getMimeType(),
			getClass().getName(),
			"Generic XML",
			"Configuration for generic XML documents (default ITS rules).",
			null,
			".xml;"));
		list.add(new FilterConfiguration(getName()+"-resx",
			getMimeType(),
			getClass().getName(),
			"RESX",
			"Configuration for Microsoft RESX documents (without binary data).",
			"resx.fprm",
			".resx;"));
		list.add(new FilterConfiguration(getName()+"-MozillaRDF",
			getMimeType(),
			getClass().getName(),
			"Mozilla RDF",
			"Configuration for Mozilla RDF documents.",
			"MozillaRDF.fprm",
			".rdf;"));
		list.add(new FilterConfiguration(getName()+"-JavaProperties",
			getMimeType(),
			getClass().getName(),
			"Java Properties XML",
			"Configuration for Java Properties files in XML.",
			"JavaProperties.fprm"));
		list.add(new FilterConfiguration(getName()+"-AndroidStrings",
			getMimeType(),
			getClass().getName(),
			"Android Strings",
			"Configuration for Android Strings XML documents.",
			"AndroidStrings.fprm"));
		list.add(new FilterConfiguration(getName()+"-WixLocalization",
			getMimeType(),
			getClass().getName(),
			"WiX Localization",
			"Configuration for WiX (Windows Installer XML) Localization files.",
			"WixLocalization.fprm",
			".wxl;"));
		list.add(new FilterConfiguration(getName()+"-AppleStringsdict",
			getMimeType(),
			getClass().getName(),
			"Apple Stringsdict",
			"Configuration for Apple Stringsdict files",
			"AppleStringsdict.fprm",
			".stringsdict;"));
		return list;
	}
	
    @Override
    public ISkeletonWriter createSkeletonWriter () {
        return new XMLSkeletonWriter();
    }

	@Override
	public EncoderManager getEncoderManager () {
		if ( encoderManager == null ) {
			encoderManager = new EncoderManager();
			encoderManager.setMapping(getMimeType(), "net.sf.okapi.common.encoder.XMLEncoder");
		}
		return encoderManager;
	}

	@Override
	protected void initializeDocument () {
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
		} catch (ParserConfigurationException e) {
			// Tried an unsupported feature. This may indicate that a different XML processor is being
			// used. If so, then its features need to be researched and applied correctly.
			// For example, using the Xerces 2 feature above on a Xerces 1 processor will throw this
			// exception.
			logger.warn("Unsupported DocumentBuilderFactory feature. Possible security vulnerabilities.", e);
		}
		// Expand entity references only if we do not protect them
		// "Expand entity" means don't have ENTITY_REFERENCE
		fact.setExpandEntityReferences(!params.protectEntityRef);
		
		// Create the document builder
		DocumentBuilder docBuilder;
		try {
			docBuilder = fact.newDocumentBuilder();
		}
		catch (ParserConfigurationException e) {
			throw new OkapiIOException(e);
		}
		//TODO: Do this only as an option
		// Avoid DTD declaration
		docBuilder.setEntityResolver(new DefaultEntityResolver());

		input.setEncoding("UTF-8"); // Default for XML, other should be auto-detected
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
		
		try {
			InputSource is = new InputSource(input.getStream());
			doc = docBuilder.parse(is);
		}
		catch ( SAXException e ) {
			throw new OkapiIOException("Parsing error.\n"+e.getMessage(), e);
		}
		catch ( IOException e ) {
			throw new OkapiIOException("IO Error when reading the document.\n"+e.getMessage(), e);
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
	protected void createStartDocumentSkeleton (StartDocument startDoc) {
		// Add the XML declaration
		skel = new GenericSkeleton();
		if ( !params.omitXMLDeclaration ) {
			skel.add("<?xml version=\"" + doc.getXmlVersion() + "\"");
			skel.add(" encoding=\"");
			skel.addValuePlaceholder(startDoc, Property.ENCODING, LocaleId.EMPTY);
			skel.add("\"");
			startDoc.setProperty(new Property(Property.ENCODING, encoding, false));
			if ( doc.getXmlStandalone() ) skel.add(" standalone=\"yes\"");
			skel.add("?>"+lineBreak);
		}
		// Add the DTD if needed
		DocumentType dt = doc.getDoctype();
		if ( dt != null ) {
			rebuildDocTypeSection(dt);
		}
	}

	private void rebuildDocTypeSection (DocumentType dt) {
		StringBuilder tmp = new StringBuilder();
		// Set the start syntax
		if ( dt.getPublicId() != null ) {
			tmp.append(String.format("<!DOCTYPE %s PUBLIC \"%s\" \"%s\"",
				dt.getName(),
				dt.getPublicId(),
				dt.getSystemId()));
		}
		else if ( dt.getSystemId() != null ) {
			tmp.append(String.format("<!DOCTYPE %s SYSTEM \"%s\"",
				dt.getName(),
				dt.getSystemId()));
		}
		else if ( dt.getInternalSubset() != null ) {
			tmp.append(String.format("<!DOCTYPE %s",
				dt.getName()));
		}
		
		// Add the internal sub-set if there is any
		if ( dt.getInternalSubset() != null ) {
			tmp.append(" ["+lineBreak);
			tmp.append(dt.getInternalSubset().replace("\n", lineBreak));
			tmp.append("]");
		}
		
		if ( tmp.length() > 0 ) {
			tmp.append(">"+lineBreak);
			skel.add(tmp.toString());
		}
	}
	
}
