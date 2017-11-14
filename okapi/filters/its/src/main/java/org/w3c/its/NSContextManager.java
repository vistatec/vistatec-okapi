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

package org.w3c.its;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

/**
 * Manages XML namespaces.
 * The reserved 'xml' and 'xmlns' prefixes are handled by default.
 * You do not need to add them. 
 */
public class NSContextManager implements NamespaceContext {

	private Hashtable<String, String> table;
	
	public NSContextManager () {
		table = new Hashtable<String, String>();
	}
	
	@Override
	public String getNamespaceURI (String prefix) {
		if ( table.containsKey(prefix) )
			return table.get(prefix);
		if ( prefix.equals(XMLConstants.XML_NS_PREFIX) )
			return XMLConstants.XML_NS_URI;
		if ( prefix.equals(XMLConstants.XMLNS_ATTRIBUTE) )
			return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
		else
			return XMLConstants.NULL_NS_URI;
	}

	@Override
	public String getPrefix (String uri) {
		Enumeration<String> E = table.keys();
		String key;
		while ( E.hasMoreElements() ) {
			key = E.nextElement();
			if ( table.get(key).equals(uri) )
				return key;
		}
		if ( uri.equals(XMLConstants.XML_NS_URI))
			return XMLConstants.XML_NS_PREFIX;
		if ( uri.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI) )
			return XMLConstants.XMLNS_ATTRIBUTE;
		else
			return null;
	}

	@Override
	public Iterator<String> getPrefixes (String uri) {
		// Not implemented
		return null;
	} 

	/**
	 * Adds a prefix/uri pair to the manager. No checking is done for existing
	 * prefix: If the same is already defined, it will be overwritten.
	 * @param prefix The prefix of the namespace
	 * @param uri The uri of the namespace
	 */
	public void addNamespace (String prefix,
		String uri) {
		table.put(prefix, uri);
	}
}
