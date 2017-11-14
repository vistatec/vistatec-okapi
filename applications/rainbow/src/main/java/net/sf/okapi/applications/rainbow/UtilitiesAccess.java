/*===========================================================================
  Copyright (C) 2008-2010 by the Okapi Framework contributors
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
============================================================================*/

package net.sf.okapi.applications.rainbow;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.xml.parsers.DocumentBuilderFactory;

import net.sf.okapi.applications.rainbow.lib.Utils;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.exceptions.OkapiException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class UtilitiesAccess {

	public static final int TYPE_UTILITY         = 0;
	public static final int TYPE_FILTER          = 1;
	public static final int TYPE_PARAMEDITOR     = 2;
	
	private LinkedHashMap<String, UtilitiesAccessItem> items;
	
	public UtilitiesAccess () {
		items = new LinkedHashMap<String, UtilitiesAccessItem>();
	}
	
	public boolean containsID (String id) {
		return items.containsKey(id);
	}
	
	//TODO: Maybe this needs to be an helper static method somewhere
	private Element getFirstElement (Element parent,
		String name)
	{
		NodeList nl = parent.getElementsByTagName(name);
		if (( nl == null ) || ( nl.getLength() == 0 )) return null;
		else return (Element)nl.item(0);
	}

	public Iterator<String> getIterator () {
		return items.keySet().iterator();
	}
	
	public void loadMenu (String path) {
		try {
			DocumentBuilderFactory Fact = DocumentBuilderFactory.newInstance();
			Fact.setValidating(false);
			Document doc = Fact.newDocumentBuilder().parse(new File(path));
			String prefLang = Utils.getCurrentLanguage(); 
			Element rootElem = doc.getDocumentElement();

			int sep = 0;
			NodeList nl = rootElem.getElementsByTagName("plugin");
			for ( int i=0; i<nl.getLength(); i++ ) {
				Element elem = (Element)nl.item(i);
				UtilitiesAccessItem item = new UtilitiesAccessItem();

				String type = elem.getAttribute("type");
				if ( type.equals("separator") ) {
					item.type = -1;
					item.id = String.format("__%d", ++sep);
					items.put(item.id, item);
					continue;
				}
				else if ( type.equals("utility") ) {
					item.type = TYPE_UTILITY;
				}
				else if ( type.equals("filter") ) {
					item.type = TYPE_FILTER;
				}
				else if ( type.equals("editor") ) {
					item.type = TYPE_PARAMEDITOR;
				}
				
				item.id = elem.getAttribute("id");
				if ( item.id.length() == 0 )
					throw new Exception("Attribute 'id' invalid or missing");
				item.pluginClass = elem.getAttribute("pluginClass");
				if ( item.pluginClass.length() == 0 )
					throw new Exception("Attribute 'pluginClass' invalid or missing");
				item.editorClass = elem.getAttribute("editorClass");

				int nDone = 0;
				NodeList infoList = elem.getElementsByTagName("info");
				for ( int j=0; j<infoList.getLength(); j++ ) {
					Element elemInfo = (Element)infoList.item(j);
					String lang = elemInfo.getAttribute("xml:lang");
					int n = 0;
					if ( Util.isSameLanguage(lang, prefLang, true) ) n = 3;
					else if ( Util.isSameLanguage(lang, prefLang, false) ) n = 2;
					else if ( lang.length() == 0 ) n = 1;
					if ( n > nDone ) {
						Element elem2 = getFirstElement(elemInfo, "name");
						item.name = Util.getTextContent(elem2);
						elem2 = getFirstElement(elemInfo, "description");
						item.description = Util.getTextContent(elem2);
						elem2 = getFirstElement(elemInfo, "provider");
						item.provider = Util.getTextContent(elem2);
						nDone = n;
					}
					if ( nDone == 3 ) break; // Best match found
				}

				// Add the new item to the list (overrides any existing one)
				items.put(item.id, item);
			}
		}
		catch ( Exception e ) {
			throw new OkapiException(e);
		}
	}

	public UtilitiesAccessItem getItem (String id) {
		return items.get(id);
	}

}
