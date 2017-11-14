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
===========================================================================*/

package net.sf.okapi.filters.xliff;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import net.sf.okapi.common.BOMNewlineEncodingDetector;
import net.sf.okapi.common.Namespaces;
import net.sf.okapi.common.annotation.GenericAnnotation;
import net.sf.okapi.common.annotation.GenericAnnotationType;
import net.sf.okapi.common.annotation.GenericAnnotations;
import net.sf.okapi.common.annotation.ITSLQIAnnotations;
import net.sf.okapi.common.annotation.ITSProvenanceAnnotations;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.filterwriter.ITSContent;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.filters.xliff.its.IITSDataStore;
import net.sf.okapi.filters.xliff.its.ITSStandoffManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extension to the XLIFFFilter to handle ITS metadata in the XLIFF document.
 */
public class XLIFFITSFilterExtension {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private ITSStandoffManager itsStandoffManager;
	private XMLStreamReader reader;
	private XMLInputFactory xmlFactory;
	private URI docURI;
	private XLIFFFilter filter;

	public XLIFFITSFilterExtension (XMLInputFactory xmlFactory,
		IITSDataStore datastore,
		URI docURI,
		XLIFFFilter filter)
	{
		this.xmlFactory = xmlFactory;
		this.itsStandoffManager = new ITSStandoffManager(datastore);
		this.docURI = docURI;
		this.filter = filter;
	}

	public void parseInDocumentITSStandoff (XMLEventReader eventReader,
		String inStreamCharset) throws XMLStreamException
	{
		// Empty string indicates metadata within doc when resolving URI.
		this.itsStandoffManager.parseXLIFF(eventReader, "", inStreamCharset);
	}
	
	/**
	 * Parse a four value lqiPos attribute.
	 * @param data the value to parse.
	 * @param logger the logger to use for warnings.
	 * @return an array of four integers set to {0,-1,0,-1} by default.
	 */
	static public int[] parseXLQIPos (String data,
		Logger logger)
	{
		int[] values = new int[]{0, -1, 0, -1};
		String[] items = data.split("\\s", 0);
		if ( items.length != 4 ) {
			logger.warn("Invalid set of values for lqiPos: '{}'.", data);
		}
		else {
			try {
				values[0] = Integer.valueOf(items[0]);
				values[1] = Integer.valueOf(items[1]);
				values[2] = Integer.valueOf(items[2]);
				values[3] = Integer.valueOf(items[3]);
			}
			catch ( NumberFormatException e ) {
				logger.warn("At least one value is invalid in lqiPos: '{}'.", data);
			}
		}
		return values;
	}

	/**
	 * Sets the reader for this object. The Reader contains parsing state of the XLIFF file
	 * and should be set before parsing begins.
	 */
	public void setXLIFFReader (XMLStreamReader reader) {
		this.reader = reader;
	}

	/**
	 * Gets the standoff manager object for this helper object.
	 * @return the standoff manager object for this helper object.
	 */
	public ITSStandoffManager getITSStandoffManager () {
		return this.itsStandoffManager;
	}

	protected void readTextUnitITSAttributes (ITextUnit resource) throws IOException {
		GenericAnnotations anns = new GenericAnnotations();

		// Check for LQI
		String val1 = reader.getAttributeValue(Namespaces.ITS_NS_URI, "locQualityIssuesRef");
		if (val1 != null) {
			ITSLQIAnnotations lqiAnn = readITSLQI();
			ITSLQIAnnotations.addAnnotations(resource, lqiAnn);
			anns.addAll(lqiAnn);
		}
		else { // Otherwise check for on-element LQI attributes
			val1 = reader.getAttributeValue(Namespaces.ITS_NS_URI, "locQualityIssueComment");
			String val2 = reader.getAttributeValue(Namespaces.ITS_NS_URI, "locQualityIssueType");
			if ((val1 != null) || (val2 != null)) {
				logger.warn("ITS Localization Quality Issue data category is to be used only on the main source and target and on mrk.");
			}
		}

		ITSProvenanceAnnotations provAnn = readITSProvenance();
		ITSProvenanceAnnotations.addAnnotations(resource, provAnn);
		anns.addAll(provAnn);

		anns.add(readITSExternalResource());
		anns.add(readITSLocaleFilter());
		anns.add(filter.getAnnotatorsRefContext().getAnnotation());

		// ITS Domain
		if ((val1 = reader.getAttributeValue(Namespaces.ITSXLF_NS_URI, "domains")) != null) {
			anns.add(new GenericAnnotation(GenericAnnotationType.DOMAIN,
				GenericAnnotationType.DOMAIN_VALUE, val1));
		}

		if (anns.size() > 0) {
			GenericAnnotations.addAnnotations(resource, anns);
		}
	}

