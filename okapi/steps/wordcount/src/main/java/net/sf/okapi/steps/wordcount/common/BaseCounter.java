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

import net.sf.okapi.common.IResource;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.IWithAnnotations;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextUnitUtil;

abstract public class BaseCounter {

	abstract protected long doCountImpl(String text, LocaleId language);
	
	public long doCount(Object text, LocaleId language) {
	
		if (text == null) return 0L;
		if (Util.isNullOrEmpty(language)) return 0L;
		
		if (text instanceof ITextUnit) {		
			ITextUnit tu = (ITextUnit)text;
			
//			if (tu.hasTarget(language))
//				return count(classRef, tu.getTarget(language), language);
//			else
			// Only words in the source are counted
			return doCount(tu.getSource(), language);
		} 
		else if (text instanceof Segment) {
			Segment seg = (Segment) text;
			return doCount(seg.getContent(), language);
		}
		else if (text instanceof TextContainer) {
			// This work on segments' content (vs. parts' content)
			TextContainer tc = (TextContainer) text;
			long res = 0;
			for ( Segment seg : tc.getSegments() ) {
				res += doCount(seg, language);
			}
			return res;
		}
		else if (text instanceof TextFragment) {			
			TextFragment tf = (TextFragment) text;
			
			return doCount(TextUnitUtil.getText(tf), language);
		}
		else if (text instanceof String) {						
			return doCountImpl((String) text, language);
		}
		
		return 0;		
	}

	private static long getValue(MetricsAnnotation ma, String metricName) {
		if (ma == null) return 0;
		
		Metrics m = ma.getMetrics();
		if (m == null) return 0;
		
		return m.getMetric(metricName);
	}
	
	protected abstract String getMetricNameForRetrieval();
	
	public static long getCount(Segment segment, String metricName) {
		return getValue(segment.getAnnotation(MetricsAnnotation.class), metricName);
	}
	
	public long doGetCount(Segment segment) {
		return getCount(segment, getMetricNameForRetrieval());
	}
	
	public static long getCount(TextContainer tc, String metricName) {
		return getValue(tc.getAnnotation(MetricsAnnotation.class), metricName);
	}
	
	public long doGetCount(TextContainer tc) {
		return getCount(tc, getMetricNameForRetrieval());
	}
	
	public static long getCount(IWithAnnotations res, String metricName) {
		return getValue(res.getAnnotation(MetricsAnnotation.class), metricName);
	}
	
	public long doGetCount(IWithAnnotations res) {
		return getCount(res, getMetricNameForRetrieval());
	}
}
