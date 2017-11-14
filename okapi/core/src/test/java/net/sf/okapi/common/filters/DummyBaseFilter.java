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

package net.sf.okapi.common.filters;

import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.filters.PropertyTextUnitPlaceholder.PlaceholderAccessType;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.common.skeleton.GenericSkeleton;

public class DummyBaseFilter extends AbstractFilter {
	private EventBuilder eventBuilder;
	
	public DummyBaseFilter() {
		eventBuilder = new EventBuilder("rootId", this);
	}
	
	public void close() {
	}

	@Override
	public String getName() {
		return "DummyBaseFilter";
	}
	
	public String getDisplayName () {
		return "Dummy Base Filter";
	}

	public IParameters getParameters() {
		return null;
	}

	public void open (RawDocument input) {
		open(input, true);
	}
	
	public void open(RawDocument input,
		boolean generateSkeleton)
	{
		if ( input.getInputCharSequence().equals("2") ) {
			createCase2();
		}
		else {
			createCase1();
		}
	}

	public void setParameters (IParameters params) {
	}

	public void setFilterConfigurationMapper (IFilterConfigurationMapper fcMapper) {
	}

	private void createCase1 () {
		setMimeType("text/xml");
		
		eventBuilder.reset("rootId", this);
		eventBuilder.addFilterEvent(createStartFilterEvent());
		
		eventBuilder.startTextUnit("Text.");
		eventBuilder.endTextUnit();
		eventBuilder.startDocumentPart("<docPart/>");
		eventBuilder.addToDocumentPart("<secondPart/>");
		eventBuilder.endDocumentPart();
		
		eventBuilder.flushRemainingTempEvents();
		eventBuilder.addFilterEvent(createEndFilterEvent());
	}

	private void createCase2 () {
		setMimeType("text/xml");
		setNewlineType("\n");

		eventBuilder.reset("rootId", this);
		eventBuilder.addFilterEvent(createStartFilterEvent());

		ArrayList<PropertyTextUnitPlaceholder> list = new ArrayList<PropertyTextUnitPlaceholder>();
		list.add(new PropertyTextUnitPlaceholder(PlaceholderAccessType.WRITABLE_PROPERTY, "attr", "val1", 10, 14));
		
		//TODO: Skeleton should be GenericSkeleton since BaseFilter uses only that one
		eventBuilder.startTextUnit("Before ", new GenericSkeleton("<tu attr='val1'>"), list);
		eventBuilder.addToTextUnit(new Code(TagType.OPENING, "bold", "<b>"));
		eventBuilder.addToTextUnit("Text");
		eventBuilder.addToTextUnit(new Code(TagType.CLOSING, "bold", "</b>"));
		
		eventBuilder.flushRemainingTempEvents();
		eventBuilder.addFilterEvent(createEndFilterEvent());
	}

	public List<FilterConfiguration> getConfigurations() {
		return null;
	}

	@Override
	protected boolean isUtf8Bom() {
		return false;
	}

	@Override
	protected boolean isUtf8Encoding() {
		return false;
	}

	public boolean hasNext() {
		return eventBuilder.hasNext();
	}

	public Event next() {
		return eventBuilder.next();
	}	
}
