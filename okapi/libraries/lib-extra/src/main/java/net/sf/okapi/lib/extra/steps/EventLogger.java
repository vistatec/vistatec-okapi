/*===========================================================================
  Copyright (C) 2010-2011 by the Okapi Framework contributors
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

package net.sf.okapi.lib.extra.steps;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.StartSubDocument;
//import net.sf.okapi.steps.xliffkit.common.persistence.sessions.OkapiJsonSession;
import net.sf.okapi.common.resource.StartSubfilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventLogger extends BasePipelineStep {

	private int indent = 0;
	private boolean increasing = true;
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private StringBuilder sb;
	
	@Override
	public String getDescription() {
		return "Logs events going through the pipeline.";
	}

	@Override
	public String getName() {
		return "Event logger";
	}

	private String getEventDescr(Event event) {
		String res = ""; 
		if (event.getResource() != null)
			res = String.format("  [%s]", event.getResource().getId());
		
		switch ( event.getEventType() ) {	
		case TEXT_UNIT:
			//res = "  " + event.getResource().getId();
			res = String.format("  [%s]", event.getResource().getId());
			res +=  "  " + ((ITextUnit) event.getResource()).getName();
			break;
		case START_DOCUMENT:
			res +=  "  " + ((StartDocument) event.getResource()).getName();
			break;
		case START_SUBDOCUMENT:
			res +=  "  " + ((StartSubDocument) event.getResource()).getName();
			break;
		case START_SUBFILTER:
			res +=  "  " + ((StartSubfilter) event.getResource()).getName();
			break;
//		case TEXT_UNIT:
//			if ("30".equals(event.getResource().getId()))
//				res += "\n" + session.writeObject(event); 
		default:
			break;
		}
		return res;
	}
		
	private void printEvent(Event event) {
		String indentStr = "";
		for (int i = 0; i < indent; i++) 
			indentStr += "  ";
				
		sb.append(indentStr);
		sb.append(event.getEventType() + getEventDescr(event));
		sb.append("\n");
	}
	
	@Override
	public Event handleEvent(Event event) {
		switch ( event.getEventType() ) {
		case START_BATCH:
			sb = new StringBuilder("\n\n");
		case START_DOCUMENT:
		case START_SUBDOCUMENT:
		case START_GROUP:
		case START_BATCH_ITEM:
		case START_SUBFILTER:
			if (!increasing) sb.append("\n");
			printEvent(event);
			indent++; 
			increasing = true;
			break;

		case END_DOCUMENT:
		case END_SUBDOCUMENT:
		case END_GROUP:
		case END_BATCH:
		case END_BATCH_ITEM:
		case END_SUBFILTER:
			if (indent > 0) indent--;
			increasing = false;
			printEvent(event);
			if (event.getEventType() == EventType.END_BATCH) {
				logger.trace(sb.toString());
			}
			break;		
			
		default:
			if (!increasing) sb.append("\n");
			printEvent(event);
		}
		
		
		return super.handleEvent(event);
	}
}
