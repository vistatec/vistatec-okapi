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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.xml.stream.events.Attribute;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a <its:provenanceRecords> element.
 */
public class ITSProvenanceCollection {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private String resource;
	private String xmlid, version;
	private List<ITSProvenance> records = new LinkedList<ITSProvenance>();

	public ITSProvenanceCollection(Iterator<Attribute> attrs, String resource) {
		this.resource = resource;
		while ( attrs.hasNext() ) {
			Attribute attr = attrs.next();
			String prefix = attr.getName().getPrefix();
			String name = attr.getName().getLocalPart();
			String value = attr.getValue();
			if ( !prefix.isEmpty() ) {
				name = prefix + ":" + name;
			}

			if ( name.equals("xml:id") ) {
				xmlid = value;
			}
			else if ( name.equals("version") ) {
				version = value;
			}
			else if ( !name.equals("xmlns:its") ) {
				logger.warn("Unrecognized attribute '{}'.", name);
			}
		}
	}

	public ITSProvenanceCollection(List<ITSProvenance> records,
			String resource, String xmlid, String version) {
		this.records = records;
		this.resource = resource;
		this.xmlid = xmlid;
		this.version = version;
	}
	
	public void addProv (Iterator<Attribute> attrs) {
		records.add(new ITSProvenance(attrs));
	}

	public String getXMLId () {
		return this.xmlid;
	}
	
	public String getVersion () {
		return version;
	}

	public String getURI () {
		return this.resource + "#" + this.xmlid;
	}

	public Iterator<ITSProvenance> iterator () {
		return records.iterator();
	}
}
