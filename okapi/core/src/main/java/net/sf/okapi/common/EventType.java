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

/**
 * The type of events used when working with the pipeline and its associated
 * interfaces such as {@link net.sf.okapi.common.filters.IFilter} or 
 * {@link net.sf.okapi.common.filterwriter.IFilterWriter}.
 */
public enum EventType {

	/**
	 * Indicates the start of an input document. A {@link net.sf.okapi.common.resource.StartDocument}
	 * resource should be associated with this event.
	 */
	START_DOCUMENT("Start Document"),

	/**
	 * Indicates the end of an input document. An {@link net.sf.okapi.common.resource.Ending}
	 * resource should be associated with this event.
	 */
	END_DOCUMENT("End Document"),

	/**
	 * Indicates the start of a sub-document. A {@link net.sf.okapi.common.resource.StartSubDocument}
	 * resource should be associated with this event.
	 */
	START_SUBDOCUMENT("Start SubDocument"),

	/**
	 * Indicates the end of a sub-document. An {@link net.sf.okapi.common.resource.Ending}
	 * resource should be associated with this event.
	 */
	END_SUBDOCUMENT("End SubDocument"),

	/**
	 * Indicates the start of a group. For example, the start tag of the
	 * &lt;table&gt; element in HTML. A {@link net.sf.okapi.common.resource.StartGroup} resource
	 * should be associated with this event.
	 */
	START_GROUP("Start Group"),

	/**
	 * Indicates the end of a group. An {@link net.sf.okapi.common.resource.Ending} resource
	 * should be associated with this event.
	 */
	END_GROUP("End Group"),

	/**
	 * Indicates a text unit. For example, a paragraph in an HTML document. A
	 * {@link net.sf.okapi.common.resource.TextUnit} resource should be associated 
	 * with this event.
	 */
	TEXT_UNIT("Text Unit"),

	/**
	 * Indicates a document part. Document parts are used to carry chunks of the
	 * input document that have no translatable data, but may have properties. A
	 * {@link net.sf.okapi.common.resource.DocumentPart} resource should be associated 
	 * with this event.
	 */
	DOCUMENT_PART("Document Part"),

	/**
	 * Indicates that the user has canceled the process. No resource are
	 * associated with this event.
	 */
	CANCELED("Cancelled"),
	
	/**
	 * Used to notify pipeline steps that the current batch operation is starting.
	 */
	START_BATCH("Start Batch"),

	/**
	 * Used to notify pipeline steps that the current batch operation is finished.
	 */
	END_BATCH("End Batch"),
	
	/**
	 * Used to notify pipeline steps that a new batch item is about to come.
	 */
	START_BATCH_ITEM("Start Batch Item"),

	/**
	 * Used to notify the pipeline steps that athe current batch item is done.
	 */
	END_BATCH_ITEM("End Batch Item"),
	
	/**
	 * Document-level event. A {@link net.sf.okapi.common.resource.RawDocument} resource
	 * should be associated with this event.
	 */
	RAW_DOCUMENT("Raw Document"),
	
	/**
	 * An Event which holds multiple related Events, possibly of different types.
	 */
	MULTI_EVENT("Multi Event"),
	
	/**
	 * A special event which holds updated runtime parameters for the pipeline.
	 */
	PIPELINE_PARAMETERS("Pipeline Parameters"),

	/**
	 * A custom event type used when steps need to exchange non-resource based
	 * information.
	 */
	CUSTOM("Custom"),

	/**
	 * No operation event that is ignored by all steps. Used as a placeholder
	 * event when steps need to stay alive without triggering any actions.
	 */
	NO_OP("NO OP"),
	
	START_SUBFILTER("Start SubFilter"),
	
	END_SUBFILTER("End SubFilter");
	
	private final String value;

	private EventType(String value) { this.value = value; }

	public String toString() { return value; }
	
	public static EventType fromString(String value) {
	    EventType[] eventTypes = EventType.values();
	    for (EventType t : eventTypes) {
	        if (t.toString().equals(value)) {
	            return t;
	        }
	    }
	    return NO_OP;
	}
}
