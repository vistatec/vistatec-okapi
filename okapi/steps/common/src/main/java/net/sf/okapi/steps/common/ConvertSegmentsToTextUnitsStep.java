/*===========================================================================
  Copyright (C) 2012 by the Okapi Framework contributors
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

package net.sf.okapi.steps.common;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.IResource;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.resource.IAlignedSegments;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.MultiEvent;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.TextUnitUtil;

/**
 * Convert single segmented {@link ITextUnit}s to multiple TextUnits, one per aligned sentence pair, for each target locale.
 * If the TextUnit refers to another {@link IResource} or is a referent then pass it on as-is even if it has segments. It's
 * possible these could be safely processed, but it would take considerable effort to implement.
 * 
 * @author hargrave
 */
public class ConvertSegmentsToTextUnitsStep extends BasePipelineStep {

	@Override
	public String getName() {
		return "Segments to Text Units Converter";
	}

	@Override
	public String getDescription() {
		return "Convert each aligned segment pair (for all target locales) to its own complete text unit"
			+ " Expects: filter events. Sends back: filter events.";
	}

	@Override
	protected Event handleTextUnit(Event event) {
		ITextUnit tu = event.getTextUnit();
		
		// if the TextUnit refers to another resource or 
		// is a referent pass it on as-is
		if (tu == null || tu.isEmpty() || !TextUnitUtil.isStandalone(tu)) {
			return event;
		}		
		
		List<Event> textUnitEvents = new LinkedList<Event>();
		
		// if there are no targets then work off of source segments
		if (tu.getTargetLocales().isEmpty()) {
			int segCount = 0;
			for (Segment srcSeg: tu.getSourceSegments()) {  
				if (srcSeg != null) {
					ITextUnit segmentTu = tu.clone();
					segmentTu.setId(segmentTu.getId() + ":" + Integer.toString(++segCount));
					segmentTu.setSourceContent(srcSeg.text);
					textUnitEvents.add(new Event(EventType.TEXT_UNIT, segmentTu));
				}
			}
			
			return new Event(EventType.MULTI_EVENT, new MultiEvent(textUnitEvents));
		}

		// otherwise work off the aligned targets
		IAlignedSegments alignedSegments = tu.getAlignedSegments();
		for (LocaleId variantTrgLoc : tu.getTargetLocales()) {
			// get iterator on source variant segments
			Iterator<Segment> variantSegments = alignedSegments.iterator(variantTrgLoc);

			// For each segment: create a separate TU
			while (variantSegments.hasNext()) {
				int segCount = 0;
				Segment srcSeg = variantSegments.next();

				// for each target segment
				for (LocaleId l : tu.getTargetLocales()) {
					Segment trgSeg = alignedSegments.getCorrespondingTarget(srcSeg, l);
					if (trgSeg != null) {
						ITextUnit segmentTu = tu.clone();
						segmentTu.setId(segmentTu.getId() + ":" + Integer.toString(++segCount));
						segmentTu.setSourceContent(srcSeg.text);
						segmentTu.setTargetContent(l, trgSeg.text);
						textUnitEvents.add(new Event(EventType.TEXT_UNIT, segmentTu));
					}
				}
			}
		}
		
		return new Event(EventType.MULTI_EVENT, new MultiEvent(textUnitEvents));
	}
}
