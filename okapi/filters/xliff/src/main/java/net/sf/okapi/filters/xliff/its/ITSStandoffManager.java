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

package net.sf.okapi.filters.xliff.its;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import net.sf.okapi.common.Namespaces;
import net.sf.okapi.common.annotation.GenericAnnotation;
import net.sf.okapi.common.annotation.GenericAnnotationType;
import net.sf.okapi.common.annotation.GenericAnnotations;
import net.sf.okapi.common.exceptions.OkapiBadFilterInputException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles resolving standoff annotation references without loading the entire
 * XLIFF into memory.
 */
public class ITSStandoffManager {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private static int FILE_NUM = 0;

	private ITSLQICollection currentLQICollection = null;
	private ITSProvenanceCollection currentProvCollection = null;
	private HashSet<String> parsedFiles = new HashSet<String>();
	private IITSDataStore dataStore;
	private String itsId;

	/**
	 * Use default in-memory HashMap of ITS standoff annotations.
	 */
	public ITSStandoffManager () {
		this(new ITSDefaultDataStore());
	}
	
	/**
	 * Use specified storage for ITS standoff annotations.
	 */
	public ITSStandoffManager (IITSDataStore dataStore) {
		this.itsId = "its"+getFileNum();
		this.dataStore = dataStore;
		this.dataStore.initialize(itsId);
	}

	private static synchronized int getFileNum () {
		return FILE_NUM++;
	}

	/**
	 * Saves input reader into a temporary file and returns a reference for
	 * additional parsing of the same content.
	 * @param reader the reader for the document to parse.
	 * @param resource an IRI reference without the fragment part.
	 * @param encoding the encoding to use for the output.
	 * @throws XMLStreamException 
	 */
	public void parseXLIFF (XMLEventReader reader,
		String resource,
		String encoding) throws XMLStreamException
	{
		try {
			while (reader.hasNext()) {
				XMLEvent event = reader.nextEvent();
				switch (event.getEventType()) {
					case XMLStreamConstants.START_ELEMENT:
						StartElement se = event.asStartElement();
						handleStartElement(se, resource);
						break;

					case XMLStreamConstants.END_ELEMENT:
						EndElement end = event.asEndElement();
						handleEndElement(end);
						break;

					default:
						break;
				}
			}
			reader.close();
		} catch (XMLStreamException ex) {
			throw new OkapiBadFilterInputException(
					"Failed to parse XLIFF for ITS annotations\n"+ex.getLocalizedMessage(), ex);
		} finally {
			if (reader != null) reader.close();
		}

		parsedFiles.add(resource);
	}

	private void handleStartElement (StartElement se,
		String resource)
	{
		@SuppressWarnings("unchecked")
		Iterator<Attribute> attrs = se.getAttributes();
		String namespace = se.getName().getPrefix();
		String tagName = se.getName().getLocalPart();

		if ( namespace.equals(Namespaces.ITS_NS_PREFIX) ) {
			if ( "locQualityIssues".equals(tagName) ) {
				currentLQICollection = new ITSLQICollection(attrs, resource);
			}
			else if ( "locQualityIssue".equals(tagName) ) {
				currentLQICollection.addLQI(attrs);
			}
			else if ( "provenanceRecords".equals(tagName) ) {
				currentProvCollection = new ITSProvenanceCollection(attrs, resource);
			}
			else if ( "provenanceRecord".equals(tagName) ) {
				currentProvCollection.addProv(attrs);
			}
		}
	}

	private void handleEndElement (EndElement end) {
		String namespace = end.getName().getPrefix();
		String tagName = end.getName().getLocalPart();
		if ( namespace.equals(Namespaces.ITS_NS_PREFIX) ) {
			if ( "locQualityIssues".equals(tagName) ) {
				dataStore.save(currentLQICollection);
			}
			else if ( "provenanceRecords".equals(tagName) ) {
				dataStore.save(currentProvCollection);
			}
		}
	}

	/**
	 * Resolve a LQI reference using the specified URI and add it to the
	 * specified annotations.
	 * @param anns annotations where to add.
	 * @param uri URI of the reference.
	 */
	public <T extends GenericAnnotations> void addLQIAnnotation (T anns,
		String uri)
	{
		ITSLQICollection results = dataStore.getLQIByURI(uri);
		if ( results != null ) {
			anns.setData(results.getURI());
			Iterator<ITSLQI> collection = results.iterator();
			while ( collection.hasNext() ) {
				ITSLQI lqi = collection.next();
				anns.add(lqi.getAnnotation());
			}
		}
		else {
			logger.warn("Failed to resolve LQI reference for '{}'.", uri);
		}
	}

	/**
	 * Resolve a provenance reference using the specified URI and add it to
	 * the specified annotations.
	 * @param anns the annotations where to add.
	 * @param uri URI of the reference.
	 */
	public <T extends GenericAnnotations> void addProvAnnotation (T anns,
		String uri)
	{
		ITSProvenanceCollection results = dataStore.getProvByURI(uri);
		if ( results != null ) {
			anns.setData(results.getURI());
			Iterator<ITSProvenance> collection = results.iterator();
			while ( collection.hasNext() ) {
				ITSProvenance prov = collection.next();
				GenericAnnotation ann = anns.add(GenericAnnotationType.PROV);
				if (prov.getPerson() != null) {
					ann.setString(GenericAnnotationType.PROV_PERSON,
						prov.getPerson());
				}
				if (prov.getOrg() != null) {
					ann.setString(GenericAnnotationType.PROV_ORG,
						prov.getOrg());
				}
				if (prov.getTool() != null) {
					ann.setString(GenericAnnotationType.PROV_TOOL,
						prov.getTool());
				}
				if (prov.getRevPerson() != null) {
					ann.setString(GenericAnnotationType.PROV_REVPERSON,
						prov.getRevPerson());
				}
				if (prov.getRevOrg() != null) {
					ann.setString(GenericAnnotationType.PROV_REVORG,
						prov.getRevOrg());
				}
				if (prov.getRevTool() != null) {
					ann.setString(GenericAnnotationType.PROV_REVTOOL,
						prov.getRevTool());
				}
				if (prov.getProvRef() != null) {
					ann.setString(GenericAnnotationType.PROV_PROVREF,
						prov.getProvRef());
				}
			}
		}
		else {
			logger.warn("Failed to resolve ITS Provenance reference for '{}'.", uri);
		}
	}

	/**
	 * Check if the named resource has already been parsed for ITS metadata.
	 * Naming consistency is expected for the resource; ie if the resource
	 * is referred to using the entire URI, all references to the same
	 * resource must do the same.
	 * @param resource An IRI reference without the fragment part
	 */
	public boolean alreadyParsed(String resource) {
		return parsedFiles.contains(resource);
	}

	public Collection<String> getStoredLQIRefs () {
		return dataStore.getStoredLQIURIs();
	}

	public Collection<String> getStoredProvRefs () {
		return dataStore.getStoredProvURIs();
	}

	public final IITSDataStore getDataStore() {
		return dataStore;
	}
}