	protected GenericAnnotations readTextContainerITSAttributes () throws IOException {
		GenericAnnotations anns = new GenericAnnotations();
		anns.addAll(readITSLQI());
		anns.addAll(readITSProvenance());
		anns.add(readITSAllowedCharacters());
		anns.add(readITSMtConfidence());
		anns.add(readITSStorageSize());
		anns.add(readITSLQR());
		return (anns.size() > 0) ? anns : null;
	}

	protected GenericAnnotations readInlineCodeITSAttributes () throws IOException {
		GenericAnnotations anns = new GenericAnnotations();

		anns.addAll(readITSLQI());
		anns.add(readITSAllowedCharacters());
		anns.add(readITSMtConfidence());
		anns.add(readITSStorageSize());
		anns.add(readITSLQR());
		anns.add(readITSExternalResource());
		anns.add(readITSLocaleFilter());
		anns.add(filter.getAnnotatorsRefContext().getAnnotation());

		String val;
		// Language Information
		val = reader.getAttributeValue(Namespaces.XML_NS_URI, "lang");
		if ( val != null ) {
			anns.add(new GenericAnnotation(GenericAnnotationType.LANG,
				GenericAnnotationType.LANG_VALUE, val));
		}

		// Preserve Space
		val = reader.getAttributeValue(Namespaces.XML_NS_URI, "space");
		if ( val != null ) {
			if ( val.equals("preserve") || val.equals("default") ) {
				anns.add(new GenericAnnotation(
					GenericAnnotationType.PRESERVEWS,
					GenericAnnotationType.PRESERVEWS_INFO, val));
			}
			else {
				logger.error("Invalid value for xml:space ('{}').", val);
			}
		}

		// Localization Note
		val = reader.getAttributeValue(null, "comment");
		if ( val != null ) {
			anns.add(new GenericAnnotation(GenericAnnotationType.LOCNOTE,
				GenericAnnotationType.LOCNOTE_VALUE, val));
		}

		String mtype = reader.getAttributeValue(null, "mtype");
		if ( mtype != null ) {

			// Terminology
			if ( mtype.equals("term") ) {
				String info = reader.getAttributeValue(
					Namespaces.ITSXLF_NS_URI, "termInfo");
				String infoRef = reader.getAttributeValue(
					Namespaces.ITSXLF_NS_URI, "termInfoRef");
				if ( (info != null) && (infoRef != null) ) {
					logger.error("Cannot have both termInfo and termInfoRef on the same element. termInfo will be used.");
				}
				else if ( infoRef != null ) {
					info = ITSContent.REF_PREFIX + infoRef;
				}

				val = reader.getAttributeValue(Namespaces.ITSXLF_NS_URI,
					"termConfidence");
				Double conf = null;
				if ( val != null ) {
					conf = Double.parseDouble(val);
				}
				anns.add(new GenericAnnotation(GenericAnnotationType.TERM,
					GenericAnnotationType.TERM_INFO, info,
					GenericAnnotationType.TERM_CONFIDENCE, conf,
					GenericAnnotationType.ANNOTATORREF,
						filter.getAnnotatorsRefContext().getAnnotatorRef("terminology")));
			}
		}

		// Text Analysis
		String taClassRef = reader.getAttributeValue(Namespaces.ITS_NS_URI, "taClassRef");
		if ( taClassRef != null ) {
			taClassRef = ITSContent.REF_PREFIX + taClassRef;
		}
		String taSource = null;
		String taIdent = reader.getAttributeValue(Namespaces.ITS_NS_URI, "taIdentRef");
		if ( taIdent != null ) {
			taIdent = ITSContent.REF_PREFIX + taIdent;
		}
		else {
			taIdent = reader.getAttributeValue(Namespaces.ITS_NS_URI, "taIdent");
			taSource = reader.getAttributeValue(Namespaces.ITS_NS_URI, "taSource");
		}
		if ( (taClassRef != null) || (taIdent != null) ) {
			val = reader.getAttributeValue(Namespaces.ITS_NS_URI, "taConfidence");
			Double conf = null;
			if ( val != null ) {
				conf = Double.parseDouble(val);
			}
			anns.add(new GenericAnnotation(GenericAnnotationType.TA,
				GenericAnnotationType.TA_CLASS, taClassRef,
				GenericAnnotationType.TA_SOURCE, taSource,
				GenericAnnotationType.TA_IDENT, taIdent,
				GenericAnnotationType.TA_CONFIDENCE, conf,
				GenericAnnotationType.ANNOTATORREF,
					filter.getAnnotatorsRefContext().getAnnotatorRef("text-analysis")));
		}

		return (anns.size() > 0) ? anns : null;
	}

