/*===========================================================================
  Copyright (C) 2013 by the Okapi Framework contributors
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

package net.sf.okapi.filters.xliff.its;

import java.util.Collection;
import java.util.HashMap;

/**
 * Simple HashMap for storing ITS references for future use.
 */
public class ITSDefaultDataStore implements IITSDataStore {
	
	private HashMap<String, ITSLQICollection> lqiDataStore;
	private HashMap<String, ITSProvenanceCollection> provDataStore;

	@Override
	public void initialize(String identifier) {
		lqiDataStore = new HashMap<String, ITSLQICollection>();
		provDataStore = new HashMap<String, ITSProvenanceCollection>();
	}

	@Override
	public ITSLQICollection getLQIByURI(String uri) {
		return lqiDataStore.get(uri);
	}

	@Override
	public ITSProvenanceCollection getProvByURI(String uri) {
		return provDataStore.get(uri);
	}

	@Override
	public void save(ITSLQICollection lqi) {
		lqiDataStore.put(lqi.getURI(), lqi);
	}

	@Override
	public void save(ITSProvenanceCollection prov) {
		provDataStore.put(prov.getURI(), prov);
	}

	@Override
	public Collection<String> getStoredLQIURIs() {
		return lqiDataStore.keySet();
	}

	@Override
	public Collection<String> getStoredProvURIs() {
		return provDataStore.keySet();
	}
}