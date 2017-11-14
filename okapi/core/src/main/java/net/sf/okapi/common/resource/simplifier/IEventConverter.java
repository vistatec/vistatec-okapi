/*===========================================================================
  Copyright (C) 2008-2014 by the Okapi Framework contributors
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

package net.sf.okapi.common.resource.simplifier;

import java.util.List;

import net.sf.okapi.common.Event;

public interface IEventConverter {

	/**
	 * Converts a given event into a different event, modifying either its type or attached resource.
	 * @param event the given event.
	 * @return a modified event. Can be MULTI_EVENT containing a list of events.
	 */
	Event convert(Event event);
	
	/**
	 * Converts a given event into a list of events.
	 * @param event the given event.
	 * @return a list of events produced by the event converter. The implementation should ensure
	 * that no MULTI_EVENT events are present in the list (MultiEvent resources are "unpacked"). 
	 */
	List<Event> convertToList(Event event);
}
