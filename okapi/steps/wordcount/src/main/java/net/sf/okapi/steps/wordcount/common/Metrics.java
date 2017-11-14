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

package net.sf.okapi.steps.wordcount.common;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

import net.sf.okapi.common.Util;

/**
 * Metrics hash-table 
 * 
 * @version 0.1 07.07.2009
 */

public class Metrics extends HashSet<String> {
	
	private static final long serialVersionUID = 4568824618144120407L;
	
	private Hashtable <String, Long> metrics;
	
	public Metrics() {		
		super();		
		metrics = new Hashtable <String, Long> ();				
	}

	public void resetMetrics() {		
		if (metrics == null) return;
		metrics.clear(); // removes all metrics from the hash table, getMetric() returns 0 for not found 
	}
	
	public boolean resetMetric(String name) {		
		return setMetric(name, 0L);
	}

	/**
	 * Get the value of the metric, specified by the given symbolic name.
	 * @param name symbolic name of the metric
	 * @return value of the metric specified by the given symbolic name 
	 */
	public long getMetric(String name) {		
		if (Util.isEmpty(name)) return 0L;
		if (metrics == null) return 0L;
		
		Long res = metrics.get(name);
		
		if (res == null) res = 0L; 
		return res;
	}
	
	public boolean setMetric(String name, long value) {		
		if (Util.isEmpty(name)) return false;
		if (metrics == null) return false;
		if (value == 0) return false;
		
		metrics.put(name, value);		
		return true;
	}
	
	public boolean registerMetric(String name) {
		if (Util.isEmpty(name)) return false;
		if (metrics == null) return false;
		
		if (metrics.containsKey(name)) return false;
		metrics.put(name, 0L);
		return true;
	}
	
	public boolean unregisterMetric(String name) {
		if (Util.isEmpty(name)) return false;
		if (metrics == null) return false;
		if (!metrics.containsKey(name)) return false;
		
		metrics.remove(name);
		return true;
	}

	@Override
	public Iterator<String> iterator() {
		return metrics.keySet().iterator();
	}	
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (String name : metrics.keySet()) {
			sb.append(String.format("%s=%d ", name, metrics.get(name)));
		}
		return sb.toString();
	}
}
