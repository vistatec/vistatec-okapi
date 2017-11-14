/*===========================================================================
  Copyright (C) 2008-2011 by the Okapi Framework contributors
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

package net.sf.okapi.applications.rainbow.packages.xliff;

import java.io.File;

import net.sf.okapi.applications.rainbow.packages.IReader;
import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.filters.xliff.XLIFFFilter;

/**
 * Implements IReader for generic XLIFF translation packages.
 */
public class Reader implements IReader {
	
	XLIFFFilter reader;
	Event event;
	
	public void closeDocument () {
		if ( reader != null ) {
			reader.close();
			reader = null;
		}
	}

	public ITextUnit getItem () {
		return event.getTextUnit();
	}

	public void openDocument (String path,
		LocaleId sourceLanguage,
		LocaleId targetLanguage) {
		try {
			closeDocument();
			reader = new XLIFFFilter();
			// Encoding is not really used so we can hard-code
			File f = new File(path);
			RawDocument res = new RawDocument(f.toURI(), "UTF-8", sourceLanguage, targetLanguage);
			reader.open(res, false);
		}
		catch ( Exception e ) {
			throw new OkapiException(e);
		}
	}

	public boolean readItem () {
		while ( reader.hasNext() ) {
			event = reader.next();
			if ( event.getEventType() == EventType.TEXT_UNIT ) {
				return true;
			}
		}
		return false;
	}

}
