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

package net.sf.okapi.steps.wordcount.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.annotation.AltTranslation;
import net.sf.okapi.common.annotation.AltTranslationsAnnotation;
import net.sf.okapi.common.query.MatchType;
import net.sf.okapi.common.resource.ISegments;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.steps.wordcount.WordCounter;

public abstract class AltAnnotationBasedCountStep extends BaseCountStep {
	private volatile BaseCounter counter;
	
	// Override this to count characters instead.
	protected Class<? extends BaseCounter> getCounterClass() {
		return WordCounter.class;
	}
	
	private BaseCounter getCounter() {
		if (counter == null) {
			Logger localLogger = LoggerFactory.getLogger(AltAnnotationBasedCountStep.class);
			try {
				counter = (BaseCounter) getCounterClass().newInstance();
			} catch (InstantiationException e) {
				localLogger.debug("Counter instantiation failed: {}", e.getMessage());
			} catch (IllegalAccessException e) {
				localLogger.debug("Counter instantiation failed: {}", e.getMessage());
			}
		}
		return counter;
	}
	
	abstract protected boolean accept(MatchType type);
	
	private boolean acceptATA(AltTranslationsAnnotation ata) {
		if (ata == null) return false;
		
//		for (AltTranslation at : ata) {
//			if (at == null) continue;
//			
//			MatchType type = at.getType();
//			if (accept(type)) return true;
//		}
		AltTranslation at = ata.getFirst(); // The top match only
		if (at != null) {
			MatchType type = at.getType();
			if (accept(type)) return true;
		}
		return false;
	}

	@Override
	protected long count(TextContainer textContainer, LocaleId locale) {
		long count = getCounter().doGetCount(getSource());
		if (count == 0) // No metrics found on the container
			count = getCounter().doCount(getSource(), locale); // Count metrics are based on counting in source
		return count;

	}

	@Override
	protected long count(Segment segment, LocaleId locale) {
		long count = getCounter().doGetCount(segment);
		if (count == 0) // No metrics found on the container
			count = getCounter().doCount(segment, locale); // Count metrics are based on counting in source
		return count;
	}

	@Override
	protected boolean countOnlyTranslatable() {
		return true;
	}

	@Override
	protected long countInTextUnit (ITextUnit textUnit) {
		if (textUnit == null) return 0;
		
		LocaleId srcLocale = getSourceLocale();
		LocaleId trgLocale = getTargetLocale();
		
		TextContainer source = textUnit.getSource();
		TextContainer target = textUnit.getTarget(trgLocale);
		if (target == null) return 0;
		
		// Individual segments metrics
		long segmentsCount = 0;
		long textContainerCount = 0;
		
		ISegments segs = target.getSegments();
		ISegments srcSegments = source.getSegments();
		if (segs != null) {
			for (Segment seg : segs) {
				if (acceptATA(seg.getAnnotation(AltTranslationsAnnotation.class))) {
					Segment srcSeg = srcSegments.get(seg.getId());
					long segCount = count(srcSeg, srcLocale);
					segmentsCount += segCount;
					saveToMetrics(seg, segCount);
				}
			}
		}
		// TC metrics
		if (acceptATA(target.getAnnotation(AltTranslationsAnnotation.class))) {
			textContainerCount = count(source, srcLocale);
			saveToMetrics(target, textContainerCount);
		}
		
		if (textContainerCount > 0) return textContainerCount;  
		if (segmentsCount > 0) return segmentsCount;
		return 0;
	}

//	@Override
//	protected CountContext getCountContext() {
//		return CountContext.CC_TARGET;
//	}
}
