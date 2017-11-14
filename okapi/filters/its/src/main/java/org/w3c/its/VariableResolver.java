/*===========================================================================
  Copyright (C) 2012-2013 by the Okapi Framework contributors
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

import java.util.Comparator;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathVariableResolver;

/**
 * Resolver for XPath string variables.
 */
class VariableResolver implements XPathVariableResolver {
	
	class QNameReverseComparator implements Comparator<QName> {

		@Override
		public int compare (QName arg0,
			QName arg1)
		{
			return arg1.toString().compareTo(arg0.toString());
		}
		
	}

	private SortedMap<QName, String> table;
	
	/**
	 * Resolves the variable for a given name.
	 * @param qName the name of the variable.
	 * @return the value for the given name, or null if no value for that name exists.
	 */
	@Override
	public Object resolveVariable (QName qName) {
		if ( table == null ) return null;
		return table.get(qName);
	}

	/**
	 * Adds a variable and its value to this object. If the variable already exists, it is overwritten.
	 * @param qName the name of the variable.
	 * @param value the value.
	 * @param overwrite true to overwrite existing values, false to preserve old value.
	 */
	public void add (QName qName,
		String value,
		boolean overwrite)
	{
		if ( !overwrite && ( table != null )) {
			if ( table.containsKey(qName) ) return; // Do not overwrite
		}
		if ( table == null ) table = new TreeMap<QName, String>(new QNameReverseComparator());
		table.put(qName, value);
	}

	/**
	 * Replaces variables $qname by their value if they are in the resolver.
	 * <p>If a variable in the string is not declared in the resolver, it is not replaced.
	 * @param xpath the string where to replace the parameters (usually an XPath expression)
	 * @return the resulting string
	 */
	public String replaceVariables (String xpath) {
		if ( table == null ) return xpath;
		for ( QName qname : table.keySet() ) {
			xpath = xpath.replace("$"+qname.toString(), quote(table.get(qname)));
		}
		return xpath;
	}
	
	/**
	 * Provide a quoted version of the text passed as parameter.
	 * @param text the text to quote.
	 * @return the text with the proper quotes.
	 */
	private String quote (String text) {
		if ( text.indexOf('\'') != -1 ) {
			if ( text.indexOf('"') != -1 ) {
				//TODO: How to represent both type of quotes
				return "'"+text+"'";
			}
			return "\""+text+"\"";
		}
		return "'"+text+"'";
	}

}
