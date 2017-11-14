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
===========================================================================*/

package net.sf.okapi.steps.common.skeletonconversion;

import net.sf.okapi.steps.common.ResourceSimplifierStep;

// TODO Remove this step

//public class SkeletonConversionStep extends TuFilteringStep {
public class SkeletonConversionStep extends ResourceSimplifierStep {

//	private GenericSkeletonSimplifier simplifier;
//	private ISkeletonWriter writer;
//	private EncoderManager em;
//	
//	public SkeletonConversionStep() {
//		super();
//	}
//	
//	public SkeletonConversionStep(ITextUnitFilter tuFilter) {
//		super(tuFilter);
//	}
//	
//	@Override
//	protected Event handleStartDocument(Event event) {
//		StartDocument sd = event.getStartDocument();
//		
//		writer = sd.getFilterWriter().getSkeletonWriter();
//		if (writer instanceof GenericSkeletonWriter) {
//			simplifier = new GenericSkeletonSimplifier(sd.isMultilingual(), (GenericSkeletonWriter) writer, sd.getEncoding(), sd.getLocale());
//		}
//		em = sd.getFilterWriter().getEncoderManager();
//		writer.processStartDocument(sd.getLocale(), sd.getEncoding(), null, em, sd);
//		return super.handleStartDocument(event);
//	}
//	
//	private void convertTu(ITextUnit tu, MultiEvent me) {
//		// After the resource simplifier tu skeleton can contain only a content placeholder and no refs in codes
//		String str = writer.processTextUnit(tu);
//		ITextUnit newTu = new TextUnit(tu.getId());
//		newTu.setIsTranslatable(false);
//		
//		me.addEvent(new Event(EventType.TEXT_UNIT, newTu));
//		me.addEvent(new Event(EventType.DOCUMENT_PART, new DocumentPart(String.format("dp_%s_conv", tu.getId()), false, new GenericSkeleton(str))));
//	}
//	
//	@Override
//	public Event handleEvent(Event event) {
//		IResource res = event.getResource();
//		if (res instanceof IReferenceable && ((IReferenceable) res).isReferent()) {
//			simplifier.convert(event); // To store a reference
//		}
//		return super.handleEvent(event);
//	}
//	
//	@Override
//	protected Event processFiltered(Event tuEvent) {
//		ITextUnit tu = tuEvent.getTextUnit();
//		if (tu.isReferent()) return tuEvent;
//		
//		Event e = simplifier.convert(tuEvent);
//		MultiEvent newMe = new MultiEvent();
//		if (e.isTextUnit()) {
//			convertTu(tu, newMe);
//		}
//		else if (e.isMultiEvent()) {			
//			MultiEvent me = e.getMultiEvent();			
//			for (Event event : me) {
//				if (event.isTextUnit()) {
//					tu = event.getTextUnit();
//					convertTu(tu, newMe);					
//				}
//				else {
//					newMe.addEvent(event);
//				}					
//			}
//		}
//		return new Event(EventType.MULTI_EVENT, newMe);
//	}
}