	/**
	 * Resolves standoff and on-element ITS Language Quality Issue data
	 * attached to the current element being processed by the
	 * XMLStreamReader.
	 * @throws IOException 
	 */
	protected ITSLQIAnnotations readITSLQI () throws IOException {
		ITSLQIAnnotations lqiAnns = new ITSLQIAnnotations();
		String val1 = reader.getAttributeValue(Namespaces.ITS_NS_URI, "locQualityIssuesRef");
		if ( val1 != null ) {
			// fetch the standoff markup
			String r = getResourcePart(val1);
			if ( !r.equals("") ) {
				fetchStandoffData(r);
			}
			itsStandoffManager.addLQIAnnotation(lqiAnns, val1);
		}
		else { // Otherwise check for on-element LQI attributes
			val1 = reader.getAttributeValue(Namespaces.ITS_NS_URI, "locQualityIssueComment");
			String val2 = reader.getAttributeValue(Namespaces.ITS_NS_URI, "locQualityIssueType");
			if (( val1 != null ) || ( val2 != null )) {
				// OK to create with one null value
				GenericAnnotation ann = new GenericAnnotation(GenericAnnotationType.LQI,
					GenericAnnotationType.LQI_COMMENT, val1,
					GenericAnnotationType.LQI_TYPE, val2);
				if ((val1 = reader.getAttributeValue(Namespaces.ITS_NS_URI, "locQualityIssueSeverity")) != null) {
					ann.setDouble(GenericAnnotationType.LQI_SEVERITY, Double.parseDouble(val1));
				}
				if ((val1 = reader.getAttributeValue(Namespaces.ITS_NS_URI, "locQualityIssueProfileRef")) != null) {
					ann.setString(GenericAnnotationType.LQI_PROFILEREF, val1);
				}
				if ((val1 = reader.getAttributeValue(Namespaces.ITS_NS_URI, "locQualityIssueEnabled")) != null) {
					ann.setBoolean(GenericAnnotationType.LQI_ENABLED, val1.equals("yes"));
				}
				
				// Read the extra attributes when present
				if ((val1 = reader.getAttributeValue(Namespaces.NS_XLIFFOKAPI, "lqiPos")) != null) {
					// Read the values
					int[] values = parseXLQIPos(val1, logger);
					ann.setInteger(GenericAnnotationType.LQI_XSTART, values[0]);
					ann.setInteger(GenericAnnotationType.LQI_XEND, values[1]);
					ann.setInteger(GenericAnnotationType.LQI_XTRGSTART, values[2]);
					ann.setInteger(GenericAnnotationType.LQI_XTRGEND, values[3]);
				}
				if ((val1 = reader.getAttributeValue(Namespaces.NS_XLIFFOKAPI, "lqiCodes")) != null) {
					ann.setString(GenericAnnotationType.LQI_XCODES, val1);
				}
				if ((val1 = reader.getAttributeValue(Namespaces.NS_XLIFFOKAPI, "lqiSegId")) != null) {
					ann.setString(GenericAnnotationType.LQI_XSEGID, val1);
				}

				// Add the annotation to the list
				lqiAnns.add(ann);
			}
		}
		return (lqiAnns.size() > 0) ? lqiAnns : null;
	}
	
	/**
	 * Resolves ITS Provenance standoff data attached to the current element
	 * being processed by the XMLStreamReader.
	 * @throws IOException 
	 */
	protected ITSProvenanceAnnotations readITSProvenance () throws IOException {
		ITSProvenanceAnnotations provAnns = new ITSProvenanceAnnotations();
		String val = reader.getAttributeValue(Namespaces.ITS_NS_URI, "provenanceRecordsRef");
		if ( val != null ) {
			String r = getResourcePart(val);
			if ( r.isEmpty() ) {
				fetchStandoffData(r);
			}
			itsStandoffManager.addProvAnnotation(provAnns, val);
		}
		else {
			// On-element markup
			GenericAnnotation ga = new GenericAnnotation(GenericAnnotationType.PROV);
			readProvFields(ga, GenericAnnotationType.PROV_PERSON, GenericAnnotationType.PROV_REVPERSON, "person");
			readProvFields(ga, GenericAnnotationType.PROV_ORG, GenericAnnotationType.PROV_REVORG, "org");
			readProvFields(ga, GenericAnnotationType.PROV_TOOL, GenericAnnotationType.PROV_REVTOOL, "tool");
			ga.setString(GenericAnnotationType.PROV_PROVREF,
				reader.getAttributeValue(Namespaces.ITS_NS_URI, "provRef"));
			if ( ga.getFieldCount() > 0 ) provAnns.add(ga);
		}

		return (provAnns.size() > 0) ? provAnns : null;
	}
	
