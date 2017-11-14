/*===========================================================================
  Copyright (C) 2008-2010 by the Okapi Framework contributors
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

package net.sf.okapi.lib.beans.v1;

import java.util.Hashtable;

import net.sf.okapi.lib.persistence.IPersistenceSession;
import net.sf.okapi.lib.persistence.PersistenceBean;
import net.sf.okapi.steps.wordcount.common.Metrics;
import net.sf.okapi.steps.wordcount.common.MetricsAnnotation;

public class MetricsAnnotationBean extends PersistenceBean<MetricsAnnotation> {

	private Hashtable<String, Long> metrics = new Hashtable<String, Long>();
	
	@Override
	protected MetricsAnnotation createObject(IPersistenceSession session) {
		return new MetricsAnnotation();
	}

	@Override
	protected void fromObject(MetricsAnnotation obj, IPersistenceSession session) {
		Metrics m = obj.getMetrics();
		for (String name : m)
			metrics.put(name, m.getMetric(name));
	}

	@Override
	protected void setObject(MetricsAnnotation obj, IPersistenceSession session) {
		Metrics m = obj.getMetrics();
		m.resetMetrics();
		for (String name : metrics.keySet())
			m.setMetric(name, metrics.get(name));
	}

	public void setMetrics(Hashtable<String, Long> metrics) {
		this.metrics = metrics;
	}

	public Hashtable<String, Long> getMetrics() {
		return metrics;
	}

}
