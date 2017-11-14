/*===========================================================================
  Copyright (C) 2009-2011 by the Okapi Framework contributors
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

import java.io.File;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filterwriter.GenericContent;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.filters.openoffice.OpenOfficeFilter;
import net.sf.okapi.filters.properties.PropertiesFilter;

public class Main {
	
	public static void main (String[] args) {
		try {
			// Create the filter (based on the extension of the input)
			IFilter filter = null;
			if ( args[0].endsWith(".properties") ) {
				filter = new PropertiesFilter();
			}
			else if ( args[0].endsWith(".odt") ) {
				filter = new OpenOfficeFilter();
			}
			// Open the document to process
			filter.open(new RawDocument(new File(args[0]).toURI(), "UTF-8", new LocaleId("en")));
			
			// Create a formatter to display text unit more prettily.
			GenericContent fmt = new GenericContent();
			
			// process the input document
			while ( filter.hasNext() ) {
				Event event = filter.next();
				if ( event.getEventType() == EventType.TEXT_UNIT ) {
					// Format and print out each text unit
					// We can use getFirstPartContent() because nothing is segmented
					fmt.setContent((event.getTextUnit()).getSource().getFirstContent());
					System.out.println(fmt.toString());
				}
			}
		}
		catch ( Throwable e ) {
			e.printStackTrace();
		}
	}
}