	private void readProvFields (GenericAnnotation ga,
		String fieldName,
		String revFieldName,
		String attributeBaseName)
	{
		// Read normal field for translation
		String val = reader.getAttributeValue(Namespaces.ITS_NS_URI, attributeBaseName);
		if ( val != null ) {
			ga.setString(fieldName, val);
		}
		else { // Try the reference variant for translation
			val = reader.getAttributeValue(Namespaces.ITS_NS_URI, attributeBaseName+"Ref");
			if ( val != null ) {
				ga.setString(fieldName, GenericAnnotationType.REF_PREFIX + val);
			}
		}
		
		// Read the field for revision 
		String baseRevName = "rev" + Character.toUpperCase(attributeBaseName.charAt(0))+ attributeBaseName.substring(1);
		val = reader.getAttributeValue(Namespaces.ITS_NS_URI, baseRevName);
		if ( val != null ) {
			ga.setString(revFieldName, val);
		}
		else { // Try the reference variant for revision
			val = reader.getAttributeValue(Namespaces.ITS_NS_URI, baseRevName+"Ref");
			if ( val != null ) {
				ga.setString(revFieldName, GenericAnnotationType.REF_PREFIX + val);
			}
		}

	}

	protected GenericAnnotation readITSAllowedCharacters () {
		String val1 = reader.getAttributeValue(Namespaces.ITS_NS_URI, "allowedCharacters");
		return (val1 == null) ? null
			: new GenericAnnotation(GenericAnnotationType.ALLOWEDCHARS,
			GenericAnnotationType.ALLOWEDCHARS_VALUE, val1);
	}
	
	protected GenericAnnotation readITSMtConfidence () {
		String val = reader.getAttributeValue(Namespaces.ITS_NS_URI, "mtConfidence");
		return (val == null) ? null
			: new GenericAnnotation(GenericAnnotationType.MTCONFIDENCE,
			GenericAnnotationType.MTCONFIDENCE_VALUE, Double.parseDouble(val),
			GenericAnnotationType.ANNOTATORREF, filter.getAnnotatorsRefContext().getAnnotatorRef("mt-confidence"));
	}

	protected GenericAnnotation readITSLQR () {
		GenericAnnotation ann = null;
		String profile = reader.getAttributeValue(Namespaces.ITS_NS_URI, "locQualityRatingProfileRef");
		String val = reader.getAttributeValue(Namespaces.ITS_NS_URI, "locQualityRatingScore");
		if ( val != null ) {
			Double score = Double.parseDouble(val);
			Double threshold = null;
			val = reader.getAttributeValue(Namespaces.ITS_NS_URI, "locQualityRatingScoreThreshold");
			if ( val != null ) {
				threshold = Double.parseDouble(val);
			}
			ann = new GenericAnnotation(GenericAnnotationType.LQR,
				GenericAnnotationType.LQR_SCORE, score,
				GenericAnnotationType.LQR_SCORETHRESHOLD, threshold,
				GenericAnnotationType.LQR_PROFILEREF, profile);
		}
		else {
			val = reader.getAttributeValue(Namespaces.ITS_NS_URI, "locQualityRatingVote");
			if ( val != null ) {
				Integer vote = Integer.parseInt(val);
				Integer threshold = null;
				val = reader.getAttributeValue(Namespaces.ITS_NS_URI, "locQualityRatingVoteThreshold");
				if ( val != null ) {
					threshold = Integer.parseInt(val);
				}
				ann = new GenericAnnotation(GenericAnnotationType.LQR,
					GenericAnnotationType.LQR_VOTE, vote,
					GenericAnnotationType.LQR_VOTETHRESHOLD, threshold,
					GenericAnnotationType.LQR_PROFILEREF, profile);
			}
		}
		return ann;
	}
	
