/*===========================================================================
  Copyright (C) 2009 by the Okapi Framework contributors
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

package net.sf.okapi.steps.simpletm2tmx;

import java.io.File;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.tm.simpletm.Database;

@UsingParameters() // No parameters
public class SimpleTM2TMXStep extends BasePipelineStep {

	private Database db;
	private boolean isDone;

	public String getDescription() {
		return "Generates a TMX document from a SimpleTM database. "
			+ "Expects: raw document. Sends back: raw document.";
	}

	public String getName() {
		return "SimpleTM to TMX";
	}

	@Override
	public boolean isDone () {
		return isDone;
	}

	@Override
	protected Event handleStartBatchItem (Event event) {
		isDone = false;
		return event;
	}

	@Override
	protected Event handleRawDocument (Event event) {
		try {
			if ( db == null ) { // Create the db if needed
				db = new Database();
			}
			else { // Just in case, make sure the previous is closed
				db.close();
			}
			
			// Export the db to TMX
			RawDocument rd = (RawDocument)event.getResource(); 
			String path = new File(rd.getInputURI()).getPath(); 
			db.open(path);
			LocaleId srcLang = rd.getSourceLocale();
			LocaleId trgLang = rd.getTargetLocale();
			String outPath = path+".tmx";
			db.exportToTMX(outPath, srcLang, trgLang);
			db.close();
	
			// Create the new resource for the RawDocument
			// It is now a TMX file, not a SimpleTM file
			File file = new File(outPath);
			event.setResource(new RawDocument(file.toURI(), "UTF-8", srcLang, trgLang));
		}
		finally {
			isDone = true;
		}
		
		return event;
	}
	
}
