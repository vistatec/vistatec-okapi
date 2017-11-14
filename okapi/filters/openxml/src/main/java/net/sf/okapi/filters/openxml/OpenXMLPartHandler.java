/*===========================================================================
  Copyright (C) 2016-2017 by the Okapi Framework contributors
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

package net.sf.okapi.filters.openxml;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.LocaleId;

public interface OpenXMLPartHandler {
	/**
	 * Open this part and perform any initial processing.  Return the
	 * first event for this part.
	 * @param documentId document identifier
	 * @param subDocumentId sub-document identifier
	 * @param srcLang the locale of the source
	 * @return first event for this part.
	 * @throws IOException if any problem is encountered
	 */
	Event open(String documentId, String subDocumentId, LocaleId srcLang) throws IOException, XMLStreamException;

	boolean hasNext();

	Event next();

	void close();

	void logEvent(Event e);
}
