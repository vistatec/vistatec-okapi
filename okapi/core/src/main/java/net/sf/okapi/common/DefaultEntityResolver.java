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

package net.sf.okapi.common;

import java.io.IOException;
import java.io.StringReader;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Helper class that implement a default EntityResolver.
 */
public class DefaultEntityResolver implements EntityResolver {

	/**
	 * Resolves a given entity to the input source for an empty XML document.
	 * @param publicID The public ID of the entity.
	 * @param systemID The system ID of the entity.
	 * @return The input source for the resolved entity. This default implementation always returns
	 * the input source for an empty XML document.
	 */
	public InputSource resolveEntity (String publicID, String systemID)
		throws SAXException, IOException
	{
		InputSource source = new InputSource(
			new StringReader("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
		source.setPublicId(publicID);
		source.setSystemId(systemID);
		return source;
	}

}
