/*===========================================================================
  Copyright (C) 2008-2009 by the Okapi Framework contributors
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

package net.sf.okapi.applications.rainbow.lib;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.sf.okapi.common.Util;
import net.sf.okapi.common.exceptions.OkapiException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class EncodingManager {
	
	private ArrayList<EncodingItem> items;
	
	public EncodingManager () {
		items = new ArrayList<EncodingItem>();
	}
	
	public int getCount () {
		return items.size();
	}
	
	public EncodingItem getItem (int index) {
		return items.get(index);
	}

	public int getIndexFromIANAName (String ianaName) {
		int i = 0;
		for ( EncodingItem item : items ) {
			if ( ianaName.equalsIgnoreCase(item.ianaName) ) return i;
			i++;
		}
		return -1;
	}
	
	public void loadList (InputStream is) {
		try {
			DocumentBuilderFactory Fact = DocumentBuilderFactory.newInstance();
			Fact.setValidating(false);
			parseDoc(Fact.newDocumentBuilder().parse(is));			
		}
		catch ( IOException e ) {
			throw new OkapiException(e);
		}
		catch ( SAXException e ) {
			throw new OkapiException(e);
		}
		catch ( ParserConfigurationException e ) {
			throw new OkapiException(e);
		}
	}
	
	public void loadList (String p_sPath) {
		try {
			DocumentBuilderFactory Fact = DocumentBuilderFactory.newInstance();
			Fact.setValidating(false);
			parseDoc(Fact.newDocumentBuilder().parse(new File(p_sPath)));			
		}
		catch ( IOException e ) {
			throw new OkapiException(e);
		}
		catch ( SAXException e ) {
			throw new OkapiException(e);
		}
		catch ( ParserConfigurationException e ) {
			throw new OkapiException(e);
		}
	}
	
	public void parseDoc(Document doc) {
		NodeList NL = doc.getElementsByTagName("encoding");
		items.clear();
		EncodingItem item;
		
		for ( int i=0; i<NL.getLength(); i++ ) {
			Node N = NL.item(i).getAttributes().getNamedItem("iana");
			if ( N == null ) throw new OkapiException("The attribute 'iana' is missing.");
			item = new EncodingItem();
			item.ianaName = Util.getTextContent(N);
			N = NL.item(i).getAttributes().getNamedItem("cp");
			if ( N == null ) item.codePage = -1;
			else item.codePage = Integer.valueOf(Util.getTextContent(N));
			N = NL.item(i).getFirstChild();
			while ( N != null ) {
				if (( N.getNodeType() == Node.ELEMENT_NODE )
					&& ( N.getNodeName().equals("name") )) {
					item.name = Util.getTextContent(N);
					break;
				}
				else N = N.getNextSibling();
			}
			if ( item.name == null ) throw new OkapiException("The element 'name' is missing.");
			items.add(item);
		}
	}
}