	protected GenericAnnotation readITSStorageSize () {
		GenericAnnotation ann = null;
		String val1 = reader.getAttributeValue(Namespaces.ITS_NS_URI, "storageSize");
		if ( val1 != null ) {
			// Get encoding info
			String enc = reader.getAttributeValue(Namespaces.ITS_NS_URI, "storageEncoding");
			if ( enc == null ) {
				enc = "UTF-8";
			}
			// Get line-break type
			String lb = reader.getAttributeValue(Namespaces.ITS_NS_URI, "lineBreakType");
			if ( lb == null ) {
				lb = "lf";
			}
			ann = new GenericAnnotation(GenericAnnotationType.STORAGESIZE,
				GenericAnnotationType.STORAGESIZE_SIZE, Integer.parseInt(val1),
				GenericAnnotationType.STORAGESIZE_ENCODING, enc,
				GenericAnnotationType.STORAGESIZE_LINEBREAK, lb);
		}
		return ann;
	}

	protected GenericAnnotation readITSExternalResource () {
		String val1 = reader.getAttributeValue(Namespaces.ITSXLF_NS_URI, "externalResourceRef");
		return (val1 == null) ? null
			: new GenericAnnotation(GenericAnnotationType.EXTERNALRES,
			GenericAnnotationType.EXTERNALRES_VALUE, val1);
	}

	protected GenericAnnotation readITSLocaleFilter () {
		String val1 = reader.getAttributeValue(Namespaces.ITS_NS_URI, "localeFilterList");
		if ( val1 != null ) {
			String val2 = reader.getAttributeValue(Namespaces.ITS_NS_URI, "localeFilterType");
			if ( val2 != null ) {
				if ( val2.equals("exclude") ) val1 = "!"+val1;
				else if ( val2.equals("include") ) {
					logger.warn("Invalid value for ITS localeFilterType ({}).", val2);
					return null;
				}
			}
			return new GenericAnnotation(GenericAnnotationType.LOCFILTER,
				GenericAnnotationType.LOCFILTER_VALUE, val1);
		}
		return null;
	}
	
//	protected GenericAnnotation readITSAnnotatorsRef () {
//		String tmp = filter.getAnnotatorsRefAnnotation(); 
//		if ( tmp != null ) {
//			return new GenericAnnotation(GenericAnnotationType.ANNOT,
//				GenericAnnotationType.ANNOT_VALUE, tmp);
//		}
//		return null;
//	}

	/**
	 * Retrieves URI resource, parses for ITS metadata, and stores any
	 * ITS metadata into the standoff manager.
	 * @param resource An IRI reference without the fragment part.
	 * @throws IOException 
	 */
	private void fetchStandoffData (String resource) throws IOException {
		// Try to avoid re-parsing the standoff document if we can
		if ( !itsStandoffManager.alreadyParsed(resource) ) {
			URL resourceURL;
			try {
				resourceURL = new URL(resource);
			}
			catch (MalformedURLException ex) {
				if (docURI != null) {
					// Try to resolve the resource as a relative reference to the file being parsed.
					try {
						resourceURL = docURI.resolve(resource).toURL();
					} catch (MalformedURLException ex1) {
						logger.warn("Resource '" + resource + "' not a valid URL", ex);
						throw new OkapiIOException("Bad uri for resource: " + resource, ex1);
					}
				}
				else {
					throw new OkapiIOException("Resource '" + resource + "' not a valid URL", ex);
				}
			}

			InputStreamReader externalResource = null;
			try (InputStream is = resourceURL.openStream();) {				
				String charset = StandardCharsets.UTF_8.name();
				BOMNewlineEncodingDetector detector = new BOMNewlineEncodingDetector(is, StandardCharsets.UTF_8);
				detector.detectBom();
				if (detector.isAutodetected()) {
					charset = detector.getEncoding();
				}
				externalResource = new InputStreamReader(is, charset);
				itsStandoffManager.parseXLIFF(xmlFactory.createXMLEventReader(externalResource), resource, charset);				
			}
			catch (MalformedURLException ex) {
				throw new OkapiIOException("Bad url for resource: " + resource, ex);
			}
			catch (IOException ex) {
				throw new OkapiIOException("Cannot find resource: " + resource, ex);
			}
			catch (XMLStreamException ex) {
				throw new OkapiIOException("Cannot open XML document.\n", ex);
			} finally {
				if (externalResource != null) {
					externalResource.close();
				}
			}
		}
	}

	/**
	 * Separates the fragment ID from the URI reference.
	 * @param uriRef the URI to process.
	 * @return the resource part of the given URI, or an empty string (e.g. for "#myId").
	 */
	protected static String getResourcePart (String uriRef) {
		int pn = uriRef.lastIndexOf('#');
		// Default resource location is the empty string (within doc).
		return pn > 0 ? uriRef.substring(0, pn) : "";
	}

	protected static String getFragmentIdPart (String uriRef) {
		int pn = uriRef.lastIndexOf('#');
		return uriRef.substring(pn + 1);
	}

}